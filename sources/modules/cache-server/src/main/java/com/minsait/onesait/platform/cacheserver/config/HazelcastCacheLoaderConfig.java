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
package com.minsait.onesait.platform.cacheserver.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class HazelcastCacheLoaderConfig {

	@Value("${onesaitplatform.hazelcast.service.discovery.strategy:service}")
	private String hazelcastServiceDiscoveryStrategy;

	@Autowired
	private Environment environment;

	@Bean
	@Profile("default")
	public HazelcastInstance defaultHazelcastInstanceEmbedded() {
		Config config = new ClasspathXmlConfig("hazelcast.xml");
		log.info("Configured Cache with data: Name : {} Instance Name: {} Group Name: {} ",
				config.getConfigurationFile(), config.getInstanceName(),  config.getGroupConfig().getName());
		return Hazelcast.newHazelcastInstance(config);
	}

	@Bean
	@Profile("docker")
	public HazelcastInstance dockerHazelcastInstanceEmbedded() {
		Properties props = new Properties();
		if (hazelcastServiceDiscoveryStrategy.equals("zookeeper")) {
			props.put("onesaitplatform.hazelcast.service.discovery.zookeeper.url",
					environment.getProperty("onesaitplatform.hazelcast.service.discovery.zookeeper.url"));
		}
		Config config = new ClasspathXmlConfig("hazelcast-" + hazelcastServiceDiscoveryStrategy + "-docker.xml", props);

		log.info("Configured Cache with data: Name : " + config.getConfigurationFile() + " Instance Name: "
				+ config.getInstanceName() + " Group Name: " + config.getGroupConfig().getName());
		return Hazelcast.newHazelcastInstance(config);
	}
}
