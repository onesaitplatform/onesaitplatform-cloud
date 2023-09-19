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
package com.minsait.onesait.platform.controlpanel.controller.mapsstyle;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.minsait.onesait.platform.config.model.MapsStyle;
import com.minsait.onesait.platform.config.services.deletion.EntityDeletionService;
import com.minsait.onesait.platform.config.services.exceptions.MapsStyleServiceException;
import com.minsait.onesait.platform.config.services.mapsmap.dto.MapsMapDTO;
import com.minsait.onesait.platform.config.services.mapsstyle.MapsStyleService;
import com.minsait.onesait.platform.config.services.mapsstyle.dto.MapsStyleDTO;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.services.resourcesinuse.ResourcesInUseService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@RequestMapping("/mapsstyle")
@Controller
@Slf4j
public class MapsStyleController {

	@Autowired
	private AppWebUtils utils;
	@Autowired
	private MapsStyleService mapsStyleService;
	@Autowired
	private UserService userService;
	@Autowired
	private ResourcesInUseService resourcesInUseService;
	@Autowired
	private EntityDeletionService entityDeletionService;

	private static final String STYLE_CREATE = "mapsstyle/create";
	private static final String STYLE_SHOW = "mapsstyle/show";
	private static final String STYLE_LIST = "mapsstyle/list";
	private static final String MAPSSTYLES = "mapsstyles";
	private static final String ELEMENT = "element";
	private static final String MESSAGE = "message";
	private static final String ERROR_403 = "error/403";
	private static final String ERROR_404 = "error/404";

	private static final String REDIRECT_STYLE_LIST = "redirect:/mapsstyle/list";

	@RequestMapping(value = "/list", produces = "text/html")
	public String list(Model model, @RequestParam(required = false, name = "name") String identification) {
		model.addAttribute(MAPSSTYLES, mapsStyleService.getStylesForUser(utils.getUserId(), identification));
		return STYLE_LIST;
	}

	 
	@GetMapping(value = "/create")
	public String create(Model model) {
		MapsStyle maps = new MapsStyle();

		model.addAttribute(ELEMENT, maps);
		return STYLE_CREATE;
	}

	@GetMapping(value = "/update/{id}", produces = "text/html")
	public String update(Model model, @PathVariable("id") String id) {
		MapsStyle mapsStyle = this.mapsStyleService.getById(id);
		if (mapsStyle != null) {
			if (!mapsStyleService.hasUserEditPermission(id, this.utils.getUserId())) {
				return ERROR_403;
			}
			model.addAttribute(ELEMENT, mapsStyle);
			return STYLE_CREATE;
		} else {
			return ERROR_404;
		}
	}

	@PostMapping(value = "/create", produces = "text/html")
	public String save(@Valid MapsStyle mapsStyle, BindingResult bindingResult, Model model,
			HttpServletRequest httpServletRequest, RedirectAttributes redirect) {
		if (bindingResult.hasErrors()) {
			log.debug("Some mapsStyle properties missing");
			mapsStyle.setId(null);
			model.addAttribute(ELEMENT, mapsStyle);
			model.addAttribute(MESSAGE, utils.getMessage("mapsStyle.validation.error", ""));
			return STYLE_CREATE;
		}
		// Error identification exist
		List<MapsStyle> list = this.mapsStyleService.getByIdentifier(mapsStyle.getIdentification());
		if (list.size() > 0) {
			mapsStyle.setId(null);
			model.addAttribute(MESSAGE, utils.getMessage("gen.identifier.exist", ""));
			mapsStyle.setId(null);
			model.addAttribute(ELEMENT, mapsStyle);

			return STYLE_CREATE;
		}
		try {
			mapsStyle.setUser(userService.getUser(utils.getUserId()));
			this.mapsStyleService.save(mapsStyle);
			return REDIRECT_STYLE_LIST;
		} catch (final MapsStyleServiceException e) {
			utils.addRedirectException(e, redirect);
			mapsStyle.setId(null);
			model.addAttribute(MESSAGE, utils.getMessage("dashboardConf.validation.error", ""));
			model.addAttribute(ELEMENT, mapsStyle);
			return STYLE_CREATE;
		}
	}

	@PostMapping(value = "/update/{id}", produces = "text/html")
	public String update(Model model, @PathVariable("id") String id, @Valid MapsStyle mapsStyle,
			BindingResult bindingResult, RedirectAttributes redirect) {

		if (bindingResult.hasErrors()) {
			log.debug("Some MapsStyle properties missing");
			utils.addRedirectMessage("gen.update.error", redirect);
			return "redirect:/mapsstyle/update/" + id;
		}
		if (!mapsStyleService.hasUserEditPermission(id, this.utils.getUserId()))
			return ERROR_403;
		try {

			this.mapsStyleService.update(mapsStyle);
		} catch (MapsStyleServiceException e) {
			log.error("Cannot update MapsStyle. {}", e.getMessage());
			utils.addRedirectException(e, redirect);
			return "redirect:/mapsstyle/update/" + id;
		}
		resourcesInUseService.removeByUser(id, utils.getUserId());
		return REDIRECT_STYLE_LIST;
	}

	@GetMapping(value = "/freeResource/{id}")
	public @ResponseBody void freeResource(@PathVariable("id") String id) {
		resourcesInUseService.removeByUser(id, utils.getUserId());
		log.info("free resource", id);
	}

	@DeleteMapping("/{id}")
	public String delete(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		try {
			this.entityDeletionService.deleteMapsStyle(id, utils.getUserId());
		} catch (final RuntimeException e) {
			utils.addRedirectException(e, redirect);
		}
		return REDIRECT_STYLE_LIST;
	}

	@PostMapping(value = { "/clone" })
	public ResponseEntity<String> clone(Model model, @RequestParam String elementid,
			@RequestParam String identification) {

		try {
			String idElem = "";
			final String userId = utils.getUserId();

			idElem = mapsStyleService.clone(mapsStyleService.getByIdANDUser(elementid, userId), identification,
					userService.getUser(userId));
			final MapsStyle opt = mapsStyleService.getById(idElem);
			if (opt == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			}

			return new ResponseEntity<>(opt.getId(), HttpStatus.OK);
		} catch (final Exception e) {
			log.error(e.getMessage());
			return new ResponseEntity<>("{\"status\" : \"fail\"}", HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = "/show/{id}", produces = "text/html")
	public String show(Model model, @PathVariable("id") String id) {
		MapsStyle mapsStyle = this.mapsStyleService.getById(id);
		if (mapsStyle == null) {
			List<MapsStyle> mapsSt = this.mapsStyleService.getByIdentifier(id);
			if (mapsSt.size() > 0) {
				mapsStyle = mapsSt.get(0);
			}
		}
		if (mapsStyle != null) {
			model.addAttribute(ELEMENT, mapsStyle);
			return STYLE_SHOW;
		} else {
			return ERROR_404;
		}
	}

	@GetMapping(value = "getStyleList")
	public @ResponseBody List<MapsStyleDTO> getStyleList() {
		return mapsStyleService.getStylesForUserWithEmpty(utils.getUserId(), null);
	}

}
