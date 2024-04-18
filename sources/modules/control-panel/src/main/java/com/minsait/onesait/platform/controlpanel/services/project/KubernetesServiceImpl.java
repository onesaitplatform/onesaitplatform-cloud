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
package com.minsait.onesait.platform.controlpanel.services.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.minsait.onesait.platform.commons.git.GitException;
import com.minsait.onesait.platform.config.components.CaasConfiguration;
import com.minsait.onesait.platform.config.components.RancherConfiguration;
import com.minsait.onesait.platform.config.model.Microservice;
import com.minsait.onesait.platform.config.model.Microservice.CaaS;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KubernetesServiceImpl implements MSAService {

	private static final String ONESAIT_PLATFORM_MSA = "onesait-platform-msa";
	private static final String KUBECTL = "kubectl";
	private static final String CONFIG = "config";
	private static final String LINE_SEPARATOR = "line.separator";
	private static final String COULD_NOT_EXECUTE_COMMAND = "Could not execute command {}";
	private static final String TMP_PATH = "/tmp/";

	@Autowired
	private ConfigurationService configurationService;

	private void setCredentialsAndContext(CaasConfiguration caasConfiguration, String namespace) {
		setCredentials(caasConfiguration);
		setCluster(caasConfiguration);
		setContext(namespace);
		useContext();
	}

	private void setCredentialsAndContext(String server, String username, String credentials, String namespace) {
		setCredentials(credentials);
		setCluster(server);
		setContext(namespace);
		useContext();
	}

	private void setCredentials(CaasConfiguration caasConfiguration) {
		setCredentials(caasConfiguration.getPassword());
	}

	private void setCredentials(String password) {
		try {
			final ProcessBuilder pb = new ProcessBuilder(KUBECTL, CONFIG, "set-credentials", ONESAIT_PLATFORM_MSA,
					"--token=" + password);
			pb.redirectErrorStream(true);
			log.info(pb.command().toString());
			executeAndReadOutput(pb.start());
		} catch (final Exception e) {
			log.error("", e.getMessage());
			throw new GitException("", e);
		}
	}

	private void setCluster(CaasConfiguration caasConfiguration) {
		setCluster(caasConfiguration.getUrl());
	}

	private void setCluster(String server) {
		try {
			final ProcessBuilder pb = new ProcessBuilder(KUBECTL, CONFIG, "set-cluster", ONESAIT_PLATFORM_MSA,
					"--insecure-skip-tls-verify=true", "--server=" + server);
			pb.redirectErrorStream(true);
			log.info(pb.command().toString());
			executeAndReadOutput(pb.start());
		} catch (final Exception e) {
			log.error("", e.getMessage());
			throw new GitException("", e);
		}
	}

	private void setContext(String namespace) {
		try {
			ProcessBuilder pb = null;
			if (namespace != null) {
				pb = new ProcessBuilder(KUBECTL, CONFIG, "set-context", ONESAIT_PLATFORM_MSA,
						"--user=" + ONESAIT_PLATFORM_MSA, "--namespace=" + namespace,
						"--cluster=" + ONESAIT_PLATFORM_MSA);
			} else {
				pb = new ProcessBuilder(KUBECTL, CONFIG, "set-context", ONESAIT_PLATFORM_MSA,
						"--user=" + ONESAIT_PLATFORM_MSA, "--cluster=" + ONESAIT_PLATFORM_MSA);
			}
			pb.redirectErrorStream(true);
			log.info(pb.command().toString());
			executeAndReadOutput(pb.start());
		} catch (final Exception e) {
			log.error("", e.getMessage());
			throw new GitException("", e);
		}
	}

	private void useContext() {
		try {
			final ProcessBuilder pb = new ProcessBuilder(KUBECTL, CONFIG, "use-context", ONESAIT_PLATFORM_MSA);
			pb.redirectErrorStream(true);
			log.info(pb.command().toString());
			executeAndReadOutput(pb.start());
		} catch (final Exception e) {
			log.error("", e.getMessage());
			throw new GitException("", e);
		}
	}

	private String executeAndReadOutput(Process p) throws IOException, InterruptedException {
		return executeAndReadOutput(p, null);
	}

	private String executeAndReadOutput(Process p, Long timeoutSeconds) throws IOException, InterruptedException {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		final StringBuilder builder = new StringBuilder();
		String line = null;
		if (timeoutSeconds != null && timeoutSeconds > 0) {
			p.waitFor(timeoutSeconds, TimeUnit.SECONDS);
		} else {
			p.waitFor();
		}
		while ((line = reader.readLine()) != null) {
			builder.append(line);
			builder.append(System.getProperty("line.separator"));
		}
		final String result = builder.toString();
		log.debug(result);
		if (result.toLowerCase().contains("not a git repository")) {
			throw new GitException("Git repository not initialized, reinitialize it");
		}
		return result;
	}

	@Override
	public List<String> getNamespacesOrProjects(String configId) {
		return getNamespacesOrProjects(configId, null);
	}

	@Override
	public List<String> getNamespacesOrProjects(String configId, String url) {
		final CaasConfiguration configuration = configurationService.getCaasConfiguration(configId);
		return getNamespacesOrProjects(configuration);

	}

	@Override
	public List<String> getNamespacesOrProjects(Object config) {
		final CaasConfiguration caasConfig = (CaasConfiguration) config;
		setCredentialsAndContext(caasConfig, null);
		final ProcessBuilder pb = new ProcessBuilder(KUBECTL, "get", "namespaces", "--no-headers", "-o",
				"custom-columns=:metadata.name");
		try {
			pb.redirectErrorStream(true);
			final String result = executeAndReadOutput(pb.start());
			return Arrays.asList(result.split("\n"));
		} catch (final Exception e) {
			log.error(COULD_NOT_EXECUTE_COMMAND, pb.command());
		}
		return new ArrayList<>();

	}

	@Override
	public List<String> getNodes(Object config, String namespace) {
		final CaasConfiguration caasConfig = (CaasConfiguration) config;
		setCredentialsAndContext(caasConfig, namespace);
		final ProcessBuilder pb = new ProcessBuilder(KUBECTL, "get", "nodes", "--no-headers", "-o",
				"custom-columns=:metadata.name");
		try {
			pb.redirectErrorStream(true);
			final String result = executeAndReadOutput(pb.start());
			return Arrays.asList(result.split("\n"));
		} catch (IOException | InterruptedException e) {
			log.error(COULD_NOT_EXECUTE_COMMAND, pb.command());
		}
		return new ArrayList<>();
	}

	@Override
	public Map<String, String> getDeployedEnvVariables(Microservice microservice, String namespace) {
		setCredentialsAndContext((CaasConfiguration) microservice.getCaaSConfiguration(), namespace);
		final ProcessBuilder pb = new ProcessBuilder(KUBECTL, "set", "env",
				"deploy/" + microservice.getIdentification(), "--list");
		try {
			pb.redirectErrorStream(true);
			final String result = executeAndReadOutput(pb.start());
			final List<String> params = Arrays.asList(result.split("\n"));
			if (params.size() > 1) {
				final Map<String, String> envVars = new HashMap<String, String>();
				for (final String param : params) {
					if (!param.startsWith("#")) {
						final String key = param.split("=")[0];
						final String value = param.split("=")[1];
						envVars.put(key, value);
					}
				}
				return envVars;
			} else {
				return new HashMap<>();
			}
		} catch (IOException | InterruptedException e) {
			log.error(COULD_NOT_EXECUTE_COMMAND, pb.command());
		}
		return new HashMap<>();

	}

	@Override
	public String createNamespaceOrProject(String configId, String name) {
		// TODO
		return null;
	}

	@Override
	public String deployNamespaceOrProject(String configId, String namespace, String realm, List<String> services) {
		// TODO
		return null;
	}

	@Override
	public String deployNamespaceOrProject(String configId, String namespace, Map<String, Integer> services, String url,
			String projectName) {
		// TODO
		return null;
	}

	@Override
	public String deployNamespaceOrProject(String configId, String namespace, String realm, List<String> services,
			String url) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public String deployMicroservice(Microservice microservice, String namespace, String node,
			String onesaitServerName) {
		setCredentialsAndContext((CaasConfiguration) microservice.getCaaSConfiguration(), namespace);
		final String deployTemplate = createDeploymentTemplate(onesaitServerName, onesaitServerName, node,
				microservice.getDockerImage(), microservice.getIdentification(), microservice.getContextPath(),
				microservice.getPort(), namespace);
		final String serviceTemplate = createServiceTemplate(microservice.getIdentification(), microservice.getPort());
		try {
			createTempYmlFile("deploy-template-" + microservice.getIdentification() + ".yml", deployTemplate);
			createTempYmlFile("service-template-" + microservice.getIdentification() + ".yml", serviceTemplate);
			kubeCreateOrApply("deploy-template-" + microservice.getIdentification() + ".yml", namespace, "create");
			kubeCreateOrApply("service-template-" + microservice.getIdentification() + ".yml", namespace, "create");
			deleteTempYmlFile("deploy-template-" + microservice.getIdentification() + ".yml");
			deleteTempYmlFile("service-template-" + microservice.getIdentification() + ".yml");
			microservice.setOpenshiftNamespace(namespace);
		} catch (final Exception e) {
			log.error("Could not deploy microservice", e);
		}
		return ((CaasConfiguration) microservice.getCaaSConfiguration()).getUrl();
	}

	private void kubeCreateOrApply(String fileName, String namespace, String command) {
		final ProcessBuilder pb = new ProcessBuilder(KUBECTL, command, "-f", TMP_PATH + fileName);
		try {
			pb.redirectErrorStream(true);
			executeAndReadOutput(pb.start());
		} catch (IOException | InterruptedException e) {
			log.error(COULD_NOT_EXECUTE_COMMAND, pb.command());
		}
	}

	@Override
	public String deployMicroservice(Microservice microservice, String namespace, String onesaitServerName) {
		return deployMicroservice(microservice, namespace, null, onesaitServerName);
	}

	@Override
	public String upgradeMicroservice(Microservice microservice, String namespace, Map<String, String> mapEnv) {
		setCredentialsAndContext((CaasConfiguration) microservice.getCaaSConfiguration(), namespace);
		scaleDeployment(microservice.getIdentification(), 0);
		unsetVarEnv(microservice, namespace);
		setVarEnv(microservice.getIdentification(), mapEnv);
		scaleDeployment(microservice.getIdentification(), 1);
		return null;
	}

	private void unsetVarEnv(Microservice microservice, String namespace) {
		final List<String> command = new ArrayList<String>();
		command.add(KUBECTL);
		command.add("set");
		command.add("env");
		command.add("deploy/" + microservice.getIdentification());
		final Map<String, String> mapEnv = getDeployedEnvVariables(microservice, namespace);
		for (final Entry<String, String> entry : mapEnv.entrySet()) {
			command.add(entry.getKey() + "-");
		}

		final ProcessBuilder pb = new ProcessBuilder(command);

		pb.redirectErrorStream(true);
		try {
			executeAndReadOutput(pb.start());
		} catch (IOException | InterruptedException e) {
			log.error(COULD_NOT_EXECUTE_COMMAND + pb.command());
		}
	}

	private void setVarEnv(String microserviceName, Map<String, String> mapEnv) {
		final List<String> command = new ArrayList<String>();
		command.add(KUBECTL);
		command.add("set");
		command.add("env");
		command.add("deploy/" + microserviceName);
		for (final Entry<String, String> entry : mapEnv.entrySet()) {
			command.add(entry.getKey() + "=" + entry.getValue());
		}

		final ProcessBuilder pb = new ProcessBuilder(command);

		pb.redirectErrorStream(true);
		try {
			executeAndReadOutput(pb.start());
		} catch (IOException | InterruptedException e) {
			log.error(COULD_NOT_EXECUTE_COMMAND + pb.command());
		}
	}

	@Override
	public String stopService(Object configuration, String stack, String namespace, String service) {
		return stopService((CaasConfiguration) configuration, namespace, service);
	}

	@Override
	public String startService(Object configuration, String stack, String namespace, String service) {
		return startService((CaasConfiguration) configuration, namespace, service);
	}

	private String stopService(CaasConfiguration configuration, String namespace, String service) {
		setCredentialsAndContext(configuration, namespace);
		scaleDeployment(service, 0);
		return configuration.getUrl();
	}

	private String startService(CaasConfiguration configuration, String namespace, String service) {
		setCredentialsAndContext(configuration, namespace);
		scaleDeployment(service, 1);
		return configuration.getUrl();
	}

	private void scaleDeployment(String service, int replicas) {
		final ProcessBuilder pb = new ProcessBuilder(KUBECTL, "scale", "--replicas=" + replicas,
				"deployment/" + service);
		pb.redirectErrorStream(true);
		log.info(pb.command().toString());
		try {
			executeAndReadOutput(pb.start());
		} catch (IOException | InterruptedException e) {
			log.error("Error stoping service {}", service, e);
		}
	}

	@Override
	public String deployMicroservice(RancherConfiguration config, String environment, String name,
			String dockerImageURL, String onesaitServerName, String contextPath, int port) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public String stopStack(RancherConfiguration rancher, String stack, String environment) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public String deployMicroservice(Microservice microservice, String environment, String worker,
			String onesaitServerName, String stack) {
		return deployMicroservice(microservice, environment, worker, onesaitServerName);
	}

	@Override
	public List<String> getRancherStacks(RancherConfiguration rancherConfig, String env) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public boolean supports(CaaS caas) {
		return CaaS.KUBERNETES.equals(caas) || CaaS.RANCHER_2.equals(caas);
	}

	@SuppressWarnings("unchecked")
	private String createDeploymentTemplate(String serverName, String onesaitServerName, String node,
			String dockerImageURL, String serviceName, String contextPath, int port, String namespace) {
		final HashMap<String, Object> scopes = new HashMap<>();
		scopes.put("MICROSERVICE", serviceName);
		scopes.put("DOCKER_IMAGE", dockerImageURL);
		scopes.put("ONESAIT_SERVER_NAME", onesaitServerName);
		scopes.put("SERVER_NAME", serverName);
		scopes.put("NODE_NAME", node);
		scopes.put("CONTEXT_PATH", contextPath);
		scopes.put("PORT", port);
		scopes.put("NAMESPACE", namespace);

		final Writer writer = new StringWriter();
		final MustacheFactory mf = new DefaultMustacheFactory();
		final Mustache mustache = mf.compile(new StringReader(KubernetesTemplates.DEPLOY_TEMPLATE),
				"deploy-create.yml");
		mustache.execute(writer, scopes);

		final Yaml yaml = new Yaml();
		final Map<String, Map<?, ?>> yamlMap = (Map<String, Map<?, ?>>) yaml.load(writer.toString());

		return yaml.dump(yamlMap);

	}

	@SuppressWarnings("unchecked")
	private String createServiceTemplate(String serviceName, int port) {
		final HashMap<String, Object> scopes = new HashMap<>();
		scopes.put("MICROSERVICE", serviceName.toLowerCase());
		scopes.put("PORT", port);

		final Writer writer = new StringWriter();
		final MustacheFactory mf = new DefaultMustacheFactory();
		final Mustache mustache = mf.compile(new StringReader(KubernetesTemplates.SERVICE_TEMPLATE),
				"service-create.yml");
		mustache.execute(writer, scopes);

		final Yaml yaml = new Yaml();
		final Map<String, Map<?, ?>> yamlMap = (Map<String, Map<?, ?>>) yaml.load(writer.toString());

		return yaml.dump(yamlMap);
	}

	private void createTempYmlFile(String filename, String output) throws IOException {
		try (final FileWriter writer = new FileWriter(TMP_PATH + filename)) {
			writer.write(output);
			writer.flush();
		} catch (final Exception e) {
			log.error("Could not open FileWriter: ", e);
		}
	}

	private boolean deleteTempYmlFile(String filename) {
		final File file = new File(TMP_PATH + filename);
		return file.delete();
	}

	@Async
	@Override
	public void runConfigInit(String server, String user, String credentials, String namespace, String verticalSchema,
			String multitenantAPIKey, Map<String, Boolean> verticalCreation) {
		verticalCreation.put(Tenant2SchemaMapper.extractVerticalNameFromSchema(verticalSchema), false);
		setCredentialsAndContext(server, user, credentials, namespace);
		scaleDeployment(MSAService.CONFIG_INIT, 0);
		final Map<String, String> var = new HashMap<String, String>();
		var.put(MSAService.MULTITENANT_SCHEMA_ENV, verticalSchema);
		if (StringUtils.hasText(multitenantAPIKey)) {
			var.put(MSAService.MULTITENANT_API_KEY, multitenantAPIKey);
		}
		setVarEnv(MSAService.CONFIG_INIT, var);
		scaleDeployment(MSAService.CONFIG_INIT, 1);
		try {
			Thread.sleep(300000);
			scaleDeployment(MSAService.CONFIG_INIT, 0);
			var.put(MSAService.MULTITENANT_SCHEMA_ENV, "onesaitplatform_config");
			var.put(MSAService.MULTITENANT_API_KEY, "");
			setVarEnv(MSAService.CONFIG_INIT, var);
			verticalCreation.put(Tenant2SchemaMapper.extractVerticalNameFromSchema(verticalSchema), true);
		} catch (final InterruptedException e) {
			log.error("Could not scale down config init", e);
		}

	}

	@Override
	public String getCurrentDockerImage(Microservice microservice, String openshiftNamespace) {
		throw new RuntimeException("Not implemented");
	}
}
