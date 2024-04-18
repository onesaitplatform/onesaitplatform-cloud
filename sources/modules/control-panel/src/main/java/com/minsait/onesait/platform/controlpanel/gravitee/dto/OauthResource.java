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

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class OauthResource {
	private String authorizationServerUrl;
	private String introspectionEndpoint;
	private String introspectionEndpointMethod;
	private String scopeSeparator;
	private String userInfoEndpoint;
	private String userInfoEndpointMethod;
	private boolean useClientAuthorizationHeader;
	private String clientAuthorizationHeaderName;
	private String clientAuthorizationHeaderScheme;
	private boolean tokenIsSuppliedByQueryParam;
	private String tokenQueryParamName;
	private boolean tokenIsSuppliedByHttpHeader;
	private boolean tokenIsSuppliedByFormUrlEncoded;
	private String tokenFormUrlEncodedName;
	private String clientId;
	private String clientSecret;

}
