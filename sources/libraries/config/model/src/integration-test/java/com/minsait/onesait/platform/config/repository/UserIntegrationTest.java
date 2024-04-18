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
import com.minsait.onesait.platform.config.model.User;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Category(IntegrationTest.class)
public class UserIntegrationTest {

	@Autowired
	UserRepository repository;

	@Autowired
	RoleRepository roleRepository;

	@Before
	public void setUp() {
		List<User> types = this.repository.findAll();
		if (types.isEmpty()) {
			// log.info("No types en tabla.Adding...");
			throw new RuntimeException("No types en Users...");
		}
	}

	@Test
	@Transactional
	public void given_SomeUsersExist_When_TheyAreCounted_Then_TheCorrectNumberIsObtained() {
		Assert.assertTrue(this.repository.count() > 6);
	}

	@Test
	@Transactional
	public void given_SomeUsersExist_When_TheyAreSearchedByUsersThatAreNotAdministrator_Then_TheCorrectUsersAreReturned() {
		Assert.assertTrue(this.repository.findUsersNoAdmin().size() > 5);
	}

	@Test
	@Transactional
	public void given_SomeUsersExist_When_TheyAreSearchedByEmail_Then_TheCorrectUsersAreReturned() {
		Assert.assertTrue(this.repository.findByEmail("administrator@onesaitplatform.com").size() == 1);
	}

	@Test
	@Transactional
	public void given_ANumberOfUsers_When_OneUserIsCreatedAndThenDeleted_Then_TheNumberOfUsersIsTheSame() {
		long count = this.repository.count();
		User type = new User();
		type.setUserId("lmgracia1");
		type.setPassword("changeIt!");
		type.setFullName("Luis Miguel Gracia");
		type.setEmail("lmgracia@onesaitplatform.com");
		type.setActive(true);
		type.setRole(this.roleRepository.findById(Role.Type.ROLE_DEVELOPER.toString()));
		repository.save(type);
		Assert.assertTrue(this.repository.count() == count + 1);
		repository.delete(type);
		Assert.assertTrue(this.repository.count() == count);

	}

}
