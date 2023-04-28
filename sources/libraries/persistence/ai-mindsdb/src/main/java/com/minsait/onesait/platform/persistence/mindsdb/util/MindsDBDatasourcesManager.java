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
package com.minsait.onesait.platform.persistence.mindsdb.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.minsait.onesait.platform.persistence.mindsdb.http.MindsDBHTTPClient;
import com.minsait.onesait.platform.persistence.mindsdb.model.MindsDBDatasource;
import com.minsait.onesait.platform.persistence.mindsdb.model.MindsDBQuery;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MindsDBDatasourcesManager {

	@Autowired
	private MindsDBHTTPClient mindsDBHTTPClient;

	private static final String MINDSDB = "mindsdb";
	private static final String SELECT_DATASOURCES = "SELECT * FROM " + MINDSDB + ".datasources";
	private static final ObjectMapper mapper = new ObjectMapper();

	public void createDatasource(String connName, String engine, String host, String port, String username,
			String password) {
		if (host!=null && port!=null && !datasourceExists(connName)) {
			try {
				doCreateDatasource(connName, engine, host, port, username, password);
			} catch (final Exception e) {
				log.error("Could not create mindsdb datasource connName: {}, engine: {}, host: {}, port: {}", connName,
						engine, host, port);
			}
		}
	}

	private void doCreateDatasource(String connName, String engine, String host, String port, String username,
			String password) throws Exception {
		log.info("Creating datasource for MindsDB with name: {}, engine: {}, host: {}, port:{}", connName, engine, host, port);
		final MindsDBDatasource ds = MindsDBDatasource.builder().host(host).port(port).user(username).password(password)
				.build();
		mindsDBHTTPClient.sendQuery(new MindsDBQuery("CREATE DATABASE " + connName + " WITH ENGINE = \"" + engine
				+ "\", PARAMETERS = " + mapper.writeValueAsString(ds)));
	}

	public boolean datasourceExists(String name) {
		boolean exists = false;
		try {
			final ArrayNode datasources = mindsDBHTTPClient.sendQueryJson(new MindsDBQuery(SELECT_DATASOURCES));
			if (!datasources.isEmpty()) {
				for (final JsonNode ds : datasources) {
					if (name.equals(ds.get("name").asText())) {
						exists = true;
					}
				}

			}
		} catch (final Exception e) {
			log.warn("Could not read MindsDB datasource: {}", e.getMessage());
			//DO NOT CREATE IF ERROR, MAY NOT BE ACTIVE
			return true;
		}
		return exists;
	}
}
