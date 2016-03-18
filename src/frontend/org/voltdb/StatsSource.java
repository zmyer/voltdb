/* This file is part of VoltDB.
 * Copyright (C) 2008-2016 VoltDB Inc.
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
package org.voltdb;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.voltcore.logging.VoltLogger;
import org.voltdb.AuthSystem.AuthUser;

import org.voltdb.VoltTable.ColumnInfo;
import org.voltdb.client.BatchTimeoutOverrideType;
import org.voltdb.client.ClientResponse;
import org.voltdb.client.HashinatorLite;
import org.voltdb.client.SyncCallback;

/**
 * Abstract superclass of all sources of statistical information inside the Java frontend.
 */
public abstract class StatsSource {

    private static final VoltLogger m_logger = new VoltLogger("HOST");

    private final Integer m_hostId;
    private final String m_hostname;

    /**
     * Statistics from ee are already formatted in VoltTable
     */
    private final boolean m_isEEStats;

    //Volatile for safe publication of the table objects
    private volatile VoltTable m_table = null;
    private final String m_name;
    /**
     * Column schema for statistical result rows
     */
    private final ArrayList<ColumnInfo> columns = new ArrayList<ColumnInfo>();

    /**
     * Map from the name of a column to its index in the a result row. In
     * order to decouple the contributions of each class in the inheritance
     * hierarchy from the integer index in the result row this map is used for
     * lookups instead of hard coding an index.
     */
    protected final HashMap<String, Integer> columnNameToIndex = new HashMap<String, Integer>();

    /**
     * Initialize this source of statistical information with the specified
     * name. Populate the column schema by calling populateColumnSchema on the
     * derived class and use it to populate the columnNameToIndex map.
     * @param name
     * @param isEE If this source represents statistics from EE
     */
    public StatsSource(String name, boolean isEE) {
        m_name = name;
        populateColumnSchema(columns);

        for (int ii = 0; ii < columns.size(); ii++) {
            columnNameToIndex.put(columns.get(ii).name, ii);
        }

        String hostname = "";
        int hostId = 0;
        if (VoltDB.instance() != null) {
            if (VoltDB.instance().getHostMessenger() != null) {
                hostname = VoltDB.instance().getHostMessenger().getHostname();
                hostId = VoltDB.instance().getHostMessenger().getHostId();
            }
        }
        m_hostname = hostname;
        m_hostId = hostId;

        // Fill in an empty table for m_table so we're not returning null before
        // tick() populates stats (in the EE cases, at least)
        resetStatsTable();

        m_isEEStats = isEE;
    }

    /**
     * Called from the constructor to generate the column schema at run time.
     * Derived classes need to override this method in order to specify the
     * columns they will be adding. The first line must always be a call the
     * superclasses version of populateColumnSchema
     * in order to ensure the columns are add to the list in the right order.
     * @param columns Output list for the column schema.
     */
    protected void populateColumnSchema(ArrayList<ColumnInfo> columns) {
        columns.add(new ColumnInfo("TIMESTAMP", VoltType.BIGINT));
        columns.add(new ColumnInfo(VoltSystemProcedure.CNAME_HOST_ID,
                                   VoltSystemProcedure.CTYPE_ID));
        columns.add(new ColumnInfo("HOSTNAME", VoltType.STRING));
    }

    /**
     * Retrieve the already constructed column schema
     * @return List of column names and types
     */
    public ArrayList<ColumnInfo> getColumnSchema() {
        return columns;
    }

    public String getPartitionColumn() {
        return "TIMESTAMP";
    }

    public VoltType getPartitionColumnType() {
        return VoltType.BIGINT;
    }

    public int getPartitionColumnIndex() {
        return 0;
    }

    /**
     * Get the latest stat values as an array of arrays of objects suitable for
     * insertion into an VoltTable
     * @param interval Whether to get stats since the beginning or since the
     * last time stats were retrieved
     * @return Array of Arrays of objects containing the latest values
     */
    public Object[][] getStatsRows(boolean interval, final Long now) {
        this.now = now;
        /*
         * Synchronizing on this allows derived classes to maintain thread safety
         */
        synchronized (this) {
            Iterator<Object> i = getStatsRowKeyIterator(interval);
            ArrayList<Object[]> rows = new ArrayList<Object[]>();
            while (i.hasNext()) {
                Object rowKey = i.next();
                Object rowValues[] = new Object[columns.size()];
                updateStatsRow(rowKey, rowValues);
                rows.add(rowValues);
            }
            return rows.toArray(new Object[0][]);
        }
    }

    /**
     * If this source contains statistics from EE. EE statistics are already
     * formatted in VoltTable, so use getStatsTable() to get the result.
     */
    public boolean isEEStats() {
        return m_isEEStats;
    }

    /**
     * For some sources like TableStats, they use VoltTable to keep track of
     * statistics. This method will return it directly.
     *
     * @return If the return value is null, you should fall back to using
     *         getStatsRows()
     */
    public VoltTable getStatsTable() {
        //Create a view for thread safety even though stats are retrieved single threaded right now
        return new VoltTable(m_table.getBuffer(), true);
    }

    /**
     * Sets the VoltTable which contains the statistics. Only sources which use
     * VoltTable to keep track of statistics need to use this.
     *
     * @param statsTable
     *            The VoltTable which contains the statistics.
     */
    public void setStatsTable(VoltTable statsTable) {
        m_table = statsTable;
    }

    /**
     * Reset the VoltTable which contains the statistics.  Only sources which use
     * VoltTable to keep track of statistics need to use this.  Allows
     * clients to reset the tracking table without having to build an empty stats table and call setStatsTable()
     */
    public void resetStatsTable() {
        m_table = new VoltTable(columns.toArray(new ColumnInfo[columns.size()]));
    }

