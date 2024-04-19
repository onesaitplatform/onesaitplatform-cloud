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
package com.minsait.onesait.platform.config.services.cache;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Cache;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.repository.CacheRepository;
import com.minsait.onesait.platform.config.repository.OPResourceRepository;

@Service
public class CacheServiceImpl implements CacheService {

    @Autowired
    private CacheRepository cacheRepository;
    
    @Autowired 
    private OPResourceRepository oprRepository;
    
    @Override
    public Cache createMap(Cache cache) {
        return cacheRepository.save(cache);
    }

    @Override
    public Cache getCacheConfiguration(String identification, User user) {
        List<OPResource> cacheConfigs = 
                oprRepository.findByIdentificationContainingIgnoreCaseAndUser(identification, user);
        
        return cacheConfigs != null && cacheConfigs.size() == 1 ? (Cache) cacheConfigs.get(0) : null;
    }

    @Override
    public void deleteByIdentificationAndUser(String identification, User user) {
        cacheRepository.deleteByIdentificationAndUser(identification, user);
    }

    @Override
    public List<Cache> findAll() {
        return cacheRepository.findAll();
    }
}
