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
package com.minsait.onesait.platform.config.services.nginxmanage;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class NginxManageServiceImpl implements NginxManageService {

	@Value("${dynamic-load-balancer.url:http://localhost:8000/nginx}")
	private String nginxServiceUrl;

	private RestTemplate restTemplate;

	@PostConstruct
	public void init() {
		this.restTemplate = new RestTemplate();

	}

	@Override
	public String getNginx() {
		return (this.restTemplate.getForObject(nginxServiceUrl, String.class));
	}

	@Override
	public String setNginx(String nginx) {
		try {
			HttpEntity<String> entity = new HttpEntity<>(nginx);
			this.restTemplate.exchange(nginxServiceUrl + "/test", HttpMethod.POST, entity, String.class);
			this.restTemplate.exchange(nginxServiceUrl + "/set", HttpMethod.POST, entity, String.class);
			return (this.restTemplate.getForObject(nginxServiceUrl, String.class));

		} catch (HttpServerErrorException e) {
			return (new String(e.getResponseBodyAsByteArray())
					+ "\n\nPress 'Previous Version' if you want undo the changes");
		}
	}

	@Override
	public String testNginx(String nginx) {
		try {
			HttpEntity<String> entity = new HttpEntity<>(nginx);
			this.restTemplate.exchange(nginxServiceUrl + "/test", HttpMethod.POST, entity, String.class);

			return (this.restTemplate.getForObject(nginxServiceUrl, String.class));
		} catch (Exception e) {
			return ("Error. Reset para volver a la versión anterior");
		}
	}

	@Override
	public String undoNginx() {
		this.restTemplate.put(nginxServiceUrl + "/undo", null);
		return (this.restTemplate.getForObject(nginxServiceUrl, String.class));
	}

	@Override
	public String resetNginx() {
		this.restTemplate.put(nginxServiceUrl + "/reset", null);
		return (this.restTemplate.getForObject(nginxServiceUrl, String.class));
	}

}
