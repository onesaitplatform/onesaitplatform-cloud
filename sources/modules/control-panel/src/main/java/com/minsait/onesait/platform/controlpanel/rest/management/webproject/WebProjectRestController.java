/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.config.services.exceptions.WebProjectServiceException;
import com.minsait.onesait.platform.config.services.webproject.NPMCommandResult.NPMCommandResultStatus;
import com.minsait.onesait.platform.config.services.webproject.WebProjectDTO;
import com.minsait.onesait.platform.config.services.webproject.WebProjectService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("api/webprojects")
@Tag(name = "Web Projects")
@ApiResponses({ @ApiResponse(responseCode = "400", description = "Bad request"),
		@ApiResponse(responseCode = "500", description = "Internal server error"),
		@ApiResponse(responseCode = "403", description = "Forbidden") })
public class WebProjectRestController {

	@Autowired
	private WebProjectService webProjectService;

	@Value("${onesaitplatform.gitlab.manager.server}")
	private String gitlabManagerServer;
	@Value("${onesaitplatform.gitlab.manager.server.internal:http://localhost:10050/gitlab/api/v1}")
	private String gitManagerUrlInternal;

	@Value("${digitaltwin.temp.dir:/tmp}")
	private String tmpDirectory;

	@Value("${onesaitplatform.gitlab.maxlimitsecondscompilenpmbloqued:600}")
	private int maxLimitSecondsCompileNpmBloqued;

	@Autowired
	private AppWebUtils utils;

