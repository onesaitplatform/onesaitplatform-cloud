/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2022 SPAIN
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

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.minsait.onesait.platform.commons.testing.IntegrationTest;
import com.minsait.onesait.platform.config.model.Dashboard;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
@Category(IntegrationTest.class)
public class DashboardRepositoryIntegrationTest {

	@Autowired
	DashboardRepository repository;

	@Autowired
	UserRepository userRep;

	@Before
	public void setUp() {
		List<Dashboard> dashboards = this.repository.findAll();
		if (dashboards.isEmpty()) {
			log.info("No dashboards...adding");
			Dashboard dashboard = new Dashboard();
			dashboard.setModel("Modelo 1");
			dashboard.setUser(this.userRep.findByUserId("collaborator"));
			dashboard.setIdentification("Nombre del modelo 1");
			repository.save(dashboard);
		}

	}

	@Test
	@Transactional
	public void given_SomeDashboardsExist_When_AllAreSearched_Then_AllAreReturned() {
		Dashboard d = this.repository.findAll().get(0);
		Assert.assertTrue(this.repository.countByIdentification(d.getIdentification()) == 1L);
	}

	@Test
	@Transactional
	public void given_SomeDashboardsExist_When_ItIsSearchedByName_TheCorrectObjectIsReturned() {
		Dashboard d = this.repository.findAll().get(0);
		Assert.assertTrue(this.repository.findByIdentification(d.getIdentification()).size() > 0);

	}

}
