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

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.persistence.ElasticsearchEnabledCondition;

import lombok.extern.slf4j.Slf4j;

@Service
@Conditional(ElasticsearchEnabledCondition.class)
@Slf4j
public class ESCountService {

	@Autowired
	ESBaseApi connector;
	
	@Autowired
	private RestHighLevelClient hlClient;
	
	private static final String COUNTING_ERROR = "Error in count query ";
	
	private CountResponse count(QueryBuilder queryBuilder, String... indices) {
	    CountRequest countRequest = new CountRequest(indices);
	    countRequest.query(queryBuilder);
	    return executeCount(countRequest, indices);
	}
	
	private CountResponse executeCount(CountRequest countRequest, String... indices) {
	    try {
            return hlClient.count(countRequest, RequestOptions.DEFAULT);         
        } catch (final IOException e) {
            log.error(COUNTING_ERROR + e.getMessage());
            return null;
        }
	}

	public long getMatchAllQueryCount(String... indices) {
	    CountResponse countResponse = count(QueryBuilders.matchAllQuery(), indices);
	    return countResponse == null ? -1 : countResponse.getCount();		
	}

	public long getQueryCount(String jsonQueryString, String... indices) {
	    CountResponse countResponse = count(QueryBuilders.wrapperQuery(jsonQueryString), indices);
	    return countResponse == null ? -1 : countResponse.getCount();
	}

}