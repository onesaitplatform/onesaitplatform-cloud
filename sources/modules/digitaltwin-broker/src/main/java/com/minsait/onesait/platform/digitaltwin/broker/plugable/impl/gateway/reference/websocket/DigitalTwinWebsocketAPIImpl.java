/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2022 SPAIN
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.minsait.onesait.platform.digitaltwin.broker.processor.ActionProcessor;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class DigitalTwinWebsocketAPIImpl implements DigitalTwinWebsocketAPI {

	private static final String NOTIFING_ERROR = "Error notifing message";
	private static final String NOTIFY_SHADOW_MESSAGE = "DigitalTwinWebsocketAPIImpl -- notifyShadowMessage -- URL:  ";

	@Autowired
	private ActionProcessor actionProcessor;

	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	/**
	 * Receives messages from Digital Twin Manager, to send to the real device
	 */
	@Override
	@MessageMapping("/sendAction")
	public void sendAction(String message, MessageHeaders messageHeaders) {
		try {

			final String apiKey = ((List) (((Map) messageHeaders.get("nativeHeaders")).get("Authorization"))).get(0)
					.toString();
			final JSONObject objMessage = new JSONObject(message);
			actionProcessor.action(apiKey, objMessage);
		} catch (final Exception e) {
			log.error("Error", e);
		}
	}

	@Override
	public void notifyShadowMessage(String apiKey, JSONObject message) {
		try {
			log.info("DigitalTwinWebsocketAPIImpl -- notifyShadowMessage: " + message);
			final String sourceTwin = message.get("id").toString();
			log.info(NOTIFY_SHADOW_MESSAGE + "/api/shadow/" + sourceTwin);
			messagingTemplate.convertAndSend("/api/shadow/" + sourceTwin, message.toString());
			log.info("Notify Shadow send.");
		} catch (final Exception e) {
			log.error(NOTIFING_ERROR, e);
		}
	}

	@Override
	public void notifyCustomMessage(String apiKey, JSONObject message) {
		try {
			log.info("DigitalTwinWebsocketAPIImpl -- notifyCustomMessage: " + message);
			final String sourceTwin = message.get("id").toString();
			log.info(NOTIFY_SHADOW_MESSAGE + "/api/custom/" + sourceTwin);
			messagingTemplate.convertAndSend("/api/custom/" + sourceTwin, message.toString());
			log.info("Notify custom send.");
		} catch (final Exception e) {
			log.error(NOTIFING_ERROR, e);
		}
	}

	@Override
	public void notifyActionMessage(String apiKey, JSONObject message) {
		try {
			log.info("DigitalTwinWebsocketAPIImpl -- notifyActionMessage: " + message);
			final String sourceTwin = message.get("id").toString();
			log.info(NOTIFY_SHADOW_MESSAGE + "/api/action/" + sourceTwin);
			messagingTemplate.convertAndSend("/api/action/" + sourceTwin, message.toString());
			log.info("Notify action send.");
		} catch (final Exception e) {
			log.error(NOTIFING_ERROR, e);
		}
	}

}
