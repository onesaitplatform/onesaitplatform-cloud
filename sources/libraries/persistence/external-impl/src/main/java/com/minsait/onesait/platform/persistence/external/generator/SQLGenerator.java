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
package com.minsait.onesait.platform.persistence.external.generator;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.OntologyVirtual;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource.VirtualDatasourceType;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.OntologyVirtualRepository;
import com.minsait.onesait.platform.persistence.external.generator.model.common.ColumnRelational;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.CreateStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.DeleteStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.DropStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.InsertStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.PreparedStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.SelectStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.UpdateStatement;
import com.minsait.onesait.platform.persistence.external.virtual.VirtualDatasourcesManager;
import com.minsait.onesait.platform.persistence.external.virtual.VirtualOntologyOpsDBRepository;

@Component
public class SQLGenerator implements SQLGeneratorInt {

	@Autowired
	private OntologyRepository ontologyRepository;

	@Autowired
	private OntologyVirtualRepository ontologyVirtualRepository;

	@Autowired
	private VirtualOntologyOpsDBRepository virtualOntologyDBRepository;

	@Autowired
	private VirtualDatasourcesManager virtualDatasourcesManager;

	private SQLGeneratorOps sqlGeneratorOps = new SQLGeneratorOpsImpl();

	@Override
	public SelectStatement buildSelect() {
		return new SelectStatement(this);
	}

	@Override
	public InsertStatement buildInsert() {
		return new InsertStatement(this);
	}

	@Override
	public UpdateStatement buildUpdate() {
		return new UpdateStatement(this);
	}

	@Override
	public DeleteStatement buildDelete() {
		return new DeleteStatement(this);
	}

	@Override
	public DropStatement buildDrop() {
		return new DropStatement(this);
	}

	@Override
	public CreateStatement buildCreate() {
		return new CreateStatement(this);
	}

	private RtdbDatasource getDataSourceForOntology(final String ontology) {
		return ontologyRepository.findByIdentification(ontology).getRtdbDatasource();
	}

	private VirtualDatasourceType getVirtualDataSourceTypeForOntology(final String ontology) {
		return virtualDatasourcesManager.getDatasourceForOntology(ontology).getSgdb();
	}

	private Map<String, Integer> getTableMetadataForOntology(OntologyVirtualDatasource virtualDataSource,
			final String databaseName, final String schemaName, final String tableName) {
		try {
			return virtualOntologyDBRepository.getTableTypes(virtualDataSource.getIdentification(), databaseName,
					schemaName, tableName);
		} catch (final SQLException exception) {
			throw new IllegalStateException("Error getting table metadata for the the ontology " + tableName,
					exception);
		}
	}

	public String getSqlTableDefinitionFromSchema(final Ontology ontology) {
		VirtualDatasourceType datasource = getVirtualDataSourceTypeForOntology(ontology.getIdentification());
		if (datasource == null) {
			throw new IllegalArgumentException("Ontology must be a Virtual Ontology with relational datasource");
		}
		if (ontology.getJsonSchema() == null) {
			throw new IllegalArgumentException("Ontology schema not found in ontology");
		}
		return getSqlTableDefinitionFromSchema(ontology.getIdentification(), ontology.getJsonSchema(), datasource);
	}

	public String getSqlTableDefinitionFromSchema(final String ontology, final String schema,
			VirtualDatasourceType datasource) {
		if (datasource == null) {
			throw new IllegalArgumentException("Ontology must be a Virtual Ontology with relational datasource");
		}
		if (schema == null) {
			throw new IllegalArgumentException("Ontology schema not found in ontology");
		}
		final List<ColumnRelational> cols = sqlGeneratorOps.generateColumnsRelational(schema);
		CreateStatement createStatement = buildCreate().setOntology(ontology);
		createStatement.setColumnsRelational(cols);

		return sqlGeneratorOps.getStandardCreate(createStatement, datasource).getStatement();
	}

