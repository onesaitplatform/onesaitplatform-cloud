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
package com.minsait.onesait.platform.digitaltwin.broker.plugable.impl.gateway.reference.websocket;

import org.json.JSONObject;
import org.springframework.messaging.MessageHeaders;

public interface DigitalTwinWebsocketAPI {

	public void sendAction(String message, MessageHeaders messageHeaders);

	public void notifyShadowMessage(String apiKey, JSONObject message);

	public void notifyCustomMessage(String apiKey, JSONObject message);

	public void notifyActionMessage(String apiKey, JSONObject message);

}
