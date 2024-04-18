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
package com.minsait.onesait.platform.persistence.external.generator;

import org.junit.Test;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.TableFunction;
import net.sf.jsqlparser.util.TablesNamesFinder;

public class TestSQL {

	@Test
	public void test() throws JSQLParserException {
		final Statement statement = CCJSqlParserUtil.parse("SELECT EXPAND(users, ) FROM app_user  as u");
		if (statement instanceof Select) {
			final Select select = (Select) statement;
			final PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
			final boolean hasExpandFn = plainSelect.getSelectItems().stream()
					.filter(s -> s instanceof SelectExpressionItem).anyMatch(s -> {
						final SelectExpressionItem se = (SelectExpressionItem) s;
						if (se.getExpression() instanceof Function) {
							return "expand"
									.equalsIgnoreCase(((Function) se.getExpression()).getParameters().toString());
						} else {
							return false;
						}
					});
			System.out.print("Has expand fn :" + hasExpandFn);

		}
//		CCJSqlParserUtil.parse("SELECT EXPAND(user_id), u FROM user_pg  as u");
	}

	@Test
	public void test2() throws JSQLParserException {
		final Statement statement = CCJSqlParserUtil.parse("SELECT EXPAND(user_id), u FROM user_pg  as u");

		final TablesNamesFinder tablesNamesFinder = new TablesNamesFinder() {
			@Override
			public void visit(Column tableColumn) {
				System.out.println("column = " + tableColumn);
			}

			@Override
			public void visit(Function function) {
				System.out.println("function = " + function.getName());
				super.visit(function);
			}

			@Override
			public void visit(Table tableName) {
				System.out.println("table = " + tableName.getFullyQualifiedName());
				super.visit(tableName);
			}

			@Override
			public void visit(TableFunction valuesList) {
				System.out.println("table function = " + valuesList.getFunction().getName());
				super.visit(valuesList);
			}
		};

		System.out.println("all extracted tables=" + tablesNamesFinder.getTableList(statement));
	}
}
