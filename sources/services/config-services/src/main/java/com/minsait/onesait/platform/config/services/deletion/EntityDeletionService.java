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
package com.minsait.onesait.platform.config.services.deletion;

import java.util.List;

import com.minsait.onesait.platform.config.model.ClientPlatformInstanceSimulation;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Token;
import com.minsait.onesait.platform.config.model.TwitterListening;

public interface EntityDeletionService {

	void deleteOntology(String id, String userId);

	void deleteTwitterListening(TwitterListening twitterListening);

	void deleteClient(String id);

	void deleteToken(Token token);

	void deleteDeviceSimulation(ClientPlatformInstanceSimulation simulation) throws Exception;

	void revokeAuthorizations(Ontology ontology);

	void deleteGadgetDataSource(String id, String userId);

	void deleteUser(String userId);

	void deleteQueryTemplate(String id);

	void deactivateUser(String userId);

	void deactivateUser(List<String> userIds);

	void invalidateUserTokens(String userId);
}
