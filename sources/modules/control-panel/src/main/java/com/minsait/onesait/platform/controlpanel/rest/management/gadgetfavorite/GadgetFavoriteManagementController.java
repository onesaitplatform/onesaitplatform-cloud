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
package com.minsait.onesait.platform.controlpanel.rest.management.gadgetfavorite;

import java.util.ArrayList;
import java.util.List;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.config.model.Gadget;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.model.GadgetFavorite;
import com.minsait.onesait.platform.config.model.GadgetTemplate;
import com.minsait.onesait.platform.config.services.exceptions.GadgetFavoriteServiceException;
import com.minsait.onesait.platform.config.services.gadgetfavorite.GadgetFavoriteService;
import com.minsait.onesait.platform.controlpanel.rest.management.gadgetfavorite.model.GadgetDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.gadgetfavorite.model.GadgetDatasourceDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.gadgetfavorite.model.GadgetFavoriteCreateDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.gadgetfavorite.model.GadgetFavoriteDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.gadgetfavorite.model.GadgetTemplateDTO;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;



@RestController
@Tag(name = "Favorite Gadget Management")
@RequestMapping("api/favoritegadget")
@ApiResponses({ @ApiResponse(responseCode = "400", description = "Bad request"),
	@ApiResponse(responseCode = "500", description = "Internal server error"), @ApiResponse(responseCode = "403", description = "Forbidden"),
	@ApiResponse(responseCode = "404", description = "Not found") })
public class GadgetFavoriteManagementController {

	@Autowired
	private GadgetFavoriteService gadgetFavoriteService;

