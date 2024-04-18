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
package com.minsait.onesait.platform.config.services.favorite;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Favorite;
import com.minsait.onesait.platform.config.model.Favorite.Type;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.FavoriteRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FavoriteServiceImpl implements FavoriteService {

	@Autowired
	private FavoriteRepository favoriteRepository;

	@Autowired
	private UserRepository userRepository;


    @Override
    public void create(Type type, String favoriteId, String userId) {
        final User user = userRepository.findByUserId(userId);
        if (null == favoriteRepository.findByFavoriteIdAndTypeAndUser(favoriteId, type, user)) {
            Favorite favorite = new Favorite();
            favorite.setFavoriteId(favoriteId);
            favorite.setType(type);
            favorite.setUser(user);
            favoriteRepository.save(favorite);
        }
    }

    @Override
    public void update(Type type, String favoriteId, String userId) {
        final User user = userRepository.findByUserId(userId);
        Favorite favorite = favoriteRepository.findByFavoriteIdAndTypeAndUser(favoriteId, type, user);
        if (null != favorite) {
            favorite.setType(type);
            favoriteRepository.save(favorite);
        }
    }

    @Override
    public void delete(Type type, String favoriteId, String userId) {
        final User user = userRepository.findByUserId(userId);
        if (favoriteId != null && type != null) {
            Favorite favorite = favoriteRepository.findByFavoriteIdAndTypeAndUser(favoriteId, type, user);
            favoriteRepository.delete(favorite);
        }
    }

	@Override
	public List<Favorite> findAll(String userId) {
		final User user = userRepository.findByUserId(userId);
		return favoriteRepository.findByUser(user);
	}

	@Override
	public List<Favorite> findByFavoriteId(String favoriteId, String userId) {
		final User user = userRepository.findByUserId(userId);
		if (favoriteId != null) {
			List<Favorite> favorites = favoriteRepository.findByUserAndFavoriteId(user, favoriteId);
			return favorites;
		}
		return null;
	}
	
    @Override
    public List<Favorite> findByType(Type type, String userId) {
        final User user = userRepository.findByUserId(userId);
        if (type != null) {
            List<Favorite> favorites = favoriteRepository.findByUserAndType(user, type);
            return favorites;
        }
        return null;
    }

    @Override
    public Favorite findByFavoriteIdAndType(String favoriteId, Type type, String userId) {
        final User user = userRepository.findByUserId(userId);
        if (type != null) {
            return favoriteRepository.findByFavoriteIdAndTypeAndUser(favoriteId, type, user);
        }
        return null;
    }

    @Override
    public void delete(Favorite fav) {
        favoriteRepository.delete(fav);
    }
}
