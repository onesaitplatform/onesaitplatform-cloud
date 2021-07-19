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
package com.minsait.onesait.platform.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.WebSocketMessageBrokerStats;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
@ComponentScan("com.minsait.onesait.platform.controller")
public class SocketBrokerConfig implements WebSocketMessageBrokerConfigurer {

	@Value("${onesaitplatform.dashboardengine.protocol.ws.inboundChannelCorePool:40}")
	private int inboundChannelCorePool;

	@Value("${onesaitplatform.dashboardengine.protocol.ws.inboundChannelMaxPool:40}")
	private int inboundChannelMaxPool;

	@Value("#{new String('${onesaitplatform.dashboardengine.protocol.ws.inboundChannelQueueSize:null}') == 'null' || new String('${onesaitplatform.dashboardengine.protocol.ws.inboundChannelQueueSize:null}') == '' ? "
			+ Integer.MAX_VALUE
			+ " : new Integer('${onesaitplatform.dashboardengine.protocol.ws.inboundChannelQueueSize:1000000}')}")
	private int inboundChannelQueueSize;

	@Value("${onesaitplatform.dashboardengine.protocol.ws.outboundChannelCorePool:20}")
	private int outboundChannelCorePool;

	@Value("${onesaitplatform.dashboardengine.protocol.ws.outboundChannelMaxPool:20}")
	private int outboundChannelMaxPool;

	@Value("#{new String('${onesaitplatform.dashboardengine.protocol.ws.outboundChannelQueueSize:null}') == 'null' || new String('${onesaitplatform.dashboardengine.protocol.ws.outboundChannelQueueSize:null}') == '' ? "
			+ Integer.MAX_VALUE
			+ " : new Integer('${onesaitplatform.dashboardengine.protocol.ws.outboundChannelQueueSize:1000000}')}")
	private int outboundChannelQueueSize;

	@Value("${onesaitplatform.dashboardengine.protocol.ws.brokerChannelCorePool:40}")
	private int brokerChannelCorePool;

	@Value("${onesaitplatform.dashboardengine.protocol.ws.brokerChannelMaxPool:40}")
	private int brokerChannelMaxPool;

	@Value("#{new String('${onesaitplatform.dashboardengine.protocol.ws.brokerChannelQueueSize:null}') == 'null' || new String('${onesaitplatform.dashboardengine.protocol.ws.brokerChannelQueueSize:null}') == '' ? "
			+ Integer.MAX_VALUE
			+ " : new Integer('${onesaitplatform.dashboardengine.protocol.ws.brokerChannelQueueSize:1000000}')}")
	private int brokerChannelQueueSize;

	@Value("${onesaitplatform.dashboardengine.protocol.ws.cacheLimit:4096}")
	private int cacheLimit;

	@Value("${onesaitplatform.dashboardengine.protocol.ws.LoggingPeriod:30000}")
	private int loggingPeriod;

	@Value("${onesaitplatform.dashboardengine.server.heartbeat:2000}")
	private long serverHeartbeatTime;

	@Autowired
	private WebSocketMessageBrokerStats webSocketMessageBrokerStats;

	@PostConstruct
	public void init() {
		webSocketMessageBrokerStats.setLoggingPeriod(loggingPeriod); // desired time in millis
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.configureBrokerChannel().taskExecutor().queueCapacity(brokerChannelQueueSize)
				.corePoolSize(brokerChannelCorePool).maxPoolSize(brokerChannelMaxPool);
		config.setCacheLimit(cacheLimit);
		config.enableSimpleBroker("/dsengine/broker");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/dsengine/solver").setAllowedOrigins("*").withSockJS()
				.setHeartbeatTime(serverHeartbeatTime);
	}

	@Override
	public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
		registration.setSendTimeLimit(60 * 1000).setSendBufferSizeLimit(200 * 1024 * 1024)
				.setMessageSizeLimit(200 * 1024 * 1024);
	}

	@Override
	public void configureClientOutboundChannel(ChannelRegistration registration) {
		registration.taskExecutor().queueCapacity(outboundChannelQueueSize).corePoolSize(outboundChannelCorePool)
				.maxPoolSize(outboundChannelMaxPool);
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.taskExecutor().queueCapacity(inboundChannelQueueSize).corePoolSize(inboundChannelCorePool)
				.maxPoolSize(inboundChannelMaxPool);
	}

}
