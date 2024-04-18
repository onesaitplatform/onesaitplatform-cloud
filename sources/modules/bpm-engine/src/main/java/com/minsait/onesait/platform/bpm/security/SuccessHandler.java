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
package com.minsait.onesait.platform.bpm.security;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.bpm.services.BPMUserManagementService;
import com.minsait.onesait.platform.config.model.security.UserPrincipal;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.security.PlugableOauthAuthenticator;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SuccessHandler implements AuthenticationSuccessHandler {

	@Value("${server.servlet.contextPath:/}")
	private String contextPath;
	@Autowired
	private BPMUserManagementService userService;
	@Autowired(required = false)
	private PlugableOauthAuthenticator plugableOauthAuthenticator;

	@Autowired
	@Lazy
	private TokenStore tokenstore;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		final String loggedUserId = authentication.getName();
		// Set vertical + tenant as it comes from Oauth processing filter

		if (plugableOauthAuthenticator != null) {
			plugableOauthAuthenticator.postProcessAuthentication(authentication);
			final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			MultitenancyContextHolder.setVerticalSchema(((UserPrincipal) auth.getPrincipal()).getVerticalSchema());
			MultitenancyContextHolder.setTenantName(((UserPrincipal) auth.getPrincipal()).getTenant());

		} else if (SecurityContextHolder.getContext().getAuthentication() instanceof OAuth2Authentication) {
			OAuth2Authentication oauth = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
			oauth = tokenstore.readAuthentication(((OAuth2AuthenticationDetails) oauth.getDetails()).getTokenValue());
			MultitenancyContextHolder.setVerticalSchema(((UserPrincipal) oauth.getPrincipal()).getVerticalSchema());
			MultitenancyContextHolder.setTenantName(((UserPrincipal) oauth.getPrincipal()).getTenant());

		}

		if (!userService.userExistsInDB(loggedUserId)) {
			userService.createUser(authentication);
		} else {
			final List<String> auths = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
					.map(g -> g.getAuthority()).toList();
			if (log.isDebugEnabled()) {
				log.debug("SuccessHandler -> auths are: {} for user {}", String.join(";", auths), loggedUserId);
			userService.updateGroups(loggedUserId, auths);
			}
		}
		userService.createTenants(authentication);
		MultitenancyContextHolder.clear();
		response.sendRedirect(contextPath);

	}

}
