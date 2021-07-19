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
package com.minsait.onesait.platform.business.services.binaryrepository;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.binaryrepository.exception.BinaryRepositoryException;
import com.minsait.onesait.platform.binaryrepository.factory.BinaryRepositoryFactory;
import com.minsait.onesait.platform.binaryrepository.model.BinaryFileData;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.BinaryFile;
import com.minsait.onesait.platform.config.model.BinaryFile.RepositoryType;
import com.minsait.onesait.platform.config.model.BinaryFileAccess;
import com.minsait.onesait.platform.config.services.binaryfile.BinaryFileService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BinaryRepositoryLogicServiceImpl implements BinaryRepositoryLogicService {

	private static final String DONT_HAVE_ACCESS = "You don't have access to this resource";
	private static final String FORBIDDEN = "This file cannot be paginated";

	@Autowired
	private BinaryRepositoryFactory binaryRepositoryFactory;
	@Autowired
	private UserService userService;
	@Autowired
	private BinaryFileService binaryFileService;
	@Autowired
	private MultitenancyService multitenancyService;

	@Value("${onesaitplatform.binary-repository.filepath:/usr/local/files/}")
	private String filePath;

	@Override
	public String addBinary(MultipartFile file, String metadata) throws BinaryRepositoryException, IOException {
		return this.addBinary(file, metadata, RepositoryType.MONGO_GRIDFS);
	}

	@Override
	public String addBinary(MultipartFile file, String metadata, RepositoryType repository)
			throws BinaryRepositoryException, IOException {
		if (repository == null) {
			repository = BinaryFile.RepositoryType.MONGO_GRIDFS;
		}
		final String randomUUID = UUID.randomUUID().toString();
		final String path = filePath + SecurityContextHolder.getContext().getAuthentication().getName() + File.separator
				+ randomUUID;
		final String id = binaryRepositoryFactory.getInstance(repository).addBinary(file.getInputStream(), metadata,
				path);
		final BinaryFile binaryFile = new BinaryFile();
		binaryFile.setFileName(file.getOriginalFilename());
		// if file then id is path
		if (repository.equals(RepositoryType.FILE)) {
			binaryFile.setPath(path);
			binaryFile.setId(id);
		} else {
			binaryFile.setId(id);
		}
		// Till UI is implemented
		binaryFile.setIdentification(file.getOriginalFilename());
		binaryFile.setRepository(repository);
		binaryFile.setMetadata(metadata);
		binaryFile.setMime(file.getContentType());
		binaryFile.setFileExtension(FilenameUtils.getExtension(file.getOriginalFilename()));
		binaryFile.setUser(userService.getUser(SecurityContextHolder.getContext().getAuthentication().getName()));
		binaryFileService.createBinaryFile(binaryFile);
		return binaryFile.getId();

	}

	@Override
	public void updateBinary(String fileId, MultipartFile file, String metadata)
			throws BinaryRepositoryException, IOException {
		if (binaryFileService.hasUserPermissionWrite(fileId,
				userService.getUser(SecurityContextHolder.getContext().getAuthentication().getName()))) {
			final BinaryFile bFile = binaryFileService.getFile(fileId);
			// changeTenant for file
			final String currentTenat = MultitenancyContextHolder.getTenantName();
			multitenancyService.findUser(bFile.getUser().getUserId())
			.ifPresent(u -> MultitenancyContextHolder.setTenantName(u.getTenant().getName()));
			binaryRepositoryFactory.getInstance(binaryFileService.getFile(fileId).getRepository()).updateBinary(fileId,
					file.getInputStream(), metadata);
			binaryFileService.updateBinaryFile(fileId, metadata, file.getContentType(), file.getOriginalFilename());
			MultitenancyContextHolder.setTenantName(currentTenat);
		} else {
			throw new BinaryRepositoryException(DONT_HAVE_ACCESS);
		}
	}

	@Override
	public void removeBinary(String fileId) throws BinaryRepositoryException {
		if (binaryFileService.hasUserPermissionWrite(fileId,
				userService.getUser(SecurityContextHolder.getContext().getAuthentication().getName()))) {
			try {
				final BinaryFile file = binaryFileService.getFile(fileId);
				// changeTenant for file
				final String currentTenat = MultitenancyContextHolder.getTenantName();
				multitenancyService.findUser(file.getUser().getUserId())
				.ifPresent(u -> MultitenancyContextHolder.setTenantName(u.getTenant().getName()));
				binaryFileService.deleteFile(fileId);
				binaryRepositoryFactory.getInstance(file.getRepository()).removeBinary(fileId);
				MultitenancyContextHolder.setTenantName(currentTenat);
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
		if (binaryFileService.hasUserPermissionRead(fileId,
				userService.getUser(SecurityContextHolder.getContext().getAuthentication().getName()))) {

			final BinaryFile file = binaryFileService.getFile(fileId);
			// changeTenant for file
			final String currentTenat = MultitenancyContextHolder.getTenantName();
			multitenancyService.findUser(file.getUser().getUserId())
			.ifPresent(u -> MultitenancyContextHolder.setTenantName(u.getTenant().getName()));
			final BinaryFileData dataFile = binaryRepositoryFactory
					.getInstance(binaryFileService.getFile(fileId).getRepository()).getBinaryFile(fileId);
			dataFile.setContentType(file.getMime());
			dataFile.setFileName(file.getFileName());
			dataFile.setMetadata(file.getMetadata());
			MultitenancyContextHolder.setTenantName(currentTenat);
			return dataFile;
		} else {
			throw new BinaryRepositoryException(DONT_HAVE_ACCESS);
		}
	}

	@Override
	public String downloadForPagination(String fileId, Long startLine, Long maxLines, Boolean skipHeaders)
			throws IOException, BinaryRepositoryException {
		if (binaryFileService.hasUserPermissionRead(fileId,
				userService.getUser(SecurityContextHolder.getContext().getAuthentication().getName()))) {

			final BinaryFile file = binaryFileService.getFile(fileId);
			if (checkMimeType(file.getMime())) {
				// changeTenant for file
				final String currentTenat = MultitenancyContextHolder.getTenantName();
				multitenancyService.findUser(file.getUser().getUserId())
				.ifPresent(u -> MultitenancyContextHolder.setTenantName(u.getTenant().getName()));
				final String dataFile = binaryRepositoryFactory.getInstance(binaryFileService.getFile(fileId).getRepository())
						.getBinaryFileForPaginate(fileId, startLine, maxLines, skipHeaders);
				MultitenancyContextHolder.setTenantName(currentTenat);
				return dataFile;
			} else {
				throw new BinaryRepositoryException(FORBIDDEN);
			}
		} else {
			throw new BinaryRepositoryException(DONT_HAVE_ACCESS);
		}
	}

	@Override
	public Boolean closePagination(String fileId) throws IOException, BinaryRepositoryException {
		if (binaryFileService.hasUserPermissionRead(fileId,
				userService.getUser(SecurityContextHolder.getContext().getAuthentication().getName()))) {

			final BinaryFile file = binaryFileService.getFile(fileId);

			// changeTenant for file
			final String currentTenat = MultitenancyContextHolder.getTenantName();
			multitenancyService.findUser(file.getUser().getUserId())
			.ifPresent(u -> MultitenancyContextHolder.setTenantName(u.getTenant().getName()));
			final Boolean isOk = binaryRepositoryFactory.getInstance(binaryFileService.getFile(fileId).getRepository())
					.closePaginate(fileId);
			MultitenancyContextHolder.setTenantName(currentTenat);
			return isOk;

		} else {
			throw new BinaryRepositoryException(DONT_HAVE_ACCESS);
		}
	}

	@Override
	public BinaryFileData getBinaryFileWOPermission(String fileId) throws IOException, BinaryRepositoryException {
		final BinaryFile file = binaryFileService.getFile(fileId);
		// changeTenant for file
		final String currentTenat = MultitenancyContextHolder.getTenantName();
		multitenancyService.findUser(file.getUser().getUserId())
		.ifPresent(u -> MultitenancyContextHolder.setTenantName(u.getTenant().getName()));
		final BinaryFileData dataFile = binaryRepositoryFactory
				.getInstance(binaryFileService.getFile(fileId).getRepository()).getBinaryFile(fileId);
		dataFile.setContentType(file.getMime());
		dataFile.setFileName(file.getFileName());
		dataFile.setMetadata(file.getMetadata());
		MultitenancyContextHolder.setTenantName(currentTenat);
		return dataFile;

	}

	@Override
	public void authorizeUser(String fileId, String userId, String accessType) throws BinaryRepositoryException {
		if (binaryFileService.hasUserPermissionWrite(fileId,
				userService.getUser(SecurityContextHolder.getContext().getAuthentication().getName()))) {
			binaryFileService.authorizeUser(fileId, BinaryFileAccess.Type.valueOf(accessType),
					userService.getUser(userId));
		} else {
			throw new BinaryRepositoryException(DONT_HAVE_ACCESS);
		}
	}

	private Boolean checkMimeType(String type) {
		if (type.equals("text/plain") || type.equals("text/css") || type.equals("application/msword")
				|| type.equals("text/html") || type.equals("text/javascript") || type.equals("application/json")
				|| type.equals("application/javascript")
				|| type.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
				|| type.equals("text/csv") || type.equals("application/vnd.ms-excel")
				|| type.equals("application/vnd.oasis.opendocument.spreadsheet")
				|| type.equals("application/vnd.oasis.opendocument.text")) {
			return true;
		}
		return false;
	}

	@Override
	public void setAuthorization(String fileId, String userId, String accessType) throws BinaryRepositoryException {
		if (binaryFileService.hasUserPermissionWrite(fileId,
				userService.getUser(SecurityContextHolder.getContext().getAuthentication().getName()))) {
			binaryFileService.setAuthorization(fileId, BinaryFileAccess.Type.valueOf(accessType),
					userService.getUser(userId));
		} else {
			throw new BinaryRepositoryException(DONT_HAVE_ACCESS);
		}
	}

	@Override
	public void deleteAuthorization(String fileId, String userId) throws BinaryRepositoryException {
		try {
			if (binaryFileService.hasUserPermissionWrite(fileId,
					userService.getUser(SecurityContextHolder.getContext().getAuthentication().getName()))) {
				binaryFileService.deleteAuthorization(fileId, userService.getUser(userId));

			} else {
				throw new BinaryRepositoryException(DONT_HAVE_ACCESS);
			}
		} catch (final GenericOPException e) {
			throw new BinaryRepositoryException(e.getMessage());
		}
	}


}
