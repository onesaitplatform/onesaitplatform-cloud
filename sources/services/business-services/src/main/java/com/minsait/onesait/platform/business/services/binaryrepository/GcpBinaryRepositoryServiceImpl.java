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
package com.minsait.onesait.platform.business.services.binaryrepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.minsait.onesait.platform.binaryrepository.exception.BinaryRepositoryException;
import com.minsait.onesait.platform.binaryrepository.model.BinaryFileData;
import com.minsait.onesait.platform.config.model.BinaryFile;
import com.minsait.onesait.platform.config.model.BinaryFile.RepositoryType;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.binaryfile.BinaryFileService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import lombok.extern.slf4j.Slf4j;

@Service("GcpBinaryRepositoryServiceImpl")
@Slf4j
public class GcpBinaryRepositoryServiceImpl implements BinaryRepositoryLogicService {

	@Autowired
	private IntegrationResourcesService resourcesService;

	@Autowired
	private UserService userService;

	@Autowired
	private BinaryFileService binaryFileService;

	@Autowired
	private MultitenancyService multitenancyService;

	private String projectId;
	private String bucketId;
	private Storage storage;

	private static final String SLASH = "/";

	@PostConstruct
	public void init() {
		try {
			projectId = resourcesService.getGlobalConfiguration().getEnv().getFiles().get("gcp-project-id").toString();
			bucketId = resourcesService.getGlobalConfiguration().getEnv().getFiles().get("gcp-bucket-id").toString();
			storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
		} catch (Exception e) {
			log.error("GCP ProjectId and BucketID nor found. {}", e);
		}
	}

	@Override
	public String addBinary(MultipartFile file, String metadata, String filePath)
			throws BinaryRepositoryException, IOException {
		return this.addBinary(file, metadata, RepositoryType.GCP, filePath);

	}

	@Override
	public String addBinary(MultipartFile file, String metadata, RepositoryType repository, String filePath)
			throws BinaryRepositoryException, IOException {
		if (repository == null) {
			repository = BinaryFile.RepositoryType.GCP;
		}
		try {
			User user = userService.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
			Storage.BlobTargetOption precondition = Storage.BlobTargetOption.doesNotExist();

			String fileName;
			if (filePath != null && !filePath.isEmpty())
				fileName = user.getUserId() + SLASH + (filePath.startsWith(SLASH) ? filePath.substring(1) : filePath)
						+ (file.getOriginalFilename().startsWith(SLASH) ? file.getOriginalFilename()
								: SLASH + file.getOriginalFilename());
			else
				fileName = user.getUserId() + (file.getOriginalFilename().startsWith(SLASH) ? file.getOriginalFilename()
						: SLASH + file.getOriginalFilename());

			BlobId blobId = BlobId.of(bucketId, fileName);
			BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
			Blob blob = storage.create(blobInfo, file.getBytes(), precondition);

			log.info("File {} uploaded to bucket {}", file.getName(), bucketId);

			BinaryFile binaryFile = new BinaryFile();
			binaryFile.setFileName(blob.getName());
			binaryFile.setIdentification(file.getOriginalFilename());
			binaryFile.setRepository(repository);
			binaryFile.setMetadata(metadata);
			binaryFile.setMime(file.getContentType());
			binaryFile.setFileExtension(FilenameUtils.getExtension(file.getOriginalFilename()));
			binaryFile.setUser(user);
			binaryFileService.createBinaryFile(binaryFile);
			return binaryFile.getId();
		} catch (Exception e) {
			log.error("Error uploading file to GCP bucket {}.", bucketId, e);
			throw new BinaryRepositoryException(
					"Error uploading file to GCP bucket " + bucketId + ". " + e.getMessage());
		}
	}

	@Override
	public void updateBinary(String fileId, MultipartFile file, String metadata)
			throws BinaryRepositoryException, IOException {
		User user = userService.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
		String fileName = user.getUserId() + SLASH + file.getOriginalFilename();
		if (!binaryFileService.hasUserPermissionWrite(fileId, user)) {
			log.error("User {} does not have permission to update the file {} on GCP bucket {}", user.getUserId(),
					fileName, bucketId);
			throw new BinaryRepositoryException("User " + user.getUserId()
					+ " does not have permission to update the file " + fileName + " on GCP bucket " + bucketId);
		}
		try {
			// changeTenant for file
			final String currentTenat = MultitenancyContextHolder.getTenantName();
			multitenancyService.findUser(user.getUserId())
					.ifPresent(u -> MultitenancyContextHolder.setTenantName(u.getTenant().getName()));

			BlobId blobId = BlobId.of(bucketId, fileName);
			Blob blob = storage.get(blobId);
			if (blob != null) {
				WritableByteChannel channel = blob.writer();
				channel.write(ByteBuffer.wrap(file.getBytes()));
				channel.close();
				log.info("File {} updated on GCP bucket.", fileName);
				binaryFileService.updateBinaryFile(fileId, metadata, file.getContentType(), fileName);
				MultitenancyContextHolder.setTenantName(currentTenat);
			} else {
				log.error("File {} doesn't exist on GCP Bucket {}", fileName, bucketId);
				throw new BinaryRepositoryException("File " + fileName + " cannot be found on GCP bucket " + bucketId);
			}
		} catch (Exception e) {
			log.error("Error updating file to GCP bucket {}.", bucketId, e);
			throw new BinaryRepositoryException(
					"Error updating file to GCP bucket " + bucketId + ". " + e.getMessage());
		}

	}

