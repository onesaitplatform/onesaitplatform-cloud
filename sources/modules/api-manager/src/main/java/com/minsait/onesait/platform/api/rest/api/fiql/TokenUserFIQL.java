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

import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.UserToken;
import com.minsait.onesait.platform.config.services.apimanager.dto.TokenUserDTO;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public final class TokenUserFIQL {

	public TokenUserDTO toTokenUsuarioDTO(UserToken token) throws GenericOPException {
		if (token != null) {
			TokenUserDTO tokenUsuarioDTO = new TokenUserDTO();
			tokenUsuarioDTO.setToken(token.getToken());
			tokenUsuarioDTO.setUserIdentification(token.getUser().getUserId());
			return tokenUsuarioDTO;
		} else {
			throw new GenericOPException("Token is null. Check this!!!");
		}

	}
}
