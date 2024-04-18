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
package com.minsait.onesait.platform.persistence.mindsdb.http;

import javax.annotation.PostConstruct;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.persistence.mindsdb.model.MindsDBPredictorDatasource;
import com.minsait.onesait.platform.persistence.mindsdb.model.MindsDBPredictorPayload;
import com.minsait.onesait.platform.persistence.mindsdb.model.MindsDBQuery;

@Component
public class MindsDBHTTPClient {

	private static final RestTemplate client = new RestTemplate(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());
	public static final String DEFAULT_MINDSDB_HTTP_API_URL = "http://mindsdb:47334";
	public static final String API_QUERY_URL = "/api/sql/query";
	public static final String API_DATASOURCES_URL = "/api/datasources";
	public static final String API_PREDICTORS_URL = "/api/predictors";
	private static final ObjectMapper mapper = new ObjectMapper();
	 

	public JsonNode createMindsDBDatasource(MindsDBPredictorDatasource ds) {
		return executeHttp(DEFAULT_MINDSDB_HTTP_API_URL + API_DATASOURCES_URL + "/" + ds.getName(), HttpMethod.PUT, ds);
	}

	public void deleteMindsDBDatasource(String name) {
		executeHttp(DEFAULT_MINDSDB_HTTP_API_URL + API_DATASOURCES_URL + "/" + name, HttpMethod.DELETE, null);
	}

	public void deleteMindsDBPredictor(String name) {
		executeHttp(DEFAULT_MINDSDB_HTTP_API_URL + API_PREDICTORS_URL + "/" + name, HttpMethod.DELETE, null);
	}

	public JsonNode getPredictor(String predictor) {
		return executeHttp(DEFAULT_MINDSDB_HTTP_API_URL + API_PREDICTORS_URL + "/" + predictor, HttpMethod.GET, null);
	}

	public JsonNode getDatasource(String name) {
		return executeHttp(DEFAULT_MINDSDB_HTTP_API_URL + API_DATASOURCES_URL + "/" + name, HttpMethod.GET, null);
	}

	public JsonNode createMindsDBPredictor(MindsDBPredictorPayload pds) {
		return executeHttp(DEFAULT_MINDSDB_HTTP_API_URL + API_PREDICTORS_URL + "/" + pds.getDatasourceName(),
				HttpMethod.PUT, pds);
	}

	public String sendQuery(MindsDBQuery query) {
		return executeRequest(DEFAULT_MINDSDB_HTTP_API_URL + API_QUERY_URL, HttpMethod.POST, query);
	}

	public ArrayNode sendQueryJson(MindsDBQuery query) {
		return executeRequestJson(DEFAULT_MINDSDB_HTTP_API_URL + API_QUERY_URL, HttpMethod.POST, query);
	}

	public String executeRequest(String url, HttpMethod method, Object reqEntity) {
		try {
			return mapper.writeValueAsString(executeRequestJson(url, method, reqEntity));
		} catch (final Exception e) {
			throw new RuntimeException("Error while querying MindsDB: " + e.getMessage());
		}
	}

	public ArrayNode executeRequestJson(String url, HttpMethod method, Object reqEntity) {
		return parseQueryResponse(executeHttp(url, method, reqEntity));
	}

	public JsonNode executeHttp(String url, HttpMethod method, Object reqEntity) {
		try {
			final HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
			final ResponseEntity<JsonNode> queryResponse = client.exchange(url, method,
					new HttpEntity<>(reqEntity, headers), JsonNode.class);
			return queryResponse.getBody();
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			throw new RuntimeException(
					"HttpResponse code : " + e.getStatusCode() + " , cause: " + e.getResponseBodyAsString());
		} catch (final Exception e) {
			throw new RuntimeException("Error while querying MindsDB: " + e.getMessage());
		}
	}

	private ArrayNode parseQueryResponse(JsonNode queryResponse) {
		final ArrayNode responseNode = mapper.createArrayNode();
		if (queryResponse != null && !queryResponse.path("column_names").isMissingNode()
				&& !queryResponse.path("data").isMissingNode()) {
			final String[] columnNames = mapper.convertValue(queryResponse.get("column_names"), String[].class);
			final ArrayNode data = mapper.convertValue(queryResponse.get("data"), ArrayNode.class);
			for (int i = 0; i < data.size(); i++) {
				final ObjectNode n = mapper.createObjectNode();
				final ArrayNode dataNode = mapper.convertValue(data.get(i), ArrayNode.class);
				for (int j = 0; j < columnNames.length; j++) {
					n.set(columnNames[j], dataNode.get(j));
				}
				responseNode.add(n);
			}

		}
		return responseNode;

	}

}
