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
package com.minsait.onesait.platform.security.plugin.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.security.PlugableOauthAuthenticator;
import com.minsait.onesait.platform.security.plugin.cache.PluginCacheManager;
import com.minsait.onesait.platform.security.plugin.condition.PluginLoadCondition;
import com.minsait.onesait.platform.security.plugin.mappers.ClaimsExtractor;
import com.minsait.onesait.platform.security.plugin.model.User;
import com.minsait.onesait.platform.security.plugin.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Component
@EnableScheduling
@Conditional(PluginLoadCondition.class)
@Slf4j
public class KeycloakPlugableAuthenticator implements PlugableOauthAuthenticator {

	@Autowired
	private UserService userService;
	@Autowired
	@Qualifier("configDBDetailsService")
	private UserDetailsService userDetailsService;
	@Autowired
	private UserInfoTokenServices userInfoTokenServices;
	@Autowired
	private ClaimsExtractor claimsExtractor;
	@Autowired
	private PluginCacheManager pluginCacheManager;

	private static final ObjectMapper mapper = new ObjectMapper();

	@Value("${oauth2.client.clientId}")
	private String clientId;

	@SuppressWarnings("unchecked")
	@Override
	public void postProcessAuthentication(Authentication authentication) {
		if (authentication instanceof OAuth2Authentication) {
			try {
				final User user = claimsExtractor
						.mapFromClaims((Map<String, Object>) ((OAuth2Authentication) authentication)
								.getUserAuthentication().getDetails());
				if (!userService.userExists(user.getUsername())) {
					userService.createUser(user);
				}
				MultitenancyContextHolder.setForced(true);
				MultitenancyContextHolder.setVerticalSchema(user.getVertical());
				final UserDetails details = userDetailsService.loadUserByUsername(authentication.getName());
				final OAuth2Request request = new OAuth2Request(null, clientId, null, true, null, null, null, null,
						null);
				SecurityContextHolder.getContext().setAuthentication(
						new OAuth2Authentication(request, new UsernamePasswordAuthenticationToken(details,
								details.getPassword(), details.getAuthorities())));
			} catch (final Exception e) {
				log.error("Error creating user", e);
				throw new RuntimeException("Error creating user within plugin");
			}
		}

	}

	@Override
	public String generateTokenForControlpanel(Authentication authentication) {
		if (authentication instanceof OAuth2Authentication) {
			return (String) mapper.convertValue(((OAuth2Authentication) authentication).getDetails(), Map.class)
					.get("tokenValue");
		}
		return null;

	}

	@Override
	public Authentication loadOauthAuthentication(String token) {
		final OAuth2Authentication auth = userInfoTokenServices.loadAuthentication(token);
		if (auth != null) {
			try {
				@SuppressWarnings("unchecked")
				final Map<String, Object> claims = (Map<String, Object>) auth.getUserAuthentication().getDetails();
				if (claims.get("vertical") != null) {
					final String value = (String) claims.get("vertical");
					MultitenancyContextHolder.setVerticalSchema(value);
					MultitenancyContextHolder.setForced(true);
				}
			} catch (final Exception e) {
				log.error("Could not read vertical from authentication");
			}
		}
		return auth;
	}

	@Override
	@Cacheable(cacheNames = "loadFullAuthentication", unless = "#result==null")
	public Authentication loadFullAuthentication(String token) {
		final Authentication authentication = loadOauthAuthentication(token);
		if (authentication != null) {
			final UserDetails details = userDetailsService.loadUserByUsername(authentication.getName());
			return new UsernamePasswordAuthenticationToken(details, details.getPassword(), details.getAuthorities());
		}
		return null;
	}

	@Scheduled(fixedDelayString = "${cache.eviction.time}")
	public void clearCaches() {
		pluginCacheManager.evictCache();
	}

	@Value("${oauth2.client.logoutUrl:/}")
	private String logoutUrl;

	@Override
	public String getLogoutUrl() {
		return logoutUrl;
	}
}
