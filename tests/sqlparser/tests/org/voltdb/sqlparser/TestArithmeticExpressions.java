/* This file is part of VoltDB.
 * Copyright (C) 2008-2016 VoltDB Inc.
 *
 * This file contains original code and/or modifications of original code.
 * Any modifications made by VoltDB Inc. are licensed under the following
 * terms and conditions:
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
/*
 * This file has been created by elves.  If you make changes to it,
 * the elves will become annoyed, will overwrite your changes with
 * whatever odd notions they have of what should
 * be here, and ignore your plaintive bleatings.  So, don't edit this file,
 * Unless you want your work to disappear.
 */
package org.voltdb.sqlparser;

import org.junit.Test;

import org.hsqldb_voltpatches.VoltXMLElement;
import org.hsqldb_voltpatches.HSQLInterface;
import org.voltdb.sqlparser.syntax.SQLKind;

import org.voltdb.sqlparser.assertions.semantics.VoltXMLElementAssert.IDTable;
import static org.voltdb.sqlparser.assertions.semantics.VoltXMLElementAssert.*;

public class TestArithmeticExpressions {
    HSQLInterface m_HSQLInterface = null;
    String        m_schema = null;
    public TestArithmeticExpressions() {
        m_HSQLInterface = HSQLInterface.loadHsqldb();
        String m_schema = "create table alpha ( id integer, beta integer, gamma integer );create table gamma ( id integer not null, zooba integer );create table fargle ( id integer not null, dooba integer )";
        try {
            m_HSQLInterface.processDDLStatementsUsingVoltSQLParser(m_schema, null);
        } catch (Exception ex) {
            System.err.printf("Error parsing ddl: %s\n", ex.getMessage());
        }
    }

    // Pattern XML:
    //    //.ELEMENT: select
    //    //.[
    //    //.....ELEMENT: columns
    //    //.....[
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = ID
    //    //....|....|.column = ID
    //    //....|....|.id = 1
    //    //....|....|.index = 0
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = BETA
    //    //....|....|.column = BETA
    //    //....|....|.id = 2
    //    //....|....|.index = 1
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = GAMMA
    //    //....|....|.column = GAMMA
    //    //....|....|.id = 3
    //    //....|....|.index = 2
    //    //....|....|.table = ALPHA
    //    //.....ELEMENT: parameters
    //    //.....ELEMENT: tablescans
    //    //.....[
    //    //....|....ELEMENT: tablescan
    //    //....|....|.jointype = inner
    //    //....|....|.table = ALPHA
    //    //....|....[
    //    //....|....|...ELEMENT: joincond
    //    //....|....|...[
    //    //....|....|....|..ELEMENT: operation
    //    //....|....|....|....id = 8
    //    //....|....|....|....optype = equal
    //    //....|....|....|..[
    //    //....|....|....|....|.ELEMENT: operation
    //    //....|....|....|....|...id = 6
    //    //....|....|....|....|...optype = add
    //    //....|....|....|....|.[
    //    //....|....|....|....|.....ELEMENT: columnref
    //    //....|....|....|....|....|..alias = ID
    //    //....|....|....|....|....|..column = ID
    //    //....|....|....|....|....|..id = 4
    //    //....|....|....|....|....|..index = 0
    //    //....|....|....|....|....|..table = ALPHA
    //    //....|....|....|....|.....ELEMENT: columnref
    //    //....|....|....|....|....|..alias = ID
    //    //....|....|....|....|....|..column = ID
    //    //....|....|....|....|....|..id = 5
    //    //....|....|....|....|....|..index = 0
    //    //....|....|....|....|....|..table = ALPHA
    //    //....|....|....|....|.ELEMENT: value
    //    //....|....|....|....|...id = 7
    //    //....|....|....|....|...value = 0
    //    //....|....|....|....|...valuetype = INTEGER
    //    //
    //    //
    //
    //
    @SuppressWarnings("unchecked")
    @Test
    public void TestSum() throws Exception {
        String sql    = "select * from alpha where id + id = 0";
        IDTable idTable = new IDTable();
        VoltXMLElement element = m_HSQLInterface.getVoltXMLFromSQLUsingVoltSQLParser(sql, null, SQLKind.DQL);
        assertThat(element)
            .hasName(1, "select")
            .hasAllOf(
                withChildNamed(2, "columns",
                    withChildNamed(3, "columnref",
                                   "alias", "ID",
                                   "column", "ID",
                                   "table", "ALPHA",
                        withAttribute(4, "alias", "ID"),
                        withAttribute(5, "column", "ID"),
                        withIdAttribute(6, idTable),
                        withAttribute(7, "index", "0"),
                        withAttribute(8, "table", "ALPHA")),
                    withChildNamed(9, "columnref",
                                   "alias", "BETA",
                                   "column", "BETA",
                                   "table", "ALPHA",
                        withAttribute(10, "alias", "BETA"),
                        withAttribute(11, "column", "BETA"),
                        withIdAttribute(12, idTable),
                        withAttribute(13, "index", "1"),
                        withAttribute(14, "table", "ALPHA")),
                    withChildNamed(15, "columnref",
                                   "alias", "GAMMA",
                                   "column", "GAMMA",
                                   "table", "ALPHA",
                        withAttribute(16, "alias", "GAMMA"),
                        withAttribute(17, "column", "GAMMA"),
                        withIdAttribute(18, idTable),
                        withAttribute(19, "index", "2"),
                        withAttribute(20, "table", "ALPHA"))),
                withChildNamed(21, "parameters"),
                withChildNamed(22, "tablescans",
                    withChildNamed(23, "tablescan",
                        withAttribute(24, "jointype", "inner"),
                        withAttribute(25, "table", "ALPHA"),
                        withChildNamed(26, "joincond",
                            withChildNamed(27, "operation",
                                           "optype", "equal",
                                withIdAttribute(28, idTable),
                                withAttribute(29, "optype", "equal"),
                                withChildNamed(30, "operation",
                                               "optype", "add",
                                    withIdAttribute(31, idTable),
                                    withAttribute(32, "optype", "add"),
                                    withChildNamed(33, "columnref",
                                                   "alias", "ID",
                                                   "column", "ID",
                                                   "table", "ALPHA",
                                        withAttribute(34, "alias", "ID"),
                                        withAttribute(35, "column", "ID"),
                                        withIdAttribute(36, idTable),
                                        withAttribute(37, "index", "0"),
                                        withAttribute(38, "table", "ALPHA")),
                                    withChildNamed(39, "columnref",
                                                   "alias", "ID",
                                                   "column", "ID",
                                                   "table", "ALPHA",
                                        withAttribute(40, "alias", "ID"),
                                        withAttribute(41, "column", "ID"),
                                        withIdAttribute(42, idTable),
                                        withAttribute(43, "index", "0"),
                                        withAttribute(44, "table", "ALPHA"))),
                                withChildNamed(45, "value",
                                    withIdAttribute(46, idTable),
                                    withAttribute(47, "value", "0"),
                                    withAttribute(48, "valuetype", "INTEGER")))))));
    }

    // Pattern XML:
    //    //.ELEMENT: select
    //    //.[
    //    //.....ELEMENT: columns
    //    //.....[
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = ID
    //    //....|....|.column = ID
    //    //....|....|.id = 1
    //    //....|....|.index = 0
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = BETA
    //    //....|....|.column = BETA
    //    //....|....|.id = 2
    //    //....|....|.index = 1
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = GAMMA
    //    //....|....|.column = GAMMA
    //    //....|....|.id = 3
    //    //....|....|.index = 2
    //    //....|....|.table = ALPHA
    //    //.....ELEMENT: parameters
    //    //.....ELEMENT: tablescans
    //    //.....[
    //    //....|....ELEMENT: tablescan
    //    //....|....|.jointype = inner
    //    //....|....|.table = ALPHA
    //    //....|....[
    //    //....|....|...ELEMENT: joincond
    //    //....|....|...[
    //    //....|....|....|..ELEMENT: operation
    //    //....|....|....|....id = 8
    //    //....|....|....|....optype = equal
    //    //....|....|....|..[
    //    //....|....|....|....|.ELEMENT: operation
    //    //....|....|....|....|...id = 6
    //    //....|....|....|....|...optype = subtract
    //    //....|....|....|....|.[
    //    //....|....|....|....|.....ELEMENT: columnref
    //    //....|....|....|....|....|..alias = ID
    //    //....|....|....|....|....|..column = ID
    //    //....|....|....|....|....|..id = 4
    //    //....|....|....|....|....|..index = 0
    //    //....|....|....|....|....|..table = ALPHA
    //    //....|....|....|....|.....ELEMENT: columnref
    //    //....|....|....|....|....|..alias = ID
    //    //....|....|....|....|....|..column = ID
    //    //....|....|....|....|....|..id = 5
    //    //....|....|....|....|....|..index = 0
    //    //....|....|....|....|....|..table = ALPHA
    //    //....|....|....|....|.ELEMENT: value
    //    //....|....|....|....|...id = 7
    //    //....|....|....|....|...value = 0
    //    //....|....|....|....|...valuetype = INTEGER
    //    //
    //    //
    //
    //
    @SuppressWarnings("unchecked")
    @Test
    public void TestDiff() throws Exception {
        String sql    = "select * from alpha where id - id = 0";
        IDTable idTable = new IDTable();
        VoltXMLElement element = m_HSQLInterface.getVoltXMLFromSQLUsingVoltSQLParser(sql, null, SQLKind.DQL);
        assertThat(element)
            .hasName(1, "select")
            .hasAllOf(
                withChildNamed(2, "columns",
                    withChildNamed(3, "columnref",
                                   "alias", "ID",
                                   "column", "ID",
                                   "table", "ALPHA",
                        withAttribute(4, "alias", "ID"),
                        withAttribute(5, "column", "ID"),
                        withIdAttribute(6, idTable),
                        withAttribute(7, "index", "0"),
                        withAttribute(8, "table", "ALPHA")),
                    withChildNamed(9, "columnref",
                                   "alias", "BETA",
                                   "column", "BETA",
                                   "table", "ALPHA",
                        withAttribute(10, "alias", "BETA"),
                        withAttribute(11, "column", "BETA"),
                        withIdAttribute(12, idTable),
                        withAttribute(13, "index", "1"),
                        withAttribute(14, "table", "ALPHA")),
                    withChildNamed(15, "columnref",
                                   "alias", "GAMMA",
                                   "column", "GAMMA",
                                   "table", "ALPHA",
                        withAttribute(16, "alias", "GAMMA"),
                        withAttribute(17, "column", "GAMMA"),
                        withIdAttribute(18, idTable),
                        withAttribute(19, "index", "2"),
                        withAttribute(20, "table", "ALPHA"))),
                withChildNamed(21, "parameters"),
                withChildNamed(22, "tablescans",
                    withChildNamed(23, "tablescan",
                        withAttribute(24, "jointype", "inner"),
                        withAttribute(25, "table", "ALPHA"),
                        withChildNamed(26, "joincond",
                            withChildNamed(27, "operation",
                                           "optype", "equal",
                                withIdAttribute(28, idTable),
                                withAttribute(29, "optype", "equal"),
                                withChildNamed(30, "operation",
                                               "optype", "subtract",
                                    withIdAttribute(31, idTable),
                                    withAttribute(32, "optype", "subtract"),
                                    withChildNamed(33, "columnref",
                                                   "alias", "ID",
                                                   "column", "ID",
                                                   "table", "ALPHA",
                                        withAttribute(34, "alias", "ID"),
                                        withAttribute(35, "column", "ID"),
                                        withIdAttribute(36, idTable),
                                        withAttribute(37, "index", "0"),
                                        withAttribute(38, "table", "ALPHA")),
                                    withChildNamed(39, "columnref",
                                                   "alias", "ID",
                                                   "column", "ID",
                                                   "table", "ALPHA",
                                        withAttribute(40, "alias", "ID"),
                                        withAttribute(41, "column", "ID"),
                                        withIdAttribute(42, idTable),
                                        withAttribute(43, "index", "0"),
                                        withAttribute(44, "table", "ALPHA"))),
                                withChildNamed(45, "value",
                                    withIdAttribute(46, idTable),
                                    withAttribute(47, "value", "0"),
                                    withAttribute(48, "valuetype", "INTEGER")))))));
    }

    // Pattern XML:
    //    //.ELEMENT: select
    //    //.[
    //    //.....ELEMENT: columns
    //    //.....[
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = ID
    //    //....|....|.column = ID
    //    //....|....|.id = 1
    //    //....|....|.index = 0
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = BETA
    //    //....|....|.column = BETA
    //    //....|....|.id = 2
    //    //....|....|.index = 1
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = GAMMA
    //    //....|....|.column = GAMMA
    //    //....|....|.id = 3
    //    //....|....|.index = 2
    //    //....|....|.table = ALPHA
    //    //.....ELEMENT: parameters
    //    //.....ELEMENT: tablescans
    //    //.....[
    //    //....|....ELEMENT: tablescan
    //    //....|....|.jointype = inner
    //    //....|....|.table = ALPHA
    //    //....|....[
    //    //....|....|...ELEMENT: joincond
    //    //....|....|...[
    //    //....|....|....|..ELEMENT: operation
    //    //....|....|....|....id = 8
    //    //....|....|....|....optype = equal
    //    //....|....|....|..[
    //    //....|....|....|....|.ELEMENT: operation
    //    //....|....|....|....|...id = 6
    //    //....|....|....|....|...optype = multiply
    //    //....|....|....|....|.[
    //    //....|....|....|....|.....ELEMENT: columnref
    //    //....|....|....|....|....|..alias = ID
    //    //....|....|....|....|....|..column = ID
    //    //....|....|....|....|....|..id = 4
    //    //....|....|....|....|....|..index = 0
    //    //....|....|....|....|....|..table = ALPHA
    //    //....|....|....|....|.....ELEMENT: columnref
    //    //....|....|....|....|....|..alias = ID
    //    //....|....|....|....|....|..column = ID
    //    //....|....|....|....|....|..id = 5
    //    //....|....|....|....|....|..index = 0
    //    //....|....|....|....|....|..table = ALPHA
    //    //....|....|....|....|.ELEMENT: value
    //    //....|....|....|....|...id = 7
    //    //....|....|....|....|...value = 0
    //    //....|....|....|....|...valuetype = INTEGER
    //    //
    //    //
    //
    //
    @SuppressWarnings("unchecked")
    @Test
    public void TestProd() throws Exception {
        String sql    = "select * from alpha where id * id = 0";
        IDTable idTable = new IDTable();
        VoltXMLElement element = m_HSQLInterface.getVoltXMLFromSQLUsingVoltSQLParser(sql, null, SQLKind.DQL);
        assertThat(element)
            .hasName(1, "select")
            .hasAllOf(
                withChildNamed(2, "columns",
                    withChildNamed(3, "columnref",
                                   "alias", "ID",
                                   "column", "ID",
                                   "table", "ALPHA",
                        withAttribute(4, "alias", "ID"),
                        withAttribute(5, "column", "ID"),
                        withIdAttribute(6, idTable),
                        withAttribute(7, "index", "0"),
                        withAttribute(8, "table", "ALPHA")),
                    withChildNamed(9, "columnref",
                                   "alias", "BETA",
                                   "column", "BETA",
                                   "table", "ALPHA",
                        withAttribute(10, "alias", "BETA"),
                        withAttribute(11, "column", "BETA"),
                        withIdAttribute(12, idTable),
                        withAttribute(13, "index", "1"),
                        withAttribute(14, "table", "ALPHA")),
                    withChildNamed(15, "columnref",
                                   "alias", "GAMMA",
                                   "column", "GAMMA",
                                   "table", "ALPHA",
                        withAttribute(16, "alias", "GAMMA"),
                        withAttribute(17, "column", "GAMMA"),
                        withIdAttribute(18, idTable),
                        withAttribute(19, "index", "2"),
                        withAttribute(20, "table", "ALPHA"))),
                withChildNamed(21, "parameters"),
                withChildNamed(22, "tablescans",
                    withChildNamed(23, "tablescan",
                        withAttribute(24, "jointype", "inner"),
                        withAttribute(25, "table", "ALPHA"),
                        withChildNamed(26, "joincond",
                            withChildNamed(27, "operation",
                                           "optype", "equal",
                                withIdAttribute(28, idTable),
                                withAttribute(29, "optype", "equal"),
                                withChildNamed(30, "operation",
                                               "optype", "multiply",
                                    withIdAttribute(31, idTable),
                                    withAttribute(32, "optype", "multiply"),
                                    withChildNamed(33, "columnref",
                                                   "alias", "ID",
                                                   "column", "ID",
                                                   "table", "ALPHA",
                                        withAttribute(34, "alias", "ID"),
                                        withAttribute(35, "column", "ID"),
                                        withIdAttribute(36, idTable),
                                        withAttribute(37, "index", "0"),
                                        withAttribute(38, "table", "ALPHA")),
                                    withChildNamed(39, "columnref",
                                                   "alias", "ID",
                                                   "column", "ID",
                                                   "table", "ALPHA",
                                        withAttribute(40, "alias", "ID"),
                                        withAttribute(41, "column", "ID"),
                                        withIdAttribute(42, idTable),
                                        withAttribute(43, "index", "0"),
                                        withAttribute(44, "table", "ALPHA"))),
                                withChildNamed(45, "value",
                                    withIdAttribute(46, idTable),
                                    withAttribute(47, "value", "0"),
                                    withAttribute(48, "valuetype", "INTEGER")))))));
    }

