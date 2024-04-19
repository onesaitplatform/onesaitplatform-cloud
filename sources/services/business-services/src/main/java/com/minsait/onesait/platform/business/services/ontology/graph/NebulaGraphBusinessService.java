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
package com.minsait.onesait.platform.business.services.ontology.graph;

import java.util.List;

import com.minsait.onesait.platform.persistence.nebula.model.NebulaEdge;
import com.minsait.onesait.platform.persistence.nebula.model.NebulaSpace;
import com.minsait.onesait.platform.persistence.nebula.model.NebulaTag;

public interface NebulaGraphBusinessService {

	void createNebulaGraphEntity(NebulaGraphEntity entity, boolean createEntity);

	void updateNebulaGraphEntity(NebulaGraphUpdateEntity entity);

	void deleteNebulaGraphEntity(String identification, String userId);

	void deleteNebulaGraphEntity(String identification);

	public List<NebulaTag> getTags(String space);

	public List<NebulaEdge> getEdges(String space);

	public NebulaSpace getSpace(String space);

}
