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
import com.minsait.onesait.platform.config.model.DataModel;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Category(IntegrationTest.class)
public class DataModelRepositoryIntegrationTest {

	@Autowired
	DataModelRepository repository;
	@Autowired
	UserRepository userRepository;

	@Before
	public void setUp() {
		List<DataModel> dataModels = this.repository.findAll();
		if (dataModels.isEmpty()) {
			throw new RuntimeException(
					"There must be DataModels loaded in your dastabase. Please execute systemconfig-init");
		}
	}

	@Test
	@Transactional
	public void given_SomeDataModelsExist_When_TheirNumberIsRequested_Then_TheCorrectNumberIsObtained() {
		Assert.assertTrue(this.repository.count() > 0);
	}

	@Test
	@Transactional
	public void given_SomeDataModelsExist_When_TheyAreSearchedByType_Then_TheCorrectObjectsAreObtained() {
		Assert.assertTrue(this.repository.findByType("IoT").size() > 1L);
	}

}
