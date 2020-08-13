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

import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataService;
import com.minsait.onesait.platform.router.service.app.model.NotificationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.model.SubscriptionModel;
import com.minsait.onesait.platform.router.service.app.model.TransactionModel;
import com.minsait.onesait.platform.router.service.processor.RouterFlowManagerService;
import com.minsait.onesait.platform.router.subscription.SubscriptionManager;
import com.minsait.onesait.platform.router.transaction.OntologyLockChecker;
import com.minsait.onesait.platform.router.transaction.OpenPlatformTransactionManager;

import lombok.extern.slf4j.Slf4j;

@Service("routerServiceImpl")
@Slf4j
public class RouterServiceImpl implements RouterService, RouterSubscriptionService {

	public static final String ONTOLOGIES_NOT_AVAILABLE_MESSAGE = "Ontologies are locked, please try again.";
	public static final String ONTOLOGIES_NOT_AVAILABLE_CODE = "ONTOLOGIES_NOT_AVAILABLE";

	@Autowired
	RouterFlowManagerService routerFlowManagerService;

	@Autowired
	OntologyDataService ontologyDataService;

	@Autowired
	OntologyLockChecker ontologyLockChecker;

	@Autowired
	private OpenPlatformTransactionManager transactionManager;

	@Autowired
	private SubscriptionManager subscriptionManager;

	@Override
	public OperationResultModel insert(NotificationModel model) {
		OperationResultModel result = new OperationResultModel();
		if (model.getOperationModel().getTransactionId() == null) {
			log.info("Insert not transactional, check if the ontology {} is locked.",
					model.getOperationModel().getOntologyName());
			final Boolean isLocked = ontologyLockChecker.isOntologyLocked(model.getOperationModel().getOntologyName());
			if (!isLocked) {
				result = routerFlowManagerService.startBrokerFlow(model);
			} else {
				// Ontology is locked for a transaction --> The operation is rejected.
				result.setStatus(false);
				result.setErrorCode(ONTOLOGIES_NOT_AVAILABLE_CODE);
				result.setMessage(ONTOLOGIES_NOT_AVAILABLE_MESSAGE);
				return result;
			}
		} else {
			result = transactionManager.insert(model);
		}
		return result;

	}

	@Override
	public OperationResultModel update(NotificationModel model) {
		OperationResultModel result = new OperationResultModel();

		if (model.getOperationModel().getTransactionId() == null) {
			log.info("Update not transactional, check if the ontology {} is locked.",
					model.getOperationModel().getOntologyName());
			final Boolean isLocked = ontologyLockChecker.isOntologyLocked(model.getOperationModel().getOntologyName());
			if (!isLocked) {
				result = routerFlowManagerService.startBrokerFlow(model);
			} else {
				// Ontology cannot be locked --> The operation is rejected.
				result.setStatus(false);
				result.setErrorCode(ONTOLOGIES_NOT_AVAILABLE_CODE);
				result.setMessage(ONTOLOGIES_NOT_AVAILABLE_MESSAGE);
				return result;
			}

		} else {
			result = transactionManager.update(model);
		}

		return result;

	}

	@Override
	public OperationResultModel delete(NotificationModel model) {
		OperationResultModel result = new OperationResultModel();
		if (model.getOperationModel().getTransactionId() == null) {
			log.info("Delete not transactional, check if the ontology {} is locked.",
					model.getOperationModel().getOntologyName());
			final Boolean isLocked = ontologyLockChecker.isOntologyLocked(model.getOperationModel().getOntologyName());
			if (!isLocked) {
				result = routerFlowManagerService.startBrokerFlow(model);
			} else {
				// Ontology cannot be locked --> The operation is rejected.
				result.setStatus(false);
				result.setErrorCode(ONTOLOGIES_NOT_AVAILABLE_CODE);
				result.setMessage(ONTOLOGIES_NOT_AVAILABLE_MESSAGE);
				return result;
			}
		} else {
			result = transactionManager.delete(model);
		}
		return result;

	}

	@Override
	public OperationResultModel query(NotificationModel model) {
		return routerFlowManagerService.startBrokerFlow(model);
	}

	@Override
	public OperationResultModel subscribe(SubscriptionModel model) {
		log.info("A subscription message arrive.");
		return subscriptionManager.subscription(model);
	}

	@Transactional
	@Override
	public OperationResultModel unsubscribe(SubscriptionModel model) {

		log.info("A unsubscription message arrive.");
		return subscriptionManager.unsubscription(model);
	}

	@Override
	public OperationResultModel startTransaction(TransactionModel model) {
		return transactionManager.startTransaction(model);

	}

	@Override
	public OperationResultModel commitTransaction(TransactionModel model) {
		return transactionManager.commitTransaction(model);
	}

	@Override
	public OperationResultModel rollbackTransaction(TransactionModel model) {
		return transactionManager.rollbackTransaction(model);
	}

	@Override
	public OperationResultModel notifyModules(NotificationModel model) {
		return routerFlowManagerService.notifyModules(model);
	}

}
