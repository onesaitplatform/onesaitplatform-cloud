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
package com.minsait.onesait.platform.config.services.notebook.configuration;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
public class NotebookServiceConfiguration {
	@Value("${onesaitplatform.analytics.notebook.zeppelinProtocol:http}")
	private String zeppelinProtocol;
	@Value("${onesaitplatform.analytics.notebook.zeppelinHostname:localhost}")
	private String zeppelinHostname;
	@Value("${onesaitplatform.analytics.notebook.zeppelinPort:8080}")
	private int zeppelinPort;
	@Value("${onesaitplatform.analytics.notebook.zeppelinPathname:}")
	private String zeppelinPathname;
	@Value("${onesaitplatform.analytics.notebook.shiroAdminUsername:#{null}}")
	private String zeppelinShiroAdminUsername;
	@Value("${onesaitplatform.analytics.notebook.shiroAdminPass:#{null}}")
	private String zeppelinShiroAdminPass;
	@Value("${onesaitplatform.analytics.notebook.shiroUsername:#{null}}")
	private String zeppelinShiroUsername;
	@Value("${onesaitplatform.analytics.notebook.shiroPass:#{null}}")
	private String zeppelinShiroPass;
	@Value("${onesaitplatform.analytics.notebook.restUsername:#{null}}")
	private String restUsername;
	@Value("${onesaitplatform.analytics.notebook.restPass:#{null}}")
	private String restPass;
	@Value("${onesaitplatform.analytics.notebook.zeppelinGlobalTimeout:#{120000}}")
	private String globalTimeout;
	private String baseURL;

	@PostConstruct
	public void init() {
		baseURL = String.format("%s://%s:%s/%s", zeppelinProtocol, zeppelinHostname, zeppelinPort, zeppelinPathname);
	}
}