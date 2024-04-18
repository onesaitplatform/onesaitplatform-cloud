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
package com.minsait.onesait.platform.controlpanel.rest.management.audit;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.audit.bean.OPAuditEvent;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.EventType;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.Module;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.OperationType;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.ResultOperationType;
import com.minsait.onesait.platform.audit.bean.OPEventFactory;
import com.minsait.onesait.platform.audit.notify.EventRouter;
import com.minsait.onesait.platform.business.services.audit.AuditService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "Audit Management")
@RestController
@RequestMapping("api/audit")
@ApiResponses({ @ApiResponse(responseCode = "400", description = "Bad request"),
	@ApiResponse(responseCode = "500", description = "Internal server error"), @ApiResponse(responseCode = "403", description = "Forbidden") })
public class AuditRestController {
	@Autowired
	private AuditService auditService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	EventRouter eventRouter;

	@Operation(summary = "Get User Audit data")
	@GetMapping("/")
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=String.class)), responseCode = "200", description = "OK"))
	@Transactional
	public ResponseEntity<?> getAudit(
			@Parameter(description= "Result Type", required = false) @RequestParam(value = "resultType", required = false, defaultValue = "all") String resultType,
			@Parameter(description= "Module Name", required = false) @RequestParam(value = "modulesname", required = false, defaultValue = "all") String modulesname,
			@Parameter(description= "Operation", required = false) @RequestParam(value = "operation", required = false, defaultValue = "all") String operation,
			@Parameter(description= "Number of Records", required = false) @RequestParam(value = "nrecords", required = false, defaultValue = "") String nrecords,
			@Parameter(description= "User", required = false) @RequestParam(value = "user", required = false, defaultValue = "") String user) {

		String userQuery;
		if (utils.isAdministrator()) {
			userQuery = user;
		} else {
			userQuery = utils.getUserId();
		}
		String error="";
		try {
			if (resultType!=null && !resultType.equals("") && !resultType.equalsIgnoreCase("ALL")) {
				error="Error in parameters: Incorrect Result Type";
				ResultOperationType.valueOf(resultType);
			}

			if (modulesname!=null && !modulesname.equals("") && !modulesname.equalsIgnoreCase("ALL")) {
				error="Error in parameters: Incorrect Module Name";
				Module.valueOf(modulesname);
			}

			if (operation!=null && !operation.equals("") && !operation.equalsIgnoreCase("ALL")) {
				error="Error in parameters: Incorrect Operation Type";
				OperationType.valueOf(operation);
			}
		} catch (Exception e) {
			log.error("Error: " + error, utils.getUserId());
			return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
		}

		String queryResult="{}";
		try {
			queryResult = auditService.getUserAuditData(resultType, modulesname, operation, nrecords, userQuery);
		} catch (final Exception e) {
			log.error("Error getting audit of user {}", utils.getUserId());
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(queryResult, HttpStatus.OK);
	}


	@Operation(summary = "Inserts Audit user info")
	@PostMapping(value = "/")
	public ResponseEntity<?> insertauditdata(@Valid @RequestBody List<OPAuditEventDTO> userAuditEvent) {

		final List<OPAuditEvent> auditEventList = fromAuditEventDTOtoAuditEvent(userAuditEvent);
		for (final OPAuditEvent opAuditEvent : auditEventList) {
			eventRouter.notify(opAuditEvent.toJson());
		}

		return new ResponseEntity<>("", HttpStatus.OK);
	}


	private List<OPAuditEvent> fromAuditEventDTOtoAuditEvent(List<OPAuditEventDTO> userAuditEventList) {
		final String user = utils.getUserId();
		final List<OPAuditEvent> opAuditEventList = new ArrayList<OPAuditEvent>();
		for (final OPAuditEventDTO opAuditEventDTO : userAuditEventList) {
			final OPAuditEvent auditevent = OPEventFactory.builder().build().createAuditEvent(EventType.SECURITY,
					"Logout Success for user: ");
			if (opAuditEventDTO.getMessage()!=null && !opAuditEventDTO.getMessage().equals("")) {
				auditevent.setMessage(opAuditEventDTO.getMessage());
			}
			if (opAuditEventDTO.getType()!=null && !opAuditEventDTO.getType().name().equals("") && OPAuditEvent.EventType.valueOf(opAuditEventDTO.getType().name())!=null) {
				auditevent.setType(OPAuditEvent.EventType.valueOf(opAuditEventDTO.getType().name()));
			}
			if (opAuditEventDTO.getMessage()!=null && !opAuditEventDTO.getMessage().equals("")) {
				auditevent.setTimeStamp(opAuditEventDTO.getTimeStamp());
			}
			if (opAuditEventDTO.getFormatedTimeStamp()!=null && !opAuditEventDTO.getFormatedTimeStamp().equals("")) {
				auditevent.setFormatedTimeStamp(opAuditEventDTO.getFormatedTimeStamp());
			}
			if (opAuditEventDTO.getOperationType()!=null && !opAuditEventDTO.getOperationType().equals("")) {
				auditevent.setOperationType(opAuditEventDTO.getOperationType());
			}
			if (opAuditEventDTO.getOtherType()!=null && !opAuditEventDTO.getOtherType().equals("")) {
				auditevent.setOtherType(opAuditEventDTO.getOtherType());
			}
			if (opAuditEventDTO.getResultOperation()!=null && !opAuditEventDTO.getResultOperation().name().equals("") && OPAuditEvent.ResultOperationType.valueOf(opAuditEventDTO.getResultOperation().name())!=null) {
				auditevent.setResultOperation(OPAuditEvent.ResultOperationType.valueOf(opAuditEventDTO.getResultOperation().name()));
			}
			if (opAuditEventDTO.getOntology()!=null && opAuditEventDTO.getOntology().equals("")) {
				auditevent.setOntology(opAuditEventDTO.getOntology());
			}

			auditevent.setUser(user);




			opAuditEventList.add(auditevent);
		}
		return opAuditEventList;
	}
}
