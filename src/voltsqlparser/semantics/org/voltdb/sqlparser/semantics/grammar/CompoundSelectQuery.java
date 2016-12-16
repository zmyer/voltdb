package org.voltdb.sqlparser.semantics.grammar;

import java.util.List;

import org.voltdb.sqlparser.syntax.grammar.IOperator;
import org.voltdb.sqlparser.syntax.grammar.ISelectQuery;
import org.voltdb.sqlparser.syntax.grammar.ISemantino;
import org.voltdb.sqlparser.syntax.grammar.Projection;
import org.voltdb.sqlparser.syntax.grammar.QuerySetOp;
import org.voltdb.sqlparser.syntax.symtab.IAST;
import org.voltdb.sqlparser.syntax.symtab.IExpressionParser;
import org.voltdb.sqlparser.syntax.symtab.ISymbolTable;
import org.voltdb.sqlparser.syntax.symtab.ITable;
import org.voltdb.sqlparser.syntax.symtab.IType;

public class CompoundSelectQuery implements ISelectQuery, IDQLStatement {

	ISelectQuery m_left;
	ISelectQuery m_right;
	QuerySetOp   m_op;
	
	public CompoundSelectQuery(QuerySetOp op, ISelectQuery left, ISelectQuery right) {
		m_op = op;
		m_left = left;
		m_right = right;
	}

	@Override
	public boolean isSimpleTable() {
		return false;
	}

	@Override
	public QuerySetOp getSetOp() throws Exception {
		return m_op;
	}

	@Override
	public ISelectQuery getLeftQuery() {
		return m_left;
	}

	@Override
	public ISelectQuery getRightQuery() {
		// TODO Auto-generated method stub
		return m_right;
	}

	@Override
	public void addProjection(String aTableName, String aColumnName, String aAlias, int aLineNo, int aColNo) {
		assert(false);
	}

	@Override
	public void addProjection(int aLineNo, int aColNo) {
		assert(false);
	}

	@Override
	public void pushSemantino(ISemantino aColumnSemantino) {
		assert(false);
	}

	@Override
	public ISemantino popSemantino() {
		assert(false);
		return null;
	}

	@Override
	public String printProjections() {
		assert(false);
		return null;
	}

	@Override
	public void addTable(ITable aITable, String aAlias) {
		assert(false);
	}

	@Override
	public String printTables() {
		assert(false);
		return null;
	}

	@Override
	public boolean hasSemantinos() {
		assert(false);
		return false;
	}

	@Override
	public ISemantino getColumnSemantino(String aColumnName, String aTableName) {
		assert(false);
		return null;
	}

	@Override
	public ISemantino getConstantSemantino(Object value, IType type) {
		assert(false);
		return null;
	}

	@Override
	public ISemantino getSemantinoMath(IOperator aOperator, ISemantino aLeftoperand, ISemantino aRightoperand) {
		assert(false);
		return null;
	}

	@Override
	public ISemantino getSemantinoCompare(IOperator aOperator, ISemantino aLeftoperand, ISemantino aRightoperand) {
		assert(false);
		return null;
	}

	@Override
	public ISemantino getSemantinoBoolean(IOperator aOperator, ISemantino aLeftoperand, ISemantino aRightoperand) {
		assert(false);
		return null;
	}

	@Override
	public List<Projection> getProjections() {
		assert(false);
		return null;
	}

	@Override
	public void setWhereCondition(ISemantino aRet) {
		assert(false);
	}

	@Override
	public IAST getWhereCondition() {
		assert(false);
		return null;
	}

	@Override
	public ISymbolTable getTables() {
		assert(false);
		return null;
	}

	@Override
	public void setAST(IAST aMakeQueryAST) {
		assert(false);
	}

	@Override
	public boolean validate() {
		assert(false);
		return false;
	}

	@Override
	public IExpressionParser getExpressionParser() {	
		assert(false);
		return null;
	}

	@Override
	public void setExpressionParser(IExpressionParser expr) {
		assert(false);
	}

}
