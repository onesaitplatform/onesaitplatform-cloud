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
package com.minsait.onesait.platform.controlpanel.rest.management.forms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.business.services.crud.CrudService;
import com.minsait.onesait.platform.business.services.datasources.dto.InputMessage;
import com.minsait.onesait.platform.business.services.datasources.exception.DatasourceException;
import com.minsait.onesait.platform.business.services.datasources.service.DatasourceService;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.Form;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.services.form.FormCreateDTO;
import com.minsait.onesait.platform.config.services.form.FormDTO;
import com.minsait.onesait.platform.config.services.form.FormDataDTO;
import com.minsait.onesait.platform.config.services.form.FormService;
import com.minsait.onesait.platform.config.services.form.ListElemSubFormDTO;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataUnauthorizedException;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@Tag(name = "Forms")
@RequestMapping("api/forms")
@ApiResponses({ @ApiResponse(responseCode = "400", description = "Bad request"),
		@ApiResponse(responseCode = "500", description = "Internal server error"),
		@ApiResponse(responseCode = "403", description = "Forbidden"),
		@ApiResponse(responseCode = "404", description = "Not found") })
@Slf4j
public class FormsRestController {

	@Autowired
	private FormService formService;
	@Autowired
	private DatasourceService datasourceService;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private CrudService crudService;
	@Autowired
	private AppWebUtils utils;

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Create Form")
	@PostMapping("/")
//	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<String> createForm(@RequestBody FormCreateDTO form) {
		formService.create(form, utils.getUserId());
		return ResponseEntity.ok(form.getJsonSchema());
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Submit Form")
	@PostMapping(value = "/submit")
//	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<String> submitData(@RequestBody String form) {
		JsonNode payload;
		try {
			payload = MAPPER.readValue(form, JsonNode.class);
			String code = "";

			code = payload.at("/metadata/formId").asText();

			final Form f = formService.getDBForm(code);
			if (f != null) {
				datasourceService.insertData(f.getOntology().getIdentification(), utils.getUserId(),
						MAPPER.writeValueAsString(payload.at("/data")));
			}
		} catch (final JsonProcessingException e) {
			log.error("Could not submit form ", e);
		}

