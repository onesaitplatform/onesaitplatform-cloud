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
package com.minsait.onesait.platform.persistence.services.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.services.exceptions.OntologyServiceException;
import com.minsait.onesait.platform.persistence.services.ManageDBPersistenceServiceFacade;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OntologyLogicService {

	@Autowired
	private ManageDBPersistenceServiceFacade manageDBPersistenceServiceFacade;

	public void createOntology(Ontology ontology, Map<String, String> config) {

		try {

			log.debug("create ontology in db " + ontology.getRtdbDatasource());
			manageDBPersistenceServiceFacade.createTable4Ontology(ontology.getIdentification(),
					ontology.getJsonSchema(), config);

		} catch (final Exception e) {

			throw new OntologyServiceException("Problems creating the ontology." + e.getMessage(), e);
		}

		log.debug("ontology created");
	}

	public Map<String, String> getAdditionalDBConfig(Ontology ontology) {
		log.debug("Get internal config in ontology " + ontology.getRtdbDatasource());
		try {
			return manageDBPersistenceServiceFacade.getAdditionalDBConfig(ontology.getIdentification());
		} catch (final Exception e) {
			throw new OntologyLogicServiceException(
					"Problems getting internal DB config of the ontology." + e.getMessage(), e);
		}
	}

	public void updateOntology(Ontology ontology, HashMap<String, String> config) {
		try {

			log.debug("update ontology in db " + ontology.getRtdbDatasource());
			manageDBPersistenceServiceFacade.updateTable4Ontology(ontology.getIdentification(),
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

}
