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
package com.minsait.onesait.platform.restplanner.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.scheduler.SchedulerType;
import com.minsait.onesait.platform.scheduler.job.BatchGenericExecutor;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RestPlannerJobExecutor implements BatchGenericExecutor {

	@Autowired
	RestPlannerJob restPlannerJob;

	private static final String RESTPLANNER_JOB_KEY = "Rest Planner";

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			if (context.getScheduler().getSchedulerName().equals(SchedulerType.RESTPLANNER.getSchedulerName())) {
				restPlannerJob.executeJob(context);
				log.debug("Rest Planner " + context.getJobDetail().getJobDataMap().getString("id") + " executed");
			}

		} catch (final Exception e) {
			log.error("Not executed" + e.getMessage(), e);
		}

	}

}
