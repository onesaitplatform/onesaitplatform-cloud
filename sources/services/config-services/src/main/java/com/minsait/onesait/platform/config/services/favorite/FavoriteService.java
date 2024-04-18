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
package com.minsait.onesait.platform.config.services.favorite;

import java.util.List;

import com.minsait.onesait.platform.config.model.Favorite;
import com.minsait.onesait.platform.config.model.Favorite.Type;

public interface FavoriteService {
	void create(Type type, String favoriteId, String userId);

	public void update(Type type, String favoriteId, String userId);

	public void delete(Type type, String favoriteId, String userId);
	
	public void delete(Favorite fav);
	
	public List<Favorite> findAll(String userId);

	public List<Favorite> findByFavoriteId(String favoriteId, String userId);
	
    public List<Favorite> findByType(Type type, String userId);
    
    public Favorite findByFavoriteIdAndType(String favoriteId, Type type, String userId);
}