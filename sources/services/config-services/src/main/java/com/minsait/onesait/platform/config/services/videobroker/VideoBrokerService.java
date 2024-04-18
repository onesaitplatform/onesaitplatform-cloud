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
package com.minsait.onesait.platform.config.services.videobroker;

import java.util.List;

import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.VideoCapture;

public interface VideoBrokerService {

	public List<VideoCapture> getVideoCaptures(String userId);

	public VideoCapture get(String id);

	public String create(VideoCapture videoCapture);

	public List<VideoCapture> getAll();

	public void update(VideoCapture videoCapture);

	public void updateState(VideoCapture videoCapture);

	public void delete(String id);

	public boolean hasUserAccess(String id, String userId);

	public Ontology createOntologyVideoResults(VideoCapture videoCapture);
}
