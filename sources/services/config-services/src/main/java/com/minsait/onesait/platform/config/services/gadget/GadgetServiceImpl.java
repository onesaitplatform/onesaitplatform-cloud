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
package com.minsait.onesait.platform.config.services.gadget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.minsait.onesait.platform.config.dto.OPResourceDTO;
import com.minsait.onesait.platform.config.model.Category;
import com.minsait.onesait.platform.config.model.CategoryRelation;
import com.minsait.onesait.platform.config.model.Gadget;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.model.GadgetMeasure;
import com.minsait.onesait.platform.config.model.GadgetTemplate;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.model.Subcategory;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.CategoryRelationRepository;
import com.minsait.onesait.platform.config.repository.GadgetMeasureRepository;
import com.minsait.onesait.platform.config.repository.GadgetRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.category.CategoryService;
import com.minsait.onesait.platform.config.services.categoryrelation.CategoryRelationService;
import com.minsait.onesait.platform.config.services.exceptions.CategoryServiceException;
import com.minsait.onesait.platform.config.services.exceptions.GadgetDatasourceServiceException;
import com.minsait.onesait.platform.config.services.exceptions.OPResourceServiceException;
import com.minsait.onesait.platform.config.services.exceptions.SubcategoryServiceException;
import com.minsait.onesait.platform.config.services.gadget.dto.GadgetDTO;
import com.minsait.onesait.platform.config.services.gadgettemplate.GadgetTemplateService;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.subcategory.SubcategoryService;
import com.minsait.onesait.platform.config.services.user.UserService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GadgetServiceImpl implements GadgetService {

	@Autowired
	private GadgetRepository gadgetRepository;

	@Autowired
	private GadgetMeasureRepository gadgetMeasureRepository;

	@Autowired
	private GadgetDatasourceService gadgetDatasourceService;

	@Autowired
	private GadgetTemplateService gadgetTemplateService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserService userService;

	@Autowired
	@Lazy
	private OPResourceService resourceService;

	@Autowired
	private CategoryRelationService categoryRelationService;

	@Autowired
	private CategoryRelationRepository categoryRelationRepository;

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private SubcategoryService subcategoryService;

	@Override
	public List<Gadget> findAllGadgets() {
		return gadgetRepository.findAll();
	}

	@Value("${onesaitplatform.dashboard.export.url:http://dashboardexport:26000}")
	private String dashboardexporturl;

	@Value("${onesaitplatform.dashboard.export.url.gadgets: http://localhost:8087/controlpanel/gadgets/view/}")
	private String prefixURLView;

	private static final String TO_IMG = "%s/imgfromurl";
	private static final String TO_PDF = "%s/pdffromurl";

	@Override
	public List<Gadget> findGadgetWithIdentificationAndType(String identification, String type, String userId) {
		List<Gadget> gadgets;
		final User user = userRepository.findByUserId(userId);

		if (userService.isUserAdministrator(user)) {
			if (type != null && identification != null) {

				gadgets = gadgetRepository.findByIdentificationContainingAndTypeContaining(identification, type);

			} else if (type == null && identification != null) {

				gadgets = gadgetRepository.findByIdentificationContaining(identification);

			} else if (type != null) {

				gadgets = gadgetRepository.findByTypeContaining(type);

			} else {

				gadgets = gadgetRepository.findAll();
			}
		} else {
			if (type != null && identification != null) {

				gadgets = gadgetRepository.findByUserAndIdentificationContainingAndTypeContaining(user, identification,
						type);

			} else if (type == null && identification != null) {

				gadgets = gadgetRepository.findByUserAndIdentificationContaining(user, identification);

			} else if (type != null) {

				gadgets = gadgetRepository.findByUserAndTypeContaining(user, type);

			} else {

				gadgets = gadgetRepository.findByUser(user);
			}
		}
		return gadgets;
	}

	@Override
	public List<String> getAllIdentifications() {
		final List<Gadget> gadgets = gadgetRepository.findAllByOrderByIdentificationAsc();
		final List<String> names = new ArrayList<>();
		for (final Gadget gadget : gadgets) {
			names.add(gadget.getIdentification());

		}
		return names;
	}

	@Override
	public List<String> getAllIdentificationsByUser(String userId) {
		User user = userService.getUser(userId);
		if (user.isAdmin()) {
			return gadgetRepository.findAllIdentifications();
		} else {
			return gadgetRepository.findAllIdentificationsByUser(user);
		}
	}

	@Override
	public Gadget getGadgetById(String userID, String gadgetId) {
		return gadgetRepository.findById(gadgetId).orElse(null);
	}

	@Override
	public void createGadget(GadgetDTO gadgetDto) {
		Gadget gadget = gadgetDTO2Gadget(gadgetDto);
		if (gadgetRepository.findByIdentification(gadget.getIdentification()) == null) {
			gadgetRepository.save(gadget);
			createCategoryRelation(gadgetDto, gadget.getId());
		}
	}

	@Override
	public List<Gadget> getUserGadgetsByType(String userID, String type) {
		final User user = userRepository.findByUserId(userID);
		final List<Gadget> gadgets = gadgetRepository.findByTypeOrderByIdentificationAsc(type);
		if (userService.isUserAdministrator(user)) {
			return gadgets;
		} else {

			final List<String> resourceIdList = new ArrayList<>();
			for (final Iterator iterator = gadgets.iterator(); iterator.hasNext();) {
				final Gadget g = (Gadget) iterator.next();
				resourceIdList.add(g.getId());
			}
			final Map<String, ResourceAccessType> midrat = resourceService
					.getResourcesAccessMapByUserAndResourceIdList(user, resourceIdList);

			final List<Gadget> result = new ArrayList<>();
			for (final Iterator iterator = gadgets.iterator(); iterator.hasNext();) {
				final Gadget g = (Gadget) iterator.next();
				if (g.getUser().getUserId().equals(userID) || (midrat.get(g.getId()) != null)) {
					result.add(g);
				}
			}
			return result;
		}
	}

	@Override
	public List<GadgetMeasure> getGadgetMeasuresByGadgetId(String userID, String gadgetId) {
		return gadgetMeasureRepository.findByGadget(gadgetRepository.findById(gadgetId).orElse(null));
	}

	@Override
	public boolean hasUserPermission(String id, String userId) {
		final User user = userRepository.findByUserId(userId);
		final Gadget gadget = gadgetRepository.findById(id).orElse(null);
		if (userService.isUserAdministrator(user)) {
			return true;
		} else if (gadget.getUser().getUserId().equals(userId)) {
			return true;
		} else {
			return resourceService.hasAccess(userId, id, ResourceAccessType.MANAGE);
		}
	}

	@Override
	public boolean hasUserViewPermission(String id, String userId) {
		return hasUserPermission(id, userId) || resourceService.hasAccess(userId, id, ResourceAccessType.VIEW);
	}

	@Override
	public void deleteGadget(String gadgetId, String userId) throws JsonProcessingException {
		if (hasUserPermission(gadgetId, userId)) {
			final Gadget gadget = gadgetRepository.findById(gadgetId).orElse(null);
			if (gadget != null) {
				if (resourceService.isResourceSharedInAnyProject(gadget)) {
					List<String> projects = new ArrayList<>();
					resourceService.getProjectsByResource(gadget).forEach(pra -> {
						if (!projects.contains(pra.getProject().getIdentification()))
							projects.add(pra.getProject().getIdentification());
					});
					throw new OPResourceServiceException(" This Gadget is shared within the Projects: "
							+ new ObjectMapper().writeValueAsString(projects)
							+ " , revoke access from projects prior to deleting");
				}
				final List<GadgetMeasure> lgmeasure = gadgetMeasureRepository.findByGadget(gadget);
				for (final GadgetMeasure gm : lgmeasure) {
					gadgetMeasureRepository.delete(gm);
				}
				categoryRelationService.deleteCategoryRelation(gadgetId);
				gadgetRepository.delete(gadget);
			} else
				throw new GadgetDatasourceServiceException("Cannot delete gadget that does not exist");
		}

	}

	@Override
	public void updateGadget(GadgetDTO gadgetdto, String gadgetDatasourceIds, String jsonMeasures, User user) {
		Gadget gadget = gadgetDTO2Gadget(gadgetdto);
		final List<GadgetMeasure> lgmeasure = gadgetMeasureRepository.findByGadget(gadget);
		for (final GadgetMeasure gm : lgmeasure) {
			gadgetMeasureRepository.delete(gm);
		}
		categoryRelationService.deleteCategoryRelation(gadget.getId());

		final Gadget gadgetDB = gadgetRepository.findById(gadget.getId()).orElse(null);
		gadget.setId(gadgetDB.getId());
		gadget.setUser(gadgetDB.getUser());
		saveGadgetAndMeasures(gadget, gadgetDatasourceIds, jsonMeasures, user, gadgetdto);
	}

	@Override
	public void updateGadget(Gadget gadget, String datasourceId, List<GadgetMeasure> measures, String category,
			String subcategory) {
		final List<GadgetMeasure> lgmeasure = gadgetMeasureRepository.findByGadget(gadget);
		for (final GadgetMeasure gm : lgmeasure) {
			gadgetMeasureRepository.delete(gm);
		}
		categoryRelationService.deleteCategoryRelation(gadget.getId());

		final Gadget gadgetDB = gadgetRepository.findById(gadget.getId()).orElse(null);
		gadget.setId(gadgetDB.getId());
		gadget.setIdentification(gadgetDB.getIdentification());
		gadget.setUser(gadgetDB.getUser());
		saveGadgetAndMeasures(gadget, datasourceId, measures, category, subcategory);
	}

	@Override
	public Gadget createGadget(GadgetDTO gadgetdto, String gadgetDatasourceIds, String jsonMeasures, User user) {
		Gadget g = gadgetDTO2Gadget(gadgetdto);
		return saveGadgetAndMeasures(g, gadgetDatasourceIds, jsonMeasures, user, gadgetdto);
	}

	@Override
	public Boolean existGadgetWithIdentification(String identification) {
		List<Gadget> gadgets;

		if (identification != null) {
			gadgets = gadgetRepository.existByIdentification(identification);
			if (!gadgets.isEmpty()) {
				return Boolean.TRUE;
			} else {
				return Boolean.FALSE;
			}
		}
		return Boolean.FALSE;
	}

	@Override
	public String getElementsAssociated(String gadgetId) {
		final JSONArray elements = new JSONArray();
		final JSONObject ontology = new JSONObject();
		final JSONObject datasource = new JSONObject();
		final Gadget gadget = gadgetRepository.findById(gadgetId).orElse(null);

		if (gadget != null && !gadgetMeasureRepository.findByGadget(gadget).isEmpty()) {
			final GadgetMeasure gadgetMeasure = gadgetMeasureRepository.findByGadget(gadget).get(0);
			ontology.put("id", gadgetMeasure.getDatasource().getOntology().getId());
			ontology.put("identification", gadgetMeasure.getDatasource().getOntology().getIdentification());
			ontology.put("type", gadgetMeasure.getDatasource().getOntology().getClass().getSimpleName());

			datasource.put("id", gadgetMeasure.getDatasource().getId());
			datasource.put("identification", gadgetMeasure.getDatasource().getIdentification());
			datasource.put("type", gadgetMeasure.getDatasource().getClass().getSimpleName());

			elements.put(datasource);
			elements.put(ontology);
		}

		return elements.toString();
	}

	private List<MeasureDto> fromJSONMeasuresStringToListString(String inputStr) {
		final ObjectMapper objectMapper = new ObjectMapper();
		final TypeFactory typeFactory = objectMapper.getTypeFactory();
		List<MeasureDto> listStr = null;
		if (inputStr != null && inputStr.trim().length() > 0) {
			try {
				listStr = objectMapper.readValue(inputStr,
						typeFactory.constructCollectionType(List.class, MeasureDto.class));
			} catch (final IOException e) {

				log.error("Exception reached " + e.getMessage(), e);
			}
		}
		return listStr;
	}

	private List<String> fromStringToListString(String inputStr) {
		final ObjectMapper objectMapper = new ObjectMapper();
		final TypeFactory typeFactory = objectMapper.getTypeFactory();
		List<String> listStr = null;
		try {
			listStr = objectMapper.readValue(inputStr, typeFactory.constructCollectionType(List.class, String.class));
		} catch (final IOException e) {

			log.error("Exception reached " + e.getMessage(), e);
		}
		return listStr;
	}

	private Gadget saveGadgetAndMeasures(Gadget g, String gadgetDatasourceIds, String jsonMeasures, User user,
			GadgetDTO gdto) {
		g.setUser(user);
		g = gadgetRepository.save(g);
		createCategoryRelation(gdto, g.getId());

		final List<MeasureDto> listJsonMeasures = fromJSONMeasuresStringToListString(jsonMeasures);
		final List<String> listDatasources = fromStringToListString(gadgetDatasourceIds);
		if (listJsonMeasures != null && listDatasources != null) {
			for (int i = 0; i < listJsonMeasures.size(); i++) {
				final GadgetMeasure gadgetMeasure = new GadgetMeasure();
				gadgetMeasure.setGadget(g);
				gadgetMeasure.setDatasource(gadgetDatasourceService.getGadgetDatasourceById(listDatasources.get(0)));
				gadgetMeasure.setConfig(listJsonMeasures.get(i).getConfig());
				gadgetMeasureRepository.save(gadgetMeasure);
			}
		}

		return g;
	}

	private Gadget gadgetDTO2Gadget(GadgetDTO gadgetDto) {
		final Gadget gadget = new Gadget();
		gadget.setConfig(gadgetDto.getConfig());
		gadget.setDescription(gadgetDto.getDescription());
		gadget.setIdentification(gadgetDto.getIdentification());
		gadget.setPublic(gadgetDto.isPublic());
		gadget.setInstance(gadgetDto.isInstance());
		String[] typeSplit = gadgetDto.getType().split(",");
		GadgetTemplate gt = null;
		gt = this.gadgetTemplateService.getGadgetTemplateById(typeSplit[0]);
		if (gt == null) {
			gt = this.gadgetTemplateService.getGadgetTemplateById(typeSplit[1]);
		}
		gadget.setType(gt);
		gadget.setId(gadgetDto.getId());

		return gadget;
	}

	private void createCategoryRelation(GadgetDTO dto, String id) {
		if (!StringUtils.isEmpty(dto.getCategory()) && id != null) {
			final Category category = categoryService.getCategoryByIdentification(dto.getCategory());
			if (category == null) {
				throw new CategoryServiceException("Category does not exist");
			}
			if (!categoryService.isValidCategoryType(Category.Type.GADGET, category.getType())) {
				throw new CategoryServiceException("Type of category is not for this element");
			}
			Subcategory subcategory = new Subcategory();
			if (!StringUtils.isEmpty(dto.getSubcategory())) {
				subcategory = subcategoryService.getSubcategoryByIdentificationAndCategory(dto.getSubcategory(),
						category);
				if (subcategory == null) {
					throw new SubcategoryServiceException("Subcategory does not exist");
				}
			}
			categoryRelationService.createCategoryRelation(id, category, subcategory, Category.Type.GADGET);
		}
	}

	private void createCategoryRelation(String categorystr, String subcategorystr, String id) {
		if (!StringUtils.isEmpty(categorystr) && id != null) {
			final Category category = categoryService.getCategoryByIdentification(categorystr);
			if (category == null) {
				throw new CategoryServiceException("Category does not exist");
			}
			if (!categoryService.isValidCategoryType(Category.Type.GADGET, category.getType())) {
				throw new CategoryServiceException("Type of category is not for this element");
			}
			Subcategory subcategory = new Subcategory();
			if (!StringUtils.isEmpty(subcategorystr)) {
				subcategory = subcategoryService.getSubcategoryByIdentificationAndCategory(subcategorystr, category);
				if (subcategory == null) {
					throw new SubcategoryServiceException("Subcategory does not exist");
				}
			}
			categoryRelationService.createCategoryRelation(id, category, subcategory, Category.Type.GADGET);
		}
	}

	private Gadget saveGadgetAndMeasures(Gadget g, String datasourceId, List<GadgetMeasure> gadgetMeasures,
			String category, String subcategory) {
		g = gadgetRepository.save(g);
		if (category != null)
			createCategoryRelation(category, subcategory, g.getId());

		if (gadgetMeasures != null && datasourceId != null) {
			for (final Iterator<GadgetMeasure> iterator = gadgetMeasures.iterator(); iterator.hasNext();) {
				final GadgetMeasure gadgetMeasure = iterator.next();
				gadgetMeasure.setGadget(g);
				gadgetMeasure.setDatasource(gadgetDatasourceService.getGadgetDatasourceById(datasourceId));
				gadgetMeasureRepository.save(gadgetMeasure);
			}
		}

		return g;
	}

	@Override
	public Gadget createGadget(Gadget g, GadgetDatasource datasource, List<GadgetMeasure> gadgetMeasures,
			String category, String subcategory) {
		g = gadgetRepository.save(g);
		if (category != null)
			createCategoryRelation(category, subcategory, g.getId());

		if (gadgetMeasures != null && datasource != null) {
			for (final Iterator<GadgetMeasure> iterator = gadgetMeasures.iterator(); iterator.hasNext();) {
				final GadgetMeasure gadgetMeasure = iterator.next();
				gadgetMeasure.setGadget(g);
				gadgetMeasure.setDatasource(datasource);
				gadgetMeasureRepository.save(gadgetMeasure);
			}
		}

		return g;
	}

	@Override
	public void addMeasuresGadget(Gadget gadget, String datasourceId, List<GadgetMeasure> newMeasures, String category,
			String subcategory) {
		if (gadget != null && datasourceId != null && newMeasures != null) {
			final List<GadgetMeasure> oldMeasures = gadgetMeasureRepository.findByGadget(gadget);

			for (final GadgetMeasure oldMeasure : oldMeasures) {
				newMeasures.removeIf(b -> b.getConfig().contains(oldMeasure.getConfig()));
			}
			gadgetRepository.findById(gadget.getId()).ifPresent(gadgetDB -> {
				gadget.setId(gadgetDB.getId());
				gadget.setIdentification(gadgetDB.getIdentification());
				gadget.setUser(gadgetDB.getUser());
				saveGadgetAndMeasures(gadget, datasourceId, newMeasures, category, subcategory);
			});

		}
	}

	@Override
	public Gadget getGadgetByIdentification(String userID, String gadgetIdentification) {
		return gadgetRepository.findByIdentification(gadgetIdentification);
	}

	@Override
	public List<String> getGadgetTypes(String userId) {

		final User user = userRepository.findByUserId(userId);

		if (userService.isUserAdministrator(user)) {
			return gadgetRepository.findGadgetTypes();
		} else {
			return gadgetRepository.findGadgetTypesbyUser(user);
		}
	}

	@Override
	public List<OPResourceDTO> getDtoByUserAndPermissions(String userId, String identification, String description) {
		User user = userService.getUser(userId);
		if (user.isAdmin()) {
			return gadgetRepository.findAllDto(identification, description);
		} else {
			return gadgetRepository.findDtoByUserAndPermissions(user, identification, description);
		}
	}

	@Override
	public void updateInstance(String id, String config) {
		final Gadget gadgetDB = gadgetRepository.findById(id).orElse(null);
		gadgetDB.setConfig(config);
		gadgetRepository.save(gadgetDB);
	}

	@Override
	public ResponseEntity<byte[]> generateImg(String id, int waittime, int height, int width, boolean fullpage,
			String params, String oauthtoken) {

		final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

		final HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.IMAGE_PNG_VALUE);
		headers.add("Access-Control-Allow-Methods", "GET");
		final HttpEntity<?> entity = new HttpEntity<>(headers);

		final UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format(TO_IMG, dashboardexporturl))
				.queryParam("waittime", waittime).queryParam("url", prefixURLView + id).queryParam("fullpage", fullpage)
				.queryParam("width", width).queryParam("height", height).queryParam("oauthtoken", oauthtoken)
				.queryParam("dashboardsparams", params);

		return restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, byte[].class);
	}

	@Override
	public ResponseEntity<byte[]> generatePDF(String id, int waittime, int height, int width, String params,
			String oauthtoken) {
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
	public String cloneGadget(Gadget gadget, String identification, User user) {
		final Gadget cloneGadget = new Gadget();

		try {

			cloneGadget.setIdentification(identification);
			cloneGadget.setUser(user);
			cloneGadget.setConfig(gadget.getConfig());
			cloneGadget.setDescription(gadget.getDescription());
			cloneGadget.setPublic(gadget.isPublic());
			cloneGadget.setInstance(gadget.isInstance());
			cloneGadget.setType(gadget.getType());

			gadgetRepository.save(cloneGadget);

			List<GadgetMeasure> gadgetMeasures = gadgetMeasureRepository.findByGadget(gadget);
			for (GadgetMeasure gm : gadgetMeasures) {
				GadgetMeasure cloneGm = new GadgetMeasure();
				cloneGm.setGadget(cloneGadget);
				cloneGm.setConfig(gm.getConfig());
				cloneGm.setDatasource(gm.getDatasource());
				gadgetMeasureRepository.save(cloneGm);
			}

			CategoryRelation cr = categoryRelationRepository.findByTypeId(gadget.getId());
			if (cr != null) {
				CategoryRelation cloneCR = new CategoryRelation();
				cloneCR.setTypeId(cloneGadget.getId());
				cloneCR.setCategory(cr.getCategory());
				cloneCR.setSubcategory(cr.getSubcategory());
				cloneCR.setType(cr.getType());
				categoryRelationRepository.save(cloneCR);
			}

			return cloneGadget.getId();
		} catch (final Exception e) {

			log.error(e.getMessage());
			return null;
		}
	}

}
