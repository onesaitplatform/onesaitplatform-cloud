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

import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.commons.model.MultiDocumentOperationResult;
import com.minsait.onesait.platform.persistence.ElasticsearchEnabledCondition;

import lombok.extern.slf4j.Slf4j;

@Service
@Conditional(ElasticsearchEnabledCondition.class)
@Slf4j
public class ESDeleteService {

    @Autowired
    private RestHighLevelClient hlClient;
	
	
	private DeleteResponse delete(String index, String id) {
	    DeleteRequest deleteRequest = new DeleteRequest(index, id);
	    return exececuteDelete(deleteRequest);
	}
	
	private BulkByScrollResponse deleteIndexByQuery(String index) {
	    DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(index);
	    deleteByQueryRequest.setQuery(QueryBuilders.matchAllQuery());
	    return executeDeleteByQuery(deleteByQueryRequest);
	}
	
	private BulkByScrollResponse deleteIndexByQuery(String index, String jsonQuery) {
        DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(index);
        deleteByQueryRequest.setQuery(QueryBuilders.wrapperQuery(jsonQuery));
        return executeDeleteByQuery(deleteByQueryRequest);
    }
	
	private DeleteResponse exececuteDelete(DeleteRequest deleteRequest) {
	    try {
            return hlClient.delete(deleteRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("Error in delete query ", e);
            return null;
        }
	}
	
	private BulkByScrollResponse executeDeleteByQuery(DeleteByQueryRequest deleteByQueryRequest) {
	    try {
            return hlClient.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("Error in delete by query ", e);
            return null;
        }
	}

	public boolean deleteById(String index, String id) {
	    DeleteResponse deleteResponse = delete(index, id);
	    return deleteResponse != null && deleteResponse.status().equals(RestStatus.OK);
	}

	public boolean deleteAll(String index) {
	    BulkByScrollResponse deleteResponse = deleteIndexByQuery(index);
	    return deleteResponse != null && deleteResponse.getDeleted() >= 0;
	}

	public MultiDocumentOperationResult deleteByQuery(String index, String jsonQuery) {
	    BulkByScrollResponse deleteResponse = deleteIndexByQuery(index, jsonQuery);	    
		MultiDocumentOperationResult result = new MultiDocumentOperationResult();
		if (deleteResponse != null) {
		    result.setCount(deleteResponse.getDeleted());
		} else {
		    result.setCount(-1);
		}
		return result;
	}
}