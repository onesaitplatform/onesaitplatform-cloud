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
package com.minsait.onesait.platform.controlpanel.rest.management.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.minsait.onesait.platform.config.model.App;
import com.minsait.onesait.platform.config.model.AppRole;
import com.minsait.onesait.platform.config.model.AppUser;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.AppUserRepository;
import com.minsait.onesait.platform.config.services.app.AppService;
import com.minsait.onesait.platform.config.services.app.dto.Realm;
import com.minsait.onesait.platform.config.services.app.dto.RealmAssociation;
import com.minsait.onesait.platform.config.services.app.dto.RealmCreate;
import com.minsait.onesait.platform.config.services.app.dto.RealmRole;
import com.minsait.onesait.platform.config.services.app.dto.RealmUpdate;
import com.minsait.onesait.platform.config.services.app.dto.RealmUser;
import com.minsait.onesait.platform.config.services.app.dto.RealmUserAuth;
import com.minsait.onesait.platform.config.services.exceptions.AppServiceException;
import com.minsait.onesait.platform.config.services.exceptions.UserServiceException;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.controller.dashboard.dto.UserDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.model.ErrorValidationResponse;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Api(value = "Realm Management", tags = { "Realm management service" })
@RestController
@RequestMapping("api/realms")
@ApiResponses({ @ApiResponse(code = 400, message = "Bad request"),
		@ApiResponse(code = 500, message = "Internal server error"), @ApiResponse(code = 403, message = "Forbidden") })
public class AppRestController {

	private static final String USER_STR = "User \"";
	private static final String NOT_EXIST = "\" does not exist";
	private static final String REALM_STR = "Realm \"";
	private static final String NOT_AUTH = "\" not authorized";
	private static final String IN_REALM_STR = "\" in Realm \"";

	private static final String NOT_FOUND = "\" not found";

	@Autowired
	private AppService appService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private UserService userService;
	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private AppUserRepository appUserRepository;

