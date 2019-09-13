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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.config.services.dataflow.DataflowService;
import com.minsait.onesait.platform.config.services.dataflow.StreamsetsApiWrapper;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "DataFlow Management", tags = { "DataFlow management service" })
@RestController
@RequestMapping("api/dataflows")
public class DataFlowManagementController {

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
		return dataflowService.statusPipelines(utils.getUserId());
	}

	@ApiOperation(value = "Export pipeline")
	@PostMapping("/pipelines/{identification}/export")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Pipeline exported", response = String.class),
			@ApiResponse(code = 400, message = "Bad request"), @ApiResponse(code = 401, message = "Unathorized"),
			@ApiResponse(code = 403, message = "Forbidden"), @ApiResponse(code = 404, message = "Not Found"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	public ResponseEntity<String> exportPipeline(
			@ApiParam(value = "Dataflow pipeline identification", required = true) @PathVariable("identification") String pipelineIdentification)
			throws UnsupportedEncodingException {
		String identification = URLDecoder.decode(pipelineIdentification, StandardCharsets.UTF_8.name());
		return dataflowService.exportPipeline(utils.getUserId(), identification);
	}

	@ApiOperation(value = "Import pipeline")
	@PostMapping("/pipelines/{identification}/import")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Pipeline imported", response = String.class),
			@ApiResponse(code = 400, message = "Bad request"), @ApiResponse(code = 401, message = "Unathorized"),
			@ApiResponse(code = 403, message = "Forbidden"), @ApiResponse(code = 404, message = "Not Found"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	public ResponseEntity<String> importPipeline(
			@ApiParam(value = "Dataflow pipeline identification", required = true) @PathVariable("identification") String pipelineIdentification,
			@RequestBody(required = false) String config) throws UnsupportedEncodingException {
		String identification = URLDecoder.decode(pipelineIdentification, StandardCharsets.UTF_8.name());
		return dataflowService.importPipeline(utils.getUserId(), identification, config);
	}

	@ApiOperation(value = "Update pipeline")
	@PostMapping("/pipelines/{identification}/update")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Pipeline updated", response = String.class),
			@ApiResponse(code = 400, message = "Bad request"), @ApiResponse(code = 401, message = "Unathorized"),
			@ApiResponse(code = 403, message = "Forbidden"), @ApiResponse(code = 404, message = "Not Found"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	public ResponseEntity<String> updatePipeline(
			@ApiParam(value = "Dataflow pipeline identification", required = true) @PathVariable("identification") String pipelineIdentification,
			@RequestBody(required = false) String config) throws UnsupportedEncodingException {
		String identification = URLDecoder.decode(pipelineIdentification, StandardCharsets.UTF_8.name());
		return dataflowService.updatePipeline(utils.getUserId(), identification, config);
	}

	@ApiOperation(value = "Clone pipeline")
	@PostMapping("/pipelines/{identification}/clone")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Pipeline cloned", response = String.class),
			@ApiResponse(code = 400, message = "Bad request"), @ApiResponse(code = 401, message = "Unathorized"),
			@ApiResponse(code = 403, message = "Forbidden"), @ApiResponse(code = 404, message = "Not Found"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	public ResponseEntity<String> clonePipeline(
			@ApiParam(value = "Dataflow pipeline origin identification", required = true) @PathVariable("identification") String pipelineIdentificationOri,
			@ApiParam(value = "Dataflow pipeline origin identification", required = true) @RequestParam("destIdentification") String pipelineIdentificationDest)
			throws UnsupportedEncodingException {
		String identificationOri = URLDecoder.decode(pipelineIdentificationOri, StandardCharsets.UTF_8.name());
		String identificationDest = URLDecoder.decode(pipelineIdentificationDest, StandardCharsets.UTF_8.name());
		return dataflowService.clonePipeline(utils.getUserId(), identificationOri, identificationDest);
	}
		
	@ApiOperation(value = "Pipelines")
    @GetMapping("/pipelines")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Status of all pipelines obtained", response = String.class),
            @ApiResponse(code = 400, message = "Bad request"), @ApiResponse(code = 401, message = "Unathorized"),
            @ApiResponse(code = 403, message = "Forbidden"), @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Internal Server Error") })
    public ResponseEntity<String> pipelines(
            @ApiParam(value = "Filter text", required = false, defaultValue = "") @RequestParam(name="filterText", required=false, defaultValue = "") String filterText,
            @ApiParam(value = "Label", required = false, defaultValue = StreamsetsApiWrapper.SystemLabel.ALL_PIPELINES, allowableValues = StreamsetsApiWrapper.SystemLabel.ALL_OPTIONS ) @RequestParam(name="label", required=false, defaultValue = StreamsetsApiWrapper.SystemLabel.ALL_PIPELINES) String label,
            @ApiParam(value = "Offset", required = false, defaultValue = "0") @RequestParam(name="offset", required=false, defaultValue = "0") int offset,
            @ApiParam(value = "Len", required = false, defaultValue = "-1") @RequestParam(name="limit", required=false, defaultValue = "-1") int len,
            @ApiParam(value = "OrderBy", required = false, defaultValue = StreamsetsApiWrapper.OrderField.NAME, allowableValues = StreamsetsApiWrapper.OrderField.ALL_OPTIONS) @RequestParam(name="orderBy", required=false, defaultValue = StreamsetsApiWrapper.OrderField.NAME) String orderBy,
            @ApiParam(value = "Order", required = false, defaultValue = StreamsetsApiWrapper.Order.ASC, allowableValues = StreamsetsApiWrapper.Order.ALL_OPTIONS) @RequestParam(name="order", required=false, defaultValue = StreamsetsApiWrapper.Order.ASC) String order,
            @ApiParam(value = "Status", required = false, defaultValue = "false") @RequestParam(name="status", required=false, defaultValue = "false") boolean status){

        return dataflowService.pipelines(utils.getUserId(), filterText, label, offset, len, orderBy, order, status);
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
}
