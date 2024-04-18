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
package com.minsait.onesait.platform.router.service.app.service.advice;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;
import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.model.NotificationEntity;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.services.flowdomain.FlowDomainService;
import com.minsait.onesait.platform.config.services.flownode.FlowNodeService;
import com.minsait.onesait.platform.libraries.nodered.auth.NoderedAuthenticationService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;
import com.minsait.onesait.platform.router.service.app.model.AdviceNotificationModel;
import com.minsait.onesait.platform.router.service.app.service.AdviceNotificationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NodeRedAdviceNotificationService implements AdviceNotificationService {

	@Autowired
	FlowNodeService flowNodeService;

	@Autowired
	FlowDomainService flowDomainService;

	@Autowired
	OntologyRepository ontologyRepository;

	@Autowired
	IntegrationResourcesService resourcesService;

	@Autowired
	private NoderedAuthenticationService noderedAthService;

	private static final String INSERT_STR = "INSERT";

	static final ImmutableMap<String, String> HTTP_METHOD_TO_NODE_METHOD = new ImmutableMap.Builder<String, String>()
			.put("POST", INSERT_STR).put("PUT", "UPDATE").put("DELETE", "DELETE").put(INSERT_STR, INSERT_STR).build();

	@Override
	public List<AdviceNotificationModel> getAdviceNotificationModel(String ontologyName, String messageType) {

		List<NotificationEntity> listNotifications = null;
		List<AdviceNotificationModel> model = null;
		final String baseUrl = resourcesService.getUrl(Module.FLOWENGINE, ServiceUrl.ADVICE);

		try {
			listNotifications = flowNodeService.getNotificationsByOntologyAndMessageType(ontologyName,
					HTTP_METHOD_TO_NODE_METHOD.get(messageType) != null ? HTTP_METHOD_TO_NODE_METHOD.get(messageType)
							: messageType);
		} catch (final IllegalArgumentException e) {
			log.debug("Deserializing enum error {}", e);

		} catch (final Exception e) {
			log.error("" + e);
		}

		if (listNotifications != null) {
			model = new ArrayList<>();
			for (final NotificationEntity notificationEntity : listNotifications) {
				final AdviceNotificationModel advice = new AdviceNotificationModel();
				advice.setEntityId(notificationEntity.getNotificationEntityId());
				advice.setUrlAuthkey("X-OP-NODEKey");
				advice.setDomainIdentification(notificationEntity.getNotificationDomain());
				FlowDomain domain = flowDomainService
						.getFlowDomainByIdentification(notificationEntity.getNotificationDomain());
				/*advice.setUrlAuthValue(noderedAthService.getNoderedAuthAccessToken(domain.getUser().getUserId(),
						notificationEntity.getNotificationDomain()));
						*/
				advice.setDomainOwner(domain.getUser().getUserId());
				advice.setUrl(baseUrl + "/" + notificationEntity.getNotificationUrl());
				advice.setRetryOnFaialureEnabled(notificationEntity.isRetryOnFaialureEnabled());
				advice.setDiscardAfterElapsedTimeEnabled(notificationEntity.isDiscardAfterElapsedTimeEnabled());
				advice.setMaxRetryElapsedTime(notificationEntity.getMaxRetryElapsedTime());
				model.add(advice);
			}
		}
		return model;
	}
	
	
}
