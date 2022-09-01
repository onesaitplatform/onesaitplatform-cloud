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
package com.minsait.onesait.platform.iotbroker.plugable.impl.gateway.reference.mqtt;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.topic.ITopic;
import com.minsait.onesait.platform.commons.exception.GenericRuntimeOPException;
import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyUnsubscribeMessage;
import com.minsait.onesait.platform.comms.protocol.json.SSAPJsonParser;
import com.minsait.onesait.platform.comms.protocol.json.Exception.SSAPParseException;
import com.minsait.onesait.platform.comms.protocol.util.SSAPMessageGenerator;
import com.minsait.onesait.platform.config.model.Subscriptor;
import com.minsait.onesait.platform.config.repository.SubscriptorRepository;
import com.minsait.onesait.platform.iotbroker.plugable.impl.security.SecurityPluginManager;
import com.minsait.onesait.platform.iotbroker.plugable.interfaces.gateway.GatewayInfo;
import com.minsait.onesait.platform.iotbroker.processor.GatewayNotifier;
import com.minsait.onesait.platform.iotbroker.processor.MessageProcessor;
import com.minsait.onesait.platform.iotbroker.processor.impl.UnsubscribeProcessor;
import com.minsait.onesait.platform.multitenant.config.model.IoTSession;

import io.moquette.BrokerConstants;
import io.moquette.broker.ClientDescriptor;
import io.moquette.broker.Server;
import io.moquette.broker.SessionRegistry;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.MemoryConfig;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.messages.InterceptConnectMessage;
import io.moquette.interception.messages.InterceptConnectionLostMessage;
import io.moquette.interception.messages.InterceptDisconnectMessage;
//import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.MqttMessageBuilders;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.extern.slf4j.Slf4j;

@ConditionalOnProperty(prefix = "onesaitplatform.iotbroker.plugable.gateway.moquette", name = "enable", havingValue = "true")
@Slf4j
@Component
public class MoquetteBroker {

