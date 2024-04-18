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
package com.minsait.onesait.platform.solver;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataUnauthorizedException;
import com.minsait.onesait.platform.dto.socket.FilterStt;
import com.minsait.onesait.platform.dto.socket.ProjectStt;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.services.QueryToolService;

@Component
@Qualifier("ElasticSolver")
public class SolverElasticImpl implements SolverInterface {

	private static final Logger log = LoggerFactory.getLogger(SolverElasticImpl.class);

	private static final String LIMIT_STR = "limit ";
	private static final String SP_LIMIT_STR = " limit ";

	@Autowired
	QueryToolService qts;

	private static String filterSeparator = " and ";

	@Override
	public String buildQueryAndSolve(String query, int maxreg, List<FilterStt> where, List<ProjectStt> project,
			List<String> group, String executeAs, String ontology)
			throws DBPersistenceException, OntologyDataUnauthorizedException, GenericOPException {
		StringBuilder sb = new StringBuilder();
		int indexLimit = query.toLowerCase().lastIndexOf(LIMIT_STR);
		if (where == null || where.size() == 0) {
			if (indexLimit == -1) {
				sb.append(query);
				sb.append(SP_LIMIT_STR + maxreg);
			} else {
				sb.append(query.substring(0, indexLimit));
				sb.append(SP_LIMIT_STR + maxreg);
			}
		} else {
			String elsWhere = buildWhere(where);
			int indexWhere = query.toLowerCase().lastIndexOf("where ");
			if (indexWhere == -1) {
				int indexGroup = query.toLowerCase().lastIndexOf("group by ");
				int indexOrder = query.toLowerCase().lastIndexOf("order by ");
				if (indexGroup == -1) {
					indexWhere = indexOrder != -1 ? indexOrder : (indexLimit != -1 ? indexLimit : query.length());
				} else {
					indexWhere = indexGroup;
				}

				sb.append(query.substring(0, indexWhere));
				sb.append(" where  " + elsWhere + " ");

			} else {
				indexWhere += 6;
				sb.append(query.substring(0, indexWhere));
				sb.append(elsWhere + " " + filterSeparator + " ");

			}
			if (indexWhere < query.length()) {
				sb.append(query.substring(indexWhere));
			}

			// Index limit over new query
			String auxNoLimit = sb.toString();
			indexLimit = auxNoLimit.toLowerCase().lastIndexOf(LIMIT_STR);

			if (indexLimit == -1) {
				sb.append(SP_LIMIT_STR + maxreg);
			} else {
				indexLimit = auxNoLimit.toLowerCase().lastIndexOf(LIMIT_STR);
				sb = new StringBuilder();
				sb.append(auxNoLimit.substring(0, indexLimit));
				sb.append(SP_LIMIT_STR + maxreg);
			}
		}

		String processedQuery = sb.toString();

		log.info("Quasar SQL execute query: " + processedQuery);

		return qts.querySQLAsJson(executeAs, ontology, processedQuery, 0);
	}

	private String buildWhere(List<FilterStt> filters) {
		if (filters == null || filters.size() == 0) {
			return "";
		} else {
			StringBuilder sb = new StringBuilder();
			for (FilterStt f : filters) {
				sb.append(f.getField());
				sb.append(" ");
				sb.append(f.getOp());
				sb.append(" ");
				sb.append(f.getExp());
				sb.append(filterSeparator);
			}
			return sb.substring(0, sb.length() - filterSeparator.length()).toString();
		}
	}
}
