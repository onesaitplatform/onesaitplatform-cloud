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
package com.minsait.onesait.platform.controlpanel.controller.webproject;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.minsait.onesait.platform.config.model.WebProject;
import com.minsait.onesait.platform.config.services.exceptions.WebProjectServiceException;
import com.minsait.onesait.platform.config.services.webproject.WebProjectDTO;
import com.minsait.onesait.platform.config.services.webproject.WebProjectService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/webprojects")
@Slf4j
public class WebProjectController {

	@Autowired
	private WebProjectService webProjectService;

	@Autowired
	private AppWebUtils utils;

	@Value("${onesaitplatform.webproject.baseurl:https://localhost:18000/web/}")
	private String rootWWW;

	private static final String WEBPROJ_CREATE = "webprojects/create";
	private static final String REDIRECT_WEBPROJ_CREATE = "redirect:/webprojects/create";
	private static final String REDIRECT_WEBPROJ_LIST = "redirect:/webprojects/list";

	@GetMapping(value = "/list", produces = "text/html")
	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	public String list(Model model, HttpServletRequest request,
			@RequestParam(required = false, name = "identification") String identification,
			@RequestParam(required = false, name = "description") String description) {

		final List<WebProjectDTO> webprojects = webProjectService
				.getWebProjectsWithDescriptionAndIdentification(utils.getUserId(), identification, description);
		model.addAttribute("webprojects", webprojects);
		model.addAttribute("rootWWW", rootWWW);

		return "webprojects/list";
	}

	@PostMapping("/getNamesForAutocomplete")
	public @ResponseBody List<String> getNamesForAutocomplete() {
		return webProjectService.getWebProjectsIdentifications(utils.getUserId());
	}

	@GetMapping(value = "/create", produces = "text/html")
	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	public String create(Model model) {

		model.addAttribute("webproject", new WebProject());
		return WEBPROJ_CREATE;
	}

	@PostMapping(value = "/create")
	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	public String createWebProject(Model model, @Valid WebProjectDTO webProject, BindingResult bindingResult,
			RedirectAttributes redirect) {
		if (bindingResult.hasErrors()) {
			log.debug("Some web project properties missing");
			utils.addRedirectMessage("webproject.validation.error", redirect);
			return REDIRECT_WEBPROJ_CREATE;
		}
		if (!webProjectService.webProjectExists(webProject.getIdentification())) {
			try {
				webProjectService.createWebProject(webProject, utils.getUserId());
			} catch (final WebProjectServiceException e) {
				log.error("Cannot create webproject because of: " + e.getMessage());
				utils.addRedirectMessage("webproject.create.error", redirect);
				return REDIRECT_WEBPROJ_CREATE;
			}
		} else {
			log.error("Cannot create webproject because of: " + "Web Project with identification: "
					+ webProject.getIdentification() + " already exists");
			utils.addRedirectMessage("webproject.validation.exists", redirect);
			return REDIRECT_WEBPROJ_CREATE;
		}

		return REDIRECT_WEBPROJ_LIST;
	}

	@GetMapping(value = "/update/{id}", produces = "text/html")
	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	public String update(Model model, @PathVariable("id") String id) {
		final WebProjectDTO webProject = webProjectService.getWebProjectById(id, utils.getUserId());

		if (webProject != null) {
			model.addAttribute("webproject", webProject);
			return WEBPROJ_CREATE;
		} else {
			return WEBPROJ_CREATE;
		}
	}

	@PutMapping(value = "/update/{id}", produces = "text/html")
	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	public String updateWebProject(Model model, @PathVariable("id") String id, @Valid WebProjectDTO webProject,
			BindingResult bindingResult, RedirectAttributes redirect) {

		if (bindingResult.hasErrors()) {
			log.debug("Some web project properties missing");
			utils.addRedirectMessage("webproject.validation.error", redirect);
			return "redirect:/webprojects/update/" + id;
		}
		try {
			webProjectService.updateWebProject(webProject, utils.getUserId());
		} catch (final WebProjectServiceException e) {
			log.error("Cannot update web project because of: " + e);
			utils.addRedirectMessage("webproject.update.error", redirect);
			return "redirect:/webprojects/update/" + id;
		}
		return REDIRECT_WEBPROJ_LIST;

	}

	@GetMapping(value = "/delete/{id}")
	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	public String deleteWebProject(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {

		final WebProjectDTO webProject = webProjectService.getWebProjectById(id, utils.getUserId());
		if (webProject != null) {
			try {
				webProjectService.deleteWebProject(id, utils.getUserId());
			} catch (final WebProjectServiceException e) {
				log.error("Cannot update web project because of: " + e);
				utils.addRedirectMessage("webproject.delete.error", redirect);
				return REDIRECT_WEBPROJ_LIST;
			}
			return REDIRECT_WEBPROJ_LIST;
		} else {
			return REDIRECT_WEBPROJ_LIST;
		}
	}

	@PostMapping(value = "/uploadZip")
	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	public ResponseEntity<String> uploadZip(MultipartHttpServletRequest request) {

		final Iterator<String> itr = request.getFileNames();
		final String uploadedFile = itr.next();
		final MultipartFile file = request.getFile(uploadedFile);
		if (file != null) {
			if (utils.isFileExtensionForbidden(file))
				return new ResponseEntity<>("File type not allowed", HttpStatus.BAD_REQUEST);
			if (file.getSize() > utils.getMaxFileSizeAllowed())
				return new ResponseEntity<>("File size too large", HttpStatus.PAYLOAD_TOO_LARGE);
		}
		try {
			webProjectService.uploadZip(file, utils.getUserId());
			return new ResponseEntity<>("{\"status\" : \"ok\"}", HttpStatus.OK);
		} catch (final WebProjectServiceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = "/downloadZip/{id}", produces = "application/zip")
	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	public ResponseEntity<?> downloadZip(@PathVariable("id") String id) {

		final WebProjectDTO webProject;
		final byte[] zipFile;

		try {
			webProject = webProjectService.getWebProjectById(id, utils.getUserId());
			if (webProject == null) {
				return new ResponseEntity<>("Web Project does not exist", HttpStatus.FORBIDDEN);
			}
		} catch (final WebProjectServiceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED); // unauthorized
		}

		try {
			zipFile = webProjectService.downloadZip(webProject.getIdentification(), utils.getUserId());
		} catch (final WebProjectServiceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}

		final HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + webProject.getIdentification() + ".zip\"");

		return new ResponseEntity<>(zipFile, headers, HttpStatus.OK);
	}

}
