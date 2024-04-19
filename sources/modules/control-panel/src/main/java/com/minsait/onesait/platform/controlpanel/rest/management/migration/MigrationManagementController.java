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
package com.minsait.onesait.platform.controlpanel.rest.management.migration;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.ws.rs.ClientErrorException;

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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.ApiQueryParameter;
import com.minsait.onesait.platform.config.model.AppChildExport;
import com.minsait.onesait.platform.config.model.AppExport;
import com.minsait.onesait.platform.config.model.AppRoleChildExport;
import com.minsait.onesait.platform.config.model.AppRoleExport;
import com.minsait.onesait.platform.config.model.AppUserExport;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformInstance;
import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.model.MigrationData;
import com.minsait.onesait.platform.config.model.MigrationData.DataType;
import com.minsait.onesait.platform.config.model.MigrationData.Status;
import com.minsait.onesait.platform.config.model.Notebook;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.OntologyTimeSeries;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesProperty;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow;
import com.minsait.onesait.platform.config.model.OntologyVirtual;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;
import com.minsait.onesait.platform.config.model.Pipeline;
import com.minsait.onesait.platform.config.model.ProjectExport;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessExport;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.services.migration.DataFromDB;
import com.minsait.onesait.platform.config.services.migration.ExportResult;
import com.minsait.onesait.platform.config.services.migration.Instance;
import com.minsait.onesait.platform.config.services.migration.MigrationConfiguration;
import com.minsait.onesait.platform.config.services.migration.MigrationError;
import com.minsait.onesait.platform.config.services.migration.MigrationErrors;
import com.minsait.onesait.platform.config.services.migration.MigrationService;
import com.minsait.onesait.platform.config.services.migration.SchemaFromDB;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.help.migration.MigrationHelper;
import com.minsait.onesait.platform.controlpanel.rest.management.dataflow.DataFlowStorageManagementController;
import com.minsait.onesait.platform.controlpanel.rest.management.flowengine.FlowengineManagementController;
import com.minsait.onesait.platform.controlpanel.rest.management.notebook.NotebookManagementController;
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
	private static final String PROJECT_EXPORT = "com.minsait.onesait.platform.config.model.ProjectExport";
	private static final String USER = "com.minsait.onesait.platform.config.model.UserExport";
	private static final String DOMAIN_DATA = "domainData";
	private static final String IDENTIFICATION = "identification";
	private static final String NOTEBOOK_DATA = "notebookData";
	private static final String IDZEP = "idzep";
	private static final String DATAFLOW_DATA = "dataflowData";
	private static final String NOTEBOOK = "com.minsait.onesait.platform.config.model.Notebook";
	private static final String DATAFLOW = "com.minsait.onesait.platform.config.model.Pipeline";
	private static final String FLOW_DOMAIN = "com.minsait.onesait.platform.config.model.FlowDomain";

	@Autowired
	private MigrationService migrationService;

	@Autowired
	private UserService userService;

	@Autowired
	private AppWebUtils utils;

	@Autowired
	FlowengineManagementController flowengineController;

	@Autowired
	NotebookManagementController notebookController;

	@Autowired
	DataFlowStorageManagementController dataflowController;

	@Autowired
	private MigrationHelper migrationHelper;

	@ApiOperation(value = "Export all. No users or projects will be exported.")
	@GetMapping("/export/all")
	@ApiResponses(@ApiResponse(response = String.class, code = 200, message = "OK"))
	public ResponseEntity<?> exportAll() {

		try {
			if (utils.isAdministrator()) {
				ExportResult allData = migrationService.exportAll();
				allData = exportDomain(allData);
				allData = exportNotebooks(allData);
				allData = exportDataflow(allData);
				final String json = migrationService.getJsonFromData(allData.getData());
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
				userData = exportDomain(userData);
				userData = exportNotebooks(userData);
				userData = exportDataflow(userData);
				final String json = migrationService.getJsonFromData(userData.getData());
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
				ExportResult projectData = migrationService.exportProject(project);
				projectData = exportDomain(projectData);
				projectData = exportNotebooks(projectData);
				projectData = exportDataflow(projectData);
				final String json = migrationService.getJsonFromData(projectData.getData());
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
				final ExportResult userData = migrationService.exportUsers(users);
				final String json = migrationService.getJsonFromData(userData.getData());
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
				final SchemaFromDB schema = migrationService.exportSchema();
				final String json = migrationService.getJsonFromSchema(schema);
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
				final SchemaFromDB currentSchema = migrationService.exportSchema();
				final String currentSchemaJson = migrationService.getJsonFromSchema(currentSchema);
				final String diffs = migrationService.compareSchemas(currentSchemaJson, otherSchema);
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

	// @ApiOperation(value = "Export Ontolgoies data.")
	// @PostMapping(value = "/exportMongo/{ontologies}")
	// @ApiResponses(@ApiResponse(response = String.class, code = 200, message =
	// "OK"))
	// public ResponseEntity<String> exportMongo(Model model, HttpServletResponse
	// response, HttpServletRequest request,
	// @ApiParam(value = "Ontologies List", required = true)
	// @PathVariable("ontologies") List<String> ontologies,
	// @ApiParam(value = "User and password of Mongo in the following format:
	// {'userMongo': <userMongo>, 'passwordMongo': <passwordMongo>}", required =
	// true) @Valid @RequestBody String data)
	// throws IOException {
	// JSONObject dataJson = new JSONObject(data);
	// File file = migrationHelper.generateFiles(ontologies,
	// dataJson.getString("userMongo"),
	// dataJson.getString("passwordMongo"));
	// return new ResponseEntity<>(file.getPath(), HttpStatus.OK);
	// }

	@ApiOperation(value = "Import Data.")
	@PostMapping("/import")
	@ApiResponses(@ApiResponse(response = String.class, code = 200, message = "OK"))
	public ResponseEntity<?> importData(@ApiParam(value = "data", required = true) @Valid @RequestBody String json,
			@PathVariable("override") Boolean override) {

		try {
			if (utils.isAdministrator()) {
				final User user = userService.getUser(utils.getUserId());
				final DataFromDB data = migrationService.getDataFromJson(json);
				migrationService.storeMigrationData(user, "data.json", "File for import", "data.json",
						json.getBytes(StandardCharsets.UTF_8), DataType.IMPORT, Status.NO_STATUS);
				final List<String> classNames = new ArrayList<>();
				for (final Class<?> clazz : data.getClasses()) {
					classNames.add(clazz.getName());
				}
				final MigrationData migrationData = migrationService.findMigrationData(user, DataType.IMPORT);
				if (migrationData == null) {
					log.error("Error: data cannot be null");
					return new ResponseEntity<>("Error: data cannot be null", HttpStatus.BAD_REQUEST);
				}
				final MigrationConfiguration config = new MigrationConfiguration();

				final List<Class<?>> sortedClazz = new LinkedList<>();
				sortedClazz.add(ProjectExport.class);
				sortedClazz.add(OPResource.class);
				sortedClazz.add(Pipeline.class);
				sortedClazz.add(Notebook.class);
				sortedClazz.add(FlowDomain.class);
				sortedClazz.add(Ontology.class);
				sortedClazz.add(OntologyVirtualDatasource.class);
				sortedClazz.add(OntologyVirtual.class);
				sortedClazz.add(OntologyTimeSeries.class);
				sortedClazz.add(OntologyTimeSeriesWindow.class);
				sortedClazz.add(OntologyTimeSeriesProperty.class);
				sortedClazz.add(Api.class);
				sortedClazz.add(ApiOperation.class);
				sortedClazz.add(ApiQueryParameter.class);
				sortedClazz.add(GadgetDatasource.class);
				sortedClazz.add(AppChildExport.class);
				sortedClazz.add(AppExport.class);
				sortedClazz.add(AppRoleChildExport.class);
				sortedClazz.add(AppRoleExport.class);
				sortedClazz.add(AppUserExport.class);
				sortedClazz.add(ClientPlatform.class);
				sortedClazz.add(ClientPlatformInstance.class);
				sortedClazz.add(ProjectResourceAccessExport.class);

				for (final Class<?> c : sortedClazz) {
					for (final Serializable id : data.getInstances(c)) {
						final Map<String, Object> result = data.getInstanceData(c, id);
						config.add(c, id, (Serializable) result.get(IDENTIFICATION),
								(Serializable) result.get("numversion"));
					}
				}

				for (final Class<?> clazz : data.getClasses()) {
					if (classNames.contains(clazz.getName())) {
						for (final Serializable id : data.getInstances(clazz)) {
							final Map<String, Object> result = data.getInstanceData(clazz, id);
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
				if (classNames.contains(PROJECT_EXPORT)) {
					errors = migrationService.importData(config, data, true, false, override);
				} else if (classNames.contains(USER)) {
					errors = migrationService.importData(config, data, false, true, override);
				} else {
					errors = migrationService.importData(config, data, false, false, override);
				}

				if (classNames.contains(FLOW_DOMAIN)) {
					importDomain(data, errors, override);
				}
				if (classNames.contains(NOTEBOOK)) {
					errors = importNotebook(data, errors, override);
				}
				if (classNames.contains(DATAFLOW)) {
					errors = importDataflow(data, errors, override);
				}

				return new ResponseEntity<>(errors.getErrors(), HttpStatus.OK);
			} else {
				log.error(USER_FORBIDDEN, utils.getUserId());
				return new ResponseEntity<>(FORBIDDEN, HttpStatus.FORBIDDEN);
			}
		} catch (final Exception e) {
			log.error("Error importing data from migration service. {}", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private ExportResult exportDomain(ExportResult data) {
		final Iterator<Serializable> iterator = data.getData().getInstances(FlowDomain.class).iterator();
		while (iterator.hasNext()) {
			final Serializable id = iterator.next();
			final Map<String, Object> obj = data.getData().getInstanceData(FlowDomain.class, id);
			try {
				final ResponseEntity<String> result = flowengineController
						.exportFlowDomainByIdentification(obj.get(IDENTIFICATION).toString());
				if (result.getStatusCode().equals(HttpStatus.OK)) {
					obj.put(DOMAIN_DATA, JSON.parse(result.getBody()));
				} else {
					log.error("Error exporting domain data {}. StatusCode {}", obj.get(IDENTIFICATION).toString(),
							result.getStatusCode().name());
					obj.put(DOMAIN_DATA, JSON.parse("[]"));
				}
			} catch (final NoderedAuthException e) {
				log.warn("Domain are not started. {}", e.getMessage());
				obj.put(DOMAIN_DATA, JSON.parse("[]"));
			}
		}
		return data;
	}

	private void importDomain(DataFromDB data, MigrationErrors errors, Boolean override) {
		// if data has FlowDomain.class then all domain data are exported too
		final Iterator<Serializable> iterator = data.getInstances(FlowDomain.class).iterator();
		final ObjectMapper mapper = new ObjectMapper();
		while (iterator.hasNext()) {
			final Serializable id = iterator.next();
			final Map<String, Object> obj = data.getInstanceData(FlowDomain.class, id);
			try {
				final ResponseEntity<String> result = flowengineController.importFlowDomainToUserAdmin(
						mapper.writeValueAsString(obj.get(DOMAIN_DATA)), obj.get(IDENTIFICATION).toString(),
						obj.get("user").toString(), override);
				if (result.getStatusCode().equals(HttpStatus.OK)) {
					log.info("Domain data imported successfully for domain {} and user {}",
							obj.get(IDENTIFICATION).toString(), obj.get("user").toString());
				} else {
					log.error("Error importing domain data {}. StatusCode {}", obj.get(IDENTIFICATION).toString(),
							result.getStatusCode().name());
				}
			} catch (final NoderedAuthException e) {
				log.warn("Domain {} are not started, data are not imported. {}", obj.get(IDENTIFICATION).toString(),
						e.getMessage());
			} catch (final JsonProcessingException e) {
				log.error("Error importing FlowEngine data {}. Error parsing JSON data {}",
						obj.get(IDENTIFICATION).toString());
				errors.addError(
						new MigrationError(new Instance(Notebook.class, id, obj.get(IDENTIFICATION).toString(), null),
								null, MigrationError.ErrorType.ERROR, "There was an error importing an entity"));
			}
		}
	}

	private ExportResult exportNotebooks(ExportResult data) {
		// if data has Notebook.class then all notebooks data are exported too
		final Iterator<Serializable> iterator = data.getData().getInstances(Notebook.class).iterator();
		while (iterator.hasNext()) {
			final Serializable id = iterator.next();
			final Map<String, Object> obj = data.getData().getInstanceData(Notebook.class, id);

			final ResponseEntity<?> result = notebookController.exportNotebook(obj.get(IDZEP).toString());
			if (result.getStatusCode().equals(HttpStatus.OK)) {
				obj.put(NOTEBOOK_DATA, JSON.parse(new String((byte[]) result.getBody(), StandardCharsets.UTF_8)));
			} else {
				log.error("Error exporting notebook data {}. StatusCode {}", obj.get(IDENTIFICATION).toString(),
						result.getStatusCode().name());
				obj.put(NOTEBOOK_DATA, JSON.parse("[]"));
			}

		}
		return data;
	}

	private MigrationErrors importNotebook(DataFromDB data, MigrationErrors errors, Boolean override) {
		// if data has FlowDomain.class then all domain data are exported too
		final Iterator<Serializable> iterator = data.getInstances(Notebook.class).iterator();
		final ObjectMapper mapper = new ObjectMapper();
		while (iterator.hasNext()) {
			final Serializable id = iterator.next();
			final Map<String, Object> obj = data.getInstanceData(Notebook.class, id);

			try {
				final ResponseEntity<?> result = notebookController.importNotebookData(
						obj.get(IDENTIFICATION).toString(), override, true,
						mapper.writeValueAsString(obj.get(NOTEBOOK_DATA)));

				if (result.getStatusCode().equals(HttpStatus.OK)) {
					log.info("Notebook data imported successfully notebook {} and user {}",
							obj.get(IDENTIFICATION).toString(), obj.get("user").toString());
					errors.addError(new MigrationError(
							new Instance(Notebook.class, id, obj.get(IDENTIFICATION).toString(), null), null,
							MigrationError.ErrorType.INFO, "Entity Persisted"));
				} else {
					log.error("Error importing Notebook data {}. StatusCode {}", obj.get(IDENTIFICATION).toString(),
							result.getStatusCode().name());
					errors.addError(new MigrationError(
							new Instance(Notebook.class, id, obj.get(IDENTIFICATION).toString(), null), null,
							MigrationError.ErrorType.ERROR, "There was an error importing an entity"));
				}
			} catch (final JsonProcessingException e) {
				log.error("Error importing Notebook data {}. Error parsing JSON data {}",
						obj.get(IDENTIFICATION).toString());
				errors.addError(
						new MigrationError(new Instance(Notebook.class, id, obj.get(IDENTIFICATION).toString(), null),
								null, MigrationError.ErrorType.ERROR, "There was an error importing an entity"));
			}

		}
		return errors;
	}

	private ExportResult exportDataflow(ExportResult data) {
		// if data has Pipeline.class then all pipelines data are exported too
		final Iterator<Serializable> iterator = data.getData().getInstances(Pipeline.class).iterator();
		while (iterator.hasNext()) {
			final Serializable id = iterator.next();
			final Map<String, Object> obj = data.getData().getInstanceData(Pipeline.class, id);

			try {
				final ResponseEntity<?> result = dataflowController.exportPipeline(obj.get(IDENTIFICATION).toString());
				if (result.getStatusCode().equals(HttpStatus.OK)) {
					obj.put(DATAFLOW_DATA, JSON.parse(result.getBody().toString()));
				} else {
					log.error("Error exporting dataflow data {}. StatusCode {}", obj.get(IDENTIFICATION).toString(),
							result.getStatusCode().name());
					obj.put(DATAFLOW_DATA, JSON.parse("[]"));
				}
			} catch (final UnsupportedEncodingException e) {
				log.error("Error exportin dataflow data: {}. {}", obj.get(IDENTIFICATION).toString(), e);
			}

		}
		return data;
	}

	private MigrationErrors importDataflow(DataFromDB data, MigrationErrors errors, Boolean override) {
		final Iterator<Serializable> iterator = data.getInstances(Pipeline.class).iterator();
		final ObjectMapper mapper = new ObjectMapper();
		while (iterator.hasNext()) {
			final Serializable id = iterator.next();
			final Map<String, Object> obj = data.getInstanceData(Pipeline.class, id);

			try {
				final ResponseEntity<?> result = dataflowController.importPipelineData(
						obj.get(IDENTIFICATION).toString(), override,
						mapper.writeValueAsString(obj.get(DATAFLOW_DATA)));

				if (result.getStatusCode().equals(HttpStatus.OK)) {
					log.info("Dataflow data imported successfully dataflow {} and user {}",
							obj.get(IDENTIFICATION).toString(), obj.get("user").toString());
					errors.addError(new MigrationError(
							new Instance(Pipeline.class, id, obj.get(IDENTIFICATION).toString(), null), null,
							MigrationError.ErrorType.INFO, "Entity Persisted"));
				} else {
					log.error("Error importing Dataflow data {}. StatusCode {}", obj.get(IDENTIFICATION).toString(),
							result.getStatusCode().name());
					errors.addError(new MigrationError(
							new Instance(Pipeline.class, id, obj.get(IDENTIFICATION).toString(), null), null,
							MigrationError.ErrorType.ERROR, "There was an error importing an entity"));
				}
			} catch (final JsonProcessingException e) {
				log.error("Error importing Dataflow data. Error parsing JSON data {}",
						obj.get(IDENTIFICATION).toString());
				errors.addError(
						new MigrationError(new Instance(Pipeline.class, id, obj.get(IDENTIFICATION).toString(), null),
								null, MigrationError.ErrorType.ERROR, "There was an error importing an entity"));
			} catch (final UnsupportedEncodingException e) {
				log.error("Error importing Dataflow data {}. {}", obj.get(IDENTIFICATION).toString());
				errors.addError(
						new MigrationError(new Instance(Pipeline.class, id, obj.get(IDENTIFICATION).toString(), null),
								null, MigrationError.ErrorType.ERROR, "There was an error importing an entity"));
			} catch (final ClientErrorException e) {
				log.error("Error importing Dataflow data {}. {}", obj.get(IDENTIFICATION).toString(), e.getMessage());
				errors.addError(
						new MigrationError(new Instance(Pipeline.class, id, obj.get(IDENTIFICATION).toString(), null),
								null, MigrationError.ErrorType.ERROR, "There was an error importing an entity"));
			}

		}
		return errors;
	}

}
