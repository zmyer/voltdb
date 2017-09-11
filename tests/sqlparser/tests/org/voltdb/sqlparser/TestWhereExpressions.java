/* This file is part of VoltDB.
 * Copyright (C) 2008-2017 VoltDB Inc.
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
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *//* This file is part of VoltDB.
 * Copyright (C) 2008-2017 VoltDB Inc.
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
import org.voltdb.planner.ParameterizationInfo;

public class TestWhereExpressions {
    HSQLInterface m_HSQLInterface = null;
    String        m_schema = null;
    public TestWhereExpressions() {
        m_HSQLInterface = HSQLInterface.loadHsqldb(ParameterizationInfo.getParamStateManager());
        String m_schema = "create table alpha ( id integer, beta integer );create table gamma ( id integer not null, zooba integer );create table fargle ( id integer not null, dooba integer )";
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
    //    //....|....|....|....id = 4
    //    //....|....|....|....optype = equal
    //    //....|....|....|..[
    //    //....|....|....|....|.ELEMENT: columnref
    //    //....|....|....|....|...alias = ID
    //    //....|....|....|....|...column = ID
    //    //....|....|....|....|...id = 1
    //    //....|....|....|....|...index = 0
    //    //....|....|....|....|...table = ALPHA
    //    //....|....|....|....|.ELEMENT: value
    //    //....|....|....|....|...id = 3
    //    //....|....|....|....|...value = 0
    //    //....|....|....|....|...valuetype = INTEGER
    //    //
    //    //
    //
    //
    @SuppressWarnings("unchecked")
    @Test
    public void TestColumnRef1() throws Exception {
        String sql    = "select * from alpha where id = 0";
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
                        withAttribute(14, "table", "ALPHA"))),
                withChildNamed(15, "parameters"),
                withChildNamed(16, "tablescans",
                    withChildNamed(17, "tablescan",
                        withAttribute(18, "jointype", "inner"),
                        withAttribute(19, "table", "ALPHA"),
                        withChildNamed(20, "joincond",
                            withChildNamed(21, "operation",
                                           "optype", "equal",
                                withIdAttribute(22, idTable),
                                withAttribute(23, "optype", "equal"),
                                withChildNamed(24, "columnref",
                                               "alias", "ID",
                                               "column", "ID",
                                               "table", "ALPHA",
                                    withAttribute(25, "alias", "ID"),
                                    withAttribute(26, "column", "ID"),
                                    withIdAttribute(27, idTable),
                                    withAttribute(28, "index", "0"),
                                    withAttribute(29, "table", "ALPHA")),
                                withChildNamed(30, "value",
                                    withIdAttribute(31, idTable),
                                    withAttribute(32, "value", "0"),
                                    withAttribute(33, "valuetype", "INTEGER")))))));
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
    //    //....|....|.tablealias = ALEF
    //    //....|....ELEMENT: columnref
    //    //....|....|.alias = BETA
    //    //....|....|.column = BETA
    //    //....|....|.id = 2
    //    //....|....|.index = 1
    //    //....|....|.table = ALPHA
    //    //....|....|.tablealias = ALEF
    //    //.....ELEMENT: parameters
    //    //.....ELEMENT: tablescans
    //    //.....[
    //    //....|....ELEMENT: tablescan
    //    //....|....|.jointype = inner
    //    //....|....|.table = ALPHA
    //    //....|....|.tablealias = ALEF
    //    //....|....[
    //    //....|....|...ELEMENT: joincond
    //    //....|....|...[
    //    //....|....|....|..ELEMENT: operation
    //    //....|....|....|....id = 3
    //    //....|....|....|....optype = equal
    //    //....|....|....|..[
    //    //....|....|....|....|.ELEMENT: columnref
    //    //....|....|....|....|...alias = ID
    //    //....|....|....|....|...column = ID
    //    //....|....|....|....|...id = 1
    //    //....|....|....|....|...index = 0
    //    //....|....|....|....|...table = ALPHA
    //    //....|....|....|....|...tablealias = ALEF
    //    //....|....|....|....|.ELEMENT: columnref
    //    //....|....|....|....|...alias = BETA
    //    //....|....|....|....|...column = BETA
    //    //....|....|....|....|...id = 2
    //    //....|....|....|....|...index = 1
    //    //....|....|....|....|...table = ALPHA
    //    //....|....|....|....|...tablealias = ALEF
    //    //
    //    //
    //
    //
    @SuppressWarnings("unchecked")
    @Test
    public void TestColumnRefAliases() throws Exception {
        String sql    = "select * from alpha as alef where alef.id = alef.beta";
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
                                   "tablealias", "ALEF",
                        withAttribute(4, "alias", "ID"),
                        withAttribute(5, "column", "ID"),
                        withIdAttribute(6, idTable),
                        withAttribute(7, "index", "0"),
                        withAttribute(8, "table", "ALPHA"),
                        withAttribute(9, "tablealias", "ALEF")),
                    withChildNamed(10, "columnref",
                                   "alias", "BETA",
                                   "column", "BETA",
                                   "table", "ALPHA",
                                   "tablealias", "ALEF",
                        withAttribute(11, "alias", "BETA"),
                        withAttribute(12, "column", "BETA"),
                        withIdAttribute(13, idTable),
                        withAttribute(14, "index", "1"),
                        withAttribute(15, "table", "ALPHA"),
                        withAttribute(16, "tablealias", "ALEF"))),
                withChildNamed(17, "parameters"),
                withChildNamed(18, "tablescans",
                    withChildNamed(19, "tablescan",
                        withAttribute(20, "jointype", "inner"),
                        withAttribute(21, "table", "ALPHA"),
                        withAttribute(22, "tablealias", "ALEF"),
                        withChildNamed(23, "joincond",
                            withChildNamed(24, "operation",
                                           "optype", "equal",
                                withIdAttribute(25, idTable),
                                withAttribute(26, "optype", "equal"),
                                withChildNamed(27, "columnref",
                                               "alias", "ID",
                                               "column", "ID",
                                               "table", "ALPHA",
                                               "tablealias", "ALEF",
                                    withAttribute(28, "alias", "ID"),
                                    withAttribute(29, "column", "ID"),
                                    withIdAttribute(30, idTable),
                                    withAttribute(31, "index", "0"),
                                    withAttribute(32, "table", "ALPHA"),
                                    withAttribute(33, "tablealias", "ALEF")),
                                withChildNamed(34, "columnref",
                                               "alias", "BETA",
                                               "column", "BETA",
                                               "table", "ALPHA",
                                               "tablealias", "ALEF",
                                    withAttribute(35, "alias", "BETA"),
                                    withAttribute(36, "column", "BETA"),
                                    withIdAttribute(37, idTable),
                                    withAttribute(38, "index", "1"),
                                    withAttribute(39, "table", "ALPHA"),
                                    withAttribute(40, "tablealias", "ALEF")))))));
    }
}
