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
package com.minsait.onesait.platform.flowengine.api.rest.pojo;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.minsait.onesait.platform.config.model.ApiOperation.Type;

import lombok.Getter;
import lombok.Setter;

public class RestApiInvocationParams {

	@Getter
	@Setter
	private String body;
	@Getter
	@Setter
	private MultiValueMap<String, Object> multipartData;
	@Getter
	@Setter
	private Map<String, String> queryParams;
	@Getter
	@Setter
	private Map<String, String> pathParams;
	@Getter
	@Setter
	private Type method;
	@Getter
	@Setter
	private String url;
	@Getter
	@Setter
	private HttpHeaders headers;
	@Getter
	@Setter
	private boolean isMultipart;
	
	public RestApiInvocationParams (){
		this.body = "";
		this.queryParams  = new HashMap<>();
		this.pathParams  = new HashMap<>();
		this.method = null;
		this.url = "";
		this.headers = new HttpHeaders();
		multipartData = new LinkedMultiValueMap<>();
		isMultipart = false;
	}
}
