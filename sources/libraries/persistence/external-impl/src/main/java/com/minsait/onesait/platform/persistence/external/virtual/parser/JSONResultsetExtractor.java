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
package com.minsait.onesait.platform.persistence.external.virtual.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.minsait.onesait.platform.config.services.ontology.dto.OntologyRelation;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataService;
import com.minsait.onesait.platform.multitenant.util.BeanUtil;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;

@Slf4j
public class JSONResultsetExtractor implements ResultSetExtractor<List<String>> {

	// JSON CHILD CLASS ATTRIBUTES
	private final String originalStatement;
	private String mainEntity;
	private boolean relatedTables;
	private final Set<String> mainTableColumnNames = new HashSet<>();
	private final Map<String, Set<String>> secondaryTableAttributes = new HashMap<>();
	private final Set<String> secondaryTables = new HashSet<>();
	private final Map<String, Set<OntologyRelation>> relationsMap = new HashMap<>();
	private final Map<String, String> entityToTable = new HashMap<>();
	private final Map<String, String> tableToEntity = new HashMap<>();
	private final OntologyDataService ontologyDataService;

	private final String statement;
	private boolean ignoreRelations = false;

	public JSONResultsetExtractor(String statement, String originalStatement) {
		this.statement = statement;
		this.originalStatement = originalStatement;
		this.ontologyDataService = BeanUtil.getBean(OntologyDataService.class);
	}

	public JSONResultsetExtractor(String statement, String originalStatement, boolean ignoreRelations) {
		this.statement = statement;
		this.originalStatement = originalStatement;
		this.ontologyDataService = BeanUtil.getBean(OntologyDataService.class);
		this.ignoreRelations = ignoreRelations;
	}

	@Override
	public List<String> extractData(ResultSet rs) throws SQLException, DataAccessException {
		Select statement = null;
		try {
			statement = (Select) CCJSqlParserUtil.parse(this.statement);
		} catch (final JSQLParserException e) {
			log.error("Error executing query. {}", e);
		}
		final TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
		final String table = tablesNamesFinder.getTableList(statement).get(0);
		setRelatedTables();
		if (rs != null) {
			List<JSONObject> result = new ArrayList<JSONObject>();
			if (relatedTables) {
				proccessResultSetRelatedTables(rs, result, table);
				result = getRelatedResult(result);
			} else {
				proccessResultSet(rs, result, table);
			}
			return result.stream().map(j -> j.toString()).collect(Collectors.toList());
		}
		return new ArrayList<String>();

	}

	private void proccessResultSetRelatedTables(ResultSet rs, List<JSONObject> result, String table)
			throws JSONException, SQLException {
		final ResultSetMetaData rsmd = rs.getMetaData();
		while (rs.next()) {
			final int numColumns = rsmd.getColumnCount();
			JSONObject mainObj = new JSONObject();
			final Map<String, JSONObject> childObjects = new HashMap<>();
			JSONObject objInter = new JSONObject();
			String currentTable = null;
			for (int i = 1; i < numColumns + 1; i++) {
				final String columnName = rsmd.getColumnLabel(i);
				final String tableName = rsmd.getTableName(i);
				if (tableName == null || tableName.equals("")) {
					// Funciones como count, sum...
					mainTableColumnNames.add(columnName);
					this.extractResultSetRowField(rs, tableName, i, rsmd.getColumnType(i), columnName, mainObj);
				} else {
					if (currentTable == null) {
						currentTable = tableName;
					}
					if (!secondaryTableAttributes.containsKey(tableName)) {
						secondaryTableAttributes.put(tableName, new HashSet<>());
					}
					if (!entityToTable.get(mainEntity).equals(tableName) && !currentTable.equals(tableName)) {
						if (objInter.length() > 0) {
							// añado el hijo al padre y sigo con mas hijos
							childObjects.put(currentTable, objInter);
//						mainObj.put(currentTable, objInter);
							objInter = new JSONObject();
						}
						currentTable = tableName;
						secondaryTableAttributes.get(tableName).add(columnName);
						this.extractResultSetRowField(rs, tableName, i, rsmd.getColumnType(i), columnName, objInter);
					} else if (!entityToTable.get(mainEntity).equals(tableName) && currentTable.equals(tableName)) {
						secondaryTableAttributes.get(tableName).add(columnName);
						this.extractResultSetRowField(rs, tableName, i, rsmd.getColumnType(i), columnName, objInter);
					} else if (entityToTable.get(mainEntity).equals(tableName)) {
						mainTableColumnNames.add(columnName);
						this.extractResultSetRowField(rs, tableName, i, rsmd.getColumnType(i), columnName, mainObj);
					}
				}
			}

			if (objInter.length() > 0) {
				childObjects.put(currentTable, objInter);
			}

			// COLOCAR EN ORDEN
			if (mainObj.length() > 0) {
				preMountJsonObject(mainObj, childObjects);
				result.add(mainObj);
				// resultJson.add(obj.toString());
			} else if (mainObj.length() <= 0 && objInter.length() > 0) {
				mainObj = objInter;
				result.add(mainObj);
			}
		}
	}

