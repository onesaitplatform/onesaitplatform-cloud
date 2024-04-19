/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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
import com.minsait.onesait.platform.config.services.digitaltwin.device.DigitalTwinDeviceService;
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
	//
	// @Autowired
	// private DigitalTwinDeviceRepository deviceRepo;
	@Autowired
	private DigitalTwinDeviceService digitalTwinDeviceService;

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
		notifierExecutor.shutdown();
	}

	@Override
	public EventResponseMessage register(String apiKey, JSONObject data) {

		// Validation apikey
		if (data.get("id") == null || data.get("endpoint") == null) {
			return new EventResponseMessage("id and endpoint are required", HttpStatus.BAD_REQUEST);
		}
		final DigitalTwinDevice device = digitalTwinDeviceService.getDigitalTwinDevicebyName(apiKey,
				data.get("id").toString());

		if (null == device) {
			return new EventResponseMessage(DT_NOT_FOUND, HttpStatus.NOT_FOUND);
		}

		if (apiKey.equals(device.getDigitalKey())) {
			// Set endpoint
			final String deviceUrl = data.get("endpoint").toString();

			final String urlSchema = deviceUrl.split("://")[0];
			final String ip = deviceUrl.split("://")[1].split("/")[0].split(":")[0];
			final String port = deviceUrl.split("://")[1].split("/")[0].split(":")[1];
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

			digitalTwinDeviceService.save(device);

			// insert the register event
			final DigitalTwinModel model = new DigitalTwinModel();
			final DigitalTwinCompositeModel compositeModel = new DigitalTwinCompositeModel();

			model.setEvent(EventType.REGISTER);
			model.setDeviceId(device.getId());
			model.setDeviceName(device.getIdentification());
			model.setType(device.getTypeId().getIdentification());
			model.setEndpoint(deviceUrl);

			compositeModel.setDigitalTwinModel(model);
			compositeModel.setTimestamp(new Timestamp(System.currentTimeMillis()));

			final OperationResultModel result = routerDigitalTwinService.insertLog(compositeModel);
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

		final DigitalTwinDevice device = digitalTwinDeviceService.getDigitalTwinDevicebyName(apiKey,
				data.get("id").toString());

		if (null == device) {
			return new EventResponseMessage(DT_NOT_FOUND, HttpStatus.NOT_FOUND);
		}

		if (apiKey.equals(device.getDigitalKey())) {
			// Set last updated
			device.setUpdatedAt(new Date());
			digitalTwinDeviceService.save(device);

			// insert the ping event
			final DigitalTwinModel model = new DigitalTwinModel();
			final DigitalTwinCompositeModel compositeModel = new DigitalTwinCompositeModel();

			model.setEvent(EventType.PING);
			model.setDeviceId(device.getId());
			model.setDeviceName(device.getIdentification());
			model.setType(device.getTypeId().getIdentification());

			compositeModel.setDigitalTwinModel(model);
			compositeModel.setTimestamp(new Timestamp(System.currentTimeMillis()));

			final OperationResultModel result = routerDigitalTwinService.insertLog(compositeModel);
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
		final DigitalTwinDevice device = digitalTwinDeviceService.getDigitalTwinDevicebyName(apiKey,
				data.get("id").toString());

		if (null == device) {
			return new EventResponseMessage(DT_NOT_FOUND, HttpStatus.NOT_FOUND);
		}

		if (apiKey.equals(device.getDigitalKey())) {
			// Set last updated
			device.setUpdatedAt(new Date());
			digitalTwinDeviceService.save(device);
			// insert trace of log
			final DigitalTwinModel model = new DigitalTwinModel();
			final DigitalTwinCompositeModel compositeModel = new DigitalTwinCompositeModel();

			model.setEvent(EventType.LOG);
			model.setLog(data.get("log").toString());
			model.setDeviceId(device.getId());
			model.setDeviceName(device.getIdentification());
			model.setType(device.getTypeId().getIdentification());

			compositeModel.setDigitalTwinModel(model);
			compositeModel.setTimestamp(new Timestamp(System.currentTimeMillis()));

			final OperationResultModel result = routerDigitalTwinService.insertLog(compositeModel);
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
		final DigitalTwinDevice device = digitalTwinDeviceService.getDigitalTwinDevicebyName(apiKey,
				data.get("id").toString());

		if (null == device) {
			return new EventResponseMessage(DT_NOT_FOUND, HttpStatus.NOT_FOUND);
		}

		if (apiKey.equals(device.getDigitalKey())) {

			// Set last updated
			device.setUpdatedAt(new Date());
			digitalTwinDeviceService.save(device);

			// insert shadow
			final DigitalTwinModel model = new DigitalTwinModel();
			final DigitalTwinCompositeModel compositeModel = new DigitalTwinCompositeModel();

			model.setEvent(com.minsait.onesait.platform.router.service.app.model.DigitalTwinModel.EventType.SHADOW);
			model.setStatus(data.get(STATUS_STR).toString());
			model.setDeviceId(device.getId());
			model.setDeviceName(device.getIdentification());
			model.setType(device.getTypeId().getIdentification());

			compositeModel.setDigitalTwinModel(model);
			compositeModel.setTimestamp(new Timestamp(System.currentTimeMillis()));

			log.info("Send updateshadow to router");
			final OperationResultModel result = routerDigitalTwinService.updateShadow(compositeModel);
			if (!result.isStatus()) {
				log.info("EventActionProcessorDelegate -- getErrorCode: " + result.getErrorCode());
				return new EventResponseMessage(result.getMessage(), HttpStatus.valueOf(result.getErrorCode()));
			}
			log.info("The Shadow is going to be notified to demo");
			notifyShadowSubscriptors(apiKey, data);

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
		final DigitalTwinDevice device = digitalTwinDeviceService.getDigitalTwinDevicebyName(apiKey,
				data.get("id").toString());

		if (null == device) {
			return new EventResponseMessage(DT_NOT_FOUND, HttpStatus.NOT_FOUND);
		}

		if (apiKey.equals(device.getDigitalKey())) {

			// Set last updated
			device.setUpdatedAt(new Date());
			digitalTwinDeviceService.save(device);

			// insert event
			final DigitalTwinModel model = new DigitalTwinModel();
			final DigitalTwinCompositeModel compositeModel = new DigitalTwinCompositeModel();

			model.setEvent(EventType.NOTEBOOK);
			model.setStatus(data.get(STATUS_STR).toString());
			model.setDeviceId(device.getId());
			model.setDeviceName(device.getIdentification());
			model.setType(device.getTypeId().getIdentification());

			compositeModel.setDigitalTwinModel(model);
			compositeModel.setTimestamp(new Timestamp(System.currentTimeMillis()));

			final OperationResultModel result = routerDigitalTwinService.insertEvent(compositeModel);
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
		final DigitalTwinDevice device = digitalTwinDeviceService.getDigitalTwinDevicebyName(apiKey,
				data.get("id").toString());

		if (null == device) {
			return new EventResponseMessage(DT_NOT_FOUND, HttpStatus.NOT_FOUND);
		}

		if (apiKey.equals(device.getDigitalKey())) {

			// Set last updated
			device.setUpdatedAt(new Date());
			digitalTwinDeviceService.save(device);

			// insert event
			final DigitalTwinModel model = new DigitalTwinModel();
			final DigitalTwinCompositeModel compositeModel = new DigitalTwinCompositeModel();

			model.setEvent(EventType.FLOW);
			model.setStatus(data.get(STATUS_STR).toString());
			model.setDeviceId(device.getId());
			model.setDeviceName(device.getIdentification());
			model.setType(device.getTypeId().getIdentification());

			compositeModel.setDigitalTwinModel(model);
			compositeModel.setTimestamp(new Timestamp(System.currentTimeMillis()));

			final OperationResultModel result = routerDigitalTwinService.insertEvent(compositeModel);
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
		final DigitalTwinDevice device = digitalTwinDeviceService.getDigitalTwinDevicebyName(apiKey,
				data.get("id").toString());

		if (null == device) {
			return new EventResponseMessage(DT_NOT_FOUND, HttpStatus.NOT_FOUND);
		}

		if (apiKey.equals(device.getDigitalKey())) {

			// Set last updated
			device.setUpdatedAt(new Date());
			digitalTwinDeviceService.save(device);

			// insert event
			final DigitalTwinModel model = new DigitalTwinModel();
			final DigitalTwinCompositeModel compositeModel = new DigitalTwinCompositeModel();

			model.setEvent(EventType.RULE);
			model.setStatus(data.get(STATUS_STR).toString());
			model.setDeviceId(device.getId());
			model.setDeviceName(device.getIdentification());
			model.setType(device.getTypeId().getIdentification());

			compositeModel.setDigitalTwinModel(model);
			compositeModel.setTimestamp(new Timestamp(System.currentTimeMillis()));

			final OperationResultModel result = routerDigitalTwinService.insertEvent(compositeModel);
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
		final DigitalTwinDevice device = digitalTwinDeviceService.getDigitalTwinDevicebyName(apiKey,
				data.get("id").toString());

		if (null == device) {
			return new EventResponseMessage(DT_NOT_FOUND, HttpStatus.NOT_FOUND);
		}

		if (apiKey.equals(device.getDigitalKey())) {

			// Set last updated
			device.setUpdatedAt(new Date());
			digitalTwinDeviceService.save(device);

			// insert event
			final DigitalTwinModel model = new DigitalTwinModel();
			final DigitalTwinCompositeModel compositeModel = new DigitalTwinCompositeModel();

			model.setEvent(EventType.CUSTOM);
			model.setStatus(data.get(STATUS_STR).toString());
			model.setDeviceId(device.getId());
			model.setDeviceName(device.getIdentification());
			model.setType(device.getTypeId().getIdentification());
			model.setEventName(data.get("event").toString());

			compositeModel.setDigitalTwinModel(model);
			compositeModel.setTimestamp(new Timestamp(System.currentTimeMillis()));

			final OperationResultModel result = routerDigitalTwinService.insertEvent(compositeModel);
			if (!result.isStatus()) {
				return new EventResponseMessage(result.getMessage(), HttpStatus.valueOf(result.getErrorCode()));
			}

			notifyCustomSubscriptors(apiKey, data);

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
		final DigitalTwinDevice device = digitalTwinDeviceService.getDigitalTwinDevicebyName(apiKey,
				data.get("id").toString());

		if (null == device) {
			return new EventResponseMessage(DT_NOT_FOUND, HttpStatus.NOT_FOUND);
		}

		if (apiKey.equals(device.getDigitalKey())) {

			// Set last updated
			device.setUpdatedAt(new Date());
			digitalTwinDeviceService.save(device);

			final DigitalTwinModel model = new DigitalTwinModel();
			final DigitalTwinCompositeModel compositeModel = new DigitalTwinCompositeModel();

			model.setActionName(data.getString("name"));
			model.setDeviceId(device.getId());
			model.setDeviceName(device.getIdentification());
			model.setType(device.getTypeId().getIdentification());
			if (data.has(STATUS_STR)) {
				model.setStatus(data.get(STATUS_STR).toString());
			}

			compositeModel.setDigitalTwinModel(model);
			compositeModel.setTimestamp(new Timestamp(System.currentTimeMillis()));

			final OperationResultModel result = routerDigitalTwinService.insertAction(compositeModel);
			if (!result.isStatus()) {
				return new EventResponseMessage(result.getMessage(), HttpStatus.valueOf(result.getErrorCode()));
			}

			notifyActionSubscriptors(apiKey, data);

			return new EventResponseMessage(result.getMessage(), HttpStatus.OK);
		} else {
			return new EventResponseMessage(TOKEN_NOT_VALID, HttpStatus.UNAUTHORIZED);
		}
	}

	private void notifyShadowSubscriptors(String apiKey, JSONObject message) {
		log.info("notifyShadowSubscriptors");
		notifierExecutor.execute(new Runnable() {
			@Override
			public void run() {
				log.info("notifyShadowSubscriptors execution");
				digitalTwinWebsocketApi.notifyShadowMessage(apiKey, message);
			}
		});

	}

	private void notifyCustomSubscriptors(String apiKey, JSONObject message) {
		log.info("notifyCustomSubscriptors");
		notifierExecutor.execute(new Runnable() {
			@Override
			public void run() {
				log.info("notifyCustomSubscriptors execution");
				digitalTwinWebsocketApi.notifyCustomMessage(apiKey, message);
			}
		});
	}

	private void notifyActionSubscriptors(String apiKey, JSONObject message) {
		log.info("notifyActionSubscriptors");
		// Notify to Gateways
		for (final ActionNotifier actionNotifier : actionNotifiers) {
			notifierExecutor.execute(new Runnable() {
				@Override
				public void run() {
					log.info("notifyActionSubscriptors execution");
					actionNotifier.notifyActionMessage(apiKey, message);
				}
			});
		}

		// Notify to Digital Twin Websocket API
		notifierExecutor.execute(new Runnable() {
			@Override
			public void run() {
				digitalTwinWebsocketApi.notifyActionMessage(apiKey, message);
			}
		});
	}

}
