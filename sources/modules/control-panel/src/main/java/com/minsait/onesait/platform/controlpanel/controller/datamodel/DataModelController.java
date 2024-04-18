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
package com.minsait.onesait.platform.controlpanel.controller.datamodel;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.minsait.onesait.platform.config.model.DataModel;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.services.datamodel.DataModelService;
import com.minsait.onesait.platform.config.services.exceptions.DataModelServiceException;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/datamodels")
@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
@Slf4j
public class DataModelController {

	@Autowired
	private DataModelService dataModelService;

	@Autowired
	private UserService userService;

	@Autowired
	private AppWebUtils utils;
	
	@Autowired 
	private HttpSession httpSession;

	private static final String DATAMOD_CREATE = "datamodels/create";
	private static final String REDIRECT_DATAMOD_LIST = "redirect:/datamodels/list";
	private static final String APP_ID = "appId";

	@GetMapping(value = "/list", produces = "text/html")
	public String list(Model model, @RequestParam(required = false) String dataModelId,
			@RequestParam(required = false) String name, @RequestParam(required = false) String description) {
		//CLEANING APP_ID FROM SESSION
		httpSession.removeAttribute(APP_ID);
		
		if ("".equals(dataModelId)) {
			dataModelId = null;
		} else if(dataModelId != null) {
			dataModelId = "%" + dataModelId + "%";
		}

		if ("".equals(name)) {
			name = null;
		} else if(name != null) {
			name = "%" + name + "%";
		}

		if ("".equals(description)) {
			description = null;
		} else if(description != null) {
			description = "%" + description + "%";
		}

		if ((dataModelId == null) && (name == null) && (description == null)) {
			log.debug("No params for filtering, loading all Data Models");
			model.addAttribute("dataModels", this.dataModelService.getAllDataModels());

		} else {
			log.debug("Params detected, filtering Data Models...");
			model.addAttribute("dataModels",
					this.dataModelService.getDataModelsByCriteria(dataModelId, name, description));
		}

		return "datamodels/list";
	}

	private void populateFormData(Model model) {
		model.addAttribute("dataModelTypes", this.dataModelService.getAllDataModelsTypes());
	}

	@GetMapping(value = "/show/{id}", produces = "text/html")
	public String show(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		try {
			DataModel dataModel = null;
			if (id != null) {
				dataModel = this.dataModelService.getDataModelById(id);
			}
			if (dataModel == null)
				return "error/404";
			model.addAttribute("dataModel", dataModel);
			return "datamodels/show";
		} catch (Exception e) {
			log.error("Error in DataModel controller: " + e.getMessage());
			return "datamodels/list";
		}
	}

	@GetMapping(value = "/create", produces = "text/html")
	public String create(Model model, @Valid DataModel dataModel, BindingResult bindingResult) {

		this.populateFormData(model);

		model.addAttribute("datamodel", new DataModel());
		return DATAMOD_CREATE;
	}

	@PostMapping(value = "/create", produces = "text/html")
	public String createDataModel(Model model, @ModelAttribute DataModel datamodel, BindingResult bindingResult,
			RedirectAttributes redirect, HttpServletRequest request) {
		try {

			if (dataModelService.dataModelExists(datamodel)) {
				log.error("This DataModel already exist");
				utils.addRedirectMessage("datamodel.error.exist", redirect);
				return REDIRECT_DATAMOD_LIST;
			}
			if(!dataModelService.validateJSON(datamodel)){
				utils.addRedirectMessage(" Error, The JSON entered is not valid ", redirect);
				return "redirect:/datamodels/create";
				
			}

			datamodel.setUser(userService.getUserByIdentification(utils.getUserId()));
			dataModelService.createDataModel(datamodel);
			log.error("DataModel created correctly");
			return REDIRECT_DATAMOD_LIST;

		} catch (Exception e) {
			log.error("Cannot create datamodel: " + e.getMessage());
			utils.addRedirectMessage("datamodel.error.created", redirect);
			return "redirect:/datamodels/create";
		}
	}

	@GetMapping(value = "/update/{id}")
	public String update(@PathVariable String id, Model model, HttpServletRequest request) {

		try {
			this.populateFormData(model);
			DataModel datamodel = this.dataModelService.getDataModelById(id);
			if (datamodel != null) {
				log.debug("Update Data Model");
				model.addAttribute("datamodel", datamodel);
				return DATAMOD_CREATE;
			} else {
				return DATAMOD_CREATE;
			}
		} catch (Exception e) {
			log.error("Cannot update datamodel: " + e.getMessage());
			return DATAMOD_CREATE;
		}
	}

	@PutMapping(value = "/update/{id}", produces = "text/html")
	public String updateDataModel(@PathVariable String id, Model model, @ModelAttribute DataModel datamodel,
			RedirectAttributes redirect, HttpServletRequest request) {
		if(!dataModelService.validateJSON(datamodel)){
			utils.addRedirectMessage(" Error, The JSON entered is not valid ", redirect);
			return "redirect:/datamodels/update/" + id;
		}
		if (datamodel != null) {
			datamodel.setUser(userService.getUserByIdentification(utils.getUserId()));
			if (!this.utils.getUserId().equals(datamodel.getUser().getUserId()) && !utils.isAdministrator())
				return "error/403";
			try {
				this.dataModelService.updateDataModel(datamodel);
			} catch (DataModelServiceException e) {
				log.debug("Could not update the Data Model");
				utils.addRedirectMessage("datamodel.error.update", redirect);
				return DATAMOD_CREATE;
			}
		} else {
			return "redirect:/datamodels/update/" + id;
		}
		model.addAttribute("Datamodel", datamodel);
		log.debug("Data Mode has been update succesfully");
		return "redirect:/datamodels/show/" + datamodel.getId();
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<String> deleteDataModel(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		DataModel dataModel = dataModelService.getDataModelById(id);
		String ontologies = dataModelService.getOntologiesById(dataModel);
		if(ontologies == "") {
			if (dataModel != null) {
				try {
					this.dataModelService.deleteDataModel(id);
					return new ResponseEntity<>("messageDeletedSuccessfully", HttpStatus.OK);
				} catch (DataModelServiceException e) {
					return new ResponseEntity<>("messageNotDeleteDataModel", HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} else {
				return new ResponseEntity<>("messageNotExistDataModel", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}else {
			return new ResponseEntity<>(ontologies, HttpStatus.BAD_REQUEST);
		}
	}

}
