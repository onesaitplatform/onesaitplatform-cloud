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
package com.minsait.onesait.platform.controlpanel.rest.management.multitenant;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.multitenant.config.model.MasterUser;
import com.minsait.onesait.platform.multitenant.config.model.MasterUserToken;
import com.minsait.onesait.platform.multitenant.config.model.Vertical;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("api/multitenant")
@PreAuthorize("hasRole('ROLE_ADMINISTRATOR') or hasRole('ROLE_PLATFORM_ADMIN')")
public class MultitenantRestService {

	@Autowired
	private MultitenancyService multitenancyService;

	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = String[].class))
	@ApiOperation("Get Verticals")
	@GetMapping("/verticals")
	public ResponseEntity<List<String>> verticals() {
		final List<Vertical> vertical = multitenancyService.getAllVerticals();
		return new ResponseEntity<>(vertical.stream().map(Vertical::getName).collect(Collectors.toList()),
				HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = Map.class))
	@ApiOperation("Get Admin token for each vertical for management purposes")
	@GetMapping("/vertical-tokens")
	@PreAuthorize("hasRole('ROLE_PLATFORM_ADMIN')")
	public ResponseEntity<Map<String, String>> verticalManagementTokens() {
		final Map<String, String> tokens = multitenancyService.getAdminTokensForVerticals().stream()
				.collect(Collectors.toMap(m -> m.getVertical().getName(), MasterUserToken::getToken, (e1, e2) -> e1));
		return new ResponseEntity<>(tokens, HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = String.class))
	@ApiOperation("User exists for management purposes")
	@GetMapping("/users/{userId}/exists")
	public ResponseEntity<String> userExists(@PathVariable("userId") String userId) {
		final MasterUser user = multitenancyService.getUser(userId);
		if (user == null) {
			return ResponseEntity.notFound().build();
		} else {
			return ResponseEntity.ok().build();
		}
	}

}
