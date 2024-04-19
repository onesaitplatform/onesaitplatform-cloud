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
package com.minsait.onesait.platform.persistence.cosmosdb.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.documentdb.ConnectionPolicy;
import com.microsoft.azure.documentdb.ConsistencyLevel;
import com.microsoft.azure.documentdb.DocumentClient;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class CosmosDBConfiguration {

	@Value("${onesaitplatform.database.cosmosdb.host:https://host:443/}")
	private String cosmosHost;
	@Value("${onesaitplatform.database.cosmosdb.master-key:masterKey}")
	private String cosmosMasterKey;

	@Autowired
	private IntegrationResourcesService resourcesService;

	@Bean
	public DocumentClient cosmosClient() {
		return new DocumentClient(host(), masterKey(), new ObjectMapper(), connectionPolicy(),
				ConsistencyLevel.Eventual);
	}

	private String host() {
		try {
			final String host = (String) resourcesService.getGlobalConfiguration().getEnv().getDatabase()
					.get("cosmosdb-host");
			if (host == null)
				return cosmosHost;
			else
				return host;
		} catch (final Exception e) {
			log.debug(
					"No configuration found on Global Configuration: propery database.cosmosdb-host, using default value from yml");
			return cosmosHost;
		}
	}

	private String masterKey() {
		try {
			final String masterKey = (String) resourcesService.getGlobalConfiguration().getEnv().getDatabase()
					.get("cosmosdb-master-key");
			if (masterKey == null)
				return cosmosMasterKey;
			else
				return masterKey;
		} catch (final Exception e) {
			log.debug(
					"No configuration found on Global Configuration: propery database.cosmosdb-master-key, using default value from yml");
			return cosmosMasterKey;
		}
	}

	public ConnectionPolicy connectionPolicy() {
		return ConnectionPolicy.GetDefault();
	}
}
