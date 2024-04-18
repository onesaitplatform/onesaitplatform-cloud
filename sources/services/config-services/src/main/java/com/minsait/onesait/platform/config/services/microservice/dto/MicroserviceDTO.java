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
package com.minsait.onesait.platform.config.services.microservice.dto;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.minsait.onesait.platform.config.components.CaasConfiguration;
import com.minsait.onesait.platform.config.components.JenkinsConfiguration;
import com.minsait.onesait.platform.config.components.RancherConfiguration;
import com.minsait.onesait.platform.config.model.Microservice.TemplateType;
import com.minsait.onesait.platform.git.GitlabConfiguration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class MicroserviceDTO {

	private String id;
	@NotNull
	@Schema(required = true)
	private String name;
	private String owner;
	private String jenkins;
	private String gitlab;
	private String caas;
	private String caasUrl;
	private boolean isDeployed;
	private String lastBuild;
	private String deploymentUrl;

	private MSConfig config;

	private JenkinsConfiguration jenkinsConfiguration;
	private RancherConfiguration rancherConfiguration;
	private CaasConfiguration openshiftConfiguration;
	private GitlabConfiguration gitlabConfiguration;
	@NotNull
	@Schema(required = true)
	private String contextPath;
	@NotNull
	@Schema(required = true)
	private int port;
	private String jobName;
	@Schema(required = true)
	String template;

	ZipMicroservice zipInfo;
	GitTemplateMicroservice gitTemplate;
	private String jobUrl;
}
