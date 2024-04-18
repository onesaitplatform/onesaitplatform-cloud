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
package com.minsait.onesait.platform.microservices;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.minsait.onesait.platform.config.components.JenkinsConfiguration;
import com.minsait.onesait.platform.config.components.RancherConfiguration;
import com.minsait.onesait.platform.git.GitlabConfiguration;

@TestConfiguration
public class MSTestConfiguration {

	@ConfigurationProperties(prefix = "rancher")
	@Bean
	public RancherConfiguration rancherConfiguration() {
		return new RancherConfiguration();
	}

	@ConfigurationProperties(prefix = "jenkins")
	@Bean
	public JenkinsConfiguration jenkinsConfiguration() {
		return new JenkinsConfiguration();
	}

	@ConfigurationProperties(prefix = "gitlab")
	@Bean
	public GitlabConfiguration gitlabConfiguration() {
		return new GitlabConfiguration();
	}

}
