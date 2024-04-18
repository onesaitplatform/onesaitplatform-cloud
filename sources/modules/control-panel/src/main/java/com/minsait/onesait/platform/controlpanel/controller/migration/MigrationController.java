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
package com.minsait.onesait.platform.controlpanel.controller.migration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ClientErrorException;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.config.model.ActionsDigitalTwinType;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.ApiQueryParameter;
import com.minsait.onesait.platform.config.model.AppChildExport;
import com.minsait.onesait.platform.config.model.AppExport;
import com.minsait.onesait.platform.config.model.AppRoleChildExport;
import com.minsait.onesait.platform.config.model.AppRoleExport;
import com.minsait.onesait.platform.config.model.AppUserExport;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformInstance;
import com.minsait.onesait.platform.config.model.Dashboard;
import com.minsait.onesait.platform.config.model.DigitalTwinType;
import com.minsait.onesait.platform.config.model.Flow;
import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.model.FlowNode;
import com.minsait.onesait.platform.config.model.Gadget;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.model.GadgetMeasure;
import com.minsait.onesait.platform.config.model.GadgetTemplate;
import com.minsait.onesait.platform.config.model.Layer;
import com.minsait.onesait.platform.config.model.MigrationData;
import com.minsait.onesait.platform.config.model.MigrationData.DataType;
import com.minsait.onesait.platform.config.model.MigrationData.Status;
import com.minsait.onesait.platform.config.model.Notebook;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.OntologyTimeSeries;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesProperty;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow;
import com.minsait.onesait.platform.config.model.OntologyUserAccess;
import com.minsait.onesait.platform.config.model.OntologyVirtual;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;
import com.minsait.onesait.platform.config.model.Pipeline;
import com.minsait.onesait.platform.config.model.Project;
import com.minsait.onesait.platform.config.model.ProjectExport;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessExport;
import com.minsait.onesait.platform.config.model.PropertyDigitalTwinType;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.UserExport;
import com.minsait.onesait.platform.config.model.UserToken;
import com.minsait.onesait.platform.config.model.Viewer;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.repository.MigrationDataRepository;
import com.minsait.onesait.platform.config.services.binaryfile.BinaryFileService;
import com.minsait.onesait.platform.config.services.migration.DataFromDB;
import com.minsait.onesait.platform.config.services.migration.ExportResult;
import com.minsait.onesait.platform.config.services.migration.Instance;
import com.minsait.onesait.platform.config.services.migration.MigrationConfiguration;
import com.minsait.onesait.platform.config.services.migration.MigrationError;
import com.minsait.onesait.platform.config.services.migration.MigrationErrors;
import com.minsait.onesait.platform.config.services.migration.MigrationService;
import com.minsait.onesait.platform.config.services.migration.SchemaFromDB;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.project.ProjectService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.help.migration.MigrationHelper;
import com.minsait.onesait.platform.controlpanel.rest.management.dataflow.DataFlowStorageManagementController;
import com.minsait.onesait.platform.controlpanel.rest.management.flowengine.FlowengineManagementController;
import com.minsait.onesait.platform.controlpanel.rest.management.notebook.NotebookManagementController;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.libraries.nodered.auth.exception.NoderedAuthException;
import com.mongodb.BasicDBObject;

import de.galan.verjson.core.IOReadException;
import de.galan.verjson.core.NamespaceMismatchException;
import de.galan.verjson.core.VersionNotSupportedException;
import de.galan.verjson.step.ProcessStepException;
import lombok.extern.slf4j.Slf4j;

@Controller
@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
@RequestMapping("/migration")
@Slf4j
public class MigrationController {

	@Value("${onesaitplatform.controlpanel.url:http://localhost:18000/controlpanel}")
	private String controlpanelUrl;

	@Autowired
	MigrationService migrationService;

	@Autowired
	private UserService userService;

	@Autowired
	private ProjectService projectService;

	@Autowired
	private AppWebUtils utils;

	@Autowired
	FlowengineManagementController flowengineController;

	@Autowired
	NotebookManagementController notebookController;

	@Autowired
	DataFlowStorageManagementController dataflowController;

	@Autowired
	OntologyService ontologyService;

	@Autowired
	private MigrationHelper migrationHelper;

	@Autowired
	HazelcastInstance hazelcast;

	@Autowired
	private MigrationDataRepository migrationDateRepository;

	@Autowired
	private BinaryFileService binaryFileService;

	private final RestTemplate restTemplate = new RestTemplate(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());

