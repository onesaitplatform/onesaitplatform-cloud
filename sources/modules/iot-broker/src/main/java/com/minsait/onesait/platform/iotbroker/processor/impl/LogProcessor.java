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
package com.minsait.onesait.platform.iotbroker.processor.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.minsait.onesait.platform.business.services.ontology.OntologyBusinessService;
import com.minsait.onesait.platform.business.services.ontology.OntologyBusinessServiceException;
import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyLogMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyReturnMessage;
import com.minsait.onesait.platform.comms.protocol.body.parent.SSAPBodyMessage;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageDirection;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageTypes;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.services.client.ClientPlatformService;
import com.minsait.onesait.platform.iotbroker.common.MessageException;
import com.minsait.onesait.platform.iotbroker.common.exception.SSAPProcessorException;
import com.minsait.onesait.platform.iotbroker.plugable.interfaces.gateway.GatewayInfo;
import com.minsait.onesait.platform.iotbroker.processor.DeviceManager;
import com.minsait.onesait.platform.iotbroker.processor.MessageTypeProcessor;
import com.minsait.onesait.platform.multitenant.config.model.IoTSession;
import com.minsait.onesait.platform.router.service.app.model.NotificationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.QueryType;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.Source;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.service.RouterService;

@Component
public class LogProcessor implements MessageTypeProcessor {

	@Autowired
	private ClientPlatformService clientPlatformService;
	@Autowired
	private RouterService routerService;
	@Autowired
	private DeviceManager deviceManager;
	@Autowired
	private OntologyBusinessService ontologyBussinessService;

	@Override
	public SSAPMessage<SSAPBodyReturnMessage> process(SSAPMessage<? extends SSAPBodyMessage> message, GatewayInfo info, Optional<IoTSession> session)
			throws OntologyBusinessServiceException, IOException {
		final SSAPMessage<SSAPBodyLogMessage> logMessage = (SSAPMessage<SSAPBodyLogMessage>) message;
		final SSAPMessage<SSAPBodyReturnMessage> response = new SSAPMessage<>();
		ClientPlatform client = null;
		Ontology ontology = null;
		if (session.isPresent()) {
			client = clientPlatformService.getByIdentification(session.get().getClientPlatform());
			ontology = clientPlatformService.getDeviceLogOntology(client);
			if (client != null && ontology == null) {
				ontology = clientPlatformService.createDeviceLogOntology(client);
				ontologyBussinessService.createOntology(ontology, ontology.getUser().getUserId(), null);
				clientPlatformService.createOntologyRelation(ontology, client);
			}
		}
		if (client != null) {
			final JsonNode instance = deviceManager.createDeviceLog(client, session.get().getDevice(),
					logMessage.getBody());
			final OperationModel model = OperationModel
					.builder(ontology.getIdentification(), OperationType.POST, client.getUser().getUserId(),
							Source.IOTBROKER)
					.body(instance.toString()).queryType(QueryType.NATIVE).deviceTemplate(client.getIdentification())
					.device(session.get().getDevice()).clientSession(logMessage.getSessionKey()).clientConnection("")
					.build();

			final NotificationModel modelNotification = new NotificationModel();
			modelNotification.setOperationModel(model);
			try {
				final OperationResultModel result = routerService.insert(modelNotification);
				if (!result.getResult().equals("ERROR")) {
					response.setDirection(SSAPMessageDirection.RESPONSE);
					response.setMessageId(logMessage.getMessageId());
					response.setMessageType(logMessage.getMessageType());
					response.setSessionKey(logMessage.getSessionKey());
					response.setBody(new SSAPBodyReturnMessage());
					response.getBody().setOk(true);
					response.getBody().setData(instance);
				} else {
					throw new SSAPProcessorException(result.getMessage());
				}

			} catch (final Exception e) {
				throw new SSAPProcessorException("Could not create log: " + e);
			}
		} else
			throw new SSAPProcessorException("Could not retrieve Device, log failed");

		return response;
	}

	@Override
	public List<SSAPMessageTypes> getMessageTypes() {
		return Collections.singletonList(SSAPMessageTypes.LOG);
	}

	@Override
	public boolean validateMessage(SSAPMessage<? extends SSAPBodyMessage> message) {
		final SSAPMessage<SSAPBodyLogMessage> logMessage = (SSAPMessage<SSAPBodyLogMessage>) message;
		if (logMessage.getBody().getMessage().isEmpty() || logMessage.getBody().getLevel() == null
				|| logMessage.getBody().getStatus() == null) {
			throw new SSAPProcessorException(String.format(MessageException.ERR_FIELD_IS_MANDATORY,
					"message, log level, status", message.getMessageType().name()));
		}
		return true;
	}

}
