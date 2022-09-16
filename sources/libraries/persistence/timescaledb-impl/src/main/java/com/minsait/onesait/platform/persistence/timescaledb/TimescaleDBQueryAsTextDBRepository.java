/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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
package com.minsait.onesait.platform.persistence.timescaledb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.persistence.interfaces.QueryAsTextDBRepository;

@Component
public class TimescaleDBQueryAsTextDBRepository implements QueryAsTextDBRepository {

	@Autowired
	private TimescaleDBBasicOpsDBRepository timescaleDBBasicOpsRepository;

	@Override
	public String queryNativeAsJson(String ontology, String query, int offset, int limit) {
		return timescaleDBBasicOpsRepository.queryNativeAsJson(ontology, query, offset, limit);
	}

	@Override
	public String queryNativeAsJson(String ontology, String query) {
		return timescaleDBBasicOpsRepository.queryNativeAsJson(ontology, query);
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset) {
		return timescaleDBBasicOpsRepository.querySQLAsJson(ontology, query, offset);
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset, int limit) {

		return timescaleDBBasicOpsRepository.querySQLAsJson(ontology, query, offset, limit);
	}

}
