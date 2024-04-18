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
package com.minsait.onesait.platform.persistence.mongodb.tools.sql;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;

@AllArgsConstructor
public class WhereExpressionVisitorAdapter extends ExpressionVisitorAdapter {

	private final StringBuilder builder;
	private static final String ISODATE_FUNCTION = "ISODate";
	private static final String OBJECTID_FUNCTION = "ObjectId";
	private static final String DOLLAR_AND = "$and";
	private static final String DOLLAR_OR = "$or";
	private static final String OR_SPLITTER = " or ";
	private static final String AND_SPLITTER = " and ";
	private static final String BOOLEAN_FUNCTION = "BOOLEAN";
	private static final String OID_FUNCTION = "OID";
	@Getter
	@Setter
	private boolean firstAnd;
	@Getter
	@Setter
	private int andsInQuery;
	@Getter
	@Setter
	private boolean firstOr;
	@Getter
	@Setter
	private int orsInQuery;

	@Override
	public void visit(Function function) {
		if (function.getName().equalsIgnoreCase(BOOLEAN_FUNCTION)) {
			final ExpressionList params = function.getParameters();
			if (params.getExpressions().size() == 1) {
				final String param = params.getExpressions().get(0).toString();
				builder.append(Boolean.valueOf(param));
				builder.append("}");
			} else {
				throw new RuntimeException("Incorrect use of " + BOOLEAN_FUNCTION + " function");
			}

		} else if (function.getName().equalsIgnoreCase(OID_FUNCTION)) {
			final ExpressionList params = function.getParameters();
			if (params.getExpressions().size() == 1) {
				final String param = ((StringValue) params.getExpressions().get(0)).getValue();
				builder.append(getObjectId(param));
				builder.append("}");
			} else {
				throw new RuntimeException("Incorrect use of " + OID_FUNCTION + " function");
			}
		} else {
			throw new RuntimeException("SQL Function " + function.getName() + " not supported");
		}
	}

	@Override
	public void visit(OrExpression or) {
		if (!isFirstOr()) {
			setFirstOr(true);
			setOrsInQuery(orsInQuery(or, 2));
		}

		if (orsInQuery(or, 2) <= 2) {
			if (!builder.toString().endsWith("{"))
				builder.append("{");
			builder.append("$or:[");
		}

		super.visit(or);
		Sql2NativeTool.removeIfLastCharacterIsComma(builder);

		if (orsInQuery(or, 2) == getOrsInQuery()) {
			builder.append("]");
			builder.append("}");
			setFirstOr(false);
		}
		builder.append(",");
	}

	@Override
	public void visit(AndExpression and) {
		if (!isFirstAnd()) {
			setFirstAnd(true);
			setAndsInQuery(andsInQuery(and, 2));
		}
		if (andsInQuery(and, 2) <= 2) {
			if (!builder.toString().endsWith("{"))
				builder.append("{");
			builder.append("$and:[");
		}
		super.visit(and);
		Sql2NativeTool.removeIfLastCharacterIsComma(builder);
		if (andsInQuery(and, 2) == getAndsInQuery()) {
			builder.append("]");
			builder.append("}");
			setFirstAnd(false);
		}

		builder.append(",");

	}

	@Override
	public void visit(NotEqualsTo net) {
		// if (builder.lastIndexOf(DOLLAR_AND) != -1 || builder.lastIndexOf(DOLLAR_OR)
		// != -1)
		builder.append("{");
		builder.append("'" + net.getLeftExpression() + "'");
		builder.append(":{$ne:");
		super.visit(net);
		// if (builder.lastIndexOf(DOLLAR_AND) != -1 || builder.lastIndexOf(DOLLAR_OR)
		// != -1)
		builder.append("}");
		builder.append(",");

	}

	@Override
	public void visit(EqualsTo eq) {
		// if (builder.lastIndexOf(DOLLAR_AND) != -1 || builder.lastIndexOf(DOLLAR_OR)
		// != -1)
		builder.append("{");
		builder.append("'" + eq.getLeftExpression() + "'");
		builder.append(":{$eq:");
		super.visit(eq);
		// if (builder.lastIndexOf(DOLLAR_AND) != -1 || builder.lastIndexOf(DOLLAR_OR)
		// != -1)
		builder.append("}");
		builder.append(",");

	}

	@Override
	public void visit(GreaterThan gt) {
		// if (builder.lastIndexOf(DOLLAR_AND) != -1 || builder.lastIndexOf(DOLLAR_OR)
		// != -1)
		builder.append("{");
		builder.append("'" + gt.getLeftExpression() + "'");
		builder.append(":{$gt:");
		super.visit(gt);
		// if (builder.lastIndexOf(DOLLAR_AND) != -1 || builder.lastIndexOf(DOLLAR_OR)
		// != -1)
		builder.append("}");
		builder.append(",");

	}

