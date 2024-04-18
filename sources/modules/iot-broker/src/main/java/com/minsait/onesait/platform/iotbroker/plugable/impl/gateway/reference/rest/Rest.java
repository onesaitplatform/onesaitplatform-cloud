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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyDeleteByIdMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyDeleteMessage;
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
import com.minsait.onesait.platform.config.model.IoTSession;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataJsonProblemException;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataService;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataUnauthorizedException;
import com.minsait.onesait.platform.interceptor.CorrelationInterceptor;
import com.minsait.onesait.platform.iotbroker.plugable.impl.security.SecurityPluginManager;
import com.minsait.onesait.platform.iotbroker.plugable.interfaces.gateway.GatewayInfo;
import com.minsait.onesait.platform.iotbroker.processor.GatewayNotifier;
import com.minsait.onesait.platform.iotbroker.processor.MessageProcessor;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
@Api(value = "rest", description = "onesait Platform operations for devices")
public class Rest extends WebMvcConfigurerAdapter {

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
	private CorrelationInterceptor logInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(logInterceptor);
	}

	@ApiOperation(value = "Logs a client device into onesaitPlatform with token.\nReturns a sessionKey to use in further operations")
	@RequestMapping(value = "/client/join", method = RequestMethod.GET)
	public ResponseEntity<?> join(
			@ApiParam(value = "Token asociated to client platform", required = true) @RequestParam(name = "token") String token,
			@ApiParam(value = "Client Platform asociated to token", required = true) @RequestParam(name = "clientPlatform") String clientPlatform,
			@ApiParam(value = "Desired ClientPlatform id. the value is chosen from user", required = true) @RequestParam(name = "clientPlatformId") String clientPlatformId) {

		final SSAPMessage<SSAPBodyJoinMessage> request = new SSAPMessage<>();
		request.setBody(new SSAPBodyJoinMessage());

		request.setDirection(SSAPMessageDirection.REQUEST);
		request.setMessageType(SSAPMessageTypes.JOIN);
		// request.setSessionKey();
		request.getBody().setToken(token);
		request.getBody().setDeviceTemplate(clientPlatform);
		request.getBody().setDevice(clientPlatformId);

		final SSAPMessage<SSAPBodyReturnMessage> response = processor.process(request, getGatewayInfo());
		if (!SSAPMessageDirection.ERROR.equals(response.getDirection())) {
			return new ResponseEntity<>(response.getBody().getData(), HttpStatus.OK);
		} else {
			return formResponseError(response);
		}
	}

	@ApiOperation(value = "Logs out a client device into onesaitPlatform with token")
	@RequestMapping(value = "/client/leave", method = RequestMethod.GET)
	public ResponseEntity<?> leave(
			@ApiParam(value = "SessionKey provided from join operation", required = true) @RequestHeader(value = "Authorization") String sessionKey) {

		final SSAPMessage<SSAPBodyLeaveMessage> request = new SSAPMessage<>();
		request.setBody(new SSAPBodyLeaveMessage());

		request.setDirection(SSAPMessageDirection.REQUEST);
		request.setMessageType(SSAPMessageTypes.LEAVE);
		request.setSessionKey(sessionKey);

		final SSAPMessage<SSAPBodyReturnMessage> response = processor.process(request, getGatewayInfo());
		if (!SSAPMessageDirection.ERROR.equals(response.getDirection())) {
			return new ResponseEntity<>(response.getBody().getData(), HttpStatus.OK);
		} else {
			return formResponseError(response);
		}
	}

	@ApiOperation(value = "Get a list of instances of a ontology data")
	@RequestMapping(value = "/ontology/{ontology}", method = RequestMethod.GET)
	public ResponseEntity<?> list(
			@ApiParam(value = "SessionKey provided from join operation", required = true) @RequestHeader(value = "Authorization") String sessionKey,
			@ApiParam(value = "Ontology to perform operation. Client platform must have granted permissions ", required = true) @PathVariable("ontology") String ontology,
			@ApiParam(value = "Examples:\n\tNATIVE: db.temperature.find({})\n\tSQL: select * from temperature; ", required = true) @RequestParam(name = "query") String query,
			@ApiParam(value = "OPTIONS: NATIVE or SQL", required = true) @RequestParam(name = "queryType") SSAPQueryType queryType) {

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

		final SSAPMessage<SSAPBodyReturnMessage> response = processor.process(request, getGatewayInfo());
		if (!SSAPMessageDirection.ERROR.equals(response.getDirection())) {
			return new ResponseEntity<>(response.getBody().getData(), HttpStatus.OK);
		} else {
			return formResponseError(response);
		}
	}

	@ApiOperation(value = "Inserts a instance of a ontology expresed in json format")
	@RequestMapping(value = "/ontology/{ontology}", method = RequestMethod.POST)
	public ResponseEntity<?> create(
			@ApiParam(value = "SessionKey provided from join operation", required = true) @RequestHeader(value = "Authorization") String sessionKey,
			@ApiParam(value = "Ontology to perform operation. Client platform must have granted permissions ", required = true) @PathVariable("ontology") String ontology,
			@ApiParam(value = "Json data representing ontology instance", required = true) @RequestBody JsonNode data) {

		final SSAPMessage<SSAPBodyInsertMessage> request = new SSAPMessage<>();
		request.setBody(new SSAPBodyInsertMessage());

		request.setDirection(SSAPMessageDirection.REQUEST);
		request.setMessageType(SSAPMessageTypes.INSERT);
		request.setSessionKey(sessionKey);
		request.getBody().setOntology(ontology);
		request.getBody().setData(data);

		final SSAPMessage<SSAPBodyReturnMessage> response = processor.process(request, getGatewayInfo());
		if (!SSAPMessageDirection.ERROR.equals(response.getDirection())) {
			return new ResponseEntity<>(response.getBody().getData(), HttpStatus.OK);
		} else {
			return formResponseError(response);
		}
	}

	@ApiOperation(value = "Updates a instance of a ontology expresed in json format")
	@RequestMapping(value = "/ontology/{ontology}/{id}", method = RequestMethod.PUT)
	public ResponseEntity<?> updateById(
			@ApiParam(value = "SessionKey provided from join operation", required = true) @RequestHeader(value = "Authorization") String sessionKey,
			@ApiParam(value = "Ontology to perform operation. Client platform must have granted permissions ", required = true) @PathVariable("ontology") String ontology,
			@ApiParam(value = "Ontology identification to perform operation", required = true) @PathVariable("id") String id,
			@ApiParam(value = "Json data representing ontology instance", required = true) @RequestBody JsonNode data,
			@ApiParam(value = "Response Will include modidified Ids", required = false) @RequestParam(name = "ids", required = false, defaultValue = "false") String ids) {

		final SSAPMessage<SSAPBodyUpdateByIdMessage> request = new SSAPMessage<>();
		request.setBody(new SSAPBodyUpdateByIdMessage());

		request.setDirection(SSAPMessageDirection.REQUEST);
		request.setMessageType(SSAPMessageTypes.UPDATE_BY_ID);
		request.setSessionKey(sessionKey);
		request.getBody().setId(id);
		request.getBody().setOntology(ontology);
		request.getBody().setData(data);
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

	//Use put operation instad this one
	@Deprecated
	@ApiOperation(value = "Updates a instance or instances of a ontology with a update query")	
	@RequestMapping(value = "/ontology/{ontology}/update", method = RequestMethod.GET)
	public ResponseEntity<?> updateByQuery(
			@ApiParam(value = "SessionKey provided from join operation", required = true) @RequestHeader(value = "Authorization") String sessionKey,
			@ApiParam(value = "Ontology to perform operation. Client platform must have granted permissions ", required = true) @PathVariable("ontology") String ontology,
			@ApiParam(value = "Examples: NATIVE: db.temperature.update({\"location\":\"Helsinki\"}, { $set:{\"value\":15}})", required = true) @RequestParam(name = "query") String query,
			@ApiParam(value = "Response Will include modidified Ids", required = false) @RequestParam(name = "ids", required = false, defaultValue = "false") String ids) {

		final SSAPMessage<SSAPBodyUpdateMessage> request = new SSAPMessage<>();
		request.setBody(new SSAPBodyUpdateMessage());

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

	@ApiOperation(value = "Updates a instance or instances of a ontology with a update query in body")
	@RequestMapping(value = "/ontology/{ontology}/update", method = RequestMethod.PUT)
	public ResponseEntity<?> updateByQueryBody(
			@ApiParam(value = "SessionKey provided from join operation", required = true) @RequestHeader(value = "Authorization") String sessionKey,
			@ApiParam(value = "Ontology to perform operation. Client platform must have granted permissions ", required = true) @PathVariable("ontology") String ontology,
			@ApiParam(value = "Examples: NATIVE: db.temperature.update({\"location\":\"Helsinki\"}, { $set:{\"value\":15}})", required = true, example = "") @RequestBody String query,
			@ApiParam(value = "Response Will include modidified Ids", required = false) @RequestParam(name = "ids", required = false, defaultValue = "false") String ids) {

		final SSAPMessage<SSAPBodyUpdateMessage> request = new SSAPMessage<>();
		request.setBody(new SSAPBodyUpdateMessage());

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

	@ApiOperation(value = "Delete a instance or instances of a ontology with a remove query")
	@RequestMapping(value = "/ontology/{ontology}/delete", method = RequestMethod.GET)
	public ResponseEntity<?> deleteByQuery(
			@ApiParam(value = "SessionKey provided from join operation", required = true) @RequestHeader(value = "Authorization") String sessionKey,
			@ApiParam(value = "Ontology to perform operation. Client platform must have granted permissions ", required = true) @PathVariable("ontology") String ontology,
			@ApiParam(value = "Examples: NATIVE: db.temperature.update({\"value\":15})", required = true) @RequestParam(name = "query") String query,
			@ApiParam(value = "Response Will include modidified Ids", required = false) @RequestParam(name = "ids", required = false, defaultValue = "false") String ids)
			throws UnsupportedEncodingException {

		final SSAPMessage<SSAPBodyDeleteMessage> request = new SSAPMessage<>();
		request.setBody(new SSAPBodyDeleteMessage());

		String ontologyDecoded = URLDecoder.decode(ontology, StandardCharsets.UTF_8.name());
		String queryDecoded = URLDecoder.decode(query, StandardCharsets.UTF_8.name());

		request.setDirection(SSAPMessageDirection.REQUEST);
		request.setMessageType(SSAPMessageTypes.DELETE);
		request.setSessionKey(sessionKey);
		request.getBody().setOntology(ontologyDecoded);
		request.getBody().setQuery(queryDecoded);

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

	@ApiOperation(value = "Delete a instance of a ontology")
	@RequestMapping(value = "/ontology/{ontology}/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteById(
			@ApiParam(value = "SessionKey provided from join operation", required = true) @RequestHeader(value = "Authorization") String sessionKey,
			@ApiParam(value = "Ontology to perform operation. Client platform must have granted permissions ", required = true) @PathVariable("ontology") String ontology,
			@ApiParam(value = "Ontology identification to perform operation", required = true) @PathVariable("id") String id,
			@ApiParam(value = "Response Will include modidified Ids", required = false) @RequestParam(name = "ids", required = false, defaultValue = "false") String ids) {

		final SSAPMessage<SSAPBodyDeleteByIdMessage> request = new SSAPMessage<>();
		request.setBody(new SSAPBodyDeleteByIdMessage());

		request.setDirection(SSAPMessageDirection.REQUEST);
		request.setMessageType(SSAPMessageTypes.DELETE_BY_ID);
		request.setSessionKey(sessionKey);
		request.getBody().setId(id);
		request.getBody().setOntology(ontology);
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

	@RequestMapping(value = "/ontology/decrypt/{ontology}", method = RequestMethod.POST)
	public ResponseEntity<?> decryptById(
			@ApiParam(value = "SessionKey provided from join operation", required = true) @RequestHeader(value = "Authorization") String sessionKey,
			@ApiParam(value = "Ontology to perform operation. Client platform must have granted permissions ", required = true) @PathVariable("ontology") String ontology,
			@ApiParam(value = "Json data representing ontology instance", required = true) @RequestBody String data) {

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

}
