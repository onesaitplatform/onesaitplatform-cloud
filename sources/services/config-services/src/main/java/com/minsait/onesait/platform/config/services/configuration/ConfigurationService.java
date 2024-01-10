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
package com.minsait.onesait.platform.config.services.configuration;

import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;

import com.minsait.onesait.platform.config.components.AIConfiguration;
import com.minsait.onesait.platform.config.components.BundleConfiguration;
import com.minsait.onesait.platform.config.components.CaasConfiguration;
import com.minsait.onesait.platform.config.components.GlobalConfiguration;
import com.minsait.onesait.platform.config.components.GoogleAnalyticsConfiguration;
import com.minsait.onesait.platform.config.components.JenkinsConfiguration;
import com.minsait.onesait.platform.config.components.MailConfiguration;
import com.minsait.onesait.platform.config.components.OpenshiftConfiguration;
import com.minsait.onesait.platform.config.components.RancherConfiguration;
import com.minsait.onesait.platform.config.components.TwitterConfiguration;
import com.minsait.onesait.platform.config.components.Urls;
import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.git.GitlabConfiguration;

public interface ConfigurationService {

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	List<Configuration> getAllConfigurations();

	List<Configuration> getAllConfigurations(User user);

	void deleteConfiguration(String id);

	Configuration getConfiguration(String id);

	Configuration getConfigurationByIdentification(String identification);

	List<Configuration> getConfigurations(Configuration.Type configurationTypeId);

	List<Configuration> getConfigurations(Configuration.Type configurationTypeId, User user);

	Configuration getConfiguration(Configuration.Type configurationType, String environment, String suffix);

	Configuration getConfiguration(Configuration.Type configurationType, String suffix);

	MailConfiguration getMailConfiguration(String environment);

	TwitterConfiguration getTwitterConfiguration(String environment, String suffix);

	GitlabConfiguration getGitlabConfiguration(String id);

	GitlabConfiguration getGitlabConfiguration(String suffix, String environment);

	GitlabConfiguration getDefautlGitlabConfiguration();

	JenkinsConfiguration getJenkinsConfiguration(String environment);

	JenkinsConfiguration getDefaultJenkinsConfiguration();

	String getDefaultJenkinsXML(String suffix);

	RancherConfiguration getRancherConfiguration(String id);

	RancherConfiguration getRancherConfiguration(String suffix, String environment);

	RancherConfiguration getDefaultRancherConfiguration();

	OpenshiftConfiguration getOpenshiftConfiguration(String id);

	List<Configuration.Type> getAllConfigurationTypes(User user);

	Configuration createConfiguration(Configuration configuration);

	boolean existsConfiguration(Configuration configuration);

	void updateConfiguration(Configuration configuration);

	boolean isValidYaml(final String yaml);

	Map fromYaml(final String yaml);

	Configuration getConfigurationByDescription(String descrption);

	Urls getEndpointsUrls(String environment);

	GlobalConfiguration getGlobalConfiguration(String environment);

	GoogleAnalyticsConfiguration getGoogleAnalyticsConfiguration(String environment);

	CaasConfiguration getCaasConfiguration(String id);

	BundleConfiguration getBundleConfiguration();

	AIConfiguration getAIConfiguration();

}
