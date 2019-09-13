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

public interface JsonRelationalHelper {

	public String getInsertStatement(String ontology, String instance);
	//
	// public String getInsertStatement(String ontology, String instance, String
	// id);
	//
	// public String getQueryFromNative(String nativeQuery);
	//
	// public String addInsertIdentifier(String insertStatement);
	//
	// public String addInsertKuduIdentifier(String insertStatement, String id);
	//
	// public String getJsonFromResultSet(String table, ResultSet data);
	//
	// public String getJsonFromResultSet(String table, ResultSet data, List<String>
	// lFields);
	//
	// public String addSelectWhereCondition(String selectStatement, String
	// condition) throws SQLStatementParseException;

	// public void checkAuthorized(String statement);
	//
	// public String getSelectObjectIdFromStatement(String updateStatement) throws
	// PersistenceException;
	//
	// public List<String> getAffectedIdsFromResultSet(ResultSet rs);
	//
	// public List<String> getJsonListFromResultset(String table, ResultSet data);
	//
	// public List<String> getJsonListFromResultset(String table, ResultSet data,
	// List<String> lFields);
	//
	// public Map<String, List<String>> getExpandFields(String selectStatement)
	// throws SQLSyntaxErrorException;
	//
	// public String buildExpandMainQuery(String selectStatement, Set<String>
	// expandFields);
	//
	// public String getExpandJsonResult(String baseOntology, ResultSet rs,
	// Map<String, List<String>> mExpandFields)
	// throws SQLException, JSONException;
	//
	// public List<String> getSelectTables(String selectStatement);
	//
	// public JSONArray convertJsonArray(String table, ResultSet rs) throws
	// SQLException, JSONException;
	//
	// public List<String> convertList(String table, ResultSet rs) throws
	// SQLException, JSONException;
	//
	// public List<JSONObject> convertJsonList(String table, ResultSet rs) throws
	// SQLException, JSONException;
	//
	// public String getUpdateStatement(String ontology, String query, String
	// datos);
	//
	// public String getJsonDataFromInsert(String ontology, String insert) throws
	// JSONException, Exception;
	//
	// public String prepareOracleBoolean(String statement) throws
	// PersistenceException;
	//
	// public List<String> getStatementTables(String statement);
	//
	// public List<List<String>> getTableFromResultSet(String ontologia, ResultSet
	// rs) throws PersistenceException;
	//
	// public String getStringTableFromResultSet(String ontologia, ResultSet rs)
	// throws PersistenceException;

}
