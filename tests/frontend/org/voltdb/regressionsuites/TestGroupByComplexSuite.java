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

package org.voltdb.regressionsuites;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.voltdb.BackendTarget;
import org.voltdb.VoltTable;
import org.voltdb.client.Client;
import org.voltdb.client.ClientResponse;
import org.voltdb.client.NullCallback;
import org.voltdb.compiler.VoltProjectBuilder;

/*
 * Functional tests of the statements compiled in the test suite
 * org.voltdb.planner.TestComplexGroupBySuite.
 */

public class TestGroupByComplexSuite extends RegressionSuite {
    private final static String [] tbs = {"R1","P1","P2","P3"};

    private void loadData(boolean extra) throws Exception {
        Client client = getClient();
        ClientResponse cr = null;

        // Empty data from table.
        for (String tb: tbs) {
            cr = client.callProcedure("@AdHoc", "DELETE FROM " + tb);
            assertEquals(ClientResponse.SUCCESS, cr.getStatus());
        }

        // Insert records into the table.
        // id, wage, dept, rate
        for (String tb: tbs) {
            String proc = tb + ".insert";
            client.callProcedure(proc, 1,  10,  1, "2013-06-18 02:00:00.123457");
            client.callProcedure(proc, 2,  20,  1, "2013-07-18 02:00:00.123457");
            client.callProcedure(proc, 3,  30,  1, "2013-07-18 10:40:01.123457");
            client.callProcedure(proc, 4,  40,  2, "2013-08-18 02:00:00.123457");
            client.callProcedure(proc, 5,  50,  2, "2013-09-18 02:00:00.123457");

            if (extra) {
                client.callProcedure(proc, 6,  10,  2, "2013-07-18 02:00:00.123457");
                client.callProcedure(proc, 7,  40,  2, "2013-09-18 02:00:00.123457");
            }
        }
    }

    private void strangeCasesAndOrderby() throws Exception {
        loadData(false);
        Client client = getClient();
        String sql;
        long[][] expected;

        for (String tb: tbs) {
            // Test GROUP BY PRIMARY KEY
            // Test pass-through columns, GROUP BY primary key
            sql = "SELECT dept, COUNT(wage) FROM " + tb +
                    " GROUP BY id ORDER BY dept DESC";
            expected = new long[][] {{2,1}, {2,1}, {1,1}, {1,1}, {1,1}};
            validateTableOfLongs(client, sql, expected);

            // Test duplicates, operator expression, GROUP BY primary key
            sql = "SELECT id, id, dept, dept+5 FROM " + tb +
                    " GROUP BY id ORDER BY id";
            expected = new long[][] {{1,1,1,6}, {2,2,1,6}, {3,3,1,6}, {4,4,2,7}, {5,5,2,7}};
            validateTableOfLongs(client, sql, expected);

            // Test function expression with GROUP BY primary key
            sql = "SELECT id, id + 1, SUM(wage)/2, ABS(dept-3) FROM " + tb +
                    " GROUP BY id ORDER BY id";
            expected = new long[][] {{1,2,5,2}, {2,3,10,2}, {3,4,15,2}, {4,5,20,1}, {5,6,25,1}};
            validateTableOfLongs(client, sql, expected);

            // Test ORDER BY alias FROM display list
            sql = "SELECT dept, COUNT(*) AS tag, SUM(wage) - 1 FROM " + tb +
                    " GROUP BY dept ORDER BY tag DESC";
            expected = new long[][] {{1, 3, 59}, {2, 2, 89}};
            validateTableOfLongs(client, sql, expected);
        }
    }

    public void testComplexAggsSuite() throws Exception {
        System.out.println("Test complex aggs...");
        complexAggs();
        complexAggsOrderbySuite();
        complexAggsDistinctLimit();
    }

    private void complexAggs() throws Exception {
        loadData(false);
        Client client = getClient();
        String sql;
        long[][] expected;

        for (String tb: tbs) {
            // Test normal GROUP BY with expressions, addition, division for AVG.
            sql = "SELECT dept, SUM(wage), COUNT(wage)+5, SUM(wage)/COUNT(wage) " +
                    "FROM " + tb + " GROUP BY dept ORDER BY dept DESC;";
            expected = new long[][] {{2, 90, 7, 45}, {1, 60, 8, 20}};
            validateTableOfLongs(client, sql, expected);

            // Test different GROUP BY column order, non-grouped TVE, sum for column, division
            query = "SELECT sum(wage)/count(wage) + 1, dept, " +
                    "SUM(wage+1), SUM(wage)/2 " +
                    "FROM " + tb + " GROUP BY dept ORDER BY dept";
            expected = new long[][] {{21, 1, 63, 30}, {46, 2, 92, 45}};
            validateTableOfLongs(client, sql, expected);

            // Test Complex Agg with functions
            sql = "SELECT dept, SUM(ABS(wage) - 1) AS tag, " +
                    "(COUNT(*)+SUM(dept*2))/2 " +
                    "FROM " + tb + " GROUP BY dept ORDER BY ABS(dept)";
            expected = new long[][] {{1, 57, 4}, {2, 88, 5}};
            validateTableOfLongs(client, sql, expected);

            // Test SUM()/COUNT(), Addition
            sql = "SELECT dept, SUM(wage), COUNT(wage), AVG(wage), " +
                    "MAX(wage), MIN(wage), SUM(wage)/COUNT(wage),  " +
                    "MAX(wage)+MIN(wage)+1 " +
                    "FROM " + tb + " GROUP BY dept ORDER BY dept";
            expected = new long[][] {
                {1, 60, 3, 20, 30, 10, 20, 41},
                {2, 90, 2, 45, 50, 40, 45, 91}};
            validateTableOfLongs(client, sql, expected);
        }
    }

