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

import com.minsait.onesait.platform.router.service.app.model.NotificationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.model.SubscriptionModel;
import com.minsait.onesait.platform.router.service.app.model.TransactionModel;

public interface RouterService {

	public OperationResultModel insert(NotificationModel model);

	public OperationResultModel update(NotificationModel model);

	public OperationResultModel delete(NotificationModel model);

	public OperationResultModel query(NotificationModel model);

	public OperationResultModel subscribe(SubscriptionModel model);

	public OperationResultModel unsubscribe(SubscriptionModel model);

	public OperationResultModel startTransaction(TransactionModel model);

	public OperationResultModel commitTransaction(TransactionModel model);

	public OperationResultModel rollbackTransaction(TransactionModel model);

	public OperationResultModel notifyModules(NotificationModel model);

}
