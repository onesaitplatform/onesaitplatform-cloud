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

import com.google.common.util.concurrent.RateLimiter;

public class GuavaRateLimiter implements ApiRateLimiter {
	private RateLimiter rateLimiter;
	private String id;
	private final int permits;

	public static ApiRateLimiter create(String id, int permits) {
		return new GuavaRateLimiter(id, permits);
	}

	private GuavaRateLimiter(String id, int permits) {
		rateLimiter = RateLimiter.create(permits);
		this.id = id;
		this.permits = permits;
	}

	@Override
	public boolean tryAcquire() {
		return rateLimiter.tryAcquire(permits);
	}

	public RateLimiter getRateLimiter() {
		return rateLimiter;
	}

	public void setRateLimiter(RateLimiter rateLimiter) {
		this.rateLimiter = rateLimiter;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}