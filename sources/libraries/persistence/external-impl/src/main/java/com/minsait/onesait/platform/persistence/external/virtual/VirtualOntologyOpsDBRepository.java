/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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
package com.minsait.onesait.platform.persistence.external.virtual;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource.VirtualDatasourceType;
import com.minsait.onesait.platform.config.services.ontology.dto.VirtualDatasourceInfoDTO;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.CreateStatement;
import com.minsait.onesait.platform.persistence.interfaces.BasicOpsDBRepository;

public interface VirtualOntologyOpsDBRepository extends BasicOpsDBRepository {

	List<String> getTables(String datasourceName);
	
	VirtualDatasourceInfoDTO getInfo(String datasourceName);
	
	public List<String> getDatabases(String datasource);

	public List<String> getSchemasDB(String datasource, String database);

	public List<String> getTables(String datasource, String database, String schema);
	
	/**
	 * Returns the first element of the table. Used to generate document schemas.
	 * @param datasource
	 * @param query
	 * @return List<String>
	 */
	List<String> getInstanceFromTable(String datasource, String query);

	Map<String, Integer> getTableTypes(String datasource, String database, String schema, String ontology) throws SQLException;

	String executeQuery(String ontology, String query);

	String getTableMetadata(String datasource, String database, String schema, String collection);

	String getSqlTableDefinitionFromSchema(String ontology, String schema, VirtualDatasourceType datasource);

	String getSQLCreateStatment(CreateStatement statement, VirtualDatasourceType datasource);

	public List<String> getStringSupportedFieldDataTypes();

	public List<String> getStringSupportedConstraintTypes();

}
