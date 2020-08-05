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

import com.mongodb.util.JSON;
import oracle.net.aso.n;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.commons.model.MultiDocumentOperationResult;
import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyReturnMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyUpdateByIdMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyUpdateMessage;
import com.minsait.onesait.platform.comms.protocol.body.parent.SSAPBodyMessage;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPErrorCode;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageTypes;
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
public class UpdateProcessor implements MessageTypeProcessor {

	@Autowired
	private RouterService routerService;
	@Autowired
	ObjectMapper objectMapper;
	@Autowired
	SecurityPluginManager securityPluginManager;

	@Override
	public SSAPMessage<SSAPBodyReturnMessage> process(SSAPMessage<? extends SSAPBodyMessage> message, GatewayInfo info) {

		if (SSAPMessageTypes.UPDATE.equals(message.getMessageType())) {
			final SSAPMessage<SSAPBodyUpdateMessage> processUpdate = (SSAPMessage<SSAPBodyUpdateMessage>) message;
			return processUpdate(processUpdate);
		}

		if (SSAPMessageTypes.UPDATE_BY_ID.equals(message.getMessageType())) {
			final SSAPMessage<SSAPBodyUpdateByIdMessage> processUpdate = (SSAPMessage<SSAPBodyUpdateByIdMessage>) message;
			return processUpdateById(processUpdate);
		}

		SSAPMessage<SSAPBodyReturnMessage> responseMessage;
		responseMessage = SSAPUtils.generateErrorMessage(message, SSAPErrorCode.PROCESSOR, "Mesage not supported");

		return responseMessage;

	}

	private SSAPMessage<SSAPBodyReturnMessage> processUpdate(SSAPMessage<SSAPBodyUpdateMessage> updateMessage) {

		SSAPMessage<SSAPBodyReturnMessage> responseMessage = new SSAPMessage<>();
		responseMessage.setBody(new SSAPBodyReturnMessage());
		responseMessage.getBody().setOk(true);

		final String sessionkey = updateMessage.getSessionKey();
		final Optional<IoTSession> session = securityPluginManager.getSession(sessionkey);

		String user = null;
		String deviceTemplate = null;

		if (session.isPresent()) {
			user = session.get().getUserID();
			deviceTemplate = session.get().getClientPlatform();
		}

		final OperationModel model = OperationModel
				.builder(updateMessage.getBody().getOntology(), OperationType.PUT, user, Source.IOTBROKER,
						updateMessage.includeIds())
				.deviceTemplate(deviceTemplate).queryType(QueryType.NATIVE).body(updateMessage.getBody().getQuery())
				.build();

		model.setClientSession(sessionkey);

		final String transactionId = updateMessage.getTransactionId();

		OperationResultModel result;
		String responseStr = null;
		String messageStr = null;
		try {
			if (null == transactionId) {
				final NotificationModel modelNotification = new NotificationModel();
				modelNotification.setOperationModel(model);

				result = routerService.update(modelNotification);
				messageStr = result.getMessage();
				responseStr = result.getResult();

				final MultiDocumentOperationResult multidocument = MultiDocumentOperationResult.fromString(responseStr);
				final JSONObject jsonObject = new JSONObject();
				jsonObject.put("nModified", multidocument.getCount());
				if(multidocument.getStrIds() != null && updateMessage.includeIds()){
					jsonObject.put("modified", new JSONArray(multidocument.getStrIds()));
				}
				responseMessage.getBody().setData(objectMapper.readTree(jsonObject.toString()));
			} else {
				model.setTransactionId(transactionId);

				final NotificationModel modelNotification = new NotificationModel();
				modelNotification.setOperationModel(model);

				result = routerService.update(modelNotification);
				messageStr = result.getMessage();

				String sequenceNumber = result.getResult();

				responseMessage.getBody().setData(objectMapper.readTree("{\"id\":\"" + sequenceNumber + "\"}"));
			}
		} catch (final Exception e) {
			log.error("Error in process:" + e.getMessage());
			final String error = MessageException.ERR_DATABASE;
			responseMessage = SSAPUtils.generateErrorMessage(updateMessage, SSAPErrorCode.PROCESSOR, error);
			if (messageStr != null) {
				responseMessage.getBody().setError(messageStr);
			}
		}

		return responseMessage;
	}

