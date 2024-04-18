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
package com.minsait.onesait.platform.persistence.mindsdb;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.persistence.interfaces.QueryAsTextDBRepository;
import com.minsait.onesait.platform.persistence.mindsdb.http.MindsDBHTTPClient;
import com.minsait.onesait.platform.persistence.mindsdb.model.MindsDBQuery;

import lombok.extern.slf4j.Slf4j;

@Component("MindsDBQueryAsTextDBRepository")
@Slf4j
public class MindsDBQueryAsTextDBRepository implements QueryAsTextDBRepository {

	private static final String MINDSDB = "mindsdb.";
	@Autowired
	private MindsDBHTTPClient mindsDBHTTPClient;
	@Autowired
	private OntologyRepository ontologyRepository;
	public static final ObjectMapper mapper = new ObjectMapper();
	private static final String SELECT_PATTERN = "(?i)FROM[ ]+([A-Za-z_-]+)[ ]+WHERE";

	@Override
	public String queryNativeAsJson(String ontology, String query, int offset, int limit) {
		return queryNativeAsJson(ontology, query);
	}

	@Override
	public String queryNativeAsJson(String ontology, String query) {
		return mindsDBHTTPClient.sendQuery(new MindsDBQuery(getQueryObject(ontology, query)));
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset) {
		return queryNativeAsJson(ontology, query);
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset, int limit) {
		return queryNativeAsJson(ontology, query);
	}

	private String getQueryObject(String ontology, String query) {

		return getQuery(ontology, query);

	}

	private String getQuery(String ontology, String query) {
		final Ontology o = ontologyRepository.findByIdentification(ontology);
		final Pattern p = Pattern.compile(SELECT_PATTERN);
		final Matcher m = p.matcher(query);
		if (m.find()) {
			query = new StringBuilder(query).replace(m.start(1), m.end(1), MINDSDB + o.getOntologyAI().getPredictor())
					.toString();
		}

		return query;
	}

}
