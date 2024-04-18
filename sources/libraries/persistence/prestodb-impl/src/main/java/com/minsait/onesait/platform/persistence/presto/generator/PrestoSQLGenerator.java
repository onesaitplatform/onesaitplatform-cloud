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
package com.minsait.onesait.platform.persistence.presto.generator;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.commons.exception.GenericRuntimeOPException;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.OntologyPresto;
import com.minsait.onesait.platform.config.repository.OntologyPrestoRepository;
import com.minsait.onesait.platform.persistence.presto.PrestoOntologyBasicOpsDBRepository;
import com.minsait.onesait.platform.persistence.presto.generator.model.common.ColumnPresto;
import com.minsait.onesait.platform.persistence.presto.generator.model.statements.PrestoCreateStatement;
import com.minsait.onesait.platform.persistence.presto.generator.model.statements.PrestoDeleteStatement;
import com.minsait.onesait.platform.persistence.presto.generator.model.statements.PrestoDropStatement;
import com.minsait.onesait.platform.persistence.presto.generator.model.statements.PrestoInsertStatement;
import com.minsait.onesait.platform.persistence.presto.generator.model.statements.PrestoPreparedStatement;
import com.minsait.onesait.platform.persistence.presto.generator.model.statements.PrestoSelectStatement;

@Component
public class PrestoSQLGenerator implements PrestoSQLGeneratorInt {

	@Autowired
	private OntologyPrestoRepository ontologyPrestoRepository;

	@Autowired
	@Lazy
	private PrestoOntologyBasicOpsDBRepository prestoOntologyDBRepository;

	private PrestoSQLGeneratorOps sqlGeneratorOps = new PrestoSQLGeneratorOpsImpl();

	@Override
	public PrestoSelectStatement buildSelect() {
		return new PrestoSelectStatement(this);
	}

	@Override
	public PrestoInsertStatement buildInsert() {
		return new PrestoInsertStatement(this);
	}

	@Override
	public PrestoDeleteStatement buildDelete() {
		return new PrestoDeleteStatement(this);
	}

	@Override
	public PrestoDropStatement buildDrop() {
		return new PrestoDropStatement(this);
	}

	@Override
	public PrestoCreateStatement buildCreate() {
		return new PrestoCreateStatement(this);
	}

	private Map<String, Integer> getTableMetadataForOntology(final String catalog, final String schema,
			final String ontology) {
		try {
			return prestoOntologyDBRepository.getTableTypes(catalog, schema, ontology);
		} catch (final GenericRuntimeOPException exception) {
			throw new IllegalStateException("Error getting table metadata for the the ontology " + ontology, exception);
		}
	}

	public String getSqlTableDefinitionFromSchema(final Ontology ontology) {
		if (ontology.getJsonSchema() == null) {
			throw new IllegalArgumentException("Ontology schema not found in ontology");
		}
		return getSqlTableDefinitionFromSchema(ontology.getIdentification(), ontology.getJsonSchema());
	}

	public String getSqlTableDefinitionFromSchema(final String ontology, final String schema) {
		if (schema == null) {
			throw new IllegalArgumentException("Ontology schema not found in ontology");
		}
		final List<ColumnPresto> cols = sqlGeneratorOps.generateColumns(schema);
		PrestoCreateStatement createStatement = buildCreate().setOntology(ontology);
		createStatement.setColumnsPresto(cols);

		return sqlGeneratorOps.getStandardCreate(createStatement).getStatement();
	}

	public PrestoPreparedStatement getSQLCreateTable(PrestoCreateStatement createStatement) {
		return sqlGeneratorOps.getStandardCreate(createStatement);
	}

	@Override
	public PrestoPreparedStatement generate(final PrestoSelectStatement selectStatement, boolean withParams) {
		if (selectStatement != null) {
			OntologyPresto op = ontologyPrestoRepository
					.findOntologyPrestoByOntologyIdentification(selectStatement.getOntology());
			final String catalog = op.getDatasourceCatalog();
			final String schema = op.getDatasourceSchema();
			final String tableName = op.getDatasourceTableName();
			final Map<String, Integer> metaData;
			try {
				metaData = this.getTableMetadataForOntology(catalog, schema, tableName);
			} catch (IllegalStateException e) {
				return sqlGeneratorOps.getStandardSelect(selectStatement, withParams);
			}
			return sqlGeneratorOps.getStandardSelect(selectStatement, metaData, withParams);
		} else {
			throw new IllegalArgumentException("Select model can't be null");
		}
	}

	@Override
	public PrestoPreparedStatement generate(final PrestoInsertStatement insertStatement, boolean withParams) {
		if (insertStatement != null) {
			OntologyPresto op = ontologyPrestoRepository
					.findOntologyPrestoByOntologyIdentification(insertStatement.getOntology());
			final String catalog = op.getDatasourceCatalog();
			final String schema = op.getDatasourceSchema();
			final String tableName = op.getDatasourceTableName();
			final Map<String, Integer> metaData;
			try {
				metaData = this.getTableMetadataForOntology(catalog, schema, tableName);
			} catch (IllegalStateException e) {
				return sqlGeneratorOps.getStandardInsert(insertStatement, withParams);
			}
			return sqlGeneratorOps.getStandardInsert(insertStatement, metaData, withParams);
		} else {
			throw new IllegalArgumentException("Insert model can't be null");
		}
	}

	@Override
	public PrestoPreparedStatement generate(final PrestoDeleteStatement deleteStatement, boolean withParams) {
		if (deleteStatement != null) {
			OntologyPresto op = ontologyPrestoRepository
					.findOntologyPrestoByOntologyIdentification(deleteStatement.getOntology());
			final String catalog = op.getDatasourceCatalog();
			final String schema = op.getDatasourceSchema();
			final String tableName = op.getDatasourceTableName();
			final Map<String, Integer> metaData;
			try {
				metaData = this.getTableMetadataForOntology(catalog, schema, tableName);
			} catch (IllegalStateException e) {
				return sqlGeneratorOps.getStandardDelete(deleteStatement, withParams);
			}
			return sqlGeneratorOps.getStandardDelete(deleteStatement, metaData, withParams);
		} else {
			throw new IllegalArgumentException("Delete model can't be null");
		}
	}

	@Override
	public PrestoPreparedStatement generate(final PrestoDropStatement dropStatement) {
		if (dropStatement != null) {
			if (dropStatement.getType() == null) {
				dropStatement.setType("TABLE");
			}
			return sqlGeneratorOps.getStandardDrop(dropStatement);
		} else {
			throw new IllegalArgumentException("Drop model can't be null");
		}
	}

	@Override
	public PrestoPreparedStatement generate(final PrestoCreateStatement createStatement) {
		if (createStatement != null) {
			createStatement.setType("TABLE");
			return sqlGeneratorOps.getStandardCreate(createStatement);
		} else {
			throw new IllegalArgumentException("Create model can't be null");
		}
	}

}
