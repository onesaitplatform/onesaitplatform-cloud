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
package com.minsait.onesait.platform.controlpanel.controller.versioning;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.business.services.versioning.RestoreRequestDTO;
import com.minsait.onesait.platform.business.services.versioning.SaveFileToEntityDTO;
import com.minsait.onesait.platform.business.services.versioning.VersioningBusinessService;
import com.minsait.onesait.platform.commons.git.CommitWrapper;
import com.minsait.onesait.platform.commons.git.GitlabConfiguration;
import com.minsait.onesait.platform.config.model.base.AuditableEntity;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;
import com.minsait.onesait.platform.config.model.interfaces.Versionable.SpecialVersionable;
import com.minsait.onesait.platform.config.versioning.RestorePlatformDTO;
import com.minsait.onesait.platform.config.versioning.RestoreReport;
import com.minsait.onesait.platform.config.versioning.RestoreReport.OperationResult;
import com.minsait.onesait.platform.config.versioning.VersioningException;
import com.minsait.onesait.platform.controlpanel.interceptor.VersioningCommitSetterFilter;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("versioning")
@Slf4j
public class VersioningController {

	private static final String EXECUTION_ID = "executionId";

	private static final String RESOURCE_VERSIONING_IS_NOT_ENABLED = "Resource versioning is not enabled in this environment, please configure Git and enable it first.";

	private static final String MESSAGE = "message";

	private static final String REDIRECT_VERSIONING_GIT_CONFIG = "redirect:/versioning/git-config";

	private static final String GIT_CONFIG_POST = "gitConfigPost";

	private static final String NOT_ENABLED = "notEnabled";

	private static final String RESTORE_RESULT = "restoreResult";

	private static final String RESTORE_RESULT_ERROR = "restoreResultError";

	@Autowired
	private VersioningBusinessService versioningBusinessService;

	@Autowired
	private AppWebUtils utils;

	@GetMapping("restore/resource")
	public String restoreResource(Model model, RedirectAttributes ra) {
		if (!versioningBusinessService.isActive() && utils.isAdministrator()) {
			ra.addFlashAttribute(NOT_ENABLED, RESOURCE_VERSIONING_IS_NOT_ENABLED);
			return REDIRECT_VERSIONING_GIT_CONFIG;
		} else if (!versioningBusinessService.isActive() && !utils.isAdministrator()) {
			ra.addFlashAttribute(MESSAGE, RESOURCE_VERSIONING_IS_NOT_ENABLED + " Contact an administrator");
			return "redirect:/main";
		}
		model.addAttribute("classes", versioningBusinessService.getVersionableClases());
		return "versioning/restore-resource";
	}

	@GetMapping("restore/resource/{class}")
	public String restoreResourceGetEntites(@PathVariable("class") String clazz, Model model) {
		final Collection<Versionable<AuditableEntity>> versionables = versioningBusinessService
				.getEntitiesForUser(utils.getUserId(), clazz);
		model.addAttribute("versionables", versionables);
		return "versioning/restore-resource :: #versionables";
	}

	@GetMapping("restore/resource/{id}/commits")
	public String commitsForEntity(@PathVariable("id") String id, @RequestParam("class") String clazz, Model model) {
		final Versionable<?> versionable = versioningBusinessService.findById(id, clazz);
		try {
			final List<CommitWrapper> commits = versioningBusinessService.getCommitsForVersionable(versionable);
			model.addAttribute("commits", commits);
		} catch (final Exception e) {
			model.addAttribute("commits", new ArrayList<>());
		}

		return "versioning/restore-resource :: #commits";
	}

