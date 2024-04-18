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
package com.minsait.onesait.platform.controlpanel.rest.management.login;

import static com.minsait.onesait.platform.controlpanel.rest.management.login.LoginPostUrl.OP_LOGIN;

import java.security.Principal;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.config.model.security.UserPrincipal;
import com.minsait.onesait.platform.controlpanel.rest.management.login.model.RequestLogin;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;
import com.minsait.onesait.platform.security.PlugableOauthAuthenticator;
import com.minsait.onesait.platform.security.jwt.ri.CustomTokenService;
import com.minsait.onesait.platform.security.jwt.ri.ResponseToken;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Login Oauth")
@RestController
@RequestMapping("api" + OP_LOGIN)
@ConditionalOnMissingBean(PlugableOauthAuthenticator.class)
@Slf4j
public class LoginManagementController {

	private static final String ERROR_STR = " Error: ";

	@Autowired
	private TokenEndpoint tokenEndpoint;

	@Autowired
	private ClientDetailsService clientDetailsService;

	@Autowired
	private AuthorizationServerTokenServices tokenServices;

	@Autowired
	private MultitenancyService multitenancyService;

	@Autowired
	CustomTokenService customTokenService;

	@Value("${security.jwt.client-id}")
	private String clientId;

	private static final String PSWD_STR = "password";
	private static final String REFRESH_TOKEN = "refresh_token";
	private static final String USERNAME = "username";
	private static final String GRANT_TYPE = "grant_type";

	private static final String ERROR_RESPONSE = "Leaving Info Token with with Error response = ";

	@Operation(summary = "Post Login Oauth2")
	@GetMapping("principal")
	public ResponseEntity<OAuth2AccessToken> principal(Authentication auth, HttpServletRequest request) {
		final OAuth2AccessToken token = postLoginOauthNopass(auth);
		return new ResponseEntity<>(token, HttpStatus.OK);

	}

