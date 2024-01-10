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
package com.minsait.onesait.platform.controlpanel.controller.opendata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.business.services.dataset.DatasetService;
import com.minsait.onesait.platform.business.services.ontology.OntologyBusinessService;
import com.minsait.onesait.platform.business.services.ontology.OntologyBusinessServiceException;
import com.minsait.onesait.platform.business.services.opendata.OpenDataPermissions;
import com.minsait.onesait.platform.business.services.opendata.organization.OrganizationService;
import com.minsait.onesait.platform.business.services.resources.ResourceService;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.dto.OntologyForList;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Api.ApiStates;
import com.minsait.onesait.platform.config.model.Api.ApiType;
import com.minsait.onesait.platform.config.model.Dashboard;
import com.minsait.onesait.platform.config.model.DatasetResource;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.Viewer;
import com.minsait.onesait.platform.config.repository.ApiRepository;
import com.minsait.onesait.platform.config.repository.DashboardRepository;
import com.minsait.onesait.platform.config.repository.DatasetResourceRepository;
import com.minsait.onesait.platform.config.repository.ViewerRepository;
import com.minsait.onesait.platform.config.services.apimanager.ApiManagerService;
import com.minsait.onesait.platform.config.services.exceptions.OpenDataServiceException;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.opendata.dto.ApiMultipart;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataField;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataOrganization;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataPackage;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataResource;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataResourceDTO;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.GraviteeApi;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.GraviteeException;
import com.minsait.onesait.platform.controlpanel.services.gravitee.GraviteeService;

import lombok.extern.slf4j.Slf4j;

@RequestMapping("/opendata/resources")
@Controller
@Slf4j
public class ResourceController {

	private static final String RESOURCE = "resource";
	private static final String VIEWERS = "viewers";
	private static final String DASHBOARDS = "dashboards";
	private static final String APIS = "apis";
	private static final String ONTOLOGIES = "ontologies";
	private static final String DATASETS = "datasets";
	private static final String ERROR_403 = "error/403";

	private static final String API_SERVICES_STR = "apiServices";
	private static final String API_SWAGGER_UI_STR = "apiSwaggerUI";
	private static final String API_ENDPOINT_STR = "apiEndpoint";
	private static final String ENDPOINT_BASE_STR = "endpointBase";

	@Autowired
	private AppWebUtils utils;
	@Autowired
	private OrganizationService organizationService;
	@Autowired
	private DatasetService datasetService;
	@Autowired
	private ResourceService resourceService;
	@Autowired
	private OpenDataPermissions openDataPermissions;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private UserService userService;
	@Autowired
	private DatasetResourceRepository resourceRepository;
	@Autowired
	private DashboardRepository dashboardRepository;
	@Autowired
	private ViewerRepository viewerRepository;
	@Autowired
	private ApiRepository apiRepository;
	@Autowired
	private OntologyBusinessService ontologyBusinessService;
	@Autowired
	private IntegrationResourcesService integrationResourcesService;
	@Autowired
	private ApiManagerService apiManagerService;

	@Autowired(required = false)
	private GraviteeService graviteeService;
	
	@Autowired 
	private HttpSession httpSession;
	
	private static final String APP_ID = "appId";

	private final ObjectMapper mapper = new ObjectMapper();

	@Value("${opendata.max-bulk-size:10000}")
	private int maxBulkSize;

	@Value("${gravitee.enable}")
	private boolean graviteeOn;

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_USER,ROLE_DATASCIENTIST')")
	@GetMapping(value = "/list", produces = "text/html")
	public String listResources(Model model, RedirectAttributes redirect,
			@RequestParam(required = false, name = "name") String name) {
		//CLEANING APP_ID FROM SESSION
		httpSession.removeAttribute(APP_ID);

		final String userToken = utils.getCurrentUserOauthToken();
		try {
			final List<OpenDataOrganization> organizationsFromUser = organizationService
					.getOrganizationsFromUser(userToken);

			final List<OpenDataPackage> datasets = datasetService.getDatasetsByUser(userToken);

			List<OpenDataResource> resources = new ArrayList<>();
			for (final OpenDataPackage dataset : datasets) {
				resources.addAll(dataset.getResources());
			}
			if (name != null) {
				resources = resources.stream().filter(r -> r.getName().toLowerCase().contains(name.toLowerCase()))
						.collect(Collectors.toList());
			}

			final List<DatasetResource> configResources = datasetService.getConfigResources(resources);

			final List<OpenDataResourceDTO> resourcesDTO = resourceService.getDTOFromResourceList(resources,
					configResources, datasets, organizationsFromUser);
			final boolean createPermissions = datasetService.getCreatePermissions(organizationsFromUser);

			model.addAttribute("resourcesList", resourcesDTO);
			model.addAttribute("createPermissions", createPermissions);

			return "opendata/resources/list";
		} catch (final ResourceAccessException e) {
			log.error("Error listing resources: " + e.getMessage());
			utils.addRedirectMessage("resources.error.accessing", redirect);
			return "redirect:/main";
		}
	}

