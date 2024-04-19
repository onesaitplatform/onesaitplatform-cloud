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
package com.minsait.onesait.platform.iotbroker.processor.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.commons.model.MultiDocumentOperationResult;
import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyInsertMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyReturnMessage;
import com.minsait.onesait.platform.comms.protocol.body.parent.SSAPBodyMessage;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageDirection;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageTypes;
import com.minsait.onesait.platform.iotbroker.common.MessageException;
import com.minsait.onesait.platform.iotbroker.common.exception.AuthorizationException;
import com.minsait.onesait.platform.iotbroker.common.exception.SSAPProcessorException;
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

@Slf4j
@Component
public class InsertProcessor implements MessageTypeProcessor {

	@Autowired
	private RouterService routerService;

	@Autowired
	ObjectMapper objectMapper;

	@Override
	public SSAPMessage<SSAPBodyReturnMessage> process(SSAPMessage<? extends SSAPBodyMessage> message, GatewayInfo info, Optional<IoTSession> session) {
		@SuppressWarnings("unchecked")
		final SSAPMessage<SSAPBodyInsertMessage> insertMessage = (SSAPMessage<SSAPBodyInsertMessage>) message;

		String user = null;
		String deviceTemplate = null;
		String device = null;
		if (session.isPresent()) {
			user = session.get().getUserID();
			deviceTemplate = session.get().getClientPlatform();
			device = session.get().getDevice();
		}

		final OperationModel model = OperationModel
				.builder(insertMessage.getBody().getOntology(), OperationType.POST, user, Source.IOTBROKER)
				.body(insertMessage.getBody().getData().toString()).queryType(QueryType.NATIVE)
				.deviceTemplate(deviceTemplate).device(device).clientSession(insertMessage.getSessionKey())
				.clientConnection("").build();

		final NotificationModel modelNotification = new NotificationModel();
		modelNotification.setOperationModel(model);

		SSAPMessage<SSAPBodyReturnMessage> responseMessage = new SSAPMessage<>();
		try {
			if (message.getTransactionId() == null) {
				responseMessage = this.processNoTransactionalInsert(modelNotification, insertMessage);
			} else {
				modelNotification.getOperationModel().setTransactionId(message.getTransactionId());
				responseMessage = this.processTransactionalInsert(modelNotification, insertMessage);
			}
		} catch (final AuthorizationException e1) {
			log.error("Error processing Insert", e1);
			throw e1;

		} catch (final Exception e1) {
			log.error("Error processing Insert", e1);
			throw new SSAPProcessorException(e1.getMessage());
		}

		return responseMessage;
	}

	@Override
	public List<SSAPMessageTypes> getMessageTypes() {
		return Collections.singletonList(SSAPMessageTypes.INSERT);
	}

	@Override
	public boolean validateMessage(SSAPMessage<? extends SSAPBodyMessage> message) {

		final SSAPMessage<SSAPBodyInsertMessage> insertMessage = (SSAPMessage<SSAPBodyInsertMessage>) message;

		if (insertMessage.getBody().getData() == null || insertMessage.getBody().getData().isNull()) {
			log.error("Error validating message");
			throw new SSAPProcessorException(String.format(MessageException.ERR_FIELD_IS_MANDATORY, "data",
					insertMessage.getMessageType().name()));
		}
		return true;
	}

	private SSAPMessage<SSAPBodyReturnMessage> processNoTransactionalInsert(NotificationModel modelNotification,
			SSAPMessage<SSAPBodyInsertMessage> insertMessage) throws Exception {
		final SSAPMessage<SSAPBodyReturnMessage> responseMessage = new SSAPMessage<>();

		final OperationResultModel result = routerService.insert(modelNotification);
		if (!result.getResult().equals("ERROR")) {

			String repositoryResponse = result.getResult();

			responseMessage.setDirection(SSAPMessageDirection.RESPONSE);
			responseMessage.setMessageId(insertMessage.getMessageId());
			responseMessage.setMessageType(insertMessage.getMessageType());
			responseMessage.setSessionKey(insertMessage.getSessionKey());
			responseMessage.setBody(new SSAPBodyReturnMessage());
			responseMessage.getBody().setOk(true);

			final MultiDocumentOperationResult multidocument = MultiDocumentOperationResult.fromString(repositoryResponse);
			final long multidocumentCount = multidocument.getCount();
			final JSONObject jsonObject = new JSONObject();
			if(multidocumentCount == 1){
				if(multidocument.getIds().isEmpty()){
					jsonObject.put("nInserted", multidocumentCount);
				} else {
					jsonObject.put("id", multidocument.getIds().get(0));
				}
			} else if (multidocumentCount > 1){
				jsonObject.put("nInserted", multidocumentCount);
				if(!multidocument.getIds().isEmpty()){
					jsonObject.put("inserted", new JSONArray(multidocument.getIds()));
				}
			}
			responseMessage.getBody().setData(objectMapper.readTree(jsonObject.toString()));
		} else {
			throw new SSAPProcessorException(result.getMessage());
		}
		return responseMessage;
	}

	private SSAPMessage<SSAPBodyReturnMessage> processTransactionalInsert(NotificationModel modelNotification,
			SSAPMessage<SSAPBodyInsertMessage> insertMessage) throws Exception {
		final SSAPMessage<SSAPBodyReturnMessage> responseMessage = new SSAPMessage<>();

		final OperationResultModel result = routerService.insert(modelNotification);

		if (result.isStatus()) {
			String sequenceNumber = result.getResult();

			responseMessage.setDirection(SSAPMessageDirection.RESPONSE);
			responseMessage.setMessageId(insertMessage.getMessageId());
			responseMessage.setMessageType(insertMessage.getMessageType());
			responseMessage.setSessionKey(insertMessage.getSessionKey());
			responseMessage.setBody(new SSAPBodyReturnMessage());
			responseMessage.getBody().setOk(true);
			responseMessage.getBody().setData(objectMapper.readTree("{\"id\":\"" + sequenceNumber + "\"}"));
		} else {
			throw new SSAPProcessorException(result.getMessage());
		}

		return responseMessage;
	}

}
