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
package com.minsait.onesait.platform.videobroker.config;

import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.core.HazelcastInstance;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class HazelcastConfig {

	@Value("${onesaitplatform.hazelcast.service.discovery.strategy:service}")
	private String hazelcastServiceDiscoveryStrategy;

	@Bean(name = "globalCache")
	@Primary
	@Profile("default")
	public HazelcastInstance defaultHazelcastInstanceEmbedded() throws IOException {
		final String configFile = "hazelcast-client.xml";
		final ClientConfig config = new XmlClientConfigBuilder(configFile).build();
		log.info("Configured Local Cache with data: Name : " + configFile + " Instance Name: "
				+ config.getInstanceName() + " Group Name: " + config.getGroupConfig().getName());
		return HazelcastClient.newHazelcastClient(config);
	}

	@Bean(name = "globalCache")
	@Primary
	@Profile("docker")
	public HazelcastInstance dockerHazelcastInstanceEmbedded(Environment environment) throws IOException {
		final Properties props = new Properties();
		if (hazelcastServiceDiscoveryStrategy.equals("zookeeper")) {
			props.put("onesaitplatform.hazelcast.service.discovery.zookeeper.url",
					environment.getProperty("onesaitplatform.hazelcast.service.discovery.zookeeper.url"));
		}

		final String configFile = "hazelcast-client-" + hazelcastServiceDiscoveryStrategy + "-docker.xml";

		final XmlClientConfigBuilder xmlClientConfigBuilder = new XmlClientConfigBuilder(configFile);
		xmlClientConfigBuilder.setProperties(props);
		final ClientConfig config = xmlClientConfigBuilder.build();

		log.info("Configured Local Cache with data: Name : " + configFile + " Instance Name: "
				+ config.getInstanceName() + " Group Name: " + config.getGroupConfig().getName());

		return HazelcastClient.newHazelcastClient(config);
	}
}
