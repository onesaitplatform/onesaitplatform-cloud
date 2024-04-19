/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
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
		if (cacheRepository.findCacheByIdentification(identification) != null)
			cacheEx = true;
		return cacheEx;
	}

	@Override
	@Transactional
	public <K, V> Cache createCache(Cache cacheData) throws CacheBusinessServiceException {
		Cache cache = cacheService.createMap(cacheData);
		if (cache != null) {

			if (existCacheObject(cache)) {
				throw new CacheBusinessServiceException(CacheBusinessServiceException.Error.NAME_OF_MAP_ALREADY_USED,
						"Error creating map");
			}

			if (cache.getType() == Type.MAP) {
				return createMap(cache);
			}
			else {
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
		if (identification==null) {
			identification = "";
		}
		return cacheRepository.findAllByIdentificationLikeOrderByIdentificationAsc(identification);
	}


	public List<String> findCachesWithIdentification(String identification) {
		List<Cache> caches;
		List<String> identifications = new ArrayList<>();

		caches = cacheRepository.findAllByOrderByIdentificationAsc();
		for (final Cache cache : caches) {
			identifications.add(cache.getIdentification());
		}
		return identifications;
	}

	public Cache getCacheWithId(String identification) {
		Cache cache;

		identification = identification == null ? "" : identification;
		cache = cacheRepository.findCacheByIdentification(identification);

		return cache;
	}

	public void deleteCacheById(String identification) {
		final Cache cache = cacheRepository.findCacheByIdentification(identification);
		hazelcastInstance.getMap(Tenant2SchemaMapper.getCachePrefix() + identification).destroy();
		cacheRepository.delete(cache);
	}

	private boolean existCacheObject(Cache cache) {
		// check if the map already exists to avoid random access to previously created
		// maps
		Collection<DistributedObject> distributedObjects = hazelcastInstance.getDistributedObjects();
		for (DistributedObject obj : distributedObjects) {
			log.trace("name: {}, serviceName: {}", obj.getName(), obj.getServiceName());
			if (obj.getName().equals(cache.getIdentification()) && obj.getServiceName().equals(HZ_MAP_SERVICE)) {
				return true;
			}
		}

		return false;
	}

	private void createCacheObject(Cache cache) {
		MapConfig mapConfig = createMapConfig(cache);
		hazelcastInstance.getConfig().addMapConfig(mapConfig);
	}

	private MapConfig createMapConfig(Cache cache) {
		MaxSizeConfig maxSizeConfig = new MaxSizeConfig();
		maxSizeConfig.setSize(cache.getSize());
		maxSizeConfig.setMaxSizePolicy(MaxSizeConfig.MaxSizePolicy.valueOf(cache.getMaxSizePolicy().toString()));

		MapConfig mapConfig = new MapConfig(Tenant2SchemaMapper.getCachePrefix() + cache.getIdentification());
		mapConfig.setMaxSizeConfig(maxSizeConfig);
		mapConfig.setEvictionPolicy(EvictionPolicy.valueOf(cache.getEvictionPolicy().toString()));
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
		Cache cacheCnf = cacheService.getCacheConfiguration(identification, user);

		if (cacheCnf != null) {
			hazelcastInstance.<K, V>getMap(Tenant2SchemaMapper.getCachePrefix() + cacheCnf.getIdentification()).put(key, value);
		} else {
			throw new CacheBusinessServiceException(CacheBusinessServiceException.Error.CACHE_DOES_NOT_EXIST,
					MSG_NOT_EXIST_HEADER + identification + MSG_NOT_EXIST_FOOTER);
		}
	}

	@Override
	public <K, V> void putAllIntoMap(String identification, Map<K, V> map, User user)
			throws CacheBusinessServiceException {
		Cache cacheCnf = cacheService.getCacheConfiguration(identification, user);

		if (cacheCnf != null) {
			hazelcastInstance.<K, V>getMap(Tenant2SchemaMapper.getCachePrefix() + cacheCnf.getIdentification()).putAll(map);
		} else {
			throw new CacheBusinessServiceException(CacheBusinessServiceException.Error.CACHE_DOES_NOT_EXIST,
					MSG_NOT_EXIST_HEADER + identification + MSG_NOT_EXIST_FOOTER);
		}

	}

	@Override
	public <K, V> V getFromMap(String identification, User user, K key) throws CacheBusinessServiceException {
		Cache cacheCnf = cacheService.getCacheConfiguration(identification, user);

		if (cacheCnf != null) {
			return hazelcastInstance.<K, V>getMap(Tenant2SchemaMapper.getCachePrefix() + cacheCnf.getIdentification()).get(key);
		} else {
			throw new CacheBusinessServiceException(CacheBusinessServiceException.Error.CACHE_DOES_NOT_EXIST,
					MSG_NOT_EXIST_HEADER + identification + MSG_NOT_EXIST_FOOTER);
		}
	}

	@Override
	public <K, V> Map<K, V> getAllFromMap(String identification, User user) throws CacheBusinessServiceException {
		Cache cacheCnf = cacheService.getCacheConfiguration(identification, user);

		if (cacheCnf != null) {
			IMap<K, V> map = hazelcastInstance.<K, V>getMap(Tenant2SchemaMapper.getCachePrefix() + identification);
			return map.getAll(map.keySet());
		} else {
			throw new CacheBusinessServiceException(CacheBusinessServiceException.Error.CACHE_DOES_NOT_EXIST,
					MSG_NOT_EXIST_HEADER + identification + MSG_NOT_EXIST_FOOTER);
		}
	}

	@Override
	public <K, V> Map<K, V> getManyFromMap(String identification, User user, Set<K> keys)
			throws CacheBusinessServiceException {
		Cache cacheCnf = cacheService.getCacheConfiguration(identification, user);

		if (cacheCnf != null) {
			IMap<K, V> map = hazelcastInstance.<K, V>getMap(Tenant2SchemaMapper.getCachePrefix() + identification);
			return map.getAll(keys);
		} else {
			throw new CacheBusinessServiceException(CacheBusinessServiceException.Error.CACHE_DOES_NOT_EXIST,
					MSG_NOT_EXIST_HEADER + identification + MSG_NOT_EXIST_FOOTER);
		}
	}

	@Override
	public void updateCache(String identification, Cache editCache) {
		final Cache cache = cacheRepository.findCacheByIdentification(identification);
		if (cache != null) {
			cache.setType(editCache.getType());
			cache.setEvictionPolicy(editCache.getEvictionPolicy());
			cache.setMaxSizePolicy(editCache.getMaxSizePolicy());
			cache.setSize(editCache.getSize());
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
		List<Cache> caches = cacheService.findAll();
		for (Cache cache : caches) {
			createCacheObject(cache);
		}
	}

}
