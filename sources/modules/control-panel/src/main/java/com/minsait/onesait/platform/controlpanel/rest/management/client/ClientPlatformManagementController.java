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
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.AccessType;
import com.minsait.onesait.platform.config.model.Token;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.client.ClientPlatformService;
import com.minsait.onesait.platform.config.services.client.dto.DeviceCreateDTO;
import com.minsait.onesait.platform.config.services.client.dto.GenerateTokensResponse;
import com.minsait.onesait.platform.config.services.client.dto.TokenActivationResponse;
import com.minsait.onesait.platform.config.services.deletion.EntityDeletionService;
import com.minsait.onesait.platform.config.services.device.dto.ClientPlatformDTO;
import com.minsait.onesait.platform.config.services.device.dto.TokenDTO;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.token.TokenService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.rest.management.client.model.ClientPlatformCreate;
import com.minsait.onesait.platform.controlpanel.rest.management.model.ErrorValidationResponse;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("api/clientplatform")
@CrossOrigin(origins = "*")
@Api(value = "Client Platform Management", tags = { "Client Platform Management" })
@ApiResponses({ @ApiResponse(code = 400, message = "Bad request"),
		@ApiResponse(code = 500, message = "Internal server error"), @ApiResponse(code = 403, message = "Forbidden") })
public class ClientPlatformManagementController {

	private static final String NOT_VALID_STR = "NOT_VALID";

	@Autowired
	private AppWebUtils utils;

	@Autowired
	private UserService userService;

	@Autowired
	private ClientPlatformService clientPlatformService;
	@Autowired
	private TokenService tokenService;
	@Autowired
	private MultitenancyService multitenancyService;
	@Autowired
	private EntityDeletionService entityDeletionService;

	@Autowired
	private OntologyService ontologyService;

