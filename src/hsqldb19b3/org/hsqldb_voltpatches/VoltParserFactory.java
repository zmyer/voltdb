package org.hsqldb_voltpatches;

import java.util.List;

import org.voltdb.sqlparser.semantics.grammar.CompoundSelectQuery;
import org.voltdb.sqlparser.semantics.grammar.InsertStatement;
import org.voltdb.sqlparser.semantics.grammar.SimpleTableSelectQuery;
import org.voltdb.sqlparser.semantics.symtab.Index;
import org.voltdb.sqlparser.semantics.symtab.ParserFactory;
import org.voltdb.sqlparser.semantics.symtab.Semantino;
import org.voltdb.sqlparser.semantics.symtab.SymbolTable;
import org.voltdb.sqlparser.semantics.symtab.SymbolTable.TablePair;
import org.voltdb.sqlparser.semantics.symtab.Table;
import org.voltdb.sqlparser.semantics.symtab.Type;
import org.voltdb.sqlparser.syntax.grammar.ICatalogAdapter;
import org.voltdb.sqlparser.syntax.grammar.IColumnIdent;
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

    private String newId() {
        return Integer.toString(m_id++);
    }

    @Override
    public IAST makeConstantAST(IType aType, Object aValueOf) {
        assert(aType instanceof Type);
        Type vtype = (Type)aType;
        VoltXMLElement answer = new VoltXMLElement("value");
        answer.withValue("id", newId());
        answer.withValue("value", aValueOf.toString());
        answer.withValue("valuetype", vtype.getName().toUpperCase());
        return answer;
    }

    @Override
    public IAST makeUnaryAST(IType aIntType, boolean aValueOf) {
        Type intType = (Type)aIntType;
        VoltXMLElement answer = new VoltXMLElement("value");
        answer.withValue("id", newId());
        answer.withValue("value", Boolean.toString(aValueOf));
        answer.withValue("valuetype", intType.getName().toUpperCase());
        return answer;
    }

    @Override
    public IAST makeBinaryAST(IOperator aOp,
                              ISemantino aLeftoperand,
                              ISemantino aRightoperand) {
        VoltXMLElement answer = new VoltXMLElement("operation");
        answer.withValue("id", newId())
              .withValue("optype", aOp.getVoltOperation());
        answer.children.add((VoltXMLElement)aLeftoperand.getAST());
        answer.children.add((VoltXMLElement)aRightoperand.getAST());
        return answer;
    }

    /**
     * Apparently we don't need to do anything to convert
     * types.  Who would have thought it.
     */
    @Override
    public IAST addTypeConversion(IAST aNode, IType aSrcType, IType aTrgType) {
        return aNode;
    }

    @Override
    public IAST makeQueryAST(List<Projection> aProjections,
                              IAST aWhereCondition,
                              ISymbolTable aTables) {
        VoltXMLElement columns = makeColumns(aProjections, aTables);
        VoltXMLElement params = makeParams();
        VoltXMLElement joincond = makeJoinCond(aWhereCondition);
        VoltXMLElement tablescans = makeTableScans(joincond, (SymbolTable)aTables);
        VoltXMLElement answer = new VoltXMLElement("select");
        answer.children.add(columns);
        answer.children.add(params);
        answer.children.add(tablescans);
        return answer;
    }

    private VoltXMLElement makeParams() {
        VoltXMLElement answer = new VoltXMLElement("parameters");
        return answer;
    }

    private VoltXMLElement makeJoinCond(IAST aWhereCondition) {
        if (aWhereCondition != null) {
            VoltXMLElement answer = new VoltXMLElement("joincond");
            answer.children.add((VoltXMLElement)aWhereCondition);
            return answer;
        }
        return null;
    }

    private VoltXMLElement makeTableScans(VoltXMLElement aJoincond, SymbolTable aTables) {
        VoltXMLElement answer = new VoltXMLElement("tablescans");
        VoltXMLElement lastChild = null;
        for (SymbolTable.TablePair tp : aTables.getTables()) {
            VoltXMLElement scan = new VoltXMLElement("tablescan");
            scan.withValue("jointype", "inner")
                .withValue("table", tp.getTable().getName().toUpperCase())
                .withValue("tablealias", tp.getAlias().toUpperCase());
            answer.children.add(scan);
            lastChild = scan;
        }
        if (aJoincond != null) {
            lastChild.children.add(aJoincond);
        }
        return answer;
    }

    private VoltXMLElement makeColumns(List<Projection> aProjections,
                                       ISymbolTable aTables) {
        SymbolTable symtab = (SymbolTable)aTables;
        VoltXMLElement answer = new VoltXMLElement("columns");
        for (Projection proj : aProjections) {
            if (proj.isStar()) {
                addAllColumns(answer, aTables);
            } else {
                String colName = proj.getColumnName();
                String tableAlias = proj.getTableName();
                String tableName = symtab.getTableNameByColumn(colName);
                if (tableName == null) {
                    ISourceLocation aloc = newSourceLocation(proj.getLineNo(),
                                                             proj.getColNo());
                    getErrorMessages().addError(aloc,
                                                "Cannot find column named \"%s\"",
                                                colName);
                    tableName = "<<NOT_FOUND>>";
                }
                String alias = proj.getAlias();
                if (alias == null) {
                    alias = colName;
                }
                addOneColumn(answer, tableName, tableAlias, colName, alias);
            }
        }
        return answer;
    }

    private void addOneColumn(VoltXMLElement aAnswer, String aTableName, String aTableAlias, String aColumnName, String aColumnAlias) {
        VoltXMLElement colref = new VoltXMLElement("columnref");
        aAnswer.children.add(colref);
        colref.withValue("id", newId())
              .withValue("table", aTableName.toUpperCase())
              .withValue("column", aColumnName.toUpperCase())
              .withValue("alias", aColumnAlias.toUpperCase());
        if (aTableAlias != null) {
            colref.withValue("tablealias", aTableAlias.toUpperCase());
        }
    }

    public void addAllColumns(VoltXMLElement aParent, ISymbolTable aTables) {
        assert(aTables instanceof SymbolTable);
        SymbolTable tables = (SymbolTable)aTables;
        for (TablePair tblpair : tables.getTables()) {
            String tableName = tblpair.getTable().getName();
            String tableAlias = tblpair.getAlias();
            Table tbl = tblpair.getTable();
            for (int idx = 0; idx < tbl.getColumnCount(); idx += 1) {
            	IColumn col = tbl.getColumnByIndex(idx);
            	String colName = col.getName();
                addOneColumn(aParent, tableName, tableAlias, colName, colName);
            }
        }
    }

    @Override
    public IAST makeColumnRef(String aRealTableName,
                              String aTableAlias,
                              String aColumnName) {
        VoltXMLElement answer = new VoltXMLElement("columnref");
        answer.withValue("alias", aColumnName.toUpperCase())
              .withValue("column", aColumnName.toUpperCase())
              .withValue("id", newId())
              .withValue("table", aRealTableName.toUpperCase())
              .withValue("tablealias", aTableAlias.toUpperCase());
        return answer;
    }

    @Override
    public VoltXMLElement makeInsertAST(IInsertStatement aInsertStatement) {
        assert(aInsertStatement instanceof InsertStatement);
        InsertStatement insertStatement = (InsertStatement)aInsertStatement;
        VoltXMLElement top = new VoltXMLElement("insert");
        top.withValue("table", insertStatement.getTableName().toUpperCase());
        VoltXMLElement columns = new VoltXMLElement("columns");
        top.children.add(columns);
        for (int idx = 0; idx < insertStatement.getNumberColumns(); idx += 1) {
            VoltXMLElement col = new VoltXMLElement("column");
            columns.children.add(col);
            col.withValue("name", insertStatement.getColumnName(idx).toUpperCase());
            VoltXMLElement val = new VoltXMLElement("value");
            col.children.add(val);
            val.withValue("id", Integer.toString(idx+1));
            val.withValue("value", insertStatement.getColumnValue(idx));
            val.withValue("valuetype", insertStatement.getColumnType(idx).getName().toUpperCase());
        }
        VoltXMLElement params = new VoltXMLElement("parameters");
        top.children.add(params);
        return top;
    }

	@Override
	public ISemantino getErrorSemantino() {
		return Semantino.getErrorSemantino();
	}

	@Override
	public IAST makeUnaryAST(IType type,
							 IOperator aOperator,
							 ISemantino aOperand) {
        VoltXMLElement answer = new VoltXMLElement("operation");
        answer.withValue("id", newId())
              .withValue("optype", aOperator.getVoltOperation());
        answer.children.add((VoltXMLElement)aOperand.getAST());
        return answer;
	}

	@Override
	public ISemantino makeQuerySemantino(ISelectQuery aQuery) {
		return new Semantino(SymbolTable.getVoidType(),
							 makeQueryAST(aQuery.getProjections(),
									 	  aQuery.getWhereCondition(),
									 	  aQuery.getTables()));
	}

	@Override
	public IIndex newIndex(ISourceLocation aLoc,
	                       String indexName,
						   ITable table,
						   IColumn column,
						   IndexType indexType) {
		if (indexName == null) {
			indexName = makeIndexName(table.getName(), column.getName(), indexType);
		}
		return new Index(aLoc, indexName, indexType);
	}

	private String makeIndexName(String tableName,
								 String columnName,
								 IndexType indexType) {
		switch (indexType) {
		case PRIMARY_KEY:
			return String.format(PRIMARY_KEY_INDEX_PATTERN, tableName.toUpperCase());
		case UNIQUE_KEY:
			return String.format(UNIQUE_KEY_INDEX_PATTERN, tableName.toUpperCase(), columnName.toUpperCase());
		case ASSUMED_UNIQUE_KEY:
			return String.format(ASSUMED_UNIQUE_KEY_INDEX_PATTERN, tableName.toUpperCase(), columnName.toUpperCase());
		case INVALID:
		default:
			assert(false);
		}
		return null;
	}

	@Override
	public ISelectQuery newCompoundQuery(QuerySetOp op, ISelectQuery left, ISelectQuery right) {
		return new CompoundSelectQuery(op, left, right);
	}

    @Override
    public ISelectQuery newSimpleTableSelectQuery(ISourceLocation aLoc,
                                                  ISymbolTable aSymbolTable,
                                                  IParserFactory aFactory,
                                                  ErrorMessageSet aErrorMessages) {
        return new SimpleTableSelectQuery(aLoc, (SymbolTable)aSymbolTable, aFactory, aErrorMessages);
    }

    @Override
    public IJoinTree newJoinTree(JoinOperator op, IJoinTree joinTree, IJoinTree right, ISemantino condition) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IColumnIdent makeColumnRef(String colName, ISourceLocation newSourceLocation) {
        // TODO: Define this.
        return null;
    }
}
