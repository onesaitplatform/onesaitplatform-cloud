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
package com.minsait.onesait.platform.persistence.mongodb.quasar.connector;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataService;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.http.BaseHttpClient;
import com.minsait.onesait.platform.persistence.mongodb.audit.aop.QuasarAuditable;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import lombok.extern.slf4j.Slf4j;

@Component
@Lazy
@EnableAspectJAutoProxy(exposeProxy = true)
@Slf4j
public class QuasarMongoDBbHttpImpl implements QuasarMongoDBbHttpConnector {

	@Value("${onesaitplatform.database.mongodb.quasar.connector.http.endpoint:http://localhost:18200/query/fs/}")
	private String quasarEndpoint;
	@Value("${onesaitplatform.database.mongodb.database:onesaitplatform_rtdb}")
	private String database;
	@Autowired
	private IntegrationResourcesService resourcesService;
	@Autowired
	private OntologyRepository repository;
	@Autowired
	private OntologyDataService ontologyDataService;
	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private BaseHttpClient quasarHttpClient;

	private static final String CONTEXT_DATA = "contextData";
	private static final String PATH_TO_COMPILED_QUERY = "physicalPlan";
	private static final String BUILDING_ERROR = "Error building URL";

	private boolean compileQueries() {
		try {
			return ((Boolean) resourcesService.getGlobalConfiguration().getEnv().getDatabase()
					.get("mongodb-quasar-compile")).booleanValue();
		} catch (final RuntimeException e) {
			return true;
		}

	}

	private int getMaxRegisters() {
		return ((Integer) resourcesService.getGlobalConfiguration().getEnv().getDatabase().get("queries-limit"))
				.intValue();
	}

	@Override
	@QuasarAuditable
	public String queryAsJson(String collection, String query, int offset, int limit) {
		String url;
		try {
			if (query.contains("*"))
				query = replaceAsterisk(collection, query);
			url = buildUrl(query, offset, limit);
		} catch (final UnsupportedEncodingException e) {
			log.error(BUILDING_ERROR, e);
			throw new DBPersistenceException(BUILDING_ERROR, e);
		}
		if (compileQueries())
			((QuasarMongoDBbHttpConnector) AopContext.currentProxy()).compileQueryAsJson(collection, query, offset);
		final String result = quasarHttpClient.invokeSQLPlugin(url, MediaType.APPLICATION_JSON_VALUE, null);
		return formatResult(result);
	}

	@Override
	public String queryAsTable(String query, int offset, int limit) {
		String url;
		try {
			url = buildUrl(query, offset, limit);
		} catch (final UnsupportedEncodingException e) {
			log.error(BUILDING_ERROR, e);
			throw new DBPersistenceException(BUILDING_ERROR, e);
		}
		return quasarHttpClient.invokeSQLPlugin(url, BaseHttpClient.ACCEPT_TEXT_CSV, null);

	}

