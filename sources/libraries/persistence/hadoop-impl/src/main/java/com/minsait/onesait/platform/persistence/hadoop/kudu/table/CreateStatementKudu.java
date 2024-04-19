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
package com.minsait.onesait.platform.persistence.hadoop.kudu.table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.CreateTable;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@NoArgsConstructor
public class CreateStatementKudu extends CreateTable {
	@NotNull
	@Size(min = 1)
	@Getter
	private String ontology;
	@Getter
	@Setter
	private String type = null;
	@Getter
	@Setter
	List<KuduColumn> columns = new ArrayList<>();
	@Getter
	@Setter
	private String primaryKey;
	@Getter
	@Setter
	private String partitions;
	@Getter
	@Setter
	private String npartitions;
	@Getter
	@Setter
	private Boolean enablePartitionIndexes;

		
	public CreateStatementKudu setOntology(String ontology) {
		if(ontology != null && !ontology.trim().isEmpty()) {
			this.ontology = ontology.trim();
			this.setTable(new Table(this.ontology));
		} else {
			throw new IllegalArgumentException("Ontology in model can't be null or empty");
		}
		return this;
		
	}
	/*
	public CreateStatementKudu addColumnRelational(ColumnRelationalKudu col) {
		List<String> colNames = new ArrayList<>();
		List<ColumnDefinition> existentCols = getColumnDefinitions();
		if (existentCols != null) {
			for (ColumnDefinition colDef: existentCols ) {
				colNames.add(colDef.getColumnName());
			}
			if (colNames.contains(col.getColumnName())) {
				throw new IllegalArgumentException("Invalid input: duplicated column name: " + col.getColumnName()); 
			}
		}
		this.columnsRelational.add(col);
		return this;
	}
	*/
}
