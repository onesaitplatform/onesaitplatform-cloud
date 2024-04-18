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
package com.minsait.onesait.platform.controlpanel.services.jenkins;

import java.util.List;
import java.util.Map;

import com.cdancy.jenkins.rest.JenkinsClient;
import com.cdancy.jenkins.rest.domain.job.BuildInfo;
import com.cdancy.jenkins.rest.domain.job.JobInfo;
import com.minsait.onesait.platform.config.components.JenkinsConfiguration;

public interface JenkinsService {

	public JobInfo getJobInfo(String jenkinsUrl, String username, String token, String jobName, String folderName);

	public void createJob(String jenkinsUrl, String username, String token, String jobName, String folderName,
			String jobConfigXML);

	public void createJobFolder(String jenkinsUrl, String username, String token, String folderName,
			String folderConfigXML);

	public void deleteJob(String jenkinsUrl, String username, String token, String jobName, String folderName);

	public int buildWithParameters(String jenkinsUrl, String username, String token, String jobName, String folderName,
			Map<String, List<String>> parameters);

	public Map<String, Object> getParametersFromJob(String jenkinsUrl, String username, String token, String jobName);

	public BuildInfo buildInfo(JenkinsConfiguration config, String jobName, String folderName, int queueId);

	public BuildInfo lastBuildInfo(JenkinsConfiguration config, String jobName, String folderName);

	public JenkinsClient getJenkinsClient(String jenkinsUrl, String username, String token);

	public JenkinsClient getJenkinsClient(String jenkinsUrl);

	public int buildWithParametersNoAuth(String jenkinsUrl, String jobName, String folderName,
			Map<String, List<String>> parameters) throws Exception;

	public BuildInfo buildInfo(String jenkinsUrl, String jobName, String folderName, int queueId);

	public boolean viewExists(String jenkinsUrl, String username, String token, String viewName);

	public void createView(String jenkinsUrl, String username, String token, String viewName);

	public void deleteView(String jenkinsUrl, String username, String token, String viewName);

	public void createJobInView(String jenkinsUrl, String username, String token, String jobName, String viewName,
			String jobConfigXML);

	public String getCrumbCSRF(String jenkinsUrl, String username, String token);
}
