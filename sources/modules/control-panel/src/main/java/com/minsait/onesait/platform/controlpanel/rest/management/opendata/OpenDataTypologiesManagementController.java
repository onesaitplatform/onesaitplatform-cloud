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
package com.minsait.onesait.platform.controlpanel.rest.management.opendata;


import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.config.model.ODTypology;
import com.minsait.onesait.platform.config.services.opendata.typology.TypologyService;
import com.minsait.onesait.platform.controlpanel.rest.management.opendata.model.OpenDataTypologyResponseDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.opendata.model.OpenDataTypologySimplifiedDTO;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Typologies Management")
@RestController
@ApiResponses({ @ApiResponse(responseCode = "400", description = "Bad request"),  @ApiResponse(responseCode = "401", description = "Unathorized"),
	@ApiResponse(responseCode = "500", description = "Internal server error"), @ApiResponse(responseCode = "403", description = "Forbidden") })
@RequestMapping("api/opendata/typologies")
@Slf4j
public class OpenDataTypologiesManagementController {

	private static final String MSG_ERROR_JSON_RESPONSE = "{\"error\":\"%s\"}";
	private static final String MSG_OK_JSON_RESPONSE = "{\"ok\":\"%s\"}";
	private static final String MSG_USER_UNAUTHORIZED = "User is unauthorized";
	private static final String MSG_TYPOLOGY_NOT_EXIST = "Typology does not exist";
	private static final String MSG_TYPOLOGY_EXISTS = "Typology already exists";
	private static final String MSG_TYPOLOGY_DELETED = "Dataset has been deleted successfully";

	@Autowired
	private AppWebUtils utils;
	@Autowired
	private TypologyService typologyService;

