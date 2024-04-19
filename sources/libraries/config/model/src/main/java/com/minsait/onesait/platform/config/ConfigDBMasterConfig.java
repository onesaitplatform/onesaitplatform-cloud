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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.hibernate.MultiTenancyStrategy;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.core.Ordered;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.minsait.onesait.platform.commons.security.PasswordEncoder;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;
import com.minsait.onesait.platform.multitenant.config.repository.VerticalRepository;
import com.minsait.onesait.platform.multitenant.util.DataSourceUtil;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableJpaRepositories(basePackages = { "com.minsait.onesait.platform.multitenant.config.repository",
		"com.minsait.onesait.platform.multitenant.config.model" }, entityManagerFactoryRef = "masterEntityManagerFactory", transactionManagerRef = "masterTransactionManager")
@EnableTransactionManagement
@ComponentScan(value = "com.minsait.onesait.platform.multitenant.config")
@Slf4j
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
public class ConfigDBMasterConfig {

	private static final String DEFAULT_DS_BEAN_NAME = "defaultDS";

	@Autowired
	private Tenant2SchemaMapper schemaMapper;
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Value("${spring.jpa.properties.hibernate.dialect:org.hibernate.dialect.MySQL5InnoDBDialect}")
	private String hibernateDialect;
	@Value("${spring.jpa.properties.hibernate.show_sql:false}")
	private boolean hibernateShowSQL;
	@Value("${spring.jpa.properties.hibernate.format_sql:false}")
	private boolean hibernateFormatSQL;
	@Value("${spring.jpa.hibernate.ddl-auto:none}")
	private String hibernateDDLAutoMode;

	@Bean(name = "masterDataSource")
	@Primary
	@ConfigurationProperties(prefix = "master.datasource.hikari")
	public DataSource masterDataSource() {
		return new HikariDataSource();
	}

	@Bean(name = "masterEntityManagerFactory")
	public LocalContainerEntityManagerFactoryBean masterEntityManagerFactory() {
		final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		em.setDataSource(masterDataSource());
		em.setPackagesToScan("com.minsait.onesait.platform.multitenant.config.model");
		em.setPersistenceUnitName("onesaitPlatform-masterdb");
		final HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		vendorAdapter.setGenerateDdl(true);
		em.setJpaVendorAdapter(vendorAdapter);

		Map<String, Object> hibernateSettings = new LinkedHashMap<>();
		hibernateSettings.putAll(jpaProperties().getProperties());
		
		em.setJpaPropertyMap(hibernateSettings);
		return em;
	}

	@Bean(name = "masterTransactionManager")
	public PlatformTransactionManager masterTransactionManager(
			@Qualifier("masterEntityManagerFactory") EntityManagerFactory emf) {
		log.info("Loaded master transaction manager");
		final JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(emf);
		return transactionManager;
	}

