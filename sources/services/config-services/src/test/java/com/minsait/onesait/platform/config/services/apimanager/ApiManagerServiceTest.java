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
package com.minsait.onesait.platform.config.services.apimanager;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Api.ApiStates;
import com.minsait.onesait.platform.config.model.Api.ApiType;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.UserApi;
import com.minsait.onesait.platform.config.repository.ApiRepository;
import com.minsait.onesait.platform.config.repository.UserApiRepository;
import com.minsait.onesait.platform.config.services.exceptions.ApiManagerServiceException;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.user.UserService;

@RunWith(MockitoJUnitRunner.class)
public class ApiManagerServiceTest {

	@Mock
	private ApiRepository apiRepository;
	@Mock
	private UserApiRepository userApiRepository;
	@Mock
	private OPResourceService resourceService;
	@Mock
	private UserService userService;

	@InjectMocks
	ApiManagerServiceImpl service;

	private static final String CREATED = "CREATED";
	private static final String DEVELOPMENT = "DEVELOPMENT";
	private static final String PUBLISHED = "PUBLISHED";
	private static final String DEPRECATED = "DEPRECATED";
	private static final String DELETED = "DELETED";
	private static final String FAKESTATE = "FAKESTATE";

	@Test
	public void given_IdentificaionAndApiType_Then_CalculateNumVersionCorrectly() {
		final String identification = "apiTest";
		final ApiType apiType = ApiType.INTERNAL_ONTOLOGY;

		final List<Api> existentApis = new ArrayList<>();

		when(apiRepository.findByIdentificationAndApiType(identification, apiType)).thenReturn(existentApis);
		assertTrue(service.calculateNumVersion(identification, apiType) == 1);

	}

	@Test
	public void given_NumVersionJson_Then_CalculateNumVersionCorrectly() {
		final String identification = "apiTest";
		final ApiType apiType = ApiType.INTERNAL_ONTOLOGY;
		final String rawJson = "{\"identification\":\"" + identification + "\",\"apiType\":\"" + apiType.toString()
				+ "\"}";

		final List<Api> existentApis = new ArrayList<>();

		when(apiRepository.findByIdentificationAndApiType(identification, apiType)).thenReturn(existentApis);
		assertTrue(service.calculateNumVersion(rawJson) == 1);

	}

	@Test
	public void given_IdentificaionAndApiType_When_ExistingApis_Then_CalculateNumVersionCorrectly() {
		final String identification = "apiTest";
		final ApiType apiType = ApiType.INTERNAL_ONTOLOGY;

		final Api api = new Api();
		api.setIdentification(identification);
		api.setNumversion(1);
		final List<Api> existentApis = new ArrayList<>();
		existentApis.add(api);

		when(apiRepository.findByIdentificationAndApiType(identification, apiType)).thenReturn(existentApis);
		assertTrue(service.calculateNumVersion(identification, apiType) == 2);

	}

	@Test
	public void given_NumVersionJson_When_ExistingApis_Then_CalculateNumVersionCorrectly() {
		final String identification = "apiTest";
		final ApiType apiType = ApiType.INTERNAL_ONTOLOGY;
		final String rawJson = "{\"identification\":\"" + identification + "\",\"apiType\":\"" + apiType.toString()
				+ "\"}";

		final Api api = new Api();
		api.setIdentification(identification);
		api.setNumversion(1);
		final List<Api> existentApis = new ArrayList<>();
		existentApis.add(api);

		when(apiRepository.findByIdentificationAndApiType(identification, apiType)).thenReturn(existentApis);
		assertTrue(service.calculateNumVersion(rawJson) == 2);

	}

	@Test
	public void given_ApiAndUser_When_ThereAreExistingApis_Then_LastVersionApiReturned() {
		final String apiId = "apiId";
		final String userId = "userId";
		final int lastVersion = 3;

		final User user = new User();
		user.setUserId(userId);

		Api api;
		final List<Api> existingApis = new ArrayList<>();
		for (int i = 1; i < lastVersion; i++) {
			api = new Api();
			api.setIdentification(apiId);
			api.setUser(user);
			api.setNumversion(i);
			existingApis.add(api);
		}

		final Api apiLast = new Api();
		apiLast.setIdentification(apiId);
		apiLast.setUser(user);
		apiLast.setNumversion(lastVersion);
		existingApis.add(apiLast);

		when(apiRepository.findByIdentificationAndUser(apiId, user)).thenReturn(existingApis);
		assertTrue(service.getLastVersionOfApiByApiAndUser(apiLast, user) == apiLast);
	}

