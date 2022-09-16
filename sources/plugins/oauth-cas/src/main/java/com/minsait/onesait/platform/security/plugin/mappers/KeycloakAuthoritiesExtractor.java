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
package com.minsait.onesait.platform.security.plugin.mappers;

import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

public class KeycloakAuthoritiesExtractor implements AuthoritiesExtractor {

	private static final String DEFAULT_ROLE = "ROLE_USER";


	@Override
	public List<GrantedAuthority> extractAuthorities(Map<String, Object> map) {
		return AuthorityUtils.createAuthorityList(extractRole(map));
	}

	@SuppressWarnings("unchecked")
	public String extractRole(Map<String, Object> map) {
		try {
			return ((List<String>) map.get("authorities")).iterator().next();
		} catch (final Exception e) {
			return DEFAULT_ROLE;
		}
	}

}
