/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
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
package com.minsait.onesait.platform.persistence.external.virtual.helper;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.ComparisonOperator;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;

@Component("OracleVirtualOntologyHelper")
@Slf4j
public class OracleVirtualOntologyHelper implements VirtualOntologyHelper {

	private static final String LIST_TABLES_QUERY = "SELECT table_name FROM user_tables";
	private static final String ROWNUM_STR = "ROWNUM";

	@Override
	public String getAllTablesStatement() {
		return LIST_TABLES_QUERY;
	}

	@Override
	public String addLimit(String query, final long queryLimit) {
		try {
			Select statement = (Select) CCJSqlParserUtil.parse(query);
			if (!query.toUpperCase().contains(ROWNUM_STR) && !query.toUpperCase().contains("COUNT(*)")) {
				log.trace("Query limited to: {} records", queryLimit);

				MinorThan limit = new MinorThan();
				limit.setLeftExpression(new LongValue(ROWNUM_STR));
				limit.setRightExpression(new LongValue(queryLimit + 1));

				Expression originalWhereExpr = ((PlainSelect) statement.getSelectBody()).getWhere();
				if (originalWhereExpr != null) {
					AndExpression andRowNum = new AndExpression(originalWhereExpr, limit);
					((PlainSelect) statement.getSelectBody()).setWhere(andRowNum);
				} else {
					((PlainSelect) statement.getSelectBody()).setWhere(limit);
				}

				query = statement.toString();

			} else if (!query.toUpperCase().contains("COUNT(*)")) {
				ExpressionDeParser expressionDeparser = new ExpressionDeParser() {
					@Override
					public void visit(AndExpression andExpression) {
						validateRowNum(andExpression.getLeftExpression());
						validateRowNum(andExpression.getRightExpression());
						super.visit(andExpression);
					}

					private void validateRowNum(Expression expr) {
						if ((expr instanceof MinorThan || expr instanceof EqualsTo)
								&& ((ComparisonOperator) expr).getLeftExpression().toString().equals(ROWNUM_STR)) {
							int currentLimit = Integer
									.parseInt((((ComparisonOperator) expr).getRightExpression()).toString());
							if (expr instanceof MinorThan && currentLimit - 1 > queryLimit) {
								((ComparisonOperator) expr).setRightExpression(new LongValue(queryLimit + 1));
							}
							if (expr instanceof EqualsTo && currentLimit > queryLimit) {
								((ComparisonOperator) expr).setRightExpression(new LongValue(queryLimit));
							}

						}

					}

				};

				StringBuilder buffer = new StringBuilder();
				SelectDeParser deparser = new SelectDeParser(expressionDeparser, buffer);
				expressionDeparser.setSelectVisitor(deparser);
				expressionDeparser.setBuffer(buffer);
				statement.getSelectBody().accept(deparser);

				query = buffer.toString();

			}
		} catch (JSQLParserException e) {
			log.error("Error adding rownum to query. {}", e);
		}
		return query;
	}

	@Override
	public String addLimit(String query, long limit, long offset) {
		/**
		 * 
		 * SELECT * FROM ( SELECT tmp.*, rownum rn FROM ( SELECT * FROM sales ORDER BY
		 * sale_date DESC ) tmp WHERE rownum <= 20 ) WHERE rn > 10
		 * 
		 */
		return this.addLimit(query, limit);
	}

}
