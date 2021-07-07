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
package com.minsait.onesait.platform.controlpanel.services.lowcode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.controlpanel.services.lowcode.FigmaDatabinding.DatabindingType;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FigmaServiceImpl implements FigmaService {

	private static final String FIGMA_REST_BASE = "https://api.figma.com/v1/files/";
	private static final String FIGMA_HEADER = "x-figma-token";
	private static final String FIGMA_PLUGIN_ID = "858477504263032980";
	private static final String FIGMA_PLUGIN_PARAM = "?plugin_data=" + FIGMA_PLUGIN_ID;
	private static final String CHILDREN_NODE = "children";
	private static final String DOCUMENT_NODE = "document";
	private static final String PLUGIN_NODE = "pluginData";
	private static final String CUSTOM_COMPONENT_NODE = "quxTypeCustom";
	private static final String[] METHOD_BINDING_TYPES = { "quxOnChangeCallback", "quxOnClickCallback",
			"quxOnLoadCallback" };
	private static final String[] DATA_BINDING_TYPES = { "quxDataBindingDefault", "quxDataBindingOutput" };

	private final RestTemplate restTemplate = new RestTemplate(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());

	@Autowired
	private FigmaFileUtils figmaFileUtils;

	@Override
	public FigmaExtractedData getPluginMappings(String fileId, String figmaToken) throws FigmaServiceException {
		final HttpHeaders headers = new HttpHeaders();
		headers.add(FIGMA_HEADER, figmaToken);
		try {
			final ResponseEntity<JsonNode> response = restTemplate.exchange(
					FIGMA_REST_BASE + fileId + FIGMA_PLUGIN_PARAM, HttpMethod.GET, new HttpEntity<>(headers),
					JsonNode.class);

			return processMappings(response.getBody());

		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("Error in Figma request: {}", e.getResponseBodyAsString(), e);
			if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
				throw new FigmaServiceException("Figma file not found", HttpStatus.NOT_FOUND.value());
			} else if (e.getStatusCode().equals(HttpStatus.FORBIDDEN)) {
				throw new FigmaServiceException("Invalid Token", HttpStatus.FORBIDDEN.value());

			}
			throw new FigmaServiceException(e.getResponseBodyAsString(), e.getStatusCode().value());
		} catch (final Exception e) {
			log.error("Error while mapping figma file model", e);
			throw new FigmaServiceException("Error while mapping figma file model", e,
					HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}

	@Override
	public File generateFromTemplate(FigmaSetUp figmaSetUp) throws FigmaServiceException {
		return figmaFileUtils.generateTemplate(figmaSetUp);
	}

	private FigmaExtractedData processMappings(JsonNode figmaFile) {
		// We always get first canvas [0]
		final JsonNode canvas = figmaFile.path(DOCUMENT_NODE).path(CHILDREN_NODE).get(0);
		final FigmaExtractedData config = new FigmaExtractedData();
		if (canvas.has(CHILDREN_NODE)) {
			final List<FigmaMethodMapping> methods = new ArrayList<>();
			final List<FigmaDatabinding> databindings = new ArrayList<>();
			final List<String> views = new ArrayList<>();
			final List<String> customComponents = new ArrayList<>();
			final ArrayNode canvasChildren = (ArrayNode) canvas.path(CHILDREN_NODE);
			canvasChildren.forEach(c -> {
				// FRAME LEVEL
				final String frame = c.path("name").asText();
				views.add(frame);
				processMappings(c, frame, methods, databindings, customComponents);
			});
			config.setDatabinding(databindings);
			config.setMethodbinding(methods);
			config.setCustomComponents(customComponents);
			config.setViews(views);
			config.setVariables(
					databindings.stream().map(FigmaDatabinding::getDataVarName).collect(Collectors.toList()));
			config.setMethods(methods.stream().map(FigmaMethodMapping::getBindedMethod).collect(Collectors.toList()));
		}
		return config;
	}

	public void processMappings(JsonNode child, String parentFrame, List<FigmaMethodMapping> methods,
			List<FigmaDatabinding> databindings, List<String> customComponents) {
		if (child.has(PLUGIN_NODE)) {
			final JsonNode pluginData = child.path(PLUGIN_NODE).path(FIGMA_PLUGIN_ID);
			for (final String element : METHOD_BINDING_TYPES) {
				if (pluginData.has(element)) {
					methods.add(FigmaMethodMapping.builder().parentFrame(parentFrame)
							.bindedMethod(pluginData.path(element).asText()).bindedMethodType(element).build());
				}
			}
			for (final String element : DATA_BINDING_TYPES) {
				if (pluginData.has(element)) {
					databindings.add(FigmaDatabinding.builder().dataType(DatabindingType.fromQuxType(element))
							.dataVarName(pluginData.path(element).asText()).parentFrame(parentFrame).build());
					// manually add variables that don't not show explicitly
					if (pluginData.path(element).asText().contains(".")) {
						final String variable = pluginData.path(element).asText().split("\\.")[0];
						final boolean contains = databindings.stream()
								.anyMatch(f -> f.getDataVarName().equals(variable));
						if (!contains) {
							databindings.add(FigmaDatabinding.builder().dataType(DatabindingType.fromQuxType(element))
									.dataVarName(variable).parentFrame(parentFrame).build());
						}
					}
				}
			}
			if (pluginData.has(CUSTOM_COMPONENT_NODE)) {
				customComponents.add(pluginData.get(CUSTOM_COMPONENT_NODE).asText());
			}
		}
		if (child.has(CHILDREN_NODE)) {
			final ArrayNode children = (ArrayNode) child.path(CHILDREN_NODE);
			children.forEach(c -> processMappings(c, parentFrame, methods, databindings, customComponents));

		}
	}

}