    // Pattern XML:
    //    //.ELEMENT: select
    //    //.[
    //    //.....ELEMENT: columns
    //    //.....[
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = ID
    //    //....|....|.column = ID
    //    //....|....|.id = 1
    //    //....|....|.index = 0
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = BETA
    //    //....|....|.column = BETA
    //    //....|....|.id = 2
    //    //....|....|.index = 1
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = GAMMA
    //    //....|....|.column = GAMMA
    //    //....|....|.id = 3
    //    //....|....|.index = 2
    //    //....|....|.table = ALPHA
    //    //.....ELEMENT: parameters
    //    //.....ELEMENT: tablescans
    //    //.....[
    //    //....|....ELEMENT: tablescan
    //    //....|....|.jointype = inner
    //    //....|....|.table = ALPHA
    //    //....|....[
    //    //....|....|...ELEMENT: joincond
    //    //....|....|...[
    //    //....|....|....|..ELEMENT: operation
    //    //....|....|....|....id = 8
    //    //....|....|....|....optype = equal
    //    //....|....|....|..[
    //    //....|....|....|....|.ELEMENT: operation
    //    //....|....|....|....|...id = 6
    //    //....|....|....|....|...optype = divide
    //    //....|....|....|....|.[
    //    //....|....|....|....|.....ELEMENT: columnref
    //    //....|....|....|....|....|..alias = ID
    //    //....|....|....|....|....|..column = ID
    //    //....|....|....|....|....|..id = 4
    //    //....|....|....|....|....|..index = 0
    //    //....|....|....|....|....|..table = ALPHA
    //    //....|....|....|....|.....ELEMENT: columnref
    //    //....|....|....|....|....|..alias = ID
    //    //....|....|....|....|....|..column = ID
    //    //....|....|....|....|....|..id = 5
    //    //....|....|....|....|....|..index = 0
    //    //....|....|....|....|....|..table = ALPHA
    //    //....|....|....|....|.ELEMENT: value
    //    //....|....|....|....|...id = 7
    //    //....|....|....|....|...value = 0
    //    //....|....|....|....|...valuetype = INTEGER
    //    //
    //    //
    //
    //
    @SuppressWarnings("unchecked")
    @Test
    public void TestDiv() throws Exception {
        String sql    = "select * from alpha where id / id = 0";
        IDTable idTable = new IDTable();
        VoltXMLElement element = m_HSQLInterface.getVoltXMLFromSQLUsingVoltSQLParser(sql, null, SQLKind.DQL);
        assertThat(element)
            .hasName(1, "select")
            .hasAllOf(
                withChildNamed(2, "columns",
                    withChildNamed(3, "columnref",
                                   "alias", "ID",
                                   "column", "ID",
                                   "table", "ALPHA",
                        withAttribute(4, "alias", "ID"),
                        withAttribute(5, "column", "ID"),
                        withIdAttribute(6, idTable),
                        withAttribute(7, "index", "0"),
                        withAttribute(8, "table", "ALPHA")),
                    withChildNamed(9, "columnref",
                                   "alias", "BETA",
                                   "column", "BETA",
                                   "table", "ALPHA",
                        withAttribute(10, "alias", "BETA"),
                        withAttribute(11, "column", "BETA"),
                        withIdAttribute(12, idTable),
                        withAttribute(13, "index", "1"),
                        withAttribute(14, "table", "ALPHA")),
                    withChildNamed(15, "columnref",
                                   "alias", "GAMMA",
                                   "column", "GAMMA",
                                   "table", "ALPHA",
                        withAttribute(16, "alias", "GAMMA"),
                        withAttribute(17, "column", "GAMMA"),
                        withIdAttribute(18, idTable),
                        withAttribute(19, "index", "2"),
                        withAttribute(20, "table", "ALPHA"))),
                withChildNamed(21, "parameters"),
                withChildNamed(22, "tablescans",
                    withChildNamed(23, "tablescan",
                        withAttribute(24, "jointype", "inner"),
                        withAttribute(25, "table", "ALPHA"),
                        withChildNamed(26, "joincond",
                            withChildNamed(27, "operation",
                                           "optype", "equal",
                                withIdAttribute(28, idTable),
                                withAttribute(29, "optype", "equal"),
                                withChildNamed(30, "operation",
                                               "optype", "divide",
                                    withIdAttribute(31, idTable),
                                    withAttribute(32, "optype", "divide"),
                                    withChildNamed(33, "columnref",
                                                   "alias", "ID",
                                                   "column", "ID",
                                                   "table", "ALPHA",
                                        withAttribute(34, "alias", "ID"),
                                        withAttribute(35, "column", "ID"),
                                        withIdAttribute(36, idTable),
                                        withAttribute(37, "index", "0"),
                                        withAttribute(38, "table", "ALPHA")),
                                    withChildNamed(39, "columnref",
                                                   "alias", "ID",
                                                   "column", "ID",
                                                   "table", "ALPHA",
                                        withAttribute(40, "alias", "ID"),
                                        withAttribute(41, "column", "ID"),
                                        withIdAttribute(42, idTable),
                                        withAttribute(43, "index", "0"),
                                        withAttribute(44, "table", "ALPHA"))),
                                withChildNamed(45, "value",
                                    withIdAttribute(46, idTable),
                                    withAttribute(47, "value", "0"),
                                    withAttribute(48, "valuetype", "INTEGER")))))));
    }

    // Pattern XML:
    //    //.ELEMENT: select
    //    //.[
    //    //.....ELEMENT: columns
    //    //.....[
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = ID
    //    //....|....|.column = ID
    //    //....|....|.id = 1
    //    //....|....|.index = 0
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = BETA
    //    //....|....|.column = BETA
    //    //....|....|.id = 2
    //    //....|....|.index = 1
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = GAMMA
    //    //....|....|.column = GAMMA
    //    //....|....|.id = 3
    //    //....|....|.index = 2
    //    //....|....|.table = ALPHA
    //    //.....ELEMENT: parameters
    //    //.....ELEMENT: tablescans
    //    //.....[
    //    //....|....ELEMENT: tablescan
    //    //....|....|.jointype = inner
    //    //....|....|.table = ALPHA
    //    //....|....[
    //    //....|....|...ELEMENT: joincond
    //    //....|....|...[
    //    //....|....|....|..ELEMENT: operation
    //    //....|....|....|....id = 10
    //    //....|....|....|....optype = equal
    //    //....|....|....|..[
    //    //....|....|....|....|.ELEMENT: operation
    //    //....|....|....|....|...id = 8
    //    //....|....|....|....|...optype = add
    //    //....|....|....|....|.[
    //    //....|....|....|....|.....ELEMENT: columnref
    //    //....|....|....|....|....|..alias = ID
    //    //....|....|....|....|....|..column = ID
    //    //....|....|....|....|....|..id = 4
    //    //....|....|....|....|....|..index = 0
    //    //....|....|....|....|....|..table = ALPHA
    //    //....|....|....|....|.....ELEMENT: operation
    //    //....|....|....|....|....|..id = 7
    //    //....|....|....|....|....|..optype = multiply
    //    //....|....|....|....|.....[
    //    //....|....|....|....|....|....ELEMENT: columnref
    //    //....|....|....|....|....|....|.alias = BETA
    //    //....|....|....|....|....|....|.column = BETA
    //    //....|....|....|....|....|....|.id = 5
    //    //....|....|....|....|....|....|.index = 1
    //    //....|....|....|....|....|....|.table = ALPHA
    //    //....|....|....|....|....|....ELEMENT: columnref
    //    //....|....|....|....|....|....|.alias = GAMMA
    //    //....|....|....|....|....|....|.column = GAMMA
    //    //....|....|....|....|....|....|.id = 6
    //    //....|....|....|....|....|....|.index = 2
    //    //....|....|....|....|....|....|.table = ALPHA
    //    //....|....|....|....|.ELEMENT: value
    //    //....|....|....|....|...id = 9
    //    //....|....|....|....|...value = 0
    //    //....|....|....|....|...valuetype = INTEGER
    //    //
    //    //
    //
    //
    @SuppressWarnings("unchecked")
    @Test
    public void TestSumProd() throws Exception {
        String sql    = "select * from alpha where id + beta * gamma = 0";
        IDTable idTable = new IDTable();
        VoltXMLElement element = m_HSQLInterface.getVoltXMLFromSQLUsingVoltSQLParser(sql, null, SQLKind.DQL);
        assertThat(element)
            .hasName(1, "select")
            .hasAllOf(
                withChildNamed(2, "columns",
                    withChildNamed(3, "columnref",
                                   "alias", "ID",
                                   "column", "ID",
                                   "table", "ALPHA",
                        withAttribute(4, "alias", "ID"),
                        withAttribute(5, "column", "ID"),
                        withIdAttribute(6, idTable),
                        withAttribute(7, "index", "0"),
                        withAttribute(8, "table", "ALPHA")),
                    withChildNamed(9, "columnref",
                                   "alias", "BETA",
                                   "column", "BETA",
                                   "table", "ALPHA",
                        withAttribute(10, "alias", "BETA"),
                        withAttribute(11, "column", "BETA"),
                        withIdAttribute(12, idTable),
                        withAttribute(13, "index", "1"),
                        withAttribute(14, "table", "ALPHA")),
                    withChildNamed(15, "columnref",
                                   "alias", "GAMMA",
                                   "column", "GAMMA",
                                   "table", "ALPHA",
                        withAttribute(16, "alias", "GAMMA"),
                        withAttribute(17, "column", "GAMMA"),
                        withIdAttribute(18, idTable),
                        withAttribute(19, "index", "2"),
                        withAttribute(20, "table", "ALPHA"))),
                withChildNamed(21, "parameters"),
                withChildNamed(22, "tablescans",
                    withChildNamed(23, "tablescan",
                        withAttribute(24, "jointype", "inner"),
                        withAttribute(25, "table", "ALPHA"),
                        withChildNamed(26, "joincond",
                            withChildNamed(27, "operation",
                                           "optype", "equal",
                                withIdAttribute(28, idTable),
                                withAttribute(29, "optype", "equal"),
                                withChildNamed(30, "operation",
                                               "optype", "add",
                                    withIdAttribute(31, idTable),
                                    withAttribute(32, "optype", "add"),
                                    withChildNamed(33, "columnref",
                                                   "alias", "ID",
                                                   "column", "ID",
                                                   "table", "ALPHA",
                                        withAttribute(34, "alias", "ID"),
                                        withAttribute(35, "column", "ID"),
                                        withIdAttribute(36, idTable),
                                        withAttribute(37, "index", "0"),
                                        withAttribute(38, "table", "ALPHA")),
                                    withChildNamed(39, "operation",
                                                   "optype", "multiply",
                                        withIdAttribute(40, idTable),
                                        withAttribute(41, "optype", "multiply"),
                                        withChildNamed(42, "columnref",
                                                       "alias", "BETA",
                                                       "column", "BETA",
                                                       "table", "ALPHA",
                                            withAttribute(43, "alias", "BETA"),
                                            withAttribute(44, "column", "BETA"),
                                            withIdAttribute(45, idTable),
                                            withAttribute(46, "index", "1"),
                                            withAttribute(47, "table", "ALPHA")),
                                        withChildNamed(48, "columnref",
                                                       "alias", "GAMMA",
                                                       "column", "GAMMA",
                                                       "table", "ALPHA",
                                            withAttribute(49, "alias", "GAMMA"),
                                            withAttribute(50, "column", "GAMMA"),
                                            withIdAttribute(51, idTable),
                                            withAttribute(52, "index", "2"),
                                            withAttribute(53, "table", "ALPHA")))),
                                withChildNamed(54, "value",
                                    withIdAttribute(55, idTable),
                                    withAttribute(56, "value", "0"),
                                    withAttribute(57, "valuetype", "INTEGER")))))));
    }

