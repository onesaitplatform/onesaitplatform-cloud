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
package com.minsait.onesait.platform.business.services.datasources.solver;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.business.services.datasources.dto.FilterStt;
import com.minsait.onesait.platform.business.services.datasources.dto.OrderByStt;
import com.minsait.onesait.platform.business.services.datasources.dto.ParamStt;
import com.minsait.onesait.platform.business.services.datasources.dto.ProjectStt;
import com.minsait.onesait.platform.business.services.datasources.exception.DashboardEngineException;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataUnauthorizedException;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.services.QueryToolService;

@Component
@Qualifier("NebulaGraphSolver")
public class SolverNebulaGraphImpl implements SolverInterface {

	private static final Logger log = LoggerFactory.getLogger(SolverNebulaGraphImpl.class);
	@Autowired
	QueryToolService qts;

	private Pattern matchquery = Pattern.compile(".* (where .*) return .*", Pattern.CASE_INSENSITIVE);

	@Override
	public String buildQueryAndSolve(String query, int maxreg, List<FilterStt> where, List<ProjectStt> project,
			List<String> group, List<OrderByStt> sort, long offset, long limit, List<ParamStt> param, boolean debug,
			String executeAs, String ontology, boolean isSimpleMode)
			throws DashboardEngineException, OntologyDataUnauthorizedException, GenericOPException {
		try {
			return qts.querySQLAsJson(executeAs, ontology, buildQuery(query, maxreg, where, project, group, sort,
					offset, limit, param, debug, executeAs, ontology, isSimpleMode), 0);
		} catch (DBPersistenceException e) {
			throw new DashboardEngineException(DashboardEngineException.Error.GENERIC_EXCEPTION,
					e.getDetailedMessage());
		}
	}

	@Override
	public String buildQuery(String query, int maxreg, List<FilterStt> where, List<ProjectStt> project,
			List<String> group, List<OrderByStt> sort, long offset, long limit, List<ParamStt> param, boolean debug,
			String executeAs, String ontology, boolean isSimpleMode) {

		String processedQuery;
		String trimQuery = query.replaceAll("\\t|\\r|\\r\\n\\t|\\n|\\r\\t", " ");
		trimQuery = trimQuery.trim().replaceAll(" +", " ");
		trimQuery = trimQuery.replaceAll(",", ", ");

		log.info("Original Query: {}", trimQuery);

		String trimParamsQuery = processQueryParams(trimQuery, param);
		String queryWithFilter = processWhere(trimParamsQuery, where);

		log.info("Params overwrite Query and filter: {}", queryWithFilter);
		processedQuery = queryWithFilter;

		return processedQuery;

	}

	private String processWhere(String query, List<FilterStt> filters) {

		if (filters == null || filters.isEmpty()) {
			return query;
		} else {

			Matcher matcher = matchquery.matcher(query);
			String whereStr = buildStringFilter(filters);

			if (matcher.find()) {
				query = query.replace(matcher.group(1), matcher.group(1) + " and " + whereStr);
			} else {
				int indexreturn = query.toLowerCase().indexOf(" return ");
				query = query.substring(0, indexreturn) + " where " + whereStr + query.substring(indexreturn + 1);
			}

			return query;
		}
	}

	private String buildStringFilter(List<FilterStt> filters) {
		String whereStr = "";
		for (FilterStt f : filters) {
			String fStr = buildStringFromFilter(f);
			if ("".equals(whereStr)) {
				whereStr = fStr + " ";
			} else {
				whereStr = " and " + fStr + " ";
			}
		}
		return whereStr;
	}

	private String buildStringFromFilter(FilterStt f) {
		StringBuilder sb = new StringBuilder();
		sb.append(f.getField());
		sb.append(" ");
		String op = f.getOp();
		if ("=".equals(op)) {
			sb.append("==");
		} else {
			sb.append(f.getOp());
		}
		sb.append(" ");
		sb.append(f.getExp());
		return sb.toString();
	}

	private String processQueryParams(String trimquery, List<ParamStt> params) {
		if (params != null && !params.isEmpty()) {
			for (ParamStt param : params) {
				trimquery = trimquery.replaceAll("\\{\\$" + param.getField() + "\\}", param.getValue());
			}
		}
		return trimquery;
	}
}
