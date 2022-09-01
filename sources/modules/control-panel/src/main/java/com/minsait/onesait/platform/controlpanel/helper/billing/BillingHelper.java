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
import com.minsait.onesait.platform.commons.ActiveProfileDetector;
import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.model.Configuration.Type;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BillingHelper {

	private static final String READ_ERROR = null;

	@Autowired
	ConfigurationService configservice;

	@Autowired
	ActiveProfileDetector profiledetector;

	@Value("${onesaitplatform.multitenancy.enabled}")
	private String MULTITENANCY;

	Type type;

	public List<ModuleStatus> getModuleStatus() {

		List<ModuleStatus> list = new ArrayList<>();

		Configuration config = configservice.getConfiguration(Type.ENDPOINT_MODULES, profiledetector.getActiveProfile(),
				"BillableModules");
		if (config == null) {
			log.error("It doesn't exist the BillableModules Centralized Configuration ");
			return list;
		}
		String json = config.getYmlConfig();
		JsonNode actualObj;
		try {
			ObjectMapper mapper = new ObjectMapper();
			actualObj = mapper.readTree(json);
		} catch (IOException e) {
			log.error("Error when parsing the Json", e);
			return list;
		}
		for (JsonNode jsonnode : actualObj) {
			try {
				ModuleStatus status = new ModuleStatus();

				Socket clientSocket = new Socket(jsonnode.get("servicename").asText(), jsonnode.get("port").asInt());
				status.setIdentification(jsonnode.get("module").asText());
				status.setName(jsonnode.get("name").asText());
				if (clientSocket.isConnected()) {
					status.setStatus(true);
				} else {
					status.setStatus(false);
				}

				list.add(status);
				clientSocket.close();
			} catch (IOException e) {
				ModuleStatus status = new ModuleStatus();
				status.setIdentification(jsonnode.get("module").asText());
				status.setName(jsonnode.get("name").asText());
				status.setStatus(false);
				list.add(status);
			}
		}
		ModuleStatus status = new ModuleStatus();
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
