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
package com.minsait.onesait.platform.persistence.historical.minio;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.services.objectstorage.MinioObjectStorageService;
import com.minsait.onesait.platform.config.services.objectstorage.ObjectStorageService;
import com.minsait.onesait.platform.config.services.objectstorage.exception.ObjectStoreBucketCreateException;
import com.minsait.onesait.platform.config.services.objectstorage.exception.ObjectStoreCreatePolicyException;
import com.minsait.onesait.platform.config.services.objectstorage.exception.ObjectStoreCreateUserException;
import com.minsait.onesait.platform.config.services.objectstorage.exception.ObjectStoreLoginException;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.config.services.usertoken.UserTokenService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import io.minio.BucketExistsArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.UploadObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.MinioException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Lazy
public class HistoricalMinioServiceImpl implements HistoricalMinioService {
	
	@Autowired 
	private OntologyRepository ontologyRepository;
	
	@Autowired
	private IntegrationResourcesService resourcesService;	
	
	@Autowired
	private ObjectStorageService objectStorageService;
	
	@Autowired
	private UserService userService;

	@Autowired
	private UserTokenService userTokenService;
	
	
	private String url;
	@Value("${onesaitplatform.database.minio.access-key:access-key}")
	private String accessKey;
	@Value("${onesaitplatform.database.minio.secret-key:secret-key}")
	private String secretKey;
	
	private static final String HISTORICAL_BUCKET_PREFIX = "presto-";
	private static final String EXTERNAL_LOCATION = "external_location";

	private MinioClient minioClient;
	
	@PostConstruct
	public void init() {
		try {
			url = resourcesService.getUrl(Module.MINIO, ServiceUrl.BASE);
			final URI uri = new URI(url);
			minioClient = MinioClient.builder()
		    		.endpoint(uri.getHost(), uri.getPort(), false)
		    		.credentials(accessKey, secretKey)
		    		.build();
		}catch(Exception e) {
			log.error("Error initializing MinioClient",e );
		}
	}
	
	
	@Override
	public void createUserAndBucketIfNotExists(String userId) throws HistoricalMinioException {
		
		try {
			String superUserToken = this.objectStorageService.logIntoObjectStorageWithSuperUser();
			if(!this.objectStorageService.existUserInObjectStore(superUserToken, userId)) {
				
				String requesterUserToken = userTokenService.getToken(userService.getUser(userId)).getToken();
				
				objectStorageService.createBucketForUser(userId);
				objectStorageService.createPolicyForBucketUser(superUserToken, userId);
				objectStorageService.createUserInObjectStore(superUserToken, userId, requesterUserToken);	
			}			
			
		} catch (ObjectStoreLoginException | ObjectStoreBucketCreateException | ObjectStoreCreateUserException | ObjectStoreCreatePolicyException e) {
			log.error("Error creating user or bucket in Object store", e);
			throw new HistoricalMinioException("Error creating user or bucket in Object store",e);
		}
		
	}

	@Override
	public void createBucketDirectory(String ontology) throws HistoricalMinioException {
		
			String bucket = this.getBucketForOntology(ontology);

			String uuid = java.util.UUID.randomUUID().toString();
			File file = new File(uuid);
			try {
				file.createNewFile();
			} catch (IOException e1) {
				log.error("Unable to create file {} for presto bucket {}", uuid, bucket);
			}
			try {
				minioClient.uploadObject(
				    UploadObjectArgs.builder()
				        .bucket(bucket).object(getOntologyPathIntoBucket(ontology) + "/" + uuid).filename(uuid).build());
			} catch (InvalidKeyException | ErrorResponseException | IllegalArgumentException | InsufficientDataException
					| InternalException | InvalidResponseException | NoSuchAlgorithmException
					| ServerException | XmlParserException | IOException | InvalidBucketNameException e) {
				file.delete();
				log.error("Error creating directory in bucket {} : {} ", bucket, e.getMessage());
				throw new HistoricalMinioException(e.getMessage(), e);
			}
			file.delete();
	}
	
