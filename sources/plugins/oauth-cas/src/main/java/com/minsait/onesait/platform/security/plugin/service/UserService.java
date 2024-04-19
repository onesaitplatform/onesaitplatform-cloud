/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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
package com.minsait.onesait.platform.security.plugin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.minsait.onesait.platform.security.plugin.condition.PluginLoadCondition;
import com.minsait.onesait.platform.security.plugin.mappers.ClaimsExtractor;
import com.minsait.onesait.platform.security.plugin.model.User;

import lombok.extern.slf4j.Slf4j;

@Service
@Conditional(PluginLoadCondition.class)
@Slf4j
public class UserService {

	@Autowired
	@Qualifier("pluginRestTemplate")
	private RestTemplate restTemplate;
	@Autowired
	private ClaimsExtractor claimsExtractor;

	@Value("${controlpanel}")
	private String controlpanel;
	private static final String API_USERS = "/api/users/";
	private static final String API_MASTER_USERS = "/api/multitenant/users/";

	public void createUser(User user) {
		try {
			restTemplate.exchange(controlpanel + API_USERS, HttpMethod.POST, new HttpEntity<>(user), String.class);
		} catch (final HttpClientErrorException e) {
			log.error("Error creating user: {} ", e.getResponseBodyAsString(), e);

		} catch (final Exception e) {
			log.error("Error creating user", e);
		}
	}

	@Cacheable(cacheNames = "userExists", unless = "#result==false")
	public boolean userExists(String userId) {
		try {
			restTemplate.exchange(controlpanel + API_MASTER_USERS + userId + "/exists", HttpMethod.GET, null, String.class);
			return true;
		} catch (final HttpClientErrorException e) {
			if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
				return false;
			} else {
				log.error("Error: ", e);
				throw e;
			}
		}
	}
}