	/**
	 * FORMAT QUERY:
	 * /query/fs/[path]?q=[query]&offset=[offset]&limit=[limit]&var.[foo]=[value]
	 *
	 * @param query
	 * @param offset
	 * @param limit
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private String buildUrl(String query, int offset, int limit) throws UnsupportedEncodingException {
		if (query.contains("_id"))
			query = replaceObjectId(query);
		query = query.replace("'", "\"");
		String params = "q=" + URLEncoder.encode(query, "UTF-8");
		if (offset > 0) {
			params += "&offset=" + offset;
		}
		if (limit > 0) {
			params += "&limit=" + limit;
		}

		return quasarEndpoint + database + "/?" + params;
	}

	private String replaceObjectId(String query) {
		final String oid = "\\w*(?<![A-Za-z`])_id";
		final String newOid = "`_id`";
		log.trace("input query with _id {}", query);
		query = query.replaceAll(oid, newOid);
		log.trace("replaced query with _id {}", query);
		return query;
	}

	private String replaceAsterisk(String collection, String query) {
		final Ontology ontology = repository.findByIdentification(collection);
		JsonNode schema;
		try {
			schema = mapper.readTree(ontology.getJsonSchema());
			if (!ontologyDataService.refJsonSchema(schema).equals("")) {
				log.debug("Modifying query that contains * {}:", query);
				final String parentNode = schema.at("/required/0").asText();
				if (parentNode != null && parentNode.trim().length() > 0) {
					query = query.replaceAll("count\\(.*?\\*.*?\\)", "count\\(" + parentNode + "\\)");
					query = query.replaceAll("\\.\\*", "");
					final Pattern pattern = Pattern.compile("(select.*?\\*.*?from)");
					final Matcher matcher = pattern.matcher(query);
					while (matcher.find()) {
						final String found = matcher.group(1);
						final String predicateQuery = query.substring(found.length(), query.length()).trim();
						String foundReplace = "";
						final Pattern p2 = Pattern.compile("(.*?\\*.*?)");
						final Matcher m2 = p2.matcher(found);
						while (m2.find()) {
							final String f2 = m2.group();
							if (!(f2.contains("{*") || f2.contains("[*"))) {

								foundReplace = f2.replaceAll("\\*",
										parentNode + " as " + parentNode + " ," + CONTEXT_DATA);
								query = query.replace(f2, foundReplace);

							}

						}
						if (predicateQuery.startsWith(collection)) {
							final Pattern p3 = Pattern.compile("(" + collection + ".*?[as|AS].*?)");
							final Matcher m3 = p3.matcher(predicateQuery);
							if (!m3.matches()) {
								query = query.substring(0, foundReplace.length()) + " from "
										+ predicateQuery.replace(collection, collection + " as c");
							}
						}

					}
				}
				log.debug("Modified query that contains * {}:", query);
			} else {
				log.error("Query for ontology {} contains * please indicate explicitly the fields you want to query",
						collection);
				throw new DBPersistenceException("Query for ontology " + collection
						+ " contains *, please indicate explicitly the fields you want to query");
			}
			return query;
		} catch (final Exception e) {
			return query;
		}

	}

	private String handleCompileQuery(String url, String query, String collection) {
		try {
			final String compileResult = quasarHttpClient.invokeSQLPlugin(url.replace("/query/", "/compile/"),
					MediaType.APPLICATION_JSON_VALUE, null);
			final JsonNode compile = mapper.readTree(compileResult);
			String nativeQuery = compile.path(PATH_TO_COMPILED_QUERY).asText();
			if (!StringUtils.isEmpty(nativeQuery)) {
				nativeQuery = nativeQuery.replaceAll("\\n", "");
				log.info("Quasar is about to execute native query: {}", nativeQuery);
				((ObjectNode) compile).put(PATH_TO_COMPILED_QUERY, nativeQuery);
			} else {
				log.info("Quasar didn't compile the query: {}", compileResult);
			}

			return mapper.writeValueAsString(compile);

		} catch (final IOException e) {
			throw new DBPersistenceException("Could not compile query");
		}

	}

	private String formatResult(String result) {

		try {
			final ArrayNode array = (ArrayNode) mapper.readTree(result);
			final ArrayNode arrayResult = mapper.createArrayNode();
			array.forEach(n -> {
				final int size = n.size();
				n.fields().forEachRemaining(e -> {
					if (("1".equals(e.getKey()) || "0".equals(e.getKey())) && size <= 2)
						arrayResult.add(e.getValue());
				});

			});
			if (arrayResult.size() == 0)
				return result;
			return mapper.writeValueAsString(arrayResult);
		} catch (final Exception e) {
			log.error("Quasar result is not an array");
			return result;
		}

	}

	@Override
	@QuasarAuditable
	public String compileQueryAsJson(String collection, String query, int offset) {
		String url;
		try {
			if (query.contains("*"))
				query = replaceAsterisk(collection, query);
			url = buildUrl(query, offset, getMaxRegisters());
		} catch (final UnsupportedEncodingException e) {
			log.error(BUILDING_ERROR, e);
			throw new DBPersistenceException(BUILDING_ERROR, e);
		}
		return handleCompileQuery(url, query, collection);

	}
}
