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
package com.minsait.onesait.platform.multitenant.util;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;
import com.minsait.onesait.platform.multitenant.config.model.Vertical;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DataSourceUtil {

	private static final String CONFIGDB_DEFAULT_DB = "onesaitplatform_config";
	private static final String DEFAULT_DS_BEAN_NAME = "defaultDS";

	@Value("${spring.datasource.hikari.jdbc-url}")
	private String connUrl;
	@Value("${spring.datasource.hikari.username}")
	private String username;
	@Value("${spring.datasource.hikari.password}")
	private String passphrase;

	@Autowired
	private ApplicationContext context;

	@Autowired
	private BeanUtil beanUtil;

	public DataSource createAndConfigureDataSource(Vertical vertical) {
		return this.createAndConfigureDataSource(vertical.getSchema());
	}

	private DataSource createAndConfigureDataSource(String schema) {
		final DataSource defaultDS = (DataSource) context.getBean(DEFAULT_DS_BEAN_NAME);
		final String newURL = ((HikariDataSource) defaultDS).getJdbcUrl().replace(CONFIGDB_DEFAULT_DB, schema);
		((HikariDataSource) defaultDS).setJdbcUrl(newURL);
		return defaultDS;
	}

	public DataSource createDefaultDatasource() {
		return this.createAndConfigureDataSource(Tenant2SchemaMapper.DEFAULT_SCHEMA);
	}

}