	@Test
	public void given_ApiAndListApiOperationsAndListUserApiAndUser_VersionatedApiIsReturned() {
		final String apiId = "apiId";
		final String userId = "userId";
		final int lastVersion = 3;

		final User user = new User();
		user.setUserId(userId);

		final Ontology ontology = new Ontology();

		Api api;
		final List<Api> existingApis = new ArrayList<>();
		for (int i = 1; i < lastVersion; i++) {
			api = new Api();
			api.setIdentification(apiId);
			api.setUser(user);
			api.setNumversion(i);
			api.setOntology(ontology);
			existingApis.add(api);
		}

		final Api apiLast = new Api();
		apiLast.setIdentification(apiId);
		apiLast.setUser(user);
		apiLast.setNumversion(lastVersion);
		apiLast.setState(ApiStates.DEVELOPMENT);
		apiLast.setOntology(ontology);
		apiLast.setApiType(ApiType.EXTERNAL_FROM_JSON);
		existingApis.add(apiLast);

		final List<ApiOperation> operations = new ArrayList<>();
		final List<UserApi> authentications = new ArrayList<>();

		final Api expectedApi = new Api();
		expectedApi.setIdentification(apiId);
		expectedApi.setUser(user);
		expectedApi.setNumversion(lastVersion + 1);
		expectedApi.setState(ApiStates.DEVELOPMENT);
		expectedApi.setOntology(ontology);
		expectedApi.setApiType(ApiType.EXTERNAL_FROM_JSON);

		when(apiRepository.findByIdentificationAndUser(apiId, user)).thenReturn(existingApis);
		when(apiRepository.save(apiLast)).thenReturn(expectedApi);

		final Api returnedApi = service.versionateApiRest(apiLast, operations, authentications, user);
		assertTrue(returnedApi.getState() == expectedApi.getState());
		assertTrue(returnedApi.getNumversion() == expectedApi.getNumversion());

	}

	@Test
	public void given_ApiAndUserOrApiIdAndUserId_When_HasUserEditAccess_Then_TrueIsReturned() {
		final String apiId = "apiId";
		final String userId = "userId";

		final Role role = new Role();
		final User user = new User();
		user.setUserId(userId);
		final Api api = new Api();
		api.setId(apiId);

		role.setId(Role.Type.ROLE_ADMINISTRATOR.name());
		user.setRole(role);
		api.setUser(user);
		api.setPublic(false);
		api.setState(ApiStates.PUBLISHED);

		when(userService.getUser(userId)).thenReturn(user);
		when(apiRepository.findById(apiId)).thenReturn(Optional.ofNullable(api));
		when(resourceService.hasAccess(userId, apiId, ResourceAccessType.MANAGE)).thenReturn(false);
		assertTrue(service.hasUserEditAccess(api, user));
		assertTrue(service.hasUserEditAccess(api.getId(), user.getUserId()));

		role.setId(Role.Type.ROLE_DEVELOPER.name());
		user.setRole(role);
		api.setUser(user);
		api.setPublic(false);

		assertTrue(service.hasUserEditAccess(api, user));
		assertTrue(service.hasUserEditAccess(apiId, userId));

		role.setId(Role.Type.ROLE_DEVELOPER.name());
		user.setRole(role);
		api.setUser(null);
		api.setPublic(false);

		assertFalse(service.hasUserEditAccess(api, user));
		assertFalse(service.hasUserEditAccess(apiId, userId));

		role.setId(Role.Type.ROLE_DEVELOPER.name());
		user.setRole(role);
		api.setUser(null);
		api.setPublic(true);

		assertFalse(service.hasUserEditAccess(api, user));
		assertFalse(service.hasUserEditAccess(apiId, userId));
	}

