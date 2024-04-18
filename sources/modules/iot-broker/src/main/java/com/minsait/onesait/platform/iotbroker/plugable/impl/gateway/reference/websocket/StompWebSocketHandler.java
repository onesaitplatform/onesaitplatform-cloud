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
package com.minsait.onesait.platform.iotbroker.plugable.impl.gateway.reference.websocket;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyReturnMessage;
import com.minsait.onesait.platform.iotbroker.plugable.interfaces.gateway.GatewayInfo;
import com.minsait.onesait.platform.iotbroker.processor.GatewayNotifier;
import com.minsait.onesait.platform.iotbroker.processor.MessageProcessor;

@ConditionalOnProperty(prefix = "onesaitplatform.iotbroker.plugable.gateway.stomp", name = "enable", havingValue = "true")
@Controller
public class StompWebSocketHandler {
	@Autowired
	GatewayNotifier subscriptor;

	@Autowired
	MessageProcessor processor;

	@Autowired
	SimpMessagingTemplate messagingTemplate;

	private static final String STOMP_GATEWAY = "stomp_gateway";

	@PostConstruct
	public void init() {
		subscriptor.addSubscriptionListener(STOMP_GATEWAY, s -> {
			messagingTemplate.convertAndSend("/topic/subscription/" + s.getSessionKey(), s);
		});

		subscriptor.addCommandListener(STOMP_GATEWAY, c -> {
			messagingTemplate.convertAndSend("/topic/command/" + c.getSessionKey(), c);
			return new SSAPMessage<>();
		});
	}

	@MessageMapping("/message/{token}")
	public void handleConnect(@Payload SSAPMessage message, @DestinationVariable("token") String token,
			MessageHeaders messageHeaders) throws JsonProcessingException {
		final SSAPMessage<SSAPBodyReturnMessage> response = processor.process(message, getGatewayInfo());
		messagingTemplate.convertAndSend("/topic/message/" + token, response);

	}

	private GatewayInfo getGatewayInfo() {
		final GatewayInfo info = new GatewayInfo();
		info.setName(STOMP_GATEWAY);
		info.setProtocol("WEBSOCKET");

		return info;
	}
}
