/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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
package com.minsait.onesait.platform.persistence.external.generator.helper;

import java.util.List;
import java.util.NoSuchElementException;

import com.minsait.onesait.platform.persistence.external.generator.model.common.ColumnRelational;
import com.minsait.onesait.platform.persistence.external.generator.model.common.Constraint;
import com.minsait.onesait.platform.persistence.external.generator.model.common.ExpandReplacement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.CreateStatement;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;

public interface SQLHelper {

	public static final String EXPAND_A_Z_A_Z = "expand\\([a-zA-Z.,_ *]*\\)";
	static final String EXPAND = "expand";

	String getValidateQuery();

	String getAllTablesStatement();

	String getTableInformationStatement(String database, String schema);

	boolean hasDatabase();

	boolean hasCrossDatabase();

	boolean hasSchema();

	String getDatabaseStatement();

	String getSchemaStatement();

	String getDatabasesStatement();

	String getSchemasStatement(String database);

	String getAllTablesStatement(String database, String schema);

	PlainSelect addLimit(final PlainSelect query, final long limit);

	PlainSelect addLimit(final PlainSelect query, final long limit, final long offset);

	String addLimit(final String query, final long limit);

	String addLimit(final String query, final long limit, final long offset);

	ColumnRelational getColumnWithSpecs(ColumnRelational col);

	String getFieldTypeString(String fieldOspType);

	Constraint getContraintWithSpecs(Constraint constaint);

	CreateStatement parseCreateStatementConstraints(CreateStatement statement);

	CreateStatement parseCreateStatementColumns(CreateStatement statement);

	CreateStatement getCreateStatementWithConstraints(CreateStatement createStatement);

	String parseGeometryFields(String query, String ontology) throws JSQLParserException;

	String getTableIndexes(String database, String schema);

	public static boolean hasExpand(String originalStatement) {
		try {
			final Statement statement = CCJSqlParserUtil.parse(originalStatement);
			if (statement instanceof Select) {
				final Select select = (Select) statement;
				final PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
				final boolean hasExpandFn = plainSelect.getSelectItems().stream()
						.filter(s -> s instanceof SelectExpressionItem).anyMatch(s -> {
							final SelectExpressionItem se = (SelectExpressionItem) s;
							if (se.getExpression() instanceof Function) {
								return EXPAND.equalsIgnoreCase(((Function) se.getExpression()).getName());
							} else {
								return false;
							}
						});
				return hasExpandFn;

			}
		} catch (final Exception e) {
			// NO-OP
		}
		return false;
	}

	public static ExpandReplacement replaceExpandInStatement(String originalStatement) {
		try {
			final Statement statement = CCJSqlParserUtil.parse(originalStatement);
			if (statement instanceof Select) {
				final Select select = (Select) statement;
				final PlainSelect plainSelect = (PlainSelect) select.getSelectBody();

				return plainSelect.getSelectItems().stream().filter(s -> s instanceof SelectExpressionItem)
						.filter(s -> {
							final SelectExpressionItem se = (SelectExpressionItem) s;
							if (se.getExpression() instanceof Function) {
								return EXPAND.equalsIgnoreCase(((Function) se.getExpression()).getName());
							} else {
								return false;
							}
						}).map(s -> {
							final SelectExpressionItem se = (SelectExpressionItem) s;
							final ExpressionList list = ((Function) se.getExpression()).getParameters();
							if (list == null) {
								return ExpandReplacement.builder()
										.statement(originalStatement.toLowerCase().replaceAll(EXPAND_A_Z_A_Z, "*"))
										.hasColumnsToExpand(false).build();
							}
							final List<String> listString = list.getExpressions().stream().map(Expression::toString)
									.toList();
							if (list.getExpressions().size() == 1 && listString.get(0).contains("*")) {
								return ExpandReplacement.builder()
										.statement(originalStatement.toLowerCase().replaceAll(EXPAND_A_Z_A_Z,
												((Function) se.getExpression()).getParameters().toString()))
										.hasColumnsToExpand(false).build();
							} else if (list.getExpressions().size() > 1
									|| (list.getExpressions().size() == 1 && !listString.contains("*"))) {

								return ExpandReplacement.builder()
										.statement(originalStatement.toLowerCase().replaceAll(EXPAND_A_Z_A_Z, "*"))
										.hasColumnsToExpand(true)
										.columnsToExpand(list.getExpressions().stream().map(e -> e.toString()).toList())
										.build();
							}
							return ExpandReplacement.builder().statement(originalStatement).hasColumnsToExpand(false)
									.build();

						}).findFirst().orElseThrow();

			}
		} catch (final Exception e) {
			// NO-OP
		}
		throw new NoSuchElementException("No expand found on query");
	}
}