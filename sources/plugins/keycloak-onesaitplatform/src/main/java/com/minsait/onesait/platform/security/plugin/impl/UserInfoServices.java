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
package com.minsait.onesait.platform.security.plugin.impl;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.FixedAuthoritiesExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.FixedPrincipalExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.BaseOAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserInfoServices extends UserInfoTokenServices {

	private final String userInfoEndpointUrl;

	private final String clientId;

	@Setter
	private OAuth2RestOperations restTemplate;

	@Setter
	private String tokenType = DefaultOAuth2AccessToken.BEARER_TYPE;

	@Setter
	private AuthoritiesExtractor authoritiesExtractor = new FixedAuthoritiesExtractor();

	@Setter
	private PrincipalExtractor principalExtractor = new FixedPrincipalExtractor();

	private static final String REGEX_REALM = ".*\\/realms\\/([a-z-_]+).*";
	private static final Pattern PATTERN_REALM = Pattern.compile(REGEX_REALM);

	public UserInfoServices(String userInfoEndpointUrl, String clientId) {
		super(userInfoEndpointUrl,clientId);
		this.userInfoEndpointUrl = userInfoEndpointUrl;
		this.clientId = clientId;
	}

	@Override
	public OAuth2Authentication loadAuthentication(String accessToken)
			throws AuthenticationException, InvalidTokenException {
		final Map<String, Object> map = getMap(computeUserInfoURL(accessToken), accessToken);
		if (map.containsKey("error")) {
			if (log.isDebugEnabled()) {
				log.debug("userinfo returned error: " + map.get("error"));
			}			
			throw new InvalidTokenException(accessToken);
		}
		return extractAuthentication(map);
	}

	private OAuth2Authentication extractAuthentication(Map<String, Object> map) {
		final Object principal = getPrincipal(map);
		final List<GrantedAuthority> authorities = authoritiesExtractor.extractAuthorities(map);
		final OAuth2Request request = new OAuth2Request(null, clientId, null, true, null, null, null, null, null);
		final UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(principal, "N/A",
				authorities);
		token.setDetails(map);
		return new OAuth2Authentication(request, token);
	}

	@Override
	protected Object getPrincipal(Map<String, Object> map) {
		final Object principal = principalExtractor.extractPrincipal(map);
		return principal == null ? "unknown" : principal;
	}

	@Override
	public OAuth2AccessToken readAccessToken(String accessToken) {
		throw new UnsupportedOperationException("Not supported: read access token");
	}

	@SuppressWarnings({ "unchecked" })
	private Map<String, Object> getMap(String path, String accessToken) {
		if (log.isDebugEnabled()) {
			log.debug("Getting user info from: " + path);
		}	
		try {
			OAuth2RestOperations restTemplate = this.restTemplate;
			if (restTemplate == null) {
				final BaseOAuth2ProtectedResourceDetails resource = new BaseOAuth2ProtectedResourceDetails();
				resource.setClientId(clientId);
				restTemplate = new OAuth2RestTemplate(resource);
			}
			final OAuth2AccessToken existingToken = restTemplate.getOAuth2ClientContext().getAccessToken();
			if (existingToken == null || !accessToken.equals(existingToken.getValue())) {
				final DefaultOAuth2AccessToken token = new DefaultOAuth2AccessToken(accessToken);
				token.setTokenType(tokenType);
				restTemplate.getOAuth2ClientContext().setAccessToken(token);
			}
			log.info("RestTemplate to keycloak");
			Map<String, Object> toReturn=restTemplate.getForEntity(path, Map.class).getBody();
			log.info("End RestTemplate to keycloak");
			return toReturn;
		} catch (final Exception ex) {
			log.warn("Could not fetch user details: " + ex.getClass() + ", " + ex.getMessage());
			return Collections.<String, Object>singletonMap("error", "Could not fetch user details");
		}
	}

	private String computeUserInfoURL(String token) {
		try {
			final String[] jwtSegments = token.split("\\.");
			final String jwtBody = jwtSegments[1];
			final String parsedBody = new String(Base64.getDecoder().decode(jwtBody));
			final ObjectMapper mapper = new ObjectMapper();
			JsonNode jsonBody = mapper.createObjectNode();
			jsonBody = mapper.readValue(parsedBody, JsonNode.class);
			final String issuer = jsonBody.get("iss").asText();
			Matcher matcher = PATTERN_REALM.matcher(issuer);
			String realm = null;
			if (matcher.find()) {
				realm = matcher.group(1);
			} else {
				log.info("No match found for keycloak realm");
			}
			if (realm != null) {
				matcher = PATTERN_REALM.matcher(userInfoEndpointUrl);
				if (matcher.find()) {
					return new StringBuilder(userInfoEndpointUrl).replace(matcher.start(1), matcher.end(1), realm)
							.toString();
				}
			}

		} catch (final Exception e) {
			log.error("Error while trying to compute Keycloak realm URL", e);
			return userInfoEndpointUrl;
		}
		return userInfoEndpointUrl;
	}

}
