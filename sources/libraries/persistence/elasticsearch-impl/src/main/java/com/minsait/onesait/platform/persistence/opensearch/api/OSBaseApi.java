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
package com.minsait.onesait.platform.persistence.opensearch.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.elasticsearch.ElasticsearchStatusException;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.ErrorResponse;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.mapping.Property;
import org.opensearch.client.opensearch._types.mapping.TypeMapping;
import org.opensearch.client.opensearch.cat.IndicesResponse;
import org.opensearch.client.opensearch.core.UpdateRequest;
import org.opensearch.client.opensearch.core.UpdateResponse;
import org.opensearch.client.opensearch.indices.Alias;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.CreateIndexResponse;
import org.opensearch.client.opensearch.indices.DeleteIndexRequest;
import org.opensearch.client.opensearch.indices.DeleteIndexResponse;
import org.opensearch.client.opensearch.indices.DeleteIndexTemplateRequest;
import org.opensearch.client.opensearch.indices.IndexSettings;
import org.opensearch.client.opensearch.indices.PutIndexTemplateRequest;
import org.opensearch.client.opensearch.indices.PutIndexTemplateResponse;
import org.opensearch.client.opensearch.indices.put_index_template.IndexTemplateMapping;
import org.opensearch.client.transport.JsonEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.persistence.OpensearchEnabledCondition;
import com.minsait.onesait.platform.persistence.opensearch.api.client.requests.policy.add.AddISMPolicyToIndicesRequest;
import com.minsait.onesait.platform.persistence.opensearch.api.client.requests.policy.add.AddISMPolicyToIndicesResponse;
import com.minsait.onesait.platform.persistence.opensearch.api.client.requests.policy.create.CreateISMPolicyRequest;
import com.minsait.onesait.platform.persistence.opensearch.api.client.requests.policy.create.CreateISMPolicyResponse;
import com.minsait.onesait.platform.persistence.opensearch.api.client.requests.policy.create.body.ISMPolicy;
import com.minsait.onesait.platform.persistence.opensearch.api.client.requests.policy.create.body.ISMPolicyAction;
import com.minsait.onesait.platform.persistence.opensearch.api.client.requests.policy.create.body.ISMTemplate;
import com.minsait.onesait.platform.persistence.opensearch.api.client.requests.policy.create.body.PolicyState;
import com.minsait.onesait.platform.persistence.opensearch.api.client.requests.policy.create.body.PolicyStateTransition;
import com.minsait.onesait.platform.persistence.opensearch.api.client.requests.policy.create.body.PolicyStateTransitionCondition;
import com.minsait.onesait.platform.persistence.opensearch.api.client.requests.policy.delete.DeleteISMPolicyRequest;
import com.minsait.onesait.platform.persistence.opensearch.api.client.requests.policy.delete.DeleteISMPolicyResponse;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import lombok.extern.slf4j.Slf4j;

@Service
@Conditional(OpensearchEnabledCondition.class)
@Slf4j
public class OSBaseApi {

	@Autowired
	private OpenSearchClient javaClient;

	@Autowired
	private IntegrationResourcesService resourcesService;

	private String defaultReplicas = "0";
	private String defaultShards = "5";
	final static String TEMPLATE_SEPARATOR = "-";

	@PostConstruct
	void initializeIt() {
		final Map<String, Object> database = resourcesService.getGlobalConfiguration().getEnv().getDatabase();

		@SuppressWarnings("unchecked")
		final Map<String, Object> elasticsearch = (Map<String, Object>) database.get("elasticsearch");

		@SuppressWarnings("unchecked")
		final Map<String, Object> defaults = (Map<String, Object>) elasticsearch.get("defaults");
		defaultReplicas = String.valueOf((int) defaults.get("replicas"));
		defaultShards = String.valueOf((int) defaults.get("shards"));
	}

