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
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.dto.GadgetDatasourceForList;
import com.minsait.onesait.platform.config.dto.OntologyForList;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.services.deletion.EntityDeletionService;
import com.minsait.onesait.platform.config.services.exceptions.GadgetDatasourceServiceException;
import com.minsait.onesait.platform.config.services.gadget.GadgetDatasourceService;
import com.minsait.onesait.platform.config.services.gadget.dto.OntologyDTO;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataUnauthorizedException;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.services.resourcesinuse.ResourcesInUseService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.services.QueryToolService;

import lombok.extern.slf4j.Slf4j;

@RequestMapping("/datasources")
@Controller
@Slf4j
public class GadgetDatasourceController {

	@Autowired
	private GadgetDatasourceService gadgetDatasourceService;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private UserService userService;

	@Autowired
	private QueryToolService queryToolService;

	@Autowired
	private EntityDeletionService entityDeletionService;

	@Autowired
	private AppWebUtils utils;

	@Autowired
	private ResourcesInUseService resourcesInUseService;

	private static final String DATASOURCE_STR = "datasource";
	private static final String DATASOURCE_ONT_SEL_STR = "datasourceOntologySelected";
	private static final String ONTOLOGIES_STR = "ontologies";
	private static final String REDIRECT_DATAS_CREATE = "redirect:/datasources/create";
	private static final String REDIRECT_DATAS_LIST = "redirect:/datasources/list";
	private static final String ERROR_403 = "error/403";
	private static final String ERROR_TRUE_STR = "{\"error\":\"true\"}";

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@RequestMapping(value = "/list", produces = "text/html")
	public String list(Model uiModel, HttpServletRequest request) {

		String identification = request.getParameter("identification");
		String description = request.getParameter("description");

		if (identification != null && identification.equals("")) {
			identification = null;
		}
		if (description != null && description.equals("")) {
			description = null;
		}

		List<GadgetDatasourceForList> datasource = this.gadgetDatasourceService
				.findGadgetDatasourceWithIdentificationAndDescription(identification, description, utils.getUserId());

		uiModel.addAttribute("datasources", datasource);
		return "datasources/list";

	}

	@PostMapping(value = "getNamesForAutocomplete")
	public @ResponseBody List<String> getNamesForAutocomplete() {
		return this.gadgetDatasourceService.getAllIdentifications();
	}

