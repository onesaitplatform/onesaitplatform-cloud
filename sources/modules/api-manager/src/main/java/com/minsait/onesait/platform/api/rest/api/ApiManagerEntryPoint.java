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
package com.minsait.onesait.platform.api.rest.api;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.api.service.ApiServiceInterface;
import com.minsait.onesait.platform.api.service.Constants;
import com.minsait.onesait.platform.api.service.impl.ApiServiceImpl.ChainProcessingStatus;
import com.minsait.onesait.platform.commons.metrics.MetricsManager;
import com.minsait.onesait.platform.commons.metrics.Source;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/server")
@Slf4j
public class ApiManagerEntryPoint {
	public static final String ENTRY_POINT_SERVLET_URI = "/server/api";
	@Autowired
	private ApiServiceInterface apiService;

	@Autowired(required = false)
	private MetricsManager metricsManager;

	@RequestMapping(value = "/api/**", method = RequestMethod.OPTIONS)
	public ResponseEntity<String> processOptions() {
		final HttpHeaders headers = getOptionsHeaders();
		return new ResponseEntity<>("[\"OPTIONS\",\"OPTIONS\"]", headers, HttpStatus.OK);
	}

	@RequestMapping(value = "/api/**", method = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
			RequestMethod.DELETE })
	public ResponseEntity<?> processRequest(HttpServletRequest request, HttpServletResponse response,
			@RequestBody(required = false) byte[] requestBody) {
		Map<String, Object> mData = new HashMap<>();
		try {
			log.debug("Processing request data");
			mData = apiService.processRequestData(request, response, requestBody);
			ChainProcessingStatus status = (ChainProcessingStatus) mData.get(Constants.STATUS);
			if (status == ChainProcessingStatus.STOP) {
				log.error("STOP state detected: exiting");
				return buildErrorResponse(mData);
			}
			log.debug("Processing logic ");
			mData = apiService.processLogic(mData);
			status = (ChainProcessingStatus) mData.get(Constants.STATUS);
			if (status == ChainProcessingStatus.STOP) {
				log.error("Error Processing Query, Stop Execution detected");
				return buildErrorResponse(mData);

			}
			log.debug("Processing output");
			mData = apiService.processOutput(mData);
			log.debug("Building response data");
			return buildResponse(mData);

		} catch (final Exception e) {
			log.error("Error processing operation", e);
			return buildErrorResponse(mData);
		} finally {
			if (null != metricsManager) {
				String ontologyName = "";
				if (mData.get(ApiServiceInterface.ONTOLOGY) != null) {
					ontologyName = mData.get(ApiServiceInterface.ONTOLOGY).toString();
				} else if ((Api) mData.get(ApiServiceInterface.API) != null
						&& ((Api) mData.get(ApiServiceInterface.API)).getOntology() != null) {
					ontologyName = ((Api) mData.get(ApiServiceInterface.API)).getOntology().getIdentification();
				}
				if (mData.get(ApiServiceInterface.USER) != null) {
					metricsManager.logMetricApiManager(mData.get(ApiServiceInterface.USER).toString(), ontologyName,
							mData.get(ApiServiceInterface.METHOD).toString(), Source.APIMANAGER,
							mData.get(ApiServiceInterface.HTTP_RESPONSE_CODE) == null ? String.valueOf(HttpStatus.OK)
									: mData.get(ApiServiceInterface.HTTP_RESPONSE_CODE).toString(),
							((Api) mData.get(ApiServiceInterface.API)).getIdentification());
				}
			}
			SecurityContextHolder.getContext().setAuthentication(null);
			MultitenancyContextHolder.setVerticalSchema(null);
		}

	}

	private ResponseEntity<?> buildResponse(Map<String, Object> mData) {
		final String contentType = (String) mData.get(Constants.CONTENT_TYPE);
		final HttpHeaders headers = getDefaultHeaders(contentType);
		final Object output = mData.get(Constants.OUTPUT);
		if (output instanceof ByteArrayResource) {
			headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(((ByteArrayResource) output).contentLength()));
			// headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline");
		}
		if (mData.get(Constants.HTTP_RESPONSE_CODE) != null) {

			return new ResponseEntity<>(output, headers, (HttpStatus) mData.get(Constants.HTTP_RESPONSE_CODE));
		} else {
			return new ResponseEntity<>(output, headers, HttpStatus.OK);
		}

	}

	private ResponseEntity<String> buildErrorResponse(Map<String, Object> mData) {
		final String contentType = (String) mData.get(Constants.CONTENT_TYPE);
		final HttpHeaders headers = getDefaultHeaders(contentType);
		if (mData.get(Constants.HTTP_RESPONSE_CODE) != null) {
			return new ResponseEntity<>((String) mData.get(Constants.REASON), headers,
					(HttpStatus) mData.get(Constants.HTTP_RESPONSE_CODE));
		} else {
			return new ResponseEntity<>((String) mData.get(Constants.REASON), headers,
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	private HttpHeaders getDefaultHeaders(String contentType) {
		final HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, contentType);
		return headers;
	}

	private HttpHeaders getOptionsHeaders() {
		final HttpHeaders headers = getDefaultHeaders(MediaType.APPLICATION_JSON_VALUE);
		headers.add("Access-Control-Allow-Headers", "X-SOFIA2-APIKey,auth-token,Content-Type");
		headers.add("Access-Control-Allow-Methods", "POST,GET,DELETE,PUT,OPTIONS");
		return headers;
	}

}
