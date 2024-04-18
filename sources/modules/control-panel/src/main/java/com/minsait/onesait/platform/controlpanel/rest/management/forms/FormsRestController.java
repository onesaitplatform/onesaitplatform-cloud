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
package com.minsait.onesait.platform.controlpanel.rest.management.forms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.minsait.onesait.platform.business.services.crud.CrudService;
import com.minsait.onesait.platform.business.services.datasources.dto.FilterStt;
import com.minsait.onesait.platform.business.services.datasources.dto.InputMessage;
import com.minsait.onesait.platform.business.services.datasources.exception.DatasourceException;
import com.minsait.onesait.platform.business.services.datasources.service.DatasourceService;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.Form;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessList;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.repository.ProjectResourceAccessRepository;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.services.form.FormCloneValueDTO;
import com.minsait.onesait.platform.config.services.form.FormCreateDTO;
import com.minsait.onesait.platform.config.services.form.FormDTO;
import com.minsait.onesait.platform.config.services.form.FormDataDTO;
import com.minsait.onesait.platform.config.services.form.FormService;
import com.minsait.onesait.platform.config.services.form.ListElemSubFormDTO;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataUnauthorizedException;
import com.minsait.onesait.platform.controlpanel.rest.management.forms.model.DatasourcesFieldsDTO;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.persistence.external.generator.model.common.WhereStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.SelectStatement;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
	@Autowired
	private ProjectResourceAccessRepository projectResourceAccessRepository;
	@Autowired
	private IntegrationResourcesService resourcesService;

	@Value("${onesaitplatform.database.mongodb.queries.defaultLimit:1000}")
	private int queryDefaultLimit;
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final String MSG_ERROR_JSON_RESPONSE = "{\"ERROR\":%s}";
	private static final String MSG_ERROR_5_CHARACTERS = "\"The identifier must have at least 5 characters\"";

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
		String result = "";
		try {
			payload = MAPPER.readValue(form, JsonNode.class);
			String code = "";
			code = payload.at("/metadata/formId").asText();
			if (!formService.hasUserAccess(utils.getUserId(), code, ResourceAccessType.VIEW)) {
				log.warn("User {} does not have access to form {}", utils.getUserId(), code);
				return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
			}
			final Form f = formService.getDBForm(code);
			if (f != null) {
				result = datasourceService.insertData(f.getOntology().getIdentification(), utils.getUserId(),
						MAPPER.writeValueAsString(payload.at("/data")));
				if (result != null && result.indexOf("\"error\":") > -1) {
					return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
				}
			}
		} catch (final JsonProcessingException e) {
			log.error("Could not submit form ", e);
		}

		return ResponseEntity.ok(result);
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Submit Form")
	@PostMapping(value = "/form/{idform}/submission")
