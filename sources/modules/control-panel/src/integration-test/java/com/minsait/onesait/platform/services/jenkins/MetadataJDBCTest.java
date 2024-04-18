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
package com.minsait.onesait.platform.services.jenkins;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;
import com.minsait.onesait.platform.config.repository.OntologyVirtualRepository;
import com.minsait.onesait.platform.controlpanel.ControlPanelWebApplication;
import com.minsait.onesait.platform.persistence.external.virtual.VirtualDataSourceDescriptor;
import com.minsait.onesait.platform.persistence.external.virtual.VirtualDatasourcesManager;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ControlPanelWebApplication.class)
@Ignore
public class MetadataJDBCTest {

	@Autowired
	private OntologyVirtualRepository ontologyVirtualRepository;

	@Autowired
	@Qualifier("VirtualDatasourcesManagerImpl")
	private VirtualDatasourcesManager virtualDatasourcesManager;

	@Test
	public void testMetadata() throws SQLException {
		final OntologyVirtualDatasource ontologyVirtualDatasource = virtualDatasourcesManager
				.getDatasourceForOntology("user_token");
		final String dataSourceName = ontologyVirtualDatasource.getIdentification();
		final VirtualDataSourceDescriptor dataSource = virtualDatasourcesManager
				.getDataSourceDescriptor(dataSourceName);
		final DatabaseMetaData databaseMetaData = dataSource.getDatasource().getConnection().getMetaData();
		try (ResultSet columns = databaseMetaData.getColumns(null, null, "USER_TOKEN", null)) {
			while (columns.next()) {
				final String columnName = columns.getString("COLUMN_NAME");
				final String columnSize = columns.getString("COLUMN_SIZE");
				final String datatype = columns.getString("DATA_TYPE");
				final String isNullable = columns.getString("IS_NULLABLE");
				final String isAutoIncrement = columns.getString("IS_AUTOINCREMENT");
			}
		}
		try (ResultSet foreignKeys = databaseMetaData.getImportedKeys(null, null, "USER_TOKEN")) {
			while (foreignKeys.next()) {
				final String pkTableName = foreignKeys.getString("PKTABLE_NAME");
				final String fkTableName = foreignKeys.getString("FKTABLE_NAME");
				final String pkColumnName = foreignKeys.getString("PKCOLUMN_NAME");
				final String fkColumnName = foreignKeys.getString("FKCOLUMN_NAME");
			}
		}

	}

}
