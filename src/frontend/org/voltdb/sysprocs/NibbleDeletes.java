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
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.voltcore.logging.VoltLogger;
import org.voltdb.CatalogContext;
import org.voltdb.ClientInterface.ExplainMode;
import org.voltdb.ClientResponseImpl;
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

    public static Integer BATCH_DELETE_TUPLE_COUNT = 1; // batch size of a SP transaction to delete tuples

    /**
     * TODO(xin):
     * @param deleteQuery
     * @return quick error response or null as valid supported query
     */
    private CompletableFuture<ClientResponse> checkQuery(String deleteQuery) {
        if (deleteQuery != null) return null;

        if (deleteQuery == null) {
            return makeQuickResponse(
                    ClientResponse.GRACEFUL_FAILURE,
                    "No SQL statement provided for @NibbleDeletes");
        }

        final List<String> sqlStatements = SQLLexer.splitStatements(deleteQuery);
        if (sqlStatements.size() != 1) {
            return makeQuickResponse(
                    ClientResponse.GRACEFUL_FAILURE,
                    "Only one DELETE query is supported for @NibbleDeletes, but gets " + sqlStatements.size()
                    + " statements: " + Arrays.toString(sqlStatements.toArray()));
        }

        // validate Delete Query Type
        if (checkQuery(deleteQuery) != null) {
            return makeQuickResponse(
                    ClientResponse.GRACEFUL_FAILURE,
                    "Only one DELETE query is supported for @NibbleDeletes");
        }

        return null;
    }

    private static String generateOrderbyLimitClause(String query, CatalogContext context) {
        // generate order by columns
        // generate limit statement

        Table table = context.getTable("P1");

        Index uniqueIndex = null;
        Index uniqueExpressionIndex = null;
        // find primary key or unique index
        for (Index index: table.getIndexes()) {
            if (index.getUnique()) {
                // TODO(xin): check predicate index as well
                if (index.getExpressionsjson().isEmpty()) {
                    uniqueIndex = index;
                    break;
                }
                uniqueExpressionIndex = index;
            }
        }

        if (uniqueIndex != null) {
            List<ColumnRef> indexedColRefs = CatalogUtil.getSortedCatalogItems(uniqueIndex.getColumns(), "index");

            // TODO(xin): check for existing order by statement

            String orderByStmt = " order by ";
            for (int i = 0; i < indexedColRefs.size(); i++) {
                ColumnRef cr = indexedColRefs.get(i);
                orderByStmt += cr.getColumn().getTypeName();
                if (i != indexedColRefs.size() - 1) {
                    orderByStmt += ", ";
                }
            }
            return query + orderByStmt + " limit " + BATCH_DELETE_TUPLE_COUNT + ";";
        }
        assert(uniqueIndex == null);
        if (uniqueExpressionIndex == null) {
            throw new RuntimeException("At leaset one unique index is needed, but found nothing");
        }

        // TODO:
        // generate an ORDER BY statement based on expression unique
        throw new RuntimeException("Unique expression index is not supported right now");
    }

    public static final ColumnInfo resultColumns[] =
            new ColumnInfo[] {
                    new ColumnInfo("PARTITION", VoltType.INTEGER),
                    new ColumnInfo("RESULT", VoltType.BIGINT)
    };

    static List<Integer> getPartitionKeys() {
        // get partition key list
        VoltTable vt = TheHashinator.getPartitionKeys(VoltType.INTEGER);
        // PARTITION_ID:INTEGER, PARTITION_KEY:INTEGER
        List<Integer> partitionKeys = new ArrayList<>();
        while (vt.advanceRow()) {
            //check for mock unit test
            if (vt.getColumnCount() == 2) {
                Integer key = (int)(vt.getLong("PARTITION_KEY"));
                partitionKeys.add(key);
            }
        }

        return partitionKeys;
    }


    // TODO: use same input as AdHoc
    public CompletableFuture<ClientResponse> run(ParameterSet params) throws InterruptedException, ExecutionException {
        if (params.size() < 1) {
            return makeQuickResponse(
                    ClientResponse.GRACEFUL_FAILURE,
                    String.format("@NibbleDeletes expects at least 1 paramter but got %d.", params.size()));
        }
        Object[] paramArray = params.toArray();
        String deleteQuery = (String) paramArray[0];

        // check input query
        CompletableFuture<ClientResponse> resultFuture = checkQuery(deleteQuery);
        if (resultFuture != null) {
            return resultFuture;
        }

        Object[] userParams = null;
        if (paramArray.length > 1) {
            userParams = Arrays.copyOfRange(paramArray, 1, paramArray.length);
        }

        CatalogContext context = VoltDB.instance().getCatalogContext();
        // table?
        // generate nibble delete query information
        String nibbleDeleteQuery = generateOrderbyLimitClause(deleteQuery, context);
        System.err.println(nibbleDeleteQuery);

        // plan force SP AdHoc query
        AdHocPlannedStatement result = null;
        try {
            result = compileAdHocSQL(context,
                                     nibbleDeleteQuery,
                                     false, // force to generate SP plan
                                     "dummy_partition_key",
                                     ExplainMode.NONE,
                                     false,
                                     userParams);
        }
        catch (AdHocPlanningException e) {
            return makeQuickResponse(ClientResponse.GRACEFUL_FAILURE, e.getMessage());
        }
        assert(result != null);

        // get sample partition keys for routing queries
        List<Integer> partitionKeys = getPartitionKeys();


        Map<Integer,CompletableFuture<ClientResponse>> allHostResponses = new LinkedHashMap<>();
        Set<Integer> finshedPartitions = new HashSet<>();
        Map<Integer,Long> deletedTuplesPerPartition = new HashMap<>();

        CompletableFuture<ClientResponse> future = null;
        int partitionCount = partitionKeys.size();

        // until all partition has finished execution (or has seen finished execution)
        while (finshedPartitions.size() != partitionCount) {
            // use partition keys to rout SP queries
            for (Integer key : partitionKeys) {
                if (allHostResponses.containsKey(key)) {
                    future = allHostResponses.get(key);
                    try {
                        ClientResponse cr = future.get(100, TimeUnit.MILLISECONDS);

                        if (cr == null) {
                            //not likely to be NULL for ClientResponse

                        }
                        Long count = cr.getResults()[0].asScalarLong();
                        if (deletedTuplesPerPartition.containsKey(key)) {
                            Long totalCount = deletedTuplesPerPartition.get(key);
                            // replace the old value
                            deletedTuplesPerPartition.put(key, totalCount + count);
                        } else {
                            deletedTuplesPerPartition.put(key, count);
                        }

                        if (count == 0) {
                            // this partition has no tuples to be deleted, mark it done
                            // however, there is no harm to keep running the SP AdHoc deletes
                            // as there may be more new tuples inserted satisfying the requirement.
                            finshedPartitions.add(key);
                        }

                    } catch (TimeoutException ex) {
                        // deal with the next partition now
                        continue;
                    }
                }

                List<AdHocPlannedStatement> stmts = new ArrayList<>();
                stmts.add(result);
                AdHocPlannedStmtBatch plannedStmtBatch =
                        new AdHocPlannedStmtBatch(userParams,
                                                  stmts,
                                                  -1,
                                                  null,
                                                  null,
                                                  new Object[]{key});
                // create the SP AdHoc transaction
                future = createAdHocTransaction(plannedStmtBatch, false);

                allHostResponses.put(key, future);
            }
        }

        // all partition has seen no tuples available to be deleted
        // prepare the final result
        VoltTable resultTable = new VoltTable(resultColumns);
        for(Integer key: deletedTuplesPerPartition.keySet()) {
            Long row = deletedTuplesPerPartition.get(key);
            resultTable.addRow(key, row);
        }

        ClientResponseImpl cri = new ClientResponseImpl(ClientResponse.SUCCESS,
                new VoltTable[]{resultTable}, "");
        CompletableFuture<ClientResponse> f = new CompletableFuture<>();
        f.complete(cri);

        return f;

    }
}
