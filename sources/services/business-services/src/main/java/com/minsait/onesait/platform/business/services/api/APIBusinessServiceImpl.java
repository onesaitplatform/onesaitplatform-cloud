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
package com.minsait.onesait.platform.business.services.api;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.minsait.onesait.platform.commons.utils.FileIOUtils;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Api.ClientJS;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.services.apimanager.ApiManagerService;
import com.minsait.onesait.platform.config.services.exceptions.OPResourceServiceException;
import com.minsait.onesait.platform.persistence.factory.BasicOpsDBRepositoryFactory;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class APIBusinessServiceImpl implements APIBusinessService {

	private static final String TMP_DIR = "/tmp";
	private static final String JS_FOLDER = "client-js";
	private static final String PATH_TO_SAMPLE = "/src/data/sample.json";
	private static final String PATH_TO_MENU = "/src/components/MenuVertical.js";
	private static final String PATH_TO_ENV = "/.env.production";
	private static final String OUTPUT_ZIP = "client-js.zip";
	private static final String APIS_VAR = "APIS";
	private static final String SAMPLES_VAR = "SAMPLES";
	private static final String SERVER_NAME_VAR = "SERVER_NAME";

	@Autowired
	private FileIOUtils fileUtils;
	@Autowired
	private BasicOpsDBRepositoryFactory basicOpsDBRepositoryFactory;
	@Autowired
	private ApiManagerService apiManagerService;
	@Autowired
	private ObjectMapper mapper;
	@Autowired
	private IntegrationResourcesService resourcesService;

	@Override
	public File generateJSClient(ClientJS framework, List<String> apiIds, String userId) {
		try {
			String pathToZip = null;
			if (ClientJS.REACT_JS.equals(framework)) {
				pathToZip = "templates/client-react-js.zip";
			} else {
				// TO-DO VUe js
				pathToZip = "";
			}
			final String basePath = TMP_DIR + File.separator + userId + File.separator + JS_FOLDER;
			log.debug("Creating DIRs");
			fileUtils.createDirs(basePath);
			log.debug("Unzipping {} to path {}", pathToZip, basePath);
			fileUtils.unzipToPath(pathToZip, basePath);
			final List<Api> apis = apiIds.stream().map(apiManagerService::getById).collect(Collectors.toList());
			log.debug("Compiling templates");
			compileTemplates(basePath, apis);
			log.debug("Zipping files to {}", basePath + File.separator + OUTPUT_ZIP);
			return fileUtils.zipFiles(basePath, TMP_DIR + File.separator + userId + File.separator + OUTPUT_ZIP);
		} catch (final Exception e) {
			log.error("Error while generating JS client", e);
			throw new OPResourceServiceException("Could not generate JS client: " + e.getMessage(), e);
		}
	}

	private void compileTemplates(String basePath, List<Api> apis) throws IOException {
		final JsonNode samples = mapper.createObjectNode();
		final ArrayNode apisMenu = mapper.createArrayNode();
		apis.forEach(a -> {
			((ObjectNode) samples).set(a.getIdentification(), getSample(a.getOntology()));
			apisMenu.add(mapper.convertValue(
					ApiClientJS.builder().name(a.getIdentification()).version("v" + a.getNumversion()).build(),
					JsonNode.class));
		});
		final String serverURL = resourcesService.getUrl(Module.DOMAIN, ServiceUrl.BASE);

		final HashMap<String, Object> scopes = new HashMap<>();
		scopes.put(SAMPLES_VAR, mapper.writeValueAsString(samples));
		scopes.put(SERVER_NAME_VAR, serverURL);
		scopes.put(APIS_VAR, apisMenu);
		compileTemplate(basePath + PATH_TO_MENU, scopes);
		compileTemplate(basePath + PATH_TO_SAMPLE, scopes);
		compileTemplate(basePath + PATH_TO_ENV, scopes);

	}

	private void compileTemplate(String writePath, Map<String, Object> scopes) throws IOException {
		log.debug("Compiling template {}", writePath);
		final String content = new String(Files.readAllBytes(Paths.get(writePath)));

		try (Writer writer = new FileWriter(writePath)) {

			final MustacheFactory mf = new DefaultMustacheFactory();
			final Mustache mustache = mf.compile(new StringReader(content), "compile");
			mustache.execute(writer, scopes);
			writer.flush();
		} catch (final IOException e) {
			log.error("error at file {}", writePath);
			throw e;
		}
	}

	private JsonNode getSample(Ontology ontology) {
		final String result = basicOpsDBRepositoryFactory.getInstance(ontology.getRtdbDatasource())
				.findAllAsJson(ontology.getIdentification(), 1);
		log.debug("Getting sample for ontology {}", ontology.getIdentification());
		ArrayNode arrayResult = null;
		try {
			arrayResult = mapper.readValue(result, ArrayNode.class);

		} catch (final IOException e) {
			log.error("Object mapper error while getting samples", e);
			throw new OPResourceServiceException("Object mapper error while getting api-ontology samples");
		}
		if (arrayResult.size() == 0) {
			throw new OPResourceServiceException("Selected API with ontology " + ontology + " has no records");
		}
		try {
			return mapper.readValue(arrayResult.get(0).asText(), JsonNode.class);
		} catch (final IOException e) {
			throw new OPResourceServiceException(
					"Error while parsing json for ontology " + ontology.getIdentification());
		}
	}

}
