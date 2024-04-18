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
package com.minsait.onesait.platform.router.service.app.service;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.annotation.PostConstruct;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.MediaType;

import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.interceptor.CorrelationInterceptor;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;
import com.minsait.onesait.platform.router.client.RouterClient;
import com.minsait.onesait.platform.router.service.app.model.NotificationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.model.SubscriptionModel;
import com.minsait.onesait.platform.router.service.app.model.TransactionModel;

import lombok.extern.slf4j.Slf4j;

@Service("routerServiceImpl")
@Slf4j
public class RouterServiceImpl implements RouterService, RouterClient<NotificationModel, OperationResultModel> {

	@Value("${onesaitplatform.router.alternativeURL:http://localhost:20000/router/router/")
	private String routerURLAlternative4;

	@Autowired
	private IntegrationResourcesService resourcesService;

	@Value("${onesaitplatform.multitenancy.enabled:false}")
	private boolean multitenancyEnabled;

	private String routerStandaloneURL;

	@Autowired
	@Qualifier("routerClientRest")
	private RestTemplate restTemplate;

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
		restTemplate.setMessageConverters(Arrays.asList(new MappingJackson2HttpMessageConverter()));
		restTemplate.getInterceptors().add(new ClientHttpRequestInterceptor() {

			@Override
			public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
					throws IOException {
				request.getHeaders().remove("Content-Type");
				request.getHeaders().add("Content-Type", "application/json");
				return execution.execute(request, body);
			}
		});
	}

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
			if (log.isDebugEnabled()) {
				log.debug("Router Rest Client result: {}", quote.toString());
			}
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

	@Override
	public OperationResultModel startTransaction(TransactionModel model) {
		return restTemplate.exchange(routerStandaloneURL + "/startTransaction", HttpMethod.POST,
				new HttpEntity<>(model, addCorrelationHeader()), OperationResultModel.class).getBody();
	}

	@Override
	public OperationResultModel commitTransaction(TransactionModel model) {
		return restTemplate.exchange(routerStandaloneURL + "/commitTransaction", HttpMethod.POST,
				new HttpEntity<>(model, addCorrelationHeader()), OperationResultModel.class).getBody();

	}

	@Override
	public OperationResultModel rollbackTransaction(TransactionModel model) {
		return restTemplate.exchange(routerStandaloneURL + "/rollbackTransaction", HttpMethod.POST,
				new HttpEntity<>(model, addCorrelationHeader()), OperationResultModel.class).getBody();

	}

	public void setRouterStandaloneURL(String routerStandaloneURL) {
		this.routerStandaloneURL = routerStandaloneURL;
	}

	@Override
	public OperationResultModel subscribe(SubscriptionModel model) {
		return restTemplate.exchange(routerStandaloneURL + "/subscribe", HttpMethod.POST,
				new HttpEntity<>(model, addCorrelationHeader()), OperationResultModel.class).getBody();
	}

	@Override
	public OperationResultModel unsubscribe(SubscriptionModel model) {
		return restTemplate.exchange(routerStandaloneURL + "/unsubscribe", HttpMethod.POST,
				new HttpEntity<>(model, addCorrelationHeader()), OperationResultModel.class).getBody();
	}

	private HttpHeaders addCorrelationHeader() {
		final HttpHeaders headers = new HttpHeaders();
		String correlationID = MDC.get(CorrelationInterceptor.CORRELATION_ID_LOG_VAR_NAME);
		if (!StringUtils.hasText(correlationID))
			correlationID = CorrelationInterceptor.generateUniqueCorrelationId();
		headers.add(CorrelationInterceptor.CORRELATION_ID_HEADER_NAME, correlationID);
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		addMultitenancyHeaders(headers);

		return headers;
	}

	private void addMultitenancyHeaders(HttpHeaders headers) {
		try {
			headers.add(Tenant2SchemaMapper.VERTICAL_HTTP_HEADER, MultitenancyContextHolder.getVerticalSchema());
			headers.add(Tenant2SchemaMapper.TENANT_HTTP_HEADER, MultitenancyContextHolder.getTenantName());
		} catch (final Exception e) {
			log.error("No authentication found, could not add tenant/vertical headers to HTTP request");
		}
	}

	@Override
	public OperationResultModel notifyModules(NotificationModel model) {
		return restTemplate.exchange(routerStandaloneURL + "notifyModules", HttpMethod.POST,
				new HttpEntity<>(model, addCorrelationHeader()), OperationResultModel.class).getBody();

	}

}
