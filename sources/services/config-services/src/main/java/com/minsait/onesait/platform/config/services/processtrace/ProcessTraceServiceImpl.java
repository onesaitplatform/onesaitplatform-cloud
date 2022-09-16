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
package com.minsait.onesait.platform.config.services.processtrace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.audit.bean.OPAuditEvent;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.EventType;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.Module;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.OperationType;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.ResultOperationType;
import com.minsait.onesait.platform.audit.bean.OPEventFactory;
import com.minsait.onesait.platform.audit.notify.EventRouter;
import com.minsait.onesait.platform.config.model.ProcessOperation;
import com.minsait.onesait.platform.config.model.ProcessTrace;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.ProcessTraceRepository;
import com.minsait.onesait.platform.config.services.exceptions.ProcessTraceServiceException;
import com.minsait.onesait.platform.config.services.processtrace.dto.OperationStatus;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProcessTraceServiceImpl implements ProcessTraceService {

	@Autowired
	private ProcessTraceRepository repository;

	@Autowired(required = false)
	@Qualifier("processExecutionMap")
	private Map<String, LinkedHashSet<OperationStatus>> processExecutionMap;

	@Autowired
	EventRouter eventRouter;

	private ObjectMapper mapper = new ObjectMapper();

	@Override
	public List<ProcessTrace> getProcessTraceByUser(User user, String identification, String description) {
		if (identification != null && identification.equals("")) {
			identification = null;
		}
		if (description != null && description.equals("")) {
			description = null;
		}
		if (identification == null && description == null) {
			if (user.isAdmin()) {
				return repository.findAll();
			} else {
				return repository.findByUser(user);
			}
		} else {

			if (identification != null && description != null) {

				if (user.isAdmin()) {
					return repository.findByIdentificationContainingAndDescriptionContaining(identification,
							description);
				} else {
					return repository.findByUserAndIdentificationContainingAndDescriptionContaining(user,
							identification, description);
				}
			} else if (identification != null) {
				if (user.isAdmin()) {
					return repository.findByIdentificationContaining(identification);
				} else {
					return repository.findByUserAndIdentificationContaining(user, identification);
				}
			} else {
				if (user.isAdmin()) {
					return repository.findByDescriptionContaining(description);
				} else {
					return repository.findByUserAndDescriptionContaining(user, description);
				}
			}
		}
	}

	@Override
	public ProcessTrace createProcessTrace(ProcessTrace processTrace) {
		if (repository.findByIdentification(processTrace.getIdentification()) != null) {
			throw new ProcessTraceServiceException(
					"ProcessTrace with identification: " + processTrace.getIdentification() + " already exists");
		}
		return repository.save(processTrace);
	}

	@Override
	public ProcessTrace getById(String id) {
		return repository.findById(id).get();
	}

	@Override
	public void updateProcessTrace(ProcessTrace processTrace) {
		repository.save(processTrace);
	}

	@Override
	public void deleteProcessTrace(ProcessTrace processTrace) {
		repository.delete(processTrace);
	}

	@Override
	public ProcessTrace getByIdentification(String identification) {
		return repository.findByIdentification(identification);
	}

	@Override
	public List<ProcessTrace> getAll() {
		return repository.findAll();
	}

	@Override
	public void checkProcessExecution(String processId)
			throws JsonGenerationException, JsonMappingException, IOException {
		log.debug("Checking Process Execution with id: {}", processId);
		ProcessTrace process = getById(processId);
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
							log.debug("Check Operation: {} --- Status: {} --- Message: {}", op.getId(), exOp.getIsOk(),
									exOp.getMessage());
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

		log.debug("Clear hazelcast map for process: {}", processId);
		processExecutionMap.put(processId, new LinkedHashSet<OperationStatus>());

	}

}
