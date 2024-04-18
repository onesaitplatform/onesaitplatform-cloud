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

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.math.NumberUtils;
import org.jline.utils.Log;
import org.springframework.util.Assert;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource.VirtualDatasourceType;
import com.minsait.onesait.platform.config.services.exceptions.OPResourceServiceException;
import com.minsait.onesait.platform.persistence.external.generator.helper.Oracle11Helper;
import com.minsait.onesait.platform.persistence.external.generator.helper.OracleHelper;
import com.minsait.onesait.platform.persistence.external.generator.helper.PostgreSQLHelper;
import com.minsait.onesait.platform.persistence.external.generator.helper.SQLHelper;
import com.minsait.onesait.platform.persistence.external.generator.helper.SQLHelperImpl;
import com.minsait.onesait.platform.persistence.external.generator.helper.SQLServerHelper;
import com.minsait.onesait.platform.persistence.external.generator.model.common.ColumnRelational;
import com.minsait.onesait.platform.persistence.external.generator.model.common.OrderByStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.common.WhereStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.CreateStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.DeleteStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.DropStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.InsertStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.PreparedStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.SelectStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.UpdateStatement;

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
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.SelectUtils;

public class SQLGeneratorOpsImpl implements SQLGeneratorOps {

    private SQLHelperImpl sqlHelper = new SQLHelperImpl();
    private PostgreSQLHelper postgresSQLHelper = new PostgreSQLHelper();
    private OracleHelper oracleHelper = new OracleHelper();
    private Oracle11Helper oracle11Helper = new Oracle11Helper();
    private SQLServerHelper sqlServerHelper = new SQLServerHelper();
    
    private class Sequence {
        
        private int i = -1;
        
        public int next () {
            return ++i;
        }
    }

    @Override
    public PreparedStatement getStandardSelect(final SelectStatement selectStatement, final VirtualDatasourceType dsType,
            final Map<String, Integer> tableColumnTypes, boolean withParams) {
        
        
        Map<String, Object> jdbcParams = new HashMap<>();
        
        final PlainSelect plainSelect = this.getStandardSelectWithoutWhere(selectStatement);
        if (selectStatement.getWhere() != null && !selectStatement.getWhere().isEmpty()) {
            plainSelect.setWhere(this.getWhereForVirtual(selectStatement.getWhere(), dsType, tableColumnTypes, new Sequence(), jdbcParams, withParams));
        }
        
        PlainSelect plainSelectLimited;
        if (selectStatement.getOffset() != null) {
            plainSelectLimited = this.getOntologyHelper(dsType).addLimit(plainSelect, selectStatement.getLimit(),
                    selectStatement.getOffset());
        } else {
            plainSelectLimited = this.getOntologyHelper(dsType).addLimit(plainSelect, selectStatement.getLimit());
        }
        
        PreparedStatement ps = new PreparedStatement(plainSelectLimited.toString());
        ps.setParams(jdbcParams);
        return ps;
    }

    @Override
    public PreparedStatement getStandardSelect(final SelectStatement selectStatement, boolean withParams) {     
        Map<String, Object> jdbcParams = new HashMap<>();
        
        final PlainSelect plainSelect = this.getStandardSelectWithoutWhere(selectStatement);
        if (selectStatement.getWhere() != null && !selectStatement.getWhere().isEmpty()) {
            plainSelect.setWhere(this.getWhereForWhereList(selectStatement.getWhere(), new Sequence(), jdbcParams, withParams));
        }
        
        PlainSelect plainSelectLimited;
        if (selectStatement.getOffset() != null) {
            plainSelectLimited =  sqlHelper.addLimit(plainSelect, selectStatement.getLimit(), selectStatement.getOffset());
        } else {
            plainSelectLimited = sqlHelper.addLimit(plainSelect, selectStatement.getLimit());
        }
        
        PreparedStatement ps = new PreparedStatement(plainSelectLimited.toString());
        ps.setParams(jdbcParams);
        return ps;
    }

