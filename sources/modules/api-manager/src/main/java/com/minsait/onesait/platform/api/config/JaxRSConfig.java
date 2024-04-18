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
package com.minsait.onesait.platform.api.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.openapi.OpenApiCustomizer;
import org.apache.cxf.jaxrs.openapi.OpenApiFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

@Configuration
public class JaxRSConfig {

	@Autowired
	ApplicationContext applicationContext;

	@Bean
	public Server rsServer(Bus bus) {

		final List<Object> lista = new ArrayList<>();
		final JAXRSServerFactoryBean endpoint = new JAXRSServerFactoryBean();
		endpoint.setBus(bus);

		final Map<String, Object> beansOfType2 = applicationContext
				.getBeansWithAnnotation(io.swagger.annotations.Api.class);

		final Iterator<Entry<String, Object>> iterator = beansOfType2.entrySet().iterator();
		while (iterator.hasNext()) {
			lista.add(iterator.next().getValue());
		}

		final List<Object> providers = new ArrayList<>();
		providers.add(new JacksonJaxbJsonProvider());

		endpoint.setProviders(providers);
		endpoint.setServiceBeans(lista);
		endpoint.setAddress("/");
		endpoint.setFeatures(Arrays.asList(createOpenApiFeature()));
		endpoint.setProperties(
				Collections.singletonMap("org.apache.cxf.management.service.counter.name", "cxf-services."));
		return endpoint.create();
	}

	public OpenApiFeature createOpenApiFeature() {
		final OpenApiFeature openApiFeature = new OpenApiFeature();
		openApiFeature.setPrettyPrint(true);
		openApiFeature.setTitle("onesait Platform API Manager");
		openApiFeature.setContactName("The onesait Platform team");
		openApiFeature.setDescription("");
		openApiFeature.setVersion("1.0.0");
		openApiFeature.setSupportSwaggerUi(true);
		final OpenApiCustomizer customizer = new JaxRSOpenAPICustomizer();
		customizer.setDynamicBasePath(true);
		openApiFeature.setCustomizer(customizer);
		openApiFeature.setScan(false);
		return openApiFeature;
	}

}
