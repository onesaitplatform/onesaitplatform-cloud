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
package com.minsait.onesait.platform.persistence.timescaledb.config;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@Conditional(TimescaleDBEnabledCondition.class)
public class TimescaleDBConfiguration {
	
	public static final String TIMESCALEDB_DATASOURCE_BEAN_NAME = "TimescaleDBDatasource";
	public static final String TIMESCALEDB_TEMPLATE_JDBC_BEAN_NAME = "TimescaleDBJdbcTemplate";
	public static final String TIMESCALEDB_PARAMETER_TEMPLATE_JDBC_BEAN_NAME = "TimescaleDBJdbcParamaterTemplate";
	
	@Value("${onesaitplatform.database.timescaledb.url}")
	private String jdbcUrl;
	@Value("${onesaitplatform.database.timescaledb.user:postgres}")
	private String timescaleUser;
	@Value("${onesaitplatform.database.timescaledb.password:password}")
	private String timescalePass;


	@Bean(name = TIMESCALEDB_DATASOURCE_BEAN_NAME)
	public DataSource dataSource() {

		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName("org.postgresql.Driver");
		dataSource.setUrl(jdbcUrl);
		dataSource.setUsername(timescaleUser);
		dataSource.setPassword(timescalePass);
		log.info("Initialized TimescaleDB");

		return dataSource;

	}
	

	@Bean(name = TIMESCALEDB_TEMPLATE_JDBC_BEAN_NAME)
	public JdbcTemplate timescaleDBJdbcTemplate(@Qualifier(TIMESCALEDB_DATASOURCE_BEAN_NAME) DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}
	
	@Bean(name = TIMESCALEDB_PARAMETER_TEMPLATE_JDBC_BEAN_NAME)
	public NamedParameterJdbcTemplate timescaleDBJdbcParameterTemplate(@Qualifier(TIMESCALEDB_DATASOURCE_BEAN_NAME) DataSource dataSource) {
		return new NamedParameterJdbcTemplate(dataSource);
	}

}
