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
package com.minsait.onesait.platform.controlpanel.gravitee.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
@Data
// @JsonIgnoreProperties(ignoreUnknown=true)
public class ApiUpdate {
	String id;
	String name;
	String version;
	// @JsonAlias({ "context_path", "contextPath" })
	// String contextPath;
	// String endpoint;
	String visibility;
	String description;
	JsonNode paths;
	JsonNode services;
	// @JsonProperty("path_mappings")
	// String[] pathMappings;
	JsonNode proxy;
	JsonNode[] resources;
	@JsonProperty("lifecycle_state")
	String lifeCycleState;

}
