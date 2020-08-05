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
package com.minsait.onesait.platform.persistence.mongodb.tools.sql;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.commons.exception.GenericRuntimeOPException;

import lombok.AllArgsConstructor;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;

@AllArgsConstructor
public class UpdateSetVisitorAdapter extends ExpressionVisitorAdapter {

	private final StringBuilder builder;
	private final int positionInStatement;
	private static final String ISODATE_FUNCTION = "ISODate";
	private static final String OBJECTID_FUNCTION = "ObjectId";
	private static final String APPEND_FUNCTION = "APPEND";
	private static final String BOOLEAN_FUNCTION = "BOOLEAN";
	private static final String OBJECT = "OBJECT";
	private static final ObjectMapper mapper = new ObjectMapper();

	@Override
	public void visit(StringValue stringValue) {
		if (isDateTimeFormat(stringValue.getValue()))
			builder.append(getDateTime(stringValue.getValue()));
		else if (isObjectId(stringValue.getValue()))
			builder.append(getObjectId(stringValue.getValue()));
		else if (isBooleanValue(stringValue.getValue()))
			builder.append(getBooleanValue(stringValue.getValue()));
		else if (isObject(stringValue.getValue())) {
			final JsonNode object = toObject(stringValue.getValue());
			builder.append(toObjectString(object));
		} else
			builder.append(stringValue);

	}

	@Override
	public void visit(LongValue longValue) {
		builder.append(longValue.getStringValue());

	}

	@Override
	public void visit(DoubleValue doubleValue) {
		builder.append(doubleValue.getValue());
	}

	@Override
	public void visit(NullValue value) {
		builder.append(value.toString().toLowerCase());
	}

	@Override
	public void visit(Function function) {
		if (function.getName().equalsIgnoreCase(APPEND_FUNCTION)) {
			final ExpressionList params = function.getParameters();
			int push2Pos;
			final String statement = "$push:{";
			// First look where to insert $push cmd
			if (positionInStatement == 0) {
				push2Pos = 0;
				builder.insert(push2Pos, statement);
			} else {
				push2Pos = builder.lastIndexOf(",");
				builder.insert(push2Pos + 1, statement);
			}

			if (params.getExpressions().size() == 1) {
				// argument is object?
				final String param = params.getExpressions().get(0).toString();
				if (isObject(param)) {
					// param is object
					final JsonNode object = toObject(param);
					// is array?
					if (object.isArray()) {
						builder.append("{$each:" + toObjectString(object).concat("}}"));
					} else {
						builder.append(toObjectString(object).concat("}"));
					}
				} else {
					builder.append(param.concat("}"));
				}
			} else if (params.getExpressions().size() == 2) {
				final String param = params.getExpressions().get(1).toString();
				if (isObject(param)) {
					// param is object
					final JsonNode object = toObject(param);
					// is array?
					if (object.isArray()) {
						builder.append("{$each:" + toObjectString(object)
								.concat(",$position:" + params.getExpressions().get(0).toString() + "}}"));
					} else {
						builder.append("{$each:[" + toObjectString(object)
								.concat("], $position:" + params.getExpressions().get(0).toString() + "}}"));
					}
				} else {
					builder.append("{$each:[" + param.concat("]}}"));
				}
			} else {
				throw new GenericRuntimeOPException("Incorrect use of APPEND function");
			}

		} else if (function.getName().equalsIgnoreCase(BOOLEAN_FUNCTION)) {
			final ExpressionList params = function.getParameters();
			if (params.getExpressions().size() == 1) {
				final String param = params.getExpressions().get(0).toString();
				builder.append(Boolean.valueOf(param));
				builder.append("}");
			} else {
				throw new RuntimeException("Incorrect use of " + BOOLEAN_FUNCTION + " function");
			}

		} else if (function.getName().equalsIgnoreCase(OBJECT)) {

			final ExpressionList params = function.getParameters();
			if (params.getExpressions().size() == 1) {
				final String param = params.getExpressions().get(0).toString();
				final JsonNode object = toObject(param);
				builder.append(toObjectString(object));
			} else {
				throw new RuntimeException("Incorrect use of " + OBJECT + " function");
			}
		} else {
			throw new GenericRuntimeOPException("SQL Function " + function.getName() + " not supported");
		}

	}

	private boolean isBooleanValue(String value) {
		return (value.equalsIgnoreCase("boolean.true") || value.equalsIgnoreCase("boolean.false"));
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
		} else {
			return null;
		}
	}

	private boolean getBooleanValue(String value) {
		return Boolean.valueOf(value.split("\\.")[1]).booleanValue();
	}

	private boolean isObject(String value) {
		try {

			value = value.replaceAll("'", "\"").replaceAll("\\\\\"", "\"");
			if (value.startsWith("\"") && value.endsWith("\""))
				value = value.substring(0 + 1, value.length() - 1);
			mapper.readTree(value);
			return true;
		} catch (final Exception e) {
			return false;
		}

	}

	private JsonNode toObject(String value) {
		try {
			value = value.replaceAll("'", "\"").replaceAll("\\\\\"", "\"");
			if (value.startsWith("\"") && value.endsWith("\""))
				value = value.substring(0 + 1, value.length() - 1);
			return mapper.readTree(value);
		} catch (final Exception e) {
			return mapper.createObjectNode();
		}
	}

	private String toObjectString(JsonNode node) {
		try {
			return mapper.writeValueAsString(node);
		} catch (final JsonProcessingException e) {
			return "";
		}
	}

}
