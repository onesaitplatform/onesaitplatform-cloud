/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
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

import static com.google.common.base.Predicates.or;
import static springfox.documentation.builders.PathSelectors.regex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Predicate;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

	private static final String INFO_VERSION = "";
	private static final String INFO_TITLE = "onesait Platform";
	private static final String INFO_DESCRIPTION = "onesait Platform Control Panel Management";

	private static final String LICENSE_NAME = "Apache2 License";
	private static final String LICENSE_URL = "http://www.apache.org/licenses/LICENSE-2.0.html";

	private static final String CONTACT_NAME = "onesait Platform Team";
	private static final String CONTACT_URL = "https://onesaitplatform.online";
	private static final String CONTACT_EMAIL = "support@onesaitplatform.com";

	private static final String HEADER_STR = "header";
	private static final String STRING_STR = "string";
	private static final String AUTH_STR = "Authorization";
	private static final String APP_JSON = "application/json";
	private static final String TEXT_PL = "text/plain";
	private static final String APP_YAML = "application/yaml";

	@Bean
	public ApiInfo apiInfo() {
		return new ApiInfoBuilder().title(INFO_TITLE).description(INFO_DESCRIPTION).termsOfServiceUrl(CONTACT_URL)
				.contact(new Contact(CONTACT_NAME, CONTACT_URL, CONTACT_EMAIL)).license(INFO_VERSION)
				.licenseUrl(LICENSE_URL).version(LICENSE_NAME).build();
	}

	List<Parameter> addRestParameters(ParameterBuilder aParameterBuilder, List<Parameter> aParameters) {
		return aParameters;
	}

	@Bean
	public Docket apiOpsAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());

		return new Docket(DocumentationType.SWAGGER_2).groupName("All groups").select()
				.apis(RequestHandlerSelectors.any()).paths(buildPathSelectorApiOps()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters));
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorApiOps() {
		return or(regex("/api/.*"), regex("/binary-repository.*"));
	}

	@Bean
	public Docket loginOpsAPI() {

		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();

		return new Docket(DocumentationType.SWAGGER_2).groupName("Authentication").select()
				.apis(RequestHandlerSelectors.any()).paths(buildPathSelectorApiOpsLogin()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters));
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorApiOpsLogin() {
		return or(regex("/api/login.*"));
	}

	@Bean
	public Docket apis() {

		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());

		return new Docket(DocumentationType.SWAGGER_2).groupName("APIs").select().apis(RequestHandlerSelectors.any())
				.paths(buildPathSelectorApis()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters));
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorApis() {
		return or(regex("/api/apis.*"));
	}

	@Bean
	public Docket clientplatform() {

		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();
		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());
		return new Docket(DocumentationType.SWAGGER_2).groupName("Clientplatform").select()
				.apis(RequestHandlerSelectors.any()).paths(buildPathSelectorClientplatform()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters));
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorClientplatform() {
		return or(regex("/api/clientplatform.*"));
	}

	@Bean
	public Docket devices() {

		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();
		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());
		return new Docket(DocumentationType.SWAGGER_2).groupName("Devices").select().apis(RequestHandlerSelectors.any())
				.paths(buildPathSelectorDevices()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters));
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorDevices() {
		return or(regex("/api/devices.*"));
	}

	@Bean
	public Docket notebookOpsAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());

		return new Docket(DocumentationType.SWAGGER_2).groupName("Notebooks").select()
				.apis(RequestHandlerSelectors.any()).paths(buildPathSelectorNotebookOps()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters));
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorNotebookOps() {
		return or(regex("/api/notebooks.*"));
	}

	@Bean
	public Docket userManagementAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());

		return new Docket(DocumentationType.SWAGGER_2).groupName("Users").select().apis(RequestHandlerSelectors.any())
				.paths(buildPathSelectorUserManagement()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters));
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorUserManagement() {
		return or(regex("/api/users.*"));
	}

	@Bean
	public Docket realmManagementAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());

		return new Docket(DocumentationType.SWAGGER_2).groupName("Realms").select().apis(RequestHandlerSelectors.any())
				.paths(buildPathSelectorRealmManagement()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters));
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorRealmManagement() {
		return or(regex("/api/realms.*"));
	}

	@Bean
	public Docket deploymentAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());

		return new Docket(DocumentationType.SWAGGER_2).groupName("Deployment").select()
				.apis(RequestHandlerSelectors.any()).paths(buildPathSelectorDeploymentManagement()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters));
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorDeploymentManagement() {
		return or(regex("/api/deployment.*"));
	}

	@Bean
	public Docket dashboardsAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());
		final Set<String> produces = new HashSet<>(Arrays.asList(APP_JSON, APP_YAML, TEXT_PL));

		return new Docket(DocumentationType.SWAGGER_2).groupName("Dashboards").select()

				.apis(RequestHandlerSelectors.any()).paths(buildPathSelectorDashoardsManagement()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters)).produces(produces);
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorDashoardsManagement() {
		return or(regex("/api/dashboards.*"));
	}

	@Bean
	public Docket dataflowAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());
		final Set<String> produces = new HashSet<>(Arrays.asList(APP_JSON, APP_YAML, TEXT_PL));
		return new Docket(DocumentationType.SWAGGER_2).groupName("Dataflows").select()
				.apis(RequestHandlerSelectors.any()).paths(buildPathSelectorDataflowsManagement()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters)).produces(produces);
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorDataflowsManagement() {
		return or(regex("/api/dataflows.*"));
	}

	@Bean
	public Docket cacheAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());
		final Set<String> produces = new HashSet<>(Arrays.asList(APP_JSON, APP_YAML, TEXT_PL));
		return new Docket(DocumentationType.SWAGGER_2).groupName("Cache").select().apis(RequestHandlerSelectors.any())
				.paths(buildPathSelectorCacheManagement()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters)).produces(produces);
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorCacheManagement() {
		return or(regex("/api/cache.*"));
	}

	@Bean
	public Docket layerssAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());

		final Set<String> produces = new HashSet<>(Arrays.asList(APP_JSON, APP_YAML, TEXT_PL));
		return new Docket(DocumentationType.SWAGGER_2).groupName("Layers").select().apis(RequestHandlerSelectors.any())

				.paths(buildPathSelectorLayersManagement()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters)).produces(produces);
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorLayersManagement() {
		return or(regex("/api/layers.*"));
	}

	@Bean
	public Docket modelssAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());
		final Set<String> produces = new HashSet<>(Arrays.asList(APP_JSON, APP_YAML, TEXT_PL));

		return new Docket(DocumentationType.SWAGGER_2).groupName("Models").select().apis(RequestHandlerSelectors.any())

				.paths(buildPathSelectorModelsManagement()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters)).produces(produces);
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorModelsManagement() {
		return or(regex("/api/models.*"));
	}

	@Bean
	public Docket configurationAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());
		final Set<String> produces = new HashSet<>(Arrays.asList(APP_JSON, APP_YAML, TEXT_PL));

		return new Docket(DocumentationType.SWAGGER_2).groupName("Configurations").select()

				.apis(RequestHandlerSelectors.any()).paths(buildPathSelectorConfigurationManagement()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters)).produces(produces);
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorConfigurationManagement() {
		return or(regex("/api/configurations.*"));
	}

	@Bean
	public Docket videobrokerAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());
		return new Docket(DocumentationType.SWAGGER_2).groupName("Videobroker").select()
				.apis(RequestHandlerSelectors.any()).paths(buildPathSelectorVideobrokerManagement()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters));
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorVideobrokerManagement() {
		return or(regex("/api/videobroker.*"));
	}

	@Bean
	public Docket ontologyManagementAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());

		return new Docket(DocumentationType.SWAGGER_2).groupName("Ontologies").select()
				.apis(RequestHandlerSelectors.any()).paths(buildPathSelectorOntologyManagement()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters));
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorOntologyManagement() {
		return or(regex("/api/ontologies.*"));
	}

	@Bean
	public Docket reportsApi() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());

		return new Docket(DocumentationType.SWAGGER_2).groupName("Reports").select().apis(RequestHandlerSelectors.any())
				.paths(buildPathSelectorReportsApi()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters));
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorReportsApi() {
		return or(regex("/api/reports.*"));
	}

	@Bean
	public Docket microservicesApi() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());

		return new Docket(DocumentationType.SWAGGER_2).groupName("Microservices").select()
				.apis(RequestHandlerSelectors.any()).paths(buildPathSelectorMicroservicesApi()).build()

				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters));
	}

	@SuppressWarnings("unchecked")

	private Predicate<String> buildPathSelectorMicroservicesApi() {
		return or(regex("/api/microservices.*"));
	}

	@Bean
	public Docket mailManagementAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());

		return new Docket(DocumentationType.SWAGGER_2).groupName("Mail").select().apis(RequestHandlerSelectors.any())
				.paths(buildPathSelectorMailManagement()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters));
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorMailManagement() {
		return or(regex("/api/mail.*"));
	}

	@Bean
	public Docket binaryAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());

		return new Docket(DocumentationType.SWAGGER_2).groupName("Binary").select().apis(RequestHandlerSelectors.any())
				.paths(buildPathSelectorBinaryManagement()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters));
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorBinaryManagement() {
		return or(regex("/binary-repository.*"));
	}

	@Bean
	public Docket projectAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());

		return new Docket(DocumentationType.SWAGGER_2).groupName("Projects").select()
				.apis(RequestHandlerSelectors.any()).paths(buildPathSelectorProjectManagement()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters));
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorProjectManagement() {
		return or(regex("/api/projects.*"));
	}

	@Bean
	public Docket restPlannerAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());

		return new Docket(DocumentationType.SWAGGER_2).groupName("Rest Planner").select()
				.apis(RequestHandlerSelectors.any()).paths(buildPathSelectorRestPlannerManagement()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters));
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorRestPlannerManagement() {
		return or(regex("/api/restplanner.*"));
	}

	@Bean
	public Docket categorizationManagementAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());

		return new Docket(DocumentationType.SWAGGER_2).groupName("Categorization").select()
				.apis(RequestHandlerSelectors.any()).paths(buildPathSelectorCategorizationManagement()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters));
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorCategorizationManagement() {
		return or(regex("/api/categorization.*"));
	}

	@Bean
	public Docket migrationManagementAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());

		return new Docket(DocumentationType.SWAGGER_2).groupName("Migration").select()
				.apis(RequestHandlerSelectors.any()).paths(buildPathSelectorMigrationManagement()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters));
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorMigrationManagement() {
		return or(regex("/api/migration.*"));
	}

	@Bean
	public Docket querytoolAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());

		return new Docket(DocumentationType.SWAGGER_2).groupName("Querytool").select()
				.apis(RequestHandlerSelectors.any()).paths(buildPathSelectorQueryToolManagement()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters));
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorQueryToolManagement() {
		return or(regex("/api/querytool.*"));
	}

	@Bean
	public Docket flowEngineManagementAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());

		return new Docket(DocumentationType.SWAGGER_2).groupName("FlowEngine").select()
				.apis(RequestHandlerSelectors.any()).paths(buildPathSelectorFlowEngineManagement()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters));
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorFlowEngineManagement() {
		return or(regex("/api/flowengine.*"));
	}

	@Bean
	public Docket auditManagementAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());

		return new Docket(DocumentationType.SWAGGER_2).groupName("Audit").select().apis(RequestHandlerSelectors.any())
				.paths(buildPathSelectorAuditManagement()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters));
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorAuditManagement() {
		return or(regex("/api/audit.*"));
	}

	@Bean
	public Docket gadgetDatasourceManagementAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());
		final Set<String> produces = new HashSet<>(Arrays.asList(APP_JSON, APP_YAML, TEXT_PL));

		return new Docket(DocumentationType.SWAGGER_2).groupName("Gadget Datasources").select()

				.apis(RequestHandlerSelectors.any()).paths(buildPathSelectorDatasourcesManagement()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters)).produces(produces);
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorDatasourcesManagement() {
		return or(regex("/api/gadgetdatasources.*"));
	}

	@Bean
	public Docket webProjectsAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());
		final Set<String> produces = new HashSet<>(Arrays.asList(APP_JSON, APP_YAML, TEXT_PL));

		return new Docket(DocumentationType.SWAGGER_2).groupName("Web projects").select()

				.apis(RequestHandlerSelectors.any()).paths(buildPathSelectorWebProjects()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters)).produces(produces);
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorWebProjects() {
		return or(regex("/api/webprojects.*"));
	}

	@Bean
	public Docket rulesAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());
		final Set<String> produces = new HashSet<>(Arrays.asList(APP_JSON, APP_YAML, TEXT_PL));

		return new Docket(DocumentationType.SWAGGER_2).groupName("Rules").select().apis(RequestHandlerSelectors.any())
				.paths(buildPathSelectorRules()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters)).produces(produces);
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorRules() {
		return or(regex("/api/rules.*"));
	}

	@Bean
	public Docket moduleNotifications() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());
		final Set<String> produces = new HashSet<>(Arrays.asList(APP_JSON, TEXT_PL));

		return new Docket(DocumentationType.SWAGGER_2).groupName("Module Notifications").select()
				.apis(RequestHandlerSelectors.any()).paths(buildPathSelectorNotifications()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters)).produces(produces);
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorNotifications() {
		return or(regex("/api/notifier.*"));
	}

	@Bean
	public Docket gadgetTemplateManagementAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());
		final Set<String> produces = new HashSet<>(Arrays.asList(APP_JSON, APP_YAML, TEXT_PL));

		return new Docket(DocumentationType.SWAGGER_2).groupName("Gadget Templates").select()

				.apis(RequestHandlerSelectors.any()).paths(buildPathSelectorGadgetTemplateManagement()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters)).produces(produces);
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorGadgetTemplateManagement() {
		return or(regex("/api/gadgettemplates.*"));
	}

	@Bean
	public Docket gadgetManagementAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());
		final Set<String> produces = new HashSet<>(Arrays.asList(APP_JSON, APP_YAML, TEXT_PL));

		return new Docket(DocumentationType.SWAGGER_2).groupName("Gadgets").select()

				.apis(RequestHandlerSelectors.any()).paths(buildPathSelectorGadgetManagement()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters)).produces(produces);
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorGadgetManagement() {
		return or(regex("/api/gadgets.*"));
	}

	@Bean
	public Docket importToolAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());
		final Set<String> produces = new HashSet<>(Arrays.asList(APP_JSON, APP_YAML, TEXT_PL));

		return new Docket(DocumentationType.SWAGGER_2).groupName("Import tool").select()
				.apis(RequestHandlerSelectors.any()).paths(buildPathSelectorImportTool()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters)).produces(produces);
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorImportTool() {
		return or(regex("/api/importtool.*"));
	}

	@Bean
	public Docket openDataPortalAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());
		final Set<String> produces = new HashSet<>(Arrays.asList(APP_JSON, APP_YAML, TEXT_PL));
		return new Docket(DocumentationType.SWAGGER_2).groupName("Open Data Portal").select()
				.apis(RequestHandlerSelectors.any()).paths(buildPathSelectorOpenDataPortalManagement()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters)).produces(produces);
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorOpenDataPortalManagement() {
		return or(regex("/api/opendata.*"));
	}

	@Bean
	public Docket dataRefinerAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());
		final Set<String> produces = new HashSet<>(Arrays.asList(APP_JSON, APP_YAML, TEXT_PL));

		return new Docket(DocumentationType.SWAGGER_2).groupName("Data Refiner").select()
				.apis(RequestHandlerSelectors.any()).paths(buildPathDataCleanerAPI()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters)).produces(produces);
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathDataCleanerAPI() {
		return or(regex("/api/datarefiner.*"));
	}

	@Bean
	public Docket lowCodeAPI() {

		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());
		final Set<String> produces = new HashSet<>(Arrays.asList(APP_JSON, APP_YAML, TEXT_PL));

		return new Docket(DocumentationType.SWAGGER_2).groupName("Low Code Generation").select()
				.apis(RequestHandlerSelectors.any()).paths(buildPathSelectorLowCode()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters)).produces(produces);
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorLowCode() {
		return or(regex("/api/low-code.*"));
	}

	@Bean
	public Docket favoriteGadgetAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());
		final Set<String> produces = new HashSet<>(Arrays.asList(APP_JSON, APP_YAML, TEXT_PL));
		return new Docket(DocumentationType.SWAGGER_2).groupName("Favorite Gadgets").select()
				.apis(RequestHandlerSelectors.any()).paths(buildPathSelectorFavoriteGadgetManagement()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters)).produces(produces);
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorFavoriteGadgetManagement() {
		return or(regex("/api/favoritegadget.*"));
	}
}
