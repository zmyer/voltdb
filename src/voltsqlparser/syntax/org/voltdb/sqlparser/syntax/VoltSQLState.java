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
package org.voltdb.sqlparser.syntax;

import java.util.ArrayList;
import java.util.List;

import org.voltdb.sqlparser.syntax.grammar.ICatalogAdapter;
import org.voltdb.sqlparser.syntax.grammar.ICreateTableStatement;
import org.voltdb.sqlparser.syntax.grammar.ISQLStatement;
import org.voltdb.sqlparser.syntax.grammar.ISelectQuery;
import org.voltdb.sqlparser.syntax.grammar.ISemantino;
import org.voltdb.sqlparser.syntax.grammar.IStatementWithTable;
import org.voltdb.sqlparser.syntax.symtab.IColumn;
import org.voltdb.sqlparser.syntax.symtab.IParserFactory;
import org.voltdb.sqlparser.syntax.symtab.ISourceLocation;
import org.voltdb.sqlparser.syntax.symtab.ISymbolTable;
import org.voltdb.sqlparser.syntax.symtab.ITable;
import org.voltdb.sqlparser.syntax.symtab.IType;
import org.voltdb.sqlparser.syntax.symtab.IndexType;
import org.voltdb.sqlparser.syntax.util.ErrorMessage;
import org.voltdb.sqlparser.syntax.util.ErrorMessageSet;

/**
 * This class contains the parsing state.  The visitor class, which visits
 * different parsing nodes, stores all the state in this class.
 */
public class VoltSQLState {
    private List<ISQLStatement> m_stmtStack = new ArrayList<>();
    private ISymbolTable m_symbolTable;
    private IParserFactory m_ParserFactory;
    private ICatalogAdapter m_catalog;
    private ErrorMessageSet m_errorMessages = new ErrorMessageSet();


    public VoltSQLState(IParserFactory aFactory, ISymbolTable aSymbolTable) {
        m_symbolTable = aSymbolTable;
        m_ParserFactory = aFactory;
        m_catalog = aFactory.getCatalog();
    }

    public ICatalogAdapter getCatalogAdapter() {
        return m_catalog;
    }


    ////////////////////////////////////////////////////////////////////////
    //
    // Error messages.
    //
    ////////////////////////////////////////////////////////////////////////
    /**
     * Return true if there are any errors.
     *
     * @return true iff there are any errors.
     */
    public boolean hasErrors() {
        return m_errorMessages.numberErrors() > 0;
    }

    public ISourceLocation newSourceLocation(int aLineNumber, int aColumnNumber) {
        return m_ParserFactory.newSourceLocation(aLineNumber, aColumnNumber);
    }

    /**
     * Add an error message at the given source location.  The arguments
     * are like String.format.
     *
     * @param location
     * @param errorMessageFormat
     * @param args
     */
    public final void addError(ISourceLocation location, String errorMessageFormat, Object ... args) {
        m_errorMessages.addError(location, errorMessageFormat, args);
    }

    /**
     * Add a warning at the given source location.  The arguments
     * are like String.format.
     *
     * @param location
     * @param errorMessageFormat
     * @param args
     */
    public final void addWarning(ISourceLocation location, String errorMessageFormat, Object ... args) {
        m_errorMessages.addWarning(location, errorMessageFormat, args);
    }

    /**
     * Get all the error messages.
     *
     * @return the error messages.
     */
    public final ErrorMessageSet getErrorMessages() {
        return m_errorMessages;
    }

    /**
     * Make the error messages all one big string for logging.
     *
     * @return All error messages as a single string.
     */
    public String getErrorMessagesAsString() {
        StringBuffer sb = new StringBuffer();
        int nerrs = getErrorMessages().size();
        if (nerrs > 0) {
            sb.append(String.format("\nOh, dear, there seem%s to be %serror%s here.\n",
                                    nerrs > 1 ? "" : "s",
                                    nerrs > 1 ? "" : "an ",
                                    nerrs > 1 ? "s" : ""));
            for (ErrorMessage em : getErrorMessages()) {
                sb.append(String.format("line %d, column %d: %s: %s\n",
                                        em.getLine(),
                                        em.getCol(),
                                        em.getSeverity(),
                                        em.getMsg()));
            }
        }
        return sb.toString();
    }

