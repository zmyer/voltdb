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

import org.voltdb.sqlparser.syntax.symtab.ISourceLocation;
import org.voltdb.sqlparser.syntax.symtab.IType;
import org.voltdb.sqlparser.syntax.symtab.TypeKind;

/**
 * This is the base class of all types.  All types have a name,
 * a maximum size and a kind.
 *
 * @author bwhite
 *
 */
public class Type extends Top implements IType {
    public TypeKind m_kind;
    public long     m_maxSize;

    public Type(ISourceLocation aLoc, String aName, TypeKind aKind) {
        this(aLoc, aName, aKind, -1);
    }

    public Type(ISourceLocation aLoc, String aName, TypeKind aKind, long aMaxSize) {
        super(aLoc, aName);
        m_kind = aKind;
        m_maxSize = aMaxSize;
    }

    public boolean equals(Type other) {
        return (this.getName().equals(other.getName()));
    }

    public final Type getType() {
        return this;
    }

    public boolean isEqualType(Type rightType) {
        return m_kind == rightType.getTypeKind();
    }

    @Override
    public final TypeKind getTypeKind() {
        return m_kind;
    }

    public long getMaxSize() {
        return m_maxSize;
    }

    @Override
    public boolean isBooleanType() {
        return m_kind.isBoolean();
    }

    @Override
    public boolean isVoidType() {
        return m_kind.isVoid();
    }
    @Override
    public boolean isErrorType() {
        return false;
    }

    @Override
    public IType makeInstance(ISourceLocation aLoc, long ... params) {
        assert(false);
        return null;
    }

    @Override
    public boolean isFixedSize() {
        return true;
    }
    @Override
    public String toString() {
        if (0 <= m_kind.getSizeInBytes() || getMaxSize() < 0) {
            return getName();
        }
        return String.format("%s(%d)", getName(), getMaxSize());
    }
}
