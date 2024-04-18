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
package com.minsait.onesait.platform.iotbroker.plugable.impl.security.reference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.IoTSession;
import com.minsait.onesait.platform.config.repository.IoTSessionRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@ConditionalOnProperty(prefix = "onesaitplatform.iotbroker.session.update.schedule", name = "enable", havingValue = "true")
@Slf4j
public class SessionSchedulerUpdaterImpl implements SessionSchedulerUpdater {

	@Autowired
	IoTSessionRepository ioTSessionRepository;

	private Map<String, IoTSession> mLastUpdates;
	private List<String> removed;

	@PostConstruct
	public void init() {
		this.mLastUpdates = new ConcurrentHashMap<>();
		this.removed = new ArrayList<>();
	}

	@Override
	public void saveSession(String sessionkey, IoTSession session) {
		synchronized (this.mLastUpdates) {
			this.mLastUpdates.put(sessionkey, session);
		}
	}

	@Override
	public void notifyDeleteSession(String sessionkey) {
		synchronized (this.mLastUpdates) {
			this.mLastUpdates.remove(sessionkey);
			this.removed.add(sessionkey);
		}
	}

	@Scheduled(fixedDelayString = "${onesaitplatform.iotbroker.device.update.schedule.delay.millis: 5000}")
	@Transactional
	public void updateSessionPhysically() {
		log.info("Update Sessions in ConfigDB");
		synchronized (this.mLastUpdates) {
			for (Map.Entry<String, IoTSession> entry : this.mLastUpdates.entrySet()) {
				IoTSession session = entry.getValue();
				if (!this.removed.contains(entry.getKey())) {// Due to concurrent conditions can happen
					if (log.isDebugEnabled()) {
						log.debug("Save Session: {} " ,entry.getKey());
					}
					ioTSessionRepository.save(session);
				}
			}
			this.mLastUpdates.clear();
			this.removed.clear();
		}
	}

}