	private void preMountJsonObject(JSONObject mainObject, Map<String, JSONObject> childObjects) {
		mountJsonObject(mainEntity, mainObject, childObjects);
	}

	private void mountJsonObject(String entity, JSONObject mainObject, Map<String, JSONObject> childObjects) {
		relationsMap.get(entity).forEach(r -> {
			final String dstEntity = r.getDstOntology();
			if (relationsMap.get(dstEntity) == null && childObjects.containsKey(entityToTable.get(dstEntity))) {
				// LO AÑADO DIRECTAMENTE
				mainObject.put(dstEntity, childObjects.get(entityToTable.get(dstEntity)));
			} else if (relationsMap.get(dstEntity) != null && childObjects.containsKey(entityToTable.get(dstEntity))) {
				// RECURSIVO ANTES DE AÑADIR
				final JSONObject child = childObjects.get(entityToTable.get(dstEntity));
				mountJsonObject(dstEntity, child, childObjects);
				// jsonObjet = function(dstEntity,childObjects)
				// mainObject.put(dstEntity,jsonObject);
				mainObject.put(dstEntity, childObjects.get(entityToTable.get(dstEntity)));
			}
		});
//		childObjects.entrySet().forEach(e -> mainObject.put(e.getKey(), e.getValue()));
	}

	private void proccessResultSet(ResultSet rs, List<JSONObject> result, String table)
			throws JSONException, SQLException {
		final ResultSetMetaData rsmd = rs.getMetaData();
		while (rs.next()) {
			final int numColumns = rsmd.getColumnCount();
			final JSONObject mainObj = new JSONObject();
			for (int i = 1; i < numColumns + 1; i++) {
				final String columnName = rsmd.getColumnLabel(i);
				String tableName = rsmd.getTableName(i);
				if (tableName == null || tableName.equals("")) {
					tableName = table;
				}

				this.extractResultSetRowField(rs, tableName, i, rsmd.getColumnType(i), columnName, mainObj);

			}
			if (mainObj.length() > 0) {
				result.add(mainObj);
			}
		}
	}

	private List<JSONObject> getRelatedResult(List<JSONObject> list) {
		return preGetRelatedResult(mainEntity, mainTableColumnNames, list);
	}

