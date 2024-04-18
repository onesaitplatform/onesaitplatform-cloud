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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.config.components.OntologyVirtualSchemaFieldType;
import com.minsait.onesait.platform.config.services.exceptions.OPResourceServiceException;
import com.minsait.onesait.platform.persistence.presto.generator.helper.SelectSwapLimitOffset;
import com.minsait.onesait.platform.persistence.presto.generator.model.common.ColumnPresto;
import com.minsait.onesait.platform.persistence.presto.generator.model.common.HistoricalOptions;
import com.minsait.onesait.platform.persistence.presto.generator.model.statements.PrestoCreateStatement;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.Offset;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.SubSelect;

@Slf4j
@Primary
@Component("PrestoSQLHelperImpl")
public class PrestoSQLHelperImpl implements PrestoSQLHelper {

	private static final String LIST_VALIDATE_QUERY = "SELECT 1";
	private static final String LIST_TABLES_QUERY = "SHOW TABLES";
	private static final String LIST_CATALOGS_QUERY = "SHOW CATALOGS";
	private static final String LIST_TABLES_IN_CATALOG_AND_SCHEMA_QUERY = "SHOW TABLES IN %s.%s";
	private static final String LIST_SCHEMAS_IN_CATALOG_QUERY = "SHOW SCHEMAS IN %s";
	protected static final String SPEC_GEOM_SRID = "4326";
	private static final String NOT_NULL = "NOT NULL";
	private static final String COMMENT = "COMMENT";
	private static final String QUOTE = "'";

	@Override
	public String getValidateQuery() {
		return LIST_VALIDATE_QUERY;
	}

	@Override
	public String getAllTablesStatement() {
		return LIST_TABLES_QUERY;
	}

	@Override
	public boolean hasDatabase() {
		return true;
	}

	@Override
	public boolean hasCrossDatabase() {
		return true;
	}

	@Override
	public boolean hasSchema() {
		return true;
	}

	@Override
	public String getDatabaseStatement() {
		return null;
	}

	@Override
	public String getSchemaStatement() {
		return null;
	}

	@Override
	public String getDatabasesStatement() {
		return LIST_CATALOGS_QUERY;
	}

	@Override
	public String getSchemasStatement(String catalog) {
		return String.format(LIST_SCHEMAS_IN_CATALOG_QUERY, catalog);
	}

	@Override
	public String getAllTablesStatement(String catalog, String schema) {
		return String.format(LIST_TABLES_IN_CATALOG_AND_SCHEMA_QUERY, catalog, schema);
	}

	@Override
	public PlainSelect addLimit(final PlainSelect select, final long limit) {
		final boolean hasLimit = (select.getLimit() != null);
		if (hasLimit) {
			final long oldLimit = ((LongValue) select.getLimit().getRowCount()).getValue();
			select.getLimit().setRowCount(new LongValue(Math.min(limit, oldLimit)));
		} else {
			final Limit qLimit = new Limit();
			qLimit.setRowCount(new LongValue(Math.max(limit, 1)));
			select.setLimit(qLimit);
		}

		return select;
	}

	@Override
	public PlainSelect addLimit(final PlainSelect select, final long limit, final long offset) {
		final PlainSelect limitedSelect = addLimit(select, limit);

		if (offset > 0) {
			final boolean hasOffset = (limitedSelect.getOffset() != null
					|| (limitedSelect.getLimit() != null && limitedSelect.getLimit().getOffset() != null));
			if (hasOffset) {
				if (limitedSelect.getOffset() != null) {
					limitedSelect.getOffset().setOffset(new LongValue(offset));
				} else {
					limitedSelect.getLimit().setOffset(new LongValue(offset));
				}
			} else {
				final Offset qOffset = new Offset();
				qOffset.setOffset(new LongValue(offset));
				limitedSelect.setOffset(qOffset);
			}
		}
		return limitedSelect;
	}

	@Override
	public String addLimit(final String query, final long limit) {
		final Optional<PlainSelect> selectStatement = parseQuery(query, false);
		if (selectStatement.isPresent()) {
			return addLimit(selectStatement.get(), limit).toString();
		} else {
			return query;
		}
	}

	@Override
	public String addLimit(final String query, final long limit, final long offset) {
		final Optional<PlainSelect> selectStatement = parseQuery(query, true);
		if (selectStatement.isPresent()) {
			return addLimit(selectStatement.get(), limit, offset).toString();
		} else {
			return query;
		}
	}

