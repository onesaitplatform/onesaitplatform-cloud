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
package com.minsait.onesait.platform.controlpanel.rest.management.dataflow;

import com.minsait.onesait.platform.config.services.dataflow.DataflowService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Api(value = "Dataflow pipeline manager", tags = "Dataflow pipeline manager")
@RestController
@RequestMapping("api/dataflows")
public class DataFlowPipelineManagementController {

	@Autowired
	private DataflowService dataflowService;
	@Autowired
	private AppWebUtils utils;

	@ApiOperation(value = "Start a pipeline")
	@PostMapping("/pipelines/{identification}/start")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Start command sent correctly", response = String.class),
			@ApiResponse(code = 400, message = "Bad request"), @ApiResponse(code = 401, message = "Unathorized"),
			@ApiResponse(code = 403, message = "Forbidden"), @ApiResponse(code = 404, message = "Not Found"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	public ResponseEntity<String> startPipeline(
			@ApiParam(value = "Dataflow pipeline identification", required = true) @PathVariable("identification") String pipelineIdentification,
			@RequestBody(required = false) String parameters) throws UnsupportedEncodingException {
		String identification = URLDecoder.decode(pipelineIdentification, StandardCharsets.UTF_8.name());
		if (parameters == null || parameters.equals("")) {
			parameters = "{}";
		}
		return dataflowService.startPipeline(utils.getUserId(), identification, parameters);
	}

	@ApiOperation(value = "Stop a pipeline")
	@PostMapping("/pipelines/{identification}/stop")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Stop command sent correctly", response = String.class),
			@ApiResponse(code = 400, message = "Bad request"), @ApiResponse(code = 401, message = "Unathorized"),
			@ApiResponse(code = 403, message = "Forbidden"), @ApiResponse(code = 404, message = "Not Found"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	public ResponseEntity<String> stopPipeline(
			@ApiParam(value = "Dataflow pipeline identification", required = true) @PathVariable("identification") String pipelineIdentification)
			throws UnsupportedEncodingException {
		String identification = URLDecoder.decode(pipelineIdentification, StandardCharsets.UTF_8.name());
		return dataflowService.stopPipeline(utils.getUserId(), identification);
	}

	@ApiOperation(value = "Pipeline status")
	@GetMapping("/pipelines/{identification}/status")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Pipeline status obtained", response = String.class),
			@ApiResponse(code = 400, message = "Bad request"), @ApiResponse(code = 401, message = "Unathorized"),
			@ApiResponse(code = 403, message = "Forbidden"), @ApiResponse(code = 404, message = "Not Found"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	public ResponseEntity<String> statusPipeline(
			@ApiParam(value = "Dataflow pipeline identification", required = true) @PathVariable("identification") String pipelineIdentification)
			throws UnsupportedEncodingException {
		String identification = URLDecoder.decode(pipelineIdentification, StandardCharsets.UTF_8.name());
		return dataflowService.statusPipeline(utils.getUserId(), identification);
	}

	@ApiOperation(value = "Pipelines status")
	@GetMapping("/pipelines/status")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Status of all pipelines obtained", response = String.class),
			@ApiResponse(code = 400, message = "Bad request"), @ApiResponse(code = 401, message = "Unathorized"),
			@ApiResponse(code = 403, message = "Forbidden"), @ApiResponse(code = 404, message = "Not Found"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	public ResponseEntity<String> statusPipelines() {
		return dataflowService.getPipelinesStatus(utils.getUserId());
	}
	
	@ApiOperation(value = "Get a pipeline metrics")
    @PostMapping("/pipelines/{identification}/metrics")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Metrics of all pipelines obtained", response = String.class),
            @ApiResponse(code = 400, message = "Bad request"), @ApiResponse(code = 401, message = "Unathorized"),
            @ApiResponse(code = 403, message = "Forbidden"), @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Internal Server Error") })
    public ResponseEntity<String> metricsPipeline(
            @ApiParam(value = "Dataflow pipeline identification", required = true) @PathVariable("identification") String pipelineIdentification)
            throws UnsupportedEncodingException {
        String identification = URLDecoder.decode(pipelineIdentification, StandardCharsets.UTF_8.name());
        return dataflowService.metricsPipeline(utils.getUserId(), identification);
    }

	@ApiOperation(value = "Reset Origin offset")
	@PostMapping("/pipelines/{identification}/resetOffset")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Pipeline origin reset", response = String.class),
			@ApiResponse(code = 400, message = "Bad request"), @ApiResponse(code = 401, message = "Unathorized"),
			@ApiResponse(code = 403, message = "Forbidden"), @ApiResponse(code = 404, message = "Not Found"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	public ResponseEntity<String> resetPipeline(@ApiParam(value = "Dataflow pipeline identification", required = true)
			@PathVariable("identification") String pipelineIdentification)
			throws UnsupportedEncodingException {
		final String identification = URLDecoder.decode(pipelineIdentification, StandardCharsets.UTF_8.name());
		return dataflowService.resetOffsetPipeline(utils.getUserId(), identification);
	}

	/* EXCEPTION HANDLERS */

	@ExceptionHandler(ResourceAccessException.class)
	@ResponseStatus(value = HttpStatus.BAD_GATEWAY)
	@ResponseBody
	public String handleOPException(final ResourceAccessException exception) {
		return "Could not access the resource. Response: " + exception.getMessage();
	}

	@ExceptionHandler({IllegalArgumentException.class , RestClientException.class, DataAccessException.class,
			BadRequestException.class})
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	@ResponseBody
	public String handleOPException(final RuntimeException exception) {
		return exception.getMessage();
	}

	@ExceptionHandler(ClientErrorException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	@ResponseBody
	public String handleOPException(final ClientErrorException exception) {
		return "Status: "+exception.getResponse().getStatus()+" Response: "+exception.getMessage();
	}

	@ExceptionHandler(NotAuthorizedException.class)
	@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
	@ResponseBody
	public String handleOPException(final NotAuthorizedException exception) {
		return exception.getMessage();
	}

	@ExceptionHandler(NotFoundException.class)
	@ResponseStatus(value = HttpStatus.NOT_FOUND)
	@ResponseBody
	public String handleOPException(final NotFoundException exception) {
		return exception.getMessage();
	}

}
