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
import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.config.components.OntologyVirtualSchemaFieldType;
import com.minsait.onesait.platform.config.services.exceptions.OPResourceServiceException;
import com.minsait.onesait.platform.persistence.external.generator.model.common.ColumnRelational;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Primary
@Component("PostgreSQLHelper")
public class PostgreSQLHelper extends SQLHelperImpl implements SQLHelper {

	private static final String LIST_TABLES_QUERY = "SELECT table_name FROM information_schema.tables WHERE table_schema NOT IN ('pg_catalog', 'information_schema')";
	private static final String SERIAL_TYPE = "serial";

	@Override
	public String getAllTablesStatement() {
		return LIST_TABLES_QUERY;
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
		case GEOMERTY:
			type = "GEOMETRY";
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
		List<String> colSpecs = col.getColumnSpecStrings();
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

			col.setColumnSpecStrings(colSpecs);
			col.getColDataType().setDataType(getFieldTypeString(col.getColDataType().getDataType()));
		}
		return col;
	}

}