    private PlainSelect getStandardSelectWithoutWhere(final SelectStatement selectStatement) {
        Table table = new Table(selectStatement.getOntology());
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
        if (columns == null || columns.isEmpty())
            return Collections.singletonList(new AllColumns());
        return this.generateSQLColumns(columns).stream().map(SelectExpressionItem::new).collect(Collectors.toList());
    }

    private Expression getWhereForVirtual(final List<WhereStatement> whereList, final VirtualDatasourceType dsType,
            final Map<String, Integer> tableColumnTypes, Sequence seq, Map<String, Object> jdbcParams, boolean withParams) {
        final int size = whereList.size();
        if (size == 1)
            return generateExpressionForVirtualWhere(whereList.get(0), dsType, tableColumnTypes, seq, jdbcParams, withParams);
        else {
            final List<Expression> values = whereList.stream()
                    .map(where -> this.generateExpressionForVirtualWhere(where, dsType, tableColumnTypes, seq, jdbcParams, withParams))
                    .collect(Collectors.toList());
            return this.generateWhereForValues(size, whereList, values);
        }
    }

    private Expression getWhereForWhereList(final List<WhereStatement> whereList, Sequence seq, Map<String, Object> jdbcParams, boolean withParams) {
        final int size = whereList.size();
        if (size == 1)
            return this.generateExpressionForWhere(whereList.get(0), seq, jdbcParams, withParams);
        else {
            final List<Expression> values = whereList.stream().map(w -> generateExpressionForWhere(w, seq, jdbcParams, withParams))
                    .collect(Collectors.toList());
            return this.generateWhereForValues(size, whereList, values);
        }
    }

    private Expression generateWhereForValues(final int size, final List<WhereStatement> whereList,
            final List<Expression> values) {
        Expression whereCondition = getConditionExpression(whereList.get(0), values.get(0), values.get(1));
        for (int i = 2; i < size; i++) {
            whereCondition = getConditionExpression(whereList.get(i - 1), whereCondition, values.get(i));
        }
        return whereCondition;
    }

    private BinaryExpression getConditionExpression(final WhereStatement where, final Expression leftExpression,
            final Expression rightExpression) {
        return where.getCondition() != null && where.getCondition().equalsIgnoreCase("OR")
                ? new OrExpression(leftExpression, rightExpression)
                : new AndExpression(leftExpression, rightExpression);
    }

    private Expression generateExpressionForWhere(final WhereStatement where, Sequence seq, Map<String, Object> jdbcParams, boolean withParams) {
        BinaryExpression expression;
        if (withParams) {
            final Object value = getValueForValue(where.getValue());
            String paramName = where.getColumn() + seq.next();
            Assert.isTrue(!jdbcParams.containsKey(paramName), "Duplicated parameter");
            jdbcParams.put(paramName, value);
            expression = this.getWhereBinaryExpression(where, new Column(where.getColumn()), new JdbcNamedParameter(paramName));
        } else {
            expression = this.getWhereBinaryExpression(where, new Column(where.getColumn()), getExpressionForValue(where.getValue()));
        }               
        return expression;
    }

    private Expression generateExpressionForVirtualWhere(final WhereStatement where,
            final VirtualDatasourceType virtualType, final Map<String, Integer> tableColumnTypes,
            Sequence seq, Map<String, Object> jdbcParams, boolean withParams) {     
        BinaryExpression expression;
        if (withParams) {
            final Object value = getValueForSQLType(tableColumnTypes.get(where.getColumn()), virtualType,
                    where.getValue());
            String paramName = where.getColumn() + seq.next();
            Assert.isTrue(!jdbcParams.containsKey(paramName), "Duplicated parameter");
            jdbcParams.put(paramName, value);
            expression = this.getWhereBinaryExpression(where, new Column(where.getColumn()), new JdbcNamedParameter(paramName));
        } else {
            expression = this.getWhereBinaryExpression(where, new Column(where.getColumn()), getExpressionForSQLType(tableColumnTypes.get(where.getColumn()), virtualType,
                    where.getValue()));
        }
        return expression;
    }

