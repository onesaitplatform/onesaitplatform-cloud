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
package com.minsait.onesait.platform.controlpanel.controller.webproject;

import java.util.Iterator;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.json.JSONObject;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.minsait.onesait.platform.config.services.exceptions.WebProjectServiceException;
import com.minsait.onesait.platform.config.services.webproject.NPMCommandResult.NPMCommandResultStatus;
import com.minsait.onesait.platform.config.services.webproject.WebProjectDTO;
import com.minsait.onesait.platform.config.services.webproject.WebProjectService;
import com.minsait.onesait.platform.controlpanel.services.resourcesinuse.ResourcesInUseService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/webprojects")
@Slf4j
public class WebProjectController {


	@Autowired
	private WebProjectService webProjectService;

	@Autowired
	private AppWebUtils utils;

	@Autowired
	private ResourcesInUseService resourcesInUseService;

	@Autowired
	private HttpSession httpSession;

	@Autowired
	private MultitenancyService masterUserService;

	private static final String DEFAULT_VERTICAL = "onesaitplatform";

	@Value("${onesaitplatform.webproject.baseurl:https://localhost:18000/web/}")
	private String rootWWW;

	@Value("${onesaitplatform.gitlab.manager.server:http://localhost:10050/gitlab/api/v1}")
	private String gitManagerUrl;

	@Value("${digitaltwin.temp.dir:/tmp}")
	private String tmpDirectory;

	private static final String WEBPROJ_CREATE = "webprojects/create";
	private static final String REDIRECT_WEBPROJ_CREATE = "redirect:/webprojects/create";
	private static final String REDIRECT_WEBPROJ_LIST = "redirect:/webprojects/list";
	private static final String REDIRECT_WEBPROJ_GIT = "redirect:/webprojects/git/";

	private static final String APP_ID = "appId";

	@GetMapping(value = "/list", produces = "text/html")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public String list(Model model, HttpServletRequest request,
			@RequestParam(required = false, name = "identification") String identification,
			@RequestParam(required = false, name = "description") String description) {

		// CLEANING APP_ID FROM SESSION
		httpSession.removeAttribute(APP_ID);

		final List<WebProjectDTO> webprojects = webProjectService
				.getWebProjectsWithDescriptionAndIdentification(utils.getUserId(), identification, description);
		model.addAttribute("webprojects", webprojects);
		String vertical_name = masterUserService.getVerticalFromSchema(MultitenancyContextHolder.getVerticalSchema())
				.getName();
		if (vertical_name.equals(DEFAULT_VERTICAL)) {
			model.addAttribute("rootWWW", rootWWW);

		} else {
			model.addAttribute("rootWWW", rootWWW + vertical_name + "/");

		}

		return "webprojects/list";
	}

	@GetMapping(value = "/git/{id}", produces = "text/html")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public String git(Model model, @PathVariable("id") String id) {
		final WebProjectDTO webProject = webProjectService.getWebProjectById(id, utils.getUserId());
		webProjectService.loadGitDetails(webProject);
		if (webProject != null) {
			model.addAttribute("baseUrl", gitManagerUrl);
			model.addAttribute("token", utils.getCurrentUserOauthToken());
			model.addAttribute("webproject", webProject);
			return "webprojects/git";
		} else {
			return "webprojects/list";
		}

	}

