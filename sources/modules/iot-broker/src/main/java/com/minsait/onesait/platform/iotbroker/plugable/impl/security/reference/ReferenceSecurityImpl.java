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

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.hazelcast.core.HazelcastInstance;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageTypes;
import com.minsait.onesait.platform.config.dto.ClientPlatformTokenDTO;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.token.TokenService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.iotbroker.common.MessageException;
import com.minsait.onesait.platform.iotbroker.common.exception.AuthenticationException;
import com.minsait.onesait.platform.iotbroker.plugable.interfaces.security.SecurityPlugin;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.config.model.IoTSession;
import com.minsait.onesait.platform.multitenant.config.model.MasterDeviceToken;
import com.minsait.onesait.platform.multitenant.config.repository.IoTSessionRepository;
import com.minsait.onesait.platform.multitenant.config.repository.MasterDeviceTokenRepository;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableScheduling
@Component
public class ReferenceSecurityImpl implements SecurityPlugin {

	@Autowired
	TokenService tokenService;
	@Autowired
	UserService userService;
	@Autowired
	OntologyService ontologyService;
	@Autowired
	IoTSessionRepository ioTSessionRepository;
	@Autowired
	IntegrationResourcesService resourcesService;
	@Autowired(required = false)
	SessionSchedulerUpdater sessionSchedulerUpdater;
	@Autowired
	MasterDeviceTokenRepository masterDeviceTokenRepository;

	@Autowired
	HazelcastInstance hazelcastInstance;

	ConcurrentHashMap<String, IoTSession> sessionList = new ConcurrentHashMap<>(200);

	@Override
	public Optional<IoTSession> authenticate(String token, String clientPlatform, String clientPlatformInstance,
			String sessionKey) {
		final Optional<MasterDeviceToken> masterToken = Optional
				.ofNullable(masterDeviceTokenRepository.findByTokenName(token));

		masterToken.ifPresent(mt -> {
			MultitenancyContextHolder.setVerticalSchema(mt.getVerticalSchema());
			MultitenancyContextHolder.setTenantName(mt.getTenant());
		});
		ClientPlatformTokenDTO cpToken = tokenService.getClientPlatformIdByTokenName(token);

		if (!masterToken.isPresent() || cpToken == null) {
			log.info("Impossible to retrieve Token with token: {}. The token does not exist or is not active.", token);
			return Optional.empty();
		} else if (!cpToken.isTokenActive()) {
			log.info("Token inactive: {}", token);
			return Optional.empty();
		}
		
		if (clientPlatform.equals(cpToken.getClientPlatformIdentification())) {
			final IoTSession session = new IoTSession();
			session.setClientPlatform(clientPlatform);
			session.setDevice(clientPlatformInstance);

			session.setExpiration(
					(Long) resourcesService.getGlobalConfiguration().getEnv().getIotbroker().get("session-expiration"));
			session.setLastAccess(ZonedDateTime.now());
			// TO-DO check cases where there is no master token present, create one?
			session.setToken(masterToken.orElse(null));
			session.setClientPlatformID(cpToken.getClientPlatformId());

			session.setUserID(cpToken.getUserId());
			session.setUserName(cpToken.getUserName());

			if (!StringUtils.isEmpty(sessionKey)) {
				getSession(sessionKey).ifPresent(s -> {
					if (s.getClientPlatformID().equals(cpToken.getClientPlatformId())) {
						closeSession(sessionKey);
						session.setSessionKey(sessionKey);
					} else
						throw new AuthenticationException(MessageException.ERR_SESSIONKEY_NOT_OWNER);
				});
				session.setSessionKey(sessionKey);

			} else {
				session.setSessionKey(UUID.randomUUID().toString());
			}

			IoTSession newSession = ioTSessionRepository.save(session);

			return Optional.of(newSession);
		}
		log.info("Impossible to retrieve ClientPlatform from identification: {}", clientPlatform);
		return Optional.empty();

	}

	@Override
	public boolean closeSession(String sessionKey) {
		ioTSessionRepository.deleteBySessionKey(sessionKey);
		return true;
	}

