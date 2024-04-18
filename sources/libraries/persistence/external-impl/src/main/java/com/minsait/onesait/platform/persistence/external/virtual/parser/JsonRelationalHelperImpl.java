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
package com.minsait.onesait.platform.persistence.external.virtual.parser;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONObject;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.insert.Insert;

@Component("JsonRelationalHelperImpl")
@Lazy
@Slf4j
public class JsonRelationalHelperImpl implements JsonRelationalHelper {

	// String ontName = nativeQuery.substring(nativeQuery.indexOf("db.") + 3,
	// nativeQuery.indexOf(qType));
	//
	// // String query=nativeQuery.substring(nativeQuery.indexOf(".find(")+6,
	// // nativeQuery.lastIndexOf(")"));
	//
	// if (qType.equals(".find")) {
	// sqlQuery.append("SELECT * FROM ").append(ontName);
	// } else if (qType.equals(".update")) {
	// sqlQuery.append("UPDATE ").append(ontName);
	// } else if (qType.equals(".remove")) {
	// sqlQuery.append("DELETE FROM ").append(ontName);
	// }
	//
	// if (nativeQuery.contains("ObjectId")) {
	// String oid = nativeQuery.substring(nativeQuery.indexOf("ObjectId(") + 10,
	// nativeQuery.indexOf("\")"));
	// sqlQuery.append(" WHERE ").append("OID='" + oid + "'");
	// } else if (nativeQuery.contains("$oid")) {
	// String oid = nativeQuery.substring(nativeQuery.indexOf("$oid") + 7,
	// nativeQuery.indexOf("\"}"));
	// sqlQuery.append(" WHERE ").append("OID='" + oid + "'");
	// }
	//
	// return sqlQuery.toString();
	// }
	//
	// @Override
	// public String getJsonDataFromInsert(String ontology, String insertStatement)
	// throws JSONException, Exception {
	//
	// insertStatement = prepareStatement(insertStatement);
	//
	// try {
	// Parser sSql = new Parser(insertStatement);
	// Insert insert = (Insert) sSql.processStatement();
	//
	// if (ontology == null || ontology.trim().length() == 0) {
	// ontology = insert.getTable();
	// }
	// if (!ontology.equalsIgnoreCase(insert.getTable())) {
	// throw new Exception(
	// "Ontology " + ontology + " does not match with insert statement table: " +
	// insert.getTable());
	// }
	//
	// Map<String, Campoontologiarelacional> mFields =
	// cacheService.getCamposOntologiaRelacional(ontology);
	//
	// Vector columsn = insert.getColumns();
	//
	// Vector values = insert.getValues();
	// JSONObject json = new JSONObject();
	//
	// for (int i = 0; i < columsn.size(); i++) {
	// String key = (String) columsn.get(i);
	// Constant val = ((Constant) values.get(i));
	//
	// Campoontologiarelacional fieldDescriptor = mFields.get(key.toUpperCase());
	// String tipoCampo = fieldDescriptor.getTipoCampo();
	//
	// if (tipoCampo.equalsIgnoreCase("JSON") ||
	// tipoCampo.equalsIgnoreCase("GEOMETRY")) {
	// json.put(key, new JSONObject(val.getValue()));
	// } else if (tipoCampo.equalsIgnoreCase("TIMESTAMP")) {
	// java.util.Date d = null;
	// try {
	// d = this.sdfOracle.parse(val.getValue());
	// } catch (Exception e) {
	// log.warn(
	// "Error parseando fecha en formato de oracle configurado en la instalación. Se
	// intenta parsear según formato ISO-8601");
	// d = this.sdfIso8601.parse(val.getValue());
	// }
	// if (d != null) {
	// String date = this.sdfIso8601.format(d);
	// JSONObject o = new JSONObject().put("$date", date);
	// json.put(key, o);
	// }
	// } else {
	// if (val.getType() == Constant.NUMBER) {
	// json.put(key, Double.parseDouble(val.getValue()));
	// }
	// if (val.getType() == Constant.STRING) {
	// json.put(key, val.getValue());
	// }
	// }
	// }
	// return json.toString();
	//
	// } catch (ParseException e) {
	// log.error("Error procesando sentencia de tipo INSERT", e);
	// throw new PersistenceException(e);
	// }
	//
	// }
	//
	// private String getJsonAsString(Object value) {
	// try {
	// String json = new ObjectMapper().writeValueAsString(value);
	// return json;
	// } catch (JsonProcessingException e) {
	// log.error("Cannot parse Json instance", e);
	// throw new PersistenceException("Cannot parse Json instance");
	// }
	// }
	//
	// private String getTimeAsString(Object value) {
	// try {
	//
	// JSONObject json = new JSONObject(new
	// ObjectMapper().writeValueAsString(value));
	// String date = json.getString("$date");
	//
	// java.util.Date d = null;
	// try {
	// d = this.sdfOracle.parse(date);
	// } catch (Exception e) {
	// log.warn(
	// "Error parseando fecha en formato de oracle configurado en la instalación. Se
	// intenta parsear según formato ISO-8601");
	// d = this.sdfIso8601.parse(date);
	// }
	//
	// String t = this.sdfOracle.format(d);
	// return "'" + t + "'";
	//
	// } catch (Exception e) {
	// log.error("Cannot parse Json instance", e);
	// throw new PersistenceException("Cannot parse Json instance");
	// }
	// }
	//
	// /**
	// * Convierte una instancia Json sobre una ontologia, con query OID, en una
	// * sentencia update
	// */
	// @Override
	// public String getUpdateStatement(String ontology, String query, String datos)
	// {
	// // Recupera la ontologia de la cache
	// Map<String, Campoontologiarelacional> mFields =
	// cacheService.getCamposOntologiaRelacional(ontology);
	// if (mFields != null) {
	// Map<String, Object> mapSchema;
	// try {
	// mapSchema = new ObjectMapper().readValue(datos, Map.class);
	// } catch (Exception e) {
	// log.error("Cannot parse Json instance", e);
	// throw new PersistenceException("Cannot parse Json instance");
	// }
	//
	// StringBuilder sqlUpdate = new StringBuilder();
	//
	// sqlUpdate.append("UPDATE ").append(ontology).append(" SET ");
	//
	// for (Entry<String, Object> entry : mapSchema.entrySet()) {
	// String key = entry.getKey().toUpperCase();
	// Campoontologiarelacional fieldDescriptor =
	// mFields.get(entry.getKey().toUpperCase());
	// if ((fieldDescriptor != null ||
	// key.equalsIgnoreCase(DAOOracleImpl.TABLE_COLUMN_CONTEXTDATA))
	// &&
	// !DAOOracleImpl.TABLE_COLUMN_OID.equalsIgnoreCase(entry.getValue().toString()))
	// {
	//
	// sqlUpdate.append(key).append("=");
	//
	// String tipoCampo = fieldDescriptor.getTipoCampo();
	// if (tipoCampo.equalsIgnoreCase("STRING")) {
	// sqlUpdate.append("'").append(entry.getValue()).append("'");
	// } else if (tipoCampo.equalsIgnoreCase("JSON") ||
	// tipoCampo.equalsIgnoreCase("GEOMETRY")) {
	// sqlUpdate.append("'").append(getJsonAsString(entry.getValue())).append("'");
	// } else if (tipoCampo.equalsIgnoreCase("TIMESTAMP")) {
	// sqlUpdate.append(getTimeAsString(entry.getValue()));
	// } else {
	// sqlUpdate.append(entry.getValue());
	// }
	// sqlUpdate.append(", ");
	// }
	// }
	// sqlUpdate.deleteCharAt(sqlUpdate.lastIndexOf(",")).append(" WHERE
	// (").append(query).append(")");
	//
	// return sqlUpdate.toString();
	//
	// } else {
	// log.error("Ontology not defined in BDTR");
	// throw new PersistenceException("Ontology not defined in BDTR");
	// }
	// }
	//
	// @Override
	// public String getInsertStatement(String ontology, String instance, String id)
	// throws PersistenceException {
	// throw new NotImplementedException("Operation not supported");
	// }
	//
	/**
	 * Convierte una instancia Json sobre una ontologia en una sentencia Insert
	 */
	@Override
	public String getInsertStatement(String ontology, String instance) {

		JSONObject objInstance = new JSONObject(instance);

		List<Column> columns = new LinkedList<>();

		ExpressionList lvalues = new ExpressionList();
		lvalues.setExpressions(new LinkedList<Expression>());

		for (Object key : objInstance.keySet()) {

			Object objValue = objInstance.get(key.toString());

			if (objValue instanceof String) {
				Column column = new Column();
				column.setColumnName(key.toString());
				columns.add(column);

				lvalues.getExpressions().add(new StringValue("'" + objValue.toString() + "'"));

			} else if (objValue instanceof Long) {
				Column column = new Column();
				column.setColumnName(key.toString());
				columns.add(column);

				lvalues.getExpressions().add(new LongValue(((Long) objValue).longValue()));
			} else if (objValue instanceof Double) {
				Column column = new Column();
				column.setColumnName(key.toString());
				columns.add(column);

				lvalues.getExpressions().add(new DoubleValue(String.valueOf(((Double) objValue).doubleValue())));
			} else if (objValue instanceof Integer) {
				Column column = new Column();
				column.setColumnName(key.toString());
				columns.add(column);

				lvalues.getExpressions().add(new LongValue(((Integer) objValue).intValue()));
			} else if (objValue instanceof Boolean) {
				Column column = new Column();
				column.setColumnName(key.toString());
				columns.add(column);

				if ((Boolean) objValue) {
					lvalues.getExpressions().add(new LongValue(1));
				} else {
					lvalues.getExpressions().add(new LongValue(0));
				}
			} else {
				log.warn("Value type: {}, for key: {} not supported", objValue.getClass().getName(), key);
			}
		}

		Insert insert = new Insert();
		Table table = new Table();
		table.setName(ontology);
		insert.setTable(table);

		insert.setColumns(columns);
		insert.setItemsList(lvalues);

		return insert.toString();
	}

