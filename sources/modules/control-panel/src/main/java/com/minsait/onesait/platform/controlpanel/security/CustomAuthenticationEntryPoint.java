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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

	private static final String LOGIN_LOCALE = "login_locale";
	private static final String LANG = "lang";
	private static final String LOGINURL = "/login";

	public CustomAuthenticationEntryPoint(String loginFormUrl) {
		super(loginFormUrl);
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		if (request.getServletPath().equals(LOGINURL) && request.getParameterMap() != null
				&& request.getParameterMap().get(LANG) != null) {
			request.getSession().setAttribute(LOGIN_LOCALE, request.getParameterMap().get(LANG)[0]);
			if (log.isDebugEnabled()) {
				log.debug("Adding parameters from request to session {} only location", request.getParameterMap());
			}
		} else {
			if (request.getSession().getAttribute(BLOCK_PRIOR_LOGIN) == null)
				request.getSession().setAttribute(BLOCK_PRIOR_LOGIN, request.getServletPath());

			if (request.getParameterMap() != null && !request.getParameterMap().isEmpty()) {
				if (log.isDebugEnabled()) {
					log.debug("Request contains parameters, adding to redirect through session");
				}
				final HashMap<String, String[]> parameterMap = request.getParameterMap().entrySet().stream().collect(
						Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (prev, next) -> next, HashMap::new));
				request.getSession().setAttribute(BLOCK_PRIOR_LOGIN_PARAMS, parameterMap);

			}

		}
		super.commence(request, response, authException);
	}

}
