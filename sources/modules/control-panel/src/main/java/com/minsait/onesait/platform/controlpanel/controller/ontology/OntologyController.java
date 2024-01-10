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
package com.minsait.onesait.platform.controlpanel.controller.ontology;

import static com.minsait.onesait.platform.business.services.ontology.OntologyServiceStatusBean.MODULE_NOT_ACTIVE_KEY;
import static tech.tablesaw.aggregate.AggregateFunctions.countNonMissing;
import static tech.tablesaw.aggregate.AggregateFunctions.max;
import static tech.tablesaw.aggregate.AggregateFunctions.mean;
import static tech.tablesaw.aggregate.AggregateFunctions.min;
import static tech.tablesaw.aggregate.AggregateFunctions.stdDev;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.hibernate.exception.ConstraintViolationException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.JsonObject;
import com.minsait.onesait.platform.business.services.ontology.CreateStatementBusiness;
import com.minsait.onesait.platform.business.services.ontology.OntologyBusinessService;
import com.minsait.onesait.platform.business.services.ontology.OntologyBusinessServiceException;
import com.minsait.onesait.platform.business.services.ontology.OntologyBusinessServiceException.Error;
import com.minsait.onesait.platform.business.services.ontology.OntologyServiceStatusBean;
import com.minsait.onesait.platform.business.services.ontology.graph.NebulaGraphBusinessService;
import com.minsait.onesait.platform.business.services.ontology.graph.NebulaGraphEntity;
import com.minsait.onesait.platform.business.services.ontology.graph.NebulaGraphUpdateEntity;
import com.minsait.onesait.platform.business.services.ontology.timeseries.TimeSerieOntologyBusinessServiceException;
import com.minsait.onesait.platform.business.services.ontology.timeseries.TimeSeriesOntologyBusinessService;
import com.minsait.onesait.platform.business.services.presto.datasource.PrestoDatasourceConfigurationService;
import com.minsait.onesait.platform.business.services.presto.datasource.PrestoDatasourceService;
import com.minsait.onesait.platform.business.services.swagger.SwaggerApiImporterService;
import com.minsait.onesait.platform.business.services.virtual.datasources.VirtualDatasourceService;
import com.minsait.onesait.platform.commons.exception.GenericOPException;

import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.App;
import com.minsait.onesait.platform.config.model.ClientPlatformOntology;
import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.model.Configuration.Type;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.OntologyDataAccess;
import com.minsait.onesait.platform.config.model.OntologyElastic;
import com.minsait.onesait.platform.config.model.OntologyKPI;
import com.minsait.onesait.platform.config.model.OntologyPresto;
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
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.model.security.UserPrincipal;
import com.minsait.onesait.platform.config.repository.ApiRepository;
import com.minsait.onesait.platform.config.repository.ClientPlatformOntologyRepository;
import com.minsait.onesait.platform.config.repository.ConfigurationRepository;
import com.minsait.onesait.platform.config.repository.OntologyKPIRepository;
import com.minsait.onesait.platform.config.repository.OntologyMqttTopicRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.services.app.AppService;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.services.datamodel.DataModelService;
import com.minsait.onesait.platform.config.services.exceptions.OntologyServiceException;
import com.minsait.onesait.platform.config.services.objectstorage.MinioObjectStorageService;
import com.minsait.onesait.platform.config.services.ontology.OntologyConfiguration;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.ontology.OntologyTimeSeriesService;
import com.minsait.onesait.platform.config.services.ontology.dto.GenerateTimescaleContinuousAggregateResponse;
import com.minsait.onesait.platform.config.services.ontology.dto.GenerateTimescaleContinuousAggregateResponse.ErrorType;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyDTO;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyKPIDTO;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyListIndexMongoConfDTO;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyPropertiesIndexConfDTO;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyTimeSeriesServiceDTO;
import com.minsait.onesait.platform.config.services.ontology.dto.TimescaleContinuousAggregateRequest;
import com.minsait.onesait.platform.config.services.ontology.dto.VirtualDatasourceDTO;
import com.minsait.onesait.platform.config.services.ontology.dto.VirtualDatasourceInfoDTO;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataJsonProblemException;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataService;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataUnauthorizedException;
import com.minsait.onesait.platform.config.services.templates.PlatformQuery;
import com.minsait.onesait.platform.config.services.templates.QueryTemplateService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.controller.ontology.model.sql.CreateStatementDTO;
import com.minsait.onesait.platform.controlpanel.controller.ontology.model.sql.HistoricalOptionsDTO.FileFormat;
import com.minsait.onesait.platform.controlpanel.rest.management.ontology.model.OntologyVirtualDataSourceDTO;
import com.minsait.onesait.platform.controlpanel.services.resourcesinuse.ResourcesInUseService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.external.virtual.VirtualRelationalOntologyManageDBRepository;
import com.minsait.onesait.platform.persistence.factory.ManageDBRepositoryFactory;
import com.minsait.onesait.platform.persistence.interfaces.ManageDBRepository;
import com.minsait.onesait.platform.persistence.mongodb.MongoNativeManageDBRepository;
import com.minsait.onesait.platform.persistence.nebula.service.NebulaGraphService;
import com.minsait.onesait.platform.persistence.services.BasicOpsPersistenceServiceFacade;
import com.minsait.onesait.platform.persistence.services.QueryToolService;
import com.minsait.onesait.platform.persistence.timescaledb.util.TimescaleDBPersistantException;
import com.minsait.onesait.platform.quartz.services.ontologyKPI.OntologyKPIService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import tech.tablesaw.aggregate.AggregateFunctions;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.NumericColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;
import tech.tablesaw.io.Source;
import tech.tablesaw.io.json.JsonReader;
import tech.tablesaw.plotly.api.Histogram;
import tech.tablesaw.plotly.traces.Trace;

@Controller
@RequestMapping("/ontologies")
@Slf4j
public class OntologyController {

	@Autowired
	private VirtualDatasourceService virtualDatasourceService;

	@Autowired
	private OntologyService ontologyConfigService;
	@Autowired
	private OntologyRepository ontologyRepository;

	@Autowired
	private ApiRepository apiRepository;
	@Autowired
	private ClientPlatformOntologyRepository clientPlatformOntologyRepository;

	@Autowired
	private ConfigurationRepository configurationRepository;
	@Autowired
	ConfigurationService configurationService;

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
	private AppService appService;

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
	private TimeSeriesOntologyBusinessService timeSeriesBusinessService;
	@Autowired
	private DataModelService dataModelService;
	@Autowired
	private QueryTemplateService queryTemplateService;
	@Autowired
	private ResourcesInUseService resourcesInUseService;

	@Autowired
	private IntegrationResourcesService resourcesService;

	@Autowired
	private NebulaGraphBusinessService nebulaGraphService;

	@Autowired(required = false)
	private MinioObjectStorageService minioObjectStoreService;

	@Autowired
	private HttpSession httpSession;

	@Autowired
	private OntologyServiceStatusBean serviceStatusBean;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private MongoNativeManageDBRepository MongoNativeDBRepository;

	@Autowired
	private PrestoDatasourceService prestoDatasourceService;

	@Autowired
	private PrestoDatasourceConfigurationService prestoDatasourceConfigurationService;

	@Autowired
	private VirtualRelationalOntologyManageDBRepository virtualRelationalOntologyManageDBRepository;

	@Autowired
	OntologyMqttTopicRepository ontologyMqttTopicRepo;

	private static final String ONTOLOGIES_STR = "ontologies";
	private static final String ONTOLOGY_STR = "ontology";
	private static final String ONTOLOGY_REST_STR = "ontologyRest";
	private static final String ONTOLOGY_ELASTIC_STR = "ontologyElastic";
	private static final String ONTOLOGIES_CREATE = "ontologies/create";
	private static final String ONTOLOGIES_CREATE_INDEX = "ontologies/createindex";
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
	private static final String ONTOLOGIES_BULK_CREATE = "ontologies/bulkcreation";
	private static final String REDIRECT_ONTOLOGIES_BULK_CREATE = "redirect:/ontologies/bulkcreation";
	private static final String DATA_MODELS_STR = "dataModels";
	private static final String DATA_MODEL_TYPES_STR = "dataModelTypes";
	private static final String RTDBS = "rtdbs";
	private static final String QUERY_RESULT = "queryResult";
	private static final String QUERY_TOOL_SHOW_QUERY = "querytool/show :: query";
	private static final String ERROR_IN_RUNQUERY = "Error in runQuery";
	private static final String TIMESERIES_DATAMODEL = "MASTER-DataModel-30";
	private static final String ONTOLOGYTSDTO = "ontologyTSDTO";
	private static final String ONTOLOGYPROPINDEXCONFSDTO = "OntologyPropertiesIndexConfDTO";
	private static final String AUTHORIZATIONS = "authorizations";
	private static final String USERS = "users";
	private static final String REALMS = "realms";
	private static final String PROPDATACLASSES = "propDclasses";
	private static final String ENTITYDATACLASSES = "entityDclasses";
	private static final String PROPERTY_NAMES = "propertyNames";
	private static final String DATAACCESSES = "dataaccesses";
	private static final String HISTORICAL = "historical";
	private static final String HISTORICAL_CATALOG = "historicalCatalog";
	private static final String HISTORICAL_SCHEMA = "historicalSchema";
	private static final String ONTOLOGIES_CREATEVIRTUAL = "ontologies/createvirtual";
	private static final String FILE_FORMATS = "fileFormats";
	private static final String IS_ONTOLOGY_REST = "isOntologyRest";
	private static final String RTDB_DATASOURCE_TYPE = "rtdbDatasourceType";
	private static final String USER_BUCKET_ONTOLOGY_PATH = "userBucketOntologiesPath";
	private static final String MODEL_JSON_LD_URL = "modeljsonldurl";
	private static final String USER_ONTOLOGY_ACCESS = "userOntologyAccess";
	private static final String APP_ID = "appId";
	private static final String REDIRECT_PROJECT_SHOW = "/controlpanel/projects/update/";
	private static final String ONTOLOGIES_CREATEPRESTO = "ontologies/createpresto";
	private static final String MQTT_TOPIC_NAME = "mqttTopicPath";
	private static final String TIMESERIESDB = "timeseriesdb";

	@Value("${onesaitplatform.database.timescaledb.connectionName:op_timeseriesdb}")
	private String timeseriesdbConnection;

