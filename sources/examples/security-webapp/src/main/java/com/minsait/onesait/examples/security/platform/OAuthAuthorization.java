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

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

public class OAuthAuthorization implements Principal{

	private String principal;
	private List<String> roles;
	private String token;
	
	public OAuthAuthorization() {
		roles = new ArrayList<>();
		
	}
	public OAuthAuthorization(JsonObject oauthInfo, String clientId) {
		
		principal = oauthInfo.getString("principal");
		token = oauthInfo.getString("access_token");
		JsonArray aux;
		
		if (clientId.equals(oauthInfo.getString("clientId")))
			aux = oauthInfo.getJsonArray("authorities");
		else {
			JsonObject app = oauthInfo.getJsonObject("apps");
			aux = app != null ? app.getJsonArray(clientId) : null;
		}
		
		if (aux != null)
			roles = aux.stream().map(JsonValue::toString).collect(Collectors.toList());
	}

	public String getName() {
		return this.principal;
	}

	public List<String> getAuthorities() {
		return this.roles;
	}
	
	public String getToken() {
		return token;
	}

	public boolean isAuthenticated() {
		return principal != null;
	}
	
	@Override
	public String toString() {
		if (isAuthenticated())
			return principal.toString() + ":" + roles.toString();
		return "Not Authenticated";
	}


}
