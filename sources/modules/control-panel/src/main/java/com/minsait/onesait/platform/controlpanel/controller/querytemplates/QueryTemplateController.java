/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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
package com.minsait.onesait.platform.controlpanel.controller.querytemplates;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.minsait.onesait.platform.config.dto.OntologyForList;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.QueryTemplate;
import com.minsait.onesait.platform.config.model.QueryTemplate.QueryType;
import com.minsait.onesait.platform.config.repository.QueryTemplateRepository;
import com.minsait.onesait.platform.config.services.deletion.EntityDeletionService;
import com.minsait.onesait.platform.config.services.exceptions.QueryTemplateServiceException;
import com.minsait.onesait.platform.config.services.gadget.dto.OntologyDTO;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.templates.QueryTemplateService;
import com.minsait.onesait.platform.config.services.templates.dto.QueryTemplateDTO;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@RequestMapping("/querytemplates")
@Controller
@Slf4j
public class QueryTemplateController {

	private static final String TEMPLATE_STR = "template";
	private static final String TEMPLATE_ONT_SEL_STR = "templateOntologySelected";
	private static final String ONTOLOGIES_STR = "ontologies";
	private static final String REDIRECT_TEMPLATE_CREATE = "redirect:/querytemplates/create";
	private static final String REDIRECT_TEMPLATE_LIST = "redirect:/querytemplates/list";

	@Autowired
	private QueryTemplateService queryTemplateService;
	@Autowired
	private QueryTemplateRepository queryTemplateRepository;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private EntityDeletionService entityDeletionService;

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@RequestMapping(value = "/list", produces = "text/html")
	public String list(Model uiModel, HttpServletRequest request, @RequestParam(required = false) String name) {
		if (name != null && name.equals("")) {
			name = null;
		}
		
		List<QueryTemplate> templates = new ArrayList<>();
		if (name == null) {
			log.debug("No params for filtering, loading all query templates");
			templates = this.queryTemplateService.getAllQueryTemplates();
		} else {
			log.debug("Params detected, filtering query templates...");
			templates = this.queryTemplateService.getQueryTemplateByCriteria(name);
		}
		
		uiModel.addAttribute("templates", templates);
		return "querytemplates/list";

	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = "/create", produces = "text/html")
	public String createQueryTemplate(Model model) {
		model.addAttribute(TEMPLATE_STR, new QueryTemplate());
		model.addAttribute(TEMPLATE_ONT_SEL_STR, "");
		model.addAttribute(ONTOLOGIES_STR, getOntologiesDTO());
		model.addAttribute("qtemplate", new QueryTemplateDTO());
		return "querytemplates/create";

	}

	@PostMapping(value = { "/create" })
	public String createQueryTemplate(Model model, @Valid QueryTemplateDTO queryTemplateDTO,
			BindingResult bindingResult, RedirectAttributes redirect) {
		if (bindingResult.hasErrors()) {
			log.debug("Some query template properties missing");
			utils.addRedirectMessage("templates.create.error", redirect);
			return REDIRECT_TEMPLATE_CREATE;
		}
		QueryTemplate queryTemplate = new QueryTemplate();
		try {
			queryTemplate.setName(queryTemplateDTO.getName());
			queryTemplate.setDescription(queryTemplateDTO.getDescription());
			queryTemplate.setQueryGenerator(queryTemplateDTO.getQueryGenerator());
			queryTemplate.setQuerySelector(queryTemplateDTO.getQuerySelector());
			queryTemplate.setType(QueryType.NATIVE);

			String onto = queryTemplateDTO.getOntology();
			Ontology ontology;
			if (onto.length() > 0) {
				ontology = ontologyService.getOntologyByIdentification(onto, this.utils.getUserId());
				if (ontology != null) {
					queryTemplate.setOntology(ontology);
				}
			}
			this.queryTemplateService.createQueryTemplate(queryTemplate);
		} catch (QueryTemplateServiceException e) {
			log.debug("Cannot create query template");
			utils.addRedirectMessage("templates.create.error", redirect);
			return REDIRECT_TEMPLATE_CREATE;
		}
		return REDIRECT_TEMPLATE_LIST;
	}

