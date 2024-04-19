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
package com.minsait.onesait.platform.scheduler.scheduler.generator.impl;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.scheduler.scheduler.generator.JobGenerator;

@Service
public class JobGeneratorImpl implements JobGenerator {

	@Override
	public JobDetail createJobDetail(JobKey jobKey, JobDataMap jobDataMap, Class<? extends Job> jobClass,
			String jobDescription) {

		return JobBuilder.newJob(jobClass).setJobData(jobDataMap).withDescription(jobDescription).storeDurably(true)
				.withIdentity(jobKey).build();
	}

}
