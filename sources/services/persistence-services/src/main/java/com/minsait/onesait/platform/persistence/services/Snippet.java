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
package com.minsait.onesait.platform.persistence.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Snippet {

	public static final String ACCEPT_TEXT_CSV = "text/csv; columnDelimiter=|&rowDelimiter=;&quoteChar='&escapeChar=\\\\";
	public static final String ACCEPT_APPLICATION_JSON = "application/json";

	public static void main(String[] args) throws IOException {

		PoolingHttpClientConnectionManager cm;
		HttpClient client;
		RequestConfig config;

		String url = "http://localhost:18200/query/fs/onesaitplatform_rtdb/?q=select+*+from+ontologytest1525105868587";

		cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(10);
		cm.setDefaultMaxPerRoute(10);

		config = RequestConfig.custom().setRedirectsEnabled(true).setConnectTimeout(10000).build();

		client = HttpClientBuilder.create().disableContentCompression().setConnectionManager(cm).build();
		HttpGet request = new HttpGet(url);
		request.setConfig(config);
		request.setHeader(HttpHeaders.ACCEPT, ACCEPT_TEXT_CSV);
		request.setHeader(HttpHeaders.CONTENT_TYPE, ACCEPT_APPLICATION_JSON);

		// add request header

		HttpResponse response = client.execute(request);
		
		log.info("Response Code : " + response.getStatusLine().getStatusCode());
		response.getEntity();
		
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
	}

}
