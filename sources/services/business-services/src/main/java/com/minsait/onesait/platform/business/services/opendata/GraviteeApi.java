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
package com.minsait.onesait.platform.business.services.opendata;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

@Component
public class GraviteeApi {

	@Autowired
	private IntegrationResourcesService integrationResourcesService;

	private String graviteeUrl;
	private static final String APIS_ENDPOINT = "/apis";
	private static final String PAGES_ENDPOINT = "/pages";
	private static final String CONTENT_ENDPOINT = "/content";

	@PostConstruct
	public void graviteeSetUp() {
		graviteeUrl = integrationResourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT);
	}

	public String executePagesApi(String graviteeId) {
		final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

		final HttpHeaders headers = new HttpHeaders();
		final HttpEntity entity = new HttpEntity(headers);
		List<Map<String, Object>> result = restTemplate
				.exchange(graviteeUrl + APIS_ENDPOINT + "/" + graviteeId + PAGES_ENDPOINT, HttpMethod.GET, entity,
						List.class)
				.getBody();
		if (result.isEmpty()) {
			return "";
		}

		return (String) result.get(0).get("id");
	}

	public String getSwaggerGravitee(String graviteeId, String pageId) {
		return graviteeUrl + APIS_ENDPOINT + "/" + graviteeId + PAGES_ENDPOINT + "/" + pageId + CONTENT_ENDPOINT;
	}

	public boolean isGraviteeApi(String resourceUrl) {
		return resourceUrl.contains(graviteeUrl + APIS_ENDPOINT);
	}

	public String getGraviteeIdFromUrl(String url) {
		return url.substring(url.indexOf(APIS_ENDPOINT + "/") + 6, url.indexOf(PAGES_ENDPOINT));
	}

}
