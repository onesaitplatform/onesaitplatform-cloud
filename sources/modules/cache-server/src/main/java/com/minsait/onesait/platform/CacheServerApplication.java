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
package com.minsait.onesait.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.CacheStatisticsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.EndpointMBeanExportAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.EndpointWebMvcAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.EndpointWebMvcManagementContextConfiguration;
import org.springframework.boot.actuate.autoconfigure.HealthIndicatorAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.JolokiaAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.MetricExportAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.MetricFilterAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.PublicMetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.TraceWebFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerPropertiesAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import de.codecentric.boot.admin.client.config.SpringBootAdminClientAutoConfiguration;

@SpringBootApplication
@Configuration
@Import({ AopAutoConfiguration.class, CacheStatisticsAutoConfiguration.class,
		EmbeddedServletContainerAutoConfiguration.class, EndpointAutoConfiguration.class,
		EndpointMBeanExportAutoConfiguration.class, EndpointWebMvcAutoConfiguration.class,
		EndpointWebMvcManagementContextConfiguration.class, HealthIndicatorAutoConfiguration.class,
		JolokiaAutoConfiguration.class, MetricExportAutoConfiguration.class, MetricFilterAutoConfiguration.class,
		PublicMetricsAutoConfiguration.class, ServerPropertiesAutoConfiguration.class,
		SpringBootAdminClientAutoConfiguration.class, TraceWebFilterAutoConfiguration.class })
@ComponentScan(basePackages = "com.minsait.onesait.platform.cacheserver.config")
public class CacheServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CacheServerApplication.class, args);
	}
}
