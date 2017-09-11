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

public class TestTableCreation {
    HSQLInterface m_HSQLInterface = null;
    String        m_schema = null;
    public TestTableCreation() {
        m_HSQLInterface = HSQLInterface.loadHsqldb(ParameterizationInfo.getParamStateManager());
    }
    /**
     * Test TINYINT type.
     *
     * Throws: Exception
     */

    //
    // SQL: create table alpha ( id TiNyInT not null, beta TINYINT)
    //
    //
    // Pattern XML:
    //.ELEMENT: databaseschema
    //...name = databaseschema
    //.[
    //.....ELEMENT: table
    //....|..name = ALPHA
    //.....[
    //....|....ELEMENT: columns
    //....|....|.name = columns
    //....|....[
    //....|....|...ELEMENT: column
    //....|....|.....index = 0
    //....|....|.....name = ID
    //....|....|.....nullable = false
    //....|....|.....size = 3
    //....|....|.....valuetype = TINYINT
    //....|....|...ELEMENT: column
    //....|....|.....index = 1
    //....|....|.....name = BETA
    //....|....|.....nullable = true
    //....|....|.....size = 3
    //....|....|.....valuetype = TINYINT
    //....|....ELEMENT: indexes
    //....|....|.name = indexes
    //....|....[
    //....|....|...ELEMENT: index
    //....|....|.....assumeunique = false
    //....|....|.....columns =
    //....|....|.....ishashindex = false
    //....|....|.....name = SYS_IDX_10002
    //....|....|.....unique = true
    //....|....ELEMENT: constraints
    //....|....|.name = constraints
    //....|....[
    //....|....|...ELEMENT: constraint
    //....|....|.....assumeunique = false
    //....|....|.....constrainttype = NOT_NULL
    //....|....|.....name = SYS_CT_10001
    //....|....|.....nameisauto = true
    //....|....|.....rowslimit = 2147483647
    //
    //
    //
    @SuppressWarnings("unchecked")
    @Test
    public void testCreateTableTinyInt() throws Exception {
        String ddl    = "create table alpha ( id TiNyInT not null, beta TINYINT)";
        IDTable idTable = new IDTable();
        HSQLInterface hif = HSQLInterface.loadHsqldb(ParameterizationInfo.getParamStateManager());
        hif.processDDLStatementsUsingVoltSQLParser(ddl, null);
        VoltXMLElement element = hif.getVoltCatalogXML(null);
        assertThat(element)
            .hasName(1, "databaseschema")
            .hasAllOf(
                withAttribute(2, "name", "databaseschema"),
                withChildNamed(3, "table",
                    withAttribute(4, "name", "ALPHA"),
                    withChildNamed(5, "columns",
                        withAttribute(6, "name", "columns"),
                        withChildNamed(7, "column",
                                       "name", "ID",
                            withAttribute(8, "index", "0"),
                            withAttribute(9, "name", "ID"),
                            withAttribute(10, "nullable", "false"),
                            withAttribute(11, "size", "3"),
                            withAttribute(12, "valuetype", "TINYINT")),
                        withChildNamed(13, "column",
                                       "name", "BETA",
                            withAttribute(14, "index", "1"),
                            withAttribute(15, "name", "BETA"),
                            withAttribute(16, "nullable", "true"),
                            withAttribute(17, "size", "3"),
                            withAttribute(18, "valuetype", "TINYINT"))),
                    withChildNamed(19, "indexes",
                        withAttribute(20, "name", "indexes"),
                        withChildNamed(21, "index",
                                       "name", "SYS_IDX_10002",
                            withAttribute(22, "assumeunique", "false"),
                            withAttribute(23, "columns", ""),
                            withAttribute(24, "ishashindex", "false"),
                            withAttribute(25, "name", "SYS_IDX_10002"),
                            withAttribute(26, "unique", "true"))),
                    withChildNamed(27, "constraints",
                        withAttribute(28, "name", "constraints"))));
    }
    /**
     * Test SMALLINT type.
     *
     * Throws: Exception
     */

    //
    // SQL: create table alpha ( id SmallInt not null, beta SMALLINT)
    //
    //
    // Pattern XML:
    //.ELEMENT: databaseschema
    //...name = databaseschema
    //.[
    //.....ELEMENT: table
    //....|..name = ALPHA
    //.....[
    //....|....ELEMENT: columns
    //....|....|.name = columns
    //....|....[
    //....|....|...ELEMENT: column
    //....|....|.....index = 0
    //....|....|.....name = ID
    //....|....|.....nullable = false
    //....|....|.....size = 5
    //....|....|.....valuetype = SMALLINT
    //....|....|...ELEMENT: column
    //....|....|.....index = 1
    //....|....|.....name = BETA
    //....|....|.....nullable = true
    //....|....|.....size = 5
    //....|....|.....valuetype = SMALLINT
    //....|....ELEMENT: indexes
    //....|....|.name = indexes
    //....|....[
    //....|....|...ELEMENT: index
    //....|....|.....assumeunique = false
    //....|....|.....columns =
    //....|....|.....ishashindex = false
    //....|....|.....name = SYS_IDX_10002
    //....|....|.....unique = true
    //....|....ELEMENT: constraints
    //....|....|.name = constraints
    //....|....[
    //....|....|...ELEMENT: constraint
    //....|....|.....assumeunique = false
    //....|....|.....constrainttype = NOT_NULL
    //....|....|.....name = SYS_CT_10001
    //....|....|.....nameisauto = true
    //....|....|.....rowslimit = 2147483647
    //
    //
    //
    @SuppressWarnings("unchecked")
    @Test
    public void testCreateTableSmallInt() throws Exception {
        String ddl    = "create table alpha ( id SmallInt not null, beta SMALLINT)";
        IDTable idTable = new IDTable();
        HSQLInterface hif = HSQLInterface.loadHsqldb(ParameterizationInfo.getParamStateManager());
        hif.processDDLStatementsUsingVoltSQLParser(ddl, null);
        VoltXMLElement element = hif.getVoltCatalogXML(null);
        assertThat(element)
            .hasName(1, "databaseschema")
            .hasAllOf(
                withAttribute(2, "name", "databaseschema"),
                withChildNamed(3, "table",
                    withAttribute(4, "name", "ALPHA"),
                    withChildNamed(5, "columns",
                        withAttribute(6, "name", "columns"),
                        withChildNamed(7, "column",
                                       "name", "ID",
                            withAttribute(8, "index", "0"),
                            withAttribute(9, "name", "ID"),
                            withAttribute(10, "nullable", "false"),
                            withAttribute(11, "size", "5"),
                            withAttribute(12, "valuetype", "SMALLINT")),
                        withChildNamed(13, "column",
                                       "name", "BETA",
                            withAttribute(14, "index", "1"),
                            withAttribute(15, "name", "BETA"),
                            withAttribute(16, "nullable", "true"),
                            withAttribute(17, "size", "5"),
                            withAttribute(18, "valuetype", "SMALLINT"))),
                    withChildNamed(19, "indexes",
                        withAttribute(20, "name", "indexes"),
                        withChildNamed(21, "index",
                                       "name", "SYS_IDX_10002",
                            withAttribute(22, "assumeunique", "false"),
                            withAttribute(23, "columns", ""),
                            withAttribute(24, "ishashindex", "false"),
                            withAttribute(25, "name", "SYS_IDX_10002"),
                            withAttribute(26, "unique", "true"))),
                    withChildNamed(27, "constraints",
                        withAttribute(28, "name", "constraints"))));
    }
    /**
     * Test INTEGER type.
     *
     * Throws: Exception
     */

