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
package com.minsait.onesait.platform.business.services.presto.datasource;

import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import lombok.Getter;

@Component
public class PrestoDatasourceConfigurationService {

	private static final String HISTORICAL_CATALOG = "prestodb-historical-catalog";

	private static final String HISTORICAL_SCHEMA = "prestodb-historical-schema";

	private static final String REALTIMEDB_CATALOG = "prestodb-realtimedb-catalog";

	@Autowired
	private IntegrationResourcesService integrationResourcesService;

	@Getter
	@Value("${onesaitplatform.database.prestodb.historicalCatalog:minio}")
	private String historicalCatalog;

	@Getter
	@Value("${onesaitplatform.database.prestodb.historicalSchema:default}")
	private String historicalSchema;

	@Getter
	@Value("${onesaitplatform.database.prestodb.realtimedbCatalog:realtimedb}")
	private String realtimedbCatalog;

	@PostConstruct
	public void loadConfig() {
		historicalCatalog = Optional.ofNullable((String) integrationResourcesService.getGlobalConfiguration().getEnv()
				.getDatabase().get(HISTORICAL_CATALOG)).orElse(historicalCatalog);
		historicalSchema = Optional.ofNullable((String) integrationResourcesService.getGlobalConfiguration().getEnv()
				.getDatabase().get(HISTORICAL_SCHEMA)).orElse(historicalSchema);
		realtimedbCatalog = Optional.ofNullable((String) integrationResourcesService.getGlobalConfiguration().getEnv()
				.getDatabase().get(REALTIMEDB_CATALOG)).orElse(realtimedbCatalog);
	}

	public Boolean isHistoricalCatalog(String catalog) {
		return catalog.equals(historicalCatalog);
	}

	public Boolean isRealtimedbCatalog(String catalog) {
		return catalog.equals(realtimedbCatalog);
	}

}