	private static final String IMPORT_DATA_STR = "importData";
	private static final String OTHER_SCHEMA_STR = "otherSchema";
	private static final String CLASS_NAMES_STR = "classNames";
	private static final String MIGRATION_SHOW = "migration/show";
	private static final String NO_CACHE_STR = "no-cache";
	private static final String USERS = "users";
	private static final String APPLICATION_DOWNLOAD = "application/x-download";
	private static final String CONTENT_DISPOSITION = "Content-Disposition";
	private static final String ATTACHMENT = "attachment; filename=data.json";
	private static final String PRAGMA = "Pragma";
	private static final String CACHE_CONTROL = "Cache-Control";
	private static final String PROJECT = "com.minsait.onesait.platform.config.model.Project";
	private static final String PROJECT_EXPORT = "com.minsait.onesait.platform.config.model.ProjectExport";
	private static final String USER = "com.minsait.onesait.platform.config.model.User";
	private static final String USER_EXPORT = "com.minsait.onesait.platform.config.model.UserExport";
	private static final String DOMAIN_DATA = "domainData";
	private static final String IDENTIFICATION = "identification";
	private static final String NOTEBOOK_DATA = "notebookData";
	private static final String IDZEP = "idzep";
	private static final String DATAFLOW_DATA = "dataflowData";
	private static final String NOTEBOOK = "com.minsait.onesait.platform.config.model.Notebook";
	private static final String DATAFLOW = "com.minsait.onesait.platform.config.model.Pipeline";
	private static final String FLOW_DOMAIN = "com.minsait.onesait.platform.config.model.FlowDomain";

	@PostConstruct
	void setUTF8Encoding() {
		restTemplate.getMessageConverters().removeIf(c -> c instanceof StringHttpMessageConverter);
		restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
	}

	@GetMapping(value = "/show", produces = "text/html")
	public String show(Model model, HttpServletRequest request) {
		ImportData importData = new ImportData();
		ImportData otherSchema = new ImportData();
		model.addAttribute(USERS, userService.getAllUsers());
		model.addAttribute("projects", projectService.getAllProjects());
		model.addAttribute(IMPORT_DATA_STR, importData);
		model.addAttribute(OTHER_SCHEMA_STR, otherSchema);
		model.addAttribute(CLASS_NAMES_STR, new ArrayList<String>());
		model.addAttribute("selectedClasses", new SelectedClasses());
		model.addAttribute("ontologies", ontologyService.getAllOntologiesForList(utils.getUserId(), "", "", "", ""));
		model.addAttribute("binaryFiles", binaryFileService.getAllFiles(userService.getUser(utils.getUserId()), true));
		return MIGRATION_SHOW;
	}

