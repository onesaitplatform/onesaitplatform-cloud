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
package com.minsait.onesait.platform.controlpanel.rest.management.internationalization;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

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
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.services.exceptions.InternationalizationServiceException;
import com.minsait.onesait.platform.config.services.internationalization.InternationalizationDTO;
import com.minsait.onesait.platform.config.services.internationalization.InternationalizationService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Internationalization Management")
@RestController
@ApiResponses({ @ApiResponse(responseCode = "400", description = "Bad request"),
		@ApiResponse(responseCode = "500", description = "Internal server error"),
		@ApiResponse(responseCode = "403", description = "Forbidden") })
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

	@Operation(summary = "Create a new internationalization")
	@PostMapping("/")
	public ResponseEntity<String> create(@RequestBody(required = true) InternationalizationDTO internationalizationDTO)
			throws JsonProcessingException {
		if (log.isDebugEnabled()) {
			log.debug("Recieved request to create a new internationalization {}",
				internationalizationDTO.getIdentification());
		}

		User user = userService.getUserByIdentification(utils.getUserId());

		if (!internationalizationDTO.getIdentification().matches(AppWebUtils.IDENTIFICATION_PATERN)) {
			return new ResponseEntity<>("Identification Error: Use alphanumeric characters and '-', '_'",
					HttpStatus.BAD_REQUEST);
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
			if (e.getError().equals(InternationalizationServiceException.Error.DUPLICATED_INTERNATIONALIZATION)) {
				return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
			} else if (e.getError().equals(InternationalizationServiceException.Error.JSON_ERROR)) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
			}
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	@Operation(summary = "Delete internationalization by identification or Id")
	@DeleteMapping("/{identification}/")
	public ResponseEntity<?> deleteInternationalization(
			@Parameter(description = "Internationalization identification", required = true) @PathVariable("identification") String internationalizationIdentification) {
		try {
			Internationalization internationalization = internationalizationS.findByIdentificationOrId(internationalizationIdentification);
			
			if (internationalization == null) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
			if (!internationalizationS.hasUserPermission(internationalization.getId(), utils.getUserId())) {
				return new ResponseEntity<>(ERROR_USER_NOT_ALLOWED,HttpStatus.FORBIDDEN);
			}	
			internationalizationS.deleteInternationalizationByIdentification(internationalization.getIdentification(),
					utils.getUserId());
		} catch (final InternationalizationServiceException exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>("Internationalization deleted successfully", HttpStatus.OK);
	}

	@Operation(summary = "Get all internationalizations Names")
	@GetMapping("/names")
	public ResponseEntity<List<String>> getAllInternationalizationsNames() {
		log.debug("Get all identifications of internationalizations");
		try {
			List<String> values = internationalizationS.getAllIdentifications();
			return ResponseEntity.ok().body(values);
		} catch (InternationalizationServiceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}
	
	@Operation(summary = "Get all internationalizations")
	@GetMapping("/")
	public ResponseEntity<List<Internationalization>> getAllInternationalizations() {
		log.debug("Get all identifications of internationalizations");
		try {
			List<Internationalization> values = internationalizationS.getAllInternationalizations();
			return ResponseEntity.ok().body(values);
		} catch (InternationalizationServiceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@Operation(summary = "Get jsoni18n from a internationalization")
	@GetMapping("jsoni18n/{identification}/")
	public ResponseEntity<String> getJsoni18n(
			@Parameter(description = "Identification of the internationalization", required = true) @PathVariable("identification") String identification,
			HttpServletResponse response) {
		if (log.isDebugEnabled()) {
			log.debug("Get jsoni18n from the internationalization {}", identification);
		}
		
		utils.cleanInvalidSpringCookie(response);

		try {
			Internationalization internationalization = internationalizationS.findByIdentificationOrId(identification);
			
			if (internationalization == null) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
			if (!internationalizationS.hasUserPermission(internationalization.getId(), utils.getUserId())) {
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}	
			
			return ResponseEntity.ok().body(internationalization.getJsoni18n());
		} catch (InternationalizationServiceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}
	
	@Operation(summary = "Get available languages in an internationalization")
	@GetMapping("{identification}/lang")
	public ResponseEntity<Set<String>> getLangs(
			@Parameter(description = "Identification of the internationalization", required = true) @PathVariable("identification") String identification,
			HttpServletResponse response) {
		if (log.isDebugEnabled()) {
			log.debug("Get jsoni18n from the internationalization {} for forms format", identification);
		}
		
		utils.cleanInvalidSpringCookie(response);
		try {
			Internationalization internationalization = internationalizationS.findByIdentificationOrId(identification);
		
			if (internationalization == null) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
			if (!internationalizationS.hasUserPermission(internationalization.getId(), utils.getUserId())) {
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}			
		
			JSONObject obj = new JSONObject(internationalization.getJsoni18n());
			obj.getJSONObject("languages").keySet();
			return ResponseEntity.ok().body(obj.getJSONObject("languages").keySet());
		} catch (InternationalizationServiceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@Operation(summary = "Get jsoni18n from a internationalization")
	@GetMapping("forms/{identification}/")
	public ResponseEntity<String> getJsoni18nForForms(
			@Parameter(description = "Identification of the internationalization", required = true) @PathVariable("identification") String identification,
			HttpServletResponse response) {
		if (log.isDebugEnabled()) {
			log.debug("Get jsoni18n from the internationalization {} for forms format", identification);
		}

		utils.cleanInvalidSpringCookie(response);

		try {
			Internationalization internationalization = internationalizationS.findByIdentificationOrId(identification);
			
			if (internationalization == null) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
			if (!internationalizationS.hasUserPermission(internationalization.getId(), utils.getUserId())) {
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}	

			JSONObject obj = new JSONObject(internationalization.getJsoni18n());
			JSONObject result = new JSONObject();
			result.append("language", obj.getString("default"));
			result.append("i18n", obj.getJSONObject("languages"));
			return ResponseEntity.ok().body(result.toString());

		} catch (InternationalizationServiceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}
	

	@Operation(summary = "Put new value to jsoni18n")
	@PutMapping("{identification}/")
	public ResponseEntity<String> editJsoni18n(
			@Parameter(description = "Identification of the internationalization", required = true) @PathVariable("identification") String identification,
			@RequestBody(required = true) String jsoni18n) {

		if (log.isDebugEnabled()) {
			log.debug("Recieved request to change jsoni18n value of {} internationalization", identification);
		}		

		Internationalization internationalization = internationalizationS.findByIdentificationOrId(identification);
		
		if (internationalization == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!internationalizationS.hasUserPermission(internationalization.getId(), utils.getUserId())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}	

		try {
			JSONObject obj = new JSONObject(jsoni18n);
			obj.getJSONObject("languages");
			obj.getString("default");
			internationalization.setJsoni18n(jsoni18n);
			internationalizationS.saveInternationalization(internationalization.getId(), internationalization, utils.getUserId());
			return ResponseEntity.ok().body(STATUS_OK);
		} catch (JSONException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()
					+ "\n Json must be like this:  {\"languages\":{\"ES\":{\"Hi\":\"Hola\"}, \"EN\":{\"Hi\":\"Hello\"}}, \"default\":\"ES\"}");
		} catch (InternationalizationServiceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	@Operation(summary = "Get translate of diferents keys values")
	@GetMapping("{identification}/{keys}")
	public ResponseEntity<String> getTranslationsByKeys(
			@Parameter(description = "Identification of the internationalization", required = true) @PathVariable("identification") String identification,
			@RequestParam(required = true) List<String> keys) {
		if (log.isDebugEnabled()) {
			log.debug("Get translations of the internationalization {}", identification);
		}

		try {
			Internationalization internationalization = internationalizationS.findByIdentificationOrId(identification);
			
			if (internationalization == null) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
			if (!internationalizationS.hasUserPermission(internationalization.getId(), utils.getUserId())) {
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}	
			
			JSONObject obj = new JSONObject(internationalization.getJsoni18n());
			Iterator<?> langs = obj.getJSONObject("languages").keys();
			List<?> langsList = IteratorUtils.toList(langs);

			JSONObject translations = new JSONObject();
			for (int j = 0; j < obj.getJSONObject("languages").length(); j++) {
				JSONObject languages = new JSONObject();
				for (int i = 0; i < keys.size(); i++) {
					if (obj.getJSONObject("languages").getJSONObject(langsList.get(j).toString()).has(keys.get(i))) {
						languages.put(keys.get(i), obj.getJSONObject("languages").getJSONObject(langsList.get(j).toString()).getString(keys.get(i)));
					} else {
						languages.put(keys.get(i), "NOT_FOUND");
					}
				}
				translations.put(langsList.get(j).toString(), languages);
			}
			return ResponseEntity.ok().body(translations.toString());
		} catch (InternationalizationServiceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}
	
	@Operation(summary = "Get internationalization by id or identification")
	@GetMapping("/{id}/")
	public ResponseEntity<Object> getInternationalization(
			@Parameter(description = "Id or identification of the internationalization", required = true) @PathVariable("id") String id,
			HttpServletResponse response) {
		if (log.isDebugEnabled()) {
			log.debug("Get internationalization {}", id);
		}
		
		utils.cleanInvalidSpringCookie(response);
		
		try {
			Internationalization internationalization = internationalizationS.findByIdentificationOrId(id);
			if (internationalization == null) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
			if (!internationalizationS.hasUserPermission(internationalization.getId(), utils.getUserId())) {
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}
			return ResponseEntity.ok().body(internationalization);
		} catch (InternationalizationServiceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}
	
	@Operation(summary = "Clone Internationalization by Identification Or Id")
	@PostMapping(value = { "/clone" })
	public ResponseEntity<String> clone(
			@Parameter(description = "Internationalization identification or id to clone") @RequestParam(required = true) String id,
			@Parameter(description = "New internationalization name") @RequestParam(required = true) String newName) {

		final User user = userService.getUser(utils.getUserId());
		if (!internationalizationS.hasUserPermission(id, user.getUserId())) {
			return new ResponseEntity<>(ERROR_USER_NOT_ALLOWED, HttpStatus.FORBIDDEN);
		}
		
		if (!newName.matches(AppWebUtils.IDENTIFICATION_PATERN)) {
			return new ResponseEntity<>("Identification Error: Use alphanumeric characters and '-', '_'",
					HttpStatus.BAD_REQUEST);
		}

		try {
			String internationalizationID = internationalizationS.clone(id, newName, user.getUserId(), true);
			return ResponseEntity.ok().body(internationalizationID);
		} catch (InternationalizationServiceException e) {
			if (e.getError().equals(InternationalizationServiceException.Error.DUPLICATED_INTERNATIONALIZATION)) {
				return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
			} else if (e.getError().equals(InternationalizationServiceException.Error.NOT_FOUND)) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
			}
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}
}
