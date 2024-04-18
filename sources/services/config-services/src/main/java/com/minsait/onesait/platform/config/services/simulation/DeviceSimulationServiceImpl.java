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
import com.minsait.onesait.platform.config.model.DeviceSimulation;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.Token;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.ClientPlatformRepository;
import com.minsait.onesait.platform.config.repository.DeviceSimulationRepository;
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
	private UserService userService;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private DeviceSimulationRepository deviceSimulationRepository;

	@Override
	public List<String> getClientsForUser(String userId) {
		List<String> clientIdentifications = new ArrayList<>();
		List<ClientPlatform> clients = null;
		User user = this.userService.getUser(userId);
		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name())) 
			clients = this.clientPlatformRepository.findAll();
		else 
			clients = this.userService.getClientsForUser(user);
		//
		for (ClientPlatform client : clients) {
			clientIdentifications.add(client.getIdentification());
		}
		return clientIdentifications;
	}

	@Override
	public List<String> getClientTokensIdentification(String clientPlatformId) {
		ClientPlatform clientPlatform = this.clientPlatformRepository.findByIdentification(clientPlatformId);
		List<String> tokens = new ArrayList<>();
		for (Token token : this.tokenRepository.findByClientPlatform(clientPlatform)) {
			tokens.add(token.getTokenName());
		}
		return tokens;
	}

	@Override
	public List<String> getClientOntologiesIdentification(String clientPlatformId) {
		List<String> ontologies = new ArrayList<>();
		for (Ontology ontology : this.ontologyService
				.getOntologiesByClientPlatform(this.clientPlatformRepository.findByIdentification(clientPlatformId))) {
			ontologies.add(ontology.getIdentification());
		}
		return ontologies;
	}

	@Override
	public List<String> getSimulatorTypes() {
		List<String> simulators = new ArrayList<>();
		for (DeviceSimulation.Type type : DeviceSimulation.Type.values()) {
			simulators.add(type.name());
		}
		return simulators;
	}

	@Override
	public List<DeviceSimulation> getAllSimulations() {
		return this.deviceSimulationRepository.findAll();
	}

	@Override
	public DeviceSimulation getSimulatorByIdentification(String identification) {
		return this.deviceSimulationRepository.findByIdentification(identification);
	}

	@Override
	public DeviceSimulation createSimulation(String identification, int interval, String userId, String json)
			throws IOException {

		ObjectMapper mapper = new ObjectMapper();
		DeviceSimulation simulation = new DeviceSimulation();

		simulation.setOntology(this.ontologyService
				.getOntologyByIdentification(mapper.readTree(json).path("ontology").asText(), userId));
		simulation.setClientPlatform(this.clientPlatformRepository
				.findByIdentification(mapper.readTree(json).path("clientPlatform").asText()));
		simulation.setToken(this.tokenRepository.findByTokenName(mapper.readTree(json).path("token").asText()));
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
		simulation.setUser(this.userService.getUser(userId));
		return this.deviceSimulationRepository.save(simulation);

	}

	@Override
	public DeviceSimulation updateSimulation(String identification, int interval, String json,
			DeviceSimulation simulation) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		simulation.setOntology(this.ontologyService.getOntologyByIdentification(
				mapper.readTree(json).path("ontology").asText(), simulation.getUser().getUserId()));
		simulation.setClientPlatform(this.clientPlatformRepository
				.findByIdentification(mapper.readTree(json).path("clientPlatform").asText()));
		simulation.setToken(this.tokenRepository.findByTokenName(mapper.readTree(json).path("token").asText()));
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
		return this.deviceSimulationRepository.save(simulation);
	}

	@Override
	public void save(DeviceSimulation simulation) {
		this.deviceSimulationRepository.save(simulation);
	}

	@Override
	public DeviceSimulation getSimulationById(String id) {

		return this.deviceSimulationRepository.findById(id);
	}

	@Override
	public List<DeviceSimulation> getSimulationsForUser(String userId) {

		return this.deviceSimulationRepository.findByUser(this.userService.getUser(userId));
	}

	@Override
	public DeviceSimulation getSimulationByJobName(String jobName) {
		return this.deviceSimulationRepository.findByJobName(jobName);
	}

}
