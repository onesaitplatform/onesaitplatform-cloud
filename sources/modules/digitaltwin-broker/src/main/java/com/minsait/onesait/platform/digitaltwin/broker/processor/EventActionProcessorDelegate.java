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
package com.minsait.onesait.platform.digitaltwin.broker.processor;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.config.model.DigitalTwinDevice;
import com.minsait.onesait.platform.config.repository.DigitalTwinDeviceRepository;
import com.minsait.onesait.platform.digitaltwin.broker.plugable.impl.gateway.reference.ActionNotifier;
import com.minsait.onesait.platform.digitaltwin.broker.plugable.impl.gateway.reference.websocket.DigitalTwinWebsocketAPI;
import com.minsait.onesait.platform.digitaltwin.broker.processor.model.EventResponseMessage;
import com.minsait.onesait.platform.router.service.app.model.DigitalTwinCompositeModel;
import com.minsait.onesait.platform.router.service.app.model.DigitalTwinModel;
import com.minsait.onesait.platform.router.service.app.model.DigitalTwinModel.EventType;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.service.RouterDigitalTwinService;

import lombok.extern.slf4j.Slf4j;

@Component
@EnableAutoConfiguration
@Slf4j
public class EventActionProcessorDelegate implements EventProcessor, ActionProcessor {

	private static final String STATUS_STR = "status";
	private static final String ID_REQUIRED = "id is required";
	private static final String TOKEN_NOT_VALID = "Token not valid";
	private static final String DT_NOT_FOUND = "Digital Twin not found";

	@Autowired
	private DigitalTwinDeviceRepository deviceRepo;

	@Autowired
	// @Qualifier("routerDigitalTwinServiceImpl")
	private RouterDigitalTwinService routerDigitalTwinService;

	@Autowired
	private List<ActionNotifier> actionNotifiers;

	@Autowired
	private DigitalTwinWebsocketAPI digitalTwinWebsocketApi;

	private ExecutorService notifierExecutor;

	@PostConstruct
	public void init() {
		notifierExecutor = Executors.newFixedThreadPool(10);
	}

	@PreDestroy
	public void destroy() {
		this.notifierExecutor.shutdown();
	}

	@Override
	public EventResponseMessage register(String apiKey, JSONObject data) {

		// Validation apikey
		if (data.get("id") == null || data.get("endpoint") == null) {
			return new EventResponseMessage("id and endpoint are required", HttpStatus.BAD_REQUEST);
		}
		DigitalTwinDevice device = deviceRepo.findByIdentification(data.get("id").toString());

		if (null == device) {
			return new EventResponseMessage(DT_NOT_FOUND, HttpStatus.NOT_FOUND);
		}

		if (apiKey.equals(device.getDigitalKey())) {
			// Set endpoint
			String deviceUrl = data.get("endpoint").toString();

			String urlSchema = deviceUrl.split("://")[0];
			String ip = deviceUrl.split("://")[1].split("/")[0].split(":")[0];
			String port = deviceUrl.split("://")[1].split("/")[0].split(":")[1];
			String contextPath;
			if (deviceUrl.split("://")[1].split("/").length > 2) {
				contextPath = deviceUrl.split("://")[1].split("/")[2];
			} else {
				contextPath = deviceUrl.split("://")[1].split("/")[1];
			}

			device.setUrlSchema(urlSchema);
			device.setIp(ip);
			device.setPort(Integer.parseInt(port));
			if (!contextPath.startsWith("/")) {
				device.setContextPath("/" + contextPath);
			} else {
				device.setContextPath(contextPath);
			}

			deviceRepo.save(device);

			// insert the register event
			DigitalTwinModel model = new DigitalTwinModel();
			DigitalTwinCompositeModel compositeModel = new DigitalTwinCompositeModel();

			model.setEvent(EventType.REGISTER);
			model.setDeviceId(device.getId());
			model.setDeviceName(device.getIdentification());
			model.setType(device.getTypeId().getName());
			model.setEndpoint(deviceUrl);

			compositeModel.setDigitalTwinModel(model);
			compositeModel.setTimestamp(new Timestamp(System.currentTimeMillis()));

			OperationResultModel result = routerDigitalTwinService.insertLog(compositeModel);
			if (!result.isStatus()) {
				return new EventResponseMessage(result.getMessage(), HttpStatus.valueOf(result.getErrorCode()));
			}

		} else {
			return new EventResponseMessage(TOKEN_NOT_VALID, HttpStatus.UNAUTHORIZED);
		}
		return new EventResponseMessage("Device Registered", HttpStatus.OK);
	}

