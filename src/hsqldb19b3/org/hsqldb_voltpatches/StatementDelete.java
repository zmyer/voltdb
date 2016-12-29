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

import java.util.List;

import org.hsqldb_voltpatches.HSQLInterface.HSQLParseException;
import org.hsqldb_voltpatches.ParserDQL.CompileContext;
import org.hsqldb_voltpatches.lib.HashMappedList;
import org.hsqldb_voltpatches.lib.HashSet;
import org.hsqldb_voltpatches.lib.HsqlArrayList;
import org.hsqldb_voltpatches.lib.HsqlList;
import org.hsqldb_voltpatches.lib.OrderedHashSet;
import org.hsqldb_voltpatches.navigator.RangeIterator;
import org.hsqldb_voltpatches.navigator.RowSetNavigator;
import org.hsqldb_voltpatches.navigator.RowSetNavigatorLinkedList;
import org.hsqldb_voltpatches.result.Result;
import org.hsqldb_voltpatches.result.ResultMetaData;
import org.hsqldb_voltpatches.rights.GrantConstants;
import org.hsqldb_voltpatches.rights.Grantee;

public class StatementDelete extends StatementDML {

    private final SortAndSlice m_sortAndSlice;
    private final boolean m_restartIdentity;

    StatementDelete(Session session, Table targetTable,
            RangeVariable[] rangeVars, CompileContext compileContext,
            SortAndSlice sas, boolean restartIdentity) {
        super(session, targetTable, rangeVars, compileContext);
        m_sortAndSlice = sas;
        m_restartIdentity = restartIdentity;

        resolveSortAndSlice(session);
    }

    /**
     * Executes a DELETE statement.  It is assumed that the argument is
     * of the correct type.
     *
     * @return the result of executing the statement
     */
    @Override
    Result getResult(Session session) {
        RowSetNavigatorLinkedList oldRows = new RowSetNavigatorLinkedList();
        RangeIterator it = RangeVariable.getIterator(session,
            targetRangeVariables);
        while (it.next()) {
            Row currentRow = it.getCurrentRow();
            oldRows.add(currentRow);
        }

        int count = delete(session, baseTable, oldRows);

        if (m_restartIdentity && targetTable.identitySequence != null) {
            targetTable.identitySequence.reset();
        }

        return Result.getUpdateCountResult(count);
    }

    // fredt - currently deletes that fail due to referential constraints are caught
    // prior to actual delete operation

    /**
     *  Highest level multiple row delete method. Corresponds to an SQL
     *  DELETE.
     */
    private static int delete(Session session, Table table, RowSetNavigator oldRows) {

        if (table.fkMainConstraints.length == 0) {
            deleteRows(session, table, oldRows);
            oldRows.beforeFirst();

            if (table.hasTrigger(Trigger.DELETE_AFTER)) {
                table.fireAfterTriggers(session, Trigger.DELETE_AFTER,
                                        oldRows);
            }

            return oldRows.getSize();
        }

        HashSet path = session.sessionContext.getConstraintPath();
        HashMappedList tableUpdateList =
            session.sessionContext.getTableUpdateList();

        if (session.database.isReferentialIntegrity()) {
            oldRows.beforeFirst();

            while (oldRows.hasNext()) {
                oldRows.next();

                Row row = oldRows.getCurrentRow();

                path.clear();
                checkCascadeDelete(session, table, tableUpdateList, row,
                                   false, path);
            }
        }

        if (session.database.isReferentialIntegrity()) {
            oldRows.beforeFirst();

            while (oldRows.hasNext()) {
                oldRows.next();

                Row row = oldRows.getCurrentRow();

                path.clear();
                checkCascadeDelete(session, table, tableUpdateList, row, true,
                                   path);
            }
        }

        oldRows.beforeFirst();

        while (oldRows.hasNext()) {
            oldRows.next();

            Row row = oldRows.getCurrentRow();

            if (!row.isDeleted(session)) {
                table.deleteNoRefCheck(session, row);
            }
        }

        for (int i = 0; i < tableUpdateList.size(); i++) {
            Table targetTable = (Table) tableUpdateList.getKey(i);
            HashMappedList updateList =
                (HashMappedList) tableUpdateList.get(i);

            if (updateList.size() > 0) {
                targetTable.updateRowSet(session, updateList, null, true);
                updateList.clear();
            }
        }

        oldRows.beforeFirst();

        if (table.hasTrigger(Trigger.DELETE_AFTER)) {
            table.fireAfterTriggers(session, Trigger.DELETE_AFTER, oldRows);
        }

        path.clear();

        return oldRows.getSize();
    }