	@PostMapping("/getNamesForAutocomplete")
	public @ResponseBody List<String> getNamesForAutocomplete() {

	    final String userToken = utils.getCurrentUserOauthToken();
        final List<OpenDataPackage> datasets = datasetService.getDatasetsByUser(userToken);
    
        List<OpenDataResource> resources = new ArrayList<>();
        for (final OpenDataPackage dataset : datasets) {
            resources.addAll(dataset.getResources());
        }

        final List<String> resourceNames = new ArrayList<>();
        for (final OpenDataResource resource : resources) {
            resourceNames.add(resource.getName());
        }
        return resourceNames;
	}

	@GetMapping(value = "/create")
	public String createResource(Model model) {
		final String userToken = utils.getCurrentUserOauthToken();
		if (!utils.isAdministrator() && !openDataPermissions.hasPermissionsToCreateResource(userToken, null)) {
			return ERROR_403;
		}

		final List<OpenDataOrganization> organizationsFromUser = organizationService
				.getOrganizationsFromUser(userToken);
		final boolean createPermissions = datasetService.getCreatePermissions(organizationsFromUser);

		model.addAttribute("createPermissions", createPermissions);
		return "opendata/resources/create";
	}

	@GetMapping(value = { "/createfromontology", "/createfromfile", "/createfromurl" })
	public String createResourceFromOntology(Model model, HttpServletRequest request) {
		final String userToken = utils.getCurrentUserOauthToken();
		if (!utils.isAdministrator() && !openDataPermissions.hasPermissionsToCreateResource(userToken, null)) {
			return ERROR_403;
		}

		List<OpenDataOrganization> organizationsFromUser = organizationService.getOrganizationsFromUser(userToken);
		organizationsFromUser = organizationsFromUser.stream()
				.filter(elem -> elem.getCapacity().equals("admin") || elem.getCapacity().equals("editor"))
				.collect(Collectors.toList());
		final List<OpenDataPackage> datasets = datasetService.getDatasetsByUserWithPermissions(userToken,
				organizationsFromUser);
		final List<OntologyForList> ontologiesFromUser = ontologyService
				.getOntologiesForListByUserId(utils.getUserId());

		model.addAttribute(DATASETS, datasets);
		model.addAttribute(ONTOLOGIES, ontologiesFromUser);
		model.addAttribute(RESOURCE, new OpenDataResourceDTO());

		final String result = getCreateUrl(request.getRequestURI());
		if (!result.contains("createfromontology")) {
			final List<String> formats = resourceService.getFilesFormats();
			model.addAttribute("formats", formats);
		}
		return result;
	}

	@GetMapping(value = { "/createfromplatformresource" })
	public String createResourceFromPlatformResource(Model model, HttpServletRequest request) {
		final String userToken = utils.getCurrentUserOauthToken();
		if (!utils.isAdministrator() && !openDataPermissions.hasPermissionsToCreateResource(userToken, null)) {
			return ERROR_403;
		}

		List<OpenDataOrganization> organizationsFromUser = organizationService.getOrganizationsFromUser(userToken);
		organizationsFromUser = organizationsFromUser.stream()
				.filter(elem -> elem.getCapacity().equals("admin") || elem.getCapacity().equals("editor"))
				.collect(Collectors.toList());
		final List<OpenDataPackage> datasets = datasetService.getDatasetsByUserWithPermissions(userToken,
				organizationsFromUser);

		model.addAttribute(DATASETS, datasets);
		model.addAttribute(RESOURCE, new OpenDataResourceDTO());

		List<Dashboard> dashboards = new ArrayList<>();
		List<Viewer> viewers = new ArrayList<>();
		List<Api> apis = new ArrayList<>();
		if (utils.isAdministrator()) {
			dashboards = dashboardRepository.findAllByOrderByIdentificationAsc();
			viewers = viewerRepository.findAllByOrderByIdentificationAsc();
			apis = apiRepository.findAll();

			apis = apis.stream()
					.filter(elem -> elem.getState() == ApiStates.DEVELOPMENT || elem.getState() == ApiStates.PUBLISHED)
					.collect(Collectors.toList());

		} else {
			final User loggedUser = userService.getUser(utils.getUserId());
			dashboards = dashboardRepository.findByUser(loggedUser);
			viewers = viewerRepository.findByUserOrderByIdentificationAsc(loggedUser);
			apis = apiRepository.findByUser(loggedUser);
			apis = apis.stream()
					.filter(elem -> elem.getState() == ApiStates.DEVELOPMENT || elem.getState() == ApiStates.PUBLISHED)
					.collect(Collectors.toList());
		}

		apis.forEach(apiToObfuscate -> {
			apiManagerService.obfuscateUsersData(apiToObfuscate);
		});

		model.addAttribute(DASHBOARDS, dashboards);
		model.addAttribute(VIEWERS, viewers);
		model.addAttribute(APIS, apis);

		populateApiModel(model);

		return "opendata/resources/createfromplatformresource";
	}

