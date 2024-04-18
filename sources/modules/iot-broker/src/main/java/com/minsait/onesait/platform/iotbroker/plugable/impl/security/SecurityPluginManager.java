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
package com.minsait.onesait.platform.iotbroker.plugable.impl.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageTypes;
import com.minsait.onesait.platform.iotbroker.plugable.interfaces.security.SecurityPlugin;
import com.minsait.onesait.platform.multitenant.config.model.IoTSession;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SecurityPluginManager implements SecurityPlugin {

	@Autowired
	private List<SecurityPlugin> plugins;

	@Override
	public Optional<IoTSession> authenticate(String token, String clientPlatform, String clientPlatformInstance,
			String sessionKey) {
		final List<IoTSession> sessions = new ArrayList<>();
		
		for (final SecurityPlugin p : plugins) {
			p.authenticate(token, clientPlatform, clientPlatformInstance, sessionKey).ifPresent(sessions::add);
		}

		if (!sessions.isEmpty()) {
			log.info( "{}:{} authenticated",clientPlatform,clientPlatformInstance);
			return Optional.of(sessions.get(0));
		}
		log.info( "{}:{} not authenticated",clientPlatform,clientPlatformInstance);
		return Optional.empty();
	}

	@Override
	public boolean closeSession(String sessionKey) {
		boolean ret = false;
		for (final SecurityPlugin p : plugins) {
			ret |= p.closeSession(sessionKey);
		}

		return ret;

	}

	@Override
	public boolean checkSessionKeyActive(Optional<IoTSession> session) {
		boolean ret = false;
		for (final SecurityPlugin p : plugins) {
			ret |= p.checkSessionKeyActive(session);
		}
		return ret;

	}

	@Override
	public boolean checkAuthorization(SSAPMessageTypes messageType, String ontology, Optional<IoTSession> session) {
		boolean ret = false;
		for (final SecurityPlugin p : plugins) {
			ret |= p.checkAuthorization(messageType, ontology, session);
		}
		return ret;

	}

	@Override
	public Optional<IoTSession> getSession(String sessionKey) {

		final List<IoTSession> ks = new ArrayList<>();
		for (final SecurityPlugin p : plugins) {
			p.getSession(sessionKey).ifPresent(ks::add);
		}

		if (!ks.isEmpty()) {
			return Optional.of(ks.get(0));
		} else {
			return Optional.empty();
		}
	}

}
