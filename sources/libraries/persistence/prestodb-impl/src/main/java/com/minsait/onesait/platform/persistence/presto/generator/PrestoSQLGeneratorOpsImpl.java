/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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
package com.minsait.onesait.platform.persistence.presto.generator;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.NumberUtils;
import org.jline.utils.Log;
import org.springframework.util.Assert;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.minsait.onesait.platform.config.services.exceptions.OPResourceServiceException;
import com.minsait.onesait.platform.persistence.presto.generator.model.common.ColumnPresto;
import com.minsait.onesait.platform.persistence.presto.generator.model.common.PrestoOrderByStatement;
import com.minsait.onesait.platform.persistence.presto.generator.model.common.PrestoWhereStatement;
import com.minsait.onesait.platform.persistence.presto.generator.model.statements.PrestoCreateStatement;
import com.minsait.onesait.platform.persistence.presto.generator.model.statements.PrestoDeleteStatement;
import com.minsait.onesait.platform.persistence.presto.generator.model.statements.PrestoDropStatement;
import com.minsait.onesait.platform.persistence.presto.generator.model.statements.PrestoInsertStatement;
import com.minsait.onesait.platform.persistence.presto.generator.model.statements.PrestoPreparedStatement;
import com.minsait.onesait.platform.persistence.presto.generator.model.statements.PrestoSelectStatement;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.HexValue;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.values.ValuesStatement;
import net.sf.jsqlparser.util.SelectUtils;

public class PrestoSQLGeneratorOpsImpl implements PrestoSQLGeneratorOps {

	private final PrestoSQLHelperImpl sqlHelper = new PrestoSQLHelperImpl();

	private class Sequence {

		private int i = -1;

		public int next() {
			return ++i;
		}
	}

	@Override
	public PrestoPreparedStatement getStandardSelect(final PrestoSelectStatement selectStatement,
			final Map<String, Integer> tableColumnTypes, boolean withParams) {

		final Map<String, Object> jdbcParams = new HashMap<>();

		final PlainSelect plainSelect = this.getStandardSelectWithoutWhere(selectStatement);
		if (selectStatement.getWhere() != null && !selectStatement.getWhere().isEmpty()) {
			plainSelect.setWhere(this.getWhere(selectStatement.getWhere(), tableColumnTypes, new Sequence(), jdbcParams,
					withParams));
		}

		PlainSelect plainSelectLimited;
		if (selectStatement.getOffset() != null) {
			plainSelectLimited = sqlHelper.addLimit(plainSelect, selectStatement.getLimit(),
					selectStatement.getOffset());
		} else {
			plainSelectLimited = sqlHelper.addLimit(plainSelect, selectStatement.getLimit());
		}

		final PrestoPreparedStatement ps = new PrestoPreparedStatement(plainSelectLimited.toString());
		ps.setParams(jdbcParams);
		return ps;
	}

	@Override
	public PrestoPreparedStatement getStandardSelect(final PrestoSelectStatement selectStatement, boolean withParams) {
		final Map<String, Object> jdbcParams = new HashMap<>();

		final PlainSelect plainSelect = this.getStandardSelectWithoutWhere(selectStatement);
		if (selectStatement.getWhere() != null && !selectStatement.getWhere().isEmpty()) {
			plainSelect.setWhere(
					this.getWhereForWhereList(selectStatement.getWhere(), new Sequence(), jdbcParams, withParams));
		}

		PlainSelect plainSelectLimited;
		if (selectStatement.getOffset() != null) {
			plainSelectLimited = sqlHelper.addLimit(plainSelect, selectStatement.getLimit(),
					selectStatement.getOffset());
		} else {
			plainSelectLimited = sqlHelper.addLimit(plainSelect, selectStatement.getLimit());
		}

		final PrestoPreparedStatement ps = new PrestoPreparedStatement(plainSelectLimited.toString());
		ps.setParams(jdbcParams);
		return ps;
	}

	private PlainSelect getStandardSelectWithoutWhere(final PrestoSelectStatement selectStatement) {
		final Table table = new Table(selectStatement.getOntology());
		if (selectStatement.getAlias() != null) {
			table.setAlias(new Alias(selectStatement.getAlias()));
		}
		final Select select = SelectUtils.buildSelectFromTable(table);
		final PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
		plainSelect.setSelectItems(this.getSelectItems(selectStatement.getColumns()));
		plainSelect.setOrderByElements(this.getOrderByFromJsonQuery(selectStatement.getOrderBy()));
		return plainSelect;
	}

