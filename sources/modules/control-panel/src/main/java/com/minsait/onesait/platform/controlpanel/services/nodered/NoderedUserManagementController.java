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
package com.minsait.onesait.platform.controlpanel.services.nodered;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.config.model.UserToken;
import com.minsait.onesait.platform.config.services.exceptions.ApiManagerServiceException;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.config.services.usertoken.UserTokenService;
import com.minsait.onesait.platform.controlpanel.rest.management.api.model.ApiResponseErrorDTO;
import com.minsait.onesait.platform.security.ri.ConfigDBDetailsService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("/nodered/auth")
public class NoderedUserManagementController {

	@Autowired
	private UserTokenService userTokenService;
	@Autowired
	private UserService userService;
	@Autowired
	private ConfigDBDetailsService detailsService;

	@ApiOperation(value = "Get users access to api by identification or id")
	@GetMapping(value = "/{userId}/{apiToken}")
	public ResponseEntity<String> getAuthorizations(
			@ApiParam(value = "User", required = true) @PathVariable(name = "userId") String userId,
			@ApiParam(value = "Api token", required = true) @PathVariable("apiToken") String apiToken) {

		ResponseEntity<String> response;
		try {
			// Multitenant context loading
			detailsService.loadUserByUserToken(apiToken);
			final UserToken userToken = userTokenService.getTokenByUserAndToken(userService.getUser(userId), apiToken);
			if (userToken != null) {
				response = new ResponseEntity<>("{}", HttpStatus.OK);
			} else {
				response = new ResponseEntity<>("{}", HttpStatus.NOT_FOUND);
			}
		} catch (final ApiManagerServiceException e) {
			final ApiResponseErrorDTO errorDTO = new ApiResponseErrorDTO(e);
			response = new ResponseEntity<>("{}", errorDTO.defaultHttpStatus());
		} catch (final Exception e) {
			response = new ResponseEntity<>("{}", HttpStatus.BAD_REQUEST);
		}

		return response;

	}
}
