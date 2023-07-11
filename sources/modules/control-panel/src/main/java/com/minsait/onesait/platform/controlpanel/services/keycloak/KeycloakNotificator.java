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
package com.minsait.onesait.platform.controlpanel.services.keycloak;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.controlpanel.services.keycloak.AdviceNotification.Type;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import lombok.extern.slf4j.Slf4j;

@Component
@ConditionalOnExpression("'${onesaitplatform.authentication.oauth.enabled}' == 'true' and '${onesaitplatform.authentication.oauth.osp-keycloak}' == 'true'")
@Slf4j
public class KeycloakNotificator {

	@Autowired
	private IntegrationResourcesService resourcesService;
	@Autowired
	private MultitenancyService multitenancyService;

	private final RestTemplate template = new RestTemplate(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());

	public void notifyRealmToKeycloak(String realm, AdviceNotification.Type type) {
		final AdviceNotification notification = AdviceNotification.builder().newVertical(false).realm(true).type(type)
				.onesaitRealm(realm).vertical(multitenancyService
						.getVerticalFromSchema(MultitenancyContextHolder.getVerticalSchema()).getName())
				.build();
		execute(resourcesService.getUrl(Module.KEYCLOAK_MANAGER, ServiceUrl.ADVICE), HttpMethod.POST, notification,
				Void.class);
	}

	public void notifyNewVerticalToKeycloak(String vertical) {
		final AdviceNotification notification = AdviceNotification.builder().newVertical(true).realm(false)
				.type(Type.CREATE).vertical(vertical).build();
		execute(resourcesService.getUrl(Module.KEYCLOAK_MANAGER, ServiceUrl.ADVICE), HttpMethod.POST, notification,
				Void.class);
	}

	private <T> ResponseEntity<T> execute(String url, HttpMethod method, Object body, Class<T> clazz) {
		try {
			final HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			return template.exchange(url, method, new HttpEntity<>(body, headers), clazz);
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("Error on request {}, code: {}, cause: {}", url, e.getRawStatusCode(),
					e.getResponseBodyAsString());
			return null;
		} catch (final ResourceAccessException e) {
			log.error("Could not notify to keycloak manager");
			return null;
		}
	}
}