	private List<JSONObject> preGetRelatedResult(String entity, Set<String> tableColumnNames, List<JSONObject> list) {
		if (tableColumnNames.isEmpty()) {
			return list.stream().collect(Collectors.toList());
		}
		// TO-DO saber si es 1-n o 1-1
		final Map<String, List<JSONObject>> result = list.stream().collect(Collectors.groupingBy(o -> {
			final JSONObject n = new JSONObject();
			tableColumnNames.forEach(c -> {
				n.put(c, o.get(c));
			});
			return n.toString();
		}));
		// SIEMPRE COMO SI FUESE 1-N de momento
		return result.entrySet().stream().map(e -> {
			final JSONObject parentObj = new JSONObject(e.getKey());
			relationsMap.get(entity).forEach(rel -> {
				final String entityTable = rel.getDstOntology();
				switch (rel.getRelationType()) {
				case ONE_TO_ONE:
				case MANY_TO_ONE:
					List<JSONObject> arrayCaseA = new ArrayList<>();
					for (final JSONObject t : e.getValue()) {
						if (t.has(entityTable)) {
							// EL OBJECTO DIRECTO
//							JSONObject value = (JSONObject) t.get(entityTable);
							arrayCaseA.add((JSONObject) t.get(entityTable));
						}
					}
					if (relationsMap.get(rel.getDstOntology()) != null) {
						arrayCaseA = preGetRelatedResult(rel.getDstOntology(),
								secondaryTableAttributes.get(entityToTable.get(rel.getDstOntology())), arrayCaseA);
					}
					if (!arrayCaseA.isEmpty()) {
						parentObj.put(entityTable, arrayCaseA.iterator().next());
					}
					break;
				case MANY_TO_MANY:
				case ONE_TO_MANY:
					List<JSONObject> arrayCaseB = new ArrayList<>();
					for (final JSONObject t : e.getValue()) {
						if (t.has(entityTable)) {
							arrayCaseB.add((JSONObject) t.get(entityTable));
						}
					}
					if (relationsMap.get(rel.getDstOntology()) != null) {
						arrayCaseB = preGetRelatedResult(rel.getDstOntology(),
								secondaryTableAttributes.get(entityToTable.get(rel.getDstOntology())), arrayCaseB);
					}
					if (!arrayCaseB.isEmpty()) {
						parentObj.put(entityTable, arrayCaseB);
					}
					break;
				default:
					break;
				}

			});

			return parentObj;
		}).collect(Collectors.toList());
	}

	private JSONObject extractResultSetRowField(ResultSet rs, String table, int rowIndex, int rowType,
			String columnName, JSONObject obj) throws JSONException, SQLException {

		if (rowType == java.sql.Types.VARCHAR) {
			final String value = rs.getString(rowIndex);
			getRecurrence(table, columnName, obj, value, false);
		} else if (rowType == java.sql.Types.BOOLEAN) {
			final Boolean value = rs.getBoolean(rowIndex);
			getRecurrence(table, columnName, obj, value, false);

		} else if (rowType == java.sql.Types.NUMERIC || rowType == java.sql.Types.BIGINT) {

			final Double dValue = rs.getDouble(rowIndex);
			if (dValue != null) {
				if ((dValue == Math.floor(dValue)) && !Double.isInfinite(dValue) && Integer.MAX_VALUE >= dValue
						&& Integer.MIN_VALUE <= dValue) {
					final int value = rs.getInt(rowIndex);
					getRecurrence(table, columnName, obj, value, false);
				} else {
					getRecurrence(table, columnName, obj, dValue.doubleValue(), false);
				}
			}
		} else if (rowType == java.sql.Types.INTEGER || rowType == java.sql.Types.SMALLINT) {
			final Integer value = rs.getObject(rowIndex) != null ? rs.getInt(rowIndex) : null;
			getRecurrence(table, columnName, obj, value, false);
		} else if (rowType == java.sql.Types.DOUBLE) {
			getRecurrence(table, columnName, obj, rs.getDouble(rowIndex), false);
		} else if (rowType == java.sql.Types.FLOAT) {
			getRecurrence(table, columnName, obj, rs.getFloat(rowIndex), false);
		} else if (rowType == java.sql.Types.DATE) {
			final Date value = rs.getDate(rowIndex);
			getRecurrence(table, columnName, obj, value, false);
		} else if (rowType == java.sql.Types.TIMESTAMP) {
			String value = null;
			try {
				final Timestamp t = rs.getTimestamp(rowIndex);
				if (t != null) {
					// value = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new
					// Date(t.getTime()));
					value = t.toString();
				}
			} catch (final IllegalArgumentException e) { // for oracle bug in timestamp(6) for nanoseconds
				if ("nanos > 999999999 or < 0".equals(e.getMessage())) { // we check only the error of oracle msg nanos
																			// > 999999999 or < 0
					value = rs.getString(rowIndex);
				} else {
					throw e;
				}
			}
			getRecurrence(table, columnName, obj, value, true);
		} else if (rowType == java.sql.Types.CLOB) {
			final Clob value = rs.getClob(rowIndex);
			getRecurrence(table, columnName, obj,
					new JSONObject(this.getContentFromInputStream(value.getAsciiStream())), false);
		} else if (rowType == java.sql.Types.BLOB) {
			final Blob value = rs.getBlob(rowIndex);
			getRecurrence(table, columnName, obj, value, false);
		} else if (rowType == java.sql.Types.NVARCHAR) {
			final String value = rs.getNString(rowIndex);
			getRecurrence(table, columnName, obj, value, false);
		} else if (rowType == java.sql.Types.TINYINT) {
			final int value = rs.getInt(rowIndex);
			getRecurrence(table, columnName, obj, value, false);
		} else if (rowType == java.sql.Types.SMALLINT) {
			final int value = rs.getInt(rowIndex);
			getRecurrence(table, columnName, obj, value, false);
		} else if (rowType == java.sql.Types.ARRAY) {
			getRecurrence(table, columnName, obj, rs.getArray(rowIndex), false);
		} else {
			final Object value = rs.getObject(rowIndex);
			getRecurrence(table, columnName, obj, value, false);
		}

		return obj;
	}

