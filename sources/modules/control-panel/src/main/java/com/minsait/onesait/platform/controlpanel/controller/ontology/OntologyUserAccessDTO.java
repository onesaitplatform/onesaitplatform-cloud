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
package com.minsait.onesait.platform.controlpanel.controller.ontology;

import com.minsait.onesait.platform.config.model.OntologyUserAccess;

import lombok.Getter;
import lombok.Setter;

public class OntologyUserAccessDTO {

	public OntologyUserAccessDTO(OntologyUserAccess ontologyUserAccessCreated) {
		this.id = ontologyUserAccessCreated.getId();
		this.typeName = ontologyUserAccessCreated.getOntologyUserAccessType().getName();
		this.userId = ontologyUserAccessCreated.getUser().getUserId();
		this.ontologyId = ontologyUserAccessCreated.getOntology().getId();
		this.userFullName = ontologyUserAccessCreated.getUser().getFullName();
		this.ontologyIdentification = ontologyUserAccessCreated.getOntology().getIdentification();
	}

	@Getter
	@Setter
	private String id;

	@Getter
	@Setter
	private String userId;

	@Getter
	@Setter
	private String typeName;

	@Getter
	@Setter
	private String ontologyId;

	@Getter
	@Setter
	private String userFullName;

	@Getter
	@Setter
	private String ontologyIdentification;
}
