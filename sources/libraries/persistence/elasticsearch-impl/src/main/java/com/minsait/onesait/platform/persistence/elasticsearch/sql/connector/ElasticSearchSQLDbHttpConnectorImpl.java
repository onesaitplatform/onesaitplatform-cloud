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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.minsait.onesait.platform.persistence.elasticsearch.ElasticSearchUtil;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Deprecated
public class ElasticSearchSQLDbHttpConnectorImpl implements ElasticSearchSQLDbHttpConnector {

	public static final String ACCEPT_TEXT_CSV = "text/csv; columnDelimiter=|&rowDelimiter=;&quoteChar='&escapeChar=\\\\";
	public static final String ACCEPT_APPLICATION_JSON = "application/json";
	static final String CONTENT_TYPE_HEADER = "Content-Type";

	private static final String BUILDING_ERROR = "Error building URL";

	@Value("${onesaitplatform.database.elasticsearch.sql.maxHttpConnections:10}")
	private int maxHttpConnections;
	@Value("${onesaitplatform.database.elasticsearch.sql.maxHttpConnectionsPerRoute:10}")
	private int maxHttpConnectionsPerRoute;
	@Value("${onesaitplatform.database.elasticsearch.sql.connectionTimeout.millis:10000}")
	private int connectionTimeout;
	@Value("${onesaitplatform.database.elasticsearch.sql.connector.http.endpoint:http://localhost:9300}")
	private String endpoint;

	@Autowired
	private ElasticSearchUtil util;

	private PoolingClientConnectionManager cm;
	private BasicHttpParams httpParams;

	@PostConstruct
	public void init() {
		/**
		 * Using the new way we obtain a java.io.EOFException: Unexpected end of ZLIB
		 * input stream It must be a bug in HttpClient
		 */

		cm = new PoolingClientConnectionManager();
		httpParams = new BasicHttpParams();
		cm.setMaxTotal(maxHttpConnections);
		cm.setDefaultMaxPerRoute(maxHttpConnectionsPerRoute);
		HttpConnectionParams.setConnectionTimeout(httpParams, connectionTimeout);
	}

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
		final String result = invokeSQLPlugin(url, ACCEPT_APPLICATION_JSON);

		try {
			res = util.parseElastiSearchResult(result, false);
		} catch (final JSONException e) {
			log.error("Error Parsing ES Result", e.getMessage());
			return result;
		}

		return res;
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
		final String result = invokeSQLPlugin(url, ACCEPT_TEXT_CSV);
		try {
			res = util.parseElastiSearchResult(result, false);
		} catch (final JSONException e) {
			log.error("Error Parsing ES Result", e.getMessage());
			return result;
		}

		return res;
	}

	private String invokeSQLPlugin(String endpoint, String accept) {
		HttpGet httpGet = null;
		HttpResponse httpResponse = null;
		String output = null;
		try (CloseableHttpClient httpClient = new DefaultHttpClient(cm, httpParams)) {
			httpClient.getConnectionManager().closeIdleConnections(0, TimeUnit.SECONDS);
			httpGet = createHttpGetRequest(endpoint, accept, null);

			if (httpGet != null) {
				try {
					log.info("Send message: to {}.", endpoint);
					httpResponse = httpClient.execute(httpGet);
					if (httpResponse != null && httpResponse.getStatusLine() != null) {
						final int httpStatusCode = httpResponse.getStatusLine().getStatusCode();
						if (httpStatusCode != 200) {
							log.warn("Error notifying message to endpoint: {}. HTTP status code {}.", endpoint,
									httpStatusCode);
						}
					} else {
						log.error("Error notifying message to endpoint: {}. Malformed HTTP response.", endpoint);
					}
					if (httpResponse != null) {
						final HttpEntity en = httpResponse.getEntity();
						final Header[] headers = httpResponse.getHeaders(CONTENT_TYPE_HEADER);
						output = EntityUtils.toString(en);
					}
					return output;

				} catch (final Exception e) {
					log.error("Error notifing message to endpoint: {}", endpoint, e);
					throw new DBPersistenceException(e);
				} finally {
					httpGet.releaseConnection();
				}

			}
		} catch (final Exception e) {
			log.error("Unable to send message: error detected while building POST request.", e);
		}
		log.warn("Cannot notify message: the HTTPPost request cannot be build.");
		throw new DBPersistenceException("Cannot notify message: the HTTPPost request cannot be build.");
	}

	private HttpGet createHttpGetRequest(String endpoint, String accept, String contentType) {
		HttpGet httpGet;
		try {
			httpGet = new HttpGet(new URI(endpoint));
			if (null != accept && accept.trim().length() > 0) {
				httpGet.setHeader("Accept", accept);
			}
			if (null != contentType && contentType.trim().length() > 0) {
				httpGet.setHeader(CONTENT_TYPE_HEADER, contentType);
			}
			httpGet.setHeader("Connection", "close");
		} catch (final URISyntaxException e1) {
			throw new IllegalArgumentException("The URI of the endpoint is invalid.");
		}
		return httpGet;
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

}
