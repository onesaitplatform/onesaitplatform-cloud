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
package com.minsait.onesait.platform.monitoring.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;

import com.minsait.onesait.platform.security.PlugableOauthAuthenticator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OperationsLoginFilter implements Filter {

	private final RemoteTokenServices tokenServices;
	private final PlugableOauthAuthenticator plugableOauthAuthenticator;

	private static final String TOKEN_PARAM = "token";
	private static final String PATH_PREFIX = "/login/operations";

	public OperationsLoginFilter(RemoteTokenServices tokenServices,
			PlugableOauthAuthenticator plugableOauthAuthenticator) {
		this.tokenServices = tokenServices;
		this.plugableOauthAuthenticator = plugableOauthAuthenticator;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		log.debug("Init OperationsLoginFilter for REST authentication");

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		final HttpServletRequest req = (HttpServletRequest) request;
		if (req.getParameter(TOKEN_PARAM) != null && req.getServletPath().startsWith(PATH_PREFIX)) {
			log.debug("Detected token in request, loading autenthication");
			final String token = req.getParameter(TOKEN_PARAM);
			try {
				final Authentication auth = loadAuthentication(token);
				if (auth != null) {
					SecurityContextHolder.getContext().setAuthentication(auth);
				}
				log.info("Loaded authentication for user {}", auth.getName());
			} catch (final Exception e) {
				log.warn("Could not authenticate", e.getMessage());
			}
			((HttpServletResponse) response).sendRedirect(req.getContextPath());
		} else {
			chain.doFilter(request, response);
		}

	}

	private Authentication loadAuthentication(String token) {
		if (plugableOauthAuthenticator != null) {
			return plugableOauthAuthenticator.loadOauthAuthentication(token);
		} else {
			return tokenServices.loadAuthentication(token);
		}
	}

	@Override
	public void destroy() {
		log.debug("Destroying filter OperationsLoginFilter");

	}

}