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

import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.ClientPlatformInstance;

import lombok.extern.slf4j.Slf4j;

@Service
@ConditionalOnProperty(prefix = "onesaitplatform.iotbroker.device.update.schedule", name = "enable", havingValue = "true")
@Slf4j
public class ClientPlatformInstanceSchedulerUpdaterImpl implements ClientPlatformInstanceScheduledUpdater {
	
	@Autowired
	ClientPlatformInstanceService cpiService;
	
	private ConcurrentHashMap<String, Pair<String,ClientPlatformInstance>> mLastUpdates;
	

	

	@PostConstruct
	public void init() {
		this.mLastUpdates = new ConcurrentHashMap<>();
	}

	@Override
	public ClientPlatformInstance saveDevice(ClientPlatformInstance device, String cpIdentification) {
		String key = String.join("-", cpIdentification, device.getIdentification());
		this.mLastUpdates.put(key, Pair.of(cpIdentification, device));
		return device;
	}
	

	@Scheduled(fixedDelayString = "${onesaitplatform.iotbroker.device.update.schedule.delay.millis: 30000}")
	public void updateDevicePhysically() {
		log.debug("Update Devices in BDC");
		
		mLastUpdates.forEach((key, value) -> {
			
			//Using remove to get and remove the value in one operation.
			//Further puts in the map will not be processed by the loop but they will be processes on the next iteration of the scheduler.
			Pair<String, ClientPlatformInstance> data = mLastUpdates.remove(key);
			if (data != null) {
				cpiService.createOrUpdateClientPlatformInstance(data.getRight(), data.getLeft());
			}		    
		});
	}
}
