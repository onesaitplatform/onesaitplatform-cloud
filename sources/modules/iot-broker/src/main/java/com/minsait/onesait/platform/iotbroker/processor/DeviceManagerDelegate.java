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
package com.minsait.onesait.platform.iotbroker.processor;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyJoinMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyLogMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyReturnMessage;
import com.minsait.onesait.platform.comms.protocol.body.parent.SSAPBodyMessage;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformInstance;
import com.minsait.onesait.platform.config.model.IoTSession;
import com.minsait.onesait.platform.config.services.client.ClientPlatformService;
import com.minsait.onesait.platform.config.services.device.ClientPlatformInstanceService;
import com.minsait.onesait.platform.iotbroker.plugable.interfaces.gateway.GatewayInfo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableScheduling
@Component
public class DeviceManagerDelegate implements DeviceManager {

	@Autowired
	ClientPlatformService clientPlatformService;
	@Autowired
	ClientPlatformInstanceService deviceService;

	@Value("${onesaitplatform.iotbroker.devices.perclient.max:0}")
	private int maxDevicesPerClient;

	ObjectMapper mapper = new ObjectMapper();

	private ThreadPoolExecutor activityExecutorPool;

	@PostConstruct
	public void init() {
		BlockingQueue activityRegistryQueue = new ArrayBlockingQueue(50);
		this.activityExecutorPool = new ThreadPoolExecutor(1, 1, 3, TimeUnit.SECONDS, activityRegistryQueue);
	}

	@PreDestroy
	public void predestroy() {
		this.activityExecutorPool.shutdown();
	}

	@Override
	public <T extends SSAPBodyMessage> boolean registerActivity(SSAPMessage<T> request,
			SSAPMessage<SSAPBodyReturnMessage> response, IoTSession session, GatewayInfo info) {
		try {
			activityExecutorPool.execute(() -> {
				try {
					ClientPlatform clientPlatform = clientPlatformService
							.getByIdentification(session.getClientPlatform());

					ClientPlatformInstance device = deviceService.getByClientPlatformIdAndIdentification(clientPlatform,
							session.getDevice());

					if (device == null) {
						if (maxDevicesPerClient > 0) {// Before creating a new Device, check if the max Device limit for
														// a
														// clientId
														// is reached
							synchronized (this) {
								List<ClientPlatformInstance> devices = deviceService
										.getByClientPlatformId(clientPlatform);
								if (devices.size() > maxDevicesPerClient) {

									devices.sort((ClientPlatformInstance o1, ClientPlatformInstance o2) -> {
										long comparation = o1.getUpdatedAt().getTime() - o2.getUpdatedAt().getTime();
										if (comparation == 0) {
											return 0;
										} else {
											return comparation > 0 ? 1 : -1;
										}

									});

									for (int i = 0; i < devices.size() - maxDevicesPerClient; i++) {
										deviceService.deleteClientPlatformInstance(devices.get(i));
									}

								}
							}
						}
						device = new ClientPlatformInstance();
						device.setClientPlatform(clientPlatform);
						device.setIdentification(session.getDevice());
						device.setProtocol(info.getProtocol());
					}

					switch (request.getMessageType()) {
					case JOIN:
						final SSAPBodyJoinMessage body = (SSAPBodyJoinMessage) request.getBody();
						device.setJsonActions(
								body.getDeviceConfiguration() != null ? body.getDeviceConfiguration().toString()
										: device.getJsonActions());
						device.setTags(body.getTags() != null ? body.getTags() : device.getTags());
						touchDevice(device, session, true, info, null, null);
						break;
					case LEAVE:
						touchDevice(device, session, false, info, null, null);
						break;
					case LOG:
						final SSAPBodyLogMessage logMessage = (SSAPBodyLogMessage) request.getBody();
						final double[] location = { logMessage.getCoordinates().getX(),
								logMessage.getCoordinates().getY() };
						touchDevice(device, session, true, info, logMessage.getStatus().name(), location);
						break;
					default:
						touchDevice(device, session, true, info, null, null);
						break;
					}
				} catch (Exception e) {
					log.error("Error registering device activity", e);
				}
			});

			return true;
		} catch (RejectedExecutionException rej) {
			log.warn("Error registering device activity", rej);
			return false;
		} catch (Exception e) {
			log.error("Error registering device activity", e);
			return false;
		}
	}

