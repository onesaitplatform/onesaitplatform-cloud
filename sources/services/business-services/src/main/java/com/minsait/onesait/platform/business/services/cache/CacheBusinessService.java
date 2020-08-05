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

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.minsait.onesait.platform.config.model.Cache;
import com.minsait.onesait.platform.config.model.User;

public interface CacheBusinessService {

    public <K,V> Cache createCache(Cache cache) throws CacheBusinessServiceException;

    public List<String> findCachesWithIdentification(String identification);
    
    public <K,V> void putIntoMap(String identification, K key, V valueObject, User user) throws CacheBusinessServiceException;

    public <K,V> void putAllIntoMap(String identification, Map<K, V> map, User user) throws CacheBusinessServiceException;

    public <K,V> void deleteMap(String identification, User user) throws CacheBusinessServiceException;

    public <K,V> V getFromMap(String identification, User user, K key) throws CacheBusinessServiceException;

    public <K,V> Map<K, V> getAllFromMap(String identification, User user) throws CacheBusinessServiceException;

    public <K,V> Map<K, V> getManyFromMap(String identification, User user, Set<K> keys) throws CacheBusinessServiceException;

    public void updateCache(String identification, Cache editCache);
    
    public void initializeAll();
    
    boolean cacheExists(String identification);

    public List<String> getCachesIdentifications(String userId);

    public Cache getCacheWithId(String id);
    
    public void deleteCacheById(String identification);

	public List<Cache> getByIdentificationLikeOrderByIdentification(String identification);

}
