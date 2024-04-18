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
package com.minsait.onesait.platform.controlpanel.rest.management.versioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.business.services.versioning.RestoreRequestDTO;
import com.minsait.onesait.platform.business.services.versioning.VersioningBusinessService;
import com.minsait.onesait.platform.commons.git.CommitWrapper;
import com.minsait.onesait.platform.commons.git.GitlabConfiguration;
import com.minsait.onesait.platform.config.model.base.AuditableEntity;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;
import com.minsait.onesait.platform.config.model.interfaces.Versionable.SpecialVersionable;
import com.minsait.onesait.platform.config.versioning.RestorePlatformDTO;
import com.minsait.onesait.platform.config.versioning.RestoreReport;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("api/versioning")
@Api(value = "Platform versioning REST API", tags = { "Platform versioning REST API" })
@Slf4j
public class VersioningRestController {

	private static final String RESOURCE_VERSIONING_IS_NOT_ENABLED = "Resource versioning is not enabled";

	@Autowired
	private VersioningBusinessService versioningBusinessService;

	@Autowired
	private AppWebUtils utils;

	@GetMapping("supported-classes")
	@Operation(summary = "Supported classes by the versioning system")
	@ApiOperation(value = "Supported classes by the versioning system")
	@ApiResponse(code = 200, message = "OK", response = Map.class)
	public ResponseEntity<?> restoreResource() {
		if (!versioningBusinessService.isActive()) {
			return new ResponseEntity<>(RESOURCE_VERSIONING_IS_NOT_ENABLED, HttpStatus.BAD_REQUEST);
		} else {
			return new ResponseEntity<>(versioningBusinessService.getVersionableClases(), HttpStatus.OK);
		}
	}

	@GetMapping("restore/resource/{class}")
	@Operation(summary = "Versionable resources in the system by fully qualified class name")
	@ApiOperation(value = "Versionable resources in the system by fully qualified class name")
	@ApiResponse(code = 200, message = "OK", response = Versionable[].class)
	public ResponseEntity<?> restoreResourceGetEntites(
			@ApiParam(example = "com.minsait.onesait.platform.config.model.Ontology") @PathVariable("class") String clazz) {
		if (!versioningBusinessService.isActive()) {
			return new ResponseEntity<>(RESOURCE_VERSIONING_IS_NOT_ENABLED, HttpStatus.BAD_REQUEST);
		} else {
			final Collection<Versionable<AuditableEntity>> versionables = versioningBusinessService
					.getEntitiesForUser(utils.getUserId(), clazz);
			return ResponseEntity.ok(versionables);
		}
	}

	@GetMapping("restore/resource/{id}/commits")
	@Operation(summary = "Resource commits by fully qualified class name and ID")
	@ApiOperation(value = "Resource commits by fully qualified class name and ID")
	@ApiResponse(code = 200, message = "OK", response = CommitWrapper[].class)
	public ResponseEntity<?> commitsForEntity(@PathVariable("id") String id,
			@ApiParam(example = "com.minsait.onesait.platform.config.model.Ontology") @RequestParam("class") String clazz) {
		if (!versioningBusinessService.isActive()) {
			return new ResponseEntity<>(RESOURCE_VERSIONING_IS_NOT_ENABLED, HttpStatus.BAD_REQUEST);
		} else {
			final Versionable<?> versionable = versioningBusinessService.findById(id, clazz);
			try {
				final List<CommitWrapper> commits = versioningBusinessService.getCommitsForVersionable(versionable);
				return ResponseEntity.ok(commits);
			} catch (final Exception e) {
				log.error("Error while getting commits for resource id {} of type {}", id, clazz, e);
				return ResponseEntity.ok(new ArrayList<>());
			}
		}
	}

