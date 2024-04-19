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
package com.minsait.onesait.platform.bpm.security.camunda;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.security.auth.AuthenticationProvider;
import org.camunda.bpm.engine.rest.security.auth.AuthenticationResult;

public class ContainerBasedAuthenticationProvider implements AuthenticationProvider {

	@Override
	public AuthenticationResult extractAuthenticatedUser(HttpServletRequest request, ProcessEngine engine) {
		final Principal principal = request.getUserPrincipal();

		if (principal == null) {
			return AuthenticationResult.unsuccessful();
		}

		final String name = principal.getName();
		if (name == null || name.isEmpty()) {
			return AuthenticationResult.unsuccessful();
		}

		return AuthenticationResult.successful(name);
	}

	@Override
	public void augmentResponseByAuthenticationChallenge(HttpServletResponse response, ProcessEngine engine) {
		// noop
	}

}