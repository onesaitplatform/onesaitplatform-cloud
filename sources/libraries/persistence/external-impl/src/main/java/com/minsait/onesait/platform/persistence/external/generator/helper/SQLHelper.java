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

import com.minsait.onesait.platform.persistence.external.generator.model.common.ColumnRelational;
import com.minsait.onesait.platform.persistence.external.generator.model.common.Constraint;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.CreateStatement;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.select.PlainSelect;

public interface SQLHelper {

	String getValidateQuery();

	String getAllTablesStatement();
	
	boolean hasDatabase();
	
	boolean hasCrossDatabase();
		
	boolean hasSchema();
	
	String getDatabaseStatement();
	
	String getSchemaStatement();

	String getDatabasesStatement();
	
	String getSchemasStatement(String database);
	
	String getAllTablesStatement(String database, String schema);

	PlainSelect addLimit(final PlainSelect query, final long limit);

	PlainSelect addLimit(final PlainSelect query, final long limit, final long offset);

	String addLimit(final String query, final long limit);

	String addLimit(final String query, final long limit, final long offset);

	ColumnRelational getColumnWithSpecs(ColumnRelational col);

	String getFieldTypeString(String fieldOspType);

	Constraint getContraintWithSpecs(Constraint constaint);

	CreateStatement parseCreateStatementConstraints(CreateStatement statement);

	CreateStatement parseCreateStatementColumns(CreateStatement statement);

	CreateStatement getCreateStatementWithConstraints(CreateStatement createStatement);

	String parseGeometryFields(String query, String ontology) throws JSQLParserException;
}