	@Operation(summary = "Create a web project")
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<String> create(@RequestParam(required = true, value = "name") String name,
			@RequestParam(required = true, value = "description") String description,
			@RequestParam(value = "mainFileName", defaultValue = "index.html") String mainFileName,
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

	@Operation(summary = "Create a git web project")
	@PostMapping(value = "create/{name}/git")
	public ResponseEntity<String> createGit(@PathVariable(required = true, value = "name") String name,
			@RequestParam(required = true, value = "description") String description,
			@RequestParam(value = "mainFileName", defaultValue = "") String mainFileName,
			@RequestParam(value = "gitToken") String gitToken, @RequestParam(value = "gitUrl") String gitUrl,
			@RequestParam(value = "npm") boolean npm,
			@RequestParam(value = "targetDirectory", required = false) String targetDirectory,
			@RequestParam(value = "runCommand", required = false) String runCommand) {
		try {
			if (!name.matches(AppWebUtils.IDENTIFICATION_PATERN)) {
				return new ResponseEntity<>("Identification Error: Use alphanumeric characters and '-', '_'",
						HttpStatus.BAD_REQUEST);
			}

			final WebProjectDTO webProject = WebProjectDTO.builder().identification(name).mainFile(mainFileName)
					.description(description).npm(npm).gitToken(gitToken).gitUrl(gitUrl).runCommand(runCommand)
					.targetDirectory(targetDirectory).build();
			validateDTOGit(webProject);

			webProjectService.createWebProject(webProject, utils.getUserId());
		} catch (final WebProjectServiceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "Update content of a web project")
	@PatchMapping(value = "{name}/zip", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
	@PutMapping(value = "{name}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<String> update(@PathVariable("name") String name,
			@RequestParam(required = true, value = "description") String description,
			@RequestParam(required = false, value = "mainFileName", defaultValue = "index.html") String mainFileName,
			@RequestParam(required = false, value = "zip") MultipartFile zip,
			@RequestParam(required = false, value = "gitUrl") String gitUrl,
			@RequestParam(required = false, value = "gitToken") String gitToken) {
		try {
			final WebProjectDTO webProject = WebProjectDTO.builder().zip(zip).identification(name)
					.mainFile(mainFileName).description(description).build();
			validateDTO(webProject);
			if (zip != null && !zip.isEmpty()) {
				webProjectService.uploadZip(zip, utils.getUserId());
			}
			webProjectService.updateWebProject(webProject, utils.getUserId());
		} catch (final WebProjectServiceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "Update web project")
	@PostMapping(value = "{name}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<String> updateWebProject(@PathVariable("name") String name,
			@RequestParam(required = true, value = "description") String description,
			@RequestParam(value = "mainFileName", defaultValue = "index.html") String mainFileName,
			@RequestPart(value = "zip") MultipartFile zip) {
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
	@PostMapping(value = "update/{name}/git")
	public ResponseEntity<String> updateWebProjectGit(@PathVariable("name") String name,
			@RequestParam(required = true, value = "description") String description,
			@RequestParam(required = false, value = "mainFileName", defaultValue = "") String mainFileName,
			@RequestParam(value = "gitToken") String gitToken, @RequestParam(value = "gitUrl") String gitUrl,
			@RequestParam(value = "npm") boolean npm,
			@RequestParam(value = "targetDirectory", required = false) String targetDirectory,
			@RequestParam(value = "runCommand", required = false) String runCommand) {
		try {
			final WebProjectDTO webProject = WebProjectDTO.builder().identification(name).mainFile(mainFileName)
					.description(description).npm(npm).gitToken(gitToken).gitUrl(gitUrl).runCommand(runCommand)
					.targetDirectory(targetDirectory).build();
			validateDTOGit(webProject);
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
	@ApiResponses(@ApiResponse(content = @Content(schema = @Schema(implementation = WebProjectDTO[].class)), responseCode = "200", description = "OK"))
	public ResponseEntity<List<WebProjectDTO>> list(@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "description", required = false) String description) {
		final List<WebProjectDTO> webprojects = webProjectService
				.getWebProjectsWithDescriptionAndIdentification(utils.getUserId(), name, description);
		return ResponseEntity.ok().body(webprojects);
	}

	@Operation(summary = "Find project by its name")
	@GetMapping("/{name}")
	@ApiResponses(@ApiResponse(content = @Content(schema = @Schema(implementation = WebProjectDTO.class)), responseCode = "200", description = "OK"))
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

		if (!StringUtils.hasText(wp.getIdentification())) {
			message = Optional.of("Identification must be provided");
		}

		if (wp.getZip() == null || wp.getZip().isEmpty()) {
			message = Optional.of("Some File access needs to be provided");
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

	private void validateDTOGit(WebProjectDTO wp) {

		Optional<String> message = Optional.empty();

		if (!StringUtils.hasText(wp.getIdentification())) {
			message = Optional.of("Identification must be provided");
		}

		if (!StringUtils.hasText(wp.getDescription())) {
			message = Optional.of("Description must be provided");
		}

		if (!StringUtils.hasText(wp.getDescription())) {
			message = Optional.of("NPM must be provided");
		}

		if (!StringUtils.hasText(wp.getGitToken())) {
			message = Optional.of("Git Details must be provided");
		}

		if (!StringUtils.hasText(wp.getGitUrl())) {
			message = Optional.of("Git Details must be provided");
		}

		if (StringUtils.hasText(wp.getTargetDirectory()) && wp.getNpm() == false) {
			message = Optional.of("Target directory is not needed if the project is not npm");
		}

		if (StringUtils.hasText(wp.getRunCommand()) && wp.getNpm() == false) {
			message = Optional.of("Run command is not needed if the project is not npm");
		}

		if (!StringUtils.hasText(wp.getTargetDirectory()) && wp.getNpm() == true) {
			message = Optional.of("Target directory is needed if the project is npm");
		}

		if (!StringUtils.hasText(wp.getRunCommand()) && wp.getNpm() == true) {
			message = Optional.of("Run command is needed if the project is npm");
		}

		if ((!StringUtils.hasText(wp.getMainFile()) || wp.getMainFile().equals("")) && wp.getNpm() == false) {
			message = Optional.of("Main File can't be empty if the project is not NPM");
		}

		if (message.isPresent()) {
			throw new WebProjectServiceException(message.get());
		}
	}

	@Operation(summary = "Deploy project")
	@PostMapping("deploy/{name}")
	@ApiResponses(@ApiResponse(content = @Content(schema = @Schema(implementation = WebProjectDTO.class)), responseCode = "200", description = "OK"))
	public ResponseEntity<String> deploy(@PathVariable("name") String name, @RequestParam(value = "path") String path,
			@RequestParam(value = "branch") String branch) {
		WebProjectDTO webProject = webProjectService.getWebProjectByName(name, utils.getUserId());
		if (webProject != null) {
			if (webProject.getNpm()) {
				if (webProjectService.isNpmInstall()) {
					return new ResponseEntity<String>("An NPM install is already happening, please try again later",
							HttpStatus.INTERNAL_SERVER_ERROR);

				} else {
					Timer timer = new Timer();
					timer.schedule(new TimerTask() {
						@Override
						public void run() {
							webProjectService.setNpmInstall(false);
						}
					}, maxLimitSecondsCompileNpmBloqued * 1000L);
					webProjectService.setNpmInstall(true);
				}
			}
			webProjectService.loadGitDetails(webProject);

			String[] file = path.split("/");
			String finalpath;
			if (file[file.length - 1].contains(".html") || file[file.length - 1].contains(".json")
					|| file[file.length - 1].contains(".js") || file[file.length - 1].contains(".yml")) {
				if (webProject.getNpm() == false) {
					webProject.setMainFile(file[file.length - 1]);
				}
				finalpath = path.replace("/" + file[file.length - 1], "");
			} else {
				finalpath = path;
			}
			RestTemplate template = new RestTemplate();
			final HttpHeaders headers = new HttpHeaders();
			final HashMap<String, Object> map = new HashMap<>();
			map.put("path", finalpath);
			map.put("branch", branch);
			headers.add("Authorization", "Bearer " + utils.getCurrentUserOauthToken());
			headers.add("X-Git-Url", webProject.getGitUrl());
			headers.add("X-Git-Token", webProject.getGitToken());
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<?> httpEntity = new HttpEntity<Object>(map, headers);
			String url = gitManagerUrlInternal + "/gitlab/downloadZip/placeHolder";
			String urlDelete = gitManagerUrlInternal + "/gitlab/deleteFiles/" + webProject.getIdentification();
			webProjectService.cloneGitAndDownload(webProject, template, httpEntity, url, urlDelete, utils.getUserId());

			return new ResponseEntity<String>("SUCCESS", HttpStatus.OK);

		} else {
			log.error("Error, webproject is null");
			return new ResponseEntity<String>("WebProject not found", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Get status on current NPM ")
	@GetMapping("getCurrentStatus")
	@ApiResponses(@ApiResponse(content = @Content(schema = @Schema(implementation = WebProjectDTO.class)), responseCode = "200", description = "OK"))
	public ResponseEntity<String> getCurrentStatus() {
		String response = webProjectService.getCurrentStatus();
		webProjectService.resetCurrentStatus();
		JSONObject obj = new JSONObject();
		obj.put("console", response);
		obj.put("install", !webProjectService.isNpmInstall());
		if (!webProjectService.isNpmInstall()) {
			if (webProjectService.getNpmStatus() == NPMCommandResultStatus.OK) {
				return new ResponseEntity<String>(obj.toString(2), HttpStatus.OK);
			} else {
				return new ResponseEntity<String>(obj.toString(2), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		return new ResponseEntity<String>(obj.toString(2), HttpStatus.OK);

	}

}
