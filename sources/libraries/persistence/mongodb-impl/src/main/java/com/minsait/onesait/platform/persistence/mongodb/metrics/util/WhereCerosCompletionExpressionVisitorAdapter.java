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
package com.minsait.onesait.platform.persistence.mongodb.metrics.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.minsait.onesait.platform.persistence.mongodb.tools.sql.Sql2NativeTool;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;

@AllArgsConstructor
public class WhereCerosCompletionExpressionVisitorAdapter extends ExpressionVisitorAdapter {

	private final StringBuilder builder;
	private static final String ISODATE_FUNCTION = "ISODate";
	private static final String OBJECTID_FUNCTION = "ObjectId";
	private static final String DOLLAR_AND = "$and";
	private static final String DOLLAR_OR = "$or";
	private static final String OR_SPLITTER = " or ";
	private static final String AND_SPLITTER = " and ";

	private static final List<String> ALLOWED_FIELDS = Arrays
			.asList(new String[] { "TimeSerie.windowType", "TimeSerie.timestamp" });

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
	public void visit(OrExpression or) {
		if (!isFirstOr()) {
			setFirstOr(true);
			setOrsInQuery(or.toString().toLowerCase().split(OR_SPLITTER).length);
		}

		if (or.toString().toLowerCase().split(OR_SPLITTER).length <= 2) {
			if (!builder.toString().endsWith("{"))
				builder.append("{");
			builder.append("$or:[");
		}

		super.visit(or);
		Sql2NativeTool.removeIfLastCharacterIsComma(builder);

		if (or.toString().toLowerCase().split(OR_SPLITTER).length == getOrsInQuery()) {
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
			setAndsInQuery(and.toString().toLowerCase().split(AND_SPLITTER).length);
		}
		if (and.toString().toLowerCase().split(AND_SPLITTER).length <= 2) {
			if (!builder.toString().endsWith("{"))
				builder.append("{");
			builder.append("$and:[");
		}
		super.visit(and);
		Sql2NativeTool.removeIfLastCharacterIsComma(builder);
		if (and.toString().toLowerCase().split(AND_SPLITTER).length == getAndsInQuery()) {
			builder.append("]");
			builder.append("}");
			setFirstAnd(false);
		}

		builder.append(",");

	}

	@Override
	public void visit(NotEqualsTo net) {
		if (ALLOWED_FIELDS.contains(net.getLeftExpression().toString())) {
			if (builder.lastIndexOf(DOLLAR_AND) != -1 || builder.lastIndexOf(DOLLAR_OR) != -1)
				builder.append("{");
			builder.append("'" + net.getLeftExpression() + "'");
			builder.append(":{$ne:");
			super.visit(net);
			if (builder.lastIndexOf(DOLLAR_AND) != -1 || builder.lastIndexOf(DOLLAR_OR) != -1)
				builder.append("}");
			builder.append(",");
		}

	}

	@Override
	public void visit(EqualsTo eq) {
		if (ALLOWED_FIELDS.contains(eq.getLeftExpression().toString())) {
			if (builder.lastIndexOf(DOLLAR_AND) != -1 || builder.lastIndexOf(DOLLAR_OR) != -1)
				builder.append("{");
			builder.append("'" + eq.getLeftExpression() + "'");
			builder.append(":{$eq:");
			super.visit(eq);
			if (builder.lastIndexOf(DOLLAR_AND) != -1 || builder.lastIndexOf(DOLLAR_OR) != -1)
				builder.append("}");
			builder.append(",");
		}
	}

	@Override
	public void visit(GreaterThan gt) {
		if (ALLOWED_FIELDS.contains(gt.getLeftExpression().toString())) {
			if (builder.lastIndexOf(DOLLAR_AND) != -1 || builder.lastIndexOf(DOLLAR_OR) != -1)
				builder.append("{");
			builder.append("'" + gt.getLeftExpression() + "'");
			builder.append(":{$gt:");
			super.visit(gt);
			if (builder.lastIndexOf(DOLLAR_AND) != -1 || builder.lastIndexOf(DOLLAR_OR) != -1)
				builder.append("}");
			builder.append(",");
		}
	}

	@Override
	public void visit(GreaterThanEquals gte) {
		if (ALLOWED_FIELDS.contains(gte.getLeftExpression().toString())) {
			if (builder.lastIndexOf(DOLLAR_AND) != -1 || builder.lastIndexOf(DOLLAR_OR) != -1)
				builder.append("{");
			builder.append("'" + gte.getLeftExpression() + "'");
			builder.append(":{$gte:");
			super.visit(gte);
			if (builder.lastIndexOf(DOLLAR_AND) != -1 || builder.lastIndexOf(DOLLAR_OR) != -1)
				builder.append("}");
			builder.append(",");
		}
	}

	@Override
	public void visit(MinorThan lt) {
		if (ALLOWED_FIELDS.contains(lt.getLeftExpression().toString())) {
			if (builder.lastIndexOf(DOLLAR_AND) != -1 || builder.lastIndexOf(DOLLAR_OR) != -1)
				builder.append("{");
			builder.append("'" + lt.getLeftExpression() + "'");
			builder.append(":{$lt:");
			super.visit(lt);
			if (builder.lastIndexOf(DOLLAR_AND) != -1 || builder.lastIndexOf(DOLLAR_OR) != -1)
				builder.append("}");
			builder.append(",");
		}
	}

	@Override
	public void visit(MinorThanEquals lte) {
		if (ALLOWED_FIELDS.contains(lte.getLeftExpression().toString())) {
			if (builder.lastIndexOf(DOLLAR_AND) != -1 || builder.lastIndexOf(DOLLAR_OR) != -1)
				builder.append("{");
			builder.append("'" + lte.getLeftExpression() + "'");
			builder.append(":{$lte:");
			super.visit(lte);
			if (builder.lastIndexOf(DOLLAR_AND) != -1 || builder.lastIndexOf(DOLLAR_OR) != -1)
				builder.append("}");
			builder.append(",");
		}
	}

	@Override
	public void visit(StringValue stringValue) {
		if (isDateTimeFormat(stringValue.getValue()))
			builder.append(getDateTime(stringValue.getValue()));
		else if (isObjectId(stringValue.getValue()))
			builder.append(getObjectId(stringValue.getValue()));
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
		if (value.equalsIgnoreCase("boolean.true") || value.equalsIgnoreCase("boolean.false"))
			return true;
		else
			return false;
	}

	private boolean isDateTimeFormat(String value) {
		return getDateTime(value) != null;

	}

	private boolean isObjectId(String value) {
		return getObjectId(value) != null;
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
		final Pattern pattern = Pattern.compile("^[0-9a-fA-F]{24}$");
		final Matcher matcher = pattern.matcher(oid);
		if (matcher.matches()) {
			return OBJECTID_FUNCTION + "('" + oid + "')";
		} else
			return null;
	}

	private boolean getBooleanValue(String value) {
		return Boolean.valueOf(value.split("\\.")[1]).booleanValue();
	}
}
