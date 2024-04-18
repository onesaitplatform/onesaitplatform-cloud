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
package com.minsait.onesait.platform.config.services.gadget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.dto.GadgetDatasourceForList;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.model.GadgetMeasure;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.GadgetDatasourceRepository;
import com.minsait.onesait.platform.config.repository.GadgetMeasureRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.exceptions.GadgetDatasourceServiceException;
import com.minsait.onesait.platform.config.services.gadget.dto.GadgetDatasourceDTOForList;
import com.minsait.onesait.platform.config.services.generic.security.SecurityService;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.project.ProjectService;
import com.minsait.onesait.platform.config.services.user.UserService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GadgetDatasourceServiceImpl implements GadgetDatasourceService {

	private static final String LIMIT_UPPERCASE = "LIMIT ";
	private static final String LIMIT_LOWERCASE = "limit ";
	private static final String PLUS_CHARACTER = " +";
	private static final String FROM_UPPERCASE = "FROM ";
	private static final String FROM_LOWERCASE = "from ";
	private static final String CLOSE_PARENTHESIS = ")";
	private static final String OPEN_PARENTHESIS = "(";
	private static final String BLANK_CHARACTER = " ";
	@Autowired
	private GadgetDatasourceRepository gadgetDatasourceRepository;
	@Autowired
	private GadgetMeasureRepository gadgetMeasureRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private OPResourceService resourceService;
	@Autowired
	private UserService userService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	SecurityService securityService;

	@Override
	public List<GadgetDatasource> findAllDatasources() {
		return gadgetDatasourceRepository.findAll();

	}

	@Override
	public List<GadgetDatasourceForList> findGadgetDatasourceWithIdentificationAndDescription(String identification,
			String description, String userId) {

		description = description == null ? "" : description;
		identification = identification == null ? "" : identification;

		List<GadgetDatasourceForList> datasources;
		final User user = userRepository.findByUserId(userId);

		if (userService.isUserAdministrator(user)) {
			datasources = gadgetDatasourceRepository
					.findForListByIdentificationContainingAndDescriptionContaining(identification, description);
		} else {
			datasources = gadgetDatasourceRepository
					.findForListByUserAndIdentificationContainingAndDescriptionContaining(user, identification,
							description);
		}
		return datasources;
	}

	@Override
	public List<String> getAllIdentifications() {
		final List<GadgetDatasource> datasources = gadgetDatasourceRepository.findAllByOrderByIdentificationAsc();
		final List<String> names = new ArrayList<>();
		for (final GadgetDatasource datasource : datasources) {
			names.add(datasource.getIdentification());

		}
		return names;
	}

	@Override
	public GadgetDatasource getGadgetDatasourceById(String id) {
		return gadgetDatasourceRepository.findById(id);
	}

	@Override
	public GadgetDatasource createGadgetDatasource(GadgetDatasource gadgetDatasource) {
		if (gadgetDatasource.getOntology() == null) {
			throw new GadgetDatasourceServiceException("Ontology is a field required.");
		}
		if (!isOntologyOnQuery(gadgetDatasource.getOntology().getIdentification(), gadgetDatasource.getQuery())){
			throw new GadgetDatasourceServiceException(
					"The query: "+gadgetDatasource.getQuery()+" is not for the ontology selected: "+gadgetDatasource.getOntology().getIdentification()) ;
		}
		if (!gadgetDatasourceExists(gadgetDatasource)) {
			log.debug("Gadget datasource no exist, creating...");
			return gadgetDatasourceRepository.save(gadgetDatasource);
		} else {
			throw new GadgetDatasourceServiceException(
					"GadgetDatasource with identification: " + gadgetDatasource.getIdentification() + " exist");
		}
	}

	@Override
	public boolean gadgetDatasourceExists(GadgetDatasource gadgetDatasource) {
		return gadgetDatasourceRepository.findByIdentification(gadgetDatasource.getIdentification()) != null;

	}

	@Override
	public void updateGadgetDatasource(GadgetDatasource gadgetDatasource) {
		if (gadgetDatasource.getOntology() == null) {
			throw new GadgetDatasourceServiceException("Ontology is a field required.");
		}
		if (!isOntologyOnQuery(gadgetDatasource.getOntology().getIdentification(), gadgetDatasource.getQuery())){
			throw new GadgetDatasourceServiceException(
					"The query: "+gadgetDatasource.getQuery()+" is not for the ontology selected: "+gadgetDatasource.getOntology().getIdentification()) ;
		}
		if (gadgetDatasourceExists(gadgetDatasource)) {
			final GadgetDatasource gadgetDatasourceDB = gadgetDatasourceRepository.findById(gadgetDatasource.getId());
			gadgetDatasourceDB.setConfig(gadgetDatasource.getConfig());
			gadgetDatasourceDB.setDbtype(gadgetDatasource.getDbtype());
			gadgetDatasourceDB.setDescription(gadgetDatasource.getDescription());
			gadgetDatasourceDB.setMaxvalues(gadgetDatasource.getMaxvalues());
			gadgetDatasourceDB.setMode(gadgetDatasource.getMode());
			gadgetDatasourceDB.setOntology(gadgetDatasource.getOntology());
			gadgetDatasourceDB.setQuery(gadgetDatasource.getQuery());
			gadgetDatasourceDB.setRefresh(gadgetDatasource.getRefresh());
			gadgetDatasourceRepository.save(gadgetDatasourceDB);
		} else {
			throw new GadgetDatasourceServiceException("Cannot update GadgetDatasource that does not exist");
		}
	}

	@Override
	public void deleteGadgetDatasource(String gadgetDatasourceId, String userId) {
		if (hasUserEditPermission(gadgetDatasourceId, userId)) {
			final GadgetDatasource gadgetDatasource = gadgetDatasourceRepository.findById(gadgetDatasourceId);
			if (gadgetDatasource != null) {
				gadgetDatasourceRepository.delete(gadgetDatasource);
			} else {
				throw new GadgetDatasourceServiceException("Cannot delete gadget datasource that does not exist");
			}
		}

	}

	@Override
	public boolean hasUserPermission(String id, String userId) {
		final User user = userRepository.findByUserId(userId);
		if (userService.isUserAdministrator(user)) {
			return true;
		} else if (gadgetDatasourceRepository.findById(id).getUser().getUserId().equals(userId)) {
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
	public String getAccessType(String id, String userId) {
		final User user = userRepository.findByUserId(userId);
		if (userService.isUserAdministrator(user)
				|| gadgetDatasourceRepository.findById(id).getUser().getUserId().equals(userId)
				|| resourceService.hasAccess(userId, id, ResourceAccessType.MANAGE)) {
			return ResourceAccessType.MANAGE.toString();
		} else if (resourceService.hasAccess(userId, id, ResourceAccessType.VIEW)) {
			return ResourceAccessType.VIEW.toString();
		}
		return null;
	}

	@Override
	public boolean hasUserEditPermission(String id, String userId) {
		return hasUserPermission(id, userId) || resourceService.hasAccess(userId, id, ResourceAccessType.MANAGE);
	}

	@Override
	public List<GadgetDatasource> getUserGadgetDatasources(String userId) {
		final User user = userRepository.findByUserId(userId);
		if (userService.isUserAdministrator(user)) {
			return gadgetDatasourceRepository.findAllByOrderByIdentificationAsc();
		} else {
			final List<GadgetDatasource> result = gadgetDatasourceRepository.findByUserOrderByIdentificationAsc(user);
			result.addAll(projectService.getResourcesForUserOfType(userId, GadgetDatasource.class));
			return result;
		}
	}

	@Override
	public List<GadgetDatasourceDTOForList> getUserGadgetDatasourcesForList(String userId) {
		final User user = userRepository.findByUserId(userId);
		List<GadgetDatasourceForList> datasourcesForList = new ArrayList<>();
		
		if (userService.isUserAdministrator(user)) {
			datasourcesForList = gadgetDatasourceRepository.findAllForListByOrderByIdentificationAsc();
		} else {
			datasourcesForList = gadgetDatasourceRepository.findForListByUserOrderByIdentificationAsc(user);
			securityService.setSecurityToInputList(datasourcesForList, user, "GadgetDatasource");
		}
		List<GadgetDatasourceDTOForList> dtos = new ArrayList<>();
		for (GadgetDatasourceForList temp : datasourcesForList) {
			if (temp.getAccessType() != null) {

				final GadgetDatasourceDTOForList obj = new GadgetDatasourceDTOForList();

				obj.setCreatedAt(temp.getCreated_at());
				obj.setDescription(temp.getDescription());
				obj.setId(temp.getId());
				obj.setIdentification(temp.getIdentification());
				obj.setUpdatedAt(temp.getUpdated_at());
				obj.setUser(temp.getUser());
				obj.setDbtype(temp.getDbtype());
				obj.setMaxvalues(temp.getMaxValues());
				obj.setMode(temp.getMode());
				obj.setOntologyIdentification(temp.getOntologyIdentification());
				obj.setQuery(temp.getQuery());
				obj.setRefresh(temp.getRefresh());
				dtos.add(obj);
			}
		}

		return dtos;
	}

	@Override
	public String getSampleQueryGadgetDatasourceById(String datasourceId, String ontology, String user) {
		final String query = gadgetDatasourceRepository.findById(datasourceId).getQuery();

		final int i = query.toLowerCase().lastIndexOf(LIMIT_LOWERCASE);
		if (i == -1) {// Add limit add the end
			return query + " limit 1";
		} else {
			return query.substring(0, i) + " limit 1";
		}
	}

	@Override
	public GadgetDatasource getDatasourceByIdentification(String dsIdentification) {
		return gadgetDatasourceRepository.findByIdentification(dsIdentification);
	}

	@Override
	public boolean isGroupDatasourceById(String id) {
		GadgetDatasource datasource = gadgetDatasourceRepository.findById(id);
		return datasource.getQuery().toLowerCase().indexOf(" group by ") != -1;
	}

	@Override
	public String getElementsAssociated(String datasourceId) {
		final JSONArray elements = new JSONArray();
		final JSONObject element = new JSONObject();

		final GadgetDatasource datasource = gadgetDatasourceRepository.findById(datasourceId);

		if (datasource != null) {
			element.put("id", datasource.getOntology().getId());
			element.put("identification", datasource.getOntology().getIdentification());
			element.put("type", datasource.getOntology().getClass().getSimpleName());

			elements.put(element);
		}

		return elements.toString();
	}

	@Override
	public String getOntologyFromDatasource(String datasource) {
		datasource = datasource.replaceAll("\\t|\\r|\\r\\n\\t|\\n|\\r\\t", BLANK_CHARACTER);
		datasource = datasource.trim().replaceAll(PLUS_CHARACTER, BLANK_CHARACTER);
		String[] list = datasource.split(FROM_LOWERCASE);
		if (list.length == 1) {
			list = datasource.split(FROM_UPPERCASE);
		}
		if (list.length > 1) {
			for (int i = 1; i < list.length; i++) {
				if (!list[i].startsWith(OPEN_PARENTHESIS)) {
					int indexOf = list[i].toLowerCase().indexOf(BLANK_CHARACTER, 0);
					int indexOfCloseBracket = list[i].toLowerCase().indexOf(CLOSE_PARENTHESIS, 0);
					indexOf = (indexOfCloseBracket != -1 && indexOfCloseBracket < indexOf) ? indexOfCloseBracket
							: indexOf;
					if (indexOf == -1) {
						indexOf = list[i].length();
					}
					return list[i].substring(0, indexOf).trim();
				}
			}
		} else {
			return "";
		}
		return "";
	}

	@Override
	public String getMaxValuesFromQuery(String query) {
		query = query.replaceAll("\\t|\\r|\\r\\n\\t|\\n|\\r\\t", BLANK_CHARACTER);
		query = query.trim().replaceAll(PLUS_CHARACTER, BLANK_CHARACTER);
		String[] list = query.split(LIMIT_LOWERCASE);
		if (list.length == 1) {
			list = query.split(LIMIT_UPPERCASE);
		}
		if (list.length > 1) {
			return list[1].trim();

		}
		return null;
	}
	
	private Boolean isOntologyOnQuery (String ontology, String query) {
		query = query.replace("\n", "");		
		while (query.contains("  ")) {
			query = query.replace("  ", " ");
		}
		
		if (query.toLowerCase().indexOf("from ") == -1 && query.toLowerCase().indexOf("update ") == -1
				&& query.toLowerCase().indexOf("join ") == -1)
			return false;
		else if (query.toLowerCase().indexOf("from " + ontology.toLowerCase()) == -1
				&& query.toLowerCase().indexOf("update " + ontology.toLowerCase()) == -1
				&& query.toLowerCase().indexOf("join " + ontology.toLowerCase()) == -1)
			return false;
		
		return true;
	}

	@Override
	public List<Object> getGadgetsUsingDatasource(String id) {
		List<GadgetMeasure> gadgetMeasureList = gadgetMeasureRepository.findByDatasource(id);
		HashMap<String, String> gadgetsIdentification = new HashMap<>();
		for (GadgetMeasure gadgetMeasure : gadgetMeasureList) {
			gadgetsIdentification.put(gadgetMeasure.getGadget().getIdentification(), gadgetMeasure.getGadget().getIdentification());
		}
		return Arrays.asList(gadgetsIdentification.keySet().toArray());
	}

}