	public boolean createIndex(String index, Map<String, Property> dataMapping, Map<String, String> config) {

		String shards = defaultShards;
		String replicas = defaultReplicas;
		if (config == null) {
			config = new HashMap<>();
		}
		// add advanced index creation: shards, replicas, etc.
		if (!config.isEmpty()) {
			try {
				shards = config.get("shards");
			} catch (final Exception e) {
				log.info("No shard config definition was found. Using defaualt value");
			}
			try {
				replicas = config.get("replicas");
			} catch (final Exception e) {
				log.info("No replicas config definition was found. Using defaualt value");
			}
		}
		IndexSettings indexSettings = new IndexSettings.Builder().numberOfShards(shards).numberOfReplicas(replicas)
				.build();
		CreateIndexRequest.Builder request = new CreateIndexRequest.Builder().index(index).settings(indexSettings);

		// Add mapping if defined
		if (!dataMapping.isEmpty()) {
			TypeMapping.Builder mappingBuilder = new TypeMapping.Builder();
			mappingBuilder.properties(dataMapping);
			request.mappings(mappingBuilder.build());
		}

		try {
			CreateIndexResponse createIndexResponse = javaClient.indices().create(request.build());
			return createIndexResponse.acknowledged();
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
			// NOTE: GetIndexRequest call does not use _cat/indices. It GETs /*, so all the
			// mappings try to be parsed. Graylog indexes throw exception on parsing. Use
			// _cat/indices instead	
			IndicesResponse resp = javaClient.cat().indices();
			return resp.valueBody().stream().map(record -> record.index()).toArray(String[]::new);
		} catch (final IOException e) {
			log.error("Error getIndexes ", e);
			return new String[0];
		}
	}

	// TODO check if this method was thought for documents or indices, Is this
	// method ever used?
	public String updateDocument(String index, String id, String jsonData) {
		try {
			final UpdateRequest<String, String> updateRequest = new UpdateRequest.Builder<String, String>().index(index)
					.id(id).doc(jsonData).build();
			final UpdateResponse<String> resp = javaClient.update(updateRequest, String.class);
			return resp.result().toString();
		} catch (final IOException e) {
			log.error("UpdateIndex", e);
			return null;
		}
	}

	public boolean deleteIndex(String index) {
		final DeleteIndexRequest deleteRequest = new DeleteIndexRequest.Builder().index(index).build();
		try {
			final DeleteIndexResponse response = javaClient.indices().delete(deleteRequest);
			log.info("Delete index result :" + response.acknowledged());
			return response.acknowledged();
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
		} catch (final OpenSearchException e ) {
			if (e.response().error().type().equals("index_not_found_exception")) {
				log.error("Error Deleting Type " + e.getMessage());
				return false;
			} else {

				throw e;
			}
		}
	}

	public boolean createTemplate(String indexPrefix, Map<String, Property> dataMapping, Map<String, String> config) {
		final IndexTemplateMapping.Builder templateMappingBuilder = new IndexTemplateMapping.Builder();
		final PutIndexTemplateRequest.Builder putIndexTemplateRequest = new PutIndexTemplateRequest.Builder()
				.name(indexPrefix);

		Integer shards = 5;// defaultShards;
		Integer replicas = 0;// defaultReplicas;
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

		IndexSettings indexSettings = new IndexSettings.Builder().numberOfShards(String.valueOf(shards))
				.numberOfReplicas(String.valueOf(replicas)).build();
		templateMappingBuilder.settings(indexSettings);
		final Map<String, Alias> aliasesMap = new HashMap<>();
		final Alias alias = new Alias.Builder().build();
		aliasesMap.put(indexPrefix, alias);
		templateMappingBuilder.aliases(aliasesMap);

		if (!dataMapping.isEmpty()) {
			TypeMapping.Builder typeMapping = new TypeMapping.Builder();
			typeMapping.properties(dataMapping);
			templateMappingBuilder.mappings(typeMapping.build());
		}
		// do not allow update
		// request.create(true);
		// is it necessary to force "not update"?
		
		putIndexTemplateRequest.indexPatterns(indexPrefix + TEMPLATE_SEPARATOR + "*");
		try {
			putIndexTemplateRequest.template(templateMappingBuilder.build());

			PutIndexTemplateResponse putIndexTemplateResponse = javaClient.indices()
					.putIndexTemplate(putIndexTemplateRequest.build());
			return putIndexTemplateResponse.acknowledged();
		} catch (final OpenSearchException | IOException e1) {
			log.error("Error Creating Index Template {}. Message:{}, Cause: {}", indexPrefix, e1.getMessage(),
					e1.getCause());
			return false;
		}
	}
	
