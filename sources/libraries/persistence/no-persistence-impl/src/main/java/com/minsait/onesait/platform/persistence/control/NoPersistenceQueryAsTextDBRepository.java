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
package com.minsait.onesait.platform.persistence.control;

import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.persistence.interfaces.QueryAsTextDBRepository;

import lombok.extern.slf4j.Slf4j;

@Component("NoPersistenceQueryAsTextDBRepository")
@Slf4j
public class NoPersistenceQueryAsTextDBRepository implements QueryAsTextDBRepository {
	private static final String NO_OP_CONTROL_ONTOLOGY = "NO-OP control ontology";

	@Override
	public String queryNativeAsJson(String ontology, String query, int offset, int limit) {
		return queryNativeAsJson(ontology, query);
	}

	@Override
	public String queryNativeAsJson(String ontology, String query) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		return "[]";
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		return queryNativeAsJson(ontology, query);
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset, int limit) {
		return queryNativeAsJson(ontology, query);
	}

}