	private SSAPMessage<SSAPBodyReturnMessage> processUpdateById(SSAPMessage<SSAPBodyUpdateByIdMessage> updateMessage) {

		SSAPMessage<SSAPBodyReturnMessage> responseMessage = new SSAPMessage<>();
		responseMessage.setBody(new SSAPBodyReturnMessage());
		responseMessage.getBody().setOk(true);
		final Optional<IoTSession> session = securityPluginManager.getSession(updateMessage.getSessionKey());

		String user = null;
		String deviceTemplate = null;

		if (session.isPresent()) {
			user = session.get().getUserID();
			deviceTemplate = session.get().getClientPlatform();
		}

		final OperationModel model = OperationModel
				.builder(updateMessage.getBody().getOntology(), OperationType.PUT, user, Source.IOTBROKER)
				.objectId(updateMessage.getBody().getId()).queryType(QueryType.NATIVE)
				.body(updateMessage.getBody().getData().toString()).deviceTemplate(deviceTemplate).build();

		OperationResultModel result;
		String responseStr = null;
		String messageStr = null;

		model.setClientSession(updateMessage.getSessionKey());

		final String transactionId = updateMessage.getTransactionId();

		try {
			if (null == transactionId) {
				final NotificationModel modelNotification = new NotificationModel();
				modelNotification.setOperationModel(model);

				result = routerService.update(modelNotification);
				responseStr = result.getResult();
				messageStr = result.getMessage();

				if (messageStr.equals("OK")) {
					if (updateMessage.includeIds()) {
						final JSONObject jsonResponse = new JSONObject();
						if (responseStr != null && !responseStr.isEmpty() && !responseStr.equals("[]")) {
							jsonResponse.put("nModified", 1);
							final JSONArray updatedId = new JSONArray();
							updatedId.put(updateMessage.getBody().getId());
							jsonResponse.put("modified", updatedId);
						} else {
							jsonResponse.put("nModified", 0);
						}
						responseMessage.getBody().setData(objectMapper.readTree(jsonResponse.toString()));
					} else {
						responseMessage.getBody().setData(objectMapper.readTree(responseStr));
					}
				} else {
					final String error = MessageException.ERR_DATABASE;
					responseMessage = SSAPUtils.generateErrorMessage(updateMessage, SSAPErrorCode.PROCESSOR, error);
					if (messageStr != null) {
						responseMessage.getBody().setError(messageStr);
					}
				}
			} else {
				model.setTransactionId(transactionId);

				final NotificationModel modelNotification = new NotificationModel();
				modelNotification.setOperationModel(model);

				result = routerService.update(modelNotification);
				messageStr = result.getMessage();
				String sequenceNumber = result.getResult();

				responseMessage.getBody().setData(objectMapper.readTree("{\"id\":\"" + sequenceNumber + "\"}"));
			}

		} catch (final Exception e) {
			log.error("Error in process:" + e.getMessage());
			final String error = MessageException.ERR_DATABASE;
			responseMessage = SSAPUtils.generateErrorMessage(updateMessage, SSAPErrorCode.PROCESSOR, error);
			if (messageStr != null) {
				responseMessage.getBody().setError(messageStr);
			}

		}

		return responseMessage;
	}

	@Override
	public boolean validateMessage(SSAPMessage<? extends SSAPBodyMessage> message) {

		if (SSAPMessageTypes.UPDATE.equals(message.getMessageType())) {
			final SSAPMessage<SSAPBodyUpdateMessage> updateMessage = (SSAPMessage<SSAPBodyUpdateMessage>) message;
			return validateMessageUpdate(updateMessage);
		}

		if (SSAPMessageTypes.UPDATE_BY_ID.equals(message.getMessageType())) {
			final SSAPMessage<SSAPBodyUpdateByIdMessage> updateMessage = (SSAPMessage<SSAPBodyUpdateByIdMessage>) message;
			return validateMessageUpdateById(updateMessage);
		}

		return false;
	}

	private boolean validateMessageUpdate(SSAPMessage<SSAPBodyUpdateMessage> updateMessage) {
		if (StringUtils.isEmpty(updateMessage.getBody().getQuery())) {
			log.error("Error quey field");
			throw new SSAPProcessorException(String.format(MessageException.ERR_FIELD_IS_MANDATORY, "quey",
					updateMessage.getMessageType().name()));
		}
		return true;
	}

	private boolean validateMessageUpdateById(SSAPMessage<SSAPBodyUpdateByIdMessage> updateMessage) {
		if (StringUtils.isEmpty(updateMessage.getBody().getId())) {
			log.error("Error id field");
			throw new SSAPProcessorException(String.format(MessageException.ERR_FIELD_IS_MANDATORY, "id",
					updateMessage.getMessageType().name()));
		}
		if (updateMessage.getBody().getData() == null || updateMessage.getBody().getData().isNull()) {
			log.error("Error data field");
			throw new SSAPProcessorException(String.format(MessageException.ERR_FIELD_IS_MANDATORY, "data",
					updateMessage.getMessageType().name()));
		}
		return true;
	}

	@Override
	public List<SSAPMessageTypes> getMessageTypes() {
		final List<SSAPMessageTypes> types = new ArrayList<>();
		types.add(SSAPMessageTypes.UPDATE);
		types.add(SSAPMessageTypes.UPDATE_BY_ID);
		return types;
	}

}
