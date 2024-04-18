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
package com.minsait.onesait.platform.simulator.job;

import java.io.IOException;

import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.simulator.job.utils.JsonUtils2;
import com.minsait.onesait.platform.simulator.service.FieldRandomizerService;
import com.minsait.onesait.platform.simulator.service.IoTBrokerClient;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DeviceSimulatorJob {

	@Autowired
	private FieldRandomizerService fieldRandomizerService;
	@Autowired
	private IoTBrokerClient persistenceService;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private JsonUtils2 jsonUtils2;

	private static final String PATH_INSTANCES = "instances";
	private static final String PATH_INSTANCES_MODE = "instancesMode";
	private static final String MODE_RANDOM = "random";
	private static final String MODE_SEC = "sequential";
	private static final String INDEX_STR = "index";

	private final ObjectMapper mapper = new ObjectMapper();

	public void execute(JobExecutionContext context) throws IOException {

		final String user = context.getJobDetail().getJobDataMap().getString("userId");

		final String json = context.getJobDetail().getJobDataMap().getString("json");

		try {
			proxyJson(user, json, context);
			log.debug("Simulated instance for user: {}", user);
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			log.error("Rest error: code {}, {}", e.getStatusCode(), e.getResponseBodyAsString());
		} catch (final Exception e) {
			log.error("Error generating the ontology instance for user:" + user + " and json:" + json, e);

		}

	}

	public void proxyJson(String user, String json, JobExecutionContext context)
			throws IOException, SchedulerException {
		final JsonNode jsonInstance = mapper.readTree(json);
		if (!jsonInstance.path(PATH_INSTANCES).isMissingNode())
			insertInstacesFromJson(user, json, context);
		else
			generateInstanceAndInsert(user, json);
	}

	public void insertInstacesFromJson(String user, String json, JobExecutionContext context)
			throws IOException, SchedulerException {
		final JsonNode contextJson = mapper.readTree(json);
		final JsonNode instances = contextJson.path(PATH_INSTANCES);
		final String clientPlatform = contextJson.get("clientPlatform").asText();
		final String clientPlatformInstance = contextJson.get("clientPlatformInstance").asText();
		final String ontology = contextJson.get("ontology").asText();

		if (!instances.isArray())
			persistenceService.insertOntologyInstance(instances.asText(), ontology, user, clientPlatform,
					clientPlatformInstance);
		else {
			String instancesMode = contextJson.path(PATH_INSTANCES_MODE).asText();
			final int size = ((ArrayNode) instances).size();
			if (StringUtils.isEmpty(instancesMode))
				instancesMode = MODE_RANDOM;
			switch (instancesMode) {
			case MODE_RANDOM:
				final int position = fieldRandomizerService.randomizeInt(0, size - 1);
				final String inst = ((ArrayNode) instances).get(position).toString();
				persistenceService.insertOntologyInstance(inst, ontology, user, clientPlatform, clientPlatformInstance);
				break;
			case MODE_SEC:
				int index = 0;
				try {
					index = context.getJobDetail().getJobDataMap().getInt(INDEX_STR);
					log.debug("Inserting item {} of json array, ontology {}", index, ontology);
				} catch (final Exception e) {
					log.error("Not index yet, setting index to 0");
				}

				if (index >= size)
					index = 0;
				final String sinst = ((ArrayNode) instances).get(index).toString();
				persistenceService.insertOntologyInstance(sinst, ontology, user, clientPlatform,
						clientPlatformInstance);
				context.getJobDetail().getJobDataMap().remove(INDEX_STR);
				context.getJobDetail().getJobDataMap().put(INDEX_STR, ++index);
				context.getScheduler().addJob(context.getJobDetail(), true);
				break;

			default:
				break;
			}
		}

	}

	public JsonNode generateInstanceAndInsert(String user, String json) throws IOException {

		final JsonNode jsonInstance = mapper.readTree(json);

		final String clientPlatform = jsonInstance.get("clientPlatform").asText();
		final String clientPlatformInstance = jsonInstance.get("clientPlatformInstance").asText();
		final String ontology = jsonInstance.get("ontology").asText();

		final JsonNode ontologySchema = jsonUtils2.generateJson(ontology, user);
		final JsonNode schema = mapper
				.readTree(ontologyService.getOntologyByIdentification(ontology, user).getJsonSchema());
		final JsonNode fieldAndValues = fieldRandomizerService.randomizeFields(jsonInstance.path("fields"),
				ontologySchema, schema);

		persistenceService.insertOntologyInstance(fieldAndValues.toString(), ontology, user, clientPlatform,
				clientPlatformInstance);
		log.debug("Inserted in ontology " + ontology + " data:" + fieldAndValues.toString());
		return fieldAndValues;
	}

}
