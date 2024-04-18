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
package com.minsait.onesait.platform.digitaltwin.broker.plugable.impl.gateway.reference.rest;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@ConditionalOnProperty(prefix = "onesaitplatform.digitaltwin.broker.rest", name = "enable", havingValue = "true")
@Configuration
public class SwaggerConfig {


	@Bean
	public GroupedOpenApi api() {
		return GroupedOpenApi.builder().group("Digital Twin Broker")
				.packagesToScan("com.minsait.onesait.platform.digitaltwin.broker.plugable.impl.gateway.reference.rest")
				.build();
	}

	@Bean
	public OpenAPI springShopOpenAPI() {
		return new OpenAPI()
				.info(new Info().contact(new Contact().email("support@onesaitplatform.com"))
						.title("onesait Platform Digital Twin Rest Gateway").description("onesait Platform Digital Twin Rest Gateway").version("v1.0.0")
						.license(new License().name("Apache License 2.0").url("https://github.com/onesaitplatform")));
	}
}
