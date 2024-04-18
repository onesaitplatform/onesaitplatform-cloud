/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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
package com.minsait.onesait.platform.persistence.services.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.services.exceptions.OntologyServiceException;
import com.minsait.onesait.platform.config.services.ontology.OntologyServiceImpl;
import com.minsait.onesait.platform.config.services.ontologymqtttopic.OntologyMqttTopicService;
import com.minsait.onesait.platform.persistence.factory.ManageDBRepositoryFactory;
import com.minsait.onesait.platform.persistence.interfaces.ManageDBRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OntologyLogicService {

	@Autowired
	private ManageDBRepositoryFactory manageDBPersistence;

	@Autowired
	private OntologyServiceImpl ontologyService;

	@Autowired
	private OntologyMqttTopicService mqttTopicService;

	public void createOntology(Ontology ontology, Map<String, String> config) {

		try {
			if (!ontology.getRtdbDatasource().equals(Ontology.RtdbDatasource.API_REST)) {
				if (log.isDebugEnabled()) {
					log.debug("create ontology in db {}", ontology.getRtdbDatasource());
				}

				String jsonschema = ontology.getJsonSchema();
				JSONObject json = new JSONObject(jsonschema);

				if (jsonschema.contains("hasrecords") && !jsonschema.contains("keeprecords")) {
					removeOntology(ontology);
					json.remove("hasrecords");
					ontology.setJsonSchema(json.toString());
				}

				this.getInstance(ontology.getRtdbDatasource()).createTable4Ontology(ontology.getIdentification(),
						ontology.getJsonSchema(), config);

				if (jsonschema.contains("keeprecords")) {
					json.remove("keeprecords");
					json.remove("hasrecords");
					ontology.setJsonSchema(json.toString());
				}
			}

			if (ontology.isAllowsCreateMqttTopic()) {
				mqttTopicService.createMqttTopic(ontology, config.get("mqttTopicName"));
			}

		} catch (final Exception e) {
			log.error("Error creating ontology", e);
			throw new OntologyServiceException("Problems creating table for ontology." + e.getMessage(), e);
		}

		log.debug("ontology created");
	}

	public Map<String, String> getAdditionalDBConfig(Ontology ontology) {
		if (log.isDebugEnabled()) {
			log.debug("Get internal config in ontology {}", ontology.getRtdbDatasource());
		}
		try {
			return this.getInstance(ontology.getRtdbDatasource()).getAdditionalDBConfig(ontology.getIdentification());
		} catch (final Exception e) {
			throw new OntologyLogicServiceException(
					"Problems getting internal DB config of the ontology." + e.getMessage(), e);
		}
	}

	public void updateOntology(Ontology ontology, HashMap<String, String> config) {
		try {
			if (log.isDebugEnabled()) {
				log.debug("update ontology in db {}", ontology.getRtdbDatasource());
			}
			this.getInstance(ontology.getRtdbDatasource()).updateTable4Ontology(ontology.getIdentification(),
					ontology.getJsonSchema(), config);

		} catch (final Exception e) {

			throw new OntologyLogicServiceException("Problems updating the ontology." + e.getMessage(), e);
		}

		log.debug("ontology updated");

	}

	public void checkSameInternalDBConfig(Ontology ontology, HashMap<String, String> configToConfigMap) {

		Map<String, String> prevConfig = getAdditionalDBConfig(ontology);
		for (Map.Entry<String, String> entry : prevConfig.entrySet()) {
			String configKey = entry.getKey();
			String configValue = entry.getValue();
			if (configValue == null) {
				if (configToConfigMap.containsKey(configKey) && configToConfigMap.get(configKey) != null) {
					throw new OntologyLogicServiceException("Incompatible internal config DB: " + configKey
							+ ", previous value null, next value " + configToConfigMap.get(configKey));
				}
			} else {
				if (!configToConfigMap.containsKey(configKey) || configToConfigMap.get(configKey) == null
						|| !configToConfigMap.get(configKey).equals(configValue)) {
					throw new OntologyLogicServiceException("Incompatible internal config DB: " + configKey
							+ ", previous value " + configValue + ", next value "
							+ (!configToConfigMap.containsKey(configKey) || configToConfigMap.get(configKey) == null
									? "null"
									: configToConfigMap.get(configKey)));
				}
			}
		}
	}

	public void removeOntology(Ontology ontology) {

		try {
			if (log.isDebugEnabled()) {
				log.debug("remove ontology in db {}", ontology.getRtdbDatasource());
			}
			this.getInstance(ontology.getRtdbDatasource()).removeTable4Ontology(ontology.getIdentification());

		} catch (final Exception e) {

			throw new OntologyLogicServiceException("Problems removing the ontology." + e.getMessage(), e);
		}

		log.debug("ontology removed");
	}

	public List<String> getListOfTables4Ontology(Ontology ontology) {
		try {
			if (log.isDebugEnabled()) {
				log.debug("list tables for ontology in db {}", ontology.getRtdbDatasource());
			}
			return this.getInstance(ontology.getRtdbDatasource())
					.getListOfTables4Ontology(ontology.getIdentification());

		} catch (final Exception e) {

			throw new OntologyLogicServiceException("Problems listing tables for the ontology." + e.getMessage(), e);
		}

	}

	private ManageDBRepository getInstance(RtdbDatasource rtdbDatasource) {
		return manageDBPersistence.getInstance(rtdbDatasource);
	}

}
