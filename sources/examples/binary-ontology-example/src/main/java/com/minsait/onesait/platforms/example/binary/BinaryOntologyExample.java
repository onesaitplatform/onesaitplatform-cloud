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
package com.minsait.onesait.platform.example.binary;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.client.RestClient;
import com.minsait.onesait.platform.comms.protocol.binary.Mime;
import com.minsait.onesait.platform.comms.protocol.util.BinarySerializer;

public class BinaryOntologyExample {

	public static void main(String[] args) throws IOException {
		ObjectMapper mapper = new ObjectMapper();

		RestClient client = new RestClient("http://localhost:8081/iotbroker");
		String token = "e7ef0742d09d4de5a3687f0cfdf7f626";
		String clientPlatform = "Ticketing App";
		String clientPlatformInstance = clientPlatform + ":REST";
		String ontology = "Ticket";

		client.connect(token, clientPlatform, clientPlatformInstance);

		String pathFile = "./logo_S4C.png";
		String outputPath = "./";

		BinarySerializer serializer = new BinarySerializer();		

		JsonNode image = null;
		try {
			image = mapper.createObjectNode();
			image = serializer.getJsonBinary("File", new File(pathFile), Mime.IMAGE_PNG);		
			String ontIns = "{ \"Ticket\" : { \"Identification\" : \"Road\", \"Status\" : \"STOPPED\", \"Email\" : \"some@gmail.com\", \"Name\" : \"Javier\", \"Response_via\" : \"Email\", \"Coordinates\" : { \"coordinates\" : { \"0\" : 40.5295428, \"1\" : -3.641471 }, \"type\" : \"Point\" }, \"Type\" : \"ROAD\", \"Description\" : \"Roads in bad shape\" }, \"contextData\" : { \"clientPatform\" : \"Ticketing App\", \"clientPatformInstance\" : \"Ticketing App: Web\", \"clientConnection\" : \"\", \"clientSession\" : \"5c4b87d2-9d43-4cb7-851e-b14b4d023f2b\", \"user\" : \"developer\", \"timezoneId\" : \"Europe/Paris\", \"timestamp\" : \"Wed Apr 11 17:11:36 CEST 2018\" } }";
			JsonNode object = mapper.readTree(ontIns);
			((ObjectNode) object.path(ontology)).set("File", image.path("File"));

			// perform POST
			client.insertInstance(ontology, object.toString());

			// perform GET
			List<JsonNode> instances = client.getOntologyInstances(ontology);

			for (JsonNode ontologyInstance : instances) {
				if (!ontologyInstance.path(ontology).path("File").isMissingNode()) {
					serializer.binaryJsonToFile(ontologyInstance.path(ontology).path("File"), outputPath);

				}
			}
		} catch (Exception e) {
			System.err.println("Error in process by:"+e.getMessage());
		}


	}
}
