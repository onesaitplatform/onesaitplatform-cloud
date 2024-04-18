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

import com.minsait.onesait.platform.config.model.ApiQueryParameter;
import com.minsait.onesait.platform.config.model.ApiQueryParameter.DataType;
import com.minsait.onesait.platform.config.model.ApiQueryParameter.HeaderType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

public class ApiQueryParameterDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	public ApiQueryParameterDTO() {

	}

	public ApiQueryParameterDTO(ApiQueryParameter apiQueryParam) {
		name = apiQueryParam.getName();
		dataType = apiQueryParam.getDataType();
		description = apiQueryParam.getDescription();
		value = apiQueryParam.getValue();
		headerType = apiQueryParam.getHeaderType();
		condition = apiQueryParam.getCondition();
	}

	@Schema(description = "Nombre del Header")
	@Getter
	@Setter
	private String name;

	@Schema(description = "Tipo de Header")
	@Getter
	@Setter
	private DataType dataType;

	@Schema(description = "Descripción del Header")
	@Getter
	@Setter
	private String description;

	@Schema(description = "Valor del Header")
	@Getter
	@Setter
	private String value;

	@Schema(description = "Condición del Header")
	@Getter
	@Setter
	private HeaderType headerType;

	@Schema(description = "Tipo de Parametro")
	@Getter
	@Setter
	private String condition;

}
