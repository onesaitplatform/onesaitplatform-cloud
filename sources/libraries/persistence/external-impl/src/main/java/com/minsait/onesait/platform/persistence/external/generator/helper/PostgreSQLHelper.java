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
package com.minsait.onesait.platform.persistence.external.generator.helper;

import java.util.ArrayList;
import java.util.List;

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

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;

@Slf4j
@Primary
@Component("PostgreSQLHelper")
public class PostgreSQLHelper extends SQLHelperImpl implements SQLHelper {

	private static final String LIST_TABLES_QUERY = "SELECT table_name FROM information_schema.tables WHERE table_schema NOT IN ('pg_catalog', 'information_schema')";
	private static final String GET_CURRENT_DATABASE_QUERY = "SELECT current_database();";
	private static final String LIST_TABLE_INFORMATION_QUERY = "SELECT table_name, column_name FROM information_schema.columns WHERE table_schema = '%s'";
	private static final String GET_TABLE_INFORMATION_QUERY = "SELECT c.relname AS table_name, a.attname AS column_name FROM pg_constraint AS pk JOIN  pg_class AS c ON pk.conrelid = c.oid JOIN  pg_attribute AS a ON a.attnum = ANY(pk.conkey) AND a.attrelid = c.oid WHERE  pk.contype = 'p'  AND c.relnamespace = (SELECT oid FROM pg_namespace WHERE nspname = '%s')";
	private static final String LIST_DATABASES_QUERY = "SELECT datname FROM pg_database";
	private static final String GET_CURRENT_SCHEMA_QUERY = "SELECT current_schema();";
	private static final String LIST_SCHEMAS_QUERY = "SELECT schema_name FROM information_schema.schemata where schema_name not like 'pg_%'";
	private static final String LIST_TABLES_IN_SCHEMA_QUERY = "SELECT table_name FROM information_schema.tables WHERE table_schema = '%s'";
	private static final String SERIAL_TYPE = "serial";

	@Autowired
	private OntologyVirtualRepository ontologyVirtualRepository;

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
	public boolean hasCrossDatabase() {
		return false;
	}

	@Override
	public String getTableInformationStatement(String database, String schema) {
		return String.format(LIST_TABLE_INFORMATION_QUERY, schema);
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
		return LIST_SCHEMAS_QUERY;
	}

	@Override
	public String getAllTablesStatement(String database, String schema) {
		return String.format(LIST_TABLES_IN_SCHEMA_QUERY, schema);
	}

	@Override
	public String getTableIndexes(String database, String schema) {
		return String.format(GET_TABLE_INFORMATION_QUERY, schema);
	}

	@Override
	public String getFieldTypeString(final String fieldOspType) {
		String type = null;
		// custom Postgres field types
		if (fieldOspType.equals(SERIAL_TYPE)) {
			type = "SERIAL";
			return type;
		}

		final OntologyVirtualSchemaFieldType fieldtype = OntologyVirtualSchemaFieldType.valueOff(fieldOspType);
		switch (fieldtype) {
		case STRING:
			type = "VARCHAR(255)";
			break;
		case OBJECT:
			type = "JSON";
			break;
		case NUMBER:
			type = "FLOAT";
			break;
		case INTEGER:
			type = "INTEGER";
			break;
		case FILE:
			type = "BYTEA";
			break;
		case DATE:
			type = "DATE";
			break;
		case TIMESTAMP_MONGO:
		case TIMESTAMP:
			type = "TIMESTAMP WITHOUT TIME ZONE";
			break;
		case ARRAY:
		case GEOMERTY:
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

	@Override
	public ColumnRelational getColumnWithSpecs(final ColumnRelational col) {
		List<String> colSpecs = col.getColumnSpecs();
		if (colSpecs == null) {
			colSpecs = new ArrayList<>();
		}

		if (col.isAutoIncrement()) {
			col.getColDataType().setDataType(getFieldTypeString("serial"));
		} else {
			if (col.isNotNull()) {
				colSpecs.add("NOT NULL");
			}

			if (col.getColDefautlValue() != null && !col.getColDefautlValue().equals("")) {
				if (col.getColDefautlValue() instanceof String) {
					String defValue = (String) col.getColDefautlValue();
					final OntologyVirtualSchemaFieldType fieldtype = OntologyVirtualSchemaFieldType
							.valueOff(col.getStringColDataType());
					if (fieldtype.equals(OntologyVirtualSchemaFieldType.STRING) && !defValue.startsWith("'")) {
						defValue = "'" + defValue + "'";
					}
					colSpecs.add("DEFAULT " + defValue);
				} else {
					colSpecs.add("DEFAULT " + col.getColDefautlValue());
				}
			}

			col.setColumnSpecs(colSpecs);
			col.getColDataType().setDataType(getFieldTypeString(col.getColDataType().getDataType()));
		}
		return col;
	}

	@Override
	public String parseGeometryFields(String query, String ontology) throws JSQLParserException {
		final Ontology o = ontologyVirtualRepository.findOntology(ontology);
		final OntologyVirtual virtual = ontologyVirtualRepository.findByOntologyId(o);
		final String jsonSchema = o.getJsonSchema();
		final JSONObject obj = new JSONObject(jsonSchema);
		final JSONObject columns = obj.getJSONObject("properties");
		// Comentado porque no se pueden pedir las columnas fk: user_id, api_id...
//		if (query.contains("_id,")) {
//			query = query.replace("_id,", "");
//		}

		if (virtual.getObjectGeometry() != null && !virtual.getObjectGeometry().trim().equals("")) {
			final Select selectStatement = (Select) CCJSqlParserUtil.parse(query);
			final PlainSelect plainSelect = (PlainSelect) selectStatement.getSelectBody();
			final Alias alias = plainSelect.getFromItem().getAlias();
			final List<SelectItem> selectItems = plainSelect.getSelectItems();
			for (final SelectItem item : selectItems) {
				if (item.toString().equals("*") || (alias != null && item.toString().equals(alias.getName()))) {
					return refactorQueryAll(columns, virtual, selectStatement, alias, item.toString());
				} else {
					query = refactorQuery(virtual, query, alias, item);
				}
			}

		}

		return query;
	}

}
