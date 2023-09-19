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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.minsait.onesait.platform.config.model.Category;
import com.minsait.onesait.platform.config.model.CategoryRelation;
import com.minsait.onesait.platform.config.model.Gadget;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.model.GadgetMeasure;
import com.minsait.onesait.platform.config.model.GadgetTemplate;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Subcategory;
import com.minsait.onesait.platform.config.repository.GadgetTemplateRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.category.CategoryService;
import com.minsait.onesait.platform.config.services.categoryrelation.CategoryRelationService;
import com.minsait.onesait.platform.config.services.exceptions.GadgetDatasourceServiceException;
import com.minsait.onesait.platform.config.services.exceptions.GadgetServiceException;
import com.minsait.onesait.platform.config.services.gadget.GadgetDatasourceService;
import com.minsait.onesait.platform.config.services.gadget.GadgetService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.subcategory.SubcategoryService;
import com.minsait.onesait.platform.controlpanel.rest.management.gadget.model.GadgetDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.gadget.model.GadgetDTOCreate;
import com.minsait.onesait.platform.controlpanel.rest.management.gadget.model.GadgetDatasourceDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.gadgettemplate.model.GadgetTemplateDTOList;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Gadget Management")
@RequestMapping("api/gadgets")
@ApiResponses({ @ApiResponse(responseCode = "400", description = "Bad request"),
		@ApiResponse(responseCode = "500", description = "Internal server error"),
		@ApiResponse(responseCode = "403", description = "Forbidden"),
		@ApiResponse(responseCode = "404", description = "Not found") })
public class GadgetManagementController {

	private static final String DBTYPE = "RTDB";
	private static final String MODE = "query";
	private static final int MAXVALUES = 100;
	private static final int REFRESH = 0;

	private static final String ERROR_FIND_GADGET = "Cannot find type for gadget, type must be base [bar, line, map, mixed, pie, radar, table, worldcloud, datadiscovery] or id/identification of gadget template";

	private static final String PATH = "/gadgets";

	@Autowired
	private GadgetService gadgetService;

	@Autowired
	private GadgetDatasourceService datasourceService;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private GadgetTemplateRepository gadgetTemplateRepository;

	@Autowired
	private AppWebUtils utils;

	@Autowired
	private UserRepository userService;

	@Autowired
	CategoryRelationService categoryRelationService;

	@Autowired
	CategoryService categoryService;

