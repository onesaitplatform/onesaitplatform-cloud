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
package com.minsait.onesait.platform.iotbroker.audit.bean;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.audit.bean.CalendarUtil;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.EventType;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.Module;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.OperationType;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.ResultOperationType;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyDeleteByIdMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyDeleteMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyInsertMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyJoinMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyLogMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyQueryMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodySubscribeMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyUnsubscribeMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyUpdateByIdMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyUpdateMessage;
import com.minsait.onesait.platform.iotbroker.plugable.interfaces.gateway.GatewayInfo;
import com.minsait.onesait.platform.multitenant.config.model.IoTSession;

import lombok.Builder;

@Builder
public class IotBrokerAuditEventFactory {

	private static final String SOURCE = "source";

	public IotBrokerAuditEvent createIotBrokerAuditEvent(SSAPBodyInsertMessage message, String messageText,
			IoTSession session, GatewayInfo info) {
		return createIotBrokerAuditEvent(message.getOntology(), null, message.getData(), OperationType.INSERT,
				messageText, info, session, message.getTags());
	}

	public IotBrokerAuditEvent createIotBrokerAuditEvent(SSAPBodyQueryMessage message, String messageText,
			IoTSession session, GatewayInfo info) {
		return createIotBrokerAuditEvent(message.getOntology(), message.getQuery(), null, OperationType.QUERY,
				messageText, info, session, message.getTags());
	}

	public IotBrokerAuditEvent createIotBrokerAuditEvent(SSAPBodySubscribeMessage message, String messageText,
			IoTSession session, GatewayInfo info) {
		return createIotBrokerAuditEvent(message.getOntology(), null, null, OperationType.SUBSCRIBE, messageText, info,
				session, null);
	}

	public IotBrokerAuditEvent createIotBrokerAuditEvent(SSAPBodyUnsubscribeMessage message, String messageText,
			IoTSession session, GatewayInfo info) {
		return createIotBrokerAuditEvent(OperationType.UNSUBSCRIBE, messageText, session, info, message.getTags());
	}

	public IotBrokerAuditEvent createIotBrokerAuditEvent(SSAPBodyLogMessage message, String messageText,
			IoTSession session, GatewayInfo info) {
		return createIotBrokerAuditEvent(OperationType.LOG, messageText, session, info, message.getTags());
	}

	public IotBrokerAuditEvent createIotBrokerAuditEvent(SSAPBodyUpdateByIdMessage message, String messageText,
			IoTSession session, GatewayInfo info) {
		return createIotBrokerAuditEvent(message.getOntology(), null, message.getData(), OperationType.UPDATE,
				messageText, info, session, message.getTags());
	}

	public IotBrokerAuditEvent createIotBrokerAuditEvent(SSAPBodyUpdateMessage message, String messageText,
			IoTSession session, GatewayInfo info) {
		return createIotBrokerAuditEvent(message.getOntology(), message.getQuery(), null, OperationType.UPDATE,
				messageText, info, session, message.getTags());
	}

	public IotBrokerAuditEvent createIotBrokerAuditEvent(SSAPBodyDeleteByIdMessage message, String messageText,
			IoTSession session, GatewayInfo info) {
		return createIotBrokerAuditEvent(message.getOntology(), null, null, OperationType.DELETE, messageText, info,
				session, message.getTags());
	}

	public IotBrokerAuditEvent createIotBrokerAuditEvent(SSAPBodyDeleteMessage message, String messageText,
			IoTSession session, GatewayInfo info) {
		return createIotBrokerAuditEvent(message.getOntology(), message.getQuery(), null, OperationType.DELETE,
				messageText, info, session, message.getTags());
	}

	public IotBrokerAuditEvent createIotBrokerAuditEvent(SSAPBodyJoinMessage message, String messageText,
			IoTSession session, GatewayInfo info) {
		return createIotBrokerAuditEvent(OperationType.JOIN, messageText, session, info, message.getTags());
	}

	// -------------------------------------------------------------------------------------------------//

	public IotBrokerAuditEvent createIotBrokerAuditEvent(OperationType operationType, String messageText,
			IoTSession session, GatewayInfo info, String tags) {
		return createIotBrokerAuditEvent(null, null, null, operationType, messageText, info, session, tags);
	}

	public IotBrokerAuditEvent createIotBrokerAuditEvent(String ontology, String query, JsonNode data,
			OperationType operationType, String messageText, GatewayInfo info, IoTSession session, String tags) {

		IotBrokerAuditEvent event = createIotBrokerAuditEvent(ontology, query, data, operationType, messageText, info,
				tags);

		if (session != null) {
			event.setUser(session.getUserID());
			event.setSessionKey(session.getSessionKey());
			event.setClientPlatform(session.getClientPlatform());
			event.setClientPlatformInstance(session.getClientPlatform());
		}

		return event;
	}

	public IotBrokerAuditEvent createIotBrokerAuditEvent(String ontology, String query, JsonNode data,
			OperationType operationType, String messageText, GatewayInfo info, String tags) {

		IotBrokerAuditEvent event = createIotBrokerAuditEvent(ontology, query, operationType, messageText, info, tags);

		event.setData((data != null) ? data.toString() : null);
		return event;
	}

	public IotBrokerAuditEvent createIotBrokerAuditEvent(String ontology, String query, OperationType operationType,
			String messageText, GatewayInfo info, String tags) {

		IotBrokerAuditEvent event = createIotBrokerAuditEvent(operationType, messageText, info, tags);

		event.setOntology(ontology);
		event.setQuery(query);
		return event;
	}

	public IotBrokerAuditEvent createIotBrokerAuditEvent(OperationType operationType, String messageText,
			GatewayInfo info, String tags) {
		Module module = null;
		if (tags != null) {
			try {
				JsonNode json = new ObjectMapper().readTree(tags);
				if (!json.has(SOURCE)) {
					module = Module.IOTBROKER;
				} else {
					module = Module.valueOf(json.get(SOURCE).asText().toUpperCase());
				}
			} catch (IOException e) {
				module = Module.IOTBROKER;
			}
		}
		IotBrokerAuditEvent event = new IotBrokerAuditEvent();
		event.setId(UUID.randomUUID().toString());
		event.setModule(module != null ? module : Module.IOTBROKER);
		event.setType(EventType.IOTBROKER);
		event.setOperationType(operationType.name());
		event.setResultOperation(ResultOperationType.SUCCESS);
		event.setMessage(messageText);
		event.setGatewayInfo(info);
		Date today = new Date();
		event.setTimeStamp(today.getTime());
		event.setFormatedTimeStamp(CalendarUtil.builder().build().convert(today));
		return event;
	}

}
