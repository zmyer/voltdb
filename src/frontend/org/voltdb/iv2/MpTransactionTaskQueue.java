/* This file is part of VoltDB.
 * Copyright (C) 2008-2017 VoltDB Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with VoltDB.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.voltdb.iv2;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.voltcore.logging.VoltLogger;
import org.voltcore.utils.CoreUtils;
import org.voltcore.utils.Pair;
import org.voltdb.CatalogContext;
import org.voltdb.exceptions.TransactionRestartException;
import org.voltdb.messaging.FragmentResponseMessage;
import org.voltdb.messaging.FragmentTaskMessage;

/**
 * Provide an implementation of the TransactionTaskQueue specifically for the MPI.
 * This class will manage separating the stream of reads and writes to different
 * Sites and block appropriately so that reads and writes never execute concurrently.
 */
public class MpTransactionTaskQueue extends TransactionTaskQueue
{
    protected static final VoltLogger tmLog = new VoltLogger("TM");

    // Track the current writes and reads in progress.  If writes contains anything, reads must be empty,
    // and vice versa
    private final Map<Long, TransactionTask> m_currentMpWrites = new HashMap<Long, TransactionTask>();
    private final Map<Long, TransactionTask> m_currentMpReads = new HashMap<Long, TransactionTask>();

    private Deque<TransactionTask> m_backlog = new ArrayDeque<TransactionTask>();

    private Deque<Pair<TransactionTask, Integer>> m_priorityBacklog = new ArrayDeque<>();

    private MpRoSitePool m_sitePool = null;

    private final Map<Long, List<Integer>> m_npTxnIdToPartitions = new HashMap<>();

    private final Map<Integer, Map<Long, TransactionTask>> m_currentNpTxnsByPartition = new HashMap<>();
    private final int MAX_TASK_DEPTH = 20;
    private final int MAX_TRIED_TIMES = 5;

    MpTransactionTaskQueue(SiteTaskerQueue queue)
    {
        super(queue);
    }

    void setMpRoSitePool(MpRoSitePool sitePool)
    {
        m_sitePool = sitePool;
    }

    synchronized void updateCatalog(String diffCmds, CatalogContext context)
    {
        m_sitePool.updateCatalog(diffCmds, context);
    }

    synchronized void updateSettings(CatalogContext context)
    {
        m_sitePool.updateSettings(context);
    }

    void shutdown()
    {
        if (m_sitePool != null) {
            m_sitePool.shutdown();
        }
    }

    /**
     * Stick this task in the backlog.
     * Many network threads may be racing to reach here, synchronize to
     * serialize queue order.
     * Always returns true in this case, side effect of extending
     * TransactionTaskQueue.
     */
    @Override
    synchronized boolean offer(TransactionTask task)
    {
        Iv2Trace.logTransactionTaskQueueOffer(task);
        m_backlog.addLast(task);
        taskQueueOffer();
        return true;
    }

