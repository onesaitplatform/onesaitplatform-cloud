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
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.config.model.BinaryFile;
import com.minsait.onesait.platform.config.services.objectstorage.dto.MinioQueryUserResponse;
import com.minsait.onesait.platform.config.services.objectstorage.exception.ObjectStoreBucketCreateException;
import com.minsait.onesait.platform.config.services.objectstorage.exception.ObjectStoreCreateGroupException;
import com.minsait.onesait.platform.config.services.objectstorage.exception.ObjectStoreCreatePolicyException;
import com.minsait.onesait.platform.config.services.objectstorage.exception.ObjectStoreCreateUserException;
import com.minsait.onesait.platform.config.services.objectstorage.exception.ObjectStoreLoginException;
import com.minsait.onesait.platform.config.services.objectstorage.exception.ObjectStoreSetGroupToUsersException;

public interface ObjectStorageService {

	public String logIntoObjectStorageWithSuperUser() throws ObjectStoreLoginException;

	public String logIntoAdministrationObjectStorage(String accesKey, String secretKey, String superUserToken)
			throws ObjectStoreLoginException;

	public String logIntoBrowserObjectStorage(String accesKey, String secretKey, String superUserToken)
			throws ObjectStoreLoginException;

	public void createUserInObjectStore(String authToken, String userId, String userPlatformToken)
			throws ObjectStoreCreateUserException;

	public boolean existUserInObjectStore(String authToken, String userId);

	public void createBucketForUser(String userId) throws ObjectStoreBucketCreateException;

	public void createPolicyForBucketUser(String authToken, String userId) throws ObjectStoreCreatePolicyException;

	public List<BinaryFile> listUserFiles(String userId);

	public String encodeWithTemporalId(String objectName);

	public String decodeTemporalId(String temporalId);

	public BinaryFile buildBinaryFile(String userId, String objectName);

	public boolean existPolicy(String authToken, String policyName);

	public void createPolicyToReadFile(String authToken, String policyName, String pathToFile)
			throws ObjectStoreCreatePolicyException;

	public void createPolicyToWriteFile(String authToken, String policyName, String pathToFile)
			throws ObjectStoreCreatePolicyException;

	public MinioQueryUserResponse getUserInObjectStore(String authToken, String userId);

	public void setPoliciesForUser(String authToken, List<String> currentPoliciesForUser, String userToSetPolicies)
			throws ObjectStoreCreatePolicyException;

	public String[] getUsersForPolicy(String authToken, String policyName) throws ObjectStoreCreatePolicyException;

	public boolean removePolicy(String authToken, String policyName);

	public ResponseEntity<Resource> downloadFile(String authToken, String filePath) throws IOException;

	public boolean removeObject(String authToken, String filePath);

	public String getUserBucketName(String userId);

	public boolean uploadObject(String authToken, String bucketName, String fileDestinationPath, MultipartFile file);

	public void createGroup(String authToken, String groupName) throws ObjectStoreCreateGroupException;

	public boolean existsGroup(String authToken, String groupName);

	public List<MinioQueryUserResponse> getUsersInObjectStore(String authToken) throws ObjectStoreCreateGroupException;

	public void setUsersToGroup(String authToken, List<String> usersId, List<String> groupsName)
			throws ObjectStoreSetGroupToUsersException;

	public void setPoliciesMulti(String authToken, List<String> policiesName, List<String> usersToSetPolicies,
			List<String> groupsToSetPolicies) throws ObjectStoreCreatePolicyException;

	public ResponseEntity<Resource> getFileListByPath(String authToken, String filePath);

	public ResponseEntity<Resource> getBuckets(String authToken);

}
