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
package com.minsait.onesait.platform.serverless.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.serverless.dto.ApplicationCreate;
import com.minsait.onesait.platform.serverless.dto.ApplicationInfo;
import com.minsait.onesait.platform.serverless.dto.ApplicationUpdate;
import com.minsait.onesait.platform.serverless.dto.ErrorResponse;
import com.minsait.onesait.platform.serverless.dto.FunctionCreate;
import com.minsait.onesait.platform.serverless.dto.FunctionInfo;
import com.minsait.onesait.platform.serverless.dto.FunctionUpdate;
import com.minsait.onesait.platform.serverless.exception.ApplicationException;
import com.minsait.onesait.platform.serverless.exception.FnException;
import com.minsait.onesait.platform.serverless.exception.UserNotFoundException;
import com.minsait.onesait.platform.serverless.service.ApplicationService;
import com.minsait.onesait.platform.serverless.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@Slf4j
@SecurityScheme(type = SecuritySchemeType.HTTP, scheme = "bearer", name = "Authorization")
@SecurityRequirement(name = "Authorization")
@Tag(name = "Serverless REST API")
public class RestService {

	private static final String APPLICATION_EXCEPTION = "Application exception";
	private static final String GENERIC_EXCEPTION_WITH_MESSAGE = "Generic exception with message: {}";
	private static final String FN_EXCEPTION_WITH_MESSAGE = "Fn exception with message: {}";
	private static final String USER_NOT_FOUND_EXCEPTION = "User not found exception";
	@Autowired
	private ApplicationService applicationService;

