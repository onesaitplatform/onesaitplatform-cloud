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
package com.minsait.onesait.platform.controlpanel.controller.ontology;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.JsonObject;
import com.minsait.onesait.platform.business.services.ontology.OntologyBusinessService;
import com.minsait.onesait.platform.business.services.ontology.OntologyBusinessServiceException;
import com.minsait.onesait.platform.business.services.ontology.OntologyBusinessServiceException.Error;
import com.minsait.onesait.platform.business.services.swagger.SwaggerApiImporterService;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.ClientPlatformOntology;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.OntologyKPI;
import com.minsait.onesait.platform.config.model.OntologyRest;
import com.minsait.onesait.platform.config.model.OntologyRestHeaders;
import com.minsait.onesait.platform.config.model.OntologyRestOperation;
import com.minsait.onesait.platform.config.model.OntologyRestOperationParam;
import com.minsait.onesait.platform.config.model.OntologyRestSecurity;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesProperty;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow;
import com.minsait.onesait.platform.config.model.OntologyUserAccess;
import com.minsait.onesait.platform.config.model.OntologyVirtual;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource.VirtualDatasourceType;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.ApiRepository;
import com.minsait.onesait.platform.config.repository.ClientPlatformOntologyRepository;
import com.minsait.onesait.platform.config.repository.OntologyKPIRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.services.datamodel.DataModelService;
import com.minsait.onesait.platform.config.services.exceptions.OntologyServiceException;
import com.minsait.onesait.platform.config.services.ontology.OntologyConfiguration;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.ontology.OntologyTimeSeriesService;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyDTO;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyKPIDTO;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyTimeSeriesServiceDTO;
import com.minsait.onesait.platform.config.services.ontology.dto.VirtualDatasourceDTO;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataJsonProblemException;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataService;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataUnauthorizedException;
import com.minsait.onesait.platform.config.services.templates.PlatformQuery;
import com.minsait.onesait.platform.config.services.templates.QueryTemplateService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.rest.management.ontology.model.OntologyVirtualDataSourceDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.ontology.model.sql.CreateStatementDTO;
import com.minsait.onesait.platform.controlpanel.services.resourcesinuse.ResourcesInUseService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.CreateStatement;
import com.minsait.onesait.platform.persistence.factory.ManageDBRepositoryFactory;
import com.minsait.onesait.platform.persistence.interfaces.ManageDBRepository;
import com.minsait.onesait.platform.persistence.services.BasicOpsPersistenceServiceFacade;
import com.minsait.onesait.platform.persistence.services.QueryToolService;
import com.minsait.onesait.platform.quartz.services.ontologyKPI.OntologyKPIService;

import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/ontologies")
@Slf4j
public class OntologyController {

	@Autowired
	private OntologyService ontologyConfigService;
	@Autowired
	private OntologyRepository ontologyRepository;

	@Autowired
	private ApiRepository apiRepository;
	@Autowired
	private ClientPlatformOntologyRepository clientPlatformOntologyRepository;

	// Lazy so in local you can start ControlPanel and no start KPI Module
	@Autowired
	@Lazy
	private OntologyKPIService ontologyKPIService;

	@Autowired
	private OntologyBusinessService ontologyBusinessService;
	@Autowired
	private BasicOpsPersistenceServiceFacade basicOpsRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private QueryToolService queryToolService;
	@Autowired
	private OntologyDataService ontologyDataService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private SwaggerApiImporterService swaggerApiImporterService;
	@Autowired
	private ManageDBRepositoryFactory manageFactory;

	@Autowired
	private OntologyKPIRepository ontologyKPIRepository;
	@Autowired
	private OntologyTimeSeriesService ontologyTimeSeriesService;
	@Autowired
	private DataModelService dataModelService;
	@Autowired
	private QueryTemplateService queryTemplateService;
	@Autowired
	private ResourcesInUseService resourcesInUseService;

	private static final String ONTOLOGIES_STR = "ontologies";
	private static final String ONTOLOGY_STR = "ontology";
	private static final String ONTOLOGY_REST_STR = "ontologyRest";
	private static final String ONTOLOGIES_CREATE = "ontologies/create";
	private static final String ONTOLOGIES_CREATE_TS = "ontologies/createtimeseries";
	private static final String ONTOLOGIES_LIST = "ontologies/list";
	private static final String REDIRECT_ONTOLOGY_LIST = "/controlpanel/ontologies/list";
	private static final String ERROR_STR = "error";
	private static final String MESSAGE_STR = "message";
	private static final String STATUS_STR = "status";
	private static final String VAL_ERROR = "validation error";
	private static final String ONT_VAL_ERROR = "ontology.validation.error";
	private static final String ONT_DEL_ERROR = "ontology.delete.error";
	private static final String CAUSE_STR = "cause";
	private static final String REDIRECT_STR = "redirect";
	private static final String GEN_INTERN_ERROR_CREATE_ONT = "Generic internal error creating ontology: ";
	private static final String REDIRECT_ONTOLOGIES_LIST = "redirect:/ontologies/list";
	private static final String DATA_MODELS_STR = "dataModels";
	private static final String DATA_MODEL_TYPES_STR = "dataModelTypes";
	private static final String RTDBS = "rtdbs";
	private static final String QUERY_RESULT = "queryResult";
	private static final String QUERY_TOOL_SHOW_QUERY = "querytool/show :: query";
	private static final String ERROR_IN_RUNQUERY = "Error in runQuery";
	private static final String TIMESERIES_DATAMODEL = "MASTER-DataModel-30";
	private static final String ONTOLOGYTSDTO = "ontologyTSDTO";
	private static final String AUTHORIZATIONS = "authorizations";
	private static final String USERS = "users";
	private static final String PROPERTY_NAMES = "propertyNames";

	private final ObjectMapper mapper = new ObjectMapper();

	@GetMapping(value = "/listAll", produces = "text/html")
	public String list(Model model, HttpServletRequest request,
			@RequestParam(required = false, name = "identification") String identification,
			@RequestParam(required = false, name = "description") String description) {

		// Scaping "" string values for parameters
		if (identification != null && identification.equals("")) {
			identification = null;
		}
		if (description != null && description.equals("")) {
			description = null;
		}

		final List<OntologyDTO> ontologies = ontologyConfigService.getAllOntologiesForList(utils.getUserId(),
				identification, description);
		model.addAttribute(ONTOLOGIES_STR, ontologies);
		model.addAttribute("filterCheck", false);
		return ONTOLOGIES_LIST;
	}

	@GetMapping(value = "/list", produces = "text/html")
	public String listAll(Model model, HttpServletRequest request,
			@RequestParam(required = false, name = "identification") String identification,
			@RequestParam(required = false, name = "description") String description) {

		// Scaping "" string values for parameters
		if (identification != null && identification.equals("")) {
			identification = null;
		}
		if (description != null && description.equals("")) {
			description = null;
		}

		List<OntologyDTO> ontologies = ontologyConfigService.getOntologiesForListByUserPropietary(utils.getUserId(),
				identification, description);

		model.addAttribute(ONTOLOGIES_STR, ontologies);
		model.addAttribute("filterCheck", true);
		return ONTOLOGIES_LIST;
	}

