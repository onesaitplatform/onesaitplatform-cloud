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
package com.minsait.onesait.platform.quartz.services.ontologyKPI;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.OntologyKPI;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.ontology.OntologyConfiguration;

public interface OntologyKPIService {

	static final String QUERY_SQL = "SQL";
	static final String QUERY_NATIVE = "NATIVE";
	static final String DATAMODEL_DEFAULT_NAME = "EmptyBase";
	static final String SCHEMA_DRAFT_VERSION = "http://json-schema.org/draft-04/schema#";

	void scheduleKpi(OntologyKPI oKPI);

	void unscheduleKpi(OntologyKPI oKPI);

	JsonNode completeSchema(String schema, String identification, String description) throws IOException;

	JsonNode organizeRootNodeIfExist(String schema) throws IOException;
	
	public void cloneOntologyKpi(Ontology ontology, Ontology clonnedOntology, User user);

}
