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
package com.minsait.onesait.platform.business.services.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.minsait.onesait.platform.business.services.dataset.DatasetService;
import com.minsait.onesait.platform.business.services.opendata.GraviteeApi;
import com.minsait.onesait.platform.business.services.opendata.OpenDataApi;
import com.minsait.onesait.platform.business.services.opendata.PlatformApi;
import com.minsait.onesait.platform.business.services.opendata.organization.OrganizationService;
import com.minsait.onesait.platform.commons.ActiveProfileDetector;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Dashboard;
import com.minsait.onesait.platform.config.model.DashboardUserAccess;
import com.minsait.onesait.platform.config.model.DashboardUserAccessType;
import com.minsait.onesait.platform.config.model.DatasetResource;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.UserApi;
import com.minsait.onesait.platform.config.model.Viewer;
import com.minsait.onesait.platform.config.repository.ApiRepository;
import com.minsait.onesait.platform.config.repository.DashboardRepository;
import com.minsait.onesait.platform.config.repository.DashboardUserAccessRepository;
import com.minsait.onesait.platform.config.repository.DashboardUserAccessTypeRepository;
import com.minsait.onesait.platform.config.repository.DatasetResourceRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.UserApiRepository;
import com.minsait.onesait.platform.config.repository.ViewerRepository;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.services.datamodel.DataModelService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataUnauthorizedException;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataDatastoreDeleteRecords;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataDatastoreResource;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataField;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataOrganization;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataPackage;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataPlatformResourceType;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataResource;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataResourceDTO;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataView;
import com.minsait.onesait.platform.config.services.opendata.dto.responses.DatastoreCreateResponse;
import com.minsait.onesait.platform.config.services.opendata.dto.responses.DatastoreDeleteResponse;
import com.minsait.onesait.platform.config.services.opendata.dto.responses.PackageSearchResponse;
import com.minsait.onesait.platform.config.services.opendata.dto.responses.ResourceCreateResponse;
import com.minsait.onesait.platform.config.services.opendata.dto.responses.ResourceShowResponse;
import com.minsait.onesait.platform.config.services.opendata.dto.responses.ResourceViewCreateResponse;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.config.model.Vertical;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.services.QueryToolService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;
import com.minsait.onesait.platform.router.service.app.model.NotificationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.QueryType;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.Source;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.service.RouterService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ResourceServiceImpl implements ResourceService {

	@Autowired
	private OpenDataApi api;
	@Autowired
	private DatasetService datasetService;
	@Autowired
	private OntologyRepository ontologyRepository;
	@Autowired
	private DatasetResourceRepository resourceRepository;
	@Autowired(required = false)
	@Qualifier("routerServiceImpl")
	private RouterService routerService;
	@Autowired
	private DataModelService dataModelService;
	@Autowired
	private UserService userService;
	@Autowired
	private DashboardRepository dashboardRepository;
	@Autowired
	private ViewerRepository viewerRepository;
	@Autowired
	private ApiRepository apiRepository;
	@Autowired
	private DashboardUserAccessRepository dashboardUserAccessRepository;
	@Autowired
	private DashboardUserAccessTypeRepository dashboardUserAccessTypeRepository;
	@Autowired
	private OrganizationService organizationService;
	@Autowired
	private UserApiRepository userApiRepository;
	@Autowired
	private PlatformApi platformApi;
	@Autowired
	private GraviteeApi graviteeApi;
	@Autowired
	private IntegrationResourcesServiceImpl resourcesService;
	@Autowired
	private ActiveProfileDetector profileDetector;
	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private QueryToolService queryToolService;
	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private MultitenancyService multitenancyService;

	@Value("${onesaitplatform.multitenancy.enabled:false}")
	private boolean multitenancyEnabled;

	@Value("${opendata.max-bulk-size:10000}")
	private int maxBulkSize;

	private String profile;
	private final ObjectMapper mapper = new ObjectMapper();

	@PostConstruct
	public void getActiveProfile() {
		profile = profileDetector.getActiveProfile();
	}

	@Override
	public List<OpenDataResourceDTO> getDTOFromResourceList(List<OpenDataResource> resources,
			List<DatasetResource> configResources, List<OpenDataPackage> datasetsFromUser,
			List<OpenDataOrganization> orgsFromUser) {
		final List<OpenDataResourceDTO> dtos = new ArrayList<>();
		for (final OpenDataResource res : resources) {
			final OpenDataResourceDTO obj = new OpenDataResourceDTO();
			obj.setId(res.getId());
			obj.setName(res.getName());
			obj.setFormat(res.getFormat());
			obj.setDatastore(res.isDatastore_active());
			obj.setUrl(res.getUrl());

			final Optional<DatasetResource> foundResourceInConfig = configResources.stream()
					.filter(elem -> elem.getId().equals(res.getId())).findFirst();
			if (foundResourceInConfig.isPresent()) {
				obj.setOntology(foundResourceInConfig.get().getOntology().getIdentification());
			}

			final String datasetId = res.getPackage_id();
			final OpenDataPackage dataset = datasetsFromUser.stream().filter(elem -> elem.getId().equals(datasetId))
					.findFirst().get();
			obj.setDataset(dataset.getTitle());

			final String organizationId = dataset.getOrganization().getId();
			final Optional<OpenDataOrganization> foundOrg = orgsFromUser.stream()
					.filter(elem -> elem.getId().equals(organizationId)).findFirst();
			if (foundOrg.isPresent()) {
				obj.setRole(foundOrg.get().getCapacity());
			} else {
				// Dataset publico que no pertenece a sus organizaciones
				obj.setRole("");
			}

			Date created = null;
			Date modified = null;
			try {
				created = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(res.getCreated());
				if (res.getLast_modified() != null) {
					modified = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(res.getLast_modified());
				}
			} catch (final ParseException e) {
				e.printStackTrace();
			}
			obj.setCreatedAt(created);
			obj.setUpdatedAt(modified);
			dtos.add(obj);
		}
		return dtos;
	}

	@Override
	public boolean existsResource(OpenDataResourceDTO resourceDTO, String userToken) {
		try {
			final String resourceName = URLEncoder.encode(resourceDTO.getName(), StandardCharsets.UTF_8.toString());
			final PackageSearchResponse responsePackages = (PackageSearchResponse) api.getOperation(
					"package_search?fq=res_name:" + resourceName + "&include_private=true", userToken,
					PackageSearchResponse.class);

			if (responsePackages.getSuccess() && responsePackages.getResult().getCount() > 0) {
				return true;
			} else {
				return false;
			}
		} catch (final HttpClientErrorException | UnsupportedEncodingException e) {
			return false;
		}
	}

	@Override
	public String createResource(OpenDataResourceDTO resourceDTO, String userToken, List<Map<String, Object>> records,
			List<OpenDataField> fields) {
		final OpenDataDatastoreResource datastoreResource = new OpenDataDatastoreResource();

		if (resourceDTO.getId() != null && !resourceDTO.getId().equals("")) {
			datastoreResource.setResource_id(resourceDTO.getId());
		} else {
			final OpenDataResource resource = new OpenDataResource();
			resource.setName(resourceDTO.getName());
			resource.setPackage_id(resourceDTO.getDataset());

			if (resourceDTO.getFormat() != null && !resourceDTO.getFormat().equals("")) {
				resource.setFormat(resourceDTO.getFormat());
			} else {
				resource.setFormat("JSON");
			}

			if (resourceDTO.getDescription() != null && !resourceDTO.getDescription().equals("")) {
				resource.setDescription(resourceDTO.getDescription());
			}
			datastoreResource.setResource(resource);
		}

		if (!fields.isEmpty()) {
			datastoreResource.setFields(fields);
		}
		datastoreResource.setRecords(records);

		final ResponseEntity<DatastoreCreateResponse> response = api.postOperation("datastore_create", userToken,
				datastoreResource, DatastoreCreateResponse.class);
		if (response.getStatusCode() == HttpStatus.OK) {
			final DatastoreCreateResponse object = response.getBody();
			return object.getResult().getResource_id();
		} else {
			return null;
		}
	}

	@Override
	public String createResource(OpenDataResourceDTO resourceDTO, String userToken, String resourceUrl, String format) {
		final OpenDataResource resource = new OpenDataResource();
		resource.setName(resourceDTO.getName());
		resource.setPackage_id(resourceDTO.getDataset());
		resource.setFormat(format);

		if (resourceUrl != null && !resourceUrl.equals("")) {
			resource.setUrl(resourceUrl);
		}

		if (resourceDTO.getDescription() != null && !resourceDTO.getDescription().equals("")) {
			resource.setDescription(resourceDTO.getDescription());
		}

		final ResponseEntity<ResourceCreateResponse> response = api.postOperation("resource_create", userToken,
				resource, ResourceCreateResponse.class);
		if (response.getStatusCode() == HttpStatus.OK) {
			final ResourceCreateResponse object = response.getBody();
			return object.getResult().getId();
		} else {
			return null;
		}
	}

	@Override
	public String updatePublicPlatformResource(OpenDataResourceDTO resourceDTO, Dashboard dashboard, Viewer viewer,
			Api api) {
		String result = "";
		if (dashboard != null) {
			dashboard.setPublic(resourceDTO.isPlatformResourcePublic());
			dashboardRepository.save(dashboard);
			result = dashboard.getId();
		} else if (viewer != null) {
			viewer.setPublic(resourceDTO.isPlatformResourcePublic());
			viewerRepository.save(viewer);
			result = viewer.getId();
		} else if (api != null) {
			api.setPublic(resourceDTO.isPlatformResourcePublic());
			apiRepository.save(api);
			if (!resourceDTO.isGraviteeSwagger() || api.getGraviteeId() == null || api.getGraviteeId().equals("")) {
				result = "v" + api.getNumversion() + "/" + api.getIdentification() + "/swagger.json";
			}
		}
		return result;
	}

	@Override
	public void persistResource(String ontology, String query, String resourceId, String name, User user) {
		final DatasetResource newResource = new DatasetResource();
		newResource.setId(resourceId);
		newResource.setQuery(query);
		newResource.setOntology(ontologyRepository.findByIdentification(ontology));
		newResource.setIdentification(name);
		newResource.setUser(user);
		resourceRepository.save(newResource);
	}

	@Override
	public void createWebView(String resourceId, String userToken) {
		final OpenDataView view = new OpenDataView();
		view.setTitle("New website view");
		view.setDescription("View to display websites in Open Data Portal");
		view.setView_type("webpage_view");
		view.setResource_id(resourceId);

		api.postOperation("resource_view_create", userToken, view, ResourceViewCreateResponse.class);
	}

	@Override
	public OpenDataResource getResourceById(String userToken, String id) {
		try {
			final ResourceShowResponse responseResource = (ResourceShowResponse) api
					.getOperation("resource_show?id=" + id, userToken, ResourceShowResponse.class);
			if (responseResource.getSuccess()) {
				final OpenDataResource resource = responseResource.getResult();
				return resource;
			} else {
				return null;
			}
		} catch (final HttpClientErrorException e) {
			log.error("Error getting resource " + e.getMessage());
			if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
				return null;
			}
			return new OpenDataResource();
		}
	}

	@Override
	public boolean getModifyPermissions(OpenDataPackage dataset, String userToken) {
		boolean modifyPermissions = false;
		final String orgId = dataset.getOrganization().getId();
		final List<OpenDataOrganization> organizationsFromUser = organizationService
				.getOrganizationsFromUser(userToken);
		final Optional<OpenDataOrganization> foundOrg = organizationsFromUser.stream()
				.filter(elem -> elem.getId().equals(orgId)).findFirst();
		if (foundOrg.isPresent()) {
			final String role = foundOrg.get().getCapacity();
			if (role.equals("admin") || role.equals("editor")) {
				modifyPermissions = true;
			}
		}
		return modifyPermissions;
	}

	@Override
	public OpenDataResourceDTO getDTOFromResource(OpenDataResource resource, List<DatasetResource> configResources,
			String dataset) {
		final OpenDataResourceDTO resourceDTO = new OpenDataResourceDTO();
		resourceDTO.setId(resource.getId());
		resourceDTO.setName(resource.getName());
		resourceDTO.setFormat(resource.getFormat());
		resourceDTO.setDescription(resource.getDescription());
		resourceDTO.setDataset(dataset);
		resourceDTO.setUrl(resource.getUrl());

		if (!configResources.isEmpty()) {
			resourceDTO.setOntology(configResources.get(0).getOntology().getIdentification());
			resourceDTO.setQuery(configResources.get(0).getQuery());
		}
		return resourceDTO;
	}

	@Override
	public List<String> getFilesFormats() {
		final List<String> formats = new ArrayList<>();
		formats.add("csv");
		formats.add("xml");
		formats.add("json");
		return formats;
	}

	@Override
	public void updateResource(OpenDataResourceDTO resourceDTO, OpenDataResource resource, String userToken) {
		resource.setName(resourceDTO.getName());
		if (resourceDTO.getDescription() != null) {
			resource.setDescription(resourceDTO.getDescription());
		} else {
			resource.setDescription("");
		}

		api.postOperation("resource_update", userToken, resource, ResourceShowResponse.class);
	}

	@Override
	public void updatePlatformResource(OpenDataResourceDTO resourceDTO, OpenDataResource resource, String resourceUrl,
			String userToken) {
		resource.setName(resourceDTO.getName());
		if (resourceDTO.getDescription() != null) {
			resource.setDescription(resourceDTO.getDescription());
		} else {
			resource.setDescription("");
		}
		if (resourceUrl != null && !resourceUrl.equals("")) {
			resource.setUrl(resourceUrl);
		}

		api.postOperation("resource_update", userToken, resource, ResourceShowResponse.class);
	}

	@Override
	public void deleteResource(String userToken, String id) {
		final OpenDataResource resource = new OpenDataResource();
		resource.setId(id);
		api.postOperation("resource_delete", userToken, resource, ResourceShowResponse.class);
	}

	@Override
	public void persistResource(String id) {
		resourceRepository.deleteById(id);

	}

	@Override
	public ResponseEntity downloadResource(String userToken, OpenDataResource resource, String format) {
		return api.download(userToken, resource, format);
	}

	@Override
	public void cleanAllRecords(String id, String userToken) {
		final OpenDataDatastoreDeleteRecords deleteBody = new OpenDataDatastoreDeleteRecords();
		deleteBody.setResource_id(id);
		deleteBody.setFilters(new HashMap<>());
		api.postOperation("datastore_delete", userToken, deleteBody, DatastoreDeleteResponse.class);
	}

	@Override
	public List<Map<String, Object>> getResourceFromUrl(String url, Map<String, String> resultMap) throws IOException {
		final URL newUrl = new URL(url);
		final URLConnection urlConn = newUrl.openConnection();
		final InputStreamReader input = new InputStreamReader(urlConn.getInputStream());
		resultMap.put("name", url);
		final String contentType = urlConn.getContentType();
		String json = "";
		if (contentType.equals("text/csv") || url.contains(".csv")) {
			json = getJsonFromCSV(input);
			resultMap.put("format", "application/vnd.ms-excel");
		} else if (contentType.equals("text/xml") || contentType.contentEquals("application/xml")
				|| url.contains(".xml")) {
			json = getJsonFromXML(input);
			resultMap.put("format", "text/xml");
		} else if (contentType.equals("application/json") || url.contains(".json")) {
			final BufferedReader reader = new BufferedReader(input);
			final StringBuilder responseStrBuilder = new StringBuilder();
			String str;
			while ((str = reader.readLine()) != null) {
				responseStrBuilder.append(str);
			}
			json = responseStrBuilder.toString();
			resultMap.put("format", "application/json");
		}
		json = json.replace("\\\"", "");
		if (!json.equals("")) {
			if (json.substring(0, 1).equals("{")) {
				final Map<String, Object> obj = mapper.readValue(json, new TypeReference<Map<String, Object>>() {
				});
				final List<Map<String, Object>> result = processMap(obj);
				return result;
			} else {
				return mapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {
				});
			}
		} else {
			resultMap.put("format", "");
			return new ArrayList<>();
		}
	}

	@Override
	public Ontology createOntology(String ontologyIdentification, String ontologyDescription, String schema,
			String userId) throws IOException {
		final Ontology ontology = new Ontology();
		ontology.setJsonSchema(completeSchema(schema, ontologyIdentification, ontologyDescription).toString());
		ontology.setIdentification(ontologyIdentification);
		ontology.setActive(true);
		ontology.setDataModel(dataModelService.getDataModelByName("EmptyBase"));
		ontology.setDescription(ontologyDescription);
		ontology.setUser(userService.getUser(userId));
		ontology.setMetainf("imported,json");
		ontology.setRtdbDatasource(Ontology.RtdbDatasource.valueOf("MONGO"));
		return ontology;
	}

	@Override
	public OperationResultModel insertDataIntoOntology(String ontology, String data, String userId)
			throws JsonProcessingException, IOException {
		final JsonNode node = mapper.readTree(data);
		final OperationModel operation = new OperationModel.Builder(ontology, OperationType.INSERT, userId,
				Source.INTERNAL_ROUTER).body(node.toString()).queryType(QueryType.NATIVE).build();
		final NotificationModel modelNotification = new NotificationModel();
		modelNotification.setOperationModel(operation);
		final OperationResultModel response = routerService.insert(modelNotification);
		return response;
	}

	@Override
	public void updateDashboardPermissions(Dashboard dashboard, String datasetId, String userToken) {
		final OpenDataPackage dataset = datasetService.getDatasetById(userToken, datasetId);
		if (dataset != null && dashboard != null) {
			final String orgId = dataset.getOwner_org();
			final List<User> orgUsers = organizationService.getUsersFromOrganization(userToken, orgId);
			for (final User user : orgUsers) {
				final DashboardUserAccess foundAccess = dashboardUserAccessRepository.findByDashboardAndUser(dashboard,
						user);
				if (foundAccess == null) {
					final DashboardUserAccess userAccess = new DashboardUserAccess();
					userAccess.setDashboard(dashboard);
					userAccess.setUser(user);
					final List<DashboardUserAccessType> viewAccess = dashboardUserAccessTypeRepository
							.findByName("VIEW");
					if (!viewAccess.isEmpty()) {
						userAccess.setDashboardUserAccessType(viewAccess.get(0));
					}
					dashboardUserAccessRepository.save(userAccess);
				}
			}
		}
	}

	@Override
	public void updateApiPermissions(Api api, String datasetId, String userToken) {
		final OpenDataPackage dataset = datasetService.getDatasetById(userToken, datasetId);
		if (dataset != null && api != null) {
			final String orgId = dataset.getOwner_org();
			final List<User> orgUsers = organizationService.getUsersFromOrganization(userToken, orgId);
			for (final User user : orgUsers) {
				final UserApi foundAccess = userApiRepository.findByApiIdAndUser(api.getId(), user.getUserId());
				if (foundAccess == null) {
					final UserApi userAccess = new UserApi();
					userAccess.setApi(api);
					userAccess.setUser(user);
					userApiRepository.save(userAccess);
				}
			}

		}

	}

	@Override
	public void updatePlatformResourcesFromDataset(OpenDataPackage dataset, User user) {
		for (final OpenDataResource resource : dataset.getResources()) {
			final String resourceUrl = resource.getUrl();
			if (resourceUrl != null && !resourceUrl.equals("")) {
				final Dashboard dashboard = checkDashboardResource(resourceUrl);
				final Viewer viewer = checkViewerResource(resourceUrl);
				final Api api = checkApiResource(resourceUrl);
				if (dashboard != null && (user.isAdmin() || dashboard.getUser().getUserId().equals(user.getUserId()))) {
					dashboard.setPublic(true);
					dashboardRepository.save(dashboard);
				} else if (viewer != null
						&& (user.isAdmin() || viewer.getUser().getUserId().equals(user.getUserId()))) {
					viewer.setPublic(true);
					viewerRepository.save(viewer);
				} else if (api != null && (user.isAdmin() || api.getUser().getUserId().equals(user.getUserId()))) {
					api.setPublic(true);
					apiRepository.save(api);
				}
			}
		}
	}

	@Override
	public Dashboard checkDashboardResource(String resourceUrl) {
		Dashboard dashboard = null;
		if (platformApi.isPlatformDashboard(resourceUrl)) {
			final String dashboardId = platformApi.getDashboardIdFromUrl(resourceUrl);
			if (dashboardRepository.findById(dashboardId).isPresent()) {
				dashboard = dashboardRepository.findById(dashboardId).get();
			}

		}
		return dashboard;
	}

	@Override
	public Viewer checkViewerResource(String resourceUrl) {
		Viewer viewer = null;
		if (platformApi.isPlatformViewer(resourceUrl)) {
			final String viewerId = platformApi.getViewerIdFromUrl(resourceUrl);
			if (viewerRepository.findById(viewerId).isPresent()) {
				viewer = viewerRepository.findById(viewerId).get();
			}

		}
		return viewer;
	}

	@Override
	public Api checkApiResource(String resourceUrl) {
		// IN CASE API HAS CHANGED, REFERENCE STILL NEEDS TO BE VALID IN ORDER TO
		// UPDATE...
		Api api = new Api();
		if (platformApi.isPlatformApi(resourceUrl)) {
			final String apiIdentification = platformApi.getApiIdentificationFromUrl(resourceUrl);
			final List<Api> foundApi = apiRepository.findByIdentification(apiIdentification);
			if (!foundApi.isEmpty()) {
				api = foundApi.get(0);
			}
		} else if (graviteeApi.isGraviteeApi(resourceUrl)) {
			final String graviteeId = graviteeApi.getGraviteeIdFromUrl(resourceUrl);
			final List<Api> allApis = apiRepository.findAll();
			final Optional<Api> foundApi = allApis.stream()
					.filter(elem -> elem.getGraviteeId() != null && elem.getGraviteeId().equals(graviteeId))
					.findFirst();
			if (foundApi.isPresent()) {
				api = foundApi.get();
			}
		}
		return api;
	}

	@Override
	public OpenDataResourceDTO updateDTOWithPlatformResource(OpenDataResourceDTO resourceDTO, Dashboard dashboard,
			Viewer viewer, Api api) {
		if (dashboard != null) {
			resourceDTO.setDashboardId(dashboard.getIdentification());
			resourceDTO.setPlatformResource("dashboard");
			resourceDTO.setPlatformResourcePublic(dashboard.isPublic());
		} else if (viewer != null) {
			resourceDTO.setViewerId(viewer.getIdentification());
			resourceDTO.setPlatformResource("viewer");
			resourceDTO.setPlatformResourcePublic(viewer.isPublic());
		} else if (api != null) {
			resourceDTO.setApiId(api.getIdentification() + " - V" + api.getNumversion());
			resourceDTO.setPlatformResource("api");
			resourceDTO.setPlatformResourcePublic(api.isPublic());
		}
		return resourceDTO;
	}

	@Override
	public List<OpenDataResource> getResourcesFromOrganization(String orgId, String userToken) {
		final List<OpenDataResource> result = new ArrayList<>();
		final PackageSearchResponse responsePackages = (PackageSearchResponse) api.getOperation(
				"package_search?include_private=true&fq=owner_org:" + orgId, userToken, PackageSearchResponse.class);

		if (responsePackages.getSuccess()) {
			final List<OpenDataPackage> datasets = responsePackages.getResult().getResults();
			for (final OpenDataPackage dataset : datasets) {
				result.addAll(dataset.getResources());
			}

		}
		return result;
	}

	@Override
	public String getSwaggerGravitee(Api api) {
		final String graviteeId = api.getGraviteeId();
		final String pageId = graviteeApi.executePagesApi(graviteeId);
		if (pageId == null || pageId.isEmpty()) {
			return null;
		} else {
			return graviteeApi.getSwaggerGravitee(graviteeId, pageId);
		}
	}

	public String getJsonFromCSV(InputStreamReader input) throws IOException {
		final CsvSchema csvSchema = CsvSchema.builder().setUseHeader(true).build();
		final CsvMapper csvMapper = new CsvMapper();
		csvMapper.enable(CsvGenerator.Feature.STRICT_CHECK_FOR_QUOTING);
		final List<Object> readAll = csvMapper.readerFor(Map.class).with(csvSchema).readValues(input).readAll();

		return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(readAll);
	}

	public String getJsonFromXML(InputStreamReader input) throws IOException {
		final BufferedReader streamReader = new BufferedReader(input);
		final StringBuilder responseStrBuilder = new StringBuilder();

		String result;
		while ((result = streamReader.readLine()) != null) {
			responseStrBuilder.append(result);
		}

		final JSONObject xmlJSONObj = XML.toJSONObject(responseStrBuilder.toString());
		return xmlJSONObj.toString(4);
	}

	public List<Map<String, Object>> processMap(Map<String, Object> obj) {
		final List<Map<String, Object>> result = new ArrayList<>();
		for (final Entry<String, Object> entry : obj.entrySet()) {

			if (entry.getValue() instanceof List) {
				return (List<Map<String, Object>>) entry.getValue();
			} else if (entry.getValue() instanceof Map) {
				return processMap((Map<String, Object>) entry.getValue());
			}
		}
		return result;
	}

	private JsonNode completeSchema(String schema, String identification, String description) throws IOException {
		final JsonNode schemaSubTree = mapper.readTree(schema);
		((ObjectNode) schemaSubTree).put("type", "object");
		((ObjectNode) schemaSubTree).put("description", "Info " + identification);

		((ObjectNode) schemaSubTree).put("$schema", "http://json-schema.org/draft-04/schema#");
		((ObjectNode) schemaSubTree).put("title", identification);

		((ObjectNode) schemaSubTree).put("additionalProperties", true);
		return schemaSubTree;
	}

	@Override
	public String getPlatformResourceUrl(OpenDataResourceDTO resourceDTO, String userToken) {

		String resourceUrl = "";
		Dashboard dashboard = null;
		Viewer viewer = null;
		Api api = null;
		switch (OpenDataPlatformResourceType.valueOf(resourceDTO.getPlatformResource().toUpperCase())) {
		case DASHBOARD:
			resourceUrl = resourcesService.getUrl(Module.DASHBOARDENGINE, ServiceUrl.ONLYVIEW);
			if (dashboardRepository.findById(resourceDTO.getDashboardId()).isPresent()) {
				dashboard = dashboardRepository.findById(resourceDTO.getDashboardId()).get();
			}
			updateDashboardPermissions(dashboard, resourceDTO.getDataset(), userToken);
			break;
		case VIEWER:
			resourceUrl = resourcesService.getUrl(Module.GIS_VIEWER, ServiceUrl.VIEW);
			if (viewerRepository.findById(resourceDTO.getViewerId()).isPresent()) {
				viewer = viewerRepository.findById(resourceDTO.getViewerId()).get();
			}
			break;
		case API:
			resourceUrl = resourcesService.getUrl(Module.APIMANAGER, ServiceUrl.SWAGGERJSON);
			if (apiRepository.findById(resourceDTO.getApiId()).isPresent()) {
				api = apiRepository.findById(resourceDTO.getApiId()).get();
				if (resourceDTO.isGraviteeSwagger() && api.getGraviteeId() != null && !api.getGraviteeId().equals("")) {
					final String urlGravitee = getSwaggerGravitee(api);
					if (urlGravitee != null) {
						resourceUrl = getSwaggerGravitee(api);
					}
				}
			}
			updateApiPermissions(api, resourceDTO.getDataset(), userToken);
			break;
		default:
			return null;
		}
		resourceUrl += updatePublicPlatformResource(resourceDTO, dashboard, viewer, api);
		if (multitenancyEnabled) {
			final Optional<Vertical> foundVertical = multitenancyService
					.getVertical(MultitenancyContextHolder.getVerticalSchema());
			if (foundVertical.isPresent()) {
				resourceUrl += "?vertical=" + foundVertical.get().getName() + "&tenant="
						+ MultitenancyContextHolder.getTenantName();
			}
		}

		return resourceUrl;
	}

	@Override
	public String getPlatformResourceFormat(OpenDataResourceDTO resourceDTO) {
		String format = "";
		switch (OpenDataPlatformResourceType.valueOf(resourceDTO.getPlatformResource().toUpperCase())) {
		case DASHBOARD:
			format = "HTML";
			break;
		case VIEWER:
			format = "HTML";
			break;
		case API:
			format = "openapi-json";
			break;
		default:
			break;
		}
		return format;
	}

	@Override
	public List<Map<String, Object>> executeQuery(String ontology, String query, String userId) {
		final List<Map<String, Object>> records = new ArrayList<>();
		final Integer queriesLimit = (Integer) configurationService.getGlobalConfiguration(profile).getEnv()
				.getDatabase().get("queries-limit");
		if (queriesLimit != null) {
			int offset = 0;
			while (true) {

				try {
					final String newQuery = "SELECT * FROM (" + query + ") AS c OFFSET " + offset;
					final String resultQuery = queryToolService.querySQLAsJson(userId, ontology, newQuery, 0);
					final List<Map<String, Object>> resultList = mapper.readValue(resultQuery,
							new TypeReference<List<Map<String, Object>>>() {
							});
					if (resultList.isEmpty()) {
						break;
					}
					records.addAll(resultList);
					offset += queriesLimit.intValue();
				} catch (DBPersistenceException | OntologyDataUnauthorizedException | GenericOPException
						| IOException e) {

					e.printStackTrace();
					break;
				}
			}
		}
		removeContextDataAndMongoId(records);
		return records;
	}

	private void removeContextDataAndMongoId(List<Map<String, Object>> records) {
		for (final Map<String, Object> map : records) {
			if (map.containsKey("contextData")) {
				map.remove("contextData");
			}
			if (map.containsKey("_id")) {
				map.remove("_id");
			}
		}
	}

	@Override
	public String createResourceIteration(OpenDataResourceDTO resourceDTO, String userToken,
			List<Map<String, Object>> records, List<OpenDataField> fields) {
		int init = 0;
		String resourceId = null;
		while (init < records.size()) {
			final List<Map<String, Object>> recordsToInsert = records.subList(init,
					Math.min(init + maxBulkSize, records.size()));
			resourceId = createResource(resourceDTO, userToken, recordsToInsert, fields);
			resourceDTO.setId(resourceId);
			init += maxBulkSize;
		}
		return resourceId;
	}

	@Override
	public List<OpenDataField> getResourceFields(String ontology, String userId) throws IOException {
		final List<OpenDataField> fields = new ArrayList<>();
		final Map<String, String> ontologyFields = ontologyService.getOntologyFields(ontology, userId);
		for (final Entry<String, String> entry : ontologyFields.entrySet()) {
			String name = entry.getKey();
			String type = entry.getValue();
			if (name.contains(".")) {
				name = name.split("\\.")[0];
				type = "object";
			}
			final String id = name;
			final Optional<OpenDataField> foundField = fields.stream().filter(elem -> elem.getId().equals(id))
					.findFirst();
			if (!foundField.isPresent()) {
				final OpenDataField newField = new OpenDataField();
				newField.setId(name);
				newField.setType(translateField(type));
				fields.add(newField);
			}
		}
		return fields;
	}

	private String translateField(String ontologyFieldType) {
		switch (ontologyFieldType) {
		case "string":
			return "text";
		case "number":
			return "float";
		case "object":
			return "json";
		case "integer":
			return "int";
		case "geometry-point":
			return "point";
		case "geometry-linestring":
			return "line";
		case "geometry-polygon":
			return "polygon";
		case "geometry-multipoint":
			return "geometry(Multipoint, 4326)";
		case "geometry-multilinestring":
			return "geometry(Multilinestring, 4326)";
		case "geometry-multipolygon":
			return "geometry(Multipolygon, 4326)";
		case "file":
			return "bytea";
		case "date":
			return "date";
		case "timestamp":
			return "timestamp";

		case "array":
			return "text";
		case "boolean":
			return "bool";
		default:
			return null;
		}
	}

	@Override
	public OpenDataResourceDTO getDTOFromResource(OpenDataResource resource, DatasetResource configResource,
			String dataset) {
		final List<DatasetResource> configResources = new ArrayList<>();
		if (configResource != null) {
			configResources.add(configResource);
		}
		return getDTOFromResource(resource, configResources, dataset);
	}

	@Override
	public String getJsonFromFile(MultipartFile file) {
		String jsonData = null;
		try {
			final String contentType = file.getContentType();
			final String fileName = file.getOriginalFilename();
			final InputStreamReader inputStream = new InputStreamReader(file.getInputStream());

			if (contentType.equals("text/csv") || fileName.contains(".csv")) {
				jsonData = getJsonFromCSV(inputStream);
			} else if (contentType.equals("text/xml") || contentType.equals("application/xml")
					|| fileName.contains(".xml")) {
				jsonData = getJsonFromXML(inputStream);
			} else if (contentType.equals("application/json") || fileName.contains(".json")) {
				final BufferedReader reader = new BufferedReader(inputStream);
				final StringBuilder responseStrBuilder = new StringBuilder();
				String str;
				while ((str = reader.readLine()) != null) {
					responseStrBuilder.append(str);
				}
				jsonData = responseStrBuilder.toString();
			}

			if (jsonData != null && !jsonData.equals("") && jsonData.substring(0, 1).equals("{")) {
				final Map<String, Object> obj = mapper.readValue(jsonData, new TypeReference<Map<String, Object>>() {
				});
				final List<Map<String, Object>> result = processMap(obj);
				jsonData = mapper.writeValueAsString(result);
			}
			return jsonData;
		} catch (final IOException e1) {
			e1.printStackTrace();
			return jsonData;
		}
	}

	@Override
	public String getFirstElement(String jsonData) {
		final JSONArray jsonArray = new JSONArray(jsonData);
		final JSONObject firstElement = jsonArray.getJSONObject(0);
		return firstElement.toString();
	}

	@Override
	public OpenDataPlatformResourceType getPlatformResourceTypeFromUrl(String resourceUrl) {
		final Dashboard dashboard = checkDashboardResource(resourceUrl);
		if (dashboard != null) {
			return OpenDataPlatformResourceType.DASHBOARD;
		}
		final com.minsait.onesait.platform.config.model.Api api = checkApiResource(resourceUrl);
		if (api != null) {
			return OpenDataPlatformResourceType.API;
		}
		final Viewer viewer = checkViewerResource(resourceUrl);
		if (viewer != null) {
			return OpenDataPlatformResourceType.VIEWER;
		}
		return null;
	}
}
