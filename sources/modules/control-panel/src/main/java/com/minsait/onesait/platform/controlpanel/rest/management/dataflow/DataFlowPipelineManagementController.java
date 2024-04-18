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
package com.minsait.onesait.platform.controlpanel.rest.management.dataflow;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import com.minsait.onesait.platform.config.services.dataflow.DataflowService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;



@Tag(name = "Dataflow pipeline manager")
@RestController
@RequestMapping("api/dataflows")
public class DataFlowPipelineManagementController {

	@Autowired
	private DataflowService dataflowService;
	@Autowired
	private AppWebUtils utils;

	@Operation(summary = "Start a pipeline")
	@PostMapping("/pipelines/{identification}/start")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Start command sent correctly", content=@Content(schema=@Schema(implementation=String.class))),
			@ApiResponse(responseCode = "400", description = "Bad request"), @ApiResponse(responseCode = "401", description = "Unathorized"),
			@ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error") })
	public ResponseEntity<String> startPipeline(
			@Parameter(description= "Dataflow pipeline identification", required = true) @PathVariable("identification") String pipelineIdentification,
			@RequestBody(required = false) String parameters) throws UnsupportedEncodingException {
		final String identification = URLDecoder.decode(pipelineIdentification, StandardCharsets.UTF_8.name());
		if (parameters == null || parameters.equals("")) {
			parameters = "{}";
		}
		ResponseEntity<String> response = dataflowService.startPipeline(utils.getUserId(), identification, parameters);
		return new ResponseEntity<String>(response.getBody(), response.getStatusCode());
	}

	@Operation(summary = "Stop a pipeline")
	@PostMapping("/pipelines/{identification}/stop")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Stop command sent correctly", content=@Content(schema=@Schema(implementation=String.class))),
			@ApiResponse(responseCode = "400", description = "Bad request"), @ApiResponse(responseCode = "401", description = "Unathorized"),
			@ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error") })
	public ResponseEntity<String> stopPipeline(
			@Parameter(description= "Dataflow pipeline identification", required = true) @PathVariable("identification") String pipelineIdentification)
					throws UnsupportedEncodingException {
		final String identification = URLDecoder.decode(pipelineIdentification, StandardCharsets.UTF_8.name());
		ResponseEntity<String> response = dataflowService.stopPipeline(utils.getUserId(), identification);
		return new ResponseEntity<String>(response.getBody(), response.getStatusCode());
	}

	@Operation(summary = "Pipeline status")
	@GetMapping("/pipelines/{identification}/status")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Pipeline status obtained", content=@Content(schema=@Schema(implementation=String.class))),
			@ApiResponse(responseCode = "400", description = "Bad request"), @ApiResponse(responseCode = "401", description = "Unathorized"),
			@ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error") })
	public ResponseEntity<String> statusPipeline(
			@Parameter(description= "Dataflow pipeline identification", required = true) @PathVariable("identification") String pipelineIdentification)
					throws UnsupportedEncodingException {
		final String identification = URLDecoder.decode(pipelineIdentification, StandardCharsets.UTF_8.name());
		ResponseEntity<String> response = dataflowService.statusPipeline(utils.getUserId(), identification);
		return new ResponseEntity<String>(response.getBody(), response.getStatusCode());
	}

	@Operation(summary = "Pipelines status")
	@GetMapping("/pipelines/status")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Status of all pipelines obtained", content=@Content(schema=@Schema(implementation=String.class))),
			@ApiResponse(responseCode = "400", description = "Bad request"), @ApiResponse(responseCode = "401", description = "Unathorized"),
			@ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error") })
	public ResponseEntity<String> statusPipelines() {
		ResponseEntity<String> response = dataflowService.getPipelinesStatus(utils.getUserId());
		return new ResponseEntity<String>(response.getBody(), response.getStatusCode());
	}

	@Operation(summary = "Get a pipeline metrics")
	@PostMapping("/pipelines/{identification}/metrics")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Metrics of all pipelines obtained", content=@Content(schema=@Schema(implementation=String.class))),
			@ApiResponse(responseCode = "400", description = "Bad request"), @ApiResponse(responseCode = "401", description = "Unathorized"),
			@ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error") })
	public ResponseEntity<String> metricsPipeline(
			@Parameter(description= "Dataflow pipeline identification", required = true) @PathVariable("identification") String pipelineIdentification)
					throws UnsupportedEncodingException {
		final String identification = URLDecoder.decode(pipelineIdentification, StandardCharsets.UTF_8.name());
		ResponseEntity<String> response = dataflowService.metricsPipeline(utils.getUserId(), identification);
		return new ResponseEntity<String>(response.getBody(), response.getStatusCode());
	}

	@Operation(summary = "Reset Origin offset")
	@PostMapping("/pipelines/{identification}/resetOffset")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Pipeline origin reset", content=@Content(schema=@Schema(implementation=String.class))),
			@ApiResponse(responseCode = "400", description = "Bad request"), @ApiResponse(responseCode = "401", description = "Unathorized"),
			@ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error") })
	public ResponseEntity<String> resetPipeline(@Parameter(description= "Dataflow pipeline identification", required = true)
	@PathVariable("identification") String pipelineIdentification)
			throws UnsupportedEncodingException {
		final String identification = URLDecoder.decode(pipelineIdentification, StandardCharsets.UTF_8.name());
		ResponseEntity<String> response = dataflowService.resetOffsetPipeline(utils.getUserId(), identification);
		return new ResponseEntity<String>(response.getBody(), response.getStatusCode());
	}

	@Operation(summary = "Get a pipeline committed offsets")
	@GetMapping("/pipelines/{identification}/committedOffsets")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Pipeline committed offsets", content=@Content(schema=@Schema(implementation=String.class))),
			@ApiResponse(responseCode = "400", description = "Bad request"), @ApiResponse(responseCode = "401", description = "Unathorized"),
			@ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error") })
	public ResponseEntity<String> getPipelineCommittedOffsets(@Parameter(description= "Dataflow pipeline identification", required = true)
	@PathVariable("identification") String pipelineIdentification)
			throws UnsupportedEncodingException {
		final String identification = URLDecoder.decode(pipelineIdentification, StandardCharsets.UTF_8.name());
		ResponseEntity<String> response = dataflowService.getPipelineCommittedOffsets(utils.getUserId(), identification);
		return new ResponseEntity<String>(response.getBody(), response.getStatusCode());
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
