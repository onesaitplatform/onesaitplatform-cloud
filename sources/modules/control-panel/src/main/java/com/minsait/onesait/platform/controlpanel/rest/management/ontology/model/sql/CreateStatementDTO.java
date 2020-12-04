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

package com.minsait.onesait.platform.controlpanel.rest.management.ontology.model.sql;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.minsait.onesait.platform.persistence.external.generator.model.common.ColumnRelational;
import com.minsait.onesait.platform.persistence.external.generator.model.common.Constraint;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.CreateStatement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class CreateStatementDTO implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	@Getter
	@Setter
	@NotNull
	private String ontology;
	@Getter
	@Setter
	private String type = null;
	@Getter
	@Setter
	@NotNull
	private List<ColumnDefinitionDTO> columnsRelational;
	@Getter
	@Setter
	@NotNull
	private List<ConstraintDTO> columnConstraints;
	
	public CreateStatementDTO(CreateStatement statement) {
		this.ontology = statement.getOntology();
		this.type = statement.getType();
		this.columnsRelational = columnsDefinitionToDTO(statement.getColumnsRelational());
		this.columnConstraints = columnsConstraintsToDTO(statement.getColumnConstraints());
		
	}
	
	private List<ColumnDefinitionDTO> columnsDefinitionToDTO(List<ColumnRelational> cols) {
		List<ColumnDefinitionDTO> columnsDef = new ArrayList<>();
		for (ColumnRelational col: cols) {
			columnsDef.add(new ColumnDefinitionDTO(col));
		}
		return columnsDef;
	}
	
	private List<ConstraintDTO> columnsConstraintsToDTO(List<Constraint> cons) {
		List<ConstraintDTO> constraintsDTO = new ArrayList<>();
		for (Constraint con: cons) {
			constraintsDTO.add(new ConstraintDTO(con));
		}
		return constraintsDTO;
	}
	
	public CreateStatement toCreateStatement() {
		CreateStatement statement = new CreateStatement();
		statement.setOntology(this.ontology);
		statement.setType(this.type);
		statement.setColumnsRelational(columnsRelationals());
		statement.setColumnConstraints(columnsConstraints());
		
		return statement;
		
	}
	
	private List<ColumnRelational> columnsRelationals() {
		List<ColumnRelational> relationals = new ArrayList<>();
		for (ColumnDefinitionDTO relationaltDTO: this.columnsRelational) {
			relationals.add(relationaltDTO.toColumnRelational());
		}
		return relationals;
	}
	
	private List<Constraint> columnsConstraints() {
		List<Constraint> constraints = new ArrayList<>();
		for (ConstraintDTO constraintDTO: this.columnConstraints) {
			constraints.add(constraintDTO.toConstraint());
		}
		return constraints;
	}

}
