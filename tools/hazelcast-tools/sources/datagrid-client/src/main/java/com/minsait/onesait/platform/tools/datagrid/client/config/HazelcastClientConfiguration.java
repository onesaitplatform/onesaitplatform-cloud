package com.minsait.onesait.platform.tools.datagrid.client.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.HazelcastInstance;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class HazelcastClientConfiguration {

	
	@Value("${hazelcast.group.name}")
	private String hazelcastGroup;
	
	@Value("${hazelcast.client.invocation.timeout.seconds}")
	private String hazelcatClientInvocationTimeoutSeconds;
	
	@Value("${hazelcast.network.cluster.members}")
	private List<String> members;
	
	@Value("${hazelcast.client.connection.attemp.period}")
	private int hazelcatClientConnectionAttempPeriod;
	
	@Value("${hazelcast.client.connection.attemp.limit}")
	private int hazelcatClientConnectionAttempLimit;
	
	@Value("${hazelcast.client.connection.timeout}")
	private int hazelcatClientConnectionTimeout;
	
	@Value("${hazelcast.client.kubernetes.namespace:}")
	private String hazelcatClientKubernetesNamespace;
	
	@Value("${hazelcast.client.kubernetes.service:}")
	private String hazelcatClientKubernetesService;
	
	
	@Bean
	@Profile("default")
	public HazelcastInstance defaultHazelcastClient() {
		HazelcastInstance hzClient = HazelcastClient.newHazelcastClient(defaultHazelcastConfig());
		
		return hzClient;
	}
	
	@Bean
	@Profile("default")
	public ClientConfig defaultHazelcastConfig() {
		
		ClientConfig clientConfig = this.commonConfig();
		clientConfig.getNetworkConfig().setAddresses(members);
		
		return clientConfig;
	}
	
	
	@Bean
	@Profile("docker")
	public HazelcastInstance dockerHazelcastInstanceEmbedded() {
		log.info("Build hazelcast instance for docker profile");
		ClientConfig config = hazelcastConfig();
		return HazelcastClient.newHazelcastClient(config);
	}

	@Bean
	@Profile("docker")
	public ClientConfig hazelcastConfig() {
		ClientConfig clientConfig = this.commonConfig();
		clientConfig.getNetworkConfig().getKubernetesConfig().setEnabled(true).setProperty("namespace", hazelcatClientKubernetesNamespace).setProperty("service-name", hazelcatClientKubernetesService);
		
		return clientConfig;
		

	}	
	
	private ClientConfig commonConfig() {
		ClientConfig config = new ClientConfig();
		config.setProperty("hazelcast.client.invocation.timeout.seconds", hazelcatClientInvocationTimeoutSeconds);
		
		GroupConfig groupConfig = config.getGroupConfig();
		groupConfig.setName(hazelcastGroup);
				
		ClientNetworkConfig network = config.getNetworkConfig();
		network.setConnectionAttemptPeriod(hazelcatClientConnectionAttempPeriod);
		network.setConnectionAttemptLimit(hazelcatClientConnectionAttempLimit);
		network.setConnectionTimeout(hazelcatClientConnectionTimeout);
		
		return config;
	}
}
