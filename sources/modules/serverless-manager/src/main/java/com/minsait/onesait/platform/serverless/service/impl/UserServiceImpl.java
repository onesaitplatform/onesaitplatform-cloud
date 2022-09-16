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
package com.minsait.onesait.platform.serverless.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.serverless.exception.UserNotFoundException;
import com.minsait.onesait.platform.serverless.model.User;
import com.minsait.onesait.platform.serverless.repository.UserRepository;
import com.minsait.onesait.platform.serverless.service.UserService;
import com.minsait.onesait.platform.serverless.utils.SecurityUtils;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepository;

	@Override
	public User getUser(String username) {
		return userRepository.findById(username).orElseThrow(
				() -> new UserNotFoundException("User " + username + " does not exist."));
	}

	@Override
	public User getCurrentUser() {
		return getUser(SecurityUtils.getCurrentUser());
	}

	@Override
	public User create(User user) {
		return userRepository.save(user);
	}

}
