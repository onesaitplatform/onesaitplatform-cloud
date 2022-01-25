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
package com.minsait.onesait.platform.config.services.notebook;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.expression.ParseException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.minsait.onesait.platform.config.dto.NotebookForList;
import com.minsait.onesait.platform.config.dto.OPResourceDTO;
import com.minsait.onesait.platform.config.model.Notebook;
import com.minsait.onesait.platform.config.model.NotebookUserAccess;
import com.minsait.onesait.platform.config.model.NotebookUserAccessType;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.NotebookRepository;
import com.minsait.onesait.platform.config.repository.NotebookUserAccessRepository;
import com.minsait.onesait.platform.config.repository.NotebookUserAccessTypeRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.exceptions.NotebookServiceException;
import com.minsait.onesait.platform.config.services.exceptions.NotebookServiceException.Error;
import com.minsait.onesait.platform.config.services.exceptions.OPResourceServiceException;
import com.minsait.onesait.platform.config.services.generic.security.SecurityService;
import com.minsait.onesait.platform.config.services.notebook.configuration.NotebookServiceConfiguration;
import com.minsait.onesait.platform.config.services.notebook.conversion.JupyterConverterImpl;
import com.minsait.onesait.platform.config.services.notebook.dto.NotebookOspInfoDTO;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.user.UserService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotebookServiceImpl implements NotebookService {

	@Autowired
	private NotebookServiceConfiguration configuration;

	@Autowired
	private NotebookRepository notebookRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private OPResourceService resourceService;

	@Autowired
	private UserService userService;

	@Autowired
	private NotebookUserAccessRepository notebookUserAccessRepository;

	@Autowired
	private NotebookUserAccessTypeRepository notebookUserAccessTypeRepository;

	@Autowired
	private JupyterConverterImpl jupyterConverter;

	@Autowired
	private SecurityService securityService;

	private static final String WITHNAME_STR = " with name: ";
	private static final String URI_POST_ERROR = "The URI of the endpoint is invalid in creation POST";
	private static final String URI_POST2_ERROR = "The URI of the endpoint is invalid in creation POST: ";
	private static final String POST_ERROR = "Exception in POST in creation POST";
	private static final String POST2_ERROR = "Exception in POST in creation POST: ";
	private static final String POST_EXECUTING_ERROR = "Exception executing creation POST, status code: ";
	private static final String POST_EXECUTING_DELETE_ERROR = "Exception executing delete notebook, status code: ";
	private static final String NAME_STR = "{'name': '";
	private static final String API_NOTEBOOK_STR = "/api/notebook/";
	private static final String DUPLICATE_NOTEBOOK_NAME = "Error duplicate notebook name";
	private static final String INVALID_FORMAT_NOTEBOOK = "Invalid format data in notebook";
	private static final String PERMISSION_DENIED = "PERMISSION DENIED";
	private static final String API_INTERPRETER_SETTING_STR = "/api/interpreter/setting/";
	private static final String API_INTERPRETER_RESTART_STR = "/api/interpreter/setting/restart/";
	private static final String API_IMPORT_STR = "/api/notebook/import";
	private static final String API_EXPORT_STR = "/api/notebook/export/";
	private static final String API_JOB_STR = "/api/notebook/job/";
	private static final String API_RUN_STR = "/api/notebook/run/";
	private static final String ERROR = "ERROR: ";
	private static final String ERROR_USERACCESS_PROPR = "Not possible to give access to proprietary user of notebook";
	private static final String ERROR_USERACCESS_ROL = "Not possible to give access to user: role not allowed";
	private static final String ERROR_NOTEBOOK_NOT_FOUND = "Notebook not found with id %s";
	private static final String ERROR_ACCESS_EXISTS = "Invalid operation, user access already exists";
	private static final String ERROR_ACCESS_NOT_EXISTS = "Invalid operation, user access not exists";
	private static final String ERROR_ACCESS_TYPE_NOT_FOUND = "Invalid input data: Access type not found";
	private static final String ERROR_USER_NOT_FOUND = "Invalid input data: User not found";
	private static final String ERROR_EXPORTING_NOTEBOOK = "Exception executing export notebook, permission denied";
	private static final String OK = "OK";
	private static final String WARNING_NOT_POSSIBLE_EXTRACT_OSP_INFO = "WARN: Not possible to extract ospInfo from notebook";
	private static final String ERROR_USER_CANNOT_OVERWRITE_NOTEBOOK = "User %s not authorized to overwrite notebook %s";
	private static final String ERROR_NOTEBOOK_NOT_FOUND_ZEPPELIN = "Notebook not found in zeppelin";
	private static final String HAS_NOT_PERMISSION_TO_RESTART_INTERPRETER = " has not permission to restart interpreter ";
	private static final String USER2 = "User ";
	private static final String UNABLE_TO_GET_INTERPRETER = "Unable to get interpreter ";
	private static final String UNABLE_TO_GET_DEFAULT_INTERPRETER = "Unable to get default interpreter";

	private String encryptRestUserpass() {
		String key = configuration.getRestUsername() + ":" + configuration.getRestPass();
		final String encryptedKey = new String(Base64.encode(key.getBytes()), StandardCharsets.UTF_8);
		key = "Basic " + encryptedKey;
		return key;
	}

	private Notebook sendZeppelinCreatePost(String path, String body, String name, User user) {
		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String idzep;
		ResponseEntity<String> responseEntity;

		log.info("Creating notebook for user: " + user.getUserId() + WITHNAME_STR + name);

		try {
			responseEntity = sendHttp(path, HttpMethod.POST, body, headers);
		} catch (final URISyntaxException e) {
			log.error(URI_POST_ERROR);
			throw new NotebookServiceException(URI_POST2_ERROR + e);
		} catch (final IOException e) {
			log.error(POST_ERROR);
			throw new NotebookServiceException(POST2_ERROR, e);
		}

		final int statusCode = responseEntity.getStatusCodeValue();
		/* 200 zeppelin 8, 201 zeppelin 7 */
		if (statusCode / 100 != 2) {
			log.error(POST_EXECUTING_ERROR + statusCode);
			throw new NotebookServiceException(POST_EXECUTING_ERROR + statusCode);
		}

		try {
			final JSONObject createResponseObj = new JSONObject(responseEntity.getBody());
			idzep = createResponseObj.getString("body");
		} catch (final JSONException e) {
			log.error("Exception parsing answer in create post");
			throw new NotebookServiceException("Exception parsing answer in create post: ", e);
		}

		return saveDBNotebook(name, idzep, user);
	}

	private String sendZeppelinCreatePostWithoutDBC(String path, String body, String name, User user) {
		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String idzep;
		ResponseEntity<String> responseEntity;

		log.info("Creating notebook for user: " + user.getUserId() + WITHNAME_STR + name);

		try {
			responseEntity = sendHttp(path, HttpMethod.POST, body, headers);
		} catch (final URISyntaxException e) {
			log.error(URI_POST_ERROR);
			throw new NotebookServiceException(URI_POST2_ERROR + e);
		} catch (final IOException e) {
			log.error(POST_ERROR);
			throw new NotebookServiceException(POST2_ERROR, e);
		}

		final int statusCode = responseEntity.getStatusCodeValue();
		/* 200 zeppelin 8, 201 zeppelin 7 */
		if (statusCode / 100 != 2) {
			log.error(POST_EXECUTING_ERROR + statusCode);
			throw new NotebookServiceException(POST_EXECUTING_ERROR + statusCode);
		}

		try {
			final JSONObject createResponseObj = new JSONObject(responseEntity.getBody());
			idzep = createResponseObj.getString("body");
		} catch (final JSONException e) {
			log.error("Exception parsing answer in create post");
			throw new NotebookServiceException("Exception parsing answer in create post: ", e);
		}

		return idzep;
	}

	@Override
	public Notebook saveDBNotebook(String name, String idzep, User user) {
		final Notebook nt = new Notebook();
		nt.setIdentification(name);
		nt.setIdzep(idzep);
		nt.setUser(user);
		notebookRepository.save(nt);
		return nt;
	}

	@Override
	public Notebook createEmptyNotebook(String name, String userId) {

		final User user = userRepository.findByUserId(userId);
		if (!existNotebookIdentification(name)) {
			return sendZeppelinCreatePost("/api/notebook", NAME_STR + name + "'}", name, user);
		} else {
			log.error(DUPLICATE_NOTEBOOK_NAME);
			throw new NotebookServiceException(Error.DUPLICATE_NOTEBOOK_NAME);
		}
	}

	@Override
	public Notebook importNotebook(String name, String data, String userId) {
		return importNotebook(name, data, userId, false, false);
	}

	@Override
	public Notebook importNotebook(String name, String data, String userId, boolean overwrite,
			boolean importAuthorizations) {
		Notebook nt = null;
		final User user = userRepository.findByUserId(userId);
		final boolean isUserAdmin = userService.isUserAdministrator(user);
		User importingUser = user;
		User notebookOwner = null;
		// if admin: import with user owner
		final NotebookOspInfoDTO ospNotebookInfo = getOspInfoFromNotebook(data);
		if (importAuthorizations && ospNotebookInfo != null) {
			notebookOwner = userRepository.findByUserId(ospNotebookInfo.getOwner());
			if (notebookOwner != null && isUserAdmin) {
				importingUser = notebookOwner;
			}
		}

		if (existNotebookIdentification(name) && overwrite) {
			final Notebook ntExists = getNotebook(name);
			if (ntExists.getUser().equals(user) || isUserAdmin) {
				log.info("Removing notebook %s by overwrite in import", ntExists.getIdentification());
				nt = updateNotebook(ntExists.getIdentification(), userId, data, importAuthorizations);
			} else {
				log.error(ERROR_USER_CANNOT_OVERWRITE_NOTEBOOK, user.getUserId(), ntExists.getIdentification());
				throw new NotebookServiceException(Error.PERMISSION_DENIED, String
						.format(ERROR_USER_CANNOT_OVERWRITE_NOTEBOOK, user.getUserId(), ntExists.getIdentification()));
			}
		} else if (!existNotebookIdentification(name)) {
			nt = createNotebook(name, data, importingUser.getUserId(), importAuthorizations);
		} else {
			log.error(DUPLICATE_NOTEBOOK_NAME);
			throw new NotebookServiceException(Error.DUPLICATE_NOTEBOOK_NAME, DUPLICATE_NOTEBOOK_NAME);
		}
		return nt;
	}

	@Override
	public Notebook importNotebookData(String name, String data, String userId, boolean overwrite,
			boolean importAuthorizations) {
		Notebook nt = null;
		User user = userRepository.findByUserId(userId);
		boolean isUserAdmin = userService.isUserAdministrator(user);
		User importingUser = user;
		User notebookOwner = null;
		// if admin: import with user owner
		NotebookOspInfoDTO ospNotebookInfo = getOspInfoFromNotebook(data);
		if (importAuthorizations && ospNotebookInfo != null) {
			notebookOwner = userRepository.findByUserId(ospNotebookInfo.getOwner());
			if (notebookOwner != null && isUserAdmin) {
				importingUser = notebookOwner;
			}
		}

		Notebook ntExists = getNotebook(name);

		log.info("Removing notebook %s by overwrite in import", ntExists.getIdentification());
		nt = updateNotebook(ntExists.getIdentification(), userId, data, importAuthorizations);

		return nt;
	}

	private Notebook updateNotebook(String notebookId, String userId, String data, boolean importAuthorizations) {
		Notebook nt = null;
		final Notebook notebookOld = getNotebook(notebookId, userId);
		final String idzepOld = notebookOld.getIdzep();
		final String nameOld = notebookOld.getIdentification();
		final User user = userRepository.findByUserId(userId);
		// create new notebook in zeppelin and update idzep in config db
		final String idZ = sendZeppelinCreatePostWithoutDBC(API_IMPORT_STR, data, nameOld, user);
		notebookOld.setIdzep(idZ);
		nt = notebookRepository.save(notebookOld);
		// update user accesses
		final NotebookOspInfoDTO ospNotebookInfo = getOspInfoFromNotebook(data);
		if (importAuthorizations && ospNotebookInfo != null) {
			final JsonArray userAccesses = ospNotebookInfo.getAuthorizations();
			final List<NotebookUserAccess> notebookUSerAcc = notebookUserAccessRepository.findByNotebook(notebookOld);
			notebookUserAccessRepository.deleteInBatch(notebookUSerAcc);
			createUserAccess(nt.getIdentification(), userAccesses);
		}
		// remove old notebook in zeppelin
		final ResponseEntity<String> responseEntity = null;
		try {
			sendHttp(API_NOTEBOOK_STR + idzepOld, HttpMethod.DELETE, "");
		} catch (URISyntaxException | IOException e) {
			log.warn("Not possible to delete deprecated notebook " + nameOld + "(" + idzepOld
					+ ") after update notebook: " + responseEntity, e);
		}
		return nt;

	}

	private Notebook createNotebook(String name, String data, String userId, boolean importAuthorizations) {
		final User user = userRepository.findByUserId(userId);
		final Notebook nt = sendZeppelinCreatePost(API_IMPORT_STR, data, name, user);

		// create user accesses
		final NotebookOspInfoDTO ospNotebookInfo = getOspInfoFromNotebook(data);
		if (importAuthorizations && ospNotebookInfo != null) {
			final JsonArray userAccesses = ospNotebookInfo.getAuthorizations();
			createUserAccess(name, userAccesses);
		}

		return nt;
	}

	public List<String> createUserAccess(String name, JsonArray userAccesses) {
		List<String> created = null;
		if (userAccesses != null && userAccesses.size() > 0) {
			final List<String> userIds = new ArrayList<>();
			final List<String> accessTypes = new ArrayList<>();
			for (final JsonElement notebookUserAcc : userAccesses) {
				userIds.add(notebookUserAcc.getAsJsonObject().get("userId").getAsString());
				accessTypes.add(notebookUserAcc.getAsJsonObject().get("accessType").getAsString().toUpperCase());
			}
			created = createUserAccess(name, userIds, accessTypes);
			log.info("Created user accesses %s for notebook %s", created.toString(), name);
		}
		return created;
	}

	@Override
	public Notebook importNotebookFromJupyter(String name, String data, String userId) {
		return importNotebookFromJupyter(name, data, userId, false, false);
	}

	@Override
	public Notebook importNotebookFromJupyter(String name, String data, String userId, boolean overwrite,
			boolean importAuthorizations) {
		String formatedData;
		try {
			formatedData = jupyterConverter.jupyterNotebookToZeppelinNotebook(data, name);
		} catch (final Exception e) {
			log.error(INVALID_FORMAT_NOTEBOOK);
			throw new NotebookServiceException(Error.INVALID_FORMAT_NOTEBOOK, e.getMessage());
		}
		return importNotebook(name, formatedData, userId, overwrite, importAuthorizations);

	}

	@Override
	public Notebook cloneNotebook(String name, String idzep, String userId) {

		final Notebook nt = notebookRepository.findByIdzep(idzep);
		final User user = userRepository.findByUserId(userId);
		if (hasUserPermissionInNotebook(nt, userId)) {
			if (!existNotebookIdentification(name)) {
				return sendZeppelinCreatePost(API_NOTEBOOK_STR + idzep, NAME_STR + name + "'}", name, user);
			} else {
				log.error(DUPLICATE_NOTEBOOK_NAME);
				throw new NotebookServiceException(Error.DUPLICATE_NOTEBOOK_NAME, DUPLICATE_NOTEBOOK_NAME);
			}
		} else {
			log.error(PERMISSION_DENIED);
			throw new NotebookServiceException(Error.PERMISSION_DENIED, PERMISSION_DENIED);
		}
	}

	@Override
	public void renameNotebook(String name, String idzep, String userId) {
		log.info("Renaming notebook " + idzep + " for user: " + userId + " to " + name);
		final Notebook nt = notebookRepository.findByIdzep(idzep);
		if (hasUserPermissionForNotebook(nt.getIdzep(), userId)) {
			if (!existNotebookIdentification(name)) {
				rename(name, nt);
			} else {
				log.error(DUPLICATE_NOTEBOOK_NAME);
				throw new NotebookServiceException(Error.DUPLICATE_NOTEBOOK_NAME, DUPLICATE_NOTEBOOK_NAME);
			}
		} else {
			log.error(PERMISSION_DENIED);
			throw new NotebookServiceException(Error.PERMISSION_DENIED, PERMISSION_DENIED);
		}
	}

	@Override
	public String cloneNotebookOnlyZeppelin(String name, String idzep, String userId) {
		final Notebook nt = notebookRepository.findByIdzep(idzep);
		final User user = userRepository.findByUserId(userId);
		if (hasUserPermissionInNotebook(nt, user)) {
			return sendZeppelinCreatePostWithoutDBC(API_NOTEBOOK_STR + idzep, NAME_STR + name + "'}", name, user);
		} else {
			return null;
		}
	}

	@Override
	public HttpHeaders exportHeaders(String notebookNameFile) {
		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		headers.set("Content-Disposition", "attachment; filename=\"" + notebookNameFile + ".json\"");
		return headers;
	}

	@Override
	public JSONObject exportNotebook(String id, String user) {
		final Notebook nt = getNotebook(id);
		final User userObj = userRepository.findByUserId(user);
		ResponseEntity<String> responseEntity;
		JSONObject notebookJSONObject;

		if (hasUserPermissionInNotebook(nt, user)) {
			try {
				responseEntity = sendHttp(API_EXPORT_STR + nt.getIdzep(), HttpMethod.GET, "");

				if (responseEntity == null) {
					log.error(ERROR_NOTEBOOK_NOT_FOUND_ZEPPELIN, nt.getIdzep());
					throw new NotebookServiceException(Error.BAD_RESPONSE_FROM_NOTEBOOK_SERVICE,
							ERROR_NOTEBOOK_NOT_FOUND_ZEPPELIN);
				}

			} catch (final URISyntaxException e) {
				log.error(URI_POST_ERROR);
				throw new NotebookServiceException(URI_POST2_ERROR + e);
			} catch (final IOException e) {
				log.error(POST_ERROR);
				throw new NotebookServiceException(POST2_ERROR, e);
			}

			final int statusCode = responseEntity.getStatusCodeValue();

			if (statusCode != 200) {
				log.error("Exception executing export notebook, status code: " + statusCode);
				throw new NotebookServiceException(Error.BAD_RESPONSE_FROM_NOTEBOOK_SERVICE,
						"Exception executing export notebook, status code: " + statusCode);
			}

			try {
				final JSONObject responseJSONObject = new JSONObject(responseEntity.getBody());
				notebookJSONObject = new JSONObject(responseJSONObject.getString("body"));
				if (isUserOwnerOfNotebook(userObj, nt) || userObj.isAdmin()) {
					setOspInfoInNotebook(id, notebookJSONObject);
				}
			} catch (final JSONException e) {
				log.error("Exception parsing answer in download notebook");
				throw new NotebookServiceException(Error.BAD_RESPONSE_FROM_NOTEBOOK_SERVICE,
						"Exception parsing answer in download notebook: ", e);
			}
			return notebookJSONObject;

		} else {
			log.error(ERROR_EXPORTING_NOTEBOOK);
			throw new NotebookServiceException(Error.PERMISSION_DENIED, ERROR_EXPORTING_NOTEBOOK);
		}
	}

	@Override
	public void removeNotebookByIdZep(String idZep, String user) {

		final Notebook nt = notebookRepository.findByIdzep(idZep);
		if (null != nt) {
			removeNotebook(nt.getIdentification(), user);
		} else {
			log.error("Error delete notebook, not exist");
			throw new NotebookServiceException("Error delete notebook, not exist");
		}
	}

	@Override
	public void removeNotebookOnlyZeppelin(String idZep, String user) {
		ResponseEntity<String> responseEntity;
		try {
			responseEntity = sendHttp(API_NOTEBOOK_STR + idZep, HttpMethod.DELETE, "");
		} catch (final URISyntaxException e) {
			log.error("The URI of the endpoint is invalid in delete notebook");
			throw new NotebookServiceException("The URI of the endpoint is invalid in delete notebook: " + e);
		} catch (final IOException e) {
			log.error(POST_ERROR);
			throw new NotebookServiceException("Exception in POST in delete notebook: ", e);
		}

		final int statusCode = responseEntity.getStatusCodeValue();

		if (statusCode != 200) {
			log.error(POST_EXECUTING_DELETE_ERROR + statusCode);
			throw new NotebookServiceException(POST_EXECUTING_DELETE_ERROR + statusCode);
		}
	}

	@Override
	public void removeNotebook(String id, String user) {
		ResponseEntity<String> responseEntity;
		final Notebook nt = notebookRepository.findByIdentification(id);
		final String name = nt.getIdentification();
		if (resourceService.isResourceSharedInAnyProject(nt)) {
			log.error("This Notebook is shared within a Project, revoke access from project prior to deleting");
			throw new OPResourceServiceException(
					"This Notebook is shared within a Project, revoke access from project prior to deleting");
		}

		log.info("Delete notebook for user: " + user + WITHNAME_STR + name);

		if (hasUserPermissionForNotebook(nt.getIdzep(), user)) {

			try {
				responseEntity = sendHttp(API_NOTEBOOK_STR + nt.getIdzep(), HttpMethod.DELETE, "");
			} catch (final URISyntaxException e) {
				log.error("The URI of the endpoint is invalid in delete notebook");
				throw new NotebookServiceException("The URI of the endpoint is invalid in delete notebook: " + e);
			} catch (final IOException e) {
				log.error(POST_ERROR);
				throw new NotebookServiceException("Exception in POST in delete notebook: ", e);
			}

			final int statusCode = responseEntity.getStatusCodeValue();

			if (statusCode != 200) {
				log.error(POST_EXECUTING_DELETE_ERROR + statusCode);
				throw new NotebookServiceException(POST_EXECUTING_DELETE_ERROR + statusCode);
			}

			for (final NotebookUserAccess nua : notebookUserAccessRepository.findByNotebook(nt)) {
				notebookUserAccessRepository.delete(nua);
			}

			notebookRepository.delete(nt);
			log.info("Notebook for user: " + user + WITHNAME_STR + name + ", successfully deleted");
		} else {
			log.error("Exception executing delete notebook, permission denied");
			throw new NotebookServiceException(Error.PERMISSION_DENIED, "Error delete notebook, permission denied");
		}
	}

	@Override
	public String loginOrGetWSToken() {
		return loginOrGetWSTokenWithUserPass(configuration.getZeppelinShiroUsername(),
				configuration.getZeppelinShiroPass());
	}

	@Override
	public String loginOrGetWSTokenAdmin() {
		return loginOrGetWSTokenWithUserPass(configuration.getZeppelinShiroAdminUsername(),
				configuration.getZeppelinShiroAdminPass());
	}

	@Override
	public String loginOrGetWSTokenByBearer(String user, String bearertoken) {
		return loginOrGetWSTokenWithUserPass(user, bearertoken);
	}

	private String loginOrGetWSTokenWithUserPass(String username, String password) {
		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		ResponseEntity<String> responseEntity;

		try {
			responseEntity = sendHttp("api/login", HttpMethod.POST, "userName=" + username + "&password=" + password,
					headers);
		} catch (final URISyntaxException e) {
			log.error("The URI of the endpoint is invalid in authentication POST");
			throw new NotebookServiceException("The URI of the endpoint is invalid in authentication POST: " + e);
		} catch (final IOException e) {
			log.error("Exception in POST in authentication POST");
			throw new NotebookServiceException("Exception in POST in authentication POST: ", e);
		}

		final int statusCode = responseEntity.getStatusCodeValue();

		if (statusCode != 200) {
			log.error("Exception executing authentication POST, status code: " + statusCode);
			throw new NotebookServiceException("Exception executing authentication POST, status code: " + statusCode);
		}

		return responseEntity.getBody();

	}

	@Override
	public ResponseEntity<String> sendHttp(HttpServletRequest requestServlet, HttpMethod httpMethod, String body)
			throws URISyntaxException, IOException {
		return sendHttp(requestServlet.getServletPath(), httpMethod, body);
	}

	@Override
	public ResponseEntity<String> sendHttp(String url, HttpMethod httpMethod, String body)
			throws URISyntaxException, IOException {
		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return sendHttp(url, httpMethod, body, headers);
	}

	@Override
	public ResponseEntity<String> sendHttp(String url, HttpMethod httpMethod, String body, HttpHeaders headers)
			throws URISyntaxException, IOException {
		final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
		headers.add("Authorization", encryptRestUserpass());
		final org.springframework.http.HttpEntity<String> request = new org.springframework.http.HttpEntity<>(body,
				headers);
		log.debug("Sending method " + httpMethod.toString() + " Notebook");
		ResponseEntity<String> response = new ResponseEntity<>(HttpStatus.ACCEPTED);
		try {
			response = restTemplate.exchange(
					new URI(configuration.getBaseURL() + url.substring(url.toLowerCase().indexOf("api"))), httpMethod,
					request, String.class);
		} catch (final Exception e) {
			log.error(e.getMessage());
		}
		log.debug("Execute method " + httpMethod.toString() + " '" + url + "' Notebook");
		final HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("Content-Type", response.getHeaders().getContentType().toString());
		return new ResponseEntity<>(response.getBody(), responseHeaders,
				HttpStatus.valueOf(response.getStatusCode().value()));
	}

	@Override
	public Notebook getNotebook(String identification, String userId) {
		final Notebook nt = notebookRepository.findByIdentification(identification);
		if (hasUserPermissionInNotebook(nt, userId)) {
			return nt;
		} else {
			return null;
		}
	}

	@Override
	public Notebook getNotebookByZepId(String notebookZepId, String userId) {
		final Notebook nt = notebookRepository.findByIdzep(notebookZepId);
		if (hasUserPermissionInNotebook(nt, userId)) {
			return nt;
		} else {
			return null;
		}
	}

	@Override
	public List<Notebook> getNotebooks(String userId) {
		final User user = userRepository.findByUserId(userId);
		if (!userService.isUserAdministrator(user)) {
			return notebookRepository.findByUserAndAccess(user);
		} else {
			return notebookRepository.findAllByOrderByIdentificationAsc();
		}
	}

	@Override
	public List<NotebookForList> getNotebooksAndByProjects(String userId) {
		final User user = userRepository.findByUserId(userId);
		List<NotebookForList> notebookForLists = notebookRepository.findAllNotebookList();
		if (user.isAdmin()) {
			return notebookForLists;
		} else {
			securityService.setSecurityToInputList(notebookForLists, user, "Notebook");
			return notebookForLists.stream().filter(n -> "EDIT".equals(n.getAccessType())).collect(Collectors.toList());
		}
	}

	@Override
	public boolean hasUserPermissionCreateNotebook(String userId) {
		final User user = userRepository.findByUserId(userId);
		return hasUserPermissionCreateNotebook(user);
	}

	private boolean hasUserPermissionCreateNotebook(User user) {
		return (userService.isUserAdministrator(user) || userService.isUserAnalytics(user));
	}

	@Override
	public boolean hasUserPermissionInNotebook(Notebook nt, String userId) {
		final User user = userRepository.findByUserId(userId);
		return hasUserPermissionInNotebook(nt, user);
	}

	private boolean hasUserPermissionInNotebook(Notebook nt, User user) {
		if (userService.isUserAdministrator(user) || nt.getUser().getUserId().equals(user.getUserId()) || nt.isPublic())
			return true;
		else { // TO-DO differentiate between access MANAGE/VIEW
			for (final NotebookUserAccess notebookUserAccess : notebookUserAccessRepository.findByNotebookAndUser(nt,
					user)) {
				if (notebookUserAccess.getNotebookUserAccessType().getId().equals("ACCESS-TYPE-1")) {
					return true;
				}
			}
			return resourceService.hasAccess(user.getUserId(), nt.getId(),
					ProjectResourceAccessParent.ResourceAccessType.VIEW);
		}
	}

	@Override
	public boolean isUserOwnerOfNotebook(String userId, Notebook notebook) {
		return notebook.getUser().getUserId().equals(userId);
	}

	@Override
	public boolean isUserOwnerOfNotebook(User user, Notebook notebook) {
		return notebook.getUser().getUserId().equals(user.getUserId());
	}

	@Override
	public void changePublic(Notebook nt) {
		if (nt != null) {
			nt.setPublic(!nt.isPublic());
			notebookRepository.save(nt);
		}

	}

	@Override
	public boolean hasUserPermissionForNotebook(String zeppelinId, String userId) {
		final Notebook nt = notebookRepository.findByIdzep(zeppelinId);
		if (nt != null)
			return this.hasUserPermissionInNotebook(nt, userId);
		return false;
	}

	@Override
	public NotebookOspInfoDTO getOspInfoFromNotebook(String notebookJson) {
		NotebookOspInfoDTO ospInfo = null;
		try {
			ospInfo = NotebookOspInfoDTO.fromJson(notebookJson);
		} catch (final ParseException e) {
			log.warn(WARNING_NOT_POSSIBLE_EXTRACT_OSP_INFO + ": " + e.getMessage());
		}
		return ospInfo;
	}

	@Override
	public NotebookOspInfoDTO getOspInfoFromDB(String notebookId) {
		NotebookOspInfoDTO ospInfo = null;
		try {
			final Notebook nt = getNotebook(notebookId);
			final String owner = nt.getUser().getUserId();
			final JsonArray authorizations = new JsonArray();
			final List<NotebookUserAccess> usersAccessType = notebookUserAccessRepository.findByNotebook(nt);
			for (final NotebookUserAccess uat : usersAccessType) {
				final String userAccessType = uat.getUser().getUserId();
				final String nameAccessType = uat.getNotebookUserAccessType().getName();
				final JsonObject jsonAccessType = new JsonObject();
				jsonAccessType.addProperty("userId", userAccessType);
				jsonAccessType.addProperty("accessType", nameAccessType);
				authorizations.add(jsonAccessType);
			}
			ospInfo = new NotebookOspInfoDTO();
			ospInfo.setOwner(owner);
			ospInfo.setAuthorizations(authorizations);
		} catch (final ParseException e) {
			log.warn(WARNING_NOT_POSSIBLE_EXTRACT_OSP_INFO + ": " + e.getMessage());
		}
		return ospInfo;
	}

	public boolean setOspInfoInNotebook(String notebookId, JSONObject notebookJSONObject) {
		boolean done = false;
		final NotebookOspInfoDTO ospInfo = getOspInfoFromDB(getNotebook(notebookId).getId());
		if (ospInfo != null) {
			final JSONObject ospJson = new JSONObject(ospInfo.toJson().toString());
			notebookJSONObject.put(NotebookOspInfoDTO.OSP_INFO, ospJson);
			done = true;
		}
		return done;
	}

	@Override
	public ResponseEntity<String> runParagraph(String zeppelinId, String paragraphId, String body)
			throws URISyntaxException, IOException {
		ResponseEntity<String> responseEntity;
		responseEntity = sendHttp(API_RUN_STR.concat(zeppelinId).concat("/").concat(paragraphId), HttpMethod.POST,
				body);
		if (responseEntity.getStatusCode() == HttpStatus.OK) {
			responseEntity = sendHttp(API_NOTEBOOK_STR.concat(zeppelinId).concat("/paragraph/").concat(paragraphId),
					HttpMethod.GET, "");
		}
		return responseEntity;
	}

	@Override
	public ResponseEntity<String> runAllParagraphs(String zeppelinId) throws URISyntaxException, IOException {
		return sendHttp(API_JOB_STR.concat(zeppelinId), HttpMethod.POST, "");
	}

	@Override
	public ResponseEntity<String> getParagraphResult(String zeppelinId, String paragraphId)
			throws URISyntaxException, IOException {
		return sendHttp(API_NOTEBOOK_STR.concat(zeppelinId).concat("/paragraph/").concat(paragraphId), HttpMethod.GET,
				"");
	}

	@Override
	public String getParagraphOutputMessage(String zeppelinId, String paragraphId)
			throws URISyntaxException, IOException {
		String rawData = null;
		final ResponseEntity<String> paragraphResult = getParagraphResult(zeppelinId, paragraphId);
		final JsonObject responseBody = new JsonParser().parse(paragraphResult.getBody()).getAsJsonObject();
		final JsonObject paragraphBody = responseBody.getAsJsonObject("body");
		final JsonObject results = paragraphBody.getAsJsonObject("results");
		final JsonArray msgs = results.getAsJsonArray("msg");
		final JsonObject data = msgs.get(0).getAsJsonObject();
		rawData = data.get("data").getAsString();
		return rawData;
	}

	@Override
	public ResponseEntity<String> getAllParagraphStatus(String zeppelinId) throws URISyntaxException, IOException {
		ResponseEntity<String> responseEntity;
		responseEntity = sendHttp(API_JOB_STR.concat(zeppelinId), HttpMethod.GET, "");

		return responseEntity;
	}

	@Override
	public List<NotebookUserAccess> getUserAccess(String notebookId) {
		final Notebook notebook = getNotebook(notebookId);
		return notebookUserAccessRepository.findByNotebook(notebook);
	}

	@Override
	public List<String> createUserAccess(String notebookId, List<String> userIds, List<String> accessTypes) {
		final List<String> created = new ArrayList<>();

		if (userIds.size() != accessTypes.size()) {
			return created;
		}

		for (int i = 0; i < userIds.size(); i++) {
			try {
				final String userId = userIds.get(i);
				final NotebookUserAccessType accessType = notebookUserAccessTypeRepository
						.findUserAccessTypeByName(accessTypes.get(i).toUpperCase());
				final String accessTypeId = accessType.getId();
				createUserAccess(notebookId, userId, accessTypeId);
				created.add(OK);
			} catch (final DataIntegrityViolationException e) {
				created.add(ERROR_ACCESS_EXISTS);
			} catch (final NotebookServiceException e) {
				created.add(e.getMessage());
			} catch (final NullPointerException e) {
				created.add(ERROR_ACCESS_NOT_EXISTS);
			} catch (final Exception e) {
				created.add(e.getMessage());
			}
		}

		return created;
	}

	@Override
	public NotebookUserAccess createUserAccess(String notebookId, String userId, String accessType) {
		NotebookUserAccess notebookUserAccess = null;

		if (!(notebookId.equals("")) && !(userId.equals("")) && !(accessType.equals(""))) {
			final User user = userRepository.findByUserId(userId);
			final Notebook notebook = getNotebook(notebookId);

			if (user == null) {
				log.error(ERROR_USER_NOT_FOUND);
				throw new NotebookServiceException(Error.NOT_FOUND, ERROR_USER_NOT_FOUND);
			}

			if (user.equals(notebook.getUser())) {
				log.error(ERROR_USERACCESS_PROPR);
				throw new NotebookServiceException(Error.PERMISSION_DENIED, ERROR_USERACCESS_PROPR);
			}

			if (!userService.isUserAnalytics(user)) {
				log.error(ERROR_USERACCESS_ROL);
				throw new NotebookServiceException(Error.PERMISSION_DENIED, ERROR_USERACCESS_ROL);
			}
			if (notebookUserAccessTypeRepository.findById(accessType).isPresent()) {
				final NotebookUserAccessType notebookUserAccessType = notebookUserAccessTypeRepository
						.findById(accessType).get();
				notebookUserAccess = new NotebookUserAccess();
				notebookUserAccess.setNotebook(notebook);
				notebookUserAccess.setUser(user);
				notebookUserAccess.setNotebookUserAccessType(notebookUserAccessType);
				notebookUserAccessRepository.save(notebookUserAccess);
			}

		}

		return notebookUserAccess;

	}

	@Override
	public Notebook getNotebook(String notebookId) {
		Notebook notebook = null;
		notebook = notebookRepository.findById(notebookId).orElse(null);
		// search notebook by ids -> retro-compatibility
		if (notebook == null) {
			notebook = notebookRepository.findByIdentification(notebookId);
		}
		if (notebook == null) {
			notebook = notebookRepository.findByIdzep(notebookId);
		}
		if (notebook == null) {
			log.error(ERROR_NOTEBOOK_NOT_FOUND, notebookId);
			throw new NotebookServiceException(String.format(ERROR_NOTEBOOK_NOT_FOUND, notebookId));
		}
		return notebook;
	}

	@Override
	public List<String> deleteUserAccess(String notebookId, List<String> userIds, List<String> accessTypes) {
		final List<String> deleted = new ArrayList<>();

		if (userIds.size() != accessTypes.size()) {
			return deleted;
		}

		final Notebook nt = getNotebook(notebookId);

		for (int i = 0; i < userIds.size(); i++) {
			try {
				final String userId = userIds.get(i);
				final User user = userRepository.findByUserId(userId);
				if (user == null) {
					log.error(ERROR_USER_NOT_FOUND);
					throw new NotebookServiceException(ERROR_USER_NOT_FOUND);
				}
				final NotebookUserAccessType accessType = notebookUserAccessTypeRepository
						.findUserAccessTypeByName(accessTypes.get(i).toUpperCase());
				if (accessType == null) {
					log.error(ERROR_ACCESS_NOT_EXISTS);
					throw new NotebookServiceException(ERROR_ACCESS_NOT_EXISTS);
				}
				final NotebookUserAccess notebookAccess = notebookUserAccessRepository
						.findByNotebookAndUserAndAccess(nt, user, accessType);
				deleteUserAccess(notebookAccess);
				deleted.add(OK);
			} catch (final NotebookServiceException e) {
				deleted.add(e.getMessage());
			} catch (final NullPointerException e) {
				deleted.add(ERROR_ACCESS_TYPE_NOT_FOUND);
			} catch (final Exception e) {
				deleted.add(e.getMessage());
			}
		}

		return deleted;
	}

	@Override
	public void deleteUserAccess(NotebookUserAccess notebookUserAcc) {
		deleteUserAccess(notebookUserAcc.getId());
	}

	@Override
	public void deleteUserAccess(String notebookUserAccessId) {
		notebookUserAccessRepository.deleteById(notebookUserAccessId);
	}

	@Override
	public String notebookNameByIdZep(String idzep, String userId) {

		final Notebook nt = notebookRepository.findByIdzep(idzep);
		if (hasUserPermissionInNotebook(nt, userId)) {
			return nt.getIdentification();
		} else {
			log.error(PERMISSION_DENIED);
			throw new NotebookServiceException(Error.PERMISSION_DENIED, PERMISSION_DENIED);
		}
	}

	private void rename(String name, Notebook nt) {

		/** NO RENAME IN ZEPPELIN 0.8.1, Change when rest api rename available **/
		/*
		 * ResponseEntity<String> rstr =
		 * sendHttp(API_NOTEBOOK_STR.concat(nt.getIdzep()).concat("/rename"),
		 * HttpMethod.PUT, NAME_STR + name + "'}"); if(rstr.getStatusCode() ==
		 * HttpStatus.OK) { nt.setIdentification(name); notebookRepository.save(nt);
		 * return true; }
		 */
		nt.setIdentification(name);
		notebookRepository.save(nt);
	}

	private boolean existNotebookIdentification(String identification) {
		final Notebook nt = notebookRepository.findByIdentification(identification);
		return nt != null;
	}

	private ResponseEntity<String> restartInterpreter(String interpreterName, String interpreterId, String body)
			throws URISyntaxException, IOException {

		ResponseEntity<String> responseEntity;

		responseEntity = sendHttp(API_INTERPRETER_RESTART_STR.concat(interpreterId), HttpMethod.PUT, body);

		if (responseEntity.getStatusCode() == HttpStatus.OK) {
			log.info("Interpreter " + interpreterName + " restarted successfully.");
			final JSONObject responseBody = new JSONObject().put("status", "OK");
			return new ResponseEntity<>(responseBody.toString(), responseEntity.getHeaders(), HttpStatus.OK);
		}

		return responseEntity;
	}

	@Override
	public ResponseEntity<String> restartInterpreter(String interpreterName, String body)
			throws URISyntaxException, IOException {

		final String interpreterId = getInterpreterId(interpreterName);
		return restartInterpreter(interpreterName, interpreterId, body);
	}

	@Override
	public ResponseEntity<String> restartInterpreter(String interpreterName, String body, User user)
			throws URISyntaxException, IOException {
		ResponseEntity<String> responseEntity;

		try {

			if (userService.isUserAdministrator(user)
					|| (userService.isUserAdministrator(user) && !isSharedInterpreterConfiguration(interpreterName))) {

				responseEntity = restartInterpreter(interpreterName, body);

			} else {
				log.error(USER2 + user.getUserId() + HAS_NOT_PERMISSION_TO_RESTART_INTERPRETER + interpreterName);
				final JSONObject responseBody = new JSONObject().put("error",
						USER2 + user.getUserId() + HAS_NOT_PERMISSION_TO_RESTART_INTERPRETER + interpreterName
								+ ". Check interpreter configuration.");
				final HttpHeaders responseHeaders = new HttpHeaders();
				responseHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
				return new ResponseEntity<>(responseBody.toString(), responseHeaders, HttpStatus.UNAUTHORIZED);
			}

		} catch (final NotebookServiceException e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return responseEntity;
	}

	private String getInterpreterId(String interpreterName) throws URISyntaxException, IOException {
		ResponseEntity<String> responseEntity;

		responseEntity = sendHttp(API_INTERPRETER_SETTING_STR, HttpMethod.GET, "");

		try {
			final JSONObject jsonResponse = new JSONObject(responseEntity.getBody());
			final JSONArray body = jsonResponse.getJSONArray("body");

			for (int i = 0; i < body.length(); i++) {
				final JSONObject object = body.getJSONObject(i);
				if (object != null && object.getString("name").equals(interpreterName)) {
					return object.getString("id");
				}
			}

		} catch (final JSONException e) {
			log.error(UNABLE_TO_GET_INTERPRETER + interpreterName + " id to restart");
			throw new NotebookServiceException(UNABLE_TO_GET_INTERPRETER + interpreterName + " id");
		}

		return interpreterName;
	}

	private boolean isSharedInterpreterConfiguration(String interpreterName) throws URISyntaxException, IOException {

		final String interpreterId = getInterpreterId(interpreterName);

		return isSharedInterpreterConfiguration(interpreterName, interpreterId);
	}

	private boolean isSharedInterpreterConfiguration(String interpreterName, String interpreterId)
			throws URISyntaxException, IOException {

		ResponseEntity<String> responseEntity;

		responseEntity = sendHttp(API_INTERPRETER_SETTING_STR.concat(interpreterId), HttpMethod.GET, "");

		try {
			final JSONObject jsonResponse = new JSONObject(responseEntity.getBody());
			final JSONObject interpreterConfiguration = jsonResponse.getJSONObject("body").getJSONObject("option");

			if (interpreterConfiguration.isNull("perNote")) {
				return true;
			} else {
				final String pernote = interpreterConfiguration.getString("perNote");
				if (pernote.isEmpty() || pernote.equals("shared")) {
					return true;
				}
			}
		} catch (final JSONException e) {
			log.error(UNABLE_TO_GET_INTERPRETER + interpreterName + " configuration to restart");
			throw new NotebookServiceException(UNABLE_TO_GET_INTERPRETER + interpreterName + " configuration");
		}

		return false;
	}

	@Override
	public ResponseEntity<String> restartAllInterpretersNotebook(String notebookId, String body, User user)
			throws URISyntaxException, IOException {

		final JSONObject responseBody = new JSONObject();

		final Map<String, String> notebookInterpreters = getNotebookInterpreters(notebookId);

		for (final Map.Entry<String, String> entry : notebookInterpreters.entrySet()) {
			final String key = "Interpreter " + entry.getKey();
			try {
				if (userService.isUserAdministrator(user) || (userService.isUserAnalytics(user)
						&& !isSharedInterpreterConfiguration(entry.getKey(), entry.getValue()))) {
					final ResponseEntity<String> responseEntity = restartInterpreter(entry.getKey(), entry.getValue(),
							body);
					if (responseEntity.getStatusCode() == HttpStatus.OK) {
						responseBody.put(key, "OK");
					} else {
						responseBody.put(key, ERROR + responseEntity.getBody());
					}

				} else {
					log.error(USER2 + user.getUserId() + HAS_NOT_PERMISSION_TO_RESTART_INTERPRETER + entry.getKey()
							+ ". Check interpreter configuration.");
					responseBody.put(key, ERROR + USER2 + HAS_NOT_PERMISSION_TO_RESTART_INTERPRETER);
				}
			} catch (final Exception e) {
				responseBody.put(key, ERROR + e);
			}
		}
		final HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
		return new ResponseEntity<>(responseBody.toString(), responseHeaders, HttpStatus.OK);
	}

	private String getDefaultInterpreter() throws URISyntaxException, IOException {
		ResponseEntity<String> responseEntity;

		responseEntity = sendHttp(API_INTERPRETER_SETTING_STR, HttpMethod.GET, "");
		try {
			final JSONObject jsonResponse = new JSONObject(responseEntity.getBody());
			final JSONArray interpreters = jsonResponse.getJSONArray("body");

			for (int i = 0; i < interpreters.length(); i++) {
				final JSONObject object = interpreters.getJSONObject(i);
				final String name = object.getString("name");
				final JSONArray interpreterGroup = object.getJSONArray("interpreterGroup");
				for (int j = 0; j < interpreterGroup.length(); j++) {
					if (interpreterGroup.getJSONObject(j).getBoolean("defaultInterpreter")) {
						return name;
					}
				}
			}
		} catch (final JSONException e) {
			log.error(UNABLE_TO_GET_DEFAULT_INTERPRETER);
			throw new NotebookServiceException(UNABLE_TO_GET_DEFAULT_INTERPRETER);
		}

		log.error(UNABLE_TO_GET_DEFAULT_INTERPRETER);
		return "";
	}

	private Map<String, String> getAllInterpretersNameAndId() throws URISyntaxException, IOException {

		ResponseEntity<String> responseEntity;
		responseEntity = sendHttp(API_INTERPRETER_SETTING_STR, HttpMethod.GET, "");
		final Map<String, String> interpreterList = new HashMap<>();

		try {
			final JSONObject jsonResponse = new JSONObject(responseEntity.getBody());
			final JSONArray interpreters = jsonResponse.getJSONArray("body");

			for (int i = 0; i < interpreters.length(); i++) {
				final JSONObject object = interpreters.getJSONObject(i);
				final String id = object.getString("id");
				final String name = object.getString("name");
				if (!interpreterList.containsKey(name)) {
					interpreterList.put(name, id);
				}
			}
		} catch (final JSONException e) {
			log.error("Unable to get default interpreter list");
			throw new NotebookServiceException("Unable to get default interpreter list");
		}

		return interpreterList;
	}

	@Override
	public Map<String, String> getNotebookInterpreters(String notebookId) throws URISyntaxException, IOException {
		ResponseEntity<String> responseEntity;

		responseEntity = sendHttp(API_NOTEBOOK_STR.concat(notebookId), HttpMethod.GET, "");

		final Map<String, String> listInterpreters = getAllInterpretersNameAndId();
		final String defaultInterpreter = getDefaultInterpreter();
		final Map<String, String> notebookInterpreters = new HashMap<>();

		try {
			final JSONObject jsonResponse = new JSONObject(responseEntity.getBody());
			final JSONArray paragraphs = jsonResponse.getJSONObject("body").getJSONArray("paragraphs");

			for (int i = 0; i < paragraphs.length(); i++) {
				final JSONObject object = paragraphs.getJSONObject(i);
				if (!object.isNull("text")) {
					final String text = object.getString("text");

					final String firstLine = text.split("\n")[0];
					if (firstLine.startsWith("%")) {
						String nameInterpreter;
						if (firstLine.contains(".")) {
							nameInterpreter = firstLine.substring(1).split("\\.")[0];
						} else {
							nameInterpreter = firstLine.substring(1);
						}
						if (!notebookInterpreters.containsKey(nameInterpreter)
								&& listInterpreters.containsKey(nameInterpreter)) {
							notebookInterpreters.put(nameInterpreter, listInterpreters.get(nameInterpreter));
						} else if (!notebookInterpreters.containsKey(nameInterpreter)
								&& !notebookInterpreters.containsKey(defaultInterpreter)) {
							notebookInterpreters.put(defaultInterpreter, listInterpreters.get(defaultInterpreter));
						}
					} else if (!notebookInterpreters.containsKey(defaultInterpreter)) {
						notebookInterpreters.put(defaultInterpreter, listInterpreters.get(defaultInterpreter));
					}
				}
			}
		} catch (final JSONException e) {
			log.error("Unable to get interpreter notebook list");
			throw new NotebookServiceException("Unable to get interpreter notebook list");
		}

		return notebookInterpreters;
	}
	
	@Override
	public List<Notebook> getNotebooksForListWithProjectsAccess(String userId) {

		final User user = userRepository.findByUserId(userId);
		List<NotebookForList> notebookForLists = notebookRepository.findAllNotebookList();
		if (!user.isAdmin()) {
			securityService.setSecurityToInputList(notebookForLists, user, "Notebook");
		}
		final List<Notebook> notebookList = new ArrayList<>();
		for (NotebookForList n: notebookForLists) {
			if (n.getAccessType() != null) {
				Notebook notebook = getNotebook(n.getId());
				notebookList.add(notebook);
			}
		}
		
		return notebookList;
	}

	@Override
	public List<OPResourceDTO> getDtoByUserAndPermissions(String userId, String identification) {
		final User user = userRepository.findByUserId(userId);
		if (!userService.isUserAdministrator(user)) {
			return notebookRepository.findAllDto(identification);
		} else {
			return notebookRepository.findDtoByUserAndPermissions(user, identification);
		}
	}

}
