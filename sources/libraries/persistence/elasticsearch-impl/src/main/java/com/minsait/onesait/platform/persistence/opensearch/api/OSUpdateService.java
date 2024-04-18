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
import java.io.Reader;
import java.io.StringReader;
import java.util.concurrent.ExecutionException;

import org.opensearch.client.json.JsonData;
import org.opensearch.client.json.JsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.InlineScript;
import org.opensearch.client.opensearch._types.Result;
import org.opensearch.client.opensearch._types.Script;
import org.opensearch.client.opensearch._types.query_dsl.MatchAllQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.QueryStringQuery;
import org.opensearch.client.opensearch.core.UpdateByQueryRequest;
import org.opensearch.client.opensearch.core.UpdateByQueryResponse;
import org.opensearch.client.opensearch.core.UpdateRequest;
import org.opensearch.client.opensearch.core.UpdateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.minsait.onesait.platform.config.model.OntologyElastic;
import com.minsait.onesait.platform.persistence.OpensearchEnabledCondition;

import jakarta.json.spi.JsonProvider;
import lombok.extern.slf4j.Slf4j;

@Service
@Conditional(OpensearchEnabledCondition.class)
@Slf4j
public class OSUpdateService {

	@Autowired
	private OpenSearchClient javaClient;

	private UpdateResponse<Object> update(String index, String id, JsonData jd) {
		UpdateRequest<Object, Object> updateReq = new UpdateRequest.Builder<>().id(id).doc(jd).docAsUpsert(false)
				.index(index).build();
		return executeUpdate(updateReq);
	}

	private UpdateByQueryResponse updateIndexByQuery(String index, String jsonScript) {
		Query q = new Query.Builder().matchAll(new MatchAllQuery.Builder().build()).build();
		Script script = new Script.Builder().inline(new InlineScript.Builder().source(jsonScript).build()).build();
		final UpdateByQueryRequest updateRequest = new UpdateByQueryRequest.Builder().script(script).query(q).build();
		return executeUpdateByQuery(updateRequest);
	}

	private UpdateByQueryResponse updateIndexByQuery(String index, String jsonScript, String jsonQuery) {
		Query q = new Query.Builder().queryString(new QueryStringQuery.Builder().query(jsonQuery).build()).build();
		Script script = new Script.Builder().inline(new InlineScript.Builder().source(jsonScript).build()).build();
		final UpdateByQueryRequest updateRequest = new UpdateByQueryRequest.Builder().script(script).query(q).build();
		return executeUpdateByQuery(updateRequest);
	}

	private UpdateResponse<Object> executeUpdate(UpdateRequest<Object, Object> updateRequest) {
		try {
			return javaClient.update(updateRequest, Object.class);
		} catch (final IOException e) {
			log.error("Error in update query ", e);
			return null;
		}
	}

	private UpdateByQueryResponse executeUpdateByQuery(UpdateByQueryRequest updateByQueryRequest) {
		try {
			return javaClient.updateByQuery(updateByQueryRequest);
		} catch (final IOException e) {
			log.error("Error in delete by query ", e);
			return null;
		}
	}

	public boolean updateIndex(String index, String id, String jsonString) {
		// we need to map the jsonString to a json object, otherwise the parser will
		// scape "quote" chars and end in parse error.
		JsonpMapper jsonpMapper = javaClient._transport().jsonpMapper();
		JsonProvider jsonProvider = jsonpMapper.jsonProvider();
		Reader reader = new StringReader(jsonString);
		JsonData jd = JsonData.from(jsonProvider.createParser(reader), jsonpMapper);
		final UpdateResponse<Object> updateResponse = update(index, id, jd);
		return updateResponse == null || updateResponse.result() == Result.NotFound ? false : true;
	}

	public long updateAll(String index, String jsonScript) throws InterruptedException, ExecutionException {
		final UpdateByQueryResponse updateIndexByQuery = updateIndexByQuery(index, jsonScript);
		return updateIndexByQuery == null ? -1 : updateIndexByQuery.updated();
	}

	public long updateByQueryAndFilter(String index, String jsonScript, String jsonQuery) {
		final UpdateByQueryResponse updateIndexByQuery = updateIndexByQuery(index, jsonScript, jsonQuery);
		return updateIndexByQuery == null ? -1 : updateIndexByQuery.updated();
	}

	public boolean updateIndexFromTemplate(OntologyElastic ontology, String id, String jsonString) {

		final JsonObject instanceObject = new JsonParser().parse(jsonString).getAsJsonObject();
		final String index = OSTemplateHelper.getIndexFromInstance(ontology, instanceObject);
		return updateIndex(index, id, jsonString);
	}
}