	@ApiOperation("Get all clientplatforms")
	@GetMapping
	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = ClientPlatformDTO[].class))
	public ResponseEntity<Object> getAllClientplatforms() {

		List<ClientPlatform> list;

		if (utils.isAdministrator()) {
			list = clientPlatformService.getAllClientPlatforms();
		} else {
			final User user = userService.getUserByIdentification(utils.getUserId());
			list = clientPlatformService.getclientPlatformsByUser(user);
		}

		final List<ClientPlatformDTO> returnlist = new ArrayList<>();
		for (final ClientPlatform clientPlatform : list) {
			returnlist.add(clientPlatformService.parseClientPlatform(clientPlatform));
		}

		return ResponseEntity.ok(returnlist);
	}

	@ApiOperation("Get clientplatform by id")
	@GetMapping("/{identification}")
	@ApiResponses({ @ApiResponse(code = 200, message = "OK", response = ClientPlatformDTO.class),
			@ApiResponse(code = 404, message = "Not found") })
	public ResponseEntity<Object> getClientplatformByID(
			@ApiParam(value = "identification  ", required = true) @PathVariable("identification") String identification) {
		final ClientPlatform clientPlatform = clientPlatformService.getByIdentification(identification);
		if (clientPlatform == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		final User user = userService.getUser(utils.getUserId());

		if (clientPlatform.getUser().getUserId().equals(utils.getUserId()) || userService.isUserAdministrator(user)) {
			return ResponseEntity.ok(clientPlatformService.parseClientPlatform(clientPlatform));
		}
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
	}

	@ApiOperation("Create new clientplatform")
	@PostMapping
	@ApiResponses(@ApiResponse(code = 201, message = "Default token", response = String.class))
	public ResponseEntity<?> createNewClientplatform(@Valid @RequestBody ClientPlatformCreate clientPlatformCreate,
			Errors errors) {
		if (errors.hasErrors()) {
			return ErrorValidationResponse.generateValidationErrorResponse(errors);
		}

		if (!clientPlatformCreate.getIdentification().matches(AppWebUtils.IDENTIFICATION_PATERN)) {
			return new ResponseEntity<>("Identification Error: Use alphanumeric characters and '-', '_'",
					HttpStatus.BAD_REQUEST);
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

	@ApiOperation("Update clientplatform")
	@PutMapping
	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = String.class))
	public ResponseEntity<?> updateClientplatform(@Valid @RequestBody ClientPlatformCreate clientPlatform,
			Errors errors) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			if (errors.hasErrors()) {
				return ErrorValidationResponse.generateValidationErrorResponse(errors);
			}

			final ClientPlatform ndevice = clientPlatformService
					.getByIdentification(clientPlatform.getIdentification());
			if (ndevice == null) {
				return new ResponseEntity<>("DigitalClient not found", HttpStatus.NOT_FOUND);
			}

			if (!clientPlatformService.hasUserManageAccess(ndevice.getId(), utils.getUserId())) {
				return new ResponseEntity<>("User has not permission to update the DigitalClient",
						HttpStatus.FORBIDDEN);
			}

			final User user = userService.getUserByIdentification(utils.getUserId());
			ArrayNode array = mapper.createArrayNode();
			for (final Entry<String, AccessType> entry : clientPlatform.getOntologies().entrySet()) {
				final Ontology ontology = ontologyService.getOntologyByIdentification(entry.getKey(),
						utils.getUserId());
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
				ObjectNode node = mapper.createObjectNode();
				node.put("id", entry.getKey());
				node.put("access", entry.getValue().name());
				array.add(node);
			}

			final List<Token> tokens = tokenService.getTokens(ndevice);

			DeviceCreateDTO device = new DeviceCreateDTO();
			device.setIdentification(clientPlatform.getIdentification());
			device.setDescription(clientPlatform.getDescription());
			device.setId(ndevice.getId());
			device.setMetadata(clientPlatform.getMetadata());
			device.setTokens(tokens.toString());

			device.setClientPlatformOntologies(mapper.writeValueAsString(array));

			device.setUserId(ndevice.getUser().getUserId());

			clientPlatformService.updateDevice(device, utils.getUserId());
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (JsonProcessingException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Coulnd't update Digital Client");
		}
	}

	@ApiOperation(value = "validate Clientplatform id with token")
	@GetMapping(value = "/validate/clientplatform/{identification}/token/{token}")
	public ResponseEntity<?> validate(
			@ApiParam(value = "identification  ", required = true) @PathVariable("identification") String identification,
			@ApiParam(value = "Token", required = true) @PathVariable(name = "token") String token) {

		try {
			final ClientPlatform cp = clientPlatformService.getByIdentification(identification);

			final String clientPlatformId = cp.getId();

			final List<Token> tokens = clientPlatformService.getTokensByClientPlatformId(clientPlatformId);

			if (tokens == null || tokens.isEmpty()) {
				return new ResponseEntity<>(NOT_VALID_STR, HttpStatus.OK);
			}

			final Token result = tokens.stream().filter(x -> token.equals(x.getTokenName()) && x.isActive()).findAny()
					.orElse(null);

			if (result == null) {
				return new ResponseEntity<>(NOT_VALID_STR, HttpStatus.OK);
			} else {
				return new ResponseEntity<>("VALID", HttpStatus.OK);
			}
		} catch (final Exception e) {
			return new ResponseEntity<>(NOT_VALID_STR, HttpStatus.OK);
		}

	}

	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@ApiOperation(value = "List all device tokens")
	@GetMapping(value = "/{identification}/token")
	public ResponseEntity<List<TokenDTO>> loadClientplatformTokens(
			@PathVariable("identification") String identification) {
		final ClientPlatform clientPlatform = clientPlatformService.getByIdentification(identification);
		if (clientPlatform == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		if (!clientPlatformService.hasUserManageAccess(clientPlatform.getId(), utils.getUserId())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		} else {
			final List<Token> tokens = tokenService.getTokens(clientPlatform);
			if (tokens != null && !tokens.isEmpty()) {
				return ResponseEntity.ok().body(tokens.stream()
						.map(t -> TokenDTO.builder().id(t.getId()).token(t.getTokenName()).active(t.isActive())
								.tenant(multitenancyService.getMasterDeviceToken(t.getTokenName()).getTenant()).build())
						.collect(Collectors.toList()));
			}
			return ResponseEntity.ok().build();
		}
	}

	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@DeleteMapping(value = "/token/{token}")
	public ResponseEntity<TokenActivationResponse> deleteToken(@PathVariable("token") String tokenName) {
		final TokenActivationResponse response = new TokenActivationResponse();
		response.setToken(tokenName);
		try {
			final Token token = tokenService.getTokenByToken(tokenName);
			if (token == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
			}
			if (!clientPlatformService.hasUserManageAccess(token.getClientPlatform().getId(), utils.getUserId())) {
				response.setOk(false);
			} else {
				entityDeletionService.deleteToken(token);
				response.setOk(true);
			}
		} catch (final Exception e) {
			response.setOk(false);
		}
		return ResponseEntity.ok().body(response);
	}

	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@PostMapping(value = "/token/{token}/active/{active}")
	public ResponseEntity<TokenActivationResponse> desactivateToken(@PathVariable("token") String tokenName,
			@PathVariable("active") Boolean active) {
		final TokenActivationResponse response = new TokenActivationResponse();
		response.setRequestedActive(active);
		response.setToken(tokenName);
		try {
			final Token token = tokenService.getTokenByToken(tokenName);
			if (token == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
			}
			tokenService.deactivateToken(token, active);
			response.setOk(true);
		} catch (final Exception e) {
			response.setOk(false);
		}
		return ResponseEntity.ok().body(response);
	}

	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@PostMapping(value = "/{identification}/token")
	public ResponseEntity<GenerateTokensResponse> generateTokens(@PathVariable("identification") String identification,
			@RequestParam(value = "tenant", required = false) String tenant) {
		if (!StringUtils.isEmpty(tenant)) {
			multitenancyService.getTenant(tenant).ifPresent(t -> MultitenancyContextHolder.setTenantName(t.getName()));
		}
		final GenerateTokensResponse response = new GenerateTokensResponse();
		final boolean check = identification == null || identification.equals("");
		response.setOk(check);
		if (!check && tokenService
				.generateTokenForClient(clientPlatformService.getByIdentification(identification)) != null) {
			response.setOk(true);
		}
		return ResponseEntity.ok().body(response);
	}

}