    private void complexAggsOrderbySuite() throws Exception {
        loadData(false);
        Client client = getClient();
        String sql;
        long[][] expected;

        for (String tb: tbs) {
            // (0) Test no GROUP BY cases
            sql = "SELECT id+dept FROM " + tb + " ORDER BY ABS(id+dept)";
            expected = new long[][] {{2}, {3}, {4}, {6}, {7}};
            validateTableOfLongs(client, sql, expected);

            sql = "SELECT id+dept FROM " + tb + " ORDER BY id+dept";
            expected = new long[][] {{2}, {3}, {4}, {6}, {7}};
            validateTableOfLongs(client, sql, expected);

            sql = "SELECT id+dept FROM " + tb + " ORDER BY id+dept, wage";
            expected = new long[][] {{2}, {3}, {4}, {6}, {7}};
            validateTableOfLongs(client, sql, expected);

            sql = "SELECT id+dept, wage FROM " + tb + " ORDER BY id+dept, wage";
            expected = new long[][] {{2, 10}, {3,20}, {4,30}, {6,40}, {7,50}};
            validateTableOfLongs(client, sql, expected);

            // (1) Test Order by COUNT(*) without complex expression
            // Test ORDER BY agg with tag
            sql = "SELECT dept, COUNT(*) AS tag, SUM(wage) FROM " + tb +
                    " GROUP BY dept ORDER BY tag DESC";
            expected = new long[][] {{1, 3, 60}, {2, 2, 90}};
            validateTableOfLongs(client, sql, expected);

            // (2) Test Order by COUNT(*) with complex expression
            // Test ORDER BY agg with tag
            sql = "SELECT dept, COUNT(*) AS tag, SUM(wage) - 1 FROM " + tb +
                    " GROUP BY dept ORDER BY tag DESC";
            expected = new long[][] {{1, 3, 59}, {2, 2, 89}};
            validateTableOfLongs(client, sql, expected);

            // (3) Test Order by with FUNCTION expression, no GROUP BY column in display columns
            // Test Order by with unambiguous alias.
            sql = "SELECT ABS(dept) AS tag1, SUM(ABS(wage) - 1) AS tag2, " +
                    "(COUNT(*)+SUM(dept*2))/2 FROM " + tb + " GROUP BY dept ORDER BY tag1";
            expected = new long[][] {{1, 57, 4}, {2, 88, 5}};
            validateTableOfLongs(client, sql, expected);

            // Test Order by without any alias.
            sql = "SELECT ABS(dept), SUM(ABS(wage) - 1) AS tag, " +
                    "(COUNT(*)+SUM(dept*2))/2 FROM " + tb + " GROUP BY dept ORDER BY ABS(dept)";
            expected = new long[][] {{1, 57, 4}, {2, 88, 5}};
            validateTableOfLongs(client, sql, expected);
        }
    }

    private void complexAggsDistinctLimit() throws Exception {
        loadData(true);
        Client client = getClient();
        String sql;
        long[][] expected;

        for (String tb: tbs) {
            // Test distinct with complex aggregations.
            sql = "SELECT dept, COUNT(wage), SUM(distinct wage), SUM(wage), " +
                    "COUNT(distinct wage)+5, SUM(wage)/(COUNT(wage)+1) FROM " + tb +
                    " GROUP BY dept ORDER BY dept DESC;";
            expected = new long[][] {{2, 4, 100, 140, 8, 28}, {1, 3, 60, 60, 8, 15}};
            validateTableOfLongs(client, sql, expected);

            // Test limit with complex aggregation.
            sql = "SELECT wage, SUM(id)+1, SUM(id+1),  SUM(dept+3)/COUNT(dept) FROM " + tb +
                    " GROUP BY wage ORDER BY wage ASC LIMIT 4 ;";
            expected = new long[][] {{10, 8, 9, 4}, {20, 3, 3, 4}, {30, 4, 4, 4}, {40, 12, 13, 5}};
            validateTableOfLongs(client, sql, expected);

            // Test distinct limit together with complex aggregation.
            sql = "SELECT wage, SUM(id)+1, SUM(id+1),  SUM(dept+3)/COUNT(distinct dept) FROM " + tb +
                    " GROUP BY wage ORDER BY wage ASC LIMIT 4 ;";
            expected = new long[][] {{10, 8, 9, 4}, {20, 3, 3, 4}, {30, 4, 4, 4}, {40, 12, 13, 10}};
            validateTableOfLongs(client, sql, expected);
        }
    }

    public void testComplexGroupBySuite() throws Exception {
        System.out.println("Test complex GROUP BY...");
        complexGroupBy();
        complexGroupByDistinctLimit();
        complexGroupByOrderbySuite();

        orderbyColumnsNotInDisplayList();
    }

