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
import java.util.Arrays;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.query_dsl.MatchAllQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.QueryStringQuery;
import org.opensearch.client.opensearch.core.CountRequest;
import org.opensearch.client.opensearch.core.CountResponse;
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
	private OpenSearchClient javaClient;

	private static final String COUNTING_ERROR = "Error in count query ";

	private CountResponse count(Query query, String... indices) {
		CountRequest countRequest = new CountRequest.Builder().index(Arrays.asList(indices)).query(query).build();
		return executeCount(countRequest);
	}

	private CountResponse executeCount(CountRequest countRequest) {
		try {
			return javaClient.count(countRequest);
		} catch (final IOException e) {
			log.error(COUNTING_ERROR + e.getMessage());
			return null;
		}
	}

	public long getMatchAllQueryCount(String... indices) {
		final Query q = new Query.Builder().matchAll(new MatchAllQuery.Builder().build()).build();
		CountResponse countResponse = count(q, indices);
		return countResponse == null ? -1 : countResponse.count();
	}

	public long getQueryCount(String jsonQueryString, String... indices) {
		final Query q = new Query.Builder().queryString(new QueryStringQuery.Builder().query(jsonQueryString).build())
				.build();
		CountResponse countResponse = count(q, indices);
		return countResponse == null ? -1 : countResponse.count();
	}
}
