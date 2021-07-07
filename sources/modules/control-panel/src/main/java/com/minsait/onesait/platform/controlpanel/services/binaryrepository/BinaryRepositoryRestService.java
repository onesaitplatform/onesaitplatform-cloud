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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.net.HttpHeaders;
import com.minsait.onesait.platform.binaryrepository.exception.BinaryRepositoryException;
import com.minsait.onesait.platform.binaryrepository.model.BinaryFileData;
import com.minsait.onesait.platform.business.services.binaryrepository.BinaryFileDTO;
import com.minsait.onesait.platform.business.services.binaryrepository.BinaryFileSimpleDTO;
import com.minsait.onesait.platform.business.services.binaryrepository.BinaryRepositoryLogicService;
import com.minsait.onesait.platform.config.model.BinaryFile;
import com.minsait.onesait.platform.config.model.BinaryFileAccess;
import com.minsait.onesait.platform.config.model.BinaryFile.RepositoryType;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.BinaryFileRepository;
import com.minsait.onesait.platform.config.services.binaryfile.BinaryFileService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "Binary repository  Management", tags = { "Binary repository management service" })
@RestController
@RequestMapping("/binary-repository")
@ApiResponses({ @ApiResponse(code = 400, message = "Bad request"),
		@ApiResponse(code = 500, message = "Internal server error"), @ApiResponse(code = 403, message = "Forbidden") })
public class BinaryRepositoryRestService {

	@Autowired
	private UserService userService;
	@Autowired
	private BinaryFileRepository binaryFileRepository;
	@Autowired
	private BinaryRepositoryLogicService binaryRepositoryLogicService;
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

