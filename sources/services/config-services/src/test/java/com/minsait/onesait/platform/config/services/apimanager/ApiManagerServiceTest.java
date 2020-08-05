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
		String identification = "apiTest";
		ApiType apiType = ApiType.INTERNAL_ONTOLOGY;

		List<Api> existentApis = new ArrayList<>();

		when(apiRepository.findByIdentificationAndApiType(identification, apiType)).thenReturn(existentApis);
		assertTrue(service.calculateNumVersion(identification, apiType) == 1);

	}

	@Test
	public void given_NumVersionJson_Then_CalculateNumVersionCorrectly() {
		String identification = "apiTest";
		ApiType apiType = ApiType.INTERNAL_ONTOLOGY;
		String rawJson = "{\"identification\":\"" + identification + "\",\"apiType\":\"" + apiType.toString() + "\"}";

		List<Api> existentApis = new ArrayList<>();

		when(apiRepository.findByIdentificationAndApiType(identification, apiType)).thenReturn(existentApis);
		assertTrue(service.calculateNumVersion(rawJson) == 1);

	}

	@Test
	public void given_IdentificaionAndApiType_When_ExistingApis_Then_CalculateNumVersionCorrectly() {
		String identification = "apiTest";
		ApiType apiType = ApiType.INTERNAL_ONTOLOGY;

		Api api = new Api();
		api.setIdentification(identification);
		api.setNumversion(1);
		List<Api> existentApis = new ArrayList<>();
		existentApis.add(api);

		when(apiRepository.findByIdentificationAndApiType(identification, apiType)).thenReturn(existentApis);
		assertTrue(service.calculateNumVersion(identification, apiType) == 2);

	}

	@Test
	public void given_NumVersionJson_When_ExistingApis_Then_CalculateNumVersionCorrectly() {
		String identification = "apiTest";
		ApiType apiType = ApiType.INTERNAL_ONTOLOGY;
		String rawJson = "{\"identification\":\"" + identification + "\",\"apiType\":\"" + apiType.toString() + "\"}";

		Api api = new Api();
		api.setIdentification(identification);
		api.setNumversion(1);
		List<Api> existentApis = new ArrayList<>();
		existentApis.add(api);

		when(apiRepository.findByIdentificationAndApiType(identification, apiType)).thenReturn(existentApis);
		assertTrue(service.calculateNumVersion(rawJson) == 2);

	}

	@Test
	public void given_ApiAndUser_When_ThereAreExistingApis_Then_LastVersionApiReturned() {
		String apiId = "apiId";
		String userId = "userId";
		int lastVersion = 3;

		User user = new User();
		user.setUserId(userId);

		Api api;
		List<Api> existingApis = new ArrayList<>();
		for (int i = 1; i < lastVersion; i++) {
			api = new Api();
			api.setIdentification(apiId);
			api.setUser(user);
			api.setNumversion(i);
			existingApis.add(api);
		}

		Api apiLast = new Api();
		apiLast.setIdentification(apiId);
		apiLast.setUser(user);
		apiLast.setNumversion(lastVersion);
		existingApis.add(apiLast);

		when(apiRepository.findByIdentificationAndUser(apiId, user)).thenReturn(existingApis);
		assertTrue(service.getLastVersionOfApiByApiAndUser(apiLast, user) == apiLast);
	}

	@Test
	public void given_ApiAndListApiOperationsAndListUserApiAndUser_VersionatedApiIsReturned() {
		String apiId = "apiId";
		String userId = "userId";
		int lastVersion = 3;

		User user = new User();
		user.setUserId(userId);

		Ontology ontology = new Ontology();

		Api api;
		List<Api> existingApis = new ArrayList<>();
		for (int i = 1; i < lastVersion; i++) {
			api = new Api();
			api.setIdentification(apiId);
			api.setUser(user);
			api.setNumversion(i);
			api.setOntology(ontology);
			existingApis.add(api);
		}

		Api apiLast = new Api();
		apiLast.setIdentification(apiId);
		apiLast.setUser(user);
		apiLast.setNumversion(lastVersion);
		apiLast.setState(ApiStates.PUBLISHED);
		apiLast.setOntology(ontology);
		apiLast.setApiType(ApiType.EXTERNAL_FROM_JSON);
		existingApis.add(apiLast);

		List<ApiOperation> operations = new ArrayList<>();
		List<UserApi> authentications = new ArrayList<>();

		Api expectedApi = new Api();
		expectedApi.setIdentification(apiId);
		expectedApi.setUser(user);
		expectedApi.setNumversion(lastVersion + 1);
		expectedApi.setState(ApiStates.DEVELOPMENT);
		expectedApi.setOntology(ontology);
		expectedApi.setApiType(ApiType.EXTERNAL_FROM_JSON);

		when(apiRepository.findByIdentificationAndUser(apiId, user)).thenReturn(existingApis);
		when(apiRepository.save(apiLast)).thenReturn(expectedApi);

		Api returnedApi = service.versionateApiRest(apiLast, operations, authentications, user);
		assertTrue(returnedApi.getState() == expectedApi.getState());
		assertTrue(returnedApi.getNumversion() == expectedApi.getNumversion());

	}

	@Test
	public void given_ApiAndUserOrApiIdAndUserId_When_HasUserEditAccess_Then_TrueIsReturned() {
		String apiId = "apiId";
		String userId = "userId";

		Role role = new Role();
		User user = new User();
		user.setUserId(userId);
		Api api = new Api();
		api.setId(apiId);

		role.setId(Role.Type.ROLE_ADMINISTRATOR.name());
		user.setRole(role);
		api.setUser(user);
		api.setPublic(false);
		api.setState(ApiStates.PUBLISHED);

		when(userService.getUser(userId)).thenReturn(user);
		when(apiRepository.findById(apiId)).thenReturn(api);
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
		String apiId = "apiId";
		String userId = "userId";

		Role role = new Role();
		User user = new User();
		user.setUserId(userId);
		Api api = new Api();
		api.setId(apiId);

		role.setId(Role.Type.ROLE_ADMINISTRATOR.name());
		user.setRole(role);
		api.setUser(user);
		api.setPublic(false);
		api.setState(ApiStates.PUBLISHED);

		when(userService.getUser(userId)).thenReturn(user);
		when(apiRepository.findById(apiId)).thenReturn(api);
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
		String apiId = "apiId";

		Api api = new Api();
		api.setId(apiId);
		api.setState(ApiStates.CREATED);

		when(apiRepository.findById(apiId)).thenReturn(api);
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
		String apiId = "apiId";

		Api api = new Api();
		api.setId(apiId);
		api.setState(ApiStates.CREATED);

		when(apiRepository.findById(apiId)).thenReturn(api);
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
		Role role = new Role();
		role.setId(Role.Type.ROLE_ADMINISTRATOR.name());

		User user = new User();
		user.setRole(role);

		Api api = new Api();
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
		Api api = new Api();
		api.setOntology(null);
		api.setApiType(ApiType.INTERNAL_ONTOLOGY);

		service.createApiRest(api, new ArrayList<ApiOperation>(), new ArrayList<UserApi>(), 0);
	}

	@Test(expected = ApiManagerServiceException.class)
	public void given_ApiAndListApiOperationsAndListUserApiAndForcedVersion_When_OntologyIdentificatioIsnNull_Then_ExceptionIsRaised() {
		Api api = new Api();
		api.setIdentification(null);
		api.setApiType(ApiType.EXTERNAL_FROM_JSON);

		service.createApiRest(api, new ArrayList<ApiOperation>(), new ArrayList<UserApi>(), 0);
	}

	@Test(expected = ApiManagerServiceException.class)
	public void given_ApiAndListApiOperationsAndListUserApiAndForcedVersion_When_OntologyIdentificationIsEmptyString_Then_ExceptionIsRaised() {
		Api api = new Api();
		api.setIdentification("");
		api.setApiType(ApiType.EXTERNAL_FROM_JSON);

		service.createApiRest(api, new ArrayList<ApiOperation>(), new ArrayList<UserApi>(), 0);
	}

	@Test(expected = ApiManagerServiceException.class)
	public void given_ApiAndListApiOperationsAndListUserApiAndForcedVersion_When_OntologyOperationsIsNul_Then_ExceptionIsRaised() {
		Api api = new Api();
		api.setIdentification("apiIdentification");
		api.setApiType(ApiType.EXTERNAL_FROM_JSON);

		service.createApiRest(api, null, new ArrayList<UserApi>(), 0);
	}

	@Test
	public void given_ApiStateAndNewState_When_CreatedToDevelopmentOrPublishedOrToSelf_Then_TrueIsReturned() {
		boolean toCreated = service.validateState(ApiStates.CREATED, CREATED);
		boolean toDevelop = service.validateState(ApiStates.CREATED, DEVELOPMENT);
		boolean toPublished = service.validateState(ApiStates.CREATED, PUBLISHED);
		assertTrue(toCreated && toDevelop && toPublished);
	}

	@Test
	public void given_ApiStateAndNewState_When_CreatedToDeprecatedOrDeletedOrOther_Then_FalseIsReturned() {
		boolean toDeprecated = service.validateState(ApiStates.CREATED, DEPRECATED);
		boolean toDeleted = service.validateState(ApiStates.CREATED, DELETED);
		boolean toOther = service.validateState(ApiStates.CREATED, FAKESTATE);
		assertFalse(toDeprecated || toDeleted || toOther);
	}

	@Test
	public void given_ApiStateAndNewState_When_DevelopmentToPublishedOrToSelf_Then_TrueIsReturned() {
		boolean toDevelop = service.validateState(ApiStates.DEVELOPMENT, DEVELOPMENT);
		boolean toPublished = service.validateState(ApiStates.DEVELOPMENT, PUBLISHED);
		assertTrue(toDevelop && toPublished);
	}

	@Test
	public void given_ApiStateAndNewState_When_DevelopmentToDeprecatedOrDeletedOrOther_Then_FalseIsReturned() {
		boolean toCreated = service.validateState(ApiStates.DEVELOPMENT, CREATED);
		boolean toDeprecated = service.validateState(ApiStates.DEVELOPMENT, DEPRECATED);
		boolean toDeleted = service.validateState(ApiStates.DEVELOPMENT, DELETED);
		boolean toOther = service.validateState(ApiStates.DEVELOPMENT, FAKESTATE);
		assertFalse(toCreated || toDeprecated || toDeleted || toOther);
	}

	@Test
	public void given_ApiStateAndNewState_When_PublishedToDeprecatedOrToSelf_Then_TrueIsReturned() {
		boolean toPublished = service.validateState(ApiStates.PUBLISHED, PUBLISHED);
		boolean toDeprecated = service.validateState(ApiStates.PUBLISHED, DEPRECATED);
		assertTrue(toPublished && toDeprecated);
	}

	@Test
	public void given_ApiStateAndNewState_When_PublishedToCreatedOrDevelopmentOrDeprecatedOrDeletedOrOther_Then_FalseIsReturned() {
		boolean toCreated = service.validateState(ApiStates.PUBLISHED, CREATED);
		boolean toDevelop = service.validateState(ApiStates.PUBLISHED, DEVELOPMENT);
		boolean toDeleted = service.validateState(ApiStates.PUBLISHED, DELETED);
		boolean toOther = service.validateState(ApiStates.PUBLISHED, FAKESTATE);
		assertFalse(toCreated || toDevelop || toDeleted || toOther);
	}

	@Test
	public void given_ApiStateAndNewState_When_DeprecatedToDeletedOrToSelf_Then_TrueIsReturned() {
		boolean toDeprecated = service.validateState(ApiStates.DEPRECATED, DEPRECATED);
		boolean toDeleted = service.validateState(ApiStates.DEPRECATED, DELETED);
		assertTrue(toDeprecated && toDeleted);
	}

	@Test
	public void given_ApiStateAndNewState_When_DeprecatedToCreatedOrDevelopmentOrPublishedOrOther_Then_FalseIsReturned() {
		boolean toCreated = service.validateState(ApiStates.DEPRECATED, CREATED);
		boolean toDevelop = service.validateState(ApiStates.DEPRECATED, DEVELOPMENT);
		boolean toPublished = service.validateState(ApiStates.DEPRECATED, PUBLISHED);
		boolean toOther = service.validateState(ApiStates.DEPRECATED, FAKESTATE);
		assertFalse(toCreated || toDevelop || toPublished || toOther);
	}

	@Test
	public void given_ApiStateAndNewState_When_DeletedToSelf_Then_TrueIsReturned() {
		boolean toDeleted = service.validateState(ApiStates.DELETED, DELETED);
		assertTrue(toDeleted);
	}

	@Test
	public void given_ApiStateAndNewState_When_DeletedToCreatedOrDevelopmentOrPublishedOrDeprecatedOrOther_Then_FalseIsReturned() {
		boolean toCreated = service.validateState(ApiStates.DELETED, CREATED);
		boolean toDevelop = service.validateState(ApiStates.DELETED, DEVELOPMENT);
		boolean toPublished = service.validateState(ApiStates.DELETED, PUBLISHED);
		boolean toDeprecated = service.validateState(ApiStates.DELETED, DEPRECATED);
		boolean toOther = service.validateState(ApiStates.DELETED, FAKESTATE);
		assertFalse(toCreated || toDevelop || toPublished || toDeprecated || toOther);
	}

}
