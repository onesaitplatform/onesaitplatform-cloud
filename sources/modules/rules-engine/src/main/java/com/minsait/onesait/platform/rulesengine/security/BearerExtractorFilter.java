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
package com.minsait.onesait.platform.rulesengine.security;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;
import org.springframework.security.oauth2.provider.authentication.TokenExtractor;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import com.minsait.onesait.platform.config.model.security.UserPrincipal;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class BearerExtractorFilter implements Filter {

	private static final String SPRING_SECURITY_CONTEXT = "SPRING_SECURITY_CONTEXT";
	@Autowired
	private TokenStore tokenstore;

	private final TokenExtractor tokenExtractor = new BearerTokenExtractor();

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		if (((HttpServletRequest) request).getHeader(HttpHeaders.AUTHORIZATION) != null) {
			final Authentication auth = tokenExtractor.extract((HttpServletRequest) request);
			if (auth instanceof PreAuthenticatedAuthenticationToken) {
				final OAuth2Authentication oauth = tokenstore.readAuthentication((String) auth.getPrincipal());

				final SecurityContext securityContext = SecurityContextHolder.getContext();
				securityContext.setAuthentication(oauth);
				if (oauth != null) {
					MultitenancyContextHolder
							.setVerticalSchema(((UserPrincipal) oauth.getPrincipal()).getVerticalSchema());
					MultitenancyContextHolder.setTenantName(((UserPrincipal) oauth.getPrincipal()).getTenant());
				}
				((HttpServletRequest) request).getSession(true).setAttribute(SPRING_SECURITY_CONTEXT, securityContext);
				chain.doFilter(request, response);
				MultitenancyContextHolder.clear();
				((HttpServletRequest) request).getSession().removeAttribute(SPRING_SECURITY_CONTEXT);
			}
		} else {
			chain.doFilter(request, response);
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

}