	@Operation(summary = "Get all typologies")
	@GetMapping("")
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=OpenDataTypologySimplifiedDTO.class)), responseCode = "200", description = "Ok"))
	public ResponseEntity<Object> getAll() {
		try {

			final List<ODTypology> typologies = typologyService.getAllTypologies();
			if (typologies == null || typologies.isEmpty()) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_TYPOLOGY_NOT_EXIST),HttpStatus.NOT_FOUND);
			}
			final List<OpenDataTypologyResponseDTO> typologiesResponse = new ArrayList<>();
			typologies.forEach(o -> typologiesResponse.add(new OpenDataTypologyResponseDTO(o)));

			return new ResponseEntity<>(typologiesResponse, HttpStatus.OK);
		}
		catch (final Exception e) {
			log.error(String.format("Error getting typology list: %s ", e.getMessage()));
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Get typology by identification")
	@GetMapping("/{identification}")
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=OpenDataTypologyResponseDTO.class)), responseCode = "200", description = "Ok"))
	public ResponseEntity<Object> get(
			@Parameter(description= "Typology identification", required = true) @PathVariable("identification") String identification) {
		try {
			if (!utils.isAdministrator()) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_USER_UNAUTHORIZED),HttpStatus.FORBIDDEN);
			}
			final ODTypology typology = typologyService.getTypologyByIdentification(identification);
			if (typology == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_TYPOLOGY_NOT_EXIST),HttpStatus.NOT_FOUND);
			}
			final OpenDataTypologyResponseDTO typologyResponse = new OpenDataTypologyResponseDTO(typology);

			return new ResponseEntity<>(typologyResponse, HttpStatus.OK);
		}
		catch (final Exception e) {
			log.error(String.format("Error getting typology %s: %s ", identification, e.getMessage()));
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Create new typology")
	@PostMapping
	public ResponseEntity<Object> create(
			@Parameter(description= "TypologyCreate", required = true) @Valid @RequestBody OpenDataTypologySimplifiedDTO typologyCreate) {
		try {
			if (typologyCreate.getIdentification() == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, "Missing required fields. Required = [identification]"),HttpStatus.BAD_REQUEST);
			}
			if (!utils.isAdministrator()) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_USER_UNAUTHORIZED),HttpStatus.FORBIDDEN);
			}
			if(typologyService.getTypologyByIdentification(typologyCreate.getIdentification()) != null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_TYPOLOGY_EXISTS),HttpStatus.BAD_REQUEST);
			}
			final ODTypology typology = createTypologyObjectForCreate(typologyCreate);
			final String typologyId = typologyService.createNewTypology(typology, utils.getUserId());
			if (typologyId == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, "Error creating typology"), HttpStatus.INTERNAL_SERVER_ERROR);
			}
			final ODTypology newTypology = typologyService.getTypologyByIdentification(typologyCreate.getIdentification());
			final OpenDataTypologyResponseDTO typologyResponse = new OpenDataTypologyResponseDTO(newTypology);

			return new ResponseEntity<>(typologyResponse, HttpStatus.OK);
		}
		catch (final Exception e) {
			log.error(String.format("Cannot create typology %s: %s ", typologyCreate.getIdentification(), e.getMessage()));
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Update an existing typology")
	@PutMapping
	public ResponseEntity<Object> update(
			@Parameter(description= "TypologynUpdate", required = true) @Valid @RequestBody OpenDataTypologySimplifiedDTO typologyUpdate) {
		try {
			if (typologyUpdate.getIdentification() == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, "Missing required fields. Required = [identification]"),HttpStatus.BAD_REQUEST);
			}
			if (!utils.isAdministrator()) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_USER_UNAUTHORIZED),HttpStatus.FORBIDDEN);
			}
			final ODTypology typology = typologyService.getTypologyByIdentification(typologyUpdate.getIdentification());
			if(typology == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_TYPOLOGY_NOT_EXIST),HttpStatus.NOT_FOUND);
			}
			final ODTypology newTypology = createTypologyObjectForUpdate(typologyUpdate, typology);
			typologyService.saveTypology(newTypology.getId(), newTypology ,utils.getUserId());
			final ODTypology typologyUpdated = typologyService.getTypologyByIdentification(newTypology.getIdentification());
			final OpenDataTypologyResponseDTO typologyResponse = new OpenDataTypologyResponseDTO(typologyUpdated);

			return new ResponseEntity<>(typologyResponse, HttpStatus.OK);
		}
		catch (final Exception e) {
			log.error(String.format("Cannot update typology %s: %s ", typologyUpdate.getIdentification(), e.getMessage()));
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Delete typology by identification")
	@DeleteMapping("/{identification}")
	public ResponseEntity<String> delete(
			@Parameter(description= "Typology identification", required = true) @PathVariable("identification") String identification) {

		try {
			if (!utils.isAdministrator()) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_USER_UNAUTHORIZED),HttpStatus.FORBIDDEN);
			}
			final ODTypology typology = typologyService.getTypologyByIdentification(identification);
			if(typology == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_TYPOLOGY_NOT_EXIST),HttpStatus.NOT_FOUND);
			}
			typologyService.deleteTypologyByIdentification(identification, utils.getUserId());
			return new ResponseEntity<>(String.format(MSG_OK_JSON_RESPONSE, MSG_TYPOLOGY_DELETED), HttpStatus.OK);
		}
		catch (final Exception e) {
			log.error("Cannot create typology %s: %s ", identification, e.getMessage());
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private ODTypology createTypologyObjectForCreate(OpenDataTypologySimplifiedDTO typologyCreate) {
		final ODTypology typology = new ODTypology();
		typology.setIdentification(typologyCreate.getIdentification());
		typology.setDescription(typologyCreate.getDescription());
		return typology;
	}

	private ODTypology createTypologyObjectForUpdate(OpenDataTypologySimplifiedDTO typologyUpdate, ODTypology oldTypology) {
		final ODTypology typology = new ODTypology();
		typology.setId(oldTypology.getId());
		typology.setIdentification(oldTypology.getIdentification());
		if (typologyUpdate.getDescription() == null) {
			typology.setDescription(oldTypology.getDescription());
		} else {
			typology.setDescription(typologyUpdate.getDescription());
		}
		return typology;
	}

}
