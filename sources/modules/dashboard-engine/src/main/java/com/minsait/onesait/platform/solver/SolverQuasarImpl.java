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

import com.minsait.onesait.platform.dto.socket.FilterStt;
import com.minsait.onesait.platform.dto.socket.ProjectStt;
import com.minsait.onesait.platform.persistence.services.QueryToolService;

@Component
@Qualifier("QuasarSolver")
public class SolverQuasarImpl implements SolverInterface {

	private static final Logger log = LoggerFactory.getLogger(SolverQuasarImpl.class);

	private static final String LIMIT_STR = "limit ";
	private static final String SP_LIMIT_STR = " limit ";

	@Autowired
	QueryToolService qts;

	private static String filterSeparator = " and ";
	private static String solvedQueryPrefix = "Solved.";

	@Override
	public String buildQueryAndSolve(String query, int maxreg, List<FilterStt> where, List<ProjectStt> project,
			List<String> group, String executeAs, String ontology) {
		String processedQuery;
		String trimQuery = query.replaceAll("\\t|\\r|\\r\\n\\t|\\n|\\r\\t", " ");
		trimQuery = trimQuery.trim().replaceAll(" +", " ");
		if (isSimpleDatasource(trimQuery)) {
			processedQuery = buildFromSimpleQuery(trimQuery, maxreg, where, project, group);
		} else {
			processedQuery = buildFromComplexQuery(trimQuery, maxreg, where, project, group);
		}
		log.info("Quasar SQL execute query: " + processedQuery);
		return qts.querySQLAsJson(executeAs, ontology, processedQuery, 0);
	}

	// Check if query of datasource is simple, no inner joins and subqueries, TODO:
	// do it in datasource creation and save it with simple datasource flag in
	// database
	private boolean isSimpleDatasource(String queryOri) {
		String query = queryOri.toLowerCase();
		return query.indexOf("inner join") == -1 && query.indexOf("select", 1) == -1
				&& query.indexOf("outer join") == -1 && query.indexOf("full join") == -1;
	}

	// from original query
	private String buildFromSimpleQuery(String query, int maxreg, List<FilterStt> where, List<ProjectStt> project,
			List<String> group) {
		StringBuilder sb = new StringBuilder();
		int indexLimit = query.toLowerCase().lastIndexOf(LIMIT_STR);
		if (where == null || where.isEmpty()) {
			if (indexLimit == -1) {
				sb.append(query);
				sb.append(SP_LIMIT_STR + maxreg);
			} else {
				sb.append(query.substring(0, indexLimit));
				sb.append(SP_LIMIT_STR + maxreg);
			}
		} else {
			// Get real project from query

			String[] realproject = getRealProject(query);
			String elsWhere = buildWhere(where, "", false, realproject);
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
				sb.append(" limit  " + maxreg);
			} else {
				indexLimit = auxNoLimit.toLowerCase().lastIndexOf(LIMIT_STR);
				int querylimit = Integer.parseInt(auxNoLimit.substring(indexLimit + 6).trim().replace("\n", ""));
				sb = new StringBuilder();
				sb.append(auxNoLimit.substring(0, indexLimit));
				sb.append(SP_LIMIT_STR + Math.min(maxreg, querylimit));
			}
		}
		return sb.toString();
	}

	private String[] getRealProject(String query) {
		String[] lsrealproject = query
				.substring(query.toLowerCase().indexOf("select ") + 7, query.toLowerCase().indexOf(" from "))
				.split(",");
		for (int i = 0; i < lsrealproject.length; i++) {
			lsrealproject[i] = lsrealproject[i].trim();
		}
		return lsrealproject;
	}

	// With subquery
	private String buildFromComplexQuery(String query, int maxreg, List<FilterStt> where, List<ProjectStt> project,
			List<String> group) {
		StringBuilder sb = new StringBuilder();
		sb.append("select ");
		sb.append(buildProject(project));
		sb.append(" from ");
		sb.append("(");
		sb.append(query);
		sb.append(" ) AS Solved ");
		sb.append(buildWhere(where, solvedQueryPrefix, true, new String[0]));
		sb.append(buildGroup(group));
		sb.append(SP_LIMIT_STR);
		sb.append(maxreg);
		return sb.toString();
	}

	private String buildProject(List<ProjectStt> projections) {
		if (projections == null || projections.isEmpty()) {
			return "Solved.* ";
		} else {
			StringBuilder sb = new StringBuilder();
			for (ProjectStt p : projections) {
				sb.append(p.getOp());
				sb.append("(");
				sb.append(p.getField());
				sb.append(")");
				sb.append(",");
			}
			return sb.substring(0, sb.length() - 1);
		}
	}

	private String buildWhere(List<FilterStt> filters, String prefix, boolean includeWhere, String[] realproject) {
		if (filters == null || filters.isEmpty()) {
			return "";
		} else {
			StringBuilder sb = new StringBuilder();
			if (includeWhere)
				sb.append(" where ");
			for (FilterStt f : filters) {
				sb.append(prefix);
				sb.append(findEndParam(f.getField(), realproject));
				sb.append(" ");
				sb.append(f.getOp());
				sb.append(" ");
				sb.append(f.getExp());
				sb.append(filterSeparator);
			}
			return sb.substring(0, sb.length() - filterSeparator.length());
		}
	}

	private String findEndParam(String param, String[] realproject) {
		for (int i = 0; i < realproject.length; i++) {
			if (realproject[i].endsWith(param)) {
				int asindex = realproject[i].toLowerCase().indexOf(" as ");
				if (asindex != -1 && realproject[i].substring(asindex + 4).equals(param)) {
					return realproject[i].substring(0, asindex);
				}
				if (realproject[i].endsWith("." + param)) {
					return realproject[i];
				}
			}

		}
		return param;
	}

	private String buildGroup(List<String> groups) {
		if (groups == null || groups.isEmpty()) {
			return "";
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append(" group by ");
			for (String g : groups) {
				sb.append(g);
				sb.append(",");
			}
			return sb.substring(0, sb.length() - 1);
		}
	}
}