    private void complexGroupBy() throws Exception {
        loadData(true);
        Client client = getClient();
        ClientResponse cr;
        VoltTable vt;
        String sql;
        long[][] expected;

        for (String tb: tbs) {
            // (1) Without extra aggregation expression
            // Test complex group-by (Function expression) without complex aggregation.
            sql = "SELECT ABS(dept) AS tag, COUNT(wage) FROM " + tb +
                    " GROUP BY ABS(dept) ORDER BY tag ";
            expected = new long[][] {{1, 3}, {2, 4}};
            validateTableOfLongs(client, sql, expected);

            // repeat above test with GROUP BY ALIAS feature
            sql = "SELECT ABS(dept) AS tag, COUNT(wage) FROM " + tb +
                    " GROUP BY tag ORDER BY tag ";
            expected = new long[][] {{1, 3}, {2, 4}};
            validateTableOfLongs(client, sql, expected);

            // Test complex group-by (normal expression) without complex aggregation.
            // Actually this AdHoc query has an extra projection node
            // because of the pass-by column dept in ORDER BY columns.
            // ParameterValueExpression equal function return false.
            // AggResultColumns contains: dept+1, COUNT(wage) and dept.
            // If it is a stored procedure, there is no extra projection node.
            // AggResultColumns: dept+1 and COUNT(wage).
            sql = "SELECT (dept+1) AS tag, COUNT(wage) FROM " + tb +
                    " GROUP BY dept+1 ORDER BY tag ";
            expected = new long[][] {{2, 3}, {3, 4}};
            validateTableOfLongs(client, sql, expected);

            // repeat above test with GROUP BY ALIAS feature
            sql = "SELECT (dept+1) AS tag, COUNT(wage) FROM " + tb +
                    " GROUP BY tag ORDER BY tag ";
            expected = new long[][] {{2, 3}, {3, 4}};
            validateTableOfLongs(client, sql, expected);

            // test GROUP BY alias with constants in expression for stored procedure
            cr = client.callProcedure(tb +"_GroupByAlias1", 1);
            assertEquals(ClientResponse.SUCCESS, cr.getStatus());
            vt = cr.getResults()[0];
            expected = new long[][] {{2, 3}, {3, 4}};
            validateTableOfLongs(vt, expected);

            // (2) With extra aggregation expression
            // Test complex group-by with with complex aggregation.
            sql = "SELECT ABS(dept) AS tag, COUNT(wage)+1 FROM " + tb +
                    " GROUP BY ABS(dept) ORDER BY tag DESC;";
            expected = new long[][] {{2, 5}, {1, 4}};
            validateTableOfLongs(client, sql, expected);

            // repeat above test with GROUP BY ALIAS feature
            sql = "SELECT ABS(dept) AS tag, COUNT(wage)+1 FROM " + tb +
                    " GROUP BY tag ORDER BY tag DESC;";
            expected = new long[][] {{2, 5}, {1, 4}};
            validateTableOfLongs(client, sql, expected);

            // Test more complex group-by with with complex aggregation.
            sql = "SELECT ABS(dept-2) AS tag, COUNT(wage)+1 FROM " + tb +
                    " GROUP BY ABS(dept-2) ORDER BY tag;";
            expected = new long[][] {{0, 5}, {1, 4}};
            validateTableOfLongs(client, sql, expected);

            // repeat above test with GROUP BY ALIAS feature
            sql = "SELECT ABS(dept-2) AS tag, COUNT(wage)+1 FROM " + tb +
                    " GROUP BY tag ORDER BY tag;";
            expected = new long[][] {{0, 5}, {1, 4}};
            validateTableOfLongs(client, sql, expected);

            // test GROUP BY alias with constants in expression for stored procedure
            cr = client.callProcedure(tb +"_GroupByAlias2", -2);
            assertEquals(ClientResponse.SUCCESS, cr.getStatus());
            vt = cr.getResults()[0];
            expected = new long[][] {{0, 5}, {1, 4}};
            validateTableOfLongs(vt, expected);


            // More hard general test case with multi GROUP BY columns and complex aggs
            sql = "SELECT ABS(dept-2) AS tag, wage, wage/2, COUNT(*)*2, " +
                    "SUM(id)/COUNT(id)+1 FROM " + tb +
                    " GROUP BY ABS(dept-2), wage ORDER BY tag, wage;";
            expected = new long[][] {
                {0,10,5,2,7}, {0,40,20,4,6}, {0,50,25,2,6},
                {1,10,5,2,2}, {1,20,10,2,3}, {1,30,15,2,4}};
            validateTableOfLongs(client, sql, expected);

            // repeat above test with GROUP BY ALIAS feature
            sql = "SELECT ABS(dept-2) AS tag, wage, wage/2, COUNT(*)*2, " +
                    "SUM(id)/COUNT(id)+1 FROM " + tb + " GROUP BY tag, wage ORDER BY tag, wage;";
            expected = new long[][] {
                {0,10,5,2,7}, {0,40,20,4,6}, {0,50,25,2,6},
                {1,10,5,2,2}, {1,20,10,2,3}, {1,30,15,2,4}};
            validateTableOfLongs(client, sql, expected);

            if ( ! isHSQL()) {
                // Timestamp function for complex GROUP BY
                sql = "SELECT truncate(day, tm) AS tag, COUNT(id)+1, " +
                        "SUM(wage)/COUNT(wage) FROM " + tb +
                        " GROUP BY truncate(day, tm) ORDER BY tag;";
                assertEquals(ClientResponse.SUCCESS, cr.getStatus());
                vt = cr.getResults()[0];
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

                Date time1 = dateFormat.parse("2013-06-18 00:00:00.000");
                Date time2 = dateFormat.parse("2013-07-18 00:00:00.000");
                Date time3 = dateFormat.parse("2013-08-18 00:00:00.000");
                Date time4 = dateFormat.parse("2013-09-18 00:00:00.000");
                expected = new long[][] {
                    {time1.getTime()*1000, 2, 10},
                    {time2.getTime()*1000, 4, 20},
                    {time3.getTime()*1000, 2, 40},
                    {time4.getTime()*1000, 3, 45} };
                validateTableOfLongs(client, sql, expected);
            }
        }
    }