    // repair is used by MPI repair to inject a repair task into the
    // SiteTaskerQueue.  Before it does this, it unblocks the MP transaction
    // that may be running in the Site thread and causes it to rollback by
    // faking an unsuccessful FragmentResponseMessage.
    synchronized void repair(SiteTasker task, List<Long> masters, Map<Integer, Long> partitionMasters, boolean balanceSPI)
    {
        // We know that every Site assigned to the MPI (either the main writer or
        // any of the MP read pool) will only have one active transaction at a time,
        // and that we either have active reads or active writes, but never both.
        // Figure out which we're doing, and then poison all of the appropriate sites.
        Map<Long, TransactionTask> currentSet;
        boolean readonly = true;
        if (!m_currentMpReads.isEmpty()) {
            assert(m_currentMpWrites.isEmpty());
            if (tmLog.isDebugEnabled()) {
                tmLog.debug("MpTTQ: repairing reads. MigratePartitionLeader:" + balanceSPI);
            }
            for (Long txnId : m_currentMpReads.keySet()) {
                m_sitePool.repair(txnId, task);
            }
            currentSet = m_currentMpReads;
        }
        else {
            if (tmLog.isDebugEnabled()) {
                tmLog.debug("MpTTQ: repairing writes. MigratePartitionLeader:" + balanceSPI);
            }
            m_taskQueue.offer(task);
            currentSet = m_currentMpWrites;
            readonly = false;
        }
        for (Entry<Long, TransactionTask> e : currentSet.entrySet()) {
            if (e.getValue() instanceof MpProcedureTask) {
                MpProcedureTask next = (MpProcedureTask)e.getValue();
                if (tmLog.isDebugEnabled()) {
                    tmLog.debug("MpTTQ: poisoning task: " + next);
                }
                next.doRestart(masters, partitionMasters);

                if (!balanceSPI || readonly) {
                    MpTransactionState txn = (MpTransactionState)next.getTransactionState();
                    // inject poison pill
                    FragmentTaskMessage dummy = new FragmentTaskMessage(0L, 0L, 0L, 0L, false, false, false);
                    FragmentResponseMessage poison =
                            new FragmentResponseMessage(dummy, 0L); // Don't care about source HSID here
                    // Provide a TransactionRestartException which will be converted
                    // into a ClientResponse.RESTART, so that the MpProcedureTask can
                    // detect the restart and take the appropriate actions.
                    TransactionRestartException restart = new TransactionRestartException(
                            "Transaction being restarted due to fault recovery or shutdown.", next.getTxnId());
                    restart.setMisrouted(false);
                    poison.setStatus(FragmentResponseMessage.UNEXPECTED_ERROR, restart);
                    txn.offerReceivedFragmentResponse(poison);
                    if (tmLog.isDebugEnabled()) {
                        tmLog.debug("MpTTQ: restarting:" + next);
                    }
                }
            }
        }
        // Now, iterate through the backlog and update the partition masters
        // for all ProcedureTasks
        Iterator<TransactionTask> iter = m_backlog.iterator();
        while (iter.hasNext()) {
            TransactionTask tt = iter.next();
            if (tt instanceof MpProcedureTask) {
                MpProcedureTask next = (MpProcedureTask)tt;

                if (tmLog.isDebugEnabled()) {
                    tmLog.debug("Repair updating task: " + next + " with masters: " + CoreUtils.hsIdCollectionToString(masters));
                }
                next.updateMasters(masters, partitionMasters);
            }
            else if (tt instanceof EveryPartitionTask) {
                EveryPartitionTask next = (EveryPartitionTask)tt;
                if (tmLog.isDebugEnabled())  {
                    tmLog.debug("Repair updating EPT task: " + next + " with masters: " + CoreUtils.hsIdCollectionToString(masters));
                }
                next.updateMasters(masters);
            }
        }
    }

    private void taskQueueOffer(TransactionTask task)
    {
        Iv2Trace.logSiteTaskerQueueOffer(task);
        if (task instanceof NpProcedureTask || !task.getTransactionState().isReadOnly()) {
            m_taskQueue.offer(task);
        } else {
            m_sitePool.doWork(task.getTxnId(), task);
        }
    }

