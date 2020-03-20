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

import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource.VirtualDatasourceType;
import com.minsait.onesait.platform.persistence.external.generator.model.common.OrderByStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.common.WhereStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.DeleteStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.InsertStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.SelectStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.UpdateStatement;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Types;
import java.util.*;

public class SQLGeneratorOpsImplTest {
	private SQLGeneratorOps generatorOps = new SQLGeneratorOpsImpl();

	private static Map<String, Integer> columnsMap = new LinkedHashMap<>();

	private final static String ONTOLOGY = "ontology";

	/** EXPECTED SELECTS **/
	private final static String EXPECTED_COMMON_SELECT = "SELECT column_1, column_2, column_3, column_4 FROM ontology WHERE column_1 < 123 AND column_2 = 'string' AND column_3 = 1 AND column_4 = '2019-01-19 03:14:07' ORDER BY column_1 DESC, column_4 LIMIT 50 OFFSET 10";
	private final static String EXPECTED_MYSQL_SELECT = "SELECT column_1, column_2, column_3, column_4 FROM ontology WHERE column_1 < 123 AND column_2 = 'string' AND column_3 = true AND column_4 = {ts '2019-01-19 03:14:07.0'} ORDER BY column_1 DESC, column_4 LIMIT 50 OFFSET 10";
	private final static String EXPECTED_MARIA_SELECT = "SELECT column_1, column_2, column_3, column_4 FROM ontology WHERE column_1 < 123 AND column_2 = 'string' AND column_3 = true AND column_4 = {ts '2019-01-19 03:14:07.0'} ORDER BY column_1 DESC, column_4 LIMIT 50 OFFSET 10";
	private final static String EXPECTED_HIVE_SELECT = "SELECT column_1, column_2, column_3, column_4 FROM ontology WHERE column_1 < 123 AND column_2 = 'string' AND column_3 = true AND column_4 = {ts '2019-01-19 03:14:07.0'} ORDER BY column_1 DESC, column_4 LIMIT 50 OFFSET 10";
	private final static String EXPECTED_IMPALA_SELECT = "SELECT column_1, column_2, column_3, column_4 FROM ontology WHERE column_1 < 123 AND column_2 = 'string' AND column_3 = true AND column_4 = {ts '2019-01-19 03:14:07.0'} ORDER BY column_1 DESC, column_4 LIMIT 50 OFFSET 10";
	private final static String EXPECTED_ORACLE_SELECT = "SELECT column_1, column_2, column_3, column_4 FROM ontology WHERE column_1 < 123 AND column_2 = 'string' AND column_3 = true AND column_4 = {ts '2019-01-19 03:14:07.0'} ORDER BY column_1 DESC, column_4 OFFSET 10 ROWS FETCH NEXT 50 ROWS ONLY";
	private final static String EXPECTED_SQLSERVER_SELECT = "SELECT column_1, column_2, column_3, column_4 FROM ontology WHERE column_1 < 123 AND column_2 = 'string' AND column_3 = true AND column_4 = {ts '2019-01-19 03:14:07.0'} ORDER BY column_1 DESC, column_4 OFFSET 10 ROWS FETCH NEXT 50 ROWS ONLY";
	private final static String EXPECTED_POSTGRESQL_SELECT = "SELECT column_1, column_2, column_3, column_4 FROM ontology WHERE column_1 < 123 AND column_2 = 'string' AND column_3 = true AND column_4 = {ts '2019-01-19 03:14:07.0'} ORDER BY column_1 DESC, column_4 LIMIT 50 OFFSET 10";

