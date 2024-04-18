/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2022 SPAIN
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
package com.minsait.onesait.platform.controlpanel.controller.ontology;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.minsait.onesait.platform.business.services.ontology.OntologyBusinessService;
import com.minsait.onesait.platform.config.model.DataModel;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.OntologyUserAccess;
import com.minsait.onesait.platform.config.model.OntologyUserAccessType;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.app.AppService;
import com.minsait.onesait.platform.config.services.deletion.EntityDeletionService;
import com.minsait.onesait.platform.config.services.exceptions.OntologyServiceException;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

@RunWith(MockitoJUnitRunner.Silent.class)
public class OntologyControllerTest {

	@Mock
	private OntologyService ontologyService;
	@Mock
	private OntologyBusinessService ontologyBusinessService;
	@Mock
	private AppWebUtils utils;
	@Mock
	private UserService userService;
	@Mock
	private EntityDeletionService entityDeletionService;
	@Mock
	private AppService appService;

	@InjectMocks
	private OntologyController ontologyController;

	private MockMvc mockMvc;

	@Before
	public void setup() {
		// Setup Spring test in standalone mode
		mockMvc = MockMvcBuilders.standaloneSetup(ontologyController).build();
	}

	@Test
	public void given_OneOntology_When_AnIncorrectIdIsSentToDeleteOne_TheViewIsRedirectToSamePage() throws Exception {
		final Ontology ontology = ontologyCreator("ontologyId", "userOntology");

		final String sessionUserId = "userOntology";

		given(ontologyService.getOntologyById(ontology.getId(), sessionUserId)).willReturn(ontology);
		doThrow(new RuntimeException()).when(ontologyBusinessService).deleteOntology(ontology.getId(), sessionUserId);

		given(utils.getUserId()).willReturn(sessionUserId);
		given(utils.isAdministrator()).willReturn(false);

		mockMvc.perform(delete("/ontologies/" + ontology.getId()))
				.andExpect(redirectedUrl("/ontologies/update/" + ontology.getId()));
	}

	@Test
	public void given_OneOntology_When_CorrectParamentersAreSentToDelete_Then_TheOntologyIsDeleted() throws Exception {
		final String sessionUserId = "userOntology";
		final Ontology ontology = ontologyCreator("ontologyId", sessionUserId);

		given(utils.getUserId()).willReturn(sessionUserId);
		given(utils.isAdministrator()).willReturn(false);

		given(ontologyService.getOntologyById(ontology.getId(), sessionUserId)).willReturn(ontology);
		doNothing().when(ontologyBusinessService).deleteOntology(ontology.getId(), sessionUserId);

		mockMvc.perform(delete("/ontologies/" + ontology.getId())).andExpect(redirectedUrl("/ontologies/list"));
	}

	@Test
	public void given_OneOntology_When_OneUserWithoutAuthorizationWantsToUpdateIt_Then_TheViewIsRedirectToCreate()
			throws Exception {
		final Ontology ontology = ontologyCreator("ontologyId", "userOntology");
		final String sessionUserId = "unknownUser";
		given(ontologyService.getOntologyById(ontology.getId(), sessionUserId))
				.willThrow(new OntologyServiceException("The user is not authorizated"));

		given(utils.getUserId()).willReturn(sessionUserId);
		given(utils.isAdministrator()).willReturn(false);

		mockMvc.perform(get("/ontologies/update/" + ontology.getId())).andExpect(view().name("ontologies/create"));
	}

	@Test
	public void given_OneOntology_IfInvalidIdIsSentToUpdate_Then_TheViewShowedIsForCreation() throws Exception {
		final String id = "invalidOntologyId";
		final String sessionUserId = "unknownUser";
		given(ontologyService.getOntologyById(id, sessionUserId)).willReturn(null);

		mockMvc.perform(get("/ontologies/update/" + id)).andExpect(view().name("ontologies/create"));
	}

