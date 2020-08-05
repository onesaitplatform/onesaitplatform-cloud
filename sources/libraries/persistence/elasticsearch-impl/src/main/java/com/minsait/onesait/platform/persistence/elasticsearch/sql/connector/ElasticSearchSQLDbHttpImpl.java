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
import java.util.Map;

import javax.annotation.PostConstruct;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.persistence.ElasticsearchEnabledCondition;
import com.minsait.onesait.platform.persistence.elasticsearch.ElasticSearchUtil;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.http.BaseHttpClient;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import lombok.extern.slf4j.Slf4j;

@Component
@Lazy
@Conditional(ElasticsearchEnabledCondition.class)
@Slf4j
public class ElasticSearchSQLDbHttpImpl implements ElasticSearchSQLDbHttpConnector {

	private static final String BUILDING_ERROR = "Error building URL";
	private static final String PARSING_ERROR = "Error Parsing Result from query:";


	private String endpoint;

	@Autowired
	private BaseHttpClient httpClient;
	
	@Autowired
    private IntegrationResourcesService resourcesService;
	
	@PostConstruct
    public void init() {
        Map<String, Object> database = resourcesService.getGlobalConfiguration().getEnv().getDatabase();
        
        @SuppressWarnings("unchecked")
        Map<String, Object>  elasticsearch = (Map<String, Object>) database.get("elasticsearch");
        
        @SuppressWarnings("unchecked")
        Map<String, Object>  sql = (Map<String, Object>) elasticsearch.get("sql");
        
        endpoint = (String) sql.get("endpoint");
    }

	@Override
	public String queryAsJson(String query, int scrollInit, int scrollEnd) {
		String url = buildUrl();
		String body = query;
		String res = null;
		try {
			if (scrollInit <= 0)
				scrollInit = 0;

			if (scrollInit > 0 && scrollEnd > 0) {
				String result = body.replaceFirst("select ", "");
				result = result.replaceFirst("SELECT ", "");

				final String scroll = "SELECT /*! USE_SCROLL(" + scrollInit + "," + scrollEnd + ")*/ ";
				body = scroll + result;
				
			} else if (scrollInit == 0 && scrollEnd > 0) {
			    body = buildQuery(query, 0, scrollEnd, false);
			}

		} catch (final UnsupportedEncodingException e) {
			log.error(BUILDING_ERROR, e);
			throw new DBPersistenceException(BUILDING_ERROR, e);
		}
		StringBuilder bodyBuilder = new StringBuilder();
		bodyBuilder.append("{\"sql\": \"");
		bodyBuilder.append(body);
		bodyBuilder.append("\"}");
		final String result = httpClient.invokeSQLPlugin(url, bodyBuilder.toString(), HttpMethod.POST, BaseHttpClient.ACCEPT_TEXT_CSV, null);
		try {
			res = ElasticSearchUtil.parseElastiSearchResult("" + result, queryHasSelectId(query));
			return res;
		} catch (final JSONException e) {
			log.error(PARSING_ERROR + query, e);
			throw new DBPersistenceException(PARSING_ERROR + query, e);
		}
	}

	@Override
	public String queryAsJson(String query, int limit) {
		String url;
		String body;
		String res = null;
		try {
		    url = buildUrl();
		    body = buildQuery(query, 0, limit, false);
		} catch (final UnsupportedEncodingException e) {
			log.error(BUILDING_ERROR, e);
			throw new DBPersistenceException(BUILDING_ERROR, e);
		}
		final String result = httpClient.invokeSQLPlugin(url, body, HttpMethod.POST, BaseHttpClient.ACCEPT_TEXT_CSV, null);
		try {
			res = ElasticSearchUtil.parseElastiSearchResult(result, queryHasSelectId(query));
			return res;
		} catch (final JSONException e) {
			log.error(PARSING_ERROR + query, e);
			throw new DBPersistenceException(PARSING_ERROR + query, e);
		}
	}

	private String buildQuery(String query, int offset, int limit, boolean encode) throws UnsupportedEncodingException {
		String params = "" + query;
		if (offset > 0) {
			params += " OFFSET " + offset;
		}
		if (limit > 0) {
			params += " limit " + limit;
		}

		if (encode) {
			params = URLEncoder.encode(params, "UTF-8");
		}
		//return (endpoint + "/_nlpcn/sql?sql=" + params);
		return params;
	}

	private String buildUrl() {
		return endpoint + "/_nlpcn/sql";
	}

	private boolean queryHasSelectId(String query) {
		return (query.substring(0, query.toLowerCase().indexOf("from")).toLowerCase().contains("_id"));
	}

}
