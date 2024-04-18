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
package com.minsait.onesait.platform.persistence.elasticsearch.api;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.script.Script;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.persistence.ElasticsearchEnabledCondition;

import lombok.extern.slf4j.Slf4j;

@Service
@Conditional(ElasticsearchEnabledCondition.class)
@Slf4j
public class ESUpdateService {

	@Autowired
	private RestHighLevelClient hlClient;

	private UpdateResponse update(String index, String id, String json) {
		final UpdateRequest updateRequest = new UpdateRequest(index, id);
		updateRequest.doc(json, XContentType.JSON);
		return executeUpdate(updateRequest);
	}

	private BulkByScrollResponse updateIndexByQuery(String index, String jsonScript) {
		final UpdateByQueryRequest updateByQueryRequest = new UpdateByQueryRequest(index);
		updateByQueryRequest.setQuery(QueryBuilders.matchAllQuery());
		updateByQueryRequest.setScript(new Script(jsonScript));
		return executeUpdateByQuery(updateByQueryRequest);
	}

	private BulkByScrollResponse updateIndexByQuery(String index, String jsonScript, String jsonQuery) {
		final UpdateByQueryRequest updateByQueryRequest = new UpdateByQueryRequest(index);
		updateByQueryRequest.setQuery(QueryBuilders.wrapperQuery(jsonQuery));
		updateByQueryRequest.setScript(new Script(jsonScript));
		return executeUpdateByQuery(updateByQueryRequest);
	}

	private UpdateResponse executeUpdate(UpdateRequest updateRequest) {
		try {
			return hlClient.update(updateRequest, RequestOptions.DEFAULT);
		} catch (final IOException e) {
			log.error("Error in update query ", e);
			return null;
		}
	}

	private BulkByScrollResponse executeUpdateByQuery(UpdateByQueryRequest updateByQueryRequest) {
		try {
			return hlClient.updateByQuery(updateByQueryRequest, RequestOptions.DEFAULT);
		} catch (final IOException e) {
			log.error("Error in delete by query ", e);
			return null;
		}
	}

	public boolean updateIndex(String index, String id, String jsonString) {
		final UpdateResponse updateResponse = update(index, id, jsonString);
		return updateResponse == null || updateResponse.getResult() == DocWriteResponse.Result.NOT_FOUND ? false : true;
	}

	public long updateAll(String index, String jsonScript) throws InterruptedException, ExecutionException {
		final BulkByScrollResponse updateIndexByQuery = updateIndexByQuery(index, jsonScript);
		return updateIndexByQuery == null ? -1 : updateIndexByQuery.getUpdated();
	}

	public long updateByQueryAndFilter(String index, String jsonScript, String jsonQuery) {
		final BulkByScrollResponse updateIndexByQuery = updateIndexByQuery(index, jsonScript, jsonQuery);
		return updateIndexByQuery == null ? -1 : updateIndexByQuery.getUpdated();
	}
}