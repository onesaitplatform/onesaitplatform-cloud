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
package com.minsait.onesait.platform.oauthserver.audit.aop;

import java.security.Principal;
import java.util.Map;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.minsait.onesait.platform.audit.aop.BaseAspect;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.OperationType;
import com.minsait.onesait.platform.audit.bean.OPAuditRemoteEvent;
import com.minsait.onesait.platform.security.jwt.ri.CustomTokenService;
import com.minsait.onesait.platform.security.jwt.ri.TokenUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Aspect
@Slf4j
public class OauthServerAuditableAspect extends BaseAspect {

	@Autowired
	private TokenUtil tokenUtil;

	@Autowired
	CustomTokenService customTokenService;

	@Autowired
	private OautServerAuditProcessor auditProcessor;

	private static final String GENERATE_MESSAGE_ID = "OAUTHSERVER_GENERATE_TOKEN";
	private static final String CHECK_MESSAGE_ID = "OAUTHSERVER_CHECK_TOKEN";
	private static final String REFRESH_MESSAGE_ID = "OAUTHSERVER_REFRESH_TOKEN";
	private static final String REVOKE_MESSAGE_ID = "OAUTHSERVER_REVOKE_TOKEN";
	private static final String OIDC_MESSAGE_ID = "OAUTHSERVER_OIDC_TOKEN";

	private static final String OAUTHSERVER_APP = "OauthServer - Application: ";
	private static final String OAUTHSERVER_ERROR = "OauthServer - ERROR: ";

	private static final String USER_ANONYMOUS = "anonymous";

	private static final String SYS_ADMIN = "sysadmin";

	@AfterReturning(returning = "token", pointcut = "@annotation(auditable) && args(authentication,..) && execution (* createAccessToken(..))")
	public void auditGenerateToken(JoinPoint joinPoint, OauthServerAuditable auditable,
			OAuth2Authentication authentication, OAuth2AccessToken token) throws Throwable {
		try {
			final String user = authentication.getName();
			final String message = OAUTHSERVER_APP + token.getAdditionalInformation().get("clientId")
					+ " generating token for user: " + user + " TOKEN: " + token;

			eventProducer.publish(auditProcessor.genetateAuditEvent(user, GENERATE_MESSAGE_ID, message,
					OperationType.OAUTH_TOKEN_GENERATION, ""));
		} catch (final Exception e) {
			eventProducer
			.publish(auditProcessor.genetateErrorEvent(SYS_ADMIN, OAUTHSERVER_ERROR + " " + GENERATE_MESSAGE_ID,
					e.getMessage(), OperationType.OAUTH_TOKEN_GENERATION, e.getMessage()));
		}
	}

	@AfterReturning(returning = "response", pointcut = "@annotation(auditable) && args(authorization,token,..) && execution (* accessInfo(..))")
	public void auditCheckToken(JoinPoint joinPoint, OauthServerAuditable auditable, String authorization, String token,
			Map<String, Object> response) throws Throwable {
		try {
			final String[] tokens = tokenUtil.extractAndDecodeHeader(authorization);

			final String appId = tokens[0];

			final OAuth2Authentication authentication = customTokenService.loadAuthentication(token);
			final String user = authentication.getName();
			final String message = OAUTHSERVER_APP + appId + " checking token of user: " + user + " TOKEN: " + token;

			eventProducer.publish(auditProcessor.genetateAuditEvent(user, CHECK_MESSAGE_ID, message,
					OperationType.OAUTH_TOKEN_CHECK, response.toString()));
		} catch (final Exception e) {
			eventProducer
			.publish(auditProcessor.genetateErrorEvent(SYS_ADMIN, OAUTHSERVER_ERROR + " " + CHECK_MESSAGE_ID,
					e.getMessage(), OperationType.OAUTH_TOKEN_CHECK, e.getMessage()));
		}
	}

