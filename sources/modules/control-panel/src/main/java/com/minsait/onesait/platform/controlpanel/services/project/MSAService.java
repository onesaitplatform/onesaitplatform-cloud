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
package com.minsait.onesait.platform.controlpanel.services.project;

import java.util.List;
import java.util.Map;

import com.minsait.onesait.platform.config.components.RancherConfiguration;
import com.minsait.onesait.platform.config.model.Microservice;
import com.minsait.onesait.platform.config.model.Microservice.CaaS;

public interface MSAService {

	public static final String MULTITENANT_SCHEMA_ENV = "CONFIGDB_SCHEMA";
	public static final String MULTITENANT_API_KEY = "ADMIN_VERTICAL_TOKEN";
	public static final String CONFIG_INIT = "configinit";

	public List<String> getNamespacesOrProjects(String configId);

	public List<String> getNamespacesOrProjects(String configId, String url);

	public List<String> getNamespacesOrProjects(Object config);

	public List<String> getNodes(Object config, String namespace);

	public Map<String, String> getDeployedEnvVariables(Microservice microservice, String namespace);

	public String createNamespaceOrProject(String configId, String name);

	public String deployNamespaceOrProject(String configId, String namespace, String realm, List<String> services);

	public String deployNamespaceOrProject(String configId, String namespace, Map<String, Integer> services, String url,
			String projectName);

	public String deployNamespaceOrProject(String configId, String namespace, String realm, List<String> services,
			String url);

	public String deployMicroservice(Microservice microservice, String namespace, String node,
			String onesaitServerName);

	public String deployMicroservice(Microservice microservice, String namespace, String onesaitServerName);

	public String upgradeMicroservice(Microservice microservice, String namespace, Map<String, String> mapEnv);

	public String stopService(Object configuration, String stack, String namespace, String service);

	public String startService(Object configuration, String stack, String namespace, String service);

	// RANCHER 1.6 specific

	public String deployMicroservice(RancherConfiguration config, String environment, String name,
			String dockerImageURL, String onesaitServerName, String contextPath, int port);

	public String stopStack(RancherConfiguration rancher, String stack, String environment);

	public String deployMicroservice(Microservice microservice, String environment, String worker,
			String onesaitServerName, String stack);

	public List<String> getRancherStacks(RancherConfiguration rancherConfig, String env);

	public void runConfigInit(String server, String user, String credentials, String namespace, String verticalSchema,
			String multitenantAPIKey, Map<String, Boolean> verticalCreation);

	public boolean supports(CaaS caas);
}
