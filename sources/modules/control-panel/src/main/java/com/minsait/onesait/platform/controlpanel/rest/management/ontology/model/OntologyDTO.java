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
package com.minsait.onesait.platform.controlpanel.rest.management.ontology.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbCleanLapse;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.Ontology.RtdbToHdbStorage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @JsonSubTypes.Type(OntologyRestDTO.class), @JsonSubTypes.Type(OntologyTimeSeriesDTO.class),
	@JsonSubTypes.Type(OntologyVirtualDTO.class), @JsonSubTypes.Type(OntologyKpiDTO.class)})
@NoArgsConstructor
@AllArgsConstructor
public class OntologyDTO {

	@Getter
	@Setter
	private String createdAt;

	@Getter
	@Setter
	private String updatedAt;
	
	@NotNull
	@Getter
	@Setter
	private String identification;
	
	@NotNull
	@Getter
	@Setter
	private String userId;
	
	@Getter
	@Setter
	private boolean active = true;
	
	@Getter
	@Setter
	private boolean allowsCreateTopic = false;
	
	@Getter
	@Setter
	private boolean allowsCypherFields = false;

	@Getter
	@Setter
	private String dataModelVersion;
	
	@Getter
	@Setter
	private String description;
	
	@Getter
	@Setter
	private boolean isPublic = false;
	
	@NotNull
	@Getter
	@Setter
	private String jsonSchema;
	
	@Getter
	@Setter
	private String metainf;
	
	@Getter
	@Setter
	private String ontologyClass;
	
	@Getter
	@Setter
	private boolean rtdbClean = false;
	
	@Enumerated(EnumType.STRING)
	@Getter
	@Setter
	private RtdbCleanLapse rtdbCleanLapse = RtdbCleanLapse.NEVER;
	
	@Getter
	@Setter
	@Enumerated(EnumType.STRING)
	private RtdbDatasource rtdbDatasource = RtdbDatasource.MONGO;
	
	@Getter
	@Setter
	private boolean rtdbToHdb = false;
	
	@Enumerated(EnumType.STRING)
	@Getter
	@Setter
	private RtdbToHdbStorage rtdbToHdbStorage = RtdbToHdbStorage.MONGO_GRIDFS;
	
	@Getter
	@Setter
	private String topic;
	
	@Getter
	@Setter
	private String xmlDiagram;

	@Getter
	@Setter
	private List<OntologyUserAccessSimplified> authorizations = new ArrayList<>();

	@Getter
	@Setter
	private DataModelDTO dataModel;
	
	@Getter
	@Setter
	private String dataModelIdentification = "EmptyBase";
	
	public OntologyDTO(Ontology ontology) {
		this.createdAt = ontology.getCreatedAt().toString();
		this.updatedAt = ontology.getUpdatedAt().toString();
		this.identification = ontology.getIdentification();
		this.userId = ontology.getUser().getUserId();
		this.active = ontology.isActive();
		this.allowsCreateTopic = ontology.isAllowsCreateTopic();
		this.allowsCypherFields = ontology.isAllowsCypherFields();
		this.dataModelVersion = ontology.getDataModelVersion();
		this.description = ontology.getDescription();
		this.isPublic = ontology.isPublic();
		this.jsonSchema = ontology.getJsonSchema();
		this.metainf = ontology.getMetainf();
		this.ontologyClass = ontology.getOntologyClass();
		this.rtdbClean = ontology.isRtdbClean();
		this.rtdbCleanLapse = ontology.getRtdbCleanLapse();
		this.rtdbDatasource = ontology.getRtdbDatasource();
		this.rtdbToHdb = ontology.isRtdbToHdb();
		this.rtdbToHdbStorage = ontology.getRtdbToHdbStorage();
		this.topic = ontology.getTopic();
		this.xmlDiagram = ontology.getXmlDiagram();
		this.dataModel = DataModelDTO.fromDataModel(ontology.getDataModel());
		this.authorizations = new ArrayList<>();
		
	}

}