	@Override
	public EventResponseMessage ping(String apiKey, JSONObject data) {

		// Validation apikey
		if (data.get("id") == null) {
			return new EventResponseMessage(ID_REQUIRED, HttpStatus.BAD_REQUEST);
		}

		DigitalTwinDevice device = deviceRepo.findByIdentification(data.get("id").toString());

		if (null == device) {
			return new EventResponseMessage(DT_NOT_FOUND, HttpStatus.NOT_FOUND);
		}

		if (apiKey.equals(device.getDigitalKey())) {
			// Set last updated
			device.setUpdatedAt(new Date());
			deviceRepo.save(device);

			// insert the ping event
			DigitalTwinModel model = new DigitalTwinModel();
			DigitalTwinCompositeModel compositeModel = new DigitalTwinCompositeModel();

			model.setEvent(EventType.PING);
			model.setDeviceId(device.getId());
			model.setDeviceName(device.getIdentification());
			model.setType(device.getTypeId().getName());

			compositeModel.setDigitalTwinModel(model);
			compositeModel.setTimestamp(new Timestamp(System.currentTimeMillis()));

			OperationResultModel result = routerDigitalTwinService.insertLog(compositeModel);
			if (!result.isStatus()) {
				return new EventResponseMessage(result.getMessage(), HttpStatus.valueOf(result.getErrorCode()));
			}

		} else {
			return new EventResponseMessage(TOKEN_NOT_VALID, HttpStatus.UNAUTHORIZED);
		}
		return new EventResponseMessage("Ping Successful", HttpStatus.OK);
	}

	@Override
	public EventResponseMessage log(String apiKey, JSONObject data) {

		if (data.get("id") == null || data.get("log") == null) {
			return new EventResponseMessage("id and log are required", HttpStatus.BAD_REQUEST);
		}

		// Validation apikey
		DigitalTwinDevice device = deviceRepo.findByIdentification(data.get("id").toString());

		if (null == device) {
			return new EventResponseMessage(DT_NOT_FOUND, HttpStatus.NOT_FOUND);
		}

		if (apiKey.equals(device.getDigitalKey())) {
			// Set last updated
			device.setUpdatedAt(new Date());
			deviceRepo.save(device);
			// insert trace of log
			DigitalTwinModel model = new DigitalTwinModel();
			DigitalTwinCompositeModel compositeModel = new DigitalTwinCompositeModel();

			model.setEvent(EventType.LOG);
			model.setLog(data.get("log").toString());
			model.setDeviceId(device.getId());
			model.setDeviceName(device.getIdentification());
			model.setType(device.getTypeId().getName());

			compositeModel.setDigitalTwinModel(model);
			compositeModel.setTimestamp(new Timestamp(System.currentTimeMillis()));

			OperationResultModel result = routerDigitalTwinService.insertLog(compositeModel);
			if (!result.isStatus()) {
				return new EventResponseMessage(result.getMessage(), HttpStatus.valueOf(result.getErrorCode()));
			}
			return new EventResponseMessage(result.getResult(), HttpStatus.OK);
		} else {
			return new EventResponseMessage(TOKEN_NOT_VALID, HttpStatus.UNAUTHORIZED);
		}
	}