	private List<SelectItem> getSelectItems(final List<String> columns) {
		if (columns == null || columns.isEmpty()) {
			return Collections.singletonList(new AllColumns());
		}
		return this.generateSQLColumns(columns).stream().map(SelectExpressionItem::new).collect(Collectors.toList());
	}

	private Expression getWhere(final List<PrestoWhereStatement> whereList, final Map<String, Integer> tableColumnTypes,
			Sequence seq, Map<String, Object> jdbcParams, boolean withParams) {
		final int size = whereList.size();
		if (size == 1) {
			return generateExpressionForVirtualWhere(whereList.get(0), tableColumnTypes, seq, jdbcParams, withParams);
		} else {
			final List<Expression> values = whereList.stream().map(where -> this
					.generateExpressionForVirtualWhere(where, tableColumnTypes, seq, jdbcParams, withParams))
					.collect(Collectors.toList());
			return this.generateWhereForValues(size, whereList, values);
		}
	}

	private Expression getWhereForWhereList(final List<PrestoWhereStatement> whereList, Sequence seq,
			Map<String, Object> jdbcParams, boolean withParams) {
		final int size = whereList.size();
		if (size == 1) {
			return this.generateExpressionForWhere(whereList.get(0), seq, jdbcParams, withParams);
		} else {
			final List<Expression> values = whereList.stream()
					.map(w -> generateExpressionForWhere(w, seq, jdbcParams, withParams)).collect(Collectors.toList());
			return this.generateWhereForValues(size, whereList, values);
		}
	}

	private Expression generateWhereForValues(final int size, final List<PrestoWhereStatement> whereList,
			final List<Expression> values) {
		Expression whereCondition = getConditionExpression(whereList.get(0), values.get(0), values.get(1));
		for (int i = 2; i < size; i++) {
			whereCondition = getConditionExpression(whereList.get(i - 1), whereCondition, values.get(i));
		}
		return whereCondition;
	}

	private BinaryExpression getConditionExpression(final PrestoWhereStatement where, final Expression leftExpression,
			final Expression rightExpression) {
		return where.getCondition() != null && where.getCondition().equalsIgnoreCase("OR")
				? new OrExpression(leftExpression, rightExpression)
				: new AndExpression(leftExpression, rightExpression);
	}

	private Expression generateExpressionForWhere(final PrestoWhereStatement where, Sequence seq,
			Map<String, Object> jdbcParams, boolean withParams) {
		BinaryExpression expression;
		if (withParams) {
			final Object value = getValueForValue(where.getValue());
			final String paramName = where.getColumn() + seq.next();
			Assert.isTrue(!jdbcParams.containsKey(paramName), "Duplicated parameter");
			jdbcParams.put(paramName, value);
			expression = this.getWhereBinaryExpression(where, new Column(where.getColumn()),
					new JdbcNamedParameter(paramName));
		} else {
			expression = this.getWhereBinaryExpression(where, new Column(where.getColumn()),
					getExpressionForValue(where.getValue()));
		}
		return expression;
	}

	private Expression generateExpressionForVirtualWhere(final PrestoWhereStatement where,
			final Map<String, Integer> tableColumnTypes, Sequence seq, Map<String, Object> jdbcParams,
			boolean withParams) {
		BinaryExpression expression;
		if (withParams) {
			final Object value = getValueForSQLType(tableColumnTypes.get(where.getColumn()), where.getValue());
			final String paramName = where.getColumn() + seq.next();
			Assert.isTrue(!jdbcParams.containsKey(paramName), "Duplicated parameter");
			jdbcParams.put(paramName, value);
			expression = this.getWhereBinaryExpression(where, new Column(where.getColumn()),
					new JdbcNamedParameter(paramName));
		} else {
			expression = this.getWhereBinaryExpression(where, new Column(where.getColumn()),
					getExpressionForSQLType(tableColumnTypes.get(where.getColumn()), where.getValue()));
		}
		return expression;
	}

