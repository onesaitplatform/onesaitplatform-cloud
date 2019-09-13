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
package com.minsait.onesait.platform.controlpanel.rest.management.notebook;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.validation.Valid;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.minsait.onesait.platform.config.model.Notebook;
import com.minsait.onesait.platform.config.model.NotebookUserAccess;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.NotebookRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.exceptions.NotebookServiceException;
import com.minsait.onesait.platform.config.services.exceptions.NotebookServiceException.Error;
import com.minsait.onesait.platform.config.services.notebook.NotebookService;
import com.minsait.onesait.platform.controlpanel.rest.NotebookOpsRestServices;
import com.minsait.onesait.platform.controlpanel.rest.management.model.ErrorValidationResponse;
import com.minsait.onesait.platform.controlpanel.rest.management.notebook.model.NotebookUserAccessSimplified;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(value = "Notebook Ops", tags = { "Notebook Ops service" })
@RestController
@RequestMapping("api/notebooks")
public class NotebookManagementController extends NotebookOpsRestServices {

	private static final String MSG_ERROR_JSON_RESPONSE = "{\"error\":\"%s\", \"msg\":\"%s\"}";
	private static final String MSG_NOTEBOOK_NOT_FOUND = "Notebook not found with id %s";
	private static final String MSG_NONE_NOTEBOOK_FOUND = "None notebook found";
	private static final String MSG_BAD_REQUEST = "Bad request";
	private static final String MSG_USER_NOT_FOUND = "User not found with id %s";
	private static final String MSG_USER_NOT_ALLOWED = "User with id %s is not authorized";
	private static final String MSG_INTERNAL_SERVER_ERROR = "Internal server error";
	private static final String MSG_DUPLICATE_NOTEBOOK_IDENT = "Duplicated notebook identification";
		
	@Autowired
	private NotebookService notebookService;
	@Autowired
	private NotebookRepository notebookRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private AppWebUtils utils;

