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
package com.minsait.onesait.platform.iotbroker.plugable.impl.gateway.reference.mqtt.security;

import java.util.List;
import java.util.Properties;

import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformOntology;
import com.minsait.onesait.platform.config.model.Ontology.AccessType;
import com.minsait.onesait.platform.config.repository.ClientPlatformOntologyRepository;
import com.minsait.onesait.platform.config.repository.ClientPlatformRepository;
import com.minsait.onesait.platform.multitenant.util.BeanUtil;

import io.moquette.broker.security.IAuthorizatorPolicy;
import io.moquette.broker.subscriptions.Topic;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthorizatorPolicy implements IAuthorizatorPolicy {

	private ClientPlatformOntologyRepository clientPlatformOntologyRepository = BeanUtil
			.getBean(ClientPlatformOntologyRepository.class);
	private ClientPlatformRepository clientPlatformRepository = BeanUtil.getBean(ClientPlatformRepository.class);

	private static String TOPIC = null;

	static final Properties properties = new Properties();
	static {
		try {
			properties.load(AuthorizatorPolicy.class.getClassLoader().getResourceAsStream("application.yml"));
			TOPIC = (String) properties.get("onesaitplatform.iotbroker.plugable.gateway.moquette.outbound_topic");
			if (TOPIC == null)
				TOPIC = "/message";

		} catch (final Exception e) {
			log.error("Could not load properties file 'application.yml'");
			TOPIC = "/message";
		}

	}

	@Override
	public boolean canWrite(Topic topic, String user, String client) {
		if ((user == null || client == null) && topic.toString().equals(TOPIC)) {
			// MQTT con protrocolo Plataforma no valida user/pass
			return true;
		}
		// Soporte MQTT nativo, validamos user/pass
		try {
			String[] split = user.split(":");
			if (split.length != 1)
				user = split[0];

			String entity = topic.getTokens().get(topic.getTokens().size() - 1).toString();
			ClientPlatform clientPlatform = clientPlatformRepository.findByIdentification(user);
			if (clientPlatform == null) {
				log.error("Error publishing data by MQTT connection. clientPlatform {} doesn't exists.", user);
				return false;
			}

			List<ClientPlatformOntology> cpOntology = clientPlatformOntologyRepository
					.findByClientPlatform(clientPlatform);

			for (ClientPlatformOntology cpo : cpOntology) {
				if (cpo.getOntology().getIdentification().equals(entity) && cpo.getOntology().isAllowsCreateMqttTopic()
						&& (cpo.getAccess().equals(AccessType.ALL) || cpo.getAccess().equals(AccessType.INSERT)))
					return true;
			}
			return false;
		} catch (Exception e) {
			log.error("Error publishing data by MQTT Native.", e);
			return false;
		}
	}

	@Override
	public boolean canRead(Topic topic, String user, String client) {
		if ((user == null || client == null) && topic.toString().equals(TOPIC)) {
			// MQTT con protrocolo Plataforma no valida user/pass
			return true;
		}
		// Soporte MQTT nativo, validamos user/pass
		try {
			String[] split = user.split(":");
			if (split.length != 1)
				user = split[0];

			String entity = topic.getTokens().get(topic.getTokens().size() - 2).toString();
			ClientPlatform clientPlatform = clientPlatformRepository.findByIdentification(user);
			if (clientPlatform == null) {
				log.error("Error publishing data by MQTT connection. clientPlatform {} doesn't exists.", user);
				return false;
			}
			List<ClientPlatformOntology> cpOntology = clientPlatformOntologyRepository
					.findByClientPlatform(clientPlatform);

			for (ClientPlatformOntology cpo : cpOntology) {
				if (cpo.getOntology().getIdentification().equals(entity)
						&& (cpo.getAccess().equals(AccessType.ALL) || cpo.getAccess().equals(AccessType.QUERY)))
					return true;
			}
			return false;
		} catch (Exception e) {
			log.error("Error reading data by MQTT.", e);
			return false;
		}
	}

}
