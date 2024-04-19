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
package com.minsait.onesait.platform.controlpanel.controller.crud;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.minsait.onesait.platform.business.services.crud.CrudService;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.controlpanel.controller.crud.dto.OntologyDTO;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.SelectStatement;

@Controller
@RequestMapping("/crud")
public class CrudController {

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private CrudService crudService;
	@Autowired
	private AppWebUtils utils;

	private static final String ERROR_TRUE = "{\"error\":\"true\"}";

	@GetMapping(value = "/admin/{id}", produces = "text/html")
	public String edit(Model model, @PathVariable("id") String id) {
		final Ontology ontology = ontologyService.getOntologyById(id, utils.getUserId());
		final OntologyDTO ontologyDTO = new OntologyDTO();
		ontologyDTO.setIdentification(ontology.getIdentification());
		ontologyDTO.setJsonSchema(ontology.getJsonSchema());
		ontologyDTO.setDatasource(ontology.getRtdbDatasource().name());
		model.addAttribute("ontology", ontologyDTO);
		model.addAttribute("uniqueId", crudService.getUniqueColumn(ontology.getIdentification(), false));
		model.addAttribute("quasar", crudService.useQuasar());
		return "crud/admin";
	}

	@PostMapping(value = { "/query" }, produces = "application/json")
	public @ResponseBody String query(String ontologyID, String query) {
		try {
			return crudService.processQuery(query, ontologyID, ApiOperation.Type.GET, "", "", utils.getUserId());
		} catch (final Exception e) {
			return ERROR_TRUE;
		}
	}

	@RequestMapping(path = "/queryParams", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody String queryParams(@Valid @RequestBody final SelectStatement selectStatement,
			final BindingResult result) {
		try {
			if (!result.hasErrors()) {
				return crudService.queryParams(selectStatement, utils.getUserId());
			} else {
				throw new IllegalArgumentException("Parameters could not be mapped to a select statement");
			}
		} catch (final Exception e) {
			return ERROR_TRUE;
		}
	}

	@PostMapping(value = { "/findById" }, produces = "application/json")
	public @ResponseBody String findById(final String ontologyID, final String oid) {
		return crudService.findById(ontologyID, oid, utils.getUserId());
	}

	@PostMapping(value = { "/deleteById" }, produces = "application/json")
	public @ResponseBody String deleteById(String ontologyID, String oid) {
		try {
			return crudService.processQuery("", ontologyID, ApiOperation.Type.DELETE, "", oid, utils.getUserId());
		} catch (final Exception e) {
			return ERROR_TRUE;
		}
	}

	@PostMapping(value = { "/insert" }, produces = "application/json")
	public @ResponseBody String insert(String ontologyID, String body) {
		try {
			return crudService.processQuery("", ontologyID, ApiOperation.Type.POST, body, "", utils.getUserId());
		} catch (final Exception e) {
			return "{\"exception\":\"true\"}";
		}
	}

	@PostMapping(value = { "/update" }, produces = "application/json")
	public @ResponseBody String update(String ontologyID, String body, String oid) {
		try {
			return crudService.processQuery("", ontologyID, ApiOperation.Type.PUT, body, oid, utils.getUserId());
		} catch (final Exception e) {
			return "{\"exception\":\"true\"}";
		}
	}

}
