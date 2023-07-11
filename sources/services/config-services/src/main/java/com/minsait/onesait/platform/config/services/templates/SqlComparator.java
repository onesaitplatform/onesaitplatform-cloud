/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2022 SPAIN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.minsait.onesait.platform.config.services.templates;

import java.util.Iterator;
import java.util.List;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.KeepExpression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.OracleHierarchicalExpression;
import net.sf.jsqlparser.expression.OracleHint;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.UserVariable;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.Distinct;
import net.sf.jsqlparser.statement.select.Fetch;
import net.sf.jsqlparser.statement.select.First;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.Offset;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.Pivot;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.Skip;
import net.sf.jsqlparser.statement.select.TableFunction;
import net.sf.jsqlparser.statement.select.Top;
import net.sf.jsqlparser.statement.select.Wait;
import net.sf.jsqlparser.statement.select.WithItem;

public class SqlComparator {

	private SqlComparator() {
		throw new IllegalStateException("Utility class");
	}

	public static MatchResult match(String query1, String query2) throws JSQLParserException {
		final Statement stmt1 = CCJSqlParserUtil.parse(query1);
		final Statement stmt2 = CCJSqlParserUtil.parse(query2);
		return matchStatement(stmt1, stmt2);
	}

	private static MatchResult matchStatement(Statement stmt1, Statement stmt2) {
		final MatchResult result = new MatchResult();
		stmt1.accept(new StatementVisitorComparator(stmt2, result));
		return result;
	}

	static void matchSelect(Select select1, Select select2, MatchResult result) {
		matchSelectBody(select1.getSelectBody(), select2.getSelectBody(), result);
		if (result.isMatch()) {
			matchWithItemsList(select1.getWithItemsList(), select2.getWithItemsList(), result);
		}
	}

	private static void matchSelectBody(SelectBody selectBody1, SelectBody selectBody2, MatchResult result) {
		selectBody1.accept(new SelectVisitorComparator(selectBody2, result));
	}

	private static void matchWithItemsList(List<WithItem> withItemsList1, List<WithItem> withItemsList2,
			MatchResult result) {
		checkNulls(withItemsList1, withItemsList2, result);
		if (result.isMatch() && withItemsList1 != null) {
			// TODO as first approach only the same SelectItems in the same order, and with
			// the same name will be considered as equal.
			// In the future this can be extended with a more sophisticated approach.
			final Iterator<WithItem> it1 = withItemsList1.iterator();
			final Iterator<WithItem> it2 = withItemsList2.iterator();

			while (it1.hasNext() && it2.hasNext()) {

				final WithItem withItem1 = it1.next();
				final WithItem withItem2 = it2.next();
				withItem1.accept(new SelectVisitorComparator(withItem2, result));
				if (!result.isMatch()) {
					break;
				}
			}

			if (it1.hasNext() || it2.hasNext()) {
				result.setResult(false);
			}

		}
	}

	static void matchWithItem(WithItem withItem1, WithItem withItem2, MatchResult result) {
		if (withItem1.getName().equals(withItem2.getName())) {
			matchSelectBody(withItem1, withItem2, result);
		} else {
			result.setResult(false);
		}

	}

