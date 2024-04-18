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
package com.minsait.onesait.platform.config.services.modelsmanager;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.minsait.onesait.platform.config.services.modelsmanager.configuration.ModelsManagerServiceConfiguration;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ModelsManagerServiceImpl implements ModelsManagerService {

	@Autowired
	private ModelsManagerServiceConfiguration configuration;

	private static String PATH_MODELSMANAGER = "/modelsmanager/";

	@Override
	public ResponseEntity<String> sendHttp(HttpServletRequest requestServlet, HttpMethod httpMethod, String body)
			throws URISyntaxException, IOException {
		return sendHttp(requestServlet.getServletPath(), httpMethod, body,
				requestServlet.getQueryString() == null ? "" : "?" + requestServlet.getQueryString());
	}

	@Override
	public ResponseEntity<String> sendHttp(String url, HttpMethod httpMethod, String body, String queryParams)
			throws URISyntaxException, IOException {
		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return sendHttp(url, httpMethod, body, headers, queryParams);
	}

	@Override
	public ResponseEntity<String> sendHttp(String url, HttpMethod httpMethod, String body, HttpHeaders headers,
			String queryString) throws URISyntaxException, IOException {
		final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

		// headers.add("Authorization", encryptRestUserpass());
		final org.springframework.http.HttpEntity<String> request = new org.springframework.http.HttpEntity<>(body,
				headers);
		if (log.isDebugEnabled()) {
			log.debug("Sending method {} Models Manager", httpMethod.toString());
		}
		ResponseEntity<String> response = new ResponseEntity<>(HttpStatus.ACCEPTED);
		try {
			response = restTemplate.exchange(
					new URI(configuration.getBaseURL() + url.substring(PATH_MODELSMANAGER.length()) + queryString),
					httpMethod, request, String.class);
		} catch (final Exception e) {
			log.error(e.getMessage());
			return new ResponseEntity<>(e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		if (log.isDebugEnabled()) {
			log.debug("Execute method {} '{}' Models Manager", httpMethod.toString(), url);
		}
		final HttpHeaders responseHeaders = new HttpHeaders();
		if (response.getHeaders().getContentType() != null) {
			responseHeaders.set("Content-Type", response.getHeaders().getContentType().toString());
		}
		return new ResponseEntity<>(response.getBody(), responseHeaders,
				HttpStatus.valueOf(response.getStatusCode().value()));
	}

}
