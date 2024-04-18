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
package com.minsait.onesait.platform.flowengine.nodered.sync;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.commons.flow.engine.dto.FlowEngineDomainStatus;
import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.repository.FlowDomainRepository;
import com.minsait.onesait.platform.flowengine.nodered.communication.NodeRedAdminClient;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NodeRedDomainSyncMonitorImpl implements NodeRedDomainSyncMonitor, Runnable {

	@Value("${onesaitplatform.flowengine.sync.monitor.interval.sec:30}")
	private int monitorInterval;
	@Value("${onesaitplatform.flowengine.sync.monitor.initial.delay.sec:20}")
	private int initialSyncDelay;

	@Autowired
	private FlowDomainRepository domainRepository;

	@Autowired
	private NodeRedAdminClient nodeRedAdminClient;

	private ScheduledExecutorService monitor;

	@PreDestroy
	public void destroy() {
		this.monitor.shutdown();
	}

	@Override
	public void startMonitor() {
		monitor = Executors.newScheduledThreadPool(1);

		// Synchronize DB with the FlowEngine info on start 
		Runnable synchMF = () -> {
			try {
				nodeRedAdminClient.synchronizeMF(getAllCdbDomains());
			} catch (Exception e) {
				log.error("Unable to sync CDB domains with NodeRedAdmin process. Cause = {}, message = {}.",
						e.getCause(), e.getMessage());
			}
		};
		monitor.schedule(synchMF, this.initialSyncDelay, TimeUnit.SECONDS);
	

		// Programa el chequeo periodico
		monitor.scheduleAtFixedRate(this, this.monitorInterval, this.monitorInterval, TimeUnit.SECONDS);
	}

	@Override
	public void stopMonitor() {
		if (null != monitor) {
			monitor.shutdown();
		}
	}

	@Override
	public void run() {

		try {
			List<FlowEngineDomainStatus> domainStatusList = nodeRedAdminClient.getAllFlowEnginesDomains();
			if (domainStatusList != null) {
				for (FlowEngineDomainStatus domainStatus : domainStatusList) {
					FlowDomain domain = domainRepository.findByIdentification(domainStatus.getDomain());
					if (domain == null) {
						log.warn(
								"Domain {} not found in CDB. Request for deletion will be asked to NodeRedAdminClient.",
								domainStatus.getDomain());
						nodeRedAdminClient.deleteFlowEngineDomain(domainStatus.getDomain());
					} else {
						//In case a domain has stoped (by not controlled causes)
						if(domainStatus.getState().equals("STOP") && !domainStatus.getState().equals(domain.getState())){
							domain.setState(domainStatus.getState());
							domainRepository.save(domain);
						}
					}
				}
			} else {
				log.error("Unable to retrieve domain's statuses.");
			}
		} catch (Exception e) {
			log.error("Unable to retrieve domain's statuses. Cause = {}, message = {}", e.getCause(), e.getClass());
		}
	}

	private List<FlowEngineDomainStatus> getAllCdbDomains() {
		List<FlowEngineDomainStatus> domainsToSync = new ArrayList<>();
		List<FlowDomain> domainList = domainRepository.findAll();
		for (FlowDomain domain : domainList) {
			FlowEngineDomainStatus domainStatus = new FlowEngineDomainStatus();
			domainStatus.setDomain(domain.getIdentification());
			domainStatus.setHome(domain.getHome());
			domainStatus.setPort(domain.getPort());
			domainStatus.setServicePort(domain.getServicePort());
			domainStatus.setState(domain.getState());
			domainsToSync.add(domainStatus);
		}
		return domainsToSync;
	}
}
