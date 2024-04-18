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
package com.minsait.onesait.platform.persistence.hadoop.kudu.table;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;

import com.minsait.onesait.platform.persistence.hadoop.hive.table.HiveColumn;
import com.minsait.onesait.platform.persistence.hadoop.util.FileUtil;
import com.minsait.onesait.platform.persistence.hadoop.util.HiveFieldType;
import com.minsait.onesait.platform.persistence.hadoop.util.JsonFieldType;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class KuduTableGeneratorTest {

	@InjectMocks
	private KuduTableGenerator kuduTableGenerator;

	private static final String ONTOLOGY_NAME = "test";
	private static final String FOLDER_PATH = "kudutable/";

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}
	/*
	 * public String getFileContent(String fileName) throws IOException {
	 * ClassLoader classLoader = getClass().getClassLoader(); File file = new
	 * File(classLoader.getResource(fileName).getFile()); return new
	 * String(Files.readAllBytes(file.toPath())); }
	 */

	/*@Test
	public void given_primitive_types_builTable() throws IOException {

		String schema = FileUtil.getFileContent(FOLDER_PATH + "kudutable_primitive_types_schema.json");

		KuduTable table = kuduTableGenerator.builTable(ONTOLOGY_NAME, schema, null);

		List<HiveColumn> columns = table.getColumns();

		Map<String, String> columnTypes = table.getColumns().stream()
				.collect(Collectors.toMap(x -> x.getName(), x -> x.getColumnType()));

		Assert.assertEquals(HiveFieldType.STRING_FIELD, columnTypes.get("field1"));
		Assert.assertEquals(HiveFieldType.FLOAT_FIELD, columnTypes.get("field2"));
		Assert.assertEquals(HiveFieldType.BOOLEAN_FIELD, columnTypes.get("field3"));
		Assert.assertEquals(HiveFieldType.INTEGER_FIELD, columnTypes.get("field4"));
		Assert.assertEquals(ONTOLOGY_NAME, table.getName());
		Assert.assertNotNull(columnTypes.get(JsonFieldType.PRIMARY_ID_FIELD));
		Assert.assertEquals(13, columns.size());

	}*/

	@Test
	public void given_primitive_types__without_root_field_builTable() throws IOException {

		String schema = FileUtil.getFileContent(FOLDER_PATH + "kudutable_primitive_types_without_root_schema.json");

		KuduTable table = kuduTableGenerator.builTable(ONTOLOGY_NAME, schema, null);

		List<HiveColumn> columns = table.getColumns();

		Map<String, String> columnTypes = table.getColumns().stream()
				.collect(Collectors.toMap(x -> x.getName(), x -> x.getColumnType()));

		Assert.assertEquals(HiveFieldType.STRING_FIELD, columnTypes.get("field1"));
		Assert.assertEquals(HiveFieldType.FLOAT_FIELD, columnTypes.get("field2"));
		Assert.assertEquals(HiveFieldType.BOOLEAN_FIELD, columnTypes.get("field3"));
		Assert.assertEquals(HiveFieldType.INTEGER_FIELD, columnTypes.get("field4"));
		Assert.assertEquals(ONTOLOGY_NAME, table.getName());
		Assert.assertNotNull(columnTypes.get(JsonFieldType.PRIMARY_ID_FIELD));
		Assert.assertEquals(13, columns.size());

	}

	@Test
	public void given_complex_types_builTable() throws IOException {

		String schema = FileUtil.getFileContent(FOLDER_PATH + "kudutable_complex_types_schema.json");

		KuduTable table = kuduTableGenerator.builTable(ONTOLOGY_NAME, schema, null);

		List<HiveColumn> columns = table.getColumns();

		Map<String, String> columnTypes = table.getColumns().stream()
				.collect(Collectors.toMap(x -> x.getName(), x -> x.getColumnType()));

		Assert.assertEquals(HiveFieldType.DOUBLE_FIELD, columnTypes.get("city" + HiveFieldType.LATITUDE_FIELD));
		Assert.assertEquals(HiveFieldType.DOUBLE_FIELD, columnTypes.get("city" + HiveFieldType.LONGITUDE_FIELD));
		Assert.assertEquals(HiveFieldType.TIMESTAMP_FIELD, columnTypes.get("created_date"));

		Assert.assertEquals(ONTOLOGY_NAME, table.getName());
		Assert.assertNotNull(columnTypes.get(JsonFieldType.PRIMARY_ID_FIELD));
		Assert.assertEquals(12, columns.size());

	}

}
