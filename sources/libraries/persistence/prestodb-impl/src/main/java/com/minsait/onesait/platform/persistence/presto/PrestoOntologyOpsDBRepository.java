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
package com.minsait.onesait.platform.persistence.presto;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.minsait.onesait.platform.commons.model.MultiDocumentOperationResult;
import com.minsait.onesait.platform.persistence.interfaces.BasicOpsDBRepository;
import com.minsait.onesait.platform.persistence.presto.generator.model.statements.PrestoCreateStatement;

public interface PrestoOntologyOpsDBRepository extends BasicOpsDBRepository {

	List<String> getTables(String datasourceName);
		
	public List<String> getCatalogs();

	public List<String> getSchemas(String catalog);

	public List<String> getTables(String catalog, String schema);
	
	/**
	 * Returns the first element of the table. Used to generate document schemas.
	 * @param datasource
	 * @param query
	 * @return List<String>
	 */
	List<String> getInstanceFromTable(String datasource, String query);

	Map<String, Integer> getTableTypes(String catalog, String schema, String ontology) throws SQLException;

	String executeQuery(String ontology, String query);

	String getTableMetadata(String catalog, String schema, String collection);

	public List<String> getStringSupportedFieldDataTypes();

	public List<String> getStringSupportedConstraintTypes();

	public String getSQLCreateStatment(PrestoCreateStatement statement);

	public String getSqlTableDefinitionFromSchema(String ontology, String schema);

	MultiDocumentOperationResult insertNative(String ontology, String insertStmt, boolean includeIds);

}
