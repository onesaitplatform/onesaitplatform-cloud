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

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyIndicationMessage;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageDirection;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageTypes;
import com.minsait.onesait.platform.config.model.SuscriptionNotificationsModel;
import com.minsait.onesait.platform.config.repository.SuscriptionModelRepository;
import com.minsait.onesait.platform.iotbroker.processor.GatewayNotifier;
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
	GatewayNotifier notifier;
	@Autowired
	SuscriptionModelRepository repository;
	@Autowired
	ObjectMapper mapper;

	@RequestMapping(value = "/advice", method = RequestMethod.POST)
	public OperationResultModel create(@RequestBody NotificationCompositeModel notification) {
		final OperationResultModel model = new OperationResultModel();

		model.setErrorCode("");
		model.setMessage("");
		model.setOperation(notification.getOperationResultModel().getOperation());
		model.setResult(notification.getOperationResultModel().getResult());
		model.setStatus(false);

		final SuscriptionNotificationsModel suscription = repository
				.findAllBySuscriptionId(notification.getNotificationEntityId());

		try {

			final SSAPMessage<SSAPBodyIndicationMessage> indication = new SSAPMessage<>();
			indication.setDirection(SSAPMessageDirection.REQUEST);
			indication.setMessageType(SSAPMessageTypes.INDICATION);
			indication.setSessionKey(suscription.getSessionKey());
			indication.setBody(new SSAPBodyIndicationMessage());

			JsonNode data;
			final String body = notification.getNotificationModel().getOperationModel().getBody();
			// final String body = notification.getOperationResultModel().getResult();
			if (StringUtils.isEmpty(body)) {
				createErrorResponse(notification, "Blank notification NOT PROCESSING");
			}

			data = mapper.readTree(body);
			indication.getBody().setData(data);

			indication.getBody().setOntology(notification.getNotificationModel().getOperationModel().getOntologyName());
			indication.getBody().setQuery("");
			indication.getBody().setSubsciptionId(notification.getNotificationEntityId());

			notifier.notify(indication);

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
