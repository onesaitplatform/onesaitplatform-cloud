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
package com.minsait.onesait.platform.controlpanel.controller.jsontool;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.minsait.onesait.platform.business.services.ontology.OntologyBusinessService;
import com.minsait.onesait.platform.commons.model.InsertResult;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.router.service.app.model.NotificationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.QueryType;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.Source;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.service.RouterService;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/jsontool")
@Slf4j
public class JsonToolController {

	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private OntologyBusinessService ontologyBusinessService;

	@Autowired
	private AppWebUtils utils;

	@Autowired
	private RouterService routerService;

	@Autowired
	private JsonToolUtils jsonToolUtils;

	private final ObjectMapper mapper = new ObjectMapper();

	private static final String PATH_PROPERTIES = "properties";

	@GetMapping("tools")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public String show(Model model) {
		model.addAttribute("datasources", ontologyService.getDatasources());
		model.addAttribute("ontologies", ontologyService.getOntologiesByUserId(utils.getUserId()));
		return "json2ontologytool/import";
	}

	@PostMapping("createontology")
	public @ResponseBody String createOntology(Model model, @RequestParam String ontologyIdentification,
			@RequestParam String ontologyDescription, @RequestParam String schema, @RequestParam String datasource)
			throws IOException {

		final Ontology ontology = jsonToolUtils.createOntology(ontologyIdentification, ontologyDescription,
				Ontology.RtdbDatasource.valueOf(datasource), schema);

		try {
			ontologyBusinessService.createOntology(ontology, ontology.getUser().getUserId(), null);
		} catch (final Exception e) {
			if (e.getCause() instanceof com.minsait.onesait.platform.config.services.ontologydata.OntologyDataJsonProblemException) {
				final JsonObject responseBody = new JsonObject();
				responseBody.addProperty("result", "ko");
				responseBody.addProperty("cause", e.getCause().getMessage());
				return responseBody.toString();
			} else {
				final JsonObject responseBody = new JsonObject();
				responseBody.addProperty("result", "ko");
				responseBody.addProperty("cause", e.getMessage().replaceAll("\"", "'"));
				return responseBody.toString();
			}
		}
		final Ontology oDb = ontologyService.getOntologyByIdentification(ontology.getIdentification(),
				utils.getUserId());
		return "{\"result\":\"ok\", \"id\":\"" + oDb.getId() + "\"}";
	}

	@PostMapping("importbulkdata")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public @ResponseBody String importBulkData(Model model, @RequestBody JsonInsertDTO insertDTO) {
		final ObjectMapper objMapper = new ObjectMapper();
		try {
			final JsonNode node = objMapper.readTree(insertDTO.getData());
			final OperationModel operation;

			operation = new OperationModel.Builder(insertDTO.getOntologyIdentification(), OperationType.INSERT,
					utils.getUserId(), Source.INTERNAL_ROUTER).body(node.toString()).queryType(QueryType.NATIVE)
							.build();
			final NotificationModel modelNotification = new NotificationModel();
			modelNotification.setOperationModel(operation);

			try {
				final OperationResultModel response = routerService.insert(modelNotification);
				if (response.getMessage().equals("OK")) {

					String output = response.getResult();
					try {

						final JSONObject obj = new JSONObject(output);
						if (obj.has(InsertResult.DATA_PROPERTY)) {
							output = obj.getString(InsertResult.DATA_PROPERTY);
						}

						Integer.parseInt(objMapper.readTree(output).path("count").asText());
						final JsonObject responseBody = new JsonObject();
						responseBody.addProperty("result", "ok");
						responseBody.addProperty("inserted", objMapper.readTree(output).path("count").asText());
						return responseBody.toString();
					} catch (final NumberFormatException | JSONException ne) {
						final JsonObject responseBody = new JsonObject();
						responseBody.addProperty("result", "ok");
						responseBody.addProperty("inserted", "");
						return responseBody.toString();
					}

				} else {
					log.error("Error insert BULK data:" + response.getMessage());
					final JsonObject responseBody = new JsonObject();
					responseBody.addProperty("result", "ERROR");
					responseBody.addProperty("cause", response.getMessage().replaceAll("\"", "'"));
					return responseBody.toString();
				}
			} catch (final Exception e) {
				final JsonObject responseBody = new JsonObject();
				responseBody.addProperty("result", "ERROR");
				responseBody.addProperty("cause", "Error insert BULK data. " + e.getMessage().replaceAll("\"", "'"));
				return responseBody.toString();
			}

		} catch (final IOException e) {
			final JsonObject responseBody = new JsonObject();
			responseBody.addProperty("result", "ko");
			responseBody.addProperty("cause", "Error parsing JSON. " + e.getMessage().replaceAll("\"", "'"));
			return responseBody.toString();
		}

	}

	@PostMapping("/getParentNodeOfSchema")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public @ResponseBody String parentNode(@RequestParam String id) throws IOException {
		final Ontology ontology = ontologyService.getOntologyByIdentification(id, utils.getUserId());
		if (ontology != null) {
			final String jsonSchema = ontology.getJsonSchema();

			final JsonNode schema = mapper.readTree(jsonSchema);
			if (schema.path(PATH_PROPERTIES).size() == 1) {
				return schema.path(PATH_PROPERTIES).fieldNames().next();
			}
		}
		return "";
	}

}
