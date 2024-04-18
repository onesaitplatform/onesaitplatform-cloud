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
package com.minsait.onesait.platform.persistence.mongodb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.commons.model.DescribeColumnData;
import com.minsait.onesait.platform.commons.rtdbmaintainer.dto.ExportData;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.interfaces.ManageDBRepository;
import com.minsait.onesait.platform.persistence.mongodb.index.MongoDbIndex;
import com.minsait.onesait.platform.persistence.mongodb.template.MongoDbTemplate;
import com.minsait.onesait.platform.persistence.util.JSONPersistenceUtilsMongo;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Component("MongoManageDBRepository")
@Scope("prototype")
@Lazy
@Slf4j
public class MongoNativeManageDBRepository implements ManageDBRepository {

	@Autowired
	private UtilMongoDB util;

	@Autowired
	private MongoDbTemplate mongoDbConnector;

	private static final String CREATE_TABLE_ONTOLOGY = "createTable4Ontology";
	private static final String CREATE_INDEX = "createIndex";
	private static final String GET_INDEX = "getIndexes";
	private static final String VALIDATE_INDEX = "validateIndexes";
	private static final String NOT_IMPLEMENTED_ALREADY = "Not Implemented Already";

	@Autowired
	private IntegrationResourcesService resourcesService;

	@Getter
	@Setter
	private String database;

	@Value("${onesaitplatform.database.mongodb.export.path:#{null}}")
	@Getter
	@Setter
	private String exportPath;

	@Value("${onesaitplatform.database.mongodb.mongoexport.path:#{null}}")
	@Getter
	@Setter
	private String mongoExportPath;

	protected ObjectMapper objectMapper;

	@PostConstruct
	public void init() {
		objectMapper = new ObjectMapper();
		objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		database = resourcesService.getGlobalConfiguration().getEnv().getDatabase().get("mongodb-database").toString();
	}

	@Override
	public String createTable4Ontology(String collection, String schema, Map<String, String> config) {
		log.debug(CREATE_TABLE_ONTOLOGY, collection, schema);
		try {
			if (collection == null || schema == null)
				throw new DBPersistenceException(
						"DAOMongoDBImpl needs a collection and a schema to create a collection into the database");

			/**
			 * Sino existe la collection la crea
			 */
			if (!mongoDbConnector.collectionExists(database, collection)) {
				mongoDbConnector.createCollection(database, collection);
			} else {

				/**
				 * Permitir creación solamente si no tiene elementos: usa la que existe sin
				 * registros
				 */
				final long countCollection = mongoDbConnector.count(database, collection, "{}");
				if (countCollection > 0) {
					log.error(CREATE_TABLE_ONTOLOGY, "The collection already exists and has records", collection);
					throw new DBPersistenceException("The collection already exists and has records");
				}
			}

			// validar que tiene geometry y crear indice en ese caso.
			validateIndexes(collection, schema);
			return collection;
		} catch (final DBPersistenceException e) {
			log.error(CREATE_TABLE_ONTOLOGY, e, collection);
			throw new DBPersistenceException(e);
		}
	}

	@Override
	public List<String> getListOfTables() {
		return mongoDbConnector.getCollectionNames(database);
	}

	@Override
	public List<String> getListOfTables4Ontology(String ontology) {
		final List<String> collections = getListOfTables();
		final ArrayList<String> result = new ArrayList<>();
		for (final String collection : collections) {
			if (collection.startsWith(ontology))
				result.add(collection);
		}
		return result;
	}

	@Override
	public void removeTable4Ontology(String ontology) {
		try {
			mongoDbConnector.dropCollection(database, ontology);
		} catch (final javax.persistence.PersistenceException e) {
			log.error("removeTable4Ontology" + e.getMessage());
			throw new DBPersistenceException(e);
		}
		log.debug("/removeTable4Ontology");

	}

