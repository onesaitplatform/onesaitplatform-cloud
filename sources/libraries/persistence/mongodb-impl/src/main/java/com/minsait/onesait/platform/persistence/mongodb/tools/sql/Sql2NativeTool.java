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
package com.minsait.onesait.platform.persistence.mongodb.tools.sql;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.vincentrussell.query.mongodb.sql.converter.QueryConverter;
import com.github.vincentrussell.query.mongodb.sql.converter.dictionary.Dictionary;
import com.github.vincentrussell.query.mongodb.sql.converter.exception.ParseException;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;

@Component
@Slf4j
public class Sql2NativeTool {

	private static final String UPDATE = "update";
	private static final String DELETE = "delete";
	private static final String SELECT = "select";
	private static final CCJSqlParserManager parserManager = new CCJSqlParserManager();
	private static final Pattern offsetlimitPattern = Pattern.compile("[ ]+offset[ ]+(\\d+)[ ]+limit[ ]+(\\d+)[ ]*",
			Pattern.CASE_INSENSITIVE);
	private static QueryConverter.Builder qc;

	@Autowired
	ConfigurationService configurationService;

	@PostConstruct
	public void init() {
		final String dictionary = configurationService.getConfiguration("MASTER-Configuration-22").getYmlConfig();
		final String utilsCode = "public final class UtilsCode {}";// configurationService.getConfiguration("MASTER-Configuration-23").getYmlConfig();
		final Dictionary d = new Dictionary();
		d.loadJSON(dictionary);
		qc = new QueryConverter.Builder().dictionary(d).aggregationAllowDiskUse(true);
	}

	// public static String translateSql(String query) {
	// log.debug("Query to be translated {}", query);
	// String result = null;
	// try {
	// if (query.trim().toLowerCase().startsWith(UPDATE)) {
	// result = translateUpdate(replaceDoubleQuotes(query));
	// } else if (query.trim().toLowerCase().startsWith(DELETE)) {
	// result = translateDelete(replaceDoubleQuotes(query));
	// } else if (query.trim().toLowerCase().startsWith(SELECT)) {
	// result = translateSelect(query);
	// }
	// if(result == null) {
	// throw new DBPersistenceException("Unsupported SQL operation");
	// } else {
	// log.debug("Query translated to :{}", result);
	// return result;
	// }
	// } catch (final JSQLParserException e) {
	// log.error("Error executing query", e);
	// throw new DBPersistenceException("Syntax Error");
	// } catch (final DBPersistenceException e) {
	// log.error("Error executing query", e);
	// throw e;
	// } catch(final Exception e) {
	// log.error("Error executing query", e);
	// throw new DBPersistenceException("Invalid SQL Syntax");
	// }
	//
	//
	// }

	public static String translateSql(String query) {
		if (log.isDebugEnabled()) {
			log.debug("Query to be translated {}", query);
		}		
		String result = null;
		try {
			if (query.trim().toLowerCase().startsWith(UPDATE)) {
				result = translateUpdate(replaceDoubleQuotes(query));
			}

		} catch (final JSQLParserException e) {
			log.error("Error executing query", e);
			throw new DBPersistenceException("{\"error\": \"Syntax Error, try the following Syntax-> UPDATE table_name "
					+ "SET column1 = value1, column2 = value2, ... " + "WHERE condition;\" }");
		} catch (final DBPersistenceException e) {
			log.error("Error executing query", e);
			throw e;
		} catch (final Exception e) {
			log.error("Error executing query", e);
			throw new DBPersistenceException("Invalid SQL Syntax");
		}

		try {
			if (query.trim().toLowerCase().startsWith(DELETE)) {
				result = translateDelete(replaceDoubleQuotes(query));
			}

		} catch (final JSQLParserException e) {
			log.error("Error executing query", e);

			throw new DBPersistenceException(
					"{\"error\": \"Syntax Error. try the following Syntax-> DELETE FROM table_name WHERE condition;\" }");
		} catch (final DBPersistenceException e) {
			log.error("Error executing query", e);
			throw e;
		} catch (final Exception e) {
			log.error("Error executing query", e);
			throw new DBPersistenceException("Invalid SQL Syntax");
		}

		try {
			if (query.trim().toLowerCase().startsWith(SELECT)) {
				result = translateSelect(query);
			}
		} catch (final ParseException e) {
			log.error("Error executing query", e);

			throw new DBPersistenceException(
					"{\"error\": \"Syntax Error. try the following Syntax-> SELECT * FROM table_name WHERE condition;\" }",e);

		} catch (final DBPersistenceException e) {
			log.error("Error executing query", e);
			throw e;
		} catch (final Exception e) {
			log.error("Error executing query", e);
			throw new DBPersistenceException("Invalid SQL Syntax", e);
		}

		return result;

	}

