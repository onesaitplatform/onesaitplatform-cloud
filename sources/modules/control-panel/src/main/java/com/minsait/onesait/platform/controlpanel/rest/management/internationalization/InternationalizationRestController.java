/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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
package com.minsait.onesait.platform.controlpanel.rest.management.internationalization;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.IteratorUtils;
import org.json.JSONException;
import org.json.JSONObject;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.minsait.onesait.platform.config.model.Internationalization;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.exceptions.InternationalizationServiceException;
import com.minsait.onesait.platform.config.services.internationalization.InternationalizationDTO;
import com.minsait.onesait.platform.config.services.internationalization.InternationalizationService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@Api(value = "Internationalization Management", tags = { "Internationalization management service" })
@RestController
@ApiResponses({ @ApiResponse(code = 400, message = "Bad request"),
		@ApiResponse(code = 500, message = "Internal server error"), @ApiResponse(code = 403, message = "Forbidden") })
@RequestMapping("api/internationalizations")
@Slf4j
public class InternationalizationRestController {

	@Autowired
	private InternationalizationService internationalizationS;

	@Autowired
	private UserService userService;

	@Autowired
	private AppWebUtils utils;

	private static final String STATUS_OK = "{\"status\": \"ok\"}";
	private static final String ERROR_USER_NOT_ALLOWED = "User is not authorized";

	@ApiOperation(value = "Create a new internationalization")
	@PostMapping("/{identification}/")
	public ResponseEntity<String> create(@RequestBody(required = true) InternationalizationDTO internationalizationDTO)
			throws JsonProcessingException {

		log.debug("Recieved request to create a new internationalization {}",
				internationalizationDTO.getIdentification());

		User user = userService.getUserByIdentification(utils.getUserId());

		if (!internationalizationDTO.getIdentification().matches(AppWebUtils.IDENTIFICATION_PATERN)) {
		    return new ResponseEntity<>("Identification Error: Use alphanumeric characters and '-', '_'", HttpStatus.BAD_REQUEST);
		}
		
		Internationalization internationalization = new Internationalization();

		internationalization.setUser(user);
		internationalization.setIdentification(internationalizationDTO.getIdentification());

		internationalization.setDescription(internationalizationDTO.getDescription());
		internationalization.setJsoni18n(internationalizationDTO.getJsoni18n());
		internationalization.setPublic(internationalizationDTO.isPublic());

		try {
			String internationalizationID = internationalizationS.createNewInternationalization(internationalization,
					user.getUserId(), true);
			return ResponseEntity.ok().body(internationalizationID);
		} catch (InternationalizationServiceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	@ApiOperation(value = "Delete internationalization by identification")
	@DeleteMapping("/{identification}/")
	public ResponseEntity<?> deleteInternationalization(
			@ApiParam(value = "Internationalization identification", required = true) @PathVariable("identification") String internationalizationIdentification) {
		try {
			final User user = userService.getUser(utils.getUserId());
			if (!internationalizationS.hasUserPermission(internationalizationIdentification, user.getUserId())) {
				return new ResponseEntity<>(ERROR_USER_NOT_ALLOWED, HttpStatus.UNAUTHORIZED);
			}
			internationalizationS.deleteInternationalizationByIdentification(internationalizationIdentification,
					utils.getUserId());
		} catch (final InternationalizationServiceException exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>("Identification deleted successfully", HttpStatus.OK);
	}

	@ApiOperation(value = "Get all internationalizations")
	@GetMapping("/")
	public ResponseEntity<List<String>> getAllInternationalizations() {
		log.debug("Get all identifications of internationalizations");
		try {
			List<String> values = internationalizationS.getAllIdentifications();
			return ResponseEntity.ok().body(values);
		} catch (InternationalizationServiceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@ApiOperation(value = "Get jsoni18n from a internationalization")
	@GetMapping("{identification}/")
	public ResponseEntity<String> getJsoni18n(
			@ApiParam(value = "Identification of the internationalization", required = true) @PathVariable("identification") String identification) {
		log.debug("Get jsoni18n from the internationalization {}", identification);

		User user = userService.getUser(utils.getUserId());

		try {
			Internationalization internationalization = internationalizationS
					.getInternationalizationByIdentification(identification, user.getUserId());
			return ResponseEntity.ok().body(internationalization.getJsoni18n());
		} catch (InternationalizationServiceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@ApiOperation(value = "Put new value to jsoni18n")
	@PutMapping("/{identification}/")
	public ResponseEntity<String> editJsoni18n(
			@ApiParam(value = "Identification of the internationalization", required = true) @PathVariable("identification") String identification,
			@RequestBody(required = true) String jsoni18n) {

		log.debug("Recieved request to change jsoni18n value of {} internationalization", identification);

		final User user = userService.getUser(utils.getUserId());
		if (!internationalizationS.hasUserPermission(identification, user.getUserId())) {
			return new ResponseEntity<>(ERROR_USER_NOT_ALLOWED, HttpStatus.UNAUTHORIZED);
		}

		Internationalization internationalization = internationalizationS
				.getInternationalizationByIdentification(identification, user.getUserId());

		try {
			JSONObject obj = new JSONObject(jsoni18n);
			obj.getJSONObject("languages");
			obj.getString("default");
			internationalization.setJsoni18n(jsoni18n);
			internationalizationS.saveInternationalization(internationalization.getId(), internationalization,
					user.getUserId());
			return ResponseEntity.ok().body(STATUS_OK);
		} catch (JSONException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()
					+ "\n Json must be like this:  {\"languages\":{\"ES\":{\"Hi\":\"Hola\"}, \"EN\":{\"Hi\":\"Hello\"}}, \"default\":\"ES\"}");
		} catch (InternationalizationServiceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	@ApiOperation(value = "Get translate of diferents keys values")
	@GetMapping("{identification}/{keys}")
	public ResponseEntity<String> getTranslationsByKeys(
			@ApiParam(value = "Identification of the internationalization", required = true) @PathVariable("identification") String identification,
			@RequestParam(required = true) List<String> keys) {
		log.debug("Get translations of the internationalization {}", identification);

		User user = userService.getUser(utils.getUserId());

		try {
			Internationalization internationalization = internationalizationS
					.getInternationalizationByIdentification(identification, user.getUserId());
			JSONObject obj = new JSONObject(internationalization.getJsoni18n());
			Iterator<?> langs = obj.getJSONObject("languages").keys();
			List<?> langsList = IteratorUtils.toList(langs);

			JSONObject translations = new JSONObject();
			for (int j = 0; j < obj.getJSONObject("languages").length(); j++) {
				JSONObject languages = new JSONObject();
				for (int i = 0; i < keys.size(); i++) {
					languages.put(keys.get(i), obj.getJSONObject("languages").getJSONObject(langsList.get(j).toString())
							.getString(keys.get(i)));
				}
				translations.put(langsList.get(j).toString(), languages);
			}
			return ResponseEntity.ok().body(translations.toString());
		} catch (InternationalizationServiceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}
}
