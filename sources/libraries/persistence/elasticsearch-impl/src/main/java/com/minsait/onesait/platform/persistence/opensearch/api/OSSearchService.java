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
package com.minsait.onesait.platform.persistence.opensearch.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.query_dsl.IdsQuery;
import org.opensearch.client.opensearch._types.query_dsl.MatchAllQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.QueryStringQuery;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.persistence.OpensearchEnabledCondition;
import com.minsait.onesait.platform.persistence.opensearch.OpenSearchUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Conditional(OpensearchEnabledCondition.class)
@Slf4j
public class OSSearchService {

	@Autowired
	private OpenSearchClient javaClient;

	private SearchResponse<Object> search(Query query, String... indices) {
		SearchRequest searchRequest = new SearchRequest.Builder().query(query).index(Arrays.asList(indices)).build();
		return executeSearch(searchRequest, indices);
	}

	private SearchResponse<Object> search(Query query, int size, String... indices) {
		SearchRequest searchRequest = new SearchRequest.Builder().query(query).index(Arrays.asList(indices)).size(size)
				.build();
		return executeSearch(searchRequest, indices);
	}

	private SearchResponse<Object> search(Query query, int from, int size, String... indices) {
		SearchRequest searchRequest = new SearchRequest.Builder().query(query).index(Arrays.asList(indices)).size(size)
				.from(from).build();
		return executeSearch(searchRequest, indices);
	}

	private SearchResponse<Object> executeSearch(SearchRequest searchRequest, String... indices) {
		try {
			return javaClient.search(searchRequest, Object.class);
		} catch (final IOException e) {
			log.error("Error in search query ", e);
			return null;
		}
	}

	public List<String> findQueryData(String jsonQuery, String... indices) {
		final Query q = new Query.Builder().queryString(new QueryStringQuery.Builder().query(jsonQuery).build())
				.build();
		SearchResponse<Object> searchResponse = search(q, indices);
		return OpenSearchUtil.processSearchResponseToStringList(searchResponse);
	}

	public String findQueryDataAsJson(String jsonQuery, String... indices) {
		final Query q = new Query.Builder().queryString(new QueryStringQuery.Builder().query(jsonQuery).build())
				.build();
		SearchResponse<Object> searchResponse = search(q, indices);
		return OpenSearchUtil.processSearchResponseToJson(searchResponse);
	}

	public String findByIndex(String index, String documentId) {
		final Query q = new Query.Builder().ids(new IdsQuery.Builder().values(documentId).build()).build();
		SearchResponse<Object> searchResponse = search(q, index);
		return OpenSearchUtil.processSearchResponseToJson(searchResponse);
	}

	public List<String> findAll(String index) {
		final Query q = new Query.Builder().matchAll(new MatchAllQuery.Builder().build()).build();
		SearchResponse<Object> searchResponse = search(q, index);
		return OpenSearchUtil.processSearchResponseToStringList(searchResponse);
	}

	public List<String> findAll(String index, int from, int size) {
		final Query q = new Query.Builder().matchAll(new MatchAllQuery.Builder().build()).build();
		SearchResponse<Object> searchResponse = search(q, from, size, index);
		return OpenSearchUtil.processSearchResponseToStringList(searchResponse);
	}

	public List<String> findAll(String index, String jsonQuery, int from, int size) {
		final Query q = new Query.Builder().queryString(new QueryStringQuery.Builder().query(jsonQuery).build())
				.build();
		SearchResponse<Object> searchResponse = search(q, from, size, index);
		return OpenSearchUtil.processSearchResponseToStringList(searchResponse);
	}

	public List<String> findAll(String index, int size) {
		final Query q = new Query.Builder().matchAll(new MatchAllQuery.Builder().build()).build();
		SearchResponse<Object> searchResponse = search(q, size, index);
		return OpenSearchUtil.processSearchResponseToStringList(searchResponse);
	}

	public String findAllAsJson(String index, int size) {
		final Query q = new Query.Builder().matchAll(new MatchAllQuery.Builder().build()).build();
		SearchResponse<Object> searchResponse = search(q, size, index);
		return OpenSearchUtil.processSearchResponseToJson(searchResponse);
	}

	public String findAllAsJson(String index, int from, int size) {
		final Query q = new Query.Builder().matchAll(new MatchAllQuery.Builder().build()).build();
		SearchResponse<Object> searchResponse = search(q, from, size, index);
		return OpenSearchUtil.processSearchResponseToJson(searchResponse);
	}

}
