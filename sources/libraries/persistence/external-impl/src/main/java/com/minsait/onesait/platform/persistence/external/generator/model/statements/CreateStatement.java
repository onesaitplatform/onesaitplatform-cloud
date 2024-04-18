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
package com.minsait.onesait.platform.persistence.external.generator.model.statements;

import com.minsait.onesait.platform.persistence.external.generator.SQLGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import com.minsait.onesait.platform.persistence.external.generator.model.common.ColumnRelational;
import com.minsait.onesait.platform.persistence.external.generator.model.common.Constraint;
import com.minsait.onesait.platform.persistence.external.generator.model.common.Constraint.ConstraintType;

@NoArgsConstructor
public class CreateStatement extends CreateTable implements SQLStatement {
	@NotNull
	@Size(min = 1)
	@Getter
	private String ontology;
	@Getter
	@Setter
	private String type = null;
	@Getter
	@Setter
	List<ColumnRelational> columnsRelational = new ArrayList<>();
	@Getter
	@Setter
	List<Constraint> columnConstraints = new ArrayList<>();
	@NotNull
	@Getter
	@Setter
	private SQLGenerator sqlGenerator;

	public CreateStatement(final SQLGenerator sqlGenerator) {
		this.sqlGenerator = sqlGenerator;
	}

	
	public CreateStatement setOntology(String ontology) {
		if(ontology != null && !ontology.trim().isEmpty()) {
			this.ontology = ontology.trim();
			this.setTable(new Table(this.ontology));
		} else {
			throw new IllegalArgumentException("Ontology in model can't be null or empty");
		}
		return this;
		
	}
	
	public CreateStatement addColumnRelational(ColumnRelational col) {
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
	
	public CreateStatement addConstraint(Constraint con) {
		
		for (Index index: this.getIndexes()) {
			if (index.getName().equals(con.getName())) {
				throw new IllegalArgumentException("Invalid input: duplicated column name: " + con.getName()); 
			}
			if (con.getEnumType().equals(ConstraintType.PRIMARY_KEY)
					&& index.getType().equalsIgnoreCase("PRIMARY KEY") ) {
				throw new IllegalArgumentException("Invalid input: PRIMARY KEY already exists: " + index.toString()); 
			}
			
		}

		this.columnConstraints.add(con);
		return this;
	}
	
	@Override
	public PreparedStatement generate(boolean withParams) {
		if(sqlGenerator != null) {
			return sqlGenerator.generate(this);
		} else {
			throw new IllegalStateException("SQL Generator service is not set, use SQLGenerator instance to generate or build the statement instead");
		}
	}

}
