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
package com.minsait.onesait.platform.business.services.ontology;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource.VirtualDatasourceType;
import com.minsait.onesait.platform.config.services.ontology.OntologyConfiguration;
import com.minsait.onesait.platform.config.services.ontology.dto.VirtualDatasourceInfoDTO;

public interface OntologyBusinessService {
	public boolean existsOntology(String identificacion);

	public void createOntology(Ontology ontology, String userId, OntologyConfiguration config)
			throws OntologyBusinessServiceException;

	public List<String> getTablesFromDatasource(String datasource);

	public List<String> getDatabasesFromDatasource(String datasource);

	public List<String> getTablesFromDatasource(String datasource, String database, String schema);

	public List<String> getSchemasFromDatasourceDatabase(String datasource, String database);
	
	public List<Map<String, Object>> getTableInformationFromDatasource(String datasource, String database, String schema);

	public String getInstance(String datasource, String collection);

	JsonNode completeSchema(String schema, String identification, String description) throws IOException;

	JsonNode organizeRootNodeIfExist(String schema) throws IOException;

	public String getRelationalSchema(String datasource, String database, String schema, String collection);

	public HashMap<String, String> getAditionalDBConfig(Ontology ontology);

	public void updateOntology(Ontology ontology, OntologyConfiguration config, boolean hasDocuments)
			throws OntologyBusinessServiceException;

	public String getSqlTableDefinitionFromSchema(String ontology, String schema, VirtualDatasourceType datasource);

	public String getSQLCreateTable(CreateStatementBusiness statement, VirtualDatasourceType datasource)
			throws OntologyBusinessServiceException;

	public Object getStringSupportedFieldDataTypes();

	public Object getStringSupportedConstraintTypes();

	public void deleteOntology(String id, String userId) throws JsonProcessingException;

	public void cloneOntology(String id, String identification, String userId, OntologyConfiguration config)
			throws OntologyBusinessServiceException;

	public VirtualDatasourceInfoDTO getInfoFromDatasource(String datasource);

	void uploadHistoricalFile(MultipartFile file, String ontology) throws OntologyBusinessServiceException;

	void deleteOntologyAndData(String id, String userId, boolean deleteData)
			throws OntologyBusinessServiceException, JsonProcessingException;

	JSONArray ontologyBulkGeneration(HttpServletRequest request, String userId);

	List<Map<String, Object>> getTablePKInformation(String datasource, String database, String schema);
	
	boolean hasDocuments(Ontology ontology);

}
