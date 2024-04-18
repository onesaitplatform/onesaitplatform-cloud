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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.minsait.onesait.platform.commons.testing.IntegrationTest;
import com.minsait.onesait.platform.config.model.App;
import com.minsait.onesait.platform.config.model.AppRole;
import com.minsait.onesait.platform.config.model.AppUser;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Project;
import com.minsait.onesait.platform.config.model.Project.ProjectType;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.base.OPResource;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Category(IntegrationTest.class)
public class OPResourceRepositoryTest {

	@Autowired
	private OPResourceRepository resourceRepository;
	@Autowired
	private ProjectRepository projectRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private AppRepository appRepository;
	@Autowired
	private AppRoleRepository appRoleRepository;

	@Test
	public void findAnyOPResource() {
		final List<OPResource> resources = resourceRepository.findAll();
		Assert.assertTrue(resources.size() > 0);
	}

	@Test
	public void findAnyOntologies() {
		final List<OPResource> resources = resourceRepository.findAll();
		final List<Ontology> ontologies = resources.stream().filter(r -> r instanceof Ontology).map(r -> (Ontology) r)
				.collect(Collectors.toList());

		Assert.assertTrue(ontologies.size() > 0);
	}

	@Test
	public void createProject_thenAssignResource_thenSaveToDB_thenDeleteResourceFromProject_andAssertResourceIsNotDeleted() {

		final OPResource resource = resourceRepository.findAll().get(0);
		final User user = userRepository.findByUserId("administrator");
		Project project = new Project();
		project.setDescription("Example project");
		project.setIdentification("THis is the project name");
		project.setType(ProjectType.ENGINE);
		project.setUser(user);

		project.getProjectResourceAccesses()
				.add(new ProjectResourceAccess(user, ResourceAccessType.MANAGE, resource, project, null, false));

		project = projectRepository.save(project);
		Assert.assertTrue(project.getProjectResourceAccesses().size() > 0);
		project.getProjectResourceAccesses().clear();
		project = projectRepository.save(project);
		Assert.assertTrue(project.getProjectResourceAccesses().size() == 0);
		Assert.assertTrue(resourceRepository.findById(resource.getId()).isPresent());

		projectRepository.delete(project);
		Assert.assertTrue(projectRepository.findByIdentification(project.getIdentification()).size() == 0);
	}

	@Test
	public void createProject_andAssignAllResources_thenDelete() {
		final User user = userRepository.findByUserId("administrator");

		final Project project = new Project();
		project.setDescription("Example project");
		project.setIdentification("THis is the project name");
		project.setType(ProjectType.ENGINE);
		project.setUser(user);
		final Set<ProjectResourceAccess> accesses = new HashSet<>();

		resourceRepository.findAll().stream().forEach(
				r -> accesses.add(new ProjectResourceAccess(user, ResourceAccessType.MANAGE, r, project, null, false)));

		project.getProjectResourceAccesses().addAll(accesses);
		projectRepository.save(project);
		projectRepository.delete(project);
		Assert.assertTrue(projectRepository.findByIdentification(project.getIdentification()).size() == 0);

	}

	@Test
	public void createRealm_thenAssignToProject_thenDelete() {
		final User user = userRepository.findByUserId("administrator");

		final Project project = new Project();
		project.setDescription("Example project");
		project.setIdentification("THis is the project name");
		project.setType(ProjectType.ENGINE);
		project.setUser(user);
		final Set<ProjectResourceAccess> accesses = new HashSet<>();

		resourceRepository.findAll().stream().forEach(
				r -> accesses.add(new ProjectResourceAccess(user, ResourceAccessType.MANAGE, r, project, null, false)));

		project.getProjectResourceAccesses().addAll(accesses);
		Project pdb = projectRepository.save(project);

		App realm = new App();
		realm.setIdentification("TestRealm");
		final AppRole role = new AppRole();
		role.setApp(realm);
		role.setName("DEVOPS");
		role.getAppUsers().addAll(userRepository.findAll().stream()
				.map(u -> AppUser.builder().user(u).role(role).build()).collect(Collectors.toSet()));
		realm.getAppRoles().add(role);
		realm = appRepository.save(realm);
		pdb.setApp(realm);
		realm.setProject(pdb);
		realm = appRepository.save(realm);
		pdb = projectRepository.save(pdb);
		pdb.getApp().getAppRoles().forEach(r -> {
			Assert.assertTrue(r.getAppUsers().size() > 0);
		});
		pdb.setApp(null);
		realm.setProject(null);
		realm.getAppRoles().clear();
		appRepository.delete(realm);
		projectRepository.delete(pdb);

	}

	@Test
	public void testAppRolesDelete() {
		App realm = new App();
		realm.setIdentification("TestRealm");
		final AppRole role = new AppRole();
		final User user = userRepository.findByUserId("administrator");
		role.getAppUsers().add(AppUser.builder().user(user).role(role).build());
		role.setName("DEVOPS");
		// role = appRoleRepository.save(role);
		realm.getAppRoles().add(role);
		role.setApp(realm);
		realm = appRepository.save(realm);
		realm.getAppRoles().clear();
		// appRoleRepository.delete(role);
		appRepository.delete(realm);

	}

	@Test
	public void findByIdentificationLike() {
		final String identification = "Ticket";
		final List<OPResource> resources = resourceRepository.findByIdentificationContainingIgnoreCase(identification);
		Assert.assertTrue(resources.size() >= 2);

		final OPResource ontology = resources.stream().filter(r -> (r instanceof Ontology))
				.filter(r -> r.getIdentification().equals("Ticket")).findFirst().orElse(null);
		Assert.assertNotNull(ontology);
		final OPResource client = resources.stream().filter(r -> (r instanceof ClientPlatform))
				.filter(r -> r.getIdentification().contains("Ticketing")).findFirst().orElse(null);
		Assert.assertNotNull(client);
	}
}