	@PostMapping(value = { "/createfromontology", "/createfromfile", "/createfromurl" }, produces = "text/html")

	public String createResource(Model model, @ModelAttribute OpenDataResourceDTO resourceDTO,
			BindingResult bindingResult, RedirectAttributes redirect, HttpServletRequest request) {
		final String userToken = utils.getCurrentUserOauthToken();
		final String userId = utils.getUserId();

		if (!utils.isAdministrator() && !openDataPermissions.hasPermissionsToCreateResource(userToken, resourceDTO)) {
			return ERROR_403;
		}

		try {
			if (resourceService.existsResource(resourceDTO, userToken)) {
				log.error("This Resource already exists");
				utils.addRedirectMessage("resources.error.exist", redirect);
				return "redirect:/" + getCreateUrl(request.getRequestURI());
			}
			final String ontology = resourceDTO.getOntology();
			String query = "select * from " + ontology;
			List<Map<String, Object>> records = new ArrayList<>();
			List<OpenDataField> fields = new ArrayList<>();
			if (resourceDTO.getCreationType().equals("QUERY")) {
				query = resourceDTO.getQuery();
				records = resourceService.executeQuery(ontology, query, userId);
			} else {
				// CREAR ONTOLOGIA SI ES NECESARIO
				if (resourceDTO.getOntologyDescription() != null && !resourceDTO.getOntologyDescription().equals("")) {
					final Ontology newOntology = resourceService.createOntology(resourceDTO.getOntology(),
							resourceDTO.getOntologyDescription(), resourceDTO.getOntologySchema(), userId);
					ontologyBusinessService.createOntology(newOntology, newOntology.getUser().getUserId(), null);
				}

				String jsonData = "";
				if (resourceDTO.getCreationType().equals("URL")) {
					final String url = resourceDTO.getUrl();
					records = resourceService.getResourceFromUrl(url, new HashMap<>());
					jsonData = mapper.writeValueAsString(records);
				} else {
					jsonData = resourceDTO.getJsonData();
					records.addAll(mapper.readValue(jsonData, new TypeReference<List<Map<String, Object>>>() {
					}));
				}

				// INSERTAMOS DATOS EN ONTOLOGIA
				final String bulkInsert = resourceService.insertDataIntoOntology(ontology, jsonData, userId)
						.getMessage();
				if (!bulkInsert.equals("OK")) {
					final String errorMessage = bulkInsert.replaceAll("\"", "'");
					log.error("Cannot insert data into ontology: " + errorMessage);
					utils.addRedirectMessageWithParam("resources.error.created.bulkinsert", errorMessage, redirect);
					return "redirect:/" + getCreateUrl(request.getRequestURI());
				}

				fields = resourceService.getResourceFields(ontology, userId);
			}

			final String resourceId = resourceService.createResourceIteration(resourceDTO, userToken, records, fields);
			if (resourceId != null) {
				resourceService.persistResource(ontology, query, resourceId, resourceDTO.getName(),
						userService.getUser(userId));

				log.debug("Resource has been created succesfully");
				return "redirect:/opendata/resources/list";
			} else {
				log.error("Cannot create resource");
				utils.addRedirectMessage("resources.error.created", redirect);
				return "redirect:/" + getCreateUrl(request.getRequestURI());
			}
		} catch (final IOException e) {
			log.error("Cannot insert data into ontology: " + e.getMessage());
			utils.addRedirectMessage("Cannot insert data into ontology: " + e.getMessage(), redirect);
		} catch (final OntologyBusinessServiceException e) {
			log.error("Cannot create the ontology: " + e.getMessage());
			utils.addRedirectMessageWithParam("resources.error.created.ontology", e.getMessage(), redirect);
		} catch (final HttpClientErrorException e) {

			log.error("Cannot insert data in Open Data Portal: " + e.getResponseBodyAsString());

			utils.addRedirectMessage(" Cannot insert data in Open Data Portal: " + e.getResponseBodyAsString(),
					redirect);

		} catch (final Exception e) {
			log.error("Cannot create resource: " + e.getMessage());
			utils.addRedirectMessage("resources.error.created", redirect);
		}
		return "redirect:/opendata/resources/list";
	}

