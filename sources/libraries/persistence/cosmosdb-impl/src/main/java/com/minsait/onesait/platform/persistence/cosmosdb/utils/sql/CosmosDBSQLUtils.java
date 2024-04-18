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
package com.minsait.onesait.platform.persistence.cosmosdb.utils.sql;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.microsoft.azure.documentdb.bulkexecutor.UpdateOperationBase;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.update.Update;

@Component
@Slf4j
public class CosmosDBSQLUtils {
	private static final String UPDATE = "update";
	private static final String DELETE = "delete";
	private static final String SELECT = "select";
	private static final String WHERE = "WHERE ";
	private static final CCJSqlParserManager parserManager = new CCJSqlParserManager();
	private static final Configuration configuration = Configuration.builder()
			.jsonProvider(new JacksonJsonNodeJsonProvider()).mappingProvider(new JacksonMappingProvider()).build();

	public boolean isUpdate(String query) {
		return query.toLowerCase().startsWith(UPDATE);
	}

	public boolean isDelete(String query) {
		return query.toLowerCase().startsWith(DELETE);
	}

	public boolean isSelect(String query) {
		return query.toLowerCase().startsWith(SELECT);
	}

	public String extractQueryDelete(String query) {

		try {
			final Delete st = (Delete) parserManager.parse(new StringReader(replaceDoubleQuotes(query)));
			if (st.getWhere() != null) {
				return WHERE + st.getWhere().toString();
			} else {
				return "";
			}
		} catch (final JSQLParserException e) {
			log.error("Error SQL delete while parsing", e);
			throw new DBPersistenceException("Wrong syntax in SQL delete query");
		}

	}

	public String extractQueryUpdate(String query) {
		try {
			final Update st = (Update) parserManager.parse(new StringReader(replaceDoubleQuotes(query)));
			if (st.getWhere() != null) {
				return WHERE + st.getWhere().toString();
			} else {
				return "";
			}
		} catch (final JSQLParserException e) {
			log.error("Error SQL while parsing update", e);
			throw new DBPersistenceException("Wrong syntax in SQL update query");
		}
	}

	public Map<String, Object> updateMapJson(String updateQuery) {
		try {
			final Map<String, Object> map = new HashMap<>();
			final Update st = (Update) parserManager.parse(new StringReader(replaceDoubleQuotes(updateQuery)));
			final List<Column> columns = st.getColumns();
			final List<Expression> expressions = st.getExpressions();
			for (int i = 0; i < columns.size(); i++) {
				map.put(columns.get(i).getName(false).replace("c.", "$."), expressions.get(i).toString());
			}
			return map;
		} catch (final JSQLParserException e) {
			log.error("Error SQL while parsing update", e);
			throw new DBPersistenceException("Wrong syntax in SQL update query");
		}
	}

	private String replaceDoubleQuotes(String query) {
		return query.replaceAll("(?<!\\\\)\"", "'");
	}

	public String updateJsonFromStatement(String json, String update) {
		final StringBuilder builderResult = new StringBuilder();
		updateMapJson(update).entrySet().forEach(e -> {
			final JsonNode node = JsonPath.using(configuration).parse(json).set(e.getKey(), e.getValue()).json();
			builderResult.append(node.toString());
		});
		return builderResult.toString();
	}

	public List<UpdateOperationBase> getUpdateOperationBase(String updateQuery) {
		try {
			final List<UpdateOperationBase> base = new ArrayList<>();

			final Update st = (Update) parserManager.parse(new StringReader(replaceDoubleQuotes(updateQuery)));
			final List<Column> columns = st.getColumns();
			final List<Expression> expressions = st.getExpressions();
			for (int i = 0; i < columns.size(); i++) {

				final String field = columns.get(i).getName(false).replace("c.", "");
				final CosmoDBUpdateOperationGenerator eva = new CosmoDBUpdateOperationGenerator(field);
				expressions.get(i).accept(eva);
				final com.microsoft.azure.documentdb.bulkexecutor.internal.UpdateOperation<Object> operation = eva
						.getValue();
				base.add(operation);

			}
			return base;
		} catch (final JSQLParserException e) {
			log.error("Error SQL while parsing update", e);
			throw new DBPersistenceException("Wrong syntax in SQL update query");
		}
	}

	public List<String> updateInstancesFromStatement(ArrayNode array, String update) {
		final List<String> modified = new ArrayList<>();
		array.forEach(j -> {
			final Set<Entry<String, UpdateOperation>> entrySet = getUpdateOperationsMap(update).entrySet();
			j = applyOperationsToJson(j, entrySet);
			modified.add(j.toString());
		});
		return modified;
	}

	private JsonNode applyOperationsToJson(JsonNode j, Set<Entry<String, UpdateOperation>> entrySet) {
		for (final Entry<String, UpdateOperation> e : entrySet) {
			final String path = "/" + e.getKey().replace(".", "/");
			final UpdateOperation operation = e.getValue();
			switch (operation.getType()) {
			case APPEND:
				j = JsonPath.using(configuration).parse(j.toString()).add("$." + e.getKey(), operation.getValue())
						.json();
				break;
			case APPEND_MULTIPLE:
				final ArrayNode values = (ArrayNode) operation.getValue();
				for (final JsonNode n : values) {
					j = JsonPath.using(configuration).parse(j.toString()).add("$." + e.getKey(), n).json();
				}
				break;
			case UNSET:
				j = JsonPath.using(configuration).parse(j.toString()).delete("$." + e.getKey()).json();
				break;
			case SET:
			default:
				if (j.at(path).isMissingNode()) {
					final String[] pathSplit = e.getKey().split("\\.");
					if (pathSplit.length > 1) {
						final String prefix = IntStream.range(0, pathSplit.length - 1).mapToObj(i -> pathSplit[i])
								.collect(Collectors.joining("."));
						j = JsonPath.using(configuration).parse(j.toString())
								.put("$." + prefix, pathSplit[pathSplit.length - 1], e.getValue().getValue()).json();
					} else {
						j = JsonPath.using(configuration).parse(j.toString())
								.put("$.", pathSplit[pathSplit.length - 1], e.getValue().getValue()).json();
					}
				} else {
					j = JsonPath.using(configuration).parse(j.toString()).set("$." + e.getKey(), operation.getValue())
							.json();
				}
				break;
			}

		}
		return j;
	}

	public Map<String, UpdateOperation> getUpdateOperationsMap(String updateQuery) {
		try {
			final Map<String, UpdateOperation> map = new HashMap<>();
			final Update st = (Update) parserManager.parse(new StringReader(replaceDoubleQuotes(updateQuery)));
			final List<Column> columns = st.getColumns();
			final List<Expression> expressions = st.getExpressions();
			for (int i = 0; i < columns.size(); i++) {
				final CosmosDBExpressionVisitorAdapter adapter = new CosmosDBExpressionVisitorAdapter();
				expressions.get(i).accept(adapter);
				// TO-DO push operations
				map.put(columns.get(i).getName(false).replace("c.", ""), adapter.getResultOperation());
			}
			return map;
		} catch (final JSQLParserException e) {
			log.error("Error SQL while parsing update", e);
			throw new DBPersistenceException("Wrong syntax in SQL update query");
		}
	}

}
