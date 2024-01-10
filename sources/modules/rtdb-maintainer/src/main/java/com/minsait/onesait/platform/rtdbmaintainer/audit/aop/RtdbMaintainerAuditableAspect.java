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
package com.minsait.onesait.platform.rtdbmaintainer.audit.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.audit.aop.BaseAspect;
import com.minsait.onesait.platform.audit.bean.OPAuditError;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.ResultOperationType;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.rtdbmaintainer.audit.bean.RtdbMaintainerAuditEvent;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Order
@Component
@Slf4j
public class RtdbMaintainerAuditableAspect extends BaseAspect {

	@Autowired
	RtdbMaintainerAuditProcessor auditProcessor;

	private static final String PROCESSING_ACTIONS = "Processing Rtdb maintainer auditable recovery actions";

	@Around("@annotation(auditable) && args(ontology,query,..)")
	public Object exportToJsonAndDeleteAudit(ProceedingJoinPoint joinPoint, Ontology ontology,
			RtdbMaintainerAuditable auditable, String query) throws Throwable {
		String file = null;
		log.debug("execute rtdb maintainer aspect method export to json");
		RtdbMaintainerAuditEvent event = auditProcessor.getEvent(ontology, joinPoint.getSignature().getName());
		eventProducer.publish(event);
		try {

			file = (String) joinPoint.proceed();
			event = auditProcessor.completeEvent(ontology, joinPoint.getSignature().getName());
		} catch (final Throwable e) {
			log.error("Exception while auditing method exportToJsonAndDeleteAudit");
			throw e;
		}

		eventProducer.publish(event);
		return file;
	}

	@Around("@annotation(auditable) && args(collection,..)")
	public Object deleteTmpGenCollection(ProceedingJoinPoint joinPoint, String collection,
			RtdbMaintainerAuditable auditable) throws Throwable {
		String file = null;
		log.debug("execute rtdb maintainer drop collection tmp gen");
		RtdbMaintainerAuditEvent event = auditProcessor.getEventDeleteCollection(collection);
		eventProducer.publish(event);
		try {

			file = (String) joinPoint.proceed();
			event = auditProcessor.completeEventDeleteCollection(collection);
		} catch (final Throwable e) {
			log.error("Exception while auditing method deleteTmpGenCollection");
			throw e;
		}

		eventProducer.publish(event);
		return file;
	}
	
	@Around("@annotation(auditable) && args(context,..)")
	public void kpiGeneration(ProceedingJoinPoint joinPoint, JobExecutionContext context,
			RtdbMaintainerAuditable auditable) throws Throwable {
		log.info("execute rtdb maintainer KPI Generation");
		
		final String user = context.getJobDetail().getJobDataMap().getString("userId");
		final String query = context.getJobDetail().getJobDataMap().getString("query");
		final String ontology = context.getJobDetail().getJobDataMap().getString("ontology");
		
		RtdbMaintainerAuditEvent event = auditProcessor.getEventKPIExecution(user, query, ontology, ResultOperationType.SUCCESS);
		try {
			joinPoint.proceed();
			eventProducer.publish(event);
		} catch (final Throwable e) {
			log.error("Exception while auditing method deleteTmpGenCollection");
			RtdbMaintainerAuditEvent eventError = auditProcessor.getEventKPIExecution(user, query, ontology, ResultOperationType.ERROR);
			eventProducer.publish(eventError);
			throw e;
		}
	}
	

	@Before("@annotation(auditable) && args(opId,..)")
	public void beforeKillOp(JoinPoint joinPoint, RtdbMaintainerAuditable auditable, long opId) {
		if (log.isDebugEnabled()) {
			log.debug("Killing mongo op with id: {}", opId);
		}		
		final RtdbMaintainerAuditEvent event = auditProcessor.getEventKillOp(opId);
		eventProducer.publish(event);

	}

	@AfterThrowing(pointcut = "@annotation(auditable) && args(ontology,..)", throwing = "ex")
	public void doRecoveryActions(JoinPoint joinPoint, RtdbMaintainerAuditable auditable, Ontology ontology,
			Exception ex) {
		log.debug(PROCESSING_ACTIONS);
		final OPAuditError event = auditProcessor.getErrorEvent(ontology, ex);
		eventProducer.publish(event);
	}

	@AfterThrowing(pointcut = "@annotation(auditable) && args(opId,..)", throwing = "ex")
	public void doRecoveryActions(JoinPoint joinPoint, RtdbMaintainerAuditable auditable, Long opId, Exception ex) {
		log.debug(PROCESSING_ACTIONS);
		final OPAuditError event = auditProcessor.getErrorEventOpKill(opId, ex);
		eventProducer.publish(event);
	}

	@AfterThrowing(pointcut = "@annotation(auditable) && args(collection,..)", throwing = "ex")
	public void doRecoveryActions(JoinPoint joinPoint, RtdbMaintainerAuditable auditable, String collection,
			Exception ex) {
		log.debug(PROCESSING_ACTIONS);
		final OPAuditError event = auditProcessor.getErrorEventDeleteCollection(collection, ex);
		eventProducer.publish(event);
	}

}
