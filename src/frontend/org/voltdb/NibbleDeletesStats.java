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

package org.voltdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.voltdb.VoltTable.ColumnInfo;

public class NibbleDeletesStats extends StatsSource
{
    static class NibbleDeletesKey {
        String query; // delete query
        int pid; // partition id

        public NibbleDeletesKey (String deleteQuery, int partitionId) {
            query = deleteQuery;
            pid = partitionId;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (obj == this) return true;

            if (obj instanceof NibbleDeletesKey) {
                NibbleDeletesKey key = (NibbleDeletesKey) obj;
                if (pid == key.pid && query.equals(key.query)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int hashCode () {
            assert(query != null);
            return Integer.hashCode(pid) + query.hashCode();
        }
    }

    static class NibbleDeletesRow {
        long totalCount = 0;
        long deleteCount = 0;

        public NibbleDeletesRow (long totalTupleCount, long deletedTuplesCount) {
            totalCount = totalTupleCount;
            deleteCount = deletedTuplesCount;
        }
    }
    private Map<NibbleDeletesKey, NibbleDeletesRow> m_nibbleDeletesStats = new HashMap<>();

    /**
     * Whether to return results in intervals since polling or since the beginning
     */
    private boolean m_interval = false;

    private Map<NibbleDeletesKey, Long> m_lastDeletedTupleCountMap = new HashMap<>();

    public static final ColumnInfo NibbleDeletesColumnInfo[] =
        new ColumnInfo[] {new ColumnInfo("QUERY", VoltType.STRING),
                          new ColumnInfo("PARTITION", VoltType.INTEGER),
                          new ColumnInfo("TOTAL_COUNT", VoltType.BIGINT),
                          new ColumnInfo("DELETE_COUNT", VoltType.BIGINT),
                          new ColumnInfo("PERCENT_DELETE", VoltType.INTEGER)
    };

    public NibbleDeletesStats() {
        super(false);
    }

    @Override
    protected void populateColumnSchema(ArrayList<ColumnInfo> columns) {
        super.populateColumnSchema(columns);
        for (ColumnInfo column : NibbleDeletesColumnInfo)
        {
            columns.add(column);
        }
    }

    @Override
    protected void updateStatsRow(Object rowKey, Object[] rowValues) {
        if (rowKey instanceof NibbleDeletesKey == false) {
            // throw exception?
            return;
        }

        NibbleDeletesKey key = (NibbleDeletesKey) rowKey;
        final NibbleDeletesRow row = m_nibbleDeletesStats.get(key);

        rowValues[columnNameToIndex.get("QUERY")] = key.query;
        rowValues[columnNameToIndex.get("PARTITION")] = key.pid;
        rowValues[columnNameToIndex.get("TOTAL_COUNT")] = row.totalCount;

        long deleteCount = row.deleteCount;
        Long lastDeleteCount = m_lastDeletedTupleCountMap.get(key);
        if (m_interval && lastDeleteCount != null) {
            deleteCount -= lastDeleteCount;
        }
        m_lastDeletedTupleCountMap.put(key, row.deleteCount);
        rowValues[columnNameToIndex.get("DELETE_COUNT")] = deleteCount;

        int percent = 100;
        if (row.totalCount != 0) {
            percent = (int) (row.deleteCount * 100 / row.totalCount) ;
        }
        rowValues[columnNameToIndex.get("PERCENT_DELETE")] = percent;
        super.updateStatsRow(rowKey, rowValues);
    }

    @Override
    protected Iterator<Object> getStatsRowKeyIterator(boolean interval) {
        m_interval = interval;
        Set<NibbleDeletesKey> keys = m_nibbleDeletesStats.keySet();
        Iterator<NibbleDeletesKey> keyIterator = keys.iterator();

        return new Iterator<Object>() {
            @Override
            public boolean hasNext() {
                return keyIterator.hasNext();
            }

            @Override
            public Object next() {
                return keyIterator.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public synchronized void updateNibbleDeletesStatsRow(String query, int pid,
            long tupleCount, long deleteCount ) {
        NibbleDeletesKey key = new NibbleDeletesKey(query, pid);
        NibbleDeletesRow row = new NibbleDeletesRow(tupleCount, deleteCount);
        m_nibbleDeletesStats.put(key, row);
    }

    public synchronized void removeFinishedNibbleDeletesStats(String query, Set<Integer> partitions) {
        for (Integer pid: partitions) {
            NibbleDeletesKey key = new NibbleDeletesKey(query, pid);
            m_nibbleDeletesStats.remove(key);
            m_lastDeletedTupleCountMap.remove(key);
        }
    }
}
