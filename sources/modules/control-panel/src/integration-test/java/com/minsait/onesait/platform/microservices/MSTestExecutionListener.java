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
package com.minsait.onesait.platform.microservices;

import static com.minsait.onesait.platform.microservices.MicroserviceIntegrationTest.MS_NAME;

import java.util.Map;

import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import com.minsait.onesait.platform.config.components.JenkinsConfiguration;
import com.minsait.onesait.platform.controlpanel.services.jenkins.JenkinsService;
import com.minsait.onesait.platform.controlpanel.services.project.MicroservicesGitRestService;
import com.minsait.onesait.platform.git.GitlabConfiguration;

public class MSTestExecutionListener extends AbstractTestExecutionListener {

	@Override
	public void afterTestClass(TestContext testContext) throws Exception {
		// By default when deleting microservices pipelines are not removed
		final JenkinsService jenkinsService = testContext.getApplicationContext().getBean(JenkinsService.class);
		final JenkinsConfiguration jenkinsConfiguration = testContext.getApplicationContext()
				.getBean(JenkinsConfiguration.class);
		final Map<String, MicroservicesGitRestService> gitlabService = testContext.getApplicationContext()
				.getBeansOfType(MicroservicesGitRestService.class);
		final GitlabConfiguration gitlabConfiguration = testContext.getApplicationContext()
				.getBean(GitlabConfiguration.class);
		jenkinsService.deleteJob(jenkinsConfiguration.getJenkinsUrl(), jenkinsConfiguration.getUsername(),
				jenkinsConfiguration.getToken(), MS_NAME, null);
		gitlabService.entrySet().iterator().next().getValue().deleteGitlabProject(MS_NAME, MS_NAME,
				gitlabConfiguration.getSite(), gitlabConfiguration.getPrivateToken());
	}

}
