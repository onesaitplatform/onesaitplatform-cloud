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
	
	
	final private static String DEVELOPER_NAME = "developer";
	final private static String API_DEVELOPER_NAME = "RestaurantsApiDev";
	final private static String API_DEVELOPER_DESC = "Api for testing user developer";
	final private static ApiStates API_DEVELOPER_STATE = ApiStates.CREATED;
	final private static boolean API_DEVELOPER_IS_PUBLIC = false;
	
	final private static String DATAVIEWER_NAME = "dataviewer";
	final private static String API_DATAVIEWER_NAME = "RestaurantsApiView";
	final private static String API_DATAVIEWER_DESC = "Api for testing user dataviewer";
	final private static ApiStates API_DATAVIEWER_STATE = ApiStates.CREATED;
	final private static boolean API_DATAVIEWER_IS_PUBLIC = false;
	
	final private static String ANALYTICS_NAME = "analytics";

	@Before
	public void setUp() throws Exception {
		mvc = MockMvcBuilders.webAppContextSetup(context).build();
		String response = mvc
				.perform(MockMvcRequestBuilders.post("/api/login").contentType(MediaType.APPLICATION_JSON)
						.content("{\"password\":\"Changed2019!\",\"username\":\"developer\"}"))
				.andReturn().getResponse().getContentAsString();
		JsonNode responseJson = this.mapper.readValue(response, JsonNode.class);
		this.oAuthHeader = "Bearer " + responseJson.get("access_token").asText();
		log.info(oAuthHeader);

		this.apiDeveloper = new Api();
		apiDeveloper.setSsl_certificate(false);
		apiDeveloper.setIdentification(API_DEVELOPER_NAME);
		apiDeveloper.setDescription(API_DEVELOPER_DESC);
		apiDeveloper.setState(API_DEVELOPER_STATE);
		apiDeveloper.setPublic(API_DEVELOPER_IS_PUBLIC);
		apiDeveloper.setUser(this.userService.getUser(DEVELOPER_NAME));

		this.apiDataViewer = new Api();
		apiDataViewer.setSsl_certificate(false);
		apiDataViewer.setIdentification(API_DATAVIEWER_NAME);
		apiDataViewer.setDescription(API_DATAVIEWER_DESC);
		apiDataViewer.setState(API_DATAVIEWER_STATE);
		apiDataViewer.setPublic(API_DATAVIEWER_IS_PUBLIC);
		apiDataViewer.setUser(this.userService.getUser(DATAVIEWER_NAME));

		this.apiDeveloper = this.apiRepository.save(apiDeveloper);
		this.apiDataViewer = this.apiRepository.save(apiDataViewer);

		this.userApi = new UserApi();
		userApi.setApi(apiDeveloper);
		userApi.setUser(this.userService.getUser(DATAVIEWER_NAME));
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
				.post("/api/apis/authorize/api/" + this.apiDeveloper.getId() + "/user/" + ANALYTICS_NAME)
				.header("Authorization", this.oAuthHeader)).andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
		mvc.perform(MockMvcRequestBuilders
				.post("/management/deauthorize/api/" + this.apiDeveloper.getId() + "/user/"  + ANALYTICS_NAME)
				.header("Authorization", this.oAuthHeader)).andExpect(MockMvcResultMatchers.status().is2xxSuccessful());

	}

	@Test
	public void user_triesToChangeOthersApiAuthorizations_andGetsKo() throws Exception {
		mvc.perform(MockMvcRequestBuilders
				.post("/api/apis/authorize/api/" + this.apiDataViewer.getId() + "/user/" + ANALYTICS_NAME)
				.header("Authorization", this.oAuthHeader))
				.andExpect(MockMvcResultMatchers.status().is4xxClientError());
	}
	
	@Test
	public void user_triesToGetAllApis_andGetsOk() throws Exception {
		mvc.perform(MockMvcRequestBuilders
				.get("/api/apis")
				.header("Authorization", this.oAuthHeader))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
	}
	
	@Test
	public void user_triesToGetAiByIdentification_andGetsOk() throws Exception {
		mvc.perform(MockMvcRequestBuilders
				.get("/api/apis/" + API_DEVELOPER_NAME)
				.header("Authorization", this.oAuthHeader))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
	}
	
	@Test
	public void user_triesToGetAiByIdentification_andGetsKo() throws Exception {
		mvc.perform(MockMvcRequestBuilders
				.get("/api/apis/" + API_DATAVIEWER_NAME)
				.header("Authorization", this.oAuthHeader))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
	}
	
}
