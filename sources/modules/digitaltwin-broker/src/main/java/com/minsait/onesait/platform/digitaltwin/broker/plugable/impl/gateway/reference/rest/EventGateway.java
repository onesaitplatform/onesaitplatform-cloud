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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;



@RequestMapping(path="/event")
@CrossOrigin(origins = "*")
@Tag(name="event", description="onesait Platform events for digital twins")
public interface EventGateway {

	@Operation(summary= "Event Register to register the endpoint of the Digital Twin")
	@RequestMapping(value="/register", method=RequestMethod.POST)
	public ResponseEntity<?> register(
			@Parameter(description= "Digital Twin Key provided from digital twin", required = true) String apiKey,
			@Parameter(description= "Json data need to execute the event", required = true) JsonNode data);


	@Operation(summary= "Event Ping")
	@RequestMapping(value="/ping", method=RequestMethod.POST)
	public ResponseEntity<?> ping(
			@Parameter(description= "Digital Twin Key provided from digital twin", required = true) String apiKey,
			@Parameter(description= "Json data need to execute the event", required = true)  JsonNode data);

	@Operation(summary= "Event Log")
	@RequestMapping(value="/log", method=RequestMethod.POST)
	public ResponseEntity<?> log(
			@Parameter(description= "Digital Twin Key provided from digital twin", required = true) String apiKey,
			@Parameter(description= "Json data need to execute the event", required = true) JsonNode data);

	@Operation(summary= "Event Shadow")
	@RequestMapping(value="/shadow", method=RequestMethod.POST)
	public ResponseEntity<?> shadow(
			@Parameter(description= "Digital Twin Key provided from digital twin", required = true) String apiKey,
			@Parameter(description= "Json data need to execute the event", required = true) JsonNode data);

	@Operation(summary= "Event Notebook")
	@RequestMapping(value="/notebook", method=RequestMethod.POST)
	public ResponseEntity<?> notebook(
			@Parameter(description= "Digital Twin Key provided from digital twin", required = true) String apiKey,
			@Parameter(description= "Json data need to execute the event", required = true) JsonNode data);

	@Operation(summary= "Event Flow")
	@RequestMapping(value="/flow", method=RequestMethod.POST)
	public ResponseEntity<?> flow(
			@Parameter(description= "Digital Twin Key provided from digital twin", required = true) String apiKey,
			@Parameter(description= "Json data need to execute the event", required = true) JsonNode data);

	@Operation(summary= "Event Rule")
	@RequestMapping(value="/rule", method=RequestMethod.POST)
	public ResponseEntity<?> rule(
			@Parameter(description= "ApiKey provided from digital twin", required = true) String apiKey,
			@Parameter(description= "Json data need to execute the event", required = true) JsonNode data);

	@Operation(summary= "Custom Event")
	@RequestMapping(value="/custom", method=RequestMethod.POST)
	public ResponseEntity<?> custom(
			@Parameter(description= "Digital Twin Key provided from digital twin", required = true) String apiKey,
			@Parameter(description= "Json data need to execute the event", required = true) JsonNode data);
}
