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
package com.minsait.onesait.platform.controlpanel.rest.management.gadget;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

import com.minsait.onesait.platform.config.model.Gadget;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.model.GadgetMeasure;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.exceptions.GadgetDatasourceServiceException;
import com.minsait.onesait.platform.config.services.gadget.GadgetDatasourceService;
import com.minsait.onesait.platform.config.services.gadget.GadgetService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.controlpanel.rest.management.gadget.model.GadgetDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.gadget.model.GadgetDTOCreate;
import com.minsait.onesait.platform.controlpanel.rest.management.gadget.model.GadgetDatasourceDTO;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(value = "Gadget Management", tags = { "Gadget management service" })
@RequestMapping("api/gadgets")
@ApiResponses({ @ApiResponse(code = 400, message = "Bad request"),
		@ApiResponse(code = 500, message = "Internal server error"), @ApiResponse(code = 403, message = "Forbidden"),
		@ApiResponse(code = 404, message = "Not found") })
public class GadgetManagementController {

	private static final String DBTYPE = "RTDB";
	private static final String MODE = "query";
	private static final int MAXVALUES = 100;
	private static final int REFRESH = 0;

	@Autowired
	private GadgetService gadgetService;

	@Autowired
	private GadgetDatasourceService datasourceService;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private AppWebUtils utils;

	@Autowired
	private UserRepository userService;

	@ApiResponses(@ApiResponse(code = 200, message = "OK"))
	@ApiOperation(value = "Get gadget by identification")
	@GetMapping("/{identification}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> getGadgetByIdentification(
			@ApiParam(value = "gadget identification", required = true) @PathVariable("identification") String identification) {

		String user = utils.getUserId();
		Gadget gadget = gadgetService.getGadgetByIdentification(user, identification);
		if (gadget == null)
			return new ResponseEntity<>("The gadget does not exist", HttpStatus.NOT_FOUND);

		if (!gadgetService.hasUserViewPermission(gadget.getId(), user)) {
			String eMessage = "The user is not authorized to view the gadget. Identification = " + identification;
			return new ResponseEntity<>(eMessage, HttpStatus.UNAUTHORIZED);
		}

		GadgetDTO dto = null;
		List<GadgetMeasure> measures = gadgetService.getGadgetMeasuresByGadgetId(utils.getUserId(), gadget.getId());
		if (measures != null) {
			dto = mapGadgetToGadgetDTO(gadget, measures.get(0).getDatasource(), measures);
		} else {
			dto = mapGadgetToGadgetDTO(gadget, null, null);
		}
		return new ResponseEntity<>(dto, HttpStatus.OK);

	}

	@ApiResponses(@ApiResponse(code = 200, message = "OK"))
	@ApiOperation(value = "Get user gadgets")
	@GetMapping("/")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> getUsersGadgets() {

		List<Gadget> gadgets = gadgetService.findGadgetWithIdentificationAndType(null, null, utils.getUserId());
		if (gadgets == null)
			return new ResponseEntity<>("[]", HttpStatus.OK);

		List<GadgetDTO> dtos = new ArrayList<>(gadgets.size());
		for (Gadget g : gadgets) {
			List<GadgetMeasure> measures = gadgetService.getGadgetMeasuresByGadgetId(utils.getUserId(), g.getId());
			if (measures != null) {
				dtos.add(mapGadgetToGadgetDTO(g, measures.get(0).getDatasource(), measures));
			} else {
				dtos.add(mapGadgetToGadgetDTO(g, null, null));
			}
		}
		return new ResponseEntity<>(dtos, HttpStatus.OK);

	}

