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
package com.minsait.onesait.platform.iotbroker.mock.router;

import java.util.ArrayList;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.iotbroker.mock.pojo.Person;
import com.minsait.onesait.platform.multitenant.config.model.IoTSession;
import com.minsait.onesait.platform.router.service.app.model.NotificationCompositeModel;
import com.minsait.onesait.platform.router.service.app.model.NotificationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.QueryType;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.Source;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;

public class RouterServiceGenerator {

	public static NotificationCompositeModel generateNotificationCompositeModel(String subscriptionId, Person subject,
			IoTSession session) throws JsonProcessingException {
		final ObjectMapper mapper = new ObjectMapper();
		final NotificationCompositeModel model = new NotificationCompositeModel();
		model.setNotificationModel(new NotificationModel());
		model.getNotificationModel()
				.setOperationModel(OperationModel
						.builder(Person.class.getSimpleName(), OperationModel.OperationType.QUERY, session.getUserID(),
								Source.IOTBROKER)
						.body(mapper.writeValueAsString(subject)).deviceTemplate(mapper.writeValueAsString(subject))
						.queryType(QueryType.NATIVE).objectId(UUID.randomUUID().toString()).build());

		model.setNotificationEntityId(subscriptionId);
		model.setOperationResultModel(new OperationResultModel());
		model.getOperationResultModel().setErrorCode("");
		model.getOperationResultModel().setMessage("OK");
		model.getOperationResultModel().setOperation("QUERY");
		final ArrayList<Person> persons = new ArrayList<>();
		persons.add(subject);
		model.getOperationResultModel().setResult(mapper.writeValueAsString(persons));
		model.getOperationResultModel().setStatus(false);
		model.setNotificationEntityId(subscriptionId);

		return model;

	}

	public static OperationResultModel generateUpdateDeleteResultOk(int numInstancesModified) {
		final OperationResultModel value = new OperationResultModel();
		value.setErrorCode("");
		value.setMessage("");
		value.setOperation("");
		value.setResult("" + numInstancesModified);
		value.setStatus(true);

		return value;
	}

	public static OperationResultModel generateUpdateByIdResultOk(String result) {
		final OperationResultModel value = new OperationResultModel();
		value.setErrorCode("");
		value.setMessage("");
		value.setOperation("");
		value.setResult(result);
		value.setStatus(true);

		return value;
	}

	public static OperationResultModel generateInserOk(String instanceId) {
		final OperationResultModel value = new OperationResultModel();
		value.setErrorCode("");
		value.setMessage("");
		value.setOperation("");
		value.setResult("" + instanceId);
		value.setStatus(true);

		return value;
	}

	public static OperationResultModel generateQueryOk(String result) {
		final OperationResultModel value = new OperationResultModel();
		value.setErrorCode("");
		value.setMessage("");
		value.setOperation("");
		value.setResult(result);
		value.setStatus(true);

		return value;
	}

	public static OperationResultModel generateSubscriptionOk(String subscriptionId) {
		final OperationResultModel value = new OperationResultModel();
		value.setErrorCode("");
		value.setMessage("");
		value.setOperation("");
		value.setResult(subscriptionId);
		value.setStatus(true);

		return value;

	}

}
