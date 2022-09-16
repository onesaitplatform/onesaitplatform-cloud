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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.security.UserPrincipal;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.config.model.MasterUser;
import com.minsait.onesait.platform.multitenant.config.model.MasterUserToken;
import com.minsait.onesait.platform.multitenant.config.model.Vertical;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("api/multitenant")
public class MultitenantRestService {

	@Autowired
	private MultitenancyService multitenancyService;
	@Autowired
	private UserService userService;


	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content=@Content(schema=@Schema(implementation=String[].class))))
	@Operation(summary="Get Verticals")
	@GetMapping("/verticals")
	@PreAuthorize("hasRole('ROLE_ADMINISTRATOR') or hasRole('ROLE_PLATFORM_ADMIN')")
	public ResponseEntity<List<String>> verticals() {
		final List<Vertical> vertical = multitenancyService.getAllVerticals();
		return new ResponseEntity<>(vertical.stream().map(Vertical::getName).collect(Collectors.toList()),
				HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content=@Content(schema=@Schema(implementation=Map.class))))
	@Operation(summary="Get Admin token for each vertical for management purposes")
	@GetMapping("/vertical-tokens")
	@PreAuthorize("hasRole('ROLE_PLATFORM_ADMIN')")
	public ResponseEntity<Map<String, String>> verticalManagementTokens() {
		final Map<String, String> tokens = multitenancyService.getAdminTokensForVerticals().stream()
				.collect(Collectors.toMap(m -> m.getVertical().getName(), MasterUserToken::getToken, (e1, e2) -> e1));
		return new ResponseEntity<>(tokens, HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content=@Content(schema=@Schema(implementation=String.class))))
	@Operation(summary="User exists for management purposes")
	@GetMapping("/users/{userId}/exists")
	@PreAuthorize("hasRole('ROLE_ADMINISTRATOR') or hasRole('ROLE_PLATFORM_ADMIN')")
	public ResponseEntity<String> userExists(@PathVariable("userId") String userId) {
		final MasterUser user = multitenancyService.getUser(userId);
		if (user == null) {
			return ResponseEntity.notFound().build();
		} else {
			return ResponseEntity.ok().build();
		}
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content=@Content(schema=@Schema(implementation=UserInfoDTO[].class))))
	@Operation(summary="Get current user info")
	@GetMapping("/me")
	public ResponseEntity<UserInfoDTO> currentUser(@RequestHeader("X-OP-APIKey") String header, Authentication auth) {
		final Optional<MasterUserToken> tokenOpt = multitenancyService.getMasterTokenByToken(header);
		if (tokenOpt.isPresent()) {
			final MasterUserToken masterToken = tokenOpt.get();
			final MasterUser user = masterToken.getMasterUser();
			final UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
			final UserInfoDTO payload =  UserInfoDTO.builder()
					.authorities(
							principal.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
					.username(user.getUserId()).name(user.getFullName()).tenant(masterToken.getTenant().getName())
					.vertical(masterToken.getVertical().getName()).email(user.getEmail()).verticals(user.getTenant().getVerticals()
							.stream().map(Vertical::getName).collect(Collectors.toList()))
					.build();
			return ResponseEntity.ok(payload);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content=@Content(schema=@Schema(implementation=UserInfoDTO[].class))))
	@Operation(summary="Get current user info")
	@GetMapping("/token/{token}")
	public ResponseEntity<UserInfoDTO> userByToken(@PathVariable("token") String token, Authentication auth) {
		final Optional<MasterUserToken> tokenOpt = multitenancyService.getMasterTokenByToken(token);
		if (tokenOpt.isPresent()) {
			final MasterUserToken masterToken = tokenOpt.get();
			final MasterUser user = masterToken.getMasterUser();
			final UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
			MultitenancyContextHolder.setVerticalSchema(masterToken.getVertical().getSchema());
			MultitenancyContextHolder.setTenantName(masterToken.getTenant().getName());
			final User u = userService.getUser(principal.getUsername());
			MultitenancyContextHolder.clear();
			final UserInfoDTO payload = UserInfoDTO.builder()
					.authorities(
							Arrays.asList(u.getRole().getId()))
					.username(user.getUserId()).name(user.getFullName()).tenant(masterToken.getTenant().getName())
					.vertical(masterToken.getVertical().getName()).email(user.getEmail()).verticals(user.getTenant().getVerticals()
							.stream().map(Vertical::getName).collect(Collectors.toList()))
					.build();
			return ResponseEntity.ok(payload);
		} else {
			return ResponseEntity.notFound().build();
		}
	}


}