	@PostMapping(value = { "/createfromplatformresource" }, produces = "text/html")
	public String createResourceFromPlatformResource(Model model, @ModelAttribute OpenDataResourceDTO resourceDTO,
			@RequestParam(required = false) String postProcessFx,
			@RequestParam(required = false, defaultValue = "false") Boolean publish2gravitee,
			BindingResult bindingResult, RedirectAttributes redirect, HttpServletRequest request) {
		final String userToken = utils.getCurrentUserOauthToken();
		if (!utils.isAdministrator() && !openDataPermissions.hasPermissionsToCreateResource(userToken, resourceDTO)) {
			return ERROR_403;
		}

		try {
			if (resourceService.existsResource(resourceDTO, userToken)) {
				log.error("This Resource already exists");
				utils.addRedirectMessage("resources.error.exist", redirect);
				return "redirect:/opendata/resources/createfromplatformresource";
			}

			if (resourceDTO.isNewApi()) {
				if (!resourceDTO.getApi().getIdentification().matches(AppWebUtils.IDENTIFICATION_PATERN)) {
					log.error("Api identification not valid");
					utils.addRedirectMessage("resources.error.identification.format", redirect);
					return "redirect:/opendata/resources/createfromplatformresource";
				}

				resourceDTO.getApi().setApiType(ApiType.EXTERNAL_FROM_JSON);
				resourceDTO.getApi().setState(Api.ApiStates.DEVELOPMENT.toString());
				final String apiId = apiManagerService
						.createApi(apiMultipartMap(resourceDTO.getApi(), utils.getUserId()), null, null);
				if (!StringUtils.isEmpty(postProcessFx))
					apiManagerService.updateApiPostProcess(apiId, postProcessFx);
				if (graviteeService != null && publish2gravitee) {
					publish2Gravitee(apiId);
				}
				resourceDTO.setApiId(apiId);
			}
			final String resourceUrl = resourceService.getPlatformResourceUrl(resourceDTO, userToken);
			final String format = resourceService.getPlatformResourceFormat(resourceDTO);
			final String id = resourceService.createResource(resourceDTO, userToken, resourceUrl, format);
			if (id != null) {
				if (!resourceDTO.getPlatformResource().equals("api")) {
					resourceService.createWebView(id, userToken);
				}
				log.debug("Resource has been created succesfully");
				return "redirect:/opendata/resources/list";
			} else {
				log.error("Cannot create resource");
				utils.addRedirectMessage("resources.error.created", redirect);
				return "redirect:/opendata/resources/createfromplatformresource";
			}
		} catch (final HttpClientErrorException e) {
			log.error("Cannot create resource in Open Data Portal: " + e.getResponseBodyAsString());
			utils.addRedirectMessage(" Cannot create resource in Open Data Portal: " + e.getResponseBodyAsString(),
					redirect);

		} catch (final Exception e) {
			log.error("Cannot create resource: " + e.getMessage());
			utils.addRedirectMessage("resources.error.created", redirect);
		}
		return "redirect:/opendata/resources/list";
	}

	@GetMapping(value = "/show/{id}", produces = "text/html")
	public String showResource(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		try {
			final String userToken = utils.getCurrentUserOauthToken();

			OpenDataResource resource = null;
			if (id != null) {
				resource = resourceService.getResourceById(userToken, id);
			}
			if (resource == null) {
				return "error/404";
			}
			if (!utils.isAdministrator() && !openDataPermissions.hasPermissionsToShowResource(resource)) {
				return ERROR_403;
			}

			final String datasetId = resource.getPackage_id();
			final OpenDataPackage dataset = datasetService.getDatasetById(userToken, datasetId);
			final boolean modifyPermissions = resourceService.getModifyPermissions(dataset, userToken);

			final List<DatasetResource> resourceConfig = resourceRepository.findResourcesByIdsList(Arrays.asList(id));
			final OpenDataResourceDTO resourceDTO = resourceService.getDTOFromResource(resource, resourceConfig,
					dataset.getTitle());

			model.addAttribute(RESOURCE, resourceDTO);
			model.addAttribute("modifyPermissions", modifyPermissions);
			return "opendata/resources/show";
		} catch (final Exception e) {
			log.error("Error in Resource controller: " + e.getMessage());
			return "opendata/resources/list";
		}
	}

