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
package com.minsait.onesait.platform.controlpanel.controller.lineage;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GraphLineageDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	private String source;
	@Getter
	@Setter
	private String target;
	@Getter
	@Setter
	private String title;
	@Getter
	@Setter
	private String classSource;
	@Getter
	@Setter
	private String classTarget;
	@Getter
	@Setter
	private String nameSource;
	@Getter
	@Setter
	private String nameTarget;
	@Getter
	@Setter
	private JsonNode properties;

	@Getter
	@Setter
	private Boolean isExternal;

	public GraphLineageDTO(String source, String target, String classSource, String classTarget, String nameSource,
			String nameTarget, String title, JsonNode properties, Boolean isExternal) {
		super();
		this.source = source;
		this.target = target;
		this.title = title;
		this.classSource = classSource;
		this.classTarget = classTarget;
		this.nameSource = nameSource;
		this.nameTarget = nameTarget;
		this.properties = properties;
		this.isExternal = isExternal;
	}

	public static GraphLineageDTO constructSingleNode(String source, String classSource, String nameSource,
			String title, JsonNode properties) {
		return new GraphLineageDTO(source, source, classSource, classSource, nameSource, nameSource, title, properties,
				false);
	}

	@Override
	@JsonRawValue
	@JsonIgnore
	public String toString() {
		final ObjectMapper mapper = new ObjectMapper();
		String result = null;
		try {
			result = mapper.writeValueAsString(this);
		} catch (final JsonProcessingException e) {
			log.error(e.getMessage());
		}
		return result;

	}
}