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
package com.minsait.onesait.platform.security.jwt.custom;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;
import org.springframework.security.oauth2.provider.authentication.TokenExtractor;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import com.minsait.onesait.platform.config.model.security.UserPrincipal;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.util.BeanUtil;
import com.minsait.onesait.platform.security.PlugableOauthAuthenticator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BearerExtractorFilter implements Filter {

	private PlugableOauthAuthenticator plugableOauthAuthenticator;
	private final TokenStore tokenStore;

	private final TokenExtractor tokenExtractor = new BearerTokenExtractor();

	public BearerExtractorFilter() {
		tokenStore = BeanUtil.getBean(TokenStore.class);
		try {
			plugableOauthAuthenticator = BeanUtil.getBean(PlugableOauthAuthenticator.class);
		} catch (final Exception e) {
			// NO-OP
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		if (((HttpServletRequest) request).getHeader(HttpHeaders.AUTHORIZATION) != null) {
			final Authentication auth = tokenExtractor.extract((HttpServletRequest) request);
			if (auth instanceof PreAuthenticatedAuthenticationToken) {
				final Authentication oauth = loadAuthentication(auth);
				setContexts(oauth);
				if (oauth != null) {
					MultitenancyContextHolder
							.setVerticalSchema(((UserPrincipal) oauth.getPrincipal()).getVerticalSchema());
					MultitenancyContextHolder.setTenantName(((UserPrincipal) oauth.getPrincipal()).getTenant());
				}

				chain.doFilter(request, response);
				clearContexts();

			} else {
				chain.doFilter(request, response);
			}
		} else {
			chain.doFilter(request, response);
		}
	}

	private Authentication loadAuthentication(Authentication auth) {
		if (plugableOauthAuthenticator != null) {
			return plugableOauthAuthenticator.loadFullAuthentication((String) auth.getPrincipal());
		} else {
			return tokenStore.readAuthentication((String) auth.getPrincipal());
		}

	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		log.debug("Initializing Bearer extractor filter");
	}

	@Override
	public void destroy() {
		log.debug("Destroying Bearer extractor filter");
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
