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
package com.minsait.onesait.platform.config.services.usertoken;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.UserToken;
import com.minsait.onesait.platform.config.repository.UserTokenRepository;

@Service

public class UserTokenServiceImpl implements UserTokenService {

	@Autowired
	private UserTokenRepository userTokenRepository;

	@Override
	public UserToken generateToken(User user) throws GenericOPException {
		UserToken userToken = new UserToken();
		if (user.getUserId() != null) {
			userToken.setUser(user);
			userToken.setToken(UUID.randomUUID().toString().replaceAll("-", ""));
			if (userTokenRepository.findByToken(userToken.getToken()) == null) {
				userToken = userTokenRepository.save(userToken);
			} else {
				throw new GenericOPException("Token with value " + userToken.getToken() + " already exists");
			}
		}
		return userToken;
	}

	@Override
	public UserToken getToken(User user) {
		return userTokenRepository.findByUser(user).get(0);
	}

	@Override
	public UserToken getTokenByToken(String token) {
		return userTokenRepository.findByToken(token);
	}

	@Override
	public UserToken getTokenByUserAndToken(User user, String token) {
		return userTokenRepository.findByUserAndToken(user, token);
	}

	@Override
	public void deactivateToken(UserToken userToken, boolean active) {
		userTokenRepository.save(userToken);

	}

	@Override
	public UserToken getTokenByID(String id) {
		return userTokenRepository.findById(id).orElse(null);
	}

	@Override
	public List<UserToken> getTokens(User user) {
		return userTokenRepository.findByUser(user);
	}

	@Override
	public void removeToken(User user, String token) {
		final UserToken userToken = userTokenRepository.findByUserAndToken(user, token);
		if (userToken != null) {
			userTokenRepository.delete(userToken);
		}
	}

}
