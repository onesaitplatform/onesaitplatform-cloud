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
package com.minsait.onesait.platform.iotbroker.plugable.impl.gateway.reference.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.JsonNode;
import com.minsait.onesait.platform.business.services.interceptor.MultitenancyInterceptor;
import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyCommitTransactionMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyDeleteByIdMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyDeleteMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyEmptyMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyEmptySessionMandatoryMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyInsertMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyJoinMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyLeaveMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyQueryMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyReturnMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyUpdateByIdMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyUpdateMessage;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPErrorCode;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageDirection;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageTypes;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPQueryResultFormat;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPQueryType;
import com.minsait.onesait.platform.comms.protocol.util.SSAPMessageGenerator;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataJsonProblemException;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataService;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataUnauthorizedException;
import com.minsait.onesait.platform.iotbroker.plugable.impl.security.SecurityPluginManager;
import com.minsait.onesait.platform.iotbroker.plugable.interfaces.gateway.GatewayInfo;
import com.minsait.onesait.platform.iotbroker.processor.GatewayNotifier;
import com.minsait.onesait.platform.iotbroker.processor.MessageProcessor;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;
import com.minsait.onesait.platform.multitenant.config.model.IoTSession;
import com.minsait.onesait.platform.multitenant.config.model.MasterDeviceToken;
import com.minsait.onesait.platform.multitenant.config.model.Vertical;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@ConditionalOnProperty(prefix = "onesaitplatform.iotbroker.plugable.gateway.rest", name = "enable", havingValue = "true")
@RestController
@RequestMapping(path = "/rest"
// produces= MediaType.APPLICATION_JSON_UTF8_VALUE,
// consumes=MediaType.APPLICATION_JSON_UTF8_VALUE
)
@EnableAutoConfiguration
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "rest", description = "onesait Platform operations for devices")
public class Rest implements WebMvcConfigurer {

	@Autowired
	MessageProcessor processor;

	@Autowired
	GatewayNotifier subscriptor;

	@Autowired
	private OntologyDataService ontologyDataService;

	@Autowired
	SecurityPluginManager securityPluginManager;

	@PostConstruct
	private void init() {
		subscriptor.addSubscriptionListener("rest_gateway", (s) -> log.info("rest_gateway fake processing"));
	}