		return ResponseEntity.ok(form);
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Submit Form")
	@PostMapping(value = "/form/{idform}/submission")
//	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<String> submitWithIdFormData(@RequestBody String form,
			@PathVariable("idform") String idform) {
		JsonNode payload;
		try {
			payload = MAPPER.readValue(form, JsonNode.class);
			final Form f = formService.getDBForm(idform);
			if (f != null) {
				datasourceService.insertData(f.getOntology().getIdentification(), utils.getUserId(),
						MAPPER.writeValueAsString(payload.at("/data")));
			}
		} catch (final JsonProcessingException e) {
			log.error("Could not submit form ", e);
		}

		return ResponseEntity.ok(form);
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Submit Update Form")
	@PostMapping("/submit/update")
//	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<String> submitUpdateData(@RequestBody String form) {
		JsonNode payload;
		try {
			payload = MAPPER.readValue(form, JsonNode.class);
			final Form f = formService.getDBForm(payload.at("/metadata/formId").asText());
			if (f != null) {
				datasourceService.update(f.getOntology().getIdentification(), payload.at("/metadata/dataId").asText(),
						utils.getUserId(), MAPPER.writeValueAsString(payload.at("/data")));
			}
		} catch (final JsonProcessingException e) {
			log.error("Could not submit form ", e);
		}

		return ResponseEntity.ok(form);
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Edit Form")
	@RequestMapping(value = "/{id}", method = { RequestMethod.POST, RequestMethod.PUT })
//	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<FormDTO> editForm(@PathVariable("id") String id, @RequestBody FormCreateDTO form) {
		final FormDTO f = formService.updateForm(form, id, utils.getUserId());
		if (f == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(f);
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Get Form Schema")
	@GetMapping(value = "/{id}/schema", produces = MediaType.APPLICATION_JSON_VALUE)
//	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<String> getFormSchema(@PathVariable("id") String id) {
		final FormDTO f = formService.getForm(id);
		if (f == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(f.getJsonSchema());
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Get Form")
	@GetMapping("/{id}")
//	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<FormDTO> getForm(@PathVariable("id") String id) {
		final FormDTO f = formService.getForm(id);
		if (f == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(f);
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "List Forms")
	@GetMapping("/")
//	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<List<FormDTO>> getForms() {
		final List<FormDTO> l = formService.getForms(utils.getUserId());
		return ResponseEntity.ok(l);
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Delete Form")
	@DeleteMapping("/{id}")
//	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<String> deleteForm(@PathVariable("id") String id) {
		try {
			formService.deleteForm(id, utils.getUserId());
		} catch (final Exception e) {
			return ResponseEntity.internalServerError().build();
		}
		return ResponseEntity.ok().build();
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Delete record by form and oid")
	@DeleteMapping("{formcode}/{oid}")
//	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<String> deleteRecord(@PathVariable("formcode") String formcode,
			@PathVariable("oid") String oid) {
		try {
			final FormDTO f = formService.getForm(formcode);
			ontologyService.getOntologyByIdentificationInsert(f.getEntity(), utils.getUserId());
			crudService.processQuery("", f.getEntity(), ApiOperation.Type.DELETE, "", oid, utils.getUserId());
		} catch (final Exception e) {
			return ResponseEntity.internalServerError().build();
		}
		return ResponseEntity.ok().build();
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Resolve datasource and get data")
	@PostMapping("/datasource")
//	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<String> datasource(@RequestBody InputMessage msg) {
		if (msg == null) {
			return ResponseEntity.notFound().build();
		}
		final String identification = msg.getDs();
		final GadgetDatasource ds = datasourceService.getGadgetDatasourceFromIdentification(identification,
				utils.getUserId());
		if (msg.getLimit() == 0) {
			msg.setLimit(ds.getMaxvalues());
		}
		String f;
		try {
			f = datasourceService.solveDatasource(msg, null, ds, utils.getUserId());
		} catch (DatasourceException | OntologyDataUnauthorizedException | GenericOPException e) {
			return ResponseEntity.notFound().build();
		}
		if (f == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(f);
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Get Form Data")
	@GetMapping("/{idForm}/data/{idData}")
//	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<FormDataDTO> getFormData(@PathVariable("idForm") String idForm,
			@PathVariable("idData") String idData) {
		final FormDTO f = formService.getForm(idForm);
		if (f == null) {
			return ResponseEntity.notFound().build();
		}
		final String schema = f.getJsonSchema();
		String result;
		try {
			result = datasourceService.getDataById(f.getEntity(), idData, utils.getUserId());
		} catch (final DatasourceException e) {

			return ResponseEntity.notFound().build();
		}
		try {
			return ResponseEntity.ok(FormDataDTO.builder().data(MAPPER.readValue(result, JsonNode.class))
					.schema(MAPPER.readValue(schema, JsonNode.class)).i18nJson(f.getI18nJson()).build());
		} catch (final JsonProcessingException e) {
			log.error("Could not marshall JSON", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Generate Form From Entity")
	@GetMapping(value = "/{entity}/generateFormFromEntity", produces = MediaType.APPLICATION_JSON_VALUE)
//	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<String> generateFormFromEntity(@PathVariable("entity") String entity) {
		String f;
		try {
			f = formService.generateFormFromEntity(entity, utils.getUserId());

			if (f == null) {
				return ResponseEntity.notFound().build();
			}
			return ResponseEntity.ok(f);
		} catch (IOException e) {
			log.error("Could not generate Form From Entity", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "List SubForms")
	@GetMapping("/subforms")
//	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<List<ListElemSubFormDTO>> getSubFormsList() {
		final List<FormDTO> l = formService.getForms(utils.getUserId());
		return ResponseEntity.ok(mapFormToListElemSubFormDTO(l));
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Get Sub Form ")
	@GetMapping(value = "/form/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
//	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<Object> getSubForm(@PathVariable("id") String id) {
		final FormDTO f = formService.getForm(id);
		if (f == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(mapFormToSubForm(f).toMap());
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))

	@Operation(summary = "Get Sub Form ")

	@GetMapping(value = "/form/{id}/v", produces = MediaType.APPLICATION_JSON_VALUE)
	// @PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<Object> getSubFormv(@PathVariable("id") String id) {
		return ResponseEntity.ok(null);
	}

	private List<ListElemSubFormDTO> mapFormToListElemSubFormDTO(List<FormDTO> list) {
		List<ListElemSubFormDTO> resulList = new ArrayList<ListElemSubFormDTO>();
		if (list != null && list.size() > 0) {
			for (FormDTO form : list) {
				ListElemSubFormDTO sub = new ListElemSubFormDTO(form.getName(), "form", form.getCode());
				resulList.add(sub);
			}
		}
		return resulList;
	}

	private JSONObject mapFormToSubForm(FormDTO form) {
		JSONObject sform = new JSONObject();
		sform.put("_id", form.getCode());

		sform.put("name", form.getName());
		sform.put("owner", form.getUserId());
		sform.put("path", form.getCode());
		sform.put("title", form.getName());
		JSONObject schema = new JSONObject(form.getJsonSchema());
		if (schema.has("components")) {
			sform.put("components", schema.getJSONArray("components"));
		} else {
			sform.put("components", new JSONArray());
		}
		return sform;
	}

}
