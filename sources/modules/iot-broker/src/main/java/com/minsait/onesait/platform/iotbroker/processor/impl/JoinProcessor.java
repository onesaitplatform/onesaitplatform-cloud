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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyJoinMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyReturnMessage;
import com.minsait.onesait.platform.comms.protocol.body.parent.SSAPBodyMessage;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageDirection;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageTypes;
import com.minsait.onesait.platform.iotbroker.common.MessageException;
import com.minsait.onesait.platform.iotbroker.common.exception.AuthenticationException;
import com.minsait.onesait.platform.iotbroker.common.exception.SSAPComplianceException;
import com.minsait.onesait.platform.iotbroker.common.exception.SSAPProcessorException;
import com.minsait.onesait.platform.iotbroker.plugable.impl.security.SecurityPluginManager;
import com.minsait.onesait.platform.iotbroker.plugable.interfaces.gateway.GatewayInfo;
import com.minsait.onesait.platform.iotbroker.processor.DeviceManager;
import com.minsait.onesait.platform.iotbroker.processor.MessageTypeProcessor;
import com.minsait.onesait.platform.multitenant.config.model.IoTSession;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JoinProcessor implements MessageTypeProcessor {

	@Autowired
	SecurityPluginManager securityManager;
	
	@Autowired
	private DeviceManager deviceManager;

	@Autowired
	ObjectMapper mapper;

	@SuppressWarnings("unchecked")
	@Override
	public SSAPMessage<SSAPBodyReturnMessage> process(SSAPMessage<? extends SSAPBodyMessage> message, GatewayInfo info, Optional<IoTSession> session) {
		final SSAPMessage<SSAPBodyJoinMessage> join = (SSAPMessage<SSAPBodyJoinMessage>) message;
		log.info("Client {}:{} ask for new session",join.getBody().getDeviceTemplate(),join.getBody().getDevice());
		final SSAPMessage<SSAPBodyReturnMessage> response = new SSAPMessage<>();
		response.setBody(new SSAPBodyReturnMessage());
		response.getBody().setOk(true);
		try {
			response.getBody().setData(mapper.readTree("{}"));
		} catch (final IOException e) {
			log.error(e.getMessage(), e);
		}

		if (StringUtils.isEmpty(join.getBody().getToken())) {
			throw new SSAPComplianceException(
					String.format(MessageException.ERR_FIELD_IS_MANDATORY, "token", message.getMessageType().name()));
		}
  
		String clientPlatformIdentification = join.getBody().getDeviceTemplate();
		String clientPlatformInstanceIdentification = join.getBody().getDevice();
		
		
		if (deviceManager.registerActivity(message, clientPlatformIdentification, clientPlatformInstanceIdentification, info)) {
		
			session = securityManager.authenticate(join.getBody().getToken(),
					clientPlatformIdentification, clientPlatformInstanceIdentification, join.getSessionKey());
			session.ifPresent(s -> {
				response.setSessionKey(s.getSessionKey());
				try {
					response.getBody().setData(mapper.readTree("{\"sessionKey\":\"" + s.getSessionKey() + "\"}"));
				} catch (final IOException e) {
					log.error(e.getMessage());
				}
			});
	
			if (!StringUtils.isEmpty(response.getSessionKey())) {
				response.setDirection(SSAPMessageDirection.RESPONSE);
				response.setMessageId(join.getMessageId());
				response.setMessageType(SSAPMessageTypes.JOIN);
			} else {
				throw new AuthenticationException(MessageException.ERR_SESSIONKEY_NOT_ASSINGED);
			}
			return response;
		} else {
			//TODO add limit configuration
			throw new AuthenticationException(MessageException.ERR_CLIENTPLATFORM_INSTANCE_LIMIT_REACHED);
		}
	}

	@Override
	public List<SSAPMessageTypes> getMessageTypes() {
		return Collections.singletonList(SSAPMessageTypes.JOIN);
	}

	@Override
	public boolean validateMessage(SSAPMessage<? extends SSAPBodyMessage> message) {
		final SSAPMessage<SSAPBodyJoinMessage> join = (SSAPMessage<SSAPBodyJoinMessage>) message;

		if (StringUtils.isEmpty(join.getBody().getDeviceTemplate())
				|| StringUtils.isEmpty(join.getBody().getDevice())) {
			throw new SSAPProcessorException(String.format(MessageException.ERR_FIELD_IS_MANDATORY, "ClientPlatform",
					join.getMessageType().name()));
		}

		return true;
	}

}
