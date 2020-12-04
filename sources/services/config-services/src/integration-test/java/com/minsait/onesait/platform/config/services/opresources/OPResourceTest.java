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
package com.minsait.onesait.platform.config.services.opresources;

import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

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
import com.minsait.onesait.platform.config.model.Project;
import com.minsait.onesait.platform.config.model.Project.ProjectType;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.repository.AppRepository;
import com.minsait.onesait.platform.config.repository.ProjectRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;

@Category(IntegrationTest.class)
@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OPResourceTest {

	@Autowired
	private OPResourceService resourceService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private AppRepository appRepository;
	@Autowired
	private ProjectRepository projectRepository;

	@Transactional
	@Test
	public void whenAssigningProjectResourceToAppUser_ThenTheUserHasAccessToThatResource() {
		final User user = userRepository.findByUserId("developer");

		final Project project = new Project();
		project.setDescription("Example project");
		project.setIdentification("THis is the project name");
		project.setType(ProjectType.ENGINE);
		project.setUser(user);
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
		final OPResource resource = ((Set<OPResource>) resourceService.getResources("developer", "")).iterator().next();
		final ProjectResourceAccess pra = new ProjectResourceAccess(null, ResourceAccessType.VIEW, resource, pdb,
				realm.getAppRoles().iterator().next());
		pdb.getProjectResourceAccesses().add(pra);
		pdb = projectRepository.save(pdb);
		Assert.assertTrue(!resourceService.hasAccess(user.getUserId(), resource.getId(), ResourceAccessType.MANAGE));
		Assert.assertTrue(resourceService.hasAccess(user.getUserId(), resource.getId(), ResourceAccessType.VIEW));
		Assert.assertTrue(!resourceService.getResourceAccess(user.getUserId(), resource.getId())
				.equals(ResourceAccessType.MANAGE));
		realm.setProject(null);
		pdb.setApp(null);
		appRepository.delete(realm);
		projectRepository.delete(pdb);

	}

}
