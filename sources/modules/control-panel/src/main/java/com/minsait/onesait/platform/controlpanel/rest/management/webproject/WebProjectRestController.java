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
package com.minsait.onesait.platform.controlpanel.rest.management.webproject;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.config.services.exceptions.WebProjectServiceException;
import com.minsait.onesait.platform.config.services.webproject.WebProjectDTO;
import com.minsait.onesait.platform.config.services.webproject.WebProjectService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;



@RestController
@RequestMapping("api/webprojects")
@Tag(name = "Web Projects")
@ApiResponses({ @ApiResponse(responseCode = "400", description = "Bad request"),
	@ApiResponse(responseCode = "500", description = "Internal server error"), @ApiResponse(responseCode = "403", description = "Forbidden") })
public class WebProjectRestController {

	@Autowired
	private WebProjectService webProjectService;
	@Autowired
	private AppWebUtils utils;

	@Operation(summary = "Create a web project")
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<String> create(@RequestParam(required = true, value = "name") String name,
			@RequestParam(required = true, value = "description") String description,
			@RequestParam(required = false, value = "mainFileName", defaultValue = "index.html") String mainFileName,
			@RequestPart("zip") MultipartFile zip) {
		try {
			if (!name.matches(AppWebUtils.IDENTIFICATION_PATERN)) {
				return new ResponseEntity<>("Identification Error: Use alphanumeric characters and '-', '_'",
						HttpStatus.BAD_REQUEST);
			}

			final WebProjectDTO webProject = WebProjectDTO.builder().zip(zip).identification(name)
					.mainFile(mainFileName).description(description).build();
			validateDTO(webProject);
			webProjectService.uploadZip(zip, utils.getUserId());
			webProjectService.createWebProject(webProject, utils.getUserId());
		} catch (final WebProjectServiceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "Update content of a web project")
	@PatchMapping(value="{name}/zip",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<String> patchZip(@RequestPart("zip") MultipartFile zip, @PathVariable("name") String name) {
		final WebProjectDTO wp = webProjectService.getWebProjectByName(name, utils.getUserId());
		if (wp == null) {
			return ResponseEntity.notFound().build();
		}
		if (zip.isEmpty()) {
			return ResponseEntity.badRequest().body("Empty zip file");
		}
		try {

			webProjectService.uploadZip(zip, utils.getUserId());
			webProjectService.updateWebProject(wp, utils.getUserId());
		} catch (final WebProjectServiceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return ResponseEntity.ok().build();
	}

	@Deprecated
	@Operation(summary = "Update web project")
	@PutMapping(value="{name}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<String> update(@PathVariable("name") String name,
			@RequestParam(required = true, value = "description") String description,
			@RequestParam(required = false, value = "mainFileName", defaultValue = "index.html") String mainFileName,
			@RequestPart("zip") MultipartFile zip) {
		try {
			final WebProjectDTO webProject = WebProjectDTO.builder().zip(zip).identification(name)
					.mainFile(mainFileName).description(description).build();
			validateDTO(webProject);
			webProjectService.uploadZip(zip, utils.getUserId());
			webProjectService.updateWebProject(webProject, utils.getUserId());
		} catch (final WebProjectServiceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "Update web project")
	@PostMapping(value="{name}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<String> updateWebProject(@PathVariable("name") String name,
			@RequestParam(required = true, value = "description") String description,
			@RequestParam(required = false, value = "mainFileName", defaultValue = "index.html") String mainFileName,
			@RequestPart("zip") MultipartFile zip) {
		try {
			final WebProjectDTO webProject = WebProjectDTO.builder().zip(zip).identification(name)
					.mainFile(mainFileName).description(description).build();
			validateDTO(webProject);
			webProjectService.uploadZip(zip, utils.getUserId());
			webProjectService.updateWebProject(webProject, utils.getUserId());
		} catch (final WebProjectServiceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "Delete web project")
	@DeleteMapping("/{name}")
	public ResponseEntity<String> delete(@PathVariable("name") String name) {
		final WebProjectDTO wp = webProjectService.getWebProjectByName(name, utils.getUserId());
		if (wp == null) {
			return ResponseEntity.notFound().build();
		}
		try {
			webProjectService.deleteWebProject(wp.getId(), utils.getUserId());
			return ResponseEntity.ok().build();
		} catch (final Exception e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@Operation(summary = "List all web projects")
	@GetMapping
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=WebProjectDTO[].class)), responseCode = "200", description = "OK"))
	public ResponseEntity<List<WebProjectDTO>> list(@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "description", required = false) String description) {
		final List<WebProjectDTO> webprojects = webProjectService
				.getWebProjectsWithDescriptionAndIdentification(utils.getUserId(), name, description);
		return ResponseEntity.ok().body(webprojects);
	}

	@Operation(summary = "Find project by its name")
	@GetMapping("/{name}")
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=WebProjectDTO.class)), responseCode = "200", description = "OK"))
	public ResponseEntity<WebProjectDTO> find(@PathVariable("name") String name) {
		final WebProjectDTO wp = webProjectService.getWebProjectByName(name, utils.getUserId());
		if (wp == null) {
			return ResponseEntity.notFound().build();
		} else {
			return ResponseEntity.ok().body(wp);
		}
	}

	private void validateDTO(WebProjectDTO wp) {

		Optional<String> message = Optional.empty();

		if (StringUtils.isEmpty(wp.getIdentification())) {
			message = Optional.of("Identification must be provided");
		}
		if (wp.getZip() == null || wp.getZip().isEmpty()) {
			message = Optional.of("Zip File must be provided");
		}
		if (utils.isFileExtensionForbidden(wp.getZip())) {
			message = Optional.of("File must be ZIP");
		}
		if (wp.getZip().getSize() > utils.getMaxFileSizeAllowed()) {
			message = Optional.of("ZIP File too large");
		}

		if (message.isPresent()) {
			throw new WebProjectServiceException(message.get());
		}
	}
}
