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
package com.minsait.onesait.platform.controlpanel.controller.jsontool;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.services.datamodel.DataModelService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

@Component
public class JsonToolUtils {
	@Autowired
	private DataModelService dataModelService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private UserService userService;
	private static final String DATAMODEL_DEFAULT_NAME = "EmptyBase";
	private static final String SCHEMA_DRAFT_VERSION = "http://json-schema.org/draft-04/schema#";
	private static final String DEFAULT_META_INF = "imported,json";
	private final ObjectMapper mapper = new ObjectMapper();

	public Ontology createOntology(String identification, String description, RtdbDatasource datasource, String schema, boolean contextdata)
			throws IOException {

		final Ontology ontology = new Ontology();
		ontology.setJsonSchema(completeSchema(schema, identification, description).toString());
		ontology.setIdentification(identification);
		ontology.setActive(true);
		ontology.setDataModel(dataModelService.getDataModelByName(DATAMODEL_DEFAULT_NAME));
		ontology.setDescription(description);
		ontology.setUser(userService.getUser(utils.getUserId()));
		ontology.setMetainf(DEFAULT_META_INF);
		ontology.setRtdbDatasource(datasource);
		ontology.setContextDataEnabled(contextdata);
		return ontology;
	}

	public JsonNode completeSchema(String schema, String identification, String description) throws IOException {
		final JsonNode schemaSubTree = mapper.readTree(schema);
		((ObjectNode) schemaSubTree).put("type", "object");
		((ObjectNode) schemaSubTree).put("description", "Info " + identification);

		((ObjectNode) schemaSubTree).put("$schema", SCHEMA_DRAFT_VERSION);
		((ObjectNode) schemaSubTree).put("title", identification);

		((ObjectNode) schemaSubTree).put("additionalProperties", true);
		return schemaSubTree;
	}
}
