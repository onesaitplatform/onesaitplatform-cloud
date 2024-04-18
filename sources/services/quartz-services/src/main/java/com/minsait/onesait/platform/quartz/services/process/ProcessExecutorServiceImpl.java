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
package com.minsait.onesait.platform.quartz.services.process;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.ProcessTrace;
import com.minsait.onesait.platform.config.repository.ProcessTraceRepository;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;
import com.minsait.onesait.platform.scheduler.SchedulerType;
import com.minsait.onesait.platform.scheduler.scheduler.bean.TaskInfo;
import com.minsait.onesait.platform.scheduler.scheduler.bean.TaskOperation;
import com.minsait.onesait.platform.scheduler.scheduler.bean.response.ScheduleResponseInfo;
import com.minsait.onesait.platform.scheduler.scheduler.service.TaskService;

@Service
public class ProcessExecutorServiceImpl implements ProcessExecutorService {

	@Autowired
	private ProcessTraceRepository processTraceRepository;

	@Autowired
	private TaskService taskService;

	@Override
	public void unscheduleProcess(ProcessTrace process) {
		final String jobName = process.getJobName();
		if (jobName != null && process.getIsActive()) {
			final TaskOperation operation = new TaskOperation();
			operation.setJobName(jobName);
			if (taskService.unscheduled(operation)) {
				process.setIsActive(false);
				process.setJobName(null);
				processTraceRepository.save(process);
			}
		}
	}

	@Override
	public void scheduleProcess(ProcessTrace process) {

		final TaskInfo task = new TaskInfo();
		task.setSchedulerType(SchedulerType.PROCESSEXECUTION);

		final Map<String, Object> jobContext = new HashMap<>();
		jobContext.put("processId", process.getId());
		jobContext.put(Tenant2SchemaMapper.VERTICAL_SCHEMA_KEY_STRING, MultitenancyContextHolder.getVerticalSchema());
		jobContext.put(Tenant2SchemaMapper.TENANT_KEY_STRING, MultitenancyContextHolder.getTenantName());
		task.setJobName("ProcessExecution");
		task.setData(jobContext);
		if (process.getDateFrom() != null) {
			task.setStartAt(process.getDateFrom());
		}
		if (process.getDateTo() != null) {
			task.setEndAt(process.getDateTo());
		}
		task.setSingleton(false);
		task.setCronExpression(process.getCron());
		task.setUsername(process.getUser().getUserId());
		final ScheduleResponseInfo response = taskService.addJob(task);
		process.setIsActive(response.isSuccess());
		process.setJobName(response.getJobName());
		processTraceRepository.save(process);

	}

}
