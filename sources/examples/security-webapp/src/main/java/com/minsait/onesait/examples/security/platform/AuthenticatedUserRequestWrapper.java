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
package com.minsait.onesait.examples.security.platform;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

public class AuthenticatedUserRequestWrapper extends HttpServletRequestWrapper {

	private OAuthAuthorization oauthInfo;

	public AuthenticatedUserRequestWrapper(HttpServletRequest request, OAuthAuthorization user) {
		super(request);
		
		this.oauthInfo = user;
	}

	@Override
	public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
		return oauthInfo != null && oauthInfo.isAuthenticated();
	}

	@Override
	public String getAuthType() {
		return "OAuth2";
	}

	@Override
	public Principal getUserPrincipal() {
		return oauthInfo;
	}

	@Override
	public boolean isUserInRole(String role) {
		return oauthInfo.getAuthorities().contains(role);
	}
	
	

}