	@ApiOperation(value = "Get single realm info")
	@GetMapping("/{identification}")
	@ApiResponses(@ApiResponse(response = Realm.class, code = 200, message = "OK"))
	@Transactional
	public ResponseEntity<?> getRealm(
			@ApiParam(value = "Realm Identification", required = true) @PathVariable("identification") String identification) {

		final App app = appService.getAppByIdentification(identification);
		if (app != null) {
			final Realm realm = appService.getRealmByAppIdentification(app.getId());
			// user not administrator and not owner is not allowed to get
			if (!utils.isAdministrator()
					&& (null == realm.getUser() || !realm.getUser().getUserId().equals(utils.getUserId()))) {
				return new ResponseEntity<>(USER_STR + utils.getUserId() + NOT_AUTH, HttpStatus.UNAUTHORIZED);
			}
			return new ResponseEntity<>(realm, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(REALM_STR + identification + NOT_EXIST, HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Get realm's configured user extra fields")
	@GetMapping("/{identification}" + "/user-extra-fields")
	@ApiResponses(@ApiResponse(response = JsonNode.class, code = 200, message = "OK"))
	@Transactional
	public ResponseEntity<?> getRealmUserExtraFields(
			@ApiParam(value = "Realm Identification", required = true) @PathVariable("identification") String identification)
			throws IOException {

		final App realm = appService.getAppByIdentification(identification);
		if (realm != null) {
			// user not administrator and not owner is not allowed to get
			if (!utils.isAdministrator()
					&& (null == realm.getUser() || !realm.getUser().getUserId().equals(utils.getUserId()))) {
				return new ResponseEntity<>(USER_STR + utils.getUserId() + NOT_AUTH, HttpStatus.UNAUTHORIZED);
			}

			if (StringUtils.isEmpty(realm.getUserExtraFields())) {
				return new ResponseEntity<>(REALM_STR + identification + "\" does not have user extra fields defined",
						HttpStatus.OK);
			} else {
				return new ResponseEntity<>(mapper.readValue(realm.getUserExtraFields(), JsonNode.class),
						HttpStatus.OK);
			}
		} else {
			return new ResponseEntity<>(REALM_STR + identification + NOT_EXIST, HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Get all active realm users filtered by 'extra_fields' attribute")
	@GetMapping("/{identification}/users/filter/extra-fields/{jsonPath}/{value}")
	@ApiResponses(@ApiResponse(response = RealmUser[].class, code = 200, message = "OK"))
	public ResponseEntity<?> getAllFilter(
			@ApiParam(value = "Realm Identification", required = true) @PathVariable("identification") String identification,
			@PathVariable(value = "jsonPath") String jsonPath, @PathVariable(value = "value") String value) {
		final App appDb = appService.getAppByIdentification(identification);

		if (appDb == null) {
			return new ResponseEntity<>(REALM_STR + identification + NOT_EXIST, HttpStatus.BAD_REQUEST);
		}

		// user not administrator and not owner is not allowed to get Realm's users
		if (!utils.isAdministrator() && !appDb.getUser().getUserId().equals(utils.getUserId())
				&& !appService.isUserInApp(utils.getUserId(), appDb.getIdentification())) {
			return new ResponseEntity<>(USER_STR + utils.getUserId() + NOT_AUTH, HttpStatus.UNAUTHORIZED);
		}
		final List<RealmUser> users = new ArrayList<>();
		List<RealmUser> filteredUsers = new ArrayList<>();
		appDb.getAppRoles().forEach(r -> users.addAll(r.getAppUsers().stream().filter(u -> {
			if (!StringUtils.isEmpty(u.getUser().getExtraFields())) {
				try {

					final Object v = JsonPath.parse(u.getUser().getExtraFields()).read("$." + jsonPath, Object.class);

					return v != null && value.equals(String.valueOf(v));
				} catch (final Exception e) {
					log.debug("Error while reading extra_fields for user {}", u.getUser().getUserId(), e);
					return false;
				}

			}
			return false;
		}).map(u -> RealmUser.builder().avatar(u.getUser().getAvatar()).extraFields(u.getUser().getExtraFields())
				.fullName(u.getUser().getFullName()).mail(u.getUser().getEmail()).role(u.getRole().getName())
				.username(u.getUser().getUserId()).build()).collect(Collectors.toList())));
		for (final RealmUser realmUser : users) {
			filteredUsers = usersWithRole(realmUser, filteredUsers);
		}
		return new ResponseEntity<>(filteredUsers, HttpStatus.OK);

	}

	@ApiOperation(value = "Update realm's user extra fields JSON config")
	@PatchMapping("/{identification}" + "/user-extra-fields")
	@Transactional
	public ResponseEntity<String> patchRealmUserExtraFields(
			@ApiParam("Realm user's extra fields") @RequestBody String userExtraFields,
			@ApiParam(value = "Realm Identification", required = true) @PathVariable("identification") String identification,
			Errors errors) {

		final App realm = appService.getAppByIdentification(identification);
		if (realm != null) {

			// user not administrator and not owner is not allowed to update
			if (!utils.isAdministrator()
					&& (null == realm.getUser() || !realm.getUser().getUserId().equals(utils.getUserId()))) {
				return new ResponseEntity<>(USER_STR + utils.getUserId() + NOT_AUTH, HttpStatus.UNAUTHORIZED);
			}

			try {
				final JsonNode extrasJson = mapper.readTree(userExtraFields);
				realm.setUserExtraFields(mapper.writeValueAsString(extrasJson));
			} catch (final IOException e) {
				return new ResponseEntity<>("Input is not valid JSON", HttpStatus.BAD_REQUEST);
			}
			appService.updateApp(realm);
			return new ResponseEntity<>(HttpStatus.OK);
		} else {
			return new ResponseEntity<>(REALM_STR + identification + NOT_EXIST, HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Get all realms info")
	@GetMapping
	@ApiResponses(@ApiResponse(response = Realm[].class, code = 200, message = "OK"))
	@Transactional
	public ResponseEntity<?> getRealms() {
		final List<AppRole> allRoles = appService.getAllRoles();
		if (utils.isAdministrator()) {
			final List<Realm> realms = appService.getAllApps().stream()
					.map(a -> new Realm(appUserRepository, a, allRoles)).collect(Collectors.toList());
			return new ResponseEntity<>(realms, HttpStatus.OK);
		} else {
			final List<Realm> realms = appService.getAppsByUser(utils.getUserId(), null).stream()
					.map(a -> new Realm(appUserRepository, a, allRoles)).collect(Collectors.toList());
			return new ResponseEntity<>(realms, HttpStatus.OK);
		}
	}

	@ApiOperation(value = "Create a realm")
	@PostMapping
	@Transactional
	public ResponseEntity<?> create(@ApiParam(value = "Realm", required = true) @RequestBody @Valid RealmCreate realm,
			Errors errors) {
		if (errors.hasErrors()) {
			return ErrorValidationResponse.generateValidationErrorResponse(errors);
		}

		final App app = new App();
		try {
			app.setIdentification(getRealmIdentification(realm));
		} catch (final Exception e) {
			return new ResponseEntity<>("Error assigning identification", HttpStatus.BAD_REQUEST);
		}

		if (!app.getIdentification().matches(AppWebUtils.IDENTIFICATION_PATERN)) {
			return new ResponseEntity<>("Identification Error: Use alphanumeric characters and '-', '_'",
					HttpStatus.BAD_REQUEST);
		}

		app.setDescription(realm.getDescription());
		app.setUser(userService.getUser(utils.getUserId()));
		if (null != realm.getSecret()) {
			app.setSecret(realm.getSecret());
		} else {
			app.setSecret(null);
		}
		if (null != realm.getTokenValiditySeconds()) {
			app.setTokenValiditySeconds(realm.getTokenValiditySeconds());
		}
		realm.getRoles().stream().map(r -> realmRole2AppRole(r, app)).forEach(r -> app.getAppRoles().add(r));
		appService.createApp(app);
		return new ResponseEntity<>(HttpStatus.CREATED);

	}

	private String getRealmIdentification(RealmCreate realm) throws Exception {
		if (realm.getIdentification() != null) {
			return realm.getIdentification();
		}

		if (realm.getRealmId() != null) {
			return realm.getRealmId();
		}
		if (realm.getName() != null) {
			return realm.getName();
		}
		throw new Exception();
	}

	@ApiOperation(value = "Updates a realm")
	@PutMapping("/{identification}")
	@Transactional
	public ResponseEntity<?> update(
			@ApiParam(value = "Realm Identification", required = true) @PathVariable("identification") String identification,
			@ApiParam(value = "New Realm Description", required = true) @RequestBody @Valid RealmUpdate realm,
			Errors errors) {
		if (errors.hasErrors()) {
			return ErrorValidationResponse.generateValidationErrorResponse(errors);
		}

		try {
			final App appDb = appService.getAppByIdentification(identification);

			if (appDb == null) {
				return new ResponseEntity<>(REALM_STR + identification + NOT_EXIST, HttpStatus.BAD_REQUEST);
			}

			// user not administrator and not owner is not allowed to update
			if (!utils.isAdministrator()
					&& (null == appDb.getUser() || !appDb.getUser().getUserId().equals(utils.getUserId()))) {
				return new ResponseEntity<>(USER_STR + utils.getUserId() + NOT_AUTH, HttpStatus.UNAUTHORIZED);
			}

			if (null != realm.getDescription()) {
				appDb.setDescription(realm.getDescription());
			}

			if (null != realm.getSecret()) {
				appDb.setSecret(realm.getSecret());
			} else {
				appDb.setSecret(null);
			}
			if (null != realm.getTokenValiditySeconds()) {
				appDb.setTokenValiditySeconds(realm.getTokenValiditySeconds());
			}

			appService.updateApp(appDb);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (final RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@ApiOperation(value = "Deletes a realm")
	@DeleteMapping("/{identification}")
	@Transactional
	public ResponseEntity<?> delete(
			@ApiParam(value = "Realm Identification", required = true) @PathVariable("identification") String identification) {

		final App appDb = appService.getAppByIdentification(identification);

		if (appDb == null) {
			return new ResponseEntity<>(REALM_STR + identification + NOT_EXIST, HttpStatus.BAD_REQUEST);
		}

		// user not administrator and not owner is not allowed to delete
		if (!utils.isAdministrator()
				&& (null == appDb.getUser() || !appDb.getUser().getUserId().equals(utils.getUserId()))) {
			return new ResponseEntity<>(USER_STR + utils.getUserId() + NOT_AUTH, HttpStatus.UNAUTHORIZED);
		}

		try {
			appService.deleteApp(appDb);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (final RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@ApiOperation(value = "Authorizes user with a role in a existing Realm")
	@PostMapping("/authorization")
	@Transactional
	public ResponseEntity<?> createAuthorization(
			@ApiParam(value = "Realm Authorization", required = true) @Valid @RequestBody RealmUserAuth authorization,
			Errors errors) {
		if (errors.hasErrors()) {
			return ErrorValidationResponse.generateValidationErrorResponse(errors);
		}

		final App appDb = appService.getAppByIdentification(authorization.getRealmId());

		if (appDb == null) {
			return new ResponseEntity<>(REALM_STR + authorization.getRealmId() + NOT_EXIST, HttpStatus.BAD_REQUEST);
		}

		// user not administrator and not owner is not allowed to authorize
		if (!utils.isAdministrator()
				&& (null == appDb.getUser() || !appDb.getUser().getUserId().equals(utils.getUserId()))) {
			return new ResponseEntity<>(USER_STR + utils.getUserId() + NOT_AUTH, HttpStatus.UNAUTHORIZED);
		}

		if (userService.getUserByIdentification(authorization.getUserId()) == null) {
			return new ResponseEntity<>(USER_STR + authorization.getUserId() + NOT_EXIST, HttpStatus.BAD_REQUEST);
		}
		try {
			final AppRole role = appService.getByRoleNameAndApp(authorization.getRoleName(), appDb);
			if (role == null) {
				return new ResponseEntity<>("Role \"" + authorization.getRoleName() + "\" does not exist in Realm "
						+ authorization.getRealmId(), HttpStatus.BAD_REQUEST);
			}
			appService.createUserAccess(appDb.getId(), authorization.getUserId(), role.getId());
			return new ResponseEntity<>(HttpStatus.CREATED);
		} catch (final AppServiceException e) {
			return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@ApiOperation(value = "Invalidates user authorization for a Realm")
	@DeleteMapping("/authorization/realm/{identification}/user/{userId}")
	@Transactional
	public ResponseEntity<?> deleteAuthorization(
			@ApiParam(value = "Realm identification", required = true) @PathVariable("identification") String identification,
			@ApiParam(value = "User id", required = true) @PathVariable("userId") String userId) {

		final App appDb = appService.getAppByIdentification(identification);

		if (appDb == null) {
			return new ResponseEntity<>(REALM_STR + identification + NOT_EXIST, HttpStatus.BAD_REQUEST);
		}

		// user not administrator and not owner is not allowed to unauthorize
		if (!utils.isAdministrator()
				&& (null == appDb.getUser() || !appDb.getUser().getUserId().equals(utils.getUserId()))) {
			return new ResponseEntity<>(USER_STR + utils.getUserId() + NOT_AUTH, HttpStatus.UNAUTHORIZED);
		}

		try {
			final AppRole role = appDb
					.getAppRoles().stream().filter(r -> r.getAppUsers().stream()
							.filter(u -> u.getUser().getUserId().equals(userId)).findAny().orElse(null) != null)
					.findAny().orElse(null);
			if (role == null) {
				return new ResponseEntity<>(
						"Authorization for user \"" + userId + IN_REALM_STR + identification + "\" not found.",
						HttpStatus.BAD_REQUEST);
			}

			final Optional<AppUser> appUser = role.getAppUsers().stream()
					.filter(u -> u.getUser().getUserId().equals(userId)).findFirst();

			if (appUser.isPresent()) {
				appDb.getAppRoles().stream().forEach(u -> u.getAppUsers().stream().
						filter(s -> s.getUser().getUserId().equals(userId)).forEach(p -> appService.deleteUserAccess(p.getId())));
			} else {
				return new ResponseEntity<>("Error retrieving user \"" + userId + IN_REALM_STR + identification,
						HttpStatus.BAD_REQUEST);
			}

			return new ResponseEntity<>(HttpStatus.OK);

		} catch (final AppServiceException e) {
			return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Creates a realm association given parent and child realms, as well as respective roles")
	@PostMapping("/association")
	@Transactional
	public ResponseEntity<?> createAssociation(
			@ApiParam(value = "Realm Association", required = true) @Valid @RequestBody RealmAssociation association,
			Errors errors) {
		if (errors.hasErrors()) {
			return ErrorValidationResponse.generateValidationErrorResponse(errors);
		}

		final App appDbChild = appService.getAppByIdentification(association.getChildRealmId());
		final App appDbParent = appService.getAppByIdentification(association.getParentRealmId());

		if (appDbChild == null || appDbParent == null) {
			return new ResponseEntity<>("Any of the specified realms does not exist", HttpStatus.BAD_REQUEST);
		}

		// user not administrator and not owner is not allowed to associate this realms
		if (!utils.isAdministrator() && (null == appDbChild.getUser()
				|| !appDbChild.getUser().getUserId().equals(utils.getUserId()) || null == appDbParent.getUser()
				|| !appDbParent.getUser().getUserId().equals(utils.getUserId()))) {
			return new ResponseEntity<>(USER_STR + utils.getUserId() + NOT_AUTH, HttpStatus.UNAUTHORIZED);
		}

		if (null == appService.getByRoleNameAndApp(association.getParentRoleName(), appDbParent)
				|| null == appService.getByRoleNameAndApp(association.getChildRoleName(), appDbChild)) {
			return new ResponseEntity<>("Any Role does not exists.", HttpStatus.BAD_REQUEST);
		}

		try {
			appService.createAssociation(association.getParentRoleName(), association.getChildRoleName(),
					association.getParentRealmId(), association.getChildRealmId());
			return new ResponseEntity<>(HttpStatus.CREATED);
		} catch (final AppServiceException e) {
			return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@ApiOperation(value = "Deletes a realm's association")
	@DeleteMapping("/association/parent-realm/{parentRealmId}/parent-role/{parentRole}/child-realm/{childRealmId}/child-role/{childRole}")
	@Transactional
	public ResponseEntity<?> deleteAssociation(
			@ApiParam(value = "Parent Realm id", required = true) @PathVariable("parentRealmId") String parentRealmId,
			@ApiParam(value = "Child Realm id", required = true) @PathVariable("childRealmId") String childRealmId,
			@ApiParam(value = "Parent role", required = true) @PathVariable("parentRole") String parentRole,
			@ApiParam(value = "Child role", required = true) @PathVariable("childRole") String childRole) {

		final App appDbChild = appService.getAppByIdentification(childRealmId);
		final App appDbParent = appService.getAppByIdentification(parentRealmId);

		if (appDbChild == null || appDbParent == null) {
			return new ResponseEntity<>("Any of the specified realms does not exist", HttpStatus.BAD_REQUEST);
		}

		// user not administrator and not owner is not allowed to delete an association
		// of this realms
		if (!utils.isAdministrator() && (null == appDbChild.getUser()
				|| !appDbChild.getUser().getUserId().equals(utils.getUserId()) || null == appDbParent.getUser()
				|| !appDbParent.getUser().getUserId().equals(utils.getUserId()))) {
			return new ResponseEntity<>(USER_STR + utils.getUserId() + NOT_AUTH, HttpStatus.UNAUTHORIZED);
		}

		if (null == appService.getByRoleNameAndApp(parentRole, appDbParent)
				|| null == appService.getByRoleNameAndApp(childRole, appDbChild)) {
			return new ResponseEntity<>("Any Role does not exists.", HttpStatus.BAD_REQUEST);
		}

		try {
			appService.deleteAssociation(parentRole, childRole, parentRealmId, childRealmId);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (final AppServiceException e) {
			return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@ApiOperation(value = "Get all roles in a Realm")
	@GetMapping("/{identification}/roles")
	@ApiResponses(@ApiResponse(response = RealmRole[].class, code = 200, message = "OK"))
	@Transactional
	public ResponseEntity<?> getRoles(
			@ApiParam(value = "Realm Identification", required = true) @PathVariable("identification") String identification) {

		final App appDb = appService.getAppByIdentification(identification);

		if (appDb == null) {
			return new ResponseEntity<>(REALM_STR + identification + NOT_EXIST, HttpStatus.BAD_REQUEST);
		}

		// user not administrator and not owner is not allowed to get Roles
		if (!utils.isAdministrator()
				&& (null == appDb.getUser() || !appDb.getUser().getUserId().equals(utils.getUserId()))) {
			return new ResponseEntity<>(USER_STR + utils.getUserId() + NOT_AUTH, HttpStatus.UNAUTHORIZED);
		}

		final List<RealmRole> roles = appDb.getAppRoles().stream()
				.map(r -> new RealmRole(r.getName(), r.getDescription())).collect(Collectors.toList());
		return new ResponseEntity<>(roles, HttpStatus.OK);
	}

	@ApiOperation(value = "Creates a role in a Realm")
	@PostMapping("/{identification}/roles")
	@Transactional
	public ResponseEntity<String> addRole(
			@ApiParam(value = "Realm Identification", required = true) @PathVariable("identification") String identification,
			@ApiParam(value = "Realm role", required = true) @Valid @RequestBody RealmRole role) {

		final App appDb = appService.getAppByIdentification(identification);

		if (appDb == null) {
			return new ResponseEntity<>(REALM_STR + identification + NOT_EXIST, HttpStatus.BAD_REQUEST);
		}
		if (appDb.getAppRoles().stream().anyMatch(r -> r.getName().equalsIgnoreCase(role.getName()))) {
			return new ResponseEntity<>("Role already exists wiht name " + role.getName(), HttpStatus.BAD_REQUEST);
		}

		// user not administrator and not owner is not allowed to get Roles
		if (!utils.isAdministrator()
				&& (null == appDb.getUser() || !appDb.getUser().getUserId().equals(utils.getUserId()))) {
			return new ResponseEntity<>(USER_STR + utils.getUserId() + NOT_AUTH, HttpStatus.UNAUTHORIZED);
		}

		final AppRole newRole = new AppRole();
		newRole.setApp(appDb);
		newRole.setName(role.getName());
		newRole.setDescription(role.getDescription());
		appDb.getAppRoles().add(newRole);
		appService.updateApp(appDb);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@ApiOperation(value = "Deletes a role in a Realm")
	@DeleteMapping("/{identification}/roles/{roleName}")
	@Transactional
	public ResponseEntity<?> deleteRole(
			@ApiParam(value = "Realm Identification", required = true) @PathVariable("identification") String identification,
			@ApiParam(value = "Role name", required = true) @PathVariable("roleName") String roleName) {

		final App appDb = appService.getAppByIdentification(identification);

		if (appDb == null) {
			return new ResponseEntity<>(REALM_STR + identification + NOT_EXIST, HttpStatus.BAD_REQUEST);
		}

		// user not administrator and not owner is not allowed to get Roles
		if (!utils.isAdministrator()
				&& (null == appDb.getUser() || !appDb.getUser().getUserId().equals(utils.getUserId()))) {
			return new ResponseEntity<>(USER_STR + utils.getUserId() + NOT_AUTH, HttpStatus.UNAUTHORIZED);
		}

		final AppRole role = appService.getByRoleNameAndApp(roleName, appDb);
		if (role == null) {
			return new ResponseEntity<>("Role \"" + roleName + "\" does not exist in realm \"" + identification + "\"",
					HttpStatus.BAD_REQUEST);
		}
		appService.deleteRole(role);
		appService.updateApp(appDb);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Get all users in a Realm")
	@GetMapping("/{identification}/users")
	@ApiResponses(@ApiResponse(response = RealmUser[].class, code = 200, message = "OK"))
	@Transactional
	public ResponseEntity<?> getUsers(
			@ApiParam(value = "Realm Identification", required = true) @PathVariable("identification") String identification) {

		final App appDb = appService.getAppByIdentification(identification);

		if (appDb == null) {
			return new ResponseEntity<>(REALM_STR + identification + NOT_EXIST, HttpStatus.BAD_REQUEST);
		}

		// user not administrator and not owner is not allowed to get Realm's users
		if (!utils.isAdministrator() && !appDb.getUser().getUserId().equals(utils.getUserId())
				&& !appService.isUserInApp(utils.getUserId(), appDb.getIdentification())) {

			return new ResponseEntity<>(USER_STR + utils.getUserId() + NOT_AUTH, HttpStatus.UNAUTHORIZED);
		}

		final List<RealmUser> users = new ArrayList<>();
		List<RealmUser> filteredUsers = new ArrayList<>();
		appDb.getAppRoles().forEach(r -> users.addAll(r.getAppUsers().stream()
				.map(u -> RealmUser.builder().avatar(u.getUser().getAvatar()).extraFields(u.getUser().getExtraFields())
						.fullName(u.getUser().getFullName()).mail(u.getUser().getEmail()).role(u.getRole().getName())
						.username(u.getUser().getUserId()).build())
				.collect(Collectors.toList())));
		for (final RealmUser realmUser : users) {
			filteredUsers = usersWithRole(realmUser, filteredUsers);
		}
		return new ResponseEntity<>(filteredUsers, HttpStatus.OK);

	}

	@ApiOperation(value = "Gets a user in  a Realm")
	@GetMapping("/{identification}/users/{userId}")
	@ApiResponses(@ApiResponse(response = RealmUser.class, code = 200, message = "OK"))
	// @Transactional
	public ResponseEntity<?> getUser(
			@ApiParam(value = "Realm Identification", required = true) @PathVariable("identification") String identification,
			@ApiParam(value = "User id", required = true) @PathVariable("userId") String userId) {

		final App appDb = appService.getAppByIdentification(identification);

		if (appDb == null) {
			return new ResponseEntity<>(REALM_STR + identification + NOT_EXIST, HttpStatus.BAD_REQUEST);
		}

		// user not administrator and not owner is not allowed to get Realm's user
		if (!utils.isAdministrator()
				&& (null == appDb.getUser() || !appDb.getUser().getUserId().equals(utils.getUserId()))
				&& !utils.getUserId().equals(userId)) {

			return new ResponseEntity<>(USER_STR + utils.getUserId() + NOT_AUTH, HttpStatus.UNAUTHORIZED);
		}

		if (!appService.isUserInApp(userId, identification)) {
			return new ResponseEntity<>(USER_STR + utils.getUserId() + NOT_EXIST, HttpStatus.NOT_FOUND);
		}

		final List<RealmUser> users = new ArrayList<>();
		List<RealmUser> filteredUsers = new ArrayList<>();
		appDb.getAppRoles().forEach(r -> users.addAll(r.getAppUsers().stream()
				.filter(u -> u.getUser().getUserId().equals(userId))
				.map(u -> RealmUser.builder().avatar(u.getUser().getAvatar()).extraFields(u.getUser().getExtraFields())
						.fullName(u.getUser().getFullName()).mail(u.getUser().getEmail()).role(u.getRole().getName())
						.username(u.getUser().getUserId()).build())
				.collect(Collectors.toList())));

		for (final RealmUser realmUser : users) {
			filteredUsers = usersWithRole(realmUser, filteredUsers);
		}

		return new ResponseEntity<>(filteredUsers, HttpStatus.OK);
	}

	@ApiOperation(value = "Invalidates user authorization for a Realm")
	@DeleteMapping("/authorization/realm/{identification}/user/{userId}/rol/{rolId}")
	@Transactional
	public ResponseEntity<?> deleteUserRolAuthorization(
			@ApiParam(value = "Realm Identification", required = true) @PathVariable("identification") String identification,
			@ApiParam(value = "User id", required = true) @PathVariable("userId") String userId,
			@ApiParam(value = "Rol id", required = true) @PathVariable("rolId") String rolId) {

		final App appDb = appService.getAppByIdentification(identification);

		if (appDb == null) {
			return new ResponseEntity<>(REALM_STR + identification + NOT_EXIST, HttpStatus.BAD_REQUEST);
		}

		// user not administrator and not owner is not allowed to unauthorize
		if (!utils.isAdministrator()
				&& (null == appDb.getUser() || !appDb.getUser().getUserId().equals(utils.getUserId()))) {
			return new ResponseEntity<>(USER_STR + utils.getUserId() + NOT_AUTH, HttpStatus.UNAUTHORIZED);
		}

		try {
			final AppRole role = appDb.getAppRoles().stream()
					.filter(r -> r.getAppUsers().stream().filter(u -> u.getUser().getUserId().equals(userId))
							.filter(s -> s.getRole().getName().equals(rolId)).findAny().orElse(null) != null)
					.findAny().orElse(null);
			if (role == null) {
				return new ResponseEntity<>("Authorization for user \"" + userId + IN_REALM_STR + identification
						+ "\" with role \"" + rolId + "\" not found.", HttpStatus.BAD_REQUEST);
			}

			final Optional<AppUser> appUser = role.getAppUsers().stream()
					.filter(u -> u.getUser().getUserId().equals(userId))
					.filter(s -> s.getRole().getName().equals(rolId)).findFirst();

			if (appUser.isPresent()) {
				appService.deleteUserAccess(appUser.get().getId());
			} else {
				return new ResponseEntity<>(
						"Error retrieving user \"" + userId + IN_REALM_STR + identification + "\" with role \"" + rolId,
						HttpStatus.BAD_REQUEST);
			}

			return new ResponseEntity<>(HttpStatus.OK);
		} catch (final AppServiceException e) {
			return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@ApiOperation(value = "Gets a user in a Realm by email")
	@GetMapping("/{identification}/user")
	@ApiResponses(@ApiResponse(response = RealmUser.class, code = 200, message = "OK"))
	public ResponseEntity<?> getUserByEmail(
			@ApiParam(value = "Realm Identification", required = true) @PathVariable("identification") String identification,
			@ApiParam(value = "Email", required = true) @RequestParam("email") String email) {

		final App appDb = appService.getAppByIdentification(identification);

		if (appDb == null) {
			log.info(REALM_STR + identification + NOT_EXIST);
			return new ResponseEntity<>(REALM_STR + identification + NOT_EXIST, HttpStatus.BAD_REQUEST);
		}

		if (!utils.isAdministrator()
				&& (null == appDb.getUser() || !appDb.getUser().getUserId().equals(utils.getUserId()))) {
			log.info(USER_STR + utils.getUserId() + NOT_AUTH);
			return new ResponseEntity<>(USER_STR + utils.getUserId() + NOT_AUTH, HttpStatus.UNAUTHORIZED);
		}

		try {
			final User userByEmail = userService.getUserByEmail(email);

			if (userByEmail == null) {
				log.info(USER_STR + " with email: " + email + NOT_FOUND);
				return new ResponseEntity<>(USER_STR + " with email: " + email + NOT_FOUND, HttpStatus.NOT_FOUND);
			}

			if (!appService.isUserInApp(userByEmail.getUserId(), identification)) {
				log.info(USER_STR + userByEmail + NOT_FOUND);
				return new ResponseEntity<>(USER_STR + userByEmail + NOT_FOUND, HttpStatus.NOT_FOUND);

			} else {
				final UserDTO result = new UserDTO();

				result.setUserId(userByEmail.getUserId());
				result.setFullName(userByEmail.getFullName());

				return new ResponseEntity<>(result, HttpStatus.OK);
			}

		} catch (final UserServiceException e) {
			log.error("Internal Server Error", e);
			return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	private List<RealmUser> usersWithRole(RealmUser realmUser, List<RealmUser> filteredUsers) {
		boolean exist = false;
		for (final RealmUser user : filteredUsers) {
			if (user.getUsername().equals(realmUser.getUsername())) {
				user.setRole(user.getRole() + "," + realmUser.getRole());
				exist = true;
			}
		}
		if (!exist) {
			filteredUsers.add(realmUser);
		}
		return filteredUsers;
	}

	private AppRole realmRole2AppRole(RealmRole role, App app) {
		final AppRole appRole = new AppRole();
		appRole.setApp(app);
		appRole.setDescription(role.getDescription());
		appRole.setName(role.getName());

		return appRole;
	}

}
