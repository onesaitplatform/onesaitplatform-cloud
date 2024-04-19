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
package com.minsait.onesait.platform.controlpanel.controller.multitenant;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.controlpanel.services.project.MSAServiceDispatcher;
import com.minsait.onesait.platform.multitenant.config.services.MultitenantConfigurationService;
import com.minsait.onesait.platform.multitenant.pojo.CaaSConfiguration;

@Component
public class MultitenantVerticalDeployer {

	@Autowired
	private MultitenantConfigurationService multitenantConfigurationService;
	@Autowired
	private MSAServiceDispatcher msaServiceDispatcher;

	private static final Map<String, Boolean> verticalCreation = new HashMap<>();

	public void createVertical(String verticalSchema, String multitenantAPIKey) {
		final CaaSConfiguration config = multitenantConfigurationService.getMultitenantCaaSConfiguration();
		if (config != null) {
			msaServiceDispatcher.dispatch(config.getType()).runConfigInit(config.getServer(), config.getUsername(),
					config.getCredentials(), config.getNamespace(), verticalSchema, multitenantAPIKey,
					verticalCreation);
		} else {
			throw new RuntimeException("Multitenant config for CaaS does not exist, run config init manually");
		}
	}

	public boolean hasFinished(String vertical) {
		final Boolean b = verticalCreation.get(vertical);
		if (b != null) {
			if (b) {
				verticalCreation.remove(vertical);
				return true;
			} else {
				return false;
			}
		}

		return true;
	}

}
