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

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;

import com.minsait.onesait.platform.config.model.Ontology.RtdbCleanLapse;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.Ontology.RtdbToHdbStorage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class OntologySimplified implements Comparable<OntologySimplified> {

	@NotNull
	@Getter
	@Setter
	private String identification;

	@NotNull
	@Getter
	@Setter
	private String jsonSchema;

	@Getter
	@Setter
	private String template;

	@Getter
	@Setter
	private String templateCategory;

	@NotNull
	@Getter
	@Setter
	private boolean active;

	@NotNull
	@Getter
	@Setter
	private boolean rtdbClean;

	@Enumerated(EnumType.STRING)
	@Getter
	@Setter
	private RtdbCleanLapse rtdbCleanLapse;

	@NotNull
	@Getter
	@Setter
	private boolean rtdbToHdb;

	@Enumerated(EnumType.STRING)
	@Getter
	@Setter
	private RtdbToHdbStorage rtdbToHdbStorage;

	@NotNull
	@Getter
	@Setter
	private boolean isPublic;

	@NotNull
	@Getter
	@Setter
	private String description;

	@NotNull
	@Getter
	@Setter
	private String metainf;

	/*
	 * @Getter
	 * 
	 * @Setter private OntologyKPI ontologyKPI;
	 */
	@Getter
	@Setter
	@NotNull
	@Enumerated(EnumType.STRING)
	private RtdbDatasource rtdbDatasource;

	@NotNull
	@Getter
	@Setter
	private boolean allowsCypherFields;

	@NotNull
	@Getter
	@Setter
	private boolean allowsCreateTopic;

	@NotNull
	@Getter
	@Setter
	String owner;

	@NotNull
	@Getter
	@Setter
	private String createdAt;

	@NotNull
	@Getter
	@Setter
	private String updatedAt;

	public OntologySimplified(com.minsait.onesait.platform.config.model.Ontology ontology) {
		this.identification = ontology.getIdentification();
		this.description = ontology.getDescription();
		this.metainf = ontology.getMetainf();
		this.owner = ontology.getUser().getUserId();
		this.jsonSchema = ontology.getJsonSchema();
		this.active = ontology.isActive();
		this.rtdbClean = ontology.isRtdbClean();
		this.rtdbCleanLapse = ontology.getRtdbCleanLapse();
		this.rtdbToHdb = ontology.isRtdbToHdb();
		if (!this.rtdbToHdb) {
			this.rtdbToHdbStorage = null;
		} else {
			this.rtdbToHdbStorage = ontology.getRtdbToHdbStorage();
		}
		this.isPublic = ontology.isPublic();
		this.rtdbDatasource = ontology.getRtdbDatasource();
		this.allowsCypherFields = ontology.isAllowsCypherFields();
		this.allowsCreateTopic = ontology.isAllowsCreateTopic();
		this.createdAt = ontology.getCreatedAt().toString();
		this.updatedAt = ontology.getUpdatedAt().toString();
		this.template = ontology.getDataModel().getIdentification();
		this.templateCategory = ontology.getDataModel().getType();
	}

	@Override
	public int compareTo(OntologySimplified o) {
		return identification.compareTo(o.getIdentification());
	}
}
