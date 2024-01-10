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
package com.minsait.onesait.platform.rulesengine.model;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class OntologyJsonWrapper {

	@Setter
	@Getter
	private Map<String, Object> json = new HashMap<>();
	private final ObjectMapper mapper = new ObjectMapper();
	@Getter
	@Setter
	private String rootNode;

	@SuppressWarnings("unchecked")
	public OntologyJsonWrapper(String jsonString) {
		if (log.isDebugEnabled()) {
			log.debug("New OntologyJsonWrapper object created with json: {}", jsonString);
		}		
		try {
			json = mapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {
			});
			if (json.keySet().size() == 1) {
				rootNode = json.keySet().iterator().next();
				json = (Map<String, Object>) json.values().iterator().next();
			}
		} catch (final Exception e) {
			log.error("Error deserializing JSON String, returning original json");
		}
	}

	public Object getProperty(String key) {
		if (log.isDebugEnabled()) {
			log.debug("OntologyJsonWrapper -- getProperty: {} -- value: {}", key, json.get(key));
		}		
		return json.get(key);
	}

	public void setProperty(String key, Object value) {
		if (log.isDebugEnabled()) {
			log.debug("OntologyJsonWrapper -- setProperty: {} -- value: {}", value, key);
		}		
		json.put(key, value);
	}

	public void removeProperty(String key) {
		json.remove(key);
	}

	public void updateProperty(String key, Object value) {
		removeProperty(key);
		json.put(key, value);
	}

	public void copyInputToOutput(OntologyJsonWrapper input) {
		setJson(input.getJson());
	}

	public void printValues() {
		log.info("OntologyJsonWrapper value: " + toJson());
	}

	public String toJson() {
		try {
			if (StringUtils.hasText(rootNode)) {
				return mapper.writeValueAsString(mapper.createObjectNode().set(rootNode, mapper.valueToTree(json)));
			}
			else {
				return mapper.writeValueAsString(json);
			}

		} catch (final JsonProcessingException e) {
			log.error("Error serializing Ontology Wrapper");
			return "";
		}
	}

}
