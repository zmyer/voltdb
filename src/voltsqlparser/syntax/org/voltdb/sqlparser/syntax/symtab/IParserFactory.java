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
/* This file is part of VoltDB.
 * Copyright (C) 2008-2016 VoltDB Inc.
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
/* This file is part of VoltDB.
 * Copyright (C) 2008-2015 VoltDB Inc.
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
 package org.voltdb.sqlparser.syntax.symtab;


import java.util.List;

import org.voltdb.sqlparser.syntax.grammar.ICatalogAdapter;
import org.voltdb.sqlparser.syntax.grammar.IColumnIdent;
import org.voltdb.sqlparser.syntax.grammar.ICreateTableStatement;
import org.voltdb.sqlparser.syntax.grammar.IIndex;
import org.voltdb.sqlparser.syntax.grammar.IInsertStatement;
import org.voltdb.sqlparser.syntax.grammar.IJoinTree;
import org.voltdb.sqlparser.syntax.grammar.IOperator;
import org.voltdb.sqlparser.syntax.grammar.ISelectQuery;
import org.voltdb.sqlparser.syntax.grammar.ISemantino;
import org.voltdb.sqlparser.syntax.grammar.JoinOperator;
import org.voltdb.sqlparser.syntax.grammar.Projection;
import org.voltdb.sqlparser.syntax.grammar.QuerySetOp;
import org.voltdb.sqlparser.syntax.util.ErrorMessageSet;

/**
 * This interface represents the operations the parser needs to create
 * its abstract intermediate representation, or IAST.  The operations are
 * in three categories:
 * <ol>
 *   <li>Constants needed at initialization from the environment</li>
 *   <li>Functions needed to implement semantic objects for statements.</li>
 *   <li>Functions needed to construct abstract AST objects.</li>
 * </ol>
 *
 * @author bwhite
 *
 */
public interface IParserFactory {
    ////////////////////////////////////////////////////////////////////////
    //
    // Environment operations
    //
    ////////////////////////////////////////////////////////////////////////
    /**
     * The standard prelude is the declarations of all names
     * which are built-in by the language.  Examples of these are
     * the types, INTEGER, BOOLEAN and so forth, and the
     * functions.
     * @return A new, initial ISymboTable object.
     */
    ISymbolTable getStandardPrelude();
    /**
     * A Catalog, from VoltDB, represents the schema.  It contains a set of
     * Tables and Indices which have already been defined.  A Catalog Adapter
     * transforms this catalog into something we can use.
     *
     * @return The current catalog.
     */
    ICatalogAdapter getCatalog();

    /**
     * Create a new empty table.  The columns will be filled in as the
     * parser finds them.
     *
     * @param aTableName
     * @return
     */
    ITable makeTable(ISourceLocation aSourceLocation, String aTableName);

    /**
     * Create a new simple table select query object.  This is a complicated
     * object which reads some projections, some tables, some join conditions,
     * group by and having clauses, then processes projections.
     *
     * @param aSymbolTable
     * @param aColNo
     * @param aLineNo
     * @return
     */
    ISelectQuery newSimpleTableSelectQuery(ISourceLocation aLoc,
                                           ISymbolTable aSymbolTable,
                                           IParserFactory aFactory,
                                           ErrorMessageSet aErrorMessages);

    /**
     * Create a new compound query object.  This is a boolean combination
     * of smaller queries.
     * @param op
     * @param left
     * @param right
     * @return
     */
    ISelectQuery newCompoundQuery(QuerySetOp op, ISelectQuery left, ISelectQuery right);
    /**
     * Once all the parts of a select statement have been seen,
     * type checked and processed, this is called to tie all the
     * bits together.  Typically this will create the final IAST
     * for the select statement.
     *
     * @param aSelectQuery
     */
    void processQuery(ISelectQuery aSelectQuery);

    /**
     * Create a new empty insert statement.  The table name,
     * column names and values will be pushed in later as the
     * parser sees them.
     *
     * @return
     */
    IInsertStatement newInsertStatement();

    /**
     * Make an IAST object for a constant.
     *
     * @param aIntType
     * @param aIntegerValue
     * @return
     */
    IAST makeConstantAST(IType aIntType, Object aIntegerValue);

    /**
     * Make an IAST object for a boolean constant.  The particular
     * boolean type is given.  There might be more than one boolean
     * type, who knows?
     *
     * @param aIntType
     * @param aBoolValue
     * @return
     */
    IAST makeUnaryAST(IType aIntType, boolean aBoolValue);

    /**
     * Given the names of a column, the table name, table alias
     * and column name, return an abstract syntax tree representing
     * the computation of the column's value.
     *
     * @param aRealTableName
     * @param aTableAlias
     * @param aColumnName
     * @return
     */
    IAST makeColumnRef(String aRealTableName,
                       String aTableAlias,
                       String aColumnName);

    /**
     * Make an IAST object representing the application of an operator,
     * such as +, -, *, /, the relational operators and the boolean
     * operators to operands.  The upper layers have arranged it so that
     * the types are all correct.  The return type has been computed by
     * the upper layers as well, so we don't need to return it here.
     *
     * @param aOp
     * @param aLeftoperand
     * @param aRightoperand
     * @return
     */
    IAST makeBinaryAST(IOperator aOp, ISemantino aLeftoperand, ISemantino aRightoperand);

