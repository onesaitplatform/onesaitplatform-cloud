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
package com.minsait.onesait.platform.serverless.security;

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
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import com.minsait.onesait.platform.serverless.security.oauth.OauthAuthenticator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BearerExtractorFilter implements Filter {

	private final OauthAuthenticator oauthAuthenticator;

	private final TokenExtractor tokenExtractor = new BearerTokenExtractor();

	public BearerExtractorFilter(OauthAuthenticator oauthAuthenticator) {

		this.oauthAuthenticator = oauthAuthenticator;

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		if (((HttpServletRequest) request).getHeader(HttpHeaders.AUTHORIZATION) != null) {
			final Authentication auth = tokenExtractor.extract((HttpServletRequest) request);
			if (auth instanceof PreAuthenticatedAuthenticationToken) {
				final Authentication oauth = loadAuthentication(auth);
				setContexts(oauth);
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
		return oauthAuthenticator.loadOauthAuthentication((String) auth.getPrincipal());
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
	}

	private void clearContexts() {
//		SecurityContextHolder.getContext().setAuthentication(null);
		SecurityContextHolder.clearContext();
	}
}
