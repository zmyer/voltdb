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

package org.voltdb.sysprocs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;

import org.voltcore.logging.VoltLogger;
import org.voltdb.CatalogContext;
import org.voltdb.ClientInterface.ExplainMode;
import org.voltdb.ClientResponseImpl;
import org.voltdb.NibbleDeletesStats;
import org.voltdb.ParameterSet;
import org.voltdb.TheHashinator;
import org.voltdb.VoltDB;
import org.voltdb.VoltTable;
import org.voltdb.VoltTable.ColumnInfo;
import org.voltdb.VoltType;
import org.voltdb.catalog.ColumnRef;
import org.voltdb.catalog.Index;
import org.voltdb.catalog.Table;
import org.voltdb.client.ClientResponse;
import org.voltdb.compiler.AdHocPlannedStatement;
import org.voltdb.compiler.AdHocPlannedStmtBatch;
import org.voltdb.parser.SQLLexer;
import org.voltdb.utils.CatalogUtil;

public class NibbleDeletes extends AdHocNTBase {
    protected static final VoltLogger compilerLog = new VoltLogger("COMPILER");
    protected static final VoltLogger hostLog = new VoltLogger("HOST");

    public final static Integer BATCH_DELETE_TUPLE_COUNT = 1000; // batch size of a SP transaction to delete tuples

    NibbleDeletesStats m_stats;

    // DELETE FROM table WHERE clause
    // No support of ORDER BY LIMIT OFFSET
    static class NibbleDeletesRequest {
        String m_normalizedQuery = null;
        String m_tableName = null;
        String m_whereClause = null;

        Object[] m_paramArray = null;
        Object[] m_userParams = null;
        Table m_table = null;

        String m_nibbleDeleteQuery = null;
        int m_batchSize = BATCH_DELETE_TUPLE_COUNT;

        public boolean isValidQuery() {
            if (m_normalizedQuery == null || m_tableName == null || m_whereClause == null) {
                return false;
            }
            return true;
        }
    }

    NibbleDeletesRequest m_info = new NibbleDeletesRequest();
    Map<Integer, Integer> m_partitionMap;

    static private String generateQueryInfo(String deleteQuery, NibbleDeletesRequest queryInfo) {
        if (deleteQuery == null) {
            return "No SQL statement provided for @NibbleDeletes";
        }

        final List<String> sqlStatements = SQLLexer.splitStatements(deleteQuery);
        if (sqlStatements.size() != 1) {
            return "Only one DELETE query is supported for @NibbleDeletes, but gets " +
                sqlStatements.size() + " statements: " + Arrays.toString(sqlStatements.toArray());
        }
        String normalizedQuery = sqlStatements.get(0);

        // validate Delete Query Type
        Matcher matcher = SQLLexer.matchDeleteStatement(normalizedQuery);
        if (!matcher.find()) {
            return "Only DELETE query with WHERE clause is supported for @NibbleDeletes, "
                    + "but received: " + deleteQuery;
        }

        int groupsize = matcher.groupCount();
        for (int i = 0; i < groupsize; i++) {
            queryInfo.m_normalizedQuery = matcher.group(0);
            queryInfo.m_tableName = matcher.group(1);
            queryInfo.m_whereClause = matcher.group(2);
            Matcher orderby = SQLLexer.matchOrderbyStatement(queryInfo.m_whereClause);
            if (orderby.find()) {
                return "ORDER BY clause is not supported for @NibbleDeletes: " + deleteQuery;
            }
            // If the query has a limit
            // it should generate non-content deterministic error
        }

        if (! queryInfo.isValidQuery()) {
            return "DELETE query is not supported for @NibbleDeletes: " + deleteQuery;
        }

        return null;
    }

