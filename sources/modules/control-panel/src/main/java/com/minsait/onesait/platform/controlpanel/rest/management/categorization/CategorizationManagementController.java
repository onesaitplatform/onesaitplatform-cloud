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
package com.minsait.onesait.platform.controlpanel.rest.management.categorization;

import java.util.List;

import javax.validation.Valid;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.Categorization;
import com.minsait.onesait.platform.config.model.CategorizationUser;
import com.minsait.onesait.platform.config.model.CategorizationUser.Type;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.categorization.CategorizationService;
import com.minsait.onesait.platform.config.services.categorization.user.CategorizationUserService;
import com.minsait.onesait.platform.config.services.device.dto.ClientPlatformDTO;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.rest.management.categorization.model.CategorizationDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.categorization.model.CategorizationUserDTO;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@Api(value = "Categorization Management", tags = { "Categorization Management" })
@RestController
@ApiResponses({ @ApiResponse(code = 400, message = "Bad request"),
		@ApiResponse(code = 500, message = "Internal server error"), @ApiResponse(code = 403, message = "Forbidden") })
@RequestMapping("api/categorization")
@Slf4j
public class CategorizationManagementController {

	@Autowired
	private AppWebUtils utils;
	@Autowired
	private UserService userService;
	@Autowired
	private CategorizationService categorizationService;
	@Autowired
	private CategorizationUserService categorizationUserService;

