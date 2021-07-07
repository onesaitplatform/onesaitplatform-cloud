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
package com.minsait.onesait.platform.flowengine.audit.aop;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.audit.aop.BaseAspect;
import com.minsait.onesait.platform.audit.bean.OPAuditError;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.OperationType;
import com.minsait.onesait.platform.commons.flow.engine.dto.FlowEngineDomain;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.FlowEngineInvokeRestApiOperationRequest;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.MailRestDTO;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.NotebookInvokeDTO;
import com.minsait.onesait.platform.flowengine.audit.bean.FlowEngineAuditEvent;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Order
@Component
@Slf4j
public class FlowEngineAuditableAspect extends BaseAspect {

	private static final String EXECUTE_FLOWENGINE_AUDITABLE = "execute aspect flowengineAuditable method doRecoveryActions";
	private static final String ERROR_AUDIT_APIMANAGER = "error auditing apimanager doRecoveryActions";
	private static final String EXCEPTION_DETECTED = "Exception Detected while executing ";

	@Autowired
	private FlowEngineAuditProcessor flowEngineAuditProcessor;

	@AfterReturning(returning = "retVal", pointcut = "@annotation(auditable) && args(domain,..)"
			+ " && execution (* com.minsait.onesait.platform.flowengine.nodered.communication.NodeRedAdminClientImpl.startFlowEngineDomain(..))")
	public void processStartFlowEngineDomain(JoinPoint joinPoint, FlowEngineAuditable auditable,
			FlowEngineDomain domain, String retVal) {

		final Method method = getMethod(joinPoint);
		final FlowEngineAuditEvent event = flowEngineAuditProcessor.getEvent(method.getName(), retVal, domain,
				OperationType.START);
		eventProducer.publish(event);

	}

	@AfterReturning(returning = "retVal", pointcut = "@annotation(auditable) && args(domain,..)"
			+ " && execution (* com.minsait.onesait.platform.flowengine.nodered.communication.NodeRedAdminClientImpl.createFlowengineDomain(..))")
	public void processCreateFlowEngineDomain(JoinPoint joinPoint, FlowEngineAuditable auditable,
			FlowEngineDomain domain, String retVal) {

		final Method method = getMethod(joinPoint);
		final FlowEngineAuditEvent event = flowEngineAuditProcessor.getEvent(method.getName(), retVal, domain,
				OperationType.INSERT);
		eventProducer.publish(event);

	}

	@AfterReturning(pointcut = "@annotation(auditable) && args(domainId,..)"
			+ " && execution (* com.minsait.onesait.platform.flowengine.nodered.communication.NodeRedAdminClientImpl.stopFlowEngineDomain(..))")
	public void processStopFlowEngine(JoinPoint joinPoint, FlowEngineAuditable auditable, String domainId) {

		final Method method = getMethod(joinPoint);
		final FlowEngineAuditEvent event = flowEngineAuditProcessor.getEvent(method.getName(), domainId,
				OperationType.STOP);
		eventProducer.publish(event);

	}

	@AfterReturning(pointcut = "@annotation(auditable) && args(domainId,..)"
			+ " && execution (* com.minsait.onesait.platform.flowengine.nodered.communication.NodeRedAdminClientImpl.deleteFlowEngineDomain(..))")
	public void processDeleteFlowEngine(JoinPoint joinPoint, FlowEngineAuditable auditable, String domainId) {

		final Method method = getMethod(joinPoint);
		final FlowEngineAuditEvent event = flowEngineAuditProcessor.getEvent(method.getName(), domainId,
				OperationType.DELETE);
		eventProducer.publish(event);

	}

