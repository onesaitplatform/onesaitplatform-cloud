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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.commons.flow.engine.dto.FlowEngineDomainStatus;
import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.repository.FlowDomainRepository;
import com.minsait.onesait.platform.flowengine.nodered.communication.NodeRedAdminClient;
import com.minsait.onesait.platform.flowengine.nodered.communication.dto.FlowDomainThreshold;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NodeRedDomainSyncMonitorImpl implements NodeRedDomainSyncMonitor, Runnable {

	@Value("${onesaitplatform.flowengine.sync.monitor.interval.sec:30}")
	private int monitorInterval;
	@Value("${onesaitplatform.flowengine.sync.monitor.initial.delay.sec:20}")
	private int initialSyncDelay;
	@Value("${onesaitplatform.flowengine.reboot.count.monitor.sec:1800}")
	private int rebootCountWindow;
	@Value("${onesaitplatform.flowengine.reboot.count.monitor.max:10}")
	private int maxRebootAmount;

	private final Map<String, List<DateTime>> rebootsHistory = new HashMap<>();

	@Autowired
	private FlowDomainRepository domainRepository;

	@Autowired
	private NodeRedAdminClient nodeRedAdminClient;
	@Autowired
	private MultitenancyService masterUserService;

	private ScheduledExecutorService monitor;

	@PreDestroy
	public void destroy() {
		monitor.shutdown();
	}

	@Override
	public void startMonitor() {
		monitor = Executors.newScheduledThreadPool(1);

		// Synchronize DB with the FlowEngine info on start
		final Runnable synchMF = () -> {

			try {
				nodeRedAdminClient.synchronizeMF(getAllCdbDomains());
			} catch (final Exception e) {
				log.error("Unable to sync CDB domains with NodeRedAdmin process. Cause = {}, message = {}.",
						e.getCause(), e.getMessage());
			}
		};
		monitor.schedule(synchMF, initialSyncDelay, TimeUnit.SECONDS);

		// Programa el chequeo periodico
		monitor.scheduleAtFixedRate(this, monitorInterval, monitorInterval, TimeUnit.SECONDS);
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
			final List<FlowEngineDomainStatus> domainStatusList = nodeRedAdminClient.getAllFlowEnginesDomains();
			if (domainStatusList != null) {

				for (final FlowEngineDomainStatus domainStatus : domainStatusList) {
					final FlowDomain domain = masterUserService.getAllVerticals().stream().map(v -> {
						MultitenancyContextHolder.setVerticalSchema(v.getSchema());
						return domainRepository.findByIdentification(domainStatus.getDomain());
					}).filter(Objects::nonNull).findFirst().orElse(null);
					analyzeDomainStatus(domain, domainStatus);

				}
				deleteOldRebootHistory();
			} else {
				log.error("Unable to retrieve domain's statuses.");
			}
		} catch (final Exception e) {
			log.error("Unable to retrieve domain's statuses. Cause = {}, message = {}", e.getCause(), e.getClass());
		}
	}

	private void analyzeDomainStatus(FlowDomain domain, FlowEngineDomainStatus domainStatus) {
		if (domain == null) {
			log.warn("Domain {} not found in CDB. Request for deletion will be asked to NodeRedAdminClient.",
					domainStatus.getDomain());
			nodeRedAdminClient.deleteFlowEngineDomain(domainStatus.getDomain());
		} else {
			// In case a domain has stopped (by not controlled
			// causes)
			checkThresholds(domain, domainStatus);
			if (domainStatus.getState().equals("STOP")) {
				if (Boolean.TRUE.equals(domain.getAutorecover())) {
					// autorecover domain
					addRebootTimestamp(domain);
					nodeRedAdminClient.startFlowEngineDomain(domainStatus);
				} else if (!domainStatus.getState().equals(domain.getState())) {
					domain.setState(domainStatus.getState());
				}
			}
			domainRepository.saveState(domain.getState(), domain.getId());
		}
	}

	private void checkThresholds(FlowDomain domain, FlowEngineDomainStatus domainStatus) {
		if (domainStatus.getSockets() != null && !domainStatus.getSockets().isEmpty()) {
			final Map<String, Integer> socketStatusCount = countSocketStates(domainStatus);
			final List<FlowDomainThreshold> thresholds = parseThresholds(domain.getThresholds());
			for (final FlowDomainThreshold threshold : thresholds) {
				final Integer status = socketStatusCount.get(threshold.getSocketStatus());
				if (status != null && threshold.getActive() && status > threshold.getLimit()) {
					log.warn(
							"Threshold {} limit has been reached: ActualStatus:{}, Limit:{} . The domain will be stopped.",
							threshold.getSocketStatus(), status, threshold.getLimit());
					// STOP FLOWDOMAIN
					domain.setState("STOP");
					nodeRedAdminClient.stopFlowEngineDomain(domain.getIdentification());
					domainRepository.saveState(domain.getState(), domain.getId());
				}
			}
		}
	}

	private Map<String, Integer> countSocketStates(FlowEngineDomainStatus domainStatus) {
		final Map<String, Integer> socketStatusCount = new HashMap<>();
		socketStatusCount.put("SOCKET COUNT", domainStatus.getSockets().size());
		for (final String line : domainStatus.getSockets()) {
			final List<String> elements = Arrays.asList(line.split(" "));
			if (!elements.isEmpty()) {
				final String socketStatusType = elements.get(elements.size() - 1).replace("(", "").replace(")", "")
						.trim();
				Integer numSocketStateType = socketStatusCount.get(socketStatusType);
				if (numSocketStateType == null) {
					numSocketStateType = 0;
				}
				socketStatusCount.put(socketStatusType, numSocketStateType + 1);
			}
		}
		return socketStatusCount;
	}

	private List<FlowDomainThreshold> parseThresholds(String data) {
		List<FlowDomainThreshold> thresholds = new ArrayList<>();
		try {
			thresholds = new ObjectMapper().readValue(data, new TypeReference<List<FlowDomainThreshold>>() {
			});
		} catch (final IOException e) {
			log.error("Error precessing ConfigDB thresholds. Current values will be ignored.");
		} catch (final NullPointerException e) {
			log.debug("No threshold defined.");
		}
		return thresholds;
	}

	private void addRebootTimestamp(FlowDomain domain) {
		List<DateTime> datetimes;
		if (rebootsHistory.get(domain.getIdentification()) == null) {
			datetimes = new ArrayList<>();
		} else {
			datetimes = rebootsHistory.get(domain.getIdentification());
		}

		datetimes.add(DateTime.now());
		rebootsHistory.put(domain.getIdentification(), datetimes);

		if (datetimes.size() >= maxRebootAmount) {
			domainRepository.saveAutorecover(false, domain.getId());
		}
	}

	private void deleteOldRebootHistory() {
		for (final Entry<String, List<DateTime>> entry : rebootsHistory.entrySet()) {
			final List<DateTime> history = entry.getValue();
			for (final DateTime datetime : history) {
				if (Seconds.secondsBetween(datetime, DateTime.now()).getSeconds() > rebootCountWindow) {
					history.remove(datetime);
				}
			}
		}
	}

	private List<FlowEngineDomainStatus> getAllCdbDomains() {
		final List<FlowEngineDomainStatus> domainsToSync = new ArrayList<>();
		masterUserService.getAllVerticals().forEach(v -> {
			MultitenancyContextHolder.setVerticalSchema(v.getSchema());
			final List<FlowDomain> domainList = domainRepository.findAll();
			for (final FlowDomain domain : domainList) {
				final FlowEngineDomainStatus domainStatus = new FlowEngineDomainStatus();
				domainStatus.setDomain(domain.getIdentification());
				domainStatus.setHome(domain.getHome());
				domainStatus.setPort(domain.getPort());
				domainStatus.setServicePort(domain.getServicePort());
				domainStatus.setState(domain.getState());
				domainsToSync.add(domainStatus);
			}
			MultitenancyContextHolder.clear();
		});
		return domainsToSync;
	}
}
