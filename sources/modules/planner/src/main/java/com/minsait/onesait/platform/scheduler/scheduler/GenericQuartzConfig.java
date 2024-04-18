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
package com.minsait.onesait.platform.scheduler.scheduler;

import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import com.minsait.onesait.platform.scheduler.config.SchedulerConfig;

public abstract class GenericQuartzConfig {

	@Value("${quartz.driverDelegateClass}")
	private String driverDelegateClass;

	@Autowired
	@Qualifier("quartzDatasource")
	protected DataSource dataSource;

	@Autowired
	@Qualifier("quartzProperties")
	protected Properties quartzProperties;

	@Autowired
	@Qualifier("quartzPropertiesSingleThread")
	protected Properties quartzPropertiesSingleThread;

	@Autowired
	protected SchedulerConfig quartzDataSourceConfig;

	public boolean checksIfAutoStartup() {
		final List<String> schedulersToStartup = quartzDataSourceConfig.getAutoStartupSchedulers();
		return schedulersToStartup != null && schedulersToStartup.contains(getSchedulerBeanName());
	}

	public abstract String getSchedulerBeanName();

	public SchedulerFactoryBean getSchedulerFactoryBean(JobFactory jobFactory,
			PlatformTransactionManager transactionManager, Boolean singleThreaded) {

		final SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();

		schedulerFactoryBean.setTransactionManager(transactionManager);
		schedulerFactoryBean.setOverwriteExistingJobs(true);
		schedulerFactoryBean.setSchedulerName(getSchedulerBeanName());
		schedulerFactoryBean.setBeanName(getSchedulerBeanName());

		// custom job factory of spring with DI support for @Autowired!
		schedulerFactoryBean.setOverwriteExistingJobs(true);
		schedulerFactoryBean.setAutoStartup(checksIfAutoStartup());

		schedulerFactoryBean.setDataSource(dataSource);

		schedulerFactoryBean.setJobFactory(jobFactory);
		quartzProperties.setProperty("org.quartz.jobStore.driverDelegateClass", driverDelegateClass);
		if (singleThreaded) {
			schedulerFactoryBean.setQuartzProperties(quartzPropertiesSingleThread);
		} else {
			schedulerFactoryBean.setQuartzProperties(quartzProperties);
		}

		return schedulerFactoryBean;
	}

}
