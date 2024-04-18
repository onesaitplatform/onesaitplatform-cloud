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
package com.minsait.onesait.platform.controlpanel.services.microservice;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.cdancy.jenkins.rest.domain.job.BuildInfo;
import com.cdancy.jenkins.rest.domain.job.JobInfo;
import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.config.components.JenkinsConfiguration;
import com.minsait.onesait.platform.config.components.RancherConfiguration;
import com.minsait.onesait.platform.config.model.DigitalTwinDevice;
import com.minsait.onesait.platform.config.model.Microservice;
import com.minsait.onesait.platform.config.model.Microservice.CaaS;
import com.minsait.onesait.platform.config.model.Microservice.TemplateType;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.services.exceptions.MicroserviceException;
import com.minsait.onesait.platform.config.services.microservice.MicroserviceService;
import com.minsait.onesait.platform.config.services.microservice.dto.DeployParameters;
import com.minsait.onesait.platform.config.services.microservice.dto.JenkinsParameter;
import com.minsait.onesait.platform.config.services.microservice.dto.MSConfig;
import com.minsait.onesait.platform.config.services.microservice.dto.MicroserviceDTO;
import com.minsait.onesait.platform.config.services.microservice.dto.ZipMicroservice;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.services.gateway.CloudGatewayService;
import com.minsait.onesait.platform.controlpanel.services.jenkins.JenkinsBuildWatcher;
import com.minsait.onesait.platform.controlpanel.services.jenkins.JenkinsService;
import com.minsait.onesait.platform.controlpanel.services.project.MSAServiceDispatcher;
import com.minsait.onesait.platform.controlpanel.services.project.MicroservicesGitServiceManager;
import com.minsait.onesait.platform.git.GitlabConfiguration;
import com.minsait.onesait.platform.git.GitlabException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MicroserviceBusinessServiceImpl implements MicroserviceBusinessService {

	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private MicroserviceService microserviceService;
	@Autowired
	private CloudGatewayService gatewayService;
	@Autowired
	private MicroservicesGitServiceManager gitServiceManager;
	@Autowired
	private JenkinsService jenkinsService;
	@Autowired
	private UserService userService;
	@Autowired
	private ApplicationContext appContext;
	@Autowired
	private TaskExecutor taskExecutor;
	@Autowired
	private MicroserviceJenkinsTemplateUtil microserviceJenkinsTemplateUtil;
	@Autowired
	private MSAServiceDispatcher msaServiceDispatcher;

	private static final String DOCKER_USERNAMEVALUE = "DOCKER_USERNAMEVALUE";
	private static final String PRIVATE_REGISTRY = "PRIVATE_REGISTRY";
	private static final String MICROSERVICE_NAME = "MICROSERVICE_NAME";
	private static final String SOURCES_PATH = "SOURCES_PATH";
	private static final String GIT_REPOSITORY = "GIT_REPOSITORY";
	private static final String GIT_TOKEN = "GIT_TOKEN";
	private static final String GIT_USER = "GIT_USER";
	private static final String GIT_URL = "GIT_URL";
	private static final String DOCKER_PATH = "DOCKER_PATH";
	private static final String DOCKER_MODULETAGVALUE = "DOCKER_MODULETAGVALUE";
	private static final String DEFAULT_PORT = "30010";
	private static final String NO_CAAS_CONFIGURATION_ERROR = "No CaaS Configuration found for this microservice";
	private static final String SERVICE_NOT_DEPLOYED_ERROR = "Service is not deployed ";
	private static final String NOT_SUPPORTED_CAAS = "Not supported Caas";
	private static final String ARCHITECTURE_URL = "https://dev.architecture.onesait.com/initializr/generate";
	private static final String LIBRARIES = "libraries";
	private static final String MODULES = "modules";
	private static final String INITIALIZR_TOKEN = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJqdGkiOiJzb2Z0dGVrSldUIiwic3ViIjoiQXJxdWl0ZWN0dXJlRFNQIiwiYXV0aG9yaXRpZXMiOlsiUk9MRV9VU0VSIl0sImlhdCI6MTU4MzIyMzYyNSwiZXhwIjoxNjgzMjI0MjI1fQ.u8T9swuGbxnhQRjivbilfJTX4cOFVhelocG6Hr4508KxbdRcBxzfRem9uyWQPAS4SDAGlfOeMRWKlVuV-0j2Zg";
	private static final String ONESAIT = "onesait-";
	private static final String BACKENDPROJECT = "backendproject-";

	@Override
	public Microservice createMicroservice(MicroserviceDTO microservice, MSConfig config, File file) {
		final Microservice ms = new Microservice();
		final boolean rollbackPipepilneCreation = !StringUtils.hasText(microservice.getJobName());

		try {
			ms.setCaas(CaaS.valueOf(microservice.getCaas()));
		} catch (final IllegalArgumentException ex) {
			throw new MicroserviceException("Not valid CaaS value");
		}

		validateMS(config, microservice, ms);
		if (!StringUtils.hasText(microservice.getContextPath())) {
			ms.setContextPath("/" + microservice.getName());
		}
		ms.setIdentification(microservice.getName());
		ms.setPort(microservice.getPort());
		ms.setContextPath(microservice.getContextPath());
		ms.setTemplateType(microservice.getTemplate());
		ms.setStripRoutePrefix(microservice.isStripRoutePrefix());
		ms.setUser(userService.getUser(microservice.getOwner()));

		if (config.isCreateGitlab()) {
			try {
				GitlabConfiguration cloneConfig = null;
				if (microservice.getGitTemplate() != null) {
					cloneConfig = GitlabConfiguration.builder().site(microservice.getGitTemplate().getUrl())
							.privateToken(microservice.getGitTemplate().getPrivateToken()).build();
				}
				ms.setGitlabRepository(createGitlabRepository(ms, file, config, cloneConfig));

			} catch (final Exception e) {
				log.error("Error while creating Gitlab repository");
				if (rollbackPipepilneCreation) {
					log.info("Rolling back creation of pipeline");
					jenkinsService.deleteJob(ms.getJenkinsConfiguration().getJenkinsUrl(),
							ms.getJenkinsConfiguration().getUsername(), ms.getJenkinsConfiguration().getToken(),
							ms.getJobName(), null);
				}
				throw new MicroserviceException("Could not create gitlab project " + e.getMessage());
			}
		} else {
			ms.setGitlabRepository(microservice.getGitlab());
		}
		ms.setActive(true);
		return microserviceService.create(ms);
	}

	@Override
	public Microservice createMicroserviceZipImport(MicroserviceDTO microservice, MSConfig config,
			MultipartFile multipart) {

		if (multipart != null && !multipart.isEmpty()) {
			final File file = new File("/tmp/" + multipart.getOriginalFilename());
			try {
				Files.write(file.toPath(), multipart.getBytes());
				return createMicroservice(microservice, config, file);
			} catch (final IOException e) {
				log.error("Wrong zip file", e);
				throw new MicroserviceException("Could not decompress Zip file");
			} finally {
				if (file.exists()) {
					file.delete();
				}
			}

		} else {
			return createMicroservice(microservice, config, null);
		}
	}

	@Override
	public Microservice createMicroserviceFromDigitalTwin(DigitalTwinDevice device, File file,
			GitlabConfiguration configuration, String sources, String docker) {
		final MicroserviceDTO microservice = MicroserviceDTO.builder().name(device.getIdentification().toLowerCase())
				.contextPath(device.getContextPath()).port(device.getPort())
				.template(TemplateType.DIGITAL_TWIN.toString()).gitlabConfiguration(configuration)
				.owner(device.getUser().getUserId()).build();

		final MSConfig config = MSConfig.builder().createGitlab(true).defaultCaaS(true)
				.defaultGitlab(!StringUtils.hasText(configuration.getPrivateToken())
						|| !StringUtils.hasText(configuration.getSite()))
				.defaultJenkins(true).docker(docker).ontology(null).sources(sources).build();

		return createMicroservice(microservice, config, file);
	}

	@Override
	public String createJenkinsPipeline(Microservice microservice) {
		return this.createJenkinsPipeline(microservice, null);
	}

	private String createJenkinsPipeline(Microservice microservice, String viewName) {
		if (!StringUtils.hasText(microservice.getJobName())
				|| !StringUtils.hasText(microservice.getJenkinsConfiguration().getJenkinsUrl())
				|| !StringUtils.hasText(microservice.getJenkinsConfiguration().getToken())) {
			throw new MicroserviceException("Jenkins configuration parameters are empty");
		}
		try {
			final String jenkinsUrl = microservice.getJenkinsConfiguration().getJenkinsUrl();
			final String username = microservice.getJenkinsConfiguration().getUsername();
			final String token = microservice.getJenkinsConfiguration().getToken();
			if (StringUtils.hasText(viewName)) {
				if (!jenkinsService.viewExists(jenkinsUrl, username, token, viewName)) {
					jenkinsService.createView(jenkinsUrl, username, token, viewName);
				}
				jenkinsService.createJobInView(jenkinsUrl, username, token, microservice.getJobName(), viewName,
						microservice.getJenkinsXML());
			} else {
				jenkinsService.createJob(jenkinsUrl, username, token, microservice.getJobName(), null,
						microservice.getJenkinsXML());
			}
			final JobInfo job = jenkinsService.getJobInfo(jenkinsUrl, username, token, microservice.getJobName(), null);
			if (job != null) {
				return job.url();
			} else {
				throw new MicroserviceException(
						"Could not create jenkins pipeline, review jenkins configuration parameters");
			}
		} catch (final Exception e) {
			log.error("Could not create jenkins pipeline", e);
			throw new MicroserviceException("Could not create jenkins pipeline " + e);
		}
	}

	@Override
	public String createGitlabRepository(Microservice microservice, File file, MSConfig config,
			GitlabConfiguration cloneConfig) throws GitlabException {
		return gitServiceManager.dispatchService(microservice.getGitlabConfiguration())
				.createGitlabProject(microservice, true, file, config, cloneConfig);

	}

	@Override
	public List<JenkinsParameter> getJenkinsJobParameters(Microservice microservice) {
		if (!StringUtils.hasText(microservice.getJobName())) {
			throw new MicroserviceException("This microservice doesn't have a jenkins pipeline associated");
		}
		final Map<String, Object> map = jenkinsService.getParametersFromJob(
				microservice.getJenkinsConfiguration().getJenkinsUrl(),
				microservice.getJenkinsConfiguration().getUsername(), microservice.getJenkinsConfiguration().getToken(),
				microservice.getJobName());
		return map.entrySet().stream()
				.map(e -> JenkinsParameter.builder().name(e.getKey())
						.value(assignParameterValue(e.getKey(), e.getValue(), microservice)).build())
				.collect(Collectors.toList());

	}

	private Object assignParameterValue(String key, Object value, Microservice microservice) {
		try {
			if (key.equalsIgnoreCase(GIT_URL)) {
				return microservice.getGitlabRepository();
			}
			if (key.equalsIgnoreCase(GIT_USER)) {
				return microservice.getGitlabConfiguration().getUser();
			}
			if (key.equalsIgnoreCase(GIT_TOKEN)) {
				return microservice.getGitlabConfiguration().getPrivateToken();
			}
		} catch (final Exception e) {
			log.warn("Could not evaluate parameter value");
		}
		return value;
	}

	@Override
	public int buildJenkins(Microservice microservice, List<JenkinsParameter> parameters) {
		final Map<String, List<String>> paramMap = parameters.stream()
				.collect(Collectors.toMap(p -> p.getName(), p -> Arrays.asList((String) p.getValue())));

		final int result = jenkinsService.buildWithParameters(microservice.getJenkinsConfiguration().getJenkinsUrl(),
				microservice.getJenkinsConfiguration().getUsername(), microservice.getJenkinsConfiguration().getToken(),
				microservice.getJobName(), null, paramMap);

		final JenkinsBuildWatcher buildWatcher = appContext.getBean(JenkinsBuildWatcher.class);
		buildWatcher.setJenkinsQueueId(result);
		buildWatcher.setMicroservice(microservice);
		buildWatcher.setClient(jenkinsService.getJenkinsClient(microservice.getJenkinsConfiguration().getJenkinsUrl(),
				microservice.getJenkinsConfiguration().getUsername(),
				microservice.getJenkinsConfiguration().getToken()));
		taskExecutor.execute(buildWatcher);

		final JenkinsParameter username = parameters.stream().filter(jp -> jp.getName().equals(DOCKER_USERNAMEVALUE))
				.findFirst().orElse(null);
		final JenkinsParameter registry = parameters.stream().filter(jp -> jp.getName().equals(PRIVATE_REGISTRY))
				.findFirst().orElse(null);
		final JenkinsParameter tag = parameters.stream().filter(jp -> jp.getName().equals(DOCKER_MODULETAGVALUE))
				.findFirst().orElse(null);
		microservice.setJenkinsQueueId(result);
		if (registry != null && username != null && tag != null) {
			microservice.setDockerImage(registry.getValue() + "/" + username.getValue() + "/"
					+ microservice.getIdentification().toLowerCase() + ":" + tag.getValue());
		} else if (registry != null && tag != null) {
			microservice.setDockerImage(
					registry.getValue() + "/" + microservice.getIdentification().toLowerCase() + ":" + tag.getValue());
		}
		microserviceService.save(microservice);
		return result;

	}

	@Override
	public DeployParameters getEnvironments(Microservice microservice) {
		checkCaaSConfig(microservice);
		final List<String> environments = msaServiceDispatcher.dispatch(microservice.getCaas())
				.getNamespacesOrProjects(microservice.getCaaSConfiguration());
		return DeployParameters.builder().id(microservice.getId()).environments(environments)
				.dockerImageUrl(microservice.getDockerImage() == null ? "" : microservice.getDockerImage())
				.onesaitServerUrl("").build();
	}

	@Override
	public DeployParameters getHosts(Microservice microservice, String environment) {
		checkCaaSConfig(microservice);
		final List<String> environments = msaServiceDispatcher.dispatch(microservice.getCaas())
				.getNamespacesOrProjects(microservice.getCaaSConfiguration());
		List<String> workers = null;
		List<String> stacks = null;
		try {
			workers = msaServiceDispatcher.dispatch(microservice.getCaas())
					.getNodes(microservice.getCaaSConfiguration(), environment);
			stacks = msaServiceDispatcher.dispatch(microservice.getCaas())
					.getRancherStacks(microservice.getRancherConfiguration(), environment);
		} catch (final Exception e) {
			log.debug("Stacks and nodes only for rancher installations.");
		}
		return DeployParameters.builder().id(microservice.getId()).workers(workers).environments(environments)
				.stacks(stacks)
				.dockerImageUrl(microservice.getDockerImage() == null ? "" : microservice.getDockerImage())
				.onesaitServerUrl("").build();

	}

	@Override
	public String deployMicroservice(Microservice microservice, String environment, String worker,
			String onesaitServerUrl, String dockerImageUrl) {
		checkCaaSConfig(microservice);
		if (StringUtils.hasText(dockerImageUrl)) {
			microservice.setDockerImage(dockerImageUrl);
		} else if (!StringUtils.hasText(microservice.getDockerImage())) {
			throw new MicroserviceException("Empty parameter docker image url ");
		}
		final String url = msaServiceDispatcher.dispatch(microservice.getCaas()).deployMicroservice(microservice,
				environment, worker, onesaitServerUrl);
		if (!microservice.getCaas().equals(CaaS.RANCHER)) {
			if (onesaitServerUrl.startsWith("http")) {
				microservice.setOpenshiftDeploymentUrl(onesaitServerUrl);
			} else {
				microservice.setOpenshiftDeploymentUrl("http://" + onesaitServerUrl);
			}
		}
		microserviceService.save(microservice);
		gatewayService.publishMicroserviceToGateway(microservice);
		return url;
	}

	@Override
	public String deployMicroservice(Microservice microservice, String environment, String worker,
			String onesaitServerUrl, String dockerImageUrl, String stack) {
		checkCaaSConfig(microservice);
		if (StringUtils.hasText(dockerImageUrl)) {
			microservice.setDockerImage(dockerImageUrl);
		} else if (!StringUtils.hasText(microservice.getDockerImage())) {
			throw new MicroserviceException("Empty parameter docker image url ");
		}
		final String url = msaServiceDispatcher.dispatch(microservice.getCaas()).deployMicroservice(microservice,
				environment, worker, onesaitServerUrl, stack);
		if (!microservice.getCaas().equals(CaaS.RANCHER)) {
			if (onesaitServerUrl.startsWith("http")) {
				microservice.setOpenshiftDeploymentUrl(onesaitServerUrl);
			} else {
				microservice.setOpenshiftDeploymentUrl("http://" + onesaitServerUrl);
			}
		}
		microserviceService.save(microservice);
		gatewayService.publishMicroserviceToGateway(microservice);
		return url;

	}

	@Override
	public void deleteMicroservice(Microservice microservice) {
		msaServiceDispatcher.dispatch(microservice.getCaas()).stopService(microservice.getCaaSConfiguration(),
				microservice.getRancherStack(), microservice.getRancherEnv(), microservice.getIdentification());
		microserviceService.delete(microservice);
	}

	@Override
	public String upgradeMicroservice(Microservice microservice, String dockerImageUrl, Map<String, String> mapEnv) {
		checkCaaSConfig(microservice);
		if (StringUtils.hasText(dockerImageUrl)) {
			microservice.setDockerImage(dockerImageUrl);
		} else if (!StringUtils.hasText(microservice.getDockerImage())) {
			throw new MicroserviceException("Empty parameter docker image url ");
		}

		final String url;
		url = msaServiceDispatcher.dispatch(microservice.getCaas()).upgradeMicroservice(microservice,
				microservice.getOpenshiftNamespace(), mapEnv);
		microserviceService.save(microservice);
		gatewayService.publishMicroserviceToGateway(microservice);
		return url;

	}

	@Override
	public Map<String, String> getEnvMap(Microservice microservice) {
		checkCaaSConfig(microservice);
		return msaServiceDispatcher.dispatch(microservice.getCaas()).getDeployedEnvVariables(microservice,
				microservice.getOpenshiftNamespace());
	}

	@Override
	public String getCurrentImage(Microservice microservice) {
		checkCaaSConfig(microservice);
		return msaServiceDispatcher.dispatch(microservice.getCaas()).getCurrentDockerImage(microservice,
				microservice.getOpenshiftNamespace());
	}

	@Override
	public void stopMicroservice(Microservice microservice) {
		checkCaaSConfig(microservice);
		msaServiceDispatcher.dispatch(microservice.getCaas()).stopService(microservice.getCaaSConfiguration(),
				microservice.getStackOrNamespace(), microservice.getRancherEnv(), microservice.getIdentification());
	}

	@Override
	public void startMicroservice(Microservice microservice) {
		checkCaaSConfig(microservice);
		msaServiceDispatcher.dispatch(microservice.getCaas()).startService(microservice.getCaaSConfiguration(),
				microservice.getStackOrNamespace(), microservice.getRancherEnv(), microservice.getIdentification());
	}

	@Override
	public boolean hasPipelineFinished(Microservice microservice) {
		boolean result = false;
		if (microservice.getJenkinsQueueId() != null) {
			try {
				final BuildInfo info = jenkinsService.buildInfo(microservice.getJenkinsConfiguration(),
						microservice.getJobName(), null, microservice.getJenkinsQueueId());
				if (info != null) {
					result = info.result() != null;
				}
			} catch (final Exception e) {
				log.warn("No item found on queue");
				result = true;
			}
		}
		if (result) {
			microservice.setJenkinsQueueId(null);
			microserviceService.save(microservice);
		}
		return result;
	}

	private void validateMS(MSConfig config, MicroserviceDTO microservice, Microservice ms) {
		if (!validServiceName(microservice.getName())) {
			throw new MicroserviceException("Invalid name, it can only contain lower case letters - and _");
		}
		setJenkinsConfiguration(config, microservice, ms);

		if (microservice.getCaas().equals(CaaS.RANCHER.toString())) {
			setRancherConfiguration(config, microservice, ms);
		} else {
			setOpenshiftConfiguration(config, microservice, ms);
		}
		if (!microservice.getTemplate().equals(Microservice.TemplateType.MLFLOW_MODEL.toString())) {
			setGitlabConfiguration(config, microservice, ms);
		}

	}

	private void setJenkinsConfiguration(MSConfig config, MicroserviceDTO microservice, Microservice ms) {
		if (config.isDefaultJenkins()) {
			final JenkinsConfiguration jenkins = configurationService.getDefaultJenkinsConfiguration();
			if (jenkins == null) {
				throw new MicroserviceException("No default Configuration for Jenkins found");
			}
			ms.setJenkinsConfiguration(jenkins);
			microservice.setJenkinsConfiguration(jenkins);

		} else {
			if (microservice.getJenkinsConfiguration() == null
					|| !StringUtils.hasText(microservice.getJenkinsConfiguration().getJenkinsUrl())
					|| !StringUtils.hasText(microservice.getJenkinsConfiguration().getToken())) {
				throw new MicroserviceException("Jenkins configuration parameters are empty");
			}
			ms.setJenkinsConfiguration(microservice.getJenkinsConfiguration());
		}
		try {
			if (!StringUtils.hasText(microservice.getJobName())) {
				ms.setJobName(microservice.getName().concat("-pipeline"));
				if (config.isCreateGitlab() && microservice.getTemplate() != null) {
					// will overwrite paths at MicroserviceTemplateUtil
					ms.setJenkinsXML(microserviceJenkinsTemplateUtil.compileXMLTemplate(config, microservice,
							config.getSources(), config.getDocker()));
				} else {
					ms.setJenkinsXML(microserviceJenkinsTemplateUtil.compileXMLTemplate(config, microservice));
				}
				ms.setJobUrl(createJenkinsPipeline(ms, config.getJenkinsView()));
			} else {
				ms.setJobName(microservice.getJobName());
				ms.setJobUrl(
						jenkinsService
								.getJobInfo(microservice.getJenkinsConfiguration().getJenkinsUrl(),
										microservice.getJenkinsConfiguration().getUsername(),
										microservice.getJenkinsConfiguration().getToken(), ms.getJobName(), null)
								.url());
			}
		} catch (final Exception e) {
			throw new MicroserviceException(e.getMessage());
		}
	}

	private void setGitlabConfiguration(MSConfig config, MicroserviceDTO microservice, Microservice ms) {

		if (config.isDefaultGitlab()) {
			final GitlabConfiguration gitlab = configurationService.getDefautlGitlabConfiguration();
			if (gitlab == null) {
				throw new MicroserviceException("No default Configuration for Gitlab found");
			}
			ms.setGitlabConfiguration(gitlab);

		} else {
			if (microservice.getGitlabConfiguration() == null
					|| !StringUtils.hasText(microservice.getGitlabConfiguration().getSite())
					|| !StringUtils.hasText(microservice.getGitlabConfiguration().getPrivateToken())) {
				throw new MicroserviceException("Gitlab configuration parameters are empty");
			}
			ms.setGitlabConfiguration(microservice.getGitlabConfiguration());
		}

		try {
			ms.setGitlabConfiguration(gitServiceManager.dispatchService(ms.getGitlabConfiguration())
					.getGitlabConfigurationFromPrivateToken(ms.getGitlabConfiguration().getSite(),
							ms.getGitlabConfiguration().getPrivateToken()));
		} catch (final GitlabException e) {
			log.error("Gitlab Exception when trying to get info from token");
			throw new MicroserviceException(e.getMessage());
		}
	}

	private void setRancherConfiguration(MSConfig config, MicroserviceDTO microservice, Microservice ms) {
		if (config.isDefaultCaaS()) {
			final RancherConfiguration rancher = configurationService.getDefaultRancherConfiguration();
			if (rancher == null) {
				throw new MicroserviceException("No default Configuration for Rancher found");
			}
			ms.setRancherConfiguration(rancher);
		} else {
			if (microservice.getRancherConfiguration() == null
					|| !StringUtils.hasText(microservice.getRancherConfiguration().getAccessKey())
					|| !StringUtils.hasText(microservice.getRancherConfiguration().getSecretKey())) {
				throw new MicroserviceException("Rancher configuration parameters are empty");
			}
			ms.setRancherConfiguration(microservice.getRancherConfiguration());
		}
	}

	private void setOpenshiftConfiguration(MSConfig config, MicroserviceDTO microservice, Microservice ms) {
		if (microservice.getOpenshiftConfiguration() == null
				|| !StringUtils.hasText(microservice.getOpenshiftConfiguration().getUser())
				|| !StringUtils.hasText(microservice.getOpenshiftConfiguration().getPassword())) {
			throw new MicroserviceException("Openshift configuration parameters are empty");
		}
		ms.setOpenshiftConfiguration(microservice.getOpenshiftConfiguration());
	}

	private boolean validServiceName(String identification) {
		final Pattern p = Pattern.compile("^[a-z-]{1,100}$");
		final Matcher m = p.matcher(identification);
		return m.matches();
	}

	private void checkCaaSConfig(Microservice microservice) {
		if (microservice.getCaas().equals(CaaS.RANCHER) && microservice.getRancherConfiguration() == null
				|| microservice.getCaas().equals(CaaS.OPENSHIFT) && microservice.getOpenshiftConfiguration() == null) {
			throw new MicroserviceException(NO_CAAS_CONFIGURATION_ERROR);
		}
	}

	@Override
	public ZipMicroservice generateArchitectureMS(MicroserviceDTO microservice, MSConfig config)
			throws URISyntaxException, IOException {
		ResponseEntity<byte[]> response = new ResponseEntity<>(HttpStatus.ACCEPTED);
		byte[] fileBytes = null;
		MSMultipartFile file = null;
		final URIBuilder url = new URIBuilder(ARCHITECTURE_URL);
		final String msName = microservice.getName().replace("-", "").replace("_", "");

		if (config.isDependencyDrools()) {
			url.addParameter(LIBRARIES, "drools-platform");
		}
		if (config.isDependencyHazelcast()) {
			url.addParameter(LIBRARIES, "hazelcast-platform");
		}
		if (config.isDependencyUserAdmin()) {
			url.addParameter(LIBRARIES, "user-administrator");
		}
		if (config.isModuleSSO()) {
			url.addParameter(MODULES, "sso-project");
		}
		if (config.isModuleSpringBatch()) {
			url.addParameter(MODULES, "spring-batch-example");
		}
		if (config.isModuleUserAdmin()) {
			url.addParameter(MODULES, "user-administrator");
		}
		url.addParameter("name", msName);
		final String headers = "Authorization: " + INITIALIZR_TOKEN;

		response = sendHttp(url.toString(), HttpMethod.GET, null, headers);
		if (response != null) {
			fileBytes = response.getBody();
		}

		file = new MSMultipartFile(fileBytes, ONESAIT + msName + ".zip");

		final ZipMicroservice zipMS = new ZipMicroservice();
		zipMS.setDocker(BACKENDPROJECT + ONESAIT + msName + "/sources/docker/");
		zipMS.setSources(BACKENDPROJECT + ONESAIT + msName + "/sources/");
		zipMS.setFile(file);

		return zipMS;
	}

	private ResponseEntity<byte[]> sendHttp(String url, HttpMethod httpMethod, String body, String headersStr)
			throws URISyntaxException, IOException {
		RestTemplate restTemplate = null;
		restTemplate = new RestTemplate(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());
		restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
		final HttpHeaders headers = toHttpHeaders(headersStr);
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
		final org.springframework.http.HttpEntity<String> request = new org.springframework.http.HttpEntity<>(body,
				headers);
		if (log.isDebugEnabled()) {
			log.debug("Sending method {}", httpMethod.toString());
		}
		ResponseEntity<byte[]> response = new ResponseEntity<>(HttpStatus.ACCEPTED);
		try {
			response = restTemplate.exchange(new URI(url), httpMethod, request, byte[].class);
		} catch (final Exception e) {
			log.error(e.getMessage());
		}
		if (log.isDebugEnabled()) {
			log.debug("Execute method {} {}", httpMethod.toString(), url);
		}
		final HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("Content-Type", response.getHeaders().getContentType().toString());
		return new ResponseEntity<>(response.getBody(), responseHeaders,
				HttpStatus.valueOf(response.getStatusCode().value()));
	}

	private HttpHeaders toHttpHeaders(String headersStr) {
		final HttpHeaders httpHeaders = new HttpHeaders();
		final String[] heads = headersStr.split("\n");
		String headerName = "";
		String headerValue = "";
		for (final String head : heads) {
			headerName = head.split(":")[0];
			headerValue = head.substring(head.indexOf(':') + 1).trim();
			httpHeaders.add(headerName, headerValue);
		}
		return httpHeaders;
	}

}
