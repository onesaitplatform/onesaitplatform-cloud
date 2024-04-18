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
package com.minsait.onesait.platform.quartz.services.simulation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.config.model.ClientPlatformInstanceSimulation;
import com.minsait.onesait.platform.config.repository.ClientPlatformInstanceSimulationRepository;
import com.minsait.onesait.platform.config.services.exceptions.SimulationServiceException;
import com.minsait.onesait.platform.config.services.simulation.DeviceSimulationService;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;
import com.minsait.onesait.platform.scheduler.SchedulerType;
import com.minsait.onesait.platform.scheduler.scheduler.bean.TaskInfo;
import com.minsait.onesait.platform.scheduler.scheduler.bean.TaskOperation;
import com.minsait.onesait.platform.scheduler.scheduler.bean.response.ScheduleResponseInfo;
import com.minsait.onesait.platform.scheduler.scheduler.service.TaskService;

@Service
@Lazy
public class SimulationServiceImpl implements SimulationService {

	@Autowired
	private DeviceSimulationService deviceSimulationService;
	@Autowired
	private TaskService taskService;
	@Autowired
	private ClientPlatformInstanceSimulationRepository clientPlatformInstanceSimulationRepository;

	@Override
	public String getDeviceSimulationJson(String identification, String clientPlatform, String token, String ontology,
			String jsonMap, String jsonInstances, String instancesMode) throws IOException {

		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode rootNode = mapper.createObjectNode();

		((ObjectNode) rootNode).put("clientPlatform", clientPlatform);

		((ObjectNode) rootNode).put("clientPlatformInstance", identification);

		((ObjectNode) rootNode).put("token", token);
		((ObjectNode) rootNode).put("ontology", ontology);

		if (StringUtils.hasText(jsonInstances)) {
			((ObjectNode) rootNode).set("instances", mapper.readTree(jsonInstances));
			((ObjectNode) rootNode).put("instancesMode", instancesMode);
		}
		if (StringUtils.hasText(jsonMap))
			((ObjectNode) rootNode).set("fields", mapper.readTree(jsonMap));
		return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
	}

	@Override
	public void createSimulation(String identification, int interval, String userId, String json) throws IOException {

		if (clientPlatformInstanceSimulationRepository.findByIdentification(identification) != null) {
			throw new SimulationServiceException("Simulation with identification: " + identification + " exists");
		}
		final ClientPlatformInstanceSimulation simulation = deviceSimulationService.createSimulation(identification,
				interval, userId, json);

		scheduleSimulation(simulation);
	}

	@Override
	public void unscheduleSimulation(ClientPlatformInstanceSimulation deviceSimulation) {
		final String jobName = deviceSimulation.getJobName();
		if (jobName != null && deviceSimulation.isActive()) {
			final TaskOperation operation = new TaskOperation();
			operation.setJobName(jobName);
			taskService.unscheduled(operation);
			deviceSimulation.setActive(false);
			deviceSimulation.setJobName(null);
			deviceSimulationService.save(deviceSimulation);
		}

	}

	@Override
	public void scheduleSimulation(ClientPlatformInstanceSimulation deviceSimulation) {

		if (!deviceSimulation.isActive()) {
			final TaskInfo task = new TaskInfo();
			task.setSchedulerType(SchedulerType.SIMULATION);

			final Map<String, Object> jobContext = new HashMap<>();
			jobContext.put("id", deviceSimulation.getId());
			jobContext.put("json", deviceSimulation.getJson());
			jobContext.put("userId", deviceSimulation.getUser().getUserId());
			jobContext.put(Tenant2SchemaMapper.VERTICAL_SCHEMA_KEY_STRING,
					MultitenancyContextHolder.getVerticalSchema());
			jobContext.put(Tenant2SchemaMapper.TENANT_KEY_STRING, MultitenancyContextHolder.getTenantName());
			task.setJobName("Device Simulation");
			task.setData(jobContext);
			task.setSingleton(false);
			task.setCronExpression(deviceSimulation.getCron());
			task.setUsername(deviceSimulation.getUser().getUserId());
			final ScheduleResponseInfo response = taskService.addJob(task);
			deviceSimulation.setActive(true);
			deviceSimulation.setJobName(response.getJobName());
			deviceSimulationService.save(deviceSimulation);

		}

	}

	@Override
	public void updateSimulation(String identification, int interval, String json,
			ClientPlatformInstanceSimulation simulation) throws IOException {
		deviceSimulationService.updateSimulation(identification, interval, json, simulation);

	}

}
