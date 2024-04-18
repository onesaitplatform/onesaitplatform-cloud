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
package com.minsait.onesait.platform.api.rest.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.minsait.onesait.platform.commons.exception.GenericOPException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

@Consumes(MediaType.APPLICATION_JSON)
@Path("/tokenuser")
@Api(value = "token user rest service")
public interface TokenUserRestService {

	@GET
	@Path("{identification}/tokenUser")
	@ApiOperation(value = "Find token by ID", notes = "Find token by ID", httpMethod = "GET", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON, authorizations = @Authorization(value = "token"))
	@ApiResponses(value = { @ApiResponse(code = 204, message = "No Content"),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 501, message = "Internal Server Error") })
	public Response getTokenUser(
			@PathParam("identification") @ApiParam(name = "identification", required = true) String identification,
			@HeaderParam("X-OP-APIKey") String token) throws GenericOPException;

	@POST
	@Path("{identification}/tokenUser")
	@ApiOperation(value = "POST token by ID", notes = "POST token by ID", httpMethod = "POST", produces = MediaType.TEXT_PLAIN, consumes = MediaType.APPLICATION_JSON, authorizations = @Authorization(value = "token"))
	@ApiResponses(value = { @ApiResponse(code = 204, message = "No Content"),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 501, message = "Internal Server Error") })

	public Response addTokenUser(
			@PathParam("identification") @ApiParam(name = "identification", required = true) String identification,
			@HeaderParam("X-OP-APIKey") String token) throws GenericOPException;

	@PUT
	@Path("{identification}/tokenUser")
	@ApiOperation(value = "PUT token by ID", notes = "PUT token by ID", httpMethod = "PUT", produces = MediaType.TEXT_PLAIN, consumes = MediaType.APPLICATION_JSON, authorizations = @Authorization(value = "token"))
	@ApiResponses(value = { @ApiResponse(code = 204, message = "No Content"),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 501, message = "Internal Server Error") })
	public Response generateToken(
			@PathParam("identification") @ApiParam(name = "identification", required = true) String identification,
			@HeaderParam("X-OP-APIKey") String token) throws GenericOPException;

}