	@Override
	public EventResponseMessage shadow(String apiKey, JSONObject data) {

		log.info("Shadow received");
		if (data.get("id") == null) {
			return new EventResponseMessage(ID_REQUIRED, HttpStatus.BAD_REQUEST);
		}

		// Validation apikey
		DigitalTwinDevice device = deviceRepo.findByIdentification(data.get("id").toString());

		if (null == device) {
			return new EventResponseMessage(DT_NOT_FOUND, HttpStatus.NOT_FOUND);
		}

		if (apiKey.equals(device.getDigitalKey())) {

			// Set last updated
			device.setUpdatedAt(new Date());
			deviceRepo.save(device);

			// insert shadow
			DigitalTwinModel model = new DigitalTwinModel();
			DigitalTwinCompositeModel compositeModel = new DigitalTwinCompositeModel();

			model.setEvent(com.minsait.onesait.platform.router.service.app.model.DigitalTwinModel.EventType.SHADOW);
			model.setStatus(data.get(STATUS_STR).toString());
			model.setDeviceId(device.getId());
			model.setDeviceName(device.getIdentification());
			model.setType(device.getTypeId().getName());

			compositeModel.setDigitalTwinModel(model);
			compositeModel.setTimestamp(new Timestamp(System.currentTimeMillis()));

			log.info("Send updateshadow to router");
			OperationResultModel result = routerDigitalTwinService.updateShadow(compositeModel);
			if (!result.isStatus()) {
				log.info("EventActionProcessorDelegate -- getErrorCode: " + result.getErrorCode());
				return new EventResponseMessage(result.getMessage(), HttpStatus.valueOf(result.getErrorCode()));
			}
			log.info("The Shadow is going to be notified to demo");
			this.notifyShadowSubscriptors(data);

			return new EventResponseMessage(result.getMessage(), HttpStatus.OK);
		} else {
			return new EventResponseMessage(TOKEN_NOT_VALID, HttpStatus.UNAUTHORIZED);
		}
	}

	@Override
	public EventResponseMessage notebook(String apiKey, JSONObject data) {

		if (data.get("id") == null) {
			return new EventResponseMessage(ID_REQUIRED, HttpStatus.BAD_REQUEST);
		}

		// Validation apikey
		DigitalTwinDevice device = deviceRepo.findByIdentification(data.get("id").toString());

		if (null == device) {
			return new EventResponseMessage(DT_NOT_FOUND, HttpStatus.NOT_FOUND);
		}

		if (apiKey.equals(device.getDigitalKey())) {

			// Set last updated
			device.setUpdatedAt(new Date());
			deviceRepo.save(device);

			// insert event
			DigitalTwinModel model = new DigitalTwinModel();
			DigitalTwinCompositeModel compositeModel = new DigitalTwinCompositeModel();

			model.setEvent(EventType.NOTEBOOK);
			model.setStatus(data.get(STATUS_STR).toString());
			model.setDeviceId(device.getId());
			model.setDeviceName(device.getIdentification());
			model.setType(device.getTypeId().getName());

			compositeModel.setDigitalTwinModel(model);
			compositeModel.setTimestamp(new Timestamp(System.currentTimeMillis()));

			OperationResultModel result = routerDigitalTwinService.insertEvent(compositeModel);
			if (!result.isStatus()) {
				return new EventResponseMessage(result.getMessage(), HttpStatus.valueOf(result.getErrorCode()));
			}
			return new EventResponseMessage(result.getMessage(), HttpStatus.OK);
		} else {
			return new EventResponseMessage(TOKEN_NOT_VALID, HttpStatus.UNAUTHORIZED);
		}
	}

