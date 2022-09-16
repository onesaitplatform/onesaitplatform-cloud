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
package com.minsait.onesait.platform.controlpanel.rest.management.restplanner;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.json.JSONArray;
import org.json.JSONObject;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.config.model.RestPlanner;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.client.ClientPlatformService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.restplanner.RestPlannerService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.config.services.usertoken.UserTokenService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.quartz.services.restplanner.RestPlannerQuartzService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Tag(name = "Rest Planner management")
@RestController
@RequestMapping("api/restplanner")
public class RestPlannerManagementController {

	@Autowired
	ClientPlatformService clientPlatformService;
	@Autowired
	RestPlannerService restPlannerService;
	@Autowired
	UserService userService;
	@Autowired
	UserTokenService userTokenService;
	@Autowired
	OntologyService ontologyService;
	@Autowired
	IntegrationResourcesService resourcesService;
	@Autowired
	AppWebUtils utils;
	@Autowired
	private RestPlannerQuartzService restPlannerQuartzService;

	private static final String ENV = "${ENV}";
	private static final String ERROR_STR = "error";
	private static final String STATUS_STR = "status";

	@Operation(summary = "List rest planners")
	@GetMapping(value = "/")
	public ResponseEntity<?> listRestPlanners(@RequestHeader("Authorization") String authorization) {
		try {

			final String userId = utils.getUserId();
			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", userId), HttpStatus.BAD_REQUEST);
			}

			JSONArray responseInfo = new JSONArray();
			List<RestPlanner> restPlanners;
			if (userService.isUserAdministrator(user))
				restPlanners = restPlannerService.getAllRestPlanners();
			else
				restPlanners = restPlannerService.getAllRestPlannersByUser(userId);

			Iterator<RestPlanner> i1 = restPlanners.iterator();
			while (i1.hasNext()) {
				RestPlanner currentRestPlanner = i1.next();
				JSONObject jsonAccess = new JSONObject();
				jsonAccess.put("identification", currentRestPlanner.getIdentification());
				jsonAccess.put("description", currentRestPlanner.getDescription());
				jsonAccess.put("active", currentRestPlanner.isActive());
				jsonAccess.put("method", currentRestPlanner.getMethod());
				jsonAccess.put("URL", currentRestPlanner.getUrl());
				jsonAccess.put("user", currentRestPlanner.getUser());
				responseInfo.put(jsonAccess);
			}

			return new ResponseEntity<>(responseInfo.toString(), HttpStatus.OK);

		} catch (Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@Operation(summary = "Get rest planner by identification")
	@GetMapping(value = "/{restplanner}")
	public ResponseEntity<?> getByIdentification(
			@Parameter(description= "Rest Planner Identification", required = true) @PathVariable("restplanner") String restPlannerId,
			@RequestHeader("Authorization") String authorization) {
		try {

			final RestPlanner restPlanner = restPlannerService.getRestPlannerByIdentification(restPlannerId);

			if (restPlanner == null) {
				return new ResponseEntity<>(String.format("Rest Planner not found with id %s", restPlannerId),
						HttpStatus.BAD_REQUEST);
			}

			final String userId = utils.getUserId();
			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", userId), HttpStatus.BAD_REQUEST);
			}

			JSONArray responseInfo = new JSONArray();

			if (!restPlannerService.hasUserPermission(restPlanner.getId(), userId))
				return new ResponseEntity<>(String.format("User with id %s is not authorized", userId),
						HttpStatus.UNAUTHORIZED);

			RestPlanner currentRestPlanner = restPlannerService.getRestPlannerByIdentification(restPlannerId);

			JSONObject jsonAccess = new JSONObject();
			jsonAccess.put("identification", currentRestPlanner.getIdentification());
			jsonAccess.put("description", currentRestPlanner.getDescription());
			jsonAccess.put("active", currentRestPlanner.isActive());
			jsonAccess.put("method", currentRestPlanner.getMethod());
			jsonAccess.put("URL", currentRestPlanner.getUrl());
			jsonAccess.put("CRON", currentRestPlanner.getCron());
			jsonAccess.put("user", currentRestPlanner.getUser());
			jsonAccess.put("headers", currentRestPlanner.getHeaders());
			jsonAccess.put("body", currentRestPlanner.getBody());
			responseInfo.put(jsonAccess);

			return new ResponseEntity<>(responseInfo.toString(), HttpStatus.OK);

		} catch (Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@Operation(summary = "Create rest planner")
	@PostMapping(value = "/")
	public ResponseEntity<?> newRestPlanner(@Valid @RequestBody RestPlannerDTO restPlannerDTO,
			@RequestHeader("Authorization") String authorization) {

		try {

			final String userId = utils.getUserId();
			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", userId), HttpStatus.BAD_REQUEST);
			}

			if (restPlannerDTO.getIdentification() == null || restPlannerDTO.getIdentification().isEmpty())
				return new ResponseEntity<>(String.format("Missing identification", userId), HttpStatus.BAD_REQUEST);

			if (!restPlannerDTO.getIdentification().matches(AppWebUtils.IDENTIFICATION_PATERN)) {
				return new ResponseEntity<>("Identification Error: Use alphanumeric characters and '-', '_'",
						HttpStatus.BAD_REQUEST);
			}

			final RestPlanner existingRestPlanner = restPlannerService
					.getRestPlannerByIdentification(restPlannerDTO.getIdentification());

			if (existingRestPlanner != null) {
				return new ResponseEntity<>(
						String.format("Rest Planner with id %s already exists", restPlannerDTO.getIdentification()),
						HttpStatus.BAD_REQUEST);
			}

			if (restPlannerDTO.getUrl() == null || restPlannerDTO.getDescription() == null
					|| restPlannerDTO.getMethod() == null || restPlannerDTO.getUrl().equals("")
					|| restPlannerDTO.getDescription().equals("")) {
				return new ResponseEntity<>(String.format("Missing input data"), HttpStatus.BAD_REQUEST);
			}

			String cron = restPlannerDTO.getCron();
			if (!CronExpression.isValidExpression(cron) || cron == null || cron.isEmpty())
				return new ResponseEntity<>(String.format("Wrong cron format"), HttpStatus.BAD_REQUEST);

			RestPlanner restPlanner = new RestPlanner();

			restPlanner.setMethod(restPlannerDTO.getMethod().toString());
			restPlanner.setUrl(restPlannerDTO.getUrl());
			restPlanner.setDescription(restPlannerDTO.getDescription());
			restPlanner.setIdentification(restPlannerDTO.getIdentification());
			restPlanner.setActive(false);
			if (restPlannerDTO.getBody() == null)
				restPlanner.setBody("");
			else
				restPlanner.setBody(restPlannerDTO.getBody());
			restPlanner.setCron(cron);
			restPlanner.setDateFrom(restPlannerDTO.getDateFrom());
			restPlanner.setDateTo(restPlannerDTO.getDateTo());
			if (restPlannerDTO.getHeaders() == null)
				restPlanner.setHeaders("");
			else
				restPlanner.setHeaders(restPlannerDTO.getHeaders());
			restPlanner.setUser(user);
			restPlannerService.createRestPlannerService(restPlanner, user);

			return new ResponseEntity<>("OK", HttpStatus.OK);

		} catch (

		Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@Operation(summary = "Delete rest planner")
	@DeleteMapping(value = "/{restplanner}")
	public ResponseEntity<?> deleteRestPlanner(
			@Parameter(description= "Rest Planner Identification", required = true) @PathVariable("restplanner") String restPlannerId,
			@RequestHeader("Authorization") String authorization) {

		try {
			final RestPlanner restPlanner = restPlannerService.getRestPlannerByIdentification(restPlannerId);

			if (restPlanner == null) {
				return new ResponseEntity<>(String.format("Rest Planner not found with id %s", restPlannerId),
						HttpStatus.BAD_REQUEST);
			}

			final String userId = utils.getUserId();
			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", userId), HttpStatus.BAD_REQUEST);
			}

			if (!restPlannerService.hasUserPermission(restPlanner.getId(), userId)) {
				return new ResponseEntity<>(String.format("User with id %s is not authorized", userId),
						HttpStatus.UNAUTHORIZED);
			}

			restPlannerService.deleteRestPlannerById(restPlanner.getId());

			return new ResponseEntity<>("OK", HttpStatus.OK);

		} catch (

		Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}

	}

	@Operation(summary = "Update rest planner")
	@PutMapping(value = "/{restplanner}")
	public ResponseEntity<?> updateRestPlanner(@Valid @RequestBody RestPlannerDTO restPlannerDTO,
			@RequestHeader("Authorization") String authorization) {

		try {
			final String userId = utils.getUserId();
			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", userId), HttpStatus.BAD_REQUEST);
			}

			final RestPlanner existingRestPlanner = restPlannerService
					.getRestPlannerByIdentification(restPlannerDTO.getIdentification());

			if (existingRestPlanner == null) {
				return new ResponseEntity<>(
						String.format("Rest Planner with id %s does not exist", restPlannerDTO.getIdentification()),
						HttpStatus.BAD_REQUEST);
			}

			String cron = restPlannerDTO.getCron();
			if (cron != null && !cron.isEmpty()) {
				if (!CronExpression.isValidExpression(cron))
					return new ResponseEntity<>(String.format("Wrong cron format"), HttpStatus.BAD_REQUEST);

				existingRestPlanner.setCron(restPlannerDTO.getCron());
			}
			existingRestPlanner.setDateFrom(restPlannerDTO.getDateFrom());
			existingRestPlanner.setDateTo(restPlannerDTO.getDateTo());
			if (restPlannerDTO.getDescription() != null && !restPlannerDTO.getDescription().equals(""))
				existingRestPlanner.setDescription(restPlannerDTO.getDescription());
			if (restPlannerDTO.getMethod() != null)
				existingRestPlanner.setMethod(restPlannerDTO.getMethod().toString());
			if (restPlannerDTO.getUrl() != null && !restPlannerDTO.getUrl().equals(""))
				existingRestPlanner.setUrl(restPlannerDTO.getUrl());
			if (restPlannerDTO.getBody() != null)
				existingRestPlanner.setBody(restPlannerDTO.getBody());
			if (restPlannerDTO.getHeaders() != null)
				existingRestPlanner.setHeaders(restPlannerDTO.getHeaders());
			restPlannerService.updateRestPlanner(existingRestPlanner);

			return new ResponseEntity<>("OK", HttpStatus.OK);

		} catch (Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}

	}

	@Operation(summary = "Start rest planner")
	@PostMapping(value = "/{restplanner}/start")
	public ResponseEntity<?> startRestPlanner(
			@Parameter(description= "Rest Planner Identification", required = true) @PathVariable("restplanner") String restPlannerId,
			@RequestHeader("Authorization") String authorization) {

		try {

			RestPlanner restPlanner = restPlannerService.getRestPlannerByIdentification(restPlannerId);

			if (restPlanner == null) {
				return new ResponseEntity<>(String.format("Rest Planner not found with id %s", restPlannerId),
						HttpStatus.BAD_REQUEST);
			}

			final String userId = utils.getUserId();
			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", userId), HttpStatus.BAD_REQUEST);
			}

			if (!restPlannerService.hasUserPermission(restPlanner.getId(), userId))
				return new ResponseEntity<>(String.format("User with id %s is not authorized", userId),
						HttpStatus.UNAUTHORIZED);

			if (restPlanner.isActive()) {
				return new ResponseEntity<>(String.format("Rest Planner is active"), HttpStatus.BAD_REQUEST);
			} else {
				String newUrl = checkEnvVariable(restPlanner);
				restPlannerQuartzService.schedule(restPlanner, newUrl);
			}
			return new ResponseEntity<>("OK", HttpStatus.OK);

		} catch (

		Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@Operation(summary = "Stop rest planner")
	@PostMapping(value = "/{restplanner}/stop")
	public ResponseEntity<?> stopRestPlanner(
			@Parameter(description= "Rest Planner Identification", required = true) @PathVariable("restplanner") String restPlannerId,
			@RequestHeader("Authorization") String authorization) {

		try {

			RestPlanner restPlanner = restPlannerService.getRestPlannerByIdentification(restPlannerId);

			if (restPlanner == null) {
				return new ResponseEntity<>(String.format("Rest Planner not found with id %s", restPlannerId),
						HttpStatus.BAD_REQUEST);
			}

			final String userId = utils.getUserId();
			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", userId), HttpStatus.BAD_REQUEST);
			}

			if (!restPlannerService.hasUserPermission(restPlanner.getId(), userId))
				return new ResponseEntity<>(String.format("User with id %s is not authorized", userId),
						HttpStatus.UNAUTHORIZED);

			if (!restPlanner.isActive()) {
				return new ResponseEntity<>(String.format("Rest Planner is not active"), HttpStatus.BAD_REQUEST);
			} else {
				restPlannerQuartzService.unschedule(restPlanner);
			}
			return new ResponseEntity<>("OK", HttpStatus.OK);

		} catch (

		Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@Operation(summary = "Execute rest planner")
	@PostMapping(value = "/{restplanner}/execute")
	public ResponseEntity<?> execute(
			@Parameter(description= "Rest Planner Identification", required = true) @PathVariable("restplanner") String restPlannerId,
			@RequestHeader("Authorization") String authorization) {

		try {

			RestPlanner restPlanner = restPlannerService.getRestPlannerByIdentification(restPlannerId);

			if (restPlanner == null) {
				return new ResponseEntity<>(String.format("Rest Planner not found with id %s", restPlannerId),
						HttpStatus.BAD_REQUEST);
			}

			final String userId = utils.getUserId();
			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", userId), HttpStatus.BAD_REQUEST);
			}

			if (!restPlannerService.hasUserPermission(restPlanner.getId(), userId))
				return new ResponseEntity<>(String.format("User with id %s is not authorized", userId),
						HttpStatus.UNAUTHORIZED);

			final Map<String, String> response = new HashMap<>();
			String newUrl = checkEnvVariable(restPlanner);
			String result = restPlannerService.execute(utils.getUserId(), newUrl, restPlanner.getMethod(),
					restPlanner.getBody(), restPlanner.getHeaders());
			if (result.startsWith("OK")) {
				response.put(STATUS_STR, result);
				return new ResponseEntity<>(response, HttpStatus.CREATED);
			} else {
				response.put(STATUS_STR, result);
				return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
			}

		} catch (Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	private String checkEnvVariable(RestPlanner restPlanner) {
		String newUrl = restPlanner.getUrl();
		if (restPlanner.getUrl().contains(ENV)) {
			newUrl = restPlanner.getUrl().split("\\/", 2)[1];
			newUrl = resourcesService.getUrl(Module.DOMAIN, ServiceUrl.BASE) + newUrl;
		}
		return newUrl;
	}
}
