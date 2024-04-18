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
package com.minsait.onesait.platform.config.services.twitter;

import java.util.List;

import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.TwitterListening;
import com.minsait.onesait.platform.config.model.User;

public interface TwitterListeningService {

	List<TwitterListening> getAllListenings();

	List<TwitterListening> getAllListeningsByUser(String userId);

	TwitterListening getListenById(String id);

	TwitterListening getListenByIdentificator(String identificator);

	List<Configuration> getAllConfigurations();

	List<Configuration> getConfigurationsByUserId(String userId);

	List<String> getClientsFromOntology(String ontologyId, String userSessionId);

	List<String> getTokensFromClient(String clientPlatformId);

	boolean existOntology(String identification, String userSessionId);

	boolean existClientPlatform(String identification);

	Ontology createTwitterOntology(String ontologyId);

	TwitterListening createListening(TwitterListening twitterListening, String userSessionId);

	void updateListening(TwitterListening twitterListening);

	TwitterListening getListeningByJobName(String jobName);

	List<String> getAllClientsForUser(User userSessionId);

}
