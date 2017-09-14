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
 package org.voltdb.sqlparser.syntax.grammar;

import java.util.List;

/**
 * This holds all the parts of a select statement.
 *
 * As we state in the package javadoc comment, an ISelect
 * object has some parts.  As usual, we keep these things
 * here in order to keep the grammar and grammar actions
 * separate, to let the grammar be reusable.
 * <ul>
 *   <li>There is an expression parser.  Since we only build one
 *       expression at a time in a single query, we only need one parser.
 *       This expression parser keeps track of the expression stack.</li>
 *   <li>There are slots for saving all the parts of the statement.
 *       These are:
 *     <ul>
 *       <li>The display list, or select list.  This is a list of
 *           {@link ISemantino}s.</li>
 *       <li>The FROM list.  This is a list of {@link IJoinTree}s.  It
 *           could be a single {@link IJoinTree}, with the list elements
 *           joined with a cross join operator.</li>
 *       <li>There are filters, for where and having expressions.  These
 *           are {@link ISemantino}s whose type is boolean.  They may be null.</li>
 *       <li>There is a list of group by keys and another list of order by
 *           keys.  The elements of each of these is an {@link ISemantino}.
 *           These may both be null.</li>
 *       <li>An {@link ISelectQuery} also has a symbol table to which it may
 *           add definitions.
 *     </ul>
 *   </li>
 *   <li>The parser factory can create these.</li>
 * </ul>
 *
 * @author bwhite
 */
public interface ISelectQuery {
    /**
     * Return the display list.  This is sometimes called the
     * select list.
     *
     * @return The select list.  This is never null.
     */
    List<ISemantino> getDisplayList();
    /**
     * Return the list of join trees.  For example, in the statement
     * <p>
     * <pre>select * from T AS A, T AS B, T AS C;</pre>
     * </p>
     * the join tree list would have elements from three copies of
     * <code>T</code>, with aliases <code>A</code>, <code>B</code>
     * and <code>C</code>.
     *
     * @return The list of join trees.  This is never null.
     */
    List<IJoinTree>  getJoinTreeList();
    /**
     * Get the order by keys.
     * @return the order by keys.  This may be null.
     */
    List<ISemantino> getOrderByKeys();
    /**
     * Get the group by keys.
     * @return the group by keys.  This may be null.
     */
    List<ISemantino> getGroupByKeys();
    /**
     * Get the where filter.
     * @return the where filter.  This may be null.
     */
    ISemantino       getWhereFilter();
    /**
     * Get the having filter.
     * @return the having filter.  This may be null.
     */
    ISemantino       getHavingFilter();
}
