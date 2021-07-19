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

package com.minsait.onesait.platform.business.services.ontology;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.minsait.onesait.platform.persistence.external.generator.model.common.ColumnRelational;
import com.minsait.onesait.platform.persistence.external.generator.model.common.Constraint;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.CreateStatement;
import com.minsait.onesait.platform.persistence.hadoop.kudu.table.CreateStatementKudu;
import com.minsait.onesait.platform.persistence.hadoop.kudu.table.KuduColumn;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class CreateStatementBusiness implements java.io.Serializable {

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
	private List<ColumnDefinitionBusiness> columnsRelational;
	@Getter
	@Setter
	private List<ConstraintBusiness> columnConstraints;
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
	
	
	public CreateStatementBusiness(CreateStatement statement) {
		this.ontology = statement.getOntology();
		this.type = statement.getType();
		this.columnsRelational = columnsDefinitionToDTO(statement.getColumnsRelational());
		this.columnConstraints = columnsConstraintsToDTO(statement.getColumnConstraints());
	}
	
	private List<ColumnDefinitionBusiness> columnsDefinitionToDTO(List<ColumnRelational> cols) {
		List<ColumnDefinitionBusiness> columnsDef = new ArrayList<>();
		for (ColumnRelational col: cols) {
			columnsDef.add(new ColumnDefinitionBusiness(col));
		}
		return columnsDef;
	}
	
	private List<ConstraintBusiness> columnsConstraintsToDTO(List<Constraint> cons) {
		List<ConstraintBusiness> constraintsDTO = new ArrayList<>();
		for (Constraint con: cons) {
			constraintsDTO.add(new ConstraintBusiness(con));
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
		for (ColumnDefinitionBusiness relationaltDTO: this.columnsRelational) {
			relationals.add(relationaltDTO.toColumnRelational());
		}
		return relationals;
	}
	
	private List<Constraint> columnsConstraints() {
		List<Constraint> constraints = new ArrayList<>();
		for (ConstraintBusiness constraintDTO: this.columnConstraints) {
			constraints.add(constraintDTO.toConstraint());
		}
		return constraints;
	}
	
	public CreateStatementBusiness(CreateStatementKudu statement) {
		this.ontology = statement.getOntology();
		this.type = statement.getType();
		this.columnsRelational = columnsDefinitionKuduToDTO(statement.getColumns());
		this.npartitions = statement.getNpartitions();
		this.partitions = statement.getPartitions();
		this.primaryKey = statement.getPrimaryKey();
		this.enablePartitionIndexes = statement.getEnablePartitionIndexes();
	}
	
	private List<ColumnDefinitionBusiness> columnsDefinitionKuduToDTO(List<KuduColumn> cols) {
		List<ColumnDefinitionBusiness> columnsDef = new ArrayList<>();
		for (KuduColumn col: cols) {
			columnsDef.add(new ColumnDefinitionBusiness(col));
		}
		return columnsDef;
	}
	
	public CreateStatementKudu toCreateStatementKudu() {
		CreateStatementKudu statement = new CreateStatementKudu();
		statement.setOntology(this.ontology);
		statement.setType(this.type);
		statement.setColumns(columnsRelationalsKudu());
		statement.setNpartitions(this.npartitions);
		statement.setPartitions(this.partitions);
		statement.setPrimaryKey(this.primaryKey);
		statement.setEnablePartitionIndexes(this.enablePartitionIndexes);
		
		return statement;
	}
	
	private List<KuduColumn> columnsRelationalsKudu() {
		List<KuduColumn> relationals = new ArrayList<>();
		for (ColumnDefinitionBusiness relationaltDTO: this.columnsRelational) {
			relationals.add(relationaltDTO.toKuduColumn());
		}
		return relationals;
	}
	
	
}