	@Autowired
	private AppWebUtils utils;

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Get favorite gadget by identification")
	@GetMapping("/{identification}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> getGadgetFavoriteByIdentification(
			@Parameter(description= "favorite gadget identification", required = true) @PathVariable("identification") String identification) {
		final String user = utils.getUserId();
		final GadgetFavorite gadgetFavorite = gadgetFavoriteService.findByIdentification(identification, user);
		if (gadgetFavorite == null) {
			return new ResponseEntity<>("The favorite gadget does not exist or you do not have administrator permission", HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(toGadgetFavoriteDTO(gadgetFavorite), HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Get all favorite gadgets identifications")
	@GetMapping("/getallidentifications")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> getAllIdentifications(@Parameter(description = "If you do not enter user id it will show you all with the administrator token, if you put user id it will show you those of that user and if you are not an administrator it will show you only yours", name = "userId")@RequestParam(value = "userId", required = false) String userId) {
		final String userlogged = utils.getUserId();	
		final List<String> list = gadgetFavoriteService.getAllIdentifications(userlogged, userId);
		return new ResponseEntity<>(list, HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Get all gadgets favorite ")
	@GetMapping("/getall")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> getAllGadgetsFavorite(@Parameter(description = "If you do not enter user id it will show you all with the administrator token, if you put user id it will show you those of that user and if you are not an administrator it will show you only yours", name = "userId")@RequestParam(value = "userId", required = false) String userId) {
		final String userlogged = utils.getUserId();
		final List<GadgetFavorite> listResults = gadgetFavoriteService.findAllGadgetFavorite(userlogged, userId);
		final List<GadgetFavoriteDTO> list = new ArrayList<GadgetFavoriteDTO>();
		for (final GadgetFavorite gadgetFavorite : listResults) {
			list.add(toGadgetFavoriteDTO(gadgetFavorite));
		}
		return new ResponseEntity<>(list, HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Get all gadgets favorite fields identification and metadata")
	@GetMapping("/getallbyapp")
	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	public ResponseEntity<?> getAllByApp(@Parameter(description = "If you do not enter user id it will show you all with the administrator token, if you put user id it will show you those of that user and if you are not an administrator it will show you only yours", name = "userId")@RequestParam(value = "userId", required = false) String userId) {
		final String userlogged = utils.getUserId();
		final List<GadgetFavorite> listResults = gadgetFavoriteService.findAllGadgetFavorite(userlogged, userId);
		final List<GadgetFavoriteDTO> list = new ArrayList<GadgetFavoriteDTO>();
		for (final GadgetFavorite gadgetFavorite : listResults) {
			list.add(toGadgetFavoriteDTOByApp(gadgetFavorite));
		}
		return new ResponseEntity<>(list, HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Returns boolean whether or not there is a favorite gadget with that identification")
	@GetMapping("/existwithidentification/{identification}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> existWithIdentification(
			@Parameter(description= "favorite gadget identification", required = true) @PathVariable("identification") String identification) {
		return new ResponseEntity<>(gadgetFavoriteService.existWithIdentification(identification), HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Create gadget favorite")
	@PostMapping("/")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> create(
			@Parameter(description= "GadgetFavoriteCreateDTO") @RequestBody GadgetFavoriteCreateDTO gadgetFavoriteDTO) {

		if (gadgetFavoriteDTO.getIdentification() == null || gadgetFavoriteDTO.getIdentification().isEmpty()
				|| gadgetFavoriteDTO.getType() == null || gadgetFavoriteDTO.getType().isEmpty()) {
			return new ResponseEntity<>("Missing required fields. Required = [identification, type]",
					HttpStatus.BAD_REQUEST);
		}
		if (gadgetFavoriteService.existWithIdentification(gadgetFavoriteDTO.getIdentification())) {
			return new ResponseEntity<>(
					"there is already a favorite gadget with identifier " + gadgetFavoriteDTO.getIdentification(),
					HttpStatus.BAD_REQUEST);
		}
		try {
			gadgetFavoriteService.create(gadgetFavoriteDTO.getIdentification(), gadgetFavoriteDTO.getIdGadget(),
					gadgetFavoriteDTO.getIdGadgetTemplate(), gadgetFavoriteDTO.getIdDatasource(),
					gadgetFavoriteDTO.getType(), gadgetFavoriteDTO.getConfig(), gadgetFavoriteDTO.getMetainf(),
					utils.getUserId());
		} catch (final GadgetFavoriteServiceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		final ObjectMapper mapper = new ObjectMapper();
		final ObjectNode rootNode = mapper.createObjectNode();
		rootNode.put("message", "Favorite gadget created");
		String result = null;
		try {
			result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
		} catch (final JsonProcessingException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Update gadget favorite")
	@PutMapping("/")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> update(
			@Parameter(description= "GadgetFavoriteCreateDTO") @RequestBody GadgetFavoriteCreateDTO gadgetFavoriteDTO) {
		if (gadgetFavoriteDTO.getIdentification() == null || gadgetFavoriteDTO.getIdentification().isEmpty()
				|| gadgetFavoriteDTO.getType() == null || gadgetFavoriteDTO.getType().isEmpty()) {
			return new ResponseEntity<>("Missing required fields. Required = [identification, type]",
					HttpStatus.BAD_REQUEST);
		}
		final String user = utils.getUserId();

		final GadgetFavorite gadgetFavorite = gadgetFavoriteService
				.findByIdentification(gadgetFavoriteDTO.getIdentification(), user);
		
		if (gadgetFavorite == null) {
			final String eMessage = " You do not have administrator permission or the favorite gadget does not exist. Identification = "
					+ gadgetFavoriteDTO.getIdentification();
			return new ResponseEntity<>(eMessage, HttpStatus.NOT_FOUND);
		}

		gadgetFavoriteService.update(gadgetFavoriteDTO.getIdentification(), gadgetFavoriteDTO.getIdGadget(),
				gadgetFavoriteDTO.getIdGadgetTemplate(), gadgetFavoriteDTO.getIdDatasource(),
				gadgetFavoriteDTO.getType(), gadgetFavoriteDTO.getConfig(), gadgetFavoriteDTO.getMetainf(),
				utils.getUserId());
		final ObjectMapper mapper = new ObjectMapper();
		final ObjectNode rootNode = mapper.createObjectNode();
		rootNode.put("message", "favorite gadget " + gadgetFavoriteDTO.getIdentification() + " has been updated ");
		String result = null;
		try {
			result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
		} catch (final JsonProcessingException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Delete favorite gadget by identification")
	@DeleteMapping("/{identification}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> delete(
			@Parameter(description= "identification") @PathVariable("identification") String identification) {
		if (identification == null || identification.isEmpty()) {
			return new ResponseEntity<>("Missing required fields. Required = [identification]", HttpStatus.BAD_REQUEST);
		}
		final String user = utils.getUserId();
		final GadgetFavorite gadgetFavorite = gadgetFavoriteService.findByIdentification(identification, user);
		if (gadgetFavorite == null) {
			final String eMessage = "You do not have administrator permission or the favorite gadget does not exist. Identification = " + identification;
			return new ResponseEntity<>(eMessage, HttpStatus.NOT_FOUND);
		}
		gadgetFavoriteService.delete(identification, user);

		final ObjectMapper mapper = new ObjectMapper();
		final ObjectNode rootNode = mapper.createObjectNode();
		rootNode.put("message", "Favorite gadget deleted");
		String result = null;
		try {
			result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
		} catch (final JsonProcessingException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Delete favorite gadget by userId")
	@DeleteMapping("/deleteByUser/{userId}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> deleteByUserId(
			@Parameter(description= "userId") @PathVariable("userId") String userId) {
		if (userId == null || userId.isEmpty()) {
			return new ResponseEntity<>("Missing required fields. Required = [userId]", HttpStatus.BAD_REQUEST);
		}
		final String userlogged = utils.getUserId();
		
		gadgetFavoriteService.deleteByUserId(userlogged, userId);

		final ObjectMapper mapper = new ObjectMapper();
		final ObjectNode rootNode = mapper.createObjectNode();
		rootNode.put("message", "Favorite gadget deleted");
		String result = null;
		try {
			result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
		} catch (final JsonProcessingException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}


	private GadgetFavoriteDTO toGadgetFavoriteDTO(GadgetFavorite gf) {
		final GadgetFavoriteDTO newGf = new GadgetFavoriteDTO();
		newGf.setGadgetTemplate(toGadgetTemplateDTO(gf.getGadgetTemplate()));
		newGf.setDatasource(toDatasourceDTO(gf.getDatasource()));
		newGf.setGadget(toGadgetDTO(gf.getGadget(), gf.getDatasource()));
		newGf.setConfig(gf.getConfig());
		newGf.setIdentification(gf.getIdentification());
		newGf.setType(gf.getType());
		newGf.setUser(gf.getUser().getUserId());
		newGf.setMetainf(gf.getMetainf());
		return newGf;
	}

	private GadgetFavoriteDTO toGadgetFavoriteDTOByApp(GadgetFavorite gf) {
		final GadgetFavoriteDTO newGf = new GadgetFavoriteDTO();
		newGf.setIdentification(gf.getIdentification());
		newGf.setMetainf(gf.getMetainf());
		return newGf;
	}

	private GadgetTemplateDTO toGadgetTemplateDTO(GadgetTemplate template) {
		GadgetTemplateDTO dto = null;
		if (template != null) {
			dto = new GadgetTemplateDTO();
			dto.setIdentification(template.getIdentification());
			dto.setDescription(template.getDescription());
			dto.setHtml(template.getTemplate());
			dto.setJs(template.getTemplateJS());
			dto.setPublic(template.isPublic());
			dto.setUser(template.getUser().getUserId());
		}
		return dto;
	}

	private GadgetDatasourceDTO toDatasourceDTO(GadgetDatasource datasource) {
		GadgetDatasourceDTO dsDTO = null;
		if (datasource != null) {
			dsDTO = new GadgetDatasourceDTO();
			dsDTO.setIdentification(datasource.getIdentification());
			dsDTO.setDescription(datasource.getDescription());
			dsDTO.setQuery(datasource.getQuery());
			dsDTO.setMaxValues(datasource.getMaxvalues());
			dsDTO.setRefresh(datasource.getRefresh());
		}
		return dsDTO;
	}

	private GadgetDTO toGadgetDTO(Gadget gadget, GadgetDatasource datasource) {
		GadgetDTO gDTO = null;
		if (gadget != null) {
			gDTO = new GadgetDTO();
			gDTO.setConfig(gadget.getConfig());
			gDTO.setType(gadget.getType().getId());
			gDTO.setDescription(gadget.getDescription());
			gDTO.setIdentification(gadget.getIdentification());
			gDTO.setUser(gadget.getUser().getUserId());
			gDTO.setDatasource(toDatasourceDTO(datasource));
			gDTO.setId(gadget.getId());
		}
		return gDTO;
	}

}