	@Override
	public boolean checkSessionKeyActive(Optional<IoTSession> s) {
		if (!s.isPresent()) {
			return false;
		}		
		
		IoTSession session = s.get();

		final ZonedDateTime now = ZonedDateTime.now();
		final ZonedDateTime lastAccess = session.getLastAccess();

		final long time = ChronoUnit.MILLIS.between(lastAccess, now);

		if (time > session.getExpiration()) {
			ioTSessionRepository.delete(session);
			return false;
		} else {
			// renew session on activity
			session.setLastAccess(now);
			session.setUpdatedAt(Date.from(now.toInstant()));
			sessionSchedulerUpdater.saveSession(session);
		}

		return true;

	}

	@Override
	public boolean checkAuthorization(SSAPMessageTypes messageType, String ontology, Optional<IoTSession> s) {

		if (!s.isPresent()) {
			return false;
		}
		
		IoTSession session = s.get();

		if (session != null && session.getToken() != null) {
			MultitenancyContextHolder.setVerticalSchema(session.getToken().getVerticalSchema());
			MultitenancyContextHolder.setTenantName(session.getToken().getTenant());
		}

		boolean clientHasAuthority = false;
		try {
			if (SSAPMessageTypes.INSERT.equals(messageType) || SSAPMessageTypes.UPDATE.equals(messageType)
					|| SSAPMessageTypes.UPDATE_BY_ID.equals(messageType) || SSAPMessageTypes.DELETE.equals(messageType)
					|| SSAPMessageTypes.DELETE_BY_ID.equals(messageType)) {

				clientHasAuthority = ontologyService.hasClientPlatformPermisionForInsert(session.getClientPlatform(),
						ontology) && ontologyService.hasUserPermissionForInsert(session.getUserID(), ontology);

			} else if (SSAPMessageTypes.QUERY.equals(messageType) || SSAPMessageTypes.SUBSCRIBE.equals(messageType)) {

				clientHasAuthority = ontologyService.hasClientPlatformPermisionForQuery(session.getClientPlatform(),
						ontology) && ontologyService.hasUserPermissionForQuery(session.getUserID(), ontology);
			}
		} catch (final Exception e) {
			log.error("Error validating operation permissions:" + e.getMessage());
			return false;
		}

		return clientHasAuthority;
	}

	@Override
	public Optional<IoTSession> getSession(String sessionKey) {
		if (StringUtils.isEmpty(sessionKey)) {
			return Optional.empty();
		}
		final IoTSession session = ioTSessionRepository.findBySessionKey(sessionKey);
		if (session == null) {
			return Optional.empty();
		} else {
			MultitenancyContextHolder.setVerticalSchema(session.getToken().getVerticalSchema());
			MultitenancyContextHolder.setTenantName(session.getToken().getTenant());
			ClientPlatformTokenDTO cpToken = tokenService.getClientPlatformIdByTokenName(session.getToken().getTokenName());
			if (cpToken == null || !cpToken.isTokenActive()) {
				closeSession(sessionKey);
				return Optional.empty();
			}
		}

		return Optional.of(session);

	}

	@PostConstruct
	private void invalidateSessionsOnInit() {
		synchronizeSessions();
		invalidateExpiredSessions();
	}

	@Scheduled(fixedDelay = 60000)
	public void invalidateExpiredSessions() {
		final long now = System.currentTimeMillis();
		final Collection<Object> sessions = getIotSessionsFromCache();
		sessions.forEach(s -> {
			IoTSession session = (IoTSession) s;
			if (now - session.getUpdatedAt().getTime() >= session.getExpiration()) {
				ioTSessionRepository.delete(session);
			}
		});
		log.info("Deleting expired session");
	}
	
	@Scheduled(fixedDelay = 86400000) //1 day
	public void invalidateExpiredSessionsFromDB() {
		final long now = System.currentTimeMillis();
		List<IoTSession> findAll = ioTSessionRepository.findAll();
		findAll.forEach(s -> {
			if (now - s.getUpdatedAt().getTime() >= s.getExpiration()) {
				ioTSessionRepository.delete(s);
			}
		});
	}

	private Collection<Object> getIotSessionsFromCache() {
		Collection<Object> values = hazelcastInstance.getMap(IoTSessionRepository.SESSIONS_REPOSITORY).values();
		return values;
		
	}

	private void synchronizeSessions() {
		ioTSessionRepository.findAll().forEach(
				s -> hazelcastInstance.getMap(IoTSessionRepository.SESSIONS_REPOSITORY).put(s.getSessionKey(), s));

	}

}
