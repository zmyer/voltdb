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
 package org.voltdb.sqlparser.semantics.symtab;
// Uses arrayList, maybe j.u.* is too much.
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.voltdb.sqlparser.syntax.grammar.IIndex;
import org.voltdb.sqlparser.syntax.symtab.IColumn;
import org.voltdb.sqlparser.syntax.symtab.ISourceLocation;
import org.voltdb.sqlparser.syntax.symtab.ITable;

public class Table extends Top implements ITable {
    Map<String, Column>   m_lookupByName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    Map<Integer, Column > m_lookupByIndex = new HashMap<>();
    Map<String, IIndex> m_lookupIndexByName = new HashMap<>();

    public Table(ISourceLocation aLoc, String aTableName) {
        super(aLoc, aTableName);
    }

    /* (non-Javadoc)
     * @see org.voltdb.sqlparser.symtab.ITable#addColumn(java.lang.String, org.voltdb.sqlparser.symtab.Column)
     */
        @Override
    public void addColumn(String name, IColumn column) {
        assert(column instanceof Column);
        m_lookupByIndex.put(m_lookupByName.size(), (Column)column);
        m_lookupByName.put(name, (Column)column);
    }

    /* (non-Javadoc)
     * @see org.voltdb.sqlparser.symtab.ITable#toString()
     */
    @Override
    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append("create table ")
           .append(getName())
           .append(" (");
        String sep = " ";
        for (int idx = 0; idx < m_lookupByName.size(); idx += 1) {
            Column ic = m_lookupByIndex.get(idx);
            str.append(sep)
               .append(ic.getName())
               .append(" ")
               .append(ic.getType().toString());
            if ( ! ic.isNullable()) {
                str.append(" NOT NULL ");
            }
            if ( ic.hasDefaultValue() ) {
                str.append(ic.getDefaultValue());
            }
           sep = ", ";
        }
        str.append(");");
        return str.toString();
    }

    @Override
    public IColumn getColumnByName(String aName) {
        return m_lookupByName.get(aName);
    }

    @Override
    public IColumn getColumnByIndex(int index) {
        return m_lookupByIndex.get(index);
    }

    @Override
    public Set<String> getColumnNamesAsSet() {
        return m_lookupByName.keySet();
    }

    @Override
    public int getColumnCount() {
        return m_lookupByName.size();
    }

    @Override
    public void addIndex(String name, IIndex index) {
        assert(index instanceof Index);
        m_lookupIndexByName.put(name, index);
    }
}