	private final ObjectMapper mapper = new ObjectMapper();

	@GetMapping(value = "/list", produces = "text/html")
	public String listAll(Model model, HttpServletRequest request,
			@RequestParam(required = false, name = "identification") String identification,
			@RequestParam(required = false, name = "description") String description,
			@RequestParam(required = false, name = "showOwned") Boolean showOwned,
			@RequestParam(required = false, name = "showAudit") Boolean showAudit,
			@RequestParam(required = false, name = "showLog") Boolean showLog) {

		// Scaping "" string values for parameters
		if (identification != null && identification.equals("")) {
			identification = null;
		}
		if (description != null && description.equals("")) {
			description = null;
		}
		if (showOwned == null) {
			showOwned = true;
		}
		if (showAudit == null) {
			showAudit = false;
		}
		if (showLog == null) {
			showLog = false;
		}

		final List<OntologyDTO> ontologies = ontologyConfigService.getOntologiesForList(utils.getUserId(),
				identification, description, showOwned, showAudit, showLog);

		model.addAttribute(ONTOLOGIES_STR, ontologies);
		model.addAttribute("showOwned", showOwned);
		model.addAttribute("showAudit", showAudit);
		model.addAttribute("showLog", showLog);
		model.addAttribute("nebulaURL", resourcesService.getUrl(Module.NEBULA_GRAPH, ServiceUrl.BASE));
		return ONTOLOGIES_LIST;
	}

	@PostMapping("/getNamesForAutocomplete")
	public @ResponseBody List<String> getNamesForAutocomplete() {
		// CLEANING APP_ID FROM SESSION
		httpSession.removeAttribute(APP_ID);

		return ontologyConfigService.getAllIdentificationsByUser(utils.getUserId());
	}

	@GetMapping(value = "/createindex/{id}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public String createindex(Model model, @PathVariable("id") String id) {

		final Ontology ontology = ontologyConfigService.getOntologyById(id, utils.getUserId());

		List<String> listIndexTrue = new ArrayList<>();
		Map<String, List<String>> virtualIndexConfigMap = new HashMap<>();
		List<OntologyPropertiesIndexConfDTO> listProperties = new ArrayList<OntologyPropertiesIndexConfDTO>();

		if (ontology.getRtdbDatasource().name() == "VIRTUAL") {
			final OntologyVirtual ontologyVirtual = ontologyConfigService.getOntologyVirtualByOntologyId(ontology);
			virtualIndexConfigMap = virtualRelationalOntologyManageDBRepository
					.getListIndexes(ontologyVirtual.getDatasourceTableName(), ontology.getIdentification());
			listProperties = ontologyConfigService.getPropertiesOntologyVirtual(ontology, virtualIndexConfigMap);

		}

		model.addAttribute(ONTOLOGYPROPINDEXCONFSDTO, listProperties);
		model.addAttribute(ONTOLOGYTSDTO, ontology);
		model.addAttribute("listIndexTrue", listIndexTrue);

		return ONTOLOGIES_CREATE_INDEX;
	}

	@PostMapping(value = "/createindexdatabase")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public @ResponseBody ResponseEntity<String> createindexdatabase(Model model, HttpServletRequest request,
			@RequestBody String dataIndex) {

		JSONObject data = new JSONObject(dataIndex);
		final Ontology ontology = ontologyConfigService.getOntologyById(data.getString("id"), utils.getUserId());
		ResponseEntity<String> response = null;
		if (ontology != null) {

			String ontologyName = data.getString("ontology");
			String typeIndex = data.getString("typeIndex");
			String indexName = data.getString("indexName");
			boolean unique = data.getBoolean("unique");
			boolean background = data.getBoolean("background");
			boolean sparse = data.getBoolean("sparse");
			boolean ttl = data.getBoolean("ttl");
			String timesecondsTTL = data.get("timesecondsTTL").toString();
			JSONArray checkboxValuesArray = data.getJSONArray("checkboxValues");

			if (ontology.getRtdbDatasource().name() == "MONGO") {

				try {
					MongoNativeDBRepository.createIndexWithParameter(ontologyName, typeIndex, indexName, unique,
							background, sparse, ttl, timesecondsTTL, checkboxValuesArray);
					response = new ResponseEntity<>("Create Index successfully", HttpStatus.OK);
				} catch (DBPersistenceException e) {
					if (e.getMessage().contains("already exists with a different")) {
						response = new ResponseEntity<>("messageErrorIndexAlreadyExist", HttpStatus.BAD_REQUEST);
					} else if (e.getMessage().contains("E11000 duplicate key error collection")) {
						response = new ResponseEntity<>("messageErrorViolatesIndexUnique", HttpStatus.BAD_REQUEST);
					} else if (e.getMessage().contains("An existing index has the same name as the requested index")) {
						response = new ResponseEntity<>("messageErrorIndexName", HttpStatus.BAD_REQUEST);
					} else if (e.getMessage().contains("An equivalent index already exists ")) {
						response = new ResponseEntity<>("messageErrorIndexExistButDifferentOptions",
								HttpStatus.BAD_REQUEST);
					} else {
						response = new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
					}
				}
			}
			// if(ontology.getRtdbDatasource().name() == "VIRTUAL"){
			// if(state == true) {
			// final OntologyVirtual ontologyVirtual =
			// ontologyConfigService.getOntologyVirtualByOntologyId(ontology);
			// virtualRelationalOntologyManageDBRepository.createIndex(ontologyVirtual.getDatasourceTableName(),
			// ontology.getIdentification(),name);
			// response = new ResponseEntity<>("Create Index successfully",HttpStatus.OK);
			// }
			// }

			return response;
		} else {
			return new ResponseEntity<>("", HttpStatus.BAD_REQUEST);
		}

	}

	@PostMapping(value = "/dropindexdatabase")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public @ResponseBody ResponseEntity<String> dropindexdatabase(Model model, HttpServletRequest request,
			@RequestBody String dataIndex) {

		JSONObject data = new JSONObject(dataIndex);
		final Ontology ontology = ontologyConfigService.getOntologyById(data.getString("id"), utils.getUserId());
		if (ontology != null) {
			ResponseEntity<String> response = null;
			String ontologyName = data.getString("ontology");
			String indexName = data.getString("indexName");
			String property = data.getString("property");

			if (property.equals("_id")) {
				response = new ResponseEntity<>("meessageCannotdelete_id", HttpStatus.BAD_REQUEST);
			} else {
				if (ontology.getRtdbDatasource().name() == "MONGO") {
					MongoNativeDBRepository.dropIndex(ontologyName, indexName);
					response = new ResponseEntity<>("Delete Index successfully", HttpStatus.OK);
				}
			}

			// if(ontology.getRtdbDatasource().name() == "VIRTUAL"){

			// final OntologyVirtual ontologyVirtual =
			// ontologyConfigService.getOntologyVirtualByOntologyId(ontology);
			// virtualRelationalOntologyManageDBRepository.dropIndex(ontology.getIdentification(),ontologyVirtual.getDatasourceTableName(),
			// name);
			// response = new ResponseEntity<>("Delete Index successfully",HttpStatus.OK);

			// }
			return response;
		} else {
			return new ResponseEntity<>("", HttpStatus.BAD_REQUEST);
		}

	}

	@GetMapping(value = "/create")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public String create(Model model) {
		if (model.asMap().containsKey(MODULE_NOT_ACTIVE_KEY)) {
			model.addAttribute("message", model.asMap().get(MODULE_NOT_ACTIVE_KEY));
		}
		model.addAttribute(ONTOLOGY_STR, new Ontology());
		model.addAttribute(ONTOLOGY_REST_STR, new OntologyRestDTO());

		final Object projectId = httpSession.getAttribute(APP_ID);
		if (projectId != null) {
			model.addAttribute(APP_ID, projectId.toString());
		}

		populateForm(model);
		return ONTOLOGIES_CREATE;
	}

	@GetMapping(value = "/createwizard", produces = "text/html")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public String createWizard(Model model) {
		Ontology ontology = (Ontology) model.asMap().get(ONTOLOGY_STR);
		model.addAttribute(ONTOLOGY_ELASTIC_STR, getDefaultElasticValues());

		if (ontology == null) {
			ontology = new Ontology();
			ontology.setPublic(false);
			model.addAttribute(ONTOLOGY_STR, ontology);
			model.addAttribute(MODEL_JSON_LD_URL, resourcesService.getUrl(Module.MODELJSONLD, ServiceUrl.BASE));
			model.addAttribute(MQTT_TOPIC_NAME, getMqttTopicPath());
		} else {
			ontology.setId(null);
			ontology.setPublic(false);
			if (ontology.isAllowsCreateMqttTopic()) {
				model.addAttribute(MQTT_TOPIC_NAME, ontologyMqttTopicRepo.findByOntology(ontology).getIdentification());
			}
			model.addAttribute(ONTOLOGY_STR, ontology);
			model.addAttribute(MODEL_JSON_LD_URL, resourcesService.getUrl(Module.MODELJSONLD, ServiceUrl.BASE));
		}

		final Object projectId = httpSession.getAttribute(APP_ID);
		if (projectId != null) {
			model.addAttribute(APP_ID, projectId.toString());
		}

		populateForm(model);
		return "ontologies/createwizard";
	}

	private String getMqttTopicPath() {
		UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		String vertical = null;
		String tenant = null;

		if (!userPrincipal.getVertical().equals(Tenant2SchemaMapper.DEFAULT_VERTICAL_NAME) || !userPrincipal.getTenant()
				.equals(Tenant2SchemaMapper.defaultTenantName(Tenant2SchemaMapper.DEFAULT_VERTICAL_NAME))) {
			vertical = userPrincipal.getVertical();
			tenant = userPrincipal.getTenant();
			return "/" + vertical + "/" + tenant + "/";
		}
		return "/";

	}

	@GetMapping(value = "/createapirest", produces = "text/html")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public String createAPIREST(Model model) {

		model.addAttribute(ONTOLOGY_STR, new Ontology());

		final Object projectId = httpSession.getAttribute(APP_ID);
		if (projectId != null) {
			model.addAttribute(APP_ID, projectId.toString());
		}

		populateFormApiRest(model);
		return "ontologies/createapirest";
	}

