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
package com.minsait.onesait.platform.persistence.cosmosdb;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.microsoft.azure.documentdb.DocumentClient;
import com.microsoft.azure.documentdb.DocumentClientException;
import com.microsoft.azure.documentdb.DocumentCollection;
import com.microsoft.azure.documentdb.PartitionKeyDefinition;
import com.microsoft.azure.documentdb.RequestOptions;
import com.microsoft.azure.documentdb.ResourceResponse;
import com.microsoft.azure.documentdb.UniqueKey;
import com.microsoft.azure.documentdb.UniqueKeyPolicy;
import com.minsait.onesait.platform.commons.model.DescribeColumnData;
import com.minsait.onesait.platform.commons.rtdbmaintainer.dto.ExportData;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;
import com.minsait.onesait.platform.persistence.cosmosdb.utils.CosmosDBUtils;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.interfaces.ManageDBRepository;

import lombok.extern.slf4j.Slf4j;

@Component("CosmosDBManageDBRepository")
@Slf4j
public class CosmosDBManageDBRepository implements ManageDBRepository {
	@Autowired
	DocumentClient cosmosClient;
	@Autowired
	CosmosDBBasicOpsDBRepository basicOpsRepository;

	@Override
	public Map<String, Boolean> getStatusDatabase() {
		final Map<String, Boolean> map = new HashMap<>();
		try {
			map.put(Tenant2SchemaMapper.getRtdbSchema(), cosmosClient.getDatabaseAccount() != null);
		} catch (final DocumentClientException e) {
			log.error("Cosmosdb database connection error");
			map.put(Tenant2SchemaMapper.getRtdbSchema(), false);
		}
		return map;
	}

	@Override
	public String createTable4Ontology(String ontology, String schema, Map<String, String> config) {
		checkCreateTable(ontology);
		final DocumentCollection collection = new DocumentCollection();
		collection.setId(ontology);
		final PartitionKeyDefinition key = new PartitionKeyDefinition();
		key.setPaths(Arrays.asList(config.get("partitionKey")));
		collection.setPartitionKey(key);
		if (config.get("uniqueKeys") != null) {
			final UniqueKeyPolicy uniqueKeyPolicy = new UniqueKeyPolicy();
			final UniqueKey uKey = new UniqueKey();
			uKey.setPaths(Arrays.asList(config.get("uniqueKeys").split(",")));
			uniqueKeyPolicy.setUniqueKeys(Arrays.asList(uKey));
			collection.setUniqueKeyPolicy(uniqueKeyPolicy);
		}
		try {
			cosmosClient.createCollection(CosmosDBUtils.databaseLink(), collection, new RequestOptions());
		} catch (final DocumentClientException e) {
			log.error("Could not create Ontology on CosmosDB", e);
			throw new DBPersistenceException("Could not create collection on CosmosDB");
		}

		return collection.getId();
	}

	@Override
	public List<String> getListOfTables() {
		return cosmosClient.readCollections(CosmosDBUtils.DATABASE_LINK_GENERIC, CosmosDBUtils.feedOptions())
				.getQueryIterable().toList().stream().map(DocumentCollection::getId).collect(Collectors.toList());

	}

	@Override
	public List<String> getListOfTables4Ontology(String ontology) {
		try {
			return Arrays
					.asList(cosmosClient.readCollection(CosmosDBUtils.collectionLink(ontology), new RequestOptions())
							.getResource().getId());
		} catch (final DocumentClientException e) {
			log.error("Collection {} does not exist in CosmosDB", ontology, e);
			throw new DBPersistenceException("Collection does not exist", e);
		}
	}

	@Override
	public void removeTable4Ontology(String ontology) {
		try {
			cosmosClient.deleteCollection(CosmosDBUtils.collectionLink(ontology), new RequestOptions());
			CosmosDBUtils.refreshCollectionCacheByReflection(cosmosClient, ontology);
		} catch (final DocumentClientException e) {
			log.error("Could not delete collection on CosmosDB", e);
			throw new DBPersistenceException("Could not delete collection", e);
		}

	}

	@Override
	public void createIndex(String ontology, String attribute) {
		// TO-DO partition key
		throw new DBPersistenceException("Not implemented");
	}

	@Override
	public void createIndex(String ontology, String nameIndex, String attribute) {
		// TO-DO partition key
		throw new DBPersistenceException("Not implemented");
	}

	@Override
	public void createIndex(String sentence) {
		// TO-DO partition key
		throw new DBPersistenceException("Not implemented");

	}

	@Override
	public void dropIndex(String ontology, String indexName) {
		// TO-DO partition key
		throw new DBPersistenceException("Not implemented");

	}

	@Override
	public List<String> getListIndexes(String ontology) {
		throw new DBPersistenceException("Not implemented");
	}

	@Override
	public String getIndexes(String ontology) {
		throw new DBPersistenceException("Not implemented");
	}

	@Override
	public void validateIndexes(String ontology, String schema) {
		throw new DBPersistenceException("Not implemented");

	}

	@Override
	public ExportData exportToJson(String ontology, long startDateMillis, String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long deleteAfterExport(String ontology, String query) {

		return 0;
	}

	@Override
	public List<DescribeColumnData> describeTable(String name) {
		throw new DBPersistenceException("Not implemented");
	}

	@Override
	public Map<String, String> getAdditionalDBConfig(String ontology) {
		throw new DBPersistenceException("Not implemented");
	}

	@Override
	public String updateTable4Ontology(String identification, String jsonSchema, Map<String, String> config) {

		return identification;
	}

	private void checkCreateTable(String ontology) {
		if (collectionExists(ontology)) {
			if (basicOpsRepository.count(ontology) > 0) {
				log.error("The collection {} already exists and has records", ontology);
				throw new DBPersistenceException("The collection already exists and has records");
			} else {
				removeTable4Ontology(ontology);
			}
		}
	}

	private boolean collectionExists(String collection) {
		try {
			final ResourceResponse<DocumentCollection> coll = cosmosClient
					.readCollection(CosmosDBUtils.collectionLink(collection), new RequestOptions());
			if (coll.getStatusCode() == 200)
				return true;
		} catch (final DocumentClientException e) {
			log.debug("Collection doesn't exist");
		}
		return false;
	}

}
