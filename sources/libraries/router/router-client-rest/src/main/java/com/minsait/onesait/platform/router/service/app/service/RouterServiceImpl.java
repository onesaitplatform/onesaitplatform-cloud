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
package com.minsait.onesait.platform.router.service.app.service;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.annotation.PostConstruct;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.interceptor.CorrelationInterceptor;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;
import com.minsait.onesait.platform.router.client.RouterClient;
import com.minsait.onesait.platform.router.service.app.model.NotificationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.model.SuscriptionModel;

import lombok.extern.slf4j.Slf4j;

@Service("routerServiceImpl")
@Slf4j
public class RouterServiceImpl implements RouterService, RouterClient<NotificationModel, OperationResultModel> {

	@Value("${onesaitplatform.router.alternativeURL:http://localhost:20000/router/router/")
	private String routerURLAlternative4;

	@Autowired
	private IntegrationResourcesService resourcesService;

	private String routerStandaloneURL;

	@PostConstruct
	public void postConstruct() {
		routerStandaloneURL = resourcesService.getUrl(Module.ROUTERSTANDALONE, ServiceUrl.ROUTER);

		if ("RESOURCE_URL_NOT_FOUND".equals(routerStandaloneURL)) {
			routerStandaloneURL = routerURLAlternative4;
		}

		log.info("Router Endpoint: " + routerStandaloneURL);

		try {
			SSLUtil.turnOffSslChecking();
		} catch (final KeyManagementException | NoSuchAlgorithmException e) {
			log.info(e.getMessage(), e);
		}

	}

	@Autowired
	@Qualifier("routerClientRest")
	private RestTemplate restTemplate;

	@Override
	public OperationResultModel execute(NotificationModel input) {
		try {

			final OperationModel model = input.getOperationModel();
			final String operation = model.getOperationType().name();

			OperationResultModel quote = new OperationResultModel();

			if (operation.equalsIgnoreCase("POST")
					|| operation.equalsIgnoreCase(OperationModel.OperationType.INSERT.name())) {
				quote = restTemplate.exchange(routerStandaloneURL + "/insert", HttpMethod.POST,
						new HttpEntity<>(input, addCorrelationHeader()), OperationResultModel.class).getBody();
			} else if (operation.equalsIgnoreCase("PUT")
					|| operation.equalsIgnoreCase(OperationModel.OperationType.UPDATE.name())) {
				final org.springframework.http.HttpEntity<NotificationModel> request = new HttpEntity<>(input,
						addCorrelationHeader());
				quote = restTemplate
						.exchange(routerStandaloneURL + "/update", HttpMethod.PUT, request, OperationResultModel.class)
						.getBody();
			}

			else if (operation.equalsIgnoreCase("DELETE")
					|| operation.equalsIgnoreCase(OperationModel.OperationType.DELETE.name())) {
				final org.springframework.http.HttpEntity<NotificationModel> request = new HttpEntity<>(input,
						addCorrelationHeader());
				quote = restTemplate.exchange(routerStandaloneURL + "/delete", HttpMethod.DELETE, request,
						OperationResultModel.class).getBody();
			}

			else if (operation.equalsIgnoreCase("GET")
					|| operation.equalsIgnoreCase(OperationModel.OperationType.QUERY.name())) {
				quote = restTemplate.exchange(routerStandaloneURL + "/query", HttpMethod.POST,
						new HttpEntity<>(input, addCorrelationHeader()), OperationResultModel.class).getBody();
			}
			log.debug("Router Rest Client result: " + quote.toString());
			return quote;

		} catch (final Exception e) {
			log.error("execute:", e);
			throw e;
		}
	}

	@Override
	public OperationResultModel insert(NotificationModel model) {
		return execute(model);
	}

	@Override
	public OperationResultModel update(NotificationModel model) {
		return execute(model);
	}

	@Override
	public OperationResultModel delete(NotificationModel model) {
		return execute(model);
	}

	@Override
	public OperationResultModel query(NotificationModel model) {
		return execute(model);
	}

	public void setRouterStandaloneURL(String routerStandaloneURL) {
		this.routerStandaloneURL = routerStandaloneURL;
	}

	@Override
	public OperationResultModel suscribe(SuscriptionModel model) {
		return null;
	}

	private HttpHeaders addCorrelationHeader() {
		final HttpHeaders headers = new HttpHeaders();
		String correlationID = MDC.get(CorrelationInterceptor.CORRELATION_ID_LOG_VAR_NAME);
		if (StringUtils.isEmpty(correlationID))
			correlationID = CorrelationInterceptor.generateUniqueCorrelationId();
		headers.add(CorrelationInterceptor.CORRELATION_ID_HEADER_NAME, correlationID);
		return headers;
	}

}
