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

import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource.VirtualDatasourceType;
import com.minsait.onesait.platform.persistence.external.generator.model.common.ColumnRelational;
import com.minsait.onesait.platform.persistence.external.generator.model.common.Constraint;
import com.minsait.onesait.platform.persistence.external.generator.model.common.Constraint.ConstraintType;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.CreateStatement;
import com.minsait.onesait.platform.persistence.external.virtual.VirtualDatasourcesManager;

import net.sf.jsqlparser.statement.create.table.ColDataType;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.any;

@RunWith(MockitoJUnitRunner.class)
public class SQLGeneratorImplTest {
	
	@InjectMocks
	private SQLGenerator generator;
	
	@Mock
	private VirtualDatasourcesManager virtualDatasourcesManager;
	
	private final static String ONTOLOGY = "ontology";

	/** EXPECTED CREATE **/
	private final static String EXPECTED_MYSQL_CREATE = "CREATE TABLE ontology (campo1 VARCHAR(255) COMMENT 'ontology/campo1', campo2 VARCHAR(255) COMMENT 'ontology/campo2')";
	private final static String EXPECTED_MARIA_CREATE = "CREATE TABLE ontology (campo1 VARCHAR(255) COMMENT 'ontology/campo1', campo2 VARCHAR(255) COMMENT 'ontology/campo2')";
	private final static String EXPECTED_HIVE_CREATE = "CREATE TABLE ontology (campo1 VARCHAR(255) COMMENT 'ontology/campo1', campo2 VARCHAR(255) COMMENT 'ontology/campo2')";
	private final static String EXPECTED_IMPALA_CREATE = "CREATE TABLE ontology (campo1 VARCHAR(255) COMMENT 'ontology/campo1', campo2 VARCHAR(255) COMMENT 'ontology/campo2')";
	private final static String EXPECTED_ORACLE_CREATE = "CREATE TABLE ontology (campo1 CHAR(255) COMMENT 'ontology/campo1', campo2 CHAR(255) COMMENT 'ontology/campo2')";
	private final static String EXPECTED_SQLSERVER_CREATE = "CREATE TABLE ontology (campo1 VARCHAR(255) COMMENT 'ontology/campo1', campo2 VARCHAR(255) COMMENT 'ontology/campo2')";
	private final static String EXPECTED_POSTGRESQL_CREATE = "CREATE TABLE ontology (campo1 VARCHAR(255) , campo2 VARCHAR(255) )";
	
	/** EXPECTED CREATE **/
	private final static String EXPECTED_MYSQL_CREATE_STATEMENT = "CREATE TABLE ontology (campo1 VARCHAR(255) COMMENT 'ontology/campo1', campo2 VARCHAR(255) COMMENT 'ontology/campo2', PRIMARY KEY (campo1))";
	private final static String EXPECTED_MARIA_CREATE_STATEMENT = "CREATE TABLE ontology (campo1 VARCHAR(255) COMMENT 'ontology/campo1', campo2 VARCHAR(255) COMMENT 'ontology/campo2', PRIMARY KEY (campo1))";
	private final static String EXPECTED_HIVE_CREATE_STATEMENT = "CREATE TABLE ontology (campo1 VARCHAR(255) COMMENT 'ontology/campo1', campo2 VARCHAR(255) COMMENT 'ontology/campo2', PRIMARY KEY (campo1))";
	private final static String EXPECTED_IMPALA_CREATE_STATEMENT = "CREATE TABLE ontology (campo1 VARCHAR(255) COMMENT 'ontology/campo1', campo2 VARCHAR(255) COMMENT 'ontology/campo2', PRIMARY KEY (campo1))";
	private final static String EXPECTED_ORACLE_CREATE_STATEMENT = "CREATE TABLE ontology (campo1 CHAR(255) COMMENT 'ontology/campo1', campo2 CHAR(255) COMMENT 'ontology/campo2', PRIMARY KEY (campo1))";
	private final static String EXPECTED_SQLSERVER_CREATE_STATEMENT = "CREATE TABLE ontology (campo1 VARCHAR(255) COMMENT 'ontology/campo1', campo2 VARCHAR(255) COMMENT 'ontology/campo2', PRIMARY KEY (campo1))";
	private final static String EXPECTED_POSTGRESQL_CREATE_STATEMENT = "CREATE TABLE ontology (campo1 VARCHAR(255) , campo2 VARCHAR(255) , PRIMARY KEY (campo1))";
	