	@GetMapping(value = "/update/{id}")
	public String updateResource(@PathVariable String id, Model model, HttpServletRequest request) {
		final String userToken = utils.getCurrentUserOauthToken();
		final String userId = utils.getUserId();
		OpenDataResource resource = null;
		if (id != null) {

			resource = resourceService.getResourceById(userToken, id);

		}
		if (resource == null) {
			return "error/404";
		}

		if (!utils.isAdministrator() && !openDataPermissions.hasPermissionsToManipulateResource(userToken, resource)) {
			return ERROR_403;
		}

		final List<DatasetResource> configResources = resourceRepository.findResourcesByIdsList(Arrays.asList(id));
		final OpenDataResourceDTO resourceDTO = resourceService.getDTOFromResource(resource, configResources,
				resource.getPackage_id());

		final List<OpenDataPackage> datasets = datasetService.getDatasetsByUser(userToken);

		List<Ontology> ontologies = new ArrayList<>();
		boolean queryPermissions = false;

		final String resourceOntology = resourceDTO.getOntology();
		if (resourceOntology != null) {
			final Ontology ontology = ontologyService.getOntologyByIdentification(resourceOntology);
			ontologies = Arrays.asList(ontology);
			queryPermissions = ontologyService.hasUserPermissionForQuery(userId, ontology);
		}

		model.addAttribute("queryPermissions", queryPermissions);

		model.addAttribute(DATASETS, datasets);
		model.addAttribute(ONTOLOGIES, ontologies);
		model.addAttribute(RESOURCE, resourceDTO);
		return "opendata/resources/createfromontology";
	}

	@GetMapping(value = "/updatePlatformResource/{id}")
	public String updateResourceFromPlatformResource(@PathVariable String id, Model model, HttpServletRequest request) {
		final String userToken = utils.getCurrentUserOauthToken();
		final String userId = utils.getUserId();
		OpenDataResource resource = null;
		if (id != null) {

			resource = resourceService.getResourceById(userToken, id);

		}
		if (resource == null) {
			return "error/404";
		}
		if (!utils.isAdministrator() && !openDataPermissions.hasPermissionsToManipulateResource(userToken, resource)) {
			return ERROR_403;
		}

		final OpenDataResourceDTO resourceDTO = resourceService.getDTOFromResource(resource, new ArrayList<>(),
				resource.getPackage_id());

		final List<OpenDataPackage> datasets = datasetService.getDatasetsByUser(userToken);
		List<Dashboard> dashboards = new ArrayList<>();
		List<Viewer> viewers = new ArrayList<>();
		List<Api> apis = new ArrayList<>();
		boolean isOwner = false;

		if (utils.isAdministrator()) {
			dashboards = dashboardRepository.findAllByOrderByIdentificationAsc();
			viewers = viewerRepository.findAllByOrderByIdentificationAsc();
			apis = apiRepository.findAll();

			apis = apis.stream()
					.filter(elem -> elem.getState() == ApiStates.DEVELOPMENT || elem.getState() == ApiStates.PUBLISHED)
					.collect(Collectors.toList());

		} else {
			final User loggedUser = userService.getUser(utils.getUserId());
			dashboards = dashboardRepository.findByUser(loggedUser);
			viewers = viewerRepository.findByUserOrderByIdentificationAsc(loggedUser);
			apis = apiRepository.findByUser(loggedUser);
			apis = apis.stream()
					.filter(elem -> elem.getState() == ApiStates.DEVELOPMENT || elem.getState() == ApiStates.PUBLISHED)
					.collect(Collectors.toList());
		}

		final String resourceUrl = resource.getUrl();
		final Dashboard dashboard = resourceService.checkDashboardResource(resourceUrl);
		final Viewer viewer = resourceService.checkViewerResource(resourceUrl);
		final Api api = resourceService.checkApiResource(resourceUrl);
		if (dashboard != null) {
			if (utils.isAdministrator() || dashboard.getUser().getUserId().equals(userId)) {
				isOwner = true;
			}
			if (dashboards.stream().filter(o -> o.getId().equals(dashboard.getId())).findAny().orElse(null) == null)
				dashboards.add(dashboard);
			resourceDTO.setDashboardId(dashboard.getId());
			resourceDTO.setPlatformResource("dashboard");
			resourceDTO.setPlatformResourcePublic(dashboard.isPublic());
		} else if (viewer != null) {
			if (utils.isAdministrator() || viewer.getUser().getUserId().equals(userId)) {
				isOwner = true;
			}
			if (viewers.stream().filter(o -> o.getId().equals(viewer.getId())).findAny().orElse(null) == null)
				viewers.add(viewer);
			resourceDTO.setViewerId(viewer.getId());
			resourceDTO.setPlatformResource("viewer");
			resourceDTO.setPlatformResourcePublic(viewer.isPublic());
		} else if (api != null) {
			if (utils.isAdministrator() || api.getUser().getUserId().equals(userId)) {
				isOwner = true;
			}
			if (apis.stream().filter(o -> o.getId().equals(api.getId())).findAny().orElse(null) == null)
				apis.add(api);
			resourceDTO.setApiId(api.getId());
			resourceDTO.setPlatformResource("api");
			resourceDTO.setPlatformResourcePublic(api.isPublic());
		}

		apis.forEach(apiToObfuscate -> {
			apiManagerService.obfuscateUsersData(apiToObfuscate);
		});

		model.addAttribute(APIS, apis);
		model.addAttribute(DATASETS, datasets);
		model.addAttribute(DASHBOARDS, dashboards);
		model.addAttribute(VIEWERS, viewers);
		model.addAttribute("isOwner", isOwner);
		model.addAttribute(RESOURCE, resourceDTO);
		model.addAttribute("api", new Api());
		return "opendata/resources/createfromplatformresource";
	}

