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

import com.minsait.onesait.platform.api.rest.api.dto.AutenticacionDTO;
import com.minsait.onesait.platform.config.model.ApiAuthentication;

public final class AuthenticationFIQL {

	private static final String AUT_TYPE_BASIC = "BASIC";
	private static final String AUT_TYPE_HEADER = "HEADER";

	private AuthenticationFIQL() {
		throw new AssertionError("Instantiating utility class...");
	}

	public static AutenticacionDTO toAutenticacionDTO(ApiAuthentication autenticacion) {
		AutenticacionDTO autenticacionDTO = new AutenticacionDTO();

		autenticacionDTO.setType(autenticacion.getType());
		autenticacionDTO.setDescription(autenticacion.getDescription());

		return autenticacionDTO;
	}

	public static ApiAuthentication copyProperties(AutenticacionDTO autenticacionDTO) {
		ApiAuthentication autenticacion = new ApiAuthentication();

		if (isValidTipo(autenticacionDTO.getType())) {
			throw new IllegalArgumentException("com.indra.sofia2.web.api.services.AutWrongTipo");
		}

		autenticacion.setType(autenticacionDTO.getType());
		autenticacion.setDescription(autenticacionDTO.getDescription());

		return autenticacion;
	}

	private static boolean isValidTipo(String tipo) {
		return (tipo.equalsIgnoreCase(AUT_TYPE_BASIC) || tipo.equalsIgnoreCase(AUT_TYPE_HEADER));
	}
}
