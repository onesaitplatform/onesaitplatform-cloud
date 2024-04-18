package com.minsait.onesait.payments.tools.streamsets.hazelcast.params;

public interface IHazelcastConfigurableComponent {
	
	public boolean isKubernetesCluster();

	public String getHazelcastClientKubernetesNamespace();

	public String getHazelcastClientKubernetesService();

	public String getHazelcastServers();

	public String getHazelcastGroupName();

	public int getHazelcastConnectionAttemptPeriod();

	public int getHazelcastConnectionAttemptLimit();

	public int getHazelcastConnectionTimeout();

	public String getHazelcastInvocationTimeoutSeconds();
}
