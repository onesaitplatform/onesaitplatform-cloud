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
import com.minsait.onesait.platform.persistence.external.virtual.helper.OracleVirtualOntologyHelper;
import com.minsait.onesait.platform.persistence.services.QueryToolService;

@Component
@Qualifier("OracleSolver")
public class SolverOracleImpl implements SolverInterface {

	private static final Logger log = LoggerFactory.getLogger(SolverOracleImpl.class);

	@Autowired
	QueryToolService qts;
	
	@Autowired
	private OracleVirtualOntologyHelper oracleVirtualOntologyHelper;

	private static String filterSeparator = " and ";
	private static String solvedQueryPrefix = "Solved.";
	private static String stringDEBoundsChar = "\"";
	private static String stringLocalBoundsChar = "'";

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
		log.info("Oracle SQL execute query: " + processedQuery);
		return qts.querySQLAsJson(executeAs, ontology, processedQuery, 0);
	}

	// Check if query of datasource is simple, no inner joins and subqueries
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

		if (where != null && where.size() != 0) {
			// Get real project from query

			String[] realproject = getRealProject(query);
			String elsWhere = buildWhere(where, "", false, realproject);
			int indexWhere = query.toLowerCase().lastIndexOf("where ");
			if (indexWhere == -1) {
				int indexGroup = query.toLowerCase().lastIndexOf("group by ");
				int indexOrder = query.toLowerCase().lastIndexOf("order by ");
				if (indexGroup == -1) {
					indexWhere = indexOrder != -1 ? indexOrder : query.length();
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
			
		}
		else {
			sb.append(query);
		}
		return oracleVirtualOntologyHelper.addLimit(sb.toString(), maxreg);
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
		sb.append(" ) Solved ");
		sb.append(buildWhere(where, solvedQueryPrefix, true, new String[0]));
		sb.append(buildGroup(group));
		return oracleVirtualOntologyHelper.addLimit(sb.toString(), maxreg);
	}

	private String buildProject(List<ProjectStt> projections) {
		if (projections == null || projections.size() == 0) {
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
		if (filters == null || filters.size() == 0) {
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
				sb.append(isFilterOverString(f)?(f.getExp().replaceFirst(stringDEBoundsChar, stringLocalBoundsChar)).replaceFirst(".$", stringLocalBoundsChar):f.getExp());
				sb.append(filterSeparator);
			}
			return sb.substring(0, sb.length() - filterSeparator.length());
		}
	}
	
	private boolean isFilterOverString(FilterStt f) {
		return f.getExp().startsWith(stringDEBoundsChar) && f.getExp().endsWith(stringDEBoundsChar);
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
		if (groups == null || groups.size() == 0) {
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