    // Pattern XML:
    //    //.ELEMENT: select
    //    //.[
    //    //.....ELEMENT: columns
    //    //.....[
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = ID
    //    //....|....|.column = ID
    //    //....|....|.id = 1
    //    //....|....|.index = 0
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = BETA
    //    //....|....|.column = BETA
    //    //....|....|.id = 2
    //    //....|....|.index = 1
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = GAMMA
    //    //....|....|.column = GAMMA
    //    //....|....|.id = 3
    //    //....|....|.index = 2
    //    //....|....|.table = ALPHA
    //    //.....ELEMENT: parameters
    //    //.....ELEMENT: tablescans
    //    //.....[
    //    //....|....ELEMENT: tablescan
    //    //....|....|.jointype = inner
    //    //....|....|.table = ALPHA
    //    //....|....[
    //    //....|....|...ELEMENT: joincond
    //    //....|....|...[
    //    //....|....|....|..ELEMENT: operation
    //    //....|....|....|....id = 10
    //    //....|....|....|....optype = equal
    //    //....|....|....|..[
    //    //....|....|....|....|.ELEMENT: operation
    //    //....|....|....|....|...id = 8
    //    //....|....|....|....|...optype = add
    //    //....|....|....|....|.[
    //    //....|....|....|....|.....ELEMENT: operation
    //    //....|....|....|....|....|..id = 6
    //    //....|....|....|....|....|..optype = multiply
    //    //....|....|....|....|.....[
    //    //....|....|....|....|....|....ELEMENT: columnref
    //    //....|....|....|....|....|....|.alias = ID
    //    //....|....|....|....|....|....|.column = ID
    //    //....|....|....|....|....|....|.id = 4
    //    //....|....|....|....|....|....|.index = 0
    //    //....|....|....|....|....|....|.table = ALPHA
    //    //....|....|....|....|....|....ELEMENT: columnref
    //    //....|....|....|....|....|....|.alias = BETA
    //    //....|....|....|....|....|....|.column = BETA
    //    //....|....|....|....|....|....|.id = 5
    //    //....|....|....|....|....|....|.index = 1
    //    //....|....|....|....|....|....|.table = ALPHA
    //    //....|....|....|....|.....ELEMENT: columnref
    //    //....|....|....|....|....|..alias = GAMMA
    //    //....|....|....|....|....|..column = GAMMA
    //    //....|....|....|....|....|..id = 7
    //    //....|....|....|....|....|..index = 2
    //    //....|....|....|....|....|..table = ALPHA
    //    //....|....|....|....|.ELEMENT: value
    //    //....|....|....|....|...id = 9
    //    //....|....|....|....|...value = 0
    //    //....|....|....|....|...valuetype = INTEGER
    //    //
    //    //
    //
    //
    @SuppressWarnings("unchecked")
    @Test
    public void TestProdSum() throws Exception {
        String sql    = "select * from alpha where id * beta + gamma = 0";
        IDTable idTable = new IDTable();
        VoltXMLElement element = m_HSQLInterface.getVoltXMLFromSQLUsingVoltSQLParser(sql, null, SQLKind.DQL);
        assertThat(element)
            .hasName(1, "select")
            .hasAllOf(
                withChildNamed(2, "columns",
                    withChildNamed(3, "columnref",
                                   "alias", "ID",
                                   "column", "ID",
                                   "table", "ALPHA",
                        withAttribute(4, "alias", "ID"),
                        withAttribute(5, "column", "ID"),
                        withIdAttribute(6, idTable),
                        withAttribute(7, "index", "0"),
                        withAttribute(8, "table", "ALPHA")),
                    withChildNamed(9, "columnref",
                                   "alias", "BETA",
                                   "column", "BETA",
                                   "table", "ALPHA",
                        withAttribute(10, "alias", "BETA"),
                        withAttribute(11, "column", "BETA"),
                        withIdAttribute(12, idTable),
                        withAttribute(13, "index", "1"),
                        withAttribute(14, "table", "ALPHA")),
                    withChildNamed(15, "columnref",
                                   "alias", "GAMMA",
                                   "column", "GAMMA",
                                   "table", "ALPHA",
                        withAttribute(16, "alias", "GAMMA"),
                        withAttribute(17, "column", "GAMMA"),
                        withIdAttribute(18, idTable),
                        withAttribute(19, "index", "2"),
                        withAttribute(20, "table", "ALPHA"))),
                withChildNamed(21, "parameters"),
                withChildNamed(22, "tablescans",
                    withChildNamed(23, "tablescan",
                        withAttribute(24, "jointype", "inner"),
                        withAttribute(25, "table", "ALPHA"),
                        withChildNamed(26, "joincond",
                            withChildNamed(27, "operation",
                                           "optype", "equal",
                                withIdAttribute(28, idTable),
                                withAttribute(29, "optype", "equal"),
                                withChildNamed(30, "operation",
                                               "optype", "add",
                                    withIdAttribute(31, idTable),
                                    withAttribute(32, "optype", "add"),
                                    withChildNamed(33, "operation",
                                                   "optype", "multiply",
                                        withIdAttribute(34, idTable),
                                        withAttribute(35, "optype", "multiply"),
                                        withChildNamed(36, "columnref",
                                                       "alias", "ID",
                                                       "column", "ID",
                                                       "table", "ALPHA",
                                            withAttribute(37, "alias", "ID"),
                                            withAttribute(38, "column", "ID"),
                                            withIdAttribute(39, idTable),
                                            withAttribute(40, "index", "0"),
                                            withAttribute(41, "table", "ALPHA")),
                                        withChildNamed(42, "columnref",
                                                       "alias", "BETA",
                                                       "column", "BETA",
                                                       "table", "ALPHA",
                                            withAttribute(43, "alias", "BETA"),
                                            withAttribute(44, "column", "BETA"),
                                            withIdAttribute(45, idTable),
                                            withAttribute(46, "index", "1"),
                                            withAttribute(47, "table", "ALPHA"))),
                                    withChildNamed(48, "columnref",
                                                   "alias", "GAMMA",
                                                   "column", "GAMMA",
                                                   "table", "ALPHA",
                                        withAttribute(49, "alias", "GAMMA"),
                                        withAttribute(50, "column", "GAMMA"),
                                        withIdAttribute(51, idTable),
                                        withAttribute(52, "index", "2"),
                                        withAttribute(53, "table", "ALPHA"))),
                                withChildNamed(54, "value",
                                    withIdAttribute(55, idTable),
                                    withAttribute(56, "value", "0"),
                                    withAttribute(57, "valuetype", "INTEGER")))))));
    }

    // Pattern XML:
    //    //.ELEMENT: select
    //    //.[
    //    //.....ELEMENT: columns
    //    //.....[
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = ID
    //    //....|....|.column = ID
    //    //....|....|.id = 1
    //    //....|....|.index = 0
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = BETA
    //    //....|....|.column = BETA
    //    //....|....|.id = 2
    //    //....|....|.index = 1
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = GAMMA
    //    //....|....|.column = GAMMA
    //    //....|....|.id = 3
    //    //....|....|.index = 2
    //    //....|....|.table = ALPHA
    //    //.....ELEMENT: parameters
    //    //.....ELEMENT: tablescans
    //    //.....[
    //    //....|....ELEMENT: tablescan
    //    //....|....|.jointype = inner
    //    //....|....|.table = ALPHA
    //    //....|....[
    //    //....|....|...ELEMENT: joincond
    //    //....|....|...[
    //    //....|....|....|..ELEMENT: operation
    //    //....|....|....|....id = 10
    //    //....|....|....|....optype = equal
    //    //....|....|....|..[
    //    //....|....|....|....|.ELEMENT: operation
    //    //....|....|....|....|...id = 8
    //    //....|....|....|....|...optype = add
    //    //....|....|....|....|.[
    //    //....|....|....|....|.....ELEMENT: columnref
    //    //....|....|....|....|....|..alias = ID
    //    //....|....|....|....|....|..column = ID
    //    //....|....|....|....|....|..id = 4
    //    //....|....|....|....|....|..index = 0
    //    //....|....|....|....|....|..table = ALPHA
    //    //....|....|....|....|.....ELEMENT: operation
    //    //....|....|....|....|....|..id = 7
    //    //....|....|....|....|....|..optype = divide
    //    //....|....|....|....|.....[
    //    //....|....|....|....|....|....ELEMENT: columnref
    //    //....|....|....|....|....|....|.alias = BETA
    //    //....|....|....|....|....|....|.column = BETA
    //    //....|....|....|....|....|....|.id = 5
    //    //....|....|....|....|....|....|.index = 1
    //    //....|....|....|....|....|....|.table = ALPHA
    //    //....|....|....|....|....|....ELEMENT: columnref
    //    //....|....|....|....|....|....|.alias = GAMMA
    //    //....|....|....|....|....|....|.column = GAMMA
    //    //....|....|....|....|....|....|.id = 6
    //    //....|....|....|....|....|....|.index = 2
    //    //....|....|....|....|....|....|.table = ALPHA
    //    //....|....|....|....|.ELEMENT: value
    //    //....|....|....|....|...id = 9
    //    //....|....|....|....|...value = 0
    //    //....|....|....|....|...valuetype = INTEGER
    //    //
    //    //
    //
    //
    @SuppressWarnings("unchecked")
    @Test
    public void TestSumDiv() throws Exception {
        String sql    = "select * from alpha where id + beta / gamma = 0";
        IDTable idTable = new IDTable();
        VoltXMLElement element = m_HSQLInterface.getVoltXMLFromSQLUsingVoltSQLParser(sql, null, SQLKind.DQL);
        assertThat(element)
            .hasName(1, "select")
            .hasAllOf(
                withChildNamed(2, "columns",
                    withChildNamed(3, "columnref",
                                   "alias", "ID",
                                   "column", "ID",
                                   "table", "ALPHA",
                        withAttribute(4, "alias", "ID"),
                        withAttribute(5, "column", "ID"),
                        withIdAttribute(6, idTable),
                        withAttribute(7, "index", "0"),
                        withAttribute(8, "table", "ALPHA")),
                    withChildNamed(9, "columnref",
                                   "alias", "BETA",
                                   "column", "BETA",
                                   "table", "ALPHA",
                        withAttribute(10, "alias", "BETA"),
                        withAttribute(11, "column", "BETA"),
                        withIdAttribute(12, idTable),
                        withAttribute(13, "index", "1"),
                        withAttribute(14, "table", "ALPHA")),
                    withChildNamed(15, "columnref",
                                   "alias", "GAMMA",
                                   "column", "GAMMA",
                                   "table", "ALPHA",
                        withAttribute(16, "alias", "GAMMA"),
                        withAttribute(17, "column", "GAMMA"),
                        withIdAttribute(18, idTable),
                        withAttribute(19, "index", "2"),
                        withAttribute(20, "table", "ALPHA"))),
                withChildNamed(21, "parameters"),
                withChildNamed(22, "tablescans",
                    withChildNamed(23, "tablescan",
                        withAttribute(24, "jointype", "inner"),
                        withAttribute(25, "table", "ALPHA"),
                        withChildNamed(26, "joincond",
                            withChildNamed(27, "operation",
                                           "optype", "equal",
                                withIdAttribute(28, idTable),
                                withAttribute(29, "optype", "equal"),
                                withChildNamed(30, "operation",
                                               "optype", "add",
                                    withIdAttribute(31, idTable),
                                    withAttribute(32, "optype", "add"),
                                    withChildNamed(33, "columnref",
                                                   "alias", "ID",
                                                   "column", "ID",
                                                   "table", "ALPHA",
                                        withAttribute(34, "alias", "ID"),
                                        withAttribute(35, "column", "ID"),
                                        withIdAttribute(36, idTable),
                                        withAttribute(37, "index", "0"),
                                        withAttribute(38, "table", "ALPHA")),
                                    withChildNamed(39, "operation",
                                                   "optype", "divide",
                                        withIdAttribute(40, idTable),
                                        withAttribute(41, "optype", "divide"),
                                        withChildNamed(42, "columnref",
                                                       "alias", "BETA",
                                                       "column", "BETA",
                                                       "table", "ALPHA",
                                            withAttribute(43, "alias", "BETA"),
                                            withAttribute(44, "column", "BETA"),
                                            withIdAttribute(45, idTable),
                                            withAttribute(46, "index", "1"),
                                            withAttribute(47, "table", "ALPHA")),
                                        withChildNamed(48, "columnref",
                                                       "alias", "GAMMA",
                                                       "column", "GAMMA",
                                                       "table", "ALPHA",
                                            withAttribute(49, "alias", "GAMMA"),
                                            withAttribute(50, "column", "GAMMA"),
                                            withIdAttribute(51, idTable),
                                            withAttribute(52, "index", "2"),
                                            withAttribute(53, "table", "ALPHA")))),
                                withChildNamed(54, "value",
                                    withIdAttribute(55, idTable),
                                    withAttribute(56, "value", "0"),
                                    withAttribute(57, "valuetype", "INTEGER")))))));
    }

    // Pattern XML:
    //    //.ELEMENT: select
    //    //.[
    //    //.....ELEMENT: columns
    //    //.....[
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = ID
    //    //....|....|.column = ID
    //    //....|....|.id = 1
    //    //....|....|.index = 0
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = BETA
    //    //....|....|.column = BETA
    //    //....|....|.id = 2
    //    //....|....|.index = 1
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = GAMMA
    //    //....|....|.column = GAMMA
    //    //....|....|.id = 3
    //    //....|....|.index = 2
    //    //....|....|.table = ALPHA
    //    //.....ELEMENT: parameters
    //    //.....ELEMENT: tablescans
    //    //.....[
    //    //....|....ELEMENT: tablescan
    //    //....|....|.jointype = inner
    //    //....|....|.table = ALPHA
    //    //....|....[
    //    //....|....|...ELEMENT: joincond
    //    //....|....|...[
    //    //....|....|....|..ELEMENT: operation
    //    //....|....|....|....id = 10
    //    //....|....|....|....optype = equal
    //    //....|....|....|..[
    //    //....|....|....|....|.ELEMENT: operation
    //    //....|....|....|....|...id = 8
    //    //....|....|....|....|...optype = add
    //    //....|....|....|....|.[
    //    //....|....|....|....|.....ELEMENT: operation
    //    //....|....|....|....|....|..id = 6
    //    //....|....|....|....|....|..optype = divide
    //    //....|....|....|....|.....[
    //    //....|....|....|....|....|....ELEMENT: columnref
    //    //....|....|....|....|....|....|.alias = ID
    //    //....|....|....|....|....|....|.column = ID
    //    //....|....|....|....|....|....|.id = 4
    //    //....|....|....|....|....|....|.index = 0
    //    //....|....|....|....|....|....|.table = ALPHA
    //    //....|....|....|....|....|....ELEMENT: columnref
    //    //....|....|....|....|....|....|.alias = BETA
    //    //....|....|....|....|....|....|.column = BETA
    //    //....|....|....|....|....|....|.id = 5
    //    //....|....|....|....|....|....|.index = 1
    //    //....|....|....|....|....|....|.table = ALPHA
    //    //....|....|....|....|.....ELEMENT: columnref
    //    //....|....|....|....|....|..alias = GAMMA
    //    //....|....|....|....|....|..column = GAMMA
    //    //....|....|....|....|....|..id = 7
    //    //....|....|....|....|....|..index = 2
    //    //....|....|....|....|....|..table = ALPHA
    //    //....|....|....|....|.ELEMENT: value
    //    //....|....|....|....|...id = 9
    //    //....|....|....|....|...value = 0
    //    //....|....|....|....|...valuetype = INTEGER
    //    //
    //    //
    //
    //
    @SuppressWarnings("unchecked")
    @Test
    public void TestDivSum() throws Exception {
        String sql    = "select * from alpha where id / beta + gamma = 0";
        IDTable idTable = new IDTable();
        VoltXMLElement element = m_HSQLInterface.getVoltXMLFromSQLUsingVoltSQLParser(sql, null, SQLKind.DQL);
        assertThat(element)
            .hasName(1, "select")
            .hasAllOf(
                withChildNamed(2, "columns",
                    withChildNamed(3, "columnref",
                                   "alias", "ID",
                                   "column", "ID",
                                   "table", "ALPHA",
                        withAttribute(4, "alias", "ID"),
                        withAttribute(5, "column", "ID"),
                        withIdAttribute(6, idTable),
                        withAttribute(7, "index", "0"),
                        withAttribute(8, "table", "ALPHA")),
                    withChildNamed(9, "columnref",
                                   "alias", "BETA",
                                   "column", "BETA",
                                   "table", "ALPHA",
                        withAttribute(10, "alias", "BETA"),
                        withAttribute(11, "column", "BETA"),
                        withIdAttribute(12, idTable),
                        withAttribute(13, "index", "1"),
                        withAttribute(14, "table", "ALPHA")),
                    withChildNamed(15, "columnref",
                                   "alias", "GAMMA",
                                   "column", "GAMMA",
                                   "table", "ALPHA",
                        withAttribute(16, "alias", "GAMMA"),
                        withAttribute(17, "column", "GAMMA"),
                        withIdAttribute(18, idTable),
                        withAttribute(19, "index", "2"),
                        withAttribute(20, "table", "ALPHA"))),
                withChildNamed(21, "parameters"),
                withChildNamed(22, "tablescans",
                    withChildNamed(23, "tablescan",
                        withAttribute(24, "jointype", "inner"),
                        withAttribute(25, "table", "ALPHA"),
                        withChildNamed(26, "joincond",
                            withChildNamed(27, "operation",
                                           "optype", "equal",
                                withIdAttribute(28, idTable),
                                withAttribute(29, "optype", "equal"),
                                withChildNamed(30, "operation",
                                               "optype", "add",
                                    withIdAttribute(31, idTable),
                                    withAttribute(32, "optype", "add"),
                                    withChildNamed(33, "operation",
                                                   "optype", "divide",
                                        withIdAttribute(34, idTable),
                                        withAttribute(35, "optype", "divide"),
                                        withChildNamed(36, "columnref",
                                                       "alias", "ID",
                                                       "column", "ID",
                                                       "table", "ALPHA",
                                            withAttribute(37, "alias", "ID"),
                                            withAttribute(38, "column", "ID"),
                                            withIdAttribute(39, idTable),
                                            withAttribute(40, "index", "0"),
                                            withAttribute(41, "table", "ALPHA")),
                                        withChildNamed(42, "columnref",
                                                       "alias", "BETA",
                                                       "column", "BETA",
                                                       "table", "ALPHA",
                                            withAttribute(43, "alias", "BETA"),
                                            withAttribute(44, "column", "BETA"),
                                            withIdAttribute(45, idTable),
                                            withAttribute(46, "index", "1"),
                                            withAttribute(47, "table", "ALPHA"))),
                                    withChildNamed(48, "columnref",
                                                   "alias", "GAMMA",
                                                   "column", "GAMMA",
                                                   "table", "ALPHA",
                                        withAttribute(49, "alias", "GAMMA"),
                                        withAttribute(50, "column", "GAMMA"),
                                        withIdAttribute(51, idTable),
                                        withAttribute(52, "index", "2"),
                                        withAttribute(53, "table", "ALPHA"))),
                                withChildNamed(54, "value",
                                    withIdAttribute(55, idTable),
                                    withAttribute(56, "value", "0"),
                                    withAttribute(57, "valuetype", "INTEGER")))))));
    }