	@GetMapping(value = "/createvirtual", produces = "text/html")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public String createVirtual(Model model) {
		model.addAttribute(ONTOLOGY_STR, new Ontology());
		model.addAttribute(HISTORICAL, false);
		model.addAttribute(USER_BUCKET_ONTOLOGY_PATH, minioObjectStoreService.getUserBucketName(utils.getUserId()) + "/"
				+ minioObjectStoreService.ONTOLOGIES_DIR);
		populateFormVirtual(model);

		final Object projectId = httpSession.getAttribute(APP_ID);
		if (projectId != null) {
			model.addAttribute(APP_ID, projectId.toString());
		}
		return ONTOLOGIES_CREATEVIRTUAL;
	}

	@GetMapping(value = "/create-graph", produces = "text/html")
	public String createGraph(Model model, RedirectAttributes ra) {
		if (!serviceStatusBean.isNebulaGraphActive()) {
			ra.addFlashAttribute(MODULE_NOT_ACTIVE_KEY,
					messageSource.getMessage("service.nebula.down", null, LocaleContextHolder.getLocale()));
			return "redirect:/ontologies/create";
		}
		model.addAttribute(ONTOLOGY_STR, new NebulaGraphEntity());
		model.addAttribute("fieldTypes", NebulaGraphService.NEBULA_TYPES);
		return "ontologies/creategraph";
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

		final Object projectId = httpSession.getAttribute(APP_ID);
		if (projectId != null) {
			model.addAttribute(APP_ID, projectId.toString());
		}

		populateKPIForm(model);
		return "ontologies/createkpi";
	}

	@GetMapping(value = "/createtimeseries", produces = "text/html")
	public String createTimeSeries(Model model) {

		model.addAttribute(ONTOLOGYTSDTO, new OntologyTimeSeriesServiceDTO());

		final Object projectId = httpSession.getAttribute(APP_ID);
		if (projectId != null) {
			model.addAttribute(APP_ID, projectId.toString());
		}

		populateFormTimeseries(model);
		return ONTOLOGIES_CREATE_TS;
	}

