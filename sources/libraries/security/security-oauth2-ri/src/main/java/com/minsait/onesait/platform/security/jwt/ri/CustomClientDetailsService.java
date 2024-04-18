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
package com.minsait.onesait.platform.security.jwt.ri;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;

import com.minsait.onesait.platform.config.model.AppList;
import com.minsait.onesait.platform.config.repository.AppRepository;
import com.minsait.onesait.platform.security.dto.ClientDetailsServiceDTO;

public class CustomClientDetailsService implements ClientDetailsService {

	@Autowired
	private AppRepository appRepository;

	@Value("${security.jwt.client-id}")
	private String defaultClientId;

	@Value("${security.jwt.client-secret}")
	private String clientSecret;

	@Value("${security.jwt.grant-type}")
	private String grantType;

	@Value("${security.jwt.expiration-time:44000}")
	private Integer tokenExpiration;

	@Value("${security.jwt.scopes}")
	private String scopes;

	private final Map<String, ClientDetailsServiceDTO> localCache = new HashMap<>();
	private static final Long OBSOLESCENCE_TIME = 60000L;

	@Override
	@Transactional
	//TO-DO this is being called too many times per request -> LOCAL CACHE, non-global bcause of changes in related entities App,AppRole,AppUser..
	public synchronized ClientDetails loadClientByClientId(String clientId) {
		final long now = System.currentTimeMillis();
		final ClientDetailsServiceDTO details = localCache.get(clientId);
		if(details == null || details.getCreatedTime() < now - OBSOLESCENCE_TIME) {
			final ClientDetails realDetails = getRealClientDetails(clientId);
			localCache.put(clientId, ClientDetailsServiceDTO.builder().createdTime(now).clientDetails(realDetails).build());
			return realDetails;
		}else {
			return details.getClientDetails();
		}

	}

	private ClientDetails getRealClientDetails(String clientId) {
		final Collection<String> types = Arrays.asList(grantType.split("\\s*,\\s*"));
		final Collection<String> scopeList = Arrays.asList(scopes.split("\\s*,\\s*"));

		final AppList app = appRepository.findAppListByIdentification(clientId);
		final BaseClientDetails details = new BaseClientDetails();
		details.setClientId(clientId);
		details.setClientSecret(clientSecret);
		details.setAutoApproveScopes(scopeList);
		details.setAuthorizedGrantTypes(types);
		details.setScope(scopeList);
		if (!clientId.equals(defaultClientId)) {
			final Set<GrantedAuthority> authorities = appRepository.findRolesListByAppId(app.getId()).stream()
					.map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
			details.setAuthorities(authorities);

			if (null != app.getSecret() && !"".equals(app.getSecret())) {
				details.setClientSecret(app.getSecret());
			} else {
				details.setClientSecret(clientSecret);
			}
			if (null != app.getTokenValiditySeconds()) {
				details.setAccessTokenValiditySeconds(app.getTokenValiditySeconds());
			}else {
				details.setAccessTokenValiditySeconds(tokenExpiration);
			}

		}else {
			details.setAccessTokenValiditySeconds(tokenExpiration);
		}
		return details;
	}

}
