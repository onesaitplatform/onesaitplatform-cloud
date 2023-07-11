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
package com.minsait.onesait.platform.controlpanel.controller.mapsmap;

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

import com.minsait.onesait.platform.config.model.MapsMap;
import com.minsait.onesait.platform.config.services.deletion.EntityDeletionService;
import com.minsait.onesait.platform.config.services.exceptions.MapsMapServiceException;
import com.minsait.onesait.platform.config.services.gis.layer.LayerService;
import com.minsait.onesait.platform.config.services.mapslayer.MapsLayerService;
import com.minsait.onesait.platform.config.services.mapslayer.dto.MapsLayerDTO;
import com.minsait.onesait.platform.config.services.mapsmap.MapsMapService;
import com.minsait.onesait.platform.config.services.mapsmap.dto.MapsMapDTO;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.services.resourcesinuse.ResourcesInUseService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@RequestMapping("/mapsmap")
@Controller
@Slf4j
public class MapsMapController {

	@Autowired
	private AppWebUtils utils;
	@Autowired
	private UserService userService;
	@Autowired
	private MapsMapService mapsMapService;
	@Autowired
	private MapsLayerService mapsLayerService;
	@Autowired
	private ResourcesInUseService resourcesInUseService;
	@Autowired
	private EntityDeletionService entityDeletionService;

	@Autowired
	LayerService layerService;

	private static final String MAP_CREATE = "mapsmap/create";
	private static final String MAP_SHOW = "mapsmap/show";
	private static final String MAP_LIST = "mapsmap/list";
	private static final String MAPSLAYERS = "mapslayers";
	private static final String ELEMENT = "element";
	private static final String MESSAGE = "message";
	private static final String ERROR_403 = "error/403";
	private static final String ERROR_404 = "error/404";
	private static final String REDIRECT_MAP_LIST = "redirect:/mapsmap/list";

	@RequestMapping(value = "/list", produces = "text/html")
	public String list(Model model, @RequestParam(required = false, name = "name") String identification) {
		model.addAttribute("mapsmaps",
				this.findNameMap(mapsMapService.getMapsForUser(utils.getUserId(), identification)));
		return MAP_LIST;
	}

	private List<MapsMapDTO> findNameMap(List<MapsMapDTO> mapsForUser) {
		if (mapsForUser.size() > 0) {
			for (Iterator iterator = mapsForUser.iterator(); iterator.hasNext();) {
				MapsMapDTO mapsmapDTO = (MapsMapDTO) iterator.next();
				JSONObject objexp = new JSONObject(mapsmapDTO.getConfig());
				mapsmapDTO.setName(objexp.getString("name"));

			}
		}
		return mapsForUser;
	}

	@GetMapping(value = "/create")
	public String create(Model model) {
		MapsMap maps = new MapsMap();
		model.addAttribute(ELEMENT, maps);
		model.addAttribute(MAPSLAYERS, this.findNameLayer(mapsLayerService.getLayersForUser(utils.getUserId(), null)));
		return MAP_CREATE;
	}

	private List<MapsLayerDTO> findNameLayer(List<MapsLayerDTO> layersForUser) {
		if (layersForUser.size() > 0) {
			for (Iterator iterator = layersForUser.iterator(); iterator.hasNext();) {
				MapsLayerDTO mapsLayerDTO = (MapsLayerDTO) iterator.next();

				JSONObject objexp = new JSONObject(mapsLayerDTO.getConfig());
				mapsLayerDTO.setName(objexp.getString("layerName"));
				mapsLayerDTO.setType(objexp.getString("layerType"));
			}
		}
		return layersForUser;
	}

	@GetMapping(value = "/update/{id}", produces = "text/html")
	public String update(Model model, @PathVariable("id") String id) {
		MapsMap mapsMap = this.mapsMapService.getById(id);
		if (mapsMap != null) {
			if (!mapsMapService.hasUserEditPermission(id, this.utils.getUserId())) {
				return ERROR_403;
			}
			model.addAttribute(ELEMENT, mapsMap);
			model.addAttribute(MAPSLAYERS,
					this.findNameLayer(this.mapsLayerService.getLayersForUser(utils.getUserId(), null)));
			return MAP_CREATE;
		} else {
			return ERROR_404;
		}
	}