	static void matchPlainSelect(PlainSelect plainSelect1, PlainSelect plainSelect2, MatchResult result) {

		matchSelectItems(plainSelect1.getSelectItems(), plainSelect2.getSelectItems(), result);

		matchFromItem(plainSelect1.getFromItem(), plainSelect2.getFromItem(), result);

		matchExpression(plainSelect1.getWhere(), plainSelect2.getWhere(), result);

		matchOrderByElements(plainSelect1.getOrderByElements(), plainSelect2.getOrderByElements(), result);

		matchGroupBy(plainSelect1.getGroupBy(), plainSelect2.getGroupBy(), result);

		matchHaving(plainSelect1.getHaving(), plainSelect2.getHaving(), result);

		matchDistinct(plainSelect1.getDistinct(), plainSelect2.getDistinct(), result);

		matchFetch(plainSelect1.getFetch(), plainSelect2.getFetch(), result);

		matchFirst(plainSelect1.getFirst(), plainSelect2.getFirst(), result);

		matchJoins(plainSelect1.getJoins(), plainSelect2.getJoins(), result);

		matchLimit(plainSelect1.getLimit(), plainSelect2.getLimit(), result);

		matchOffset(plainSelect1.getOffset(), plainSelect2.getOffset(), result);

		matchSkip(plainSelect1.getSkip(), plainSelect2.getSkip(), result);

		matchTop(plainSelect1.getTop(), plainSelect2.getTop(), result);

		matchWait(plainSelect1.getWait(), plainSelect2.getWait(), result);

		matchIntoTables(plainSelect1.getIntoTables(), plainSelect2.getIntoTables(), result);

		matchForUpdateTable(plainSelect1.getForUpdateTable(), plainSelect2.getForUpdateTable(), result);

		matchMySqlSqlCalcFoundRows(plainSelect1.getMySqlSqlCalcFoundRows(), plainSelect2.getMySqlSqlCalcFoundRows(),
				result);

		matchMySqlSqlNoCache(plainSelect1.getMySqlSqlCacheFlag() != null, plainSelect2.getMySqlSqlCacheFlag() != null,
				result);

		matchOracleHierarchical(plainSelect1.getOracleHierarchical(), plainSelect2.getOracleHierarchical(), result);

		matchOracleHint(plainSelect1.getOracleHint(), plainSelect2.getOracleHint(), result);

	}

	static void matchSelectItems(List<SelectItem> selectItems1, List<SelectItem> selectItems2, MatchResult result) {
		checkNulls(selectItems1, selectItems2, result);
		if (result.isMatch() && selectItems1 != null) {
			// TODO In the future this can be extended with a more sophisticated approach.

			// The same SelectItems in the same order, and with the same name
			if (selectItems1.size() == selectItems2.size()) {
				matchSelectItemsEqualSize(selectItems1, selectItems2, result);
			} else if (selectItems2.size() == 1) {
				// The template consists of a variable which will store some
				// SelectExpressionItems in Array format
				// net.sf.jsqlparser.statement.select.AllColumns
				final SelectItem selectItem = selectItems2.get(0);
				if (!(selectItem instanceof AllColumns)) {
					final SelectExpressionItem selectItem2 = (SelectExpressionItem) selectItems2.get(0);
					if (SelectExpressionItem.class.equals(selectItems2.get(0).getClass())
							&& UserVariable.class.equals(selectItem2.getExpression().getClass())) {
						final UserVariable userVariable = (UserVariable) selectItem2.getExpression();
						result.addVariable(userVariable.getName(), selectItems1.toString(), VariableData.Type.STRING);
						result.setResult(true);
					}
				}
			}
		}
	}

	private static void matchSelectItemsEqualSize(List<SelectItem> selectItems1, List<SelectItem> selectItems2,
			MatchResult result) {
		final Iterator<SelectItem> it1 = selectItems1.iterator();
		final Iterator<SelectItem> it2 = selectItems2.iterator();

		while (it1.hasNext() && it2.hasNext()) {
			final SelectItem selectItem1 = it1.next();
			final SelectItem selectItem2 = it2.next();
			selectItem1.accept(new SelectItemVisitorComparator(selectItem2, result));
			if (!result.isMatch()) {
				break;
			}
		}

		if (it1.hasNext() || it2.hasNext()) {
			result.setResult(false);
		}
	}

	static void matchAllColumns(AllColumns allColumns1, AllColumns allColumns2, MatchResult result) {
		final boolean match = allColumns1.toString().equals(allColumns2.toString());
		result.setResult(match);
	}

	static void matchAllTableColumns(AllTableColumns allTableColumns1, AllTableColumns allTableColumns2,
			MatchResult result) {
		final boolean match = allTableColumns1.toString().equals(allTableColumns2.toString());
		result.setResult(match);
	}

	static void matchSelectExpressionItem(SelectExpressionItem selectExpressionItem1,
			SelectExpressionItem selectExpressionItem2, MatchResult result) {
		if (selectExpressionItem1.getExpression().getClass().equals(CaseExpression.class)
				|| selectExpressionItem1.getExpression().getClass().equals(Function.class)
				|| selectExpressionItem1.getExpression().getClass().equals(Parenthesis.class)) {
			checkNulls(selectExpressionItem1.getExpression(), selectExpressionItem2.getExpression(), result);
			if (result.isMatch() && selectExpressionItem1.getExpression() != null) {
				selectExpressionItem1.getExpression()
						.accept(new ExpressionVisitorComparator(selectExpressionItem2.getExpression(), result));
			}
		} else {
			final boolean match = selectExpressionItem1.toString().equals(selectExpressionItem2.toString());
			result.setResult(match);
		}
	}