	private final static String EXAMPLE_SIMPLE_SCHEMA =  "{" + 
			"    \"$schema\": \"http://json-schema.org/draft-03/schema\"," + 
			"    \"id\": \"ontology\"," + 
			"    \"type\": \"object\"," + 
			"    \"required\": false," +
			"    \"properties\": {" + 
			"        \"campo1\": {" + 
			"            \"type\": \"string\"," + 
			"            \"id\": \"ontology/campo1\"," + 
			"            \"required\": false" + 
			"        }," + 
			"        \"campo2\": {" + 
			"            \"type\": \"string\"," + 
			"            \"id\": \"ontology/campo2\"," + 
			"            \"required\": false" + 
			"        }" + 
			"    }" +
			"}";


	public Ontology generateOntology() {
		Ontology ontology = new Ontology();
		ontology.setIdentification(ONTOLOGY);
		ontology.setJsonSchema(EXAMPLE_SIMPLE_SCHEMA);
		return ontology;
	}
	
	public ColumnRelational generateColumnRelational(String name, String type) {
		ColDataType dType = new ColDataType();
		dType.setDataType(type);
		
		ColumnRelational campo = new ColumnRelational();
		campo.setColumnName(name);
		campo.setColDataType(dType);
		campo.setNotNull(false);
		campo.setColComment(ONTOLOGY + "/" + name);
		
		return campo;
	}
	
	public Constraint generateConstraint(String name, String field) {
		Constraint constraint = new Constraint();
		constraint.setName(name);
		constraint.setColumnsNames(new ArrayList<>(Arrays.asList(field)));
		constraint.setType(ConstraintType.PRIMARY_KEY.name());
		
		return constraint;
	}
	
	public CreateStatement generateCreateStatement() {
		ColDataType dType = new ColDataType();
		dType.setDataType("string");
		
		ColumnRelational campo1 = generateColumnRelational("campo1", "string");
		ColumnRelational campo2 = generateColumnRelational("campo2", "string");
		
		
		List<Constraint> constraints = new ArrayList<>();
		constraints.add(generateConstraint("PK_campo1", "campo1"));
		CreateStatement statement = new CreateStatement();
		statement.setOntology(ONTOLOGY);
		statement.setColumnsRelational(Arrays.asList(campo1, campo2));
		statement.setColumnConstraints(constraints);
		
		return statement;
		
	}
	
	public OntologyVirtualDatasource generateDataSource(VirtualDatasourceType dsType) {
		OntologyVirtualDatasource datasource = new OntologyVirtualDatasource();
		datasource.setSgdb(dsType);
		return datasource;
	}
	
	@Test
	public void testGetSqlTableDefinitionFromSchema() {
		when(virtualDatasourcesManager.getDatasourceForOntology(any())).thenReturn(generateDataSource(VirtualDatasourceType.MYSQL));
		String mysqlDefinition = generator.getSqlTableDefinitionFromSchema(generateOntology());
		Assert.assertEquals("Generation table SQL definition for MYSQL is not passing test", EXPECTED_MYSQL_CREATE, mysqlDefinition);
		
		when(virtualDatasourcesManager.getDatasourceForOntology(any())).thenReturn(generateDataSource(VirtualDatasourceType.MARIADB));
		String mariaDefinition = generator.getSqlTableDefinitionFromSchema(generateOntology());
		Assert.assertEquals("Generation table SQL definition for MARIADB is not passing test", EXPECTED_MARIA_CREATE, mariaDefinition);
		
		when(virtualDatasourcesManager.getDatasourceForOntology(any())).thenReturn(generateDataSource(VirtualDatasourceType.HIVE));
		String hiveDefinition = generator.getSqlTableDefinitionFromSchema(generateOntology());
		Assert.assertEquals("Generation table SQL definition for HIVE is not passing test", EXPECTED_HIVE_CREATE, hiveDefinition);
		
		when(virtualDatasourcesManager.getDatasourceForOntology(any())).thenReturn(generateDataSource(VirtualDatasourceType.IMPALA));
		String impalaDefinition = generator.getSqlTableDefinitionFromSchema(generateOntology());
		Assert.assertEquals("Generation table SQL definition for IMPALA is not passing test", EXPECTED_IMPALA_CREATE, impalaDefinition);
		
		when(virtualDatasourcesManager.getDatasourceForOntology(any())).thenReturn(generateDataSource(VirtualDatasourceType.ORACLE));
		String oracleDefinition = generator.getSqlTableDefinitionFromSchema(generateOntology());
		Assert.assertEquals("Generation table SQL definition for ORACLE is not passing test", EXPECTED_ORACLE_CREATE, oracleDefinition);
		
		when(virtualDatasourcesManager.getDatasourceForOntology(any())).thenReturn(generateDataSource(VirtualDatasourceType.SQLSERVER));
		String sqlserverDefinition = generator.getSqlTableDefinitionFromSchema(generateOntology());
		Assert.assertEquals("Generation table SQL definition for SQLSERVER is not passing test", EXPECTED_SQLSERVER_CREATE, sqlserverDefinition);
		
		when(virtualDatasourcesManager.getDatasourceForOntology(any())).thenReturn(generateDataSource(VirtualDatasourceType.POSTGRESQL));
		String postgreDefinition = generator.getSqlTableDefinitionFromSchema(generateOntology());
		Assert.assertEquals("Generation table SQL definition for POSTGRESQL is not passing test", EXPECTED_POSTGRESQL_CREATE, postgreDefinition);
			
	}
	
