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
package com.minsait.onesait.platform.simulator.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.minsait.onesait.platform.config.model.Token;
import com.minsait.onesait.platform.config.services.client.ClientPlatformService;
import com.minsait.onesait.platform.config.services.token.TokenService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Service
@EnableScheduling
@Slf4j
public class IoTBrokerClientImpl implements IoTBrokerClient {

	@Autowired
	private RestService restService;
	@Autowired
	private ClientPlatformService clientPlatformService;
	@Autowired
	private TokenService tokenService;
	@Autowired
	private IntegrationResourcesService intregationResourcesService;
	private static final String UNAUTHORIZED_ONTOLOGY = "Unauthorized ontology";
	@Getter
	private Map<String, String> sessionKeys;
	private List<String> deviceBlackList;

	private String iotbrokerUrl;

	@PostConstruct
	public void setUp() {
		this.sessionKeys = new HashMap<>();
		this.iotbrokerUrl = this.intregationResourcesService.getUrl(Module.IOTBROKER, ServiceUrl.BASE);
		this.deviceBlackList = new ArrayList<>();
	}

	public void connectDeviceRest(String clientPlatform, String clientPlatformInstance) {
		final Token token = this.tokenService.getToken(this.clientPlatformService.getByIdentification(clientPlatform));
		try {
			final JsonNode response = this.restService.connectRest(iotbrokerUrl, token, clientPlatform,
					clientPlatformInstance);
			String sessionKey = response.get("sessionKey").asText();
			log.info("Session Key :" + sessionKey);
			if (sessionKey != null)
				this.sessionKeys.put(clientPlatform, sessionKey);
		} catch (RuntimeException e) {
			log.error("Fail to connect to IoTBroker", e);
			throw e;
		}

	}

	@Override
	public void insertOntologyInstance(String instance, String ontology, String user, String clientPlatform,
			String clientPlatformInstance) throws IOException {

		if (this.sessionKeys.get(clientPlatform) != null) {

			JsonNode response = this.restService.insertRest(iotbrokerUrl, instance, ontology,
					this.sessionKeys.get(clientPlatform));
			if (log.isDebugEnabled()) {
				log.debug("Response from Rest Service: {}", response.asText());
			}			

			if (response.path("id").isMissingNode()
					&& response.asText().equals(IoTBrokerClientImpl.UNAUTHORIZED_ONTOLOGY)) {
				log.debug("Attemping to renew session key");
				this.connectDeviceRest(clientPlatform, clientPlatformInstance);
				this.insertOntologyInstance(instance, ontology, user, clientPlatform, clientPlatformInstance);
			}

			if (this.deviceBlackList.contains(clientPlatform))
				this.deviceBlackList.remove(clientPlatform);
			if (log.isDebugEnabled()) {
				log.debug("Device {}", clientPlatformInstance);
			}			
		} else {
			this.connectDeviceRest(clientPlatform, clientPlatformInstance);
			this.insertOntologyInstance(instance, ontology, user, clientPlatform, clientPlatformInstance);
		}
	}

	@Override
	public void disconnectDeviceRest(String identification) {
		this.restService.disconnectRest(iotbrokerUrl, this.sessionKeys.get(identification));
		this.sessionKeys.remove(identification);
		log.info("Closed session for device " + identification);
	}

	@Scheduled(fixedDelay = 60000)
	private void disconnectBlackListedDevices() {
		for (Iterator<String> iterator = this.deviceBlackList.iterator(); iterator.hasNext();) {
			String device = iterator.next();
			this.disconnectDeviceRest(device);
			iterator.remove();
		}
		for (Map.Entry<String, String> entry : this.sessionKeys.entrySet()) {
			this.deviceBlackList.add(entry.getKey());
		}
	}

}