	@AfterReturning(returning = "token", pointcut = "@annotation(auditable) && args(refreshTokenValue,tokenRequest,..) && execution (* refreshAccessToken(..))")
	public void auditRefreshToken(JoinPoint joinPoint, OauthServerAuditable auditable, String refreshTokenValue,
			TokenRequest tokenRequest, OAuth2AccessToken token) throws Throwable {
		try {
			final String user = token.getAdditionalInformation().get("principal").toString();
			final String message = OAUTHSERVER_APP + tokenRequest.getClientId() + " refreshes token for user: " + user
					+ " REFRESH TOKEN: " + refreshTokenValue;

			eventProducer.publish(auditProcessor.genetateAuditEvent(user, REFRESH_MESSAGE_ID, message,
					OperationType.OAUTH_TOKEN_REFRESH, "NEW ACCESS TOKEN: " + token.getValue()));
		} catch (final Exception e) {
			eventProducer
			.publish(auditProcessor.genetateErrorEvent(SYS_ADMIN, OAUTHSERVER_ERROR + " " + REFRESH_MESSAGE_ID,
					e.getMessage(), OperationType.OAUTH_TOKEN_REFRESH, e.getMessage()));
		}
	}

	@Around("@annotation(auditable) && args(authorization,token,..) && execution(* revokeAccesToken(..))")
	public Map<String, Object> auditRevokeToken(ProceedingJoinPoint joinPoint, OauthServerAuditable auditable,
			String authorization, String token) throws Throwable {
		Map<String, Object> response = null;
		OPAuditRemoteEvent oautServerEvent = null;
		try {
			final String[] tokens = tokenUtil.extractAndDecodeHeader(authorization);
			final String appId = tokens[0];

			final OAuth2Authentication authentication = customTokenService.loadAuthentication(token);
			final String user = authentication.getName();

			final String message = OAUTHSERVER_APP + appId + " revoking token: " + token + " of user: " + user;
			oautServerEvent = auditProcessor.genetateAuditEvent(user, REVOKE_MESSAGE_ID, message,
					OperationType.OAUTH_TOKEN_REVOCATION, "");
			log.debug(message);
		} catch (final Exception e) {
			eventProducer
			.publish(auditProcessor.genetateErrorEvent(SYS_ADMIN, OAUTHSERVER_ERROR + " " + REVOKE_MESSAGE_ID,
					e.getMessage(), OperationType.OAUTH_TOKEN_REVOCATION, e.getMessage()));
		}

		response = (Map<String, Object>) joinPoint.proceed();

		eventProducer.publish(oautServerEvent);
		return response;
	}

	@Around("args(principal,parameters,..) && execution(* org.springframework.security.oauth2.provider.endpoint.TokenEndpoint.postAccessToken(..))")
	public ResponseEntity<OAuth2AccessToken> auditTokenEndpoint(ProceedingJoinPoint joinPoint, Principal principal,
			Map<String, String> parameters) throws Throwable {
		final long start = System.currentTimeMillis();
		final Authentication client = (Authentication) principal;
		String clientId = client.getName();
		if (client instanceof OAuth2Authentication) {
			// Might be a client and user combined authentication
			clientId = ((OAuth2Authentication) client).getOAuth2Request().getClientId();
		}
		log.info("New postAccessToken request with clientId: {} and username: {}", clientId,
				parameters.get("username"));

		@SuppressWarnings("unchecked")
		final ResponseEntity<OAuth2AccessToken> response = (ResponseEntity<OAuth2AccessToken>) joinPoint.proceed();
		log.info("End postAccessToken  time: {}, response status: {}", System.currentTimeMillis() - start,
				response.getStatusCode());
		log.trace("Token generated is {}", response.getBody().getValue());
		eventProducer
		.publish(auditProcessor.genetateAuditEvent(parameters.get("username"), GENERATE_MESSAGE_ID,
				OAUTHSERVER_APP + clientId + " generating token for user: " + parameters.get("username")
				+ " TOKEN: " + response.getBody().getValue(),
				OperationType.OAUTH_TOKEN_GENERATION, ""));
		return response;
	}

