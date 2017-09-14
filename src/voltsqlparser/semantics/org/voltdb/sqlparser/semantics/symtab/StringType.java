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
package org.voltdb.sqlparser.semantics.symtab;

import org.voltdb.sqlparser.syntax.grammar.ISemantino;
import org.voltdb.sqlparser.syntax.grammar.IStringType;
import org.voltdb.sqlparser.syntax.symtab.ISourceLocation;
import org.voltdb.sqlparser.syntax.symtab.ITop;
import org.voltdb.sqlparser.syntax.symtab.TypeKind;

public class StringType extends Type implements ITop, IStringType {
    public StringType(ISourceLocation aLoc, String aName, TypeKind aKind) {
        super(aLoc, aName, aKind);
    }
    private StringType(ISourceLocation aLoc, String aName, TypeKind aKind, long aMaxSize) {
        super(aLoc, aName, aKind, aMaxSize);
    }
    /* (non-Javadoc)
     * @see org.voltdb.sqlparser.semantics.symtab.IStringType#makeInstance(long)
     */
    @Override
    public StringType makeInstance(ISourceLocation aLoc, long ...aSize) {
        assert(aSize.length == 1);
        StringType answer = new StringType(aLoc, getName(), getTypeKind(), aSize[0]);
        return answer;
    }
    @Override
    public ISemantino evalConstant(String aString) {
        // TODO Auto-generated method stub
        return null;
    }
}