	private BinaryExpression getWhereBinaryExpression(final PrestoWhereStatement where, final Column column,
			final Expression value) {
		final BinaryExpression operator;
		switch (where.getOperator()) {
		case "=":
			operator = new EqualsTo();
			break;
		case "!=":
			operator = new NotEqualsTo();
			break;
		case "<":
			operator = new MinorThan();
			break;
		case ">":
			operator = new GreaterThan();
			break;
		case "<=":
			operator = new MinorThanEquals();
			break;
		case ">=":
			operator = new GreaterThanEquals();
			break;
		default:
			throw new IllegalArgumentException("Operator " + where.getOperator() + " not supported");
		}
		operator.setLeftExpression(column);

		if (where.hasFunction()) {
			final Function function = new Function();
			function.setName(where.getFunction());
			function.setParameters(new ExpressionList(value));
			operator.setRightExpression(function);
		} else {
			operator.setRightExpression(value);
		}

		return operator;
	}

	private List<OrderByElement> getOrderByFromJsonQuery(final List<PrestoOrderByStatement> ordersBy) {
		final List<OrderByElement> orderByList = new ArrayList<>();
		if (ordersBy != null && !ordersBy.isEmpty()) {
			for (final PrestoOrderByStatement orderBy : ordersBy) {
				final OrderByElement order = new OrderByElement();
				order.setExpression(new Column(orderBy.getColumn()));
				order.setAsc(orderBy.getOrder().equalsIgnoreCase("ASC"));
				orderByList.add(order);
			}
		}
		return orderByList;
	}

	@Override
	public PrestoPreparedStatement getStandardInsert(final PrestoInsertStatement insertStatement, boolean withParams) {

		final Map<String, Object> jdbcParams = new HashMap<>();
		final Insert insert = this.generateInsertWithoutValues(insertStatement, this
				.generateSQLMultiExpressionList(insertStatement.getValues(), new Sequence(), jdbcParams, withParams));

		final PrestoPreparedStatement ps = new PrestoPreparedStatement(insert.toString());
		ps.setParams(jdbcParams);
		return ps;
	}

	@Override
	public PrestoPreparedStatement getStandardInsert(final PrestoInsertStatement insertStatement,
			final Map<String, Integer> tableColumnTypes, boolean withParams) {

		final Map<String, Object> jdbcParams = new HashMap<>();
		final Insert insert = this.generateInsertWithoutValues(insertStatement, this.generateSQLMultiExpressionList(
				insertStatement.getValues(), tableColumnTypes, new Sequence(), jdbcParams, withParams));

		final PrestoPreparedStatement ps = new PrestoPreparedStatement(insert.toString());
		ps.setParams(jdbcParams);
		return ps;
	}

	private Insert generateInsertWithoutValues(final PrestoInsertStatement insertStatement, Select valuesExpressionList) {
		final Insert insert = new Insert().withTable(new Table(insertStatement.getOntology()))
				.withColumns(this.generateSQLColumns(insertStatement.getColumns())).withSelect(valuesExpressionList);
		return insert;
	}

	private List<Column> generateSQLColumns(final List<String> columns) {
		return columns.stream().map(Column::new).collect(Collectors.toList());
	}

	public List<ColumnPresto> generateColumns(final String ontologyJsonSchema) {
		final List<ColumnPresto> cols = new ArrayList<>();
		try {
			final JsonParser parser = new JsonParser();
			final JsonElement jsonTree = parser.parse(ontologyJsonSchema);
			if (jsonTree.isJsonObject()) {
				final JsonObject jsonObject = jsonTree.getAsJsonObject();
				extractAllFieldsFromJson(cols, jsonObject);
			} else {
				throw new OPResourceServiceException(
						"Invalid schema to be converted to SQL schema: " + ontologyJsonSchema);
			}

		} catch (final Exception e) {
			Log.warn("Not possible to convert schema to SQL schema: ", e.getMessage());
			throw new OPResourceServiceException("Not possible to convert schema to SQL schema: " + e.getMessage());
		}
		return cols;
	}

	private void extractAllFieldsFromJson(List<ColumnPresto> cols, JsonObject datosJson) {
		if (datosJson.has("properties")) {
			final JsonObject propertiesJson = datosJson.get("properties").getAsJsonObject();
			final Set<Entry<String, JsonElement>> keyValues = propertiesJson.entrySet();
			for (final Entry<String, JsonElement> entry : keyValues) {
				final String fieldName = entry.getKey();
				final JsonObject fieldSpec = entry.getValue().getAsJsonObject();
				final boolean fieldIsRequired = fieldSpec.get("required").getAsBoolean();
				final String fieldDescription = fieldSpec.get("id").getAsString();
				final String fieldType = fieldSpec.get("type").getAsString();

				final ColumnPresto col = new ColumnPresto();
				final ColDataType colDT = new ColDataType();
				colDT.setDataType(fieldType);
				col.setColDataType(colDT);
				col.setColumnName(fieldName);
				col.setNotNull(fieldIsRequired);
				col.setColComment(fieldDescription);
				cols.add(col);
			}
		}
	}

