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
package com.minsait.onesait.platform.interceptor;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import lombok.extern.slf4j.Slf4j;

@Deprecated
@Slf4j
public class CorrelationInterceptor extends HandlerInterceptorAdapter {
	public static final String CORRELATION_ID_HEADER_NAME = "X-Correlation-Id";
	public static final String CORRELATION_ID_LOG_VAR_NAME = "correlationId";

	@Override
	public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler)
			throws Exception {
		final String correlationId = getCorrelationIdFromHeader(request);
		MDC.put(CORRELATION_ID_LOG_VAR_NAME, correlationId);
		log.info("New request with address: {} , URL:{} , User-Agent: {} , has id: {} ", request.getRemoteAddr(),
				request.getRequestURL(), request.getHeader(HttpHeaders.USER_AGENT), correlationId);
		return true;
	}

	@Override
	public void afterCompletion(final HttpServletRequest request, final HttpServletResponse response,
			final Object handler, final Exception ex) throws Exception {
		MDC.remove(CORRELATION_ID_LOG_VAR_NAME);
	}

	private String getCorrelationIdFromHeader(final HttpServletRequest request) {
		String correlationId = request.getHeader(CORRELATION_ID_HEADER_NAME);
		if (!StringUtils.hasText(correlationId)) {
			correlationId = generateUniqueCorrelationId();
		}
		return correlationId;
	}

	public static String generateUniqueCorrelationId() {
		return UUID.randomUUID().toString();
	}
}