	@PostMapping("/getNamesForAutocomplete")
	public @ResponseBody List<String> getNamesForAutocomplete() {
		return ontologyConfigService.getAllIdentificationsByUser(utils.getUserId());
	}

	@GetMapping(value = "/create")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public String create(Model model) {
		model.addAttribute(ONTOLOGY_STR, new Ontology());
		model.addAttribute(ONTOLOGY_REST_STR, new OntologyRestDTO());
		populateForm(model);
		return ONTOLOGIES_CREATE;
	}

	@GetMapping(value = "/createwizard", produces = "text/html")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public String createWizard(Model model) {
		Ontology ontology = (Ontology) model.asMap().get(ONTOLOGY_STR);
		if (ontology == null) {
			ontology = new Ontology();
			ontology.setPublic(false);
			model.addAttribute(ONTOLOGY_STR, ontology);
		} else {
			ontology.setId(null);
			ontology.setPublic(false);
			model.addAttribute(ONTOLOGY_STR, ontology);
		}

		populateForm(model);
		return "ontologies/createwizard";
	}

	@GetMapping(value = "/createapirest", produces = "text/html")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public String createAPIREST(Model model) {

		model.addAttribute(ONTOLOGY_STR, new Ontology());
		populateFormApiRest(model);
		return "ontologies/createapirest";
	}

	@GetMapping(value = "/createvirtual", produces = "text/html")
	public String createVirtual(Model model) {

		model.addAttribute(ONTOLOGY_STR, new Ontology());
		populateFormVirtual(model);
		return "ontologies/createvirtual";
	}

	@GetMapping(value = "/createkpi", produces = "text/html")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public String createkpi(Model model) {
		OntologyKPIDTO ontology = (OntologyKPIDTO) model.asMap().get(ONTOLOGY_STR);
		if (ontology == null) {
			ontology = new OntologyKPIDTO();
			ontology.setPublic(false);
			model.addAttribute(ONTOLOGY_STR, ontology);
		} else {
			ontology.setId(null);
			ontology.setPublic(false);
			model.addAttribute(ONTOLOGY_STR, ontology);
		}

		populateKPIForm(model);
		return "ontologies/createkpi";
	}

	@GetMapping(value = "/createtimeseries", produces = "text/html")
	public String createTimeSeries(Model model) {

		model.addAttribute(ONTOLOGYTSDTO, new OntologyTimeSeriesServiceDTO());
		populateFormTimeseries(model);
		return ONTOLOGIES_CREATE_TS;
	}

