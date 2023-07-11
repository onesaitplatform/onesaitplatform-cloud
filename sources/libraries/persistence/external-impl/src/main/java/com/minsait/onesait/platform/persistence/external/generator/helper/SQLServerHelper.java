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
package com.minsait.onesait.platform.persistence.external.generator.helper;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.config.components.OntologyVirtualSchemaFieldType;
import com.minsait.onesait.platform.config.services.exceptions.OPResourceServiceException;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.statement.select.Fetch;
import net.sf.jsqlparser.statement.select.Offset;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Top;

@Component("SQLServerHelper")
@Slf4j
public class SQLServerHelper extends SQLHelperImpl implements SQLHelper {

	private final static String LIST_TABLES_QUERY = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES";
	private static final String GET_CURRENT_DATABASE_QUERY = "SELECT DB_NAME()";
	private static final String LIST_DATABASES_QUERY = "Select name from sysdatabases";
	private static final String GET_CURRENT_SCHEMA_QUERY = "SELECT SCHEMA_NAME()";
	private static final String LIST_SCHEMAS_QUERY = ""
			+ "DECLARE @SQL VARCHAR(MAX) = 'use %s;SELECT schema_name FROM information_schema.schemata;'"
			+ "EXEC(@SQL);";
	private static final String LIST_TABLES_IN_DATABASE_IN_SCHEMA_QUERY = ""
			+ "DECLARE @SQL VARCHAR(MAX) = 'use %s;SELECT table_name FROM information_schema.tables WHERE table_schema = ''%s''';"
			+ "EXEC(@SQL);";

	@Override
	public String getAllTablesStatement() {
		return LIST_TABLES_QUERY;
	}

	@Override
	public boolean hasDatabase() {
		return true;
	}

	@Override
	public boolean hasSchema() {
		return true;
	}

	@Override
	public String getDatabaseStatement() {
		return GET_CURRENT_DATABASE_QUERY;
	}

	@Override
	public String getSchemaStatement() {
		return GET_CURRENT_SCHEMA_QUERY;
	}

	@Override
	public String getDatabasesStatement() {
		return LIST_DATABASES_QUERY;
	}

	@Override
	public String getSchemasStatement(String database) {
		return String.format(LIST_SCHEMAS_QUERY, database);
	}

	@Override
	public String getAllTablesStatement(String database, String schema) {
		return String.format(LIST_TABLES_IN_DATABASE_IN_SCHEMA_QUERY, database, schema);
	}

	@Override
	public PlainSelect addLimit(final PlainSelect select, final long limit) {
		final boolean hasTop = (select.getTop() != null);
		final boolean hasFetch = (select.getFetch() != null);
		if (hasFetch) {
			final long oldFetch = select.getFetch().getRowCount();
			final long newFetch = Math.min(oldFetch, limit);
			select.getFetch().setRowCount(newFetch);
		} else if (hasTop) {
			final long oldTop = ((LongValue) select.getTop().getExpression()).getValue();
			final long newTop = Math.min(oldTop, limit);
			select.getTop().setExpression(new LongValue(newTop));
		} else {
			final Top top = new Top();
			top.setExpression(new LongValue(limit));
			select.setTop(top);
		}
		return select;
	}

	@Override
	public PlainSelect addLimit(final PlainSelect select, final long limit, final long offset) {
		final PlainSelect limitedSelect = addLimit(select, limit);
		if (offset > 0) {
			final boolean hasOrderBy = (limitedSelect.getOrderByElements() != null);
			final boolean hasTop = (limitedSelect.getTop() != null);
			final boolean hasOffset = (limitedSelect.getOffset() != null);

			// Order by mandatory to do offset
			if (!hasOrderBy) {
				final OrderByElement orderByElement = new OrderByElement();
				orderByElement.setExpression(new LongValue(1));
				final List<OrderByElement> orderByList = new LinkedList<>();
				orderByList.add(orderByElement);
				limitedSelect.setOrderByElements(orderByList);
			}

			// Change to fetch to use offset
			if (hasTop) {
				final long finalLimit = ((LongValue) select.getTop().getExpression()).getValue();
				limitedSelect.setTop(null);

				final Fetch fetch = new Fetch();
				fetch.setRowCount(finalLimit);
				fetch.setFetchParam("ROWS");
				limitedSelect.setFetch(fetch);
			}

			// Set new offset
			if (hasOffset) {
				limitedSelect.getOffset().setOffset(new LongValue(offset));
			} else {
				final Offset newOffset = new Offset();
				newOffset.setOffset(new LongValue(offset));
				newOffset.setOffsetParam("ROWS");
				limitedSelect.setOffset(newOffset);
			}
		}
		return limitedSelect;
	}

	@Override
	public String getFieldTypeString(String fieldOspType) {
		String type = null;

		final OntologyVirtualSchemaFieldType fieldtype = OntologyVirtualSchemaFieldType.valueOff(fieldOspType);
		switch (fieldtype) {
		case STRING:
			type = "VARCHAR(255)";
			break;
		case OBJECT:
			type = "TEXT";
			break;
		case NUMBER:
			type = "FLOAT";
			break;
		case INTEGER:
			type = "INT";
			break;
		case GEOMERTY:
			type = "TEXT";
			break;
		case FILE:
			type = "VARBINARY";
			break;
		case TIMESTAMP_MONGO:
		case TIMESTAMP:
			type = "DATETIME";
			break;
		case ARRAY:
			type = "TEXT";
			break;
		case BOOLEAN:
			type = "BIT";
			break;
		default:
			throw new OPResourceServiceException("OntologySchemaFieldType not suported: " + fieldtype.getValue());
		}

		return type;
	}

}