    private BinaryExpression getWhereBinaryExpression(final WhereStatement where, final Column column,
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

    private List<OrderByElement> getOrderByFromJsonQuery(final List<OrderByStatement> ordersBy) {
        final List<OrderByElement> orderByList = new ArrayList<>();
        if (ordersBy != null && !ordersBy.isEmpty()) {
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
    public PreparedStatement getStandardInsert(final InsertStatement insertStatement, boolean withParams) {
        
        Map<String, Object> jdbcParams = new HashMap<>();
        final Insert insert = this.generateInsertWithoutValues(insertStatement);
        
        insert.setItemsList(this.generateSQLMultiExpressionList(insertStatement.getValues(), new Sequence(), jdbcParams, withParams));
        
        PreparedStatement ps = new PreparedStatement(insert.toString());
        ps.setParams(jdbcParams);
        return ps;
    }

    @Override
    public PreparedStatement getStandardInsert(final InsertStatement insertStatement,
            final VirtualDatasourceType virtualDatasourceType, final Map<String, Integer> tableColumnTypes, boolean withParams) {
        
        Map<String, Object> jdbcParams = new HashMap<>();       
        final Insert insert = this.generateInsertWithoutValues(insertStatement);
        
        insert.setItemsList(this.generateSQLMultiExpressionList(insertStatement.getValues(), virtualDatasourceType,
                tableColumnTypes, new Sequence(),  jdbcParams, withParams));
        
        PreparedStatement ps = new PreparedStatement(insert.toString());
        ps.setParams(jdbcParams);
        return ps;
    }

    private Insert generateInsertWithoutValues(final InsertStatement insertStatement) {
        final Insert insert = new Insert();
        insert.setTable(new Table(insertStatement.getOntology()));
        insert.setColumns(this.generateSQLColumns(insertStatement.getColumns()));
        return insert;
    }

    private List<Column> generateSQLColumns(final List<String> columns) {
        return columns.stream().map(Column::new).collect(Collectors.toList());
    }

    @Override
    public List<ColumnRelational> generateColumnsRelational(final String ontologyJsonSchema) {
        List<ColumnRelational> cols = new ArrayList<>();

        try {
            JsonParser parser = new JsonParser();
            JsonElement jsonTree = parser.parse(ontologyJsonSchema);
            if (jsonTree.isJsonObject()) {
                JsonObject jsonObject = jsonTree.getAsJsonObject();
                extractAllFieldsFromJson(cols, jsonObject);
            } else {
                throw new OPResourceServiceException(
                        "Invalid schema to be converted to SQL schema: " + ontologyJsonSchema);
            }

        } catch (Exception e) {
            Log.warn("Not possible to convert schema to SQL schema: ", e.getMessage());
            throw new OPResourceServiceException("Not possible to convert schema to SQL schema: " + e.getMessage());
        }
        return cols;
    }

    @Override
    public List<ColumnRelational> generateSQLColumnsRelational(final String ontologyJsonSchema,
            final VirtualDatasourceType dsType) {
        List<ColumnRelational> colsSQL = new ArrayList<>();

        try {
            SQLHelper helper = getOntologyHelper(dsType);
            List<ColumnRelational> cols = generateColumnsRelational(ontologyJsonSchema);
            for (ColumnRelational col : cols) {
                colsSQL.add(helper.getColumnWithSpecs(col));
            }

        } catch (Exception e) {
            Log.warn("Not possible to convert schema to SQL columns with spec schema: {}", e.getMessage());
            throw new OPResourceServiceException(e.getMessage());
        }
        return colsSQL;
    }

    private void extractAllFieldsFromJson(List<ColumnRelational> cols, JsonObject datosJson) {
        if (datosJson.has("properties")) {
            JsonObject propertiesJson = datosJson.get("properties").getAsJsonObject();
            Set<Entry<String, JsonElement>> keyValues = propertiesJson.entrySet();
            for (Entry<String, JsonElement> entry : keyValues) {
                String fieldName = entry.getKey();
                JsonObject fieldSpec = entry.getValue().getAsJsonObject();

                boolean fieldIsRequired = fieldSpec.get("required").getAsBoolean();
                String fieldDescription = fieldSpec.get("id").getAsString();
                String fieldType = fieldSpec.get("type").getAsString();

                ColumnRelational col = new ColumnRelational();
                ColDataType colDT = new ColDataType();
                colDT.setDataType(fieldType);
                col.setColDataType(colDT);
                col.setColumnName(fieldName);
                col.setNotNull(fieldIsRequired);
                col.setColComment(fieldDescription);
                cols.add(col);

            }
        }
    }

    private ExpressionList generateSQLExpressionList(final Map<String, String> values, final Sequence seq, Map<String, Object> jdbcParams, boolean withParams) {
        final List<Expression> expressions = new ArrayList<>();
        for (Map.Entry<String, String> entry : values.entrySet()) {     
            if (withParams) {
                String paramName = entry.getKey() + seq.next();
                Assert.isTrue(!jdbcParams.containsKey(paramName), "Duplicated param");              
                jdbcParams.put(paramName, getValueForValue(values.get(entry.getKey())));
                expressions.add(new JdbcNamedParameter(paramName));
            } else {
                expressions.add(getExpressionForValue(values.get(entry.getKey())));
            }
        }
        return new ExpressionList(expressions);
    }

    private ExpressionList generateSQLExpressionListForVirtual(final Map<String, String> values,
            final VirtualDatasourceType virtualDatasourceType, final Map<String, Integer> tableColumnTypes,
            final Sequence seq, Map<String, Object> jdbcParams, boolean withParams) {
        List<Expression> expressions = new ArrayList<>();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String paramName = entry.getKey() + seq.next();
            Assert.isTrue(!jdbcParams.containsKey(paramName), "Duplicated param");
            if (withParams) {
                jdbcParams.put(paramName, getValueForSQLType(tableColumnTypes.get(entry.getKey()), virtualDatasourceType,
                        entry.getValue()));
                expressions.add(new JdbcNamedParameter(paramName));
            } else {
                expressions.add(getExpressionForSQLType(tableColumnTypes.get(entry.getKey()), virtualDatasourceType,
                        entry.getValue()));
            }           
        }
        return new ExpressionList(expressions);
    }

    private MultiExpressionList generateSQLMultiExpressionList(final List<Map<String, String>> valuesList, Sequence seq, Map<String, Object> jdbcParams, boolean withParams) {
        final MultiExpressionList multiExpressionList = new MultiExpressionList();
        for (int i = 0; i < valuesList.size(); i++) {
            multiExpressionList.addExpressionList(generateSQLExpressionList(valuesList.get(i), seq, jdbcParams, withParams));
        }
        return multiExpressionList;
    }

    private MultiExpressionList generateSQLMultiExpressionList(final List<Map<String, String>> valuesList,
            final VirtualDatasourceType virtualDatasourceType, final Map<String, Integer> tableColumnTypes, Sequence seq, Map<String, Object> jdbcParams, boolean withParams) {
        final MultiExpressionList multiExpressionList = new MultiExpressionList();
        for (int i = 0; i < valuesList.size(); i++) {
            multiExpressionList.addExpressionList(generateSQLExpressionListForVirtual(valuesList.get(i), virtualDatasourceType, tableColumnTypes, seq, jdbcParams, withParams));
        }
        return multiExpressionList;
    }

    @Override
    public PreparedStatement getOracleInsertSQL(final InsertStatement insertStatement,
            final Map<String, Integer> tableColumnTypes, boolean withParams) {
        if (insertStatement.getValues().size() > 1) {
            return this.getOracleMultiInsertSQL(insertStatement, tableColumnTypes, new Sequence(), withParams);
        } else {
            return this.getStandardInsert(insertStatement, VirtualDatasourceType.ORACLE, tableColumnTypes, withParams);
        }
    }

    private PreparedStatement getOracleMultiInsertSQL(final InsertStatement insertStatement, final Map<String, Integer> tableColumnTypes, Sequence seq, boolean withParams) {
        /*
         * Oracle multi insert example
         * 
         * INSERT ALL INTO suppliers (supplier_id, supplier_name) VALUES (1000, 'IBM')
         * INTO suppliers (supplier_id, supplier_name) VALUES (2000, 'Microsoft') INTO
         * suppliers (supplier_id, supplier_name) VALUES (3000, 'Google') SELECT * FROM
         * dual;
         */
        
        Map<String, Object> jdbcParams = new HashMap<>();       
        
        final StringBuilder sb = new StringBuilder();
        sb.append("INSERT ALL");
        final List<Column> columns = this.generateSQLColumns(insertStatement.getColumns());

        for (int i = 0; i < insertStatement.getValues().size(); i++) {
            Map<String, String> values = insertStatement.getValues().get(i);
            final StringBuilder subSB = new StringBuilder();
            subSB.append(" INTO ").append(insertStatement.getOntology() + " ")
                    .append(PlainSelect.getStringList(columns, true, true)).append(" VALUES (")
                    .append(PlainSelect.getStringList(this
                            .generateSQLExpressionListForVirtual(values, VirtualDatasourceType.ORACLE, tableColumnTypes, seq, jdbcParams, withParams)
                            .getExpressions(), true, false))
                    .append(")");

            sb.append(subSB.toString());
        }
        sb.append(" SELECT * FROM dual");
        
        PreparedStatement ps = new PreparedStatement(sb.toString());
        ps.setParams(jdbcParams);
        return ps;
    }

    @Override
    public PreparedStatement getStandardUpdate(final UpdateStatement updateStatement, boolean withParams) {
        
        Map<String, Object> jdbcParams = new HashMap<>();
        final Update update = new Update();
        
        update.setTables(Collections.singletonList(new Table(updateStatement.getOntology())));
        update.setColumns(this.generateSQLColumns(new ArrayList<>(updateStatement.getValues().keySet())));
        Sequence seq = new Sequence();
        update.setExpressions(this.generateSQLExpressionList(updateStatement.getValues(), seq, jdbcParams, withParams).getExpressions());
        update.setWhere(this.getWhereForWhereList(updateStatement.getWhere(), seq, jdbcParams, withParams));
        
        PreparedStatement ps = new PreparedStatement(update.toString());
        ps.setParams(jdbcParams);
        
        return ps;
    }

    @Override
    public PreparedStatement getStandardUpdate(final UpdateStatement updateStatement,
            final VirtualDatasourceType virtualDatasourceType, final Map<String, Integer> tableColumnTypes, boolean withParams) {       
        Map<String, Object> jdbcParams = new HashMap<>();
        final Update update = new Update();
        
        update.setTables(Collections.singletonList(new Table(updateStatement.getOntology())));
        update.setColumns(this.generateSQLColumns(new ArrayList<>(updateStatement.getValues().keySet())));
        Sequence seq = new Sequence();
        update.setExpressions(this.generateSQLExpressionListForVirtual(updateStatement.getValues(), virtualDatasourceType, tableColumnTypes, seq, jdbcParams, withParams).getExpressions());
        update.setWhere(this.getWhereForVirtual(updateStatement.getWhere(), virtualDatasourceType, tableColumnTypes, seq, jdbcParams, withParams));
        
        PreparedStatement ps = new PreparedStatement(update.toString());
        ps.setParams(jdbcParams);
        
        return ps;
    }

    @Override
    public PreparedStatement getStandardDelete(final DeleteStatement deleteStatement, boolean withParams) {
        
        Map<String, Object> jdbcParams = new HashMap<>();
        
        final Delete delete = new Delete();
        delete.setTable(new Table(deleteStatement.getOntology()));
        delete.setWhere(this.getWhereForWhereList(deleteStatement.getWhere(), new Sequence(), jdbcParams, withParams));
        
        PreparedStatement ps = new PreparedStatement(delete.toString());
        ps.setParams(jdbcParams);
        return ps;
    }

    @Override
    public PreparedStatement getStandardDelete(final DeleteStatement deleteStatement,
            final VirtualDatasourceType virtualDatasourceType, final Map<String, Integer> tableColumnTypes, boolean withParams) {       
        Map<String, Object> jdbcParams = new HashMap<>();
        final Delete delete = new Delete();
        delete.setTable(new Table(deleteStatement.getOntology()));
        PreparedStatement ps;
        delete.setWhere(this.getWhereForVirtual(deleteStatement.getWhere(), virtualDatasourceType, tableColumnTypes, new Sequence(), jdbcParams, withParams));
        ps = new PreparedStatement(delete.toString());
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
                    LongValue longExpression = new LongValue("false".equalsIgnoreCase(value.trim()) ? 0 : 1);
                    return raw ? longExpression.getValue() : longExpression;                    
                } else {
                    return raw ? new StringValue(value).getValue() : new StringValue(value);                    
                }
            }
        }
    }

    private Expression getExpressionForSQLType(final Integer sqlType, final VirtualDatasourceType relationalType,
            final String value) {
        return (Expression) getObjectForSQLType(sqlType, relationalType, value, false);
    }
    
    private Object getValueForSQLType(final Integer sqlType, final VirtualDatasourceType relationalType,
            final String value) {
        return getObjectForSQLType(sqlType, relationalType, value, true);
    }
    
    private Object getObjectForSQLType(final Integer sqlType, final VirtualDatasourceType relationalType,
            final String value, boolean raw) {
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
                    switch (relationalType) {
                    case SQLSERVER:
                        final StringValue newValue = new StringValue(value);
                        newValue.setPrefix("N");
                        return raw ? newValue.getValue() : newValue;
                    default:
                        return raw ? new StringValue(value).getValue() : new StringValue(value);
                    }
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
                    switch (relationalType) {
                    case POSTGRESQL:
                        final Function function = new Function();
                        function.setName("XMLPARSE");
                        function.setParameters(new ExpressionList(new StringValue(value)));
                        return raw ? function.toString() : function;
                    case SQLSERVER:
                        final StringValue newValue = new StringValue(value);
                        newValue.setPrefix("N");
                        return raw ? newValue.getValue() : newValue;
                    default:
                        return raw ? new StringValue(value).getValue() : new StringValue(value);
                    }
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

    @Override
    public PreparedStatement getStandardCreate(CreateStatement createStatement) {
        createStatement = sqlHelper.parseCreateStatementColumns(createStatement);
        createStatement = sqlHelper.parseCreateStatementConstraints(createStatement);
        return new PreparedStatement(createStatement.toString());
    }

    @Override
    public PreparedStatement getStandardCreate(CreateStatement createStatement, VirtualDatasourceType virtualDatasourceType) {
        SQLHelper helper = getOntologyHelper(virtualDatasourceType);
        createStatement = helper.parseCreateStatementColumns(createStatement);
        createStatement = helper.parseCreateStatementConstraints(createStatement);
        return new PreparedStatement(createStatement.toString());
    }

    @Override
    public PreparedStatement getStandardDrop(DropStatement dropStatement) {
        return  new PreparedStatement(dropStatement.toString());
    }

    @Override
    public PreparedStatement getStandardDrop(DropStatement dropStatement, VirtualDatasourceType virtualDatasourceType) {
        // Implements different logic if needed getting sql helper
        return new PreparedStatement(dropStatement.toString());
    }

    @Override
    public PreparedStatement getOracleDrop(DropStatement dropStatement, VirtualDatasourceType virtualDatasourceType) {

        if (dropStatement.isIfExists()) {
            // TODO: implements oracle custom
            throw new NotImplementedException("Drop table if exists over oracle db is not implemented");
        } else {
            return new PreparedStatement(dropStatement.toString());
        }

    }

}
