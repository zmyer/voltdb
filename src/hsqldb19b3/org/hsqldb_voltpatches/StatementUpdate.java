/* Copyright (c) 2001-2009, The HSQL Development Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the HSQL Development Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL HSQL DEVELOPMENT GROUP, HSQLDB.ORG,
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hsqldb_voltpatches;

import org.hsqldb_voltpatches.HSQLInterface.HSQLParseException;
import org.hsqldb_voltpatches.ParserDQL.CompileContext;
import org.hsqldb_voltpatches.RangeVariable.RangeIteratorBase;
import org.hsqldb_voltpatches.lib.HashMappedList;
import org.hsqldb_voltpatches.lib.OrderedHashSet;
import org.hsqldb_voltpatches.result.Result;
import org.hsqldb_voltpatches.result.ResultMetaData;
import org.hsqldb_voltpatches.rights.GrantConstants;
import org.hsqldb_voltpatches.rights.Grantee;
import org.hsqldb_voltpatches.types.Type;

public class StatementUpdate extends StatementDML {

    public StatementUpdate(Session session, Table targetTable,
            RangeVariable[] rangeVars, int[] updateColumnMap,
            Expression[] colExpressions, boolean[] checkColumns,
            CompileContext compileContext) {
        super(session, targetTable, rangeVars, updateColumnMap,
                colExpressions, checkColumns, compileContext);
    }

    @Override
    Result getResult(Session session) {
        return executeUpdateStatement(session);
    }

    /**
     * Executes an UPDATE statement.  It is assumed that the argument
     * is of the correct type.
     *
     * @return the result of executing the statement
     */
    Result executeUpdateStatement(Session session) {

        int            count          = 0;
        Expression[]   colExpressions = updateExpressions;
        HashMappedList rowset         = new HashMappedList();
        Type[]         colTypes       = baseTable.getColumnTypes();
        RangeIteratorBase it = RangeVariable.getIterator(session,
            targetRangeVariables);
        Expression checkCondition = null;

        if (targetTable != baseTable) {
            checkCondition =
                ((TableDerived) targetTable).getQueryExpression()
                    .getMainSelect().checkQueryCondition;
        }

        while (it.next()) {
            session.sessionData.startRowProcessing();

            Row      row  = it.getCurrentRow();
            Object[] data = row.getData();
            Object[] newData = getUpdatedData(session, baseTable,
                                              updateColumnMap, colExpressions,
                                              colTypes, data);

            if (checkCondition != null) {
                it.currentData = newData;

                boolean check = checkCondition.testCondition(session);

                if (!check) {
                    throw Error.error(ErrorCode.X_44000);
                }
            }

            rowset.add(row, newData);
        }

/* debug 190
        if (rowset.size() == 0) {
            System.out.println(targetTable.getName().name + " zero update: session "
                               + session.getId());
        } else if (rowset.size() >1) {
           System.out.println("multiple update: session "
                              + session.getId() + ", " + rowset.size());
       }

//* debug 190 */
        count = update(session, baseTable, rowset);

        return Result.getUpdateCountResult(count);
    }

    @Override
    void getTriggerTableNames(OrderedHashSet set, boolean write) {
        for (TriggerDef td : baseTable.triggerList) {
            if (td.getPrivilegeType() != GrantConstants.UPDATE) {
                continue;
            }

            for (Statement statement : td.statements) {
                set.addAll(statement.getTableNamesForRead());
            }
        }
    }

    @Override
    void checkSpecificAccessRights(Grantee grantee) {
        grantee.checkUpdate(targetTable, updateCheckColumns);
    }

    /**
     * Provides the toString() implementation.
     */
    @Override
    String describeImpl(Session session) throws Exception {
        StringBuffer sb = new StringBuffer("UPDATE[\n");
        appendColumns(sb, updateColumnMap).append('\n');
        appendTable(sb).append('\n');
        appendCondition(session, sb);
        for (RangeVariable trv : targetRangeVariables) {
            sb.append(trv.describe(session)).append('\n');
        }
        appendParams(sb).append('\n');
        appendSubqueries(session, sb).append(']');
        return sb.toString();
    }

    StringBuffer appendColumns(StringBuffer sb, int[] columnMap) {
        if (columnMap == null || updateExpressions == null) {
            return sb;
        }

        sb.append("COLUMNS=[");

        for (int i = 0; i < columnMap.length; i++) {
            sb.append('\n').append(columnMap[i]).append(':').append(
                ' ').append(
                targetTable.getColumn(columnMap[i]).getNameString()).append(
                '[').append(updateExpressions[i]).append(']');
        }

        sb.append(']');

        return sb;
    }

    /**
     * Returns the metadata, which is empty if the CompiledStatement does not
     * generate a Result.
     */
    @Override
    public ResultMetaData getResultMetaData() {
        switch (type) {
            case StatementTypes.INSERT :
            case StatementTypes.UPDATE_WHERE :
                return ResultMetaData.emptyResultMetaData;

            default :
                throw Error.runtimeError(
                    ErrorCode.U_S0500,
                    "CompiledStatement.getResultMetaData()");
        }
    }

    @Override
    boolean[] getInsertOrUpdateColumnCheckList() {
        return updateCheckColumns;
    }

    /**
     * VoltDB added method to get a non-catalog-dependent
     * representation of this HSQLDB object.
     * @param session The current Session object may be needed to resolve
     * some names.
     * @return XML, correctly indented, representing this object.
     * @throws HSQLParseException
     */
    @Override
    VoltXMLElement voltGetStatementXML(Session session)
            throws HSQLParseException {
        VoltXMLElement xml = new VoltXMLElement("update");
        voltAppendTargetColumns(session, updateColumnMap, updateExpressions, xml);
        voltAppendChildScans(session, xml);
        voltAppendCondition(session, xml);

        voltAppendParameters(session, xml, parameters);
        xml.attributes.put("table", targetTable.getName().name);
        return xml;
    }

 }
