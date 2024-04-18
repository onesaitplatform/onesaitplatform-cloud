/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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

import com.fasterxml.jackson.databind.JsonNode;
import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyLogMessage;
import com.minsait.onesait.platform.comms.protocol.body.parent.SSAPBodyMessage;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.iotbroker.plugable.interfaces.gateway.GatewayInfo;

public interface DeviceManager {

	public <T extends SSAPBodyMessage> boolean registerActivity(SSAPMessage<T> request, String clientPlatformIdentification, String clientPlatformInstanceIdentification, GatewayInfo info);

	public JsonNode createDeviceLog(ClientPlatform client, String deviceId, SSAPBodyLogMessage logMessage)
			throws IOException;

}