//	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<String> submitWithIdFormData(@RequestBody String form,
			@PathVariable("idform") String idform) {
		JsonNode payload;
		try {
			if (!formService.hasUserAccess(utils.getUserId(), idform, ResourceAccessType.VIEW)) {
				log.warn("User {} does not have access to form {}", utils.getUserId(), idform);
				return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
			}
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
			String idform = payload.at("/metadata/formId").asText();
			if (!formService.hasUserAccess(utils.getUserId(), idform, ResourceAccessType.VIEW)) {
				log.warn("User {} does not have access to form {}", utils.getUserId(), idform);
				return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
			}

			final Form f = formService.getDBForm(idform);
			if (f != null) {
				String result = datasourceService.update(f.getOntology().getIdentification(),
						payload.at("/metadata/dataId").asText(), utils.getUserId(),
						MAPPER.writeValueAsString(payload.at("/data")));
				if (result != null && result.equals("[ ]")) {
					return ResponseEntity.status(HttpStatus.CONFLICT)
							.body("Error the operation could not be performed");
				}
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
		if (!formService.hasUserAccess(utils.getUserId(), id, ResourceAccessType.MANAGE)) {
			log.warn("User {} does not have access to form {}", utils.getUserId(), id);
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
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
		if (!formService.hasUserAccess(utils.getUserId(), id, ResourceAccessType.VIEW)) {
			log.warn("User {} does not have access to form {}", utils.getUserId(), id);
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		final FormDTO f = formService.getForm(id);
		if (f == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(f.getJsonSchema());
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Get Form")
	@GetMapping("/{code}")
//	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<FormDTO> getForm(@PathVariable("code") String code) {
		final Form form = formService.getDBForm(code);
		if (form == null)
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);

		if (!formService.hasUserAccess(utils.getUserId(), code, ResourceAccessType.VIEW)) {
			log.warn("User {} does not have access to form {}", utils.getUserId(), code);
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		final FormDTO f = formService.getForm(code);
		if (f == null) {
			return ResponseEntity.notFound().build();
		}
		f.setDatasources(getDatasourcesData(f, null));
		return ResponseEntity.ok(f);
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Get Form")
	@GetMapping("getFormById/{id}")
//	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<FormDTO> getFormByID(@PathVariable("id") String id) {
		final Form form = formService.getDBForm(id);
		if (form == null)
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);

		if (!formService.hasUserAccess(utils.getUserId(), id, ResourceAccessType.VIEW)) {
			log.warn("User {} does not have access to form {}", utils.getUserId(), id);
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		final FormDTO f = formService.getFormById(id);
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
			if (!formService.hasUserAccess(utils.getUserId(), id, ResourceAccessType.MANAGE)) {
				log.warn("User {} does not have access to form {}", utils.getUserId(), id);
				return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
			}
			final FormDTO f = formService.getForm(id);
			ProjectResourceAccessList getResource = null;
			getResource = projectResourceAccessRepository.getResource_id(f.getId());
			if (getResource != null) {
				return new ResponseEntity<>("This Form is shared via App, remove it from Apps before deleting it.", HttpStatus.CONFLICT);
			}
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
		log.info("Look for datasource");
		final String identification = msg.getDs();
		final GadgetDatasource ds = datasourceService.getGadgetDatasourceFromIdentification(identification,
				utils.getUserId());
		if (ds == null) {
			return ResponseEntity.notFound().build();
		}
		if (msg.getLimit() == 0) {
			msg.setLimit(ds.getMaxvalues());
		}
		String f;
		try {
			log.info("solve query for datasource");
			f = datasourceService.solveDatasource(msg, null, ds, utils.getUserId());
		} catch (DatasourceException | OntologyDataUnauthorizedException | GenericOPException e) {
			return ResponseEntity.notFound().build();
		}
		if (f == null) {
			return ResponseEntity.notFound().build();
		}
		log.info("return response from datasource");
		return ResponseEntity.ok(f);
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Resolve entity and get data")
	@PostMapping("/entitydata")
//	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<String> entitydata(@RequestBody InputMessage msg) {
		if (msg == null || msg.getDs() == null) {
			return ResponseEntity.notFound().build();
		}
		log.info("Look for entity");

		final Ontology ontology = ontologyService.getOntologyByIdentificationInsert(msg.getDs(), utils.getUserId());
		if (ontology == null) {
			return ResponseEntity.notFound().build();
		}
		String f;
		try {
			log.info("solve query for entity");
			SelectStatement selectStatement = new SelectStatement();
			selectStatement.setOntology(ontology.getIdentification());
			selectStatement.setLimit(getMaxRegisters());
			if (msg.getFilter() != null && msg.getFilter().size() > 0) {
				selectStatement.setWhere(mapFiltersForEntity(msg.getFilter()));
			}
			if (msg.getLimit() > 0) {
				selectStatement.setLimit(msg.getLimit());
			}
			if (msg.getOffset() > 0) {
				selectStatement.setOffset(msg.getOffset());
			}
			f = crudService.queryParams(selectStatement, utils.getUserId());
		} catch (DatasourceException e) {
			return ResponseEntity.notFound().build();
		}
		if (f == null) {
			return ResponseEntity.notFound().build();
		}
		log.info("return response from entity");
		return ResponseEntity.ok(f);
	}

	private List<WhereStatement> mapFiltersForEntity(List<FilterStt> filter) {
		List<WhereStatement> whereStList = new ArrayList<WhereStatement>();
		for (Iterator iterator = filter.iterator(); iterator.hasNext();) {
			FilterStt filterStt = (FilterStt) iterator.next();
			WhereStatement where = new WhereStatement(filterStt.getField(), filterStt.getOp(), filterStt.getExp());
			whereStList.add(where);
		}
		return whereStList;
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Get Form Data")
	@GetMapping("/{idForm}/data/{idData}")
//	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<FormDataDTO> getFormData(@PathVariable("idForm") String idForm,
			@PathVariable("idData") String idData) {
		if (!formService.hasUserAccess(utils.getUserId(), idForm, ResourceAccessType.VIEW)) {
			log.warn("User {} does not have access to form {}", utils.getUserId(), idForm);
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
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
					.datasources(getDatasourcesData(f, result)).schema(MAPPER.readValue(schema, JsonNode.class))
					.i18nJson(f.getI18nJson()).build());
		} catch (final JsonProcessingException e) {
			log.error("Could not marshall JSON", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Generate Form From Entity")
	@GetMapping(value = "/{codeTemplate}/generateFormFromEntity", produces = MediaType.APPLICATION_JSON_VALUE)
//	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<String> generateForm(@PathVariable("codeTemplate") String codeTemplate) {
		return automaticlyGeneratedForm(codeTemplate, null);
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Generate Form From Entity")
	@GetMapping(value = "/{codeTemplate}/generateFormFromEntity/{entity}", produces = MediaType.APPLICATION_JSON_VALUE)
//	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<String> generateFormFromEntity(@PathVariable("codeTemplate") String codeTemplate,
			@PathVariable("entity") String entity) {
		return automaticlyGeneratedForm(codeTemplate, entity);
	}

	private ResponseEntity<String> automaticlyGeneratedForm(String codeTemplate, String entity) {
		String f;
		try {
			f = formService.generateFormFromEntity(codeTemplate, entity, utils.getUserId());

			if (f == null) {
				return ResponseEntity.notFound().build();
			}
			return ResponseEntity.ok(f);
		} catch (final IOException e) {
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

	@Operation(summary = "Clone Form by code")
	@PostMapping(value = { "/clone" })
	public ResponseEntity<String> cloneForm(
			@Parameter(description = "Form code to clone") @RequestParam(required = true) String code,
			@Parameter(description = "New form name") @RequestParam(required = true) String newName) {
		try {
			if (newName == null || newName.trim().length() < 5) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_ERROR_5_CHARACTERS),
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
			String newCode = formService.createCode(newName, utils.getUserId());
			final Form form = formService.getDBForm(newCode);
			if (form != null)
				return new ResponseEntity<>(HttpStatus.CONFLICT);

			if (!formService.hasUserAccess(utils.getUserId(), code, ResourceAccessType.VIEW)) {
				log.warn("User {} does not have access to form {}", utils.getUserId(), code);
				return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
			}
			final String userId = utils.getUserId();
			formService.clone(code, newName, userId);
			return ResponseEntity.ok(null);
		} catch (final Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	@Operation(summary = "Clone Form by Id")
	@PostMapping(value = { "/clonebyId" })
	public ResponseEntity<String> cloneFormById(
			@Parameter(description = "Form id to clone") @RequestParam(required = true) String id,
			@Parameter(description = "New form name") @RequestParam(required = true) String newName) {
		try {
			if (newName == null || newName.trim().length() < 5) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_ERROR_5_CHARACTERS),
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
			String newCode = formService.createCode(newName, utils.getUserId());
			final Form form = formService.getDBForm(newCode);
			if (form != null)
				return new ResponseEntity<>(HttpStatus.CONFLICT);

			if (!formService.hasUserAccess(utils.getUserId(), id, ResourceAccessType.VIEW)) {
				log.warn("User {} does not have access to form {}", utils.getUserId(), id);
				return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
			}
			final String userId = utils.getUserId();
			formService.cloneById(id, newName, userId);
			return ResponseEntity.ok(null);
		} catch (final Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	@Operation(summary = "Clone registry by oid, form code, path parameter and new value")
	@PostMapping(value = { "/clonevalue" })
	public ResponseEntity<String> cloneValue(@RequestBody FormCloneValueDTO form) {
		try {

			if (form.getCloneIdentification() == null || form.getCloneIdentification().trim().length() == 0
					|| form.getForm() == null || form.getForm().trim().length() == 0 || form.getNewValue() == null
					|| form.getNewValue().trim().length() == 0 || form.getOid() == null
					|| form.getOid().trim().length() == 0) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Some empty fields");
			}

			final FormDTO f = formService.getForm(form.getForm());
			if (f == null) {
				return ResponseEntity.notFound().build();
			}
			final String userId = utils.getUserId();
			if (!formService.hasUserAccess(userId, form.getForm(), ResourceAccessType.VIEW)) {
				log.warn("User {} does not have access to form {}", userId, form.getForm());
				return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
			}

			final Ontology ontology = ontologyService.getOntologyByIdentificationInsert(f.getEntity(),
					utils.getUserId());
			if (ontology == null) {
				return ResponseEntity.notFound().build();
			}

			// get data by oid
			String result = datasourceService.getDataById(f.getEntity(), form.getOid(), userId);
			if (result == null || result.length() == 0) {
				return ResponseEntity.notFound().build();
			}

			JsonArray jsonArray = new Gson().fromJson(result, JsonArray.class);
			// update oldvalue with new value
			updateJsonElement(jsonArray.get(0), form.getCloneIdentification(), form.getNewValue());

			String id = crudService.getUniqueColumn(ontology.getIdentification(), true);
			if (id != null && id.length() > 0) {
				if (ontology.getRtdbDatasource().equals(RtdbDatasource.MONGO)) {
					deleteJsonElement(jsonArray.get(0), "_id");
				}

				if (ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)) {
					if (!id.equals(form.getCloneIdentification())) {
						deleteJsonElement(jsonArray.get(0), id);
					} else {
						updateJsonElement(jsonArray.get(0), id,
								Long.toString(Math.abs(UUID.randomUUID().getLeastSignificantBits())));
					}
				} else {
					deleteJsonElement(jsonArray.get(0), id);
				}
			}

			if (ontology.isContextDataEnabled()) {
				deleteJsonElement(jsonArray.get(0), "contextData");
			}

			// create new data
			datasourceService.insertData(ontology.getIdentification(), userId, jsonArray.get(0).toString());

			return ResponseEntity.ok(null);
		} catch (final Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Get Entity parameters")
	@GetMapping("/entityparameters/{entityIdentification}")
//	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> getEntityParameters(@PathVariable("entityIdentification") String entityIdentification) {

		if (entityIdentification == null)
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);

		Map<String, String> result = null;
		try {
			result = ontologyService.getOntologyFieldsQueryTool(entityIdentification, utils.getUserId());
		} catch (IOException e) {

			e.printStackTrace();
		}
		if (result == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(mapToDatasourcesFieldsDTO(result));
	}

	private List<DatasourcesFieldsDTO> mapToDatasourcesFieldsDTO(Map<String, String> result) {
		List<DatasourcesFieldsDTO> list = new ArrayList<>();
		for (Map.Entry<String, String> entry : result.entrySet()) {
			DatasourcesFieldsDTO dto = new DatasourcesFieldsDTO();
			dto.setName(entry.getKey());
			dto.setType(entry.getValue());
			list.add(dto);
		}

		return list;
	}

	private static void updateJsonElement(JsonElement json, String path, String newValue) {
		final String[] parts = path.split("\\.|\\[|\\]");
		JsonElement result = json;
		String key = "";
		for (int i = 0; i < parts.length - 1; i++) {
			key = parts[i];

			key = key.trim();
			if (key.isEmpty())
				continue;

			if (result == null) {
				result = JsonNull.INSTANCE;
				break;
			}

			if (result.isJsonObject()) {
				result = ((JsonObject) result).get(key);
			} else if (result.isJsonArray()) {
				final int ix = Integer.valueOf(key) - 1;
				result = ((JsonArray) result).get(ix);
			} else {
				break;
			}
		}
		key = parts[parts.length - 1];
		JsonElement oldvalue = ((JsonObject) result).get(key);
		if (oldvalue.getAsJsonPrimitive().isString()) {
			((JsonObject) result).addProperty(key, newValue);
		} else if (oldvalue.getAsJsonPrimitive().isBoolean()) {
			((JsonObject) result).addProperty(key, Boolean.valueOf(newValue));
		} else if (oldvalue.getAsJsonPrimitive().isNumber()) {
			((JsonObject) result).addProperty(key, Integer.valueOf(newValue));
		} else if (oldvalue.getAsJsonPrimitive().isJsonNull()) {
			((JsonObject) result).addProperty(key, newValue);
		}
	}

	private static void deleteJsonElement(JsonElement json, String path) {
		final String[] parts = path.split("\\.|\\[|\\]");
		JsonElement result = json;
		String key = "";
		for (int i = 0; i < parts.length - 1; i++) {
			key = parts[i];

			key = key.trim();
			if (key.isEmpty())
				continue;

			if (result == null) {
				result = JsonNull.INSTANCE;
				break;
			}

			if (result.isJsonObject()) {
				result = ((JsonObject) result).get(key);
			} else if (result.isJsonArray()) {
				final int ix = Integer.valueOf(key) - 1;
				result = ((JsonArray) result).get(ix);
			} else {
				break;
			}
		}
		key = parts[parts.length - 1];
		if (((JsonObject) result).get(key) != null) {
			((JsonObject) result).remove(key);
		}
	}

	private JsonElement getJsonElement(JsonElement json, String path) {
		final String[] parts = path.split("\\.|\\[|\\]");
		JsonElement result = json;

		for (String key : parts) {

			key = key.trim();
			if (key.isEmpty())
				continue;

			if (result == null) {
				result = JsonNull.INSTANCE;
				break;
			}

			if (result.isJsonObject()) {
				result = ((JsonObject) result).get(key);
			} else if (result.isJsonArray()) {
				final int ix = Integer.valueOf(key) - 1;
				result = ((JsonArray) result).get(ix);
			} else {
				break;
			}
		}

		return result;
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

	private JsonNode getDatasourcesData(FormDTO f, String data) {
		if (f == null || f.getConfig() == null) {
			return null;
		}

		JSONObject config = new JSONObject(f.getConfig());
		JSONArray datasources = config.getJSONArray("datasources");
		if (datasources == null || datasources.isEmpty()) {
			return null;
		}
		log.info("Look for datasource");

		JsonNode resultDatasouces = MAPPER.createObjectNode();
		for (Iterator iterator = datasources.iterator(); iterator.hasNext();) {
			JSONObject identifierDatasource = (JSONObject) iterator.next();
			GadgetDatasource ds = datasourceService
					.getGadgetDatasourceFromIdentification(identifierDatasource.getString("id"), utils.getUserId());
			InputMessage msg = new InputMessage();
			msg.setLimit(ds.getMaxvalues());
			if (identifierDatasource.has("filters")) {
				JSONArray filters = identifierDatasource.getJSONArray("filters");
				if (filters.length() > 0) {
					for (Iterator iteratorFilters = filters.iterator(); iteratorFilters.hasNext();) {
						JSONObject filter = (JSONObject) iteratorFilters.next();
						FilterStt newFilterStt = new FilterStt();
						newFilterStt.setOp(filter.getString("op"));
						newFilterStt.setField(filter.getString("field"));
						String type = filter.getString("type");
						String condition = filter.getString("condition");
						String conditiontype = filter.getString("conditiontype");
						if (conditiontype.equals("entity") && data != null) {
							JsonArray jsonArray = new Gson().fromJson(data, JsonArray.class);
							JsonElement jsonResult = getJsonElement(jsonArray.get(0), condition);
							condition = jsonResult.getAsString();
							addFilter(msg, newFilterStt, type, condition);

						} else if (conditiontype.equals("value")) {
							addFilter(msg, newFilterStt, type, condition);
						}

					}
				}
			}
			String resultData;
			try {
				log.info("solve query for datasource");
				resultData = datasourceService.solveDatasource(msg, null, ds, utils.getUserId());
				((ObjectNode) resultDatasouces).put(identifierDatasource.getString("id"),
						MAPPER.readValue(resultData, JsonNode.class));
			} catch (DatasourceException | OntologyDataUnauthorizedException | GenericOPException
					| JsonProcessingException | JSONException e) {
				log.info("error");
			}
		}
		return resultDatasouces;
	}

	private void addFilter(InputMessage msg, FilterStt newFilterStt, String type, String condition) {
		// Simple case is a value
		if (type.equals("STRING")) {
			condition = "'" + condition + "'";
		}
		newFilterStt.setExp(condition);
		// if is a filter from value of element form
		if (msg.getFilter() == null) {
			msg.setFilter(new ArrayList<FilterStt>());
		}
		msg.getFilter().add(newFilterStt);
	}

	private int getMaxRegisters() {
		try {
			return ((Integer) resourcesService.getGlobalConfiguration().getEnv().getDatabase().get("queries-limit"))
					.intValue();
		} catch (final Exception e) {
			return queryDefaultLimit;
		}
	}
}