	@Override
	public EventResponseMessage flow(String apiKey, JSONObject data) {

		if (data.get("id") == null) {
			return new EventResponseMessage(ID_REQUIRED, HttpStatus.BAD_REQUEST);
		}

		// Validation apikey
		DigitalTwinDevice device = deviceRepo.findByIdentification(data.get("id").toString());

		if (null == device) {
			return new EventResponseMessage(DT_NOT_FOUND, HttpStatus.NOT_FOUND);
		}

		if (apiKey.equals(device.getDigitalKey())) {

			// Set last updated
			device.setUpdatedAt(new Date());
			deviceRepo.save(device);

			// insert event
			DigitalTwinModel model = new DigitalTwinModel();
			DigitalTwinCompositeModel compositeModel = new DigitalTwinCompositeModel();

			model.setEvent(EventType.FLOW);
			model.setStatus(data.get(STATUS_STR).toString());
			model.setDeviceId(device.getId());
			model.setDeviceName(device.getIdentification());
			model.setType(device.getTypeId().getName());

			compositeModel.setDigitalTwinModel(model);
			compositeModel.setTimestamp(new Timestamp(System.currentTimeMillis()));

			OperationResultModel result = routerDigitalTwinService.insertEvent(compositeModel);
			if (!result.isStatus()) {
				return new EventResponseMessage(result.getMessage(), HttpStatus.valueOf(result.getErrorCode()));
			}
			return new EventResponseMessage(result.getMessage(), HttpStatus.OK);
		} else {
			return new EventResponseMessage(TOKEN_NOT_VALID, HttpStatus.UNAUTHORIZED);
		}
	}

	@Override
	public EventResponseMessage rule(String apiKey, JSONObject data) {

		if (data.get("id") == null) {
			return new EventResponseMessage(ID_REQUIRED, HttpStatus.BAD_REQUEST);
		}

		// Validation apikey
		DigitalTwinDevice device = deviceRepo.findByIdentification(data.get("id").toString());

		if (null == device) {
			return new EventResponseMessage(DT_NOT_FOUND, HttpStatus.NOT_FOUND);
		}

		if (apiKey.equals(device.getDigitalKey())) {

			// Set last updated
			device.setUpdatedAt(new Date());
			deviceRepo.save(device);

			// insert event
			DigitalTwinModel model = new DigitalTwinModel();
			DigitalTwinCompositeModel compositeModel = new DigitalTwinCompositeModel();

			model.setEvent(EventType.RULE);
			model.setStatus(data.get(STATUS_STR).toString());
			model.setDeviceId(device.getId());
			model.setDeviceName(device.getIdentification());
			model.setType(device.getTypeId().getName());

			compositeModel.setDigitalTwinModel(model);
			compositeModel.setTimestamp(new Timestamp(System.currentTimeMillis()));

			OperationResultModel result = routerDigitalTwinService.insertEvent(compositeModel);
			if (!result.isStatus()) {
				return new EventResponseMessage(result.getMessage(), HttpStatus.valueOf(result.getErrorCode()));
			}
			return new EventResponseMessage(result.getMessage(), HttpStatus.OK);
		} else {
			return new EventResponseMessage(TOKEN_NOT_VALID, HttpStatus.UNAUTHORIZED);
		}
	}