    // generate order by columns
    // generate limit statement
    private static String generateOrderbyLimitClause(Table table, int limitSize) {
        Index uniqueIndex = null;
        Index uniqueExpressionIndex = null;
        // find primary key or unique index
        for (Index index: table.getIndexes()) {
            if (! index.getPredicatejson().isEmpty()) {
                // skip predicate index
                continue;
            }
            if (index.getUnique()) {
                if (index.getExpressionsjson().isEmpty()) {
                    uniqueIndex = index;
                    break;
                }
                uniqueExpressionIndex = index;
            }
        }

        if (uniqueIndex != null) {
            List<ColumnRef> indexedColRefs = CatalogUtil.getSortedCatalogItems(uniqueIndex.getColumns(), "index");

            String orderByStmt = " order by ";
            for (int i = 0; i < indexedColRefs.size(); i++) {
                ColumnRef cr = indexedColRefs.get(i);
                orderByStmt += cr.getColumn().getTypeName();
                if (i != indexedColRefs.size() - 1) {
                    orderByStmt += ", ";
                }
            }
            return orderByStmt + " limit " + limitSize;
        }
        assert(uniqueIndex == null);
        if (uniqueExpressionIndex == null) {
            throw new RuntimeException("At leaset one unique index is needed, but found nothing");
        }

        // TODO: generate an ORDER BY statement based on expression unique
        throw new RuntimeException("Unique expression index is not supported right now");
    }

    public static final ColumnInfo resultColumns[] =
            new ColumnInfo[] {
                    new ColumnInfo("PARTITION", VoltType.INTEGER),
                    new ColumnInfo("TOTAL_COUNT", VoltType.BIGINT),
                    new ColumnInfo("DELETE_COUNT", VoltType.BIGINT)
    };

    static Map<Integer, Integer> getPartitionMap() {
        // get partition key list
//        VoltTable vt = TheHashinator.getPartitionKeys(VoltType.INTEGER);
        VoltTable vt = TheHashinator.instance.get().getSecond().getSupplierForType(VoltType.INTEGER).get();

        // PARTITION_ID:INTEGER, PARTITION_KEY:INTEGER
        Map<Integer, Integer> partitionMap = new LinkedHashMap<>();

        vt.resetRowPosition();
        while (vt.advanceRow()) {
            assert(vt.getColumnCount() == 2);

            Integer id = (int)vt.getLong("PARTITION_ID");
            Integer key = (int)(vt.getLong("PARTITION_KEY"));
            partitionMap.put(id, key);
        }
        if (partitionMap.size() == 0) {
            throw new RuntimeException("Get no partitions result, impossible for a running cluster");
        }

        return partitionMap;
    }

    static private String validateParameterAndGetRequestInfo(ParameterSet params, NibbleDeletesRequest info) {
        if (params.size() < 1) {
            return String.format("@NibbleDeletes expects at least 1 paramter but got %d.", params.size());
        }
        info.m_paramArray = params.toArray();
        Object[] paramArray = info.m_paramArray;

        String deleteQuery = (String) paramArray[0];

        // validate input query
        String errorMsg = generateQueryInfo(deleteQuery, info);
        if (errorMsg != null) {
            return errorMsg;
        }

        CatalogContext context = VoltDB.instance().getCatalogContext();
        info.m_table = context.getTable(info.m_tableName);
        if (info.m_table == null) {
            return "DELETE statement for table " + info.m_tableName + " can not be found";
        }
        if (info.m_table.getIsreplicated()) {
            return "DELETE statement for replicated table " + info.m_tableName +
                   " is not supported for @NibbleDeletes";
        }

        // update the batch size
        if (paramArray.length > 1) {
            if (paramArray[1] == null) {
                return "Expect Integer parameter but received NULL";
            }
            if ((Integer) paramArray[1] == null) {
                return "Expect Integer parameter but received " + paramArray[1];
            }
            int deleteBatchSize = (int) paramArray[1];
            if (deleteBatchSize <= 0) {
                return "Invalid delete batch limit size " + paramArray[1];
            }
            info.m_batchSize = deleteBatchSize;
        }

        if (paramArray.length > 2) {
            info.m_userParams = Arrays.copyOfRange(paramArray, 2, paramArray.length);
        }

        return null;
    }

