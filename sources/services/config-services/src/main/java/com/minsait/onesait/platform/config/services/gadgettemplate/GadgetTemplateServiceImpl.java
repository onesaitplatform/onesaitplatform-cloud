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
package com.minsait.onesait.platform.config.services.gadgettemplate;

import java.util.ArrayList;
import java.util.List;

import org.jline.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.GadgetTemplate;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.GadgetTemplateRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.exceptions.GadgetTemplateServiceException;

@Service
public class GadgetTemplateServiceImpl implements GadgetTemplateService {

	@Autowired
	private GadgetTemplateRepository gadgetTemplateRepository;

	@Autowired
	private UserRepository userRepository;

	public static final String ADMINISTRATOR = "ROLE_ADMINISTRATOR";

	@Override
	public List<GadgetTemplate> findAllGadgetTemplates() {
		return this.gadgetTemplateRepository.findAll();
	}

	@Override
	public List<GadgetTemplate> findGadgetTemplateWithIdentificationAndDescription(String identification,
			String description, String userId) {
		List<GadgetTemplate> gadgetTemplates;
		User user = this.userRepository.findByUserId(userId);

		if (user.getRole().getId().equals(ADMINISTRATOR)) {
			gadgetTemplates = this.gadgetTemplateRepository.findAll();
		} else {
			gadgetTemplates = this.gadgetTemplateRepository.findGadgetTemplateByUserAndIsPublicTrue(user.getUserId());
		}

		return gadgetTemplates;
	}

	@Override
	public List<String> getAllIdentifications() {
		List<GadgetTemplate> gadgetTemplates = this.gadgetTemplateRepository.findAllByOrderByIdentificationAsc();
		List<String> names = new ArrayList<>();
		for (GadgetTemplate gadgetTemplate : gadgetTemplates) {
			names.add(gadgetTemplate.getIdentification());
		}
		return names;
	}

	@Override
	public GadgetTemplate getGadgetTemplateById(String id) {
		return this.gadgetTemplateRepository.findById(id);
	}

	@Override
	public boolean hasUserPermission(String id, String userId) {
		User user = userRepository.findByUserId(userId);
		if (user.getRole().getId().equals(ADMINISTRATOR)) {
			Log.info("user has permission");
			return true;
		} else {
			Log.info("user has not permission");
			return gadgetTemplateRepository.findById(id).getUser().getUserId().equals(userId);
		}
	}

	@Override
	public void updateGadgetTemplate(GadgetTemplate gadgettemplate) {
		gadgetTemplateRepository.save(gadgettemplate);
	}

	@Override
	public void createGadgetTemplate(GadgetTemplate gadgettemplate) {
		gadgetTemplateRepository.save(gadgettemplate);

	}

	@Override
	public void deleteGadgetTemplate(String id, String userId) {
		if (hasUserPermission(id, userId)) {
			GadgetTemplate gadgetTemplate = this.gadgetTemplateRepository.findById(id);
			if (gadgetTemplate != null) {
				this.gadgetTemplateRepository.delete(gadgetTemplate);
			} else
				throw new GadgetTemplateServiceException("Cannot delete gadgetTemplate that does not exist");
		}

	}

	@Override
	public List<GadgetTemplate> getUserGadgetTemplate(String userId) {
		return this.gadgetTemplateRepository.findGadgetTemplateByUserAndIsPublicTrue(userId);
	}

	@Override
	public GadgetTemplate getGadgetTemplateByIdentification(String identification, String userId) {
		User user = this.userRepository.findByUserId(userId);
		GadgetTemplate gadgetTemplate;
		if (user.getRole().getId().equals(ADMINISTRATOR)) {
			gadgetTemplate = this.gadgetTemplateRepository.findByIdentification(identification);
		} else {
			gadgetTemplate = this.gadgetTemplateRepository
					.findGadgetTemplateByUserAndIsPublicTrueAndIdentification(user.getUserId(), identification);
		}
		return gadgetTemplate;
	}

}
