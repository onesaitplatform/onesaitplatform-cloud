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

import com.minsait.onesait.platform.config.model.SuscriptionNotificationsModel;
import com.minsait.onesait.platform.config.repository.SuscriptionModelRepository;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;
import com.minsait.onesait.platform.router.service.app.model.AdviceNotificationModel;
import com.minsait.onesait.platform.router.service.app.model.SuscriptionModel;
import com.minsait.onesait.platform.router.service.app.service.AdviceNotificationService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class IotBrokerAdviceNotificationService implements AdviceNotificationService {

	@Autowired
	SuscriptionModelRepository repository;

	@Autowired
	IntegrationResourcesService resourcesService;

	@Override
	public List<AdviceNotificationModel> getAdviceNotificationModel(String ontologyName, String messageType) {

		List<SuscriptionNotificationsModel> listNotifications = null;
		List<AdviceNotificationModel> model = null;
		String baseUrl = resourcesService.getUrl(Module.IOTBROKER, ServiceUrl.ADVICE);

		try {
			listNotifications = repository.findAllByOntologyName(ontologyName);
		} catch (Exception e) {
			log.error("getAdviceNotificationModel", e);
		}

		if (listNotifications != null) {
			model = new ArrayList<>();
			for (SuscriptionNotificationsModel notificationEntity : listNotifications) {
				AdviceNotificationModel advice = new AdviceNotificationModel();
				advice.setEntityId(notificationEntity.getSuscriptionId());
				advice.setUrl(baseUrl);

				SuscriptionModel sus = new SuscriptionModel();
				sus.setOntologyName(notificationEntity.getOntologyName());
				sus.setOperationType(
						com.minsait.onesait.platform.router.service.app.model.SuscriptionModel.OperationType
								.valueOf(notificationEntity.getOperationType().name()));
				sus.setQuery(notificationEntity.getQuery());
				sus.setQueryType(SuscriptionModel.QueryType.valueOf(notificationEntity.getQueryType().name()));
				sus.setSessionKey(notificationEntity.getSessionKey());
				sus.setSuscriptionId(notificationEntity.getSuscriptionId());
				sus.setUser(notificationEntity.getUser());

				advice.setSuscriptionModel(sus);
				model.add(advice);
			}
		}
		return model;
	}
}
