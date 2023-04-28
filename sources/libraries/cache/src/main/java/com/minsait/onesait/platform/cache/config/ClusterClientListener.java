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
package com.minsait.onesait.platform.cache.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.hazelcast.client.Client;
import com.hazelcast.client.ClientListener;
import com.hazelcast.core.HazelcastInstance;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ClusterClientListener implements ClientListener {

	@Autowired(required = false)
	@Qualifier("globalCache")
	private HazelcastInstance instance;

	@Override
	public void clientConnected(Client client) {
		log.info("Cache. Client Connected: " + client.getName());
		log.info("Cache. Info Added: " + client.getUuid());
	}

	@Override
	public void clientDisconnected(Client client) {
		log.info("Cache. Client Disconnected: " + client.getName());

	}

}
