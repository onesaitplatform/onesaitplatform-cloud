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
package com.minsait.onesait.platform.controlpanel.controller.mapsproject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.model.MapsProject;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.services.deletion.EntityDeletionService;
import com.minsait.onesait.platform.config.services.exceptions.DashboardServiceException;
import com.minsait.onesait.platform.config.services.exceptions.MapsProjectServiceException;
import com.minsait.onesait.platform.config.services.mapsmap.MapsMapService;
import com.minsait.onesait.platform.config.services.mapsmap.dto.MapsMapDTO;
import com.minsait.onesait.platform.config.services.mapsproject.MapsProjectService;
import com.minsait.onesait.platform.config.services.mapsstyle.MapsStyleService;
import com.minsait.onesait.platform.config.services.user.UserAmplified;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.helper.apimanager.ApiManagerHelper;
import com.minsait.onesait.platform.controlpanel.services.resourcesinuse.ResourcesInUseService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@RequestMapping("/mapsproject")
@Controller
@Slf4j
public class MapsProjectController {

	@Autowired
	private AppWebUtils utils;
	@Autowired
	private MapsProjectService mapsProjectService;

	@Autowired
	private UserService userService;
	@Autowired
	private MapsMapService mapsMapService;
	@Autowired
	private MapsStyleService mapsStyleService;
	@Autowired
	private ResourcesInUseService resourcesInUseService;
	@Autowired
	private EntityDeletionService entityDeletionService;
	@Autowired
	private ApiManagerHelper apiManagerHelper;
	@Autowired
	ConfigurationService configurationService;
	/*
	 * @Autowired private ResourcesInUseService resourcesInUseService;
	 */

	private static final String PROJECT_CREATE = "mapsproject/create";
	private static final String PROJECT_SHOW = "mapsproject/show";
	private static final String PROJECT_LIST = "mapsproject/list";
	private static final String MAPSSTYLES = "mapsstyles";
	private static final String MAPSMAPS = "mapsmaps";
	private static final String ELEMENT = "element";
	private static final String MESSAGE = "message";
	private static final String ERROR_403 = "error/403";
	private static final String ERROR_404 = "error/404";
	private static final String REDIRECT_PROJECT_LIST = "redirect:/mapsproject/list";

	@RequestMapping(value = "/list", produces = "text/html")
	public String list(Model model, @RequestParam(required = false, name = "name") String identification) {
		model.addAttribute("mapsprojects", mapsProjectService.getProjectsForUser(utils.getUserId(), identification));
		if (utils.isAdministrator()) {
			List<UserAmplified> users = userService.getAllUsersList();
			for (Iterator iterator = users.iterator(); iterator.hasNext();) {
				UserAmplified userAmplified = (UserAmplified) iterator.next();
				if (!userAmplified.getRole().equals("ROLE_ADMINISTRATOR")
						&& !userAmplified.getRole().equals("ROLE_DATASCIENTIST")
						&& !userAmplified.getRole().equals("ROLE_DEVELOPER")) {
					iterator.remove();
				}

			}

			model.addAttribute("users", users);
		}
		final Configuration configuration = configurationService.getConfiguration(Configuration.Type.MAPS_PROJECT,
				"default", null);

		@SuppressWarnings("unchecked")
		final ArrayList<String> urls = (ArrayList<String>) configurationService.fromYaml(configuration.getYmlConfig())
				.get("urls");
		@SuppressWarnings("unchecked")
		final ArrayList<String> descriptions = (ArrayList<String>) configurationService
				.fromYaml(configuration.getYmlConfig()).get("descriptions");
		apiManagerHelper.populateUserTokenForm(model);
		model.addAttribute("urls", urls);
		model.addAttribute("descriptions", descriptions);

		return PROJECT_LIST;
	}

	@GetMapping(value = "/create")
	public String create(Model model) {
		MapsProject projects = new MapsProject();
		model.addAttribute(ELEMENT, projects);
		model.addAttribute(MAPSSTYLES, mapsStyleService.getStylesForUserWithEmpty(utils.getUserId(), null));
		model.addAttribute(MAPSMAPS, findName(mapsMapService.getMapsForUser(utils.getUserId(), null)));
		return PROJECT_CREATE;
	}

	private List<MapsMapDTO> findName(List<MapsMapDTO> mapsForUser) {
		if (mapsForUser.size() > 0) {
			for (Iterator iterator = mapsForUser.iterator(); iterator.hasNext();) {
				MapsMapDTO mapsmapDTO = (MapsMapDTO) iterator.next();
				JSONObject objexp = new JSONObject(mapsmapDTO.getConfig());
				mapsmapDTO.setName(objexp.getString("name"));

			}
		}
		return mapsForUser;
	}

