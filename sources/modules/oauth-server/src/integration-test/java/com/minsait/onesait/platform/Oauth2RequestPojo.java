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
package com.minsait.onesait.platform;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder
public class Oauth2RequestPojo {
	@JsonProperty("grant_type")
	private final String grantType;
	private final String username;
	private final String password;
	@JsonProperty("client_id")
	private final String clientId;
	private final String scope;
	@JsonProperty("refresh_token")
	private final String refreshToken;

	public MultiValueMap<String, String> getMultiValueMap() {

		final MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("grant_type", grantType);
		map.add("username", username);
		map.add("password", password);
		map.add("client_id", clientId);
		map.add("scope", scope);

		return map;

	}

}
