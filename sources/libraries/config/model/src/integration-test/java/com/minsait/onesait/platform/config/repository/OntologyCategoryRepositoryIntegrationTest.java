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
import com.minsait.onesait.platform.config.model.OntologyCategory;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
@Category(IntegrationTest.class)
public class OntologyCategoryRepositoryIntegrationTest {

	@Autowired
	OntologyCategoryRepository repository;

	@Before
	public void setUp() {
		List<OntologyCategory> categories = this.repository.findAll();
		if (categories.isEmpty()) {
			log.info("No ontology categories found..adding");
			OntologyCategory category = new OntologyCategory();
			category.setId("MASTER-Ontology-Category-1");
			category.setIdentification("ontologias_categoria_cultura");
			category.setDescription("ontologias_categoria_cultura_desc");
			this.repository.save(category);
		}
	}

	@Test
	@Transactional
	public void given_SomeOntologyCategoriesExist_When_ItIsSearchedById_Then_TheCorrectObjectIsObtained() {
		OntologyCategory category = this.repository.findAll().get(0);
		Assert.assertTrue(this.repository.findById(category.getId()) != null);
	}
}
