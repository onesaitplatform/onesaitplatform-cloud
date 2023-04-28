package com.minsait.onesait.payments.tools.streamsets.hazelcast.params;

import com.streamsets.pipeline.api.ConfigDef;

public class HazelcastConfigBean {
	
	@ConfigDef(required = true, type = ConfigDef.Type.BOOLEAN, defaultValue = "true", label = "Use kubernetes discovery", description = "Use kubernetes discovery", displayPosition = 10, group = "HAZELCAST_SERVER_CONFIG")
	public boolean kubernetesCluster;

	@ConfigDef(required = false, type = ConfigDef.Type.STRING, defaultValue = "<server>:<port>", label = "Hazelcast servers", displayPosition = 10, group = "HAZELCAST_SERVER_CONFIG", dependsOn = "kubernetesCluster", triggeredByValue = "false")
	public String hazelcastServers;

	@ConfigDef(required = false, type = ConfigDef.Type.STRING, defaultValue = "<cluster_namespace_in_kubernetes>", label = "Cluster Namespace in kubernetes", displayPosition = 10, group = "HAZELCAST_SERVER_CONFIG", dependsOn = "kubernetesCluster", triggeredByValue = "true")
	public String hazelcastClientKubernetesNamespace;

	@ConfigDef(required = false, type = ConfigDef.Type.STRING, defaultValue = "<cluster_service_in_kubernetes>", label = "Cluster Service in kubernetes", displayPosition = 10, group = "HAZELCAST_SERVER_CONFIG", dependsOn = "kubernetesCluster", triggeredByValue = "true")
	public String hazelcastClientKubernetesService;

	@ConfigDef(required = true, type = ConfigDef.Type.STRING, defaultValue = "", label = "Hazelcast group name", displayPosition = 10, group = "HAZELCAST_SERVER_CONFIG")
	public String hazelcastGroupName;

	@ConfigDef(required = true, type = ConfigDef.Type.NUMBER, defaultValue = "20000", label = "Hazelcast connection attemp period", displayPosition = 10, group = "HAZELCAST_CONNECTION_CONFIG")
	public int hazelcastConnectionAttemptPeriod;

	@ConfigDef(required = true, type = ConfigDef.Type.NUMBER, defaultValue = "0", label = "Hazelcast connection attemp limit", displayPosition = 10, group = "HAZELCAST_CONNECTION_CONFIG")
	public int hazelcastConnectionAttemptLimit;

	@ConfigDef(required = true, type = ConfigDef.Type.NUMBER, defaultValue = "5000", label = "Hazelcast connection timeout", displayPosition = 10, group = "HAZELCAST_CONNECTION_CONFIG")
	public int hazelcastConnectionTimeout;

	@ConfigDef(required = true, type = ConfigDef.Type.STRING, defaultValue = "20", label = "Hazelcast invocation timeout seconds", displayPosition = 10, group = "HAZELCAST_CONNECTION_CONFIG")
	public String hazelcastInvocationTimeoutSeconds;

	public boolean isKubernetesCluster() {
		return kubernetesCluster;
	}

	public String getHazelcastServers() {
		return hazelcastServers;
	}

	public String getHazelcastClientKubernetesNamespace() {
		return hazelcastClientKubernetesNamespace;
	}

	public String getHazelcastClientKubernetesService() {
		return hazelcastClientKubernetesService;
	}

	public String getHazelcastGroupName() {
		return hazelcastGroupName;
	}

	public int getHazelcastConnectionAttemptPeriod() {
		return hazelcastConnectionAttemptPeriod;
	}

	public int getHazelcastConnectionAttemptLimit() {
		return hazelcastConnectionAttemptLimit;
	}

	public int getHazelcastConnectionTimeout() {
		return hazelcastConnectionTimeout;
	}

	public String getHazelcastInvocationTimeoutSeconds() {
		return hazelcastInvocationTimeoutSeconds;
	}

}
