/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2022 SPAIN
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
package com.minsait.onesait.platform.config.services.objectstorage;

import java.io.IOException;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.activation.MimetypesFileTypeMap;
import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import com.minsait.onesait.platform.commons.ActiveProfileDetector;
import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.config.components.Urls;
import com.minsait.onesait.platform.config.model.BinaryFile;
import com.minsait.onesait.platform.config.model.BinaryFile.RepositoryType;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.binaryfile.BinaryFileService;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.services.objectstorage.dto.MinioChangePasswordRequest;
import com.minsait.onesait.platform.config.services.objectstorage.dto.MinioCreateGroupRequest;
import com.minsait.onesait.platform.config.services.objectstorage.dto.MinioCreatePolicyBucketUserRequest;
import com.minsait.onesait.platform.config.services.objectstorage.dto.MinioCreatePolicyFileReadRequest;
import com.minsait.onesait.platform.config.services.objectstorage.dto.MinioCreatePolicyFileWriteRequest;
import com.minsait.onesait.platform.config.services.objectstorage.dto.MinioCreateUserRequest;
import com.minsait.onesait.platform.config.services.objectstorage.dto.MinioCreateUserResponse;
import com.minsait.onesait.platform.config.services.objectstorage.dto.MinioGetAllUsersResponse;
import com.minsait.onesait.platform.config.services.objectstorage.dto.MinioGetGroupResponse;
import com.minsait.onesait.platform.config.services.objectstorage.dto.MinioLoginRequest;
import com.minsait.onesait.platform.config.services.objectstorage.dto.MinioLoginResponse;
import com.minsait.onesait.platform.config.services.objectstorage.dto.MinioPolicyResponse;
import com.minsait.onesait.platform.config.services.objectstorage.dto.MinioQueryUserResponse;
import com.minsait.onesait.platform.config.services.objectstorage.dto.MinioSetPolicyMultiRequest;
import com.minsait.onesait.platform.config.services.objectstorage.dto.MinioSetPolicyToUserRequest;
import com.minsait.onesait.platform.config.services.objectstorage.dto.MinioUsersGroupsBulkRequest;
import com.minsait.onesait.platform.config.services.objectstorage.dto.MultipartInputStreamFileResource;
import com.minsait.onesait.platform.config.services.objectstorage.exception.ObjectStoreBucketCreateException;
import com.minsait.onesait.platform.config.services.objectstorage.exception.ObjectStoreCreateGroupException;
import com.minsait.onesait.platform.config.services.objectstorage.exception.ObjectStoreCreatePolicyException;
import com.minsait.onesait.platform.config.services.objectstorage.exception.ObjectStoreCreateUserException;
import com.minsait.onesait.platform.config.services.objectstorage.exception.ObjectStoreLoginException;
import com.minsait.onesait.platform.config.services.objectstorage.exception.ObjectStoreSetGroupToUsersException;
import com.minsait.onesait.platform.config.services.user.UserService;

import io.minio.BucketExistsArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.RegionConflictException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Lazy
public class MinioObjectStorageService implements ObjectStorageService {

	private final static String PATH_SCAPE_FOR_TEMPORAL_ID = "__!__";

	public final static String ONTOLOGIES_DIR = "/datalake";

	public final static String ONESAIT_PLATFORM_PUBLIC_FILES_GROUP = "onesait_platform_public_files";

	private MinioClient minioClient;

	private RestTemplate restTemplate;

	private String minioBaseUrl;

	private String minioAdminUrl;

	private String minioBrowserUrl;

	@Value("${onesaitplatform.database.minio.access-key:access-key}")
	private String accessKey;

	@Value("${onesaitplatform.database.minio.secret-key:secret-key}")
	private String secretKey;

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private ActiveProfileDetector profileDetector;

	@Autowired
	private UserService userService;
	@Autowired
	private BinaryFileService binaryFileService;

