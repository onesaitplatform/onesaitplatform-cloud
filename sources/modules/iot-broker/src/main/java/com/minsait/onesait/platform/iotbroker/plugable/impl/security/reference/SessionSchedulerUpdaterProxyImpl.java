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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minsait.onesait.platform.multitenant.config.model.IoTSession;
import com.minsait.onesait.platform.multitenant.config.repository.IoTSessionRepository;

@Service
public class SessionSchedulerUpdaterProxyImpl implements SessionSchedulerUpdaterProxy {

	@Autowired
	IoTSessionRepository ioTSessionRepository;
	
	@CachePut(cacheNames = IoTSessionRepository.SESSIONS_REPOSITORY, key = "#p0.sessionKey", unless = "#result == null")
	@Transactional
	public IoTSession updateSession(IoTSession session) {
		int updated = ioTSessionRepository.updateSession(session.getId(), session.getSessionKey(), session.getClientPlatform(), 
				session.getClientPlatformID(), session.getDevice(), session.getToken(), session.getUserID(), 
				session.getUserName(), session.getExpiration(), session.getLastAccess(), session.getUpdatedAt());
		return updated > 0 ? session : null;
	}

}
