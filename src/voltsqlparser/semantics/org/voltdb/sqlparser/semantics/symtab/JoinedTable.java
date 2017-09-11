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
package org.voltdb.sqlparser.semantics.symtab;

import org.voltdb.sqlparser.syntax.grammar.IJoinTree;
import org.voltdb.sqlparser.syntax.grammar.ISemantino;
import org.voltdb.sqlparser.syntax.grammar.JoinOperator;
import org.voltdb.sqlparser.syntax.symtab.ISourceLocation;

/**
 * This is a joined table.  It's constructed of applying a join
 * operator to two JoinTrees and a join condition.
 *
 * @author Bill White
 *
 */
public class JoinedTable extends JoinTree {
    JoinOperator m_joinOperator;
    JoinTree     m_left;
    JoinTree     m_right;
    Semantino    m_joinCondition;

    JoinedTable(ISourceLocation aLoc, String aName) {
        super(aLoc, aName);
    }

    @Override
    public boolean isTableReference() {
        return false;
    }

    @Override
    public JoinOperator getJoinOperator() {
        return m_joinOperator;
    }

    public final void setJoinOperator(JoinOperator aJoinOperator) {
        this.m_joinOperator = aJoinOperator;
    }

    @Override
    public IJoinTree getLeft() {
        return m_left;
    }

    public final void setLeft(JoinTree aLeft) {
        this.m_left = aLeft;
    }

    @Override
    public IJoinTree getRight() {
        return m_right;
    }

    public final void setRight(JoinTree aRight) {
        this.m_right = aRight;
    }

    @Override
    public ISemantino getJoinCondition() {
        return m_joinCondition;
    }

    public final void setJoinCondition(Semantino aJoinCondition) {
        this.m_joinCondition = aJoinCondition;
    }
}
