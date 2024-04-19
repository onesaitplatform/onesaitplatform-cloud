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
package com.minsait.onesait.platform.persistence.cosmosdb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.commons.model.MultiDocumentOperationResult;
import com.minsait.onesait.platform.persistence.cosmosdb.utils.sql.CosmosDBSQLUtils;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.interfaces.QueryAsTextDBRepository;

import lombok.extern.slf4j.Slf4j;

@Component("CosmosDBQueryAsTextDBRepository")
@Slf4j
public class CosmosDBQueryAsTextDBRepository implements QueryAsTextDBRepository {

	@Autowired
	private CosmosDBBasicOpsDBRepository cosmosDBBasicOpsRepository;

	@Autowired
	private CosmosDBSQLUtils sqlUtils;

	@Override
	public String queryNativeAsJson(String ontology, String query, int offset, int limit) {
		query = query + " OFFSET " + offset + " LIMIT " + limit;
		return cosmosDBBasicOpsRepository.querySQLAsJson(ontology, query);

	}

	@Override
	public String queryNativeAsJson(String ontology, String query) {
		return cosmosDBBasicOpsRepository.querySQLAsJson(ontology, query);
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset) {
		if (sqlUtils.isSelect(query))
			return cosmosDBBasicOpsRepository.querySQLAsJson(ontology, query, offset);
		else if (sqlUtils.isDelete(query)) {
			final MultiDocumentOperationResult result = cosmosDBBasicOpsRepository.deleteNative(ontology, query, true);
			return String.valueOf(result.getCount());
		} else if (sqlUtils.isUpdate(query)) {
			final MultiDocumentOperationResult result = cosmosDBBasicOpsRepository.updateNative(ontology, query, true);
			return String.valueOf(result.getCount());
		} else {
			throw new DBPersistenceException("Not supported SQL operation");
		}
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset, int limit) {
		if (sqlUtils.isSelect(query))
			return cosmosDBBasicOpsRepository.querySQLAsJson(ontology, query, offset, limit);
		else if (sqlUtils.isDelete(query)) {
			final MultiDocumentOperationResult result = cosmosDBBasicOpsRepository.deleteNative(ontology, query, true);
			return String.valueOf(result.getCount());
		} else if (sqlUtils.isUpdate(query)) {
			final MultiDocumentOperationResult result = cosmosDBBasicOpsRepository.updateNative(ontology, query, true);
			return String.valueOf(result.getCount());
		} else {
			throw new DBPersistenceException("Not supported SQL operation");
		}
	}

}
