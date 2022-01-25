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
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.User;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
@Category(IntegrationTest.class)
public class OntologyRepositoryIntegrationTest {

	@Autowired
	OntologyRepository repository;
	@Autowired
	UserRepository userRepository;

	private User getUserCollaborator() {
		return this.userRepository.findByUserId("collaborator");
	}

	@Before
	public void setUp() {
		List<Ontology> ontologies = this.repository.findAll();
		if (ontologies.isEmpty()) {
			log.info("No ontologies..adding");
			Ontology ontology = new Ontology();
			ontology.setJsonSchema("{}");
			ontology.setIdentification("Id 1");
			ontology.setDescription("Description");
			ontology.setActive(true);
			ontology.setRtdbClean(true);
			ontology.setPublic(true);
			ontology.setUser(getUserCollaborator());
			repository.save(ontology);

			ontology = new Ontology();
			ontology.setJsonSchema("{Data:,Temperature:}");
			ontology.setDescription("Description");
			ontology.setIdentification("Id 2");
			ontology.setActive(true);
			ontology.setRtdbClean(true);
			ontology.setPublic(true);
			ontology.setUser(getUserCollaborator());
			repository.save(ontology);

		}
	}

	@Test
	@Transactional
	public void given_SomeOntologiesExist_WhenItIsSearchedByIdentifycationAndDescriptionUsingLike_Then_TheCorrectObjectIsReturned() {
		Ontology o = this.repository.findAll().get(0);
		o.isActive();
		Assert.assertTrue(this.repository
				.findByIdentificationLikeAndDescriptionLikeAndActiveTrue(o.getIdentification(), o.getDescription())
				.size() > 0);
	}

	public void countByIsActiveTrueAndIsPublicTrue() {
		Assert.assertTrue(this.repository.countByActiveTrueAndIsPublicTrue() == 1L);

	}
}