	@PostMapping("")
	public ResponseEntity<?> addBinary(@RequestParam("file") MultipartFile file,
			@RequestParam(value = "metadata", required = false) String metadata,
			@RequestParam(value = "repository", required = false) RepositoryType repository) {
		try {
			if (file.getSize() > getMaxSize().longValue()) {
				return new ResponseEntity<>("File is larger than max size allowed", HttpStatus.INTERNAL_SERVER_ERROR);
			}
			final String fileId = binaryRepositoryLogicService.addBinary(file, metadata, repository);
			return new ResponseEntity<>(fileId, HttpStatus.CREATED);
		} catch (final Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

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

	@GetMapping("/{id}")
	public ResponseEntity<?> getBinary(@PathVariable("id") String fileId) {
		try {
			final BinaryFileData file = binaryRepositoryLogicService.getBinaryFile(fileId);
			return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(new BinaryFileDTO(file));
		} catch (final BinaryRepositoryException e) {

			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		} catch (final IOException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

	}

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

	@GetMapping("/download/{id}")
	public ResponseEntity<ByteArrayResource> getBinary(@PathVariable("id") String fileId,
			@RequestParam(value = "disposition", required = false) String disposition) {
		try {

			final BinaryFileData file = binaryRepositoryLogicService.getBinaryFile(fileId);
			final ByteArrayResource resource = new ByteArrayResource(
					((ByteArrayOutputStream) file.getData()).toByteArray());

			if (StringUtils.isEmpty(disposition)) {
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
	@PutMapping("/{id}")
	public ResponseEntity<?> updateBinary(@PathVariable("id") String fileId, @RequestParam("file") MultipartFile file,
			@RequestParam(value = "metadata", required = false) String metadata) throws IOException {
		try {
			binaryRepositoryLogicService.updateBinary(fileId, file, metadata);
			return new ResponseEntity<>(HttpStatus.ACCEPTED);
		} catch (final BinaryRepositoryException e) {

			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		} catch (final IOException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

	}

	@PostMapping("/{id}")
	public ResponseEntity<?> update(@PathVariable("id") String fileId, @RequestParam("file") MultipartFile file,
			@RequestParam(value = "metadata", required = false) String metadata) throws IOException {
		try {
			binaryRepositoryLogicService.updateBinary(fileId, file, metadata);
			return new ResponseEntity<>(HttpStatus.ACCEPTED);
		} catch (final BinaryRepositoryException e) {

			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		} catch (final IOException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteBinary(@PathVariable("id") String fileId) {
		try {
			binaryRepositoryLogicService.removeBinary(fileId);
			return new ResponseEntity<>(HttpStatus.ACCEPTED);
		} catch (final BinaryRepositoryException e) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

	@GetMapping("/{id}/paginate")
	public ResponseEntity<String> paginate(@PathVariable("id") String fileId,
			@RequestParam(value = "startLine", required = true) Long startLine,
			@RequestParam(value = "maxLines", required = true) Long maxLines,
			@RequestParam(value = "skipHeaders", required = true) Boolean skipHeaders) {
		try {
			final String file = binaryRepositoryLogicService.downloadForPagination(fileId, startLine, maxLines,
					skipHeaders);
			return ResponseEntity.ok().body(file);

		} catch (final BinaryRepositoryException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
		} catch (final IOException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
		}

	}

	@GetMapping("/{id}/paginate/close")
	public ResponseEntity<String> closePaginate(@PathVariable("id") String fileId) {
		try {
			final boolean isOk = binaryRepositoryLogicService.closePagination(fileId);
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
	
	@GetMapping(value = "/{id}/authorizations")
	public ResponseEntity<?> getAuthorizations(@PathVariable("id") String fileId) {
		
		if (binaryFileService.getFile(fileId) == null) {
			return new ResponseEntity<>(FILE_NOT_EXISTS, HttpStatus.NOT_FOUND);
		}
		if(!binaryFileService.hasUserPermissionWrite(fileId, userService.getUser(webUtils.getUserId()))) {
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

	@PostMapping(value = "/{id}/authorizations")
	public ResponseEntity<?> setAuthorizations(
			@ApiParam(value = "id") @PathVariable(value = "id") String fileId,
			@Valid @RequestBody BinaryFileAccessSimplifiedDTO binaryFileAccess) {
	
		if (binaryFileService.getFile(fileId) == null) {
			return new ResponseEntity<>(FILE_NOT_EXISTS, HttpStatus.NOT_FOUND);
		}
		if (!binaryFileService.hasUserPermissionWrite(fileId, userService.getUser(webUtils.getUserId()))) {
			return new ResponseEntity<>(UNAUTHORIZED_USER, HttpStatus.UNAUTHORIZED);
		}
		if (userService.getUser(binaryFileAccess.getUserId()) == null) {
			return new ResponseEntity<>(USER_NOT_EXISTS, HttpStatus.BAD_REQUEST);
		}
		if (!binaryFileAccess.getAccessType().equals(BinaryFileAccess.Type.READ.toString()) && 
				!binaryFileAccess.getAccessType().equals(BinaryFileAccess.Type.WRITE.toString())) {
			return new ResponseEntity<>(String.format(WRONG_ACCESS_TYPE, BinaryFileAccess.Type.READ.toString(), BinaryFileAccess.Type.WRITE.toString()), 
					HttpStatus.BAD_REQUEST);
		}
		try {
			binaryRepositoryLogicService.setAuthorization(fileId, binaryFileAccess.getUserId(),  binaryFileAccess.getAccessType());
		}
		catch (final BinaryRepositoryException e) {				
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(AUTH_UPDATED, HttpStatus.OK);
	}

	@DeleteMapping(value = "/{id}/authorizations/{userId}")
	public ResponseEntity<?> removeAuthorizations(
			@ApiParam(value = "id") @PathVariable(value = "id") String fileId,
			@ApiParam(value = "userId") @PathVariable(value = "userId") String userId) {

		if (binaryFileService.getFile(fileId) == null) {
			return new ResponseEntity<>(FILE_NOT_EXISTS, HttpStatus.NOT_FOUND);
		}
		if(!binaryFileService.hasUserPermissionWrite(fileId, userService.getUser(webUtils.getUserId()))) {
			return new ResponseEntity<>(UNAUTHORIZED_USER, HttpStatus.UNAUTHORIZED);
		}
		try {
			binaryRepositoryLogicService.deleteAuthorization(fileId, userId);
		}
		catch (final BinaryRepositoryException e) {				
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(AUTH_DELETED,HttpStatus.OK);
	}
}
