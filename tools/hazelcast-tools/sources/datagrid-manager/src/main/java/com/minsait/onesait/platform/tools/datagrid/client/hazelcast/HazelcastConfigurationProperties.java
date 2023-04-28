package com.minsait.onesait.platform.tools.datagrid.client.hazelcast;

import java.util.List;

import lombok.Data;

@Data
public class HazelcastConfigurationProperties {
	
	public enum ConnectionType {
		TCP,
		KUBERNETES
	}
	
	private ConnectionType connectionType;

	private String hazelcastGroup;
	
	private String hazelcastClientInvocationTimeoutSeconds;
	
	private List<String> members;
	
	private int hazelcastClientConnectionAttempPeriod;
	
	private int hazelcastClientConnectionAttempLimit;
	
	private int hazelcastClientConnectionTimeout;
	
	private String hazelcastClientKubernetesNamespace;
	
	private String hazelcastClientKubernetesService;
	
}
