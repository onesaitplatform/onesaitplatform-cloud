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
package com.minsait.onesait.platform.persistence.elasticsearch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.commons.model.DescribeColumnData;
import com.minsait.onesait.platform.commons.rtdbmaintainer.dto.ExportData;
import com.minsait.onesait.platform.persistence.elasticsearch.api.ESBaseApi;
import com.minsait.onesait.platform.persistence.elasticsearch.api.ESCountService;
import com.minsait.onesait.platform.persistence.elasticsearch.api.ESDeleteService;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.interfaces.ManageDBRepository;
import com.minsait.onesait.platform.persistence.util.JSONPersistenceUtilsElasticSearch;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Component("ElasticSearchManageDBRepository")
@Scope("prototype")
@Lazy
@Slf4j
public class ElasticSearchManageDBRepository implements ManageDBRepository {

	private static final String NOT_IMPLEMENTED_ALREADY = "Not Implemented Already";

	@Autowired
	private ESBaseApi connector;
	@Autowired
	private ESCountService eSCountService;

	@Autowired
	private ESDeleteService eSDeleteService;

	@Value("${onesaitplatform.database.elasticsearch.dump.path:null}")
	@Getter
	@Setter
	private String dumpPath;

	@Value("${onesaitplatform.database.elasticsearch.elasticdump.path:null}")
	@Getter
	@Setter
	private String elasticDumpPath;

	@Value("${onesaitplatform.database.elasticsearch.sql.connector.http.endpoint:http://localhost:9300}")
	@Getter
	@Setter
	private String elasticSearchEndpoint;

	private String createTestIndex(String index) {
		final String res = connector.createIndex(index);
		log.debug("ElasticSearchManageDBRepository createTestIndex {}, res: ",index, res);
		return res;
	}

	@Override
	public Map<String, Boolean> getStatusDatabase() {
		log.error("Error implementing");
		throw new DBPersistenceException(NOT_IMPLEMENTED_ALREADY);
	}

	@Override
	public String createTable4Ontology(final String ontology, final String schema, final Map<String, String> config) {
		try {
			if (JSONPersistenceUtilsElasticSearch.isJSONSchema(schema)) {

				String elasticIndex = JSONPersistenceUtilsElasticSearch.getElasticSearchSchemaFromJSONSchema(schema);
				try {
					final String res = connector.createIndex(ontology.toLowerCase());
					log.info("Index result : {} ", res);

					try {
						if (!elasticIndex.isEmpty() && (!connector.createType(ontology.toLowerCase(),
								ontology.toLowerCase(), elasticIndex))) {
							log.error("Error mapping");
							throw new DBPersistenceException("Error mapping type.");
						}
					} catch (final Exception e) {
						connector.deleteIndex(ontology.toLowerCase());
						log.error("Error mapping type: " + e.getMessage(), e);
						throw new DBPersistenceException("Error mapping type. Message: " + e.getMessage(), e);
					}

				} catch (final Exception e) {
					log.error("Error creating index: " + e.getMessage(), e);
					throw new DBPersistenceException("Error creating index. Message: " + e.getMessage(), e);
				}

			} else {
				log.error("Json schema is not valid. It does not contains $schema field.");
				throw new DBPersistenceException("Json schema is not valid. It does not contains $schema field.");
			}

		} catch (final Exception e) {
			log.error("Could not generate elasticSearch index. Message: " + e.getMessage(), e);
			throw new DBPersistenceException("Could not generate Elastic Search index. Message: " + e.getMessage(), e);
		}

		return ontology.toLowerCase();
	}

	@Override
	public List<String> getListOfTables() {
		final List<String> list = new ArrayList<>();
		final String result = connector.getIndexes();
		if (result != null)
			list.add(result);
		return list;

	}

	@Override
	public List<String> getListOfTables4Ontology(String ontology) {
		ontology = ontology.toLowerCase();
		final List<String> list = new ArrayList<>();
		final String result = connector.getIndexes();
		if (result != null && result.indexOf(ontology) != .1) {
			list.add(ontology);
		}
		return list;
	}