	@PostMapping(value = "/clone")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public ResponseEntity<Map<String, String>> cloneOntology(Model model, @RequestParam String id,
			@RequestParam String identification, HttpServletRequest request) {

		final Map<String, String> response = new HashMap<>();
		final OntologyConfiguration config = new OntologyConfiguration(request);

		try {
			ontologyBusinessService.cloneOntology(id, identification, utils.getUserId(), config);
		} catch (final OntologyServiceException | OntologyBusinessServiceException e) {
			log.error("Error clonning ontology", e);
			response.put(STATUS_STR, ERROR_STR);
			response.put(CAUSE_STR, e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Transactional
	@PostMapping(value = "/cloneKpi")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public ResponseEntity<Map<String, String>> cloneOntologyKpi(Model model, @RequestParam String id,
			@RequestParam String identification, HttpServletRequest request) {

		final Map<String, String> response = new HashMap<>();
		final OntologyConfiguration config = new OntologyConfiguration(request);

		try {
			ontologyBusinessService.cloneOntology(id, identification, utils.getUserId(), config);
			final Ontology cloneOntology = ontologyConfigService.getOntologyByIdentification(identification);
			final Ontology ontology = ontologyConfigService.getOntologyById(id, utils.getUserId());
			ontologyKPIService.cloneOntologyKpi(ontology, cloneOntology, userService.getUser(utils.getUserId()));
		} catch (final OntologyServiceException | OntologyBusinessServiceException e) {
			log.error("Error clonning ontology", e);
			response.put(STATUS_STR, ERROR_STR);
			response.put(CAUSE_STR, e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping(value = { "/create", "/createwizard", "/createapirest", "/createvirtual", "/createhistorical",
			"/createpresto" })
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

			if (ontology.getRtdbDatasource().equals(RtdbDatasource.PRESTO) && (prestoDatasourceConfigurationService
					.isHistoricalCatalog(config.getDatasourceCatalog())
					|| prestoDatasourceConfigurationService.isRealtimedbCatalog(config.getDatasourceCatalog()))) {
				ontologyConfigService.getOntologyById(config.getDatasourceTableName(), utils.getUserId());
			}

			ontologyBusinessService.createOntology(ontology, utils.getUserId(), config);

			if (ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)
					|| ontology.getRtdbDatasource().equals(RtdbDatasource.API_REST)
					|| ontology.getRtdbDatasource().equals(RtdbDatasource.PRESTO)) {
				response.put(REDIRECT_STR, REDIRECT_ONTOLOGY_LIST);
			}

			final Object projectId = httpSession.getAttribute(APP_ID);
			if (projectId != null) {
				httpSession.setAttribute("resourceTypeAdded", OPResource.Resources.ONTOLOGY.toString());
				httpSession.setAttribute("resourceIdentificationAdded", ontology.getIdentification());
				httpSession.removeAttribute(APP_ID);
				response.put(REDIRECT_STR, REDIRECT_PROJECT_SHOW + projectId.toString());
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
	@Transactional
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
			/*
			 * final Ontology createdOnt =
			 * ontologyTimeSeriesService.createOntologyTimeSeries(ontologyTimeSeriesDTO,
			 * config, true, true);
			 */
			final Ontology createdOnt = timeSeriesBusinessService.createOntology(ontologyTimeSeriesDTO, config, true,
					true);
			if (createdOnt != null) {
				response.put(REDIRECT_STR, REDIRECT_ONTOLOGY_LIST);

				final Object projectId = httpSession.getAttribute(APP_ID);
				if (projectId != null) {
					httpSession.setAttribute("resourceTypeAdded", OPResource.Resources.ONTOLOGY.toString());
					httpSession.setAttribute("resourceIdentificationAdded", createdOnt.getIdentification());
					httpSession.removeAttribute(APP_ID);
					response.put(REDIRECT_STR, REDIRECT_PROJECT_SHOW + projectId.toString());
				}

				response.put(STATUS_STR, "ok");
				return new ResponseEntity<>(response, HttpStatus.CREATED);
			} else {
				response.put(REDIRECT_STR, "/controlpanel/ontologies/list");

				final Object projectId = httpSession.getAttribute(APP_ID);
				if (projectId != null) {
					response.put(REDIRECT_STR, REDIRECT_PROJECT_SHOW + projectId.toString());
				}

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
		ontology.setSupportsJsonLd(ontologyKPIDTO.isSupportsJsonLd());
		ontology.setJsonLdContext(ontologyKPIDTO.getJsonLdContext());
		ontology.setEnableDataClass(ontologyKPIDTO.isEnableDataClass());

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

				final List<App> realms = appService.getAppsByUser(ontology.getUser().getUserId(), null);

				final List<OntologyDataAccess> dataAccesses = ontologyConfigService
						.getOntologyUserDataAccesses(ontology.getId(), utils.getUserId());
				final List<OntologyDataAccessDTO> dataAccessesDTO = new ArrayList<>();

				for (final OntologyDataAccess dataAccess : dataAccesses) {
					if (dataAccess.getUser() != null && dataAccess.getUser().isActive()
							|| dataAccess.getAppRole() != null) {
						dataAccessesDTO.add(new OntologyDataAccessDTO(dataAccess));
					}
				}

				final List<String> propdclasses = new ArrayList<>();
				final List<String> entitydclasses = new ArrayList<>();
				;
				List<Configuration> configs = configurationRepository.findByType(Type.DATACLASS);
				for (Configuration config : configs) {
					String dcname = config.getIdentification();

					final Map<String, Object> dclassyml = (Map<String, Object>) configurationService
							.fromYaml(config.getYmlConfig()).get("dataclass");
					ArrayList<Map<String, Object>> rules = (ArrayList<Map<String, Object>>) dclassyml
							.get("dataclassrules");
					for (Map<String, Object> rule : rules) {
						if (rule.get("ruletype").toString().equalsIgnoreCase("entity")) {
							entitydclasses.add(dcname + "." + rule.get("rulename").toString());
						} else if (rule.get("ruletype").toString().equalsIgnoreCase("property")) {
							propdclasses.add(dcname + "." + rule.get("rulename").toString());
						}
					}
				}

				model.addAttribute(DATAACCESSES, dataAccessesDTO);
				model.addAttribute(AUTHORIZATIONS, authorizationsDTO);
				model.addAttribute(ONTOLOGY_STR, ontology);
				model.addAttribute(ONTOLOGYTSDTO, otsDTO);
				model.addAttribute(USERS, users);
				model.addAttribute(REALMS, realms);
				model.addAttribute(PROPDATACLASSES, propdclasses);
				model.addAttribute(ENTITYDATACLASSES, entitydclasses);
				if (ontology.isAllowsCreateMqttTopic()) {
					model.addAttribute(MQTT_TOPIC_NAME,
							ontologyMqttTopicRepo.findByOntology(ontology).getIdentification());
				}
				final OntologyElasticDTO elasticOntologyDTO = getDefaultElasticValues();
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
				} else if (ontology.getRtdbDatasource().equals(RtdbDatasource.ELASTIC_SEARCH)
						|| ontology.getRtdbDatasource().equals(RtdbDatasource.OPEN_SEARCH)) {
					// GET OntologyElastic object
					final OntologyElastic elasticOntology = ontologyConfigService
							.getOntologyElasticByOntologyId(ontology);
					if (elasticOntology != null) {
						elasticOntologyDTO.setReplicas(elasticOntology.getReplicas());
						elasticOntologyDTO.setShards(elasticOntology.getShards());
						elasticOntologyDTO.setCustomConfig(elasticOntology.getCustomConfig());
						elasticOntologyDTO.setTemplateConfig(elasticOntology.getTemplateConfig());
						elasticOntologyDTO.setPatternField(elasticOntology.getPatternField());
						elasticOntologyDTO.setPatternFunction(elasticOntology.getPatternFunction().name());
						elasticOntologyDTO.setSubstringStart(elasticOntology.getSubstringStart());
						elasticOntologyDTO.setSubstringEnd(elasticOntology.getSubstringEnd());
						elasticOntologyDTO.setCustomIdConfig(elasticOntology.getCustomIdConfig());
						elasticOntologyDTO.setAllowsUpsertById(elasticOntology.getAllowsUpsertById());
						elasticOntologyDTO.setCustomIdField(elasticOntology.getIdField());
					}
				}
				if (ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)) {
					final OntologyVirtual ontologyVirtual = ontologyConfigService
							.getOntologyVirtualByOntologyId(ontology);
					populateFormVirtual(model);
					final VirtualDatasourceInfoDTO vDTO = ontologyBusinessService
							.getInfoFromDatasource(ontologyVirtual.getDatasourceId().getIdentification());
					model.addAttribute("datasource", ontologyVirtual.getDatasourceId());
					model.addAttribute("databaseName",
							ontologyVirtual.getDatasourceDatabase() == null
									|| "".equals(ontologyVirtual.getDatasourceDatabase()) ? vDTO.getCurrentDatabase()
											: ontologyVirtual.getDatasourceDatabase());
					model.addAttribute("schemaName",
							ontologyVirtual.getDatasourceSchema() == null
									|| "".equals(ontologyVirtual.getDatasourceSchema()) ? vDTO.getCurrentSchema()
											: ontologyVirtual.getDatasourceSchema());
					model.addAttribute("tableName", ontologyVirtual.getDatasourceTableName());
					model.addAttribute("objId", ontologyVirtual.getObjectId());
					model.addAttribute("objGeometry", ontologyVirtual.getObjectGeometry());
					model.addAttribute(HISTORICAL, false);
					return ONTOLOGIES_CREATEVIRTUAL;
				}
				if (ontology.getRtdbDatasource().equals(RtdbDatasource.PRESTO)) {
					final OntologyPresto ontologyPresto = ontologyConfigService.getOntologyPrestoByOntologyId(ontology);
					populateFormHistorical(model);
					populateFormPresto(model);
					model.addAttribute("databaseName", ontologyPresto.getDatasourceCatalog());
					model.addAttribute("catalogName", ontologyPresto.getDatasourceCatalog());
					model.addAttribute("schemaName", ontologyPresto.getDatasourceSchema());
					model.addAttribute("tableName", ontologyPresto.getDatasourceTableName());
					model.addAttribute(HISTORICAL, prestoDatasourceConfigurationService
							.isHistoricalCatalog(ontologyPresto.getDatasourceCatalog()));

					return ONTOLOGIES_CREATEPRESTO;
				}
				if (ontology.getRtdbDatasource().equals(RtdbDatasource.NEBULA_GRAPH)) {
					model.addAttribute("nebulaTags", nebulaGraphService.getTags(ontology.getIdentification()));
					model.addAttribute("nebulaEdges", nebulaGraphService.getEdges(ontology.getIdentification()));
					model.addAttribute("fieldTypes", NebulaGraphService.NEBULA_TYPES);
				}

				if (ontology.getDataModel().getId().equals(TIMESERIES_DATAMODEL)) {

					populateFormTimeseries(model);
					return ONTOLOGIES_CREATE_TS;
				} else {
					model.addAttribute(ONTOLOGY_REST_STR, new OntologyRestDTO());
					populateForm(model);
				}
				model.addAttribute(ONTOLOGY_ELASTIC_STR, elasticOntologyDTO);

				if (ontology.getRtdbDatasource().equals(RtdbDatasource.MONGO)) {
					List<String> listIndexTrue = new ArrayList<>();
					List<OntologyPropertiesIndexConfDTO> listProperties = new ArrayList<OntologyPropertiesIndexConfDTO>();
					List<OntologyListIndexMongoConfDTO> listIndex = new ArrayList<OntologyListIndexMongoConfDTO>();

					String getindexMongoDB = MongoNativeDBRepository.getIndexesOptions(ontology.getIdentification());
					listIndex = ontologyConfigService.getIndexTrue(getindexMongoDB);
					listProperties = ontologyConfigService.getPropertiesOntology(ontology, listIndexTrue);

					model.addAttribute(ONTOLOGYPROPINDEXCONFSDTO, listProperties);
					model.addAttribute(ONTOLOGYTSDTO, ontology);
					model.addAttribute("listIndex", listIndex);

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

				final List<OntologyDataAccess> dataAccesses = ontologyConfigService
						.getOntologyUserDataAccesses(ontology.getId(), utils.getUserId());
				final List<OntologyDataAccessDTO> dataAccessesDTO = new ArrayList<>();

				for (final OntologyDataAccess dataAccess : dataAccesses) {
					if (dataAccess.getUser() != null && dataAccess.getUser().isActive()
							|| dataAccess.getAppRole() != null) {
						dataAccessesDTO.add(new OntologyDataAccessDTO(dataAccess));
					}
				}

				model.addAttribute(DATAACCESSES, dataAccessesDTO);
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
			@RequestParam(required = false, name = "nebulaEntity") String nebulaEntity, HttpServletRequest request)
			throws DBPersistenceException, OntologyDataUnauthorizedException, GenericOPException {
		final Map<String, String> response = new HashMap<>();

		if (bindingResult.hasErrors()) {
			log.debug("Some ontology properties missing");
			response.put(STATUS_STR, ERROR_STR);
			response.put(CAUSE_STR, utils.getMessage(ONT_VAL_ERROR, VAL_ERROR));
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

		try {

			boolean hasDocuments = ontologyBusinessService.hasDocuments(ontology);

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

			ontologyBusinessService.updateOntology(ontology, config, hasDocuments);

			ontologyConfigService.updateOntology(ontology, utils.getUserId(), config, hasDocuments);

			if (ontologyFound != null && ontologyFound.getOntologyKPI() != null
					&& ontologyFound.getOntologyKPI().getId() != null) {
				ontologyKPIRepository.save(ontology.getOntologyKPI());
			}

			if (ontologyFound != null && ontologyFound.getOntologyKPI() != null
					&& ontologyFound.getOntologyKPI().getId() != null && ontology.getOntologyKPI().isActive()) {

				ontologyKPIService.unscheduleKpi(ontology.getOntologyKPI());
				ontologyKPIService.scheduleKpi(ontology.getOntologyKPI());

			}

			if (StringUtils.hasText(nebulaEntity)) {
				nebulaGraphService
						.updateNebulaGraphEntity(mapper.readValue(nebulaEntity, NebulaGraphUpdateEntity.class));
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
		} catch (final JsonProcessingException e) {
			log.error("Nebula entity not valid JSON");
			response.put(STATUS_STR, ERROR_STR);
			response.put(CAUSE_STR, "Nebula entity not valid JSON");
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
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
			// ontologyTimeSeriesService.updateOntologyTimeSeries(ontologyDTO,
			// utils.getUserId(), config);
			timeSeriesBusinessService.updateOntology(ontologyDTO, utils.getUserId(), config, true);

		} catch (final OntologyServiceException | OntologyDataJsonProblemException
				| TimeSerieOntologyBusinessServiceException e) {
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
					if (apis.equals("") && iotClients.equals("")) {
						utils.addRedirectMessageWithParam(ONT_DEL_ERROR,
								"Cannot delete an ontology with attached elements", redirect);
					} else {
						utils.addRedirectMessageWithParam(ONT_DEL_ERROR,
								"Cannot delete an ontology with attached elements:\n -API: " + apis + "\n"
										+ "-IoTClient: " + iotClients,
								redirect);
					}
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

				String userOntologyAccess = "";
				if (utils.isAdministrator() || ontology.getUser().getUserId().equals(utils.getUserId())) {

					userOntologyAccess = "ALL";

				}
				for (final OntologyUserAccess authorization : authorizations) {
					if (authorization.getUser().isActive()) {
						authorizationsDTO.add(new OntologyUserAccessDTO(authorization));
					}
					if (authorization.getUser().getUserId().equals(utils.getUserId())) {

						userOntologyAccess = authorization.getOntologyUserAccessType().getName().toUpperCase();

					}
				}

				final List<User> users = userService.getAllActiveUsers();
				OntologyTimeSeriesServiceDTO otsDTO = new OntologyTimeSeriesServiceDTO();
				if (ontology.getDataModel().getId().equals(TIMESERIES_DATAMODEL)) {
					otsDTO = ontologyTimeSeriesService.generateOntologyTimeSeriesDTO(ontology);
				}

				final List<OntologyDataAccess> dataAccesses = ontologyConfigService
						.getOntologyUserDataAccesses(ontology.getId(), utils.getUserId());
				final List<OntologyDataAccessDTO> dataAccessesDTO = new ArrayList<>();

				for (final OntologyDataAccess dataAccess : dataAccesses) {
					if (dataAccess.getUser() != null && dataAccess.getUser().isActive()
							|| dataAccess.getAppRole() != null) {
						dataAccessesDTO.add(new OntologyDataAccessDTO(dataAccess));
					}
				}

				model.addAttribute(DATAACCESSES, dataAccessesDTO);

				model.addAttribute(ONTOLOGYTSDTO, otsDTO);
				model.addAttribute(ONTOLOGY_STR, ontology);
				model.addAttribute(USER_ONTOLOGY_ACCESS, userOntologyAccess);
				if (ontology.isAllowsCreateMqttTopic()) {
					model.addAttribute(MQTT_TOPIC_NAME,
							ontologyMqttTopicRepo.findByOntology(ontology).getIdentification() + "/"
									+ ontology.getIdentification());
				}
				model.addAttribute(AUTHORIZATIONS, authorizationsDTO);
				User sessionUser = userService.getUser(id);
				for (OntologyUserAccessDTO permission : authorizationsDTO) {
					if (permission.getTypeName() == "ALL" && permission.getUserId().equals(sessionUser)) {
						OntologyDataAccessDTO obj = new OntologyDataAccessDTO(null);
						obj.setOntologyPermission(true);
					}
				}
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
				} else if (ontology.getRtdbDatasource().equals(RtdbDatasource.ELASTIC_SEARCH)
						|| ontology.getRtdbDatasource().equals(RtdbDatasource.OPEN_SEARCH)) {
					final OntologyElasticDTO elasticOntologyDTO = getDefaultElasticValues();
					final OntologyElastic elasticOntology = ontologyConfigService
							.getOntologyElasticByOntologyId(ontology);
					if (elasticOntology != null) {
						elasticOntologyDTO.setReplicas(elasticOntology.getReplicas());
						elasticOntologyDTO.setShards(elasticOntology.getShards());
						elasticOntologyDTO.setCustomConfig(elasticOntology.getCustomConfig());
						elasticOntologyDTO.setTemplateConfig(elasticOntology.getTemplateConfig());
						elasticOntologyDTO.setPatternField(elasticOntology.getPatternField());
						elasticOntologyDTO.setPatternFunction(elasticOntology.getPatternFunction().name());
						elasticOntologyDTO.setSubstringStart(elasticOntology.getSubstringStart());
						elasticOntologyDTO.setSubstringEnd(elasticOntology.getSubstringEnd());
						elasticOntologyDTO.setCustomIdConfig(elasticOntology.getCustomIdConfig());
						elasticOntologyDTO.setAllowsUpsertById(elasticOntology.getAllowsUpsertById());
						elasticOntologyDTO.setCustomIdField(elasticOntology.getIdField());
					}
					model.addAttribute(ONTOLOGY_ELASTIC_STR, elasticOntologyDTO);
				} else if (ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)) {
					final OntologyVirtual ov = ontologyConfigService.getOntologyVirtualByOntologyId(ontology);
					model.addAttribute(RTDB_DATASOURCE_TYPE, ov.getDatasourceId().getSgdb());
					model.addAttribute(IS_ONTOLOGY_REST, false);
				} else if (ontology.getRtdbDatasource().equals(RtdbDatasource.PRESTO)) {
					final OntologyPresto op = ontologyConfigService.getOntologyPrestoByOntologyId(ontology);
					model.addAttribute(RTDB_DATASOURCE_TYPE, op.getDatasourceCatalog().toUpperCase());
					model.addAttribute(IS_ONTOLOGY_REST, false);
				} else if (ontology.getRtdbDatasource().equals(RtdbDatasource.NEBULA_GRAPH)) {
					model.addAttribute(IS_ONTOLOGY_REST, false);
					model.addAttribute("nebulaTags", nebulaGraphService.getTags(ontology.getIdentification()));
					model.addAttribute("nebulaEdges", nebulaGraphService.getEdges(ontology.getIdentification()));
				} else {
					model.addAttribute(IS_ONTOLOGY_REST, false);
				}
				if (ontology.getRtdbDatasource().equals(RtdbDatasource.MONGO)) {

					List<OntologyListIndexMongoConfDTO> listIndex = new ArrayList<OntologyListIndexMongoConfDTO>();

					if (ontology.getRtdbDatasource().name() == "MONGO") {
						// String getindexMongoDB =
						// MongoNativeDBRepository.getIndexes(entity.getIdentification());
						String getindexMongoDB = MongoNativeDBRepository
								.getIndexesOptions(ontology.getIdentification());
						listIndex = ontologyConfigService.getIndexTrue(getindexMongoDB);

					}
					model.addAttribute("listIndex", listIndex);
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

		final List<Ontology.RtdbDatasource> listRtdbs = ontologyConfigService.getDatasources().stream()
				.filter(o -> !Arrays.asList(RtdbDatasource.VIRTUAL, RtdbDatasource.API_REST, RtdbDatasource.AI_MINDS_DB,
						RtdbDatasource.DIGITAL_TWIN, RtdbDatasource.PRESTO, RtdbDatasource.TIMESCALE,
						RtdbDatasource.NEBULA_GRAPH).contains(o))
				.collect(Collectors.toList());
		model.addAttribute(RTDBS, listRtdbs);
		model.addAttribute(ONTOLOGIES_STR, ontologyConfigService.getOntologiesByUserId(utils.getUserId()));
		model.addAttribute("modes", Ontology.RtdbToHdbStorage.values());

		final List<String> propdclasses = new ArrayList<>();
		final List<String> entitydclasses = new ArrayList<>();
		;
		List<Configuration> configs = configurationRepository.findByType(Type.DATACLASS);
		for (Configuration config : configs) {
			String dcname = config.getIdentification();

			final Map<String, Object> dclassyml = (Map<String, Object>) configurationService
					.fromYaml(config.getYmlConfig()).get("dataclass");
			ArrayList<Map<String, Object>> rules = (ArrayList<Map<String, Object>>) dclassyml.get("dataclassrules");
			for (Map<String, Object> rule : rules) {
				if (rule.get("ruletype").toString().equalsIgnoreCase("entity")) {
					entitydclasses.add(dcname + "." + rule.get("rulename").toString());
				} else if (rule.get("ruletype").toString().equalsIgnoreCase("property")) {
					propdclasses.add(dcname + "." + rule.get("rulename").toString());
				}
			}
		}
		model.addAttribute(PROPDATACLASSES, propdclasses);
		model.addAttribute(ENTITYDATACLASSES, entitydclasses);
	}

	private List<RtdbDatasource> filterRtdbForKPIs(List<RtdbDatasource> list) {
		List<RtdbDatasource> result = new ArrayList<RtdbDatasource>();
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			RtdbDatasource rtdbDatasource = (RtdbDatasource) iterator.next();
			if (rtdbDatasource.name().equals("MONGO") || rtdbDatasource.name().equals("ELASTIC_SEARCH")
					|| rtdbDatasource.name().equals("OPEN_SEARCH") || rtdbDatasource.name().equals("COSMOS_DB")) {
				result.add(rtdbDatasource);
			}

		}

		return result;
	}

	private List<Ontology> filterOntologyForKPIs(List<Ontology> list) {
		List<Ontology> result = new ArrayList<Ontology>();
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			Ontology onto = (Ontology) iterator.next();
			if (onto.getRtdbDatasource().name().equals("MONGO")
					|| onto.getRtdbDatasource().name().equals("ELASTIC_SEARCH")
					|| onto.getRtdbDatasource().name().equals("OPEN_SEARCH")
					|| onto.getRtdbDatasource().name().equals("COSMOS_DB")) {
				result.add(onto);
			}

		}

		return result;
	}

	private void populateKPIForm(Model model) {
		model.addAttribute(DATA_MODELS_STR, ontologyConfigService.getAllDataModels());
		model.addAttribute(DATA_MODEL_TYPES_STR, ontologyConfigService.getAllDataModelTypes());
		model.addAttribute(RTDBS, filterRtdbForKPIs(ontologyConfigService.getDatasources()));
		model.addAttribute(ONTOLOGIES_STR,
				filterOntologyForKPIs(ontologyConfigService.getOntologiesByUserId(utils.getUserId())));

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

		List<VirtualDatasourceDTO> dsList = new ArrayList<>();

		if (!ontologyConfigService.getDatasourcesRelationals().isEmpty()) {
			if (utils.isAdministrator()) {
				dsList = ontologyConfigService.getDatasourcesRelationals();
			} else {
				dsList = ontologyConfigService.getPublicOrOwnedDatasourcesRelationals(utils.getUserId());
			}
		}
		dsList.removeIf(ds -> (ds.getIdentification().equals(RtdbDatasource.PRESTO.toString())));
		model.addAttribute("datasources", dsList);
		model.addAttribute("collectionNames", new ArrayList<String>());

		model.addAttribute("datasource", new OntologyVirtualDatasource());
		model.addAttribute(USER_BUCKET_ONTOLOGY_PATH, minioObjectStoreService.getUserBucketName(utils.getUserId())
				+ MinioObjectStorageService.ONTOLOGIES_DIR);

		model.addAttribute(TIMESERIESDB, timeseriesdbConnection);
	}

	private void populateFormTimeseries(Model model) {
		model.addAttribute(DATA_MODELS_STR, ontologyConfigService.getEmptyBaseDataModel());
		model.addAttribute(DATA_MODEL_TYPES_STR, ontologyConfigService.getAllDataModelTypes());
		model.addAttribute(RTDBS, ontologyConfigService.getDatasources());
		final List<Ontology.RtdbDatasource> timeseriesEngines = new ArrayList<>();
		timeseriesEngines.add(Ontology.RtdbDatasource.MONGO);
		timeseriesEngines.add(Ontology.RtdbDatasource.TIMESCALE);
		model.addAttribute("timeseriesengs", timeseriesEngines);
		model.addAttribute("types", OntologyTimeSeriesProperty.PropertyDataType.values());
		model.addAttribute("windowtypes", OntologyTimeSeriesWindow.WindowType.values());
		model.addAttribute("frequencies", OntologyTimeSeriesWindow.FrecuencyUnit.values());
		model.addAttribute("aggregates", Arrays.asList(OntologyTimeSeriesWindow.AggregationFunction.LAST));
		model.addAttribute("timescaleAggregates",
				Arrays.asList(OntologyTimeSeriesProperty.AggregationFunction.values()));
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

	@PostMapping(value = "/dataaccess", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public ResponseEntity<OntologyDataAccessDTO> createOrUpdateDataAccess(@RequestParam String ontId,
			@RequestParam String realm, @RequestParam String role, @RequestParam String user, @RequestParam String rule,
			@RequestParam String id) {
		try {
			final OntologyDataAccess ontologyDataAccessCreated = ontologyConfigService.createOrUpdateDataAccess(ontId,
					realm, role, user, rule, utils.getUserId());
			final OntologyDataAccessDTO ontologyDataAccessDTO = new OntologyDataAccessDTO(ontologyDataAccessCreated);
			return new ResponseEntity<>(ontologyDataAccessDTO, HttpStatus.CREATED);
		} catch (final RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping(value = "/dataaccess/delete", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public ResponseEntity<String> deleteDataAccess(@RequestParam String id) {

		try {
			ontologyConfigService.deleteDataAccess(id, utils.getUserId());
			return new ResponseEntity<>("{\"status\" : \"ok\"}", HttpStatus.OK);
		} catch (final RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = "/getDatabases/{datasource}")
	public @ResponseBody ResponseEntity<?> getDatabases(@PathVariable("datasource") String datasource) {
		try {
			final List<String> tables = ontologyBusinessService.getDatabasesFromDatasource(datasource);
			if (tables.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			} else {
				return new ResponseEntity<>(tables, HttpStatus.OK);
			}
		} catch (final Exception e) {
			return new ResponseEntity<>("Error processing the request: " + e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = "/getDatasourceInfo/{datasource}")
	public @ResponseBody ResponseEntity<?> getInfoDatasource(@PathVariable("datasource") String datasource) {
		try {
			final VirtualDatasourceInfoDTO infoDTO = ontologyBusinessService.getInfoFromDatasource(datasource);
			return new ResponseEntity<>(infoDTO, HttpStatus.OK);
		} catch (final Exception e) {
			return new ResponseEntity<>("Error processing the request: " + e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = "/getSchemas/{datasource}")
	public @ResponseBody ResponseEntity<?> getSchemas(@PathVariable("datasource") String datasource) {
		return getSchemasDB(datasource, null);
	}

	@GetMapping(value = "/getInfoDto/{datasource}")
	public @ResponseBody ResponseEntity<?> getInfoDto(@PathVariable("datasource") String datasourceIdentification) {

		VirtualDatasourceInfoDTO infoDTO = ontologyBusinessService.getInfoFromDatasource(datasourceIdentification);
		return new ResponseEntity<>(infoDTO, HttpStatus.OK);
	}

	@GetMapping(value = "/getDomain/{datasource}")
	public @ResponseBody ResponseEntity<?> getDomain(@PathVariable("datasource") String datasourceIdentification) {
		User user = userService.getUser(utils.getUserId());

		List<OntologyVirtualDatasource> datasources = virtualDatasourceService.getAllDatasourcesByUser(user);

		for (OntologyVirtualDatasource datasource : datasources) {
			if (datasource.getIdentification().equals(datasourceIdentification)) {
				return new ResponseEntity<>(datasource.getDatasourceDomain(), HttpStatus.OK);
			}
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@GetMapping(value = "/getSchemas/{datasource}/{database}")
	public @ResponseBody ResponseEntity<?> getSchemasDB(@PathVariable("datasource") String datasource,
			@PathVariable("database") String database) {
		try {
			final List<String> tables = ontologyBusinessService.getSchemasFromDatasourceDatabase(datasource, database);
			if (tables.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			} else {
				return new ResponseEntity<>(tables, HttpStatus.OK);
			}
		} catch (final Exception e) {
			return new ResponseEntity<>("Error processing the request: " + e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = "/getTableInformation/{datasource}/db/{database}")
	public @ResponseBody ResponseEntity<?> getTableInformationDB(@PathVariable("datasource") String datasource,
			@PathVariable("database") String database) {
		return getTableInformationDBSC(datasource, database, null);
	}

	@GetMapping(value = "/getTableInformation/{datasource}/sc/{schema}")
	public @ResponseBody ResponseEntity<?> getTableInformationSC(@PathVariable("datasource") String datasource,
			@PathVariable("schema") String schema) {
		return getTableInformationDBSC(datasource, null, schema);
	}

	@GetMapping(value = "/getTableInformation/{datasource}/db/{database}/sc/{schema}")
	public @ResponseBody ResponseEntity<?> getTableInformationDBSC(@PathVariable("datasource") String datasource,
			@PathVariable("database") String database, @PathVariable("schema") String schema) {
		try {
			final List<Map<String, Object>> columns = ontologyBusinessService
					.getTableInformationFromDatasource(datasource, database, schema);
			List<Map<String, Object>> tablesPKInformation = ontologyBusinessService.getTablePKInformation(datasource,
					database, schema);
			if (columns.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			} else {
				Map<String, JSONArray> tableInfMap = new HashMap<>();

				for (Map<String, Object> tableColumnInf : columns) {

					if (tableInfMap.get(tableColumnInf.get("TABLE_NAME")) == null) {
						JSONArray columnInfArray = new JSONArray();
						JSONObject columnInf = new JSONObject();
						columnInf.put("COLUMN_NAME", (String) tableColumnInf.get("COLUMN_NAME"));

						for (Map<String, Object> pkInf : tablesPKInformation) {
							if (tableColumnInf.get("TABLE_NAME").equals(pkInf.get("TABLE_NAME"))
									&& tableColumnInf.get("COLUMN_NAME").equals(pkInf.get("COLUMN_NAME"))) {
								columnInf.put("PK", true);
								break;
							} else {
								columnInf.put("PK", false);
							}
						}
						columnInfArray.put(columnInf);
						tableInfMap.put((String) tableColumnInf.get("TABLE_NAME"), columnInfArray);
					} else {
						JSONObject columnInf = new JSONObject();
						columnInf.put("COLUMN_NAME", (String) tableColumnInf.get("COLUMN_NAME"));

						for (Map<String, Object> pkInf : tablesPKInformation) {
							if (tableColumnInf.get("TABLE_NAME").equals(pkInf.get("TABLE_NAME"))
									&& tableColumnInf.get("COLUMN_NAME").equals(pkInf.get("COLUMN_NAME"))) {
								columnInf.put("PK", true);
								break;
							} else {
								columnInf.put("PK", false);
							}
						}
						tableInfMap.get(tableColumnInf.get("TABLE_NAME")).put(columnInf);
					}
				}

				JSONArray tableInfArray = new JSONArray();

				for (String tableName : tableInfMap.keySet()) {

					JSONObject tableInf = new JSONObject();
					tableInf.put("TABLE_NAME", tableName);
					tableInf.put("COLUMS_NAMES", tableInfMap.get(tableName));
					tableInfArray.put(tableInf);
				}

				if (datasource.equals(RtdbDatasource.PRESTO.toString())
						&& (prestoDatasourceConfigurationService.isHistoricalCatalog(database)
								|| prestoDatasourceConfigurationService.isRealtimedbCatalog(database))) {
					List<String> ontologies = ontologyConfigService.getAllIdentificationsByUser(utils.getUserId());

					List<String> authorizedEntities = ontologies.stream().map(s -> s.toLowerCase())
							.filter(columns::contains).collect(Collectors.toList());

					return new ResponseEntity<>(authorizedEntities, HttpStatus.OK);
				} else {
					return new ResponseEntity<>(tableInfArray.toString(), HttpStatus.OK);
				}
			}
		} catch (final Exception e) {
			return new ResponseEntity<>("Error processing the request: " + e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = "/getTables/{datasource}/db/{database}")
	public @ResponseBody ResponseEntity<?> getTablesDB(@PathVariable("datasource") String datasource,
			@PathVariable("database") String database) {
		return getTablesDBSC(datasource, database, null);
	}

	@GetMapping(value = "/getTables/{datasource}/sc/{schema}")
	public @ResponseBody ResponseEntity<?> getTablesSC(@PathVariable("datasource") String datasource,
			@PathVariable("schema") String schema) {
		return getTablesDBSC(datasource, null, schema);
	}

	@GetMapping(value = "/getTables/{datasource}/db/{database}/sc/{schema}")
	public @ResponseBody ResponseEntity<?> getTablesDBSC(@PathVariable("datasource") String datasource,
			@PathVariable("database") String database, @PathVariable("schema") String schema) {
		try {
			final List<String> tables = ontologyBusinessService.getTablesFromDatasource(datasource, database, schema);
			if (tables.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			} else {
				if (datasource.equals(RtdbDatasource.PRESTO.toString())
						&& (prestoDatasourceConfigurationService.isHistoricalCatalog(database)
								|| prestoDatasourceConfigurationService.isRealtimedbCatalog(database))) {
					List<String> ontologies = ontologyConfigService.getAllIdentificationsByUser(utils.getUserId());

					List<String> authorizedEntities = ontologies.stream().map(s -> s.toLowerCase())
							.filter(tables::contains).collect(Collectors.toList());

					return new ResponseEntity<>(authorizedEntities, HttpStatus.OK);
				} else {
					return new ResponseEntity<>(tables, HttpStatus.OK);
				}
			}
		} catch (final Exception e) {
			return new ResponseEntity<>("Error processing the request: " + e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = "/getTables/{datasource}")
	public @ResponseBody ResponseEntity<?> getTables(@PathVariable("datasource") String datasource) {
		try {
			final List<String> tables = ontologyBusinessService.getTablesFromDatasource(datasource);
			if (tables.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			} else {
				return new ResponseEntity<>(tables, HttpStatus.OK);
			}
		} catch (final Exception e) {
			return new ResponseEntity<>("Error processing the request: " + e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = "/hasDocuments/{id}")
	public @ResponseBody ResponseEntity<?> hasDocuments(@PathVariable("id") String id) {
		try {
			final Ontology ontology = ontologyConfigService.getOntologyById(id, utils.getUserId());

			return new ResponseEntity<>(new Boolean(ontologyBusinessService.hasDocuments(ontology)), HttpStatus.OK);

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
			final String metaData = ontologyBusinessService.getRelationalSchema(datasource, null, null, collection);
			if (metaData.isEmpty()) {
				return new ResponseEntity<>("Collection or datasource not found", HttpStatus.NOT_FOUND);
			} else {
				return new ResponseEntity<>(metaData, HttpStatus.OK);
			}
		} catch (final Exception e) {
			return new ResponseEntity<>("Error processing the request", HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = "/getRelationalSchema/{datasource}/sc/{schema}/{collection}")
	public @ResponseBody ResponseEntity<?> getRelationalSchemaSC(@PathVariable("datasource") String datasource,
			@PathVariable("collection") String collection, @PathVariable("schema") String schema) {
		return getRelationalSchemaDBSC(datasource, null, schema, collection);
	}

	@GetMapping(value = "/getRelationalSchema/{datasource}/db/{database}/{collection}")
	public @ResponseBody ResponseEntity<?> getRelationalSchemaDB(@PathVariable("datasource") String datasource,
			@PathVariable("collection") String collection, @PathVariable("database") String database) {
		return getRelationalSchemaDBSC(datasource, database, null, collection);
	}

	@GetMapping(value = "/getRelationalSchema/{datasource}/db/{database}/sc/{schema}/{collection}")
	public @ResponseBody ResponseEntity<?> getRelationalSchemaDBSC(@PathVariable("datasource") String datasource,
			@PathVariable("database") String database, @PathVariable("schema") String schema,
			@PathVariable("collection") String collection) {
		try {
			final String metaData = ontologyBusinessService.getRelationalSchema(datasource, database, schema,
					collection);
			if (metaData.isEmpty()) {
				return new ResponseEntity<>("Collection or datasource not found", HttpStatus.NOT_FOUND);
			} else {
				return new ResponseEntity<>(metaData, HttpStatus.OK);
			}
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

		if (useQuasar()) {
			query = "select onequeryontology from ( " + query + " ) as onequeryontology limit 1";
		} else {
			query = "select * from ( " + query + " ) as onequeryontology limit 1";
		}
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

		final Object projectId = httpSession.getAttribute(APP_ID);
		if (projectId != null) {
			httpSession.setAttribute("resourceTypeAdded", OPResource.Resources.ONTOLOGY.toString());
			httpSession.setAttribute("resourceIdentificationAdded", ontology.getIdentification());
			httpSession.removeAttribute(APP_ID);
			response.put(REDIRECT_STR, REDIRECT_PROJECT_SHOW + projectId.toString());
		}

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
					response.put(CAUSE_STR, utils.getMessage(result.get(MESSAGE_STR), ""));
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
			@Parameter(description = "Ontology name") @RequestParam(required = true) String ontology,
			@Parameter(description = "Ontology json schema") @RequestParam(required = true) String schema,
			@Parameter(description = "Datasource type") @RequestParam(required = true) VirtualDatasourceType datasource) {
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
			@Parameter(description = "Ontology identification") @PathVariable("identification") String identification,
			@Parameter(description = "Ontology Database") @RequestParam(name = "database", required = false) String database,
			@Parameter(description = "Ontology Schema") @RequestParam(name = "schema", required = false) String schema,
			@Parameter(description = "Ontology json schema") @Valid @RequestBody(required = true) CreateStatementDTO statementDTO,
			@Parameter(description = "Datasource type") @RequestParam(required = true) VirtualDatasourceType datasource) {
		ResponseEntity<String> response = null;
		try {
			final CreateStatementBusiness statement = statementDTO.toCreateStatement();
			statement.setOntology(identification);
			statement.setDatabase(database);
			statement.setSchema(schema);
			final String definition = ontologyBusinessService.getSQLCreateTable(statement, datasource);
			final JsonObject responseBody = new JsonObject();
			responseBody.addProperty("statement", definition);
			response = new ResponseEntity<>(responseBody.toString(), HttpStatus.OK);

		} catch (final Exception e) {

			response = new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		return response;
	}

	@GetMapping("/virtual/datasource/{identification}")
	public ResponseEntity<?> getDataSourceByIdentification(
			@Parameter(description = "Ontology datasource identification") @PathVariable(required = true) String identification) {
		ResponseEntity<?> response = null;
		try {
			final User user = userService.getUser(utils.getUserId());
			OntologyVirtualDatasource datasource = null;
			datasource = ontologyConfigService.getOntologyVirtualDatasourceByName(identification);

			if (datasource == null) {
				response = new ResponseEntity<>(HttpStatus.NO_CONTENT);
			} else {
				if (userService.isUserAdministrator(user) || datasource.isPublic()
						|| datasource.getUser().getUserId().contentEquals(user.getUserId())) {

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
		final List<String> propertyNames = new LinkedList<String>();
		for (final OntologyTimeSeriesProperty otsp : props) {
			propertyNames.add(otsp.getPropertyName());
		}
		return propertyNames;
	}

	@GetMapping("/statistics/{identification}")
	public String getStatistics(Model model,
			@Parameter(description = "Ontology identification") @PathVariable(required = true) String identification) {
		try {

			if (ontologyConfigService.hasUserPermissionForQuery(utils.getUserId(), identification)) {

				String queryResult = queryToolService.querySQLAsJson(utils.getUserId(), identification,
						"select * from " + identification + " limit 1000", 0);
				Table table = new JsonReader().read(Source.fromString(queryResult));

				List<OntologyStatisticsDTO> statistics = generateStatistics(queryResult, table);

				model.addAttribute("statistics", statistics);
			} else {
				log.error("You don't have permissions for this ontology");
				model.addAttribute("Statistics", utils.getMessage("querytool.ontology.access.denied.json",
						"You don't have permissions for this ontology"));
				return REDIRECT_ONTOLOGIES_LIST;
			}

		} catch (final Exception e) {
			log.error("You don't have permissions for this ontology", e);
			model.addAttribute("Statistics", e.getMessage());
			return REDIRECT_ONTOLOGIES_LIST;
		}
		return "ontologies/showstatistics";
	}

	private List<OntologyStatisticsDTO> generateStatistics(String data, Table table) throws JsonProcessingException {
		if (table.columnNames().contains("_id.$oid"))
			table = table.rejectColumns("_id.$oid");
		if (table.columnNames().contains("contextData.user"))
			table = table.rejectColumns("contextData.user", "contextData.clientConnection", "contextData.timestamp",
					"contextData.timestampMillis", "contextData.clientSession", "contextData.device",
					"contextData.deviceTemplate", "contextData.source", "contextData.timezoneId");
		List<OntologyStatisticsDTO> statistics = new ArrayList<>();
		for (Column<?> c : table.columns()) {
			OntologyStatisticsDTO ontStat = new OntologyStatisticsDTO();
			ontStat.setField(c.name());
			ontStat.setType(c.type().name());
			Table sum = table.summarize(c, countNonMissing, mean, min, max, stdDev).apply();
			if (!sum.columnNames().stream().anyMatch((a) -> a.startsWith("Count")))
				ontStat.setCountNonNull(null);
			if (!sum.columnNames().stream().anyMatch((a) -> a.startsWith("Mean")))
				ontStat.setMean(null);
			if (!sum.columnNames().stream().anyMatch((a) -> a.startsWith("Min")))
				ontStat.setMin(null);
			if (!sum.columnNames().stream().anyMatch((a) -> a.startsWith("Max")))
				ontStat.setMax(null);
			if (!sum.columnNames().stream().anyMatch((a) -> a.startsWith("Std")))
				ontStat.setStd(null);

			for (String cName : sum.columnNames()) {
				double value = (double) sum.column(cName).get(0);
				if (cName.startsWith("Count"))
					ontStat.setCountNonNull(value);
				else if (cName.startsWith("Mean"))
					ontStat.setMean(value);
				else if (cName.startsWith("Min"))
					ontStat.setMin(value);
				else if (cName.startsWith("Max"))
					ontStat.setMax(value);
				else if (cName.startsWith("Std"))
					ontStat.setStd(value);
			}

			if (c.type().equals(ColumnType.DOUBLE) || c.type().equals(ColumnType.FLOAT)
					|| c.type().equals(ColumnType.INTEGER) || c.type().equals(ColumnType.LONG)
					|| c.type().equals(ColumnType.SHORT)) {
				ontStat.setP25(AggregateFunctions.percentile((NumericColumn<?>) c, 0.25));
				ontStat.setP50(AggregateFunctions.percentile((NumericColumn<?>) c, 0.50));
				ontStat.setP75(AggregateFunctions.percentile((NumericColumn<?>) c, 0.75));
				Trace t = Histogram.create("Distribution of " + c.name(), table, c.name()).getTraces()[0];
				double[] graph = Stream.of(t.asJavascript(0).split("=")[1].split("x:")[1].split("opacity")[0]
						.substring(2, t.asJavascript(0).split("=")[1].split("x:")[1].split("opacity")[0].length() - 3)
						.trim().replace("\"", "").split(",")).mapToDouble(Double::parseDouble).toArray();

				ontStat.setGraph(new ObjectMapper().writeValueAsString(graph));
			} else {
				ontStat.setP25(null);
				ontStat.setP50(null);
				ontStat.setP75(null);
				ontStat.setGraph(null);
			}
			statistics.add(ontStat);
		}
		return statistics;
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

	@GetMapping(value = "/isHistoricalOntology/{id}")
	public @ResponseBody boolean isHistoricalOntology(@PathVariable("id") String id) {
		final Ontology o = ontologyRepository.findById(id).get();
		if (o.getRtdbDatasource().equals(Ontology.RtdbDatasource.PRESTO)) {
			final OntologyPresto op = ontologyConfigService.getOntologyPrestoByOntologyId(o);
			if (op != null) {
				if (prestoDatasourceConfigurationService.isHistoricalCatalog(op.getDatasourceCatalog()))
					return true;
			}
		}
		return false;
	}

	private boolean useQuasar() {
		try {
			return ((Boolean) resourcesService.getGlobalConfiguration().getEnv().getDatabase()
					.get("mongodb-use-quasar")).booleanValue();
		} catch (final RuntimeException e) {
			return true;
		}
	}

	private OntologyElasticDTO getDefaultElasticValues() {

		final OntologyElasticDTO elasticOntology = new OntologyElasticDTO();
		try {
			final Map<String, Object> database = resourcesService.getGlobalConfiguration().getEnv().getDatabase();

			@SuppressWarnings("unchecked")
			final Map<String, Object> elasticsearch = (Map<String, Object>) database.get("elasticsearch");

			@SuppressWarnings("unchecked")
			final Map<String, Object> defaults = (Map<String, Object>) elasticsearch.get("defaults");

			elasticOntology.setReplicas((int) defaults.get("replicas"));
			elasticOntology.setShards((int) defaults.get("shards"));
		} catch (final Exception e) {
			log.error(
					"Could not load values for ElasticSearc shards and replicas from configuration. Using default values.");
			elasticOntology.setReplicas(0);
			elasticOntology.setShards(5);
		}
		elasticOntology.setSubstringStart(0);
		elasticOntology.setSubstringEnd(-1);
		elasticOntology.setTemplateConfig(false);
		elasticOntology.setCustomConfig(false);
		elasticOntology.setCustomIdConfig(false);
		elasticOntology.setAllowsUpsertById(false);
		elasticOntology.setPatternFunction("NONE");
		return elasticOntology;
	}

	@GetMapping(value = "/createhistorical", produces = "text/html")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public String createHistorical(Model model, RedirectAttributes redirect) {
		if (!serviceStatusBean.isMinIOActive()) {
			redirect.addFlashAttribute(MODULE_NOT_ACTIVE_KEY,
					messageSource.getMessage("service.minio.down", null, LocaleContextHolder.getLocale()));
			return "redirect:/ontologies/create";
		}
		if (!serviceStatusBean.isPrestoActive()) {
			redirect.addFlashAttribute(MODULE_NOT_ACTIVE_KEY,
					messageSource.getMessage("service.presto.down", null, LocaleContextHolder.getLocale()));
			return "redirect:/ontologies/create";
		}
		model.addAttribute(ONTOLOGY_STR, new Ontology());
		model.addAttribute(HISTORICAL, true);
		model.addAttribute(FILE_FORMATS, FileFormat.values());
		populateFormHistorical(model);
		populateFormPresto(model);

		final Object projectId = httpSession.getAttribute(APP_ID);
		if (projectId != null) {
			model.addAttribute(APP_ID, projectId.toString());
		}

		return ONTOLOGIES_CREATEVIRTUAL;
	}

	@PostMapping(value = "/uploadHistoricalFile")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public ResponseEntity<Map<String, String>> uploadHistoricalFile(@RequestParam("file") MultipartFile file,
			@RequestParam(value = "ontologyName", required = true) String ontology, RedirectAttributes redirect) {
		final Map<String, String> response = new HashMap<>();
		if (file != null) {
			if (utils.isFileExtensionForbidden(file)) {
				response.put(STATUS_STR, ERROR_STR);
				response.put(CAUSE_STR, "File type not allowed");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			if (file.getSize() < 0) {
				response.put(STATUS_STR, ERROR_STR);
				response.put(CAUSE_STR, "Empty file");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			if (file.getSize() > utils.getMaxFileSizeAllowed()) {
				response.put(STATUS_STR, ERROR_STR);
				response.put(CAUSE_STR, "File size too large");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
		}
		try {
			if (file.getSize() > utils.getMaxFileSizeAllowed().longValue()) {
				throw new Exception("The file size is larger than max allowed");
			}
			ontologyBusinessService.uploadHistoricalFile(file, ontology);
			response.put(REDIRECT_STR, REDIRECT_ONTOLOGY_LIST);

			final Object projectId = httpSession.getAttribute(APP_ID);
			if (projectId != null) {
				httpSession.setAttribute("resourceTypeAdded", OPResource.Resources.ONTOLOGY.toString());
				httpSession.setAttribute("resourceIdentificationAdded", ontology);
				httpSession.removeAttribute(APP_ID);
				response.put(REDIRECT_STR, REDIRECT_PROJECT_SHOW + projectId.toString());
			}

			response.put(STATUS_STR, "ok");
			return new ResponseEntity<>(response, HttpStatus.CREATED);
		} catch (final Exception e) {
			log.error("Could not create binary file: {}", e);
			response.put(STATUS_STR, ERROR_STR);
			response.put(CAUSE_STR, e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
	}

	@Transactional
	@DeleteMapping("/{id}/data/{deleteData}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public String deleteWithData(Model model, @PathVariable("id") String id,
			@PathVariable("deleteData") boolean deleteData, RedirectAttributes redirect) {

		final Ontology ontology = ontologyConfigService.getOntologyById(id, utils.getUserId());
		if (ontology != null) {
			try {
				ontologyBusinessService.deleteOntologyAndData(id, utils.getUserId(), deleteData);
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
					if (apis.equals("") && iotClients.equals("")) {
						utils.addRedirectMessageWithParam(ONT_DEL_ERROR,
								"Cannot delete an ontology with attached elements", redirect);
					} else {
						utils.addRedirectMessageWithParam(ONT_DEL_ERROR,
								"Cannot delete an ontology with attached elements:\n -API: " + apis + "\n"
										+ "-IoTClient: " + iotClients,
								redirect);
					}
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

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PostMapping(value = "/createTimescaleContinuousAggregate")
	public @ResponseBody GenerateTimescaleContinuousAggregateResponse createTimescaleAggregate(
			@RequestBody TimescaleContinuousAggregateRequest request,
			@RequestParam(value = "ontologyIdentification", required = true) String ontologyIdentification) {

		final GenerateTimescaleContinuousAggregateResponse response = new GenerateTimescaleContinuousAggregateResponse();
		final boolean check = request == null || request.getName() == null || request.getAggregateQuery().equals("");
		response.setOk(!check);
		// Create aggregate and scheduler
		try {
			timeSeriesBusinessService.createContinuousAggregate(ontologyIdentification, utils.getUserId(), request);
		} catch (final TimescaleDBPersistantException e) {
			response.setOk(false);
			switch (e.getErrorType()) {
			default:
			case GENERAL:
				response.setErrorType(ErrorType.GENERAL);
				break;
			case AGGREGATE_CREATION:
				response.setErrorType(ErrorType.TIMESCALE_TABLE);
				break;
			case AGGREGATE_POLICY:
				response.setErrorType(ErrorType.TIMESCALE_POLICY);
				break;
			}
			response.setErrorMessage(e.getCause().getMessage());
		} catch (final OntologyServiceException e) {
			response.setOk(false);
			switch (e.getError()) {
			case USER_ACCESS_NOT_FOUND:
				response.setErrorType(ErrorType.UNAUTHORIZED);
				break;
			case EXISTING_ONTOLOGY:
				response.setErrorType(ErrorType.DUPLICATE_NAME);
				break;
			default:
				response.setErrorType(ErrorType.GENERAL);
			}
			response.setErrorMessage(e.getCause().getMessage());
		} catch (final Exception e) {
			log.error("Error while creating Timescale Aggregate for ontology {}. Cause={}, message={}",
					ontologyIdentification, e.getCause(), e.getMessage());
			response.setOk(false);
			response.setErrorType(ErrorType.GENERAL);
			response.setErrorMessage(e.getMessage());
		}
		return response;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@DeleteMapping(value = "/deleteTimescaleContinuousAggregate")
	public @ResponseBody GenerateTimescaleContinuousAggregateResponse deleteTimescaleAggregate(
			@RequestParam(value = "aggregateName", required = true) String aggregateName,
			@RequestParam(value = "ontologyIdentification", required = true) String ontologyIdentification) {

		final GenerateTimescaleContinuousAggregateResponse response = new GenerateTimescaleContinuousAggregateResponse();
		final boolean check = aggregateName == null || ontologyIdentification == null || aggregateName.isEmpty()
				|| ontologyIdentification.isEmpty();
		response.setOk(!check);
		// Create aggregate and scheduler
		try {
			timeSeriesBusinessService.deleteContinuousAggregate(ontologyIdentification, utils.getUserId(),
					aggregateName);
		} catch (final Exception e) {
			log.error("Error while deleting Timescale Aggregate for ontology {}. Cause={}, message={}",
					ontologyIdentification, e.getCause(), e.getMessage());
			response.setOk(false);
			response.setErrorMessage(e.getMessage());
		}
		return response;
	}

	@GetMapping(value = "/createpresto", produces = "text/html")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public String createPresto(Model model, RedirectAttributes redirect) {
		if (!serviceStatusBean.isPrestoActive()) {
			redirect.addFlashAttribute(MODULE_NOT_ACTIVE_KEY,
					messageSource.getMessage("service.presto.down", null, LocaleContextHolder.getLocale()));
			return "redirect:/ontologies/create";
		}

		final Ontology ontology = new Ontology();
		ontology.setJsonSchema("{\"type\": \"object\", \"$schema\": \"http://json-schema.org/draft-03/schema\","
				+ "\"required\": false,\"title\": \"\",\"description\": \"\",\"properties\": {},"
				+ "\"additionalProperties\": true}");
		ontology.setRtdbDatasource(RtdbDatasource.PRESTO);
		ontology.setDataModel(dataModelService.getDataModelByName(OntologyService.DATAMODEL_DEFAULT_NAME));
		model.addAttribute(ONTOLOGY_STR, ontology);
		model.addAttribute(HISTORICAL, false);
		populateFormPresto(model);

		final List<String> catalogList = prestoDatasourceService.getPrestoCatalogsByUser(utils.getUserId());
		model.addAttribute("catalogs",
				catalogList.stream().filter(s -> !prestoDatasourceConfigurationService.isHistoricalCatalog(s))
						.collect(Collectors.toList()));

		final Object projectId = httpSession.getAttribute(APP_ID);
		if (projectId != null) {
			model.addAttribute(APP_ID, projectId.toString());
		}

		return ONTOLOGIES_CREATEPRESTO;
	}

	private void populateFormHistorical(Model model) {
		model.addAttribute(DATA_MODELS_STR, ontologyConfigService.getEmptyBaseDataModel());
		model.addAttribute(DATA_MODEL_TYPES_STR, ontologyConfigService.getAllDataModelTypes());
		model.addAttribute(RTDBS, ontologyConfigService.getDatasources());
		model.addAttribute("fieldTypes", ontologyBusinessService.getStringSupportedFieldDataTypes());
		model.addAttribute("constraintTypes", ontologyBusinessService.getStringSupportedConstraintTypes());

		final List<VirtualDatasourceDTO> dsList = new ArrayList<>();
		final VirtualDatasourceDTO virtualDatasourceDTO = new VirtualDatasourceDTO(RtdbDatasource.PRESTO.toString(),
				"");
		dsList.add(virtualDatasourceDTO);
		model.addAttribute("datasources", dsList);

		final List<String> dbList = ontologyBusinessService
				.getDatabasesFromDatasource(RtdbDatasource.PRESTO.toString());
		model.addAttribute("databases", dbList);
		model.addAttribute("collections", new ArrayList<String>());
		model.addAttribute(HISTORICAL_CATALOG, prestoDatasourceConfigurationService.getHistoricalCatalog());
		model.addAttribute(HISTORICAL_SCHEMA, prestoDatasourceConfigurationService.getHistoricalSchema());
		model.addAttribute(USER_BUCKET_ONTOLOGY_PATH, minioObjectStoreService.getUserBucketName(utils.getUserId())
				+ MinioObjectStorageService.ONTOLOGIES_DIR);

		model.addAttribute("historicalCatalogActive", serviceStatusBean.isMinIOActive());
	}

	private void populateFormPresto(Model model) {

		final List<String> catalogList = prestoDatasourceService.getPrestoCatalogsByUser(utils.getUserId());
		final OntologyVirtualDatasource ds = new OntologyVirtualDatasource();

		ds.setIdentification(VirtualDatasourceType.PRESTO.toString());
		model.addAttribute("catalogs", catalogList);
		model.addAttribute("datasource", ds);
		model.addAttribute("tableNames", new ArrayList<String>());
	}

	@GetMapping(value = "/bulkcreation", produces = "text/html")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public String bulkCreation(Model model) {

		User user = userService.getUser(utils.getUserId());
		List<OntologyVirtualDatasource> datasources = virtualDatasourceService.getAllDatasourcesByUser(user).stream()
				.filter(ovd -> !ovd.getIdentification().equals(timeseriesdbConnection)).collect(Collectors.toList());
		final List<User> users = userService.getAllActiveUsers();
		model.addAttribute("datasources", datasources);
		model.addAttribute("users", users);

		return ONTOLOGIES_BULK_CREATE;
	}

	@PostMapping(value = "/bulkcreation/create")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public ResponseEntity<Map<String, String>> ontologiesBulkCreation(Model model, RedirectAttributes redirect,
			HttpServletRequest request) throws IOException {

		JSONArray failedOntologies = ontologyBusinessService.ontologyBulkGeneration(request, utils.getUserId());
		final Map<String, String> response = new HashMap<>();

		if (failedOntologies.length() == 0) {
			response.put(REDIRECT_STR, REDIRECT_ONTOLOGY_LIST);
			response.put(STATUS_STR, "ok");
			return new ResponseEntity<>(response, HttpStatus.CREATED);
		} else {

			response.put(STATUS_STR, ERROR_STR);
			response.put(CAUSE_STR, "error.");
			response.put("failedOntologies", failedOntologies.toString());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);

		}
	}
}
