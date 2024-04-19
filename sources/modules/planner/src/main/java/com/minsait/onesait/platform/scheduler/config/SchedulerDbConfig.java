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
package com.minsait.onesait.platform.scheduler.config;

import static com.minsait.onesait.platform.scheduler.PropertyNames.SCHEDULER_PROPERTIES_LOCATION;
import static com.minsait.onesait.platform.scheduler.config.DbConfigPropertyNames.SCHEDULER_BASE_PACKAGE;
import static com.minsait.onesait.platform.scheduler.config.DbConfigPropertyNames.SCHEDULER_DATASOURCE_NAME;
import static com.minsait.onesait.platform.scheduler.config.DbConfigPropertyNames.SCHEDULER_DATASOURCE_PROPERTY;
import static com.minsait.onesait.platform.scheduler.config.DbConfigPropertyNames.SCHEDULER_ENTITY_MANAGER_FACTORY_NAME;
import static com.minsait.onesait.platform.scheduler.config.DbConfigPropertyNames.SCHEDULER_JPA_PROPERTY;
import static com.minsait.onesait.platform.scheduler.config.DbConfigPropertyNames.SCHEDULER_TRANSACTION_MANAGER_NAME;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
@ConditionalOnResource(resources = SCHEDULER_PROPERTIES_LOCATION)
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef = SCHEDULER_ENTITY_MANAGER_FACTORY_NAME, transactionManagerRef = SCHEDULER_TRANSACTION_MANAGER_NAME, basePackages = {
		SCHEDULER_BASE_PACKAGE })
public class SchedulerDbConfig {

	@Bean
	@ConfigurationProperties(SCHEDULER_JPA_PROPERTY)
	public JpaProperties quartzJpaProperties() {
		return new JpaProperties();
	}

	@Bean(SCHEDULER_DATASOURCE_NAME)
	@ConfigurationProperties(SCHEDULER_DATASOURCE_PROPERTY)
	public DataSource quartzDatasource() {
		return new HikariDataSource();
	}

	@Bean(SCHEDULER_ENTITY_MANAGER_FACTORY_NAME)
	@DependsOn(SCHEDULER_DATASOURCE_NAME)
	public LocalContainerEntityManagerFactoryBean quartzEntityManagerFactory(EntityManagerFactoryBuilder builder,
			@Qualifier(SCHEDULER_DATASOURCE_NAME) DataSource dataSource) {

		return builder.dataSource(dataSource).packages(SCHEDULER_BASE_PACKAGE).persistenceUnit("quartz")
				.properties(quartzJpaProperties().getProperties()).build();
	}

	@Bean(SCHEDULER_TRANSACTION_MANAGER_NAME)
	@DependsOn(SCHEDULER_ENTITY_MANAGER_FACTORY_NAME)
	public PlatformTransactionManager quartzTransactionManager(
			@Qualifier(SCHEDULER_ENTITY_MANAGER_FACTORY_NAME) EntityManagerFactory entityManagerFactory) {
		return new JpaTransactionManager(entityManagerFactory);
	}

}
