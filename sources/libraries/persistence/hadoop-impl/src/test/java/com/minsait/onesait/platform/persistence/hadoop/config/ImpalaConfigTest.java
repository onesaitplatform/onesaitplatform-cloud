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
package com.minsait.onesait.platform.persistence.hadoop.config;

import static org.junit.Assert.assertTrue;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import com.minsait.onesait.platform.persistence.hadoop.common.NameBeanConst;

import lombok.extern.slf4j.Slf4j;

@TestConfiguration
@Slf4j
public class ImpalaConfigTest {

	@Value("${onesaitplatform.database.impala.url}")
	private String url;

	@Bean(name = NameBeanConst.IMPALA_DATASOURCE_BEAN_NAME)
	public DataSource dataSource() {

		BasicDataSource dataSource = new BasicDataSource();

		dataSource.setUrl(url);

		dataSource.setDriverClassName("org.apache.hive.jdbc.HiveDriver");

		log.error("Initialized impala");

		return dataSource;
	}

	@Bean(name = NameBeanConst.IMPALA_TEMPLATE_JDBC_BEAN_NAME)
	public JdbcTemplate hiveJdbcTemplate(@Qualifier(NameBeanConst.IMPALA_DATASOURCE_BEAN_NAME) DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

	@Test
	public void testSimple() {
		assertTrue(true);
	}
}
