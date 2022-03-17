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
package com.minsait.onesait.platform.controlpanel.rest.management.audit;


import java.util.ArrayList;
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
import com.minsait.onesait.platform.audit.bean.OPEventFactory;
import com.minsait.onesait.platform.audit.notify.EventRouter;
import com.minsait.onesait.platform.business.services.audit.AuditService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Api(value = "Audit Management", tags = { "Audit management service" })
@RestController
@RequestMapping("api/audit")
@ApiResponses({ @ApiResponse(code = 400, message = "Bad request"),
		@ApiResponse(code = 500, message = "Internal server error"), @ApiResponse(code = 403, message = "Forbidden") })
public class AuditRestController {
	@Autowired
	private AuditService auditService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	EventRouter eventRouter;
	
	@ApiOperation(value = "Get User Audit data")
	@GetMapping("/")
	@ApiResponses(@ApiResponse(response = String.class, code = 200, message = "OK"))
	@Transactional
	public ResponseEntity<?> getAudit(
		@ApiParam(value = "Result Type", required = false) @RequestParam(value = "resultType", required = false, defaultValue = "all") String resultType,
		@ApiParam(value = "Module Name", required = false) @RequestParam(value = "modulesname", required = false, defaultValue = "all") String modulesname,
		@ApiParam(value = "Operation", required = false) @RequestParam(value = "operation", required = false, defaultValue = "all") String operation,
		@ApiParam(value = "Number of Records", required = false) @RequestParam(value = "nrecords", required = false, defaultValue = "") String nrecords,
		@ApiParam(value = "User", required = false) @RequestParam(value = "user", required = false, defaultValue = "") String user) {
		
		String userQuery;
		if (utils.getRole().equals("ROLE_ADMINISTRATOR")) {
			userQuery = user;
		} else {
			userQuery = utils.getUserId();
		}
		String queryResult="{}";
		try {
			queryResult = auditService.getUserAuditData(resultType, modulesname, operation, nrecords, userQuery);
		} catch (final Exception e) {
			log.error("Error getting audit of user {}", utils.getUserId());
		}

		return new ResponseEntity<>(queryResult, HttpStatus.OK);
	}
	
	
	@ApiOperation(value = "Inserts Audit user info")
	@PostMapping(value = "/")
	public ResponseEntity<?> insertauditdata(@Valid @RequestBody List<OPAuditEventDTO> userAuditEvent) {
		
		List<OPAuditEvent> auditEventList = fromAuditEventDTOtoAuditEvent(userAuditEvent);
		for (OPAuditEvent opAuditEvent : auditEventList) {
			eventRouter.notify(opAuditEvent.toJson());
		}
		
		return new ResponseEntity<>("", HttpStatus.OK);
	}


	private List<OPAuditEvent> fromAuditEventDTOtoAuditEvent(List<OPAuditEventDTO> userAuditEventList) {
		String user = utils.getUserId();
		List<OPAuditEvent> opAuditEventList = new ArrayList<OPAuditEvent>();
		for (OPAuditEventDTO opAuditEventDTO : userAuditEventList) {
			OPAuditEvent auditevent = OPEventFactory.builder().build().createAuditEvent(EventType.SECURITY,
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
