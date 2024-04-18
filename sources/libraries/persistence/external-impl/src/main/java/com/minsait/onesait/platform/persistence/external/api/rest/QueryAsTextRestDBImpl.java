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
package com.minsait.onesait.platform.persistence.external.api.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.interfaces.QueryAsTextDBRepository;

import lombok.extern.slf4j.Slf4j;

@Component("QueryAsTextRestDBImpl")
@Scope("prototype")
@Slf4j
public class QueryAsTextRestDBImpl implements QueryAsTextDBRepository {

	@Autowired
	private ExternalApiRestOpsDBRepository apiRestOntologyOps;

	private static final String ERRORQUERYNATIVEASJSON = "Error queryNativeAsJson: {}";

	@Override
	public String queryNativeAsJson(String ontology, String query, int offset, int limit) {
		try {
			return apiRestOntologyOps.queryNativeAsJson(ontology, query, offset, limit);
		} catch (Exception e) {
			log.error(ERRORQUERYNATIVEASJSON, e.getMessage());
			throw new DBPersistenceException(e);
		}
	}

	@Override
	public String queryNativeAsJson(String ontology, String query) {
		try {
			return apiRestOntologyOps.queryNativeAsJson(ontology, query);
		} catch (Exception e) {
			log.error(ERRORQUERYNATIVEASJSON, e.getMessage());
			throw new DBPersistenceException(e);
		}
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset) {
		try {
			return apiRestOntologyOps.querySQLAsJson(ontology, query, offset);
		} catch (Exception e) {
			log.error(ERRORQUERYNATIVEASJSON, e.getMessage());
			throw new DBPersistenceException(e);
		}
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset, int limit) {
		try {
			return apiRestOntologyOps.querySQLAsJson(ontology, query, offset);
		} catch (Exception e) {
			log.error(ERRORQUERYNATIVEASJSON, e.getMessage());
			throw new DBPersistenceException(e);
		}
	}
}
