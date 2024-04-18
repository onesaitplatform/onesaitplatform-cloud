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
package com.minsait.onesait.platform.bpm.rest;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.bpm.services.AuthorizationManagementService;
import com.minsait.onesait.platform.bpm.services.BPMUserManagementService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("management")
@Slf4j
public class BPMManagementRestController {

	@Autowired
	private AuthorizationManagementService authorizationManagementService;
	@Autowired
	private BPMUserManagementService userManagementService;

	@Deprecated
	@PostMapping("/authorizations/authorize/{userId}")
	public ResponseEntity<String> createAuth(@PathVariable("userId") String userId, Principal principal) {
		return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
	}

	@PostMapping("/authorizations/user/{userId}/tenant/{tenantId}")
	// TO-DO check administrator
	public ResponseEntity<String> authorizeUserOnTenant(@PathVariable("userId") String userId,
			@PathVariable("tenantId") String tenantId, Principal principal) {
		try {
			authorizationManagementService.authorizeUserOnTenat(userId, tenantId);
		} catch (final Exception e) {
			log.error("Could not create Authorization", e);
			return new ResponseEntity<>("Could not create Authorization, please review logs",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping("/tenants")
	public ResponseEntity<List<String>> getAllTenants() {
		return new ResponseEntity<>(authorizationManagementService.getAllTenants(), HttpStatus.OK);
	}

	@GetMapping("/tenants/user/{userId}")
	public ResponseEntity<List<String>> getTenants(@PathVariable("userId") String userId) {
		return new ResponseEntity<>(authorizationManagementService.getTenants(userId), HttpStatus.OK);
	}

	@GetMapping("/tenants/{tenantId}/users")
	public ResponseEntity<List<String>> getUsersInTenant(@PathVariable("tenantId") String tenantId) {
		return new ResponseEntity<>(authorizationManagementService.getUsersInTenant(tenantId), HttpStatus.OK);
	}

	@PostMapping("/sync-users")
	public ResponseEntity<String> syncUsers() {
		try {
			userManagementService.syncUsers();
		} catch (final Exception e) {
			log.error("Could not sync users", e);
			return new ResponseEntity<>("Could not sync users, please review logs", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
