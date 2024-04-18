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
package com.minsait.onesait.platform.config.services.opendata.typology;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.ODTypology;
import com.minsait.onesait.platform.config.model.ODTypologyDataset;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.ODTypologyRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.exceptions.ODTypologyServiceException;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.user.UserService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TypologyServiceImpl implements TypologyService {

	@Autowired
	private ODTypologyRepository typologyRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private OPResourceService resourceService;

	@Value("${onesaitplatform.controlpanel.url:http://localhost:18000/controlpanel}")
	private String basePath;

	protected ObjectMapper objectMapper;

	@PostConstruct
	public void init() {
		objectMapper = new ObjectMapper();
		objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
	}

	private static final String TYPOLOGY_NOT_EXIST = "Typology does not exist in the database";

	@Override
	public List<ODTypology> findTypologyWithIdentificationAndDescription(String identification, String description,
			String userId) {
		List<ODTypology> typologies;

		description = description == null ? "" : description;
		identification = identification == null ? "" : identification;

		typologies = typologyRepository.findByIdentificationContainingAndDescriptionContaining(identification,
				description);

		return typologies.stream().map(temp -> {
			final ODTypology obj = new ODTypology();
			obj.setCreatedAt(temp.getCreatedAt());
			obj.setDescription(temp.getDescription());
			obj.setId(temp.getId());
			obj.setIdentification(temp.getIdentification());
			obj.setUpdatedAt(temp.getUpdatedAt());
			obj.setUser(temp.getUser());
			return obj;
		}).collect(Collectors.toList());

	}

	@Override
	public List<String> getAllIdentifications() {
		final List<ODTypology> typologies = typologyRepository.findAllByOrderByIdentificationAsc();
		final List<String> identifications = new ArrayList<>();
		for (final ODTypology typology : typologies) {
			identifications.add(typology.getIdentification());

		}
		return identifications;
	}

	@Override
	public List<ODTypology> getAllTypologies() {
		return typologyRepository.findAll();
	}

	@Transactional
	@Override
	public void deleteTypology(String typologyId, String userId) {
		final ODTypology typology = typologyRepository.findTypologyById(typologyId);
		if (typology != null) {
			typologyRepository.delete(typology);
		} else {
			throw new ODTypologyServiceException("Cannot delete typology that does not exist");
		}

	}

	@Override
	public void deleteTypologyByIdentification(String typologyId, String userId) {
		final ODTypology typology = typologyRepository.findTypologyByIdentification(typologyId);
		if (typology != null) {
			typologyRepository.delete(typology);
		} else {
			throw new ODTypologyServiceException("Cannot delete typology that does not exist");
		}

	}

	@Override
	public boolean hasUserPermission(String identification, String userId) {
		final User user = userService.getUserByIdentification(userId);
		if (user.isAdmin()) {
			return true;
		} else {
			return typologyRepository.findTypologyByIdentification(identification).getUser().getUserId().equals(userId);
		}
	}

	@Override
	public void saveTypology(String id, ODTypology typology, String userId) {

		final ODTypology typologyEnt = typologyRepository.findTypologyById(typology.getId());
		typologyEnt.setIdentification(typology.getIdentification());
		typologyEnt.setDescription(typology.getDescription());

		typologyRepository.save(typologyEnt);

	}

	@Override
	public ODTypology getTypologyById(String id) {
		return typologyRepository.findTypologyById(id);
	}

	@Override
	public ODTypology getTypologyByIdentification(String identification) {
		if (!typologyRepository.findByIdentification(identification).isEmpty())
			return typologyRepository.findByIdentification(identification).get(0);
		else
			return null;
	}

	private ODTypology getNewTypology(ODTypology typology, String userId) {
		log.debug("Typology no exist, creating...");
		final ODTypology d = new ODTypology();

		d.setDescription(typology.getDescription());
		d.setIdentification(typology.getIdentification());
		d.setUser(userService.getUserByIdentification(userId));

		return typologyRepository.save(d);
	}

	@Override
	public boolean typologyExists(String identification) {
		return !CollectionUtils.isEmpty(typologyRepository.findByIdentification(identification));
	}

	@Override
	public String createNewTypology(ODTypology typology, String userId) {
		if (typologyExists(typology.getIdentification()))
			throw new ODTypologyServiceException("Typology already exists in Database");

		final ODTypology dAux = getNewTypology(typology, userId);

		return dAux.getId();

	}

	@Transactional
	@Override
	public String updatePublicTypology(ODTypology typology, String userId) {
		if (!typologyExists(typology.getIdentification())) {
			throw new ODTypologyServiceException(TYPOLOGY_NOT_EXIST);
		} else {
			final ODTypology d = typologyRepository.findTypologyById(typology.getId());
			d.setDescription(typology.getDescription());
			d.setIdentification(typology.getIdentification());

			return d.getId();
		}
	}

	@Override
	public List<ODTypology> getByUserId(String userId) {
		final User user = userService.getUserByIdentification(userId);
		if (user.isAdmin()) {
			return typologyRepository.findAllByOrderByIdentificationAsc();
		} else {
			return typologyRepository.findByUserOrderByIdentificationAsc(user);
		}
	}

	@Override
	public ODTypology getTypologyEditById(String id, String userId) {
		if (hasUserEditPermission(id, userId)) {
			return typologyRepository.findTypologyById(id);
		}
		throw new ODTypologyServiceException("Cannot view Typology that does not exist or don't have permission");
	}

	@Override
	public boolean hasUserEditPermission(String id, String userId) {
		final User user = userService.getUserByIdentification(userId);
		if (user.isAdmin()) {
			return true;
		} else {
			final boolean propietary = typologyRepository.findTypologyById(id).getUser().getUserId().equals(userId);
			if (propietary) {
				return true;
			}
			return resourceService.hasAccess(userId, id, ResourceAccessType.MANAGE);
		}
	}

	@Override
	public boolean hasUserViewPermission(String id, String userId) {
		final User user = userService.getUserByIdentification(userId);

		if (user.isAdmin()) {
			return true;
		} else {
			final boolean propietary = typologyRepository.findTypologyById(id).getUser().getUserId().equals(userId);
			if (propietary) {
				return true;
			}

			return resourceService.hasAccess(userId, id, ResourceAccessType.VIEW);
		}
	}

	@Override
	public String getCredentialsString(String userId) {
		final User user = userService.getUserByIdentification(userId);
		return user.getUserId();
	}
	
	@Override
	public String getTypologyIdByTypologyIdentification(String identification) {
		final ODTypology typology = getTypologyByIdentification(identification);
		if (typology != null)
			return typology.getId();
		else 
			return null;
	}
	
}