	@Operation(summary = "Post Login Oauth2")
	@PostMapping
	public ResponseEntity<OAuth2AccessToken> postLoginOauth2(@Valid @RequestBody RequestLogin request) {

		try {
			if (StringUtils.hasText(request.getVertical())) {
				multitenancyService.getVertical(request.getVertical()).ifPresent(v -> {
					MultitenancyContextHolder.setVerticalSchema(v.getSchema());
					MultitenancyContextHolder.setForced(true);
				});
			}
			final ClientDetails authenticatedClient = clientDetailsService.loadClientByClientId(clientId);

			final Map<String, String> parameters = new HashMap<>();
			parameters.put(GRANT_TYPE, PSWD_STR);
			parameters.put(USERNAME, request.getUsername());
			parameters.put(PSWD_STR, request.getPassword());
			parameters.put("vertical", request.getVertical());

			final Principal principal = new UsernamePasswordAuthenticationToken(authenticatedClient.getClientId(),
					authenticatedClient.getClientSecret(), authenticatedClient.getAuthorities());
			final ResponseEntity<OAuth2AccessToken> token = tokenEndpoint.postAccessToken(principal, parameters);

			final OAuth2AccessToken accessToken = token.getBody();
			multitenancyService.updateLastLogin(request.getUsername());
			return getResponse(accessToken);

		} catch (final Exception e) {
			log.error(OP_LOGIN + ERROR_STR + e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}

	public OAuth2AccessToken postLoginOauthNopass(Authentication authentication) {

		String vertical = null;
		if (authentication.getPrincipal() instanceof UserPrincipal) {
			vertical = ((UserPrincipal) authentication.getPrincipal()).getVertical();
		}
		final HashMap<String, String> authorizationParameters = new HashMap<>();
		authorizationParameters.put("scope", "openid");
		authorizationParameters.put(USERNAME, authentication.getName());
		authorizationParameters.put("client_id", clientId);
		authorizationParameters.put("vertical", vertical);

		final Set<String> responseType = new HashSet<>();
		responseType.add(PSWD_STR);

		final Set<String> scopes = new HashSet<>();
		scopes.add("openid");

		final OAuth2Request authorizationRequest = new OAuth2Request(authorizationParameters, clientId,
				authentication.getAuthorities(), true, scopes, null, "", responseType, null);

		final UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
				authentication.getPrincipal(), null, authentication.getAuthorities());

		final OAuth2Authentication authenticationRequest = new OAuth2Authentication(authorizationRequest,
				authenticationToken);
		authenticationRequest.setAuthenticated(true);

		return tokenServices.createAccessToken(authenticationRequest);

	}

	@Operation(summary = "GET Login Oauth2")
	@GetMapping(value = "/username/{username}/password/{password}")
	@Deprecated
	public ResponseEntity<OAuth2AccessToken> getLoginOauth2(
			@Parameter(description = USERNAME, required = true) @PathVariable(USERNAME) String username,
			@Parameter(description = PSWD_STR, required = true) @PathVariable(name = PSWD_STR) String password,
			@Parameter(description = "vertical", required = false) @RequestParam(name = "vertical") String vertical) {

		try {

			final RequestLogin request = new RequestLogin();
			request.setPassword(password);
			request.setUsername(username);
			request.setVertical(vertical);

			return postLoginOauth2(request);

		} catch (final Exception e) {
			log.error(OP_LOGIN + ERROR_STR + e.getMessage(), e);
			log.error(OP_LOGIN + ERROR_STR + e.getStackTrace());
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}
	@Operation(summary = "Get the refresh token by token Id")
	@PostMapping(value = "/refresh")
	public ResponseEntity<OAuth2AccessToken> renewToken(@RequestBody String id) {
		try {

			log.info("Entering Renew Token with id = " + id);

			final OAuth2Authentication authentication = customTokenService.loadAuthentication(id);
			final OAuth2AccessToken token = customTokenService.getAccessToken(authentication);

			final OAuth2RefreshToken refreshToken = token.getRefreshToken();

			final String client = authentication.getOAuth2Request().getClientId();
			final Collection<String> scope = authentication.getOAuth2Request().getScope();
			final Map<String, String> parameters = authentication.getOAuth2Request().getRequestParameters();

			final TokenRequest tokenRequest = new TokenRequest(parameters, client, scope, PSWD_STR);

			final OAuth2AccessToken tokenRefreshed = customTokenService.refreshAccessToken(refreshToken.getValue(),
					tokenRequest);

			return getResponse(tokenRefreshed);

		} catch (final Exception e) {
			final ResponseToken r = new ResponseToken();
			r.setToken("-1");
			log.info(ERROR_RESPONSE + e.getLocalizedMessage());
			return getResponse(null);
		}

	}
	
	@Operation(summary = "Refreshes the access token by refresh token")
	@PostMapping(value = "/refresh_token")
	public ResponseEntity<OAuth2AccessToken> refreshToken(@RequestBody String refreshToken) {
		try {

			final ClientDetails authenticatedClient = clientDetailsService.loadClientByClientId(clientId);

			final Map<String, String> parameters = new HashMap<>();
			parameters.put(GRANT_TYPE, REFRESH_TOKEN);
			parameters.put(REFRESH_TOKEN, refreshToken);

			final Principal principal = new UsernamePasswordAuthenticationToken(authenticatedClient.getClientId(),
					authenticatedClient.getClientSecret(), authenticatedClient.getAuthorities());
			final ResponseEntity<OAuth2AccessToken> token = tokenEndpoint.postAccessToken(principal, parameters);

			final OAuth2AccessToken accessToken = token.getBody();

			return getResponse(accessToken);

		} catch (final Exception e) {
			final ResponseToken r = new ResponseToken();
			r.setToken("-1");
			log.info(ERROR_RESPONSE + e.getLocalizedMessage());
			return getResponse(null);
		}

	}

	// public OAuth2AccessToken enhance(OAuth2AccessToken accessToken,
	// OAuth2Authentication authentication) {
	// final Map<String, Object> additionalInfo = new HashMap<>();
	// additionalInfo.put("name", authentication.getName());
	//
	// final Collection<GrantedAuthority> authorities =
	// authentication.getAuthorities();
	// final List<String> collect =
	// authorities.stream().map(GrantedAuthority::getAuthority)
	// .collect(Collectors.toList());
	// final Object principal =
	// authentication.getUserAuthentication().getPrincipal();
	// additionalInfo.put("authorities", collect);
	// additionalInfo.put("principal", principal instanceof String ? principal :
	// ((User) principal).getUsername());
	// additionalInfo.put("parameters",
	// authentication.getOAuth2Request().getRequestParameters());
	// additionalInfo.put("clientId",
	// authentication.getOAuth2Request().getClientId());
	// additionalInfo.put("grantType",
	// authentication.getOAuth2Request().getGrantType());
	//
	// ((DefaultOAuth2AccessToken)
	// accessToken).setAdditionalInformation(additionalInfo);
	// return accessToken;
	// }

	@Operation(summary = "Get all user access information with access token by token Id")
	@PostMapping(value = "/info")
	public ResponseEntity<OAuth2AccessToken> info(@RequestBody String tokenId) {
		try {

			log.info("Entering Info Token with id = " + tokenId);

			final OAuth2Authentication authentication = customTokenService.loadAuthentication(tokenId);
			final OAuth2AccessToken token = customTokenService.getAccessToken(authentication);

			return getResponse(token);
		} catch (final Exception e) {
			log.info(ERROR_RESPONSE + e.getLocalizedMessage());
			return getResponse(null);
		}

	}

	private ResponseEntity<OAuth2AccessToken> getResponse(OAuth2AccessToken accessToken) {
		final HttpHeaders headers = new HttpHeaders();
		headers.set("Cache-Control", "no-store");
		headers.set("Pragma", "no-cache");
		return new ResponseEntity<>(accessToken, headers, HttpStatus.OK);
	}

}