    public VoltTable.ColumnInfo[] getColumns() {
        return columns.toArray(new ColumnInfo[columns.size()]);
    }

    private Long now = System.currentTimeMillis();

    /**
     * Update the parameter array with the latest values. This is similar to
     * populateColumnSchema in that it must be overriden by derived classes and
     * the derived class implementation must call the super classes
     * implementation.
     * @param rowKey Key identifying the specific row to be populated
     * @param rowValues Output parameter. Array of values to be updated.
     */
    protected void updateStatsRow(Object rowKey, Object rowValues[]) {
        rowValues[0] = now;
        rowValues[1] = m_hostId;
        rowValues[2] = m_hostname;
    }

    /**
     * Retrieve an iterator that iterates over the keys identifying all unique
     * stats rows available for retrieval from the stats source
     * @param interval Whether return stats that are recorded from the
     * beginning or since the last time they were iterated
     * @return Iterator of Objects representing keys that identify unique stats rows
     */
    abstract protected Iterator<Object> getStatsRowKeyIterator(boolean interval);

    private InternalConnectionHandler getConnectionHadler()
    {
        return VoltDB.instance().getClientInterface().getInternalConnectionHandler();
    }

    private AuthUser getInternalUser()
    {
        return VoltDB.instance().getCatalogContext().authSystem.getInternalAdminUser();
    }

    private static final Map<String, AtomicBoolean> m_tableBuiltMap = new HashMap<>();
    protected void buildStatsTable() {
        AtomicBoolean built = null;
        synchronized(m_tableBuiltMap) {
            String name = this.getClass().toString().trim();
            built = m_tableBuiltMap.get(this.getClass().toString().trim());
            if (built == null) {
                built = new AtomicBoolean(false);
                m_tableBuiltMap.put(name, built);
            }
        }

        if (built.get()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(m_name).append(" (\n");
        int i = 0;
        String pkeyStr = "";
        for (ColumnInfo c : columns) {
            sb.append("COL_").append(c.name).append(" ").append(c.type.toSQLString());
            if (c.name.equalsIgnoreCase(getPartitionColumn())) {
                sb.append(" NOT NULL");
                pkeyStr = new StringBuilder().append(",\n CONSTRAINT PK_").append(m_name).append(" PRIMARY KEY ( COL_").append(c.name).append(")\n").toString();
            }
            sb = (i < columns.size()-1) ? sb.append(",\n") : sb.append(pkeyStr);
            i++;
        }
        sb.append(");\n");
        sb.append("PARTITION TABLE ").append(m_name).append(" ON COLUMN ").append("COL_").append(getPartitionColumn()).append(";");
        SyncCallback cb = new SyncCallback();
        if (!getConnectionHadler().callProcedure(getInternalUser(), true, BatchTimeoutOverrideType.NO_TIMEOUT, cb, "@AdHoc", sb.toString())) {
            built.set(false);
            return;
        }
        try {
            cb.waitForResponse();
        } catch (InterruptedException e) {
            m_logger.error("Interrupted while pausing cluster for resource overusage", e);
            return;
        }
        ClientResponseImpl r = ClientResponseImpl.class.cast(cb.getResponse());
        if (r.getStatus() != ClientResponse.SUCCESS) {
            if (r.getStatusString().contains("object name already exists")) {
                synchronized(m_tableBuiltMap) {
                    String name = this.getClass().toString().trim();
                    built = m_tableBuiltMap.get(this.getClass().toString().trim());
                    if (built == null) {
                        built = new AtomicBoolean(true);
                        m_tableBuiltMap.put(name, built);
                    } else {
                        built.set(true);
                    }
                }
            }
            return;
        }
        m_logger.info("Built Table: " + sb.toString());
        built.set(true);
    }

    protected boolean isTableBuilt() {
        AtomicBoolean built = m_tableBuiltMap.get(this.getClass().toString().trim());
        if (built == null) {
            return false;
        }
        return built.get();
    }

    protected boolean skipCollection() {
//        String name = this.getClass().toString().trim();
//        if (name.contains("TableStats"))
//            return true;
        return false;
    }

    public VoltTable[] splitTables(VoltTable values) {
        VoltTable[] tables = new VoltTable[1];
        tables[0] = values;
        return tables;
    }

    protected boolean persistStats(VoltTable values, boolean sync) {
        if (!isTableBuilt() || skipCollection()) {
            return false;
        }
        if (values == null || values.getRowCount() <= 0) {
            return false;
        }
        VoltTable[] tables = splitTables(values);
        for (int i = 0; i < tables.length; i++) {
            SyncCallback cb = new SyncCallback();
            Object rpartitionParam = HashinatorLite.valueToBytes(tables[i].fetchRow(0).get(getPartitionColumnIndex(), getPartitionColumnType()));
            if (!getConnectionHadler().callProcedure(getInternalUser(), true, BatchTimeoutOverrideType.NO_TIMEOUT, cb,
                    "@LoadSinglepartitionTable", rpartitionParam, m_name, (byte )1, tables[i])) {
                return false;
            }
            if (sync) {
                try {
                    cb.waitForResponse();
                } catch (InterruptedException e) {
                    m_logger.error("Interrupted while pausing cluster for resource overusage", e);
                    return false;
                }
                ClientResponseImpl r = ClientResponseImpl.class.cast(cb.getResponse());
                if (r.getStatus() != ClientResponse.SUCCESS) {
                    m_logger.info("Failed to insert: " + r.getStatusString());
                    return false;
                }
            }
        }
        return true;
    }

}
