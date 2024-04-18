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
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.dto.GadgetDatasourceForList;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess.ResourceAccessType;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.GadgetDatasourceRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.exceptions.GadgetDatasourceServiceException;
import com.minsait.onesait.platform.config.services.gadget.dto.GadgetDatasourceDTOForList;
import com.minsait.onesait.platform.config.services.generic.security.SecurityService;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.project.ProjectService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GadgetDatasourceServiceImpl implements GadgetDatasourceService {

	@Autowired
	private GadgetDatasourceRepository gadgetDatasourceRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private OPResourceService resourceService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	SecurityService securityService;
	public static final String ADMINISTRATOR = "ROLE_ADMINISTRATOR";

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

		if (user.getRole().getId().equals(GadgetServiceImpl.ADMINISTRATOR)) {
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
		return gadgetDatasourceRepository.findIdentificationByOrderByIdentificationAsc();
	}

	@Override
	public GadgetDatasource getGadgetDatasourceById(String id) {
		return gadgetDatasourceRepository.findById(id);
	}

	@Override
	public GadgetDatasource createGadgetDatasource(GadgetDatasource gadgetDatasource) {
		if (!gadgetDatasourceExists(gadgetDatasource)) {
			log.debug("Gadget datasource no exist, creating...");
			return gadgetDatasourceRepository.save(gadgetDatasource);
		} else {
			throw new GadgetDatasourceServiceException("Gadget Datasource already exists in Database");
		}
	}

	@Override
	public boolean gadgetDatasourceExists(GadgetDatasource gadgetDatasource) {
		return gadgetDatasourceRepository.findByIdentification(gadgetDatasource.getIdentification()) != null;

	}

	@Override
	public void updateGadgetDatasource(GadgetDatasource gadgetDatasource) {
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
		if (user.getRole().getId().equals(ADMINISTRATOR)) {
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
		if (user.getRole().getId().equals(ADMINISTRATOR)
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
		if (user.getRole().getId().equals(ADMINISTRATOR)) {
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
		if (user.getRole().getId().equals(ADMINISTRATOR)) {
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

		final int i = query.toLowerCase().lastIndexOf("limit ");
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

}
