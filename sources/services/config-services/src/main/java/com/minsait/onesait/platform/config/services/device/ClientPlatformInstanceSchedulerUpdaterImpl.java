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
package com.minsait.onesait.platform.config.services.device;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.ClientPlatformInstance;
import com.minsait.onesait.platform.config.repository.ClientPlatformInstanceRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@ConditionalOnProperty(prefix = "onesaitplatform.iotbroker.device.update.schedule", name = "enable", havingValue = "true")
@Slf4j
public class ClientPlatformInstanceSchedulerUpdaterImpl implements ClientPlatformInstanceScheduledUpdater {

	@Autowired
	ClientPlatformInstanceRepository deviceRepository;

	private Map<String, ClientPlatformInstance> mLastUpdates;

	@PostConstruct
	public void init() {
		this.mLastUpdates = new ConcurrentHashMap<>();
	}

	@Override
	public ClientPlatformInstance updateDevice(ClientPlatformInstance device) {
		synchronized (this.mLastUpdates) {
			String key = device.getClientPlatform().getId() + device.getIdentification() + device.getSessionKey();
			this.mLastUpdates.put(key, device);
		}

		return device;
	}

	@Scheduled(fixedDelayString = "${onesaitplatform.iotbroker.device.update.schedule.delay.millis: 5000}")
	@Transactional
	public void updateDevicePhysically() {
		log.info("Update Devices in BDC");
		synchronized (this.mLastUpdates) {
			for (Map.Entry<String, ClientPlatformInstance> entry : this.mLastUpdates.entrySet()) {
				if (log.isDebugEnabled()) {
					log.debug("Update Device: " + entry.getKey());
				}
				ClientPlatformInstance device = entry.getValue();
				deviceRepository.updateClientPlatformInstance(device.getClientPlatform(), device.getIdentification(),
						device.getSessionKey(), device.getProtocol(), device.getLocation(), device.getUpdatedAt(),
						device.getStatus(), device.isConnected(), device.isDisabled(), device.getId());
			}
			this.mLastUpdates.clear();
		}
	}

}
