package com.minsait.onesait.payments.tools.streamsets.hazelcast.processor;

import com.minsait.onesait.payments.tools.streamsets.hazelcast.params.Groups;
import com.minsait.onesait.payments.tools.streamsets.hazelcast.params.HazelcastConfigBean;
import com.streamsets.pipeline.api.ConfigDefBean;
import com.streamsets.pipeline.api.ConfigGroups;
import com.streamsets.pipeline.api.GenerateResourceBundle;
import com.streamsets.pipeline.api.StageDef;


@StageDef(version = 1, label = "Hazelcast Lookup", description = "Hazelcast Lookup", icon = "hazelcast.png", onlineHelpRefUrl = "")
@ConfigGroups(Groups.class)
@GenerateResourceBundle
public class HazelcastDProcessor extends HazelcastProcessor {

	@ConfigDefBean
	public HazelcastConfigBean configs;

	@Override
	public boolean isKubernetesCluster() {
		return configs.isKubernetesCluster();
	}

	@Override
	public String getHazelcastClientKubernetesNamespace() {
		return configs.getHazelcastClientKubernetesNamespace();
	}

	@Override
	public String getHazelcastClientKubernetesService() {
		return configs.getHazelcastClientKubernetesService();
	}

	@Override
	public String getHazelcastServers() {
		return configs.getHazelcastServers();
	}

	@Override
	public String getHazelcastGroupName() {
		return configs.getHazelcastGroupName();
	}

	@Override
	public int getHazelcastConnectionAttemptPeriod() {
		return configs.getHazelcastConnectionAttemptPeriod();
	}

	@Override
	public int getHazelcastConnectionAttemptLimit() {
		return configs.getHazelcastConnectionAttemptLimit();
	}

	@Override
	public int getHazelcastConnectionTimeout() {
		return configs.getHazelcastConnectionTimeout();
	}

	@Override
	public String getHazelcastInvocationTimeoutSeconds() {
		return configs.getHazelcastInvocationTimeoutSeconds();
	}


}