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
package com.minsait.onesait.platform.iotbroker.processor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.minsait.onesait.platform.commons.metrics.MetricsManager;
import com.minsait.onesait.platform.commons.metrics.Source;
import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyReturnMessage;
import com.minsait.onesait.platform.comms.protocol.body.parent.SSAPBodyMessage;
import com.minsait.onesait.platform.comms.protocol.body.parent.SSAPBodyOntologyMessage;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPErrorCode;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageDirection;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageTypes;
import com.minsait.onesait.platform.comms.protocol.json.SSAPJsonParser;
import com.minsait.onesait.platform.comms.protocol.json.Exception.SSAPParseException;
import com.minsait.onesait.platform.comms.protocol.util.SSAPMessageGenerator;
import com.minsait.onesait.platform.iotbroker.audit.aop.IotBrokerAuditable;
import com.minsait.onesait.platform.iotbroker.common.MessageException;
import com.minsait.onesait.platform.iotbroker.common.exception.AuthenticationException;
import com.minsait.onesait.platform.iotbroker.common.exception.AuthorizationException;
import com.minsait.onesait.platform.iotbroker.common.exception.OntologySchemaException;
import com.minsait.onesait.platform.iotbroker.common.exception.SSAPProcessorException;
import com.minsait.onesait.platform.iotbroker.common.util.SSAPUtils;
import com.minsait.onesait.platform.iotbroker.plugable.impl.security.SecurityPluginManager;
import com.minsait.onesait.platform.iotbroker.plugable.interfaces.gateway.GatewayInfo;
import com.minsait.onesait.platform.log.interceptor.aop.LogInterceptable;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.config.model.IoTSession;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MessageProcessorDelegate implements MessageProcessor {

	@Autowired
	SecurityPluginManager securityPluginManager;

	@Autowired
	List<MessageTypeProcessor> processors;

	@Autowired
	private DeviceManager deviceManager;

	@Autowired(required = false)
	private MetricsManager metricsManager;

	private static final String ERROR_PROCESSING = "Error processing message";

	@IotBrokerAuditable
	@Override
	public <T extends SSAPBodyMessage> SSAPMessage<SSAPBodyReturnMessage> process(SSAPMessage<T> message,
			GatewayInfo info) {

		SSAPMessage<SSAPBodyReturnMessage> response = null;
		Optional<IoTSession> session = Optional.empty();
		try {

			session = securityPluginManager.getSession(message.getSessionKey());

			final Optional<SSAPMessage<SSAPBodyReturnMessage>> error = validateMessage(message, session);

			if (error.isPresent()) {
				return error.get();
			}


			final MessageTypeProcessor processor = proxyProcesor(message);

			processor.validateMessage(message);

			if (session.isPresent()) {
				String clientPlatform = session.get().getClientPlatform();
				String clientPlatformInstance = session.get().getDevice();
				deviceManager.registerActivity(message, clientPlatform, clientPlatformInstance, info);
			}

			//session parameter can be changed by processors. For example join does it.
			//this is an optimization to avoid multiple calls to getSession. Multiple calls to getSession has bad performance on
			//high load scenarios, even getting sessions from cache
			response = processor.process(message, info, session);

			if (!SSAPMessageDirection.ERROR.equals(response.getDirection())) {
				response.setDirection(SSAPMessageDirection.RESPONSE);
				response.setMessageId(message.getMessageId());
				response.setMessageType(message.getMessageType());
			}

		} catch (final SSAPProcessorException | OntologySchemaException e) {
			log.error(ERROR_PROCESSING, e);
			response = SSAPUtils.generateErrorMessage(message, SSAPErrorCode.PROCESSOR,
					String.format(e.getMessage(), message.getMessageType().name()));
		} catch (final AuthorizationException e) {
			log.error(ERROR_PROCESSING, e);
			response = SSAPUtils.generateErrorMessage(message, SSAPErrorCode.AUTHORIZATION,
					String.format(e.getMessage(), message.getMessageType().name()));
		} catch (final AuthenticationException e) {
			log.error(ERROR_PROCESSING, e);
			response = SSAPUtils.generateErrorMessage(message, SSAPErrorCode.AUTENTICATION,
					String.format(e.getMessage(), message.getMessageType().name()));
		} catch (final Exception e) {
			log.error(ERROR_PROCESSING, e);
			response = SSAPUtils.generateErrorMessage(message, SSAPErrorCode.PROCESSOR,
					String.format(e.getMessage(), message.getMessageType().name()));
		} finally {

			final SSAPMessage<SSAPBodyReturnMessage> resp = response;

			session.ifPresent( s -> {

				// Metrics
				if (null != metricsManager && metricsManager.isMetricsEnabled()) {
					String ontology = null;
					if (message.getBody() instanceof SSAPBodyOntologyMessage) {
						ontology = ((SSAPBodyOntologyMessage) message.getBody()).getOntology();
					}

					String metricsStatus = "OK";
					if (null == resp || null == resp.getBody() || !resp.getBody().isOk()) {
						metricsStatus = "KO";
					}

					metricsManager.logMetricDigitalBroker(s.getUserID(), ontology, message.getMessageType(),
							Source.IOTBROKER, metricsStatus);
				}
			});
			MultitenancyContextHolder.clear();
		}
		return response;
	}

	@Override
	@LogInterceptable
	public String process(String message, GatewayInfo info) {
		SSAPMessage<SSAPBodyReturnMessage> response = null;
		SSAPMessage<?> request = null;
		try {
			request = SSAPJsonParser.getInstance().deserialize(message);
			response = this.process(request, info);
		} catch (final SSAPParseException e) {
			response = SSAPUtils.generateErrorMessage(request, SSAPErrorCode.PROCESSOR,
					"Request message is not parseable" + e.getMessage());
		}
		try {
			return SSAPJsonParser.getInstance().serialize(response);
		} catch (final SSAPParseException e) {
			return e.getMessage();
		}

	}

	private Optional<SSAPMessage<SSAPBodyReturnMessage>> validateMessage(
			SSAPMessage<? extends SSAPBodyMessage> message, Optional<IoTSession> session) {
		SSAPMessage<SSAPBodyReturnMessage> response = null;

		// Check presence of sessionKey and authorization of sessionKey
		if (message.getBody().isSessionKeyMandatory() && !StringUtils.hasText(message.getSessionKey())) {
			response = SSAPMessageGenerator.generateResponseErrorMessage(message, SSAPErrorCode.PROCESSOR, String
					.format(MessageException.ERR_FIELD_IS_MANDATORY, "Sessionkey", message.getMessageType().name()));

			return Optional.of(response);
		}

		if (message.getBody().isSessionKeyMandatory()) {
			if (!securityPluginManager.checkSessionKeyActive(session)) {
				response = SSAPMessageGenerator.generateResponseErrorMessage(message, SSAPErrorCode.AUTENTICATION,
						String.format(MessageException.ERR_SESSIONKEY_NOT_VALID, message.getMessageType().name()));
				return Optional.of(response);
			}
		}
		// Check if ontology is present and autorization for ontology
		if (message.getBody().isOntologyMandatory()) {
			final SSAPBodyOntologyMessage body = (SSAPBodyOntologyMessage) message.getBody();
			if (!StringUtils.hasText(body.getOntology())) {
				response = SSAPMessageGenerator.generateResponseErrorMessage(message, SSAPErrorCode.PROCESSOR,
						String.format(MessageException.ERR_ONTOLOGY_SCHEMA, message.getMessageType().name()));
				return Optional.of(response);
			}

			if (!securityPluginManager.checkAuthorization(message.getMessageType(), body.getOntology(),
					session)) {
				response = SSAPMessageGenerator.generateResponseErrorMessage(message, SSAPErrorCode.AUTHORIZATION,
						String.format(MessageException.ERR_ONTOLOGY_AUTH, message.getMessageType().name()));
				return Optional.of(response);
			}
		}
		return Optional.empty();
	}

	private MessageTypeProcessor proxyProcesor(SSAPMessage<? extends SSAPBodyMessage> message) {

		if (null == message.getMessageType()) {
			throw new SSAPProcessorException(MessageException.ERR_SSAP_MESSAGETYPE_MANDATORY_NOT_NULL);
		}

		final SSAPMessageTypes type = message.getMessageType();

		final List<MessageTypeProcessor> filteredProcessors = processors.stream()
				.filter(p -> p.getMessageTypes().contains(type)).collect(Collectors.toList());

		if (filteredProcessors.isEmpty()) {
			throw new SSAPProcessorException(
					String.format(MessageException.ERR_PROCESSOR_NOT_FOUND, message.getMessageType()));
		}

		return filteredProcessors.get(0);

	}

}