    // Pattern XML:
    //    //.ELEMENT: select
    //    //.[
    //    //.....ELEMENT: columns
    //    //.....[
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = ID
    //    //....|....|.column = ID
    //    //....|....|.id = 1
    //    //....|....|.index = 0
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = BETA
    //    //....|....|.column = BETA
    //    //....|....|.id = 2
    //    //....|....|.index = 1
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = GAMMA
    //    //....|....|.column = GAMMA
    //    //....|....|.id = 3
    //    //....|....|.index = 2
    //    //....|....|.table = ALPHA
    //    //.....ELEMENT: parameters
    //    //.....ELEMENT: tablescans
    //    //.....[
    //    //....|....ELEMENT: tablescan
    //    //....|....|.jointype = inner
    //    //....|....|.table = ALPHA
    //    //....|....[
    //    //....|....|...ELEMENT: joincond
    //    //....|....|...[
    //    //....|....|....|..ELEMENT: operation
    //    //....|....|....|....id = 10
    //    //....|....|....|....optype = equal
    //    //....|....|....|..[
    //    //....|....|....|....|.ELEMENT: operation
    //    //....|....|....|....|...id = 8
    //    //....|....|....|....|...optype = subtract
    //    //....|....|....|....|.[
    //    //....|....|....|....|.....ELEMENT: columnref
    //    //....|....|....|....|....|..alias = ID
    //    //....|....|....|....|....|..column = ID
    //    //....|....|....|....|....|..id = 4
    //    //....|....|....|....|....|..index = 0
    //    //....|....|....|....|....|..table = ALPHA
    //    //....|....|....|....|.....ELEMENT: operation
    //    //....|....|....|....|....|..id = 7
    //    //....|....|....|....|....|..optype = multiply
    //    //....|....|....|....|.....[
    //    //....|....|....|....|....|....ELEMENT: columnref
    //    //....|....|....|....|....|....|.alias = BETA
    //    //....|....|....|....|....|....|.column = BETA
    //    //....|....|....|....|....|....|.id = 5
    //    //....|....|....|....|....|....|.index = 1
    //    //....|....|....|....|....|....|.table = ALPHA
    //    //....|....|....|....|....|....ELEMENT: columnref
    //    //....|....|....|....|....|....|.alias = GAMMA
    //    //....|....|....|....|....|....|.column = GAMMA
    //    //....|....|....|....|....|....|.id = 6
    //    //....|....|....|....|....|....|.index = 2
    //    //....|....|....|....|....|....|.table = ALPHA
    //    //....|....|....|....|.ELEMENT: value
    //    //....|....|....|....|...id = 9
    //    //....|....|....|....|...value = 0
    //    //....|....|....|....|...valuetype = INTEGER
    //    //
    //    //
    //
    //
    @SuppressWarnings("unchecked")
    @Test
    public void TestDiffProd() throws Exception {
        String sql    = "select * from alpha where id - beta * gamma = 0";
        IDTable idTable = new IDTable();
        VoltXMLElement element = m_HSQLInterface.getVoltXMLFromSQLUsingVoltSQLParser(sql, null, SQLKind.DQL);
        assertThat(element)
            .hasName(1, "select")
            .hasAllOf(
                withChildNamed(2, "columns",
                    withChildNamed(3, "columnref",
                                   "alias", "ID",
                                   "column", "ID",
                                   "table", "ALPHA",
                        withAttribute(4, "alias", "ID"),
                        withAttribute(5, "column", "ID"),
                        withIdAttribute(6, idTable),
                        withAttribute(7, "index", "0"),
                        withAttribute(8, "table", "ALPHA")),
                    withChildNamed(9, "columnref",
                                   "alias", "BETA",
                                   "column", "BETA",
                                   "table", "ALPHA",
                        withAttribute(10, "alias", "BETA"),
                        withAttribute(11, "column", "BETA"),
                        withIdAttribute(12, idTable),
                        withAttribute(13, "index", "1"),
                        withAttribute(14, "table", "ALPHA")),
                    withChildNamed(15, "columnref",
                                   "alias", "GAMMA",
                                   "column", "GAMMA",
                                   "table", "ALPHA",
                        withAttribute(16, "alias", "GAMMA"),
                        withAttribute(17, "column", "GAMMA"),
                        withIdAttribute(18, idTable),
                        withAttribute(19, "index", "2"),
                        withAttribute(20, "table", "ALPHA"))),
                withChildNamed(21, "parameters"),
                withChildNamed(22, "tablescans",
                    withChildNamed(23, "tablescan",
                        withAttribute(24, "jointype", "inner"),
                        withAttribute(25, "table", "ALPHA"),
                        withChildNamed(26, "joincond",
                            withChildNamed(27, "operation",
                                           "optype", "equal",
                                withIdAttribute(28, idTable),
                                withAttribute(29, "optype", "equal"),
                                withChildNamed(30, "operation",
                                               "optype", "subtract",
                                    withIdAttribute(31, idTable),
                                    withAttribute(32, "optype", "subtract"),
                                    withChildNamed(33, "columnref",
                                                   "alias", "ID",
                                                   "column", "ID",
                                                   "table", "ALPHA",
                                        withAttribute(34, "alias", "ID"),
                                        withAttribute(35, "column", "ID"),
                                        withIdAttribute(36, idTable),
                                        withAttribute(37, "index", "0"),
                                        withAttribute(38, "table", "ALPHA")),
                                    withChildNamed(39, "operation",
                                                   "optype", "multiply",
                                        withIdAttribute(40, idTable),
                                        withAttribute(41, "optype", "multiply"),
                                        withChildNamed(42, "columnref",
                                                       "alias", "BETA",
                                                       "column", "BETA",
                                                       "table", "ALPHA",
                                            withAttribute(43, "alias", "BETA"),
                                            withAttribute(44, "column", "BETA"),
                                            withIdAttribute(45, idTable),
                                            withAttribute(46, "index", "1"),
                                            withAttribute(47, "table", "ALPHA")),
                                        withChildNamed(48, "columnref",
                                                       "alias", "GAMMA",
                                                       "column", "GAMMA",
                                                       "table", "ALPHA",
                                            withAttribute(49, "alias", "GAMMA"),
                                            withAttribute(50, "column", "GAMMA"),
                                            withIdAttribute(51, idTable),
                                            withAttribute(52, "index", "2"),
                                            withAttribute(53, "table", "ALPHA")))),
                                withChildNamed(54, "value",
                                    withIdAttribute(55, idTable),
                                    withAttribute(56, "value", "0"),
                                    withAttribute(57, "valuetype", "INTEGER")))))));
    }

    // Pattern XML:
    //    //.ELEMENT: select
    //    //.[
    //    //.....ELEMENT: columns
    //    //.....[
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = ID
    //    //....|....|.column = ID
    //    //....|....|.id = 1
    //    //....|....|.index = 0
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = BETA
    //    //....|....|.column = BETA
    //    //....|....|.id = 2
    //    //....|....|.index = 1
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = GAMMA
    //    //....|....|.column = GAMMA
    //    //....|....|.id = 3
    //    //....|....|.index = 2
    //    //....|....|.table = ALPHA
    //    //.....ELEMENT: parameters
    //    //.....ELEMENT: tablescans
    //    //.....[
    //    //....|....ELEMENT: tablescan
    //    //....|....|.jointype = inner
    //    //....|....|.table = ALPHA
    //    //....|....[
    //    //....|....|...ELEMENT: joincond
    //    //....|....|...[
    //    //....|....|....|..ELEMENT: operation
    //    //....|....|....|....id = 10
    //    //....|....|....|....optype = equal
    //    //....|....|....|..[
    //    //....|....|....|....|.ELEMENT: operation
    //    //....|....|....|....|...id = 8
    //    //....|....|....|....|...optype = subtract
    //    //....|....|....|....|.[
    //    //....|....|....|....|.....ELEMENT: operation
    //    //....|....|....|....|....|..id = 6
    //    //....|....|....|....|....|..optype = multiply
    //    //....|....|....|....|.....[
    //    //....|....|....|....|....|....ELEMENT: columnref
    //    //....|....|....|....|....|....|.alias = ID
    //    //....|....|....|....|....|....|.column = ID
    //    //....|....|....|....|....|....|.id = 4
    //    //....|....|....|....|....|....|.index = 0
    //    //....|....|....|....|....|....|.table = ALPHA
    //    //....|....|....|....|....|....ELEMENT: columnref
    //    //....|....|....|....|....|....|.alias = BETA
    //    //....|....|....|....|....|....|.column = BETA
    //    //....|....|....|....|....|....|.id = 5
    //    //....|....|....|....|....|....|.index = 1
    //    //....|....|....|....|....|....|.table = ALPHA
    //    //....|....|....|....|.....ELEMENT: columnref
    //    //....|....|....|....|....|..alias = GAMMA
    //    //....|....|....|....|....|..column = GAMMA
    //    //....|....|....|....|....|..id = 7
    //    //....|....|....|....|....|..index = 2
    //    //....|....|....|....|....|..table = ALPHA
    //    //....|....|....|....|.ELEMENT: value
    //    //....|....|....|....|...id = 9
    //    //....|....|....|....|...value = 0
    //    //....|....|....|....|...valuetype = INTEGER
    //    //
    //    //
    //
    //
    @SuppressWarnings("unchecked")
    @Test
    public void TestProdDiff() throws Exception {
        String sql    = "select * from alpha where id * beta - gamma = 0";
        IDTable idTable = new IDTable();
        VoltXMLElement element = m_HSQLInterface.getVoltXMLFromSQLUsingVoltSQLParser(sql, null, SQLKind.DQL);
        assertThat(element)
            .hasName(1, "select")
            .hasAllOf(
                withChildNamed(2, "columns",
                    withChildNamed(3, "columnref",
                                   "alias", "ID",
                                   "column", "ID",
                                   "table", "ALPHA",
                        withAttribute(4, "alias", "ID"),
                        withAttribute(5, "column", "ID"),
                        withIdAttribute(6, idTable),
                        withAttribute(7, "index", "0"),
                        withAttribute(8, "table", "ALPHA")),
                    withChildNamed(9, "columnref",
                                   "alias", "BETA",
                                   "column", "BETA",
                                   "table", "ALPHA",
                        withAttribute(10, "alias", "BETA"),
                        withAttribute(11, "column", "BETA"),
                        withIdAttribute(12, idTable),
                        withAttribute(13, "index", "1"),
                        withAttribute(14, "table", "ALPHA")),
                    withChildNamed(15, "columnref",
                                   "alias", "GAMMA",
                                   "column", "GAMMA",
                                   "table", "ALPHA",
                        withAttribute(16, "alias", "GAMMA"),
                        withAttribute(17, "column", "GAMMA"),
                        withIdAttribute(18, idTable),
                        withAttribute(19, "index", "2"),
                        withAttribute(20, "table", "ALPHA"))),
                withChildNamed(21, "parameters"),
                withChildNamed(22, "tablescans",
                    withChildNamed(23, "tablescan",
                        withAttribute(24, "jointype", "inner"),
                        withAttribute(25, "table", "ALPHA"),
                        withChildNamed(26, "joincond",
                            withChildNamed(27, "operation",
                                           "optype", "equal",
                                withIdAttribute(28, idTable),
                                withAttribute(29, "optype", "equal"),
                                withChildNamed(30, "operation",
                                               "optype", "subtract",
                                    withIdAttribute(31, idTable),
                                    withAttribute(32, "optype", "subtract"),
                                    withChildNamed(33, "operation",
                                                   "optype", "multiply",
                                        withIdAttribute(34, idTable),
                                        withAttribute(35, "optype", "multiply"),
                                        withChildNamed(36, "columnref",
                                                       "alias", "ID",
                                                       "column", "ID",
                                                       "table", "ALPHA",
                                            withAttribute(37, "alias", "ID"),
                                            withAttribute(38, "column", "ID"),
                                            withIdAttribute(39, idTable),
                                            withAttribute(40, "index", "0"),
                                            withAttribute(41, "table", "ALPHA")),
                                        withChildNamed(42, "columnref",
                                                       "alias", "BETA",
                                                       "column", "BETA",
                                                       "table", "ALPHA",
                                            withAttribute(43, "alias", "BETA"),
                                            withAttribute(44, "column", "BETA"),
                                            withIdAttribute(45, idTable),
                                            withAttribute(46, "index", "1"),
                                            withAttribute(47, "table", "ALPHA"))),
                                    withChildNamed(48, "columnref",
                                                   "alias", "GAMMA",
                                                   "column", "GAMMA",
                                                   "table", "ALPHA",
                                        withAttribute(49, "alias", "GAMMA"),
                                        withAttribute(50, "column", "GAMMA"),
                                        withIdAttribute(51, idTable),
                                        withAttribute(52, "index", "2"),
                                        withAttribute(53, "table", "ALPHA"))),
                                withChildNamed(54, "value",
                                    withIdAttribute(55, idTable),
                                    withAttribute(56, "value", "0"),
                                    withAttribute(57, "valuetype", "INTEGER")))))));
    }