	@Autowired
	private MultitenancyInterceptor multitenancyInterceptor;
	@Autowired
	private MultitenancyService multitenancyService;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(multitenancyInterceptor);
	}

	@Operation(summary = "Logs a client device into onesaitPlatform with token.\nReturns a sessionKey to use in further operations")
	@GetMapping(value = "/client/join")
	public ResponseEntity<?> join(
			@Parameter(description = "Token asociated to client platform", required = true) @RequestParam(name = "token") String token,
			@Parameter(description = "Client Platform asociated to token", required = true) @RequestParam(name = "clientPlatform") String clientPlatform,
			@Parameter(description = "Desired ClientPlatform id. the value is chosen from user", required = true) @RequestParam(name = "clientPlatformId") String clientPlatformId,
			@Parameter(description = "Key-value optional tags.", example = "{\"source\":\"IOTBROKER\", \"key\": \"value\"}", required = false) @RequestParam(name = "tags", required = false) String tags) {

		final SSAPMessage<SSAPBodyJoinMessage> request = new SSAPMessage<>();
		request.setBody(new SSAPBodyJoinMessage());

		request.setDirection(SSAPMessageDirection.REQUEST);
		request.setMessageType(SSAPMessageTypes.JOIN);
		request.getBody().setToken(token);
		request.getBody().setDeviceTemplate(clientPlatform);
		request.getBody().setDevice(clientPlatformId);
		request.getBody().setTags(tags);

		final SSAPMessage<SSAPBodyReturnMessage> response = processor.process(request, getGatewayInfo());
		if (!SSAPMessageDirection.ERROR.equals(response.getDirection())) {
			return new ResponseEntity<>(response.getBody().getData(), HttpStatus.OK);
		} else {
			return formResponseError(response);
		}
	}

	@Operation(summary = "Logs a client device into onesaitPlatform Kafka cluster with token, vertical and tenant.\nReturns a sessionKey to use in further operations")
	@GetMapping(value = "/client/kafka/join")
	public ResponseEntity<?> joinKafka(
			@Parameter(description = "Token asociated to client platform", required = true) @RequestParam(name = "token") String token,
			@Parameter(description = "Client Platform asociated to token", required = true) @RequestParam(name = "clientPlatform") String clientPlatform,
			@Parameter(description = "Desired ClientPlatform id. the value is chosen from user", required = true) @RequestParam(name = "clientPlatformId") String clientPlatformId,
			@Parameter(description = "Client platform vertical", required = true) @RequestParam(name = "vertical") String vertical,
			@Parameter(description = "Client Platform tenant", required = true) @RequestParam(name = "tenant") String tenant,
			@Parameter(description = "Key-value optional tags.", example = "{\"source\":\"IOTBROKER\", \"key\": \"value\"}", required = false) @RequestParam(name = "tags", required = false) String tags) {

		final SSAPMessage<SSAPBodyReturnMessage> verticalAndTenantResponse = checkVerticalAndTenant(token, vertical,
				tenant);
		if (verticalAndTenantResponse != null) {
			return formResponseError(verticalAndTenantResponse);
		}
		final SSAPMessage<SSAPBodyJoinMessage> request = new SSAPMessage<>();
		request.setBody(new SSAPBodyJoinMessage());

		request.setDirection(SSAPMessageDirection.REQUEST);
		request.setMessageType(SSAPMessageTypes.JOIN);
		request.getBody().setToken(token);
		request.getBody().setDeviceTemplate(clientPlatform);
		request.getBody().setDevice(clientPlatformId);
		request.getBody().setTags(tags);

		final SSAPMessage<SSAPBodyReturnMessage> response = processor.process(request, getGatewayInfo());
		if (!SSAPMessageDirection.ERROR.equals(response.getDirection())) {
			return new ResponseEntity<>(response.getBody().getData(), HttpStatus.OK);
		} else {
			return formResponseError(response);
		}
	}

	@Operation(summary = "Logs out a client device into onesaitPlatform with token", security = {
			@SecurityRequirement(name = "session-key") })
	@GetMapping(value = "/client/leave")
	public ResponseEntity<?> leave(
			@Parameter(description = "SessionKey provided from join operation", hidden = true) @RequestHeader(value = "Authorization") String sessionKey,
			@Parameter(description = "Key-value optional tags.", example = "{\"source\":\"IOTBROKER\", \"key\": \"value\"}", required = false) @RequestParam(name = "tags", required = false) String tags) {

		final SSAPMessage<SSAPBodyLeaveMessage> request = new SSAPMessage<>();
		request.setBody(new SSAPBodyLeaveMessage());

		request.setDirection(SSAPMessageDirection.REQUEST);
		request.setMessageType(SSAPMessageTypes.LEAVE);
		request.setSessionKey(sessionKey);
		request.getBody().setTags(tags);

		final SSAPMessage<SSAPBodyReturnMessage> response = processor.process(request, getGatewayInfo());
		if (!SSAPMessageDirection.ERROR.equals(response.getDirection())) {
			return new ResponseEntity<>(response.getBody().getData(), HttpStatus.OK);
		} else {
			return formResponseError(response);
		}
	}

	@Operation(summary = "Get a list of instances of a ontology data", security = {
			@SecurityRequirement(name = "session-key") })
	@GetMapping(value = "/ontology/{ontology}")
	public ResponseEntity<?> list(
			@Parameter(description = "SessionKey provided from join operation", hidden = true) @RequestHeader(value = "Authorization") String sessionKey,
			@Parameter(description = "Ontology to perform operation. Client platform must have granted permissions ", required = true) @PathVariable("ontology") String ontology,
			@Parameter(description = "Examples:\n\tNATIVE: db.temperature.find({})\n\tSQL: select * from temperature ", required = true) @RequestParam(name = "query") String query,
			@Parameter(description = "OPTIONS: NATIVE or SQL", required = true) @RequestParam(name = "queryType") SSAPQueryType queryType,
			@Parameter(description = "Key-value optional tags.", example = "{\"source\":\"IOTBROKER\", \"key\": \"value\"}", required = false) @RequestParam(name = "tags", required = false) String tags) {

		log.debug("Request with ontology {} and query {} type {}", ontology, query, queryType);

		final SSAPMessage<SSAPBodyQueryMessage> request = new SSAPMessage<>();
		request.setBody(new SSAPBodyQueryMessage());

		request.setDirection(SSAPMessageDirection.REQUEST);
		request.setMessageType(SSAPMessageTypes.QUERY);
		request.setSessionKey(sessionKey);
		request.getBody().setCacheTime(0);
		request.getBody().setOntology(ontology);
		request.getBody().setQuery(query);
		request.getBody().setQueryType(queryType);
		request.getBody().setResultFormat(SSAPQueryResultFormat.JSON);
		request.getBody().setTags(tags);

		final SSAPMessage<SSAPBodyReturnMessage> response = processor.process(request, getGatewayInfo());
		if (!SSAPMessageDirection.ERROR.equals(response.getDirection())) {
			return new ResponseEntity<>(response.getBody().getData(), HttpStatus.OK);
		} else {
			return formResponseError(response);
		}
	}

	@Operation(summary = "Get a list of instances of a ontology data through POST HTTP", security = {
			@SecurityRequirement(name = "session-key") })
	@PostMapping(value = "/ontology/{ontology}/query")
	public ResponseEntity<?> postList(
			@Parameter(description = "SessionKey provided from join operation", hidden = true) @RequestHeader(value = "Authorization") String sessionKey,
			@Parameter(description = "Ontology to perform operation. Client platform must have granted permissions ", required = true) @PathVariable("ontology") String ontology,
			@Parameter(description = "Examples:\n\tNATIVE: db.temperature.find({})\n\tSQL: select * from temperature ", required = true) @RequestParam(name = "query") String query,
			@Parameter(description = "OPTIONS: NATIVE or SQL", required = true) @RequestParam(name = "queryType") SSAPQueryType queryType,
			@Parameter(description = "Key-value optional tags.", example = "{\"source\":\"IOTBROKER\", \"key\": \"value\"}", required = false) @RequestParam(name = "tags", required = false) String tags) {

		return list(sessionKey, ontology, query, queryType, tags);

	}

	@Operation(summary = "Inserts a instance of a ontology expresed in json format", security = {
			@SecurityRequirement(name = "session-key") })
	@PostMapping(value = "/ontology/{ontology}")
	public ResponseEntity<?> create(
			@Parameter(description = "SessionKey provided from join operation", hidden = true) @RequestHeader(value = "Authorization") String sessionKey,
			@Parameter(description = "TransactionId provided from start transaction operation", required = false) @RequestHeader(value = "TransactionId", required = false) String transactionId,
			@Parameter(description = "Ontology to perform operation. Client platform must have granted permissions ", required = true) @PathVariable("ontology") String ontology,
			@Parameter(description = "Json data representing ontology instance", required = true) @RequestBody JsonNode data,
			@Parameter(description = "Key-value optional tags.", example = "{\"source\":\"IOTBROKER\", \"key\": \"value\"}", required = false) @RequestParam(name = "tags", required = false) String tags) {

		final SSAPMessage<SSAPBodyInsertMessage> request = new SSAPMessage<>();
		request.setBody(new SSAPBodyInsertMessage());

		if (null != transactionId && transactionId.length() > 0) {
			request.setTransactionId(transactionId);
		}

		request.setDirection(SSAPMessageDirection.REQUEST);
		request.setMessageType(SSAPMessageTypes.INSERT);
		request.setSessionKey(sessionKey);
		request.getBody().setOntology(ontology);
		request.getBody().setData(data);
		request.getBody().setTags(tags);

		final SSAPMessage<SSAPBodyReturnMessage> response = processor.process(request, getGatewayInfo());
		if (!SSAPMessageDirection.ERROR.equals(response.getDirection())) {
			return new ResponseEntity<>(response.getBody().getData(), HttpStatus.OK);
		} else {
			return formResponseError(response);
		}
	}

	@Operation(summary = "Updates a instance of a ontology expresed in json format", security = {
			@SecurityRequirement(name = "session-key") })
	@PutMapping(value = "/ontology/{ontology}/{id}")
	public ResponseEntity<?> updateById(
			@Parameter(description = "SessionKey provided from join operation", hidden = true) @RequestHeader(value = "Authorization") String sessionKey,
			@Parameter(description = "TransactionId provided from start transaction operation", required = false) @RequestHeader(value = "TransactionId", required = false) String transactionId,
			@Parameter(description = "Ontology to perform operation. Client platform must have granted permissions ", required = true) @PathVariable("ontology") String ontology,
			@Parameter(description = "Ontology identification to perform operation", required = true) @PathVariable("id") String id,
			@Parameter(description = "Json data representing ontology instance", required = true) @RequestBody JsonNode data,
			@Parameter(description = "Response Will include modidified Ids", required = false) @RequestParam(name = "ids", required = false, defaultValue = "false") String ids,
			@Parameter(description = "Key-value optional tags.", example = "{\"source\":\"IOTBROKER\", \"key\": \"value\"}", required = false) @RequestParam(name = "tags", required = false) String tags) {

		final SSAPMessage<SSAPBodyUpdateByIdMessage> request = new SSAPMessage<>();
		request.setBody(new SSAPBodyUpdateByIdMessage());

		if (null != transactionId && transactionId.length() > 0) {
			request.setTransactionId(transactionId);
		}

		request.setDirection(SSAPMessageDirection.REQUEST);
		request.setMessageType(SSAPMessageTypes.UPDATE_BY_ID);
		request.setSessionKey(sessionKey);
		request.getBody().setId(id);
		request.getBody().setOntology(ontology);
		request.getBody().setData(data);
		request.getBody().setTags(tags);
		try {
			request.setIncludeIds(Boolean.parseBoolean(ids));
		} catch (final Exception e) {
			request.setIncludeIds(false);
		}

		final SSAPMessage<SSAPBodyReturnMessage> response = processor.process(request, getGatewayInfo());
		if (!SSAPMessageDirection.ERROR.equals(response.getDirection())) {
			return new ResponseEntity<>(response.getBody().getData(), HttpStatus.OK);
		} else {
			return formResponseError(response);
		}
	}

	// Use put operation instad this one
	@Deprecated
	@Operation(summary = "Updates a instance or instances of a ontology with a update query", security = {
			@SecurityRequirement(name = "session-key") })
	@GetMapping(value = "/ontology/{ontology}/update")
	public ResponseEntity<?> updateByQuery(
			@Parameter(description = "SessionKey provided from join operation", hidden = true) @RequestHeader(value = "Authorization") String sessionKey,
			@Parameter(description = "TransactionId provided from start transaction operation", required = false) @RequestHeader(value = "TransactionId", required = false) String transactionId,
			@Parameter(description = "Ontology to perform operation. Client platform must have granted permissions ", required = true) @PathVariable("ontology") String ontology,
			@Parameter(description = "Examples: NATIVE: db.temperature.update({\"location\":\"Helsinki\"}, { $set:{\"value\":15}})", required = true) @RequestParam(name = "query") String query,
			@Parameter(description = "Response Will include modidified Ids", required = false) @RequestParam(name = "ids", required = false, defaultValue = "false") String ids) {

		final SSAPMessage<SSAPBodyUpdateMessage> request = new SSAPMessage<>();
		request.setBody(new SSAPBodyUpdateMessage());

		if (null != transactionId && transactionId.length() > 0) {
			request.setTransactionId(transactionId);
		}

		request.setDirection(SSAPMessageDirection.REQUEST);
		request.setMessageType(SSAPMessageTypes.UPDATE);
		request.setSessionKey(sessionKey);
		request.getBody().setOntology(ontology);
		request.getBody().setQuery(query);

		try {
			request.setIncludeIds(Boolean.parseBoolean(ids));
		} catch (final Exception e) {
			request.setIncludeIds(false);
		}

		final SSAPMessage<SSAPBodyReturnMessage> response = processor.process(request, getGatewayInfo());
		if (!SSAPMessageDirection.ERROR.equals(response.getDirection())) {
			return new ResponseEntity<>(response.getBody().getData(), HttpStatus.OK);
		} else {
			return formResponseError(response);
		}
	}

	@Operation(summary = "Updates a instance or instances of a ontology with a update query in body", security = {
			@SecurityRequirement(name = "session-key") })
	@PutMapping(value = "/ontology/{ontology}/update")
	public ResponseEntity<?> updateByQueryBody(
			@Parameter(description = "SessionKey provided from join operation", hidden = true) @RequestHeader(value = "Authorization") String sessionKey,
			@Parameter(description = "TransactionId provided from start transaction operation", required = false) @RequestHeader(value = "TransactionId", required = false) String transactionId,
			@Parameter(description = "Ontology to perform operation. Client platform must have granted permissions ", required = true) @PathVariable("ontology") String ontology,
			@Parameter(description = "Examples: NATIVE: db.temperature.update({\"location\":\"Helsinki\"}, { $set:{\"value\":15}})", required = true, example = "") @RequestBody String query,
			@Parameter(description = "Response Will include modidified Ids", required = false) @RequestParam(name = "ids", required = false, defaultValue = "false") String ids,
			@Parameter(description = "Key-value optional tags.", example = "{\"source\":\"IOTBROKER\", \"key\": \"value\"}", required = false) @RequestParam(name = "tags", required = false) String tags) {

		final SSAPMessage<SSAPBodyUpdateMessage> request = new SSAPMessage<>();
		request.setBody(new SSAPBodyUpdateMessage());

		if (null != transactionId && transactionId.length() > 0) {
			request.setTransactionId(transactionId);
		}

		request.setDirection(SSAPMessageDirection.REQUEST);
		request.setMessageType(SSAPMessageTypes.UPDATE);
		request.setSessionKey(sessionKey);
		request.getBody().setOntology(ontology);
		request.getBody().setQuery(query);
		request.getBody().setTags(tags);

		try {
			request.setIncludeIds(Boolean.parseBoolean(ids));
		} catch (final Exception e) {
			request.setIncludeIds(false);
		}

		final SSAPMessage<SSAPBodyReturnMessage> response = processor.process(request, getGatewayInfo());
		if (!SSAPMessageDirection.ERROR.equals(response.getDirection())) {
			return new ResponseEntity<>(response.getBody().getData(), HttpStatus.OK);
		} else {
			return formResponseError(response);
		}
	}

	@Operation(summary = "Delete a instance or instances of a ontology with a remove query", security = {
			@SecurityRequirement(name = "session-key") })
	@GetMapping(value = "/ontology/{ontology}/delete")
	public ResponseEntity<?> deleteByQuery(
			@Parameter(description = "SessionKey provided from join operation", hidden = true) @RequestHeader(value = "Authorization") String sessionKey,
			@Parameter(description = "TransactionId provided from start transaction operation", required = false) @RequestHeader(value = "TransactionId", required = false) String transactionId,
			@Parameter(description = "Ontology to perform operation. Client platform must have granted permissions ", required = true) @PathVariable("ontology") String ontology,
			@Parameter(description = "Examples: NATIVE: db.temperature.update({\"value\":15})", required = true) @RequestParam(name = "query") String query,
			@Parameter(description = "Response Will include modidified Ids", required = false) @RequestParam(name = "ids", required = false, defaultValue = "false") String ids,
			@Parameter(description = "Key-value optional tags.", example = "{\"source\":\"IOTBROKER\", \"key\": \"value\"}", required = false) @RequestParam(name = "tags", required = false) String tags)
			throws UnsupportedEncodingException {

		final SSAPMessage<SSAPBodyDeleteMessage> request = new SSAPMessage<>();
		request.setBody(new SSAPBodyDeleteMessage());

		if (null != transactionId && transactionId.length() > 0) {
			request.setTransactionId(transactionId);
		}

		final String ontologyDecoded = URLDecoder.decode(ontology, StandardCharsets.UTF_8.name());
		final String queryDecoded = URLDecoder.decode(query, StandardCharsets.UTF_8.name());

		request.setDirection(SSAPMessageDirection.REQUEST);
		request.setMessageType(SSAPMessageTypes.DELETE);
		request.setSessionKey(sessionKey);
		request.getBody().setOntology(ontologyDecoded);
		request.getBody().setQuery(queryDecoded);
		request.getBody().setTags(tags);

		try {
			request.setIncludeIds(Boolean.parseBoolean(ids));
		} catch (final Exception e) {
			request.setIncludeIds(false);
		}

		final SSAPMessage<SSAPBodyReturnMessage> response = processor.process(request, getGatewayInfo());
		if (!SSAPMessageDirection.ERROR.equals(response.getDirection())) {
			return new ResponseEntity<>(response.getBody().getData(), HttpStatus.OK);
		} else {
			return formResponseError(response);
		}
	}

	@Operation(summary = "Delete a instance of a ontology", security = { @SecurityRequirement(name = "session-key") })
	@DeleteMapping(value = "/ontology/{ontology}/{id}")
	public ResponseEntity<?> deleteById(
			@Parameter(description = "SessionKey provided from join operation", hidden = true) @RequestHeader(value = "Authorization") String sessionKey,
			@Parameter(description = "TransactionId provided from start transaction operation", required = false) @RequestHeader(value = "TransactionId", required = false) String transactionId,
			@Parameter(description = "Ontology to perform operation. Client platform must have granted permissions ", required = true) @PathVariable("ontology") String ontology,
			@Parameter(description = "Ontology identification to perform operation", required = true) @PathVariable("id") String id,
			@Parameter(description = "Response Will include modidified Ids", required = false) @RequestParam(name = "ids", required = false, defaultValue = "false") String ids,
			@Parameter(description = "Key-value optional tags.", example = "{\"source\":\"IOTBROKER\", \"key\": \"value\"}", required = false) @RequestParam(name = "tags", required = false) String tags) {

		final SSAPMessage<SSAPBodyDeleteByIdMessage> request = new SSAPMessage<>();
		request.setBody(new SSAPBodyDeleteByIdMessage());

		if (null != transactionId && transactionId.length() > 0) {
			request.setTransactionId(transactionId);
		}

		request.setDirection(SSAPMessageDirection.REQUEST);
		request.setMessageType(SSAPMessageTypes.DELETE_BY_ID);
		request.setSessionKey(sessionKey);
		request.getBody().setId(id);
		request.getBody().setOntology(ontology);
		request.getBody().setTags(tags);
		try {
			request.setIncludeIds(Boolean.parseBoolean(ids));
		} catch (final Exception e) {
			request.setIncludeIds(false);
		}

		final SSAPMessage<SSAPBodyReturnMessage> response = processor.process(request, getGatewayInfo());
		if (!SSAPMessageDirection.ERROR.equals(response.getDirection())) {
			return new ResponseEntity<>(response.getBody().getData(), HttpStatus.OK);
		} else {
			return formResponseError(response);
		}
	}

	@PostMapping(value = "/ontology/decrypt/{ontology}")
	@Operation(summary = "Decrypts ontology data", security = { @SecurityRequirement(name = "session-key") })
	public ResponseEntity<?> decryptById(
			@Parameter(description = "SessionKey provided from join operation", hidden = true) @RequestHeader(value = "Authorization") String sessionKey,
			@Parameter(description = "Ontology to perform operation. Client platform must have granted permissions ", required = true) @PathVariable("ontology") String ontology,
			@Parameter(description = "Json data representing ontology instance", required = true) @RequestBody String data) {

		final Optional<IoTSession> session = securityPluginManager.getSession(sessionKey);

		if (session.isPresent()) {
			final String user = session.get().getUserID();

			String decryptedData = null;
			try {
				decryptedData = ontologyDataService.decrypt(data, ontology, user);
			} catch (final OntologyDataUnauthorizedException e) {
				return new ResponseEntity<>("Only the ontology owner can decrypt data", HttpStatus.UNAUTHORIZED);
			} catch (final OntologyDataJsonProblemException e) {
				return new ResponseEntity<>("Error processing data", HttpStatus.INTERNAL_SERVER_ERROR);
			}

			if (decryptedData != null) {
				return new ResponseEntity<>(decryptedData, HttpStatus.OK);
			} else {
				return new ResponseEntity<>("Error", HttpStatus.BAD_REQUEST);
			}
		} else {
			return new ResponseEntity<>("A valid user is necessary", HttpStatus.UNAUTHORIZED);
		}
	}

	@Operation(summary = "Start a Transaction", security = { @SecurityRequirement(name = "session-key") })
	@GetMapping(value = "/transaction/start")
	public ResponseEntity<?> startTx(
			@Parameter(description = "SessionKey provided from join operation", hidden = true) @RequestHeader(value = "Authorization") String sessionKey,
			@Parameter(description = "Key-value optional tags.", example = "{\"source\":\"IOTBROKER\", \"key\": \"value\"}", required = false) @RequestParam(name = "tags", required = false) String tags) {

		final SSAPMessage<SSAPBodyEmptySessionMandatoryMessage> request = new SSAPMessage<>();
		request.setBody(new SSAPBodyEmptySessionMandatoryMessage());

		request.setDirection(SSAPMessageDirection.REQUEST);
		request.setMessageType(SSAPMessageTypes.START_TRANSACTION);
		request.setSessionKey(sessionKey);
		request.getBody().setTags(tags);

		final SSAPMessage<SSAPBodyReturnMessage> response = processor.process(request, getGatewayInfo());
		if (!SSAPMessageDirection.ERROR.equals(response.getDirection())) {
			return new ResponseEntity<>(response.getBody().getData(), HttpStatus.OK);
		} else {
			return formResponseError(response);
		}

	}

	@Operation(summary = "Commit a Transaction", security = { @SecurityRequirement(name = "session-key") })
	@GetMapping(value = "/transaction/commit/{transactionId}")
	public ResponseEntity<?> commitTx(
			@Parameter(description = "SessionKey provided from join operation", hidden = true) @RequestHeader(value = "Authorization") String sessionKey,
			@Parameter(description = "Lock ontologies during the transaction", required = false) @RequestHeader(value = "LockOntologies", defaultValue = "false") Boolean lockOntologies,
			@Parameter(description = "Transaction Identifier", required = true) @PathVariable("transactionId") String transactionId,
			@Parameter(description = "Key-value optional tags.", example = "{\"source\":\"IOTBROKER\", \"key\": \"value\"}", required = false) @RequestParam(name = "tags", required = false) String tags) {

		final SSAPMessage<SSAPBodyCommitTransactionMessage> request = new SSAPMessage<>();
		final SSAPBodyCommitTransactionMessage commitBody = new SSAPBodyCommitTransactionMessage();
		commitBody.setLockOntologies(lockOntologies);
		commitBody.setTags(tags);

		request.setBody(commitBody);

		request.setTransactionId(transactionId);

		request.setDirection(SSAPMessageDirection.REQUEST);
		request.setMessageType(SSAPMessageTypes.COMMIT_TRANSACTION);
		request.setSessionKey(sessionKey);

		final SSAPMessage<SSAPBodyReturnMessage> response = processor.process(request, getGatewayInfo());
		if (!SSAPMessageDirection.ERROR.equals(response.getDirection())) {
			return new ResponseEntity<>(response.getBody().getData(), HttpStatus.OK);
		} else {
			return formResponseError(response);
		}
	}

	@Operation(summary = "Rollback a Transaction", security = { @SecurityRequirement(name = "session-key") })
	@GetMapping(value = "/transaction/rollback/{transactionId}")
	public ResponseEntity<?> rollbackTx(
			@Parameter(description = "SessionKey provided from join operation", hidden = true) @RequestHeader(value = "Authorization") String sessionKey,
			@Parameter(description = "Transaction Identifier", required = true) @PathVariable("transactionId") String transactionId,
			@Parameter(description = "Key-value optional tags.", example = "{\"source\":\"IOTBROKER\", \"key\": \"value\"}", required = false) @RequestParam(name = "tags", required = false) String tags) {

		final SSAPMessage<SSAPBodyEmptyMessage> request = new SSAPMessage<>();
		request.setBody(new SSAPBodyEmptyMessage());
		request.setTransactionId(transactionId);
		request.getBody().setTags(tags);

		request.setDirection(SSAPMessageDirection.REQUEST);
		request.setMessageType(SSAPMessageTypes.ROLLBACK_TRANSACTION);
		request.setSessionKey(sessionKey);

		final SSAPMessage<SSAPBodyReturnMessage> response = processor.process(request, getGatewayInfo());
		if (!SSAPMessageDirection.ERROR.equals(response.getDirection())) {
			return new ResponseEntity<>(response.getBody().getData(), HttpStatus.OK);
		} else {
			return formResponseError(response);
		}
	}

	@Operation(summary = "Subscribe by subscription. The subscription have to be defined in controlpanel", security = {
			@SecurityRequirement(name = "session-key") })
	@PostMapping(value = "/subscribe/{subscription}")
	public ResponseEntity<?> subscribe(
			@Parameter(description = "SessionKey provided from join operation", hidden = true) @RequestHeader(value = "Authorization") String sessionKey,
			@Parameter(description = "Subscription to perform the operation. Client platform must have granted permissions ", required = true) @PathVariable("subscription") String subscription,
			@Parameter(description = "Value of the subscription ontology field", required = true) @RequestParam(name = "queryValue") String queryValue,
			@Parameter(description = "Endpoint where notifications will be sent", required = true) @RequestParam(name = "callback") String callback,
			@Parameter(description = "Key-value optional tags.", example = "{\"source\":\"IOTBROKER\", \"key\": \"value\"}", required = false) @RequestParam(name = "tags", required = false) String tags) {

		final SSAPMessage<SSAPBodyReturnMessage> response = processor.process(SSAPMessageGenerator
				.generateRequestSubscriptionMessage(subscription, queryValue, callback, sessionKey, subscription, tags),
				getGatewayInfo());
		if (!SSAPMessageDirection.ERROR.equals(response.getDirection())) {
			return new ResponseEntity<>(response.getBody().getData(), HttpStatus.OK);
		} else {
			return formResponseError(response);
		}

	}

	@Operation(summary = "Unsubscribe by subscriptionId.", security = { @SecurityRequirement(name = "session-key") })
	@PostMapping(value = "/unsubscribe/{subscriptionId}")
	public ResponseEntity<?> unsubscribe(
			@Parameter(description = "SessionKey provided from join operation", hidden = true) @RequestHeader(value = "Authorization") String sessionKey,
			@Parameter(description = "Subscription ID ", required = true) @PathVariable("subscriptionId") String subscriptionId,
			@Parameter(description = "Key-value optional tags.", example = "{\"source\":\"IOTBROKER\", \"key\": \"value\"}", required = false) @RequestParam(name = "tags", required = false) String tags) {

		final SSAPMessage<SSAPBodyReturnMessage> response = processor.process(
				SSAPMessageGenerator.generateRequestUnsubscribeMessage(sessionKey, subscriptionId, tags),
				getGatewayInfo());
		if (!SSAPMessageDirection.ERROR.equals(response.getDirection())) {
			return new ResponseEntity<>(response.getBody().getData(), HttpStatus.OK);
		} else {
			return formResponseError(response);
		}

	}

	private ResponseEntity<?> formResponseError(SSAPMessage<SSAPBodyReturnMessage> response) {
		final SSAPErrorCode code = response.getBody().getErrorCode();
		HttpStatus status;

		switch (code) {
		case AUTENTICATION:
			status = HttpStatus.UNAUTHORIZED;
			break;

		case AUTHORIZATION:
			status = HttpStatus.FORBIDDEN;
			break;

		case PROCESSOR:
			status = HttpStatus.BAD_REQUEST;
			break;

		default:
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			break;
		}

		return new ResponseEntity<>(response.getBody().getError(), status);
	}

	private GatewayInfo getGatewayInfo() {
		final GatewayInfo info = new GatewayInfo();
		info.setName("rest_gateway");
		info.setProtocol("REST");

		return info;
	}

	private SSAPMessage<SSAPBodyReturnMessage> checkVerticalAndTenant(String token, String vertical, String tenant) {
		final MasterDeviceToken deviceToken = multitenancyService.getMasterDeviceToken(token);
		SSAPMessage<SSAPBodyReturnMessage> errorResponse = null;
		// IF VRTICAL AND TENANT ARE EMPTY, they correspond to the defaults
		if (tenant == null || tenant.isEmpty()) {
			tenant = Tenant2SchemaMapper.defaultTenantName(Tenant2SchemaMapper.DEFAULT_VERTICAL_NAME);
		}
		if (vertical == null || vertical.isEmpty()) {
			vertical = Tenant2SchemaMapper.DEFAULT_VERTICAL_NAME;
		}
		String tokenVerticalName = "";
		if (deviceToken != null) {
			for (final Vertical v : multitenancyService.getAllVerticals()) {
				if (v.getSchema().equals(deviceToken.getVerticalSchema())) {
					tokenVerticalName = v.getName();
					break;
				}
			}
		}
		// CHECK VERTICAL AND TENANT OF TOKEN
		if (deviceToken == null || !deviceToken.getTenant().equals(tenant) || !tokenVerticalName.equals(vertical)) {

			errorResponse = new SSAPMessage<>();
			errorResponse.setMessageType(SSAPMessageTypes.JOIN);
			errorResponse.setDirection(SSAPMessageDirection.ERROR);
			final SSAPBodyReturnMessage body = new SSAPBodyReturnMessage();
			body.setErrorCode(SSAPErrorCode.AUTHORIZATION);
			body.setError("Unauthorized access to vertical/tenant for this client/token.");
			body.setOk(false);
			errorResponse.setBody(body);

		}
		return errorResponse;
	}

}