    /**
     * Given a string, such as "+", look up the abstract operator for
     * that string.  The target may define its own operators, such as
     * "&&", "||", "%" or "^".
     *
     * @param aText
     * @return
     */
    IOperator getExpressionOperator(String aText);

    /**
     * Given an IAST node N of type aSrcType, return an IAST node which
     * represents the conversion of N to type aTrgType.
     *
     * @param aNode
     * @param aSrcType
     * @param aTrgType
     * @return
     */
    IAST addTypeConversion(IAST aNode, IType aSrcType, IType aTrgType);

    /**
     * Given two semantinos, both operators, return a pair of semantinos
     * which represent expressions converted to a common type.
     *
     * @param aLeftoperand
     * @param aRightoperand
     * @return
     */
    ISemantino[] tuac(ISemantino aLeftoperand, ISemantino aRightoperand, ISymbolTable aSymbolTable);

    /**
     * Given a the pieces of a query, return the abstract syntax tree
     * for the query.
     *
     * @param aProjections
     * @param aWhereCondition
     * @param aTables
     * @return
     */
    IAST makeQueryAST(List<Projection> aProjections,
                      IAST aWhereCondition,
                      ISymbolTable aTables);
    /**
     * Make an AST for in insert statement.
     *
     * @param aInsertStatement
     * @return
     */
    IAST makeInsertAST(IInsertStatement aInsertStatement);
    /**
     * Get the set of error messages for this factory.
     * @return
     */
    ErrorMessageSet getErrorMessages();

    /**
     * Get the Boolean type.
     * @return
     */
    IType getBooleanType();
    /**
     * Get the error type.  This is used mostly when a parsing
     * error needs a type, but they type does not need to be anything
     * specific.
     */
    IType getErrorType();
    /**
     * Make a new reference to a column with the given name.  The
     * reference is at the given column and line number.
     *
     * @param colName
     * @param colLineNo
     * @param colColNo
     * @return
     */
    IColumnIdent makeColumnRef(String colName,
                               int colLineNo,
                               int colColNo);

    /**
     * When handling errors, we sometimes need a Semantino to represent
     * a value which is not in evidence.
     *
     * @return
     */
    ISemantino getErrorSemantino();

    /**
     * Make a unary IAST for a Semantino.
     * @param type
     * @param aOperator
     * @param aOperand
     * @return
     */
    IAST makeUnaryAST(IType type, IOperator aOperator, ISemantino aOperand);
    /**
     * Make a Semantino for a query statement.
     *
     * @param query The query.
     * @return
     */
    ISemantino makeQuerySemantino(ISelectQuery query);

    /**
     * Make a new index with the given name, on the given
     * table, column and index type.
     *
     * @param table The table of the index.
     * @param column The column of the index
     * @param m_isUniqueConstraint UNIQUE is specified.
     * @param m_isAssumedUnique ASSUMEUNIQUE is specified.
     * @param m_isPrimaryKey PRIMARY KEY is specified.
     * @return A new index.
     */
    IIndex newIndex(ISourceLocation aLoc,
                    String indexName,
                    ITable table,
                    IColumn column,
                    IndexType it);
    /**
     * Combine two join trees with a join operation and a join condition.
     *
     * @param op The operation.
     * @param left The left subtree.
     * @param right The right subtree.
     * @param condition The join condition.
     * @return
     */
    IJoinTree newJoinTree(JoinOperator op, IJoinTree joinTree, IJoinTree right, ISemantino condition);

    /**
     * Make a new table reference.
     *
     * @param aTableName The table name.
     * @param aTableAlias The table alias.
     */
    IJoinTree newTableReference(String aTableName, String aTableAlias);
    /**
     * Make a new derived table from a select query.
     * @param derivedTable
     * @param text
     * @return
     */
    IJoinTree newDerivedJoinTree(ISelectQuery derivedTable, String tableName);
    /**
     * Make a new location.
     */
    ISourceLocation newSourceLocation(int aLineNumber, int aColumnNumber);
    /**
     * Make a new column reference to the named column.
     *
     * @param colName
     * @param newSourceLocation
     * @return
     */
    IColumnIdent makeColumnRef(String colName, ISourceLocation newSourceLocation);
    /**
     * Make a new ICreateTable object.
     *
     * @return The new object.
     */
    ICreateTableStatement makeCreateTableStatement();
    /**
     * Lookup or create a type with the given name and parameters.
     *
     * @param upperCase
     * @param v0
     * @param v1
     * @return
     */
    IType makeType(String upperCase, String v0, String v1);
    /**
     * Make a new Column with the given attributes.  Don't insert
     * it into anything, just return it.
     *
     * @param aColumnName The column name.
     * @param aType The column type.
     * @param aDefaultValue The Semantino for the default value.
     * @param aNotNull Whether the column can be null.
     * @param aKeyType What kind of key this column is, if it is a key.
     * @return
     */
    IColumn makeColumn(String aColumnName,
                       IType aType,
                       ISemantino aDefaultValue,
                       boolean aNotNull,
                       IndexType aKeyType);
}
