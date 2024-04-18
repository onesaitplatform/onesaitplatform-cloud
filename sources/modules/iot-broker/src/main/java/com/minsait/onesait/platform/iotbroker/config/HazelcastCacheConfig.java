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
package com.minsait.onesait.platform.iotbroker.config;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.topic.ITopic;
import com.minsait.onesait.platform.config.services.processtrace.dto.OperationStatus;
import com.minsait.onesait.platform.iotbroker.plugable.impl.gateway.reference.mqtt.MqttSession;
import com.minsait.onesait.platform.iotbroker.plugable.impl.gateway.reference.mqtt.NotificatorServiceImpl;
import com.minsait.onesait.platform.iotbroker.plugable.impl.gateway.reference.websocket.NotificatorWSServiceImpl;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class HazelcastCacheConfig {

	@Autowired
	private HazelcastInstance hazelcastInstance;

	@Autowired
	private NotificatorServiceImpl notificationService;

	@Autowired
	private NotificatorWSServiceImpl notificationWSService;

	@Bean(name = "notification")
	public ITopic<String> hazelcastNotification() {
		ITopic<String> topic = hazelcastInstance.getTopic("notification");
		String registerId = topic
				.addMessageListener(msg -> notificationService.notifyHazelcastTopic(msg.getMessageObject())).toString();
		log.info("Mqtt listener created with id: {}", registerId);
		return topic;
	}

	@Bean(name = "notificationWS")
	public ITopic<String> hazelcastNotificationWS() {
		ITopic<String> topic = hazelcastInstance.getTopic("notificationWS");
		String registerId = topic
				.addMessageListener(msg -> notificationWSService.notifyHazelcastTopic(msg.getMessageObject()))
				.toString();
		log.info("WS listener created with id: {}", registerId);
		return topic;
	}

	@Bean(name = "brokerSubscriptors")
	public IMap<String, List<String>> brokerSubscriptors() {
		return hazelcastInstance.getMap("brokerSubscriptors");
	}

	@Bean(name = "brokerSubscriptorsWS")
	public IMap<String, List<String>> brokerSubscriptorsWS() {
		return hazelcastInstance.getMap("brokerSubscriptorsWS");
	}

	@Bean(name = "processExecutionMap")
	public Map<String, LinkedHashSet<OperationStatus>> processExecutionMap() {
		return hazelcastInstance.getMap("processExecutionMap");
	}

	@Bean(name = "mqttClientSessions")
	public IMap<String, MqttSession> mqttClientSessions() {
		return hazelcastInstance.getMap("mqttClientSessions");
	}

}
