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
package com.minsait.onesait.platform.controlpanel.rest.management.ontology;

import static tech.tablesaw.aggregate.AggregateFunctions.countNonMissing;
import static tech.tablesaw.aggregate.AggregateFunctions.max;
import static tech.tablesaw.aggregate.AggregateFunctions.mean;
import static tech.tablesaw.aggregate.AggregateFunctions.min;
import static tech.tablesaw.aggregate.AggregateFunctions.stdDev;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.minsait.onesait.platform.business.services.ontology.OntologyBusinessService;
import com.minsait.onesait.platform.business.services.ontology.OntologyBusinessServiceException;
import com.minsait.onesait.platform.business.services.ontology.graph.NebulaGraphBusinessService;
import com.minsait.onesait.platform.business.services.ontology.graph.NebulaGraphEntity;
import com.minsait.onesait.platform.business.services.ontology.graph.NebulaGraphUpdateEntity;
import com.minsait.onesait.platform.business.services.ontology.timeseries.TimeSerieOntologyBusinessServiceException;
import com.minsait.onesait.platform.business.services.ontology.timeseries.TimeSeriesOntologyBusinessService;
import com.minsait.onesait.platform.business.services.virtual.datasources.VirtualDatasourceService;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.DataModel;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.OntologyElastic;
import com.minsait.onesait.platform.config.model.OntologyKPI;
import com.minsait.onesait.platform.config.model.OntologyRest;
import com.minsait.onesait.platform.config.model.OntologyTimeSeries;
import com.minsait.onesait.platform.config.model.OntologyUserAccess;
import com.minsait.onesait.platform.config.model.OntologyVirtual;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.datamodel.DataModelService;
import com.minsait.onesait.platform.config.services.deletion.EntityDeletionService;
import com.minsait.onesait.platform.config.services.exceptions.OntologyServiceException;
import com.minsait.onesait.platform.config.services.ontology.OntologyConfiguration;
import com.minsait.onesait.platform.config.services.ontology.OntologyKpiCRUDService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.ontology.OntologyServiceImpl;
import com.minsait.onesait.platform.config.services.ontology.OntologyTimeSeriesService;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyKPIDTO;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyTimeSeriesServiceDTO;
import com.minsait.onesait.platform.config.services.ontologydata.DataSchemaValidationException;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataJsonProblemException;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataService;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataUnauthorizedException;
import com.minsait.onesait.platform.config.services.ontologyrest.OntologyRestService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.controller.jsontool.JsonToolUtils;
import com.minsait.onesait.platform.controlpanel.controller.ontology.OntologyStatisticsDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.ontology.model.KpiDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.ontology.model.OntologyCreateDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.ontology.model.OntologyDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.ontology.model.OntologyElasticDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.ontology.model.OntologyKpiDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.ontology.model.OntologyResponseErrorDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.ontology.model.OntologyRestDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.ontology.model.OntologySimplified;
import com.minsait.onesait.platform.controlpanel.rest.management.ontology.model.OntologySimplifiedResponseDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.ontology.model.OntologyTimeSeriesDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.ontology.model.OntologyUpdate;
import com.minsait.onesait.platform.controlpanel.rest.management.ontology.model.OntologyUserAccessSimplified;
import com.minsait.onesait.platform.controlpanel.rest.management.ontology.model.OntologyVirtualDTO;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.services.BasicOpsPersistenceServiceFacade;
import com.minsait.onesait.platform.persistence.services.QueryToolService;
import com.minsait.onesait.platform.quartz.services.ontologyKPI.OntologyKPIService;

import edu.emory.mathcs.backport.java.util.Collections;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Ontology Management")
@RestController
@ApiResponses({ @ApiResponse(responseCode = "400", description = "Bad request"),
		@ApiResponse(responseCode = "500", description = "Internal server error"),
		@ApiResponse(responseCode = "403", description = "Forbidden") })
@RequestMapping("api/ontologies")
@Slf4j
public class OntologyManagementController {

	private static final String USER_IS_NOT_AUTH = "The user is not authorized";
	private static final String ONTOLOGY_STR = "Ontology \"";
	private static final String NOT_EXIST = "\" does not exist";
	private static final String ALREADY_EXISTS = "\" already exists";
	private static final String ONTOLOGY_NOT_FOUND = "Ontologies not found";
	private static final String MESSAGE_STR = "message";
	private static final String STATUS_STR = "status";
	private static final String ERROR_STR = "error";
	private static final String OK_STR = "OK";
	private static final String IDENTIFICATION_STR = "identification";
	private static final String ERROR_NOT_IDENTIFICATION_PROVIDED = "Not 'identification' field provided";
	private static final String ERROR_IDENTIFICATION_FORMAT = "Identification Error: Use alphanumeric characters and '-', '_'";
	private static final String MSG_ONTOLOGY_CREATED_SUCCESS = "Ontology created successfully";
	private static final String MSG_ONTOLOGY_UPDATED_SUCCESS = "Ontology updated successfully";
	private static final String ERROR_CREATING_DATA_MODEL = "Not possible to create data model";
	private static final String ERROR_MISSING_DATAMODEL = "Not datamodel or datamodel identification provided";
	private static final String ERROR_INVALID_DATA_FORMAT = "Invalid date format, use: 'dd-MM-yyyy HH:mm:ss'";
	private static final String ERROR_DATASOURCE_DOMAIN = "Datasource domain name already exists";

	@Autowired
	private AppWebUtils utils;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private OntologyService ontologyConfigService;
	@Autowired
	private OntologyRestService ontologyRestService;
	@Autowired
	private OntologyKpiCRUDService ontologyKpiCRUDService;
	@Autowired
	private OntologyKPIService ontologyKPIService;
	@Autowired
	private OntologyTimeSeriesService ontologyTimeSeriesService;
	@Autowired
	private UserService userService;
	@Autowired
	private QueryToolService queryToolService;
	@Autowired
	private VirtualDatasourceService virtualDatasourceService;
	@Autowired
	private EntityDeletionService entityDeletionService;
	@Autowired
	private OntologyBusinessService ontologyBusinessService;
	@Autowired
	private DataModelService dataModelService;
	@Autowired
	private OntologyDTOConverter ontologyDTOConverter;
	@Autowired
	private BasicOpsPersistenceServiceFacade basicOpsRepository;
	@Autowired
	private NebulaGraphBusinessService nebulaGraphBusinessService;
	@Autowired
	private TimeSeriesOntologyBusinessService timeseriesBusinessService;

