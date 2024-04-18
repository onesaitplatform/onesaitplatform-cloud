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
package com.minsait.onesait.platform.business.services.resources;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Dashboard;
import com.minsait.onesait.platform.config.model.DatasetResource;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.Viewer;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataField;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataOrganization;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataPackage;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataPlatformResourceType;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataResource;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataResourceDTO;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;

public interface ResourceService {

	public List<OpenDataResourceDTO> getDTOFromResourceList(List<OpenDataResource> resources,
			List<DatasetResource> configResources, List<OpenDataPackage> datasetsFromUser,
			List<OpenDataOrganization> orgsFromUser);

	public String createResource(OpenDataResourceDTO resourceDTO, String userToken, List<Map<String, Object>> records,
			List<OpenDataField> fields);
	
	public boolean existsResource(OpenDataResourceDTO resourceDTO, String userToken);

	public String createResource(OpenDataResourceDTO resourceDTO, String userToken, String resourceUrl, String format);

	public String updatePublicPlatformResource(OpenDataResourceDTO resourceDTO, Dashboard dashboard, Viewer viewer,
			Api api);

	public void persistResource(String ontology, String query, String resourceId, String name, User user);

	public void createWebView(String resourceId, String userToken);

	public OpenDataResource getResourceById(String userToken, String id);

	public boolean getModifyPermissions(OpenDataPackage dataset, String userToken);

	public OpenDataResourceDTO getDTOFromResource(OpenDataResource resource, List<DatasetResource> configResources,
			String dataset);

	public List<String> getFilesFormats();

	public void updateResource(OpenDataResourceDTO resourceDTO, OpenDataResource resource, String userToken);

	public void deleteResource(String userToken, String id);

	public void persistResource(String id);

	public ResponseEntity downloadResource(String userToken, OpenDataResource resource, String format);

	public void cleanAllRecords(String id, String userToken);

	public List<Map<String, Object>> getResourceFromUrl(String url, Map<String, String> resultMap)
			throws IOException;

	public Ontology createOntology(String ontologyIdentification, String ontologyDescription, String schema,
			String userId) throws IOException;

	public OperationResultModel insertDataIntoOntology(String ontology, String data, String userId)
			throws JsonProcessingException, IOException;

	public void updateDashboardPermissions(Dashboard dashboard, String datasetId, String userToken);

	public void updateApiPermissions(Api api, String datasetId, String userToken);

	public void updatePlatformResourcesFromDataset(OpenDataPackage dataset, User user);

	public Dashboard checkDashboardResource(String resourceUrl);

	public Viewer checkViewerResource(String resourceUrl);

	public Api checkApiResource(String resourceUrl);

	public OpenDataResourceDTO updateDTOWithPlatformResource(OpenDataResourceDTO resourceDTO, Dashboard dashboard,
			Viewer viewer, Api api);

	public List<OpenDataResource> getResourcesFromOrganization(String orgId, String userToken);
	
	public String getSwaggerGravitee(Api api);

	public String getPlatformResourceUrl(OpenDataResourceDTO resourceDTO, String userToken);

	public String getPlatformResourceFormat(OpenDataResourceDTO resourceDTO);

	public List<Map<String, Object>> executeQuery(String ontology, String query, String userId);

	public String createResourceIteration(OpenDataResourceDTO resourceDTO, String userToken, List<Map<String, Object>> records,
			List<OpenDataField> fields);

	public List<OpenDataField> getResourceFields(String ontology, String userId) throws IOException;

	public OpenDataResourceDTO getDTOFromResource(OpenDataResource resource, DatasetResource configResource, String dataset);

	public String getJsonFromFile(MultipartFile file);

	public String getFirstElement(String jsonData);

	public void updatePlatformResource(OpenDataResourceDTO resourceDTO, OpenDataResource resource, String resourceUrl,
			String userToken);

	public OpenDataPlatformResourceType getPlatformResourceTypeFromUrl(String resourceUrl);

}