	//
	// /**
	// * Añade el Object identifier al registro a insertar
	// */
	// @SuppressWarnings("unchecked")
	// @Override
	// public String addInsertIdentifier(String insertStatement) {
	// insertStatement = prepareStatement(insertStatement);
	// try {
	// String contextData = getContextData();
	// Parser sSql = new Parser(insertStatement);
	// Insert insert = (Insert) sSql.processStatement();
	// insert.getColumns().add(DAOOracleImpl.TABLE_COLUMN_OID);
	// insert.getValues().add(new Constant("SYS_GUID()", Constant.UNKNOWN));
	// insert.getColumns().add(DAOOracleImpl.TABLE_COLUMN_CONTEXTDATA);
	// insert.getValues().add(new Constant(contextData, Constant.UNKNOWN));
	//
	// return insert.toString();
	//
	// } catch (ParseException e) {
	// log.error("Error procesando sentencia de tipo INSERT", e);
	// throw new PersistenceException(e);
	// }
	//
	// }
	//
	// private String getContextData() {
	// StringBuffer cntxData = new StringBuffer();
	// cntxData.append("'").append("{");
	// cntxData.append("\"session_key\":\"UserPlatform\",");
	// if (getUser() != null) {
	// cntxData.append("\"user\":\"" + getUser().getIdentificacion() + "\",");
	// } else {
	// cntxData.append("\"user\":\"\",");
	// }
	// cntxData.append("\"kp\":\"\",");
	// cntxData.append("\"kp_instancia\":\"\",");
	//
	// // Adaptar timestamp a formato ISODate
	// cntxData.append("\"timestamp\":{\"$date\":\"" +
	// CalendarAdapter.marshalUtcDate() + "\"}");
	// cntxData.append("}").append("'");
	//
	// return cntxData.toString();
	//
	// }
	//
	// @Override
	// public String prepareOracleBoolean(String statement) throws
	// PersistenceException {
	// statement = prepareStatement(statement);
	// try {
	// Parser sSql = new Parser(statement);
	// Statement stmt = (Statement) sSql.processStatement();
	//
	// // Prepara el dato a insertar o actualizar
	// if (stmt instanceof Insert) {
	// Vector<Object> values = ((Insert) stmt).getValues();
	// for (int i = 0; i < values.size(); i++) {
	// Object value = values.get(i);
	// if (value instanceof Constant) {
	// if (((Constant) value).getValue().equalsIgnoreCase("TRUE")) {
	// values.remove(i);
	// values.insertElementAt(new Constant("1", Constant.UNKNOWN), i);
	// } else if (((Constant) value).getValue().equalsIgnoreCase("FALSE")) {
	// values.remove(i);
	// values.insertElementAt(new Constant("0", Constant.UNKNOWN), i);
	// }
	// }
	// }
	// return stmt.toString();
	// } else if (stmt instanceof Update) {
	// Hashtable setValues = ((Update) stmt).getSet();
	// Set<Entry> entrySet = setValues.entrySet();
	// for (Entry entry : entrySet) {
	// Object value = entry.getValue();
	// if (value instanceof Constant) {
	// if (((Constant) value).getValue().equalsIgnoreCase("TRUE")) {
	// entry.setValue(new Constant("1", Constant.UNKNOWN));
	// } else if (((Constant) value).getValue().equalsIgnoreCase("FALSE")) {
	// entry.setValue(new Constant("0", Constant.UNKNOWN));
	// }
	// }
	// }
	// statement = stmt.toString();
	// }
	// // Prepara la condicion WHERE si existe
	// if (statement.toUpperCase().contains("WHERE")) {
	// statement = prepareWhere(statement);
	// }
	//
	// } catch (ParseException e) {
	// log.error("Error procesando sentencia de tipo INSERT", e);
	// throw new PersistenceException(e);
	// }
	//
	// return statement;
	// }
	//
	// private String prepareWhere(String statement) throws ParseException {
	//
	// statement = prepareStatement(statement);
	//
	// Parser sSql = new Parser(statement);
	//
	// Exp whereExp = null;
	//
	// Statement processStatement = (Statement) sSql.processStatement();
	//
	// if (processStatement instanceof Update) {
	// whereExp = ((Update) processStatement).getWhere();
	// } else if (processStatement instanceof Delete) {
	// whereExp = ((Delete) processStatement).getWhere();
	// } else if (processStatement instanceof Query) {
	// whereExp = ((Query) processStatement).getWhere();
	// }
	//
	// if (whereExp != null) {
	// reduceExp(whereExp);
	// }
	//
	// if (processStatement != null) {
	// return processStatement.toString();
	// } else {
	// return statement;
	// }
	// }
	//
	// private Exp reduceExp(Exp exp) {
	// if (exp instanceof Constant) {
	// if (((Constant) exp).getValue().equalsIgnoreCase("TRUE")) {
	// exp = new Constant("1", Constant.UNKNOWN);
	// } else if (((Constant) exp).getValue().equalsIgnoreCase("FALSE")) {
	// exp = new Constant("0", Constant.UNKNOWN);
	// }
	// } else if (exp instanceof Expression) {
	// Vector operands = ((Expression) exp).getOperands();
	//
	// for (int i = 0; i < operands.size(); i++) {
	// Object op = operands.get(i);
	// operands.removeElementAt(i);
	// operands.add(i, reduceExp((Exp) op));
	// }
	//
	// } else if (exp instanceof Query) {
	// reduceExp(((Query) exp).getWhere());
	// }
	// return exp;
	// }
	//
	// @Override
	// public String addSelectWhereCondition(String selectStatement, String
	// condition) throws SQLStatementParseException {
	// // selectStatement = prepareStatement(selectStatement);
	// try {
	// return selectStatement;
	//
	// // Parser sSql = new Parser(selectStatement);
	// // Query query = new Query();
	// // if (selectStatement.startsWith("select ")) {
	// // query = (Query) sSql.processStatement();
	// // Exp where = query.getWhere();
	// // if (where == null) {
	// // where = new Constant(condition, Constant.UNKNOWN);
	// // query.addWhere(where);
	// // } else {
	// // Expression expWhere = new Expression("AND", where, new Constant(condition,
	// // Constant.UNKNOWN));
	// // query.addWhere(expWhere);
	// // }
	// // return query.toString();
	// // }
	// //
	// // return selectStatement;
	// //
	// } catch (JSQLParserException e) {
	// log.error("Error procesando sentencia de tipo SELECT", e);
	// throw new SQLStatementParseException(e);
	// }
	// }

