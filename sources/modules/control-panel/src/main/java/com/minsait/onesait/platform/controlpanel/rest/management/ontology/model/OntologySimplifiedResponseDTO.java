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
package com.minsait.onesait.platform.controlpanel.rest.management.ontology.model;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.minsait.onesait.platform.config.model.Ontology;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class OntologySimplifiedResponseDTO {

	@NotNull
	@Getter
	@Setter
	private String id;
	
	@NotNull
	@Getter
	@Setter
	private String identification;
	
	@NotNull
	@Getter
	@Setter
	private String userId;
	
	@NotNull
	@Getter
	@Setter
	private boolean active;
	
	@NotNull
	@Getter
	@Setter
	private String description;
	
	@NotNull
	@Getter
	@Setter
	private boolean isPublic;
	
	@NotNull
	@Getter
	@Setter
	private String metainf;
	
	@Getter
	@Setter
	private String ontologyClass;
	
	@Getter
	@Setter
	private String topic;
	
	@Getter
	@Setter
	private List<OntologyUserAccessSimplified> authorizations;

	@Getter
	@Setter
	private DataModelDTO dataModel;
	
	@Getter
	@Setter
	private String msg;
	
	public OntologySimplifiedResponseDTO(Ontology ontology) {
		this.id = ontology.getId();
		this.identification = ontology.getIdentification();
		this.userId = ontology.getUser().getUserId();
		this.active = ontology.isActive();
		this.description = ontology.getDescription();
		this.isPublic = ontology.isPublic();
		this.metainf = ontology.getMetainf();
		this.ontologyClass = ontology.getOntologyClass();
		//this.topic = ontology.getTopic();
		this.dataModel = DataModelDTO.fromDataModel(ontology.getDataModel());
		this.authorizations = new ArrayList<>();
		this.msg = "OK";
		
	}
	
}