	private static void matchFromItem(FromItem fromItem1, FromItem fromItem2, MatchResult result) {
		fromItem1.accept(new FromItemVisitorComparator(fromItem2, result));
	}

	private static void matchExpression(Expression expression1, Expression expression2, MatchResult result) {
		checkNulls(expression1, expression2, result);
		if (result.isMatch() && expression1 != null) {
			expression1.accept(new ExpressionVisitorComparator(expression2, result));
		}
	}

	private static void matchOrderByElements(List<OrderByElement> orderByElements1,
			List<OrderByElement> orderByElements2, MatchResult result) {
		checkNulls(orderByElements1, orderByElements2, result);
		if (result.isMatch() && orderByElements1 != null) {
			// TODO as first approach only the same OrderByElements in the same order, and
			// with the same name will be considered as equal.
			// In the future this can be extended with a more sophisticated approach.
			final Iterator<OrderByElement> it1 = orderByElements1.iterator();
			final Iterator<OrderByElement> it2 = orderByElements2.iterator();

			while (it1.hasNext() && it2.hasNext()) {
				final OrderByElement orderByElement1 = it1.next();
				final OrderByElement orderByElement2 = it2.next();
				orderByElement1.accept(new OrderByVisitorComparator(orderByElement2, result));
				if (!result.isMatch()) {
					break;
				}
			}

			if (it1.hasNext() || it2.hasNext()) {
				result.setResult(false);
			}
		}
	}

	static void matchOrderByElement(OrderByElement orderByElement1, OrderByElement orderByElement2,
			MatchResult result) {
		final boolean match = orderByElement1.toString().equals(orderByElement2.toString());
		result.setResult(match);
	}

	private static void matchGroupBy(GroupByElement groupBy1, GroupByElement groupBy2, MatchResult result) {
		checkNulls(groupBy1, groupBy2, result);
		if (result.isMatch() && groupBy1 != null) {
			matchGroupByColumnReferences(groupBy1.getGroupByExpressions(), groupBy2.getGroupByExpressions(), result);
		}
	}

	private static void matchGroupByColumnReferences(List<Expression> groupByColumnReferences1,
			List<Expression> groupByColumnReferences2, MatchResult result) {
		checkNulls(groupByColumnReferences1, groupByColumnReferences2, result);
		if (result.isMatch() && groupByColumnReferences1 != null) {
			// TODO as first approach only the same OrderByElements in the same order, and
			// with the same name will be considered as equal.
			// In the future this can be extended with a more sophisticated approach.
			final Iterator<Expression> it1 = groupByColumnReferences1.iterator();
			final Iterator<Expression> it2 = groupByColumnReferences2.iterator();

			while (it1.hasNext() && it2.hasNext()) {
				final Expression expression1 = it1.next();
				final Expression expression2 = it2.next();
				expression1.accept(new ExpressionVisitorComparator(expression2, result));
				if (!result.isMatch()) {
					break;
				}
			}

			if (it1.hasNext() || it2.hasNext()) {
				result.setResult(false);
			}
		}
	}

	static void matchGroupByColumnReference(Expression expression1, Expression expression2, MatchResult result) {
		final boolean match = expression1.toString().equals(expression2.toString());
		result.setResult(match);
	}

	private static void matchHaving(Expression having1, Expression having2, MatchResult result) {
		checkNulls(having1, having2, result);
		if (result.isMatch() && having1 != null) {
			having1.accept(new ExpressionVisitorComparator(having2, result));
		}
	}

	private static void matchDistinct(Distinct distinct1, Distinct distinct2, MatchResult result) {
		checkNulls(distinct1, distinct2, result);
		if (result.isMatch() && distinct1 != null) {
			if (distinct1.isUseUnique() == distinct2.isUseUnique()) {
				matchSelectItems(distinct1.getOnSelectItems(), distinct2.getOnSelectItems(), result);
			} else {
				result.setResult(false);
			}
		}
	}