	@AfterReturning(returning = "retVal", pointcut = "@annotation(auditable) && args(ontologyIdentificator, queryType, query, domainName,..)"
			+ " && execution (* com.minsait.onesait.platform.flowengine.api.rest.service.impl.FlowEngineNodeServiceImpl.submitQuery(..))")
	public void processSubmitQuery(JoinPoint joinPoint, FlowEngineAuditable auditable, String ontologyIdentificator,
			String queryType, String query, String domainName, String retVal) {

		final FlowEngineAuditEvent event = flowEngineAuditProcessor.getQueryEvent(ontologyIdentificator, query,
				queryType, retVal, domainName);

		eventProducer.publish(event);
	}

	@AfterReturning(returning = "retVal", pointcut = "@annotation(auditable) && args(ontology, data, domainName,..)"
			+ " && execution (* com.minsait.onesait.platform.flowengine.api.rest.service.impl.FlowEngineNodeServiceImpl.submitInsert(..))")
	public void processInsert(JoinPoint joinPoint, FlowEngineAuditable auditable, String ontology, String data,
			String domainName, String retVal) {

		final FlowEngineAuditEvent event = flowEngineAuditProcessor.getInsertEvent(ontology, data, retVal, domainName);

		eventProducer.publish(event);
	}

	@AfterReturning(returning = "retVal", pointcut = "@annotation(auditable) && args(notebookInvocationData,..)"
			+ " && execution (* com.minsait.onesait.platform.flowengine.api.rest.service.impl.FlowEngineNodeServiceImpl.invokeNotebook(..))")
	public void processInvokeNotebook(JoinPoint joinPoint, FlowEngineAuditable auditable,
			NotebookInvokeDTO notebookInvocationData, ResponseEntity<String> retVal) {

		final FlowEngineAuditEvent event = flowEngineAuditProcessor.processInvokeNotebook(notebookInvocationData,
				retVal);

		eventProducer.publish(event);
	}

	@AfterReturning(returning = "retVal", pointcut = "@annotation(auditable) && args(invokeRequest,..)"
			+ " && execution (* com.minsait.onesait.platform.flowengine.api.rest.service.impl.FlowEngineNodeServiceImpl.invokeRestApiOperation(..))")
	public void processInvokeAPI(JoinPoint joinPoint, FlowEngineAuditable auditable,
			FlowEngineInvokeRestApiOperationRequest invokeRequest, ResponseEntity<String> retVal) {

		final FlowEngineAuditEvent event = flowEngineAuditProcessor.processInvokeAPI(invokeRequest, retVal);

		eventProducer.publish(event);
	}

	@AfterReturning(returning = "retVal", pointcut = "@annotation(auditable) && args(domainName,pipelineIdentification,..)"
			+ " && execution (* com.minsait.onesait.platform.flowengine.api.rest.service.impl.FlowEngineNodeServiceImpl.getPipelineStatus(..))")
	public void processCheckDataflowStatus(JoinPoint joinPoint, FlowEngineAuditable auditable, String domainName,
			String pipelineIdentification, ResponseEntity<String> retVal) {

		final FlowEngineAuditEvent event = flowEngineAuditProcessor.processCheckDataflowStatus(domainName,
				pipelineIdentification, retVal);

		eventProducer.publish(event);
	}

	@AfterReturning(returning = "retVal", pointcut = "@annotation(auditable) && args(domainName,pipelineIdentification,..)"
			+ " && execution (* com.minsait.onesait.platform.flowengine.api.rest.service.impl.FlowEngineNodeServiceImpl.stopDataflow(..))")
	public void processStopDataflow(JoinPoint joinPoint, FlowEngineAuditable auditable, String domainName,
			String pipelineIdentification, ResponseEntity<String> retVal) {

		final FlowEngineAuditEvent event = flowEngineAuditProcessor.processStopDataflow(domainName,
				pipelineIdentification, retVal);

		eventProducer.publish(event);
	}

