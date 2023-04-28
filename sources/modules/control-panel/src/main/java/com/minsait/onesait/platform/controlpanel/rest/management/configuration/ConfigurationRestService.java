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
package com.minsait.onesait.platform.controlpanel.rest.management.configuration;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
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
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.rest.management.configuration.model.ConfigurationSimplified;
import com.minsait.onesait.platform.controlpanel.rest.management.model.ErrorValidationResponse;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;



@Tag(name = "Configuration Management")
@RestController
@ApiResponses({ @ApiResponse(responseCode = "400", description = "Bad request"),
		@ApiResponse(responseCode = "500", description = "Internal server error"), @ApiResponse(responseCode = "403", description = "Forbidden") })
@RequestMapping("/api/configurations")
@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
public class ConfigurationRestService {

	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private UserService userService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private OPResourceService resourceService;

	@Operation(summary="Get all configurations")
	@GetMapping
	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content=@Content(schema=@Schema(implementation=ConfigurationSimplified[].class))))
	public ResponseEntity<?> getAll() {
		final List<ConfigurationSimplified> configurations = configurationService
				.getAllConfigurations(userService.getUser(utils.getUserId())).stream()
				.map(c -> new ConfigurationSimplified(c)).collect(Collectors.toList());

		return new ResponseEntity<>(configurations, HttpStatus.OK);

	}

	@Operation(summary="Get configuration by parameters")
	@GetMapping("/type/{type}/environment/{environment}/realm/{realm}")
	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content=@Content(schema=@Schema(implementation=ConfigurationSimplified.class))))
	@Deprecated
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

	@Operation(summary="Get configuration By ID")
	@GetMapping("/{id}")
	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content=@Content(schema=@Schema(implementation=ConfigurationSimplified.class))))
	public ResponseEntity<?> getById(@ApiParam("id") @PathVariable(value = "id", required = true) String id) {

		final Configuration configuration = configurationService.getConfiguration(id);
		if (configuration != null && (utils.isAdministrator()
				|| configuration.getUser().getUserId().equals(utils.getUserId())
				|| resourceService.hasAccess(utils.getUserId(), configuration.getId(), ResourceAccessType.VIEW)
				|| resourceService.hasAccess(utils.getUserId(), configuration.getId(), ResourceAccessType.MANAGE))) {
			return new ResponseEntity<>(new ConfigurationSimplified(configuration), HttpStatus.OK);
		} else if (configuration == null) {
			return new ResponseEntity<>("No configuration with id " + id, HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>("User has not permission for configuration:  " + id, HttpStatus.FORBIDDEN);
		}
	}

	@Operation(summary="Get configuration By identification")
	@GetMapping("/identification/{identification}")
	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content=@Content(schema=@Schema(implementation=ConfigurationSimplified.class))))
	public ResponseEntity<?> getByIdentification(
			@ApiParam("identification") @PathVariable(value = "identification", required = true) String identification) {

		final Configuration configuration = configurationService.getConfigurationByIdentification(identification);
		if (configuration != null && (utils.isAdministrator()
				|| configuration.getUser().getUserId().equals(utils.getUserId())
				|| resourceService.hasAccess(utils.getUserId(), configuration.getId(), ResourceAccessType.VIEW)
				|| resourceService.hasAccess(utils.getUserId(), configuration.getId(), ResourceAccessType.MANAGE))) {
			return new ResponseEntity<>(new ConfigurationSimplified(configuration), HttpStatus.OK);
		} else if (configuration == null) {
			return new ResponseEntity<>("No configuration with identification " + identification, HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>("User has not permission for configuration:  " + identification,
					HttpStatus.FORBIDDEN);
		}
	}

	@Operation(summary="Get configuration By Identification, Environment and Type")
	@GetMapping("/{identification}/type/{type}/environment/{environment}")
	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content=@Content(schema=@Schema(implementation=ConfigurationSimplified.class))))
	public ResponseEntity<?> getByIdentification(
			@ApiParam("identification") @PathVariable(value = "identification", required = true) String identification,
			@ApiParam("type") @PathVariable(value = "type", required = true) Configuration.Type type,
			@ApiParam("environment") @PathVariable(value = "environment", required = true) String environment, HttpServletResponse response) {
		utils.cleanInvalidSpringCookie(response);
		final Configuration configuration = configurationService.getConfiguration(type, environment, identification);
		if (configuration != null && (utils.isAdministrator()
				|| configuration.getUser().getUserId().equals(utils.getUserId())
				|| resourceService.hasAccess(utils.getUserId(), configuration.getId(), ResourceAccessType.VIEW)
				|| resourceService.hasAccess(utils.getUserId(), configuration.getId(), ResourceAccessType.MANAGE))) {
			return new ResponseEntity<>(new ConfigurationSimplified(configuration), HttpStatus.OK);
		} else if (configuration == null) {
			return new ResponseEntity<>("No configuration with identification: " + identification + " - environment: "
					+ environment + " - type: " + type.name(), HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>("User has not permission.", HttpStatus.FORBIDDEN);
		}

	}

	@Operation(summary="Get configuration by parameters")
	@GetMapping("/type/{type}/real/{realm}")
	@Deprecated
	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content=@Content(schema=@Schema(implementation=ConfigurationSimplified[].class))))
	public ResponseEntity<?> getByIdRealm(
			@ApiParam("Type") @PathVariable(value = "type", required = true) Configuration.Type type,
			@ApiParam("Realm") @PathVariable(value = "realm", required = true) String realm) {

		final Configuration configuration = configurationService.getConfiguration(type, realm);

		if (configuration == null)
			return new ResponseEntity<>("No configuration for realm " + realm + " of type " + type.name(),
					HttpStatus.NOT_FOUND);

		return new ResponseEntity<>(new ConfigurationSimplified(configuration), HttpStatus.OK);

	}

	@Operation(summary="Update configuration")
	@PutMapping("/{id}")
	public ResponseEntity<?> update(@ApiParam("ID") @PathVariable(value = "id", required = true) String id,
			@ApiParam("Configuration") @Valid @RequestBody ConfigurationSimplified config, Errors errors) {
		if (errors.hasErrors()) {
			return ErrorValidationResponse.generateValidationErrorResponse(errors);
		}

		final Configuration configuration = configurationService.getConfiguration(id);
		if (configuration != null && (utils.isAdministrator()
				|| configuration.getUser().getUserId().equals(utils.getUserId())
				|| resourceService.hasAccess(utils.getUserId(), configuration.getId(), ResourceAccessType.MANAGE))) {
			configuration.setDescription(config.getDescription());
			configuration.setEnvironment(config.getEnvironment());
			configuration.setIdentification(config.getIdentification());
			configuration.setType(config.getType());
			configuration.setYmlConfig(config.getYml());
			configurationService.updateConfiguration(configuration);
			return new ResponseEntity<>(HttpStatus.OK);
		} else if (configuration == null) {
			return new ResponseEntity<>("No configuration with id: " + id, HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>("User has not permission.", HttpStatus.FORBIDDEN);
		}

	}

	@Operation(summary="Update configuration")
	@PutMapping
	public ResponseEntity<?> updateByIdentification(
			@ApiParam("Configuration") @Valid @RequestBody ConfigurationSimplified config, Errors errors) {
		if (errors.hasErrors()) {
			return ErrorValidationResponse.generateValidationErrorResponse(errors);
		}

		final Configuration configuration = configurationService.getConfiguration(config.getType(),
				config.getEnvironment(), config.getIdentification());

		if (configuration != null && (utils.isAdministrator()
				|| configuration.getUser().getUserId().equals(utils.getUserId())
				|| resourceService.hasAccess(utils.getUserId(), configuration.getId(), ResourceAccessType.MANAGE))) {
			configuration.setDescription(config.getDescription());
			configuration.setEnvironment(config.getEnvironment());
			configuration.setIdentification(config.getIdentification());
			configuration.setType(config.getType());
			configuration.setYmlConfig(config.getYml());
			configurationService.updateConfiguration(configuration);
			return new ResponseEntity<>(HttpStatus.OK);
		} else if (configuration == null) {
			return new ResponseEntity<>(
					"Configuration with identification: " + config.getIdentification() + " - environment: "
							+ config.getEnvironment() + " - type: " + config.getType().name() + " does not exist",
					HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>("User has not permission.", HttpStatus.FORBIDDEN);
		}
	}

	@Operation(summary="Create configuration")
	@PostMapping
	public ResponseEntity<?> create(
			@ApiParam("Configuration") @RequestBody @Valid ConfigurationSimplified configuration, Errors errors) {
		if (errors.hasErrors()) {
			return ErrorValidationResponse.generateValidationErrorResponse(errors);
		}

		Configuration configurationDb = new Configuration();
		configurationDb.setDescription(configuration.getDescription());
		configurationDb.setEnvironment(configuration.getEnvironment());
		configurationDb.setIdentification(configuration.getIdentification());
		configurationDb.setType(configuration.getType());
		configurationDb.setYmlConfig(configuration.getYml());
		configurationDb.setUser(userService.getUser(utils.getUserId()));
		configurationDb = configurationService.createConfiguration(configurationDb);
		return new ResponseEntity<>(configurationDb.getId(), HttpStatus.CREATED);

	}

	@Operation(summary="Delete configuration")
	@DeleteMapping("/{id}")
	public ResponseEntity<?> delete(@ApiParam("ID") @PathVariable(value = "id", required = true) String id) {
		final Configuration configuration = configurationService.getConfiguration(id);
		if (configuration != null && (utils.isAdministrator()
				|| configuration.getUser().getUserId().equals(utils.getUserId())
				|| resourceService.hasAccess(utils.getUserId(), configuration.getId(), ResourceAccessType.MANAGE))) {
			configurationService.deleteConfiguration(id);
			return new ResponseEntity<>(HttpStatus.OK);
		} else if (configuration == null) {
			return new ResponseEntity<>("Configuration with id: " + id + " does not exist", HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>("User has not permission.", HttpStatus.FORBIDDEN);
		}
	}

	@Operation(summary="Delete configuration by identification, environment and type")
	@DeleteMapping("/{identification}/type/{type}/environment/{environment}")
	public ResponseEntity<?> deleteByIdentification(
			@ApiParam("identification") @PathVariable(value = "identification", required = true) String identification,
			@ApiParam("type") @PathVariable(value = "type", required = true) Configuration.Type type,
			@ApiParam("environment") @PathVariable(value = "environment", required = true) String environment) {
		final Configuration configuration = configurationService.getConfiguration(type, environment, identification);
		if (configuration != null && (utils.isAdministrator()
				|| configuration.getUser().getUserId().equals(utils.getUserId())
				|| resourceService.hasAccess(utils.getUserId(), configuration.getId(), ResourceAccessType.MANAGE))) {
			configurationService.deleteConfiguration(configuration.getId());
			return new ResponseEntity<>(HttpStatus.OK);
		} else if (configuration == null) {
			return new ResponseEntity<>("Configuration with identification: " + identification + " - environment: "
					+ environment + " - type: " + type.name() + " does not exist", HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>("User has not permission.", HttpStatus.FORBIDDEN);
		}
	}

}
