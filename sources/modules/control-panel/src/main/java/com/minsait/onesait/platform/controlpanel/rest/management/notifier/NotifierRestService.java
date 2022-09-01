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
package com.minsait.onesait.platform.controlpanel.rest.management.notifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.router.service.app.model.NotificationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.QueryType;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.Source;
import com.minsait.onesait.platform.router.service.app.service.RouterService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Module Notifications")
@RestController
@ApiResponses({ @ApiResponse(responseCode = "400", description = "Bad request"),
	@ApiResponse(responseCode = "500", description = "Internal server error"), @ApiResponse(responseCode = "403", description = "Forbidden") })
@RequestMapping("api/notifier")
@Slf4j
public class NotifierRestService {

	@Autowired
	private AppWebUtils utils;
	@Autowired
	private RouterService routerService;

	@Operation(summary = "Notifies the operation to the semantic information broker")
	@PostMapping("/notify")
	public ResponseEntity<String> notifyToRouter(@RequestBody Notification notification) {
		final OperationModel.Builder model = new OperationModel.Builder(notification.getOntology(),
				OperationType.valueOf(notification.getOperation().name()), utils.getUserId(), Source.INTERNAL_ROUTER);
		if (!StringUtils.isEmpty(notification.getQuery())) {
			model.queryType(QueryType.valueOf(notification.getQueryType().name()));
			model.body(notification.getQuery());
		} else {
			model.body(notification.getPayload());
		}
		if (!StringUtils.isEmpty(notification.getId())) {
			model.objectId(notification.getId());
		}
		final NotificationModel modelNotification = new NotificationModel();
		modelNotification.setOperationModel(model.build());
		try {
			routerService.notifyModules(modelNotification);
		} catch (final Exception e) {
			log.error("Error while trying to notify operation to onesait modules", e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return ResponseEntity.ok().build();
	}
}
