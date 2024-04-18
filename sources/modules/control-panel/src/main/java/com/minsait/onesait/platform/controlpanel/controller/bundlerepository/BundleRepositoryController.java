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
package com.minsait.onesait.platform.controlpanel.controller.bundlerepository;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.net.HttpHeaders;
import com.minsait.onesait.platform.business.services.bundles.BundleBusinessService;
import com.minsait.onesait.platform.business.services.versioning.VersioningBusinessService;
import com.minsait.onesait.platform.commons.git.GitlabConfiguration;
import com.minsait.onesait.platform.config.components.BundleConfiguration;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.versioning.RestoreReport;
import com.minsait.onesait.platform.config.versioning.VersioningException;
import com.minsait.onesait.platform.controlpanel.controller.versioning.ie.BundleController;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/bundlerepository")
@Slf4j
public class BundleRepositoryController {

	@Autowired
	private AppWebUtils utils;

	@Autowired
	private HttpSession httpSession;

	@Autowired
	private BundleBusinessService bundleBusinessService;
	
	@Autowired
	private VersioningBusinessService versioningBusinessService;
	
	@Autowired
	private ConfigurationService configurationService;

	private static final String EXECUTION_ID = "executionId";
	private static final String BUNDLE = "bundle";
	private static final String BUNDLES = "bundles";
	private static final String GIT_CONFIG_ERROR = "gitconfigerror";
	private static final String BUNDLE_GIT_CONFIG_ERROR = "versioning.bundle.config.error";
	private static final String BUNDLE_LIST = "bundlerepository/list";
	private static final String BUNDLE_SHOW = "bundlerepository/show";
	private static final String APP_ID = "appId";
	private static final String IMPORT_RESULT_ERROR = "importResultError";

	@GetMapping(value = "/list", produces = "text/html")
	public String list(Model model, HttpServletRequest request) {
		// CLEANING APP_ID FROM SESSION
		httpSession.removeAttribute(APP_ID);
		
		final BundleConfiguration config = configurationService.getBundleConfiguration();
		
		if (config!=null) {
			model.addAttribute(BUNDLES, bundleBusinessService.getBundles());
		} else {
			model.addAttribute(GIT_CONFIG_ERROR, BUNDLE_GIT_CONFIG_ERROR);
		}
		return BUNDLE_LIST;
	}

	@GetMapping(value = "view/{bundleId}", produces = "text/html")
	public Object viewbunddle(Model model, @PathVariable String bundleId, HttpServletRequest request) {
		// CLEANING APP_ID FROM SESSION
		httpSession.removeAttribute(APP_ID);
		final String accept = request.getHeader(org.springframework.http.HttpHeaders.ACCEPT);
		if (accept.contains("image") && !accept.contains("html")) {
			final String referer = request.getHeader(org.springframework.http.HttpHeaders.REFERER);
			try {
				final File output = bundleBusinessService.downloadFile(referer.substring(referer.indexOf("view/") + 5),
						bundleId);
				final ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(output.toPath()));
				return ResponseEntity.ok()
						.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=".concat(output.getName()))
						.contentLength(resource.contentLength())
						.contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);
			} catch (final Exception e) {
				return ResponseEntity.internalServerError().build();
			}
		} else {
			model.addAttribute(BUNDLE, bundleBusinessService.getBundle(bundleId));
		}

		return BUNDLE_SHOW;
	}

	@GetMapping(value = "view/**", produces = "text/html")
	public Object viewFile(Model model, HttpServletRequest request) {
		final String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		final String referer = request.getHeader(org.springframework.http.HttpHeaders.REFERER);
		try {
			final File output = bundleBusinessService.downloadFile(referer.substring(referer.indexOf("view/") + 5),
					URLDecoder.decode(path.replace("/bundlerepository/view/", ""), StandardCharsets.UTF_8));
			final ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(output.toPath()));
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=".concat(output.getName()))
					.contentLength(resource.contentLength())
					.contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);
		} catch (final Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	@PostMapping(value = "download/{bundleId}")
	public ResponseEntity<ByteArrayResource> download(@PathVariable String bundleId) {
		try {
			final File output = bundleBusinessService.downloadBundle(bundleId);
			final ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(output.toPath()));
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=".concat(output.getName()))
					.contentLength(resource.contentLength())
					.contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);
		} catch (final Exception e) {
			return ResponseEntity.internalServerError().build();
		}

	}

	@PostMapping(value = "install/{bundleId}")
	public String install(@PathVariable String bundleId, RedirectAttributes ra) {
		try {
			final RestoreReport report = new RestoreReport();
			report.setExecutionId(UUID.randomUUID().toString());
			report.setUserId(utils.getUserId());
			bundleBusinessService.installBundle(bundleId, report);
			ra.addFlashAttribute(EXECUTION_ID, report.getExecutionId());

		} catch (final Exception e) {
			log.error("Could not create git project ", e);
			ra.addFlashAttribute("message", "Could not load bundle " + e.getMessage());
		}

		return "redirect:/bundlerepository/view/" + bundleId;
	}
	
	@PostMapping("fromZIP")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public String installBundle(@RequestParam("zipFile") MultipartFile zipFile, RedirectAttributes ra) {
		try {
			final RestoreReport report = new RestoreReport();
			report.setExecutionId(UUID.randomUUID().toString());
			report.setUserId(utils.getUserId());
			if (zipFile != null) {
				versioningBusinessService.loadBundleZip(report, zipFile.getInputStream(),
						zipFile.getOriginalFilename());
			} else {
				throw new VersioningException("Please select file to upload");
			}
			ra.addFlashAttribute(EXECUTION_ID, report.getExecutionId());
		} catch (final Exception e) {
			log.error("Could not create git project ", e);
			ra.addFlashAttribute(IMPORT_RESULT_ERROR, "Could not load bundle " + e.getMessage());
		}
		return "redirect:/" + BUNDLE_LIST;
	}
	
}
