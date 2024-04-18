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
package com.minsait.onesait.platform.scheduler.scheduler.instance;

import static com.minsait.onesait.platform.scheduler.PropertyNames.SCHEDULER_PROPERTIES_LOCATION;

import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import com.minsait.onesait.platform.scheduler.scheduler.BatchScheduler;
import com.minsait.onesait.platform.scheduler.scheduler.GenericBatchScheduler;
import com.minsait.onesait.platform.scheduler.scheduler.GenericQuartzConfig;

@Configuration
@ConditionalOnResource(resources = SCHEDULER_PROPERTIES_LOCATION)
public class ProcessExecutionQuartzConfig extends GenericQuartzConfig {

	private static final String SCHEDULER_BEAN_FACTORY_NAME = "processexecution-scheduler-factory";

	@Bean(SCHEDULER_BEAN_FACTORY_NAME)
	public SchedulerFactoryBean processExecutionSchedulerFactoryBean(JobFactory jobFactory,
			PlatformTransactionManager transactionManager) {
		return getSchedulerFactoryBean(jobFactory, transactionManager, false);
	}

	@Bean(SchedulerNames.PROCESS_EXECUTION_NAME)
	public BatchScheduler processExecutionScheduler(
			@Autowired @Qualifier(SCHEDULER_BEAN_FACTORY_NAME) SchedulerFactoryBean schedulerFactoryBean) {
		return new GenericBatchScheduler(schedulerFactoryBean.getScheduler(), getSchedulerBeanName());
	}

	@Override
	public String getSchedulerBeanName() {
		return SchedulerNames.PROCESS_EXECUTION_NAME;
	}

}