    private void complexGroupByDistinctLimit() throws Exception {
        loadData(true);
        Client client = getClient();
        String sql;
        long[][] expected;

        for (String tb: tbs) {
            // (1) Without extra aggregation expression
            // Test complex group-by (Function expression) without complex aggregation.
            // (Duplicates: two 2s for dept)
            // Test distinct
            sql = "SELECT ABS(wage) AS tag, COUNT(distinct dept) FROM " + tb +
                    " GROUP BY ABS(wage) ORDER BY tag ";
            expected = new long[][] {{10, 2}, {20, 1}, {30, 1}, {40, 1}, {50, 1}};
            validateTableOfLongs(client, sql, expected);

            // Test limit
            sql = "SELECT ABS(wage) AS tag, COUNT(dept) FROM " + tb +
                    " GROUP BY ABS(wage) ORDER BY tag limit 4";
            expected = new long[][] {{10, 2}, {20, 1}, {30, 1}, {40, 2}};
            validateTableOfLongs(client, sql, expected);

            // Test distinct and limit
            sql = "SELECT ABS(wage) AS tag, COUNT(distinct dept) FROM " + tb +
                    " GROUP BY ABS(wage) ORDER BY tag limit 4";
            expected = new long[][] {{10, 2}, {20, 1}, {30, 1}, {40, 1}};
            validateTableOfLongs(client, sql, expected);


            // (2) With extra aggregation expression
            // Test complex group-by with with complex aggregation.
            // (Duplicates: two 2s for dept)
            // Test distinct
            sql = "SELECT ABS(wage) AS tag, COUNT(distinct dept)+1 FROM " + tb +
                    " GROUP BY ABS(wage) ORDER BY tag ";
            expected = new long[][] {{10, 3}, {20, 2}, {30, 2}, {40, 2}, {50, 2}};
            validateTableOfLongs(client, sql, expected);

            // Test limit
            sql = "SELECT ABS(wage) AS tag, COUNT(dept)+1 FROM " + tb +
                    " GROUP BY ABS(wage) ORDER BY tag limit 4";
            expected = new long[][] {{10, 3}, {20, 2}, {30, 2}, {40, 3}};
            validateTableOfLongs(client, sql, expected);

            // Test distinct and limit
            sql = "SELECT ABS(wage) AS tag, COUNT(distinct dept)+1 FROM " + tb +
                    " GROUP BY ABS(wage) ORDER BY tag limit 4 ";
            expected = new long[][] {{10, 3}, {20, 2}, {30, 2}, {40, 2}};
            validateTableOfLongs(client, sql, expected);


            // (3) More hard general test case with multi GROUP BY columns and complex aggs
            // (Duplicates: two 40s for wage)
            // Test distinct
            sql = "SELECT ABS(dept-2) AS tag, wage, wage/2, COUNT(distinct wage)*2, " +
                    "SUM(id)/COUNT(id)+1 FROM " + tb +
                    " GROUP BY ABS(dept-2), wage ORDER BY tag, wage;";
            expected = new long[][] {
                {0,10,5,2,7}, {0,40,20,2,6}, {0,50,25,2,6},
                {1,10,5,2,2}, {1,20,10,2,3}, {1,30,15,2,4}};
            validateTableOfLongs(client, sql, expected);

            // Test Limit
            sql = "SELECT ABS(dept-2) AS tag, wage, wage/2, COUNT(wage)*2, " +
                    "SUM(id)/COUNT(id)+1 FROM " + tb +
                    " GROUP BY ABS(dept-2), wage ORDER BY tag, wage LIMIT 5;";
            expected = new long[][] {
                {0,10,5,2,7}, {0,40,20,4,6}, {0,50,25,2,6},
                {1,10,5,2,2}, {1,20,10,2,3}};
            validateTableOfLongs(client, sql, expected);

            // Test distinct and limit
            sql = "SELECT ABS(dept-2) AS tag, wage, wage/2, COUNT(distinct wage)*2, " +
                    "SUM(id)/COUNT(id)+1 FROM " + tb +
                    " GROUP BY ABS(dept-2), wage ORDER BY tag, wage LIMIT 5;";
            expected = new long[][] {
                {0,10,5,2,7}, {0,40,20,2,6}, {0,50,25,2,6},
                {1,10,5,2,2}, {1,20,10,2,3}};
            validateTableOfLongs(client, sql, expected);
        }
    }


    private void complexGroupByOrderbySuite() throws Exception {
        loadData(true);
        Client client = getClient();
        String sql;
        long[][] expected;

        for (String tb: tbs) {
            //(1) Test complex group-by with no extra aggregation expressions.
            // Test ORDER BY with tag
            sql = "SELECT ABS(dept) AS tag, COUNT(wage), SUM(id), AVG(wage)  FROM " + tb +
                    " GROUP BY ABS(dept) ORDER BY tag ";
            expected = new long[][] {{1, 3, 6, 20}, {2, 4, 22, 35}};
            validateTableOfLongs(client, sql, expected);

            // Test ORDER BY without tag
            sql = "SELECT ABS(dept) AS tag, COUNT(wage), SUM(id), AVG(wage) FROM " + tb +
                    " GROUP BY ABS(dept) ORDER BY ABS(dept) ";
            expected = new long[][] {{1, 3, 6, 20}, {2, 4, 22, 35}};
            validateTableOfLongs(client, sql, expected);

            //(2) Test complex group-by with complex aggregation.
            // Test ORDER BY with tag
            sql = "SELECT ABS(dept-2) AS tag, COUNT(wage)+1, AVG(wage)/2 FROM " + tb +
                    " GROUP BY ABS(dept-2) ORDER BY tag;";
            expected = new long[][] {{0, 5, 17}, {1, 4, 10}};
            validateTableOfLongs(client, sql, expected);

            // Test ORDER BY without tag
            sql = "SELECT ABS(dept-2) AS tag, COUNT(wage)+1, AVG(wage)/2 FROM " + tb +
                    " GROUP BY ABS(dept-2) ORDER BY ABS(dept-2);";
            expected = new long[][] {{0, 5, 17}, {1, 4, 10}};
            validateTableOfLongs(client, sql, expected);

            //(3) More hard general test cases with multi GROUP BY columns and complex aggs
            // Test ORDER BY with tag
            sql = "SELECT ABS(dept-2) AS tag, wage, wage/2, COUNT(*)*2, " +
                    "SUM(id)/COUNT(id)+1 FROM " + tb + " GROUP BY ABS(dept-2), wage ORDER BY tag, wage;";
            expected = new long[][] {
                {0,10,5,2,7}, {0,40,20,4,6}, {0,50,25,2,6},
                {1,10,5,2,2}, {1,20,10,2,3}, {1,30,15,2,4}};
            validateTableOfLongs(client, sql, expected);

            // Test ORDER BY without tag
            sql = "SELECT ABS(dept-2) AS tag, wage, wage/2, COUNT(*)*2, " +
                    "SUM(id)/COUNT(id)+1 FROM " + tb + " GROUP BY ABS(dept-2), wage ORDER BY ABS(dept-2), wage;";
            expected = new long[][] {
                {0,10,5,2,7}, {0,40,20,4,6}, {0,50,25,2,6},
                {1,10,5,2,2}, {1,20,10,2,3}, {1,30,15,2,4}};
            validateTableOfLongs(client, sql, expected);

            // Test ORDER BY without tag and not in display columns
            sql = "SELECT wage, wage/2, COUNT(*)*2, SUM(id)/COUNT(id)+1 FROM " + tb +
                    " GROUP BY ABS(dept-2), wage ORDER BY ABS(dept-2), wage;";
            expected = new long[][] {{10,5,2,7}, {40,20,4,6}, {50,25,2,6}, {10,5,2,2}, {20,10,2,3}, {30,15,2,4}};
            validateTableOfLongs(client, sql, expected);


            //(4) Other ORDER BY expressions (id+dept), expressions on that.
            sql = "SELECT id+dept, SUM(wage)+1 FROM " + tb +
                    " GROUP BY id+dept ORDER BY ABS(id+dept)";
            expected = new long[][] {{2, 11}, {3,21}, {4,31}, {6,41}, {7,51}, {8,11}, {9,41}};
            validateTableOfLongs(client, sql, expected);

            sql = "SELECT id+dept, AVG(wage) FROM " + tb +
                    " GROUP BY id+dept ORDER BY ABS(id+dept)";
            expected = new long[][] {{2, 10}, {3,20}, {4,30}, {6,40}, {7,50}, {8,10}, {9,40}};
            validateTableOfLongs(client, sql, expected);

            sql = "SELECT id+dept FROM " + tb +
                    " GROUP BY id+dept ORDER BY ABS(id+dept)";
            expected = new long[][] {{2}, {3}, {4}, {6}, {7}, {8}, {9}};
            validateTableOfLongs(client, sql, expected);

            sql = "SELECT id+dept, wage FROM " + tb +
                    " GROUP BY id+dept, wage ORDER BY ABS(id+dept), ABS(wage)";
            expected = new long[][] {{2, 10}, {3,20}, {4,30}, {6,40}, {7,50}, {8,10}, {9,40}};
            validateTableOfLongs(client, sql, expected);


            // Expressions on the columns from selected list
            sql = "SELECT id+dept, AVG(wage) AS tag FROM " + tb +
                    " GROUP BY id+dept ORDER BY ABS(tag), id+dept";
            expected = new long[][] {{2, 10}, {8,10}, {3,20}, {4,30}, {6,40}, {9,40}, {7,50}};
            validateTableOfLongs(client, sql, expected);

            sql = "SELECT id+dept, AVG(wage) AS tag FROM " + tb +
                    " GROUP BY id+dept ORDER BY ABS(AVG(wage)), id+dept";
            expected = new long[][] {{2, 10}, {8,10}, {3,20}, {4,30}, {6,40}, {9,40}, {7,50}};
            validateTableOfLongs(client, sql, expected);

            sql = "SELECT id+dept, AVG(wage) AS tag FROM " + tb +
                    " GROUP BY id+dept ORDER BY ABS(AVG(wage)) + 1, id+dept";
            expected = new long[][] {{2, 10}, {8,10}, {3,20}, {4,30}, {6,40}, {9,40}, {7,50}};
            validateTableOfLongs(client, sql, expected);

            sql = "SELECT id+dept, AVG(wage)+1 AS tag FROM " + tb +
                    " GROUP BY id+dept ORDER BY ABS(tag), id+dept";
            expected = new long[][] {{2, 11}, {8,11}, {3,21}, {4,31}, {6,41}, {9,41}, {7,51}};
            validateTableOfLongs(client, sql, expected);

            sql = "SELECT id+dept, AVG(wage)+1 AS tag FROM " + tb +
                    " GROUP BY id+dept ORDER BY ABS(AVG(wage)+1), id+dept";
            expected = new long[][] {{2, 11}, {8,11}, {3,21}, {4,31}, {6,41}, {9,41}, {7,51}};
            validateTableOfLongs(client, sql, expected);

            sql = "SELECT id+dept, AVG(wage)+1 AS tag FROM " + tb +
                    " GROUP BY id+dept ORDER BY ABS(AVG(wage)+1) + 1, id+dept";
            expected = new long[][] {{2, 11}, {8,11}, {3,21}, {4,31}, {6,41}, {9,41}, {7,51}};
            validateTableOfLongs(client, sql, expected);
        }
    }