	@Test
	public void given_OneOntology_WhenCorrectParementersAreSentToUpdate_TheCreateWizardViewIsShowedWithTheCorrectParameters()
			throws Exception {

		final Ontology ontology = ontologyCreator("ontologyId", "userOntology");

		final List<User> users = createUsers();

		final List<OntologyUserAccess> accesses = createAccesses();

		final String sessionUserId = "userOntology";

		given(ontologyService.getOntologyById(ontology.getId(), sessionUserId)).willReturn(ontology);
		given(ontologyService.getOntologyUserAccesses(ontology.getId(), sessionUserId)).willReturn(accesses);
		given(userService.getAllActiveUsers()).willReturn(users);
		given(appService.getAppsByUser(ontology.getUser().getUserId(), null)).willReturn(new ArrayList<>());

		given(utils.getUserId()).willReturn(sessionUserId);
		given(utils.isAdministrator()).willReturn(false);

		mockMvc.perform(get("/ontologies/update/" + ontology.getId())).andExpect(status().isOk())
				.andExpect(view().name("ontologies/createwizard")).andExpect(model().attribute("ontology", ontology))
				.andExpect(model().attribute("users", users))
				// authorizations is serialized using OntologyUserAccessDTO, to check the
				// content of
				// this attribute, it would be necessary to implement a custom
				// Matcher<OntologyUserAccess>
				.andExpect(model().attributeExists("authorizations"));
	}

	@Test
	public void given_OneOntology_When_OneUserWithoutAuthorizationWantsToViewDetails_Then_TheListViewIsServed()
			throws Exception {
		final Ontology ontology = ontologyCreator("ontologyId", "userOntology");

		final String sessionUserId = "unknownUser";

		given(ontologyService.getOntologyById(ontology.getId(), sessionUserId))
				.willThrow(new OntologyServiceException("The user is not authorized"));

		given(utils.getUserId()).willReturn(sessionUserId);
		given(utils.isAdministrator()).willReturn(false);

		mockMvc.perform(get("/ontologies/show/" + ontology.getId())).andExpect(redirectedUrl("/ontologies/list"));
	}

	@Test
	public void given_AnyState_When_AnInvalidIdIsProvidedToShowOntologyDetails_Then_TheListViewIsServed()
			throws Exception {
		final String id = "invalidOntologyId";

		final String sessionUserId = "unknownUser";

		given(ontologyService.getOntologyById(id, sessionUserId)).willReturn(null);

		mockMvc.perform(get("/ontologies/show/" + id)).andExpect(redirectedUrl("/ontologies/list"));
	}

	@Test
	public void given_OneOntology_When_CorrectParametersAreProvided_Then_TheShowViewIsServedWithTheCorrectAttributes()
			throws Exception {

		final Ontology ontology = ontologyCreator("ontologyId", "userOntology");

		final List<User> users = createUsers();

		final List<OntologyUserAccess> accesses = createAccesses();

		final String sessionUserId = "userOntology";

		given(ontologyService.getOntologyById(ontology.getId(), sessionUserId)).willReturn(ontology);
		given(ontologyService.getOntologyUserAccesses(ontology.getId(), sessionUserId)).willReturn(accesses);
		given(userService.getAllActiveUsers()).willReturn(users);

		given(utils.getUserId()).willReturn(sessionUserId);
		given(utils.isAdministrator()).willReturn(false);

		mockMvc.perform(get("/ontologies/show/" + ontology.getId())).andExpect(status().isOk())
				.andExpect(view().name("ontologies/show")).andExpect(model().attribute("ontology", ontology))
				.andExpect(model().attribute("users", users))
				// authorizations is serialized using OntologyUserAccessDTO, to check the
				// content of
				// this attribute, it would be necessary to implement a custom
				// Matcher<OntologyUserAccess>
				.andExpect(model().attributeExists("authorizations"));
	}

