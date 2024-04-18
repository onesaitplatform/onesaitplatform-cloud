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
package com.minsait.onesait.platform.bpm.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Value("${onesaitplatform.analytics.dataflow.version:3.10.0}")
	private String streamsetsVersion;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
		registry.addResourceHandler("/notebooks/app/**").addResourceLocations("classpath:/static/notebooks/");
		registry.addResourceHandler("/dataflow/{instance}/app/**")
				.addResourceLocations("classpath:/static/dataflow/" + streamsetsVersion + "/");
		registry.addResourceHandler("/dataflow/app/**")
				.addResourceLocations("classpath:/static/dataflow/" + streamsetsVersion + "/");
		registry.addResourceHandler("/gitlab/**").addResourceLocations("classpath:/static/gitlab/");
	}
}