    public void testOtherCases() throws Exception {
        System.out.println("Test other cases...");
        strangeCasesAndOrderby();

        ENG4285();
        ENG5016();

        supportedCases();
        unsupportedCases();

        ENG7046();
    }

    private void ENG4285() throws Exception {
        loadData(false);
        Client client = getClient();
        String sql;
        long[][] expected;

        for (String tb: tbs) {
            sql = "SELECT dept, SUM(wage-id) FROM " + tb +
                    " GROUP BY dept ORDER BY dept";
            expected = new long[][] {{1, 54}, {2, 81}};
            validateTableOfLongs(client, sql, expected);

            sql = "SELECT dept, SUM(wage-id), AVG(wage-id), COUNT(*)" +
                    " FROM " + tb + " GROUP BY dept ORDER BY dept";
            expected = new long[][] {{1, 54, 18, 3}, {2, 81, 40, 2}};
            validateTableOfLongs(client, sql, expected);

            sql = "SELECT dept, SUM(wage-id) + 1, " +
                    "AVG(wage-id), COUNT(*) FROM " + tb +
                    " GROUP BY dept ORDER BY dept";
            expected = new long[][] {{1, 55, 18, 3}, {2, 82, 40, 2}};
            validateTableOfLongs(client, sql, expected);

            sql = "SELECT dept, SUM(wage-extract(month FROM tm)), " +
                    "AVG(wage-extract(month FROM tm)), COUNT(dept) FROM " + tb +
                    " GROUP BY dept ORDER BY dept";
            expected = new long[][] {{1, 40, 13, 3}, {2, 73, 36, 2}};
            validateTableOfLongs(client, sql, expected);
        }
    }

    // Test GROUP BY columns do not have to be in display columns.
    private void ENG5016() throws Exception {
        loadData(false);
        Client client = getClient();
        String sql;
        VoltTable vt;
        long[][] expected;

        for (String tb: tbs) {
            sql = "SELECT COUNT(*), SUM(wage) FROM " + tb +
                    " GROUP BY dept ORDER BY SUM(wage)";
            expected = new long[][] {{3, 60}, {2, 90}};
            validateTableOfLongs(client, sql, expected);

            sql = "SELECT COUNT(*) AS tag, SUM(wage), SUM(wage) FROM " + tb +
                    " GROUP BY dept ORDER BY tag";
            expected = new long[][] {{2, 90, 90}, {3, 60, 60}};
            vt = client.callProcedure("@AdHoc", sql).getResults()[0];
            validateTableOfLongs(vt, expected);

            // Demo bug, ENG-5149
            // Check column alias for the identical aggregation
            assertEquals("C2", vt.getColumnName(1));
            assertEquals("C3", vt.getColumnName(2));
            assertEquals(1, vt.getColumnIndex("C2"));
            assertEquals(2, vt.getColumnIndex("C3"));

            sql = "SELECT COUNT(*) AS tag, " +
                    "SUM(wage)+1 AS NO_BUG, SUM(wage)+1 FROM " + tb +
                    " GROUP BY dept ORDER BY tag";
            expected = new long[][] {{2, 91, 91}, {3, 61, 61}};
            vt = client.callProcedure("@AdHoc", sql).getResults()[0];
            validateTableOfLongs(vt, expected);

            assertEquals("NO_BUG", vt.getColumnName(1));
            assertEquals("C3", vt.getColumnName(2));
            assertEquals(1, vt.getColumnIndex("NO_BUG"));
            assertEquals(2, vt.getColumnIndex("C3"));
        }
    }

