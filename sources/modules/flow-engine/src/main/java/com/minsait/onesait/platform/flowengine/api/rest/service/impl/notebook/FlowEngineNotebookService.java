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
package com.minsait.onesait.platform.flowengine.api.rest.service.impl.notebook;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.model.Notebook;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.flowdomain.FlowDomainService;
import com.minsait.onesait.platform.config.services.notebook.NotebookService;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.DecodedAuthentication;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.NotebookDTO;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.NotebookInvokeDTO;
import com.minsait.onesait.platform.flowengine.api.rest.service.FlowEngineValidationNodeService;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.config.model.MasterUser;
import com.minsait.onesait.platform.multitenant.config.repository.MasterUserRepository;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FlowEngineNotebookService {


	@Autowired
	private FlowEngineValidationNodeService flowEngineValidationNodeService;
	@Autowired
	private NotebookService notebookService;
	@Autowired
	private FlowDomainService domainService;
	@Autowired
	private MasterUserRepository masterUserRepository;
	
	private static final String ERROR_DOMAIN = "{'error':'Domain ";
	
	public List<NotebookDTO> getNotebooksByUser(String authentication) {

		final List<NotebookDTO> notebooks = new ArrayList<>();
		final DecodedAuthentication decodedAuth = flowEngineValidationNodeService.decodeAuth(authentication);
		flowEngineValidationNodeService.validateUser(decodedAuth.getUserId());
		final List<Notebook> notebooksList = notebookService.getNotebooksForListWithProjectsAccess(decodedAuth.getUserId());

		for (final Notebook notebook : notebooksList) {
			final NotebookDTO notebookDTO = new NotebookDTO();
			notebookDTO.setId(notebook.getIdzep());
			notebookDTO.setName(notebook.getIdentification());
			notebooks.add(notebookDTO);
		}
		return notebooks;
	}

	public String getNotebookJSONDataByUser(String notebookId, String authentication) {
		final DecodedAuthentication decodedAuth = flowEngineValidationNodeService.decodeAuth(authentication);
		flowEngineValidationNodeService.validateUser(decodedAuth.getUserId());
		return notebookService.exportNotebook(notebookId, decodedAuth.getUserId()).toString();
	}

	public ResponseEntity<String> invokeNotebook(NotebookInvokeDTO notebookInvocationData) {
		MultitenancyContextHolder.setVerticalSchema(notebookInvocationData.getVerticalSchema());
		final FlowDomain domain = domainService.getFlowDomainByIdentification(notebookInvocationData.getDomainName());
		if (domain == null) {
			log.error("Domain {} not found for Notebook execution.", notebookInvocationData.getDomainName());
			return new ResponseEntity<>(
					ERROR_DOMAIN + notebookInvocationData.getDomainName()
							+ " not found for Notebook invocation: '" + notebookInvocationData.getNotebookId() + "'.'}",
					HttpStatus.BAD_REQUEST);
		}
		final User platformUser = domain.getUser();
		final MasterUser user = masterUserRepository.findByUserId(platformUser.getUserId());
		MultitenancyContextHolder.setTenantName(user.getTenant().getName());
		final Notebook notebook = notebookService.getNotebookByZepId(notebookInvocationData.getNotebookId(),
				domain.getUser().getUserId());
		final String cloneName = notebook.getIdentification() + "-clone-" + UUID.randomUUID();
		// clone notebook
		final String cloneNotebookId = notebookService.cloneNotebookOnlyZeppelin(cloneName,
				notebookInvocationData.getNotebookId(), domain.getUser().getUserId());
		// run notebook according to data in DTO
		notebookInvocationData.setNotebookId(cloneNotebookId);
		try {
			final ResponseEntity<String> response = runNotebookInvocation(notebookInvocationData);
			// delete clone
			notebookService.removeNotebookOnlyZeppelin(cloneNotebookId, domain.getUser().getUserId());
			return response;
		} catch (URISyntaxException | IOException e) {
			final String msg = "{'error':'" + e.getMessage() + "'}";
			return new ResponseEntity<>(msg, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	private ResponseEntity<String> runNotebookInvocation(NotebookInvokeDTO notebookInvocationData)
			throws URISyntaxException, IOException {
		final List<String> allParagraphsResult = new ArrayList<>();
		if (Boolean.TRUE.equals(notebookInvocationData.getExecuteNotebook())) {
			final ResponseEntity<String> executionResult = notebookService
					.runAllParagraphs(notebookInvocationData.getNotebookId());
			if (executionResult.getStatusCode() != HttpStatus.OK) {

				return executionResult;
			} else {
				// get each requested Paragraph output
				for (final Map<String, String> paragraph : notebookInvocationData.getOutputParagraphs()) {
					final String paragraphId = paragraph.get("paragraph");
					final ResponseEntity<String> paragraphResult = notebookService
							.getParagraphResult(notebookInvocationData.getNotebookId(), paragraphId);
					if (paragraphResult.getStatusCode() == HttpStatus.OK) {
						allParagraphsResult.add(paragraphResult.getBody());
					} else {
						return paragraphResult;
					}
				}
				return new ResponseEntity<>(new Gson().toJson(allParagraphsResult), HttpStatus.OK);
			}
		} else {
			return notebookService.runParagraph(notebookInvocationData.getNotebookId(),
					notebookInvocationData.getParagraphId(), notebookInvocationData.getExecutionParams());
		}
	}
}
