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
package com.minsait.onesait.platform.example.scheduling.config.scheduler;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.scheduler.SchedulerType;
import com.minsait.onesait.platform.scheduler.job.JobParamNames;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AnotherExampleJob {

	public void execute(JobExecutionContext context) {

		JobDataMap data = context.getMergedJobDataMap();
		String username = data.getString(JobParamNames.USERNAME);
		SchedulerType schedulerName = (SchedulerType) data.get(JobParamNames.SCHEDULER_TYPE);
		log.info("executing job test" + username + " " + schedulerName);
	}

}
