package com.minsait.onesait.platform.tools.datagrid.client.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {
	
	@Value("${swagger.external.host:}")
	private String externalHost;
	
	
	@Bean
	public Docket api() {
		if(externalHost.length()>0) {
			return new Docket(DocumentationType.SWAGGER_2).host(externalHost).select().apis(RequestHandlerSelectors.any())
					.paths(PathSelectors.any()).build();
		}else {
			return new Docket(DocumentationType.SWAGGER_2).select().apis(RequestHandlerSelectors.any())
					.paths(PathSelectors.any()).build();
		}
	}
}


