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
package com.minsait.onesait.platform.config.services.modelsmanager;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public interface ModelsManagerService {
	public ResponseEntity<String> sendHttp(HttpServletRequest requestServlet, HttpMethod httpMethod, String body)
			throws URISyntaxException, IOException;

	public ResponseEntity<String> sendHttp(String url, HttpMethod httpMethod, String body, String queryString)
			throws URISyntaxException, IOException;

	public ResponseEntity<String> sendHttp(String url, HttpMethod httpMethod, String body, HttpHeaders headers, String queryString)
			throws URISyntaxException, IOException;
}
