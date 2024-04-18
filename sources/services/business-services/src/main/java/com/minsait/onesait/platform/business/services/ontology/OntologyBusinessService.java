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
package com.minsait.onesait.platform.business.services.ontology;

import java.util.HashMap;
import java.util.List;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.services.ontology.OntologyConfiguration;

public interface OntologyBusinessService {
	public boolean existsOntology(String identificacion);
	
	public void createOntology(Ontology ontology, String userId, OntologyConfiguration config) throws OntologyBusinessServiceException;

	public List<String> getTablesFromDatasource(String datasource);

	public String getInstance(String datasource, String collection);

	JsonNode completeSchema(String schema, String identification, String description) throws IOException;

	JsonNode organizeRootNodeIfExist(String schema) throws IOException;

	public String getRelationalSchema(String datasource, String collection);
	
	public HashMap<String, String> getAditionalDBConfig(Ontology ontology);

	public void updateOntology(Ontology ontology, OntologyConfiguration config, boolean hasDocuments) throws OntologyBusinessServiceException;
}