	/** EXPECTED INSERTS **/
	private final static String EXPECTED_COMMON_INSERT = "INSERT INTO ontology (column_1, column_2, column_3, column_4) VALUES (123, 'string', 1, '2019-01-19 03:14:07'), (456, NULL, 0, '2017-05-13 06:18:09'), (789, 'string', 0, NULL)";
	private final static String EXPECTED_MYSQL_INSERT = "INSERT INTO ontology (column_1, column_2, column_3, column_4) VALUES (123, 'string', true, {ts '2019-01-19 03:14:07.0'}), (456, NULL, FALSE, {ts '2017-05-13 06:18:09.0'}), (789, 'string', false, NULL)";
	private final static String EXPECTED_MARIA_INSERT = "INSERT INTO ontology (column_1, column_2, column_3, column_4) VALUES (123, 'string', true, {ts '2019-01-19 03:14:07.0'}), (456, NULL, FALSE, {ts '2017-05-13 06:18:09.0'}), (789, 'string', false, NULL)";
	private final static String EXPECTED_HIVE_INSERT = "INSERT INTO ontology (column_1, column_2, column_3, column_4) VALUES (123, 'string', true, {ts '2019-01-19 03:14:07.0'}), (456, NULL, FALSE, {ts '2017-05-13 06:18:09.0'}), (789, 'string', false, NULL)";
	private final static String EXPECTED_IMPALA_INSERT = "INSERT INTO ontology (column_1, column_2, column_3, column_4) VALUES (123, 'string', true, {ts '2019-01-19 03:14:07.0'}), (456, NULL, FALSE, {ts '2017-05-13 06:18:09.0'}), (789, 'string', false, NULL)";
	private final static String EXPECTED_ORACLE_INSERT = "INSERT ALL INTO ontology (column_1, column_2, column_3, column_4) VALUES (123, 'string', true, {ts '2019-01-19 03:14:07.0'}) INTO ontology (column_1, column_2, column_3, column_4) VALUES (456, NULL, FALSE, {ts '2017-05-13 06:18:09.0'}) INTO ontology (column_1, column_2, column_3, column_4) VALUES (789, 'string', false, NULL) SELECT * FROM dual";
	private final static String EXPECTED_SQLSERVER_INSERT = "INSERT INTO ontology (column_1, column_2, column_3, column_4) VALUES (123, 'string', true, {ts '2019-01-19 03:14:07.0'}), (456, NULL, FALSE, {ts '2017-05-13 06:18:09.0'}), (789, 'string', false, NULL)";
	private final static String EXPECTED_POSTGRESQL_INSERT = "INSERT INTO ontology (column_1, column_2, column_3, column_4) VALUES (123, 'string', true, {ts '2019-01-19 03:14:07.0'}), (456, NULL, FALSE, {ts '2017-05-13 06:18:09.0'}), (789, 'string', false, NULL)";

	/** EXPECTED UPDATE **/
	private final static String EXPECTED_COMMON_UPDATE = "UPDATE ontology SET column_1 = 123, column_2 = 'string', column_3 = 1, column_4 = '2019-01-19 03:14:07' WHERE column_1 < 123 AND column_2 = 'string' AND column_3 = 1 AND column_4 = '2019-01-19 03:14:07'";
	private final static String EXPECTED_MYSQL_UPDATE = "UPDATE ontology SET column_1 = 123, column_2 = 'string', column_3 = true, column_4 = {ts '2019-01-19 03:14:07.0'} WHERE column_1 < 123 AND column_2 = 'string' AND column_3 = true AND column_4 = {ts '2019-01-19 03:14:07.0'}";
	private final static String EXPECTED_MARIA_UPDATE = "UPDATE ontology SET column_1 = 123, column_2 = 'string', column_3 = true, column_4 = {ts '2019-01-19 03:14:07.0'} WHERE column_1 < 123 AND column_2 = 'string' AND column_3 = true AND column_4 = {ts '2019-01-19 03:14:07.0'}";
	private final static String EXPECTED_HIVE_UPDATE = "UPDATE ontology SET column_1 = 123, column_2 = 'string', column_3 = true, column_4 = {ts '2019-01-19 03:14:07.0'} WHERE column_1 < 123 AND column_2 = 'string' AND column_3 = true AND column_4 = {ts '2019-01-19 03:14:07.0'}";
	private final static String EXPECTED_IMPALA_UPDATE = "UPDATE ontology SET column_1 = 123, column_2 = 'string', column_3 = true, column_4 = {ts '2019-01-19 03:14:07.0'} WHERE column_1 < 123 AND column_2 = 'string' AND column_3 = true AND column_4 = {ts '2019-01-19 03:14:07.0'}";
	private final static String EXPECTED_ORACLE_UPDATE = "UPDATE ontology SET column_1 = 123, column_2 = 'string', column_3 = true, column_4 = {ts '2019-01-19 03:14:07.0'} WHERE column_1 < 123 AND column_2 = 'string' AND column_3 = true AND column_4 = {ts '2019-01-19 03:14:07.0'}";
	private final static String EXPECTED_SQLSERVER_UPDATE = "UPDATE ontology SET column_1 = 123, column_2 = 'string', column_3 = true, column_4 = {ts '2019-01-19 03:14:07.0'} WHERE column_1 < 123 AND column_2 = 'string' AND column_3 = true AND column_4 = {ts '2019-01-19 03:14:07.0'}";
	private final static String EXPECTED_POSTGRESQL_UPDATE = "UPDATE ontology SET column_1 = 123, column_2 = 'string', column_3 = true, column_4 = {ts '2019-01-19 03:14:07.0'} WHERE column_1 < 123 AND column_2 = 'string' AND column_3 = true AND column_4 = {ts '2019-01-19 03:14:07.0'}";

