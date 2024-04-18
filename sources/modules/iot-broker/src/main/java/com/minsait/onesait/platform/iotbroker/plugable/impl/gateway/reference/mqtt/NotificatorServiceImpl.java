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
package com.minsait.onesait.platform.iotbroker.plugable.impl.gateway.reference.mqtt;

import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyIndicationMessage;
import com.minsait.onesait.platform.comms.protocol.json.SSAPJsonParser;
import com.minsait.onesait.platform.comms.protocol.json.Exception.SSAPParseException;
import com.minsait.onesait.platform.config.model.Subscriptor;
import com.minsait.onesait.platform.config.repository.SubscriptorRepository;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.MqttMessageBuilders;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotificatorServiceImpl implements NotificatorService {

	@Value("${onesaitplatform.iotbroker.plugable.gateway.moquette.subscription_topic:/topic/subscription}")
	private String subscriptionTopic;

	@Value("${onesaitplatform.router.notifications.pool.subscription.attemps:2}")
	private int qos;

	@Autowired
	private SubscriptorRepository subscriptorRepository;

	@Autowired
	private ObjectMapper mapper;

	Executor messageExecutor = Executors.newSingleThreadExecutor();

	@Override
	public void notifyHazelcastTopic(String msg) {
		log.info("Message received = {}", msg);

		SSAPMessage<SSAPBodyIndicationMessage> message;
		try {
			message = SSAPJsonParser.getInstance().deserialize(msg);

			final Collection<String> clients = MoquetteBroker.getClients();
			final Subscriptor subscriptor = subscriptorRepository.findBySubscriptionId(message.getBody().getSubscriptionId());
			if (clients.contains(subscriptor.getClientId())) {
				messageExecutor.execute(() -> {
					log.info("Digital Broker has the MQTT connection with client.");
					final SSAPMessage<SSAPBodyIndicationMessage> s = message;
					String playload = "";
					try {
						playload = SSAPJsonParser.getInstance().serialize(s);
					} catch (final SSAPParseException e) {
						log.error("Error serializing indicator message" + e.getMessage());
					}
					final MqttPublishMessage message1 = MqttMessageBuilders.publish()
							.topicName(subscriptionTopic + "/" + s.getSessionKey()).retained(false)
							.qos(MqttQoS.valueOf(qos)).payload(Unpooled.copiedBuffer(playload.getBytes())).build();
					MoquetteBroker.getServer().internalPublish(message1, s.getSessionKey());
				});
			}
		} catch (final SSAPParseException e) {
			log.error("Error parsing SSAPIndicationMessage. msg = {}", msg, e);
		}
	}
}
