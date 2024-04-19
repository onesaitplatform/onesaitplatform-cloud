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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import com.minsait.onesait.platform.commons.testing.IntegrationTest;
import com.minsait.onesait.platform.config.components.JenkinsConfiguration;
import com.minsait.onesait.platform.config.components.RancherConfiguration;
import com.minsait.onesait.platform.config.model.Microservice.TemplateType;
import com.minsait.onesait.platform.config.model.UserToken;
import com.minsait.onesait.platform.config.repository.ClientPlatformRepository;
import com.minsait.onesait.platform.config.repository.UserTokenRepository;
import com.minsait.onesait.platform.config.services.deletion.EntityDeletionService;
import com.minsait.onesait.platform.config.services.microservice.dto.JenkinsParameter;
import com.minsait.onesait.platform.config.services.microservice.dto.MSConfig;
import com.minsait.onesait.platform.config.services.microservice.dto.MicroserviceDTO;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.ControlPanelWebApplication;
import com.minsait.onesait.platform.controlpanel.rest.microservice.MicroserviceEntity;
import com.minsait.onesait.platform.controlpanel.services.jenkins.JenkinsService;
import com.minsait.onesait.platform.controlpanel.services.project.MicroservicesGitServiceManager;
import com.minsait.onesait.platform.git.GitlabConfiguration;
import com.minsait.onesait.platform.git.GitlabException;

