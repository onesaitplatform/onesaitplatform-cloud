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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.business.services.datasources.dto.FilterStt;
import com.minsait.onesait.platform.business.services.datasources.dto.OrderByStt;
import com.minsait.onesait.platform.business.services.datasources.dto.ParamStt;
import com.minsait.onesait.platform.business.services.datasources.dto.ProjectStt;
import com.minsait.onesait.platform.persistence.external.generator.helper.SQLHelper;
import com.minsait.onesait.platform.persistence.services.util.QueryParsers;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;

@Component
@Qualifier("OracleSolver")
public class SolverOracleImpl extends SolverSQLImpl {

	@Autowired
	@Qualifier("OracleHelper")
	private SQLHelper sqlHelper;

	private static String stringDEBoundsChar = "\"";
	private static String stringLocalBoundsChar = "'";

	@Override
	protected String buildFromSimpleQuery(String query, int maxreg, List<FilterStt> where, List<ProjectStt> project,
			List<String> group, List<OrderByStt> orderby, long offset, long limit, List<ParamStt> param)
			throws JSQLParserException {

		return addLimitOffset(
				buildJSQLFromSimpleQueryNoLimitOffset(filterAS(query), where, project, group, orderby, param), maxreg,
				offset, limit);
	}

	@Override
	protected String addLimitOffset(PlainSelect select, int maxreg, long offset, long limit) {
		Limit querylimit = select.getLimit();

		Long min = (limit > 0 ? Math.min(maxreg, limit) : maxreg);

		if (querylimit != null) {
			min = Math.min(min, ((LongValue) querylimit.getRowCount()).getValue());
		}

		// Disable limit and added to end of the query
		select.setLimit(null);

		if (offset > 0) {
			return sqlHelper.addLimit(select.toString(), min, offset);
		} else {
			return sqlHelper.addLimit(select.toString(), min);
		}
	}

	@Override
	protected Expression buildExpFromFilter(FilterStt f, List<SelectItem> realproject, String prefix)
			throws JSQLParserException {
		StringBuilder sb = new StringBuilder();
		sb.append(prefix);
		sb.append(findEndParamV2(f.getField(), realproject));
		sb.append(" ");
		sb.append(f.getOp());
		sb.append(" ");
		sb.append(appendExp(f));
		return CCJSqlParserUtil.parseCondExpression(sb.toString());
	}

	private String filterAS(String query) {
		return query.replaceAll(" (?i)as ", "\\ ");
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
		return m.matches() && date.contains(":") && date.contains("-");
	}

	private String makeTimeStamp(String date) {

		return "TO_TIMESTAMP(" + date + ",'YYYY-MM-DD\"T\"HH24:MI:SS.ff3\"Z\"')";
	}

	private boolean isFilterOverString(FilterStt f) {
		return f.getExp().startsWith(stringDEBoundsChar) && f.getExp().endsWith(stringDEBoundsChar);
	}
}
