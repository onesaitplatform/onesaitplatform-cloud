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
package com.minsait.onesait.platform.persistence.hadoop.config;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import com.minsait.onesait.platform.persistence.hadoop.common.NameBeanConst;
import com.minsait.onesait.platform.persistence.hadoop.config.condition.HadoopEnabledCondition;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@Conditional(HadoopEnabledCondition.class)
public class ImpalaConfiguration {

	@Value("${onesaitplatform.database.impala.url}")
	private String url;

	@Bean(name = NameBeanConst.IMPALA_DATASOURCE_BEAN_NAME)
	public DataSource dataSource() {

		BasicDataSource dataSource = new BasicDataSource();
		// DataSourceBuilder dataSource = DataSourceBuilder.create();

		dataSource.setUrl(url);
		// org.apache.hadoop.hive.jdbc.HiveDriver
		dataSource.setDriverClassName("org.apache.hive.jdbc.HiveDriver");

		log.info("Initialized impala");

		// DataSourceBuilder factory =
		// DataSourceBuilder.create().driverClassName("org.apache.hive.jdbc.HiveDriver")
		// .url(url).username(username).password(password);
		/*
		 * dataSource.setDriverClassName(driverClassName); dataSource.setUrl(url);
		 * dataSource.setUsername(username); dataSource.setPassword("");
		 * dataSource.setValidationQuery(validationQuery);
		 * dataSource.setTestOnBorrow(true); dataSource.setTestOnReturn(true);
		 * dataSource.setTestWhileIdle(true);
		 * dataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
		 * dataSource.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
		 * dataSource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
		 * dataSource.setInitialSize(initialSize); dataSource.setMaxActive(maxActive);
		 * dataSource.setMaxIdle(maxIdle); dataSource.setMinIdle(minIdle);
		 * dataSource.setMaxWait(maxWait);
		 * dataSource.setMaxOpenPreparedStatements(maxOpenPreparedStatements);
		 * dataSource.setPoolPreparedStatements(poolPreparedStatements);
		 * dataSource.setRemoveAbandoned(removeAbandoned);
		 * dataSource.setRemoveAbandonedTimeout(removeAbandonedTimeout);
		 */
		return dataSource;
	}

	@Bean(name = NameBeanConst.IMPALA_TEMPLATE_JDBC_BEAN_NAME)
	public JdbcTemplate hiveJdbcTemplate(@Qualifier(NameBeanConst.IMPALA_DATASOURCE_BEAN_NAME) DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

}
