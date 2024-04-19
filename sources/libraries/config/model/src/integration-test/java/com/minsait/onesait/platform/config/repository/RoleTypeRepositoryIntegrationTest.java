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

import java.util.ArrayList;
import java.util.Arrays;
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
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.Role.Type;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Category(IntegrationTest.class)
public class RoleTypeRepositoryIntegrationTest {

	@Autowired
	RoleRepository repository;

	@Before
	public void setUp() {
		final List<Type> types = new ArrayList<>(Arrays.asList(Role.Type.values()));
		if (types.isEmpty()) {
			// log.info("No types en tabla.Adding...");
			throw new RuntimeException("No role types in class Role...");
		}
	}

	@Test
	@Transactional
	public void given_SomeRoleTypesExist_When_TheyAreCounted_Then_TheCorrectNumberIsObtained() {
		Assert.assertTrue(repository.findAll().size() == 9);
		Assert.assertTrue(repository.count() == 9);
	}

	@Test
	@Transactional
	public void given_SomeRoleTypesExist_When_TheyAreCountedById_Then_OneIsReturned() {
		Assert.assertTrue(repository.countById("ROLE_ADMINISTRATOR") == 1L);
	}

	@Test
	@Transactional
	public void given_SomeRoleTypesExist_When_ItIsSearchedByName_Then_TheCorrectObjectIsObtained() {
		Assert.assertTrue(repository.findById("ROLE_ADMINISTRATOR").isPresent());
	}

}
