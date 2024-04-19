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
package com.minsait.onesait.platform.cache.config;

import java.net.UnknownHostException;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionConfig.MaxSizePolicy;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.core.Client;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import com.minsait.onesait.platform.cache.listener.ClusterMembershipListener;
import com.minsait.onesait.platform.cache.listener.HzDistributedObjectListener;
import com.minsait.onesait.platform.cache.listener.NodeLifecycleListener;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@ConditionalOnResource(resources = { "hazelcast.xml" })
public class HazelcastCacheLoader {

	@Value("${onesaitplatform.hazelcast.service.discovery.strategy:service}")
	private String hazelcastServiceDiscoveryStrategy;

	@Value("${onesaitplatform.transaction.timeout.seconds:60}")
	private int transactionTimeout;
	@Value("${onesaitplatform.oauth.cache.timeout.seconds:900}")
	private int revokeTokenTimeout;
	@Value("${onesaitplatform.oauth.cache.size:100000}")
	private int revokeTokenSize;
	@Value("${onesaitplatform.oauth.cache.maxsize.policy:PER_NODE}")
	private String revokeTokenMaxSizePolicy;

	@Bean(name = "globalCache")
	@Primary
	@Profile("default")
	public HazelcastInstance defaultHazelcastInstanceEmbedded() {
		final Config config = new ClasspathXmlConfig("hazelcast.xml");
		log.info("Configured Local Cache with data: Name : " + config.getConfigurationFile() + " Instance Name: "
				+ config.getInstanceName() + " Group Name: " + config.getGroupConfig().getName());
		try {
			config.getMapConfig("transactionalOperations").setTimeToLiveSeconds(transactionTimeout);
			config.getMapConfig("lockedOntologies").setTimeToLiveSeconds(transactionTimeout);
			config.getMapConfig("revokedTokens").setTimeToLiveSeconds(revokeTokenTimeout)
			.setMaxSizeConfig(new MaxSizeConfig(revokeTokenSize,
					com.hazelcast.config.MaxSizeConfig.MaxSizePolicy.valueOf(revokeTokenMaxSizePolicy)))
			.setEvictionPolicy(EvictionPolicy.LFU);
		} catch (final Exception e) {
			log.info("ignoring maps transactionalOperations and lockedOntologies");
		}
		addCacheConfig(config, "MasterUserRepository");
		addCacheConfig(config, "VerticalRepository");
		addCacheConfig(config, "MasterUserRepositoryLazy");

		return Hazelcast.newHazelcastInstance(config);
	}

	@Bean(name = "globalCache")
	@Primary
	@Profile("docker")
	public HazelcastInstance dockerHazelcastInstanceEmbedded(Environment environment) {
		final Properties props = new Properties();
		if (hazelcastServiceDiscoveryStrategy.equals("zookeeper")) {
			props.put("onesaitplatform.hazelcast.service.discovery.zookeeper.url",
					environment.getProperty("onesaitplatform.hazelcast.service.discovery.zookeeper.url"));
		}
		final Config config = new ClasspathXmlConfig("hazelcast-" + hazelcastServiceDiscoveryStrategy + "-docker.xml",
				props);
		log.info("Configured Local Cache with data: Name : " + config.getConfigurationFile() + " Instance Name: "
				+ config.getInstanceName() + " Group Name: " + config.getGroupConfig().getName());
		try {
			config.getMapConfig("transactionalOperations").setTimeToLiveSeconds(transactionTimeout);
			config.getMapConfig("lockedOntologies").setTimeToLiveSeconds(transactionTimeout);
			config.getMapConfig("revokedTokens").setTimeToLiveSeconds(revokeTokenTimeout)
			.setMaxSizeConfig(new MaxSizeConfig(revokeTokenSize,
					com.hazelcast.config.MaxSizeConfig.MaxSizePolicy.valueOf(revokeTokenMaxSizePolicy)))
			.setEvictionPolicy(EvictionPolicy.LFU);
		} catch (final Exception e) {
			log.info("ignoring maps transactionalOperations and lockedOntologies");
		}
		addCacheConfig(config, "MasterUserRepository");
		addCacheConfig(config, "VerticalRepository");
		addCacheConfig(config, "MasterUserRepositoryLazy");
		return Hazelcast.newHazelcastInstance(config);
	}

	@Bean
	public CacheManager cacheManager(HazelcastInstance hazelcastInstance) {
		if (hazelcastInstance != null) {
			hazelcastInstance.getCluster().addMembershipListener(new ClusterMembershipListener());
			final HzDistributedObjectListener sample = new HzDistributedObjectListener();
			hazelcastInstance.addDistributedObjectListener(sample);
			hazelcastInstance.getLifecycleService().addLifecycleListener(new NodeLifecycleListener());
			hazelcastInstance.getClientService().addClientListener(new ClusterClientListener() {
				@Override
				public void clientConnected(Client client) {
					log.info("Cache. Client Connected: " + client.getName());
					log.info("Cache. Info Added: " + client.getUuid());
				}

				@Override
				public void clientDisconnected(Client client) {
					log.info("Cache. Client Disconnected: " + client.getName());
					try {
						final IQueue<String> disconectedClientsQueue = hazelcastInstance
								.getQueue("disconectedClientsQueue");
						disconectedClientsQueue
						.put(client.getSocketAddress().getAddress().getLocalHost().getHostName());
						final IQueue<String> disconectedClientsSubscription = hazelcastInstance
								.getQueue("disconectedClientsSubscription");
						disconectedClientsSubscription
						.put(client.getSocketAddress().getAddress().getLocalHost().getHostName());
						log.info("Cache. Info Added to the queue: " + client.getSocketAddress().getHostName() + ":"
								+ client.getSocketAddress().getPort());
					} catch (InterruptedException | UnknownHostException e) {
						log.error("Error inserting disconnected client to the queue {}", e);
					}

				}
			});
			hazelcastInstance.getClientService().addClientListener(new ClusterClientListener());
			final CacheManager manager = new HazelcastCacheManagerOP(hazelcastInstance);
			log.info("Configured Local Cache Manager: Name : " + manager.toString());
			return manager;
		} else {
			return new NoOpCacheManager();
		}
	}

	private void addCacheConfig(Config config, String cacheName) {
		final NearCacheConfig nearCacheConfig = new NearCacheConfig().setInMemoryFormat(InMemoryFormat.OBJECT)
				.setCacheLocalEntries(true).setInvalidateOnChange(false).setTimeToLiveSeconds(600)
				.setEvictionConfig(new EvictionConfig().setMaximumSizePolicy(MaxSizePolicy.ENTRY_COUNT).setSize(5000)
						.setEvictionPolicy(EvictionPolicy.LRU));
		config.getMapConfig(cacheName).setInMemoryFormat(InMemoryFormat.BINARY).setNearCacheConfig(nearCacheConfig);
	}
}
