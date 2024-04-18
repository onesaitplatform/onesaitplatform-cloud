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

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.audit.aop.BaseAspect;
import com.minsait.onesait.platform.audit.bean.OPAuditError;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.OperationType;
import com.minsait.onesait.platform.commons.flow.engine.dto.FlowEngineDomain;
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

		final FlowEngineAuditEvent event = flowEngineAuditProcessor.getInsertEvent(ontology, data, retVal,
				domainName);

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

}
