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
package com.minsait.onesait.platform.controlpanel.controller.presto;

import static com.minsait.onesait.platform.business.services.ontology.OntologyServiceStatusBean.MODULE_NOT_ACTIVE_KEY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.business.services.ontology.OntologyServiceStatusBean;
import com.minsait.onesait.platform.business.services.presto.datasource.PrestoDatasourceConfigurationService;
import com.minsait.onesait.platform.business.services.presto.datasource.PrestoConnectionProperty;
import com.minsait.onesait.platform.business.services.presto.datasource.PrestoDatasourceService;
import com.minsait.onesait.platform.config.model.OntologyPrestoDatasource;
import com.minsait.onesait.platform.config.model.OntologyPrestoDatasource.PrestoDatasourceType;
import com.minsait.onesait.platform.config.services.exceptions.PrestoDatasourceServiceException;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("presto")
@Slf4j
public class PrestoController {

	private static final String PRESTO_UI = "presto/ui";
	private static final String PRESTO_DATASOURCE_LIST = "presto/datasources/list";
	private static final String REDIRECT_PRESTO_DATASOURCE_LIST = "redirect:/presto/datasources/list";
	private static final String PRESTO_DATASOURCE_CREATE = "presto/datasources/create";
	private static final String REDIRECT_PRESTO_DATASOURCE_CREATE = "redirect:/presto/datasources/create";
	private static final String PRESTO_DATASOURCE_SHOW = "presto/datasources/show";
	private static final String REDIRECT_PRESTO_DATASOURCE_SHOW = "redirect:/presto/datasources/show/";
	private static final String REDIRECT_PRESTO_DATASOURCE_UPDATE = "redirect:/presto/datasources/update/";
	private static final String REDIRECT_MAIN = "redirect:/main";

	private static final String DATASOURCES_TYPE_STR = "datasources";
	private static final String DATASOURCE_TYPE_STR = "datasourceType";
	private static final String DATASOURCE_STR = "datasource";
	private static final String PROPERTIES_LIST_STR = "propertiesList";
	private static final String HISTORICAL_CATALOG_STR = "historicalCatalog";
	private static final String REALTIMEDB_CATALOG_STR = "realtimedbCatalog";
	private static final String CATALOG_LIST_STR = "catalogList";
	
	private static final String CONNECTOR_NAME_PROPERTY = "connector.name";

	@Autowired
	private PrestoDatasourceService prestoService;

	@Autowired
	private AppWebUtils utils;
	
