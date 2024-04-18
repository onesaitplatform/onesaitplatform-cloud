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
package com.minsait.onesait.platform.persistence.mongodb.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.Test;

import com.minsait.onesait.platform.persistence.mongodb.tools.sql.Sql2NativeTool;
import com.minsait.onesait.platform.persistence.mongodb.tools.sql.UpdateSetVisitorAdapter;
import com.minsait.onesait.platform.persistence.mongodb.tools.sql.WhereExpressionVisitorAdapter;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.update.Update;

@Slf4j
public class SqlParserTest {

	private final CCJSqlParserManager parserManager = new CCJSqlParserManager();

	@Test
	public void UpdateSqlGetsTranslated() throws JSQLParserException {
		final String update = "UPDATE HelsinkiPopulation SET Helsinki.year=2018, Helsinki.population=10000 WHERE Helsinki.year > 1875 AND Helsinki.year != 2000 ";
		final Update statement = (Update) parserManager.parse(new StringReader(update));
		final List<Column> columns = statement.getColumns();

		assertEquals("Helsinki.year", columns.get(0).getFullyQualifiedName());
		assertEquals("HelsinkiPopulation", statement.getTables().get(0).getFullyQualifiedName());

		final List<Expression> expressions = statement.getExpressions();

		final StringBuilder mongoDbQuery = new StringBuilder();
		mongoDbQuery.append("db.".concat(statement.getTables().get(0).getFullyQualifiedName()).concat(".update({"));

		final StringBuilder random = new StringBuilder();

		final Expression where = statement.getWhere();
		where.accept(new WhereExpressionVisitorAdapter(random, false, 0, false, 0));
		if (random.toString().endsWith(","))
			random.deleteCharAt(random.length() - 1);
		random.append("}");

		mongoDbQuery.append(random + ",{");

		IntStream.range(0, columns.size()).forEach(i -> {
			final StringBuilder filter = new StringBuilder();
			if (i != 0)
				filter.append(",");
			filter.append("'" + columns.get(i).getFullyQualifiedName() + "':");
			expressions.get(i).accept(new UpdateSetVisitorAdapter(filter));
			mongoDbQuery.append(filter);
		});
		mongoDbQuery.append("})");
		assertEquals(mongoDbQuery.toString(),
				"db.HelsinkiPopulation.update({$and:[{'Helsinki.year':{$gt:1875}},{'Helsinki.year':{$ne:2000}}]},{'Helsinki.year':2018,'Helsinki.population':10000})");

	}

	@Test
	public void UpdateSqlGetsTranslated_withTool() {
		final String update = "UPDATE HelsinkiPopulation SET Helsinki.year=2018, Helsinki.population=10000 WHERE Helsinki.year > 1875 AND Helsinki.year != 2000 ";
		final String translatedQuery = Sql2NativeTool.translateSql(update);
		assertEquals(translatedQuery,
				"db.HelsinkiPopulation.update({$and:[{'Helsinki.year':{$gt:1875}},{'Helsinki.year':{$ne:2000}}]},{'Helsinki.year':2018,'Helsinki.population':10000})");
		log.info("{}", update);
		log.info("{}", translatedQuery);
	}

	@Test
	public void UpdateSelectWithDateTime() {
		final String update = "UPDATE DemoDate SET DemoDate.id='#5' WHERE DemoDate.date='2019-02-21T15:34:38.127Z'";
		final String translatedQuery = Sql2NativeTool.translateSql(update);
		assertTrue(translatedQuery.contains("ISODate"));
		log.info("{}", update);
		log.info("{}", translatedQuery);
	}

	@Test
	public void UpdateSelectWithOID() {
		final String update = "UPDATE DemoDate SET DemoDate.id='#5' WHERE _id='5c6694fe032037138a4b7615'";
		final String translatedQuery = Sql2NativeTool.translateSql(update);
		assertTrue(translatedQuery.contains("ObjectId"));
		log.info("{}", update);
		log.info("{}", translatedQuery);
	}

	@Test
	public void UpdateSelectWithOID_andDoubleQuotes() {
		final String update = "UPDATE DemoDate SET DemoDate.id='#5' WHERE _id=\"5c6694fe032037138a4b7615\"";
		final String translatedQuery = Sql2NativeTool.translateSql(update);
		assertTrue(translatedQuery.contains("ObjectId"));
		log.info("{}", update);
		log.info("{}", translatedQuery);
	}

	@Test
	public void UpdateSelectOR() {
		final String update = "UPDATE DemoDate SET DemoDate.id='#5' WHERE _id=\"5c6694fe032037138a4b7615\" OR _id=\"5c6694fe032037138a4b7615\"";
		final String translatedQuery = Sql2NativeTool.translateSql(update);
		assertTrue(translatedQuery.contains("$or"));
		log.info("{}", update);
		log.info("{}", translatedQuery);
	}

	@Test
	public void UpdateSelectORAND() {
		final String update = "UPDATE DemoDate SET DemoDate.id='#5' WHERE (_id=\"5c6694fe032037138a4b7615\" OR _id=\"5c6694fe032037138a4b7615\") AND DemoDate.id='#5' ";
		final String translatedQuery = Sql2NativeTool.translateSql(update);
		assertTrue(translatedQuery.contains("$or") && translatedQuery.contains("$and"));
		assertTrue(translatedQuery.lastIndexOf("$or") > translatedQuery.lastIndexOf("$and"));
		log.info("{}", update);
		log.info("{}", translatedQuery);
	}

	@Test
	public void UpdateSelectANDOR() {
		final String update = "UPDATE DemoDate SET DemoDate.id='#5' WHERE (_id=\"5c6694fe032037138a4b7615\" AND _id=\"5c6694fe032037138a4b7615\") OR DemoDate.id='#5' ";
		final String translatedQuery = Sql2NativeTool.translateSql(update);
		assertTrue(translatedQuery.contains("$or") && translatedQuery.contains("$and"));
		assertTrue(translatedQuery.lastIndexOf("$or") < translatedQuery.lastIndexOf("$and"));
		log.info("{}", update);
		log.info("{}", translatedQuery);
	}
}