	@Override
	public void removeBinary(String fileId) throws BinaryRepositoryException {
		User user = userService.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
		if (!binaryFileService.hasUserPermissionWrite(fileId, user)) {
			log.error("User {} does not have permission to remove the file {} on GCP bucket {}", user.getUserId(),
					fileId, bucketId);
			throw new BinaryRepositoryException("User " + user.getUserId()
					+ " does not have permission to remove the file " + fileId + " on GCP bucket " + bucketId);
		}
		try {
			final BinaryFile file = binaryFileService.getFile(fileId);
			// changeTenant for file
			final String currentTenat = MultitenancyContextHolder.getTenantName();
			multitenancyService.findUser(file.getUser().getUserId())
					.ifPresent(u -> MultitenancyContextHolder.setTenantName(u.getTenant().getName()));
			binaryFileService.deleteFile(fileId);

			Boolean isDeleted = storage.delete(bucketId, file.getFileName());
			if (!isDeleted) {
				log.error("The object {} wasn't found in GCP bucket {}", file.getFileName(), bucketId);
				throw new BinaryRepositoryException(
						"The object " + file.getFileName() + " wasn't found in GCP bucket " + bucketId);
			}
			MultitenancyContextHolder.setTenantName(currentTenat);
			log.info("Object {} deleted from GCP bucket {}", file.getFileName(), bucketId);
		} catch (Exception e) {
			log.error("Error deleting file to GCP bucket {}.", bucketId, e);
			throw new BinaryRepositoryException(
					"Error deleting file to GCP bucket " + bucketId + ". " + e.getMessage());

		}

	}

	@Override
	public BinaryFileData getBinaryFile(String fileId) throws IOException, BinaryRepositoryException {
		User user = userService.getUser(SecurityContextHolder.getContext().getAuthentication().getName());

		if (!binaryFileService.hasUserPermissionWrite(fileId, user)) {
			log.error("User {} does not have permission to get the file {} from GCP bucket {}", user.getUserId(),
					fileId, bucketId);
			throw new BinaryRepositoryException("User " + user.getUserId()
					+ " does not have permission to get the file " + fileId + " from GCP bucket " + bucketId);
		}
		try {
			final BinaryFile file = binaryFileService.getFile(fileId);
			// changeTenant for file
			final String currentTenat = MultitenancyContextHolder.getTenantName();
			multitenancyService.findUser(file.getUser().getUserId())
					.ifPresent(u -> MultitenancyContextHolder.setTenantName(u.getTenant().getName()));
			BlobId blobId = BlobId.of(bucketId, file.getFileName());
			byte[] content = storage.readAllBytes(blobId);
			InputStream inputStream = new ByteArrayInputStream(content);
			OutputStream outputStream = new ByteArrayOutputStream();
			IOUtils.copy(inputStream, outputStream);
			outputStream.close();

			BinaryFileData dataFile = BinaryFileData.builder().data(outputStream).build();

			dataFile.setContentType(file.getMime());
			dataFile.setFileName(file.getFileName());
			dataFile.setMetadata(file.getMetadata());
			MultitenancyContextHolder.setTenantName(currentTenat);
			return dataFile;
		} catch (Exception e) {
			log.error("Error getting file from GCP bucket {}.", bucketId, e);
			throw new BinaryRepositoryException(
					"Error getting file from GCP bucket " + bucketId + ". " + e.getMessage());

		}
	}

	@Override
	public void authorizeUser(String fileId, String userId, String accessType) throws BinaryRepositoryException {
		// TODO Auto-generated method stub

	}

	@Override
	public BinaryFileData getBinaryFileWOPermission(String fileId) throws IOException, BinaryRepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String downloadForPagination(String fileId, Long startLine, Long maxLines, Boolean skipHeaders)
			throws IOException, BinaryRepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean closePagination(String fileId) throws IOException, BinaryRepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAuthorization(String fileId, String userId, String accessType) throws BinaryRepositoryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteAuthorization(String fileId, String userId) throws BinaryRepositoryException {
		// TODO Auto-generated method stub

	}

}
