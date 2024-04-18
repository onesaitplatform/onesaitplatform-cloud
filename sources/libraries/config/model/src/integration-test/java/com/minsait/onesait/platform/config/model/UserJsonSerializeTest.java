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
package com.minsait.onesait.platform.config.model;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@DataJpaTest
public class UserJsonSerializeTest {

	@Test
	@Transactional
	public void givenBidirectionRelation_whenUsingJacksonReferenceAnnotation_thenCorrect()
			throws JsonProcessingException {
		final User user = new User();
		user.setUserId("administrator");
		final Project project = new Project();
		project.setId("1");
		final Set<User> users = new HashSet<>();
		users.add(user);
		project.setUser(user);
		project.setUsers(users);

		final Set<Project> projects = new HashSet<>();
		projects.add(project);
		user.setProjects(projects);

		final String serialization = new ObjectMapper().writeValueAsString(user);
		final String s = new ObjectMapper().writeValueAsString(project);
	}

}
