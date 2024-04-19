/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2022 SPAIN
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
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.services.apimanager.dto.ApiDTO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/apis")
@Api(value = "Api Service")
public interface ApiRestService {

	@GET
	@Path("/{identification}")
	@ApiOperation(value = "Find Api by ID", notes = "Returns an API", httpMethod = "GET", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON, authorizations = @Authorization(value = "token"))
	@ApiResponses(value = { @ApiResponse(code = 204, message = "No Content"),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 501, message = "Internal Server Error") })
	public Response getApi(
			@PathParam("identification") @ApiParam(name = "identification", required = true) String identification,
			@HeaderParam("X-OP-APIKey") String token) throws GenericOPException;

	@GET
	@Path("/")
	@ApiOperation(value = "Find Api by ID", notes = "Returns an API with All Parameters", httpMethod = "GET", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON, authorizations = @Authorization(value = "token"))
	@ApiResponses(value = { @ApiResponse(code = 204, message = "No Content"),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 501, message = "Internal Server Error") })
	public Response getApiFilter(
			@DefaultValue("") @QueryParam("identification") @ApiParam(name = "identification", required = false) String identification,
			@DefaultValue("") @QueryParam("estado") @ApiParam(name = "estado", required = false) String estado,
			@DefaultValue("") @QueryParam("usuario") @ApiParam(name = "usuario", required = false) String usuario,
			@HeaderParam("X-OP-APIKey") String token) throws GenericOPException;

	@POST
	@Path("/")
	@ApiOperation(value = "Creación API", notes = "Creación API", httpMethod = "POST", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON, authorizations = @Authorization(value = "token"))
	@ApiResponses(value = { @ApiResponse(code = 204, message = "No Content"),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 501, message = "Internal Server Error") })
	public Response create(ApiDTO api, @HeaderParam("X-OP-APIKey") String token) throws GenericOPException;

	@POST
	@Path("/changestate/{indentifier}")
	@ApiOperation(value = "Change STate API", notes = "Change STate API", httpMethod = "POST", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON, authorizations = @Authorization(value = "token"))
	@ApiResponses(value = { @ApiResponse(code = 204, message = "No Content"),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 501, message = "Internal Server Error") })
	public Response create(
			@DefaultValue("") @QueryParam("indentifier") @ApiParam(name = "indentifier") String indentifier,
			com.minsait.onesait.platform.config.model.Api.ApiStates api, @HeaderParam("X-OP-APIKey") String token)
			throws GenericOPException;

	@PUT
	@Path("/")
	@ApiOperation(value = "Modify API", notes = "Modify API", httpMethod = "PUT", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON, authorizations = @Authorization(value = "token"))
	@ApiResponses(value = { @ApiResponse(code = 204, message = "No Content"),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 501, message = "Internal Server Error") })
	public Response update(ApiDTO suscripcion, @HeaderParam("X-OP-APIKey") String token) throws GenericOPException;

	@DELETE
	@Path("/")
	@ApiOperation(value = "Delete API", notes = "Delete API", httpMethod = "DELETE", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON, authorizations = @Authorization(value = "token"))
	@ApiResponses(value = { @ApiResponse(code = 204, message = "No Content"),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 501, message = "Internal Server Error") })
	public Response delete(ApiDTO suscripcion, @HeaderParam("X-OP-APIKey") String token) throws GenericOPException;

	@DELETE
	@Path("/{identification}/{numversion}")
	@ApiOperation(value = "Delete API", notes = "Delete API", httpMethod = "DELETE", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON, authorizations = @Authorization(value = "token"))
	@ApiResponses(value = { @ApiResponse(code = 204, message = "No Content"),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 501, message = "Internal Server Error") })
	public Response deleteByIdentificationNumversion(
			@PathParam("identification") @ApiParam(name = "identification", required = true) String identification,
			@PathParam("numversion") @ApiParam(name = "numversion", required = true) String numversion,
			@HeaderParam("X-OP-APIKey") String token) throws GenericOPException;

	@GET
	@Path("/user/{idUsuario}")
	@ApiOperation(value = "GET USER", notes = "GET USER", httpMethod = "GET", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON, authorizations = @Authorization(value = "token"))
	@ApiResponses(value = { @ApiResponse(code = 204, message = "No Content"),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 501, message = "Internal Server Error") })
	public Response getApiUsuario(
			@PathParam("idUsuario") @ApiParam(name = "idUsuario", required = true) String idUsuario,
			@HeaderParam("X-OP-APIKey") String token) throws GenericOPException;
	
	@GET
	@Path("/cache/clean")
	@ApiOperation(value = "Clean Cache", notes = "Cleans API Manager Cache", httpMethod = "GET", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON, authorizations = @Authorization(value = "X-OP-APIKey"))
	@ApiResponses(value = { @ApiResponse(code = 204, message = "No Content"),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 501, message = "Internal Server Error") })
	public Response cleanCache(
			@HeaderParam("X-OP-APIKey") String tokenOauth) throws GenericOPException;

	
	@GET
	@Path("/cache/clean/{identification}/{numversion}")
	@ApiOperation(value = "Clean Api Cache", notes = "Cleans an API Cache", httpMethod = "GET", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON, authorizations = @Authorization(value = "X-OP-APIKey"))
	@ApiResponses(value = { @ApiResponse(code = 204, message = "No Content"),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 501, message = "Internal Server Error") })
	public Response cleanApiCache(
			@PathParam("identification") @ApiParam(name = "identification", required = true) String identification,
			@PathParam("numversion") @ApiParam(name = "numversion", required = true) String numversion,
			@HeaderParam("X-OP-APIKey") String tokenOauth) throws GenericOPException;

}