	private ExpressionList generateSQLExpressionList(final Map<String, String> values, final Sequence seq,
			Map<String, Object> jdbcParams, boolean withParams) {
		final List<Expression> expressions = new ArrayList<>();
		for (final Map.Entry<String, String> entry : values.entrySet()) {
			if (withParams) {
				final String paramName = entry.getKey() + seq.next();
				Assert.isTrue(!jdbcParams.containsKey(paramName), "Duplicated param");
				jdbcParams.put(paramName, getValueForValue(values.get(entry.getKey())));
				expressions.add(new JdbcNamedParameter(paramName));
			} else {
				expressions.add(getExpressionForValue(values.get(entry.getKey())));
			}
		}
		return new ExpressionList(expressions);
	}

	private ExpressionList generateSQLExpressionList(final Map<String, String> values,
			final Map<String, Integer> tableColumnTypes, final Sequence seq, Map<String, Object> jdbcParams,
			boolean withParams) {
		final List<Expression> expressions = new ArrayList<>();
		for (final Map.Entry<String, String> entry : values.entrySet()) {
			final String paramName = entry.getKey() + seq.next();
			Assert.isTrue(!jdbcParams.containsKey(paramName), "Duplicated param");
			if (withParams) {
				jdbcParams.put(paramName, getValueForSQLType(tableColumnTypes.get(entry.getKey()), entry.getValue()));
				expressions.add(new JdbcNamedParameter(paramName));
			} else {
				expressions.add(getExpressionForSQLType(tableColumnTypes.get(entry.getKey()), entry.getValue()));
			}
		}
		return new ExpressionList(expressions);
	}

	private Select generateSQLMultiExpressionList(final List<Map<String, String>> valuesList, Sequence seq,
			Map<String, Object> jdbcParams, boolean withParams) {
		final MultiExpressionList multiExpressionList = new MultiExpressionList();
		for (final Map<String, String> element : valuesList) {
			multiExpressionList.addExpressionList(generateSQLExpressionList(element, seq, jdbcParams, withParams));
		}
		return new Select().withSelectBody(new ValuesStatement().withExpressions(multiExpressionList));
	}

	private Select generateSQLMultiExpressionList(final List<Map<String, String>> valuesList,
			final Map<String, Integer> tableColumnTypes, Sequence seq, Map<String, Object> jdbcParams,
			boolean withParams) {
		final MultiExpressionList multiExpressionList = new MultiExpressionList();
		for (final Map<String, String> element : valuesList) {
			multiExpressionList.addExpressionList(
					generateSQLExpressionList(element, tableColumnTypes, seq, jdbcParams, withParams));
		}

		return new Select().withSelectBody(new ValuesStatement().withExpressions(multiExpressionList));
	}

	@Override
	public PrestoPreparedStatement getStandardDelete(final PrestoDeleteStatement deleteStatement, boolean withParams) {

		final Map<String, Object> jdbcParams = new HashMap<>();

		final Delete delete = new Delete();
		delete.setTable(new Table(deleteStatement.getOntology()));
		delete.setWhere(this.getWhereForWhereList(deleteStatement.getWhere(), new Sequence(), jdbcParams, withParams));

		final PrestoPreparedStatement ps = new PrestoPreparedStatement(delete.toString());
		ps.setParams(jdbcParams);
		return ps;
	}

	@Override
	public PrestoPreparedStatement getStandardDelete(final PrestoDeleteStatement deleteStatement,
			final Map<String, Integer> tableColumnTypes, boolean withParams) {
		final Map<String, Object> jdbcParams = new HashMap<>();
		final Delete delete = new Delete();
		delete.setTable(new Table(deleteStatement.getOntology()));
		PrestoPreparedStatement ps;
		delete.setWhere(
				this.getWhere(deleteStatement.getWhere(), tableColumnTypes, new Sequence(), jdbcParams, withParams));
		ps = new PrestoPreparedStatement(delete.toString());
		ps.setParams(jdbcParams);
		return ps;
	}

	private Expression getExpressionForValue(final String value) {
		return (Expression) getObjectForValue(value, false);
	}

	private Object getValueForValue(final String value) {
		return getObjectForValue(value, true);
	}

