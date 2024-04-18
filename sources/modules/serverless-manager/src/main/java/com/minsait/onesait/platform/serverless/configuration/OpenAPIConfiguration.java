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
package com.minsait.onesait.platform.serverless.configuration;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenAPIConfiguration {

	@Value("${SERVER_NAME:localhost}")
	private String serverName;

	@Value("${server.servlet.contextPath:/serverless-manager}")
	private String contextPath;

	@Bean
	@Profile("docker")
	public OpenAPI customOpenAPIDocker() {
		final List<Server> servers = new ArrayList<>();
		final Server server = new Server().description("Backend").url("https://" + serverName + contextPath);
		servers.add(server);
		return generateOpenAPI(servers);
	}
	@Bean
	@Profile("default")
	public OpenAPI customOpenAPI() {
		final List<Server> servers = new ArrayList<>();
		final Server server = new Server().description("Backend").url("http://localhost:8086"+ contextPath);
		servers.add(server);
		return generateOpenAPI(servers);

	}

	private OpenAPI generateOpenAPI(List<Server> servers) {
		return new OpenAPI().info(new Info().title("Serverless API").version("1")).components(new Components())
				.servers(servers);
	}



}
