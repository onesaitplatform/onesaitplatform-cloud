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
package com.minsait.onesait.platform.controlpanel.controller.gadget;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.minsait.onesait.platform.config.model.Gadget;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.model.GadgetMeasure;
import com.minsait.onesait.platform.config.model.GadgetTemplate;
import com.minsait.onesait.platform.config.services.exceptions.GadgetDatasourceServiceException;
import com.minsait.onesait.platform.config.services.exceptions.GadgetServiceException;
import com.minsait.onesait.platform.config.services.gadget.GadgetDatasourceService;
import com.minsait.onesait.platform.config.services.gadget.GadgetService;
import com.minsait.onesait.platform.config.services.gadget.dto.GadgetDTO;
import com.minsait.onesait.platform.config.services.gadget.dto.GadgetDatasourceDTO;
import com.minsait.onesait.platform.config.services.gadget.dto.GadgetMeasureDTO;
import com.minsait.onesait.platform.config.services.gadget.dto.OntologyDTO;
import com.minsait.onesait.platform.config.services.gadgettemplate.GadgetTemplateService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.services.resourcesinuse.ResourcesInUseService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@RequestMapping("/gadgets")
@Controller
@Slf4j
public class GadgetController {

	private static final String REDIRECT_ERROR = "error/403";
	private static final String MEASURES = "measures";

	@Autowired
	private GadgetService gadgetService;

	@Autowired
	private UserService userService;

	@Autowired
	private GadgetDatasourceService gadgetDatasourceService;

	@Autowired
	private AppWebUtils utils;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private ResourcesInUseService resourcesInUseService;

	@Autowired
	private GadgetTemplateService gadgetTemplateService;

	private static final String IFRAME_STR = "iframe";
	private static final String GADGET_STR = "gadget";
	private static final String DATASOURCES_STR = "datasources";
	private static final String ONTOLOGIES_STR = "ontologies";
	private static final String GADGETS_CREATE = "gadgets/create";
	private static final String GADGETS_SHOW = "gadgets/show";
	private static final String REDIRECT_GADGETS_CREATE = "redirect:/gadgets/create";
	private static final String REDIRECT_GADGETS_LIST = "redirect:/gadgets/list";
	private static final String ERROR_TRUE_STR = "{\"error\":\"true\"}";
	private static final String GADGET_TEMPLATE_TYPE = "template";

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	@RequestMapping(value = "/list", produces = "text/html")
	public String list(Model uiModel, HttpServletRequest request) {

		uiModel.addAttribute("user", utils.getUserId());
		uiModel.addAttribute("userRole", utils.getRole());

		uiModel.addAttribute("gadgetTypes", gadgetService.getGadgetTypes());

		String identification = request.getParameter("name");
		String type = request.getParameter("type");

		if (identification != null && identification.equals("")) {
			identification = null;
		}
		if (type != null && type.equals("")) {
			type = null;
		}

		final List<Gadget> gadget = gadgetService.findGadgetWithIdentificationAndType(identification, type,
				utils.getUserId());
		// gadgets: tiene que coincidir con el del list
		uiModel.addAttribute("gadgets", gadget);

		// Gadget Templates
		String description = request.getParameter("description");
		if (description != null && description.equals("")) {
			description = null;
		}

		List<GadgetTemplate> gadgetTemplate = new ArrayList<GadgetTemplate>();

		if (type == null || type.equals(GADGET_TEMPLATE_TYPE)) {
			gadgetTemplate = this.gadgetTemplateService
					.findGadgetTemplateWithIdentificationAndDescription(identification, description, utils.getUserId());
		}

		uiModel.addAttribute("gadgetTemplates", gadgetTemplate);
		return "gadgets/list";

	}

	@RequestMapping(method = RequestMethod.POST, value = "getNamesForAutocomplete")
	public @ResponseBody List<String> getNamesForAutocomplete() {
		return gadgetService.getAllIdentifications();
	}

