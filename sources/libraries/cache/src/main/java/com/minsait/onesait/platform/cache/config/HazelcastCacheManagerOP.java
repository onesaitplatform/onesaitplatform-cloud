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
package com.minsait.onesait.platform.cache.config;

import org.springframework.cache.Cache;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;

public class HazelcastCacheManagerOP extends HazelcastCacheManager {

	private static final String SESSIONS_REPOSITORY = "IoTSessionRepository";

	public HazelcastCacheManagerOP(HazelcastInstance hazelcastInstance) {
		super(hazelcastInstance);
	}

	@Override
	public Cache getCache(String name) {
		final String prefixed = Tenant2SchemaMapper.getCachePrefix();
		if (isGlobalCache(name) || name.startsWith(prefixed)) {
			return super.getCache(name);
		} else {
			return super.getCache(prefixed + name);
		}

	}

	private boolean isGlobalCache(String name) {
		return SESSIONS_REPOSITORY.equals(name);
	}
}
