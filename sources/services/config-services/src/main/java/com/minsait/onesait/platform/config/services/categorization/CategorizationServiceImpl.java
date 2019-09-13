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
package com.minsait.onesait.platform.config.services.categorization;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Categorization;
import com.minsait.onesait.platform.config.model.CategorizationUser;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.CategorizationRepository;
import com.minsait.onesait.platform.config.repository.CategorizationUserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CategorizationServiceImpl implements CategorizationService {

	@Autowired
	private CategorizationRepository categorizationRepository;

	@Autowired
	private CategorizationUserRepository categorizationUserRepository;

	@Override
	public void createCategorization(String name, String json, User user) {
		final Categorization categorization = new Categorization();
		final CategorizationUser categorizationUser = new CategorizationUser();

		try {
			categorization.setIdentification(name);
			categorization.setJson(json);
			categorization.setUser(user);

			categorizationRepository.save(categorization);

			categorizationUser.setCategorization(categorizationRepository.findByIdentification(name));
			categorizationUser.setUser(user);
			categorizationUser.setActive(false);
			categorizationUser.setAuthorizationTypeEnum(CategorizationUser.Type.OWNER);

			categorizationUserRepository.save(categorizationUser);
		} catch (Exception e) {
			log.error("Error creating a new Categorization: " + e.getMessage());
		}
	}

	@Override
	public void editCategorization(String id, String json) {

		try {
			final Categorization categorization = categorizationRepository.findById(id);

			categorization.setJson(json);

			categorizationRepository.save(categorization);
		} catch (Exception e) {
			log.error("Error editing the theme: " + e.getMessage());
		}
	}

	@Override
	public void setActive(String id, User user) {

		try {
			List<CategorizationUser> actives = categorizationUserRepository.findByUserAndActive(user);
			for (CategorizationUser categorization : actives) {
				categorization.setActive(false);
				categorizationUserRepository.save(categorization);
			}
			final CategorizationUser tree = categorizationUserRepository.findById(id);
			tree.setActive(true);
			categorizationUserRepository.save(tree);
		} catch (Exception e) {
			log.error("Error setting to active: " + e.getMessage());
		}
	}

	@Override
	public void deactivate(String id) {
		try {
			final CategorizationUser tree = categorizationUserRepository.findById(id);
			tree.setActive(false);
			categorizationUserRepository.save(tree);
		} catch (Exception e) {
			log.error("Error setting to inactive: " + e.getMessage());
		}
	}

	@Override
	public void addAuthorization(Categorization categorization, User user, String accessType) {
		final CategorizationUser categorizationUser = new CategorizationUser();

		categorizationUser.setCategorization(categorization);
		categorizationUser.setUser(user);
		categorizationUser.setAuthorizationType(accessType);
		categorizationUser.setActive(false);

		categorizationUserRepository.save(categorizationUser);
	}

	@Override
	public boolean hasUserPermission(User user, Categorization categorization) {
		return (categorizationUserRepository.findByUserAndCategorization(user, categorization) != null);
	}

}