	@Test
	public void given_ApiAndUserOrApiIdAndUserId_When_HasUserAccess_Then_TrueIsReturned() {
		final String apiId = "apiId";
		final String userId = "userId";

		final Role role = new Role();
		final User user = new User();
		user.setUserId(userId);
		final Api api = new Api();
		api.setId(apiId);

		role.setId(Role.Type.ROLE_ADMINISTRATOR.name());
		user.setRole(role);
		api.setUser(user);
		api.setPublic(false);
		api.setState(ApiStates.PUBLISHED);

		when(userService.getUser(userId)).thenReturn(user);
		when(apiRepository.findById(apiId)).thenReturn(Optional.ofNullable(api));
		when(userApiRepository.findByApiIdAndUser(apiId, userId)).thenReturn(null);
		assertTrue(service.hasUserAccess(api, user));
		assertTrue(service.hasUserAccess(api.getId(), user.getUserId()));

		role.setId(Role.Type.ROLE_DEVELOPER.name());
		user.setRole(role);
		api.setUser(user);
		api.setPublic(false);

		assertTrue(service.hasUserAccess(api, user));
		assertTrue(service.hasUserAccess(api.getId(), user.getUserId()));

		role.setId(Role.Type.ROLE_DEVELOPER.name());
		user.setRole(role);
		api.setUser(null);
		api.setPublic(false);

		assertFalse(service.hasUserAccess(api, user));
		assertFalse(service.hasUserAccess(api.getId(), user.getUserId()));

		role.setId(Role.Type.ROLE_DEVELOPER.name());
		user.setRole(role);
		api.setUser(null);
		api.setPublic(true);

		assertTrue(service.hasUserAccess(api, user));
		assertTrue(service.hasUserAccess(api.getId(), user.getUserId()));

	}

	@Test
	public void given_ApiOrApiId_When_IsApiStateValidForUserAccess_Then_TrueIsReturned() {
		final String apiId = "apiId";

		final Api api = new Api();
		api.setId(apiId);
		api.setState(ApiStates.CREATED);

		when(apiRepository.findById(apiId)).thenReturn(Optional.ofNullable(api));
		assertFalse(service.isApiStateValidForUserAccess(api));
		assertFalse(service.isApiStateValidForUserAccess(apiId));

		api.setState(ApiStates.DEVELOPMENT);

		assertTrue(service.isApiStateValidForUserAccess(api));
		assertTrue(service.isApiStateValidForUserAccess(apiId));

		api.setState(ApiStates.PUBLISHED);

		assertTrue(service.isApiStateValidForUserAccess(api));
		assertTrue(service.isApiStateValidForUserAccess(apiId));

		api.setState(ApiStates.DEPRECATED);

		assertTrue(service.isApiStateValidForUserAccess(api));
		assertTrue(service.isApiStateValidForUserAccess(apiId));

		api.setState(ApiStates.DELETED);

		assertFalse(service.isApiStateValidForEdit(api));
		assertFalse(service.isApiStateValidForEdit(apiId));

	}

	@Test
	public void given_ApiOrApiId_When_IsApiStateValidForEdit_Then_TrueIsReturned() {
		final String apiId = "apiId";

		final Api api = new Api();
		api.setId(apiId);
		api.setState(ApiStates.CREATED);

		when(apiRepository.findById(apiId)).thenReturn(Optional.ofNullable(api));
		assertTrue(service.isApiStateValidForEdit(api));
		assertTrue(service.isApiStateValidForEdit(apiId));

		api.setState(ApiStates.DEVELOPMENT);

		assertTrue(service.isApiStateValidForEdit(api));
		assertTrue(service.isApiStateValidForEdit(apiId));

		api.setState(ApiStates.PUBLISHED);

		assertFalse(service.isApiStateValidForEdit(api));
		assertFalse(service.isApiStateValidForEdit(apiId));

		api.setState(ApiStates.DEPRECATED);

		assertFalse(service.isApiStateValidForEdit(api));
		assertFalse(service.isApiStateValidForEdit(apiId));

		api.setState(ApiStates.DELETED);

		assertFalse(service.isApiStateValidForEdit(api));
		assertFalse(service.isApiStateValidForEdit(apiId));

	}