	@Operation(summary = "Get ontology by identification")
	@GetMapping("/identification/{identification}")
	@ApiResponses(@ApiResponse(content = @Content(schema = @Schema(implementation = OntologySimplified.class)), responseCode = "200", description = "OK"))
	public ResponseEntity<?> get(
			@Parameter(description = "Ontology identification", required = true) @PathVariable("identification") String ontologyIdentification) {

		try {
			final Ontology ontology = ontologyService.getOntologyByIdentification(ontologyIdentification,
					utils.getUserId());
			if (ontology == null) {
				return new ResponseEntity<>(ONTOLOGY_STR + ontologyIdentification + NOT_EXIST, HttpStatus.NOT_FOUND);
			} else {
				final OntologySimplified ontologySimplified = new OntologySimplified(ontology);
				if (ontology.getRtdbDatasource().equals(RtdbDatasource.NEBULA_GRAPH)) {
					ontologySimplified.setVertices(nebulaGraphBusinessService.getTags(ontologyIdentification));
					ontologySimplified.setEdges(nebulaGraphBusinessService.getEdges(ontologyIdentification));
				}
				return new ResponseEntity<>(ontologySimplified, HttpStatus.OK);
			}

		} catch (final OntologyServiceException exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.UNAUTHORIZED);
		}
	}

	@Operation(summary = "Get ontology by id")
	@GetMapping("/id/{id}")
	@ApiResponses(@ApiResponse(content = @Content(schema = @Schema(implementation = OntologySimplified.class)), responseCode = "200", description = "OK"))
	public ResponseEntity<?> getById(
			@Parameter(description = "Ontology id", required = true) @PathVariable("id") String ontologyId) {

		try {
			final Ontology ontology = ontologyService.getOntologyById(ontologyId, utils.getUserId());
			if (ontology == null) {
				return new ResponseEntity<>(ONTOLOGY_STR + ontologyId + NOT_EXIST, HttpStatus.NOT_FOUND);
			} else {
				final OntologySimplified ontologySimplified = new OntologySimplified(ontology);
				if (ontology.getRtdbDatasource().equals(RtdbDatasource.NEBULA_GRAPH)) {
					ontologySimplified.setVertices(nebulaGraphBusinessService.getTags(ontology.getIdentification()));
					ontologySimplified.setEdges(nebulaGraphBusinessService.getEdges(ontology.getIdentification()));
				}
				return new ResponseEntity<>(ontologySimplified, HttpStatus.OK);
			}

		} catch (final OntologyServiceException exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.UNAUTHORIZED);
		}
	}

	@Operation(summary = "Delete ontology by identification")
	@DeleteMapping("/{identification}")
	@Transactional
	public ResponseEntity<String> delete(
			@Parameter(description = "Ontology identification", required = true) @PathVariable("identification") String ontologyIdentification) {
		try {
			final Ontology ontology = ontologyService.getOntologyByIdentification(ontologyIdentification,
					utils.getUserId());

			if (ontology == null) {
				return new ResponseEntity<>(ONTOLOGY_STR + ontologyIdentification + NOT_EXIST, HttpStatus.BAD_REQUEST);
			}
			// final User user = userService.getUser(utils.getUserId());
			if ((utils.isAdministrator()) || ontology.getUser().getUserId().equals(utils.getUserId())) {
				ontologyBusinessService.deleteOntology(ontology.getId(), utils.getUserId());
			} else {
				return new ResponseEntity<>(USER_IS_NOT_AUTH, HttpStatus.UNAUTHORIZED);
			}

		} catch (final OntologyServiceException | JsonProcessingException exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>("Ontology deleted successfully", HttpStatus.OK);
	}

	@Operation(summary = "Get all ontologies. Filtering by dataModel or type=kpi if desired (dataModel and type are exclusive and can not be used both in the same request)")
	@GetMapping
	@ApiResponses(@ApiResponse(content = @Content(schema = @Schema(implementation = OntologySimplified[].class)), responseCode = "200", description = "OK"))
	public ResponseEntity<?> getAll(
			@Parameter(description = "Datamodel of the listed ontologies. Ignored if empty", required = false) @RequestParam(value = "dataModel", required = false, defaultValue = "") String dataModel,
			@Parameter(description = "Allowed values: (kpi|timeseries). Ignored if empty", required = false) @RequestParam(value = "type", required = false, defaultValue = "") String type) {

		if (dataModel.length() > 0 && type.length() > 0) {
			log.error("dataModel and type attributes cannot be in the same request");
			return new ResponseEntity<>("dataModel and type attributes cannot be in the same request",
					HttpStatus.BAD_REQUEST);
		}

		if (dataModel.equals("") && type.equals("")) {
			return this.getAll();
		} else if (dataModel.length() > 0) {// Search by DataModel
			return getAllByDataModel(dataModel);
		} else { // Search by Type
			return getAllByType(type);
		}
	}

	private ResponseEntity<?> getAll() {
		final List<Ontology> ontologies = ontologyService.getOntologiesByUserId(utils.getUserId());
		if (ontologies == null) {
			return new ResponseEntity<>(ONTOLOGY_NOT_FOUND, HttpStatus.NOT_FOUND);
		} else {
			final Set<OntologySimplified> ontologiesList = new TreeSet<>();
			ontologies.forEach(o -> ontologiesList.add(new OntologySimplified(o)));
			return new ResponseEntity<>(ontologiesList, HttpStatus.OK);
		}
	}

	private ResponseEntity<?> getAllByDataModel(String dataModel) {

		final List<Ontology> ontologies = ontologyService.getOntologiesByUserIdAndDataModel(utils.getUserId(),
				dataModel);
		if (ontologies == null) {
			return new ResponseEntity<>(ONTOLOGY_NOT_FOUND, HttpStatus.NOT_FOUND);
		} else {
			final Set<OntologySimplified> ontologiesList = new TreeSet<>();
			ontologies.forEach(o -> ontologiesList.add(new OntologySimplified(o)));
			return new ResponseEntity<>(ontologiesList, HttpStatus.OK);
		}

	}

	private ResponseEntity<?> getAllByType(String type) {
		// Validates type
		switch (type) {
		case OntologyServiceImpl.KPI_TYPE:
		case OntologyServiceImpl.TIMESERIES_TYPE:
			break;
		default:
			log.error("type {} not allowed, no ontologies will be returned", type);
			return new ResponseEntity<>("type: " + type + " not allowed", HttpStatus.BAD_REQUEST);
		}

		final List<Ontology> ontologies = ontologyService.getOntologiesByUserIdAndType(utils.getUserId(), type);
		if (ontologies == null) {
			return new ResponseEntity<>(ONTOLOGY_NOT_FOUND, HttpStatus.NOT_FOUND);
		} else {
			final Set<OntologySimplified> ontologiesList = new TreeSet<>();
			ontologies.forEach(o -> ontologiesList.add(new OntologySimplified(o)));
			return new ResponseEntity<>(ontologiesList, HttpStatus.OK);
		}
	}

	@Operation(summary = "Create new ontology")
	@PostMapping
	public ResponseEntity<?> create(
			@Parameter(description = "OntologyCreate", required = true) @Valid @RequestBody OntologyCreateDTO ontologyCreate) {
		final User user = userService.getUser(utils.getUserId());

		if (!userService.isUserAdministrator(user) && !userService.isUserDeveloper(user)) {
			return new ResponseEntity<>(USER_IS_NOT_AUTH, HttpStatus.UNAUTHORIZED);
		}

		if (!ontologyCreate.getIdentification().matches(AppWebUtils.IDENTIFICATION_PATERN)) {
			return new ResponseEntity<>(ERROR_IDENTIFICATION_FORMAT, HttpStatus.BAD_REQUEST);
		}

		final Ontology ontology = ontologyDTOConverter.ontologyCreateDTOToOntology(ontologyCreate, user);
		final OntologyConfiguration ontologyConfig = new OntologyConfiguration();
		if (ontology.getRtdbDatasource().equals(Ontology.RtdbDatasource.ELASTIC_SEARCH)
				|| ontology.getRtdbDatasource().equals(RtdbDatasource.OPEN_SEARCH)) {
			ontologyConfig.setAllowsCustomElasticConfig(true);
			ontologyConfig.setShards(String.valueOf(ontologyCreate.getShards()));
			ontologyConfig.setReplicas(String.valueOf(ontologyCreate.getReplicas()));
			ontologyConfig.setAllowsTemplateConfig(
					ontologyCreate.getPatternField() != null && !ontologyCreate.getPatternField().isEmpty());
			ontologyConfig.setPatternField(ontologyCreate.getPatternField());
			ontologyConfig.setPatternFunction(ontologyCreate.getPatternFunction());
			ontologyConfig.setSubstringStart(String.valueOf(ontologyCreate.getSubstringStart()));
			ontologyConfig.setSubstringEnd(String.valueOf(ontologyCreate.getSubstringEnd()));
		}
		try {
			ontologyBusinessService.createOntology(ontology, utils.getUserId(), ontologyConfig);
		} catch (final OntologyDataJsonProblemException jsonException) {
			return new ResponseEntity<>(jsonException.getMessage(), HttpStatus.BAD_REQUEST);
		} catch (final OntologyServiceException exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		} catch (final OntologyBusinessServiceException exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(MSG_ONTOLOGY_CREATED_SUCCESS, HttpStatus.OK);
	}

	@Operation(summary = "Create new rest ontology")
	@PostMapping(value = { "/rest" })
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> createRestOntology(
			@Parameter(description = "OntologyCreate", required = true) @Valid @RequestBody OntologyRestDTO ontologyDTO) {

		final User user = userService.getUser(utils.getUserId());

		final Ontology ontology = ontologyDTOConverter.ontologyDTOToOntology(ontologyDTO, user);
		final OntologyConfiguration ontologyConfig = ontologyDTOConverter
				.ontologyRestDTOToOntologyConfiguration(ontologyDTO);
		try {
			ontologyBusinessService.createOntology(ontology, utils.getUserId(), ontologyConfig);
		} catch (final OntologyDataJsonProblemException jsonException) {
			return new ResponseEntity<>(jsonException.getMessage(), HttpStatus.BAD_REQUEST);
		} catch (final OntologyServiceException exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		} catch (final OntologyBusinessServiceException exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(MSG_ONTOLOGY_CREATED_SUCCESS, HttpStatus.OK);
	}

	@Operation(summary = "Create new graph ontology")
	@PostMapping(value = { "/graph" })
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<String> createNebulaGraph(@RequestBody NebulaGraphEntity entity) {
		try {
			entity.setUser(utils.getUserId());
			nebulaGraphBusinessService.createNebulaGraphEntity(entity, true);
		} catch (final Exception e) {
			return ResponseEntity.internalServerError().body(e.getMessage());
		}
		return new ResponseEntity<>(MSG_ONTOLOGY_CREATED_SUCCESS, HttpStatus.OK);
	}

	@Operation(summary = "Update graph ontology")
	@PutMapping(value = { "/{identification}/graph" })
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<String> updateNebulaGraph(@RequestBody NebulaGraphUpdateEntity entity,
			@PathVariable("identification") String identification) {
		try {
			final Ontology ontology = ontologyService.getOntologyByIdentification(identification, utils.getUserId());
			if (ontology == null) {
				return new ResponseEntity<>(ONTOLOGY_STR + identification + NOT_EXIST, HttpStatus.BAD_REQUEST);
			}
			final User user = userService.getUser(utils.getUserId());
			if (!ontologyService.hasUserPermisionForChangeOntology(user, ontology)) {
				return new ResponseEntity<>(USER_IS_NOT_AUTH, HttpStatus.UNAUTHORIZED);
			}
			entity.setIdentification(identification);
			nebulaGraphBusinessService.updateNebulaGraphEntity(entity);
		} catch (final Exception e) {
			return ResponseEntity.internalServerError().body(e.getMessage());
		}
		return new ResponseEntity<>(MSG_ONTOLOGY_CREATED_SUCCESS, HttpStatus.OK);
	}

	@Operation(summary = "Delete new graph ontology")
	@DeleteMapping(value = { "/graph/{identification}" })
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<String> deleteNebulaGraph(@PathVariable("identification") String identification) {
		try {
			nebulaGraphBusinessService.deleteNebulaGraphEntity(identification, utils.getUserId());
		} catch (final Exception e) {
			return ResponseEntity.internalServerError().body(e.getMessage());
		}
		return new ResponseEntity<>(MSG_ONTOLOGY_CREATED_SUCCESS, HttpStatus.OK);
	}

	@Operation(summary = "Create new virtual ontology")
	@PostMapping(value = { "/virtual" })
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> createVirtualOntology(
			@Parameter(description = "OntologyCreate", required = true) @Valid @RequestBody OntologyVirtualDTO ontologyDTO,
			HttpServletRequest request) {

		final User user = userService.getUser(utils.getUserId());

		final Ontology ontology = ontologyDTOConverter.ontologyDTOToOntology(ontologyDTO, user);
		final OntologyConfiguration ontologyConfig = new OntologyConfiguration(request);

		try {
			ontologyBusinessService.createOntology(ontology, utils.getUserId(), ontologyConfig);
		} catch (final OntologyDataJsonProblemException jsonException) {
			return new ResponseEntity<>(jsonException.getMessage(), HttpStatus.BAD_REQUEST);
		} catch (final OntologyServiceException exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		} catch (final OntologyBusinessServiceException exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(MSG_ONTOLOGY_CREATED_SUCCESS, HttpStatus.OK);
	}

	@Operation(summary = "Create new timeseries ontology")
	@PostMapping(value = { "/timeseries" })
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> createTSOntology(@Valid @RequestBody OntologyTimeSeriesDTO ontologyTimeSeriesDTO) {

		ResponseEntity<?> response;

		try {
			final User user = userService.getUser(utils.getUserId());
			ontologyTimeSeriesDTO.setUserId(utils.getUserId());

			final OntologyTimeSeriesServiceDTO ontologyServiceDTO = ontologyDTOConverter
					.ontologyTimeSeriesDTOToOntologyTimeSeriesServiceDTO(ontologyTimeSeriesDTO, user);
			final OntologyConfiguration config = ontologyDTOConverter
					.ontologyTimeSeriesDTOToOntologyConfiguration(ontologyTimeSeriesDTO);
			// final Ontology ontology =
			// ontologyTimeSeriesService.createOntologyTimeSeries(ontologyServiceDTO,
			// config, false, false);
			final Ontology ontology = timeseriesBusinessService.createOntology(ontologyServiceDTO, config, false,
					false);

			if (ontology != null) {
				response = new ResponseEntity<>(MSG_ONTOLOGY_CREATED_SUCCESS, HttpStatus.OK);
			} else {
				throw new OntologyServiceException("Not possible to create Ontology Time Series",
						OntologyServiceException.Error.GENERIC_ERROR);
			}

		} catch (final OntologyServiceException exception) {
			log.error("Error creating ontology TimeSeries", exception);
			final OntologyResponseErrorDTO errorDTO = new OntologyResponseErrorDTO(exception);
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
		} catch (final OntologyDataJsonProblemException exception) {
			log.error("Error creating ontology TimeSeries", exception);
			response = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		} catch (final Exception exception) {
			log.error("Error creating ontology TimeSeries", exception);
			response = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return response;

	}

	private OntologyKPI createKPIinDB(OntologyKPI ontologyKPI) {

		OntologyKPI saved = null;

		try {
			saved = ontologyKpiCRUDService.save(ontologyKPI);
			ontologyKPIService.scheduleKpi(ontologyKPI);
		} catch (final Exception exception) {
			log.error("Error creating KPI in DB", exception);
			ontologyKpiCRUDService.remove(ontologyKPI);
			throw new OntologyServiceException("Not possible to create kpi in db",
					OntologyServiceException.Error.GENERIC_ERROR);
		}
		return saved;
	}

	private Ontology createOntologyKPI(OntologyKPIDTO ontologyKPIDTO, User user)
			throws IOException, OntologyBusinessServiceException {

		final String ontologyQueryIdentification = ontologyConfigService
				.getOntologyFromQuery(ontologyKPIDTO.getQuery());
		final Ontology ontologyQuery = ontologyService.getOntologyByIdentification(ontologyQueryIdentification,
				utils.getUserId());
		if (ontologyQuery == null) {
			throw new OntologyServiceException(USER_IS_NOT_AUTH, OntologyServiceException.Error.PERMISSION_DENIED);
		}

		final Ontology ontology = ontologyDTOConverter.ontologyKPIDTOToOntology(ontologyKPIDTO, user);
		ontology.setJsonSchema(ontologyBusinessService
				.completeSchema(ontologyKPIDTO.getSchema(), ontologyKPIDTO.getName(), ontologyKPIDTO.getDescription())
				.toString());
		final OntologyConfiguration config = ontologyDTOConverter.ontologyKpiDTOToOntologyConfiguration(ontologyKPIDTO);
		ontologyBusinessService.createOntology(ontology, user.getUserId(), config);
		return ontology;
	}

	@Operation(summary = "Get ontology kpi by identification")
	@GetMapping("kpi/{identification}")
	@ApiResponses(@ApiResponse(content = @Content(schema = @Schema(implementation = OntologyKpiDTO.class)), responseCode = "200", description = "OK"))
	public ResponseEntity<?> getkpi(
			@Parameter(description = "Ontology identification", required = true) @PathVariable("identification") String ontologyIdentification) {
		try {
			final Ontology ontology = ontologyService.getOntologyByIdentification(ontologyIdentification,
					utils.getUserId());
			if (ontology == null) {
				return new ResponseEntity<>(ONTOLOGY_STR + ontologyIdentification + NOT_EXIST, HttpStatus.BAD_REQUEST);
			} else {

				if (ontology.getOntologyKPI() != null) {
					final KpiDTO kpiDTO = new KpiDTO();
					kpiDTO.setCron(ontology.getOntologyKPI().getCron());
					if (ontology.getOntologyKPI().getDateFrom() != null) {
						kpiDTO.setDateFrom(ontology.getOntologyKPI().getDateFrom().toString());
					}
					if (ontology.getOntologyKPI().getDateTo() != null) {
						kpiDTO.setDateTo(ontology.getOntologyKPI().getDateTo().toString());
					}
					kpiDTO.setJobName(ontology.getOntologyKPI().getJobName());
					kpiDTO.setPostProcess(ontology.getOntologyKPI().getPostProcess());
					kpiDTO.setQuery(ontology.getOntologyKPI().getQuery());
					final OntologyKpiDTO ontologyKPI = new OntologyKpiDTO(ontology, kpiDTO);
					return new ResponseEntity<>(ontologyKPI, HttpStatus.OK);
				}
				final OntologyKpiDTO ontologyKPI = new OntologyKpiDTO(ontology);
				return new ResponseEntity<>(ontologyKPI, HttpStatus.OK);
			}

		} catch (final OntologyServiceException exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.UNAUTHORIZED);
		}
	}

	@Operation(summary = "Create new kpi ontology")
	@PostMapping(value = { "/kpi" })
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	@Transactional
	public ResponseEntity<?> createKPIOntology(@Valid @RequestBody OntologyKpiDTO ontologyDTO) {

		ResponseEntity<?> response;

		try {
			final User user = userService.getUser(utils.getUserId());
			final OntologyKPIDTO ontologyKPIDTO = ontologyDTOConverter.ontologyKpiDTOToOntologyKPIDTO(ontologyDTO);
			final Ontology ontology = createOntologyKPI(ontologyKPIDTO, user);
			final DataModel dataModel = findOrCreateDataModel(ontologyDTO);
			ontology.setDataModel(dataModel);
			final OntologyKPI ontologyKpi = ontologyDTOConverter.ontologyKPIDTOToOntologyKPI(ontologyKPIDTO, ontology,
					user);
			createKPIinDB(ontologyKpi);

			response = new ResponseEntity<>(MSG_ONTOLOGY_CREATED_SUCCESS, HttpStatus.OK);

		} catch (final OntologyServiceException exception) {
			log.error("Error creating Kpi ontology", exception);
			final OntologyResponseErrorDTO errorDTO = new OntologyResponseErrorDTO(exception);
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
		} catch (final OntologyDataJsonProblemException exception) {
			log.error("Error creating Kpi ontology", exception);
			response = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		} catch (final Exception exception) {
			log.error("Error creating Kpi ontology", exception);
			response = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return response;

	}

	@Operation(summary = "Update an existing ontology")
	@PutMapping
	public ResponseEntity<?> update(
			@Parameter(description = "OntologyUpdate", required = true) @Valid @RequestBody OntologyUpdate ontologyUpdate)
			throws OntologyDataUnauthorizedException, GenericOPException {
		final User user = userService.getUser(utils.getUserId());

		if (!userService.isUserAdministrator(user) && !userService.isUserDeveloper(user)) {
			return new ResponseEntity<>(USER_IS_NOT_AUTH, HttpStatus.UNAUTHORIZED);
		}
		try {
			final Ontology ontology = ontologyService.getOntologyByIdentification(ontologyUpdate.getIdentification(),
					utils.getUserId());
			if (ontology == null) {
				return new ResponseEntity<>(ONTOLOGY_STR + ontologyUpdate.getIdentification() + NOT_EXIST,
						HttpStatus.BAD_REQUEST);
			}
			final boolean hasDocuments = ontologyBusinessService.hasDocuments(ontology);

			if (ontologyUpdate.getDescription() != null) {
				ontology.setDescription(ontologyUpdate.getDescription());
			}
			if (ontologyUpdate.getMetainf() != null) {
				ontology.setMetainf(ontologyUpdate.getMetainf());
			}
			if (ontologyUpdate.getJsonSchema() != null) {
				ontology.setJsonSchema(ontologyUpdate.getJsonSchema());
			}
			if (ontologyUpdate.getActive() != null) {
				ontology.setActive(ontologyUpdate.getActive());
			}
			if (ontologyUpdate.getIsPublic() != null) {
				ontology.setPublic(ontologyUpdate.getIsPublic());
				;
			}
			if (ontologyUpdate.getAllowsCypherFields() != null) {
				ontology.setAllowsCypherFields(ontologyUpdate.getAllowsCypherFields());
			}
			if (ontologyUpdate.getAllowsCreateTopic() != null) {
				ontology.setAllowsCreateTopic(ontologyUpdate.getAllowsCreateTopic());
			}
			if (ontologyUpdate.getRtdbClean() != null) {
				ontology.setRtdbClean(ontologyUpdate.getRtdbClean());

			}
			if (ontologyUpdate.getRtdbCleanLapse() != null) {
				ontology.setRtdbCleanLapse(ontologyUpdate.getRtdbCleanLapse());
			}
			if (ontologyUpdate.getRtdbToHdb() != null) {
				ontology.setRtdbToHdb(ontologyUpdate.getRtdbToHdb());
			}
			if (ontologyUpdate.getRtdbToHdbStorage() != null) {
				ontology.setRtdbToHdbStorage(ontologyUpdate.getRtdbToHdbStorage());
			}
			if (ontologyUpdate.getAllowsCreateTopic() != null) {
				ontology.setAllowsCreateTopic(ontologyUpdate.getAllowsCreateTopic());
			}
			if (ontologyUpdate.getAllowsCreateNotificationTopic() != null) {
				ontology.setAllowsCreateNotificationTopic(ontologyUpdate.getAllowsCreateNotificationTopic());
			}
			if (ontologyUpdate.getContextDataEnabled() != null) {
				ontology.setContextDataEnabled(ontologyUpdate.getContextDataEnabled());
			}

			final OntologyConfiguration ontologyConfig = new OntologyConfiguration();
			try {
				ontologyService.updateOntology(ontology, utils.getUserId(), ontologyConfig, hasDocuments);
			} catch (OntologyDataJsonProblemException | OntologyServiceException exception) {
				return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
			}
		} catch (final OntologyServiceException exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.UNAUTHORIZED);
		}
		return new ResponseEntity<>("Ontology updated successfully", HttpStatus.OK);
	}

	@Operation(summary = "Clone an Ontology by ontology identification or id")
	@PostMapping(value = "/clone/{identification}")
	@Transactional
	public ResponseEntity<?> cloneOntology(
			@Parameter(description = "Ontology identification or id", required = true) @PathVariable("identification") String identification,
			@Parameter(description = "New Identification. In case it is a virtual/relational entity, this will be the prefix.") @RequestParam(required = true) String newIdentification) {
		try {
			final User user = userService.getUser(utils.getUserId());
			final Ontology ontology = ontologyService.getOntologyByIdOrIdentification(identification, user.getUserId());
			if (ontology == null) {
				return new ResponseEntity<>(ONTOLOGY_STR + identification + NOT_EXIST, HttpStatus.NOT_FOUND);
			}
			if (!ontologyService.hasUserPermissionForQuery(user, ontology)) {
				return new ResponseEntity<>(USER_IS_NOT_AUTH, HttpStatus.UNAUTHORIZED);
			}

			ontologyBusinessService.cloneOntology(ontology.getId(), newIdentification, user.getUserId(), null);

			if (ontology.getOntologyKPI() != null) {
				final Ontology cloneOntology = ontologyConfigService.getOntologyByIdentification(newIdentification);
				ontologyKPIService.cloneOntologyKpi(ontology, cloneOntology, user);
			}

			return new ResponseEntity<>(HttpStatus.OK);

		} catch (final OntologyServiceException exception) {
			log.error("Error cloning ontology", exception);
			if (exception.getError().equals(OntologyServiceException.Error.EXISTING_ONTOLOGY)) {
				return new ResponseEntity<>(exception.getMessage(), HttpStatus.CONFLICT);
			}
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.UNAUTHORIZED);
		} catch (final OntologyBusinessServiceException e) {
			log.error("Error cloning ontology", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}catch (final Exception e) {
			log.error("Error cloning ontology", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Get users access authorizations for an ontology by ontology identification")
	@GetMapping("/{identification}/authorizations")
	@ApiResponses(@ApiResponse(content = @Content(schema = @Schema(implementation = OntologySimplified.class)), responseCode = "200", description = "OK"))
	public ResponseEntity<?> getOntologyAuthorizations(
			@Parameter(description = "Ontology identification", required = true) @PathVariable("identification") String ontologyIdentification) {

		try {
			final Ontology ontology = ontologyService.getOntologyByIdentification(ontologyIdentification,
					utils.getUserId());
			if (ontology == null) {
				return new ResponseEntity<>(ONTOLOGY_STR + ontologyIdentification + NOT_EXIST, HttpStatus.BAD_REQUEST);
			}
			if (!ontologyService.hasOntologyUsersAuthorized(ontology.getId())) {
				return new ResponseEntity<>(ONTOLOGY_STR + ontologyIdentification + "\" does not have authorizations",
						HttpStatus.BAD_REQUEST);
			}

			final List<OntologyUserAccess> ontologyUserAccessList = ontologyService
					.getOntologyUserAccesses(ontology.getId(), utils.getUserId());
			final Set<OntologyUserAccessSimplified> ontologyUserAccessSimplifiedList = new TreeSet<>();
			ontologyUserAccessList
					.forEach(o -> ontologyUserAccessSimplifiedList.add(new OntologyUserAccessSimplified(o)));

			return new ResponseEntity<>(ontologyUserAccessSimplifiedList, HttpStatus.OK);

		} catch (final OntologyServiceException exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.UNAUTHORIZED);
		}
	}

	@Operation(summary = "Set user access authorizations for an ontology by ontology identification")
	@PostMapping("/{identification}/authorizations")
	public ResponseEntity<?> setOntologyAuthorizations(
			@Parameter(description = "Ontology identification", required = true) @PathVariable("identification") String ontologyIdentification,
			@Valid @RequestBody OntologyUserAccessSimplified ontologyUserAccessSimplified) {
		try {
			final Ontology ontology = ontologyService.getOntologyByIdentification(ontologyIdentification,
					utils.getUserId());
			if (ontology == null) {
				return new ResponseEntity<>(ONTOLOGY_STR + ontologyIdentification + NOT_EXIST, HttpStatus.BAD_REQUEST);
			}

			final User user = userService.getUser(ontologyUserAccessSimplified.getUserId());
			if (user == null) {
				return new ResponseEntity<>("User \"" + ontologyUserAccessSimplified.getUserId() + NOT_EXIST,
						HttpStatus.BAD_REQUEST);
			}
			try {
				final OntologyUserAccess ontologyUserAccess = ontologyService
						.getOntologyUserAccessByOntologyIdAndUserId(ontology.getId(),
								ontologyUserAccessSimplified.getUserId(), utils.getUserId());
				ontologyService.updateOntologyUserAccess(ontologyUserAccess.getId(),
						ontologyUserAccessSimplified.getTypeName(), utils.getUserId());
				return new ResponseEntity<>("Authorization updated successfully", HttpStatus.OK);
			} catch (final IllegalStateException e) {
				return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
			} catch (final OntologyServiceException e) {
				ontologyService.createUserAccess(ontology.getId(), user.getUserId(),
						ontologyUserAccessSimplified.getTypeName(), utils.getUserId());
				return new ResponseEntity<>("Authorization created successfully", HttpStatus.OK);
			}

		} catch (final OntologyServiceException exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@Operation(summary = "Delete user access authorization for an ontology by ontology identification and user id")
	@DeleteMapping("/{identification}/authorizations/{userId}")
	public ResponseEntity<String> deleteOntologyAuthorizations(
			@Parameter(description = "Ontology identification", required = true) @PathVariable("identification") String ontologyIdentification,
			@Parameter(description = "User id", required = true) @PathVariable("userId") String userId) {
		try {

			final Ontology ontology = ontologyService.getOntologyByIdentification(ontologyIdentification,
					utils.getUserId());
			if (ontology == null) {
				return new ResponseEntity<>(ONTOLOGY_STR + ontologyIdentification + NOT_EXIST, HttpStatus.BAD_REQUEST);
			}

			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>("User \"" + userId + NOT_EXIST, HttpStatus.BAD_REQUEST);
			}

			final OntologyUserAccess ontologyUserAccess = ontologyService
					.getOntologyUserAccessByOntologyIdAndUserId(ontology.getId(), userId, utils.getUserId());
			ontologyService.deleteOntologyUserAccess(ontologyUserAccess.getId(), utils.getUserId());

			return new ResponseEntity<>("Authorization deleted successfully", HttpStatus.OK);

		} catch (final OntologyServiceException exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.UNAUTHORIZED);
		}
	}

	@Operation(summary = "KPI Execution")
	@GetMapping("/{identification}/executeKPI")
	public ResponseEntity<String> kpiExecution(
			@Parameter(description = "Ontology identification", required = true) @PathVariable("identification") String ontologyIdentification) {

		try {
			final Ontology ontology = ontologyService.getOntologyByIdentification(ontologyIdentification,
					utils.getUserId());
			if (ontology == null) {
				return new ResponseEntity<>(ONTOLOGY_STR + ontologyIdentification + NOT_EXIST, HttpStatus.BAD_REQUEST);
			} else {

				if (ontology.getOntologyKPI() != null) {
					final Map<String, String> result = ontologyService.executeKPI(utils.getUserId(),
							ontology.getOntologyKPI().getQuery(), ontology.getIdentification(),
							ontology.getOntologyKPI().getPostProcess());

					if (result != null && result.get(STATUS_STR).equals(ERROR_STR)) {
						return new ResponseEntity<>(utils.getMessage(result.get(MESSAGE_STR), ""),
								HttpStatus.INTERNAL_SERVER_ERROR);
					}

					return new ResponseEntity<>("KPI successfully executed", HttpStatus.OK);

				} else {
					return new ResponseEntity<>("Not KPI ontology", HttpStatus.BAD_REQUEST);
				}
			}
		} catch (final Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.UNAUTHORIZED);
		}
	}

	@Operation(summary = "KPI Execution with parameters")
	@PostMapping("/{identification}/executeKPI")
	public ResponseEntity<String> kpiParamExecution(
			@Parameter(description = "Ontology identification", required = true) @PathVariable("identification") String ontologyIdentification,
			@Valid @RequestBody String kpiParameters) {

		final ObjectMapper mapper = new ObjectMapper();
		JsonNode parameters = null;
		try {
			parameters = mapper.readTree(kpiParameters);
		} catch (final IOException e) {
			return new ResponseEntity<>("Error parsing KPI ontology's parameters", HttpStatus.BAD_REQUEST);
		}
		try {
			final Ontology ontology = ontologyService.getOntologyByIdentification(ontologyIdentification,
					utils.getUserId());
			if (ontology == null) {
				return new ResponseEntity<>(ONTOLOGY_STR + ontologyIdentification + NOT_EXIST, HttpStatus.BAD_REQUEST);
			} else {
				if (ontology.getOntologyKPI() != null) {

					String query = ontology.getOntologyKPI().getQuery();
					final Iterator<Entry<String, JsonNode>> paramIter = parameters.fields();
					while (paramIter.hasNext()) {
						final Entry<String, JsonNode> param = paramIter.next();
						query = query.replace("{$" + param.getKey() + "}", param.getValue().asText());
					}
					final Map<String, String> result = ontologyService.executeKPI(utils.getUserId(), query,
							ontology.getIdentification(), ontology.getOntologyKPI().getPostProcess());
					if (result != null && result.get(STATUS_STR).equals(ERROR_STR)) {
						return new ResponseEntity<>(utils.getMessage(result.get(MESSAGE_STR), ""),
								HttpStatus.INTERNAL_SERVER_ERROR);
					}

					return new ResponseEntity<>("KPI successfully executed", HttpStatus.OK);

				} else {
					return new ResponseEntity<>("Not KPI ontology", HttpStatus.BAD_REQUEST);
				}
			}
		} catch (final Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.UNAUTHORIZED);
		}
	}

	@Operation(summary = "Update rest ontology")
	@PutMapping(value = { "/rest" })
	public ResponseEntity<?> update(
			@Parameter(description = "Ontology", required = true) @Valid @RequestBody OntologyRestDTO ontologyDTO) {
		final User user = userService.getUser(utils.getUserId());

		if (!userService.isUserAdministrator(user) && !userService.isUserDeveloper(user)) {
			return new ResponseEntity<>(USER_IS_NOT_AUTH, HttpStatus.UNAUTHORIZED);
		}

		final Ontology ontologyDb = ontologyConfigService.getOntologyByIdentification(ontologyDTO.getIdentification());
		if (ontologyDb == null) {
			return new ResponseEntity<>("Ontology not found", HttpStatus.BAD_REQUEST);
		}
		final Ontology ontology = ontologyDTOConverter.ontologyDTOToOntology(ontologyDTO, user);
		ontology.setId(ontologyDb.getId());
		ontology.setDataModel(ontologyDb.getDataModel());
		final OntologyConfiguration ontologyConfig = ontologyDTOConverter
				.ontologyRestDTOToOntologyConfiguration(ontologyDTO);
		try {
			ontologyConfigService.updateOntology(ontology, utils.getUserId(), ontologyConfig);
		} catch (final OntologyDataJsonProblemException jsonException) {
			return new ResponseEntity<>(jsonException.getMessage(), HttpStatus.BAD_REQUEST);
		} catch (final OntologyServiceException exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(MSG_ONTOLOGY_UPDATED_SUCCESS, HttpStatus.OK);
	}

	@Operation(summary = "Update time series ontology")
	@PutMapping(value = { "/timeseries" })
	public ResponseEntity<?> update(
			@Parameter(description = "Ontology", required = true) @Valid @RequestBody OntologyTimeSeriesDTO ontologyDTO) {
		final User user = userService.getUser(utils.getUserId());

		if (!userService.isUserAdministrator(user) && !userService.isUserDeveloper(user)) {
			return new ResponseEntity<>(USER_IS_NOT_AUTH, HttpStatus.UNAUTHORIZED);
		}

		final Ontology ontologyDb = ontologyConfigService.getOntologyByIdentification(ontologyDTO.getIdentification());
		if (ontologyDb == null) {
			return new ResponseEntity<>("Ontology not found", HttpStatus.BAD_REQUEST);
		}
		final Ontology ontology = ontologyDTOConverter.ontologyDTOToOntology(ontologyDTO, user);
		ontology.setId(ontologyDb.getId());
		ontology.setDataModel(ontologyDb.getDataModel());
		final OntologyConfiguration ontologyConfig = ontologyDTOConverter
				.ontologyTimeSeriesDTOToOntologyConfiguration(ontologyDTO);

		final OntologyTimeSeries ontologyTS = ontologyTimeSeriesService.getOntologyByOntology(ontologyDb);
		final OntologyTimeSeriesServiceDTO ontologyTSDTO = ontologyDTOConverter
				.ontologyTimeSeriesDTOToOntologyTimeSeriesServiceDTO(ontologyDTO, user);
		ontologyTSDTO.setId(ontologyTS.getId());
		try {
			ontologyTimeSeriesService.updateOntologyTimeSeries(ontologyTSDTO, utils.getUserId(), ontologyConfig, false,
					false);
		} catch (final OntologyDataJsonProblemException jsonException) {
			return new ResponseEntity<>(jsonException.getMessage(), HttpStatus.BAD_REQUEST);
		} catch (final OntologyServiceException exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(MSG_ONTOLOGY_UPDATED_SUCCESS, HttpStatus.OK);
	}

	@Operation(summary = "Update virtual ontology")
	@PutMapping(value = { "/virtual" })
	public ResponseEntity<?> update(
			@Parameter(description = "Ontology", required = true) @Valid @RequestBody OntologyVirtualDTO ontologyDTO) {
		final User user = userService.getUser(utils.getUserId());

		if (!userService.isUserAdministrator(user) && !userService.isUserDeveloper(user)) {
			return new ResponseEntity<>(USER_IS_NOT_AUTH, HttpStatus.UNAUTHORIZED);
		}

		final Ontology ontologyDb = ontologyConfigService.getOntologyByIdentification(ontologyDTO.getIdentification());
		if (ontologyDb == null) {
			return new ResponseEntity<>("Ontology not found", HttpStatus.BAD_REQUEST);
		}
		final Ontology ontology = ontologyDTOConverter.ontologyDTOToOntology(ontologyDTO, user);
		ontology.setId(ontologyDb.getId());
		ontology.setDataModel(ontologyDb.getDataModel());
		final OntologyConfiguration ontologyConfig = ontologyDTOConverter
				.ontologyVirtualDTOToOntologyConfiguration(ontologyDTO, ontologyDb.getRtdbDatasource().name());
		try {
			ontologyConfigService.updateOntology(ontology, utils.getUserId(), ontologyConfig);
		} catch (final OntologyDataJsonProblemException jsonException) {
			return new ResponseEntity<>(jsonException.getMessage(), HttpStatus.BAD_REQUEST);
		} catch (final OntologyServiceException exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(MSG_ONTOLOGY_UPDATED_SUCCESS, HttpStatus.OK);
	}

	@Operation(summary = "Update kpi ontology")
	@PutMapping(value = { "/kpi" })
	public ResponseEntity<?> update(
			@Parameter(description = "Ontology", required = true) @Valid @RequestBody OntologyKpiDTO ontologyDTO) {
		final User user = userService.getUser(utils.getUserId());

		if (!userService.isUserAdministrator(user) && !userService.isUserDeveloper(user)) {
			return new ResponseEntity<>(USER_IS_NOT_AUTH, HttpStatus.UNAUTHORIZED);
		}

		final Ontology ontologyDb = ontologyService.getOntologyByIdentification(ontologyDTO.getIdentification(), utils.getUserId());
		if (ontologyDb == null) {
			return new ResponseEntity<>("Ontology not found", HttpStatus.BAD_REQUEST);
		}

		if (ontologyDb.getOntologyKPI() == null || ontologyDb.getOntologyKPI().getId() == null) {
			return new ResponseEntity<>("Ontology is not kpi type", HttpStatus.BAD_REQUEST);
		}

		try {

			final Ontology ontology = ontologyDTOConverter.ontologyDTOToOntology(ontologyDTO, user);
			ontology.setId(ontologyDb.getId());
			ontology.setDataModel(ontologyDb.getDataModel());
			final OntologyKPIDTO ontologyKPIDTO = ontologyDTOConverter.ontologyKpiDTOToOntologyKPIDTO(ontologyDTO);

			final OntologyConfiguration ontologyConfig = ontologyDTOConverter
					.ontologyKpiDTOToOntologyConfiguration(ontologyKPIDTO);
			final OntologyKPI kpi = ontologyDb.getOntologyKPI();

			kpi.setCron(ontologyKPIDTO.getCron());
			kpi.setJobName(ontologyKPIDTO.getJobName());
			kpi.setPostProcess(ontologyKPIDTO.getPostProcess());
			kpi.setQuery(ontologyKPIDTO.getQuery());
			kpi.setOntology(ontology);

			ontology.setOntologyKPI(kpi);

			if (ontology.getOntologyKPI() != null && ontology.getOntologyKPI().getId() == null) {
				ontology.setOntologyKPI(null);
			}

			final boolean hasDocuments = ontologyBusinessService.hasDocuments(ontology);

			ontologyBusinessService.updateOntology(ontology, ontologyConfig, hasDocuments);

			ontologyConfigService.updateOntology(ontology, utils.getUserId(), ontologyConfig, hasDocuments);
			ontologyKpiCRUDService.save(kpi);
			if (ontologyDb.getOntologyKPI() != null && ontologyDb.getOntologyKPI().getId() != null
					&& ontology.getOntologyKPI().isActive()) {

				ontologyKPIService.unscheduleKpi(ontology.getOntologyKPI());
				ontologyKPIService.scheduleKpi(ontology.getOntologyKPI());

			}

		} catch (final OntologyDataJsonProblemException jsonException) {
			return new ResponseEntity<>(jsonException.getMessage(), HttpStatus.BAD_REQUEST);
		} catch (final OntologyServiceException exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		} catch (final ParseException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		} catch (final OntologyBusinessServiceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		} catch (final DBPersistenceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(MSG_ONTOLOGY_UPDATED_SUCCESS, HttpStatus.OK);
	}

	@Operation(summary = "Get Statistics")
	@GetMapping("/{identification}/statistics")
	public ResponseEntity<?> getStatistics(
			@Parameter(description = "Ontology identification", required = true) @PathVariable("identification") String identification) {

		try {
			if (ontologyConfigService.hasUserPermissionForQuery(utils.getUserId(), identification)) {

				String queryResult = queryToolService.querySQLAsJson(utils.getUserId(), identification,
						"select * from " + identification + " limit 1000", 0);
				Table table = new JsonReader().read(Source.fromString(queryResult));

				List<OntologyStatisticsDTO> statistics = generateStatistics(queryResult, table);
				return new ResponseEntity<>(statistics, HttpStatus.OK);
			} else {
				log.error("You don't have permissions for this ontology");
				return new ResponseEntity<>("You don't have permissions for this ontology", HttpStatus.UNAUTHORIZED);
			}
		} catch (final Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.UNAUTHORIZED);
		}
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

	private JsonObject importAuthorizations(OntologyDTO ontologyDTO, Ontology ontology) {
		final JsonObject importingAuthJsonErrors = new JsonObject();
		for (final OntologyUserAccessSimplified userac : ontologyDTO.getAuthorizations()) {
			try {
				ontologyService.createUserAccess(ontology.getId(), userac.getUserId(), userac.getTypeName(),
						utils.getUserId());
				importingAuthJsonErrors.addProperty(userac.getUserId(), OK_STR);
			} catch (final Exception e) {
				importingAuthJsonErrors.addProperty(userac.getUserId(), ERROR_STR);
			}
		}
		return importingAuthJsonErrors;
	}

	private Ontology importFromOntologyElasticDTO(OntologyElasticDTO ontologyDTO, DataModel datamodel)
			throws OntologyBusinessServiceException {
		final User user = getImportingUser(ontologyDTO);
		final Ontology ontology = ontologyDTOConverter.ontologyDTOToOntology(ontologyDTO, user);
		ontology.setDataModel(datamodel);
		final OntologyConfiguration ontologyConfig = new OntologyConfiguration();
		ontologyConfig.setShards(String.valueOf(ontologyDTO.getShards()));
		ontologyConfig.setReplicas(String.valueOf(ontologyDTO.getReplicas()));
		ontologyConfig.setAllowsCustomElasticConfig(ontologyDTO.isCustomConfig());

		ontologyConfig.setAllowsTemplateConfig(
				ontologyDTO.getPatternField() != null && !ontologyDTO.getPatternField().isEmpty());
		ontologyConfig.setPatternField(ontologyDTO.getPatternField());
		ontologyConfig.setPatternFunction(ontologyDTO.getPatternFunction());
		ontologyConfig.setSubstringStart(String.valueOf(ontologyDTO.getSubstringStart()));
		ontologyConfig.setSubstringEnd(String.valueOf(ontologyDTO.getSubstringEnd()));
		ontologyBusinessService.createOntology(ontology, user.getUserId(), ontologyConfig);

		return ontology;
	}

	private Ontology importFromOntologyDTO(OntologyDTO ontologyDTO, DataModel datamodel)
			throws OntologyBusinessServiceException {
		final User user = getImportingUser(ontologyDTO);
		final Ontology ontology = ontologyDTOConverter.ontologyDTOToOntology(ontologyDTO, user);
		ontology.setDataModel(datamodel);
		final OntologyConfiguration ontologyConfig = new OntologyConfiguration();
		ontologyBusinessService.createOntology(ontology, user.getUserId(), ontologyConfig);

		return ontology;
	}

	private Ontology importFromOntologyTimeSeriesDTO(OntologyTimeSeriesDTO ontologyDTO) throws TimeSerieOntologyBusinessServiceException, JsonProcessingException {
		final User user = getImportingUser(ontologyDTO);
		ontologyDTO.setUserId(user.getUserId());
		final OntologyTimeSeriesServiceDTO ontologyServiceDTO = ontologyDTOConverter
				.ontologyTimeSeriesDTOToOntologyTimeSeriesServiceDTO(ontologyDTO, user);
		final OntologyConfiguration config = ontologyDTOConverter
				.ontologyTimeSeriesDTOToOntologyConfiguration(ontologyDTO);
		// return ontologyTimeSeriesService.createOntologyTimeSeries(ontologyServiceDTO,
		// config, false, false);
		return timeseriesBusinessService.createOntology(ontologyServiceDTO, config, false, false);
	}

	private Ontology importFromOntologyKpisDTO(OntologyKpiDTO ontologyDTO, DataModel dataModel)
			throws IOException, OntologyBusinessServiceException {
		try {
			final User user = getImportingUser(ontologyDTO);
			Ontology ontology = null;

			final OntologyKPIDTO ontologyKPIDTO = ontologyDTOConverter.ontologyKpiDTOToOntologyKPIDTO(ontologyDTO);
			ontology = createOntologyKPI(ontologyKPIDTO, user);
			final OntologyKPI ontologyKpi = ontologyDTOConverter.ontologyKPIDTOToOntologyKPI(ontologyKPIDTO, ontology,
					user);
			createKPIinDB(ontologyKpi);

			return ontology;
		} catch (final ParseException e) {
			log.error("Error parsing date arguments in importFromOntologyKpiDTO");
			throw new OntologyServiceException(ERROR_INVALID_DATA_FORMAT);
		}
	}

	private Ontology importFromOntologyRestDTO(OntologyRestDTO ontologyDTO, DataModel dataModel)
			throws OntologyBusinessServiceException {
		final User user = getImportingUser(ontologyDTO);
		final Ontology ontology = ontologyDTOConverter.ontologyDTOToOntology(ontologyDTO, user);
		ontology.setDataModel(dataModel);
		final OntologyConfiguration ontologyConfig = ontologyDTOConverter
				.ontologyRestDTOToOntologyConfiguration(ontologyDTO);
		ontologyBusinessService.createOntology(ontology, user.getUserId(), ontologyConfig);

		return ontology;
	}

	private Ontology importFromOntologyVirtualDTO(OntologyVirtualDTO ontologyDTO, DataModel dataModel)
			throws OntologyBusinessServiceException {
		final User user = getImportingUser(ontologyDTO);
		final Ontology ontology = ontologyDTOConverter.ontologyDTOToOntology(ontologyDTO, user);
		ontology.setDataModel(dataModel);

		OntologyVirtualDatasource datasource = null;
		datasource = ontologyService.getOntologyVirtualDatasourceByName(ontologyDTO.getDatasource().getName());
		if (datasource == null) { // use existent or create if not exists
			datasource = ontologyDTOConverter
					.ontologyVirtualDatasourceDTOToOntologyVirtualDataSource(ontologyDTO.getDatasource(), user);
			try {
				virtualDatasourceService.createDatasource(datasource);
			} catch (final GenericOPException e) {
				throw new OntologyServiceException(ERROR_DATASOURCE_DOMAIN,
						OntologyServiceException.Error.GENERIC_ERROR);
			}
		}
		final OntologyConfiguration ontologyConfig = ontologyDTOConverter
				.ontologyVirtualDTOToOntologyConfiguration(ontologyDTO, datasource.getId());

		ontologyBusinessService.createOntology(ontology, user.getUserId(), ontologyConfig);

		return ontology;
	}

	private User getImportingUser(OntologyDTO ontologyDTO) {
		User user = userService.getUser(utils.getUserId());
		if (userService.isUserAdministrator(user) && ontologyDTO.getUserId() != null) {
			final User userDTO = userService.getUser(ontologyDTO.getUserId());
			if (userDTO != null) {
				user = userDTO;
			}
		}

		return user;
	}

	private DataModel findOrCreateDataModel(OntologyDTO ontologyDTO) {
		DataModel datamodel = new DataModel();
		datamodel.setIdentification(ontologyDTO.getDataModelIdentification());
		// Si no existe, crearlo, porque sino, el que viene no tiene id
		if (ontologyDTO.getDataModelIdentification() != null && !ontologyDTO.getDataModelIdentification().equals("")) {
			if (!dataModelService.dataModelExists(datamodel) && ontologyDTO.getDataModel() != null) {
				final DataModel newDataModel = ontologyDTOConverter.datamodelDTOToDataModel(ontologyDTO.getDataModel());
				datamodel = dataModelService.createDataModel(newDataModel);
			} else {
				datamodel = dataModelService.getDataModelByName(ontologyDTO.getDataModelIdentification());
			}
		} else {
			throw new OntologyServiceException(ERROR_MISSING_DATAMODEL,
					OntologyServiceException.Error.MISSING_DATA_MODEL);
		}

		return datamodel;
	}

	private OntologySimplifiedResponseDTO createImportingResponseDTO(Ontology ontology, JsonObject messageJson) {
		final OntologySimplifiedResponseDTO importedOntologyDTO = new OntologySimplifiedResponseDTO(ontology);
		importedOntologyDTO.setAuthorizations(ontologyDTOConverter
				.ontologyUserAccessesToOntologyUserAccessSimplified(ontology.getOntologyUserAccesses()));
		importedOntologyDTO.setMsg(messageJson.toString());
		return importedOntologyDTO;
	}

	private void raiseExceptionIfNotAllowedImport(User user, String ontologyDTOString) {
		if (!userService.isUserAdministrator(user) && !userService.isUserDeveloper(user)) {
			throw new OntologyServiceException(USER_IS_NOT_AUTH, OntologyServiceException.Error.PERMISSION_DENIED);
		}

		if (!ontologyDTOConverter.canConvert(ontologyDTOString, OntologyDTO.class)) {
			throw new OntologyServiceException(ERROR_INVALID_DATA_FORMAT);
		}
	}

	private OntologySimplifiedResponseDTO importFromAnyOntologyDTO(String identification, boolean importAuthorizations,
			boolean overwrite, String ontologyDTOString) throws Exception {
		try {
			final User user = userService.getUser(utils.getUserId());
			raiseExceptionIfNotAllowedImport(user, ontologyDTOString);

			final OntologyDTO ontologyMasterDTO = ontologyDTOConverter.jsonStringToOntologyDTO(ontologyDTOString);
			ontologyMasterDTO.setIdentification(identification);
			Ontology ontology = ontologyService.getOntologyByIdentification(identification, utils.getUserId());

			if (ontology != null) {
				if (overwrite) {
					if (userService.isUserAdministrator(user)
							|| user.getUserId().equals(ontology.getUser().getUserId())) {
						if (ontologyDTOConverter.canConvert(ontologyMasterDTO, OntologyKpiDTO.class)
								&& ontologyService.existsOntology(ontologyMasterDTO.getIdentification())) {
							throw new OntologyServiceException(USER_IS_NOT_AUTH,
									OntologyServiceException.Error.PERMISSION_DENIED);
						} else {
							ontologyBusinessService.deleteOntology(ontology.getId(), utils.getUserId());
						}
					} else {
						throw new OntologyServiceException(USER_IS_NOT_AUTH,
								OntologyServiceException.Error.PERMISSION_DENIED);
					}
				} else {
					throw new OntologyServiceException(ONTOLOGY_STR + identification + ALREADY_EXISTS,
							OntologyServiceException.Error.EXISTING_ONTOLOGY);
				}
			}

			final DataModel datamodel = findOrCreateDataModel(ontologyMasterDTO);

			// importing different logic ------------------------------------------------->
			if (ontologyDTOConverter.canConvert(ontologyMasterDTO, OntologyTimeSeriesDTO.class)) {
				ontology = importFromOntologyTimeSeriesDTO(
						ontologyDTOConverter.getMapper().convertValue(ontologyMasterDTO, OntologyTimeSeriesDTO.class));
			} else if (ontologyDTOConverter.canConvert(ontologyMasterDTO, OntologyKpiDTO.class)) {
				ontology = importFromOntologyKpisDTO(
						ontologyDTOConverter.getMapper().convertValue(ontologyMasterDTO, OntologyKpiDTO.class),
						datamodel);
			} else if (ontologyDTOConverter.canConvert(ontologyMasterDTO, OntologyRestDTO.class)) {
				ontology = importFromOntologyRestDTO(
						ontologyDTOConverter.getMapper().convertValue(ontologyMasterDTO, OntologyRestDTO.class),
						datamodel);
			} else if (ontologyDTOConverter.canConvert(ontologyMasterDTO, OntologyVirtualDTO.class)) {
				ontology = importFromOntologyVirtualDTO(
						ontologyDTOConverter.getMapper().convertValue(ontologyMasterDTO, OntologyVirtualDTO.class),
						datamodel);
			} else if (ontologyDTOConverter.canConvert(ontologyMasterDTO, OntologyElasticDTO.class)) {
				ontology = importFromOntologyElasticDTO(
						ontologyDTOConverter.getMapper().convertValue(ontologyMasterDTO, OntologyElasticDTO.class),
						datamodel);
			} else {
				ontology = importFromOntologyDTO(ontologyMasterDTO, datamodel);
			}
			// <------------------------------------------------- importing different logic

			final JsonObject messageJson = new JsonObject();
			if (importAuthorizations && !ontologyMasterDTO.getAuthorizations().isEmpty()) {
				final JsonObject importingAuthJsonErrors = importAuthorizations(ontologyMasterDTO, ontology);
				messageJson.add("authorizations", importingAuthJsonErrors);
			}
			return createImportingResponseDTO(ontology, messageJson);
		} catch (final Exception exception) {
			log.error("Not possible to import ontology: {}", exception.getMessage());
			throw exception;
		}
	}

	@Operation(summary = "Import ontology")
	@PostMapping(value = "/import/{identification}")
	@Transactional
	public ResponseEntity<?> importOntology(
			@Parameter(description = "Ontology identification", required = true) @PathVariable("identification") String identification,
			@Parameter(description = "Import authorizations if exist") @RequestParam(required = false, defaultValue = "false") boolean importAuthorizations,
			@Parameter(description = "Overwrite ontology if exist") @RequestParam(required = false, defaultValue = "false") boolean overwrite,
			@RequestBody String ontologyDTOString) {

		ResponseEntity<?> response;

		try {
			final OntologySimplifiedResponseDTO importedOntologyDTO = importFromAnyOntologyDTO(identification,
					importAuthorizations, overwrite, ontologyDTOString);
			log.info("Imported ontology successfully: {}", identification);
			response = new ResponseEntity<>(importedOntologyDTO, HttpStatus.OK);

		} catch (final OntologyServiceException exception) {
			log.error("Error importing ontology: {}", exception.getMessage());
			final OntologyResponseErrorDTO errorDTO = new OntologyResponseErrorDTO(exception);
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
		} catch (final Exception exception) {
			log.error("Error importing ontology: {}", exception.getMessage());
			response = new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	@Operation(summary = "Import ontologies")
	@PostMapping(value = "/import")
	@Transactional
	public ResponseEntity<?> importOntologies(
			@Parameter(description = "Import authorizations if exist") @RequestParam(required = false, defaultValue = "false") boolean importAuthorizations,
			@Parameter(description = "Overwrite ontology if exist") @RequestParam(required = false, defaultValue = "false") boolean overwrite,
			@RequestBody String ontologyDTOsArrayString) {

		try {
			final JsonArray jsonResponse = new JsonArray();
			final JsonArray ontologiesDTOs = new JsonParser().parse(ontologyDTOsArrayString).getAsJsonArray();
			for (final JsonElement jsonElement : ontologiesDTOs) {
				final JsonObject ontologyDTOJson = jsonElement.getAsJsonObject();
				final JsonObject jsonObjectResponse = tryImportOntology(ontologyDTOJson, importAuthorizations,
						overwrite);
				jsonResponse.add(jsonObjectResponse);
			}
			return new ResponseEntity<>(jsonResponse.toString(), HttpStatus.OK);
		} catch (final Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public JsonObject tryImportOntology(JsonObject ontologyDTOJson, boolean importAuthorizations, boolean overwrite) {
		String identification = "";
		String msg = "";
		String status = "";
		if (!ontologyDTOJson.has(IDENTIFICATION_STR)) {
			msg = ERROR_NOT_IDENTIFICATION_PROVIDED;
		} else {
			try {
				identification = ontologyDTOJson.get(IDENTIFICATION_STR).getAsString();
				final String ontologyDTOString = ontologyDTOJson.toString();

				importFromAnyOntologyDTO(identification, importAuthorizations, overwrite, ontologyDTOString);
				msg = ONTOLOGY_STR + identification + "\" successfully imported";
				status = OK_STR;
				log.info("Imported ontology successfully: {}", identification);
			} catch (final Exception e) {
				msg = e.getMessage();
				status = ERROR_STR;
				log.warn("Not possible to import one ontology on multiple importing: {} - {}", e.getMessage(),
						ontologyDTOJson.toString());
			}
		}
		return constructJsonResponse(identification, status, msg);
	}

	private JsonObject constructJsonResponse(String identification, String status, String msg) {
		final JsonObject jsonObjectResponse = new JsonObject();
		jsonObjectResponse.addProperty(IDENTIFICATION_STR, identification);
		jsonObjectResponse.addProperty(STATUS_STR, status);
		jsonObjectResponse.addProperty(MESSAGE_STR, msg);
		return jsonObjectResponse;
	}

	public HttpHeaders exportHeaders(String ontologyNameFile) {
		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType("application/octet-stream"));
		headers.set("Content-Disposition", "attachment; filename=\"" + ontologyNameFile + ".json\"");
		return headers;
	}

	private OntologyDTO createExportingDTO(Ontology ontology) {
		OntologyDTO ontologyDTO = null;

		final List<OntologyKPI> ontologyKpis = ontologyService.getOntologyKpisByOntology(ontology); // TODO: Bug,
		// ontology_id not
		// UK constraint in
		// db
		final OntologyRest ontologyRest = ontologyRestService
				.getOntologyRestByIdentification(ontology.getIdentification());
		final OntologyTimeSeries ontologyTimeSeries = ontologyTimeSeriesService.getOntologyByOntology(ontology);
		final OntologyVirtual ontologyVirtual = ontologyService.getOntologyVirtualByOntologyId(ontology);
		final OntologyElastic ontologyElastic = ontologyService.getOntologyElasticByOntologyId(ontology);
		if (!ontologyKpis.isEmpty()) {
			final OntologyKPI ontologyKPI = ontologyKpis.get(0); // TODO: Bug, ontology_id not UK constraint in db, so
			// list.get(0)
			final KpiDTO kpiDTO = new KpiDTO(ontologyKPI);
			ontologyDTO = new OntologyKpiDTO(ontology, kpiDTO);
		} else if (ontologyRest != null) {
			ontologyDTO = new OntologyRestDTO(ontologyRest);
		} else if (ontologyTimeSeries != null) {
			ontologyDTO = new OntologyTimeSeriesDTO(ontologyTimeSeries);
		} else if (ontologyVirtual != null) {
			ontologyDTO = new OntologyVirtualDTO(ontologyVirtual);
		} else if (ontologyElastic != null) {
			ontologyDTO = new OntologyElasticDTO(ontologyElastic);
		} else {
			ontologyDTO = new OntologyDTO(ontology);
		}
		// data model
		if (ontology.getDataModel() != null) {
			ontologyDTO.setDataModelIdentification(ontology.getDataModel().getIdentification());
		}
		// authorizations
		if (utils.isAdministrator() || utils.getUserId().equals(ontology.getUser().getUserId())) {
			ontologyDTO.setAuthorizations(ontologyDTOConverter
					.ontologyUserAccessesToOntologyUserAccessSimplified(ontology.getOntologyUserAccesses()));
		}

		return ontologyDTO;
	}

	private OntologyDTO exportOntology(String ontologyIdentification) {
		final Ontology ontology = ontologyService.getOntologyByIdentification(ontologyIdentification,
				utils.getUserId());

		if (ontology == null) {
			throw new OntologyServiceException(ONTOLOGY_STR + ontologyIdentification + NOT_EXIST,
					OntologyServiceException.Error.NOT_FOUND);
		}
		if (!ontologyConfigService.hasUserPermissionForQuery(utils.getUserId(), ontology)) {
			throw new OntologyServiceException(USER_IS_NOT_AUTH, OntologyServiceException.Error.PERMISSION_DENIED);
		}
		final OntologyDTO ontologyExportDTO = createExportingDTO(ontology);
		log.info("Exported ontology succesfully: {}", ontologyIdentification);
		return ontologyExportDTO;
	}

	@Operation(summary = "Export ontology")
	@GetMapping("/export/{identification}")
	public ResponseEntity<?> exportOntologyByIdentification(
			@Parameter(description = "Ontology identification", required = true) @PathVariable("identification") String ontologyIdentification) {

		ResponseEntity<?> response;

		try {
			final OntologyDTO ontologyDTO = exportOntology(ontologyIdentification);
			final HttpHeaders headers = exportHeaders(ontologyIdentification.trim());

			final byte[] payload = new ObjectMapper().writeValueAsBytes(ontologyDTO);
			final ByteArrayResource resource = new ByteArrayResource(payload);
			headers.set(HttpHeaders.CONTENT_LENGTH, String.valueOf(payload.length));
			response = new ResponseEntity<>(resource, headers, HttpStatus.OK);

		} catch (final OntologyServiceException exception) {
			log.error("Error exporting ontology: {}", exception.getMessage());
			final OntologyResponseErrorDTO errorDTO = new OntologyResponseErrorDTO(exception);
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
		} catch (final Exception exception) {
			log.error("Error exporting ontology: {}", exception.getMessage());
			response = new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	@Operation(summary = "Export ontologies")
	@GetMapping("/export/")
	public ResponseEntity<?> exportOntologyByIdentification() {

		ResponseEntity<?> response;

		try {
			final List<OntologyDTO> exportedOntologies = new ArrayList<>();
			final List<Ontology> ontologiesWithAuth = ontologyService.getOntologiesByOwner(utils.getUserId());
			for (final Ontology ontology : ontologiesWithAuth) {
				final OntologyDTO ontologyDTO = exportOntology(ontology.getIdentification());
				exportedOntologies.add(ontologyDTO);
			}

			Collections.sort(exportedOntologies, new Comparator<OntologyDTO>() {
				@Override
				public final int compare(final OntologyDTO s1, final OntologyDTO s2) {
					return s1.getCreatedAt().compareToIgnoreCase(s2.getCreatedAt());
				}
			});

			final HttpHeaders headers = exportHeaders(utils.getUserId() + "_ontologies");
			final ObjectMapper mapper = new ObjectMapper();
			final String responseString = mapper.writerFor(new TypeReference<List<OntologyDTO>>() {
			}).writeValueAsString(exportedOntologies);

			response = new ResponseEntity<>(responseString, headers, HttpStatus.OK);

		} catch (final OntologyServiceException exception) {
			final OntologyResponseErrorDTO errorDTO = new OntologyResponseErrorDTO(exception);
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
		} catch (final Exception exception) {
			response = new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	@Autowired
	private JsonToolUtils jsonToolUtils;
	@Autowired
	private OntologyDataService ontologyDataService;

	@Operation(summary = "Create ontology or updates existing one from JSON Schema")
	@PostMapping("/{identification}/create-update")
	public ResponseEntity<String> createOrUpdateOntology(@RequestBody String schema,
			@PathVariable("identification") String identification, boolean contextdata,
			@RequestParam(required = false, name = "repository") RtdbDatasource rtdbDatasource) {
		if (ontologyConfigService.existsOntology(identification)) {
			try {
				final Ontology ontology = ontologyService.getOntologyByIdentification(identification);
				ontology.setJsonSchema(jsonToolUtils.completeSchema(schema, identification, identification).toString());
				final boolean hasDocuments = ontologyBusinessService.hasDocuments(ontology);;
				ontologyService.updateOntology(ontology, utils.getUserId(), new OntologyConfiguration(),
						hasDocuments);
			} catch (final Exception e) {
				log.error("Could not update ontology from JSON schema", e);
				return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
			}

		} else {
			try {
				if (rtdbDatasource == null)
					rtdbDatasource = RtdbDatasource.MONGO;
				final Ontology ontology = jsonToolUtils.createOntology(identification, identification, rtdbDatasource,
						schema, contextdata);
				ontologyBusinessService.createOntology(ontology, ontology.getUser().getUserId(), null);
			} catch (final IOException | OntologyBusinessServiceException e) {
				log.error("Error while creating ontology from schema ", e);
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		return ResponseEntity.ok().build();
	}

	@Operation(summary = "Validates a given JSON with its ontology JSON schema")
	@PostMapping("/{identification}/validate-schema")
	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	public ResponseEntity<ValidationReport> validateSchema(@RequestBody String payload,
			@PathVariable("identification") String identification) {
		if (!ontologyService.existsOntology(identification)) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		final ObjectMapper mapper = new ObjectMapper();
		JsonNode instance = null;
		try {
			instance = mapper.readTree(payload);
		} catch (final IOException e) {
			log.error("invalid JSON", e);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		try {
			ontologyDataService.checkOntologySchemaCompliance(instance,
					ontologyService.getOntologyByIdentification(identification));
		} catch (final DataSchemaValidationException e) {
			log.warn("JSON validation error");
			return new ResponseEntity<>(ValidationReport.builder().ok(false).errors(e.getMessage()).build(),
					HttpStatus.OK);
		}
		return new ResponseEntity<>(ValidationReport.builder().ok(true).build(), HttpStatus.OK);

	}

}
