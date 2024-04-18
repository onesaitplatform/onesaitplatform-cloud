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
package com.minsait.onesait.platform.persistence.nebula.configuration;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.vesoft.nebula.client.graph.NebulaPoolConfig;
import com.vesoft.nebula.client.graph.data.HostAddress;
import com.vesoft.nebula.client.graph.net.NebulaPool;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class NebulaGraphConfiguration {

	private static final Integer NEBULA_POOL_SIZE = 10;

	@Autowired
	private IntegrationResourcesService integrationResourcesService;

	@Bean
	public NebulaPool nebulaPool() {
		final NebulaPool pool = new NebulaPool();
		try {
			final NebulaPoolConfig nebulaPoolConfig = new NebulaPoolConfig();
			nebulaPoolConfig.setMaxConnSize(getPoolSize());
			final List<HostAddress> addresses = new ArrayList<>();
			configureHosts(addresses);
			pool.init(addresses, nebulaPoolConfig);
		} catch (final Exception e) {
			log.warn("Could not initialize Nebula Graph Connection Pool", e);
		}
		return pool;
	}

	private void configureHosts(List<HostAddress> addresses) {
		final String hosts = (String) integrationResourcesService.getGlobalConfiguration().getEnv().getDatabase()
				.get("nebula-hosts");
		final String[] splitHosts = hosts.split(",");
		for (final String host : splitHosts) {
			addresses.add(new HostAddress(host.split(":")[0], Integer.valueOf(host.split(":")[1])));
		}
	}

	private Integer getPoolSize() {
		try {
			final String poolSize = (String) integrationResourcesService.getGlobalConfiguration().getEnv().getDatabase()
					.get("nebula-pool-size");
			return Integer.valueOf(poolSize);
		} catch (final Exception e) {
			log.error("No pool size found, returning default value {}", NEBULA_POOL_SIZE, e.getMessage());
			return NEBULA_POOL_SIZE;
		}
	}
}
