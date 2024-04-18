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
package com.minsait.onesait.platform.controlpanel.gravitee.dto;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class IdentityProvider {

	private String description;
	private String name;
	private boolean enabled;
	private JsonNode configuration;
	private ArrayNode roleMappings;
	private JsonNode userProfileMapping;
	private String type;

	private static final String PORTAL_USER = "            \"portal\": \"USER\",";
	private static final String BRACKETS_LEFT = "        {";
	private static final String BRACKETS_RIGHT = "        },";
	private static final String SERVER_NAME = "SERVER_NAME";
	private static final String DEFAULT_SERVER_NAME = "localhost";
	private static final int DEFAULT_OAUTH_PORT = 21000;

	private static final String CONSTANT_N = "\n";

	public static final String DEFAULT_OAUTH_RESOURCE_2_UPDATE_G3 = "{" + CONSTANT_N
			+ "    \"name\": \"onesait account\"," + CONSTANT_N + "    \"enabled\": true," + CONSTANT_N
			+ "    \"configuration\": {" + CONSTANT_N + "        \"scopes\": [" + CONSTANT_N + "            \"openid\""
			+ CONSTANT_N + "        ]," + CONSTANT_N + "        \"clientId\": \"onesaitplatform\"," + CONSTANT_N
			+ "        \"clientSecret\": \"onesaitplatform\"," + CONSTANT_N
			+ "        \"tokenEndpoint\": \"https://{{SERVER_NAME}}/oauth-server/oauth/token\"," + CONSTANT_N
			+ "        \"tokenIntrospectionEndpoint\": \"https://{{SERVER_NAME}}/oauth-server/openplatform-oauth/tokenInfo\","
			+ CONSTANT_N + "        \"checkTokenEndpoint\": \"https://{{SERVER_NAME}}/oauth-server/oauth/check_token\","
			+ CONSTANT_N + "        \"authorizeEndpoint\": \"https://{{SERVER_NAME}}/oauth-server/oauth/authorize\","
			+ CONSTANT_N + "        \"userInfoEndpoint\": \"https://{{SERVER_NAME}}/oauth-server/oidc/userinfo\","
			+ CONSTANT_N + "        \"userLogoutEndpoint\": \"https://{{SERVER_NAME}}/oauth-server/logout\","
			+ CONSTANT_N + "        \"color\": \"#74e1f1\"" + CONSTANT_N + "    }," + CONSTANT_N
			+ "    \"roleMappings\": [" + CONSTANT_N + BRACKETS_LEFT + CONSTANT_N
			+ "\"organizations\":[\"USER\"],\"environments\":{\"DEFAULT\":[\"USER\"]},"
			+ "            \"condition\": \"{#jsonPath(#profile, '$.role') == \\\"ROLE_USER\\\"}\"" + CONSTANT_N
			+ CONSTANT_N + BRACKETS_RIGHT + CONSTANT_N + BRACKETS_LEFT + CONSTANT_N
			+ "\"organizations\":[\"USER\"],\"environments\":{\"DEFAULT\":[\"API_PUBLISHER\"]},"
			+ "            \"condition\": \"{#jsonPath(#profile, '$.role') == \\\"ROLE_DEVELOPER\\\"}\"" + CONSTANT_N
			+ BRACKETS_RIGHT + CONSTANT_N + BRACKETS_LEFT + CONSTANT_N
			+ "\"organizations\":[\"USER\"],\"environments\":{\"DEFAULT\":[\"API_PUBLISHER\"]},"
			+ "            \"condition\": \"{#jsonPath(#profile, '$.role') == \\\"ROLE_DATASCIENTIST\\\"}\""
			+ CONSTANT_N + BRACKETS_RIGHT + CONSTANT_N + BRACKETS_LEFT + CONSTANT_N
			+ "\"organizations\":[\"ADMIN\"],\"environments\":{\"DEFAULT\":[\"ADMIN\"]},"
			+ "            \"condition\": \"{#jsonPath(#profile, '$.role') == \\\"ROLE_ADMINISTRATOR\\\"}\""
			+ CONSTANT_N + "        }" + CONSTANT_N + "    ]," + CONSTANT_N + "    \"userProfileMapping\": {"
			+ CONSTANT_N + "        \"id\": \"username\"," + CONSTANT_N + "        \"firstname\": \"name\","
			+ CONSTANT_N + "        \"lastname\": \"userid\"," + CONSTANT_N + "        \"email\": \"mail\"" + CONSTANT_N
			+ "    }" + CONSTANT_N + "}";
	public static final String DEFAULT_KEYCLOAK_RESOURCE_2_UPDATE_G3 = "{" + CONSTANT_N
			+ "    \"name\": \"onesait account\"," + CONSTANT_N + "    \"enabled\": true," + CONSTANT_N
			+ "    \"configuration\": {" + CONSTANT_N + "        \"scopes\": [" + CONSTANT_N + "            \"openid\""
			+ CONSTANT_N + "        ]," + CONSTANT_N + "        \"clientId\": \"onesaitplatform\"," + CONSTANT_N
			+ "        \"clientSecret\": \"onesaitplatform\"," + CONSTANT_N
			+ "        \"tokenEndpoint\": \"https://{{SERVER_NAME}}/auth/realms/onesaitplatform/protocol/openid-connect/token\","
			+ CONSTANT_N
			+ "        \"tokenIntrospectionEndpoint\": \"https://{{SERVER_NAME}}/auth/realms/onesaitplatform/protocol/openid-connect/token/introspect\","
			+ CONSTANT_N
			+ "        \"checkTokenEndpoint\": \"https://{{SERVER_NAME}}/auth/realms/onesaitplatform/protocol/openid-connect/token/introspect\","
			+ CONSTANT_N
			+ "        \"authorizeEndpoint\": \"https://{{SERVER_NAME}}/auth/realms/onesaitplatform/protocol/openid-connect/auth\","
			+ CONSTANT_N
			+ "        \"userInfoEndpoint\": \"https://{{SERVER_NAME}}/auth/realms/onesaitplatform/protocol/openid-connect/userinfo\","
			+ CONSTANT_N
			+ "        \"userLogoutEndpoint\": \"https://{{SERVER_NAME}}/auth/realms/onesaitplatform/protocol/openid-connect/logout\","
			+ CONSTANT_N + "        \"color\": \"#74e1f1\"" + CONSTANT_N + "    }," + CONSTANT_N
			+ "    \"roleMappings\": [" + CONSTANT_N + BRACKETS_LEFT + CONSTANT_N
			+ "\"organizations\":[\"USER\"],\"environments\":{\"DEFAULT\":[\"USER\"]},"
			+ "            \"condition\": \"{#jsonPath(#profile, '$.role') == \\\"ROLE_USER\\\"}\"" + CONSTANT_N
			+ CONSTANT_N + BRACKETS_RIGHT + CONSTANT_N + BRACKETS_LEFT + CONSTANT_N
			+ "\"organizations\":[\"USER\"],\"environments\":{\"DEFAULT\":[\"API_PUBLISHER\"]},"
			+ "            \"condition\": \"{#jsonPath(#profile, '$.role') == \\\"ROLE_DEVELOPER\\\"}\"" + CONSTANT_N
			+ BRACKETS_RIGHT + CONSTANT_N + BRACKETS_LEFT + CONSTANT_N
			+ "\"organizations\":[\"USER\"],\"environments\":{\"DEFAULT\":[\"API_PUBLISHER\"]},"
			+ "            \"condition\": \"{#jsonPath(#profile, '$.role') == \\\"ROLE_DATASCIENTIST\\\"}\""
			+ CONSTANT_N + BRACKETS_RIGHT + CONSTANT_N + BRACKETS_LEFT + CONSTANT_N
			+ "\"organizations\":[\"ADMIN\"],\"environments\":{\"DEFAULT\":[\"ADMIN\"]},"
			+ "            \"condition\": \"{#jsonPath(#profile, '$.role') == \\\"ROLE_ADMINISTRATOR\\\"}\""
			+ CONSTANT_N + "        }" + CONSTANT_N + "    ]," + CONSTANT_N + "    \"userProfileMapping\": {"
			+ CONSTANT_N + "        \"id\": \"username\"," + CONSTANT_N + "        \"firstname\": \"name\","
			+ CONSTANT_N + "        \"lastname\": \"userid\"," + CONSTANT_N + "        \"email\": \"mail\"" + CONSTANT_N
			+ "    }" + CONSTANT_N + "}";

	public static final String DEFAULT_OAUTH_RESOURCE_2_UPDATE = "{" + CONSTANT_N + "    \"name\": \"onesait account\","
			+ CONSTANT_N + "    \"enabled\": true," + CONSTANT_N + "    \"configuration\": {" + CONSTANT_N
			+ "        \"scopes\": [" + CONSTANT_N + "            \"openid\"" + CONSTANT_N + "        ]," + CONSTANT_N
			+ "        \"clientId\": \"onesaitplatform\"," + CONSTANT_N
			+ "        \"clientSecret\": \"onesaitplatform\"," + CONSTANT_N
			+ "        \"tokenEndpoint\": \"https://{{SERVER_NAME}}/oauth-server/oauth/token\"," + CONSTANT_N
			+ "        \"tokenIntrospectionEndpoint\": \"https://{{SERVER_NAME}}/oauth-server/openplatform-oauth/tokenInfo\","
			+ CONSTANT_N + "        \"checkTokenEndpoint\": \"https://{{SERVER_NAME}}/oauth-server/oauth/check_token\","
			+ CONSTANT_N + "        \"authorizeEndpoint\": \"https://{{SERVER_NAME}}/oauth-server/oauth/authorize\","
			+ CONSTANT_N + "        \"userInfoEndpoint\": \"https://{{SERVER_NAME}}/oauth-server/oidc/userinfo\","
			+ CONSTANT_N + "        \"userLogoutEndpoint\": \"https://{{SERVER_NAME}}/oauth-server/logout\","
			+ CONSTANT_N + "        \"color\": \"#74e1f1\"" + CONSTANT_N + "    }," + CONSTANT_N
			+ "    \"roleMappings\": [" + CONSTANT_N + BRACKETS_LEFT + CONSTANT_N
			+ "            \"condition\": \"{#jsonPath(#profile, '$.role') == \\\"ROLE_USER\\\"}\"," + CONSTANT_N
			+ PORTAL_USER + CONSTANT_N + "            \"management\": \"USER\"" + CONSTANT_N + BRACKETS_RIGHT
			+ CONSTANT_N + BRACKETS_LEFT + CONSTANT_N
			+ "            \"condition\": \"{#jsonPath(#profile, '$.role') == \\\"ROLE_DEVELOPER\\\"}\"," + CONSTANT_N
			+ PORTAL_USER + CONSTANT_N + "            \"management\": \"API_PUBLISHER\"" + CONSTANT_N + BRACKETS_RIGHT
			+ CONSTANT_N + BRACKETS_LEFT + CONSTANT_N
			+ "            \"condition\": \"{#jsonPath(#profile, '$.role') == \\\"ROLE_DATASCIENTIST\\\"}\","
			+ CONSTANT_N + PORTAL_USER + CONSTANT_N + "            \"management\": \"API_PUBLISHER\"" + CONSTANT_N
			+ BRACKETS_RIGHT + CONSTANT_N + BRACKETS_LEFT + CONSTANT_N
			+ "            \"condition\": \"{#jsonPath(#profile, '$.role') == \\\"ROLE_ADMINISTRATOR\\\"}\","
			+ CONSTANT_N + "            \"portal\": \"ADMIN\"," + CONSTANT_N + "            \"management\": \"ADMIN\""
			+ CONSTANT_N + "        }" + CONSTANT_N + "    ]," + CONSTANT_N + "    \"userProfileMapping\": {"
			+ CONSTANT_N + "        \"id\": \"username\"," + CONSTANT_N + "        \"firstname\": \"name\","
			+ CONSTANT_N + "        \"lastname\": \"userid\"," + CONSTANT_N + "        \"email\": \"mail\"" + CONSTANT_N
			+ "    }" + CONSTANT_N + "}";
	public static final String DEFAULT_KEYCLOAK_RESOURCE_2_UPDATE = "{" + CONSTANT_N
			+ "    \"name\": \"onesait account\"," + CONSTANT_N + "    \"enabled\": true," + CONSTANT_N
			+ "    \"configuration\": {" + CONSTANT_N + "        \"scopes\": [" + CONSTANT_N + "            \"openid\""
			+ CONSTANT_N + "        ]," + CONSTANT_N + "        \"clientId\": \"onesaitplatform\"," + CONSTANT_N
			+ "        \"clientSecret\": \"onesaitplatform\"," + CONSTANT_N
			+ "        \"tokenEndpoint\": \"https://{{SERVER_NAME}}/auth/realms/onesaitplatform/protocol/openid-connect/token\","
			+ CONSTANT_N
			+ "        \"tokenIntrospectionEndpoint\": \"https://{{SERVER_NAME}}/auth/realms/onesaitplatform/protocol/openid-connect/token/introspect\","
			+ CONSTANT_N
			+ "        \"checkTokenEndpoint\": \"https://{{SERVER_NAME}}/auth/realms/onesaitplatform/protocol/openid-connect/token/introspect\","
			+ CONSTANT_N
			+ "        \"authorizeEndpoint\": \"https://{{SERVER_NAME}}/auth/realms/onesaitplatform/protocol/openid-connect/auth\","
			+ CONSTANT_N
			+ "        \"userInfoEndpoint\": \"https://{{SERVER_NAME}}/auth/realms/onesaitplatform/protocol/openid-connect/userinfo\","
			+ CONSTANT_N
			+ "        \"userLogoutEndpoint\": \"https://{{SERVER_NAME}}/auth/realms/onesaitplatform/protocol/openid-connect/logout\","
			+ CONSTANT_N + "        \"color\": \"#74e1f1\"" + CONSTANT_N + "    }," + CONSTANT_N
			+ "    \"roleMappings\": [" + CONSTANT_N + BRACKETS_LEFT + CONSTANT_N
			+ "            \"condition\": \"{#jsonPath(#profile, '$.role') == \\\"ROLE_USER\\\"}\"," + CONSTANT_N
			+ PORTAL_USER + CONSTANT_N + "            \"management\": \"USER\"" + CONSTANT_N + BRACKETS_RIGHT
			+ CONSTANT_N + BRACKETS_LEFT + CONSTANT_N
			+ "            \"condition\": \"{#jsonPath(#profile, '$.role') == \\\"ROLE_DEVELOPER\\\"}\"," + CONSTANT_N
			+ PORTAL_USER + CONSTANT_N + "            \"management\": \"API_PUBLISHER\"" + CONSTANT_N + BRACKETS_RIGHT
			+ CONSTANT_N + BRACKETS_LEFT + CONSTANT_N
			+ "            \"condition\": \"{#jsonPath(#profile, '$.role') == \\\"ROLE_DATASCIENTIST\\\"}\","
			+ CONSTANT_N + PORTAL_USER + CONSTANT_N + "            \"management\": \"API_PUBLISHER\"" + CONSTANT_N
			+ BRACKETS_RIGHT + CONSTANT_N + BRACKETS_LEFT + CONSTANT_N
			+ "            \"condition\": \"{#jsonPath(#profile, '$.role') == \\\"ROLE_ADMINISTRATOR\\\"}\","
			+ CONSTANT_N + "            \"portal\": \"ADMIN\"," + CONSTANT_N + "            \"management\": \"ADMIN\""
			+ CONSTANT_N + "        }" + CONSTANT_N + "    ]," + CONSTANT_N + "    \"userProfileMapping\": {"
			+ CONSTANT_N + "        \"id\": \"username\"," + CONSTANT_N + "        \"firstname\": \"name\","
			+ CONSTANT_N + "        \"lastname\": \"userid\"," + CONSTANT_N + "        \"email\": \"mail\"" + CONSTANT_N
			+ "    }" + CONSTANT_N + "}";

	public static final String DEFAULT_OAUTH_RESOURCE_2_CREATE = "{" + CONSTANT_N + "    \"name\": \"onesait account\","
			+ CONSTANT_N + "    \"description\": \"onesait oauth server\"," + CONSTANT_N + "    \"type\": \"oidc\","
			+ CONSTANT_N + "    \"enabled\": true," + CONSTANT_N + "    \"configuration\": {" + CONSTANT_N
			+ "        \"scopes\": [" + CONSTANT_N + "            \"openid\"" + CONSTANT_N + "        ]," + CONSTANT_N
			+ "        \"clientId\": \"onesaitplatform\"," + CONSTANT_N
			+ "        \"clientSecret\": \"onesaitplatform\"," + CONSTANT_N
			+ "        \"tokenEndpoint\": \"https://{{SERVER_NAME}}/controlpanel/oauth/token\"," + CONSTANT_N
			+ "        \"tokenIntrospectionEndpoint\": \"https://{{SERVER_NAME}}/controlpanel/oauth/check_token\","
			+ CONSTANT_N + "        \"checkTokenEndpoint\": \"https://{{SERVER_NAME}}/controlpanel/oauth/check_token\","
			+ CONSTANT_N + "        \"authorizeEndpoint\": \"https://{{SERVER_NAME}}/controlpanel/oauth/authorize\","
			+ CONSTANT_N + "        \"userInfoEndpoint\": \"https://{{SERVER_NAME}}/oauth-server/oidc/userinfo\","
			+ CONSTANT_N + "        \"userLogoutEndpoint\": \"https://{{SERVER_NAME}}/controlpanel/logout\","
			+ CONSTANT_N + "        \"color\": \"#74e1f1\"" + CONSTANT_N + "    }," + CONSTANT_N
			+ "    \"userProfileMapping\": {" + CONSTANT_N + "        \"id\": \"username\"," + CONSTANT_N
			+ "        \"firstname\": \"name\"," + CONSTANT_N + "        \"lastname\": \"userid\"," + CONSTANT_N
			+ "        \"email\": \"mail\"" + CONSTANT_N + "    }" + CONSTANT_N + "}";

	public static final String DEFAULT_KEYCLOAK_RESOURCE_2_CREATE = "{" + CONSTANT_N
			+ "    \"name\": \"onesait account\"," + CONSTANT_N + "    \"description\": \"onesait oauth server\","
			+ CONSTANT_N + "    \"type\": \"oidc\"," + CONSTANT_N + "    \"enabled\": true," + CONSTANT_N
			+ "    \"configuration\": {" + CONSTANT_N + "        \"scopes\": [" + CONSTANT_N + "            \"openid\""
			+ CONSTANT_N + "        ]," + CONSTANT_N + "        \"clientId\": \"onesaitplatform\"," + CONSTANT_N
			+ "        \"clientSecret\": \"onesaitplatform\"," + CONSTANT_N
			+ "        \"tokenEndpoint\": \"https://{{SERVER_NAME}}/auth/realms/onesaitplatform/protocol/openid-connect/token\","
			+ CONSTANT_N
			+ "        \"tokenIntrospectionEndpoint\": \"https://{{SERVER_NAME}}/auth/realms/onesaitplatform/protocol/openid-connect/token/introspect\","
			+ CONSTANT_N
			+ "        \"checkTokenEndpoint\": \"https://{{SERVER_NAME}}/auth/realms/onesaitplatform/protocol/openid-connect/token/introspect\","
			+ CONSTANT_N
			+ "        \"authorizeEndpoint\": \"https://{{SERVER_NAME}}/auth/realms/onesaitplatform/protocol/openid-connect/auth\","
			+ CONSTANT_N
			+ "        \"userInfoEndpoint\": \"https://{{SERVER_NAME}}/auth/realms/onesaitplatform/protocol/openid-connect/userinfo\","
			+ CONSTANT_N
			+ "        \"userLogoutEndpoint\": \"https://{{SERVER_NAME}}/auth/realms/onesaitplatform/protocol/openid-connect/logout\","
			+ CONSTANT_N + "        \"color\": \"#74e1f1\"" + CONSTANT_N + "    }," + CONSTANT_N
			+ "    \"userProfileMapping\": {" + CONSTANT_N + "        \"id\": \"username\"," + CONSTANT_N
			+ "        \"firstname\": \"name\"," + CONSTANT_N + "        \"lastname\": \"userid\"," + CONSTANT_N
			+ "        \"email\": \"mail\"" + CONSTANT_N + "    }" + CONSTANT_N + "}";

	public static String getOauthResourceCreate(String graviteeVersion) {
		if (graviteeVersion.startsWith("3")) {
			return DEFAULT_OAUTH_RESOURCE_2_CREATE;
		} else {
			return DEFAULT_OAUTH_RESOURCE_2_CREATE;
		}
	}

	public static String getOauthResourceUpdate(String graviteeVersion) {
		if (graviteeVersion.startsWith("3")) {
			return DEFAULT_OAUTH_RESOURCE_2_UPDATE_G3;
		} else {
			return DEFAULT_OAUTH_RESOURCE_2_UPDATE;
		}
	}

	public static IdentityProvider getFromString(String identity) throws IOException {
		final ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(compileServerName(identity), IdentityProvider.class);
	}

	private static String compileServerName(String template) {
		final Writer writer = new StringWriter();
		final StringReader reader = new StringReader(template);
		final HashMap<String, String> scopes = new HashMap<>();
		final String serverName = System.getenv(SERVER_NAME);
		if (StringUtils.isEmpty(serverName)) {
			scopes.put(SERVER_NAME, DEFAULT_SERVER_NAME + ":" + DEFAULT_OAUTH_PORT);
		} else {
			scopes.put(SERVER_NAME, serverName);
		}
		final MustacheFactory mf = new DefaultMustacheFactory();
		final Mustache mustache = mf.compile(reader, "oauth path");
		mustache.execute(writer, scopes);
		return writer.toString();
	}

}
