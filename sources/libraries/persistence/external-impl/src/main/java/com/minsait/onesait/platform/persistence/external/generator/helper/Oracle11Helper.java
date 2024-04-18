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
package com.minsait.onesait.platform.persistence.external.generator.helper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.config.components.OntologyVirtualSchemaFieldType;
import com.minsait.onesait.platform.config.services.exceptions.OPResourceServiceException;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.schema.Column;

@Component("Oracle11Helper")
@Slf4j
public class Oracle11Helper extends SQLHelperImpl implements SQLHelper {

	// private static final String LIST_TABLES_QUERY = "SELECT table_name FROM
	// (SELECT view_name AS table_name FROM user_views UNION SELECT table_name AS
	// table_name FROM user_tables) ORDER BY table_name asc";
	private static final String LIST_TABLES_QUERY = "SELECT table_name FROM ("
			+ "SELECT view_name AS table_name FROM user_views UNION "
			+ "SELECT table_name AS table_name FROM user_tables UNION "
			+ "SELECT table_name AS table_name FROM user_tab_privs WHERE grantee IN (SELECT user from dual)"
			+ ") ORDER BY table_name asc";
	private static final String ROWNUM_STR = "ROWNUM";
	private static final String ROWNUM_ALIAS_STR = "ROWNUMALIAS";
	private static final String ALIAS_SUBQUERY = "t";

	@Override
	public String getAllTablesStatement() {
		return LIST_TABLES_QUERY;
	}

	@Override
	public PlainSelect addLimit(final PlainSelect plainSelect, final long limit) {
		// Limit SELECT
		final PlainSelect limitSelect = new PlainSelect();
		limitSelect.addSelectItems(new AllTableColumns(new Table(ALIAS_SUBQUERY)));
		// Limit WHERE
		final MinorThanEquals rowNumLimit = new MinorThanEquals();
		rowNumLimit.setLeftExpression(new Column(ROWNUM_STR));
		rowNumLimit.setRightExpression(new LongValue(limit));
		limitSelect.setWhere(rowNumLimit);
		// Adds the original query to the limit query using sub select
		final SubSelect limitSubSelect = new SubSelect();
		limitSubSelect.setSelectBody(plainSelect);
		limitSubSelect.setAlias(new Alias(ALIAS_SUBQUERY,false));
		limitSelect.setFromItem(limitSubSelect);

		return limitSelect;
	}

	//When selectItems are * or t.* or some function or expresion without alias return is *, in other cases return is list of columns and aliases
	private List<SelectItem> getAliasColumnOrAll(List<SelectItem> selectItems) {
		List<SelectItem> sItems = new LinkedList<SelectItem>();
		for(SelectItem sItem: selectItems) {
			if (sItem instanceof AllColumns || sItem instanceof AllTableColumns) {
				sItems = new LinkedList<SelectItem>();
				sItems.add(new AllColumns());
				return sItems;
			} else if (sItem instanceof SelectExpressionItem) {
				SelectExpressionItem seItem = (SelectExpressionItem)sItem;
				if(seItem.getAlias() != null && seItem.getAlias().getName() != null) {
					sItems.add(new SelectExpressionItem(new Column(seItem.getAlias().getName())));
				}
				else if(seItem.getExpression() instanceof Column){
					sItems.add(seItem);
				}
				else {
					sItems = new LinkedList<SelectItem>();
					sItems.add(new AllColumns());
					return sItems;
				}
			}
		}
		return sItems;
	}


	//In this method we add the ROWNUMALIAS column for offset in some cases (* and not column field with not alias) this columns is returned
	public PlainSelect addLimit(final PlainSelect select, final long limit, final long offset) {
		if (offset <= 0) {
			return addLimit(select, limit);
		}

		final List<SelectItem> selectItems = select.getSelectItems();

		// Gets the limited query an adds a column rownum with alias
		final PlainSelect selectWithLimit = addLimit(select, limit+offset);

		final List<SelectItem> limitSelectItems = new ArrayList<>(selectItems);
		final SelectExpressionItem rowNumColumn = new SelectExpressionItem(new Column(ROWNUM_STR));
		rowNumColumn.setAlias(new Alias(ROWNUM_ALIAS_STR));
		limitSelectItems.add(rowNumColumn);
		selectWithLimit.addSelectItems(rowNumColumn);

		final PlainSelect offsetSelect = new PlainSelect();

		final GreaterThan rowNumOffset = new GreaterThan();
		rowNumOffset.setLeftExpression(new Column(ROWNUM_ALIAS_STR));
		rowNumOffset.setRightExpression(new LongValue(offset));
		offsetSelect.setWhere(rowNumOffset);
		offsetSelect.setSelectItems(getAliasColumnOrAll(selectItems));

		// Adds the limited query to the query with offset using sub select
		final SubSelect offsetSubSelect = new SubSelect();
		offsetSubSelect.setSelectBody(selectWithLimit);
		offsetSubSelect.setAlias(new Alias(ALIAS_SUBQUERY,false));

		offsetSelect.setFromItem(offsetSubSelect);
		return offsetSelect;
	}

	@Override
	public String getFieldTypeString(String fieldOspType) {
		String type = null;

		OntologyVirtualSchemaFieldType fieldtype = OntologyVirtualSchemaFieldType.valueOff(fieldOspType);
		switch (fieldtype) {
		case STRING:
			type = "CHAR(255)";
			break;
		case OBJECT:
			type = "CHAR(255)";
			break;
		case NUMBER:
			type = "NUMBER";
			break;
		case INTEGER:
			type = "NUMBER";
			break;
		case GEOMERTY:
			type = "VARCHAR2";
			break;
		case FILE:
			type = "BFILE";
			break;
		case TIMESTAMP_MONGO:
		case TIMESTAMP:
			type = "DATE";
			break;
		case ARRAY:
			type = "VARCHAR2";
			break;
		case BOOLEAN:
			type = "NUMBER(1)";
			break;
		default:
			throw new OPResourceServiceException("OntologySchemaFieldType not suported: " + fieldtype.getValue());
		}

		return type;
	}

}