	public void showDatasources(Model model) {
		model.addAttribute("datasources", this.gadgetDatasourceService.getAllIdentifications());

	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/create", produces = "text/html")
	public String createGadget(Model model) {
		model.addAttribute(DATASOURCE_STR, new GadgetDatasourceDTO());
		model.addAttribute(DATASOURCE_ONT_SEL_STR, "");
		model.addAttribute(ONTOLOGIES_STR, getOntologiesDTO());
		return "datasources/create";

	}

	/*
	 * @PreAuthorize(
	 * "@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	 * 
	 * @PostMapping(value = { "/create" }) public String createDataSource(Model
	 * model, @Valid GadgetDatasource gadgetDatasource, BindingResult bindingResult,
	 * RedirectAttributes redirect) { if (bindingResult.hasErrors()) {
	 * log.debug("Some gadget datasource properties missing");
	 * utils.addRedirectMessage("datasources.create.error", redirect); return
	 * REDIRECT_DATAS_CREATE; } try {
	 * gadgetDatasource.setUser(this.userService.getUser(this.utils.getUserId()));
	 * 
	 * String ontology = getOntologyFromDatasource(gadgetDatasource.getQuery());
	 * Ontology onto; if (ontology.length() > 0) { onto =
	 * ontologyService.getOntologyByIdentification(ontology,
	 * this.utils.getUserId()); if (onto != null) {
	 * gadgetDatasource.setOntology(onto); } }
	 * this.gadgetDatasourceService.createGadgetDatasource(gadgetDatasource); }
	 * catch (GadgetDatasourceServiceException e) {
	 * log.debug("Cannot create gadget datasource");
	 * utils.addRedirectMessage("datasources.create.error", redirect); return
	 * REDIRECT_DATAS_CREATE; } return REDIRECT_DATAS_LIST; }
	 */

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PostMapping(value = { "/create" })
	public String createDataSource(Model model, @Valid GadgetDatasourceDTO gadgetDatasourceDTO,
			BindingResult bindingResult, RedirectAttributes redirect) {
		if (bindingResult.hasErrors()) {
			log.debug("Some gadget datasource properties missing");
			utils.addRedirectMessage("datasources.create.error", redirect);
			return REDIRECT_DATAS_CREATE;
		}
		GadgetDatasource gadgetDatasource = new GadgetDatasource();
		try {
			gadgetDatasource.setUser(this.userService.getUser(this.utils.getUserId()));
			gadgetDatasource.setConfig(gadgetDatasourceDTO.getConfig());
			gadgetDatasource.setDescription(gadgetDatasourceDTO.getDescription());
			gadgetDatasource.setIdentification(gadgetDatasourceDTO.getIdentification());
			gadgetDatasource.setMaxvalues(gadgetDatasourceDTO.getMaxvalues());
			gadgetDatasource.setMode(gadgetDatasourceDTO.getMode());
			gadgetDatasource.setQuery(gadgetDatasourceDTO.getQuery());
			gadgetDatasource.setRefresh(gadgetDatasourceDTO.getRefresh());
			gadgetDatasource.setDbtype(gadgetDatasourceDTO.getDbtype());
			gadgetDatasource.setOntology(ontologyService.getOntologyByIdentification(
					gadgetDatasourceDTO.getOntologyIdentification(), this.utils.getUserId()));
			if (gadgetDatasourceDTO.getOntologyIdentification() == null
					|| gadgetDatasourceDTO.getOntologyIdentification().trim().length() == 0) {
				String ontology = gadgetDatasourceService.getOntologyFromDatasource(gadgetDatasource.getQuery());
				Ontology onto;
				if (ontology.length() > 0) {
					onto = ontologyService.getOntologyByIdentification(ontology, this.utils.getUserId());
					if (onto != null) {
						gadgetDatasource.setOntology(onto);
					}
				}
			}
			this.gadgetDatasourceService.createGadgetDatasource(gadgetDatasource);
		} catch (GadgetDatasourceServiceException e) {
			log.error("Cannot create gadget datasource. {}", e.getMessage());
			utils.addRedirectException(e, redirect);
			return REDIRECT_DATAS_CREATE;
		}
		return REDIRECT_DATAS_LIST;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/update/{id}", produces = "text/html")
	public String update(Model model, @PathVariable("id") String id) {
		GadgetDatasource gadgetDatasource = this.gadgetDatasourceService.getGadgetDatasourceById(id);
		if (gadgetDatasource != null) {
			if (!gadgetDatasourceService.hasUserEditPermission(id, this.utils.getUserId())) {
				return ERROR_403;
			}

			model.addAttribute(DATASOURCE_STR, gadgetDatasourceToDTO(gadgetDatasource));
			String ontologyIdentification = "";
			if (gadgetDatasource.getOntology() != null && gadgetDatasource.getOntology().getIdentification() != null) {
				ontologyIdentification = gadgetDatasource.getOntology().getIdentification();
			}
			model.addAttribute(DATASOURCE_ONT_SEL_STR, ontologyIdentification);
			model.addAttribute(ONTOLOGIES_STR, getOntologiesDTO());
			model.addAttribute(ResourcesInUseService.RESOURCEINUSE,
					resourcesInUseService.isInUse(id, utils.getUserId()));
			resourcesInUseService.put(id, utils.getUserId());
			return "datasources/create";
		} else {
			return "error/404";
		}
	}

	private GadgetDatasourceDTO gadgetDatasourceToDTO(GadgetDatasource gadgetds) {
		final GadgetDatasourceDTO gDto = new GadgetDatasourceDTO();
		if (gadgetds != null) {
			gDto.setId(gadgetds.getId());
			gDto.setConfig(gadgetds.getConfig());
			gDto.setDescription(gadgetds.getDescription());
			gDto.setIdentification(gadgetds.getIdentification());
			gDto.setDbtype(gadgetds.getDbtype());
			gDto.setMaxvalues(gadgetds.getMaxvalues());
			gDto.setQuery(gadgetds.getQuery());
			gDto.setMode(gadgetds.getMode());
			gDto.setRefresh(gadgetds.getRefresh());
			if (gadgetds.getOntology() != null) {
				gDto.setOntologyIdentification(gadgetds.getOntology().getIdentification());
			}

			return gDto;
		}

		return null;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PutMapping(value = "/update/{id}", produces = "text/html")
	public String updateGadgetDatasource(Model model, @PathVariable("id") String id,
			@Valid GadgetDatasourceDTO gadgetDatasourceDTO, BindingResult bindingResult, RedirectAttributes redirect) {

		if (bindingResult.hasErrors()) {
			log.debug("Some Gadget Datasource properties missing");
			utils.addRedirectMessage("datasources.update.error", redirect);
			return "redirect:/datasources/update/" + id;
		}
		if (!gadgetDatasourceService.hasUserEditPermission(id, this.utils.getUserId()))
			return ERROR_403;
		try {
			GadgetDatasource gd = this.gadgetDatasourceService.getGadgetDatasourceById(id);
			gd.setMaxvalues(gadgetDatasourceDTO.getMaxvalues());
			gd.setRefresh(gadgetDatasourceDTO.getRefresh());
			gd.setQuery(gadgetDatasourceDTO.getQuery());
			gd.setDescription(gadgetDatasourceDTO.getDescription());
			// If ontology not selected get from query
			if (gadgetDatasourceDTO.getOntologyIdentification() == null) {
				String ontology = gadgetDatasourceService.getOntologyFromDatasource(gadgetDatasourceDTO.getQuery());
				Ontology onto;
				if (ontology.length() > 0) {
					onto = ontologyService.getOntologyByIdentification(ontology, this.utils.getUserId());
					if (onto != null) {
						gd.setOntology(onto);
					}
				}
			} else {
				gd.setOntology(ontologyService.getOntologyByIdentification(
						gadgetDatasourceDTO.getOntologyIdentification(), this.utils.getUserId()));
			}

			this.gadgetDatasourceService.updateGadgetDatasource(gd);
		} catch (GadgetDatasourceServiceException e) {
			log.error("Cannot update gadget datasource. {}", e.getMessage());
			utils.addRedirectException(e, redirect);
			return "redirect:/datasources/update/" + id;
		}
		resourcesInUseService.removeByUser(id, utils.getUserId());
		return REDIRECT_DATAS_LIST;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@DeleteMapping("/{id}")
	public String delete(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		try {
			this.entityDeletionService.deleteGadgetDataSource(id, utils.getUserId());
		} catch (final RuntimeException e) {
			utils.addRedirectException(e, redirect);
		}
		return REDIRECT_DATAS_LIST;
	}

	@GetMapping(value = "/getGadgetsUsingDatasource/{id}", produces = "application/json")
	public @ResponseBody List<String> getGadgetsUsingDatasource(@PathVariable("id") String id) {
		return this.gadgetDatasourceService.getGadgetsUsingDatasource(id);
	}

	@GetMapping(value = "/getUserGadgetDatasources", produces = "application/json")
	public @ResponseBody List<GadgetDatasource> getUserGadgetDatasources() {
		return this.gadgetDatasourceService.getUserGadgetDatasources(utils.getUserId());
	}

	@GetMapping(value = "/getDatasourceById/{id}", produces = "application/json")
	public @ResponseBody GadgetDatasource getDatasourceById(@PathVariable("id") String id) {
		return this.gadgetDatasourceService.getGadgetDatasourceById(id);
	}

	@GetMapping(value = "/getDatasourceByIdentification/{id}", produces = "application/json")
	public @ResponseBody GadgetDatasource getDatasourceByIdentification(@PathVariable("id") String id) {
		GadgetDatasource datasource = this.gadgetDatasourceService.getDatasourceByIdentification(id);
		if (this.gadgetDatasourceService.hasUserPermission(datasource.getId(), utils.getUserId())) {
			return datasource;
		} else {
			return null;
		}
	}

	@GetMapping(value = "/isGroupDatasourceById/{id}", produces = "application/json")
	@ResponseBody
	public ResponseEntity<Boolean> isGroupDatasourceByIdentification(@PathVariable("id") String id) {
		boolean isGroup = this.gadgetDatasourceService.isGroupDatasourceById(id);
		if (this.gadgetDatasourceService.hasUserPermission(id, utils.getUserId())) {
			return new ResponseEntity<>(isGroup, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}

	@GetMapping(value = "/getSampleDatasource/{id}", produces = "application/json")
	public @ResponseBody String getSampleDatasource(@PathVariable("id") String datasourceId)
			throws DBPersistenceException, OntologyDataUnauthorizedException, GenericOPException {
		if (gadgetDatasourceService.hasUserViewPermission(datasourceId, this.utils.getUserId())) {
			GadgetDatasource gd = this.gadgetDatasourceService.getGadgetDatasourceById(datasourceId);
			String query = gd.getQuery();
			Ontology ontologyEnt = gd.getOntology();
			String ontology;
			if (ontologyEnt != null) {
				ontology = ontologyEnt.getIdentification();
			} else {
				ontology = gadgetDatasourceService.getOntologyFromDatasource(query);
			}
			Ontology o = ontologyService.getOntologyByIdentification(ontology, utils.getUserId());
			String sampleQuery = this.gadgetDatasourceService.getSampleQueryGadgetDatasourceById(datasourceId, ontology,
					utils.getUserId());
			if (!o.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)) {
				return queryToolService.querySQLAsJson(this.utils.getUserId(), ontology, sampleQuery, 0);
			} else {
				return queryToolService.queryNativeAsJson(this.utils.getUserId(), ontology, query);
			}

		} else {
			return "403";
		}
	}

	@PostMapping(value = { "/deleteFromGadget" }, produces = "application/json")
	public @ResponseBody String deleteFromGadget(String id) {
		try {

			this.entityDeletionService.deleteGadgetDataSource(id, utils.getUserId());
		} catch (GadgetDatasourceServiceException e) {
			log.error("Cannot delete gadget datasource, {}", e.getMessage());
			return ERROR_TRUE_STR;
		}
		return "{\"ok\":\"true\"}";
	}

	@PostMapping(value = { "/createFromGadget" }, produces = "application/json")
	public @ResponseBody String createDataSourceFromGadget(GadgetDatasourceDTO gadgetDatasourceDTO) {

		GadgetDatasource gadgetDatasource = new GadgetDatasource();
		try {
			gadgetDatasource.setUser(this.userService.getUser(this.utils.getUserId()));
			gadgetDatasource.setConfig(gadgetDatasourceDTO.getConfig());
			gadgetDatasource.setDescription(gadgetDatasourceDTO.getDescription());
			gadgetDatasource.setIdentification(gadgetDatasourceDTO.getIdentification());
			gadgetDatasource.setMaxvalues(gadgetDatasourceDTO.getMaxvalues());
			gadgetDatasource.setMode(gadgetDatasourceDTO.getMode());
			gadgetDatasource.setQuery(gadgetDatasourceDTO.getQuery());
			gadgetDatasource.setRefresh(gadgetDatasourceDTO.getRefresh());
			gadgetDatasource.setDbtype(gadgetDatasourceDTO.getDbtype());
			gadgetDatasource.setOntology(ontologyService.getOntologyByIdentification(
					gadgetDatasourceDTO.getOntologyIdentification(), this.utils.getUserId()));
			this.gadgetDatasourceService.createGadgetDatasource(gadgetDatasource);
		} catch (GadgetDatasourceServiceException e) {
			log.info("Cannot create gadget datasource. {}", e.getMessage());

			return ERROR_TRUE_STR;
		}
		return "{\"id\":\"" + gadgetDatasource.getId() + "\"}";
	}

	@PostMapping(value = "/updateGadgetDatasourceFromGadget/{id}", produces = "text/html")
	public @ResponseBody String updateGadgetDatasourceFromGadget(@PathVariable("id") String id,
			GadgetDatasource gadgetDatasource) {
		if (!gadgetDatasourceService.hasUserEditPermission(id, this.utils.getUserId()))
			return ERROR_TRUE_STR;
		try {
			GadgetDatasource gd = this.gadgetDatasourceService.getGadgetDatasourceById(id);

			gd.setMaxvalues(gadgetDatasource.getMaxvalues());
			gd.setRefresh(gadgetDatasource.getRefresh());
			gd.setQuery(gadgetDatasource.getQuery());
			String ontology = gadgetDatasourceService.getOntologyFromDatasource(gadgetDatasource.getQuery());
			Ontology onto;
			if (ontology.length() > 0) {
				onto = ontologyService.getOntologyByIdentification(ontology, this.utils.getUserId());
				if (onto != null) {
					gadgetDatasource.setOntology(onto);
				}
				gd.setOntology(onto);
			}
			this.gadgetDatasourceService.updateGadgetDatasource(gd);
		} catch (GadgetDatasourceServiceException e) {
			log.error("Cannot update gadget datasource. {}", e.getMessage());

			return ERROR_TRUE_STR;
		}
		return "{\"id\":\"" + gadgetDatasource.getId() + "\"}";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/show/{id}", produces = "text/html")
	public String show(Model model, @PathVariable("id") String id) {
		GadgetDatasource gadgetDatasource = this.gadgetDatasourceService.getGadgetDatasourceById(id);
		if (gadgetDatasource != null) {
			if (!gadgetDatasourceService.hasUserViewPermission(id, this.utils.getUserId()))
				return ERROR_403;
			model.addAttribute(DATASOURCE_STR, gadgetDatasource);
			String ontologyIdentification = "";
			if (gadgetDatasource.getOntology() != null && gadgetDatasource.getOntology().getIdentification() != null) {
				ontologyIdentification = gadgetDatasource.getOntology().getIdentification();
			}
			model.addAttribute(DATASOURCE_ONT_SEL_STR, ontologyIdentification);
			model.addAttribute(ONTOLOGIES_STR, getOntologiesDTO());
			model.addAttribute("accessType", gadgetDatasourceService.getAccessType(id, utils.getUserId()));
			return "datasources/show";
		} else {
			return "error/404";
		}
	}

	private List<OntologyDTO> getOntologiesDTO() {
		List<OntologyDTO> listOntologies = new ArrayList<>();
		List<OntologyForList> ontologies = this.ontologyService.getOntologiesForListByUserId(utils.getUserId());
		if (ontologies != null && !ontologies.isEmpty()) {
			for (Iterator<OntologyForList> iterator = ontologies.iterator(); iterator.hasNext();) {
				OntologyForList ontology = iterator.next();
				OntologyDTO oDTO = new OntologyDTO();
				oDTO.setIdentification(ontology.getIdentification());
				oDTO.setDescription(ontology.getDescription());
				oDTO.setUser(ontology.getUser().getUserId());
				listOntologies.add(oDTO);
			}
		}
		return listOntologies;
	}

	@GetMapping(value = "/freeResource/{id}")
	public @ResponseBody void freeResource(@PathVariable("id") String id) {
		resourcesInUseService.removeByUser(id, utils.getUserId());
		log.info("free resource", id);
	}
}