	/** EXPECTED DELETE **/
	private final static String EXPECTED_COMMON_DELETE = "DELETE FROM ontology WHERE column_1 < 123 AND column_2 = 'string' AND column_3 = 1 AND column_4 = '2019-01-19 03:14:07'";
	private final static String EXPECTED_MYSQL_DELETE = "DELETE FROM ontology WHERE column_1 < 123 AND column_2 = 'string' AND column_3 = true AND column_4 = {ts '2019-01-19 03:14:07.0'}";
	private final static String EXPECTED_MARIA_DELETE = "DELETE FROM ontology WHERE column_1 < 123 AND column_2 = 'string' AND column_3 = true AND column_4 = {ts '2019-01-19 03:14:07.0'}";
	private final static String EXPECTED_HIVE_DELETE = "DELETE FROM ontology WHERE column_1 < 123 AND column_2 = 'string' AND column_3 = true AND column_4 = {ts '2019-01-19 03:14:07.0'}";
	private final static String EXPECTED_IMPALA_DELETE = "DELETE FROM ontology WHERE column_1 < 123 AND column_2 = 'string' AND column_3 = true AND column_4 = {ts '2019-01-19 03:14:07.0'}";
	private final static String EXPECTED_ORACLE_DELETE = "DELETE FROM ontology WHERE column_1 < 123 AND column_2 = 'string' AND column_3 = true AND column_4 = {ts '2019-01-19 03:14:07.0'}";
	private final static String EXPECTED_SQLSERVER_DELETE = "DELETE FROM ontology WHERE column_1 < 123 AND column_2 = 'string' AND column_3 = true AND column_4 = {ts '2019-01-19 03:14:07.0'}";
	private final static String EXPECTED_POSTGRESQL_DELETE = "DELETE FROM ontology WHERE column_1 < 123 AND column_2 = 'string' AND column_3 = true AND column_4 = {ts '2019-01-19 03:14:07.0'}";

	@BeforeClass
	public static void setUp()  {
		columnsMap.put("column_1", Types.INTEGER);
		columnsMap.put("column_2", Types.VARCHAR);
		columnsMap.put("column_3", Types.BOOLEAN);
		columnsMap.put("column_4", Types.TIMESTAMP);
	}

