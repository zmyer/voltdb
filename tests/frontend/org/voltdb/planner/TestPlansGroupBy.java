/* This file is part of VoltDB.
 * Copyright (C) 2008-2017 VoltDB Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package org.voltdb.planner;

import java.util.ArrayList;
import java.util.List;

import org.voltdb.expressions.AbstractExpression;
import org.voltdb.plannodes.AbstractJoinPlanNode;
import org.voltdb.plannodes.AbstractPlanNode;
import org.voltdb.plannodes.AbstractReceivePlanNode;
import org.voltdb.plannodes.AbstractScanPlanNode;
import org.voltdb.plannodes.AggregatePlanNode;
import org.voltdb.plannodes.HashAggregatePlanNode;
import org.voltdb.plannodes.IndexScanPlanNode;
import org.voltdb.plannodes.LimitPlanNode;
import org.voltdb.plannodes.NodeSchema;
import org.voltdb.plannodes.OrderByPlanNode;
import org.voltdb.plannodes.ProjectionPlanNode;
import org.voltdb.plannodes.ReceivePlanNode;
import org.voltdb.plannodes.SchemaColumn;
import org.voltdb.plannodes.SendPlanNode;
import org.voltdb.types.ExpressionType;
import org.voltdb.types.PlanNodeType;
import org.voltdb.types.SortDirectionType;

public class TestPlansGroupBy extends PlannerTestCase {
    @Override
    protected void setUp() throws Exception {
        setupSchema(TestPlansGroupBy.class.getResource("testplans-groupby-ddl.sql"),
                "testplansgroupby", false);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testInlineSerialAgg_noGroupBy() {
        checkSimpleTableInlineAgg("SELECT SUM(A1) FROM T1", PlanNodeType.SEQSCAN);
        checkSimpleTableInlineAgg("SELECT MIN(A1) FROM T1", PlanNodeType.SEQSCAN);
        checkSimpleTableInlineAgg("SELECT MAX(A1) FROM T1", PlanNodeType.SEQSCAN);
        checkSimpleTableInlineAgg("SELECT SUM(A1), COUNT(A1) FROM T1",
                PlanNodeType.SEQSCAN);

        // There is no index defined on column B3
        checkSimpleTableInlineAgg("SELECT SUM(A3) FROM T3 WHERE B3 > 3",
                PlanNodeType.SEQSCAN);
        checkSimpleTableInlineAgg("SELECT MIN(A3) FROM T3 WHERE B3 > 3",
                PlanNodeType.SEQSCAN);
        checkSimpleTableInlineAgg("SELECT MAX(A3) FROM T3 WHERE B3 > 3",
                PlanNodeType.SEQSCAN);
        checkSimpleTableInlineAgg("SELECT COUNT(A3) FROM T3 WHERE B3 > 3",
                PlanNodeType.SEQSCAN);

        // Index scan
        checkSimpleTableInlineAgg("SELECT SUM(A3) FROM T3 WHERE PKEY > 3",
                PlanNodeType.INDEXSCAN);
        checkSimpleTableInlineAgg("SELECT MIN(A3) FROM T3 WHERE PKEY > 3",
                PlanNodeType.INDEXSCAN);
        checkSimpleTableInlineAgg("SELECT MAX(A3) FROM T3 WHERE PKEY > 3",
                PlanNodeType.INDEXSCAN);
        checkSimpleTableInlineAgg("SELECT COUNT(A3) FROM T3 WHERE PKEY > 3",
                PlanNodeType.INDEXSCAN);
    }

    private void checkSimpleTableInlineAgg(String sql, PlanNodeType scanType) {
        AbstractPlanNode p;
        List<AbstractPlanNode> pns;

        pns = compileToFragments(sql);
        assertLeftChain(pns.get(0), PlanNodeType.SEND,
                PlanNodeType.AGGREGATE, PlanNodeType.RECEIVE);

        p = followAssertedLeftChain(pns.get(1), PlanNodeType.SEND, scanType);
        assertNotNull(p.getInlinePlanNode(PlanNodeType.PROJECTION));
        assertNotNull(p.getInlinePlanNode(PlanNodeType.AGGREGATE));
    }

    // AVG is optimized with SUM / COUNT, generating extra projection node
    // In future, inline projection for aggregation.
    public void testInlineSerialAgg_noGroupBy_special() {
        AbstractPlanNode p;
        List<AbstractPlanNode> pns;
        String sql;

        sql = "SELECT AVG(A1) FROM T1";
        pns = compileToFragments(sql);
        //* enable to debug */ printExplainPlan(pns);
        followAssertedLeftChain(pns.get(0), PlanNodeType.SEND,
                PlanNodeType.PROJECTION, PlanNodeType.AGGREGATE,
                PlanNodeType.RECEIVE);

        p = followAssertedLeftChain(pns.get(1), PlanNodeType.SEND,
                PlanNodeType.SEQSCAN);
        assertNotNull(p.getInlinePlanNode(PlanNodeType.PROJECTION));
        assertNotNull(p.getInlinePlanNode(PlanNodeType.AGGREGATE));
    }

    public void checkAggregateOptimizationWithIndex(String sql, int nFragments,
            PlanNodeType aggType, String index) {
        AbstractPlanNode p;
        List<AbstractPlanNode> pns = compileToFragments(sql);
        assertEquals(nFragments, pns.size());
        p = followAssertedLeftChain(pns.get(nFragments-1), PlanNodeType.SEND,
                PlanNodeType.INDEXSCAN);
        assertNotNull(p.getInlinePlanNode(aggType));
        assertTrue(p.toExplainPlanString().contains(index));
    }

    /**
     * VoltDB has two optimizations to use the ordered output of an index scan to
     * avoid a (full) hash aggregation. In one case, this takes advantage of an
     * existing index scan already in the plan -- this case applies generally to
     * partial indexes (with WHERE clauses) and full indexes. In another case, the
     * index scan is introduced as a replacement for the sequential scan.
     * For simplicity, this case does not consider partial indexes -- it would have
     * to validate that the query conditions imply the predicate of the index.
     * This could be implemented some day.
     */
    public void testAggregateOptimizationWithIndex() {
        AbstractPlanNode p;
        List<AbstractPlanNode> pns;
        String sql;
        String index;

        sql = "SELECT A, COUNT(B) FROM R2 WHERE B > 2 GROUP BY A;";
        index = "primary key index";
        checkAggregateOptimizationWithIndex(sql, 1, PlanNodeType.HASHAGGREGATE, index);

        // matching the partial index WHERE clause
        sql = "SELECT A, COUNT(B) FROM R2 WHERE B > 3 GROUP BY A;";
        index = "PARTIAL_IDX_R2";
        checkAggregateOptimizationWithIndex(sql, 1, PlanNodeType.AGGREGATE, index);

        // using the partial index with serial aggregation
        sql = "SELECT A, COUNT(B) FROM R2 WHERE A > 5 AND B > 3 GROUP BY A;";
        index = "PARTIAL_IDX_R2";
        checkAggregateOptimizationWithIndex(sql, 1, PlanNodeType.AGGREGATE, index);

        // ORDER BY will help pick up the partial index
        sql = "SELECT A, COUNT(B) FROM R2 WHERE B > 3 GROUP BY A ORDER BY A;";
        index = "PARTIAL_IDX_R2";
        checkAggregateOptimizationWithIndex(sql, 1, PlanNodeType.AGGREGATE, index);

        // using the partial index with partial aggregation
        sql = "SELECT C, A, MAX(B) FROM R2 WHERE A > 0 AND B > 3 GROUP BY C, A";
        index = "PARTIAL_IDX_R2";
        checkAggregateOptimizationWithIndex(sql, 1, PlanNodeType.PARTIALAGGREGATE, index);

        // Partition IndexScan with HASH aggregate is optimized to use Partial aggregate -
        // index (F_D1) covers part of the GROUP BY columns
        sql = "SELECT F_D1, F_VAL1, MAX(F_VAL2) FROM F WHERE F_D1 > 0 " +
                " GROUP BY F_D1, F_VAL1 ORDER BY F_D1, MAX(F_VAL2)";
        index = "COL_F_TREE1";
        checkAggregateOptimizationWithIndex(sql, 2, PlanNodeType.PARTIALAGGREGATE, index);

        // IndexScan with HASH aggregate is optimized to use Serial aggregate -
        // index (F_VAL1, F_VAL2) covers all of the GROUP BY columns
        sql = "SELECT F_VAL1, F_VAL2, MAX(F_VAL3) FROM RF WHERE F_VAL1 > 0 " + 
                " GROUP BY F_VAL2, F_VAL1");
        index = "COL_RF_TREE2";
        checkAggregateOptimizationWithIndex(sql, 1, PlanNodeType.AGGREGATE, index);

        // IndexScan with HASH aggregate remains not optimized -
        // The first column index (F_VAL1, F_VAL2) is not part of the GROUP BY
        sql = "SELECT F_VAL2, MAX(F_VAL2) FROM RF WHERE F_VAL1 > 0 GROUP BY F_VAL2";
        index = "COL_RF_TREE2"));
        checkAggregateOptimizationWithIndex(sql, 1, PlanNodeType.HASHAGGREGATE, index);

        // Partition IndexScan with HASH aggregate remains unoptimized -
        // index (F_VAL1, F_VAL2) does not cover any of the GROUP BY columns
        sql = "SELECT MAX(F_VAL2) FROM F WHERE F_VAL1 > 0 GROUP BY F_D1";
        index = "COL_F_TREE2";
        checkAggregateOptimizationWithIndex(sql, 2, PlanNodeType.HASHAGGREGATE, index);

        // IndexScan with HASH aggregate remains unoptimized
        // - the index COL_RF_HASH is not scannable
        sql = "SELECT F_VAL3, MAX(F_VAL2) FROM RF WHERE F_VAL3 = 0 GROUP BY F_VAL3";
        pns = compileToFragments(sql);
        assertEquals(1, pns.size());
        p = followAssertedLeftChain(pns.get(0), PlanNodeType.SEND,
                PlanNodeType.INDEXSCAN);
        assertNotNull(p.getInlinePlanNode(PlanNodeType.HASHAGGREGATE));
        assertTrue(p.toExplainPlanString().contains("COL_RF_HASH"));

        // WHERE clause not matching
        sql = "SELECT A, COUNT(B) FROM R2 WHERE B > 2 GROUP BY A ORDER BY A;";
        pns = compileToFragments(sql);
        assertEquals(1, pns.size());
        p = followAssertedLeftChain(pns.get(0), PlanNodeType.SEND,
                PlanNodeType.PROJECTION, PlanNodeType.ORDERBY,
                PlanNodeType.SEQSCAN);
        assertNotNull(p.getInlinePlanNode(PlanNodeType.HASHAGGREGATE));
    }

    public void testCountStar() {
        compileToFragments("SELECT COUNT(*) FROM T1");
    }

    public void testCountDistinct() {
        AbstractPlanNode p;
        List<AbstractPlanNode> pns;
        String sql;

        // push down distinct because of GROUP BY partition column
        sql = "SELECT A4, COUNT(distinct B4) FROM T4 GROUP BY A4";
        pns = compileToFragments(sql);
        followAssertedLeftChain(pns.get(0), PlanNodeType.SEND,
                PlanNodeType.RECEIVE);

        p = followAssertedLeftChain(pns.get(1), PlanNodeType.SEND,
                PlanNodeType.INDEXSCAN);
        assertNotNull(p.getInlinePlanNode(PlanNodeType.HASHAGGREGATE));

        // GROUP BY multiple columns
        sql = "SELECT C4, A4, COUNT(distinct B4) FROM T4 GROUP BY C4, A4";
        pns = compileToFragments(sql);
        followAssertedLeftChain(pns.get(0), PlanNodeType.SEND,
                PlanNodeType.RECEIVE);

        p = pns.get(1).getChild(0);
        assertTrue(p instanceof AbstractScanPlanNode);
        assertNotNull(p.getInlinePlanNode(PlanNodeType.HASHAGGREGATE));

        // not push down distinct
        sql = "SELECT ABS(A4), COUNT(distinct B4) FROM T4 GROUP BY ABS(A4)";
        pns = compileToFragments(sql);
        p = pns.get(0).getChild(0);
        assertTrue(p instanceof HashAggregatePlanNode);
        assertTrue(p.getChild(0) instanceof ReceivePlanNode);

        p = pns.get(1).getChild(0);
        assertTrue(p instanceof AbstractScanPlanNode);
        assertNull(p.getInlinePlanNode(PlanNodeType.HASHAGGREGATE));

        // test not GROUP BY partition column with index available
        sql = "SELECT A.NUM, COUNT(DISTINCT A.ID ) AS Q58 FROM P2 A GROUP BY A.NUM; ";
        pns = compileToFragments(sql);
        p = pns.get(0).getChild(0);
        assertTrue(p instanceof HashAggregatePlanNode);
        assertTrue(p.getChild(0) instanceof ReceivePlanNode);

        p = pns.get(1).getChild(0);
        assertTrue(p instanceof IndexScanPlanNode);
        assertTrue(p.toExplainPlanString().contains("for deterministic order only"));
    }

    public void testDistinctA1() {
        compileToFragments("SELECT DISTINCT A1 FROM T1");
    }

    public void testDistinctA1_Subquery() {
        AbstractPlanNode p;
        List<AbstractPlanNode> pns;
        String sql;

        // Distinct rewrote with GROUP BY
        sql = "SELECT * FROM (SELECT DISTINCT A1 FROM T1) temp";
        pns = compileToFragments(sql);

        followAssertedLeftChain(pns.get(0), PlanNodeType.SEND,
                PlanNodeType.SEQSCAN, PlanNodeType.HASHAGGREGATE,
                PlanNodeType.RECEIVE);

        p = followAssertedLeftChain(pns.get(1), PlanNodeType.SEND,
                PlanNodeType.INDEXSCAN);
        assertNotNull(p.getInlinePlanNode(PlanNodeType.HASHAGGREGATE));
    }

    public void testGroupByA1() {
        AbstractPlanNode p;
        AggregatePlanNode aggNode;
        List<AbstractPlanNode> pns;
        String sql;

        sql = "SELECT A1 FROM T1 GROUP BY A1";
        pns = compileToFragments(sql);
        p = pns.get(0).getChild(0);
        assertTrue(p instanceof AggregatePlanNode);
        assertTrue(p.getChild(0) instanceof ReceivePlanNode);

        p = pns.get(1).getChild(0);
        assertTrue(p instanceof AbstractScanPlanNode);
        // No index, inline hash aggregate
        assertNotNull(p.getInlinePlanNode(PlanNodeType.HASHAGGREGATE));

        // HAVING
        sql = "SELECT A1, COUNT(*) FROM T1 GROUP BY A1 HAVING COUNT(*) > 3";
        pns = compileToFragments(sql);
        p = pns.get(0).getChild(0);
        assertTrue(p instanceof AggregatePlanNode);
        aggNode = (AggregatePlanNode)p;
        assertNotNull(aggNode.getPostPredicate());
        assertTrue(p.getChild(0) instanceof ReceivePlanNode);

        p = pns.get(1).getChild(0);
        assertTrue(p instanceof AbstractScanPlanNode);
        // No index, inline hash aggregate
        assertNotNull(p.getInlinePlanNode(PlanNodeType.HASHAGGREGATE));
        aggNode = (AggregatePlanNode)p.getInlinePlanNode(PlanNodeType.HASHAGGREGATE);
        assertNull(aggNode.getPostPredicate());

    }

    private void checkGroupByPartitionKey(String sql, boolean topAgg, boolean having) {
        AbstractPlanNode p;
        AggregatePlanNode aggNode;

        List<AbstractPlanNode> pns = compileToFragments(sql);
        p = pns.get(0).getChild(0);
        if (topAgg) {
            assertTrue(p instanceof AggregatePlanNode);
            if (having) {
                aggNode = (AggregatePlanNode)p;
                assertNotNull(aggNode.getPostPredicate());
            }
            p = p.getChild(0);
        }
        assertTrue(p instanceof ReceivePlanNode);

        p = pns.get(1).getChild(0);
        assertTrue(p instanceof AbstractScanPlanNode);

        PlanNodeType aggType = PlanNodeType.HASHAGGREGATE;
        if (p instanceof IndexScanPlanNode &&
                ((IndexScanPlanNode)p).isForGroupingOnly() ) {
            aggType = PlanNodeType.AGGREGATE;
        }
        assertNotNull(p.getInlinePlanNode(aggType));

        if (having && !topAgg) {
            aggNode = (AggregatePlanNode)p.getInlinePlanNode(aggType);
            assertNotNull(aggNode.getPostPredicate());
        }
    }

    public void testGroupByPartitionKey() {
        String sql;

        // Primary key is equal to partition key
        sql = "SELECT PKEY, COUNT(*) FROM T1 GROUP BY PKEY";
        // "its primary key index (for optimized grouping only)"
        // Not sure why not use serial aggregate instead
        checkGroupByPartitionKey(sql, false, false);

        // Test HAVING expression
        sql = "SELECT PKEY, COUNT(*) FROM T1 GROUP BY PKEY HAVING COUNT(*) > 3";
        checkGroupByPartitionKey(sql, false, true);

        // Primary key is not equal to partition key
        sql = "SELECT A3, COUNT(*) FROM T3 GROUP BY A3";
        checkGroupByPartitionKey(sql, false, false);

        // Test HAVING expression
        sql = "SELECT A3, COUNT(*) FROM T3 GROUP BY A3 HAVING COUNT(*) > 3";
        checkGroupByPartitionKey(sql, false, true);

        // GROUP BY partition key and others
        sql = "SELECT B3, A3, COUNT(*) FROM T3 GROUP BY B3, A3";
        checkGroupByPartitionKey(sql, false, false);

        // Test HAVING expression
        sql = "SELECT B3, A3, COUNT(*) FROM T3 GROUP BY B3, A3 HAVING COUNT(*) > 3";
        checkGroupByPartitionKey(sql, false, true);
    }

    public void testGroupByPartitionKey_Negative() {
        String sql;

        sql = "SELECT ABS(PKEY), COUNT(*) FROM T1 GROUP BY ABS(PKEY)";
        checkGroupByPartitionKey(sql, true, false);

        sql = "SELECT ABS(PKEY), COUNT(*) FROM T1 GROUP BY ABS(PKEY) HAVING COUNT(*) > 3";
        checkGroupByPartitionKey(sql, true, true);
    }

    // GROUP BY with index
    private void checkGroupByOnlyPlan(String sql,
            boolean twoFragments, PlanNodeType type) {
        List<AbstractPlanNode> pns = compileToFragments(sql);
        AbstractPlanNode apn = pns.get(0).getChild(0);
        if (twoFragments) {
            assertEquals(PlanNodeType.HASHAGGREGATE, apn.getPlanNodeType());
            apn = pns.get(1).getChild(0);
        }
        assertEquals(PlanNodeType.INDEXSCAN, apn.getPlanNodeType());
        assertNotNull(apn.getInlinePlanNode(type));
    }

    public void testGroupByOnly() {
        List<AbstractPlanNode> pns;
        String sql;

        System.out.println("Starting testGroupByOnly");

        /**
         * Serial Aggregate cases
         */
        // Replicated Table

        // only GROUP BY cols in SELECT clause
        sql = "SELECT F_D1 FROM RF GROUP BY F_D1";
        checkGroupByOnlyPlan(sql, false, PlanNodeType.AGGREGATE);

        // SELECT cols in GROUP BY and other aggregate cols
        sql = "SELECT F_D1, COUNT(*) FROM RF GROUP BY F_D1";
        checkGroupByOnlyPlan(sql, false, PlanNodeType.AGGREGATE);

        // aggregate cols are part of keys of used index
        sql = "SELECT F_VAL1, SUM(F_VAL2) FROM RF GROUP BY F_VAL1";
        checkGroupByOnlyPlan(sql, false, PlanNodeType.AGGREGATE);

        // expr index, full indexed case
        sql = "SELECT F_D1 + F_D2, COUNT(*) FROM RF GROUP BY F_D1 + F_D2";
        checkGroupByOnlyPlan(sql, false, PlanNodeType.AGGREGATE);

        // function index, prefix indexed case
        sql = "SELECT ABS(F_D1), COUNT(*) FROM RF GROUP BY ABS(F_D1)";
        checkGroupByOnlyPlan(sql, false, PlanNodeType.AGGREGATE);

        // order of GROUP BY cols is different of them in index definition
        // index on (ABS(F_D1), F_D2 - F_D3), GROUP BY on (F_D2 - F_D3, ABS(F_D1))
        sql = "SELECT F_D2 - F_D3, ABS(F_D1), COUNT(*) " +
                "FROM RF GROUP BY F_D2 - F_D3, ABS(F_D1)";
        checkGroupByOnlyPlan(sql, false, PlanNodeType.AGGREGATE);

        sql = "SELECT F_VAL1, F_VAL2, COUNT(*) FROM RF GROUP BY F_VAL2, F_VAL1";
        checkGroupByOnlyPlan(sql, false, PlanNodeType.AGGREGATE);

        // Partitioned Table
        sql = "SELECT F_D1 FROM F GROUP BY F_D1";
        // index scan for GROUP BY only, no need using hash aggregate
        checkGroupByOnlyPlan(sql, true, PlanNodeType.AGGREGATE);

        sql = "SELECT F_D1, COUNT(*) FROM F GROUP BY F_D1";
        checkGroupByOnlyPlan(sql, true, PlanNodeType.AGGREGATE);

        sql = "SELECT F_VAL1, SUM(F_VAL2) FROM F GROUP BY F_VAL1";
        checkGroupByOnlyPlan(sql, true, PlanNodeType.AGGREGATE);

        sql = "SELECT F_D1 + F_D2, COUNT(*) FROM F GROUP BY F_D1 + F_D2";
        checkGroupByOnlyPlan(sql, true, PlanNodeType.AGGREGATE);

        sql = "SELECT ABS(F_D1), COUNT(*) FROM F GROUP BY ABS(F_D1)";
        checkGroupByOnlyPlan(sql, true, PlanNodeType.AGGREGATE);

        sql = "SELECT F_D2 - F_D3, ABS(F_D1), COUNT(*) " +
                "FROM F GROUP BY F_D2 - F_D3, ABS(F_D1)";
        checkGroupByOnlyPlan(sql, true, PlanNodeType.AGGREGATE);

        /**
         * Hash Aggregate cases
         */
        // unoptimized case (only use second col of the index), but will be replaced in
        // SeqScanToIndexScan optimization for deterministic reason
        // use EXPR_RF_TREE1 not EXPR_RF_TREE2
        sql = "SELECT F_D2 - F_D3, COUNT(*) FROM RF GROUP BY F_D2 - F_D3";
        checkGroupByOnlyPlan(sql, false, PlanNodeType.HASHAGGREGATE);

        // unoptimized case: index is not scannable
        sql = "SELECT F_VAL3, COUNT(*) FROM RF GROUP BY F_VAL3";
        checkGroupByOnlyPlan(sql, false, PlanNodeType.HASHAGGREGATE);

        // unoptimized case: F_D2 is not prefix indexable
        sql = "SELECT F_D2, COUNT(*) FROM RF GROUP BY F_D2";
        checkGroupByOnlyPlan(sql, false, PlanNodeType.HASHAGGREGATE);

        // unoptimized case (only uses second col of the index), will not be replaced in
        // SeqScanToIndexScan for determinism because of non-deterministic receive.
        // Use primary key index
        sql = "SELECT F_D2 - F_D3, COUNT(*) FROM F GROUP BY F_D2 - F_D3";
        checkGroupByOnlyPlan(sql, true, PlanNodeType.HASHAGGREGATE);

        // unoptimized case (only uses second col of the index), will be replaced in
        // SeqScanToIndexScan for determinism.
        // use EXPR_F_TREE1 not EXPR_F_TREE2
        sql = "SELECT F_D2 - F_D3, COUNT(*) FROM RF GROUP BY F_D2 - F_D3";
        checkGroupByOnlyPlan(sql, false, PlanNodeType.HASHAGGREGATE);

        /**
         * Partial Aggregate cases
         */
        // unoptimized case: no prefix index found for (F_D1, F_D2)
        sql = "SELECT F_D1, F_D2, COUNT(*) FROM RF GROUP BY F_D1, F_D2";
        checkGroupByOnlyPlan(sql, false, PlanNodeType.PARTIALAGGREGATE);

        sql = "SELECT ABS(F_D1), F_D3, COUNT(*) FROM RF GROUP BY ABS(F_D1), F_D3";
        checkGroupByOnlyPlan(sql, false, PlanNodeType.PARTIALAGGREGATE);

        // partition table
        sql = "SELECT F_D1, F_D2, COUNT(*) FROM F GROUP BY F_D1, F_D2";
        checkGroupByOnlyPlan(sql, true, PlanNodeType.PARTIALAGGREGATE);

        sql = "SELECT ABS(F_D1), F_D3, COUNT(*) FROM F GROUP BY ABS(F_D1), F_D3";
        checkGroupByOnlyPlan(sql, true, PlanNodeType.PARTIALAGGREGATE);

        /**
         * Regression case
         */
        // ENG-9990 Repeating GROUP BY partition key in SELECT corrupts output schema.
        //* enable to debug */ boolean was = AbstractPlanNode.enableVerboseExplainForDebugging();
        sql = "SELECT G_PKEY, COUNT(*) C, G_PKEY FROM G GROUP BY G_PKEY";
        pns = compileToFragments(sql);
        //* enable to debug */ System.out.println(pns.get(0).toExplainPlanString());
        //* enable to debug */ System.out.println(pns.get(1).toExplainPlanString());
        //* enable to debug */ AbstractPlanNode.restoreVerboseExplainForDebugging(was);
        AbstractPlanNode pn = pns.get(0);
        pn = pn.getChild(0);
        NodeSchema os = pn.getOutputSchema();
        // The problem was a mismatch between the output schema
        // of the coordinator's send node and its feeding receive node
        // that had incorrectly rearranged its columns.
        SchemaColumn middleCol = os.getColumns().get(1);
        System.out.println(middleCol.toString());
        assertTrue(middleCol.getColumnAlias().equals("C"));

    }

    private void checkPartialAggregate(List<AbstractPlanNode> pns,
            boolean twoFragments) {
        AbstractPlanNode apn;
        if (twoFragments) {
            assertEquals(2, pns.size());
            apn = pns.get(1).getChild(0);
        }
        else {
            assertEquals(1, pns.size());
            apn = pns.get(0).getChild(0);
        }

        assertTrue(apn.toExplainPlanString().toLowerCase().contains("partial"));
    }

    public void testPartialSerialAggregateOnJoin() {
        String sql;
        List<AbstractPlanNode> pns;

        sql = "SELECT G.G_D1, RF.F_D2, COUNT(*) " +
                "FROM G LEFT OUTER JOIN RF ON G.G_D2 = RF.F_D1 " +
                "GROUP BY G.G_D1, RF.F_D2";
        pns = compileToFragments(sql);
        checkPartialAggregate(pns, true);

        // With different GROUP BY key ordered
        sql = "SELECT G.G_D1, RF.F_D2, COUNT(*) " +
                "FROM G LEFT OUTER JOIN RF ON G.G_D2 = RF.F_D1 " +
                "GROUP BY RF.F_D2, G.G_D1";
        pns = compileToFragments(sql);
        checkPartialAggregate(pns, true);


        // three table joins with aggregate
        sql = "SELECT G.G_D1, G.G_PKEY, RF.F_D2, F.F_D3, COUNT(*) " +
                "FROM G LEFT OUTER JOIN F ON G.G_PKEY = F.F_PKEY " +
                "     LEFT OUTER JOIN RF ON G.G_D1 = RF.F_D1 " +
                "GROUP BY G.G_D1, G.G_PKEY, RF.F_D2, F.F_D3";
        pns = compileToFragments(sql);
        checkPartialAggregate(pns, true);

        // With different GROUP BY key ordered
        sql = "SELECT G.G_D1, G.G_PKEY, RF.F_D2, F.F_D3, COUNT(*) " +
                "FROM G LEFT OUTER JOIN F ON G.G_PKEY = F.F_PKEY " +
                "     LEFT OUTER JOIN RF ON G.G_D1 = RF.F_D1 " +
                "GROUP BY G.G_PKEY, RF.F_D2, G.G_D1, F.F_D3";
        pns = compileToFragments(sql);
        checkPartialAggregate(pns, true);

        // With different GROUP BY key ordered
        sql = "SELECT G.G_D1, G.G_PKEY, RF.F_D2, F.F_D3, COUNT(*) " +
                "FROM G LEFT OUTER JOIN F ON G.G_PKEY = F.F_PKEY " +
                "     LEFT OUTER JOIN RF ON G.G_D1 = RF.F_D1 " +
                "GROUP BY RF.F_D2, G.G_PKEY, F.F_D3, G.G_D1";
        pns = compileToFragments(sql);
        checkPartialAggregate(pns, true);
    }


    // check GROUP BY query with limit
    // Query has GROUP BY partition column and limit, does not have ORDER BY
    private void checkGroupByOnlyPlanWithLimit(String sql, boolean twoFragments,
            boolean isHashAggregator, boolean inlineLimit) {
        // 'inlineLimit' means LIMIT gets pushed down for partition table and
        // inlined with aggregate.
        List<AbstractPlanNode> pns = compileToFragments(sql);
        AbstractPlanNode apn = pns.get(0).getChild(0);

        if ( ! inlineLimit || twoFragments) {
            assertEquals(PlanNodeType.LIMIT, apn.getPlanNodeType());
            apn = apn.getChild(0);
        }

        // GROUP BY partition column does not need top GROUP BY node.
        if (twoFragments) {
            apn = pns.get(1).getChild(0);
            if ( ! inlineLimit) {
                assertEquals(PlanNodeType.LIMIT, apn.getPlanNodeType());
                apn = apn.getChild(0);
            }
        }

        // For a single table aggregate, it is inline always.
        assertEquals(PlanNodeType.INDEXSCAN, apn.getPlanNodeType());
        if (isHashAggregator) {
            assertNotNull(apn.getInlinePlanNode(PlanNodeType.HASHAGGREGATE));
        }
        else {
            assertNotNull(apn.getInlinePlanNode(PlanNodeType.AGGREGATE));
            if (inlineLimit) {
                AbstractPlanNode p = apn.getInlinePlanNode(PlanNodeType.AGGREGATE);
                assertNotNull(p.getInlinePlanNode(PlanNodeType.LIMIT));
            }
        }
    }

    // GROUP BY With LIMIT without ORDER BY
    public void testGroupByWithLimit() {
        String sql;

        // replicated table with serial aggregation and inlined limit
        sql = "SELECT F_PKEY FROM RF GROUP BY F_PKEY LIMIT 5";
        checkGroupByOnlyPlanWithLimit(sql, false, false, true);

        sql = "SELECT F_D1 FROM RF GROUP BY F_D1 LIMIT 5";
        checkGroupByOnlyPlanWithLimit(sql, false, false, true);

        // partitioned table with serial aggregation and inlined limit
        // GROUP BY columns contain the partition key is the only case allowed
        sql = "SELECT F_PKEY FROM F GROUP BY F_PKEY LIMIT 5";
        checkGroupByOnlyPlanWithLimit(sql, true, false, true);

        // Explain plan for the above query
        /*
           RETURN RESULTS TO STORED PROCEDURE
            LIMIT 5
             RECEIVE FROM ALL PARTITIONS

           RETURN RESULTS TO STORED PROCEDURE
            INDEX SCAN of "F" using its primary key index (for optimized grouping only)
             inline Serial AGGREGATION ops
              inline LIMIT 5
        */
        List<AbstractPlanNode> pns = compileToFragments(sql);
        String expectedStr = "  inline Serial AGGREGATION ops: \n" +
                             "   inline LIMIT 5";
        String explainPlan = "";
        for (AbstractPlanNode apn: pns) {
            explainPlan += apn.toExplainPlanString();
        }
        assertTrue(explainPlan.contains(expectedStr));

        sql = "SELECT A3, COUNT(*) FROM T3 GROUP BY A3 LIMIT 5";
        checkGroupByOnlyPlanWithLimit(sql, true, false, true);

        sql = "SELECT A3, B3, COUNT(*) FROM T3 GROUP BY A3, B3 LIMIT 5";
        checkGroupByOnlyPlanWithLimit(sql, true, false, true);

        sql = "SELECT A3, B3, COUNT(*) FROM T3 WHERE A3 > 1 GROUP BY A3, B3 LIMIT 5";
        checkGroupByOnlyPlanWithLimit(sql, true, false, true);

        //
        // negative tests
        //
        sql = "SELECT F_VAL2 FROM RF GROUP BY F_VAL2 LIMIT 5";
        checkGroupByOnlyPlanWithLimit(sql, false, true, false);

        // Limit should not be pushed down for case like:
        // GROUP BY non-partition without partition key and ORDER BY.
        // ENG-6485
    }

    public void testEdgeComplexRelatedCases() {
        List<AbstractPlanNode> pns;
        String sql;
        AbstractPlanNode p;

        sql = "SELECT PKEY+A1 FROM T1 ORDER BY PKEY+A1";
        pns = compileToFragments(sql);
        p = pns.get(0).getChild(0);
        assertTrue(p instanceof ProjectionPlanNode);
        assertTrue(p.getChild(0) instanceof OrderByPlanNode);
        assertTrue(p.getChild(0).getChild(0) instanceof ReceivePlanNode);

        p = pns.get(1).getChild(0);
        assertTrue(p instanceof AbstractScanPlanNode);

        // Useless ORDER BY clause.
        sql = "SELECT COUNT(*) FROM P1 ORDER BY PKEY";
        pns = compileToFragments(sql);
        p = pns.get(0).getChild(0);
        assertTrue(p instanceof AggregatePlanNode);
        assertTrue(p.getChild(0) instanceof ReceivePlanNode);
        p = pns.get(1).getChild(0);
        assertTrue(p instanceof AbstractScanPlanNode);

        sql = "SELECT A1, COUNT(*) AS tag FROM P1 GROUP BY A1 ORDER BY tag, A1 LIMIT 1";
        pns = compileToFragments(sql);
        p = pns.get(0).getChild(0);

        // ENG-5066: now Limit is pushed under Projection
        // Limit is also inlined with Orderby node
        assertTrue(p instanceof ProjectionPlanNode);
        p = p.getChild(0);
        assertTrue(p instanceof OrderByPlanNode);
        assertNotNull(p.getInlinePlanNode(PlanNodeType.LIMIT));
        assertTrue(p.getChild(0) instanceof AggregatePlanNode);

        p = pns.get(1).getChild(0);
        // inline aggregate
        assertTrue(p instanceof AbstractScanPlanNode);
        assertNotNull(p.getInlinePlanNode(PlanNodeType.HASHAGGREGATE));

        sql = "SELECT F_D1, COUNT(*) AS tag FROM RF GROUP BY F_D1 ORDER BY tag";
        pns = compileToFragments(sql);
        p = pns.get(0).getChild(0);
        //* enable to debug */ System.out.println("DEBUG: " + p.toExplainPlanString());
        assertTrue(p instanceof ProjectionPlanNode);
        p = p.getChild(0);
        assertTrue(p instanceof OrderByPlanNode);
        p = p.getChild(0);
        assertTrue(p instanceof IndexScanPlanNode);
        assertNotNull(p.getInlinePlanNode(PlanNodeType.AGGREGATE));

        sql = "SELECT F_D1, COUNT(*) FROM RF GROUP BY F_D1 ORDER BY 2";
        pns = compileToFragments(sql);
        p = pns.get(0).getChild(0);
        //* enable to debug */ System.out.println("DEBUG: " + p.toExplainPlanString());
        assertTrue(p instanceof ProjectionPlanNode);
        p = p.getChild(0);
        assertTrue(p instanceof OrderByPlanNode);
        p = p.getChild(0);
        assertTrue(p instanceof IndexScanPlanNode);
        assertNotNull(p.getInlinePlanNode(PlanNodeType.AGGREGATE));
    }

    private List<AbstractPlanNode> checkHasComplexAgg(String sql) {
        return checkHasComplexAgg(sql, false);
    }

    private List<AbstractPlanNode> checkHasComplexAgg(String sql,
            boolean projectPushdown) {
        List<AbstractPlanNode> pns = compileToFragments(sql);
        boolean isDistributed = pns.size() > 1 ? true: false;

        if (projectPushdown) {
            assertTrue(isDistributed);
        }

        AbstractPlanNode p = pns.get(0).getChild(0);
        if (p instanceof LimitPlanNode) {
            p = p.getChild(0);
        }
        if (p instanceof OrderByPlanNode) {
            p = p.getChild(0);
        }
        if ( ! projectPushdown) {
            assertTrue(p instanceof ProjectionPlanNode);
        }
        while (p.getChildCount() > 0) {
            p = p.getChild(0);
            assertFalse(p instanceof ProjectionPlanNode);
        }

        if (isDistributed) {
            p = pns.get(1).getChild(0);
            int projectCount = 0;
            while ( p.getChildCount() > 0) {
                p = p.getChild(0);
                if (p instanceof ProjectionPlanNode) {
                    projectCount++;
                    assertTrue(projectPushdown);
                }
            }
            if (projectPushdown) {
                assertEquals(1, projectCount);
            }
        }
        return pns;
    }

    public void testComplexAggwithLimit() {
        List<AbstractPlanNode> pns;
        String sql;

        sql = "SELECT A1, SUM(A1), SUM(A1)+11 FROM P1 GROUP BY A1 ORDER BY A1 LIMIT 2";
        pns = checkHasComplexAgg(sql);

        // Test limit is not pushed down
        AbstractPlanNode p = pns.get(0).getChild(0);
        assertTrue(p instanceof OrderByPlanNode);
        assertNotNull(p.getInlinePlanNode(PlanNodeType.LIMIT));
        assertTrue(p.getChild(0) instanceof ProjectionPlanNode);
        assertTrue(p.getChild(0).getChild(0) instanceof AggregatePlanNode);

        p = pns.get(1).getChild(0);
        // inline limit with ORDER BY
        assertTrue(p instanceof OrderByPlanNode);
        assertNotNull(p.getInlinePlanNode(PlanNodeType.LIMIT));
        p = p.getChild(0);
        // inline aggregate
        assertTrue(p instanceof AbstractScanPlanNode);
        assertNotNull(p.getInlinePlanNode(PlanNodeType.HASHAGGREGATE));

    }

    public void testComplexAggwithDistinct() {
        List<AbstractPlanNode> pns;
        String sql;

        sql = "SELECT A1, SUM(A1), SUM(distinct A1)+11 FROM P1 GROUP BY A1 ORDER BY A1";
        pns = checkHasComplexAgg(sql);

        // Test aggregation node not push down with distinct
        AbstractPlanNode p = pns.get(0).getChild(0);
        assertTrue(p instanceof OrderByPlanNode);
        assertTrue(p.getChild(0) instanceof ProjectionPlanNode);
        assertTrue(p.getChild(0).getChild(0) instanceof AggregatePlanNode);

        p = pns.get(1).getChild(0);
        assertTrue(p instanceof AbstractScanPlanNode);
    }

    public void testComplexAggwithLimitDistinct() {
        List<AbstractPlanNode> pns;
        String sql;

        sql = "SELECT A1, SUM(A1), SUM(distinct A1)+11 FROM P1 GROUP BY A1 ORDER BY A1 LIMIT 2";
        pns = checkHasComplexAgg(sql);

        // Test no limit push down
        AbstractPlanNode p = pns.get(0).getChild(0);
        assertTrue(p instanceof OrderByPlanNode);
        assertNotNull(p.getInlinePlanNode(PlanNodeType.LIMIT));
        assertTrue(p.getChild(0) instanceof ProjectionPlanNode);
        assertTrue(p.getChild(0).getChild(0) instanceof AggregatePlanNode);

        p = pns.get(1).getChild(0);
        assertTrue(p instanceof AbstractScanPlanNode);
    }

    public void testComplexAggCase() {
        String sql;

        sql = "SELECT A1, SUM(A1), SUM(A1)+11 FROM P1 GROUP BY A1";
        checkHasComplexAgg(sql);

        sql = "SELECT A1, SUM(PKEY) AS A2, (SUM(PKEY) / 888) AS A3, (SUM(PKEY) + 1) AS A4 FROM P1 GROUP BY A1";
        checkHasComplexAgg(sql);

        sql = "SELECT A1, SUM(PKEY), COUNT(PKEY), (AVG(PKEY) + 1) AS A4 FROM P1 GROUP BY A1";
        checkHasComplexAgg(sql);
    }

    public void testComplexAggCaseProjectPushdown() {
        String sql;

        // This complex aggregate case will push down ORDER BY LIMIT
        // so the projection plan node should be also pushed down
        sql = "SELECT PKEY, SUM(A1) + 1 FROM P1 GROUP BY PKEY ORDER BY 1, 2 LIMIT 10";
        checkHasComplexAgg(sql, true);

        sql = "SELECT PKEY, AVG(A1) FROM P1 GROUP BY PKEY ORDER BY 1, 2 LIMIT 10";
        checkHasComplexAgg(sql, true);
    }

    public void testComplexGroupBy() {
        List<AbstractPlanNode> pns;
        String sql;

        sql = "SELECT A1, ABS(A1), ABS(A1)+1, SUM(B1) FROM P1 GROUP BY A1, ABS(A1)";
        checkHasComplexAgg(sql);

        // Check it can compile
        sql = "SELECT ABS(A1), SUM(B1) FROM P1 GROUP BY ABS(A1)";
        pns = compileToFragments(sql);
        AbstractPlanNode p = pns.get(0).getChild(0);
        assertTrue(p instanceof AggregatePlanNode);

        p = pns.get(1).getChild(0);
        // inline aggregate
        assertTrue(p instanceof AbstractScanPlanNode);
        assertNotNull(p.getInlinePlanNode(PlanNodeType.HASHAGGREGATE));

        sql = "SELECT A1+PKEY, avg(B1) AS tag FROM P1 GROUP BY A1+PKEY ORDER BY ABS(tag), A1+PKEY";
        checkHasComplexAgg(sql);
    }

    private void checkOptimizedAgg(String sql, boolean optimized) {
        List<AbstractPlanNode> pns = compileToFragments(sql);
        checkOptimizedAgg(pns, optimized);
    }

    private void checkOptimizedAgg(List<AbstractPlanNode> pns, boolean optimized) {
        AbstractPlanNode p = pns.get(0).getChild(0);
        if (optimized) {
            assertTrue(p instanceof ProjectionPlanNode);
            assertTrue(p.getChild(0) instanceof AggregatePlanNode);

            p = pns.get(1).getChild(0);
            // push down for optimization
            assertTrue(p instanceof AbstractScanPlanNode);

            assertTrue(p.getInlinePlanNode(PlanNodeType.AGGREGATE) != null ||
                    p.getInlinePlanNode(PlanNodeType.HASHAGGREGATE) != null);
        }
        else {
            assertEquals(1, pns.size());
            assertTrue(p instanceof AbstractScanPlanNode);
            assertTrue(p.getInlinePlanNode(PlanNodeType.AGGREGATE) != null ||
                    p.getInlinePlanNode(PlanNodeType.HASHAGGREGATE) != null);
        }
    }

    public void testUnOptimizedAVG() {
        List<AbstractPlanNode> pns;
        String sql;

        sql = "SELECT AVG(A1) FROM R1";
        checkOptimizedAgg(sql, false);

        sql = "SELECT A1, AVG(PKEY) FROM R1 GROUP BY A1";
        checkOptimizedAgg(sql, false);

        sql = "SELECT A1, AVG(PKEY)+1 FROM R1 GROUP BY A1";
        pns = checkHasComplexAgg(sql);
        AbstractPlanNode p = pns.get(0).getChild(0);
        assertTrue(p instanceof ProjectionPlanNode);
        p = p.getChild(0);
        assertTrue(p instanceof AbstractScanPlanNode);
        assertTrue(p.getInlinePlanNode(PlanNodeType.AGGREGATE) != null ||
                p.getInlinePlanNode(PlanNodeType.HASHAGGREGATE) != null);
    }

    public void testOptimizedAVG() {
        List<AbstractPlanNode> pns;
        String sql;

        sql = "SELECT AVG(A1) FROM P1";
        pns = checkHasComplexAgg(sql);
        checkOptimizedAgg(pns, true);

        sql = "SELECT A1, AVG(PKEY) FROM P1 GROUP BY A1";
        pns = checkHasComplexAgg(sql);
        // Test avg pushed down by replacing it with sum, count
        checkOptimizedAgg(pns, true);

        sql = "SELECT A1, AVG(PKEY)+1 FROM P1 GROUP BY A1";
        pns = checkHasComplexAgg(sql);
        // Test avg pushed down by replacing it with sum, count
        checkOptimizedAgg(pns, true);
    }

    public void testGroupByColsNotInDisplayCols() {
        String sql;

        sql = "SELECT SUM(PKEY) FROM P1 GROUP BY A1";
        checkHasComplexAgg(sql);

        sql = "SELECT SUM(PKEY), SUM(PKEY) FROM P1 GROUP BY A1";
        checkHasComplexAgg(sql);
    }

    private void checkGroupByAliasFeature(String sql1, String sql2, boolean exact) {
        String explainStr1;
        String explainStr2;
        explainStr1 = buildExplainPlan(sql1);
        explainStr2 = buildExplainPlan(sql2);
        if ( ! exact) {
            explainStr1 = explainStr1.replaceAll("TEMP_TABLE\\.column#[\\d]",
                    "TEMP_TABLE.column#[Index]");
            explainStr2 = explainStr2.replaceAll("TEMP_TABLE\\.column#[\\d]",
                    "TEMP_TABLE.column#[Index]");
            assertEquals(explainStr1, explainStr2);
        }
        assertEquals(explainStr1, explainStr2);
    }

    public void testGroupByBooleanConstants() {
        String[] conditions = {"1=1", "1=0", "TRUE", "FALSE", "1>2"};
        for (String condition : conditions) {
            failToCompile("SELECT COUNT(P1.PKEY) FROM P1 GROUP BY " + condition,
                    "A GROUP BY clause does not allow a BOOLEAN expression.");
        }
    }

    public void testGroupByAliasENG9872() {
        String sql;

        // If we have an alias in a GROUP BY clause, and
        // the alias is to an aggregate, we need to reject
        // this.
        sql = "SELECT 2*COUNT(P1.PKEY) AS AAA FROM P1 GROUP BY AAA";
        failToCompile(sql, "invalid GROUP BY expression:  COUNT()");
        // Ambiguity.
        sql = "SELECT P1.PKEY AS AAA, P1.PKEY AS AAA FROM P1 GROUP BY AAA";
        failToCompile(sql, "Group by expression \"AAA\" is ambiguous");
        // More ambiguity.  Also, the count aggregate is used
        // in the GROUP BY, but we see the ambiguity first.
        sql = "SELECT 2*COUNT(P1.PKEY) AS AAA, P1.PKEY AS AAA FROM P1 GROUP BY AAA";
        failToCompile(sql, "Group by expression \"AAA\" is ambiguous");
        // This used to fail because we ignored SELECT lists
        // which had no aggregates.  Now we look at all of them.
        compile("SELECT P1.PKEY AS AAA FROM P1 GROUP BY AAA");
    }

    public void testGroupByAliasNegativeCases() {
        List<AbstractPlanNode> pns;
        String sql;

        // GROUP BY aggregate expression
        try {
            pns = compileInvalidToFragments(
                    "SELECT ABS(PKEY) AS sp, COUNT(*) AS ct FROM P1 GROUP BY COUNT(*)");
            fail("Did not expect invalid GROUP BY query to succeed.");
        }
        catch (Exception ex) {
            assertTrue(ex.getMessage().contains("invalid GROUP BY expression:  COUNT()"));
        }

        try {
            pns = compileInvalidToFragments(
                    "SELECT ABS(PKEY) AS sp, COUNT(*) AS ct FROM P1 GROUP BY ct");
            fail("Did not expect invalid GROUP BY alias query to succeed.");
        }
        catch (Exception ex) {
            assertTrue(ex.getMessage().contains("invalid GROUP BY expression:  COUNT()"));
        }

        try {
            pns = compileInvalidToFragments(
                    "SELECT ABS(PKEY) AS sp, (COUNT(*) +1 ) AS ct FROM P1 GROUP BY ct");
            fail("Did not expect invalid GROUP BY expression alias query to succeed.");
        }
        catch (Exception ex) {
            assertTrue(ex.getMessage().contains("invalid GROUP BY expression:  COUNT()"));
        }

        // GROUP BY alias and expression
        try {
            pns = compileInvalidToFragments(
                    "SELECT ABS(PKEY) AS sp, COUNT(*) AS ct FROM P1 GROUP BY sp + 1");
            fail("Did not expect invalid GROUP BY alias expression query to succeed.");
        }
        catch (Exception ex) {
            assertTrue(ex.getMessage().contains(
                    "user lacks privilege or object not found: SP"));
        }

        // HAVING
        try {
            pns = compileInvalidToFragments(
                    "SELECT ABS(A1), COUNT(*) AS ct FROM P1 GROUP BY ABS(A1) HAVING ct > 3");
            fail("Did not expect invalid HAVING condition on alias query to succeed.");
        }
        catch (Exception ex) {
            assertTrue(ex.getMessage().contains(
                    "user lacks privilege or object not found: CT"));
        }

        // GROUP BY column.alias
        try {
            pns = compileInvalidToFragments(
                    "SELECT ABS(PKEY) AS sp, COUNT(*) AS ct FROM P1 GROUP BY P1.sp");
            fail("Did not expect invalid GROUP BY qualified alias query to succeed.");
        }
        catch (Exception ex) {
            assertTrue(ex.getMessage().contains(
                    "user lacks privilege or object not found: P1.SP"));
        }

        //
        // ambiguous GROUP BY query because of A1 is a column name and a SELECT alias
        //
        sql = "SELECT ABS(A1) AS A1, COUNT(*) AS ct FROM P1 GROUP BY A1";
        pns = compileToFragments(sql);
        //* enable to debug */ printExplainPlan(pns);
        AbstractPlanNode p = pns.get(1).getChild(0);
        assertTrue(p instanceof AbstractScanPlanNode);
        AggregatePlanNode agg = AggregatePlanNode.getInlineAggregationNode(p);
        assertNotNull(agg);
        // GROUP BY column, instead of the ABS(A1) expression
        assertEquals(agg.getGroupByExpressions().get(0).getExpressionType(), ExpressionType.VALUE_TUPLE);
    }

    public void testGroupByAlias() {
        String sql1, sql2;

        // GROUP BY alias for expression
        sql1 = "SELECT ABS(PKEY) AS sp, COUNT(*) AS ct FROM P1 GROUP BY sp";
        sql2 = "SELECT ABS(PKEY) AS sp, COUNT(*) AS ct FROM P1 GROUP BY ABS(PKEY)";
        checkQueriesPlansAreTheSame(sql1, sql2);

        // GROUP BY multiple alias (expression or column)
        sql1 = "SELECT A1 AS A, ABS(PKEY) AS sp, COUNT(*) AS ct FROM P1 GROUP BY A, sp";
        sql2 = "SELECT A1 AS A, ABS(PKEY) AS sp, COUNT(*) AS ct FROM P1 GROUP BY A, ABS(PKEY)";
        checkQueriesPlansAreTheSame(sql1, sql2);
        sql2 = "SELECT A1 AS A, ABS(PKEY) AS sp, COUNT(*) AS ct FROM P1 GROUP BY A1, sp";
        checkQueriesPlansAreTheSame(sql1, sql2);
        sql2 = "SELECT A1 AS A, ABS(PKEY) AS sp, COUNT(*) AS ct FROM P1 GROUP BY A1, ABS(PKEY)";
        checkQueriesPlansAreTheSame(sql1, sql2);

        // GROUP BY and SELECT in different orders
        sql2 = "SELECT ABS(PKEY) AS sp, A1 AS A, COUNT(*) AS ct FROM P1 GROUP BY A, ABS(PKEY)";
        checkGroupByAliasFeature(sql1, sql2, false);

        sql2 = "SELECT ABS(PKEY) AS sp, COUNT(*) AS ct, A1 AS A FROM P1 GROUP BY A, ABS(PKEY)";
        checkGroupByAliasFeature(sql1, sql2, false);

        sql2 = "SELECT COUNT(*) AS ct, ABS(PKEY) AS sp, A1 AS A FROM P1 GROUP BY A, ABS(PKEY)";
        checkGroupByAliasFeature(sql1, sql2, false);

        sql2 = "SELECT A1 AS A, COUNT(*) AS ct, ABS(PKEY) AS sp FROM P1 GROUP BY A, ABS(PKEY)";
        checkGroupByAliasFeature(sql1, sql2, false);

        sql2 = "SELECT A1 AS A, COUNT(*) AS ct, ABS(PKEY) AS sp FROM P1 GROUP BY ABS(PKEY), A";
        checkGroupByAliasFeature(sql1, sql2, false);

        // GROUP BY alias with selected constants
        sql1 = "SELECT 1, ABS(PKEY) AS sp, COUNT(*) AS ct FROM P1 GROUP BY sp";
        sql2 = "SELECT 1, ABS(PKEY) AS sp, COUNT(*) AS ct FROM P1 GROUP BY ABS(PKEY)";
        checkQueriesPlansAreTheSame(sql1, sql2);

        // GROUP BY alias on joined results
        sql1 = "SELECT ABS(P1.PKEY) AS sp, COUNT(*) AS ct FROM P1, R1 WHERE P1.A1 = R1.A1 GROUP BY sp";
        sql2 = "SELECT ABS(P1.PKEY) AS sp, COUNT(*) AS ct FROM P1, R1 WHERE P1.A1 = R1.A1 GROUP BY ABS(P1.PKEY)";
        checkQueriesPlansAreTheSame(sql1, sql2);

        // GROUP BY expression with constants parameter
        sql1 = "SELECT ABS(P1.PKEY + 1) AS sp, COUNT(*) AS ct FROM P1 GROUP BY sp";
        sql2 = "SELECT ABS(P1.PKEY + 1) AS sp, COUNT(*) AS ct FROM P1 GROUP BY ABS(P1.PKEY + 1)";
        checkQueriesPlansAreTheSame(sql1, sql2);

        // GROUP BY constants with alias
        sql1 = "SELECT 5 AS tag, COUNT(*) AS ct FROM P1 GROUP BY tag";
        sql2 = "SELECT 5 AS tag, COUNT(*) AS ct FROM P1 GROUP BY 5";
        checkQueriesPlansAreTheSame(sql1, sql2);
    }

    private void checkMVNoFix_NoAgg_NormalQueries(
            String sql) {
        // the first '-1' indicates that there is no top aggregation node.
        checkMVReaggregateFeature(sql, false,
                -1, -1,
                -1, -1,
                false, false);
    }

    private void checkMVNoFix_NoAgg_NormalQueries_MergeReceive(
            String sql, SortDirectionType sortDirection) {
        checkMVReaggregateFeatureMergeReceive(sql, false,
                -1, -1,
                false, false, sortDirection);
    }

    private void checkMVNoFix_NoAgg(
            String sql, int numGroupByOfTopAggNode, int numAggsOfTopAggNode,
            boolean aggPushdown, boolean aggInline) {
        checkMVReaggregateFeature(sql, false,
                numGroupByOfTopAggNode, numAggsOfTopAggNode,
                -1, -1, aggPushdown, aggInline);

    }

    public void testNoFix_MVBasedQuery() {
        String sql;

        // (1) Table V_P1_NO_FIX_NEEDED:

        // Normal SELECT queries
        checkMVNoFix_NoAgg_NormalQueries("SELECT * FROM V_P1_NO_FIX_NEEDED");

        sql = "SELECT V_SUM_C1 FROM V_P1_NO_FIX_NEEDED ORDER BY V_A1";
        checkMVNoFix_NoAgg_NormalQueries_MergeReceive(sql, SortDirectionType.ASC);

        sql = "SELECT V_SUM_C1 FROM V_P1_NO_FIX_NEEDED LIMIT 1";
        checkMVNoFix_NoAgg_NormalQueries(sql);

        // Distributed distinct SELECT query
        sql = "SELECT DISTINCT V_SUM_C1 FROM V_P1_NO_FIX_NEEDED";
        checkMVNoFix_NoAgg(sql, 1, 0, true, true);

        // Distributed GROUP BY query
        sql = "SELECT V_SUM_C1 FROM V_P1_NO_FIX_NEEDED GROUP by V_SUM_C1";
        checkMVNoFix_NoAgg(sql, 1, 0, true, true);

        sql = "SELECT V_SUM_C1, SUM(V_CNT) FROM V_P1_NO_FIX_NEEDED " +
                "GROUP by V_SUM_C1";
        checkMVNoFix_NoAgg(sql, 1, 1, true, true);

        // (2) Table V_P1 and V_P1_NEW:
        sql = "SELECT SUM(V_SUM_C1) FROM V_P1";
        checkMVReaggregateFeature(sql, false, 0, 1, -1, -1, true, true);

        sql = "SELECT MIN(V_MIN_C1) FROM V_P1_NEW";
        checkMVReaggregateFeature(sql, false, 0, 1, -1, -1, true, true);

        sql = "SELECT MAX(V_MAX_D1) FROM V_P1_NEW";
        checkMVReaggregateFeature(sql, false, 0, 1, -1, -1, true, true);

        sql = "SELECT MAX(V_MAX_D1) FROM V_P1_NEW GROUP BY V_A1";
        checkMVNoFix_NoAgg(sql, 1, 1, true, true);

        sql = "SELECT V_A1, MAX(V_MAX_D1) FROM V_P1_NEW GROUP BY V_A1";
        checkMVNoFix_NoAgg(sql, 1, 1, true, true);

        sql = "SELECT V_A1,V_B1, MAX(V_MAX_D1) FROM V_P1_NEW GROUP BY V_A1, V_B1";
        checkMVNoFix_NoAgg(sql, 2, 1, true, true);

        // (3) Join Query
        // Voter example query in 'Results' stored procedure.
        sql = "   SELECT a.contestant_name   AS contestant_name"
                + "        , a.contestant_number AS contestant_number"
                + "        , SUM(b.num_votes)    AS total_votes"
                + "     FROM v_votes_by_contestant_number_state AS b"
                + "        , contestants AS a"
                + "    WHERE a.contestant_number = b.contestant_number"
                + " GROUP BY a.contestant_name"
                + "        , a.contestant_number"
                + " ORDER BY total_votes DESC"
                + "        , contestant_number ASC"
                + "        , contestant_name ASC;";
        checkMVNoFix_NoAgg(sql, 2, 1, true, true);


        sql = "SELECT SUM(v_p1.v_cnt) " +
                "FROM v_p1 INNER JOIN v_r1 USING(v_a1)";
        checkMVNoFix_NoAgg(sql, 0, 1, true, true);

        sql = "SELECT v_p1.v_b1, SUM(v_p1.v_sum_d1) " +
                "FROM v_p1 INNER JOIN v_r1 ON v_p1.v_a1 > v_r1.v_a1 " +
                "GROUP BY v_p1.v_b1;";
        checkMVNoFix_NoAgg(sql, 1, 1, true, true);

        sql = "SELECT MAX(v_r1.v_a1) " +
                "FROM v_p1 INNER JOIN v_r1 ON v_p1.v_a1 = v_r1.v_a1 " +
                "INNER JOIN r1v ON v_p1.v_a1 = r1v.v_a1 ";
        checkMVNoFix_NoAgg(sql, 0, 1, true, true);
    }

    public void testMVBasedQuery_EdgeCases() {
        String sql;

        // No aggregation will be pushed down.
        sql = "SELECT COUNT(*) FROM V_P1";
        checkMVFix_TopAgg_ReAgg(sql, 0, 1, 2, 0);

        sql = "SELECT SUM(v_a1) FROM V_P1";
        checkMVFix_TopAgg_ReAgg(sql, 0, 1, 2, 0);

        sql = "SELECT COUNT(v_a1) FROM V_P1";
        checkMVFix_TopAgg_ReAgg(sql, 0, 1, 2, 0);

        sql = "SELECT MAX(v_a1) FROM V_P1";
        checkMVFix_TopAgg_ReAgg(sql, 0, 1, 2, 0);

        // ENG-5386 opposite cases.
        sql = "SELECT SUM(V_SUM_C1+1) FROM V_P1";
        checkMVFix_TopAgg_ReAgg(sql, 0, 1, 2, 1);

        sql = "SELECT SUM(V_SUM_C1) FROM V_P1 WHERE V_SUM_C1 > 3";
        checkMVFix_TopAgg_ReAgg(sql, 0, 1, 2, 1);

        sql = "SELECT V_SUM_C1, MAX(V_MAX_D1) FROM V_P1_NEW GROUP BY V_SUM_C1";
        checkMVFix_TopAgg_ReAgg(sql, 1, 1, 2, 2);

        // ENG-5669 HAVING edge cases.
        sql = "SELECT SUM(V_SUM_C1) FROM V_P1 HAVING MAX(V_SUM_D1) > 3";
        checkMVFix_TopAgg_ReAgg(sql, 0, 2, 2, 2);

        sql = "SELECT SUM(V_SUM_C1) FROM V_P1 HAVING SUM(V_SUM_D1) > 3";
        checkMVReaggregateFeature(sql, false, 0, 2, -1, -1, true, true);

        // distinct on the v_a1 (part of the GROUP BY columns in the view)
        // aggregate pushed down for optimization
        sql = "SELECT distinct v_a1 FROM V_P1";
        checkMVFix_TopAgg_ReAgg(sql, 1, 0, 1, 0);

        sql = "SELECT v_a1 FROM V_P1 GROUP BY v_a1";
        checkMVFix_TopAgg_ReAgg(sql, 1, 0, 1, 0);

        sql = "SELECT distinct v_cnt FROM V_P1";
        checkMVFix_TopAgg_ReAgg(sql, 1, 0, 2, 1);

        sql = "SELECT v_cnt FROM V_P1 GROUP BY v_cnt";
        checkMVFix_TopAgg_ReAgg(sql, 1, 0, 2, 1);
    }

    public void testMVBasedQuery_NoAggQuery() {
        String sql;
        //        CREATE VIEW V_P1 (V_A1, V_B1, V_CNT, V_SUM_C1, V_SUM_D1)
        //        AS SELECT A1, B1, COUNT(*), SUM(C1), COUNT(D1)
        //        FROM P1  GROUP BY A1, B1;

        String[] tbs = {"V_P1", "V_P1_ABS"};
        for (String tb: tbs) {
            sql = "SELECT * FROM " + tb;
            checkMVFix_reAgg(sql, 2, 3);

            sql = "SELECT * FROM " + tb + " ORDER BY V_A1 DESC";
            checkMVFix_reAgg_MergeReceive(sql, 2, 3, SortDirectionType.DESC);

            sql = "SELECT * FROM " + tb + " ORDER BY V_A1, V_B1";
            checkMVFix_reAgg_MergeReceive(sql, 2, 3, SortDirectionType.ASC);

            sql = "SELECT * FROM " + tb + " ORDER BY V_SUM_D1";
            checkMVFix_reAgg(sql, 2, 3);

            sql = "SELECT * FROM " + tb + " limit 1";
            checkMVFix_reAgg(sql, 2, 3);

            sql = "SELECT * FROM " + tb + " ORDER BY V_A1, V_B1 limit 1";
            checkMVFix_reAgg_MergeReceive(sql, 2, 3, SortDirectionType.ASC);

            sql = "SELECT v_sum_c1 FROM " + tb;
            checkMVFix_reAgg(sql, 2, 1);

            sql = "SELECT v_sum_c1 FROM " + tb + " ORDER BY v_sum_c1";
            checkMVFix_reAgg(sql, 2, 1);

            sql = "SELECT v_sum_c1 FROM " + tb + " ORDER BY v_sum_d1";
            checkMVFix_reAgg(sql, 2, 2);

            sql = "SELECT v_sum_c1 FROM " + tb + " limit 1";
            checkMVFix_reAgg(sql, 2, 1);

            sql = "SELECT v_sum_c1 FROM " + tb + " ORDER BY v_sum_c1 limit 1";
            checkMVFix_reAgg(sql, 2, 1);
        }
    }

    public void testMVBasedQuery_AggQuery() {
        String sql;
        //      CREATE VIEW V_P1 (V_A1, V_B1, V_CNT, V_SUM_C1, V_SUM_D1)
        //      AS SELECT A1, B1, COUNT(*), SUM(C1), COUNT(D1)
        //      FROM P1  GROUP BY A1, B1;

        String[] tbs = {"V_P1", "V_P1_ABS"};

        for (String tb: tbs) {
            // Test set (1): GROUP BY
            sql = "SELECT V_SUM_C1 FROM " + tb +
                    " GROUP by V_SUM_C1";
            checkMVFix_TopAgg_ReAgg(sql, 1, 0, 2, 1);

            // because we have ORDER BY.
            sql = "SELECT V_SUM_C1 FROM " + tb +
                    " GROUP by V_SUM_C1 " +
                    " ORDER BY V_SUM_C1";
            checkMVFix_TopAgg_ReAgg(sql, 1, 0, 2, 1);

            sql = "SELECT V_SUM_C1 FROM " + tb +
                    " GROUP by V_SUM_C1 " +
                    " ORDER BY V_SUM_C1 LIMIT 5";
            checkMVFix_TopAgg_ReAgg(sql, 1, 0, 2, 1);

            sql = "SELECT V_SUM_C1 FROM " + tb +
                    " GROUP by V_SUM_C1 LIMIT 5";
            checkMVFix_TopAgg_ReAgg(sql, 1, 0, 2, 1);

            // Test set (2):
            sql = "SELECT V_SUM_C1, SUM(V_CNT) FROM " + tb +
                    " GROUP by V_SUM_C1";
            checkMVFix_TopAgg_ReAgg(sql, 1, 1, 2, 2);

            sql = "SELECT V_SUM_C1, SUM(V_CNT) FROM " + tb +
                    " GROUP by V_SUM_C1 " +
                    " ORDER BY V_SUM_C1";
            checkMVFix_TopAgg_ReAgg(sql, 1, 1, 2, 2);

            sql = "SELECT V_SUM_C1, SUM(V_CNT) FROM " + tb +
                    " GROUP by V_SUM_C1 " +
                    " ORDER BY V_SUM_C1 limit 2";
            checkMVFix_TopAgg_ReAgg(sql, 1, 1, 2, 2);

            // Distinct: No aggregation push down.
            sql = "SELECT V_SUM_C1, SUM(distinct V_CNT) " +
                    "FROM " + tb + " GROUP by V_SUM_C1 " +
                    " ORDER BY V_SUM_C1";
            checkMVFix_TopAgg_ReAgg(sql, 1, 1, 2, 2);

            // Test set (3)
            sql = "SELECT V_A1,V_B1, V_SUM_C1, SUM(V_SUM_D1) FROM " + tb +
                    " GROUP BY V_A1,V_B1, V_SUM_C1";
            checkMVFix_TopAgg_ReAgg(sql, 3, 1, 2, 2);

            sql = "SELECT V_A1,V_B1, V_SUM_C1, SUM(V_SUM_D1) FROM " + tb +
                    " GROUP BY V_A1,V_B1, V_SUM_C1 " +
                    " ORDER BY V_A1,V_B1, V_SUM_C1";
            checkMVFix_TopAgg_ReAgg(sql, 3, 1, 2, 2);

            sql = "SELECT V_A1,V_B1, V_SUM_C1, SUM(V_SUM_D1) FROM " + tb +
                    " GROUP BY V_A1,V_B1, V_SUM_C1 " +
                    " ORDER BY V_A1,V_B1, V_SUM_C1 LIMIT 5";
            checkMVFix_TopAgg_ReAgg(sql, 3, 1, 2, 2);

            sql = "SELECT V_A1,V_B1, V_SUM_C1, SUM(V_SUM_D1) FROM " + tb +
                    " GROUP BY V_A1,V_B1, V_SUM_C1 " +
                    " ORDER BY V_A1, V_SUM_C1 LIMIT 5";
            checkMVFix_TopAgg_ReAgg(sql, 3, 1, 2, 2);

            // Distinct: No aggregation push down.
            sql = "SELECT V_A1,V_B1, V_SUM_C1, SUM( distinct V_SUM_D1) FROM " +
                    tb + " GROUP BY V_A1,V_B1, V_SUM_C1 " +
                    " ORDER BY V_A1, V_SUM_C1 LIMIT 5";
            checkMVFix_TopAgg_ReAgg(sql, 3, 1, 2, 2);
        }
    }

    private void checkMVFixWithWhere(String sql, String aggFilter, String scanFilter) {
        List<AbstractPlanNode> pns;

        pns = compileToFragments(sql);
        //* enable to debug */ printExplainPlan(pns);
        checkMVFixWithWhere(pns,
                ( (aggFilter == null) ? null: new String[] {aggFilter}),
                ( (scanFilter == null) ? null: new String[] {scanFilter}));
    }

    private void checkMVFixWithWhere(String sql, Object aggFilters[]) {
        List<AbstractPlanNode> pns = compileToFragments(sql);
        //* enable to debug */ printExplainPlan(pns);
        checkMVFixWithWhere(pns, aggFilters, null);
    }

    private void checkMVFixWithWhere(List<AbstractPlanNode> pns,
            Object aggFilters, Object scanFilters) {
        AbstractPlanNode p = pns.get(0);

        List<AbstractPlanNode> nodes = p.findAllNodesOfClass(AbstractReceivePlanNode.class);
        assertEquals(1, nodes.size());
        p = nodes.get(0);

        // Find re-aggregation node.
        assertTrue(p instanceof ReceivePlanNode);
        assertTrue(p.getParent(0) instanceof HashAggregatePlanNode);
        HashAggregatePlanNode reAggNode = (HashAggregatePlanNode) p.getParent(0);
        String reAggNodeStr = reAggNode.toExplainPlanString().toLowerCase();

        // Find scan node.
        p = pns.get(1);
        assertEquals(1, p.getScanNodeList().size());
        p = p.getScanNodeList().get(0);
        String scanNodeStr = p.toExplainPlanString().toLowerCase();

        if (aggFilters != null) {
            String[] aggFilterStrings = null;
            if (aggFilters instanceof String) {
                aggFilterStrings = new String[] { (String) aggFilters };
            }
            else {
                aggFilterStrings = (String[]) aggFilters;
            }
            for (String aggFilter : aggFilterStrings) {
                //* enable to debug */ System.out.println(reAggNodeStr.contains(aggFilter.toLowerCase()));
                assertTrue(reAggNodeStr.contains(aggFilter.toLowerCase()));
                //* enable to debug */ System.out.println(scanNodeStr.contains(aggFilter.toLowerCase()));
                assertFalse(scanNodeStr.contains(aggFilter.toLowerCase()));
            }
        }
        else {
            assertNull(reAggNode.getPostPredicate());
        }

        if (scanFilters != null) {
            String[] scanFilterStrings = null;
            if (scanFilters instanceof String) {
                scanFilterStrings = new String[] { (String) scanFilters };
            }
            else {
                scanFilterStrings = (String[]) scanFilters;
            }
            for (String scanFilter : scanFilterStrings) {
                //* enable to debug */ System.out.println(reAggNodeStr.contains(scanFilter.toLowerCase()));
                assertFalse(reAggNodeStr.contains(scanFilter.toLowerCase()));
                //* enable to debug */ System.out.println(scanNodeStr.contains(scanFilter.toLowerCase()));
                assertTrue(scanNodeStr.contains(scanFilter.toLowerCase()));
            }
        }
    }

    public void testMVBasedQuery_Where() {
        String sql;
        //      CREATE VIEW V_P1 (V_A1, V_B1, V_CNT, V_SUM_C1, V_SUM_D1)
        //      AS SELECT A1, B1, COUNT(*), SUM(C1), COUNT(D1)
        //      FROM P1  GROUP BY A1, B1;
        // Test
        boolean asItWas = AbstractExpression.disableVerboseExplainForDebugging();
        sql = "SELECT * FROM V_P1 WHERE v_cnt = 1";
        checkMVFixWithWhere(sql, "v_cnt = 1", null);
        sql = "SELECT * FROM V_P1 WHERE v_a1 = 9";
        checkMVFixWithWhere(sql, null, "v_a1 = 9");
        sql = "SELECT * FROM V_P1 WHERE v_a1 = 9 AND v_cnt = 1";
        checkMVFixWithWhere(sql, "v_cnt = 1", "v_a1 = 9");
        sql = "SELECT * FROM V_P1 WHERE v_a1 = 9 OR v_cnt = 1";
        checkMVFixWithWhere(sql, new String[] {"v_a1 = 9) OR ", "v_cnt = 1)"});
        sql = "SELECT * FROM V_P1 WHERE v_a1 = v_cnt + 1";
        checkMVFixWithWhere(sql, new String[] {"v_a1 = (", "v_cnt + 1)"});
        AbstractExpression.restoreVerboseExplainForDebugging(asItWas);
    }

    private void checkMVFixWithJoin_ReAgg(String sql,
            int numGroupByOfReaggNode,
            int numAggsOfReaggNode,
            Object aggFilter,
            String scanFilter) {
        checkMVFixWithJoin(sql, -1, -1,
                numGroupByOfReaggNode, numAggsOfReaggNode,
                aggFilter, scanFilter);
    }

    private void checkMVFixWithJoin(String sql,
            int numGroupByOfTopAggNode, int numAggsOfTopAggNode,
            int numGroupByOfReaggNode, int numAggsOfReaggNode,
            Object aggFilters, Object scanFilters) {
        String[] joinType = {"inner join", "left join", "right join"};

        for (int i = 0; i < joinType.length; i++) {
            String newsql = sql.replace("@joinType", joinType[i]);
            //* enable to debug */ System.err.println("Query:" + newsql);
            // No join node under receive node.
            List<AbstractPlanNode> pns = checkMVReaggregateFeature(newsql, true,
                    numGroupByOfTopAggNode, numAggsOfTopAggNode,
                    numGroupByOfReaggNode, numAggsOfReaggNode, false, false);

            checkMVFixWithWhere(pns, aggFilters, scanFilters);
        }
    }

    /**
     * No tested for Outer join, no 'using' unclear column reference tested.
     * Non-aggregation queries.
     */
    public void testMVBasedQuery_Join_NoAgg() {
        boolean asItWas = AbstractExpression.disableVerboseExplainForDebugging();
        String sql = "";

        // Two tables joins.
        sql = "SELECT v_a1 FROM v_p1 @joinType v_r1 USING(v_a1)";
        checkMVFixWithJoin_ReAgg(sql, 2, 0, null, null);

        sql = "SELECT v_a1 FROM v_p1 @joinType v_r1 USING(v_a1) " +
                "WHERE v_a1 > 1 AND v_p1.v_cnt > 2 AND v_r1.v_b1 < 3 ";
        checkMVFixWithJoin_ReAgg(sql, 2, 1, "v_cnt > 2", null /* "v_a1 > 1" is optional */);

        sql = "SELECT v_p1.v_cnt " +
                "FROM v_p1 @joinType v_r1 ON v_p1.v_cnt = v_r1.v_cnt " +
                "WHERE v_p1.v_cnt > 1 AND v_p1.v_a1 > 2 AND v_p1.v_sum_c1 < 3 AND v_r1.v_b1 < 4 ";
        checkMVFixWithJoin_ReAgg(sql, 2, 2,
                new String[] { "v_sum_c1 < 3)", "v_cnt > 1)" }, "v_a1 > 2");

        // join on different columns.
        sql = "SELECT v_p1.v_cnt FROM v_r1 @joinType v_p1 ON v_r1.v_sum_c1 = v_p1.v_sum_d1 ";
        checkMVFixWithJoin_ReAgg(sql, 2, 2, null, null);


        // Three tables joins.
        sql = "SELECT v_r1.v_a1, v_r1.v_cnt " +
                "FROM v_p1 @joinType v_r1 ON v_p1.v_a1 = v_r1.v_a1 " +
                "@joinType r1v ON v_p1.v_a1 = r1v.v_a1 ";
        checkMVFixWithJoin_ReAgg(sql, 2, 0, null, null);

        sql = "SELECT v_r1.v_cnt, v_r1.v_a1 " +
                "FROM v_p1 @joinType v_r1 ON v_p1.v_cnt = v_r1.v_cnt " +
                "@joinType r1v ON v_p1.v_cnt = r1v.v_cnt ";
        checkMVFixWithJoin_ReAgg(sql, 2, 1, null, null);

        // join on different columns.
        sql = "SELECT v_p1.v_cnt " +
                "FROM v_r1 @joinType v_p1 ON v_r1.v_sum_c1 = v_p1.v_sum_d1 " +
                "@joinType r1v ON v_p1.v_cnt = r1v.v_sum_c1";
        checkMVFixWithJoin_ReAgg(sql, 2, 2, null, null);

        sql = "SELECT v_r1.v_cnt, v_r1.v_a1 " +
                "FROM v_p1 @joinType v_r1 ON v_p1.v_cnt = v_r1.v_cnt " +
                "@joinType r1v ON v_p1.v_cnt = r1v.v_cnt " +
                "WHERE v_p1.v_cnt > 1 AND v_p1.v_a1 > 2 AND v_p1.v_sum_c1 < 3 AND v_r1.v_b1 < 4 ";
        checkMVFixWithJoin(sql, -1, -1, 2, 2,
                new String[] {"v_cnt > 1", "v_sum_c1 < 3"}, "v_a1 > 2");

        sql = "SELECT v_r1.v_cnt, v_r1.v_a1 " +
                "FROM v_p1 @joinType v_r1 ON v_p1.v_cnt = v_r1.v_cnt " +
                "@joinType r1v ON v_p1.v_cnt = r1v.v_cnt WHERE v_p1.v_cnt > 1 AND v_p1.v_a1 > 2 AND " +
                "v_p1.v_sum_c1 < 3 AND v_r1.v_b1 < 4 AND r1v.v_sum_c1 > 6";
        checkMVFixWithJoin(sql, -1, -1, 2, 2,
                new String[] {"v_cnt > 1", "v_sum_c1 < 3"}, "v_a1 > 2");
        AbstractExpression.restoreVerboseExplainForDebugging(asItWas);
    }

    /**
     * No tested for Outer join, no 'using' unclear column reference tested.
     * Aggregation queries.
     */
    public void testMVBasedQuery_Join_Agg() {
        boolean asItWas = AbstractExpression.disableVerboseExplainForDebugging();
        String sql = "";

        // Two tables joins.
        sql = "SELECT SUM(v_a1) " +
                "FROM v_p1 @joinType v_r1 USING(v_a1)";
        checkMVFixWithJoin(sql, 0, 1, 2, 0, null, null);

        sql = "SELECT SUM(v_p1.v_a1) " +
                "FROM v_p1 @joinType v_r1 ON v_p1.v_a1 = v_r1.v_a1";
        checkMVFixWithJoin(sql, 0, 1, 2, 0, null, null);

        sql = "SELECT SUM(v_r1.v_a1) " +
                "FROM v_p1 @joinType v_r1 ON v_p1.v_a1 = v_r1.v_a1";
        checkMVFixWithJoin(sql, 0, 1, 2, 0, null, null);

        sql = "SELECT v_p1.v_b1, SUM(v_a1) " +
                "FROM v_p1 @joinType v_r1 USING(v_a1) " +
                "GROUP BY v_p1.v_b1;";
        checkMVFixWithJoin(sql, 1, 1, 2, 0, null, null);

        sql = "SELECT v_p1.v_b1, v_p1.v_cnt, SUM(v_a1) " +
                "FROM v_p1 @joinType v_r1 USING(v_a1) " +
                "GROUP BY v_p1.v_b1, v_p1.v_cnt;";
        checkMVFixWithJoin(sql, 2, 1, 2, 1, null, null);

        sql = "SELECT v_p1.v_b1, v_p1.v_cnt, SUM(v_p1.v_a1) " +
                "FROM v_p1 @joinType v_r1 ON v_p1.v_a1 = v_r1.v_a1 " +
                "WHERE v_p1.v_a1 > 1 AND v_p1.v_cnt < 8 " +
                "GROUP BY v_p1.v_b1, v_p1.v_cnt;";
        checkMVFixWithJoin(sql, 2, 1, 2, 1, "v_cnt < 8", "v_a1 > 1");

        sql = "SELECT v_p1.v_b1, v_p1.v_cnt, SUM(v_p1.v_a1), MAX(v_p1.v_sum_c1) " +
                "FROM v_p1 @joinType v_r1 ON v_p1.v_a1 = v_r1.v_a1 " +
                "WHERE v_p1.v_a1 > 1 AND v_p1.v_cnt < 8 GROUP BY v_p1.v_b1, v_p1.v_cnt;";
        checkMVFixWithJoin(sql, 2, 2, 2, 2, "v_cnt < 8", "v_a1 > 1");

        sql = "SELECT v_r1.v_b1, SUM(v_a1) " +
                "FROM v_p1 @joinType v_r1 USING(v_a1) GROUP BY v_r1.v_b1;";
        checkMVFixWithJoin(sql, 1, 1, 2, 0, null, null);

        sql = "SELECT v_r1.v_b1, v_r1.v_cnt, SUM(v_a1) " +
                "FROM v_p1 @joinType v_r1 USING(v_a1) " +
                "GROUP BY v_r1.v_b1, v_r1.v_cnt;";
        checkMVFixWithJoin(sql, 2, 1, 2, 0, null, null);

        sql = "SELECT v_r1.v_b1, v_p1.v_cnt, SUM(v_a1) " +
                "FROM v_p1 @joinType v_r1 USING(v_a1) " +
                "GROUP BY v_r1.v_b1, v_p1.v_cnt;";
        checkMVFixWithJoin(sql, 2, 1, 2, 1, null, null);


        // Three tables joins.
        sql = "SELECT MAX(v_p1.v_a1) " +
                "FROM v_p1 @joinType v_r1 ON v_p1.v_a1 = v_r1.v_a1 " +
                "@joinType r1v ON v_p1.v_a1 = r1v.v_a1 ";
        checkMVFixWithJoin(sql, 0, 1, 2, 0, null, null);

        sql = "SELECT MIN(v_p1.v_cnt) " +
                "FROM v_p1 @joinType v_r1 ON v_p1.v_cnt = v_r1.v_cnt " +
                "@joinType r1v ON v_p1.v_cnt = r1v.v_cnt ";
        checkMVFixWithJoin(sql, 0, 1, 2, 1, null, null);

        sql = "SELECT MIN(v_p1.v_cnt) " +
                "FROM v_p1 @joinType v_r1 ON v_p1.v_cnt = v_r1.v_cnt " +
                "@joinType r1v ON v_p1.v_cnt = r1v.v_cnt " +
                "WHERE v_p1.v_cnt > 1 AND v_p1.v_a1 < 5 AND v_r1.v_b1 > 9";
        checkMVFixWithJoin(sql, 0, 1, 2, 1, "v_cnt > 1", "v_a1 < 5");


        sql = "SELECT v_p1.v_cnt, v_p1.v_b1, SUM(v_p1.v_sum_d1) " +
                "FROM v_p1 @joinType v_r1 ON v_p1.v_cnt = v_r1.v_cnt " +
                "@joinType r1v ON v_p1.v_cnt = r1v.v_cnt " +
                "GROUP BY v_p1.v_cnt, v_p1.v_b1";
        checkMVFixWithJoin(sql, 2, 1, 2, 2, null, null);

        sql = "SELECT v_p1.v_cnt, v_p1.v_b1, SUM(v_p1.v_sum_d1), MAX(v_r1.v_a1)  " +
                "FROM v_p1 @joinType v_r1 ON v_p1.v_cnt = v_r1.v_cnt " +
                "@joinType r1v ON v_p1.v_cnt = r1v.v_cnt " +
                "GROUP BY v_p1.v_cnt, v_p1.v_b1";
        checkMVFixWithJoin(sql, 2, 2, 2, 2, null, null);

        sql = "SELECT v_p1.v_cnt, v_p1.v_b1, SUM(v_p1.v_sum_d1) " +
                "FROM v_p1 @joinType v_r1 ON v_p1.v_cnt = v_r1.v_cnt " +
                "@joinType r1v ON v_p1.v_cnt = r1v.v_cnt " +
                "WHERE v_p1.v_cnt > 1 AND v_p1.v_a1 > 2 AND v_p1.v_sum_c1 < 3 AND v_r1.v_b1 < 4 " +
                "GROUP BY v_p1.v_cnt, v_p1.v_b1 ";
        checkMVFixWithJoin(sql, 2, 1, 2, 3, new String[] { "v_sum_c1 < 3)", "v_cnt > 1)" }, "v_a1 > 2");
        AbstractExpression.restoreVerboseExplainForDebugging(asItWas);
    }

    public void testENG5385() {
        boolean asItWas = AbstractExpression.disableVerboseExplainForDebugging();
        String sql = "";

        sql = "SELECT v_p1.v_a1 " + 
                " FROM v_p1 left join v_r1 " +
                " ON v_p1.v_a1 = v_r1.v_a1 AND v_p1.v_cnt = 2 ";
        checkMVFixWithJoin_ReAgg(sql, 2, 1, "v_cnt = 2", null);

        // When ENG-5385 is fixed, use the next line to check its plan.
//        checkMVFixWithJoin_reAgg(sql, 2, 1, null, null);
        AbstractExpression.restoreVerboseExplainForDebugging(asItWas);
    }

    public void testENG6962DistinctCases() {
        String sql;
        String sql_rewrote;
        sql = "SELECT distinct A1, B1 FROM R1";
        sql_rewrote = "SELECT A1, B1 FROM R1 GROUP BY A1, B1";
        checkQueriesPlansAreTheSame(sql, sql_rewrote);

        sql = "SELECT distinct A1+B1 FROM R1";
        sql_rewrote = "SELECT A1+B1 FROM R1 GROUP BY A1+B1";
        checkQueriesPlansAreTheSame(sql, sql_rewrote);
    }

    public void testENG389_Having() {
        String sql;

        boolean asItWas = AbstractExpression.disableVerboseExplainForDebugging();
        //      CREATE VIEW V_P1 (V_A1, V_B1, V_CNT, V_SUM_C1, V_SUM_D1)
        //      AS SELECT A1, B1, COUNT(*), SUM(C1), COUNT(D1)
        //      FROM P1  GROUP BY A1, B1;

        sql = "SELECT SUM(V_A1) FROM v_r1 HAVING v_cnt > 3";
        failToCompile(sql, "invalid HAVING expression");

        sql= "SELECT SUM(V_A1) FROM v_r1 HAVING 3 > 3";
        failToCompile(sql, "does not support HAVING clause without aggregation");

        sql = "SELECT V_A1, COUNT(v_cnt) FROM v_r1 GROUP BY v_a1 HAVING COUNT(v_cnt) > 1; ";
        checkHavingClause(sql, true, ".v_cnt) having (c2 > 1)");

        sql = "SELECT SUM(V_A1) FROM v_r1 HAVING avg(v_cnt) > 3; ";
        checkHavingClause(sql, true, ".v_cnt) having (column#1 > 3)");

        sql = "SELECT avg(v_cnt) FROM v_r1 HAVING avg(v_cnt) > 3; ";
        checkHavingClause(sql, true, ".v_cnt) having (c1 > 3)");

        AbstractExpression.restoreVerboseExplainForDebugging(asItWas);
    }

    private void checkHavingClause(String sql,
            boolean aggInline,
            Object aggPostFilters) {
        List<AbstractPlanNode> pns = compileToFragments(sql);
        AbstractPlanNode p = pns.get(0);
        AggregatePlanNode aggNode;

        ArrayList<AbstractPlanNode> nodesList = p.findAllNodesOfType(PlanNodeType.AGGREGATE);
        assertEquals(1, nodesList.size());
        p = nodesList.get(0);

        boolean isInline = p.isInline();
        assertEquals(aggInline, isInline);

        assertTrue(p instanceof AggregatePlanNode);
        aggNode = (AggregatePlanNode) p;

        String aggNodeStr = aggNode.toExplainPlanString().toLowerCase();

        if (aggPostFilters != null) {
            String[] aggFilterStrings = null;
            if (aggPostFilters instanceof String) {
                aggFilterStrings = new String[] { (String) aggPostFilters };
            }
            else {
                aggFilterStrings = (String[]) aggPostFilters;
            }
            for (String aggFilter : aggFilterStrings) {
                assertTrue(aggNodeStr.contains(aggFilter.toLowerCase()));
            }
        }
        else {
            assertNull(aggNode.getPostPredicate());
        }
    }

    private void checkMVFix_reAgg_MergeReceive(
            String sql,
            int numGroupByOfReaggNode, int numAggsOfReaggNode,
            SortDirectionType sortDirection) {
        checkMVReaggregateFeatureMergeReceive(sql, true,
                numGroupByOfReaggNode, numAggsOfReaggNode,
                false, false, sortDirection);
}

    private void checkMVFix_reAgg(
            String sql,
            int numGroupByOfReaggNode, int numAggsOfReaggNode) {
        checkMVReaggregateFeature(sql, true,
                -1, -1,
                numGroupByOfReaggNode, numAggsOfReaggNode,
                false, false);
    }

    private void checkMVFix_TopAgg_ReAgg(
            String sql,
            int numGroupByOfTopAggNode, int numAggsOfTopAggNode,
            int numGroupByOfReaggNode, int numAggsOfReaggNode) {
        checkMVReaggregateFeature(sql, true,
                numGroupByOfTopAggNode, numAggsOfTopAggNode,
                numGroupByOfReaggNode, numAggsOfReaggNode,
                false, false);
    }

    // topNode, reAggNode
    private void checkMVReaggregateFeatureMergeReceive(String sql,
            boolean needFix,
            int numGroupByOfReaggNode,
            int numAggsOfReaggNode,
            boolean aggPushdown,
            boolean aggInline,
            SortDirectionType sortDirection) {
        List<AbstractPlanNode> pns = compileToFragments(sql);
        assertEquals(2, pns.size());
        AbstractPlanNode p = pns.get(0);
        assertTrue(p instanceof SendPlanNode);
        p = p.getChild(0);
        if (p instanceof ProjectionPlanNode) {
            p = p.getChild(0);
        }
        AbstractPlanNode receiveNode = p;
        assertNotNull(receiveNode);

        AggregatePlanNode reAggNode = AggregatePlanNode.getInlineAggregationNode(receiveNode);

        if (needFix) {
            assertNotNull(reAggNode);

            assertEquals(numGroupByOfReaggNode, reAggNode.getGroupByExpressionsSize());
            assertEquals(numAggsOfReaggNode, reAggNode.getAggregateTypesSize());
        }
        else {
            assertNull(reAggNode);
        }

        p = pns.get(1);
        assertTrue(p instanceof SendPlanNode);
        p = p.getChild(0);

        assertTrue(p instanceof IndexScanPlanNode);
        assertEquals(sortDirection, ((IndexScanPlanNode)p).getSortDirection());

    }

    // topNode, reAggNode
    private List<AbstractPlanNode> checkMVReaggregateFeature(String sql, boolean needFix,
            int numGroupByOfTopAggNode, int numAggsOfTopAggNode,
            int numGroupByOfReaggNode, int numAggsOfReaggNode,
            boolean aggPushdown, boolean aggInline) {

        List<AbstractPlanNode> pns = compileToFragments(sql);
        assertEquals(2, pns.size());
        AbstractPlanNode p = pns.get(0);
        assertTrue(p instanceof SendPlanNode);
        p = p.getChild(0);

        if (p instanceof ProjectionPlanNode) {
            p = p.getChild(0);
        }

        if (p instanceof LimitPlanNode) {
            // No limit pushed down.
            p = p.getChild(0);
        }

        if (p instanceof OrderByPlanNode) {
            p = p.getChild(0);
        }
        HashAggregatePlanNode reAggNode = null;

        List<AbstractPlanNode> nodes = p.findAllNodesOfClass(AbstractReceivePlanNode.class);
        assertEquals(1, nodes.size());
        AbstractPlanNode receiveNode = nodes.get(0);

        // Indicates that there is no top aggregation node.
        if (numGroupByOfTopAggNode == -1) {
            if (needFix) {
                p = receiveNode.getParent(0);
                assertTrue(p instanceof HashAggregatePlanNode);
                reAggNode = (HashAggregatePlanNode) p;

                assertEquals(numGroupByOfReaggNode, reAggNode.getGroupByExpressionsSize());
                assertEquals(numAggsOfReaggNode, reAggNode.getAggregateTypesSize());

                p = p.getChild(0);
            }
            assertTrue(p instanceof ReceivePlanNode);

            p = pns.get(1);
            assertTrue(p instanceof SendPlanNode);
            p = p.getChild(0);

            assertTrue(p instanceof AbstractScanPlanNode);
            return pns;
        }

        if (p instanceof ProjectionPlanNode) {
            p = p.getChild(0);
        }

        //
        // Hash top aggregate node
        //
        AggregatePlanNode topAggNode = null;
        if (p instanceof AbstractJoinPlanNode) {
            // Inline aggregation with join
            topAggNode = AggregatePlanNode.getInlineAggregationNode(p);
        }
        else {
            assertTrue(p instanceof AggregatePlanNode);
            topAggNode = (AggregatePlanNode) p;
            p = p.getChild(0);
        }
        assertEquals(numGroupByOfTopAggNode, topAggNode.getGroupByExpressionsSize());
        assertEquals(numAggsOfTopAggNode, topAggNode.getAggregateTypesSize());

        if (needFix) {
            p = receiveNode.getParent(0);
            assertTrue(p instanceof HashAggregatePlanNode);
            reAggNode = (HashAggregatePlanNode) p;

            assertEquals(numGroupByOfReaggNode, reAggNode.getGroupByExpressionsSize());
            assertEquals(numAggsOfReaggNode, reAggNode.getAggregateTypesSize());

            p = p.getChild(0);
        }
        assertTrue(p instanceof ReceivePlanNode);

        // Test the second part
        p = pns.get(1);
        assertTrue(p instanceof SendPlanNode);
        p = p.getChild(0);

        if (aggPushdown) {
            assertTrue(!needFix);
            if (aggInline) {
                assertNotNull(AggregatePlanNode.getInlineAggregationNode(p));
            }
            else {
                assertTrue(p instanceof AggregatePlanNode);
                p = p.getChild(0);
            }
        }

        if (needFix) {
            assertTrue(p instanceof AbstractScanPlanNode);
        }
        else {
            assertTrue(p instanceof AbstractScanPlanNode ||
                    p instanceof AbstractJoinPlanNode);
        }
        return pns;
    }
}
