package com.minsait.onesait.platform.tools.datagrid.cache.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;

@Service
public class CacheManagerService {

	@Autowired
	private HazelcastInstance hzInstance;
	
	
	public void createMap(String mapName) {
		MapConfig mapConfig=new MapConfig();
		mapConfig.setName(mapName);
		//TODO En caso necesario se pueden añadir configuraciones concretas si se necesitan
//		mapConfig.setTimeToLiveSeconds(100);
//		mapConfig.setEvictionPolicy(EvictionPolicy.LRU);
//		mapConfig.setMaxSizeConfig(maxSizeConfig)
		
		hzInstance.getConfig().addMapConfig(mapConfig);
	}
	
	
}