    //
    // SQL: create table alpha ( id integer not null, beta integer)
    //
    //
    // Pattern XML:
    //.ELEMENT: databaseschema
    //...name = databaseschema
    //.[
    //.....ELEMENT: table
    //....|..name = ALPHA
    //.....[
    //....|....ELEMENT: columns
    //....|....|.name = columns
    //....|....[
    //....|....|...ELEMENT: column
    //....|....|.....index = 0
    //....|....|.....name = ID
    //....|....|.....nullable = false
    //....|....|.....size = 10
    //....|....|.....valuetype = INTEGER
    //....|....|...ELEMENT: column
    //....|....|.....index = 1
    //....|....|.....name = BETA
    //....|....|.....nullable = true
    //....|....|.....size = 10
    //....|....|.....valuetype = INTEGER
    //....|....ELEMENT: indexes
    //....|....|.name = indexes
    //....|....[
    //....|....|...ELEMENT: index
    //....|....|.....assumeunique = false
    //....|....|.....columns =
    //....|....|.....ishashindex = false
    //....|....|.....name = SYS_IDX_10002
    //....|....|.....unique = true
    //....|....ELEMENT: constraints
    //....|....|.name = constraints
    //....|....[
    //....|....|...ELEMENT: constraint
    //....|....|.....assumeunique = false
    //....|....|.....constrainttype = NOT_NULL
    //....|....|.....name = SYS_CT_10001
    //....|....|.....nameisauto = true
    //....|....|.....rowslimit = 2147483647
    //
    //
    //
    @SuppressWarnings("unchecked")
    @Test
    public void testCreateTableInteger() throws Exception {
        String ddl    = "create table alpha ( id integer not null, beta integer)";
        IDTable idTable = new IDTable();
        HSQLInterface hif = HSQLInterface.loadHsqldb(ParameterizationInfo.getParamStateManager());
        hif.processDDLStatementsUsingVoltSQLParser(ddl, null);
        VoltXMLElement element = hif.getVoltCatalogXML(null);
        assertThat(element)
            .hasName(1, "databaseschema")
            .hasAllOf(
                withAttribute(2, "name", "databaseschema"),
                withChildNamed(3, "table",
                    withAttribute(4, "name", "ALPHA"),
                    withChildNamed(5, "columns",
                        withAttribute(6, "name", "columns"),
                        withChildNamed(7, "column",
                                       "name", "ID",
                            withAttribute(8, "index", "0"),
                            withAttribute(9, "name", "ID"),
                            withAttribute(10, "nullable", "false"),
                            withAttribute(11, "size", "10"),
                            withAttribute(12, "valuetype", "INTEGER")),
                        withChildNamed(13, "column",
                                       "name", "BETA",
                            withAttribute(14, "index", "1"),
                            withAttribute(15, "name", "BETA"),
                            withAttribute(16, "nullable", "true"),
                            withAttribute(17, "size", "10"),
                            withAttribute(18, "valuetype", "INTEGER"))),
                    withChildNamed(19, "indexes",
                        withAttribute(20, "name", "indexes"),
                        withChildNamed(21, "index",
                                       "name", "SYS_IDX_10002",
                            withAttribute(22, "assumeunique", "false"),
                            withAttribute(23, "columns", ""),
                            withAttribute(24, "ishashindex", "false"),
                            withAttribute(25, "name", "SYS_IDX_10002"),
                            withAttribute(26, "unique", "true"))),
                    withChildNamed(27, "constraints",
                        withAttribute(28, "name", "constraints"))));
    }
    /**
     * Test BIGINT type.
     *
     * Throws: Exception
     */

    //
    // SQL: create table alpha ( id BiGiNt not null, beta bIgInT)
    //
    //
    // Pattern XML:
    //.ELEMENT: databaseschema
    //...name = databaseschema
    //.[
    //.....ELEMENT: table
    //....|..name = ALPHA
    //.....[
    //....|....ELEMENT: columns
    //....|....|.name = columns
    //....|....[
    //....|....|...ELEMENT: column
    //....|....|.....index = 0
    //....|....|.....name = ID
    //....|....|.....nullable = false
    //....|....|.....size = 19
    //....|....|.....valuetype = BIGINT
    //....|....|...ELEMENT: column
    //....|....|.....index = 1
    //....|....|.....name = BETA
    //....|....|.....nullable = true
    //....|....|.....size = 19
    //....|....|.....valuetype = BIGINT
    //....|....ELEMENT: indexes
    //....|....|.name = indexes
    //....|....[
    //....|....|...ELEMENT: index
    //....|....|.....assumeunique = false
    //....|....|.....columns =
    //....|....|.....ishashindex = false
    //....|....|.....name = SYS_IDX_10002
    //....|....|.....unique = true
    //....|....ELEMENT: constraints
    //....|....|.name = constraints
    //....|....[
    //....|....|...ELEMENT: constraint
    //....|....|.....assumeunique = false
    //....|....|.....constrainttype = NOT_NULL
    //....|....|.....name = SYS_CT_10001
    //....|....|.....nameisauto = true
    //....|....|.....rowslimit = 2147483647
    //
    //
    //
    @SuppressWarnings("unchecked")
    @Test
    public void testCreateTableBigInt() throws Exception {
        String ddl    = "create table alpha ( id BiGiNt not null, beta bIgInT)";
        IDTable idTable = new IDTable();
        HSQLInterface hif = HSQLInterface.loadHsqldb(ParameterizationInfo.getParamStateManager());
        hif.processDDLStatementsUsingVoltSQLParser(ddl, null);
        VoltXMLElement element = hif.getVoltCatalogXML(null);
        assertThat(element)
            .hasName(1, "databaseschema")
            .hasAllOf(
                withAttribute(2, "name", "databaseschema"),
                withChildNamed(3, "table",
                    withAttribute(4, "name", "ALPHA"),
                    withChildNamed(5, "columns",
                        withAttribute(6, "name", "columns"),
                        withChildNamed(7, "column",
                                       "name", "ID",
                            withAttribute(8, "index", "0"),
                            withAttribute(9, "name", "ID"),
                            withAttribute(10, "nullable", "false"),
                            withAttribute(11, "size", "19"),
                            withAttribute(12, "valuetype", "BIGINT")),
                        withChildNamed(13, "column",
                                       "name", "BETA",
                            withAttribute(14, "index", "1"),
                            withAttribute(15, "name", "BETA"),
                            withAttribute(16, "nullable", "true"),
                            withAttribute(17, "size", "19"),
                            withAttribute(18, "valuetype", "BIGINT"))),
                    withChildNamed(19, "indexes",
                        withAttribute(20, "name", "indexes"),
                        withChildNamed(21, "index",
                                       "name", "SYS_IDX_10002",
                            withAttribute(22, "assumeunique", "false"),
                            withAttribute(23, "columns", ""),
                            withAttribute(24, "ishashindex", "false"),
                            withAttribute(25, "name", "SYS_IDX_10002"),
                            withAttribute(26, "unique", "true"))),
                    withChildNamed(27, "constraints",
                        withAttribute(28, "name", "constraints"))));
    }
    /**
     * Test DECIMAL type, default scale and precision.
     *
     * Throws: Exception
     */