	@Scheduled(fixedDelay = 60000)
	public void updatingDevicesPeriodic() {
		updatingDevices();
	}

	@PostConstruct
	public void updatingDevicesAtStartUp() {
		updatingDevices();
	}

	private void updatingDevices() {
		log.info("Start Updating all devices");
		final Calendar c = Calendar.getInstance();
		long millis = c.getTimeInMillis() - 5 * 60 * 1000l;
		c.setTimeInMillis(millis);

		// Setting connected false when 5 minutes without activity
		int n = deviceService.updateClientPlatformInstanceStatusAndDisableWhenUpdatedAtLessThanDate(false, false,
				c.getTime());
		log.info("End Updating all devices: {} disconected", n);

		// Setting disabled a true when 1 day witout activity
		millis = c.getTimeInMillis() - 24 * 60 * 60 * 1000l;
		c.setTimeInMillis(millis);
		n = deviceService.updateClientPlatformInstanceStatusAndDisableWhenUpdatedAtLessThanDate(false, true,
				c.getTime());
		log.info("End Updating all devices: {} disabled", n);

	}

	private void touchDevice(ClientPlatformInstance device, IoTSession session, boolean connected, GatewayInfo info,
			String status, double[] location) {
		log.info("Start Updating device {}", device.getIdentification());
		device.setStatus(status == null ? ClientPlatformInstance.StatusType.OK.name() : status);
		device.setSessionKey(session.getSessionKey());
		device.setConnected(connected);
		device.setDisabled(false);
		device.setProtocol(info.getProtocol());
		device.setUpdatedAt(new Date());
		device.setClientPlatform(clientPlatformService.getByIdentification(session.getClientPlatform()));// nuevo
		device.setIdentification(session.getDevice());// nuevo
		if (location != null)
			device.setLocation(location);
		if (device.getId() != null && location == null)
			deviceService.updateClientPlatformInstance(device);
		else
			deviceService.createClientPlatformInstance(device);

		log.info("End Updating device {}", device.getIdentification());
	}

	@Override
	public JsonNode createDeviceLog(ClientPlatform client, String deviceId, SSAPBodyLogMessage logMessage)
			throws IOException {
		final ClientPlatformInstance device = deviceService.getByClientPlatformIdAndIdentification(client, deviceId);
		final double longitude = logMessage.getCoordinates() == null ? 0 : logMessage.getCoordinates().getX();
		final double latitude = logMessage.getCoordinates() == null ? 0 : logMessage.getCoordinates().getY();
		return createLogInstance(device, logMessage.getStatus().name(), logMessage.getLevel().name(),
				logMessage.getMessage(), logMessage.getExtraData().toString(), longitude, latitude,
				logMessage.getCommandId());

	}

	public JsonNode createLogInstance(ClientPlatformInstance device, String status, String level, String message,
			String extraOptions, double longitude, double latitude, String commandId) {
		final JsonNode root = mapper.createObjectNode();
		final JsonNode properties = mapper.createObjectNode();
		((ObjectNode) properties).put("device", device.getIdentification());
		((ObjectNode) properties).put("level", level);
		((ObjectNode) properties).put("status", status);
		((ObjectNode) properties).put("message", message);
		final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		((ObjectNode) properties).put("timestamp", df.format(new Date()));
		if (extraOptions != null)
			((ObjectNode) properties).put("extraOptions", extraOptions);
		if (longitude != 0 && latitude != 0) {
			final JsonNode subcoordinates = mapper.createObjectNode();
			((ObjectNode) subcoordinates).put("latitude", latitude);
			((ObjectNode) subcoordinates).put("longitude", longitude);
			final JsonNode coordinates = mapper.createObjectNode();
			((ObjectNode) coordinates).set("coordinates", subcoordinates);
			((ObjectNode) coordinates).put("type", "Point");
			((ObjectNode) properties).set("location", coordinates);
		}
		if (commandId != null) {
			((ObjectNode) properties).put("commandId", commandId);
		}
		((ObjectNode) root).set("DeviceLog", properties);
		return root;

	}

}
