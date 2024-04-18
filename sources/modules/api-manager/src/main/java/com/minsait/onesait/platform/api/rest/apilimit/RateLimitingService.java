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
package com.minsait.onesait.platform.api.rest.apilimit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class RateLimitingService {

	private final Map<String, ApiRateLimiter> limiters = new ConcurrentHashMap<>();

	private static final int PERMITS = 5;

	private static final Object lock1 = new Object();

	public boolean processRateLimit(String clientId, int permits) {
		if (clientId == null) {
			return true;
		}
		final ApiRateLimiter limiter = getRateLimiter(clientId, permits);
		return (limiter.tryAcquire());
	}

	private ApiRateLimiter getRateLimiter(String clientId, int permits) {
		if (limiters.containsKey(clientId)) {
			return limiters.get(clientId);
		} else {
			synchronized (lock1) {
				if (limiters.containsKey(clientId)) {
					return limiters.get(clientId);
				}

				final ApiRateLimiter rateLimiter = createRateLimiter(clientId, permits);
				limiters.put(clientId, rateLimiter);
				return rateLimiter;
			}
		}
	}

	private ApiRateLimiter createRateLimiter(String clientId, int attempts) {
		if (attempts >= 0) {
			return GuavaRateLimiter.create(clientId, attempts);
		} else {
			return GuavaRateLimiter.create(clientId, PERMITS);
		}
	}

}