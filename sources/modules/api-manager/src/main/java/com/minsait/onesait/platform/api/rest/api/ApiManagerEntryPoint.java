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
package com.minsait.onesait.platform.api.rest.api;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdOptions;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.minsait.onesait.platform.api.service.ApiServiceInterface;
import com.minsait.onesait.platform.api.service.Constants;
import com.minsait.onesait.platform.api.service.impl.ApiServiceImpl.ChainProcessingStatus;
import com.minsait.onesait.platform.commons.metrics.MetricsManager;
import com.minsait.onesait.platform.commons.metrics.Source;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;

import jakarta.json.JsonObject;
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
				return buildErrorResponse(mData, "");
			}
			log.debug("Processing logic ");
			mData = apiService.processLogic(mData);
			status = (ChainProcessingStatus) mData.get(Constants.STATUS);
			if (status == ChainProcessingStatus.STOP) {
				log.error("Error Processing Query, Stop Execution detected");
				return buildErrorResponse(mData, "");

			}
			log.debug("Processing output");
			mData = apiService.processOutput(mData);
			log.debug("Building response data");
			return buildResponse(mData);

		} catch (final Exception e) {
			log.error("Error processing operation", e);
			return buildErrorResponse(mData, e.getMessage());
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
			//SecurityContextHolder.getContext().setAuthentication(null);
			SecurityContextHolder.clearContext();
			MultitenancyContextHolder.setVerticalSchema(null);
		}

	}

	private ResponseEntity<?> buildResponse(Map<String, Object> mData) {
		final String contentType = (String) mData.get(Constants.CONTENT_TYPE);
		final HttpHeaders headers = getDefaultHeaders(contentType,
				mData.get(Constants.HTTP_RESPONSE_HEADERS) != null
						? (HttpHeaders) mData.get(Constants.HTTP_RESPONSE_HEADERS)
						: null);

		Object output = mData.get(Constants.OUTPUT);
		if (output instanceof ByteArrayResource) {
			headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(((ByteArrayResource) output).contentLength()));
			// headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline");
		}

		final Ontology onto = (Ontology) mData.get(Constants.ONTOLOGY);
		if (onto != null) {
			if (onto.isSupportsJsonLd() && contentType.equals("application/ld+json")) {
				try {

					// for all elements put @type and delete _id and context
					final JSONObject context = new JSONObject(onto.getJsonLdContext().toString());
					final JSONArray typeArray = context.getJSONArray("@type");
					// context.remove("@type");

					final JSONArray originalArray = new JSONArray(output.toString());
					for (int i = 0; i < originalArray.length(); i++) {
						final JSONObject explrObject = originalArray.getJSONObject(i);
						if (explrObject.has("_id")) {
							explrObject.remove("_id");
						}
						if (explrObject.has("contextData")) {
							explrObject.remove("contextData");
						}
						final String key = (String) explrObject.keys().next();
						if (explrObject.has(key)) {
							final JSONObject parameterObj = explrObject.getJSONObject(key);
							parameterObj.put("@type", typeArray.get(0).toString());
						}

						/*
						 * JSONObject contexObj = new JSONObject(); contexObj.put("@vocab",
						 * "http://schema.org/"); JSONObject elemRoot = new JSONObject();
						 * elemRoot.put("@id", typeArray.get(0).toString()); contexObj.put(key,
						 * elemRoot);
						 */

						final JSONObject contexObj = new JSONObject(onto.getJsonLdContext());
						// contexObj.put("@vocab", "http://schema.org/");
						// JSONObject elemRoot = new JSONObject();
						// elemRoot.put("@id", typeArray.get(0).toString());
						// contexObj.put(key, elemRoot);

						explrObject.put("@context", contexObj.get("@context"));
					}

					final Document documentJson = JsonDocument
							.of(new ByteArrayInputStream(originalArray.toString().getBytes()));
					final Document documentContext = JsonDocument
							.of(new ByteArrayInputStream(onto.getJsonLdContext().getBytes()));

					final JsonLdOptions opt = new JsonLdOptions();
					// opt.setUseNativeTypes(true);
					// EXPANDED MODE
					// JsonArray jsonArray =
					// JsonLd.expand(documentJson).context(documentContext).options(opt).get();
					// COMPACT MODE
					final JsonObject jsonArray = JsonLd.compact(documentJson, documentContext).get();

					output = jsonArray;
				} catch (final JsonLdError ex) {
					return new ResponseEntity<>(ex.getMessage(), headers, HttpStatus.INTERNAL_SERVER_ERROR);
				} catch (final JSONException ex) {
					return new ResponseEntity<>(ex.getMessage(), headers, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} else if (!onto.isSupportsJsonLd() && contentType.equals("application/ld+json")) {
				return new ResponseEntity<>(headers, HttpStatus.NOT_ACCEPTABLE);
			}
		}

		if (mData.get(Constants.HTTP_RESPONSE_CODE) != null) {

			return new ResponseEntity<>(output, headers, (HttpStatus) mData.get(Constants.HTTP_RESPONSE_CODE));
		} else {
			return new ResponseEntity<>(output.toString(), headers, HttpStatus.OK);
		}
	}

	private ResponseEntity<String> buildErrorResponse(Map<String, Object> mData, String ex) {
		final String contentType = (String) mData.get(Constants.CONTENT_TYPE);
		final HttpHeaders headers = getDefaultHeaders(contentType,
				mData.get(Constants.HTTP_RESPONSE_HEADERS) != null
						? (HttpHeaders) mData.get(Constants.HTTP_RESPONSE_HEADERS)
						: null);
		// TO-DO revisar ELISA
//		if (contentType == null && !ex.isEmpty()) {
//			return new ResponseEntity<>(ex, headers, HttpStatus.BAD_REQUEST);
//		}
		if (mData.get(Constants.HTTP_RESPONSE_CODE) != null) {
			return new ResponseEntity<>((String) mData.get(Constants.REASON), headers,
					(HttpStatus) mData.get(Constants.HTTP_RESPONSE_CODE));
		} else {
			return new ResponseEntity<>((String) mData.get(Constants.REASON), headers,
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	private HttpHeaders getDefaultHeaders(String contentType, HttpHeaders initialheaders) {

		final HttpHeaders headers = new HttpHeaders();
		if (initialheaders != null) {
			initialheaders.entrySet().forEach(e -> {
				headers.add(e.getKey(), e.getValue().iterator().next());
			});
		}
		if (!headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
			headers.add(HttpHeaders.CONTENT_TYPE, contentType);
		}
		return headers;
	}

	private HttpHeaders getOptionsHeaders() {
		final HttpHeaders headers = getDefaultHeaders(MediaType.APPLICATION_JSON_VALUE, null);
		headers.add("Access-Control-Allow-Headers", "X-SOFIA2-APIKey,auth-token,Content-Type");
		headers.add("Access-Control-Allow-Methods", "POST,GET,DELETE,PUT,OPTIONS");
		return headers;
	}

}