	@PostMapping(value = "/applications", consumes = "application/json")
	@Operation(summary = "Create new application")
	public ResponseEntity<Object> create(@RequestBody ApplicationCreate application) {
		try {
			final ApplicationInfo dto = applicationService.create(application);
			return ResponseEntity.ok(dto);
		} catch (final UserNotFoundException e) {
			log.error(USER_NOT_FOUND_EXCEPTION, e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(), HttpStatus.FORBIDDEN);
		} catch (final ApplicationException e) {
			log.error(APPLICATION_EXCEPTION, e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.valueOf(e.getCode().getCode()));
		} catch (final FnException e) {
			log.error(FN_EXCEPTION_WITH_MESSAGE, e.getMessage(), e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.valueOf(e.getCode().getCode()));
		} catch (final Exception e) {
			log.error(GENERIC_EXCEPTION_WITH_MESSAGE, e.getMessage(), e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Operation(summary = "Create new function")
	@PostMapping(value = "/applications/{appName}/functions", consumes = "application/json")
	public ResponseEntity<Object> createFunction(@RequestBody FunctionCreate function,
			@PathVariable("appName") String appName) {
		try {
			final FunctionInfo dto = applicationService.create(function, appName);
			return ResponseEntity.ok(dto);
		} catch (final UserNotFoundException e) {
			log.error(USER_NOT_FOUND_EXCEPTION, e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(), HttpStatus.FORBIDDEN);
		} catch (final FnException e) {
			log.error(FN_EXCEPTION_WITH_MESSAGE, e.getMessage(), e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.valueOf(e.getCode().getCode()));
		} catch (final ApplicationException e) {
			log.error(APPLICATION_EXCEPTION, e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.valueOf(e.getCode().getCode()));
		} catch (final Exception e) {
			log.error(GENERIC_EXCEPTION_WITH_MESSAGE, e.getMessage(), e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Update an existing application")
	@PutMapping(value = "/applications/{appName}", consumes = "application/json")
	public ResponseEntity<Object> updateApp(@PathVariable("appName") String appName,
			@RequestBody ApplicationUpdate app) {
		try {
			final ApplicationInfo dto = applicationService.update(app, appName);
			return ResponseEntity.ok(dto);
		} catch (final UserNotFoundException e) {
			log.error(USER_NOT_FOUND_EXCEPTION, e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(), HttpStatus.FORBIDDEN);
		} catch (final FnException e) {
			log.error(FN_EXCEPTION_WITH_MESSAGE, e.getMessage(), e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.valueOf(e.getCode().getCode()));
		} catch (final ApplicationException e) {
			log.error(APPLICATION_EXCEPTION, e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.valueOf(e.getCode().getCode()));
		} catch (final Exception e) {
			log.error(GENERIC_EXCEPTION_WITH_MESSAGE, e.getMessage(), e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Delete an existing application")
	@DeleteMapping("/applications/{appName}")
	public ResponseEntity<Object> deleteApp(@PathVariable("appName") String appName) {
		try {
			applicationService.delete(appName);
			return ResponseEntity.ok().build();
		} catch (final UserNotFoundException e) {
			log.error(USER_NOT_FOUND_EXCEPTION, e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(), HttpStatus.FORBIDDEN);
		} catch (final FnException e) {
			log.error(FN_EXCEPTION_WITH_MESSAGE, e.getMessage(), e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.valueOf(e.getCode().getCode()));
		} catch (final ApplicationException e) {
			log.error(APPLICATION_EXCEPTION, e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.valueOf(e.getCode().getCode()));
		} catch (final Exception e) {
			log.error(GENERIC_EXCEPTION_WITH_MESSAGE, e.getMessage(), e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Delete an existing function")
	@DeleteMapping("/applications/{appName}/functions/{fnName}")
	public ResponseEntity<Object> deleteFn(@PathVariable("appName") String appName,
			@PathVariable("fnName") String fnName) {
		try {
			applicationService.deleteFunction(appName, fnName);
			return ResponseEntity.ok().build();
		} catch (final UserNotFoundException e) {
			log.error(USER_NOT_FOUND_EXCEPTION, e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(), HttpStatus.FORBIDDEN);
		} catch (final FnException e) {
			log.error(FN_EXCEPTION_WITH_MESSAGE, e.getMessage(), e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.valueOf(e.getCode().getCode()));
		} catch (final ApplicationException e) {
			log.error(APPLICATION_EXCEPTION, e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.valueOf(e.getCode().getCode()));
		} catch (final Exception e) {
			log.error(GENERIC_EXCEPTION_WITH_MESSAGE, e.getMessage(), e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Update an existing function")
	@PutMapping(value = "/applications/{appName}/functions/{fnName}", consumes = "application/json")
	public ResponseEntity<Object> updateFunction(@PathVariable("appName") String appName,
			@PathVariable("fnName") String fnName, @RequestBody FunctionUpdate function) {
		try {
			final FunctionInfo dto = applicationService.updateFunction(appName, fnName, function);
			return ResponseEntity.ok(dto);
		} catch (final UserNotFoundException e) {
			log.error(USER_NOT_FOUND_EXCEPTION, e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(), HttpStatus.FORBIDDEN);
		} catch (final FnException e) {
			log.error(FN_EXCEPTION_WITH_MESSAGE, e.getMessage(), e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.valueOf(e.getCode().getCode()));
		} catch (final ApplicationException e) {
			log.error(APPLICATION_EXCEPTION, e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.valueOf(e.getCode().getCode()));
		} catch (final Exception e) {
			log.error(GENERIC_EXCEPTION_WITH_MESSAGE, e.getMessage(), e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Update an existing function")
	@PutMapping(value = "/applications/{appName}/functions/{fnName}/update-version", consumes = "application/json")
	public ResponseEntity<Object> updateFunctionsVersion(@PathVariable("appName") String appName,
			@PathVariable("fnName") String fnName, @RequestBody String version) {
		try {
			applicationService.updateFunctionsVersion(appName, fnName, version.replaceAll("\"", ""));
			return ResponseEntity.ok().build();
		} catch (final UserNotFoundException e) {
			log.error(USER_NOT_FOUND_EXCEPTION, e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(), HttpStatus.FORBIDDEN);
		} catch (final FnException e) {
			log.error(FN_EXCEPTION_WITH_MESSAGE, e.getMessage(), e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.valueOf(e.getCode().getCode()));
		} catch (final ApplicationException e) {
			log.error(APPLICATION_EXCEPTION, e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.valueOf(e.getCode().getCode()));
		} catch (final Exception e) {
			log.error(GENERIC_EXCEPTION_WITH_MESSAGE, e.getMessage(), e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Get a function")
	@GetMapping("/applications/{appName}/functions/{fnName}")
	public ResponseEntity<Object> getFunction(@PathVariable("appName") String appName,
			@PathVariable("fnName") String fnName) {
		try {
			final FunctionInfo dto = applicationService.getFunction(appName, fnName);
			return ResponseEntity.ok(dto);
		} catch (final UserNotFoundException e) {
			log.error(USER_NOT_FOUND_EXCEPTION, e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(), HttpStatus.FORBIDDEN);
		} catch (final FnException e) {
			log.error(FN_EXCEPTION_WITH_MESSAGE, e.getMessage(), e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.valueOf(e.getCode().getCode()));
		} catch (final ApplicationException e) {
			log.error(APPLICATION_EXCEPTION, e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.valueOf(e.getCode().getCode()));
		} catch (final Exception e) {
			log.error(GENERIC_EXCEPTION_WITH_MESSAGE, e.getMessage(), e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Get applications by user")
	@GetMapping("/applications/users/{userId}")
	public ResponseEntity<Object> applicationsByUser(@PathVariable("userId") String userId) {
		try {
			return ResponseEntity.ok(applicationService.list(userId));
		} catch (final UserNotFoundException e) {
			log.error(USER_NOT_FOUND_EXCEPTION, e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(), HttpStatus.FORBIDDEN);
		} catch (final FnException e) {
			log.error(FN_EXCEPTION_WITH_MESSAGE, e.getMessage(), e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.valueOf(e.getCode().getCode()));
		} catch (final Exception e) {
			log.error(GENERIC_EXCEPTION_WITH_MESSAGE, e.getMessage(), e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Get applications for current authenticated user")
	@GetMapping("/applications/users/self")
	public ResponseEntity<Object> applicationsSelf() {
		try {
			return ResponseEntity.ok(applicationService.list(SecurityUtils.getCurrentUser()));
		} catch (final UserNotFoundException e) {
			log.error(USER_NOT_FOUND_EXCEPTION, e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(), HttpStatus.FORBIDDEN);
		} catch (final FnException e) {
			log.error(FN_EXCEPTION_WITH_MESSAGE, e.getMessage(), e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.valueOf(e.getCode().getCode()));
		} catch (final Exception e) {
			log.error(GENERIC_EXCEPTION_WITH_MESSAGE, e.getMessage(), e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/applications")
	@PreAuthorize("hasRole('ADMINISTRATOR')")
	@Operation(summary = "Get all applications")
	public ResponseEntity<Object> allApplications() {
		try {
			return ResponseEntity.ok(applicationService.list());
		} catch (final UserNotFoundException e) {
			log.error(USER_NOT_FOUND_EXCEPTION, e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(), HttpStatus.FORBIDDEN);
		} catch (final FnException e) {
			log.error(FN_EXCEPTION_WITH_MESSAGE, e.getMessage(), e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.valueOf(e.getCode().getCode()));
		} catch (final Exception e) {
			log.error(GENERIC_EXCEPTION_WITH_MESSAGE, e.getMessage(), e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/applications/{appName}")
	@Operation(summary = "Get application by its name")
	public ResponseEntity<Object> appByName(@PathVariable("appName") String appName) {
		try {
			return ResponseEntity.ok(applicationService.find(appName));
		} catch (final UserNotFoundException e) {
			log.error(USER_NOT_FOUND_EXCEPTION, e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(), HttpStatus.FORBIDDEN);
		} catch (final ApplicationException e) {
			log.error(APPLICATION_EXCEPTION, e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.valueOf(e.getCode().getCode()));
		} catch (final Exception e) {
			log.error(GENERIC_EXCEPTION_WITH_MESSAGE, e.getMessage(), e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Deploy a function")
	@PostMapping(value = "/applications/{appName}/functions/{fnName}/deploy", consumes = "application/json")
	public ResponseEntity<Object> deployFn(@PathVariable("appName") String appName,
			@PathVariable("fnName") String fnName) {
		try {
			return ResponseEntity.ok(applicationService.deploy(appName, fnName));
		} catch (final UserNotFoundException e) {
			log.error(USER_NOT_FOUND_EXCEPTION, e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(), HttpStatus.FORBIDDEN);
		} catch (final FnException e) {
			log.error(FN_EXCEPTION_WITH_MESSAGE, e.getMessage(), e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.valueOf(e.getCode().getCode()));
		} catch (final ApplicationException e) {
			log.error(APPLICATION_EXCEPTION, e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.valueOf(e.getCode().getCode()));
		} catch (final Exception e) {
			log.error(GENERIC_EXCEPTION_WITH_MESSAGE, e.getMessage(), e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Get function's environment")
	@GetMapping("/applications/{appName}/functions/{fnName}/environment")
	public ResponseEntity<Object> getEnvironment(@PathVariable("appName") String appName,
			@PathVariable("fnName") String fnName) {
		try {
			return ResponseEntity.ok(applicationService.getFunctionsEnvironment(appName, fnName));
		} catch (final UserNotFoundException e) {
			log.error(USER_NOT_FOUND_EXCEPTION, e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(), HttpStatus.FORBIDDEN);
		} catch (final FnException e) {
			log.error(FN_EXCEPTION_WITH_MESSAGE, e.getMessage(), e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.valueOf(e.getCode().getCode()));
		} catch (final ApplicationException e) {
			log.error(APPLICATION_EXCEPTION, e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.valueOf(e.getCode().getCode()));
		} catch (final Exception e) {
			log.error(GENERIC_EXCEPTION_WITH_MESSAGE, e.getMessage(), e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Get function's environment")
	@PutMapping(value = "/applications/{appName}/functions/{fnName}/environment", consumes = "application/json")
	public ResponseEntity<Object> updateEnvironment(@PathVariable("appName") String appName,
			@PathVariable("fnName") String fnName, @RequestBody ObjectNode config) {
		try {
			applicationService.updateFunctionsEnvironmnet(appName, fnName, config);
			return ResponseEntity.ok().build();
		} catch (final UserNotFoundException e) {
			log.error(USER_NOT_FOUND_EXCEPTION, e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(), HttpStatus.FORBIDDEN);
		} catch (final FnException e) {
			log.error(FN_EXCEPTION_WITH_MESSAGE, e.getMessage(), e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.valueOf(e.getCode().getCode()));
		} catch (final ApplicationException e) {
			log.error(APPLICATION_EXCEPTION, e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.valueOf(e.getCode().getCode()));
		} catch (final Exception e) {
			log.error(GENERIC_EXCEPTION_WITH_MESSAGE, e.getMessage(), e);
			return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
