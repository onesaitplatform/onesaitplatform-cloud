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
package com.minsait.onesait.platform.libraries.flow.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.minsait.onesait.platform.commons.flow.engine.dto.FlowEngineDomain;
import com.minsait.onesait.platform.commons.flow.engine.dto.FlowEngineDomainStatus;
import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.config.model.security.UserPrincipal;
import com.minsait.onesait.platform.libraries.flow.engine.exception.FlowEngineServiceException;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FlowEngineServiceImpl implements FlowEngineService {

	private HttpComponentsClientHttpRequestFactory httpRequestFactory;
	private RestTemplate restTemplate;
	
	@Autowired
	private IntegrationResourcesService resourcesService;
	
	@Value("${onesaitplatform.flowengine.services.request.timeout.ms:5000}")
	private int restRequestTimeout;

	@Value("${onesaitplatform.controlpanel.avoidsslverification:false}")
	private boolean avoidSSLVerification;
	
	@PostConstruct
	public void init() {
		
		if (avoidSSLVerification) {
			httpRequestFactory = SSLUtil.getHttpRequestFactoryAvoidingSSLVerification();
		} else {
			httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		}
		httpRequestFactory.setConnectionRequestTimeout(restRequestTimeout);
		restTemplate = new RestTemplate(httpRequestFactory);

		restTemplate.getInterceptors().add((r, b, e) -> {
			Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication()).ifPresent(a -> {
				r.getHeaders().add(Tenant2SchemaMapper.VERTICAL_HTTP_HEADER,
						((UserPrincipal) a.getPrincipal()).getVertical());
				r.getHeaders().add(Tenant2SchemaMapper.TENANT_HTTP_HEADER,
						((UserPrincipal) a.getPrincipal()).getTenant());
			});
			return e.execute(r, b);
		});
	}
	
	@Override
	public void stopFlowEngineDomain(String domainId) {

		try {
			restTemplate.put(getRestBaseUrl() + "/domain/stop/" + domainId, null);
		} catch (final Exception e) {
			log.error("Unable to stop domain " + domainId);
			throw new FlowEngineServiceException("Unable to stop domain " + domainId, e);
		}

	}

	@Override
	public void startFlowEngineDomain(FlowEngineDomain domain) {

		try {
			final HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			final HttpEntity<FlowEngineDomain> domainToStart = new HttpEntity<>(domain, headers);
			restTemplate.postForObject(getRestBaseUrl() + "/domain/start/", domainToStart, String.class);
		} catch (final Exception e) {
			log.error("Unable to start domain " + domain.getDomain());
			throw new FlowEngineServiceException("Unable to start domain " + domain.getDomain(), e);
		}
	}

	@Override
	public void createFlowengineDomain(FlowEngineDomain domain) {

		try {
			final HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			final HttpEntity<FlowEngineDomain> newDomain = new HttpEntity<>(domain, headers);
			restTemplate.postForObject(getRestBaseUrl() + "/domain", newDomain, String.class);
		} catch (final Exception e) {
			log.error("Unable to create domain " + domain.getDomain());
			throw new FlowEngineServiceException("Unable to create domain " + domain.getDomain(), e);
		}
	}

	@Override
	public void deleteFlowEngineDomain(String domainId) {

		try {
			restTemplate.delete(getRestBaseUrl() + "/domain/" + domainId);
		} catch (final Exception e) {
			log.error("Unable to delete domain " + domainId);
			throw new FlowEngineServiceException("Unable to delete domain " + domainId, e);
		}
	}

	@Override
	public FlowEngineDomain getFlowEngineDomain(String domainId) {

		FlowEngineDomain domain = null;
		try {
			domain = restTemplate.getForObject(getRestBaseUrl() + "/domain/" + domainId, FlowEngineDomain.class);
		} catch (final Exception e) {
			log.error("Unable to retrieve domain " + domainId);
			throw new FlowEngineServiceException("Unable to retrieve domain " + domainId, e);
		}
		return domain;
	}

	@Override
	public List<FlowEngineDomainStatus> getAllFlowEnginesDomains() {

		List<FlowEngineDomainStatus> response = new ArrayList<>();
		try {
			final ResponseEntity<String> responseHttp = restTemplate.getForEntity(getRestBaseUrl() + "/domain/all",
					String.class);
			response = (List<FlowEngineDomainStatus>) FlowEngineDomainStatus
					.fromJsonArrayToDomainStatus(responseHttp.getBody());
		} catch (final Exception e) {
			log.error("Unable to retrieve all domains.");
			throw new FlowEngineServiceException("Unable to retrieve all domains.", e);
		}
		return response;
	}

	@Override
	public List<FlowEngineDomainStatus> getFlowEngineDomainStatus(List<String> domainList) {

		List<FlowEngineDomainStatus> response = new ArrayList<>();
		try {
			final StringBuilder data = new StringBuilder();
			for (final String domId : domainList) {

				data.append(domId).append(",");
			}
			final UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getRestBaseUrl() + "/domain/status")
					.queryParam("domainList", data.toString().substring(0, data.toString().length() - 1));

			final ResponseEntity<String> responseHttp = restTemplate.getForEntity(builder.toUriString(), String.class);
			response = (List<FlowEngineDomainStatus>) FlowEngineDomainStatus
					.fromJsonArrayToDomainStatus(responseHttp.getBody());
		} catch (final Exception e) {
			log.error("Unable to retrieve domains' status. " + domainList.toString());
			throw new FlowEngineServiceException("Unable to retrieve domains' status.", e);
		}
		return response;
	}

	@Override
	public void deployFlowengineDomain(String domain, String data) {
		final RestTemplate restTemplate = new RestTemplate(httpRequestFactory);

		try {

			final String domainRecord = "{'domain':'" + domain + "'}";
			final JSONArray jsonArray = new JSONArray(data);
			final JSONArray fullDeployment = new JSONArray();
			fullDeployment.put(new JSONObject(domainRecord));
			for (int i = 0; i < jsonArray.length(); i++) {
				final JSONObject o = jsonArray.getJSONObject(i);
				fullDeployment.put(o);
			}
			data = fullDeployment.toString();

			final HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			final HttpEntity<String> newDomain = new HttpEntity<>(data, headers);
			restTemplate.postForObject(getRestBaseUrl() + "/deploy", newDomain, String.class);
		} catch (final Exception e) {
			log.error("Unable to deploy domain " + domain);
			throw new FlowEngineServiceException("Unable to deploy domain " + domain, e);
		}
	}

	@Override
	public String exportDomainFromFS(String domain) {
		final RestTemplate restTemplate = new RestTemplate(httpRequestFactory);

		try {
			return restTemplate.getForObject(getRestBaseUrl() + "/exportDomainFromFS/" + domain, String.class);
		} catch (final Exception e) {
			log.error("Unable to retrieve export json for domain " + domain);
			throw new FlowEngineServiceException("Unable to retrieve export json form domain " + domain, e);
		}
	}
	
	private String getRestBaseUrl() {
		return resourcesService.getUrl(Module.FLOWENGINE, ServiceUrl.BASE);
	}

}