	@Override
	public void visit(GreaterThanEquals gte) {
		// if (builder.lastIndexOf(DOLLAR_AND) != -1 || builder.lastIndexOf(DOLLAR_OR)
		// != -1)
		builder.append("{");
		builder.append("'" + gte.getLeftExpression() + "'");
		builder.append(":{$gte:");
		super.visit(gte);
		// if (builder.lastIndexOf(DOLLAR_AND) != -1 || builder.lastIndexOf(DOLLAR_OR)
		// != -1)
		builder.append("}");
		builder.append(",");

	}

	@Override
	public void visit(MinorThan lt) {
		// if (builder.lastIndexOf(DOLLAR_AND) != -1 || builder.lastIndexOf(DOLLAR_OR)
		// != -1)
		builder.append("{");
		builder.append("'" + lt.getLeftExpression() + "'");
		builder.append(":{$lt:");
		super.visit(lt);
		// if (builder.lastIndexOf(DOLLAR_AND) != -1 || builder.lastIndexOf(DOLLAR_OR)
		// != -1)
		builder.append("}");
		builder.append(",");

	}

	@Override
	public void visit(MinorThanEquals lte) {
		// if (builder.lastIndexOf(DOLLAR_AND) != -1 || builder.lastIndexOf(DOLLAR_OR)
		// != -1)
		builder.append("{");
		builder.append("'" + lte.getLeftExpression() + "'");
		builder.append(":{$lte:");
		super.visit(lte);
		// if (builder.lastIndexOf(DOLLAR_AND) != -1 || builder.lastIndexOf(DOLLAR_OR)
		// != -1)
		builder.append("}");
		builder.append(",");

	}

	@Override
	public void visit(StringValue stringValue) {
		if (isDateTimeFormat(stringValue.getValue()))
			builder.append(getDateTime(stringValue.getValue()));
		// else if (isObjectId(stringValue.getValue()))
		// builder.append(getObjectId(stringValue.getValue()));
		else if (isBooleanValue(stringValue.getValue()))
			builder.append(getBooleanValue(stringValue.getValue()));
		else
			builder.append(stringValue);
		builder.append("}");

	}

	@Override
	public void visit(LongValue longValue) {
		super.visit(longValue);
		builder.append(longValue.getStringValue());
		builder.append("}");

	}

	@Override
	public void visit(DoubleValue doubleValue) {
		super.visit(doubleValue);
		builder.append(doubleValue.getValue());
		builder.append("}");
	}

	@Override
	public void visit(NullValue value) {
		super.visit(value);
		builder.append(value.toString().toLowerCase());
		builder.append("}");
	}

	private boolean isBooleanValue(String value) {
		return (value.equalsIgnoreCase("boolean.true") || value.equalsIgnoreCase("boolean.false"));
	}

	private boolean isDateTimeFormat(String value) {
		return getDateTime(value) != null;

	}

	private boolean isObjectId(String value) {

		final Pattern pattern = Pattern.compile("^[0-9a-fA-F]{24}$");
		final Matcher matcher = pattern.matcher(value);
		return matcher.matches();
	}

	private String getDateTime(String dateTimeValue) {
		final DateFormat dfr = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		try {
			dfr.parse(dateTimeValue);
			return ISODATE_FUNCTION + "('" + dateTimeValue + "')";
		} catch (final ParseException e) {
			return null;
		}
	}

	private String getObjectId(String oid) {
		return OBJECTID_FUNCTION + "('" + oid + "')";
	}

	private boolean getBooleanValue(String value) {
		return Boolean.valueOf(value.split("\\.")[1]).booleanValue();
	}

	private int orsInQuery(OrExpression or, int count) {
		final Expression left = or.getLeftExpression();
		final Expression right = or.getRightExpression();
		if (left instanceof OrExpression)
			count = orsInQuery((OrExpression) left, count);
		if (right instanceof OrExpression)
			count = orsInQuery((OrExpression) right, count);
		return count;

	}

	private int andsInQuery(AndExpression or, int count) {
		final Expression left = or.getLeftExpression();
		final Expression right = or.getRightExpression();
		if (left instanceof AndExpression) {
			count++;
			count = andsInQuery((AndExpression) left, count);
		}
		if (right instanceof AndExpression) {
			count++;
			count = andsInQuery((AndExpression) right, count);
		}
		return count;

	}
}
