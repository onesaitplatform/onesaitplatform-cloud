/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
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
package com.minsait.onesait.platform.controlpanel.rest.management.dashboard;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
@Deprecated
@RequestMapping(path = "/dashboard")
@CrossOrigin(origins = "*")
@Api(value = "Dashboard Management", tags = { "Dashoard management service" })
@ApiResponses({ @ApiResponse(code = 400, message = "Bad request"),
		@ApiResponse(code = 500, message = "Internal server error"), @ApiResponse(code = 403, message = "Forbidden") })
public interface DashboardsRest {

	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = DashboardDTO[].class))
	@ApiOperation(value = "Get dashboards by user, category and subcategory. (Category and subcategory are optionals)")
	@RequestMapping(value = "/getByUser", method = RequestMethod.POST)
	public ResponseEntity<?> getByUser(@ApiParam(value = "User identification", required = true) String userId,
			@ApiParam(value = "Category", required = false) String category,
			@ApiParam(value = "Subcategory", required = false) String subcategory);

	
	
}
