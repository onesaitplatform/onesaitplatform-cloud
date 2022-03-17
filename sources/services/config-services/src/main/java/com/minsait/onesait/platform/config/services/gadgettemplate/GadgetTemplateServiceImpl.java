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
package com.minsait.onesait.platform.config.services.gadgettemplate;

import java.util.ArrayList;
import java.util.List;

import com.minsait.onesait.platform.config.model.GadgetTemplateType;
import com.minsait.onesait.platform.config.repository.GadgetTemplateTypeRepository;
import org.jline.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.GadgetTemplate;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.GadgetTemplateRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.exceptions.GadgetTemplateServiceException;
import com.minsait.onesait.platform.config.services.user.UserService;

@Service
public class GadgetTemplateServiceImpl implements GadgetTemplateService {

	@Autowired
	private GadgetTemplateRepository gadgetTemplateRepository;

	@Autowired
	private GadgetTemplateTypeRepository gadgetTemplateTypeRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserService userService;

	public static final String ADMINISTRATOR = "ROLE_ADMINISTRATOR";

	@Override
	public List<GadgetTemplate> findAllGadgetTemplates() {
		return gadgetTemplateRepository.findAll();
	}

	@Override
	public List<GadgetTemplate> findGadgetTemplateWithIdentificationAndDescription(String identification,
			String description, String userId) {
		List<GadgetTemplate> gadgetTemplates;
		final User user = userRepository.findByUserId(userId);

		if (user.getRole().getId().equals(ADMINISTRATOR)) {
			if (identification == null) {
				gadgetTemplates = gadgetTemplateRepository.findAll();
			} else {
				gadgetTemplates = gadgetTemplateRepository.findByIdentificationContaining(identification);
			}
		} else {
			if (identification == null) {
				gadgetTemplates = gadgetTemplateRepository.findGadgetTemplateByUserAndIsPublicTrue(user.getUserId());
			} else {
				gadgetTemplates = gadgetTemplateRepository
						.findGadgetTemplateByUserAndIsPublicTrueAndIdentificationLike(user.getUserId(), identification);
			}
		}

		return gadgetTemplates;
	}

	@Override
	public List<String> getAllIdentifications() {
		final List<GadgetTemplate> gadgetTemplates = gadgetTemplateRepository.findAllByOrderByIdentificationAsc();
		final List<String> names = new ArrayList<>();
		for (final GadgetTemplate gadgetTemplate : gadgetTemplates) {
			names.add(gadgetTemplate.getIdentification());
		}
		return names;
	}

	@Override
	public GadgetTemplate getGadgetTemplateById(String id) {
		return gadgetTemplateRepository.findById(id).orElse(null);
	}

	@Override
	public GadgetTemplate getGadgetTemplateByIdentification(String identification) {
		return gadgetTemplateRepository.findByIdentification(identification);
	}

	@Override
	public boolean hasUserPermission(String id, String userId) {
		final User user = userRepository.findByUserId(userId);
		if (userService.isUserAdministrator(user)) {
			Log.info("user has permission");
			return true;
		} else {
			Log.info("user has not permission");
			return gadgetTemplateRepository.findById(id).orElse(null).getUser().getUserId().equals(userId);
		}
	}

	@Override
	public void updateGadgetTemplate(GadgetTemplate gadgettemplate) {
		gadgetTemplateRepository.save(gadgettemplate);
	}

	@Override
	public void createGadgetTemplate(GadgetTemplate gadgettemplate) {
		try {
			gadgetTemplateRepository.save(gadgettemplate);
		} catch (final Exception e) {
			throw new GadgetTemplateServiceException("Can not save gadgetTemplate");
		}

	}

	@Override
	public void deleteGadgetTemplate(String id, String userId) {
		if (hasUserPermission(id, userId)) {
			final GadgetTemplate gadgetTemplate = gadgetTemplateRepository.findById(id).orElse(null);
			if (gadgetTemplate != null) {
				gadgetTemplateRepository.delete(gadgetTemplate);
			} else
				throw new GadgetTemplateServiceException("Can not delete gadgetTemplate that does not exist");
		}

	}

	@Override
	public List<GadgetTemplate> getUserGadgetTemplate(String userId) {
		return gadgetTemplateRepository.findGadgetTemplateByUserAndIsPublicTrue(userId);
	}

	@Override
	public List<GadgetTemplate> getUserGadgetTemplate(String userId, String type) {
		final User user = userRepository.findByUserId(userId);

		if (user.getRole().getId().equals(ADMINISTRATOR)) {
			return this.gadgetTemplateRepository.findByType(type);
		}
		else {
			return this.gadgetTemplateRepository.findGadgetTemplateByUserAndIsPublicTrueAndType(userId, type);
		}
	}

	@Override
	public GadgetTemplate getGadgetTemplateByIdentification(String identification, String userId) {
		final User user = userRepository.findByUserId(userId);
		GadgetTemplate gadgetTemplate;
		if (userService.isUserAdministrator(user)) {
			gadgetTemplate = gadgetTemplateRepository.findByIdentification(identification);
		} else {
			gadgetTemplate = gadgetTemplateRepository
					.findGadgetTemplateByUserAndIsPublicTrueAndIdentification(user.getUserId(), identification);
		}
		return gadgetTemplate;
	}

	@Override
	public List<GadgetTemplateType> getTemplateTypes() {
		return gadgetTemplateTypeRepository.findAll();
	}

	@Override
	public GadgetTemplateType getTemplateTypeById(String id) {
		return gadgetTemplateTypeRepository.findById(id).orElse(null);
	}
}
