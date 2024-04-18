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
package com.minsait.onesait.platform.config.services.categorization.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Categorization;
import com.minsait.onesait.platform.config.model.CategorizationUser;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.CategorizationUserRepository;

@Service
public class CategorizationUserServiceImpl implements CategorizationUserService {
	
	@Autowired
	private CategorizationUserRepository categorizationUserRepository;

	@Override
	public List<CategorizationUser> findbyUser(User user) {
		if(user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return categorizationUserRepository.findAllOwner();
		}
		return categorizationUserRepository.findByUserAndAuth(user);
	}

	@Override
	public CategorizationUser findByCategorizationAndUser(Categorization categorization, User user) {
		return categorizationUserRepository.findByUserAndCategorization(user, categorization);
	}

	@Override
	public void deleteCategorizationUser(CategorizationUser categorizationUser) {
		categorizationUserRepository.delete(categorizationUser);
	}

}
