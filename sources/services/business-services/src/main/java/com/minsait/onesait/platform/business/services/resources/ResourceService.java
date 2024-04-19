/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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
package com.minsait.onesait.platform.business.services.resources;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Dashboard;
import com.minsait.onesait.platform.config.model.DatasetResource;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.Viewer;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;

public interface ResourceService {

	public void persistResource(String ontology, String query, String resourceId, String name, User user);

	public List<String> getFilesFormats();

	public List<Map<String, Object>> getResourceFromUrl(String url, Map<String, String> resultMap)
			throws IOException, ParserConfigurationException, SAXException;

	public Ontology createOntology(String ontologyIdentification, String ontologyDescription, String schema,
			String userId) throws IOException;

	public OperationResultModel insertDataIntoOntology(String ontology, String data, String userId)
			throws JsonProcessingException, IOException;

	public Api checkApiResource(String resourceUrl);

	public List<Map<String, Object>> executeQuery(String ontology, String query, String userId);

	public String getJsonFromFile(MultipartFile file);

	public String getFirstElement(String jsonData);

}
