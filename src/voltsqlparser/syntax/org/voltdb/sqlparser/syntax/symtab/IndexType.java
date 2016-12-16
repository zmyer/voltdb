package org.voltdb.sqlparser.syntax.symtab;

public enum IndexType {
	INVALID("INVALID"),
	UNIQUE_KEY("UNIQUE"),
	ASSUMED_UNIQUE_KEY("ASSUMEDUNIQUE"),
	PRIMARY_KEY("PRIMARY KEY");

	private IndexType(String syntaxName) {
		m_syntaxName = syntaxName;
	}
	
	private String m_syntaxName;

	public String syntaxName() {
		return m_syntaxName;
	}
}
