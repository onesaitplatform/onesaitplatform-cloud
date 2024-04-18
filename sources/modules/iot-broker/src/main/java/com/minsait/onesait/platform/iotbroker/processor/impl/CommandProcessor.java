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
package com.minsait.onesait.platform.iotbroker.processor.impl;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyCommandMessage;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageDirection;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageTypes;
import com.minsait.onesait.platform.iotbroker.plugable.impl.security.SecurityPluginManager;
import com.minsait.onesait.platform.iotbroker.processor.GatewayNotifier;
import com.minsait.onesait.platform.multitenant.config.model.IoTSession;

@RestController
@RequestMapping(path = "/")
@EnableAutoConfiguration
@CrossOrigin(origins = "*")
public class CommandProcessor {

	@Autowired
	GatewayNotifier notifier;
	@Autowired
	ObjectMapper mapper;
	@Autowired
	SecurityPluginManager securityPluginManager;

	@RequestMapping(value = "/commandAsync/{command}", method = RequestMethod.POST)
	public boolean sendAsync(@PathVariable(name = "command") String command,
			@RequestHeader(value = "Authorization", required = true) String sessionKey, @RequestBody JsonNode params) {

		Optional<IoTSession> session = securityPluginManager.getSession(sessionKey);
		
		if (this.securityPluginManager.checkSessionKeyActive(session)) {
			final SSAPMessage<SSAPBodyCommandMessage> cmd = new SSAPMessage<>();
			cmd.setBody(new SSAPBodyCommandMessage());
			cmd.setDirection(SSAPMessageDirection.REQUEST);
			cmd.setMessageType(SSAPMessageTypes.COMMAND);
			cmd.setSessionKey(sessionKey);
			cmd.getBody().setCommandId(UUID.randomUUID().toString());
			cmd.getBody().setCommand(command);
			cmd.getBody().setParams(params);

			notifier.sendCommandAsync(cmd);

			return true;
		} else
			return false;
	}

}