    //
    // SQL: create table alpha ( id integer not null, beta Decimal                 not null)
    //
    //
    // Pattern XML:
    //.ELEMENT: databaseschema
    //...name = databaseschema
    //.[
    //.....ELEMENT: table
    //....|..name = ALPHA
    //.....[
    //....|....ELEMENT: columns
    //....|....|.name = columns
    //....|....[
    //....|....|...ELEMENT: column
    //....|....|.....index = 0
    //....|....|.....name = ID
    //....|....|.....nullable = false
    //....|....|.....size = 10
    //....|....|.....valuetype = INTEGER
    //....|....|...ELEMENT: column
    //....|....|.....index = 1
    //....|....|.....name = BETA
    //....|....|.....nullable = false
    //....|....|.....size = 100
    //....|....|.....valuetype = DECIMAL
    //....|....ELEMENT: indexes
    //....|....|.name = indexes
    //....|....[
    //....|....|...ELEMENT: index
    //....|....|.....assumeunique = false
    //....|....|.....columns =
    //....|....|.....ishashindex = false
    //....|....|.....name = SYS_IDX_10003
    //....|....|.....unique = true
    //....|....ELEMENT: constraints
    //....|....|.name = constraints
    //....|....[
    //....|....|...ELEMENT: constraint
    //....|....|.....assumeunique = false
    //....|....|.....constrainttype = NOT_NULL
    //....|....|.....name = SYS_CT_10001
    //....|....|.....nameisauto = true
    //....|....|.....rowslimit = 2147483647
    //....|....|...ELEMENT: constraint
    //....|....|.....assumeunique = false
    //....|....|.....constrainttype = NOT_NULL
    //....|....|.....name = SYS_CT_10002
    //....|....|.....nameisauto = true
    //....|....|.....rowslimit = 2147483647
    //
    //
    //
    @SuppressWarnings("unchecked")
    @Test
    public void testCreateTableWithDecimal() throws Exception {
        String ddl    = "create table alpha ( id integer not null, beta Decimal                 not null)";
        IDTable idTable = new IDTable();
        HSQLInterface hif = HSQLInterface.loadHsqldb(ParameterizationInfo.getParamStateManager());
        hif.processDDLStatementsUsingVoltSQLParser(ddl, null);
        VoltXMLElement element = hif.getVoltCatalogXML(null);
        assertThat(element)
            .hasName(1, "databaseschema")
            .hasAllOf(
                withAttribute(2, "name", "databaseschema"),
                withChildNamed(3, "table",
                    withAttribute(4, "name", "ALPHA"),
                    withChildNamed(5, "columns",
                        withAttribute(6, "name", "columns"),
                        withChildNamed(7, "column",
                                       "name", "ID",
                            withAttribute(8, "index", "0"),
                            withAttribute(9, "name", "ID"),
                            withAttribute(10, "nullable", "false"),
                            withAttribute(11, "size", "10"),
                            withAttribute(12, "valuetype", "INTEGER")),
                        withChildNamed(13, "column",
                                       "name", "BETA",
                            withAttribute(14, "index", "1"),
                            withAttribute(15, "name", "BETA"),
                            withAttribute(16, "nullable", "false"),
                            withAttribute(17, "size", "100"),
                            withAttribute(18, "valuetype", "DECIMAL"))),
                    withChildNamed(19, "indexes",
                        withAttribute(20, "name", "indexes"),
                        withChildNamed(21, "index",
                                       "name", "SYS_IDX_10003",
                            withAttribute(22, "assumeunique", "false"),
                            withAttribute(23, "columns", ""),
                            withAttribute(24, "ishashindex", "false"),
                            withAttribute(25, "name", "SYS_IDX_10003"),
                            withAttribute(26, "unique", "true"))),
                    withChildNamed(27, "constraints",
                        withAttribute(28, "name", "constraints"))));
    }
    /**
     * Test DECIMAL type with scale and precision.
     *
     * Throws: Exception
     */

