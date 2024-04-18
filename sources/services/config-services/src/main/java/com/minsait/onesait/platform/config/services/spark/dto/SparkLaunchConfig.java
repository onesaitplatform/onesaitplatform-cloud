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
package com.minsait.onesait.platform.config.services.spark.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SparkLaunchConfig {

	@Getter
	@Setter
	private String driverExtraClassPath;
	@Getter
	@Setter
	private String driverExtraJavaOpts;
	@Getter
	@Setter
	private String driverExtraLibPath;
	@Getter
	@Setter
	@JsonInclude(JsonInclude.Include.ALWAYS)
	private String driverMemory;
	@Getter
	@Setter
	private String executorExtraClassPath;
	@Getter
	@Setter
	private String executorExtraJavaOpts;
	@Getter
	@Setter
	private String executorExtraLibPath;
	@Getter
	@Setter
	@JsonInclude(JsonInclude.Include.ALWAYS)
	private String executorMemory;
	@Getter
	@Setter
	@JsonInclude(JsonInclude.Include.ALWAYS)
	private String executorCores;
}
