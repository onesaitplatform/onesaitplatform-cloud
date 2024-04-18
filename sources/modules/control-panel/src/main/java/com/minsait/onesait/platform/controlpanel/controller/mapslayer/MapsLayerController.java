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
package com.minsait.onesait.platform.controlpanel.controller.mapslayer;

import java.util.ArrayList;
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

import com.minsait.onesait.platform.config.model.Layer;
import com.minsait.onesait.platform.config.model.MapsLayer;
import com.minsait.onesait.platform.config.services.deletion.EntityDeletionService;
import com.minsait.onesait.platform.config.services.exceptions.MapsLayerServiceException;
import com.minsait.onesait.platform.config.services.gis.layer.LayerService;
import com.minsait.onesait.platform.config.services.mapslayer.MapsLayerService;
import com.minsait.onesait.platform.config.services.mapslayer.dto.MapsLayerDTO;
import com.minsait.onesait.platform.config.services.mapsstyle.MapsStyleService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.services.resourcesinuse.ResourcesInUseService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@RequestMapping("/mapslayer")
@Controller
@Slf4j
public class MapsLayerController {

	@Autowired
	private AppWebUtils utils;
	@Autowired
	private UserService userService;
	@Autowired
	private MapsStyleService mapsStyleService;
	@Autowired
	private MapsLayerService mapsLayerService;
	@Autowired
	private ResourcesInUseService resourcesInUseService;
	@Autowired
	private EntityDeletionService entityDeletionService;

	@Autowired
	LayerService layerService;

	private static final String LAYER_CREATE = "mapslayer/create";
	private static final String LAYER_SHOW = "mapslayer/show";
	private static final String LAYER_LIST = "mapslayer/list";
	private static final String MAPSSTYLES = "mapsstyles";
	private static final String ELEMENT = "element";
	private static final String MESSAGE = "message";
	private static final String ONTOLOGIES_STR = "ontologies";
	private static final String ERROR_403 = "error/403";
	private static final String ERROR_404 = "error/404";

	private static final String REDIRECT_LAYER_LIST = "redirect:/mapslayer/list";

	@RequestMapping(value = "/list", produces = "text/html")
	public String list(Model model, @RequestParam(required = false, name = "name") String identification) {

		List<MapsLayerDTO> list = this
				.findTypeAndName(mapsLayerService.getLayersForUser(utils.getUserId(), identification));

		model.addAttribute("mapslayers", list);
		return LAYER_LIST;
	}

