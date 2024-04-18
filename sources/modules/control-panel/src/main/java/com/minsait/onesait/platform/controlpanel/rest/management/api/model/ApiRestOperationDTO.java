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
package com.minsait.onesait.platform.controlpanel.rest.management.api.model;

import java.io.Serializable;
import java.util.ArrayList;

import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.ApiOperation.Type;
import com.minsait.onesait.platform.config.model.ApiQueryParameter;
import com.minsait.onesait.platform.controlpanel.controller.apimanager.ApiQueryParameterDTO;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class ApiRestOperationDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "Identificación de la Operacion")
	@Getter
	@Setter
	private String identification;

	@ApiModelProperty(value = "Descripción de la Operacion")
	@Getter
	@Setter
	private String description;

	@ApiModelProperty(value = "Tipo de Operacion")
	@Getter
	@Setter
	private Type operation;

	@ApiModelProperty(value = "Enpoint Particular de la Operacion")
	@Getter
	@Setter
	private String endpoint;

	@ApiModelProperty(value = "Path de la Operacion")
	@Getter
	@Setter
	private String path;

	@ApiModelProperty(value = "QueryParams de la Operacion")
	@Getter
	@Setter
	private ArrayList<ApiQueryParameterDTO> queryParams;

	@ApiModelProperty(value = "Postprocesado de la Operacion")
	@Getter
	@Setter
	private String postProcess;

	public ApiRestOperationDTO(ApiOperation apiOp) {
		this.identification = apiOp.getIdentification();
		this.description = apiOp.getDescription();
		this.operation = apiOp.getOperation();
		this.endpoint = apiOp.getEndpoint();
		this.path = apiOp.getPath();
		this.postProcess = apiOp.getPostProcess();
		this.queryParams = new ArrayList<>();

		for (ApiQueryParameter apiQueryParam : apiOp.getApiqueryparameters()) {
			ApiQueryParameterDTO apiQueryParameterDTO = new ApiQueryParameterDTO(apiQueryParam);
			this.queryParams.add(apiQueryParameterDTO);
		}
	}

	public Api toAPI() {
		return null;

	}

}
