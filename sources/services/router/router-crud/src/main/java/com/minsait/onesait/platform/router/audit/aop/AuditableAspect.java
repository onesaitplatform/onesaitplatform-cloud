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
package com.minsait.onesait.platform.router.audit.aop;

import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.audit.aop.BaseAspect;
import com.minsait.onesait.platform.audit.bean.OPAuditError;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.EventType;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.Module;
import com.minsait.onesait.platform.audit.bean.OPEventFactory;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Order
@Component
@Slf4j
public class AuditableAspect extends BaseAspect {

	private static final String PROCESSING_STR = "Processing :";
	private static final String TYPE_STR = " Type : ";
	private static final String BY_USER_STR = " By User : ";
	private static final String CALL_FOR = "INFO Log @@AfterThrowing Call For: {} -> {}";
	private static final String CALL_FOR_EXCEPTION = "INFO Log @@AfterThrowing Call For: {} -> {}. Exception Message: {}";
	private static final String CALL_FOR_CLASS = "INFO Log @@AfterThrowing Call For: {} -> {}. Class: {}";

	// @Around(value = "@annotation(auditable)")
	public Object processTx(ProceedingJoinPoint joinPoint, Auditable auditable) throws java.lang.Throwable {

		final String className = getClassName(joinPoint);
		final String methodName = getMethod(joinPoint).getName();
		long executionTime = -1l;

		final long start = System.currentTimeMillis();
		Object proceed = null;

		OPAuditEvent event = null;

		final OperationModel model = (OperationModel) getTheObject(joinPoint, OperationModel.class);
		if (model != null) {
			event = Sofia2RouterEventFactory.createAuditEvent(joinPoint, auditable, EventType.DATA,
					PROCESSING_STR + className + "-> " + methodName);
			event.setOntology(model.getOntologyName());
			event.setOperationType(model.getOperationType().name());
			event.setUser(model.getUser());
			event.setMessage("Executing operation for Ontology : " + model.getOntologyName() + TYPE_STR
					+ model.getOperationType().name() + BY_USER_STR + model.getUser() + " With ObjectId: "
					+ model.getObjectId());
		}

		else {
			event = Sofia2RouterEventFactory.createAuditEvent(joinPoint, auditable, EventType.GENERAL,
					PROCESSING_STR + className + "-> " + methodName);
			event.setMessage("Action Performed");
		}

		try {
			proceed = joinPoint.proceed();
		}

		finally {
			updateStats(className, methodName, System.currentTimeMillis() - start);

			eventProducer.publish(event);
		}

		executionTime = System.currentTimeMillis() - start;

		log.info("Execution of class {}, method {} executed in {} ms", className, methodName, executionTime);

		return proceed;
	}

	// @Before("@annotation(auditable)")
	public void beforeExecution(JoinPoint joinPoint, Auditable auditable) {
		final String className = getClassName(joinPoint);
		final String methodName = getMethod(joinPoint).getName();

		OPAuditEvent event = null;

		final OperationModel model = (OperationModel) getTheObject(joinPoint, OperationModel.class);
		if (model != null) {
			event = Sofia2RouterEventFactory.createAuditEvent(joinPoint, auditable, EventType.DATA,
					PROCESSING_STR + className + "-> " + methodName);
			event.setOntology(model.getOntologyName());
			event.setOperationType(model.getOperationType().name());
			event.setUser(model.getUser());
			event.setMessage("Before Executing operation for Ontology : " + model.getOntologyName() + TYPE_STR
					+ model.getOperationType().name() + BY_USER_STR + model.getUser() + " With ObjectId: "
					+ model.getObjectId());
		}

		else {
			event = Sofia2RouterEventFactory.createAuditEvent(joinPoint, auditable, EventType.GENERAL,
					PROCESSING_STR + className + "-> " + methodName);
			event.setMessage("Action Being Performed");
		}

		eventProducer.publish(event);
	}

	@SuppressWarnings("rawtypes")
	@AfterReturning(pointcut = "@annotation(auditable)", returning = "retVal")
	public void afterReturningExecution(JoinPoint joinPoint, Object retVal, Auditable auditable) {

		final String className = getClassName(joinPoint);
		final String methodName = getMethod(joinPoint).getName();

		if (retVal != null) {

			OPAuditEvent event = Sofia2RouterEventFactory.createAuditEvent(joinPoint, auditable, EventType.DATA,
					"Execution of :" + className + "-> " + methodName);

			if (retVal instanceof ResponseEntity) {
				final ResponseEntity response = (ResponseEntity) retVal;
				if (log.isDebugEnabled()) {
					log.debug(
						"After -> CALL FOR {} -> {} RETURNED CODE: ", className, methodName,
						response.getStatusCode());
				}				
			}

			if (retVal instanceof OperationResultModel) {
				final OperationModel model = (OperationModel) getTheObject(joinPoint, OperationModel.class);

				final OperationResultModel response = (OperationResultModel) retVal;
				event = Sofia2RouterEventFactory.createAuditEvent(joinPoint, auditable, EventType.DATA,
						PROCESSING_STR + className + "-> " + methodName);
				event.setOntology(model.getOntologyName());
				event.setOperationType(model.getOperationType().name());
				event.setUser(model.getUser());
				event.setMessage("operation for Ontology : " + model.getOntologyName() + TYPE_STR
						+ model.getOperationType().name() + BY_USER_STR + model.getUser() + " Has a Response: "
						+ response.getMessage());

				final Map<String, Object> data = new HashMap<>();
				data.put("data", retVal);

				eventProducer.publish(event);
			}

			// not storing returned value at this moment.. Do I need?

		}
	}

	@AfterThrowing(pointcut = "@annotation(auditable)", throwing = "ex")
	public void doRecoveryActions(JoinPoint joinPoint, Exception ex, Auditable auditable) {

		final String className = getClassName(joinPoint);
		final String methodName = getMethod(joinPoint).getName();

		OPAuditError event = null;

		final OperationModel model = (OperationModel) getTheObject(joinPoint, OperationModel.class);

		if (model != null) {

			final String messageOperation = "Exception Detected while operation : " + model.getOntologyName() + TYPE_STR
					+ model.getOperationType().name() + BY_USER_STR + model.getUser();

			event = OPEventFactory.builder().build().createAuditEventError(model.getUser(), messageOperation,
					Module.ROUTER, ex);

			event.setOntology(model.getOntologyName());
			event.setOperationType(model.getOperationType().name());

		} else {
			event = OPEventFactory.builder().build().createAuditEventError("Exception Detected", Module.ROUTER, ex);
		}

		OPEventFactory.builder().build().setErrorDetails(event, ex);
		eventProducer.publish(event);

		log.debug(CALL_FOR, className, methodName);
		log.debug(CALL_FOR_EXCEPTION, className, methodName, ex.getMessage());
		log.debug(CALL_FOR_CLASS, className, methodName, ex.getClass().getName());

	}

}