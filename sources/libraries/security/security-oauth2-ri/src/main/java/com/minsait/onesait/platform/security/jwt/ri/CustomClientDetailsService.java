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

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;

import com.minsait.onesait.platform.config.model.App;
import com.minsait.onesait.platform.config.model.AppRole;
import com.minsait.onesait.platform.config.repository.AppRepository;

public class CustomClientDetailsService implements ClientDetailsService {

	@Autowired
	private AppRepository appRepository;

	@Value("${security.jwt.client-id}")
	private String defaultClientId;

	@Value("${security.jwt.client-secret}")
	private String clientSecret;

	@Value("${security.jwt.grant-type}")
	private String grantType;

	@Value("${security.jwt.scopes}")
	private String scopes;

	@Override
	public ClientDetails loadClientByClientId(String clientId) {

		final Collection<String> types = Arrays.asList(grantType.split("\\s*,\\s*"));
		final Collection<String> scopeList = Arrays.asList(scopes.split("\\s*,\\s*"));

		final App app = appRepository.findByIdentification(clientId);
		final BaseClientDetails details = new BaseClientDetails();
		details.setClientId(clientId);
		details.setClientSecret(clientSecret);
		details.setAutoApproveScopes(scopeList);
		details.setAuthorizedGrantTypes(types);
		details.setScope(scopeList);
		if (!clientId.equals(defaultClientId)) {
			final Set<GrantedAuthority> authorities = app.getAppRoles().stream().map(AppRole::getName)
					.map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
			details.setAuthorities(authorities);

			if (null != app.getSecret() && !"".equals(app.getSecret())) {
				details.setClientSecret(app.getSecret());
			} else {
				details.setClientSecret(clientSecret);
			}
			if (null != app.getTokenValiditySeconds()) {
				details.setAccessTokenValiditySeconds(app.getTokenValiditySeconds());
			}
		}
		return details;
	}

}
