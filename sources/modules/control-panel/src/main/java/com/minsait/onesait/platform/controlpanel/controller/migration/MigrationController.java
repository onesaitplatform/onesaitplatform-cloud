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
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.model.MigrationData;
import com.minsait.onesait.platform.config.model.Notebook;
import com.minsait.onesait.platform.config.model.Pipeline;
import com.minsait.onesait.platform.config.model.User;
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
import com.mongodb.util.JSON;

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
	private static final String USER = "com.minsait.onesait.platform.config.model.UserExport";
	private static final String DOMAIN_DATA = "domainData";
	private static final String IDENTIFICATION = "identification";
	private static final String NOTEBOOK_DATA = "notebookData";
	private static final String IDZEP = "idzep";
	private static final String DATAFLOW_DATA = "dataflowData";

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
		model.addAttribute("ontologies", ontologyService.getAllOntologiesForList(utils.getUserId(), null, null));
		return MIGRATION_SHOW;
	}

	@GetMapping(value = "/export")
	public ResponseEntity<String> export(Model model, HttpServletResponse response, HttpServletRequest request)
			throws IllegalAccessException, IOException {
		ExportResult allData = migrationService.exportAll();
		allData = this.exportDomain(allData);
		allData = this.exportNotebooks(allData);
		allData = this.exportDataflow(allData);
		String json = migrationService.getJsonFromData(allData.getData());
		ResponseEntity<String> output = new ResponseEntity<>(json, HttpStatus.OK);
		response.setContentType(APPLICATION_DOWNLOAD);
		response.setHeader(CONTENT_DISPOSITION, ATTACHMENT);
		response.setHeader(PRAGMA, NO_CACHE_STR);
		response.setHeader(CACHE_CONTROL, NO_CACHE_STR);
		return output;
	}

	@GetMapping(value = "/exportUser/{id}")
	public ResponseEntity<String> exportUser(Model model, HttpServletResponse response, HttpServletRequest request,
			@PathVariable("id") String userId) throws IllegalArgumentException, IllegalAccessException, IOException {
		ExportResult userData = migrationService.exportUser(userService.getUser(userId));
		userData = this.exportDomain(userData);
		userData = this.exportNotebooks(userData);
		userData = this.exportDataflow(userData);
		String json = migrationService.getJsonFromData(userData.getData());
		ResponseEntity<String> output = new ResponseEntity<>(json, HttpStatus.OK);
		response.setContentType(APPLICATION_DOWNLOAD);
		response.setHeader(CONTENT_DISPOSITION, ATTACHMENT);
		response.setHeader(PRAGMA, NO_CACHE_STR);
		response.setHeader(CACHE_CONTROL, NO_CACHE_STR);
		return output;
	}

	@GetMapping(value = "/exportUsers/{users}")
	public ResponseEntity<String> exportUsers(Model model, HttpServletResponse response, HttpServletRequest request,
			@PathVariable("users") List<String> users)
			throws IllegalArgumentException, IllegalAccessException, IOException {
		ExportResult userData = migrationService.exportUsers(users);
		String json = migrationService.getJsonFromData(userData.getData());
		ResponseEntity<String> output = new ResponseEntity<>(json, HttpStatus.OK);
		response.setContentType(APPLICATION_DOWNLOAD);
		response.setHeader(CONTENT_DISPOSITION, ATTACHMENT);
		response.setHeader(PRAGMA, NO_CACHE_STR);
		response.setHeader(CACHE_CONTROL, NO_CACHE_STR);
		return output;
	}

	@GetMapping(value = "/exportProject/{project}")
	public ResponseEntity<String> exportProject(Model model, HttpServletResponse response, HttpServletRequest request,
			@PathVariable("project") String project)
			throws IllegalArgumentException, IllegalAccessException, IOException {
		ExportResult userData = migrationService.exportProject(project);
		String json = migrationService.getJsonFromData(userData.getData());
		ResponseEntity<String> output = new ResponseEntity<>(json, HttpStatus.OK);
		response.setContentType(APPLICATION_DOWNLOAD);
		response.setHeader(CONTENT_DISPOSITION, ATTACHMENT);
		response.setHeader(PRAGMA, NO_CACHE_STR);
		response.setHeader(CACHE_CONTROL, NO_CACHE_STR);
		return output;
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
					importData.getFileName(), bytes);

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
		MigrationData migrationData = migrationService.findMigrationData(user);
		if (migrationData == null) {
			throw new IllegalStateException("The file should be loaded previouly");
		}
		String json = new String(migrationData.getFile(), StandardCharsets.UTF_8);
		DataFromDB data = migrationService.getDataFromJson(json);

		MigrationConfiguration config = new MigrationConfiguration();

		for (Class<?> clazz : data.getClasses()) {
			if (importData.getClasses().contains(clazz.getName())) {
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
		if (importData.getClasses().contains(PROJECT_EXPORT)) {
			errors = migrationService.importData(config, data, true, false, importData.getOverride());
		} else if (importData.getClasses().contains(USER)) {
			errors = migrationService.importData(config, data, false, true, importData.getOverride());
		} else {
			errors = migrationService.importData(config, data, false, false, importData.getOverride());
		}
		this.importDomain(data, errors, importData.getOverride());
		errors = this.importNotebook(data, errors, importData.getOverride());
		errors = this.importDataflow(data, errors, importData.getOverride());

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

	private void importDomain(DataFromDB data, MigrationErrors errors, Boolean override) {
		// if data has FlowDomain.class then all domain data are exported too
		Iterator<Serializable> iterator = data.getInstances(FlowDomain.class).iterator();
		ObjectMapper mapper = new ObjectMapper();
		while (iterator.hasNext()) {
			Serializable id = iterator.next();
			Map<String, Object> obj = data.getInstanceData(FlowDomain.class, id);
			try {
				ResponseEntity<String> result = flowengineController.importFlowDomainToUserAdmin(
						mapper.writeValueAsString(obj.get(DOMAIN_DATA)), obj.get(IDENTIFICATION).toString(),
						obj.get("user").toString(), override);
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

	private ExportResult exportNotebooks(ExportResult data) {
		// if data has Notebook.class then all notebooks data are exported too
		Iterator<Serializable> iterator = data.getData().getInstances(Notebook.class).iterator();
		while (iterator.hasNext()) {
			Serializable id = iterator.next();
			Map<String, Object> obj = data.getData().getInstanceData(Notebook.class, id);

			ResponseEntity<?> result = notebookController.exportNotebook(obj.get(IDZEP).toString());
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
		Iterator<Serializable> iterator = data.getInstances(Notebook.class).iterator();
		ObjectMapper mapper = new ObjectMapper();
		while (iterator.hasNext()) {
			Serializable id = iterator.next();
			Map<String, Object> obj = data.getInstanceData(Notebook.class, id);

			try {
				ResponseEntity<?> result = notebookController.importNotebook(obj.get(IDENTIFICATION).toString(),
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

	private ExportResult exportDataflow(ExportResult data) {
		// if data has Pipeline.class then all pipelines data are exported too
		Iterator<Serializable> iterator = data.getData().getInstances(Pipeline.class).iterator();
		while (iterator.hasNext()) {
			Serializable id = iterator.next();
			Map<String, Object> obj = data.getData().getInstanceData(Pipeline.class, id);

			try {
				ResponseEntity<?> result = dataflowController.exportPipeline(obj.get(IDENTIFICATION).toString());
				if (result.getStatusCode().equals(HttpStatus.OK)) {
					obj.put(DATAFLOW_DATA, JSON.parse(result.getBody().toString()));
				} else {
					log.error("Error exporting dataflow data {}. StatusCode {}", obj.get(IDENTIFICATION).toString(),
							result.getStatusCode().name());
					obj.put(DATAFLOW_DATA, JSON.parse("[]"));
				}
			} catch (UnsupportedEncodingException e) {
				log.error("Error exportin dataflow data: {}. {}", obj.get(IDENTIFICATION).toString(), e);
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
				ResponseEntity<?> result = dataflowController.importPipeline(obj.get(IDENTIFICATION).toString(),
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
			}

		}
		return errors;
	}

}
