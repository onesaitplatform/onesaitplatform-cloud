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
package com.minsait.onesait.platform.config.services.mapslayer;

import java.util.List;

import com.minsait.onesait.platform.config.model.MapsLayer;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.mapslayer.dto.MapsLayerDTO;

public interface MapsLayerService {

	public List<MapsLayerDTO> getLayersForUser(String userId, String identification);

	public List<MapsLayer> getByIdentifier(String identification);

	public void save(MapsLayer mapsLayer);

	public MapsLayer getByIdANDUser(String id, String userId);

	public boolean hasUserViewPermission(String id, String userId);

	public String getAccessType(String id, String userId);

	public boolean hasUserEditPermission(String id, String userId);

	public boolean hasUserPermission(String id, String userId);

	public boolean exists(MapsLayer mapsStyle);

	public void update(MapsLayer mapsStyle);

	public void delete(String id, String userId);

	public MapsLayer getById(String identification);

	public String clone(MapsLayer originalMapsLayer, String identification, User user);

	public MapsLayer getByIdentificationANDUser(String identification, String userId);

}