    //
    // SQL: create table alpha ( id integer not null, beta Decimal (10, 100) not null)
    //
    //
    // Pattern XML:
    //.ELEMENT: databaseschema
    //...name = databaseschema
    //.[
    //.....ELEMENT: table
    //....|..name = ALPHA
    //.....[
    //....|....ELEMENT: columns
    //....|....|.name = columns
    //....|....[
    //....|....|...ELEMENT: column
    //....|....|.....index = 0
    //....|....|.....name = ID
    //....|....|.....nullable = false
    //....|....|.....size = 10
    //....|....|.....valuetype = INTEGER
    //....|....|...ELEMENT: column
    //....|....|.....index = 1
    //....|....|.....name = BETA
    //....|....|.....nullable = false
    //....|....|.....size = 10
    //....|....|.....valuetype = DECIMAL
    //....|....ELEMENT: indexes
    //....|....|.name = indexes
    //....|....[
    //....|....|...ELEMENT: index
    //....|....|.....assumeunique = false
    //....|....|.....columns =
    //....|....|.....ishashindex = false
    //....|....|.....name = SYS_IDX_10003
    //....|....|.....unique = true
    //....|....ELEMENT: constraints
    //....|....|.name = constraints
    //....|....[
    //....|....|...ELEMENT: constraint
    //....|....|.....assumeunique = false
    //....|....|.....constrainttype = NOT_NULL
    //....|....|.....name = SYS_CT_10001
    //....|....|.....nameisauto = true
    //....|....|.....rowslimit = 2147483647
    //....|....|...ELEMENT: constraint
    //....|....|.....assumeunique = false
    //....|....|.....constrainttype = NOT_NULL
    //....|....|.....name = SYS_CT_10002
    //....|....|.....nameisauto = true
    //....|....|.....rowslimit = 2147483647
    //
    //
    //
    @SuppressWarnings("unchecked")
    @Test
    public void testCreateTableWithDecimalScalePrecision() throws Exception {
        String ddl    = "create table alpha ( id integer not null, beta Decimal (10, 100) not null)";
        IDTable idTable = new IDTable();
        HSQLInterface hif = HSQLInterface.loadHsqldb(ParameterizationInfo.getParamStateManager());
        hif.processDDLStatementsUsingVoltSQLParser(ddl, null);
        VoltXMLElement element = hif.getVoltCatalogXML(null);
        assertThat(element)
            .hasName(1, "databaseschema")
            .hasAllOf(
                withAttribute(2, "name", "databaseschema"),
                withChildNamed(3, "table",
                    withAttribute(4, "name", "ALPHA"),
                    withChildNamed(5, "columns",
                        withAttribute(6, "name", "columns"),
                        withChildNamed(7, "column",
                                       "name", "ID",
                            withAttribute(8, "index", "0"),
                            withAttribute(9, "name", "ID"),
                            withAttribute(10, "nullable", "false"),
                            withAttribute(11, "size", "10"),
                            withAttribute(12, "valuetype", "INTEGER")),
                        withChildNamed(13, "column",
                                       "name", "BETA",
                            withAttribute(14, "index", "1"),
                            withAttribute(15, "name", "BETA"),
                            withAttribute(16, "nullable", "false"),
                            withAttribute(17, "size", "10"),
                            withAttribute(18, "valuetype", "DECIMAL"))),
                    withChildNamed(19, "indexes",
                        withAttribute(20, "name", "indexes"),
                        withChildNamed(21, "index",
                                       "name", "SYS_IDX_10003",
                            withAttribute(22, "assumeunique", "false"),
                            withAttribute(23, "columns", ""),
                            withAttribute(24, "ishashindex", "false"),
                            withAttribute(25, "name", "SYS_IDX_10003"),
                            withAttribute(26, "unique", "true"))),
                    withChildNamed(27, "constraints",
                        withAttribute(28, "name", "constraints"))));
    }
    /**
     * Test FLOAT type
     *
     * Throws: Exception
     */

    //
    // SQL: create table alpha ( id BiGiNt not null, beta FlOaT)
    //
    //
    // Pattern XML:
    //.ELEMENT: databaseschema
    //...name = databaseschema
    //.[
    //.....ELEMENT: table
    //....|..name = ALPHA
    //.....[
    //....|....ELEMENT: columns
    //....|....|.name = columns
    //....|....[
    //....|....|...ELEMENT: column
    //....|....|.....index = 0
    //....|....|.....name = ID
    //....|....|.....nullable = false
    //....|....|.....size = 19
    //....|....|.....valuetype = BIGINT
    //....|....|...ELEMENT: column
    //....|....|.....index = 1
    //....|....|.....name = BETA
    //....|....|.....nullable = true
    //....|....|.....size = 0
    //....|....|.....valuetype = FLOAT
    //....|....ELEMENT: indexes
    //....|....|.name = indexes
    //....|....[
    //....|....|...ELEMENT: index
    //....|....|.....assumeunique = false
    //....|....|.....columns =
    //....|....|.....ishashindex = false
    //....|....|.....name = SYS_IDX_10002
    //....|....|.....unique = true
    //....|....ELEMENT: constraints
    //....|....|.name = constraints
    //....|....[
    //....|....|...ELEMENT: constraint
    //....|....|.....assumeunique = false
    //....|....|.....constrainttype = NOT_NULL
    //....|....|.....name = SYS_CT_10001
    //....|....|.....nameisauto = true
    //....|....|.....rowslimit = 2147483647
    //
    //
    //
    @SuppressWarnings("unchecked")
    @Test
    public void testCreateTableFloat() throws Exception {
        String ddl    = "create table alpha ( id BiGiNt not null, beta FlOaT)";
        IDTable idTable = new IDTable();
        HSQLInterface hif = HSQLInterface.loadHsqldb(ParameterizationInfo.getParamStateManager());
        hif.processDDLStatementsUsingVoltSQLParser(ddl, null);
        VoltXMLElement element = hif.getVoltCatalogXML(null);
        assertThat(element)
            .hasName(1, "databaseschema")
            .hasAllOf(
                withAttribute(2, "name", "databaseschema"),
                withChildNamed(3, "table",
                    withAttribute(4, "name", "ALPHA"),
                    withChildNamed(5, "columns",
                        withAttribute(6, "name", "columns"),
                        withChildNamed(7, "column",
                                       "name", "ID",
                            withAttribute(8, "index", "0"),
                            withAttribute(9, "name", "ID"),
                            withAttribute(10, "nullable", "false"),
                            withAttribute(11, "size", "19"),
                            withAttribute(12, "valuetype", "BIGINT")),
                        withChildNamed(13, "column",
                                       "name", "BETA",
                            withAttribute(14, "index", "1"),
                            withAttribute(15, "name", "BETA"),
                            withAttribute(16, "nullable", "true"),
                            withAttribute(17, "size", "0"),
                            withAttribute(18, "valuetype", "FLOAT"))),
                    withChildNamed(19, "indexes",
                        withAttribute(20, "name", "indexes"),
                        withChildNamed(21, "index",
                                       "name", "SYS_IDX_10002",
                            withAttribute(22, "assumeunique", "false"),
                            withAttribute(23, "columns", ""),
                            withAttribute(24, "ishashindex", "false"),
                            withAttribute(25, "name", "SYS_IDX_10002"),
                            withAttribute(26, "unique", "true"))),
                    withChildNamed(27, "constraints",
                        withAttribute(28, "name", "constraints"))));
    }
    /**
     * Test VARCHAR type
     *
     * Throws: Exception
     */

