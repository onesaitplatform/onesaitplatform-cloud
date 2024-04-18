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
package com.minsait.onesait.platform.controlpanel.rest.management.notebook.model;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin(origins = "*")
@Api(value = "Model Management", tags = { "Model management service" })
@ApiResponses({ @ApiResponse(code = 400, message = "Bad request"),
		@ApiResponse(code = 500, message = "Internal server error"), @ApiResponse(code = 403, message = "Forbidden") })
public interface ModelsRest {

	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = ModelDTO[].class))
	@ApiOperation(value = "Get Models category and subcategory, using header token to identify the user")
	@RequestMapping(value = "/getByCategoryAndSubcategory", method = RequestMethod.POST)
	public ResponseEntity<?> getByCategoryAndSubcategory(String authorization,
			@ApiParam(value = "Category", required = true) String category,
			@ApiParam(value = "Subcategory", required = true) String subcategory);

	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = ModelDTO[].class))
	@ApiOperation(value = "Get Model and name of the model, using header token to identify the user")
	@RequestMapping(value = "/getByModelId", method = RequestMethod.POST)
	public ResponseEntity<?> getByUserHeaderAndModelId(String authorization,
			@ApiParam(value = "Model name", required = true) String modelName);

	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = ModelDTO[].class))
	@ApiOperation(value = "Get Models by user, category and subcategory.")
	@RequestMapping(value = "/getByUserAndCategoryAndSubcategory", method = RequestMethod.POST)
	public ResponseEntity<?> getByUserAndCategoryAndSubcaegory(
			@RequestHeader(value = "Authorization") String authorization,
			@ApiParam(value = "User identification", required = true) String userId,
			@ApiParam(value = "Category", required = true) String category,
			@ApiParam(value = "Subcategory", required = true) String subcategory);

	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = ModelDTO[].class))
	@ApiOperation(value = "Get Model by user and name of the model")
	@RequestMapping(value = "/getByUserAndModelId", method = RequestMethod.POST)
	public ResponseEntity<?> getByUserAndModelId(@RequestHeader(value = "Authorization") String authorization,
			@ApiParam(value = "User identification", required = true) String userId,
			@ApiParam(value = "Model name", required = true) String modelName);

	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = String.class))
	@ApiOperation(value = "Execute model")
	@RequestMapping(value = "/executeModel", method = RequestMethod.POST)
	public ResponseEntity<?> executeModel(@RequestHeader(value = "Authorization") String authorization,
			@ApiParam(value = "User identification", required = true) String userId,
			@ApiParam(value = "A JSON with parameters needed yo execute the model", required = true) String params,
			@ApiParam(value = "Model name", required = true) String modelName);

	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = String.class))
	@ApiOperation(value = "Save the execution of the model")
	@RequestMapping(value = "/saveExecution", method = RequestMethod.POST)
	public ResponseEntity<?> saveExecution(@RequestHeader(value = "Authorization") String authorization,
			@ApiParam(value = "User identification", required = true) String userId,
			@ApiParam(value = "A JSON with parameters needed yo execute the model", required = true) String params,
			@ApiParam(value = "Model name", required = true) String modelName,
			@ApiParam(value = "Name of the execution", required = true) String executionName,
			@ApiParam(value = "Description of the execution", required = true) String executionDescription,
			@ApiParam(value = "Execution ID", required = true) String executionId);

	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = ExecutionDTO[].class))
	@ApiOperation(value = "Get List of executions of models")
	@RequestMapping(value = "/getExecutions", method = RequestMethod.POST)
	public ResponseEntity<?> getExecutions(@RequestHeader(value = "Authorization") String authorization,
			@ApiParam(value = "User identification", required = true) String userId);

	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = String.class))
	@ApiOperation(value = "Show an execution of model")
	@RequestMapping(value = "/showExecution", method = RequestMethod.POST)
	public ResponseEntity<?> showExecution(@RequestHeader(value = "Authorization") String authorization,
			@ApiParam(value = "User identification", required = true) String userId,
			@ApiParam(value = "Name of the execution", required = true) String executionName);

}
