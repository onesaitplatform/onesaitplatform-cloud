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
package com.minsait.onesait.platform.restplanner.scheduler.listener;

import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.scheduler.SchedulerType;
import com.minsait.onesait.platform.scheduler.scheduler.service.BatchSchedulerFactory;

import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RestPlannerSchedulerListenerConfig implements ApplicationListener<ContextRefreshedEvent> {

	@Autowired
	@Qualifier("restplanner-scheduler-factory")
	private SchedulerFactoryBean schedulerFactory;

	@Autowired
	private BatchSchedulerFactory batchSchedulerFactory;

	@Autowired
	private SchedulerListener restPlannerSchedulerListener;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {

		try {
			batchSchedulerFactory.getScheduler(SchedulerType.RESTPLANNER).getListenerManager()
					.addSchedulerListener(restPlannerSchedulerListener);
		} catch (SchedulerException | NotFoundException e) {
			log.error("Error on Rest Planner Scheduler Listener", e);
		}
		log.info("*******init scheduler listener*************");
		schedulerFactory.setSchedulerListeners(restPlannerSchedulerListener);
	}
}
