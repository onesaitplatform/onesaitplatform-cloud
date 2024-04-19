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
package com.minsait.onesait.platform.serverless.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.minsait.onesait.platform.serverless.model.Function;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class FunctionInfo {

	private String name;
	private String pathToYaml;//path dentro del repo a la función i.e. functions/randomizer/
	private String image;
	private Integer memory;
	private List<String> invokeEndpoints = new ArrayList<>();
	private Map<String, Object> environment = new HashMap<>();

	public FunctionInfo (Function function) {
		name = function.getName();
		pathToYaml = function.getPathToYaml();
	}
}
