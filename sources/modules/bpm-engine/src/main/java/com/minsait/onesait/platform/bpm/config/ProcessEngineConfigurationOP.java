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
package com.minsait.onesait.platform.bpm.config;

import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.configuration.Ordering;
import org.camunda.bpm.spring.boot.starter.configuration.impl.AbstractCamundaConfiguration;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordering.DEFAULT_ORDER + 1)
public class ProcessEngineConfigurationOP extends AbstractCamundaConfiguration {
	@Override
	public void preInit(SpringProcessEngineConfiguration processEngineConfiguration) {
		processEngineConfiguration.setGroupResourceWhitelistPattern("[a-zA-Z0-9-._]+");
		processEngineConfiguration.setUserResourceWhitelistPattern("[a-zA-Z0-9-_.@]+");
		processEngineConfiguration.setTenantResourceWhitelistPattern("[a-zA-Z0-9-._@]+");
	}
}