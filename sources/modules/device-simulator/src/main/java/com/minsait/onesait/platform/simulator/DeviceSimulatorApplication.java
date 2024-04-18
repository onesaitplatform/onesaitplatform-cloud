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
package com.minsait.onesait.platform.simulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.endpoint.MetricsEndpoint;
import org.springframework.boot.actuate.endpoint.MetricsEndpointMetricReader;
import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
@EnableAutoConfiguration
// @EnableCaching
@ComponentScan(basePackages = { "com.minsait.onesait.platform" }, lazyInit = true)
public class DeviceSimulatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(DeviceSimulatorApplication.class, args);
	}

	@Configuration
	@Profile("default")
	@ComponentScan(basePackages = { "com.ibm.javametrics.spring", "com.minsait.onesait.platform" }, lazyInit = true)
	static class LocalConfig {
	}

	/**
	 * Exports the all endpoint metrics like those implementing
	 * {@link PublicMetrics}.
	 */
	@Bean
	public MetricsEndpointMetricReader metricsEndpointMetricReader(MetricsEndpoint metricsEndpoint) {
		return new MetricsEndpointMetricReader(metricsEndpoint);
	}

}