	private Object getObjectForValue(final String value, boolean raw) {
		if (value == null) {
			return raw ? null : new NullValue();
		} else {
			if (NumberUtils.isNumber(value)) {
				return raw ? new DoubleValue(value).getValue() : new DoubleValue(value);
			} else {
				if ("false".equalsIgnoreCase(value.trim()) || "true".equalsIgnoreCase(value.trim())) {
					final LongValue longExpression = new LongValue("false".equalsIgnoreCase(value.trim()) ? 0 : 1);
					return raw ? longExpression.getValue() : longExpression;
				} else {
					return raw ? new StringValue(value).getValue() : new StringValue(value);
				}
			}
		}
	}

	private Expression getExpressionForSQLType(final Integer sqlType, final String value) {
		return (Expression) getObjectForSQLType(sqlType, value, false);
	}

	private Object getValueForSQLType(final Integer sqlType, final String value) {
		return getObjectForSQLType(sqlType, value, true);
	}

	private Object getObjectForSQLType(final Integer sqlType, final String value, boolean raw) {
		if (sqlType == null) {
			throw new IllegalArgumentException("SQL type not found in table metadata");
		} else {
			if (value == null) {
				return raw ? null : new NullValue();
			} else {
				switch (sqlType) {
				case Types.NULL:
					return raw ? null : new NullValue();
				case Types.BOOLEAN:
					return raw ? Boolean.parseBoolean(value) : new LongValue(value);
				case Types.BIT:
				case Types.INTEGER:
				case Types.SMALLINT:
				case Types.TINYINT:
				case Types.ROWID:
				case Types.BIGINT:
					return raw ? new LongValue(value).getValue() : new LongValue(value);
				case Types.NUMERIC:
				case Types.DECIMAL:
				case Types.DOUBLE:
				case Types.FLOAT:
				case Types.REAL:
					return raw ? new DoubleValue(value).getValue() : new DoubleValue(value);
				case Types.CHAR:
				case Types.VARCHAR:
				case Types.LONGVARCHAR:
				case Types.OTHER:
					return raw ? new StringValue(value).getValue() : new StringValue(value);
				case Types.NCHAR:
				case Types.NVARCHAR:
				case Types.LONGNVARCHAR:
					return raw ? new StringValue(value).getValue() : new StringValue(value);
				case Types.TIMESTAMP:
				case Types.TIMESTAMP_WITH_TIMEZONE:
					if (value.trim().isEmpty()) {
						return raw ? new StringValue(value).getValue() : new StringValue(value);
					} else {
						return raw ? new TimestampValue(value).getValue() : new TimestampValue(value);
					}
				case Types.DATE:
					if (value.trim().isEmpty()) {
						return raw ? new StringValue(value).getValue() : new StringValue(value);
					} else {
						// It needs two characters wrapping the value because JSQLParser trim it
						return raw ? new DateValue(" " + value + " ").getValue() : new DateValue(" " + value + " ");
					}
				case Types.TIME:
				case Types.TIME_WITH_TIMEZONE:
					if (value.trim().isEmpty()) {
						return raw ? new StringValue(value).getValue() : new StringValue(value);
					} else {
						// It needs two characters wrapping the value because JSQLParser trim it
						return raw ? new TimeValue(" " + value + " ").getValue() : new TimeValue(" " + value + " ");
					}
				case Types.SQLXML:
					return raw ? new StringValue(value).getValue() : new StringValue(value);
				case Types.BINARY:
				case Types.VARBINARY:
				case Types.LONGVARBINARY:
				case Types.CLOB:
				case Types.NCLOB:
				case Types.BLOB:
					return raw ? new HexValue(value).getValue() : new HexValue(value);
				case Types.STRUCT:
				case Types.REF_CURSOR:
				case Types.REF:
				case Types.DATALINK:
				case Types.DISTINCT:
				case Types.JAVA_OBJECT:
				case Types.ARRAY:
					throw new IllegalArgumentException("Row type " + sqlType + " not supported");
				default:
					throw new IllegalArgumentException("Row type " + sqlType + " not recognized");
				}
			}
		}
	}

	@Override
	public PrestoPreparedStatement getStandardCreate(PrestoCreateStatement createStatement) {
		createStatement = sqlHelper.parseCreateStatementColumns(createStatement);

		return new PrestoPreparedStatement(createStatement.toString());
	}

	@Override
	public PrestoPreparedStatement getStandardDrop(PrestoDropStatement dropStatement) {
		return new PrestoPreparedStatement(dropStatement.toString());
	}

}