	@GetMapping(value = "/update/{id}", produces = "text/html")
	public String update(Model model, @PathVariable("id") String id) {
		QueryTemplate queryTemplate = this.queryTemplateService.getQueryTemplateById(id);
		if (queryTemplate != null) {
			model.addAttribute(TEMPLATE_STR, queryTemplate);
			String ontologyIdentification = "";
			if (queryTemplate.getOntology() != null && queryTemplate.getOntology().getIdentification() != null) {
				ontologyIdentification = queryTemplate.getOntology().getIdentification();
			}
			model.addAttribute(TEMPLATE_ONT_SEL_STR, ontologyIdentification);
			model.addAttribute(ONTOLOGIES_STR, getOntologiesDTO());
			QueryTemplateDTO qTemplate = new QueryTemplateDTO();
			qTemplate.setDescription(queryTemplate.getDescription());
			qTemplate.setName(queryTemplate.getName());
			qTemplate.setOntology(ontologyIdentification);
			qTemplate.setQueryGenerator(queryTemplate.getQueryGenerator());
			qTemplate.setQuerySelector(queryTemplate.getQuerySelector());
			model.addAttribute("qtemplate", qTemplate);
			return "querytemplates/create";
		} else {
			return "error/404";
		}
	}

	@PutMapping(value = "/update/{id}", produces = "text/html")
	public String updateQueryTemplate(Model model, @PathVariable("id") String id,
			@Valid QueryTemplateDTO queryTemplateDTO, BindingResult bindingResult, RedirectAttributes redirect) {

		QueryTemplate queryTemplate = queryTemplateRepository.findByName(queryTemplateDTO.getName());
		queryTemplate.setDescription(queryTemplateDTO.getDescription());
		queryTemplate.setQueryGenerator(queryTemplateDTO.getQueryGenerator());
		queryTemplate.setQuerySelector(queryTemplateDTO.getQuerySelector());

		if (bindingResult.hasErrors()) {
			log.debug("Some Query Template properties missing");
			utils.addRedirectMessage("templates.update.error", redirect);
			return "redirect:/querytemplates/update/" + id;
		}
		try {
			String onto = queryTemplateDTO.getOntology();
			Ontology ontology;
			if (onto.length() > 0) {
				ontology = ontologyService.getOntologyByIdentification(onto, this.utils.getUserId());
				if (ontology != null) {
					queryTemplate.setOntology(ontology);
				}
			} else {
				queryTemplate.setOntology(null);
			}
			this.queryTemplateService.updateQueryTemplate(queryTemplate);
		} catch (QueryTemplateServiceException e) {
			log.debug("Cannot update Query Template");
			utils.addRedirectMessage("templates.update.error", redirect);
			return REDIRECT_TEMPLATE_CREATE;
		}
		return REDIRECT_TEMPLATE_LIST;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@DeleteMapping("/{id}")
	public String delete(Model model, @PathVariable("id") String id) {
		this.entityDeletionService.deleteQueryTemplate(id);
		return REDIRECT_TEMPLATE_LIST;
	}

	@GetMapping(value = "/show/{id}", produces = "text/html")
	public String show(Model model, @PathVariable("id") String id) {
		QueryTemplate queryTemplate = this.queryTemplateService.getQueryTemplateById(id);
		if (queryTemplate != null) {
			model.addAttribute(TEMPLATE_STR, queryTemplate);
			String ontologyIdentification = "";
			if (queryTemplate.getOntology() != null && queryTemplate.getOntology().getIdentification() != null) {
				ontologyIdentification = queryTemplate.getOntology().getIdentification();
			}
			model.addAttribute(TEMPLATE_ONT_SEL_STR, ontologyIdentification);
			model.addAttribute(ONTOLOGIES_STR, getOntologiesDTO());
			return "querytemplates/show";
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

}
