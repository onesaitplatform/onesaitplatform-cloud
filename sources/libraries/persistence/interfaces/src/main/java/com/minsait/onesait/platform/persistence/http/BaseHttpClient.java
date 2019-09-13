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
package com.minsait.onesait.platform.persistence.http;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.exceptions.QueryNativeFormatException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BaseHttpClient {

	public static final String ACCEPT_TEXT_CSV = "text/csv; columnDelimiter=|&rowDelimiter=;&quoteChar='&escapeChar=\\\\";

	private static final String NOTIFING_ERROR = "Error notifing message to endpoint: {}, {}";

	@Autowired(required = false)
	@Qualifier("dataHubRest")
	private RestTemplate restTemplate;

	public String invokeSQLPlugin(String endpoint, String accept, String contentType) {

		String output = null;
		final ObjectMapper mapper = new ObjectMapper();
		try {

			final HttpEntity<?> entity = new HttpEntity<>(getHeaders(accept, contentType));
			final ResponseEntity<JsonNode> response = restTemplate.exchange(new URI(endpoint), HttpMethod.GET, entity,
					JsonNode.class);
			log.info("Send message: to {}.", endpoint);

			if (response != null) {
				output = mapper.writeValueAsString(response.getBody());
				if (response.getStatusCode() != HttpStatus.OK) {
					log.warn("{}: {}. HTTP status code {}.", NOTIFING_ERROR, endpoint, response.getStatusCode());
					throw new DBPersistenceException(
							"Error: httpStatusCode=" + response.getStatusCode() + " error:" + output);
				}
			} else {
				log.error("{} {}. Malformed HTTP response.", NOTIFING_ERROR, endpoint);
			}
			return output;
		} catch (final DBPersistenceException e) {
			log.error(NOTIFING_ERROR, endpoint, e);
			throw e;
		} catch (final HttpStatusCodeException e) {
			log.error(NOTIFING_ERROR, endpoint, e.getResponseBodyAsString());
			throw new QueryNativeFormatException(NOTIFING_ERROR + endpoint, e);
		} catch (final Exception e) {
			log.error("Error Invocating: {}, {}", endpoint, e);
			throw new DBPersistenceException(e);
		}

	}

	public String invokeSQLPlugin(String endpoint) {
		return invokeSQLPlugin(endpoint, ACCEPT_TEXT_CSV, null);
	}

	private HttpHeaders getHeaders(String accept, String contentType) {

		final HttpHeaders headers = new HttpHeaders();
		if (null != accept && accept.trim().length() > 0) {
			headers.add(HttpHeaders.ACCEPT, accept);
		}
		if (null != contentType && contentType.trim().length() > 0) {
			headers.add(HttpHeaders.CONTENT_TYPE, contentType);
		} else {
			headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		}
		return headers;
	}

}
