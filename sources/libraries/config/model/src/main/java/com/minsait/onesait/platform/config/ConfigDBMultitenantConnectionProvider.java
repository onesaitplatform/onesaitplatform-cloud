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

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.sql.DataSource;

import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;
import com.minsait.onesait.platform.multitenant.config.model.Vertical;
import com.minsait.onesait.platform.multitenant.config.repository.VerticalRepository;
import com.minsait.onesait.platform.multitenant.util.DataSourceUtil;

@Configuration
public class ConfigDBMultitenantConnectionProvider extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Autowired
	private VerticalRepository verticalRepository;
	@Autowired
	private DataSourceUtil dataSourceUtil;

	private final Map<String, DataSource> dataSources = new TreeMap<>();

	@Override
	protected DataSource selectAnyDataSource() {
		if (dataSources.isEmpty()) {
			final List<Vertical> verticals = verticalRepository.findAll();
			verticals.forEach(v -> dataSources.put(v.getSchema(), dataSourceUtil.createAndConfigureDataSource(v)));
		}

		// try to always return default tenant schema
		if (!dataSources.containsKey(Tenant2SchemaMapper.DEFAULT_SCHEMA))
			dataSources.put(Tenant2SchemaMapper.DEFAULT_SCHEMA, dataSourceUtil.createDefaultDatasource());

		String returnDS = Tenant2SchemaMapper.DEFAULT_SCHEMA;
		if (StringUtils.hasText(System.getenv().get(ConfigDBTenantConfig.CONFIGDB_TENANT_ENVVAR)))
			returnDS = System.getenv().get(ConfigDBTenantConfig.CONFIGDB_TENANT_ENVVAR);

		return dataSources.get(returnDS);
	}

	@Override
	protected DataSource selectDataSource(String tenantIdentifier) {
		// to-do tenant if lost

		if (!dataSources.containsKey(tenantIdentifier)) {
			final List<Vertical> verticals = verticalRepository.findAll();
			verticals.forEach(v -> dataSources.put(v.getSchema(), dataSourceUtil.createAndConfigureDataSource(v)));
		}
		return dataSources.get(tenantIdentifier);
	}

}
