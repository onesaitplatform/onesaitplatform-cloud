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
package com.minsait.spark;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Enumeration;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class SparkAuth implements javax.servlet.Filter {

	private static final Logger log = Logger.getLogger(SparkAuth.class.getName());
	private String oauthValidatorUrl = "http://localhost:21000/oauth-server/openplatform-oauth/check_token";

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		Cookie[] cookies = request.getCookies();
		Cookie cookie = null;
		if (cookies != null) {
			for (Cookie c : cookies) {
				if (c.getName().equals("sparkAuth")) {
					cookie = c;
				}
			}
		}
		if (cookie != null) {
			// check vs oauth manager
			if (checkPlatformToken(cookie.getValue())) {
				// all good
				chain.doFilter(request, response);
			} else {
				// not valid
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			}
		} else {
			// authenticate
			String token = req.getParameter("token");
			if (token == null || token.isEmpty()) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			} else {
				if (checkPlatformToken(token)) {
					// all good, create cookie
					cookie = new Cookie("sparkAuth", token);
					response.addCookie(cookie);
					chain.doFilter(request, response);
				} else {
					// not valid
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
				}
			}

		}

	}

	private boolean checkPlatformToken(String token) throws IOException {
		String auth = "onesaitplatform:onesaitplatform";
		byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
		String authHeaderValue = "Basic " + new String(encodedAuth);

		URL url = new URL(oauthValidatorUrl + "?token=" + token);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		con.setRequestProperty("Authorization", authHeaderValue);
		con.setRequestProperty("Accept", "application/json");
		con.setDoOutput(true);
		con.connect();

		if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
			return true;
		}
		return false;
	}

	@Override
	public void init(FilterConfig cfg) throws ServletException {


		log.info("Filter starts loading init parameters.");
		@SuppressWarnings("unchecked")
		Enumeration<String> e = cfg.getInitParameterNames();
		while (e.hasMoreElements()) {
			String param = e.nextElement();
			if (log.isDebugEnabled()) {
				log.debug("lectura inicical param: " + param + " value " + cfg.getInitParameter(param));
			}			
			if (param.equals("checkTokenUrl")) {
				oauthValidatorUrl = cfg.getInitParameter(param);
			}
		}
		log.info("Filter finished loading init parameters.");

	}

}
