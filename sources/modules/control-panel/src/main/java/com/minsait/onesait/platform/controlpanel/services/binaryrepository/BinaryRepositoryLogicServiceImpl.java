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
package com.minsait.onesait.platform.controlpanel.services.binaryrepository;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.binaryrepository.exception.BinaryRepositoryException;
import com.minsait.onesait.platform.binaryrepository.factory.BinaryRepositoryFactory;
import com.minsait.onesait.platform.binaryrepository.model.BinaryFileData;
import com.minsait.onesait.platform.config.model.BinaryFile;
import com.minsait.onesait.platform.config.model.BinaryFile.RepositoryType;
import com.minsait.onesait.platform.config.model.BinaryFileAccess;
import com.minsait.onesait.platform.config.services.binaryfile.BinaryFileService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BinaryRepositoryLogicServiceImpl implements BinaryRepositoryLogicService {

	private static final String DONT_HAVE_ACCESS = "You don't have access to this resource";

	@Autowired
	private BinaryRepositoryFactory binaryRepositoryFactory;
	@Autowired
	private UserService userService;
	@Autowired
	private BinaryFileService binaryFileService;
	@Autowired
	private AppWebUtils webUtils;
	@Value("${onesaitplatform.binary-repository.filepath}")
	private String filePath;

	@Override
	public String addBinary(MultipartFile file, String metadata) throws BinaryRepositoryException, IOException {
		return this.addBinary(file, metadata, RepositoryType.MONGO_GRIDFS);
	}

	@Override
	public String addBinary(MultipartFile file, String metadata, RepositoryType repository)
			throws BinaryRepositoryException, IOException {
		if (repository == null)
			repository = BinaryFile.RepositoryType.MONGO_GRIDFS;
		final String randomUUID = UUID.randomUUID().toString();
		final String path = filePath + webUtils.getUserId() + File.separator + randomUUID;
		final String id = binaryRepositoryFactory.getInstance(repository).addBinary(file.getInputStream(), metadata,
				path);
		final BinaryFile binaryFile = new BinaryFile();
		binaryFile.setFileName(file.getOriginalFilename());
		// if file then id is path
		if (repository.equals(RepositoryType.FILE)) {
			binaryFile.setPath(path);
			binaryFile.setId(id);
		} else
			binaryFile.setId(id);
		// Till UI is implemented
		binaryFile.setIdentification(file.getName());
		binaryFile.setRepository(repository);
		binaryFile.setMetadata(metadata);
		binaryFile.setMime(file.getContentType());
		binaryFile.setFileExtension(FilenameUtils.getExtension(file.getOriginalFilename()));
		binaryFile.setUser(userService.getUser(webUtils.getUserId()));
		binaryFileService.createBinaryFile(binaryFile);
		return binaryFile.getId();

	}

	@Override
	public void updateBinary(String fileId, MultipartFile file, String metadata)
			throws BinaryRepositoryException, IOException {
		if (binaryFileService.hasUserPermissionWrite(fileId, userService.getUser(webUtils.getUserId()))) {
			binaryRepositoryFactory.getInstance(binaryFileService.getFile(fileId).getRepository()).updateBinary(fileId,
					file.getInputStream(), metadata);
			binaryFileService.updateBinaryFile(fileId, metadata, file.getContentType(), file.getOriginalFilename());
		} else {
			throw new BinaryRepositoryException(DONT_HAVE_ACCESS);
		}
	}

	@Override
	public void removeBinary(String fileId) throws BinaryRepositoryException {
		if (binaryFileService.hasUserPermissionWrite(fileId, userService.getUser(webUtils.getUserId()))) {
			try {
				final BinaryFile file = binaryFileService.getFile(fileId);
				binaryFileService.deleteFile(fileId);
				binaryRepositoryFactory.getInstance(file.getRepository()).removeBinary(fileId);
			} catch (final BinaryRepositoryException e) {
				throw e;
			} catch (final Exception e) {
				log.error("Binary file may be associated to a report, please remove from report");
				throw new BinaryRepositoryException(
						"Binary file may be associated to a report, please remove from report");
			}
		} else {
			throw new BinaryRepositoryException(DONT_HAVE_ACCESS);
		}
	}

	@Override
	public BinaryFileData getBinaryFile(String fileId) throws IOException, BinaryRepositoryException {
		if (binaryFileService.hasUserPermissionRead(fileId, userService.getUser(webUtils.getUserId()))) {
			final BinaryFile file = binaryFileService.getFile(fileId);
			final BinaryFileData dataFile = binaryRepositoryFactory
					.getInstance(binaryFileService.getFile(fileId).getRepository()).getBinaryFile(fileId);
			dataFile.setContentType(file.getMime());
			dataFile.setFileName(file.getFileName());
			dataFile.setMetadata(file.getMetadata());
			return dataFile;
		} else {
			throw new BinaryRepositoryException(DONT_HAVE_ACCESS);
		}
	}

	@Override
	public void authorizeUser(String fileId, String userId, String accessType) throws BinaryRepositoryException {
		if (binaryFileService.hasUserPermissionWrite(fileId, userService.getUser(webUtils.getUserId())))
			binaryFileService.authorizeUser(fileId, BinaryFileAccess.Type.valueOf(accessType),
					userService.getUser(userId));
		else
			throw new BinaryRepositoryException(DONT_HAVE_ACCESS);
	}

}