    //
    // SQL: create table alpha ( id BiGiNt not null, beta varchar(100))
    //
    //
    // Pattern XML:
    //.ELEMENT: databaseschema
    //...name = databaseschema
    //.[
    //.....ELEMENT: table
    //....|..name = ALPHA
    //.....[
    //....|....ELEMENT: columns
    //....|....|.name = columns
    //....|....[
    //....|....|...ELEMENT: column
    //....|....|.....index = 0
    //....|....|.....name = ID
    //....|....|.....nullable = false
    //....|....|.....size = 19
    //....|....|.....valuetype = BIGINT
    //....|....|...ELEMENT: column
    //....|....|.....bytes = false
    //....|....|.....index = 1
    //....|....|.....name = BETA
    //....|....|.....nullable = true
    //....|....|.....size = 100
    //....|....|.....valuetype = VARCHAR
    //....|....ELEMENT: indexes
    //....|....|.name = indexes
    //....|....[
    //....|....|...ELEMENT: index
    //....|....|.....assumeunique = false
    //....|....|.....columns =
    //....|....|.....ishashindex = false
    //....|....|.....name = SYS_IDX_10002
    //....|....|.....unique = true
    //....|....ELEMENT: constraints
    //....|....|.name = constraints
    //....|....[
    //....|....|...ELEMENT: constraint
    //....|....|.....assumeunique = false
    //....|....|.....constrainttype = NOT_NULL
    //....|....|.....name = SYS_CT_10001
    //....|....|.....nameisauto = true
    //....|....|.....rowslimit = 2147483647
    //
    //
    //
    @SuppressWarnings("unchecked")
    @Test
    public void testCreateTableVarchar() throws Exception {
        String ddl    = "create table alpha ( id BiGiNt not null, beta varchar(100))";
        IDTable idTable = new IDTable();
        HSQLInterface hif = HSQLInterface.loadHsqldb(ParameterizationInfo.getParamStateManager());
        hif.processDDLStatementsUsingVoltSQLParser(ddl, null);
        VoltXMLElement element = hif.getVoltCatalogXML(null);
        assertThat(element)
            .hasName(1, "databaseschema")
            .hasAllOf(
                withAttribute(2, "name", "databaseschema"),
                withChildNamed(3, "table",
                    withAttribute(4, "name", "ALPHA"),
                    withChildNamed(5, "columns",
                        withAttribute(6, "name", "columns"),
                        withChildNamed(7, "column",
                                       "name", "ID",
                            withAttribute(8, "index", "0"),
                            withAttribute(9, "name", "ID"),
                            withAttribute(10, "nullable", "false"),
                            withAttribute(11, "size", "19"),
                            withAttribute(12, "valuetype", "BIGINT")),
                        withChildNamed(13, "column",
                                       "name", "BETA",
                            withAttribute(14, "bytes", "false"),
                            withAttribute(15, "index", "1"),
                            withAttribute(16, "name", "BETA"),
                            withAttribute(17, "nullable", "true"),
                            withAttribute(18, "size", "100"),
                            withAttribute(19, "valuetype", "VARCHAR"))),
                    withChildNamed(20, "indexes",
                        withAttribute(21, "name", "indexes"),
                        withChildNamed(22, "index",
                                       "name", "SYS_IDX_10002",
                            withAttribute(23, "assumeunique", "false"),
                            withAttribute(24, "columns", ""),
                            withAttribute(25, "ishashindex", "false"),
                            withAttribute(26, "name", "SYS_IDX_10002"),
                            withAttribute(27, "unique", "true"))),
                    withChildNamed(28, "constraints",
                        withAttribute(29, "name", "constraints"))));
    }
    /**
     * Test VARBINARY type
     *
     * Throws: Exception
     */

    //
    // SQL: create table alpha ( id BiGiNt not null, beta varbinary(100))
    //
    //
    // Pattern XML:
    //.ELEMENT: databaseschema
    //...name = databaseschema
    //.[
    //.....ELEMENT: table
    //....|..name = ALPHA
    //.....[
    //....|....ELEMENT: columns
    //....|....|.name = columns
    //....|....[
    //....|....|...ELEMENT: column
    //....|....|.....index = 0
    //....|....|.....name = ID
    //....|....|.....nullable = false
    //....|....|.....size = 19
    //....|....|.....valuetype = BIGINT
    //....|....|...ELEMENT: column
    //....|....|.....index = 1
    //....|....|.....name = BETA
    //....|....|.....nullable = true
    //....|....|.....size = 100
    //....|....|.....valuetype = VARBINARY
    //....|....ELEMENT: indexes
    //....|....|.name = indexes
    //....|....[
    //....|....|...ELEMENT: index
    //....|....|.....assumeunique = false
    //....|....|.....columns =
    //....|....|.....ishashindex = false
    //....|....|.....name = SYS_IDX_10002
    //....|....|.....unique = true
    //....|....ELEMENT: constraints
    //....|....|.name = constraints
    //....|....[
    //....|....|...ELEMENT: constraint
    //....|....|.....assumeunique = false
    //....|....|.....constrainttype = NOT_NULL
    //....|....|.....name = SYS_CT_10001
    //....|....|.....nameisauto = true
    //....|....|.....rowslimit = 2147483647
    //
    //
    //
    @SuppressWarnings("unchecked")
    @Test
    public void testCreateTableVarbinary() throws Exception {
        String ddl    = "create table alpha ( id BiGiNt not null, beta varbinary(100))";
        IDTable idTable = new IDTable();
        HSQLInterface hif = HSQLInterface.loadHsqldb(ParameterizationInfo.getParamStateManager());
        hif.processDDLStatementsUsingVoltSQLParser(ddl, null);
        VoltXMLElement element = hif.getVoltCatalogXML(null);
        assertThat(element)
            .hasName(1, "databaseschema")
            .hasAllOf(
                withAttribute(2, "name", "databaseschema"),
                withChildNamed(3, "table",
                    withAttribute(4, "name", "ALPHA"),
                    withChildNamed(5, "columns",
                        withAttribute(6, "name", "columns"),
                        withChildNamed(7, "column",
                                       "name", "ID",
                            withAttribute(8, "index", "0"),
                            withAttribute(9, "name", "ID"),
                            withAttribute(10, "nullable", "false"),
                            withAttribute(11, "size", "19"),
                            withAttribute(12, "valuetype", "BIGINT")),
                        withChildNamed(13, "column",
                                       "name", "BETA",
                            withAttribute(14, "index", "1"),
                            withAttribute(15, "name", "BETA"),
                            withAttribute(16, "nullable", "true"),
                            withAttribute(17, "size", "100"),
                            withAttribute(18, "valuetype", "VARBINARY"))),
                    withChildNamed(19, "indexes",
                        withAttribute(20, "name", "indexes"),
                        withChildNamed(21, "index",
                                       "name", "SYS_IDX_10002",
                            withAttribute(22, "assumeunique", "false"),
                            withAttribute(23, "columns", ""),
                            withAttribute(24, "ishashindex", "false"),
                            withAttribute(25, "name", "SYS_IDX_10002"),
                            withAttribute(26, "unique", "true"))),
                    withChildNamed(27, "constraints",
                        withAttribute(28, "name", "constraints"))));
    }
    /**
     * Test TIMESTAMP type
     *
     * Throws: Exception
     */

