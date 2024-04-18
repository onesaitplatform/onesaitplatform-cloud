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
package com.minsait.onesait.platform.config.services.gis.viewer;

import java.util.List;

import com.minsait.onesait.platform.config.model.BaseLayer;
import com.minsait.onesait.platform.config.model.Viewer;

public interface ViewerService {

	List<Viewer> findAllViewers(String userId);

	List<BaseLayer> findAllBaseLayers();

	List<BaseLayer> getBaseLayersByTechnology(String technology);

	Viewer create(Viewer viewer, String baseMap);

	Boolean hasUserViewPermission(String id, String userId);

	Viewer getViewerById(String id, String userId);

	void deleteViewer(Viewer viewer, String userId);

	Boolean checkExist(Viewer viewer);

	List<Viewer> checkAllViewerByCriteria(String userId, String identification, String description);

	Viewer getViewerPublicById(String id);

}