	@Override
	public void createIndex(String sentence) {
		log.debug(CREATE_INDEX, sentence);
		String pquery = null;
		String collection = null;
		Map<String, Integer> indexKeys = null;
		IndexOptions indexOptions = null;
		try {
			pquery = sentence.trim();
			if (pquery.indexOf(".createIndex(") == -1)
				throw new DBPersistenceException("No db.<collection>.createIndex() found in sentence");
			if (pquery.indexOf(".createIndex({\"") == -1 && pquery.indexOf(".createIndex({'") == -1)
				throw new DBPersistenceException(
						"Please use ' in sentences, example: db.<collection>.createIndex({'<attribute>':1}) ");

			collection = util.getCollectionName(pquery);

			try {
				pquery = pquery.substring(pquery.indexOf("createIndex(") + 12, pquery.indexOf("})") + 1);
			} catch (final Exception e) {
				log.error("Query bad formed:" + pquery
						+ ".Expected db.<collection>.createIndex({<attribute>:1},{name:'name_index',....})");
				throw new DBPersistenceException("Query bad formed:" + pquery
						+ ".Expected db.<collection>.createIndex({<attribute>:1},{name:'name_index',....})");
			}
			List<String> keyElements= getElements(pquery);
			try {
				indexKeys = objectMapper.readValue(keyElements.get(0),
						new TypeReference<Map<String, ?>>() {
						});
				if (keyElements.size() == 2) {
					indexOptions = objectMapper.readValue(keyElements.get(1),
							IndexOptions.class);
				}					
			} catch (final IOException e) {
				log.error("Invalid index key or index options. Sentence = {}, cause = {}, errorMessage = {}.",
						sentence, e.getCause(), e.getMessage());
				throw new DBPersistenceException("Invalid index key or index options", e);
			}
			mongoDbConnector.createIndex(database, collection, new MongoDbIndex(indexKeys, indexOptions));

		} catch (final DBPersistenceException e) {
			log.error(CREATE_INDEX + e.getMessage());
			throw new DBPersistenceException(e);
		}
	}
	
	private List<String> getElements(String query){
		List<String> elements = new ArrayList<>();
		StringBuilder element = new StringBuilder();
		int i = 0;
		int openKeys = 0;
		int comma = 0;
		while (i < query.length()) {
			final char c = query.charAt(i);
			i++;
			if (c == '{') {
				element.append(c);
				openKeys++;
			} else if (c == '}') {
				element.append(c);
				openKeys--;
				if (openKeys == 0) {
					elements.add(element.toString());
					element = new StringBuilder();
				}
			} else if (openKeys == 0) {
				if (c == ',') {
					comma++;
				} else if (c != ' ' && c != '\n' && c != '\t') {
					String errorMessage = String.format("Query malformed, error on character in position %s. Query: %s",i,query);
					log.error(errorMessage);
					throw new DBPersistenceException(errorMessage);
				}
			} else {
				element.append(c);
			}
		}
		if (elements.size() > 2 || comma != elements.size()-1) {
			String errorMessage = String.format("createIndex({keys},{options}) structure malformed on query: %s",query);
			log.error(errorMessage);
			throw new DBPersistenceException(errorMessage);	
		}
		return elements;
	}

	@Override
	public Map<String, Boolean> getStatusDatabase() {
		final Map<String, Boolean> map = new HashMap<>();
		map.put(database, mongoDbConnector.testConnection());
		return map;
	}

	@Override
	public void dropIndex(String ontology, String indexName) {
		log.debug("dropIndex", indexName, ontology);
		if (indexName != null && ontology != null) {
			try {
				mongoDbConnector.dropIndex(database, ontology, new MongoDbIndex(indexName));
			} catch (final DBPersistenceException e) {
				log.error("dropIndex", e, indexName);
				throw new DBPersistenceException(e);
			}
		}
	}

	@Override
	public List<String> getListIndexes(String ontology) {
		log.debug(GET_INDEX, ontology);
		try {
			final List<String> index = new ArrayList<>();
			if (ontology != null) {
				return mongoDbConnector.getIndexesAsStrings(database, ontology);
			}
			return index;
		} catch (final Exception e) {
			log.error(GET_INDEX, e);
			throw new DBPersistenceException(e);
		}
	}

	@Override
	public String getIndexes(String ontology) {
		log.debug(GET_INDEX, ontology);
		try {
			final List<MongoDbIndex> list = mongoDbConnector.getIndexes(database, ontology);
			return objectMapper.writeValueAsString(list);
		} catch (final Exception e) {
			log.error(GET_INDEX, e);
			throw new DBPersistenceException(e);
		}
	}

	private void computeGeometryIndex(String collection, String name, String schema) {
		log.debug("computeGeometryIndex", collection, name);

		try {
			final List<String> list = JSONPersistenceUtilsMongo.getGeoIndexes(schema);
			if (list != null && !list.isEmpty())
				for (final String string : list) {
					createIndex(collection, string, "2dsphere");
					ensureGeoIndex(collection, string);
				}
		} catch (final Exception e) {
			log.error("Cannot create geo indexes: " + e.getMessage(), e);
		}

		if (!name.isEmpty()) {
			createIndex(collection, name + ": \"2dsphere\"");
		}
		log.debug("DONE : computeGeometryIndex", collection, name);
	}

