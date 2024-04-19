/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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
package com.minsait.onesait.platform.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.minsait.onesait.platform.config.model.User;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class LoginController {

	@Value("${onesaitplatform.password.pattern}")
	private String passwordPattern;

	@GetMapping("login")
	public String login(Model model) {
		model.addAttribute("users", new User());
		model.addAttribute("passwordPattern", passwordPattern);
		return "login";
	}


	@GetMapping("/logout")
	public void exit(HttpServletRequest request, HttpServletResponse response) {
		// token can be revoked here if needed
		new SecurityContextLogoutHandler().logout(request, null, null);
		try {
			// sending back to client app
			response.sendRedirect(request.getHeader("referer"));
		} catch (final IOException e) {
			log.error("exit",e);
		}
	}
}
