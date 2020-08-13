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
package com.minsait.onesait.platform.controlpanel.interceptor;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.filter.OAuth2AuthenticationFailureEvent;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.TokenExtractor;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import com.minsait.onesait.platform.business.services.interceptor.InterceptorCommon;
import com.minsait.onesait.platform.multitenant.util.BeanUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BearerTokenFilter implements Filter {

	private final TokenExtractor tokenExtractor = new BearerTokenExtractorPlatform();
	private final TokenStore tokenStore;

	public BearerTokenFilter() {
		tokenStore = BeanUtil.getBean(TokenStore.class);

	}

	private void publish(ApplicationEvent event) {
		try {
			final ApplicationContext eventPublisher = BeanUtil.getContext();
			eventPublisher.publishEvent(event);
		} catch (final Exception e) {
			log.debug("No application event publisher found on Spring Context");
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		log.debug("Init BearerTokenFilter for REST authentication");

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		final HttpServletRequest req = (HttpServletRequest) request;
		final HttpServletResponse resp = (HttpServletResponse) response;
		final Authentication auth = tokenExtractor.extract(req);
		if (auth instanceof PreAuthenticatedAuthenticationToken) {
			try {
				// save previous auth
				InterceptorCommon.setPreviousAuthenticationOnSession(req.getSession());
				log.debug("Detected Bearer token in request, loading autenthication");
				final OAuth2Authentication oauth = tokenStore.readAuthentication((String) auth.getPrincipal());
				if (oauth == null) {
					resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					resp.setContentType("application/json;charset=UTF-8");
					resp.getWriter().write("{\"error\": \"Incorrect or Expired Authorization Header\"}");
					resp.getWriter().flush();
					resp.getWriter().close();
				}
				InterceptorCommon.setContexts(oauth);
				log.debug("Loaded authentication for user {}", oauth.getName());
				publish(new AuthenticationSuccessEvent(auth));

				chain.doFilter(request, response);
			} catch (final Exception e) {
				log.error(e.getMessage());
				final BadCredentialsException bad = new BadCredentialsException("Could not obtain access token", e);
				publish(new OAuth2AuthenticationFailureEvent(bad));
			} finally {
				log.debug("Clearing authentication contexts");
				InterceptorCommon.clearContexts(
						(Authentication) req.getSession().getAttribute(InterceptorCommon.SESSION_ATTR_PREVIOUS_AUTH),
						req.getSession());
			}

		} else {
			chain.doFilter(request, response);
		}

	}

	@Override
	public void destroy() {
		log.debug("Destroying filter XOpAPIKeyFilter");

	}
}