	@PostMapping("restore/resource")
	@Operation(summary = "Restore resource from commit")
	@ApiOperation(value = "Restore resource from commit")
	public ResponseEntity<String> restoreFile(@RequestBody RestoreRequestDTO restoreRequest) {
		if (!versioningBusinessService.isActive()) {
			return new ResponseEntity<>(RESOURCE_VERSIONING_IS_NOT_ENABLED, HttpStatus.BAD_REQUEST);
		} else {
			restoreRequest.setUserId(utils.getUserId());
			RestoreReport report = new RestoreReport();
			try {
				report = versioningBusinessService.restoreFile(restoreRequest, report);
				if (report.getErrors().isEmpty()) {
					return ResponseEntity.ok().build();
				} else {
					return new ResponseEntity<>(String.join(";", report.getErrors()), HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} catch (final Exception e) {
				log.error("Error while restoring resource", e);
				if (report.getErrors().isEmpty()) {
					return new ResponseEntity<>("Could not restore resource", HttpStatus.INTERNAL_SERVER_ERROR);
				} else {
					return new ResponseEntity<>("Could not restore resource: " + String.join(";", report.getErrors()),
							HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}
		}
	}

	@GetMapping("restore/resource/{id}/commit/{commitId}/file-content")
	@Operation(summary = "Resource serialized content by ID, commit SHA and fully qualified class name")
	@ApiOperation(value = "Resource serialized content by ID, commit SHA and fully qualified class name")
	@ApiResponse(code = 200, message = "OK", response = String.class)
	public ResponseEntity<String> fileContent(@PathVariable("id") Object id, @PathVariable("commitId") String commitId,
			@ApiParam(example = "com.minsait.onesait.platform.config.model.Ontology") @RequestParam("class") String clazz) {
		if (!versioningBusinessService.isActive()) {
			return new ResponseEntity<>(RESOURCE_VERSIONING_IS_NOT_ENABLED, HttpStatus.BAD_REQUEST);
		} else {
			final String fileContent = versioningBusinessService
					.getFileContent(versioningBusinessService.findById(id, clazz), commitId);
			return ResponseEntity.ok().body(fileContent);
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping("git-config")
	@Operation(summary = "Current GIT configuration for versioning system")
	@ApiOperation(value = "Current GIT configuration for versioning system")
	@ApiResponse(code = 200, message = "OK", response = GitlabConfiguration.class)
	public ResponseEntity<?> gitConfig() {
		if (!versioningBusinessService.isActive()) {
			return new ResponseEntity<>(RESOURCE_VERSIONING_IS_NOT_ENABLED, HttpStatus.BAD_REQUEST);
		} else {
			final GitlabConfiguration gitConfig = versioningBusinessService.getGitConfiguration();
			return ResponseEntity.ok(gitConfig == null ? new GitlabConfiguration() : gitConfig);
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping("git-config")
	@Operation(summary = "Creates or modifies current GIT configuration")
	@ApiOperation(value = "Creates or modifies current GIT configuration")
	public ResponseEntity<String> gitConfigPost(@ModelAttribute GitlabConfiguration gitConfig,
			@RequestParam(name = "createGit", required = false, defaultValue = "false") Boolean createGit) {
		try {
			versioningBusinessService.createGitConfiguration(gitConfig, createGit);
			return ResponseEntity.ok().build();
		} catch (final Exception e) {
			log.error("Could not create git project ", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@DeleteMapping("git-config")
	@Operation(summary = "Removes current GIT configuration")
	@ApiOperation(value = "Removes current GIT configuration")
	public ResponseEntity<String> deleteGitConfig() {
		versioningBusinessService.removeGitConfiguration();
		return ResponseEntity.ok().build();
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping("enable/{enable}")
	@Operation(summary = "Enables or disables versioning of resources")
	@ApiOperation(value = "Enables or disables versioning of resources")
	public ResponseEntity<String> enableFeature(@PathVariable("enable") Boolean enable) {
		if (!versioningBusinessService.isActive()) {
			return new ResponseEntity<>(RESOURCE_VERSIONING_IS_NOT_ENABLED, HttpStatus.BAD_REQUEST);
		} else {
			versioningBusinessService.enableFeature(enable);
			return ResponseEntity.ok().build();
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping("snapshot/platform")
	@Operation(summary = "Creates a Snapshot of current resources in GIT (Async process)")
	@ApiOperation(value = "Creates a Snapshot of current resources in GIT (Async process)")
	public ResponseEntity<String> snapshotPlatform(@RequestParam(name = "tag-name", required = false) String tagName,
			@RequestBody(required = false) Map<String, Set<String>> exclusions) {
		if (!versioningBusinessService.isActive()) {
			return new ResponseEntity<>(RESOURCE_VERSIONING_IS_NOT_ENABLED, HttpStatus.BAD_REQUEST);
		} else {
			final RestoreReport report = new RestoreReport();
			if (exclusions != null) {
				if(!StringUtils.hasText(tagName)) {
					return ResponseEntity.badRequest().body("Please send tag-name parameter");
				}
				report.setExcludeResources(exclusions);
			}
			report.setExecutionId(UUID.randomUUID().toString());
			versioningBusinessService.generateSnapShot(tagName, report);
			return ResponseEntity.ok(report.getExecutionId());
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping("process/execution/{executionId}")
	@Operation(summary = "Result of the async process by execution ID")
	@ApiOperation(value = "Result of the async process by execution ID")
	@ApiResponses({ @ApiResponse(code = 200, message = "Execution results", response = RestoreReport.class),
		@ApiResponse(code = 204, message = "Execution did not finish or doesn't exist") })
	public ResponseEntity<?> getExecutionReport(@PathVariable String executionId) {
		if (!versioningBusinessService.isActive()) {
			return new ResponseEntity<>(RESOURCE_VERSIONING_IS_NOT_ENABLED, HttpStatus.BAD_REQUEST);
		} else {
			final RestoreReport report = versioningBusinessService.getReport(executionId);
			if (report != null && report.isFinished()) {
				return ResponseEntity.ok().body(report);
			} else {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping("restore/platform")
	@Operation(summary = "Restores platform resources from GIT (Async process)")
	@ApiOperation(value = "Restores platform resources from GIT (Async process)")
	public ResponseEntity<String> restorePlatformPost(@ModelAttribute RestorePlatformDTO restoreDTO) {
		if (!versioningBusinessService.isActive()) {
			return new ResponseEntity<>(RESOURCE_VERSIONING_IS_NOT_ENABLED, HttpStatus.BAD_REQUEST);
		} else {
			final RestoreReport report = new RestoreReport();
			report.setExecutionId(UUID.randomUUID().toString());
			versioningBusinessService.restorePlatform(restoreDTO, report);
			return ResponseEntity.ok(report.getExecutionId());
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping("git-config/re-initialize")
	@Operation(summary = "Re-initializes GIT local repository")
	@ApiOperation(value = "Re-initializes GIT local repository")
	public ResponseEntity<String> reinitGit() {
		if (!versioningBusinessService.isActive()) {
			return new ResponseEntity<>(RESOURCE_VERSIONING_IS_NOT_ENABLED, HttpStatus.BAD_REQUEST);
		} else {
			versioningBusinessService.reinitializeGitDir();
			return ResponseEntity.ok().build();
		}
	}

	@PostMapping("git-config/sync-repo")
	@Operation(summary = "Re-syncs GIT remote repository and database")
	@ApiOperation(value = "Re-syncs GIT remote repository and database")
	public ResponseEntity<String> syncGitAndDB() {
		if (!versioningBusinessService.isActive()) {
			return new ResponseEntity<>(RESOURCE_VERSIONING_IS_NOT_ENABLED, HttpStatus.BAD_REQUEST);
		} else {
			versioningBusinessService.syncGitAndDB();
			return ResponseEntity.ok().build();
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping("/git-config/tag-valid")
	@Operation(summary = "Check if GIT tag is valid")
	@ApiOperation(value = "Check if GIT tag is valid")
	@ApiResponses({ @ApiResponse(code = 200, message = "Valid Tag"),
		@ApiResponse(code = 400, message = "Invalid Tag") })
	public ResponseEntity<String> isTagValid(@RequestBody String tagName) {
		final boolean isValid = versioningBusinessService.isTagValid(tagName);
		if (isValid) {
			return ResponseEntity.ok().build();
		} else {
			return ResponseEntity.badRequest().build();
		}
	}

	@PostMapping("/commit/{versionable}/{id}")
	@Operation(summary = "Commit special resource changes (Flows, dataflows, notebooks)")
	@ApiOperation(value = "Commit special resource changes (Flows, dataflows, notebooks)")
	public ResponseEntity<String> commitChangesSpecial(@PathVariable("id") String id,
			@PathVariable("versionable") SpecialVersionable versionable, @RequestBody String commitMessage) {
		if (!versioningBusinessService.isActive()) {
			return new ResponseEntity<>(RESOURCE_VERSIONING_IS_NOT_ENABLED, HttpStatus.BAD_REQUEST);
		} else {
			versioningBusinessService.commitSpecialVersionable(versionable, id, commitMessage);
			return ResponseEntity.ok().build();
		}
	}
}
