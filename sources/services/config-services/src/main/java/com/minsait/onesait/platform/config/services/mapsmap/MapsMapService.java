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
package com.minsait.onesait.platform.config.services.mapsmap;

import java.util.List;

import com.minsait.onesait.platform.config.model.MapsMap;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.mapsmap.dto.MapsMapDTO;

public interface MapsMapService {

	public List<MapsMapDTO> getMapsForUser(String userId, String identification);

	public MapsMap getByIdANDUser(String id, String userId);

	public void save(MapsMap mapsMap);

	public void update(MapsMap mapsMap);

	public boolean exists(MapsMap mapsMap);

	public boolean hasUserPermission(String id, String userId);

	public boolean hasUserViewPermission(String id, String userId);

	public String getAccessType(String id, String userId);

	public boolean hasUserEditPermission(String id, String userId);

	public void delete(String id, String userId);

	public String clone(MapsMap originalMapsMap, String identification, User user);

	public List<MapsMap> getByIdentifier(String identification);

	public MapsMap getById(String identification);

}
