/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2022 SPAIN
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;

import com.minsait.onesait.platform.config.model.security.UserPrincipal;

public class EnhancedAuthenticationKeyGenerator extends DefaultAuthenticationKeyGenerator {

	private static final String VERTICAL = "vertical";

	private static final String CLIENT_ID = "client_id";

	private static final String SCOPE = "scope";

	private static final String USERNAME = "username";

	@Override
	public String extractKey(OAuth2Authentication authentication) {
		final Map<String, String> values = new LinkedHashMap<>();
		final OAuth2Request authorizationRequest = authentication.getOAuth2Request();

		values.put(USERNAME, authentication.getName());
		values.put(CLIENT_ID, authorizationRequest.getClientId());
		if (authorizationRequest.getScope() != null) {
			values.put(SCOPE, OAuth2Utils.formatParameterList(new TreeSet<>(authorizationRequest.getScope())));
		}
		if (authorizationRequest.getRequestParameters().get(VERTICAL) != null) {
			values.put(VERTICAL, authorizationRequest.getRequestParameters().get(VERTICAL));
		} else if (authentication.getUserAuthentication().getPrincipal() instanceof UserPrincipal && !StringUtils
				.isEmpty(((UserPrincipal) authentication.getUserAuthentication().getPrincipal()).getVertical())) {
			values.put(VERTICAL, ((UserPrincipal) authentication.getUserAuthentication().getPrincipal()).getVertical());
		}
		return generateKey(values);
	}

	@SuppressWarnings("unchecked")
	public String extractKeyFromMap(Map<String, Object> parameters) {
		final Map<String, String> values = new LinkedHashMap<>();
		values.put(USERNAME, (String)parameters.get("name"));
		values.put(CLIENT_ID, (String) parameters.get(CLIENT_ID));
		if(parameters.containsKey(VERTICAL)) {
			values.put(VERTICAL, (String) parameters.get(VERTICAL));
		}
		if(parameters.containsKey(SCOPE)) {
			values.put(SCOPE, OAuth2Utils.formatParameterList((ArrayList<String>)parameters.get(SCOPE)));
		}
		return generateKey(values);
	}


}
