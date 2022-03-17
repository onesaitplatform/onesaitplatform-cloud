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
package com.minsait.onesait.platform.persistence.hadoop.kudu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.hadoop.config.condition.HadoopEnabledCondition;
import com.minsait.onesait.platform.persistence.hadoop.util.HadoopQueryProcessor;
import com.minsait.onesait.platform.persistence.interfaces.QueryAsTextDBRepository;
import com.minsait.onesait.platform.persistence.models.ErrorResult;

import lombok.extern.slf4j.Slf4j;

@Component("KuduQueryAsTextDBRepository")
@Scope("prototype")
@Lazy
@Slf4j
@Conditional(HadoopEnabledCondition.class)
public class KuduQueryAsTextDBRepository implements QueryAsTextDBRepository {

	@Autowired
	KuduBasicOpsDBRepository kuduBasicOpsDBRepository;

	@Autowired
	private HadoopQueryProcessor queryProcessor;

	private static final String UPDATE_KUDU_PREFIX = "update";
	private static final String DELETE_KUDU_PREFIX = "delete";

	@Override
	public String queryNativeAsJson(String ontology, String query, int offset, int limit) {
		try {
			String queryLC = query.toLowerCase();
			String procQuery = queryProcessor.parse(query);

			if (queryLC.startsWith(UPDATE_KUDU_PREFIX)) {
				return kuduBasicOpsDBRepository.updateNative(ontology, procQuery, false).toString();
			} else if (queryLC.startsWith(DELETE_KUDU_PREFIX)) {
				return kuduBasicOpsDBRepository.deleteNative(ontology, procQuery, false).toString();
			} else {
				return kuduBasicOpsDBRepository.queryNativeAsJson(ontology, procQuery);
			}
		} catch (Exception e) {
			log.error("Error in queryNativeAsJson", e);
			throw new DBPersistenceException(new ErrorResult(ErrorResult.PersistenceType.KUDU, e.getMessage()),
					e.getMessage());
		}
	}

	@Override
	public String queryNativeAsJson(String ontology, String query) {
		return queryNativeAsJson(ontology, query, -1, -1);
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset) {
		return queryNativeAsJson(ontology, query, offset, -1);
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset, int limit) {
		return queryNativeAsJson(ontology, query, offset, limit);
	}

}