	@Test
	public void given_UserAndApi_When_IsOwnerOrAdmin_Then_TrueIsReturned() {
		final Role role = new Role();
		role.setId(Role.Type.ROLE_ADMINISTRATOR.name());

		final User user = new User();
		user.setRole(role);

		final Api api = new Api();
		api.setUser(user);

		assertTrue(service.isUserOwnerOrAdmin(user, api));

		role.setId(Role.Type.ROLE_DATASCIENTIST.name());
		user.setRole(role);
		api.setUser(user);

		assertTrue(service.isUserOwnerOrAdmin(user, api));

		role.setId(Role.Type.ROLE_ADMINISTRATOR.name());
		user.setRole(role);
		api.setUser(null);
		when(userService.isUserAdministrator(user)).thenReturn(true);

		assertTrue(service.isUserOwnerOrAdmin(user, api));

		when(userService.isUserAdministrator(user)).thenReturn(false);
		role.setId(Role.Type.ROLE_DATASCIENTIST.name());
		user.setRole(role);
		api.setUser(null);

		assertFalse(service.isUserOwnerOrAdmin(user, api));

	}

	@Test(expected = ApiManagerServiceException.class)
	public void given_ApiAndListApiOperationsAndListUserApiAndForcedVersion_When_OntologyIsNullAndInternalOntology_Then_ExceptionIsRaised() {
		final Api api = new Api();
		api.setOntology(null);
		api.setApiType(ApiType.INTERNAL_ONTOLOGY);

		service.createApiRest(api, new ArrayList<ApiOperation>(), new ArrayList<UserApi>(), 0);
	}

	@Test(expected = ApiManagerServiceException.class)
	public void given_ApiAndListApiOperationsAndListUserApiAndForcedVersion_When_OntologyIdentificatioIsnNull_Then_ExceptionIsRaised() {
		final Api api = new Api();
		api.setIdentification(null);
		api.setApiType(ApiType.EXTERNAL_FROM_JSON);

		service.createApiRest(api, new ArrayList<ApiOperation>(), new ArrayList<UserApi>(), 0);
	}

	@Test(expected = ApiManagerServiceException.class)
	public void given_ApiAndListApiOperationsAndListUserApiAndForcedVersion_When_OntologyIdentificationIsEmptyString_Then_ExceptionIsRaised() {
		final Api api = new Api();
		api.setIdentification("");
		api.setApiType(ApiType.EXTERNAL_FROM_JSON);

		service.createApiRest(api, new ArrayList<ApiOperation>(), new ArrayList<UserApi>(), 0);
	}

	@Test(expected = ApiManagerServiceException.class)
	public void given_ApiAndListApiOperationsAndListUserApiAndForcedVersion_When_OntologyOperationsIsNul_Then_ExceptionIsRaised() {
		final Api api = new Api();
		api.setIdentification("apiIdentification");
		api.setApiType(ApiType.EXTERNAL_FROM_JSON);

		service.createApiRest(api, null, new ArrayList<UserApi>(), 0);
	}

	@Test
	public void given_ApiStateAndNewState_When_CreatedToDevelopmentOrPublishedOrToSelf_Then_TrueIsReturned() {
		final boolean toCreated = service.validateState(ApiStates.CREATED, CREATED);
		final boolean toDevelop = service.validateState(ApiStates.CREATED, DEVELOPMENT);
		final boolean toPublished = service.validateState(ApiStates.CREATED, PUBLISHED);
		assertTrue(toCreated && toDevelop && toPublished);
	}

	@Test
	public void given_ApiStateAndNewState_When_CreatedToDeprecatedOrDeletedOrOther_Then_FalseIsReturned() {
		final boolean toDeprecated = service.validateState(ApiStates.CREATED, DEPRECATED);
		final boolean toDeleted = service.validateState(ApiStates.CREATED, DELETED);
		final boolean toOther = service.validateState(ApiStates.CREATED, FAKESTATE);
		assertFalse(toDeprecated || toDeleted || toOther);
	}

