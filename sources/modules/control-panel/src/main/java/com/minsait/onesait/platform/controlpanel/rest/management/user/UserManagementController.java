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
package com.minsait.onesait.platform.controlpanel.rest.management.user;

import static com.minsait.onesait.platform.controlpanel.rest.management.user.UserManagementUrl.OP_USER;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import com.jayway.jsonpath.JsonPath;
import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.services.exceptions.UserServiceException;
import com.minsait.onesait.platform.config.services.user.UserAmplified;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.rest.management.model.ErrorValidationResponse;
import com.minsait.onesait.platform.controlpanel.rest.management.user.model.UserId;
import com.minsait.onesait.platform.controlpanel.rest.management.user.model.UserSimplified;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@Api(value = "User Management", tags = { "User management service" })
@RestController
@RequestMapping("api" + OP_USER)
@ApiResponses({ @ApiResponse(code = 400, message = "Bad request"),
		@ApiResponse(code = 500, message = "Internal server error"), @ApiResponse(code = 403, message = "Forbidden") })
@Slf4j
public class UserManagementController {

	private static final String DOES_NOT_EXIST = "\" does not exist";
	private static final String USER_STR = "User \"";

	@Autowired
	private AppWebUtils utils;
	@Autowired
	private UserService userService;
	@Autowired
	private MultitenancyService multitenancyService;
	@Autowired
	private ConfigurationService configurationService;