	@Override
	public boolean validateBucketString(String ontology, String statement) {
		final String validBucket = getExternalLocationForOntology(ontology);

		int indexExternalLocation = statement.toLowerCase().indexOf(EXTERNAL_LOCATION);
		if (indexExternalLocation == -1)
			return false;
		String str = statement.substring(indexExternalLocation + EXTERNAL_LOCATION.length()).trim();
		if (str.indexOf("=") == 0 && str.substring(1).trim().indexOf("'") == 0) {
			str = str.substring(str.indexOf("'")+1).trim();
			if (str.length() > validBucket.length()) {
				str = str.substring(0, validBucket.length());
				if (str.equals(validBucket)) {
					return true;
				}
			}
		}		
		return false;
	}
	
	@Override
	public String getBucketForOntology(String ontology) {
		final Ontology o = ontologyRepository.findByIdentification(ontology);
		final String userId = o.getUser().getUserId();
		
		return this.objectStorageService.getUserBucketName(userId);
	}
	
	
	
	@Override
	public String getExternalLocationForOntology(String ontology) {
		final Ontology o = ontologyRepository.findByIdentification(ontology);
		final String userId = o.getUser().getUserId();
		
		return "s3a://" + this.objectStorageService.getUserBucketName(userId) + getOntologyPathIntoBucket(ontology);
	}
	
	@Override
	public void uploadMultipartFileForOntology(MultipartFile file, String ontology) throws HistoricalMinioException {
		final String fileSuffix = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		final String fileName = FilenameUtils.getBaseName(file.getOriginalFilename()) + "_" + fileSuffix + "." +
				FilenameUtils.getExtension(file.getOriginalFilename());
		final String bucket = getBucketForOntology(ontology);
		try {
			 minioClient.putObject(
				     PutObjectArgs.builder().bucket(bucket).object(getOntologyPathIntoBucket(ontology) + "/" + fileName).stream(
				             file.getInputStream(), file.getSize(), -1)
				         .build());			
		} catch (InvalidKeyException | ErrorResponseException | IllegalArgumentException | InsufficientDataException
				| InternalException | InvalidResponseException | NoSuchAlgorithmException
				| ServerException | XmlParserException | IOException | InvalidBucketNameException e) {
			log.error("Error creating directory in bucket {} : {} ", bucket, e.getMessage());
			throw new HistoricalMinioException(e.getMessage(), e);
		}
	}
	
	@Override
	public void removeBucketDirectoryAndDataFromOntology(String ontology) throws HistoricalMinioException {
		final String bucket = getBucketForOntology(ontology);
		final String directory = getOntologyPathIntoBucket(ontology);
		
		List<DeleteObject> objectsToDelete = new LinkedList<>();
		final Iterable<Result<Item>> bucketObjectList = minioClient.listObjects(
			    ListObjectsArgs.builder().bucket(bucket).prefix(directory).recursive(true).build());
		
		bucketObjectList.forEach((bo) -> {
			try {
				objectsToDelete.add(new DeleteObject(
						URLDecoder.decode(bo.get().objectName(), StandardCharsets.UTF_8.name())));
			} catch (InvalidKeyException | ErrorResponseException | IllegalArgumentException
					| InsufficientDataException | InternalException
					| InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
					| IOException | InvalidBucketNameException e) {
				log.error("Error creating list to delete objects from bucket {} : {} ", bucket, e.getMessage());
			}
		});
		
		try {
			Iterable<Result<DeleteError>> results = minioClient.removeObjects(
		        RemoveObjectsArgs.builder().bucket(bucket).objects(objectsToDelete).build());
			for (Result<DeleteError> result : results) {
				DeleteError error = result.get();
				log.error("Error deleting object {} : {} ", error.objectName(), error.message());
				throw new HistoricalMinioException("Error deleting object from MinIO:  "+ error.message());
			}
		} catch (InvalidKeyException | ErrorResponseException | IllegalArgumentException | InsufficientDataException
				| InternalException | InvalidResponseException | NoSuchAlgorithmException
				| ServerException | XmlParserException | IOException | InvalidBucketNameException  e) {
			log.error("Error deleting bucket {} and directory {} : {} ", bucket, directory, e.getMessage());
			throw new HistoricalMinioException("Error deleting data from MinIO: " + e.getMessage(), e);
		}
	}
	
	private String getOntologyPathIntoBucket(String ontology) {
		return MinioObjectStorageService.ONTOLOGIES_DIR+"/"+ontology/*.toLowerCase()*/+"/";
	}
	
}