	@Test
	public void testStandardSelect() {
		final List<String> columns = Arrays.asList("column_1", "column_2", "column_3", "column_4");

		final List<WhereStatement> where = Arrays.asList(
				new WhereStatement("column_1","<","123"),
				new WhereStatement("column_2","=","string"),
				new WhereStatement("column_3","=","true"),
				new WhereStatement("column_4","=","2019-01-19 03:14:07")
		);

		final List<OrderByStatement> ordersBy = Arrays.asList(
				new OrderByStatement().setColumn("column_1").setOrder("DESC"),
				new OrderByStatement().setColumn("column_4").setOrder("ASC")
		);

		final SelectStatement select = new SelectStatement()
				.setOntology(ONTOLOGY)
				.setColumns(columns)
				.setWhere(where)
				.setLimit(50)
				.setOffset(10)
				.setOrderBy(ordersBy);

		final String plainSelect = generatorOps.getStandardSelect(select).toString();

		final String mysqlSelect = generatorOps.getStandardSelect(select, VirtualDatasourceType.MYSQL, columnsMap).toString();
		final String mariaSelect = generatorOps.getStandardSelect(select, VirtualDatasourceType.MARIADB, columnsMap).toString();
		final String hiveSelect = generatorOps.getStandardSelect(select, VirtualDatasourceType.HIVE, columnsMap).toString();
		final String impalaSelect = generatorOps.getStandardSelect(select, VirtualDatasourceType.IMPALA, columnsMap).toString();
		final String oracleSelect = generatorOps.getStandardSelect(select, VirtualDatasourceType.ORACLE, columnsMap).toString();
		final String sqlServerSelect = generatorOps.getStandardSelect(select, VirtualDatasourceType.SQLSERVER, columnsMap).toString();
		final String postgresSelect = generatorOps.getStandardSelect(select, VirtualDatasourceType.POSTGRESQL, columnsMap).toString();

		Assert.assertEquals("Standard SQL select is not passing test",EXPECTED_COMMON_SELECT, plainSelect);
		Assert.assertEquals("MySQL select is not passing test",EXPECTED_MYSQL_SELECT, mysqlSelect);
		Assert.assertEquals("MariaDB select is not passing test", EXPECTED_MARIA_SELECT, mariaSelect);
		Assert.assertEquals("Hive select is not passing test", EXPECTED_HIVE_SELECT, hiveSelect);
		Assert.assertEquals("Impala select is not passing test",EXPECTED_IMPALA_SELECT, impalaSelect);
		Assert.assertEquals("Oracle select is not passing test",EXPECTED_ORACLE_SELECT, oracleSelect);
		Assert.assertEquals("SQLServer select is not passing test",EXPECTED_SQLSERVER_SELECT, sqlServerSelect);
		Assert.assertEquals("PostgreSQL select is not passing test",EXPECTED_POSTGRESQL_SELECT, postgresSelect);
	}

	@Test
	public void testStandardInsert() {
		final List<String> columns = Arrays.asList("column_1", "column_2", "column_3", "column_4");
		final Map<String, String> valuesMap1 = new LinkedHashMap<>();
		valuesMap1.put("column_1", "123");
		valuesMap1.put("column_2", "string");
		valuesMap1.put("column_3", "true");
		valuesMap1.put("column_4", "2019-01-19 03:14:07");

		final Map<String, String> valuesMap2 = new LinkedHashMap<>();
		valuesMap2.put("column_1", "456");
		valuesMap2.put("column_3", "FALSE");
		valuesMap2.put("column_4", "2017-05-13 06:18:09");

		final Map<String, String> valuesMap3 = new LinkedHashMap<>();
		valuesMap3.put("column_1", "789");
		valuesMap3.put("column_2", "string");
		valuesMap3.put("column_3", "false");

		final List<Map<String, String>> values = Arrays.asList(valuesMap1,valuesMap2,valuesMap3);

		final InsertStatement insertStatement = new InsertStatement()
				.setOntology(ONTOLOGY)
				.setColumns(columns)
				.setValues(values);

		final String commonInsert = this.generatorOps.getStandardInsert(insertStatement).toString();

		final String mysqlInsert = this.generatorOps.getStandardInsert(insertStatement, VirtualDatasourceType.MYSQL, columnsMap).toString();
		final String mariaInsert = this.generatorOps.getStandardInsert(insertStatement, VirtualDatasourceType.MARIADB, columnsMap).toString();
		final String hiveInsert = this.generatorOps.getStandardInsert(insertStatement, VirtualDatasourceType.HIVE, columnsMap).toString();
		final String impalaInsert = this.generatorOps.getStandardInsert(insertStatement, VirtualDatasourceType.IMPALA, columnsMap).toString();
		final String sqlServerInsert = this.generatorOps.getStandardInsert(insertStatement, VirtualDatasourceType.SQLSERVER, columnsMap).toString();
		final String postgreSQLInsert = this.generatorOps.getStandardInsert(insertStatement, VirtualDatasourceType.POSTGRESQL, columnsMap).toString();
		final String oracleInsert = this.generatorOps.getOracleInsertSQL(insertStatement, columnsMap);

		Assert.assertEquals("Standard SQL Insert is not passing the test", EXPECTED_COMMON_INSERT, commonInsert);
		Assert.assertEquals("MySQL Insert is not passing the test", EXPECTED_MYSQL_INSERT, mysqlInsert);
		Assert.assertEquals("MariaDB Insert is not passing the test", EXPECTED_MARIA_INSERT, mariaInsert);
		Assert.assertEquals("Hive Insert is not passing the test", EXPECTED_HIVE_INSERT, hiveInsert);
		Assert.assertEquals("Impala Insert is not passing the test", EXPECTED_IMPALA_INSERT, impalaInsert);
		Assert.assertEquals("SQLServer Insert is not passing the test", EXPECTED_SQLSERVER_INSERT, sqlServerInsert);
		Assert.assertEquals("PostgreSQL Insert is not passing the test", EXPECTED_POSTGRESQL_INSERT, postgreSQLInsert);
		Assert.assertEquals("Oracle SQL Insert is not passing the test", EXPECTED_ORACLE_INSERT, oracleInsert);
	}

