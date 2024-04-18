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
package com.minsait.onesait.platform.flowengine.api.rest.service;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Set;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.DigitalTwinTypeDTO;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.FlowEngineInvokeRestApiOperationRequest;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.MailRestDTO;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.NotebookDTO;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.NotebookInvokeDTO;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.RestApiDTO;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.RestApiOperationDTO;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.UserDomainValidationRequest;

import javassist.NotFoundException;

public interface FlowEngineNodeService {

	public ResponseEntity<String> deploymentNotification(String json);

	public List<String> getApiRestCategories(String authentication);

	public List<RestApiDTO> getApiRestByUser(String authentication);

	public List<RestApiOperationDTO> getApiRestOperationsByUser(String apiName, Integer version, String authentication);

	public Set<String> getOntologyByUser(String authentication);

	public List<String> getClientPlatformByUser(String authentication);

	public String validateUserDomain(UserDomainValidationRequest request);

	public String submitQuery(String ontology, String queryType, String query, String authentication)
			throws JsonProcessingException, NotFoundException;

	public String submitInsert(String ontology, String data, String domainName)
			throws JsonProcessingException, NotFoundException;

	public List<DigitalTwinTypeDTO> getDigitalTwinTypes(String authentication);

	public ResponseEntity<String> invokeRestApiOperation(FlowEngineInvokeRestApiOperationRequest invokeRequest);

	public void sendMail(MailRestDTO mail);

	public void sendSimpleMail(MailRestDTO mail);

	public List<NotebookDTO> getNotebooksByUser(String authentication);

	public String getNotebookJSONDataByUser(String notebookId, String authentication);
	
	public ResponseEntity<String> invokeNotebook(NotebookInvokeDTO noebookInvocationData);
	
	public List<String> getPipelinesByUser(String authentication);
	
	public ResponseEntity<String> getPipelineStatus(String domainName, String idPipeline);

	public ResponseEntity<String> stopDataflow(String domainName, String pipelineIdentification);
	
	public ResponseEntity<String> startDataflow(String domainName, String pipelineIdentification, String parameters, boolean resetOrigin);
}