    // Pattern XML:
    //    //.ELEMENT: select
    //    //.[
    //    //.....ELEMENT: columns
    //    //.....[
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = ID
    //    //....|....|.column = ID
    //    //....|....|.id = 1
    //    //....|....|.index = 0
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = BETA
    //    //....|....|.column = BETA
    //    //....|....|.id = 2
    //    //....|....|.index = 1
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = GAMMA
    //    //....|....|.column = GAMMA
    //    //....|....|.id = 3
    //    //....|....|.index = 2
    //    //....|....|.table = ALPHA
    //    //.....ELEMENT: parameters
    //    //.....ELEMENT: tablescans
    //    //.....[
    //    //....|....ELEMENT: tablescan
    //    //....|....|.jointype = inner
    //    //....|....|.table = ALPHA
    //    //....|....[
    //    //....|....|...ELEMENT: joincond
    //    //....|....|...[
    //    //....|....|....|..ELEMENT: operation
    //    //....|....|....|....id = 10
    //    //....|....|....|....optype = equal
    //    //....|....|....|..[
    //    //....|....|....|....|.ELEMENT: operation
    //    //....|....|....|....|...id = 8
    //    //....|....|....|....|...optype = subtract
    //    //....|....|....|....|.[
    //    //....|....|....|....|.....ELEMENT: columnref
    //    //....|....|....|....|....|..alias = ID
    //    //....|....|....|....|....|..column = ID
    //    //....|....|....|....|....|..id = 4
    //    //....|....|....|....|....|..index = 0
    //    //....|....|....|....|....|..table = ALPHA
    //    //....|....|....|....|.....ELEMENT: operation
    //    //....|....|....|....|....|..id = 7
    //    //....|....|....|....|....|..optype = divide
    //    //....|....|....|....|.....[
    //    //....|....|....|....|....|....ELEMENT: columnref
    //    //....|....|....|....|....|....|.alias = BETA
    //    //....|....|....|....|....|....|.column = BETA
    //    //....|....|....|....|....|....|.id = 5
    //    //....|....|....|....|....|....|.index = 1
    //    //....|....|....|....|....|....|.table = ALPHA
    //    //....|....|....|....|....|....ELEMENT: columnref
    //    //....|....|....|....|....|....|.alias = GAMMA
    //    //....|....|....|....|....|....|.column = GAMMA
    //    //....|....|....|....|....|....|.id = 6
    //    //....|....|....|....|....|....|.index = 2
    //    //....|....|....|....|....|....|.table = ALPHA
    //    //....|....|....|....|.ELEMENT: value
    //    //....|....|....|....|...id = 9
    //    //....|....|....|....|...value = 0
    //    //....|....|....|....|...valuetype = INTEGER
    //    //
    //    //
    //
    //
    @SuppressWarnings("unchecked")
    @Test
    public void TestDiffDiv() throws Exception {
        String sql    = "select * from alpha where id - beta / gamma = 0";
        IDTable idTable = new IDTable();
        VoltXMLElement element = m_HSQLInterface.getVoltXMLFromSQLUsingVoltSQLParser(sql, null, SQLKind.DQL);
        assertThat(element)
            .hasName(1, "select")
            .hasAllOf(
                withChildNamed(2, "columns",
                    withChildNamed(3, "columnref",
                                   "alias", "ID",
                                   "column", "ID",
                                   "table", "ALPHA",
                        withAttribute(4, "alias", "ID"),
                        withAttribute(5, "column", "ID"),
                        withIdAttribute(6, idTable),
                        withAttribute(7, "index", "0"),
                        withAttribute(8, "table", "ALPHA")),
                    withChildNamed(9, "columnref",
                                   "alias", "BETA",
                                   "column", "BETA",
                                   "table", "ALPHA",
                        withAttribute(10, "alias", "BETA"),
                        withAttribute(11, "column", "BETA"),
                        withIdAttribute(12, idTable),
                        withAttribute(13, "index", "1"),
                        withAttribute(14, "table", "ALPHA")),
                    withChildNamed(15, "columnref",
                                   "alias", "GAMMA",
                                   "column", "GAMMA",
                                   "table", "ALPHA",
                        withAttribute(16, "alias", "GAMMA"),
                        withAttribute(17, "column", "GAMMA"),
                        withIdAttribute(18, idTable),
                        withAttribute(19, "index", "2"),
                        withAttribute(20, "table", "ALPHA"))),
                withChildNamed(21, "parameters"),
                withChildNamed(22, "tablescans",
                    withChildNamed(23, "tablescan",
                        withAttribute(24, "jointype", "inner"),
                        withAttribute(25, "table", "ALPHA"),
                        withChildNamed(26, "joincond",
                            withChildNamed(27, "operation",
                                           "optype", "equal",
                                withIdAttribute(28, idTable),
                                withAttribute(29, "optype", "equal"),
                                withChildNamed(30, "operation",
                                               "optype", "subtract",
                                    withIdAttribute(31, idTable),
                                    withAttribute(32, "optype", "subtract"),
                                    withChildNamed(33, "columnref",
                                                   "alias", "ID",
                                                   "column", "ID",
                                                   "table", "ALPHA",
                                        withAttribute(34, "alias", "ID"),
                                        withAttribute(35, "column", "ID"),
                                        withIdAttribute(36, idTable),
                                        withAttribute(37, "index", "0"),
                                        withAttribute(38, "table", "ALPHA")),
                                    withChildNamed(39, "operation",
                                                   "optype", "divide",
                                        withIdAttribute(40, idTable),
                                        withAttribute(41, "optype", "divide"),
                                        withChildNamed(42, "columnref",
                                                       "alias", "BETA",
                                                       "column", "BETA",
                                                       "table", "ALPHA",
                                            withAttribute(43, "alias", "BETA"),
                                            withAttribute(44, "column", "BETA"),
                                            withIdAttribute(45, idTable),
                                            withAttribute(46, "index", "1"),
                                            withAttribute(47, "table", "ALPHA")),
                                        withChildNamed(48, "columnref",
                                                       "alias", "GAMMA",
                                                       "column", "GAMMA",
                                                       "table", "ALPHA",
                                            withAttribute(49, "alias", "GAMMA"),
                                            withAttribute(50, "column", "GAMMA"),
                                            withIdAttribute(51, idTable),
                                            withAttribute(52, "index", "2"),
                                            withAttribute(53, "table", "ALPHA")))),
                                withChildNamed(54, "value",
                                    withIdAttribute(55, idTable),
                                    withAttribute(56, "value", "0"),
                                    withAttribute(57, "valuetype", "INTEGER")))))));
    }

    // Pattern XML:
    //    //.ELEMENT: select
    //    //.[
    //    //.....ELEMENT: columns
    //    //.....[
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = ID
    //    //....|....|.column = ID
    //    //....|....|.id = 1
    //    //....|....|.index = 0
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = BETA
    //    //....|....|.column = BETA
    //    //....|....|.id = 2
    //    //....|....|.index = 1
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = GAMMA
    //    //....|....|.column = GAMMA
    //    //....|....|.id = 3
    //    //....|....|.index = 2
    //    //....|....|.table = ALPHA
    //    //.....ELEMENT: parameters
    //    //.....ELEMENT: tablescans
    //    //.....[
    //    //....|....ELEMENT: tablescan
    //    //....|....|.jointype = inner
    //    //....|....|.table = ALPHA
    //    //....|....[
    //    //....|....|...ELEMENT: joincond
    //    //....|....|...[
    //    //....|....|....|..ELEMENT: operation
    //    //....|....|....|....id = 10
    //    //....|....|....|....optype = equal
    //    //....|....|....|..[
    //    //....|....|....|....|.ELEMENT: operation
    //    //....|....|....|....|...id = 8
    //    //....|....|....|....|...optype = subtract
    //    //....|....|....|....|.[
    //    //....|....|....|....|.....ELEMENT: operation
    //    //....|....|....|....|....|..id = 6
    //    //....|....|....|....|....|..optype = divide
    //    //....|....|....|....|.....[
    //    //....|....|....|....|....|....ELEMENT: columnref
    //    //....|....|....|....|....|....|.alias = ID
    //    //....|....|....|....|....|....|.column = ID
    //    //....|....|....|....|....|....|.id = 4
    //    //....|....|....|....|....|....|.index = 0
    //    //....|....|....|....|....|....|.table = ALPHA
    //    //....|....|....|....|....|....ELEMENT: columnref
    //    //....|....|....|....|....|....|.alias = BETA
    //    //....|....|....|....|....|....|.column = BETA
    //    //....|....|....|....|....|....|.id = 5
    //    //....|....|....|....|....|....|.index = 1
    //    //....|....|....|....|....|....|.table = ALPHA
    //    //....|....|....|....|.....ELEMENT: columnref
    //    //....|....|....|....|....|..alias = GAMMA
    //    //....|....|....|....|....|..column = GAMMA
    //    //....|....|....|....|....|..id = 7
    //    //....|....|....|....|....|..index = 2
    //    //....|....|....|....|....|..table = ALPHA
    //    //....|....|....|....|.ELEMENT: value
    //    //....|....|....|....|...id = 9
    //    //....|....|....|....|...value = 0
    //    //....|....|....|....|...valuetype = INTEGER
    //    //
    //    //
    //
    //
    @SuppressWarnings("unchecked")
    @Test
    public void TestDivDiff() throws Exception {
        String sql    = "select * from alpha where id / beta - gamma = 0";
        IDTable idTable = new IDTable();
        VoltXMLElement element = m_HSQLInterface.getVoltXMLFromSQLUsingVoltSQLParser(sql, null, SQLKind.DQL);
        assertThat(element)
            .hasName(1, "select")
            .hasAllOf(
                withChildNamed(2, "columns",
                    withChildNamed(3, "columnref",
                                   "alias", "ID",
                                   "column", "ID",
                                   "table", "ALPHA",
                        withAttribute(4, "alias", "ID"),
                        withAttribute(5, "column", "ID"),
                        withIdAttribute(6, idTable),
                        withAttribute(7, "index", "0"),
                        withAttribute(8, "table", "ALPHA")),
                    withChildNamed(9, "columnref",
                                   "alias", "BETA",
                                   "column", "BETA",
                                   "table", "ALPHA",
                        withAttribute(10, "alias", "BETA"),
                        withAttribute(11, "column", "BETA"),
                        withIdAttribute(12, idTable),
                        withAttribute(13, "index", "1"),
                        withAttribute(14, "table", "ALPHA")),
                    withChildNamed(15, "columnref",
                                   "alias", "GAMMA",
                                   "column", "GAMMA",
                                   "table", "ALPHA",
                        withAttribute(16, "alias", "GAMMA"),
                        withAttribute(17, "column", "GAMMA"),
                        withIdAttribute(18, idTable),
                        withAttribute(19, "index", "2"),
                        withAttribute(20, "table", "ALPHA"))),
                withChildNamed(21, "parameters"),
                withChildNamed(22, "tablescans",
                    withChildNamed(23, "tablescan",
                        withAttribute(24, "jointype", "inner"),
                        withAttribute(25, "table", "ALPHA"),
                        withChildNamed(26, "joincond",
                            withChildNamed(27, "operation",
                                           "optype", "equal",
                                withIdAttribute(28, idTable),
                                withAttribute(29, "optype", "equal"),
                                withChildNamed(30, "operation",
                                               "optype", "subtract",
                                    withIdAttribute(31, idTable),
                                    withAttribute(32, "optype", "subtract"),
                                    withChildNamed(33, "operation",
                                                   "optype", "divide",
                                        withIdAttribute(34, idTable),
                                        withAttribute(35, "optype", "divide"),
                                        withChildNamed(36, "columnref",
                                                       "alias", "ID",
                                                       "column", "ID",
                                                       "table", "ALPHA",
                                            withAttribute(37, "alias", "ID"),
                                            withAttribute(38, "column", "ID"),
                                            withIdAttribute(39, idTable),
                                            withAttribute(40, "index", "0"),
                                            withAttribute(41, "table", "ALPHA")),
                                        withChildNamed(42, "columnref",
                                                       "alias", "BETA",
                                                       "column", "BETA",
                                                       "table", "ALPHA",
                                            withAttribute(43, "alias", "BETA"),
                                            withAttribute(44, "column", "BETA"),
                                            withIdAttribute(45, idTable),
                                            withAttribute(46, "index", "1"),
                                            withAttribute(47, "table", "ALPHA"))),
                                    withChildNamed(48, "columnref",
                                                   "alias", "GAMMA",
                                                   "column", "GAMMA",
                                                   "table", "ALPHA",
                                        withAttribute(49, "alias", "GAMMA"),
                                        withAttribute(50, "column", "GAMMA"),
                                        withIdAttribute(51, idTable),
                                        withAttribute(52, "index", "2"),
                                        withAttribute(53, "table", "ALPHA"))),
                                withChildNamed(54, "value",
                                    withIdAttribute(55, idTable),
                                    withAttribute(56, "value", "0"),
                                    withAttribute(57, "valuetype", "INTEGER")))))));
    }

    // Pattern XML:
    //    //.ELEMENT: select
    //    //.[
    //    //.....ELEMENT: columns
    //    //.....[
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = ID
    //    //....|....|.column = ID
    //    //....|....|.id = 1
    //    //....|....|.index = 0
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = BETA
    //    //....|....|.column = BETA
    //    //....|....|.id = 2
    //    //....|....|.index = 1
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = GAMMA
    //    //....|....|.column = GAMMA
    //    //....|....|.id = 3
    //    //....|....|.index = 2
    //    //....|....|.table = ALPHA
    //    //.....ELEMENT: parameters
    //    //.....ELEMENT: tablescans
    //    //.....[
    //    //....|....ELEMENT: tablescan
    //    //....|....|.jointype = inner
    //    //....|....|.table = ALPHA
    //    //....|....[
    //    //....|....|...ELEMENT: joincond
    //    //....|....|...[
    //    //....|....|....|..ELEMENT: operation
    //    //....|....|....|....id = 10
    //    //....|....|....|....optype = equal
    //    //....|....|....|..[
    //    //....|....|....|....|.ELEMENT: operation
    //    //....|....|....|....|...id = 8
    //    //....|....|....|....|...optype = subtract
    //    //....|....|....|....|.[
    //    //....|....|....|....|.....ELEMENT: operation
    //    //....|....|....|....|....|..id = 6
    //    //....|....|....|....|....|..optype = add
    //    //....|....|....|....|.....[
    //    //....|....|....|....|....|....ELEMENT: columnref
    //    //....|....|....|....|....|....|.alias = ID
    //    //....|....|....|....|....|....|.column = ID
    //    //....|....|....|....|....|....|.id = 4
    //    //....|....|....|....|....|....|.index = 0
    //    //....|....|....|....|....|....|.table = ALPHA
    //    //....|....|....|....|....|....ELEMENT: columnref
    //    //....|....|....|....|....|....|.alias = BETA
    //    //....|....|....|....|....|....|.column = BETA
    //    //....|....|....|....|....|....|.id = 5
    //    //....|....|....|....|....|....|.index = 1
    //    //....|....|....|....|....|....|.table = ALPHA
    //    //....|....|....|....|.....ELEMENT: columnref
    //    //....|....|....|....|....|..alias = GAMMA
    //    //....|....|....|....|....|..column = GAMMA
    //    //....|....|....|....|....|..id = 7
    //    //....|....|....|....|....|..index = 2
    //    //....|....|....|....|....|..table = ALPHA
    //    //....|....|....|....|.ELEMENT: value
    //    //....|....|....|....|...id = 9
    //    //....|....|....|....|...value = 0
    //    //....|....|....|....|...valuetype = INTEGER
    //    //
    //    //
    //
    //
    @SuppressWarnings("unchecked")
    @Test
    public void TestSumDiff() throws Exception {
        String sql    = "select * from alpha where id + beta - gamma = 0";
        IDTable idTable = new IDTable();
        VoltXMLElement element = m_HSQLInterface.getVoltXMLFromSQLUsingVoltSQLParser(sql, null, SQLKind.DQL);
        assertThat(element)
            .hasName(1, "select")
            .hasAllOf(
                withChildNamed(2, "columns",
                    withChildNamed(3, "columnref",
                                   "alias", "ID",
                                   "column", "ID",
                                   "table", "ALPHA",
                        withAttribute(4, "alias", "ID"),
                        withAttribute(5, "column", "ID"),
                        withIdAttribute(6, idTable),
                        withAttribute(7, "index", "0"),
                        withAttribute(8, "table", "ALPHA")),
                    withChildNamed(9, "columnref",
                                   "alias", "BETA",
                                   "column", "BETA",
                                   "table", "ALPHA",
                        withAttribute(10, "alias", "BETA"),
                        withAttribute(11, "column", "BETA"),
                        withIdAttribute(12, idTable),
                        withAttribute(13, "index", "1"),
                        withAttribute(14, "table", "ALPHA")),
                    withChildNamed(15, "columnref",
                                   "alias", "GAMMA",
                                   "column", "GAMMA",
                                   "table", "ALPHA",
                        withAttribute(16, "alias", "GAMMA"),
                        withAttribute(17, "column", "GAMMA"),
                        withIdAttribute(18, idTable),
                        withAttribute(19, "index", "2"),
                        withAttribute(20, "table", "ALPHA"))),
                withChildNamed(21, "parameters"),
                withChildNamed(22, "tablescans",
                    withChildNamed(23, "tablescan",
                        withAttribute(24, "jointype", "inner"),
                        withAttribute(25, "table", "ALPHA"),
                        withChildNamed(26, "joincond",
                            withChildNamed(27, "operation",
                                           "optype", "equal",
                                withIdAttribute(28, idTable),
                                withAttribute(29, "optype", "equal"),
                                withChildNamed(30, "operation",
                                               "optype", "subtract",
                                    withIdAttribute(31, idTable),
                                    withAttribute(32, "optype", "subtract"),
                                    withChildNamed(33, "operation",
                                                   "optype", "add",
                                        withIdAttribute(34, idTable),
                                        withAttribute(35, "optype", "add"),
                                        withChildNamed(36, "columnref",
                                                       "alias", "ID",
                                                       "column", "ID",
                                                       "table", "ALPHA",
                                            withAttribute(37, "alias", "ID"),
                                            withAttribute(38, "column", "ID"),
                                            withIdAttribute(39, idTable),
                                            withAttribute(40, "index", "0"),
                                            withAttribute(41, "table", "ALPHA")),
                                        withChildNamed(42, "columnref",
                                                       "alias", "BETA",
                                                       "column", "BETA",
                                                       "table", "ALPHA",
                                            withAttribute(43, "alias", "BETA"),
                                            withAttribute(44, "column", "BETA"),
                                            withIdAttribute(45, idTable),
                                            withAttribute(46, "index", "1"),
                                            withAttribute(47, "table", "ALPHA"))),
                                    withChildNamed(48, "columnref",
                                                   "alias", "GAMMA",
                                                   "column", "GAMMA",
                                                   "table", "ALPHA",
                                        withAttribute(49, "alias", "GAMMA"),
                                        withAttribute(50, "column", "GAMMA"),
                                        withIdAttribute(51, idTable),
                                        withAttribute(52, "index", "2"),
                                        withAttribute(53, "table", "ALPHA"))),
                                withChildNamed(54, "value",
                                    withIdAttribute(55, idTable),
                                    withAttribute(56, "value", "0"),
                                    withAttribute(57, "valuetype", "INTEGER")))))));
    }