	public PreparedStatement getSQLCreateTable(CreateStatement createStatement, VirtualDatasourceType datasource) {
		PreparedStatement statement = null;
		if (datasource.equals(VirtualDatasourceType.ORACLE)) {
			// TODO: oracle databases drop if exists custom SQL
			statement = sqlGeneratorOps.getStandardCreate(createStatement, datasource);
		} else {
			statement = sqlGeneratorOps.getStandardCreate(createStatement, datasource);
		}

		return statement;
	}

	@Override
	public PreparedStatement generate(final SelectStatement selectStatement, boolean withParams) {
		if (selectStatement != null) {
			final RtdbDatasource ontologyDS = getDataSourceForOntology(selectStatement.getOntology());
			final PreparedStatement select;
			if (ontologyDS.equals(RtdbDatasource.VIRTUAL)) {
				final OntologyVirtualDatasource virtualDataSource = virtualDatasourcesManager
						.getDatasourceForOntology(selectStatement.getOntology());
				OntologyVirtual ov = ontologyVirtualRepository
						.findOntologyVirtualByOntologyIdentification(selectStatement.getOntology());
				final String ontologyVirtualTable = ov.getDatasourceTableName();
				final String ontologyVirtualDatabase = ov.getDatasourceDatabase();
				final String ontologyVirtualSchema = ov.getDatasourceSchema();

				final VirtualDatasourceType virtualDataSourceType = virtualDataSource.getSgdb();
				final Map<String, Integer> metaData = this.getTableMetadataForOntology(virtualDataSource,
						ontologyVirtualDatabase, ontologyVirtualSchema, ontologyVirtualTable);
				select = sqlGeneratorOps.getStandardSelect(selectStatement, virtualDataSourceType, metaData,
						withParams);
			} else {
				select = sqlGeneratorOps.getStandardSelect(selectStatement, withParams);
			}
			return select;
		} else {
			throw new IllegalArgumentException("Select model can't be null");
		}
	}

	@Override
	public PreparedStatement generate(final InsertStatement insertStatement, boolean withParams) {
		if (insertStatement != null) {
			final RtdbDatasource ontologyDS = getDataSourceForOntology(insertStatement.getOntology());
			final PreparedStatement insert;
			if (ontologyDS.equals(RtdbDatasource.VIRTUAL)) {

				final OntologyVirtualDatasource virtualDataSource = virtualDatasourcesManager
						.getDatasourceForOntology(insertStatement.getOntology());
				OntologyVirtual ov = ontologyVirtualRepository
						.findOntologyVirtualByOntologyIdentification(insertStatement.getOntology());
				final String ontologyVirtualTable = ov.getDatasourceTableName();
				final String ontologyVirtualDatabase = ov.getDatasourceDatabase();
				final String ontologyVirtualSchema = ov.getDatasourceSchema();

				final VirtualDatasourceType virtualDataSourceType = virtualDataSource.getSgdb();
				final Map<String, Integer> metaData = this.getTableMetadataForOntology(virtualDataSource,
						ontologyVirtualDatabase, ontologyVirtualSchema, ontologyVirtualTable);

				if (virtualDataSourceType.equals(VirtualDatasourceType.ORACLE)
						|| virtualDataSourceType.equals(VirtualDatasourceType.ORACLE11)) {
					insert = sqlGeneratorOps.getOracleInsertSQL(insertStatement, metaData, withParams);
				} else {
					insert = sqlGeneratorOps.getStandardInsert(insertStatement, virtualDataSourceType, metaData,
							withParams);
				}
			} else {
				insert = sqlGeneratorOps.getStandardInsert(insertStatement, true);
			}
			return insert;
		} else {
			throw new IllegalArgumentException("Insert model can't be null");
		}
	}

