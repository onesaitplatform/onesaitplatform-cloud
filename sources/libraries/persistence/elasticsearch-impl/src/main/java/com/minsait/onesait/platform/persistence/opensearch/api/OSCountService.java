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

import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.core.CountRequest;
import org.opensearch.client.core.CountResponse;
import org.opensearch.index.query.QueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.persistence.OpensearchEnabledCondition;

import lombok.extern.slf4j.Slf4j;

@Service
@Conditional(OpensearchEnabledCondition.class)
@Slf4j
public class OSCountService {

	@Autowired
	OSBaseApi connector;
	
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