    private static void deleteRows(Session session, Table table, RowSetNavigator oldRows) {
        while (oldRows.hasNext()) {
            oldRows.next();
            Row row = oldRows.getCurrentRow();
            if (!row.isDeleted(session)) {
                table.deleteNoRefCheck(session, row);
            }
        }
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
        VoltXMLElement xml;
            xml = new VoltXMLElement("delete");
            // DELETE has no target columns
            voltAppendChildScans(session, xml);
            voltAppendCondition(session, xml);
            voltAppendSortAndSlice(session, xml);

        voltAppendParameters(session, xml, parameters);
        xml.attributes.put("table", targetTable.getName().name);
        return xml;
    }

    /**
     * Appends XML for ORDER BY/LIMIT/OFFSET to this statement's XML.
     * */
    void voltAppendSortAndSlice(Session session, VoltXMLElement xml)
            throws HSQLParseException {
        if (m_sortAndSlice == null || m_sortAndSlice == SortAndSlice.noSort) {
            return;
        }

        // Is target a view?
        if (targetTable.getBaseTable() != targetTable) {
            // This check is unreachable, but if writable views are ever supported there
            // will be some more work to do to resolve columns in ORDER BY properly.
            throw new HSQLParseException("DELETE with ORDER BY, LIMIT or OFFSET is currently unsupported on views.");
        }

        if (m_sortAndSlice.hasLimit() && !m_sortAndSlice.hasOrder()) {
            throw new HSQLParseException("DELETE statement with LIMIT or OFFSET but no ORDER BY would produce "
                    + "non-deterministic results.  Please use an ORDER BY clause.");
        }
        else if (m_sortAndSlice.hasOrder() && !m_sortAndSlice.hasLimit()) {
            // This is harmless, but the order by is meaningless in this case.  Should
            // we let this slide?
            throw new HSQLParseException("DELETE statement with ORDER BY but no LIMIT or OFFSET is not allowed.  "
                    + "Consider removing the ORDER BY clause, as it has no effect here.");
        }

        List<VoltXMLElement> newElements = voltGetLimitOffsetXMLFromSortAndSlice(session, m_sortAndSlice);

        // This code isn't shared with how SELECT's ORDER BY clauses are serialized since there's
        // some extra work that goes on there to handle references to SELECT clauses aliases, etc.
        HsqlArrayList exprList = m_sortAndSlice.exprList;
        if (exprList != null) {
            VoltXMLElement orderColumnsXml = new VoltXMLElement("ordercolumns");
            for (int i = 0; i < exprList.size(); ++i) {
                Expression e = (Expression)exprList.get(i);
                VoltXMLElement elem = e.voltGetXML(session);
                orderColumnsXml.children.add(elem);
            }

            newElements.add(orderColumnsXml);
        }

        xml.children.addAll(newElements);
    }

    @Override
    void getTriggerTableNames(OrderedHashSet set, boolean write) {
        for (TriggerDef td : baseTable.triggerList) {
            if (td.getPrivilegeType() != GrantConstants.DELETE) {
                continue;
            }

            for (Statement statement : td.statements) {
                set.addAll(statement.getTableNamesForRead());
            }
        }
    }

    @Override
    void checkSpecificAccessRights(Grantee grantee) {
        grantee.checkDelete(targetTable);
    }

    /**
     * Provides the toString() implementation.
     */
    @Override
    String describeImpl(Session session) throws Exception {
        StringBuffer sb = new StringBuffer("DELETE[\n");
        appendTable(sb).append('\n');
        appendCondition(session, sb);
        for (RangeVariable trv : targetRangeVariables) {
            sb.append(trv.describe(session)).append('\n');
        }
        appendParams(sb).append('\n');
        appendSubqueries(session, sb).append(']');
        return sb.toString();
    }

    /**
     * Returns the metadata, which is empty if the CompiledStatement does not
     * generate a Result.
     */
    @Override
    public ResultMetaData getResultMetaData() {
        return ResultMetaData.emptyResultMetaData;
    }

    private void resolveSortAndSlice(Session session) {
        assert(m_sortAndSlice != null);

        if (m_sortAndSlice == SortAndSlice.noSort) {
            return;
        }

        // Resolve columns in the ORDER BY clause.
        // This code copied how compileDelete resolves columns
        // in its WHERE clause.
        for (int i = 0; i < m_sortAndSlice.exprList.size(); ++i) {
            Expression expr = (Expression) m_sortAndSlice.exprList.get(i);
            HsqlList unresolved =
                expr.resolveColumnReferences(RangeVariable.emptyArray, null);

            unresolved = Expression.resolveColumnSet(rangeVariables,
                    unresolved, null);

            ExpressionColumn.checkColumnsResolved(unresolved);
            expr.resolveTypes(session, null);
        }
    }
}
