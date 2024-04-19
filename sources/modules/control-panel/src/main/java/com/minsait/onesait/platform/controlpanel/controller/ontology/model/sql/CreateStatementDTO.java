/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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
package com.minsait.onesait.platform.controlpanel.controller.ontology.model.sql;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.minsait.onesait.platform.business.services.ontology.CreateStatementBusiness;
import com.minsait.onesait.platform.business.services.ontology.HistoricalOptionsBusiness;
import com.minsait.onesait.platform.business.services.ontology.ColumnDefinitionBusiness;
import com.minsait.onesait.platform.business.services.ontology.ConstraintBusiness;

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
	@Getter
	@Setter
	private HistoricalOptionsDTO historicalOptions;
	
	
	public CreateStatementDTO(CreateStatementBusiness statement) {
		this.ontology = statement.getOntology();
		this.type = statement.getType();
		this.columnsRelational = columnsDefinitionToDTO(statement.getColumnsRelational());
		this.columnConstraints = columnsConstraintsToDTO(statement.getColumnConstraints());
		this.primaryKey = statement.getPrimaryKey();
		this.partitions = statement.getPartitions();
		this.npartitions = statement.getNpartitions();
		this.enablePartitionIndexes = statement.getEnablePartitionIndexes();
	}
	
	private List<ColumnDefinitionDTO> columnsDefinitionToDTO(List<ColumnDefinitionBusiness> cols) {
		List<ColumnDefinitionDTO> columnsDef = new ArrayList<>();
		for (ColumnDefinitionBusiness col: cols) {
			columnsDef.add(new ColumnDefinitionDTO(col));
		}
		return columnsDef;
	}
	
	private List<ConstraintDTO> columnsConstraintsToDTO(List<ConstraintBusiness> cons) {
		List<ConstraintDTO> constraintsDTO = new ArrayList<>();
		for (ConstraintBusiness con: cons) {
			constraintsDTO.add(new ConstraintDTO(con));
		}
		return constraintsDTO;
	}
	
	public CreateStatementBusiness toCreateStatement() {
		CreateStatementBusiness statement = new CreateStatementBusiness();
		statement.setOntology(this.ontology);
		statement.setType(this.type);
		statement.setColumnsRelational(columnsRelationals());
		statement.setColumnConstraints(columnsConstraints());
		statement.setPartitions(this.partitions);
		statement.setNpartitions(this.npartitions);
		statement.setPrimaryKey(this.primaryKey);
		statement.setEnablePartitionIndexes(this.enablePartitionIndexes);
		if (historicalOptions != null) {
			statement.setHistoricalOptions(historicalOptions());;
		}
		return statement;
		
	}
	
	private List<ColumnDefinitionBusiness> columnsRelationals() {
		List<ColumnDefinitionBusiness> relationals = new ArrayList<>();
		for (ColumnDefinitionDTO relationaltDTO: this.columnsRelational) {
			relationals.add(relationaltDTO.toColumnRelational());
		}
		return relationals;
	}
	
	private List<ConstraintBusiness> columnsConstraints() {
		List<ConstraintBusiness> constraints = new ArrayList<>();
		for (ConstraintDTO constraintDTO: this.columnConstraints) {
			constraints.add(constraintDTO.toConstraint());
		}
		return constraints;
	}
	
	private HistoricalOptionsBusiness historicalOptions() {
		HistoricalOptionsBusiness fo = new HistoricalOptionsBusiness();
		if (this.historicalOptions.getFileFormat() != null) {
			fo.setFileFormat(this.historicalOptions.getFileFormat().toString());
		} else {
			fo.setFileFormat(null);
		}
		fo.setEscapeCharacter(this.historicalOptions.getEscapeCharacter());
		fo.setQuoteCharacter(this.historicalOptions.getQuoteCharacter());
		fo.setSeparatorCharacter(this.historicalOptions.getSeparatorCharacter());
		fo.setPartitions(this.historicalOptions.getPartitions());
		fo.setExternalLocation(this.historicalOptions.getExternalLocation());
		return fo;
	}

}
