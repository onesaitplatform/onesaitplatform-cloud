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

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.SuscriptionNotificationsModel;
import com.minsait.onesait.platform.config.model.SuscriptionNotificationsModel.OperationType;
import com.minsait.onesait.platform.config.model.SuscriptionNotificationsModel.QueryType;
import com.minsait.onesait.platform.config.repository.SuscriptionModelRepository;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataService;
import com.minsait.onesait.platform.router.service.app.model.NotificationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.model.SuscriptionModel;
import com.minsait.onesait.platform.router.service.processor.RouterFlowManagerService;

@Service("routerServiceImpl")
public class RouterServiceImpl implements RouterService, RouterSuscriptionService {

	@Autowired
	RouterFlowManagerService routerFlowManagerService;

	@Autowired
	SuscriptionModelRepository repository;

	@Autowired
	OntologyDataService ontologyDataService;

	@Override
	public OperationResultModel insert(NotificationModel model) {
		return routerFlowManagerService.startBrokerFlow(model);
	}

	@Override
	public OperationResultModel update(NotificationModel model) {
		return routerFlowManagerService.startBrokerFlow(model);
	}

	@Override
	public OperationResultModel delete(NotificationModel model) {
		return routerFlowManagerService.startBrokerFlow(model);
	}

	@Override
	public OperationResultModel query(NotificationModel model) {
		return routerFlowManagerService.startBrokerFlow(model);
	}

	@Override
	public OperationResultModel suscribe(SuscriptionModel model) {

		SuscriptionNotificationsModel m = new SuscriptionNotificationsModel();
		m.setOntologyName(model.getOntologyName());
		m.setOperationType(OperationType.valueOf(model.getOperationType().name()));
		m.setQuery(model.getQuery());
		m.setQueryType(QueryType.valueOf(model.getQueryType().name()));
		m.setSessionKey(model.getSessionKey());
		m.setSuscriptionId(model.getSuscriptionId());
		m.setUser(model.getUser());

		SuscriptionNotificationsModel saved = repository.save(m);

		OperationResultModel result = new OperationResultModel();
		result.setErrorCode("");
		result.setOperation("SUSCRIBE");
		result.setResult(saved.getId());
		result.setMessage("Suscription to " + saved.getOntologyName() + " has "
				+ repository.findAllByOntologyName(model.getOntologyName()).size());
		return result;
	}

	@Transactional
	@Override
	public OperationResultModel unSuscribe(SuscriptionModel model) {

		repository.deleteBySuscriptionId(model.getSuscriptionId());

		OperationResultModel result = new OperationResultModel();
		result.setErrorCode("");
		result.setOperation("UNSUSCRIBE");
		result.setResult("OK");
		result.setMessage("Suscription " + model.getSuscriptionId() + " removed");
		return result;
	}

}
