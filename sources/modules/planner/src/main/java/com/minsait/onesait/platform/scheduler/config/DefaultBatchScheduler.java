/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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

import org.quartz.SchedulerException;
import org.quartz.impl.DirectSchedulerFactory;
import org.quartz.simpl.RAMJobStore;
import org.quartz.simpl.ZeroSizeThreadPool;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.minsait.onesait.platform.scheduler.scheduler.BatchScheduler;
import com.minsait.onesait.platform.scheduler.scheduler.GenericBatchScheduler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class DefaultBatchScheduler  {
	
	
	@Bean("defaultScheduler")
	@ConditionalOnMissingBean(BatchScheduler.class)	
	public BatchScheduler getDefaultBatchScheduler () {		
		
		
		try {
			ZeroSizeThreadPool threadPool = new ZeroSizeThreadPool();
			
			RAMJobStore jobStore = new RAMJobStore();
			DirectSchedulerFactory.getInstance().createScheduler(threadPool, jobStore);
			return new GenericBatchScheduler(DirectSchedulerFactory.getInstance().getScheduler(), "defaultScheduler");
		} catch (SchedulerException e) {
			log.error("Error init default scheduler", e);
		}
		return null;
		
		
	}

}