	@Test
	public void testStandardUpdate() {
		final Map<String, String> valuesMap1 = new LinkedHashMap<>();
		valuesMap1.put("column_1", "123");
		valuesMap1.put("column_2", "string");
		valuesMap1.put("column_3", "true");
		valuesMap1.put("column_4", "2019-01-19 03:14:07");

		final List<WhereStatement> where = Arrays.asList(
				new WhereStatement("column_1","<","123"),
				new WhereStatement("column_2","=","string"),
				new WhereStatement("column_3","=","true"),
				new WhereStatement("column_4","=","2019-01-19 03:14:07")
		);

		final UpdateStatement updateStatement = new UpdateStatement()
				.setOntology(ONTOLOGY)
				.setValues(valuesMap1)
				.setWhere(where);

		final String commonUpdate = generatorOps.getStandardUpdate(updateStatement).toString();

		final String mysqlUpdate = generatorOps.getStandardUpdate(updateStatement, VirtualDatasourceType.MYSQL, columnsMap).toString();
		final String mariaUpdate = generatorOps.getStandardUpdate(updateStatement, VirtualDatasourceType.MARIADB, columnsMap).toString();
		final String hiveUpdate = generatorOps.getStandardUpdate(updateStatement, VirtualDatasourceType.HIVE, columnsMap).toString();
		final String impalaUpdate = generatorOps.getStandardUpdate(updateStatement, VirtualDatasourceType.IMPALA, columnsMap).toString();
		final String oracleUpdate = generatorOps.getStandardUpdate(updateStatement, VirtualDatasourceType.ORACLE, columnsMap).toString();
		final String sqlServerUpdate = generatorOps.getStandardUpdate(updateStatement, VirtualDatasourceType.SQLSERVER, columnsMap).toString();
		final String postgresUpdate = generatorOps.getStandardUpdate(updateStatement, VirtualDatasourceType.POSTGRESQL, columnsMap).toString();

		Assert.assertEquals("Standard SQL update is not passing test",EXPECTED_COMMON_UPDATE, commonUpdate);
		Assert.assertEquals("MySQL update is not passing test",EXPECTED_MYSQL_UPDATE, mysqlUpdate);
		Assert.assertEquals("MariaDB update is not passing test", EXPECTED_MARIA_UPDATE, mariaUpdate);
		Assert.assertEquals("Hive update is not passing test", EXPECTED_HIVE_UPDATE, hiveUpdate);
		Assert.assertEquals("Impala update is not passing test",EXPECTED_IMPALA_UPDATE, impalaUpdate);
		Assert.assertEquals("Oracle update is not passing test",EXPECTED_ORACLE_UPDATE, oracleUpdate);
		Assert.assertEquals("SQLServer update is not passing test",EXPECTED_SQLSERVER_UPDATE, sqlServerUpdate);
		Assert.assertEquals("PostgreSQL update is not passing test",EXPECTED_POSTGRESQL_UPDATE, postgresUpdate);
	}

