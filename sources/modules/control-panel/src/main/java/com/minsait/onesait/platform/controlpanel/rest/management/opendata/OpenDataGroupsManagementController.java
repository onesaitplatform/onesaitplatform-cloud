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
package com.minsait.onesait.platform.controlpanel.rest.management.opendata;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.business.services.opendata.group.GroupService;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataGroup;
import com.minsait.onesait.platform.controlpanel.rest.management.opendata.model.OpenDataGroupResponseDTO;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Groups Management")
@RestController
@ApiResponses({ @ApiResponse(responseCode = "400", description = "Bad request"),  @ApiResponse(responseCode = "401", description = "Unathorized"),
	@ApiResponse(responseCode = "500", description = "Internal server error"), @ApiResponse(responseCode = "403", description = "Forbidden") })
@RequestMapping("api/opendata/groups")
@Slf4j
public class OpenDataGroupsManagementController {

	private static final String MSG_ERROR_JSON_RESPONSE = "{\"error\":\"%s\"}";
	private static final String MSG_GROUP_NOT_EXIST = "Group does not exist";

	@Autowired
	private AppWebUtils utils;
	@Autowired
	private GroupService groupService;

	@Operation(summary = "Get all groups")
	@GetMapping("")
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=OpenDataGroupResponseDTO.class)), responseCode = "200", description = "Ok"))
	public ResponseEntity<Object> getAll() {
		try {
			final String userToken = utils.getUserOauthTokenByCurrentHttpRequest();

			final List<OpenDataGroup> groupsFromUser = groupService.getGroupsByUser(userToken);
			if (groupsFromUser == null || groupsFromUser.isEmpty()) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_GROUP_NOT_EXIST), HttpStatus.NOT_FOUND);
			}
			final List<OpenDataGroupResponseDTO> groups = new ArrayList<>();
			groupsFromUser.forEach(o -> groups.add(new OpenDataGroupResponseDTO(o)));

			return new ResponseEntity<>(groups, HttpStatus.OK);
		} catch (final Exception e) {
			log.error(String.format("Error getting groups list: %s ", e.getMessage()));
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