	private List<MapsLayerDTO> findTypeAndName(List<MapsLayerDTO> layersForUser) {
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

	@GetMapping(value = "/create")
	public String create(Model model) {
		MapsLayer maps = new MapsLayer();
		model.addAttribute(ELEMENT, maps);
		model.addAttribute(MAPSSTYLES, mapsStyleService.getStylesForUserWithEmpty(utils.getUserId(), null));
		model.addAttribute(ONTOLOGIES_STR, getPlatformLayers());
		return LAYER_CREATE;
	}

	@GetMapping(value = "/update/{id}", produces = "text/html")
	public String update(Model model, @PathVariable("id") String id) {
		MapsLayer mapsLayer = this.mapsLayerService.getById(id);
		if (mapsLayer != null) {
			if (!mapsLayerService.hasUserEditPermission(id, this.utils.getUserId())) {
				return ERROR_403;
			}
			model.addAttribute(ELEMENT, mapsLayer);
			model.addAttribute(MAPSSTYLES, mapsStyleService.getStylesForUserWithEmpty(utils.getUserId(), null));
			model.addAttribute(ONTOLOGIES_STR, getPlatformLayers());
			return LAYER_CREATE;
		} else {
			return ERROR_404;
		}
	}

	@PostMapping(value = "/create", produces = "text/html")
	public String save(@Valid MapsLayer mapsLayer, BindingResult bindingResult, Model model,
			HttpServletRequest httpServletRequest, RedirectAttributes redirect) {
		if (bindingResult.hasErrors()) {
			log.debug("Some mapsLayer properties missing");
			mapsLayer.setId(null);
			model.addAttribute(ELEMENT, mapsLayer);
			model.addAttribute(MAPSSTYLES, mapsStyleService.getStylesForUserWithEmpty(utils.getUserId(), null));
			model.addAttribute(ONTOLOGIES_STR, getPlatformLayers());
			model.addAttribute(MESSAGE, utils.getMessage("mapsLayer.validation.error", ""));
			return LAYER_CREATE;
		}
		// Error identification exist
		List<MapsLayer> list = this.mapsLayerService.getByIdentifier(mapsLayer.getIdentification());
		if (list.size() > 0) {
			mapsLayer.setId(null);
			model.addAttribute(MESSAGE, utils.getMessage("gen.identifier.exist", ""));
			mapsLayer.setId(null);
			model.addAttribute(MAPSSTYLES, mapsStyleService.getStylesForUserWithEmpty(utils.getUserId(), null));
			model.addAttribute(ONTOLOGIES_STR, getPlatformLayers());
			model.addAttribute(ELEMENT, mapsLayer);

			return LAYER_CREATE;
		}
		try {
			mapsLayer.setUser(userService.getUser(utils.getUserId()));
			this.mapsLayerService.save(mapsLayer);
			return REDIRECT_LAYER_LIST;
		} catch (final MapsLayerServiceException e) {
			utils.addRedirectException(e, redirect);
			mapsLayer.setId(null);
			model.addAttribute(MESSAGE, utils.getMessage("dashboardConf.validation.error", ""));
			model.addAttribute(MAPSSTYLES, mapsStyleService.getStylesForUserWithEmpty(utils.getUserId(), null));
			model.addAttribute(ONTOLOGIES_STR, getPlatformLayers());
			model.addAttribute(ELEMENT, mapsLayer);
			return LAYER_CREATE;
		}
	}

	@PostMapping(value = "/update/{id}", produces = "text/html")
	public String update(Model model, @PathVariable("id") String id, @Valid MapsLayer mapsLayer,
			BindingResult bindingResult, RedirectAttributes redirect) {

		if (bindingResult.hasErrors()) {
			log.debug("Some MapsLayer properties missing");
			utils.addRedirectMessage("gen.update.error", redirect);
			return "redirect:/mapslayer/update/" + id;
		}
		if (!mapsLayerService.hasUserEditPermission(id, this.utils.getUserId()))
			return ERROR_403;
		try {

			this.mapsLayerService.update(mapsLayer);
		} catch (MapsLayerServiceException e) {
			log.error("Cannot update MapsLayer. {}", e.getMessage());
			utils.addRedirectException(e, redirect);
			return "redirect:/mapslayer/update/" + id;
		}
		resourcesInUseService.removeByUser(id, utils.getUserId());
		return REDIRECT_LAYER_LIST;
	}

	@GetMapping(value = "/freeResource/{id}")
	public @ResponseBody void freeResource(@PathVariable("id") String id) {
		resourcesInUseService.removeByUser(id, utils.getUserId());
		log.info("free resource", id);
	}

	@DeleteMapping("/{id}")
	public String delete(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		try {
			this.entityDeletionService.deleteMapsLayer(id, utils.getUserId());
		} catch (final RuntimeException e) {
			utils.addRedirectException(e, redirect);
		}
		return REDIRECT_LAYER_LIST;
	}

	@PostMapping(value = { "/clone" })
	public ResponseEntity<String> clone(Model model, @RequestParam String elementid,
			@RequestParam String identification) {

		try {
			String idElem = "";
			final String userId = utils.getUserId();

			idElem = mapsLayerService.clone(mapsLayerService.getByIdANDUser(elementid, userId), identification,
					userService.getUser(userId));
			final MapsLayer opt = mapsLayerService.getById(idElem);
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
		MapsLayer mapsLayer = this.mapsLayerService.getById(id);
		if (mapsLayer == null) {
			List<MapsLayer> mapsSt = this.mapsLayerService.getByIdentifier(id);
			if (mapsSt.size() > 0) {
				mapsLayer = mapsSt.get(0);
			}
		}
		if (mapsLayer != null) {
			model.addAttribute(ELEMENT, mapsLayer);

			return LAYER_SHOW;
		} else {
			return ERROR_404;
		}
	}

	@GetMapping(value = "getLayersList")
	public @ResponseBody List<MapsLayerDTO> getLayersList() {
		return this.findTypeAndName(this.mapsLayerService.getLayersForUser(utils.getUserId(), null));
	}

	private List<Layer> getPlatformLayers() {
		List<Layer> layers = new ArrayList<>();
		layers = layerService.findAllLayers(utils.getUserId());

		for (Iterator iterator = layers.iterator(); iterator.hasNext();) {
			Layer layer = (Layer) iterator.next();
			if (layer.getExternalType() != null) {
				iterator.remove();
			}
		}
		return layers;

	}

}
