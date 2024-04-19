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
package com.minsait.onesait.platform.controlpanel.config;

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

	private static final String AUTH_STR = "Authorization";

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
		return GroupedOpenApi.builder().group("All groups")
				.pathsToMatch("/api/**", "/binary-repository", "/binary-repository/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();
	}

	@Bean
	public GroupedOpenApi loginOpsAPI() {

		return GroupedOpenApi.builder().group("Authentication").pathsToMatch("/api/login", "/api/login/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();
	}

	@Bean
	public GroupedOpenApi apis() {
		return GroupedOpenApi.builder().group("APIs").pathsToMatch("/api/apis", "/api/apis/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();
	}

	@Bean
	public GroupedOpenApi clientplatform() {
		return GroupedOpenApi.builder().group("Clientplatform")
				.pathsToMatch("/api/clientplatform", "/api/clientplatform/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();
	}

	@Bean
	public GroupedOpenApi devices() {
		return GroupedOpenApi.builder().group("Devices").pathsToMatch("/api/devices", "/api/devices/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();
	}

	@Bean
	public GroupedOpenApi notebookOpsAPI() {
		return GroupedOpenApi.builder().group("Notebooks").pathsToMatch("/api/notebooks", "/api/notebooks/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();
	}

	@Bean
	public GroupedOpenApi userManagementAPI() {
		return GroupedOpenApi.builder().group("Users").pathsToMatch("/api/users", "/api/users/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();

	}

	@Bean
	public GroupedOpenApi realmManagementAPI() {
		return GroupedOpenApi.builder().group("Realms").pathsToMatch("/api/realms", "/api/realms/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();
	}

	@Bean
	public GroupedOpenApi deploymentAPI() {
		return GroupedOpenApi.builder().group("Deployment").pathsToMatch("/api/deployment", "/api/deployment/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();
	}

	@Bean
	public GroupedOpenApi dashboardsAPI() {
		return GroupedOpenApi.builder().group("Dashboards").pathsToMatch("/api/dashboards", "/api/dashboards/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();
	}

	@Bean
	public GroupedOpenApi dataflowAPI() {
		return GroupedOpenApi.builder().group("Dataflows").pathsToMatch("/api/dataflows", "/api/dataflows/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();
	}

	@Bean
	public GroupedOpenApi cacheAPI() {
		return GroupedOpenApi.builder().group("Cache").pathsToMatch("/api/caches", "/api/caches/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();
	}

	@Bean
	public GroupedOpenApi layerssAPI() {
		return GroupedOpenApi.builder().group("Layers").pathsToMatch("/api/layers", "/api/layers/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();
	}

	@Bean
	public GroupedOpenApi modelssAPI() {
		return GroupedOpenApi.builder().group("Models").pathsToMatch("/api/models", "/api/models/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();
	}

	@Bean
	public GroupedOpenApi configurationAPI() {
		return GroupedOpenApi.builder().group("Configurations")
				.pathsToMatch("/api/configurations", "/api/configurations/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();
	}

	@Bean
	public GroupedOpenApi videobrokerAPI() {
		return GroupedOpenApi.builder().group("Videobroker").pathsToMatch("/api/videobroker", "/api/videobroker/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();
	}

	@Bean
	public GroupedOpenApi ontologyManagementAPI() {
		return GroupedOpenApi.builder().group("Ontologies").pathsToMatch("/api/ontologies", "/api/ontologies/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();
	}

	@Bean
	public GroupedOpenApi reportsApi() {
		return GroupedOpenApi.builder().group("Reports").pathsToMatch("/api/reports", "/api/reports/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();
	}

	@Bean
	public GroupedOpenApi microservicesApi() {
		return GroupedOpenApi.builder().group("Microservices")
				.pathsToMatch("/api/microservices", "/api/microservices/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();
	}

	@Bean
	public GroupedOpenApi mailManagementAPI() {
		return GroupedOpenApi.builder().group("Mail").pathsToMatch("/api/mail", "/api/mail/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();

	}

	@Bean
	public GroupedOpenApi binaryAPI() {
		return GroupedOpenApi.builder().group("Binary").pathsToMatch("/binary-repository", "/binary-repository/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();
	}

	@Bean
	public GroupedOpenApi projectAPI() {
		return GroupedOpenApi.builder().group("Projects").pathsToMatch("/api/projects", "/api/projects/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();
	}

	@Bean
	public GroupedOpenApi restPlannerAPI() {
		return GroupedOpenApi.builder().group("Rest Planner").pathsToMatch("/api/restplanner", "/api/restplanner/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();
	}

	@Bean
	public GroupedOpenApi categorizationManagementAPI() {
		return GroupedOpenApi.builder().group("Categorization")
				.pathsToMatch("/api/categorization", "/api/categorization/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();

	}

	@Bean
	public GroupedOpenApi migrationManagementAPI() {
		return GroupedOpenApi.builder().group("Migration").pathsToMatch("/api/migration", "/api/migration/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();

	}

	@Bean
	public GroupedOpenApi querytoolAPI() {
		return GroupedOpenApi.builder().group("Querytool").pathsToMatch("/api/querytool", "/api/querytool/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();
	}

	@Bean
	public GroupedOpenApi flowEngineManagementAPI() {
		return GroupedOpenApi.builder().group("FlowEngine").pathsToMatch("/api/flowengine", "/api/flowengine/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();

	}

	@Bean
	public GroupedOpenApi auditManagementAPI() {
		return GroupedOpenApi.builder().group("Audit").pathsToMatch("/api/audit", "/api/audit/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();

	}

	@Bean
	public GroupedOpenApi gadgetDatasourceManagementAPI() {
		return GroupedOpenApi.builder().group("Gadget Datasources")
				.pathsToMatch("/api/gadgetdatasources", "/api/gadgetdatasources/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();

	}

	@Bean
	public GroupedOpenApi webProjectsAPI() {
		return GroupedOpenApi.builder().group("Web projects").pathsToMatch("/api/webprojects", "/api/webprojects/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();

	}

	@Bean
	public GroupedOpenApi rulesAPI() {
		return GroupedOpenApi.builder().group("Rules").pathsToMatch("/api/rules", "/api/rules/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();
	}

	@Bean
	public GroupedOpenApi moduleNotifications() {
		return GroupedOpenApi.builder().group("Module Notifications").pathsToMatch("/api/notifier", "/api/notifier/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();
	}

	@Bean
	public GroupedOpenApi gadgetTemplateManagementAPI() {
		return GroupedOpenApi.builder().group("Gadget Templates")
				.pathsToMatch("/api/gadgettemplates", "/api/gadgettemplates/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();

	}

	@Bean
	public GroupedOpenApi gadgetManagementAPI() {
		return GroupedOpenApi.builder().group("Gadgets").pathsToMatch("/api/gadgets", "/api/gadgets/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();

	}

	@Bean
	public GroupedOpenApi importToolAPI() {
		return GroupedOpenApi.builder().group("Import tool").pathsToMatch("/api/importtool", "/api/importtool/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();
	}

	@Bean
	public GroupedOpenApi openDataPortalAPI() {
		return GroupedOpenApi.builder().group("Open Data Portal").pathsToMatch("/api/opendata", "/api/opendata/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();
	}

	@Bean
	public GroupedOpenApi internationalizationAPI() {
		return GroupedOpenApi.builder().group("Internationalization")
				.pathsToMatch("/api/internationalizations", "/api/internationalizations/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();

	}

	@Bean
	public GroupedOpenApi dataRefinerAPI() {
		return GroupedOpenApi.builder().group("Data Refiner").pathsToMatch("/api/datarefiner", "/api/datarefiner/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();
	}

	@Bean
	public GroupedOpenApi lowCodeAPI() {
		return GroupedOpenApi.builder().group("Low Code Generation").pathsToMatch("/api/low-code", "/api/low-code/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();

	}

	@Bean
	public GroupedOpenApi favoriteAPI() {
		return GroupedOpenApi.builder().group("Favorites").pathsToMatch("/api/favorites", "/api/favorites/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();
	}

	@Bean
	public GroupedOpenApi favoriteGadgetAPI() {
		return GroupedOpenApi.builder().group("Favorite Gadgets")
				.pathsToMatch("/api/favoritegadget", "/api/favoritegadget/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();
	}

	@Bean
	public GroupedOpenApi multitenantAPI() {
		return GroupedOpenApi.builder().group("Multitenant Management")
				.pathsToMatch("/api/multitenant", "/api/multitenant/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();
	}

	@Bean
	public GroupedOpenApi objectStorageAPI() {
		return GroupedOpenApi.builder().group("Object Storage Management")
				.pathsToMatch("/api/objectstorage", "/api/objectstorage/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();

	}

	@Bean
	public GroupedOpenApi categoriesAPI() {
		return GroupedOpenApi.builder().group("Categories").pathsToMatch("/api/categories", "/api/categories/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();

	}

	@Bean
	public GroupedOpenApi versioningAPI() {
		return GroupedOpenApi.builder().group("Versioning").pathsToMatch("/api/versioning", "/api/versioning/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();

	}
	
	@Bean
	public GroupedOpenApi sparkLauncherAPI() {
		return GroupedOpenApi.builder().group("Spark Launcher Management")
				.pathsToMatch("/api/sparklauncher", "/api/sparklauncher/**")
				.addOperationCustomizer(new GlobalHeaderOperationCustomizer()).build();

	}

}
