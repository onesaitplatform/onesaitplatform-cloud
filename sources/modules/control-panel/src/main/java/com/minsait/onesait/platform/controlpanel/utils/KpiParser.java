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
package com.minsait.onesait.platform.controlpanel.utils;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.minsait.onesait.platform.commons.exception.GenericOPException;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression.DateTime;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

@Slf4j
public final class KpiParser {

	private static final String S_CURRENT = "$current_";
	private static final String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	private static final String DATE_FORMAT = "yyyy-MM-dd";

	private static Expression getTemporaryParameters(String fieldDate, final Calendar calendar, boolean firstTime,
			Date dateIni) {

		Between bet = new Between();
		DateTimeLiteralExpression dateTime = new DateTimeLiteralExpression();
		DateTimeLiteralExpression dateTimeEnd = new DateTimeLiteralExpression();
		TemporalityKpi date = TemporalityKpi.valueOf(fieldDate);
		Calendar startDate = (Calendar) calendar.clone();
		Calendar endDate = (Calendar) calendar.clone();

		switch (date) {
		case CURRENT_MINUTE:
			break;
		case CURRENT_HOUR:

			startDate.set(Calendar.MINUTE, 0);
			endDate.set(Calendar.MINUTE, 59);
			break;
		case CURRENT_DAY:
			break;
		case CURRENT_YEAR:
			startDate.set(Calendar.MONTH, 0);
			endDate.set(Calendar.MONTH, 11);
			break;
		case CURRENT_MONTH:

			startDate.set(Calendar.DAY_OF_MONTH, 1);
			endDate.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
			break;
		default:
			log.error("Error getting temporary parameters");
			throw new IllegalArgumentException("Unknown temporary reference: " + fieldDate);
		}
		if (date == TemporalityKpi.CURRENT_MINUTE || date == TemporalityKpi.CURRENT_HOUR) {

			dateTimeEnd.setType(DateTime.TIMESTAMP);
			dateTimeEnd.setValue(getFormat(endDate.getTime(), ISO_FORMAT));
			dateTime.setType(DateTime.TIMESTAMP);

			if (firstTime && dateIni != null) {
				dateTime.setValue(getFormat(dateIni, ISO_FORMAT));
			} else if (firstTime && dateIni == null) {

				MinorThanEquals minor = new MinorThanEquals();
				minor.setRightExpression(dateTimeEnd);
			} else {
				startDate.set(Calendar.SECOND, 0);
				dateTime.setValue(getFormat(startDate.getTime(), ISO_FORMAT));
			}
		} else {
			dateTime.setType(DateTime.DATE);
			dateTimeEnd.setType(DateTime.DATE);
			dateTimeEnd.setValue(getFormat(endDate.getTime(), DATE_FORMAT));

			if (firstTime) {
				dateTime.setValue(getFormat(dateIni, DATE_FORMAT));
			} else if (dateIni == null) {

				MinorThanEquals minor = new MinorThanEquals();
				minor.setRightExpression(dateTimeEnd);
			} else {
				dateTime.setValue(getFormat(startDate.getTime(), DATE_FORMAT));
			}
		}

		bet.setBetweenExpressionStart(dateTime);
		bet.setBetweenExpressionEnd(dateTimeEnd);
		return bet;
	}

	private static String getFormat(Date date, String pattern) {
		return "(\"" + new SimpleDateFormat(pattern).format(date) + "\")";
	}

	private static String operatefields(String queryResult, Calendar calendar) {

		Pattern p = Pattern.compile("(\\$current_\\w+)");
		Matcher matcher = p.matcher(queryResult);
		DateTimeLiteralExpression dateTime = new DateTimeLiteralExpression();
		while (matcher.find()) {
			TemporalityKpi date = TemporalityKpi.valueOf(matcher.group());
			switch (date) {
			case CURRENT_MINUTE:
			case CURRENT_HOUR:
				dateTime.setType(DateTime.TIMESTAMP);
				dateTime.setValue(getFormat(calendar.getTime(), ISO_FORMAT));
				queryResult = queryResult.replace(date.name(), dateTime.toString());
				break;
			case CURRENT_DAY:
			case CURRENT_MONTH:
			case CURRENT_YEAR:
				dateTime.setType(DateTime.DATE);
				dateTime.setValue(getFormat(calendar.getTime(), DATE_FORMAT));
				queryResult = queryResult.replace(date.name(), dateTime.toString());
				break;
			default:
				throw new IllegalArgumentException("Unknown temporary reference: " + date.name());
			}
		}
		return queryResult;
	}

