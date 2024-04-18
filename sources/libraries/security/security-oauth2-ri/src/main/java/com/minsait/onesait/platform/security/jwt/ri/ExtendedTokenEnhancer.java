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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import com.minsait.onesait.platform.config.model.AppRoleList;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.config.model.Vertical;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;

public class ExtendedTokenEnhancer implements TokenEnhancer {

	@Value("${security.jwt.client-id}")
	private String defaultClientId;

	private static final String VERTICAL = "vertical";
	private static final String TENANT = "tenant";
	@Value("${onesaitplatform.multitenancy.enabled:false}")
	private boolean multitenancyEnabled;

	@Autowired
	private MultitenancyService multitenancyService;

	@Autowired
	private TokenUtil tokenUtil;

	@Override
	public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {

		final Map<String, Object> additionalInfo = new HashMap<>();

		additionalInfo.put("name", authentication.getName());
		additionalInfo.put("principal", authentication.getUserAuthentication().getName());
		additionalInfo.put("parameters", authentication.getOAuth2Request().getRequestParameters());
		additionalInfo.put("clientId", authentication.getOAuth2Request().getClientId());
		additionalInfo.put("grantType", authentication.getOAuth2Request().getGrantType());
		if (multitenancyEnabled) {
			multitenancyService.findUser(authentication.getName()).ifPresent(u -> {
				additionalInfo.put(VERTICAL,
						multitenancyService.getVertical(MultitenancyContextHolder.getVerticalSchema()).get().getName());
				additionalInfo.put(TENANT, u.getTenant().getName());
				additionalInfo.put("verticals",
						u.getTenant().getVerticals().stream().map(Vertical::getName).collect(Collectors.toList()));
			});
		}
		final String clientId = authentication.getOAuth2Request().getClientId();
		if (clientId.equals(defaultClientId)) {
			additionalInfo.put("authorities", getPlatformAuthorities(authentication));
		} else {
			final String userId = authentication.getUserAuthentication().getName();
			final List<AppRoleList> roles = tokenUtil.getAppRoles(userId, clientId);

			additionalInfo.put("apps", tokenUtil.getChildRoles(roles));
			additionalInfo.put("authorities", getAuthorities(roles));
		}
		((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);

		return accessToken;
	}

	private Set<String> getAuthorities(List<AppRoleList> roles) {
		return roles.stream().map(AppRoleList::getName).collect(Collectors.toSet());
	}

	private List<String> getPlatformAuthorities(OAuth2Authentication authentication) {
		final Collection<GrantedAuthority> authorities = authentication.getAuthorities();
		return authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
	}

}