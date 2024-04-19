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
package com.minsait.onesait.platform.flowengine.nodered.communication;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.commons.flow.engine.dto.FlowEngineDomain;
import com.minsait.onesait.platform.commons.flow.engine.dto.FlowEngineDomainStatus;
import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.services.flowdomain.FlowDomainService;
import com.minsait.onesait.platform.flowengine.audit.aop.FlowEngineAuditable;
import com.minsait.onesait.platform.flowengine.exception.NodeRedAdminServiceException;
import com.minsait.onesait.platform.flowengine.exception.NotSynchronizedToCdbException;
import com.minsait.onesait.platform.flowengine.nodered.communication.dto.SynchronizeDomainStatusRequest;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NodeRedAdminClientImpl implements NodeRedAdminClient {
	@Value("${onesaitplatform.flowengine.admin.url}")
	private String flowengineUrl;
	// Services
	@Value("${onesaitplatform.flowengine.services.request.timeout.ms:5000}")
	private int restRequestTimeout;
	@Value("${onesaitplatform.flowengine.services.stop.admin}")
	private String stopflowEngine;
	@Value("${onesaitplatform.flowengine.services.domain.status}")
	private String flowEngineDomainStatus;
	@Value("${onesaitplatform.flowengine.services.domain.getall}")
	private String flowEngineDomainGetAll;
	@Value("${onesaitplatform.flowengine.services.domain.get}")
	private String flowEngineDomainGet;
	@Value("${onesaitplatform.flowengine.services.domain.create}")
	private String flowEngineDomainCreate;
	@Value("${onesaitplatform.flowengine.services.domain.delete}")
	private String flowEngineDomainDelete;
	@Value("${onesaitplatform.flowengine.services.domain.start}")
	private String flowEngineDomainStart;
	@Value("${onesaitplatform.flowengine.services.domain.stop}")
	private String flowEngineDomainStop;
	@Value("${onesaitplatform.flowengine.services.sync}")
	private String syncFlowEngineDomains;
	@Value("${onesaitplatform.flowengine.home.base:/tmp/}")
	private String homeBase;
	@Autowired
	private FlowDomainService domainService;

	private HttpComponentsClientHttpRequestFactory httpRequestFactory;
	private ObjectMapper mapper;
	private boolean isSynchronizedWithBDC;

	@PostConstruct
	public void init() {
		httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		httpRequestFactory.setConnectTimeout(restRequestTimeout);
		this.mapper = new ObjectMapper();
		resetSynchronizedWithBDC();
	}

	@Override
	public String stopFlowEngine() {
		String response = null;
		RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
		try {
			response = restTemplate.postForObject(flowengineUrl + stopflowEngine, null, String.class);
		} catch (Exception e) {
			log.warn("Unable to stop the flow engine. Cause={}, message={}", e.getCause(), e.getMessage());
			throw new NodeRedAdminServiceException("Unable to stop the flow engine.");
		}
		return response;
	}

	@Override
	public void resetSynchronizedWithBDC() {
		this.isSynchronizedWithBDC = false;
	}

	@Override
	@FlowEngineAuditable
	public void stopFlowEngineDomain(String domain) {
		checkIsSynchronized();
		RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
		try {
			restTemplate.put(flowengineUrl + flowEngineDomainStop + "/" + domain, null);
		} catch (Exception e) {
			log.warn("Unable to stop the flow engine Domain={}. Cause={}, message={}", domain, e.getCause(),
					e.getMessage());
			throw new NodeRedAdminServiceException("Unable to stop the flow engine Domain = " + domain + ".");
		}
	}

	@Override
	@FlowEngineAuditable
	public String startFlowEngineDomain(FlowEngineDomain domain) {
		String response = null;
		checkIsSynchronized();
		RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<FlowEngineDomain> domainToStart = new HttpEntity<>(domain, headers);
			response = restTemplate.postForObject(flowengineUrl + flowEngineDomainStart, domainToStart, String.class);
		} catch (Exception e) {
			log.warn("Unable to start Domain={}. Cause={}, message={}", domain, e.getCause(), e.getMessage());
			throw new NodeRedAdminServiceException("Unable to start Domain = " + domain + ".");
		}
		return response;
	}

	@Override
	@FlowEngineAuditable
	public String createFlowengineDomain(FlowEngineDomain domain) {
		String response = null;
		checkIsSynchronized();
		RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<FlowEngineDomain> newDomain = new HttpEntity<>(domain, headers);
			response = restTemplate.postForObject(flowengineUrl + flowEngineDomainCreate, newDomain, String.class);
		} catch (Exception e) {
			log.warn("Unable to create Domain={}. Cause={}, message={}", domain, e.getCause(), e.getMessage());
			throw new NodeRedAdminServiceException("Unable to create Domain = " + domain + ".");
		}
		return response;
	}

	@Override
	@FlowEngineAuditable
	public void deleteFlowEngineDomain(String domainId) {
		checkIsSynchronized();
		RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
		try {
			restTemplate.delete(flowengineUrl + flowEngineDomainDelete + "/" + domainId);
		} catch (Exception e) {
			log.warn("Unable to Delete Domain={}. Cause={}, message={}", domainId, e.getCause(), e.getMessage());
			throw new NodeRedAdminServiceException("Unable to delete Domain = " + domainId + ".");
		}
	}

	@Override
	public FlowEngineDomain getFlowEngineDomain(String domainId) {
		FlowEngineDomain response = null;
		checkIsSynchronized();
		RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
		try {
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(flowengineUrl + flowEngineDomainGet)
					.queryParam("domain", domainId);
			String responseRest = restTemplate.getForObject(builder.build().encode().toUri(), String.class);
			response = mapper.readValue(responseRest, new TypeReference<FlowEngineDomain>() {
			});
		} catch (Exception e) {
			log.warn("Unable to retrieve Domain={}. Cause={}, message={}", domainId, e.getCause(), e.getMessage());
			throw new NodeRedAdminServiceException("Unable to retrieve Domain " + domainId + ".");
		}
		return response;
	}

	@Override
	public List<FlowEngineDomainStatus> getAllFlowEnginesDomains() {
		List<FlowEngineDomainStatus> domainStatus = new ArrayList<>();
		checkIsSynchronized();
		RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
		try {
			String responseRest = restTemplate.getForObject(flowengineUrl + flowEngineDomainGetAll, String.class);
			domainStatus = (List<FlowEngineDomainStatus>) FlowEngineDomainStatus
					.fromJsonArrayToDomainStatus(responseRest);
		} catch (Exception e) {
			log.warn("Unable to retrieve all flow engine domains. Cause={}, message={}", e.getCause(), e.getMessage());
			throw new NodeRedAdminServiceException("Unable to retrieve all flow engine domains.");
		}
		return domainStatus;
	}

	@Override
	public List<FlowEngineDomainStatus> getFlowEngineDomainStatus(List<String> domainList) {
		List<FlowEngineDomainStatus> response = null;
		checkIsSynchronized();
		RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
		try {
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(flowengineUrl + flowEngineDomainStatus)
					.queryParam("domains", mapper.writeValueAsString(domainList));

			String responseRest = restTemplate.getForObject(builder.build().encode().toUri(), String.class);
			response = (List<FlowEngineDomainStatus>) FlowEngineDomainStatus.fromJsonArrayToDomainStatus(responseRest);
		} catch (Exception e) {
			log.error("Error retrieving domain's statuses from NodeRedAdminClient. Cause = {}, message = {}",
					e.getCause(), e.getMessage());
			throw new NodeRedAdminServiceException("Unable to retrieve domain's statuses from NodeRedAdminClient.");
		}
		return response;
	}

	@Override
	public String synchronizeMF(List<FlowEngineDomainStatus> domainList) {
		String response = null;
		SynchronizeDomainStatusRequest synchronizeDomainStatusRequest = new SynchronizeDomainStatusRequest();
		synchronizeDomainStatusRequest.setListDomain(domainList);
		RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> domainsToSync = new HttpEntity<>(synchronizeDomainStatusRequest.toJson(), headers);
			response = restTemplate.postForObject(flowengineUrl + syncFlowEngineDomains, domainsToSync, String.class);
			this.isSynchronizedWithBDC = true;
		} catch (Exception e) {
			log.warn("Unable to synchronize domains with CDB. Cause={}, message={}", e.getCause(), e.getMessage());
			throw new NodeRedAdminServiceException("Unable to synchronize domains with CDB.");
		}
		return response;
	}

	private void checkIsSynchronized() {
		if (!this.isSynchronizedWithBDC) {
			log.warn("NodeRed AdminClient is not synchronized with CDB data.");
			throw new NotSynchronizedToCdbException("NodeRed AdminClient is not synchronized with CDB data.");
		}
	}

	@Override
	public String exportDomainFromFS(String domain) {
		try {
			FlowDomain flowDomain = domainService.getFlowDomainByIdentification(domain);
			if (flowDomain != null) {
				String path = homeBase + File.separator + flowDomain.getUser().getUserId() + File.separator + "flows_"
						+ domain + ".json";
				File file = new File(path);
				if( file.exists()){
					return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
				}
				log.error("Could not access to {} domian FS json", domain);
				throw new NodeRedAdminServiceException("NodeRed AdminClient could not FlowDomain json.");
			} else {
				log.error("Domain {} not found.", domain);
				throw new NodeRedAdminServiceException("NodeRed AdminClient could not find the domain.");
			}
		} catch (Exception e) {
			log.error("Could not access to {} domian FS json", domain);
			throw new NodeRedAdminServiceException("NodeRed AdminClient could not FlowDomain json.");
		}
	}
}
