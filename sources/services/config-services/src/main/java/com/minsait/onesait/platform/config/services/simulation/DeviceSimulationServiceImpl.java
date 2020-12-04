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
package com.minsait.onesait.platform.config.services.simulation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformInstanceSimulation;
import com.minsait.onesait.platform.config.model.ClientPlatformOntology;
import com.minsait.onesait.platform.config.model.Token;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.ClientPlatformInstanceSimulationRepository;
import com.minsait.onesait.platform.config.repository.ClientPlatformOntologyRepository;
import com.minsait.onesait.platform.config.repository.ClientPlatformRepository;
import com.minsait.onesait.platform.config.repository.TokenRepository;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.user.UserService;

@Service
public class DeviceSimulationServiceImpl implements DeviceSimulationService {

	@Autowired
	private TokenRepository tokenRepository;
	@Autowired
	private ClientPlatformRepository clientPlatformRepository;
	@Autowired
	private ClientPlatformOntologyRepository clientPlatformOntologyRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private ClientPlatformInstanceSimulationRepository deviceSimulationRepository;

	@Override
	public List<String> getClientsForUser(String userId) {
		final List<String> clientIdentifications = new ArrayList<>();
		List<ClientPlatform> clients = null;
		final User user = userService.getUser(userId);
		if (userService.isUserAdministrator(user))
			clients = clientPlatformRepository.findAll();
		else
			clients = userService.getClientsForUser(user);
		//
		for (final ClientPlatform client : clients) {
			clientIdentifications.add(client.getIdentification());
		}
		return clientIdentifications;
	}

	@Override
	public List<String> getClientTokensIdentification(String clientPlatformId) {
		final ClientPlatform clientPlatform = clientPlatformRepository.findByIdentification(clientPlatformId);
		final List<String> tokens = new ArrayList<>();
		for (final Token token : tokenRepository.findByClientPlatform(clientPlatform)) {
			tokens.add(token.getTokenName());
		}
		return tokens;
	}

	@Override
	public List<String> getClientOntologiesIdentification(String clientPlatformId) {
		final List<String> ontologies = new ArrayList<>();
		for (final ClientPlatformOntology clientPlatformOntology : clientPlatformOntologyRepository
				.findByClientPlatformAndInsertAccess(clientPlatformId)) {
			ontologies.add(clientPlatformOntology.getOntology().getIdentification());
		}
		return ontologies;
	}

	@Override
	public List<String> getSimulatorTypes() {
		final List<String> simulators = new ArrayList<>();
		for (final ClientPlatformInstanceSimulation.Type type : ClientPlatformInstanceSimulation.Type.values()) {
			simulators.add(type.name());
		}
		return simulators;
	}

	@Override
	public List<ClientPlatformInstanceSimulation> getAllSimulations() {
		return deviceSimulationRepository.findAll();
	}

	@Override
	public ClientPlatformInstanceSimulation getSimulatorByIdentification(String identification) {
		return deviceSimulationRepository.findByIdentification(identification);
	}

	@Override
	public ClientPlatformInstanceSimulation createSimulation(String identification, int interval, String userId,
			String json) throws IOException {

		final ObjectMapper mapper = new ObjectMapper();
		final ClientPlatformInstanceSimulation simulation = new ClientPlatformInstanceSimulation();

		simulation.setOntology(
				ontologyService.getOntologyByIdentification(mapper.readTree(json).path("ontology").asText(), userId));
		simulation.setClientPlatform(
				clientPlatformRepository.findByIdentification(mapper.readTree(json).path("clientPlatform").asText()));
		simulation.setToken(tokenRepository.findByTokenName(mapper.readTree(json).path("token").asText()));
		simulation.setIdentification(identification);
		simulation.setJson(json);
		simulation.setInterval(interval);

		int minutes = 0;
		int seconds = interval;
		if (interval >= 0) {
			for (int i = 0; i < (interval / 60); i++) {
				minutes++;
				seconds = seconds - 60;
			}
		}
		if (minutes == 0)
			simulation.setCron("0/" + seconds + " * * ? * * *");
		else
			simulation.setCron("0/" + seconds + " 0/" + minutes + " * ? * * *");
		simulation.setActive(false);
		simulation.setUser(userService.getUser(userId));
		return deviceSimulationRepository.save(simulation);

	}

	@Override
	public ClientPlatformInstanceSimulation updateSimulation(String identification, int interval, String json,
			ClientPlatformInstanceSimulation simulation) throws IOException {
		final ObjectMapper mapper = new ObjectMapper();
		simulation.setOntology(ontologyService.getOntologyByIdentification(
				mapper.readTree(json).path("ontology").asText(), simulation.getUser().getUserId()));
		simulation.setClientPlatform(
				clientPlatformRepository.findByIdentification(mapper.readTree(json).path("clientPlatform").asText()));
		simulation.setToken(tokenRepository.findByTokenName(mapper.readTree(json).path("token").asText()));
		simulation.setIdentification(identification);
		simulation.setJson(json);
		simulation.setInterval(interval);

		int minutes = 0;
		int seconds = interval;
		if (interval >= 0) {
			for (int i = 0; i < (interval / 60); i++) {
				minutes++;
				seconds = seconds - 60;
			}
		}
		if (minutes == 0)
			simulation.setCron("0/" + seconds + " * * ? * * *");
		else
			simulation.setCron("0/" + seconds + " 0/" + minutes + " * ? * * *");
		simulation.setActive(false);
		return deviceSimulationRepository.save(simulation);
	}

	@Override
	public void save(ClientPlatformInstanceSimulation simulation) {
		deviceSimulationRepository.save(simulation);
	}

	@Override
	public ClientPlatformInstanceSimulation getSimulationById(String id) {

		return deviceSimulationRepository.findById(id).orElse(null);
	}

	@Override
	public List<ClientPlatformInstanceSimulation> getSimulationsForUser(String userId) {

		return deviceSimulationRepository.findByUser(userService.getUser(userId));
	}

	@Override
	public ClientPlatformInstanceSimulation getSimulationByJobName(String jobName) {
		return deviceSimulationRepository.findByJobName(jobName);
	}

}
