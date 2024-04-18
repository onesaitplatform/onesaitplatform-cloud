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
package com.minsait.onesait.platform.persistence.mongodb.tools.sql;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.IntStream;

import com.github.vincentrussell.query.mongodb.sql.converter.ParseException;
import com.github.vincentrussell.query.mongodb.sql.converter.QueryConverter;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.update.Update;

public class Sql2NativeTool {

	private static final String UPDATE = "update";
	private static final String DELETE = "delete";
	private static final String SELECT = "select";
	private static final CCJSqlParserManager parserManager = new CCJSqlParserManager();

	private Sql2NativeTool() {
		throw new IllegalStateException("Utility class");
	}

	public static String translateSql(String query) {

		try {
			if (query.trim().toLowerCase().startsWith(UPDATE))
				return translateUpdate(replaceDoubleQuotes(query));
			else if (query.trim().toLowerCase().startsWith(DELETE))
				return translateDelete(replaceDoubleQuotes(query));
			else if (query.trim().toLowerCase().startsWith(SELECT))
				return translateSelect(query);

		} catch (final JSQLParserException e) {
			throw new DBPersistenceException("Invalid SQL syntax");
		}

		throw new DBPersistenceException("Unsupported SQL operation");
	}

	private static String translateUpdate(String query) throws JSQLParserException {
		final StringBuilder mongoDbQuery = new StringBuilder();
		final Update statement = (Update) parserManager.parse(new StringReader(query));
		final List<Column> columns = statement.getColumns();
		final List<Expression> expressions = statement.getExpressions();

		mongoDbQuery.append("db.".concat(statement.getTables().get(0).getFullyQualifiedName()).concat(".update({"));
		final Expression where = statement.getWhere();
		if (null != where)
			where.accept(new WhereExpressionVisitorAdapter(mongoDbQuery, false, 0, false, 0));
		removeIfLastCharacterIsComma(mongoDbQuery);
		mongoDbQuery.append("},{");

		// for special ops such as $push define variable
		// currently we only support $push operations
		final StringBuilder spOps = new StringBuilder();
		final StringBuilder update = new StringBuilder();
		IntStream.range(0, columns.size()).forEach(i -> {
			final StringBuilder filter = new StringBuilder();
			if (i != 0)
				filter.append(",");
			filter.append("'" + columns.get(i).getFullyQualifiedName() + "':");
			expressions.get(i).accept(new UpdateSetVisitorAdapter(filter, i));
			if (filter.indexOf("$push") != -1)
				spOps.append(filter);
			else
				update.append(filter);
		});
		if (spOps.length() > 0) {
			if (spOps.charAt(0) == ',')
				spOps.deleteCharAt(0);
			mongoDbQuery.append(spOps);
			if (update.length() > 0) {
				if (update.charAt(0) == ',')
					update.deleteCharAt(0);
				mongoDbQuery.append(",$set:{" + update + "}");
			}
		} else {
			mongoDbQuery.append(update);
		}
		mongoDbQuery.append("})");
		return mongoDbQuery.toString();
	}

	private static String translateDelete(String query) throws JSQLParserException {
		final StringBuilder mongoDbQuery = new StringBuilder();

		final Delete statement = (Delete) parserManager.parse(new StringReader(query));
		mongoDbQuery.append("db.".concat(statement.getTable().getFullyQualifiedName()).concat(".remove({"));
		final Expression where = statement.getWhere();
		if (null != where)
			where.accept(new WhereExpressionVisitorAdapter(mongoDbQuery, false, 0, false, 0));

		removeIfLastCharacterIsComma(mongoDbQuery);

		mongoDbQuery.append("})");
		return mongoDbQuery.toString();
	}

	private static String translateSelect(String query) {
		try {
			final QueryConverter queryConverter = new QueryConverter(query);
			final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			queryConverter.write(byteArrayOutputStream);
			return byteArrayOutputStream.toString(StandardCharsets.UTF_8.name());
		} catch (final ParseException | IOException e) {
			throw new DBPersistenceException(e.getMessage(), e);
		}

	}

	private static String replaceDoubleQuotes(String query) {
		return query.replaceAll("(?<!\\\\)\"", "'");
	}

	public static void removeIfLastCharacterIsComma(StringBuilder sb) {
		if (sb.toString().endsWith(","))
			sb.deleteCharAt(sb.length() - 1);
	}
}