	public static List<String> translateSql(List<String> queries) {
		final List<String> translatedQueries = new ArrayList<>();
		try {
			for (final String query : queries) {
				if (query.trim().toLowerCase().startsWith(UPDATE)) {
					translatedQueries.add(translateUpdate(replaceDoubleQuotes(query)));
				} else if (query.trim().toLowerCase().startsWith(DELETE)) {
					translatedQueries.add(translateDelete(replaceDoubleQuotes(query)));
				} else if (query.trim().toLowerCase().startsWith(SELECT)) {
					translatedQueries.add(translateSelect(query));
				}
			}
			return translatedQueries;
		} catch (final JSQLParserException | ParseException e) {
			throw new DBPersistenceException("Invalid SQL syntax", e);
		}
	}

	private static synchronized String translateSelect(String query) throws ParseException {
		try {
			final QueryConverter queryConverter = qc.sqlString(preprocessQuery(query)).build();
			final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			queryConverter.write(byteArrayOutputStream);
			return byteArrayOutputStream.toString(StandardCharsets.UTF_8.name());
		} catch (final ParseException e) {
			throw e;
		} catch (final IOException e) {
			throw new DBPersistenceException(e.getMessage(), e);
		}

	}

	private static String translateUpdate(String query) throws JSQLParserException {
		final StringBuilder mongoDbQuery = new StringBuilder();
		final Update statement = (Update) parserManager.parse(new StringReader(query));
		final List<UpdateSet> updateSet = statement.getUpdateSets();

		mongoDbQuery.append("db.".concat(statement.getTable().getFullyQualifiedName()).concat(".update("));
		final Expression where = statement.getWhere();
		if (null != where) {
			where.accept(new WhereExpressionVisitorAdapter(mongoDbQuery, false, 0, false, 0));
			removeIfLastCharacterIsComma(mongoDbQuery);
			mongoDbQuery.append(",{");
		} else {
			mongoDbQuery.append("{},{");
		}

		// for special ops such as $push define variable
		// currently we only support $push operations
		final StringBuilder spOps = new StringBuilder();
		final StringBuilder update = new StringBuilder();
		IntStream.range(0, updateSet.size()).forEach(i -> {
			final List<Column> columns = updateSet.get(i).getColumns();
			final List<Expression> expressions = updateSet.get(i).getExpressions();
			IntStream.range(0, columns.size()).forEach(j -> {
				final StringBuilder filter = new StringBuilder();
				if (i != 0 ) {
					filter.append(",");
				} else if(j != 0) {
					filter.append(",");
				}
				filter.append("'" + columns.get(j).getFullyQualifiedName() + "':");
				//This only supports set expressions with one value, which is 99.9% of the cases
				expressions.get(0).accept(new UpdateSetVisitorAdapter(filter, j));
				if (filter.indexOf("$push") != -1) {
					spOps.append(filter);
				} else {
					update.append(filter);
				}
			});
		});
		if (spOps.length() > 0) {
			if (spOps.charAt(0) == ',') {
				spOps.deleteCharAt(0);
			}
			mongoDbQuery.append(spOps);
			if (update.length() > 0) {
				if (update.charAt(0) == ',') {
					update.deleteCharAt(0);
				}
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

		mongoDbQuery.append("db.".concat(statement.getTable().getFullyQualifiedName()).concat(".remove("));

		final Expression where = statement.getWhere();

		if (null != where) {
			where.accept(new WhereExpressionVisitorAdapter(mongoDbQuery, false, 0, false, 0));
			removeIfLastCharacterIsComma(mongoDbQuery);
			mongoDbQuery.append(")");
		} else {
			mongoDbQuery.append("{})");
		}
		return mongoDbQuery.toString();
	}

	// For legacy compatibility: change "offset limit" query to "limit offset"
	private static String preprocessQuery(String sql) {
		return offsetlimitPattern.matcher(sql.replaceAll("[\n\t\r]", " ")).replaceAll(" limit $2 offset $1 ");
	}

	private static String replaceDoubleQuotes(String query) {
		return query.replaceAll("(?<!\\\\)\"", "'");
	}

	public static void removeIfLastCharacterIsComma(StringBuilder sb) {
		if (sb.toString().endsWith(",")) {
			sb.deleteCharAt(sb.length() - 1);
		}
	}
}
