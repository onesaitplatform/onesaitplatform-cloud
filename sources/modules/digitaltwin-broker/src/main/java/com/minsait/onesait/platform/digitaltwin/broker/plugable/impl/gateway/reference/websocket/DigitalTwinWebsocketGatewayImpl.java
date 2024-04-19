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

import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import com.minsait.onesait.platform.digitaltwin.broker.plugable.impl.gateway.reference.ActionNotifier;
import com.minsait.onesait.platform.digitaltwin.broker.processor.EventProcessor;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class DigitalTwinWebsocketGatewayImpl implements DigitalTwinWebsocketGateway, ActionNotifier {

	@Autowired
	private EventProcessor eventProcessor;

	@Override
	@MessageMapping("/custom")
	public void custom(String message, MessageHeaders messageHeaders) {
		try {

			final String apiKey = ((List) (((Map) messageHeaders.get("nativeHeaders")).get("Authorization"))).get(0)
					.toString();
			final JSONObject objMessage = new JSONObject(message);
			eventProcessor.custom(apiKey, objMessage);
		} catch (final Exception e) {
			log.error("Error", e);
		}
	}

	@Override
	public void notifyActionMessage(String apiKey, JSONObject message) {
		// not neccesary
	}

}
