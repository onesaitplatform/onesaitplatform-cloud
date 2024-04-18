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
package com.minsait.onesait.platform.controlpanel.interceptor;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import com.minsait.onesait.platform.business.services.interceptor.InterceptorCommon;
import com.minsait.onesait.platform.multitenant.util.BeanUtil;
import com.minsait.onesait.platform.security.ri.ConfigDBDetailsService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class XOpAPIKeyFilter implements Filter {

	private final ConfigDBDetailsService detailsService;

	private static final String X_OP_APIKEY = "X-OP-APIKey";

	public XOpAPIKeyFilter() {
		detailsService = BeanUtil.getBean(ConfigDBDetailsService.class);

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
		log.debug("Init XOpAPIKeyFilter for REST authentication");

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		final HttpServletRequest req = (HttpServletRequest) request;
		boolean hasSession = false;
		if (req.getHeader(X_OP_APIKEY) != null) {
			log.debug("Detected header {} in request, loading autenthication", X_OP_APIKEY);
			hasSession = req.getSession(false) != null;
			if (hasSession) {
				InterceptorCommon.setPreviousAuthenticationOnSession(req.getSession(false));
			}
			final String token = req.getHeader(X_OP_APIKEY);
			try {
				final UserDetails details = detailsService.loadUserByUserToken(token);
				if (details != null) {
					final Authentication auth = new UsernamePasswordAuthenticationToken(details, details.getPassword(),
							details.getAuthorities());
					InterceptorCommon.setContexts(auth);
					if (log.isDebugEnabled()) {
						log.debug("Loaded authentication for user {}", auth.getName());
					}					
					publish(new AuthenticationSuccessEvent(auth));
				}

				chain.doFilter(request, response);
			} catch (final Exception e) {
				log.error(e.getMessage());

			} finally {
				log.debug("Clearing authentication contexts");
				if (hasSession) {
					InterceptorCommon.clearContexts(req.getSession(false));
				} else {
					if (req.getSession(false) != null) {
						req.getSession(false).invalidate();
					}
				}
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