	private static void matchFetch(Fetch fetch1, Fetch fetch2, MatchResult result) {
		checkNulls(fetch1, fetch2, result);
		if (result.isMatch() && fetch1 != null) {
			final boolean match = fetch1.toString().equals(fetch2.toString());
			result.setResult(match);
		}
	}

	private static void matchFirst(First first1, First first2, MatchResult result) {
		checkNulls(first1, first2, result);
		if (result.isMatch() && first1 != null) {
			final boolean match = first1.toString().equals(first2.toString());
			result.setResult(match);
		}
	}

	private static void matchJoins(List<Join> joins1, List<Join> joins2, MatchResult result) {
		checkNulls(joins1, joins2, result);
		if (result.isMatch() && joins1 != null) {
			// TODO as first approach only the same OrderByElements in the same order, and
			// with the same name will be considered as equal.
			// In the future this can be extended with a more sophisticated approach.
			final Iterator<Join> it1 = joins1.iterator();
			final Iterator<Join> it2 = joins2.iterator();

			while (it1.hasNext() && it2.hasNext()) {
				final Join join1 = it1.next();
				final Join join2 = it2.next();
				matchJoin(join1, join2, result);
				if (!result.isMatch()) {
					break;
				}
			}

			if (it1.hasNext() || it2.hasNext()) {
				result.setResult(false);
			}
		}
	}

	private static void matchJoin(Join join1, Join join2, MatchResult result) {
		checkNulls(join1, join2, result);
		if (result.isMatch() && join1 != null) {
			matchExpression(join1.getOnExpression(), join2.getOnExpression(), result);
			if (result.isMatch()) {
				matchFromItem(join1.getRightItem(), join2.getRightItem(), result);
				if (result.isMatch()) {
					matchColumns(join1.getUsingColumns(), join2.getUsingColumns(), result);
				}
			}

		}
	}

	private static void matchColumns(List<Column> usingColumns1, List<Column> usingColumns2, MatchResult result) {
		checkNulls(usingColumns1, usingColumns2, result);
		if (result.isMatch() && usingColumns1 != null) {
			// TODO as first approach only the same OrderByElements in the same order, and
			// with the same name will be considered as equal.
			// In the future this can be extended with a more sophisticated approach.
			final Iterator<Column> it1 = usingColumns1.iterator();
			final Iterator<Column> it2 = usingColumns2.iterator();

			while (it1.hasNext() && it2.hasNext()) {
				final Column column1 = it1.next();
				final Column column2 = it2.next();
				matchColumn(column1, column2, result);
				if (!result.isMatch()) {
					break;
				}
			}

			if (it1.hasNext() || it2.hasNext()) {
				result.setResult(false);
			}
		}

	}

	private static void matchLimit(Limit limit1, Limit limit2, MatchResult result) {
		checkNulls(limit1, limit2, result);
		if (result.isMatch() && limit1 != null) {
			matchExpression(limit1.getOffset(), limit2.getOffset(), result);
			if (result.isMatch()) {
				matchExpression(limit1.getRowCount(), limit2.getRowCount(), result);
			}
		}

	}

	private static void matchOffset(Offset offset1, Offset offset2, MatchResult result) {
		checkNulls(offset1, offset2, result);
		if (result.isMatch() && offset1 != null) {
			result.setResult(offset1.getOffset() == offset2.getOffset());
			if (result.isMatch()) {
				result.setResult(offset1.getOffsetParam().equals(offset2.getOffsetParam()));
				if (result.isMatch()) {
					matchExpression(offset1.getOffset(), offset2.getOffset(), result);
				}
			}
		}
	}

	private static void matchSkip(Skip skip1, Skip skip2, MatchResult result) {
		checkNulls(skip1, skip2, result);
		if (result.isMatch() && skip1 != null) {
			result.setResult(skip1.getRowCount().equals(skip2.getRowCount()));
			if (result.isMatch()) {
				result.setResult(skip1.getVariable().equals(skip2.getVariable()));
				if (result.isMatch()) {
					matchJdbcParameter(skip1.getJdbcParameter(), skip2.getJdbcParameter(), result);
				}
			}
		}
	}