	@AfterReturning(returning = "retVal", pointcut = "@annotation(auditable) && args(domainName,pipelineIdentification,..)"
			+ " && execution (* com.minsait.onesait.platform.flowengine.api.rest.service.impl.FlowEngineNodeServiceImpl.startDataflow(..))")
	public void processStartDataflow(JoinPoint joinPoint, FlowEngineAuditable auditable, String domainName,
			String pipelineIdentification, ResponseEntity<String> retVal) {

		final FlowEngineAuditEvent event = flowEngineAuditProcessor.processStartDataflow(domainName,
				pipelineIdentification, retVal);

		eventProducer.publish(event);
	}

	@AfterReturning(pointcut = "@annotation(auditable) && args(mail,..)"
			+ " && execution (* com.minsait.onesait.platform.flowengine.api.rest.service.impl.FlowEngineNodeServiceImpl.sendMail(..))")
	public void processSendMail(JoinPoint joinPoint, FlowEngineAuditable auditable, MailRestDTO mail) {

		final FlowEngineAuditEvent event = flowEngineAuditProcessor.processSendMail(mail);

		eventProducer.publish(event);
	}

	@AfterThrowing(pointcut = "@annotation(auditable) && args(domain,..)"
			+ " && (execution (* com.minsait.onesait.platform.flowengine.nodered.communication.NodeRedAdminClientImpl.startFlowEngineDomain(..))"
			+ " || execution (* com.minsait.onesait.platform.flowengine.nodered.communication.NodeRedAdminClientImpl.createFlowengineDomain(..)))", throwing = "ex")
	public void doRecoveryActions(JoinPoint joinPoint, Exception ex, FlowEngineAuditable auditable,
			FlowEngineDomain domain) {

		log.debug(EXECUTE_FLOWENGINE_AUDITABLE);

		try {
			final Method method = getMethod(joinPoint);
			final OPAuditError event = flowEngineAuditProcessor.getErrorEvent(method.getName(), domain, ex);
			eventProducer.publish(event);
		} catch (Exception e) {
			log.error(ERROR_AUDIT_APIMANAGER, e);
		}
	}

	@AfterThrowing(pointcut = "@annotation(auditable) && args(domain,..)"
			+ " && (execution (* com.minsait.onesait.platform.flowengine.nodered.communication.NodeRedAdminClientImpl.stopFlowEngineDomain(..))"
			+ " || execution (* com.minsait.onesait.platform.flowengine.nodered.communication.NodeRedAdminClientImpl.deleteFlowEngineDomain(..)))", throwing = "ex")
	public void doRecoveryActionsDoamin(JoinPoint joinPoint, Exception ex, FlowEngineAuditable auditable,
			String domain) {

		log.debug(EXECUTE_FLOWENGINE_AUDITABLE);

		try {
			Method method = getMethod(joinPoint);
			String message = EXCEPTION_DETECTED + method.getName() + " for domain : " + domain;
			OPAuditError event = flowEngineAuditProcessor.getErrorEvent(method.getName(), message, domain, ex);
			eventProducer.publish(event);

		} catch (Exception e) {
			log.error(ERROR_AUDIT_APIMANAGER, e);
		}
	}

	@AfterThrowing(pointcut = "@annotation(auditable) && args(ontology,queryType,query,domainName,..)"
			+ " && execution (* com.minsait.onesait.platform.flowengine.nodered.communication.NodeRedAdminClientImpl.submitQuery(..))", throwing = "ex")
	public void doRecoveryActionsFlowEngineNodeQuery(JoinPoint joinPoint, FlowEngineAuditable auditable, Exception ex,
			String ontology, String queryType, String query, String domainName) {

		log.debug(EXECUTE_FLOWENGINE_AUDITABLE);

		try {
			Method method = getMethod(joinPoint);
			String message = EXCEPTION_DETECTED + method.getName() + ", " + "operation query on ontology " + ontology
					+ " with query " + query;

			final OPAuditError event = flowEngineAuditProcessor.getErrorEvent(message, domainName, ex);

			eventProducer.publish(event);
		} catch (Exception e) {
			log.error(ERROR_AUDIT_APIMANAGER, e);
		}
	}

