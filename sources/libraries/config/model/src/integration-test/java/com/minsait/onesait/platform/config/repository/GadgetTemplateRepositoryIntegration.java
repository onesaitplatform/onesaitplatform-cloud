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

import org.junit.After;
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
import com.minsait.onesait.platform.config.model.GadgetTemplate;
import com.minsait.onesait.platform.config.model.User;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
@Category(IntegrationTest.class)
public class GadgetTemplateRepositoryIntegration {

	@Autowired
	GadgetTemplateRepository repository;
	@Autowired
	UserRepository userRepository;

	private User getUserCollaborator() {
		return this.userRepository.findByUserId("developer");
	}

	@Before
	public void setUp() {
		GadgetTemplate gadgetTemplate = new GadgetTemplate();
		gadgetTemplate.setUser(getUserCollaborator());
		gadgetTemplate.setPublic(true);
		gadgetTemplate.setIdentification("GadgetTemplateTest000");
		gadgetTemplate.setDescription("Template Test");
		gadgetTemplate.setTemplate("<label>Test</label>");
		repository.save(gadgetTemplate);
	}

	@After
	public void tearDown() {
		GadgetTemplate gadgetTemplate = repository.findByIdentification("GadgetTemplateTest000");
		repository.delete(gadgetTemplate);

	}

	@Test
	@Transactional
	public void given_SomeGadgetsTemplateExist_When_TheyAreSearchedByUser_Then_TheCorrectObjectIsObtained() {
		GadgetTemplate gadgetTemplate = repository.findByIdentification("GadgetTemplateTest000");
		Assert.assertTrue(this.repository.findByUser(gadgetTemplate.getUser()).size() > 0);
	}

	@Test
	@Transactional
	public void given_SomeGadgetsTemplateExist_When_TheyAreSearchedByID_Then_TheCorrectObjectIsObtained() {
		GadgetTemplate gadgetTemplate = repository.findByIdentification("GadgetTemplateTest000");
		Assert.assertTrue(this.repository.findById(gadgetTemplate.getId()) != null);
	}

	@Test
	@Transactional
	public void given_SomeGadgetsTemplateExist_When_TheyArefindByUserAndIdentificationContaining_Then_TheCorrectObjectIsObtained() {
		GadgetTemplate gadgetTemplate = repository.findByIdentification("GadgetTemplateTest000");
		Assert.assertTrue(this.repository
				.findByUserAndIdentificationContaining(gadgetTemplate.getUser(), gadgetTemplate.getIdentification())
				.size() > 0);
	}

	@Test
	@Transactional
	public void given_SomeGadgetsTemplateExist_When_TheyArefindByUserAndDescriptionContaining_Then_TheCorrectObjectIsObtained() {
		GadgetTemplate gadgetTemplate = repository.findByIdentification("GadgetTemplateTest000");
		Assert.assertTrue(this.repository
				.findByUserAndDescriptionContaining(gadgetTemplate.getUser(), gadgetTemplate.getDescription())
				.size() > 0);
	}

}
