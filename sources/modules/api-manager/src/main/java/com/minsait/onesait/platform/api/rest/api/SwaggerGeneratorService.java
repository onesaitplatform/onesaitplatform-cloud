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
package com.minsait.onesait.platform.api.rest.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.minsait.onesait.platform.commons.exception.GenericOPException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/swagger")
@Api(value = "Swagger Generator")
public interface SwaggerGeneratorService {

	/**
	 *
	 * @deprecated
	 */
	@GET
	@Path("/{identificacion}/{token}")
	@Deprecated
	@ApiOperation(value = "Generate Swagger.json File", notes = "Generate Swagger.json File", httpMethod = "GET", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
	@ApiResponses(value = { @ApiResponse(code = 204, message = "No Content"),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 501, message = "Internal Server Error") })
	public Response getApi(
			@PathParam("identificacion") @ApiParam(name = "identificacion", required = true) String identificacion,
			@PathParam("token") @ApiParam(name = "token", required = true) String token) throws GenericOPException;

	@GET
	@Path("/{version}/{identification}/swagger.json")
	@ApiOperation(value = "Generate Swagger.json File", notes = "Generate Swagger.json File", httpMethod = "GET", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
	@ApiResponses(value = { @ApiResponse(code = 204, message = "No Content"),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 501, message = "Internal Server Error") })
	public Response getApiWithoutToken(
			@PathParam("version") @ApiParam(name = "version", required = true) String version,
			@PathParam("identification") @ApiParam(name = "identification", required = true) String identificacion,
			@QueryParam("vertical") @ApiParam(name = "vertical", required = false) String vertical)
			throws GenericOPException;
}