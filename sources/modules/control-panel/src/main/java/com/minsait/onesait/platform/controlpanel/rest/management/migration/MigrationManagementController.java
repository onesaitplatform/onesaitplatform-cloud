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
package com.minsait.onesait.platform.controlpanel.rest.management.migration;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.model.MigrationData;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.migration.DataFromDB;
import com.minsait.onesait.platform.config.services.migration.ExportResult;
import com.minsait.onesait.platform.config.services.migration.MigrationConfiguration;
import com.minsait.onesait.platform.config.services.migration.MigrationErrors;
import com.minsait.onesait.platform.config.services.migration.MigrationService;
import com.minsait.onesait.platform.config.services.migration.SchemaFromDB;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.rest.management.flowengine.FlowengineManagementController;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.libraries.nodered.auth.exception.NoderedAuthException;
import com.mongodb.util.JSON;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@Api(value = "Migration Management", tags = { "Migration management service" })
@RestController
@ApiResponses({ @ApiResponse(code = 400, message = "Bad request"),
		@ApiResponse(code = 500, message = "Internal server error"), @ApiResponse(code = 403, message = "Forbidden") })
@RequestMapping("api/migration")
@Slf4j
public class MigrationManagementController {

	private static final String FORBIDDEN = "Error: Forbidden";
	private static final String USER_FORBIDDEN = "User {} forbidden to export data.";
	private static final String PROJECT = "com.minsait.onesait.platform.config.model.Project";
	private static final String USER = "com.minsait.onesait.platform.config.model.User";
	private static final String DOMAIN_DATA = "domainData";
	private static final String IDENTIFICATION = "identification";

	@Autowired
	private MigrationService migrationService;

	@Autowired
	private UserService userService;

	@Autowired
	private AppWebUtils utils;

	@Autowired
	FlowengineManagementController flowengineController;

