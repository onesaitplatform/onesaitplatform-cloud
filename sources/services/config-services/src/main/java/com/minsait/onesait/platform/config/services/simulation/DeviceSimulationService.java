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
package com.minsait.onesait.platform.config.services.simulation;

import java.io.IOException;
import java.util.List;

import com.minsait.onesait.platform.config.model.ClientPlatformInstanceSimulation;

public interface DeviceSimulationService {

	List<String> getClientsForUser(String userId);

	List<String> getClientTokensIdentification(String clientPlatformId);

	List<String> getClientOntologiesIdentification(String clientPlatformId);

	List<String> getSimulatorTypes();

	List<ClientPlatformInstanceSimulation> getAllSimulations();

	ClientPlatformInstanceSimulation getSimulatorByIdentification(String identification);

	ClientPlatformInstanceSimulation createSimulation(String identification, int interval, String userId, String json)
			throws IOException;

	void save(ClientPlatformInstanceSimulation simulation);

	ClientPlatformInstanceSimulation getSimulationById(String id);

	List<ClientPlatformInstanceSimulation> getSimulationsForUser(String userId);

	ClientPlatformInstanceSimulation updateSimulation(String identification, int interval, String json, ClientPlatformInstanceSimulation simulation)
			throws IOException;

	ClientPlatformInstanceSimulation getSimulationByJobName(String jobName);
}
