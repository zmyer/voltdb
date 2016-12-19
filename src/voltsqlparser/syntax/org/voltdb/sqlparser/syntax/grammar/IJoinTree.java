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
package org.voltdb.sqlparser.syntax.grammar;

/**
 * An IJoinTree is either a table reference or a join of
 * two other join trees.  A table reference has a table name
 * and a table alias.  A join of two join trees has a join type,
 * INNER, RIGHTOUTER, LEFTOUTER, FULLOUTER or CROSS, and has
 * a non-null join condition.  The join condition is a boolean
 * expression.
 *
 * @author poppa
 *
 */
public interface IJoinTree {
    /**
     * True if this is a table reference, and false otherwise.
     * @return
     */
    boolean isTableReference();
    /**
     * If this is a table reference, then return the table name.
     * If it is not a table reference, return null.
     *
     * @return A table name or null;
     */
    String getTableName();
    /**
     * If this is a table reference, return the table alias name.
     * If it is not a table reference, return null.
     *
     * @return The table alias or null;
     */
    String getTableAlias();

    /**
     * If this is not a table reference, return the join operator.
     * If it is a table reference, return null.
     *
     * @return A join operator or null.
     */
    JoinOperator getJoinOperator();

    /**
     * If this is not a table reference, return the left join tree.
     * If it is a table reference, return null.
     *
     * @return The left join tree or null.
     */
    IJoinTree getLeft();

    /**
     * If this is not a table reference, return the right join tree.
     * If it is a table reference, return null.
     *
     * @return The left join tree or null.
     */
    IJoinTree getRight();

    /**
     * If this is not a table reference, return the join condition.
     * If it is a table reference return null.  Note that the
     * join condition is a semantino, since we never query its
     * structure here.  We only care about its type, and we don't
     * care much about that.
     *
     * @return A join condition or null.
     */
    ISemantino getJoinCondition();
}