	@GetMapping(value = "/export")
	public ResponseEntity<String> export(Model model, HttpServletResponse response, HttpServletRequest request)
			throws IllegalAccessException, IOException {
		User loggedUser = userService.getUser(utils.getUserId());
		final List<MigrationData> migrationData = migrationDateRepository.findByUser(loggedUser);

		Boolean status = checkExport();
		if (status != null && !status) {
			return new ResponseEntity<>(utils.getMessage("migration.export.not.finished",
					"There is another export in progress for this user, please wait a few minutes for it to finish to launch a new one."),
					HttpStatus.FORBIDDEN);
		} else if (status == null) {
			return new ResponseEntity<>(
					utils.getMessage("migration.export.error",
							"There was an error with the export, please contact the support team."),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		if (!migrationData.isEmpty()) {
			migrationDateRepository.delete(migrationData.get(0));
		}
		migrationService.storeMigrationData(loggedUser, "exportAll", "File for export", "exportAll", null,
				DataType.EXPORT, Status.IN_PROGRESS);

		ExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		executor.execute(exportRunnable(loggedUser, request.getHeader("Authorization"), controlpanelUrl));
		return new ResponseEntity<>(HttpStatus.OK);
	}

	private Runnable exportRunnable(User loggedUser, String token, String controlpanelUrl) {

		return new Runnable() {

			MigrationController migrationController = new MigrationController();

			@Override
			public void run() {
				try {
					ExportResult allData = migrationService.exportAll();
					allData = migrationController.exportDomain(allData, token, controlpanelUrl);
					allData = migrationController.exportNotebooks(allData, token, controlpanelUrl);
					allData = migrationController.exportDataflow(allData, token, controlpanelUrl);
					String json = migrationService.getJsonFromData(allData.getData());

					migrationService.updateStoreMigrationData(loggedUser, json, DataType.EXPORT);

				} catch (final Exception e) {
					log.error("Error exporting migration all data", e);
					migrationService.updateStoreMigrationData(loggedUser, null, DataType.EXPORT);
				}
			}
		};

	}

	@GetMapping(value = "/exportUser/{id}")
	public ResponseEntity<String> exportUser(Model model, HttpServletResponse response, HttpServletRequest request,
			@PathVariable("id") String userId) throws IllegalArgumentException, IllegalAccessException, IOException {
		User loggedUser = userService.getUser(utils.getUserId());
		User userToExport = userService.getUser(userId);
		final List<MigrationData> migrationData = migrationDateRepository.findByUser(loggedUser);

		Boolean status = checkExport();
		if (status != null && !status) {
			return new ResponseEntity<>(utils.getMessage("migration.export.not.finished",
					"There is another export in progress for this user, please wait a few minutes for it to finish to launch a new one."),
					HttpStatus.FORBIDDEN);
		} else if (status == null) {
			return new ResponseEntity<>(
					utils.getMessage("migration.export.error",
							"There was an error with the export, please contact the support team."),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		if (!migrationData.isEmpty()) {
			migrationDateRepository.delete(migrationData.get(0));
		}
		migrationService.storeMigrationData(loggedUser, "exportByUser", "File for export", "exportByUser", null,
				DataType.EXPORT, Status.IN_PROGRESS);

		ExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		executor.execute(
				exportByUserRunnable(userToExport, loggedUser, request.getHeader("Authorization"), controlpanelUrl));
		return new ResponseEntity<>(HttpStatus.OK);
	}

	private Runnable exportByUserRunnable(User user, User loggedUser, String token, String controlpanelUrl) {

		return new Runnable() {

			MigrationController migrationController = new MigrationController();

			@Override
			public void run() {
				try {
					ExportResult userData = migrationService.exportUser(user);
					userData = migrationController.exportDomain(userData, token, controlpanelUrl);
					userData = migrationController.exportNotebooks(userData, token, controlpanelUrl);
					userData = migrationController.exportDataflow(userData, token, controlpanelUrl);
					String json = migrationService.getJsonFromData(userData.getData());

					migrationService.updateStoreMigrationData(loggedUser, json, DataType.EXPORT);

				} catch (final Exception e) {
					log.error("Error exporting migration data by user {}", user.getUserId(), e);
					migrationService.updateStoreMigrationData(loggedUser, null, DataType.EXPORT);
				}
			}
		};

	}

	@GetMapping(value = "/downloadExport")
	public ResponseEntity<String> downloadExport(Model model, HttpServletResponse response, HttpServletRequest request)
			throws IllegalArgumentException, IllegalAccessException, IOException {
		User loggedUser = userService.getUser(utils.getUserId());
		final List<MigrationData> migrationDataFinished = migrationDateRepository.findByUserAndTypeAndStatus(loggedUser,
				DataType.EXPORT, Status.FINISHED);

		ResponseEntity<String> output = new ResponseEntity<>(new String(migrationDataFinished.get(0).getFile()),
				HttpStatus.OK);
		response.setContentType(APPLICATION_DOWNLOAD);
		response.setHeader(CONTENT_DISPOSITION, ATTACHMENT);
		response.setHeader(PRAGMA, NO_CACHE_STR);
		response.setHeader(CACHE_CONTROL, NO_CACHE_STR);

		migrationDateRepository.delete(migrationDataFinished.get(0));
		return output;

	}

	@GetMapping(value = "/checkDownloadExport")
	public ResponseEntity<String> checkDownloadExport(Model model, HttpServletResponse response,
			HttpServletRequest request) throws IllegalArgumentException, IllegalAccessException, IOException {
		User loggedUser = userService.getUser(utils.getUserId());
		final List<MigrationData> migrationDataFinished = migrationDateRepository.findByUserAndTypeAndStatus(loggedUser,
				DataType.EXPORT, Status.FINISHED);

		if (migrationDataFinished != null && !migrationDataFinished.isEmpty() && migrationDataFinished.size() == 1) {
			return new ResponseEntity<>("true", HttpStatus.OK);
		}

		final List<MigrationData> migrationDataError = migrationDateRepository.findByUserAndTypeAndStatus(loggedUser,
				DataType.EXPORT, Status.ERROR);

		if (migrationDataError != null && !migrationDataError.isEmpty() && migrationDataError.size() == 1) {
			migrationDateRepository.delete(migrationDataError.get(0));
			return new ResponseEntity<>("error", HttpStatus.OK);
		}

		final List<MigrationData> migrationDataInProgress = migrationDateRepository
				.findByUserAndTypeAndStatus(loggedUser, DataType.EXPORT, Status.IN_PROGRESS);

		if (migrationDataInProgress != null && !migrationDataInProgress.isEmpty()
				&& migrationDataInProgress.size() == 1) {
			return new ResponseEntity<>("false", HttpStatus.OK);
		}

		return new ResponseEntity<>("null", HttpStatus.OK);
	}

	@GetMapping(value = "/exportUsers/{users}")
	public ResponseEntity<String> exportUsers(Model model, HttpServletResponse response, HttpServletRequest request,
			@PathVariable("users") List<String> users)
			throws IllegalArgumentException, IllegalAccessException, IOException {
		if (users==null || users.isEmpty() || users.get(0).equals("null")) {
			return new ResponseEntity<>(utils.getMessage("migration.export.select.user.error",
					"Please select users to export."),
					HttpStatus.FORBIDDEN);
		}
		
		User loggedUser = userService.getUser(utils.getUserId());
		final List<MigrationData> migrationData = migrationDateRepository.findByUser(loggedUser);
		Boolean status = checkExport();
		if (status != null && !status) {
			return new ResponseEntity<>(utils.getMessage("migration.export.not.finished",
					"There is another export in progress for this user, please wait a few minutes for it to finish to launch a new one."),
					HttpStatus.FORBIDDEN);
		} else if (status == null) {
			return new ResponseEntity<>(
					utils.getMessage("migration.export.error",
							"There was an error with the export, please contact the support team."),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		if (!migrationData.isEmpty()) {
			migrationDateRepository.delete(migrationData.get(0));
		}
		migrationService.storeMigrationData(loggedUser, "exportUsers", "File for export", "exportUsers", null,
				DataType.EXPORT, Status.IN_PROGRESS);

		ExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		executor.execute(exportUsersRunnable(loggedUser, users));
		return new ResponseEntity<>(HttpStatus.OK);
	}

	private Runnable exportUsersRunnable(User loggedUser, List<String> users) {

		return new Runnable() {

			@Override
			public void run() {
				try {
					ExportResult userData = migrationService.exportUsers(users);
					String json = migrationService.getJsonFromData(userData.getData());

					migrationService.updateStoreMigrationData(loggedUser, json, DataType.EXPORT);

				} catch (final Exception e) {
					log.error("Error exporting users {}", users, e);
					migrationService.updateStoreMigrationData(loggedUser, null, DataType.EXPORT);
				}
			}
		};

	}

	@GetMapping(value = "/exportProject/{project}")
	public ResponseEntity<String> exportProject(Model model, HttpServletResponse response, HttpServletRequest request,
			@PathVariable("project") String project)
			throws IllegalArgumentException, IllegalAccessException, IOException {
		if (project==null || project.equals("null")) {
			return new ResponseEntity<>(utils.getMessage("migration.export.select.project.error",
					"Please select project to export."),
					HttpStatus.FORBIDDEN);
		}
		
		User loggedUser = userService.getUser(utils.getUserId());
		final List<MigrationData> migrationData = migrationDateRepository.findByUser(loggedUser);

		Boolean status = checkExport();
		if (status != null && !status) {
			return new ResponseEntity<>(utils.getMessage("migration.export.not.finished",
					"There is another export in progress for this user, please wait a few minutes for it to finish to launch a new one."),
					HttpStatus.FORBIDDEN);
		} else if (status == null) {
			return new ResponseEntity<>(
					utils.getMessage("migration.export.error",
							"There was an error with the export, please contact the support team."),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		if (!migrationData.isEmpty()) {
			migrationDateRepository.delete(migrationData.get(0));
		}
		migrationService.storeMigrationData(loggedUser, "exportByProject", "File for export", "exportByProject", null,
				DataType.EXPORT, Status.IN_PROGRESS);

		ExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		executor.execute(
				exportByProjectRunnable(project, loggedUser, request.getHeader("Authorization"), controlpanelUrl));
		return new ResponseEntity<>(HttpStatus.OK);
	}

	private Boolean checkExport() {
		User loggedUser = userService.getUser(utils.getUserId());
		final List<MigrationData> migrationData = migrationDateRepository.findByUser(loggedUser);
		if (migrationData != null && !migrationData.isEmpty()
				&& migrationData.get(0).getStatus().equals(Status.ERROR)) {
			return null;
		}
		if (migrationData != null && !migrationData.isEmpty() && migrationData.size() == 1
				&& migrationData.get(0).getType().equals(DataType.EXPORT)
				&& migrationData.get(0).getStatus().equals(Status.IN_PROGRESS)) {
			return false;
		}
		return true;
	}

	private Runnable exportByProjectRunnable(String project, User loggedUser, String token, String controlpanelUrl) {

		return new Runnable() {

			MigrationController migrationController = new MigrationController();

			@Override
			public void run() {
				try {
					ExportResult projectData = migrationService.exportProject(project);
					projectData = migrationController.exportDomain(projectData, token, controlpanelUrl);
					projectData = migrationController.exportNotebooks(projectData, token, controlpanelUrl);
					projectData = migrationController.exportDataflow(projectData, token, controlpanelUrl);
					String json = migrationService.getJsonFromData(projectData.getData());

					migrationService.updateStoreMigrationData(loggedUser, json, DataType.EXPORT);

				} catch (final Exception e) {
					log.error("Error exporting migration data by project {}", project, e);
					migrationService.updateStoreMigrationData(loggedUser, null, DataType.EXPORT);
				}
			}
		};

	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = "/exportSchema")
	public ResponseEntity<String> exportSchema(Model model, HttpServletResponse response, HttpServletRequest request)
			throws IOException {
		SchemaFromDB schema = migrationService.exportSchema();
		String json = migrationService.getJsonFromSchema(schema);
		ResponseEntity<String> output = new ResponseEntity<>(json, HttpStatus.OK);
		response.setContentType(APPLICATION_DOWNLOAD);
		response.setHeader(CONTENT_DISPOSITION, ATTACHMENT);
		response.setHeader(PRAGMA, NO_CACHE_STR);
		response.setHeader(CACHE_CONTROL, NO_CACHE_STR);
		return output;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping(value = "/exportMongo/{ontologies}")
	public ResponseEntity<InputStreamResource> exportMongo(Model model, HttpServletResponse response,
			HttpServletRequest request, @PathVariable("ontologies") List<String> ontologies, @RequestBody String body)
			throws IOException {
		JSONObject jsonBody = new JSONObject(body);
		File file = migrationHelper.generateFiles(ontologies, jsonBody.getString("userMongo"),
				jsonBody.getString("passwordMongo"));
		final HttpHeaders respHeaders = new HttpHeaders();
		respHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		respHeaders.setContentDispositionFormData("attachment", file.getName());
		respHeaders.setContentLength(file.length());
		final InputStreamResource isr = new InputStreamResource(new FileInputStream(file));
		return new ResponseEntity<>(isr, respHeaders, HttpStatus.OK);
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping(value = "/exportFiles/{files}")
	public ResponseEntity<InputStreamResource> exportFiles(Model model, HttpServletResponse response,
			HttpServletRequest request, @PathVariable("files") List<String> files, @RequestBody String body)
			throws IOException {
		JSONObject jsonBody = new JSONObject(body);
		File file = migrationHelper.generateBinaryFilesExport(files, jsonBody.getString("userMongo"),
				jsonBody.getString("passwordMongo"));
		final HttpHeaders respHeaders = new HttpHeaders();
		respHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		respHeaders.setContentDispositionFormData("attachment", file.getName());
		respHeaders.setContentLength(file.length());
		final InputStreamResource isr = new InputStreamResource(new FileInputStream(file));
		return new ResponseEntity<>(isr, respHeaders, HttpStatus.OK);
	}

	@PostMapping(value = "/compareSchema", produces = "text/html")
	public String compareSchema(Model model, HttpServletResponse response, HttpServletRequest request,
			ImportData otherSchema) throws IOException {
		if (otherSchema.getContent() != null) {
			MultipartFile content = otherSchema.getContent();
			byte[] bytes = content.getBytes();
			String otherSchemaJson = new String(bytes, StandardCharsets.UTF_8);
			SchemaFromDB currentSchema = migrationService.exportSchema();
			String currentSchemaJson = migrationService.getJsonFromSchema(currentSchema);
			String diffs = migrationService.compareSchemas(currentSchemaJson, otherSchemaJson);
			model.addAttribute(IMPORT_DATA_STR, new ImportData());
			model.addAttribute(OTHER_SCHEMA_STR, otherSchema);
			model.addAttribute(CLASS_NAMES_STR, new ArrayList<String>());
			model.addAttribute("selectedClasses", new SelectedClasses());
			model.addAttribute("diffs", diffs);
		}
		return MIGRATION_SHOW;
	}

	@PostMapping(value = "/loadimport", produces = "text/html", params = "action=load")
	public String loadImportFile(Model model, HttpServletRequest request, ImportData importData) throws IOException,
			VersionNotSupportedException, NamespaceMismatchException, ProcessStepException, IOReadException {
		if (importData.getContent() != null) {
			MultipartFile content = importData.getContent();
			byte[] bytes = content.getBytes();
			String json = new String(bytes, StandardCharsets.UTF_8);
			DataFromDB data = migrationService.getDataFromJson(json);

			// store the file in the database. Currently, only one file per user.
			String userId = utils.getUserId();
			User user = userService.getUser(userId);
			migrationService.storeMigrationData(user, importData.getFileName(), "File for import",
					importData.getFileName(), bytes, DataType.IMPORT, Status.NO_STATUS);

			model.addAttribute(OTHER_SCHEMA_STR, new ImportData());

			List<Clazz> classNames = new ArrayList<>();
			List<String> clazzs = new ArrayList<>();
			for (Class<?> clazz : data.getClasses()) {
				String[] split = clazz.getCanonicalName().split("\\.");
				classNames.add(new Clazz(split[split.length - 1], clazz.getName()));
				clazzs.add(clazz.getName());
			}
			model.addAttribute(CLASS_NAMES_STR, classNames);
			importData.setClasses(clazzs);
			model.addAttribute(IMPORT_DATA_STR, importData);
		}
		return MIGRATION_SHOW;
	}

	@PostMapping(value = "/loadimport", produces = "text/html", params = "action=import")
	public String importJson(Model model, HttpServletRequest request, ImportData importData)
			throws VersionNotSupportedException, NamespaceMismatchException, ProcessStepException, IOReadException,
			ClassNotFoundException, NoSuchFieldException, IllegalAccessException, InstantiationException {

		String userId = utils.getUserId();
		User user = userService.getUser(userId);
		MigrationData migrationData = migrationService.findMigrationData(user, DataType.IMPORT);
		if (migrationData == null) {
			throw new IllegalStateException("The file should be loaded previouly");
		}
		String json = new String(migrationData.getFile(), StandardCharsets.UTF_8);
		DataFromDB data = migrationService.getDataFromJson(json);

		MigrationConfiguration config = new MigrationConfiguration();

		List<Class<?>> sortedClazz = new LinkedList<>();
		sortedClazz.add(User.class);
		sortedClazz.add(UserExport.class);
		sortedClazz.add(UserToken.class);
		sortedClazz.add(Project.class);
		sortedClazz.add(ProjectExport.class);
		sortedClazz.add(OPResource.class);
		sortedClazz.add(Pipeline.class);
		sortedClazz.add(Notebook.class);
		sortedClazz.add(FlowDomain.class);
		sortedClazz.add(Ontology.class);
		sortedClazz.add(Flow.class);
		sortedClazz.add(FlowNode.class);
		sortedClazz.add(OntologyVirtualDatasource.class);
		sortedClazz.add(OntologyVirtual.class);
		sortedClazz.add(OntologyTimeSeries.class);
		sortedClazz.add(OntologyTimeSeriesWindow.class);
		sortedClazz.add(OntologyTimeSeriesProperty.class);
		sortedClazz.add(OntologyUserAccess.class);
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
		sortedClazz.add(Gadget.class);
		sortedClazz.add(GadgetTemplate.class);
		sortedClazz.add(GadgetMeasure.class);
		sortedClazz.add(Dashboard.class);
		sortedClazz.add(DigitalTwinType.class);
		sortedClazz.add(PropertyDigitalTwinType.class);
		sortedClazz.add(ActionsDigitalTwinType.class);
		sortedClazz.add(Layer.class);
		sortedClazz.add(Viewer.class);
		sortedClazz.add(ProjectResourceAccess.class);
		sortedClazz.add(ProjectResourceAccessExport.class);

		for (Class<?> c : sortedClazz) {
			if (importData.getClasses().contains(c.getName())) {
				for (Serializable id : data.getInstances(c)) {
					Map<String, Object> result = data.getInstanceData(c, id);
					if (c.getName().startsWith(PROJECT)) {
						config.addProject(c, id, (Serializable) result.get(IDENTIFICATION));
					} else if (c.getName().equals(USER)) {
						config.addUser(c, id);
					} else {
						config.add(c, id, (Serializable) result.get(IDENTIFICATION),
								(Serializable) result.get("numversion"));
					}
				}
			}
		}

		for (Class<?> clazz : data.getClasses()) {
			if (importData.getClasses().contains(clazz.getName()) && !sortedClazz.contains(clazz)) {
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

		model.addAttribute(CLASS_NAMES_STR, new ArrayList<String>());
		model.addAttribute(IMPORT_DATA_STR, new ImportData());
		model.addAttribute(OTHER_SCHEMA_STR, new ImportData());

		MigrationErrors errors = new MigrationErrors();

		if (importData.getClasses().contains(FLOW_DOMAIN)) {
			this.importDomain(data, errors, importData.getOverride());
		}

		if (importData.getClasses().contains(PROJECT) || importData.getClasses().contains(PROJECT_EXPORT)) {
			errors = migrationService.importData(config, data, true, false, importData.getOverride());
		} else if (importData.getClasses().contains(USER) || importData.getClasses().contains(USER_EXPORT)) {
			errors = migrationService.importData(config, data, false, true, importData.getOverride());
		} else {
			errors = migrationService.importData(config, data, false, false, importData.getOverride());
		}

		if (importData.getClasses().contains(NOTEBOOK)) {
			errors = this.importNotebook(data, errors, importData.getOverride());
		}
		if (importData.getClasses().contains(DATAFLOW)) {
			errors = this.importDataflow(data, errors, importData.getOverride());
		}

		model.addAttribute("errors", errors.getErrors());

		return MIGRATION_SHOW;

	}

	@GetMapping(value = "/cleanCache")
	public String cleanCache(Model model, HttpServletResponse response, HttpServletRequest request) throws IOException {
		final Collection<DistributedObject> distributedObjects = hazelcast.getDistributedObjects();
		for (DistributedObject distributedObject : distributedObjects) {
			if (distributedObject instanceof IMap) {
				final IMap<?, ?> map = (IMap) distributedObject;
				map.clear();
			}
		}
		model.addAttribute(CLASS_NAMES_STR, new ArrayList<String>());
		model.addAttribute(IMPORT_DATA_STR, new ImportData());
		model.addAttribute(OTHER_SCHEMA_STR, new ImportData());
		model.addAttribute("errors", new ArrayList<MigrationErrors>());
		return MIGRATION_SHOW;
	}

	private ExportResult exportDomain(ExportResult data, String token, String controlpanelUrl) {
		// if data has FlowDomain.class then all domain data are exported too
		Iterator<Serializable> iterator = data.getData().getInstances(FlowDomain.class).iterator();
		ObjectMapper mapper = new ObjectMapper();
		while (iterator.hasNext()) {
			Serializable id = iterator.next();
			Map<String, Object> obj = data.getData().getInstanceData(FlowDomain.class, id);
			try {
				HttpHeaders headers = new HttpHeaders();
				headers.add("Authorization", token);

				HttpEntity<?> entity = new HttpEntity<>(headers);
				ResponseEntity<String> result = restTemplate.exchange(
						controlpanelUrl.concat("/api/flowengine/export/domain/" + obj.get(IDENTIFICATION).toString()),
						HttpMethod.GET, entity, String.class);
				if (result.getStatusCode().equals(HttpStatus.OK)) {
					obj.put(DOMAIN_DATA, mapper.readValue(result.getBody(), JsonNode.class));
				} else {
					log.error("Error exporting domain data {}. StatusCode {}", obj.get(IDENTIFICATION).toString(),
							result.getStatusCode().name());
					obj.put(DOMAIN_DATA, mapper.readValue("[]", JsonNode.class));
				}
			} catch (NoderedAuthException e) {
				log.warn("Domain are not started. {}", e.getMessage());
				try {
					obj.put(DOMAIN_DATA, mapper.readValue("[]", JsonNode.class));
				} catch (IOException e1) {
					log.error("Error parsing domain export result. DomainId: {} ", obj.get(IDENTIFICATION).toString(),
							e);
				}
			} catch (IOException e) {
				log.error("Error parsing domain export result. DomainId: {} ", obj.get(IDENTIFICATION).toString(), e);
				try {
					obj.put(DOMAIN_DATA, mapper.readValue("[]", JsonNode.class));
				} catch (IOException e1) {
					log.error("Error parsing domain export result. DomainId: {} ", obj.get(IDENTIFICATION).toString(),
							e);
				}
			}
		}
		return data;
	}

	private void importDomain(DataFromDB data, MigrationErrors errors, Boolean override) {
		// if data has FlowDomain.class then all domain data are exported too
		Iterator<Serializable> iterator = data.getInstances(FlowDomain.class).iterator();
		ObjectMapper mapper = new ObjectMapper();
		while (iterator.hasNext()) {
			Serializable id = iterator.next();
			Map<String, Object> obj = data.getInstanceData(FlowDomain.class, id);
			try {
				ResponseEntity<String> result = flowengineController.importDomainToUserFromProject(
						obj.get("user").toString(), mapper.writeValueAsString(obj), obj.get(IDENTIFICATION).toString(),
						override);
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
			} catch (JsonProcessingException e) {
				log.error("Error importing FlowEngine data {}. Error parsing JSON data {}",
						obj.get(IDENTIFICATION).toString());
				errors.addError(
						new MigrationError(new Instance(Notebook.class, id, obj.get(IDENTIFICATION).toString(), null),
								null, MigrationError.ErrorType.ERROR, "There was an error importing an entity"));
			}
		}
	}

	private ExportResult exportNotebooks(ExportResult data, String token, String controlpanelUrl) {
		// if data has Notebook.class then all notebooks data are exported too
		Iterator<Serializable> iterator = data.getData().getInstances(Notebook.class).iterator();
		ObjectMapper mapper = new ObjectMapper();
		while (iterator.hasNext()) {
			Serializable id = iterator.next();
			Map<String, Object> obj = data.getData().getInstanceData(Notebook.class, id);

			HttpHeaders headers = new HttpHeaders();
			headers.add("Authorization", token);

			HttpEntity<?> entity = new HttpEntity<>(headers);
			ResponseEntity<String> result = restTemplate.exchange(
					controlpanelUrl.concat("/api/notebooks/export/" + obj.get(IDZEP).toString()), HttpMethod.GET,
					entity, String.class);
			try {
				if (result.getStatusCode().equals(HttpStatus.OK)) {

					obj.put(NOTEBOOK_DATA, mapper.readValue(result.getBody(), JsonNode.class));

				} else {
					log.error("Error exporting notebook data {}. StatusCode {}", obj.get(IDENTIFICATION).toString(),
							result.getStatusCode().name());
					obj.put(NOTEBOOK_DATA, mapper.readValue("[]", JsonNode.class));
				}

			} catch (IOException e) {
				log.error("Error parsing notebook export result. notebook: {} ", obj.get(IDENTIFICATION).toString(), e);
				try {
					obj.put(NOTEBOOK_DATA, mapper.readValue("[]", JsonNode.class));
				} catch (IOException e1) {
					log.error("Error parsing notebook export result. notebook: {} ", obj.get(IDENTIFICATION).toString(),
							e);
				}
			}

		}
		return data;
	}

	private MigrationErrors importNotebook(DataFromDB data, MigrationErrors errors, Boolean override) {
		// if data has FlowDomain.class then all domain data are exported too
		Iterator<Serializable> iterator = data.getInstances(Notebook.class).iterator();
		ObjectMapper mapper = new ObjectMapper();
		while (iterator.hasNext()) {
			Serializable id = iterator.next();
			Map<String, Object> obj = data.getInstanceData(Notebook.class, id);

			try {
				ResponseEntity<?> result = notebookController.importNotebookData(obj.get(IDENTIFICATION).toString(),
						override, true, mapper.writeValueAsString(obj.get(NOTEBOOK_DATA)));

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
			} catch (JsonProcessingException e) {
				log.error("Error importing Notebook data {}. Error parsing JSON data {}",
						obj.get(IDENTIFICATION).toString());
				errors.addError(
						new MigrationError(new Instance(Notebook.class, id, obj.get(IDENTIFICATION).toString(), null),
								null, MigrationError.ErrorType.ERROR, "There was an error importing an entity"));
			}

		}
		return errors;
	}

	private ExportResult exportDataflow(ExportResult data, String token, String controlpanelUrl) {
		// if data has Pipeline.class then all pipelines data are exported too
		DataFlowStorageManagementController dataflowController = new DataFlowStorageManagementController();
		Iterator<Serializable> iterator = data.getData().getInstances(Pipeline.class).iterator();
		while (iterator.hasNext()) {
			Serializable id = iterator.next();
			Map<String, Object> obj = data.getData().getInstanceData(Pipeline.class, id);

			HttpHeaders headers = new HttpHeaders();
			headers.add("Authorization", token);

			HttpEntity<?> entity = new HttpEntity<>(headers);
			ResponseEntity<?> result = restTemplate.exchange(
					controlpanelUrl
							.concat("/api/dataflows/pipelines/" + obj.get(IDENTIFICATION).toString() + "/export/"),
					HttpMethod.POST, entity, String.class);

			if (result.getStatusCode().equals(HttpStatus.OK)) {
				obj.put(DATAFLOW_DATA, BasicDBObject.parse(result.getBody().toString()));
			} else {
				log.error("Error exporting dataflow data {}. StatusCode {}", obj.get(IDENTIFICATION).toString(),
						result.getStatusCode().name());
				obj.put(DATAFLOW_DATA, BasicDBObject.parse("[]"));
			}

		}
		return data;
	}

	private MigrationErrors importDataflow(DataFromDB data, MigrationErrors errors, Boolean override) {
		Iterator<Serializable> iterator = data.getInstances(Pipeline.class).iterator();
		ObjectMapper mapper = new ObjectMapper();
		while (iterator.hasNext()) {
			Serializable id = iterator.next();
			Map<String, Object> obj = data.getInstanceData(Pipeline.class, id);

			try {
				ResponseEntity<?> result = dataflowController.importPipelineData(obj.get(IDENTIFICATION).toString(),
						override, mapper.writeValueAsString(obj.get(DATAFLOW_DATA)));

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
			} catch (JsonProcessingException e) {
				log.error("Error importing Dataflow data. Error parsing JSON data {}",
						obj.get(IDENTIFICATION).toString());
				errors.addError(
						new MigrationError(new Instance(Pipeline.class, id, obj.get(IDENTIFICATION).toString(), null),
								null, MigrationError.ErrorType.ERROR, "There was an error importing an entity"));
			} catch (UnsupportedEncodingException e) {
				log.error("Error importing Dataflow data {}. {}", obj.get(IDENTIFICATION).toString());
				errors.addError(
						new MigrationError(new Instance(Pipeline.class, id, obj.get(IDENTIFICATION).toString(), null),
								null, MigrationError.ErrorType.ERROR, "There was an error importing an entity"));
			} catch (ClientErrorException e) {
				log.error("Error importing Dataflow data {}. {}", obj.get(IDENTIFICATION).toString(), e.getMessage());
				errors.addError(
						new MigrationError(new Instance(Pipeline.class, id, obj.get(IDENTIFICATION).toString(), null),
								null, MigrationError.ErrorType.ERROR, "There was an error importing an entity"));
			}

		}
		return errors;
	}

}
