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
package com.minsait.onesait.platform.config.services.flowdomain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Flow;
import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.model.FlowDomain.State;
import com.minsait.onesait.platform.config.model.FlowNode;
import com.minsait.onesait.platform.config.model.FlowNode.Type;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.FlowDomainRepository;
import com.minsait.onesait.platform.config.repository.FlowNodeRepository;
import com.minsait.onesait.platform.config.repository.FlowRepository;
import com.minsait.onesait.platform.config.services.apimanager.ApiManagerService;
import com.minsait.onesait.platform.config.services.exceptions.FlowDomainServiceException;
import com.minsait.onesait.platform.config.services.exceptions.OPResourceServiceException;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FlowDomainServiceImpl implements FlowDomainService {

	@Value("${onesaitplatform.flowengine.port.domain.min:8000}")
	private int domainPortMin;
	@Value("${onesaitplatform.flowengine.port.domain.max:8500}")
	private int domainPortMax;
	@Value("${onesaitplatform.flowengine.port.service.min:7000}")
	private int servicePortMin;
	@Value("${onesaitplatform.flowengine.port.service.max:7500}")
	private int servicePortMax;
	@Value("${onesaitplatform.flowengine.home.base:/tmp/}")
	private String homeBase;

	@Autowired
	public FlowDomainRepository domainRepository;

	@Autowired
	private FlowRepository flowRepository;

	@Autowired
	private FlowNodeRepository nodeRepository;

	@Autowired
	private OPResourceService resourceService;
	@Autowired
	private UserService userService;
	@Autowired
	private ApiManagerService apiManagerService;
	@Autowired
	private MultitenancyService multitenancyService;

	@Override
	public List<FlowDomain> getFlowDomainByUser(User user) {
		if (Role.Type.ROLE_ADMINISTRATOR.name().equalsIgnoreCase(user.getRole().getId())) {
			return domainRepository.findAll();
		}
		final List<FlowDomain> domains = new ArrayList<>();
		final FlowDomain domain = domainRepository.findByUserUserId(user.getUserId());
		if (domain != null) {
			domains.add(domain);
		}
		return domains;
	}

	@Override
	public void deleteFlowDomainFlows(String domainIdentification, User user) {
		final FlowDomain domain = domainRepository.findByIdentification(domainIdentification);
		// Delete all data from this Domain,
		// including flows, nodes and properties
		final List<Flow> flows = flowRepository.findByFlowDomain_Identification(domain.getIdentification());
		for (final Flow flow : flows) {
			final List<FlowNode> nodes = nodeRepository.findByFlow_NodeRedFlowId(flow.getNodeRedFlowId());
			for (final FlowNode node : nodes) {
				if (node.getFlowNodeType() == Type.API_REST) {
					deleteFlowAPI(node, user);
				}
				nodeRepository.delete(node);
			}
			flowRepository.delete(flow);
		}
	}

	private void deleteFlowAPI(FlowNode node, User user) {
		try {
			final List<Api> apis = apiManagerService.loadAPISByFilter(node.getIdentification(), null, null,
					user.getUserId());
			String apiIdentificaction = null;
			for (final Api api : apis) {
				if (api.getIdentification().equals(node.getIdentification())
						&& api.getUser().getUserId().equals(user.getUserId())) {
					apiIdentificaction = api.getId();
					apiManagerService.removeAPI(apiIdentificaction);
				}
			}
		} catch (final Exception e) {
			log.error("Unable to remove the defined API {} while deploying.", node.getIdentification());
			throw new FlowDomainServiceException(
					"Unable to remove the defined API " + node.getIdentification() + " while deploying.");
		}
	}

	@Override
	public void deleteFlowdomain(String domainIdentification) {
		final FlowDomain domain = domainRepository.findByIdentification(domainIdentification);
		if (resourceService.isResourceSharedInAnyProject(domain)) {
			throw new OPResourceServiceException(
					"This Flow Domain is shared within a Project, revoke access from project prior to deleting");
		}
		deleteFlowDomainFlows(domainIdentification, domain.getUser());
		domainRepository.deleteByIdentification(domainIdentification);
	}

	@Override
	public FlowDomain getFlowDomainByIdentification(String identification) {
		return domainRepository.findByIdentification(identification);
	}

	@Override
	public FlowDomain createFlowDomain(String identification, User user) {

		// validate against global domains
		if (multitenancyService.getFlowDomainByIdentification(identification) != null) {
			log.debug("Flow domain {} already exist.", identification);
			throw new FlowDomainServiceException("The requested flow domain already exists in CDB");
		}

		final FlowDomain domain = new FlowDomain();
		domain.setIdentification(identification);
		domain.setActive(true);
		domain.setState(State.STOP.name());
		domain.setUser(user);
		domain.setHome(homeBase + user.getUserId());
		// Check free domain ports
		final List<Integer> usedDomainPorts = multitenancyService.getAllDomainsPorts();
		Integer selectedPort = domainPortMin;
		boolean portFound = false;
		while (selectedPort <= domainPortMax && !portFound) {
			if (!usedDomainPorts.contains(selectedPort)) {
				portFound = true;
			} else {
				selectedPort++;
			}
		}
		if (!portFound) {
			log.error("No port available found for domain = {}.", identification);
			throw new FlowDomainServiceException("No port available found for domain " + identification);
		}
		domain.setPort(selectedPort);
		// Check free service ports
		final List<Integer> usedServicePorts = multitenancyService.getAllDomainServicePorts();
		Integer selectedServicePort = servicePortMin;
		boolean servicePortFound = false;
		while (selectedServicePort <= servicePortMax && !servicePortFound) {
			if (!usedServicePorts.contains(selectedServicePort)) {
				servicePortFound = true;
			} else {
				selectedServicePort++;
			}
		}
		if (!servicePortFound) {
			log.error("No service port available found for domain = {}.", identification);
			throw new FlowDomainServiceException("No service port available found for domain " + identification);
		}
		domain.setServicePort(selectedServicePort);
		domainRepository.save(domain);
		return domain;
	}

	@Override
	public boolean flowDomainExists(FlowDomain domain) {
		return domainRepository.findByIdentification(domain.getIdentification()) != null;

	}

	@Override
	public void updateDomain(FlowDomain domain) {

		if (!flowDomainExists(domain)) {
			log.error("Domain not found for identification = {}.", domain.getIdentification());
			throw new FlowDomainServiceException("Domain " + domain.getIdentification() + " not found.");
		} else {
			domainRepository.save(domain);
		}
	}

	@Override
	public boolean domainExists(String domainIdentification) {
		return domainRepository.findByIdentification(domainIdentification) != null;
	}

	@Override
	public FlowDomain getFlowDomainById(String id) {
		return domainRepository.findById(id).orElse(null);
	}

	@Override
	public boolean hasUserManageAccess(String id, String userId) {
		final Optional<FlowDomain> opt = domainRepository.findById(id);
		if (!opt.isPresent()) {
			return false;
		}
		final FlowDomain domain = opt.get();
		if (domain.getUser().getUserId().equals(userId)) {
			return true;
		} else if (userService.getUser(userId).getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name())) {
			return true;
		} else {
			return resourceService.hasAccess(userId, id, ResourceAccessType.MANAGE);
		}
	}

	@Override
	public boolean hasUserViewAccess(String id, String userId) {

		final Optional<FlowDomain> opt = domainRepository.findById(id);
		if (!opt.isPresent()) {
			return false;
		}
		final FlowDomain domain = opt.get();
		if (domain.getUser().getUserId().equals(userId)) {
			return true;
		} else if (userService.getUser(userId).getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name())) {
			return true;
		} else {
			return resourceService.hasAccess(userId, id, ResourceAccessType.VIEW);
		}
	}

	@Override
	public List<Flow> getFlows(FlowDomain domain) {
		return flowRepository.findByFlowDomain_Identification(domain.getIdentification());
	}
}
