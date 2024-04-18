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
package com.minsait.onesait.platform.controlpanel.rest.management.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformOntology;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.AccessType;
import com.minsait.onesait.platform.config.model.Token;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.client.ClientPlatformService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.rest.management.client.model.ClientPlatformCreate;
import com.minsait.onesait.platform.controlpanel.rest.management.client.model.ClientPlatformDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.model.ErrorValidationResponse;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("api/devices")
@CrossOrigin(origins = "*")
@Api(value = "Client Platform Management", tags = { "Client Platform Management" })
@ApiResponses({ @ApiResponse(code = 400, message = "Bad request"),
		@ApiResponse(code = 500, message = "Internal server error"), @ApiResponse(code = 403, message = "Forbidden") })
public class ClientPlatformManagementController {

	private static final String PATH = "/device";

	private static final String NOT_VALID_STR = "NOT_VALID";

	@Autowired
	private AppWebUtils utils;

	@Autowired
	private UserService userService;

	@Autowired
	private ClientPlatformService clientPlatformService;

	@Autowired
	private OntologyService ontologyService;

	@ApiOperation("Get all devices")
	@GetMapping
	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = ClientPlatformDTO[].class))
	public ResponseEntity<Object> getAllDevices() {

		List<ClientPlatform> list;

		if (utils.isAdministrator()) {
			list = clientPlatformService.getAllClientPlatforms();
		} else {
			final User user = userService.getUserByIdentification(utils.getUserId());
			list = clientPlatformService.getclientPlatformsByUser(user);
		}

		final List<ClientPlatformDTO> returnlist = new ArrayList<>();
		for (final ClientPlatform clientPlatform : list) {
			returnlist.add(parseClientPlatform(clientPlatform));
		}

		return ResponseEntity.ok(returnlist);
	}

	@ApiOperation("Get device by id")
	@GetMapping("/{identification}")
	@ApiResponses({ @ApiResponse(code = 200, message = "OK", response = ClientPlatformDTO.class),
			@ApiResponse(code = 404, message = "Not found") })
	public ResponseEntity<Object> getDeviceByID(
			@ApiParam(value = "identification  ", required = true) @PathVariable("identification") String identification) {
		final ClientPlatform clientPlatform = clientPlatformService.getByIdentification(identification);
		if (clientPlatform == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		if (clientPlatform.getUser().getUserId().equals(utils.getUserId())) {
			return ResponseEntity.ok(parseClientPlatform(clientPlatform));
		}
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
	}

	@ApiOperation("Create new device")
	@PostMapping
	@ApiResponses(@ApiResponse(code = 201, message = "Default token", response = String.class))
	public ResponseEntity<?> createNewDevice(@Valid @RequestBody ClientPlatformCreate clientPlatformCreate,
			Errors errors) {
		if (errors.hasErrors()) {
			return ErrorValidationResponse.generateValidationErrorResponse(errors);
		}

		// Get Ontologies
		final User user = userService.getUserByIdentification(utils.getUserId());
		final Map<Ontology, AccessType> ontologies = new HashMap<>();
		for (final Entry<String, AccessType> entry : clientPlatformCreate.getOntologies().entrySet()) {
			final Ontology ontology = ontologyService.getOntologyByIdentification(entry.getKey(), utils.getUserId());
			if (ontology == null) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Coulnd't found ontology");
			}

			boolean hasPermissions = false;

			switch (entry.getValue()) {
			case ALL:
				hasPermissions = ontologyService.hasUserPermisionForChangeOntology(user, ontology);
				break;
			case INSERT:
				hasPermissions = ontologyService.hasUserPermissionForInsert(user, ontology);
				break;
			case QUERY:
				hasPermissions = ontologyService.hasUserPermissionForQuery(user, ontology);
				break;
			default:
				break;
			}
			if (!hasPermissions) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
			}
			ontologies.put(ontology, entry.getValue());
		}

		// Create base Client Platform
		final ClientPlatform clientPlatform = new ClientPlatform();
		clientPlatform.setDescription(clientPlatformCreate.getDescription());
		clientPlatform.setMetadata(clientPlatformCreate.getMetadata());
		clientPlatform.setIdentification(clientPlatformCreate.getIdentification());
		clientPlatform.setUser(user);

		final Token token = clientPlatformService.createClientTokenWithAccessType(ontologies, clientPlatform);

		return ResponseEntity.status(HttpStatus.CREATED).body(token.getTokenName());
	}

	@ApiOperation(value = "validate Device id with token")
	@RequestMapping(value = "/validate/device/{identification}/token/{token}", method = RequestMethod.GET)
	public ResponseEntity<?> validate(
			@ApiParam(value = "identification  ", required = true) @PathVariable("identification") String identification,
			@ApiParam(value = "Token", required = true) @PathVariable(name = "token") String token) {

		try {
			final ClientPlatform cp = clientPlatformService.getByIdentification(identification);

			final String clientPlatformId = cp.getId();

			final List<Token> tokens = clientPlatformService.getTokensByClientPlatformId(clientPlatformId);

			if (tokens == null || tokens.isEmpty())
				return new ResponseEntity<>(NOT_VALID_STR, HttpStatus.OK);

			final Token result = tokens.stream().filter(x -> token.equals(x.getTokenName())).findAny().orElse(null);

			if (result == null)
				return new ResponseEntity<>(NOT_VALID_STR, HttpStatus.OK);
			else
				return new ResponseEntity<>("VALID", HttpStatus.OK);
		} catch (final Exception e) {
			return new ResponseEntity<>(NOT_VALID_STR, HttpStatus.OK);
		}

	}

	private ClientPlatformDTO parseClientPlatform(ClientPlatform clientPlatform) {

		final Map<String, AccessType> ontologies = new HashMap<>();
		for (final ClientPlatformOntology ontology : clientPlatform.getClientPlatformOntologies()) {
			ontologies.put(ontology.getOntology().getIdentification(), ontology.getAccess());
		}

		final List<String> tokens = new ArrayList<>();
		for (final Token token : clientPlatform.getTokens()) {
			tokens.add(token.getTokenName());
		}

		return ClientPlatformDTO.builder().user(clientPlatform.getUser().getUserId())
				.description(clientPlatform.getDescription()).identification(clientPlatform.getIdentification())
				.metadata(clientPlatform.getMetadata()).ontologies(ontologies).tokens(tokens).build();

	}

}
