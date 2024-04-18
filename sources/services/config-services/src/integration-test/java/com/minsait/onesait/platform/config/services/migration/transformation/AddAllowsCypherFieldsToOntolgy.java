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
package com.minsait.onesait.platform.config.services.migration.transformation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import de.galan.verjson.step.transformation.Transformation;
import static de.galan.verjson.util.Transformations.*;

import java.util.function.Consumer;


public class AddAllowsCypherFieldsToOntolgy extends Transformation{

	@Override
	protected void transform(JsonNode node) {
		JsonNode nodeAllData = node.get("allData");
		
		if (nodeAllData.isArray()) {
			ArrayNode arrayAllData = (ArrayNode) nodeAllData;
			arrayAllData.forEach(consumerData);
			
		} else {
			throw new IllegalArgumentException("The json provided has an invalid format");
		}
	}
	
	private Consumer<JsonNode> consumerData = new Consumer<JsonNode>() {
		@Override
		public void accept(JsonNode node) {
			JsonNode jsonNode = node.get("class");
			String className = jsonNode.asText();
			switch (className) {
			case "com.minsait.onesait.platform.config.model.Ontology":
				JsonNode ontologiesNode = node.get("instances");
				ArrayNode arrayOntologies = (ArrayNode) ontologiesNode;
				arrayOntologies.forEach(transformOntology);
				break;

			default:
				break;
			}
		}
	};
	
	private Consumer<? super JsonNode> transformOntology = new Consumer<JsonNode>() {
		@Override
		public void accept(JsonNode node) {
			JsonNode jsonNode = node.get("data");
			obj(jsonNode).put("allowsCypherFields", false);
		}
	};

}
