package com.minsait.onesait.microservice.config;

import static springfox.documentation.builders.PathSelectors.regex;

import java.util.Arrays;
import java.util.HashSet;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.Sets;

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
	private static final String CONTACT_URL = "https://www.sofia4cities.com";
	private static final String CONTACT_EMAIL = "select4citiesminsait@gmail.com";

	private static final String HEADER_STR = "header";
	private static final String STRING_STR = "string";
	private static final String AUTH_STR = "Authorization";

	private static final String REST_BASE_PACKAGE = "com.minsait.onesait.microservice.rest";
	private static final String APP_JSON = "application/json";
	private static final String TEXT_PL = "text/plain";
	private static final String APP_YAML = "application/yaml";

	@Bean
	public ApiInfo apiInfo() {
		return new ApiInfoBuilder().title(INFO_TITLE).description(INFO_DESCRIPTION).termsOfServiceUrl(CONTACT_URL)
				.contact(new Contact(CONTACT_NAME, CONTACT_URL, CONTACT_EMAIL)).license(INFO_VERSION)
				.licenseUrl(LICENSE_URL).version(LICENSE_NAME).build();
	}

	@Bean
	public Docket userApi() {

		return new Docket(DocumentationType.SWAGGER_2)
				.produces(new HashSet<>(Arrays.asList(APP_JSON, TEXT_PL, APP_YAML))).groupName("user-api").select()
				.apis(RequestHandlerSelectors.basePackage(REST_BASE_PACKAGE)).paths(regex("/user.*")).build()
				.globalOperationParameters(Arrays.asList(authHeader())).protocols(Sets.newHashSet("http", "https"));
	}

	@Bean
	public Docket restaurantApi() {

		return new Docket(DocumentationType.SWAGGER_2)
				.produces(new HashSet<>(Arrays.asList(APP_JSON, TEXT_PL, APP_YAML))).groupName("${ONTOLOGY}-api")
				.select().apis(RequestHandlerSelectors.basePackage(REST_BASE_PACKAGE)).paths(regex("/${ONTOLOGY}.*"))
				.build().protocols(Sets.newHashSet("http", "https"));
	}


	private Parameter authHeader() {
		final ParameterBuilder pb = new ParameterBuilder();
		return pb.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true).build();
	}
}
