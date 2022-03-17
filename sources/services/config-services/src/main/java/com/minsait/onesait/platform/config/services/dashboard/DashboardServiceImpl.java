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
package com.minsait.onesait.platform.config.services.dashboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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
import com.minsait.onesait.platform.config.dto.DashboardForList;
import com.minsait.onesait.platform.config.dto.OPResourceDTO;
import com.minsait.onesait.platform.config.model.Category;
import com.minsait.onesait.platform.config.model.CategoryRelation;
import com.minsait.onesait.platform.config.model.Dashboard;
import com.minsait.onesait.platform.config.model.Dashboard.DashboardType;
import com.minsait.onesait.platform.config.model.DashboardConf;
import com.minsait.onesait.platform.config.model.DashboardUserAccess;
import com.minsait.onesait.platform.config.model.DashboardUserAccessType;
import com.minsait.onesait.platform.config.model.Gadget;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.model.GadgetMeasure;
import com.minsait.onesait.platform.config.model.GadgetTemplate;
import com.minsait.onesait.platform.config.model.I18nResources;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.Subcategory;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.repository.CategoryRelationRepository;
import com.minsait.onesait.platform.config.repository.CategoryRepository;
import com.minsait.onesait.platform.config.repository.DashboardConfRepository;
import com.minsait.onesait.platform.config.repository.DashboardRepository;
import com.minsait.onesait.platform.config.repository.DashboardUserAccessRepository;
import com.minsait.onesait.platform.config.repository.DashboardUserAccessTypeRepository;
import com.minsait.onesait.platform.config.repository.GadgetDatasourceRepository;
import com.minsait.onesait.platform.config.repository.GadgetMeasureRepository;
import com.minsait.onesait.platform.config.repository.GadgetRepository;
import com.minsait.onesait.platform.config.repository.GadgetTemplateRepository;
import com.minsait.onesait.platform.config.repository.I18nResourcesRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.SubcategoryRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.category.CategoryService;
import com.minsait.onesait.platform.config.services.categoryrelation.CategoryRelationService;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardAccessDTO;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardCreateDTO;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardDTO;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardExportDTO;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardImportResponsetDTO;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardOrder;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardSimplifiedDTO;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardUserAccessDTO;
import com.minsait.onesait.platform.config.services.dashboardapi.dto.GadgetTemplateDTO;
import com.minsait.onesait.platform.config.services.exceptions.DashboardServiceException;
import com.minsait.onesait.platform.config.services.exceptions.DashboardServiceException.ErrorType;
import com.minsait.onesait.platform.config.services.exceptions.OPResourceServiceException;
import com.minsait.onesait.platform.config.services.gadget.dto.GadgetDTO;
import com.minsait.onesait.platform.config.services.gadget.dto.GadgetDatasourceDTO;
import com.minsait.onesait.platform.config.services.gadget.dto.GadgetMeasureDTO;
import com.minsait.onesait.platform.config.services.gadget.dto.OntologyDTO;
import com.minsait.onesait.platform.config.services.generic.security.SecurityService;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.subcategory.SubcategoryService;

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
	private GadgetTemplateRepository gadgetTemplateRepository;
	@Autowired
	private GadgetDatasourceRepository gadgetDatasourceRepository;
	@Autowired
	private GadgetMeasureRepository gadgetMeasureRepository;
	@Autowired
	private OntologyRepository ontologyRepository;
	@Autowired(required = false)
	private MetricsManager metricsManager;
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private SubcategoryService subCategoryService;
	@Autowired
	private CategoryRelationService categoryRelationService;
	@Autowired
	I18nResourcesRepository i18nRR;
	@Autowired
	SecurityService securityService;

	@Value("${onesaitplatform.controlpanel.url:http://localhost:18000/controlpanel}")
	private String basePath;

	protected ObjectMapper objectMapper;

	@Value("${onesaitplatform.dashboard.export.url.view:http://localhost:8087/controlpanel/dashboards/viewiframe/}")
	private String prefixURLView;

	@Value("${onesaitplatform.dashboard.export.url:http://dashboardexport:26000}")
	private String dashboardexporturl;

	@Value("${onesaitplatform.dashboardengine.client.maxheartbeattime:5000}")
	private long clientMaxHeartbeatTime;

	@Value("${onesaitplatform.dashboardengine.client.protocol:all}")
	private String protocol;
	
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
	private static final String IDENTIFICATION = "identification";
	private static final String PAGES = "pages";
	private static final String SYNOPTIC = "synoptic";
	private static final String CONDITIONS = "conditions";
	private static final String LAYERS = "layers";
	private static final String GRIDBOARD = "gridboard";
	private static final String DATASOURCE = "datasource";
	private static final String TEMPLATE = "template";
	private static final String ERROR_SAVING_DASHBOARD_FORBIDDEN = "Cannot update Dashboard that does not exist or don't have permission";
	private static final String JSON_PARSE_EXCEPTION = "Json parse exception";
	private static final String JSON_MAPPING_EXCEPTION = "Json mapping exception";
	private static final String IO_EXCEPTION = "IO exception";

	@Override
	public List<DashboardDTO> findDashboardWithIdentificationAndDescription(String identification, String description,
			String userId) {
		List<DashboardForList> dashboardsForList = null;

		final User sessionUser = userRepository.findByUserId(userId);

		description = description == null ? "" : description;
		identification = identification == null ? "" : identification;

		try {
			if (sessionUser.isAdmin()) {
				dashboardsForList = dashboardRepository
						.findByIdentificationContainingAndDescriptionContaining(identification, description);
			} else {
				dashboardsForList = dashboardRepository
						.findByUserAndPermissionsANDIdentificationContainingAndDescriptionContaining(sessionUser,
								identification, description);
				securityService.setSecurityToInputList(dashboardsForList, sessionUser, "Dashboard");
			}
		} catch (final Exception e) {
			log.error(e.getMessage());
		}

		return dashboardsForList.stream().map(temp -> {
			final DashboardDTO obj = new DashboardDTO();
			obj.setCreatedAt(temp.getCreated_at());
			obj.setDescription(temp.getDescription());
			obj.setId(temp.getId());
			obj.setIdentification(temp.getIdentification());
			obj.setHasImage(Boolean.TRUE);
			obj.setPublic(temp.isPublic());
			obj.setUpdatedAt(temp.getUpdated_at());
			obj.setUserAccessType(temp.getAccessType());
			obj.setUser(temp.getUser());
			obj.setType(temp.getType());
			return obj;
		}).collect(Collectors.toList());

	}

	@Override
	public List<DashboardDTO> findDashboardWithIdentificationAndType(String identification, String type, String user) {
		List<DashboardForList> dashboardsForList = new ArrayList<>();

		final User sessionUser = userRepository.findByUserId(user);

		try {
			if (sessionUser.isAdmin()) {
				if (type == null) {
					dashboardsForList = dashboardRepository.findByIdentificationContainingFofList(identification);
				} else {
					if (type.equals("")) {
						dashboardsForList = dashboardRepository
								.findDashboardByIdentificationContainingAndType(identification);
					} else {
						dashboardsForList = dashboardRepository.findByIdentificationContainingAndType(identification,
								DashboardType.valueOf(type));
					}
				}
			} else {
				if (type == null) {
					dashboardsForList = dashboardRepository
							.findByUserAndPermissionsANDIdentificationContaining(sessionUser, identification);
				} else {
					if (type.equals("")) {
						dashboardsForList = dashboardRepository
								.findDashboardByUserAndPermissionsANDIdentificationContaining(sessionUser,
										identification);
					} else {
						dashboardsForList = dashboardRepository
								.findByUserAndPermissionsANDIdentificationContainingAndTypeForList(sessionUser,
										identification, DashboardType.valueOf(type));
					}
				}
				securityService.setSecurityToInputList(dashboardsForList, sessionUser, "Dashboard");
			}
		} catch (final Exception e) {
			log.error(e.getMessage());
		}

		return dashboardsForList.stream().map(temp -> {
			final DashboardDTO obj = new DashboardDTO();
			obj.setCreatedAt(temp.getCreated_at());
			obj.setDescription(temp.getDescription());
			obj.setId(temp.getId());
			obj.setIdentification(temp.getIdentification());
			obj.setHasImage(Boolean.TRUE);
			obj.setPublic(temp.isPublic());
			obj.setUpdatedAt(temp.getUpdated_at());
			obj.setUserAccessType(temp.getAccessType());
			obj.setUser(temp.getUser());
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
		final Dashboard dashboard = dashboardRepository.findById(dashboardId).orElse(null);

		if (dashboard != null && hasUserEditPermission(dashboardId, userId)) {
			if (resourceService.isResourceSharedInAnyProject(dashboard)) {
				throw new OPResourceServiceException(
						"This Dashboard is shared within a Project, revoke access from project prior to deleting");
			}
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

		final Dashboard d = dashboardRepository.findById(dashboardId).orElse(null);
		if (resourceService.isResourceSharedInAnyProject(d)) {
			throw new OPResourceServiceException(
					"This Dashboard is shared within a Project, revoke access from project prior to deleting");
		}
		dashboardUserAccessRepository.deleteByDashboard(d);
		return d.getId();

	}

	@Override
	public String deleteDashboardUserAccess(List<DashboardUserAccessDTO> dtos, String dashboardIdentification,
			boolean deleteAll) {
		final JSONObject response = new JSONObject();
		for (final DashboardUserAccessDTO dto : dtos) {
			final String key = dto.getUserId();
			String value = "";
			String error = "ERROR: Invalid input data:";
			boolean e = false;

			final User user = userRepository.findByUserId(dto.getUserId());
			if (user == null) {
				error += "User not found.";
				e = true;
			}
			DashboardUserAccess dashUA = null;
			if (deleteAll) {
				dashUA = getDashboardUserAccessByIdentificationAndUser(dashboardIdentification, user);
			} else {
				final List<DashboardUserAccessType> accessType = dashboardUserAccessTypeRepository
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
		if (user.isAdmin()) {
			return true;
		} else {
			final Optional<Dashboard> d = dashboardRepository.findById(id);
			if (d.isPresent()) {
				return d.get().getUser().getUserId().equals(userId);
			} else {
				return false;
			}
		}
	}

	@Override
	public boolean hasUserEditPermission(String id, String userId) {
		final User user = userRepository.findByUserId(userId);
		if (user.isAdmin()) {
			return true;
		} else {
			final DashboardForList dfl = dashboardRepository.findForListById(id);
			final ArrayList<DashboardForList> ldfl = new ArrayList<>();
			ldfl.add(dfl);
			securityService.setSecurityToInputList(ldfl, user, "Dashboard");
			return ldfl.get(0).getAccessType() == null ? false : ldfl.get(0).getAccessType().equals("EDIT");
		}
	}

	@Override
	public boolean hasUserViewPermission(String id, String userId) {
		final User user = userRepository.findByUserId(userId);
		final Optional<Dashboard> d = dashboardRepository.findById(id);
		if (!d.isPresent()) {
			return false;
		}
		if (d.get().isPublic()) {
			return true;
		} else if (userId.equals(ANONYMOUSUSER) || user == null) {
			return d.get().isPublic();
		} else if (user.isAdmin()) {
			return true;
		} else {
			final boolean propietary = d.get().getUser().getUserId().equals(userId);
			if (propietary) {
				return true;
			}
			final DashboardUserAccess userAuthorization = dashboardUserAccessRepository.findByDashboardAndUser(d.get(),
					user);

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

		if (user.isAdmin() || dashboard.getUser().getUserId().equals(user.getUserId())) {
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
			dashboardRepository.findById(dashboard.getId()).ifPresent(dashboardEnt -> {
				dashboardEnt.setCustomcss(dashboard.getCustomcss());
				dashboardEnt.setCustomjs(dashboard.getCustomjs());
				dashboardEnt.setDescription(dashboard.getDescription());
				dashboardEnt.setJsoni18n(dashboard.getJsoni18n());
				dashboardEnt.setModel(dashboard.getModel());
				dashboardEnt.setPublic(dashboard.isPublic());
				dashboardEnt.setHeaderlibs(dashboard.getHeaderlibs());
				dashboardEnt.setType(dashboard.getType());
				dashboardRepository.save(dashboardEnt);
			});

		} else {
			throw new DashboardServiceException(ERROR_SAVING_DASHBOARD_FORBIDDEN);
		}
	}

	@Override
	public void saveDashboardModel(String id, String model, String userId) {
		if (hasUserEditPermission(id, userId)) {
			dashboardRepository.saveModel(model, id);
		} else {
			throw new DashboardServiceException(ERROR_SAVING_DASHBOARD_FORBIDDEN);
		}
	}

	@Override
	public void saveDashboardHeaderLibs(String id, String HeaderLibs, String userId) {
		if (hasUserEditPermission(id, userId)) {
			dashboardRepository.saveHeaderLibs(HeaderLibs, id);
		} else {
			throw new DashboardServiceException(ERROR_SAVING_DASHBOARD_FORBIDDEN);
		}
	}

	@Override
	public Dashboard getDashboardById(String id, String userId) {
		return dashboardRepository.findById(id).orElse(null);
	}

	@Override
	public Dashboard getDashboardByIdentification(String identification, String userId) {
		if (!dashboardRepository.findByIdentification(identification).isEmpty()) {
			return dashboardRepository.findByIdentification(identification).get(0);
		} else {
			return null;
		}
	}

	@Override
	public Dashboard getDashboardEditById(String id, String userId) {
		if (hasUserEditPermission(id, userId)) {
			return dashboardRepository.findById(id).orElse(null);
		}
		throw new DashboardServiceException("Cannot view Dashboard that does not exist or don't have permission");
	}

	@Override
	public boolean dashboardExists(String identification) {
		return !CollectionUtils.isEmpty(dashboardRepository.findByIdentification(identification));
	}

	@Override
	public boolean dashboardExistsById(String id) {
		final Optional<Dashboard> dash = dashboardRepository.findById(id);
		return dash.isPresent() && dash.get().getId().length() != 0;
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
			cloneI18nResource(originalDashboard, cloneDashboard, user);
			return cloneDashboard.getId();
		} catch (final Exception e) {

			log.error(e.getMessage());
			return null;
		}
	}

	private void cloneI18nResource(Dashboard originalDashboard, Dashboard cloneDashboard, User user) {

		final List<I18nResources> i18nRRList = i18nRR.findByOPResourceId(originalDashboard.getId());

		if (!i18nRRList.isEmpty()) {
			final OPResource opr = getDashboardById(cloneDashboard.getId(), user.getUserId());
			for (final I18nResources i18nResources : i18nRRList) {
				final I18nResources i18nRe = new I18nResources();
				i18nRe.setI18n(i18nResources.getI18n());
				i18nRe.setOpResource(opr);
				i18nRR.save(i18nRe);
			}
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

		final User sessionUser = userRepository.findByUserId(userId);
		if (sessionUser.getRole().getId().equals(Role.Type.ROLE_USER.toString())
				|| sessionUser.getRole().getId().equals(Role.Type.ROLE_DATAVIEWER.toString())) {
			metricsManagerLogControlPanelDashboardsCreation(userId, "KO");
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
			model = dashboardConfRepository.findById(dashboard.getDashboardConfId()).orElse(new DashboardConf())
					.getModel();
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
			metricsManagerLogControlPanelDashboardsCreation(userId, "KO");
			throw new DashboardServiceException(DASH_CREATE_AUTH_EXCEPT);
		}

		if (dashboardExists(dashboard.getIdentification())) {
			throw new DashboardServiceException("Dashboard already exists in Database");
		}

		final Dashboard dAux = getNewDashboard(dashboard, userId);

		if (!StringUtils.isEmpty(dashboard.getCategory()) && !StringUtils.isEmpty(dashboard.getSubcategory())
				&& categoryRelationRepository.findByTypeId(dAux.getId()) != null) {
			createCategoryRelation(dashboard, dAux.getId());

		}

		try {
			if (dashboard.getAuthorizations() != null) {
				final List<DashboardAccessDTO> access = objectMapper.readValue(dashboard.getAuthorizations(),
						objectMapper.getTypeFactory().constructCollectionType(List.class, DashboardAccessDTO.class));
				for (final DashboardAccessDTO dashboardAccessDTO : access) {
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
		final JSONObject response = new JSONObject();
		for (final DashboardUserAccessDTO dto : dtos) {
			final String key = dto.getUserId();
			String value = "";
			String error = "ERROR. Invalid input data: ";
			boolean e = false;
			final User user = userRepository.findByUserId(dto.getUserId());
			if (user == null) {
				error += "User not found.";
				e = true;
			}
			final DashboardUserAccess dUA = getDashboardUserAccessByIdentificationAndUser(dashboard.getIdentification(),
					user);
			if (!updated && dUA != null) {
				error += "The authorization already exists.";
				e = true;
			}
			if (updated && dUA == null) {
				error += "The authorization does not exist.";
				e = true;
			}

			final List<DashboardUserAccessType> accessType = dashboardUserAccessTypeRepository
					.findByName(dto.getAccessType());
			if (accessType == null || accessType.isEmpty()) {
				error += "Access type not found.";
				e = true;
			}
			if (!e) {
				DashboardUserAccess uA;

				final Date currentDate = new Date();
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
		final Optional<Dashboard> d = dashboardRepository.findById(dashboard.getId());
		if (!d.isPresent()) {
			throw new DashboardServiceException(DASH_NOT_EXIST);
		} else {

			dashboardUserAccessRepository.deleteByDashboard(d.get());
			return dashboard.getId();

		}
	}

	@Transactional
	@Override
	public String saveUpdateAccess(DashboardCreateDTO dashboard, String userId) {
		final Optional<Dashboard> d = dashboardRepository.findById(dashboard.getId());
		if (!d.isPresent()) {
			throw new DashboardServiceException(DASH_NOT_EXIST);
		} else {

			try {
				if (dashboard.getAuthorizations() != null) {
					final List<DashboardAccessDTO> access = objectMapper.readValue(dashboard.getAuthorizations(),
							objectMapper.getTypeFactory().constructCollectionType(List.class,
									DashboardAccessDTO.class));
					for (final DashboardAccessDTO dashboardAccessDTO : access) {
						final DashboardUserAccess dua = new DashboardUserAccess();
						dua.setDashboard(d.get());
						final List<DashboardUserAccessType> managedTypes = dashboardUserAccessTypeRepository
								.findByName(dashboardAccessDTO.getAccesstypes());
						final DashboardUserAccessType managedType = managedTypes != null
								&& !CollectionUtils.isEmpty(managedTypes) ? managedTypes.get(0) : null;
								dua.setDashboardUserAccessType(managedType);
								dua.setUser(userRepository.findByUserId(dashboardAccessDTO.getUsers()));
								dashboardUserAccessRepository.save(dua);
					}
				}
				return dashboard.getId();

			} catch (final IOException e) {

				throw new DashboardServiceException(AUTH_PARSE_EXCEPT);
			}

		}
	}

	@Transactional
	@Override
	public String updatePublicDashboard(DashboardCreateDTO dashboard, String userId) {
		final Optional<Dashboard> d = dashboardRepository.findById(dashboard.getId());
		if (!d.isPresent()) {
			throw new DashboardServiceException(DASH_NOT_EXIST);
		} else {

			d.get().setPublic(dashboard.getPublicAccess());
			d.get().setDescription(dashboard.getDescription());
			d.get().setHeaderlibs(dashboard.getHeaderlibs());
			d.get().setIdentification(dashboard.getIdentification());
			try {
				if (dashboard.getImage() != null && !dashboard.getImage().isEmpty()) {
					d.get().setImage(dashboard.getImage().getBytes());
				} else {
					d.get().setImage(null);
				}
			} catch (final IOException e) {
				log.error(e.getMessage());
			}
			final Dashboard dAux = dashboardRepository.save(d.get());

			if (dashboard.getCategory() != null && dashboard.getSubcategory() != null
					&& !dashboard.getCategory().isEmpty() && !dashboard.getSubcategory().isEmpty()) {

				CategoryRelation categoryRelation = categoryRelationRepository.findByTypeId(d.get().getId());

				if (categoryRelation == null) {
					categoryRelation = new CategoryRelation();
				}

				categoryRelation
				.setCategory(categoryRepository.findByIdentification(dashboard.getCategory()).get(0).getId());
				categoryRelation.setSubcategory(
						subcategoryRepository.findByIdentification(dashboard.getSubcategory()).get(0).getId());
				categoryRelation.setType(CategoryRelation.Type.DASHBOARD);
				categoryRelation.setTypeId(dAux.getId());

				categoryRelationRepository.save(categoryRelation);
			}

			return dashboard.getId();
		}
	}

	@Override
	public byte[] getImgBytes(String id) {
		final Dashboard d = dashboardRepository.findById(id).orElse(new Dashboard());

		return d.getImage();
	}

	@Override
	public List<Dashboard> getByUserId(String userId) {
		final User sessionUser = userRepository.findByUserId(userId);
		if (sessionUser.isAdmin()) {
			return dashboardRepository.findAllByOrderByIdentificationAsc();
		} else {
			return dashboardRepository.findByUserOrderByIdentificationAsc(sessionUser);
		}
	}

	@Override
	public List<String> getIdentificationsByUserId(String userId) {
		final User sessionUser = userRepository.findByUserId(userId);
		if (sessionUser.isAdmin()) {
			return dashboardRepository.findAllIdentificationsByOrderByIdentificationAsc();
		} else {
			return dashboardRepository.findIdentificationsByUserAndPermissions(sessionUser);
		}
	}

	@Transactional
	@Override
	public void updateDashboardSimplified(String id, DashboardSimplifiedDTO dashboard, String userId) {
		if (hasUserEditPermission(id, userId)) {
			dashboardRepository.findById(id).ifPresent(dashboardEnt -> {
				if (dashboard.getDescription() != null) {
					dashboardEnt.setDescription(dashboard.getDescription());
				}
				if (dashboard.getIdentification() != null) {
					dashboardEnt.setIdentification(dashboard.getIdentification());
				}
				dashboardEnt.setPublic(dashboard.isPublic());
				dashboardRepository.save(dashboardEnt);
			});
		} else {
			throw new DashboardServiceException(ERROR_SAVING_DASHBOARD_FORBIDDEN);
		}
	}

	@Override
	public List<Dashboard> getByUserIdOrdered(String userId, DashboardOrder order) {
		final User sessionUser = userRepository.findByUserId(userId);
		final boolean isAdmin = sessionUser.isAdmin();
		switch (order) {
		case CREATED_AT_ASC:
			if (isAdmin) {
				return dashboardRepository.findAllByOrderByCreatedAtAsc();
			}
			return dashboardRepository.findByUserPermissionOrderByCreatedAtAsc(sessionUser);
		case CREATED_AT_DESC:
			if (isAdmin) {
				return dashboardRepository.findAllByOrderByCreatedAtDesc();
			}
			return dashboardRepository.findByUserPermissionOrderByCreatedAtDesc(sessionUser);
		case MODIFIED_AT_ASC:
			if (isAdmin) {
				return dashboardRepository.findAllByOrderByUpdatedAtAsc();
			}
			return dashboardRepository.findByUserPermissionOrderByUpdatedAtAsc(sessionUser);
		case MODIFIED_AT_DESC:
			if (isAdmin) {
				return dashboardRepository.findAllByOrderByUpdatedAtDesc();
			}
			return dashboardRepository.findByUserPermissionOrderByUpdatedAtDesc(sessionUser);
		case IDENTIFICATION_DESC:
			if (isAdmin) {
				return dashboardRepository.findAllByOrderByIdentificationDesc();
			}
			return dashboardRepository.findByUserPermissionOrderByIdentificationDesc(sessionUser);
		case IDENTIFICATION_ASC:
		default:
			if (isAdmin) {
				return dashboardRepository.findAllByOrderByIdentificationAsc();
			}
			return dashboardRepository.findByUserPermissionOrderByIdentificationAsc(sessionUser);
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
		final ArrayList<String> listDatasourcesIDfromSynop = new ArrayList<>();
		final ArrayList<String> listGadgetMeasuresID = new ArrayList<>();
		final ArrayList<String> listGadgetTemplatesID = new ArrayList<>();
		try {
			final Map<String, Object> obj = objectMapper.readValue(dashboard.getModel(),
					new TypeReference<Map<String, Object>>() {
			});
			if (obj.containsKey(PAGES)) {

				((ArrayList<Object>) obj.get(PAGES)).forEach(o -> {
					final Map<String, Object> page = (Map<String, Object>) o;
					addGridBoardFromPage(page, listDatasourcesID, listGadgetMeasuresID, listGadgetsID,
							listGadgetTemplatesID);

				});
			}
			if (obj.containsKey(SYNOPTIC)) {
				addDatasourcesFromSynop(listDatasourcesIDfromSynop, obj);

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
		dashboard.setGadgetTemplates(listGadgetTemplatesID.stream().map(this::gadgetTemplateToDTO)
				.filter(Objects::nonNull).collect(Collectors.toList()));

		if (listDatasourcesIDfromSynop != null && listDatasourcesIDfromSynop.size() > 0) {
			final List<String> listSynopId = listDatasourcesIDfromSynop.stream().distinct()
					.collect(Collectors.toList());

			final List<GadgetDatasourceDTO> synopList = listSynopId.stream()
					.map(this::gadgetDatasourceToDTObyIdentification).filter(Objects::nonNull)
					.collect(Collectors.toList());

			if (dashboard.getGadgetDatasources() != null && dashboard.getGadgetDatasources().size() > 0) {
				// remove repeats
				for (final Object element : synopList) {
					final GadgetDatasourceDTO synopGadgetDatasourceDTO = (GadgetDatasourceDTO) element;
					boolean found = false;
					for (final Object element2 : dashboard.getGadgetDatasources()) {
						final GadgetDatasourceDTO gadgetDatasourceDTO = (GadgetDatasourceDTO) element2;
						if (gadgetDatasourceDTO.getId() == synopGadgetDatasourceDTO.getId()) {
							found = true;
						}
					}
					if (!found) {
						dashboard.getGadgetDatasources().add(synopGadgetDatasourceDTO);
					}
				}
			} else {
				dashboard.setGadgetDatasources(synopList);
			}
		}

		return dashboard;
	}

	private DashboardExportDTO lightAddGadgets(DashboardExportDTO dashboard) {
		final ArrayList<GadgetDTO> listGadgets = new ArrayList<>();
		final ArrayList<GadgetMeasureDTO> listGadgetMeasures = new ArrayList<>();
		final ArrayList<GadgetTemplateDTO> listGadgetTemplates = new ArrayList<>();
		try {
			final Map<String, Object> obj = objectMapper.readValue(dashboard.getModel(),
					new TypeReference<Map<String, Object>>() {
			});
			if (obj.containsKey(PAGES)) {

				((ArrayList<Object>) obj.get(PAGES)).forEach(o -> {
					final Map<String, Object> page = (Map<String, Object>) o;
					lightAddGridBoardFromPage(page, listGadgetMeasures, listGadgets, listGadgetTemplates);

				});
			}
		} catch (final Exception e) {
			log.error(JSON_PARSE_EXCEPTION, e);
		}

		dashboard.setGadgets(listGadgets.stream().filter(Objects::nonNull).collect(Collectors.toList()));
		dashboard.setGadgetMeasures(listGadgetMeasures.stream().filter(Objects::nonNull).collect(Collectors.toList()));
		dashboard
		.setGadgetTemplates(listGadgetTemplates.stream().filter(Objects::nonNull).collect(Collectors.toList()));

		return dashboard;
	}

	private void addDatasourcesFromSynop(final ArrayList<String> listDatasourcesIDfromSynop,
			final Map<String, Object> obj) {
		final Map<String, Object> synop = (Map<String, Object>) obj.get(SYNOPTIC);
		final ArrayList<Object> conditions = (ArrayList<Object>) synop.get(CONDITIONS);
		conditions.forEach(svgLabel -> {
			final ArrayList<Object> oSVG = (ArrayList<Object>) svgLabel;
			for (final Object o : oSVG) {
				if (o instanceof Map) {
					if (((Map<String, Object>) o).containsKey(DATASOURCE)) {
						listDatasourcesIDfromSynop.add((String) ((Map<String, Object>) o).get(DATASOURCE));
					}
				}
			}

		});
	}

	@SuppressWarnings("unchecked")
	private void addGridBoardFromPage(Map<String, Object> page, List<String> listDatasourcesID,
			List<String> listGadgetMeasuresID, List<String> listGadgetsID, List<String> listGadgetTemplatesID) {
		final ArrayList<Object> layers = (ArrayList<Object>) page.get(LAYERS);
		final Map<String, Object> layer = (Map<String, Object>) layers.get(0);
		final ArrayList<Object> gridboard = (ArrayList<Object>) layer.get(GRIDBOARD);
		gridboard.forEach(ob -> {
			final Map<String, Object> gadget = (Map<String, Object>) ob;
			if (gadget.containsKey(DATASOURCE)) { // template
				final Map<String, Object> datasource = (Map<String, Object>) gadget.get(DATASOURCE);
				final String datasourceId = (String) datasource.get("id");
				listDatasourcesID.add(datasourceId);
				if (gadget.containsKey(TEMPLATE)) {
					listGadgetTemplatesID.add((String) gadget.get(TEMPLATE));
				}
			} else if (gadget.containsKey("id")) {
				final String gadgetId = (String) gadget.get("id");
				listGadgetsID.add(gadgetId);
				final List<GadgetMeasure> gadgetMeasures = gadgetMeasureRepository
						.findByGadget(gadgetRepository.findById(gadgetId).orElse(null));
				if (!CollectionUtils.isEmpty(gadgetMeasures)) {
					for (final GadgetMeasure gadgetMeasure : gadgetMeasures) {
						listGadgetMeasuresID.add(gadgetMeasure.getId());
					}
					final String datasource = gadgetMeasureRepository
							.findByGadget(gadgetRepository.findById(gadgetId).orElse(null)).get(0).getDatasource()
							.getId();
					listDatasourcesID.add(datasource);
				}
			}
		});
	}

	private void lightAddGridBoardFromPage(Map<String, Object> page, List<GadgetMeasureDTO> listGadgetMeasures,
			List<GadgetDTO> listGadgets, List<GadgetTemplateDTO> listGadgetsTemplates) {
		final ArrayList<Object> layers = (ArrayList<Object>) page.get(LAYERS);
		final Map<String, Object> layer = (Map<String, Object>) layers.get(0);
		final ArrayList<Object> gridboard = (ArrayList<Object>) layer.get(GRIDBOARD);
		gridboard.forEach(ob -> {
			final Map<String, Object> gadget = (Map<String, Object>) ob;
			if (gadget.containsKey("id")) {
				final String gadgetId = (String) gadget.get("id");
				final Gadget g = gadgetRepository.findById(gadgetId).orElse(null);
				listGadgets.add(gadgetToDTO(g));
				final List<GadgetMeasure> gadgetMeasures = gadgetMeasureRepository.findByGadget(g);
				if (!CollectionUtils.isEmpty(gadgetMeasures)) {
					for (final GadgetMeasure gadgetMeasure : gadgetMeasures) {
						listGadgetMeasures.add(lightGadgetMeasureToDTO(gadgetMeasure));
					}
				}
			}
			if (gadget.containsKey("template")) {
				final String templateId = (String) gadget.get("template");
				final GadgetTemplate gt = gadgetTemplateRepository.findByIdentification(templateId);
				listGadgetsTemplates.add(gadgetTemplateToDTO(gt));
			}
		});
	}

	@Override
	public DashboardImportResponsetDTO importDashboard(DashboardExportDTO dashboardimportDTO, String userId,
			boolean overwrite, boolean importAuthorizations) {
		Dashboard dashboard = new Dashboard();
		final DashboardImportResponsetDTO dashboardImportResultDTO = new DashboardImportResponsetDTO();
		if (dashboardExists(dashboardimportDTO.getIdentification())) {
			if (overwrite) {
				dashboard = dashboardRepository.findByIdentification(dashboardimportDTO.getIdentification()).get(0);
				if (!hasUserEditPermission(dashboard.getId(), userId)) {
					return dashboardImportResultDTO;
				}
			} else {
				return dashboardImportResultDTO;
			}
		}
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
		if (dashboard.getImage() != null && dashboard.getImage().length == 0) {
			dashboard.setImage(null);
		}

		final Dashboard dAux = dashboardRepository.save(dashboard);

		if (!StringUtils.isEmpty(dashboardimportDTO.getCategory())
				&& !StringUtils.isEmpty(dashboardimportDTO.getSubcategory())
				&& categoryRelationRepository.findByTypeId(dAux.getId()) != null) {
			this.createCategoryRelation(dashboardimportDTO, dashboard.getId());

		}

		// include DASH_AUTHS
		if (importAuthorizations) {
			dashboardUserAccessRepository.deleteByDashboard(dAux);
			includeDashboardAuths(dashboardimportDTO, dashboard.getId());
		}
		// include GADGETS
		includeGadgets(dashboardimportDTO, userId);

		// include GADGET_DATASOURCES
		includeGadgetDatasoures(dashboardimportDTO, userId);
		// include GADGET_MEASURES
		includeGadgetMeasures(dashboardimportDTO);
		// include GADGET_TEMPLATES
		includeGadgetTemplates(dashboardimportDTO, userId);

		final Dashboard dashboardResponse = dashboardRepository.findByIdentification(dashboard.getIdentification())
				.get(0);

		if (dashboardResponse != null) {
			dashboardImportResultDTO.setId(dashboardResponse.getId());
			dashboardImportResultDTO.setIdentification(dashboardResponse.getIdentification());
		}

		return dashboardImportResultDTO;

	}

	private void includeDashboardAuths(DashboardExportDTO dashboard, String id) {
		for (final DashboardUserAccessDTO dashboardUADTO : dashboard.getDashboardAuths()) {
			final DashboardUserAccess dashboardUA = new DashboardUserAccess();
			dashboardUA.setDashboard(dashboardRepository.findById(id).orElse(null));
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
			if (!gadgetRepository.findById(gadgetDTO.getId()).isPresent()) {
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

	private void includeGadgetTemplates(DashboardExportDTO dashboard, String userId) {
		for (final GadgetTemplateDTO gadgetTemplateDTO : dashboard.getGadgetTemplates()) {
			final GadgetTemplate gadgetTemplate = new GadgetTemplate();
			if (gadgetTemplateRepository.findByIdentification(gadgetTemplateDTO.getIdentification()) == null) {
				gadgetTemplate.setId(gadgetTemplateDTO.getId());
				gadgetTemplate.setHeaderlibs(gadgetTemplateDTO.getHeaderlibs());
				gadgetTemplate.setTemplate(gadgetTemplateDTO.getTemplate());
				gadgetTemplate.setTemplateJS(gadgetTemplateDTO.getTemplateJS());
				gadgetTemplate.setDescription(gadgetTemplateDTO.getDescription());
				gadgetTemplate.setIdentification(gadgetTemplateDTO.getIdentification());
				gadgetTemplate.setPublic(gadgetTemplateDTO.isPublic());
				gadgetTemplate.setType(gadgetTemplateDTO.getType());
				gadgetTemplate.setUser(userRepository.findByUserId(userId));

				gadgetTemplateRepository.save(gadgetTemplate);
			}
		}
	}

	private void includeGadgetDatasoures(DashboardExportDTO dashboard, String userId) {
		for (final GadgetDatasourceDTO gadgetDSDTO : dashboard.getGadgetDatasources()) {
			final GadgetDatasource gadgetDS = new GadgetDatasource();
			if (!gadgetDatasourceRepository.findById(gadgetDSDTO.getId()).isPresent()) {
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
						&& ontologyRepository.findByIdentification(oDTO.getIdentification()) != null) {
					gadgetDS.setOntology(ontologyRepository.findByIdentification(oDTO.getIdentification()));
				} else {
					gadgetDS.setOntology(null);
				}
				gadgetDS.setUser(userRepository.findByUserId(userId));
				gadgetDatasourceRepository.save(gadgetDS);
			}
		}
	}

	private void includeGadgetMeasures(DashboardExportDTO dashboard) {
		for (final GadgetMeasureDTO gadgetMeasureDTO : dashboard.getGadgetMeasures()) {
			final GadgetMeasure gadgetMeasure = new GadgetMeasure();
			if (!gadgetMeasureRepository.findById(gadgetMeasureDTO.getId()).isPresent()) {
				gadgetMeasure.setId(gadgetMeasureDTO.getId());
				gadgetMeasure.setConfig(gadgetMeasureDTO.getConfig());
				gadgetMeasure.setGadget(gadgetRepository.findById(gadgetMeasureDTO.getGadget().getId()).orElse(null));
				gadgetMeasure.setDatasource(
						gadgetDatasourceRepository.findById(gadgetMeasureDTO.getDatasource().getId()).orElse(null));

				gadgetMeasureRepository.save(gadgetMeasure);
			}
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public String getElementsAssociated(String dashboardId) {
		final JSONArray elements = new JSONArray();

		final Optional<Dashboard> opt = dashboardRepository.findById(dashboardId);
		if (!opt.isPresent()) {
			return null;
		}
		final Dashboard dashboard = opt.get();
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
	@Override
	public JSONArray getGadgets(List<String> dashboardList, String userId) {
		final JSONArray elements = new JSONArray();

		for (final String dashboardId : dashboardList) {

			final Dashboard dashboard = dashboardRepository.findByIdentificationOrId(dashboardId, dashboardId);

			if (dashboard == null || !hasUserViewPermission(dashboard.getId(), userId)) {
				continue;
			}

			try {
				final Map<String, Object> obj = objectMapper.readValue(dashboard.getModel(),
						new TypeReference<Map<String, Object>>() {
				});
				if (obj.containsKey(PAGES)) {
					((ArrayList<Object>) obj.get(PAGES)).forEach(o -> {
						final Map<String, Object> page = (Map<String, Object>) o;
						processPageForGadgets(page, elements, dashboard);
					});
				}
			} catch (final JsonParseException e) {
				log.error(JSON_PARSE_EXCEPTION, e);
			} catch (final JsonMappingException e) {
				log.error(JSON_MAPPING_EXCEPTION, e);
			} catch (final IOException e) {
				log.error(IO_EXCEPTION, e);
			}
		}
		return elements;
	}

	@SuppressWarnings("unchecked")
	private JSONArray processPageForGadgets(Map<String, Object> page, JSONArray elements, Dashboard dashboard) {
		final ArrayList<Object> layers = (ArrayList<Object>) page.get(LAYERS);
		final Map<String, Object> layer = (Map<String, Object>) layers.get(0);
		((ArrayList<Object>) layer.get(GRIDBOARD)).forEach(o -> {
			final Map<String, Object> gridboard = (Map<String, Object>) o;
			processGridboardForGadgets(gridboard, elements, page.get("title").toString(), dashboard);
		});

		return elements;

	}

	@SuppressWarnings("unchecked")
	private void processGridboardForGadgets(Map<String, Object> gadget, JSONArray elements, String title,
			Dashboard dashboard) {

		if (gadget.containsKey("id")) {
			final JSONObject e = new JSONObject();
			final Map<String, Object> header = (Map<String, Object>) gadget.get("header");
			final Map<String, Object> gadgetTitle = (Map<String, Object>) header.get("title");
			e.put("dashboardId", dashboard.getId());
			e.put("dashboardIdentification", dashboard.getIdentification());
			e.put("title", gadgetTitle.get("text").toString());
			e.put("type", gadget.get("type").toString());
			e.put("page", title);
			elements.put(e);
		}
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

		if (gadget.containsKey(DATASOURCE) && !org.springframework.util.StringUtils.isEmpty(gadget.get(DATASOURCE))) {
			final Map<String, Object> datasource = (Map<String, Object>) gadget.get(DATASOURCE);
			final String datasourceId = (String) datasource.get("id");
			final GadgetDatasource datasourceObj = gadgetDatasourceRepository.findById(datasourceId).orElse(null);
			final JSONObject e = new JSONObject();
			e.put("id", datasourceObj.getId());
			e.put(IDENTIFICATION, datasourceObj.getIdentification());
			e.put("type", datasourceObj.getClass().getSimpleName());
			added.add(datasourceObj.getId());
			elements.put(e);
		} else if (gadget.containsKey("id") && !added.contains(gadget.get("id").toString())
				&& gadgetRepository.findById(gadget.get("id").toString()) != null
				&& gadgetRepository.findById(gadget.get("id").toString()).isPresent()) {

			final Gadget gadgetObj = gadgetRepository.findById(gadget.get("id").toString()).orElse(null);
			JSONObject e = new JSONObject();
			e.put("id", gadgetObj.getId());
			e.put(IDENTIFICATION, gadgetObj.getIdentification());
			e.put("type", gadgetObj.getClass().getSimpleName());
			added.add(gadgetObj.getId());
			elements.put(e);
			final List<GadgetMeasure> gadgetMeasures = gadgetMeasureRepository
					.findByGadget(gadgetRepository.findById(gadgetObj.getId()).orElse(null));
			if (!gadgetMeasures.isEmpty()) {

				final GadgetDatasource datasourceObj = gadgetMeasureRepository
						.findByGadget(gadgetRepository.findById(gadgetObj.getId()).orElse(null)).get(0).getDatasource();
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
		final Gadget gadget = gadgetRepository.findById(gadgetId).orElse(null);
		return gadgetToDTO(gadget);
	}

	private GadgetDTO gadgetToDTO(Gadget gadget) {
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

	private GadgetTemplateDTO gadgetTemplateToDTO(GadgetTemplate gadgetTemplate) {
		final GadgetTemplateDTO gtDto = new GadgetTemplateDTO();
		if (gadgetTemplate != null) {
			gtDto.setId(gadgetTemplate.getId());
			gtDto.setTemplate(gadgetTemplate.getTemplate());
			gtDto.setTemplateJS(gadgetTemplate.getTemplateJS());
			gtDto.setIdentification(gadgetTemplate.getIdentification());
			gtDto.setPublic(gadgetTemplate.isPublic());
			gtDto.setType(gadgetTemplate.getType());
			gtDto.setDescription(gadgetTemplate.getDescription());
			gtDto.setHeaderlibs(gadgetTemplate.getHeaderlibs());
			return gtDto;
		}
		return null;
	}

	private GadgetTemplateDTO gadgetTemplateToDTO(String identification) {
		final GadgetTemplate gadgetTemplate = gadgetTemplateRepository.findByIdentification(identification);
		return gadgetTemplateToDTO(gadgetTemplate);
	}

	private GadgetDatasourceDTO gadgetDatasourceToDTO(String gadgetDSId) {
		final GadgetDatasource gadgetds = gadgetDatasourceRepository.findById(gadgetDSId).orElse(null);
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

	private GadgetDatasourceDTO gadgetDatasourceToDTObyIdentification(String identification) {
		final GadgetDatasource gadgetds = gadgetDatasourceRepository.findByIdentification(identification);
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
		final GadgetMeasure gadgetMeasure = gadgetMeasureRepository.findById(gadgetMeasuresId).orElse(null);
		return gadgetMeasureToDTO(gadgetMeasure);
	}

	private GadgetMeasureDTO gadgetMeasureToDTO(GadgetMeasure gadgetMeasure) {

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

	private GadgetMeasureDTO lightGadgetMeasureToDTO(GadgetMeasure gadgetMeasure) {

		final GadgetMeasureDTO gDto = new GadgetMeasureDTO();
		if (gadgetMeasure != null) {
			gDto.setId(gadgetMeasure.getId());
			gDto.setConfig(gadgetMeasure.getConfig());
			final GadgetDTO gdto = new GadgetDTO();
			gdto.setId(gadgetMeasure.getGadget().getId());
			gDto.setGadget(gdto);
			final GadgetDatasourceDTO gdsdto = new GadgetDatasourceDTO();
			gdsdto.setId(gadgetMeasure.getDatasource().getId());
			gdsdto.setRefresh(gadgetMeasure.getDatasource().getRefresh());
			gdsdto.setMode(gadgetMeasure.getDatasource().getMode());
			gdsdto.setIdentification(gadgetMeasure.getDatasource().getIdentification());
			gDto.setDatasource(gdsdto);
			return gDto;
		}
		return null;
	}

	@Override
	public ResponseEntity<byte[]> generateImgFromDashboardId(String id, int waittime, int height, int width,
			boolean fullpage, String params, String oauthtoken) {

		final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

		final HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.IMAGE_PNG_VALUE);

		final HttpEntity<?> entity = new HttpEntity<>(headers);

		final UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format(TO_IMG, dashboardexporturl))
				.queryParam("waittime", waittime).queryParam("url", prefixURLView + id).queryParam("fullpage", fullpage)
				.queryParam("width", width).queryParam("height", height).queryParam("oauthtoken", oauthtoken)
				.queryParam("dashboardsparams", params);

		return restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, byte[].class);
	}

	@Override
	public ResponseEntity<byte[]> generatePDFFromDashboardId(String id, int waittime, int height, int width,
			String params, String oauthtoken) {
		final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.add("Access-Control-Allow-Methods", "GET");
		headers.add("Access-Control-Allow-Headers", "Content-Type");
		headers.add("Content-Disposition", "filename=" + id + ".pdf");
		headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
		headers.add("Pragma", "no-cache");
		headers.add("Expires", "0");

		final HttpEntity<?> entity = new HttpEntity<>(headers);

		final UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format(TO_PDF, dashboardexporturl))
				.queryParam("waittime", waittime).queryParam("url", prefixURLView + id).queryParam("width", width)
				.queryParam("height", height).queryParam("oauthtoken", oauthtoken)
				.queryParam("dashboardsparams", params);

		return restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, byte[].class);
	}

	@Override
	public DashboardExportDTO exportDashboardDTO(String dashboardId, String userId) {
		final Dashboard dashboard = getDashboardById(dashboardId, userId);
		if (dashboard == null || !hasUserEditPermission(dashboardId, userId)) {
			throw new DashboardServiceException(ErrorType.NOT_FOUND, "NOT FOUND");
		}

		final CategoryRelation categoryRelationship = categoryRelationService.getByIdType(dashboard.getId());
		String categoryIdentification = null;
		String subCategoryIdentification = null;
		if (categoryRelationship != null) {
			final Category category = categoryService.getCategoryByIdentification(categoryRelationship.getCategory());
			final Subcategory subcategory = subCategoryService
					.getSubcategoryById(categoryRelationship.getSubcategory());
			categoryIdentification = category.getIdentification();
			subCategoryIdentification = subcategory.getIdentification();
		}

		final int ngadgets = getNumGadgets(dashboard);

		final List<DashboardUserAccess> dashaccesses = getDashboardUserAccesses(dashboard);
		final List<DashboardUserAccessDTO> dashAuths = dashAuthstoDTO(dashaccesses);

		final DashboardExportDTO dashboardDTO = DashboardExportDTO.builder().id(dashboard.getId())
				.identification(dashboard.getIdentification()).user(dashboard.getUser().getUserId())
				.category(categoryIdentification).subcategory(subCategoryIdentification).nGadgets(ngadgets)
				.headerlibs(dashboard.getHeaderlibs()).createdAt(dashboard.getCreatedAt())
				.description(dashboard.getDescription()).modifiedAt(dashboard.getUpdatedAt()).dashboardAuths(dashAuths)
				.model(dashboard.getModel()).type(dashboard.getType()).build();

		final DashboardExportDTO dashWGadgets = addGadgets(dashboardDTO);

		return dashWGadgets;
	}

	// Only for visualization purposes
	@Override
	public DashboardExportDTO getBungleDashboardDTO(String dashboardId, String userId) {
		final Dashboard dashboard = getDashboardById(dashboardId, userId);
		if (dashboard == null || !hasUserViewPermission(dashboardId, userId)) {
			throw new DashboardServiceException(ErrorType.NOT_FOUND, "NOT FOUND");
		}

		final DashboardExportDTO dashboardDTO = DashboardExportDTO.builder().model(dashboard.getModel())
				.id(dashboard.getId()).build();

		final DashboardExportDTO dashWGadgets = lightAddGadgets(dashboardDTO);

		return dashWGadgets;
	}

	private List<DashboardUserAccessDTO> dashAuthstoDTO(List<DashboardUserAccess> dashaccesses) {
		final ArrayList<DashboardUserAccessDTO> dashAuths = new ArrayList<>();
		for (final DashboardUserAccess dashua : dashaccesses) {
			final DashboardUserAccessDTO dashAccDTO = new DashboardUserAccessDTO();
			dashAccDTO.setUserId(dashua.getUser().getUserId());
			dashAccDTO.setAccessType(dashua.getDashboardUserAccessType().getName());
			dashAuths.add(dashAccDTO);
		}
		return dashAuths;
	}

	private void metricsManagerLogControlPanelDashboardsCreation(String userId, String result) {
		if (null != metricsManager) {
			metricsManager.logControlPanelDashboardsCreation(userId, result);
		}
	}

	@Override
	public JSONObject getAllInternationalizationJSON(Dashboard dashboard) {
		JSONObject json1, json2;
		Iterator<?> langs, langs2;

		final List<I18nResources> i18nR = i18nRR.findByOPResourceId(dashboard.getId());
		if (!i18nR.isEmpty()) {
			json1 = new JSONObject(i18nR.get(0).getI18n().getJsoni18n());
			langs = json1.getJSONObject("languages").keys();
			for (int i = 1; i < i18nR.size(); i++) {
				json2 = new JSONObject(i18nR.get(i).getI18n().getJsoni18n());
				langs2 = json2.getJSONObject("languages").keys();

				final ArrayList<String> sameKeys = getSameKeys(langs, langs2);
				for (int k = 0; k < sameKeys.size(); k++) {
					final Iterator<?> langjson = ((JSONObject) json1.getJSONObject("languages").get(sameKeys.get(k)))
							.keys();
					final Iterator<?> langjson2 = ((JSONObject) json2.getJSONObject("languages").get(sameKeys.get(k)))
							.keys();
					final ArrayList<String> diffKeys = getDifferentKeys(langjson, langjson2);
					for (int j = 0; j < diffKeys.size(); j++) {
						json1.getJSONObject("languages").getJSONObject(sameKeys.get(k)).put(diffKeys.get(j), json2
								.getJSONObject("languages").getJSONObject(sameKeys.get(k)).getString(diffKeys.get(j)));
					}
				}

				langs = json1.getJSONObject("languages").keys();
				langs2 = json2.getJSONObject("languages").keys();
				final ArrayList<String> diffKeys = getDifferentKeys(langs, langs2);
				for (int j = 0; j < diffKeys.size(); j++) {
					json1.getJSONObject("languages").put(diffKeys.get(j),
							json2.getJSONObject("languages").getJSONObject(diffKeys.get(j)));
				}
			}
			return json1;
		} else {
			return new JSONObject();
		}
	}

	@Override
	public long getClientMaxHeartbeatTime() {
		return clientMaxHeartbeatTime;
	}
	
	@Override
	public String getProtocol() {
		return protocol;
	}

	private ArrayList<String> getSameKeys(Iterator<?> it1, Iterator<?> it2) {
		final ArrayList<String> sameKeys = new ArrayList<>();
		final List<?> it1List = IteratorUtils.toList(it1);
		while (it2.hasNext()) {
			final String key = (String) it2.next();
			;
			if (it1List.contains(key)) {
				sameKeys.add(key);
			}
		}
		return sameKeys;
	}

	private ArrayList<String> getDifferentKeys(Iterator<?> it1, Iterator<?> it2) {
		final ArrayList<String> differentKeys = new ArrayList<>();
		final List<?> it1List = IteratorUtils.toList(it1);
		while (it2.hasNext()) {
			final String key = (String) it2.next();
			if (!it1List.contains(key)) {
				differentKeys.add(key);
			}
		}
		return differentKeys;
	}

	@Override
	public List<OPResourceDTO> getDtoByUserAndPermissions(String userId, String identification, String description) {
		final User sessionUser = userRepository.findByUserId(userId);
		if (sessionUser.isAdmin()) {
			return dashboardRepository.findAllDto(identification, description);
		} else {
			return dashboardRepository.findDtoByUserAndPermissions(sessionUser, identification, description);
		}
	}

}