	// @Override
	// public List<String> getSelectTables(String selectStatement) {
	// selectStatement = prepareStatement(selectStatement);
	// try {
	// Parser sSql = new Parser(selectStatement);
	// Query query = (Query) sSql.processStatement();
	//
	// Vector<FromItem> vFrom = query.getFrom();
	// List<String> lTables = new ArrayList<String>();
	// for (int i = 0; i < vFrom.size(); i++) {
	// lTables.add(((FromItem) vFrom.get(i)).getTable());
	// }
	// return lTables;
	//
	// } catch (ParseException e) {
	// log.error("Error procesando sentencia de tipo INSERT", e);
	// throw new PersistenceException(e);
	// }
	// }
	//
	// @Override
	// public List<String> getStatementTables(String statement) {
	// statement = prepareStatement(statement);
	// try {
	// Parser sSql = new Parser(statement);
	// Statement processStatement = (Statement) sSql.processStatement();
	//
	// List<String> lTables = new ArrayList<String>();
	//
	// if (processStatement instanceof Query) {
	// Vector<FromItem> vFrom = ((Query) processStatement).getFrom();
	//
	// for (int i = 0; i < vFrom.size(); i++) {
	// lTables.add(((FromItem) vFrom.get(i)).getTable());
	// }
	//
	// } else if (processStatement instanceof Update) {
	// lTables.add(((Update) processStatement).getTable());
	// } else if (processStatement instanceof Insert) {
	// lTables.add(((Insert) processStatement).getTable());
	// } else if (processStatement instanceof Delete) {
	// lTables.add(((Delete) processStatement).getTable());
	// }
	// return lTables;
	//
	// } catch (ParseException e) {
	// log.error("Error procesando sentencia de tipo INSERT", e);
	// throw new PersistenceException(e);
	// }
	// }
	//
	// @Override
	// public String getJsonFromResultSet(String table, ResultSet data) throws
	// PersistenceException {
	// try {
	// if (data != null) {
	// return this.convertJsonArray(table, data).toString();
	// }
	// return "{}";
	// } catch (Exception e) {
	// log.error("Error convirtiendo resultado de un select en JSON", e);
	// throw new PersistenceException(e);
	// }
	// }
	//
	// @Override
	// public String getJsonFromResultSet(String table, ResultSet data, List<String>
	// lFields) throws PersistenceException {
	// try {
	// if (data != null) {
	// return this.convertJsonArray(table, data, lFields).toString();
	// }
	// return "{}";
	// } catch (Exception e) {
	// log.error("Error convirtiendo resultado de un select en JSON", e);
	// throw new PersistenceException(e);
	// }
	// }
	//
	// @Override
	// public void checkAuthorized(String statement) throws PersistenceException {
	// try {
	// // Recupera la lista de tablas de la sentencia y el uso que se le da
	// List<TableResult> lTablesUsage =
	// StatementsParser.getTableNamesFromSQLStatement(statement);
	//
	// // Comprueba los permisos sobre cada tabla accedida
	// for (TableResult table : lTablesUsage) {
	// String ontology = table.getTableName();
	// SSAPMessageTypes operationType = StatementsParser
	// .fromTableAccessModeToSSAPMessageType(table.getAccessMode());
	//
	// this.securityPluginManager.checkAuthorization(operationType, ontology, null);
	// }
	//
	// } catch (NotSupportedStatementException e) {
	// log.error("Tipo de sentencia no soportado", e);
	// throw new PersistenceException(e);
	// } catch (ParseException e) {
	// log.error("No se ha podido parsear la sentencia", e);
	// throw new PersistenceException(e);
	// }
	//
	// }
	//
	// @Override
	// public String getSelectObjectIdFromStatement(String statement) throws
	// PersistenceException {
	// try {
	// statement = prepareStatement(statement);
	//
	// Parser sSql = new Parser(statement);
	// Statement stmt = sSql.processStatement();
	//
	// String table = null;
	// Exp whereClause = null;
	// if (stmt instanceof Update) {
	// table = ((Update) stmt).getTable();
	// whereClause = ((Update) stmt).getWhere();
	// } else if (stmt instanceof Delete) {
	// table = ((Delete) stmt).getTable();
	// whereClause = ((Delete) stmt).getWhere();
	// }
	//
	// if (table != null) {
	// String returnStatement = null;
	// if
	// (Ontologia.findOntologiasByIdentificacion(table).getSingleResult().getBdtrdatasourceid().getSgbd()
	// .equals("PostgreSQL")) {
	// returnStatement = "SELECT " + DAOPostgreSQLImpl.TABLE_COLUMN_OID + " FROM " +
	// table;
	// } else {
	// returnStatement = "SELECT " + DAOOracleImpl.TABLE_COLUMN_OID + " FROM " +
	// table;
	// }
	// if (whereClause != null) {
	// returnStatement += " WHERE " + whereClause.toString();
	// }
	//
	// return returnStatement;
	// } else {
	// throw new PersistenceException("Imposible recuperar nombre de la tabla de la
	// sentencia: " + statement
	// + ". La sentencia debe ser de tipo UPDATE o DELETE");
	// }
	//
	// } catch (ParseException e) {
	// log.error("No se ha podido parsear la sentencia", e);
	// throw new PersistenceException(e);
	// }
	//
	// }
	//
	// @Override
	// public List<String> getAffectedIdsFromResultSet(ResultSet rs) throws
	// PersistenceException {
	// List<String> result = new ArrayList<String>();
	// try {
	// while (rs.next()) {
	// String oid;
	// try {
	// oid = rs.getString(DAOOracleImpl.TABLE_COLUMN_OID);
	// } catch (SQLException e) {
	// log.error("No se ha podido extraer el identificador de un registro. Se
	// considera nulo");
	// oid = null;
	// }
	// if (oid != null) {
	// result.add("{\"_id\":\"" + oid + "\"}");
	// } else {
	// result.add("{\"_id\":null}");
	// }
	// }
	// } catch (SQLException e) {
	// log.error("Error porcesando sentencia de recuperación de identificadores de
	// registro");
	// throw new PersistenceException(e);
	// }
	//
	// return result;
	// }
	//
	// @Override
	// public List<String> getJsonListFromResultset(String table, ResultSet data)
	// throws PersistenceException {
	// try {
	// if (data != null) {
	// return this.convertList(table, data);
	// }
	// return new ArrayList<String>();
	// } catch (Exception e) {
	// log.error("Error convirtiendo resultado de un select en lista de JSON", e);
	// throw new PersistenceException(e);
	// }
	// }
	//
	// @Override
	// public List<String> getJsonListFromResultset(String table, ResultSet data,
	// List<String> lFields)
	// throws PersistenceException {
	// try {
	// if (data != null) {
	// return this.convertList(table, data, lFields);
	// }
	// return new ArrayList<String>();
	// } catch (Exception e) {
	// log.error("Error convirtiendo resultado de un select en lista de JSON", e);
	// throw new PersistenceException(e);
	// }
	// }
	//
	// @Override
	// public Map<String, List<String>> getExpandFields(String selectStatement)
	// throws SQLSyntaxErrorException {
	// Map<String, List<String>> mExpandFields = new HashMap<String,
	// List<String>>();
	//
	// if (selectStatement.contains("EXPAND")) {
	// String[] tokens = selectStatement.split(" ");
	// Iterator<String> tokensIterator = Arrays.asList(tokens).iterator();
	//
	// boolean expandToken = false;
	// StringBuilder expandClause = new StringBuilder();
	// while (tokensIterator.hasNext()) {
	// String token = tokensIterator.next();
	// if (expandToken) {
	// String nextToken = null;
	// if (token.endsWith(".") && tokensIterator.hasNext()) {
	// nextToken = tokensIterator.next();
	// }
	// expandClause.append(token);
	// if (nextToken != null) {
	// expandClause.append(nextToken);
	// }
	//
	// } else if (token.equals("EXPAND")) {
	// expandToken = true;
	// }
	// }
	//
	// List<String> expandFields =
	// Arrays.asList(expandClause.toString().split(","));
	//
	// for (String field : expandFields) {
	// String[] fieldSelect = field.split("\\.");
	// if (fieldSelect.length != 2) {
	// throw new SQLSyntaxErrorException(
	// "EXPAND clause cannot find referenced field for expanded field: " + field);
	// }
	// List<String> lFields = mExpandFields.get(fieldSelect[0]);
	// if (lFields == null) {
	// lFields = new ArrayList<String>();
	// mExpandFields.put(fieldSelect[0], lFields);
	// }
	//
	// if (!lFields.contains("*") && !lFields.contains(fieldSelect[1])) {
	// if (fieldSelect[1].equals("*")) {
	// lFields.clear();
	// }
	// lFields.add(fieldSelect[1]);
	// }
	//
	// }
	//
	// }
	//
	// return mExpandFields;
	// }
	//
	// @Override
	// public String buildExpandMainQuery(String selectStatement, Set<String>
	// expandFields) throws ParseException {
	// Parser sSql = new Parser(selectStatement);
	// Query query = (Query) sSql.processStatement();
	//
	// @SuppressWarnings("unchecked")
	// Vector<SelectItem> vSelect = query.getSelect();
	// Vector<SelectItem> vExpand = new Vector<SelectItem>();
	//
	// for (String field : expandFields) {
	// boolean found = false;
	// for (SelectItem item : vSelect) {
	// if (item.getColumn().equals("*")) {
	// return selectStatement;
	// } else if (item.getColumn().equalsIgnoreCase(field)) {
	// found = true;
	// break;
	// }
	// }
	// if (!found) {
	// vExpand.add(new SelectItem(field));
	// }
	// }
	// vSelect.addAll(vExpand);
	//
	// return query.toString();
	//
	// }
	//
	// @Override
	// public String getExpandJsonResult(String baseOntology, ResultSet rs,
	// Map<String, List<String>> mExpandFields,
	// DAOTRPersistence dao) throws SQLException, JSONException {
	//
	// ResultSetMetaData rsmd = rs.getMetaData();
	// int columnNumber = rsmd.getColumnCount();
	//
	// // Extrae los indices en el resultset de las columnas a expandir
	// List<Integer> lFieldIndex = new ArrayList<Integer>();
	// for (String field : mExpandFields.keySet()) {
	// for (int i = 1; i <= columnNumber; i++) {
	// if (rsmd.getColumnName(i).equalsIgnoreCase(field)) {
	// lFieldIndex.add(i);
	// }
	// }
	// }
	//
	// JSONArray json = new JSONArray();
	//
	// while (rs.next()) {
	// int numColumns = rsmd.getColumnCount();
	// JSONObject obj = new JSONObject();
	//
	// for (int i = 1; i < numColumns + 1; i++) {
	// String columnName = rsmd.getColumnName(i);
	//
	// if (!lFieldIndex.contains(i)) {// Campo normal
	// String tableName = rsmd.getTableName(i);
	// if (tableName == null || tableName.equals("")) {
	// tableName = baseOntology;
	// }
	// obj = this.extractResultSetRowField(rs, tableName, rsmd.getColumnType(i),
	// columnName, obj);
	//
	// } else {// campo a expandir
	// List<JSONObject> lData = this.extractExpandfield(baseOntology, columnName,
	// mExpandFields,
	// rsmd.getColumnType(i), rs, dao);
	//
	// if (lData != null && lData.size() > 0) {
	// if (lData.size() == 1) {
	// obj.put(columnName, lData.get(0));
	// } else {
	// obj.put(columnName, lData);
	// }
	// }
	// }
	// }
	//
	// if (obj.length() > 0) {
	// json.put(obj);
	// }
	//
	// }
	//
	// return json.toString();
	// }
	//
	// private String buildExpandDerivedQuery(String baseOntology, String
	// columnName, List<String> lSelectFiedls,
	// int rowType, ResultSet rs) {
	//
	// StringBuilder baseStatement = new StringBuilder();
	//
	// try {
	// Campoontologiarelacional campobase = Campoontologiarelacional
	// .findCampoontologiarelacionalByOntologiaIdAndNombrecampo(baseOntology,
	// columnName);
	// Campoontologiarelacional referencedField =
	// campobase.getIdCampoReferenciado();
	//
	// if (referencedField != null) {
	// String referencedTable =
	// referencedField.getIdOntologia().getIdentificacion();
	//
	// baseStatement.append("SELECT ");
	// int numberOfFields = lSelectFiedls.size();
	// for (int i = 0; i < numberOfFields; i++) {
	// baseStatement.append(lSelectFiedls.get(i));
	// if (i < numberOfFields - 1) {
	// baseStatement.append(", ");
	// }
	// }
	//
	// baseStatement.append(" FROM ").append(referencedTable).append(" WHERE ")
	// .append(referencedField.getNombreCampo()).append("=");
	//
	// if (rowType == java.sql.Types.VARCHAR) {
	// String value = rs.getString(columnName);
	// if (value != null) {
	// baseStatement.append("'").append(value).append("'");
	// } else {
	// baseStatement.append("NULL");
	// }
	// } else if (rowType == java.sql.Types.BOOLEAN) {
	// Boolean value = rs.getBoolean(columnName);
	// if (value != null) {
	// baseStatement.append(rs.getBoolean(columnName));
	// } else {
	// baseStatement.append("NULL");
	// }
	// } else if (rowType == java.sql.Types.NUMERIC) {
	// Double value = rs.getDouble(columnName);
	// if (value != null) {
	// if ((value == Math.floor(value)) && !Double.isInfinite(value)) {
	// baseStatement.append((int) value.doubleValue());
	// } else {
	// baseStatement.append(value.doubleValue());
	// }
	//
	// } else {
	// baseStatement.append("NULL");
	// }
	// } else if (rowType == java.sql.Types.INTEGER) {
	// Integer value = rs.getInt(columnName);
	// if (value != null) {
	// baseStatement.append(value);
	// } else {
	// baseStatement.append("NULL");
	// }
	// } else if (rowType == java.sql.Types.DOUBLE) {
	// Double value = rs.getDouble(columnName);
	// if (value != null) {
	// baseStatement.append(value);
	// } else {
	// baseStatement.append("NULL");
	// }
	// } else if (rowType == java.sql.Types.FLOAT) {
	// Float value = rs.getFloat(columnName);
	// if (value == null) {
	// baseStatement.append(value);
	// } else {
	// baseStatement.append("NULL");
	// }
	// } else if (rowType == java.sql.Types.DATE) {
	// Date value = rs.getDate(columnName);
	// if (value != null) {
	// baseStatement.append(value);
	// } else {
	// baseStatement.append("NULL");
	// }
	// } else if (rowType == java.sql.Types.TIMESTAMP) {
	// Timestamp value = rs.getTimestamp(columnName);
	// if (value != null) {
	// baseStatement.append("'").append(value).append("'");
	// } else {
	// baseStatement.append("NULL");
	// }
	// } else if (rowType == java.sql.Types.CLOB) {
	// Clob value = rs.getClob(columnName);
	// if (value != null) {
	// baseStatement.append("'").append(this.getContentFromInputStream(value.getAsciiStream()))
	// .append("'");
	// } else {
	// baseStatement.append("NULL");
	// }
	// } else if (rowType == java.sql.Types.BLOB) {
	// Blob value = rs.getBlob(columnName);
	// if (value != null) {
	// baseStatement.append(value);
	// } else {
	// baseStatement.append("NULL");
	// }
	// } else if (rowType == java.sql.Types.NVARCHAR) {
	// String value = rs.getNString(columnName);
	// if (value != null) {
	// baseStatement.append("'").append(value).append("'");
	// } else {
	// baseStatement.append("NULL");
	// }
	// } else if (rowType == java.sql.Types.TINYINT) {
	// baseStatement.append(rs.getInt(columnName));
	// } else if (rowType == java.sql.Types.SMALLINT) {
	// baseStatement.append(rs.getInt(columnName));
	// } else if (rowType == java.sql.Types.ARRAY) {
	// baseStatement.append(rs.getArray(columnName));
	// } else if (rowType == java.sql.Types.BIGINT) {
	// baseStatement.append(rs.getInt(columnName));
	// } else {
	// Object value = rs.getObject(columnName);
	// if (value != null) {
	// baseStatement.append(value);
	// } else {
	// baseStatement.append("NULL");
	// }
	// }
	//
	// }
	// } catch (Exception e) {
	// log.error("Error getting from BDC referenced field for " + baseOntology + "."
	// + columnName, e);
	// }
	//
	// return baseStatement.toString();
	// }
	//
	// private List<JSONObject> extractExpandfield(String baseOntology, String
	// columnName,
	// Map<String, List<String>> mExpandFields, int rowType, ResultSet rs,
	// DAOTRPersistence dao) {
	//
	// String expandQuery = this.buildExpandDerivedQuery(baseOntology, columnName,
	// mExpandFields.get(columnName),
	// rowType, rs);
	//
	// // Aqui habria que usar una cache local
	// return dao.queryInternal(expandQuery);
	//
	// }
	//
	// private List<String> convertList(String table, ResultSet rs, List<String>
	// lFields)
	// throws SQLException, JSONException {
	// List<String> lResult = new ArrayList<String>();
	// ResultSetMetaData rsmd = rs.getMetaData();
	//
	// List<String> lUpperFields = new ArrayList<String>();
	// for (String field : lFields) {
	// lUpperFields.add(field.toUpperCase());
	// }
	//
	// while (rs.next()) {
	// int numColumns = rsmd.getColumnCount();
	// JSONObject obj = new JSONObject();
	//
	// for (int i = 1; i < numColumns + 1; i++) {
	// String columnName = rsmd.getColumnName(i);
	// if (lUpperFields.contains(columnName.toUpperCase())) {
	// String tableName = rsmd.getTableName(i);
	// if (tableName == null || tableName.equals("")) {
	// tableName = table;
	// }
	// obj = this.extractResultSetRowField(rs, tableName, rsmd.getColumnType(i),
	// columnName, obj);
	// }
	// }
	//
	// if (obj.length() > 0) {
	// lResult.add(obj.toString());
	// }
	// }
	// return lResult;
	//
	// }
	//
	// @Override
	// public List<String> convertList(String table, ResultSet rs) throws
	// SQLException, JSONException {
	// List<String> lResult = new ArrayList<String>();
	// ResultSetMetaData rsmd = rs.getMetaData();
	//
	// while (rs.next()) {
	// int numColumns = rsmd.getColumnCount();
	// JSONObject obj = new JSONObject();
	//
	// for (int i = 1; i < numColumns + 1; i++) {
	// String columnName = rsmd.getColumnName(i);
	// String tableName = rsmd.getTableName(i);
	// if (tableName == null || tableName.equals("")) {
	// tableName = table;
	// }
	// obj = this.extractResultSetRowField(rs, tableName, rsmd.getColumnType(i),
	// columnName, obj);
	// }
	//
	// if (obj.length() > 0) {
	// lResult.add(obj.toString());
	// }
	// }
	// return lResult;
	//
	// }
	//
	// @Override
	// public List<JSONObject> convertJsonList(String table, ResultSet rs) throws
	// SQLException, JSONException {
	// List<JSONObject> lResult = new ArrayList<JSONObject>();
	// ResultSetMetaData rsmd = rs.getMetaData();
	//
	// while (rs.next()) {
	// int numColumns = rsmd.getColumnCount();
	// JSONObject obj = new JSONObject();
	//
	// for (int i = 1; i < numColumns + 1; i++) {
	// String columnName = rsmd.getColumnName(i);
	// String tableName = rsmd.getTableName(i);
	// if (tableName == null || tableName.equals("")) {
	// tableName = table;
	// }
	// obj = this.extractResultSetRowField(rs, tableName, rsmd.getColumnType(i),
	// columnName, obj);
	// }
	//
	// if (obj.length() > 0) {
	// lResult.add(obj);
	// }
	// }
	// return lResult;
	//
	// }
	//
	// @Override
	// public JSONArray convertJsonArray(String table, ResultSet rs) throws
	// SQLException, JSONException {
	// JSONArray json = new JSONArray();
	// ResultSetMetaData rsmd = rs.getMetaData();
	// String tableName = null;
	//
	// while (rs.next()) {
	// int numColumns = rsmd.getColumnCount();
	// JSONObject obj = new JSONObject();
	//
	// for (int i = 1; i < numColumns + 1; i++) {
	// String columnName = rsmd.getColumnName(i);
	// try {
	// tableName = rsmd.getTableName(i);
	// } catch (Exception e) {
	// }
	//
	// if (tableName == null || tableName.equals("")) {
	// tableName = table;
	// }
	// obj = this.extractResultSetRowField(rs, tableName, rsmd.getColumnType(i),
	// columnName, obj);
	//
	// }
	// if (obj.length() > 0) {
	// json.put(obj);
	// }
	// }
	// return json;
	// }
	//
	// private JSONArray convertJsonArray(String table, ResultSet rs, List<String>
	// lFields)
	// throws SQLException, JSONException {
	// JSONArray json = new JSONArray();
	// ResultSetMetaData rsmd = rs.getMetaData();
	//
	// List<String> lUpperFields = new ArrayList<String>();
	// for (String field : lFields) {
	// lUpperFields.add(field.toUpperCase());
	// }
	//
	// while (rs.next()) {
	// int numColumns = rsmd.getColumnCount();
	// JSONObject obj = new JSONObject();
	//
	// for (int i = 1; i < numColumns + 1; i++) {
	// String columnName = rsmd.getColumnName(i);
	// if (lUpperFields.contains(columnName.toUpperCase())) {
	// String tableName = rsmd.getTableName(i);
	// if (tableName == null || tableName.equals("")) {
	// tableName = table;
	// }
	// obj = this.extractResultSetRowField(rs, tableName, rsmd.getColumnType(i),
	// columnName, obj);
	// }
	// }
	//
	// if (obj.length() > 0) {
	// json.put(obj);
	// }
	// }
	//
	// return json;
	// }
	//
	// private JSONObject extractResultSetRowField(ResultSet rs, String table, int
	// rowType, String columnName,
	// JSONObject obj) throws JSONException, SQLException {
	//
	// Map<String, Campoontologiarelacional> mFields =
	// cacheService.getCamposOntologiaRelacional(table);
	//
	// boolean booleanField = false;
	// if (mFields != null) {
	// for (String key : mFields.keySet()) {
	// if (key.equalsIgnoreCase(columnName)) {
	// columnName = key;
	// if (mFields.get(key).getTipoCampo().equalsIgnoreCase("boolean")) {
	// booleanField = true;
	// }
	// }
	// }
	// }
	// if (rowType == java.sql.Types.VARCHAR) {
	// String value = rs.getString(columnName);
	// if (value != null) {
	// obj.put(columnName, value);
	// }
	// } else if (rowType == java.sql.Types.BOOLEAN) {
	// Boolean value = rs.getBoolean(columnName);
	// if (value != null) {
	// obj.put(columnName, value);
	// }
	// } else if (rowType == java.sql.Types.NUMERIC) {
	// int value = rs.getInt(columnName);
	// if (booleanField) {
	// if (value == 0) {
	// obj.put(columnName, false);
	// } else {
	// obj.put(columnName, true);
	// }
	// } else {
	// Double dValue = rs.getDouble(columnName);
	// if (dValue != null) {
	// if ((dValue == Math.floor(dValue)) && !Double.isInfinite(dValue)) {
	// obj.put(columnName, value);
	// } else {
	// obj.put(columnName, dValue.doubleValue());
	// }
	//
	// } else {
	// obj.put(columnName, "NULL");
	// }
	// }
	// } else if (rowType == java.sql.Types.INTEGER) {
	// int value = rs.getInt(columnName);
	// if (booleanField) {
	// if (value == 0) {
	// obj.put(columnName, false);
	// } else {
	// obj.put(columnName, true);
	// }
	// } else {
	// obj.put(columnName, value);
	// }
	// } else if (rowType == java.sql.Types.DOUBLE) {
	// obj.put(columnName, rs.getDouble(columnName));
	// } else if (rowType == java.sql.Types.FLOAT) {
	// obj.put(columnName, rs.getFloat(columnName));
	// } else if (rowType == java.sql.Types.DATE) {
	// Date value = rs.getDate(columnName);
	// if (value != null) {
	// obj.put(columnName, value);
	// }
	// } else if (rowType == java.sql.Types.TIMESTAMP) {
	// Timestamp value = rs.getTimestamp(columnName);
	// if (value != null) {
	// String date = new
	// SimpleDateFormat("yyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(value);
	// JSONObject o = new JSONObject().put("$date", date);
	// obj.put(columnName, o);
	// }
	// } else if (rowType == java.sql.Types.CLOB) {
	// Clob value = rs.getClob(columnName);
	// if (value != null) {
	// obj.put(columnName, new
	// JSONObject(this.getContentFromInputStream(value.getAsciiStream())));
	// }
	// } else if (rowType == java.sql.Types.BLOB) {
	// Blob value = rs.getBlob(columnName);
	// if (value != null) {
	// obj.put(columnName, value);
	// }
	// } else if (rowType == java.sql.Types.NVARCHAR) {
	// String value = rs.getNString(columnName);
	// if (value != null) {
	// obj.put(columnName, value);
	// }
	// } else if (rowType == java.sql.Types.TINYINT) {
	// int value = rs.getInt(columnName);
	// if (booleanField) {
	// if (value == 0) {
	// obj.put(columnName, false);
	// } else {
	// obj.put(columnName, true);
	// }
	// } else {
	// obj.put(columnName, value);
	// }
	// } else if (rowType == java.sql.Types.SMALLINT) {
	// int value = rs.getInt(columnName);
	// if (booleanField) {
	// if (value == 0) {
	// obj.put(columnName, false);
	// } else {
	// obj.put(columnName, true);
	// }
	// } else {
	// obj.put(columnName, value);
	// }
	// } else if (rowType == java.sql.Types.ARRAY) {
	// obj.put(columnName, rs.getArray(columnName));
	// } else if (rowType == java.sql.Types.BIGINT) {
	// obj.put(columnName, rs.getInt(columnName));
	// } else {
	// Object value = rs.getObject(columnName);
	// if (value != null) {
	// obj.put(columnName, rs.getObject(columnName));
	// }
	// }
	//
	// return obj;
	// }
	//
	// private String getContentFromInputStream(InputStream is) {
	//
	// BufferedReader br = null;
	// StringBuilder sb = new StringBuilder();
	//
	// String line;
	// try {
	//
	// br = new BufferedReader(new InputStreamReader(is));
	// while ((line = br.readLine()) != null) {
	// sb.append(line);
	// }
	//
	// } catch (IOException e) {
	// } finally {
	// if (br != null) {
	// try {
	// br.close();
	// } catch (IOException e) {
	// }
	// }
	// }
	//
	// return sb.toString();
	//
	// }
	//
	// private String prepareStatement(String statement) {
	// statement = statement.trim();
	// if (statement.startsWith("{") && statement.endsWith("}")) {
	// statement = statement.substring(1, statement.length() - 1).trim();
	// }
	// if (!statement.endsWith(";")) {
	// statement += ";";
	// }
	// return statement;
	// }
	//
	// private Usuario getUser() {
	// Usuario user = null;
	// Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	// if (auth != null) {
	// if (auth.getDetails() != null) {
	// user = cacheService.getUserById(
	// (((AuthenticationContainer) ((UsernamePasswordAuthenticationToken)
	// auth).getDetails())
	// .getUserDetail()).getId());
	// }
	// }
	// return user;
	// }
	//
	// @Override
	// public String addInsertKuduIdentifier(String insertStatement, String id) {
	// throw new NotImplementedException("Operation not supported with Oracle");
	// }
	//
	// @Override
	// public List<List<String>> getTableFromResultSet(String ontologia, ResultSet
	// rs) throws PersistenceException {
	// throw new NotImplementedException("Operation not supported with Oracle");
	// }
	//
	// @Override
	// public String getStringTableFromResultSet(String ontologia, ResultSet rs)
	// throws PersistenceException {
	// throw new NotImplementedException("Operation not supported in Oracle
	// Database.");
	// }
}
