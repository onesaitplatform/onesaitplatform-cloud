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
import com.minsait.onesait.platform.config.model.Gadget;
import com.minsait.onesait.platform.config.model.User;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
@Category(IntegrationTest.class)
public class GadgetRepositoryIntegrationTest {

	@Autowired
	GadgetRepository repository;
	@Autowired
	UserRepository userRepository;
	@Autowired
	private GadgetTemplateRepository gadgetTemplateRepository;

	private User getUserCollaborator() {
		return this.userRepository.findByUserId("collaborator");
	}

	@Before
	public void setUp() {
		List<Gadget> gadgets = this.repository.findAll();
		if (gadgets.isEmpty()) {
			log.info("No gadgets ...");
			Gadget gadget = new Gadget();
			gadget.setUser(getUserCollaborator());
			gadget.setPublic(true);
			gadget.setIdentification("Gadget1");
			gadget.setType(gadgetTemplateRepository.findById("line").orElse(null));

			repository.save(gadget);
		}
	}

	@Test
	@Transactional
	public void given_SomeGadgetsExist_When_TheyAreSearchedByUserAndType_Then_TheCorrectObjectIsObtained() {
		Gadget gadget = this.repository.findAll().get(0);
		Assert.assertTrue(this.repository.findByUserAndType(gadget.getUser(), gadget.getType().getId()).size() > 0);
	}

}