    //
    // SQL: create table alpha ( id BiGiNt not null, beta timestamp)
    //
    //
    // Pattern XML:
    //.ELEMENT: databaseschema
    //...name = databaseschema
    //.[
    //.....ELEMENT: table
    //....|..name = ALPHA
    //.....[
    //....|....ELEMENT: columns
    //....|....|.name = columns
    //....|....[
    //....|....|...ELEMENT: column
    //....|....|.....index = 0
    //....|....|.....name = ID
    //....|....|.....nullable = false
    //....|....|.....size = 19
    //....|....|.....valuetype = BIGINT
    //....|....|...ELEMENT: column
    //....|....|.....index = 1
    //....|....|.....name = BETA
    //....|....|.....nullable = true
    //....|....|.....size = 8
    //....|....|.....valuetype = TIMESTAMP
    //....|....ELEMENT: indexes
    //....|....|.name = indexes
    //....|....[
    //....|....|...ELEMENT: index
    //....|....|.....assumeunique = false
    //....|....|.....columns =
    //....|....|.....ishashindex = false
    //....|....|.....name = SYS_IDX_10002
    //....|....|.....unique = true
    //....|....ELEMENT: constraints
    //....|....|.name = constraints
    //....|....[
    //....|....|...ELEMENT: constraint
    //....|....|.....assumeunique = false
    //....|....|.....constrainttype = NOT_NULL
    //....|....|.....name = SYS_CT_10001
    //....|....|.....nameisauto = true
    //....|....|.....rowslimit = 2147483647
    //
    //
    //
    @SuppressWarnings("unchecked")
    @Test
    public void testCreateTableTimestamp() throws Exception {
        String ddl    = "create table alpha ( id BiGiNt not null, beta timestamp)";
        IDTable idTable = new IDTable();
        HSQLInterface hif = HSQLInterface.loadHsqldb(ParameterizationInfo.getParamStateManager());
        hif.processDDLStatementsUsingVoltSQLParser(ddl, null);
        VoltXMLElement element = hif.getVoltCatalogXML(null);
        assertThat(element)
            .hasName(1, "databaseschema")
            .hasAllOf(
                withAttribute(2, "name", "databaseschema"),
                withChildNamed(3, "table",
                    withAttribute(4, "name", "ALPHA"),
                    withChildNamed(5, "columns",
                        withAttribute(6, "name", "columns"),
                        withChildNamed(7, "column",
                                       "name", "ID",
                            withAttribute(8, "index", "0"),
                            withAttribute(9, "name", "ID"),
                            withAttribute(10, "nullable", "false"),
                            withAttribute(11, "size", "19"),
                            withAttribute(12, "valuetype", "BIGINT")),
                        withChildNamed(13, "column",
                                       "name", "BETA",
                            withAttribute(14, "index", "1"),
                            withAttribute(15, "name", "BETA"),
                            withAttribute(16, "nullable", "true"),
                            withAttribute(17, "size", "8"),
                            withAttribute(18, "valuetype", "TIMESTAMP"))),
                    withChildNamed(19, "indexes",
                        withAttribute(20, "name", "indexes"),
                        withChildNamed(21, "index",
                                       "name", "SYS_IDX_10002",
                            withAttribute(22, "assumeunique", "false"),
                            withAttribute(23, "columns", ""),
                            withAttribute(24, "ishashindex", "false"),
                            withAttribute(25, "name", "SYS_IDX_10002"),
                            withAttribute(26, "unique", "true"))),
                    withChildNamed(27, "constraints",
                        withAttribute(28, "name", "constraints"))));
    }
    /**
     * Test bigint PRIMARY KEY
     *
     * Throws: Exception
     */

    //
    // SQL: create table alpha ( id BiGiNt not null PRIMARY KEY, beta timestamp)
    //
    //
    // Pattern XML:
    //.ELEMENT: databaseschema
    //...name = databaseschema
    //.[
    //.....ELEMENT: table
    //....|..name = ALPHA
    //.....[
    //....|....ELEMENT: columns
    //....|....|.name = columns
    //....|....[
    //....|....|...ELEMENT: column
    //....|....|.....index = 0
    //....|....|.....name = ID
    //....|....|.....nullable = false
    //....|....|.....size = 19
    //....|....|.....valuetype = BIGINT
    //....|....|...ELEMENT: column
    //....|....|.....index = 1
    //....|....|.....name = BETA
    //....|....|.....nullable = true
    //....|....|.....size = 8
    //....|....|.....valuetype = TIMESTAMP
    //....|....ELEMENT: indexes
    //....|....|.name = indexes
    //....|....[
    //....|....|...ELEMENT: index
    //....|....|.....assumeunique = false
    //....|....|.....columns = ID
    //....|....|.....ishashindex = false
    //....|....|.....name = VOLTDB_AUTOGEN_IDX_PK_ALPHA_ID
    //....|....|.....unique = true
    //....|....ELEMENT: constraints
    //....|....|.name = constraints
    //....|....[
    //....|....|...ELEMENT: constraint
    //....|....|.....assumeunique = false
    //....|....|.....constrainttype = PRIMARY_KEY
    //....|....|.....index = VOLTDB_AUTOGEN_IDX_PK_ALPHA_ID
    //....|....|.....name = VOLTDB_AUTOGEN_CT__PK_ALPHA_ID
    //....|....|.....nameisauto = true
    //....|....|.....rowslimit = 2147483647
    //....|....|...ELEMENT: constraint
    //....|....|.....assumeunique = false
    //....|....|.....constrainttype = NOT_NULL
    //....|....|.....name = SYS_CT_10001
    //....|....|.....nameisauto = true
    //....|....|.....rowslimit = 2147483647
    //
    //
    //
    @SuppressWarnings("unchecked")
    @Test
    public void testCreateTablePrimaryKey() throws Exception {
        String ddl    = "create table alpha ( id BiGiNt not null PRIMARY KEY, beta timestamp)";
        IDTable idTable = new IDTable();
        HSQLInterface hif = HSQLInterface.loadHsqldb(ParameterizationInfo.getParamStateManager());
        hif.processDDLStatementsUsingVoltSQLParser(ddl, null);
        VoltXMLElement element = hif.getVoltCatalogXML(null);
        assertThat(element)
            .hasName(1, "databaseschema")
            .hasAllOf(
                withAttribute(2, "name", "databaseschema"),
                withChildNamed(3, "table",
                    withAttribute(4, "name", "ALPHA"),
                    withChildNamed(5, "columns",
                        withAttribute(6, "name", "columns"),
                        withChildNamed(7, "column",
                                       "name", "ID",
                            withAttribute(8, "index", "0"),
                            withAttribute(9, "name", "ID"),
                            withAttribute(10, "nullable", "false"),
                            withAttribute(11, "size", "19"),
                            withAttribute(12, "valuetype", "BIGINT")),
                        withChildNamed(13, "column",
                                       "name", "BETA",
                            withAttribute(14, "index", "1"),
                            withAttribute(15, "name", "BETA"),
                            withAttribute(16, "nullable", "true"),
                            withAttribute(17, "size", "8"),
                            withAttribute(18, "valuetype", "TIMESTAMP"))),
                    withChildNamed(19, "indexes",
                        withAttribute(20, "name", "indexes")),
                    withChildNamed(21, "constraints",
                        withAttribute(22, "name", "constraints"))));
    }
    /**
     * Test bigint UNIQUE KEY
     *
     * Throws: Exception
     */

