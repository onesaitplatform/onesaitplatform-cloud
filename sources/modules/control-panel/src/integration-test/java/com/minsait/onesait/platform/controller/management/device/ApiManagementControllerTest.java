/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
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
package com.minsait.onesait.platform.controller.management.device;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.commons.testing.IntegrationTest;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Api.ApiStates;
import com.minsait.onesait.platform.config.model.UserApi;
import com.minsait.onesait.platform.config.repository.ApiRepository;
import com.minsait.onesait.platform.config.repository.UserApiRepository;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.ControlPanelWebApplication;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ControlPanelWebApplication.class)
@Category(IntegrationTest.class)
@ContextConfiguration
@WebAppConfiguration
@Slf4j
@Ignore
public class ApiManagementControllerTest {

	@Autowired
	private WebApplicationContext context;
	MockMvc mvc;
	@Autowired
	ObjectMapper mapper;
	private String oAuthHeader;
	private Api apiDeveloper;
	private Api apiDataViewer;
	private UserApi userApi;

	@Autowired
	UserService userService;
	@Autowired
	ApiRepository apiRepository;
	@Autowired
	UserApiRepository userApiRepository;

	@Before
	public void setUp() throws Exception {
		mvc = MockMvcBuilders.webAppContextSetup(context).build();
		String response = mvc
				.perform(MockMvcRequestBuilders.post("/api-ops/login").contentType(MediaType.APPLICATION_JSON)
						.content("{\"password\":\"Changed!\",\"username\":\"developer\"}"))
				.andReturn().getResponse().getContentAsString();
		JsonNode responseJson = this.mapper.readValue(response, JsonNode.class);
		this.oAuthHeader = "Bearer " + responseJson.get("access_token").asText();
		log.info(oAuthHeader);

		this.apiDeveloper = new Api();
		apiDeveloper.setSsl_certificate(false);
		apiDeveloper.setIdentification("Api get tickets");
		apiDeveloper.setDescription("Api for testing");
		apiDeveloper.setState(ApiStates.CREATED);
		apiDeveloper.setPublic(false);
		apiDeveloper.setUser(this.userService.getUser("developer"));

		this.apiDataViewer = new Api();
		apiDataViewer.setSsl_certificate(false);
		apiDataViewer.setIdentification("Api get tickets");
		apiDataViewer.setDescription("Api for testing");
		apiDataViewer.setState(ApiStates.CREATED);
		apiDataViewer.setPublic(false);
		apiDataViewer.setUser(this.userService.getUser("dataviewer"));

		this.apiDeveloper = this.apiRepository.save(apiDeveloper);
		this.apiDataViewer = this.apiRepository.save(apiDataViewer);

		this.userApi = new UserApi();
		userApi.setApi(apiDeveloper);
		userApi.setUser(this.userService.getUser("dataviewer"));
		this.userApi = this.userApiRepository.save(userApi);

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

	}

	@After
	public void tearDown() {
		this.userApiRepository.delete(userApi);
		this.apiRepository.delete(apiDataViewer);
		this.apiRepository.delete(apiDeveloper);
	}

	@Test
	public void oauthToken_isNotNull() {
		Assert.assertNotNull(oAuthHeader);
	}

	@Test
	public void user_triesToChangeItsApiAuthorizations_andGetsOk() throws Exception {
		mvc.perform(MockMvcRequestBuilders
				.post("/management/authorize/api/" + this.apiDeveloper.getId() + "/user/analytics")
				.header("Authorization", this.oAuthHeader)).andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
		mvc.perform(MockMvcRequestBuilders
				.post("/management/deauthorize/api/" + this.apiDeveloper.getId() + "/user/analytics")
				.header("Authorization", this.oAuthHeader)).andExpect(MockMvcResultMatchers.status().is2xxSuccessful());

	}

	@Test
	public void user_triesToChangeOthersApiAuthorizations_andGetsKo() throws Exception {
		mvc.perform(MockMvcRequestBuilders
				.post("/management/authorize/api/" + this.apiDataViewer.getId() + "/user/analytics")
				.header("Authorization", this.oAuthHeader))
				.andExpect(MockMvcResultMatchers.status().is4xxClientError());
	}
}
