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
package com.minsait.onesait.platform.controlpanel.rest.management.gadgetdatasource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.business.services.gadget.GadgetDatasourceBusinessService;
import com.minsait.onesait.platform.business.services.gadget.GadgetDatasourceBusinessServiceException;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.deletion.EntityDeletionService;
import com.minsait.onesait.platform.config.services.exceptions.GadgetDatasourceServiceException;
import com.minsait.onesait.platform.config.services.gadget.GadgetDatasourceService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.controlpanel.rest.management.gadgetdatasource.model.DatasourceDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.gadgetdatasource.model.DatasourceDTOCreate;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Datasource Management")
@RestController
@RequestMapping("api/gadgetdatasources")
@ApiResponses({ @ApiResponse(responseCode = "400", description = "Bad request"),
		@ApiResponse(responseCode = "500", description = "Internal server error"),
		@ApiResponse(responseCode = "403", description = "Forbidden") })
@Slf4j
public class GadgetDatasourceManagementRestController {

	@Autowired
	private GadgetDatasourceService datasourceService;

	@Autowired
	private GadgetDatasourceBusinessService gadgetDatasourceBusinessService;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private EntityDeletionService deletionService;

	@Autowired
	private AppWebUtils utils;

	private static final String DBTYPE = "RTDB";
	private static final String MODE = "query";
	private static final int MAXVALUES = 100;
	private static final String CONFIG = "";

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Get datasources")
	@GetMapping
	public ResponseEntity<?> getDatasources() {
		try {
			final List<GadgetDatasource> datasourceList = datasourceService.getUserGadgetDatasources(utils.getUserId());
			final List<DatasourceDTO> dtos = new ArrayList<>(datasourceList.size());
			for (final GadgetDatasource ds : datasourceList) {
				dtos.add(fromDatasourceToDTO(ds));
			}

			return new ResponseEntity<>(dtos, HttpStatus.OK);
		} catch (final GadgetDatasourceServiceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Get datasource by identification or id")
	@GetMapping("/{identification}")
	public ResponseEntity<?> getDatasourceByIdentification(
			@Parameter(description = "identification", required = true) @PathVariable("identification") String identification) {
		try {
			GadgetDatasource datasource = datasourceService.getDatasourceByIdentification(identification);
			if (datasource == null) {
				datasource = datasourceService.getGadgetDatasourceById(identification);
				if (datasource == null) {
					return new ResponseEntity<>("Datasource not found.", HttpStatus.OK);
				}
			}
			if (!datasourceService.hasUserViewPermission(datasource.getId(), utils.getUserId())) {
				return new ResponseEntity<>("The user is not authorized to retrieve the datasource.",
						HttpStatus.UNAUTHORIZED);
			}

			return new ResponseEntity<>(fromDatasourceToDTO(datasource), HttpStatus.OK);
		} catch (final GadgetDatasourceServiceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Create a datasource")
	@PostMapping
	public ResponseEntity<?> createDatasource(
			@Parameter(description = "DatasourceDTO", required = true) @Valid @RequestBody DatasourceDTOCreate datasourceDTO) {
		try {
			if (datasourceDTO.getQuery() == null || datasourceDTO.getIdentification() == null) {
				return new ResponseEntity<>("Missing required field. Required = [identification, query]",
						HttpStatus.BAD_REQUEST);
			}

			if (!datasourceDTO.getIdentification().matches(AppWebUtils.IDENTIFICATION_PATERN)) {
				return new ResponseEntity<>("Identification Error: Use alphanumeric characters and '-', '_'",
						HttpStatus.BAD_REQUEST);
			}

			final GadgetDatasource datasource = fromDTOtoDatasource(datasourceDTO);
			if (datasource == null) {
				return new ResponseEntity<>("The ontology does not exist.", HttpStatus.BAD_REQUEST);
			}

			if (!ontologyService.hasUserPermissionForQuery(utils.getUserId(),
					datasource.getOntology().getIdentification())) {
				return new ResponseEntity<>("The user does not have permissions to use the ontology.",
						HttpStatus.UNAUTHORIZED);
			}

			final GadgetDatasource dsCreated = datasourceService.createGadgetDatasource(datasource);

			return new ResponseEntity<>(fromDatasourceToDTO(dsCreated), HttpStatus.OK);
		} catch (final GadgetDatasourceServiceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Update a datasource")
	@PutMapping
	public ResponseEntity<?> updateDatasource(
			@Parameter(description = "DatasourceDTO", required = true) @Valid @RequestBody DatasourceDTOCreate datasourceDTO) {
		try {
			if (datasourceDTO.getIdentification() == null) {
				return new ResponseEntity<>("Missing required field. Required = [identification]",
						HttpStatus.BAD_REQUEST);
			}

			final GadgetDatasource existingDatasource = datasourceService
					.getDatasourceByIdentification(datasourceDTO.getIdentification());
			if (existingDatasource == null) {
				return new ResponseEntity<>("The datasource does not exist.", HttpStatus.NOT_FOUND);
			}

			if (!datasourceService.hasUserEditPermission(existingDatasource.getId(), utils.getUserId())) {
				return new ResponseEntity<>(
						"The user is not authorized to update a datasource on behalf of another user.",
						HttpStatus.UNAUTHORIZED);
			}

			final GadgetDatasource datasource = copyProperties(existingDatasource, datasourceDTO);
			if (datasource == null) {
				return new ResponseEntity<>("The ontology does not exist.", HttpStatus.BAD_REQUEST);
			}

			if (!ontologyService.hasUserPermissionForQuery(utils.getUserId(),
					datasource.getOntology().getIdentification())) {
				return new ResponseEntity<>("The user does not have permissions to use the ontology.",
						HttpStatus.BAD_REQUEST);
			}

			datasource.setId(existingDatasource.getId());
			datasource.setUpdatedAt(new Date());
			datasourceService.updateGadgetDatasource(datasource);

			return new ResponseEntity<>(fromDatasourceToDTO(datasource), HttpStatus.OK);
		} catch (final GadgetDatasourceServiceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Delete a datasource by identification")
	@DeleteMapping("/{identification}")
	public ResponseEntity<?> deleteDatasource(
			@Parameter(description = "identification", required = true) @PathVariable("identification") String identification) {

		final GadgetDatasource ds = datasourceService.getDatasourceByIdentification(identification);
		if (ds == null) {
			return new ResponseEntity<>("The datasource does not exist.", HttpStatus.NOT_FOUND);
		}

		if (!datasourceService.hasUserEditPermission(ds.getId(), utils.getUserId())) {
			return new ResponseEntity<>("The user is not authorized to delete a datasource on behalf of another user.",
					HttpStatus.UNAUTHORIZED);
		}

		deletionService.deleteGadgetDataSource(ds.getId(), utils.getUserId());

		return new ResponseEntity<>(String.format("The datasource %s has been deleted", identification), HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Get sample of datasource by identification or id")
	@GetMapping("/getSample/{identification}")
	public ResponseEntity<?> getSampleDatasourceByIdentification(
			@Parameter(description = "identification", required = true) @PathVariable("identification") String identification,
			@Parameter(description = "Record limit of sample", required = false) @RequestParam(value = "limit", required = false) Integer limit) {
		try {
			String datasourceId;
			final GadgetDatasource datasource = datasourceService.getDatasourceByIdentification(identification);
			if (datasource != null) {
				datasourceId = datasource.getId();
			} else {
				datasourceId = identification;
			}

			try {
				return new ResponseEntity<>(gadgetDatasourceBusinessService.getSampleGadgetDatasourceById(datasourceId,
						utils.getUserId(), limit == null ? 1 : limit, false), HttpStatus.OK);
			} catch (final GadgetDatasourceBusinessServiceException datasourceBusinessServiceException) {
				switch (datasourceBusinessServiceException.getErrorType()) {
				case NOT_FOUND:
					log.error("Datasource " + datasourceId + " not found ", datasourceBusinessServiceException);
					return new ResponseEntity<>("The datasource does not exist.", HttpStatus.NOT_FOUND);
				case UNAUTHORIZED:
					log.error("Datasource " + datasourceId + " unauthorized", datasourceBusinessServiceException);
					return new ResponseEntity<>("The datasource is unanthorized.", HttpStatus.UNAUTHORIZED);
				default:
					return new ResponseEntity<>("Generic error.", HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} catch (final Exception e) {
				log.error("Error generic executing sample datasource ", e);
				return new ResponseEntity<>("Generic error " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (final GadgetDatasourceServiceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Get fields of datasource by identification or id")
	@GetMapping("/getFields/{identification}")
	public ResponseEntity<?> getFieldsDatasourceByIdentification(
			@Parameter(description = "identification", required = true) @PathVariable("identification") String identification) {
		try {
			String datasourceId;
			final GadgetDatasource datasource = datasourceService.getDatasourceByIdentification(identification);
			if (datasource != null) {
				datasourceId = datasource.getId();
			} else {
				datasourceId = identification;
			}

			try {
				return new ResponseEntity<>(gadgetDatasourceBusinessService.getFieldsGadgetDatasourceById(datasourceId,
						utils.getUserId(), false), HttpStatus.OK);
			} catch (final GadgetDatasourceBusinessServiceException datasourceBusinessServiceException) {
				switch (datasourceBusinessServiceException.getErrorType()) {
				case NOT_FOUND:
					log.error("Datasource " + datasourceId + " not found ", datasourceBusinessServiceException);
					return new ResponseEntity<>("The datasource does not exist.", HttpStatus.NOT_FOUND);
				case NOT_DATA:
					log.error("Datasource " + datasourceId + " not data ", datasourceBusinessServiceException);
					return new ResponseEntity<>("the datasource has no data.", HttpStatus.NO_CONTENT);
				case UNAUTHORIZED:
					log.error("Datasource " + datasourceId + " unauthorized", datasourceBusinessServiceException);
					return new ResponseEntity<>("The datasource is unanthorized.", HttpStatus.UNAUTHORIZED);
				default:
					return new ResponseEntity<>("Generic error.", HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} catch (final Exception e) {
				log.error("Error generic executing sample datasource ", e);
				return new ResponseEntity<>("Generic error " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (final GadgetDatasourceServiceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Get filter fields of datasource by identification or id")
	@GetMapping("/getFilterFields/{identification}")
	public ResponseEntity<?> getFilterFieldsDatasourceByIdentification(
			@Parameter(description = "identification", required = true) @PathVariable("identification") String identification) {
		try {
			String datasourceId;
			final GadgetDatasource datasource = datasourceService.getDatasourceByIdentification(identification);
			if (datasource != null) {
				datasourceId = datasource.getId();
			} else {
				datasourceId = identification;
			}

			try {
				return new ResponseEntity<>(gadgetDatasourceBusinessService.getFieldsGadgetDatasourceById(datasourceId,
						utils.getUserId(), true), HttpStatus.OK);
			} catch (final GadgetDatasourceBusinessServiceException datasourceBusinessServiceException) {
				switch (datasourceBusinessServiceException.getErrorType()) {
				case NOT_FOUND:
					log.error("Datasource " + datasourceId + " not found ", datasourceBusinessServiceException);
					return new ResponseEntity<>("The datasource does not exist.", HttpStatus.NOT_FOUND);
				case UNAUTHORIZED:
					log.error("Datasource " + datasourceId + " unauthorized", datasourceBusinessServiceException);
					return new ResponseEntity<>("The datasource is unanthorized.", HttpStatus.UNAUTHORIZED);
				default:
					return new ResponseEntity<>("Generic error.", HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} catch (final Exception e) {
				log.error("Error generic executing sample datasource ", e);
				return new ResponseEntity<>("Generic error " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (final GadgetDatasourceServiceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private DatasourceDTO fromDatasourceToDTO(GadgetDatasource ds) {
		final DatasourceDTO dto = new DatasourceDTO();
		dto.setDescription(ds.getDescription());
		dto.setIdentification(ds.getIdentification());
		dto.setMaxvalues(ds.getMaxvalues());
		dto.setQuery(ds.getQuery());
		dto.setRefresh(ds.getRefresh());
		dto.setUser(ds.getUser().getUserId());
		dto.setAccessMode(ds.getMode());
		dto.setDatabase(ds.getDbtype());
		if (ds.getOntology() == null) {
			throw new GadgetDatasourceServiceException(
					"Error Ontology not assigned to datasource " + ds.getIdentification());
		}
		dto.setOntology(ds.getOntology().getIdentification());
		dto.setCreatedAt(ds.getCreatedAt().toString());
		dto.setUpdatedAt(ds.getUpdatedAt().toString());
		return dto;
	}

	private GadgetDatasource fromDTOtoDatasource(DatasourceDTOCreate dto) {
		final GadgetDatasource ds = new GadgetDatasource();

		ds.setIdentification(dto.getIdentification());
		if (dto.getDescription() == null) {
			ds.setDescription("");
		}
		ds.setDescription(dto.getDescription());
		if (dto.getMaxvalues() == null || dto.getMaxvalues() == 0) {
			ds.setMaxvalues(MAXVALUES);
		} else {
			ds.setMaxvalues(dto.getMaxvalues());
		}
		ds.setRefresh(dto.getRefresh());
		ds.setUser(userRepository.findByUserId(utils.getUserId()));
		ds.setQuery(dto.getQuery());
		ds.setDbtype(DBTYPE);
		ds.setMode(MODE);
		ds.setConfig(CONFIG);

		final Ontology o = ontologyService
				.getOntologyByIdentification(datasourceService.getOntologyFromDatasource(dto.getQuery()));
		if (o == null) {
			return null;
		}
		ds.setOntology(o);
		return ds;
	}

	private GadgetDatasource copyProperties(GadgetDatasource original, DatasourceDTOCreate dto) {
		if (dto.getDescription() != null) {
			original.setDescription(dto.getDescription());
		}
		if (dto.getMaxvalues() != null) {
			original.setMaxvalues(dto.getMaxvalues());
		}
		if (dto.getRefresh() != null) {
			original.setRefresh(dto.getRefresh());
		}
		if (dto.getQuery() != null && !dto.getQuery().equals(original.getQuery())) {
			final Ontology o = ontologyService
					.getOntologyByIdentification(datasourceService.getOntologyFromDatasource(dto.getQuery()));
			if (o == null) {
				return null;
			}
			original.setOntology(o);
			original.setQuery(dto.getQuery());
		}
		return original;
	}

}
