package com.minsait.onesait.platform.tools.datagrid.configuration;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class HazelcastDatagridConfiguration {

	@Value("${hazelcast.config.elastic.memory.total.size}")
	private String hazelcastMemoryTotalSize;

	@Value("${hazelcast.config.waitsecondsbeforejoin}")
	private String hazelcastWaitSecondsBeforeJoin;

	@Value("${hazelcast.config.monitor.interval}")
	private String hazelcastMonitorInterval;

	@Value("${hazelcast.config.elastic.memory.enable}")
	private String hazelcastEnableElasticMemory;

	@Value("${hazelcast.network.port:}")
	private int hazelcastNetworkPort;

	@Value("${hazelcast.network.portautoincrement:}")
	private boolean hazelcastNetworkPortAutoIncrement;

	@Value("${hazelcast.network.members}")
	private List<String> members;

	@Value("${hazelcast.group.name}")
	private String hazelcastGroupName;
	
	@Value("${kubernetes.namespace:}")
	private String kubernetesNamespace;
	
	@Value("${kubernetes.servicename:}")
	private String kubernetesServiceName;

	private final static String HAZELCAST_LOGGING_TYPE = "hazelcast.logging.type";
	private final static String HAZELCAST_ENABLE_JMX = "hazelcast.jmx";
	private final static String HAZELCAST_ENABLE_MEMCACHE = "hazelcast.memcache.enabled";
	private final static String HAZELCAST_ENABLE_REST = "hazelcast.rest.enabled";
	private final static String HAZELCAST_WAIT_SECONDS_BEFORE_JOIN = "hazelcast.wait.seconds.before.join";
	private final static String HAZELCAST_MONITOR_INTERVAL = "hazelcast.connection.monitor.interval";
	private final static String HAZELCAST_ELASTIC_MEMORY_ENABLED = "hazelcast.elastic.memory.enabled";
	private final static String HAZELCAST_MEMORY_TOTAL_SIZE = "hazelcast.elastic.memory.total.size";

	@Bean
	@Profile("default")
	public HazelcastInstance defaultHazelcastInstanceEmbedded() {
		log.info("Build hazelcast instance for default profile");
		Config config = defaultHazelcastConfig();
		return Hazelcast.newHazelcastInstance(config);
	}

	@Bean
	@Profile("default")
	public Config defaultHazelcastConfig() {
		log.info("Build hazelcast config for default profile");
		Config config = commonConfig();

		NetworkConfig network = config.getNetworkConfig();
		network.setPort(hazelcastNetworkPort);
		network.setPortAutoIncrement(hazelcastNetworkPortAutoIncrement);

		JoinConfig join = network.getJoin();
		join.getMulticastConfig().setEnabled(true);
		
		this.members.forEach(member -> {
			join.getTcpIpConfig().addMember(member);
		});

		return config;
	}

	@Bean
	@Profile("docker")
	public HazelcastInstance dockerHazelcastInstanceEmbedded() {
		log.info("Build hazelcast instance for docker profile");
		Config config = hazelcastConfig();
		return Hazelcast.newHazelcastInstance(config);
	}

	@Bean
	@Profile("docker")
	public Config hazelcastConfig() {
		log.info("Build hazelcast config for docker profile");
		Config config = commonConfig();
		
		JoinConfig joinConfig = config.getNetworkConfig().getJoin();
		joinConfig.getMulticastConfig().setEnabled(false);
		
		joinConfig.getKubernetesConfig().setEnabled(true).setProperty("namespace", kubernetesNamespace)
				.setProperty("service-name", kubernetesServiceName);
		return config;
	}
	
	private Config commonConfig() {
		Config config = new Config();

		config.setProperty(HAZELCAST_LOGGING_TYPE, "slf4j");
		config.setProperty(HAZELCAST_ENABLE_JMX, "true");
		config.setProperty(HAZELCAST_ENABLE_MEMCACHE, "true");
		config.setProperty(HAZELCAST_ENABLE_REST, "true");
		config.setProperty(HAZELCAST_WAIT_SECONDS_BEFORE_JOIN, hazelcastWaitSecondsBeforeJoin);
		config.setProperty(HAZELCAST_MONITOR_INTERVAL, hazelcastMonitorInterval);
		config.setProperty(HAZELCAST_ELASTIC_MEMORY_ENABLED, hazelcastEnableElasticMemory);
		config.setProperty(HAZELCAST_MEMORY_TOTAL_SIZE, hazelcastMemoryTotalSize);

		GroupConfig groupConfig = config.getGroupConfig();
		groupConfig.setName(hazelcastGroupName);
		config.setGroupConfig(groupConfig);
		

		return config;
	}
}
