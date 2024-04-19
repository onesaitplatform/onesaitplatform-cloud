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

import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.impl.plugin.AdministratorAuthorizationPlugin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.minsait.onesait.platform.bpm.model.Groups;

@Configuration
public class BPMGeneralConfig {

	@Value("${onesaitplatform.camunda.admin-user:administrator}")
	private String adminUser;

	@Bean
	public ProcessEnginePlugin administratorAuthorizationPlugin() {
		final AdministratorAuthorizationPlugin administratorAuthorizationPlugin = new AdministratorAuthorizationPlugin();
		administratorAuthorizationPlugin.setAdministratorUserName(adminUser);
		administratorAuthorizationPlugin.setAdministratorGroupName(Groups.CAMUNDA_ADMIN.getValue());
		return administratorAuthorizationPlugin;
	}

}
