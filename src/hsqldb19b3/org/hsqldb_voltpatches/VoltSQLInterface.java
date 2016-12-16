package org.hsqldb_voltpatches;

import java.io.IOException;

import org.hsqldb_voltpatches.HSQLInterface.HSQLParseException;
import org.voltdb.sqlparser.semantics.symtab.CatalogAdapter;
import org.voltdb.sqlparser.syntax.SQLKind;
import org.voltdb.sqlparser.syntax.SQLParserDriver;
import org.voltdb.sqlparser.syntax.symtab.IColumn;
import org.voltdb.sqlparser.semantics.symtab.CatalogAdapter;
import org.voltdb.sqlparser.semantics.symtab.Column;

public class VoltSQLInterface {
    public static final String XML_SCHEMA_NAME = "databaseschema";

    /**
     * Process a hunk of DDL.  If aAdapter is null, use the adapter in
     * this HSQLInterface, which is the catalog of the existing database.
     *
     * @param sql
     * @param aAdapter
     * @throws HSQLParseException
     */
    public void processDDLStatementsUsingVoltSQLParser(String sql, CatalogAdapter aAdapter) throws HSQLParseException {
        CatalogAdapter adapter = (aAdapter == null) ? m_catalogAdapter : aAdapter;
        VoltParserFactory factory = new VoltParserFactory(adapter);
        VoltDDLVisitor visitor = new VoltDDLVisitor(factory, null);
        processSQLWithListener(sql, visitor, SQLKind.DDL);
    }

    public void processSQLWithListener(String aSQL, VoltDDLVisitor aVisitor, SQLKind aKind) throws HSQLParseException {
        SQLParserDriver driver;
        try {
            driver = new SQLParserDriver(aSQL, aVisitor, aKind);
        } catch (IOException e) {
            throw new HSQLParseException(e.getMessage());
        }
        driver.walk(aVisitor);
        if (aVisitor.hasErrors()) {
            throw new HSQLParseException(aVisitor.getErrorMessagesAsString());
        }
    }

    /**
     * Process a DQL string and return the VoltXML for the resulting
     * catalog.  If the CatalogAdapter is supplied use it.  If it is
     * null, use the HSQLInterface's CatalogAdapter, which is the one
     * representing the current database.  Note that this *does not*
     * update the CatalogAdapter.  This is just a DQL statement.
     * @param aKind TODO
     * @param sql
     *
     * @return
     * @throws HSQLParseException
     */
    public VoltXMLElement getVoltXMLFromSQLUsingVoltSQLParser(String aSQL, CatalogAdapter aAdapter, SQLKind aKind) throws HSQLParseException {
        CatalogAdapter adapter = (aAdapter == null) ? m_catalogAdapter : aAdapter;
        VoltParserFactory factory = new VoltParserFactory(adapter);
        VoltDDLVisitor visitor = new VoltDDLVisitor(factory, null);
        processSQLWithListener(aSQL, visitor, aKind);
        return visitor.getVoltXML();
    }

    public boolean usingVoltSQLParser() {
        return Boolean.getBoolean("useVoltSQLParser");
    }
    /*
     * This is the VoltDB Sql Parser catalog.
     */
    private CatalogAdapter m_catalogAdapter = new CatalogAdapter();

    public final CatalogAdapter getCatalogAdapter() {
        return m_catalogAdapter;
    }

    public final void setCatalogAdapter(CatalogAdapter aAdapter) {
        m_catalogAdapter = aAdapter;
    }

    /**
     * Fetch a VoltXMLElement from the catalog.
     *
     * @param aTableName
     * @param aAdapter
     * @return
     */
    public VoltXMLElement getVoltCatalogXML(String aTableName) {
        VoltXMLElement xml = new VoltXMLElement(XML_SCHEMA_NAME);
        xml.withValue("name", "databaseschema");
        for (String tblName : m_catalogAdapter.getTableNames()) {
            if ((aTableName != null) && (!aTableName.equalsIgnoreCase(tblName))) {
                continue;
            }
            org.voltdb.sqlparser.semantics.symtab.Table table = m_catalogAdapter.getTableByName(tblName);
            VoltXMLElement tableXML = new VoltXMLElement("table");
            tableXML.withValue("name", table.getName().toUpperCase());
            xml.children.add(tableXML);
            VoltXMLElement columnsXML = new VoltXMLElement("columns");
            columnsXML.withValue("name", "columns");
            tableXML.children.add(columnsXML);
            for (int colIdx = 0; colIdx < table.getColumnCount(); colIdx += 1) {
                IColumn col = table.getColumnByIndex(colIdx);
                String colName = col.getName();
                VoltXMLElement colXML = new VoltXMLElement("column");
                colXML.withValue("name", colName.toUpperCase())
                      .withValue("index", Integer.toString(colIdx))
                      .withValue("nullable", (col.isNullable() ? "true" : "false"))
                      .withValue("valuetype", col.getType().getName().toUpperCase());
                // What about the default value?
                columnsXML.children.add(colXML);
            }
            VoltXMLElement indicesXML = new VoltXMLElement("indexes");
            indicesXML.withValue("name", "indexes");
            tableXML.children.add(indicesXML);
            VoltXMLElement constraintsXML = new VoltXMLElement("constraints");
            constraintsXML.withValue("name", "constraints");
            tableXML.children.add(constraintsXML);
        }
        return xml;
    }

}
