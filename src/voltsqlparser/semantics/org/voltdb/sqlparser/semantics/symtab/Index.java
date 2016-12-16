package org.voltdb.sqlparser.semantics.symtab;

import org.voltdb.sqlparser.syntax.grammar.IIndex;
import org.voltdb.sqlparser.syntax.symtab.IndexType;

public class Index extends Top implements IIndex {
	IndexType m_indexType;
	
	public Index(String aName, IndexType it) {
		super(aName);
		m_indexType = it;
	}

}
