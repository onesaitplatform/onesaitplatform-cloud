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
package com.minsait.onesait.platform.digitaltwin.broker.plugable.impl.gateway.reference.rest;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.minsait.onesait.platform.digitaltwin.broker.processor.EventProcessor;
import com.minsait.onesait.platform.digitaltwin.broker.processor.model.EventResponseMessage;

import lombok.extern.slf4j.Slf4j;

@RestController
@ConditionalOnProperty(prefix = "onesaitplatform.digitaltwin.broker.rest", name = "enable", havingValue = "true")
@EnableAutoConfiguration
@Slf4j
public class EventGatewayImpl implements EventGateway {

	@Autowired
	private EventProcessor eventProcessor;

	@Override
	public ResponseEntity<?> register(@RequestHeader(value = "Authorization") String apiKey,
			@RequestBody JsonNode data) {
		if (log.isDebugEnabled()) {
			log.debug("Received register event: {}", data.toString());
		}
		try {
			EventResponseMessage eventMessage = eventProcessor.register(apiKey, new JSONObject(data.toString()));
			return new ResponseEntity<>(eventMessage.getMessage(), eventMessage.getCode());

		} catch (Exception e) {
			log.error("Error in register event", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<?> ping(@RequestHeader(value = "Authorization") String apiKey, @RequestBody JsonNode data) {

		if (log.isDebugEnabled()) {
			log.debug("Received ping event: {}", data.toString());
		}
		try {
			EventResponseMessage eventMessage = eventProcessor.ping(apiKey, new JSONObject(data.toString()));
			return new ResponseEntity<>(eventMessage.getMessage(), eventMessage.getCode());

		} catch (Exception e) {
			log.error("Error in ping event", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<?> log(@RequestHeader(value = "Authorization") String apiKey, @RequestBody JsonNode data) {

		if (log.isDebugEnabled()) {
			log.debug("Received log event: {}", data.toString());
		}
		try {
			EventResponseMessage eventMessage = eventProcessor.log(apiKey, new JSONObject(data.toString()));
			return new ResponseEntity<>(eventMessage.getMessage(), eventMessage.getCode());

		} catch (Exception e) {
			log.error("Error in log event", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<?> shadow(@RequestHeader(value = "Authorization") String apiKey, @RequestBody JsonNode data) {

		if (log.isDebugEnabled()) {
			log.debug("Received shadow event: {}", data.toString());
		}
		try {
			EventResponseMessage eventMessage = eventProcessor.shadow(apiKey, new JSONObject(data.toString()));
			return new ResponseEntity<>(eventMessage.getMessage(), eventMessage.getCode());

		} catch (Exception e) {
			log.error("Error in shadow event", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<?> notebook(@RequestHeader(value = "Authorization") String apiKey,
			@RequestBody JsonNode data) {

		if (log.isDebugEnabled()) {
			log.debug("Received notebook event: {}", data.toString());
		}
		try {
			EventResponseMessage eventMessage = eventProcessor.notebook(apiKey, new JSONObject(data.toString()));
			return new ResponseEntity<>(eventMessage.getMessage(), eventMessage.getCode());

		} catch (Exception e) {
			log.error("Error in notebook event", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<?> flow(@RequestHeader(value = "Authorization") String apiKey, @RequestBody JsonNode data) {

		if (log.isDebugEnabled()) {
			log.debug("Received flow event: {}", data.toString());
		}
		try {
			EventResponseMessage eventMessage = eventProcessor.flow(apiKey, new JSONObject(data.toString()));
			return new ResponseEntity<>(eventMessage.getMessage(), eventMessage.getCode());

		} catch (Exception e) {
			log.error("Error in flow event", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Override
	public ResponseEntity<?> rule(@RequestHeader(value = "Authorization") String apiKey, @RequestBody JsonNode data) {

		if (log.isDebugEnabled()) {
			log.debug("Received rule event: {}", data.toString());
		}
		try {
			EventResponseMessage eventMessage = eventProcessor.rule(apiKey, new JSONObject(data.toString()));
			return new ResponseEntity<>(eventMessage.getMessage(), eventMessage.getCode());

		} catch (Exception e) {
			log.error("Error in rule event", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Override
	public ResponseEntity<?> custom(@RequestHeader(value = "Authorization") String apiKey, @RequestBody JsonNode data) {
		if (log.isDebugEnabled()) {
			log.debug("Received custom event: {}", data.toString());
		}
		try {
			EventResponseMessage eventMessage = eventProcessor.custom(apiKey, new JSONObject(data.toString()));
			return new ResponseEntity<>(eventMessage.getMessage(), eventMessage.getCode());

		} catch (Exception e) {
			log.error("Error in rule custom", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
