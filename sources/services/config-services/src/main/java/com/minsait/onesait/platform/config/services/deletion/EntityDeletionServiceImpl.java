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
package com.minsait.onesait.platform.config.services.deletion;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.ClientConnection;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformInstance;
import com.minsait.onesait.platform.config.model.ClientPlatformInstanceSimulation;
import com.minsait.onesait.platform.config.model.ClientPlatformOntology;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.model.GadgetMeasure;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.OntologyKPI;
import com.minsait.onesait.platform.config.model.OntologyTimeSeries;
import com.minsait.onesait.platform.config.model.Token;
import com.minsait.onesait.platform.config.model.TwitterListening;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.repository.ApiRepository;
import com.minsait.onesait.platform.config.repository.ClientConnectionRepository;
import com.minsait.onesait.platform.config.repository.ClientPlatformInstanceRepository;
import com.minsait.onesait.platform.config.repository.ClientPlatformInstanceSimulationRepository;
import com.minsait.onesait.platform.config.repository.ClientPlatformOntologyRepository;
import com.minsait.onesait.platform.config.repository.ClientPlatformRepository;
import com.minsait.onesait.platform.config.repository.GadgetDatasourceRepository;
import com.minsait.onesait.platform.config.repository.GadgetMeasureRepository;
import com.minsait.onesait.platform.config.repository.GadgetRepository;
import com.minsait.onesait.platform.config.repository.OPResourceRepository;
import com.minsait.onesait.platform.config.repository.OntologyKPIRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.OntologyRestHeadersRepository;
import com.minsait.onesait.platform.config.repository.OntologyRestRepository;
import com.minsait.onesait.platform.config.repository.OntologyRestSecurityRepository;
import com.minsait.onesait.platform.config.repository.OntologyTimeSeriesRepository;
import com.minsait.onesait.platform.config.repository.OntologyUserAccessRepository;
import com.minsait.onesait.platform.config.repository.QueryTemplateRepository;
import com.minsait.onesait.platform.config.repository.TokenRepository;
import com.minsait.onesait.platform.config.repository.TwitterListeningRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.repository.UserTokenRepository;
import com.minsait.onesait.platform.config.services.exceptions.OPResourceServiceException;
import com.minsait.onesait.platform.config.services.exceptions.OntologyServiceException;
import com.minsait.onesait.platform.config.services.exceptions.QueryTemplateServiceException;
import com.minsait.onesait.platform.config.services.exceptions.UserServiceException;
import com.minsait.onesait.platform.config.services.gadget.GadgetDatasourceService;
import com.minsait.onesait.platform.config.services.kafka.KafkaAuthorizationService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.project.ProjectService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.multitenant.config.model.OAuthAccessToken;
import com.minsait.onesait.platform.multitenant.config.repository.OAuthAccessTokenRepository;
import com.minsait.onesait.platform.multitenant.config.repository.OAuthRefreshTokenRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EntityDeletionServiceImpl implements EntityDeletionService {

	@Autowired
	private ApiRepository apiRepository;
	@Autowired
	private OntologyRepository ontologyRepository;
	@Autowired
	private OntologyUserAccessRepository ontologyUserAccessRepository;
	@Autowired
	private ClientPlatformOntologyRepository clientPlatformOntologyRepository;
	@Autowired
	private TwitterListeningRepository twitterListeningRepository;
	@Autowired
	private ClientPlatformInstanceSimulationRepository deviceSimulationRepository;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private UserTokenRepository userTokenRepository;
	@Autowired
	private OAuthAccessTokenRepository oauthAccessTokenRepository;
	@Autowired
	private OAuthRefreshTokenRepository oauthRefreshTokenRepository;
	@Autowired
	private ClientConnectionRepository clientConnectionRepository;
	@Autowired
	private ClientPlatformRepository clientPlatformRepository;
	@Autowired
	private OntologyRestRepository ontologyRestRepository;
	@Autowired
	private OntologyRestSecurityRepository ontologyRestSecurityRepository;
	@Autowired
	private OntologyRestHeadersRepository ontologyRestHeaderRepository;
	@Autowired
	private ClientPlatformInstanceRepository deviceRepository;
	@Autowired
	private OPResourceRepository resourceRepository;
	@Autowired
	private OPResourceService resourceService;
	@Autowired
	private GadgetDatasourceRepository gadgetDatasourceRepository;
	@Autowired
	private GadgetDatasourceService gadgetDatasourceService;
	@Autowired
	private GadgetMeasureRepository gadgetMeasureRepository;
	@Autowired
	private GadgetRepository gadgetRepository;
	@Autowired
	private OntologyTimeSeriesRepository ontologyTimeSeriesRepository;
	@Autowired
	private TokenRepository tokenRepository;
	@Autowired
	private ProjectService projectService;

	@Autowired
	private QueryTemplateRepository queryTemplateRepository;
	@Autowired
	private KafkaAuthorizationService kafkaAuthorizationService;

	@Autowired
	private OntologyKPIRepository kpiRepository;

	@Override
	public void deleteOntology(String id, String userId) {

		try {
			final User user = userService.getUser(userId);
			final Ontology ontology = ontologyService.getOntologyById(id, userId);
			if (resourceService.isResourceSharedInAnyProject(ontology))
				throw new OPResourceServiceException(
						"This Ontology is shared within a Project, revoke access from project prior to deleting");
			if (ontologyService.hasUserPermisionForChangeOntology(user, ontology)) {
				if (clientPlatformOntologyRepository.findByOntology(ontology) != null) {
					clientPlatformOntologyRepository.findByOntology(ontology).forEach(cpo -> {
						final ClientPlatform client = cpo.getClientPlatform();
						client.getClientPlatformOntologies().removeIf(r -> r.getOntology().equals(ontology));
						clientPlatformOntologyRepository.deleteById(cpo.getId());
						kafkaAuthorizationService.removeAclToOntologyClient(cpo);
					});

				}

				if (!apiRepository.findByOntology(ontology).isEmpty()) {
					apiRepository.findByOntology(ontology).forEach(a -> {
						projectService.deleteResourceFromProjects(a.getId());
						apiRepository.delete(a);
					});
				}
				if (ontologyUserAccessRepository.findByOntology(ontology) != null) {
					ontologyUserAccessRepository.deleteByOntology(ontology);
				}
				if (twitterListeningRepository.findByOntology(ontology) != null) {
					twitterListeningRepository.deleteByOntology(ontology);
				}
				if (ontologyUserAccessRepository.findByOntology(ontology) != null) {
					ontologyUserAccessRepository.deleteByOntology(ontology);
				}
				if (!twitterListeningRepository.findByOntology(ontology).isEmpty()) {
					twitterListeningRepository.deleteByOntology(ontology);
				}
				if (!deviceSimulationRepository.findByOntology(ontology).isEmpty()) {
					deviceSimulationRepository.deleteByOntology(ontology);
				}

				if (ontologyRestRepository.findByOntologyId(ontology) != null) {
					ontologyRestHeaderRepository
							.delete(ontologyRestRepository.findByOntologyId(ontology).getHeaderId());
				}
				if (ontologyRestRepository.findByOntologyId(ontology) != null) {
					ontologyRestSecurityRepository
							.delete(ontologyRestRepository.findByOntologyId(ontology).getSecurityId());
				}

				gadgetDatasourceRepository.findByOntology(ontology).forEach(g -> {
					deleteGadgetDataSource(g.getId(), userId);

				});

				List<OntologyKPI> kpi = kpiRepository.findByOntology(ontology);

				if (!kpi.isEmpty()) {
					kpiRepository.delete(kpi);
				}

				List<OntologyTimeSeries> timeSeries = ontologyTimeSeriesRepository.findByOntology(ontology);

				if (!timeSeries.isEmpty()) {
					ontologyTimeSeriesRepository.delete(timeSeries);
					final Ontology stats = ontologyRepository
							.findByIdentification(timeSeries.get(0).getOntology().getIdentification() + "_stats");
					if (stats != null) {
						ontologyRepository.deleteById(stats.getId());
					}
				}

				ontologyRepository.deleteById(id);

			} else {
				throw new OntologyServiceException("You dont have rights to delete ontology");
			}
		} catch (final Exception e) {
			throw e;
		}

	}

	@Override
	@Transactional
	public void deleteTwitterListening(TwitterListening twitterListening) {
		twitterListeningRepository.deleteById(twitterListening.getId());
	}

	@Override
	@Transactional
	public void deleteClient(String id) {
		try {

			final ClientPlatform client = clientPlatformRepository.findById(id);
			final List<ClientPlatformOntology> cpf = clientPlatformOntologyRepository.findByClientPlatform(client);
			if (cpf != null && cpf.size() > 0) {
				for (final Iterator<ClientPlatformOntology> iterator = cpf.iterator(); iterator.hasNext();) {
					final ClientPlatformOntology clientPlatformOntology = (ClientPlatformOntology) iterator.next();
					clientPlatformOntologyRepository.delete(clientPlatformOntology);
					kafkaAuthorizationService.removeAclToOntologyClient(clientPlatformOntology);
				}

			}
			final List<ClientConnection> cc = clientConnectionRepository.findByClientPlatform(client);
			if (cc != null && cc.size() > 0) {
				for (final Iterator<ClientConnection> iterator = cc.iterator(); iterator.hasNext();) {
					final ClientConnection clientConnection = iterator.next();
					clientConnectionRepository.delete(clientConnection);
				}
			}

			final List<ClientPlatformInstance> ld = deviceRepository.findByClientPlatform(client);
			if (ld != null && ld.size() > 0) {
				for (final Iterator<ClientPlatformInstance> iterator = ld.iterator(); iterator.hasNext();) {
					final ClientPlatformInstance device = iterator.next();
					deviceRepository.delete(device);
				}
			}
			final List<ClientPlatformInstanceSimulation> lds = deviceSimulationRepository.findByClientPlatform(client);
			if (lds != null && lds.size() > 0) {
				for (final Iterator<ClientPlatformInstanceSimulation> iterator = lds.iterator(); iterator.hasNext();) {
					final ClientPlatformInstanceSimulation deviceSim = iterator.next();
					deviceSimulationRepository.delete(deviceSim);
				}
			}
			clientPlatformRepository.delete(client);
			final Ontology ontoLog = ontologyRepository.findByIdentification("LOG_" + client.getIdentification());
			if (ontoLog != null) {
				deleteOntology(ontoLog.getId(), client.getUser().getUserId());
			}

		} catch (final Exception e) {
			log.error("Error deleting ClientPlatform", e);
			throw new OntologyServiceException("Couldn't delete ClientPlatform");
		}
	}

	@Override
	@Transactional
	public void deleteToken(Token token) {
		try {
			ClientPlatform cp = token.getClientPlatform();

			Set<Token> tokens = new HashSet<>();

			for (Token tokencp : cp.getTokens()) {
				if (!tokencp.getId().equals(token.getId())) {
					tokens.add(tokencp);
				}
			}
			
			tokenRepository.delete(token);
			
			cp.setTokens(tokens);
			clientPlatformRepository.save(cp);

		} catch (final Exception e) {
			log.error("Error deleting Token", e);
			throw new OntologyServiceException("Couldn't delete Token");
		}

	}

	@Override
	@Transactional
	public void deleteDeviceSimulation(ClientPlatformInstanceSimulation simulation) throws Exception {

		try {
			if (!simulation.isActive())
				deviceSimulationRepository.deleteById(simulation.getId());
			else
				throw new OntologyServiceException("Simulation is currently running");

		} catch (final Exception e) {
			log.error("Error deleting Simulation", e);
			throw new OntologyServiceException("Couldn't delete Simulation");
		}

	}

	@Override
	@Transactional
	public void revokeAuthorizations(Ontology ontology) {
		try {
			ontologyUserAccessRepository.deleteByOntology(ontology);
		} catch (final Exception e) {
			log.error("Error deleting Authorizations", e);
			throw new OntologyServiceException("Couldn't delete ontology's authorizations");
		}

	}

	@Override
	@Transactional
	public void deleteGadgetDataSource(String id, String userId) {
		if (gadgetDatasourceService.hasUserPermission(id, userId)) {
			final GadgetDatasource gadgetDS = gadgetDatasourceRepository.findById(id);
			if (gadgetDS != null) {
				if (resourceService.isResourceSharedInAnyProject(gadgetDS))
					throw new OPResourceServiceException(
							"This Datasource is shared within a Project, revoke access from project prior to deleting");

				// find measures with this datasource
				final List<GadgetMeasure> list = gadgetMeasureRepository.findByDatasource(id);
				if (list.size() > 0) {
					final HashSet<String> map = new HashSet<>();
					for (final GadgetMeasure gm : list) {
						map.add(gm.getGadget().getId());
					}
					for (final String gadgetId : map) {
						// Delete gadget for id
						gadgetRepository.delete(gadgetId);
					}

				}
				gadgetDatasourceRepository.delete(id);
			} else {
				throw new OntologyServiceException("Couldn't delete gadgetDataSource");
			}
		}
	}

	@Override
	public void deleteUser(String userId) {
		try {
			userRepository.deleteByUserId(userId);
			invalidateUserTokens(userId);
		} catch (final Exception e) {
			try {
				final List<OPResource> resources = resourceRepository.findByUser(userRepository.findByUserId(userId));
				if (resources.size() == 1 && resources.get(0) instanceof Ontology
						&& resources.get(0).getIdentification().toLowerCase().contains("audit")) {
					resourceRepository.delete(resources.get(0).getId());
					userRepository.deleteByUserId(userId);
					invalidateUserTokens(userId);
					return;
				}
				throw new UserServiceException("Could not delete user, there are resources owned by " + userId);

			} catch (final Exception e2) {
				log.error("Error deleting User", e2);
				throw e2;
			}

		}

	}

	@Override
	@Transactional
	public void deleteQueryTemplate(String id) {
		try {
			queryTemplateRepository.delete(id);
		} catch (final Exception e) {
			log.error("Error deleting Query Template", e);
			throw new QueryTemplateServiceException("Couldn't delete the query template");
		}

	}

	@Override
	public void deactivateUser(String userId) {
		final User user = userRepository.findByUserId(userId);

		if (user != null) {
			user.setDateDeleted(new Date());
			user.setActive(false);
			userRepository.save(user);
			invalidateUserTokens(userId);
			log.info("User {} has been deactivated", userId);
		} else {
			throw new UserServiceException("Invalid user " + userId);
		}

	}

	@Override
	public void deactivateUser(List<String> userIds) {

		try {
			final List<User> users = userRepository.findAll(userIds);

			if (users != null && users.size() > 0) {
				users.forEach(user -> {
					user.setDateDeleted(new Date());
					user.setActive(false);
					invalidateUserTokens(user.getUserId());
					log.info("User has been deleted from database");
				});
				userRepository.save(users);
			}

		} catch (final Exception e) {
			throw new UserServiceException("An exception occurred during update/delete users from database", e);
		}
	}

	@Override
	public void invalidateUserTokens(String userId) {
		log.debug("Deleteing user token x-op-apikey for user {}", userId);
		userTokenRepository.deleteByUser(userRepository.findByUserId(userId));
		log.debug("Revoking Oauth2 access tokens for user{}", userId);
		final Collection<OAuthAccessToken> tokens = oauthAccessTokenRepository.findByUserName(userId);
		tokens.forEach(t -> {
			oauthRefreshTokenRepository.delete(t.getRefreshToken());
			oauthAccessTokenRepository.delete(t.getTokenId());
		});

	}
}
