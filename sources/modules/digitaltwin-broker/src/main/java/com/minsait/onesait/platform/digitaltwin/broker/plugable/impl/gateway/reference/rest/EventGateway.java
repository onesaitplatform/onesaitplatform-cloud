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
package com.minsait.onesait.platform.digitaltwin.broker.plugable.impl.gateway.reference.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;



@RequestMapping(path="/event")
@CrossOrigin(origins = "*")
@Api(value="event", description="onesait Platform events for digital twins")
public interface EventGateway {
	
	@ApiOperation(value = "Event Register to register the endpoint of the Digital Twin")
	@RequestMapping(value="/register", method=RequestMethod.POST)
	public ResponseEntity<?> register(
			@ApiParam(value = "Digital Twin Key provided from digital twin", required = true) String apiKey,
			@ApiParam(value = "Json data need to execute the event", required = true) JsonNode data);
		
	
	@ApiOperation(value = "Event Ping")
	@RequestMapping(value="/ping", method=RequestMethod.POST)
	public ResponseEntity<?> ping(
			@ApiParam(value = "Digital Twin Key provided from digital twin", required = true) String apiKey,
			@ApiParam(value = "Json data need to execute the event", required = true)  JsonNode data);
	
	@ApiOperation(value = "Event Log")
	@RequestMapping(value="/log", method=RequestMethod.POST)
	public ResponseEntity<?> log(
			@ApiParam(value = "Digital Twin Key provided from digital twin", required = true) String apiKey,
			@ApiParam(value = "Json data need to execute the event", required = true) JsonNode data);
	
	@ApiOperation(value = "Event Shadow")
	@RequestMapping(value="/shadow", method=RequestMethod.POST)
	public ResponseEntity<?> shadow(
			@ApiParam(value = "Digital Twin Key provided from digital twin", required = true) String apiKey,
			@ApiParam(value = "Json data need to execute the event", required = true) JsonNode data);
	
	@ApiOperation(value = "Event Notebook")
	@RequestMapping(value="/notebook", method=RequestMethod.POST)
	public ResponseEntity<?> notebook(
			@ApiParam(value = "Digital Twin Key provided from digital twin", required = true) String apiKey,
			@ApiParam(value = "Json data need to execute the event", required = true) JsonNode data);
	
	@ApiOperation(value = "Event Flow")
	@RequestMapping(value="/flow", method=RequestMethod.POST)
	public ResponseEntity<?> flow(
			@ApiParam(value = "Digital Twin Key provided from digital twin", required = true) String apiKey,
			@ApiParam(value = "Json data need to execute the event", required = true) JsonNode data);
	
	@ApiOperation(value = "Event Rule")
	@RequestMapping(value="/rule", method=RequestMethod.POST)
	public ResponseEntity<?> rule(
			@ApiParam(value = "ApiKey provided from digital twin", required = true) String apiKey,
			@ApiParam(value = "Json data need to execute the event", required = true) JsonNode data);
	
	@ApiOperation(value = "Custom Event")
	@RequestMapping(value="/custom", method=RequestMethod.POST)
	public ResponseEntity<?> custom(
			@ApiParam(value = "Digital Twin Key provided from digital twin", required = true) String apiKey,
			@ApiParam(value = "Json data need to execute the event", required = true) JsonNode data);
}
