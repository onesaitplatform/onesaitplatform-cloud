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
package com.minsait.onesait.platform.controlpanel.controller.apimanager;

import java.io.Serializable;
import java.util.ArrayList;

import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.ApiOperation.Type;
import com.minsait.onesait.platform.config.model.ApiQueryParameter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class ApiOperationDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	public ApiOperationDTO(ApiOperation apiOp) {
		identification = apiOp.getIdentification();
		description = apiOp.getDescription();
		operation = apiOp.getOperation();
		endpoint = apiOp.getEndpoint();
		path = apiOp.getPath();
		postProcess = apiOp.getPostProcess();
		queryParams = new ArrayList<>();
		for (final ApiQueryParameter apiQueryParam : apiOp.getApiqueryparameters()) {
			final ApiQueryParameterDTO apiQueryParameterDTO = new ApiQueryParameterDTO(apiQueryParam);
			queryParams.add(apiQueryParameterDTO);
		}
	}

	@Schema(description = "Identificación de la Operacion")
	@Getter
	@Setter
	private String identification;

	@Schema(description = "Descripción de la Operacion")
	@Getter
	@Setter
	private String description;

	@Schema(description = "Tipo de Operacion")
	@Getter
	@Setter
	private Type operation;

	@Schema(description = "Enpoint Particular de la Operacion")
	@Getter
	@Setter
	private String endpoint;

	@Schema(description = "Path de la Operacion")
	@Getter
	@Setter
	private String path;

	@Schema(description = "QueryParams de la Operacion")
	@Getter
	@Setter
	private ArrayList<ApiQueryParameterDTO> queryParams;

	@Schema(description = "Postprocesado de la Operacion")
	@Getter
	@Setter
	private String postProcess;

}
