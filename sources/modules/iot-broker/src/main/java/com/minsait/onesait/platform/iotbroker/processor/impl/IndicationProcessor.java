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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyIndicationMessage;
import com.minsait.onesait.platform.comms.protocol.util.SSAPMessageGenerator;
import com.minsait.onesait.platform.iotbroker.processor.GatewayNotifier;
import com.minsait.onesait.platform.iotbroker.subscription.model.SubscriptorClient;
import com.minsait.onesait.platform.iotbroker.subscription.notificator.RestNotificatorImpl;
import com.minsait.onesait.platform.router.service.app.model.NotificationCompositeModel;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(path = "/")
@EnableAutoConfiguration
@CrossOrigin(origins = "*")
public class IndicationProcessor {

	@Autowired
	GatewayNotifier mqttNotifier;
	@Autowired
	ObjectMapper mapper;
	@Autowired
	RestNotificatorImpl restNotifier;

	@PostMapping(value = "/advice")
	public OperationResultModel create(@RequestBody NotificationCompositeModel notification) {
		final OperationResultModel model = new OperationResultModel();

		try {

			model.setStatus(true);
			model.setErrorCode("");
			model.setResult("");

			String dataToNotify = notification.getNotificationModel().getOperationModel().getBody();
			SubscriptorClient client = mapper.readValue(
					notification.getNotificationModel().getOperationModel().getClientConnection(),
					SubscriptorClient.class);

			if (client.getSubscriptionGW().equals("rest_gateway")){
				log.info("Client {} is going to be notified via REST by rest_gateway.", client.getClientId());
				Boolean isOk = restNotifier.notify(dataToNotify, client);
				if (!isOk) {
					model.setStatus(false);
					model.setMessage("Impossible to notify subscriber");
					model.setResult("Error");
				}
			} else if (client.getSubscriptionGW().equals("moquette_gateway")) {
				log.info("Client {} is going to be notified via MQTT.", client.getClientId());
				SSAPMessage<SSAPBodyIndicationMessage> response = SSAPMessageGenerator
						.generateResponseIndicationMessage(client.getSubscriptionId(), dataToNotify, client.getSessionKey());
				mqttNotifier.notify(client.getSubscriptionGW(), response);
			} else if (client.getSubscriptionGW().equals("stomp_gateway")) {

				log.info("Client {} is going to be notified via WEBSOCKET.", client.getClientId());
				SSAPMessage<SSAPBodyIndicationMessage> response = SSAPMessageGenerator
						.generateResponseIndicationMessage(client.getSubscriptionId(), dataToNotify, client.getSessionKey());
				mqttNotifier.notify(client.getSubscriptionGW(), response);
				
			} else {
				log.error("Protocol not supported", client.getSubscriptionGW());
				createErrorResponse(notification, "Protocol not supported" + client.getSubscriptionGW());
			}

		} catch (final IOException e) {
			log.error("Indication result can't be process", e.getMessage());
			createErrorResponse(notification, e.getMessage());
			return model;
		}

		return model;
	}

	private OperationResultModel createErrorResponse(NotificationCompositeModel notification, String message) {
		final OperationResultModel model = new OperationResultModel();
		model.setErrorCode("ERROR");
		model.setMessage(message);
		model.setOperation(notification.getOperationResultModel().getOperation());
		model.setResult(notification.getOperationResultModel().getResult());
		model.setStatus(false);
		return model;
	}

}