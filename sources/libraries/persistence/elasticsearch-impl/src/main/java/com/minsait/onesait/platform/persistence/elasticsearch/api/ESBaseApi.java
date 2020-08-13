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
package com.minsait.onesait.platform.persistence.elasticsearch.api;

import java.io.IOException;

import javax.annotation.PostConstruct;

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
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.persistence.ElasticsearchEnabledCondition;

import lombok.extern.slf4j.Slf4j;

@Service
@Conditional(ElasticsearchEnabledCondition.class)
@Slf4j
public class ESBaseApi {

	@Autowired
	private RestHighLevelClient hlClient;

	@PostConstruct
	void initializeIt() {

	}

	public boolean createIndex(String index) {
		try {
		    CreateIndexRequest request = new CreateIndexRequest(index);
		    request.settings(Settings.builder()); //TODO add advanced index creation: shards, replicas, etc.
		    
		    //TODO mappings for properties can be provided
		    
		    CreateIndexResponse createIndexResponse = hlClient.indices().create(request, RequestOptions.DEFAULT);

		    //TODO change return data or use void
		    return createIndexResponse.isAcknowledged();
		} catch (IOException e) {
			log.error("Error Creating Index " + e.getMessage());
			return false;
		}
	}

	public String[] getIndexes() {		
		try {
		    GetIndexRequest request = new GetIndexRequest("*");
		    GetIndexResponse response = hlClient.indices().get(request, RequestOptions.DEFAULT);
		    return response.getIndices();
		} catch (IOException e) {
			log.error("Error getIndexes ", e);
			return new String[0];
		}
	}

	//TODO check if this method was thought for documents or indices
	public String updateDocument(String index, String id, String jsonData) {
		try {
		    UpdateRequest request = new UpdateRequest(index, id);
		    request.doc(jsonData, XContentType.JSON);
		    UpdateResponse updateResponse = hlClient.update(
		            request, RequestOptions.DEFAULT);
		    return updateResponse.getResult().toString();
		} catch (IOException e) {
			log.error("UpdateIndex", e);
			return null;
		}
	}

	public boolean deleteIndex(String index) {
	    DeleteIndexRequest request = new DeleteIndexRequest(index);
		try {
		    AcknowledgedResponse deleteIndexResponse = hlClient.indices().delete(request, RequestOptions.DEFAULT);
			log.info("Delete index result :" + deleteIndexResponse.isAcknowledged());
			return deleteIndexResponse.isAcknowledged();
		} catch (IOException e) {
			log.error("Error Deleting Type " + e.getMessage());
			return false;
		}
	}

	public boolean prepareIndex(String index, String dataMapping) throws IOException {
	    
	    PutMappingRequest request = new PutMappingRequest(index);
	    request.source(dataMapping, XContentType.JSON);
	    AcknowledgedResponse putMappingResponse = hlClient.indices().putMapping(request, RequestOptions.DEFAULT);
	    return putMappingResponse.isAcknowledged();
	    
	}

}
