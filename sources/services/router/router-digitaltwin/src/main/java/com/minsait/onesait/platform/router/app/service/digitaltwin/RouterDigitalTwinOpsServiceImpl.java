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
package com.minsait.onesait.platform.router.app.service.digitaltwin;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.PropertyDigitalTwinType;
import com.minsait.onesait.platform.config.repository.DigitalTwinTypeRepository;
import com.minsait.onesait.platform.config.repository.PropertyDigitalTwinTypeRepository;
import com.minsait.onesait.platform.persistence.mongodb.MongoBasicOpsDBRepository;
import com.minsait.onesait.platform.router.service.app.model.DigitalTwinCompositeModel;
import com.minsait.onesait.platform.router.service.app.model.DigitalTwinModel;
import com.minsait.onesait.platform.router.service.app.model.NotificationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.service.RouterDigitalTwinService;
import com.minsait.onesait.platform.router.service.app.service.RouterService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RouterDigitalTwinOpsServiceImpl implements RouterDigitalTwinService {

	static final String EVENTS_COLLECTION = "TwinEvents";
	static final String LOG_COLLECTION = "TwinLogs";
	static final String PROPERTIES_COLLECTION = "TwinProperties";
	static final String ACTIONS_COLLECTION = "TwinActions";

	private static final String ROUTER_DT_SERVICE_OPERATION = "Router Digital Twin Service Operation ";
	private static final String SDATE = "$date";
	private static final String DEVICE_ID = "deviceId";
	private static final String TIMESTAMP_STR = "timestamp";

	DateTimeFormatter timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	@Autowired
	private MongoBasicOpsDBRepository mongoRepo;

	@Autowired
	private PropertyDigitalTwinTypeRepository propertyDigitalTwinTypeRepo;

	@Autowired
	private DigitalTwinTypeRepository digitalTwinTypeRepository;

	@Autowired
	@Qualifier("routerServiceImpl")
	private RouterService routerService;

	@Override
	public OperationResultModel insertEvent(DigitalTwinCompositeModel compositeModel) {
		log.info(ROUTER_DT_SERVICE_OPERATION + compositeModel.getDigitalTwinModel().toString());
		OperationResultModel result = new OperationResultModel();
		DigitalTwinModel model = compositeModel.getDigitalTwinModel();

		final String event = model.getEvent().name();

		JSONObject instance = new JSONObject();

		JSONObject timestamp = new JSONObject();
		timestamp.put(SDATE, LocalDateTime.now().format(timestampFormatter));

		instance.put(DEVICE_ID, model.getDeviceId());
		instance.put("type", model.getType());
		instance.put(TIMESTAMP_STR, timestamp);
		instance.put("event", event);

		if (model.getEventName() != null) {
			instance.put("eventName", model.getEventName());
		}

		Optional<JSONObject> optionalContent = buildEventContent(model);
		if (optionalContent.isPresent()) {
			instance.put("content", optionalContent.get());
		}

		String output = "";

		result.setMessage("OK");
		result.setStatus(true);

		try {

			output = mongoRepo.insert(EVENTS_COLLECTION, null, instance.toString());

		} catch (final Exception e) {
			result.setResult(output);
			result.setStatus(false);
			result.setMessage(e.getMessage());
		}

		result.setResult(output);
		result.setOperation(event);
		return result;
	}

	@Override
	public OperationResultModel insertLog(DigitalTwinCompositeModel compositeModel) {
		log.info(ROUTER_DT_SERVICE_OPERATION + compositeModel.getDigitalTwinModel().toString());
		OperationResultModel result = new OperationResultModel();
		DigitalTwinModel model = compositeModel.getDigitalTwinModel();

		String event = model.getEvent().name();

		JSONObject instance = new JSONObject();

		JSONObject timestamp = new JSONObject();
		timestamp.put(SDATE, LocalDateTime.now().format(timestampFormatter));

		instance.put("trace", model.getLog());
		instance.put(DEVICE_ID, model.getDeviceId());
		instance.put("type", model.getType());
		instance.put(TIMESTAMP_STR, timestamp);

		String output = "";

		result.setMessage("OK");
		result.setStatus(true);

		try {

			output = mongoRepo.insert(LOG_COLLECTION, null, instance.toString());

		} catch (final Exception e) {
			result.setResult(output);
			result.setStatus(false);
			result.setMessage(e.getMessage());
		}

		result.setResult(output);
		result.setOperation(event);
		return result;
	}

	@Override
	public OperationResultModel updateShadow(DigitalTwinCompositeModel compositeModel) {
		String output = "";
		log.info(ROUTER_DT_SERVICE_OPERATION + compositeModel.getDigitalTwinModel().toString());
		OperationResultModel result = new OperationResultModel();

		DigitalTwinModel model = compositeModel.getDigitalTwinModel();

		String event = model.getEvent().name();

		JSONObject timestamp = new JSONObject();
		timestamp.put(SDATE, LocalDateTime.now().format(timestampFormatter));

		JSONObject instance = new JSONObject();
		instance.put(DEVICE_ID, model.getDeviceId());
		instance.put("type", model.getType());
		instance.put(TIMESTAMP_STR, timestamp);

		List<PropertyDigitalTwinType> properties = propertyDigitalTwinTypeRepo
				.findByTypeId(digitalTwinTypeRepository.findByIdentification(model.getType()));

		JSONObject status = new JSONObject(model.getStatus());
		Iterator<String> keys = status.keys();
		String property = keys.next();

		Map<String, PropertyDigitalTwinType> mOntologyProperties = new HashMap<>();
		boolean ontologyProperties = false;
		for (PropertyDigitalTwinType prop : properties) {
			if (prop.getName().equals(property) && prop.getType().equalsIgnoreCase("ontology")) {
				ontologyProperties = true;
				mOntologyProperties.put(prop.getName(), prop);
			}
		}

		if (ontologyProperties) {

			Iterator<String> itKeys = status.keys();
			while (itKeys.hasNext()) {
				String key = itKeys.next();
				PropertyDigitalTwinType ontologyProperty = mOntologyProperties.get(key);
				Object data = status.get(key);

				if (null != ontologyProperty && null != data && JSONObject.NULL != data) {
					try {
						String ontology = ontologyProperty.getUnit();

						OperationModel insertOperationModel = OperationModel
								.builder(ontology, OperationType.INSERT, null, OperationModel.Source.DIGITALTWINBROKER)
								.body(data.toString()).build();

						NotificationModel modelNotification = new NotificationModel();
						modelNotification.setOperationModel(insertOperationModel);
						routerService.insert(modelNotification);

					} catch (Exception e) {
						log.error("Error storing Digital Twin Ontology type", e);
					}
				}

			}

		}

		instance.put("status", new JSONObject(model.getStatus()));

		result.setMessage("OK");
		result.setStatus(true);

		try {

			output = mongoRepo.insert(PROPERTIES_COLLECTION + model.getType().substring(0, 1).toUpperCase()
					+ model.getType().substring(1), null, instance.toString());

		} catch (final Exception e) {
			result.setResult(output);
			result.setStatus(false);
			result.setMessage(e.getMessage());
		}

		result.setResult(output);
		result.setOperation(event);
		return result;
	}

	@Override
	public OperationResultModel insertAction(DigitalTwinCompositeModel compositeModel) {
		log.info(ROUTER_DT_SERVICE_OPERATION + compositeModel.getDigitalTwinModel().toString());
		OperationResultModel result = new OperationResultModel();

		DigitalTwinModel model = compositeModel.getDigitalTwinModel();

		String action = model.getActionName();

		JSONObject timestamp = new JSONObject();
		timestamp.put(SDATE, LocalDateTime.now().format(timestampFormatter));

		JSONObject instance = new JSONObject();
		instance.put(DEVICE_ID, model.getDeviceId());
		instance.put("type", model.getType());
		instance.put(TIMESTAMP_STR, timestamp);
		instance.put("action", action);

		String output = "";
		result.setMessage("OK");
		result.setStatus(true);

		try {

			output = mongoRepo.insert(
					ACTIONS_COLLECTION + model.getType().substring(0, 1).toUpperCase() + model.getType().substring(1),
					null, instance.toString());

		} catch (final Exception e) {
			result.setResult(output);
			result.setStatus(false);
			result.setMessage(e.getMessage());
		}

		result.setResult(output);
		result.setOperation(action);
		return result;
	}

	private Optional<JSONObject> buildEventContent(DigitalTwinModel model) {
		JSONObject content = new JSONObject();

		switch (model.getEvent()) {
		case REGISTER:
			content.put("endpoint", model.getEndpoint());
			return Optional.of(content);

		default:
			return Optional.empty();
		}

	}
}