    //
    // SQL: create table alpha ( id BiGiNt not null UNIQUE, beta timestamp)
    //
    //
    // Pattern XML:
    //.ELEMENT: databaseschema
    //...name = databaseschema
    //.[
    //.....ELEMENT: table
    //....|..name = ALPHA
    //.....[
    //....|....ELEMENT: columns
    //....|....|.name = columns
    //....|....[
    //....|....|...ELEMENT: column
    //....|....|.....index = 0
    //....|....|.....name = ID
    //....|....|.....nullable = false
    //....|....|.....size = 19
    //....|....|.....valuetype = BIGINT
    //....|....|...ELEMENT: column
    //....|....|.....index = 1
    //....|....|.....name = BETA
    //....|....|.....nullable = true
    //....|....|.....size = 8
    //....|....|.....valuetype = TIMESTAMP
    //....|....ELEMENT: indexes
    //....|....|.name = indexes
    //....|....[
    //....|....|...ELEMENT: index
    //....|....|.....assumeunique = false
    //....|....|.....columns =
    //....|....|.....ishashindex = false
    //....|....|.....name = SYS_IDX_10003
    //....|....|.....unique = true
    //....|....|...ELEMENT: index
    //....|....|.....assumeunique = false
    //....|....|.....columns = ID
    //....|....|.....ishashindex = false
    //....|....|.....name = VOLTDB_AUTOGEN_IDX_CT_ALPHA_ID
    //....|....|.....unique = true
    //....|....ELEMENT: constraints
    //....|....|.name = constraints
    //....|....[
    //....|....|...ELEMENT: constraint
    //....|....|.....assumeunique = false
    //....|....|.....constrainttype = NOT_NULL
    //....|....|.....name = SYS_CT_10001
    //....|....|.....nameisauto = true
    //....|....|.....rowslimit = 2147483647
    //....|....|...ELEMENT: constraint
    //....|....|.....assumeunique = false
    //....|....|.....constrainttype = UNIQUE
    //....|....|.....index = VOLTDB_AUTOGEN_IDX_CT_ALPHA_ID
    //....|....|.....name = VOLTDB_AUTOGEN_CT__CT_ALPHA_ID
    //....|....|.....nameisauto = true
    //....|....|.....rowslimit = 2147483647
    //
    //
    //
    @SuppressWarnings("unchecked")
    @Test
    public void testCreateTableUniqueKey() throws Exception {
        String ddl    = "create table alpha ( id BiGiNt not null UNIQUE, beta timestamp)";
        IDTable idTable = new IDTable();
        HSQLInterface hif = HSQLInterface.loadHsqldb(ParameterizationInfo.getParamStateManager());
        hif.processDDLStatementsUsingVoltSQLParser(ddl, null);
        VoltXMLElement element = hif.getVoltCatalogXML(null);
        assertThat(element)
            .hasName(1, "databaseschema")
            .hasAllOf(
                withAttribute(2, "name", "databaseschema"),
                withChildNamed(3, "table",
                    withAttribute(4, "name", "ALPHA"),
                    withChildNamed(5, "columns",
                        withAttribute(6, "name", "columns"),
                        withChildNamed(7, "column",
                                       "name", "ID",
                            withAttribute(8, "index", "0"),
                            withAttribute(9, "name", "ID"),
                            withAttribute(10, "nullable", "false"),
                            withAttribute(11, "size", "19"),
                            withAttribute(12, "valuetype", "BIGINT")),
                        withChildNamed(13, "column",
                                       "name", "BETA",
                            withAttribute(14, "index", "1"),
                            withAttribute(15, "name", "BETA"),
                            withAttribute(16, "nullable", "true"),
                            withAttribute(17, "size", "8"),
                            withAttribute(18, "valuetype", "TIMESTAMP"))),
                    withChildNamed(19, "indexes",
                        withAttribute(20, "name", "indexes"),
                        withChildNamed(21, "index",
                                       "name", "SYS_IDX_10003",
                            withAttribute(22, "assumeunique", "false"),
                            withAttribute(23, "columns", ""),
                            withAttribute(24, "ishashindex", "false"),
                            withAttribute(25, "name", "SYS_IDX_10003"),
                            withAttribute(26, "unique", "true"))),
                    withChildNamed(27, "constraints",
                        withAttribute(28, "name", "constraints"))));
    }
    /**
     * Test bigint ASSUMEUNIQUE KEY
     *
     * Throws: Exception
     */

