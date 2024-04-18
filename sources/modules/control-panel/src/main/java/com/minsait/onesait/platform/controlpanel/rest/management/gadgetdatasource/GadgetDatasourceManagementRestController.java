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
import org.springframework.web.bind.annotation.RestController;

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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "Datasource Management", tags = { "Datasource management service" })
@RestController
@RequestMapping("api/gadgetdatasources")
@ApiResponses({ @ApiResponse(code = 400, message = "Bad request"),
		@ApiResponse(code = 500, message = "Internal server error"), @ApiResponse(code = 403, message = "Forbidden") })
public class GadgetDatasourceManagementRestController {

	@Autowired
	private GadgetDatasourceService datasourceService;

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

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@ApiResponses(@ApiResponse(code = 200, message = "OK"))
	@ApiOperation(value = "Get datasources")
	@GetMapping
	public ResponseEntity<?> getDatasources() {
		try {
			final List<GadgetDatasource> datasourceList = datasourceService.getUserGadgetDatasources(utils.getUserId());
			final List<DatasourceDTO> dtos = new ArrayList<>(datasourceList.size());
			for (GadgetDatasource ds : datasourceList) {
				dtos.add(fromDatasourceToDTO(ds));
			}

			return new ResponseEntity<>(dtos, HttpStatus.OK);
		} catch (GadgetDatasourceServiceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@ApiResponses(@ApiResponse(code = 200, message = "OK"))
	@ApiOperation(value = "Get datasource by identification or id")
	@GetMapping("/{identification}")
	public ResponseEntity<?> getDatasourceByIdentification(
			@ApiParam(value = "identification", required = true) @PathVariable("identification") String identification) {
		try {
			GadgetDatasource datasource = datasourceService.getDatasourceByIdentification(identification);
			if (datasource == null) {
				datasource = datasourceService.getGadgetDatasourceById(identification);
				if (datasource == null)
					return new ResponseEntity<>("Datasource not found.", HttpStatus.OK);
			}
			if (!datasourceService.hasUserViewPermission(datasource.getId(), utils.getUserId()))
				return new ResponseEntity<>("The user is not authorized to retrieve the datasource.",
						HttpStatus.UNAUTHORIZED);

			return new ResponseEntity<>(fromDatasourceToDTO(datasource), HttpStatus.OK);
		} catch (GadgetDatasourceServiceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@ApiResponses(@ApiResponse(code = 200, message = "OK"))
	@ApiOperation(value = "Create a datasource")
	@PostMapping
	public ResponseEntity<?> createDatasource(
			@ApiParam(value = "DatasourceDTO", required = true) @Valid @RequestBody DatasourceDTOCreate datasourceDTO) {
		try {
			if (datasourceDTO.getQuery() == null || datasourceDTO.getIdentification() == null)
				return new ResponseEntity<>("Missing required field. Required = [identification, query]",
						HttpStatus.BAD_REQUEST);

			if (!datasourceDTO.getIdentification().matches(AppWebUtils.IDENTIFICATION_PATERN)) {
				return new ResponseEntity<>("Identification Error: Use alphanumeric characters and '-', '_'",
						HttpStatus.BAD_REQUEST);
			}

			GadgetDatasource datasource = fromDTOtoDatasource(datasourceDTO);
			if (datasource == null)
				return new ResponseEntity<>("The ontology does not exist.", HttpStatus.BAD_REQUEST);

			if (!ontologyService.hasUserPermissionForQuery(utils.getUserId(),
					datasource.getOntology().getIdentification()))
				return new ResponseEntity<>("The user does not have permissions to use the ontology.",
						HttpStatus.UNAUTHORIZED);

			final GadgetDatasource dsCreated = datasourceService.createGadgetDatasource(datasource);

			return new ResponseEntity<>(fromDatasourceToDTO(dsCreated), HttpStatus.OK);
		} catch (GadgetDatasourceServiceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@ApiResponses(@ApiResponse(code = 200, message = "OK"))
	@ApiOperation(value = "Update a datasource")
	@PutMapping
	public ResponseEntity<?> updateDatasource(
			@ApiParam(value = "DatasourceDTO", required = true) @Valid @RequestBody DatasourceDTOCreate datasourceDTO) {
		try {
			if (datasourceDTO.getIdentification() == null)
				return new ResponseEntity<>("Missing required field. Required = [identification]",
						HttpStatus.BAD_REQUEST);

			GadgetDatasource existingDatasource = datasourceService
					.getDatasourceByIdentification(datasourceDTO.getIdentification());
			if (existingDatasource == null)
				return new ResponseEntity<>("The datasource does not exist.", HttpStatus.NOT_FOUND);

			if (!datasourceService.hasUserEditPermission(existingDatasource.getId(), utils.getUserId()))
				return new ResponseEntity<>(
						"The user is not authorized to update a datasource on behalf of another user.",
						HttpStatus.UNAUTHORIZED);

			GadgetDatasource datasource = copyProperties(existingDatasource, datasourceDTO);
			if (datasource == null)
				return new ResponseEntity<>("The ontology does not exist.", HttpStatus.BAD_REQUEST);

			if (!ontologyService.hasUserPermissionForQuery(utils.getUserId(),
					datasource.getOntology().getIdentification()))
				return new ResponseEntity<>("The user does not have permissions to use the ontology.",
						HttpStatus.BAD_REQUEST);

			datasource.setId(existingDatasource.getId());
			datasource.setUpdatedAt(new Date());
			datasourceService.updateGadgetDatasource(datasource);

			return new ResponseEntity<>(fromDatasourceToDTO(datasource), HttpStatus.OK);
		} catch (GadgetDatasourceServiceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@ApiResponses(@ApiResponse(code = 200, message = "OK"))
	@ApiOperation(value = "Delete a datasource by identification")
	@DeleteMapping("/{identification}")
	public ResponseEntity<?> deleteDatasource(
			@ApiParam(value = "identification", required = true) @PathVariable("identification") String identification) {

		GadgetDatasource ds = datasourceService.getDatasourceByIdentification(identification);
		if (ds == null)
			return new ResponseEntity<>("The datasource does not exist.", HttpStatus.NOT_FOUND);

		if (!datasourceService.hasUserEditPermission(ds.getId(), utils.getUserId()))
			return new ResponseEntity<>("The user is not authorized to delete a datasource on behalf of another user.",
					HttpStatus.UNAUTHORIZED);

		deletionService.deleteGadgetDataSource(ds.getId(), utils.getUserId());

		return new ResponseEntity<>(String.format("The datasource %s has been deleted", identification), HttpStatus.OK);
	}

	private DatasourceDTO fromDatasourceToDTO(GadgetDatasource ds) {
		DatasourceDTO dto = new DatasourceDTO();
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
		GadgetDatasource ds = new GadgetDatasource();

		ds.setIdentification(dto.getIdentification());
		if (dto.getDescription() == null)
			ds.setDescription("");
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

		Ontology o = ontologyService
				.getOntologyByIdentification(datasourceService.getOntologyFromDatasource(dto.getQuery()));
		if (o == null)
			return null;
		ds.setOntology(o);
		return ds;
	}

	private GadgetDatasource copyProperties(GadgetDatasource original, DatasourceDTOCreate dto) {
		if (dto.getDescription() != null)
			original.setDescription(dto.getDescription());
		if (dto.getMaxvalues() != null)
			original.setMaxvalues(dto.getMaxvalues());
		if (dto.getRefresh() != null)
			original.setRefresh(dto.getRefresh());
		if (dto.getQuery() != null && !dto.getQuery().equals(original.getQuery())) {
			Ontology o = ontologyService
					.getOntologyByIdentification(datasourceService.getOntologyFromDatasource(dto.getQuery()));
			if (o == null)
				return null;
			original.setOntology(o);
			original.setQuery(dto.getQuery());
		}
		return original;
	}

}