    private void supportedCases() throws Exception {
        loadData(false);
        Client client = getClient();
        String sql;
        long[][] expected;

        for (String tb: tbs) {
            // Test ORDER BY agg without tag
            sql = "SELECT dept, COUNT(*), SUM(wage) FROM " + tb +
                    " GROUP BY dept ORDER BY SUM(wage)";
            expected = new long[][] {{1, 3, 60}, {2, 2, 90}};
            validateTableOfLongs(client, sql, expected);

            sql = "SELECT dept, COUNT(*) AS tag, SUM(wage) FROM " + tb +
                    " GROUP BY dept ORDER BY COUNT(*) DESC";
            expected = new long[][] {{1, 3, 60}, {2, 2, 90}};
            validateTableOfLongs(client, sql, expected);

            sql = "SELECT dept, COUNT(*), SUM(wage) FROM " + tb +
                    " GROUP BY dept ORDER BY COUNT(*) DESC";
            expected = new long[][] {{1, 3, 60}, {2, 2, 90}};
            validateTableOfLongs(client, sql, expected);

            sql = "SELECT dept, COUNT(*) AS tag, SUM(wage) - 1 FROM " + tb +
                    " GROUP BY dept ORDER BY COUNT(*) DESC";
            expected = new long[][] {{1, 3, 59}, {2, 2, 89}};
            validateTableOfLongs(client, sql, expected);
        }
    }

    private void ENG7046() throws Exception {
        Client client = getClient();
        VoltTable vt;
        client.callProcedure("TB_STRING.insert", 1, "MA");

        if ( ! isHSQL()) {
            // Hsql does not support DECODE function
            vt = client.callProcedure("@AdHoc",
                    "SELECT MIN(decode(state, upper(state), state, " +
                            "state || ' with this kind of rambling string added to it may not be inlinable')) " +
                            "FROM tb_string").getResults()[0];
            validateTableColumnOfScalarVarchar(vt, new String[] {"MA"});
        }
    }

    // Test unsupported ORDER BY column not in display columns
    private void unsupportedCases() throws Exception {
        loadData(false);
        Client client = getClient();
        String sql;

        for (String tb: tbs) {
            // Test ORDER BY agg not in display columns
            sql = "SELECT dept, AVG(wage), SUM(wage) FROM " + tb +
                    " GROUP BY dept ORDER BY COUNT(*) DESC";
            verifyProcFails(client, "invalid ORDER BY expression", "@AdHoc", sql);

            sql = "SELECT dept, COUNT(*) FROM " + tb +
                    " GROUP BY dept ORDER BY SUM(wage) DESC";
            verifyProcFails(client, "invalid ORDER BY expression", "@AdHoc", sql);

            // Test GROUP BY column not in display columns
            sql = "SELECT AVG(wage), SUM(wage) FROM " + tb +
                    " GROUP BY dept ORDER BY COUNT(*) DESC";
            verifyProcFails(client, "invalid ORDER BY expression", "@AdHoc", sql);

            // Test ORDER BY agg not in display columns
            sql = "SELECT dept, AVG(wage), SUM(wage) - 1 FROM " + tb +
                    " GROUP BY dept ORDER BY COUNT(*) DESC";
            verifyProcFails(client, "invalid ORDER BY expression", "@AdHoc", sql);

            // Test GROUP BY column not in display columns
            sql = "SELECT AVG(wage), SUM(wage) - 1 FROM " + tb +
                    " GROUP BY dept ORDER BY COUNT(*) DESC";
            verifyProcFails(client, "invalid ORDER BY expression", "@AdHoc", sql);

            // Test ORDER BY without tag and not in display columns,
            // and not equal to GROUP BY columns
            sql = "SELECT COUNT(wage), SUM(id), AVG(wage)  FROM " + tb +
                    " GROUP BY ABS(dept) ORDER BY COUNT(*) ";
            verifyProcFails(client, "invalid ORDER BY expression", "@AdHoc", sql);
        }
    }

    public void testAggregateOnJoin() throws Exception {
        System.out.println("Test aggs on joins...");
        loadData(false);
        Client client = getClient();
        VoltTable vt;
        String sql;
        long[][] expected;

        sql = "SELECT r1.id, COUNT(*) FROM r1, p2 " +
               " WHERE r1.id = p2.dept GROUP BY r1.id ORDER BY 1;";
        vt = client.callProcedure("@Explain", sql).getResults()[0];
        assertTrue(vt.toString().toLowerCase().contains("inline hash"));

        expected = new long[][] {{1,3}, {2,2}};
        validateTableOfLongs(client, sql, expected);
    }