	@Override
	public EventResponseMessage custom(String apiKey, JSONObject data) {

		if (data.get("id") == null) {
			return new EventResponseMessage(ID_REQUIRED, HttpStatus.BAD_REQUEST);
		}

		// Validation apikey
		DigitalTwinDevice device = deviceRepo.findByIdentification(data.get("id").toString());

		if (null == device) {
			return new EventResponseMessage(DT_NOT_FOUND, HttpStatus.NOT_FOUND);
		}

		if (apiKey.equals(device.getDigitalKey())) {

			// Set last updated
			device.setUpdatedAt(new Date());
			deviceRepo.save(device);

			// insert event
			DigitalTwinModel model = new DigitalTwinModel();
			DigitalTwinCompositeModel compositeModel = new DigitalTwinCompositeModel();

			model.setEvent(EventType.CUSTOM);
			model.setStatus(data.get(STATUS_STR).toString());
			model.setDeviceId(device.getId());
			model.setDeviceName(device.getIdentification());
			model.setType(device.getTypeId().getName());
			model.setEventName(data.get("event").toString());

			compositeModel.setDigitalTwinModel(model);
			compositeModel.setTimestamp(new Timestamp(System.currentTimeMillis()));

			OperationResultModel result = routerDigitalTwinService.insertEvent(compositeModel);
			if (!result.isStatus()) {
				return new EventResponseMessage(result.getMessage(), HttpStatus.valueOf(result.getErrorCode()));
			}

			notifyCustomSubscriptors(data);

			return new EventResponseMessage(result.getMessage(), HttpStatus.OK);
		} else {
			return new EventResponseMessage(TOKEN_NOT_VALID, HttpStatus.UNAUTHORIZED);
		}
	}

	@Override
	public EventResponseMessage action(String apiKey, JSONObject data) {

		if (data.get("id") == null) {
			return new EventResponseMessage(ID_REQUIRED, HttpStatus.BAD_REQUEST);
		}

		// Validation apikey
		DigitalTwinDevice device = deviceRepo.findByIdentification(data.get("id").toString());

		if (null == device) {
			return new EventResponseMessage(DT_NOT_FOUND, HttpStatus.NOT_FOUND);
		}

		if (apiKey.equals(device.getDigitalKey())) {

			// Set last updated
			device.setUpdatedAt(new Date());
			deviceRepo.save(device);

			DigitalTwinModel model = new DigitalTwinModel();
			DigitalTwinCompositeModel compositeModel = new DigitalTwinCompositeModel();

			model.setActionName(data.getString("name"));
			model.setDeviceId(device.getId());
			model.setDeviceName(device.getIdentification());
			model.setType(device.getTypeId().getName());
			if (data.has(STATUS_STR)) {
				model.setStatus(data.get(STATUS_STR).toString());
			}

			compositeModel.setDigitalTwinModel(model);
			compositeModel.setTimestamp(new Timestamp(System.currentTimeMillis()));

			OperationResultModel result = routerDigitalTwinService.insertAction(compositeModel);
			if (!result.isStatus()) {
				return new EventResponseMessage(result.getMessage(), HttpStatus.valueOf(result.getErrorCode()));
			}

			notifyActionSubscriptors(data);

			return new EventResponseMessage(result.getMessage(), HttpStatus.OK);
		} else {
			return new EventResponseMessage(TOKEN_NOT_VALID, HttpStatus.UNAUTHORIZED);
		}
	}

	private void notifyShadowSubscriptors(JSONObject message) {
		log.info("notifyShadowSubscriptors");
		notifierExecutor.execute(new Runnable() {
			@Override
			public void run() {
				log.info("notifyShadowSubscriptors execution");
				digitalTwinWebsocketApi.notifyShadowMessage(message);
			}
		});

	}

	private void notifyCustomSubscriptors(JSONObject message) {
		log.info("notifyCustomSubscriptors");
		notifierExecutor.execute(new Runnable() {
			@Override
			public void run() {
				log.info("notifyCustomSubscriptors execution");
				digitalTwinWebsocketApi.notifyCustomMessage(message);
			}
		});
	}

	private void notifyActionSubscriptors(JSONObject message) {
		log.info("notifyActionSubscriptors");
		// Notify to Gateways
		for (ActionNotifier actionNotifier : actionNotifiers) {
			notifierExecutor.execute(new Runnable() {
				@Override
				public void run() {
					log.info("notifyActionSubscriptors execution");
					actionNotifier.notifyActionMessage(message);
				}
			});
		}

		// Notify to Digital Twin Websocket API
		notifierExecutor.execute(new Runnable() {
			@Override
			public void run() {
				digitalTwinWebsocketApi.notifyActionMessage(message);
			}
		});
	}

}
