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
package com.minsait.onesait.platform.libraries.nodered.auth;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.services.flowdomain.FlowDomainService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.config.services.usertoken.UserTokenService;
import com.minsait.onesait.platform.libraries.nodered.auth.exception.NoderedAuthException;
import com.minsait.onesait.platform.libraries.nodered.auth.pojo.NoderedAuthenticationResult;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NoderedAuthenticationServiceImpl implements NoderedAuthenticationService {


	private String proxyUrl;
	@Value("${onesaitplatform.flowengine.services.request.timeout.ms:5000}")
	private int restRequestTimeout;
	@Value("${onesaitplatform.router.avoidsslverification:false}")
	private boolean avoidSSLVerification;
	private HttpComponentsClientHttpRequestFactory httpRequestFactory;
	@Autowired
	private FlowDomainService domainService;

	@Autowired
	private UserTokenService userTokenService;
	@Autowired
	private UserService userService;
	
	@Autowired
	private IntegrationResourcesService resourcesService;

	private static final String LOGIN_URL = "/auth/token";
	private static final String CHECK_URL = "/settings";

	@PostConstruct
	public void init() {
		proxyUrl = resourcesService.getUrl(Module.FLOWENGINE, ServiceUrl.ADVICE);
		if (avoidSSLVerification) {
			httpRequestFactory = SSLUtil.getHttpRequestFactoryAvoidingSSLVerification();
		} else {
			httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		}
		
		httpRequestFactory.setConnectTimeout(restRequestTimeout);
	}

	@Override
	public String getNoderedAuthAccessToken(String user, String domain) {
		FlowDomain flowDomain = domainService.getFlowDomainByIdentification(domain);
		String accessToken = flowDomain.getAccessToken();
		if (!checkAccessToken(domain, flowDomain.getAccessToken())) {
			// Invalid Token, regenerate by login into nodeRED
			// get Api token for user
			String apiToken = userTokenService.getToken(userService.getUser(user)).getToken();
			accessToken = loginToNodeRED(user, domain, apiToken);
			flowDomain.setAccessToken(accessToken);
			domainService.updateDomain(flowDomain);
		}
		return accessToken;
	}

	private String loginToNodeRED(String user, String domain, String apiToken) {
		RestTemplate restTemplate = new RestTemplate(httpRequestFactory);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("client_id", "node-red-editor");
		map.add("grant_type", "pass" + "word");
		map.add("scope", "");
		map.add("username", user);
		map.add("pass" + "word", apiToken);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

		String url = proxyUrl + domain + LOGIN_URL;
		try{
			ResponseEntity<NoderedAuthenticationResult> result = restTemplate.postForEntity(url, request,
					NoderedAuthenticationResult.class);
			if (result.getStatusCode() == HttpStatus.OK) {
				return result.getBody().getAccessToken();
			} else {
				throwNewLoginError(user, domain, result.getStatusCodeValue());
			}
		} catch (HttpClientErrorException e){
			throwNewLoginError(user, domain, e.getRawStatusCode());
		}
		return null;		
	}
	
	private void throwNewLoginError(String user, String domain, int retCode){
		log.error("Error while login to NodeRED. Unable to login user {} into domain {}. Return Code: {}", user,
				domain, retCode);
		throw new NoderedAuthException("Error while login to NodeRED. Unable to login user " + user
				+ " into domain " + domain + ". Return Code: " + retCode);
	}

	private boolean checkAccessToken(String domain, String token) {
		if (token == null) {
			return false;
		}
		RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
		String url = proxyUrl + domain + CHECK_URL;
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Content-Type", "application/json");
		headers.add("Authorization", "Bearer " + token);
		try{
			ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers),
					String.class);
			return responseEntity.getStatusCode() == HttpStatus.OK;
		}catch(HttpClientErrorException e){
			log.debug("Token for domain " + domain +" is not valid.");
			return false;
		}
	}

}