	@AfterThrowing(pointcut = "@annotation(auditable) && args(ontology,data,domainName,..)"
			+ " && execution (* com.minsait.onesait.platform.flowengine.nodered.communication.NodeRedAdminClientImpl.submitInsert(..))", throwing = "ex")
	public void doRecoveryActionsFlowEngineNodeInsert(JoinPoint joinPoint, FlowEngineAuditable auditable, Exception ex,
			String ontology, String data, String domainName) {
		log.debug(EXECUTE_FLOWENGINE_AUDITABLE);

		try {
			Method method = getMethod(joinPoint);
			String message = EXCEPTION_DETECTED + method.getName() + ", " + "Operation insert on ontology " + ontology
					+ " with data " + data;

			final OPAuditError event = flowEngineAuditProcessor.getErrorEvent(message, domainName, ex);

			eventProducer.publish(event);
		} catch (Exception e) {
			log.error(ERROR_AUDIT_APIMANAGER, e);
		}
	}

	@AfterThrowing(pointcut = "@annotation(auditable) && args(invokeRequest,..)"
			+ " && execution (* com.minsait.onesait.platform.flowengine.api.rest.service.impl.FlowEngineNodeServiceImpl.invokeRestApiOperation(..))", throwing = "ex")
	public void doRecoveryActionsFlowEngineNodeInvokeAPI(JoinPoint joinPoint, FlowEngineAuditable auditable,
			Exception ex, FlowEngineInvokeRestApiOperationRequest invokeRequest) {
		log.debug(EXECUTE_FLOWENGINE_AUDITABLE);

		try {
			Method method = getMethod(joinPoint);
			String message = EXCEPTION_DETECTED + method.getName() + ", " + "Executing invocation on API [v"
					+ invokeRequest.getApiVersion() + "] - " + invokeRequest.getApiName() + " - Operation: "
					+ invokeRequest.getOperationName() + "(" + invokeRequest.getOperationMethod() + ")";

			final OPAuditError event = flowEngineAuditProcessor.getErrorEvent(message, invokeRequest.getDomainName(),
					ex);

			eventProducer.publish(event);
		} catch (Exception e) {
			log.error(ERROR_AUDIT_APIMANAGER, e);
		}
	}

	@AfterThrowing(pointcut = "@annotation(auditable) && args(notebookInvocationData,..)"
			+ " && execution (* com.minsait.onesait.platform.flowengine.api.rest.service.impl.FlowEngineNodeServiceImpl.invokeNotebook(..))", throwing = "ex")
	public void doRecoveryActionsFlowEngineNodeInvokeNotebook(JoinPoint joinPoint, FlowEngineAuditable auditable,
			Exception ex, NotebookInvokeDTO notebookInvocationData) {
		log.debug(EXECUTE_FLOWENGINE_AUDITABLE);

		try {
			Method method = getMethod(joinPoint);
			StringBuilder message = new StringBuilder().append(EXCEPTION_DETECTED).append(", ").append(method.getName())
					.append(", ");
			if (Boolean.TRUE.equals(notebookInvocationData.getExecuteNotebook())) {
				// full notebook execution
				message.append("Executed Notebook ").append(notebookInvocationData.getNotebookId());
			} else {
				// just paragraph execution
				message.append("Executed Paragraph ").append(notebookInvocationData.getParagraphId())
						.append(" from Notebook ").append(notebookInvocationData.getNotebookId());
			}
			final OPAuditError event = flowEngineAuditProcessor.getErrorEvent(message.toString(),
					notebookInvocationData.getDomainName(), ex);

			eventProducer.publish(event);
		} catch (Exception e) {
			log.error(ERROR_AUDIT_APIMANAGER, e);
		}
	}

