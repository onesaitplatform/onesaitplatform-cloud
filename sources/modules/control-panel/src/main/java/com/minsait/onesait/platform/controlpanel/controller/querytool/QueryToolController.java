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
package com.minsait.onesait.platform.controlpanel.controller.querytool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.exception.SQLGrammarException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.MigrationData;
import com.minsait.onesait.platform.config.model.MigrationData.DataType;
import com.minsait.onesait.platform.config.model.MigrationData.Status;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.exceptions.OntologyServiceException;
import com.minsait.onesait.platform.config.services.migration.MigrationService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyDTO;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataService;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataUnauthorizedException;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.exceptions.QueryNativeFormatException;
import com.minsait.onesait.platform.persistence.factory.ManageDBRepositoryFactory;
import com.minsait.onesait.platform.persistence.interfaces.ManageDBRepository;
import com.minsait.onesait.platform.persistence.services.QueryToolService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import au.com.bytecode.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/querytool")
@Slf4j
public class QueryToolController {

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private OntologyDataService ontologyDataService;

	@Autowired
	private QueryToolService queryToolService;

	@Autowired
	private ManageDBRepositoryFactory manageFactory;

	@Autowired
	private AppWebUtils utils;

	@Autowired
	private UserService userService;

	@Autowired
	MigrationService migrationService;

	@Autowired
	private IntegrationResourcesService resourcesServices;

	@Value("${onesaitplatform.queryTool.allowedOperations:false}")
	private Boolean queryToolAllowedOperations;

	@Value("${onesaitplatform.binary-repository.tmp.file.path:/tmp/files/}")
	private String tmpDir;

	@Value("${onesaitplatform.database.mongodb.queries.defaultLimit:1000}")
	private int queryDefaultLimit;

	public static final String QUERY_SQL = "SQL";
	public static final String QUERY_NATIVE = "NATIVE";
	private static final String QUERY_RESULT_STR = "queryResult";
	private static final String QUERY_TOOL_SHOW_QUERY = "querytool/show :: query";
	private static final String CONTEXT_USER = "$context.userId";
	private static final String RUNQUERYERROR = "Error in runQuery";
	private static final String APPLICATION_DOWNLOAD = "application/x-download";
	private static final String CONTENT_DISPOSITION = "Content-Disposition";
	private static final String ATTACHMENT = "attachment; filename=data.json";
	private static final String PRAGMA = "Pragma";
	private static final String CACHE_CONTROL = "Cache-Control";
	private static final String NO_CACHE_STR = "no-cache";

