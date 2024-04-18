/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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
package com.minsait.onesait.platform.config.services.form;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.config.model.Form;
import com.minsait.onesait.platform.config.model.I18nResources;
import com.minsait.onesait.platform.config.model.Internationalization;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.repository.FormRepository;
import com.minsait.onesait.platform.config.repository.I18nResourcesRepository;
import com.minsait.onesait.platform.config.services.exceptions.FormServiceException;
import com.minsait.onesait.platform.config.services.internationalization.InternationalizationService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyFieldDTO;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.user.UserService;

@Service
public class FormServiceImpl implements FormService {

	@Autowired
	private FormRepository formRepository;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private OPResourceService resourceService;

	@Autowired
	private UserService userService;

	@Autowired
	I18nResourcesRepository i18nRR;
	@Autowired
	private InternationalizationService internationalizationService;

	@Override
	public boolean hasUserAccess(String userId, String formId, ResourceAccessType accessType) {
		final Form form = this.getDBForm(formId);
		final User u = userService.getUser(userId);
		if (form != null && u != null) {
			return form.getUser().getUserId().equals(userId)
					|| u.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name())
					|| resourceService.hasAccess(userId, form.getId(), accessType);
		}
		return false;
	}

	@Override
	public void create(FormCreateDTO form, String userId) {

		if (formRepository.findByCode(this.createCode(form.getName(), userId)).isPresent()) {
			throw new FormServiceException("A form with that name already exists");
		}
		final User u = new User();
		u.setUserId(userId);
		final Form f = new Form();
		f.setUser(u);
		f.setIdentification(form.getName());
		f.setCode(this.createCode(form.getName(), userId));
		f.setDescription(form.getDescription());
		f.setJsonSchema(form.getJsonSchema());
		f.setConfig(form.getConfig());

		if (StringUtils.hasText(form.getEntity())) {
			Ontology o = ontologyService.getOntologyByIdentification(form.getEntity(), userId);
			if (o == null) {
				o = ontologyService.getOntologyById(form.getEntity(), userId);
			}
			f.setOntology(o);
		}
		formRepository.save(f);
		createModifyI18nResource(f.getId(), form.getI18n(), userId);
	}

	@Override
	public FormDTO getForm(String code) {
		Optional<Form> f = formRepository.findByCode(code);
		if (f.isPresent()) {
			final List<I18nResources> i18n = i18nRR.findByOPResourceId(f.get().getId());
			return FormDTO.builder().id(f.get().getId()).userId(f.get().getUserJson()).name(f.get().getIdentification())
					.jsonSchema(f.get().getJsonSchema())
					.entity(f.get().getOntology() != null ? f.get().getOntology().getIdentification() : null)
					.dateCreated(f.get().getCreatedAt()).dateUpdated(f.get().getUpdatedAt()).code(f.get().getCode())
					.description(f.get().getDescription())
					.i18n(i18n != null && i18n.size() > 0 ? i18n.get(0).getI18n().getId() : null)
					.i18nJson(i18n != null && i18n.size() > 0
							? formatInternationalization(i18n.get(0).getI18n().getJsoni18n())
							: null)
					.config(f.get().getConfig()).build();
		}
		return null;
	}

	@Override
	public FormDTO getFormById(String id) {
		Optional<Form> f = formRepository.findById(id);
		if (f.isPresent()) {
			final List<I18nResources> i18n = i18nRR.findByOPResourceId(f.get().getId());
			return FormDTO.builder().id(f.get().getId()).userId(f.get().getUserJson()).name(f.get().getIdentification())
					.jsonSchema(f.get().getJsonSchema())
					.entity(f.get().getOntology() != null ? f.get().getOntology().getIdentification() : null)
					.dateCreated(f.get().getCreatedAt()).dateUpdated(f.get().getUpdatedAt()).code(f.get().getCode())
					.description(f.get().getDescription())
					.i18n(i18n != null && i18n.size() > 0 ? i18n.get(0).getI18n().getId() : null)
					.i18nJson(i18n != null && i18n.size() > 0
							? formatInternationalization(i18n.get(0).getI18n().getJsoni18n())
							: null)
					.config(f.get().getConfig()).build();
		}
		return null;
	}

	private String formatInternationalization(String i18m) {
		if (i18m == null) {
			return null;
		}
		final JSONObject jInternatilization = new JSONObject(i18m);
		final JSONObject result = new JSONObject();
		if (jInternatilization.has("default") && !jInternatilization.isNull("default")) {
			result.put("language", jInternatilization.get("default"));
		} else {
			Iterator<String> keys = jInternatilization.getJSONObject("languages").keys();
			if (keys.hasNext()) {
				String key = keys.next();
				result.put("language", key);
			}
		}
		if (jInternatilization.has("languages")) {
			result.put("i18n", jInternatilization.getJSONObject("languages"));
		}
		return result.toString();
	}

	@Override
	public String generateFormFromEntity(String codeTemplate, String entity, String userId) throws IOException {

		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode form = mapper.createObjectNode();
		final ArrayNode components = mapper.createArrayNode();
		((ObjectNode) form).put("display", "form");

		if (entity == null) {
			if (codeTemplate.equals("empty")) {

			}
		} else {
			// entity validation
			Ontology o = ontologyService.getOntologyByIdentification(entity, userId);
			if (o == null) {
				o = ontologyService.getOntologyById(entity, userId);
			}
			if (o == null) {
				return null;
			}
			// entity get fields
			final Map<String, OntologyFieldDTO> pMap = ontologyService.getOntologyFieldsAndDescForms(entity, userId);

			if (codeTemplate.equals("unorderedFields")) {
				for (final OntologyFieldDTO ofDTO : pMap.values()) {
					createComponent(mapper, ofDTO, components);

				}
				// add submit button to form
				createSubmitButton(mapper, components);
				((ObjectNode) form).putArray("components").addAll(components);
			}
		}
		return form.toString();
	}

	private void createComponent(ObjectMapper mapper, OntologyFieldDTO ofDTO, ArrayNode components) {

		if (ofDTO.getType().equals("string")) {
			if (ofDTO.getFormat() != null && ofDTO.getFormat().equals("date")) {
				final JsonNode component = mapper.createObjectNode();
				((ObjectNode) component).put("label", ofDTO.getPath());
				((ObjectNode) component).put("format", "yyyy-MM-dd");
				((ObjectNode) component).putObject("datePicker");
				((ObjectNode) component.get("datePicker")).put("disableWeekends", false);
				((ObjectNode) component.get("datePicker")).put("disableWeekdays", false);
				((ObjectNode) component).put("tableView", true);
				((ObjectNode) component).put("enableTime", false);
				((ObjectNode) component).put("calculateValue",
						"if(value!=null && value.trim().length>''){\n  value = moment(value).format(\"YYYY-MM-DD\")\n}");
				((ObjectNode) component).put("enableMinDateInput", false);
				((ObjectNode) component).put("enableMaxDateInput", false);
				((ObjectNode) component).put("key", ofDTO.getPath());
				((ObjectNode) component).put("type", "datetime");
				((ObjectNode) component).put("input", true);
				((ObjectNode) component).putObject("widget");
				((ObjectNode) component.get("widget")).put("type", "calendar");
				((ObjectNode) component.get("widget")).put("displayInTimezone", "viewer");
				((ObjectNode) component.get("widget")).put("locale", "en");
				((ObjectNode) component.get("widget")).put("useLocaleSettings", false);
				((ObjectNode) component.get("widget")).put("allowInput", true);
				((ObjectNode) component.get("widget")).put("mode", "single");
				((ObjectNode) component.get("widget")).put("enableTime", false);
				((ObjectNode) component.get("widget")).put("noCalendar", false);
				((ObjectNode) component.get("widget")).put("format", "yyyy-MM-dd");
				((ObjectNode) component.get("widget")).put("hourIncrement", 1);
				((ObjectNode) component.get("widget")).put("minuteIncrement", 1);
				((ObjectNode) component.get("widget")).put("time_24hr", false);
				((ObjectNode) component.get("widget")).putNull("minDate");
				((ObjectNode) component.get("widget")).put("disableWeekends", false);
				((ObjectNode) component.get("widget")).put("disableWeekdays", false);
				((ObjectNode) component.get("widget")).putNull("maxDate");

				if (ofDTO.getDescription() != null && ofDTO.getDescription().length() > 0) {
					((ObjectNode) component).put("tooltip", ofDTO.getDescription());
				}
				components.add(component);
			} else if (ofDTO.getFormat() != null && ofDTO.getFormat().equals("datetime")) {
				final JsonNode component = mapper.createObjectNode();
				((ObjectNode) component).put("label", ofDTO.getPath());
				((ObjectNode) component).put("format", "yyyy-MM-dd hh:mm:ss");
				((ObjectNode) component).putObject("datePicker");
				((ObjectNode) component.get("datePicker")).put("disableWeekends", false);
				((ObjectNode) component.get("datePicker")).put("disableWeekdays", false);
				((ObjectNode) component).putObject("timePicker");
				((ObjectNode) component.get("timePicker")).put("showMeridian", false);
				((ObjectNode) component).put("tableView", true);
				((ObjectNode) component).put("enableTime", true);
				((ObjectNode) component).put("calculateValue",
						"if(value!=null && value.trim().length>''){\n  value = moment(value).format(\"YYYY-MM-DD HH:mm:ss\")\n}");
				((ObjectNode) component).put("enableMinDateInput", false);
				((ObjectNode) component).put("enableMaxDateInput", false);
				((ObjectNode) component).put("key", ofDTO.getPath());
				((ObjectNode) component).put("type", "datetime");
				((ObjectNode) component).put("input", true);
				((ObjectNode) component).putObject("widget");
				((ObjectNode) component.get("widget")).put("type", "calendar");
				((ObjectNode) component.get("widget")).put("displayInTimezone", "viewer");
				((ObjectNode) component.get("widget")).put("locale", "en");
				((ObjectNode) component.get("widget")).put("useLocaleSettings", false);
				((ObjectNode) component.get("widget")).put("allowInput", true);
				((ObjectNode) component.get("widget")).put("mode", "single");
				((ObjectNode) component.get("widget")).put("enableTime", true);
				((ObjectNode) component.get("widget")).put("noCalendar", false);
				((ObjectNode) component.get("widget")).put("format", "yyyy-MM-dd hh:mm:ss");
				((ObjectNode) component.get("widget")).put("hourIncrement", 1);
				((ObjectNode) component.get("widget")).put("minuteIncrement", 1);
				((ObjectNode) component.get("widget")).put("time_24hr", true);
				((ObjectNode) component.get("widget")).putNull("minDate");
				((ObjectNode) component.get("widget")).put("disableWeekends", false);
				((ObjectNode) component.get("widget")).put("disableWeekdays", false);
				((ObjectNode) component.get("widget")).putNull("maxDate");

				if (ofDTO.getDescription() != null && ofDTO.getDescription().length() > 0) {
					((ObjectNode) component).put("tooltip", ofDTO.getDescription());
				}
				components.add(component);

			} else {
				final JsonNode component = mapper.createObjectNode();
				((ObjectNode) component).put("label", ofDTO.getPath());
				((ObjectNode) component).put("tableView", true);
				((ObjectNode) component).put("key", ofDTO.getPath());
				((ObjectNode) component).put("type", "textfield");
				((ObjectNode) component).put("input", true);
				if (ofDTO.getDescription() != null && ofDTO.getDescription().length() > 0) {
					((ObjectNode) component).put("tooltip", ofDTO.getDescription());
				}
				components.add(component);
			}
		} else if (ofDTO.getType().equals("number")) {
			final JsonNode component = mapper.createObjectNode();
			((ObjectNode) component).put("label", ofDTO.getPath());
			((ObjectNode) component).put("mask", false);
			((ObjectNode) component).put("tableView", true);
			((ObjectNode) component).put("delimiter", false);
			((ObjectNode) component).put("requireDecimal", false);
			((ObjectNode) component).put("inputFormat", "plain");
			((ObjectNode) component).put("truncateMultipleSpaces", false);
			((ObjectNode) component).put("key", ofDTO.getPath());
			((ObjectNode) component).put("type", "number");
			((ObjectNode) component).put("input", true);
			if (ofDTO.getDescription() != null && ofDTO.getDescription().length() > 0) {
				((ObjectNode) component).put("tooltip", ofDTO.getDescription());
			}
			components.add(component);
		} else if (ofDTO.getType().equals("integer")) {
			final JsonNode component = mapper.createObjectNode();
			((ObjectNode) component).put("label", ofDTO.getPath());
			((ObjectNode) component).put("mask", false);
			((ObjectNode) component).put("tableView", true);
			((ObjectNode) component).put("delimiter", false);
			((ObjectNode) component).put("requireDecimal", false);
			((ObjectNode) component).put("inputFormat", "plain");
			((ObjectNode) component).put("truncateMultipleSpaces", false);
			((ObjectNode) component).put("key", ofDTO.getPath());
			((ObjectNode) component).put("decimalLimit", 0);
			((ObjectNode) component).put("type", "number");
			((ObjectNode) component).put("input", true);
			if (ofDTO.getDescription() != null && ofDTO.getDescription().length() > 0) {
				((ObjectNode) component).put("tooltip", ofDTO.getDescription());
			}
			components.add(component);
		} else if (ofDTO.getType().equals("boolean")) {
			final JsonNode component = mapper.createObjectNode();
			((ObjectNode) component).put("label", ofDTO.getPath());
			((ObjectNode) component).put("tableView", true);
			((ObjectNode) component).put("key", ofDTO.getPath());
			((ObjectNode) component).put("type", "checkbox");
			((ObjectNode) component).put("input", true);
			((ObjectNode) component).put("defaultValue", false);
			if (ofDTO.getDescription() != null && ofDTO.getDescription().length() > 0) {
				((ObjectNode) component).put("tooltip", ofDTO.getDescription());
			}
			components.add(component);
		} else if (ofDTO.getType().equals("date")) {
			final JsonNode component = mapper.createObjectNode();
			((ObjectNode) component).put("label", ofDTO.getPath());
			((ObjectNode) component).put("format", "Y-m-d");
			((ObjectNode) component).putObject("datePicker");
			((ObjectNode) component.get("datePicker")).put("disableWeekends", false);
			((ObjectNode) component.get("datePicker")).put("disableWeekdays", false);
			((ObjectNode) component).put("tableView", true);
			((ObjectNode) component).put("enableTime", false);
			((ObjectNode) component).put("calculateValue",
					"if(value!=null && value.trim().length>0){value = moment(value).format('YYYY-MM-DD')}");
			((ObjectNode) component).put("enableMinDateInput", false);
			((ObjectNode) component).put("enableMaxDateInput", false);
			((ObjectNode) component).put("key", ofDTO.getPath());
			((ObjectNode) component).put("type", "datetime");
			((ObjectNode) component).put("input", true);
			((ObjectNode) component).putObject("widget");
			((ObjectNode) component.get("widget")).put("type", "calendar");
			((ObjectNode) component.get("widget")).put("displayInTimezone", "viewer");
			((ObjectNode) component.get("widget")).put("locale", "en");
			((ObjectNode) component.get("widget")).put("useLocaleSettings", false);
			((ObjectNode) component.get("widget")).put("allowInput", true);
			((ObjectNode) component.get("widget")).put("mode", "single");
			((ObjectNode) component.get("widget")).put("enableTime", false);
			((ObjectNode) component.get("widget")).put("noCalendar", false);
			((ObjectNode) component.get("widget")).put("format", "yyyy-MM-dd");
			((ObjectNode) component.get("widget")).put("hourIncrement", 1);
			((ObjectNode) component.get("widget")).put("minuteIncrement", 1);
			((ObjectNode) component.get("widget")).put("time_24hr", false);
			((ObjectNode) component.get("widget")).putNull("minDate");
			((ObjectNode) component.get("widget")).put("disableWeekends", false);
			((ObjectNode) component.get("widget")).put("disableWeekdays", false);
			((ObjectNode) component.get("widget")).putNull("maxDate");

			if (ofDTO.getDescription() != null && ofDTO.getDescription().length() > 0) {
				((ObjectNode) component).put("tooltip", ofDTO.getDescription());
			}
			components.add(component);
		} else if (ofDTO.getType().equals("date-time")) {
			final JsonNode component = mapper.createObjectNode();
			((ObjectNode) component).put("label", ofDTO.getPath());
			((ObjectNode) component).put("format", "yyyy-MM-ddThh:mm:ss");
			((ObjectNode) component).putObject("datePicker");
			((ObjectNode) component.get("datePicker")).put("disableWeekends", false);
			((ObjectNode) component.get("datePicker")).put("disableWeekdays", false);
			((ObjectNode) component).putObject("timePicker");
			((ObjectNode) component.get("timePicker")).put("showMeridian", false);
			((ObjectNode) component).put("tableView", true);
			((ObjectNode) component).put("enableTime", true);
			((ObjectNode) component).put("calculateValue",
					"if(value!=null && value.trim().length>0){ value = moment(value).format('YYYY-MM-DDTHH:mm:ss\\Z')}");
			((ObjectNode) component).put("enableMinDateInput", false);
			((ObjectNode) component).put("enableMaxDateInput", false);
			((ObjectNode) component).put("key", ofDTO.getPath());
			((ObjectNode) component).put("type", "datetime");
			((ObjectNode) component).put("input", true);
			((ObjectNode) component).putObject("widget");
			((ObjectNode) component.get("widget")).put("type", "calendar");
			((ObjectNode) component.get("widget")).put("displayInTimezone", "viewer");
			((ObjectNode) component.get("widget")).put("locale", "en");
			((ObjectNode) component.get("widget")).put("useLocaleSettings", false);
			((ObjectNode) component.get("widget")).put("allowInput", true);
			((ObjectNode) component.get("widget")).put("mode", "single");
			((ObjectNode) component.get("widget")).put("enableTime", true);
			((ObjectNode) component.get("widget")).put("noCalendar", false);
			((ObjectNode) component.get("widget")).put("format", "yyyy-MM-dd hh:mm:ss\\Z");
			((ObjectNode) component.get("widget")).put("hourIncrement", 1);
			((ObjectNode) component.get("widget")).put("minuteIncrement", 1);
			((ObjectNode) component.get("widget")).put("time_24hr", true);
			((ObjectNode) component.get("widget")).putNull("minDate");
			((ObjectNode) component.get("widget")).put("disableWeekends", false);
			((ObjectNode) component.get("widget")).put("disableWeekdays", false);
			((ObjectNode) component.get("widget")).putNull("maxDate");

			if (ofDTO.getDescription() != null && ofDTO.getDescription().length() > 0) {
				((ObjectNode) component).put("tooltip", ofDTO.getDescription());
			}
			components.add(component);

		} else if (ofDTO.getType().equals("email")) {
			final JsonNode component = mapper.createObjectNode();
			((ObjectNode) component).put("label", ofDTO.getPath());
			((ObjectNode) component).put("tableView", true);
			((ObjectNode) component).put("key", ofDTO.getPath());
			((ObjectNode) component).put("type", "email");
			((ObjectNode) component).put("input", true);
			if (ofDTO.getDescription() != null && ofDTO.getDescription().length() > 0) {
				((ObjectNode) component).put("tooltip", ofDTO.getDescription());
			}
			components.add(component);

		}

	}

	private void createSubmitButton(ObjectMapper mapper, ArrayNode components) {
		final JsonNode component = mapper.createObjectNode();
		((ObjectNode) component).put("label", "Submit");
		((ObjectNode) component).put("showValidations", false);
		((ObjectNode) component).put("tableView", true);
		((ObjectNode) component).put("key", "submit");
		((ObjectNode) component).put("type", "button");
		((ObjectNode) component).put("input", true);
		components.add(component);
	}

	@Override
	public Form getDBForm(String code) {
		Form form = formRepository.findByCode(code).orElse(null);
		if (form == null) {
			form = formRepository.findById(code).orElse(null);
		}
		return form;
	}

	@Override
	public List<FormDTO> getForms(String userId) {
		final User u = userService.getUser(userId);
		if (u != null) {
			if (u.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name())) {
				return formRepository.findAll().stream()
						.map(f -> FormDTO.builder().id(f.getId()).userId(f.getUserJson()).name(f.getIdentification())
								.jsonSchema(f.getJsonSchema())
								.entity(f.getOntology() != null ? f.getOntology().getIdentification() : null)
								.dateCreated(f.getCreatedAt()).dateUpdated(f.getUpdatedAt()).code(f.getCode())
								.config(f.getConfig()).description(f.getDescription()).build())
						.toList();

			} else {
				return formRepository.findByUser(u).stream()
						.map(f -> FormDTO.builder().id(f.getId()).userId(f.getUserJson()).name(f.getIdentification())
								.jsonSchema(f.getJsonSchema())
								.entity(f.getOntology() != null ? f.getOntology().getIdentification() : null)
								.dateCreated(f.getCreatedAt()).dateUpdated(f.getUpdatedAt()).code(f.getCode())
								.config(f.getConfig()).description(f.getDescription()).build())
						.toList();
			}
		}
		return null;
	}

	@Override
	public void deleteForm(String code, String userId) {
		final User u = userService.getUser(userId);
		final Form f = formRepository.findByCode(code).orElse(null);
		if (u != null && f != null && u.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name())
				|| f.getUser().getUserId().equals(userId)) {
			i18nRR.deleteAll(i18nRR.findByOPResourceId(f.getId()));
			formRepository.deleteById(f.getId());
		}
	}

	@Override
	public FormDTO updateForm(FormCreateDTO form, String code, String userId) {
		final User u = userService.getUser(userId);
		Form f = formRepository.findByCode(code).orElse(null);
		if (u != null && f != null && 
			(u.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name()) || resourceService.hasAccess(userId, f.getId(), ResourceAccessType.MANAGE)	|| f.getUser().getUserId().equals(userId))) {
			f.setJsonSchema(form.getJsonSchema());
			f.setConfig(form.getConfig());

			f.setDescription(form.getDescription());
			if (StringUtils.hasText(form.getEntity())) {
				Ontology o = ontologyService.getOntologyByIdentification(form.getEntity(), userId);
				if (o == null) {
					o = ontologyService.getOntologyById(form.getEntity(), userId);
				}
				f.setOntology(o);
			} else {
				f.setOntology(null);
			}
			f = formRepository.save(f);

			createModifyI18nResource(f.getId(), form.getI18n(), userId);
			return FormDTO.builder().jsonSchema(f.getJsonSchema()).id(f.getId()).dateCreated(f.getCreatedAt())
					.dateUpdated(f.getUpdatedAt()).name(f.getIdentification()).userId(f.getUserJson())
					.entity(f.getOntology() != null ? f.getOntology().getIdentification() : null).build();
		} else {
			throw new FormServiceException(FormServiceException.ErrorType.NOT_FOUND,
					"Cannot update form that does not exist");
		}

	}

	@Override
	public void createModifyI18nResource(String idForm, String i18n, String userId) {

		i18nRR.deleteAll(i18nRR.findByOPResourceId(idForm));
		if (i18n != null && i18n.length() > 0) {
			final Internationalization inter = internationalizationService.getInternationalizationById(i18n, userId);
			final OPResource opr = formRepository.findById(idForm).orElse(null);
			if (inter != null && opr != null) {
				final I18nResources i18nR = new I18nResources();
				i18nR.setI18n(inter);
				i18nR.setOpResource(opr);
				i18nRR.save(i18nR);
			}
		}

	}

	// Create code from name without spaces and userId like code-userid
	@Override
	public String createCode(String name, String userId) {
		return name.replaceAll("\\s+", "") + "-" + userId;
	}

	@Override
	public void clone(String code, String newName, String userId) {
		final Form f = formRepository.findByCode(code).orElse(null);
		final FormCreateDTO formCreateDTO = new FormCreateDTO();
		formCreateDTO.setDescription(f.getDescription());
		if (f.getOntology() != null) {
			formCreateDTO.setEntity(f.getOntology().getIdentification());
		}
		final List<I18nResources> listi18 = i18nRR.findByOPResourceId(f.getId());
		if (listi18 != null && listi18.size() > 0) {
			formCreateDTO.setI18n(listi18.get(0).getI18n().getId());
		}
		formCreateDTO.setJsonSchema(f.getJsonSchema());
		formCreateDTO.setConfig(f.getConfig());
		formCreateDTO.setName(newName);
		this.create(formCreateDTO, userId);

	}

	@Override
	public void cloneById(String id, String newName, String userId) {
		final Form f = formRepository.findById(id).orElse(null);
		final FormCreateDTO formCreateDTO = new FormCreateDTO();
		formCreateDTO.setDescription(f.getDescription());
		if (f.getOntology() != null) {
			formCreateDTO.setEntity(f.getOntology().getIdentification());
		}
		final List<I18nResources> listi18 = i18nRR.findByOPResourceId(f.getId());
		if (listi18 != null && listi18.size() > 0) {
			formCreateDTO.setI18n(listi18.get(0).getI18n().getId());
		}
		formCreateDTO.setJsonSchema(f.getJsonSchema());
		formCreateDTO.setConfig(f.getConfig());
		formCreateDTO.setName(newName);
		this.create(formCreateDTO, userId);
	}

}