	@Test
	public void given_ApiStateAndNewState_When_DevelopmentToPublishedOrToSelf_Then_TrueIsReturned() {
		final boolean toDevelop = service.validateState(ApiStates.DEVELOPMENT, DEVELOPMENT);
		final boolean toPublished = service.validateState(ApiStates.DEVELOPMENT, PUBLISHED);
		assertTrue(toDevelop && toPublished);
	}

	@Test
	public void given_ApiStateAndNewState_When_DevelopmentToDeprecatedOrDeletedOrOther_Then_FalseIsReturned() {
		final boolean toCreated = service.validateState(ApiStates.DEVELOPMENT, CREATED);
		final boolean toDeprecated = service.validateState(ApiStates.DEVELOPMENT, DEPRECATED);
		final boolean toDeleted = service.validateState(ApiStates.DEVELOPMENT, DELETED);
		final boolean toOther = service.validateState(ApiStates.DEVELOPMENT, FAKESTATE);
		assertFalse(toCreated || toDeprecated || toDeleted || toOther);
	}

	@Test
	public void given_ApiStateAndNewState_When_PublishedToDeprecatedOrToSelf_Then_TrueIsReturned() {
		final boolean toPublished = service.validateState(ApiStates.PUBLISHED, PUBLISHED);
		final boolean toDeprecated = service.validateState(ApiStates.PUBLISHED, DEPRECATED);
		assertTrue(toPublished && toDeprecated);
	}

	@Test
	public void given_ApiStateAndNewState_When_PublishedToCreatedOrDevelopmentOrDeprecatedOrDeletedOrOther_Then_FalseIsReturned() {
		final boolean toCreated = service.validateState(ApiStates.PUBLISHED, CREATED);
		final boolean toDevelop = service.validateState(ApiStates.PUBLISHED, DEVELOPMENT);
		final boolean toDeleted = service.validateState(ApiStates.PUBLISHED, DELETED);
		final boolean toOther = service.validateState(ApiStates.PUBLISHED, FAKESTATE);
		assertFalse(toCreated || toDevelop || toDeleted || toOther);
	}

	@Test
	public void given_ApiStateAndNewState_When_DeprecatedToDeletedOrToSelf_Then_TrueIsReturned() {
		final boolean toDeprecated = service.validateState(ApiStates.DEPRECATED, DEPRECATED);
		final boolean toDeleted = service.validateState(ApiStates.DEPRECATED, DELETED);
		assertTrue(toDeprecated && toDeleted);
	}

	@Test
	public void given_ApiStateAndNewState_When_DeprecatedToCreatedOrDevelopmentOrPublishedOrOther_Then_FalseIsReturned() {
		final boolean toCreated = service.validateState(ApiStates.DEPRECATED, CREATED);
		final boolean toDevelop = service.validateState(ApiStates.DEPRECATED, DEVELOPMENT);
		final boolean toPublished = service.validateState(ApiStates.DEPRECATED, PUBLISHED);
		final boolean toOther = service.validateState(ApiStates.DEPRECATED, FAKESTATE);
		assertFalse(toCreated || toDevelop || toPublished || toOther);
	}

	@Test
	public void given_ApiStateAndNewState_When_DeletedToSelf_Then_TrueIsReturned() {
		final boolean toDeleted = service.validateState(ApiStates.DELETED, DELETED);
		assertTrue(toDeleted);
	}

	@Test
	public void given_ApiStateAndNewState_When_DeletedToCreatedOrDevelopmentOrPublishedOrDeprecatedOrOther_Then_FalseIsReturned() {
		final boolean toCreated = service.validateState(ApiStates.DELETED, CREATED);
		final boolean toDevelop = service.validateState(ApiStates.DELETED, DEVELOPMENT);
		final boolean toPublished = service.validateState(ApiStates.DELETED, PUBLISHED);
		final boolean toDeprecated = service.validateState(ApiStates.DELETED, DEPRECATED);
		final boolean toOther = service.validateState(ApiStates.DELETED, FAKESTATE);
		assertFalse(toCreated || toDevelop || toPublished || toDeprecated || toOther);
	}

}