	@Value("${onesaitplatform.iotbroker.plugable.gateway.moquette.port:1883}")
	private String port;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.moquette.pool:10}")
	private int pool;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.moquette.host:localhost}")
	private String host;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.moquette.store:moquette_store.mapdb}")
	private String store;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.moquette.outbound_topic:/topic/message}")
	private String outboundTopic;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.moquette.inbound_topic:/queue/message}")
	private String inboundTopic;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.moquette.subscription_topic:/topic/subscription}")
	private String subscriptionTopic;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.moquette.command_topic:/topic/command}")
	private String commandTopic;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.moquette.maxBytesInMessage:8092}")
	private String maxBytes;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.moquette.ssl.enable:false}")
	public boolean sslEnabled;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.moquette.ssl.port:8883}")
	public String sslPort;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.moquette.ssl.jks_path:develkeystore.jks}")
	public String jksPath;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.moquette.ssl.keystore_password:changeIt!}")
	public String keyStorePassword;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.moquette.ssl.keymanager_password:changeIt!}")
	public String keyManagerPassword;

	@Value("${onesaitplatform.router.notifications.pool.subscription.attemps:2}")
	private int qos;

	@Autowired
	protected MessageProcessor processor;

	@Autowired
	private UnsubscribeProcessor unsubscribe;

	@Autowired
	private SubscriptorRepository subscriptorRepository;

	@Autowired
	SecurityPluginManager securityPluginManager;

	@Autowired
	@Qualifier("notification")
	private ITopic<String> topic;

	private static Server server = new Server();

	@Autowired
	GatewayNotifier subscriptor;

	@Autowired
	HazelcastInstance hazelcastInstance;

	IQueue<String> disconectedClientsSubscription;

	@Autowired
	@Qualifier("brokerSubscriptors")
	IMap<String, List<String>> brokerSubscriptors;

	public static Server getServer() {
		return server;
	}

	@SuppressWarnings("unchecked")
	public static Collection<String> getClients() {
		final Server s = MoquetteBroker.getServer();
		try {
			final Field sessionsField = Server.class.getDeclaredField("sessions");
			sessionsField.setAccessible(true);
			final SessionRegistry sessions = (SessionRegistry) sessionsField.get(s);
			final Method getClientsMethod = SessionRegistry.class.getDeclaredMethod("listConnectedClients");
			getClientsMethod.setAccessible(true);
			final Collection<ClientDescriptor> clients = (Collection<ClientDescriptor>) getClientsMethod.invoke(sessions);
			return clients.stream().map(cd -> cd.getClientID()).collect(Collectors.toList());
		} catch (final Exception e) {
			log.error("Error while playing reflection for mqtt clients (Moquette 0.15 changes)");
		}
		return new ArrayList<>();
	}

	class PublisherListener extends AbstractInterceptHandler {

		@Override
		public void onConnect(InterceptConnectMessage msg) {

			if (brokerSubscriptors != null) {
				List<String> clients;
				try {
					clients = brokerSubscriptors.get(InetAddress.getLocalHost().getHostName());

					if (clients != null) {
						clients.add(msg.getClientID());
						brokerSubscriptors.put(InetAddress.getLocalHost().getHostName(), clients);
					} else {
						clients = new ArrayList<>();
						clients.add(msg.getClientID());
						brokerSubscriptors.put(InetAddress.getLocalHost().getHostName(), clients);
					}
				} catch (final UnknownHostException e) {
					log.error("Unknown Host in MoquetteBroker connecting client with id. {} {}", msg.getClientID(), e);
				}
			}
		}

		@Override
		public String getID() {
			return "ssapInterceptor";
		}

		@Override
		public void onPublish(InterceptPublishMessage msg) {
			final ByteBuf byteBuf = msg.getPayload();
			final String playload = new String(ByteBufUtil.getBytes(byteBuf), Charset.forName("UTF-8"));
			final String response = processor.process(playload, getGatewayInfo());

			final MqttPublishMessage message = MqttMessageBuilders.publish()
					.topicName(outboundTopic + "/" + msg.getClientID()).retained(false).qos(MqttQoS.EXACTLY_ONCE)
					.payload(Unpooled.copiedBuffer(response.getBytes())).build();

			getServer().internalPublish(message, msg.getClientID());
		}

		@Override
		public void onConnectionLost(InterceptConnectionLostMessage msg) {
			log.info("Connection Lost with client {}. The subscriptions of this client are going to be deleted.",
					msg.getClientID());

			final List<Subscriptor> subscriptors = subscriptorRepository.findByClientId(msg.getClientID());
			for (final Subscriptor subscriptor : subscriptors) {
				final SSAPMessage<SSAPBodyUnsubscribeMessage> unsubscribeMessage = SSAPMessageGenerator
						.generateRequestUnsubscribeMessage(null, subscriptor.getSubscriptionId(), null);
				final Optional<IoTSession> session = securityPluginManager
						.getSession(unsubscribeMessage.getSessionKey());
				unsubscribe.process(unsubscribeMessage, getGatewayInfo(), session);
			}
			final MqttPublishMessage message = MqttMessageBuilders.publish()
					.topicName(outboundTopic + "/" + msg.getClientID()).retained(false).qos(MqttQoS.EXACTLY_ONCE)
					.payload(Unpooled.copiedBuffer("Connection Lost, please connect again.".getBytes())).build();

			try {
				brokerSubscriptors.get(InetAddress.getLocalHost().getHostName()).remove(msg.getClientID());
			} catch (final UnknownHostException e) {
				log.error("Unknown Host in MoquetteBroker connecting client with id. {} {}", msg.getClientID(), e);
			}

			getServer().internalPublish(message, msg.getClientID());
		}

		@Override
		public void onDisconnect(InterceptDisconnectMessage msg) {

			log.info("Connection Lost with client {}. The subscriptions of this client are going to be deleted.",
					msg.getClientID());

			final List<Subscriptor> subscriptors = subscriptorRepository.findByClientId(msg.getClientID());
			for (final Subscriptor subscriptor : subscriptors) {
				final SSAPMessage<SSAPBodyUnsubscribeMessage> unsubscribeMessage = SSAPMessageGenerator
						.generateRequestUnsubscribeMessage(null, subscriptor.getSubscriptionId(), null);
				final Optional<IoTSession> session = securityPluginManager
						.getSession(unsubscribeMessage.getSessionKey());
				unsubscribe.process(unsubscribeMessage, getGatewayInfo(), session);
			}

			try {
				brokerSubscriptors.get(InetAddress.getLocalHost().getHostName()).remove(msg.getClientID());
			} catch (final UnknownHostException e) {
				log.error("Unknown Host in MoquetteBroker connecting client with id. {} {}", msg.getClientID(), e);
			}
		}
	}

	@PostConstruct
	public void init() {
		try {

			disconectedClientsSubscription = hazelcastInstance.getQueue("disconectedClientsSubscription");

			final ExecutorService thread2 = Executors.newSingleThreadExecutor();
			thread2.execute(() -> {
				while (true) {
					try {
						final String hostName = disconectedClientsSubscription.take();
						log.info("Client disconnected: {}", hostName);
						if (hostName != null) {
							final List<String> clients = brokerSubscriptors.get(hostName);
							if (clients != null && !clients.isEmpty()) {
								for (final String client : clients) {
									final List<Subscriptor> subscriptors = subscriptorRepository.findByClientId(client);
									for (final Subscriptor subscriptor : subscriptors) {
										final SSAPMessage<SSAPBodyUnsubscribeMessage> unsubscribeMessage = SSAPMessageGenerator
												.generateRequestUnsubscribeMessage(null,
														subscriptor.getSubscriptionId(), null);
										final Optional<IoTSession> session = securityPluginManager
												.getSession(unsubscribeMessage.getSessionKey());
										unsubscribe.process(unsubscribeMessage, getGatewayInfo(), session);
									}
								}
							}
						}
					} catch (final Exception e1) {
						log.error("Interrupted Disconnected Clients Queue listening", e1);
					}
				}
			});

			subscriptor.addSubscriptionListener("moquette_gateway", s -> {
				String playload = "";
				try {
					playload = SSAPJsonParser.getInstance().serialize(s);
				} catch (final SSAPParseException e) {
					log.error("Error serializing indicator message" + e.getMessage());
				}
				final MqttPublishMessage message = MqttMessageBuilders.publish()
						.topicName(subscriptionTopic + "/" + s.getSessionKey()).retained(false)
						.qos(MqttQoS.valueOf(qos)).payload(Unpooled.copiedBuffer(playload.getBytes())).build();

				final Collection<String> clients = MoquetteBroker.getClients();
				final Subscriptor subscriptor = subscriptorRepository
						.findBySubscriptionId(s.getBody().getSubscriptionId());
				if (clients.contains(subscriptor.getClientId())) {
					log.info("Digital Broker has the MQTT connection with client.");
					MoquetteBroker.getServer().internalPublish(message, s.getSessionKey());
				} else {
					log.info("Digital Broker has NOT the MQTT connection with client. Check if others broker has it.");
					try {
						topic.publish(SSAPJsonParser.getInstance().serialize(s));
					} catch (final Exception e) {
						log.info("error publishing message on topic. ", e);
					}
				}
			});

			subscriptor.addCommandListener("moquette_gateway",

					s -> {
						String playload = "";
						try {
							playload = SSAPJsonParser.getInstance().serialize(s);
						} catch (final SSAPParseException e) {
							log.error("Error serializing indicator message" + e.getMessage());
						}
						final MqttPublishMessage message = MqttMessageBuilders.publish()
								.topicName(commandTopic + "/" + s.getSessionKey()).retained(false)
								.qos(MqttQoS.valueOf(qos)).payload(Unpooled.copiedBuffer(playload.getBytes())).build();

						MoquetteBroker.getServer().internalPublish(message, s.getSessionKey());

						return null;
					});

			final Properties brokerProperties = new Properties();
			//			brokerProperties.put(BrokerConstants.STORAGE_CLASS_NAME, MapDBPersistentStore.class.getName());
			//			brokerProperties.put(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME, store);
			brokerProperties.put(BrokerConstants.PORT_PROPERTY_NAME, port);
			brokerProperties.put(BrokerConstants.BROKER_INTERCEPTOR_THREAD_POOL_SIZE, pool);
			brokerProperties.put(BrokerConstants.HOST_PROPERTY_NAME, host);
			brokerProperties.put(BrokerConstants.NETTY_MAX_BYTES_PROPERTY_NAME, maxBytes);

			if (sslEnabled) {
				brokerProperties.put(BrokerConstants.JKS_PATH_PROPERTY_NAME, jksPath);
				brokerProperties.put(BrokerConstants.KEY_STORE_PASSWORD_PROPERTY_NAME, keyStorePassword);
				brokerProperties.put(BrokerConstants.KEY_MANAGER_PASSWORD_PROPERTY_NAME, keyManagerPassword);
				brokerProperties.put(BrokerConstants.SSL_PORT_PROPERTY_NAME, sslPort);
			}

			final IConfig memoryConfig = new MemoryConfig(brokerProperties);
			server.startServer(memoryConfig);
			server.addInterceptHandler(new PublisherListener());

			try {
				Thread.sleep(2000);
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
				log.warn("Error initializing MoquetteBroker", e);
			}

		} catch (final IOException e) {
			log.error("Error initializing MoquetteBroker", e);
		}

	}

	@PreDestroy
	public void stopServer() {
		log.info("Stopping Moquette server...");
		try {
			server.stopServer();
		} catch (final Throwable e) {
			log.error("Unable to stop Moquette server. Cause = {}, errorMessage = {}.", e.getCause(), e.getMessage(),
					e);
			throw new GenericRuntimeOPException("Unable to stop Moquette server.", e);
		}
		log.info("The Moquette server has been stopped.");

		log.info("Resetting connection limits...");
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		port = port;
	}

	public int getPool() {
		return pool;
	}

	public void setPool(int pool) {
		pool = pool;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		host = host;
	}

	public String getStore() {
		return store;
	}

	public void setStore(String store) {
		store = store;
	}

	private GatewayInfo getGatewayInfo() {
		final GatewayInfo info = new GatewayInfo();
		info.setName("moquette_gateway");
		info.setProtocol("MQTT");

		return info;
	}
}
