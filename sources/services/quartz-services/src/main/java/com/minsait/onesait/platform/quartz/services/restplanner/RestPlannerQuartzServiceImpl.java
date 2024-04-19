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
package com.minsait.onesait.platform.quartz.services.restplanner;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.RestPlanner;
import com.minsait.onesait.platform.config.repository.RestPlannerRepository;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;
import com.minsait.onesait.platform.scheduler.SchedulerType;
import com.minsait.onesait.platform.scheduler.scheduler.bean.TaskInfo;
import com.minsait.onesait.platform.scheduler.scheduler.bean.TaskOperation;
import com.minsait.onesait.platform.scheduler.scheduler.bean.response.ScheduleResponseInfo;
import com.minsait.onesait.platform.scheduler.scheduler.service.TaskService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RestPlannerQuartzServiceImpl implements RestPlannerQuartzService {

	@Autowired
	private RestPlannerRepository restPlannerRepository;
	@Autowired
	private TaskService taskService;

	@Override
	public void schedule(RestPlanner restPlanner, String url) {
		if (!restPlanner.isActive()) {
			final TaskInfo task = new TaskInfo();
			task.setSchedulerType(SchedulerType.RESTPLANNER);

			final Map<String, Object> jobContext = new HashMap<>();
			jobContext.put("identification", restPlanner.getIdentification());
			jobContext.put("id", restPlanner.getId());
			jobContext.put("url", url);
			jobContext.put("method", restPlanner.getMethod());
			jobContext.put("headers", restPlanner.getHeaders());
			jobContext.put("body", restPlanner.getBody());
			jobContext.put("userId", restPlanner.getUser().getUserId());
			jobContext.put(Tenant2SchemaMapper.VERTICAL_SCHEMA_KEY_STRING,
					MultitenancyContextHolder.getVerticalSchema());
			jobContext.put(Tenant2SchemaMapper.TENANT_KEY_STRING, MultitenancyContextHolder.getTenantName());
			task.setJobName("Rest Planner");
			task.setData(jobContext);
			task.setSingleton(false);
			task.setCronExpression(restPlanner.getCron());
			if (restPlanner.getDateFrom() != null) {
				task.setStartAt(restPlanner.getDateFrom());
			}
			if (restPlanner.getDateTo() != null) {
				task.setEndAt(restPlanner.getDateTo());
			}
			task.setUsername(restPlanner.getUser().getUserId());
			final ScheduleResponseInfo response = taskService.addJob(task);
			restPlanner.setActive(response.isSuccess());
			restPlanner.setJobName(response.getJobName());
			restPlannerRepository.save(restPlanner);
		}
	}

	@Override
	public void unschedule(RestPlanner restPlanner) {
		final String jobName = restPlanner.getJobName();
		if (jobName != null && restPlanner.isActive()) {
			final TaskOperation operation = new TaskOperation();
			operation.setJobName(jobName);
			if (taskService.unscheduled(operation)) {
				restPlanner.setActive(false);
				restPlanner.setJobName(null);
				restPlannerRepository.save(restPlanner);
			}
		}
	}

}
