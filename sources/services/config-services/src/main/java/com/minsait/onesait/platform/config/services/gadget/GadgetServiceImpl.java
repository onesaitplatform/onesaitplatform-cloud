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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.minsait.onesait.platform.config.model.Gadget;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.model.GadgetMeasure;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess.ResourceAccessType;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.GadgetMeasureRepository;
import com.minsait.onesait.platform.config.repository.GadgetRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.exceptions.GadgetDatasourceServiceException;
import com.minsait.onesait.platform.config.services.exceptions.OPResourceServiceException;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;

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
	private UserRepository userRepository;

	@Autowired
	private OPResourceService resourceService;

	public static final String ADMINISTRATOR = "ROLE_ADMINISTRATOR";

	@Override
	public List<Gadget> findAllGadgets() {
		return gadgetRepository.findAll();
	}

	@Override
	public List<Gadget> findGadgetWithIdentificationAndType(String identification, String type, String userId) {
		List<Gadget> gadgets;
		final User user = userRepository.findByUserId(userId);

		if (user.getRole().getId().equals(GadgetServiceImpl.ADMINISTRATOR)) {
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
	public Gadget getGadgetById(String userID, String gadgetId) {
		return gadgetRepository.findById(gadgetId);
	}

	@Override
	public void createGadget(Gadget gadget) {
		if (gadgetRepository.findByIdentification(gadget.getIdentification()) == null) {
			gadgetRepository.save(gadget);
		}

	}

	@Override
	public List<Gadget> getUserGadgetsByType(String userID, String type) {
		final User user = userRepository.findByUserId(userID);
		final List<Gadget> gadgets = gadgetRepository.findByTypeOrderByIdentificationAsc(type);
		if (user.getRole().getId().equals(ADMINISTRATOR)) {
			return gadgets;
		} else {
			return gadgets.stream()
					.filter(g -> g.getUser().getUserId().equals(userID)
							|| resourceService.hasAccess(userID, g.getId(), ResourceAccessType.VIEW))
					.collect(Collectors.toList());
		}
	}

	@Override
	public List<GadgetMeasure> getGadgetMeasuresByGadgetId(String userID, String gadgetId) {
		return gadgetMeasureRepository.findByGadget(gadgetRepository.findById(gadgetId));
	}

	@Override
	public boolean hasUserPermission(String id, String userId) {
		final User user = userRepository.findByUserId(userId);
		final Gadget gadget = gadgetRepository.findById(id);
		if (user.getRole().getId().equals(ADMINISTRATOR)) {
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
	public void deleteGadget(String gadgetId, String userId) {
		if (hasUserPermission(gadgetId, userId)) {
			final Gadget gadget = gadgetRepository.findById(gadgetId);
			if (gadget != null) {
				if (resourceService.isResourceSharedInAnyProject(gadget))
					throw new OPResourceServiceException(
							"This gadget is shared within a Project, revoke access from project prior to deleting");
				final List<GadgetMeasure> lgmeasure = gadgetMeasureRepository.findByGadget(gadget);
				for (final GadgetMeasure gm : lgmeasure) {
					gadgetMeasureRepository.delete(gm);
				}
				gadgetRepository.delete(gadget);
			} else
				throw new GadgetDatasourceServiceException("Cannot delete gadget that does not exist");
		}

	}

	@Override
	public void updateGadget(Gadget gadget, String gadgetDatasourceIds, String jsonMeasures) {
		final List<GadgetMeasure> lgmeasure = gadgetMeasureRepository.findByGadget(gadget);
		for (final GadgetMeasure gm : lgmeasure) {
			gadgetMeasureRepository.delete(gm);
		}
		final Gadget gadgetDB = gadgetRepository.findById(gadget.getId());
		gadget.setId(gadgetDB.getId());
		gadget.setUser(gadgetDB.getUser());
		saveGadgetAndMeasures(gadget, gadgetDatasourceIds, jsonMeasures);
	}

	@Override
	public void updateGadget(Gadget gadget, String datasourceId, List<GadgetMeasure> measures) {
		final List<GadgetMeasure> lgmeasure = gadgetMeasureRepository.findByGadget(gadget);
		for (final GadgetMeasure gm : lgmeasure) {
			gadgetMeasureRepository.delete(gm);
		}
		final Gadget gadgetDB = gadgetRepository.findById(gadget.getId());
		gadget.setId(gadgetDB.getId());
		gadget.setIdentification(gadgetDB.getIdentification());
		gadget.setUser(gadgetDB.getUser());
		saveGadgetAndMeasures(gadget, datasourceId, measures);
	}

	@Override
	public Gadget createGadget(Gadget gadget, String gadgetDatasourceIds, String jsonMeasures) {
		return saveGadgetAndMeasures(gadget, gadgetDatasourceIds, jsonMeasures);
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
		JSONArray elements = new JSONArray();
		JSONObject ontology = new JSONObject();
		JSONObject datasource = new JSONObject();
		Gadget gadget = gadgetRepository.findById(gadgetId);

		if (gadget != null && !gadgetMeasureRepository.findByGadget(gadget).isEmpty()) {
			GadgetMeasure gadgetMeasure = gadgetMeasureRepository.findByGadget(gadget).get(0);
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
		try {
			listStr = objectMapper.readValue(inputStr,
					typeFactory.constructCollectionType(List.class, MeasureDto.class));
		} catch (final IOException e) {

			log.error("Exception reached " + e.getMessage(), e);
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

	private Gadget saveGadgetAndMeasures(Gadget g, String gadgetDatasourceIds, String jsonMeasures) {
		g = gadgetRepository.save(g);

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

	private Gadget saveGadgetAndMeasures(Gadget g, String datasourceId, List<GadgetMeasure> gadgetMeasures) {
		g = gadgetRepository.save(g);

		if (gadgetMeasures != null && datasourceId != null) {
			for (Iterator<GadgetMeasure> iterator = gadgetMeasures.iterator(); iterator.hasNext();) {
				GadgetMeasure gadgetMeasure = (GadgetMeasure) iterator.next();
				gadgetMeasure.setGadget(g);
				gadgetMeasure.setDatasource(gadgetDatasourceService.getGadgetDatasourceById(datasourceId));
				gadgetMeasureRepository.save(gadgetMeasure);
			}
		}

		return g;
	}

	@Override
	public Gadget createGadget(Gadget g, GadgetDatasource datasource, List<GadgetMeasure> gadgetMeasures) {

		g = gadgetRepository.save(g);

		if (gadgetMeasures != null && datasource != null) {
			for (Iterator<GadgetMeasure> iterator = gadgetMeasures.iterator(); iterator.hasNext();) {
				GadgetMeasure gadgetMeasure = (GadgetMeasure) iterator.next();
				gadgetMeasure.setGadget(g);
				gadgetMeasure.setDatasource(datasource);
				gadgetMeasureRepository.save(gadgetMeasure);
			}
		}

		return g;
	}

	@Override
	public void addMeasuresGadget(Gadget gadget, String datasourceId, List<GadgetMeasure> newMeasures) {
		if (gadget != null && datasourceId != null && newMeasures != null) {
			final List<GadgetMeasure> oldMeasures = gadgetMeasureRepository.findByGadget(gadget);

			for (final GadgetMeasure oldMeasure : oldMeasures) {
				newMeasures.removeIf(b -> b.getConfig().contains(oldMeasure.getConfig()));
			}
			final Gadget gadgetDB = gadgetRepository.findById(gadget.getId());
			gadget.setId(gadgetDB.getId());
			gadget.setIdentification(gadgetDB.getIdentification());
			gadget.setUser(gadgetDB.getUser());
			saveGadgetAndMeasures(gadget, datasourceId, newMeasures);
		}
	}

}