	@GetMapping("show")
	public String show(Model model) {
		final List<OntologyDTO> ontologies = ontologyService
				.getAllOntologiesForListWithProjectsAccess(utils.getUserId());

		final List<String> tables = queryToolService.getTables();
		model.addAttribute("ontologies", ontologies);
		model.addAttribute("userRole", utils.getRole());
		model.addAttribute("configDBTables", tables);
		return "querytool/show";

	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping("queryconfigdb")
	public String runQueryConfigDB(Model model, @RequestParam String query, @RequestParam String tableName)
			throws JsonProcessingException {
		query = query.trim();
		List<String> queryResult = new LinkedList<>();

		try {
			if (query.toLowerCase().startsWith("select") && query.split(";").length == 1) {
				queryResult = queryToolService.querySQLtoConfigDB(query);
				model.addAttribute(QUERY_RESULT_STR, queryResult);
			} else if (queryToolAllowedOperations) {
				queryResult = queryToolService.updateSQLtoConfigDB(query);
				model.addAttribute(QUERY_RESULT_STR, queryResult);
			} else {
				model.addAttribute(QUERY_RESULT_STR,
						utils.getMessage("querytool.error.operation", "Unallowed operation"));
			}
			return QUERY_TOOL_SHOW_QUERY;
		} catch (final SQLGrammarException e) {
			log.error("Error while executing SQL query in ConfigDB {}", e.getMessage());
			model.addAttribute(QUERY_RESULT_STR, e.getSQLException());
			return QUERY_TOOL_SHOW_QUERY;
		} catch (final RuntimeException e) {
			log.error("Error while executing SQL query in ConfigDB {}", e.getMessage());
			model.addAttribute(QUERY_RESULT_STR, e.getMessage());
			return QUERY_TOOL_SHOW_QUERY;
		}

	}

	@PostMapping("query")
	public String runQuery(Model model, @RequestParam String queryType, @RequestParam String query,
			@RequestParam String ontologyIdentification) throws JsonProcessingException {
		String queryResult = null;

		/*
		 * final Ontology ontology =
		 * ontologyService.getOntologyByIdentification(ontologyIdentification,
		 * utils.getUserId());
		 */

		try {
			final Ontology ontology = ontologyService.getOntologyByIdentification(ontologyIdentification,
					utils.getUserId());
			if (ontologyService.hasUserPermissionForQuery(utils.getUserId(), ontologyIdentification)) {
				final ManageDBRepository manageDB = manageFactory.getInstance(ontologyIdentification);
				if (!ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)
						&& !ontology.getRtdbDatasource().equals(RtdbDatasource.API_REST)
						&& manageDB.getListOfTables4Ontology(ontologyIdentification).isEmpty()) {
					manageDB.createTable4Ontology(ontologyIdentification, "{}", null);
				}
				query = query.replace(CONTEXT_USER, utils.getUserId());
				if (queryType.toUpperCase().equals(QUERY_SQL)
						&& !ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)) {
					queryResult = queryToolService.querySQLAsJson(utils.getUserId(), ontologyIdentification, query, 0);
					model.addAttribute(QUERY_RESULT_STR, queryResult);
					return QUERY_TOOL_SHOW_QUERY;

				} else if (queryType.toUpperCase().equals(QUERY_NATIVE)
						|| ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)) {
					queryResult = queryToolService.queryNativeAsJson(utils.getUserId(), ontologyIdentification, query);
					model.addAttribute(QUERY_RESULT_STR, queryResult);
					return QUERY_TOOL_SHOW_QUERY;
				} else {
					return utils.getMessage("querytool.querytype.notselected", "Please select queryType Native or SQL");
				}
			} else {
				model.addAttribute(QUERY_RESULT_STR, utils.getMessage("querytool.ontology.access.denied.json",
						"You don't have permissions for this ontology"));
				return QUERY_TOOL_SHOW_QUERY;
			}

		} catch (final QueryNativeFormatException e) {
			log.error(RUNQUERYERROR, e);
			model.addAttribute(QUERY_RESULT_STR, "Malformed Query.");
			return QUERY_TOOL_SHOW_QUERY;
		} catch (final DBPersistenceException e) {
			log.error(RUNQUERYERROR, e);
			model.addAttribute(QUERY_RESULT_STR, e.getMessage());
			return QUERY_TOOL_SHOW_QUERY;
		} catch (final OntologyServiceException e) {
			model.addAttribute(QUERY_RESULT_STR, utils.getMessage("querytool.ontology.access.denied.json",
					"You don't have permissions for this ontology"));
			return QUERY_TOOL_SHOW_QUERY;
		} catch (final Exception e) {
			log.error(RUNQUERYERROR, e);
			model.addAttribute(QUERY_RESULT_STR, utils.getMessage("querytool.query.native.error", e.getMessage()));
			return QUERY_TOOL_SHOW_QUERY;
		}

	}

	@PostMapping("compile")
	public String compile(Model model, @RequestParam String queryType, @RequestParam String query,
			@RequestParam String ontologyIdentification) throws JsonProcessingException {
		final Ontology ontology = ontologyService.getOntologyByIdentification(ontologyIdentification,
				utils.getUserId());
		try {
			if (ontology != null && queryType.equalsIgnoreCase(QUERY_SQL)
					&& !ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)) {
				final String queryResult = queryToolService.compileSQLQueryAsJson(utils.getUserId(), ontology, query,
						0);
				model.addAttribute(QUERY_RESULT_STR, queryResult);
				return QUERY_TOOL_SHOW_QUERY;
			} else {
				model.addAttribute(QUERY_RESULT_STR,
						utils.getMessage("querytool.error.sqlonly", "Please select queryType Native or SQL"));
				return QUERY_TOOL_SHOW_QUERY;

			}
		} catch (final DBPersistenceException e) {
			log.error(RUNQUERYERROR, e);
			model.addAttribute(QUERY_RESULT_STR, e.getMessage());
			return QUERY_TOOL_SHOW_QUERY;
		} catch (final Exception e) {
			log.error(RUNQUERYERROR, e);
			model.addAttribute(QUERY_RESULT_STR,
					utils.getMessage("querytool.query.native.error", "Error malformed query"));
			return QUERY_TOOL_SHOW_QUERY;
		}

	}

	@PostMapping("ontologyfields")
	public String getOntologyFields(Model model, @RequestParam String ontologyIdentification) throws IOException {

		model.addAttribute("fields",
				ontologyService.getOntologyFieldsQueryTool(ontologyIdentification, utils.getUserId()));
		return "querytool/show :: fields";

	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping("tableColumns")
	public String getTableColumns(Model model, @RequestParam String tableName) {

		model.addAttribute("fields", queryToolService.getTableColumns(tableName));
		return "querytool/show :: fields";

	}

	@PostMapping("relations")
	public String getOntologyRelations(Model model, @RequestParam String ontologyIdentification) throws IOException {
		model.addAttribute("relations", ontologyDataService.getOntologyReferences(ontologyIdentification));
		final Ontology ontology = ontologyService.getOntologyByIdentification(ontologyIdentification,
				utils.getUserId());
		if (ontology != null)
			model.addAttribute("datasource", ontology.getRtdbDatasource().name());
		return "querytool/show :: relations";
	}

	@GetMapping("/rtdb/{ontology}")
	public @ResponseBody String getRtdb(Model model, @PathVariable("ontology") String ontologyIdentification) {
		return ontologyService.getRtdbFromOntology(ontologyIdentification);
	}

	@GetMapping(value = "/checkDownloadExport")
	public ResponseEntity<String> checkDownloadExport(Model model, HttpServletResponse response,
			HttpServletRequest request) throws IllegalArgumentException, IllegalAccessException, IOException {
		User loggedUser = userService.getUser(utils.getUserId());

		final List<MigrationData> migrationDataFinished = migrationService.findByUserAndTypeAndStatus(loggedUser,
				DataType.QUERY, Status.FINISHED);

		if (migrationDataFinished != null && !migrationDataFinished.isEmpty() && migrationDataFinished.size() == 1) {
			return new ResponseEntity<>("true", HttpStatus.OK);
		}

		final List<MigrationData> migrationDataError = migrationService.findByUserAndTypeAndStatus(loggedUser,
				DataType.QUERY, Status.ERROR);

		if (migrationDataError != null && !migrationDataError.isEmpty() && migrationDataError.size() == 1) {
			migrationService.deleteMigrationData(migrationDataError.get(0));
			return new ResponseEntity<>("error", HttpStatus.OK);
		}

		final List<MigrationData> migrationDataInProgress = migrationService.findByUserAndTypeAndStatus(loggedUser,
				DataType.QUERY, Status.IN_PROGRESS);

		if (migrationDataInProgress != null && !migrationDataInProgress.isEmpty()
				&& migrationDataInProgress.size() == 1) {
			return new ResponseEntity<>("false", HttpStatus.OK);
		}

		return new ResponseEntity<>("null", HttpStatus.OK);
	}

	@GetMapping(value = "/getTypeDownload")
	public ResponseEntity<String> getTypeDownload(Model model, HttpServletResponse response, HttpServletRequest request)
			throws IllegalArgumentException, IllegalAccessException, IOException {
		User loggedUser = userService.getUser(utils.getUserId());
		final List<MigrationData> migrationDataFinished = migrationService.findByUserAndTypeAndStatus(loggedUser,
				DataType.QUERY, Status.FINISHED);
		if (!migrationDataFinished.isEmpty())
			return new ResponseEntity<>(migrationDataFinished.get(0).getDescription(), HttpStatus.OK);
		else
			return new ResponseEntity<>("There are no data to download.", HttpStatus.NOT_FOUND);

	}

	@GetMapping(value = "/downloadJSON")
	public ResponseEntity<String> downloadJSON(Model model, HttpServletResponse response, HttpServletRequest request)
			throws IllegalArgumentException, IllegalAccessException, IOException {
		User loggedUser = userService.getUser(utils.getUserId());
		final List<MigrationData> migrationDataFinished = migrationService.findByUserAndTypeAndStatus(loggedUser,
				DataType.QUERY, Status.FINISHED);

		ResponseEntity<String> output = new ResponseEntity<>(new String(migrationDataFinished.get(0).getFile()),
				HttpStatus.OK);
		response.setContentType(APPLICATION_DOWNLOAD);
		response.setHeader(CONTENT_DISPOSITION, ATTACHMENT);
		response.setHeader(PRAGMA, NO_CACHE_STR);
		response.setHeader(CACHE_CONTROL, NO_CACHE_STR);

		migrationService.deleteMigrationData(migrationDataFinished.get(0));
		return output;

	}

	@GetMapping(value = "/downloadCSV")
	public ResponseEntity<InputStreamResource> downloadExport(Model model, HttpServletResponse response,
			HttpServletRequest request) throws IllegalArgumentException, IllegalAccessException, IOException {
		User loggedUser = userService.getUser(utils.getUserId());
		final List<MigrationData> migrationDataFinished = migrationService.findByUserAndTypeAndStatus(loggedUser,
				DataType.QUERY, Status.FINISHED);

		final File file = createFile(tmpDir + File.separator + UUID.randomUUID());
		FileWriter outputfile = new FileWriter(file.getAbsolutePath() + File.separator + "OntologyResult.csv");
		CSVWriter writer = new CSVWriter(outputfile, ';', CSVWriter.NO_QUOTE_CHARACTER,
				CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

		List<String[]> csvData = new ArrayList<>();
		List<String> data = Arrays.asList(new String(migrationDataFinished.get(0).getFile())
				.substring(1, new String(migrationDataFinished.get(0).getFile()).length() - 1).split("],"));
		for (String d : data) {
			String[] array = d.startsWith("[") ? d.substring(1).split("\",") : d.split("\",");
			for (int i = 0; i < array.length; i++) {
				array[i] = array[i].replace("\\\"", "\"");
				array[i] = array[i].endsWith("]") ? array[i].substring(0, array[i].length() - 1) : array[i];
				array[i] = array[i].endsWith("\"") ? array[i].substring(0, array[i].length() - 1) : array[i];
				array[i] = array[i].startsWith("\"") ? array[i].substring(1) : array[i];
			}
			csvData.add(array);
		}

		writer.writeAll(csvData);
		writer.close();

		File finalFile = new File(file.getAbsolutePath() + File.separator + "OntologyResult.csv");
		final HttpHeaders respHeaders = new HttpHeaders();
		respHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		respHeaders.setContentDispositionFormData("attachment", finalFile.getName());
		respHeaders.setContentLength(finalFile.length());
		final InputStreamResource isr = new InputStreamResource(new FileInputStream(finalFile));
		deleteDirectory(finalFile);
		migrationService.deleteMigrationData(migrationDataFinished.get(0));
		return new ResponseEntity<>(isr, respHeaders, HttpStatus.OK);

	}

	@PostMapping("download")
	public ResponseEntity<String> download(Model model, @RequestParam String downloadType,
			@RequestParam String queryType, @RequestParam String query, @RequestParam String ontologyIdentification) {

		ExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		User loggedUser = userService.getUser(utils.getUserId());
		try {
			MigrationData migrationData = migrationService.findMigrationData(loggedUser, DataType.QUERY);

			Boolean status = checkExport(migrationData);
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

			final Ontology ontology = ontologyService.getOntologyByIdentification(ontologyIdentification,
					utils.getUserId());
			if (ontologyService.hasUserPermissionForQuery(utils.getUserId(), ontologyIdentification)) {
				query = query.replace(CONTEXT_USER, utils.getUserId());
				if (migrationData != null)
					migrationService.deleteMigrationData(migrationData);

				migrationService.storeMigrationData(loggedUser, "exportQuery", downloadType, "exportQuery", null,
						DataType.QUERY, Status.IN_PROGRESS);
				executor.execute(downloadRunnable(loggedUser, downloadType, queryType, query, ontology));
				return new ResponseEntity<>(HttpStatus.OK);
			} else {
				return new ResponseEntity<>(utils.getMessage("querytool.ontology.access.denied.json",
						"You don't have permissions for this ontology."), HttpStatus.FORBIDDEN);
			}

		} catch (final OntologyServiceException e) {
			return new ResponseEntity<>(utils.getMessage("querytool.ontology.access.denied.json",
					"You don't have permissions for this ontology."), HttpStatus.FORBIDDEN);
		} catch (final Exception e) {
			log.error(RUNQUERYERROR, e);
			return new ResponseEntity<>(
					utils.getMessage("migration.export.error",
							"There was an error with the export, please contact the support team."),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	private Runnable downloadRunnable(User loggedUser, String downloadType, String queryType, String query,
			Ontology ontology) {

		return new Runnable() {

			public static final String QUERY_SQL = "SQL";
			public static final String QUERY_NATIVE = "NATIVE";
			public static final String RUNQUERYERROR = "Error in runQuery";

			@Override
			public void run() {
				String queryResult = null;

				if (queryType.toUpperCase().equals(QUERY_SQL)
						&& !ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)) {
					try {
						queryResult = queryToolService.querySQLAsJson(loggedUser.getUserId(),
								ontology.getIdentification(), query, 0, loggedUser.isAdmin() ? -1 : getMaxRegisters());
					} catch (DBPersistenceException | OntologyDataUnauthorizedException | GenericOPException e) {
						log.error(RUNQUERYERROR, e);
						migrationService.updateStoreMigrationData(loggedUser, null, DataType.QUERY);
					}
				} else if (queryType.toUpperCase().equals(QUERY_NATIVE)
						|| ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)) {
					queryResult = queryToolService.queryNativeAsJson(loggedUser.getUserId(),
							ontology.getIdentification(), query, 0, loggedUser.isAdmin() ? -1 : getMaxRegisters());
				}
				if (downloadType.equals("CSV")) {
					try {
						queryResult = new ObjectMapper()
								.writeValueAsString(generateCSVResult(queryResult, ontology.getIdentification()));
					} catch (JsonProcessingException e) {
						log.error("error parsing result query on CSV format", e);
						migrationService.updateStoreMigrationData(loggedUser, null, DataType.QUERY);
					}
				}
				if (queryResult == null)
					migrationService.updateStoreMigrationData(loggedUser, null, DataType.QUERY);
				else
					migrationService.updateStoreMigrationData(loggedUser, queryResult, DataType.QUERY);

			}
		};

	}

	private int getMaxRegisters() {
		try {
			return ((Integer) resourcesServices.getGlobalConfiguration().getEnv().getDatabase().get("queries-limit"))
					.intValue();
		} catch (final Exception e) {
			return queryDefaultLimit;
		}
	}

	private Boolean checkExport(MigrationData migrationData) {
		if (migrationData != null && migrationData.getStatus().equals(Status.ERROR)) {
			return null;
		}
		if (migrationData != null && migrationData.getType().equals(DataType.QUERY)
				&& migrationData.getStatus().equals(Status.IN_PROGRESS)) {
			return false;
		}
		return true;
	}

	private File createFile(String path) {

		log.info("New file is going to be generate on: " + path);

		final File file = new File(path);

		if (!file.exists()) {
			final Boolean success = file.mkdirs();
			if (!success) {
				log.error("Creating values file for deploy OP falied.");
				return null;
			}
		} else {
			log.error("Creating values file for deploy OP falied, the temporary directory don't exist: "
					+ file.getAbsolutePath());
			return null;
		}

		return file;

	}

	private boolean deleteDirectory(File directoryToBeDeleted) {
		final File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (final File file : allContents) {
				deleteDirectory(file);
			}
		}
		return directoryToBeDeleted.delete();
	}

	private List<String[]> generateCSVResult(String queryResult, String ontologyIdentificadion) {
		try {
			JsonNode jsonTree = new ObjectMapper().readTree(queryResult);
			List<String[]> csvData = new ArrayList<>();

			JsonNode firstObject = jsonTree.elements().next();
			List<String> headers = new ArrayList<>();
			firstObject.fieldNames().forEachRemaining(fieldName -> {
				headers.add(fieldName);
			});
			csvData.add(headers.toArray(new String[0]));
			Iterator<JsonNode> iterator = jsonTree.elements();
			while (iterator.hasNext()) {
				JsonNode obj = iterator.next();
				List<String> data = new ArrayList<>();
				obj.fields().forEachRemaining(field -> {
					JsonNode node = field.getValue();
					if (node.isObject())
						try {
							data.add(new ObjectMapper().writeValueAsString(field.getValue()));
						} catch (JsonProcessingException e) {
							log.error("Error parsing query result to CSV format to export", e);
						}
					else
						data.add(field.getValue().asText());
				});
				csvData.add(data.toArray(new String[0]));
			}

			return csvData;
		} catch (IOException e) {
			log.error("Error parsing query result to CSV format to export", e);
			return null;
		}
	}

}