	@PutMapping(value = "/update/{id}", produces = "text/html")
	public String updateResource(@PathVariable String id, Model model, @ModelAttribute OpenDataResourceDTO resourceDTO,
			RedirectAttributes redirect, HttpServletRequest request) {
		final String userToken = utils.getCurrentUserOauthToken();
		if (resourceDTO != null) {
			if (!utils.isAdministrator()
					&& !openDataPermissions.hasPermissionsToManipulateResource(userToken, resourceDTO)) {
				return ERROR_403;
			}
			try {
				final OpenDataResource resource = resourceService.getResourceById(userToken, id);
				if (resource != null) {
					resourceService.updateResource(resourceDTO, resource, userToken);
				}

			} catch (final Exception e) {
				if (log.isDebugEnabled()) {
					log.debug("Cannot update resource {}", e.getMessage());
				}
				utils.addRedirectMessage("resources.error.updated", redirect);
				return "redirect:/opendata/resources/list";
			}
		} else {
			return "redirect:/opendata/resources/update/" + id;
		}
		log.debug("Resource has been updated succesfully");
		return "redirect:/opendata/resources/show/" + resourceDTO.getId();
	}

	@PutMapping(value = "/updatePlatformResource/{id}", produces = "text/html")
	public String updateResourceFromPlatformResource(@PathVariable String id, Model model,
			@ModelAttribute OpenDataResourceDTO resourceDTO, RedirectAttributes redirect, HttpServletRequest request) {
		final String userToken = utils.getCurrentUserOauthToken();
		if (resourceDTO != null) {
			if (!utils.isAdministrator()
					&& !openDataPermissions.hasPermissionsToManipulateResource(userToken, resourceDTO)) {
				return ERROR_403;
			}
			try {
				final OpenDataResource resource = resourceService.getResourceById(userToken, id);
				if (resource != null) {
					final String resourceUrl = resourceService.getPlatformResourceUrl(resourceDTO, userToken);
					final boolean createWebView = !(resource.getUrl().equals(resourceUrl));
					resourceService.updatePlatformResource(resourceDTO, resource, resourceUrl, userToken);

					if (!resourceDTO.getPlatformResource().equals("api") && createWebView) {
						resourceService.createWebView(id, userToken);
					}

					final Dashboard dashboard = resourceService.checkDashboardResource(resourceUrl);
					final Viewer viewer = resourceService.checkViewerResource(resourceUrl);
					final Api api = resourceService.checkApiResource(resourceUrl);
					resourceService.updatePublicPlatformResource(resourceDTO, dashboard, viewer, api);
				}

			} catch (final Exception e) {
				if (log.isDebugEnabled()) {
					log.debug("Cannot update resource {}", e.getMessage());
				}
				utils.addRedirectMessage("resources.error.updated", redirect);
				return "redirect:/opendata/resources/list";
			}
		} else {
			return "redirect:/opendata/resources/updatePlatformResource/" + id;
		}
		log.debug("Resource has been updated succesfully");
		return "redirect:/opendata/resources/showPlatformResource/" + resourceDTO.getId();
	}

