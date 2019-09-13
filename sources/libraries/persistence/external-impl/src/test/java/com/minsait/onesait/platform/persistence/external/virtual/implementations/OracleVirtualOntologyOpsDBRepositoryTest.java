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
package com.minsait.onesait.platform.persistence.external.virtual.implementations;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.minsait.onesait.platform.persistence.external.virtual.VirtualRelationalOntologyOpsDBRepository;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
@Ignore("There is NO ORACLE in CI Environment")
public class OracleVirtualOntologyOpsDBRepositoryTest {

	// @Autowired
	// private OracleVirtualOntologyOpsDBRepository oracleVirtualRepo;

	@Autowired
	private VirtualRelationalOntologyOpsDBRepository virtualRepo;

	@Before
	public void doBefore() throws Exception {

	}

	@After
	public void tearDown() {

	}

	// @Test
	// public void listTablesTest() {
	// oracleVirtualRepo.init("oracle4");
	// List<String> lTables = oracleVirtualRepo.getTables();
	// for (String table : lTables) {
	// log.info(table);
	// }
	// }

	// @Test
	// public void listExecuteSelectTest() {
	// oracleVirtualRepo.init("oracle4");
	//
	// try {
	// oracleVirtualRepo.executeQuery("Select * from Mitabla t where t.cond=10 and
	// t.cond2<25");
	// } catch (Exception e) {
	// }
	//
	// }

	@Test
	public void testInsert() {
		for (int i = 0; i < 100; i++) {
			virtualRepo.insert("OpenPlatform", "", "{\"COLUMN1\": \"string1\",\"COLUMN2\": \"string2\",\"COLUMN3\": "
					+ i + ",\"COLUMN4\": 4.4,\"COLUMN5\":true,\"COLUMN6\": \"string 3\",\"COLUMN7\": 12}");
		}

	}

	@Test
	public void findById() {
		String result = virtualRepo.findById("OpenPlatform", "12");
		System.out.println("############################################################################");
		System.out.println(result);
		System.out.println("############################################################################");
	}

	// @Test
	public void findAllAsJson() {
		String result = virtualRepo.findAllAsJson("OpenPlatform");
		System.out.println("############################################################################");
		System.out.println(result);
		System.out.println("############################################################################");
	}

	// @Test
	public void findAllAsJsonLimit() {
		String result = virtualRepo.findAllAsJson("OpenPlatform", 1);
		System.out.println("############################################################################");
		System.out.println(result);
		System.out.println("############################################################################");
	}

	// @Test
	public void findAll() {
		List<String> result = virtualRepo.findAll("OpenPlatform");
		System.out.println("############################################################################");
		System.out.println(result);
		System.out.println("############################################################################");
	}

	// @Test
	public void findAllLimit() {
		List<String> result = virtualRepo.findAll("OpenPlatform", 1);
		System.out.println("############################################################################");
		System.out.println(result);
		System.out.println("############################################################################");
	}

	// @Test
	public void countNative() {
		long result = virtualRepo.countNative("OpenPlatform", "SELECT COUNT(*) FROM OpenPlatform");
		System.out.println("############################################################################");
		System.out.println(result);
		System.out.println("############################################################################");
	}

	// @Test
	public void testUpdate() {
		virtualRepo.updateNative("OpenPlatform", "UPDATE OpenPlatform set column1='Actualizado' WHERE column3=3",
				false);
	}

	@Test
	public void testDelete() {
		virtualRepo.delete("OpenPlatform", false);
	}

	// @Test
	public void testQuery() {
		List<String> lResult = virtualRepo.queryNative("OpenPlatform",
				"SELECT * FROM OpenPlatform WHERE column1='string 1' AND ROWNUM = 200 ");
		for (String result : lResult) {
			System.out.println(result);
		}
	}

	public void testQuery2() {
		String result = virtualRepo.queryNativeAsJson("OpenPlatform",
				"SELECT * FROM OpenPlatform WHERE column1='string 1' AND ROWNUM = 200 ");

		System.out.println(result);
	}

}