	@SuppressWarnings("unchecked")
	@Around("args(token) && execution(* org.springframework.security.oauth2.provider.endpoint.CheckTokenEndpoint.checkToken(..))")
	public Map<String, ?> auditCheckTokenEndpoint(ProceedingJoinPoint joinPoint, String token) throws Throwable {
		final long start = System.currentTimeMillis();
		log.trace("New checkToken request with token: {} and username: {}", token);
		// TO-DO delete me ODEO testing
		if ("v_token1_bearer_ERROR".equals(token)) {
			throw new InvalidTokenException("v_token1_bearer_ERROR is invalid");
		}
		Map<String, ?> response = null;
		try {
			response = (Map<String, ?>) joinPoint.proceed();
			log.info("End checkToken  time: {}, token of user: {}", System.currentTimeMillis() - start,
					response.get("username"));
		} catch (final InvalidTokenException e) {

			eventProducer.publish(
					auditProcessor.genetateErrorEvent(SYS_ADMIN, OAUTHSERVER_ERROR + " " + CHECK_MESSAGE_ID,
							e.getMessage(), OperationType.OAUTH_TOKEN_CHECK, e.getMessage()));
			throw e;

		}
		return response;
	}

	@AfterThrowing(throwing = "exception", pointcut = "execution(* org.springframework.security.oauth2.provider.endpoint.TokenEndpoint.postAccessToken(..))")
	public void auditTokenEndpointException(Throwable exception) {
		log.error("Exception throwed in TokenEndpoint.postAccessToken: {}, class: {}", exception.getMessage(),
				exception.getClass().getName(), exception);
		eventProducer.publish(
				auditProcessor.genetateErrorEvent(SYS_ADMIN, OAUTHSERVER_ERROR + " " + GENERATE_MESSAGE_ID,
						exception.getMessage(), OperationType.OAUTH_TOKEN_GENERATION, exception.getMessage()));
	}

	@AfterThrowing(throwing = "exception", pointcut = "args(token) && execution(* org.springframework.security.oauth2.provider.endpoint.CheckTokenEndpoint.checkToken(..))")
	public void auditCheckTokenEndpointException(Throwable exception, String token) {
		log.error("Exception throwed in CheckTokenEndpoint.checkToken: {}, class: {}", exception.getMessage(),
				exception.getClass().getName());
		log.debug("Token was {}", token);
		eventProducer
		.publish(auditProcessor.genetateErrorEvent(SYS_ADMIN, OAUTHSERVER_ERROR + " " + CHECK_MESSAGE_ID,
				exception.getMessage(), OperationType.OAUTH_TOKEN_CHECK, exception.getMessage()));
	}

	@AfterReturning(returning = "node", pointcut = "@annotation(auditable) && args(token,..) && execution (* userInfo(..))")
	public void auditOIDCToken(JoinPoint joinPoint, OauthServerAuditable auditable, OAuth2Authentication token,
			JsonNode node) throws Throwable {
		try {
			final String message = "TOKEN details for user: " + token.getName() + " using TOKEN: "
					+ ((OAuth2AuthenticationDetails) token.getDetails()).getTokenValue().toString();

			eventProducer.publish(auditProcessor.genetateAuditEvent(token.getName(), OIDC_MESSAGE_ID, message,
					OperationType.OAUTH_TOKEN_OIDC, "USER DETAILS: " + node.toString()));
		} catch (final Exception e) {
			eventProducer.publish(
					auditProcessor.genetateErrorEvent(SYS_ADMIN, OAUTHSERVER_ERROR + " " + OIDC_MESSAGE_ID,
							e.getMessage(), OperationType.OAUTH_TOKEN_OIDC, e.getMessage()));
		}
	}

}