	@GetMapping(value = "/update/{id}", produces = "text/html")
	public String update(Model model, @PathVariable("id") String id) {
		MapsProject mapsProject = this.mapsProjectService.getById(id);
		if (mapsProject != null) {
			if (!mapsProjectService.hasUserEditPermission(id, this.utils.getUserId())) {
				return ERROR_403;
			}
			model.addAttribute(ELEMENT, mapsProject);
			model.addAttribute(MAPSSTYLES, mapsStyleService.getStylesForUserWithEmpty(utils.getUserId(), null));
			model.addAttribute(MAPSMAPS, findName(this.mapsMapService.getMapsForUser(utils.getUserId(), null)));
			return PROJECT_CREATE;
		} else {
			return ERROR_404;
		}
	}

	@PostMapping(value = "/create", produces = "text/html")
	public String save(@Valid MapsProject mapsProject, BindingResult bindingResult, Model model,
			HttpServletRequest httpServletRequest, RedirectAttributes redirect) {
		if (bindingResult.hasErrors()) {
			log.debug("Some mapsProject properties missing");
			mapsProject.setId(null);
			model.addAttribute(ELEMENT, mapsProject);
			model.addAttribute(MAPSSTYLES, mapsStyleService.getStylesForUserWithEmpty(utils.getUserId(), null));
			model.addAttribute(MAPSMAPS, findName(this.mapsMapService.getMapsForUser(utils.getUserId(), null)));
			model.addAttribute(MESSAGE, utils.getMessage("mapsProject.validation.error", ""));
			return PROJECT_CREATE;
		}
		// Error identification exist
		List<MapsProject> list = this.mapsProjectService.getByIdentifier(mapsProject.getIdentification());
		if (list.size() > 0) {
			mapsProject.setId(null);
			model.addAttribute(MESSAGE, utils.getMessage("gen.identifier.exist", ""));
			mapsProject.setId(null);
			model.addAttribute(MAPSSTYLES, mapsStyleService.getStylesForUserWithEmpty(utils.getUserId(), null));
			model.addAttribute(MAPSMAPS, findName(this.mapsMapService.getMapsForUser(utils.getUserId(), null)));
			model.addAttribute(ELEMENT, mapsProject);
			return PROJECT_CREATE;
		}
		try {
			mapsProject.setUser(userService.getUser(utils.getUserId()));
			this.mapsProjectService.save(mapsProject);
			return REDIRECT_PROJECT_LIST;
		} catch (final MapsProjectServiceException e) {
			utils.addRedirectException(e, redirect);
			mapsProject.setId(null);
			model.addAttribute(MESSAGE, utils.getMessage("dashboardConf.validation.error", ""));
			model.addAttribute(MAPSSTYLES, mapsStyleService.getStylesForUserWithEmpty(utils.getUserId(), null));
			model.addAttribute(MAPSMAPS, findName(this.mapsMapService.getMapsForUser(utils.getUserId(), null)));
			model.addAttribute(ELEMENT, mapsProject);
			return PROJECT_CREATE;
		}
	}

	@PostMapping(value = "/update/{id}", produces = "text/html")
	public String update(Model model, @PathVariable("id") String id, @Valid MapsProject mapsProject,
			BindingResult bindingResult, RedirectAttributes redirect) {

		if (bindingResult.hasErrors()) {
			log.debug("Some MapsProject properties missing");
			utils.addRedirectMessage("gen.update.error", redirect);
			return "redirect:/mapsproject/update/" + id;
		}
		if (!mapsProjectService.hasUserEditPermission(id, this.utils.getUserId()))
			return ERROR_403;
		try {

			this.mapsProjectService.update(mapsProject);
		} catch (MapsProjectServiceException e) {
			log.error("Cannot update MapsProject. {}", e.getMessage());
			utils.addRedirectException(e, redirect);
			return "redirect:/mapsproject/update/" + id;
		}
		resourcesInUseService.removeByUser(id, utils.getUserId());
		return REDIRECT_PROJECT_LIST;
	}

	@GetMapping(value = "/freeResource/{id}")
	public @ResponseBody void freeResource(@PathVariable("id") String id) {
		resourcesInUseService.removeByUser(id, utils.getUserId());
		log.info("free resource", id);
	}

	@DeleteMapping("/{id}")
	public String deleteProject(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		try {
			this.entityDeletionService.deleteMapsProject(id,false, utils.getUserId());
		} catch (final RuntimeException e) {
			utils.addRedirectException(e, redirect);
		}
		return REDIRECT_PROJECT_LIST;
	}
	@DeleteMapping("/full/{id}")
	public String deleteProjectAndDependencies(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		try {
			this.entityDeletionService.deleteMapsProject(id,true, utils.getUserId());
		} catch (final RuntimeException e) {
			utils.addRedirectException(e, redirect);
		}
		return REDIRECT_PROJECT_LIST;
	}