    // Pattern XML:
    //    //.ELEMENT: select
    //    //.[
    //    //.....ELEMENT: columns
    //    //.....[
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = ID
    //    //....|....|.column = ID
    //    //....|....|.id = 1
    //    //....|....|.index = 0
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = BETA
    //    //....|....|.column = BETA
    //    //....|....|.id = 2
    //    //....|....|.index = 1
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = GAMMA
    //    //....|....|.column = GAMMA
    //    //....|....|.id = 3
    //    //....|....|.index = 2
    //    //....|....|.table = ALPHA
    //    //.....ELEMENT: parameters
    //    //.....ELEMENT: tablescans
    //    //.....[
    //    //....|....ELEMENT: tablescan
    //    //....|....|.jointype = inner
    //    //....|....|.table = ALPHA
    //    //....|....[
    //    //....|....|...ELEMENT: joincond
    //    //....|....|...[
    //    //....|....|....|..ELEMENT: operation
    //    //....|....|....|....id = 10
    //    //....|....|....|....optype = equal
    //    //....|....|....|..[
    //    //....|....|....|....|.ELEMENT: operation
    //    //....|....|....|....|...id = 8
    //    //....|....|....|....|...optype = add
    //    //....|....|....|....|.[
    //    //....|....|....|....|.....ELEMENT: operation
    //    //....|....|....|....|....|..id = 6
    //    //....|....|....|....|....|..optype = subtract
    //    //....|....|....|....|.....[
    //    //....|....|....|....|....|....ELEMENT: columnref
    //    //....|....|....|....|....|....|.alias = ID
    //    //....|....|....|....|....|....|.column = ID
    //    //....|....|....|....|....|....|.id = 4
    //    //....|....|....|....|....|....|.index = 0
    //    //....|....|....|....|....|....|.table = ALPHA
    //    //....|....|....|....|....|....ELEMENT: columnref
    //    //....|....|....|....|....|....|.alias = BETA
    //    //....|....|....|....|....|....|.column = BETA
    //    //....|....|....|....|....|....|.id = 5
    //    //....|....|....|....|....|....|.index = 1
    //    //....|....|....|....|....|....|.table = ALPHA
    //    //....|....|....|....|.....ELEMENT: columnref
    //    //....|....|....|....|....|..alias = GAMMA
    //    //....|....|....|....|....|..column = GAMMA
    //    //....|....|....|....|....|..id = 7
    //    //....|....|....|....|....|..index = 2
    //    //....|....|....|....|....|..table = ALPHA
    //    //....|....|....|....|.ELEMENT: value
    //    //....|....|....|....|...id = 9
    //    //....|....|....|....|...value = 0
    //    //....|....|....|....|...valuetype = INTEGER
    //    //
    //    //
    //
    //
    @SuppressWarnings("unchecked")
    @Test
    public void TestDiffSum() throws Exception {
        String sql    = "select * from alpha where id - beta + gamma = 0";
        IDTable idTable = new IDTable();
        VoltXMLElement element = m_HSQLInterface.getVoltXMLFromSQLUsingVoltSQLParser(sql, null, SQLKind.DQL);
        assertThat(element)
            .hasName(1, "select")
            .hasAllOf(
                withChildNamed(2, "columns",
                    withChildNamed(3, "columnref",
                                   "alias", "ID",
                                   "column", "ID",
                                   "table", "ALPHA",
                        withAttribute(4, "alias", "ID"),
                        withAttribute(5, "column", "ID"),
                        withIdAttribute(6, idTable),
                        withAttribute(7, "index", "0"),
                        withAttribute(8, "table", "ALPHA")),
                    withChildNamed(9, "columnref",
                                   "alias", "BETA",
                                   "column", "BETA",
                                   "table", "ALPHA",
                        withAttribute(10, "alias", "BETA"),
                        withAttribute(11, "column", "BETA"),
                        withIdAttribute(12, idTable),
                        withAttribute(13, "index", "1"),
                        withAttribute(14, "table", "ALPHA")),
                    withChildNamed(15, "columnref",
                                   "alias", "GAMMA",
                                   "column", "GAMMA",
                                   "table", "ALPHA",
                        withAttribute(16, "alias", "GAMMA"),
                        withAttribute(17, "column", "GAMMA"),
                        withIdAttribute(18, idTable),
                        withAttribute(19, "index", "2"),
                        withAttribute(20, "table", "ALPHA"))),
                withChildNamed(21, "parameters"),
                withChildNamed(22, "tablescans",
                    withChildNamed(23, "tablescan",
                        withAttribute(24, "jointype", "inner"),
                        withAttribute(25, "table", "ALPHA"),
                        withChildNamed(26, "joincond",
                            withChildNamed(27, "operation",
                                           "optype", "equal",
                                withIdAttribute(28, idTable),
                                withAttribute(29, "optype", "equal"),
                                withChildNamed(30, "operation",
                                               "optype", "add",
                                    withIdAttribute(31, idTable),
                                    withAttribute(32, "optype", "add"),
                                    withChildNamed(33, "operation",
                                                   "optype", "subtract",
                                        withIdAttribute(34, idTable),
                                        withAttribute(35, "optype", "subtract"),
                                        withChildNamed(36, "columnref",
                                                       "alias", "ID",
                                                       "column", "ID",
                                                       "table", "ALPHA",
                                            withAttribute(37, "alias", "ID"),
                                            withAttribute(38, "column", "ID"),
                                            withIdAttribute(39, idTable),
                                            withAttribute(40, "index", "0"),
                                            withAttribute(41, "table", "ALPHA")),
                                        withChildNamed(42, "columnref",
                                                       "alias", "BETA",
                                                       "column", "BETA",
                                                       "table", "ALPHA",
                                            withAttribute(43, "alias", "BETA"),
                                            withAttribute(44, "column", "BETA"),
                                            withIdAttribute(45, idTable),
                                            withAttribute(46, "index", "1"),
                                            withAttribute(47, "table", "ALPHA"))),
                                    withChildNamed(48, "columnref",
                                                   "alias", "GAMMA",
                                                   "column", "GAMMA",
                                                   "table", "ALPHA",
                                        withAttribute(49, "alias", "GAMMA"),
                                        withAttribute(50, "column", "GAMMA"),
                                        withIdAttribute(51, idTable),
                                        withAttribute(52, "index", "2"),
                                        withAttribute(53, "table", "ALPHA"))),
                                withChildNamed(54, "value",
                                    withIdAttribute(55, idTable),
                                    withAttribute(56, "value", "0"),
                                    withAttribute(57, "valuetype", "INTEGER")))))));
    }

    // Pattern XML:
    //    //.ELEMENT: select
    //    //.[
    //    //.....ELEMENT: columns
    //    //.....[
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = ID
    //    //....|....|.column = ID
    //    //....|....|.id = 1
    //    //....|....|.index = 0
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = BETA
    //    //....|....|.column = BETA
    //    //....|....|.id = 2
    //    //....|....|.index = 1
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = GAMMA
    //    //....|....|.column = GAMMA
    //    //....|....|.id = 3
    //    //....|....|.index = 2
    //    //....|....|.table = ALPHA
    //    //.....ELEMENT: parameters
    //    //.....ELEMENT: tablescans
    //    //.....[
    //    //....|....ELEMENT: tablescan
    //    //....|....|.jointype = inner
    //    //....|....|.table = ALPHA
    //    //....|....[
    //    //....|....|...ELEMENT: joincond
    //    //....|....|...[
    //    //....|....|....|..ELEMENT: operation
    //    //....|....|....|....id = 10
    //    //....|....|....|....optype = equal
    //    //....|....|....|..[
    //    //....|....|....|....|.ELEMENT: operation
    //    //....|....|....|....|...id = 8
    //    //....|....|....|....|...optype = divide
    //    //....|....|....|....|.[
    //    //....|....|....|....|.....ELEMENT: operation
    //    //....|....|....|....|....|..id = 6
    //    //....|....|....|....|....|..optype = multiply
    //    //....|....|....|....|.....[
    //    //....|....|....|....|....|....ELEMENT: columnref
    //    //....|....|....|....|....|....|.alias = ID
    //    //....|....|....|....|....|....|.column = ID
    //    //....|....|....|....|....|....|.id = 4
    //    //....|....|....|....|....|....|.index = 0
    //    //....|....|....|....|....|....|.table = ALPHA
    //    //....|....|....|....|....|....ELEMENT: columnref
    //    //....|....|....|....|....|....|.alias = BETA
    //    //....|....|....|....|....|....|.column = BETA
    //    //....|....|....|....|....|....|.id = 5
    //    //....|....|....|....|....|....|.index = 1
    //    //....|....|....|....|....|....|.table = ALPHA
    //    //....|....|....|....|.....ELEMENT: columnref
    //    //....|....|....|....|....|..alias = GAMMA
    //    //....|....|....|....|....|..column = GAMMA
    //    //....|....|....|....|....|..id = 7
    //    //....|....|....|....|....|..index = 2
    //    //....|....|....|....|....|..table = ALPHA
    //    //....|....|....|....|.ELEMENT: value
    //    //....|....|....|....|...id = 9
    //    //....|....|....|....|...value = 0
    //    //....|....|....|....|...valuetype = INTEGER
    //    //
    //    //
    //
    //
    @SuppressWarnings("unchecked")
    @Test
    public void TestProdDiv() throws Exception {
        String sql    = "select * from alpha where id * beta / gamma = 0";
        IDTable idTable = new IDTable();
        VoltXMLElement element = m_HSQLInterface.getVoltXMLFromSQLUsingVoltSQLParser(sql, null, SQLKind.DQL);
        assertThat(element)
            .hasName(1, "select")
            .hasAllOf(
                withChildNamed(2, "columns",
                    withChildNamed(3, "columnref",
                                   "alias", "ID",
                                   "column", "ID",
                                   "table", "ALPHA",
                        withAttribute(4, "alias", "ID"),
                        withAttribute(5, "column", "ID"),
                        withIdAttribute(6, idTable),
                        withAttribute(7, "index", "0"),
                        withAttribute(8, "table", "ALPHA")),
                    withChildNamed(9, "columnref",
                                   "alias", "BETA",
                                   "column", "BETA",
                                   "table", "ALPHA",
                        withAttribute(10, "alias", "BETA"),
                        withAttribute(11, "column", "BETA"),
                        withIdAttribute(12, idTable),
                        withAttribute(13, "index", "1"),
                        withAttribute(14, "table", "ALPHA")),
                    withChildNamed(15, "columnref",
                                   "alias", "GAMMA",
                                   "column", "GAMMA",
                                   "table", "ALPHA",
                        withAttribute(16, "alias", "GAMMA"),
                        withAttribute(17, "column", "GAMMA"),
                        withIdAttribute(18, idTable),
                        withAttribute(19, "index", "2"),
                        withAttribute(20, "table", "ALPHA"))),
                withChildNamed(21, "parameters"),
                withChildNamed(22, "tablescans",
                    withChildNamed(23, "tablescan",
                        withAttribute(24, "jointype", "inner"),
                        withAttribute(25, "table", "ALPHA"),
                        withChildNamed(26, "joincond",
                            withChildNamed(27, "operation",
                                           "optype", "equal",
                                withIdAttribute(28, idTable),
                                withAttribute(29, "optype", "equal"),
                                withChildNamed(30, "operation",
                                               "optype", "divide",
                                    withIdAttribute(31, idTable),
                                    withAttribute(32, "optype", "divide"),
                                    withChildNamed(33, "operation",
                                                   "optype", "multiply",
                                        withIdAttribute(34, idTable),
                                        withAttribute(35, "optype", "multiply"),
                                        withChildNamed(36, "columnref",
                                                       "alias", "ID",
                                                       "column", "ID",
                                                       "table", "ALPHA",
                                            withAttribute(37, "alias", "ID"),
                                            withAttribute(38, "column", "ID"),
                                            withIdAttribute(39, idTable),
                                            withAttribute(40, "index", "0"),
                                            withAttribute(41, "table", "ALPHA")),
                                        withChildNamed(42, "columnref",
                                                       "alias", "BETA",
                                                       "column", "BETA",
                                                       "table", "ALPHA",
                                            withAttribute(43, "alias", "BETA"),
                                            withAttribute(44, "column", "BETA"),
                                            withIdAttribute(45, idTable),
                                            withAttribute(46, "index", "1"),
                                            withAttribute(47, "table", "ALPHA"))),
                                    withChildNamed(48, "columnref",
                                                   "alias", "GAMMA",
                                                   "column", "GAMMA",
                                                   "table", "ALPHA",
                                        withAttribute(49, "alias", "GAMMA"),
                                        withAttribute(50, "column", "GAMMA"),
                                        withIdAttribute(51, idTable),
                                        withAttribute(52, "index", "2"),
                                        withAttribute(53, "table", "ALPHA"))),
                                withChildNamed(54, "value",
                                    withIdAttribute(55, idTable),
                                    withAttribute(56, "value", "0"),
                                    withAttribute(57, "valuetype", "INTEGER")))))));
    }

