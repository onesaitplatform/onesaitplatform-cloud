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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;
import com.minsait.onesait.platform.router.client.RouterClient;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.model.SuscriptionModel;

import lombok.extern.slf4j.Slf4j;

@Service("routerSuscriptionServiceImpl")
@Slf4j
public class RouterSuscriptionServiceImpl
		implements RouterSuscriptionService, RouterClient<SuscriptionModel, OperationResultModel> {

	private String routerStandaloneURL;
	@Value("${onesaitplatform.router.alternativeURL : http://localhost:20000/router/router/")
	private String routerURLAlternative4;
	@Autowired
	private IntegrationResourcesService resourcesService;

	@PostConstruct
	public void postConstruct() {
		routerStandaloneURL = resourcesService.getUrl(Module.ROUTERSTANDALONE, ServiceUrl.ROUTER);

		if ("RESOURCE_URL_NOT_FOUND".equals(routerStandaloneURL)) {
			routerStandaloneURL = routerURLAlternative4;
		}

		try {
			SSLUtil.turnOffSslChecking();
		} catch (final KeyManagementException | NoSuchAlgorithmException e) {
			log.info(e.getMessage(), e);
		}

	}

	@Override
	public OperationResultModel execute(SuscriptionModel model) {
		final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
		restTemplate.getInterceptors().add(new BasicAuthorizationInterceptor("admin", "admin"));

		final String operation = model.getOperationType().name();

		OperationResultModel quote = new OperationResultModel();

		if (operation.equalsIgnoreCase("SUSCRIBE")
				|| operation.equalsIgnoreCase(SuscriptionModel.OperationType.SUSCRIBE.name())) {
			quote = restTemplate.postForObject(routerStandaloneURL + "/suscribe", model, OperationResultModel.class);
		} else if (operation.equalsIgnoreCase("UNSUSCRIBE")
				|| operation.equalsIgnoreCase(SuscriptionModel.OperationType.UNSUSCRIBE.name())) {
			quote = restTemplate.postForObject(routerStandaloneURL + "/unsuscribe", model, OperationResultModel.class);
		}

		log.info(quote.toString());
		return quote;
	}

	@Override
	public OperationResultModel suscribe(SuscriptionModel model) {
		return execute(model);
	}

	@Override
	public OperationResultModel unSuscribe(SuscriptionModel model) {
		return execute(model);
	}

}
