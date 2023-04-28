package com.minsait.onesait.payments.tools.streamsets.hazelcast.connection;

import java.util.Arrays;
import java.util.List;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.HazelcastInstance;

public class HazelcastConnection {
	
	public static HazelcastInstance createHazelcastInstanceByTcpIpSPI(String hazelcastServers, String hazelcastInvocationTimeoutSeconds, String hazelcastGroupName, int hazelcastConnectionAttemptPeriod, int hazelcastConnectionAttemptLimit, int hazelcastConnectionTimeout) {
		
		ClientConfig clientConfig= createCommonConfig(hazelcastInvocationTimeoutSeconds, hazelcastGroupName, hazelcastConnectionAttemptPeriod, hazelcastConnectionAttemptLimit, hazelcastConnectionTimeout);
		
		List<String> addresses = Arrays.asList(hazelcastServers.split(","));

		clientConfig.getNetworkConfig().setAddresses(addresses);
		
		return HazelcastClient.newHazelcastClient(clientConfig);
	}
	
	public static HazelcastInstance createHazelcastInstanceByKubernetesSPI(String hazelcastClientKubernetesNamespace, String hazelcastClientKubernetesService, String hazelcastInvocationTimeoutSeconds, String hazelcastGroupName, int hazelcastConnectionAttemptPeriod, int hazelcastConnectionAttemptLimit, int hazelcastConnectionTimeout) {
		
		ClientConfig clientConfig= createCommonConfig(hazelcastInvocationTimeoutSeconds, hazelcastGroupName, hazelcastConnectionAttemptPeriod, hazelcastConnectionAttemptLimit, hazelcastConnectionTimeout);
		
		clientConfig.getNetworkConfig().getKubernetesConfig().setEnabled(true).setProperty("namespace", hazelcastClientKubernetesNamespace).setProperty("service-name", hazelcastClientKubernetesService);
		
		return HazelcastClient.newHazelcastClient(clientConfig);
	}
	
	private static ClientConfig createCommonConfig(String hazelcastInvocationTimeoutSeconds, String hazelcastGroupName, int hazelcastConnectionAttemptPeriod, int hazelcastConnectionAttemptLimit, int hazelcastConnectionTimeout) {
		ClientConfig config = new ClientConfig();
		config.setProperty("hazelcast.client.invocation.timeout.seconds", hazelcastInvocationTimeoutSeconds);

		GroupConfig groupConfig = config.getGroupConfig();
		groupConfig.setName(hazelcastGroupName);

		ClientNetworkConfig network = config.getNetworkConfig();
		network.setConnectionAttemptPeriod(hazelcastConnectionAttemptPeriod);
		network.setConnectionAttemptLimit(hazelcastConnectionAttemptLimit);
		network.setConnectionTimeout(hazelcastConnectionTimeout);
		
		return config;
	}

}