    // Pattern XML:
    //    //.ELEMENT: select
    //    //.[
    //    //.....ELEMENT: columns
    //    //.....[
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = ID
    //    //....|....|.column = ID
    //    //....|....|.id = 1
    //    //....|....|.index = 0
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = BETA
    //    //....|....|.column = BETA
    //    //....|....|.id = 2
    //    //....|....|.index = 1
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = GAMMA
    //    //....|....|.column = GAMMA
    //    //....|....|.id = 3
    //    //....|....|.index = 2
    //    //....|....|.table = ALPHA
    //    //.....ELEMENT: parameters
    //    //.....ELEMENT: tablescans
    //    //.....[
    //    //....|....ELEMENT: tablescan
    //    //....|....|.jointype = inner
    //    //....|....|.table = ALPHA
    //    //....|....[
    //    //....|....|...ELEMENT: joincond
    //    //....|....|...[
    //    //....|....|....|..ELEMENT: operation
    //    //....|....|....|....id = 10
    //    //....|....|....|....optype = equal
    //    //....|....|....|..[
    //    //....|....|....|....|.ELEMENT: operation
    //    //....|....|....|....|...id = 8
    //    //....|....|....|....|...optype = multiply
    //    //....|....|....|....|.[
    //    //....|....|....|....|.....ELEMENT: operation
    //    //....|....|....|....|....|..id = 6
    //    //....|....|....|....|....|..optype = divide
    //    //....|....|....|....|.....[
    //    //....|....|....|....|....|....ELEMENT: columnref
    //    //....|....|....|....|....|....|.alias = ID
    //    //....|....|....|....|....|....|.column = ID
    //    //....|....|....|....|....|....|.id = 4
    //    //....|....|....|....|....|....|.index = 0
    //    //....|....|....|....|....|....|.table = ALPHA
    //    //....|....|....|....|....|....ELEMENT: columnref
    //    //....|....|....|....|....|....|.alias = BETA
    //    //....|....|....|....|....|....|.column = BETA
    //    //....|....|....|....|....|....|.id = 5
    //    //....|....|....|....|....|....|.index = 1
    //    //....|....|....|....|....|....|.table = ALPHA
    //    //....|....|....|....|.....ELEMENT: columnref
    //    //....|....|....|....|....|..alias = GAMMA
    //    //....|....|....|....|....|..column = GAMMA
    //    //....|....|....|....|....|..id = 7
    //    //....|....|....|....|....|..index = 2
    //    //....|....|....|....|....|..table = ALPHA
    //    //....|....|....|....|.ELEMENT: value
    //    //....|....|....|....|...id = 9
    //    //....|....|....|....|...value = 0
    //    //....|....|....|....|...valuetype = INTEGER
    //    //
    //    //
    //
    //
    @SuppressWarnings("unchecked")
    @Test
    public void TestDivProd() throws Exception {
        String sql    = "select * from alpha where id / beta * gamma = 0";
        IDTable idTable = new IDTable();
        VoltXMLElement element = m_HSQLInterface.getVoltXMLFromSQLUsingVoltSQLParser(sql, null, SQLKind.DQL);
        assertThat(element)
            .hasName(1, "select")
            .hasAllOf(
                withChildNamed(2, "columns",
                    withChildNamed(3, "columnref",
                                   "alias", "ID",
                                   "column", "ID",
                                   "table", "ALPHA",
                        withAttribute(4, "alias", "ID"),
                        withAttribute(5, "column", "ID"),
                        withIdAttribute(6, idTable),
                        withAttribute(7, "index", "0"),
                        withAttribute(8, "table", "ALPHA")),
                    withChildNamed(9, "columnref",
                                   "alias", "BETA",
                                   "column", "BETA",
                                   "table", "ALPHA",
                        withAttribute(10, "alias", "BETA"),
                        withAttribute(11, "column", "BETA"),
                        withIdAttribute(12, idTable),
                        withAttribute(13, "index", "1"),
                        withAttribute(14, "table", "ALPHA")),
                    withChildNamed(15, "columnref",
                                   "alias", "GAMMA",
                                   "column", "GAMMA",
                                   "table", "ALPHA",
                        withAttribute(16, "alias", "GAMMA"),
                        withAttribute(17, "column", "GAMMA"),
                        withIdAttribute(18, idTable),
                        withAttribute(19, "index", "2"),
                        withAttribute(20, "table", "ALPHA"))),
                withChildNamed(21, "parameters"),
                withChildNamed(22, "tablescans",
                    withChildNamed(23, "tablescan",
                        withAttribute(24, "jointype", "inner"),
                        withAttribute(25, "table", "ALPHA"),
                        withChildNamed(26, "joincond",
                            withChildNamed(27, "operation",
                                           "optype", "equal",
                                withIdAttribute(28, idTable),
                                withAttribute(29, "optype", "equal"),
                                withChildNamed(30, "operation",
                                               "optype", "multiply",
                                    withIdAttribute(31, idTable),
                                    withAttribute(32, "optype", "multiply"),
                                    withChildNamed(33, "operation",
                                                   "optype", "divide",
                                        withIdAttribute(34, idTable),
                                        withAttribute(35, "optype", "divide"),
                                        withChildNamed(36, "columnref",
                                                       "alias", "ID",
                                                       "column", "ID",
                                                       "table", "ALPHA",
                                            withAttribute(37, "alias", "ID"),
                                            withAttribute(38, "column", "ID"),
                                            withIdAttribute(39, idTable),
                                            withAttribute(40, "index", "0"),
                                            withAttribute(41, "table", "ALPHA")),
                                        withChildNamed(42, "columnref",
                                                       "alias", "BETA",
                                                       "column", "BETA",
                                                       "table", "ALPHA",
                                            withAttribute(43, "alias", "BETA"),
                                            withAttribute(44, "column", "BETA"),
                                            withIdAttribute(45, idTable),
                                            withAttribute(46, "index", "1"),
                                            withAttribute(47, "table", "ALPHA"))),
                                    withChildNamed(48, "columnref",
                                                   "alias", "GAMMA",
                                                   "column", "GAMMA",
                                                   "table", "ALPHA",
                                        withAttribute(49, "alias", "GAMMA"),
                                        withAttribute(50, "column", "GAMMA"),
                                        withIdAttribute(51, idTable),
                                        withAttribute(52, "index", "2"),
                                        withAttribute(53, "table", "ALPHA"))),
                                withChildNamed(54, "value",
                                    withIdAttribute(55, idTable),
                                    withAttribute(56, "value", "0"),
                                    withAttribute(57, "valuetype", "INTEGER")))))));
    }

    // Pattern XML:
    //    //.ELEMENT: select
    //    //.[
    //    //.....ELEMENT: columns
    //    //.....[
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = ID
    //    //....|....|.column = ID
    //    //....|....|.id = 1
    //    //....|....|.index = 0
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = BETA
    //    //....|....|.column = BETA
    //    //....|....|.id = 2
    //    //....|....|.index = 1
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = GAMMA
    //    //....|....|.column = GAMMA
    //    //....|....|.id = 3
    //    //....|....|.index = 2
    //    //....|....|.table = ALPHA
    //    //.....ELEMENT: parameters
    //    //.....ELEMENT: tablescans
    //    //.....[
    //    //....|....ELEMENT: tablescan
    //    //....|....|.jointype = inner
    //    //....|....|.table = ALPHA
    //    //....|....[
    //    //....|....|...ELEMENT: joincond
    //    //....|....|...[
    //    //....|....|....|..ELEMENT: operation
    //    //....|....|....|....id = 10
    //    //....|....|....|....optype = equal
    //    //....|....|....|..[
    //    //....|....|....|....|.ELEMENT: operation
    //    //....|....|....|....|...id = 8
    //    //....|....|....|....|...optype = add
    //    //....|....|....|....|.[
    //    //....|....|....|....|.....ELEMENT: operation
    //    //....|....|....|....|....|..id = 6
    //    //....|....|....|....|....|..optype = add
    //    //....|....|....|....|.....[
    //    //....|....|....|....|....|....ELEMENT: columnref
    //    //....|....|....|....|....|....|.alias = ID
    //    //....|....|....|....|....|....|.column = ID
    //    //....|....|....|....|....|....|.id = 4
    //    //....|....|....|....|....|....|.index = 0
    //    //....|....|....|....|....|....|.table = ALPHA
    //    //....|....|....|....|....|....ELEMENT: columnref
    //    //....|....|....|....|....|....|.alias = BETA
    //    //....|....|....|....|....|....|.column = BETA
    //    //....|....|....|....|....|....|.id = 5
    //    //....|....|....|....|....|....|.index = 1
    //    //....|....|....|....|....|....|.table = ALPHA
    //    //....|....|....|....|.....ELEMENT: columnref
    //    //....|....|....|....|....|..alias = GAMMA
    //    //....|....|....|....|....|..column = GAMMA
    //    //....|....|....|....|....|..id = 7
    //    //....|....|....|....|....|..index = 2
    //    //....|....|....|....|....|..table = ALPHA
    //    //....|....|....|....|.ELEMENT: value
    //    //....|....|....|....|...id = 9
    //    //....|....|....|....|...value = 0
    //    //....|....|....|....|...valuetype = INTEGER
    //    //
    //    //
    //
    //
    @SuppressWarnings("unchecked")
    @Test
    public void TestSumSum() throws Exception {
        String sql    = "select * from alpha where id + beta + gamma = 0";
        IDTable idTable = new IDTable();
        VoltXMLElement element = m_HSQLInterface.getVoltXMLFromSQLUsingVoltSQLParser(sql, null, SQLKind.DQL);
        assertThat(element)
            .hasName(1, "select")
            .hasAllOf(
                withChildNamed(2, "columns",
                    withChildNamed(3, "columnref",
                                   "alias", "ID",
                                   "column", "ID",
                                   "table", "ALPHA",
                        withAttribute(4, "alias", "ID"),
                        withAttribute(5, "column", "ID"),
                        withIdAttribute(6, idTable),
                        withAttribute(7, "index", "0"),
                        withAttribute(8, "table", "ALPHA")),
                    withChildNamed(9, "columnref",
                                   "alias", "BETA",
                                   "column", "BETA",
                                   "table", "ALPHA",
                        withAttribute(10, "alias", "BETA"),
                        withAttribute(11, "column", "BETA"),
                        withIdAttribute(12, idTable),
                        withAttribute(13, "index", "1"),
                        withAttribute(14, "table", "ALPHA")),
                    withChildNamed(15, "columnref",
                                   "alias", "GAMMA",
                                   "column", "GAMMA",
                                   "table", "ALPHA",
                        withAttribute(16, "alias", "GAMMA"),
                        withAttribute(17, "column", "GAMMA"),
                        withIdAttribute(18, idTable),
                        withAttribute(19, "index", "2"),
                        withAttribute(20, "table", "ALPHA"))),
                withChildNamed(21, "parameters"),
                withChildNamed(22, "tablescans",
                    withChildNamed(23, "tablescan",
                        withAttribute(24, "jointype", "inner"),
                        withAttribute(25, "table", "ALPHA"),
                        withChildNamed(26, "joincond",
                            withChildNamed(27, "operation",
                                           "optype", "equal",
                                withIdAttribute(28, idTable),
                                withAttribute(29, "optype", "equal"),
                                withChildNamed(30, "operation",
                                               "optype", "add",
                                    withIdAttribute(31, idTable),
                                    withAttribute(32, "optype", "add"),
                                    withChildNamed(33, "operation",
                                                   "optype", "add",
                                        withIdAttribute(34, idTable),
                                        withAttribute(35, "optype", "add"),
                                        withChildNamed(36, "columnref",
                                                       "alias", "ID",
                                                       "column", "ID",
                                                       "table", "ALPHA",
                                            withAttribute(37, "alias", "ID"),
                                            withAttribute(38, "column", "ID"),
                                            withIdAttribute(39, idTable),
                                            withAttribute(40, "index", "0"),
                                            withAttribute(41, "table", "ALPHA")),
                                        withChildNamed(42, "columnref",
                                                       "alias", "BETA",
                                                       "column", "BETA",
                                                       "table", "ALPHA",
                                            withAttribute(43, "alias", "BETA"),
                                            withAttribute(44, "column", "BETA"),
                                            withIdAttribute(45, idTable),
                                            withAttribute(46, "index", "1"),
                                            withAttribute(47, "table", "ALPHA"))),
                                    withChildNamed(48, "columnref",
                                                   "alias", "GAMMA",
                                                   "column", "GAMMA",
                                                   "table", "ALPHA",
                                        withAttribute(49, "alias", "GAMMA"),
                                        withAttribute(50, "column", "GAMMA"),
                                        withIdAttribute(51, idTable),
                                        withAttribute(52, "index", "2"),
                                        withAttribute(53, "table", "ALPHA"))),
                                withChildNamed(54, "value",
                                    withIdAttribute(55, idTable),
                                    withAttribute(56, "value", "0"),
                                    withAttribute(57, "valuetype", "INTEGER")))))));
    }

    // Pattern XML:
    //    //.ELEMENT: select
    //    //.[
    //    //.....ELEMENT: columns
    //    //.....[
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = ID
    //    //....|....|.column = ID
    //    //....|....|.id = 1
    //    //....|....|.index = 0
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = BETA
    //    //....|....|.column = BETA
    //    //....|....|.id = 2
    //    //....|....|.index = 1
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = GAMMA
    //    //....|....|.column = GAMMA
    //    //....|....|.id = 3
    //    //....|....|.index = 2
    //    //....|....|.table = ALPHA
    //    //.....ELEMENT: parameters
    //    //.....ELEMENT: tablescans
    //    //.....[
    //    //....|....ELEMENT: tablescan
    //    //....|....|.jointype = inner
    //    //....|....|.table = ALPHA
    //    //....|....[
    //    //....|....|...ELEMENT: joincond
    //    //....|....|...[
    //    //....|....|....|..ELEMENT: operation
    //    //....|....|....|....id = 10
    //    //....|....|....|....optype = equal
    //    //....|....|....|..[
    //    //....|....|....|....|.ELEMENT: operation
    //    //....|....|....|....|...id = 8
    //    //....|....|....|....|...optype = subtract
    //    //....|....|....|....|.[
    //    //....|....|....|....|.....ELEMENT: operation
    //    //....|....|....|....|....|..id = 6
    //    //....|....|....|....|....|..optype = subtract
    //    //....|....|....|....|.....[
    //    //....|....|....|....|....|....ELEMENT: columnref
    //    //....|....|....|....|....|....|.alias = ID
    //    //....|....|....|....|....|....|.column = ID
    //    //....|....|....|....|....|....|.id = 4
    //    //....|....|....|....|....|....|.index = 0
    //    //....|....|....|....|....|....|.table = ALPHA
    //    //....|....|....|....|....|....ELEMENT: columnref
    //    //....|....|....|....|....|....|.alias = BETA
    //    //....|....|....|....|....|....|.column = BETA
    //    //....|....|....|....|....|....|.id = 5
    //    //....|....|....|....|....|....|.index = 1
    //    //....|....|....|....|....|....|.table = ALPHA
    //    //....|....|....|....|.....ELEMENT: columnref
    //    //....|....|....|....|....|..alias = GAMMA
    //    //....|....|....|....|....|..column = GAMMA
    //    //....|....|....|....|....|..id = 7
    //    //....|....|....|....|....|..index = 2
    //    //....|....|....|....|....|..table = ALPHA
    //    //....|....|....|....|.ELEMENT: value
    //    //....|....|....|....|...id = 9
    //    //....|....|....|....|...value = 0
    //    //....|....|....|....|...valuetype = INTEGER
    //    //
    //    //
    //
    //
    @SuppressWarnings("unchecked")
    @Test
    public void TestDiffDiff() throws Exception {
        String sql    = "select * from alpha where id - beta - gamma = 0";
        IDTable idTable = new IDTable();
        VoltXMLElement element = m_HSQLInterface.getVoltXMLFromSQLUsingVoltSQLParser(sql, null, SQLKind.DQL);
        assertThat(element)
            .hasName(1, "select")
            .hasAllOf(
                withChildNamed(2, "columns",
                    withChildNamed(3, "columnref",
                                   "alias", "ID",
                                   "column", "ID",
                                   "table", "ALPHA",
                        withAttribute(4, "alias", "ID"),
                        withAttribute(5, "column", "ID"),
                        withIdAttribute(6, idTable),
                        withAttribute(7, "index", "0"),
                        withAttribute(8, "table", "ALPHA")),
                    withChildNamed(9, "columnref",
                                   "alias", "BETA",
                                   "column", "BETA",
                                   "table", "ALPHA",
                        withAttribute(10, "alias", "BETA"),
                        withAttribute(11, "column", "BETA"),
                        withIdAttribute(12, idTable),
                        withAttribute(13, "index", "1"),
                        withAttribute(14, "table", "ALPHA")),
                    withChildNamed(15, "columnref",
                                   "alias", "GAMMA",
                                   "column", "GAMMA",
                                   "table", "ALPHA",
                        withAttribute(16, "alias", "GAMMA"),
                        withAttribute(17, "column", "GAMMA"),
                        withIdAttribute(18, idTable),
                        withAttribute(19, "index", "2"),
                        withAttribute(20, "table", "ALPHA"))),
                withChildNamed(21, "parameters"),
                withChildNamed(22, "tablescans",
                    withChildNamed(23, "tablescan",
                        withAttribute(24, "jointype", "inner"),
                        withAttribute(25, "table", "ALPHA"),
                        withChildNamed(26, "joincond",
                            withChildNamed(27, "operation",
                                           "optype", "equal",
                                withIdAttribute(28, idTable),
                                withAttribute(29, "optype", "equal"),
                                withChildNamed(30, "operation",
                                               "optype", "subtract",
                                    withIdAttribute(31, idTable),
                                    withAttribute(32, "optype", "subtract"),
                                    withChildNamed(33, "operation",
                                                   "optype", "subtract",
                                        withIdAttribute(34, idTable),
                                        withAttribute(35, "optype", "subtract"),
                                        withChildNamed(36, "columnref",
                                                       "alias", "ID",
                                                       "column", "ID",
                                                       "table", "ALPHA",
                                            withAttribute(37, "alias", "ID"),
                                            withAttribute(38, "column", "ID"),
                                            withIdAttribute(39, idTable),
                                            withAttribute(40, "index", "0"),
                                            withAttribute(41, "table", "ALPHA")),
                                        withChildNamed(42, "columnref",
                                                       "alias", "BETA",
                                                       "column", "BETA",
                                                       "table", "ALPHA",
                                            withAttribute(43, "alias", "BETA"),
                                            withAttribute(44, "column", "BETA"),
                                            withIdAttribute(45, idTable),
                                            withAttribute(46, "index", "1"),
                                            withAttribute(47, "table", "ALPHA"))),
                                    withChildNamed(48, "columnref",
                                                   "alias", "GAMMA",
                                                   "column", "GAMMA",
                                                   "table", "ALPHA",
                                        withAttribute(49, "alias", "GAMMA"),
                                        withAttribute(50, "column", "GAMMA"),
                                        withIdAttribute(51, idTable),
                                        withAttribute(52, "index", "2"),
                                        withAttribute(53, "table", "ALPHA"))),
                                withChildNamed(54, "value",
                                    withIdAttribute(55, idTable),
                                    withAttribute(56, "value", "0"),
                                    withAttribute(57, "valuetype", "INTEGER")))))));
    }

