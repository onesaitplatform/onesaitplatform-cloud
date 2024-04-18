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
package com.minsait.onesait.platform.api.rest.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.services.apimanager.dto.ApiSuscripcionDTO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/subscriptions")
@Api(value = "Api Service")
public interface ApiSuscriptionRestService {

	@GET
	@Path("/{identification}")
	@ApiOperation(value = "Find Suscriptions by ID", notes = "Returns an Suscriptions", httpMethod = "GET", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON, authorizations = @Authorization(value = "token"))

	@ApiResponses(value = { @ApiResponse(code = 204, message = "No Content"),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 501, message = "Internal Server Error") })
	public Response getApiSuscripciones(
			@PathParam("identification") @ApiParam(name = "identification", required = true) String identification,
			@HeaderParam("X-OP-APIKey") String token) throws GenericOPException;

	@GET
	@Path("/user/{identificacionUsuario}")
	@ApiOperation(value = "Find Suscriptions by identificacionUsuario", notes = "Returns Suscriptions", httpMethod = "GET", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON, authorizations = @Authorization(value = "token"))

	@ApiResponses(value = { @ApiResponse(code = 204, message = "No Content"),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 501, message = "Internal Server Error") })
	public Response getApiSuscripcionesUsuario(
			@PathParam("identificacionUsuario") @ApiParam(name = "identificacionUsuario", required = true) String identificacionUsuario,
			@HeaderParam("X-OP-APIKey") String token) throws GenericOPException;

	@POST
	@Path("/")
	@ApiOperation(value = "authorize a Suscription", notes = "authorize a Suscription", httpMethod = "POST", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON, authorizations = @Authorization(value = "token"))

	@ApiResponses(value = { @ApiResponse(code = 204, message = "No Content"),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 501, message = "Internal Server Error") })
	public Response autorize(ApiSuscripcionDTO suscripcion, @HeaderParam("X-OP-APIKey") String token)
			throws GenericOPException;

	@PUT
	@Path("/")
	@ApiOperation(value = "autorizeUpdate a Suscription", notes = "autorizeUpdate a Suscription", httpMethod = "PUT", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON, authorizations = @Authorization(value = "token"))
	@ApiResponses(value = { @ApiResponse(code = 204, message = "No Content"),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 501, message = "Internal Server Error") })
	public Response autorizeUpdate(ApiSuscripcionDTO suscripcion, @HeaderParam("X-OP-APIKey") String token)
			throws GenericOPException;

	@DELETE
	@Path("/")
	@ApiOperation(value = "deleteAutorizacion a Suscription", notes = "deleteAutorizacion a Suscription", httpMethod = "DELETE", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON, authorizations = @Authorization(value = "token"))
	@ApiResponses(value = { @ApiResponse(code = 204, message = "No Content"),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 501, message = "Internal Server Error") })
	public Response deleteAutorizacion(ApiSuscripcionDTO suscripcion, @HeaderParam("X-OP-APIKey") String token)
			throws GenericOPException;
}
