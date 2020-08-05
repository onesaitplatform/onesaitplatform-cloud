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
package com.minsait.onesait.platform.iotbroker.processor.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.commons.model.MultiDocumentOperationResult;
import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyQueryMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyReturnMessage;
import com.minsait.onesait.platform.comms.protocol.body.parent.SSAPBodyMessage;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPErrorCode;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageTypes;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPQueryType;
import com.minsait.onesait.platform.iotbroker.common.MessageException;
import com.minsait.onesait.platform.iotbroker.common.exception.SSAPProcessorException;
import com.minsait.onesait.platform.iotbroker.common.util.SSAPUtils;
import com.minsait.onesait.platform.iotbroker.plugable.impl.security.SecurityPluginManager;
import com.minsait.onesait.platform.iotbroker.plugable.interfaces.gateway.GatewayInfo;
import com.minsait.onesait.platform.iotbroker.processor.MessageTypeProcessor;
import com.minsait.onesait.platform.multitenant.config.model.IoTSession;
import com.minsait.onesait.platform.router.service.app.model.NotificationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.QueryType;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.Source;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.service.RouterService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class QueryProcessor implements MessageTypeProcessor {
	@Autowired
	private RouterService routerService;
	@Autowired
	ObjectMapper objectMapper;
	@Autowired
	SecurityPluginManager securityPluginManager;

	@Override
	public SSAPMessage<SSAPBodyReturnMessage> process(SSAPMessage<? extends SSAPBodyMessage> message, GatewayInfo info) {

		final SSAPMessage<SSAPBodyQueryMessage> queryMessage = (SSAPMessage<SSAPBodyQueryMessage>) message;
		SSAPMessage<SSAPBodyReturnMessage> responseMessage = new SSAPMessage<>();
		responseMessage.setBody(new SSAPBodyReturnMessage());
		responseMessage.getBody().setOk(true);
		final Optional<IoTSession> session = securityPluginManager.getSession(queryMessage.getSessionKey());

		String user = null;

		if (session.isPresent()) {
			user = session.get().getUserID();
		}

		QueryType type;
		if (SSAPQueryType.SQL.equals(queryMessage.getBody().getQueryType())) {
			type = OperationModel.QueryType.SQL;
		} else {
			type = QueryType.valueOf(queryMessage.getBody().getQueryType().name());
		}

		final OperationModel model = OperationModel
				.builder(queryMessage.getBody().getOntology(), OperationType.QUERY, user, Source.IOTBROKER)
				.body(queryMessage.getBody().getQuery()).queryType(type).build();

		final NotificationModel modelNotification = new NotificationModel();
		modelNotification.setOperationModel(model);

		OperationResultModel result;
		String responseStr = null;
		String messageStr = null;
		try {
			result = routerService.query(modelNotification);
			responseStr = result.getResult();
			messageStr = result.getMessage();

			if (SSAPQueryType.SQL.equals(queryMessage.getBody().getQueryType())
					&& queryMessage.getBody().getQuery().trim().toLowerCase().startsWith("update")) {// Update

				final MultiDocumentOperationResult multidocument = MultiDocumentOperationResult.fromString(responseStr);

				final String response = String.format("{\"nModified\":%s }", multidocument.getCount());
				responseMessage.getBody().setData(objectMapper.readTree(response));
			} else if (SSAPQueryType.SQL.equals(queryMessage.getBody().getQueryType())
					&& queryMessage.getBody().getQuery().trim().toLowerCase().startsWith("delete")) {// Delete
				final MultiDocumentOperationResult multidocument = MultiDocumentOperationResult.fromString(responseStr);
				final String response = String.format("{\"nDeleted\":%s }", multidocument.getCount());
				responseMessage.getBody().setData(objectMapper.readTree(response));
			} else {
				responseMessage.getBody().setData(objectMapper.readTree(responseStr));
			}

			if (session.isPresent()) {
				responseMessage.setSessionKey(session.get().getSessionKey());
			}

		} catch (final Exception e) {
			log.error("Error proccesing query", e);

			final String error = MessageException.ERR_DATABASE;
			responseMessage = SSAPUtils.generateErrorMessage(message, SSAPErrorCode.PROCESSOR, error);
			if (messageStr != null) {
				responseMessage.getBody().setError(messageStr);
			}
		}

		return responseMessage;
	}

	@Override
	public List<SSAPMessageTypes> getMessageTypes() {
		return Collections.singletonList(SSAPMessageTypes.QUERY);
	}

	@Override
	public boolean validateMessage(SSAPMessage<? extends SSAPBodyMessage> message) {
		final SSAPMessage<SSAPBodyQueryMessage> queryMessage = (SSAPMessage<SSAPBodyQueryMessage>) message;

		if (StringUtils.isEmpty(queryMessage.getBody().getQuery())) {
			throw new SSAPProcessorException(
					String.format(MessageException.ERR_FIELD_IS_MANDATORY, "query", message.getMessageType().name()));
		}

		if (queryMessage.getBody().getQueryType() == null) {
			throw new SSAPProcessorException(String.format(MessageException.ERR_FIELD_IS_MANDATORY, "queryType",
					message.getMessageType().name()));
		}

		return true;
	}

}
