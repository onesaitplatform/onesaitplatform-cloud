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
package com.minsait.onesait.platform.router.processtrace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.jayway.jsonpath.JsonPath;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.EventType;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.Module;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.OperationType;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.ResultOperationType;
import com.minsait.onesait.platform.audit.bean.OPEventFactory;
import com.minsait.onesait.platform.audit.notify.EventRouter;
import com.minsait.onesait.platform.config.model.ProcessOperation;
import com.minsait.onesait.platform.config.model.ProcessTrace;
import com.minsait.onesait.platform.config.repository.ProcessOperationRepository;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.processtrace.ProcessTraceService;
import com.minsait.onesait.platform.config.services.processtrace.dto.OperationStatus;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.service.ProcessExecutionService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProcessExecutionServiceImpl implements ProcessExecutionService {

	@Autowired
	@Qualifier("processExecutionMap")
	private Map<String, LinkedHashSet<OperationStatus>> processExecutionMap;

	@Autowired
	private ProcessOperationRepository processOperationRepo;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private ProcessTraceService processTraceService;
	@Autowired
	private EventRouter eventRouter;

	private ObjectMapper mapper = new ObjectMapper();

	@Override
	public void checkOperation(OperationModel operation, OperationResultModel result, Integer count) {
		if (operation.getOntologyName() != null) {
			List<ProcessOperation> operations = processOperationRepo
					.findByOntologyId(ontologyService.getOntologyByIdentification(operation.getOntologyName()));
			if (log.isDebugEnabled()) {
				log.debug("Checking if exist a process with operations on the ontology {}", operation.getOntologyName());
			}
			operations.forEach(op -> {
				if (op.getProcessTraceId().getIsActive()) {
					if (log.isDebugEnabled()) {
						log.debug("TypeOperation: {} --- Source Operation: {}", op.getType().name(), op.getSources());
					}
					List<String> sourceList = new ArrayList<>(Arrays.asList(op.getSources().split(",")));
					if (op.getType().name().equals(operation.getOperationType().name())
							&& (sourceList.contains(operation.getSource().name().toLowerCase())
									|| sourceList.contains("all"))) {
						Boolean isOk = true;
						if (op.getProcessTraceId().getIsFiltered()) {
							isOk = checkFilteredField(operation, op);
						}

						if (isOk) {
							LinkedHashSet<OperationStatus> OpStatusList = processExecutionMap
									.containsKey(op.getProcessTraceId().getId())
											? processExecutionMap.get(op.getProcessTraceId().getId())
											: new LinkedHashSet<>();
							OperationStatus opStatus = OpStatusList.stream()
									.filter(o -> o.getOperationId().equals(op.getId())).findFirst().orElse(null);
							if (log.isDebugEnabled()) {
								log.debug("Operation in process: {}", op.getProcessTraceId().getIdentification());
							}

							if (opStatus == null) {
								opStatus = new OperationStatus();
								opStatus.setOperationId(op.getId());
								if (op.getProcessTraceId().getCheckExecutions()) {
									opStatus.setNumExecutions(count);
								}
								opStatus.setIsOk(result.isStatus());
								opStatus.setMessage(result.getMessage());
								if (log.isDebugEnabled()) {
									log.debug("Execution success for operation {} in process {}", op.getId(),
										op.getProcessTraceId().getIdentification());
								}

							} else {
								OpStatusList.remove(opStatus);
								if (op.getProcessTraceId().getCheckExecutions()) {
									opStatus.setNumExecutions(opStatus.getNumExecutions() + count);
								}
								opStatus.setIsOk(result.isStatus());
								opStatus.setMessage(result.getMessage());
								if (log.isDebugEnabled()) {
									log.debug("Execution success for operation {} in process {}", op.getId(),
										op.getProcessTraceId().getIdentification());
								}
							}

							OpStatusList.add(opStatus);
							processExecutionMap.put(op.getProcessTraceId().getId(), OpStatusList);
						}

					}
				}
			});
		}
	}

	private Boolean checkFilteredField(OperationModel operation, ProcessOperation op) {
		String data = operation.getBody();
		try {
			JsonNode jsonData = mapper.readTree(data);
			String root = getRootField(op.getOntologyId().getJsonSchema());
			if (jsonData.isArray()) {
				for (JsonNode d : jsonData) {
					String fieldValue = JsonPath.parse(mapper.writeValueAsString(d))
							.read("$." + root + "." + op.getFieldId());
					if (!fieldValue.equals(op.getFieldValue())) {
						return false;
					}
				}
			} else {
				String fieldValue = JsonPath.parse(data).read("$." + root + "." + op.getFieldId());
				if (!fieldValue.equals(op.getFieldValue())) {
					return false;
				}
			}
		} catch (IOException e) {
			log.error("Error parsing Json Data {}.", data, e);
			return false;
		}
		return true;
	}

	private String getRootField(String schema) {
		try {
			JSONObject jsonschema = new JSONObject(schema);
			Iterator<String> iterator = jsonschema.keys();
			String root = null;
			while (iterator.hasNext()) {
				String prop = iterator.next();
				try {
					Iterator<String> iteratorAux = jsonschema.getJSONObject(prop).keys();
					while (iteratorAux.hasNext()) {
						String p = iteratorAux.next();
						if (jsonschema.getJSONObject(prop).getJSONObject(p).has("$ref")) {
							root = p;
							break;
						}
					}
				} catch (Exception e) {
				}
			}

			return root;
		} catch (JSONException e) {
			return null;
		}
	}

	@Override
	public void checkProcessExecution(String processId)
			throws JsonGenerationException, JsonMappingException, IOException {
		if (log.isDebugEnabled()) {
			log.debug("Checking Process Execution with id: {}", processId);
		}
		ProcessTrace process = processTraceService.getById(processId);
		if (process.getIsActive()) {
			Integer numOpSuccess = 0;
			LinkedHashSet<OperationStatus> executedOps = processExecutionMap.get(process.getId());
			List<ProcessOperation> operations = process.getOperations().stream().collect(Collectors.toList());

			Map<String, Object> extraData = new HashMap<>();
			OPAuditEvent auditEvent = new OPAuditEvent();
			auditEvent = OPEventFactory.builder().build().createAuditEvent(auditEvent, EventType.PROCESS_EXECUTION,
					null);
			auditEvent.setOperationType(OperationType.PROCESS_EXECUTION.name());
			auditEvent.setUser(process.getUser().getUserId());
			auditEvent.setModule(Module.CONTROLPANEL);
			auditEvent.setOntology("Audit_" + process.getUser().getUserId());
			auditEvent.setOtherType("ProcessTrace");
			auditEvent.setId(process.getId());
			auditEvent.setVersion(process.getVersion());

			if (process.getIsOrdered()) {

				operations.sort((o1, o2) -> o1.getPosition().compareTo(o2.getPosition()));
				List<OperationStatus> executedOpsList = new ArrayList<>(executedOps);

				Boolean success = true;
				if (executedOpsList.isEmpty()) {
					operations.stream().forEach(op -> {
						ObjectNode obj = mapper.createObjectNode();
						log.debug("Operation KO");
						obj.put("status", false);
						obj.put("message", "Operation not executed.");
						extraData.put(op.getId(), obj);
					});
					success = false;
				} else {
					for (int i = 0; i < operations.size(); i++) {
						ProcessOperation op = operations.get(i);
						ObjectNode obj = mapper.createObjectNode();
						try {
							OperationStatus exOp = executedOpsList.get(i);
							if (log.isDebugEnabled()) {
								log.debug("Check Operation: {} --- Status: {} --- Message: {}", op.getId(), exOp.getIsOk(),
									exOp.getMessage());
							}
							
							if (!exOp.getOperationId().equals(op.getId())) {
								log.debug("Operation KO");
								success = false;
								obj.put("status", false);
								obj.put("message", "Operation not executed.");
								extraData.put(op.getId(), obj);
							} else if (success && exOp.getOperationId().equals(op.getId()) && exOp.getIsOk()
									&& ((op.getProcessTraceId().getCheckExecutions()
											&& exOp.getNumExecutions().equals(op.getNumExecutions()))
											|| !op.getProcessTraceId().getCheckExecutions())) {
								log.debug("Operation OK");
								numOpSuccess++;
								obj.put("status", true);
								obj.put("message", "Operation OK");
								extraData.put(op.getId(), obj);
							} else if (exOp.getOperationId().equals(op.getId())
									&& op.getProcessTraceId().getCheckExecutions()
									&& !exOp.getNumExecutions().equals(op.getNumExecutions())) {
								log.debug("Operation KO");
								success = false;
								obj.put("status", false);
								obj.put("message", "Numer of executions failed.");
								extraData.put(op.getId(), obj);
								numOpSuccess++;
							} else {
								log.debug("Operation KO");
								success = false;
								obj.put("status", false);
								obj.put("message", exOp.getMessage());
								extraData.put(op.getId(), obj);
							}
						} catch (IndexOutOfBoundsException e) {
							log.debug("Operation KO");
							obj.put("status", false);
							obj.put("message", "Operation not executed.");
							extraData.put(op.getId(), obj);
						}

					}
				}

				if (!success) {
					auditEvent.setMessage(
							"Process with identification " + process.getIdentification() + " executed failed.");
					auditEvent.setResultOperation(ResultOperationType.ERROR);
				} else {
					auditEvent.setMessage(
							"Process with identification " + process.getIdentification() + " executed successfuly.");
					auditEvent.setResultOperation(ResultOperationType.SUCCESS);
				}
			} else {

				for (ProcessOperation op : operations) {
					ObjectNode obj = mapper.createObjectNode();
					if (executedOps.stream().filter(exOp -> exOp.getOperationId().equals(op.getId())).findFirst()
							.isPresent()) {
						OperationStatus exOp = executedOps.stream()
								.filter(exOpAux -> exOpAux.getOperationId().equals(op.getId())).findFirst().get();
						if (exOp.getIsOk() && ((op.getProcessTraceId().getCheckExecutions()
								&& exOp.getNumExecutions().equals(op.getNumExecutions()))
								|| !op.getProcessTraceId().getCheckExecutions())) {
							log.debug("Operation OK");
							obj.put("status", true);
							obj.put("message", "Operation OK");
							extraData.put(op.getId(), obj);
							numOpSuccess++;
						} else if (op.getProcessTraceId().getCheckExecutions()
								&& !exOp.getNumExecutions().equals(op.getNumExecutions())) {
							log.debug("Operation KO");
							obj.put("status", false);
							obj.put("message", "Numer of executions failed.");
							extraData.put(op.getId(), obj);
							numOpSuccess++;
						} else {
							log.debug("Operation KO");
							obj.put("status", false);
							obj.put("message", exOp.getMessage());
							extraData.put(op.getId(), obj);
						}
					} else {
						log.debug("Operation KO");
						obj.put("status", false);
						obj.put("message", "Operation not executed.");
						extraData.put(op.getId(), obj);
					}
				}
				if (operations.size() == numOpSuccess) {
					log.debug("Process Execution OK");
					auditEvent.setMessage(
							"Process with identification " + process.getIdentification() + " executed successfuly.");
					auditEvent.setResultOperation(ResultOperationType.SUCCESS);
				} else {
					log.debug("Process Execution KO");
					auditEvent.setMessage(
							"Process with identification " + process.getIdentification() + " executed failed.");
					auditEvent.setResultOperation(ResultOperationType.ERROR);
				}
			}

			auditEvent.setExtraData(extraData);
			eventRouter.notify(new ObjectMapper().writeValueAsString(auditEvent));
		}
		if (log.isDebugEnabled()) {
			log.debug("Clear hazelcast map for process: {}", processId);
		}
		processExecutionMap.put(processId, new LinkedHashSet<OperationStatus>());

	}

}
