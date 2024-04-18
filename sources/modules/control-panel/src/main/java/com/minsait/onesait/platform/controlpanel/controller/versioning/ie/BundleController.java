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
package com.minsait.onesait.platform.controlpanel.controller.versioning.ie;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import com.minsait.onesait.platform.business.services.versioning.VersioningBusinessService;
import com.minsait.onesait.platform.commons.git.GitlabConfiguration;
import com.minsait.onesait.platform.config.versioning.BundleGenerateDTO;
import com.minsait.onesait.platform.config.versioning.RestoreReport;
import com.minsait.onesait.platform.config.versioning.VersioningException;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("bundles")
@Slf4j
public class BundleController {

	@Autowired
	private VersioningBusinessService versioningBusinessService;
	@Autowired
	private AppWebUtils utils;

	private static final String EXPORT_RESULT_ERROR = "exportResultError";
	private static final String IMPORT_RESULT_ERROR = "importResultError";
	private static final String MESSAGE = "message";

	private static final String EXECUTION_ID = "executionId";

	@GetMapping("generate")
	public String generateBundle(RedirectAttributes ra, Model model) {
		if (model.asMap().get(EXPORT_RESULT_ERROR) != null) {
			model.addAttribute(MESSAGE, model.asMap().get(EXPORT_RESULT_ERROR));
		}
		if (model.asMap().get(EXECUTION_ID) != null) {
			model.addAttribute(EXECUTION_ID, model.asMap().get(EXECUTION_ID));
		}
		model.addAttribute("gitConfig", new GitlabConfiguration());
		model.addAttribute("classes", versioningBusinessService.getVersionableSimpleClassNames());
		model.addAttribute("versionables", versioningBusinessService.versionablesVOForUser(utils.getUserId()));

		return "bundles/generate";
	}

	@PostMapping("generate")
	public Object generateBundle(@RequestParam(name = "folderName", required = true) String folderName,
			@RequestParam(name = "title", required = true) String bundleTitle,
			@RequestParam(required = false, name = "inclusions") String inclusionsString,
			@RequestParam(required = false, name = "readme") String readme,
			@RequestParam(required = false, name = "shortDesc") String shortDesc,
			@RequestParam(required = false, name = "version") String version,
			@RequestParam(required = true, name = "generateZip") Boolean generateZip,
			@RequestPart(required = false, name = "extraFiles") List<MultipartFile> extraFiles,
			@RequestPart(required = false, name = "image") MultipartFile bundleImage,
			@ModelAttribute GitlabConfiguration gitConfig, RedirectAttributes ra) throws IOException {
		final RestoreReport report = new RestoreReport();
		if (StringUtils.hasText(inclusionsString)) {
			final Map<String, Set<String>> inclusions = new ObjectMapper().readValue(inclusionsString,
					new TypeReference<HashMap<String, Set<String>>>() {
					});
			if (!StringUtils.hasText(folderName)) {
				ra.addFlashAttribute(EXPORT_RESULT_ERROR, "Invalid folder name");
				return "redirect:/bundles/generate";
			}
			report.setIncludeResources(inclusions);
			report.setExecutionId(UUID.randomUUID().toString());
			final BundleGenerateDTO bundle = buildBundleDTO(extraFiles, bundleImage, readme, folderName, bundleTitle,
					shortDesc, version);
			if (generateZip) {
				try {
					final File output = versioningBusinessService.createZipBundle(report, bundle);
					final ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(output.toPath()));
					return ResponseEntity.ok()
							.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=".concat(output.getName()))
							.contentLength(resource.contentLength())
							.contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);
				} catch (final Exception e) {
					log.error("Error ziping files", e);
					ra.addFlashAttribute(EXPORT_RESULT_ERROR, "Error ziping bundle " + e.getMessage());
					return "redirect:/bundles/generate";
				}
			} else {
				versioningBusinessService.createBundle(gitConfig, report, bundle);
			}
			ra.addFlashAttribute(EXECUTION_ID, report.getExecutionId());
			return "redirect:/bundles/generate";
		} else {
			ra.addFlashAttribute(EXPORT_RESULT_ERROR, "Please select resources to be exported");
			return "redirect:/bundles/generate";
		}

	}

	@GetMapping("load")
	public String loadBundle(Model model) {
		if (model.asMap().get(IMPORT_RESULT_ERROR) != null) {
			model.addAttribute(MESSAGE, model.asMap().get(IMPORT_RESULT_ERROR));
		}
		if (model.asMap().get(EXECUTION_ID) != null) {
			model.addAttribute(EXECUTION_ID, model.asMap().get(EXECUTION_ID));
		}
		final GitlabConfiguration gitConfig = new GitlabConfiguration();
		model.addAttribute("gitConfig", gitConfig);
		return "bundles/load";
	}

	@PostMapping("load")
	public String loadBundle(@ModelAttribute GitlabConfiguration gitConfig,
			@RequestParam(name = "folderName", required = false) String folderName,
			@RequestParam(name = "fromZip", required = true) Boolean fromZip,
			@RequestParam(name = "importAsCurrent", required = false, defaultValue = "true") Boolean importAsCurrent,
			@RequestPart(required = false, name = "zipFile") MultipartFile zipFile, RedirectAttributes ra) {
		try {
			final RestoreReport report = new RestoreReport();
			report.setExecutionId(UUID.randomUUID().toString());
			if (importAsCurrent) {
				report.setUserId(utils.getUserId());
			}
			if (fromZip) {
				if (zipFile != null) {
					versioningBusinessService.loadBundleZip(report, zipFile.getInputStream(),
							zipFile.getOriginalFilename());
				} else {
					throw new VersioningException("Please select file to upload");
				}
			} else {
				versioningBusinessService.restoreBundle(report, gitConfig, folderName);
			}
			ra.addFlashAttribute(EXECUTION_ID, report.getExecutionId());
		} catch (final Exception e) {
			log.error("Could not create git project ", e);
			ra.addFlashAttribute(IMPORT_RESULT_ERROR, "Could not load bundle " + e.getMessage());
		}
		return "redirect:/bundles/load";
	}

	private BundleGenerateDTO buildBundleDTO(List<MultipartFile> extraFiles, MultipartFile bundleImage, String readme,
			String folderName, String bundleTitle, String shortDesc, String version) {
		return BundleGenerateDTO.builder().extraResources(extraFiles).image(bundleImage).readme(readme)
				.folderName(folderName).title(bundleTitle).shortDesc(shortDesc).version(version).build();
	}
}