	@GetMapping(value = "/showPlatformResource/{id}")
	public String showResourceFromPlatformResource(@PathVariable String id, Model model, HttpServletRequest request) {
		final String userToken = utils.getCurrentUserOauthToken();
		OpenDataResource resource = null;
		if (id != null) {

			resource = resourceService.getResourceById(userToken, id);

		}
		if (resource == null) {
			return "error/404";
		}
		if (!utils.isAdministrator() && !openDataPermissions.hasPermissionsToShowResource(resource)) {
			return ERROR_403;
		}

		final String datasetId = resource.getPackage_id();
		final OpenDataPackage dataset = datasetService.getDatasetById(userToken, datasetId);
		final boolean modifyPermissions = resourceService.getModifyPermissions(dataset, userToken);

		OpenDataResourceDTO resourceDTO = resourceService.getDTOFromResource(resource, new ArrayList<>(),
				dataset.getTitle());

		final String resourceUrl = resource.getUrl();
		final Dashboard dashboard = resourceService.checkDashboardResource(resourceUrl);
		final Viewer viewer = resourceService.checkViewerResource(resourceUrl);
		final Api api = resourceService.checkApiResource(resourceUrl);
		resourceDTO = resourceService.updateDTOWithPlatformResource(resourceDTO, dashboard, viewer, api);

		model.addAttribute(RESOURCE, resourceDTO);
		model.addAttribute("modifyPermissions", modifyPermissions);
		return "opendata/resources/showplatformresource";
	}

