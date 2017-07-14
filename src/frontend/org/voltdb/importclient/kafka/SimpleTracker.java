package org.voltdb.importclient.kafka;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongBinaryOperator;

import org.voltdb.importer.CommitTracker;

//Simple tracker used for timed based commit.
final class SimpleTracker implements CommitTracker {
    private final AtomicLong m_commitPoint = new AtomicLong(-1);
    @Override
    public void submit(long offset) {
        //NoOp
    }

    @Override
    public long commit(long commit) {
        return m_commitPoint.accumulateAndGet(commit, new LongBinaryOperator() {
            @Override
            public long applyAsLong(long orig, long newval) {
                return (orig > newval) ? orig : newval;
            }
        });
    }

    @Override
    public void resetTo(long offset) {
        m_commitPoint.set(offset);
    }
}