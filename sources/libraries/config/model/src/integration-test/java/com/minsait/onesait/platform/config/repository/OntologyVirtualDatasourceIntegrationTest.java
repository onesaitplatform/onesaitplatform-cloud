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
package com.minsait.onesait.platform.config.repository;

import java.util.Date;

import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.minsait.onesait.platform.commons.testing.IntegrationTest;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource.VirtualDatasourceType;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
@Category(IntegrationTest.class)
@Ignore("Pendiente resolver")
public class OntologyVirtualDatasourceIntegrationTest {

	@Autowired
	private OntologyVirtualDatasourceRepository repository;

	@Test
	@Transactional
	public void addDatasource() {
		OntologyVirtualDatasource data = new OntologyVirtualDatasource();
		data.setCreatedAt(new Date());
		data.setUpdatedAt(new Date());
		data.setUser("sys as sysdba");
		data.setCredentials("indra2013");
		data.setPoolSize("10");
		data.setDatasourceName("oracle");
		data.setSgdb(VirtualDatasourceType.ORACLE);
		data.setQueryLimit(100);
		data.setConnectionString("jdbc:oracle:thin:@10.0.0.6:1521:XE");

		this.repository.saveAndFlush(data);

	}

}
