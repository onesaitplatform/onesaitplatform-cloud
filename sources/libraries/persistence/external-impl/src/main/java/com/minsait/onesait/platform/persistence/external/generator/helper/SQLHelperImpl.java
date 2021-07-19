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
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.config.components.OntologyVirtualSchemaFieldType;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.OntologyVirtual;
import com.minsait.onesait.platform.config.repository.OntologyVirtualRepository;
import com.minsait.onesait.platform.config.services.exceptions.OPResourceServiceException;
import com.minsait.onesait.platform.persistence.external.generator.model.common.ColumnRelational;
import com.minsait.onesait.platform.persistence.external.generator.model.common.Constraint;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.CreateStatement;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.Offset;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.util.SelectUtils;

@Slf4j
@Primary
@Component("SQLHelperImpl")
public class SQLHelperImpl implements SQLHelper {

	private static final String LIST_TABLES_QUERY = "SHOW TABLES";
	protected static final String SPEC_GEOM_SRID = "4326";
	private static final String ST_AS_GEO_JSON = "ST_AsGeoJSON(";

	@Autowired
	private OntologyVirtualRepository ontologyVirtualRepository;

	@Override
	public String getAllTablesStatement() {
		return LIST_TABLES_QUERY;
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
					limitedSelect.getOffset().setOffset(offset);
				} else {
					limitedSelect.getLimit().setOffset(new LongValue(offset));
				}
			} else {
				final Offset qOffset = new Offset();
				qOffset.setOffset(offset);
				limitedSelect.setOffset(qOffset);
			}
		}
		return limitedSelect;
	}

	@Override
	public String addLimit(final String query, final long limit) {
		final Optional<PlainSelect> selectStatement = parseQuery(query);
		if (selectStatement.isPresent()) {
			return addLimit(selectStatement.get(), limit).toString();
		} else {
			return query;
		}
	}

	@Override
	public String addLimit(final String query, final long limit, final long offset) {
		final Optional<PlainSelect> selectStatement = parseQuery(query);
		if (selectStatement.isPresent()) {
			return addLimit(selectStatement.get(), limit, offset).toString();
		} else {
			return query;
		}
	}

	private Optional<PlainSelect> parseQuery(final String query) {
		try {
			final Statement statement = CCJSqlParserUtil.parse(query);
			if (statement instanceof Select) {
				return Optional.ofNullable((PlainSelect) ((Select) statement).getSelectBody());
			} else {
				log.debug("The statement passed as argument is not a select query, returning the original");
				return Optional.empty();
			}
		} catch (final JSQLParserException e) {
			log.debug("Could not parse the query with JSQL returning the original. ", e);
			return Optional.empty();
		}
	}

	@Override
	public String getFieldTypeString(String fieldOspType) {
		String type = null;

		OntologyVirtualSchemaFieldType fieldtype = OntologyVirtualSchemaFieldType.valueOff(fieldOspType);
		switch (fieldtype) {
		case STRING:
			type = "VARCHAR(255)";
			break;
		case OBJECT:
			type = "VARCHAR(255)";
			break;
		case NUMBER:
			type = "FLOAT";
			break;
		case INTEGER:
			type = "INT";
			break;
		case GEOMERTY:
			type = "POINT";
			break;
		case FILE:
			type = "BLOB";
			break;
		case DATE:
			type = "DATETIME";
			break;
		case TIMESTAMP_MONGO:
		case TIMESTAMP:
			type = "TIMESTAMP";
			break;
		case ARRAY:
			type = "TEXT";
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
	public CreateStatement parseCreateStatementConstraints(CreateStatement statement) {
		List<Constraint> constraintOptions = statement.getColumnConstraints();
		List<Index> parsedOptions = new ArrayList<>();
		for (Constraint constraint : constraintOptions) {

			if (constraint.getColumnsNames().isEmpty()) {
				throw new OPResourceServiceException("Invalid constraint specification: no columns");
			}

			List<String> cols = statement.getColumnDefinitions().stream().map(ColumnDefinition::getColumnName)
					.collect(Collectors.toList());
			for (String col : constraint.getColumnsNames()) {
				if (!cols.contains(col)) {
					throw new OPResourceServiceException("Invalid constraint column: " + col);
				}
			}
			parsedOptions.add(getContraintWithSpecs(constraint));

		}
		statement.setIndexes(parsedOptions);
		return statement;
	}

	@Override
	public CreateStatement parseCreateStatementColumns(CreateStatement statement) {
		List<ColumnRelational> columnsRelational = statement.getColumnsRelational();
		List<ColumnDefinition> parsedOptions = new ArrayList<>();
		for (ColumnRelational columnRelat : columnsRelational) {
			parsedOptions.add(getColumnWithSpecs(columnRelat));
		}
		statement.setColumnDefinitions(parsedOptions);
		return statement;
	}

	@Override
	public Constraint getContraintWithSpecs(final Constraint constraint) {
		if (constraint.getType().contains("PRIMARY")) {
			constraint.setType("PRIMARY KEY");
			constraint.setName((String)null);

		} else if (constraint.getType().contains("FOREIGN")
				&& (constraint.getReferencedTable() != null && constraint.getReferencedColumn() != null)) {

			constraint.setType("FOREIGN KEY");
			ArrayList<String> idxSpec = new ArrayList<>();
			idxSpec.add("REFERENCES");
			idxSpec.add(constraint.getReferencedTable() + "(" + constraint.getReferencedColumn() + ")");
			constraint.setIndexSpec(idxSpec);
			constraint.setName((String)null);
		}

		else if (constraint.getType().contains("UNIQUE")) {
			constraint.setType("UNIQUE");
			ArrayList<String> idxSpec = new ArrayList<>();
			constraint.setIndexSpec(idxSpec);
			constraint.setName((String)null);
		}

		return constraint;

	}

	@Override
	public ColumnRelational getColumnWithSpecs(final ColumnRelational col) {
		List<String> colSpecs = col.getColumnSpecStrings();
		if (colSpecs == null) {
			colSpecs = new ArrayList<>();
		}
		if (col.isNotNull()) {
			colSpecs.add("NOT NULL");
		}

		if (col.isAutoIncrement()) {
			colSpecs.add("AUTO_INCREMENT");
		} else if (col.getColDefautlValue() != null && !col.getColDefautlValue().equals("")) {
			if (col.getColDefautlValue() instanceof String) {
				String defValue = (String) col.getColDefautlValue();
				OntologyVirtualSchemaFieldType fieldtype = OntologyVirtualSchemaFieldType
						.valueOff(col.getStringColDataType());
				if (fieldtype.equals(OntologyVirtualSchemaFieldType.STRING) && !defValue.startsWith("'")) {
					defValue = "'" + defValue + "'";
				}
				colSpecs.add("DEFAULT " + defValue);
			} else {
				colSpecs.add("DEFAULT " + col.getColDefautlValue());
			}

		}

		if (col.getColComment() != null) {
			colSpecs.add("COMMENT '" + col.getColComment() + "'");
		}

		col.setColDataType(getFieldTypeString(col.getStringColDataType()));
		col.setColumnSpecStrings(colSpecs);
		return col;
	}

	@Override
	public CreateStatement getCreateStatementWithConstraints(CreateStatement createStatement) {
		createStatement = parseCreateStatementColumns(createStatement);
		createStatement = parseCreateStatementConstraints(createStatement);
		return createStatement;
	}

	@Override
	public String parseGeometryFields(String query, String ontology) throws JSQLParserException {
		Ontology o = ontologyVirtualRepository.findOntology(ontology);
		OntologyVirtual virtual = ontologyVirtualRepository.findByOntologyId(o);
		String jsonSchema = o.getJsonSchema();
		JSONObject obj = new JSONObject(jsonSchema);
		JSONObject columns = obj.getJSONObject("properties");
		if (query.contains("_id,")) {
			query = query.replace("_id,", "");
		}

		if (virtual.getObjectGeometry() != null && !virtual.getObjectGeometry().trim().equals("")) {
			Select selectStatement = (Select) CCJSqlParserUtil.parse(query);
			PlainSelect plainSelect = (PlainSelect) selectStatement.getSelectBody();
			Alias alias = plainSelect.getFromItem().getAlias();
			List<SelectItem> selectItems = plainSelect.getSelectItems();
			for (SelectItem item : selectItems) {
				if (item.toString().equals("*") || (alias != null && item.toString().equals(alias.getName()))) {
					return refactorQueryAll(columns, virtual, selectStatement, alias, item.toString());
				} else {
					query = refactorQuery(virtual, query, alias, item);
				}
			}

		}

		return query.replace("\"", "'");
	}

	protected String refactorQueryAll(JSONObject columns, OntologyVirtual virtual, Select selectStatement, Alias alias,
			String item) {
		Iterator<String> keys = columns.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			if (columns.get(key) instanceof JSONObject) {
				if (key.equals(virtual.getObjectGeometry()) && alias != null) {
					SelectUtils.addExpression(selectStatement, new Column(
							ST_AS_GEO_JSON + alias.getName() + "." + key + ") as " + virtual.getObjectGeometry()));
				} else if (key.equals(virtual.getObjectGeometry()) && alias == null) {
					SelectUtils.addExpression(selectStatement,
							new Column(ST_AS_GEO_JSON + key + ") as " + virtual.getObjectGeometry()));
				} else if (!key.equals(virtual.getObjectGeometry()) && alias != null) {
					SelectUtils.addExpression(selectStatement, new Column(alias.getName() + "." + key));
				} else if (!key.equals(virtual.getObjectGeometry()) && alias == null) {
					SelectUtils.addExpression(selectStatement, new Column(key));
				}
			}
		}
		return selectStatement.toString().replaceFirst(item.equals("*") ? "\\" + item + "," : item + ",", "")
				.replace("\"", "'");

	}

	protected String refactorQuery(OntologyVirtual virtual, String query, Alias alias, SelectItem item) {
		SelectExpressionItem i = (SelectExpressionItem) item;
		Alias itemAlias = i.getAlias();
		if (itemAlias == null
				&& ((alias != null && item.toString().equals(alias.getName() + "." + virtual.getObjectGeometry()))
						|| (alias == null && item.toString().equals(virtual.getObjectGeometry())))) {
			query = query
					.replace(item.toString(), ST_AS_GEO_JSON + item.toString() + ") as " + virtual.getObjectGeometry())
					.replace("\"", "'");
		} else if (itemAlias != null) {
			if (alias != null && item.toString().equalsIgnoreCase(
					alias.getName() + "." + virtual.getObjectGeometry() + " as " + itemAlias.getName())) {
				query = query.replace(item.toString(), ST_AS_GEO_JSON + alias.getName() + "."
						+ virtual.getObjectGeometry() + ") as " + itemAlias.getName()).replace("\"", "'");
			} else if (alias == null
					&& item.toString().equalsIgnoreCase(virtual.getObjectGeometry() + " as " + itemAlias.getName())) {
				query = query
						.replace(item.toString(),
								ST_AS_GEO_JSON + virtual.getObjectGeometry() + ") as " + itemAlias.getName())
						.replace("\"", "'");
			}
		}
		return query;
	}

}