	@Autowired
	private OntologyServiceStatusBean serviceStatusBean;
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private PrestoDatasourceConfigurationService prestoDatasourceConfigurationService;
	
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = "/ui")
	public String iframe(Model model, RedirectAttributes redirect) {
		
		if (!serviceStatusBean.isPrestoActive()) {
			redirect.addFlashAttribute(MODULE_NOT_ACTIVE_KEY,
					messageSource.getMessage("service.presto.down", null, LocaleContextHolder.getLocale()));
			return REDIRECT_MAIN;
		}
		
		model.addAttribute("url", getPrestoMonitoringUrl());
		return PRESTO_UI;
	}

	private String getPrestoMonitoringUrl() {
		return "/../presto/ui/";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = "/datasources/list", produces = "text/html")
	public String list(Model model, RedirectAttributes redirect, @RequestParam(required = false, name = "identification") String identification,
			@RequestParam(required = false, name = "type") PrestoDatasourceType type) {

		if (!serviceStatusBean.isPrestoActive()) {
			redirect.addFlashAttribute(MODULE_NOT_ACTIVE_KEY,
					messageSource.getMessage("service.presto.down", null, LocaleContextHolder.getLocale()));
			return REDIRECT_MAIN;
		}

		model.addAttribute(DATASOURCES_TYPE_STR,
				prestoService.getAllByIdentificationAndTypeAndUser(identification, type, utils.getUserId()));
		model.addAttribute(DATASOURCE_TYPE_STR, Arrays.asList(PrestoDatasourceType.values()));
		model.addAttribute(HISTORICAL_CATALOG_STR, prestoDatasourceConfigurationService.getHistoricalCatalog());
		model.addAttribute(REALTIMEDB_CATALOG_STR, prestoDatasourceConfigurationService.getRealtimedbCatalog());

		return PRESTO_DATASOURCE_LIST;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping("/datasources/getNamesForAutocomplete")
	public @ResponseBody List<String> getNamesForAutocomplete() {
		List<String> datasources = prestoService.getAllPrestoDatasourceIdentifications();
		return datasources;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = "/datasources/create", produces = "text/html")
	public String create(Model model, RedirectAttributes redirect) {

		if (!serviceStatusBean.isPrestoActive()) {
			redirect.addFlashAttribute(MODULE_NOT_ACTIVE_KEY,
					messageSource.getMessage("service.presto.down", null, LocaleContextHolder.getLocale()));
			return REDIRECT_MAIN;
		}
		model.addAttribute(DATASOURCE_STR, new PrestoConnectorDTO());
		model.addAttribute(DATASOURCE_TYPE_STR, Arrays.asList(PrestoDatasourceType.values()));
		model.addAttribute(HISTORICAL_CATALOG_STR, prestoDatasourceConfigurationService.getHistoricalCatalog());
		model.addAttribute(REALTIMEDB_CATALOG_STR, prestoDatasourceConfigurationService.getRealtimedbCatalog());
		model.addAttribute(CATALOG_LIST_STR, prestoService.getAllPrestoDatasourceIdentifications());
		
		return PRESTO_DATASOURCE_CREATE;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping(value = "/datasources/create")
	@Transactional
	public String create(Model model, @Valid PrestoConnectorDTO datasource, BindingResult bindingResult,
			RedirectAttributes redirect, HttpServletRequest request) {

		if (bindingResult.hasErrors()) {
			log.debug("Some datasource properties missing");
			utils.addRedirectMessage("presto.datasource.validation.error", redirect);
			return REDIRECT_PRESTO_DATASOURCE_CREATE;
		}
		
		try {
			if (prestoService.existsPrestoDatasourceIdentification(datasource.getIdentification())) {
				log.error("Cannot create Presto datasource because of: datasource identification already exists");
				utils.addRedirectMessage("presto.datasource.exists.error", redirect);
				redirect.addFlashAttribute(datasource);
				return REDIRECT_PRESTO_DATASOURCE_CREATE;
			}

			final Properties properties = getPropertiesFromDatasourceDTO(datasource);
			prestoService.createPrestoDatasource(convertConnectorDTOToDatasource(datasource), properties,
					utils.getUserId());

		} catch (final PrestoDatasourceServiceException e) {
			log.error("Cannot create Presto datasource because of:" + e.getMessage(), e);
			utils.addRedirectException(e, redirect);
			redirect.addFlashAttribute(datasource);
			return REDIRECT_PRESTO_DATASOURCE_CREATE;			
		}

		return REDIRECT_PRESTO_DATASOURCE_LIST;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping("/datasources/show/{id}")
	public String show(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {

		try {
			final OntologyPrestoDatasource datasource = prestoService.getPrestoDatasourceById(id);
			if (datasource != null) {
				final Properties properties = prestoService
						.getPropertiesFromPrestoDatasource(datasource);
				model.addAttribute(DATASOURCE_STR, datasource);
				model.addAttribute(PROPERTIES_LIST_STR, formatProperties(properties));
				model.addAttribute(HISTORICAL_CATALOG_STR, prestoDatasourceConfigurationService.getHistoricalCatalog());
				model.addAttribute(REALTIMEDB_CATALOG_STR, prestoDatasourceConfigurationService.getRealtimedbCatalog());
				return PRESTO_DATASOURCE_SHOW;
			} else {
				utils.addRedirectMessage("presto.datasource.notfound.error", redirect);
				return REDIRECT_PRESTO_DATASOURCE_LIST;
			}
		} catch (final PrestoDatasourceServiceException e) {
			utils.addRedirectMessage(e.getMessage(), redirect);
			return REDIRECT_PRESTO_DATASOURCE_LIST;
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = "/datasources/update/{id}", produces = "text/html")
	public String update(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		try {
			final OntologyPrestoDatasource ds = prestoService.getPrestoDatasourceById(id);
			if (ds != null) {
				final Properties properties = prestoService.getPropertiesFromPrestoDatasource(ds);
				properties.remove(CONNECTOR_NAME_PROPERTY);
				final PrestoConnectorDTO datasource = convertDatasourceToConnectorDTO(ds, properties);
				model.addAttribute(DATASOURCE_STR, datasource);
				model.addAttribute(DATASOURCE_TYPE_STR, Arrays.asList(PrestoDatasourceType.values()));
				model.addAttribute(PROPERTIES_LIST_STR, properties);
				model.addAttribute(HISTORICAL_CATALOG_STR, prestoDatasourceConfigurationService.getHistoricalCatalog());
				model.addAttribute(REALTIMEDB_CATALOG_STR, prestoDatasourceConfigurationService.getRealtimedbCatalog());
				return PRESTO_DATASOURCE_CREATE;
			} else {
				utils.addRedirectMessage("presto.datasource.notfound.error", redirect);
				return REDIRECT_PRESTO_DATASOURCE_LIST;
			}
		} catch (final Exception e) {
			utils.addRedirectMessage(e.getMessage(), redirect);
			return REDIRECT_PRESTO_DATASOURCE_LIST;
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PutMapping(value = "/datasources/update/{id}", produces = "text/html")
	public String updatePrestoConnector(Model model, @PathVariable("id") String id,
			@Valid PrestoConnectorDTO datasource, BindingResult bindingResult, RedirectAttributes redirect,
			HttpServletRequest httpServletRequest) {

		if (bindingResult.hasErrors()) {
			log.debug("Some Presto datasource properties missing");
			utils.addRedirectMessage("presto.datasource.validation.error", redirect);
			return REDIRECT_PRESTO_DATASOURCE_UPDATE + id;
		}

		try {
			final Properties properties = getPropertiesFromDatasourceDTO(datasource);
			final OntologyPrestoDatasource datasourceDB = prestoService.getPrestoDatasourceById(id);
			if (datasourceDB != null) {
				if (!prestoDatasourceConfigurationService.isHistoricalCatalog(datasourceDB.getIdentification()) &&
						!prestoDatasourceConfigurationService.isRealtimedbCatalog(datasourceDB.getIdentification())) {
					datasourceDB.setPublic(datasource.getIsPublic());
				}
				prestoService.updatePrestoDatasource(datasourceDB, properties);
			} else {
				log.error("Cannot update Presto datasource {}", id);
				utils.addRedirectMessage("presto.datasource.notfound.error", redirect);
				return REDIRECT_PRESTO_DATASOURCE_UPDATE + id;
			}

		} catch (PrestoDatasourceServiceException e) {
			log.error("Cannot update Presto datasource", e);
			utils.addRedirectMessage(e.getMessage(), redirect);
			return REDIRECT_PRESTO_DATASOURCE_UPDATE + id;
		}
		return REDIRECT_PRESTO_DATASOURCE_SHOW + id;

	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@DeleteMapping("/datasources/{id}")
	public String delete(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {

		final OntologyPrestoDatasource datasource = prestoService.getPrestoDatasourceById(id);

		if (datasource != null) {
			try {
				prestoService.deletePrestoDatasource(datasource);
			} catch (final Exception e) {
				utils.addRedirectMessage(e.getMessage(), redirect);
				log.error("Error deleting Presto datasource. ", e);
				return REDIRECT_PRESTO_DATASOURCE_LIST;
			}
			return REDIRECT_PRESTO_DATASOURCE_LIST;
		} else {
			utils.addRedirectMessage("presto.datasource.notfound.error", redirect);
			return REDIRECT_PRESTO_DATASOURCE_LIST;
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/datasources/checkConnection/{catalog}")
	public @ResponseBody ResponseEntity<?> checkConnection(@PathVariable("catalog") String catalog) {
		Boolean valid;
		try {
			valid = this.prestoService.checkConnection(catalog);
			return new ResponseEntity<>(valid, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = "/datasources/{type}/properties/")
	public @ResponseBody ResponseEntity<?> getPropertiesByType(@PathVariable("type") PrestoDatasourceType type) {
		try {			
			List<PrestoConnectionProperty> properties = prestoService.getConfigPropertiesByType(type.getPrestoDatasourceType());
			return new ResponseEntity<>(properties, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	
	private OntologyPrestoDatasource convertConnectorDTOToDatasource(PrestoConnectorDTO dto) {
		final OntologyPrestoDatasource datasource = new OntologyPrestoDatasource();
		datasource.setIdentification(dto.getIdentification());
		datasource.setType(dto.getType());
		datasource.setPublic(dto.getIsPublic());
		return datasource;
	}

	private PrestoConnectorDTO convertDatasourceToConnectorDTO(OntologyPrestoDatasource ds) {
		final PrestoConnectorDTO datasource = new PrestoConnectorDTO();
		datasource.setId(ds.getId());
		datasource.setIdentification(ds.getIdentification());
		datasource.setType(ds.getType());
		datasource.setIsPublic(ds.isPublic());
		return datasource;
	}

	private PrestoConnectorDTO convertDatasourceToConnectorDTO(OntologyPrestoDatasource ds, Properties properties) {
		final PrestoConnectorDTO datasource = convertDatasourceToConnectorDTO(ds);
		final List<PrestoConnectorPropertyDTO> list = new ArrayList<>();
		for (Entry<Object, Object> e : properties.entrySet()) {
			list.add(new PrestoConnectorPropertyDTO(e.getKey().toString(), e.getValue().toString()));
		}
		JSONArray jsonArray = new JSONArray(list);
		datasource.setPropertiesList(jsonArray.toString());
		return datasource;
	}

	private Properties getPropertiesFromDatasourceDTO(PrestoConnectorDTO datasource) {
		final Properties properties = new Properties();
		properties.put(CONNECTOR_NAME_PROPERTY, datasource.getType().getPrestoDatasourceType());
		try {
			ObjectMapper mapper = new ObjectMapper();
			List<PrestoConnectorPropertyDTO> propertiesList = mapper.readValue(datasource.getPropertiesList(),
					new TypeReference<List<PrestoConnectorPropertyDTO>>() {
					});
			for (PrestoConnectorPropertyDTO property : propertiesList) {
				properties.put(property.getName(), property.getValue());
			}
		} catch (JsonProcessingException e) {
			log.error("Error processing properties for catalog {}", datasource.getIdentification());
			throw new PrestoDatasourceServiceException("Error processing properties for catalog");
		}

		return properties;
	}

	private String formatProperties(Properties properties) {
		String s = "";
		for (Entry<Object, Object> e : properties.entrySet()) {
			s += e.getKey() + "=" + e.getValue() + "\n";
		}
		return s;
	}
	
	
}
