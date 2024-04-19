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

import static com.minsait.onesait.platform.controlpanel.security.SpringSecurityConfig.BLOCK_PRIOR_LOGIN;
import static com.minsait.onesait.platform.controlpanel.security.SpringSecurityConfig.BLOCK_PRIOR_LOGIN_PARAMS;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.http.HttpHeaders;

import lombok.extern.slf4j.Slf4j;

//@Component
//@Order(Ordered.HIGHEST_PRECEDENCE)
@Deprecated
@Slf4j
public class HttpRequestLoggerFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// NO-OP
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		final HttpServletRequest httpRequest = (HttpServletRequest) request;
		try {
			log.debug("Request URL {}",
					httpRequest.getScheme() + "://" + httpRequest.getServerName() + ":" + httpRequest.getServerPort()
					+ httpRequest.getRequestURI() + "?"
					+ (httpRequest.getQueryString() == null ? "" : httpRequest.getQueryString()));
			if (httpRequest.getHeader(HttpHeaders.AUTHORIZATION) != null) {
				log.debug("Header authorization {}", httpRequest.getHeader(HttpHeaders.AUTHORIZATION));
			}

			if (httpRequest.getSession() != null && httpRequest.getSession().getAttribute(BLOCK_PRIOR_LOGIN) != null) {
				log.debug("Attribute block prior login {} ", httpRequest.getSession().getAttribute(BLOCK_PRIOR_LOGIN));
			}
			if (httpRequest.getSession() != null
					&& httpRequest.getSession().getAttribute(BLOCK_PRIOR_LOGIN_PARAMS) != null) {
				@SuppressWarnings("unchecked")
				final Map<String, String[]> params = (Map<String, String[]>) httpRequest.getSession()
				.getAttribute(BLOCK_PRIOR_LOGIN_PARAMS);
				if (!params.isEmpty()) {
					final String serializedParams = "?" + URLEncodedUtils.format(params.entrySet().stream()
							.map(e -> new BasicNameValuePair(e.getKey(), e.getValue()[0])).collect(Collectors.toList()),
							StandardCharsets.UTF_8);
					log.debug("Retrieved parameters from request to session: {}", serializedParams);
				}
			}
			chain.doFilter(request, response);
		} catch (final Exception e) {
			// rethrow
			log.debug("Error in execution", e);
			throw e;
		}

	}

	@Override
	public void destroy() {
		// NO-OP

	}

}