	public static String queryAnalyzerAndLimit1(String query, boolean firstTime, Date startDate) throws Exception {

		CCJSqlParserManager parserManager = new CCJSqlParserManager();
		Select select = (Select) parserManager.parse(new StringReader(query));
		PlainSelect ps = (PlainSelect) select.getSelectBody();
		Calendar calendar = Calendar.getInstance();

		Expression e = generateExpression(ps.getWhere(), calendar, firstTime, startDate);
		ps.setWhere(e);
		Limit limit = new Limit();
		ps.setLimit(limit);
		return operatefields(ps.toString(), calendar);
	}

	public static String queryAnalyzer(String query, boolean firstTime, Date startDate) throws Exception {

		CCJSqlParserManager parserManager = new CCJSqlParserManager();
		Select select = (Select) parserManager.parse(new StringReader(query));
		PlainSelect ps = (PlainSelect) select.getSelectBody();
		Calendar calendar = Calendar.getInstance();

		// operator equalsTo
		Expression e = generateExpression(ps.getWhere(), calendar, firstTime, startDate);
		ps.setWhere(e);
		return operatefields(ps.toString(), calendar);
	}

	private static Expression generateExpression(Expression e, final Calendar calendar, final boolean firstTime,
			final Date dateIni) throws GenericOPException {

		if (e instanceof AndExpression) {

			AndExpression field = (AndExpression) e;
			if (field.getLeftExpression() instanceof Expression) {
				Expression ex = generateExpression(field.getLeftExpression(), calendar, firstTime, dateIni);
				field.setLeftExpression(ex);
			}
			if (field.getRightExpression() instanceof Expression) {
				Expression ex = generateExpression(field.getRightExpression(), calendar, firstTime, dateIni);
				field.setRightExpression(ex);
			}
			return field;
		} else if (e instanceof OrExpression) {

			OrExpression field = (OrExpression) e;
			if (field.getLeftExpression() instanceof Expression) {
				Expression ex = generateExpression(field.getLeftExpression(), calendar, firstTime, dateIni);
				field.setLeftExpression(ex);
			}
			if (field.getRightExpression() instanceof Expression) {
				Expression ex = generateExpression(field.getRightExpression(), calendar, firstTime, dateIni);
				field.setRightExpression(ex);
			}
			return field;
		} else if (e instanceof EqualsTo) {

			EqualsTo field = (EqualsTo) e;
			if (field.getLeftExpression().toString().contains(S_CURRENT)
					&& field.getRightExpression().toString().contains(S_CURRENT)) {
				log.error("Error generating expression");
				throw new GenericOPException("Two or more temporary dependencies detected in the same operand");
			} else if (field.getLeftExpression().toString().contains(S_CURRENT)) {

				Expression expression = getTemporaryParameters(field.getLeftExpression().toString(), calendar,
						firstTime, dateIni);
				if (expression instanceof Between) {
					Between bet = (Between) expression;
					bet.setLeftExpression(field.getRightExpression());
					return bet;
				} else {
					MinorThanEquals minor = (MinorThanEquals) expression;
					minor.setLeftExpression(field.getRightExpression());
					return minor;
				}

			} else if (field.getRightExpression().toString().contains(S_CURRENT)) {

				Expression expression = getTemporaryParameters(field.getRightExpression().toString(), calendar,
						firstTime, dateIni);
				if (expression instanceof Between) {
					Between bet = (Between) expression;
					bet.setLeftExpression(field.getLeftExpression());
					return bet;
				} else {
					MinorThanEquals minor = (MinorThanEquals) expression;
					minor.setLeftExpression(field.getLeftExpression());
					return minor;
				}

			} else {
				// if not $current_ dont touch
				return field;
			}
		} else {
			// parser not required
			return e;
		}
	}

}
