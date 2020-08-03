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
package com.minsait.onesait.platform.persistence.external.generator;

import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource.VirtualDatasourceType;
import com.minsait.onesait.platform.persistence.external.generator.helper.*;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.DeleteStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.InsertStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.SelectStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.UpdateStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.common.OrderByStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.common.WhereStatement;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.SelectUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.sql.Types;
import java.util.*;
import java.util.stream.Collectors;

public class SQLGeneratorOpsImpl implements SQLGeneratorOps {

	private SQLHelperImpl sqlHelper = new SQLHelperImpl();
	private OracleHelper oracleHelper = new OracleHelper();
	private Oracle11Helper oracle11Helper = new Oracle11Helper();
	private SQLServerHelper sqlServerHelper = new SQLServerHelper();
	private PostgreSQLHelper postgresSQLHelper = new PostgreSQLHelper();

	@Override
	public PlainSelect getStandardSelect(final SelectStatement selectStatement, final VirtualDatasourceType dsType,
										 final Map<String, Integer> tableColumnTypes){
		final PlainSelect plainSelect = this.getStandardSelectWithoutWhere(selectStatement);
		if(selectStatement.getWhere() != null && !selectStatement.getWhere().isEmpty()) {
			plainSelect.setWhere(this.getWhereForVirtual(selectStatement.getWhere(), dsType, tableColumnTypes));
		}
		if(selectStatement.getOffset() != null){
			return this.getOntologyHelper(dsType).addLimit(plainSelect, selectStatement.getLimit(), selectStatement.getOffset());
		} else{
			return this.getOntologyHelper(dsType).addLimit(plainSelect, selectStatement.getLimit());
		}
	}

	@Override
	public PlainSelect getStandardSelect(final SelectStatement selectStatement){
		final PlainSelect plainSelect = this.getStandardSelectWithoutWhere(selectStatement);
		if(selectStatement.getWhere() != null && !selectStatement.getWhere().isEmpty()) {
			plainSelect.setWhere(this.getWhereForWhereList(selectStatement.getWhere()));
		}
		if(selectStatement.getOffset() != null){
			return sqlHelper.addLimit(plainSelect, selectStatement.getLimit(), selectStatement.getOffset());
		} else {
			return sqlHelper.addLimit(plainSelect, selectStatement.getLimit());
		}
	}

	private PlainSelect getStandardSelectWithoutWhere(final SelectStatement selectStatement){
		final Select select = SelectUtils.buildSelectFromTable(new Table(selectStatement.getOntology()));
		final PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
		plainSelect.setSelectItems(this.getSelectItems(selectStatement.getColumns()));
		plainSelect.setOrderByElements(this.getOrderByFromJsonQuery(selectStatement.getOrderBy()));
		return plainSelect;
	}

	private List<SelectItem> getSelectItems(final List<String> columns){
		if(columns == null || columns.isEmpty()) return Collections.singletonList(new AllColumns());
		return this.generateSQLColumns(columns).stream().map(SelectExpressionItem::new).collect(Collectors.toList());
	}

	private Expression getWhereForVirtual(final List<WhereStatement> whereList,
										  final VirtualDatasourceType dsType,
										  final Map<String, Integer> tableColumnTypes) {
		final int size = whereList.size();
		if(size == 1) return generateExpressionForVirtualWhere(whereList.get(0), dsType, tableColumnTypes);
		else {
			final List<Expression> values = whereList.stream()
					.map(where -> this.generateExpressionForVirtualWhere(where, dsType, tableColumnTypes))
					.collect(Collectors.toList());
			return this.generateWhereForValues(size, whereList, values);
		}
	}

	private Expression getWhereForWhereList(final List<WhereStatement> whereList){
		final int size = whereList.size();
		if(size == 1) return generateExpressionForWhere(whereList.get(0));
		else {
			final List<Expression> values = whereList.stream()
					.map(this::generateExpressionForWhere)
					.collect(Collectors.toList());
			return this.generateWhereForValues(size, whereList, values);
		}
	}

	private Expression generateWhereForValues(final int size, final List<WhereStatement> whereList, final List<Expression> values){
		Expression whereCondition = getConditionExpression(whereList.get(0), values.get(0), values.get(1));
		for (int i = 2; i < size; i++) {
			whereCondition = getConditionExpression(whereList.get(i-1), whereCondition, values.get(i));
		}
		return whereCondition;
	}

