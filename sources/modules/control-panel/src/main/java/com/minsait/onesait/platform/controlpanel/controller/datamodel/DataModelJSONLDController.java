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
package com.minsait.onesait.platform.controlpanel.controller.datamodel;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.DataModel;
import com.minsait.onesait.platform.config.services.datamodel.DataModelService;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/datamodelsjsonld")

@Slf4j
public class DataModelJSONLDController {

	@Autowired
	private DataModelService dataModelService;

	@Autowired
	private ObjectMapper mapper;

	private static final String JSONLD_MODEL = "datamodelsjsonld/show";

	private void populateFormData(Model model) {
		model.addAttribute("dataModelTypes", this.dataModelService.getAllDataModelsTypes());
	}

	@GetMapping(value = "/{modelId}/{id}")
	public String update(@PathVariable String modelId, @PathVariable String id, Model model,
			HttpServletRequest request) {

		try {
			this.populateFormData(model);
			DataModel datamodel = this.dataModelService.getDataModelById(modelId);
			if (datamodel != null) {
				log.debug("Show jsonld datamodel");
				mapper = new ObjectMapper();
				JsonNode actualObj = mapper.readTree(datamodel.getJsonSchema());
				model.addAttribute("idJson", id);
				model.addAttribute("identificationSchema", datamodel.getIdentification());
				model.addAttribute("datamodel", actualObj);
				model.addAttribute("datamodeltxt", datamodel.getJsonSchema());
				return JSONLD_MODEL;
			} else {
				return JSONLD_MODEL;
			}
		} catch (Exception e) {
			log.error("Cannot show jsonld datamodel: " + e.getMessage());
			return JSONLD_MODEL;
		}
	}

}
