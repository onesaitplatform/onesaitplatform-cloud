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
package com.minsait.onesait.platform.business.services.ontology;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.minsait.onesait.platform.commons.kafka.KafkaService;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.services.exceptions.OntologyServiceException;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataJsonProblemException;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.persistence.services.util.OntologyLogicService;
import com.minsait.onesait.platform.persistence.services.util.OntologyLogicServiceException;

//@RunWith(MockitoJUnitRunner.StrictStubs.class)
@RunWith(MockitoJUnitRunner.class)
public class OntologyBusinessServiceImplTest {

	@InjectMocks
	OntologyBusinessServiceImpl service;

	@Mock
	private OntologyService ontologyService;

	@Mock
	private OntologyLogicService ontologyLogicService;

	@Mock
	private KafkaService kafkaService;

	@Mock
	private UserService userService;

	@Test()
	public void given_OntologyDataAndUser_When_OntologyIdentificationIsNotValid_Then_TheAnExceptionIsThrown() {
		when(ontologyService.isIdValid(any())).thenReturn(false);
		Ontology ontology = new Ontology();
		String userId = "me";
		try {
			service.createOntology(ontology, userId, null);
		} catch (OntologyBusinessServiceException e) {
			assertTrue("An invalid identifier should cause an exception",
					e.getError().equals(OntologyBusinessServiceException.Error.ILLEGAL_ARGUMENT));
		}
	}

	@Test()
	public void given_OntologyDataAndUser_When_OntologySchemaIsNotValid_Then_TheAnExceptionIsThrown() {
		Ontology ontology = new Ontology();
		ontology.setJsonSchema("{bad json schema}");
		String userId = "me";

		when(ontologyService.isIdValid(any())).thenReturn(true);
		doThrow(new OntologyDataJsonProblemException()).when(ontologyService).checkOntologySchema(any());

		try {
			service.createOntology(ontology, userId, null);
		} catch (OntologyBusinessServiceException e) {
			assertTrue("An invalid json schema should cause an exception",
					e.getError().equals(OntologyBusinessServiceException.Error.NO_VALID_SCHEMA));
		}
	}

	@Test()
	public void given_OntologyDataAndUser_When_OntologyWithKafkaTopicFailsCreationgTopic_Then_TheAnExceptionIsThrown() {
		Ontology ontology = new Ontology();
		ontology.setAllowsCreateTopic(true);
		String userId = "me";

		when(ontologyService.isIdValid(any())).thenReturn(true);
		when(kafkaService.createTopicForOntology(any())).thenReturn(false);

		try {
			service.createOntology(ontology, userId, null);
		} catch (OntologyBusinessServiceException e) {
			assertTrue("An error creating the kafka topic should cause an exception",
					e.getError().equals(OntologyBusinessServiceException.Error.KAFKA_TOPIC_CREATION_ERROR));
		}
	}

	@Test()
	public void given_OntologyDataAndUser_When_OntologyWithKafkaTopicFailsCreatingTheOntology_Then_TheAnExceptionIsThrown() {
		Ontology ontology = new Ontology();
		ontology.setAllowsCreateTopic(true);
		String userId = "me";

		when(ontologyService.isIdValid(any())).thenReturn(true);
		when(kafkaService.createTopicForOntology(any())).thenReturn(true);
		doThrow(new OntologyServiceException("Error creating ontology")).when(ontologyService).createOntology(any(),
				any());

		try {
			service.createOntology(ontology, userId, null);
		} catch (OntologyBusinessServiceException e) {
			verify(kafkaService, times(1)).deleteTopic(any());
			assertTrue("If an error happens creating the ontology config data, the kafka topic should be undo",
					e.getError().equals(OntologyBusinessServiceException.Error.CONFIG_CREATION_ERROR));
		}
	}

	@Test()
	public void given_OntologyDataAndUser_When_OntologyWithKafkaTopicFailsCreatingTheOntologyAndThenItFailsCleaningTheTopic_Then_TheAnExceptionIsThrown() {
		Ontology ontology = new Ontology();
		ontology.setAllowsCreateTopic(true);
		String userId = "me";

		when(ontologyService.isIdValid(any())).thenReturn(true);
		when(kafkaService.createTopicForOntology(any())).thenReturn(true);
		doThrow(new OntologyServiceException("Error creating ontology")).when(ontologyService).createOntology(any(),
				any());
		doThrow(new RuntimeException()).when(kafkaService).deleteTopic(any());
		try {
			service.createOntology(ontology, userId, null);
		} catch (OntologyBusinessServiceException e) {
			assertTrue("An exception should be thrown when it is not possible to undo the kafka topic creation",
					e.getError().equals(OntologyBusinessServiceException.Error.CONFIG_CREATION_ERROR_UNCLEAN));
		}
	}

	@Test()
	public void given_OntologyDataAndUser_When_OntologyWithKafkaTopicFailsCreatingTheOntologyInPersistence_Then_TheAnExceptionIsThrown() {
		Ontology ontology = new Ontology();
		ontology.setAllowsCreateTopic(true);
		String userId = "me";

		when(ontologyService.isIdValid(any())).thenReturn(true);
		when(kafkaService.createTopicForOntology(any())).thenReturn(true);
		doThrow(new OntologyLogicServiceException("Error creating ontology")).when(ontologyLogicService)
				.createOntology(any(),any());
		try {
			service.createOntology(ontology, userId, null);
		} catch (OntologyBusinessServiceException e) {
			verify(kafkaService, times(1)).deleteTopic(any());
			assertTrue("An exception should be thrown when the ontology is not created in the persistence layer",
					e.getError().equals(OntologyBusinessServiceException.Error.PERSISTENCE_CREATION_ERROR));
		}
	}

	@Test()
	public void given_OntologyDataAndUser_When_OntologyWithKafkaTopicFailsCreatingTheOntologyInPersistenceAndThenItFailsCleaningTheTopic_Then_TheAnExceptionIsThrown() {
		Ontology ontology = new Ontology();
		ontology.setAllowsCreateTopic(true);
		String userId = "me";

		when(ontologyService.isIdValid(any())).thenReturn(true);
		when(kafkaService.createTopicForOntology(any())).thenReturn(true);
		doThrow(new OntologyLogicServiceException("Error creating ontology")).when(ontologyLogicService)
				.createOntology(any(),any());
		doThrow(new RuntimeException()).when(kafkaService).deleteTopic(any());
		try {
			service.createOntology(ontology, userId, null);
		} catch (OntologyBusinessServiceException e) {
			assertTrue("An exception should be thrown when it is not possible to undo the kafka topic creation",
					e.getError().equals(OntologyBusinessServiceException.Error.PERSISTENCE_CREATION_ERROR_UNCLEAN));
		}
	}

}
