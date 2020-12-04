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
package com.minsait.onesait.platform.config.services.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minsait.onesait.platform.commons.metrics.MetricsManager;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformOntology;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.AccessType;
import com.minsait.onesait.platform.config.model.Ontology.RtdbCleanLapse;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.model.Token;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.ClientPlatformOntologyRepository;
import com.minsait.onesait.platform.config.repository.ClientPlatformRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.TokenRepository;
import com.minsait.onesait.platform.config.services.client.dto.DeviceCreateDTO;
import com.minsait.onesait.platform.config.services.datamodel.DataModelService;
import com.minsait.onesait.platform.config.services.device.dto.ClientPlatformDTO;
import com.minsait.onesait.platform.config.services.device.dto.TokenDTO;
import com.minsait.onesait.platform.config.services.exceptions.ClientPlatformServiceException;
import com.minsait.onesait.platform.config.services.kafka.KafkaAuthorizationService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.token.TokenService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ClientPlatformServiceImpl implements ClientPlatformService {
	@Autowired
	private ClientPlatformRepository clientPlatformRepository;
	@Autowired
	private ClientPlatformOntologyRepository clientPlatformOntologyRepository;
	@Autowired
	private OntologyRepository ontologyRepository;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private TokenService tokenService;
	@Autowired
	private UserService userService;
	@Autowired
	private DataModelService dataModelService;
	@Autowired
	private TokenRepository tokenRepository;
	@Autowired
	private OPResourceService resourceService;
	@Autowired(required = false)
	private MetricsManager metricsManager;
	@Autowired
	private KafkaAuthorizationService kafkaAuthorizationService;
	@Autowired
	private MultitenancyService multitenancyService;
	@Value("${onesaitplatform.multitenancy.enabled:false}")
	private boolean isMultitenancyEnabled;

	private static final String LOG_ONTOLOGY_PREFIX = "LOG_";
	private static final String LOG_DEVICE_DATA_MODEL = "DeviceLog";
	private static final String ACCESS_STR = "access";
	private static final String NOT_ACCESS = "This user has not access";
	private static final String USER_HAS_NOT_CORRECT_ACCESS = "The user: {}, has not the correct access to the Ontology: {}";

	@Override
	public Token createClientAndToken(List<Ontology> ontologies, ClientPlatform clientPlatform) {
		if (clientPlatformRepository.findByIdentification(clientPlatform.getIdentification()) == null) {
			final String encryptionKey = UUID.randomUUID().toString();
			clientPlatform.setEncryptionKey(encryptionKey);
			clientPlatform = clientPlatformRepository.save(clientPlatform);

			for (final Ontology ontology : ontologies) {
				final ClientPlatformOntology relation = new ClientPlatformOntology();
				relation.setClientPlatform(clientPlatform);
				relation.setAccess(Ontology.AccessType.ALL);
				relation.setOntology(ontology);
				// If relation does not exist then create
				if (clientPlatformOntologyRepository.findByOntologyAndClientPlatform(ontology.getIdentification(),
						clientPlatform.getIdentification()) == null) {
					clientPlatformOntologyRepository.save(relation);
					kafkaAuthorizationService.addAclToOntologyClient(relation);
				}
			}

			metricsManagerLogControlPanelClientsPlatformCreation(clientPlatform.getUser().getUserId(), "OK");
			return tokenService.generateTokenForClient(clientPlatform);
		} else {
			metricsManagerLogControlPanelClientsPlatformCreation(clientPlatform.getUser().getUserId(), "KO");
			throw new ClientPlatformServiceException("Platform Client already exists");
		}
	}

	@Override
	public ClientPlatform getByIdentification(String identification) {
		return clientPlatformRepository.findByIdentification(identification);
	}

	@Override
	public List<ClientPlatform> getAllClientPlatforms() {
		return clientPlatformRepository.findAll();
	}

	@Override
	public List<ClientPlatform> getclientPlatformsByUser(User user) {
		return clientPlatformRepository.findByUser(user);
	}

	private List<ClientPlatform> getClientPlatform(String userId, String identification) {

		final User user = userService.getUser(userId);
		List<ClientPlatform> clients = new ArrayList<>();

		if (userService.isUserAdministrator(user)) {
			if (identification != null) {
				final ClientPlatform cli = clientPlatformRepository.findByIdentification(identification);
				if (cli != null) {
					clients.add(cli);
				}
			} else {
				clients = clientPlatformRepository.findAll();
			}
		} else {
			if (identification != null) {
				final ClientPlatform cliUs = clientPlatformRepository.findByUserAndIdentification(user, identification);
				if (cliUs != null) {
					clients.add(cliUs);
				}
			} else {
				clients = clientPlatformRepository.findByUser(user);
			}
		}

		return clients;
	}

	private void checkClientPlatform(List<ClientPlatform> clientPlatforms, String[] ontologies, ClientPlatform client) {
		for (final String ontologie : ontologies) {
			final Ontology o = ontologyRepository.findByIdentification(ontologie);
			if (o != null) {
				final ClientPlatformOntology clpo = clientPlatformOntologyRepository
						.findByOntologyAndClientPlatform(o.getIdentification(), client.getIdentification());
				if (clpo != null && !clientPlatforms.contains(client)) {
					clientPlatforms.add(client);
				}
			}
		}
	}

	@Override
	public List<ClientPlatform> getAllClientPlatformByCriteria(String userId, String identification,
			String[] ontologies) {
		final List<ClientPlatform> clients = getClientPlatform(userId, identification);

		if (ontologies != null && ontologies.length > 0) {

			final List<ClientPlatform> clientPlatforms = new ArrayList<>();

			for (final ClientPlatform client : clients) {
				checkClientPlatform(clientPlatforms, ontologies, client);
			}
			return clientPlatforms;
		}
		return clients;
	}

	@Override
	public List<AccessType> getClientPlatformOntologyAccessLevel() {
		final List<AccessType> list = new ArrayList<>();
		list.add(Ontology.AccessType.ALL);
		list.add(Ontology.AccessType.INSERT);
		list.add(Ontology.AccessType.QUERY);
		return list;
	}

	@Override
	@Transactional
	public ClientPlatform createClientPlatform(DeviceCreateDTO device, String userId, Boolean isUpdate) {

		if (clientPlatformRepository.findByIdentification(device.getIdentification()) != null) {
			metricsManagerLogControlPanelClientsPlatformCreation(userId, "KO");
			throw new ClientPlatformServiceException(
					"Device with identification:" + device.getIdentification() + " exists");
		}

		try {
			final ClientPlatform ndevice = new ClientPlatform();
			ndevice.setIdentification(device.getIdentification());
			ndevice.setMetadata(device.getMetadata());
			ndevice.setDescription(device.getDescription());

			final JSONArray ontologies = new JSONArray(device.getClientPlatformOntologies());
			final Set<ClientPlatformOntology> clientsPlatformOntologies = new HashSet<>();
			for (int i = 0; i < ontologies.length(); i++) {
				final JSONObject ontology = ontologies.getJSONObject(i);

				final Ontology ontologyDB = ontologyRepository.findByIdentification(ontology.getString("id"));
				final AccessType accessType = AccessType.valueOf(ontology.getString(ACCESS_STR));
				if (!ontologyService.hasUserPermission(userService.getUser(userId), accessType, ontologyDB)) {
					log.error(USER_HAS_NOT_CORRECT_ACCESS, userId, ontology.getString("id"));
					metricsManagerLogControlPanelClientsPlatformCreation(userId, "KO");
					throw new ClientPlatformServiceException(NOT_ACCESS);
				}
				final ClientPlatformOntology clientPlatformOntology = new ClientPlatformOntology();
				// Check if the user has access to the ontology

				clientPlatformOntology.setAccess(AccessType.valueOf(ontology.getString(ACCESS_STR)));
				clientPlatformOntology.setOntology(ontologyDB);
				clientsPlatformOntologies.add(clientPlatformOntology);
			}

			ndevice.setUser(userService.getUser(userId));

			final String encryptionKey = UUID.randomUUID().toString();
			ndevice.setEncryptionKey(encryptionKey);

			final ClientPlatform cli = clientPlatformRepository.save(ndevice);

			final JSONArray tokensArray = new JSONArray(device.getTokens());
			Set<Token> tokens = new HashSet<>();
			for (int i = 0; i < tokensArray.length(); i++) {
				final JSONObject token = tokensArray.getJSONObject(i);
				final Token tokn = new Token();
				tokn.setClientPlatform(cli);
				tokn.setTokenName(token.getString("token"));
				tokn.setActive(token.getBoolean("active"));
				tokenRepository.save(tokn);
				tokens.add(tokn);
			}

			for (final ClientPlatformOntology cpoNew : clientsPlatformOntologies) {
				cpoNew.setClientPlatform(cli);
				clientPlatformOntologyRepository.save(cpoNew);
				kafkaAuthorizationService.addAclToOntologyOnClientCreation(cpoNew, tokens);
			}

			metricsManagerLogControlPanelClientsPlatformCreation(userId, "OK");

			return ndevice;
		} catch (final Exception e) {
			metricsManagerLogControlPanelClientsPlatformCreation(userId, "KO");
			throw e;
		}

	}

	private boolean updateAccessForOntology(JSONArray ontologies, ClientPlatformOntology clientPlatformOntology,
			String userId) {
		for (int i = 0; i < ontologies.length(); i++) {
			final JSONObject ontology = ontologies.getJSONObject(i);

			final AccessType accessType = AccessType.valueOf(ontology.getString(ACCESS_STR));

			if (ontology.getString("id").equals(clientPlatformOntology.getOntology().getIdentification())) {
				if (!ontologyService.hasUserPermission(userService.getUser(userId), accessType,
						clientPlatformOntology.getOntology())) {
					log.error(USER_HAS_NOT_CORRECT_ACCESS, userId, ontology.getString("id"));
					throw new ClientPlatformServiceException(NOT_ACCESS);
				}

				clientPlatformOntology.setAccess(AccessType.valueOf(ontology.getString(ACCESS_STR)));
				clientPlatformOntologyRepository.save(clientPlatformOntology);
				kafkaAuthorizationService.addAclToOntologyClient(clientPlatformOntology);

				return true;
			}
		}

		return false;
	}

	private void createAccessForOntology(JSONObject ontology, ClientPlatform ndevice, String userId) {

		final AccessType accessType = AccessType.valueOf(ontology.getString(ACCESS_STR));
		final Ontology ontologyDB = ontologyRepository.findByIdentification(ontology.getString("id"));

		// Check if the user has access to the ontology
		if (!ontologyService.hasUserPermission(userService.getUser(userId), accessType, ontologyDB)) {
			log.error(USER_HAS_NOT_CORRECT_ACCESS, userId, ontology.getString("id"));
			throw new ClientPlatformServiceException(NOT_ACCESS);
		}
		final ClientPlatformOntology clientPlatformOntology = new ClientPlatformOntology();

		clientPlatformOntology.setAccess(AccessType.valueOf(ontology.getString(ACCESS_STR)));
		clientPlatformOntology.setOntology(ontologyDB);
		clientPlatformOntology.setClientPlatform(ndevice);
		if (clientPlatformOntologyRepository.findByOntologyAndClientPlatform(ontology.getString("id"),
				ndevice.getIdentification()) == null) {
			clientPlatformOntologyRepository.save(clientPlatformOntology);
			kafkaAuthorizationService.addAclToOntologyClient(clientPlatformOntology);
		}
	}

	@Override
	@Transactional
	public void updateDevice(DeviceCreateDTO device, String userId) {
		final ClientPlatform ndevice = clientPlatformRepository.findByIdentification(device.getIdentification());
		ndevice.setMetadata(device.getMetadata());
		ndevice.setDescription(device.getDescription());
		JSONArray ontologies = new JSONArray();
		if (device.getClientPlatformOntologies() != null) {
			ontologies = new JSONArray(device.getClientPlatformOntologies());
		}
		for (final Iterator<ClientPlatformOntology> iterator = ndevice.getClientPlatformOntologies()
				.iterator(); iterator.hasNext();) {
			final ClientPlatformOntology clientPlatformOntology = iterator.next();
			final boolean find = updateAccessForOntology(ontologies, clientPlatformOntology, userId);

			if (!find) {
				kafkaAuthorizationService.removeAclToOntologyClient(clientPlatformOntology);
				iterator.remove();
			}
		}
		for (int i = 0; i < ontologies.length(); i++) {
			final JSONObject ontology = ontologies.getJSONObject(i);
			boolean find = false;
			for (final ClientPlatformOntology clientPlatformOntology : ndevice.getClientPlatformOntologies()) {
				if (ontology.getString("id").equals(clientPlatformOntology.getOntology().getIdentification())) {
					find = true;
					break;
				}
			}
			if (!find) {
				createAccessForOntology(ontology, ndevice, userId);
			}
		}
		clientPlatformRepository.save(ndevice);
		log.debug("stop");
	}

	@Override
	public void createOntologyRelation(Ontology ontology, ClientPlatform clientPlatform) {

		final ClientPlatformOntology relation = new ClientPlatformOntology();
		relation.setClientPlatform(clientPlatform);
		relation.setAccess(Ontology.AccessType.ALL);
		relation.setOntology(ontology);
		// If relation does not exist then create
		if (clientPlatformOntologyRepository.findByOntologyAndClientPlatform(ontology.getIdentification(),
				clientPlatform.getIdentification()) == null) {
			clientPlatformOntologyRepository.save(relation);
			kafkaAuthorizationService.addAclToOntologyClient(relation);
		}

	}

	@Override
	public Ontology getDeviceLogOntology(ClientPlatform client) {
		return ontologyRepository
				.findByIdentification((LOG_ONTOLOGY_PREFIX + client.getIdentification()).replaceAll(" ", ""));

	}

	@Override
	public List<Token> getTokensByClientPlatformId(String clientPlatformId) {
		final Optional<ClientPlatform> clientPlatform = clientPlatformRepository.findById(clientPlatformId);
		if (clientPlatform.isPresent()) {
			return tokenService.getTokens(clientPlatform.get());
		} else {
			return new ArrayList<>();
		}

	}

	@Override
	public List<Ontology> getOntologiesByClientPlatform(String clientPlatformIdentification) {
		final ClientPlatform client = clientPlatformRepository.findByIdentification(clientPlatformIdentification);
		if (client != null) {
			return clientPlatformOntologyRepository.findByClientPlatform(client).stream()
					.map(ClientPlatformOntology::getOntology).collect(Collectors.toList());
		}
		return new ArrayList<>();
	}

	@Override
	public ClientPlatform getById(String id) {
		return clientPlatformRepository.findById(id).orElse(null);
	}

	@Override
	public boolean hasUserManageAccess(String id, String userId) {
		final User user = userService.getUser(userId);
		final Optional<ClientPlatform> opt = clientPlatformRepository.findById(id);
		if (opt.isPresent()) {
			final ClientPlatform clientPlatform = opt.get();
			if (user.equals(clientPlatform.getUser()) || userService.isUserAdministrator(user)) {
				return true;
			} else {
				return resourceService.hasAccess(userId, id, ResourceAccessType.MANAGE);
			}
		} else {
			return false;
		}

	}

	@Override
	public boolean hasUserViewAccess(String id, String userId) {
		final User user = userService.getUser(userId);
		final Optional<ClientPlatform> opt = clientPlatformRepository.findById(id);
		if (opt.isPresent()) {
			final ClientPlatform clientPlatform = opt.get();
			if (user.equals(clientPlatform.getUser()) || userService.isUserAdministrator(user)) {
				return true;
			} else {
				return resourceService.hasAccess(userId, id, ResourceAccessType.VIEW);
			}
		} else {
			return false;
		}

	}

	@Override
	public Token createClientTokenWithAccessType(Map<Ontology, AccessType> ontologies, ClientPlatform clientPlatform) {
		if (clientPlatformRepository.findByIdentification(clientPlatform.getIdentification()) == null) {
			final String encryptionKey = UUID.randomUUID().toString();
			clientPlatform.setEncryptionKey(encryptionKey);
			clientPlatform = clientPlatformRepository.save(clientPlatform);

			for (final Entry<Ontology, AccessType> ontology : ontologies.entrySet()) {
				final ClientPlatformOntology relation = new ClientPlatformOntology();
				relation.setClientPlatform(clientPlatform);
				relation.setAccess(ontology.getValue());
				relation.setOntology(ontology.getKey());
				// If relation does not exist then create
				if (clientPlatformOntologyRepository.findByOntologyAndClientPlatform(
						ontology.getKey().getIdentification(), clientPlatform.getIdentification()) == null) {
					clientPlatformOntologyRepository.save(relation);
					kafkaAuthorizationService.addAclToOntologyClient(relation);
				}
			}

			return tokenService.generateTokenForClient(clientPlatform);
		} else {
			throw new ClientPlatformServiceException("Platform Client already exists");
		}
	}

	private void metricsManagerLogControlPanelClientsPlatformCreation(String userId, String result) {
		if (null != metricsManager) {
			metricsManager.logControlPanelClientsPlatformCreation(userId, result);
		}
	}

	@Override
	public Ontology createDeviceLogOntology(final ClientPlatform client) {
		final Ontology logOntology = new Ontology();
		logOntology.setDataModel(dataModelService.getDataModelByName(LOG_DEVICE_DATA_MODEL));
		logOntology.setIdentification(LOG_ONTOLOGY_PREFIX + client.getIdentification().replaceAll(" ", ""));
		logOntology.setActive(true);
		logOntology.setUser(client.getUser());
		logOntology
				.setDescription("System Ontology. Centralized Log for devices of type " + client.getIdentification());
		logOntology.setJsonSchema(dataModelService.getDataModelByName(LOG_DEVICE_DATA_MODEL).getJsonSchema());
		logOntology.setPublic(false);
		logOntology.setRtdbClean(true);
		logOntology.setRtdbDatasource(RtdbDatasource.MONGO);
		logOntology.setRtdbCleanLapse(RtdbCleanLapse.SIX_MONTHS);
		return logOntology;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ClientPlatformDTO parseClientPlatform(ClientPlatform clientPlatform) {

		final Map<String, AccessType> ontologies = new HashMap<>();
		for (final ClientPlatformOntology ontology : clientPlatform.getClientPlatformOntologies()) {
			ontologies.put(ontology.getOntology().getIdentification(), ontology.getAccess());
		}
		final List tokens = new ArrayList();
		if (isMultitenancyEnabled) {
			for (final Token token : clientPlatform.getTokens()) {
				tokens.add(TokenDTO.builder().token(token.getTokenName()).active(token.isActive())
						.tenant(multitenancyService.getMasterDeviceToken(token.getTokenName()).getTenant()).build());
			}
		} else {
			for (final Token token : clientPlatform.getTokens()) {
				tokens.add(token.getTokenName());
			}
		}

		return ClientPlatformDTO.builder().user(clientPlatform.getUser().getUserId())
				.description(clientPlatform.getDescription()).identification(clientPlatform.getIdentification())
				.metadata(clientPlatform.getMetadata()).ontologies(ontologies).tokens(tokens).build();
	}

	@Override
	public ClientPlatform update(ClientPlatform clientPlatform) {
		return clientPlatformRepository.save(clientPlatform);
	}

}
