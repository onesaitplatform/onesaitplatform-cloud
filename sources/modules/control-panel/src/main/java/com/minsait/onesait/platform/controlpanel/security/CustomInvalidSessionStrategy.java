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
package com.minsait.onesait.platform.controlpanel.security;

import static com.minsait.onesait.platform.controlpanel.security.SpringSecurityConfig.BLOCK_PRIOR_LOGIN;
import static com.minsait.onesait.platform.controlpanel.security.SpringSecurityConfig.BLOCK_PRIOR_LOGIN_PARAMS;
import static com.minsait.onesait.platform.controlpanel.security.SpringSecurityConfig.INVALIDATE_SESSION_FORCED;
import static com.minsait.onesait.platform.controlpanel.security.SpringSecurityConfig.LOGIN_STR;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.session.InvalidSessionStrategy;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CustomInvalidSessionStrategy implements InvalidSessionStrategy {

	@Value("${server.servlet.contextPath:/controlpanel}")
	private String contextPath;
	private String destination;
	private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

	@PostConstruct
	void setDestionation() {
		destination = contextPath + LOGIN_STR;
	}

	@Override
	public void onInvalidSessionDetected(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		if (log.isDebugEnabled()) {
			log.debug("Invalid session, creating new one and redirecting to {}", destination);
		}
		// always new session
		request.getSession();
		if (request.getAttribute(INVALIDATE_SESSION_FORCED) != null) {
			log.info("Session forced to invalidate, redirecting to: {}", request.getAttribute(BLOCK_PRIOR_LOGIN));
			redirectStrategy.sendRedirect(request, response, (String) request.getAttribute(BLOCK_PRIOR_LOGIN));
		} else {
			if (!LOGIN_STR.equals(request.getServletPath())) {
				request.getSession().setAttribute(BLOCK_PRIOR_LOGIN, request.getServletPath());
			}
			if (request.getParameterMap() != null && !request.getParameterMap().isEmpty()) {
				log.debug("Request contains parameters, adding to redirect through session");
				final HashMap<String, String[]> parameterMap = request.getParameterMap().entrySet().stream().collect(
						Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (prev, next) -> next, HashMap::new));
				request.getSession().setAttribute(BLOCK_PRIOR_LOGIN_PARAMS, parameterMap);

			}

			redirectStrategy.sendRedirect(request, response, destination);
		}

	}

}