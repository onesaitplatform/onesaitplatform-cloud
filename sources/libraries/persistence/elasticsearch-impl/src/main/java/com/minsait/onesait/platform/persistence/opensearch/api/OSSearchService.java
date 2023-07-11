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
package com.minsait.onesait.platform.persistence.opensearch.api;

import java.io.IOException;
import java.util.List;

import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.index.query.QueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.persistence.OpensearchEnabledCondition;
import com.minsait.onesait.platform.persistence.opensearch.OpenSearchUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Conditional(OpensearchEnabledCondition.class)
@Slf4j
public class OSSearchService {


	@Autowired
	private RestHighLevelClient hlClient;
	
	private SearchResponse search(QueryBuilder queryBuilder, String...indices) {        
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); 
        searchSourceBuilder.query(queryBuilder);         
        return executeSearch(searchSourceBuilder, indices);
	}
	
	private SearchResponse search(QueryBuilder queryBuilder, int size, String...indices) {        
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); 
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.size(size);
        return executeSearch(searchSourceBuilder, indices);
    }
	
	private SearchResponse search(QueryBuilder queryBuilder, int from, int size, String...indices) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); 
        searchSourceBuilder.query(queryBuilder); 
        searchSourceBuilder.size(size);
        searchSourceBuilder.from(from);        
        return executeSearch(searchSourceBuilder, indices);
    }

    private SearchResponse executeSearch(SearchSourceBuilder searchSourceBuilder, String... indices) {
        SearchRequest searchRequest = new SearchRequest(indices);
        searchRequest.source(searchSourceBuilder);              
        try {                       
            return hlClient.search(searchRequest, RequestOptions.DEFAULT);                             
        } catch (final IOException e) {
            log.error("Error in search query ", e);
            return null;
        }
    }	

	public List<String> findQueryData(String jsonQuery, String... indices) {	    
		SearchResponse searchResponse = search(QueryBuilders.wrapperQuery(jsonQuery), indices);
		return OpenSearchUtil.processSearchResponseToStringList(searchResponse);
	}    

	public String findQueryDataAsJson(String jsonQuery, String... indices) {	    
		SearchResponse searchResponse = search(QueryBuilders.wrapperQuery(jsonQuery), indices);		
		return OpenSearchUtil.processSearchResponseToJson(searchResponse);
	}

	public String findByIndex(String index, String documentId) {	   
	    SearchResponse searchResponse = search(QueryBuilders.idsQuery().addIds(documentId), index);	    
	    return OpenSearchUtil.processSearchResponseToJson(searchResponse);
	}

	public List<String> findAll(String index) {
		SearchResponse searchResponse = search(QueryBuilders.matchAllQuery(), index);
		return OpenSearchUtil.processSearchResponseToStringList(searchResponse);
	}

	public List<String> findAll(String index, int from, int size) {
	    SearchResponse searchResponse = search(QueryBuilders.matchAllQuery(), from, size, index);
	    return OpenSearchUtil.processSearchResponseToStringList(searchResponse);
	}

	public List<String> findAll(String index, String jsonQuery, int from, int size) {
	    SearchResponse searchResponse = search(QueryBuilders.wrapperQuery(jsonQuery), from, size, index);
        return OpenSearchUtil.processSearchResponseToStringList(searchResponse);
	}

	public List<String> findAll(String index, int size) {
	    SearchResponse searchResponse = search(QueryBuilders.matchAllQuery(), size, index);
        return OpenSearchUtil.processSearchResponseToStringList(searchResponse);
	}

	public String findAllAsJson(String index, int size) {
	    SearchResponse searchResponse = search(QueryBuilders.matchAllQuery(), size, index);
        return OpenSearchUtil.processSearchResponseToJson(searchResponse);
	}

	public String findAllAsJson(String index, int from, int size) {
        SearchResponse searchResponse = search(QueryBuilders.matchAllQuery(), from, size, index);
        return OpenSearchUtil.processSearchResponseToJson(searchResponse);	    
	}
	
}