	@PostConstruct
	public void init() {
		restTemplate = new RestTemplate(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());
		restTemplate.setErrorHandler(new ResponseErrorHandler() {// This error handler allow to handle 40X codes

			@Override
			public boolean hasError(ClientHttpResponse response) throws IOException {
				return false;
			}

			@Override
			public void handleError(ClientHttpResponse response) throws IOException {

			}
		});

		try {
			Urls urls = configurationService.getEndpointsUrls(profileDetector.getActiveProfile());

			this.minioBaseUrl = urls.getMinio().getBase();
			this.minioAdminUrl = urls.getMinio().getAdmin().getInternal();
			this.minioBrowserUrl = urls.getMinio().getBrowser().getInternal();

			if (!this.minioBaseUrl.endsWith("/")) {
				this.minioBaseUrl += "/";
			}

			if (!this.minioAdminUrl.endsWith("/")) {
				this.minioAdminUrl += "/";
			}

			if (!this.minioBrowserUrl.endsWith("/")) {
				this.minioBrowserUrl += "/";
			}

			final URI uri = new URI(this.minioBaseUrl);
			minioClient = MinioClient.builder().endpoint(uri.getHost(), uri.getPort(), false)
					.credentials(accessKey, secretKey).build();

		} catch (Exception e) {
			log.error("Error initializing MinioClient", e);
		}

		try {
			// Crea el grupo de ficheros publicos si no existe
			String authToken = this.logIntoObjectStorageWithSuperUser();
			if (!this.existsGroup(authToken, ONESAIT_PLATFORM_PUBLIC_FILES_GROUP)) {
				this.createGroup(authToken, ONESAIT_PLATFORM_PUBLIC_FILES_GROUP);
			}

			// Incluye a todos los usuarios que no estuvieran en el grupo de ficheros
			// publics
			List<MinioQueryUserResponse> lUsersInMinio = this.getUsersInObjectStore(authToken);
			List<String> usersToSetPolicy = new ArrayList<String>();
			lUsersInMinio.forEach(user -> {
				if (user.getMemberOf() == null) {
					usersToSetPolicy.add(user.getAccessKey());
				} else if (Arrays.asList(user.getMemberOf()).stream()
						.filter(p -> p.equals(ONESAIT_PLATFORM_PUBLIC_FILES_GROUP)).collect(Collectors.toList())
						.size() == 0) {
					usersToSetPolicy.add(user.getAccessKey());
				}
			});

			this.setUsersToGroup(authToken, usersToSetPolicy,
					new ArrayList<>(Arrays.asList(new String[] { ONESAIT_PLATFORM_PUBLIC_FILES_GROUP })));

		} catch (ObjectStoreLoginException | ObjectStoreCreateGroupException | ObjectStoreSetGroupToUsersException e) {
			log.warn("Error creating {} policy", ONESAIT_PLATFORM_PUBLIC_FILES_GROUP, e);
		} catch (Exception e) {
			log.warn("Error creating {} policy", ONESAIT_PLATFORM_PUBLIC_FILES_GROUP, e);
		}
	}

	@Override
	public void createUserInObjectStore(String authToken, String userId, String userPlatformToken)
			throws ObjectStoreCreateUserException {
		log.info("Try to create new User in MinIO");

		User user = userService.getUser(userId);

		List<String> policies = new ArrayList<String>();

		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			policies.add("consoleAdmin");
		}

		policies.add("Allow_" + userId + "_Bucket");

