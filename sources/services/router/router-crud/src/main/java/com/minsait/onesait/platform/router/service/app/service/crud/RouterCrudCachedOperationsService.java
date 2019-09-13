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
package com.minsait.onesait.platform.router.service.app.service.crud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.persistence.services.BasicOpsPersistenceServiceFacade;
import com.minsait.onesait.platform.persistence.services.QueryToolService;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.QueryType;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@CacheConfig(cacheNames = { "queries" })
public class RouterCrudCachedOperationsService {

	@Autowired
	private QueryToolService queryToolService;

	@Autowired
	private BasicOpsPersistenceServiceFacade basicOpsService;

	@Cacheable("queries")
	public OperationResultModel queryCache(OperationModel operationModel) {

		log.info("Router CACHE EXPLICIT Crud Service Operation {}", operationModel.toString());
		return queryNoCache(operationModel);
	}

	public static boolean nullString(String l) {
		if (l == null)
			return true;
		else
			return (l.equalsIgnoreCase(""));
	}

	public OperationResultModel queryNoCache(OperationModel operationModel) {

		log.info("Router NO CACHING Crud Service Operation {} ", operationModel.toString());

		final OperationResultModel result = new OperationResultModel();

		final String METHOD = operationModel.getOperationType().name();
		final String BODY = operationModel.getBody();
		final String QUERY_TYPE = operationModel.getQueryType().name();
		final String ontologyName = operationModel.getOntologyName();
		final String OBJECT_ID = operationModel.getObjectId();
		final String USER = operationModel.getUser();
		final String CLIENTPLATFORM = operationModel.getDeviceTemplate();

		String OUTPUT = "";
		result.setMessage("OK");
		result.setStatus(true);

		try {
			if (METHOD.equalsIgnoreCase("GET") || METHOD.equalsIgnoreCase(OperationModel.OperationType.QUERY.name())) {

				if (QUERY_TYPE != null) {
					if (QUERY_TYPE.equalsIgnoreCase(QueryType.SQL.name())) {
						// OUTPUT = queryToolService.querySQLAsJson(ontologyName, QUERY, 0);
						OUTPUT = (!nullString(CLIENTPLATFORM))
								? queryToolService.querySQLAsJsonForPlatformClient(CLIENTPLATFORM, ontologyName, BODY,
										0)
								: queryToolService.querySQLAsJson(USER, ontologyName, BODY, 0);
					} else if (QUERY_TYPE.equalsIgnoreCase(QueryType.NATIVE.name())) {
						// OUTPUT = queryToolService.queryNativeAsJson(ontologyName, QUERY, 0,0);
						OUTPUT = (!nullString(CLIENTPLATFORM))
								? queryToolService.queryNativeAsJsonForPlatformClient(CLIENTPLATFORM, ontologyName,
										BODY, 0, 0)
								: queryToolService.queryNativeAsJson(USER, ontologyName, BODY, 0, 0);
					} else {
						OUTPUT = basicOpsService.findById(ontologyName, OBJECT_ID);
					}
				} else {
					OUTPUT = basicOpsService.findById(ontologyName, OBJECT_ID);
				}
			}
		} catch (final Exception e) {
			result.setResult(OUTPUT);
			result.setStatus(false);
			result.setMessage(e.getMessage());
		}

		result.setResult(OUTPUT);
		result.setOperation(METHOD);
		return result;
	}
}
