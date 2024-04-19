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
package com.minsait.onesait.platform.api.config;

import java.util.Arrays;

import org.apache.cxf.jaxrs.openapi.OpenApiCustomizer;

import io.swagger.v3.oas.integration.api.OpenAPIConfiguration;
import io.swagger.v3.oas.models.servers.Server;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JaxRSOpenAPICustomizer extends OpenApiCustomizer {

	@Override
	public OpenAPIConfiguration customize(OpenAPIConfiguration configuration) {
		// add https server for deployed applications
		super.customize(configuration);
		try {
			final boolean hasHttps = configuration.getOpenAPI().getServers().stream()
					.anyMatch(s -> s.getUrl().startsWith("https"));
			if (!hasHttps) {
				final Server server = configuration.getOpenAPI().getServers().iterator().next();
				final Server httpsServer = new Server().url(server.getUrl().replace("http", "https"));
				configuration.getOpenAPI().setServers(Arrays.asList(server, httpsServer));
			}

		} catch (final Exception e) {
			log.debug("Could not add HTTPS server");
		}
		return configuration;
	}

}