	private BinaryExpression getConditionExpression(final WhereStatement where, final Expression leftExpression, final Expression rightExpression){
		return where.getCondition() != null && where.getCondition().equalsIgnoreCase("OR") ? new OrExpression(leftExpression, rightExpression) : new AndExpression(leftExpression, rightExpression);
	}

	private Expression generateExpressionForWhere(final WhereStatement where){
		final Expression value = getSimplifiedExpressionForValue(where.getValue());
		return this.getWhereBinaryExpression(where, new Column(where.getColumn()), value);
	}

	private Expression generateExpressionForVirtualWhere(final WhereStatement where,
												   final VirtualDatasourceType virtualType,
												   final Map<String, Integer> tableColumnTypes ){
		final Expression value = getExpressionForSQLType(tableColumnTypes.get(where.getColumn()), virtualType, where.getValue());
		return this.getWhereBinaryExpression(where, new Column(where.getColumn()), value);
	}

	private BinaryExpression getWhereBinaryExpression(final WhereStatement where, final Column column, final Expression value) {
		final BinaryExpression operator;
		switch (where.getOperator()){
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
				throw new IllegalArgumentException("Operator "+where.getOperator()+" not supported");
		}
		operator.setLeftExpression(column);

		if(where.hasFunction()) {
			final Function function = new Function();
			function.setName(where.getFunction());
			function.setParameters(new ExpressionList(value));
			operator.setRightExpression(function);
		} else {
			operator.setRightExpression(value);
		}

		return operator;
	}

	private List<OrderByElement> getOrderByFromJsonQuery(final List<OrderByStatement> ordersBy) {
		final List<OrderByElement> orderByList = new ArrayList<>();
		if(ordersBy != null && !ordersBy.isEmpty()) {
			for (final OrderByStatement orderBy : ordersBy) {
				final OrderByElement order = new OrderByElement();
				order.setExpression(new Column(orderBy.getColumn()));
				order.setAsc(orderBy.getOrder().equalsIgnoreCase("ASC"));
				orderByList.add(order);
			}
		}
		return orderByList;
	}

	@Override
	public Insert getStandardInsert(final InsertStatement insertStatement){
		final Insert insert = this.generateInsertWithoutValues(insertStatement);
		insert.setItemsList(this.generateSQLMultiExpressionList(insertStatement.getValues()));
		return insert;
	}

	@Override
	public Insert getStandardInsert(final InsertStatement insertStatement,
									final VirtualDatasourceType virtualDatasourceType,
									final Map<String, Integer> tableColumnTypes){
		final Insert insert = this.generateInsertWithoutValues(insertStatement);
		insert.setItemsList(this.generateSQLMultiExpressionList(insertStatement.getValues(), virtualDatasourceType, tableColumnTypes));
		return insert;
	}

	private Insert generateInsertWithoutValues(final InsertStatement insertStatement){
		final Insert insert = new Insert();
		insert.setTable(new Table(insertStatement.getOntology()));
		insert.setColumns(this.generateSQLColumns(insertStatement.getColumns()));
		return insert;
	}

	private List<Column> generateSQLColumns(final List<String> columns) {
		return columns.stream()
				.map(Column::new)
				.collect(Collectors.toList());
	}

	private ExpressionList generateSQLExpressionList(final Map<String, String> values){
		final List<Expression> expressions = values
				.values()
				.stream()
				.map(this::getSimplifiedExpressionForValue)
				.collect(Collectors.toList());

		return new ExpressionList(expressions);
	}

	private ExpressionList generateSQLExpressionListForVirtual(final Map<String, String> values,
															   final VirtualDatasourceType virtualDatasourceType,
															   final Map<String, Integer> tableColumnTypes){
		final List<Expression> expressions = values.entrySet().stream()
				.map( entry -> getExpressionForSQLType(tableColumnTypes.get(entry.getKey()), virtualDatasourceType, entry.getValue()))
				.collect(Collectors.toList());
		return new ExpressionList(expressions);
	}

