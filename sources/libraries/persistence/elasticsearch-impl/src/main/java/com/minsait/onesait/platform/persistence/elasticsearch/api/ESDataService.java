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
package com.minsait.onesait.platform.persistence.elasticsearch.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.persistence.elasticsearch.ElasticSearchUtil;

import io.searchbox.core.DocumentResult;
import io.searchbox.core.Get;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ESDataService {

	@Autowired
	ESBaseApi connector;
	@Autowired
	private ElasticSearchUtil util;

	private static final String QUERY_ERROR = "Query Error:";
	private static final String SIZE_STR = "[SIZE]";
	private static final String FROM_STR = "[FROM]";

	public List<String> findQueryData(String jsonQueryString, String... indexes) {
		log.info("findQueryData");

		jsonQueryString = jsonQueryString.replaceAll("\\n", "");
		jsonQueryString = jsonQueryString.replaceAll("\\r", "");

		final List<String> list = new ArrayList<>(Arrays.asList(indexes));
		final Search search = new Search.Builder(jsonQueryString).addIndex(list.get(0)).build();

		SearchResult result;
		try {
			result = connector.getHttpClient().execute(search);

			if (result.isSucceeded())
				return result.getSourceAsStringList();
			else {
				log.error("Error in findQueryDataAsJson:" + result.getErrorMessage());
				return Arrays.asList(QUERY_ERROR + result.getErrorMessage());
			}
		} catch (final IOException e) {
			log.error("Error in findQueryByJSON ", e);
			return Arrays.asList(QUERY_ERROR + e.getLocalizedMessage());
		}
	}

	public String findQueryDataAsJson(String jsonQueryString, String... indexes) {
		log.info("findQueryDataAsJson with query:" + jsonQueryString);

		jsonQueryString = jsonQueryString.replaceAll("\\n", "");
		jsonQueryString = jsonQueryString.replaceAll("\\r", "");

		final List<String> list = new ArrayList<>(Arrays.asList(indexes));
		final Search search = new Search.Builder(jsonQueryString).addIndex(list.get(0)).build();
		SearchResult result;
		try {
			result = connector.getHttpClient().execute(search);
			if (result.isSucceeded())
				return util.parseElastiSearchResult(result.getJsonString(), false);
			else {
				log.error("Error in findQueryDataAsJson:" + result.getErrorMessage());
				return QUERY_ERROR + result.getErrorMessage();
			}

		} catch (final IOException e) {
			log.error("Error in findQueryByJSON ", e);
			return QUERY_ERROR + e.getLocalizedMessage();
		} catch (final JSONException e) {
			log.error("Error in findQueryByJSON PArsing result ", e);
			return QUERY_ERROR + e.getLocalizedMessage();
		}

	}

	public String findByIndex(String index, String type, String documentId) {
		log.info("findByIndex");
		try {
			final DocumentResult result = connector.getHttpClient()
					.execute(new Get.Builder(index, documentId).type(type).build());

			return result.getSourceAsString();
		} catch (final Exception e) {
			log.error("findByIndex", e);
			return "QueryError:" + e.getLocalizedMessage();
		}
	}

	public List<String> findAllByType(String ontology) {
		return findQueryData(ESBaseApi.QUERY_ALL, ontology);
	}

	public List<String> findAllByType(String ontology, int from, int limit) {
		String query = ESBaseApi.QUERY_ALL_SIZE_FROM_TO;
		query = query.replace(SIZE_STR, "" + limit);
		query = query.replace(FROM_STR, "" + from);

		return findQueryData(query, ontology);
	}

	public List<String> findAllByType(String ontology, String query, int from, int limit) {
		String querybase = ESBaseApi.QUERY_ALL_SIZE_FROM_TO_QUERY;

		querybase = querybase.replace(SIZE_STR, "" + limit);
		querybase = querybase.replace(FROM_STR, "" + from);
		querybase = querybase.replace("[QUERY]", "" + query);

		return findQueryData(querybase, ontology);
	}

	public List<String> findAllByType(String ontology, int limit) {
		String query = ESBaseApi.QUERY_ALL_SIZE;
		query = query.replace(SIZE_STR, "" + limit);

		return findQueryData(query, ontology);
	}

	public String findAllByTypeAsJson(String ontology, int limit) {
		String query = ESBaseApi.QUERY_ALL_SIZE;
		query = query.replace(SIZE_STR, "" + limit);

		return findQueryDataAsJson(query, ontology);
	}

	public String findAllByTypeAsJson(String ontology, int from, int limit) {
		String query = ESBaseApi.QUERY_ALL_SIZE_FROM_TO;
		query = query.replace(SIZE_STR, "" + limit);
		query = query.replace(FROM_STR, "" + from);

		return findQueryDataAsJson(query, ontology);
	}

}