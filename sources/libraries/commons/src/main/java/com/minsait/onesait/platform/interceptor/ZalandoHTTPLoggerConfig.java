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
package com.minsait.onesait.platform.interceptor;

import java.util.List;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.CorrelationId;

@Configuration
public class ZalandoHTTPLoggerConfig {

	public static final String CORRELATION_ID_HEADER_NAME = "X-Correlation-Id";
	public static final String CORRELATION_ID_LOG_VAR_NAME = "correlationId";

	@Bean
	public CorrelationId correlationId() {
		return request -> {
			final List<String> correlationHeader = request.getHeaders().get(CORRELATION_ID_HEADER_NAME);
			String id = UUID.randomUUID().toString();
			if(correlationHeader != null) {
				id = correlationHeader.iterator().next();
			}
			MDC.put(CORRELATION_ID_LOG_VAR_NAME, id);
			return id;
		};
	}

}

