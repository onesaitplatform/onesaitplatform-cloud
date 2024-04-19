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
package com.minsait.onesait.platform.config.services.kafka;

import java.util.Set;

import com.minsait.onesait.platform.config.model.ClientPlatformOntology;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Token;

public interface KafkaAuthorizationService {

	public void addAclToOntologyClient(ClientPlatformOntology clientPlatformOntology);

	public void addAclToOntologyOnClientCreation(ClientPlatformOntology clientPlatformOntology, Set<Token> tokens);

	public void removeAclToOntologyClient(ClientPlatformOntology clientPlatformOntology);

	public void checkOntologyAclAfterUpdate(Ontology ontology);

	public void addAclToClientForNewToken(Token token);

	public void deactivateToken(Token token, boolean active);

	public void removeAclToClientForToken(Token token);

	public void addAclToOntologyClient(ClientPlatformOntology clientPlatformOntology, boolean createInputTopic,
			boolean createNotificationTopic);

}