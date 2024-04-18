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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import com.minsait.onesait.platform.config.model.Pipeline;
import com.minsait.onesait.platform.config.services.dataflow.DataflowService;
import com.minsait.onesait.platform.config.services.dataflow.StreamsetsApiWrapper;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;



@Tag(name = "Dataflow pipeline storage")
@RestController
@RequestMapping("api/dataflows")
public class DataFlowStorageManagementController {

	@Autowired
	private DataflowService dataflowService;
	@Autowired
	private AppWebUtils utils;

	@Operation(summary = "Get pipeline configuration")
	@GetMapping("/pipelines/{identification}/configuration")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Pipeline configuration", content=@Content(schema=@Schema(implementation=String.class))),
			@ApiResponse(responseCode = "400", description = "Bad request"), @ApiResponse(responseCode = "401", description = "Unauthorized"),
			@ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error") })
	public ResponseEntity<String> configPipeline(
			@Parameter(description= "Dataflow pipeline identification", required = true) @PathVariable("identification") String pipelineIdentification)
					throws UnsupportedEncodingException {
		final String identification = URLDecoder.decode(pipelineIdentification, StandardCharsets.UTF_8.name());
		ResponseEntity<String> response = dataflowService.getPipelineConfiguration(utils.getUserId(), identification);
		return new ResponseEntity<String>(response.getBody(), response.getStatusCode());
	}

	@Operation(summary = "Export pipeline")
	@PostMapping("/pipelines/{identification}/export")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Pipeline exported", content=@Content(schema=@Schema(implementation=String.class))),
			@ApiResponse(responseCode = "400", description = "Bad request"), @ApiResponse(responseCode = "401", description = "Unauthorized"),
			@ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error") })
	public ResponseEntity<String> exportPipeline(
			@Parameter(description= "Dataflow pipeline identification", required = true) @PathVariable("identification") String pipelineIdentification)
					throws UnsupportedEncodingException {
		final String identification = URLDecoder.decode(pipelineIdentification, StandardCharsets.UTF_8.name());
		ResponseEntity<String> response = dataflowService.exportPipeline(utils.getUserId(), identification);
		return new ResponseEntity<String>(response.getBody(), response.getStatusCode());
	}

	@Operation(summary = "Import pipeline")
	@PostMapping("/pipelines/{identification}/import")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Pipeline imported", content=@Content(schema=@Schema(implementation=String.class))),
			@ApiResponse(responseCode = "400", description = "Bad request"), @ApiResponse(responseCode = "401", description = "Unauthorized"),
			@ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error") })
	public ResponseEntity<String> importPipeline(
			@Parameter(description= "Dataflow pipeline identification", required = true) @PathVariable("identification") String pipelineIdentification,
			@Parameter(description= "Overwrite pipeline if exists") @RequestParam(required = false, defaultValue = "false") boolean overwrite,
			@RequestBody(required = false) String config) throws UnsupportedEncodingException {
		final String identification = URLDecoder.decode(pipelineIdentification, StandardCharsets.UTF_8.name());
		return dataflowService.importPipeline(utils.getUserId(), identification, config, overwrite);
	}

	@Operation(summary = "Import pipeline data")
	@PostMapping("/pipelines/{identification}/importData")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Pipeline imported", content=@Content(schema=@Schema(implementation=String.class))),
			@ApiResponse(responseCode = "400", description = "Bad request"), @ApiResponse(responseCode = "401", description = "Unauthorized"),
			@ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error") })
	public ResponseEntity<String> importPipelineData(
			@Parameter(description= "Dataflow pipeline identification", required = true) @PathVariable("identification") String pipelineIdentification,
			@Parameter(description= "Overwrite pipeline if exists") @RequestParam(required = false, defaultValue = "false") boolean overwrite,
			@RequestBody(required = false) String config) throws UnsupportedEncodingException {
		final String identification = URLDecoder.decode(pipelineIdentification, StandardCharsets.UTF_8.name());
		ResponseEntity<String> response = dataflowService.importPipelineData(utils.getUserId(), identification, config, overwrite);
		return new ResponseEntity<String>(response.getBody(), response.getStatusCode());
	}

	@Operation(summary = "Update pipeline")
	@PostMapping("/pipelines/{identification}/update")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Pipeline updated", content=@Content(schema=@Schema(implementation=String.class))),
			@ApiResponse(responseCode = "400", description = "Bad request"), @ApiResponse(responseCode = "401", description = "Unauthorized"),
			@ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error") })
	public ResponseEntity<String> updatePipeline(
			@Parameter(description= "Dataflow pipeline identification", required = true) @PathVariable("identification") String pipelineIdentification,
			@RequestBody(required = false) String config) throws UnsupportedEncodingException {
		final String identification = URLDecoder.decode(pipelineIdentification, StandardCharsets.UTF_8.name());
		ResponseEntity<String> response = dataflowService.updatePipeline(utils.getUserId(), identification, config);
		return new ResponseEntity<String>(response.getBody(), response.getStatusCode());
	}

