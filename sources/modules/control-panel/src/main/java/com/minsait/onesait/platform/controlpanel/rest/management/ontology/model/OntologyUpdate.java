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

import javax.validation.constraints.NotNull;

import com.minsait.onesait.platform.config.model.Ontology.RtdbCleanLapse;
import com.minsait.onesait.platform.config.model.Ontology.RtdbToHdbStorage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class OntologyUpdate {
	
	@Getter
	@Setter
	@NotNull
	private String identification;
	
	@Getter
	@Setter
	private String description;
	
	@Getter
	@Setter
	private String metainf;
	
	@Getter
	@Setter
	private Boolean active;
	
	@Getter
	@Setter
	private Boolean isPublic;
	
	@Getter
	@Setter
	private Boolean allowsCypherFields;
	
	@Getter
	@Setter
	private String jsonSchema;
	
	@Getter
	@Setter
	private Boolean rtdbClean;
	
	@Getter
	@Setter
	private RtdbCleanLapse rtdbCleanLapse;
	
	@Getter
	@Setter
	private Boolean rtdbToHdb;
	
	@Getter
	@Setter
	private RtdbToHdbStorage rtdbToHdbStorage;
	
	@Getter
	@Setter
	private Boolean allowsCreateTopic;

}
