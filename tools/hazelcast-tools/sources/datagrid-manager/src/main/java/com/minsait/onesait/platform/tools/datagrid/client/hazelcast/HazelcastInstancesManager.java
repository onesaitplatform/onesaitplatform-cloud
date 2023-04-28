package com.minsait.onesait.platform.tools.datagrid.client.hazelcast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.LifecycleEvent.LifecycleState;
import com.minsait.onesait.platform.tools.datagrid.client.exception.DuplicateInstanceException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class HazelcastInstancesManager {
	
	@Value("${hazelcast.client.invocation.timeout.seconds}")
	private String hazelcastClientInvocationTimeoutSeconds;
	
	@Value("${hazelcast.client.connection.attemp.period}")
	private int hazelcastClientConnectionAttempPeriod;
	
	@Value("${hazelcast.client.connection.attemp.limit}")
	private int hazelcastClientConnectionAttempLimit;
	
	@Value("${hazelcast.client.connection.timeout}")
	private int hazelcastClientConnectionTimeout;
	
	
	
	private Map<String, HazelcastInstance> hazelcastInstances;
	private Map<String, HazelcastInstanceLifecycleListener> hazelcastLifecycleListeners;
	
	@PostConstruct
	public void init() {
		this.hazelcastInstances = new HashMap<String, HazelcastInstance>();
		this.hazelcastLifecycleListeners = new HashMap<String, HazelcastInstanceLifecycleListener>();
	}
	
	
	public void createHazelcastInstance(String instanceId, HazelcastConfigurationProperties config) throws DuplicateInstanceException {
		if(this.hazelcastInstances.containsKey(instanceId)){
			throw new DuplicateInstanceException("Intance with identifier: "+instanceId+" currently exists");
		}
		
		config.setHazelcastClientInvocationTimeoutSeconds(hazelcastClientInvocationTimeoutSeconds);
		config.setHazelcastClientConnectionAttempPeriod(hazelcastClientConnectionAttempPeriod);
		config.setHazelcastClientConnectionAttempLimit(hazelcastClientConnectionAttempLimit);
		config.setHazelcastClientConnectionTimeout(hazelcastClientConnectionTimeout);
		
		HazelcastInstance hazelcastInstance=null;
		switch(config.getConnectionType()){
			case TCP:        hazelcastInstance=this.buildTcpHazelcastClient(config);
					         break;
			case KUBERNETES: hazelcastInstance=this.buildKubernetesHazelcastInstanceEmbedded(config);
			                 break;
		}
		
		this.hazelcastInstances.put(instanceId, hazelcastInstance);
		this.hazelcastLifecycleListeners.put(instanceId, new HazelcastInstanceLifecycleListener());
		
		hazelcastInstance.getLifecycleService().addLifecycleListener(this.hazelcastLifecycleListeners.get(instanceId));
		
	}
	
	public HazelcastInstance getHazelcastInstance(String datagridInstance) {
		return this.hazelcastInstances.get(datagridInstance);
	}
	
	public LifecycleState getHazelcastInstanceState(String datagridInstance) {
		return this.hazelcastLifecycleListeners.get(datagridInstance).getState();
	}
	
	public List<String> listAllHazelcastInstances() {
		List<String> lInstances=new ArrayList<String>();
		this.hazelcastInstances.keySet().forEach(lInstances::add);
		
		return lInstances;
	}
	
	
	private HazelcastInstance buildTcpHazelcastClient(HazelcastConfigurationProperties config) {
		HazelcastInstance hzClient = HazelcastClient.newHazelcastClient(buildTcpHazelcastConfig(config));
		
		return hzClient;
	}
	

	private ClientConfig buildTcpHazelcastConfig(HazelcastConfigurationProperties config) {
		
		ClientConfig clientConfig = this.commonConfig(config);
		clientConfig.getNetworkConfig().setAddresses(config.getMembers());
		
		return clientConfig;
	}
	
	
	private HazelcastInstance buildKubernetesHazelcastInstanceEmbedded(HazelcastConfigurationProperties config) {
		log.info("Build hazelcast instance for docker profile");
		ClientConfig clientConfig = buildKuberneteshazelcastConfig(config);
		return HazelcastClient.newHazelcastClient(clientConfig);
	}

	
	private ClientConfig buildKuberneteshazelcastConfig(HazelcastConfigurationProperties config) {
		ClientConfig clientConfig = this.commonConfig(config);
		clientConfig.getNetworkConfig().getKubernetesConfig().setEnabled(true).setProperty("namespace", config.getHazelcastClientKubernetesNamespace())
			.setProperty("service-name", config.getHazelcastClientKubernetesService());
		
		return clientConfig;
		

	}	
	
	private ClientConfig commonConfig(HazelcastConfigurationProperties config) {
		
		ClientConfig clientConfig = new ClientConfig();
		clientConfig.setProperty("hazelcast.client.invocation.timeout.seconds", config.getHazelcastClientInvocationTimeoutSeconds());
		
		GroupConfig groupConfig = clientConfig.getGroupConfig();
		groupConfig.setName(config.getHazelcastGroup());
				
		ClientNetworkConfig network = clientConfig.getNetworkConfig();
		network.setConnectionAttemptPeriod(config.getHazelcastClientConnectionAttempPeriod());
		network.setConnectionAttemptLimit(config.getHazelcastClientConnectionAttempLimit());
		network.setConnectionTimeout(config.getHazelcastClientConnectionTimeout());
		
		return clientConfig;
	}

}