		MinioCreateUserRequest createUserRequest = new MinioCreateUserRequest(userId, userPlatformToken,
				new String[] { ONESAIT_PLATFORM_PUBLIC_FILES_GROUP }, policies.toArray(new String[policies.size()]));

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);

		HttpEntity<MinioCreateUserRequest> entity = new HttpEntity<>(createUserRequest, headers);

		ResponseEntity<MinioCreateUserResponse> response = restTemplate.exchange(this.minioAdminUrl + "api/v1/users",
				HttpMethod.POST, entity, MinioCreateUserResponse.class);

		if (response.getStatusCode() != HttpStatus.CREATED) {
			throw new ObjectStoreCreateUserException("Error creating user " + userId + " in MinIO");
		}
		log.info("User created in MinIO");
	}

	@Override
	public boolean existUserInObjectStore(String authToken, String userId) {
		log.info("Check if user exists in MinIO");

		return this.getUserInResponseEntity(authToken, userId).getStatusCode() == HttpStatus.OK;

	}

	@Override
	public MinioQueryUserResponse getUserInObjectStore(String authToken, String userId) {
		log.info("Get user from in MinIO");

		return this.getUserInResponseEntity(authToken, userId).getBody();

	}

	private ResponseEntity<MinioQueryUserResponse> getUserInResponseEntity(String authToken, String userId) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(this.minioAdminUrl + "api/v1/user")
				.queryParam("name", userId);

		ResponseEntity<MinioQueryUserResponse> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET,
				new HttpEntity<>(headers), MinioQueryUserResponse.class);

		if (response.getStatusCode() != HttpStatus.OK && response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
			log.warn("Erro quering user in MinIO. Status Code: " + response.getStatusCode());
		}

		return response;
	}

	@Override
	public String logIntoObjectStorageWithSuperUser() throws ObjectStoreLoginException {
		log.info("Log into MinIO system");

		return this.logIntoObjectStorage(this.minioAdminUrl, accessKey, secretKey, null);
	}

	@Override
	public String logIntoAdministrationObjectStorage(String accesKey, String secretKey, String superUserToken)
			throws ObjectStoreLoginException {
		return this.logIntoObjectStorage(this.minioAdminUrl, accesKey, secretKey, superUserToken);
	}

	@Override
	public String logIntoBrowserObjectStorage(String accesKey, String secretKey, String superUserToken)
			throws ObjectStoreLoginException {
		return this.logIntoObjectStorage(this.minioBrowserUrl, accesKey, secretKey, superUserToken);
	}

	private String logIntoObjectStorage(String serverUrl, String userAccesKey, String userSecretKey,
			String superUserToken) throws ObjectStoreLoginException {
		log.info("Log into MinIO system");

		MinioLoginRequest credentials = new MinioLoginRequest(userAccesKey, userSecretKey);

		ResponseEntity<MinioLoginResponse> response = restTemplate.postForEntity(serverUrl + "api/v1/login",
				credentials, MinioLoginResponse.class);

		if (response.getStatusCode() == HttpStatus.CREATED) {
			return response.getBody().getSessionId();
		} else if ((response.getStatusCode() == HttpStatus.UNAUTHORIZED
				|| response.getStatusCode() == HttpStatus.FORBIDDEN
				|| response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) && superUserToken != null) {// Sometimes
																												// return
																												// 500
																												// error
																												// code
																												// and
																												// the
																												// 403
																												// code
																												// in
																												// the
																												// message
			boolean changed = this.changeUserPassword(userAccesKey, userSecretKey, superUserToken);
			if (changed) {
				response = restTemplate.postForEntity(serverUrl + "api/v1/login", credentials,
						MinioLoginResponse.class);

				if (response.getStatusCode() != HttpStatus.CREATED) {
					log.error("Error loggin to MinIO after changing password. Response code {}",
							response.getStatusCodeValue());
					throw new ObjectStoreLoginException(
							"Error on login to MinIO console after changing password. Response Code: "
									+ response.getStatusCodeValue());
				}

				return response.getBody().getSessionId();
			}
		}

		log.warn("Error loggin to MinIO. Response code {}", response.getStatusCodeValue());
		throw new ObjectStoreLoginException(
				"Error on login to MinIO console. Response Code: " + response.getStatusCodeValue());

	}

	private boolean changeUserPassword(String username, String newPassword, String superUserToken) {
		MinioChangePasswordRequest changePwdRequest = new MinioChangePasswordRequest(username, newPassword);

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + superUserToken);

		HttpEntity<MinioChangePasswordRequest> entity = new HttpEntity<>(changePwdRequest, headers);

		ResponseEntity<Void> response = restTemplate.exchange(
				this.minioAdminUrl + "api/v1/account/change-user-password", HttpMethod.POST, entity, Void.class);

		return response.getStatusCode() == HttpStatus.CREATED;
	}

	@Override
	public void createBucketForUser(String userId) throws ObjectStoreBucketCreateException {
		try {
			String buckeName = getUserBucketName(userId);

			boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(buckeName).build());

			if (!bucketExists) {
				minioClient.makeBucket(MakeBucketArgs.builder().bucket(buckeName).build());
			}
		} catch (InvalidKeyException | ErrorResponseException | IllegalArgumentException | InsufficientDataException
				| InternalException | InvalidBucketNameException | InvalidResponseException | NoSuchAlgorithmException
				| ServerException | XmlParserException | IOException | RegionConflictException e) {
			log.error("Error creating bucket", e);
			throw new ObjectStoreBucketCreateException("Error creating bucket", e);
		}
	}

	@Override
	public void createPolicyForBucketUser(String authToken, String userId) throws ObjectStoreCreatePolicyException {
		String bucketName = getUserBucketName(userId);

		log.info("Create policty for bucket {}", bucketName);

		MinioCreatePolicyBucketUserRequest createPolicyRequest = new MinioCreatePolicyBucketUserRequest(
				"Allow_" + userId + "_Bucket", bucketName);

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);

		HttpEntity<MinioCreatePolicyBucketUserRequest> entity = new HttpEntity<>(createPolicyRequest, headers);

		ResponseEntity<MinioPolicyResponse> response = restTemplate.exchange(this.minioAdminUrl + "api/v1/policies",
				HttpMethod.POST, entity, MinioPolicyResponse.class);

		if (response.getStatusCode() != HttpStatus.CREATED) {
			throw new ObjectStoreCreatePolicyException("Error creating policy Al)low_" + userId + "_Bucket in MinIO");
		}
		log.info("Policy created in MinIO");

	}

	@Override
	public boolean existPolicy(String authToken, String policyName) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);

		HttpEntity<Void> entityCheckPolicy = new HttpEntity<>(headers);

		ResponseEntity<MinioPolicyResponse> responseCheckPolicy = restTemplate.exchange(
				this.minioAdminUrl + "api/v1/policy?name=" + policyName, HttpMethod.GET, entityCheckPolicy,
				MinioPolicyResponse.class);

		return responseCheckPolicy.getStatusCode() == HttpStatus.OK;

	}

	@Override
	public void createPolicyToReadFile(String authToken, String policyName, String pathToFile)
			throws ObjectStoreCreatePolicyException {

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);

		MinioCreatePolicyFileReadRequest createPolicyRequest = new MinioCreatePolicyFileReadRequest(policyName,
				pathToFile);

		HttpEntity<MinioCreatePolicyFileReadRequest> entityCreatePolicy = new HttpEntity<>(createPolicyRequest,
				headers);

		ResponseEntity<MinioPolicyResponse> responseCreatePolicy = restTemplate.exchange(
				this.minioAdminUrl + "api/v1/policies", HttpMethod.POST, entityCreatePolicy, MinioPolicyResponse.class);

		if (responseCreatePolicy.getStatusCode() != HttpStatus.OK
				&& responseCreatePolicy.getStatusCode() != HttpStatus.CREATED) {
			log.warn("Error creating policy to allow user to read file HttpCode: {}",
					responseCreatePolicy.getStatusCodeValue());
			throw new ObjectStoreCreatePolicyException("Error creating policy to allow user to read file");
		}
	}

	@Override
	public void createPolicyToWriteFile(String authToken, String policyName, String pathToFile)
			throws ObjectStoreCreatePolicyException {

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);

		MinioCreatePolicyFileWriteRequest createPolicyRequest = new MinioCreatePolicyFileWriteRequest(policyName,
				pathToFile);

		HttpEntity<MinioCreatePolicyFileWriteRequest> entityCreatePolicy = new HttpEntity<>(createPolicyRequest,
				headers);

		ResponseEntity<MinioPolicyResponse> responseCreatePolicy = restTemplate.exchange(
				this.minioAdminUrl + "api/v1/policies", HttpMethod.POST, entityCreatePolicy, MinioPolicyResponse.class);

		if (responseCreatePolicy.getStatusCode() != HttpStatus.OK
				&& responseCreatePolicy.getStatusCode() != HttpStatus.CREATED) {
			log.warn("Error creating policy to allow user to read file HttpCode: {}",
					responseCreatePolicy.getStatusCodeValue());
			throw new ObjectStoreCreatePolicyException("Error creating policy to allow user to read file");
		}
	}

	@Override
	public boolean removePolicy(String authToken, String policyName) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);

		HttpEntity<Void> entityCheckPolicy = new HttpEntity<>(headers);

		ResponseEntity<Void> responseCheckPolicy = restTemplate.exchange(
				this.minioAdminUrl + "api/v1/policy?name=" + policyName, HttpMethod.DELETE, entityCheckPolicy,
				Void.class);

		return (responseCheckPolicy.getStatusCode() == HttpStatus.OK
				|| responseCheckPolicy.getStatusCode() == HttpStatus.NO_CONTENT);
	}

	@Override
	public void setPoliciesForUser(String authToken, List<String> policiesForUser, String userToSetPolicies)
			throws ObjectStoreCreatePolicyException {

		MinioSetPolicyToUserRequest setPolicyRequest = new MinioSetPolicyToUserRequest(
				policiesForUser.toArray(new String[policiesForUser.size()]), userToSetPolicies);

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);

		HttpEntity<MinioSetPolicyToUserRequest> entity = new HttpEntity<>(setPolicyRequest, headers);

		ResponseEntity<MinioPolicyResponse> responseSetPolicies = restTemplate
				.exchange(this.minioAdminUrl + "api/v1/set-policy", HttpMethod.PUT, entity, MinioPolicyResponse.class);

		if (responseSetPolicies.getStatusCode() != HttpStatus.OK
				&& responseSetPolicies.getStatusCode() != HttpStatus.NO_CONTENT) {
			log.warn("Error setting policy to user HttpCode: {}", responseSetPolicies.getStatusCodeValue());
			throw new ObjectStoreCreatePolicyException("Error setting policy to user");
		}
	}

	@Override
	public void setPoliciesMulti(String authToken, List<String> policiesName, List<String> usersToSetPolicies,
			List<String> groupsToSetPolicies) throws ObjectStoreCreatePolicyException {

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);

		MinioSetPolicyMultiRequest setPolicyRequest = new MinioSetPolicyMultiRequest(
				policiesName.toArray(new String[policiesName.size()]),
				usersToSetPolicies.toArray(new String[usersToSetPolicies.size()]),
				groupsToSetPolicies.toArray(new String[groupsToSetPolicies.size()]));

		HttpEntity<MinioSetPolicyMultiRequest> entity = new HttpEntity<>(setPolicyRequest, headers);

		ResponseEntity<Void> responseSetPolicies = restTemplate.exchange(this.minioAdminUrl + "api/v1/set-policy-multi",
				HttpMethod.PUT, entity, Void.class);

		if (responseSetPolicies.getStatusCode() != HttpStatus.OK
				&& responseSetPolicies.getStatusCode() != HttpStatus.NO_CONTENT) {
			log.warn("Error setting policy to user HttpCode: {}", responseSetPolicies.getStatusCodeValue());
			throw new ObjectStoreCreatePolicyException("Error setting policy to user");
		}
	}

	@Override
	public String[] getUsersForPolicy(String authToken, String policyName) throws ObjectStoreCreatePolicyException {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);

		HttpEntity<Void> entityGetUsersForPolicy = new HttpEntity<>(headers);

		ResponseEntity<String[]> responseGetUsersForPolicy = restTemplate.exchange(
				this.minioAdminUrl + "api/v1/policies/" + policyName + "/users", HttpMethod.GET,
				entityGetUsersForPolicy, String[].class);

		if (responseGetUsersForPolicy.getStatusCode() != HttpStatus.OK
				&& responseGetUsersForPolicy.getStatusCode() != HttpStatus.NO_CONTENT) {
			log.warn("Error getting users for policy HttpCode: {}", responseGetUsersForPolicy.getStatusCodeValue());
			throw new ObjectStoreCreatePolicyException("Error getting users for policy ");
		}

		return responseGetUsersForPolicy.getBody();

	}

	@Override
	public List<BinaryFile> listUserFiles(String userId) {
		List<BinaryFile> filesList = new ArrayList<BinaryFile>();

		ListObjectsArgs args = ListObjectsArgs.builder().bucket(getUserBucketName(userId)).recursive(true)
				.useUrlEncodingType(false).build();
		this.minioClient.listObjects(args).forEach(object -> {
			try {
				if (!object.get().isDir()) {

					String objectName = object.get().objectName();

					BinaryFile bFile = this.buildBinaryFile(userId, getUserBucketName(userId) + "/" + objectName);
					bFile.setId(encodeWithTemporalId(getUserBucketName(userId) + "/" + objectName));// Id intermedio
																									// mientras no está
																									// en BD. Es
																									// necesario escapar
																									// las barras
					bFile.setUpdatedAt(Date.from(object.get().lastModified().toInstant()));

					filesList.add(bFile);
				}
			} catch (InvalidKeyException | ErrorResponseException | IllegalArgumentException | InsufficientDataException
					| InternalException | InvalidBucketNameException | InvalidResponseException
					| NoSuchAlgorithmException | ServerException | XmlParserException | IOException e) {
				log.error("Error listing user files", e);
			}
		});

		return filesList;

	}

	@Override
	public ResponseEntity<Resource> downloadFile(String authToken, String filePath) throws IOException {
		String bucketName = filePath.substring(0, filePath.indexOf('/'));
		String prefixSearch = filePath.substring(filePath.indexOf('/') + 1, filePath.length());

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);

		HttpEntity<Void> entityGetFile = new HttpEntity<>(headers);

		ResponseEntity<Resource> responseGetFile = restTemplate.exchange(
				this.minioAdminUrl + "api/v1/buckets/" + bucketName + "/objects/download?prefix=" + prefixSearch,
				HttpMethod.GET, entityGetFile, Resource.class);

		return responseGetFile;

	}

	@Override
	public ResponseEntity<Resource> getFileListByPath(String authToken, String filePath) {
		String bucketName = filePath.substring(0, filePath.indexOf('/'));
		String prefixSearch = filePath.substring(filePath.indexOf('/') + 1, filePath.length());

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);

		HttpEntity<Void> entityGetFile = new HttpEntity<>(headers);

		ResponseEntity<Resource> responseGetFile = restTemplate.exchange(
				this.minioAdminUrl + "api/v1/buckets/" + bucketName + "/objects?prefix=" + prefixSearch, HttpMethod.GET,
				entityGetFile, Resource.class);

		return responseGetFile;

	}

	@Override
	public ResponseEntity<Resource> getBuckets(String authToken) {

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);

		HttpEntity<Void> entityGetFile = new HttpEntity<>(headers);
		ResponseEntity<Resource> responseGetFile = restTemplate.exchange(this.minioAdminUrl + "api/v1/buckets",
				HttpMethod.GET, entityGetFile, Resource.class);
		return responseGetFile;

	}

	@Override
	public boolean uploadObject(String authToken, String bucketName, String fileDestinationPath, MultipartFile file) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		try {
			body.add(Integer.toString(file.getInputStream().available()),
					new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename()));
			String fileDestinationPathTemp = "/";
			if (fileDestinationPath != null && fileDestinationPath.trim().length() > 1) {
				fileDestinationPathTemp = fileDestinationPath + "/";
			}
			String filePath = bucketName + fileDestinationPathTemp + file.getOriginalFilename();
			List<BinaryFile> tempfile = this.binaryFileService.getFileByPath(filePath);
			if (tempfile != null && tempfile.size() > 0) {
				binaryFileService.updateUpdateTime(tempfile.get(0).getId());
			}

			if (null != fileDestinationPath && fileDestinationPath.trim().length() > 0) {
				fileDestinationPath = "?prefix=" + fileDestinationPath.trim();
				if (!fileDestinationPath.endsWith("/")) {
					fileDestinationPath += "/";
				}
			}

			HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

			ResponseEntity<String> responsePostFile = restTemplate.exchange(
					this.minioAdminUrl + "api/v1/buckets/" + bucketName + "/objects/upload" + fileDestinationPath,
					HttpMethod.POST, requestEntity, String.class);

			return (responsePostFile.getStatusCode() == HttpStatus.OK
					|| responsePostFile.getStatusCode() == HttpStatus.NO_CONTENT
					|| responsePostFile.getStatusCode() == HttpStatus.CREATED);

		} catch (IOException e) {
			log.error("Error uploading file", e);

		}

		return false;

	}

	@Override
	public boolean removeObject(String authToken, String filePath) {

		String bucketName = filePath.substring(0, filePath.indexOf('/'));
		String filePathInBucket = filePath.substring(filePath.indexOf('/') + 1, filePath.length());

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);

		HttpEntity<Void> entityCheckPolicy = new HttpEntity<>(headers);

		ResponseEntity<Void> responseCheckPolicy = restTemplate.exchange(
				this.minioAdminUrl + "api/v1/buckets/" + bucketName + "/objects?path=" + filePathInBucket,
				HttpMethod.DELETE, entityCheckPolicy, Void.class);

		return (responseCheckPolicy.getStatusCode() == HttpStatus.OK
				|| responseCheckPolicy.getStatusCode() == HttpStatus.NO_CONTENT);
	}

	@Override
	public String getUserBucketName(String userId) {
		if (userId.indexOf("_") > 0) {
			userId = userId.replace('_', '-');			
		}
		userId = userId.toLowerCase();
		return userId + "bucket";
	}

	@Override
	public String encodeWithTemporalId(String objectName) {
		return objectName.replaceAll("/", PATH_SCAPE_FOR_TEMPORAL_ID);
	}

	@Override
	public String decodeTemporalId(String temporalId) {
		return temporalId.replaceAll(PATH_SCAPE_FOR_TEMPORAL_ID, "/");
	}

	@Override
	public BinaryFile buildBinaryFile(String userId, String objectName) {
		BinaryFile bFile = new BinaryFile();

		int lastIndexOfSlash = objectName.lastIndexOf('/') > 0 ? objectName.lastIndexOf('/') + 1 : 0;
		int lastIndexOfDot = objectName.lastIndexOf('.') > 0 ? objectName.lastIndexOf('.') + 1 : 0;

		bFile.setIdentification(objectName.substring(lastIndexOfSlash));
		bFile.setFileExtension(objectName.substring(lastIndexOfDot));
		bFile.setFileName(objectName.substring(lastIndexOfSlash));
		bFile.setPath(objectName);
		bFile.setPublic(false);
		bFile.setRepository(RepositoryType.MINIO_S3);
		bFile.setUser(userService.getUserByIdentification(userId));

		MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();
		String mimeType = fileTypeMap.getContentType(objectName);

		mimeType = mimeType == null ? "application/octet-stream" : mimeType;
		bFile.setMime(mimeType);

		return bFile;
	}

	@Override
	public void createGroup(String authToken, String groupName) throws ObjectStoreCreateGroupException {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);

		MinioCreateGroupRequest createGroupRequest = new MinioCreateGroupRequest(groupName, new String[] {});

		HttpEntity<MinioCreateGroupRequest> entityCreateGroup = new HttpEntity<>(createGroupRequest, headers);

		ResponseEntity<Void> responseCreateGroup = restTemplate.exchange(this.minioAdminUrl + "api/v1/groups",
				HttpMethod.POST, entityCreateGroup, Void.class);

		if (responseCreateGroup.getStatusCode() != HttpStatus.OK
				&& responseCreateGroup.getStatusCode() != HttpStatus.CREATED) {
			log.warn("Error creating policy to allow user to read file HttpCode: {}",
					responseCreateGroup.getStatusCodeValue());
			throw new ObjectStoreCreateGroupException("Error creating policy to allow user to read file");
		}
	}

	@Override
	public boolean existsGroup(String authToken, String groupName) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);

		HttpEntity<Void> entityGetGroup = new HttpEntity<>(headers);

		ResponseEntity<MinioGetGroupResponse> responseCreateGroup = restTemplate.exchange(
				this.minioAdminUrl + "api/v1/group?name=" + groupName, HttpMethod.GET, entityGetGroup,
				MinioGetGroupResponse.class);

		if (responseCreateGroup.getStatusCode() != HttpStatus.OK) {
			return false;
		}
		return responseCreateGroup.getBody().getName().equals(groupName);
	}

	@Override
	public List<MinioQueryUserResponse> getUsersInObjectStore(String authToken) throws ObjectStoreCreateGroupException {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);

		HttpEntity<Void> entityGetAllUsers = new HttpEntity<>(headers);

		ResponseEntity<MinioGetAllUsersResponse> responseGetAllUsers = restTemplate.exchange(
				this.minioAdminUrl + "api/v1/users", HttpMethod.GET, entityGetAllUsers, MinioGetAllUsersResponse.class);

		if (responseGetAllUsers.getStatusCode() != HttpStatus.OK) {
			log.warn("Error creating policy to allow user to read file HttpCode: {}",
					responseGetAllUsers.getStatusCodeValue());
			throw new ObjectStoreCreateGroupException("Error creating policy to allow user to read file");
		}
		if (responseGetAllUsers.getBody().getUsers() == null) {// No hay usuarios
			return new ArrayList<MinioQueryUserResponse>();
		} else {
			return new ArrayList<>(Arrays.asList(responseGetAllUsers.getBody().getUsers()));
		}

	}

	@Override
	public void setUsersToGroup(String authToken, List<String> usersId, List<String> groupsName)
			throws ObjectStoreSetGroupToUsersException {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);

		MinioUsersGroupsBulkRequest setUsersGroupRequest = new MinioUsersGroupsBulkRequest(
				usersId.toArray(new String[usersId.size()]), groupsName.toArray(new String[groupsName.size()]));

		HttpEntity<MinioUsersGroupsBulkRequest> entity = new HttpEntity<>(setUsersGroupRequest, headers);

		ResponseEntity<Void> responseSetGroup = restTemplate.exchange(this.minioAdminUrl + "api/v1/users-groups-bulk",
				HttpMethod.PUT, entity, Void.class);

		if (responseSetGroup.getStatusCode() != HttpStatus.OK
				&& responseSetGroup.getStatusCode() != HttpStatus.NO_CONTENT) {
			log.warn("Error setting policy to user HttpCode: {}", responseSetGroup.getStatusCodeValue());
			throw new ObjectStoreSetGroupToUsersException("Error setting policy to user");
		}
	}

}