	@ApiOperation(value = "Export all. No users or projects will be exported.")
	@GetMapping("/export/all")
	@ApiResponses(@ApiResponse(response = String.class, code = 200, message = "OK"))
	public ResponseEntity<?> exportAll() {

		try {
			if (utils.isAdministrator()) {
				ExportResult allData = migrationService.exportAll();
				allData = this.exportDomain(allData);
				String json = migrationService.getJsonFromData(allData.getData());
				log.info("Exporting all data successfuly.");
				return new ResponseEntity<>(json, HttpStatus.OK);
			} else {
				log.error(USER_FORBIDDEN, utils.getUserId());
				return new ResponseEntity<>(FORBIDDEN, HttpStatus.FORBIDDEN);
			}

		} catch (IllegalArgumentException | IllegalAccessException | JsonProcessingException e) {
			log.error("Error exporting all data from migration service. {}", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Export by user. Only the selected user will be exported and projects will not be exported.")
	@GetMapping("/exportByUser/{user}")
	@ApiResponses(@ApiResponse(response = String.class, code = 200, message = "OK"))
	public ResponseEntity<?> exportByUser(
			@ApiParam(value = "User", required = true) @PathVariable("user") String user) {
		try {
			if (utils.isAdministrator()) {
				ExportResult userData = migrationService.exportUser(userService.getUser(user));
				userData = this.exportDomain(userData);
				String json = migrationService.getJsonFromData(userData.getData());
				log.info("Exporting data by user {} successfuly.", user);
				return new ResponseEntity<>(json, HttpStatus.OK);
			} else {
				log.error(USER_FORBIDDEN, utils.getUserId());
				return new ResponseEntity<>(FORBIDDEN, HttpStatus.FORBIDDEN);
			}
		} catch (IllegalArgumentException | IllegalAccessException | JsonProcessingException e) {
			log.error("Error exporting data of the user {} from migration service. {}", user, e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Export by project. Users will not be exported.")
	@GetMapping("/exportByProject/{project}")
	@ApiResponses(@ApiResponse(response = String.class, code = 200, message = "OK"))
	public ResponseEntity<?> exportByProject(
			@ApiParam(value = "Project", required = true) @PathVariable("project") String project) {

		try {
			if (utils.isAdministrator()) {
				ExportResult userData = migrationService.exportProject(project);
				String json = migrationService.getJsonFromData(userData.getData());
				log.info("Exporting data by project {} successfuly.", project);
				return new ResponseEntity<>(json, HttpStatus.OK);
			} else {
				log.error(USER_FORBIDDEN, utils.getUserId());
				return new ResponseEntity<>(FORBIDDEN, HttpStatus.FORBIDDEN);
			}
		} catch (IllegalArgumentException | IllegalAccessException | JsonProcessingException e) {
			log.error("Error exporting data of the project {} from migration service. {}", project, e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Export users. Only the selected users will be exported.")
	@GetMapping("/exportUsers/{users}")
	@ApiResponses(@ApiResponse(response = String.class, code = 200, message = "OK"))
	public ResponseEntity<?> exportUsers(
			@ApiParam(value = "Users List", required = true) @PathVariable("users") List<String> users) {

		try {
			if (utils.isAdministrator()) {
				ExportResult userData = migrationService.exportUsers(users);
				String json = migrationService.getJsonFromData(userData.getData());
				log.info("Exporting users {} successfuly.", users);
				return new ResponseEntity<>(json, HttpStatus.OK);
			} else {
				log.error(USER_FORBIDDEN, utils.getUserId());
				return new ResponseEntity<>(FORBIDDEN, HttpStatus.FORBIDDEN);
			}
		} catch (IllegalArgumentException | IllegalAccessException | JsonProcessingException e) {
			log.error("Error exporting users {} from migration service. {}", users, e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Export schema.")
	@GetMapping("/export/schema")
	@ApiResponses(@ApiResponse(response = String.class, code = 200, message = "OK"))
	public ResponseEntity<?> exportSchema() {

		try {
			if (utils.isAdministrator()) {
				SchemaFromDB schema = migrationService.exportSchema();
				String json = migrationService.getJsonFromSchema(schema);
				log.info("Exporting schema successfuly.");
				return new ResponseEntity<>(json, HttpStatus.OK);
			} else {
				log.error(USER_FORBIDDEN, utils.getUserId());
				return new ResponseEntity<>(FORBIDDEN, HttpStatus.FORBIDDEN);
			}

		} catch (IllegalArgumentException | JsonProcessingException e) {
			log.error("Error exporting schema from migration service. {}", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Compare schema.")
	@PostMapping("/compare/schema")
	@ApiResponses(@ApiResponse(response = String.class, code = 200, message = "OK"))
	public ResponseEntity<?> compareSchema(
			@ApiParam(value = "Schema", required = true) @Valid @RequestBody String otherSchema) {

		try {
			if (utils.isAdministrator()) {
				SchemaFromDB currentSchema = migrationService.exportSchema();
				String currentSchemaJson = migrationService.getJsonFromSchema(currentSchema);
				String diffs = migrationService.compareSchemas(currentSchemaJson, otherSchema);
				log.info("Compare schema successfuly.");
				return new ResponseEntity<>(diffs, HttpStatus.OK);
			} else {
				log.error(USER_FORBIDDEN, utils.getUserId());
				return new ResponseEntity<>(FORBIDDEN, HttpStatus.FORBIDDEN);
			}
		} catch (IllegalArgumentException | IOException e) {
			log.error("Error comparing schema from migration service. {}", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Import Data.")
	@PostMapping("/import")
	@ApiResponses(@ApiResponse(response = String.class, code = 200, message = "OK"))
	public ResponseEntity<?> importData(@ApiParam(value = "data", required = true) @Valid @RequestBody String json) {

		try {
			if (utils.isAdministrator()) {
				User user = userService.getUser(utils.getUserId());
				DataFromDB data = migrationService.getDataFromJson(json);
				migrationService.storeMigrationData(user, "data.json", "File for import", "data.json",
						json.getBytes(StandardCharsets.UTF_8));
				List<String> classNames = new ArrayList<>();
				for (Class<?> clazz : data.getClasses()) {
					classNames.add(clazz.getName());
				}
				MigrationData migrationData = migrationService.findMigrationData(user);
				if (migrationData == null) {
					log.error("Error: data cannot be null");
					return new ResponseEntity<>("Error: data cannot be null", HttpStatus.BAD_REQUEST);
				}
				MigrationConfiguration config = new MigrationConfiguration();

				for (Class<?> clazz : data.getClasses()) {
					if (classNames.contains(clazz.getName())) {
						for (Serializable id : data.getInstances(clazz)) {
							Map<String, Object> result = data.getInstanceData(clazz, id);
							if (clazz.getName().startsWith(PROJECT)) {
								config.addProject(clazz, id, (Serializable) result.get(IDENTIFICATION));
							} else if (clazz.getName().equals(USER)) {
								config.addUser(clazz, id);
							} else {
								config.add(clazz, id, (Serializable) result.get(IDENTIFICATION),
										(Serializable) result.get("numversion"));
							}
						}
					}
				}

				MigrationErrors errors = new MigrationErrors();
				if (classNames.contains(PROJECT)) {
					errors = migrationService.importData(config, data, true, false);
				} else if (classNames.contains(USER)) {
					errors = migrationService.importData(config, data, false, true);
				} else {
					errors = migrationService.importData(config, data, false, false);
				}

				this.importDomain(data);

				return new ResponseEntity<>(errors.getErrors(), HttpStatus.OK);
			} else {
				log.error(USER_FORBIDDEN, utils.getUserId());
				return new ResponseEntity<>(FORBIDDEN, HttpStatus.FORBIDDEN);
			}
		} catch (Exception e) {
			log.error("Error importing data from migration service. {}", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private ExportResult exportDomain(ExportResult data) {
		// if data has FlowDomain.class then all domain data are exported too
		Iterator<Serializable> iterator = data.getData().getInstances(FlowDomain.class).iterator();
		while (iterator.hasNext()) {
			Serializable id = iterator.next();
			Map<String, Object> obj = data.getData().getInstanceData(FlowDomain.class, id);
			try {
				ResponseEntity<String> result = flowengineController
						.exportFlowDomainByIdentification(obj.get(IDENTIFICATION).toString());
				if (result.getStatusCode().equals(HttpStatus.OK)) {
					obj.put(DOMAIN_DATA, JSON.parse(result.getBody()));
				} else {
					log.error("Error exporting domain data {}. StatusCode {}", obj.get(IDENTIFICATION).toString(),
							result.getStatusCode().name());
					obj.put(DOMAIN_DATA, JSON.parse("[]"));
				}
			} catch (NoderedAuthException e) {
				log.warn("Domain are not started. {}", e.getMessage());
				obj.put(DOMAIN_DATA, JSON.parse("[]"));
			}
		}
		return data;
	}

	private void importDomain(DataFromDB data) {
		// if data has FlowDomain.class then all domain data are exported too
		Iterator<Serializable> iterator = data.getInstances(FlowDomain.class).iterator();
		while (iterator.hasNext()) {
			Serializable id = iterator.next();
			Map<String, Object> obj = data.getInstanceData(FlowDomain.class, id);
			try {
				ResponseEntity<String> result = flowengineController.importFlowDomainToUserAdmin(
						obj.get(DOMAIN_DATA).toString(), obj.get(IDENTIFICATION).toString(),
						obj.get("user").toString());
				if (result.getStatusCode().equals(HttpStatus.OK)) {
					log.info("Domain data imported successfully for domain {} and user {}",
							obj.get(IDENTIFICATION).toString(), obj.get("user").toString());
				} else {
					log.error("Error importing domain data {}. StatusCode {}", obj.get(IDENTIFICATION).toString(),
							result.getStatusCode().name());
				}
			} catch (NoderedAuthException e) {
				log.warn("Domain {} are not started, data are not imported. {}", obj.get(IDENTIFICATION).toString(),
						e.getMessage());
			}
		}
	}

}
