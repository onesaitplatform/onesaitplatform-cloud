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
package com.minsait.onesait.platform.iotbroker.processor.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyReturnMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyUnsubscribeMessage;
import com.minsait.onesait.platform.comms.protocol.body.parent.SSAPBodyMessage;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPErrorCode;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageDirection;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageTypes;
import com.minsait.onesait.platform.iotbroker.common.MessageException;
import com.minsait.onesait.platform.iotbroker.common.exception.SSAPProcessorException;
import com.minsait.onesait.platform.iotbroker.common.util.SSAPUtils;
import com.minsait.onesait.platform.iotbroker.plugable.interfaces.gateway.GatewayInfo;
import com.minsait.onesait.platform.iotbroker.processor.MessageTypeProcessor;
import com.minsait.onesait.platform.multitenant.config.model.IoTSession;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.model.SubscriptionModel;
import com.minsait.onesait.platform.router.service.app.service.RouterService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UnsubscribeProcessor implements MessageTypeProcessor {

	@Autowired
	private RouterService routerService;
	@Autowired
	ObjectMapper objectMapper;

	@Override
	public SSAPMessage<SSAPBodyReturnMessage> process(SSAPMessage<? extends SSAPBodyMessage> message, GatewayInfo info,
			Optional<IoTSession> session) {
		final SSAPMessage<SSAPBodyUnsubscribeMessage> unsubscribeMessage = (SSAPMessage<SSAPBodyUnsubscribeMessage>) message;
		SSAPMessage<SSAPBodyReturnMessage> response = new SSAPMessage<>();
		response.setBody(new SSAPBodyReturnMessage());

		final SubscriptionModel model = new SubscriptionModel();
		model.setSuscriptionId(unsubscribeMessage.getBody().getSubscriptionId());
		model.setOperationType(SubscriptionModel.OperationType.UNSUBSCRIBE);
		model.setSessionKey(unsubscribeMessage.getSessionKey());
		session.ifPresent(s -> model.setUser(s.getUserID()));

		OperationResultModel routerResponse = null;
		try {
			routerResponse = routerService.unsubscribe(model);
		} catch (final Exception e1) {
			log.error("Error in process:{}", e1.getMessage());
			response = SSAPUtils.generateErrorMessage(unsubscribeMessage, SSAPErrorCode.PROCESSOR, e1.getMessage());
			return response;
		}
		final String errorCode = routerResponse.getErrorCode();
		final String messageResponse = routerResponse.getMessage();
		final String operation = routerResponse.getOperation();
		final String result = routerResponse.getResult();
		log.error("{} {} {} {}", errorCode, messageResponse, operation, result);

		if (StringUtils.hasText(routerResponse.getErrorCode())) {
			response = SSAPUtils.generateErrorMessage(unsubscribeMessage, SSAPErrorCode.PROCESSOR,
					routerResponse.getErrorCode());
			return response;

		}
		response.setDirection(SSAPMessageDirection.RESPONSE);
		response.setMessageType(SSAPMessageTypes.UNSUBSCRIBE);
		response.setSessionKey(unsubscribeMessage.getSessionKey());
		response.getBody().setOk(true);
		response.getBody().setError(routerResponse.getErrorCode());
		final String dataStr = "{\"message\": \"" + routerResponse.getMessage() + "\"}";
		JsonNode data;
		try {
			data = objectMapper.readTree(dataStr);
			response.getBody().setData(data);
		} catch (final IOException e) {
			log.error("Error in process:{}", e.getMessage());
			response = SSAPUtils.generateErrorMessage(unsubscribeMessage, SSAPErrorCode.PROCESSOR, e.getMessage());
			return response;
		}

		return response;

	}

	@Override
	public List<SSAPMessageTypes> getMessageTypes() {
		return Collections.singletonList(SSAPMessageTypes.UNSUBSCRIBE);
	}

	@Override
	public boolean validateMessage(SSAPMessage<? extends SSAPBodyMessage> message) {
		final SSAPMessage<SSAPBodyUnsubscribeMessage> unsubscribeMessage = (SSAPMessage<SSAPBodyUnsubscribeMessage>) message;

		if (!StringUtils.hasText(unsubscribeMessage.getBody().getSubscriptionId())) {
			throw new SSAPProcessorException(String.format(MessageException.ERR_FIELD_IS_MANDATORY, "subscriptionId",
					message.getMessageType().name()));
		}

		return true;
	}

}
