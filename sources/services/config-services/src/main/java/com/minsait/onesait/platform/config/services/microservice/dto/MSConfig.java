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
package com.minsait.onesait.platform.config.services.microservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MSConfig {

	@Schema(required = true, ref = "true")
	private boolean createGitlab;
	@Schema(required = true)
	private boolean defaultGitlab;
	@Schema(required = true, ref = "true")
	private boolean defaultJenkins;
	@Schema(required = true, ref = "true")
	private boolean defaultCaaS;
	private String modelRunId;
	private String sources;
	private String docker;
	private String ontology;
	private String notebook;
	private String jenkinsView;
	private String gitlabGroup;
	private boolean dependencyUserAdmin;
	private boolean dependencyHazelcast;
	private boolean dependencyDrools;
	private boolean moduleSSO;
	private boolean moduleUserAdmin;
	private boolean moduleSpringBatch;

}
