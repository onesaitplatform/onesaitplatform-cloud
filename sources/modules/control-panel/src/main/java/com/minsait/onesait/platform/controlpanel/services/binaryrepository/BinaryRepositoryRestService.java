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
package com.minsait.onesait.platform.controlpanel.services.binaryrepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.net.HttpHeaders;
import com.minsait.onesait.platform.binaryrepository.exception.BinaryRepositoryException;
import com.minsait.onesait.platform.binaryrepository.model.BinaryFileData;
import com.minsait.onesait.platform.business.services.binaryrepository.BinaryFileDTO;
import com.minsait.onesait.platform.business.services.binaryrepository.BinaryFileSimpleDTO;
import com.minsait.onesait.platform.business.services.binaryrepository.factory.BinaryRepositoryServiceFactory;
import com.minsait.onesait.platform.config.model.BinaryFile;
import com.minsait.onesait.platform.config.model.BinaryFile.RepositoryType;
import com.minsait.onesait.platform.config.model.BinaryFileAccess;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.BinaryFileRepository;
import com.minsait.onesait.platform.config.services.binaryfile.BinaryFileService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Binary repository  Management")
@RestController
@RequestMapping("/binary-repository")
@ApiResponses({ @ApiResponse(responseCode = "400", description = "Bad request"),
		@ApiResponse(responseCode = "500", description = "Internal server error"),
		@ApiResponse(responseCode = "403", description = "Forbidden") })
@Slf4j

public class BinaryRepositoryRestService {

	public enum Type {
		MONGO_GRIDFS, FILE, GCP
	}

	@Autowired
	private UserService userService;
	@Autowired
	private BinaryFileRepository binaryFileRepository;
	@Autowired
	private BinaryRepositoryServiceFactory binaryFactory;
	@Autowired
	private IntegrationResourcesService resourcesService;

	@Autowired
	private BinaryFileService binaryFileService;
	@Autowired
	private AppWebUtils webUtils;

	@Value("${onesaitplatform.controlpanel.url:http://localhost:18000/controlpanel}")
	private String basePath;

	private static final String UNAUTHORIZED_USER = "Unauthorized user";
	private static final String FILE_NOT_EXISTS = "File does not exist";
	private static final String USER_NOT_EXISTS = "User does not exist";
	private static final String WRONG_ACCESS_TYPE = "Access type must be %s or %s";
	private static final String AUTH_DELETED = "Authorization deleted successfully";
	private static final String AUTH_UPDATED = "Authorization updated successfully";

