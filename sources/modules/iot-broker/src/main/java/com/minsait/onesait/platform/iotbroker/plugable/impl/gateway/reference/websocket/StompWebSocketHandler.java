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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ITopic;
import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyReturnMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodySubscribeMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyUnsubscribeMessage;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageTypes;
import com.minsait.onesait.platform.comms.protocol.json.SSAPJsonParser;
import com.minsait.onesait.platform.comms.protocol.util.SSAPMessageGenerator;
import com.minsait.onesait.platform.config.model.Subscriptor;
import com.minsait.onesait.platform.config.repository.SubscriptorRepository;
import com.minsait.onesait.platform.iotbroker.plugable.interfaces.gateway.GatewayInfo;
import com.minsait.onesait.platform.iotbroker.processor.GatewayNotifier;
import com.minsait.onesait.platform.iotbroker.processor.MessageProcessor;
import com.minsait.onesait.platform.iotbroker.processor.impl.UnsubscribeProcessor;

import lombok.extern.slf4j.Slf4j;

@ConditionalOnProperty(prefix = "onesaitplatform.iotbroker.plugable.gateway.stomp", name = "enable", havingValue = "true")
@Slf4j
@Controller
public class StompWebSocketHandler implements ApplicationListener<SessionConnectEvent> {
	
	@Autowired
	GatewayNotifier subscritorGW;

	@Autowired
	MessageProcessor processor;
	
	@Autowired
	private UnsubscribeProcessor unsubscribe;

	@Autowired
	SimpMessagingTemplate messagingTemplate;

	@Autowired
	@Qualifier("brokerSubscriptorsWS")
	IMap<String, List<String>> brokerSubscriptorsWS;
	
	@Autowired
	@Qualifier("notificationWS")
	private ITopic<String> topic;
	
	Map<String, MessageRetryNotification> messageNotification = new HashMap<String, MessageRetryNotification>();
	
	@Autowired
	private SubscriptorRepository subscriptorRepository;
	
	@Value("${onesaitplatform.iotbroker.plugable.gateway.stomp.pool:200}")
	private int pool;
	
