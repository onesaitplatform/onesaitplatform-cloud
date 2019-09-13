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
package com.minsait.onesait.platform.config.services.dashboard;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.commons.metrics.MetricsManager;
import com.minsait.onesait.platform.config.model.Category;
import com.minsait.onesait.platform.config.model.CategoryRelation;
import com.minsait.onesait.platform.config.model.Dashboard;
import com.minsait.onesait.platform.config.model.DashboardUserAccess;
import com.minsait.onesait.platform.config.model.DashboardUserAccessType;
import com.minsait.onesait.platform.config.model.Gadget;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.model.GadgetMeasure;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess.ResourceAccessType;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.Subcategory;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.CategoryRelationRepository;
import com.minsait.onesait.platform.config.repository.CategoryRepository;
import com.minsait.onesait.platform.config.repository.DashboardConfRepository;
import com.minsait.onesait.platform.config.repository.DashboardRepository;
import com.minsait.onesait.platform.config.repository.DashboardUserAccessRepository;
import com.minsait.onesait.platform.config.repository.DashboardUserAccessTypeRepository;
import com.minsait.onesait.platform.config.repository.GadgetDatasourceRepository;
import com.minsait.onesait.platform.config.repository.GadgetMeasureRepository;
import com.minsait.onesait.platform.config.repository.GadgetRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.SubcategoryRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardAccessDTO;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardCreateDTO;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardDTO;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardExportDTO;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardOrder;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardSimplifiedDTO;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardUserAccessDTO;
import com.minsait.onesait.platform.config.services.exceptions.DashboardServiceException;
import com.minsait.onesait.platform.config.services.exceptions.OPResourceServiceException;
import com.minsait.onesait.platform.config.services.gadget.dto.GadgetDTO;
import com.minsait.onesait.platform.config.services.gadget.dto.GadgetDatasourceDTO;
import com.minsait.onesait.platform.config.services.gadget.dto.GadgetMeasureDTO;
import com.minsait.onesait.platform.config.services.gadget.dto.OntologyDTO;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DashboardServiceImpl implements DashboardService {

	@Autowired
	private DashboardRepository dashboardRepository;
	@Autowired
	private DashboardUserAccessRepository dashboardUserAccessRepository;
	@Autowired
	private DashboardUserAccessTypeRepository dashboardUserAccessTypeRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private CategoryRepository categoryRepository;
	@Autowired
	private SubcategoryRepository subcategoryRepository;
	@Autowired
	private CategoryRelationRepository categoryRelationRepository;
	@Autowired
	private OPResourceService resourceService;
	@Autowired
	private DashboardConfRepository dashboardConfRepository;
	@Autowired
	private GadgetRepository gadgetRepository;
	@Autowired
	private GadgetDatasourceRepository gadgetDatasourceRepository;
	@Autowired
	private GadgetMeasureRepository gadgetMeasureRepository;
	@Autowired
	private OntologyRepository ontologyRepository;
	@Autowired(required = false)
	private MetricsManager metricsManager;


	@Value("${onesaitplatform.controlpanel.url:http://localhost:18000/controlpanel}")
	private String basePath;

	protected ObjectMapper objectMapper;

	@Value("${onesaitplatform.dashboardengine.url.view:http://localhost:8087/controlpanel/dashboards/viewiframe/}")
	private String prefixURLView;

	@Value("${onesaitplatform.dashboard.export.url:http://dashboardexport:26000}")
	private String dashboardexporturl;

	@PostConstruct
	public void init() {
		objectMapper = new ObjectMapper();
		objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
	}

	private static final String ANONYMOUSUSER = "anonymousUser";
	private static final String AUTH_PARSE_EXCEPT = "Authorizations parse Exception";
	private static final String DASH_NOT_EXIST = "Dashboard does not exist in the database";
	private static final String CATEGORY_SUBCATEGORY_NOTFOUND = "Category and subcategory not found";

	private static final String DASH_CREATE_AUTH_EXCEPT = "You do not have authorization to create dashboards";
	private static final String TO_IMG = "%s/imgfromurl";
	private static final String TO_PDF = "%s/pdffromurl";
	private static final String URI_POST_ERROR = "The URI of the endpoint is invalid in creation POST";
	private static final String URI_POST2_ERROR = "The URI of the endpoint is invalid in creation POST: ";
	private static final String POST_ERROR = "Exception in POST in creation POST";
	private static final String POST2_ERROR = "Exception in POST in creation POST: ";
	private static final String DUPLICATE_DASHBOARD_NAME = "Error duplicate dashboard name";
	private static final String POST_EXECUTING_ERROR = "Exception executing creation POST, status code: ";
	private static final String IDENTIFICATION = "identification";
	private static final String PAGES = "pages";
	private static final String LAYERS = "layers";
	private static final String GRIDBOARD = "gridboard";
	private static final String DATASOURCE = "datasource";
	private static final String ERROR_SAVING_DASHBOARD_FORBIDDEN = "Cannot update Dashboard that does not exist or don't have permission";
	private static final String JSON_PARSE_EXCEPTION = "Json parse exception";
	private static final String JSON_MAPPING_EXCEPTION = "Json mapping exception";
	private static final String IO_EXCEPTION = "IO exception";

	@Override
	public List<DashboardDTO> findDashboardWithIdentificationAndDescription(String identification, String description,
			String userId) {
		List<Dashboard> dashboards;
		final User sessionUser = userRepository.findByUserId(userId);

		description = description == null ? "" : description;
		identification = identification == null ? "" : identification;

		if (sessionUser.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			dashboards = dashboardRepository.findByIdentificationContainingAndDescriptionContaining(identification,
					description);
		} else {
			dashboards = dashboardRepository
					.findByUserAndPermissionsANDIdentificationContainingAndDescriptionContaining(sessionUser,
							identification, description);
		}

		return dashboards.stream().map(temp -> {
			final DashboardDTO obj = new DashboardDTO();
			obj.setCreatedAt(temp.getCreatedAt());
			obj.setDescription(temp.getDescription());
			obj.setId(temp.getId());
			obj.setIdentification(temp.getIdentification());
			if (null != temp.getImage()) {
				obj.setHasImage(Boolean.TRUE);
			} else {
				obj.setHasImage(Boolean.FALSE);
			}
			obj.setPublic(temp.isPublic());
			obj.setUpdatedAt(temp.getUpdatedAt());
			obj.setUser(temp.getUser());
			obj.setUserAccessType(getUserTypePermissionForDashboard(temp, sessionUser));
			obj.setType(temp.getType());
			return obj;
		}).collect(Collectors.toList());

	}

	@Override
	public List<String> getAllIdentifications() {
		final List<Dashboard> dashboards = dashboardRepository.findAllByOrderByIdentificationAsc();
		final List<String> identifications = new ArrayList<>();
		for (final Dashboard dashboard : dashboards) {
			identifications.add(dashboard.getIdentification());

		}
		return identifications;
	}

	@Transactional
	@Override
	public void deleteDashboard(String dashboardId, String userId) {
		final Dashboard dashboard = dashboardRepository.findById(dashboardId);
		if (dashboard != null && hasUserEditPermission(dashboardId, userId)) {
			if (resourceService.isResourceSharedInAnyProject(dashboard))
				throw new OPResourceServiceException(
						"This Dashboard is shared within a Project, revoke access from project prior to deleting");
			final CategoryRelation categoryRelation = categoryRelationRepository.findByTypeId(dashboard.getId());
			if (categoryRelation != null) {

				categoryRelationRepository.delete(categoryRelation);
			}
			dashboardUserAccessRepository.deleteByDashboard(dashboard);
			dashboardRepository.delete(dashboard);
		} else {
			throw new DashboardServiceException("Cannot delete dashboard that does not exist");
		}

	}

	@Transactional
	@Override
	public String deleteDashboardAccess(String dashboardId, String userId) {

		final Dashboard d = dashboardRepository.findById(dashboardId);
		if (resourceService.isResourceSharedInAnyProject(d))
			throw new OPResourceServiceException(
					"This Dashboard is shared within a Project, revoke access from project prior to deleting");
		dashboardUserAccessRepository.deleteByDashboard(d);
		return d.getId();

	}

	@Override
	public String deleteDashboardUserAccess(List<DashboardUserAccessDTO> dtos, String dashboardIdentification,
			boolean deleteAll) {
		JSONObject response = new JSONObject();
		for (DashboardUserAccessDTO dto : dtos) {
			String key = dto.getUserId();
			String value = "";
			String error = "ERROR: Invalid input data:";
			boolean e = false;

			User user = userRepository.findByUserId(dto.getUserId());
			if (user == null) {
				error += "User not found.";
				e = true;
			}
			DashboardUserAccess dashUA = null;
			if (deleteAll) {
				dashUA = getDashboardUserAccessByIdentificationAndUser(dashboardIdentification, user);
			} 
			else {
				List<DashboardUserAccessType> accessType = dashboardUserAccessTypeRepository
						.findByName(dto.getAccessType());
				if (accessType == null || accessType.isEmpty()) {
					error += "The access type does not exist.";
					e = true;
				} else {
					dashUA = getDashboardUserAccessByIdentificationAndUserAndAccessType(dashboardIdentification, user,
							accessType.get(0));
				}
			}
			if (dashUA == null) {
				error += "The authorization does not exist.";
				e = true;
			}
			if (!e) {
				dashboardUserAccessRepository.delete(dashUA);
				value = "OK";
			} else {
				value = error;
			}
			response.put(key, value);
		}
		return response.toString();
	}

	@Override
	public boolean hasUserPermission(String id, String userId) {
		final User user = userRepository.findByUserId(userId);
		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return true;
		} else {
			return dashboardRepository.findById(id).getUser().getUserId().equals(userId);
		}
	}

	@Override
	public boolean hasUserEditPermission(String id, String userId) {
		final User user = userRepository.findByUserId(userId);
		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return true;
		} else {
			final boolean propietary = dashboardRepository.findById(id).getUser().getUserId().equals(userId);
			if (propietary) {
				return true;
			}
			final DashboardUserAccess userAuthorization = dashboardUserAccessRepository
					.findByDashboardAndUser(dashboardRepository.findById(id), user);

			if (userAuthorization != null) {
				switch (DashboardUserAccessType.Type
						.valueOf(userAuthorization.getDashboardUserAccessType().getName())) {
				case EDIT:
					return true;
				case VIEW:
				default:
					return false;
				}
			} else {
				return resourceService.hasAccess(userId, id, ResourceAccessType.MANAGE);
			}

		}
	}

	@Override
	public boolean hasUserViewPermission(String id, String userId) {
		final User user = userRepository.findByUserId(userId);

		if (dashboardRepository.findById(id).isPublic()) {
			return true;
		} else if (userId.equals(ANONYMOUSUSER) || user == null) {
			return dashboardRepository.findById(id).isPublic();
		} else if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return true;
		} else {
			final boolean propietary = dashboardRepository.findById(id).getUser().getUserId().equals(userId);
			if (propietary) {
				return true;
			}
			final DashboardUserAccess userAuthorization = dashboardUserAccessRepository
					.findByDashboardAndUser(dashboardRepository.findById(id), user);

			if (userAuthorization != null) {
				switch (DashboardUserAccessType.Type
						.valueOf(userAuthorization.getDashboardUserAccessType().getName())) {
				case EDIT:
					return true;
				case VIEW:
					return true;
				default:
					return false;
				}
			} else {
				return resourceService.hasAccess(userId, id, ResourceAccessType.VIEW);
			}

		}
	}

	public String getUserTypePermissionForDashboard(Dashboard dashboard, User user) {

		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())
				|| dashboard.getUser().getUserId().equals(user.getUserId())) {
			return DashboardUserAccessType.Type.EDIT.toString();
		}

		final DashboardUserAccess userAuthorization = dashboardUserAccessRepository.findByDashboardAndUser(dashboard,
				user);

		if (userAuthorization != null) {
			switch (DashboardUserAccessType.Type.valueOf(userAuthorization.getDashboardUserAccessType().getName())) {
			case EDIT:
				return DashboardUserAccessType.Type.EDIT.toString();
			case VIEW:
				return DashboardUserAccessType.Type.VIEW.toString();
			default:
				return DashboardUserAccessType.Type.VIEW.toString();
			}
		} else {
			if (resourceService.getResourceAccess(user.getUserId(), dashboard.getId()) != null) {
				switch (resourceService.getResourceAccess(user.getUserId(), dashboard.getId())) {
				case MANAGE:
					return DashboardUserAccessType.Type.EDIT.toString();
				case VIEW:
				default:
					return DashboardUserAccessType.Type.VIEW.toString();
				}
			}

		}

		return DashboardUserAccessType.Type.VIEW.toString();
	}

	@Override
	public void saveDashboard(String id, Dashboard dashboard, String userId) {
		if (hasUserEditPermission(id, userId)) {
			final Dashboard dashboardEnt = dashboardRepository.findById(dashboard.getId());
			dashboardEnt.setCustomcss(dashboard.getCustomcss());
			dashboardEnt.setCustomjs(dashboard.getCustomjs());
			dashboardEnt.setDescription(dashboard.getDescription());
			dashboardEnt.setJsoni18n(dashboard.getJsoni18n());
			dashboardEnt.setModel(dashboard.getModel());
			dashboardEnt.setPublic(dashboard.isPublic());
			dashboardEnt.setHeaderlibs(dashboard.getHeaderlibs());
			dashboardEnt.setType(dashboard.getType());
			dashboardRepository.save(dashboardEnt);
		} else {
			throw new DashboardServiceException(ERROR_SAVING_DASHBOARD_FORBIDDEN);
		}
	}

	@Override
	public void saveDashboardModel(String id, String model, String userId) {
		if (hasUserEditPermission(id, userId)) {
			final Dashboard dashboardEnt = dashboardRepository.findById(id);
			dashboardEnt.setModel(model);

			dashboardRepository.save(dashboardEnt);
		} else {
			throw new DashboardServiceException(ERROR_SAVING_DASHBOARD_FORBIDDEN);
		}
	}

	@Override
	public Dashboard getDashboardById(String id, String userId) {
		return dashboardRepository.findById(id);
	}

	@Override
	public Dashboard getDashboardByIdentification(String identification, String userId) {
		if (!dashboardRepository.findByIdentification(identification).isEmpty())
			return dashboardRepository.findByIdentification(identification).get(0);
		else
			return null;
	}

	@Override
	public Dashboard getDashboardEditById(String id, String userId) {
		if (hasUserEditPermission(id, userId)) {
			return dashboardRepository.findById(id);
		}
		throw new DashboardServiceException("Cannot view Dashboard that does not exist or don't have permission");
	}

	@Override
	public String getCredentialsString(String userId) {
		final User user = userRepository.findByUserId(userId);
		return user.getUserId();
	}

	@Override
	public boolean dashboardExists(String identification) {
		return !CollectionUtils.isEmpty(dashboardRepository.findByIdentification(identification));
	}

	@Override
	public boolean dashboardExistsById(String id) {
		final Dashboard dash = dashboardRepository.findById(id);
		return dash != null && dash.getId().length() != 0;
	}

	@Override
	public String cloneDashboard(Dashboard originalDashboard, String identification, User user) {
		final Dashboard cloneDashboard = new Dashboard();

		try {

			cloneDashboard.setIdentification(identification);
			cloneDashboard.setUser(user);
			cloneDashboard.setCustomcss(originalDashboard.getCustomcss());
			cloneDashboard.setCustomjs(originalDashboard.getCustomjs());
			cloneDashboard.setDescription(originalDashboard.getDescription());
			cloneDashboard.setHeaderlibs(originalDashboard.getHeaderlibs());
			cloneDashboard.setImage(originalDashboard.getImage());
			cloneDashboard.setPublic(originalDashboard.isPublic());
			cloneDashboard.setJsoni18n(originalDashboard.getJsoni18n());
			cloneDashboard.setModel(originalDashboard.getModel());
			cloneDashboard.setType(originalDashboard.getType());

			dashboardRepository.save(cloneDashboard);

			return cloneDashboard.getId();
		} catch (final Exception e) {

			log.error(e.getMessage());
			return null;
		}
	}

	private Dashboard getNewDashboard(DashboardCreateDTO dashboard, String userId) {
		log.debug("Dashboard no exist, creating...");
		final Dashboard d = new Dashboard();
		d.setCustomcss("");
		d.setCustomjs("");
		d.setJsoni18n("");
		try {
			d.setImage(dashboard.getImage() != null ? dashboard.getImage().getBytes() : null);
		} catch (final IOException e1) {
			log.error("Could not read image");
		}	

		User sessionUser = userRepository.findByUserId(userId);
		if (sessionUser.getRole().getId().equals(Role.Type.ROLE_USER.toString())
				|| sessionUser.getRole().getId().equals(Role.Type.ROLE_DATAVIEWER.toString())) {
			this.metricsManagerLogControlPanelDashboardsCreation(userId, "KO");
			throw new DashboardServiceException(DASH_CREATE_AUTH_EXCEPT);
		}
		d.setDescription(dashboard.getDescription());
		d.setIdentification(dashboard.getIdentification());
		d.setPublic(dashboard.getPublicAccess());
		d.setUser(userRepository.findByUserId(userId));
		d.setHeaderlibs(dashboard.getHeaderlibs());
		d.setType(dashboard.getType());
		String model = null;

		if (dashboard.getDashboardConfId() == null) {
			model = dashboardConfRepository.findByIdentification("default").get(0).getModel();
		} else {
			model = dashboardConfRepository.findById(dashboard.getDashboardConfId()).getModel();
		}
		d.setModel(model);

		return dashboardRepository.save(d);
	}

	private void createCategoryRelation(DashboardCreateDTO dashboard, String id) {
		try {
			final Category category = categoryRepository.findByIdentification(dashboard.getCategory()).get(0);
			final Subcategory subcategory = subcategoryRepository.findByIdentification(dashboard.getSubcategory())
					.get(0);

			final CategoryRelation categoryRelation = new CategoryRelation();
			categoryRelation.setCategory(category.getId());
			categoryRelation.setSubcategory(subcategory.getId());
			categoryRelation.setType(CategoryRelation.Type.DASHBOARD);
			categoryRelation.setTypeId(id);

			categoryRelationRepository.save(categoryRelation);
		} catch (final Exception e) {
			log.error("Category or Subcategory not found:");
			throw new DashboardServiceException(CATEGORY_SUBCATEGORY_NOTFOUND);
		}
	}

	private void createCategoryRelation(DashboardExportDTO dashboard, String id) {
		try {
			final Category category = categoryRepository.findByIdentification(dashboard.getCategory()).get(0);
			final Subcategory subcategory = subcategoryRepository.findByIdentification(dashboard.getSubcategory())
					.get(0);

			final CategoryRelation categoryRelation = new CategoryRelation();
			categoryRelation.setCategory(category.getId());
			categoryRelation.setSubcategory(subcategory.getId());
			categoryRelation.setType(CategoryRelation.Type.DASHBOARD);
			categoryRelation.setTypeId(id);

			categoryRelationRepository.save(categoryRelation);
		} catch (final Exception e) {
			log.error("Category or Subcategory not found:");
			throw new DashboardServiceException(CATEGORY_SUBCATEGORY_NOTFOUND);
		}
	}

	@Override
	public String createNewDashboard(DashboardCreateDTO dashboard, String userId) {

		final User sessionUser = userRepository.findByUserId(userId);
		if (sessionUser.getRole().getId().equals(Role.Type.ROLE_USER.toString())
				|| sessionUser.getRole().getId().equals(Role.Type.ROLE_DATAVIEWER.toString())) {
			this.metricsManagerLogControlPanelDashboardsCreation(userId, "KO");
			throw new DashboardServiceException(DASH_CREATE_AUTH_EXCEPT);
		}

		if (dashboardExists(dashboard.getIdentification()))
			throw new DashboardServiceException("Dashboard already exists in Database");

		final Dashboard dAux = getNewDashboard(dashboard, userId);

		if (!StringUtils.isEmpty(dashboard.getCategory()) && !StringUtils.isEmpty(dashboard.getSubcategory())
				&& categoryRelationRepository.findByTypeId(dAux.getId()) != null) {
			createCategoryRelation(dashboard, dAux.getId());

		}

		try {
			if (dashboard.getAuthorizations() != null) {
				final List<DashboardAccessDTO> access = objectMapper.readValue(dashboard.getAuthorizations(),
						objectMapper.getTypeFactory().constructCollectionType(List.class, DashboardAccessDTO.class));
				for (final Iterator<DashboardAccessDTO> iterator = access.iterator(); iterator.hasNext();) {
					final DashboardAccessDTO dashboardAccessDTO = iterator.next();
					final DashboardUserAccess dua = new DashboardUserAccess();
					dua.setDashboard(dAux);
					final List<DashboardUserAccessType> managedTypes = dashboardUserAccessTypeRepository
							.findByName(dashboardAccessDTO.getAccesstypes());
					final DashboardUserAccessType managedType = managedTypes != null
							&& !CollectionUtils.isEmpty(managedTypes) ? managedTypes.get(0) : null;
					dua.setDashboardUserAccessType(managedType);
					dua.setUser(userRepository.findByUserId(dashboardAccessDTO.getUsers()));
					dashboardUserAccessRepository.save(dua);
				}

			}

		} catch (final Exception e) {
			throw new DashboardServiceException(AUTH_PARSE_EXCEPT);
		}

		return dAux.getId();

	}


	@Override
	public List<DashboardUserAccess> getDashboardUserAccesses(Dashboard dashboard) {
		return dashboardUserAccessRepository.findByDashboard(dashboard);
	}

	@Override
	public DashboardUserAccess getDashboardUserAccessByIdentificationAndUser(String identification, User user) {
		final Dashboard dashboard = dashboardRepository.findByIdentification(identification).get(0);
		return dashboardUserAccessRepository.findByDashboardAndUser(dashboard, user);
	}

	public DashboardUserAccess getDashboardUserAccessByIdentificationAndUserAndAccessType(String identification,
			User user, DashboardUserAccessType accessType) {
		final Dashboard dashboard = dashboardRepository.findByIdentification(identification).get(0);
		return dashboardUserAccessRepository.findByDashboardAndUserAndDashboardUserAccessType(dashboard, user,
				accessType);
	}

	@Override
	public String insertDashboardUserAccess(Dashboard dashboard, List<DashboardUserAccessDTO> dtos, boolean updated) {
		JSONObject response = new JSONObject();
		for (DashboardUserAccessDTO dto : dtos) {
			String key = dto.getUserId();
			String value = "";
			String error = "ERROR. Invalid input data: ";
			boolean e = false;
			User user = userRepository.findByUserId(dto.getUserId());
			if (user == null) {
				error += "User not found.";
				e = true;
			}
			DashboardUserAccess dUA = getDashboardUserAccessByIdentificationAndUser(dashboard.getIdentification(),
					user);
			if (!updated && dUA != null) {
				error += "The authorization already exists.";
				e = true;
			}
			if (updated && dUA == null) {
				error += "The authorization does not exist.";
				e = true;
			}

			List<DashboardUserAccessType> accessType = dashboardUserAccessTypeRepository
					.findByName(dto.getAccessType());
			if (accessType == null || accessType.isEmpty()) {
				error += "Access type not found.";
				e = true;
			}
			if (!e) {
				DashboardUserAccess uA;

				Date currentDate = new Date();
				if (updated) {
					uA = dUA;
					uA.setDashboardUserAccessType(accessType.get(0));
					uA.setUpdatedAt(currentDate);
				} else {
					uA = new DashboardUserAccess();
					uA.setDashboard(dashboard);
					uA.setDashboardUserAccessType(accessType.get(0));
					uA.setUser(user);
					uA.setCreatedAt(currentDate);
					uA.setUpdatedAt(currentDate);
				}
				dashboardUserAccessRepository.save(uA);
				value = "OK";
			} else {
				value = error;
			}
			response.put(key, value);
		}
		return response.toString();

	}

	@Transactional
	@Override
	public String cleanDashboardAccess(DashboardCreateDTO dashboard, String userId) {
		if (!dashboardExistsById(dashboard.getId())) {
			throw new DashboardServiceException(DASH_NOT_EXIST);
		} else {

			final Dashboard d = dashboardRepository.findById(dashboard.getId());
			dashboardUserAccessRepository.deleteByDashboard(d);
			return d.getId();

		}
	}

	@Transactional
	@Override
	public String saveUpdateAccess(DashboardCreateDTO dashboard, String userId) {
		if (!dashboardExistsById(dashboard.getId())) {
			throw new DashboardServiceException(DASH_NOT_EXIST);
		} else {

			final Dashboard d = dashboardRepository.findById(dashboard.getId());

			try {
				if (dashboard.getAuthorizations() != null) {
					final List<DashboardAccessDTO> access = objectMapper.readValue(dashboard.getAuthorizations(),
							objectMapper.getTypeFactory().constructCollectionType(List.class,
									DashboardAccessDTO.class));
					for (final Iterator<DashboardAccessDTO> iterator = access.iterator(); iterator.hasNext();) {
						final DashboardAccessDTO dashboardAccessDTO = iterator.next();
						final DashboardUserAccess dua = new DashboardUserAccess();
						dua.setDashboard(dashboardRepository.findById(dashboard.getId()));
						final List<DashboardUserAccessType> managedTypes = dashboardUserAccessTypeRepository
								.findByName(dashboardAccessDTO.getAccesstypes());
						final DashboardUserAccessType managedType = managedTypes != null
								&& !CollectionUtils.isEmpty(managedTypes) ? managedTypes.get(0) : null;
						dua.setDashboardUserAccessType(managedType);
						dua.setUser(userRepository.findByUserId(dashboardAccessDTO.getUsers()));
						dashboardUserAccessRepository.save(dua);
					}
				}
				return d.getId();

			} catch (final IOException e) {

				throw new DashboardServiceException(AUTH_PARSE_EXCEPT);
			}

		}
	}

	@Transactional
	@Override
	public String updatePublicDashboard(DashboardCreateDTO dashboard, String userId) {
		if (!dashboardExistsById(dashboard.getId())) {
			throw new DashboardServiceException(DASH_NOT_EXIST);
		} else {
			final Dashboard d = dashboardRepository.findById(dashboard.getId());
			d.setPublic(dashboard.getPublicAccess());
			d.setDescription(dashboard.getDescription());
			d.setHeaderlibs(dashboard.getHeaderlibs());
			d.setIdentification(dashboard.getIdentification());
			try {
				if (dashboard.getImage() != null && !dashboard.getImage().isEmpty()) {
					d.setImage(dashboard.getImage().getBytes());
				} else {
					d.setImage(null);
				}
			} catch (final IOException e) {
				log.error(e.getMessage());
			}
			final Dashboard dAux = dashboardRepository.save(d);

			if (dashboard.getCategory() != null && dashboard.getSubcategory() != null
					&& !dashboard.getCategory().isEmpty() && !dashboard.getSubcategory().isEmpty()) {

				CategoryRelation categoryRelation = categoryRelationRepository.findByTypeId(d.getId());

				if (categoryRelation == null) {
					categoryRelation = new CategoryRelation();
				}

				categoryRelation.setCategory(
					categoryRepository.findByIdentification(dashboard.getCategory()).get(0).getId());
				categoryRelation.setSubcategory(
					subcategoryRepository.findByIdentification(dashboard.getSubcategory()).get(0).getId());
				categoryRelation.setType(CategoryRelation.Type.DASHBOARD);
				categoryRelation.setTypeId(dAux.getId());

				categoryRelationRepository.save(categoryRelation);
			}

			return d.getId();
		}
	}

	@Override
	public byte[] getImgBytes(String id) {
		final Dashboard d = dashboardRepository.findById(id);

		return d.getImage();
	}

	@Override
	public List<Dashboard> getByUserId(String userId) {
		final User sessionUser = userRepository.findByUserId(userId);
		if (sessionUser.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return dashboardRepository.findAllByOrderByIdentificationAsc();
		} else {
			return dashboardRepository.findByUserOrderByIdentificationAsc(sessionUser);
		}
	}

	@Transactional
	@Override
	public void updateDashboardSimplified(String id, DashboardSimplifiedDTO dashboard, String userId) {
		if (hasUserEditPermission(id, userId)) {
			final Dashboard dashboardEnt = dashboardRepository.findById(id);
			if (dashboardEnt != null) {
				if (dashboard.getDescription() != null)
					dashboardEnt.setDescription(dashboard.getDescription());
				if (dashboard.getIdentification() != null)
					dashboardEnt.setIdentification(dashboard.getIdentification());
				dashboardEnt.setPublic(dashboard.isPublic());
				dashboardRepository.save(dashboardEnt);
			} else {
				throw new DashboardServiceException(DASH_NOT_EXIST);
			}
		} else {
			throw new DashboardServiceException(ERROR_SAVING_DASHBOARD_FORBIDDEN);
		}
	}

	@Override
	public List<Dashboard> getByUserIdOrdered(String userId, DashboardOrder order) {
		final User sessionUser = userRepository.findByUserId(userId);
		if (sessionUser.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return dashboardRepository.findAllByOrderByIdentificationAsc();
		}
		switch (order) {
		case CREATED_AT_ASC:
			return dashboardRepository.findByUserOrderByCreatedAtAsc(sessionUser);
		case CREATED_AT_DESC:
			return dashboardRepository.findByUserOrderByCreatedAtDesc(sessionUser);
		case MODIFIED_AT_ASC:
			return dashboardRepository.findByUserOrderByUpdatedAtAsc(sessionUser);
		case MODIFIED_AT_DESC:
			return dashboardRepository.findByUserOrderByUpdatedAtDesc(sessionUser);
		case IDENTIFICATION_DESC:
			return dashboardRepository.findByUserOrderByIdentificationDesc(sessionUser);
		case IDENTIFICATION_ASC:
		default:
			return dashboardRepository.findByUserOrderByIdentificationAsc(sessionUser);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public int getNumGadgets(Dashboard dashboard) {
		final String model = dashboard.getModel();
		int ngadgets = 0;
		try {
			final Map<String, Object> obj = objectMapper.readValue(model, new TypeReference<Map<String, Object>>() {
			});
			if (obj.containsKey(PAGES)) {
				final ArrayList<Object> pages = (ArrayList<Object>) obj.get(PAGES);
				final int npages = pages.size();
				for (int i = 0; i < npages; i++) {
					final Map<String, Object> page = (Map<String, Object>) pages.get(i);
					final ArrayList<Object> layers = (ArrayList<Object>) page.get(LAYERS);
					final Map<String, Object> layer = (Map<String, Object>) layers.get(0);
					final ArrayList<Object> gridboard = (ArrayList<Object>) layer.get(GRIDBOARD);
					ngadgets += gridboard.size();
				}
				ngadgets -= 1;
			}
		} catch (final JsonParseException e) {
			log.error(JSON_PARSE_EXCEPTION, e);
		} catch (final JsonMappingException e) {
			log.error(JSON_MAPPING_EXCEPTION, e);
		} catch (final IOException e) {
			log.error(IO_EXCEPTION, e);
		}

		return ngadgets;
	}

	@SuppressWarnings("unchecked")
	@Override
	public DashboardExportDTO addGadgets(DashboardExportDTO dashboard) {
		final ArrayList<String> listGadgetsID = new ArrayList<>();
		final ArrayList<String> listDatasourcesID = new ArrayList<>();
		final ArrayList<String> listGadgetMeasuresID = new ArrayList<>();
		try {
			final Map<String, Object> obj = objectMapper.readValue(dashboard.getModel(),
					new TypeReference<Map<String, Object>>() {
					});
			if (obj.containsKey(PAGES)) {

				((ArrayList<Object>) obj.get(PAGES)).forEach(o -> {
					final Map<String, Object> page = (Map<String, Object>) o;
					addGridBoardFromPage(page, listDatasourcesID, listGadgetMeasuresID, listGadgetsID);

				});
			}
		} catch (final Exception e) {
			log.error(JSON_PARSE_EXCEPTION, e);
		}

		dashboard.setGadgets(
				listGadgetsID.stream().map(this::gadgetToDTO).filter(Objects::nonNull).collect(Collectors.toList()));
		dashboard.setGadgetDatasources(listDatasourcesID.stream().map(this::gadgetDatasourceToDTO)
				.filter(Objects::nonNull).collect(Collectors.toList()));
		dashboard.setGadgetMeasures(listGadgetMeasuresID.stream().map(this::gadgetMeasureToDTO).filter(Objects::nonNull)
				.collect(Collectors.toList()));

		return dashboard;
	}

	@SuppressWarnings("unchecked")
	private void addGridBoardFromPage(Map<String, Object> page, List<String> listDatasourcesID,
			List<String> listGadgetMeasuresID, List<String> listGadgetsID) {
		final ArrayList<Object> layers = (ArrayList<Object>) page.get(LAYERS);
		final Map<String, Object> layer = (Map<String, Object>) layers.get(0);
		final ArrayList<Object> gridboard = (ArrayList<Object>) layer.get(GRIDBOARD);
		gridboard.forEach(ob -> {
			final Map<String, Object> gadget = (Map<String, Object>) ob;
			if (gadget.containsKey(DATASOURCE)) {
				final Map<String, Object> datasource = (Map<String, Object>) gadget.get(DATASOURCE);
				final String datasourceId = (String) datasource.get("id");
				listDatasourcesID.add(datasourceId);
			} else if (gadget.containsKey("id")) {
				final String gadgetId = (String) gadget.get("id");
				listGadgetsID.add(gadgetId);
				final List<GadgetMeasure> gadgetMeasures = gadgetMeasureRepository
						.findByGadget(gadgetRepository.findById(gadgetId));
				if (!CollectionUtils.isEmpty(gadgetMeasures)) {
					for (final GadgetMeasure gadgetMeasure : gadgetMeasures) {
						listGadgetMeasuresID.add(gadgetMeasure.getId());
					}
					final String datasource = gadgetMeasureRepository.findByGadget(gadgetRepository.findById(gadgetId))
							.get(0).getDatasource().getId();
					listDatasourcesID.add(datasource);
				}
			}
		});
	}

	@Override
	public String importDashboard(DashboardExportDTO dashboardimportDTO, String userId) {
		final Dashboard dashboard = new Dashboard();
		if (!dashboardExists(dashboardimportDTO.getIdentification())) {
			dashboard.setIdentification(dashboardimportDTO.getIdentification());
			String description = "";
			if (dashboardimportDTO.getDescription() != null) {
				description = dashboardimportDTO.getDescription();
			}
			dashboard.setDescription(description);
			dashboard.setPublic(dashboardimportDTO.isPublic());
			dashboard.setHeaderlibs(dashboardimportDTO.getHeaderlibs());
			dashboard.setCreatedAt(dashboardimportDTO.getCreatedAt());
			dashboard.setUpdatedAt(dashboardimportDTO.getModifiedAt());
			dashboard.setUser(userRepository.findByUserId(userId));
			dashboard.setCustomcss("");
			dashboard.setCustomjs("");
			dashboard.setJsoni18n("");
			dashboard.setType(dashboardimportDTO.getType());
			dashboard.setModel(dashboardimportDTO.getModel());

			final Dashboard dAux = dashboardRepository.save(dashboard);

			if (!StringUtils.isEmpty(dashboardimportDTO.getCategory())
					&& !StringUtils.isEmpty(dashboardimportDTO.getSubcategory())
					&& categoryRelationRepository.findByTypeId(dAux.getId()) != null) {
				this.createCategoryRelation(dashboardimportDTO, dashboard.getId());

			}

			// include DASH_AUTHS
			includeDashboardAuths(dashboardimportDTO, dashboard.getId());

			// include GADGETS
			includeGadgets(dashboardimportDTO, userId);

			// include GADGET_DATASOURCES
			includeGadgetDatasoures(dashboardimportDTO, userId);
			// include GADGET_MEASURES
			includeGadgetMeasures(dashboardimportDTO);

			return dashboardRepository.findByIdentification(dashboard.getIdentification()).get(0).getIdentification();

		} else {
			return "";
		}

	}

	private void includeDashboardAuths(DashboardExportDTO dashboard, String id) {
		for (final DashboardUserAccessDTO dashboardUADTO : dashboard.getDashboardAuths()) {
			final DashboardUserAccess dashboardUA = new DashboardUserAccess();
			dashboardUA.setDashboard(dashboardRepository.findById(id));
			final List<DashboardUserAccessType> managedTypes = dashboardUserAccessTypeRepository
					.findByName(dashboardUADTO.getAccessType());
			final DashboardUserAccessType managedType = managedTypes != null && !CollectionUtils.isEmpty(managedTypes)
					? managedTypes.get(0)
					: null;
			dashboardUA.setDashboardUserAccessType(managedType);
			dashboardUA.setUser(userRepository.findByUserId(dashboardUADTO.getUserId()));

			dashboardUserAccessRepository.save(dashboardUA);
		}
	}

	private void includeGadgets(DashboardExportDTO dashboard, String userId) {
		for (final GadgetDTO gadgetDTO : dashboard.getGadgets()) {
			final Gadget gadget = new Gadget();
			if (gadgetRepository.findById(gadgetDTO.getId()) == null) {
				gadget.setId(gadgetDTO.getId());
				gadget.setConfig(gadgetDTO.getConfig());
				gadget.setDescription(gadgetDTO.getDescription());
				gadget.setIdentification(gadgetDTO.getIdentification());
				gadget.setPublic(gadgetDTO.isPublic());
				gadget.setType(gadgetDTO.getType());
				gadget.setUser(userRepository.findByUserId(userId));

				gadgetRepository.save(gadget);
			}
		}
	}

	private void includeGadgetDatasoures(DashboardExportDTO dashboard, String userId) {
		for (final GadgetDatasourceDTO gadgetDSDTO : dashboard.getGadgetDatasources()) {
			final GadgetDatasource gadgetDS = new GadgetDatasource();
			if (gadgetDatasourceRepository.findById(gadgetDSDTO.getId()) == null) {
				gadgetDS.setId(gadgetDSDTO.getId());
				gadgetDS.setConfig(gadgetDSDTO.getConfig());
				gadgetDS.setDbtype(gadgetDSDTO.getDbtype());
				gadgetDS.setDescription(gadgetDSDTO.getDescription());
				gadgetDS.setIdentification(gadgetDSDTO.getIdentification());
				gadgetDS.setMaxvalues(gadgetDSDTO.getMaxvalues());
				gadgetDS.setMode(gadgetDSDTO.getMode());
				gadgetDS.setQuery(gadgetDSDTO.getQuery());
				gadgetDS.setRefresh(gadgetDSDTO.getRefresh());
				final OntologyDTO oDTO = gadgetDSDTO.getOntology();
				if (oDTO.getIdentification() != null
						&& ontologyRepository.findByIdentification(oDTO.getIdentification()) != null)
					gadgetDS.setOntology(ontologyRepository.findByIdentification(oDTO.getIdentification()));
				else
					gadgetDS.setOntology(null);
				gadgetDS.setUser(userRepository.findByUserId(userId));
				gadgetDatasourceRepository.save(gadgetDS);
			}
		}
	}

	private void includeGadgetMeasures(DashboardExportDTO dashboard) {
		for (final GadgetMeasureDTO gadgetMeasureDTO : dashboard.getGadgetMeasures()) {
			final GadgetMeasure gadgetMeasure = new GadgetMeasure();
			if (gadgetMeasureRepository.findById(gadgetMeasureDTO.getId()).isEmpty()) {
				gadgetMeasure.setId(gadgetMeasureDTO.getId());
				gadgetMeasure.setConfig(gadgetMeasureDTO.getConfig());
				gadgetMeasure.setGadget(gadgetRepository.findById(gadgetMeasureDTO.getGadget().getId()));
				gadgetMeasure
						.setDatasource(gadgetDatasourceRepository.findById(gadgetMeasureDTO.getDatasource().getId()));

				gadgetMeasureRepository.save(gadgetMeasure);
			}
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public String getElementsAssociated(String dashboardId) {
		final JSONArray elements = new JSONArray();

		final Dashboard dashboard = dashboardRepository.findById(dashboardId);
		final List<String> added = new ArrayList<>();

		try {
			final Map<String, Object> obj = objectMapper.readValue(dashboard.getModel(),
					new TypeReference<Map<String, Object>>() {
					});
			if (obj.containsKey(PAGES)) {
				((ArrayList<Object>) obj.get(PAGES)).forEach(o -> {
					final Map<String, Object> page = (Map<String, Object>) o;
					processPageForElements(page, elements, added);
				});
			}
		} catch (final JsonParseException e) {
			log.error(JSON_PARSE_EXCEPTION, e);
		} catch (final JsonMappingException e) {
			log.error(JSON_MAPPING_EXCEPTION, e);
		} catch (final IOException e) {
			log.error(IO_EXCEPTION, e);
		}
		return elements.toString();
	}

	@SuppressWarnings("unchecked")
	private void processPageForElements(Map<String, Object> page, JSONArray elements, List<String> added) {
		final ArrayList<Object> layers = (ArrayList<Object>) page.get(LAYERS);
		final Map<String, Object> layer = (Map<String, Object>) layers.get(0);
		((ArrayList<Object>) layer.get(GRIDBOARD)).forEach(o -> {
			final Map<String, Object> gridboard = (Map<String, Object>) o;
			processGridboardForElements(gridboard, elements, added);
		});

	}

	@SuppressWarnings("unchecked")
	private void processGridboardForElements(Map<String, Object> gadget, JSONArray elements, List<String> added) {

		if (gadget.containsKey(DATASOURCE)) {
			final Map<String, Object> datasource = (Map<String, Object>) gadget.get(DATASOURCE);
			final String datasourceId = (String) datasource.get("id");
			final GadgetDatasource datasourceObj = gadgetDatasourceRepository.findById(datasourceId);
			final JSONObject e = new JSONObject();
			e.put("id", datasourceObj.getId());
			e.put(IDENTIFICATION, datasourceObj.getIdentification());
			e.put("type", datasourceObj.getClass().getSimpleName());
			added.add(datasourceObj.getId());
			elements.put(e);
		} else if (gadget.containsKey("id") && !added.contains(gadget.get("id").toString())
				&& gadgetRepository.findById(gadget.get("id").toString()) != null) {
			final Gadget gadgetObj = gadgetRepository.findById(gadget.get("id").toString());
			JSONObject e = new JSONObject();
			e.put("id", gadgetObj.getId());
			e.put(IDENTIFICATION, gadgetObj.getIdentification());
			e.put("type", gadgetObj.getClass().getSimpleName());
			added.add(gadgetObj.getId());
			elements.put(e);
			final List<GadgetMeasure> gadgetMeasures = gadgetMeasureRepository
					.findByGadget(gadgetRepository.findById(gadgetObj.getId()));
			if (!gadgetMeasures.isEmpty()) {

				final GadgetDatasource datasourceObj = gadgetMeasureRepository
						.findByGadget(gadgetRepository.findById(gadgetObj.getId())).get(0).getDatasource();
				if (!added.contains(datasourceObj.getId())) {
					e = new JSONObject();
					e.put("id", datasourceObj.getId());
					e.put(IDENTIFICATION, datasourceObj.getIdentification());
					e.put("type", datasourceObj.getClass().getSimpleName());
					added.add(datasourceObj.getId());
					elements.put(e);
				}
				if (!added.contains(datasourceObj.getOntology().getId())) {
					e = new JSONObject();
					e.put("id", datasourceObj.getOntology().getId());
					e.put(IDENTIFICATION, datasourceObj.getOntology().getIdentification());
					e.put("type", datasourceObj.getOntology().getClass().getSimpleName());
					added.add(datasourceObj.getOntology().getId());
					elements.put(e);
				}
			}
		}
	}

	private GadgetDTO gadgetToDTO(String gadgetId) {
		final Gadget gadget = gadgetRepository.findById(gadgetId);
		final GadgetDTO gDto = new GadgetDTO();
		if (gadget != null) {
			gDto.setId(gadget.getId());
			gDto.setConfig(gadget.getConfig());
			gDto.setIdentification(gadget.getIdentification());
			gDto.setPublic(gadget.isPublic());
			gDto.setType(gadget.getType());
			gDto.setDescription(gadget.getDescription());
			return gDto;
		}
		return null;
	}

	private GadgetDatasourceDTO gadgetDatasourceToDTO(String gadgetDSId) {
		final GadgetDatasource gadgetds = gadgetDatasourceRepository.findById(gadgetDSId);
		final GadgetDatasourceDTO gDto = new GadgetDatasourceDTO();
		if (gadgetds != null) {
			gDto.setId(gadgetds.getId());
			gDto.setConfig(gadgetds.getConfig());
			gDto.setDescription(gadgetds.getDescription());
			gDto.setIdentification(gadgetds.getIdentification());
			gDto.setDbtype(gadgetds.getDbtype());
			gDto.setMaxvalues(gadgetds.getMaxvalues());
			gDto.setQuery(gadgetds.getQuery());
			gDto.setMode(gadgetds.getMode());
			gDto.setRefresh(gadgetds.getRefresh());
			final OntologyDTO oDTO = new OntologyDTO();
			if (gadgetds.getOntology() != null) {
				oDTO.setIdentification(gadgetds.getOntology().getIdentification());
				oDTO.setDescription(gadgetds.getOntology().getDescription());
				oDTO.setUser(gadgetds.getOntology().getUser().getUserId());
			}
			gDto.setOntology(oDTO);
			return gDto;
		}

		return null;
	}

	private GadgetMeasureDTO gadgetMeasureToDTO(String gadgetMeasuresId) {
		final GadgetMeasure gadgetMeasure = gadgetMeasureRepository.findOne(gadgetMeasuresId);
		final GadgetMeasureDTO gDto = new GadgetMeasureDTO();
		if (gadgetMeasure != null) {
			gDto.setId(gadgetMeasure.getId());
			gDto.setConfig(gadgetMeasure.getConfig());
			gDto.setGadget(gadgetToDTO(gadgetMeasure.getGadget().getId()));
			gDto.setDatasource(gadgetDatasourceToDTO(gadgetMeasure.getDatasource().getId()));
			return gDto;
		}
		return null;
	}

	@Override
	public ResponseEntity<byte[]> generateImgFromDashboardId(String id, int waittime, int height, int width,
			boolean fullpage, String params, String oauthtoken) {

		final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.IMAGE_PNG_VALUE);

		HttpEntity<?> entity = new HttpEntity<>(headers);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format(TO_IMG, dashboardexporturl))
				.queryParam("waittime", waittime).queryParam("url", prefixURLView + id).queryParam("fullpage", fullpage)
				.queryParam("width", width).queryParam("height", height).queryParam("oauthtoken", oauthtoken)
				.queryParam("dashboardsparams", params);

		return restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, byte[].class);
	}

	@Override
	public ResponseEntity<byte[]> generatePDFFromDashboardId(String id, int waittime, int height, int width,
			String params, String oauthtoken) {
		final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.add("Access-Control-Allow-Methods", "GET");
		headers.add("Access-Control-Allow-Headers", "Content-Type");
		headers.add("Content-Disposition", "filename=" + id + ".pdf");
		headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
		headers.add("Pragma", "no-cache");
		headers.add("Expires", "0");

		HttpEntity<?> entity = new HttpEntity<>(headers);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format(TO_PDF, dashboardexporturl))
				.queryParam("waittime", waittime).queryParam("url", prefixURLView + id).queryParam("width", width)
				.queryParam("height", height).queryParam("oauthtoken", oauthtoken)
				.queryParam("dashboardsparams", params);

		return restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, byte[].class);
	}

	@Override
	public String importDashboard(String identification, String data, String userId, String token) {

		final User user = userRepository.findByUserId(userId);
		if (!dashboardExists(identification)) {
			return sendCreatePost("/api/dashboards/import", data, identification, user, token);
		} else {
			log.error(DUPLICATE_DASHBOARD_NAME);
			throw new DashboardServiceException("DUPLICATE_DASHBOARD_NAME");
		}

	}

	private String sendCreatePost(String path, String body, String name, User user, String token) {
		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		headers.add("Authorization", "Bearer " + token);
		ResponseEntity<String> responseEntity;

		log.info("Creating dashboard for user: " + user.getUserId() + " name:  " + name);

		try {

			responseEntity = sendHttp(path, HttpMethod.POST, body, headers);
		} catch (final URISyntaxException e) {
			log.error(URI_POST_ERROR);
			throw new DashboardServiceException(URI_POST2_ERROR + e);
		} catch (final IOException e) {
			log.error(POST_ERROR);
			throw new DashboardServiceException(POST2_ERROR + e);
		}

		final int statusCode = responseEntity.getStatusCodeValue();
		/* 200 zeppelin 8, 201 zeppelin 7 */
		if (statusCode / 100 != 2) {
			log.error(POST_EXECUTING_ERROR + statusCode);
			throw new DashboardServiceException(POST_EXECUTING_ERROR + statusCode);
		}

		log.info("Dashboard for user: " + user.getUserId() + " " + name + ", successfully created");
		return name;
	}

	@Override
	public ResponseEntity<byte[]> exportDashboard(String id, String userId, String token) {
		final Dashboard ds = dashboardRepository.findById(id);
		ResponseEntity<String> responseEntity;
		JSONObject dashboardJSONObject;

		if (hasUserPermission(id, userId)) {
			try {
				responseEntity = sendHttp("/api/dashboards/export/" + id, HttpMethod.GET, "", token);
			} catch (final URISyntaxException e) {
				log.error(URI_POST_ERROR);
				throw new DashboardServiceException(URI_POST2_ERROR + e);
			} catch (final IOException e) {
				log.error(POST_ERROR);
				throw new DashboardServiceException(POST2_ERROR + e);
			}

			final int statusCode = responseEntity.getStatusCodeValue();

			if (statusCode != 200) {
				log.error("Exception executing export dashboard, status code: " + statusCode);
				throw new DashboardServiceException("Exception executing export dashboard, status code: " + statusCode);
			}

			final HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.TEXT_PLAIN);
			headers.set("Content-Disposition", "attachment; filename=\"" + ds.getIdentification() + ".json\"");
			try {
				dashboardJSONObject = new JSONObject(responseEntity.getBody());

			} catch (final JSONException e) {
				log.error("Exception parsing answer in download dashboard");
				throw new DashboardServiceException("Exception parsing answer in download dashboard: " + e);
			}
			return new ResponseEntity<>(dashboardJSONObject.toString().getBytes(StandardCharsets.UTF_8), headers,
					HttpStatus.OK);

		} else {
			log.error("Exception executing export dashboard, permission denied");
			throw new DashboardServiceException("Error export dashboard, permission denied");
		}
	}

	@Override
	public ResponseEntity<String> sendHttp(HttpServletRequest requestServlet, HttpMethod httpMethod, String body,
			String token) throws URISyntaxException, IOException {
		return sendHttp(requestServlet.getServletPath(), httpMethod, body, token);
	}

	@Override
	public ResponseEntity<String> sendHttp(String url, HttpMethod httpMethod, String body, String token)
			throws URISyntaxException, IOException {
		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		headers.add("Authorization", "Bearer " + token);
		return sendHttp(url, httpMethod, body, headers);
	}

	@Override
	public ResponseEntity<String> sendHttp(String url, HttpMethod httpMethod, String body, HttpHeaders headers)
			throws URISyntaxException, IOException {
		final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

		final org.springframework.http.HttpEntity<String> request = new org.springframework.http.HttpEntity<>(body,
				headers);
		log.debug("Sending method " + httpMethod.toString() + " Dashboard");
		ResponseEntity<String> response = new ResponseEntity<>(HttpStatus.ACCEPTED);
		try {
			response = restTemplate.exchange(new URI(basePath + url.toLowerCase()), httpMethod, request, String.class);
		} catch (final Exception e) {
			log.error(e.getMessage());
		}
		log.debug("Execute method " + httpMethod.toString() + " '" + url + "' Dashboard");
		final HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("Content-Type", response.getHeaders().getContentType().toString());
		return new ResponseEntity<>(response.getBody(), responseHeaders,
				HttpStatus.valueOf(response.getStatusCode().value()));
	}

	private void metricsManagerLogControlPanelDashboardsCreation(String userId, String result) {
		if (null != metricsManager) {
			metricsManager.logControlPanelDashboardsCreation(userId, result);
		}
	}

}