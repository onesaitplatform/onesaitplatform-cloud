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
package com.minsait.onesait.platform.persistence.elasticsearch.sql.connector;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.persistence.elasticsearch.ElasticSearchUtil;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.http.BaseHttpClient;

import lombok.extern.slf4j.Slf4j;

@Component
@Lazy
@Slf4j
public class ElasticSearchSQLDbHttpImpl implements ElasticSearchSQLDbHttpConnector {

	private static final String BUILDING_ERROR = "Error building URL";
	private static final String PARSING_ERROR = "Error Parsing Result from query:";

	@Value("${onesaitplatform.database.elasticsearch.sql.maxHttpConnections:10}")
	private int maxHttpConnections;
	@Value("${onesaitplatform.database.elasticsearch.sql.maxHttpConnectionsPerRoute:10}")
	private int maxHttpConnectionsPerRoute;

	@Value("${onesaitplatform.database.elasticsearch.sql.connector.http.endpoint:http://localhost:9300}")
	private String endpoint;

	@Autowired
	private ElasticSearchUtil util;

	@Autowired
	private BaseHttpClient httpClient;

	@Override
	public String queryAsJson(String query, int scrollInit, int scrollEnd) {
		String url = null;
		String res = null;
		try {
			if (scrollInit <= 0)
				scrollInit = 0;

			if (scrollInit > 0 && scrollEnd > 0) {
				String result = query.replaceFirst("select ", "");
				result = result.replaceFirst("SELECT ", "");

				final String scroll = "SELECT /*! USE_SCROLL(" + scrollInit + "," + scrollEnd + ")*/ ";
				query = scroll + result;

				url = buildUrl(query);
			} else if (scrollInit == 0 && scrollEnd > 0) {
				url = buildUrl(query, 0, scrollEnd, true);
			}

		} catch (final UnsupportedEncodingException e) {
			log.error(BUILDING_ERROR, e);
			throw new DBPersistenceException(BUILDING_ERROR, e);
		}
		final String result = httpClient.invokeSQLPlugin(url, BaseHttpClient.ACCEPT_TEXT_CSV, null);
		try {
			res = util.parseElastiSearchResult("" + result, queryHasSelectId(query));
			return res;
		} catch (final JSONException e) {
			log.error(PARSING_ERROR + query, e);
			throw new DBPersistenceException(PARSING_ERROR + query, e);
		}
	}

	@Override
	public String queryAsJson(String query, int limit) {
		String url;
		String res = null;
		try {
			url = buildUrl(query, 0, limit, true);
		} catch (final UnsupportedEncodingException e) {
			log.error(BUILDING_ERROR, e);
			throw new DBPersistenceException(BUILDING_ERROR, e);
		}
		final String result = httpClient.invokeSQLPlugin(url, BaseHttpClient.ACCEPT_TEXT_CSV, null);
		try {
			res = util.parseElastiSearchResult(result, queryHasSelectId(query));
			return res;
		} catch (final JSONException e) {
			log.error(PARSING_ERROR + query, e);
			throw new DBPersistenceException(PARSING_ERROR + query, e);
		}
	}

	private String buildUrl(String query, int offset, int limit, boolean encode) throws UnsupportedEncodingException {
		String params = "" + query;
		if (offset > 0) {
			params += " OFFSET " + offset;
		}
		if (limit > 0) {
			params += " limit " + limit;
		}

		if (encode)
			params = URLEncoder.encode(params, "UTF-8");
		return (endpoint + "/_sql?sql=" + params);
	}

	private String buildUrl(String query) throws UnsupportedEncodingException {
		return (endpoint + "/_sql?sql=" + URLEncoder.encode(query, "UTF-8"));
	}

	private boolean queryHasSelectId(String query) {
		return (query.substring(0, query.toLowerCase().indexOf("from")).toLowerCase().contains("_id"));
	}

}
