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
package com.minsait.onesait.platform.audit.aop;

import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

import com.minsait.onesait.platform.audit.bean.DashboardEngineAuditEvent;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.OperationType;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.ResultOperationType;
import com.minsait.onesait.platform.dto.socket.InputMessage;
import com.minsait.onesait.platform.security.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class DashboardEngineAuditableAspect extends BaseAspect {

	@Autowired
	private DashboardEngineAuditProcessor dashboardEngineAuditProcessor;
	@Autowired
	private AppWebUtils utils;

	@Around("@annotation(auditable) && args(msg) && execution(* com.minsait.onesait.platform.service.SolverServiceImpl.solveDatasource(..))")
	public String auditSolveDatasource(ProceedingJoinPoint joinPoint, DashboardEngineAuditable auditable,
			InputMessage msg) throws Throwable {
		log.debug("Auditing solveDatasource()");
		final String message = "Query for: " + msg.getDs() + ",  params: filter: " + msg.getFilter() + ", project: "
				+ msg.getProject() + ", group: " + msg.getGroup() + ", sort: " + msg.getSort() + ", offset: "
				+ msg.getOffset() + ", limit: " + msg.getLimit();
		DashboardEngineAuditEvent event = dashboardEngineAuditProcessor.genetateAuditEvent(utils.getUserId(),
				UUID.randomUUID().toString(), null, OperationType.SOLVER_DS, null, msg.getDashboard(), msg.getDs(),
				message);
		String response = null;
		try {
			response = (String) joinPoint.proceed();
			event = dashboardEngineAuditProcessor.completeAuditEvent(event, response);
			eventProducer.publish(event);
			return response;
		} catch (final Exception e) {
			event = dashboardEngineAuditProcessor.completeAuditEventWithError(event, e);
			eventProducer.publish(event);
			throw e;
		}
	}

	@Before("@annotation(auditable) && args(event) && execution(* com.minsait.onesait.platform.websocket.event.StompConnectedEventListener.onApplicationEvent(..))")
	public void auditWSConnected(DashboardEngineAuditable auditable, SessionConnectedEvent event) {
		final DashboardEngineAuditEvent e = dashboardEngineAuditProcessor.genetateAuditEvent(event.getUser().getName(),
				UUID.randomUUID().toString(), null, OperationType.WEBSOCKET_CONNECTED, null, null, null, null);
		e.setResultOperation(ResultOperationType.SUCCESS);
		eventProducer.publish(e);
	}

	@Around("args(im) && execution(* *..validate*(com.minsait.onesait.platform.security.dashboard.engine.dto.InputMessage))")
	public boolean auditValidationServicePlugin(ProceedingJoinPoint joinPoint, com.minsait.onesait.platform.security.dashboard.engine.dto.InputMessage im) throws Throwable {
		boolean result = false;
		final DashboardEngineAuditEvent e = dashboardEngineAuditProcessor.genetateAuditEvent(utils.getUserId(),
				UUID.randomUUID().toString(), null, OperationType.PLUGIN_VALIDATION_SERVICE, null, im.getDashboard(), im.getDs(), im.getQuery());
		e.setResultOperation(ResultOperationType.SUCCESS);
		// TO-DO get signature package.
		try {
			result = (boolean) joinPoint.proceed();
			if (result) {
				e.setResultOperation(ResultOperationType.SUCCESS);
				e.setMessage("Validation Service of signature: "+ joinPoint.getSignature().toLongString() +" returned true");
			} else {
				e.setResultOperation(ResultOperationType.WARNING);
				e.setMessage("Validation Service of signature: "+ joinPoint.getSignature().toLongString() +" returned false");
			}
			eventProducer.publish(e);
		} catch (final Exception ex) {
			dashboardEngineAuditProcessor.completeAuditEventWithError(e, ex);
			eventProducer.publish(e);
			throw ex;
		}

		return result;
	}

}
