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
package com.minsait.onesait.platform.business.services.ontology;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.minsait.onesait.platform.persistence.external.generator.model.common.ColumnRelational;
import com.minsait.onesait.platform.persistence.external.generator.model.common.Constraint;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.CreateStatement;
import com.minsait.onesait.platform.persistence.presto.generator.model.common.ColumnPresto;
import com.minsait.onesait.platform.persistence.presto.generator.model.common.HistoricalOptions;
import com.minsait.onesait.platform.persistence.presto.generator.model.statements.PrestoCreateStatement;

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
	@NotNull
	private String database;
	@Getter
	@Setter
	@NotNull
	private String schema;
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
	@Getter
	@Setter
	private HistoricalOptionsBusiness historicalOptions;

	public CreateStatementBusiness(CreateStatement statement) {
		this.ontology = statement.getOntology();
		this.type = statement.getType();
		this.columnsRelational = columnsDefinitionToDTO(statement.getColumnsRelational());
		this.columnConstraints = columnsConstraintsToDTO(statement.getColumnConstraints());
	}

	private List<ColumnDefinitionBusiness> columnsDefinitionToDTO(List<ColumnRelational> cols) {
		final List<ColumnDefinitionBusiness> columnsDef = new ArrayList<>();
		for (final ColumnRelational col : cols) {
			columnsDef.add(new ColumnDefinitionBusiness(col));
		}
		return columnsDef;
	}

	private List<ConstraintBusiness> columnsConstraintsToDTO(List<Constraint> cons) {
		final List<ConstraintBusiness> constraintsDTO = new ArrayList<>();
		for (final Constraint con : cons) {
			constraintsDTO.add(new ConstraintBusiness(con));
		}
		return constraintsDTO;
	}

	public CreateStatement toCreateStatement() {
		final CreateStatement statement = new CreateStatement();
		statement.setOntology(this.ontology);
		statement.setDatabase(this.database);
		statement.setSchema(this.schema);
		statement.setType(this.type);
		statement.setColumnsRelational(columnsRelationals());
		statement.setColumnConstraints(columnsConstraints());

		return statement;
	}

	private List<ColumnRelational> columnsRelationals() {
		final List<ColumnRelational> relationals = new ArrayList<>();
		for (final ColumnDefinitionBusiness relationaltDTO : this.columnsRelational) {
			relationals.add(relationaltDTO.toColumnRelational());
		}
		return relationals;
	}

	private List<Constraint> columnsConstraints() {
		final List<Constraint> constraints = new ArrayList<>();
		for (final ConstraintBusiness constraintDTO : this.columnConstraints) {
			constraints.add(constraintDTO.toConstraint());
		}
		return constraints;
	}

	public PrestoCreateStatement toCreateStatementPresto() {
		final PrestoCreateStatement statement = new PrestoCreateStatement();
		statement.setOntology(this.ontology);
		statement.setDatabase(this.database);
		statement.setSchema(this.schema);
		statement.setType(this.type);
		statement.setColumnsPresto(columnsPresto());
		if (this.historicalOptions != null) {
			statement.setHistoricalOptions(historicalOptionsPresto());
		}
		return statement;
	}

	private List<ColumnPresto> columnsPresto() {
		final List<ColumnPresto> columns = new ArrayList<>();
		for (final ColumnDefinitionBusiness column : this.columnsRelational) {
			columns.add(column.toColumnPresto());
		}
		return columns;
	}

	private HistoricalOptions historicalOptionsPresto() {
		final HistoricalOptions ho = new HistoricalOptions();
		ho.setExternalLocation(this.historicalOptions.getExternalLocation());
		ho.setPartitions(this.historicalOptions.getPartitions());
		ho.setFileFormat(this.historicalOptions.getFileFormat());
		ho.setEscapeCharacter(this.historicalOptions.getEscapeCharacter());
		ho.setQuoteCharacter(this.historicalOptions.getQuoteCharacter());
		ho.setSeparatorCharacter(this.historicalOptions.getSeparatorCharacter());
		return ho;
	}

}
