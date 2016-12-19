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
 package org.voltdb.sqlparser.syntax.grammar;

import java.util.List;

import org.voltdb.sqlparser.syntax.symtab.IAST;
import org.voltdb.sqlparser.syntax.symtab.IExpressionParser;
import org.voltdb.sqlparser.syntax.symtab.ISymbolTable;
import org.voltdb.sqlparser.syntax.symtab.ITable;
import org.voltdb.sqlparser.syntax.symtab.IType;

/**
 * This holds all the parts of a select statement.
 *
 * @author bwhite
 */
public interface ISelectQuery {

    /**
     * True iff this is a simple table.  Simple tables
     * are select statements, with no boolean operations.
     * @return
     */
    boolean isSimpleTable();

    /**
     * If this is a not a simple table, it's the
     * combination of two other select queries.  Return
     * the set operation combining them.  It's an error
     * if this is called on a simple table query.
     */
    QuerySetOp getSetOp() throws Exception;

    /**
     * Return the left hand query of a compound query.
     *
     * @return
     */
    ISelectQuery getLeftQuery();
    /**
     * Return the right hand query of a compound query.
     * @return
     */
    ISelectQuery getRightQuery();

    /**
     * Add a projection.  This is a select list element.
     *
     * @param aTableName
     * @param aColumnName
     * @param aAlias
     * @param aLineNo
     * @param aColNo
     */
    void addProjection(String aTableName, String aColumnName, String aAlias, int aLineNo, int aColNo);

    /**
     * Add a projection.
     * @param aLineNo
     * @param aColNo
     */
    void addProjection(int aLineNo, int aColNo);

    void pushSemantino(ISemantino aColumnSemantino);

    ISemantino popSemantino();

    String printProjections();

    void addTable(ITable aITable, String aAlias);

    String printTables();

    boolean hasSemantinos();

    ISemantino getColumnSemantino(String aColumnName, String aTableName);

    ISemantino getConstantSemantino(Object value, IType type);

    ISemantino getSemantinoMath(IOperator aOperator, ISemantino aLeftoperand,
            ISemantino aRightoperand);

    ISemantino getSemantinoCompare(IOperator aOperator, ISemantino aLeftoperand,
            ISemantino aRightoperand);

    ISemantino getSemantinoBoolean(IOperator aOperator, ISemantino aLeftoperand,
            ISemantino aRightoperand);

    List<Projection> getProjections();

    void setWhereCondition(ISemantino aRet);

    IAST getWhereCondition();

    ISymbolTable getTables();

    void setAST(IAST aMakeQueryAST);

    boolean validate();

    IExpressionParser getExpressionParser();

    void setExpressionParser(IExpressionParser expr);

    /**
     * Add a join condition to the select query.
     * @param joinTree
     */
    void addJoinTree(IJoinTree joinTree);
}
