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
package com.minsait.onesait.platform.api.rest.api.fiql;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.context.i18n.LocaleContextHolder;

import com.minsait.onesait.platform.api.rest.api.dto.ApiHeaderDTO;
import com.minsait.onesait.platform.config.model.ApiHeader;

public final class HeaderFIQL {

	private static final String API_REQUERIDO = "REQUIRED";
	private static final String API_OPCIONAL = "OPTIONAL";
	private static final String API_CONSTANTE = "CONSTANT";
	private static final String API_NUMBER = "NUMBER";
	private static final String API_BOOLEAN = "BOOLEAN";
	private static final String API_STRING = "STRING";

	static Locale locale = LocaleContextHolder.getLocale();

	private HeaderFIQL() {
		throw new AssertionError("Instantiating utility class...");
	}

	public static List<ApiHeaderDTO> toHeaderDTO(Set<ApiHeader> apiheaders) {
		ArrayList<ApiHeaderDTO> headersDTO = new ArrayList<>();
		for (ApiHeader apiheader : apiheaders) {
			ApiHeaderDTO apiheaderDTO = toHeaderDTO(apiheader);
			headersDTO.add(apiheaderDTO);
		}
		return headersDTO;
	}

	public static ApiHeaderDTO toHeaderDTO(ApiHeader apiheader) {
		ApiHeaderDTO apiheaderDTO = new ApiHeaderDTO();
		apiheaderDTO.setName(apiheader.getName());
		apiheaderDTO.setType(apiheader.getHeader_type());
		apiheaderDTO.setCondition(apiheader.getHeader_condition());
		apiheaderDTO.setDescription(apiheader.getHeader_description());
		apiheaderDTO.setValue(apiheader.getHeader_value());
		return apiheaderDTO;
	}

	public static ApiHeader copyProperties(ApiHeaderDTO apiheaderDTO) {
		ApiHeader apiheader = new ApiHeader();

		if (apiheaderDTO.getName() == null || apiheaderDTO.getName().equals("")) {
			throw new IllegalArgumentException("com.indra.sofia2.web.api.services.HeaderNombreRequired");
		}
		if (apiheaderDTO.getType() == null || apiheaderDTO.getType().equals("")) {

			throw new IllegalArgumentException("com.indra.sofia2.web.api.services.HeaderTipoRequired");
		}
		if (!isValidType(apiheaderDTO.getType())) {

			throw new IllegalArgumentException("com.indra.sofia2.web.api.services.HeaderWrongTipo");
		}
		if (apiheaderDTO.getCondition() == null || apiheaderDTO.getCondition().equals("")) {
			throw new IllegalArgumentException("com.indra.sofia2.web.api.services.HeaderCondicionRequired");
		}
		if (!isValidCondition(apiheaderDTO.getCondition())) {

			throw new IllegalArgumentException("com.indra.sofia2.web.api.services.HeaderWrongCondicion");
		}
		if (!isValidTypeValue(apiheaderDTO.getType(), apiheaderDTO.getValue())) {

			throw new IllegalArgumentException("com.indra.sofia2.web.api.services.HeaderWrongTipoValue");
		}
		if (!isValidCondicionValue(apiheaderDTO.getCondition(), apiheaderDTO.getValue())) {

			throw new IllegalArgumentException("com.indra.sofia2.web.api.services.HeaderWrongCondicionValue");
		}
		apiheader.setName(apiheaderDTO.getName());
		apiheader.setHeader_type(apiheaderDTO.getType());
		apiheader.setHeader_condition(apiheaderDTO.getCondition());
		apiheader.setHeader_description(apiheaderDTO.getDescription());
		apiheader.setHeader_value(apiheaderDTO.getValue());

		return apiheader;
	}

	private static boolean isValidCondicionValue(String condicion, String valor) {
		if (condicion.equals(API_CONSTANTE)) {
			return (valor != null && !valor.equals(""));
		}
		return true;
	}

	private static boolean isValidTypeValue(String tipo, String valor) {
		if (tipo.equals(API_NUMBER)) {
			try {
				Integer.parseInt(valor);
			} catch (Exception e) {
				return false;
			}
		} else if (tipo.equals(API_BOOLEAN)) {
			try {
				Boolean.parseBoolean(valor);
			} catch (Exception e) {
				return false;
			}
		}
		return true;
	}

	private static boolean isValidCondition(String tipo) {
		return (tipo.equalsIgnoreCase(API_CONSTANTE) || tipo.equalsIgnoreCase(API_OPCIONAL)
				|| tipo.equalsIgnoreCase(API_REQUERIDO));
	}

	private static boolean isValidType(String tipo) {
		return (tipo.equalsIgnoreCase(API_STRING) || tipo.equalsIgnoreCase(API_NUMBER)
				|| tipo.equalsIgnoreCase(API_BOOLEAN));
	}

}
