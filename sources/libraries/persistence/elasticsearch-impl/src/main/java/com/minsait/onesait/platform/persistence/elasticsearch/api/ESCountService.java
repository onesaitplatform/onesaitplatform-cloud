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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.searchbox.core.Count;
import io.searchbox.core.CountResult;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ESCountService {

	@Autowired
	ESBaseApi connector;
	
	private static final String COUNTING_ERROR = "Error counting type ";

	public long getMatchAllQueryCountByType(String type, String... indexes) {
		final String query = ESBaseApi.QUERY_ALL;

		final List<String> list = new ArrayList<>(Arrays.asList(indexes));
		Count count = null;
		if (type == null) {
			count = new Count.Builder().query(query).addIndex(list).build();
		} else {
			count = new Count.Builder().query(query).addIndex(list).addType(type).build();
		}

		CountResult result;
		try {
			result = connector.getHttpClient().execute(count);
			return result.getCount().longValue();
		} catch (final IOException e) {
			log.error(COUNTING_ERROR + e.getMessage());
			return -1;
		}

	}

	public long getMatchAllQueryCount(String... indexes) {
		return getMatchAllQueryCountByType(null, indexes);
	}

	public long getQueryCount(String jsonQueryString, String... indexes) {
		final List<String> list = new ArrayList<>(Arrays.asList(indexes));
		try {
			final CountResult result = connector.getHttpClient()
					.execute(new Count.Builder().addIndex(list).query(jsonQueryString).build());

			return result.getCount().longValue();
		} catch (final IOException e) {
			log.error(COUNTING_ERROR + e.getMessage());
			return -1;
		} catch (final RuntimeException e) {
			final StringBuilder builder = new StringBuilder();
			Arrays.asList(indexes).forEach(i -> builder.append(i));
			log.error(COUNTING_ERROR + builder.toString() + " , index(es) may not exist, returning count = 0");
			return 0;
		}
	}

}