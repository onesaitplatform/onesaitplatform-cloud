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
package com.minsait.onesait.platform.config.services.internationalization;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.I18nResources;
import com.minsait.onesait.platform.config.model.Internationalization;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.I18nResourcesRepository;
import com.minsait.onesait.platform.config.repository.InternationalizationRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.exceptions.InternationalizationServiceException;
import com.minsait.onesait.platform.config.services.exceptions.OPResourceServiceException;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.user.UserService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class InternationalizationServiceImpl implements InternationalizationService {

	@Autowired
	private InternationalizationRepository internationalizationRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private I18nResourcesRepository i18nRRepository;
	@Autowired
	@Lazy
	private OPResourceService resourceService;
	@Autowired
	private UserService userService;

	@Value("${onesaitplatform.controlpanel.url:http://localhost:18000/controlpanel}")
	private String basePath;

	protected ObjectMapper objectMapper;

	@Value("${onesaitplatform.internationalizationengine.url.view:http://localhost:8087/controlpanel/internationalizations/viewiframe/}")
	private String prefixURLView;

	@Value("${onesaitplatform.internationalization.export.url:http://internationalizationexport:26000}")
	private String internationalizationexporturl;

	@PostConstruct
	public void init() {
		objectMapper = new ObjectMapper();
		objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
	}

	private static final String ANONYMOUSUSER = "anonymousUser";
	private static final String JSON18N_NOT_EXIST = "Internationalization does not exist in the database";

	@Override
	public List<Internationalization> findInternationalizationWithIdentificationAndDescription(String identification,
			String description, String userId) {
		List<Internationalization> internationalizations;

		description = description == null ? "" : description;
		identification = identification == null ? "" : identification;
		
		final User user = userRepository.findByUserId(userId);
		
		if (userService.isUserAdministrator(user)) {
			internationalizations = internationalizationRepository
					.findByIdentificationContainingAndDescriptionContaining(identification, description);
		} else {
			internationalizations = internationalizationRepository
					.findByUserAndIdentificationContainingAndDescriptionContainingOrderByIdentificationAsc(user, identification, description);
		}
		

		return internationalizations.stream().map(temp -> {
			final Internationalization obj = new Internationalization();
			obj.setCreatedAt(temp.getCreatedAt());
			obj.setDescription(temp.getDescription());
			obj.setId(temp.getId());
			obj.setJsoni18n(temp.getJsoni18n());
			obj.setIdentification(temp.getIdentification());
			obj.setPublic(temp.isPublic());
			obj.setUpdatedAt(temp.getUpdatedAt());
			obj.setUser(temp.getUser());
			return obj;
		}).collect(Collectors.toList());

	}

	@Override
	public List<String> getAllIdentifications() {
		final List<Internationalization> internationalizations = internationalizationRepository
				.findAllByOrderByIdentificationAsc();
		final List<String> identifications = new ArrayList<>();
		for (final Internationalization internationalization : internationalizations) {
			identifications.add(internationalization.getIdentification());

		}
		return identifications;
	}

	@Override
	public List<Internationalization> getAllInternationalizations() {
		return internationalizationRepository.findAll();
	}

	@Transactional
	@Override
	public void deleteInternationalization(String internationalizationId, String userId) {
		final Internationalization internationalization = internationalizationRepository
				.findById(internationalizationId).orElse(null);
		if (internationalization != null) {
			if (resourceService.isResourceSharedInAnyProject(internationalization))
				throw new OPResourceServiceException(
						"This Internationalization is shared within a Project, revoke access from project prior to deleting");
			final I18nResources i18nR = i18nRRepository.findById(internationalization.getId()).orElse(null);
			if (i18nR != null) {
				i18nRRepository.delete(i18nR);
			}
			internationalizationRepository.delete(internationalization);
		} else {
			throw new InternationalizationServiceException("Cannot delete internationalization that does not exist");
		}

	}

	@Override
	public void deleteInternationalizationByIdentification(String internationalizationId, String userId) {
		final Internationalization internationalization = internationalizationRepository
				.findInternationalizationByIdentification(internationalizationId);
		if (internationalization != null) {
			if (resourceService.isResourceSharedInAnyProject(internationalization))
				throw new OPResourceServiceException(
						"This Internationalization is shared within a Project, revoke access from project prior to deleting");
			final I18nResources i18nR = i18nRRepository.findById(internationalization.getId()).orElse(null);
			if (i18nR != null) {
				i18nRRepository.delete(i18nR);
			}
			internationalizationRepository.delete(internationalization);
		} else {
			throw new InternationalizationServiceException("Cannot delete internationalization that does not exist");
		}

	}

	@Override
	public boolean hasUserPermission(String identification, String userId) {
		final User user = userRepository.findByUserId(userId);
		if (userService.isUserAdministrator(user)) {
			return true;
		} else {
			return internationalizationRepository.findInternationalizationByIdentification(identification).getUser()
					.getUserId().equals(userId);
		}
	}

	@Override
	public void saveInternationalization(String id, Internationalization internationalization, String userId) {

		internationalizationRepository.findById(internationalization.getId()).ifPresent(internationalizationEnt -> {
			internationalizationEnt.setIdentification(internationalization.getIdentification());
			internationalizationEnt.setDescription(internationalization.getDescription());
			internationalizationEnt.setJsoni18n(internationalization.getJsoni18n());
			internationalizationEnt.setPublic(internationalization.isPublic());

			internationalizationRepository.save(internationalizationEnt);
		});

	}

	@Override
	public Internationalization getInternationalizationById(String id, String userId) {
		return internationalizationRepository.findById(id).orElse(null);
	}

	@Override
	public Internationalization getInternationalizationByIdentification(String identification, String userId) {
		if (!internationalizationRepository.findByIdentification(identification).isEmpty())
			return internationalizationRepository.findByIdentification(identification).get(0);
		else
			return null;
	}

	private Internationalization getNewInternationalization(Internationalization internationalization, String userId,
			boolean restapi) {
		log.debug("Internationalization no exist, creating...");
		final Internationalization d = new Internationalization();

		d.setDescription(internationalization.getDescription());
		d.setIdentification(internationalization.getIdentification());
		d.setPublic(internationalization.isPublic());
		d.setUser(userRepository.findByUserId(userId));

		// Controlar formato del json antes de guardarlo
		final String jsoni18n = internationalization.getJsoni18n();
		if (restapi) {
			try {
				final JSONObject obj = new JSONObject(jsoni18n);
				if (obj.getJSONObject("languages") != null && obj.getString("default") != null) {
					d.setJsoni18n(jsoni18n);
				}
			} catch (final JSONException e) {
				throw new InternationalizationServiceException(e.getMessage()
						+ "\n Json must be like this:  \"jsoni18n\":\"{\\\"languages\\\":{\\\"ES\\\":{\\\"Hi\\\":\\\"Hola\\\"}, \\\"EN\\\":{\\\"Hi\\\":\\\"Hello\\\"}}, \\\"default\\\":\\\"ES\\\"}\"");
			}
		} else {
			d.setJsoni18n(jsoni18n);
		}

		return internationalizationRepository.save(d);
	}

	@Override
	public boolean internationalizationExists(String identification) {
		return !CollectionUtils.isEmpty(internationalizationRepository.findByIdentification(identification));
	}

	@Override
	public String createNewInternationalization(Internationalization internationalization, String userId,
			boolean restapi) {
		if (internationalizationExists(internationalization.getIdentification()))
			throw new InternationalizationServiceException("Internationalization already exists in Database");

		final Internationalization dAux = getNewInternationalization(internationalization, userId, restapi);

		return dAux.getId();

	}

	@Transactional
	@Override
	public String updatePublicInternationalization(Internationalization internationalization, String userId) {
		final Optional<Internationalization> opt = internationalizationRepository
				.findById(internationalization.getId());
		if (!opt.isPresent()) {
			throw new InternationalizationServiceException(JSON18N_NOT_EXIST);
		} else {
			final Internationalization d = opt.get();
			d.setDescription(internationalization.getDescription());
			d.setIdentification(internationalization.getIdentification());
			d.setJsoni18n(internationalization.getJsoni18n());
			d.setPublic(internationalization.isPublic());

			return d.getId();
		}
	}

	@Override
	public List<Internationalization> getByUserIdOrPublic(String userId) {
		final User sessionUser = userRepository.findByUserId(userId);
		if (userService.isUserAdministrator(sessionUser)) {
			return internationalizationRepository.findAllByOrderByIdentificationAsc();
		} else {
			return internationalizationRepository.findByUserOrIsPublic(sessionUser);
		}
	}
	
	@Override
	public List<Internationalization> getByUserId(String userId) {
		final User sessionUser = userRepository.findByUserId(userId);
		if (userService.isUserAdministrator(sessionUser)) {
			return internationalizationRepository.findAllByOrderByIdentificationAsc();
		} else {
			return internationalizationRepository.findByUserOrderByIdentificationAsc(sessionUser);
		}
	}

	@Override
	public Internationalization getInternationalizationEditById(String id, String userId) {
		if (hasUserEditPermission(id, userId)) {
			return internationalizationRepository.findById(id).orElse(null);
		}
		throw new InternationalizationServiceException(
				"Cannot view Internationalization that does not exist or don't have permission");
	}

	@Override
	public boolean hasUserEditPermission(String id, String userId) {
		final User user = userRepository.findByUserId(userId);
		if (userService.isUserAdministrator(user)) {
			return true;
		} else {
			final Optional<Internationalization> opt = internationalizationRepository.findById(id);
			if (opt.isPresent()) {
				final boolean propietary = opt.get().getUser().getUserId().equals(userId);
				if (propietary) {
					return true;
				}
				return resourceService.hasAccess(userId, id, ResourceAccessType.MANAGE);
			} else {
				return false;
			}
		}
	}

	@Override
	public boolean hasUserViewPermission(String id, String userId) {
		final User user = userRepository.findByUserId(userId);
		final Optional<Internationalization> opt = internationalizationRepository.findById(id);
		if (!opt.isPresent())
			return false;
		if (opt.get().isPublic()) {
			return true;
		} else if (userId.equals(ANONYMOUSUSER) || user == null) {
			return opt.get().isPublic();
		} else if (userService.isUserAdministrator(user)) {
			return true;
		} else {
			final boolean propietary = opt.get().getUser().getUserId().equals(userId);
			if (propietary) {
				return true;
			}

			return resourceService.hasAccess(userId, id, ResourceAccessType.VIEW);
		}
	}

	@Override
	public String getCredentialsString(String userId) {
		final User user = userRepository.findByUserId(userId);
		return user.getUserId();
	}

	@Override
	public List<Internationalization> getInternationalizationsByResourceId(String resourceId) {
		return internationalizationRepository.findByOPResourceId(resourceId);
	}
}
