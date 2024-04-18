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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
@Builder
@Data
public class ApiOauthResource {

	public static final String AUTH_SERVER_PRIMARY = "auth-server-primary";
	// parameterize SERVER_NAME and VERTICAL
	public static final String KEYCLOAK_SERVER = "https://%s/auth/realms/%s/protocol/openid-connect";
	public static final String OAUTH_SERVER = "https://%s/oauth-server";

	public String name;
	public String type;
	public boolean enabled;
	public Configuration configuration;

	@NoArgsConstructor
	@AllArgsConstructor
	@JsonInclude(Include.NON_NULL)
	@Builder
	@Data
	public static class Configuration {
		public String authorizationServerUrl;
		public boolean useSystemProxy;
		public String introspectionEndpointMethod;
		public String scopeSeparator;
		public String userInfoEndpoint;
		public String userInfoEndpointMethod;
		public boolean useClientAuthorizationHeader;
		public String clientAuthorizationHeaderName;
		public String clientAuthorizationHeaderScheme;
		public boolean tokenIsSuppliedByQueryParam;
		public String tokenQueryParamName;
		public boolean tokenIsSuppliedByHttpHeader;
		public boolean tokenIsSuppliedByFormUrlEncoded;
		public String tokenFormUrlEncodedName;
		public String userClaim;
		public String introspectionEndpoint;
		public String clientId;
		public String clientSecret;
	}

	public static ApiOauthResource defaultApiOauthResource(boolean useKeycloak, String clientId, String clientSecret) {
		final ApiOauthResourceBuilder builder = ApiOauthResource.builder().name(AUTH_SERVER_PRIMARY).type("oauth2")
				.enabled(true);
		if (useKeycloak) {
			final String uri = String.format(KEYCLOAK_SERVER, System.getenv("SERVER_NAME"),
					Tenant2SchemaMapper.extractVerticalNameFromSchema(MultitenancyContextHolder.getVerticalSchema()));
			return builder.configuration(Configuration.builder().authorizationServerUrl(uri).useSystemProxy(false)
					.introspectionEndpointMethod("POST").scopeSeparator(" ").userInfoEndpoint("/userinfo")
					.userInfoEndpointMethod("GET").useClientAuthorizationHeader(true)
					.clientAuthorizationHeaderName("Authorization").clientAuthorizationHeaderScheme("Basic")
					.tokenIsSuppliedByQueryParam(false).tokenQueryParamName("token").tokenIsSuppliedByHttpHeader(false)
					.tokenIsSuppliedByFormUrlEncoded(true).tokenFormUrlEncodedName("token").userClaim("username")
					.introspectionEndpoint("/token/introspect").clientId(clientId).clientSecret(clientSecret).build())
					.build();

		} else {
			final String uri = String.format(OAUTH_SERVER, System.getenv("SERVER_NAME"));
			return builder.configuration(Configuration.builder().authorizationServerUrl(uri).useSystemProxy(false)
					.introspectionEndpointMethod("POST").scopeSeparator(" ").userInfoEndpoint("/oidc/userinfo")
					.userInfoEndpointMethod("GET").useClientAuthorizationHeader(true)
					.clientAuthorizationHeaderName("Authorization").clientAuthorizationHeaderScheme("Basic")
					.tokenIsSuppliedByQueryParam(true).tokenQueryParamName("token").tokenIsSuppliedByHttpHeader(false)
					.tokenIsSuppliedByFormUrlEncoded(false).tokenFormUrlEncodedName("token").userClaim("username")
					.introspectionEndpoint("/oauth/check_token").clientId(clientId).clientSecret(clientSecret).build())
					.build();

		}

	}
}
