/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2022 SPAIN
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
package com.minsait.onesait.platform.business.services.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.InvalidConfigurationException;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.minsait.onesait.platform.config.model.Cache;
import com.minsait.onesait.platform.config.model.Cache.Type;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.CacheRepository;
import com.minsait.onesait.platform.config.services.cache.CacheService;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CacheBusinessServiceImpl implements CacheBusinessService {
	
	@Autowired
	private CacheBusinessService cacheBS;
	
	@Autowired
	private CacheService cacheService;

	@Autowired
	private HazelcastInstance hazelcastInstance;

	@Autowired
	private CacheRepository cacheRepository;

	private static final String HZ_MAP_SERVICE = "hz:impl:mapService";
	private static final String MSG_NOT_EXIST_HEADER = "Cache with identification ";
	private static final String MSG_NOT_EXIST_FOOTER = " does not exist or user does not have authorization";

	@Override
	public boolean cacheExists(String identification) {
		boolean cacheEx = false;
		if (cacheRepository.findCacheByIdentification(identification) != null) {
			cacheEx = true;
		}
		return cacheEx;
	}

	@Override
	@Transactional
	public <K, V> Cache createCache(Cache cacheData) throws CacheBusinessServiceException {
		final Cache cache = cacheService.createMap(cacheData);
		if (cache != null) {

			if (existCacheObject(cache)) {
				throw new CacheBusinessServiceException(CacheBusinessServiceException.Error.NAME_OF_MAP_ALREADY_USED,
						"Error creating map");
			}

			if (cache.getType() == Type.MAP) {
				return createMap(cache);
			} else {
				throw new CacheBusinessServiceException(CacheBusinessServiceException.Error.UNSUPPORTED_TYPE,
						"The cache type is not supported");
			}

		} else {
			throw new CacheBusinessServiceException(CacheBusinessServiceException.Error.CACHE_WAS_NOT_CREATED,
					"Error creating map");
		}
	}

	private <K, V> Cache createMap(Cache cache) {
		createCacheObject(cache);

		hazelcastInstance.<K, V>getMap(Tenant2SchemaMapper.getCachePrefix() + cache.getIdentification());

		return cache;
	}

	@Override
	public List<Cache> getByIdentificationLikeOrderByIdentification(String identification) {
		if (identification == null) {
			identification = "";
		}
		return cacheRepository.findAllByIdentificationLikeOrderByIdentificationAsc(identification);
	}

	@Override
	public List<String> findCachesWithIdentification(String identification) {
		List<Cache> caches;
		final List<String> identifications = new ArrayList<>();

		caches = cacheRepository.findAllByOrderByIdentificationAsc();
		for (final Cache cache : caches) {
			identifications.add(cache.getIdentification());
		}
		return identifications;
	}

	@Override
	public Cache getCacheWithId(String identification) {
		Cache cache;

		identification = identification == null ? "" : identification;
		cache = cacheRepository.findCacheByIdentification(identification);

		return cache;
	}

	@Override
	public void deleteCacheById(String identification) {
		final Cache cache = cacheRepository.findCacheByIdentification(identification);
		hazelcastInstance.getMap(Tenant2SchemaMapper.getCachePrefix() + identification).destroy();
		cacheRepository.delete(cache);
	}

	private boolean existCacheObject(Cache cache) {
		// check if the map already exists to avoid random access to previously created
		// maps
		final Collection<DistributedObject> distributedObjects = hazelcastInstance.getDistributedObjects();
		for (final DistributedObject obj : distributedObjects) {
			log.trace("name: {}, serviceName: {}", obj.getName(), obj.getServiceName());
			if (obj.getName().equals(cache.getIdentification()) && obj.getServiceName().equals(HZ_MAP_SERVICE)) {
				return true;
			}
		}

		return false;
	}

	private void createCacheObject(Cache cache) {
		final MapConfig mapConfig = createMapConfig(cache);
		try {
			hazelcastInstance.getConfig().addMapConfig(mapConfig);
		} catch (final InvalidConfigurationException e) {
			log.warn("Could not add dynamic cache {}, conflicts with existing one", cache.getIdentification());
		}
	}

	private MapConfig createMapConfig(Cache cache) {
		final EvictionConfig evictionConfig = new EvictionConfig();
		evictionConfig.setSize(cache.getSize());
		evictionConfig
		.setMaxSizePolicy(EvictionConfig.DEFAULT_MAX_SIZE_POLICY.valueOf(cache.getMaxSizePolicy().toString()));
		evictionConfig.setEvictionPolicy(EvictionPolicy.valueOf(cache.getEvictionPolicy().toString()));

		final MapConfig mapConfig = new MapConfig(Tenant2SchemaMapper.getCachePrefix() + cache.getIdentification());
		mapConfig.setEvictionConfig(evictionConfig);
		return mapConfig;
	}

	@Override
	@Transactional
	public <K, V> void deleteMap(String identification, User user) throws CacheBusinessServiceException {
		cacheService.deleteByIdentificationAndUser(identification, user);
		hazelcastInstance.<K, V>getMap(Tenant2SchemaMapper.getCachePrefix() + identification).destroy();
	}

	@Override
	public <K, V> void putIntoMap(String identification, K key, V value, User user)
			throws CacheBusinessServiceException {
		final Cache cacheCnf = cacheService.getCacheConfiguration(identification, user);
		 Map<String, String> values = cacheBS.getAllFromMap(identification, user);
		 
		if (cacheCnf != null) {
			
			if((cacheCnf.getSize() > values.size()) || values.containsKey(key)) {
				hazelcastInstance.<K, V>getMap(Tenant2SchemaMapper.getCachePrefix() + cacheCnf.getIdentification()).put(key,
				value);	
			} else {
				
				List<String> keysList = new ArrayList<>(values.keySet());
				String searchKey = key.toString();
				switch (cacheCnf.getEvictionPolicy().name()) {
					case "LRU": {
						searchKey = keysList.get(0);
						  break;
					}
					case "LFU": {
						searchKey = keysList.get(keysList.size() - 1);
						  break;
					}
					case "RANDOM": {
						Random random = new Random();
				        int randomIndex = random.nextInt(keysList.size());
				        searchKey = keysList.get(randomIndex);
				        break;
					}
				}
				
				hazelcastInstance.<K, V>getMap(Tenant2SchemaMapper.getCachePrefix() + cacheCnf.getIdentification()).remove(searchKey);
				hazelcastInstance.<K, V>getMap(Tenant2SchemaMapper.getCachePrefix() + cacheCnf.getIdentification()).put(key,
						value);
				
			}
			
		} else {
			throw new CacheBusinessServiceException(CacheBusinessServiceException.Error.CACHE_DOES_NOT_EXIST,
					MSG_NOT_EXIST_HEADER + identification + MSG_NOT_EXIST_FOOTER);
		}
	}

	@Override
	public <K, V> void putAllIntoMap(String identification, Map<K, V> map, User user)
			throws CacheBusinessServiceException {
		
		 final Cache cacheCnf = cacheService.getCacheConfiguration(identification, user);
		 Map<String, String> values = cacheBS.getAllFromMap(identification, user);
		 
		
		 Map<String, String> totalValues = new HashMap<>();
		 totalValues.putAll(values);
		 totalValues.putAll((Map<String, String>) map);
		 
	     Integer totalNoRep = totalValues.size();
	     Integer additionalCacheNeeded =  totalNoRep - cacheCnf.getSize();

	     List<String>  keysList = new ArrayList<>(totalValues.keySet());
		 String searchKey = "";
		 
		 HashSet keysDistinct = new HashSet<>(map.keySet());
         keysDistinct.addAll(values.keySet());
         keysDistinct.removeAll(map.keySet());

		 
	     if(additionalCacheNeeded > 0){
	    	 
	    	 additionalCacheNeeded = Math.abs(additionalCacheNeeded - keysDistinct.size());
	    
	    	 if(keysDistinct.size() != 0) {
	    		 Iterator iterator = keysDistinct.iterator();
	    		 for (int i = 0; i <= additionalCacheNeeded; i++) {
		    		 Object key = iterator.next();
		    		 hazelcastInstance.<K, V>getMap(Tenant2SchemaMapper.getCachePrefix() + cacheCnf.getIdentification()).remove(key);
		    		 totalValues.remove(key);
	    		 }
	    		 hazelcastInstance.<K, V>getMap(Tenant2SchemaMapper.getCachePrefix() + cacheCnf.getIdentification()).putAll((Map<K, V>)totalValues);
	    	 }
	    	 
	    	 
			 if(totalValues.size() > cacheCnf.getSize()) {
				 Integer cacheEvictionNumber = totalValues.size() - cacheCnf.getSize();
				 switch (cacheCnf.getEvictionPolicy().name()) {
					case "LRU":{
						for(int i = 0; i < cacheEvictionNumber; i++ ) {
							searchKey = keysList.get(i);
							totalValues.remove(searchKey);
							if( values.containsKey(searchKey)) {
								 hazelcastInstance.<K, V>getMap(Tenant2SchemaMapper.getCachePrefix() + cacheCnf.getIdentification()).remove(searchKey);
							 }
						}
						break;
					}
					case "LFU": {
						for(int i = 0; i < cacheEvictionNumber; i++ ) {
							 searchKey = keysList.get(keysList.size() - 1);
							 totalValues.remove(searchKey);
							 if(values.containsKey(searchKey)) {
								 hazelcastInstance.<K, V>getMap(Tenant2SchemaMapper.getCachePrefix() + cacheCnf.getIdentification()).remove(searchKey);
							 }
						}
						break;
					}
					case "RANDOM": {
						for(int i = 0; i < cacheEvictionNumber; i++ ) {
							
							Random random = new Random();
					        int randomIndex = random.nextInt(keysList.size());
					        searchKey = keysList.get(randomIndex);
					        totalValues.remove(searchKey);
					        if( values.containsKey(searchKey)) {
					        	hazelcastInstance.<K, V>getMap(Tenant2SchemaMapper.getCachePrefix() + cacheCnf.getIdentification()).remove(searchKey);
							 }
						}
						break;
					}
				}
				hazelcastInstance.<K, V>getMap(Tenant2SchemaMapper.getCachePrefix() + cacheCnf.getIdentification()).putAll((Map<K, V>)totalValues);				 
			 }
			
		}else{	
		 hazelcastInstance.<K, V>getMap(Tenant2SchemaMapper.getCachePrefix() + cacheCnf.getIdentification())
		 .putAll(map);
		}				
	}

	@Override
	public <K, V> V getFromMap(String identification, User user, K key) throws CacheBusinessServiceException {
		final Cache cacheCnf = cacheService.getCacheConfiguration(identification, user);

		if (cacheCnf != null) {
			return hazelcastInstance.<K, V>getMap(Tenant2SchemaMapper.getCachePrefix() + cacheCnf.getIdentification())
					.get(key);
		} else {
			throw new CacheBusinessServiceException(CacheBusinessServiceException.Error.CACHE_DOES_NOT_EXIST,
					MSG_NOT_EXIST_HEADER + identification + MSG_NOT_EXIST_FOOTER);
		}
	}

	@Override
	public <K, V> Map<K, V> getAllFromMap(String identification, User user) throws CacheBusinessServiceException {
		final Cache cacheCnf = cacheService.getCacheConfiguration(identification, user);

		if (cacheCnf != null) {
			final IMap<K, V> map = hazelcastInstance.<K, V>getMap(Tenant2SchemaMapper.getCachePrefix() + identification);
			return map.getAll(map.keySet());
		} else {
			throw new CacheBusinessServiceException(CacheBusinessServiceException.Error.CACHE_DOES_NOT_EXIST,
					MSG_NOT_EXIST_HEADER + identification + MSG_NOT_EXIST_FOOTER);
		}
	}

	@Override
	public <K, V> Map<K, V> getManyFromMap(String identification, User user, Set<K> keys)
			throws CacheBusinessServiceException {
		final Cache cacheCnf = cacheService.getCacheConfiguration(identification, user);

		if (cacheCnf != null) {
			final IMap<K, V> map = hazelcastInstance.<K, V>getMap(Tenant2SchemaMapper.getCachePrefix() + identification);
			return map.getAll(keys);
		} else {
			throw new CacheBusinessServiceException(CacheBusinessServiceException.Error.CACHE_DOES_NOT_EXIST,
					MSG_NOT_EXIST_HEADER + identification + MSG_NOT_EXIST_FOOTER);
		}
	}

	@Override
	public void updateCache(String identification, Cache editCache, User user) throws CacheBusinessServiceException {
		final Cache cache = cacheRepository.findCacheByIdentification(identification);
		 Map<String, String> values = cacheBS.getAllFromMap(identification, user);
		 List<String> keysList = new ArrayList<>(values.keySet());
		 String searchKey = "";
		 
		 if (cache != null) {
			cache.setType(editCache.getType());
			cache.setEvictionPolicy(editCache.getEvictionPolicy());
			cache.setMaxSizePolicy(editCache.getMaxSizePolicy());
			
			if(cache.getSize() <= editCache.getSize() || editCache.getSize() >= values.size()) {
				cache.setSize(editCache.getSize());
				
			}else {
			
			Integer	totalDeleteCache = values.size() - editCache.getSize();
				switch (cache.getEvictionPolicy().name()) {
				case "LRU": {
					for(int i = 0; i < totalDeleteCache; i++ ) {
						searchKey = keysList.get(i);
						hazelcastInstance.getMap(Tenant2SchemaMapper.getCachePrefix() + identification).remove(searchKey);
					}
					break;
				}
				case "LFU": {
					for(int i = 0; i < totalDeleteCache; i++ ) {
						searchKey = keysList.get(keysList.size() - 1);
						keysList.remove(searchKey);
						hazelcastInstance.getMap(Tenant2SchemaMapper.getCachePrefix() + identification).remove(searchKey);
					}
					break;
				}
				case "RANDOM": {
					for(int i = 0; i < totalDeleteCache; i++ ) {
						Random random = new Random();
				        int randomIndex = random.nextInt(keysList.size());
				        searchKey = keysList.get(randomIndex);
				        keysList.remove(searchKey);
				        hazelcastInstance.getMap(Tenant2SchemaMapper.getCachePrefix() + identification).remove(searchKey);
					}
			        break;
				}
			}
			
				cache.setSize(editCache.getSize());
			}
			
			cacheRepository.save(cache);
		}
	}

	@Override
	public List<String> getCachesIdentifications(String userId) {
		List<Cache> caches;
		final List<String> identifications = new ArrayList<>();

		caches = cacheRepository.findAllByOrderByIdentificationAsc();

		for (final Cache cache : caches) {
			identifications.add(cache.getIdentification());
		}

		return identifications;
	}

	@Override
	public void initializeAll() {
		final List<Cache> caches = cacheService.findAll();
		for (final Cache cache : caches) {
			createCacheObject(cache);
		}
	}

}
