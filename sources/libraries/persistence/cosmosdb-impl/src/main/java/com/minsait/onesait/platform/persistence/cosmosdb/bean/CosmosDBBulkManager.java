/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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
package com.minsait.onesait.platform.persistence.cosmosdb.bean;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.microsoft.azure.documentdb.DocumentClient;
import com.microsoft.azure.documentdb.PartitionKeyDefinition;
import com.microsoft.azure.documentdb.bulkexecutor.DocumentBulkExecutor;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CosmosDBBulkManager {

	@Getter
	private final Map<String, DocumentBulkExecutor> bulkExecutorCache = new HashMap<>();

	@Autowired
	private DocumentClient cosmosClient;

	@Autowired
	private OntologyRepository ontologyRepository;

	private static final int THROUGHPUT_PER_COLLECTION = 10000;

	public DocumentBulkExecutor get(String ontology) {
		if (bulkExecutorCache.get(ontology) != null) {
			return bulkExecutorCache.get(ontology);
		} else {
			return put(ontology);
		}
	}

	public DocumentBulkExecutor put(String ontology) {
		final Ontology o = ontologyRepository.findByIdentification(ontology);
		if (o != null) {
			final PartitionKeyDefinition key = new PartitionKeyDefinition();
			key.setPaths(Arrays.asList(o.getPartitionKey()));
			try {
				bulkExecutorCache.put(ontology,
						DocumentBulkExecutor.builder().from(cosmosClient, Tenant2SchemaMapper.getRtdbSchema(),
								o.getIdentification(), key, THROUGHPUT_PER_COLLECTION).build());
				return bulkExecutorCache.get(ontology);
			} catch (final Exception e) {
				log.error("Could not allocate DocumentBulkExecutor for ontology {}", ontology, e);
				throw new DBPersistenceException("Could not execute bulk operation", e);
			}
		}
		throw new DBPersistenceException("Could not execute bulk operation for non existing ontology " + ontology);
	}

}