	@ApiOperation(value = "Get user by id")
	@GetMapping("/{id}")
	@ApiResponses(@ApiResponse(response = UserAmplified.class, code = 200, message = "OK"))
	public ResponseEntity<?> get(
			@ApiParam(value = "User id", example = "developer", required = true) @PathVariable("id") String userId) {
		if (isUserAdminOrSameAsRequest(userId)) {
			if (userService.getUser(userId) == null) {
				return new ResponseEntity<>(USER_STR + userId + DOES_NOT_EXIST, HttpStatus.NOT_FOUND);
			}
			return new ResponseEntity<>(new UserAmplified(userService.getUser(userId)), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

	@ApiOperation(value = "Delete user by id")
	@DeleteMapping("/{id}")
	public ResponseEntity<?> delete(
			@ApiParam(value = "User id", example = "developer", required = true) @PathVariable("id") String userId,
			@ApiParam(value = "Hard delete (DB)", name = "hardDelete") @RequestParam(value = "hardDelete", required = false, defaultValue = "false") boolean hardDelete) {
		if (isUserAdminOrSameAsRequest(userId)) {
			log.info("User to be deleted: " + userId);
			if (userService.getUser(userId) == null) {
				return new ResponseEntity<>(USER_STR + userId + DOES_NOT_EXIST, HttpStatus.NOT_FOUND);
			}
			if (!hardDelete) {
				utils.deactivateSessions(userId);
				userService.deleteUser(userId);
			} else {
				log.info("Hard deleting user \"{}\"", userId);
				try {
					utils.deactivateSessions(userId);
					userService.hardDeleteUser(userId);
				} catch (final Exception e) {
					return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}

			log.info("User succesfully deleted");

			return new ResponseEntity<>(HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

	@ApiOperation(value = "Delete multiple users by ids")
	@DeleteMapping
	public ResponseEntity<?> deleteMultiple(
			@ApiParam(value = "User ids", example = "developer,guest,observer", required = true) @RequestBody @Valid List<UserId> userIds) {
		try {
			final List<String> userCollection = new ArrayList<>();
			for (final UserId userId : userIds) {
				if (!isUserAdminOrSameAsRequest(userId.getId())) {
					log.error("Cannot delete admin user from database: " + userId.getId());
					return new ResponseEntity<>(HttpStatus.FORBIDDEN);
				}
				userCollection.add(userId.getId());
			}

			userCollection.forEach((userId) -> {
				utils.deactivateSessions(userId);
			});

			userService.deleteUser(userCollection);
			log.info("Users have been remove from database");

			return new ResponseEntity<>(HttpStatus.OK);
		} catch (final Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Get all active users")
	@GetMapping
	@ApiResponses(@ApiResponse(response = UserSimplified[].class, code = 200, message = "OK"))
	public ResponseEntity<?> getAll() {
		if (utils.isAdministrator()) {
			final List<UserAmplified> users = userService.getAllActiveUsersList();

			return new ResponseEntity<>(users, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

	@ApiOperation(value = "Create new user")
	@PostMapping
	public ResponseEntity<?> create(@ApiParam(value = "User", required = true) @Valid @RequestBody UserSimplified user,
			Errors errors) {
		if (errors.hasErrors()) {
			return ErrorValidationResponse.generateValidationErrorResponse(errors);
		}
		if (utils.isAdministrator()) {

			if (!utils.paswordValidation(user.getPassword())) {
				return new ResponseEntity<>("Password format is not valid", HttpStatus.BAD_REQUEST);
			}
			if (userService.getUser(user.getUsername()) != null) {
				return new ResponseEntity<>("User already exists", HttpStatus.CONFLICT);
			}
			if (!user.getUsername().matches("[a-zA-Z0-9@._]*")) {
				return new ResponseEntity<>("Identification Error: Use alphanumeric characters and '@', '.', '_'",
						HttpStatus.BAD_REQUEST);
			}
			if (userService.emailExists(user.getMail())) {
				return new ResponseEntity<>("Mail already taken", HttpStatus.UNPROCESSABLE_ENTITY);
			}

			final User userDb = new User();
			userDb.setUserId(user.getUsername());
			userDb.setActive(true);
			userDb.setFullName(user.getFullName());
			userDb.setPassword(user.getPassword());
			if (user.getAvatar() != null) {
				userDb.setAvatar(user.getAvatar());
			}
			if (!StringUtils.isEmpty(user.getExtraFields())) {
				userDb.setExtraFields(user.getExtraFields());
			}
			userDb.setEmail(user.getMail());
			try {
				userDb.setRole(userService.getUserRoleById(user.getRole()));
				userService.createUser(userDb);
				return new ResponseEntity<>(HttpStatus.CREATED);
			} catch (final UserServiceException e) {
				log.error("Error creating user", e);
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}

		} else {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

	@ApiOperation(value = "Update an existing user")
	@PutMapping
	public ResponseEntity<?> update(@ApiParam(value = "User", required = true) @Valid @RequestBody UserSimplified user,
			Errors errors) {
		if (errors.hasErrors()) {
			return ErrorValidationResponse.generateValidationErrorResponse(errors);
		}
		if (isUserAdminOrSameAsRequest(user.getUsername())) {
			if (userService.getUser(user.getUsername()) == null) {
				return new ResponseEntity<>(USER_STR + user.getUsername() + DOES_NOT_EXIST, HttpStatus.NOT_FOUND);
			}
			if (!userService.canUserUpdateMail(user.getUsername(), user.getMail())) {
				return new ResponseEntity<>("Mail already taken", HttpStatus.UNPROCESSABLE_ENTITY);
			}
			final User userDb = new User();
			userDb.setUserId(user.getUsername());
			userDb.setActive(true);
			userDb.setFullName(user.getFullName());
			userDb.setEmail(user.getMail());
			userDb.setAvatar(user.getAvatar());
			if (!StringUtils.isEmpty(user.getExtraFields())) {
				userDb.setExtraFields(user.getExtraFields());
			}

			userDb.setRole(userService.getUserRoleById(user.getRole()));

			try {
				if (!StringUtils.isEmpty(user.getPassword()) && utils.paswordValidation(user.getPassword())) {
					userDb.setPassword(user.getPassword());

					final Configuration configuration = configurationService
							.getConfiguration(Configuration.Type.EXPIRATIONUSERS, "default", null);
					final Map<String, Object> ymlExpirationUsersPassConfig = (Map<String, Object>) configurationService
							.fromYaml(configuration.getYmlConfig()).get("Authentication");
					final int numberLastEntriesToCheck = (Integer) ymlExpirationUsersPassConfig
							.get("numberLastEntriesToCheck");

					if (!multitenancyService.isValidPass(user.getUsername(), user.getPassword(),
							numberLastEntriesToCheck)) {
						throw new UserServiceException("Password not valid because it has already been used before");
					}
					userService.updatePassword(userDb);
				} else if (!StringUtils.isEmpty(user.getPassword())) {
					throw new UserServiceException("New password format is not valid");
				}
				userService.updateUser(userDb);
				return new ResponseEntity<>(HttpStatus.OK);
			} catch (final UserServiceException e) {
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}

		} else {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

	@ApiOperation(value = "Update an existing user")
	@PatchMapping
	public ResponseEntity<?> patch(@ApiParam(value = "User", required = true) @RequestBody UserSimplified user,
			Errors errors) {
		if (errors.hasErrors()) {
			return ErrorValidationResponse.generateValidationErrorResponse(errors);
		}
		if (isUserAdminOrSameAsRequest(user.getUsername())) {
			if (userService.getUser(user.getUsername()) == null) {
				return new ResponseEntity<>(USER_STR + user.getUsername() + DOES_NOT_EXIST, HttpStatus.NOT_FOUND);
			}
			try {
				final User u = patchExistingAttributes(userService.getUser(user.getUsername()), user);

				if (!userService.canUserUpdateMail(user.getUsername(), u.getEmail())) {
					return new ResponseEntity<>("Mail already taken", HttpStatus.UNPROCESSABLE_ENTITY);
				}
				userService.updateUser(u);
				return new ResponseEntity<>(HttpStatus.OK);
			} catch (final UserServiceException e) {
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}

		} else {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

	@ApiOperation("Get roles of the platform")
	@GetMapping("/roles")
	@ApiResponses(@ApiResponse(response = String[].class, code = 200, message = "OK"))
	public ResponseEntity<?> getRoles() {
		final List<String> rolesId = new ArrayList<>();
		userService.getAllRoles().forEach(r -> rolesId.add(r.getId()));
		return new ResponseEntity<>(rolesId, HttpStatus.OK);
	}

	@ApiOperation("Activates user")
	@PostMapping("/activate/{userId}")
	public ResponseEntity<String> activate(@PathVariable("userId") String userId) {
		if (isUserAdminOrSameAsRequest(userId)) {
			if (userService.getUser(userId) == null) {
				return new ResponseEntity<>(USER_STR + userId + DOES_NOT_EXIST, HttpStatus.NOT_FOUND);
			}
			userService.activateUser(userService.getUser(userId));
		} else {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation("Changes a password")
	@PostMapping("/{userId}/change-password")
	public ResponseEntity<?> changePassword(@ApiParam("User id") @PathVariable("userId") String userId,
			@ApiParam(value = "Password", required = true) @Valid @RequestBody String password) {
		if (isUserAdminOrSameAsRequest(userId)) {
			if (userService.getUser(userId) == null) {
				return new ResponseEntity<>(USER_STR + userId + DOES_NOT_EXIST, HttpStatus.NOT_FOUND);
			}
			if (!StringUtils.isEmpty(password) && utils.paswordValidation(password)) {
				final User user = userService.getUser(userId);
				user.setPassword(password);
				final Configuration configuration = configurationService
						.getConfiguration(Configuration.Type.EXPIRATIONUSERS, "default", null);
				final Map<String, Object> ymlExpirationUsersPassConfig = (Map<String, Object>) configurationService
						.fromYaml(configuration.getYmlConfig()).get("Authentication");
				final int numberLastEntriesToCheck = (Integer) ymlExpirationUsersPassConfig
						.get("numberLastEntriesToCheck");
				if (!multitenancyService.isValidPass(userId, password, numberLastEntriesToCheck)) {
					throw new UserServiceException("Password not valid because it has already been used before");
				}
				userService.updatePassword(user);
				return new ResponseEntity<>(HttpStatus.OK);

			} else {
				return new ResponseEntity<>("New password format is not valid", HttpStatus.BAD_REQUEST);
			}
		} else {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

	private boolean isUserAdminOrSameAsRequest(String userId) {
		return utils.getUserId().equals(userId) || utils.isAdministrator();
	}

	private User patchExistingAttributes(User user, UserSimplified dto) {
		user.setUserId(dto.getUsername());
		user.setActive(true);
		if (!StringUtils.isEmpty(dto.getFullName())) {
			user.setFullName(dto.getFullName());
		}
		if (!StringUtils.isEmpty(dto.getMail())) {
			user.setEmail(dto.getMail());
		}
		if (!StringUtils.isEmpty(dto.getMail())) {
			user.setAvatar(dto.getAvatar());
		}
		if (!StringUtils.isEmpty(dto.getExtraFields())) {
			user.setExtraFields(dto.getExtraFields());
		}
		if (!StringUtils.isEmpty(dto.getRole())) {
			user.setRole(userService.getUserRoleById(dto.getRole()));
		}
		if (!StringUtils.isEmpty(dto.getPassword()) && utils.paswordValidation(dto.getPassword())) {
			user.setPassword(dto.getPassword());
			final Configuration configuration = configurationService
					.getConfiguration(Configuration.Type.EXPIRATIONUSERS, "default", null);
			final Map<String, Object> ymlExpirationUsersPassConfig = (Map<String, Object>) configurationService
					.fromYaml(configuration.getYmlConfig()).get("Authentication");
			final int numberLastEntriesToCheck = (Integer) ymlExpirationUsersPassConfig.get("numberLastEntriesToCheck");
			if (!multitenancyService.isValidPass(dto.getUsername(), dto.getPassword(), numberLastEntriesToCheck)) {
				throw new UserServiceException("Password not valid because it has already been used before");
			}
			userService.updatePassword(user);
		} else if (!StringUtils.isEmpty(dto.getPassword())) {
			throw new UserServiceException("New password format is not valid");
		}
		return user;
	}

	@ApiOperation(value = "Get all active users filtered by 'extra_fields' attribute")
	@GetMapping("/filter/extra-fields/{jsonPath}/{value}")
	@ApiResponses(@ApiResponse(response = UserSimplified[].class, code = 200, message = "OK"))
	public ResponseEntity<?> getAllFilter(@PathVariable(value = "jsonPath") String jsonPath,
			@PathVariable(value = "value") String value) {
		if (utils.isAdministrator()) {
			final Set<UserSimplified> users = new TreeSet<>();
			userService.getAllActiveUsers().stream().filter(u -> {
				if (!StringUtils.isEmpty(u.getExtraFields())) {
					try {

						final Object v = JsonPath.parse(u.getExtraFields()).read("$." + jsonPath, Object.class);

						return v != null && value.equals(String.valueOf(v));
					} catch (final Exception e) {
						log.debug("Error while reading extra_fields for user {}", u.getUserId(), e);
						return false;
					}

				}
				return false;
			}).forEach(u -> users.add(new UserSimplified(u)));
			return new ResponseEntity<>(users, HttpStatus.OK);
		} else

		{
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

}