	@PostMapping(value = "/create", produces = "text/html")
	public String save(@Valid MapsMap mapsMap, BindingResult bindingResult, Model model,
			HttpServletRequest httpServletRequest, RedirectAttributes redirect) {
		if (bindingResult.hasErrors()) {
			log.debug("Some mapsMap properties missing");
			mapsMap.setId(null);
			model.addAttribute(ELEMENT, mapsMap);
			model.addAttribute(MAPSLAYERS,
					this.findNameLayer(this.mapsLayerService.getLayersForUser(utils.getUserId(), null)));
			model.addAttribute(MESSAGE, utils.getMessage("mapsMap.validation.error", ""));
			return MAP_CREATE;
		}
		// Error identification exist
		List<MapsMap> list = this.mapsMapService.getByIdentifier(mapsMap.getIdentification());
		if (list.size() > 0) {
			mapsMap.setId(null);
			model.addAttribute(MESSAGE, utils.getMessage("gen.identifier.exist", ""));
			mapsMap.setId(null);
			model.addAttribute(MAPSLAYERS,
					this.findNameLayer(this.mapsLayerService.getLayersForUser(utils.getUserId(), null)));
			model.addAttribute(ELEMENT, mapsMap);
			return MAP_CREATE;
		}
		try {
			mapsMap.setUser(userService.getUser(utils.getUserId()));
			this.mapsMapService.save(mapsMap);
			return REDIRECT_MAP_LIST;
		} catch (final MapsMapServiceException e) {
			utils.addRedirectException(e, redirect);
			mapsMap.setId(null);
			model.addAttribute(MESSAGE, utils.getMessage("dashboardConf.validation.error", ""));
			model.addAttribute(MAPSLAYERS,
					this.findNameLayer(this.mapsLayerService.getLayersForUser(utils.getUserId(), null)));
			model.addAttribute(ELEMENT, mapsMap);
			return MAP_CREATE;
		}
	}

	@PostMapping(value = "/update/{id}", produces = "text/html")
	public String update(Model model, @PathVariable("id") String id, @Valid MapsMap mapsMap,
			BindingResult bindingResult, RedirectAttributes redirect) {

		if (bindingResult.hasErrors()) {
			log.debug("Some MapsMap properties missing");
			utils.addRedirectMessage("gen.update.error", redirect);
			return "redirect:/mapsmap/update/" + id;
		}
		if (!mapsMapService.hasUserEditPermission(id, this.utils.getUserId()))
			return ERROR_403;
		try {

			this.mapsMapService.update(mapsMap);
		} catch (MapsMapServiceException e) {
			log.error("Cannot update MapsMap. {}", e.getMessage());
			utils.addRedirectException(e, redirect);
			return "redirect:/mapsmap/update/" + id;
		}
		resourcesInUseService.removeByUser(id, utils.getUserId());
		return REDIRECT_MAP_LIST;
	}

	@GetMapping(value = "/freeResource/{id}")
	public @ResponseBody void freeResource(@PathVariable("id") String id) {
		resourcesInUseService.removeByUser(id, utils.getUserId());
		log.info("free resource", id);
	}

	@DeleteMapping("/{id}")
	public String delete(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		try {
			this.entityDeletionService.deleteMapsMap(id, utils.getUserId());
		} catch (final RuntimeException e) {
			utils.addRedirectException(e, redirect);
		}
		return REDIRECT_MAP_LIST;
	}

	@PostMapping(value = { "/clone" })
	public ResponseEntity<String> clone(Model model, @RequestParam String elementid,
			@RequestParam String identification) {

		try {
			String idElem = "";
			final String userId = utils.getUserId();

			idElem = mapsMapService.clone(mapsMapService.getByIdANDUser(elementid, userId), identification,
					userService.getUser(userId));
			final MapsMap opt = mapsMapService.getById(idElem);
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
		MapsMap mapsMap = this.mapsMapService.getById(id);
		if (mapsMap == null) {
			List<MapsMap> mapsSt = this.mapsMapService.getByIdentifier(id);
			if (mapsSt.size() > 0) {
				mapsMap = mapsSt.get(0);
			}
		}
		if (mapsMap != null) {
			model.addAttribute(ELEMENT, mapsMap);
			model.addAttribute(MAPSLAYERS,
					this.findNameLayer(this.mapsLayerService.getLayersForUser(utils.getUserId(), null)));
			return MAP_SHOW;
		} else {
			return ERROR_404;
		}
	}

	@GetMapping(value = "getMapsList")
	public @ResponseBody List<MapsMapDTO> getMapsList() {
		return findNameMap(mapsMapService.getMapsForUser(utils.getUserId(), null));
	}

}
