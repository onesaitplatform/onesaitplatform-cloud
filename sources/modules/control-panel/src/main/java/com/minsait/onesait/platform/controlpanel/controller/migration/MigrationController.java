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

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.config.model.MigrationData;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.migration.DataFromDB;
import com.minsait.onesait.platform.config.services.migration.ExportResult;
import com.minsait.onesait.platform.config.services.migration.MigrationConfiguration;
import com.minsait.onesait.platform.config.services.migration.MigrationErrors;
import com.minsait.onesait.platform.config.services.migration.MigrationService;
import com.minsait.onesait.platform.config.services.migration.SchemaFromDB;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import de.galan.verjson.core.IOReadException;
import de.galan.verjson.core.NamespaceMismatchException;
import de.galan.verjson.core.VersionNotSupportedException;
import de.galan.verjson.step.ProcessStepException;

@Controller
@PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
@RequestMapping("/migration")
public class MigrationController {

	@Autowired
	MigrationService migrationService;

	@Autowired
	private UserService userService;

	@Autowired
	private AppWebUtils utils;

	private static final String IMPORT_DATA_STR = "importData";
	private static final String OTHER_SCHEMA_STR = "otherSchema";
	private static final String CLASS_NAMES_STR = "classNames";
	private static final String MIGRATION_SHOW = "migration/show";
	private static final String NO_CACHE_STR = "no-cache";

	@GetMapping(value = "/show", produces = "text/html")
	public String show(Model model, HttpServletRequest request) {
		ImportData importData = new ImportData();
		ImportData otherSchema = new ImportData();
		model.addAttribute(IMPORT_DATA_STR, importData);
		model.addAttribute(OTHER_SCHEMA_STR, otherSchema);
		model.addAttribute(CLASS_NAMES_STR, new ArrayList<String>());
		model.addAttribute("selectedClasses", new SelectedClasses());
		return MIGRATION_SHOW;
	}

	@GetMapping(value = "/export")
	public ResponseEntity<String> export(Model model, HttpServletResponse response, HttpServletRequest request)
			throws IllegalAccessException, IOException {
		ExportResult allData = migrationService.exportAll();
		String json = migrationService.getJsonFromData(allData.getData());
		ResponseEntity<String> output = new ResponseEntity<>(json, HttpStatus.OK);
		response.setContentType("application/x-download");
		response.setHeader("Content-Disposition", "attachment; filename=data.json");
		response.setHeader("Pragma", NO_CACHE_STR);
		response.setHeader("Cache-Control", NO_CACHE_STR);
		// response.getOutputStream().write(json.getBytes());
		return output;
	}

	@GetMapping(value = "/exportSchema")
	public ResponseEntity<String> exportSchema(Model model, HttpServletResponse response, HttpServletRequest request)
			throws IOException {
		SchemaFromDB schema = migrationService.exportSchema();
		String json = migrationService.getJsonFromSchema(schema);
		ResponseEntity<String> output = new ResponseEntity<>(json, HttpStatus.OK);
		response.setContentType("application/x-download");
		response.setHeader("Content-Disposition", "attachment; filename=schema.json");
		response.setHeader("Pragma", NO_CACHE_STR);
		response.setHeader("Cache-Control", NO_CACHE_STR);
		// response.getOutputStream().write(json.getBytes());
		return output;
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
			// ResponseEntity<String> output = new ResponseEntity<String>(diffs,
			// HttpStatus.OK);
			// response.setContentType("application/x-download");
			// response.setHeader("Content-Disposition", "attachment; filename=diffs.json");
			// response.setHeader("Pragma", NO_CACHE_STR);
			// response.setHeader("Cache-Control", NO_CACHE_STR);
			// return output;

			// } else {
			// return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
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

			List<String> classNames = new ArrayList<>();
			for (Class<?> clazz : data.getClasses()) {
				classNames.add(clazz.getName());
			}
			model.addAttribute(CLASS_NAMES_STR, classNames);
			importData.setClasses(classNames);
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
					config.add(clazz, id);
				}
			}
		}

		model.addAttribute(CLASS_NAMES_STR, new ArrayList<String>());
		model.addAttribute(IMPORT_DATA_STR, new ImportData());
		model.addAttribute(OTHER_SCHEMA_STR, new ImportData());

		MigrationErrors errors = migrationService.importData(config, data);
		model.addAttribute("errors", errors.getErrors());

		return MIGRATION_SHOW;
	}

}
