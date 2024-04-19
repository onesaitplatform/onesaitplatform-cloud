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
package com.minsait.onesait.platform.bpm.security;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.authentication.TokenExtractor;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import com.minsait.onesait.platform.config.model.security.UserPrincipal;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.util.BeanUtil;
import com.minsait.onesait.platform.security.PlugableOauthAuthenticator;
import com.minsait.onesait.platform.security.ri.ConfigDBDetailsService;

public class BearerAuthenticationFilter implements Filter {

	private final TokenExtractor tokenExtractor = new BearerTokenExtractor();
	private static final String X_OP_APIKEY = "X-OP-APIKey";

	private final TokenStore tokenStore;
	private final ConfigDBDetailsService detailsService;
	private PlugableOauthAuthenticator plugableOauthAuthenticator;

	public BearerAuthenticationFilter() {
		tokenStore = BeanUtil.getBean(TokenStore.class);
		detailsService = BeanUtil.getBean(ConfigDBDetailsService.class);
		try {
			plugableOauthAuthenticator = BeanUtil.getBean(PlugableOauthAuthenticator.class);
		} catch (final Exception e) {
			// NO-OP
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// nothing

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (((HttpServletRequest) request).getHeader(HttpHeaders.AUTHORIZATION) != null) {
			final Authentication auth = tokenExtractor.extract((HttpServletRequest) request);
			if (auth instanceof PreAuthenticatedAuthenticationToken) {
				final Authentication oauth = this.loadAuthentication(auth);
				setContexts(oauth);
				chain.doFilter(request, response);
				clearContexts();

			} else {
				chain.doFilter(request, response);
			}
		} else if (((HttpServletRequest) request).getHeader(X_OP_APIKEY) != null) {
			final String token = ((HttpServletRequest) request).getHeader(X_OP_APIKEY);
			final UserDetails details = detailsService.loadUserByUserToken(token);
			final Authentication auth = new UsernamePasswordAuthenticationToken(details, details.getPassword(),
					details.getAuthorities());
			setContexts(auth);
			chain.doFilter(request, response);
			clearContexts();
		} else if (SecurityContextHolder.getContext().getAuthentication() instanceof OAuth2Authentication) {
			final OAuth2Authentication oauth = (OAuth2Authentication) SecurityContextHolder.getContext()
					.getAuthentication();
			if (oauth.getDetails() != null) {
				final Authentication authentication = loadAuthentication(
						((OAuth2AuthenticationDetails) oauth.getDetails()).getTokenValue());
				setMultitenantContext(authentication);
			} else {
				setMultitenantContext(oauth);
			}
			chain.doFilter(request, response);
			clearMultitenantContext();

		} else {
			chain.doFilter(request, response);
		}
	}

	@Override
	public void destroy() {
		// nothing

	}

	private Authentication loadAuthentication(Authentication auth) {
		if (plugableOauthAuthenticator != null) {
			return plugableOauthAuthenticator.loadFullAuthentication((String) auth.getPrincipal());
		} else {
			return tokenStore.readAuthentication((String) auth.getPrincipal());
		}

	}

	private Authentication loadAuthentication(String token) {
		if (plugableOauthAuthenticator != null) {
			return plugableOauthAuthenticator.loadFullAuthentication(token);
		} else {
			return tokenStore.readAuthentication(token);
		}

	}

	private void setContexts(Authentication auth) {
		SecurityContextHolder.getContext().setAuthentication(auth);
		setMultitenantContext(auth);
	}

	private void clearContexts() {
		clearMultitenantContext();
		SecurityContextHolder.getContext().setAuthentication(null);
	}

	private void setMultitenantContext(Authentication auth) {
		MultitenancyContextHolder.setVerticalSchema(((UserPrincipal) auth.getPrincipal()).getVerticalSchema());
		MultitenancyContextHolder.setTenantName(((UserPrincipal) auth.getPrincipal()).getTenant());
	}

	private void clearMultitenantContext() {
		MultitenancyContextHolder.clear();
	}
}
