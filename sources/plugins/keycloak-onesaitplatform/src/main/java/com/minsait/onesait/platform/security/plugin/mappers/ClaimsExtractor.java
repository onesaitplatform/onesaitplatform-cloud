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
package com.minsait.onesait.platform.security.plugin.mappers;

import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.security.plugin.condition.PluginLoadCondition;
import com.minsait.onesait.platform.security.plugin.model.User;
import com.minsait.onesait.platform.security.plugin.model.UserClaims;

@Component
@Conditional(PluginLoadCondition.class)
public class ClaimsExtractor {

	private static final String MAIL_SUFFIX = "@keycloak.com";

	private final KeycloakAuthoritiesExtractor authoritiesExtractor = new KeycloakAuthoritiesExtractor();
	private final KeycloakPrincipalExtractor principalExtractor = new KeycloakPrincipalExtractor();

	private static final ObjectMapper mapper = new ObjectMapper();

	public User mapFromClaims(Map<String, Object> map) throws JsonProcessingException {
		final UserClaims claims = mapper.convertValue(map, UserClaims.class);

		final User user = new User();
		user.setFullName(claims.getName());
		user.setUsername((String) principalExtractor.extractPrincipal(map));
		user.setMail(StringUtils.hasText(claims.getEmail()) ? claims.getEmail()
				: claims.getPreferredUsername() + MAIL_SUFFIX);
		user.setExtraFields(mapper.writeValueAsString(claims));
		user.setPassword(randomPassword());
		user.setRole(authoritiesExtractor.extractRole(map));
		user.setVertical(map.get("vertical") != null ? (String) map.get("vertical") : null);
		return user;
	}

	private String randomPassword() {
		return RandomStringUtils.randomAlphabetic(1).toUpperCase() + UUID.randomUUID().toString().substring(0, 10)
				+ "$";
	}
}
