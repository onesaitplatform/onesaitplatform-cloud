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

import java.io.IOException;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.AppRoleList;
import com.minsait.onesait.platform.config.model.AppUserList;
import com.minsait.onesait.platform.config.repository.AppRoleRepository;
import com.minsait.onesait.platform.config.repository.AppUserRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TokenUtil {

	@Autowired
	private AppUserRepository userRepo;
	@Autowired
	private AppRoleRepository roleRepository;
	@Autowired
	private UserRepository userRepository;

	private static final String PRINCIPAL = "principal";
	private static final String USER_NAME = "user_name";
	private static final String NAME = "name";

	private static final String TENANT = "tenant";
	private static final String VERICALS = "verticals";

	public String[] extractAndDecodeHeader(String header) throws IOException {

		final byte[] base64Token = header.substring(6).getBytes("UTF-8");
		byte[] decoded;
		try {
			decoded = Base64.getDecoder().decode(base64Token);
		} catch (final IllegalArgumentException e) {
			throw new BadCredentialsException("Failed to decode basic authentication token");
		}

		final String token = new String(decoded, "UTF-8");

		final int delim = token.indexOf(':');

		if (delim == -1) {
			throw new BadCredentialsException("Invalid basic authentication token");
		}
		return new String[] { token.substring(0, delim), token.substring(delim + 1) };
	}

	public Map<String, Object> convertAccessToken(OAuth2Authentication authentication, OAuth2AccessToken token,
			String appTokenId, String appId) {
		final Map<String, Object> response = new HashMap<>();

		response.putAll(genericConvertAccessToken(authentication, token, appId));
		response.putAll(realmsConvertAccessToken(authentication, appTokenId, appId));

		return response;
	}

	private Map<String, ?> genericConvertAccessToken(OAuth2Authentication authentication, OAuth2AccessToken token,
			String appId) {
		final Map<String, Object> response = new HashMap<>();
		final OAuth2Request clientToken = authentication.getOAuth2Request();

		final Set<String> scopes = new HashSet<>();
		scopes.add(appId);

		response.put(DefaultAccessTokenConverter.SCOPE, scopes);

		if (token.getAdditionalInformation().containsKey(NAME)) {
			response.put(NAME, token.getAdditionalInformation().get(NAME));
		}

		if (token.getAdditionalInformation().containsKey(NAME)) {
			response.put(USER_NAME, token.getAdditionalInformation().get(NAME));
		}

		if (token.getAdditionalInformation().containsKey(PRINCIPAL)) {
			response.put(PRINCIPAL, token.getAdditionalInformation().get(PRINCIPAL));
		}

		if (token.getAdditionalInformation().containsKey(DefaultAccessTokenConverter.JTI)) {
			response.put(DefaultAccessTokenConverter.JTI,
					token.getAdditionalInformation().get(DefaultAccessTokenConverter.JTI));
		}

		if (token.getExpiration() != null) {
			response.put(DefaultAccessTokenConverter.EXP, token.getExpiration().getTime() / 1000);
		}

		if (authentication.getOAuth2Request().getGrantType() != null) {
			response.put(DefaultAccessTokenConverter.GRANT_TYPE, authentication.getOAuth2Request().getGrantType());
		}

		response.put(DefaultAccessTokenConverter.CLIENT_ID, appId);
		if (clientToken.getResourceIds() != null && !clientToken.getResourceIds().isEmpty()) {
			response.put(DefaultAccessTokenConverter.AUD, clientToken.getResourceIds());
		}

		if (token.getAdditionalInformation().containsKey(TENANT)) {
			response.put(TENANT, token.getAdditionalInformation().get(TENANT));
		}

		if (token.getAdditionalInformation().containsKey(VERICALS)) {
			response.put(VERICALS, token.getAdditionalInformation().get(VERICALS));
		}
		return response;
	}

	private Map<String, ?> realmsConvertAccessToken(OAuth2Authentication authentication, String appTokenId,
			String appId) {
		final Map<String, Object> response = new HashMap<>();

		if (appTokenId.equals(appId)) {
			log.info("App auth IN token definition = {}, Returning all token info", appId);

			final String userId = authentication.getUserAuthentication().getName();

			final List<AppRoleList> roles = getAppRoles(userId, appTokenId);

			final Set<String> rolesList = new HashSet<>();

			for (final AppRoleList role : roles) {
				rolesList.add(role.getName());
			}
			if (rolesList.isEmpty()) {
				rolesList.add(userRepository.findByUserId(userId).getRole().getId());
			}

			response.put(DefaultAccessTokenConverter.AUTHORITIES, rolesList);
		} else {
			final String userId = authentication.getUserAuthentication().getName();

			// Assoc. Realm's Roles
			final List<AppRoleList> roles = getAppRoles(userId, appTokenId);

			final Map<String, Set<String>> appsRoles = getChildRoles(roles);

			Set<String> rolesList;
			rolesList = appsRoles.get(appId);

			if (rolesList == null) {
				rolesList = new HashSet<>();
			}

			// App Roles
			final List<AppRoleList> appRoles = getAppRoles(userId, appId);

			for (final AppRoleList role : appRoles) {
				rolesList.add(role.getName());
			}

			response.put(DefaultAccessTokenConverter.AUTHORITIES, rolesList);

			log.info("App auth IN token info = {}, Returning app credentials", appId);
		}
		return response;
	}

	public Map<String, Set<String>> getChildRoles(List<AppRoleList> roles) {

		final Map<String, Set<String>> retorno = new HashMap<>();
		roles.forEach((AppRoleList role) -> retorno.putAll(getChildRoles(role, retorno)));

		return retorno;
	}

	public Map<String, Set<String>> getChildRoles(AppRoleList role, Map<String, Set<String>> relationshipMap) {
		final Map<String, Set<String>> retorno = new HashMap<>();
		final List<AppRoleList> childs = roleRepository.findChildAppRoleListById(role.getId());
		childs.forEach(childRole -> {
			final String appId = childRole.getApp().getIdentification();
			if (!retorno.containsKey(appId) && relationshipMap.containsKey(appId)) {
				retorno.put(appId, relationshipMap.get(appId));
			} else if (retorno.containsKey(appId) && relationshipMap.containsKey(appId)) {
				retorno.put(appId, Stream.of(relationshipMap.get(appId), retorno.get(appId)).flatMap(Collection::stream)
						.collect(Collectors.toSet()));

			} else if (!retorno.containsKey(appId) && !relationshipMap.containsKey(appId)) {
				retorno.put(appId, new HashSet<>());
			}

			retorno.get(appId).add(childRole.getName());
		});

		return retorno;
	}

	public List<AppRoleList> getAppRoles(String userId, String clientId) {
		final List<AppUserList> roles = userRepo.findAppUserListByUserAndIdentification(userId, clientId);
		return roles.stream().map(AppUserList::getRole).collect(Collectors.toList());
	}

}
