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
package com.minsait.onesait.platform.controlpanel.rest.management.models;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.minsait.onesait.platform.controlpanel.rest.management.models.model.ExecutionDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.models.model.ModelDTO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin(origins = "*")
@Api(value = "Model Management", tags = { "Model management service" })
@ApiResponses({ @ApiResponse(code = 400, message = "Bad request"),
		@ApiResponse(code = 500, message = "Internal server error"), @ApiResponse(code = 403, message = "Forbidden") })
public interface ModelsRestController {

	public ResponseEntity<?> getByCategoryAndSubcategory(String category, String subcategory);

	public ResponseEntity<?> getByUserHeaderAndModelId(String modelName);

	public ResponseEntity<?> getByUserAndCategoryAndSubcaegory(String userId, String category, String subcategory);

	public ResponseEntity<?> getByUserAndModelId(String userId, String modelName);

	public ResponseEntity<?> executeModel(String userId, String params, String modelName, boolean returnData);

	public ResponseEntity<?> saveExecution(String userId, String params, String modelName, String executionName, String executionDescription, String executionId);

	public ResponseEntity<?> getExecutions(String userId);

	public ResponseEntity<?> showExecution(String userId, String executionName);

}
