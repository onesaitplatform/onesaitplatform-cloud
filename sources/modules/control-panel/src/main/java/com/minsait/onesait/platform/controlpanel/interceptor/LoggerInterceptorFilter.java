/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class LoggerInterceptorFilter implements Filter {

	public static final String CORRELATION_ID_HEADER_NAME = "X-Correlation-Id";
	public static final String CORRELATION_ID_LOG_VAR_NAME = "correlationId";

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// NO-OP

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		final String correlationId = getCorrelationIdFromHeader((HttpServletRequest) request);
		MDC.put(CORRELATION_ID_LOG_VAR_NAME, correlationId);
		log.info("New request with address: {} , URL:{} , User-Agent: {} , has id: {} ", request.getRemoteAddr(),
				((HttpServletRequest) request).getRequestURL(),
				((HttpServletRequest) request).getHeader(HttpHeaders.USER_AGENT), correlationId);
		chain.doFilter(request, response);
		MDC.remove(CORRELATION_ID_LOG_VAR_NAME);

	}

	@Override
	public void destroy() {
		// NO-OP

	}

	private String getCorrelationIdFromHeader(final HttpServletRequest request) {
		String correlationId = request.getHeader(CORRELATION_ID_HEADER_NAME);
		if (StringUtils.isEmpty(correlationId)) {
			correlationId = generateUniqueCorrelationId();
		}
		return correlationId;
	}

	public static String generateUniqueCorrelationId() {
		return UUID.randomUUID().toString();
	}

}
