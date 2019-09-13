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
import com.minsait.onesait.platform.persistence.external.virtual.helper.SQLServerVirtualOntologyHelper;
import com.minsait.onesait.platform.persistence.services.QueryToolService;

@Component
@Qualifier("SQLServerSolver")
public class SolverSQLServerImpl implements SolverInterface {

	private static final Logger log = LoggerFactory.getLogger(SolverSQLServerImpl.class);

	@Autowired
	QueryToolService qts;
	
	@Autowired
	private SQLServerVirtualOntologyHelper sqlServerVirtualOntologyHelper;

	private static final String FROM = " from ";
	private static final String FILTER_SEPARATOR = " AND ";
	private static final String SOLVED_QUERY_PREFIX = "Solved.";
	private static final String STRING_DEBOUND_CHAR = "\"";
	private static final String STRING_LOCAL_BOUNDS_CHAR = "'";

	@Override
	public String buildQueryAndSolve(String query, int maxreg, List<FilterStt> where, List<ProjectStt> project,
			List<String> group, String executeAs, String ontology) {
		String processedQuery;
		String trimQuery = query.replaceAll("\\t|\\r|\\r\\n\\t|\\n|\\r\\t|;", " ");
		trimQuery = trimQuery.trim().replaceAll(" +", " ");
		if (isSimpleDatasource(trimQuery)) {
			processedQuery = buildFromSimpleQuery(trimQuery, maxreg, where, group);
		} else {
			processedQuery = buildFromComplexQuery(trimQuery, maxreg, where, project, group);
		}
		log.info("SQLServer execute query: {}", processedQuery);
		return qts.querySQLAsJson(executeAs, ontology, processedQuery, 0);
	}

	private boolean isSimpleDatasource(String queryOri) {
		String query = queryOri.toLowerCase();
		return query.indexOf("inner join") == -1 && query.indexOf("select", 1) == -1
				&& query.indexOf("outer join") == -1 && query.indexOf("full join") == -1;
	}


	private String buildFromSimpleQuery(final String query, final int maxreg, final List<FilterStt> where, final List<String> group) {
		final StringBuilder sb = new StringBuilder();
		if (where != null && !where.isEmpty()) {
			final String[] realproject = getRealProject(query);
			final String elsWhere = buildWhere(where, "", false, realproject);
			final int indexWhere = query.toLowerCase().lastIndexOf("where ");
			
			if (indexWhere == -1) {
				writeWhereNotFound(query, sb, indexWhere);
				sb.append(" where  " + elsWhere + " ");
			} else {
				final int finalIndexWhere = indexWhere + 6;
				sb.append(query.substring(0, finalIndexWhere));
				sb.append(elsWhere + " " + FILTER_SEPARATOR + " ");
				
				if (indexWhere < query.length()) sb.append(query.substring(finalIndexWhere));
			}
		} else sb.append(query);
		
		return sqlServerVirtualOntologyHelper.addLimit(sb.toString(), maxreg);
	}

	private void writeWhereNotFound(final String query, final StringBuilder sb, final int indexWhere) {
		final int indexGroup = query.toLowerCase().lastIndexOf("group by ");
		final int indexOrder = query.toLowerCase().lastIndexOf("order by ");
		
		if (indexGroup == -1) {
			final int finalIndex = indexOrder != -1 ? indexOrder : query.length();
			sb.append(query.substring(0, finalIndex));
			if (indexWhere < query.length()) sb.append(query.substring(finalIndex));
		} else {
			sb.append(query.substring(0, indexGroup));
			if (indexWhere < query.length()) sb.append(query.substring(indexGroup));
		}
	}

	private String[] getRealProject(String query) {
		String[] lsrealproject;
		
		if(query.toLowerCase().indexOf("select top",0) != -1) {
			lsrealproject = query
					.substring(query.toLowerCase().indexOf(')') + 1, query.toLowerCase().indexOf(FROM))
					.split(",");
		} else {
			lsrealproject = query
					.substring(query.toLowerCase().indexOf("select ") + 7, query.toLowerCase().indexOf(FROM))
					.split(",");
		}
		
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
		sb.append(FROM);
		sb.append("(");
		sb.append(query);
		sb.append(" ) as Solved ");
		sb.append(buildWhere(where, SOLVED_QUERY_PREFIX, true, new String[0]));
		sb.append(buildGroup(group));
		return sqlServerVirtualOntologyHelper.addLimit(sb.toString(), maxreg);
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
				sb.append(isFilterOverString(f)?(f.getExp().replaceFirst(STRING_DEBOUND_CHAR, STRING_LOCAL_BOUNDS_CHAR)).replaceFirst(".$", STRING_LOCAL_BOUNDS_CHAR):f.getExp());
				sb.append(FILTER_SEPARATOR);
			}
			return sb.substring(0, sb.length() - FILTER_SEPARATOR.length());
		}
	}
	
	private boolean isFilterOverString(FilterStt f) {
		return f.getExp().startsWith(STRING_DEBOUND_CHAR) && f.getExp().endsWith(STRING_DEBOUND_CHAR);
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