	@PostMapping(value = { "/clone" })
	public ResponseEntity<String> clone(Model model, @RequestParam String elementid,
			@RequestParam String identification) {

		try {
			String idElem = "";
			final String userId = utils.getUserId();
			 User sessionUser = userService.getUser( utils.getUserId());
			idElem = mapsProjectService.clone(mapsProjectService.getByIdANDUser(elementid, userId), identification,
					sessionUser,sessionUser);
			final List<MapsProject> opt = mapsProjectService.getByIdentifier(idElem);
			if (opt == null && opt.size() == 0) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			}
			return new ResponseEntity<>(identification, HttpStatus.OK);
		} catch (final Exception e) {
			log.error(e.getMessage());
			return new ResponseEntity<>("{\"status\" : \"fail\"}", HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping(value = { "/cloneusers" })
	public ResponseEntity<String> clone(Model model, @RequestParam String elementid,
			@RequestParam String identification, @RequestParam String users) {

		try {
			String idElem = "";
			final String userId = utils.getUserId();
			  User sessionUser = userService.getUser(userId);
			JSONArray usersJson = new JSONArray(users);

			for (int i = 0; i < usersJson.length(); i++) {

				idElem = mapsProjectService.clone(mapsProjectService.getByIdANDUser(elementid, userId),
						randomIdentfication(identification), userService.getUser(usersJson.getString(i)),sessionUser);
				final List<MapsProject> opt = mapsProjectService.getByIdentifier(idElem);
				if (opt == null && opt.size() == 0) {
					return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
				}
			}
			return new ResponseEntity<>(identification, HttpStatus.OK);
		} catch (final Exception e) {
			log.error(e.getMessage());
			return new ResponseEntity<>("{\"status\" : \"fail\"}", HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = "/show/{id}", produces = "text/html")
	public String show(Model model, @PathVariable("id") String id) {
		MapsProject mapsProject = this.mapsProjectService.getById(id);
		if (mapsProject != null) {

			if (!utils.isAdministrator() && !mapsProject.isPublic()) {
				if (!mapsProject.getUser().getUserId().equals(utils.getUserId())) {
					return ERROR_404;
				}
			}

			model.addAttribute(ELEMENT, mapsProject);
			model.addAttribute(MAPSMAPS, findName(mapsMapService.getMapsForUser(utils.getUserId(), null)));
			return PROJECT_SHOW;
		} else {
			return ERROR_404;
		}
	}

	@Transactional
	@RequestMapping(value = "/importMapsProject", method = RequestMethod.POST)
	@ResponseBody
	public String importMapsProject(@RequestBody String importJSON,
			@RequestParam(required = false, defaultValue = "true") boolean overwrite) {
		try {

			final String identification = mapsProjectService.importMapsProject(importJSON, overwrite,
					userService.getUser(utils.getUserId()));

			final JSONObject response = new JSONObject();
			response.put("status", HttpStatus.OK);
			response.put("message", identification);
			return response.toString();

		} catch (final MapsProjectServiceException e) {
			log.error("Cannot import maps project: ", e);
			final JSONObject response = new JSONObject();
			response.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
			response.put("message", e.getMessage());
			return response.toString();

		} catch (final Exception e) {
			log.error("Cannot import maps project: ", e);
			final JSONObject response = new JSONObject();
			response.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
			response.put("message", e.getMessage());
			return response.toString();
		}
	}

	@RequestMapping(value = "/exportMapsProject/{id}", method = RequestMethod.GET, produces = "text/html")
	@ResponseBody
	public ResponseEntity<byte[]> exportMapsProject(@PathVariable("id") String id, Model uiModel) {
		String export = null;

		final ObjectMapper mapper = new ObjectMapper();
		try {
			export = mapsProjectService.exportMapsProject(id, userService.getUser(utils.getUserId()));
		} catch (final JSONException e) {
			log.error("Exception parsing answer in download MapsProjects");
			throw new DashboardServiceException("Exception parsing answer in download dashboard: " + e);
		}

		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		headers.set("Content-Disposition", "attachment; filename=\"" + id + ".json\"");

		return new ResponseEntity<>(export.getBytes(StandardCharsets.UTF_8), headers, HttpStatus.OK);

	}

	private String randomIdentfication(String text) {
		Random random = new Random();
		int rn = random.nextInt(999) + 0;
		String post = "" + new Date().getTime() + rn;
		return text + "-" + post.substring(post.length() - 5);
	}
}