	@GetMapping(value = "getUserGadgetsByType/{type}")
	public @ResponseBody List<Gadget> getUserGadgetsByType(@PathVariable("type") String type) {
		return gadgetService.getUserGadgetsByType(utils.getUserId(), type);
	}

	@GetMapping(value = "getGadgetConfigById/{gadgetId}")
	public @ResponseBody Gadget getGadgetConfigById(@PathVariable("gadgetId") String gadgetId) {
		return gadgetService.getGadgetById(utils.getUserId(), gadgetId);
	}

	@GetMapping(value = "getGadgetMeasuresByGadgetId/{gadgetId}")
	public @ResponseBody List<GadgetMeasure> getGadgetMeasuresByGadgetId(@PathVariable("gadgetId") String gadgetId) {
		return gadgetService.getGadgetMeasuresByGadgetId(utils.getUserId(), gadgetId);
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/create", produces = "text/html")
	public String createGadget(Model model) {
		model.addAttribute(IFRAME_STR, Boolean.FALSE);
		model.addAttribute(GADGET_STR, new Gadget());
		model.addAttribute(DATASOURCES_STR, gadgetDatasourceService.getUserGadgetDatasourcesForList(utils.getUserId()));
		model.addAttribute(ONTOLOGIES_STR, getOntologiesDTO());
		return GADGETS_CREATE;

	}

	@GetMapping(value = "/createiframe/{type}", produces = "text/html")
	public String createGadgetWithOrigin(Model model, @PathVariable("type") String type) {
		model.addAttribute("type", type);
		model.addAttribute(IFRAME_STR, Boolean.TRUE);
		model.addAttribute(GADGET_STR, new Gadget());
		model.addAttribute(DATASOURCES_STR, gadgetDatasourceService.getUserGadgetDatasourcesForList(utils.getUserId()));
		model.addAttribute(ONTOLOGIES_STR, getOntologiesDTO());
		return GADGETS_CREATE;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PostMapping(value = "/create", produces = "text/html")
	public String saveGadget(@Valid Gadget gadget, BindingResult bindingResult, String jsonMeasures,
			String datasourcesMeasures, Model uiModel, HttpServletRequest httpServletRequest,
			RedirectAttributes redirect) {
		if (bindingResult.hasErrors()) {
			log.debug("Some gadget properties missing");
			utils.addRedirectMessage("gadgets.validation.error", redirect);
			return REDIRECT_GADGETS_CREATE;
		}
		try {
			gadget.setUser(userService.getUser(utils.getUserId()));
			gadgetService.createGadget(gadget, datasourcesMeasures, jsonMeasures);
		} catch (final GadgetDatasourceServiceException e) {
			log.debug("Cannot create gadget datasource");
			utils.addRedirectMessage("gadgetDatasource.create.error", redirect);
			return REDIRECT_GADGETS_CREATE;
		}
		return REDIRECT_GADGETS_LIST;

	}

	@PostMapping(value = { "/createiframe" }, produces = "application/json")
	public @ResponseBody String saveGadgetIframe(Gadget gadget, String jsonMeasures, String datasourcesMeasures) {
		Gadget g;
		try {
			gadget.setUser(userService.getUser(utils.getUserId()));
			g = gadgetService.createGadget(gadget, datasourcesMeasures, jsonMeasures);
		} catch (final GadgetDatasourceServiceException e) {
			log.debug("Cannot create gadget");

			return ERROR_TRUE_STR;
		}
		return "{\"id\":\"" + g.getId() + "\",\"identification\":\"" + g.getIdentification() + "\",\"type\":\""
				+ g.getType() + "\"}";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/update/{gadgetId}", produces = "text/html")
	public String createGadget(Model model, @PathVariable("gadgetId") String gadgetId) {
		if (!gadgetService.hasUserPermission(gadgetId, utils.getUserId()))
			return REDIRECT_ERROR;
		model.addAttribute(GADGET_STR, mapGadgetToGadgetDTO(gadgetService.getGadgetById(utils.getUserId(), gadgetId)));
		model.addAttribute(MEASURES, mapGadgetMeasureListToGadgetMeasureDTOList(
				gadgetService.getGadgetMeasuresByGadgetId(utils.getUserId(), gadgetId)));
		model.addAttribute(DATASOURCES_STR, gadgetDatasourceService.getUserGadgetDatasourcesForList(utils.getUserId()));
		model.addAttribute(ONTOLOGIES_STR, getOntologiesDTO());
		model.addAttribute(IFRAME_STR, Boolean.FALSE);
		model.addAttribute("dataSourceAccessType",
				gadgetDatasourceService.getAccessType(gadgetService
						.getGadgetMeasuresByGadgetId(utils.getUserId(), gadgetId).get(0).getDatasource().getId(),
						utils.getUserId()));

		model.addAttribute(ResourcesInUseService.RESOURCEINUSE,
				resourcesInUseService.isInUse(gadgetId, utils.getUserId()));
		resourcesInUseService.put(gadgetId, utils.getUserId());

		return GADGETS_CREATE;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/view/{gadgetId}", produces = "text/html")
	public String showGadget(Model model, @PathVariable("gadgetId") String gadgetId) {
		if (!gadgetService.hasUserViewPermission(gadgetId, utils.getUserId()))
			return REDIRECT_ERROR;
		model.addAttribute(GADGET_STR, mapGadgetToGadgetDTO(gadgetService.getGadgetById(utils.getUserId(), gadgetId)));
		model.addAttribute(MEASURES, mapGadgetMeasureListToGadgetMeasureDTOList(
				gadgetService.getGadgetMeasuresByGadgetId(utils.getUserId(), gadgetId)));
		model.addAttribute(DATASOURCES_STR, gadgetDatasourceService.getUserGadgetDatasourcesForList(utils.getUserId()));
		model.addAttribute(ONTOLOGIES_STR, getOntologiesDTO());
		model.addAttribute(IFRAME_STR, Boolean.FALSE);
		return GADGETS_SHOW;
	}

	@GetMapping(value = "/hasUserPermission/{gadgetId}", produces = "application/json")
	@ResponseBody
	public Boolean hasUserPermission(Model model, @PathVariable("gadgetId") String gadgetId) {
		return gadgetService.hasUserPermission(gadgetId, utils.getUserId());
	}

	@GetMapping(value = "/updateiframe/{gadgetId}", produces = "text/html")
	public String updateGadgetIframe(Model model, @PathVariable("gadgetId") String gadgetId) {
		model.addAttribute(GADGET_STR, mapGadgetToGadgetDTO(gadgetService.getGadgetById(utils.getUserId(), gadgetId)));
		model.addAttribute(MEASURES, mapGadgetMeasureListToGadgetMeasureDTOList(
				gadgetService.getGadgetMeasuresByGadgetId(utils.getUserId(), gadgetId)));
		model.addAttribute(DATASOURCES_STR, gadgetDatasourceService.getUserGadgetDatasourcesForList(utils.getUserId()));
		model.addAttribute(ONTOLOGIES_STR, getOntologiesDTO());
		model.addAttribute(IFRAME_STR, Boolean.TRUE);
		model.addAttribute("dataSourceAccessType",
				gadgetDatasourceService.getAccessType(gadgetService
						.getGadgetMeasuresByGadgetId(utils.getUserId(), gadgetId).get(0).getDatasource().getId(),
						utils.getUserId()));
		model.addAttribute(ResourcesInUseService.RESOURCEINUSEDASHBOARD,
				resourcesInUseService.isInUse(gadgetId, utils.getUserId()));
		resourcesInUseService.put(gadgetId, utils.getUserId());

		return GADGETS_CREATE;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PostMapping(value = { "/updateiframe/{gadgetId}" }, produces = "application/json")
	public @ResponseBody String saveUpadteGadgetIframe(@PathVariable("gadgetId") String gadgetId, Gadget gadget,
			String jsonMeasures, String datasourcesMeasures) {
		if (!gadgetService.hasUserPermission(gadgetId, utils.getUserId())) {
			return REDIRECT_ERROR;
		}

		try {
			gadget.setUser(userService.getUser(utils.getUserId()));
			gadgetService.updateGadget(gadget, datasourcesMeasures, jsonMeasures);
			resourcesInUseService.removeByUser(gadgetId, utils.getUserId());
		} catch (final GadgetDatasourceServiceException e) {
			log.debug("Cannot updateiframe gadget");

			return ERROR_TRUE_STR;
		}
		return "{\"id\":\"" + gadgetId + "\"}";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@DeleteMapping("/{id}")
	public String delete(Model model, @PathVariable("id") String id, RedirectAttributes ra) {
		try {
			gadgetService.deleteGadget(id, utils.getUserId());
		} catch (final RuntimeException e) {
			utils.addRedirectException(e, ra);
		}
		return REDIRECT_GADGETS_LIST;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PostMapping("/gadgettemplates/{id}")
	public String delete(Model model, @PathVariable("id") String id) {
		log.info("Controlador");
		this.gadgetTemplateService.deleteGadgetTemplate(id, utils.getUserId());
		return REDIRECT_GADGETS_LIST;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PutMapping(value = "/update/{id}", produces = "text/html")
	public String updateGadget(Model model, @PathVariable("id") String id, @Valid Gadget gadget, String jsonMeasures,
			String datasourcesMeasures, BindingResult bindingResult, RedirectAttributes redirect) {

		if (bindingResult.hasErrors()) {
			log.debug("Some Gadget properties missing");
			utils.addRedirectMessage("gadgets.validation.error", redirect);
			return "redirect:/gadgets/update/" + id;
		}
		if (!gadgetService.hasUserPermission(id, utils.getUserId()))
			return REDIRECT_ERROR;
		try {
			gadgetService.updateGadget(gadget, datasourcesMeasures, jsonMeasures);
			resourcesInUseService.removeByUser(id, utils.getUserId());
		} catch (final GadgetServiceException e) {
			log.debug("Cannot update gadget datasource");
			utils.addRedirectMessage("gadgetDatasource.update.error", redirect);
			return REDIRECT_GADGETS_CREATE;
		}
		return REDIRECT_GADGETS_LIST;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PostMapping(value = { "/existGadget" }, produces = "application/json")
	public @ResponseBody String existGadget(String identification) {

		try {
			return "{\"exist\":\"" + gadgetService.existGadgetWithIdentification(identification) + "\"}";
		} catch (final GadgetDatasourceServiceException e) {
			log.debug("Cannot create gadget");
			return ERROR_TRUE_STR;
		}
	}

	private List<OntologyDTO> getOntologiesDTO() {
		final Set<com.minsait.onesait.platform.config.services.ontology.dto.OntologyDTO> ontologies = new LinkedHashSet<>(
				ontologyService.getAllOntologiesForListWithProjectsAccess(utils.getUserId()));
		return ontologies
				.stream().map(o -> OntologyDTO.builder().identification(o.getIdentification())
						.description(o.getDescription()).user(o.getUser().getUserId()).build())
				.collect(Collectors.toList());

		/**
		 * final List<OntologyDTO> listOntologies = new ArrayList<OntologyDTO>(); final
		 * List<Ontology> ontologies =
		 * ontologyService.getOntologiesByUserId(utils.getUserId()); if (ontologies !=
		 * null && ontologies.size() > 0) { for (final Iterator<Ontology> iterator =
		 * ontologies.iterator(); iterator.hasNext();) { final Ontology ontology =
		 * iterator.next(); final OntologyDTO oDTO = new OntologyDTO();
		 * oDTO.setIdentification(ontology.getIdentification());
		 * oDTO.setDescription(ontology.getDescription());
		 * oDTO.setUser(ontology.getUser().getUserId()); listOntologies.add(oDTO); } }
		 * return listOntologies;
		 **/
	}

	private GadgetDTO mapGadgetToGadgetDTO(Gadget gadget) {
		final GadgetDTO gDTO = new GadgetDTO();
		gDTO.setConfig(gadget.getConfig());
		gDTO.setDescription(gadget.getDescription());
		gDTO.setIdentification(gadget.getIdentification());
		gDTO.setPublic(gadget.isPublic());
		gDTO.setType(gadget.getType());
		gDTO.setId(gadget.getId());
		return gDTO;

	}

	private List<GadgetDatasourceDTO> mapGadgetDataSourceListToGadgetDataSourceDTOList(
			List<GadgetDatasource> listGadgetDataSource) {
		final List<GadgetDatasourceDTO> listGDS = new ArrayList<>();
		if (listGadgetDataSource != null && !listGadgetDataSource.isEmpty()) {
			for (final Iterator<GadgetDatasource> iterator = listGadgetDataSource.iterator(); iterator.hasNext();) {
				final GadgetDatasource gadgetDataSource = iterator.next();
				listGDS.add(mapGadgetDataSourceToGadgetDataSourceDTO(gadgetDataSource));
			}
		}
		return listGDS;

	}

	private GadgetDatasourceDTO mapGadgetDataSourceToGadgetDataSourceDTO(GadgetDatasource gadgetDataSource) {
		final GadgetDatasourceDTO gDTO = new GadgetDatasourceDTO();
		gDTO.setConfig(gadgetDataSource.getConfig());
		gDTO.setDescription(gadgetDataSource.getDescription());
		gDTO.setIdentification(gadgetDataSource.getIdentification());
		gDTO.setId(gadgetDataSource.getId());
		gDTO.setDbtype(gadgetDataSource.getDbtype());
		gDTO.setMaxvalues(gadgetDataSource.getMaxvalues());
		gDTO.setMode(gadgetDataSource.getMode());
		final OntologyDTO oDTO = new OntologyDTO();
		if (gadgetDataSource.getOntology() != null) {
			oDTO.setDescription(gadgetDataSource.getOntology().getDescription());
			oDTO.setIdentification(gadgetDataSource.getOntology().getIdentification());
			oDTO.setUser(gadgetDataSource.getOntology().getUser().getUserId());
			gDTO.setOntology(oDTO);
		}
		gDTO.setQuery(gadgetDataSource.getQuery());
		gDTO.setRefresh(gadgetDataSource.getRefresh());
		return gDTO;

	}

	private List<GadgetMeasureDTO> mapGadgetMeasureListToGadgetMeasureDTOList(List<GadgetMeasure> listGadgetMeasure) {
		final List<GadgetMeasureDTO> listGM = new ArrayList<>();
		if (listGadgetMeasure != null && !listGadgetMeasure.isEmpty()) {
			for (final Iterator<GadgetMeasure> iterator = listGadgetMeasure.iterator(); iterator.hasNext();) {
				final GadgetMeasure gadgetM = iterator.next();
				listGM.add(mapGadgetMeasuretoGadgetMeasureDTO(gadgetM));
			}
		}
		return listGM;

	}

	private GadgetMeasureDTO mapGadgetMeasuretoGadgetMeasureDTO(GadgetMeasure gadgetMeasure) {
		final GadgetMeasureDTO gDTO = new GadgetMeasureDTO();
		gDTO.setId(gadgetMeasure.getId());
		gDTO.setDatasource(mapGadgetDataSourceToGadgetDataSourceDTO(gadgetMeasure.getDatasource()));
		gDTO.setGadget(mapGadgetToGadgetDTO(gadgetMeasure.getGadget()));
		gDTO.setConfig(gadgetMeasure.getConfig());

		return gDTO;
	}

	@GetMapping(value = "/freeResource/{id}")
	public @ResponseBody void freeResource(@PathVariable("id") String id) {
		resourcesInUseService.removeByUser(id, utils.getUserId());
		log.info("free resource", id);
	}

}