	@ApiOperation(value = "Runs paragraph synchronously by identification or id")
	@PostMapping(value = "/run/notebook/{notebookId}/paragraph/{paragraphId}")
	public ResponseEntity<?> runParagraph(
			@ApiParam(value = "Notebook Zeppelin Id", required = true) @PathVariable("notebookId") String notebookId,
			@ApiParam(value = "Paragraph Id", required = true) @PathVariable(name = "paragraphId") String paragraphId,
			@ApiParam(value = "Input parameters") @RequestBody(required = false) String parameters) {

		final String userId = utils.getUserId();
		Notebook nt = notebookService.getNotebook(notebookId);
		final boolean authorized = notebookService.hasUserPermissionForNotebook(nt.getIdzep(), userId);

		if (authorized) {
			try {
				return notebookService.runParagraph(nt.getIdzep(), paragraphId, parameters != null ? parameters : "");
			} catch (final Exception e) {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

	}

	@ApiOperation(value = "Runs all paragraphs synchronously by identification or id")
	@PostMapping(value = "/run/notebook/{notebookId}")
	public ResponseEntity<?> runAllParagraphs(
			@ApiParam(value = "Notebook Zeppelin Id", required = true) @PathVariable("notebookId") String notebookId) {

		final String userId = utils.getUserId();
		Notebook nt = notebookService.getNotebook(notebookId);
		final boolean authorized = notebookService.hasUserPermissionForNotebook(nt.getIdzep(), userId);

		if (authorized) {
			try {
				return notebookService.runAllParagraphs(nt.getIdzep());
			} catch (final Exception e) {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

	}

	@ApiOperation(value = "Get the results of a paragraph by identification or id")
	@GetMapping(value = "/result/notebook/{notebookId}/paragraph/{paragraphId}")
	public ResponseEntity<?> getParagraphResult(
			@ApiParam(value = "Notebook Zeppelin Id", required = true) @PathVariable("notebookId") String notebookId,
			@ApiParam(value = "Paragraph Id", required = true) @PathVariable(name = "paragraphId") String paragraphId) {

		final String userId = utils.getUserId();
		Notebook nt = notebookService.getNotebook(notebookId);
		final boolean authorized = notebookService.hasUserPermissionForNotebook(nt.getIdzep(), userId);

		if (authorized) {
			try {
				return notebookService.getParagraphResult(nt.getIdzep(), paragraphId);
			} catch (final Exception e) {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

	}

	@ApiOperation(value = "Get the status of all paragraphs by identification or id")
	@GetMapping(value = "/status/notebook/{notebookId}")
	public ResponseEntity<?> getAllParagraphStatus(
			@ApiParam(value = "Notebook Zeppelin Id", required = true) @PathVariable("notebookId") String notebookId) {

		final String userId = utils.getUserId();
		Notebook nt = notebookService.getNotebook(notebookId);
		final boolean authorized = notebookService.hasUserPermissionForNotebook(nt.getIdzep(), userId);

		if (authorized) {
			try {
				return notebookService.getAllParagraphStatus(nt.getIdzep());
			} catch (final Exception e) {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

	}

	@ApiOperation(value = "Clone a notebook by identification or id only zeppelin")
	@GetMapping(value = "/run/notebook/{notebookId}/{nameClone}")
	public ResponseEntity<?> cloneNotebook(
			@ApiParam(value = "Notebook Zeppelin Id", required = true) @PathVariable("notebookId") String notebookId,
			@ApiParam(value = "Name for the clone", required = true) @PathVariable("nameClone") String nameClone) {

		final String userId = utils.getUserId();
		Notebook nt = notebookService.getNotebook(notebookId);
		final boolean authorized = notebookService.hasUserPermissionForNotebook(nt.getIdzep(), userId);

		if (authorized) {
			try {
				final String id = notebookService.cloneNotebookOnlyZeppelin(nameClone, nt.getIdzep(), userId);
				return new ResponseEntity<>(id, HttpStatus.OK);
			} catch (final Exception e) {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

	}

	@ApiOperation(value = "Clone a notebook by identification or id and save db")
	@GetMapping(value = "/clone/notebook/{notebookId}/{nameClone}")
	public ResponseEntity<?> cloneNotebookSaveDB(
			@ApiParam(value = "Notebook Zeppelin Id", required = true) @PathVariable("notebookId") String notebookId,
			@ApiParam(value = "Name for the clone", required = true) @PathVariable("nameClone") String nameClone) {

		final String userId = utils.getUserId();
		Notebook nt = notebookService.getNotebook(notebookId);
		final boolean authorized = notebookService.hasUserPermissionForNotebook(nt.getIdzep(), userId);

		if (authorized) {
			try {
				final Notebook result = notebookService.cloneNotebook(nameClone, nt.getIdzep(), userId);
				return new ResponseEntity<>(result.getIdzep(), HttpStatus.OK);
			} catch (final Exception e) {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

	}

	@ApiOperation(value = "Create a notebook onesait")
	@PostMapping(value = "/create/notebook/{nameCreate}")
	public ResponseEntity<?> createEmptyNotebook(
			@ApiParam(value = "Name for the create", required = true) @PathVariable("nameCreate") String nameCreate) {

		final String userId = utils.getUserId();
		final boolean authorized = notebookService.hasUserPermissionCreateNotebook(userId);

		if (authorized) {
			try {
				final String id = notebookService.createEmptyNotebook(nameCreate, userId).getIdzep();
				return new ResponseEntity<>(id, HttpStatus.OK);
			} catch (final Exception e) {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

	}

	@ApiOperation(value = "Delete notebook by identification or id")
	@DeleteMapping(value = "/delete/notebook/{notebookId}")
	public ResponseEntity<?> deleteNotebook(
			@ApiParam(value = "Id notebook for the delete", required = true) @PathVariable("notebookId") String notebookId) {

		final String userId = utils.getUserId();

		try {
			Notebook nt = notebookService.getNotebook(notebookId);
			notebookService.removeNotebookByIdZep(nt.getIdzep(), userId);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (final Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@ApiOperation(value = "Get a paragraph information of notebook by identification or id")
	@GetMapping(value = "/api/notebook/{notebookId}/paragraph/{paragraphId}")
	public ResponseEntity<?> getParagraphInfo(
			@ApiParam(value = "Notebook Zeppelin Id", required = true) @PathVariable("notebookId") String notebookId,
			@ApiParam(value = "Paragraph Id", required = true) @PathVariable(name = "paragraphId") String paragraphId) {

		final String userId = utils.getUserId();
		Notebook nt = notebookService.getNotebook(notebookId);
		final boolean authorized = notebookService.hasUserPermissionForNotebook(nt.getIdzep(), userId);

		if (authorized) {
			try {
				return notebookService.getParagraphResult(nt.getIdzep(), paragraphId);
			} catch (final Exception e) {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

	}

	@ApiOperation(value = "List notebooks")
	@GetMapping(value = "/list/")
	public ResponseEntity<?> listNotebook() {
		List<Notebook> notebooks;
		final JSONObject notebooksInfo = new JSONObject();
		final String userId = utils.getUserId();
		final User user = userRepository.findByUserId(userId);

		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			notebooks = notebookRepository.findAll();
		} else {
			notebooks = notebookRepository.findByUser(user);
		}
		try {
			if (!notebooks.isEmpty()) {
				notebooks.stream().forEach(n -> notebooksInfo.put(n.getIdzep(), n.getIdentification()));
				return new ResponseEntity<>(notebooksInfo.toString(), HttpStatus.OK);
			} else {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, "", MSG_NONE_NOTEBOOK_FOUND), HttpStatus.NOT_FOUND);
			}

		} catch (final Exception e) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, "", MSG_INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	

	@ApiOperation(value = "Export notebook by identification or id")
	@GetMapping(value = "/export/{notebookId}")
	public ResponseEntity<?> exportNotebook(
			@ApiParam(value = "Id notebook for the export", required = true) @PathVariable("notebookId") String notebookId) {

		final String userId = utils.getUserId();
		Notebook nt = notebookService.getNotebook(notebookId);
		final boolean authorized = notebookService.hasUserPermissionInNotebook(nt, userId);

		if (authorized) {
			try {
				JSONObject notebookJson = notebookService.exportNotebook(nt.getIdzep(), userId);
				final HttpHeaders headers = notebookService.exportHeaders(nt.getIdentification());
				return new ResponseEntity<>(notebookJson.toString().getBytes(StandardCharsets.UTF_8), headers,
						HttpStatus.OK);
			} catch (final Exception e) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage(), MSG_INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, "", String.format(MSG_USER_NOT_ALLOWED, userId)), HttpStatus.UNAUTHORIZED);

	}

	@ApiOperation(value = "Export all notebooks")
	@GetMapping(value = "/export/")
	public ResponseEntity<?> exportAllNotebooks() {
		List<Notebook> notebooks;
		final JsonArray notebooksArray = new JsonArray();
		final String userId = utils.getUserId();
		final User user = userRepository.findByUserId(userId);

		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			notebooks = notebookRepository.findAll();
		} else {
			notebooks = notebookRepository.findByUser(user);
		}
		HttpHeaders headers = notebookService.exportHeaders(userId + "_notebooks");
		try {
			if (!notebooks.isEmpty()) {
				for (int i = 0; i < notebooks.size(); i++) {
					final Notebook n = notebooks.get(i);
					JsonObject notebookJson = new JsonParser().parse(notebookService.exportNotebook(n.getIdzep(), userId).toString()).getAsJsonObject();
					notebooksArray.add(notebookJson);
				}
				return new ResponseEntity<>(notebooksArray.toString().getBytes(Charset.forName("UTF-8")), headers,
						HttpStatus.OK);
			} else {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, "", MSG_NONE_NOTEBOOK_FOUND), HttpStatus.NOT_FOUND);
			}

		} catch (final Exception e) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage(), MSG_INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	
	private JsonObject importNotebookResponse(Notebook note) {
		final JsonObject jsonResponse = new JsonObject();
		jsonResponse.addProperty("id", note.getIdzep());
		jsonResponse.addProperty("identification", note.getIdentification());
		jsonResponse.addProperty("msg", "Successfully imported");
		return jsonResponse;
	}
	
	private JsonObject importNotebookResponse(String notebookId, String notebookName, String msg) {
		final JsonObject jsonResponse = new JsonObject();
		jsonResponse.addProperty("id", notebookId);
		jsonResponse.addProperty("identification", notebookName);
		jsonResponse.addProperty("msg", msg);
		return jsonResponse;
	}

	@ApiOperation(value = "Import notebook")
	@PostMapping(value = "/import/{notebookName}")
	public ResponseEntity<?> importNotebook(
			@ApiParam(value = "Notebook Zeppelin Name", required = true) @PathVariable("notebookName") String notebookName,
			@ApiParam(value = "Overwrite notebook if exists") @RequestParam(required = false, defaultValue = "false") boolean overwrite,
			@ApiParam(value = "Import authorizations if exist") @RequestParam(required = false, defaultValue = "false") boolean importAuthorizations,
			@ApiParam(value = "Input parameters") @RequestBody(required = true) String data) {
		
		final String userId = utils.getUserId();
		final boolean authorized = notebookService.hasUserPermissionCreateNotebook(userId);

		if (authorized) {
			try {
				final Notebook note = notebookService.importNotebook(notebookName, data, userId, overwrite, importAuthorizations);
				return new ResponseEntity<>(importNotebookResponse(note).toString(), HttpStatus.OK);
			} catch(NotebookServiceException e) {
				if (e.getError().name().equals(Error.DUPLICATE_NOTEBOOK_NAME.toString())) {
					return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage(), MSG_DUPLICATE_NOTEBOOK_IDENT), HttpStatus.CONFLICT);
				} else if (e.getError().name().equals(Error.PERMISSION_DENIED.toString())) {
					return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage(), String.format(MSG_USER_NOT_ALLOWED, userId)), HttpStatus.UNAUTHORIZED);
				}
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage(), MSG_BAD_REQUEST), HttpStatus.BAD_REQUEST);
			} catch (final Exception e) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage(), MSG_INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

	}
	
	@ApiOperation(value = "Import jupyter notebook")
	@PostMapping(value = "/import/jupyter/{notebookName}")
	public ResponseEntity<?> importJupyterNotebook(
			@ApiParam(value = "Notebook Zeppelin Name", required = true) @PathVariable("notebookName") String notebookName,
			@ApiParam(value = "Overwrite notebook if exists") @RequestParam(required = false, defaultValue = "false") boolean overwrite,
			@ApiParam(value = "Import authorizations if exist") @RequestParam(required = false, defaultValue = "false") boolean importAuthorizations,
			@ApiParam(value = "Input parameters") @RequestBody(required = true) String data) {

		final String userId = utils.getUserId();
		final boolean authorized = notebookService.hasUserPermissionCreateNotebook(userId);

		if (authorized) {
			try {
				final Notebook note = notebookService.importNotebookFromJupyter(notebookName, data, userId);
				return new ResponseEntity<>(importNotebookResponse(note).toString(), HttpStatus.OK);
			} catch (final Exception e) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage(), MSG_INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

	}

	@ApiOperation(value = "Import several notebooks")
	@PostMapping(value = "/import/")
	public ResponseEntity<?> importSeveralNotebooks(
			@ApiParam(value = "Overwrite notebook if exists") @RequestParam(required = false, defaultValue = "false") boolean overwrite,
			@ApiParam(value = "Import authorizations if exist") @RequestParam(required = false, defaultValue = "false") boolean importAuthorizations,
			@ApiParam(value = "Input parameters") @RequestBody(required = true) String data) {

		final String userId = utils.getUserId();
		final boolean authorized = notebookService.hasUserPermissionCreateNotebook(userId);

		if (authorized) {
			JsonArray importedNotebooksResponse = new JsonArray();
			try {
				JsonElement jelement = new JsonParser().parse(data);
				final JsonArray inputJsonArray = jelement.getAsJsonArray();
				Iterator<JsonElement> jsonIter = inputJsonArray.iterator();
				int counter = 0;
				while (jsonIter.hasNext()) {
					final JsonObject currentNotebook = jsonIter.next().getAsJsonObject();
					final String notebookName = currentNotebook.get("name").getAsString();
					final String notebookId = currentNotebook.get("id").getAsString();
					JsonObject noteResponse = tryImportNotebook(overwrite, importAuthorizations, userId,
							currentNotebook, notebookName, notebookId);
					importedNotebooksResponse.add(noteResponse);
					counter++;
				}

				if (counter == 0) {
					return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
				}
				return new ResponseEntity<>(importedNotebooksResponse.toString(), HttpStatus.OK);
			} catch (final Exception e) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage(), MSG_INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

	}

	private JsonObject tryImportNotebook(boolean overwrite, boolean importAuthorizations, final String userId,
			final JsonObject currentNotebook, final String notebookName, final String notebookId) {
		JsonObject noteResponse;
		try {
			final Notebook note = notebookService.importNotebook(notebookName, currentNotebook.toString(),
					userId, overwrite, importAuthorizations);
			noteResponse = importNotebookResponse(note);
		} catch (final Exception e) {
			noteResponse = importNotebookResponse(notebookId, notebookName, "Error importing notebook: " + e.getMessage());
		}
		return noteResponse;
	}
	
	@ApiOperation(value = "Restart global interpreter")
	@GetMapping(value = "/restart/{interpreterName}")
	public ResponseEntity<?> restartInterpreter(
			@ApiParam(value = "Interpreter Name", required = true) @PathVariable(name = "interpreterName") String interpreterName) {
		
		final String userId = utils.getUserId();
		final User user = userRepository.findByUserId(userId);

		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			try {
				return notebookService.restartInterpreter(interpreterName, "");
			} catch (final Exception e) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage(), MSG_INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
				
	}
	
	@ApiOperation(value = "Restart interpreter of a notebook by identification or id")
	@GetMapping(value = "/restart/{interpreterName}/notebook/{notebookId}")
	public ResponseEntity<?> restartInterpreter(
			@ApiParam(value = "Interpreter Name", required = true) @PathVariable(name = "interpreterName") String interpreterName,
			@ApiParam(value = "Notebook Id", required = true) @PathVariable(name = "notebookId") String notebookId) {
		
		final String userId = utils.getUserId();
		Notebook nt = notebookService.getNotebook(notebookId);
		final User user = userRepository.findByUserId(userId);
		final boolean authorized = notebookService.hasUserPermissionForNotebook(nt.getIdzep(), userId);

		if (authorized) {
			try {
				JSONObject body = new JSONObject();
				body.put("noteId", nt.getIdzep());
				
				return notebookService.restartInterpreter(interpreterName, body.toString(), user);
			} catch (final Exception e) {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);			
	}
	
	@ApiOperation(value = "Restart all interpreters of a notebook by identification or id")
	@GetMapping(value = "/restart/notebook/{notebookId}")
	public ResponseEntity<?> restartInterpreters(
			@ApiParam(value = "Notebook Id", required = true) @PathVariable(name = "notebookId") String notebookId) {
		
		final String userId = utils.getUserId();
		Notebook nt = notebookService.getNotebook(notebookId);
		final User user = userRepository.findByUserId(userId);
		final boolean authorized = notebookService.hasUserPermissionForNotebook(nt.getIdzep(), userId);

		if (authorized) {
			try {
				JSONObject body = new JSONObject();
				body.put("noteId", nt.getIdzep());
				
				return notebookService.restartAllInterpretersNotebook(nt.getIdzep(), body.toString(), user);
			} catch (final Exception e) {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);			
	}
	
	@ApiOperation(value = "Get users access to notebook by identification or id")
	@GetMapping(value = "/{notebookId}/authorizations/")
	public ResponseEntity<?> getNotebookAuthorizations(
			@PathVariable(name = "notebookId") String notebookId) {
	
		try {
			final Notebook notebook = notebookService.getNotebook(notebookId);
					
			if (notebook == null) {
				return new ResponseEntity<>(String.format(MSG_NOTEBOOK_NOT_FOUND, notebookId), HttpStatus.BAD_REQUEST);
			}

			final String userId = utils.getUserId();
			final User user = userRepository.findByUserId(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format(MSG_USER_NOT_FOUND, userId), HttpStatus.BAD_REQUEST);
			}
			
			if (!(user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString()) || notebookService.isUserOwnerOfNotebook(user, notebook))) {
				return new ResponseEntity<>(String.format(MSG_USER_NOT_ALLOWED, userId), HttpStatus.UNAUTHORIZED);
			}
			
			JSONArray responseInfo = new JSONArray();
						
			List<NotebookUserAccess> userAccesses = notebookService.getUserAccess(notebook.getIdzep());
			
			Iterator<NotebookUserAccess> i1 = userAccesses.iterator();
			while (i1.hasNext()) {
				NotebookUserAccessSimplified currentUserAccess = new NotebookUserAccessSimplified(i1.next());
				JSONObject jsonAccess = new JSONObject();
				jsonAccess.put("userId", currentUserAccess.getUserId());
				jsonAccess.put("accessType", currentUserAccess.getAccessType());
				responseInfo.put(jsonAccess);
			}
				
			return new ResponseEntity<>(responseInfo.toString(), HttpStatus.OK);

		} catch (Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	
	@ApiOperation(value = "Create users access to notebook by identification or id")
	@PostMapping(value = "/{notebookId}/authorizations/")
	public ResponseEntity<?> createNotebookAuthorizations(
			@PathVariable(name = "notebookId") String notebookId, 
			@Valid @RequestBody List<NotebookUserAccessSimplified> notebookAccess, Errors errors) {
	
		if (errors.hasErrors())
			return ErrorValidationResponse.generateValidationErrorResponse(errors);
		
		try {
			final Notebook notebook = notebookService.getNotebook(notebookId);
					
			if (notebook == null) {
				return new ResponseEntity<>(String.format(MSG_NOTEBOOK_NOT_FOUND, notebookId), HttpStatus.BAD_REQUEST);
			}

			final String userId = utils.getUserId();
			final User user = userRepository.findByUserId(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format(MSG_USER_NOT_FOUND, userId), HttpStatus.BAD_REQUEST);
			}
			
			if (!(user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString()) || notebookService.isUserOwnerOfNotebook(user, notebook))) {
				return new ResponseEntity<>(String.format(MSG_USER_NOT_ALLOWED, userId), HttpStatus.UNAUTHORIZED);
			}
			
			JSONObject responseInfo = new JSONObject();
			List<String> userIds = new ArrayList<>();
			List<String> accessTypes = new ArrayList<>();
			for (NotebookUserAccessSimplified notebookUserAcc: notebookAccess) {
				userIds.add(notebookUserAcc.getUserId());
				accessTypes.add(notebookUserAcc.getAccessType().toUpperCase());
			}
			
			List<String> created = notebookService.createUserAccess(notebook.getIdzep(), userIds, accessTypes);
			
			Iterator<String> i1 = userIds.iterator();
			Iterator<String> i2 = created.iterator();
			while (i1.hasNext() && i2.hasNext()) {
				responseInfo.put(i1.next(), i2.next());
			}
				
			return new ResponseEntity<>(responseInfo.toString(), HttpStatus.OK);

		} catch (Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	
	@ApiOperation(value = "Delete users access to notebook by identification or id")
	@DeleteMapping(value = "/{notebookId}/authorizations/")
	public ResponseEntity<?> deleteNotebookAuthorizations(
			@PathVariable(name = "notebookId") String notebookId, 
			@Valid @RequestBody List<NotebookUserAccessSimplified> notebookAccess, Errors errors) {
	
		if (errors.hasErrors())
			return ErrorValidationResponse.generateValidationErrorResponse(errors);
		
		try {
			final Notebook notebook = notebookService.getNotebook(notebookId);
					
			if (notebook == null) {
				return new ResponseEntity<>(String.format(MSG_NOTEBOOK_NOT_FOUND, notebookId), HttpStatus.BAD_REQUEST);
			}

			final String userId = utils.getUserId();
			final User user = userRepository.findByUserId(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format(MSG_USER_NOT_FOUND, userId), HttpStatus.BAD_REQUEST);
			}
			
			if (!(user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString()) || notebookService.isUserOwnerOfNotebook(user, notebook))) {
				return new ResponseEntity<>(String.format(MSG_USER_NOT_ALLOWED, userId), HttpStatus.UNAUTHORIZED);
			}
			
			JSONObject responseInfo = new JSONObject();
			List<String> userIds = new ArrayList<>();
			List<String> accessTypes = new ArrayList<>();
			for (NotebookUserAccessSimplified notebookUserAcc: notebookAccess) {
				userIds.add(notebookUserAcc.getUserId());
				accessTypes.add(notebookUserAcc.getAccessType().toUpperCase());
			}
			
			List<String> deleted = notebookService.deleteUserAccess(notebookId, userIds, accessTypes);
			
			Iterator<String> i1 = userIds.iterator();
			Iterator<String> i2 = deleted.iterator();
			while (i1.hasNext() && i2.hasNext()) {
				responseInfo.put(i1.next(), i2.next());
			}
				
			return new ResponseEntity<>(responseInfo.toString(), HttpStatus.OK);

		} catch (Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

}