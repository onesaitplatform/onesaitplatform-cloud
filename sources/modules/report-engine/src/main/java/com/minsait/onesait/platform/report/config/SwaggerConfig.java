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
package com.minsait.onesait.platform.report.config;

import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {

	private static final String INFO_VERSION = "";
	private static final String INFO_TITLE = "onesait Platform";
	private static final String INFO_DESCRIPTION = "onesait Platform Control Panel Management";

	private static final String LICENSE_NAME = "Apache2 License";
	private static final String LICENSE_URL = "http://www.apache.org/licenses/LICENSE-2.0.html";

	private static final String CONTACT_NAME = "onesait Platform Team";
	private static final String CONTACT_URL = "https://onesaitplatform.online";
	private static final String CONTACT_EMAIL = "support@onesaitplatform.com";

	static final String AUTH_STR = "Authorization";

	@Bean
	public OpenAPI springOpenAPI() {
		return new OpenAPI()
				.info(new Info().contact(new Contact().email(CONTACT_EMAIL).name(CONTACT_NAME).url(CONTACT_URL))
						.title(INFO_TITLE).description(INFO_DESCRIPTION).version(INFO_VERSION)
						.license(new License().name(LICENSE_NAME).url(LICENSE_URL)))
				.components(new Components()
						.addSecuritySchemes("X-OP-APIKey",
								new SecurityScheme().type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER)
								.name("X-OP-APIKey"))
						.addSecuritySchemes("JWT-Token", new SecurityScheme().type(SecurityScheme.Type.HTTP)
								.scheme("bearer").bearerFormat("JWT")));
	}

	public static class GlobalHeaderOperationCustomizer implements OperationCustomizer {
		@Override
		public Operation customize(Operation operation, HandlerMethod handlerMethod) {
			operation.addSecurityItem(new SecurityRequirement().addList("JWT-Token").addList("X-OP-APIKey"));
			return operation;
		}
	}

	@Bean
	public GroupedOpenApi apiOpsAPI() {
		return GroupedOpenApi.builder().group("Report Engine").pathsToMatch("/api/reports.*")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();
	}

}
