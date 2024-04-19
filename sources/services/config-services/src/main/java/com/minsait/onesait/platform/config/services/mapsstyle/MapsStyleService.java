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
package com.minsait.onesait.platform.config.services.mapsstyle;

import java.util.List;

import com.minsait.onesait.platform.config.model.MapsStyle;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.mapsstyle.dto.MapsStyleDTO;

public interface MapsStyleService {

	public List<MapsStyle> getStylesForUser(String userId, String identification);

	public List<MapsStyle> getByIdentifier(String identification);

	public void save(MapsStyle mapsStyle);

	public MapsStyle getByIdANDUser(String id, String userId);

	public boolean hasUserViewPermission(String id, String userId);

	public String getAccessType(String id, String userId);

	public boolean hasUserEditPermission(String id, String userId);

	public boolean hasUserPermission(String id, String userId);

	public boolean exists(MapsStyle mapsStyle);

	public void update(MapsStyle mapsStyle);

	public void delete(String id, String userId);

	public MapsStyle getById(String identification);

	public String clone(MapsStyle originalMapsStyle, String identification, User user);

	List<MapsStyleDTO> getStylesForUserWithEmpty(String userId, String identification);

}
