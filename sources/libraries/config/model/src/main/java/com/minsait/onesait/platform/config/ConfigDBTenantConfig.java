/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2022 SPAIN
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
package com.minsait.onesait.platform.config;

import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import lombok.extern.slf4j.Slf4j;

@Configuration
@ComponentScan(value = "com.minsait.onesait.platform", excludeFilters = {
		@ComponentScan.Filter(type = FilterType.REGEX, pattern = { "com.minsait.onesait.platform.config.services.*",
				"com.minsait.onesait.platform.multitenant.*" }) })
@EnableJpaRepositories(entityManagerFactoryRef = "entityManagerFactory", transactionManagerRef = "transactionManager", basePackages = {
		"com.minsait.onesait.platform.config.repository" })
@EnableTransactionManagement
@Slf4j
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE - 1)
@Import({ ConfigDBMasterConfig.class })
public class ConfigDBTenantConfig {

	public static final String PERSISTENCE_UNIT_NAME_TENANT = "onesaitplatform";
	public static final String CONFIGDB_TENANT_ENVVAR = "CONFIGDB_SCHEMA";

}