	private MultiExpressionList generateSQLMultiExpressionList(final List<Map<String, String>> valuesList){
		final MultiExpressionList multiExpressionList = new MultiExpressionList();
		valuesList.stream()
				.map(this::generateSQLExpressionList)
				.forEach(multiExpressionList::addExpressionList);
		return multiExpressionList;
	}

	private MultiExpressionList generateSQLMultiExpressionList(final List<Map<String, String>> valuesList,
															   final VirtualDatasourceType virtualDatasourceType,
															   final Map<String, Integer> tableColumnTypes){
		final MultiExpressionList multiExpressionList = new MultiExpressionList();
		valuesList.stream()
				.map( values -> generateSQLExpressionListForVirtual(values, virtualDatasourceType, tableColumnTypes))
				.forEach(multiExpressionList::addExpressionList);
		return multiExpressionList;
	}

	@Override
	public String getOracleInsertSQL(final InsertStatement insertStatement,
									 final Map<String, Integer> tableColumnTypes){
		if(insertStatement.getValues().size() > 1){
			return this.getOracleMultiInsertSQL(insertStatement, tableColumnTypes);
		} else {
			return this.getStandardInsert(insertStatement, VirtualDatasourceType.ORACLE, tableColumnTypes).toString();
		}
	}

	private String getOracleMultiInsertSQL(final InsertStatement insertStatement,
										   final Map<String, Integer> tableColumnTypes ){
		/*	Oracle multi insert example

			INSERT ALL
			  INTO suppliers (supplier_id, supplier_name) VALUES (1000, 'IBM')
			  INTO suppliers (supplier_id, supplier_name) VALUES (2000, 'Microsoft')
			  INTO suppliers (supplier_id, supplier_name) VALUES (3000, 'Google')
			SELECT * FROM dual;
		*/
		final StringBuilder sb = new StringBuilder();
		sb.append("INSERT ALL");
		final List<Column> columns = this.generateSQLColumns(insertStatement.getColumns());

		for (Map<String, String> values : insertStatement.getValues()) {
			final StringBuilder subSB = new StringBuilder();
			subSB.append(" INTO ")
				.append(insertStatement.getOntology()+" ")
				.append(PlainSelect.getStringList(columns, true, true))
				.append(" VALUES (")
				.append(PlainSelect.getStringList(this.generateSQLExpressionListForVirtual(values, VirtualDatasourceType.ORACLE, tableColumnTypes).getExpressions(),true, false))
				.append(")");

			sb.append(subSB.toString());
		}
		sb.append(" SELECT * FROM dual");
		return sb.toString();
	}

	@Override
	public Update getStandardUpdate(final UpdateStatement updateStatement) {
		final Update update = new Update();
		update.setTables(Collections.singletonList(new Table(updateStatement.getOntology())));
		update.setColumns(this.generateSQLColumns(new ArrayList<>(updateStatement.getValues().keySet())));
		update.setExpressions(this.generateSQLExpressionList(updateStatement.getValues()).getExpressions());
		update.setWhere(this.getWhereForWhereList(updateStatement.getWhere()));
		return update;
	}

	@Override
	public Update getStandardUpdate(final UpdateStatement updateStatement,
									final VirtualDatasourceType virtualDatasourceType,
									final Map<String, Integer> tableColumnTypes) {
		final Update update = new Update();
		update.setTables(Collections.singletonList(new Table(updateStatement.getOntology())));
		update.setColumns(this.generateSQLColumns(new ArrayList<>(updateStatement.getValues().keySet())));
		update.setExpressions(this.generateSQLExpressionListForVirtual(updateStatement.getValues(),virtualDatasourceType,tableColumnTypes).getExpressions());
		update.setWhere(this.getWhereForVirtual(updateStatement.getWhere(),virtualDatasourceType,tableColumnTypes));
		return update;
	}

	@Override
	public Delete getStandardDelete(final DeleteStatement deleteStatement) {
		final Delete delete = new Delete();
		delete.setTable(new Table(deleteStatement.getOntology()));
		delete.setWhere(this.getWhereForWhereList(deleteStatement.getWhere()));
		return delete;
	}

