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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.HazelcastInstance;
import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyReturnMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodySubscribeMessage;
import com.minsait.onesait.platform.comms.protocol.body.parent.SSAPBodyMessage;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPErrorCode;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageDirection;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageTypes;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPQueryType;
import com.minsait.onesait.platform.config.model.IoTSession;
import com.minsait.onesait.platform.config.model.SuscriptionNotificationsModel;
import com.minsait.onesait.platform.config.repository.SuscriptionModelRepository;
import com.minsait.onesait.platform.iotbroker.common.MessageException;
import com.minsait.onesait.platform.iotbroker.common.exception.SSAPProcessorException;
import com.minsait.onesait.platform.iotbroker.common.util.SSAPUtils;
import com.minsait.onesait.platform.iotbroker.plugable.impl.security.SecurityPluginManager;
import com.minsait.onesait.platform.iotbroker.processor.MessageTypeProcessor;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.model.SuscriptionModel;
import com.minsait.onesait.platform.router.service.app.service.RouterSuscriptionService;

import lombok.extern.slf4j.Slf4j;

@Component
@EnableScheduling
@Slf4j
public class SubscribeProcessor implements MessageTypeProcessor {

	@Autowired
	private RouterSuscriptionService routerService;
	@Autowired
	SecurityPluginManager securityPluginManager;
	@Autowired
	ObjectMapper objectMapper;
	@Autowired
	SuscriptionModelRepository repository;

	@Autowired
	HazelcastInstance hazelcastInstance;
	private static final String SUBSCRIPTION_REPOSITORY = "SuscriptionModelRepository";

	@Override
	public SSAPMessage<SSAPBodyReturnMessage> process(SSAPMessage<? extends SSAPBodyMessage> message) {

		@SuppressWarnings("unchecked")
		final SSAPMessage<SSAPBodySubscribeMessage> subscribeMessage = (SSAPMessage<SSAPBodySubscribeMessage>) message;
		SSAPMessage<SSAPBodyReturnMessage> response = new SSAPMessage<>();
		final String subsId = UUID.randomUUID().toString();
		response.setBody(new SSAPBodyReturnMessage());

		final Optional<IoTSession> session = securityPluginManager.getSession(subscribeMessage.getSessionKey());

		final SuscriptionModel model = new SuscriptionModel();
		model.setOntologyName(subscribeMessage.getBody().getOntology());
		model.setOperationType(SuscriptionModel.OperationType.SUSCRIBE);
		model.setQuery(subscribeMessage.getBody().getQuery());

		SuscriptionModel.QueryType qType = SuscriptionModel.QueryType.NATIVE;
		if (SSAPQueryType.SQL.equals(subscribeMessage.getBody().getQueryType())) {
			qType = SuscriptionModel.QueryType.SQLLIKE;
		}
		model.setQueryType(qType);
		model.setSessionKey(subscribeMessage.getSessionKey());

		model.setSuscriptionId(subsId);
		session.ifPresent(s -> model.setUser(s.getUserID()));

		OperationResultModel routerResponse = null;
		try {
			routerResponse = routerService.suscribe(model);
		} catch (final Exception e1) {
			log.error("Error in process:" + e1.getMessage());
			response = SSAPUtils.generateErrorMessage(subscribeMessage, SSAPErrorCode.PROCESSOR, e1.getMessage());
			return response;
		}

		final String errorCode = routerResponse.getErrorCode();
		final String messageResponse = routerResponse.getMessage();
		final String operation = routerResponse.getOperation();
		final String result = routerResponse.getResult();
		log.error(errorCode + " " + messageResponse + " " + operation + " " + result);

		if (!StringUtils.isEmpty(routerResponse.getErrorCode())) {
			response = SSAPUtils.generateErrorMessage(subscribeMessage, SSAPErrorCode.PROCESSOR,
					routerResponse.getErrorCode());
			return response;

		}
		response.setDirection(SSAPMessageDirection.RESPONSE);
		response.setMessageType(SSAPMessageTypes.SUBSCRIBE);
		response.setSessionKey(subscribeMessage.getSessionKey());
		response.getBody().setOk(true);
		response.getBody().setError(routerResponse.getErrorCode());
		final String dataStr = "{\"subscriptionId\": \"" + subsId + "\",\"message\": \"" + routerResponse.getMessage()
				+ "\"}";
		JsonNode data;
		try {
			data = objectMapper.readTree(dataStr);
			response.getBody().setData(data);
		} catch (final IOException e) {
			log.error("Error in process:" + e.getMessage());
			response = SSAPUtils.generateErrorMessage(subscribeMessage, SSAPErrorCode.PROCESSOR, e.getMessage());
			return response;
		}

		return response;
	}

	@Override
	public List<SSAPMessageTypes> getMessageTypes() {
		return Collections.singletonList(SSAPMessageTypes.SUBSCRIBE);
	}

	@Override
	public boolean validateMessage(SSAPMessage<? extends SSAPBodyMessage> message) {
		@SuppressWarnings("unchecked")
		final SSAPMessage<SSAPBodySubscribeMessage> subscribeMessage = (SSAPMessage<SSAPBodySubscribeMessage>) message;

		if (StringUtils.isEmpty(subscribeMessage.getBody().getQuery())) {
			throw new SSAPProcessorException(
					String.format(MessageException.ERR_FIELD_IS_MANDATORY, "query", message.getMessageType().name()));
		}

		if (subscribeMessage.getBody().getQueryType() == null) {
			throw new SSAPProcessorException(String.format(MessageException.ERR_FIELD_IS_MANDATORY, "queryType",
					message.getMessageType().name()));
		}

		return true;
	}

	@PostConstruct
	private void cleanSubscriptions() {

		repository.deleteAll();
	}

	@Scheduled(fixedDelay = 300000)
	public void deleteOldSubscriptions() {
		final ArrayList<SuscriptionNotificationsModel> subscriptionsToRemove = new ArrayList<>();
		final List<SuscriptionNotificationsModel> subscriptions = getSubscriptionsFromCache();
		for (final SuscriptionNotificationsModel subscription : subscriptions) {

			final Optional<IoTSession> session = securityPluginManager.getSession(subscription.getSessionKey());

			if (!session.isPresent())
				subscriptionsToRemove.add(subscription);

		}
		repository.delete(subscriptionsToRemove);
	}

	private List<SuscriptionNotificationsModel> getSubscriptionsFromCache() {
		return hazelcastInstance.getMap(SUBSCRIPTION_REPOSITORY).entrySet().stream()
				.map(e -> (SuscriptionNotificationsModel) e.getValue()).collect(Collectors.toList());
	}

}
