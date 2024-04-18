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
package com.minsait.onesait.platform.config.services.digitaltwin.type;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.minsait.onesait.platform.config.model.ActionsDigitalTwinType;
import com.minsait.onesait.platform.config.model.DigitalTwinType;
import com.minsait.onesait.platform.config.model.EventsDigitalTwinType;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.PropertyDigitalTwinType;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.ActionsDigitalTwinTypeRepository;
import com.minsait.onesait.platform.config.repository.DataModelRepository;
import com.minsait.onesait.platform.config.repository.DigitalTwinTypeRepository;
import com.minsait.onesait.platform.config.repository.EventsDigitalTwinTypeRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.PropertyDigitalTwinTypeRepository;
import com.minsait.onesait.platform.config.services.exceptions.DigitalTwinServiceException;
import com.minsait.onesait.platform.config.services.ontology.OntologyConfiguration;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.user.UserService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DigitalTwinTypeServiceImpl implements DigitalTwinTypeService {

	@Autowired
	DigitalTwinTypeRepository digitalTwinTypeRepo;

	@Autowired
	PropertyDigitalTwinTypeRepository propDigitalTwinTypeRepo;

	@Autowired
	ActionsDigitalTwinTypeRepository actDigitalTwinTypeRepo;

	@Autowired
	EventsDigitalTwinTypeRepository evtDigitalTwinTypeRepo;

	@Autowired
	private UserService userService;

	@Autowired
	private DataModelRepository dataModelRepo;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private OntologyRepository ontologyRepo;

	private static final String PROP_STR = "propiedades";
	private static final String DESC_STR = "description";
	private static final String TWIN_PROP_STR = "TwinProperties";

	@Override
	public DigitalTwinType getDigitalTwinTypeById(String id) {
		return digitalTwinTypeRepo.findById(id);
	}

	@Override
	public List<PropertyDigitalTwinTypeDTO> getPropertiesByDigitalId(String typeId) {

		final List<PropertyDigitalTwinTypeDTO> lPropertiesDTO = new ArrayList<>();
		final List<PropertyDigitalTwinType> lProperties = propDigitalTwinTypeRepo
				.findByTypeId(digitalTwinTypeRepo.findById(typeId));

		for (final PropertyDigitalTwinType prop : lProperties) {
			lPropertiesDTO.add(new PropertyDigitalTwinTypeDTO(prop.getId(), prop.getType(), prop.getName(),
					prop.getUnit(), prop.getDirection(), prop.getDescription()));
		}

		return lPropertiesDTO;
	}

	@Override
	public List<ActionsDigitalTwinTypeDTO> getActionsByDigitalId(String typeId) {

		final List<ActionsDigitalTwinTypeDTO> lActionsDTO = new ArrayList<>();
		final List<ActionsDigitalTwinType> lActions = actDigitalTwinTypeRepo
				.findByTypeId(digitalTwinTypeRepo.findById(typeId));

		for (final ActionsDigitalTwinType act : lActions) {
			lActionsDTO.add(new ActionsDigitalTwinTypeDTO(act.getId(), act.getName(), act.getDescription()));
		}

		return lActionsDTO;
	}

	@Override
	public List<EventsDigitalTwinTypeDTO> getEventsByDigitalId(String typeId) {

		final List<EventsDigitalTwinTypeDTO> lEventsDTO = new ArrayList<>();
		final List<EventsDigitalTwinType> lEvents = evtDigitalTwinTypeRepo
				.findByTypeId(digitalTwinTypeRepo.findById(typeId));

		for (final EventsDigitalTwinType event : lEvents) {
			if (!event.getType().equalsIgnoreCase(EventsDigitalTwinType.Type.PING.name())
					&& !event.getType().equalsIgnoreCase(EventsDigitalTwinType.Type.REGISTER.name())) {
				lEventsDTO.add(new EventsDigitalTwinTypeDTO(event.getId(), event.getType(), event.getName(),
						event.isStatus(), event.getDescription()));
			}
		}

		return lEventsDTO;
	}

	@Override
	public String getLogicByDigitalId(String typeId) {
		final String logic = digitalTwinTypeRepo.findById(typeId).getLogic();
		if (logic != null) {
			return logic.replace("\\r", "");
		}
		return "";
	}

	@Override
	public List<String> getAllIdentifications() {
		final List<DigitalTwinType> digitalTypes = digitalTwinTypeRepo.findAllByOrderByNameAsc();
		final List<String> identifications = new ArrayList<>();
		for (final DigitalTwinType type : digitalTypes) {
			identifications.add(type.getName());
		}
		return identifications;
	}

	@Override
	public List<DigitalTwinType> getAll() {
		return digitalTwinTypeRepo.findAll();
	}

	@Override
	public void createDigitalTwinType(DigitalTwinType digitalTwinType, HttpServletRequest httpServletRequest) {
		if (digitalTwinTypeRepo.findByName(digitalTwinType.getName()) == null) {
			try {

				final String[] properties = httpServletRequest.getParameterValues(PROP_STR);
				final String[] actions = httpServletRequest.getParameterValues("acciones");
				final String[] events = httpServletRequest.getParameterValues("eventos");
				final String logic = httpServletRequest.getParameter("logic");

				final Set<PropertyDigitalTwinType> propertyDigitalTwinTypes = new HashSet<>();
				final Set<ActionsDigitalTwinType> actionDigitalTwinTypes = new HashSet<>();
				final Set<EventsDigitalTwinType> eventDigitalTwinTypes = new HashSet<>();

				final User user = userService.getUser(digitalTwinType.getUser().getUserId());
				if (user != null) {
					digitalTwinType.setUser(user);

					// Add PING and REGISTER events by default
					final EventsDigitalTwinType ping = new EventsDigitalTwinType();
					ping.setName("ping");
					ping.setStatus(true);
					ping.setType(EventsDigitalTwinType.Type.PING);
					ping.setDescription("Ping the platform to keepalive the device");
					ping.setTypeId(digitalTwinType);
					eventDigitalTwinTypes.add(ping);

					final EventsDigitalTwinType register = new EventsDigitalTwinType();
					register.setDescription("REGISTER");
					register.setName("register");
					register.setStatus(true);
					register.setType(EventsDigitalTwinType.Type.REGISTER);
					register.setDescription("Register the device into the plaform");
					register.setTypeId(digitalTwinType);
					eventDigitalTwinTypes.add(register);

					addProperties(properties, propertyDigitalTwinTypes, digitalTwinType);
					addActions(actions, actionDigitalTwinTypes, digitalTwinType);
					addEvents(events, eventDigitalTwinTypes, digitalTwinType);

					digitalTwinType.setPropertyDigitalTwinTypes(propertyDigitalTwinTypes);
					digitalTwinType.setActionDigitalTwinTypes(actionDigitalTwinTypes);
					digitalTwinType.setEventDigitalTwinTypes(eventDigitalTwinTypes);
					digitalTwinType.setLogic(logic.replace("\\n", System.getProperty("line.separator"))
							.replace("\\r", "").replace("\\t", "   "));
					digitalTwinTypeRepo.save(digitalTwinType);
				} else {
					log.error("Invalid user");
				}

			} catch (final Exception e) {
				throw new DigitalTwinServiceException("Problems creating the digital twin type", e);
			}

		} else {
			throw new DigitalTwinServiceException(
					"Digital Twin Type with identification: " + digitalTwinType.getName() + " exists");
		}
	}

	private void addProperties(String[] properties, Set<PropertyDigitalTwinType> propertyDigitalTwinTypes,
			DigitalTwinType digitalTwinType) {
		if (properties != null && !properties[0].equals("")) {
			for (final String prop : properties) {
				final JSONObject json = new JSONObject(prop);
				final PropertyDigitalTwinType p = new PropertyDigitalTwinType();
				p.setDescription(json.getString(DESC_STR));
				p.setName(json.getString("name"));
				p.setType(json.getString("type"));
				p.setUnit(json.getString("units"));
				p.setDirection(PropertyDigitalTwinType.Direction.valueOf(json.getString("direction").toUpperCase()));
				p.setTypeId(digitalTwinType);
				propertyDigitalTwinTypes.add(p);
			}
		}
	}

	private void addActions(String[] actions, Set<ActionsDigitalTwinType> actionDigitalTwinTypes,
			DigitalTwinType digitalTwinType) {
		if (actions != null && !actions[0].equals("")) {
			for (final String action : actions) {
				final ActionsDigitalTwinType act = new ActionsDigitalTwinType();
				final JSONObject json = new JSONObject(action);
				act.setName(json.getString("name"));
				act.setDescription(json.getString(DESC_STR));
				act.setTypeId(digitalTwinType);
				actionDigitalTwinTypes.add(act);
			}
		}
	}

	private void addEvents(String[] events, Set<EventsDigitalTwinType> eventDigitalTwinTypes,
			DigitalTwinType digitalTwinType) {
		if (events != null && !events[0].equals("")) {
			for (final String event : events) {
				final EventsDigitalTwinType evt = new EventsDigitalTwinType();
				final JSONObject json = new JSONObject(event);
				evt.setName(json.getString("name"));
				evt.setDescription(json.getString(DESC_STR));
				evt.setStatus(json.getBoolean("status"));
				evt.setType(EventsDigitalTwinType.Type.valueOf(json.getString("type").toUpperCase()));
				evt.setTypeId(digitalTwinType);
				eventDigitalTwinTypes.add(evt);
			}
		}
	}

	@Override
	public void getDigitalTwinToUpdate(Model model, String id, String sessionUserId) {
		model.addAttribute("ontologies", ontologyService.getAllOntologies(sessionUserId));
		final DigitalTwinType digitalTwinType = digitalTwinTypeRepo.findById(id);
		if (digitalTwinType != null) {
			model.addAttribute("digitaltwintype", digitalTwinType);
			model.addAttribute(PROP_STR, getPropertiesByDigitalId(id));
			model.addAttribute("acciones", getActionsByDigitalId(id));
			model.addAttribute("eventos", getEventsByDigitalId(id));
			model.addAttribute("logica", getLogicByDigitalId(id));
		} else {
			log.error("DigitalTwinType with id:" + id + ", not found.");
		}
	}

	@Override
	public void updateDigitalTwinType(DigitalTwinType digitalTwinType, HttpServletRequest httpServletRequest) {

		// Update DigitalTwinType
		final DigitalTwinType digitalTwinTypeDb = digitalTwinTypeRepo.findById(digitalTwinType.getId());
		if (digitalTwinTypeDb != null) {
			digitalTwinTypeRepo.delete(digitalTwinTypeDb);
			createDigitalTwinType(digitalTwinType, httpServletRequest);
			createOntologyForShadow(digitalTwinType, httpServletRequest);
		} else {
			log.error("DigitalTwinType with identification:" + digitalTwinType.getName()
					+ "don't exist in data base to update.");
		}

	}

	@Override
	public void deleteDigitalTwinType(DigitalTwinType digitalTwinType) {
		digitalTwinTypeRepo.delete(digitalTwinType);
	}

	@Override
	public List<DigitalTwinType> getDigitalTwinTypesByUserId(String sessionUserId) {
		final User sessionUser = userService.getUser(sessionUserId);
		if (sessionUser.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return digitalTwinTypeRepo.findAll();
		} else {
			return digitalTwinTypeRepo.findByUser(sessionUser);
		}
	}

	@Override
	public void createOntologyForShadow(DigitalTwinType type, HttpServletRequest httpServletRequest) {
		final String[] properties = httpServletRequest.getParameterValues(PROP_STR);
		final JSONObject json = new JSONObject();

		try {
			json.put("$schema", "http://json-schema.org/draft-04/schema#");
			json.put("tittle",
					TWIN_PROP_STR + type.getName().substring(0, 1).toUpperCase() + type.getName().substring(1));
			json.put("type", "object");

			final JSONObject propertiesBis = new JSONObject();
			final JSONObject status = new JSONObject();
			final JSONArray requiredBis = new JSONArray();

			if (properties != null && !properties[0].equals("")) {
				for (final String prop : properties) {
					final JSONObject j = new JSONObject(prop);

					status.put(j.getString("name"), new JSONObject("{\"type\":\"" + j.getString("type") + "\"}"));
				}
			}
			status.put("type", "object");
			propertiesBis.put("status", status);
			propertiesBis.put("type", new JSONObject("{\"type\":\"string\"}"));
			propertiesBis.put("deviceId", new JSONObject("{\"type\":\"string\"}"));
			propertiesBis.put("timestamp", new JSONObject("{\"type\":\"string\",\"format\":\"date-time\"}"));

			requiredBis.put("type");
			requiredBis.put("deviceId");
			requiredBis.put("timestamp");

			json.put("properties", propertiesBis);
			json.put("required", requiredBis);
			json.put("additionalProperties", true);

			Ontology ontology = ontologyRepo.findByIdentification(
					TWIN_PROP_STR + type.getName().substring(0, 1).toUpperCase() + type.getName().substring(1));
			final OntologyConfiguration config = new OntologyConfiguration(httpServletRequest);

			if (ontology == null) {
				ontology = new Ontology();
				ontology.setActive(true);
				ontology.setDataModel(dataModelRepo.findByName("EmptyBase").get(0));
				ontology.setDescription("Shadow of the Digital Twin type");
				ontology.setIdentification(
						TWIN_PROP_STR + type.getName().substring(0, 1).toUpperCase() + type.getName().substring(1));
				ontology.setJsonSchema(json.toString());
				ontology.setPublic(false);
				ontology.setUser(type.getUser());
				ontology.setRtdbDatasource(RtdbDatasource.DIGITAL_TWIN);

				ontologyService.createOntology(ontology, config);
			} else {
				ontology.setJsonSchema(json.toString());

				ontologyService.updateOntology(ontology, type.getUser().getUserId(), config);
			}

		} catch (final JSONException e) {
			log.error("Error creating the ontology for the shadow od the Digital Twin Type " + type.getName(), e);
		}

	}

	@Override
	public boolean isIdValid(String identification) {

		final String regExp = "^[^\\d].*";
		return (identification.matches(regExp));
	}

	@Override
	public void populateCreateNewType(Model model, String sessionUserId) {

		model.addAttribute("digitaltwintype", new DigitalTwinType());
		model.addAttribute("ontologies", ontologyService.getAllOntologies(sessionUserId));
	}

}