	@ApiResponses(@ApiResponse(code = 200, message = "OK"))
	@ApiOperation(value = "Create gadget")
	@PostMapping("/")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> createGadget(@ApiParam(value = "GadgetDTOCreate") @RequestBody GadgetDTOCreate gadgetDTO) {

		if (gadgetDTO.getIdentification() == null || gadgetDTO.getIdentification().isEmpty()
				|| gadgetDTO.getConfig() == null || gadgetDTO.getType() == null || gadgetDTO.getDatasource() == null
				|| gadgetDTO.getDescription() == null)
			return new ResponseEntity<>(
					"Missing required fields. Required = [identification, description, datasource, config, type]",
					HttpStatus.BAD_REQUEST);

		if (gadgetService.getGadgetByIdentification(utils.getUserId(), gadgetDTO.getIdentification()) != null) {
			return new ResponseEntity<>(
					"The gadget with identification " + gadgetDTO.getIdentification() + " already exists",
					HttpStatus.BAD_REQUEST);
		}

		if (!gadgetDTO.getIdentification().matches(AppWebUtils.IDENTIFICATION_PATERN)) {
			return new ResponseEntity<>("Identification Error: Use alphanumeric characters and '-', '_'",
					HttpStatus.BAD_REQUEST);
		}

		GadgetDatasource datasource = new GadgetDatasource();
		try {
			datasource = retrieveOrCreateDatasource(gadgetDTO.getDatasource(), gadgetDTO.getIdentification());
		} catch (GadgetDatasourceServiceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}

		Gadget gadget = mapGadgetDTOToGadget(gadgetDTO);
		gadget.setUser(userService.findByUserId(utils.getUserId()));

		List<GadgetMeasure> gmList = listStringToGadgetMeasureList(gadgetDTO.getGadgetMeasures());

		Gadget gadgetCreate = gadgetService.createGadget(gadget, datasource, gmList);

		return new ResponseEntity<>(mapGadgetToGadgetDTO(gadgetCreate, datasource, gmList), HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(code = 200, message = "OK"))
	@ApiOperation(value = "Update gadget")
	@PutMapping("/")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> updateGadget(@ApiParam(value = "GadgetDTOCreate") @RequestBody GadgetDTOCreate gadgetDTO) {

		if (gadgetDTO.getIdentification() == null || gadgetDTO.getIdentification().isEmpty()
				|| gadgetDTO.getConfig() == null || gadgetDTO.getType() == null || gadgetDTO.getDatasource() == null
				|| gadgetDTO.getDescription() == null)
			return new ResponseEntity<>(
					"Missing required fields. Required = [identification, description, datasource, config, type]",
					HttpStatus.BAD_REQUEST);

		final String user = utils.getUserId();
		final Gadget gadget = gadgetService.getGadgetByIdentification(user, gadgetDTO.getIdentification());
		if (gadget == null) {
			String eMessage = "The gadget does not exist. Identification = " + gadgetDTO.getIdentification();
			return new ResponseEntity<>(eMessage, HttpStatus.NOT_FOUND);
		}

		if (!gadgetService.hasUserPermission(gadget.getId(), user)) {
			String eMessage = String.format(
					"The user is not authorized to update the gadget. Identification = %s, userId = %s",
					gadgetDTO.getIdentification(), user);
			return new ResponseEntity<>(eMessage, HttpStatus.UNAUTHORIZED);
		}

		GadgetDatasource datasource = new GadgetDatasource();
		try {
			datasource = retrieveOrCreateDatasource(gadgetDTO.getDatasource(), gadgetDTO.getIdentification());
		} catch (GadgetDatasourceServiceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}

		List<GadgetMeasure> gmList = listStringToGadgetMeasureList(gadgetDTO.getGadgetMeasures());
		gadget.setConfig(gadgetDTO.getConfig());
		gadget.setType(gadgetDTO.getType());
		gadget.setDescription(gadgetDTO.getDescription());
		gadgetService.updateGadget(gadget, datasource.getId(), gmList);

		return new ResponseEntity<>(mapGadgetToGadgetDTO(gadget, datasource, gmList), HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(code = 200, message = "OK"))
	@ApiOperation(value = "Delete gadget by identification")
	@DeleteMapping("/{identification}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> deleteGadget(
			@ApiParam(value = "identification") @PathVariable("identification") String identification) {

		String user = utils.getUserId();
		final Gadget gadget = gadgetService.getGadgetByIdentification(user, identification);
		if (gadget == null) {
			String eMessage = "The gadget does not exist. Identification = " + identification;
			return new ResponseEntity<>(eMessage, HttpStatus.NOT_FOUND);
		}

		if (!gadgetService.hasUserPermission(gadget.getId(), user)) {
			String eMessage = String.format(
					"The user is not authorized to delete the gadget. Identification = %s, userId = %s", identification,
					user);
			return new ResponseEntity<>(eMessage, HttpStatus.UNAUTHORIZED);
		}
		gadgetService.deleteGadget(gadget.getId(), user);

		return new ResponseEntity<>("Gadget deleted.", HttpStatus.OK);
	}

	private GadgetDatasource retrieveOrCreateDatasource(GadgetDatasourceDTO dto, String gadgetIdentification) {

		GadgetDatasource datasource = null;
		String query = dto.getQuery();

		if ((dto.getIdentification() == null || dto.getIdentification().isEmpty()) && query == null) {
			throw new GadgetDatasourceServiceException("Either the datasourceId or the query must be defined.");
		}

		if (dto.getIdentification() != null && !dto.getIdentification().isEmpty()) {
			GadgetDatasource existingDs = datasourceService.getDatasourceByIdentification(dto.getIdentification());

			if (existingDs == null && query == null) {
				String eMessage = String.format("The datasource does not exist. Id = %s.", dto.getIdentification());
				throw new GadgetDatasourceServiceException(eMessage);
			}
			if (existingDs != null && !datasourceService.hasUserPermission(existingDs.getId(), utils.getUserId())) {
				String eMessage = String.format(
						"The user does not have permissions to use the datasource. DatasourceId = %s, user = %s.",
						dto.getIdentification(), utils.getUserId());
				throw new GadgetDatasourceServiceException(eMessage);
			}
			if (existingDs != null && datasourceService.hasUserPermission(existingDs.getId(), utils.getUserId())) {
				datasource = existingDs;
				query = datasource.getQuery();
			}
		}

		Ontology ontology = ontologyService
				.getOntologyByIdentification(datasourceService.getOntologyFromDatasource(query));
		checkOntology(ontology);

		if (datasource != null) {
			updateDatasource(datasource, dto, ontology);
		} else {
			datasource = createDatasource(dto, gadgetIdentification, ontology);
		}

		return datasource;
	}

	private GadgetDatasource createDatasource(GadgetDatasourceDTO dto, String gadgetIdentification, Ontology ontology) {
		if (dto.getIdentification() == null || dto.getIdentification().isEmpty()) {
			dto.setIdentification(gadgetIdentification + "_" + new Date().getTime());
		}
		if (!dto.getIdentification().matches(AppWebUtils.IDENTIFICATION_PATERN)) {
			throw new GadgetDatasourceServiceException(
					"Identification Datasource Error: Use alphanumeric characters and '-', '_'");
		}
		GadgetDatasource ds = createDatasourceObject(dto, ontology);
		final GadgetDatasource dsCreated = datasourceService.createGadgetDatasource(ds);

		if (dsCreated == null) {
			String eMessage = "Unable to create datasource. Identification = " + dto.getIdentification();
			throw new GadgetDatasourceServiceException(eMessage);
		}
		return dsCreated;
	}

	private void updateDatasource(GadgetDatasource ds, GadgetDatasourceDTO dto, Ontology ontology) {
		updateDatasourceObject(ds, dto, ontology);
		datasourceService.updateGadgetDatasource(ds);
	}

	private void checkOntology(Ontology ontology) {
		if (ontology == null) {
			throw new GadgetDatasourceServiceException("The ontology does not exist.");
		}
		if (!ontologyService.hasUserPermissionForQuery(utils.getUserId(), ontology.getIdentification())) {
			throw new GadgetDatasourceServiceException("The user does not have permissions to use the ontology.");
		}
	}

	private GadgetDatasource createDatasourceObject(GadgetDatasourceDTO datasource, Ontology o) {
		GadgetDatasource ds = new GadgetDatasource();

		ds.setIdentification(datasource.getIdentification());
		ds.setDescription(datasource.getDescription());
		ds.setUser(userService.findByUserId(utils.getUserId()));
		ds.setQuery(datasource.getQuery());
		ds.setOntology(o);

		if (datasource.getMaxValues() == null) {
			ds.setMaxvalues(MAXVALUES);
		} else {
			ds.setMaxvalues(datasource.getMaxValues());
		}
		if (datasource.getRefresh() == null) {
			ds.setRefresh(REFRESH);
		} else {
			ds.setRefresh(datasource.getRefresh());
		}
		ds.setMode(MODE);
		ds.setDbtype(DBTYPE);

		return ds;
	}

	private void updateDatasourceObject(GadgetDatasource ds, GadgetDatasourceDTO dto, Ontology o) {
		if (dto.getDescription() != null) {
			ds.setDescription(dto.getDescription());
		}
		if (dto.getQuery() != null) {
			ds.setQuery(dto.getQuery());
		}
		if (o != null) {
			ds.setOntology(o);
		}
		if (dto.getMaxValues() != null) {
			ds.setMaxvalues(dto.getMaxValues());
		}
		if (dto.getRefresh() != null) {
			ds.setRefresh(dto.getRefresh());
		}
	}

	private GadgetDTO mapGadgetToGadgetDTO(Gadget gadget, GadgetDatasource datasource,
			List<GadgetMeasure> gadgetMeasures) {
		final GadgetDTO gDTO = new GadgetDTO();
		final GadgetDatasourceDTO dsDTO = new GadgetDatasourceDTO();

		gDTO.setConfig(gadget.getConfig());
		gDTO.setType(gadget.getType());
		gDTO.setDescription(gadget.getDescription());
		gDTO.setIdentification(gadget.getIdentification());
		gDTO.setUser(gadget.getUser().getUserId());
		if (gadgetMeasures != null) {
			List<String> gadgetMeasureList = new ArrayList<>();
			for (GadgetMeasure gm : gadgetMeasures) {
				gadgetMeasureList.add(gm.getConfig());
			}
			gDTO.setGadgetMeasures(gadgetMeasureList);
		}
		if (datasource != null) {
			dsDTO.setIdentification(datasource.getIdentification());
			dsDTO.setDescription(datasource.getDescription());
			dsDTO.setQuery(datasource.getQuery());
			dsDTO.setMaxValues(datasource.getMaxvalues());
			dsDTO.setRefresh(datasource.getRefresh());
			gDTO.setDatasource(dsDTO);
		}
		return gDTO;
	}

	private Gadget mapGadgetDTOToGadget(GadgetDTOCreate gDTO) {
		final Gadget g = new Gadget();

		g.setIdentification(gDTO.getIdentification());
		g.setDescription(gDTO.getDescription());
		g.setType(gDTO.getType());
		g.setConfig(gDTO.getConfig());

		return g;
	}

	private List<GadgetMeasure> listStringToGadgetMeasureList(List<String> list) {

		List<GadgetMeasure> gmList = new ArrayList<>();
		if (list != null) {
			for (String s : list) {
				GadgetMeasure gm = new GadgetMeasure();
				gm.setConfig(s);
				gmList.add(gm);
			}
		}
		return gmList;
	}

}