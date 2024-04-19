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
package com.minsait.onesait.platform.persistence.elasticsearch.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.client.indices.PutComposableIndexTemplateRequest;
import org.elasticsearch.cluster.metadata.AliasMetadata;
import org.elasticsearch.cluster.metadata.ComposableIndexTemplate;
import org.elasticsearch.cluster.metadata.Template;
import org.elasticsearch.common.compress.CompressedXContent;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.persistence.ElasticsearchEnabledCondition;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import lombok.extern.slf4j.Slf4j;

@Service
@Conditional(ElasticsearchEnabledCondition.class)
@Slf4j
public class ESBaseApi {

	@Autowired
	private RestHighLevelClient hlClient;

	@Autowired
	private IntegrationResourcesService resourcesService;

	private int defaultReplicas = 0;
	private int defaultShards = 5;
	final static String TEMPLATE_SEPARATOR = "-";

	@PostConstruct
	void initializeIt() {
		final Map<String, Object> database = resourcesService.getGlobalConfiguration().getEnv().getDatabase();

		@SuppressWarnings("unchecked")
		final Map<String, Object> elasticsearch = (Map<String, Object>) database.get("elasticsearch");

		@SuppressWarnings("unchecked")
		final Map<String, Object> defaults = (Map<String, Object>) elasticsearch.get("defaults");

		defaultReplicas = (int) defaults.get("replicas");
		defaultShards = (int) defaults.get("shards");
	}

	public boolean createIndex(String index, String dataMapping, Map<String, String> config) {

		final CreateIndexRequest request = new CreateIndexRequest(index);
		Integer shards = defaultShards;
		Integer replicas = defaultReplicas;
		if (config == null) {
			config = new HashMap<>();
		}
		// add advanced index creation: shards, replicas, etc.
		if (!config.isEmpty()) {
			try {
				shards = Integer.parseInt(config.get("shards"));
			} catch (final Exception e) {
				log.info("No shard config definition was found. Using defaualt value");
			}
			try {
				replicas = Integer.parseInt(config.get("replicas"));
			} catch (final Exception e) {
				log.info("No replicas config definition was found. Using defaualt value");
			}
		}

		request.settings(
				Settings.builder().put("index.number_of_shards", shards).put("index.number_of_replicas", replicas));
		// Add mapping if defined
		if (!dataMapping.isEmpty()) {
			request.mapping(dataMapping, XContentType.JSON);
		}

		try {
			final CreateIndexResponse createIndexResponse = hlClient.indices().create(request, RequestOptions.DEFAULT);

			// TODO change return data or use void
			return createIndexResponse.isAcknowledged();
		} catch (final ElasticsearchStatusException e) {
			if (e.getMessage().contains("already exists")) {
				log.warn("Index {} on ElasticSerach already exists, so skipping creation", index);
				return true;
			}
			log.error("HTTP Error while creating index {}", e.getMessage());
			return false;
		} catch (final IOException e) {
			log.error("Error Creating Index " + e.getMessage());
			return false;
		}
	}

	public String[] getIndexes() {
		try {
			final GetIndexRequest request = new GetIndexRequest("*");
			final GetIndexResponse response = hlClient.indices().get(request, RequestOptions.DEFAULT);
			return response.getIndices();
		} catch (final IOException e) {
			log.error("Error getIndexes ", e);
			return new String[0];
		}
	}

	// TODO check if this method was thought for documents or indices
	public String updateDocument(String index, String id, String jsonData) {
		try {
			final UpdateRequest request = new UpdateRequest(index, id);
			request.doc(jsonData, XContentType.JSON);
			final UpdateResponse updateResponse = hlClient.update(request, RequestOptions.DEFAULT);
			return updateResponse.getResult().toString();
		} catch (final IOException e) {
			log.error("UpdateIndex", e);
			return null;
		}
	}

	public boolean deleteIndex(String index) {
		final DeleteIndexRequest request = new DeleteIndexRequest(index);
		try {
			final AcknowledgedResponse deleteIndexResponse = hlClient.indices().delete(request, RequestOptions.DEFAULT);
			log.info("Delete index result :" + deleteIndexResponse.isAcknowledged());
			return deleteIndexResponse.isAcknowledged();
		} catch (final IOException e) {
			log.error("Error Deleting Type " + e.getMessage());
			return false;
		} catch (final ElasticsearchStatusException e) {
			if (e.getResourceType() != null && e.getResourceType().equals("index_or_alias") && e.status() != null
					&& e.status().name() != null && e.status().name().equals("NOT_FOUND")) {
				log.error("Error Deleting Type " + e.getMessage());
				return false;
			} else {

				throw e;
			}
		}
	}

	public boolean createTemplate(String indexPrefix, String dataMapping, Map<String, String> config) {

		final PutComposableIndexTemplateRequest request = new PutComposableIndexTemplateRequest().name(indexPrefix);

		Integer shards = defaultShards;
		Integer replicas = defaultReplicas;
		if (config == null) {
			config = new HashMap<>();
		}
		// add advanced index creation: shards, replicas, etc.
		if (!config.isEmpty()) {
			try {
				shards = Integer.parseInt(config.get("shards"));
			} catch (final Exception e) {
				log.info("No shard config definition was found. Using defaualt value");
			}
			try {
				replicas = Integer.parseInt(config.get("replicas"));
			} catch (final Exception e) {
				log.info("No replicas config definition was found. Using defaualt value");
			}
		}
		final AliasMetadata placeholderAlias = AliasMetadata.builder(indexPrefix).build();
		final Map<String, AliasMetadata> aliases = new HashMap<>();
		aliases.put(indexPrefix, placeholderAlias);
		final Settings settings = Settings.builder().put("index.number_of_shards", shards)
				.put("index.number_of_replicas", replicas).build();
		// Add mapping if defined
		CompressedXContent mapping = null;
		if (!dataMapping.isEmpty()) {
			try {
				mapping = new CompressedXContent(dataMapping);
			} catch (final Exception e) {
				log.error("Error on mapping while creating Index Template {}. Message:{}, Cause: {}", indexPrefix,
						e.getMessage(), e.getCause());
				return false;
			}
		}
		final Template template = new Template(settings, mapping, aliases);

		final ComposableIndexTemplate composableIndexTemplate = new ComposableIndexTemplate(
				Arrays.asList(indexPrefix + TEMPLATE_SEPARATOR + "*"), template, null, 200L, null, null);
		request.indexTemplate(composableIndexTemplate);

		// do not allow update
		request.create(true);
		try {
			final AcknowledgedResponse putTemplateResponse = hlClient.indices().putIndexTemplate(request,
					RequestOptions.DEFAULT);
			// TODO change return data or use void
			return putTemplateResponse.isAcknowledged();
		} catch (final IOException e) {
			log.error("Error Creating Index Template {}. Message:{}, Cause: {}", indexPrefix, e.getMessage(),
					e.getCause());
			return false;
		}
	}
}