	@Test
	public void testOracle11LimitOffset() {
		final String oracleQueryLimit = "SELECT column_1, column_2, column_3, column_4 FROM (SELECT column_1, column_2, column_3, column_4 FROM table WHERE column_1 < 123 AND column_2 = 'string' AND column_3 = true AND column_4 = {ts '2019-01-19 03:14:07.0'} ORDER BY column_1 DESC, column_4) WHERE ROWNUM <= 4";
		final String oracleQueryLimitOffset = "SELECT column_1, column_2, column_3, column_4 FROM (SELECT column_1, column_2, column_3, column_4, ROWNUM AS ROWNUMALIAS FROM (SELECT column_1, column_2, column_3, column_4 FROM table WHERE column_1 < 123 AND column_2 = 'string' AND column_3 = true AND column_4 = {ts '2019-01-19 03:14:07.0'} ORDER BY column_1 DESC, column_4) WHERE ROWNUM <= 8) WHERE ROWNUMALIAS > 4";

		final List<String> columns = Arrays.asList("column_1", "column_2", "column_3", "column_4");

		final List<WhereStatement> where = Arrays.asList(
				new WhereStatement("column_1","<","123"),
				new WhereStatement("column_2","=","string"),
				new WhereStatement("column_3","=","true"),
				new WhereStatement("column_4","=","2019-01-19 03:14:07")
		);

		final List<OrderByStatement> ordersBy = Arrays.asList(
				new OrderByStatement().setColumn("column_1").setOrder("DESC"),
				new OrderByStatement().setColumn("column_4").setOrder("ASC")
		);

		final SelectStatement selectLimit = new SelectStatement()
				.setColumns(columns)
				.setOntology("table")
				.setLimit(4)
				.setWhere(where)
				.setOrderBy(ordersBy);

		final SelectStatement selectOffset = new SelectStatement()
				.setColumns(columns)
				.setOntology("table")
				.setOffset(4)
				.setLimit(4)
				.setWhere(where)
				.setOrderBy(ordersBy);

		final String generatedWithLimit = generatorOps.getStandardSelect(selectLimit, VirtualDatasourceType.ORACLE11, columnsMap).toString();
		final String generatedWithOffset = generatorOps.getStandardSelect(selectOffset, VirtualDatasourceType.ORACLE11, columnsMap).toString();

		Assert.assertEquals(oracleQueryLimit, generatedWithLimit);
		Assert.assertEquals(oracleQueryLimitOffset, generatedWithOffset);
	}

	@Test
	public void testStandardDelete() {
		final List<WhereStatement> where = Arrays.asList(
				new WhereStatement("column_1","<","123"),
				new WhereStatement("column_2","=","string"),
				new WhereStatement("column_3","=","true"),
				new WhereStatement("column_4","=","2019-01-19 03:14:07")
		);

		final DeleteStatement deleteStatement = new DeleteStatement()
				.setOntology(ONTOLOGY)
				.setWhere(where);

		final String commonDelete = generatorOps.getStandardDelete(deleteStatement).toString();

		final String mysqlDelete = generatorOps.getStandardDelete(deleteStatement, VirtualDatasourceType.MYSQL, columnsMap).toString();
		final String mariaDelete = generatorOps.getStandardDelete(deleteStatement, VirtualDatasourceType.MARIADB, columnsMap).toString();
		final String hiveDelete = generatorOps.getStandardDelete(deleteStatement, VirtualDatasourceType.HIVE, columnsMap).toString();
		final String impalaDelete = generatorOps.getStandardDelete(deleteStatement, VirtualDatasourceType.IMPALA, columnsMap).toString();
		final String oracleDelete = generatorOps.getStandardDelete(deleteStatement, VirtualDatasourceType.ORACLE, columnsMap).toString();
		final String sqlServerDelete = generatorOps.getStandardDelete(deleteStatement, VirtualDatasourceType.SQLSERVER, columnsMap).toString();
		final String postgresDelete = generatorOps.getStandardDelete(deleteStatement, VirtualDatasourceType.POSTGRESQL, columnsMap).toString();

		Assert.assertEquals("Standard SQL delete is not passing test",EXPECTED_COMMON_DELETE, commonDelete);
		Assert.assertEquals("MySQL delete is not passing test",EXPECTED_MYSQL_DELETE, mysqlDelete);
		Assert.assertEquals("MariaDB delete is not passing test", EXPECTED_MARIA_DELETE, mariaDelete);
		Assert.assertEquals("Hive delete is not passing test", EXPECTED_HIVE_DELETE, hiveDelete);
		Assert.assertEquals("Impala delete is not passing test",EXPECTED_IMPALA_DELETE, impalaDelete);
		Assert.assertEquals("Oracle delete is not passing test",EXPECTED_ORACLE_DELETE, oracleDelete);
		Assert.assertEquals("SQLServer delete is not passing test",EXPECTED_SQLSERVER_DELETE, sqlServerDelete);
		Assert.assertEquals("PostgreSQL delete is not passing test",EXPECTED_POSTGRESQL_DELETE, postgresDelete);
	}

}