	@PostMapping(value = "git/{id}/deploy")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<String> deployGit(@PathVariable("id") String id, @RequestParam("branch") String branch,
			@RequestParam("path") String path) {
		final WebProjectDTO webProject = webProjectService.getWebProjectById(id, utils.getUserId());
		if (webProject != null) {
			if (webProject.getNpm()) {
				if (webProjectService.isNpmInstall()) {
					return new ResponseEntity<String>("An NPM install is already happening, please try again later",
							HttpStatus.INTERNAL_SERVER_ERROR);

				} else {
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
			String url = gitManagerUrl + "/gitlab/downloadZip/placeHolder";

			try {
				ResponseEntity<Resource> response = template.exchange(url, HttpMethod.POST, httpEntity, Resource.class);
				Resource resource = response.getBody();
				byte[] bytes = resource.getInputStream().readAllBytes();
				File targetFile = new File(tmpDirectory + "/" + webProject.getIdentification() + "/"
						+ webProject.getIdentification() + ".zip");
				File directory = new File(tmpDirectory + "/" + webProject.getIdentification());
				if (!directory.exists()) {
					directory.mkdir();
				}
				OutputStream outStream = new FileOutputStream(targetFile);
				outStream.write(bytes);
				outStream.close();
				if (webProject.getNpm()) {
					webProjectService.unzipFile(tmpDirectory + "/" + webProject.getIdentification() + "/",
							webProject.getIdentification() + ".zip");
					webProjectService.compileNPM(webProject, utils.getUserId());
					targetFile.delete();

				} else {
					webProjectService.uploadZip(targetFile, utils.getUserId());
					webProjectService.updateWebProject(webProject, utils.getUserId());
					targetFile.delete();

				}

			} catch (IOException e) {
				log.error("Error:", e);
				if (webProject.getNpm()) {
					if (webProjectService.isNpmInstall()) {
						webProjectService.setNpmInstall(false);
					}
				}
				return new ResponseEntity<String>("Error when reading the files", HttpStatus.INTERNAL_SERVER_ERROR);
			} catch (HttpClientErrorException e) {
				if (webProject.getNpm()) {
					if (webProjectService.isNpmInstall()) {
						webProjectService.setNpmInstall(false);
					}
				}
				log.error("Error:", e);
				return new ResponseEntity<String>("Error when downloading the files from Git",
						HttpStatus.INTERNAL_SERVER_ERROR);
			} catch (HttpServerErrorException e) {
				if (webProject.getNpm()) {
					if (webProjectService.isNpmInstall()) {
						webProjectService.setNpmInstall(false);
					}
				}
				log.error("Error:", e);
				return new ResponseEntity<String>("Error when downloading the files from Git",
						HttpStatus.INTERNAL_SERVER_ERROR);
			} catch (final WebProjectServiceException e) {
				if (webProject.getNpm()) {
					if (webProjectService.isNpmInstall()) {
						webProjectService.setNpmInstall(true);
					}
				}
				log.error("Error:", e);
				return new ResponseEntity<String>("Error updating Web Project details",
						HttpStatus.INTERNAL_SERVER_ERROR);
			}

			return new ResponseEntity<String>("SUCCESS", HttpStatus.OK);

		} else {
			log.error("Error, webproject is null");
			return new ResponseEntity<String>("WebProject not found", HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@GetMapping(value = "/git/getNPMStatus")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<String> getNPMStatus() {
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

	@PostMapping("/getNamesForAutocomplete")
	public @ResponseBody List<String> getNamesForAutocomplete() {
		return webProjectService.getWebProjectsIdentifications(utils.getUserId());
	}

	@GetMapping(value = "/create", produces = "text/html")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public String create(Model model) {
		WebProjectDTO web = new WebProjectDTO();
		web.setNpm(false);
		model.addAttribute("webproject", web);
		return WEBPROJ_CREATE;
	}

	@PostMapping(value = "/create")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
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
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public String update(Model model, @PathVariable("id") String id) {
		final WebProjectDTO webProject = webProjectService.getWebProjectById(id, utils.getUserId());
		webProjectService.loadGitDetails(webProject);
		if (webProject != null) {
			model.addAttribute(ResourcesInUseService.RESOURCEINUSE,
					resourcesInUseService.isInUse(id, utils.getUserId()));
			resourcesInUseService.put(id, utils.getUserId());
			model.addAttribute("webproject", webProject);
			return WEBPROJ_CREATE;
		} else {
			return WEBPROJ_CREATE;
		}
	}

	@PostMapping(value = "/update/{id}", produces = "text/html")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
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
		resourcesInUseService.removeByUser(id, utils.getUserId());
		return REDIRECT_WEBPROJ_LIST;

	}

	@GetMapping(value = "/delete/{id}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
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
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<String> uploadZip(MultipartHttpServletRequest request) {

		final Iterator<String> itr = request.getFileNames();
		final String uploadedFile = itr.next();
		final MultipartFile file = request.getFile(uploadedFile);
		if (file != null) {
			if (utils.isFileExtensionForbidden(file)) {
				return new ResponseEntity<>("File type not allowed", HttpStatus.BAD_REQUEST);
			}
			if (file.getSize() > utils.getMaxFileSizeAllowed()) {
				return new ResponseEntity<>("File size too large", HttpStatus.PAYLOAD_TOO_LARGE);
			}
		}
		try {
			webProjectService.uploadZip(file, utils.getUserId());
			return new ResponseEntity<>("{\"status\" : \"ok\"}", HttpStatus.OK);
		} catch (final WebProjectServiceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping(value = "/uploadWebTemplate")
	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	public ResponseEntity<String> useTemplate() {
		try {
			webProjectService.uploadWebTemplate(utils.getUserId());
			return new ResponseEntity<>("{\"status\" : \"ok\"}", HttpStatus.OK);
		} catch (final WebProjectServiceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = "/downloadZip/{id}", produces = "application/zip")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
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

	@GetMapping(value = "/freeResource/{id}")
	public @ResponseBody void freeResource(@PathVariable("id") String id) {
		resourcesInUseService.removeByUser(id, utils.getUserId());
		log.info("free resource", id);
	}

}
