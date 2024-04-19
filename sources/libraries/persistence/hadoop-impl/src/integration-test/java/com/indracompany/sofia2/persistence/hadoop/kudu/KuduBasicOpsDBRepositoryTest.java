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
package com.indracompany.sofia2.persistence.hadoop.kudu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.minsait.onesait.platform.commons.testing.IntegrationTest;
import com.minsait.onesait.platform.persistence.hadoop.common.NameBeanConst;
import com.minsait.onesait.platform.persistence.hadoop.util.FileUtil;
import com.minsait.onesait.platform.persistence.hadoop.util.JsonFieldType;
import com.minsait.onesait.platform.persistence.interfaces.BasicOpsDBRepository;
import com.minsait.onesait.platform.persistence.interfaces.ManageDBRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Category(IntegrationTest.class)
@Ignore
public class KuduBasicOpsDBRepositoryTest {

	@Autowired
	@Qualifier(NameBeanConst.KUDU_BASIC_OPS_BEAN_NAME)
	private BasicOpsDBRepository kuduBasicOpsDBRepository;

	@Autowired
	@Qualifier(NameBeanConst.KUDU_MANAGE_DB_REPO_BEAN_NAME)
	private ManageDBRepository kuduManageDBRepository;

	private static String ontology_name = "test";
	private static final String FOLDER_PATH = "kudutable/";
	private static String schema = "";

	private static final String instance1 = "{\"field1\":  \"field1value\", \"field2\": 1.0, \"field3\": true, \"field4\": 2, \"contextdata\": {\"deviceTemplate\" : \"\",\"device\": \"\",\"clientConnection\": \"\",\"clientSession\": \"\",\"user\": \"\",\"timezoneId\": \"\", \"timestamp\": \"\", \"timestampMillis\": 23123123}}";
	private static final String instance2 = "{\"field1\":  \"field2value\", \"field2\": 2.0, \"field3\": false, \"field4\": 3, \"contextdata\": {\"deviceTemplate\" : \"\",\"device\": \"\",\"clientConnection\": \"\",\"clientSession\": \"\",\"user\": \"\",\"timezoneId\": \"\", \"timestamp\": \"\", \"timestampMillis\": 23123123}}";

	private static final List<String> instances = Arrays.asList(instance1, instance2);

	private static boolean runDataInitialization = true;

	// All tests are comented because in CI we don't use a cloudera installation to
	// test it in integration environment
	// create temporaly table for tests
	@PostConstruct
	public void init() throws IOException {
		if (runDataInitialization) {
			ontology_name = ontology_name + "_" + Calendar.getInstance().getTimeInMillis();
			schema = FileUtil.getFileContent(FOLDER_PATH + "kudutable_primitive_types_schema.json");
			kuduManageDBRepository.createTable4Ontology(ontology_name, schema, null);
			runDataInitialization = false;
		}
	}

	@PreDestroy
	public void destroy() {
		// remove temporaly table for tests
		kuduManageDBRepository.removeTable4Ontology(ontology_name);
	}

	@Before
	public void setUp() throws IOException {
		// delete all rows before each test
		kuduBasicOpsDBRepository.delete(ontology_name, false);
	}

	@Ignore
	@Test
	public void given_insert() {
		String oid = kuduBasicOpsDBRepository.insert(ontology_name, instances.get(0));

		JSONObject jObject = new JSONObject(oid);
		String id = jObject.getString(JsonFieldType.PRIMARY_ID_FIELD);

		String instance = kuduBasicOpsDBRepository.findById(ontology_name, id);
		assertNotNull(instance);
		assertNotEquals("", instance);
	}

	@Ignore
	@Test
	public void given_findById() {

		String id = kuduBasicOpsDBRepository.insert(ontology_name, instances.get(0));
		String instance = kuduBasicOpsDBRepository.findById(ontology_name, id);

		assertNotNull(instance);
		assertNotEquals("", instance);
	}

	@Ignore
	@Test
	public void given_findAll() {
		kuduBasicOpsDBRepository.insert(ontology_name, instances.get(0));
		kuduBasicOpsDBRepository.insert(ontology_name, instances.get(1));

		List<String> instances = kuduBasicOpsDBRepository.findAll(ontology_name);

		assertEquals(2, instances.size());
	}

	@Ignore
	@Test
	public void given_count() {
		kuduBasicOpsDBRepository.insert(ontology_name, instances.get(0));
		kuduBasicOpsDBRepository.insert(ontology_name, instances.get(1));

		long num = kuduBasicOpsDBRepository.count(ontology_name);

		assertEquals(2, num);
	}

	@Ignore
	@Test
	public void given_delete_by_id() {
		String oid = kuduBasicOpsDBRepository.insert(ontology_name, instances.get(0));

		JSONObject jObject = new JSONObject(oid);
		String id = jObject.getString(JsonFieldType.PRIMARY_ID_FIELD);

		long rows = kuduBasicOpsDBRepository.deleteNativeById(ontology_name, id).getCount();

		assertEquals(1, rows);
	}

}
