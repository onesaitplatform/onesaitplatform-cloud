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
package com.minsait.onesait.platform.persistence.opensearch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.opensearch.client.opensearch._types.mapping.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.commons.model.DescribeColumnData;
import com.minsait.onesait.platform.commons.rtdbmaintainer.dto.ExportData;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.OntologyElastic;
import com.minsait.onesait.platform.config.repository.OntologyElasticRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.persistence.OpensearchEnabledCondition;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.interfaces.ManageDBRepository;
import com.minsait.onesait.platform.persistence.opensearch.api.OSBaseApi;
import com.minsait.onesait.platform.persistence.opensearch.api.OSCountService;
import com.minsait.onesait.platform.persistence.opensearch.api.OSDeleteService;
import com.minsait.onesait.platform.persistence.util.JSONPersistenceUtilsElasticSearch;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Component("OpenSearchManageDBRepository")
@Scope("prototype")
@Conditional(OpensearchEnabledCondition.class)
@Lazy
@Slf4j
public class OpenSearchManageDBRepository implements ManageDBRepository {

	private static final String NOT_IMPLEMENTED_ALREADY = "Not Implemented Already";
	private static final String ALLOWS_TEMPLATE_CONFIG = "allowsTemplateConfig";
	private static final String TTL_RETENTION_PERIOD = "ttlRetentionPeriod";
	private static final String TTL_PRIORITY = "ttlPriority";

	@Autowired
	private OSBaseApi connector;
	@Autowired
	private OSCountService oSCountService;

	@Autowired
	private OSDeleteService oSDeleteService;

	@Autowired
	private IntegrationResourcesService resourcesService;

	@Getter
	@Setter
	private String dumpPath;

	@Getter
	@Setter
	private String elasticDumpPath;

	@Getter
	@Setter
	private String openSearchEndpoint;

	@Autowired
	private OntologyRepository ontologyRepository;
	@Autowired
	private OntologyElasticRepository ontologyElasticRepository;

	@PostConstruct
	public void init() {
		final Map<String, Object> database = resourcesService.getGlobalConfiguration().getEnv().getDatabase();

		@SuppressWarnings("unchecked")
		final Map<String, Object> opensearch = (Map<String, Object>) database.get("opensearch");

		@SuppressWarnings("unchecked")
		final Map<String, Object> sql = (Map<String, Object>) opensearch.get("sql");

		@SuppressWarnings("unchecked")
		final Map<String, Object> dump = (Map<String, Object>) opensearch.get("dump");

		openSearchEndpoint = (String) sql.get("endpoint");

		dumpPath = (String) dump.get("path");
		elasticDumpPath = (String) dump.get("elasticDumpCmd");
	}

	@Override
	public Map<String, Boolean> getStatusDatabase() {
		log.error("Error implementing");
		throw new DBPersistenceException(NOT_IMPLEMENTED_ALREADY);
	}

	@Override
	public String createTable4Ontology(final String ontology, final String schema, final Map<String, String> config) {
		try {
			// Delete custom Policy if there is one
			connector.deleteTTLPolicy(ontology);
			// Create custom Policy if there is one and apply it to the index name as
			// ism_template
			if (config != null && config.get(TTL_RETENTION_PERIOD) != null
					&& config.get(TTL_RETENTION_PERIOD).isEmpty()) {
				connector.createTTLPolicy(ontology, ontology, config.get(TTL_RETENTION_PERIOD),
						Integer.parseInt(config.get(TTL_PRIORITY)));
			}
			// Create index
			if (JSONPersistenceUtilsElasticSearch.isJSONSchema(schema)) {

				final Map<String, Property> mapping = JSONPersistenceUtilsElasticSearch
						.getOpenSearchSchemaFromJSONSchema(schema);
				try {
					if (config != null && config.get(ALLOWS_TEMPLATE_CONFIG) != null
							&& !config.get(ALLOWS_TEMPLATE_CONFIG).isEmpty()
							&& Boolean.parseBoolean(config.get(ALLOWS_TEMPLATE_CONFIG))) {
						log.info("Creating ElasticSearch template for ontology: {}", ontology);
						final boolean res = connector.createTemplate(ontology.toLowerCase(), mapping, config);
						log.info("Template result : {} ", res);
					} else {
						log.info("Creating ElasticSearch index for ontology: {}", ontology);
						final boolean res = connector.createIndex(ontology.toLowerCase(), mapping, config);
						log.info("Index result : {} ", res);
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
		final String[] result = connector.getIndexes();
		if (result != null) {
			list.addAll(Arrays.asList(result));
		}
		return list;

	}

	@Override
	public List<String> getListOfTables4Ontology(String ontology) {
		ontology = ontology.toLowerCase();
		final List<String> list = new ArrayList<>();
		final String[] result = connector.getIndexes();
		final List<String> ontologies = Arrays.asList(result);
		if (result != null && ontologies.indexOf(ontology) != .1) {
			list.add(ontology);
		}
		return list;
	}

	@Override
	public void removeTable4Ontology(String ontology) {
		ontology = ontology.toLowerCase();
		final Ontology ontol = ontologyRepository.findByIdentification(ontology);
		final OntologyElastic elasticOntol = ontologyElasticRepository.findByOntologyId(ontol);
		// Check if ontology is a template
		if (elasticOntol != null && elasticOntol.getTemplateConfig() == true) {
			connector.deleteTemplate(ontology);
		} else {
			connector.deleteIndex(ontology);
		}
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

		if (pathToFile.equals("default")) {
			path = dumpPath + ontology.toLowerCase() + format.format(new Date()) + ".json";
		} else {
			path = pathToFile;
		}
		if (oSCountService.getQueryCount(queryElastic, ontology.toLowerCase()) > 0) {
			final ProcessBuilder pb;

			pb = new ProcessBuilder("elasticdump", "--input=" + openSearchEndpoint + "/" + ontology.toLowerCase(),
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
				log.info("Cmd elasticdump output: {}", builder.toString());
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
		return oSDeleteService.deleteByQuery(ontology.toLowerCase(), query).getCount();
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

	@Override
	public void createTTLIndex(String ontology, String attribute, Long seconds) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_ALREADY);

	}

}