	@PostMapping("restore/resource")
	public ResponseEntity<RestoreReport> restoreFile(@RequestBody RestoreRequestDTO restoreRequest) {
		restoreRequest.setUserId(utils.getUserId());
		RestoreReport report = new RestoreReport();
		try {
			report = versioningBusinessService.restoreFile(restoreRequest, report);
			if (report.getErrors().isEmpty()) {
				report.setResultMessage("Restored resource with id " + restoreRequest.getEntityId() + " from commit "
						+ restoreRequest.getCommitId() + " file name " + restoreRequest.getFileName());
				return ResponseEntity.ok(report);
			} else {
				report.setResultMessage("Error restoring resource with id " + restoreRequest.getEntityId()
						+ " from commit " + restoreRequest.getCommitId() + " file name " + restoreRequest.getFileName()
						+ "\nErrors are: " + String.join(";", report.getErrors()));
				return new ResponseEntity<>(report, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (final Exception e) {
			log.error("Error while restoring resource", e);
			if (report.getErrors().isEmpty()) {
				report.setResultMessage("Error restoring resource with id " + restoreRequest.getEntityId()
						+ " from commit " + restoreRequest.getCommitId() + " file name " + restoreRequest.getFileName()
						+ "\n" + VersioningException.processErrorMessageToFront(e));
				return new ResponseEntity<>(report, HttpStatus.INTERNAL_SERVER_ERROR);
			} else {
				report.setResultMessage("Error restoring resource with id " + restoreRequest.getEntityId()
						+ " from commit " + restoreRequest.getCommitId() + " file name " + restoreRequest.getFileName()
						+ "\nErrors are: " + String.join(";", report.getErrors()) + "\n"
						+ VersioningException.processErrorMessageToFront(e));
				return new ResponseEntity<>(report, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}

	@GetMapping("restore/resource/{id}/commit/{commitId}/file-content")
	public ResponseEntity<String> fileContent(@PathVariable("id") Object id, @PathVariable("commitId") String commitId,
			@RequestParam("class") String clazz,
			@RequestParam(required = false, defaultValue = "false", value = "isFilePath") Boolean isFilePath,
			@RequestParam(required = false, value = "filePath") String filePath) {
		String fileContent;
		if (!isFilePath) {
			fileContent = versioningBusinessService.getFileContent(versioningBusinessService.findById(id, clazz),
					commitId);
		} else {
			fileContent = versioningBusinessService.getFileContent(filePath, commitId);
		}
		return ResponseEntity.ok().body(fileContent);
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping("git-config")
	public String gitConfig(Model model) {
		if (model.asMap().get(NOT_ENABLED) != null) {
			model.addAttribute(MESSAGE, model.asMap().get(NOT_ENABLED));
		}
		if (model.asMap().get(GIT_CONFIG_POST) != null) {
			model.addAttribute(MESSAGE, model.asMap().get(GIT_CONFIG_POST));
		}
		final GitlabConfiguration gitConfig = versioningBusinessService.getGitConfiguration();
		model.addAttribute("gitConfig", gitConfig == null ? new GitlabConfiguration() : gitConfig);
		return "versioning/git-configuration";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping("git-config")
	public String gitConfigPost(@ModelAttribute GitlabConfiguration gitConfig,
			@RequestParam(name = "createGit", required = false, defaultValue = "false") Boolean createGit,
			RedirectAttributes ra, HttpServletRequest req) {
		try {
			versioningBusinessService.createGitConfiguration(gitConfig, createGit);
			req.setAttribute(VersioningCommitSetterFilter.VERSIONING_ENABLED_ATT, versioningBusinessService.isActive());
		} catch (final Exception e) {
			log.error("Could not create git project ", e);
			ra.addFlashAttribute(GIT_CONFIG_POST, "Could not create git project " + e.getMessage());
		}
		return REDIRECT_VERSIONING_GIT_CONFIG;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@DeleteMapping("git-config")
	public String deleteGitConfig() {
		versioningBusinessService.removeGitConfiguration();
		return REDIRECT_VERSIONING_GIT_CONFIG;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping("enable-feature/{enable}")
	public ResponseEntity<String> enableFeature(@PathVariable("enable") Boolean enable, HttpServletRequest req) {
		versioningBusinessService.enableFeature(enable);
		req.setAttribute(VersioningCommitSetterFilter.VERSIONING_ENABLED_ATT, versioningBusinessService.isActive());
		return ResponseEntity.ok().build();
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping("restore/platform")
	public String restorePlatform(Model model, RedirectAttributes ra) {
		if (!versioningBusinessService.isActive()) {
			ra.addFlashAttribute(NOT_ENABLED, RESOURCE_VERSIONING_IS_NOT_ENABLED);
			return REDIRECT_VERSIONING_GIT_CONFIG;
		}
		if (model.asMap().get(EXECUTION_ID) != null) {
			model.addAttribute(EXECUTION_ID, model.asMap().get(EXECUTION_ID));
		}
		if (model.asMap().get(RESTORE_RESULT) != null) {
			model.addAttribute("info", model.asMap().get(RESTORE_RESULT));
		} else if (model.asMap().get(RESTORE_RESULT_ERROR) != null) {
			model.addAttribute(MESSAGE, model.asMap().get(RESTORE_RESULT_ERROR));
		}
		model.addAttribute("sourceConfig", new RestorePlatformDTO());
		model.addAttribute("restoreTypes", RestorePlatformDTO.Restore.values());
		return "versioning/restore-platform";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping("snapshot/platform")
	public String snapshotPlatform(RedirectAttributes ra, Model model) {
		if (!versioningBusinessService.isActive()) {
			ra.addFlashAttribute(NOT_ENABLED, RESOURCE_VERSIONING_IS_NOT_ENABLED);
			return REDIRECT_VERSIONING_GIT_CONFIG;
		}
		if (model.asMap().get(EXECUTION_ID) != null) {
			model.addAttribute(EXECUTION_ID, model.asMap().get(EXECUTION_ID));
		}
		if (model.asMap().get(RESTORE_RESULT) != null) {
			model.addAttribute("info", model.asMap().get(RESTORE_RESULT));
		} else if (model.asMap().get(RESTORE_RESULT_ERROR) != null) {
			model.addAttribute(MESSAGE, model.asMap().get(RESTORE_RESULT_ERROR));
		}
		model.addAttribute("classes", versioningBusinessService.getVersionableSimpleClassNames());
		model.addAttribute("versionables", versioningBusinessService.versionablesVO());

		return "versioning/snapshot";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping("snapshot/platform")
	public String snapshotPlatform(@RequestParam(name = "tag-name", required = false) String tagName,
			@RequestParam(required = false, name = "exclusions") String exclusionsString, RedirectAttributes ra)
			throws IOException {
		final RestoreReport report = new RestoreReport();
		if (StringUtils.hasText(exclusionsString)) {
			final Map<String, Set<String>> exclusions = new ObjectMapper().readValue(exclusionsString,
					new TypeReference<HashMap<String, Set<String>>>() {
					});
			if (!exclusions.isEmpty()) {
				if (!StringUtils.hasText(tagName)) {
					ra.addFlashAttribute(RESTORE_RESULT_ERROR, "Please send tag-name parameter");
					return "redirect:/versioning/snapshot/platform";
				}
				report.setExcludeResources(exclusions);
			}
		}
		report.setExecutionId(UUID.randomUUID().toString());
		versioningBusinessService.generateSnapShot(tagName, report);
		ra.addFlashAttribute(EXECUTION_ID, report.getExecutionId());
		return "redirect:/versioning/snapshot/platform";
	}

	@GetMapping("execution/{executionId}")
	public ResponseEntity<RestoreReport> getExecutionReport(@PathVariable String executionId) {
		final RestoreReport report = versioningBusinessService.getReport(executionId);
		if (report != null && report.isFinished()) {
			return ResponseEntity.ok().body(report);
		} else {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping("restore/platform")
	public String restorePlatformPost(@ModelAttribute RestorePlatformDTO restoreDTO, RedirectAttributes ra) {
		final RestoreReport report = new RestoreReport();
		report.setExecutionId(UUID.randomUUID().toString());
		versioningBusinessService.restorePlatform(restoreDTO, report);
		ra.addFlashAttribute(EXECUTION_ID, report.getExecutionId());
		return "redirect:/versioning/restore/platform";
	}

	@PostMapping("save/local-changes")
	public ResponseEntity<RestoreReport> saveLocalChanges(@RequestBody SaveFileToEntityDTO saveFileToEntityDTO) {
		saveFileToEntityDTO.setUserId(utils.getUserId());
		final RestoreReport report = versioningBusinessService.saveFileChangesToEntity(saveFileToEntityDTO);
		if (OperationResult.SUCCESS.equals(report.getOperationResult())) {
			return ResponseEntity.ok(report);
		} else {
			return new ResponseEntity<>(report, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping("git-config/re-initialize")
	public ResponseEntity<String> reinitGit() {
		versioningBusinessService.reinitializeGitDir();
		return ResponseEntity.ok().build();
	}

	@PostMapping("git-config/sync-repo")
	public ResponseEntity<String> syncGitAndDB() {
		versioningBusinessService.syncGitAndDB();
		return ResponseEntity.ok().build();
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping("/git-config/tag-valid")
	public ResponseEntity<String> isTagValid(@RequestBody String tagName) {
		final boolean isValid = versioningBusinessService.isTagValid(tagName);
		if (isValid) {
			return ResponseEntity.ok().build();
		} else {
			return ResponseEntity.badRequest().build();
		}
	}

	@PostMapping("/commit/{versionable}/{id}")
	public ResponseEntity<String> commitChangesSpecial(@PathVariable("id") String id,
			@PathVariable("versionable") SpecialVersionable versionable, @RequestBody String commitMessage) {
		versioningBusinessService.commitSpecialVersionable(versionable, id, commitMessage);
		return ResponseEntity.ok().build();
	}

}
