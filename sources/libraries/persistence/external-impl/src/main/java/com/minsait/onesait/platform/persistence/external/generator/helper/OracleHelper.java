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

import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.config.components.OntologyVirtualSchemaFieldType;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.OntologyVirtual;
import com.minsait.onesait.platform.config.repository.OntologyVirtualRepository;
import com.minsait.onesait.platform.config.services.exceptions.OPResourceServiceException;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.Fetch;
import net.sf.jsqlparser.statement.select.Offset;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;

@Component("OracleHelper")
@Slf4j
public class OracleHelper extends SQLHelperImpl implements SQLHelper {

	private static final String LIST_VALIDATE_QUERY = "SELECT 1 FROM DUAL";
	// private static final String LIST_TABLES_QUERY = "SELECT table_name FROM
	// (SELECT view_name AS table_name FROM user_views UNION SELECT table_name AS
	// table_name FROM user_tables) ORDER BY table_name asc";
	private static final String LIST_TABLES_QUERY = "SELECT table_name FROM ("
			+ "SELECT view_name AS table_name FROM user_views UNION "
			+ "SELECT table_name AS table_name FROM user_tables UNION "
			+ "SELECT table_name AS table_name FROM user_tab_privs WHERE grantee IN (SELECT user from dual)"
			+ ") ORDER BY table_name asc";
	private static final String GET_CURRENT_SCHEMA_QUERY = "SELECT SYS_CONTEXT('USERENV','CURRENT_SCHEMA') FROM DUAL";
	private static final String LIST_SCHEMAS_QUERY = "SELECT USERNAME FROM ALL_USERS";
	private static final String LIST_TABLES_IN_SCHEMA_QUERY = "SELECT OBJECT_NAME FROM ALL_OBJECTS WHERE OWNER = '%s' AND OBJECT_TYPE in ('TABLE', 'VIEW') ORDER BY OBJECT_NAME";

	@Autowired
	private OntologyVirtualRepository ontologyVirtualRepository;

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
		return false;
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
		// TODO Auto-generated method stub
		return GET_CURRENT_SCHEMA_QUERY;
	}

	@Override
	public String getDatabasesStatement() {
		return null;
	}

	@Override
	public String getSchemasStatement(String database) {
		// TODO Auto-generated method stub
		return LIST_SCHEMAS_QUERY;
	}

	@Override
	public String getAllTablesStatement(String database, String schema) {
		return String.format(LIST_TABLES_IN_SCHEMA_QUERY, schema);
	}

	@Override
	public PlainSelect addLimit(final PlainSelect select, final long limit) {
		final boolean hasFetch = select.getFetch() != null;
		if (hasFetch) {
			final long oldFetch = select.getFetch().getRowCount();
			select.getFetch().setRowCount(Math.min(oldFetch, limit));
		} else {
			final Fetch qFetch = new Fetch();
			qFetch.setFetchParam("ROWS");
			qFetch.setRowCount(Math.max(limit, 1));
			select.setFetch(qFetch);
		}
		return select;
	}

	@Override
	public PlainSelect addLimit(final PlainSelect select, final long limit, final long offset) {
		final PlainSelect limitedSelect = addLimit(select, limit);
		if (offset > 0) {
			final boolean hasOffset = limitedSelect.getOffset() != null;
			if (hasOffset) {
				if (limitedSelect.getOffset() != null) {
					limitedSelect.getOffset().setOffset(new LongValue(offset));
				} else {
					limitedSelect.getLimit().setOffset(new LongValue(offset));
				}
			} else {
				final Offset qOffset = new Offset();
				qOffset.setOffsetParam("ROWS");
				qOffset.setOffset(new LongValue(Math.max(offset, 0)));
				limitedSelect.setOffset(qOffset);
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
			type = "CHAR(255)";
			break;
		case OBJECT:
			type = "CHAR(255)";
			break;
		case NUMBER:
			type = "FLOAT";
			break;
		case INTEGER:
			type = "INTEGER";
			break;
		case GEOMERTY:
			type = "VARCHAR2";
			break;
		case FILE:
			type = "LONG RAW";
			break;
		case TIMESTAMP_MONGO:
		case TIMESTAMP:
			type = "TIMESTAMP";
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

	@Override
	public String parseGeometryFields(String query, String ontology) throws JSQLParserException {
		final Ontology o = ontologyVirtualRepository.findOntology(ontology);
		final OntologyVirtual virtual = ontologyVirtualRepository.findByOntologyId(o);
		final String jsonSchema = o.getJsonSchema();
		final JSONObject obj = new JSONObject(jsonSchema);
		final JSONObject columns = obj.getJSONObject("properties");
		if (query.contains("_id,")) {
			query = query.replace("_id,", "");
		}

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