	private static void matchJdbcParameter(JdbcParameter jdbcParameter1, JdbcParameter jdbcParameter2,
			MatchResult result) {
		checkNulls(jdbcParameter1, jdbcParameter2, result);
		if (result.isMatch() && jdbcParameter1 != null) {
			result.setResult(jdbcParameter1.isUseFixedIndex() == jdbcParameter2.isUseFixedIndex());
			if (result.isMatch()) {
				result.setResult(jdbcParameter1.getIndex().equals(jdbcParameter2.getIndex()));
			}
		}
	}

	static void matchJdbcNamedParameter(JdbcNamedParameter jdbcNamedParameter1, JdbcNamedParameter jdbcNamedParameter2,
			MatchResult result) {
		checkNulls(jdbcNamedParameter1, jdbcNamedParameter2, result);
		if (result.isMatch() && jdbcNamedParameter1 != null) {
			result.setResult(jdbcNamedParameter1.getName().equals(jdbcNamedParameter2.getName()));
		}
	}

	private static void matchTop(Top top1, Top top2, MatchResult result) {
		checkNulls(top1, top2, result);
		if (result.isMatch() && top1 != null) {
			result.setResult(top1.isPercentage() == top2.isPercentage());
			if (result.isMatch()) {
				result.setResult(top1.hasParenthesis() == top2.hasParenthesis());
				if (result.isMatch()) {
					matchExpression(top1.getExpression(), top2.getExpression(), result);
				}
			}
		}
	}

	private static void matchWait(Wait wait1, Wait wait2, MatchResult result) {
		checkNulls(wait1, wait2, result);
		if (result.isMatch() && wait1 != null) {
			result.setResult(wait1.getTimeout() == wait2.getTimeout());
		}
	}

	private static void matchIntoTables(List<Table> intoTables1, List<Table> intoTables2, MatchResult result) {
		checkNulls(intoTables1, intoTables2, result);
		if (result.isMatch() && intoTables1 != null) {
			// TODO as first approach only the same OrderByElements in the same order, and
			// with the same name will be considered as equal.
			// In the future this can be extended with a more sophisticated approach.
			final Iterator<Table> it1 = intoTables1.iterator();
			final Iterator<Table> it2 = intoTables2.iterator();

			while (it1.hasNext() && it2.hasNext()) {
				final Table table1 = it1.next();
				final Table table2 = it2.next();
				matchTable(table1, table2, result);
				if (!result.isMatch()) {
					break;
				}
			}

			if (it1.hasNext() || it2.hasNext()) {
				result.setResult(false);
			}
		}
	}

	private static void matchTable(Table table1, Table table2, MatchResult result) {
		checkNulls(table1, table2, result);

		if (!result.isMatch() && table1 != null) {
			table1.accept(new FromItemVisitorComparator(table2, result));
			if (!result.isMatch()) {
				result.setResult(table1.toString().equals(table2.toString()));
			}
		}
	}

	private static void matchForUpdateTable(Table forUpdateTable1, Table forUpdateTable2, MatchResult result) {
		checkNulls(forUpdateTable1, forUpdateTable2, result);
		if (!result.isMatch() && forUpdateTable1 != null) {
			forUpdateTable1.accept(new FromItemVisitorComparator(forUpdateTable2, result));
			if (!result.isMatch()) {
				forUpdateTable1.accept(new IntoTableVisitorComparator(forUpdateTable2, result));
			}
		}
	}

	private static void matchMySqlSqlCalcFoundRows(boolean mySqlSqlCalcFoundRows1, boolean mySqlSqlCalcFoundRows2,
			MatchResult result) {
		result.setResult(mySqlSqlCalcFoundRows1 == mySqlSqlCalcFoundRows2);
	}

	private static void matchMySqlSqlNoCache(boolean mySqlSqlNoCache1, boolean mySqlSqlNoCache2, MatchResult result) {
		result.setResult(mySqlSqlNoCache1 == mySqlSqlNoCache2);
	}

	private static void matchOracleHierarchical(OracleHierarchicalExpression oracleHierarchical1,
			OracleHierarchicalExpression oracleHierarchical2, MatchResult result) {
		checkNulls(oracleHierarchical1, oracleHierarchical2, result);
		if (result.isMatch() && oracleHierarchical1 != null) {
			result.setResult(oracleHierarchical1.toString().equals(oracleHierarchical2.toString()));
		}
	}

