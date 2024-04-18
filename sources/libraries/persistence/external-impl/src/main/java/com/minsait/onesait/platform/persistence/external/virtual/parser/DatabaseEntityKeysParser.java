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
package com.minsait.onesait.platform.persistence.external.virtual.parser;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource.VirtualDatasourceType;
import com.minsait.onesait.platform.persistence.external.virtual.constraints.ForeignKey;
import com.minsait.onesait.platform.persistence.external.virtual.constraints.PrimaryKey;
import com.minsait.onesait.platform.persistence.external.virtual.constraints.TableKeysHolder;
import com.minsait.onesait.platform.persistence.external.virtual.constraints.TableKeysHolder.SupportedDatabase;
import com.minsait.onesait.platform.persistence.external.virtual.constraints.UniqueKey;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DatabaseEntityKeysParser {

	private final DatabaseMetaData dm;
	private final String tableName;
	private SupportedDatabase databaseType;

	public DatabaseEntityKeysParser(DatabaseMetaData dm, String tableName, VirtualDatasourceType type) {
		this.dm = dm;
		this.tableName = tableName;
		if (type.equals(VirtualDatasourceType.POSTGRESQL)) {
			this.databaseType = SupportedDatabase.POSTGRES;
		} else if (type.equals(VirtualDatasourceType.MARIADB) || type.equals(VirtualDatasourceType.MYSQL)) {
			this.databaseType = SupportedDatabase.MARIADB;
		} else {
			throw new IllegalArgumentException("Virtual Datasource Type " + type + " not Supported");
		}
	}

	public TableKeysHolder getKeysHolder() throws SQLException {
		final TableKeysHolder kh = new TableKeysHolder(databaseType, tableName);
		getPrimaryKeys(kh);
		getForeignKeys(kh);
		getUniqueKeys(kh);
//		getExportedKeys(kh); We do not process inverse relations
		return kh;
	}

	private void getForeignKeys(TableKeysHolder kh) throws SQLException {
		final ResultSet rs = dm.getImportedKeys(null, null, tableName);
		while (rs.next()) {
			final int numColumns = rs.getMetaData().getColumnCount();
			String tableColumn = "";
			String referencedSchema = "";
			String referencedTable = "";
			String referencedColumn = "";
			for (int i = 1; i < numColumns + 1; i++) {
				final String columnName = rs.getMetaData().getColumnLabel(i);
				if ("pktable_schem".equalsIgnoreCase(columnName)) {
					referencedSchema = rs.getString(i);
				} else if ("pktable_name".equalsIgnoreCase(columnName)) {
					referencedTable = rs.getString(i);
				} else if ("pkcolumn_name".equalsIgnoreCase(columnName)) {
					referencedColumn = rs.getString(i);
				} else if ("fkcolumn_name".equalsIgnoreCase(columnName)) {
					tableColumn = rs.getString(i);
				}
			}
			kh.getForeignKeys()
					.add(ForeignKey.builder().database(databaseType).tableColumn(tableColumn)
							.referencedSchema(referencedSchema).referencedTable(referencedTable)
							.referencedColumn(referencedColumn).build());
			kh.getReferendTables().add(referencedTable);
		}
	}

	// PRIMARY KEY WITH MULTIPLE COLUMNS?
	private void getPrimaryKeys(TableKeysHolder kh) throws SQLException {
		final ResultSet rs = dm.getPrimaryKeys(null, null, tableName);
		while (rs.next()) {
			final int numColumns = rs.getMetaData().getColumnCount();
			String tableColumn = "";
			for (int i = 1; i < numColumns + 1; i++) {
				final String columnName = rs.getMetaData().getColumnLabel(i);
				if ("column_name".equalsIgnoreCase(columnName)) {
					tableColumn = rs.getString(i);
				}
			}
			kh.setPrimaryKey(PrimaryKey.builder().database(databaseType).tableColumn(tableColumn).build());
		}
	}

	private void getUniqueKeys(TableKeysHolder kh) throws SQLException {
		final ResultSet rs = dm.getIndexInfo(null, null, tableName, true, true);
		final Map<String, Set<String>> uniqueKeysMap = new HashMap<>();
		while (rs.next()) {
			final int numColumns = rs.getMetaData().getColumnCount();
			String tableColumn = "";
			String indexName = "";
			for (int i = 1; i < numColumns + 1; i++) {
				final String columnName = rs.getMetaData().getColumnLabel(i);
				if ("index_name".equalsIgnoreCase(columnName)) {
					indexName = rs.getString(i);
				} else if ("column_name".equalsIgnoreCase(columnName)) {
					tableColumn = rs.getString(i);
				}
			}
			if (!uniqueKeysMap.containsKey(indexName) && !indexName.equalsIgnoreCase("primary")) {
				uniqueKeysMap.put(indexName, new HashSet<>());
			}
			if (!indexName.equalsIgnoreCase("primary")) {
				uniqueKeysMap.get(indexName).add(tableColumn);
			}
		}
		uniqueKeysMap.entrySet().forEach(e -> {
			if (e.getValue().size() == 1 && kh.getPrimaryKey() != null
					&& kh.getPrimaryKey().getTableColumn().equals(e.getValue().iterator().next())) {
				log.debug("Index is also primary key, skipping this unique key");
			} else {
				kh.getUniqueKeys().add(UniqueKey.builder().database(databaseType).tableColumns(e.getValue()).build());
			}
		});
	}

	private void getExportedKeys(TableKeysHolder kh) throws SQLException {
		final ResultSet rs = dm.getExportedKeys(null, null, tableName);
		while (rs.next()) {
			final int numColumns = rs.getMetaData().getColumnCount();
			String tableName = "";
			for (int i = 1; i < numColumns + 1; i++) {
				final String columnName = rs.getMetaData().getColumnLabel(i);
				if ("fktable_name".equalsIgnoreCase(columnName)) {
					tableName = rs.getString(i);
				}
			}
			kh.getReferendTables().add(tableName);
		}
	}

}
