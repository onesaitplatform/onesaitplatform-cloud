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
package com.minsait.onesait.platform.controlpanel.rest.management.api.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.ApiOperation.Type;
import com.minsait.onesait.platform.config.model.ApiQueryParameter;
import com.minsait.onesait.platform.controlpanel.controller.apimanager.ApiQueryParameterDTO;

import edu.emory.mathcs.backport.java.util.Collections;
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

	@ApiModelProperty(value = "ID de la Operacion")
	@Getter
	@Setter
	private String id;

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
		identification = apiOp.getIdentification();
		description = apiOp.getDescription();
		operation = apiOp.getOperation();
		endpoint = apiOp.getEndpoint();
		path = apiOp.getPath();
		postProcess = apiOp.getPostProcess();
		queryParams = new ArrayList<>();
		id = apiOp.getId();

		for (final ApiQueryParameter apiQueryParam : apiOp.getApiqueryparameters()) {
			final ApiQueryParameterDTO apiQueryParameterDTO = new ApiQueryParameterDTO(apiQueryParam);
			queryParams.add(apiQueryParameterDTO);
		}

		Collections.sort(queryParams, new Comparator<ApiQueryParameterDTO>() {
			@Override
			public int compare(ApiQueryParameterDTO s1, ApiQueryParameterDTO s2) {
				return s1.getName().compareToIgnoreCase(s2.getName());
			}
		});
	}

	public Api toAPI() {
		return null;

	}

}
