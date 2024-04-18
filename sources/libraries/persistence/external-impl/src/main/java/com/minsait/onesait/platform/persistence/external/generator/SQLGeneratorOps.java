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

import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource.VirtualDatasourceType;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.DeleteStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.InsertStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.SelectStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.UpdateStatement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.update.Update;

import java.util.Map;

public interface SQLGeneratorOps {

	PlainSelect getStandardSelect(SelectStatement selectStatement, VirtualDatasourceType dsType,
								  Map<String, Integer> tableColumnTypes);

	PlainSelect getStandardSelect(SelectStatement selectStatement);

	Insert getStandardInsert(InsertStatement insertStatement);

	Insert getStandardInsert(InsertStatement insertStatement, VirtualDatasourceType virtualDatasourceType,
							 Map<String, Integer> tableColumnTypes);

	String getOracleInsertSQL(InsertStatement insertStatement, Map<String, Integer> tableColumnTypes);

	Update getStandardUpdate(UpdateStatement updateStatement);

	Update getStandardUpdate(UpdateStatement updateStatement,
							 VirtualDatasourceType virtualDatasourceType,
							 Map<String, Integer> tableColumnTypes);

	Delete getStandardDelete(DeleteStatement deleteStatement);

	Delete getStandardDelete(DeleteStatement deleteStatement,
							 VirtualDatasourceType virtualDatasourceType,
							 Map<String, Integer> tableColumnTypes);
}
