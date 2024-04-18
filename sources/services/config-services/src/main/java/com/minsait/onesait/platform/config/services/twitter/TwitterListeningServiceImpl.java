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
		return this.twitterListeningRepository.findAll();
	}

	@Override
	public List<TwitterListening> getAllListeningsByUser(String userId) {
		User user = userService.getUser(userId);
		return this.twitterListeningRepository.findByUser(user);
	}

	@Override
	public TwitterListening getListenById(String id) {
		return this.twitterListeningRepository.findById(id);
	}

	@Override
	public TwitterListening getListenByIdentificator(String identificator) {
		return this.twitterListeningRepository.findByIdentification(identificator);
	}

	@Override
	public List<Configuration> getAllConfigurations() {
		return this.configurationService.getConfigurations(Configuration.Type.TWITTER);
	}

	@Override
	public List<Configuration> getConfigurationsByUserId(String userId) {
		List<Configuration> configurationsByUser = new ArrayList<>();
		for (Configuration configuration : this.getAllConfigurations()) {
			if (configuration.getUser().getUserId().equals(userId))
				configurationsByUser.add(configuration);
		}

		return configurationsByUser;

	}

	@Override
	public List<String> getClientsFromOntology(String ontologyId, String userSessionId) {
		Ontology ontology = this.ontologyService.getOntologyByIdentification(ontologyId, userSessionId);
		List<String> clients = new ArrayList<>();
		for (ClientPlatformOntology clientPlatform : this.clientPlatformOntologyRepository.findByOntology(ontology)) {
			clients.add(clientPlatform.getClientPlatform().getIdentification());
		}
		return clients;
	}

	@Override
	public List<String> getTokensFromClient(String clientPlatformId) {
		ClientPlatform clientPlatform = this.clientPlatformRepository.findByIdentification(clientPlatformId);
		List<String> tokens = new ArrayList<>();
		for (Token token : this.tokenRepository.findByClientPlatform(clientPlatform)) {
			tokens.add(token.getTokenName());
		}
		return tokens;
	}

	@Override
	public TwitterListening createListening(TwitterListening twitterListening, String userSessionId) {
		if (twitterListening.getOntology().getId() == null)
			twitterListening.setOntology(this.ontologyService
					.getOntologyByIdentification(twitterListening.getOntology().getIdentification(), userSessionId));
		if (twitterListening.getToken().getId() == null)
			twitterListening.setToken(this.tokenRepository.findByTokenName(twitterListening.getToken().getTokenName()));
		if (twitterListening.getConfiguration().getId() == null)
			twitterListening.setConfiguration(this.configurationService
					.getConfigurationByDescription(twitterListening.getConfiguration().getDescription()));

		twitterListening = this.twitterListeningRepository.save(twitterListening);
		return twitterListening;

	}

	@Override

	public void updateListening(TwitterListening twitterListening) {
		TwitterListening newTwitterListening = this.twitterListeningRepository.findById(twitterListening.getId());
		if (newTwitterListening != null) {
			newTwitterListening.setIdentification(twitterListening.getIdentification());
			newTwitterListening.setConfiguration(this.configurationService
					.getConfigurationByDescription(twitterListening.getConfiguration().getDescription()));
			newTwitterListening.setTopics(twitterListening.getTopics());
			newTwitterListening.setDateFrom(twitterListening.getDateFrom());
			newTwitterListening.setDateTo(twitterListening.getDateTo());
			// newTwitterListening.setJobName(twitterListening.getJobName());
			this.twitterListeningRepository.save(newTwitterListening);
		}

	}

	@Override
	public boolean existOntology(String identification, String userSessionId) {
		return (this.ontologyService.getOntologyByIdentification(identification, userSessionId) != null);
	}

	@Override
	public boolean existClientPlatform(String identification) {
		return (this.clientPlatformRepository.findByIdentification(identification) != null);
	}

	@Override
	public Ontology createTwitterOntology(String ontologyId) {
		DataModel dataModelTwitter = this.dataModelRepository.findByIdentification("Twitter").get(0);
		Ontology ontology = new Ontology();
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
		return this.twitterListeningRepository.findByJobName(jobName);
	}

	@Override
	public List<String> getAllClientsForUser(User userSessionId) {
		List<String> clients = new ArrayList<>();
		for (ClientPlatform client : this.clientPlatformRepository.findByUser(userSessionId)) {
			clients.add(client.getIdentification());
		}
		return clients;
	}

}
