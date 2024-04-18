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
package com.minsait.onesait.platform.digitaltwin.broker.plugable.impl.gateway.reference.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RequestMapping(path = "/config")
@CrossOrigin(origins = "*")
@Api(value = "config", description = "Onesaitplatform config for digital twins")
public interface ConfigGateway {

	@ApiOperation(value = "Get WoT of the Digital Twin")
	@RequestMapping(value = "/getWot", method = RequestMethod.POST)
	public ResponseEntity<?> getWot(
			@ApiParam(value = "Digital Twin Key provided from digital twin", required = true) String apiKey,
			@ApiParam(value = "Json data need to execute the event", required = true) JsonNode data);

}