	@DeleteMapping("/delete/{id}")
	public String deleteResource(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {

		final String userToken = utils.getCurrentUserOauthToken();
		final OpenDataResource resource = resourceService.getResourceById(userToken, id);
		if (!utils.isAdministrator() && !openDataPermissions.hasPermissionsToManipulateResource(userToken, resource)) {

			return ERROR_403;
		}
		if (resource != null) {
			try {
				resourceService.deleteResource(userToken, id);

				resourceService.persistResource(id);
			} catch (final EmptyResultDataAccessException e) {
				if (log.isDebugEnabled()) {
					log.debug("The resource does not exist in ConfigDB: {}", e.getMessage());
				}
			} catch (final Exception e) {
				log.error("Could not delete the Resource");
				utils.addRedirectMessage("resources.error.delete", redirect);
			}
			log.debug("The Resource has been deleted correctly");
		} else {
			log.error("The Resource does not exist");
		}
		return "redirect:/opendata/resources/list";
	}

	@GetMapping("/download/{id}")
	public ResponseEntity downloadResource(Model model, @PathVariable("id") String id,
			@RequestParam("format") String format, RedirectAttributes redirect) {

		final String userToken = utils.getCurrentUserOauthToken();
		final OpenDataResource resource = resourceService.getResourceById(userToken, id);

		if (!utils.isAdministrator() && !openDataPermissions.hasPermissionsToShowResource(resource)) {
			return null;
		}
		if (resource != null) {
			try {
				return resourceService.downloadResource(userToken, resource, format);

			} catch (final Exception e) {
				log.debug("Could not download the Resource");
				utils.addRedirectMessage("resources.error.download", redirect);
				return null;
			}
		} else {
			log.debug("The Resource does not exist");
			return null;
		}
	}

	@PutMapping("/updateResourceInOpenDataPortal")
	public ResponseEntity<Map<String, String>> updateResourceInOpenDataPortal(
			@RequestBody OpenDataResourceDTO resourceDTO) {
		final String userToken = utils.getCurrentUserOauthToken();
		final String userId = utils.getUserId();
		final Map<String, String> result = new HashMap<>();
		if (resourceDTO != null) {
			if (!utils.isAdministrator()
					&& !openDataPermissions.hasPermissionsToManipulateResource(userToken, resourceDTO)) {
				result.put("error", "No permissions");
				return new ResponseEntity<>(result, HttpStatus.FORBIDDEN);
			}
			try {
				final String id = resourceDTO.getId();
				final OpenDataResource resource = resourceService.getResourceById(userToken, id);

				if (resource != null) {
					resourceService.cleanAllRecords(id, userToken);
					final String ontology = resourceDTO.getOntology();
					final String query = resourceDTO.getQuery();
					final List<Map<String, Object>> records = resourceService.executeQuery(ontology, query, userId);
					final List<OpenDataField> fields = new ArrayList<>();
					List<Map<String, Object>> recordsToInsert = new ArrayList<>();
					int init = 0;
					while (init < records.size()) {
						recordsToInsert = records.subList(init, Math.min(init + maxBulkSize, records.size()));
						resourceService.createResource(resourceDTO, userToken, recordsToInsert, fields);
						init += maxBulkSize;
					}
				}
			} catch (final HttpClientErrorException e) {
				log.error("Cannot update resource in Open Data Portal: " + e.getResponseBodyAsString());
				result.put("error", e.getResponseBodyAsString());
				return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
			} catch (final Exception e) {
				log.error("Cannot update resource " + e.getMessage());
				result.put("error", e.getMessage());
				return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			result.put("error", "ResourceDTO null");
			return new ResponseEntity<>(result, HttpStatus.NO_CONTENT);
		}
		log.debug("Resource has been updated succesfully");
		result.put("success", utils.getMessage("resources.update.success", ""));
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@GetMapping("/getResourceFromURL")
	public ResponseEntity<Map<String, String>> getResourceFromURL(Model model, @RequestParam String url) {
		final Map<String, String> resultMap = new HashMap<>();
		try {
			final List<Map<String, Object>> result = resourceService.getResourceFromUrl(url, resultMap);
			if (result.isEmpty()) {
				resultMap.put("error", "URL resource not acceptable");
				return new ResponseEntity<Map<String, String>>(resultMap, HttpStatus.NOT_ACCEPTABLE);
			}
			resultMap.put("data", mapper.writeValueAsString(result.get(0)));
			return new ResponseEntity<Map<String, String>>(resultMap, HttpStatus.OK);
		} catch (final Exception e) {
			log.error("Cannot access URL: " + e.getMessage());
			resultMap.put("error", e.getMessage());
			return new ResponseEntity<Map<String, String>>(resultMap, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private String getCreateUrl(String uri) {
		if (uri.contains("createfromontology")) {
			return "opendata/resources/createfromontology";
		} else if (uri.contains("createfromfile")) {
			return "opendata/resources/createfromfile";
		} else {
			return "opendata/resources/createfromurl";
		}
	}

	private Api apiMultipartMap(ApiMultipart apiMultipart, String userId) {
		final Api api = new Api();
		api.setId(apiMultipart.getId());
		api.setIdentification(apiMultipart.getIdentification());
		api.setApiType(apiMultipart.getApiType());
		api.setPublic(apiMultipart.isPublic());
		api.setDescription(apiMultipart.getDescription());
		api.setCategory(Api.ApiCategories.valueOf(apiMultipart.getCategory()));
		api.setOntology(apiMultipart.getOntology());
		api.setEndpointExt(apiMultipart.getEndpointExt());
		api.setMetaInf(apiMultipart.getMetaInf());
		api.setImageType(apiMultipart.getImageType());
		if (apiMultipart.getState() == null) {
			api.setState(Api.ApiStates.CREATED);
		} else {
			api.setState(Api.ApiStates.valueOf(apiMultipart.getState()));
		}
		api.setSsl_certificate(apiMultipart.isSslCertificate());
		api.setUser(userService.getUser(userId));
		if (apiMultipart.getCachetimeout() != null) {
			if (apiMultipart.getCachetimeout() < 1000 || apiMultipart.getCachetimeout() > 10) {
				api.setApicachetimeout(apiMultipart.getCachetimeout());
			}
		}
		if (apiMultipart.getApilimit() != null) {
			if (apiMultipart.getApilimit() <= 0) {
				api.setApilimit(1);
			} else {
				api.setApilimit(apiMultipart.getApilimit());
			}
		}
		api.setSwaggerJson(apiMultipart.getSwaggerJson());
		api.setCreatedAt(apiMultipart.getCreatedAt());
		try {
			api.setImage(apiMultipart.getImage().getBytes());
		} catch (final Exception e) {
		}
		api.setApiType(apiMultipart.getApiType());
		return api;
	}

	private void publish2Gravitee(String apiId) throws GenericOPException {
		final Api apiDb = apiManagerService.getById(apiId);
		try {
			final GraviteeApi graviteeApi = graviteeService.processApi(apiDb);
			apiDb.setGraviteeId(graviteeApi.getApiId());
			apiManagerService.updateApi(apiDb);
		} catch (final GraviteeException e) {
			log.error("Could not publish API to Gravitee {}", e.getMessage());
		}
	}

	private void populateApiModel(Model model) {
		model.addAttribute(ENDPOINT_BASE_STR, integrationResourcesService.getUrl(Module.APIMANAGER, ServiceUrl.BASE));
		model.addAttribute(API_ENDPOINT_STR, "");
		model.addAttribute(API_SERVICES_STR,
				integrationResourcesService.getUrl(Module.APIMANAGER, ServiceUrl.SWAGGERJSON));
		model.addAttribute(API_SWAGGER_UI_STR,
				integrationResourcesService.getUrl(Module.APIMANAGER, ServiceUrl.SWAGGERUI));

		model.addAttribute("categories", Api.ApiCategories.values());
		Api api = new Api();
		api.setApiType(ApiType.EXTERNAL_FROM_JSON);
		model.addAttribute("api", api);
		model.addAttribute("graviteeOn", graviteeOn);
	}

}