	@Operation(summary = "Clone pipeline")
	@PostMapping("/pipelines/{identification}/clone")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Pipeline cloned", content=@Content(schema=@Schema(implementation=String.class))),
			@ApiResponse(responseCode = "400", description = "Bad request"), @ApiResponse(responseCode = "401", description = "Unauthorized"),
			@ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error") })
	public ResponseEntity<String> clonePipeline(
			@Parameter(description= "Dataflow pipeline origin identification", required = true) @PathVariable("identification") String pipelineIdentificationOri,
			@Parameter(description= "Dataflow pipeline dest identification", required = true) @RequestParam("destIdentification") String pipelineIdentificationDest)
					throws UnsupportedEncodingException {
		final String identificationOri = URLDecoder.decode(pipelineIdentificationOri, StandardCharsets.UTF_8.name());
		final String identificationDest = URLDecoder.decode(pipelineIdentificationDest, StandardCharsets.UTF_8.name());
		ResponseEntity<String> response = dataflowService.clonePipeline(utils.getUserId(), identificationOri, identificationDest);
		return new ResponseEntity<String>(response.getBody(), response.getStatusCode());
	}

	@Operation(summary = "Pipelines")
	@GetMapping("/pipelines")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Status of all pipelines obtained", content=@Content(schema=@Schema(implementation=String.class))),
			@ApiResponse(responseCode = "400", description = "Bad request"), @ApiResponse(responseCode = "401", description = "Unauthorized"),
			@ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error") })
	public ResponseEntity<String> pipelines(
			@Parameter(description= "Filter text", required = false) @RequestParam(name = "filterText", required = false, defaultValue = "") String filterText,
			@Parameter(description= "Label", required = false) @RequestParam(name = "label", required = false, defaultValue = StreamsetsApiWrapper.SystemLabel.ALL_PIPELINES) String label,
			@Parameter(description= "Offset", required = false) @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
			@Parameter(description= "Len", required = false) @RequestParam(name = "limit", required = false, defaultValue = "-1") int len,
			@Parameter(description= "OrderBy", required = false) @RequestParam(name = "orderBy", required = false, defaultValue = StreamsetsApiWrapper.OrderField.NAME) String orderBy,
			@Parameter(description= "Order", required = false) @RequestParam(name = "order", required = false, defaultValue = StreamsetsApiWrapper.Order.ASC) String order,
			@Parameter(description= "Status", required = false) @RequestParam(name = "status", required = false, defaultValue = "false") boolean status) {

		ResponseEntity<String> response = dataflowService.pipelines(utils.getUserId(), filterText, label, offset, len, orderBy, order, status);
		return new ResponseEntity<String>(response.getBody(), response.getStatusCode());
	}
	
	@Operation(summary = "get pipeline by identification or id")
	@GetMapping("/pipelines/{id}")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Status of all pipelines obtained", content=@Content(schema=@Schema(implementation=String.class))),
			@ApiResponse(responseCode = "400", description = "Bad request"), @ApiResponse(responseCode = "401", description = "Unauthorized"),
			@ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error") })
	public ResponseEntity<Object> getPipeline(
			@Parameter(description= "Dataflow pipeline identification or id", required = true) @PathVariable("id") String pipelineIdentification)
					throws UnsupportedEncodingException {

		final String identification = URLDecoder.decode(pipelineIdentification, StandardCharsets.UTF_8.name());
		return dataflowService.getPipelineByIdentificationOrId(identification, utils.getUserId());
	}

	@Operation(summary = "delete pipeline")
	@DeleteMapping("/pipelines/{identification}")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Status of all pipelines obtained", content=@Content(schema=@Schema(implementation=String.class))),
			@ApiResponse(responseCode = "400", description = "Bad request"), @ApiResponse(responseCode = "401", description = "Unauthorized"),
			@ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error") })
	public ResponseEntity<String> deletePipeline(
			@Parameter(description= "Dataflow pipeline identification", required = true) @PathVariable("identification") String pipelineIdentification)
					throws UnsupportedEncodingException {

		final String identification = URLDecoder.decode(pipelineIdentification, StandardCharsets.UTF_8.name());
		final Pipeline pipeline = dataflowService.getPipelineByIdentification(identification);
		dataflowService.deleteHardPipeline(pipeline.getId(), utils.getUserId());
		return ResponseEntity.ok(identification);
	}

	/* EXCEPTION HANDLERS */

	@ExceptionHandler(ResourceAccessException.class)
	@ResponseStatus(value = HttpStatus.BAD_GATEWAY)
	@ResponseBody
	public String handleOPException(final ResourceAccessException exception) {
		return "Could not access the resource. Response: " + exception.getMessage();
	}

	@ExceptionHandler({ IllegalArgumentException.class, RestClientException.class, DataAccessException.class,
		BadRequestException.class })
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	@ResponseBody
	public String handleOPException(final RuntimeException exception) {
		return exception.getMessage();
	}

	@ExceptionHandler(ClientErrorException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	@ResponseBody
	public String handleOPException(final ClientErrorException exception) {
		return "Status: " + exception.getResponse().getStatus() + " Response: " + exception.getMessage();
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