	@Override
	public void removeTable4Ontology(String ontology) {
		ontology = ontology.toLowerCase();
		eSDeleteService.deleteAll(ontology, ontology);

	}

	@Override
	public void createIndex(String ontology, String attribute) {
		log.error(NOT_IMPLEMENTED_ALREADY);
		throw new DBPersistenceException(NOT_IMPLEMENTED_ALREADY);

	}

	@Override
	public void createIndex(String ontology, String nameIndex, String attribute) {
		log.error(NOT_IMPLEMENTED_ALREADY);
		throw new DBPersistenceException(NOT_IMPLEMENTED_ALREADY);

	}

	@Override
	public void createIndex(String sentence) {
		log.error(NOT_IMPLEMENTED_ALREADY);
		throw new DBPersistenceException(NOT_IMPLEMENTED_ALREADY);
	}

	@Override
	public void dropIndex(String ontology, String indexName) {
		log.error(NOT_IMPLEMENTED_ALREADY);
		throw new DBPersistenceException(NOT_IMPLEMENTED_ALREADY);

	}

	@Override
	public List<String> getListIndexes(String ontology) {
		log.error(NOT_IMPLEMENTED_ALREADY);
		throw new DBPersistenceException(NOT_IMPLEMENTED_ALREADY);

	}

	@Override
	public String getIndexes(String ontology) {
		log.error(NOT_IMPLEMENTED_ALREADY);
		throw new DBPersistenceException(NOT_IMPLEMENTED_ALREADY);
	}

	@Override
	public void validateIndexes(String ontology, String schema) {
		log.error(NOT_IMPLEMENTED_ALREADY);
		throw new DBPersistenceException(NOT_IMPLEMENTED_ALREADY);

	}

	@Override
	public ExportData exportToJson(String ontology, long startDateMillis, String pathToFile) {
		final SimpleDateFormat format = new SimpleDateFormat("yyyy-dd-MM-hh-mm");
		final String query = "--searchBody {\\\"query\\\":{\\\"range\\\":{\\\"contextData.timestampMillis\\\":{\\\"lte\\\":"
				+ startDateMillis + "}}}}";
		final String queryElastic = "{\r\n" + "\"query\" : {\r\n"
				+ "    \"range\" : {\r\n  \"contextData.timestampMillis\" : {\r\n \"lte\" : " + startDateMillis
				+ " \r\n} \r\n} \r\n" + "  }\r\n" + "}";
		final String path;

		if (pathToFile.equals("default"))
			path = dumpPath + ontology.toLowerCase() + format.format(new Date()) + ".json";
		else
			path = pathToFile;
		if (eSCountService.getQueryCount(queryElastic, ontology.toLowerCase()) > 0) {
			final ProcessBuilder pb;

			pb = new ProcessBuilder("elasticdump", "--input=" + elasticSearchEndpoint + "/" + ontology.toLowerCase(),
					"--output=" + path, query, "--delete=true");
			try {
				final Process p = pb.start();
				final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
				final StringBuilder builder = new StringBuilder();
				String line = null;
				p.waitFor();
				while ((line = reader.readLine()) != null) {
					builder.append(line);
					builder.append(System.getProperty("line.separator"));
				}
				builder.toString();
				log.info("Cmd elasticdump output: {}",builder.toString());
				log.info("Created export file for ontology {} at {}", ontology, path);
			} catch (IOException | InterruptedException e) {
				log.error("Could not execute command {}", e.getMessage());
				throw new DBPersistenceException("Could not execute command: " + pb.command().toString() + e);
			}
		}
		return ExportData.builder().filterQuery(queryElastic).path(path).build();
	}

	@Override
	public long deleteAfterExport(String ontology, String query) {
		return eSDeleteService.deleteByQuery(ontology.toLowerCase(), ontology.toLowerCase(), query, false).getCount();
	}

	@Override
	public List<DescribeColumnData> describeTable(String name) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_ALREADY);
	}

	@Override
	public Map<String, String> getAdditionalDBConfig(String ontology) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_ALREADY);
	}

	@Override
	public String updateTable4Ontology(String identification, String jsonSchema, Map<String, String> config) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_ALREADY);
	}

}
