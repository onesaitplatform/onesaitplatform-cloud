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
package com.minsait.onesait.platform.controlpanel.controller.apimanager;

import java.io.Serializable;
import java.util.ArrayList;

import com.minsait.onesait.platform.config.model.ApiHeader;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.ApiOperation.Type;
import com.minsait.onesait.platform.config.model.ApiQueryParameter;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

public class ApiOperationDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	public ApiOperationDTO() {

	}

	public ApiOperationDTO(ApiOperation apiOp) {
		this.identification = apiOp.getIdentification();
		this.description = apiOp.getDescription();
		this.operation = apiOp.getOperation();
		this.endpoint = apiOp.getEndpoint();
		this.path = apiOp.getPath();
		this.postProcess = apiOp.getPostProcess();
		this.headers = new ArrayList<>();
		this.queryParams = new ArrayList<>();
		for (ApiHeader apiheader : apiOp.getApiheaders()) {
			ApiHeaderDTO apiheaderDTO = new ApiHeaderDTO(apiheader);
			this.headers.add(apiheaderDTO);
		}
		for (ApiQueryParameter apiQueryParam : apiOp.getApiqueryparameters()) {
			ApiQueryParameterDTO apiQueryParameterDTO = new ApiQueryParameterDTO(apiQueryParam);
			this.queryParams.add(apiQueryParameterDTO);
		}
	}

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

	@ApiModelProperty(value = "Headers de la Operacion")
	@Getter
	@Setter
	private ArrayList<ApiHeaderDTO> headers;

	@ApiModelProperty(value = "QueryParams de la Operacion")
	@Getter
	@Setter
	private ArrayList<ApiQueryParameterDTO> queryParams;

	@ApiModelProperty(value = "Postprocesado de la Operacion")
	@Getter
	@Setter
	private String postProcess;

}