	private static void matchOracleHint(OracleHint oracleHint1, OracleHint oracleHint2, MatchResult result) {
		checkNulls(oracleHint1, oracleHint2, result);
		if (result.isMatch() && oracleHint1 != null) {
			result.setResult(oracleHint1.toString().equals(oracleHint2.toString()));
		}
	}

	static void matchColumn(Column tableColumn1, Column tableColumn2, MatchResult result) {
		checkNulls(tableColumn1, tableColumn2, result);
		if (result.isMatch() && tableColumn1 != null) {
			final boolean match = tableColumn1.getFullyQualifiedName().equals(tableColumn2.getFullyQualifiedName());
			result.setResult(match);
		}
	}

	static void matchStringValue(StringValue stringValue1, StringValue stringValue2, MatchResult result) {
		checkNulls(stringValue1, stringValue2, result);
		if (result.isMatch() && stringValue1 != null) {

			final String value1 = stringValue1.getValue();
			final String value2 = stringValue2.getValue();

			checkNulls(value1, value2, result);
			if (result.isMatch() && value1 != null) {
				final boolean match = value1.equals(stringValue2.getValue());
				result.setResult(match);
			}
		}
	}

	static void matchStringValueWithVariable(StringValue stringValue1, UserVariable userVariable2, MatchResult result) {
		result.addVariable(userVariable2.getName(), stringValue1.getValue(), VariableData.Type.STRING);
		result.setResult(true);
	}

	private static void checkNulls(Object object1, Object object2, MatchResult result) {

		result.setResult((object1 == null && object2 == null) || (object1 != null && object2 != null));
	}

	static void matchLongValueValue(LongValue longValue1, LongValue longValue2, MatchResult result) {
		checkNulls(longValue1, longValue2, result);
		if (result.isMatch() && longValue1 != null) {

			final long value1 = longValue1.getValue();
			final long value2 = longValue2.getValue();

			result.setResult(value1 == value2);
		}

	}

	static void matchLongValueValueWithVariable(LongValue longValue1, UserVariable userVariable2, MatchResult result) {
		result.addVariable(userVariable2.getName(), longValue1.getStringValue(), VariableData.Type.LONG);
		result.setResult(true);

	}

	static void matchTableFunction(TableFunction tableFunction1, TableFunction tableFunction2, MatchResult result) {
		final Alias alias1 = tableFunction1.getAlias();
		final Alias alias2 = tableFunction2.getAlias();
		matchAlias(alias1, alias2, result);
		if (result.isMatch()) {
			final Pivot pivot1 = tableFunction1.getPivot();
			final Pivot pivot2 = tableFunction2.getPivot();
			matchPivot(pivot1, pivot2, result);
		}
		if (result.isMatch()) {
			final Function function1 = tableFunction1.getFunction();
			final Function function2 = tableFunction2.getFunction();
			matchFunction(function1, function2, result);
		}
	}

	private static void matchFunction(Function function1, Function function2, MatchResult result) {
		checkNulls(function1, function2, result);
		if (result.isMatch() && function1 != null) {
			final String attribute1 = function1.getAttributeName();
			final String attribute2 = function2.getAttributeName();
			checkNulls(attribute1, attribute2, result);
			if (result.isMatch() && attribute1 != null) {
				result.setResult(function1.getAttributeName().equals(function2.getAttributeName()));
			}
			if (result.isMatch()) {
				final KeepExpression keep1 = function1.getKeep();
				final KeepExpression keep2 = function2.getKeep();
				matchKeepExpression(keep1, keep2, result);
			}
			if (result.isMatch()) {
				final ExpressionList expressionList1 = function1.getParameters();
				final ExpressionList expressionList2 = function2.getParameters();
				expressionList1.accept(new ItemListVisitorComparator(expressionList2, result));
			}
		}
	}

	private static void matchKeepExpression(KeepExpression keep1, KeepExpression keep2, MatchResult result) {
		checkNulls(keep1, keep2, result);
	}

	private static void matchPivot(Pivot pivot1, Pivot pivot2, MatchResult result) {
		checkNulls(pivot1, pivot2, result);
	}

	private static void matchAlias(Alias alias1, Alias alias2, MatchResult result) {
		checkNulls(alias1, alias2, result);
		if (result.isMatch() && alias1 != null) {
			result.setResult(alias1.toString().equals(alias2.toString()));
		}
	}
}
