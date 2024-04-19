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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(value = "Favorite Gadget Management", tags = { "Favorite Gadget management service" })
@RequestMapping("api/favoritegadget")
@ApiResponses({ @ApiResponse(code = 400, message = "Bad request"),
		@ApiResponse(code = 500, message = "Internal server error"), @ApiResponse(code = 403, message = "Forbidden"),
		@ApiResponse(code = 404, message = "Not found") })
public class GadgetFavoriteManagementController {

	@Autowired
	private GadgetFavoriteService gadgetFavoriteService;

	@Autowired
	private AppWebUtils utils;

	@ApiResponses(@ApiResponse(code = 200, message = "OK"))
	@ApiOperation(value = "Get favorite gadget by identification")
	@GetMapping("/{identification}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> getGadgetFavoriteByIdentification(
			@ApiParam(value = "favorite gadget identification", required = true) @PathVariable("identification") String identification) {
		String user = utils.getUserId();
		GadgetFavorite gadgetFavorite = gadgetFavoriteService.findByIdentification(identification, user);
		if (gadgetFavorite == null) {
			return new ResponseEntity<>("The gadget favorite does not exist", HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(toGadgetFavoriteDTO(gadgetFavorite), HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(code = 200, message = "OK"))
	@ApiOperation(value = "Get all favorite gadgets identifications")
	@GetMapping("/getallidentifications")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> getAllIdentifications() {
		String user = utils.getUserId();
		List<String> list = gadgetFavoriteService.getAllIdentifications(user);
		return new ResponseEntity<>(list, HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(code = 200, message = "OK"))
	@ApiOperation(value = "Get all gadgets favorite ")
	@GetMapping("/getall")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> getAllGadgetsFavorite() {
		String user = utils.getUserId();
		List<GadgetFavorite> listResults = gadgetFavoriteService.findAll(user);
		List<GadgetFavoriteDTO> list = new ArrayList<GadgetFavoriteDTO>();
		for (GadgetFavorite gadgetFavorite : listResults) {
			list.add(toGadgetFavoriteDTO(gadgetFavorite));
		}
		return new ResponseEntity<>(list, HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(code = 200, message = "OK"))
	@ApiOperation(value = "Returns boolean whether or not there is a favorite gadget with that identification")
	@GetMapping("/existwithidentification/{identification}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> existWithIdentification(
			@ApiParam(value = "favorite gadget identification", required = true) @PathVariable("identification") String identification) {
		return new ResponseEntity<>(gadgetFavoriteService.existWithIdentification(identification), HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(code = 200, message = "OK"))
	@ApiOperation(value = "Create gadget favorite")
	@PostMapping("/")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> create(
			@ApiParam(value = "GadgetFavoriteCreateDTO") @RequestBody GadgetFavoriteCreateDTO gadgetFavoriteDTO) {

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
					gadgetFavoriteDTO.getType(), gadgetFavoriteDTO.getConfig(), utils.getUserId());
		} catch (GadgetFavoriteServiceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootNode = mapper.createObjectNode();
		rootNode.put("message", "Favorite gadget created");
		String result = null;
		try {
			result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(code = 200, message = "OK"))
	@ApiOperation(value = "Update gadget favorite")
	@PutMapping("/")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> update(
			@ApiParam(value = "GadgetFavoriteCreateDTO") @RequestBody GadgetFavoriteCreateDTO gadgetFavoriteDTO) {
		if (gadgetFavoriteDTO.getIdentification() == null || gadgetFavoriteDTO.getIdentification().isEmpty()
				|| gadgetFavoriteDTO.getType() == null || gadgetFavoriteDTO.getType().isEmpty()) {
			return new ResponseEntity<>("Missing required fields. Required = [identification, type]",
					HttpStatus.BAD_REQUEST);
		}
		final String user = utils.getUserId();

		final GadgetFavorite gadgetFavorite = gadgetFavoriteService
				.findByIdentification(gadgetFavoriteDTO.getIdentification(), user);
		if (gadgetFavorite == null) {
			String eMessage = "The favorite gadget does not exist. Identification = "
					+ gadgetFavoriteDTO.getIdentification();
			return new ResponseEntity<>(eMessage, HttpStatus.NOT_FOUND);
		}

		gadgetFavoriteService.update(gadgetFavoriteDTO.getIdentification(), gadgetFavoriteDTO.getIdGadget(),
				gadgetFavoriteDTO.getIdGadgetTemplate(), gadgetFavoriteDTO.getIdDatasource(),
				gadgetFavoriteDTO.getType(), gadgetFavoriteDTO.getConfig(), utils.getUserId());
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootNode = mapper.createObjectNode();
		rootNode.put("message", "favorite gadget " + gadgetFavoriteDTO.getIdentification() + " has been updated ");
		String result = null;
		try {
			result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(code = 200, message = "OK"))
	@ApiOperation(value = "Delete favorite gadget by identification")
	@DeleteMapping("/{identification}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> delete(
			@ApiParam(value = "identification") @PathVariable("identification") String identification) {
		if (identification == null || identification.isEmpty()) {
			return new ResponseEntity<>("Missing required fields. Required = [identification]", HttpStatus.BAD_REQUEST);
		}
		String user = utils.getUserId();
		final GadgetFavorite gadgetFavorite = gadgetFavoriteService.findByIdentification(identification, user);
		if (gadgetFavorite == null) {
			String eMessage = "The favorite gadget does not exist. Identification = " + identification;
			return new ResponseEntity<>(eMessage, HttpStatus.NOT_FOUND);
		}
		gadgetFavoriteService.delete(identification, user);

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootNode = mapper.createObjectNode();
		rootNode.put("message", "Favorite gadget deleted");
		String result = null;
		try {
			result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	private GadgetFavoriteDTO toGadgetFavoriteDTO(GadgetFavorite gf) {
		GadgetFavoriteDTO newGf = new GadgetFavoriteDTO();
		newGf.setGadgetTemplate(toGadgetTemplateDTO(gf.getGadgetTemplate()));
		newGf.setDatasource(toDatasourceDTO(gf.getDatasource()));
		newGf.setGadget(toGadgetDTO(gf.getGadget(), gf.getDatasource()));
		newGf.setConfig(gf.getConfig());
		newGf.setIdentification(gf.getIdentification());
		newGf.setType(gf.getType());
		newGf.setUser(gf.getUser().getUserId());
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
			gDTO.setType(gadget.getType());
			gDTO.setDescription(gadget.getDescription());
			gDTO.setIdentification(gadget.getIdentification());
			gDTO.setUser(gadget.getUser().getUserId());
			gDTO.setDatasource(toDatasourceDTO(datasource));
			gDTO.setId(gadget.getId());
		}
		return gDTO;
	}

}