    public ISQLStatement pushStatement(ISQLStatement aStmt) {
        m_stmtStack.add(aStmt);
        return aStmt;
    }

    public ISQLStatement topStatement(boolean doPop) {
        int idx = m_stmtStack.size();
        assert(idx > 0);
        return doPop ? m_stmtStack.remove(idx) : m_stmtStack.get(idx);
    }

    /**
     * Return a statement which has a table.
     * @param doPop
     * @return
     */
    public IStatementWithTable topStatementWithTable(boolean doPop) {
        ISQLStatement stmt = topStatement(false);
        return (stmt instanceof IStatementWithTable)
                ? (IStatementWithTable)stmt
                : null;
    }
    /**
     * Fetch or pop the top statement if it's an ICreateTableStatement.  Otherwise
     * return null.
     *
     * @param doPop If this is true then pop the top off.  Otherwise just peek at it.
     * @return The top element if it is an ICreateTableStatement.
     */
    public ICreateTableStatement topCreateTableStatement(boolean doPop) {
        ISQLStatement stmt = topStatement(false);
        return (stmt instanceof ICreateTableStatement) ? (ICreateTableStatement)topStatement(doPop) : null;
    }

    /**
     * Fetch or pop the top statement if it's an ISelectQuery.  Otherwise just return null.
     * @param doPop If this is true, then pop the top element off.  Otherwise, just peek at it.
     *
     * @return The top element if it's an ISelectQuery.  Otherwise just return null;
     */
    public ISelectQuery topSelectQuery(boolean doPop) {
        ISQLStatement stmt = topStatement(false);
        return (stmt instanceof ICreateTableStatement) ? (ISelectQuery)topStatement(doPop) : null;
    }

    ////////////////////////////////////////////////////////////////////////
    //
    // Expression Evaluation.
    //
    ////////////////////////////////////////////////////////////////////////
    /**
     * Evaluate the constant string, interpreted as the given type.
     * @param type
     * @param string
     * @return
     */
    ISemantino evalStringAsType(IType aType, String aString) {
        return aType.evalConstant(aString);
    }
    ////////////////////////////////////////////////////////////////////////
    //
    // Functions to make entities.
    //
    ////////////////////////////////////////////////////////////////////////
    ITable makeTable(ISourceLocation aLoc, String aTableName) {
        return m_ParserFactory.makeTable(aLoc, aTableName);
    }

    public void addTableToCatalog(ITable aTable) {
        m_catalog.addTable(aTable);
    }

    IColumn makeColumn(String aColumnName,
                       IType  aType,
                       ISemantino aDefaultValue,
                       boolean    aNotNull,
                       IndexType  aKeyType) {
        return m_ParserFactory.makeColumn(aColumnName, aType, aDefaultValue, aNotNull, aKeyType);
    }


    ////////////////////////////////////////////////////////////////////////
    //
    // Forward to the factory.
    //
    ////////////////////////////////////////////////////////////////////////
    public IType makeType(String aTypeName, String aParam0, String aParam1) {
        return m_ParserFactory.makeType(aTypeName, aParam0, aParam1);
    }
    ////////////////////////////////////////////////////////////////////////
    //
    // State accessors.
    //
    ////////////////////////////////////////////////////////////////////////
    public final ISymbolTable getSymbolTable() {
        return m_symbolTable;
    }

    public final IParserFactory getParserFactory() {
        return m_ParserFactory;
    }

    public final ICatalogAdapter getCatalog() {
        return m_catalog;
    }

}
