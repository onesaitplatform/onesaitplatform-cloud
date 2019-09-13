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
package com.minsait.onesait.platform.business.services.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.DataModel;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.DataModelRepository;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.config.services.utils.ServiceUtils;
import com.minsait.onesait.platform.persistence.services.ManageDBPersistenceServiceFacade;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserOperationsService {
	private static final String ANONYMOUS_USER = "anonymous";
	private static final String ADMIN_USER = "administrator";
	@Autowired
	private ManageDBPersistenceServiceFacade manageFacade;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private DataModelRepository dataModelRepository;

	@Autowired
	private UserService userService;

	public String getAuditOntology(String userId) {
		return ServiceUtils.getAuditCollectionName(userId);
	}

	public void createAuditOntology(String userId) {
		final User user = userService.getUser(userId);
		if (user != null) {
			createPostOperationsUser(user);
			createPostOntologyUser(user);
		} else if (userId.equals(ANONYMOUS_USER)) {
			createPostOperationsUser(userService.getUserByIdentification(ADMIN_USER), true);
			createPostOntologyUser(userService.getUserByIdentification(ADMIN_USER), true);
		}
	}

	public void createPostOperationsUser(User user) {

		this.createPostOperationsUser(user, false);

	}

	public void createPostOperationsUser(User user, boolean anonymous) {
		final String collectionAuditName;
		if (anonymous)
			collectionAuditName = ServiceUtils.getAuditCollectionName(ANONYMOUS_USER);
		else
			collectionAuditName = ServiceUtils.getAuditCollectionName(user.getUserId());

		if (ontologyService.getOntologyByIdentification(collectionAuditName, user.getUserId()) == null) {
			final DataModel dataModel = dataModelRepository.findByName("AuditPlatform").get(0);
			final Ontology ontology = new Ontology();
			ontology.setJsonSchema(dataModel.getJsonSchema());
			ontology.setIdentification(collectionAuditName);
			ontology.setDataModel(dataModel);
			ontology.setDescription(
					"System Ontology. Auditory of operations between user and Platform for user: " + user.getUserId());
			ontology.setActive(true);
			ontology.setRtdbClean(true);
			ontology.setRtdbToHdb(true);
			ontology.setPublic(false);
			ontology.setUser(user);
			ontology.setRtdbDatasource(RtdbDatasource.ELASTIC_SEARCH);

			ontologyService.createOntology(ontology, null);

		}

	}

	private void update(User user, RtdbDatasource datasource, boolean anonymous) {
		final String collectionAuditName;
		if (anonymous)
			collectionAuditName = ServiceUtils.getAuditCollectionName(ANONYMOUS_USER);
		else
			collectionAuditName = ServiceUtils.getAuditCollectionName(user.getUserId());

		final Ontology ontology = ontologyService.getOntologyByIdentification(collectionAuditName, user.getUserId());
		ontology.setRtdbDatasource(datasource);

		ontologyService.updateOntology(ontology, user.getUserId(), null);
	}

	public void createPostOntologyUser(User user, boolean anonymous) {
		final String collectionAuditName;
		if (anonymous)
			collectionAuditName = ServiceUtils.getAuditCollectionName(ANONYMOUS_USER);
		else
			collectionAuditName = ServiceUtils.getAuditCollectionName(user.getUserId());
		final DataModel dataModel = dataModelRepository.findByName("AuditPlatform").get(0);
		try {
			manageFacade.createTable4Ontology(collectionAuditName, dataModel.getJsonSchema(), null);
		} catch (final Exception e) {
			log.error("Audit ontology couldn't be created in ElasticSearch, so we need Mongo to Store Something");
			update(user, RtdbDatasource.MONGO, anonymous);
			manageFacade.createTable4Ontology(collectionAuditName, dataModel.getJsonSchema(),null);
		}

	}

	public void createPostOntologyUser(User user) {

		this.createPostOntologyUser(user, false);
	}

}
