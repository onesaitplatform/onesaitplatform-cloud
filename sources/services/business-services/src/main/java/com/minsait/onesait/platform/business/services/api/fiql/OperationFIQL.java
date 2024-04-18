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
package com.minsait.onesait.platform.business.services.api.fiql;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;

import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.services.apimanager.dto.OperacionDTO;

public final class OperationFIQL {
	

	static Locale locale = LocaleContextHolder.getLocale();
	
	private OperationFIQL() {
		throw new AssertionError("Instantiating utility class...");
	}

	public static List<OperacionDTO> toOperacionDTO(List<ApiOperation> operaciones) {
		List<OperacionDTO> operacionesDTO = new ArrayList<>();
		for (ApiOperation operacion : operaciones) {
			operacionesDTO.add(toOperacionDTO(operacion));
		}
		return operacionesDTO;
	}
	
	public static OperacionDTO toOperacionDTO(ApiOperation operacion) {
		OperacionDTO operacionDTO = new OperacionDTO();
		
		operacionDTO.setDescription(operacion.getDescription());
		operacionDTO.setEndpoint(operacion.getEndpoint());
		operacionDTO.setIdentification(operacion.getIdentification());
		operacionDTO.setOperation(operacion.getOperation());
		operacionDTO.setPath(operacion.getPath());
		operacionDTO.setPostProcess(operacion.getPostProcess());

		// Se copian los queryparams
		operacionDTO.setQueryParams(QueryParameterFIQL.toQueryParamDTO(operacion.getApiqueryparameters()));
		
		return operacionDTO;
	}

	public static ApiOperation copyProperties(OperacionDTO operacionDTO) {
		ApiOperation operacion = new ApiOperation();

		if (!isValidOperacion(operacionDTO.getOperation().name())) {

			throw new IllegalArgumentException("com.indra.sofia2.web.api.services.HeaderWrongOperacion");
		}
		
		operacion.setDescription(operacionDTO.getDescription());
		operacion.setEndpoint(operacionDTO.getEndpoint());
		operacion.setIdentification(operacionDTO.getIdentification());
		operacion.setOperation(operacionDTO.getOperation());
		operacion.setPath(operacionDTO.getPath());
		operacion.setPostProcess(operacionDTO.getPostProcess());
		
		return operacion;
	}

	private static boolean isValidOperacion(String operacion) {
		return (operacion.equalsIgnoreCase(ApiOperation.Type.DELETE.name())
				|| operacion.equalsIgnoreCase(ApiOperation.Type.POST.name())
				|| operacion.equalsIgnoreCase(ApiOperation.Type.PUT.name())
				|| operacion.equalsIgnoreCase(ApiOperation.Type.GET.name()));
	}
}
