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
package com.minsait.onesait.platform.persistence.external.generator;

import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource.VirtualDatasourceType;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.persistence.external.generator.helper.SQLHelper;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.DeleteStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.InsertStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.UpdateStatement;
import com.minsait.onesait.platform.persistence.external.virtual.VirtualDatasourcesManager;
import com.minsait.onesait.platform.persistence.external.virtual.VirtualOntologyDBRepository;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.SelectStatement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Map;

@Component
public class SQLGenerator implements SQLGeneratorInt {

	@Autowired
	private OntologyRepository ontologyRepository;

	@Autowired
	private VirtualOntologyDBRepository virtualOntologyDBRepository;

	@Autowired
	private VirtualDatasourcesManager virtualDatasourcesManager;

	private SQLGeneratorOps sqlGeneratorOps = new SQLGeneratorOpsImpl();

	@Override
	public SelectStatement buildSelect(){
		return new SelectStatement(this);
	}

	@Override
	public InsertStatement buildInsert(){
		return new InsertStatement(this);
	}

	@Override
	public UpdateStatement buildUpdate(){
		return new UpdateStatement(this);
	}

	@Override
	public DeleteStatement buildDelete(){
		return new DeleteStatement(this);
	}

	private RtdbDatasource getDataSourceForOntology(final String ontology){
		return ontologyRepository.findByIdentification(ontology).getRtdbDatasource();
	}

	private VirtualDatasourceType getVirtualDataSourceTypeForOntology(final String ontology){
		return virtualDatasourcesManager.getDatasourceForOntology(ontology).getSgdb();
	}

	private Map<String, Integer> getTableMetadataForOntology(final String ontology){
		try {
			final String dataSource = virtualDatasourcesManager.getDatasourceForOntology(ontology).getDatasourceName();
			return virtualOntologyDBRepository.getTableTypes(dataSource, ontology);
		} catch (final SQLException exception){
			throw new IllegalStateException("Error getting table metadata for the the ontology "+ontology, exception);
		}
	}

	@Override
	public String generate(final SelectStatement selectStatement) {
		if(selectStatement != null){
			final RtdbDatasource ontologyDS = getDataSourceForOntology(selectStatement.getOntology());
			final PlainSelect select;
			if(ontologyDS.equals(RtdbDatasource.VIRTUAL)){
				final VirtualDatasourceType virtualDataSource = this.getVirtualDataSourceTypeForOntology(selectStatement.getOntology());
				final Map<String, Integer> metaData = this.getTableMetadataForOntology(selectStatement.getOntology());
				select = sqlGeneratorOps.getStandardSelect(selectStatement, virtualDataSource, metaData);
			} else {
				select = sqlGeneratorOps.getStandardSelect(selectStatement);
			}
			return select.toString();
		} else {
			throw new IllegalArgumentException("Select model can't be null");
		}
	}

	@Override
	public String generate(final InsertStatement insertStatement){
		if(insertStatement != null){
			final RtdbDatasource ontologyDS = getDataSourceForOntology(insertStatement.getOntology());
			final String insert;
			if(ontologyDS.equals(RtdbDatasource.VIRTUAL)){
				final VirtualDatasourceType virtualDataSource = this.getVirtualDataSourceTypeForOntology(insertStatement.getOntology());
				final Map<String, Integer> metaData = this.getTableMetadataForOntology(insertStatement.getOntology());
				if(virtualDataSource.equals(VirtualDatasourceType.ORACLE)) {
					insert = sqlGeneratorOps.getOracleInsertSQL(insertStatement, metaData);
				} else{
					insert = sqlGeneratorOps.getStandardInsert(insertStatement, virtualDataSource, metaData).toString();
				}
			} else {
				insert = sqlGeneratorOps.getStandardInsert(insertStatement).toString();
			}
			return insert;
		} else {
			throw new IllegalArgumentException("Insert model can't be null");
		}
	}

	@Override
	public String generate(final DeleteStatement deleteStatement){
		if(deleteStatement != null){
			final RtdbDatasource ontologyDS = getDataSourceForOntology(deleteStatement.getOntology());
			if(ontologyDS.equals(RtdbDatasource.VIRTUAL)){
				final VirtualDatasourceType virtualDataSource = this.getVirtualDataSourceTypeForOntology(deleteStatement.getOntology());
				final Map<String, Integer> metaData = this.getTableMetadataForOntology(deleteStatement.getOntology());
				return sqlGeneratorOps.getStandardDelete(deleteStatement, virtualDataSource, metaData).toString();
			} else {
				return sqlGeneratorOps.getStandardDelete(deleteStatement).toString();
			}
		} else {
			throw new IllegalArgumentException("Delete model can't be null");
		}
	}

	@Override
	public String generate(final UpdateStatement updateStatement) {
		if(updateStatement != null){
			final RtdbDatasource ontologyDS = getDataSourceForOntology(updateStatement.getOntology());
			if(ontologyDS.equals(RtdbDatasource.VIRTUAL)){
				final VirtualDatasourceType virtualDataSource = this.getVirtualDataSourceTypeForOntology(updateStatement.getOntology());
				final Map<String, Integer> metaData = this.getTableMetadataForOntology(updateStatement.getOntology());
				return sqlGeneratorOps.getStandardUpdate(updateStatement, virtualDataSource, metaData).toString();
			} else {
				return sqlGeneratorOps.getStandardUpdate(updateStatement).toString();
			}
		} else {
			throw new IllegalArgumentException("Update model can't be null");
		}
	}
}

