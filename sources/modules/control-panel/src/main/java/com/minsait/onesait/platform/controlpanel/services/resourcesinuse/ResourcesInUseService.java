/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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
package com.minsait.onesait.platform.controlpanel.services.resourcesinuse;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ResourcesInUseService {

	@Autowired()
	@Qualifier("resourcesInUseCache")
	private Map<String, VerificationInUse> resourcesInUseCache;

	@Value("${onesaitplatform.resourcesinuse.cache.resourcesInUseTime}")
	private int resourcesInUseTime;

	public static final String RESOURCEINUSE = "resourceinuse";
	public static final String RESOURCEINUSEDASHBOARD = "resourceinuseDashboard";

	@Scheduled(fixedDelay = 600000)
	public void cleanResourcesInUse() {
		final Date now = new Date();
		final Iterator<Entry<String, VerificationInUse>> iterator = resourcesInUseCache.entrySet().iterator();
		while (iterator.hasNext()) {
			final Entry<String, VerificationInUse> next = iterator.next();
			if (next.getValue().getExpirationDate().getTime() < now.getTime())
				resourcesInUseCache.remove(next.getKey());
		}

	}

	public boolean isInUse(String id, String userId) {
		if (resourcesInUseCache.get(id) != null && !resourcesInUseCache.get(id).getUserId().equals(userId)) {
			return true;
		}
		return false;
	}

	public void put(String id, String userId) {
		if (resourcesInUseCache.get(id) != null) {
			if (resourcesInUseCache.get(id).getUserId().equals(userId)) {
				resourcesInUseCache.get(id).setExpirationDate(new Date());
				resourcesInUseCache.get(id).setUserId(userId);
			}
		} else {
			final Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			c.add(Calendar.MINUTE, resourcesInUseTime);
			resourcesInUseCache.put(id,
					VerificationInUse.builder().id(id).userId(userId).expirationDate(c.getTime()).build());
		}
	}

	public void remove(String id) {
		resourcesInUseCache.remove(id);
	}

	public void removeByUser(String id, String userId) {
		if (resourcesInUseCache.get(id) != null) {
			if (resourcesInUseCache.get(id).getUserId().equals(userId)) {
				resourcesInUseCache.remove(id);
			}
		}
	}
}
