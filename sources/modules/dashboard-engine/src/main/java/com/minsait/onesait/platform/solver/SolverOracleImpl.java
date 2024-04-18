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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.minsait.onesait.platform.persistence.external.generator.helper.SQLHelper;
import com.minsait.onesait.platform.persistence.services.QueryToolService;
import com.minsait.onesait.platform.persistence.services.util.QueryParsers;

@Component
@Qualifier("OracleSolver")
public class SolverOracleImpl implements SolverInterface {

	private static final Logger log = LoggerFactory.getLogger(SolverOracleImpl.class);

	@Autowired
	QueryToolService qts;

	@Autowired
	@Qualifier("OracleHelper")
	private SQLHelper sqlHelper;

	private static String filterSeparator = " and ";
	private static String solvedQueryPrefix = "Solved.";
	private static String stringDEBoundsChar = "\"";
	private static String stringLocalBoundsChar = "'";

	@Override
	public String buildQueryAndSolve(String query, int maxreg, List<FilterStt> where, List<ProjectStt> project,
			List<String> group, String executeAs, String ontology)
			throws DBPersistenceException, OntologyDataUnauthorizedException, GenericOPException {
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
		// delete as from query
		query = filterAS(query);

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

		} else {
			sb.append(query);
		}
		return sqlHelper.addLimit(sb.toString(), maxreg);
	}

	private String filterAS(String query) {
		query = query.replaceAll(" as ", "\\ ");
		query = query.replaceAll(" AS ", "\\ ");
		query = query.replaceAll(" As ", "\\ ");

		return query;
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
		return sqlHelper.addLimit(sb.toString(), maxreg);
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
				sb.append(appendExp(f));
				sb.append(filterSeparator);
			}
			return sb.substring(0, sb.length() - filterSeparator.length());
		}
	}

	private String appendExp(FilterStt f) {
		String exp = isFilterOverString(f) ? (f.getExp().replaceFirst(stringDEBoundsChar, stringLocalBoundsChar))
				.replaceFirst(".$", stringLocalBoundsChar) : f.getExp();

		if (QueryParsers.hasNowFunction(exp)) {
			exp = QueryParsers.parseFunctionNow(exp);
			exp = makeTimeStamp(exp);
		} else {
			exp = mapperTimestamp_TO_Timestamp(exp);
		}
		return exp;
	}

	private String mapperTimestamp_TO_Timestamp(String exp) {
		int asindex = exp.toLowerCase().indexOf("timestamp");
		if (asindex != -1) {
			String date = exp.substring(exp.indexOf("(") + 1, exp.indexOf(")"));
			exp = makeTimeStamp(date);
		} else if (isDate(exp.replace("'", ""))) {
			exp = makeTimeStamp(exp);
		}

		return exp;
	}

	public boolean isDate(String date) {
		if (date == null || date.trim().length() < 5) {
			return false;
		}
		Pattern p = Pattern.compile(
				"^([\\+-]?\\d{4}(?!\\d{2}\\b))((-?)((0[1-9]|1[0-2])(\\3([12]\\d|0[1-9]|3[01]))?|W([0-4]\\d|5[0-2])(-?[1-7])?|(00[1-9]|0[1-9]\\d|[12]\\d{2}|3([0-5]\\d|6[1-6])))([T\\s]((([01]\\d|2[0-3])((:?)[0-5]\\d)?|24\\:?00)([\\.,]\\d+(?!:))?)?(\\17[0-5]\\d([\\.,]\\d+)?)?([zZ]|([\\+-])([01]\\d|2[0-3]):?([0-5]\\d)?)?)?)?$");
		Matcher m = p.matcher(date);
		return m.matches();
	}

	private String makeTimeStamp(String date) {

		return "TO_TIMESTAMP(" + date + ",'YYYY-MM-DD\"T\"HH24:MI:SS.ff3\"Z\"')";
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
