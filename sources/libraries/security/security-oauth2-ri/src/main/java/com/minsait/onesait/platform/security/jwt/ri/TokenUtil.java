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

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.AppRole;
import com.minsait.onesait.platform.config.model.AppUser;
import com.minsait.onesait.platform.config.repository.AppUserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TokenUtil {
	
	@Autowired
	private AppUserRepository userRepo;
	
	private static final String PRINCIPAL = "principal";
	private static final String USER_NAME = "user_name";
	private static final String NAME = "name";

	public String[] extractAndDecodeHeader(String header) throws IOException {

		byte[] base64Token = header.substring(6).getBytes("UTF-8");
		byte[] decoded;
		try {
			decoded = Base64.decode(base64Token);
		}
		catch (IllegalArgumentException e) {
			throw new BadCredentialsException("Failed to decode basic authentication token");
		}

		String token = new String(decoded, "UTF-8");

		int delim = token.indexOf(':');

		if (delim == -1) {
			throw new BadCredentialsException("Invalid basic authentication token");
		}
		return new String[] { token.substring(0, delim), token.substring(delim + 1) };
	}
	
	public Map<String, Object> convertAccessToken(OAuth2Authentication authentication, OAuth2AccessToken token, String appTokenId, String appId) {
		Map<String, Object> response = new HashMap<>();
		
		response.putAll(genericConvertAccessToken(authentication, token, appId));
		response.putAll(realmsConvertAccessToken(authentication, appTokenId, appId));
		
		return response;
	}
	
	private Map<String, ?> genericConvertAccessToken(OAuth2Authentication authentication, OAuth2AccessToken token, String appId) {
		Map<String, Object> response = new HashMap<>();
		OAuth2Request clientToken = authentication.getOAuth2Request();
		
		Set<String> scopes = new HashSet<>();
	    scopes.add(appId);
	    
	    response.put(DefaultAccessTokenConverter.SCOPE, scopes);

		if (token.getAdditionalInformation().containsKey(NAME)) {
			response.put(NAME, token.getAdditionalInformation().get(NAME));
		}
		
		if (token.getAdditionalInformation().containsKey(NAME)) {
			response.put(USER_NAME, token.getAdditionalInformation().get(NAME));
		}
		
		if (token.getAdditionalInformation().containsKey(PRINCIPAL)) {
			response.put(PRINCIPAL, token.getAdditionalInformation().get(PRINCIPAL));
		}
		
		if (token.getAdditionalInformation().containsKey(DefaultAccessTokenConverter.JTI)) {
			response.put(DefaultAccessTokenConverter.JTI, token.getAdditionalInformation().get(DefaultAccessTokenConverter.JTI));
		}

		if (token.getExpiration() != null) {
			response.put(DefaultAccessTokenConverter.EXP, token.getExpiration().getTime() / 1000);
		}
		
		if (authentication.getOAuth2Request().getGrantType()!=null) {
			response.put(DefaultAccessTokenConverter.GRANT_TYPE, authentication.getOAuth2Request().getGrantType());
		}

		response.put(DefaultAccessTokenConverter.CLIENT_ID, appId);
		if (clientToken.getResourceIds() != null && !clientToken.getResourceIds().isEmpty()) {
			response.put(DefaultAccessTokenConverter.AUD, clientToken.getResourceIds());
		}
		return response;
	}
	
	private Map<String, ?> realmsConvertAccessToken(OAuth2Authentication authentication, String appTokenId, String appId) {
		Map<String, Object> response = new HashMap<>();
		
		if (appTokenId.equals(appId)) {
			log.info("App auth IN token definition = {}, Returning all token info",appId);
			
			String userId = authentication.getUserAuthentication().getName();
			
			List<AppRole> roles = getAppRoles(userId, appTokenId);
			
        	Set<String> rolesList = new HashSet<>(); 
			
			for (AppRole role : roles) {
				rolesList.add(role.getName());
			}

			response.put(DefaultAccessTokenConverter.AUTHORITIES, rolesList);
		} else {
			String userId = authentication.getUserAuthentication().getName();

			//Assoc. Realm's Roles
			List<AppRole> roles = getAppRoles(userId, appTokenId);
			
        	Map<String, Set<String>> appsRoles = getChildRoles(roles);
        	
        	Set<String> rolesList;
        	rolesList = appsRoles.get(appId);
			
			if (rolesList==null) {
				rolesList = new HashSet<>(); 
			}
        	
			//App Roles
			List<AppRole> appRoles = getAppRoles(userId, appId);
			
			for (AppRole role : appRoles) {
				rolesList.add(role.getName());
			}
			
			response.put(DefaultAccessTokenConverter.AUTHORITIES, rolesList);
			
			log.info("App auth IN token info = {}, Returning app credentials",appId);
		}
		return response;
	}
	
	public Map<String, Set<String>> getChildRoles(List<AppRole> roles){
		
		Map<String, Set<String>> retorno = new HashMap<>();
		roles.forEach((AppRole role) ->	retorno.putAll(getChildRoles(role)));
		
		return retorno;
    }
	
	public Map<String, Set<String>> getChildRoles(AppRole role){
		Map<String, Set<String>> retorno = new HashMap<>();

		role.getChildRoles().forEach(childRole -> {
			String appId = childRole.getApp().getIdentification();
			if (!retorno.containsKey(appId))
				retorno.put(appId, new HashSet<>());
			retorno.get(appId).add(childRole.getName());
		});

		return retorno;
	}

	public List<AppRole> getAppRoles(String userId, String clientId){
    	List<AppUser> roles = userRepo.findByUserAndIdentification(userId, clientId);
        return roles.stream().map(AppUser::getRole).collect(Collectors.toList());
    }

}