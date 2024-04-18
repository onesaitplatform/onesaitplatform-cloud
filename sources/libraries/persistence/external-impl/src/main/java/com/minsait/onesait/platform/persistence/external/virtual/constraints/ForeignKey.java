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

import com.minsait.onesait.platform.persistence.external.virtual.constraints.TableKeysHolder.SupportedDatabase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ForeignKey {

	private String tableColumn;
	/* FOR POSTGRES, CURRENTLY NOT BEING USED */
	private String referencedSchema;
	private String referencedTable;
	private String referencedColumn;
	private SupportedDatabase database;

	public String generateSQL(String tableName) {
		final String fkName = "fk_" + tableName.replace(".", "_") + "_" + referencedTable.replace(".", "_");
		switch (database) {
		case POSTGRES:

			return "ALTER TABLE " + tableName + " ADD CONSTRAINT " + fkName + " FOREIGN KEY " + "(" + tableColumn + ")"
					+ " REFERENCES " + referencedTable + " (" + referencedColumn + ");";
		case MARIADB:
		default:
			return "ALTER TABLE " + tableName + " ADD FOREIGN KEY (" + tableColumn + ") REFERENCES " + referencedTable
					+ "(" + referencedColumn + ");";
		}
	}

}