	@Override
	public void validateIndexes(String collection, String schema) {
		log.debug(VALIDATE_INDEX, collection, schema);
		if (collection != null) {
			if (schema.trim().length() > 0) {
				String esquema = util.prepareEsquema(schema);
				if (esquema.contains("'")) {
					esquema = esquema.replace("'", "\"");
				}
				try {
					final Map<String, Object> obj2 = objectMapper.readValue(esquema,
							new TypeReference<Map<String, Object>>() {
							});
					List<String> names;
					if (obj2.containsKey("properties")) {

						final Map<String, Object> proper = (Map<String, Object>) obj2.get("properties");
						names = util.getParentProperties(proper, obj2);
						if (!names.isEmpty()) {
							for (final String name : names)
								createIndex(collection, name + ": \"2dsphere\"");
						}

						for (final String name : names)
							computeGeometryIndex(collection, name, schema);

					}
				} catch (final JsonParseException | JsonMappingException e) {
					log.error(VALIDATE_INDEX, e);
					throw new DBPersistenceException(e.getMessage());
				} catch (final IOException e) {
					log.error(VALIDATE_INDEX, e);
					throw new DBPersistenceException(new Exception(e.getMessage()));
				}
			} else {
				log.warn(VALIDATE_INDEX, "Not found ontology");
				throw new DBPersistenceException(new Exception("Not found ontology " + collection));
			}
		}
	}

	@Override
	public void createIndex(String ontology, String attribute) {
		log.debug(CREATE_INDEX, attribute, ontology);
		final Map<String, Integer> indexKey = new HashMap<>();
		indexKey.put(attribute, 1);
		try {
			mongoDbConnector.createIndex(database, ontology, new MongoDbIndex(indexKey));
		} catch (final DBPersistenceException e) {
			log.error(CREATE_INDEX, e, attribute);
			throw new DBPersistenceException(e);
		}
	}

	public void ensureGeoIndex(String ontology, String attribute) {
		try {
			log.debug("ensureGeoIndex", ontology, attribute);
			mongoDbConnector.createIndex(database, ontology, Indexes.geo2dsphere(attribute));
		} catch (final DBPersistenceException e) {
			log.error(CREATE_INDEX, e, attribute);

		}
	}

	@Override
	public void createIndex(String ontology, String name, String attribute) {
		log.debug(CREATE_INDEX, attribute, name, ontology);
		try {
			createIndex("db." + ontology + ".createIndex({'" + attribute + "':1},{'name':'" + name + "'})");
		} catch (final DBPersistenceException e) {
			log.error(CREATE_INDEX, e, attribute);
			throw new DBPersistenceException(e);
		}
	}

	@Override
	public ExportData exportToJson(String ontology, long startDateMillis, String pathToFile) {
		final SimpleDateFormat format = new SimpleDateFormat("yyyy-dd-MM-hh-mm");
		final String query = "{'contextData.timestampMillis':{$lte:" + startDateMillis + "}}";
		final String path;

		if (pathToFile.equals("default"))
			path = exportPath + ontology + format.format(new Date()) + ".json";
		else
			path = pathToFile;

		if (mongoDbConnector.count(database, ontology, "count(" + query + ")") > 0) {
			ProcessBuilder pb = null;
			if (mongoDbConnector.getCredentials().isEnableMongoDbAuthentication()) {
				pb = new ProcessBuilder("mongoexport", "--host",
						mongoDbConnector.getConnection().getServerAddressList().get(0).getHost(), "--db", database,
						"--username", mongoDbConnector.getCredentials().getUsername(), "--password",
						mongoDbConnector.getCredentials().getPassword(), "--collection", ontology, "--query", query,
						"--out", path, "--authenticationDatabase",
						mongoDbConnector.getCredentials().getAuthenticationDatabase());
			} else {
				pb = new ProcessBuilder("mongoexport", "--db", database, "--collection", ontology, "--query", query,
						"--out", path);
			}

			try {
				pb.redirectErrorStream(true);
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
				log.info("Mongoexport command {}", pb.command());
				log.info("Cmd mongoexport output: {} ",builder.toString());
				log.info("Created export file for ontology {} at {}", ontology, path);
			} catch (IOException | InterruptedException e) {
				log.error("Could not execute command {}", e.getMessage());
				throw new DBPersistenceException("Could not execute command: " + pb.command().toString() + e);
			}
		} else
			log.debug("No ontologies to export");
		return ExportData.builder().filterQuery(query).path(path).build();
	}

	@Override
	public long deleteAfterExport(String ontology, String query) {
		return mongoDbConnector.remove(database, ontology, query, false).getCount();
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
