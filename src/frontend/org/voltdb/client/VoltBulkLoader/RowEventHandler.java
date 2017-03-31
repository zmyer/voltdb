package org.voltdb.client.VoltBulkLoader;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceReportingEventHandler;
import org.voltcore.logging.VoltLogger;
import org.voltdb.ClientResponseImpl;
import org.voltdb.ParameterConverter;
import org.voltdb.VoltTable;
import org.voltdb.VoltType;
import org.voltdb.VoltTypeException;
import org.voltdb.client.ClientImpl;
import org.voltdb.client.ClientResponse;
import org.voltdb.client.ProcedureCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;


public class RowEventHandler implements
        SequenceReportingEventHandler<RowEvent> {
    private final String m_name;
    long preSequnce = 0L;
    private Sequence sequenceCallback;  // TODO: use for the batchEventProcessor
    //Table used to build up requests to the PartitionProcessor
    final VoltTable.ColumnInfo m_columnInfo[];
    VoltType[] m_columnTypes;
    VoltTable m_table;
    ArrayList<VoltBulkLoaderRow> m_buf;
    final boolean m_isMP;
    final String m_procName;
    final byte m_upsert;
    final ClientImpl m_clientImpl;
    final String m_tableName;
    //Zero based index of the partitioned column in the table
    final int m_partitionedColumnIndex;
    //Partitioned column type
    final VoltType m_partitionColumnType;
    final ExecutorService m_es;
    private static final VoltLogger loaderLog = new VoltLogger("LOADER");

    public RowEventHandler(ClientImpl clientImpl,VoltTable.ColumnInfo columnInfo[], VoltType[] columnTypes, String tableName,
                           int batchSize, boolean isMP, String procName, byte upsert, ExecutorService es,
                           int partitionedColumnIndex, VoltType partitionColumnType, String name) {
        m_columnInfo =  columnInfo;
        m_table = new VoltTable(columnInfo);
        m_columnTypes = columnTypes;
        m_buf = new ArrayList<VoltBulkLoaderRow>(batchSize);
        m_isMP = isMP;
        m_procName = procName;
        m_upsert = upsert;
        m_clientImpl = clientImpl;
        m_tableName = tableName;
        m_partitionedColumnIndex = partitionedColumnIndex;
        m_partitionColumnType = partitionColumnType;
        m_es = es;
        m_name = name;
    }

    @Override
    public void onEvent(RowEvent row, long sequence, boolean endOfBatch) throws Exception {
        addToTable(row);
        if (endOfBatch) {
            loaderLog.debug("SequenceSpan for " + m_name + " : " + (sequence - preSequnce));
            preSequnce = sequence;
            // System.out.println("Event: " + row.m_row.m_rowHandle + " sequence: " + sequence + " endOfBatch: " + endOfBatch);
            loadTable(new PartitionProcedureCallback(m_buf),m_table);
            m_buf.clear();
        }
    }

    // Callback for batch submissions to the Client. A failed request submits the entire
    // batch of rows to m_failedQueue for row by row processing on m_failureProcessor.
    class PartitionProcedureCallback implements ProcedureCallback {
        final List<VoltBulkLoaderRow> m_batchRowList;

        PartitionProcedureCallback(List<VoltBulkLoaderRow> batchRowList) {
            m_batchRowList = new ArrayList<VoltBulkLoaderRow>(batchRowList);
        }

        // Called by Client to inform us of the status of the bulk insert.
        @Override
        public void clientCallback(ClientResponse response) throws InterruptedException {
            if (response.getStatus() != ClientResponse.SUCCESS) {
                // Queue up all rows for individual processing by originating BulkLoader's FailureProcessor.
                m_es.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            reinsertFailed(m_batchRowList);
                        } catch (Exception e) {
                            // loaderLog.error("Failed to re-insert failed batch", e);
                        }
                    }
                });
            }
            else {
                m_batchRowList.get(0).m_loader.m_loaderCompletedCnt.addAndGet(m_batchRowList.size());
                m_batchRowList.get(0).m_loader.m_outstandingRowCount.addAndGet(-1 * m_batchRowList.size());
            }
        }
    }

    private void reinsertFailed(List<VoltBulkLoaderRow> rows) throws Exception {
        VoltTable tmpTable = new VoltTable(m_columnInfo);
        for (final VoltBulkLoaderRow row : rows) {
            // No need to check error here if a correctedLine has come here it was
            // previously successful.
            try {
                Object row_args[] = new Object[row.m_rowData.length];
                for (int i = 0; i < row_args.length; i++) {
                    final VoltType type = m_columnTypes[i];
                    row_args[i] = ParameterConverter.tryToMakeCompatible(type.classFromType(),
                            row.m_rowData[i]);
                }
                tmpTable.addRow(row_args);
            } catch (VoltTypeException ex) {
                // Should never happened because the bulk conversion in PerPartitionProcessor
                // should have caught this
                continue;
            }

            ProcedureCallback callback = new ProcedureCallback() {
                @Override
                public void clientCallback(ClientResponse response) throws Exception {
                    row.m_loader.m_loaderCompletedCnt.incrementAndGet();
                    row.m_loader.m_outstandingRowCount.decrementAndGet();

                    //one insert at a time callback
                    if (response.getStatus() != ClientResponse.SUCCESS) {
                        row.m_loader.m_notificationCallBack.failureCallback(row.m_rowHandle, row.m_rowData, response);
                    }
                }
            };

            loadTable(callback, tmpTable);
        }
    }

    @Override
    public void setSequenceCallback(Sequence sequence) {

    }

    // TODO: dynamic change batchsize
    boolean updateMinBatchTriggerSize(int minBatchTriggerSize) {
        return false;
    }

    private void addToTable(RowEvent row) {
        VoltBulkLoaderRow currRow = row.m_row;
        m_buf.add(currRow);

        VoltBulkLoader loader = currRow.m_loader;
        Object row_args[];
        row_args = new Object[currRow.m_rowData.length];
        try {
            for (int i = 0; i < row_args.length; i++) {
                final VoltType type = m_columnTypes[i];
                row_args[i] = ParameterConverter.tryToMakeCompatible(type.classFromType(),
                        currRow.m_rowData[i]);
            }
        } catch (VoltTypeException e) {
            loader.generateError(currRow.m_rowHandle, currRow.m_rowData, e.getMessage());
            loader.m_outstandingRowCount.decrementAndGet();

        }
        m_table.addRow(row_args);
    }

    private void loadTable(ProcedureCallback callback, VoltTable toSend) throws Exception {
        if (toSend.getRowCount() <= 0) {
            return;
        }

        try {
            if (m_isMP) {
                m_clientImpl.callProcedure(callback, m_procName, m_tableName, m_upsert, toSend);
            } else {
                Object rpartitionParam = VoltType.valueToBytes(toSend.fetchRow(0).get(
                        m_partitionedColumnIndex, m_partitionColumnType));
                m_clientImpl.callProcedure(callback, m_procName, rpartitionParam, m_tableName, m_upsert, toSend);
            }
        } catch (IOException e) {
            final ClientResponse r = new ClientResponseImpl(
                    ClientResponse.CONNECTION_LOST, new VoltTable[0],
                    "Connection to database was lost");
            callback.clientCallback(r);
        }
        toSend.clearRowData();
    }
}
