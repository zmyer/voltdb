package org.voltdb.sqlparser.syntax.symtab;

public interface ISourceLocation {
	public int getLineNumber();
	public int getColumnNumber();
}
