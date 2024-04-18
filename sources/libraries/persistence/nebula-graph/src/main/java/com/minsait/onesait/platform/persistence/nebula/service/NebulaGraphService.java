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
package com.minsait.onesait.platform.persistence.nebula.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.minsait.onesait.platform.persistence.nebula.model.NebulaEdge;
import com.minsait.onesait.platform.persistence.nebula.model.NebulaSpace;
import com.minsait.onesait.platform.persistence.nebula.model.NebulaTag;
import com.minsait.onesait.platform.persistence.nebula.service.NebulaGraphServiceImpl.NebulaType;

public interface NebulaGraphService {

	public static final List<String> NEBULA_TYPES = new ArrayList<>(
			Arrays.asList("string", "int", "float", "double", "bool", "date", "time","datetime", "timestamp")
			);

	void createSpace(NebulaSpace space, List<NebulaTag> tags, List<NebulaEdge> edges);

	void createSpace(NebulaSpace space);

	void dropSpace(String space);

	void createTag(String space, NebulaTag tag);

	void dropTag(String space, String tagName);

	void alter(String space, NebulaType nebulaType, String elementName, Map<String, String> toAdd, List<String> toDrop, Map<String, String> toChange );

	void createEdge(String space, NebulaEdge edge);

	void dropEdge(String space, String edgeName);

	JsonNode getHostsInfo();

	JsonNode getTagNames(String space);

	JsonNode getEdgeNames(String space);

	JsonNode executeNGQL(String space, String ngqlQuery);

	List<String> getSpaces();

	String executeNGQLString(String space, String ngqlQuery);

	List<NebulaTag> getTags(String space);

	List<NebulaEdge> getEdges(String space);

	NebulaSpace getSpace(String space);

}
