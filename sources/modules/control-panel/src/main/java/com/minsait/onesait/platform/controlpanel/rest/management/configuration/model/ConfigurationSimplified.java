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
package com.minsait.onesait.platform.controlpanel.rest.management.configuration.model;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.minsait.onesait.platform.config.model.Configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class ConfigurationSimplified {

	private String id;
	private String username;
	@NotNull
	private Configuration.Type type;
	@NotNull
	private String description;
	@NotNull
	private String identification;
	@NotNull
	private String environment;
	@NotNull
	private String yml;

	public ConfigurationSimplified(Configuration configuration) {
		id = configuration.getId();
		username = configuration.getUser().getUserId();
		type = configuration.getType();
		description = configuration.getDescription();
		identification = configuration.getSuffix();
		environment = configuration.getEnvironment();
		yml = configuration.getYmlConfig();

	}

}
