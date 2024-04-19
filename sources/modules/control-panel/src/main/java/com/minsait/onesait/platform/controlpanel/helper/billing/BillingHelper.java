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
package com.minsait.onesait.platform.controlpanel.helper.billing;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.model.Configuration.Type;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BillingHelper {

	private static final String READ_ERROR = null;

	@Autowired
	private IntegrationResourcesService integrationResourcesService;

	@Value("${onesaitplatform.multitenancy.enabled}")
	private String MULTITENANCY;

	Type type;

	public List<ModuleStatus> getModuleStatus() {

		final List<ModuleStatus> list = new ArrayList<>();

		final Configuration config = integrationResourcesService.getBillableModules();

		if (config == null) {
			log.error("It doesn't exist the BillableModules Centralized Configuration ");
			return list;
		}
		final String json = config.getYmlConfig();
		JsonNode actualObj;
		try {
			final ObjectMapper mapper = new ObjectMapper();
			actualObj = mapper.readTree(json);
		} catch (final IOException e) {
			log.error("Error when parsing the Json", e);
			return list;
		}
		for (final JsonNode jsonnode : actualObj) {
			try {
				final ModuleStatus status = new ModuleStatus();

				Socket clientSocket;
				try {
					clientSocket = new Socket(jsonnode.get("servicename").asText() + "." + jsonnode.get("stack").asText(), jsonnode.get("port").asInt());
				}catch(NullPointerException e) {
					clientSocket = new Socket(jsonnode.get("servicename").asText(), jsonnode.get("port").asInt());

				}
				status.setIdentification(jsonnode.get("module").asText());
				status.setName(jsonnode.get("name").asText());
				if (clientSocket.isConnected()) {
					status.setStatus(true);
				} else {
					status.setStatus(false);
				}

				list.add(status);
				clientSocket.close();
			} catch (final IOException e) {
				final ModuleStatus status = new ModuleStatus();
				status.setIdentification(jsonnode.get("module").asText());
				status.setName(jsonnode.get("name").asText());
				status.setStatus(false);
				list.add(status);
			}
		}
		final ModuleStatus status = new ModuleStatus();
		status.setIdentification("multitenant");
		status.setName("Multitenant");
		if (MULTITENANCY.equals("true")) {
			status.setStatus(true);
		} else {
			status.setStatus(false);
		}
		list.add(status);
		return list;
	}

}
