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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.commons.model.MultiDocumentOperationResult;
import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyDeleteByIdMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyDeleteMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyReturnMessage;
import com.minsait.onesait.platform.comms.protocol.body.parent.SSAPBodyMessage;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPErrorCode;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageTypes;
import com.minsait.onesait.platform.iotbroker.common.MessageException;
import com.minsait.onesait.platform.iotbroker.common.exception.SSAPProcessorException;
import com.minsait.onesait.platform.iotbroker.common.util.SSAPUtils;
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
public class DeleteProcessor implements MessageTypeProcessor {

	@Autowired
	private RouterService routerService;
	@Autowired
	ObjectMapper objectMapper;

	@Override
	public SSAPMessage<SSAPBodyReturnMessage> process(SSAPMessage<? extends SSAPBodyMessage> message, GatewayInfo info, Optional<IoTSession> session) {

		if (SSAPMessageTypes.DELETE.equals(message.getMessageType())) {
			final SSAPMessage<SSAPBodyDeleteMessage> deleteMessage = (SSAPMessage<SSAPBodyDeleteMessage>) message;
			return processDelete(deleteMessage, session);
		}

		if (SSAPMessageTypes.DELETE_BY_ID.equals(message.getMessageType())) {
			final SSAPMessage<SSAPBodyDeleteByIdMessage> deleteMessage = (SSAPMessage<SSAPBodyDeleteByIdMessage>) message;
			return processDeleteById(deleteMessage, session);
		}

		SSAPMessage<SSAPBodyReturnMessage> responseMessage;
		responseMessage = SSAPUtils.generateErrorMessage(message, SSAPErrorCode.PROCESSOR, "Mesage not supported");
		return responseMessage;
	}

	private SSAPMessage<SSAPBodyReturnMessage> processDeleteById(SSAPMessage<SSAPBodyDeleteByIdMessage> message, Optional<IoTSession> session) {
		SSAPMessage<SSAPBodyReturnMessage> responseMessage = new SSAPMessage<>();
		responseMessage.setBody(new SSAPBodyReturnMessage());
		responseMessage.getBody().setOk(true);

		final String user = session.isPresent() ? session.get().getUserID() : null;
		final String deviceTemplate = session.isPresent() ? session.get().getClientPlatform() : null;
		final OperationModel model = OperationModel
				.builder(message.getBody().getOntology(), OperationType.DELETE, user, OperationModel.Source.IOTBROKER)
				.objectId(message.getBody().getId()).queryType(QueryType.NATIVE).deviceTemplate(deviceTemplate).build();

		model.setClientSession(message.getSessionKey());

		final String transactionId = message.getTransactionId();

		OperationResultModel result;
		String responseStr = null;
		String messageStr = null;
		try {
			if (null == transactionId) {
				final NotificationModel modelNotification = new NotificationModel();
				modelNotification.setOperationModel(model);

				result = routerService.delete(modelNotification);
				responseStr = result.getResult();
				messageStr = result.getMessage();

				if (messageStr.equals("OK")) {
					if (message.includeIds()) {
						final JSONObject jsonObject = new JSONObject(responseStr);
						final int nDeleted = jsonObject.getInt("count");
						final String deleted = nDeleted > 0 ? "[\"" + message.getBody().getId() + "\"]" : "[]";

						final String response = String.format("{\"nDeleted\":%s, \"deleted\":%s}", nDeleted, deleted);

						responseMessage.getBody().setData(objectMapper.readTree(response));
					} else {
						final String response = String.format("{\"nDeleted\":%s}", responseStr);
						responseMessage.getBody().setData(objectMapper.readTree(response));
					}

				} else {
					final String error = MessageException.ERR_DATABASE;
					responseMessage = SSAPUtils.generateErrorMessage(message, SSAPErrorCode.PROCESSOR, error);
					if (messageStr != null) {
						responseMessage.getBody().setError(messageStr);
					}

				}
			} else {
				model.setTransactionId(transactionId);

				final NotificationModel modelNotification = new NotificationModel();
				modelNotification.setOperationModel(model);

				result = routerService.delete(modelNotification);
				messageStr = result.getMessage();
				String sequenceNumber = result.getResult();

				responseMessage.getBody().setData(objectMapper.readTree("{\"id\":\"" + sequenceNumber + "\"}"));

			}

		} catch (final Exception e) {
			log.error("Error deleting by ObjectId", e);

			final String error = MessageException.ERR_DATABASE;
			responseMessage = SSAPUtils.generateErrorMessage(message, SSAPErrorCode.PROCESSOR, error);
			if (messageStr != null) {
				responseMessage.getBody().setError(messageStr);
			}
		}

		return responseMessage;
	}