	private String getContentFromInputStream(InputStream is) {

		final StringBuilder sb = new StringBuilder();

		String line;
		try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

		} catch (final IOException e) {
			log.error(e.getMessage());
		}

		return sb.toString();

	}

	private void getRecurrence(String tableName, String columnName, JSONObject obj, Object value, boolean isDate)
			throws JSONException {

		final JSONObject o2 = new JSONObject();
		final JSONObject o = new JSONObject();

		if (value != null && isDate) {
			// JSONObject aux = new JSONObject().put("$date", value);
			// o2 = new JSONObject().put(columnName, aux);
			o2.put(columnName, value);
		} else if (value != null) {
			o2.put(columnName, value);
		} else {
			o2.put(columnName, JSONObject.NULL);
		}
		checkifExistfield(o2, obj);
	}

	private JSONObject checkifExistfield(JSONObject source, JSONObject target) throws JSONException {

		if (JSONObject.getNames(source) != null) {
			for (final String key : JSONObject.getNames(source)) {
				final Object value = source.get(key);
				if (!target.has(key)) {
					target.put(key, value);
				} else {
					if (value instanceof JSONObject) {
						final JSONObject valueJson = (JSONObject) value;
						checkifExistfield(valueJson, target.getJSONObject(key));
					} else {
						target.put(key, value);
					}
				}
			}
		}
		return target;
	}

	private void setRelatedTables() {
		// TO-DO create hierarchy for children
		if (!ignoreRelations) {
			Select st = null;
			try {
				st = (Select) CCJSqlParserUtil.parse(this.originalStatement);
				final List<String> entities = new TablesNamesFinder().getTableList(st);
				final String mainEntity = entities.get(0);
				try {
					final Set<OntologyRelation> relations = ontologyDataService.getOntologyReferences(mainEntity);
					if (!relations.isEmpty()) {
						relationsMap.put(mainEntity, relations.stream()
								.filter(or -> entities.contains(or.getDstOntology())).collect(Collectors.toSet()));
						this.mainEntity = mainEntity;
						relatedTables = true;
						entities.remove(0);
						entities.forEach(e -> {
							try {
								final Set<OntologyRelation> rs = ontologyDataService.getOntologyReferences(e);
								if (!rs.isEmpty()) {
									relationsMap.put(e, rs.stream().filter(or -> entities.contains(or.getDstOntology()))
											.collect(Collectors.toSet()));
								}
								final String table = ontologyDataService.getTableForEntity(e);
								entityToTable.put(e, table);
								tableToEntity.put(table, e);
							} catch (final IOException ex) {
								log.error("No relations on {}", e, ex);
							}
						});
						secondaryTables.addAll(entities);
						final String mainTable = ontologyDataService.getTableForEntity(mainEntity);
						entityToTable.put(mainEntity, mainTable);
						tableToEntity.put(mainTable, mainEntity);

					}
				} catch (final IOException e) {
					relatedTables = false;
				}
			} catch (final JSQLParserException e) {
				log.error("Error executing query. {}", e);
			}
		}
	}
}