    private boolean allowToRun(MpTransactionState state, boolean isNpTxn) {
        if (!m_currentMpWrites.isEmpty()) {
            return false;
        }

        if (isNpTxn) {
            Set<Integer> partitions = state.getInvolvedPartitionIds();
            for (Integer pid: partitions) {
                Map<Long, TransactionTask> partitionMap = m_currentNpTxnsByPartition.get(pid);
                if (partitionMap != null && !partitionMap.isEmpty()) {
                    return false;
                }
            }
        } else {
            for (Map<Long, TransactionTask> partitionMap : m_currentNpTxnsByPartition.values()) {
                if (!partitionMap.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private TransactionTask pollFirstTask(boolean isPriorityTask) {
        if (isPriorityTask) {
            Pair<TransactionTask, Integer> item = m_priorityBacklog.pollFirst();
            if (item == null) return null;
            return item.getFirst();
        } else {
            return m_backlog.pollFirst();
        }
    }

    private boolean taskQueueOfferInternal(TransactionTask task, boolean isPriorityTask) {
        boolean isReadOnly = task.getTransactionState().isReadOnly();
        boolean isNpTxn = task instanceof NpProcedureTask;
        MpTransactionState state = ((MpTransactionState) task.getTransactionState());

        // read only task optimization for MP reads currently
        // no 2p read only pool yet for further optimization
        if (! allowToRun(state, isNpTxn)) {
            return false;
        }
        tmLog.error(TxnEgo.txnIdToString(task.getTxnId()) + " is NpTxn " + isNpTxn + ", read only " + isReadOnly);

        // Np task or MP write task
        if (isNpTxn || !isReadOnly) {
            if (!m_currentMpReads.isEmpty()) {
                // there are mp reads not drained yet, can not run any write
                return false;
            }
            if (isNpTxn) {
                Set<Integer> partitions = state.getInvolvedPartitionIds();
                for (Integer pid: partitions) {
                    Map<Long, TransactionTask> txnsMap = m_currentNpTxnsByPartition.get(pid);
                    if (txnsMap == null) {
                        txnsMap = new HashMap<Long, TransactionTask>();
                    }
                    txnsMap.put(task.getTxnId(), task);
                    m_currentNpTxnsByPartition.put(pid, txnsMap);
                }
                m_npTxnIdToPartitions.put(task.getTxnId(), new ArrayList<>(partitions));
            } else {
                m_currentMpWrites.put(task.getTxnId(), task);
            }

            task = pollFirstTask(isPriorityTask);
            taskQueueOffer(task);
            return true;
        }

        boolean retval = false;
        while (task != null && task.getTransactionState().isReadOnly() &&
                m_sitePool.canAcceptWork())
        {
            task = m_backlog.pollFirst();
            assert(task.getTransactionState().isReadOnly());
            m_currentMpReads.put(task.getTxnId(), task);
            taskQueueOffer(task);
            retval = true;
            // Prime the pump with the head task, if any.  If empty,
            // task will be null
            task = pollFirstTask(isPriorityTask);
        }
        return retval;
    }

    /**
     *
     * @return how many tasks get offered
     */
    private int taskQueueOffer()
    {
        tmLog.warn("[taskQueueOffer]: " + this.toString());

        int tasksTaken = 0;
        if (m_priorityBacklog.isEmpty() && m_backlog.isEmpty()) {
            return tasksTaken;
        }
        TransactionTask task;
        // check priority backlog first
        int counts = m_priorityBacklog.size();
        for (int i = 0; i < counts && !m_priorityBacklog.isEmpty(); i++) {
            if (!m_currentMpWrites.isEmpty()) {
                return tasksTaken;
            }

            Pair<TransactionTask, Integer> item = m_priorityBacklog.peekFirst();
            task = item.getFirst();
            if (item.getSecond() > MAX_TRIED_TIMES) {
                return tasksTaken;
            }

            if (taskQueueOfferInternal(task, true)) {
                tmLog.error("[priority queue taken]: " + task);
                tasksTaken++;
                continue;
            }
            tmLog.error("[priority queue not taken, requeue it back]: " + task);
            m_priorityBacklog.pollFirst();
            m_priorityBacklog.addLast(new Pair<TransactionTask, Integer>(task, item.getSecond() + 1));
        }

        // start to process MAX_TASK_DEPTH tasks from normal backlog, stop when hitting MP task
        for (int i = 0; !m_backlog.isEmpty() && i < MAX_TASK_DEPTH; i++) {
            // early return for any schedule work if MP writes are not empty
            if (!m_currentMpWrites.isEmpty()) {
                return tasksTaken;
            }

            // We may not queue the next task, just peek to get the read-only state
            task = m_backlog.peekFirst();

            if (taskQueueOfferInternal(task, false)) {
                tasksTaken++;
                tmLog.error("[normal queue taken]: " + task);
            } else {
                tmLog.error("[normal queue not taken, queue it into priority]: " + task);
                task = m_backlog.pollFirst();
                m_priorityBacklog.add(new Pair<TransactionTask, Integer>(task, 1));
            }
        }
        return tasksTaken;
    }

    /**
     * Indicate that the transaction associated with txnId is complete.  Perform
     * management of reads/writes in progress then call taskQueueOffer() to
     * submit additional tasks to be done, determined by whatever the current state is.
     * See giant comment at top of taskQueueOffer() for what happens.
     */
    @Override
    synchronized int flush(long txnId)
    {
        int offered = 0;
        if (m_currentMpReads.containsKey(txnId)) {
            m_currentMpReads.remove(txnId);
            m_sitePool.completeWork(txnId);
        }
        else if (m_currentMpWrites.containsKey(txnId)) {
            m_currentMpWrites.remove(txnId);
            assert(m_currentMpWrites.isEmpty());
        } else {
            assert(m_npTxnIdToPartitions.containsKey(txnId));
            List<Integer> partitions = m_npTxnIdToPartitions.get(txnId);
            for (Integer pid: partitions) {
                Map<Long, TransactionTask> txnsMap = m_currentNpTxnsByPartition.get(pid);
                txnsMap.remove(txnId);
            }
            m_npTxnIdToPartitions.remove(txnId);
        }
        offered += taskQueueOffer();
        return offered;
    }

    /**
     * Restart the current task at the head of the queue.  This will be called
     * instead of flush by the currently blocking MP transaction in the event a
     * restart is necessary.
     */
    @Override
    synchronized void restart()
    {
        if (!m_currentMpReads.isEmpty()) {
            // re-submit all the tasks in the current read set to the pool.
            // the pool will ensure that things submitted with the same
            // txnID will go to the the MpRoSite which is currently running it
            for (TransactionTask task : m_currentMpReads.values()) {
                taskQueueOffer(task);
            }
        }
        else if (!m_currentMpWrites.isEmpty()) {
            TransactionTask task;
            // There currently should only ever be one current write.  This
            // is the awkward way to get a single value out of a Map
            task = m_currentMpWrites.entrySet().iterator().next().getValue();
            taskQueueOffer(task);
        } else {
            // restart Np transactions
            for (Long txnId: m_npTxnIdToPartitions.keySet()) {
                Integer pid = m_npTxnIdToPartitions.get(txnId).get(0);

                TransactionTask task = m_currentNpTxnsByPartition.get(pid).get(txnId);
                taskQueueOffer(task);
            }
        }
    }

    /**
     * How many Tasks are un-runnable?
     * @return
     */
    @Override
    synchronized int size()
    {
        return m_backlog.size();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("MpTransactionTaskQueue:").append("\n");
        if (!m_currentMpReads.isEmpty()) {
            sb.append("current mp reads size: " + m_currentMpReads.size()).append("\n");
        }

        if (!m_currentMpWrites.isEmpty()) {
            sb.append("current mp writes size: ").append(m_currentMpWrites.size()).append("\n");
        }

        if (!m_npTxnIdToPartitions.isEmpty()) {
            sb.append("current np transaction size: ").append(m_npTxnIdToPartitions.size()).append("\n");
            for (Long txnId: m_npTxnIdToPartitions.keySet()) {
                sb.append("\tTxn ").append(TxnEgo.txnIdToString(txnId)).append(" -> ");
                m_npTxnIdToPartitions.get(txnId).forEach(item -> sb.append(item).append(" "));
            }
            sb.append("\n");
            for (Integer pid: m_currentNpTxnsByPartition.keySet()) {
                sb.append("\tPartition ").append(pid).append(" -> ");
                m_currentNpTxnsByPartition.get(pid).keySet().forEach(
                        txnId -> sb.append(TxnEgo.txnIdToString(txnId)).append(" "));
            }
            sb.append("\n");
        }

        if (!m_priorityBacklog.isEmpty()) {
            sb.append("\tPriority queue HEAD: ").append(m_priorityBacklog.getFirst().getFirst())
                .append(" tried with ").append(m_priorityBacklog.getFirst().getSecond()).append("\n");
        }

        sb.append("\tBacklog SIZE: ").append(m_backlog.size()).append("\n");
        if (!m_backlog.isEmpty()) {
            sb.append("\tbacklog queue HEAD: ").append(m_backlog.getFirst()).append("\n");
        }
        return sb.toString();
    }
}
