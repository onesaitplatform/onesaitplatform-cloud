/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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
package com.minsait.onesait.platform.business.services.ontology;

import java.net.Socket;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OntologyServiceStatusBean {

	private static final String MINIO = "historicaldb";

	private static final String PRESTO = "presto";

	private static final String NEBULAGRAPH = "nebulagraph";

	public static final String MODULE_NOT_ACTIVE_KEY="moduleNotActive";

	@Autowired
	private IntegrationResourcesService integrationResourcesService;

	private JsonNode modulesConfig;

	@PostConstruct
	public void loadConfig() {
		final Configuration config = integrationResourcesService.getBillableModules();
		if (config != null) {
			try {
				modulesConfig = new ObjectMapper().readTree(config.getYmlConfig());
			} catch (final Exception e) {
				log.error("Could not load billable modules config");
			}
		}
	}

	public boolean isNebulaGraphActive() {
		return isActive(NEBULAGRAPH);
	}

	public boolean isPrestoActive() {
		return isActive(PRESTO);
	}

	public boolean isMinIOActive() {
		return isActive(MINIO);
	}

	private boolean isActive(String module) {
		final JsonNode m = getModule(module);
		if (m != null) {
			try (Socket clientSocket = new Socket(m.get("servicename").asText(), m.get("port").asInt())) {
				if (clientSocket.isConnected()) {
					return true;
				}
			} catch (final Exception e) {
				log.warn("Module {} is not active", m.get("module").asText(), e.getMessage());
			}
		}
		return false;
	}

	private JsonNode getModule(String filter) {
		for (final JsonNode module : modulesConfig) {
			if (module.get("module").asText().contains(filter)) {
				return module;
			}
		}
		return null;
	}
}