    public void testHavingClause() throws Exception {
        System.out.println("test Having clause...");
        loadData(false);
        Client client = getClient();
        String sql;
        long[][] expected;

        for (String tb: tbs) {
            // Test normal GROUP BY with expressions, addition, division for AVG.
            sql = "SELECT dept, SUM(wage), COUNT(wage)+5, SUM(wage)/COUNT(wage) " +
                    "FROM " + tb + " GROUP BY dept ORDER BY dept DESC;";
            expected = new long[][] {{2, 90, 7, 45}, {1, 60, 8, 20}};
            validateTableOfLongs(client, sql, expected);

            // Test having
            sql = "SELECT dept, SUM(wage) FROM " + tb +
                    " GROUP BY dept HAVING SUM(wage) > 60 ORDER BY dept DESC;";
            expected = new long[][] {{2, 90}};
            validateTableOfLongs(client, sql, expected);

            sql = "SELECT dept, SUM(wage), COUNT(wage)+5 FROM " + tb +
                    " GROUP BY dept HAVING COUNT(wage)+5 <> 7 ORDER BY dept DESC;";
            expected = new long[][] {{1, 60, 8}};
            validateTableOfLongs(client, sql, expected);


            // Test agg in having clause that is not in display list
            sql = "SELECT dept, SUM(wage) FROM " + tb +
                    " GROUP BY dept HAVING COUNT(wage)+5 <> 7 ORDER BY dept DESC;";
            expected = new long[][] {{1, 60}};
            validateTableOfLongs(client, sql, expected);


            // Test normal GROUP BY with expressions, addition, division for AVG.
            sql = "SELECT dept, SUM(wage), COUNT(wage)+5, " +
                    "SUM(wage)/COUNT(wage) FROM " + tb + " GROUP BY dept HAVING SUM(wage) < 80 ORDER BY dept DESC;";
            expected = new long[][] {{1, 60, 8, 20}};
            validateTableOfLongs(client, sql, expected);

            // Test Having with COUNT(*)
            sql = "SELECT COUNT(*) FROM " + tb +
                    " HAVING COUNT(*) > 60 " +
                    " ORDER BY 1 DESC;";
            expected = new long[][] {};
            validateTableOfLongs(client, sql, expected);

            // Test Having with AVG
            sql = "SELECT AVG(wage) FROM " + tb +
                    " HAVING SUM(id) > 20 " +
                    " ORDER BY 1 DESC;";
            expected = new long[][] {};
            validateTableOfLongs(client, sql, expected);
        }
    }

