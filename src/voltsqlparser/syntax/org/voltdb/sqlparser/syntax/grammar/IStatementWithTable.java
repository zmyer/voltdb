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

import org.voltdb.sqlparser.syntax.symtab.ITable;

/**
 * Objects which implement this interface are statements which have a natural
 * associated table.  Examples are create table, create view and create index.
 */
public interface IStatementWithTable {
    /**
     * Set the table object itself.
     *
     * @param makeTable
     */
    void setTable(ITable makeTable);
    /**
     * Get the table object.
     * @return
     */
    ITable getTable();
}