	@ApiOperation("Get all categorizations")
	@GetMapping
	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = Categorization[].class))
	public ResponseEntity<Object> getAllCategorizations() {
		List<Categorization> categorizationList;
		if (utils.isAdministrator()) {
			categorizationList = categorizationService.getAllCategorizations();
		} else {
			final User user = userService.getUserByIdentification(utils.getUserId());
			categorizationList = categorizationService.getCategorizationsByUser(user);
		}
		return ResponseEntity.ok(categorizationList);
	}

	@ApiOperation(value = "Get categorization by id")
	@GetMapping("/{identification}")
	@ApiResponses({ @ApiResponse(code = 200, message = "OK", response = Categorization.class), @ApiResponse(code = 404, message = "Not found") })
	public ResponseEntity<Object> getCategorizationByID(
			@ApiParam(value = "identification", required = true) @PathVariable("identification") String identification) {
		final Categorization categorization = categorizationService.getCategorizationByIdentification(identification);
		if (categorization == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		User user = userService.getUser(utils.getUserId());
		if (categorization.getUser().equals(user) || categorizationUserService.findByCategorizationAndUser(categorization, user)!=null) {
			return ResponseEntity.ok(categorization);
		}
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
	}

	@ApiOperation("Create new categorization")
	@PostMapping
	@ApiResponses(@ApiResponse(code = 201, message = "Categorization created", response = String.class))
	public ResponseEntity<?> createCategorization(
			@ApiParam(value = "categorization", required = true) @Valid @RequestBody CategorizationDTO categorizationDTO,
			Errors errors) {
		try {
			
			if (!categorizationDTO.getIdentification().matches(AppWebUtils.IDENTIFICATION_PATERN)) {
			    return new ResponseEntity<>("Identification Error: Use alphanumeric characters and '-', '_'", HttpStatus.BAD_REQUEST);
			}
			
			if (categorizationService.getCategorizationByIdentification(categorizationDTO.getIdentification()) != null) {
				log.error("There is a Categorization Tree with the same Identification");
				throw new GenericOPException("There is a Categorization Tree with the same Identification");
			}
			User user = userService.getUser(utils.getUserId());
			
			categorizationService.createCategorization(categorizationDTO.getIdentification(), categorizationDTO.getJson(), user);
		} catch (Exception e) {
			log.error("Could not create the Categorization tree");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}		
		return new ResponseEntity<>("Categorization created successfully", HttpStatus.OK);
	}
	
	@ApiOperation(value = "Update an existing Categorization")
	@PutMapping
	public ResponseEntity<String> updateCategorization (
			@ApiParam(value = "categorization", required = true) @Valid @RequestBody CategorizationDTO categorization) {
		final Categorization categorizationMemory = categorizationService.getCategorizationByIdentification(categorization.getIdentification());
		if (!categorizationMemory.getUser().getUserId().equals(utils.getUserId())
				&& !utils.getRole().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		try {			
			categorizationService.updateCategorization(categorizationMemory, categorization.getJson());
		} catch (Exception e) {
			log.error("Could not create the Categorization tree");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		return new ResponseEntity<>("Categorization updated successfully", HttpStatus.OK);
	}
	
	@ApiOperation(value = "Delete an existing Categorization by Identification")
	@DeleteMapping("/{identification}")
	public ResponseEntity<String> delete(
			@ApiParam(value = "Categorization identification", required = true) @PathVariable("identification") String categoryIdentification) {
		final Categorization categorization = categorizationService.getCategorizationByIdentification(categoryIdentification);
		if (!categorization.getUser().getUserId().equals(utils.getUserId())
				&& !utils.getRole().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		try {
			categorizationService.deteleConfiguration(categorization.getId());
			return new ResponseEntity<>("Categorization deleted successfully", HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error delating the categorization tree: " + e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
	}
	
	@ApiOperation(value = "Activate selected Categorization for user")
	@GetMapping(value = "/{identification}/activate/{userId}")
	public ResponseEntity<String> active(
			@ApiParam(value = "Ontology identification", required = true) @PathVariable("identification") String categorizationIdentification,
			@ApiParam(value = "User identification", required = true) @PathVariable("userId") String userId) {
		try {
			if (userId.equals(utils.getUserId()) || utils.isAdministrator()) {
				User user = userService.getUser(userId);
				categorizationService.activateByCategoryAndUser(categorizationIdentification, user);
			}
			return new ResponseEntity<>("Categorization activated successfully", HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error activating the tree: " + e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}	
	}	
	
	@ApiOperation(value = "Deactivate selected Categorization for user")
	@GetMapping(value = "/{identification}/deactivate/{userId}")
	public ResponseEntity<String> inactive(
			@ApiParam(value = "Ontology identification", required = true) @PathVariable("identification") String categorizationIdentification,
			@ApiParam(value = "User identification", required = true) @PathVariable("userId") String userId) {
		try {
			if (userId.equals(utils.getUserId()) || utils.isAdministrator()) {
				User user = userService.getUser(userId);
				categorizationService.deactivateByCategoryAndUser(categorizationIdentification, user);
			}
			return new ResponseEntity<>("Categorization deactivated successfully", HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error deactivating the tree: " + e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
	}	
	
	@ApiOperation("Get active categorizations")
	@GetMapping(value = "/getactive")
	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = ClientPlatformDTO[].class))
	public ResponseEntity<Object> getActiveCategorizations() {
		List<Categorization> categorizations = categorizationService.getActiveCategorizations(userService.getUser(utils.getUserId()));
		if (categorizations==null || categorizations.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		return ResponseEntity.ok(categorizations);
	}	
	
	@ApiOperation(value = "Authorize categorization to user")
	@PostMapping(value = "{identification}/authorization")
	public ResponseEntity<String> addAuthorization (
			@ApiParam(value = "Categorization identification", required = true) @PathVariable("identification") String categorizationIdentification,
			@Valid @RequestBody CategorizationUserDTO categorizationUserDTO, Errors errors) {
		
		final Categorization categorization = categorizationService.getCategorizationByIdentification(categorizationIdentification);
		if (!categorization.getUser().getUserId().equals(utils.getUserId()) 
				&& !utils.isAdministrator()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		try {
			User user = userService.getUser(categorizationUserDTO.getUserId());
			Type shareType = CategorizationUser.Type.valueOf(categorizationUserDTO.getAuthorizationType());
			categorizationService.addAuthorization(categorization, user, categorizationUserDTO.getAuthorizationType());
		} catch (Exception e) {
			log.error("Could not create the share authorization");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		return new ResponseEntity<>("User authorizated successfully", HttpStatus.OK);
	}	
	
	@ApiOperation(value = "Deauthorize categorization to user")
	@DeleteMapping(value = "{identification}/authorization/{userId}")
	public ResponseEntity<String> deleteAuthorization(
			@ApiParam(value = "Categorization identification", required = true) @PathVariable("identification") String identification,
			@ApiParam(value = "User id", required = true) @PathVariable("userId") String userId) {
		final Categorization categorization = categorizationService.getCategorizationByIdentification(identification);
		if (!categorization.getUser().getUserId().equals(utils.getUserId())
				&& !utils.getRole().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		try {
			final User user = userService.getUser(userId);
			final CategorizationUser categorizationUser = categorizationUserService.findByCategorizationAndUser(categorization, user);
			categorizationUserService.deleteCategorizationUser(categorizationUser);
		} catch (Exception e) {
			log.error("Could not delete the share authorization");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		return new ResponseEntity<>("User authorization removed successfully", HttpStatus.OK);
	}
	
	@ApiOperation("Get categorization from node")
	@GetMapping(value = "/{identification}/getcategory/{nodeId}")
	public ResponseEntity<String> getCategorization(
			@ApiParam(value = "Categorization identification", required = true) @PathVariable("identification") String identification,
			@ApiParam(value = "Node id", required = true) @PathVariable("nodeId") String nodeId) {
		
		JSONArray category;
		final Categorization categorization = categorizationService.getCategorizationByIdentification(identification);
		
		try {
			final User user = userService.getUser(utils.getUserId());
			final CategorizationUser categorizationUser = categorizationUserService.findByCategorizationAndUser(categorization, user);
			
			if (categorizationUser==null) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		
		try {
			category = categorizationService.getCategoryNode(categorization.getJson(), nodeId);
		} catch (Exception e) {
			log.error("Could not get category");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		return ResponseEntity.ok(category.toString());
	}
	
	@ApiOperation("Get nodes from categorization")
	@PostMapping(value = "/{identification}/elements")
	public ResponseEntity<String> getNodesCategorization(
			@ApiParam(value = "Categorization identification", required = true) @PathVariable("identification") String identification,
			@Valid @RequestBody String categorizationPath, Errors errors) {
		JSONArray resultNodes;
		final Categorization categorization = categorizationService.getCategorizationByIdentification(identification);
		
		try {
			final User user = userService.getUser(utils.getUserId());
			final CategorizationUser categorizationUser = categorizationUserService.findByCategorizationAndUser(categorization, user);
			
			if (categorizationUser==null) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		try {
			resultNodes = categorizationService.getNodesCategory(categorization.getJson(), categorizationPath);
		} catch (Exception e) {
			log.error("Could not get category");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		return ResponseEntity.ok(resultNodes.toString());
	}
	
}
