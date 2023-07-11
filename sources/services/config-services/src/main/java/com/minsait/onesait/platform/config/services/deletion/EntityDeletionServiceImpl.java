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
package com.minsait.onesait.platform.config.services.deletion;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.App;
import com.minsait.onesait.platform.config.model.ClientConnection;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformInstance;
import com.minsait.onesait.platform.config.model.ClientPlatformInstanceSimulation;
import com.minsait.onesait.platform.config.model.ClientPlatformOntology;
import com.minsait.onesait.platform.config.model.DashboardUserAccess;
import com.minsait.onesait.platform.config.model.DroolsRule;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.model.GadgetMeasure;
import com.minsait.onesait.platform.config.model.MapsLayer;
import com.minsait.onesait.platform.config.model.MapsMap;
import com.minsait.onesait.platform.config.model.MapsProject;
import com.minsait.onesait.platform.config.model.MapsStyle;
import com.minsait.onesait.platform.config.model.NotebookUserAccess;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.OntologyKPI;
import com.minsait.onesait.platform.config.model.OntologyTimeSeries;
import com.minsait.onesait.platform.config.model.PipelineUserAccess;
import com.minsait.onesait.platform.config.model.Project;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess;
import com.minsait.onesait.platform.config.model.Token;
import com.minsait.onesait.platform.config.model.TwitterListening;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.UserApi;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.repository.ApiRepository;
import com.minsait.onesait.platform.config.repository.AppRepository;
import com.minsait.onesait.platform.config.repository.AppUserRepository;
import com.minsait.onesait.platform.config.repository.ClientConnectionRepository;
import com.minsait.onesait.platform.config.repository.ClientPlatformInstanceRepository;
import com.minsait.onesait.platform.config.repository.ClientPlatformInstanceSimulationRepository;
import com.minsait.onesait.platform.config.repository.ClientPlatformOntologyRepository;
import com.minsait.onesait.platform.config.repository.ClientPlatformRepository;
import com.minsait.onesait.platform.config.repository.DashboardUserAccessRepository;
import com.minsait.onesait.platform.config.repository.DatasetResourceRepository;
import com.minsait.onesait.platform.config.repository.DroolsRuleRepository;
import com.minsait.onesait.platform.config.repository.GadgetDatasourceRepository;
import com.minsait.onesait.platform.config.repository.GadgetMeasureRepository;
import com.minsait.onesait.platform.config.repository.GadgetRepository;
import com.minsait.onesait.platform.config.repository.LineageRelationsRepository;
import com.minsait.onesait.platform.config.repository.NotebookUserAccessRepository;
import com.minsait.onesait.platform.config.repository.OPResourceRepository;
import com.minsait.onesait.platform.config.repository.OntologyKPIRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.OntologyRestHeadersRepository;
import com.minsait.onesait.platform.config.repository.OntologyRestRepository;
import com.minsait.onesait.platform.config.repository.OntologyRestSecurityRepository;
import com.minsait.onesait.platform.config.repository.OntologyTimeSeriesRepository;
import com.minsait.onesait.platform.config.repository.OntologyUserAccessRepository;
import com.minsait.onesait.platform.config.repository.PipelineUserAccessRepository;
import com.minsait.onesait.platform.config.repository.ProjectRepository;
import com.minsait.onesait.platform.config.repository.ProjectResourceAccessRepository;
import com.minsait.onesait.platform.config.repository.QueryTemplateRepository;
import com.minsait.onesait.platform.config.repository.TokenRepository;
import com.minsait.onesait.platform.config.repository.TwitterListeningRepository;
import com.minsait.onesait.platform.config.repository.UserApiRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.repository.UserTokenRepository;
import com.minsait.onesait.platform.config.services.apimanager.ApiManagerService;
import com.minsait.onesait.platform.config.services.app.AppService;
import com.minsait.onesait.platform.config.services.dashboard.DashboardService;
import com.minsait.onesait.platform.config.services.dataflow.DataflowService;
import com.minsait.onesait.platform.config.services.exceptions.OPResourceServiceException;
import com.minsait.onesait.platform.config.services.exceptions.OntologyServiceException;
import com.minsait.onesait.platform.config.services.exceptions.QueryTemplateServiceException;
import com.minsait.onesait.platform.config.services.exceptions.UserServiceException;
import com.minsait.onesait.platform.config.services.gadget.GadgetDatasourceService;
import com.minsait.onesait.platform.config.services.kafka.KafkaAuthorizationService;
import com.minsait.onesait.platform.config.services.mapslayer.MapsLayerService;
import com.minsait.onesait.platform.config.services.mapsmap.MapsMapService;
import com.minsait.onesait.platform.config.services.mapsproject.MapsProjectService;
import com.minsait.onesait.platform.config.services.mapsstyle.MapsStyleService;
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
	private ProjectResourceAccessRepository resourceAccessRepository;
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
	private DatasetResourceRepository datasetResourceRepository;
	@Autowired
	@Lazy
	private OPResourceService resourceService;
	@Autowired
	private GadgetDatasourceRepository gadgetDatasourceRepository;
	@Autowired
	@Lazy
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
	@Autowired
	private AppUserRepository appUserRepository;
	@Autowired
	private DroolsRuleRepository droolsRuleRepository;
	@Autowired
	private LineageRelationsRepository lineageRelationsRepository;
	@Autowired
	private AppRepository appRepository;
	@Autowired
	private ProjectRepository projectRepository;
	@Autowired
	private AppService appService;

	private static final String ADMINISTRATOR = "administrator";
	private MapsStyleService mapsStyleService;
	@Autowired
	private MapsLayerService mapsLayerService;
	@Autowired
	private MapsMapService mapsMapService;
	@Autowired
	private MapsProjectService mapsProjectService;
	@Autowired
	private ProjectResourceAccessRepository praRepository;
	@Autowired
	private DashboardUserAccessRepository dashboardUserAccessRepository;
	@Autowired
	private NotebookUserAccessRepository notebookUserAccessRepository;
	@Autowired
	private UserApiRepository userApiRepository;
	@Autowired
	private PipelineUserAccessRepository pipelineUserAccessRepository;
	@Autowired
	private DashboardService dashboardService;
	@Autowired
	private ApiManagerService apiManagerService;
	@Autowired
	private DataflowService dataflowService;

	@Override
	public void deleteOntology(String id, String userId, Boolean isHardDeleted) {

		final User user = userService.getUser(userId);
		Boolean isShared = false;
		Ontology ontology = ontologyService.getOntologyById(id, userId);
		if (ontology.isPublic()) {
			isShared = true;
		}
		if (resourceService.isResourceSharedInAnyProject(ontology)) {
			throw new OPResourceServiceException(
					"This Ontology is shared within a Project, revoke access from project prior to deleting");
		}
		if (ontologyService.hasUserPermisionForChangeOntology(user, ontology)) {
			for (ClientPlatformOntology cpo : clientPlatformOntologyRepository.findByOntology(ontology)) {
				final ClientPlatform client = cpo.getClientPlatform();
				if (!isHardDeleted || (isHardDeleted && client.getUser().equals(ontology.getUser()))) {
					client.getClientPlatformOntologies().removeIf(r -> r.getOntology().equals(ontology));
					clientPlatformOntologyRepository.deleteById(cpo.getId());
					kafkaAuthorizationService.removeAclToOntologyClient(cpo);
				} else if (isHardDeleted && !client.getUser().equals(ontology.getUser())) {
					isShared = true;
				}
			}

			for (Api a : apiRepository.findByOntology(ontology)) {
				if (!isHardDeleted || (isHardDeleted && a.getUser().equals(ontology.getUser()))) {
					projectService.deleteResourceFromProjects(a.getId());
					apiRepository.delete(a);
				} else if (isHardDeleted && !a.getUser().equals(ontology.getUser())) {
					isShared = true;
				}
			}

			if (!ontologyUserAccessRepository.findByOntology(ontology).isEmpty()) {
				if (!isHardDeleted)
					ontologyUserAccessRepository.deleteByOntology(ontology);
				else
					isShared = true;
			}

			if (!twitterListeningRepository.findByOntology(ontology).isEmpty()) {
				twitterListeningRepository.deleteByOntology(ontology);
			}
			if (!deviceSimulationRepository.findByOntology(ontology).isEmpty()) {
				if (!isHardDeleted)
					deviceSimulationRepository.deleteByOntology(ontology);
				else
					isShared = true;
			}

			if (ontologyRestRepository.findByOntologyId(ontology) != null) {
				ontologyRestHeaderRepository.delete(ontologyRestRepository.findByOntologyId(ontology).getHeaderId());
			}
			if (ontologyRestRepository.findByOntologyId(ontology) != null) {
				ontologyRestSecurityRepository
						.delete(ontologyRestRepository.findByOntologyId(ontology).getSecurityId());
			}
			if (!datasetResourceRepository.findByOntology(ontology).isEmpty()) {
				datasetResourceRepository.findByOntology(ontology).forEach(a -> {
					datasetResourceRepository.delete(a);
				});
			}

			for (DroolsRule dr : droolsRuleRepository
					.findBySourceOntologyOrTargetOntology(ontology.getIdentification())) {
				if (!isHardDeleted || (isHardDeleted && dr.getUser().equals(ontology.getUser())))
					droolsRuleRepository.delete(dr);
				else
					isShared = true;
			}

			for (GadgetDatasource gd : gadgetDatasourceRepository.findByOntology(ontology)) {
				if (!isHardDeleted || (isHardDeleted && gd.getUser().equals(ontology.getUser())))
					deleteGadgetDataSource(gd.getId(), userId);
				else
					isShared = true;
			}

			final List<OntologyKPI> kpi = kpiRepository.findByOntology(ontology);
			final List<OntologyTimeSeries> timeSeries = ontologyTimeSeriesRepository.findByOntology(ontology);

			if (isShared && isHardDeleted) {
				// If ontology is shared on hard delete user option, change the ontology owner
				// to administrator
				ontology.setUser(userService.getUser(ADMINISTRATOR));
				ontology.setPublic(true);
				ontologyRepository.save(ontology);
			} else {
				if (!kpi.isEmpty()) {
					for (Iterator iterator = kpi.iterator(); iterator.hasNext();) {
						OntologyKPI ontokpi = (OntologyKPI) iterator.next();
						kpiRepository.delete(ontokpi);
					}
					ontologyRepository.deleteById(id);

				} else if (!timeSeries.isEmpty()) {
					ontologyTimeSeriesRepository.deleteByOntology(ontology);
					final Ontology stats = ontologyRepository
							.findByIdentification(timeSeries.get(0).getOntology().getIdentification() + "_stats");
					ontologyRepository.deleteById(id);
					if (stats != null) {
						ontologyRepository.deleteById(stats.getId());

					}
				} else {
					ontologyRepository.deleteById(id);
				}
			}

		} else {
			throw new OntologyServiceException("You dont have rights to delete ontology");
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

			final ClientPlatform client = clientPlatformRepository.findById(id).orElse(null);
			final List<ClientPlatformOntology> cpf = clientPlatformOntologyRepository.findByClientPlatform(client);
			if (!CollectionUtils.isEmpty(cpf)) {
				for (final ClientPlatformOntology clientPlatformOntology : cpf) {
					clientPlatformOntologyRepository.delete(clientPlatformOntology);
					kafkaAuthorizationService.removeAclToOntologyClient(clientPlatformOntology);
				}

			}
			final List<ClientConnection> cc = clientConnectionRepository.findByClientPlatform(client);
			if (!CollectionUtils.isEmpty(cc)) {
				for (final ClientConnection clientConnection : cc) {
					clientConnectionRepository.delete(clientConnection);
				}
			}

			final List<ClientPlatformInstance> ld = deviceRepository.findByClientPlatform(client);
			if (!CollectionUtils.isEmpty(ld)) {
				for (final ClientPlatformInstance device : ld) {
					deviceRepository.delete(device);
				}
			}
			final List<ClientPlatformInstanceSimulation> lds = deviceSimulationRepository.findByClientPlatform(client);
			if (!CollectionUtils.isEmpty(lds)) {
				for (final ClientPlatformInstanceSimulation deviceSim : lds) {
					deviceSimulationRepository.delete(deviceSim);
				}
			}
			clientPlatformRepository.delete(client);
			final Ontology ontoLog = ontologyRepository.findByIdentification("LOG_" + client.getIdentification());
			if (ontoLog != null) {
				deleteOntology(ontoLog.getId(), client.getUser().getUserId(), false);
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
			final ClientPlatform cp = token.getClientPlatform();

			final Set<Token> tokens = new HashSet<>();

			for (final Token tokencp : cp.getTokens()) {
				if (!tokencp.getId().equals(token.getId())) {
					tokens.add(tokencp);
				}
			}
			final List<ClientPlatformInstanceSimulation> simulations = deviceSimulationRepository
					.findByClientPlatform(cp);
			simulations.forEach(this::deleteDeviceSimulation);

			tokenRepository.delete(token);

			cp.setTokens(tokens);
			clientPlatformRepository.save(cp);
			// Remove Kafka ACLs
			kafkaAuthorizationService.removeAclToClientForToken(token);
		} catch (final Exception e) {
			log.error("Error deleting Token", e);
			throw new OntologyServiceException("Couldn't delete Token");
		}

	}

	@Override
	@Transactional
	public void deleteDeviceSimulation(ClientPlatformInstanceSimulation simulation) {

		try {

			deviceSimulationRepository.deleteById(simulation.getId());

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
	public void deleteGadgetDataSource(String id, String userId) {
		if (gadgetDatasourceService.hasUserPermission(id, userId)) {
			final GadgetDatasource gadgetDS = gadgetDatasourceRepository.findById(id).orElse(null);
			if (gadgetDS != null) {
				if (resourceService.isResourceSharedInAnyProject(gadgetDS)) {
					throw new OPResourceServiceException(
							"This Datasource is shared within a Project, revoke access from project prior to deleting");
				}

				// find measures with this datasource
				final List<GadgetMeasure> list = gadgetMeasureRepository.findByDatasource(id);
				if (list.size() > 0) {
					final HashSet<String> map = new HashSet<>();
					for (final GadgetMeasure gm : list) {
						map.add(gm.getGadget().getId());
					}
					for (final String gadgetId : map) {
						// Delete gadget for id
						gadgetRepository.deleteById(gadgetId);
					}

				}
				gadgetDatasourceRepository.deleteById(id);
			} else {
				throw new OntologyServiceException("Couldn't delete gadgetDataSource");
			}
		}
	}

	@Override
	public void deleteUser(String userId) {
		try {
			lineageRelationsRepository.deleteByUser(userId);
			appUserRepository.deleteByUserId(userId);
			userRepository.deleteByUserId(userId);
			invalidateUserTokens(userId);
		} catch (final Exception e) {
			try {
				User user = userRepository.findByUserId(userId);

				ontologyUserAccessRepository.findByUser(user).forEach(oua -> {
					ontologyService.deleteOntologyUserAccess(oua.getId(), userId);
				});

				List<DashboardUserAccess> das = dashboardUserAccessRepository.findByUser(user);
				if (das.size() > 0) {
					dashboardService.deleteDashboardUserAccessForAUser(userId);
				}
				List<NotebookUserAccess> not = notebookUserAccessRepository.findByUser(user);
				if (not.size() > 0) {
					notebookUserAccessRepository.deleteAll(not);
				}

				List<UserApi> userap = userApiRepository.findByUser(user);
				if (userap.size() > 0) {
					apiManagerService.deleteUserApiForAUser(userId);
				}
				List<PipelineUserAccess> pipe = pipelineUserAccessRepository.findByUser(user);
				if (pipe.size() > 0) {
					dataflowService.deletePipeUserAccessForAUser(userId);
				}

				final List<OPResource> resources = resourceRepository.findByUser(user);

				if (resources.size() == 1 && resources.get(0) instanceof Ontology
						&& resources.get(0).getIdentification().toLowerCase().contains("audit")) {
					resourceRepository.deleteById(resources.get(0).getId());
					userRepository.deleteByUserId(userId);
					invalidateUserTokens(userId);
					return;
				} else if (resources.isEmpty()) {
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
	public void hardDeleteUser(String userId) {

		try {
			User user = userService.getUserNoCache(userId);
			lineageRelationsRepository.deleteByUser(userId);
			User admin = userService.getUser(ADMINISTRATOR);

			for (Project p : projectService.getProjectsForUser(userId)) {
				Set<ProjectResourceAccess> pra = projectService.getAllResourcesAccesses(p.getId());
				if (pra.isEmpty()) {
					projectService.deleteProject(p.getId());
					if (p.getApp() != null)
						appRepository.delete(p.getApp());
				} else {
					if (p.getApp() != null) {
						appUserRepository.deleteByUserId(userId);
						App appDb = appRepository.findById(p.getApp().getId()).orElse(null);
						if (appDb != null) {
							if (appDb.getChildApps().isEmpty()) {
								appDb.setUser(admin);
								appRepository.save(appDb);
							} else {
								appDb.getChildApps().forEach(ac -> {
									App appDbChild = appRepository.findById(ac.getId()).orElse(null);
									appDbChild.setUser(admin);
									appRepository.save(appDbChild);
								});
							}
						}
					} else {
						p.getUsers().remove(user);
						user.getProjects().removeIf(pr -> pr.getId().equals(p.getId()));
						userService.saveExistingUser(user);
						userService.evictFromCache(user);
					}

					if (p.getUser().equals(user))
						p.setUser(admin);
					projectRepository.save(p);

					pra.forEach(ra -> {
						if (ra.getUser().equals(user)) {
							praRepository.delete(ra);
						} else if (ra.getResource().getUser().equals(user)) {
							ra.getResource().setUser(admin);
							resourceRepository.save(ra.getResource());
						}
					});

				}
			}

			appRepository.findByUser(user).forEach(a -> appRepository.delete(a));
			ontologyUserAccessRepository.findByUser(user).forEach(oua -> {
				ontologyService.deleteOntologyUserAccess(oua.getId(), ADMINISTRATOR);
			});

			ontologyRepository.findByUserNotChacheable(userService.getUser(userId)).forEach(o -> {
				if (resourceAccessRepository.findProjectsWithResourceId(o.getId()).isEmpty())
					this.deleteOntology(o.getId(), userId, true);
			});

			resourceRepository.findByUser(user).forEach(r -> {
				if (resourceAccessRepository.findProjectsWithResourceId(r.getId()).isEmpty())
					resourceRepository.deleteById(r.getId());
			});

			List<DashboardUserAccess> das = dashboardUserAccessRepository.findByUser(user);
			if (das.size() > 0) {
				dashboardService.deleteDashboardUserAccessForAUser(userId);
			}

			List<NotebookUserAccess> not = notebookUserAccessRepository.findByUser(user);
			if (not.size() > 0) {
				notebookUserAccessRepository.deleteAll(not);
			}

			List<UserApi> userap = userApiRepository.findByUser(user);
			if (userap.size() > 0) {
				apiManagerService.deleteUserApiForAUser(userId);
			}
			List<PipelineUserAccess> pipe = pipelineUserAccessRepository.findByUser(user);
			if (pipe.size() > 0) {
				dataflowService.deletePipeUserAccessForAUser(userId);
			}

			invalidateUserTokens(userId);
			userService.hardDeleteUser(userId);

		} catch (final Exception e2) {
			log.error("Error deleting User", e2);
			throw e2;
		}
	}

	@Override
	@Transactional
	public void deleteQueryTemplate(String id) {
		try {
			queryTemplateRepository.deleteById(id);
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
			final List<User> users = userRepository.findAllById(userIds);

			if (!CollectionUtils.isEmpty(users)) {
				users.forEach(user -> {
					user.setDateDeleted(new Date());
					user.setActive(false);
					invalidateUserTokens(user.getUserId());
					log.info("User has been deleted from database");
				});
				userRepository.saveAll(users);
			}

		} catch (final Exception e) {
			throw new UserServiceException("An exception occurred during update/delete users from database", e);
		}
	}

	@Override
	public void invalidateUserTokens(String userId) {
		log.debug("Deleteing user token x-op-apikey for user {}", userId);
		userTokenRepository.deleteByUser(userId);
		log.debug("Revoking Oauth2 access tokens for user{}", userId);
		final Collection<OAuthAccessToken> tokens = oauthAccessTokenRepository.findByUserName(userId);
		tokens.forEach(t -> {
			oauthRefreshTokenRepository.deleteById(t.getRefreshToken());
			oauthAccessTokenRepository.deleteById(t.getTokenId());
		});

	}

	@Override
	@Transactional
	public void deleteMapsStyle(String id, String userId) {
		if (mapsStyleService.hasUserPermission(id, userId)) {
			final MapsStyle mapsStyle = mapsStyleService.getById(id);
			if (mapsStyle != null) {
				if (resourceService.isResourceSharedInAnyProject(mapsStyle)) {
					throw new OPResourceServiceException(
							"This Map Style is shared within a Project, revoke access from project prior to deleting");
				}
				mapsStyleService.delete(id, userId);
			} else {
				throw new OntologyServiceException("Couldn't delete Map Style");
			}
		}
	}

	@Override
	@Transactional
	public void deleteMapsLayer(String id, String userId) {
		if (mapsLayerService.hasUserPermission(id, userId)) {
			final MapsLayer mapsLayer = mapsLayerService.getById(id);
			if (mapsLayer != null) {
				if (resourceService.isResourceSharedInAnyProject(mapsLayer)) {
					throw new OPResourceServiceException(
							"This Map Layer is shared within a Project, revoke access from project prior to deleting");
				}
				// TODO validate if is used on maps

				mapsLayerService.delete(id, userId);
			} else {
				throw new OntologyServiceException("Couldn't delete Map Layer");
			}
		}
	}

	@Override
	@Transactional
	public void deleteMapsMap(String id, String userId) {
		if (mapsMapService.hasUserPermission(id, userId)) {
			final MapsMap mapsMap = mapsMapService.getById(id);
			if (mapsMap != null) {
				if (resourceService.isResourceSharedInAnyProject(mapsMap)) {
					throw new OPResourceServiceException(
							"This Map is shared within a Project, revoke access from project prior to deleting");
				}
				// TODO validate if is used on maps

				mapsMapService.delete(id, userId);
			} else {
				throw new OntologyServiceException("Couldn't delete Map");
			}
		}
	}

	@Override
	@Transactional
	public void deleteMapsProject(String id, String userId) {
		if (mapsMapService.hasUserPermission(id, userId)) {
			final MapsProject mapsProject = mapsProjectService.getById(id);
			if (mapsProject != null) {
				if (resourceService.isResourceSharedInAnyProject(mapsProject)) {
					throw new OPResourceServiceException(
							"This Map is shared within a Project, revoke access from project prior to deleting");
				}
				// TODO validate if is used on maps

				mapsProjectService.delete(id, userId);
			} else {
				throw new OntologyServiceException("Couldn't delete Map");
			}
		}
	}
}
