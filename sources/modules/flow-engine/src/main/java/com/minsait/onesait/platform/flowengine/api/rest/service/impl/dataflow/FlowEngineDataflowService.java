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
package com.minsait.onesait.platform.flowengine.api.rest.service.impl.dataflow;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.model.Pipeline;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.dataflow.DataflowService;
import com.minsait.onesait.platform.config.services.flowdomain.FlowDomainService;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.DataflowDTO;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.DecodedAuthentication;
import com.minsait.onesait.platform.flowengine.api.rest.service.FlowEngineValidationNodeService;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.config.model.MasterUser;
import com.minsait.onesait.platform.multitenant.config.repository.MasterUserRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FlowEngineDataflowService {


	@Autowired
	private FlowEngineValidationNodeService flowEngineValidationNodeService;
	@Autowired
	private DataflowService dataflowService;
	@Autowired
	private FlowDomainService domainService;
	@Autowired
	private MasterUserRepository masterUserRepository;
	
	private static final String ERROR_DOMAIN = "{'error':'Domain ";
	private static final String PIPELINE_NOT_EXISTS = "Specified Pipeline '{}' does not exist.";
	private static final String PIPELINE_NOT_EXITS_MSG = "{'error':'Specified Pipeline does not exist.'}";
	
	public List<String> getPipelinesByUser(String authentication) {
		final List<String> pipelinesByUser = new ArrayList<>();
		final DecodedAuthentication decodedAuth = flowEngineValidationNodeService.decodeAuth(authentication);
		final User sofia2User = flowEngineValidationNodeService.validateUser(decodedAuth.getUserId());
		final List<Pipeline> pipelines = dataflowService.getPipelinesForListWithProjectsAccess(sofia2User.getUserId());
		for (final Pipeline pipeline : pipelines) {
			pipelinesByUser.add(pipeline.getIdentification());
		}
		return pipelinesByUser;
	}

	
	public ResponseEntity<String> getPipelineStatus(DataflowDTO dataflowData) {
		MultitenancyContextHolder.setVerticalSchema(dataflowData.getVerticalSchema());
		final FlowDomain domain = domainService.getFlowDomainByIdentification(dataflowData.getDomainName());
		if (domain == null) {
			log.error("Domain {} not found for DataFlow Status Check execution.", dataflowData.getDomainName());
			return new ResponseEntity<>(ERROR_DOMAIN + dataflowData.getDomainName()
					+ " not found for DataFlow Status Check execution: '" + dataflowData.getDataflowIdentification() + "'.'}",
					HttpStatus.BAD_REQUEST);
		}
		final User platformUser = domain.getUser();
		final MasterUser user = masterUserRepository.findByUserId(platformUser.getUserId());
		MultitenancyContextHolder.setTenantName(user.getTenant().getName());
		final Pipeline pipeline = dataflowService.getPipelineByIdentification(dataflowData.getDataflowIdentification());
		if (pipeline == null) {
			// Pipeline does not exist
			log.error(PIPELINE_NOT_EXISTS, dataflowData.getDataflowIdentification());
			return new ResponseEntity<>(PIPELINE_NOT_EXITS_MSG, HttpStatus.NOT_FOUND);
		}

		if (!dataflowService.hasUserViewPermission(pipeline, domain.getUser().getUserId())) {
			// User has no permissions over the requested pipeline
			log.error("User has no VIEW permissions over the specified Pipeline '{}'.", dataflowData.getDataflowIdentification());
			return new ResponseEntity<>(
					"{'error':'Forbidden. User has no VIEW permissions over the requested resource.'}",
					HttpStatus.FORBIDDEN);
		}

		return dataflowService.statusPipeline(domain.getUser().getUserId(), dataflowData.getDataflowIdentification());
	}

	
	public ResponseEntity<String> stopDataflow(DataflowDTO dataflowData) {
		MultitenancyContextHolder.setVerticalSchema(dataflowData.getVerticalSchema());
		final FlowDomain domain = domainService.getFlowDomainByIdentification(dataflowData.getDomainName());
		if (domain == null) {
			log.error("Domain {} not found for DataFlow Stop execution.", dataflowData.getDomainName());
			return new ResponseEntity<>(ERROR_DOMAIN + dataflowData.getDomainName() + " not found for DataFlow Stop execution: '"
					+ dataflowData.getDataflowIdentification() + "'.'}", HttpStatus.BAD_REQUEST);
		}
		final User platformUser = domain.getUser();
		final MasterUser user = masterUserRepository.findByUserId(platformUser.getUserId());
		MultitenancyContextHolder.setTenantName(user.getTenant().getName());
		final Pipeline pipeline = dataflowService.getPipelineByIdentification(dataflowData.getDataflowIdentification());
		if (pipeline == null) {
			// Pipeline does not exist
			log.error(PIPELINE_NOT_EXISTS, dataflowData.getDataflowIdentification());
			return new ResponseEntity<>(PIPELINE_NOT_EXITS_MSG, HttpStatus.NOT_FOUND);
		}

		if (!dataflowService.hasUserEditPermission(pipeline, domain.getUser().getUserId())) {
			// User has no permissions over the requested pipeline
			log.error("User has no EDIT permissions over the specified Pipeline '{}'.", dataflowData.getDataflowIdentification());
			return new ResponseEntity<>(
					"{'error':'Forbidden. User has no EDIT permissions over the requested resource.'}",
					HttpStatus.FORBIDDEN);
		}
		return dataflowService.stopPipeline(domain.getUser().getUserId(), dataflowData.getDataflowIdentification());
	}

	
	public ResponseEntity<String> startDataflow(DataflowDTO dataflowData) {
		MultitenancyContextHolder.setVerticalSchema(dataflowData.getVerticalSchema());
		final FlowDomain domain = domainService.getFlowDomainByIdentification(dataflowData.getDomainName());

		if (domain == null) {
			log.error("Domain {} not found for DataFlow Start execution.", dataflowData.getDomainName());
			return new ResponseEntity<>(ERROR_DOMAIN + dataflowData.getDomainName() + " not found for DataFlow Start execution: '"
					+ dataflowData.getDataflowIdentification() + "'.'}", HttpStatus.BAD_REQUEST);
		}
		final User platformUser = domain.getUser();
		final MasterUser user = masterUserRepository.findByUserId(platformUser.getUserId());
		MultitenancyContextHolder.setTenantName(user.getTenant().getName());
		final Pipeline pipeline = dataflowService.getPipelineByIdentification(dataflowData.getDataflowIdentification());
		if (pipeline == null) {
			// Pipeline does not exist
			log.error(PIPELINE_NOT_EXISTS, dataflowData.getDataflowIdentification());
			return new ResponseEntity<>(PIPELINE_NOT_EXITS_MSG, HttpStatus.NOT_FOUND);
		}

		if (!dataflowService.hasUserEditPermission(pipeline, domain.getUser().getUserId())) {
			// User has no permissions over the requested pipeline
			log.error("User has no EDIT permissions over the specified Pipeline '{}'.", dataflowData.getDataflowIdentification());
			return new ResponseEntity<>(
					"{'error':'Forbidden. User has no EDIT permissions over the requested resource.'}",
					HttpStatus.FORBIDDEN);
		}

		if (dataflowData.getResetOrigin()) {
			dataflowService.resetOffsetPipeline(domain.getUser().getUserId(), dataflowData.getDataflowIdentification());
		}
		return dataflowService.startPipeline(domain.getUser().getUserId(), dataflowData.getDataflowIdentification(), dataflowData.getParameters());
	}
}