	private SSAPMessage<SSAPBodyReturnMessage> processDelete(SSAPMessage<SSAPBodyDeleteMessage> message, Optional<IoTSession> session) {

		SSAPMessage<SSAPBodyReturnMessage> responseMessage = new SSAPMessage<>();
		responseMessage.setBody(new SSAPBodyReturnMessage());
		responseMessage.getBody().setOk(true);

		final String user = session.isPresent() ? session.get().getUserID() : null;
		final String deviceTemplate = session.isPresent() ? session.get().getClientPlatform() : null;
		final OperationModel model = OperationModel
				.builder(message.getBody().getOntology(), OperationType.DELETE, user, Source.IOTBROKER,
						message.includeIds())
				.queryType(QueryType.NATIVE).body(message.getBody().getQuery()).deviceTemplate(deviceTemplate).build();

		model.setClientSession(message.getSessionKey());

		OperationResultModel result;
		String responseStr = null;
		String messageStr = null;

		final String transactionId = message.getTransactionId();
		try {
			if (null == transactionId) {
				final NotificationModel modelNotification = new NotificationModel();
				modelNotification.setOperationModel(model);

				result = routerService.delete(modelNotification);
				messageStr = result.getMessage();
				responseStr = result.getResult();

				final MultiDocumentOperationResult multidocument = MultiDocumentOperationResult.fromString(responseStr);

				final String response;

				if (message.includeIds()) {
					response = String.format("{\"nDeleted\":%s, \"deleted\":%s}", multidocument.getCount(),
							multidocument.getStrIds());
				} else {
					response = String.format("{\"nDeleted\":%s}", multidocument.getCount());
				}

				responseMessage.getBody().setData(objectMapper.readTree(response));

			} else {
				model.setTransactionId(transactionId);

				final NotificationModel modelNotification = new NotificationModel();
				modelNotification.setOperationModel(model);

				result = routerService.delete(modelNotification);
				messageStr = result.getMessage();
				String sequenceNumber = result.getResult();

				responseMessage.getBody().setData(objectMapper.readTree("{\"id\":\"" + sequenceNumber + "\"}"));
			}

		} catch (final Exception e) {
			log.error("Error in processDelete:" + e.getMessage());

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
		final List<SSAPMessageTypes> types = new ArrayList<>();
		types.add(SSAPMessageTypes.DELETE);
		types.add(SSAPMessageTypes.DELETE_BY_ID);
		return types;
	}

	@Override
	public boolean validateMessage(SSAPMessage<? extends SSAPBodyMessage> message) {

		if (SSAPMessageTypes.DELETE.equals(message.getMessageType())) {
			final SSAPMessage<SSAPBodyDeleteMessage> deleteMessage = (SSAPMessage<SSAPBodyDeleteMessage>) message;
			return validateDelete(deleteMessage);
		}

		if (SSAPMessageTypes.DELETE_BY_ID.equals(message.getMessageType())) {
			final SSAPMessage<SSAPBodyDeleteByIdMessage> deleteMessage = (SSAPMessage<SSAPBodyDeleteByIdMessage>) message;
			return validateDeleteById(deleteMessage);
		}

		return false;
	}

	private boolean validateDeleteById(SSAPMessage<SSAPBodyDeleteByIdMessage> message) {
		if (StringUtils.isEmpty(message.getBody().getId())) {
			log.error("Error in validateDeleteById");
			throw new SSAPProcessorException(
					String.format(MessageException.ERR_FIELD_IS_MANDATORY, "id", message.getMessageType().name()));
		}

		return true;
	}

	private boolean validateDelete(SSAPMessage<SSAPBodyDeleteMessage> message) {
		if (StringUtils.isEmpty(message.getBody().getQuery())) {
			log.error("Error in validateDelete");
			throw new SSAPProcessorException(
					String.format(MessageException.ERR_FIELD_IS_MANDATORY, "quey", message.getMessageType().name()));
		}
		return true;
	}

}
