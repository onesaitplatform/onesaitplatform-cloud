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
package com.minsait.onesait.platform.controlpanel.rest.management.objectstorage;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.config.model.BinaryFile;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.binaryfile.BinaryFileService;
import com.minsait.onesait.platform.config.services.objectstorage.ObjectStorageService;
import com.minsait.onesait.platform.config.services.objectstorage.exception.ObjectStoreBucketCreateException;
import com.minsait.onesait.platform.config.services.objectstorage.exception.ObjectStoreCreatePolicyException;
import com.minsait.onesait.platform.config.services.objectstorage.exception.ObjectStoreCreateUserException;
import com.minsait.onesait.platform.config.services.objectstorage.exception.ObjectStoreLoginException;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.config.services.usertoken.UserTokenService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "Object Storage")
@RestController
@RequestMapping("api/objectstorage")
public class ObjectStorageManagementController {

	@Autowired
	private AppWebUtils utils;

	@Autowired
	private UserService userService;

	@Autowired
	private UserTokenService userTokenService;

	@Autowired
	private ObjectStorageService objectStorageService;

	@Autowired
	private IntegrationResourcesService resourcesService;

	@Autowired
	private BinaryFileService binaryFileService;

	@Operation(summary = "Create user in object storage")
	@PostMapping("/management/createUser/{identification}")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "User Created", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "409", description = "Already Exists in Object Store"),
			@ApiResponse(responseCode = "400", description = "Bad request"),
			@ApiResponse(responseCode = "401", description = "Unauthorized"),
			@ApiResponse(responseCode = "403", description = "Forbidden"),
			@ApiResponse(responseCode = "404", description = "Not Found in Onesait Platform"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error") })
	public ResponseEntity<Void> createUser(
			@Parameter(description = "User identification", required = true) @PathVariable("identification") String userIdentification)
			throws UnsupportedEncodingException {
		final String identification = URLDecoder.decode(userIdentification, StandardCharsets.UTF_8.name());

		final String requesterUser = utils.getUserId();
		final String requesterRole = utils.getRole();

		final User userToCreate = userService.getUser(identification);

		if (userToCreate == null) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}

		if (!requesterRole.equals(Role.Type.ROLE_ADMINISTRATOR.name()) && !requesterUser.equals(identification)) {
			return new ResponseEntity<Void>(HttpStatus.FORBIDDEN);
		}

		if (requesterRole.equals(Role.Type.ROLE_DATAVIEWER.name()) || requesterRole.equals(Role.Type.ROLE_DEVOPS.name())
				|| requesterRole.equals(Role.Type.ROLE_PLATFORM_ADMIN.name())
				|| requesterRole.equals(Role.Type.ROLE_USER.name())
				|| requesterRole.equals(Role.Type.ROLE_EDGE_ADMINISTRATOR.name())
				|| requesterRole.equals(Role.Type.ROLE_EDGE_DEVELOPER.name())
				|| requesterRole.equals(Role.Type.ROLE_EDGE_USER.name())
				|| requesterRole.equals(Role.Type.ROLE_PARTNER.name())
				|| requesterRole.equals(Role.Type.ROLE_PREVERIFIED_TENANT_USER.name())) {
			return new ResponseEntity<Void>(HttpStatus.FORBIDDEN);
		}

		// The requester is administrator or is the user requesting to create himself a
		// user in Object Storage
		try {
			// GetToken
			final String authToken = objectStorageService.logIntoObjectStorageWithSuperUser();

			// Check if user Exists
			final boolean existsUser = objectStorageService.existUserInObjectStore(authToken, userToCreate.getUserId());
			if (!existsUser) {// TODO --> ROLLBACK SI ALGO FALLA

				objectStorageService.createBucketForUser(userToCreate.getUserId());
				objectStorageService.createPolicyForBucketUser(authToken, userToCreate.getUserId());
				objectStorageService.createUserInObjectStore(authToken, userToCreate.getUserId(),
						userTokenService.getToken(userToCreate).getToken());

				return new ResponseEntity<Void>(HttpStatus.CREATED);
			} else {
				return new ResponseEntity<Void>(HttpStatus.CONFLICT);
			}
		} catch (ObjectStoreLoginException | ObjectStoreCreateUserException | ObjectStoreBucketCreateException
				| ObjectStoreCreatePolicyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ResponseEntity<Void>(HttpStatus.CREATED);
	}

	@Operation(summary = "Upload/update Object to Object Store")
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Object Uploaded", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Bad request"),
			@ApiResponse(responseCode = "401", description = "Unauthorized"),
			@ApiResponse(responseCode = "403", description = "Forbidden"),
			@ApiResponse(responseCode = "404", description = "Not Found in Onesait Platform"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error") })
	public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file,
			@RequestParam(value = "filePath", required = true) String filePath) {

		try {

			if (file.getSize() > getMaxSize().longValue()) {
				return new ResponseEntity<>("File is larger than max size allowed", HttpStatus.INTERNAL_SERVER_ERROR);
			}

			final String requesterUser = utils.getUserId();

			String objectStoreAuthToken = null;
			try {
				objectStoreAuthToken = objectStorageService.logIntoObjectStorageWithSuperUser();
			} catch (final ObjectStoreLoginException e) {
				log.error("Error loing with superuser in MinIO", e);
				throw e;
			}

			final String requesterUserToken = userTokenService.getToken(userService.getUser(requesterUser)).getToken();

			if (!objectStorageService.existUserInObjectStore(objectStoreAuthToken, requesterUser)) {
				objectStorageService.createBucketForUser(requesterUser);
				objectStorageService.createPolicyForBucketUser(objectStoreAuthToken, requesterUser);
				objectStorageService.createUserInObjectStore(objectStoreAuthToken, requesterUser, requesterUserToken);
			}

			final String userToken = objectStorageService.logIntoAdministrationObjectStorage(requesterUser,
					requesterUserToken, objectStoreAuthToken);

			objectStorageService.uploadObject(userToken, objectStorageService.getUserBucketName(requesterUser),
					filePath, file);

			return new ResponseEntity<>(HttpStatus.OK);

		} catch (final Exception e) {
			log.error("Could not create binary file: {}", e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Operation(summary = "Delete Object in Object Store")
	@DeleteMapping("")
	public ResponseEntity<?> delete(@RequestParam("filePath") String filePath) {
		try {

			final String requesterUser = utils.getUserId();
			final String requesterUserToken = userTokenService.getToken(userService.getUser(requesterUser)).getToken();

			final String objectStoreAuthToken = objectStorageService.logIntoObjectStorageWithSuperUser();
			final String userToken = objectStorageService.logIntoAdministrationObjectStorage(requesterUser,
					requesterUserToken, objectStoreAuthToken);

			final String userBucketName = objectStorageService.getUserBucketName(requesterUser);

			if (!filePath.startsWith(userBucketName)) {
				if (filePath.startsWith("/")) {
					filePath = userBucketName + filePath;
				} else {
					filePath = userBucketName + "/" + filePath;
				}
			}

			// Borrar el fichero en el Object Store
			if (!objectStorageService.removeObject(userToken, filePath)) {
				log.warn("user not allowed to remove file");
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}

			// Borrar las politicas asociadas al fichero que pudieran existir (READ o WRITE
			// al haber concedido permisos)
			final String policyReadName = "Allow_READ_" + filePath.replace('/', '_').replace('.', '_');
			final String policyWriteName = "Allow_WRITE_" + filePath.replace('/', '_').replace('.', '_');

			if (objectStorageService.existPolicy(objectStoreAuthToken, policyReadName)) {
				objectStorageService.removePolicy(objectStoreAuthToken, policyReadName);
			}

			if (objectStorageService.existPolicy(objectStoreAuthToken, policyWriteName)) {
				objectStorageService.removePolicy(objectStoreAuthToken, policyWriteName);
			}

			// Borrar las asociaciaciones asociadas el fichero en BDC
			final List<BinaryFile> lFilesFromBDC = binaryFileService.getFileByPath(filePath);
			if (null != lFilesFromBDC && lFilesFromBDC.size() > 0) {
				binaryFileService.deleteFile(lFilesFromBDC.get(0).getId());
			}

		} catch (final ObjectStoreLoginException e) {
			log.error("Error removing file", e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}

	@Operation(summary = "Get Object from Object Store")
	@GetMapping("")
	public ResponseEntity<Resource> getBinary(@RequestParam("filePath") String filePath) {
		try {
			final String requesterUser = utils.getUserId();
			final String requesterUserToken = userTokenService.getToken(userService.getUser(requesterUser)).getToken();
			String objectStoreAuthToken = null;
			try {
				objectStoreAuthToken = objectStorageService.logIntoObjectStorageWithSuperUser();
			} catch (final ObjectStoreLoginException e) {
				log.error("Error loing with superuser in MinIO", e);
				throw e;
			}

			final String userToken = objectStorageService.logIntoAdministrationObjectStorage(requesterUser,
					requesterUserToken, objectStoreAuthToken);

			final String userBucketName = objectStorageService.getUserBucketName(requesterUser);

			if (!filePath.startsWith(userBucketName)) {
				if (filePath.startsWith("/")) {
					filePath = userBucketName + filePath;
				} else {
					filePath = userBucketName + "/" + filePath;
				}
			}

			return objectStorageService.downloadFile(userToken, filePath);

		} catch (final IOException e) {
			log.error("Error downloading file", e);
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} catch (final ObjectStoreLoginException e) {
			log.error("Error authenticating with object store server", e);
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

	}

	@Operation(summary = "Get list files by path")
	@GetMapping("getFileList")
	public ResponseEntity<?> getFileListByPath(@RequestParam(value = "bucket", required = false) String bucket,
			@RequestParam("prefix") String prefix) {
		try {
			String requesterUser = utils.getUserId();
			String requesterUserToken = userTokenService.getToken(userService.getUser(requesterUser)).getToken();
			String objectStoreAuthToken = null;
			try {
				objectStoreAuthToken = this.objectStorageService.logIntoObjectStorageWithSuperUser();
			} catch (ObjectStoreLoginException e) {
				log.error("Error loing with superuser in MinIO", e);
				throw e;
			}

			String userToken = this.objectStorageService.logIntoAdministrationObjectStorage(requesterUser,
					requesterUserToken, objectStoreAuthToken);

			String userBucketName;
			if (bucket == null) {
				userBucketName = this.objectStorageService.getUserBucketName(requesterUser);
			} else {
				if (utils.getRole().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
					userBucketName = bucket;
				} else {
					userBucketName = this.objectStorageService.getUserBucketName(requesterUser);
				}
			}

			if (!prefix.startsWith(userBucketName)) {
				if (prefix.startsWith("/")) {
					prefix = userBucketName + prefix;
				} else {
					prefix = userBucketName + "/" + prefix;
				}
			}
			return this.objectStorageService.getFileListByPath(userToken, prefix);

		} catch (ObjectStoreLoginException e) {
			log.error("Error authenticating with object store server", e);
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

	}

	@Operation(summary = "Get buckets list")
	@GetMapping("getBuckets")
	public ResponseEntity<?> getBucketsList() {
		try {
			String requesterUser = utils.getUserId();
			String requesterUserToken = userTokenService.getToken(userService.getUser(requesterUser)).getToken();
			String objectStoreAuthToken = null;
			try {
				objectStoreAuthToken = this.objectStorageService.logIntoObjectStorageWithSuperUser();
			} catch (ObjectStoreLoginException e) {
				log.error("Error loing with superuser in MinIO", e);
				throw e;
			}

			String userToken = this.objectStorageService.logIntoAdministrationObjectStorage(requesterUser,
					requesterUserToken, objectStoreAuthToken);

			if (utils.getRole().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {

				return this.objectStorageService.getBuckets(userToken);
			} else {
				JSONObject jsonResult = new JSONObject();
				JSONArray buckets = new JSONArray();
				JSONObject bucket = new JSONObject();
				bucket.put("name", this.objectStorageService.getUserBucketName(requesterUser));
				buckets.put(bucket);
				jsonResult.put("buckets", buckets);
				return new ResponseEntity<>(jsonResult.toString(), HttpStatus.OK);
			}

		} catch (ObjectStoreLoginException e) {
			log.error("Error authenticating with object store server", e);
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

	}

	private Long getMaxSize() {
		return (Long) resourcesService.getGlobalConfiguration().getEnv().getFiles().get("max-size");
	}

}
