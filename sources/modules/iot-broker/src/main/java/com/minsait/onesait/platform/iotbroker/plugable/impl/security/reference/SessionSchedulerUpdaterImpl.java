/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2022 SPAIN
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
package com.minsait.onesait.platform.iotbroker.plugable.impl.security.reference;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.multitenant.config.model.IoTSession;

import lombok.extern.slf4j.Slf4j;

@Service
@ConditionalOnProperty(prefix = "onesaitplatform.iotbroker.session.update.schedule", name = "enable", havingValue = "true")
@Slf4j
public class SessionSchedulerUpdaterImpl implements SessionSchedulerUpdater {
	
	//to call internal transactional methods
	@Autowired
	SessionSchedulerUpdaterProxy proxy;

	private Map<String, IoTSession> mLastUpdates;

	@PostConstruct
	public void init() {
		this.mLastUpdates = new ConcurrentHashMap<>();
	}

	@Override
	public void saveSession(IoTSession session) {
		this.mLastUpdates.put(session.getSessionKey(), session);
	}

	@Scheduled(fixedDelayString = "${onesaitplatform.iotbroker.device.update.schedule.delay.millis: 30000}")
	public void updateSessionPhysically() {
		log.debug("Update Sessions in ConfigDB");
		
		mLastUpdates.forEach((key, value) ->{
			//Using remove to get and remove the value in one operation.
			//Further puts of the same key in the map will not be processed by the loop but it will be processes on the next iteration of the scheduler.
			IoTSession session = mLastUpdates.remove(key);
			if (session != null) {
				if (log.isDebugEnabled()) {
					log.debug("Save Session: {} ", key);
				}				
				proxy.updateSession(session);				
			}
		});
	}
	


}
