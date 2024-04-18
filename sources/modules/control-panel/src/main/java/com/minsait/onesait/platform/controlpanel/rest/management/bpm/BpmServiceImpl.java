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
package com.minsait.onesait.platform.controlpanel.rest.management.bpm;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.config.services.exceptions.BPMServiceException;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BpmServiceImpl implements BpmService {
	
	@Value("${onesaitplatform.bpm.url}")
	private String bpmUrl;

	@Autowired
	private IntegrationResourcesService resourcesService;
	
	private RestTemplate restTemplate= new RestTemplate(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());
	
	@Override
	public Object getAllProcessDef(String bearer) {
		try {
			final HttpHeaders headers = new HttpHeaders();
			headers.add("Authorization", "Bearer " + bearer);
			
			ResponseEntity<Object> res = restTemplate.exchange(resourcesService.getUrl(Module.BPM_ENGINE, ServiceUrl.BASE) + "/engine-rest/process-definition", HttpMethod.GET, new HttpEntity<>(headers), Object.class);
			return res.getBody();
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("error", e);
			throw new BPMServiceException(
					"HttpResponse code : " + e.getStatusCode() + " , cause: " + e.getResponseBodyAsString());
		}
	}

	@Override
	public Object getAllProcessDefById(String id, String bearer) {
		try {
			final HttpHeaders headers = new HttpHeaders();
			headers.add("Authorization", "Bearer " + bearer);
			
			ResponseEntity<Object> res = restTemplate.exchange(bpmUrl + "/engine-rest/process-definition/" + id, HttpMethod.GET, new HttpEntity<>(headers), Object.class);
			return res.getBody();
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("error", e);
			throw new BPMServiceException(
					"HttpResponse code : " + e.getStatusCode() + " , cause: " + e.getResponseBodyAsString());
		}
	}	@Override
	public String getProcessDefXmlById(String id, String bearer) {
		try {
			final HttpHeaders headers = new HttpHeaders();
			headers.add("Authorization", "Bearer " + bearer);
			
			ResponseEntity<String> res = restTemplate.exchange(resourcesService.getUrl(Module.BPM_ENGINE, ServiceUrl.BASE) + "/engine-rest/process-definition/" + id + "/xml", HttpMethod.GET, new HttpEntity<>(headers), String.class);
			JSONObject jsonRes = new JSONObject(res.getBody().toString());
			String xml = jsonRes.getString("bpmn20Xml");
			return xml;
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("error", e);
			throw new BPMServiceException(
					"HttpResponse code : " + e.getStatusCode() + " , cause: " + e.getResponseBodyAsString());
		}
	}
	
	@Override
	public Object cloneProcessDef(BpmRequestDTO request, String bearer) throws IOException {
		String sourcePDId = request.getSourceProcessDefId();
		String targetDeployment = request.getTargetDeployment();
		String targetDeploymentId = targetDeployment.replaceAll("\\s+","");
		
		String xml = getProcessDefXmlById(sourcePDId, bearer);
		Object processDef = getAllProcessDefById(sourcePDId, bearer);
		
		JSONObject process = new JSONObject(processDef.toString());
		String key = process.getString("key");
		String name = process.getString("name");
		
		xml = xml.replaceAll(key,targetDeploymentId);
		xml = xml.replaceAll(name, targetDeployment);
		
		File file = null;
		try {
			final MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
			file = new File("/tmp/" + targetDeploymentId +"-clone.bpmn");
			Files.write(file.toPath(), xml.getBytes());
			formData.add("data", new FileSystemResource(file));
			formData.add("deployment-name", targetDeployment);
			formData.add("tenant-id", request.getTargetTenantId());
			
			final HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE);
			headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + bearer);
			ResponseEntity<Object> res = restTemplate.exchange(resourcesService.getUrl(Module.BPM_ENGINE, ServiceUrl.DEPLOYMENT), HttpMethod.POST,
					new HttpEntity<>(formData, headers), Object.class);
			return res.getBody();
		} finally {
			file = new File("/tmp/" + targetDeploymentId +"-clone.bpmn");
			if (file != null && file.exists()) {
				file.delete();
			}
		}
	}

	@Override
	public Object getAllDeployments(String bearer) {
		try {
			final HttpHeaders headers = new HttpHeaders();
			headers.add("Authorization", "Bearer " + bearer);
			
			ResponseEntity<Object> res = restTemplate.exchange(bpmUrl + "/engine-rest/deployment", HttpMethod.GET, new HttpEntity<>(headers), Object.class);
			return res.getBody();
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("error", e);
			throw new BPMServiceException(
					"HttpResponse code : " + e.getStatusCode() + " , cause: " + e.getResponseBodyAsString());
		}
	}

	@Override
	public Object getDeploymentById(String id, String bearer) {
		try {
			final HttpHeaders headers = new HttpHeaders();
			headers.add("Authorization", "Bearer " + bearer);
			
			ResponseEntity<Object> res = restTemplate.exchange(bpmUrl + "/engine-rest/deployment/" + id, HttpMethod.GET, new HttpEntity<>(headers), Object.class);
			return res.getBody();
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("error", e);
			throw new BPMServiceException(
					"HttpResponse code : " + e.getStatusCode() + " , cause: " + e.getResponseBodyAsString());
		}
	}
	
	@Override
	public Object getAllProcessInstances(String bearer) {
		try {
			final HttpHeaders headers = new HttpHeaders();
			headers.add("Authorization", "Bearer " + bearer);
			
			ResponseEntity<Object> res = restTemplate.exchange(bpmUrl + "/engine-rest/process-instance", HttpMethod.GET, new HttpEntity<>(headers), Object.class);
			return res.getBody();
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("error", e);
			throw new BPMServiceException(
					"HttpResponse code : " + e.getStatusCode() + " , cause: " + e.getResponseBodyAsString());
		}
	}

	@Override
	public Object getProcessInstanceById(String id, String bearer) {
		try {
			final HttpHeaders headers = new HttpHeaders();
			headers.add("Authorization", "Bearer " + bearer);
			
			ResponseEntity<Object> res = restTemplate.exchange(bpmUrl + "/engine-rest/process-instance/" + id, HttpMethod.GET, new HttpEntity<>(headers), Object.class);
			return res.getBody();
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("error", e);
			throw new BPMServiceException(
					"HttpResponse code : " + e.getStatusCode() + " , cause: " + e.getResponseBodyAsString());
		}
	}

}
