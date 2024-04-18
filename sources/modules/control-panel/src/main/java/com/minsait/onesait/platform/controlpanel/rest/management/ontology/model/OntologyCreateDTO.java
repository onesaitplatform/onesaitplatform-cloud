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
package com.minsait.onesait.platform.controlpanel.rest.management.ontology.model;

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
public class OntologyCreateDTO {
	@Getter
	@Setter
	@NotNull
	private String identification;

	@Getter
	@Setter
	@NotNull
	private String description;

	@Getter
	@Setter
	@NotNull
	private String metainf;

	@Getter
	@Setter
	@NotNull
	private boolean active;

	@Getter
	@Setter
	@NotNull
	private boolean isPublic;

	@Getter
	@Setter
	@NotNull
	private boolean allowsCypherFields;

	@Getter
	@Setter
	@NotNull
	private String jsonSchema;

	@Getter
	@Setter
	@NotNull
	private boolean rtdbClean;

	@Getter
	@Setter
	private RtdbCleanLapse rtdbCleanLapse;

	@Getter
	@Setter
	private RtdbDatasource rtdbDatasource;

	@Getter
	@Setter
	@NotNull
	private boolean rtdbToHdb;

	@Getter
	@Setter
	private RtdbToHdbStorage rtdbToHdbStorage;

	@Getter
	@Setter
	@NotNull
	private boolean allowsCreateTopic;

	@Getter
	@Setter
	private boolean allowsCreateNotificationTopic;

	@Getter
	@Setter
	private boolean contextDataEnabled;

	@Getter
	@Setter
	private int shards;

	@Getter
	@Setter
	private int replicas;

	@Getter
	@Setter
	private String patternField;

	@Getter
	@Setter
	private String patternFunction;

	@Getter
	@Setter
	private int substringStart;

	@Getter
	@Setter
	private int substringEnd;
}
