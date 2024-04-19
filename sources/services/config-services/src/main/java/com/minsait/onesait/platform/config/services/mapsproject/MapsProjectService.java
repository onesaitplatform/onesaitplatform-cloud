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
package com.minsait.onesait.platform.config.services.mapsproject;

import java.util.List;

import com.minsait.onesait.platform.config.model.MapsProject;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.mapsproject.dto.MapsProjectDTO;

public interface MapsProjectService {

	public List<MapsProject> getProjectsForUser(String userId, String identification);

	List<MapsProject> getByIdentifier(String identification);

	MapsProject getById(String identification);

	MapsProject getByIdANDUser(String id, String userId);

	void save(MapsProject mapsLayer);

	void update(MapsProject mapsLayer);

	boolean exists(MapsProject mapsLayer);

	boolean hasUserPermission(String id, String userId);

	boolean hasUserViewPermission(String id, String userId);

	String getAccessType(String id, String userId);

	boolean hasUserEditPermission(String id, String userId);

	void delete(String id, String userId);

	String clone(MapsProject originalMapsProject, String identification, User user);

	String importMapsProject(String originalMapProject, boolean overwrite, User user);

	String exportMapsProject(String id, User user);

	List<MapsProjectDTO> getProjectsForUserDTO(String userId, String identification);

}
