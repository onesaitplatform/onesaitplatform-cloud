/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor

public class OntologyElasticDTO {
	@Getter
	@Setter
	private int shards;
	@Getter
	@Setter
	private int replicas;
	@Getter
	@Setter
	private Boolean customConfig;
	@Getter
	@Setter
	private Boolean templateConfig;
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
	@Getter
	@Setter
	private Boolean customIdConfig;
	@Getter
	@Setter
	private String customIdField;
	@Getter
	@Setter
	private Boolean allowsUpsertById;

}