	@Test
	public void testGetSQLCreateTable() {
		when(virtualDatasourcesManager.getDatasourceForOntology(any())).thenReturn(generateDataSource(VirtualDatasourceType.MYSQL));
		String mysqlDefinition = generator.getSQLCreateTable(generateCreateStatement(), VirtualDatasourceType.MYSQL).getStatement();
		Assert.assertEquals("Generation table SQL definition for MYSQL is not passing test", EXPECTED_MYSQL_CREATE_STATEMENT, mysqlDefinition);
		
		when(virtualDatasourcesManager.getDatasourceForOntology(any())).thenReturn(generateDataSource(VirtualDatasourceType.MARIADB));
		String mariaDefinition = generator.getSQLCreateTable(generateCreateStatement(), VirtualDatasourceType.MARIADB).getStatement();
		Assert.assertEquals("Generation table SQL definition for MARIADB is not passing test", EXPECTED_MARIA_CREATE_STATEMENT, mariaDefinition);
		
		when(virtualDatasourcesManager.getDatasourceForOntology(any())).thenReturn(generateDataSource(VirtualDatasourceType.HIVE));
		String hiveDefinition = generator.getSQLCreateTable(generateCreateStatement(), VirtualDatasourceType.HIVE).getStatement();
		Assert.assertEquals("Generation table SQL definition for HIVE is not passing test", EXPECTED_HIVE_CREATE_STATEMENT, hiveDefinition);
		
		when(virtualDatasourcesManager.getDatasourceForOntology(any())).thenReturn(generateDataSource(VirtualDatasourceType.IMPALA));
		String impalaDefinition = generator.getSQLCreateTable(generateCreateStatement(), VirtualDatasourceType.IMPALA).getStatement();
		Assert.assertEquals("Generation table SQL definition for IMPALA is not passing test", EXPECTED_IMPALA_CREATE_STATEMENT, impalaDefinition);
		
		when(virtualDatasourcesManager.getDatasourceForOntology(any())).thenReturn(generateDataSource(VirtualDatasourceType.ORACLE));
		String oracleDefinition = generator.getSQLCreateTable(generateCreateStatement(), VirtualDatasourceType.ORACLE).getStatement();
		Assert.assertEquals("Generation table SQL definition for ORACLE is not passing test", EXPECTED_ORACLE_CREATE_STATEMENT, oracleDefinition);
		
		when(virtualDatasourcesManager.getDatasourceForOntology(any())).thenReturn(generateDataSource(VirtualDatasourceType.SQLSERVER));
		String sqlserverDefinition = generator.getSQLCreateTable(generateCreateStatement(), VirtualDatasourceType.SQLSERVER).getStatement();
		Assert.assertEquals("Generation table SQL definition for SQLSERVER is not passing test", EXPECTED_SQLSERVER_CREATE_STATEMENT, sqlserverDefinition);
		
		when(virtualDatasourcesManager.getDatasourceForOntology(any())).thenReturn(generateDataSource(VirtualDatasourceType.POSTGRESQL));
		String postgreDefinition = generator.getSQLCreateTable(generateCreateStatement(), VirtualDatasourceType.POSTGRESQL).getStatement();
		Assert.assertEquals("Generation table SQL definition for POSTGRESQL is not passing test", EXPECTED_POSTGRESQL_CREATE_STATEMENT, postgreDefinition);
			
	}

}