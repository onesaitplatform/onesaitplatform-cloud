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
package com.minsait.onesait.platform.quartz.services.ontologyKPI;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.OntologyKPI;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.OntologyKPIRepository;
import com.minsait.onesait.platform.config.services.ontology.OntologyConfiguration;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;
import com.minsait.onesait.platform.scheduler.SchedulerType;
import com.minsait.onesait.platform.scheduler.scheduler.bean.TaskInfo;
import com.minsait.onesait.platform.scheduler.scheduler.bean.TaskOperation;
import com.minsait.onesait.platform.scheduler.scheduler.bean.response.ScheduleResponseInfo;
import com.minsait.onesait.platform.scheduler.scheduler.service.TaskService;

@Service
public class OntologyKPIServiceImpl implements OntologyKPIService {

	@Autowired
	private OntologyKPIRepository ontologyKPIRepository;
	
	@Autowired
	private TaskService taskService;

	@Override
	public void unscheduleKpi(OntologyKPI oKPI) {
		final String jobName = oKPI.getJobName();
		if (jobName != null && oKPI.isActive()) {
			final TaskOperation operation = new TaskOperation();
			operation.setJobName(jobName);
			if (taskService.unscheduled(operation)) {
				oKPI.setActive(false);
				oKPI.setJobName(null);
				ontologyKPIRepository.save(oKPI);
			}
		}
	}

	@Override
	public JsonNode completeSchema(String schema, String identification, String description) throws IOException {
		final JsonNode schemaSubTree = organizeRootNodeIfExist(schema);
		((ObjectNode) schemaSubTree).put("type", "object");
		((ObjectNode) schemaSubTree).put("description", "Info " + identification);

		((ObjectNode) schemaSubTree).put("$schema", SCHEMA_DRAFT_VERSION);
		((ObjectNode) schemaSubTree).put("title", identification);

		((ObjectNode) schemaSubTree).put("additionalProperties", true);
		return schemaSubTree;
	}

	@Override
	public JsonNode organizeRootNodeIfExist(String schema) throws IOException {

		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode schemaSubTree = mapper.readTree(schema);
		boolean find = Boolean.FALSE;
		for (final Iterator<Entry<String, JsonNode>> elements = schemaSubTree.fields(); elements.hasNext();) {
			final Entry<String, JsonNode> e = elements.next();
			if (e.getKey().equals("properties")) {
				e.getValue().fields();
				for (final Iterator<Entry<String, JsonNode>> properties = e.getValue().fields(); properties
						.hasNext();) {
					final Entry<String, JsonNode> prop = properties.next();
					final String field = prop.getKey();
					if (!field.toUpperCase().equals(field) && Character.isUpperCase(field.charAt(0))) {
						((ObjectNode) schemaSubTree).set("datos", prop.getValue());
						final String newString = "{\"type\": \"string\",\"$ref\": \"#/datos\"}";
						final JsonNode newNode = mapper.readTree(newString);
						prop.setValue(newNode);
						find = Boolean.TRUE;
						break;
					}
					if (find) {
						break;
					}
				}
			}
		}
		return schemaSubTree;
	}

	@Override
	public void scheduleKpi(OntologyKPI oKPI) {
		if (!oKPI.isActive()) {
			final TaskInfo task = new TaskInfo();
			task.setSchedulerType(SchedulerType.OKPI);

			final Map<String, Object> jobContext = new HashMap<>();
			jobContext.put("id", oKPI.getId());
			jobContext.put("ontology", oKPI.getOntology().getIdentification());
			jobContext.put("query", oKPI.getQuery());
			jobContext.put("userId", oKPI.getUser().getUserId());
			jobContext.put("postProcess", oKPI.getPostProcess());
			jobContext.put(Tenant2SchemaMapper.VERTICAL_SCHEMA_KEY_STRING,
					MultitenancyContextHolder.getVerticalSchema());
			jobContext.put(Tenant2SchemaMapper.TENANT_KEY_STRING, MultitenancyContextHolder.getTenantName());
			task.setJobName("Ontology KPI");
			task.setData(jobContext);
			if (oKPI.getDateFrom() != null) {
				task.setStartAt(oKPI.getDateFrom());
			}
			if (oKPI.getDateTo() != null) {
				task.setEndAt(oKPI.getDateTo());
			}
			task.setSingleton(false);
			task.setCronExpression(oKPI.getCron());
			task.setUsername(oKPI.getUser().getUserId());
			final ScheduleResponseInfo response = taskService.addJob(task);
			oKPI.setActive(response.isSuccess());
			oKPI.setJobName(response.getJobName());
			ontologyKPIRepository.save(oKPI);
		}
	}
	
	@Override
	public void cloneOntologyKpi(Ontology ontology, Ontology clonnedOntology, User user) {
			
			final List<OntologyKPI> kpis = ontologyKPIRepository.findByOntology(ontology);
			for(OntologyKPI ontologyKPIDTO : kpis) {
				final OntologyKPI oKPI = new OntologyKPI();
				oKPI.setCron(ontologyKPIDTO.getCron());
				oKPI.setDateFrom(ontologyKPIDTO.getDateFrom());
				oKPI.setDateTo(ontologyKPIDTO.getDateTo());
				if (ontologyKPIDTO.getDateTo() != null && ontologyKPIDTO.getDateFrom() == null) {
					final Date now = new Date();
					if (ontologyKPIDTO.getDateTo().before(now)) {
						final Calendar dateFrom = Calendar.getInstance();
						dateFrom.setTime(ontologyKPIDTO.getDateTo());
						dateFrom.add(Calendar.HOUR, -1);
						oKPI.setDateFrom(dateFrom.getTime());
					} else {
						oKPI.setDateFrom(now);
					}
				}
				if (ontologyKPIDTO.getDateTo() != null && ontologyKPIDTO.getDateFrom() != null
						&& ontologyKPIDTO.getDateTo().before(ontologyKPIDTO.getDateFrom())) {
					oKPI.setDateFrom(null);
					oKPI.setDateTo(null);
				}
				oKPI.setActive(Boolean.FALSE);
				oKPI.setOntology(clonnedOntology);
				oKPI.setQuery(ontologyKPIDTO.getQuery());
				oKPI.setUser(user);
				oKPI.setPostProcess(ontologyKPIDTO.getPostProcess());
				ontologyKPIRepository.save(oKPI);
				scheduleKpi(oKPI);
			}
				
		}
}
