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
package com.minsait.onesait.platform.business.services.audit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataUnauthorizedException;
import com.minsait.onesait.platform.config.services.utils.ServiceUtils;
import com.minsait.onesait.platform.persistence.services.QueryToolService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuditServiceImpl implements AuditService {

	@Autowired
	private QueryToolService queryToolService;
	
	@Override
	public String getUserAuditData(String resultType, String modulesname, String operation,	String offset, String user) throws Exception {

		String where = getWhereForQuery(resultType, modulesname, operation);
		try {
			return getResultForQuery(user, where, offset);
		} catch (final Exception e) {
			log.error("Error getting audit of user {}", user);
			throw e;
		}
	}
	
	private String getResultForQuery(String user, String where, String offset) throws OntologyDataUnauthorizedException, GenericOPException {

		final String collection = ServiceUtils.getAuditCollectionName(user);

		String query = "select message, type, user, formatedTimeStamp, module, ontology, operationType, resultOperation, data from "
				+ collection;

		if (!where.equalsIgnoreCase("")) {
			query += " WHERE " + where;
		}
		
		if (offset.equals("")) {
			offset="50";
		}

		query += " order by timeStamp desc limit " + Integer.parseInt(offset);

		return queryToolService.querySQLAsJson(user, collection, query, 0);

	}
	
	private String getWhereForQuery(String resultOperation, String module, String operation) {
		String where = "";
		
		if (!resultOperation.equalsIgnoreCase("all")) {
			where += " resultOperation = \"" + resultOperation + "\"";
		}
		if (!module.equalsIgnoreCase("all")) {
			if (!where.equals("")) {
				where += " and";
			}
			where += " module = \"" + module + "\"";
		}
		if (!operation.equalsIgnoreCase("all")) {
			if (!where.equals("")) {
				where += " and";
			}
			where += " operationType = \"" + operation + "\"";
		}
		return where;
	}
}