    // This test case will trigger temp table "delete as we go" feature on join node
    // Turned off this test case because of valgrind timeout.
    public void turnOfftestAggregateOnJoinForMemoryIssue() throws Exception {
        Client client = getClient();
        ClientResponse cr;
        String sql;
        long[][] expected;

        // Empty data from table.
        for (String tb: tbs) {
            cr = client.callProcedure("@AdHoc", "DELETE FROM " + tb);
            assertEquals(ClientResponse.SUCCESS, cr.getStatus());
        }
        int scale = 10;
        int numOfRecords = scale * 1000;
        // Insert records into the table.
        // id, wage, dept, rate
        String timeStamp = "2013-06-18 02:00:00.123457";

        String[] myProcs = {"R1.insert", "P1.insert"};
        for (String insertProc: myProcs) {
            for (int ii = 1; ii <= numOfRecords; ii++) {
                client.callProcedure(new NullCallback(),
                        insertProc, ii,  ii % 1000,  ii % 2, timeStamp);
            }
        }

        try {
            client.drain();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Serial aggregation because of no GROUP BY
        sql = "SELECT SUM(R1.wage) " +
                " FROM R1, P1 WHERE R1.id = P1.id ;";
        expected = new long[][] {{499500 * scale}};
        validateTableOfLongs(client, sql, expected);


        // hash aggregation because of no index on GROUP BY key
        sql = "SELECT R1.dept, SUM(R1.wage) " +
                " FROM R1, P1 WHERE R1.id = P1.id Group by R1.dept ORDER BY R1.dept;";
        expected = new long[][] {{0, 249500 * scale}, {1, 250000 * scale}};
        validateTableOfLongs(client, sql, expected);
    }

    public void testDistinctWithGroupBy() throws Exception {
        System.out.println("Test Distinct...");
        loadData(true);
        Client client = getClient();
        String sql;
        long[][] expected;

        for (String tb: tbs) {
            sql = "SELECT DISTINCT id, COUNT(dept) FROM " + tb +
                    " GROUP BY id, wage ORDER BY id, 2 ";
            expected = new long[][] {{1,1}, {2,1}, {3,1}, {4,1}, {5,1}, {6,1}, {7,1}};
            validateTableOfLongs(client, sql, expected);

            // test LIMIT/OFFSET
            sql = "SELECT DISTINCT id, COUNT(dept) FROM " + tb +
                    " GROUP BY id, wage ORDER BY 1, 2 LIMIT 2 ";
            expected = new long[][] {{1,1}, {2,1}};
            validateTableOfLongs(client, sql, expected);

            // query with one column distinct
            sql = "SELECT DISTINCT COUNT(*) FROM " + tb +
                    " GROUP BY ABS(dept-2), wage ORDER BY 1;";
            expected = new long[][] {{1}, {2}};
            validateTableOfLongs(client, sql, expected);

            // (1) base query without distinct
            sql = "SELECT wage, COUNT(*) FROM " + tb +
                    " GROUP BY ABS(dept-2), wage ORDER BY 1, 2;";
            expected = new long[][] {{10,1}, {10,1}, {20,1}, {30,1}, {40,2}, {50,1}};
            validateTableOfLongs(client, sql, expected);

            // query with multiple columns distinct
            sql = "SELECT DISTINCT wage, COUNT(*) FROM " + tb +
                    " GROUP BY ABS(dept-2), wage ORDER BY 1, 2;";
            expected = new long[][] {{10,1}, {20,1}, {30,1}, {40,2}, {50,1}};
            validateTableOfLongs(client, sql, expected);

            // test LIMIT/OFFSET
            sql = "SELECT DISTINCT wage, COUNT(*) FROM " + tb +
                    " GROUP BY ABS(dept-2), wage ORDER BY 1, 2 LIMIT 2;";
            expected = new long[][] {{10,1}, {20,1}};
            validateTableOfLongs(client, sql, expected);

            // query with multiple expressions distinct
            sql = "SELECT DISTINCT id, COUNT(dept) + 1 FROM " + tb +
                    " GROUP BY id, wage ORDER BY 1, 2";
            expected = new long[][] {{1,2}, {2,2}, {3,2}, {4,2}, {5,2}, {6,2}, {7,2}};
            validateTableOfLongs(client, sql, expected);

            sql = "SELECT DISTINCT wage, COUNT(*)+1 FROM " + tb +
                    " GROUP BY ABS(dept-2), wage ORDER BY 1, 2;";
            expected = new long[][] {{10,2}, {20,2}, {30,2}, {40,3}, {50,2}};
            validateTableOfLongs(client, sql, expected);

            // test LIMIT/OFFSET
            sql = "SELECT DISTINCT wage, COUNT(*)+1 FROM " + tb +
                    " GROUP BY ABS(dept-2), wage ORDER BY 1, 2 LIMIT 2;";
            expected = new long[][] {{10,2}, {20,2}};
            validateTableOfLongs(client, sql, expected);
        }
    }

    private void orderbyColumnsNotInDisplayList() throws Exception {
        System.out.println("Test testOrderbyColumnsNotInDisplayList...");
        loadData(true);
        Client client = getClient();
        String sql;
        long[][] expected;

        for (String tb: tbs) {
            // Test Order by column not in Display columns
            sql = "SELECT SUM(ABS(wage) - 1) AS tag, " +
                    "(COUNT(*)+SUM(dept*2))/2 FROM " + tb +
                    " GROUP BY dept ORDER BY ABS(dept)";
            expected = new long[][] {{57, 4}, {136, 10}};
            validateTableOfLongs(client, sql, expected);

            sql = "SELECT COUNT(wage), SUM(id) FROM " + tb +
                    " GROUP BY ABS(dept) ORDER BY ABS(dept) ";
            expected = new long[][] {{3, 6}, {4, 22}};
            validateTableOfLongs(client, sql, expected);

            sql = "SELECT COUNT(wage), SUM(id), AVG(wage)  FROM " + tb +
                    " GROUP BY ABS(dept) ORDER BY ABS(dept) ";
            expected = new long[][] {{3, 6, 20}, {4, 22, 35}};
            validateTableOfLongs(client, sql, expected);

            sql =  "SELECT COUNT(wage)+1, AVG(wage)/2 FROM " + tb +
                    " GROUP BY ABS(dept-2) ORDER BY ABS(dept-2);";
            expected = new long[][] {{5, 17}, {4, 10}};
            validateTableOfLongs(client, sql, expected);

            sql = "SELECT COUNT(*) AS tag, SUM(wage) FROM " + tb +
                    " GROUP BY dept ORDER BY ABS(dept) DESC";
            expected = new long[][] {{4, 140}, {3, 60}};
            validateTableOfLongs(client, sql, expected);
        }
    }

    //
    // Suite builder boilerplate
    //
    public TestGroupByComplexSuite(String name) {
        super(name);
    }

    static public junit.framework.Test suite() {
        VoltServerConfig config = null;
        MultiConfigSuiteBuilder builder = new MultiConfigSuiteBuilder(
                TestGroupByComplexSuite.class);

        String addProcs = "";
        for (String tb: tbs) {
            addProcs += "CREATE PROCEDURE " + tb + "_GroupByAlias1 AS " +
                    " SELECT (dept+?) AS tag, COUNT(wage) FROM " + tb +
                    " GROUP BY tag ORDER BY tag;";

            addProcs += "CREATE PROCEDURE " + tb + "_GroupByAlias2 AS " +
                    " SELECT ABS(dept+?) AS tag, COUNT(wage)+1 FROM " + tb +
                    " GROUP BY tag ORDER BY tag;";
        }

        VoltProjectBuilder project = new VoltProjectBuilder();
        final String literalSchema =
                "CREATE TABLE R1 ( " +
                "ID INTEGER DEFAULT 0 NOT NULL, " +
                "WAGE INTEGER, " +
                "DEPT INTEGER, " +
                "TM TIMESTAMP DEFAULT NULL, " +
                "PRIMARY KEY (ID) );" +

                "CREATE TABLE P1 ( " +
                "ID INTEGER DEFAULT 0 NOT NULL, " +
                "WAGE INTEGER NOT NULL, " +
                "DEPT INTEGER NOT NULL, " +
                "TM TIMESTAMP DEFAULT NULL, " +
                "PRIMARY KEY (ID) );" +
                "PARTITION TABLE P1 ON COLUMN ID;" +

                "CREATE TABLE P2 ( " +
                "ID INTEGER DEFAULT 0 NOT NULL ASSUMEUNIQUE, " +
                "WAGE INTEGER NOT NULL, " +
                "DEPT INTEGER NOT NULL, " +
                "TM TIMESTAMP DEFAULT NULL, " +
                "PRIMARY KEY (ID, DEPT) );" +
                "PARTITION TABLE P2 ON COLUMN DEPT;" +

                "CREATE TABLE P3 ( " +
                "ID INTEGER DEFAULT 0 NOT NULL ASSUMEUNIQUE, " +
                "WAGE INTEGER NOT NULL, " +
                "DEPT INTEGER NOT NULL, " +
                "TM TIMESTAMP DEFAULT NULL, " +
                "PRIMARY KEY (ID, WAGE) );" +
                "PARTITION TABLE P3 ON COLUMN WAGE;" +

                "CREATE TABLE TB_STRING ( " +
                "ID INTEGER DEFAULT 0 NOT NULL, " +
                "STATE VARCHAR(2), " +
                "PRIMARY KEY (ID) );" +

                "create table t ( a INTEGER NOT NULL, b INTEGER NOT NULL, c INTEGER NOT NULL); " +

                addProcs
                ;
        try {
            project.addLiteralSchema(literalSchema);
        }
        catch (IOException e) {
            fail();
        }
        boolean success;

        config = new LocalCluster("groupByComplex-onesite.jar", 1, 1, 0, BackendTarget.NATIVE_EE_JNI);
        success = config.compile(project);
        assertTrue(success);
        builder.addServerConfig(config);

        config = new LocalCluster("groupByComplex-hsql.jar", 1, 1, 0, BackendTarget.HSQLDB_BACKEND);
        success = config.compile(project);
        assertTrue(success);
        builder.addServerConfig(config);

        // Cluster
        config = new LocalCluster("groupByComplex-cluster.jar", 2, 3, 1, BackendTarget.NATIVE_EE_JNI);
        success = config.compile(project);
        assertTrue(success);
        builder.addServerConfig(config);

        return builder;
    }
}
