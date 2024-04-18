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
package com.minsait.onesait.platform.persistence.cosmosdb.utils.sql;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.commons.exception.GenericRuntimeOPException;
import com.minsait.onesait.platform.persistence.cosmosdb.utils.sql.UpdateOperation.Type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;

@NoArgsConstructor
@AllArgsConstructor
public class CosmosDBExpressionVisitorAdapter extends ExpressionVisitorAdapter {

	@Getter
	private Object value = null;
	@Getter
	private UpdateOperation resultOperation;
	private static final String APPEND_FUNCTION = "APPEND";
	private static final String BOOLEAN_FUNCTION = "BOOLEAN";
	private static final String OBJECT = "OBJECT";
	private static final String UNSET = "UNSET";
	private static final ObjectMapper mapper = new ObjectMapper();

	@Override
	public void visit(StringValue stringValue) {
		if (isBooleanValue(stringValue.getValue())) {
			value = getBooleanValue(stringValue.getValue());
			resultOperation = UpdateOperation.builder().type(Type.SET).value(value).build();
		} else if (isObject(stringValue.getValue())) {
			final JsonNode object = toObject(stringValue.getValue());
			value = object;
			resultOperation = UpdateOperation.builder().type(Type.SET).value(value).build();
		} else {
			value = stringValue.getValue();
			resultOperation = UpdateOperation.builder().type(Type.SET).value(value).build();
		}

	}

	@Override
	public void visit(LongValue longValue) {
		value = longValue.getValue();
		resultOperation = UpdateOperation.builder().type(Type.SET).value(value).build();

	}

	@Override
	public void visit(DoubleValue doubleValue) {
		value = doubleValue.getValue();
		resultOperation = UpdateOperation.builder().type(Type.SET).value(value).build();
	}

	@Override
	public void visit(NullValue nullValue) {
		resultOperation = UpdateOperation.builder().type(Type.SET).value(null).build();
	}

	@Override
	public void visit(Function function) {
		if (function.getName().equalsIgnoreCase(APPEND_FUNCTION)) {
			final ExpressionList params = function.getParameters();

			if (params.getExpressions().size() == 1 || params.getExpressions().size() == 2) {
				// argument is object?
				final String param = params.getExpressions().get(0).toString();
				if (isObject(param)) {
					// param is object
					final JsonNode object = toObject(param);
					value = object;
					if (object.isArray()) {
						resultOperation = UpdateOperation.builder().type(Type.APPEND_MULTIPLE).value(value).build();
					} else {
						resultOperation = UpdateOperation.builder().type(Type.APPEND).value(value).build();
					}
				} else {
					// TO-DO if subvalue is boolean or int etc not an string
					value = param;
					resultOperation = UpdateOperation.builder().type(Type.APPEND).value(value).build();
				}
			} else {
				throw new GenericRuntimeOPException("Incorrect use of APPEND function");
			}

		} else if (function.getName().equalsIgnoreCase(BOOLEAN_FUNCTION)) {
			final ExpressionList params = function.getParameters();
			if (params.getExpressions().size() == 1) {
				final String param = params.getExpressions().get(0).toString();
				value = Boolean.valueOf(param);
				resultOperation = UpdateOperation.builder().type(Type.SET).value(value).build();
			} else {
				throw new RuntimeException("Incorrect use of " + BOOLEAN_FUNCTION + " function");
			}
		} else if (function.getName().equalsIgnoreCase(UNSET)) {
			resultOperation = UpdateOperation.builder().type(Type.UNSET).value(null).build();
		} else if (function.getName().equalsIgnoreCase(OBJECT)) {

			final ExpressionList params = function.getParameters();
			if (params.getExpressions().size() == 1) {
				final String param = params.getExpressions().get(0).toString();
				final JsonNode object = toObject(param);
				value = object;
				resultOperation = UpdateOperation.builder().type(Type.SET).value(value).build();
			} else {
				throw new RuntimeException("Incorrect use of " + OBJECT + " function");
			}
		} else {
			throw new GenericRuntimeOPException("SQL Function " + function.getName() + " not supported");
		}

	}

	private boolean isBooleanValue(String value) {
		return value.equalsIgnoreCase("boolean.true") || value.equalsIgnoreCase("boolean.false");
	}

	private boolean getBooleanValue(String value) {
		return Boolean.valueOf(value.split("\\.")[1]).booleanValue();
	}

	private boolean isObject(String value) {
		try {

			value = value.replaceAll("'", "\"").replaceAll("\\\\\"", "\"");
			if (value.startsWith("\"") && value.endsWith("\"")) {
				value = value.substring(0 + 1, value.length() - 1);
			}
			mapper.readTree(value);
			return value.startsWith("{") || value.startsWith("[");
		} catch (final Exception e) {
			return false;
		}

	}

	private JsonNode toObject(String value) {
		try {
			value = value.replaceAll("'", "\"").replaceAll("\\\\\"", "\"");
			if (value.startsWith("\"") && value.endsWith("\"")) {
				value = value.substring(0 + 1, value.length() - 1);
			}
			return mapper.readTree(value);
		} catch (final Exception e) {
			return mapper.createObjectNode();
		}
	}

}
