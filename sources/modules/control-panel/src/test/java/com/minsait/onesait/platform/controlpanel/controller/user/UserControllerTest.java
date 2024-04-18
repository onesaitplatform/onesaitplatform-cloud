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
package com.minsait.onesait.platform.controlpanel.controller.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.text.SimpleDateFormat;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.ControlPanelWebApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ControlPanelWebApplication.class)
@Ignore
public class UserControllerTest {

	private MockMvc mockMvc;
	@Autowired
	WebApplicationContext context;
	@Autowired
	UserService userService;

	@Before
	public void initTests() {
		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
	}

	@Test
	public void given_AnyState_When_APortToUserCreateIsRequestedWithTheCorrectParamenters_Then_TheUserIsCreatedAndTheViewIsRedirected()
			throws Exception {
		User user = this.mockUser();
		// mock userService
		userService = Mockito.mock(UserService.class);

		// List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
		// grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMINISTRATOR"));
		// Authentication authentication = new
		// UsernamePasswordAuthenticationToken("admin", "admin", grantedAuthorities);
		// SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		// Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		// SecurityContextHolder.setContext(securityContext);

		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

		mockMvc.perform(post("/users/create").param("fullName", user.getFullName()).param("active", "true")
				.param("password", user.getPassword()).param("userId", user.getUserId()).param("email", user.getEmail())
				.param("dateCreated", formatter.format(user.getCreatedAt()))
				.param("roleTypeId.name", user.getRole().getName())).andDo(print())
				.andExpect(status().is3xxRedirection());

	}

	public User mockUser() {
		User user = new User();
		user.setActive(true);
		user.setEmail("admin@gmail.com");
		Role role = new Role();
		role.setName("ROLE_ADMINISTRATOR");
		user.setRole(role);
		user.setPassword("somePass");
		user.setCreatedAt(new java.util.Date());
		user.setRole(role);
		user.setUserId("admin");
		user.setEmail("some@email.com");
		user.setFullName("Admin s4c");
		return user;

	}
}