	@Value("${onesaitplatform.iotbroker.plugable.gateway.stomp.attemps:2}")
	private int qos;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.stomp.attemps_interval:10}")
	private int retryinterval;
	
	@Value("${onesaitplatform.iotbroker.plugable.gateway.stomp.subscription_topic:/topic/subscription}")
	private String subscriptionTopic;
	
	private static final String STOMP_GATEWAY = "stomp_gateway";

	@PostConstruct
	public void init() {		
		subscritorGW.addSubscriptionListener(STOMP_GATEWAY, s -> {
			Subscriptor clientSubscriptor = subscriptorRepository.findBySubscriptionId(s.getBody().getSubscriptionId());	
			if (subscribedToBroker(clientSubscriptor.getClientId())) {
				if (messageNotification.size()<pool) {
					MessageRetryNotification messageRetryNotification = new MessageRetryNotification(s, qos);
					messageNotification.put(messageRetryNotification.getNotificationMessage().getMessageId(), messageRetryNotification);
					log.info(messageRetryNotification.getNotificationMessage().getMessageId() + " queued");
				}
				log.info("Sending subscription notification " + subscriptionTopic  + "/" + s.getSessionKey());
				messagingTemplate.convertAndSend(subscriptionTopic + "/" + s.getSessionKey(), s);
			} else {
				try {
					topic.publish(SSAPJsonParser.getInstance().serialize(s));
				} catch (Exception e) {
					log.info("error publishing message on topic. ", e);
				}
			}
		});

		subscritorGW.addCommandListener(STOMP_GATEWAY, c -> {
			messagingTemplate.convertAndSend("/topic/command/" + c.getSessionKey(), c);
			return new SSAPMessage<>();
		});
		
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		
		executor.scheduleAtFixedRate(() -> {			
			if (!messageNotification.isEmpty()) {
				log.info(messageNotification.size() + " pending messages (subscription notifications)");
				for (Map.Entry<String, MessageRetryNotification> entry : messageNotification.entrySet()) {
					if (entry.getValue().getQos()>0) {
						entry.getValue().setQos(entry.getValue().getQos()-1);
						log.info("Re Trying subscription notification " + subscriptionTopic + "/" + entry.getValue().getNotificationMessage().getSessionKey());
						messagingTemplate.convertAndSend(subscriptionTopic  + "/" + 
								entry.getValue().getNotificationMessage().getSessionKey(), 
								entry.getValue().getNotificationMessage());
					} else {
						Subscriptor subscriptor = subscriptorRepository.findBySubscriptionId(entry.getValue().getNotificationMessage().getBody().getSubscriptionId());
						log.info("Unsubscribing " + subscriptor.getSubscriptionId());
						unsubscribe.process(SSAPMessageGenerator.generateRequestUnsubscribeMessage(null, subscriptor.getSubscriptionId()), getGatewayInfo());
						
						subscriptorRepository.delete(subscriptor);
						messageNotification.remove(entry.getKey());
					}
				}			
			}
		}, 0, retryinterval, TimeUnit.SECONDS);
		
	}

	@MessageMapping("/message/{token}")
	public void handleConnect(@Payload SSAPMessage message, @DestinationVariable("token") String token,
			MessageHeaders messageHeaders) throws JsonProcessingException {
		
		updateBrokerSubscriptors(message);
		
		final SSAPMessage<SSAPBodyReturnMessage> response = processor.process(message, getGatewayInfo());
		messagingTemplate.convertAndSend("/topic/message/" + token, response);

	}
	
	@MessageMapping("/ack/{messageid}")
	public void handleSubscriptionAck(@DestinationVariable("messageid") String message,
			MessageHeaders messageHeaders) throws JsonProcessingException {
			log.info("ACK RECEIVED. MessageID: " + message );
			
			messageNotification.remove(message);
	}
	
	private void updateBrokerSubscriptors (SSAPMessage message) {
		try {
			if (brokerSubscriptorsWS != null) {
				if (message.getMessageType().equals(SSAPMessageTypes.SUBSCRIBE)) {
					List<String> clients = brokerSubscriptorsWS.get(InetAddress.getLocalHost().getHostName());
	
					if (clients != null) {
						if (!clients.contains(((SSAPMessage<SSAPBodySubscribeMessage>)message).getBody().getClientId())){
							clients.add(((SSAPMessage<SSAPBodySubscribeMessage>)message).getBody().getClientId());
							brokerSubscriptorsWS.put(InetAddress.getLocalHost().getHostName(), clients);
						}
					} else {
						clients = new ArrayList<>();
						clients.add(((SSAPMessage<SSAPBodySubscribeMessage>)message).getBody().getClientId());
						brokerSubscriptorsWS.put(InetAddress.getLocalHost().getHostName(), clients);
					}
				} else if (message.getMessageType().equals(SSAPMessageTypes.UNSUBSCRIBE)) {
					List<String> clients = brokerSubscriptorsWS.get(InetAddress.getLocalHost().getHostName());
					if (clients != null) {
						
						Subscriptor clientSubscriptor = subscriptorRepository.findBySubscriptionId(((SSAPMessage<SSAPBodyUnsubscribeMessage>)message).getBody().getSubscriptionId());	
					
						clients.remove(clientSubscriptor.getClientId());
						brokerSubscriptorsWS.put(InetAddress.getLocalHost().getHostName(), clients);
					}
				}
			}
		} catch (UnknownHostException e) {
			log.error("Unknown Host in WebSocketBroker connecting client with id. {} {}", message.getSessionKey(), e);
		}
	}
	
	private boolean subscribedToBroker(String clientId) {
		List<String> clients;
		try {
			clients = brokerSubscriptorsWS.get(InetAddress.getLocalHost().getHostName());
			if (clients != null ) {
				return (clients.contains(clientId));
			} else {
				return false;
			}
		} catch (UnknownHostException e) {
			log.error("Unknown Host in WebSocketBroker connecting client with id. {} {}", clientId, e);
			return false;
		}
	}

	@EventListener
	public void onSocketDisconnected(SessionDisconnectEvent event) {
		try {
			if (brokerSubscriptorsWS != null) {
				List<String> clients = brokerSubscriptorsWS.get(InetAddress.getLocalHost().getHostName());
				if (clients != null) {
					clients.remove(event.getSessionId());
				}
			}
		} catch (UnknownHostException e) {
			log.error("Unknown Host in WebSocketBroker connecting client with id. {} {}", event.getSessionId(), e);
		}
		List<Subscriptor> subscriptors = subscriptorRepository.findByClientId(event.getSessionId());
		
		for (Subscriptor subscriptorClient : subscriptors) {
			unsubscribe.process(SSAPMessageGenerator.generateRequestUnsubscribeMessage(null, subscriptorClient.getSubscriptionId()), getGatewayInfo());
		}

	}

	@Override
	public void onApplicationEvent(SessionConnectEvent event) {	
	}

	private GatewayInfo getGatewayInfo() {
		final GatewayInfo info = new GatewayInfo();
		info.setName(STOMP_GATEWAY);
		info.setProtocol("WEBSOCKET");

		return info;
	}
}
