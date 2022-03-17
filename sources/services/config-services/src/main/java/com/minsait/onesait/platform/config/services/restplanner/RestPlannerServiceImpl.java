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
package com.minsait.onesait.platform.config.services.restplanner;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.config.model.RestPlanner;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.RestPlannerRepository;
import com.minsait.onesait.platform.config.services.exceptions.OPResourceServiceException;
import com.minsait.onesait.platform.config.services.user.UserService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RestPlannerServiceImpl implements RestPlannerService {

	@Autowired
	private RestPlannerRepository restPlannerRepository;
	@Autowired
	private UserService userService;
	@Value("${onesaitplatform.restPlanner.checkSSL:false}")
	private Boolean checkSSL;

	@Override
	public List<RestPlanner> getAllRestPlanners() {
		return restPlannerRepository.findAll();
	}

	@Override
	public List<RestPlanner> getAllRestPlannersByUser(String userId) {
		final User user = userService.getUser(userId);
		return restPlannerRepository.findByUser(user);
	}

	@Override
	public boolean hasUserPermission(String id, String userId) {
		final User user = userService.getUser(userId);
		if (userService.isUserAdministrator(user)) {
			return true;
		} else {
			final Optional<RestPlanner> rp = restPlannerRepository.findById(id);
			if (rp.isPresent())
				return rp.get().getUser().getUserId().equals(userId);
			else
				return false;
		}
	}

	@Override
	public RestPlanner getRestPlannerById(String id) {
		return restPlannerRepository.findById(id).orElse(null);
	}

	@Override
	public RestPlanner getRestPlannerByIdentification(String identificator) {
		return restPlannerRepository.findByIdentification(identificator);
	}

	@Override
	public RestPlanner createRestPlannerService(RestPlanner restPlanner, User user) {
		restPlanner.setUser(user);
		restPlanner.setActive(false);
		restPlannerRepository.save(restPlanner);
		return restPlanner;
	}

	@Override
	public RestPlanner updateRestPlanner(RestPlanner restPlanner) {
		final Optional<RestPlanner> opt = restPlannerRepository.findById(restPlanner.getId());
		if (!opt.isPresent())
			throw new OPResourceServiceException("Rest Planner not found");
		final RestPlanner restPlannerDB = opt.get();
		restPlannerDB.setCron(restPlanner.getCron());
		restPlannerDB.setDescription(restPlanner.getDescription());
		restPlannerDB.setMethod(restPlanner.getMethod());
		restPlannerDB.setUrl(restPlanner.getUrl());
		restPlannerDB.setBody(restPlanner.getBody());
		restPlannerDB.setHeaders(restPlanner.getHeaders());
		restPlannerDB.setDateFrom(restPlanner.getDateFrom());
		restPlannerDB.setDateTo(restPlanner.getDateTo());
		restPlannerRepository.save(restPlannerDB);
		return restPlannerDB;
	}

	@Override
	@Transactional
	public void deleteRestPlannerById(String id) {
		restPlannerRepository.findById(id).ifPresent(rp -> restPlannerRepository.delete(rp));

	}

	@Override
	public String execute(String user, String url, String method, String body, String headers) throws Exception {
		try {
			String result = "";
			ResponseEntity<String> resp = sendHttp(url, HttpMethod.valueOf(method), body, headers);
			if (resp.getStatusCode().is4xxClientError() || resp.getStatusCode().is5xxServerError())
				result = "ERROR - " + resp.getStatusCodeValue() + " - " + resp.getBody();
			else
				result = "OK - " + resp.getStatusCodeValue() + " - " + resp.getBody();
			return result;
		} catch (final Exception e) {
			return "ERROR - " + e.getMessage();
		} finally {

		}
	}

	private ResponseEntity<String> sendHttp(String url, HttpMethod httpMethod, String body, String headersStr)
			throws URISyntaxException, IOException {
		RestTemplate restTemplate = null;
		int statusCode;
		if (checkSSL) {
			restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
		} else {
			restTemplate = new RestTemplate(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());
		}
		final HttpHeaders headers = toHttpHeaders(headersStr);
		final org.springframework.http.HttpEntity<String> request = new org.springframework.http.HttpEntity<>(body,
				headers);
		log.debug("Sending method " + httpMethod.toString());
		ResponseEntity<String> response = new ResponseEntity<>(HttpStatus.ACCEPTED);
		try {
			log.debug("Execute method " + httpMethod.toString() + " " + url);
			response = restTemplate.exchange(new URI(url), httpMethod, request, String.class);
		} catch (final Exception e) {
			log.debug(e.getMessage());
			statusCode = getStatusCode(e.getMessage());
			return new ResponseEntity<>(e.getMessage(), new HttpHeaders(), HttpStatus.valueOf(statusCode));
		}
		final HttpHeaders responseHeaders = new HttpHeaders();
		if (response.getHeaders().getContentType() != null) {
			responseHeaders.set("Content-Type", response.getHeaders().getContentType().toString());
		}
		return new ResponseEntity<>(response.getBody(), responseHeaders,
				HttpStatus.valueOf(response.getStatusCode().value()));
	}

	private int getStatusCode(String e) {
		int statusCode;
		try {
			statusCode = Integer.parseInt(e.split(" ")[0]);
		} catch (NumberFormatException ex) {
			statusCode = 500;
		}
		return statusCode;
	}

	private HttpHeaders toHttpHeaders(String headersStr) {
		final HttpHeaders httpHeaders = new HttpHeaders();
		final String[] heads = headersStr.split("\n");
		String headerName = "";
		String headerValue = "";
		for (final String head : heads) {
			headerName = head.split(":")[0];
			headerValue = head.substring(head.indexOf(':') + 1).trim();
			httpHeaders.add(headerName, headerValue);
		}
		return httpHeaders;
	}

}