    // Pattern XML:
    //    //.ELEMENT: select
    //    //.[
    //    //.....ELEMENT: columns
    //    //.....[
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = ID
    //    //....|....|.column = ID
    //    //....|....|.id = 1
    //    //....|....|.index = 0
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = BETA
    //    //....|....|.column = BETA
    //    //....|....|.id = 2
    //    //....|....|.index = 1
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = GAMMA
    //    //....|....|.column = GAMMA
    //    //....|....|.id = 3
    //    //....|....|.index = 2
    //    //....|....|.table = ALPHA
    //    //.....ELEMENT: parameters
    //    //.....ELEMENT: tablescans
    //    //.....[
    //    //....|....ELEMENT: tablescan
    //    //....|....|.jointype = inner
    //    //....|....|.table = ALPHA
    //    //....|....[
    //    //....|....|...ELEMENT: joincond
    //    //....|....|...[
    //    //....|....|....|..ELEMENT: operation
    //    //....|....|....|....id = 10
    //    //....|....|....|....optype = equal
    //    //....|....|....|..[
    //    //....|....|....|....|.ELEMENT: operation
    //    //....|....|....|....|...id = 8
    //    //....|....|....|....|...optype = multiply
    //    //....|....|....|....|.[
    //    //....|....|....|....|.....ELEMENT: operation
    //    //....|....|....|....|....|..id = 6
    //    //....|....|....|....|....|..optype = multiply
    //    //....|....|....|....|.....[
    //    //....|....|....|....|....|....ELEMENT: columnref
    //    //....|....|....|....|....|....|.alias = ID
    //    //....|....|....|....|....|....|.column = ID
    //    //....|....|....|....|....|....|.id = 4
    //    //....|....|....|....|....|....|.index = 0
    //    //....|....|....|....|....|....|.table = ALPHA
    //    //....|....|....|....|....|....ELEMENT: columnref
    //    //....|....|....|....|....|....|.alias = BETA
    //    //....|....|....|....|....|....|.column = BETA
    //    //....|....|....|....|....|....|.id = 5
    //    //....|....|....|....|....|....|.index = 1
    //    //....|....|....|....|....|....|.table = ALPHA
    //    //....|....|....|....|.....ELEMENT: columnref
    //    //....|....|....|....|....|..alias = GAMMA
    //    //....|....|....|....|....|..column = GAMMA
    //    //....|....|....|....|....|..id = 7
    //    //....|....|....|....|....|..index = 2
    //    //....|....|....|....|....|..table = ALPHA
    //    //....|....|....|....|.ELEMENT: value
    //    //....|....|....|....|...id = 9
    //    //....|....|....|....|...value = 0
    //    //....|....|....|....|...valuetype = INTEGER
    //    //
    //    //
    //
    //
    @SuppressWarnings("unchecked")
    @Test
    public void TestProdProd() throws Exception {
        String sql    = "select * from alpha where id * beta * gamma = 0";
        IDTable idTable = new IDTable();
        VoltXMLElement element = m_HSQLInterface.getVoltXMLFromSQLUsingVoltSQLParser(sql, null, SQLKind.DQL);
        assertThat(element)
            .hasName(1, "select")
            .hasAllOf(
                withChildNamed(2, "columns",
                    withChildNamed(3, "columnref",
                                   "alias", "ID",
                                   "column", "ID",
                                   "table", "ALPHA",
                        withAttribute(4, "alias", "ID"),
                        withAttribute(5, "column", "ID"),
                        withIdAttribute(6, idTable),
                        withAttribute(7, "index", "0"),
                        withAttribute(8, "table", "ALPHA")),
                    withChildNamed(9, "columnref",
                                   "alias", "BETA",
                                   "column", "BETA",
                                   "table", "ALPHA",
                        withAttribute(10, "alias", "BETA"),
                        withAttribute(11, "column", "BETA"),
                        withIdAttribute(12, idTable),
                        withAttribute(13, "index", "1"),
                        withAttribute(14, "table", "ALPHA")),
                    withChildNamed(15, "columnref",
                                   "alias", "GAMMA",
                                   "column", "GAMMA",
                                   "table", "ALPHA",
                        withAttribute(16, "alias", "GAMMA"),
                        withAttribute(17, "column", "GAMMA"),
                        withIdAttribute(18, idTable),
                        withAttribute(19, "index", "2"),
                        withAttribute(20, "table", "ALPHA"))),
                withChildNamed(21, "parameters"),
                withChildNamed(22, "tablescans",
                    withChildNamed(23, "tablescan",
                        withAttribute(24, "jointype", "inner"),
                        withAttribute(25, "table", "ALPHA"),
                        withChildNamed(26, "joincond",
                            withChildNamed(27, "operation",
                                           "optype", "equal",
                                withIdAttribute(28, idTable),
                                withAttribute(29, "optype", "equal"),
                                withChildNamed(30, "operation",
                                               "optype", "multiply",
                                    withIdAttribute(31, idTable),
                                    withAttribute(32, "optype", "multiply"),
                                    withChildNamed(33, "operation",
                                                   "optype", "multiply",
                                        withIdAttribute(34, idTable),
                                        withAttribute(35, "optype", "multiply"),
                                        withChildNamed(36, "columnref",
                                                       "alias", "ID",
                                                       "column", "ID",
                                                       "table", "ALPHA",
                                            withAttribute(37, "alias", "ID"),
                                            withAttribute(38, "column", "ID"),
                                            withIdAttribute(39, idTable),
                                            withAttribute(40, "index", "0"),
                                            withAttribute(41, "table", "ALPHA")),
                                        withChildNamed(42, "columnref",
                                                       "alias", "BETA",
                                                       "column", "BETA",
                                                       "table", "ALPHA",
                                            withAttribute(43, "alias", "BETA"),
                                            withAttribute(44, "column", "BETA"),
                                            withIdAttribute(45, idTable),
                                            withAttribute(46, "index", "1"),
                                            withAttribute(47, "table", "ALPHA"))),
                                    withChildNamed(48, "columnref",
                                                   "alias", "GAMMA",
                                                   "column", "GAMMA",
                                                   "table", "ALPHA",
                                        withAttribute(49, "alias", "GAMMA"),
                                        withAttribute(50, "column", "GAMMA"),
                                        withIdAttribute(51, idTable),
                                        withAttribute(52, "index", "2"),
                                        withAttribute(53, "table", "ALPHA"))),
                                withChildNamed(54, "value",
                                    withIdAttribute(55, idTable),
                                    withAttribute(56, "value", "0"),
                                    withAttribute(57, "valuetype", "INTEGER")))))));
    }

    // Pattern XML:
    //    //.ELEMENT: select
    //    //.[
    //    //.....ELEMENT: columns
    //    //.....[
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = ID
    //    //....|....|.column = ID
    //    //....|....|.id = 1
    //    //....|....|.index = 0
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = BETA
    //    //....|....|.column = BETA
    //    //....|....|.id = 2
    //    //....|....|.index = 1
    //    //....|....|.table = ALPHA
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = GAMMA
    //    //....|....|.column = GAMMA
    //    //....|....|.id = 3
    //    //....|....|.index = 2
    //    //....|....|.table = ALPHA
    //    //.....ELEMENT: parameters
    //    //.....ELEMENT: tablescans
    //    //.....[
    //    //....|....ELEMENT: tablescan
    //    //....|....|.jointype = inner
    //    //....|....|.table = ALPHA
    //    //....|....[
    //    //....|....|...ELEMENT: joincond
    //    //....|....|...[
    //    //....|....|....|..ELEMENT: operation
    //    //....|....|....|....id = 10
    //    //....|....|....|....optype = equal
    //    //....|....|....|..[
    //    //....|....|....|....|.ELEMENT: operation
    //    //....|....|....|....|...id = 8
    //    //....|....|....|....|...optype = divide
    //    //....|....|....|....|.[
    //    //....|....|....|....|.....ELEMENT: operation
    //    //....|....|....|....|....|..id = 6
    //    //....|....|....|....|....|..optype = divide
    //    //....|....|....|....|.....[
    //    //....|....|....|....|....|....ELEMENT: columnref
    //    //....|....|....|....|....|....|.alias = ID
    //    //....|....|....|....|....|....|.column = ID
    //    //....|....|....|....|....|....|.id = 4
    //    //....|....|....|....|....|....|.index = 0
    //    //....|....|....|....|....|....|.table = ALPHA
    //    //....|....|....|....|....|....ELEMENT: columnref
    //    //....|....|....|....|....|....|.alias = BETA
    //    //....|....|....|....|....|....|.column = BETA
    //    //....|....|....|....|....|....|.id = 5
    //    //....|....|....|....|....|....|.index = 1
    //    //....|....|....|....|....|....|.table = ALPHA
    //    //....|....|....|....|.....ELEMENT: columnref
    //    //....|....|....|....|....|..alias = GAMMA
    //    //....|....|....|....|....|..column = GAMMA
    //    //....|....|....|....|....|..id = 7
    //    //....|....|....|....|....|..index = 2
    //    //....|....|....|....|....|..table = ALPHA
    //    //....|....|....|....|.ELEMENT: value
    //    //....|....|....|....|...id = 9
    //    //....|....|....|....|...value = 0
    //    //....|....|....|....|...valuetype = INTEGER
    //    //
    //    //
    //
    //
    @SuppressWarnings("unchecked")
    @Test
    public void TestDivDiv() throws Exception {
        String sql    = "select * from alpha where id / beta / gamma = 0";
        IDTable idTable = new IDTable();
        VoltXMLElement element = m_HSQLInterface.getVoltXMLFromSQLUsingVoltSQLParser(sql, null, SQLKind.DQL);
        assertThat(element)
            .hasName(1, "select")
            .hasAllOf(
                withChildNamed(2, "columns",
                    withChildNamed(3, "columnref",
                                   "alias", "ID",
                                   "column", "ID",
                                   "table", "ALPHA",
                        withAttribute(4, "alias", "ID"),
                        withAttribute(5, "column", "ID"),
                        withIdAttribute(6, idTable),
                        withAttribute(7, "index", "0"),
                        withAttribute(8, "table", "ALPHA")),
                    withChildNamed(9, "columnref",
                                   "alias", "BETA",
                                   "column", "BETA",
                                   "table", "ALPHA",
                        withAttribute(10, "alias", "BETA"),
                        withAttribute(11, "column", "BETA"),
                        withIdAttribute(12, idTable),
                        withAttribute(13, "index", "1"),
                        withAttribute(14, "table", "ALPHA")),
                    withChildNamed(15, "columnref",
                                   "alias", "GAMMA",
                                   "column", "GAMMA",
                                   "table", "ALPHA",
                        withAttribute(16, "alias", "GAMMA"),
                        withAttribute(17, "column", "GAMMA"),
                        withIdAttribute(18, idTable),
                        withAttribute(19, "index", "2"),
                        withAttribute(20, "table", "ALPHA"))),
                withChildNamed(21, "parameters"),
                withChildNamed(22, "tablescans",
                    withChildNamed(23, "tablescan",
                        withAttribute(24, "jointype", "inner"),
                        withAttribute(25, "table", "ALPHA"),
                        withChildNamed(26, "joincond",
                            withChildNamed(27, "operation",
                                           "optype", "equal",
                                withIdAttribute(28, idTable),
                                withAttribute(29, "optype", "equal"),
                                withChildNamed(30, "operation",
                                               "optype", "divide",
                                    withIdAttribute(31, idTable),
                                    withAttribute(32, "optype", "divide"),
                                    withChildNamed(33, "operation",
                                                   "optype", "divide",
                                        withIdAttribute(34, idTable),
                                        withAttribute(35, "optype", "divide"),
                                        withChildNamed(36, "columnref",
                                                       "alias", "ID",
                                                       "column", "ID",
                                                       "table", "ALPHA",
                                            withAttribute(37, "alias", "ID"),
                                            withAttribute(38, "column", "ID"),
                                            withIdAttribute(39, idTable),
                                            withAttribute(40, "index", "0"),
                                            withAttribute(41, "table", "ALPHA")),
                                        withChildNamed(42, "columnref",
                                                       "alias", "BETA",
                                                       "column", "BETA",
                                                       "table", "ALPHA",
                                            withAttribute(43, "alias", "BETA"),
                                            withAttribute(44, "column", "BETA"),
                                            withIdAttribute(45, idTable),
                                            withAttribute(46, "index", "1"),
                                            withAttribute(47, "table", "ALPHA"))),
                                    withChildNamed(48, "columnref",
                                                   "alias", "GAMMA",
                                                   "column", "GAMMA",
                                                   "table", "ALPHA",
                                        withAttribute(49, "alias", "GAMMA"),
                                        withAttribute(50, "column", "GAMMA"),
                                        withIdAttribute(51, idTable),
                                        withAttribute(52, "index", "2"),
                                        withAttribute(53, "table", "ALPHA"))),
                                withChildNamed(54, "value",
                                    withIdAttribute(55, idTable),
                                    withAttribute(56, "value", "0"),
                                    withAttribute(57, "valuetype", "INTEGER")))))));
    }
}
