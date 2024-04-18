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
import java.util.Arrays;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.query_dsl.IdsQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.QueryStringQuery;
import org.opensearch.client.opensearch.core.DeleteByQueryRequest;
import org.opensearch.client.opensearch.core.DeleteByQueryResponse;
import org.opensearch.client.opensearch.core.DeleteRequest;
import org.opensearch.client.opensearch.core.DeleteResponse;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.commons.model.MultiDocumentOperationResult;
import com.minsait.onesait.platform.persistence.OpensearchEnabledCondition;

import lombok.extern.slf4j.Slf4j;

@Service
@Conditional(OpensearchEnabledCondition.class)
@Slf4j
public class OSDeleteService {

	@Autowired
	private OpenSearchClient javaClient;

	private org.opensearch.client.opensearch.core.DeleteResponse delete(String index, String id) {
		DeleteRequest deleteRequest = new DeleteRequest.Builder().index(index).id(id).build();
		return exececuteDelete(deleteRequest);
	}

	private DeleteByQueryResponse deleteIndexByQuery(String index) {
		DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest.Builder().index(index).build();
		return executeDeleteByQuery(deleteByQueryRequest);
	}

	private DeleteByQueryResponse deleteIndexByQuery(String index, String jsonQuery) {
		Query q = new Query.Builder().queryString(new QueryStringQuery.Builder().query(jsonQuery).build()).build();
		DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest.Builder().index(index).query(q).build();
		return executeDeleteByQuery(deleteByQueryRequest);
	}

	private DeleteResponse exececuteDelete(DeleteRequest deleteRequest) {
		try {
			return javaClient.delete(deleteRequest);
		} catch (IOException e) {
			log.error("Error in delete query ", e);
			return null;
		}
	}

	private DeleteByQueryResponse executeDeleteByQuery(DeleteByQueryRequest deleteByQueryRequest) {
		try {

			return javaClient.deleteByQuery(deleteByQueryRequest);
		} catch (IOException e) {
			log.error("Error in delete by query ", e);
			return null;
		}
	}

	public boolean deleteById(String index, String id) {
		DeleteResponse deleteResponse = delete(index, id);
		// is it necessary to check the result status??
		// return deleteResponse != null &&
		// deleteResponse.status().equals(RestStatus.OK);
		return deleteResponse != null;
	}

	public boolean deleteAll(String index) {
		DeleteByQueryResponse deleteResponse = deleteIndexByQuery(index);
		return deleteResponse != null && deleteResponse.deleted() >= 0;
	}

	public MultiDocumentOperationResult deleteByQuery(String index, String jsonQuery) {
		DeleteByQueryResponse deleteResponse = deleteIndexByQuery(index, jsonQuery);
		MultiDocumentOperationResult result = new MultiDocumentOperationResult();
		if (deleteResponse != null) {
			result.setCount(deleteResponse.deleted());
		} else {
			result.setCount(-1);
		}
		return result;
	}

	public boolean deleteByIdFromTemplate(String indexAlias, String objectId) {
		final Query query = new Query.Builder().ids(new IdsQuery.Builder().values(objectId).build()).build();
		SearchRequest searchRequest = new SearchRequest.Builder().query(query).index(Arrays.asList(indexAlias)).build();
		SearchResponse<Object> response;
		try {
			response = javaClient.search(searchRequest, Object.class);
			if (response != null && response.hits().total().value() >= 1) {
				return deleteById(response.hits().hits().get(0).index(), objectId);
			}
		} catch (OpenSearchException | IOException e) {
			log.error("Error in delete by query. ID not found in alias/template.", e);
			return false;
		}

		// no hits for that ID means no record
		return false;
	}
}
