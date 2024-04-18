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
package com.minsait.onesait.platform.persistence.opensearch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.persistence.OpensearchEnabledCondition;
import com.minsait.onesait.platform.persistence.interfaces.BasicOpsDBRepository;
import com.minsait.onesait.platform.persistence.interfaces.QueryAsTextDBRepository;

@Component("QueryAsTextOpenSearchDBRepository")
@Scope("prototype")
@Conditional(OpensearchEnabledCondition.class)
public class OpenSearchQueryAsTextDBRepository implements QueryAsTextDBRepository {

	@Autowired
	@Qualifier("OpenSearchBasicOpsDBRepository")
	private BasicOpsDBRepository openSearchBasicOpsDBRepository;

	@Override
	public String queryNativeAsJson(String ontology, String query, int offset, int limit) {
		query = query.replaceAll(ontology, ontology.toLowerCase());
		query = query.replaceAll(ontology.toLowerCase() + "\\.", ontology + "\\.");
		ontology = ontology.toLowerCase();
		return openSearchBasicOpsDBRepository.queryNativeAsJson(ontology, query, offset, limit);
	}

	@Override
	public String queryNativeAsJson(String ontology, String query) {
		query = query.replaceAll(ontology, ontology.toLowerCase());
		query = query.replaceAll(ontology.toLowerCase() + "\\.", ontology + "\\.");
		ontology = ontology.toLowerCase();
		return openSearchBasicOpsDBRepository.queryNativeAsJson(ontology, query);
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset) {
		query = query.replaceAll(ontology, ontology.toLowerCase());
		query = query.replaceAll(ontology.toLowerCase() + "\\.", ontology + "\\.");
		ontology = ontology.toLowerCase();
		return openSearchBasicOpsDBRepository.querySQLAsJson(ontology, query, offset);
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset, int limit) {
		query = query.replaceAll(ontology, ontology.toLowerCase());
		ontology = ontology.toLowerCase();
		return openSearchBasicOpsDBRepository.querySQLAsJson(ontology, query, offset, limit);
	}
}
