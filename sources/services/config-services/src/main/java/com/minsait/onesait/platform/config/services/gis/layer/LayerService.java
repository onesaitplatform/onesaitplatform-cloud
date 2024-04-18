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
package com.minsait.onesait.platform.config.services.gis.layer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.minsait.onesait.platform.config.model.Layer;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.User;

public interface LayerService {

	List<Layer> findAllLayers(String userId);

	List<String> getAllIdentificationsByUser(String userId);

	Ontology getOntologyByIdentification(String identification, String sessionUserId);

	void create(Layer layer);

	Layer findById(String id, String userId);

	void deleteLayer(Layer layer, String userId);

	Map<String, String> getOntologyGeometryFields(String identification, String sessionUserId) throws IOException;

	Layer getLayerByIdentification(String identification, User user);

	Boolean isLayerInUse(String layer);

	Layer findByIdentification(String string);

	Map<String, String> getLayersTypes(String userId);

	String getLayerWms(String layer);

	String getLayerKml(String layerIdentification);

	String getLayerSvgImage(String layer);

	List<String> getQueryFields(String query, String ontology, String userId);

	String getQueryParamsAndRefresh(String layer);

	Boolean checkExist(String layerIdentification);

	List<Layer> checkAllLayersByCriteria(String userId, String identification, String description);

}
