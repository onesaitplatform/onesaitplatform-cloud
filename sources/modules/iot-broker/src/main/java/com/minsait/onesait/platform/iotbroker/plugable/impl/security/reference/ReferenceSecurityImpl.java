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
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.hazelcast.core.HazelcastInstance;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageTypes;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.Token;
import com.minsait.onesait.platform.config.services.client.ClientPlatformService;
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
	ClientPlatformService clientPlatformService;
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
		final Token retrivedToken = tokenService.getTokenByToken(token);
		if (!masterToken.isPresent() || retrivedToken == null) {
			log.info("Impossible to retrieve Token with token: {}", token);
			return Optional.empty();
		} else if (!retrivedToken.isActive()) {
			log.info("Token inactive: {}", token);
			return Optional.empty();
		}

		final ClientPlatform clientPlatformDB = retrivedToken.getClientPlatform();
		if (clientPlatform.equals(clientPlatformDB.getIdentification())) {
			final IoTSession session = new IoTSession();
			session.setClientPlatform(clientPlatform);
			session.setDevice(clientPlatformInstance);

			session.setExpiration(
					(Long) resourcesService.getGlobalConfiguration().getEnv().getIotbroker().get("session-expiration"));
			session.setLastAccess(ZonedDateTime.now());
			// TO-DO check cases where there is no master token presente, create one?
			session.setToken(masterToken.orElse(null));
			session.setClientPlatformID(clientPlatformDB.getId());

			session.setUserID(retrivedToken.getClientPlatform().getUser().getUserId());
			session.setUserName(retrivedToken.getClientPlatform().getUser().getFullName());

			if (!StringUtils.isEmpty(sessionKey)) {
				getSession(sessionKey).ifPresent(s -> {
					if (s.getClientPlatformID().equals(clientPlatformDB.getId())) {
						closeSession(sessionKey);
						session.setSessionKey(sessionKey);
					} else
						throw new AuthenticationException(MessageException.ERR_SESSIONKEY_NOT_OWNER);
				});
				session.setSessionKey(sessionKey);

			} else {
				session.setSessionKey(UUID.randomUUID().toString());
			}

			ioTSessionRepository.save(session);

			return Optional.of(session);
		}
		log.info("Impossible to retrieve ClientPlatform from identification: {}", clientPlatform);
		return Optional.empty();

	}

	@Override
	public boolean closeSession(String sessionKey) {
		ioTSessionRepository.deleteBySessionKey(sessionKey);
		sessionSchedulerUpdater.notifyDeleteSession(sessionKey);
		return true;
	}

	@Override
	public boolean checkSessionKeyActive(String sessionKey) {
		final IoTSession session = ioTSessionRepository.findBySessionKey(sessionKey);
		if (session == null) {
			return false;
		}

		final ZonedDateTime now = ZonedDateTime.now();
		final ZonedDateTime lastAccess = session.getLastAccess();

		final long time = ChronoUnit.MILLIS.between(now, lastAccess);

		if (time > session.getExpiration()) {
			ioTSessionRepository.delete(ioTSessionRepository.findBySessionKey(sessionKey));
			return false;
		} else {
			// renew session on activity
			session.setLastAccess(now);
			// sessionList.put(sessionKey, session);
			// ioTSessionRepository.save(session);
			sessionSchedulerUpdater.saveSession(sessionKey, session);
		}

		return true;

	}

	@Override
	public boolean checkAuthorization(SSAPMessageTypes messageType, String ontology, String sessionKey) {

		if (!checkSessionKeyActive(sessionKey)) {
			return false;
		}

		final IoTSession session = ioTSessionRepository.findBySessionKey(sessionKey);
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
			final Token token = tokenService.getTokenByToken(session.getToken().getTokenName());
			if (token == null || !token.isActive()) {
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
		final List<IoTSession> sessions = getIotSessionsFromCache();
		for (final IoTSession s : sessions) {
			if (now - s.getLastAccess().toInstant().toEpochMilli() >= s.getExpiration())
				ioTSessionRepository.delete(s);
		}
		log.info("Deleting expired session");
	}

	private List<IoTSession> getIotSessionsFromCache() {
		return hazelcastInstance.getMap(IoTSessionRepository.SESSIONS_REPOSITORY).entrySet().stream()
				.map(e -> (IoTSession) e.getValue()).collect(Collectors.toList());
	}

	private void synchronizeSessions() {
		ioTSessionRepository.findAll().forEach(
				s -> hazelcastInstance.getMap(IoTSessionRepository.SESSIONS_REPOSITORY).put(s.getSessionKey(), s));

	}

}
