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
package com.minsait.onesait.platform.controlpanel.controller.ksql.flow.dto;

import com.minsait.onesait.platform.config.model.KsqlResource.FlowResourceType;
import com.minsait.onesait.platform.config.model.KsqlResource.KsqlResourceType;

import lombok.Getter;
import lombok.Setter;

public class KsqlResourceDTO {
	
	@Getter
	@Setter
	private String id;
	@Getter
	@Setter
	private String identification;
	@Getter
	@Setter
	private String description;
	@Getter
	@Setter
	private FlowResourceType resourceType;
	@Getter
	@Setter
	private KsqlResourceType ksqlType;
	@Getter
	@Setter
	private String statementText;

	@Getter
	@Setter
	private OntologyJsonSchemaDto ontology;
}
