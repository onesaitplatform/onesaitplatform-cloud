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
package com.minsait.onesait.examples.security.platform.spring;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import com.minsait.onesait.examples.security.platform.OAuthAuthorization;

public class SpringOAuthFilter extends GenericFilterBean {

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {

		HttpServletRequestWrapper request = (HttpServletRequestWrapper) servletRequest;
		if (request.authenticate((HttpServletResponse) servletResponse)) {
			OAuthAuthorization auth = (OAuthAuthorization)request.getUserPrincipal();
			
			SecurityContextHolder.getContext().setAuthentication(new OauthInfoSpring(auth));
		}
		
		filterChain.doFilter(servletRequest, servletResponse);
	}

	private class OauthInfoSpring implements Authentication{

		/**
		 * 
		 */
		private static final long serialVersionUID = 8453329447099011271L;
		private OAuthAuthorization data;
		private boolean authenticated;

		public OauthInfoSpring (OAuthAuthorization data){
			this.data = data;
			this.authenticated = data != null && data.isAuthenticated();
		}
		
		public List<GrantedAuthority> getAuthorities() {
			return data.getAuthorities().stream()
					.map(role -> new SimpleGrantedAuthority(role))
					.collect(Collectors.toList());
		}

		public Object getCredentials() {
			// TODO Auto-generated method stub
			return data.getToken();
		}

		public Object getDetails() {
			return null;
		}

		public Object getPrincipal() {
			return data;
		}

		public void setAuthenticated(boolean arg0) throws IllegalArgumentException {
			authenticated = arg0;
		}

		@Override
		public String getName() {
			return data.getName();
		}

		@Override
		public boolean isAuthenticated() {
			return authenticated;
		}
		
	}
	
}