	@Operation(summary = "Create File")
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> addBinary(@Parameter @RequestPart("file") MultipartFile file,
			@RequestParam(value = "metadata", required = false) String metadata,
			@RequestParam(value = "repository", required = false) Type repository,
			@RequestParam(value = "filePath", required = false) String filePath) {
		try {
			if (file.getSize() > getMaxSize().longValue()) {
				return new ResponseEntity<>("File is larger than max size allowed", HttpStatus.INTERNAL_SERVER_ERROR);
			}
			RepositoryType type = repository != null ? BinaryFile.RepositoryType.valueOf(repository.name())
					: BinaryFile.RepositoryType.valueOf(RepositoryType.MONGO_GRIDFS.name());
			final String fileId = binaryFactory.getInstance(type).addBinary(file, metadata, type, filePath);
			return new ResponseEntity<>(fileId, HttpStatus.CREATED);
		} catch (final Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Change the file to public")
	@PostMapping("/public")
	@ResponseBody
	public ResponseEntity<String> changePublic(@RequestParam("id") String fileId) {
		if (binaryFileService.isUserOwner(fileId, userService.getUser(webUtils.getUserId()))) {
			binaryFileService.changePublic(fileId);
			return ResponseEntity.ok().build();
		} else {
			return ResponseEntity.badRequest().build();
		}
	}

	@Operation(summary = "Get file data by id")
	@GetMapping("/{id}")
	public ResponseEntity<?> getBinary(@PathVariable("id") String fileId,
			@RequestParam(value = "repository", required = false) Type repository) {
		try {
			RepositoryType type = repository != null ? BinaryFile.RepositoryType.valueOf(repository.name())
					: BinaryFile.RepositoryType.valueOf(RepositoryType.MONGO_GRIDFS.name());
			final BinaryFileData file = binaryFactory.getInstance(type).getBinaryFile(fileId);
			return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(new BinaryFileDTO(file));
		} catch (final BinaryRepositoryException e) {

			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		} catch (final IOException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

	}

	@Operation(summary = "Get all files data")
	@GetMapping("/")
	public ResponseEntity<?> getAll() {
		List<BinaryFileSimpleDTO> binaryFiles;

		final User user = userService.getUser(webUtils.getUserId());
		if (userService.isUserAdministrator(user)) {
			binaryFiles = parseToDTO(binaryFileRepository.findAll(), user);
		} else {
			binaryFiles = parseToDTO(binaryFileRepository.findByUser(user), user);
		}
		return ResponseEntity.ok(binaryFiles);
	}

	private List<BinaryFileSimpleDTO> parseToDTO(List<BinaryFile> binaryFileList, User user) {

		final List<BinaryFileSimpleDTO> binaryFileSimpleList = new ArrayList<>();

		for (final BinaryFile binaryFile : binaryFileList) {
			final BinaryFileSimpleDTO binaryFileSimpleDTO = new BinaryFileSimpleDTO(binaryFile, basePath + "/files/");
			binaryFileSimpleDTO.setOwned(binaryFile.getUser().equals(user));
			binaryFileSimpleList.add(binaryFileSimpleDTO);
		}

		return binaryFileSimpleList;
	}

	@Operation(summary = "Download File by Id")
	@GetMapping("/download/{id}")
	public ResponseEntity<ByteArrayResource> getBinary(@PathVariable("id") String fileId,
			@RequestParam(value = "disposition", required = false) String disposition,
			@RequestParam(value = "repository", required = false) Type repository) {
		try {
			RepositoryType type = repository != null ? BinaryFile.RepositoryType.valueOf(repository.name())
					: BinaryFile.RepositoryType.valueOf(RepositoryType.MONGO_GRIDFS.name());
			final BinaryFileData file = binaryFactory.getInstance(type).getBinaryFile(fileId);
			final ByteArrayResource resource = new ByteArrayResource(
					((ByteArrayOutputStream) file.getData()).toByteArray());

			if (!StringUtils.hasText(disposition)) {
				return ResponseEntity.ok()
						.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=".concat(file.getFileName()))
						.contentLength(resource.contentLength())
						.contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);
			} else {
				return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "inline")
						.contentType(MediaType.parseMediaType(file.getContentType()))
						.contentLength(resource.contentLength()).body(resource);
			}

		} catch (final BinaryRepositoryException e) {

			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		} catch (final IOException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

	}

	@Deprecated
	@PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> updateBinary(@PathVariable("id") String fileId,
			@Parameter @RequestPart("file") MultipartFile file,
			@RequestParam(value = "metadata", required = false) String metadata,
			@RequestParam(value = "repository", required = false) Type repository) throws IOException {
		try {
			RepositoryType type = repository != null ? BinaryFile.RepositoryType.valueOf(repository.name())
					: BinaryFile.RepositoryType.valueOf(RepositoryType.MONGO_GRIDFS.name());
			binaryFactory.getInstance(type).updateBinary(fileId, file, metadata);
			return new ResponseEntity<>(HttpStatus.ACCEPTED);
		} catch (final BinaryRepositoryException e) {

			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		} catch (final IOException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

	}

	@Operation(summary = "Update file by id")
	@PostMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> update(@PathVariable("id") String fileId,
			@Parameter @RequestPart("file") MultipartFile file,
			@RequestParam(value = "metadata", required = false) String metadata,
			@RequestParam(value = "repository", required = false) Type repository) throws IOException {
		try {
			RepositoryType type = repository != null ? BinaryFile.RepositoryType.valueOf(repository.name())
					: BinaryFile.RepositoryType.valueOf(RepositoryType.MONGO_GRIDFS.name());
			binaryFactory.getInstance(type).updateBinary(fileId, file, metadata);
			return new ResponseEntity<>(HttpStatus.ACCEPTED);
		} catch (final BinaryRepositoryException e) {

			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		} catch (final IOException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

	}

	@Operation(summary = "Delete file by id")
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteBinary(@PathVariable("id") String fileId,
			@RequestParam(value = "repository", required = false) Type repository) {
		try {
			RepositoryType type = repository != null ? BinaryFile.RepositoryType.valueOf(repository.name())
					: BinaryFile.RepositoryType.valueOf(RepositoryType.MONGO_GRIDFS.name());
			binaryFactory.getInstance(type).removeBinary(fileId);
			return new ResponseEntity<>(HttpStatus.ACCEPTED);
		} catch (final BinaryRepositoryException e) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

	@Operation(summary = "Delete files that have a date equal to or less than the timestamp entered")
	@DeleteMapping("/before/{timestamp}")
	public ResponseEntity<?> deleteBinary(@PathVariable("timestamp") Long timestamp,
			@RequestParam(value = "repository", required = false) RepositoryType repository) {
		final List<String> ids = binaryFileRepository.getAllIdsBeforeDate(new Date(timestamp));
		ids.forEach(id -> {
			try {
				binaryFactory.getInstance(repository != null ? repository : RepositoryType.MONGO_GRIDFS)
						.removeBinary(id);
			} catch (final BinaryRepositoryException e) {
				log.error("Error deleting file {}", id, e);
			}
		});
		return new ResponseEntity<>(HttpStatus.ACCEPTED);

	}

	@Operation(summary = "Create a file in /tmp/files with the content of the lines entered in the document pagination by entering its id")
	@GetMapping("/{id}/paginate")
	public ResponseEntity<String> paginate(@PathVariable("id") String fileId,
			@RequestParam(value = "startLine", required = true) Long startLine,
			@RequestParam(value = "maxLines", required = true) Long maxLines,
			@RequestParam(value = "skipHeaders", required = true) Boolean skipHeaders,
			@RequestParam(value = "repository", required = false) Type repository) {
		try {
			RepositoryType type = repository != null ? BinaryFile.RepositoryType.valueOf(repository.name())
					: BinaryFile.RepositoryType.valueOf(RepositoryType.MONGO_GRIDFS.name());
			final String file = binaryFactory.getInstance(type).downloadForPagination(fileId, startLine, maxLines,
					skipHeaders);
			return ResponseEntity.ok().body(file);

		} catch (final BinaryRepositoryException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
		} catch (final IOException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
		}

	}

	@Operation(summary = "Delete the file created in /tmp/files with the content of the lines entered in the document pagination by entering its id")
	@GetMapping("/{id}/paginate/close")
	public ResponseEntity<String> closePaginate(@PathVariable("id") String fileId,
			@RequestParam(value = "repository", required = false) Type repository) {
		try {
			RepositoryType type = repository != null ? BinaryFile.RepositoryType.valueOf(repository.name())
					: BinaryFile.RepositoryType.valueOf(RepositoryType.MONGO_GRIDFS.name());
			final boolean isOk = binaryFactory.getInstance(type).closePagination(fileId);
			if (isOk) {
				return ResponseEntity.ok().build();
			}
			return new ResponseEntity<>("Pagination cannot be closed.", HttpStatus.FORBIDDEN);

		} catch (final BinaryRepositoryException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
		} catch (final IOException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
		}

	}

	private Long getMaxSize() {
		return (Long) resourcesService.getGlobalConfiguration().getEnv().getFiles().get("max-size");
	}

	@Operation(summary = "Get file authorizations by Id")
	@GetMapping(value = "/{id}/authorizations")
	public ResponseEntity<?> getAuthorizations(@PathVariable("id") String fileId) {

		if (binaryFileService.getFile(fileId) == null) {
			return new ResponseEntity<>(FILE_NOT_EXISTS, HttpStatus.NOT_FOUND);
		}
		if (!binaryFileService.hasUserPermissionWrite(fileId, userService.getUser(webUtils.getUserId()))) {
			return new ResponseEntity<>(UNAUTHORIZED_USER, HttpStatus.UNAUTHORIZED);
		}
		try {
			final List<BinaryFileAccess> accesses = binaryFileService.getAuthorizations(fileId,
					userService.getUser(webUtils.getUserId()));
			final List<BinaryFileAccessSimplifiedDTO> authorizationsDTO = new ArrayList<>();
			accesses.stream().forEach(a -> authorizationsDTO.add(new BinaryFileAccessSimplifiedDTO(a)));
			return new ResponseEntity<>(authorizationsDTO, HttpStatus.OK);
		} catch (final Exception e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@Operation(summary = "Create file authorizations by Id, adding userId and accessType")
	@PostMapping(value = "/{id}/authorizations")
	public ResponseEntity<?> setAuthorizations(@Parameter(description = "id") @PathVariable(value = "id") String fileId,
			@Valid @RequestBody BinaryFileAccessSimplifiedDTO binaryFileAccess,
			@RequestParam(value = "repository", required = false) RepositoryType repository) {

		if (binaryFileService.getFile(fileId) == null) {
			return new ResponseEntity<>(FILE_NOT_EXISTS, HttpStatus.NOT_FOUND);
		}
		if (!binaryFileService.hasUserPermissionWrite(fileId, userService.getUser(webUtils.getUserId()))) {
			return new ResponseEntity<>(UNAUTHORIZED_USER, HttpStatus.UNAUTHORIZED);
		}
		if (userService.getUser(binaryFileAccess.getUserId()) == null) {
			return new ResponseEntity<>(USER_NOT_EXISTS, HttpStatus.BAD_REQUEST);
		}
		if (!binaryFileAccess.getAccessType().equals(BinaryFileAccess.Type.READ.toString())
				&& !binaryFileAccess.getAccessType().equals(BinaryFileAccess.Type.WRITE.toString())) {
			return new ResponseEntity<>(String.format(WRONG_ACCESS_TYPE, BinaryFileAccess.Type.READ.toString(),
					BinaryFileAccess.Type.WRITE.toString()), HttpStatus.BAD_REQUEST);
		}
		try {
			binaryFactory.getInstance(repository != null ? repository : RepositoryType.MONGO_GRIDFS)
					.setAuthorization(fileId, binaryFileAccess.getUserId(), binaryFileAccess.getAccessType());
		} catch (final BinaryRepositoryException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(AUTH_UPDATED, HttpStatus.OK);
	}

	@Operation(summary = "Delete user authorizations by Id")
	@DeleteMapping(value = "/{id}/authorizations/{userId}")
	public ResponseEntity<?> removeAuthorizations(
			@Parameter(description = "id") @PathVariable(value = "id") String fileId,
			@Parameter(description = "userId") @PathVariable(value = "userId") String userId,
			@RequestParam(value = "repository", required = false) Type repository) {

		if (binaryFileService.getFile(fileId) == null) {
			return new ResponseEntity<>(FILE_NOT_EXISTS, HttpStatus.NOT_FOUND);
		}
		if (!binaryFileService.hasUserPermissionWrite(fileId, userService.getUser(webUtils.getUserId()))) {
			return new ResponseEntity<>(UNAUTHORIZED_USER, HttpStatus.UNAUTHORIZED);
		}
		try {
			RepositoryType type = repository != null ? BinaryFile.RepositoryType.valueOf(repository.name())
					: BinaryFile.RepositoryType.valueOf(RepositoryType.MONGO_GRIDFS.name());
			binaryFactory.getInstance(type).deleteAuthorization(fileId, userId);
		} catch (final BinaryRepositoryException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(AUTH_DELETED, HttpStatus.OK);
	}
}