    //
    // SQL: create table alpha ( id BiGiNt not null ASSUMEUNIQUE, beta timestamp)
    //
    //
    // Pattern XML:
    //.ELEMENT: databaseschema
    //...name = databaseschema
    //.[
    //.....ELEMENT: table
    //....|..name = ALPHA
    //.....[
    //....|....ELEMENT: columns
    //....|....|.name = columns
    //....|....[
    //....|....|...ELEMENT: column
    //....|....|.....index = 0
    //....|....|.....name = ID
    //....|....|.....nullable = false
    //....|....|.....size = 19
    //....|....|.....valuetype = BIGINT
    //....|....|...ELEMENT: column
    //....|....|.....index = 1
    //....|....|.....name = BETA
    //....|....|.....nullable = true
    //....|....|.....size = 8
    //....|....|.....valuetype = TIMESTAMP
    //....|....ELEMENT: indexes
    //....|....|.name = indexes
    //....|....[
    //....|....|...ELEMENT: index
    //....|....|.....assumeunique = false
    //....|....|.....columns =
    //....|....|.....ishashindex = false
    //....|....|.....name = SYS_IDX_10003
    //....|....|.....unique = true
    //....|....|...ELEMENT: index
    //....|....|.....assumeunique = true
    //....|....|.....columns = ID
    //....|....|.....ishashindex = false
    //....|....|.....name = VOLTDB_AUTOGEN_IDX_CT_ALPHA_ID
    //....|....|.....unique = true
    //....|....ELEMENT: constraints
    //....|....|.name = constraints
    //....|....[
    //....|....|...ELEMENT: constraint
    //....|....|.....assumeunique = false
    //....|....|.....constrainttype = NOT_NULL
    //....|....|.....name = SYS_CT_10001
    //....|....|.....nameisauto = true
    //....|....|.....rowslimit = 2147483647
    //....|....|...ELEMENT: constraint
    //....|....|.....assumeunique = true
    //....|....|.....constrainttype = UNIQUE
    //....|....|.....index = VOLTDB_AUTOGEN_IDX_CT_ALPHA_ID
    //....|....|.....name = VOLTDB_AUTOGEN_CT__CT_ALPHA_ID
    //....|....|.....nameisauto = true
    //....|....|.....rowslimit = 2147483647
    //
    //
    //
    @SuppressWarnings("unchecked")
    @Test
    public void testCreateTableAssumeUniqueKey() throws Exception {
        String ddl    = "create table alpha ( id BiGiNt not null ASSUMEUNIQUE, beta timestamp)";
        IDTable idTable = new IDTable();
        HSQLInterface hif = HSQLInterface.loadHsqldb(ParameterizationInfo.getParamStateManager());
        hif.processDDLStatementsUsingVoltSQLParser(ddl, null);
        VoltXMLElement element = hif.getVoltCatalogXML(null);
        assertThat(element)
            .hasName(1, "databaseschema")
            .hasAllOf(
                withAttribute(2, "name", "databaseschema"),
                withChildNamed(3, "table",
                    withAttribute(4, "name", "ALPHA"),
                    withChildNamed(5, "columns",
                        withAttribute(6, "name", "columns"),
                        withChildNamed(7, "column",
                                       "name", "ID",
                            withAttribute(8, "index", "0"),
                            withAttribute(9, "name", "ID"),
                            withAttribute(10, "nullable", "false"),
                            withAttribute(11, "size", "19"),
                            withAttribute(12, "valuetype", "BIGINT")),
                        withChildNamed(13, "column",
                                       "name", "BETA",
                            withAttribute(14, "index", "1"),
                            withAttribute(15, "name", "BETA"),
                            withAttribute(16, "nullable", "true"),
                            withAttribute(17, "size", "8"),
                            withAttribute(18, "valuetype", "TIMESTAMP"))),
                    withChildNamed(19, "indexes",
                        withAttribute(20, "name", "indexes"),
                        withChildNamed(21, "index",
                                       "name", "SYS_IDX_10003",
                            withAttribute(22, "assumeunique", "false"),
                            withAttribute(23, "columns", ""),
                            withAttribute(24, "ishashindex", "false"),
                            withAttribute(25, "name", "SYS_IDX_10003"),
                            withAttribute(26, "unique", "true"))),
                    withChildNamed(27, "constraints",
                        withAttribute(28, "name", "constraints"))));
    }
    /**
     * Test bigint ASSUMEUNIQUE KEY
     *
     * Throws: Exception
     */

    //
    // SQL: create table alpha ( id BiGiNt default '100', beta timestamp)
    //
    //
    // Pattern XML:
    //.ELEMENT: databaseschema
    //...name = databaseschema
    //.[
    //.....ELEMENT: table
    //....|..name = ALPHA
    //.....[
    //....|....ELEMENT: columns
    //....|....|.name = columns
    //....|....[
    //....|....|...ELEMENT: column
    //....|....|.....index = 0
    //....|....|.....name = ID
    //....|....|.....nullable = true
    //....|....|.....size = 19
    //....|....|.....valuetype = BIGINT
    //....|....|...[
    //....|....|....|..ELEMENT: default
    //....|....|....|..[
    //....|....|....|....|.ELEMENT: value
    //....|....|....|....|...id = 1
    //....|....|....|....|...value = 100
    //....|....|....|....|...valuetype = BIGINT
    //....|....|...ELEMENT: column
    //....|....|.....index = 1
    //....|....|.....name = BETA
    //....|....|.....nullable = true
    //....|....|.....size = 8
    //....|....|.....valuetype = TIMESTAMP
    //....|....ELEMENT: indexes
    //....|....|.name = indexes
    //....|....[
    //....|....|...ELEMENT: index
    //....|....|.....assumeunique = false
    //....|....|.....columns =
    //....|....|.....ishashindex = false
    //....|....|.....name = SYS_IDX_10001
    //....|....|.....unique = true
    //....|....ELEMENT: constraints
    //....|....|.name = constraints
    //
    //
    //
    @SuppressWarnings("unchecked")
    @Test
    public void testCreateTableDefaultValue() throws Exception {
        String ddl    = "create table alpha ( id BiGiNt default '100', beta timestamp)";
        IDTable idTable = new IDTable();
        HSQLInterface hif = HSQLInterface.loadHsqldb(ParameterizationInfo.getParamStateManager());
        hif.processDDLStatementsUsingVoltSQLParser(ddl, null);
        VoltXMLElement element = hif.getVoltCatalogXML(null);
        assertThat(element)
            .hasName(1, "databaseschema")
            .hasAllOf(
                withAttribute(2, "name", "databaseschema"),
                withChildNamed(3, "table",
                    withAttribute(4, "name", "ALPHA"),
                    withChildNamed(5, "columns",
                        withAttribute(6, "name", "columns"),
                        withChildNamed(7, "column",
                                       "name", "ID",
                            withAttribute(8, "index", "0"),
                            withAttribute(9, "name", "ID"),
                            withAttribute(10, "nullable", "true"),
                            withAttribute(11, "size", "19"),
                            withAttribute(12, "valuetype", "BIGINT"),
                            withChildNamed(13, "default",
                                withChildNamed(14, "value",
                                    withIdAttribute(15, idTable),
                                    withAttribute(16, "value", "100"),
                                    withAttribute(17, "valuetype", "BIGINT")))),
                        withChildNamed(18, "column",
                                       "name", "BETA",
                            withAttribute(19, "index", "1"),
                            withAttribute(20, "name", "BETA"),
                            withAttribute(21, "nullable", "true"),
                            withAttribute(22, "size", "8"),
                            withAttribute(23, "valuetype", "TIMESTAMP"))),
                    withChildNamed(24, "indexes",
                        withAttribute(25, "name", "indexes"),
                        withChildNamed(26, "index",
                                       "name", "SYS_IDX_10001",
                            withAttribute(27, "assumeunique", "false"),
                            withAttribute(28, "columns", ""),
                            withAttribute(29, "ishashindex", "false"),
                            withAttribute(30, "name", "SYS_IDX_10001"),
                            withAttribute(31, "unique", "true"))),
                    withChildNamed(32, "constraints",
                        withAttribute(33, "name", "constraints"))));
    }
}