    private static AdHocPlannedStatement planQueryForcedSP (CatalogContext context, String query,
            Object[] userParams) throws AdHocPlanningException {
        // plan force SP AdHoc query
        AdHocPlannedStatement result = compileAdHocSQL(context,
                                                       query,
                                                       false, // force to generate SP plan
                                                       "dummy_partition_key",
                                                       ExplainMode.NONE,
                                                       false,
                                                       userParams);
        return result;
    }

    public CompletableFuture<ClientResponse> run(ParameterSet params)
            throws InterruptedException, ExecutionException {

        // check input parameters
        String errorMsg = validateParameterAndGetRequestInfo(params, m_info);
        if (errorMsg != null) {
            return quickErrorResponse( errorMsg);
        }

        // generate nibble delete query information
        String nibbleDeleteQuery = m_info.m_normalizedQuery +
                generateOrderbyLimitClause(m_info.m_table, m_info.m_batchSize);
        m_info.m_nibbleDeleteQuery = nibbleDeleteQuery;

        CatalogContext context = VoltDB.instance().getCatalogContext();
        AdHocPlannedStatement result = null;
        try {
            // plan force SP AdHoc query
            result = planQueryForcedSP(context, nibbleDeleteQuery, m_info.m_userParams);
        }
        catch (AdHocPlanningException e) {
            return quickErrorResponse( e.getMessage());
        }

        // get sample partition keys for routing queries
        m_partitionMap = getPartitionMap();
        int partitionCount = m_partitionMap.size();
        m_stats = VoltDB.instance().getNibbleDeletesStatsSource();

        final Map<Integer, Long> totalCountMap;
        try {
            totalCountMap = gatherTotalTuplesPerPartitions(
                    m_info, context, m_info.m_userParams, m_partitionMap);
        } catch (AdHocPlanningException | InterruptedException | ExecutionException ex) {
            return quickErrorResponse("Error while gather total number tuples to be deleted: " + ex.getMessage());
        }

        Map<Integer,CompletableFuture<ClientResponse>> allHostResponses = new LinkedHashMap<>();
        Map<Integer,Long> deletedTuplesMap = new HashMap<>();
        CompletableFuture<ClientResponse> future = null;

        Set<Integer> finshedPartitions = new HashSet<>();
        // until all partition has finished execution (or has seen finished execution)
        while (finshedPartitions.size() != partitionCount) {
            if (isCancelled()) {
                return genereateResult(totalCountMap, deletedTuplesMap,
                        "@NibbleDeletes is cancelled");
            }

            // use partition keys to rout SP queries
            for (Entry<Integer, Integer> entry : m_partitionMap.entrySet()) {
                if (isCancelled()) {
                    return genereateResult(totalCountMap, deletedTuplesMap,
                            "@NibbleDeletes is cancelled");
                }
                Integer pid = entry.getKey();
                Integer sampleKey = entry.getValue();

                if (allHostResponses.containsKey(pid)) {
                    future = allHostResponses.get(pid);
                    try {
                        ClientResponse cr = future.get(100, TimeUnit.MILLISECONDS);

                        Long count = cr.getResults()[0].asScalarLong();
                        Long newDeletedCount = count;
                        if (deletedTuplesMap.containsKey(pid)) {
                            Long totalCount = deletedTuplesMap.get(pid);
                            // replace the old value
                            newDeletedCount += totalCount;
                        }
                        deletedTuplesMap.put(pid, newDeletedCount);

                        m_stats.updateNibbleDeletesStatsRow(nibbleDeleteQuery, pid,
                                totalCountMap.get(pid), newDeletedCount);

                        if (count == 0) {
                            // this partition has no tuples to be deleted, mark it done
                            // however, there is no harm to keep running the SP AdHoc deletes
                            // as there may be more new tuples inserted satisfying the requirement.
                            finshedPartitions.add(pid);
                        }

                    } catch (TimeoutException ex) {
                        // deal with the next partition now
                        continue;
                    }
                }

                List<AdHocPlannedStatement> stmts = new ArrayList<>();
                stmts.add(result);
                AdHocPlannedStmtBatch plannedStmtBatch =
                        new AdHocPlannedStmtBatch(m_info.m_userParams,
                                                  stmts,
                                                  -1,
                                                  null,
                                                  null,
                                                  new Object[]{sampleKey});
                // create the SP AdHoc transaction
                future = createAdHocTransaction(plannedStmtBatch, false);

                allHostResponses.put(pid, future);
            }
        }

        // all partition has seen no tuples available to be deleted
        // prepare the final result
        CompletableFuture<ClientResponse> f = genereateResult(totalCountMap, deletedTuplesMap, "");
        return f;
    }

