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
package com.minsait.onesait.platform.persistence.external.virtual.constraints;

import java.util.HashSet;
import java.util.Set;

import lombok.Data;

@Data
public class TableKeysHolder {

	public enum SupportedDatabase {
		POSTGRES, MARIADB
	}

	private SupportedDatabase databaseType;
	private String tableName;
	private PrimaryKey primaryKey = new PrimaryKey();
	private Set<ForeignKey> foreignKeys = new HashSet<>();
	private Set<UniqueKey> uniqueKeys = new HashSet<>();
	private Set<String> referendTables = new HashSet<>();

	public TableKeysHolder(SupportedDatabase databaseType, String tableName) {
		this.databaseType = databaseType;
		this.tableName = tableName;
	}

	public String generateSQL(String targetTable) {
		final StringBuilder sb = new StringBuilder();
		sb.append(primaryKey.generateSQL(targetTable));
		uniqueKeys.forEach(uk -> sb.append(uk.generateSQL(targetTable)));
		foreignKeys.forEach(fk -> sb.append(fk.generateSQL(targetTable)));
		return sb.toString();
	}

	public String generateUKs(String targetTable) {
		final StringBuilder sb = new StringBuilder();
		sb.append(primaryKey.generateSQL(targetTable));
		uniqueKeys.forEach(uk -> sb.append(uk.generateSQL(targetTable)));
		return sb.toString();
	}

	public String generateFKs(String targetTable) {
		final StringBuilder sb = new StringBuilder();
		foreignKeys.forEach(fk -> sb.append(fk.generateSQL(targetTable)));
		return sb.toString();
	}

}
