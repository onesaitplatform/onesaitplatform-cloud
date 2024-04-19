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
package com.minsait.onesait.platform.persistence.cosmosdb.utils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.documentdb.DocumentClient;
import com.microsoft.azure.documentdb.FeedOptions;
import com.microsoft.azure.documentdb.PartitionKey;
import com.microsoft.azure.documentdb.RequestOptions;
import com.microsoft.azure.documentdb.internal.routing.CollectionCache;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CosmosDBUtils {

	public static final String COLLECTION_LINK_GENERIC = "/dbs/%s/colls/%s";

	public static final String COLLECTION_LINK_GENERIC_CACHE = "dbs/%s/colls/%s";

	public static final String DATABASE_LINK_GENERIC = "/dbs/%s";

	public static final String DOCUMENT_LINK_GENERIC = "/dbs/%s/colls/%s/docs/%s";

	public static final String COUNT_QUERY_GENERIC = "SELECT VALUE COUNT(1) FROM c";

	public static final String QUERY_BY_ID = "SELECT * FROM c WHERE c.id=\"%s\"";

	public static final String QUERY_GENERIC = "SELECT * FROM c ";

	public static final String AGGREGATE_FIELD = "_aggregate";

	private static final ObjectMapper mapper = new ObjectMapper();

	public static String collectionLink(String ontology) {
		return String.format(CosmosDBUtils.COLLECTION_LINK_GENERIC, Tenant2SchemaMapper.getRtdbSchema(), ontology);
	}

	public static String collectionLinkCache(String ontology) {
		return String.format(CosmosDBUtils.COLLECTION_LINK_GENERIC_CACHE, Tenant2SchemaMapper.getRtdbSchema(),
				ontology);
	}

	public static String databaseLink() {
		return String.format(CosmosDBUtils.DATABASE_LINK_GENERIC, Tenant2SchemaMapper.getRtdbSchema());
	}

	public static String documentLink(String ontology, String id) {
		return String.format(CosmosDBUtils.DOCUMENT_LINK_GENERIC, Tenant2SchemaMapper.getRtdbSchema(), ontology, id);
	}

	public static String countQuery() {
		return CosmosDBUtils.COUNT_QUERY_GENERIC;
	}

	public static String queryId(String id) {
		return String.format(CosmosDBUtils.QUERY_BY_ID, id);
	}

	public static FeedOptions feedOptions() {
		// Common feed options for every operation
		final FeedOptions fo = new FeedOptions();
		fo.setEnableCrossPartitionQuery(true);

		return fo;
	}

	public static RequestOptions requestOptions(String json, String partitionKey) {
		final RequestOptions ro = new RequestOptions();

		try {
			final Object value = mapper.readTree(json).at(partitionKey).asText();
			final PartitionKey key = new PartitionKey(value);
			ro.setPartitionKey(key);
		} catch (final IOException e) {
			log.error("Could not extract partition key value");

		}
		return ro;
	}

	public static RequestOptions requestOptions(JsonNode json, String partitionKey) {
		final RequestOptions ro = new RequestOptions();

		final Object value = json.at(partitionKey).asText();
		final PartitionKey key = new PartitionKey(value);
		ro.setPartitionKey(key);

		return ro;
	}

	public static RequestOptions requestOptions(String partitionKey) {
		final RequestOptions ro = new RequestOptions();
		final PartitionKey key = new PartitionKey(partitionKey);
		ro.setPartitionKey(key);
		return ro;
	}

	public static void refreshCollectionCacheByReflection(DocumentClient client, String collection) {

		try {
			final Method getCollectionCache = DocumentClient.class.getDeclaredMethod("getCollectionCache");
			getCollectionCache.setAccessible(true);

			final CollectionCache cache = (CollectionCache) getCollectionCache.invoke(client);

			cache.refresh(CosmosDBUtils.collectionLinkCache(collection));

		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			log.error("Could not refresh Collection Cache for ontology {} on CosmosDB", collection);
		}

	}
}
