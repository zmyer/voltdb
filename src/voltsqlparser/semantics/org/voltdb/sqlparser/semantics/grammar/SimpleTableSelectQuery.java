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
package org.voltdb.sqlparser.semantics.grammar;

import java.util.ArrayList;
import java.util.List;

import org.voltdb.sqlparser.semantics.symtab.SymbolTable;
import org.voltdb.sqlparser.syntax.grammar.IDQLStatement;
import org.voltdb.sqlparser.syntax.grammar.IJoinTree;
import org.voltdb.sqlparser.syntax.grammar.ISelectQuery;
import org.voltdb.sqlparser.syntax.grammar.ISemantino;
import org.voltdb.sqlparser.syntax.grammar.Projection;
import org.voltdb.sqlparser.syntax.symtab.IParserFactory;
import org.voltdb.sqlparser.syntax.symtab.ISourceLocation;
import org.voltdb.sqlparser.syntax.util.ErrorMessageSet;



public class SimpleTableSelectQuery implements ISelectQuery, IDQLStatement {
    List<Projection> m_projections = new ArrayList<>();


    public SimpleTableSelectQuery(ISourceLocation aLoc,
                                  SymbolTable aParent,
                                  IParserFactory aFactory,
                                  ErrorMessageSet aErrorMessages) {
    }


    @Override
    public List<ISemantino> getDisplayList() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public List<IJoinTree> getJoinTreeList() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public List<ISemantino> getOrderByKeys() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public List<ISemantino> getGroupByKeys() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public ISemantino getWhereFilter() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public ISemantino getHavingFilter() {
        // TODO Auto-generated method stub
        return null;
    }
}
