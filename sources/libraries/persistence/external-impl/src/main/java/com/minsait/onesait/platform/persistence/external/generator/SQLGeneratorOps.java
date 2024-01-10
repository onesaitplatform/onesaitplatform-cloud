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
package com.minsait.onesait.platform.persistence.external.generator;

import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource.VirtualDatasourceType;
import com.minsait.onesait.platform.persistence.external.generator.model.common.ColumnRelational;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.CreateIndexStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.CreateStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.DeleteStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.DropIndexStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.DropStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.GetIndexStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.InsertStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.PreparedStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.SelectStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.UpdateStatement;

import net.sf.jsqlparser.statement.create.table.CreateTable;

import java.util.List;
import java.util.Map;

public interface SQLGeneratorOps {

    PreparedStatement getStandardSelect(SelectStatement selectStatement, VirtualDatasourceType dsType,
								  Map<String, Integer> tableColumnTypes, boolean withParams);

    PreparedStatement getStandardSelect(SelectStatement selectStatement, boolean withParams);

	PreparedStatement getStandardInsert(InsertStatement insertStatement, boolean withParams);

	PreparedStatement getStandardInsert(InsertStatement insertStatement, VirtualDatasourceType virtualDatasourceType,
							 Map<String, Integer> tableColumnTypes, boolean withParams);

	PreparedStatement getOracleInsertSQL(InsertStatement insertStatement, Map<String, Integer> tableColumnTypes, boolean withParams);

	PreparedStatement getStandardUpdate(UpdateStatement updateStatement, boolean withParams);

	PreparedStatement getStandardUpdate(UpdateStatement updateStatement,
							 VirtualDatasourceType virtualDatasourceType,
							 Map<String, Integer> tableColumnTypes, boolean withParams);

	PreparedStatement getStandardDelete(DeleteStatement deleteStatement, boolean withParams);
	
	PreparedStatement getStandardDelete(DeleteStatement deleteStatement,
							 VirtualDatasourceType virtualDatasourceType,
							 Map<String, Integer> tableColumnTypes, boolean withParams);
	
	PreparedStatement getStandardDrop(DropStatement dropStatement);
	
	PreparedStatement getStandardDrop(DropStatement dropStatement,
			 VirtualDatasourceType virtualDatasourceType);
	
	PreparedStatement getIndexStatement(GetIndexStatement getIndexStatement);
	
	PreparedStatement createIndex(CreateIndexStatement createIndexStatement);

	PreparedStatement getOracleDrop(DropStatement dropStatement, VirtualDatasourceType virtualDatasourceType);

	PreparedStatement getStandardCreate(CreateStatement createStatement, VirtualDatasourceType virtualDatasourceType);

	PreparedStatement getStandardCreate(CreateStatement createStatement);

	List<ColumnRelational> generateSQLColumnsRelational(String ontologyJsonSchema, VirtualDatasourceType dsType);

	List<ColumnRelational> generateColumnsRelational(String ontologyJsonSchema);

	PreparedStatement dropIndex(DropIndexStatement dropIndexStatement);

}
