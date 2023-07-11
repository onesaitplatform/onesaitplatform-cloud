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
package com.minsait.onesait.platform.controlpanel.rest.management.mapsproject;

import java.util.List;

import javax.validation.Valid;

import org.json.JSONObject;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.exceptions.MapsProjectServiceException;
import com.minsait.onesait.platform.config.services.mapsproject.MapsProjectService;
import com.minsait.onesait.platform.config.services.mapsproject.dto.MapsProjectDTO;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@CrossOrigin(origins = "*")
@Tag(name = "Maps Project Management")
@RequestMapping("api/mapsproject")
@ApiResponses({ @ApiResponse(responseCode = "400", description = "Bad request"),
		@ApiResponse(responseCode = "500", description = "Internal server error"),
		@ApiResponse(responseCode = "403", description = "Forbidden") })
public class MapsProjectRestController {

	private static final String CONSTANT_NOT_FOUND_OR_UNAUTHORIZED = "\"Maps Project not found or unauthorized\"";

	@Autowired
	private MapsProjectService mapsProjectService;
	@Autowired
	private UserRepository userService;
	@Autowired
	private AppWebUtils utils;

	protected ObjectMapper objectMapper;

	@Operation(summary = "Import maps project")
	@PostMapping("/import")
	public ResponseEntity<?> importDashboard(
			@Parameter(description = "Overwrite if exists") @RequestParam(required = false, defaultValue = "false") boolean overwrite,
			@Parameter(description = "maps project json", required = true) @Valid @RequestBody String json,
			Errors errors) {
		try {
			JSONObject js = new JSONObject(json);
			String id = js.getJSONObject("mapConfig").getJSONObject("mainMapOptions").getString("id");

			if (!id.matches(AppWebUtils.IDENTIFICATION_PATERN_SPACES)) {
				return new ResponseEntity<>("Identification Error: Use alphanumeric characters and '-', '_', ' '",
						HttpStatus.BAD_REQUEST);
			}

		} catch (Exception e) {
			return new ResponseEntity<>("Json malformed", HttpStatus.BAD_REQUEST);
		}

		String identificationResult = mapsProjectService.importMapsProject(json, overwrite,
				userService.findByUserId(utils.getUserId()));

		if (identificationResult != null) {

			return new ResponseEntity<>(identificationResult, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(identificationResult, HttpStatus.FORBIDDEN);
		}
	}

	@Operation(summary = "Export maps project by identification")
	@GetMapping("/export/{identification}")
	public ResponseEntity<?> export(
			@Parameter(description = "maps project identification", required = true) @PathVariable("identification") String identification) {
		String mp;
		try {
			mp = mapsProjectService.exportMapsProject(identification, userService.findByUserId(utils.getUserId()));

		} catch (final MapsProjectServiceException e) {
			switch (e.getErrorType()) {
			case NOT_FOUND:
				return new ResponseEntity<>(CONSTANT_NOT_FOUND_OR_UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
			default:
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}

		}
		return new ResponseEntity<>(mp, HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Get Maps Projects")
	@GetMapping("/")

	public ResponseEntity<?> getMapsProjects() {

		final List<MapsProjectDTO> mapsProjects = mapsProjectService.getProjectsForUserDTO(utils.getUserId(), null);
		if (mapsProjects == null) {
			return new ResponseEntity<>("[]", HttpStatus.OK);
		}
		return new ResponseEntity<>(mapsProjects, HttpStatus.OK);

	}

}
