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
package com.minsait.onesait.platform.controller;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.validation.Valid;
import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.minsait.onesait.platform.config.model.security.UserPrincipal;
import com.minsait.onesait.platform.dto.socket.InputMessage;
import com.minsait.onesait.platform.dto.socket.OutputMessage;
import com.minsait.onesait.platform.exception.DashboardEngineException;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.service.SolverService;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class SocketController {

	@Autowired
	private SimpMessagingTemplate simpMessagingTemplate;

	@Autowired
	private SolverService solverService;
	private static final String AUTH_VALUE_ANONYMOUS = "anonymous";

	@CrossOrigin
	@MessageMapping("/dsengine/solver/{id}")
	public void response(@DestinationVariable("id") Long id, SimpMessageHeaderAccessor headerAccessor,
			InputMessage msg) {
		simpMessagingTemplate.convertAndSend("/dsengine/broker/" + id, executeInputMsg(msg));
	}
	
	@RequestMapping(path = "/dsengine/rest/solver/{datasource}",
	        method = RequestMethod.POST,
	        produces = "application/json")
	    public @ResponseBody ResponseEntity<OutputMessage> restResponse(@PathParam("datasource") String datasource, @Valid @RequestBody final InputMessage msg) {
		OutputMessage out = executeInputMsg(msg);
		return new ResponseEntity<>(executeInputMsg(msg), HttpStatus.valueOf(out.getCode()));
	}
	
	private OutputMessage executeInputMsg(InputMessage msg) {
		final long startTime = System.currentTimeMillis();

		setMultitenantContext();

		log.info("Query for: " + msg.getDs() + ",  params: filter: " + msg.getFilter() + ", project: "
				+ msg.getProject() + ", group: " + msg.getGroup() + ", sort: " + msg.getSort() + ", offset: "
				+ msg.getOffset() + ", limit: " + msg.getLimit());

		String result;
		boolean error = false;
		int code;

		try {
			if (!msg.isDebug()) {
				result = solverService.solveDatasource(msg);
			} else {
				result = "{\"query\":\"" + solverService.explainDatasource(msg) + "\"}";
			}
			code = 200;
		} catch (final DashboardEngineException e) {
			error = true;
			result = e.getError() + ": " + e.getMessage();
			log.error(result);
			code = e.getError().getCode();
		} catch (final Exception e) {
			error = true;
			result = "General Exception: " + e.getMessage();
			log.error(result);
			code = 500;
		}

		log.info("Query for: " + msg.getDs() + ",  params: filter: " + msg.getFilter() + ", project: "
				+ msg.getProject() + ", group: " + msg.getGroup() + ", sort: " + msg.getSort() + ", offset: "
				+ msg.getOffset() + ", limit: " + msg.getLimit() + " executed in "
				+ (System.currentTimeMillis() - startTime) / 1000f + "(s)");

		return new OutputMessage(result,
				new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date()), startTime, error, code);
	}

	private void setMultitenantContext() {
		final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && !auth.getPrincipal().equals(AUTH_VALUE_ANONYMOUS)) {
			MultitenancyContextHolder.setVerticalSchema(((UserPrincipal) auth.getPrincipal()).getVerticalSchema());
			MultitenancyContextHolder.setTenantName(((UserPrincipal) auth.getPrincipal()).getTenant());
		}
	}

}
