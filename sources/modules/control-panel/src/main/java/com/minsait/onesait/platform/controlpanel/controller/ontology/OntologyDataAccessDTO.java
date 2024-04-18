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

import com.minsait.onesait.platform.config.model.OntologyDataAccess;

import lombok.Getter;
import lombok.Setter;

public class OntologyDataAccessDTO {

	public OntologyDataAccessDTO(OntologyDataAccess ontologyDataAccessCreated) {
		this.id = ontologyDataAccessCreated.getId();
		this.ontologyId = ontologyDataAccessCreated.getOntology().getId();
		this.ontologyIdentification = ontologyDataAccessCreated.getOntology().getIdentification();
		if (ontologyDataAccessCreated.getUser()!=null) {
			this.userId = ontologyDataAccessCreated.getUser().getUserId();
		}
		if (ontologyDataAccessCreated.getAppRole()!=null) {
			this.appRole = ontologyDataAccessCreated.getAppRole().getId();
			this.appRoleName = ontologyDataAccessCreated.getAppRole().getName();
			this.appName = ontologyDataAccessCreated.getAppRole().getApp().getIdentification();
		}
		this.rule = ontologyDataAccessCreated.getRule();
	}

	@Getter
	@Setter
	private String id;

	@Getter
	@Setter
	private String ontologyId;
	
	@Getter
	@Setter
	private String ontologyIdentification;
	
	@Getter
	@Setter
	private boolean ontologyPermission;
	
	@Getter
	@Setter
	private String userId;

	@Getter
	@Setter
	private String appName;
	
	@Getter
	@Setter
	private String appRole;
	
	@Getter
	@Setter
	private String appRoleName;

	@Getter
	@Setter
	private String rule;

}
