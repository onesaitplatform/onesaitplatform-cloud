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
package com.minsait.onesait.platform.examples.iotclient4springboot.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.minsait.onesait.platform.client.springboot.fromjson.ContextData;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class TicketOntology {

	// only needed for native queries
	private JsonNode _id;

	/**
	 * Options: You can ignore contextData with:@JsonIgnore You can get as a
	 * JsonNode: private JsonNode contextData;
	 */
	private ContextData contextData;

	@JsonProperty("Ticket")
	private Ticket ticket;
}