	@AfterThrowing(pointcut = "@annotation(auditable) && args(domainName,pipelineIdentification,..)"
			+ " && execution (* com.minsait.onesait.platform.flowengine.api.rest.service.impl.FlowEngineNodeServiceImpl.getPipelineStatus(..))", throwing = "ex")
	public void doRecoveryActionsFlowEngineNodeCheclDataflowStatus(JoinPoint joinPoint, FlowEngineAuditable auditable,
			Exception ex, String domainName, String pipelineIdentification) {
		log.debug(EXECUTE_FLOWENGINE_AUDITABLE);

		try {
			Method method = getMethod(joinPoint);
			String message = EXCEPTION_DETECTED + method.getName() + ", " + "Check Status on Dataflow "
					+ pipelineIdentification;

			final OPAuditError event = flowEngineAuditProcessor.getErrorEvent(message.toString(), domainName, ex);

			eventProducer.publish(event);
		} catch (Exception e) {
			log.error(ERROR_AUDIT_APIMANAGER, e);
		}
	}

	@AfterThrowing(pointcut = "@annotation(auditable) && args(domainName,pipelineIdentification,..)"
			+ " && execution (* com.minsait.onesait.platform.flowengine.api.rest.service.impl.FlowEngineNodeServiceImpl.stopDataflow(..))", throwing = "ex")
	public void doRecoveryActionsFlowEngineNodeStopDataflow(JoinPoint joinPoint, FlowEngineAuditable auditable,
			Exception ex, String domainName, String pipelineIdentification) {
		log.debug(EXECUTE_FLOWENGINE_AUDITABLE);

		try {
			Method method = getMethod(joinPoint);
			String message = EXCEPTION_DETECTED + method.getName() + ", " + "Stop Dataflow " + pipelineIdentification;

			final OPAuditError event = flowEngineAuditProcessor.getErrorEvent(message.toString(), domainName, ex);

			eventProducer.publish(event);
		} catch (Exception e) {
			log.error(ERROR_AUDIT_APIMANAGER, e);
		}
	}

	@AfterThrowing(pointcut = "@annotation(auditable) && args(domainName,pipelineIdentification,..)"
			+ " && execution (* com.minsait.onesait.platform.flowengine.api.rest.service.impl.FlowEngineNodeServiceImpl.startDataflow(..))", throwing = "ex")
	public void doRecoveryActionsFlowEngineNodeStartDataflow(JoinPoint joinPoint, FlowEngineAuditable auditable,
			Exception ex, String domainName, String pipelineIdentification) {
		log.debug(EXECUTE_FLOWENGINE_AUDITABLE);

		try {
			Method method = getMethod(joinPoint);
			String message = EXCEPTION_DETECTED + method.getName() + ", " + "Start Dataflow " + pipelineIdentification;

			final OPAuditError event = flowEngineAuditProcessor.getErrorEvent(message.toString(), domainName, ex);

			eventProducer.publish(event);
		} catch (Exception e) {
			log.error(ERROR_AUDIT_APIMANAGER, e);
		}
	}
	
	@AfterThrowing(pointcut = "@annotation(auditable) && args(mail,..)"
			+ " && execution (* com.minsait.onesait.platform.flowengine.api.rest.service.impl.FlowEngineNodeServiceImpl.sendMail(..))", throwing = "ex")
	public void doRecoveryActionsFlowEngineNodeSendMail(JoinPoint joinPoint, FlowEngineAuditable auditable,
			Exception ex, MailRestDTO mail) {
		log.debug(EXECUTE_FLOWENGINE_AUDITABLE);

		try {
			Method method = getMethod(joinPoint);
			String message = EXCEPTION_DETECTED + method.getName() + ", " + "Send Email. TO:" + Arrays.toString(mail.getTo()) + ", SUBJECT: " + mail.getSubject();

			final OPAuditError event = flowEngineAuditProcessor.getErrorEvent(message, mail.getDomainName(), ex);

			eventProducer.publish(event);
		} catch (Exception e) {
			log.error(ERROR_AUDIT_APIMANAGER, e);
		}
	}
}
