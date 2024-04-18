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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minsait.onesait.platform.commons.metrics.MetricsManager;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformOntology;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.AccessType;
import com.minsait.onesait.platform.config.model.Ontology.RtdbCleanLapse;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess.ResourceAccessType;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.Token;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.ClientPlatformOntologyRepository;
import com.minsait.onesait.platform.config.repository.ClientPlatformRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.TokenRepository;
import com.minsait.onesait.platform.config.services.client.dto.DeviceCreateDTO;
import com.minsait.onesait.platform.config.services.datamodel.DataModelService;
import com.minsait.onesait.platform.config.services.exceptions.ClientPlatformServiceException;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.token.TokenService;
import com.minsait.onesait.platform.config.services.user.UserService;

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
				}
			}
			
			this.metricsManagerLogControlPanelClientsPlatformCreation(clientPlatform.getUser().getUserId(), "OK");
			return tokenService.generateTokenForClient(clientPlatform);
		} else {
			this.metricsManagerLogControlPanelClientsPlatformCreation(clientPlatform.getUser().getUserId(), "KO");
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

		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			if (identification != null) {
				clients.add(clientPlatformRepository.findByIdentification(identification));
			} else {
				clients = clientPlatformRepository.findAll();
			}
		} else {
			if (identification != null) {
				clients.add(clientPlatformRepository.findByUserAndIdentification(user, identification));
			} else {
				clients = clientPlatformRepository.findByUser(user);
			}
		}

		return clients;
	}

	private void checkClientPlatform(List<ClientPlatform> clientPlatforms, String[] ontologies, ClientPlatform client) {
		for (int i = 0; i < ontologies.length; i++) {
			final Ontology o = ontologyRepository.findByIdentification(ontologies[i]);
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
			this.metricsManagerLogControlPanelClientsPlatformCreation(userId, "KO");
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
					this.metricsManagerLogControlPanelClientsPlatformCreation(userId, "KO");
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
			for (int i = 0; i < tokensArray.length(); i++) {
				final JSONObject token = tokensArray.getJSONObject(i);
				final Token tokn = new Token();
				tokn.setClientPlatform(cli);
				tokn.setTokenName(token.getString("token"));
				tokn.setActive(token.getBoolean("active"));
				tokenRepository.save(tokn);
	
			}
	
			for (final ClientPlatformOntology cpoNew : clientsPlatformOntologies) {
				cpoNew.setClientPlatform(cli);
				clientPlatformOntologyRepository.save(cpoNew);
			}
			
			this.metricsManagerLogControlPanelClientsPlatformCreation(userId, "OK");
	
			return ndevice;
		} catch (Exception e) {
			this.metricsManagerLogControlPanelClientsPlatformCreation(userId, "KO");
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
		}
	}

	@Override
	@Transactional
	public void updateDevice(DeviceCreateDTO device, String userId) {
		final ClientPlatform ndevice = clientPlatformRepository.findByIdentification(device.getIdentification());
		ndevice.setMetadata(device.getMetadata());
		ndevice.setDescription(device.getDescription());
		ndevice.setUser(userService.getUser(userId));
		JSONArray ontologies = new JSONArray();
		if (device.getClientPlatformOntologies() != null) {
			ontologies = new JSONArray(device.getClientPlatformOntologies());
		}
		for (final Iterator<ClientPlatformOntology> iterator = ndevice.getClientPlatformOntologies()
				.iterator(); iterator.hasNext();) {
			final ClientPlatformOntology clientPlatformOntology = iterator.next();
			final boolean find = updateAccessForOntology(ontologies, clientPlatformOntology, userId);

			if (!find) {
				clientPlatformOntologyRepository.delete(clientPlatformOntology);
				iterator.remove();
			}
		}
		for (int i = 0; i < ontologies.length(); i++) {
			final JSONObject ontology = ontologies.getJSONObject(i);
			boolean find = false;
			for (final Iterator<ClientPlatformOntology> iterator = ndevice.getClientPlatformOntologies()
					.iterator(); iterator.hasNext();) {
				final ClientPlatformOntology clientPlatformOntology = iterator.next();
				if (ontology.getString("id").equals(clientPlatformOntology.getOntology().getIdentification())) {
					find = true;
					break;
				}
			}
			if (!find) {
				createAccessForOntology(ontology, ndevice, userId);
			}
		}

		final ClientPlatform cli = clientPlatformRepository.save(ndevice);

		final Set<Token> tokens = new HashSet<>();
		final JSONArray tokensArray = new JSONArray(device.getTokens());
		for (int i = 0; i < tokensArray.length(); i++) {
			final JSONObject token = tokensArray.getJSONObject(i);
			final Token tokn = new Token();
			tokn.setClientPlatform(cli);
			tokn.setTokenName(token.getString("token"));
			tokn.setActive(token.getBoolean("active"));
			tokens.add(tokn);
		}
		cli.setTokens(tokens);
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

	@Override
	public Ontology getDeviceLogOntology(ClientPlatform client) {
		return ontologyRepository
				.findByIdentification((LOG_ONTOLOGY_PREFIX + client.getIdentification()).replaceAll(" ", ""));

	}

	@Override
	public List<Token> getTokensByClientPlatformId(String clientPlatformId) {
		final ClientPlatform clientPlatform = clientPlatformRepository.findById(clientPlatformId);
		return tokenService.getTokens(clientPlatform);
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
		return clientPlatformRepository.findOne(id);
	}

	@Override
	public boolean hasUserManageAccess(String id, String userId) {
		final User user = userService.getUser(userId);
		final ClientPlatform clientPlatform = clientPlatformRepository.findById(id);
		if (user.equals(clientPlatform.getUser())
				|| user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString()))
			return true;
		else {
			return resourceService.hasAccess(userId, id, ResourceAccessType.MANAGE);
		}

	}

	@Override
	public boolean hasUserViewAccess(String id, String userId) {
		final User user = userService.getUser(userId);
		final ClientPlatform clientPlatform = clientPlatformRepository.findById(id);
		if (user.equals(clientPlatform.getUser())
				|| user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString()))
			return true;
		else {
			return resourceService.hasAccess(userId, id, ResourceAccessType.VIEW);
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
				}
			}

			return tokenService.generateTokenForClient(clientPlatform);
		} else {
			throw new ClientPlatformServiceException("Platform Client already exists");
		}
	}
	
	private void metricsManagerLogControlPanelClientsPlatformCreation(String userId, String result) {
		if (null != this.metricsManager) {
			this.metricsManager.logControlPanelClientsPlatformCreation(userId, result);
		}
	}
	

}
