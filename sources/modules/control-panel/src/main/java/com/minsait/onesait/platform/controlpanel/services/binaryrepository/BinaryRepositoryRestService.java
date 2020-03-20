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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.net.HttpHeaders;
import com.minsait.onesait.platform.binaryrepository.exception.BinaryRepositoryException;
import com.minsait.onesait.platform.binaryrepository.model.BinaryFileData;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.BinaryFile;
import com.minsait.onesait.platform.config.model.BinaryFile.RepositoryType;
import com.minsait.onesait.platform.config.repository.BinaryFileRepository;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

@RestController
@RequestMapping("/binary-repository")
public class BinaryRepositoryRestService {

	@Autowired
	private AppWebUtils utils;
	@Autowired
	private UserService userService;
	@Autowired
	private BinaryFileRepository binaryFileRepository;
	@Autowired
	private BinaryRepositoryLogicService binaryRepositoryLogicService;
	@Autowired
	private IntegrationResourcesService resourcesService;
	
	@Value("${onesaitplatform.controlpanel.url:http://localhost:18000/controlpanel}")
	private String basePath;

	@PostMapping("")
	public ResponseEntity<?> addBinary(@RequestParam("file") MultipartFile file,
			@RequestParam(value = "metadata", required = false) String metadata,
			@RequestParam(value = "repository", required = false) RepositoryType repository) {
		try {
			if (file.getSize() > getMaxSize().longValue())
				return new ResponseEntity<>("File is larger than max size allowed", HttpStatus.INTERNAL_SERVER_ERROR);
			final String fileId = binaryRepositoryLogicService.addBinary(file, metadata, repository);
			return new ResponseEntity<>(fileId, HttpStatus.CREATED);
		} catch (final Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
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
		User user = userService.getUser(utils.getUserId());
		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name())) {
			binaryFiles = parseToDTO(binaryFileRepository.findAll(), user);
		} else {
			binaryFiles = parseToDTO(binaryFileRepository.findByUser(user), user);
		}
		return ResponseEntity.ok(binaryFiles);
	}

	private List<BinaryFileSimpleDTO> parseToDTO(List<BinaryFile> binaryFileList, User user) {
		
		List<BinaryFileSimpleDTO> binaryFileSimpleList = new ArrayList<>();
		
		for (BinaryFile binaryFile : binaryFileList) {
			BinaryFileSimpleDTO binaryFileSimpleDTO = new BinaryFileSimpleDTO(binaryFile, basePath + "/files/");
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

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteBinary(@PathVariable("id") String fileId) {
		try {
			binaryRepositoryLogicService.removeBinary(fileId);
			return new ResponseEntity<>(HttpStatus.ACCEPTED);
		} catch (final BinaryRepositoryException e) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

	private Long getMaxSize() {
		return (Long) resourcesService.getGlobalConfiguration().getEnv().getFiles().get("max-size");
	}
}
