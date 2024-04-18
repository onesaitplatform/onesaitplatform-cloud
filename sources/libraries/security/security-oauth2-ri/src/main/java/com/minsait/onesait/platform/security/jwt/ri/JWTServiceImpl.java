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
package com.minsait.onesait.platform.security.jwt.ri;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.services.oauth.JWTService;

@Service
public class JWTServiceImpl implements JWTService {

	@Resource(name = "tokenServices")
	CustomTokenService tokenServices;

	public String extractToken(HttpServletRequest request) {
		String authorization = request.getHeader("Authorization");
		if (authorization != null && authorization.contains("Bearer")) {
			String tokenId = authorization.substring("Bearer".length() + 1);

			OAuth2Authentication authentication = tokenServices.loadAuthentication(tokenId);

			return authentication.getUserAuthentication().getName();
		} else {
			return null;
		}
	}

	public String extractToken(String tokenId) {
		OAuth2Authentication authentication = tokenServices.loadAuthentication(tokenId);

		if (null != authentication && null != authentication.getUserAuthentication()
				&& null != authentication.getPrincipal()) {
			Authentication au = authentication.getUserAuthentication();

			return au.getName();
		} else {
			return null;
		}

	}

	public Object extractTokenPrincipal(String tokenId) {
		OAuth2Authentication authentication = tokenServices.loadAuthentication(tokenId);
		return authentication.getPrincipal();
	}

	public Authentication getAuthentication(String tokenId) {
		return tokenServices.loadAuthentication(tokenId);
	}

}
