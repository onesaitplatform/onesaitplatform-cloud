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
package com.minsait.onesait.platform.config.converters;

import java.io.IOException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.components.GitlabConfiguration;

@Converter(autoApply = true)
public class GitlabConfigurationConverter implements AttributeConverter<GitlabConfiguration, String> {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String convertToDatabaseColumn(GitlabConfiguration attribute) {
		try {
			return objectMapper.writeValueAsString(attribute);
		} catch (final JsonProcessingException ex) {
			return null;
		}
	}

	@Override
	public GitlabConfiguration convertToEntityAttribute(String dbData) {
		try {
			return objectMapper.readValue(dbData, GitlabConfiguration.class);
		} catch (final IOException ex) {
			return null;
		}
	}

}
