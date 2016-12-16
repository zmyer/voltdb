package org.voltdb.sqlparser.semantics.symtab;

import org.voltdb.sqlparser.syntax.symtab.ITop;
import org.voltdb.sqlparser.syntax.symtab.TypeKind;

public class GeographyPointType extends Type implements ITop {

	public GeographyPointType(String aName, TypeKind aKind) {
		super(aName, aKind);
	}

}
