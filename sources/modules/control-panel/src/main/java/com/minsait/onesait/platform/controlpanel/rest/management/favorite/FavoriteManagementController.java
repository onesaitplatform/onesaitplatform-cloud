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
package com.minsait.onesait.platform.controlpanel.rest.management.favorite;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.config.model.Dashboard;
import com.minsait.onesait.platform.config.model.Favorite;
import com.minsait.onesait.platform.config.model.Favorite.Type;
import com.minsait.onesait.platform.config.services.dashboard.DashboardService;
import com.minsait.onesait.platform.config.services.exceptions.FavoriteServiceException;
import com.minsait.onesait.platform.config.services.favorite.FavoriteService;
import com.minsait.onesait.platform.controlpanel.rest.management.favorite.model.FavoriteCreateDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.favorite.model.FavoriteDTO;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;



@RestController
@Tag(name = "Favorites Management")
@RequestMapping("api/favorites")
@ApiResponses({
	@ApiResponse(responseCode = "400",
			description = "Bad request"),
	@ApiResponse(responseCode = "500",
	description = "Internal server error"),
	@ApiResponse(responseCode = "403",
	description = "Forbidden"),
	@ApiResponse(responseCode = "404",
	description = "Not found")
})
public class FavoriteManagementController {

	@Autowired
	private FavoriteService favoriteService;

	@Autowired
	private DashboardService dashboardService;

	@Autowired
	private AppWebUtils utils;

	@ApiResponses(@ApiResponse(responseCode = "200",  description = "OK"))
	@Operation(summary = "Get favorites")
	@GetMapping("/")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> getAllFavorites(
			@RequestParam(value = "type", required = false) Type type) {
		final String user = utils.getUserId();
		final List<Favorite> listResults = favoriteService.findAll(user);
		return new ResponseEntity<>(toFavoriteDTOList(listResults), HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(responseCode = "200",  description = "OK"))
	@Operation(summary = "Create favorite")
	@PostMapping("/")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> create(
			@Parameter(description= "FavoriteCreateDTO") @RequestBody FavoriteCreateDTO favoriteDTO) {

		if (favoriteDTO.getIdentification() == null || favoriteDTO.getIdentification().isEmpty()
				|| favoriteDTO.getType() == null) {
			return new ResponseEntity<>("Missing required fields. Required = [identification, type]",
					HttpStatus.BAD_REQUEST);
		}
		final Type type = favoriteDTO.getType();
		final String favorite = favoriteDTO.getIdentification();

		if(Type.DASHBOARD.equals(type)) {
			final Dashboard dash = dashboardService.getDashboardByIdentification(favorite, utils.getUserId());
			if(dash == null) {
				return new ResponseEntity<>("The dashboard " + favorite + " does not exist.", HttpStatus.NOT_FOUND);
			}
			if (!dashboardService.hasUserViewPermission(dash.getId(), utils.getUserId())) {
				return new ResponseEntity<>("User is unauthorized.", HttpStatus.UNAUTHORIZED);
			}

			final Favorite fav = favoriteService.findByFavoriteIdAndType(dash.getId(), type, utils.getUserId());
			if (fav != null) {
				return new ResponseEntity<>(
						"There is already a favorite with identifier " + favoriteDTO.getIdentification() + " and type " +
								favoriteDTO.getType(), HttpStatus.BAD_REQUEST);
			}
			try {
				favoriteService.create(type, dash.getId(), utils.getUserId());
			} catch (final FavoriteServiceException e) {
				return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
			}
		}

		final ObjectMapper mapper = new ObjectMapper();
		final ObjectNode rootNode = mapper.createObjectNode();
		rootNode.put("message", "The " + favoriteDTO.getType() + " " + favorite + " has been created as a favorite");
		String result = null;
		try {
			result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
		} catch (final JsonProcessingException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Delete favorite by identification and type")
	@DeleteMapping("/{identification}/type/{type}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> delete(
			@Parameter(description= "Favorite identification", required = true) @PathVariable("identification") String favoriteId,
			@Parameter(description= "type", required = true) @PathVariable("type") Type type) {
		if (favoriteId == null || favoriteId.isEmpty()) {
			return new ResponseEntity<>("Missing required fields. Required = [identification]", HttpStatus.BAD_REQUEST);
		}
		final String user = utils.getUserId();
		String favoriteIdentification = null;

		if(Type.DASHBOARD.equals(type)) {
			final Dashboard dash = dashboardService.getDashboardByIdentification(favoriteId, utils.getUserId());
			if(dash == null) {
				return new ResponseEntity<>("The dashboard does not exist.", HttpStatus.NOT_FOUND);
			}

			favoriteIdentification = dash.getId();
			final Favorite favorite = favoriteService.findByFavoriteIdAndType(favoriteIdentification, type, user);
			if (favorite == null) {
				final String eMessage = "The favorite element does not exist. [" + favoriteId + ", " + type.toString() + "]" ;
				return new ResponseEntity<>(eMessage, HttpStatus.NOT_FOUND);
			}
		}

		favoriteService.delete(type, favoriteIdentification, user);

		final ObjectMapper mapper = new ObjectMapper();
		final ObjectNode rootNode = mapper.createObjectNode();
		rootNode.put("message", "Favorite element deleted");
		String result = null;
		try {
			result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
		} catch (final JsonProcessingException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	private List<FavoriteDTO> toFavoriteDTOList(List<Favorite> fs) {
		final List<FavoriteDTO> fsdto = new ArrayList<FavoriteDTO>();
		for (final Favorite f : fs) {
			final FavoriteDTO newF = new FavoriteDTO();
			if(f.getType().equals(Type.DASHBOARD)){
				final String userId = f.getUser().getUserId();
				final Dashboard dashboard = dashboardService.getDashboardById(f.getFavoriteId(), userId);
				if(dashboard != null) {
					final String favoriteId = dashboard.getIdentification();
					newF.setFavoriteId(favoriteId);
					newF.setType(f.getType().toString());
					newF.setUser(userId);
				}
			}
			if(newF != null) {
				fsdto.add(newF);
			}
		}
		return fsdto;
	}
}
