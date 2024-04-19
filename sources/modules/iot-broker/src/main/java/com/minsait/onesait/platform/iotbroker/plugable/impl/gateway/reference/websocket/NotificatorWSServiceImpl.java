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
package com.minsait.onesait.platform.iotbroker.plugable.impl.gateway.reference.websocket;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.hazelcast.core.IMap;
import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyIndicationMessage;
import com.minsait.onesait.platform.comms.protocol.json.SSAPJsonParser;
import com.minsait.onesait.platform.comms.protocol.json.Exception.SSAPParseException;
import com.minsait.onesait.platform.config.model.Subscriptor;
import com.minsait.onesait.platform.config.repository.SubscriptorRepository;
import com.minsait.onesait.platform.iotbroker.plugable.impl.gateway.reference.mqtt.NotificatorService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotificatorWSServiceImpl implements NotificatorService {

	@Autowired
	@Qualifier("brokerSubscriptors")
	IMap<String, List<String>> brokerSubscriptors;

	@Autowired
	private SubscriptorRepository subscriptorRepository;

	Executor messageExecutor = Executors.newSingleThreadExecutor();
	
	@Autowired
	SimpMessagingTemplate messagingTemplate;

	@Override
	public void notifyHazelcastTopic(String msg) {
		log.info("Message received = {}", msg);

		SSAPMessage<SSAPBodyIndicationMessage> message;
		try {
			message = SSAPJsonParser.getInstance().deserialize(msg);

			Collection<String> clients = brokerSubscriptors.get(InetAddress.getLocalHost().getHostName());
			Subscriptor subscriptor = subscriptorRepository.findBySubscriptionId(message.getBody().getSubscriptionId());
			if (clients!=null && clients.contains(subscriptor.getClientId())) {
				messageExecutor.execute(new Runnable() {
					@Override
					public void run() {
						log.info("Digital Broker has the WS connection with client.");
						
						final SSAPMessage<SSAPBodyIndicationMessage> s = message;
						messagingTemplate.convertAndSend("/topic/subscription/" + s.getSessionKey(), s);
					}
				});
			}
		} catch (SSAPParseException e) {
			log.error("Error parsing SSAPIndicationMessage. msg = {}", msg, e);
		} catch (UnknownHostException e1) {
			log.error("Unknown Host in WebSocketBroker connecting client with id. {}", e1);
		}
	}
}
