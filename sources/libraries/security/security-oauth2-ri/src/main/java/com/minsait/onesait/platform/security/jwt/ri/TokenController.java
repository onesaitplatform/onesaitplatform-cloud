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
package com.minsait.onesait.platform.security.jwt.ri;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.minsait.onesait.platform.oauthserver.audit.aop.OauthServerAuditable;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@ConditionalOnBean(AuthorizationServerEndpointsConfiguration.class)
public class TokenController {

	@Value("${security.jwt.client-id}")
	private String clientId;

	@Autowired
	private AuthorizationServerEndpointsConfiguration configuration;

	@Autowired
	private ClientDetailsService clientDetailsService;

	@Autowired
	private TokenUtil sofia2TokenUtil;

	private static final String LEAVING_INFO_ERROR = "Leaving Info Token with with Error response = {} ";
	private static final String REVOKE_ERROR_RESPONSE = "Revoke with with Error response = {} ";

	@Resource(name = "tokenStore")
	TokenStore tokenStore;

	@Autowired
	CustomTokenService customTokenService;

	@RequestMapping(method = RequestMethod.GET, value = "/openplatform-oauth/token-values")
	@ResponseBody
	public List<String> getTokens() {

		final List<String> tokenValues = new ArrayList<>();
		final Collection<OAuth2AccessToken> tokens = tokenStore.findTokensByClientId(clientId);

		if (tokens != null) {
			for (final OAuth2AccessToken token : tokens) {
				tokenValues.add(token.getValue());
			}
		}
		return tokenValues;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/openplatform-oauth/tokens")
	@ResponseBody
	public Collection<OAuth2AccessToken> getTokenLists() {

		return tokenStore.findTokensByClientId(clientId);

	}

	@RequestMapping(method = RequestMethod.POST, value = "/openplatform-oauth/tokens/revokeRefreshToken/{tokenId:.*}")
	@ResponseBody
	public String revokeRefreshToken(@PathVariable String tokenId) {
		if (tokenStore instanceof JdbcTokenStore) {
			((JdbcTokenStore) tokenStore).removeRefreshToken(tokenId);
		}
		return tokenId;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/openplatform-oauth/renewToken")
	@ResponseBody

	/**
	 *
	 * @deperecated (since 2019-08, it generates non compliant token , refactor to
	 *              resfresh validn token)
	 */
	@Deprecated
	public ResponseToken renewToken(@RequestBody String id) {
		try {

			log.info("Entering Renew Token with id = {}", id);

			final OAuth2Authentication authentication = customTokenService.loadAuthentication(id);
			final OAuth2AccessToken token = customTokenService.getAccessToken(authentication);

			final OAuth2RefreshToken refreshToken = token.getRefreshToken();

			final String clientId = authentication.getOAuth2Request().getClientId();
			final Collection<String> scope = authentication.getOAuth2Request().getScope();
			final Map<String, String> parameters = authentication.getOAuth2Request().getRequestParameters();

			final TokenRequest tokenRequest = new TokenRequest(parameters, clientId, scope, "password");

			final OAuth2AccessToken tokenRefreshed = customTokenService.refreshAccessToken(refreshToken.getValue(),
					tokenRequest);

			final Date expiration = tokenRefreshed.getExpiration();

			final ResponseToken r = new ResponseToken();
			r.setOauthInfo(tokenRefreshed);
			r.setExpirationTimestamp(expiration);

			final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

			r.setExpirationFormatted(dateFormat.format(expiration));
			r.setToken(tokenRefreshed.getValue());

			log.info("Leaving Renew Token with with response = {}", r);

			return r;

		} catch (final Exception e) {
			final ResponseToken r = new ResponseToken();
			r.setToken("-1");
			log.info(LEAVING_INFO_ERROR, e.getLocalizedMessage());
			return r;
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/openplatform-oauth/tokenInfo")
	@ResponseBody
	public ResponseToken info(@RequestBody String tokenId) {
		try {

			log.info("Entering Info Token with id = {}", tokenId);

			final OAuth2Authentication authentication = customTokenService.loadAuthentication(tokenId);

			final OAuth2AccessToken token = customTokenService.getAccessToken(authentication);

			final ResponseToken r = new ResponseToken();
			r.setOauthInfo(token);
			r.setExpirationTimestamp(token.getExpiration());

			final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

			r.setExpirationFormatted(dateFormat.format(token.getExpiration()));
			r.setToken(token.getValue());

			log.info("Leaving Info Token with response = {}", r);

			return r;
		} catch (final Exception e) {
			final ResponseToken r = new ResponseToken();
			r.setToken("-1");
			log.info(LEAVING_INFO_ERROR, e.getLocalizedMessage());
			return r;
		}
	}

	@OauthServerAuditable
	@RequestMapping(method = RequestMethod.POST, value = "/openplatform-oauth/revoke_token")
	@ResponseBody
	public Map<String, ?> revokeAccesToken(@RequestHeader(value = "Authorization") String authorization,
			@RequestParam("token") String value) {
		final Map<String, Object> response = new HashMap<>();

		String appId = "";
		String appSecret = "";

		try {
			// createNewToken ();
			final String[] tokens = sofia2TokenUtil.extractAndDecodeHeader(authorization);

			assert tokens.length == 2;

			appId = tokens[0];
			appSecret = tokens[1];

			try {
				final ClientDetails clientId = clientDetailsService.loadClientByClientId(appId);

				log.info("Entering Access Info Token with Tokenid = {} ", value);

				if (!clientId.getClientSecret().equals(appSecret)) {
					response.put(OAuth2Exception.ERROR, OAuth2Exception.ACCESS_DENIED);
					response.put(OAuth2Exception.DESCRIPTION, value);

					log.info(REVOKE_ERROR_RESPONSE, OAuth2Exception.ACCESS_DENIED);

					return response;
				}
			} catch (final Exception e) {
				response.put(OAuth2Exception.ERROR, OAuth2Exception.ACCESS_DENIED);
				response.put(OAuth2Exception.DESCRIPTION, value);

				log.info(REVOKE_ERROR_RESPONSE, OAuth2Exception.ACCESS_DENIED);

				return response;
			}

			final OAuth2Authentication authentication = customTokenService.loadAuthentication(value);
			final OAuth2AccessToken token = customTokenService.getAccessToken(authentication);

			final String appTokenId = (String) token.getAdditionalInformation().get("clientId");

			if (appId.equals(appTokenId)) {
				tokenStore.removeAccessToken(token);
			} else {
				throw new OAuth2Exception(OAuth2Exception.ACCESS_DENIED);
			}

			response.put(OAuth2AccessToken.ACCESS_TOKEN, value);
			response.put(OAuth2Utils.STATE, OAuth2Exception.INVALID_TOKEN);

			log.info("Leaving Revoke Access Token with response = {} ", response);

			return response;
		} catch (final Exception e) {

			response.put(OAuth2Exception.ERROR, OAuth2Exception.INVALID_TOKEN);
			response.put(OAuth2Exception.DESCRIPTION, value);

			log.info(REVOKE_ERROR_RESPONSE, e.getLocalizedMessage());

			return response;
		}
	}

	@OauthServerAuditable
	@RequestMapping(method = RequestMethod.POST, value = "/openplatform-oauth/check_token")
	@ResponseBody
	public Map<String, Object> accessInfo(@RequestHeader(value = "Authorization") String authorization,
			@RequestParam("token") String value) {
		final Map<String, Object> response = new HashMap<>();

		String appId = "";
		String appSecret = "";

		try {
			final String[] tokens = sofia2TokenUtil.extractAndDecodeHeader(authorization);

			assert tokens.length == 2;

			appId = tokens[0];
			appSecret = tokens[1];

			if (!clientDetailsService.loadClientByClientId(appId).getClientSecret().equals(appSecret)) {
				response.put(OAuth2Exception.ERROR, OAuth2Exception.ACCESS_DENIED);
				response.put(OAuth2Exception.DESCRIPTION, value);

				log.info(REVOKE_ERROR_RESPONSE, OAuth2Exception.ACCESS_DENIED);

				return response;
			}

			log.info("Entering Access Info Token with Tokenid = {}", value);

			final OAuth2Authentication authentication = customTokenService.loadAuthentication(value);
			final OAuth2AccessToken token = customTokenService.getAccessToken(authentication);

			final String appTokenId = (String) token.getAdditionalInformation().get("clientId");

			// Build response
			response.putAll(sofia2TokenUtil.convertAccessToken(authentication, token, appTokenId, appId));

			log.info("Leaving Access Info Token with response = {}", response);
			return response;
		} catch (final Exception e) {

			response.put(OAuth2Exception.ERROR, OAuth2Exception.INVALID_TOKEN);
			response.put(OAuth2Exception.DESCRIPTION, e.getLocalizedMessage());

			log.info(LEAVING_INFO_ERROR, e.getLocalizedMessage());

			return response;
		}
	}

}