	@Test
	public void given_OneOntology_When_OneNotAuthorizedUserWantsToCreateAUserAccess_Then_TheUserAccessIsNotCreatedAndABadRequestIsResponsed()
			throws Exception {
		final OntologyUserAccess access = ontologyUserAccessCreator("ontologyId", "userOntology", "user", "ALL",
				"accessId");
		final String sessionUserId = "unknown";

		given(utils.getUserId()).willReturn(sessionUserId);
		given(utils.isAdministrator()).willReturn(false);

		doThrow(new OntologyServiceException("The user is not authorized")).when(ontologyService).createUserAccess(
				access.getOntology().getId(), access.getUser().getUserId(),
				access.getOntologyUserAccessType().getName(), sessionUserId);

		mockMvc.perform(
				post("/ontologies/authorization").param("accesstype", access.getOntologyUserAccessType().getName())
						.param("ontology", access.getOntology().getId()).param("user", access.getUser().getUserId()))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void given_OneOntologyWithUserAccess_When_CorrectParamentersAreSentToDeleteTheUserAccess_Then_TheUserAccessIsDeletedAndAStatusIsResponsed()
			throws Exception {

		final OntologyUserAccess access = ontologyUserAccessCreator("ontologyId", "userOntology", "user", "ALL",
				"accessId");

		final String sessionUserId = "userOntology";

		given(utils.getUserId()).willReturn(sessionUserId);
		given(utils.isAdministrator()).willReturn(false);

		given(ontologyService.getOntologyUserAccessById(access.getId(), sessionUserId)).willReturn(access);

		mockMvc.perform(post("/ontologies/authorization/delete").param("id", access.getId())).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.status", is("ok")));
	}

	@Test
	public void given_OneOnotologyWithUserAccess_When_OneUserWithoutAuthorizationWantsToDeleteTheUserAccess_Then_TheUserAccessIsNotDeletedAndABadRequestIsResponsed()
			throws Exception {

		final OntologyUserAccess access = ontologyUserAccessCreator("ontologyId", "userOntology", "user", "ALL",
				"accessId");

		final String sessionUserId = "unknown";

		given(utils.getUserId()).willReturn(sessionUserId);
		given(utils.isAdministrator()).willReturn(false);

		doThrow(new OntologyServiceException("The user is not authorized")).when(ontologyService)
				.deleteOntologyUserAccess(access.getId(), sessionUserId);

		mockMvc.perform(post("/ontologies/authorization/delete").param("id", access.getId()))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void given_OneOntologyWithUserAccess_When_CorrectParametersAreSentToUpdate_Then_TheOntologyUserAccessIsUpdatedAndTheNewValuesAreReturnedAsJSON()
			throws Exception {
		final OntologyUserAccess accessOld = ontologyUserAccessCreator("ontologyId", "userOntology", "user", "ALL",
				"accessId");
		final OntologyUserAccess accessNew = ontologyUserAccessCreator("ontologyId", "userOntology", "user", "QUERY",
				"accessId");

		final String sessionUserId = "userOntology";

		given(utils.getUserId()).willReturn(sessionUserId);
		given(utils.isAdministrator()).willReturn(false);

		given(ontologyService.getOntologyUserAccessById(accessOld.getId(), sessionUserId)).willReturn(accessNew);

		mockMvc.perform(post("/ontologies/authorization/update").param("id", accessOld.getId()).param("accesstype",
				accessOld.getOntologyUserAccessType().getName())).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.id", is(accessNew.getId())))
				.andExpect(jsonPath("$.userId", is(accessNew.getUser().getUserId())))
				.andExpect(jsonPath("$.typeName", is(accessNew.getOntologyUserAccessType().getName())));
	}

	@Test
	public void given_OneOntologyWithUserAccess_When_OneUserWithAuthorizationsWantsToUpdateTheUserAccess_Then_TheUpdateIsNotPerformedAndABadRequestIsResponsed()
			throws Exception {
		final OntologyUserAccess access = ontologyUserAccessCreator("ontologyId", "userOntology", "user", "ALL",
				"accessId");

		final String sessionUserId = "unknown";

		given(utils.getUserId()).willReturn(sessionUserId);
		given(utils.isAdministrator()).willReturn(false);

		doThrow(new OntologyServiceException("The user is not authorizated")).when(ontologyService)
				.updateOntologyUserAccess(access.getUser().getUserId(), "QUERY", sessionUserId);

		mockMvc.perform(post("/ontologies/authorization/update").param("id", access.getId()).param("accesstype",
				access.getOntologyUserAccessType().getName())).andExpect(status().isBadRequest());
	}

	@Test
	public void given_OneOntologyWithTwoUserAccesses_When_CorrectParametersAreSentToListUserAccesses_Then_TheTwoUserAccessesAreResponsedAsAJSONArray()
			throws Exception {
		final Ontology ontology = ontologyCreator("ontologyId", "userOntology");

		final List<OntologyUserAccess> accesses = createAccesses();

		final String sessionUserId = "userOntology";

		given(ontologyService.getOntologyById(ontology.getId(), sessionUserId)).willReturn(ontology);
		given(ontologyService.getOntologyUserAccesses(ontology.getId(), sessionUserId)).willReturn(accesses);

		given(utils.getUserId()).willReturn(sessionUserId);
		given(utils.isAdministrator()).willReturn(false);

		mockMvc.perform(get("/ontologies/authorization").param("id", ontology.getId())).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].id", is(accesses.get(0).getId())))
				.andExpect(jsonPath("$[0].userId", is(accesses.get(0).getUser().getUserId())))
				.andExpect(jsonPath("$[0].typeName", is(accesses.get(0).getOntologyUserAccessType().getName())))
				.andExpect(jsonPath("$[0].ontologyId", is(accesses.get(0).getOntology().getId())))
				.andExpect(jsonPath("$[1].id", is(accesses.get(1).getId())))
				.andExpect(jsonPath("$[1].userId", is(accesses.get(1).getUser().getUserId())))
				.andExpect(jsonPath("$[1].typeName", is(accesses.get(1).getOntologyUserAccessType().getName())))
				.andExpect(jsonPath("$[1].ontologyId", is(accesses.get(1).getOntology().getId())));
	}

	@Test
	public void given_OneOntology_When_OneUserWithoutAuthorizationWantsToObtainTheUserAccesses_Then_ABadRequestIsResponsed()
			throws Exception {
		final Ontology ontology = ontologyCreator("ontologyId", "userOntology");

		final String sessionUserId = "unknown";

		given(ontologyService.getOntologyById(ontology.getId(), sessionUserId))
				.willThrow(new OntologyServiceException("The user is not authorized"));

		given(utils.getUserId()).willReturn(sessionUserId);
		given(utils.isAdministrator()).willReturn(false);

		mockMvc.perform(get("/ontologies/authorization").param("id", ontology.getId()))
				.andExpect(status().isBadRequest());
	}

	private Ontology ontologyCreator(String ontologyId, String userId) {
		final User userOntologyOwner = new User();
		userOntologyOwner.setUserId(userId);
		userOntologyOwner.setActive(true);

		final Ontology ontology = new Ontology();
		ontology.setId(ontologyId);
		ontology.setUser(userOntologyOwner);
		final DataModel datamodel = new DataModel();
		datamodel.setId("MISCELANEOUS");
		ontology.setDataModel(datamodel);

		return ontology;
	}

	private OntologyUserAccess ontologyUserAccessCreator(String ontologyId, String userOntologyOwnerId,
			String userIdToGiveAccessId, String accessTypeName, String accessId) {

		final User userAuthorized = new User();
		userAuthorized.setUserId(userIdToGiveAccessId);
		userAuthorized.setActive(true);

		final OntologyUserAccessType type = new OntologyUserAccessType();
		type.setName(accessTypeName);

		final OntologyUserAccess access = new OntologyUserAccess();
		access.setId(accessId);
		access.setOntology(ontologyCreator(ontologyId, userOntologyOwnerId));
		access.setUser(userAuthorized);
		access.setOntologyUserAccessType(type);

		return access;
	}

	private List<OntologyUserAccess> createAccesses() {
		final List<OntologyUserAccess> accesses = new ArrayList<>(2);
		final OntologyUserAccess access1 = ontologyUserAccessCreator("ontologyId", "userOntology", "user1", "ALL",
				"accessId1");
		final OntologyUserAccess access2 = ontologyUserAccessCreator("ontologyId", "userOntology", "user2", "ALL",
				"accessId2");
		accesses.add(access1);
		accesses.add(access2);
		return accesses;
	}

	private List<User> createUsers() {
		final User administrator = new User();
		administrator.setUserId("administrador");
		administrator.setActive(true);
		final User user = new User();
		user.setUserId("user");
		user.setActive(true);
		final User developer = new User();
		developer.setActive(true);
		developer.setUserId("developer");
		final List<User> users = new ArrayList<>();
		users.add(administrator);
		users.add(user);
		users.add(developer);
		return users;
	}
}
