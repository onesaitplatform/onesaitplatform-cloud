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
package com.minsait.onesait.platform.controlpanel.rest.management.configuration;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.rest.management.configuration.model.ConfigurationSimplified;
import com.minsait.onesait.platform.controlpanel.rest.management.model.ErrorValidationResponse;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "Configuration Management", tags = { "Configuration management service" })
@RestController
@ApiResponses({ @ApiResponse(code = 400, message = "Bad request"),
		@ApiResponse(code = 500, message = "Internal server error"), @ApiResponse(code = 403, message = "Forbidden") })
@RequestMapping("/api/configurations")
@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
public class ConfigurationRestService {

	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private UserService userService;
	@Autowired
	private AppWebUtils utils;

	@ApiOperation("Get all configurations")
	@GetMapping
	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = ConfigurationSimplified[].class))
	public ResponseEntity<?> getAll() {
		final List<ConfigurationSimplified> configurations = configurationService.getAllConfigurations().stream()
				.map(c -> new ConfigurationSimplified(c)).collect(Collectors.toList());

		return new ResponseEntity<>(configurations, HttpStatus.OK);

	}

	@ApiOperation("Get configuration by parameters")
	@GetMapping("/type/{type}/environment/{environment}/realm/{realm}")
	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = ConfigurationSimplified.class))
	public ResponseEntity<?> get(
			@ApiParam("Type") @PathVariable(value = "type", required = true) Configuration.Type type,
			@ApiParam("Environment") @PathVariable(value = "environment", required = true) String environment,
			@ApiParam("Realm") @PathVariable(value = "realm", required = true) String realm) {

		final Configuration configuration = configurationService.getConfiguration(type, environment, realm);

		if (configuration == null)
			return new ResponseEntity<>("No configuration for realm " + realm + " of type " + type.name()
					+ " and environment " + environment, HttpStatus.NOT_FOUND);
		return new ResponseEntity<>(new ConfigurationSimplified(configuration), HttpStatus.OK);

	}

	@ApiOperation("Get configuration")
	@GetMapping("/{id}")
	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = ConfigurationSimplified.class))
	public ResponseEntity<?> getById(@ApiParam("id") @PathVariable(value = "id", required = true) String id) {

		final Configuration configuration = configurationService.getConfiguration(id);
		if (configuration == null)
			return new ResponseEntity<>("No configuration with id " + id, HttpStatus.NOT_FOUND);
		return new ResponseEntity<>(new ConfigurationSimplified(configuration), HttpStatus.OK);

	}

	@ApiOperation("Get configuration by parameters")
	@GetMapping("/type/{type}/real/{realm}")
	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = ConfigurationSimplified[].class))
	public ResponseEntity<?> getByIdRealm(
			@ApiParam("Type") @PathVariable(value = "type", required = true) Configuration.Type type,
			@ApiParam("Realm") @PathVariable(value = "realm", required = true) String realm) {

		final Configuration configuration = configurationService.getConfiguration(type, realm);

		if (configuration == null)
			return new ResponseEntity<>("No configuration for realm " + realm + " of type " + type.name(),
					HttpStatus.NOT_FOUND);

		return new ResponseEntity<>(new ConfigurationSimplified(configuration), HttpStatus.OK);

	}

	@ApiOperation("Update configuration")
	@PutMapping("/{id}")
	public ResponseEntity<?> update(@ApiParam("ID") @PathVariable(value = "id", required = true) String id,
			@ApiParam("Configuration") @Valid @RequestBody ConfigurationSimplified config, Errors errors) {
		if (errors.hasErrors()) {
			return ErrorValidationResponse.generateValidationErrorResponse(errors);
		}

		final Configuration configuration = configurationService.getConfiguration(id);

		if (configuration == null)
			return new ResponseEntity<>("Configuration with id " + id + " does not exist", HttpStatus.BAD_REQUEST);

		configuration.setDescription(config.getDescription());
		configuration.setEnvironment(config.getEnvironment());
		configuration.setSuffix(config.getSuffix());
		configuration.setType(config.getType());
		configuration.setYmlConfig(config.getYml());
		configurationService.updateConfiguration(configuration);
		return new ResponseEntity<>(HttpStatus.OK);

	}

	@ApiOperation("Create configuration")
	@PostMapping
	public ResponseEntity<?> create(
			@ApiParam("Configuration") @RequestBody @Valid ConfigurationSimplified configuration, Errors errors) {
		if (errors.hasErrors()) {
			return ErrorValidationResponse.generateValidationErrorResponse(errors);
		}

		Configuration configurationDb = new Configuration();
		configurationDb.setDescription(configuration.getDescription());
		configurationDb.setEnvironment(configuration.getEnvironment());
		configurationDb.setSuffix(configuration.getSuffix());
		configurationDb.setType(configuration.getType());
		configurationDb.setYmlConfig(configuration.getYml());
		configurationDb.setUser(userService.getUser(utils.getUserId()));
		configurationDb = configurationService.createConfiguration(configurationDb);
		return new ResponseEntity<>(configurationDb.getId(), HttpStatus.CREATED);

	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@ApiOperation("Delete configuration")
	@DeleteMapping("/{id}")
	public ResponseEntity<?> delete(@ApiParam("ID") @PathVariable(value = "id", required = true) String id) {
		final Configuration configuration = configurationService.getConfiguration(id);
		if (configuration == null)
			return new ResponseEntity<>("Configuration with id " + id + " does not exist", HttpStatus.BAD_REQUEST);
		configurationService.deleteConfiguration(id);
		return new ResponseEntity<>(HttpStatus.OK);

	}

}
