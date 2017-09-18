package org.hsqldb_voltpatches;

import java.util.List;

import org.voltdb.sqlparser.semantics.symtab.ParserFactory;
import org.voltdb.sqlparser.syntax.grammar.ICatalogAdapter;
import org.voltdb.sqlparser.syntax.grammar.IColumnIdent;
import org.voltdb.sqlparser.syntax.grammar.ICreateTableStatement;
import org.voltdb.sqlparser.syntax.grammar.IIndex;
import org.voltdb.sqlparser.syntax.grammar.IInsertStatement;
import org.voltdb.sqlparser.syntax.grammar.IJoinTree;
import org.voltdb.sqlparser.syntax.grammar.IOperator;
import org.voltdb.sqlparser.syntax.grammar.ISelectQuery;
import org.voltdb.sqlparser.syntax.grammar.ISemantino;
import org.voltdb.sqlparser.syntax.grammar.JoinOperator;
import org.voltdb.sqlparser.syntax.grammar.Projection;
import org.voltdb.sqlparser.syntax.grammar.QuerySetOp;
import org.voltdb.sqlparser.syntax.symtab.IAST;
import org.voltdb.sqlparser.syntax.symtab.IColumn;
import org.voltdb.sqlparser.syntax.symtab.IParserFactory;
import org.voltdb.sqlparser.syntax.symtab.ISourceLocation;
import org.voltdb.sqlparser.syntax.symtab.ISymbolTable;
import org.voltdb.sqlparser.syntax.symtab.ITable;
import org.voltdb.sqlparser.syntax.symtab.IType;
import org.voltdb.sqlparser.syntax.symtab.IndexType;
import org.voltdb.sqlparser.syntax.util.ErrorMessageSet;

/**
 * This is the most derived class of the parser factory hierarchy.  Its
 * main goal is creating IAST objects.  We discover here that, much to our
 * surprise, the IAST objects are, in fact, VoltXMLElements. Who would have
 * thunk it?
 *
 * @author bwhite
 *
 */
public class VoltParserFactory extends ParserFactory implements IParserFactory {
	private static final String PRIMARY_KEY_INDEX_PATTERN = "VOLTDB_PK_%s_INDEX";
	private static final String UNIQUE_KEY_INDEX_PATTERN = "VOLTDB_UNIQUE_%s_%s_INDEX";
	private static final String ASSUMED_UNIQUE_KEY_INDEX_PATTERN = "VOLTDB_ASSUMED_UNIQUE_%s_%s_INDEX";
    int m_id = 1;
    public VoltParserFactory(ICatalogAdapter aCatalog) {
        super(aCatalog);
    }
    @Override
    public ITable makeTable(ISourceLocation aSourceLocation, String aTableName) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public IType makeType(String upperCase, String v0, String v1) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public ICatalogAdapter getCatalog() {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public ISelectQuery newSimpleTableSelectQuery(ISourceLocation aLoc, ISymbolTable aSymbolTable,
            IParserFactory aFactory, ErrorMessageSet aErrorMessages) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public ISelectQuery newCompoundQuery(QuerySetOp op, ISelectQuery left, ISelectQuery right) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public void processQuery(ISelectQuery aSelectQuery) {
        // TODO Auto-generated method stub

    }
    @Override
    public IInsertStatement newInsertStatement() {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public IAST makeConstantAST(IType aIntType, Object aIntegerValue) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public IAST makeUnaryAST(IType aIntType, boolean aBoolValue) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public IAST makeColumnRef(String aRealTableName, String aTableAlias, String aColumnName) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public IAST makeBinaryAST(IOperator aOp, ISemantino aLeftoperand, ISemantino aRightoperand) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public IOperator getExpressionOperator(String aText) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public IAST addTypeConversion(IAST aNode, IType aSrcType, IType aTrgType) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public IAST makeQueryAST(List<Projection> aProjections, IAST aWhereCondition, ISymbolTable aTables) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public IAST makeInsertAST(IInsertStatement aInsertStatement) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public ISemantino getErrorSemantino() {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public IAST makeUnaryAST(IType type, IOperator aOperator, ISemantino aOperand) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public ISemantino makeQuerySemantino(ISelectQuery query) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public IIndex newIndex(ISourceLocation aLoc, String indexName, ITable table, IColumn column, IndexType it) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public IJoinTree newJoinTree(JoinOperator op, IJoinTree joinTree, IJoinTree right, ISemantino condition) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public IJoinTree newTableReference(String aTableName, String aTableAlias) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public IJoinTree newDerivedJoinTree(ISelectQuery derivedTable, String tableName) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public ISourceLocation newSourceLocation(int aLineNumber, int aColumnNumber) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public IColumnIdent makeColumnRef(String colName, ISourceLocation newSourceLocation) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public ICreateTableStatement makeCreateTableStatement() {
        // TODO Auto-generated method stub
        return null;
    }
}
