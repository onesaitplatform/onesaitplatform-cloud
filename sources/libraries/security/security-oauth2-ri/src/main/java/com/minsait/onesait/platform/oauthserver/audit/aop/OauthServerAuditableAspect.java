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
package com.minsait.onesait.platform.oauthserver.audit.aop;

import java.util.Map;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
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

@Component
@Aspect
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
		
	@AfterReturning(returning = "token", pointcut = "@annotation(auditable) && args(authentication,..) && execution (* createAccessToken(..))")
	public void auditGenerateToken(JoinPoint joinPoint, OauthServerAuditable auditable, OAuth2Authentication authentication, OAuth2AccessToken token) throws Throwable{
		try {
			final String user = authentication.getName();
			final String message = OAUTHSERVER_APP + token.getAdditionalInformation().get("clientId") + " generating token for user: " + user + " TOKEN: " + token;
					
			eventProducer.publish(auditProcessor.genetateAuditEvent(user, GENERATE_MESSAGE_ID, message, OperationType.OAUTH_TOKEN_GENERATION, ""));
		} catch (Exception e) {
			eventProducer.publish(auditProcessor.genetateErrorEvent(USER_ANONYMOUS, OAUTHSERVER_ERROR + " " + GENERATE_MESSAGE_ID, e.getMessage(), OperationType.OAUTH_TOKEN_GENERATION, e.getMessage()));
		}
	}	
	
	@AfterReturning(returning = "response", pointcut = "@annotation(auditable) && args(authorization,token,..) && execution (* accessInfo(..))")
	public void auditCheckToken(JoinPoint joinPoint, OauthServerAuditable auditable, String authorization, String token, Map<String, Object> response) throws Throwable{
		try {
			final String[] tokens = tokenUtil.extractAndDecodeHeader(authorization);
	
			String appId = tokens[0];
			
			final OAuth2Authentication authentication = customTokenService.loadAuthentication(token);
			final String user = authentication.getName();
			final String message = OAUTHSERVER_APP + appId + " checking token of user: " + user + " TOKEN: " + token;
		
			eventProducer.publish(auditProcessor.genetateAuditEvent(user, CHECK_MESSAGE_ID, message, OperationType.OAUTH_TOKEN_CHECK, response.toString()));
		} catch (Exception e) {
			eventProducer.publish(auditProcessor.genetateErrorEvent(USER_ANONYMOUS, OAUTHSERVER_ERROR + " " + CHECK_MESSAGE_ID, e.getMessage(), OperationType.OAUTH_TOKEN_CHECK, e.getMessage()));
		}
	}
	
	@AfterReturning(returning = "token", pointcut = "@annotation(auditable) && args(refreshTokenValue,tokenRequest,..) && execution (* refreshAccessToken(..))")
	public void auditRefreshToken(JoinPoint joinPoint, OauthServerAuditable auditable, String refreshTokenValue, TokenRequest tokenRequest, OAuth2AccessToken token) throws Throwable{
		try {
			String user = token.getAdditionalInformation().get("principal").toString();
			String message = OAUTHSERVER_APP + tokenRequest.getClientId() + " refreshes token for user: " + user + " REFRESH TOKEN: " + refreshTokenValue;
			
			eventProducer.publish(auditProcessor.genetateAuditEvent(user, REFRESH_MESSAGE_ID, message, OperationType.OAUTH_TOKEN_REFRESH, "NEW ACCESS TOKEN: " + token.getValue()));
		} catch (Exception e) {
			eventProducer.publish(auditProcessor.genetateErrorEvent(USER_ANONYMOUS, OAUTHSERVER_ERROR + " " + REFRESH_MESSAGE_ID, e.getMessage(), OperationType.OAUTH_TOKEN_REFRESH, e.getMessage()));
		}
	}	
	
	@Around("@annotation(auditable) && args(authorization,token,..) && execution(* revokeAccesToken(..))")
	public  Map<String, Object> auditRevokeToken(ProceedingJoinPoint joinPoint, OauthServerAuditable auditable,	String authorization, String token) throws Throwable {
		Map<String, Object> response = null;
		OPAuditRemoteEvent oautServerEvent = null;
		try {
			final String[] tokens = tokenUtil.extractAndDecodeHeader(authorization);
			String appId = tokens[0];

			final OAuth2Authentication authentication = customTokenService.loadAuthentication(token);
			final String user = authentication.getName();
		
			String message = OAUTHSERVER_APP + appId + " revoking token: " + token + " of user: " + user;
			oautServerEvent = auditProcessor.genetateAuditEvent(user, REVOKE_MESSAGE_ID, message, OperationType.OAUTH_TOKEN_REVOCATION, "");
		} catch (Exception e) {
			eventProducer.publish(auditProcessor.genetateErrorEvent(USER_ANONYMOUS, OAUTHSERVER_ERROR + " " + REVOKE_MESSAGE_ID, e.getMessage(), OperationType.OAUTH_TOKEN_REVOCATION, e.getMessage()));
		}
		
		response = (Map<String, Object>) joinPoint.proceed();
		
		eventProducer.publish(oautServerEvent);

		return response;
	}
	
	@AfterReturning(returning = "node", pointcut = "@annotation(auditable) && args(token,..) && execution (* userInfo(..))")
	public void auditOIDCToken(JoinPoint joinPoint, OauthServerAuditable auditable, OAuth2Authentication token, JsonNode node) throws Throwable{
		try {
			String message = "TOKEN details for user: " + token.getName() + " using TOKEN: " + ((OAuth2AuthenticationDetails)token.getDetails()).getTokenValue().toString();
			
			eventProducer.publish(auditProcessor.genetateAuditEvent(token.getName(), OIDC_MESSAGE_ID, message, OperationType.OAUTH_TOKEN_OIDC, "USER DETAILS: " + node.toString()));
		} catch (Exception e) {
			eventProducer.publish(auditProcessor.genetateErrorEvent(USER_ANONYMOUS, OAUTHSERVER_ERROR + " " + OIDC_MESSAGE_ID, e.getMessage(), OperationType.OAUTH_TOKEN_OIDC, e.getMessage()));
		}
	}	
	
}
