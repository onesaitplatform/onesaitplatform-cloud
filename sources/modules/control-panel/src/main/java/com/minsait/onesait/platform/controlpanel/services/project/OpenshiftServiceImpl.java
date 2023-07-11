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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.minsait.onesait.platform.commons.git.GitOperations;
import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.config.components.CaasConfiguration;
import com.minsait.onesait.platform.config.components.OpenshiftConfiguration;
import com.minsait.onesait.platform.config.components.RancherConfiguration;
import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.model.Microservice;
import com.minsait.onesait.platform.config.model.Microservice.CaaS;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.git.GitlabConfiguration;
import com.minsait.onesait.platform.git.GitlabException;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;

import avro.shaded.com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OpenshiftServiceImpl implements MSAService {

	private static final String LINE_SEPARATOR = "line.separator";
	private static final String COULD_NOT_EXECUTE_COMMAND = "Could not execute command {}";
	private static final String LIMITS = "--limits=cpu=400m,memory=1024Mi";
	private static final String REQUESTS = "--requests=cpu=1m,memory=1Mi";

	@Value("${onesaitplatform.docker.mandatory-services:elasticdb,configdb,configinit,quasar,realtimedb,controlpanelservice,schedulerdb,monitoringuiservice,loadbalancerservice,routerservice,cacheservice}")
	private String[] mandatoryServices;
	@Value("${onesaitplatform.docker.openshift.imagenamespace:onesait}")
	private String imageNameSpace;
	@Value("${onesaitplatform.docker.openshift.module_tag}")
	private String moduleTag;
	@Value("${onesaitplatform.docker.openshift.infra_tag}")
	private String infraTag;
	@Value("${onesaitplatform.docker.openshift.persistence_tag}")
	private String persistenceTag;
	@Value("${onesaitplatform.docker.openshift.persistence_tag_mongodb}")
	private String persistenceTagMongoDB;
	@Value("${onesaitplatform.docker.openshift.server_name}")
	private String serverName;
	@Value("${onesaitplatform.docker.openshift.realtimedbuseauth}")
	private boolean realTimeDBUseAuth;
	@Value("${onesaitplatform.docker.openshift.authdb}")
	private String authDB;
	@Value("${onesaitplatform.docker.openshift.authparams}")
	private String authParams;
	@Value("${onesaitplatform.docker.openshift.replicas}")
	private int replicas;
	@Value("${onesaitplatform.docker.openshift.persistent}")
	private boolean persistence;

	@Value("${onesaitplatform.docker.openshift.templates.git_path}")
	private String gitPath;
	@Value("${onesaitplatform.docker.openshift.templates.tmp_path}")
	private String tmpPath;
	@Value("${onesaitplatform.docker.openshift.templates.origin}")
	private String gitOrigin;
	@Autowired
	private GitOperations gitOperations;

	@Value("${onesaitplatform.microservices.remedy.enabled:false}")
	private boolean remedyEnabled;
	@Value("${onesaitplatform.microservices.remedy.url:}")
	private String remedyURL;
	@Value("${onesaitplatform.microservices.remedy.token:}")
	private String remedyToken;

	private static final ObjectMapper mapper = new ObjectMapper();
	private final RestTemplate remedyTemplate = new RestTemplate(
			SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());

	static final ImmutableMap<String, String> SERVICE_TO_TEMPLATE = new ImmutableMap.Builder<String, String>()
			.put("quasar", "/modulestemplates/11-template-quasar.yml")
			.put("controlpanelservice", "/modulestemplates/12-template-controlpanel-persistent.yml")
			.put("apimanagerservice", "/modulestemplates/13-template-apimanager.yml")
			.put("flowengineservice", "17-template-flowengine-persistent.yml")
			.put("iotbrokerservice", "/modulestemplates/14-template-iotbroker.yml")
			.put("cacheservice", "/modulestemplates/21-template-cacheserver.yml")
			.put("loadbalancerservice", "/modulestemplates/20-template-loadbalancer-persistent.yml")
			.put("routerservice", "/modulestemplates/22-template-semanticinfbroker.yml")
			.put("oauthservice", "/modulestemplates/23-template-oauthserver.yml")
			.put("configinit", "/modulestemplates/24-template-configinit.yml")
			.put("dashboardengineservice", "/modulestemplates/18-template-dashboardengine.yml")
			.put("monitoringuiservice", "/modulestemplates/19-template-monitoringui.yml")
			.put("zeppelin", "/modulestemplates/25-template-notebook.yml")
			.put("configdb", "/persistencetemplates/11-template-configdb-persistent.yml")
			.put("schedulerdb", "/persistencetemplates/12-template-schedulerdb-persistent.yml")
			.put("realtimedb", "/persistencetemplates/13-template-realtimedb-persistent.yml")
			.put("elasticdb", "/persistencetemplates/14-template-elasticdb-persistent.yml")
			.put("kafka", "/persistencetemplates/16-template-kafka.yml")
			.put("zookeeper", "/persistencetemplates/15-template-zookeeper.yml").build();
	@Autowired
	private ConfigurationService configurationService;

	@Override
	public List<String> getNamespacesOrProjects(String openshiftConfigId) {
		final OpenshiftConfiguration configuration = configurationService.getOpenshiftConfiguration(openshiftConfigId);
		return getNamespacesOrProjects(openshiftConfigId, configuration.getInstance());
	}

	@Override
	public List<String> getNamespacesOrProjects(String openshiftConfigId, String url) {
		loginOc(openshiftConfigId, url);
		final ProcessBuilder pb = new ProcessBuilder("oc", "projects", "--short=true");
		try {
			pb.redirectErrorStream(true);
			final Process p = pb.start();
			final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			final StringBuilder builder = new StringBuilder();
			String line = null;
			p.waitFor();
			while ((line = reader.readLine()) != null) {
				builder.append(line);
				builder.append(System.getProperty(LINE_SEPARATOR));
			}
			return Arrays.asList(builder.toString().split("\n"));
		} catch (IOException | InterruptedException e) {
			log.error(COULD_NOT_EXECUTE_COMMAND, pb.command());
		}
		return new ArrayList<>();
	}

	public List<String> getNamespacesOrProjects(CaasConfiguration openshift) {
		loginOc(openshift);
		final ProcessBuilder pb = new ProcessBuilder("oc", "projects", "--short=true");
		try {
			pb.redirectErrorStream(true);
			final Process p = pb.start();
			final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			final StringBuilder builder = new StringBuilder();
			String line = null;
			p.waitFor();
			while ((line = reader.readLine()) != null) {
				builder.append(line);
				builder.append(System.getProperty(LINE_SEPARATOR));
			}
			return Arrays.asList(builder.toString().split("\n"));
		} catch (IOException | InterruptedException e) {
			log.error(COULD_NOT_EXECUTE_COMMAND, pb.command());
		}
		return new ArrayList<>();
	}

	@Override
	public String createNamespaceOrProject(String openshiftConfigId, String name) {
		return null;
	}

	private void loginOc(String openshiftConfigId, String url) {
		final OpenshiftConfiguration configuration = configurationService.getOpenshiftConfiguration(openshiftConfigId);

		final ProcessBuilder pb;
		if (StringUtils.hasText(configuration.getToken())) {
			pb = new ProcessBuilder("oc", "login", url, "--token", configuration.getToken(),
					"--insecure-skip-tls-verify");
		} else {
			pb = new ProcessBuilder("oc", "login", url, "-u", configuration.getUser(), "-p",
					configuration.getPassword(), "--insecure-skip-tls-verify");
		}
		try {
			executeProcess(pb);
		} catch (IOException | InterruptedException e) {
			log.error(COULD_NOT_EXECUTE_COMMAND, pb.command());
		}
	}

	private void loginOc(CaasConfiguration openshift) {
		loginOc(openshift.getUrl(), openshift.getUser(), openshift.getPassword());
	}

	private void loginOc(String server, String user, String credentials) {
		final ProcessBuilder pb = new ProcessBuilder("oc", "login", server, "-u", user, "-p", credentials,
				"--insecure-skip-tls-verify");
		try {
			pb.redirectErrorStream(true);
			executeProcess(pb);
		} catch (IOException | InterruptedException e) {
			log.error(COULD_NOT_EXECUTE_COMMAND, pb.command());
		}
	}

	@Override
	public String deployNamespaceOrProject(String openshiftConfigId, String project, String realm,
			List<String> services) {
		final OpenshiftConfiguration configuration = configurationService.getOpenshiftConfiguration(openshiftConfigId);
		return deployNamespaceOrProject(openshiftConfigId, project, realm, services, configuration.getInstance());
	}

	@Override
	public String deployNamespaceOrProject(String openshiftConfigId, String project, String realm,
			List<String> services, String url) {
		// include mandatory services if not present
		final List<String> allServices = services;
		Arrays.asList(mandatoryServices).stream().forEach(s -> {
			if (!allServices.contains(s)) {
				allServices.add(s);
			}
		});

		log.info("Deploying in openshift with following services");
		allServices.forEach(s -> log.info(s));

		try {

			log.debug("Pulling templates from repo");
			pullTemplatesFromRepo();

			final List<String> templates = allServices.stream().map(
					s -> "/tmp/oc-templates/devops/build-deploy/openshift/onesaitplatform" + SERVICE_TO_TEMPLATE.get(s))
					.collect(Collectors.toList());

			log.debug("Login to oc");
			loginOc(openshiftConfigId, url);
			log.debug("Setting project {}", project);
			setProject(project);
			log.debug("Proccesing templates and creating deploy + service in oc");
			templates.forEach(s -> processTemplate(s, allServices.contains("kafka")));
			log.debug("Deleting template directory {}", tmpPath);
			gitOperations.deleteDirectory(tmpPath);

		} catch (final GitlabException e) {
			log.error("Could not download oc templates from repository, aborting deployment");
			throw new RuntimeException("Could not download oc templates from repository, aborting deployment");
		}

		return null;
	}

	private void pullTemplatesFromRepo() throws GitlabException {
		final GitlabConfiguration gitlabConfig = configurationService.getGitlabConfiguration(
				configurationService.getConfiguration(Configuration.Type.GITLAB, "onesaitPlatform").getId());
		if (gitlabConfig == null) {
			throw new GitlabException("No gitlab configuration found for the platform credentials");
		}
		log.debug("Creating directory {}", tmpPath);
		gitOperations.createDirectory(tmpPath);
		log.debug("Configure git with username: {} , email: {}", gitlabConfig.getUser(), gitlabConfig.getEmail());
		gitOperations.configureGitlabAndInit(gitlabConfig.getUser(), gitlabConfig.getEmail(), tmpPath);
		log.debug("Setting sparseCheckout true");
		gitOperations.sparseCheckoutConfig(tmpPath);
		final String compiledOrigin = getCompiledGitOrigin(gitlabConfig.getUser(), gitlabConfig.getPassword());
		log.debug("Adding origin {}", compiledOrigin);
		gitOperations.addOrigin(compiledOrigin, tmpPath, true);
		log.debug("Adding path {} to sparse checkout file", gitPath);
		gitOperations.sparseCheckoutAddPath(gitPath, tmpPath);
		log.debug("Checkin out on branch master");
		gitOperations.checkout("master", tmpPath);

	}

	private String getCompiledGitOrigin(String username, String password) {
		final HashMap<String, Object> scopes = new HashMap<>();
		scopes.put("username", username);
		scopes.put("password", password);
		final Writer writer = new StringWriter();
		final StringReader reader = new StringReader(gitOrigin);
		final MustacheFactory mf = new DefaultMustacheFactory();
		final Mustache mustache = mf.compile(reader, "origin");
		mustache.execute(writer, scopes);
		return writer.toString();
	}

	private void setProject(String project) {
		final ProcessBuilder pb = new ProcessBuilder("oc", "project", project);
		try {
			executeProcess(pb);
		} catch (IOException | InterruptedException e) {
			log.error(COULD_NOT_EXECUTE_COMMAND, pb.command());
		}
	}

	private void processTemplate(String template, boolean kafkaEnabled) {
		final Yaml yaml = new Yaml();
		try (final FileWriter writter = new FileWriter(template, false)) {
			@SuppressWarnings("unchecked")
			final Map<String, Object> yamlMap = (Map<String, Object>) yaml
					.load(new FileInputStream(new File(template)));
			if (yamlMap.containsKey("parameters")) {
				yamlMap.remove("parameters");
			}
			final String yml = yaml.dump(yamlMap);
			final Map<String, String> parameters = new HashMap<>();
			parameters.put("${MODULE_TAG}", moduleTag);
			parameters.put("${INFRA_TAG}", infraTag);
			parameters.put("${PERSISTENCE_TAG}", persistenceTag);
			parameters.put("${PERSISTENCE_TAG_MONGODB}", persistenceTagMongoDB);
			parameters.put("${SERVER_NAME}", serverName);
			parameters.put("${REALTIMEDBUSEAUTH}", Boolean.toString(realTimeDBUseAuth));
			parameters.put("${AUTHDB}", authDB);
			parameters.put("${AUTHPARAMS}", authParams);
			parameters.put("${REPLICAS}", Integer.toString(replicas));
			parameters.put("${PERSISTENT}", Boolean.toString(persistence));
			parameters.put("${KAFKAENABLED}", Boolean.toString(kafkaEnabled));
			parameters.put("${IMAGENAMESPACE}", imageNameSpace);

			final StringBuffer sb = new StringBuffer();
			final Pattern linuxParam = Pattern.compile("(\\$\\{[^}]+\\})");
			final Matcher matcher = linuxParam.matcher(yml);
			while (matcher.find()) {
				final String repString = parameters.get(matcher.group(1));
				if (repString != null) {
					matcher.appendReplacement(sb, repString);
				}
			}
			matcher.appendTail(sb);

			writter.write(sb.toString());
			writter.flush();
			writter.close();

			createPod(template);

		} catch (final IOException e) {
			log.error("Error while operating with file {}, {}", template, e.getMessage());
		}
	}

	private void createPod(String template) {
		final ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c",
				"oc process -f " + template + " | oc create -f -");
		try {
			pb.redirectErrorStream(true);
			executeProcess(pb);
		} catch (IOException | InterruptedException e) {
			log.error(COULD_NOT_EXECUTE_COMMAND, pb.command());
		}
	}

	@Override
	public String deployMicroservice(Microservice microservice, String project, String onesaitServerUrl) {
		try {
			createDeployment(microservice.getOpenshiftConfiguration(), project, microservice.getDockerImage(),
					microservice.getIdentification(), microservice.getPort());
			Thread.sleep(2000);
			createService(microservice.getOpenshiftConfiguration(), project, microservice.getDockerImage(),
					microservice.getIdentification(), microservice.getPort());
			Thread.sleep(2000);
			setLimits(microservice.getIdentification());
			microservice.setOpenshiftNamespace(project);
		} catch (final Exception e) {
			log.error("Could not deploy microservice", e);
		}
		return microservice.getOpenshiftConfiguration().getUrl();
	}

	@Override
	public Map<String, String> getDeployedEnvVariables(Microservice microservice, String project) {
		loginOc(microservice.getOpenshiftConfiguration());
		setProject(project);
		final ProcessBuilder pb = new ProcessBuilder("oc", "set", "env",
				"deployment/" + microservice.getIdentification(), "--list");
		try {
			pb.redirectErrorStream(true);
			final Process p = pb.start();
			final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			final StringBuilder builder = new StringBuilder();
			String line = null;
			p.waitFor();
			while ((line = reader.readLine()) != null) {
				builder.append(line);
				builder.append(System.getProperty(LINE_SEPARATOR));
			}
			final List<String> params = Arrays.asList(builder.toString().split("\n"));
			if (params.size() > 1) {
				final Map<String, String> envVars = new HashMap<String, String>();
				for (final String param : params.subList(1, params.size())) {
					final String key = param.split("=")[0];
					final String value = param.split("=")[1];
					envVars.put(key, value);
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

	private void openshiftDeploy(CaasConfiguration openshift, String project, String dockerImageUrl,
			String microserviceName) throws InterruptedException {
		loginOc(openshift);
		setProject(project);
		final ProcessBuilder pb = new ProcessBuilder("oc", "new-app", dockerImageUrl, "--name=" + microserviceName);

		try {
			executeProcess(pb);
		} catch (IOException | InterruptedException e) {
			log.error(COULD_NOT_EXECUTE_COMMAND + pb.command());
		}

	}

	private void createDeployment(CaasConfiguration openshift, String project, String dockerImageUrl,
			String microserviceName, Integer port) throws InterruptedException {
		loginOc(openshift);
		setProject(project);
		final ProcessBuilder pb = new ProcessBuilder("oc", "create", "deployment", microserviceName,
				"--image=" + dockerImageUrl, "--port=" + port);

		try {
			pb.redirectErrorStream(true);
			executeProcess(pb);
		} catch (IOException | InterruptedException e) {
			log.error(COULD_NOT_EXECUTE_COMMAND + pb.command());
		}

	}

	private void createService(CaasConfiguration openshift, String project, String dockerImageUrl,
			String microserviceName, Integer port) throws InterruptedException {
		loginOc(openshift);
		setProject(project);
		final ProcessBuilder pb = new ProcessBuilder("oc", "create", "service", "clusterip", microserviceName,
				"--tcp=" + port + ":" + port);

		try {
			pb.redirectErrorStream(true);
			executeProcess(pb);
		} catch (IOException | InterruptedException e) {
			log.error(COULD_NOT_EXECUTE_COMMAND + pb.command());
		}

	}

	private void setLimits(String microserviceName) {
		final ProcessBuilder pb = new ProcessBuilder("oc", "set", "resources", "deployment", microserviceName, LIMITS,
				REQUESTS);

		try {
			executeProcess(pb);
		} catch (IOException | InterruptedException e) {
			log.error(COULD_NOT_EXECUTE_COMMAND, pb.command());
		}
	}

	private void createRoute(String microserviceName, int port, String contextPath, String onesaitServerUrl) {
		final ProcessBuilder pb = new ProcessBuilder("oc", "create", "route", "edge", "--service=" + microserviceName,
				"--hostname=" + onesaitServerUrl, "--port=" + port, "--path=" + contextPath);

		try {
			executeProcess(pb);
		} catch (IOException | InterruptedException e) {
			log.error(COULD_NOT_EXECUTE_COMMAND, pb.command());
		}
	}

	@Override
	public String upgradeMicroservice(Microservice microservice, String project, Map<String, String> mapEnv) {
		try {
			loginOc(microservice.getOpenshiftConfiguration());
			setProject(project);
			scaleDown(microservice.getIdentification());
			unsetVarEnv(microservice, project);
			setVarEnv(microservice.getIdentification(), mapEnv);
			setImage(microservice.getIdentification(), microservice.getDockerImage());
			scaleUp(microservice.getIdentification());
			if (remedyEnabled) {
				toRemedyWebhookDCM(project, microservice.getIdentification());
			}
		} catch (final Exception e) {
			log.error("Could not stop microservice", e);
		}
		return microservice.getOpenshiftConfiguration().getUrl();
	}

	private String setImage(String microserviceName, String dockerImage) {
		final ProcessBuilder pb = new ProcessBuilder("oc", "set", "image", "deployment/" + microserviceName,
				microserviceName + "=" + dockerImage);

		pb.redirectErrorStream(true);
		try {
			return executeProcess(pb);
		} catch (IOException | InterruptedException e) {
			log.error(COULD_NOT_EXECUTE_COMMAND + pb.command());
			return null;
		}
	}

	public String stopService(CaasConfiguration openshift, String project, String service) {
		try {
			loginOc(openshift);
			setProject(project);
			scaleDown(service);
		} catch (final Exception e) {
			log.error("Could not stop microservice", e);
		}
		return openshift.getUrl();
	}

	public String startService(CaasConfiguration openshift, String project, String service) {
		try {
			loginOc(openshift);
			setProject(project);
			scaleUp(service);
		} catch (final Exception e) {
			log.error("Could not stop microservice", e);
		}
		return openshift.getUrl();
	}

	private String scaleDown(String microserviceName) {
		final ProcessBuilder pb = new ProcessBuilder("oc", "scale", "--replicas=0", "deployment/" + microserviceName);

		pb.redirectErrorStream(true);
		try {
			return executeProcess(pb);
		} catch (IOException | InterruptedException e) {
			log.error(COULD_NOT_EXECUTE_COMMAND + pb.command());
			return null;
		}
	}

	private String scaleUp(String microserviceName) {
		final ProcessBuilder pb = new ProcessBuilder("oc", "scale", "--replicas=1", "deployment/" + microserviceName);

		pb.redirectErrorStream(true);
		try {
			return executeProcess(pb);
		} catch (IOException | InterruptedException e) {
			log.error(COULD_NOT_EXECUTE_COMMAND + pb.command());
			return null;
		}
	}

	private String setVarEnv(String microserviceName, Map<String, String> mapEnv) {
		final List<String> command = new ArrayList<String>();
		command.add("oc");
		command.add("set");
		command.add("env");
		command.add("deployment/" + microserviceName);
		for (final Entry<String, String> entry : mapEnv.entrySet()) {
			command.add(entry.getKey() + "=" + entry.getValue());
		}

		final ProcessBuilder pb = new ProcessBuilder(command);

		pb.redirectErrorStream(true);
		try {
			return executeProcess(pb);
		} catch (IOException | InterruptedException e) {
			log.error(COULD_NOT_EXECUTE_COMMAND + pb.command());
			return null;
		}
	}

	private String unsetVarEnv(Microservice microservice, String project) {
		final List<String> command = new ArrayList<String>();
		command.add("oc");
		command.add("set");
		command.add("env");
		command.add("deployment/" + microservice.getIdentification());
		final Map<String, String> mapEnv = getDeployedEnvVariables(microservice, project);
		for (final Entry<String, String> entry : mapEnv.entrySet()) {
			command.add(entry.getKey() + "-");
		}

		final ProcessBuilder pb = new ProcessBuilder(command);

		pb.redirectErrorStream(true);
		try {
			return executeProcess(pb);
		} catch (IOException | InterruptedException e) {
			log.error(COULD_NOT_EXECUTE_COMMAND + pb.command());
			return null;
		}
	}

	@Override
	public List<String> getNamespacesOrProjects(Object config) {
		return getNamespacesOrProjects((CaasConfiguration) config);
	}

	@Override
	public List<String> getNodes(Object config, String namespace) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public String deployNamespaceOrProject(String configId, String namespace, Map<String, Integer> services, String url,
			String projectName) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public String deployMicroservice(Microservice microservice, String namespace, String node,
			String onesaitServerName) {
		return deployMicroservice(microservice, namespace, onesaitServerName);
	}

	@Override
	public String stopService(Object configuration, String stack, String namespace, String service) {
		return stopService((CaasConfiguration) configuration, namespace, service);
	}

	@Override
	public String startService(Object configuration, String stack, String namespace, String service) {
		return startService((CaasConfiguration) configuration, namespace, service);
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
		return deployMicroservice(microservice, environment, onesaitServerName);
	}

	@Override
	public List<String> getRancherStacks(RancherConfiguration rancherConfig, String env) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public boolean supports(CaaS caas) {
		return CaaS.OPENSHIFT.equals(caas);
	}

	private String executeProcess(ProcessBuilder pb) throws InterruptedException, IOException {
		final Process p = pb.start();
		final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		final StringBuilder builder = new StringBuilder();
		String line = null;
		p.waitFor();
		while ((line = reader.readLine()) != null) {
			builder.append(line);
			builder.append(System.getProperty(LINE_SEPARATOR));
		}
		final String result = builder.toString();
		log.trace("Execution of process {} returned: {}", String.join(" ", pb.command()), result);
		return result;
	}

	// This is for Remedy Integration
	private void toRemedyWebhookDCM(String project, String microservice) {
		final ObjectNode payload = mapper.createObjectNode();
		payload.put("resumen", "Despliegue_Realizado");
		payload.put("microservice", microservice);
		payload.put("entorno", project);
		payload.put("resolucion", "OK");
		final HttpHeaders headers = new HttpHeaders();
		headers.add("api-key", remedyToken);
		try {
			final HttpEntity<ObjectNode> body = new HttpEntity<ObjectNode>(payload, headers);
			final ResponseEntity<String> response = remedyTemplate.exchange(remedyURL, HttpMethod.POST, body,
					String.class);
			log.info("Remedy response for payload {}, is {}", payload, response.getBody());
		} catch (final Exception e) {
			log.error("Remedy webhooks are enabled, couldn't complete webhook request for service: {} in namespace: {}",
					microservice, project, e);
		}

	}

	@Async
	@Override
	public void runConfigInit(String server, String user, String credentials, String namespace, String verticalSchema,
			String multitenantAPIKey, Map<String, Boolean> verticalCreation) {
		verticalCreation.put(Tenant2SchemaMapper.extractVerticalNameFromSchema(verticalSchema), false);
		loginOc(server, user, credentials);
		setProject(namespace);
		scaleDown(MSAService.CONFIG_INIT);
		final Map<String, String> var = new HashMap<String, String>();
		var.put(MSAService.MULTITENANT_SCHEMA_ENV, verticalSchema);
		if (StringUtils.hasText(multitenantAPIKey)) {
			var.put(MSAService.MULTITENANT_API_KEY, multitenantAPIKey);
		}
		setVarEnv(MSAService.CONFIG_INIT, var);
		scaleUp(MSAService.CONFIG_INIT);
		try {
			Thread.sleep(300000);
			scaleDown(MSAService.CONFIG_INIT);
			var.put(MSAService.MULTITENANT_SCHEMA_ENV, "onesaitplatform_config");
			var.put(MSAService.MULTITENANT_API_KEY, "");
			setVarEnv(MSAService.CONFIG_INIT, var);
			verticalCreation.put(Tenant2SchemaMapper.extractVerticalNameFromSchema(verticalSchema), true);
		} catch (final InterruptedException e) {
			log.error("Could not scale down config init", e);
		}

	}

}