	@Bean
	public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
		log.info("Loaded master PersistenceExceptionTranslationPostProcessor");
		return new PersistenceExceptionTranslationPostProcessor();
	}

	@Bean
	@Primary
	@ConfigurationProperties("spring.jpa")
	public JpaProperties jpaProperties() {
		return new JpaProperties();
	}

	@Bean(name = "jpaVendorAdapter")
	public JpaVendorAdapter jpaVendorAdapter() {
		log.info("Loaded tenant jpaVendorAdapter");
		return new HibernateJpaVendorAdapter();
	}

	@Bean(name = "transactionManager")
	@Primary
	public PlatformTransactionManager transactionManager(
			@Qualifier("entityManagerFactory") EntityManagerFactory tenantEntityManager) {
		log.info("Loaded tenant transactionManager");
		final JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(tenantEntityManager);
		return transactionManager;
	}

	/**
	 * The multi tenant connection provider
	 *
	 * @return
	 */
	@Bean(name = "datasourceBasedMultitenantConnectionProvider")
	@DependsOn("masterEntityManagerFactory")
	public MultiTenantConnectionProvider multiTenantConnectionProvider(VerticalRepository verticalRepository,
			DataSourceUtil dataSourceUtil) {
		log.info("Loaded tenant multiTenantConnectionProvider");
		return new ConfigDBMultitenantConnectionProvider();
	}

	/**
	 * The current tenant identifier resolver
	 *
	 * @return
	 */
	@Bean(name = "currentTenantIdentifierResolver")
	public CurrentTenantIdentifierResolver currentTenantIdentifierResolver() {
		log.info("Loaded tenant currentTenantIdentifierResolver");
		return new ConfigDBCurrentTenantResolver();
	}

	/**
	 * Creates the entity manager factory bean which is required to access the JPA
	 * functionalities provided by the JPA persistence provider, i.e. Hibernate in
	 * this case.
	 *
	 * @param connectionProvider
	 * @param tenantResolver
	 * @return
	 */
	@Bean(name = "entityManagerFactory")
	@Primary
	@DependsOn("datasourceBasedMultitenantConnectionProvider")
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(
			@Qualifier("datasourceBasedMultitenantConnectionProvider") MultiTenantConnectionProvider connectionProvider,
			@Qualifier("currentTenantIdentifierResolver") CurrentTenantIdentifierResolver tenantResolver) {

		log.info("tenantEntityManagerFactory setting up!");
		final LocalContainerEntityManagerFactoryBean emfBean = new LocalContainerEntityManagerFactoryBean();
		// All tenant related entities, repositories and service classes must be scanned
		emfBean.setPackagesToScan("com.minsait.onesait.platform.config");
		emfBean.setJpaVendorAdapter(jpaVendorAdapter());
		emfBean.setPersistenceUnitName(ConfigDBTenantConfig.PERSISTENCE_UNIT_NAME_TENANT);
		final Map<String, Object> properties = new HashMap<>();
		properties.put(org.hibernate.cfg.Environment.MULTI_TENANT, MultiTenancyStrategy.SCHEMA);
		properties.put(org.hibernate.cfg.Environment.MULTI_TENANT_CONNECTION_PROVIDER, connectionProvider);
		properties.put(org.hibernate.cfg.Environment.MULTI_TENANT_IDENTIFIER_RESOLVER, tenantResolver);
		properties.put(org.hibernate.cfg.Environment.PHYSICAL_NAMING_STRATEGY,
				"com.minsait.onesait.platform.config.converters.CustomPhysicalNamingStrategy");
		properties.put(org.hibernate.cfg.Environment.IMPLICIT_NAMING_STRATEGY,
				SpringImplicitNamingStrategy.class.getName());
		
		if(hibernateDialect.contains("PostgreSQL")) {
			properties.put(org.hibernate.cfg.Environment.MAX_FETCH_DEPTH, 0);//Avoid JOINs which causes problems with generated jpa sentences with Entities subclasses from another one and where a same name field has different types
		}

		properties.put(org.hibernate.cfg.Environment.DIALECT, hibernateDialect);
		properties.put(org.hibernate.cfg.Environment.SHOW_SQL, hibernateShowSQL);
		properties.put(org.hibernate.cfg.Environment.FORMAT_SQL, hibernateFormatSQL);
		properties.put(org.hibernate.cfg.Environment.HBM2DDL_AUTO, hibernateDDLAutoMode);
		properties.put(org.hibernate.cfg.Environment.HBM2DLL_CREATE_NAMESPACES, false);
		
				

		emfBean.setJpaPropertyMap(properties);
		log.info("tenantEntityManagerFactory set up successfully!");
		return emfBean;
	}

	@ConfigurationProperties("spring.datasource.hikari")
	@Bean(DEFAULT_DS_BEAN_NAME)
	@Scope("prototype")
	public DataSource defaultDS() {
		return new HikariDataSource();
	}

}
