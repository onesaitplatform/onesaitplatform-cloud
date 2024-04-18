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
package com.minsait.onesait.platform.api.audit.aop;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.api.audit.bean.ApiManagerAuditEvent;
import com.minsait.onesait.platform.api.service.Constants;
import com.minsait.onesait.platform.api.service.impl.ApiServiceImpl.ChainProcessingStatus;
import com.minsait.onesait.platform.audit.bean.AuditConst;
import com.minsait.onesait.platform.audit.bean.CalendarUtil;
import com.minsait.onesait.platform.audit.bean.OPAuditError;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.EventType;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.Module;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.OperationType;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.ResultOperationType;
import com.minsait.onesait.platform.audit.bean.OPEventFactory;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.User;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ApiManagerAuditProcessor {

	public ApiManagerAuditEvent getStoppedEvent(Map<String, Object> data) {
		ApiManagerAuditEvent event = null;
		try {
			if (ChainProcessingStatus.STOP.equals(data.get(Constants.STATUS))) {
				final String reason = (String) data.get(Constants.REASON);

				final String remoteAddress = (String) data.get(Constants.REMOTE_ADDRESS);
				final Ontology ontology = (Ontology) data.get(Constants.ONTOLOGY);
				final String method = (String) data.get(Constants.METHOD);

				final String query = (String) data.get(Constants.QUERY);
				final String body = (String) data.get(Constants.BODY);

				final OperationType operationType = getAuditOperationFromMethod(method);

				final User user = (User) data.get(Constants.USER);

				final String userId = getUserId(user);
				final String ontologyId = getOntologyId(ontology);
				final String operation = getOperation(operationType);

				final Api api = (Api) data.get(Constants.API);
				final Date today = new Date();

				event = ApiManagerAuditEvent.builder().id(UUID.randomUUID().toString()).module(Module.APIMANAGER)
						.type(EventType.APIMANAGER).operationType(operation).resultOperation(ResultOperationType.ERROR)
						.remoteAddress(remoteAddress).message(reason).data(body).ontology(ontologyId).query(query)
						.timeStamp(today.getTime()).user(userId).api(api != null ? api.getIdentification() : null)
						.formatedTimeStamp(CalendarUtil.builder().build().convert(today)).build();
			}

		} catch (final Exception e) {
			log.error("error processing stopped event ", e);
		}
		return event;
	}

	public ApiManagerAuditEvent getEvent(Map<String, Object> data) {
		final String remoteAddress = (String) data.get(Constants.REMOTE_ADDRESS);
		final Ontology ontology = (Ontology) data.get(Constants.ONTOLOGY);
		final String method = (String) data.get(Constants.METHOD);
		final String query = (String) data.get(Constants.QUERY);
		final String body = (String) data.get(Constants.BODY);
		final User user = (User) data.get(Constants.USER);
		final Api api = (Api) data.get(Constants.API);
		final OperationType operationType = getAuditOperationFromMethod(method);
		final String operation = getOperation(operationType);

		final Date today = new Date();

		final String userId = getUserId(user);
		final String ontologyId = getOntologyId(ontology);

		final String message = operation + " on ontology " + ontologyId + " by user " + userId;

		return (ApiManagerAuditEvent.builder().id(UUID.randomUUID().toString()).module(Module.APIMANAGER)
				.type(EventType.APIMANAGER).operationType(operation).resultOperation(ResultOperationType.SUCCESS)
				.remoteAddress(remoteAddress).message(message).data(body).ontology(ontologyId).query(query)
				.timeStamp(today.getTime()).user(userId).api(api != null ? api.getIdentification() : null)
				.formatedTimeStamp(CalendarUtil.builder().build().convert(today)).build());

	}

	public OPAuditError getErrorEvent(Map<String, Object> data, Exception ex) {
		final String remoteAddress = (String) data.get(Constants.REMOTE_ADDRESS);
		final Ontology ontology = (Ontology) data.get(Constants.ONTOLOGY);
		final String method = (String) data.get(Constants.METHOD);
		final User user = (User) data.get(Constants.USER);

		final OperationType operationType = getAuditOperationFromMethod(method);
		final String operation = getOperation(operationType);

		final String userId = getUserId(user);
		final String ontologyId = getOntologyId(ontology);

		final String messageOperation = "Exception Detected while operation : " + ontologyId + " Type : " + operation;

		return (OPEventFactory.builder().build().createAuditEventError(userId, messageOperation, remoteAddress,
				Module.APIMANAGER, ex));

	}

	public ApiManagerAuditEvent completeEvent(ApiManagerAuditEvent event) {
		if (event != null && (event.getUser() == null || "".equals(event.getUser()))) {
			log.debug("the user is null so set user to anonymous");
			event.setUser(AuditConst.ANONYMOUS_USER);
		}
		return event;
	}

	public OperationType getAuditOperationFromMethod(String method) {
		OperationType operationType = null;
		if (method != null) {
			if (method.equalsIgnoreCase(ApiOperation.Type.GET.name())) {
				operationType = OperationType.QUERY;
			} else if (method.equalsIgnoreCase(ApiOperation.Type.POST.name())) {
				operationType = OperationType.INSERT;
			} else if (method.equalsIgnoreCase(ApiOperation.Type.PUT.name())) {
				operationType = OperationType.UPDATE;
			} else if (method.equalsIgnoreCase(ApiOperation.Type.DELETE.name())) {
				operationType = OperationType.DELETE;
			}
		}
		log.debug("The audit operation from method {} is {} ",method,operationType);
		return operationType;

	}

	public String getUserId(User user) {
		return user != null ? user.getUserId() : AuditConst.ANONYMOUS_USER;
	}

	public String getOntologyId(Ontology ontology) {
		return ontology != null ? ontology.getIdentification() : null;
	}

	public String getOperation(OperationType operationType) {
		return operationType != null ? operationType.name() : "";
	}

}