@SpringBootTest(classes = ControlPanelWebApplication.class)
@Category(IntegrationTest.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@TestPropertySource(locations = "/application-integration-test.yml")
@RunWith(SpringRunner.class)
public class MicroserviceIntegrationTest {

	@Autowired
	private JenkinsConfiguration jenkinsConfiguration;
	@Autowired
	private RancherConfiguration rancherConfiguration;
	@Autowired
	private GitlabConfiguration gitlabConfiguration;
	@Autowired
	private UserTokenRepository userTokenRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private ClientPlatformRepository clientPlatformRepository;

	@Autowired
	private MicroservicesGitServiceManager gitServiceManager;
	@Autowired
	private JenkinsService jenkinsService;

	@Value("${controlpanel}")
	private String controlPanel;

	@Autowired
	private EntityDeletionService deletionService;

	private String token;

	private final TestRestTemplate restTemplate = new TestRestTemplate();

	private MicroserviceDTO microservice;

	private MSConfig msConfig;

	public static final String MS_NAME = "restaurants-test";
	private static final String MS_NAME_INVALID = "RestaurantsTest";
	private static final String MS_CTXT_PATH = "/restaurants";
	private static int PORT = 30010;
	private static final String DOCKER_PATH = "sources/docker/";
	private static final String SOURCES_PATH = "sources/";
	private static final String ONTOLOGY = "restaurants";
	private static final String REGISTRY = "registry.onesaitplatform.com";
	private static final String BASE_URL = "/api/microservices";
	private static final String USER = "developer";
	private static final String X_OP_APIKEY = "X-OP-APIKey";
	private static final String BRANCHNAME = "master";
	private static final String BRANCHNAME_PARAM = "GIT_BRANCHNAME";
	private static final String REGISTRY_PARAM = "PRIVATE_REGISTRY";
	private static final String DEVICE_NAME = "restaurantsDeviceMicroservice";

	@Before
	public void setUp() {
		microservice = MicroserviceDTO.builder().contextPath(MS_CTXT_PATH).port(PORT).name(MS_NAME)
				.gitlabConfiguration(gitlabConfiguration).jenkinsConfiguration(jenkinsConfiguration)
				.rancherConfiguration(rancherConfiguration).template(TemplateType.IOT_CLIENT_ARCHETYPE).build();
		msConfig = MSConfig.builder().createGitlab(true).defaultCaaS(false).defaultGitlab(false).defaultJenkins(false)
				.docker(DOCKER_PATH).sources(SOURCES_PATH).ontology(ONTOLOGY).build();
		setXOPAPIKey();

	}

	@Test
	public void a_WhenMSDataAndMSConfigAreProvided_Expect_MicroserviceCreated() {
		final ResponseEntity<String> response = restTemplate.exchange(getURL(), HttpMethod.POST,
				new HttpEntity<>(MicroserviceEntity.builder().microservice(microservice).config(msConfig).build(),
						headers()),
				String.class);

		assertTrue(response.getStatusCode().equals(HttpStatus.CREATED));
	}

	@Test
	public void b_WhenRetrievingJenkinsBuildParameters_Expect_RegistryOKAndGitBranchMaster() {
		final ResponseEntity<List<JenkinsParameter>> response = restTemplate.exchange(
				getURL() + "/" + MS_NAME + "/build/parameters", HttpMethod.GET, new HttpEntity<>(null, headers()),
				new ParameterizedTypeReference<List<JenkinsParameter>>() {
				});
		assertTrue(response.getStatusCode().equals(HttpStatus.OK));
		assertTrue(!CollectionUtils.isEmpty(response.getBody()));
		assertTrue(response.getBody().stream()
				.anyMatch(p -> p.getName().equals(BRANCHNAME_PARAM) && p.getValue().equals(BRANCHNAME)));
		assertTrue(response.getBody().stream()
				.anyMatch(p -> p.getName().equals(REGISTRY_PARAM) && p.getValue().equals(REGISTRY)));

	}

	@Test
	public void c_WhenBuildingJenkinsPipelineWithDefaultParameters_Expect_BuildingOK() {
		final ResponseEntity<List<JenkinsParameter>> response = restTemplate.exchange(
				getURL() + "/" + MS_NAME + "/build/parameters", HttpMethod.GET, new HttpEntity<>(null, headers()),
				new ParameterizedTypeReference<List<JenkinsParameter>>() {
				});
		assertTrue(response.getStatusCode().equals(HttpStatus.OK));
		assertTrue(!CollectionUtils.isEmpty(response.getBody()));
		final ResponseEntity<String> buildResponse = restTemplate.exchange(getURL() + "/" + MS_NAME + "/build",
				HttpMethod.POST, new HttpEntity<>(response.getBody(), headers()), String.class);
		assertTrue(buildResponse.getStatusCode().equals(HttpStatus.OK));

	}

	@Test
	public void d_WhenRemovingMicroservice_Then_JenkinsPipelineIsNotRemoved() throws GitlabException {
		final ResponseEntity<String> response = restTemplate.exchange(getURL() + "/" + MS_NAME, HttpMethod.DELETE,
				new HttpEntity<>(null, headers()), String.class);
		assertTrue(response.getStatusCode().equals(HttpStatus.OK));
		assertNotNull(jenkinsService.getJobInfo(jenkinsConfiguration.getJenkinsUrl(),
				jenkinsConfiguration.getUsername(), jenkinsConfiguration.getToken(), MS_NAME + "-pipeline", null));

		// By default when deleting microservices pipelines are not removed
		jenkinsService.deleteJob(jenkinsConfiguration.getJenkinsUrl(), jenkinsConfiguration.getUsername(),
				jenkinsConfiguration.getToken(), MS_NAME + "-pipeline", null);
		gitServiceManager.dispatchService(gitlabConfiguration).deleteGitlabProject(MS_NAME, MS_NAME,
				gitlabConfiguration.getSite(), gitlabConfiguration.getPrivateToken());
		deletionService.deleteClient(clientPlatformRepository.findByIdentification(DEVICE_NAME).getId());
	}

	private String getURL() {
		return controlPanel + BASE_URL;
	}

	private HttpHeaders headers() {
		final HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_UTF8_VALUE);
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
		headers.add(X_OP_APIKEY, token);
		return headers;
	}

	private void setXOPAPIKey() {
		final List<UserToken> tokens = userTokenRepository.findByUser(userService.getUser(USER));
		if (tokens.isEmpty()) {
			final UserToken newToken = new UserToken();
			newToken.setUser(userService.getUser(USER));
			newToken.setToken(UUID.randomUUID().toString());
			token = userTokenRepository.save(newToken).getToken();
		} else {
			token = tokens.get(0).getToken();
		}

	}

}
