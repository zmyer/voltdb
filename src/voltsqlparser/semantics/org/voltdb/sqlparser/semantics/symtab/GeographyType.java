package org.voltdb.sqlparser.semantics.symtab;

import org.voltdb.sqlparser.syntax.grammar.IGeographyType;
import org.voltdb.sqlparser.syntax.symtab.ISymbolTable;
import org.voltdb.sqlparser.syntax.symtab.ITop;
import org.voltdb.sqlparser.syntax.symtab.IType;
import org.voltdb.sqlparser.syntax.symtab.TypeKind;

public class GeographyType extends Type implements IGeographyType {

	private GeographyType(String aName, TypeKind aKind, long aMaxSize) {
		super(aName, aKind, aMaxSize);
	}
	public GeographyType(String aName, TypeKind aKind) {
		this(aName, aKind, -1);
	}
	
	@Override
	public IType makeInstance(long ... params) {
		if (params.length != 1) {
			return null;
		}
		return new GeographyType(getName(), getTypeKind(), params[0]);
	}

}
