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
package threads;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.client.MQTTClient;
import com.minsait.onesait.platform.client.enums.LogLevel;
import com.minsait.onesait.platform.client.enums.StatusType;

import application.Application;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DeviceConnectionThread implements Runnable {

	private static final String deviceTemplate = "DeviceMaster";
	private static final String token = "a16b9e7367734f04bc720e981fcf483f";
	private static final int timeout = 50;
	private final double latitude;
	private final double longitude;
	private final String logMessage;
	private final String deviceId;

	private String sessionKey;
	private final String serverUrl;
	private final String tags;
	private final MQTTClient client;
	private final ObjectMapper mapper = new ObjectMapper();
	private JsonNode deviceConfig;

	public DeviceConnectionThread(String serverUrl, double latitude, double longitude, String logMessage,
			String deviceId, String tags) {
		super();
		this.serverUrl = serverUrl;
		this.latitude = latitude;
		this.longitude = longitude;
		this.logMessage = logMessage;
		this.deviceId = deviceId;
		this.tags = tags;
		client = new MQTTClient(this.serverUrl, false);
		client.setTimeout(timeout);
		try {
			deviceConfig = mapper.readTree(
					"[{\"action_power\":{\"shutdown\":0,\"start\":1,\"reboot\":2}},{\"action_light\":{\"on\":1,\"off\":0}}]");
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		sessionKey = client.connect(token, deviceTemplate, deviceId, tags, deviceConfig);
		if (sessionKey != null) {
			client.subscribeCommands(new com.minsait.onesait.platform.client.model.SubscriptionListener() {

				@Override
				public void onMessageArrived(String message) {
					try {
						final JsonNode cmdMsg = mapper.readTree(message);
						generateCommandResponse(cmdMsg);
					} catch (final IOException e) {

						e.printStackTrace();
					}

				}

			});

			while (true) {
				LogLevel level = LogLevel.INFO;
				StatusType status = StatusType.OK;
				if (logMessage.equals(Application.ERROR_MESSAGE)) {
					level = LogLevel.ERROR;
					status = StatusType.ERROR;
				}
				client.log(logMessage, latitude, longitude, status, level);
				try {
					Thread.sleep(30000);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected void generateCommandResponse(JsonNode cmdMsg) {
		client.logCommand("Executed command " + cmdMsg.get("params").toString(), latitude, longitude, StatusType.OK,
				LogLevel.INFO, cmdMsg.get("commandId").asText());

	}

}