	@Override
	public PreparedStatement generate(final DeleteStatement deleteStatement, boolean withParams) {
		if (deleteStatement != null) {
			final RtdbDatasource ontologyDS = getDataSourceForOntology(deleteStatement.getOntology());
			if (ontologyDS.equals(RtdbDatasource.VIRTUAL)) {

				final OntologyVirtualDatasource virtualDataSource = virtualDatasourcesManager
						.getDatasourceForOntology(deleteStatement.getOntology());
				OntologyVirtual ov = ontologyVirtualRepository
						.findOntologyVirtualByOntologyIdentification(deleteStatement.getOntology());
				final String ontologyVirtualTable = ov.getDatasourceTableName();
				final String ontologyVirtualDatabase = ov.getDatasourceDatabase();
				final String ontologyVirtualSchema = ov.getDatasourceSchema();

				final VirtualDatasourceType virtualDataSourceType = virtualDataSource.getSgdb();
				final Map<String, Integer> metaData = this.getTableMetadataForOntology(virtualDataSource,
						ontologyVirtualDatabase, ontologyVirtualSchema, ontologyVirtualTable);

				return sqlGeneratorOps.getStandardDelete(deleteStatement, virtualDataSourceType, metaData, withParams);
			} else {
				return sqlGeneratorOps.getStandardDelete(deleteStatement, withParams);
			}
		} else {
			throw new IllegalArgumentException("Delete model can't be null");
		}
	}

	@Override
	public PreparedStatement generate(final UpdateStatement updateStatement, boolean withParams) {
		if (updateStatement != null) {
			final RtdbDatasource ontologyDS = getDataSourceForOntology(updateStatement.getOntology());
			if (ontologyDS.equals(RtdbDatasource.VIRTUAL)) {

				final OntologyVirtualDatasource virtualDataSource = virtualDatasourcesManager
						.getDatasourceForOntology(updateStatement.getOntology());
				OntologyVirtual ov = ontologyVirtualRepository
						.findOntologyVirtualByOntologyIdentification(updateStatement.getOntology());
				final String ontologyVirtualTable = ov.getDatasourceTableName();
				final String ontologyVirtualDatabase = ov.getDatasourceDatabase();
				final String ontologyVirtualSchema = ov.getDatasourceSchema();

				final VirtualDatasourceType virtualDataSourceType = virtualDataSource.getSgdb();
				final Map<String, Integer> metaData = this.getTableMetadataForOntology(virtualDataSource,
						ontologyVirtualDatabase, ontologyVirtualSchema, ontologyVirtualTable);

				return sqlGeneratorOps.getStandardUpdate(updateStatement, virtualDataSourceType, metaData, withParams);
			} else {
				return sqlGeneratorOps.getStandardUpdate(updateStatement, withParams);
			}
		} else {
			throw new IllegalArgumentException("Update model can't be null");
		}
	}

	@Override
	public PreparedStatement generate(final DropStatement dropStatement) {
		PreparedStatement statement = null;
		if (dropStatement != null) {
			if (dropStatement.getType() == null) {
				dropStatement.setType("TABLE");
			}
			final RtdbDatasource ontologyDS = getDataSourceForOntology(dropStatement.getOntology());
			if (ontologyDS.equals(RtdbDatasource.VIRTUAL)) {
				final VirtualDatasourceType virtualDataSource = this
						.getVirtualDataSourceTypeForOntology(dropStatement.getOntology());
				if (virtualDataSource.equals(VirtualDatasourceType.ORACLE)) {
					// TODO: oracle databases drop if exists custom SQL
					statement = sqlGeneratorOps.getOracleDrop(dropStatement, virtualDataSource);
				} else {
					statement = sqlGeneratorOps.getStandardDrop(dropStatement, virtualDataSource);
				}
			} else {
				statement = sqlGeneratorOps.getStandardDrop(dropStatement);
			}
		} else {
			throw new IllegalArgumentException("Drop model can't be null");
		}
		return statement;
	}

	@Override
	public PreparedStatement generate(final CreateStatement createStatement) {
		PreparedStatement statement = null;
		if (createStatement != null) {
			createStatement.setType("TABLE");

			final RtdbDatasource ontologyDS = getDataSourceForOntology(createStatement.getOntology());
			if (ontologyDS.equals(RtdbDatasource.VIRTUAL)) {
				final VirtualDatasourceType virtualDataSource = this
						.getVirtualDataSourceTypeForOntology(createStatement.getOntology());
				statement = getSQLCreateTable(createStatement, virtualDataSource);
			} else {
				statement = sqlGeneratorOps.getStandardCreate(createStatement);
			}
		} else {
			throw new IllegalArgumentException("Create model can't be null");
		}
		return statement;
	}
}
