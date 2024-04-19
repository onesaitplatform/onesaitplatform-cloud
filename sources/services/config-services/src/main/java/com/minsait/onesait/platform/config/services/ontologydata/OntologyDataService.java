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
package com.minsait.onesait.platform.config.services.ontologydata;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyRelation;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;

public interface OntologyDataService {

	public ProcessingReport reportJsonSchemaValid(String jsonSchema) throws IOException;

	public List<String> preProcessInsertData(final OperationModel operationModel, final boolean addContextData, final Ontology ontology)
			throws IOException;

	public void checkOntologySchemaCompliance(final JsonNode data, final Ontology ontology);

	String preProcessUpdateData(OperationModel operationModel) throws IOException;

	public String decrypt(String data, String ontologyName, String user) throws OntologyDataUnauthorizedException;

	public void checkTitleCaseSchema(String jsonSchema);

	public void checkRequiredFields(String dbJsonSchema, String newJsonSchema);

	public Set<OntologyRelation> getOntologyReferences(String ontologyIdentification) throws IOException;

	public Map<String, String> getOntologyPropertiesWithPath4Type(String ontologyIdentification, String type);

	public String refJsonSchema(JsonNode schema);

	public void checkSameSchema(String dbJsonSchema, String newJsonSchema);

	public String decryptAllUsers(String data, String ontologyName) throws OntologyDataUnauthorizedException;

	public String encryptQuery(String query, boolean mongo) throws GenericOPException;

}
