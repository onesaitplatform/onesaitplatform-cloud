/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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
package com.minsait.onesait.platform.config.services.twitter;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformOntology;
import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.model.DataModel;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Token;
import com.minsait.onesait.platform.config.model.TwitterListening;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.ClientPlatformOntologyRepository;
import com.minsait.onesait.platform.config.repository.ClientPlatformRepository;
import com.minsait.onesait.platform.config.repository.DataModelRepository;
import com.minsait.onesait.platform.config.repository.TokenRepository;
import com.minsait.onesait.platform.config.repository.TwitterListeningRepository;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.user.UserService;

@Service
public class TwitterListeningServiceImpl implements TwitterListeningService {

	@Autowired
	private TwitterListeningRepository twitterListeningRepository;
	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private ClientPlatformRepository clientPlatformRepository;
	@Autowired
	private ClientPlatformOntologyRepository clientPlatformOntologyRepository;
	@Autowired
	private DataModelRepository dataModelRepository;
	@Autowired
	private TokenRepository tokenRepository;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private UserService userService;

	@Override
	public List<TwitterListening> getAllListenings() {
		return twitterListeningRepository.findAll();
	}

	@Override
	public List<TwitterListening> getAllListeningsByUser(String userId) {
		final User user = userService.getUser(userId);
		return twitterListeningRepository.findByUser(user);
	}

	@Override
	public TwitterListening getListenById(String id) {
		return twitterListeningRepository.findById(id).orElse(null);
	}

	@Override
	public TwitterListening getListenByIdentificator(String identificator) {
		return twitterListeningRepository.findByIdentification(identificator);
	}

	@Override
	public List<Configuration> getAllConfigurations() {
		return configurationService.getConfigurations(Configuration.Type.TWITTER);
	}

	@Override
	public List<Configuration> getConfigurationsByUserId(String userId) {
		final List<Configuration> configurationsByUser = new ArrayList<>();
		for (final Configuration configuration : getAllConfigurations()) {
			if (configuration.getUser().getUserId().equals(userId))
				configurationsByUser.add(configuration);
		}

		return configurationsByUser;

	}

	@Override
	public List<String> getClientsFromOntology(String ontologyId, String userSessionId) {
		final Ontology ontology = ontologyService.getOntologyByIdentification(ontologyId, userSessionId);
		final List<String> clients = new ArrayList<>();
		for (final ClientPlatformOntology clientPlatform : clientPlatformOntologyRepository.findByOntology(ontology)) {
			clients.add(clientPlatform.getClientPlatform().getIdentification());
		}
		return clients;
	}

	@Override
	public List<String> getTokensFromClient(String clientPlatformId) {
		final ClientPlatform clientPlatform = clientPlatformRepository.findByIdentification(clientPlatformId);
		final List<String> tokens = new ArrayList<>();
		for (final Token token : tokenRepository.findByClientPlatform(clientPlatform)) {
			tokens.add(token.getTokenName());
		}
		return tokens;
	}

	@Override
	public TwitterListening createListening(TwitterListening twitterListening, String userSessionId) {
		if (twitterListening.getOntology().getId() == null)
			twitterListening.setOntology(ontologyService
					.getOntologyByIdentification(twitterListening.getOntology().getIdentification(), userSessionId));
		if (twitterListening.getToken().getId() == null)
			twitterListening.setToken(tokenRepository.findByTokenName(twitterListening.getToken().getTokenName()));
		if (twitterListening.getConfiguration().getId() == null)
			twitterListening.setConfiguration(configurationService
					.getConfigurationByDescription(twitterListening.getConfiguration().getDescription()));

		twitterListening = twitterListeningRepository.save(twitterListening);
		return twitterListening;

	}

	@Override

	public void updateListening(TwitterListening twitterListening) {
		final TwitterListening newTwitterListening = twitterListeningRepository.findById(twitterListening.getId())
				.orElse(null);
		if (newTwitterListening != null) {
			newTwitterListening.setIdentification(twitterListening.getIdentification());
			newTwitterListening.setConfiguration(configurationService
					.getConfigurationByDescription(twitterListening.getConfiguration().getDescription()));
			newTwitterListening.setTopics(twitterListening.getTopics());
			newTwitterListening.setDateFrom(twitterListening.getDateFrom());
			newTwitterListening.setDateTo(twitterListening.getDateTo());
			// newTwitterListening.setJobName(twitterListening.getJobName());
			twitterListeningRepository.save(newTwitterListening);
		}

	}

	@Override
	public boolean existOntology(String identification, String userSessionId) {
		return (ontologyService.getOntologyByIdentification(identification, userSessionId) != null);
	}

	@Override
	public boolean existClientPlatform(String identification) {
		return (clientPlatformRepository.findByIdentification(identification) != null);
	}

	@Override
	public Ontology createTwitterOntology(String ontologyId) {
		final DataModel dataModelTwitter = dataModelRepository.findByIdentification("Twitter").get(0);
		final Ontology ontology = new Ontology();
		ontology.setIdentification(ontologyId);
		if (dataModelTwitter.getType().equals(DataModel.MainType.SOCIAL_MEDIA.toString()))
			ontology.setDescription("Ontology created for tweet recollection");
		ontology.setJsonSchema(dataModelTwitter.getJsonSchema());
		ontology.setDataModel(dataModelTwitter);
		ontology.setActive(true);
		ontology.setPublic(false);
		ontology.setRtdbClean(false);
		ontology.setRtdbToHdb(false);
		return ontology;

	}

	@Override
	public TwitterListening getListeningByJobName(String jobName) {
		return twitterListeningRepository.findByJobName(jobName);
	}

	@Override
	public List<String> getAllClientsForUser(User userSessionId) {
		final List<String> clients = new ArrayList<>();
		for (final ClientPlatform client : clientPlatformRepository.findByUser(userSessionId)) {
			clients.add(client.getIdentification());
		}
		return clients;
	}

}
