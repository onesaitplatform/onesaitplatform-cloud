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
package com.minsait.onesait.platform.controlpanel.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CheckSecurityFilter extends HandlerInterceptorAdapter {

	private static final String PREFIX = "/api/";
	private static final String PREFIX_EXCEPTION = "/api/login";


	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if ((request.getServletPath().startsWith(PREFIX) && !request.getServletPath().startsWith(PREFIX_EXCEPTION))
				&& (SecurityContextHolder.getContext().getAuthentication() == null || SecurityContextHolder.getContext()
						.getAuthentication() instanceof AnonymousAuthenticationToken)) {
			log.warn("Trying to access REST API path without authentication");

			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType("application/json;charset=UTF-8");
			response.getWriter().write("{\"error\": \"Incorrect or Expired Authorization Header\"}");
			response.getWriter().flush();
			response.getWriter().close();
			return false;

		}
		return super.preHandle(request, response, handler);
	}

}