	public boolean deleteTemplate(String template) {
		DeleteIndexTemplateRequest request = new DeleteIndexTemplateRequest.Builder().name(template).build();
		try {
			javaClient.indices().deleteIndexTemplate(request);
		} catch (OpenSearchException | IOException e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}

	public boolean createTTLPolicy(String policyName, String alias, String period, int priority) {

		JsonEndpoint<CreateISMPolicyRequest, CreateISMPolicyResponse, ErrorResponse> endpoint = (JsonEndpoint<CreateISMPolicyRequest, CreateISMPolicyResponse, ErrorResponse>) CreateISMPolicyRequest._ENDPOINT;
		// Create states
		List<PolicyState> states = new ArrayList<>();
		// HOT State
		PolicyStateTransitionCondition condition = new PolicyStateTransitionCondition.Builder().min_index_age(period)
				.build();
		PolicyStateTransition transition = new PolicyStateTransition.Builder().state_name("deleted")
				.conditions(condition).build();
		states.add(new PolicyState.Builder().name("hot").transitions(transition).build());
		// DELETED state
		ISMPolicyAction action = new ISMPolicyAction.Builder().name("delete").build();
		states.add(new PolicyState.Builder().name("deleted").actions(action).build());
		// Create ISM Template
		ISMTemplate applyPolicyToTemplates = new ISMTemplate.Builder().index_patterns(alias).priority(priority).build();
		// Create policy
		String description = "Retention Policy for " + alias + " indexes";
		ISMPolicy policy = new ISMPolicy.Builder().description(description).default_state("hot")
				.ism_template(applyPolicyToTemplates).states(states).build();
		// Add policy to request
		CreateISMPolicyRequest request = new CreateISMPolicyRequest.Builder().name(policyName).policy(policy).build();
		try {
			javaClient._transport().performRequest(request, endpoint, javaClient._transportOptions());
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}

		return true;
	}
	
	public boolean deleteTTLPolicy(String policyName) {
		JsonEndpoint<DeleteISMPolicyRequest, DeleteISMPolicyResponse, ErrorResponse> endpoint = (JsonEndpoint<DeleteISMPolicyRequest, DeleteISMPolicyResponse, ErrorResponse>) DeleteISMPolicyRequest._ENDPOINT;
		DeleteISMPolicyRequest request = new DeleteISMPolicyRequest.Builder().name(policyName).build();
		try {
			javaClient._transport().performRequest(request, endpoint, javaClient._transportOptions());
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		} catch (OpenSearchException e) {
			if (e.response().status()==404){
				// Trying to delete non existing Policy, go on.
				return true;
			}
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean addPolicyToIndices(String policy_id, String indicesRegex) {
		JsonEndpoint<AddISMPolicyToIndicesRequest, AddISMPolicyToIndicesResponse, ErrorResponse> endpoint = (JsonEndpoint<AddISMPolicyToIndicesRequest, AddISMPolicyToIndicesResponse, ErrorResponse>) AddISMPolicyToIndicesRequest._ENDPOINT;
		AddISMPolicyToIndicesRequest request = new AddISMPolicyToIndicesRequest.Builder().indices(indicesRegex).policy_id(policy_id).build();
		try {
			javaClient._transport().performRequest(request, endpoint, javaClient._transportOptions());
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		} 
		return true;
	}

}
