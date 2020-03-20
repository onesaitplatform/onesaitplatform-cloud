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
package com.minsait.onesait.platform.libraries.flow.engine;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.minsait.onesait.platform.commons.flow.engine.dto.FlowEngineDomain;
import com.minsait.onesait.platform.commons.flow.engine.dto.FlowEngineDomainStatus;
import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.libraries.flow.engine.exception.FlowEngineServiceException;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Value

@Slf4j
public class FlowEngineServiceImpl implements FlowEngineService {

	private HttpComponentsClientHttpRequestFactory httpRequestFactory;
	private String restBaseUrl;

	public FlowEngineServiceImpl(String restBaseUrl, int restRequestTimeout, boolean avoidSSLVerification) {

		if (avoidSSLVerification) {
			httpRequestFactory = SSLUtil.getHttpRequestFactoryAvoidingSSLVerification();
		} else {
			httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		}
		httpRequestFactory.setConnectionRequestTimeout(restRequestTimeout);

		this.restBaseUrl = restBaseUrl;
	}

	public FlowEngineServiceImpl(String restBaseUrl, int restRequestTimeout) {
		this(restBaseUrl, restRequestTimeout, false);
	}

	@Override
	public void stopFlowEngineDomain(String domainId) {
		RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
		try {
			restTemplate.put(restBaseUrl + "/domain/stop/" + domainId, null);
		} catch (Exception e) {
			log.error("Unable to stop domain " + domainId);
			throw new FlowEngineServiceException("Unable to stop domain " + domainId, e);
		}

	}

	@Override
	public void startFlowEngineDomain(FlowEngineDomain domain) {
		RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<FlowEngineDomain> domainToStart = new HttpEntity<>(domain, headers);
			restTemplate.postForObject(restBaseUrl + "/domain/start/", domainToStart, String.class);
		} catch (Exception e) {
			log.error("Unable to start domain " + domain.getDomain());
			throw new FlowEngineServiceException("Unable to start domain " + domain.getDomain(), e);
		}
	}

	@Override
	public void createFlowengineDomain(FlowEngineDomain domain) {
		RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<FlowEngineDomain> newDomain = new HttpEntity<>(domain, headers);
			restTemplate.postForObject(restBaseUrl + "/domain", newDomain, String.class);
		} catch (Exception e) {
			log.error("Unable to create domain " + domain.getDomain());
			throw new FlowEngineServiceException("Unable to create domain " + domain.getDomain(), e);
		}
	}

	@Override
	public void deleteFlowEngineDomain(String domainId) {
		RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
		try {
			restTemplate.delete(restBaseUrl + "/domain/" + domainId);
		} catch (Exception e) {
			log.error("Unable to delete domain " + domainId);
			throw new FlowEngineServiceException("Unable to delete domain " + domainId, e);
		}
	}

	@Override
	public FlowEngineDomain getFlowEngineDomain(String domainId) {
		RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
		FlowEngineDomain domain = null;
		try {
			domain = restTemplate.getForObject(restBaseUrl + "/domain/" + domainId, FlowEngineDomain.class);
		} catch (Exception e) {
			log.error("Unable to retrieve domain " + domainId);
			throw new FlowEngineServiceException("Unable to retrieve domain " + domainId, e);
		}
		return domain;
	}

	@Override
	public List<FlowEngineDomainStatus> getAllFlowEnginesDomains() {
		RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
		List<FlowEngineDomainStatus> response = new ArrayList<>();
		try {
			ResponseEntity<String> responseHttp = restTemplate.getForEntity(restBaseUrl + "/domain/all", String.class);
			response = (List<FlowEngineDomainStatus>) FlowEngineDomainStatus
					.fromJsonArrayToDomainStatus(responseHttp.getBody());
		} catch (Exception e) {
			log.error("Unable to retrieve all domains.");
			throw new FlowEngineServiceException("Unable to retrieve all domains.", e);
		}
		return response;
	}

	@Override
	public List<FlowEngineDomainStatus> getFlowEngineDomainStatus(List<String> domainList) {
		RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
		List<FlowEngineDomainStatus> response = new ArrayList<>();
		try {
			StringBuffer data = new StringBuffer();
			for (String domId : domainList) {
				data.append(domId).append(",");
			}
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(restBaseUrl + "/domain/status")
					.queryParam("domainList", data.toString().substring(0, data.toString().length() - 1));

			ResponseEntity<String> responseHttp = restTemplate.getForEntity(builder.toUriString(), String.class);
			response = (List<FlowEngineDomainStatus>) FlowEngineDomainStatus
					.fromJsonArrayToDomainStatus(responseHttp.getBody());
		} catch (Exception e) {
			log.error("Unable to retrieve domains' status. " + domainList.toString());
			throw new FlowEngineServiceException("Unable to retrieve domains' status.", e);
		}
		return response;
	}
	
	@Override
	public void deployFlowengineDomain(String domain, String data) {
		RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
		try {

			String domainRecord = "{'domain':'"+domain+"'}";
			JSONArray jsonArray = new JSONArray(data);
			JSONArray fullDeployment = new JSONArray();
			fullDeployment.put(new JSONObject(domainRecord));
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject o = jsonArray.getJSONObject(i);
				fullDeployment.put(o);
			}
			data = fullDeployment.toString();

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> newDomain = new HttpEntity<>(data, headers);
			restTemplate.postForObject(restBaseUrl + "/deploy", newDomain, String.class);
		} catch (Exception e) {
			log.error("Unable to deploy domain " + domain);
			throw new FlowEngineServiceException("Unable to deploy domain " + domain, e);
		}
	}

}