	@Override
	public Delete getStandardDelete(final DeleteStatement deleteStatement,
									final VirtualDatasourceType virtualDatasourceType,
									final Map<String, Integer> tableColumnTypes) {
		final Delete delete = new Delete();
		delete.setTable(new Table(deleteStatement.getOntology()));
		delete.setWhere(this.getWhereForVirtual(deleteStatement.getWhere(), virtualDatasourceType, tableColumnTypes));
		return delete;
	}

	private Expression getSimplifiedExpressionForValue(final String value){
		if(value == null) {
			return new NullValue();
		} else {
			if(NumberUtils.isNumber(value)) {
				return new DoubleValue(value);
			} else {
				if("false".equalsIgnoreCase(value.trim()) || "true".equalsIgnoreCase(value.trim())) {
					return new LongValue("false".equalsIgnoreCase(value.trim()) ? 0 : 1);
				} else{
					return new StringValue(value);
				}
			}
		}
	}

	private Expression getExpressionForSQLType(final Integer sqlType, final VirtualDatasourceType relationalType,
											   final String value){
		if(sqlType == null) {
			throw new IllegalArgumentException("SQL type not found in table metadata");
		} else {
			if (value == null) {
				return new NullValue();
			} else {
				switch (sqlType) {
					case Types.NULL:
						return new NullValue();
					case Types.BOOLEAN:
					case Types.BIT:
					case Types.INTEGER:
					case Types.SMALLINT:
					case Types.TINYINT:
					case Types.ROWID:
					case Types.BIGINT:
						return new LongValue(value);
					case Types.NUMERIC:
					case Types.DECIMAL:
					case Types.DOUBLE:
					case Types.FLOAT:
					case Types.REAL:
						return new DoubleValue(value);
					case Types.CHAR:
					case Types.VARCHAR:
					case Types.LONGVARCHAR:
						return new StringValue(value);
					case Types.NCHAR:
					case Types.NVARCHAR:
					case Types.LONGNVARCHAR:
						switch (relationalType) {
							case SQLSERVER:
								final StringValue newValue = new StringValue(value);
								newValue.setPrefix("N");
								return newValue;
							default:
								return new StringValue(value);
						}
					case Types.TIMESTAMP:
					case Types.TIMESTAMP_WITH_TIMEZONE:
						if (value.trim().isEmpty()) {
							return new StringValue(value);
						} else {
							return new TimestampValue(value);
						}
					case Types.DATE:
						if (value.trim().isEmpty()) {
							return new StringValue(value);
						} else {
							// It needs two characters wrapping the value because JSQLParser trim it
							return new DateValue(" "+value+" ");
						}
					case Types.TIME:
					case Types.TIME_WITH_TIMEZONE:
						if (value.trim().isEmpty()) {
							return new StringValue(value);
						} else {
							// It needs two characters wrapping the value because JSQLParser trim it
							return new TimeValue(" "+value+" ");
						}
					case Types.SQLXML:
						switch (relationalType) {
							case POSTGRESQL:
								final Function function = new Function();
								function.setName("XMLPARSE");
								function.setParameters(new ExpressionList(new StringValue(value)));
								return function;
							case SQLSERVER:
								final StringValue newValue = new StringValue(value);
								newValue.setPrefix("N");
								return newValue;
							default:
								return new StringValue(value);
						}
					case Types.BINARY:
					case Types.VARBINARY:
					case Types.LONGVARBINARY:
					case Types.CLOB:
					case Types.NCLOB:
					case Types.BLOB:
						return new HexValue(value);
					case Types.STRUCT:
					case Types.REF_CURSOR:
					case Types.REF:
					case Types.DATALINK:
					case Types.DISTINCT:
					case Types.JAVA_OBJECT:
					case Types.OTHER:
					case Types.ARRAY:
						throw new IllegalArgumentException("Row type " + sqlType + " not supported");
					default:
						throw new IllegalArgumentException("Row type " + sqlType + " not recognized");
				}
			}
		}
	}

	private SQLHelper getOntologyHelper(final VirtualDatasourceType type) {
		switch (type) {
			case SQLSERVER:
				return sqlServerHelper;
			case ORACLE11:
				return oracle11Helper;
			case ORACLE:
				return oracleHelper;
			case POSTGRESQL:
				return postgresSQLHelper;
			case MARIADB:
			case MYSQL:
			case HIVE:
			case IMPALA:
			default:
				return sqlHelper;
		}
	}

}