	@PostMapping(value = { "/create", "/createwizard", "/createapirest", "/createvirtual" })
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public ResponseEntity<Map<String, String>> createOntology(Model model, @Valid Ontology ontology,
			BindingResult bindingResult, RedirectAttributes redirect, HttpServletRequest request) {
		final Map<String, String> response = new HashMap<>();

		if (bindingResult.hasErrors()) {
			response.put(STATUS_STR, ERROR_STR);
			response.put(CAUSE_STR, utils.getMessage(ONT_VAL_ERROR, VAL_ERROR));
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

		try {
			ontology.setOntologyKPI(null);
			final OntologyConfiguration config = new OntologyConfiguration(request);
			ontologyBusinessService.createOntology(ontology, utils.getUserId(), config);

			if (ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)
					|| ontology.getRtdbDatasource().equals(RtdbDatasource.API_REST)) {
				response.put(REDIRECT_STR, REDIRECT_ONTOLOGY_LIST);
			}
			response.put(STATUS_STR, "ok");
			return new ResponseEntity<>(response, HttpStatus.CREATED);

		} catch (final OntologyBusinessServiceException e) {
			log.error("Error creating ontology", e);
			final Error error = e.getError();
			switch (error) {
			case ILLEGAL_ARGUMENT:
				response.put(STATUS_STR, ERROR_STR);
				response.put(CAUSE_STR, utils.getMessage(ONT_VAL_ERROR, VAL_ERROR));
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			case NO_VALID_SCHEMA:
				response.put(STATUS_STR, ERROR_STR);
				response.put(CAUSE_STR, "Invalid json schema: " + e.getMessage());
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			case KAFKA_TOPIC_CREATION_ERROR:
			case CONFIG_CREATION_ERROR:
			case CONFIG_CREATION_ERROR_UNCLEAN:
			case PERSISTENCE_CREATION_ERROR:
			case PERSISTENCE_CREATION_ERROR_UNCLEAN:
				log.error("Cannot create ontology because of: " + e.getMessage());
				response.put(STATUS_STR, ERROR_STR);
				response.put(CAUSE_STR, e.getMessage());
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			default:
				log.error(GEN_INTERN_ERROR_CREATE_ONT + e.getMessage());
				response.put(STATUS_STR, ERROR_STR);
				response.put(CAUSE_STR, e.getMessage());
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (final Exception e) {
			log.error(GEN_INTERN_ERROR_CREATE_ONT + e.getMessage());
			response.put(STATUS_STR, ERROR_STR);
			response.put(CAUSE_STR, e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping(value = { "/createtimeseries" })
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public ResponseEntity<Map<String, String>> createTimeseriesOntology(Model model,
			@Valid OntologyTimeSeriesServiceDTO ontologyTimeSeriesDTO, BindingResult bindingResult,
			RedirectAttributes redirect, HttpServletRequest request) {
		final Map<String, String> response = new HashMap<>();

		if (bindingResult.hasErrors()) {
			response.put(STATUS_STR, ERROR_STR);
			response.put(CAUSE_STR, utils.getMessage(ONT_VAL_ERROR, VAL_ERROR));
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

		if (ontologyBusinessService.existsOntology(ontologyTimeSeriesDTO.getIdentification())) {
			response.put(STATUS_STR, ERROR_STR);
			response.put(CAUSE_STR, "Exists another ontology with this identification");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		try {
			ontologyTimeSeriesDTO.setUser(userService.getUser(utils.getUserId()));
			final OntologyConfiguration config = new OntologyConfiguration(request);
			final Ontology createdOnt = ontologyTimeSeriesService.createOntologyTimeSeries(ontologyTimeSeriesDTO,
					config, true, true);
			if (createdOnt != null) {
				response.put(REDIRECT_STR, REDIRECT_ONTOLOGY_LIST);
				response.put(STATUS_STR, "ok");
				return new ResponseEntity<>(response, HttpStatus.CREATED);
			} else {
				response.put(REDIRECT_STR, "/controlpanel/ontologies/list");
				response.put(STATUS_STR, "ko");
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}

		} catch (final OntologyServiceException | OntologyDataJsonProblemException e) {
			log.error("Error creating ontology TimeSeries", e);

			log.error(GEN_INTERN_ERROR_CREATE_ONT, e);
			response.put(STATUS_STR, ERROR_STR);
			response.put(CAUSE_STR, e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);

		} catch (final Exception e) {
			log.error(GEN_INTERN_ERROR_CREATE_ONT, e);
			response.put(STATUS_STR, ERROR_STR);
			response.put(CAUSE_STR, e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Transactional
	@PostMapping(value = { "/createkpi" })
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public ResponseEntity<Map<String, String>> createKPIOntology(Model model, @Valid OntologyKPIDTO ontologyKPIDTO,
			BindingResult bindingResult, RedirectAttributes redirect, HttpServletRequest request) {

		final Map<String, String> response = new HashMap<>();

		if (bindingResult.hasErrors()) {
			response.put(STATUS_STR, ERROR_STR);
			response.put(CAUSE_STR, utils.getMessage(ONT_VAL_ERROR, VAL_ERROR));
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

		if (!ontologyKPIDTO.isNewOntology()) {
			// Find Existing ontology and validate if has a KPI information.

			final Ontology ontology = ontologyRepository.findByIdentificationIgnoreCase(ontologyKPIDTO.getId()).get(0);
			final List<OntologyKPI> kpis = ontologyKPIRepository.findByOntology(ontology);
			// if exist ontology with kpi
			if (!kpis.isEmpty()) {
				response.put(STATUS_STR, ERROR_STR);
				response.put(CAUSE_STR, utils.getMessage(ONT_VAL_ERROR, "the ontology has a kpi associated"));
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}

			return createKPIinDB(ontologyKPIDTO, response, ontology, utils.getUserId());
		} else {

			try {

				final Ontology ontology = createOntologyKPI(ontologyKPIDTO, request);

				return createKPIinDB(ontologyKPIDTO, response, ontology, utils.getUserId());

			} catch (final OntologyBusinessServiceException e) {
				final Error error = e.getError();
				switch (error) {
				case ILLEGAL_ARGUMENT:
					response.put(STATUS_STR, ERROR_STR);
					response.put(CAUSE_STR, utils.getMessage(ONT_VAL_ERROR, VAL_ERROR));
					return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
				case NO_VALID_SCHEMA:
					response.put(STATUS_STR, ERROR_STR);
					response.put(CAUSE_STR, "Invalid json schema: " + e.getMessage());
					return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
				case KAFKA_TOPIC_CREATION_ERROR:
				case CONFIG_CREATION_ERROR:
				case CONFIG_CREATION_ERROR_UNCLEAN:
				case PERSISTENCE_CREATION_ERROR:
				case PERSISTENCE_CREATION_ERROR_UNCLEAN:
					log.error("Cannot create ontology because of: " + e.getMessage());
					response.put(STATUS_STR, ERROR_STR);
					response.put(CAUSE_STR, e.getMessage());
					return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
				default:
					log.error(GEN_INTERN_ERROR_CREATE_ONT + e.getMessage());
					response.put(STATUS_STR, ERROR_STR);
					response.put(CAUSE_STR, e.getMessage());
					return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} catch (final Exception e) {
				log.error(GEN_INTERN_ERROR_CREATE_ONT + e.getMessage());
				response.put(STATUS_STR, ERROR_STR);
				response.put(CAUSE_STR, e.getMessage());
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}

		}

	}

	private Ontology createOntologyKPI(OntologyKPIDTO ontologyKPIDTO, HttpServletRequest request)
			throws IOException, OntologyBusinessServiceException {
		final Ontology ontology = new Ontology();
		ontology.setJsonSchema(ontologyBusinessService
				.completeSchema(ontologyKPIDTO.getSchema(), ontologyKPIDTO.getName(), ontologyKPIDTO.getDescription())
				.toString());
		ontology.setIdentification(ontologyKPIDTO.getName());
		ontology.setActive(ontologyKPIDTO.isActive());
		ontology.setPublic(ontologyKPIDTO.isPublic());
		ontology.setDataModel(dataModelService.getDataModelByName(OntologyService.DATAMODEL_DEFAULT_NAME));
		ontology.setDescription(ontologyKPIDTO.getDescription());
		ontology.setUser(userService.getUser(utils.getUserId()));
		ontology.setMetainf(ontologyKPIDTO.getMetainf());
		ontology.setRtdbDatasource(Ontology.RtdbDatasource.valueOf(ontologyKPIDTO.getDatasource()));

		final OntologyConfiguration config = new OntologyConfiguration(request);
		ontologyBusinessService.createOntology(ontology, utils.getUserId(), config);
		return ontology;
	}

	@GetMapping(value = "/update/{id}", produces = "text/html")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public String update(Model model, @PathVariable("id") String id) {
		try {
			final Ontology ontology = ontologyConfigService.getOntologyById(id, utils.getUserId());
			if (ontology != null) {

				final List<OntologyUserAccess> authorizations = ontologyConfigService
						.getOntologyUserAccesses(ontology.getId(), utils.getUserId());
				final List<OntologyUserAccessDTO> authorizationsDTO = new ArrayList<>();

				for (final OntologyUserAccess authorization : authorizations) {
					if (authorization.getUser().isActive()) {
						authorizationsDTO.add(new OntologyUserAccessDTO(authorization));
					}
				}

				final List<User> users = userService.getAllActiveUsers();

				OntologyTimeSeriesServiceDTO otsDTO = new OntologyTimeSeriesServiceDTO();
				if (ontology.getDataModel().getId().equals(TIMESERIES_DATAMODEL)) {
					otsDTO = ontologyTimeSeriesService.generateOntologyTimeSeriesDTO(ontology);
				}

				model.addAttribute(AUTHORIZATIONS, authorizationsDTO);
				model.addAttribute(ONTOLOGY_STR, ontology);
				model.addAttribute(ONTOLOGYTSDTO, otsDTO);
				model.addAttribute(USERS, users);

				// InUseService
				if (resourcesInUseService != null) {
					model.addAttribute(ResourcesInUseService.RESOURCEINUSE,
							resourcesInUseService.isInUse(id, utils.getUserId()));
					resourcesInUseService.put(id, utils.getUserId());
				}
				if (ontology.getRtdbDatasource().equals(RtdbDatasource.API_REST)) {
					final OntologyRest ontologyRest = ontologyConfigService.getOntologyRestByOntologyId(ontology);
					populateRestForm(model, ontologyRest);
					populateFormApiRest(model);
					return "ontologies/createapirest";
				} else if (ontology.getRtdbDatasource().equals(RtdbDatasource.KUDU)) {
					final HashMap<String, String> dbProperties = ontologyBusinessService.getAditionalDBConfig(ontology);
					for (final Map.Entry<String, String> entry : dbProperties.entrySet()) {
						model.addAttribute(entry.getKey(), entry.getValue());
					}
				}
				if (ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)) {
					final OntologyVirtual ontologyVirtual = ontologyConfigService
							.getOntologyVirtualByOntologyId(ontology);
					populateFormVirtual(model);
					model.addAttribute("datasource", ontologyVirtual.getDatasourceId());
					model.addAttribute("tableName", ontologyVirtual.getDatasourceTableName());
					model.addAttribute("objId", ontologyVirtual.getObjectId());
					model.addAttribute("objGeometry", ontologyVirtual.getObjectGeometry());
					return "ontologies/createvirtual";
				}
				if (ontology.getDataModel().getId().equals(TIMESERIES_DATAMODEL)) {

					populateFormTimeseries(model);
					return ONTOLOGIES_CREATE_TS;
				} else {
					model.addAttribute(ONTOLOGY_REST_STR, new OntologyRestDTO());
					populateForm(model);
				}

				return "ontologies/createwizard";

			} else {
				return ONTOLOGIES_CREATE;
			}
		} catch (final RuntimeException e) {
			return ONTOLOGIES_CREATE;
		}
	}

	@GetMapping(value = "/updatetimeseries/{id}", produces = "text/html")
	public String updateTimeseries(Model model, @PathVariable("id") String id) {
		try {
			final Ontology ontology = ontologyConfigService.getOntologyById(id, utils.getUserId());
			if (ontology != null) {

				final List<OntologyUserAccess> authorizations = ontologyConfigService
						.getOntologyUserAccesses(ontology.getId(), utils.getUserId());
				final List<OntologyUserAccessDTO> authorizationsDTO = new ArrayList<>();

				for (final OntologyUserAccess authorization : authorizations) {
					if (authorization.getUser().isActive()) {
						authorizationsDTO.add(new OntologyUserAccessDTO(authorization));
					}
				}

				final List<User> users = userService.getAllActiveUsers();

				OntologyTimeSeriesServiceDTO otsDTO = new OntologyTimeSeriesServiceDTO();
				if (ontology.getDataModel().getId().equals(TIMESERIES_DATAMODEL)) {
					otsDTO = ontologyTimeSeriesService.generateOntologyTimeSeriesDTO(ontology);
					populateFormTimeseries(model);
				}

				model.addAttribute(AUTHORIZATIONS, authorizationsDTO);
				model.addAttribute(ONTOLOGY_STR, ontology);
				model.addAttribute(ONTOLOGYTSDTO, otsDTO);
				model.addAttribute(PROPERTY_NAMES, getPropertyNames(otsDTO.getTimeSeriesProperties()));
				model.addAttribute(USERS, users);

				model.addAttribute(ResourcesInUseService.RESOURCEINUSE,
						resourcesInUseService.isInUse(id, utils.getUserId()));
				resourcesInUseService.put(id, utils.getUserId());

				return ONTOLOGIES_CREATE_TS;
			} else {
				return ONTOLOGIES_CREATE;
			}
		} catch (final RuntimeException e) {
			return ONTOLOGIES_CREATE;
		}
	}

	private void populateRestForm(Model model, OntologyRest ontologyRest) {

		final List<OntologyRestOperationDTO> lOperationsDTO = new ArrayList<>();

		final OntologyRestSecurity security = ontologyConfigService.getOntologyRestSecurityByOntologyRest(ontologyRest);
		final OntologyRestHeaders headers = ontologyConfigService.getOntologyRestHeadersByOntologyRest(ontologyRest);
		final List<OntologyRestOperation> lOperations = ontologyConfigService.getOperationsByOntologyRest(ontologyRest);
		for (final OntologyRestOperation operation : lOperations) {
			final List<OntologyRestOperationParam> lOperationsParams = ontologyConfigService
					.getOperationsParamsByOperation(operation);

			final List<OntologyRestOperationParamDTO> params = new ArrayList<>();

			for (final OntologyRestOperationParam operationParam : lOperationsParams) {
				params.add(new OntologyRestOperationParamDTO(operationParam.getIndexParam(), operationParam.getName(),
						operationParam.getType().name(), operationParam.getField()));
			}

			lOperationsDTO.add(new OntologyRestOperationDTO(operation.getName(), operation.getPath(),
					operation.getType().name(), operation.getDefaultOperationType().name(), operation.getOrigin(),
					operation.getDescription(), params));

		}

		final OntologyRestDTO ontologyRestDTO = new OntologyRestDTO(ontologyRest.getBaseUrl(),
				ontologyRest.getSecurityType().name(), security.getConfig(), headers.getConfig(), lOperationsDTO,
				ontologyRest.getJsonSchema());

		model.addAttribute(ONTOLOGY_REST_STR, ontologyRestDTO);

	}

	@PutMapping(value = "/update/{id}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public ResponseEntity<Map<String, String>> updateOntology(Model model, @PathVariable("id") String id,
			@Valid Ontology ontology, BindingResult bindingResult, RedirectAttributes redirect,
			HttpServletRequest request)
			throws DBPersistenceException, OntologyDataUnauthorizedException, GenericOPException {
		final Map<String, String> response = new HashMap<>();
		if (bindingResult.hasErrors()) {
			log.debug("Some ontology properties missing");
			response.put(STATUS_STR, ERROR_STR);
			response.put(CAUSE_STR, utils.getMessage(ONT_VAL_ERROR, VAL_ERROR));
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

		try {

			long count = 0;
			if (ontology.getRtdbDatasource() != RtdbDatasource.API_REST) {
				count = basicOpsRepository.count(ontology.getIdentification());

			}

			final OntologyConfiguration config = new OntologyConfiguration(request);

			// Get KPI ID by looking for the ontology in DDBB because HTML doesn't retrieve
			// it
			final Ontology ontologyFound = ontologyConfigService.getOntologyById(id, utils.getUserId());
			if (ontologyFound != null && ontologyFound.getOntologyKPI() != null
					&& ontologyFound.getOntologyKPI().getId() != null) {
				ontology.getOntologyKPI().setId(ontologyFound.getOntologyKPI().getId());
				ontology.getOntologyKPI().setUser(ontologyFound.getOntologyKPI().getUser());
				ontology.getOntologyKPI().setActive(ontologyFound.getOntologyKPI().isActive());
				ontology.getOntologyKPI().setJobName(ontologyFound.getOntologyKPI().getJobName());
				ontology.getOntologyKPI().setOntology(ontology);

			}

			if (ontology != null && ontology.getOntologyKPI() != null && ontology.getOntologyKPI().getId() == null) {
				ontology.setOntologyKPI(null);
			}

			ontologyBusinessService.updateOntology(ontology, config, count > 0 ? true : false);

			ontologyConfigService.updateOntology(ontology, utils.getUserId(), config, count > 0 ? true : false);

			if (ontologyFound != null && ontologyFound.getOntologyKPI() != null
					&& ontologyFound.getOntologyKPI().getId() != null) {
				ontologyKPIRepository.save(ontology.getOntologyKPI());
			}

			if (ontologyFound != null && ontologyFound.getOntologyKPI() != null
					&& ontologyFound.getOntologyKPI().getId() != null && ontology.getOntologyKPI().isActive()) {

				ontologyKPIService.unscheduleKpi(ontology.getOntologyKPI());
				ontologyKPIService.scheduleKpi(ontology.getOntologyKPI());

			}

		} catch (final OntologyServiceException | OntologyDataJsonProblemException e) {
			log.error("Cannot update ontology {}", e.getMessage());
			response.put(STATUS_STR, ERROR_STR);
			response.put(CAUSE_STR, e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (final DBPersistenceException e) {
			log.error("Could not check if ontology has documents in RTDB");
			response.put(STATUS_STR, ERROR_STR);
			response.put(CAUSE_STR, e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);

		} catch (final OntologyBusinessServiceException e) {
			final Error error = e.getError();
			switch (error) {
			case PERSISTENCE_CREATION_ERROR:
			case PERSISTENCE_CREATION_ERROR_UNCLEAN:
				log.error("Cannot update ontology because of: " + e.getMessage());
				response.put(STATUS_STR, ERROR_STR);
				response.put(CAUSE_STR, e.getMessage());
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			default:
				log.error(GEN_INTERN_ERROR_CREATE_ONT + e.getMessage());
				response.put(STATUS_STR, ERROR_STR);
				response.put(CAUSE_STR, e.getMessage());
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);

			}
		}
		resourcesInUseService.removeByUser(id, utils.getUserId());
		response.put(STATUS_STR, "ok");
		response.put(REDIRECT_STR, "/controlpanel/ontologies/show/" + id);
		return new ResponseEntity<>(response, HttpStatus.ACCEPTED);

	}

	@PutMapping(value = "/updatetimeseries/{id}")
	public ResponseEntity<Map<String, String>> updateOntologyTimeseries(Model model, @PathVariable("id") String id,
			OntologyTimeSeriesServiceDTO ontologyDTO, BindingResult bindingResult, RedirectAttributes redirect,
			HttpServletRequest request) throws OntologyBusinessServiceException {
		final Map<String, String> response = new HashMap<>();
		final Ontology ontology = ontologyConfigService.getOntologyById(id, utils.getUserId());
		ontologyDTO.setIdentification(ontology.getIdentification());
		if (bindingResult.hasErrors()) {
			log.debug("Some ontology properties missing");
			response.put(STATUS_STR, ERROR_STR);
			response.put(CAUSE_STR, utils.getMessage(ONT_VAL_ERROR, VAL_ERROR));
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

		try {
			final OntologyConfiguration config = new OntologyConfiguration(request);
			ontologyTimeSeriesService.updateOntologyTimeSeries(ontologyDTO, utils.getUserId(), config);

		} catch (final OntologyServiceException | OntologyDataJsonProblemException e) {
			log.error("Cannot update ontology {}", e.getMessage());
			response.put(STATUS_STR, ERROR_STR);
			response.put(CAUSE_STR, e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		resourcesInUseService.removeByUser(id, utils.getUserId());
		response.put(STATUS_STR, "ok");
		response.put(REDIRECT_STR, "/controlpanel/ontologies/show/" + id);
		return new ResponseEntity<>(response, HttpStatus.ACCEPTED);

	}

	@Transactional
	@DeleteMapping("/{id}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public String delete(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {

		final Ontology ontology = ontologyConfigService.getOntologyById(id, utils.getUserId());
		if (ontology != null) {
			try {
				if (ontology.getOntologyKPI() != null) {
					ontologyKPIService.unscheduleKpi(ontology.getOntologyKPI());
				}
				ontologyBusinessService.deleteOntology(id, utils.getUserId());

			} catch (final Exception e) {
				if (e.getCause() instanceof ConstraintViolationException) {
					String apis = "";
					String iotClients = "";
					for (final Api api : apiRepository.findByOntology(ontology)) {
						apis = apis + api.getIdentification() + " ";
					}
					for (final ClientPlatformOntology cpo : clientPlatformOntologyRepository.findByOntology(ontology)) {
						iotClients = iotClients + cpo.getClientPlatform().getIdentification() + " ";
					}
					if (apis.equals("") && iotClients.equals(""))
						utils.addRedirectMessageWithParam(ONT_DEL_ERROR,
								"Cannot delete an ontology with attached elements", redirect);
					else
						utils.addRedirectMessageWithParam(ONT_DEL_ERROR,
								"Cannot delete an ontology with attached elements:\n -API: " + apis + "\n"
										+ "-IoTClient: " + iotClients,
								redirect);
				} else {
					utils.addRedirectMessageWithParam(ONT_DEL_ERROR, e.getMessage(), redirect);
				}
				log.error("Error deleting ontology. ", e);
				return "redirect:/ontologies/update/" + id;
			}
			return REDIRECT_ONTOLOGIES_LIST;
		} else {
			return REDIRECT_ONTOLOGIES_LIST;
		}
	}

	@GetMapping("/show/{id}")
	public String show(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		try {
			final Ontology ontology = ontologyConfigService.getOntologyById(id, utils.getUserId());
			if (ontology != null) {

				final List<OntologyUserAccess> authorizations = ontologyConfigService
						.getOntologyUserAccesses(ontology.getId(), utils.getUserId());
				final List<OntologyUserAccessDTO> authorizationsDTO = new ArrayList<>();

				for (final OntologyUserAccess authorization : authorizations) {
					if (authorization.getUser().isActive()) {
						authorizationsDTO.add(new OntologyUserAccessDTO(authorization));
					}
				}

				final List<User> users = userService.getAllActiveUsers();
				OntologyTimeSeriesServiceDTO otsDTO = new OntologyTimeSeriesServiceDTO();
				if (ontology.getDataModel().getId().equals(TIMESERIES_DATAMODEL)) {
					otsDTO = ontologyTimeSeriesService.generateOntologyTimeSeriesDTO(ontology);
				}
				model.addAttribute(ONTOLOGYTSDTO, otsDTO);
				model.addAttribute(ONTOLOGY_STR, ontology);

				model.addAttribute(AUTHORIZATIONS, authorizationsDTO);
				model.addAttribute(USERS, users);

				if (ontology.getRtdbDatasource().equals(RtdbDatasource.API_REST)) {
					final OntologyRest ontologyRest = ontologyConfigService.getOntologyRestByOntologyId(ontology);

					if (ontologyRest != null) {
						populateRestForm(model, ontologyRest);
						return "ontologies/showapirest";
					} else {
						utils.addRedirectMessage("ontology.notfound.error", redirect);
						return REDIRECT_ONTOLOGIES_LIST;
					}
				} else {
					model.addAttribute("isOntologyRest", false);
				}

				return "ontologies/show";

			} else {
				utils.addRedirectMessage("ontology.notfound.error", redirect);
				return REDIRECT_ONTOLOGIES_LIST;
			}
		} catch (final OntologyServiceException e) {
			return REDIRECT_ONTOLOGIES_LIST;
		}
	}

	@GetMapping("/getFromId/{identification}")
	public @ResponseBody Ontology getFromIdentification(@PathVariable("identification") String identification) {
		try {
			final Ontology ontology = ontologyConfigService.getOntologyByIdentification(identification,
					utils.getUserId());
			if (ontology != null) {
				return ontology;
			} else {
				return null;
			}
		} catch (final OntologyServiceException e) {
			return null;
		}
	}

	private void populateForm(Model model) {
		model.addAttribute(DATA_MODELS_STR, ontologyConfigService.getAllDataModels());
		model.addAttribute(DATA_MODEL_TYPES_STR, ontologyConfigService.getAllDataModelTypes());
		model.addAttribute(RTDBS, ontologyConfigService.getDatasources());
		model.addAttribute(ONTOLOGIES_STR, ontologyConfigService.getOntologiesByUserId(utils.getUserId()));
		model.addAttribute("modes", Ontology.RtdbToHdbStorage.values());
	}

	private void populateKPIForm(Model model) {
		model.addAttribute(DATA_MODELS_STR, ontologyConfigService.getAllDataModels());
		model.addAttribute(DATA_MODEL_TYPES_STR, ontologyConfigService.getAllDataModelTypes());
		model.addAttribute(RTDBS, ontologyConfigService.getDatasources());
		model.addAttribute(ONTOLOGIES_STR, ontologyConfigService.getOntologiesByUserId(utils.getUserId()));

	}

	private void populateFormApiRest(Model model) {
		model.addAttribute(DATA_MODELS_STR, ontologyConfigService.getEmptyBaseDataModel());
		model.addAttribute(DATA_MODEL_TYPES_STR, ontologyConfigService.getAllDataModelTypes());
		model.addAttribute(RTDBS, ontologyConfigService.getDatasources());
	}

	private void populateFormVirtual(Model model) {
		model.addAttribute(DATA_MODELS_STR, ontologyConfigService.getEmptyBaseDataModel());
		model.addAttribute(DATA_MODEL_TYPES_STR, ontologyConfigService.getAllDataModelTypes());
		model.addAttribute(RTDBS, ontologyConfigService.getDatasources());

		model.addAttribute("fieldTypes", ontologyBusinessService.getStringSupportedFieldDataTypes());

		model.addAttribute("constraintTypes", ontologyBusinessService.getStringSupportedConstraintTypes());

		if (ontologyConfigService.getDatasourcesRelationals().isEmpty()) {

			model.addAttribute("datasources", new ArrayList<OntologyVirtualDatasource>());
			model.addAttribute("collectionNames", new ArrayList<String>());

		} else {

			List<VirtualDatasourceDTO> dsList;
			if (utils.getRole().equals("ROLE_ADMINISTRATOR"))
				dsList = ontologyConfigService.getDatasourcesRelationals();
			else
				dsList = ontologyConfigService.getPublicOrOwnedDatasourcesRelationals(utils.getUserId());

			model.addAttribute("datasources", dsList);
			model.addAttribute("collectionNames", new ArrayList<String>());

		}

		model.addAttribute("datasource", new OntologyVirtualDatasource());
	}

	private void populateFormTimeseries(Model model) {
		model.addAttribute(DATA_MODELS_STR, ontologyConfigService.getEmptyBaseDataModel());
		model.addAttribute(DATA_MODEL_TYPES_STR, ontologyConfigService.getAllDataModelTypes());
		model.addAttribute(RTDBS, ontologyConfigService.getDatasources());
		model.addAttribute("timeseriesengs", Ontology.RtdbDatasource.MONGO);
		model.addAttribute("types", OntologyTimeSeriesProperty.PropertyDataType.values());
		model.addAttribute("windowtypes", OntologyTimeSeriesWindow.WindowType.values());
		model.addAttribute("frequencies", OntologyTimeSeriesWindow.FrecuencyUnit.values());
		model.addAttribute("aggregates", Arrays.asList(OntologyTimeSeriesWindow.AggregationFunction.LAST));
		model.addAttribute("erasefreqs", OntologyTimeSeriesWindow.RetentionUnit.values());
	}

	@PostMapping(value = "/authorization", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public ResponseEntity<OntologyUserAccessDTO> createAuthorization(@RequestParam String accesstype,
			@RequestParam String ontology, @RequestParam String user) {

		try {
			final OntologyUserAccess ontologyUserAccessCreated = ontologyConfigService.createUserAccess(ontology, user,
					accesstype, utils.getUserId());
			final OntologyUserAccessDTO ontologyUserAccessDTO = new OntologyUserAccessDTO(ontologyUserAccessCreated);
			return new ResponseEntity<>(ontologyUserAccessDTO, HttpStatus.CREATED);

		} catch (final RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

	}

	@PostMapping(value = "/authorization/delete", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public ResponseEntity<String> deleteAuthorization(@RequestParam String id) {

		try {
			ontologyConfigService.deleteOntologyUserAccess(id, utils.getUserId());
			return new ResponseEntity<>("{\"status\" : \"ok\"}", HttpStatus.OK);
		} catch (final RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping(value = "/authorization/update", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public @ResponseBody ResponseEntity<OntologyUserAccessDTO> updateAuthorization(@RequestParam String id,
			@RequestParam String accesstype) {

		try {
			ontologyConfigService.updateOntologyUserAccess(id, accesstype, utils.getUserId());
			final OntologyUserAccess ontologyUserAccessCreated = ontologyConfigService.getOntologyUserAccessById(id,
					utils.getUserId());
			final OntologyUserAccessDTO ontologyUserAccessDTO = new OntologyUserAccessDTO(ontologyUserAccessCreated);
			return new ResponseEntity<>(ontologyUserAccessDTO, HttpStatus.OK);
		} catch (final RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = "/authorization", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public @ResponseBody ResponseEntity<List<OntologyUserAccessDTO>> getAuthorizations(@RequestParam("id") String id) {

		try {
			final Ontology ontology = ontologyConfigService.getOntologyById(id, utils.getUserId());

			final List<OntologyUserAccess> authorizations = ontologyConfigService
					.getOntologyUserAccesses(ontology.getId(), utils.getUserId());
			final List<OntologyUserAccessDTO> authorizationsDTO = new ArrayList<>();
			for (final OntologyUserAccess authorization : authorizations) {
				if (authorization.getUser().isActive()) {
					authorizationsDTO.add(new OntologyUserAccessDTO(authorization));
				}
			}
			return new ResponseEntity<>(authorizationsDTO, HttpStatus.OK);
		} catch (final RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = "/getTables/{datasource}")
	public @ResponseBody ResponseEntity<?> getTables(@PathVariable("datasource") String datasource) {
		try {
			final List<String> tables = ontologyBusinessService.getTablesFromDatasource(datasource);
			if (tables.isEmpty())
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			else
				return new ResponseEntity<>(tables, HttpStatus.OK);
		} catch (final Exception e) {
			return new ResponseEntity<>("Error processing the request: " + e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = "/hasDocuments/{id}")
	public @ResponseBody ResponseEntity<?> hasDocuments(@PathVariable("id") String id) {
		try {
			final Ontology ontology = ontologyConfigService.getOntologyById(id, utils.getUserId());

			long count = 0;
			if (ontology.getRtdbDatasource() != RtdbDatasource.API_REST) {
				count = basicOpsRepository.count(ontology.getIdentification());
			}

			final Boolean hasDocuments = count > 0 ? true : false;

			return new ResponseEntity<>(hasDocuments, HttpStatus.OK);

		} catch (final Exception e) {
			return new ResponseEntity<>("Error processing the request: " + e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = "/getInstance/{datasource}/{collection}")
	public @ResponseBody String getInstance(@PathVariable("datasource") String datasource,
			@PathVariable("collection") String collection) {

		return ontologyBusinessService.getInstance(datasource, collection);
	}

	@GetMapping(value = "/getRelationalSchema/{datasource}/{collection}")
	public @ResponseBody ResponseEntity<?> getRelationalSchema(@PathVariable("datasource") String datasource,
			@PathVariable("collection") String collection) {
		try {
			final String metaData = ontologyBusinessService.getRelationalSchema(datasource, collection);
			if (metaData.isEmpty())
				return new ResponseEntity<>("Collection or datasource not found", HttpStatus.NOT_FOUND);
			else
				return new ResponseEntity<>(metaData, HttpStatus.OK);
		} catch (final Exception e) {
			return new ResponseEntity<>("Error processing the request", HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = "/schema/{identification}", produces = "application/json")
	public ResponseEntity<?> getSchema(@PathVariable("identification") String identification) {

		final Ontology ontology = ontologyConfigService.getOntologyByIdentification(identification, utils.getUserId());
		if (ontology != null) {
			try {
				mapper.enable(SerializationFeature.INDENT_OUTPUT);
				final JsonNode schema = mapper.readTree(ontology.getJsonSchema());
				return new ResponseEntity<>(mapper.writeValueAsString(schema), HttpStatus.OK);
			} catch (final IOException e) {
				return new ResponseEntity<>("Ontology schema is not valid Json", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			return new ResponseEntity<>("No existing ontology with id " + identification, HttpStatus.NOT_FOUND);
		}
	}

	@GetMapping("/{id}/properties/type/{type}")
	public ResponseEntity<Map<String, String>> getPropertiesWithPath(@PathVariable("id") String identification,
			@PathVariable("type") String type) {

		final Map<String, String> properties = ontologyDataService.getOntologyPropertiesWithPath4Type(identification,
				type);
		return new ResponseEntity<>(properties, HttpStatus.OK);

	}

	@GetMapping("/swaggerApi")
	public ResponseEntity<String> getSwaggerAPIDefinition(@RequestParam("url") String swaggerUrl) {

		String jsonDefinition = "";
		try {
			jsonDefinition = swaggerApiImporterService.getApiDefinition(swaggerUrl);
		} catch (final Exception e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(jsonDefinition, HttpStatus.OK);
	}

	@PostMapping("queryKPI")
	public String runQuery(Model model, @RequestParam String queryType, @RequestParam String query,
			@RequestParam String ontologyIdentification) throws JsonProcessingException {
		String queryResult = null;

		final Ontology ontology = ontologyConfigService.getOntologyByIdentification(ontologyIdentification,
				utils.getUserId());

		// validate if queryu has parameters at this case show a message
		final String patternString = "\\{.*\\$\\w+\\.*}";
		final Pattern pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
		final Matcher matcher = pattern.matcher(query);
		if (matcher.find()) {

			model.addAttribute(QUERY_RESULT, utils.getMessage("ontology.kpi.executed.has.parameters",
					"The KPI has parameters, please to test the query enter a valid value instead of the parameter, then replace it with the parameter to save."));
			return QUERY_TOOL_SHOW_QUERY;
		}
		try {
			if (ontologyConfigService.hasUserPermissionForQuery(utils.getUserId(), ontologyIdentification)) {
				final ManageDBRepository manageDB = manageFactory.getInstance(ontologyIdentification);
				if (manageDB.getListOfTables4Ontology(ontologyIdentification).isEmpty()) {
					manageDB.createTable4Ontology(ontologyIdentification, "{}", null);
				}
				if (queryType.equalsIgnoreCase(OntologyService.QUERY_SQL)
						&& !ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)) {
					// get ontology from query for the case when use query templates
					final PlatformQuery newQuery = queryTemplateService.getTranslatedQuery(ontology.getIdentification(),
							query);
					String ontoIden = null;
					if (newQuery == null) {
						ontoIden = ontologyConfigService.getOntologyFromQuery(query);
					} else {
						ontoIden = ontologyConfigService.getOntologyFromQuery(newQuery.getQuery());
					}

					queryResult = queryToolService.querySQLAsJson(utils.getUserId(), ontoIden, query, 0);
					model.addAttribute(QUERY_RESULT, queryResult);
					return QUERY_TOOL_SHOW_QUERY;

				} else if (queryType.equalsIgnoreCase(OntologyService.QUERY_NATIVE)
						|| ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)) {
					// get ontology from query for the case when use query templates
					final PlatformQuery newQuery = queryTemplateService.getTranslatedQuery(ontology.getIdentification(),
							query);

					String ontoIden = null;
					if (newQuery == null) {
						ontoIden = ontologyConfigService.getOntologyFromQuery(query);
					} else {
						ontoIden = ontologyConfigService.getOntologyFromQuery(newQuery.getQuery());
					}

					queryResult = queryToolService.queryNativeAsJson(utils.getUserId(), ontoIden, query);
					model.addAttribute(QUERY_RESULT, queryResult);
					return QUERY_TOOL_SHOW_QUERY;
				} else {
					model.addAttribute(QUERY_RESULT, utils.getMessage("querytool.querytype.notselected",
							"Please select queryType Native or SQL"));
					return QUERY_TOOL_SHOW_QUERY;

				}
			} else {

				model.addAttribute(QUERY_RESULT, utils.getMessage("querytool.ontology.access.denied.json",
						"You don't have permissions for this ontology"));
				return QUERY_TOOL_SHOW_QUERY;
			}
		} catch (final DBPersistenceException e) {
			log.error(ERROR_IN_RUNQUERY, e);
			model.addAttribute(QUERY_RESULT, e.getMessage());
			return QUERY_TOOL_SHOW_QUERY;
		} catch (final Exception e) {
			log.error(ERROR_IN_RUNQUERY, e);
			model.addAttribute(QUERY_RESULT, utils.getMessage("querytool.query.native.error", "Error malformed query"));
			return QUERY_TOOL_SHOW_QUERY;
		}

	}

	@PostMapping("queryKPIOne")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public @ResponseBody String runQueryOne(Model model, @RequestParam String queryType, @RequestParam String query,
			@RequestParam String ontologyIdentification) throws JsonProcessingException {
		String queryResult = null;
		query = "select onequeryontology from ( " + query + " ) as onequeryontology limit 1";
		final Ontology ontology = ontologyConfigService.getOntologyByIdentification(ontologyIdentification,
				utils.getUserId());

		try {
			if (ontologyConfigService.hasUserPermissionForQuery(utils.getUserId(), ontologyIdentification)) {
				final ManageDBRepository manageDB = manageFactory.getInstance(ontologyIdentification);
				if (manageDB.getListOfTables4Ontology(ontologyIdentification).isEmpty()) {
					manageDB.createTable4Ontology(ontologyIdentification, "{}", null);
				}
				if (queryType.equalsIgnoreCase(OntologyService.QUERY_SQL)
						&& !ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)) {
					queryResult = queryToolService.querySQLAsJson(utils.getUserId(), ontologyIdentification, query, 0);

					return queryResult;

				} else if (queryType.equalsIgnoreCase(OntologyService.QUERY_NATIVE)
						|| ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)) {
					queryResult = queryToolService.queryNativeAsJson(utils.getUserId(), ontologyIdentification, query);
					return queryResult;
				} else {
					return utils.getMessage("querytool.querytype.notselected", "Please select queryType Native or SQL");
				}
			} else {
				return utils.getMessage("querytool.ontology.access.denied.json",
						"You don't have permissions for this ontology");
			}
		} catch (final DBPersistenceException e) {
			log.error(ERROR_IN_RUNQUERY, e);
			model.addAttribute(QUERY_RESULT, e.getMessage());
			return QUERY_TOOL_SHOW_QUERY;
		} catch (final Exception e) {
			log.error(ERROR_IN_RUNQUERY, e);
			model.addAttribute(QUERY_RESULT, utils.getMessage("querytool.query.native.error", "Error malformed query"));
			return QUERY_TOOL_SHOW_QUERY;
		}

	}

	private ResponseEntity<Map<String, String>> createKPIinDB(OntologyKPIDTO ontologyKPIDTO,
			Map<String, String> response, Ontology ontology, String userID) {

		final OntologyKPI oKPI = new OntologyKPI();
		oKPI.setCron(ontologyKPIDTO.getCron());
		oKPI.setDateFrom(ontologyKPIDTO.getDateFrom());
		oKPI.setDateTo(ontologyKPIDTO.getDateTo());
		if (ontologyKPIDTO.getDateTo() != null && ontologyKPIDTO.getDateFrom() == null) {
			final Date now = new Date();
			if (ontologyKPIDTO.getDateTo().before(now)) {
				final Calendar dateFrom = Calendar.getInstance();
				dateFrom.setTime(ontologyKPIDTO.getDateTo());
				dateFrom.add(Calendar.HOUR, -1);
				oKPI.setDateFrom(dateFrom.getTime());
			} else {
				oKPI.setDateFrom(now);
			}
		}
		if (ontologyKPIDTO.getDateTo() != null && ontologyKPIDTO.getDateFrom() != null
				&& ontologyKPIDTO.getDateTo().before(ontologyKPIDTO.getDateFrom())) {
			oKPI.setDateFrom(null);
			oKPI.setDateTo(null);
		}
		oKPI.setActive(Boolean.FALSE);
		oKPI.setOntology(ontology);
		oKPI.setQuery(ontologyKPIDTO.getQuery());
		oKPI.setUser(userService.getUser(userID));
		oKPI.setPostProcess(ontologyKPIDTO.getPostProcess());
		ontologyKPIRepository.save(oKPI);
		ontologyKPIService.scheduleKpi(oKPI);
		response.put(REDIRECT_STR, "/controlpanel/ontologies/list");
		response.put(STATUS_STR, "ok");
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@PostMapping("startstop")
	public String startStop(Model model, @RequestParam String id) {
		final Ontology ontology = ontologyConfigService.getOntologyById(id, utils.getUserId());

		if (ontology.getOntologyKPI() != null) {
			if (ontology.getOntologyKPI().isActive()) {
				ontologyKPIService.unscheduleKpi(ontology.getOntologyKPI());
			} else {
				ontologyKPIService.scheduleKpi(ontology.getOntologyKPI());
			}
		}

		return REDIRECT_ONTOLOGIES_LIST;
	}

	@PostMapping("executeKPI")
	public ResponseEntity<Map<String, String>> executeKPI(Model model, @RequestParam String id) {
		final Ontology ontology = ontologyConfigService.getOntologyById(id, utils.getUserId());
		final Map<String, String> response = new HashMap<>();
		try {

			if (ontology.getOntologyKPI() != null) {
				// validate if queryu has parameters at this case show a message
				final String patternString = "\\{.*\\$\\w+\\.*}";
				final Pattern pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
				final Matcher matcher = pattern.matcher(ontology.getOntologyKPI().getQuery());
				if (matcher.find()) {
					response.put(STATUS_STR, ERROR_STR);
					response.put(CAUSE_STR, utils.getMessage("ontology.kpi.executed.has.parameters",
							"The KPI has parameters, please to test the query enter a valid value instead of the parameter, then replace it with the parameter to save."));
					return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
				}

				final Map<String, String> result = ontologyConfigService.executeKPI(utils.getUserId(),
						ontology.getOntologyKPI().getQuery(), ontology.getIdentification(),
						ontology.getOntologyKPI().getPostProcess());
				if (result != null && result.get(STATUS_STR).equals(ERROR_STR)) {
					response.put(STATUS_STR, ERROR_STR);
					response.put(CAUSE_STR, utils.getMessage(result.get(MESSAGE_STR), ""));
					return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
				} else {
					response.put(STATUS_STR, "ok");
					return new ResponseEntity<>(response, HttpStatus.CREATED);
				}

			} else {
				response.put(STATUS_STR, ERROR_STR);
				response.put(CAUSE_STR, "");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
		} catch (final Exception e) {
			response.put(STATUS_STR, ERROR_STR);
			response.put(CAUSE_STR, e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@GetMapping("/virtual/schema/sql")
	public ResponseEntity<String> getSqlTableDefinitionFromSchema(
			@ApiParam(value = "Ontology name") @RequestParam(required = true) String ontology,
			@ApiParam(value = "Ontology json schema") @RequestParam(required = true) String schema,
			@ApiParam(value = "Datasource type") @RequestParam(required = true) VirtualDatasourceType datasource) {
		ResponseEntity<String> response = null;
		try {
			final String definition = ontologyBusinessService.getSqlTableDefinitionFromSchema(ontology, schema,
					datasource);
			response = new ResponseEntity<>(definition, HttpStatus.OK);

		} catch (final Exception e) {

			response = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		return response;
	}

	@PostMapping("/virtual/sql/converter/create/{identification}")
	public ResponseEntity<String> getSQLCreateTable(
			@ApiParam(value = "Ontology identification") @PathVariable("identification") String identification,
			@ApiParam(value = "Ontology json schema") @Valid @RequestBody(required = true) CreateStatementDTO statementDTO,
			@ApiParam(value = "Datasource type") @RequestParam(required = true) VirtualDatasourceType datasource) {
		ResponseEntity<String> response = null;
		try {
			final CreateStatement statement = statementDTO.toCreateStatement();
			statement.setOntology(identification);
			final String definition = ontologyBusinessService.getSQLCreateTable(statement, datasource);
			final JsonObject responseBody = new JsonObject();
			responseBody.addProperty("statement", definition);
			response = new ResponseEntity<>(responseBody.toString(), HttpStatus.OK);

		} catch (final Exception e) {

			response = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		return response;
	}

	@GetMapping("/virtual/datasource/{identification}")
	public ResponseEntity<?> getDataSourceByIdentification(
			@ApiParam(value = "Ontology datasource identification") @PathVariable(required = true) String identification) {
		ResponseEntity<?> response = null;
		try {
			final User user = userService.getUser(utils.getUserId());
			OntologyVirtualDatasource datasource = null;
			datasource = ontologyConfigService.getOntologyVirtualDatasourceByName(identification);

			if (datasource == null) {
				response = new ResponseEntity<>(HttpStatus.NO_CONTENT);
			} else {
				if (userService.isUserAdministrator(user) || datasource.isPublic()
						|| datasource.getUser().contentEquals(user.getUserId())) {

					final OntologyVirtualDataSourceDTO datasourceDTO = new OntologyVirtualDataSourceDTO(datasource);
					response = new ResponseEntity<>(datasourceDTO, HttpStatus.OK);
				}
			}

		} catch (final Exception e) {

			response = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		return response;
	}

	private List<String> getPropertyNames(Set<OntologyTimeSeriesProperty> props) {
		List<String> propertyNames = new LinkedList<String>();
		for (OntologyTimeSeriesProperty otsp : props) {
			propertyNames.add(otsp.getPropertyName());
		}
		return propertyNames;
	}

	@GetMapping(value = "/freeResource/{id}")
	public @ResponseBody void freeResource(@PathVariable("id") String id) {
		resourcesInUseService.removeByUser(id, utils.getUserId());
		log.info("free resource", id);
	}

	@GetMapping(value = "/getResourcesAssociated/{id}")
	public @ResponseBody Map<String, List<String>> getResourcesAssociated(@PathVariable("id") String id) {

		return ontologyConfigService.getResourcesFromOntology(ontologyRepository.findById(id).get());
	}

}