    private Map<Integer, Long> gatherTotalTuplesPerPartitions(
            NibbleDeletesRequest info, CatalogContext context, Object[] userParams,
            Map<Integer, Integer> partitionMaps)
                    throws AdHocPlanningException, InterruptedException, ExecutionException {
        // plan force SP SELECT query
        String selectQuery = String.format("select count(*) from %s where %s;",
                info.m_tableName,
                info.m_whereClause);
        System.out.println(selectQuery);
        // plan select query
        AdHocPlannedStatement selectResult = planQueryForcedSP(context, selectQuery, userParams);

        Map<Integer, CompletableFuture<ClientResponse>> futureMap = new HashMap<>();
        for (Entry<Integer, Integer> entry : partitionMaps.entrySet()) {
            Integer pid = entry.getKey();
            Integer sampleKey = entry.getValue();

            List<AdHocPlannedStatement> stmts = new ArrayList<>();
            stmts.add(selectResult);
            AdHocPlannedStmtBatch plannedStmtBatch =
                    new AdHocPlannedStmtBatch(userParams,
                                              stmts,
                                              -1,
                                              null,
                                              null,
                                              new Object[]{sampleKey});
            // create the SP AdHoc transaction
            CompletableFuture<ClientResponse> future = createAdHocTransaction(plannedStmtBatch, false);
            futureMap.put(pid, future);
        }

        Map<Integer, Long> resultMap = new HashMap<>();
        for (Integer pid: futureMap.keySet()) {
            CompletableFuture<ClientResponse> future = futureMap.get(pid);
            ClientResponse cr;
            cr = future.get();
            Long tupleCount = cr.getResults()[0].asScalarLong();
            resultMap.put(pid, tupleCount);

            // update the initial statistic row
            m_stats.updateNibbleDeletesStatsRow(info.m_nibbleDeleteQuery, pid, tupleCount, 0);
        }

        return resultMap;
    }

    protected CompletableFuture<ClientResponse> quickErrorResponse(String msg) {
        ClientResponseImpl cri = new ClientResponseImpl(ClientResponse.GRACEFUL_FAILURE, new VoltTable[0], msg);
        CompletableFuture<ClientResponse> f = new CompletableFuture<>();
        f.complete(cri);

        m_stats.removeFinishedNibbleDeletesStats(m_info.m_nibbleDeleteQuery, m_partitionMap.keySet());

        return f;
    }

    private CompletableFuture<ClientResponse> genereateResult(
            Map<Integer,Long> totalCountMap,
            Map<Integer,Long> deletedTuplesMap,
            String statusString)
    {
        VoltTable resultTable = new VoltTable(resultColumns);
        for(Integer pid: deletedTuplesMap.keySet()) {
            Long totalCount = totalCountMap.get(pid);
            Long deletedCount = deletedTuplesMap.get(pid);

            resultTable.addRow(pid, totalCount, deletedCount);
        }

        ClientResponseImpl cri = new ClientResponseImpl(ClientResponse.SUCCESS,
                new VoltTable[]{resultTable}, statusString);
        CompletableFuture<ClientResponse> f = new CompletableFuture<>();
        f.complete(cri);

        m_stats.removeFinishedNibbleDeletesStats(m_info.m_nibbleDeleteQuery, m_partitionMap.keySet());

        return f;
    }
}
