package org.hsqldb_voltpatches;

import org.voltdb.sqlparser.semantics.symtab.ParserFactory;
import org.voltdb.sqlparser.syntax.VoltSQLState;
import org.voltdb.sqlparser.syntax.VoltSQLVisitor;

public class VoltDDLVisitor extends VoltSQLVisitor<VoltSQLState> {
    public VoltDDLVisitor(ParserFactory aFactory, VoltSQLState state) {
        super(state);
    }

    public boolean hasErrors() {
        return getState().hasErrors();
    }

    public String getErrorMessagesAsString() {
        return getState().getErrorMessagesAsString();
    }

    public VoltXMLElement getVoltXML() {
        return null;
    }

}
