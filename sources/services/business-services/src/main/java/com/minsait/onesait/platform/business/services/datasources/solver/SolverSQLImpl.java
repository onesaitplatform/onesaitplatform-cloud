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
package com.minsait.onesait.platform.business.services.datasources.solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

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

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.Offset;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;

@Component
@Qualifier("SQLSolver")
public class SolverSQLImpl implements SolverInterface {

	private static final Logger log = LoggerFactory.getLogger(SolverSQLImpl.class);
	@Autowired
	QueryToolService qts;

	private static String[] ESPECIAL_FUNCTIONS = new String[] { "date_histogram" };
	private static final String PRE_COMPLEX_QUERY_TPL = "select * from (";
	private static final String POST_COMPLEX_QUERY_TPL = ") as Solved";

	@Override
	public String buildQueryAndSolve(String query, int maxreg, List<FilterStt> where, List<ProjectStt> project,
			List<String> group, List<OrderByStt> sort, long offset, long limit, List<ParamStt> param, boolean debug,
			String executeAs, String ontology, boolean isSimpleMode)
			throws DashboardEngineException, OntologyDataUnauthorizedException, GenericOPException {
		try {
			return qts.querySQLAsJson(executeAs, ontology, buildQuery(query, maxreg, where, project, group, sort,
					offset, limit, param, debug, executeAs, ontology, isSimpleMode), 0);
		} catch (final DBPersistenceException e) {
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

		log.info("Params overwrite Query: {}", trimParamsQuery);

		final HashMap<String, String> hashMapArrays = new HashMap<String, String>();
		trimParamsQuery = parseArrays(trimParamsQuery, hashMapArrays);
		trimParamsQuery = commentGroupByfunctions(trimParamsQuery);

		try {
			if (isSimpleMode || isSimpleDatasource(trimQuery)) {
				processedQuery = buildFromSimpleQuery(trimParamsQuery, maxreg, where, project, group, sort, offset,
						limit, param);
				processedQuery = unparseArrays(processedQuery, hashMapArrays);
				processedQuery = unCommentGroupByfunctions(processedQuery);
				if (log.isDebugEnabled()) {
					log.debug("commentGroupByfunctions Query: {}", trimParamsQuery);
				}
			} else {
				processedQuery = buildFromComplexQuery(trimParamsQuery, maxreg, where, project, group, sort, offset,
						limit, param);
				processedQuery = unparseArrays(processedQuery, hashMapArrays);
				processedQuery = unCommentGroupByfunctions(processedQuery);
			}
		} catch (final JSQLParserException e) {
			throw new DashboardEngineException(DashboardEngineException.Error.PARSE_EXCEPTION, e.getCause());
		}
			log.info("SQL execute query: {}", processedQuery);

		return processedQuery;

	}

	private String unCommentGroupByfunctions(String query) {
		for (final String espFunction : ESPECIAL_FUNCTIONS) {
			final int index = query.indexOf(espFunction);
			if (index >= 0) {
				final int indexleft = query.indexOf("(", index);
				query = query.substring(0, indexleft + 1) + query.substring(indexleft + 2, query.length());
				final int indexright = query.indexOf(")", indexleft);
				query = query.substring(0, indexright - 1) + query.substring(indexright, query.length());
			}
		}
		return query;

	}

	private String commentGroupByfunctions(String query) {
		for (final String espFunction : ESPECIAL_FUNCTIONS) {
			final int index = query.indexOf(espFunction);
			if (index >= 0) {
				final int indexleft = query.indexOf("(", index);
				query = query.substring(0, indexleft + 1) + "`" + query.substring(indexleft + 1, query.length());
				final int indexright = query.indexOf(")", indexleft);
				query = query.substring(0, indexright) + "`" + query.substring(indexright, query.length());
			}
		}
		return query;
	}

	private static String parseArrays(String query, HashMap<String, String> hasmap) {
		final StringTokenizer tokens = new StringTokenizer(query);
		while (tokens.hasMoreTokens()) {
			final String str = tokens.nextToken();
			String key = str;
			if (str.indexOf('[') > 0) {
				key = "aa" + UUID.randomUUID().toString().replace("-", "");
				hasmap.put(key, str);
				query = query.replace(str, key);
			}
		}

		return query;
	}

	private static String unparseArrays(String query, HashMap<String, String> hasmap) {

		for (final Map.Entry<String, String> pair : hasmap.entrySet()) {
			query = query.replace(pair.getKey(), pair.getValue());
		}

		return query;
	}

	// Check if query of datasource is simple, no inner joins and subqueries,
	// do it in datasource creation and save it with simple datasource flag in
	// database
	private boolean isSimpleDatasource(String queryOri) {
		final String query = queryOri.toLowerCase();
		return query.indexOf("inner join") == -1 && query.indexOf("select", 1) == -1
				&& query.indexOf("outer join") == -1 && query.indexOf("full join") == -1 && query.indexOf("join") == -1;
	}

	// from original query with not limit and offset, many databases are the same
	// here
	protected PlainSelect buildJSQLFromSimpleQueryNoLimitOffset(String query, List<FilterStt> where,
			List<ProjectStt> project, List<String> group, List<OrderByStt> orderby, List<ParamStt> param)
			throws JSQLParserException {

		final Statement statement = CCJSqlParserUtil.parse(query);
		if (log.isDebugEnabled()) {
			log.debug("CCJSqlParserUtil.parse: {}", query);
		}
		final PlainSelect select = (PlainSelect) ((Select) statement).getSelectBody();
		final List<SelectItem> selectItems = select.getSelectItems();

		if (project != null && !project.isEmpty()) {
			select.setSelectItems(buildProjectV2(selectItems, project));
			selectItems.addAll(select.getSelectItems());
		}

		final Expression querywhere = select.getWhere();

		select.setWhere(buildWhereV2(where, "", selectItems, querywhere));

		if (group != null && !group.isEmpty()) {
			List<Expression> querygroup = null;
			if (select.getGroupBy() != null) {
				querygroup = select.getGroupBy().getGroupByExpressions();
			}
			final GroupByElement gbyelement = new GroupByElement();
			gbyelement.setGroupByExpressions(buildGroupByV2(group, "", selectItems, querygroup));
			select.setGroupByElement(gbyelement);
			select.setHaving(buildHavingV2(where, "", selectItems, querywhere));
		}

		if (orderby != null && !orderby.isEmpty()) {
			final List<OrderByElement> queryorderby = select.getOrderByElements();
			select.setOrderByElements(buildOrderByV2(orderby, "", selectItems, queryorderby));
		}

		return select;
	}

	// from original query
	protected String buildFromSimpleQuery(String query, int maxreg, List<FilterStt> where, List<ProjectStt> project,
			List<String> group, List<OrderByStt> orderby, long offset, long limit, List<ParamStt> param)
			throws JSQLParserException {

		return addLimitOffset(buildJSQLFromSimpleQueryNoLimitOffset(query, where, project, group, orderby, param),
				maxreg, offset, limit);
	}

	protected String addLimitOffset(PlainSelect select, int maxreg, long offset, long limit) {
		final Limit querylimit = select.getLimit();

		Long min = (limit > 0 ? Math.min(maxreg, limit) : maxreg);
		final Limit laux = new Limit();
		if (querylimit != null) {
			min = Math.min(min, ((LongValue) querylimit.getRowCount()).getValue());
		}
		laux.setRowCount(new LongValue(min));

		select.setLimit(laux);

		if (offset > 0) {
			final Expression offsetExp = new LongValue(offset);
			final Offset oaux = new Offset();
			oaux.setOffset(offsetExp);
			select.setOffset(oaux);
		}

		return select.toString();
	}

	// With subquery
	private String buildFromComplexQuery(String query, int maxreg, List<FilterStt> where, List<ProjectStt> project,
			List<String> group, List<OrderByStt> sort, long offset, long limit, List<ParamStt> param)
			throws JSQLParserException {
		query = PRE_COMPLEX_QUERY_TPL + query + POST_COMPLEX_QUERY_TPL;
		return buildFromSimpleQuery(query, maxreg, where, project, group, sort, offset, limit, param);
	}

	protected SelectItem generateSelectItemFromProject(ProjectStt pstt, List<SelectItem> selectItem) {
		final Column fieldValue = new Column(findEndParamV2(pstt.getField(), selectItem));
		SelectExpressionItem sitem;
		if (pstt.getOp() != null && pstt.getOp().trim().length() > 0) {
			final List<Expression> expressions = new ArrayList<>();
			expressions.add(fieldValue);
			final ExpressionList expressionList = new ExpressionList();
			expressionList.setExpressions(expressions);

			final Function function = new Function();
			function.setName(pstt.getOp());
			function.setParameters(expressionList);
			sitem = new SelectExpressionItem(function);
		} else {
			sitem = new SelectExpressionItem(fieldValue);
			if (!pstt.getField().equals(fieldValue.getColumnName())) {
				sitem.setAlias(new Alias(pstt.getField(), true));
			}

		}
		if (pstt.getAlias() != null && !"".contentEquals(pstt.getAlias())) {
			sitem.setAlias(new Alias(pstt.getAlias(), true));
		}
		return sitem;
	}

	protected List<SelectItem> buildProjectV2(List<SelectItem> selectItem, List<ProjectStt> projections) {
		if (projections == null || projections.isEmpty()) {
			return selectItem;
		} else {
			final List<SelectItem> selectItemOverwrite = new ArrayList<>();
			for (final ProjectStt p : projections) {
				selectItemOverwrite.add(generateSelectItemFromProject(p, selectItem));
			}
			return selectItemOverwrite;
		}
	}

	protected Expression buildExpFromFilter(FilterStt f, List<SelectItem> realproject, String prefix)
			throws JSQLParserException {
		final StringBuilder sb = new StringBuilder();
		sb.append(prefix);
		sb.append(findEndParamV2(f.getField(), realproject));
		sb.append(" ");
		sb.append(f.getOp());
		sb.append(" ");
		sb.append(f.getExp());
		return CCJSqlParserUtil.parseCondExpression(sb.toString());
	}

	private boolean isHavingExp(String exp) {
		final String expaux = exp.toLowerCase().replace(" ", "");
		return (expaux.indexOf("sum(") != -1 || expaux.indexOf("max(") != -1 || expaux.indexOf("min(") != -1
				|| expaux.indexOf("avg(") != -1 || expaux.indexOf("count(") != -1);
	}

	protected Expression buildFilterV2(List<FilterStt> filters, String prefix, List<SelectItem> realproject,
			Expression filterex, boolean isHaving) throws JSQLParserException {

		if (filters == null || filters.isEmpty()) {
			return filterex;
		} else {
			Expression filterexaux = null;
			int startfindex = 0;
			if (filterex != null) {
				filterexaux = filterex;
			} else {
				boolean found = false;
				while (startfindex < filters.size() && !found) {
					final String realField = findEndParamV2(filters.get(startfindex).getField(), realproject);
					if (isHaving == isHavingExp(realField)) {
						filterexaux = buildExpFromFilter(filters.get(startfindex), realproject, prefix);
						found = true;
					}
					startfindex++;
				}
			}

			for (int i = startfindex; i < filters.size(); i++) {
				if (isHaving == isHavingExp(findEndParamV2(filters.get(i).getField(), realproject))) {
					filterexaux = new AndExpression(filterexaux,
							buildExpFromFilter(filters.get(i), realproject, prefix));
				}
			}
			return filterexaux;
		}
	}

	protected Expression buildWhereV2(List<FilterStt> filters, String prefix, List<SelectItem> realproject,
			Expression whereex) throws JSQLParserException {

		return buildFilterV2(filters, prefix, realproject, whereex, false);
	}

	protected Expression buildHavingV2(List<FilterStt> filters, String prefix, List<SelectItem> realproject,
			Expression havingex) throws JSQLParserException {

		return buildFilterV2(filters, prefix, realproject, havingex, true);
	}

	protected List<Expression> buildGroupByV2(List<String> groups, String prefix, List<SelectItem> realproject,
			List<Expression> groupex) throws JSQLParserException {

		if (groups == null || groups.isEmpty()) {
			return groupex;
		} else {
			final List<Expression> groupexaux = (groupex != null && !groupex.isEmpty() ? groupex
					: new ArrayList<Expression>());

			for (final String group : groups) {
				groupexaux.add(CCJSqlParserUtil.parseExpression(findEndParamV2(group, realproject)));
			}
			return groupexaux;
		}
	}

	protected List<OrderByElement> buildOrderByV2(List<OrderByStt> orderbys, String prefix,
			List<SelectItem> realproject, List<OrderByElement> orderbyex) throws JSQLParserException {

		if (orderbys == null || orderbys.isEmpty()) {
			return orderbyex;
		} else {
			final List<OrderByElement> orderbyexaux = (orderbyex != null && !orderbyex.isEmpty() ? orderbyex
					: new ArrayList<OrderByElement>());

			for (final OrderByStt orderby : orderbys) {
				final OrderByElement newOrder = new OrderByElement();
				newOrder.setExpression(
						CCJSqlParserUtil.parseExpression(findEndParamV2(orderby.getField(), realproject)));
				newOrder.setAsc(orderby.isAsc());
				orderbyexaux.add(newOrder);
			}
			return orderbyexaux;
		}
	}

	protected String findEndParamV2(String param, List<SelectItem> realproject) {
		for (final SelectItem s : realproject) {
			final String field = s.toString();
			if (field.endsWith(param)) {
				final int asindex = field.toLowerCase().indexOf(" as ");
				if (asindex != -1 && field.substring(asindex + 4).equals(param)) {
					return field.substring(0, asindex);
				}
				if (field.endsWith("." + param)) {
					return field;
				}
			}
		}
		return param;
	}

	private String processQueryParams(String trimquery, List<ParamStt> params) {
		if (params != null && !params.isEmpty()) {
			for (final ParamStt param : params) {
				trimquery = trimquery.replaceAll("\\{\\$" + param.getField() + "\\}", param.getValue());
			}
		}
		return trimquery;
	}
}
