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

import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.audit.bean.AuditConst;
import com.minsait.onesait.platform.audit.bean.CalendarUtil;
import com.minsait.onesait.platform.audit.bean.OPAuditError;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.EventType;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.Module;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.OperationType;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.ResultOperationType;
import com.minsait.onesait.platform.audit.bean.OPEventFactory;
import com.minsait.onesait.platform.commons.flow.engine.dto.FlowEngineDomain;
import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.FlowDomainRepository;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.DecodedAuthentication;
import com.minsait.onesait.platform.flowengine.api.rest.service.FlowEngineValidationNodeService;
import com.minsait.onesait.platform.flowengine.audit.bean.FlowEngineAuditEvent;
import com.minsait.onesait.platform.flowengine.audit.bean.FlowEngineAuditEvent.FlowEngineAuditEventBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FlowEngineAuditProcessor {

	@Autowired
	private FlowDomainRepository domainRepository;

	@Autowired
	private FlowEngineValidationNodeService flowEngineValidationNodeService;

	public String getUserId(String domainId) {

		String userId = null;

		if (domainId != null) {

			FlowDomain flowDomain = domainRepository.findByIdentification(domainId);

			if (flowDomain != null) {
				userId = flowDomain.getUser().getUserId();
			} else {
				userId = AuditConst.ANONYMOUS_USER;
			}
		}

		return userId;
	}

	public String getUserId(FlowEngineDomain domain) {
		String userId = null;

		if (domain != null) {
			userId = getUserId(domain.getDomain());
		}

		return userId;
	}

	public FlowEngineAuditEvent getEvent(String methodName, String retVal, FlowEngineDomain domain,
			OperationType operation) {

		log.debug("getEvent for operation " + methodName + " and return value " + retVal);
		String userId = getUserId(domain);

		FlowEngineAuditEventBuilder builder = FlowEngineAuditEvent.builder();

		ResultOperationType resultOperation = null;
		String message = null;

		if (!"OK".equals(retVal)) {
			builder.message(retVal);
			message = retVal;
			resultOperation = ResultOperationType.ERROR;
		} else {
			message = "Executed operation " + methodName + " for domain " + domain.getDomain() + " by user " + userId;
			resultOperation = ResultOperationType.SUCCESS;
		}

		return getEvent(domain.getDomain(), userId, operation, message, resultOperation);
	}

	public FlowEngineAuditEvent getEvent(String methodName, String domainId, OperationType operation) {

		log.debug("getEvent for operation " + methodName + " and domain " + domainId);
		String userId = getUserId(domainId);

		String message = "Executed operation " + methodName + " for domain " + domainId + " by user " + userId;

		return getEvent(domainId, userId, operation, message, ResultOperationType.SUCCESS);
	}

	public FlowEngineAuditEvent getQueryEvent(String ontology, String query, String queryType, String retVal,
			String authentication) {
		String message = "Query message on ontology " + ontology;
		return getEvent(ontology, query, queryType, null, message, authentication, OperationType.QUERY);
	}

	public FlowEngineAuditEvent getInsertEvent(String ontology, String data, String retVal, String authentication) {
		String message = "Executed insert on ontology " + ontology;
		return getEvent(ontology, null, null, data, message, authentication, OperationType.INSERT);
	}

	public FlowEngineAuditEvent getEvent(String ontology, String query, String queryType, String data, String message,
			String authentication, OperationType operation) {

		log.debug("getEvent for operation");
		FlowEngineAuditEvent event = null;

		try {

			final DecodedAuthentication decodedAuth = flowEngineValidationNodeService.decodeAuth(authentication);
			final User sofia2User = flowEngineValidationNodeService.validateUser(decodedAuth.getUserId());

			event = getEvent(null, sofia2User.getUserId(), operation, message, ResultOperationType.SUCCESS);
			event.setData(data);
			event.setOntology(ontology);
			event.setQuery(query);

		} catch (Exception e) {
			log.error("error processing getEvent submit operation ", e);
		}

		return event;
	}

	public FlowEngineAuditEvent getEvent(String domain, String user, OperationType operation, String message,
			ResultOperationType result) {

		Date today = new Date();

		return FlowEngineAuditEvent.builder().id(UUID.randomUUID().toString()).module(Module.FLOWENGINE).domain(domain)
				.type(EventType.FLOWENGINE).operationType(operation.name()).timeStamp(today.getTime()).user(user)
				.message(message).resultOperation(result)
				.formatedTimeStamp(CalendarUtil.builder().build().convert(today)).build();
	}

	public OPAuditError getErrorEvent(String methodName, FlowEngineDomain domain, Exception ex) {

		log.debug("getEventError for operation " + methodName);
		String userId = getUserId(domain);
		String messageOperation = "Exception Detected while executing " + methodName + " for domain : "
				+ domain.getDomain();

		return createErrorEvent(userId, messageOperation, ex);
	}

	public OPAuditError getErrorEvent(String message, String authentication, Exception ex) {

		OPAuditError event = null;

		try {

			final DecodedAuthentication decodedAuth = flowEngineValidationNodeService.decodeAuth(authentication);
			final User sofia2User = flowEngineValidationNodeService.validateUser(decodedAuth.getUserId());

			event = createErrorEvent(sofia2User.getUserId(), message, ex);
		} catch (Exception e) {
			log.error("error getting error event", e);
		}

		return event;
	}

	public OPAuditError getErrorEvent(String method, String message, String domain, Exception ex) {
		String userId = getUserId(domain);
		return createErrorEvent(userId, message, ex);
	}

	public OPAuditError createErrorEvent(String userId, String message, Exception ex) {

		return OPEventFactory.builder().build().createAuditEventError(userId, message, Module.FLOWENGINE, ex);
	}

}
