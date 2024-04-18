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
package com.minsait.onesait.platform.api.audit.aop;

import java.util.Map;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.api.audit.bean.ApiManagerAuditEvent;
import com.minsait.onesait.platform.api.service.impl.ApiServiceException;
import com.minsait.onesait.platform.audit.aop.BaseAspect;
import com.minsait.onesait.platform.audit.bean.OPAuditError;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.ResultOperationType;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Order
@Component
@Slf4j
public class ApiManagerAuditableAspect extends BaseAspect {

	@Autowired
	private ApiManagerAuditProcessor apiManagerAuditProcessor;

	@AfterReturning(value = "@annotation(auditable)", returning = "data")
	public void processRequestData(JoinPoint joinPoint, ApiManagerAuditable auditable, Map<String, Object> data) {
		try {

			final ApiManagerAuditEvent event = apiManagerAuditProcessor.getStoppedEvent(data);
			eventProducer.publish(event);

		} catch (final Exception e) {
			log.error("error after process method ", e);
		}
	}

	@Around(value = "@annotation(auditable) && args(data,..) && execution (* com.minsait.onesait.platform.api.processor.*.process(..))")
	public Object processQuery(ProceedingJoinPoint joinPoint, ApiManagerAuditable auditable, Map<String, Object> data)
			throws java.lang.Throwable {

		log.debug("Execute api manager aspect method process");

		ApiManagerAuditEvent event = null;
		Map<String, Object> retVal = null;

		try {

			event = apiManagerAuditProcessor.getEvent(data);

			final Map<String, Object> proceed = extracted(joinPoint);
			retVal = proceed;

			event = apiManagerAuditProcessor.completeEvent(event);

		} catch (final ApiServiceException e) {
			log.error("Error process apimanager process ", e);
			if (event != null) {
				event.setResultOperation(ResultOperationType.ERROR);
				event.setMessage(e.getMessage());
			}
		}

		eventProducer.publish(event);

		return retVal;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> extracted(ProceedingJoinPoint joinPoint) throws Throwable {
		return (Map<String, Object>) joinPoint.proceed();
	}

	@AfterThrowing(pointcut = "@annotation(auditable) && args(data,..)", throwing = "ex")
	public void doRecoveryActions(JoinPoint joinPoint, Exception ex, ApiManagerAuditable auditable,
			Map<String, Object> data) {

		log.debug("Execute aspect apimanager method doRecoveryActions");

		try {
			final OPAuditError event = apiManagerAuditProcessor.getErrorEvent(data, ex);
			eventProducer.publish(event);

		} catch (final Exception e) {
			log.error("error auditing apimanager doRecoveryActions", e);
		}
	}
}