	@Autowired
	SubcategoryService subcategoryService;

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Get gadget by identification")
	@GetMapping("/{identification}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> getGadgetByIdentification(
			@Parameter(description = "gadget identification", required = true) @PathVariable("identification") String identification) {

		final String user = utils.getUserId();
		final Gadget gadget = gadgetService.getGadgetByIdentification(user, identification);
		if (gadget == null) {
			return new ResponseEntity<>("The gadget does not exist", HttpStatus.NOT_FOUND);
		}

		if (!gadgetService.hasUserViewPermission(gadget.getId(), user)) {
			final String eMessage = "The user is not authorized to view the gadget. Identification = " + identification;
			return new ResponseEntity<>(eMessage, HttpStatus.UNAUTHORIZED);
		}

		GadgetDTO dto = null;
		final List<GadgetMeasure> measures = gadgetService.getGadgetMeasuresByGadgetId(utils.getUserId(),
				gadget.getId());
		if (measures != null && measures.size() > 0) {
			dto = mapGadgetToGadgetDTO(gadget, measures.get(0).getDatasource(), measures);
		} else {
			dto = mapGadgetToGadgetDTO(gadget, null, null);
		}
		return new ResponseEntity<>(dto, HttpStatus.OK);

	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Get user gadgets")
	@GetMapping("/")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> getUsersGadgets() {

		final List<Gadget> gadgets = gadgetService.findGadgetWithIdentificationAndType(null, null, utils.getUserId());
		if (gadgets == null) {
			return new ResponseEntity<>("[]", HttpStatus.OK);
		}

		final List<GadgetDTO> dtos = new ArrayList<>(gadgets.size());
		for (final Gadget g : gadgets) {
			final List<GadgetMeasure> measures = gadgetService.getGadgetMeasuresByGadgetId(utils.getUserId(),
					g.getId());
			if (measures != null && measures.size() > 0) {
				dtos.add(mapGadgetToGadgetDTO(g, measures.get(0).getDatasource(), measures));
			} else {
				dtos.add(mapGadgetToGadgetDTO(g, null, null));
			}
		}
		return new ResponseEntity<>(dtos, HttpStatus.OK);

	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Create gadget")
	@PostMapping("/")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> createGadget(
			@Parameter(description = "GadgetDTOCreate") @RequestBody GadgetDTOCreate gadgetDTO) {

		if (gadgetDTO.getIdentification() == null || gadgetDTO.getIdentification().isEmpty()
				|| gadgetDTO.getConfig() == null || gadgetDTO.getType() == null || gadgetDTO.getDescription() == null) {
			return new ResponseEntity<>(
					"Missing required fields. Required = [identification, description, config, type]",
					HttpStatus.BAD_REQUEST);
		}

		if (gadgetService.getGadgetByIdentification(utils.getUserId(), gadgetDTO.getIdentification()) != null) {
			return new ResponseEntity<>(
					"The gadget with identification " + gadgetDTO.getIdentification() + " already exists",
					HttpStatus.BAD_REQUEST);
		}

		if (!gadgetDTO.getIdentification().matches(AppWebUtils.IDENTIFICATION_PATERN)) {
			return new ResponseEntity<>("Identification Error: Use alphanumeric characters and '-', '_'",
					HttpStatus.BAD_REQUEST);
		}
		GadgetDatasource datasource = null;
		if (gadgetDTO.getDatasource() != null) {
			datasource = new GadgetDatasource();
			try {
				datasource = retrieveOrCreateDatasource(gadgetDTO.getDatasource(), gadgetDTO.getIdentification());
			} catch (final GadgetDatasourceServiceException e) {
				return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
			}
		}
		final Gadget gadget = mapGadgetDTOToGadget(gadgetDTO);
		gadget.setUser(userService.findByUserId(utils.getUserId()));

		final List<GadgetMeasure> gmList = listStringToGadgetMeasureList(gadgetDTO.getGadgetMeasures());

		final Gadget gadgetCreate = gadgetService.createGadget(gadget, datasource, gmList, gadgetDTO.getCategory(),
				gadgetDTO.getSubcategory());

		return new ResponseEntity<>(mapGadgetToGadgetDTO(gadgetCreate, datasource, gmList), HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Update gadget")
	@PutMapping("/")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> updateGadget(
			@Parameter(description = "GadgetDTOCreate") @RequestBody GadgetDTOCreate gadgetDTO) {

		if (gadgetDTO.getIdentification() == null || gadgetDTO.getIdentification().isEmpty()
				|| gadgetDTO.getConfig() == null || gadgetDTO.getType() == null || gadgetDTO.getDescription() == null) {
			return new ResponseEntity<>(
					"Missing required fields. Required = [identification, description, config, type]",
					HttpStatus.BAD_REQUEST);
		}

		final String user = utils.getUserId();
		final Gadget gadget = gadgetService.getGadgetByIdentification(user, gadgetDTO.getIdentification());
		if (gadget == null) {
			final String eMessage = "The gadget does not exist. Identification = " + gadgetDTO.getIdentification();
			return new ResponseEntity<>(eMessage, HttpStatus.NOT_FOUND);
		}

		if (!gadgetService.hasUserPermission(gadget.getId(), user)) {
			final String eMessage = String.format(
					"The user is not authorized to update the gadget. Identification = %s, userId = %s",
					gadgetDTO.getIdentification(), user);
			return new ResponseEntity<>(eMessage, HttpStatus.UNAUTHORIZED);
		}

		GadgetDatasource datasource = new GadgetDatasource();
		if (gadgetDTO.getDatasource() != null) {
			try {
				datasource = retrieveOrCreateDatasource(gadgetDTO.getDatasource(), gadgetDTO.getIdentification());
			} catch (final GadgetDatasourceServiceException e) {
				return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
			}
		}

		final List<GadgetMeasure> gmList = listStringToGadgetMeasureList(gadgetDTO.getGadgetMeasures());
		gadget.setConfig(gadgetDTO.getConfig());
		gadget.setType(getGadgetTemplateByIdOrIdentification(gadgetDTO.getType()));
		if (gadgetDTO.getType() == null) {
			throw new GadgetServiceException(ERROR_FIND_GADGET);
		}
		gadget.setDescription(gadgetDTO.getDescription());
		gadgetService.updateGadget(gadget, datasource.getId(), gmList, gadgetDTO.getCategory(),
				gadgetDTO.getSubcategory());
		return new ResponseEntity<>(mapGadgetToGadgetDTO(gadget, datasource, gmList), HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Delete gadget by identification")
	@DeleteMapping("/{identification}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> deleteGadget(
			@Parameter(description = "identification") @PathVariable("identification") String identification)
			throws JsonProcessingException {

		final String user = utils.getUserId();
		final Gadget gadget = gadgetService.getGadgetByIdentification(user, identification);
		if (gadget == null) {
			final String eMessage = "The gadget does not exist. Identification = " + identification;
			return new ResponseEntity<>(eMessage, HttpStatus.NOT_FOUND);
		}

		if (!gadgetService.hasUserPermission(gadget.getId(), user)) {
			final String eMessage = String.format(
					"The user is not authorized to delete the gadget. Identification = %s, userId = %s", identification,
					user);
			return new ResponseEntity<>(eMessage, HttpStatus.UNAUTHORIZED);
		}
		gadgetService.deleteGadget(gadget.getId(), user);

		return new ResponseEntity<>("Gadget deleted.", HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = byte[].class))))
	@Operation(summary = "Generate image of gadget")
	@GetMapping(PATH + "/generateImage/{identification}")
	public ResponseEntity<byte[]> generateImage(@RequestHeader(value = "Authorization") String bearerToken,
			@Parameter(description = "Gadget ID", required = true) @PathVariable("identification") String id,
			@Parameter(description = "Wait time (ms) for rendering gadget", required = true) @RequestParam("waittime") int waittime,
			@Parameter(description = "Render Height", required = true) @RequestParam("height") int height,
			@Parameter(description = "Render Width", required = true) @RequestParam("width") int width,
			@Parameter(description = "Fullpage", required = false) @RequestParam(value = "fullpage", defaultValue = "false") Boolean fullpage,
			@Parameter(description = "Dashboard Params", required = false) @RequestParam(value = "params", required = false) String params) {
		Gadget gadget = gadgetService.getGadgetByIdentification(utils.getUserId(), id);
		if (gadget == null) {
			gadget = gadgetService.getGadgetById(utils.getUserId(), id);
		}
		if (gadget == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		} else if (!gadgetService.hasUserViewPermission(id, utils.getUserId())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}

		return gadgetService.generateImg(gadget.getId(), waittime, height, width, fullpage == null ? false : fullpage,
				params, prepareRequestToken(bearerToken));
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = byte[].class))))
	@Operation(summary = "Generate PDF of gadget")
	@GetMapping(PATH + "/generatePDF/{identification}")
	public ResponseEntity<byte[]> generatePDF(@RequestHeader(value = "Authorization") String bearerToken,
			@Parameter(description = "Dashboard ID", required = true) @PathVariable("identification") String id,
			@Parameter(description = "Wait time (ms) for rendering dashboard", required = true) @RequestParam("waittime") int waittime,
			@Parameter(description = "Render Height", required = true) @RequestParam("height") int height,
			@Parameter(description = "Render Width", required = true) @RequestParam("width") int width,
			@Parameter(description = "Dashboard Params", required = false) @RequestParam(value = "params", required = false) String params) {
		Gadget gadget = gadgetService.getGadgetByIdentification(utils.getUserId(), id);
		if (gadget == null) {
			gadget = gadgetService.getGadgetById(utils.getUserId(), id);
		}
		if (gadget == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		} else if (!gadgetService.hasUserViewPermission(id, utils.getUserId())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}

		return gadgetService.generatePDF(gadget.getId(), waittime, height, width, params,
				prepareRequestToken(bearerToken));
	}

	private GadgetDatasource retrieveOrCreateDatasource(GadgetDatasourceDTO dto, String gadgetIdentification) {

		GadgetDatasource datasource = null;
		String query = dto.getQuery();

		if ((dto.getIdentification() == null || dto.getIdentification().isEmpty()) && query == null) {
			throw new GadgetDatasourceServiceException("Either the datasourceId or the query must be defined.");
		}

		if (dto.getIdentification() != null && !dto.getIdentification().isEmpty()) {
			final GadgetDatasource existingDs = datasourceService
					.getDatasourceByIdentification(dto.getIdentification());

			if (existingDs == null && query == null) {
				final String eMessage = String.format("The datasource does not exist. Id = %s.",
						dto.getIdentification());
				throw new GadgetDatasourceServiceException(eMessage);
			}
			if (existingDs != null && !datasourceService.hasUserPermission(existingDs.getId(), utils.getUserId())) {
				final String eMessage = String.format(
						"The user does not have permissions to use the datasource. DatasourceId = %s, user = %s.",
						dto.getIdentification(), utils.getUserId());
				throw new GadgetDatasourceServiceException(eMessage);
			}
			if (existingDs != null && datasourceService.hasUserPermission(existingDs.getId(), utils.getUserId())) {
				datasource = existingDs;
				query = datasource.getQuery();
			}
		}

		final Ontology ontology = ontologyService
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
		final GadgetDatasource ds = createDatasourceObject(dto, ontology);
		final GadgetDatasource dsCreated = datasourceService.createGadgetDatasource(ds);

		if (dsCreated == null) {
			final String eMessage = "Unable to create datasource. Identification = " + dto.getIdentification();
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
		final GadgetDatasource ds = new GadgetDatasource();

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
		gDTO.setId(gadget.getId());
		gDTO.setConfig(gadget.getConfig());
		gDTO.setType(gadget.getType().getId());
		gDTO.setTemplate(toGadgetTemplateDTO(gadget.getType()));
		gDTO.setDescription(gadget.getDescription());
		gDTO.setIdentification(gadget.getIdentification());
		gDTO.setUser(gadget.getUser().getUserId());
		if (gadgetMeasures != null && gadgetMeasures.size() > 0) {
			final List<String> gadgetMeasureList = new ArrayList<>();
			for (final GadgetMeasure gm : gadgetMeasures) {
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
		gDTO.setInstance(gadget.isInstance());
		gDTO.setCreatedAt(gadget.getCreatedAt().toString());
		gDTO.setUpdatedAt(gadget.getUpdatedAt().toString());

		final CategoryRelation cr = categoryRelationService.getByIdType(gadget.getId());
		if (cr != null) {
			final Category c = categoryService.getCategoryById(cr.getCategory());
			if (c != null) {
				gDTO.setCategory(c.getIdentification());
			}
			final Subcategory s = subcategoryService.getSubcategoryById(cr.getSubcategory());
			if (s != null) {
				gDTO.setSubcategory(s.getIdentification());
			}
		}
		return gDTO;
	}

	private GadgetTemplateDTOList toGadgetTemplateDTO(GadgetTemplate template) {

		final GadgetTemplateDTOList dto = new GadgetTemplateDTOList();
		dto.setIdentification(template.getIdentification());
		dto.setDescription(template.getDescription());
		dto.setHtml(template.getTemplate());
		dto.setJs(template.getTemplateJS());
		dto.setPublic(template.isPublic());
		dto.setUser(template.getUser().getUserId());
		dto.setType(template.getType());
		final CategoryRelation cr = categoryRelationService.getByIdType(template.getId());
		if (cr != null) {
			final Category c = categoryService.getCategoryById(cr.getCategory());
			if (c != null) {
				dto.setCategory(c.getIdentification());
			}
			final Subcategory s = subcategoryService.getSubcategoryById(cr.getSubcategory());
			if (s != null) {
				dto.setSubcategory(s.getIdentification());
			}
		}
		dto.setCreatedAt(template.getCreatedAt().toString());
		dto.setUpdatedAt(template.getUpdatedAt().toString());

		return dto;
	}

	private Gadget mapGadgetDTOToGadget(GadgetDTOCreate gDTO) {
		final Gadget g = new Gadget();

		g.setIdentification(gDTO.getIdentification());
		g.setDescription(gDTO.getDescription());
		g.setType(getGadgetTemplateByIdOrIdentification(gDTO.getType()));
		if (g.getType() == null) {
			throw new GadgetServiceException(ERROR_FIND_GADGET);
		}
		g.setConfig(gDTO.getConfig());
		if (gDTO.getInstance() == null) {
			g.setInstance(false);
		} else {
			g.setInstance(gDTO.getInstance());
		}

		return g;
	}

	private GadgetTemplate getGadgetTemplateByIdOrIdentification(String ident) {
		GadgetTemplate gt = gadgetTemplateRepository.findById(ident).orElse(null);
		if (gt == null) {
			gt = gadgetTemplateRepository.findByIdentification(ident);
		}
		return gt;
	}

	private List<GadgetMeasure> listStringToGadgetMeasureList(List<String> list) {

		final List<GadgetMeasure> gmList = new ArrayList<>();
		if (list != null) {
			for (final String s : list) {
				final GadgetMeasure gm = new GadgetMeasure();
				gm.setConfig(s);
				gmList.add(gm);
			}
		}
		return gmList;
	}

	private String prepareRequestToken(String rawToken) {
		return rawToken.substring("Bearer ".length()).trim();
	}
}
