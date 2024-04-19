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
package com.minsait.onesait.platform.controlpanel.helper.processtrace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.audit.notify.EventRouter;
import com.minsait.onesait.platform.business.services.audit.AuditService;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.ProcessOperation;
import com.minsait.onesait.platform.config.model.ProcessOperation.Type;
import com.minsait.onesait.platform.config.model.ProcessTrace;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataUnauthorizedException;
import com.minsait.onesait.platform.config.services.processtrace.dto.OperationStatus;
import com.minsait.onesait.platform.config.services.processtrace.dto.ProcessOperationDTO;
import com.minsait.onesait.platform.config.services.processtrace.dto.ProcessTraceCreateDTO;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.controller.processtrace.ExecutionHistoric;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ProcessTraceHelper {

	public enum Units {
		SECONDS, MINUTES, HOURS, DAYS
	}

	@Autowired
	private UserService userService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	@Qualifier("processExecutionMap")
	private Map<String, LinkedHashSet<OperationStatus>> processExecutionMap;
	@Autowired
	EventRouter eventRouter;
	@Autowired
	private AuditService auditService;

	private ObjectMapper mapper = new ObjectMapper();

	public List<ExecutionHistoric> populateExecutionHistorics(String queryResult, ProcessTrace process)
			throws JsonProcessingException, IOException {
		List<ExecutionHistoric> executions = new ArrayList<>();

		JsonNode json = mapper.readTree(queryResult);

		if (isElasticOntology("Audit_" + process.getUser().getUserId()) && json.has(0) && json.get(0).has("total")
				&& json.get(0).get("total").get("value").asInt() == 0) {
			log.debug("There are not records on elastic for the ontology {}", "Audit_" + process.getUser().getUserId());
			return executions;
		}

		if (json.isArray()) {
			for (JsonNode jsonNode : json) {
				Long timeStamp = jsonNode.get("timeStamp").asLong();
				String resultOperation = jsonNode.get("resultOperation").asText();
				String extraData = jsonNode.get("extraData").asText();

				JsonNode failedJson = mapper.readTree(extraData);
				Iterator<Entry<String, JsonNode>> it = failedJson.getFields();
				List<OperationStatus> opsStatus = new ArrayList<>();
				while (it.hasNext()) {
					Entry<String, JsonNode> entry = it.next();
					JsonNode opStatus = entry.getValue();
					opsStatus.add(OperationStatus.builder().isOk(opStatus.get("status").asBoolean())
							.operationId(entry.getKey()).message(opStatus.get("message").asText()).build());
				}
				executions.add(ExecutionHistoric.builder().date(new Date(timeStamp)).status(resultOperation)
						.operationsStatus(opsStatus).build());
			}
		}
		executions.sort((e1, e2) -> e1.getDate().compareTo(e2.getDate()));
		return executions;
	}

	private boolean isElasticOntology(String ontologyId) {
		Ontology ontology = ontologyService.getOntologyByIdentification(ontologyId);
		if (ontology != null)
			return ontology.getRtdbDatasource().equals(RtdbDatasource.ELASTIC_SEARCH);
		return false;
	}

	public com.fasterxml.jackson.databind.JsonNode buildProperties(ProcessOperation op) {
		com.fasterxml.jackson.databind.node.ObjectNode properties = new com.fasterxml.jackson.databind.ObjectMapper()
				.createObjectNode();

		properties.put("Operation ID", op.getId());
		properties.put("Type", op.getType().name());
		properties.put("Entity", op.getOntologyId().getIdentification());
		properties.put("Source", op.getSources().replace(",", ", "));

		String query = "select extraData FROM Audit_" + op.getProcessTraceId().getUser().getUserId()
				+ " AS c where module='CONTROLPANEL' and operationType='PROCESS_EXECUTION' and id='"
				+ op.getProcessTraceId().getId() + "' and version=" + op.getProcessTraceId().getVersion()
				+ " order by timeStamp desc limit 1";
		try {
			String result = auditService.getCustomQueryData(query, op.getProcessTraceId().getUser().getUserId());
			JsonNode json = mapper.readTree(result);
			if (json.isArray() && json.size() > 0) {
				if (isElasticOntology("Audit_" + op.getProcessTraceId().getUser().getUserId()) && json.has(0)
						&& json.get(0).has("total") && json.get(0).get("total").get("value").asInt() == 0) {
					log.debug("There are not records on elastic for the ontology {}",
							"Audit_" + op.getProcessTraceId().getUser().getUserId());
				} else {
					JsonNode extraData = mapper.readTree(json.get(0).get("extraData").asText());
					Iterator<Entry<String, JsonNode>> it = extraData.getFields();
					while (it.hasNext()) {
						Entry<String, JsonNode> entry = it.next();
						JsonNode opStatus = entry.getValue();
						if (entry.getKey().equals(op.getId())) {
							properties.put("LastStatus", !opStatus.get("status").asBoolean() ? "Failed" : "Success");
							properties.put("LastMessage", opStatus.get("message").asText());
						}
					}
				}
			}
		} catch (GenericOPException | DBPersistenceException | OntologyDataUnauthorizedException | IOException e) {
			log.error("Error getting extraData from process {}", op.getProcessTraceId().getIdentification(), e);
		}

		return properties;

	}

	public ProcessTrace convertToProcessTrace(ProcessTraceCreateDTO dto, String[] operations, ProcessTrace process)
			throws JsonParseException, JsonMappingException, IOException {

		process.setDescription(dto.getDescription());
		process.setIdentification(dto.getIdentification());
		process.setIsActive(dto.getIsActive());
		process.setIsOrdered(dto.getIsOrdered());
		process.setUser(userService.getUser(utils.getUserId()));
		process.setCron(dto.getCron());
		process.setCheckExecutions(dto.getCheckExecutions());
		process.setDateFrom(dto.getDateFrom());
		process.setDateTo(dto.getDateTo());
		process.setIsFiltered(dto.getIsFiltered());
		if (dto.getDateTo() != null && dto.getDateFrom() == null) {
			final Date now = new Date();
			if (dto.getDateTo().before(now)) {
				final Calendar dateFrom = Calendar.getInstance();
				dateFrom.setTime(dto.getDateTo());
				dateFrom.add(Calendar.HOUR, -1);
				process.setDateFrom(dateFrom.getTime());
			} else {
				process.setDateFrom(now);
			}
		}
		if (dto.getDateTo() != null && dto.getDateFrom() != null && dto.getDateTo().before(dto.getDateFrom())) {
			process.setDateFrom(null);
			process.setDateTo(null);
		}

		List<ProcessOperationDTO> operationList = new ArrayList<>();
		for (int i = 0; i < operations.length; i++) {
			operationList.add(mapper.readValue(operations[i], ProcessOperationDTO.class));
		}

		if (process.getId() != null && !checkOperationsUpdated(operations, process)) {

			process.getOperations().clear();
			operationList.stream().map(x -> convertToProcessOperation(x, process))
					.forEach(x -> process.getOperations().add(x));
		} else if (process.getId() == null) {
			operationList.stream().map(x -> convertToProcessOperation(x, process))
					.forEach(x -> process.getOperations().add(x));
		}

		if (process.getId() != null && !checkOperationsUpdated(operations, process)) {
			process.setVersion(process.getVersion() + 1);
		}

		return process;
	}

	public ProcessOperation convertToProcessOperation(ProcessOperationDTO dto, ProcessTrace process) {

		ProcessOperation operation = new ProcessOperation();
		operation.setNumExecutions(Integer.parseInt(dto.getNumExecutions()));
		operation.setPosition(Integer.parseInt(dto.getPosition()));
		operation.setSources(dto.getSources());
		operation.setType(Type.valueOf(dto.getType().toUpperCase()));
		operation.setOntologyId(ontologyService.getOntologyByIdentification(dto.getOntology(), utils.getUserId()));
		operation.setProcessTraceId(process);
		operation.setFieldId(dto.getFieldId());
		operation.setFieldValue(dto.getFieldValue());

		return operation;
	}

	public boolean checkOperationsUpdated(String[] arrayOperations, ProcessTrace processDb)
			throws JsonParseException, JsonMappingException, IOException {
		List<ProcessOperationDTO> updatedOperations = new ArrayList<>();
		for (int i = 0; i < arrayOperations.length; i++) {
			updatedOperations.add(mapper.readValue(arrayOperations[i], ProcessOperationDTO.class));
		}
		List<ProcessOperationDTO> dbOperations = convertToProcessOperation(processDb.getOperations());

		if (processDb.getIsOrdered()) {
			updatedOperations.sort((o1, o2) -> o1.getPosition().compareTo(o2.getPosition()));
			dbOperations.sort((o1, o2) -> o1.getPosition().compareTo(o2.getPosition()));
			return dbOperations.equals(updatedOperations);
		} else {
			return new HashSet<>(updatedOperations).equals(new HashSet<>(dbOperations));
		}
	}

	public ProcessTraceCreateDTO convertToProcessTraceDTO(ProcessTrace process)
			throws JsonParseException, JsonMappingException, IOException {
		ProcessTraceCreateDTO dto = new ProcessTraceCreateDTO();
		dto.setDescription(process.getDescription());
		dto.setIdentification(process.getIdentification());
		dto.setIsActive(process.getIsActive());
		dto.setIsOrdered(process.getIsOrdered());
		dto.setId(process.getId());
		dto.setCheckExecutions(process.getCheckExecutions());
		dto.setIsFiltered(process.getIsFiltered());

		dto.setCron(process.getCron());

		return dto;
	}

	public List<ProcessOperationDTO> convertToProcessOperation(Set<ProcessOperation> operations) {

		List<ProcessOperationDTO> operationList = new ArrayList<>();
		for (ProcessOperation operation : operations) {

			ProcessOperationDTO dto = new ProcessOperationDTO();
			dto.setNumExecutions(operation.getNumExecutions().toString());
			dto.setPosition(operation.getPosition().toString());
			dto.setSources(operation.getSources());
			dto.setType(operation.getType().name().toLowerCase());
			dto.setOntology(operation.getOntologyId().getIdentification());
			dto.setId(operation.getId());
			dto.setFieldId(operation.getFieldId());
			dto.setFieldValue(operation.getFieldValue());

			operationList.add(dto);
		}

		return operationList;
	}
}