	private Optional<PlainSelect> parseQuery(final String query, final boolean swapLimitOffset) {
		try {
			final Statement statement = CCJSqlParserUtil.parse(query);
			if (statement instanceof Select) {
				if (swapLimitOffset) {
					swapLimitOffsetForToString(statement);
				}
				return Optional.ofNullable(getPlainSelectFromSelect(((Select) statement).getSelectBody()));
			} else {
				log.debug("The statement passed as argument is not a select query, returning the original");
				return Optional.empty();
			}
		} catch (final JSQLParserException e) {
			log.debug("Could not parse the query with JSQL returning the original. ", e);
			return Optional.empty();
		}
	}
	
	private void swapLimitOffsetForToString(Statement statement) {
		StatementVisitorAdapter statementVisitor = new StatementVisitorAdapter() {
    	    public void visit(Select select) {
    	    	if (select.getSelectBody() instanceof PlainSelect) {
    	    		select.setSelectBody(new SelectSwapLimitOffset((PlainSelect) select.getSelectBody()));
    	    	}
    	    }
    	};
    	
    	statement.accept(statementVisitor);
	}
	
	private PlainSelect getPlainSelectFromSelect(SelectBody selectBody) {
		if (SetOperationList.class.isInstance(selectBody)) { // union
			PlainSelect plainSelect = new PlainSelect();

			List<SelectItem> ls = new LinkedList<>();
			ls.add(new AllColumns());
			plainSelect.setSelectItems(ls);
			
			SubSelect subSelect = new SubSelect();
			subSelect.setSelectBody(selectBody);
			subSelect.setAlias(new Alias("U"));
			plainSelect.setFromItem(subSelect);
						
			return plainSelect;
		} else {
			return (PlainSelect) selectBody;
		}
	}

	@Override
	public String getFieldTypeString(String fieldOspType) {
		String type = null;

		final OntologyVirtualSchemaFieldType fieldtype = OntologyVirtualSchemaFieldType.valueOff(fieldOspType);
		switch (fieldtype) {
		case STRING:
			type = "VARCHAR";
			break;
		case OBJECT:
			type = "VARCHAR";
			break;
		case NUMBER:
			type = "DOUBLE";
			break;
		case INTEGER:
			type = "INTEGER";
			break;
		case GEOMERTY:
			type = "VARCHAR";
			break;
		case FILE:
			type = "VARCHAR";
			break;
		case DATE:
			type = "DATE";
			break;
		case TIMESTAMP_MONGO:
		case TIMESTAMP:
			type = "TIMESTAMP";
			break;
		case ARRAY:
			type = "ARRAY(VARCHAR)";
			break;
		case BOOLEAN:
			type = "BOOLEAN";
			break;
		default:
			throw new OPResourceServiceException("OntologySchemaFieldType not suported: " + fieldtype.getValue());
		}

		return type;
	}

	@Override
	public PrestoCreateStatement parseCreateStatementColumns(PrestoCreateStatement statement) {
		final List<ColumnPresto> columnsPresto = statement.getColumnsPresto();
		final List<ColumnDefinition> parsedOptions = new ArrayList<>();
		for (final ColumnPresto columnPresto : columnsPresto) {
			parsedOptions.add(getColumnWithSpecs(columnPresto));
		}
		statement.setColumnDefinitions(parsedOptions);
		return statement;
	}

	@Override
	public ColumnPresto getColumnWithSpecs(final ColumnPresto col) {
		List<String> colSpecs = col.getColumnSpecs();
		if (colSpecs == null) {
			colSpecs = new ArrayList<>();
		}
		if (col.isNotNull()) {
			colSpecs.add(NOT_NULL);
		}
		if (col.getColComment() != null) {
			colSpecs.add(COMMENT + QUOTE + col.getColComment() + QUOTE);
		}
		col.setColDataType(getFieldTypeString(col.getStringColDataType()));
		col.setColumnSpecs(colSpecs);
		return col;
	}

	@Override
	public PrestoCreateStatement parseHistoricalOptionsStatement(PrestoCreateStatement statement) {
		final HistoricalOptions ho = statement.getHistoricalOptions();
		if (ho != null) {
			statement.setTableOptionsStrings(ho.buildHistoricalOptions());
		}
		return statement;
	}

}
